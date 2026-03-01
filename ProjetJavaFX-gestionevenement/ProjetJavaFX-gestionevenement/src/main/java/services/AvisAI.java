package services;

import Modele.Avis;
import utiles.mydb;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class AvisAI {
    private static final String AVIS_SCHEMA = "doua";
    
    // Positive and negative word dictionaries for sentiment analysis
    private static final Set<String> POSITIVE_WORDS = new HashSet<>(Arrays.asList(
        "excellent", "super", "génial", "parfait", "magnifique", "fabuleux", "incroyable",
        "awesome", "amazing", "wonderful", "fantastic", "great", "good", "nice", "love",
        "beautiful", "belle", "beau", "bon", "meilleur", "top", "bravo", "félicitations",
        "satisfait", "content", "heureux", "plaisir", "agrégable", "recommande", "à voir"
    ));
    
    private static final Set<String> NEGATIVE_WORDS = new HashSet<>(Arrays.asList(
        "mauvais", "terrible", "décevant", "nul", "horrible", "catastrophique", "affreux",
        "awful", "bad", "terrible", "horrible", "disappointing", "worst", "hate", "ugly",
        "moche", "null", "déçu", "pas", "non", "jamais", "éviter", "déconseille", "nul",
        "chiant", "ennuyeux", "long", "barbant", "perte", "temps", "argent", "volé"
    ));
    
    // Review authenticity indicators
    private static final Set<String> SPAM_INDICATORS = new HashSet<>(Arrays.asList(
        "click here", "buy now", "free", "win", "prize", "money", "cash", "$$$",
        "http", "www", ".com", "link", "offer", "discount", "promo", "sale"
    ));
    
    public double analyzeSentiment(String commentaire) {
        if (commentaire == null || commentaire.trim().isEmpty()) {
            return 0.0; // Neutral sentiment for empty comments
        }
        
        String text = commentaire.toLowerCase();
        String[] words = text.split("\\s+");
        
        int positiveCount = 0;
        int negativeCount = 0;
        
        for (String word : words) {
            // Remove punctuation
            word = word.replaceAll("[^a-zA-Zàâäéèêëïîôöùûüÿç]", "");
            
            if (POSITIVE_WORDS.contains(word)) {
                positiveCount++;
            } else if (NEGATIVE_WORDS.contains(word)) {
                negativeCount++;
            }
        }
        
        // Handle negations
        int negationCount = countNegations(text);
        negativeCount += negationCount;
        
        // Calculate sentiment score
        int totalSentimentWords = positiveCount + negativeCount;
        if (totalSentimentWords == 0) {
            return 0.0; // Neutral if no sentiment words found
        }
        
        double sentimentScore = (double) (positiveCount - negativeCount) / Math.sqrt(totalSentimentWords);
        
        // Normalize to [-1, 1] range
        return Math.max(-1.0, Math.min(1.0, sentimentScore));
    }
    
    public double calculateAuthenticityScore(Avis avis) {
        double score = 1.0; // Start with perfect score
        
        // Factor 1: Comment length
        String commentaire = avis.getCommentaire();
        if (commentaire == null || commentaire.trim().isEmpty()) {
            score -= 0.5;
        } else if (commentaire.length() < 10) {
            score -= 0.3;
        } else if (commentaire.length() > 1000) {
            score -= 0.2; // Unusually long reviews might be spam
        }
        
        // Factor 2: Spam indicators
        score -= calculateSpamPenalty(commentaire);
        
        // Factor 3: Rating consistency
        double sentimentScore = analyzeSentiment(commentaire);
        int rating = avis.getNote();
        double expectedRating = convertSentimentToRating(sentimentScore);
        double ratingDifference = Math.abs(rating - expectedRating);
        score -= Math.min(0.3, ratingDifference * 0.1);
        
        // Factor 4: User review history
        int userReviewCount = getUserReviewCount(avis.getUtilisateurId());
        if (userReviewCount == 1) {
            score -= 0.1; // First-time reviewer, slightly lower trust
        } else if (userReviewCount > 50) {
            score -= 0.2; // Very active reviewer might be professional
        }
        
        // Factor 5: Timing patterns
        if (hasSuspiciousTiming(avis)) {
            score -= 0.3;
        }
        
        return Math.max(0.0, Math.min(1.0, score));
    }
    
    public Map<String, List<Avis>> clusterReviews(List<Avis> reviews) {
        Map<String, List<Avis>> clusters = new HashMap<>();
        
        for (Avis avis : reviews) {
            String category = categorizeReview(avis);
            clusters.computeIfAbsent(category, k -> new ArrayList<>()).add(avis);
        }
        
        // Sort clusters by size (largest first)
        return clusters.entrySet().stream()
            .sorted(Map.Entry.<String, List<Avis>>comparingByValue(
                Comparator.comparingInt(List::size)).reversed())
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }
    
    public String generateReviewSummary(List<Avis> reviews) {
        if (reviews.isEmpty()) {
            return "No reviews available.";
        }
        
        // Calculate basic statistics
        double avgRating = reviews.stream()
            .mapToInt(Avis::getNote)
            .average()
            .orElse(0.0);
            
        double avgSentiment = reviews.stream()
            .mapToDouble(avis -> analyzeSentiment(avis.getCommentaire()))
            .average()
            .orElse(0.0);
        
        // Cluster reviews to understand themes
        Map<String, List<Avis>> clusters = clusterReviews(reviews);
        
        // Generate summary
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("Overall Rating: %.1f/5.0 (%d reviews)\n", 
            avgRating, reviews.size()));
        
        // Add sentiment analysis
        String sentimentDesc = getSentimentDescription(avgSentiment);
        summary.append(String.format("Overall Sentiment: %s\n", sentimentDesc));
        
        // Add main themes
        if (clusters.size() > 1) {
            summary.append("Main Themes: ");
            List<String> themes = clusters.keySet().stream()
                .limit(3)
                .collect(Collectors.toList());
            summary.append(String.join(", ", themes));
            summary.append("\n");
        }
        
        // Add most common positive and negative points
        Map<String, Integer> positivePoints = extractKeyPoints(reviews, true);
        Map<String, Integer> negativePoints = extractKeyPoints(reviews, false);
        
        if (!positivePoints.isEmpty()) {
            String topPositive = positivePoints.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("");
            summary.append(String.format("Most Praised: %s\n", topPositive));
        }
        
        if (!negativePoints.isEmpty()) {
            String topNegative = negativePoints.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("");
            summary.append(String.format("Main Concern: %s\n", topNegative));
        }
        
        return summary.toString();
    }
    
    private String categorizeReview(Avis avis) {
        String commentaire = avis.getCommentaire();
        if (commentaire == null) return "General";
        
        String text = commentaire.toLowerCase();
        int rating = avis.getNote();
        
        // Category based on rating
        if (rating >= 4) {
            return "Positive";
        } else if (rating <= 2) {
            return "Negative";
        } else {
            return "Neutral";
        }
        
        // More sophisticated categorization could be added here
        // based on content analysis (e.g., "Service", "Venue", "Content", etc.)
    }
    
    private int countNegations(String text) {
        String[] negations = {"pas", "ne", "jamais", "rien", "nul", "sans", "aucun", "ni"};
        int count = 0;
        
        for (String negation : negations) {
            if (text.contains(negation)) {
                count++;
            }
        }
        
        return count;
    }
    
    private double calculateSpamPenalty(String commentaire) {
        if (commentaire == null) return 0.0;
        
        String text = commentaire.toLowerCase();
        double penalty = 0.0;
        
        // Check for spam indicators
        for (String indicator : SPAM_INDICATORS) {
            if (text.contains(indicator)) {
                penalty += 0.2;
            }
        }
        
        // Check for excessive capitalization
        int upperCaseCount = (int) text.chars()
            .filter(Character::isUpperCase)
            .count();
        if (upperCaseCount > text.length() * 0.3) {
            penalty += 0.2;
        }
        
        // Check for excessive punctuation
        int punctuationCount = (int) text.chars()
            .filter(c -> !Character.isLetterOrDigit(c) && !Character.isWhitespace(c))
            .count();
        if (punctuationCount > text.length() * 0.1) {
            penalty += 0.1;
        }
        
        return Math.min(0.5, penalty);
    }
    
    private double convertSentimentToRating(double sentimentScore) {
        // Convert sentiment score [-1, 1] to rating [1, 5]
        return 3.0 + (sentimentScore * 2.0);
    }
    
    private int getUserReviewCount(int userId) {
        String sql = "SELECT COUNT(*) FROM " + AVIS_SCHEMA + ".avis WHERE utilisateur_id = ?";
        
        try (Connection cnx = mydb.getInstance().getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting user review count: " + e.getMessage());
        }
        
        return 0;
    }
    
    private boolean hasSuspiciousTiming(Avis avis) {
        // Check if review was made very quickly after reservation
        String sql = "SELECT TIMESTAMPDIFF(MINUTE, r.dateReservation, a.date_avis) " +
                     "FROM " + AVIS_SCHEMA + ".avis a JOIN reservation r ON a.idReservation = r.idReservation " +
                     "WHERE a.idAvis = ?";
        
        try (Connection cnx = mydb.getInstance().getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            
            ps.setInt(1, avis.getIdAvis());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int minutesDiff = rs.getInt(1);
                    return minutesDiff < 5; // Suspicious if reviewed within 5 minutes
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking review timing: " + e.getMessage());
        }
        
        return false;
    }
    
    private String getSentimentDescription(double sentimentScore) {
        if (sentimentScore > 0.5) return "Very Positive";
        if (sentimentScore > 0.2) return "Positive";
        if (sentimentScore > -0.2) return "Neutral";
        if (sentimentScore > -0.5) return "Negative";
        return "Very Negative";
    }
    
    private Map<String, Integer> extractKeyPoints(List<Avis> reviews, boolean positive) {
        Map<String, Integer> keyPoints = new HashMap<>();
        
        // Simple keyword extraction
        Set<String> keywords = positive ? POSITIVE_WORDS : NEGATIVE_WORDS;
        
        for (Avis avis : reviews) {
            String commentaire = avis.getCommentaire();
            if (commentaire == null) continue;
            
            String text = commentaire.toLowerCase();
            for (String keyword : keywords) {
                if (text.contains(keyword)) {
                    keyPoints.merge(keyword, 1, Integer::sum);
                }
            }
        }
        
        return keyPoints;
    }
    
    // Database helper methods for getting reviews
    public List<Avis> getReviewsByEvent(int eventId) {
        List<Avis> reviews = new ArrayList<>();
        String sql = "SELECT a.* FROM " + AVIS_SCHEMA + ".avis a " +
                     "JOIN reservation r ON a.idReservation = r.idReservation " +
                     "WHERE r.idEvenement = ?";
        
        try (Connection cnx = mydb.getInstance().getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Avis avis = new Avis();
                    avis.setIdAvis(rs.getInt("idAvis"));
                    avis.setIdReservation(rs.getInt("idReservation"));
                    avis.setUtilisateurId(rs.getInt("utilisateur_id"));
                    avis.setNote(rs.getInt("note"));
                    avis.setCommentaire(rs.getString("commentaire"));
                    avis.setDateAvis(rs.getTimestamp("date_avis").toLocalDateTime());
                    reviews.add(avis);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting reviews by event: " + e.getMessage());
        }
        
        return reviews;
    }
    
    public void updateReviewWithAI(int avisId) {
        String sql = "SELECT * FROM " + AVIS_SCHEMA + ".avis WHERE idAvis = ?";
        
        try (Connection cnx = mydb.getInstance().getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            
            ps.setInt(1, avisId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Avis avis = new Avis();
                    avis.setIdAvis(avisId);
                    avis.setIdReservation(rs.getInt("idReservation"));
                    avis.setUtilisateurId(rs.getInt("utilisateur_id"));
                    avis.setNote(rs.getInt("note"));
                    avis.setCommentaire(rs.getString("commentaire"));
                    avis.setDateAvis(rs.getTimestamp("date_avis").toLocalDateTime());
                    
                    // Calculate AI scores
                    double sentimentScore = analyzeSentiment(avis.getCommentaire());
                    double authenticityScore = calculateAuthenticityScore(avis);
                    
                    // Update database with AI scores
                    updateAIScores(avisId, sentimentScore, authenticityScore);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error updating review with AI: " + e.getMessage());
        }
    }
    
    private void updateAIScores(int avisId, double sentimentScore, double authenticityScore) {
        String sql = "UPDATE " + AVIS_SCHEMA + ".avis SET sentiment_score = ?, authenticity_score = ? WHERE idAvis = ?";
        
        try (Connection cnx = mydb.getInstance().getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            
            ps.setDouble(1, sentimentScore);
            ps.setDouble(2, authenticityScore);
            ps.setInt(3, avisId);
            ps.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error updating AI scores: " + e.getMessage());
        }
    }
}
