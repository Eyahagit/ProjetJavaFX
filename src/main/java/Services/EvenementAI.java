package Services;

import Models.Evenement;
import utils.Database;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class EvenementAI {
    private static final String AVIS_SCHEMA = "doua";
    private Connection connection;
    
    public EvenementAI(Connection connection) {
        this.connection = connection;
    }
    
    public EvenementAI() {
        this.connection = Database.getInstance().getConnection();
    }
    
    public List<Evenement> recommendEvents(int userId) {
        List<Evenement> allEvents = getAllEvents();
        List<Integer> userEventIds = getUserAttendedEvents(userId);
        
        // Build user preference vector from attended events
        String userPreferences = buildUserPreferenceVector(userEventIds);
        
        // Score events based on content similarity
        return allEvents.stream()
            .filter(e -> !userEventIds.contains(e.getIdEvenement()))
            .sorted((e1, e2) -> Double.compare(
                calculateContentSimilarity(userPreferences, e2),
                calculateContentSimilarity(userPreferences, e1)
            ))
            .limit(5)
            .collect(Collectors.toList());
    }
    
    public double calculateDynamicPrice(int eventId) {
        Evenement event = getEventById(eventId);
        if (event == null) return 0.0;
        
        double basePrice = getBasePrice(eventId);
        double demandFactor = calculateDemandFactor(eventId);
        double timeFactor = calculateTimeFactor(event.getDate());
        double popularityFactor = calculatePopularityFactor(eventId);
        
        return basePrice * demandFactor * timeFactor * popularityFactor;
    }
    
    public int predictAttendance(int eventId) {
        List<Integer> historicalData = getHistoricalAttendance(eventId);
        if (historicalData.isEmpty()) return 50; // Default prediction
        
        // Simple moving average with trend
        double average = historicalData.stream()
            .mapToInt(Integer::intValue)
            .average()
            .orElse(50.0);
            
        // Apply growth factor based on recent trend
        double trendFactor = calculateTrendFactor(historicalData);
        
        return (int) (average * trendFactor);
    }
    
    private String buildUserPreferenceVector(List<Integer> eventIds) {
        StringBuilder preferences = new StringBuilder();
        for (int eventId : eventIds) {
            Evenement event = getEventById(eventId);
            if (event != null) {
                preferences.append(event.getTitre()).append(" ")
                          .append(event.getDescription()).append(" ")
                          .append(event.getLocalisation()).append(" ");
            }
        }
        return preferences.toString().toLowerCase();
    }
    
    private double calculateContentSimilarity(String userPreferences, Evenement event) {
        String eventContent = (event.getTitre() + " " + 
                              event.getDescription() + " " + 
                              event.getLocalisation()).toLowerCase();
        
        // Simple word-based similarity
        Set<String> userWords = Arrays.stream(userPreferences.split("\\s+"))
            .filter(w -> w.length() > 3)
            .collect(Collectors.toSet());
            
        Set<String> eventWords = Arrays.stream(eventContent.split("\\s+"))
            .filter(w -> w.length() > 3)
            .collect(Collectors.toSet());
            
        Set<String> intersection = new HashSet<>(userWords);
        intersection.retainAll(eventWords);
        
        Set<String> union = new HashSet<>(userWords);
        union.addAll(eventWords);
        
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }
    
    private double calculateDemandFactor(int eventId) {
        int currentReservations = getCurrentReservationCount(eventId);
        int maxCapacity = getMaxCapacity(eventId);
        
        if (maxCapacity == 0) return 1.0;
        
        double occupancyRate = (double) currentReservations / maxCapacity;
        
        // Increase price as occupancy increases
        if (occupancyRate > 0.9) return 1.5;
        if (occupancyRate > 0.7) return 1.3;
        if (occupancyRate > 0.5) return 1.1;
        return 1.0;
    }
    
    private double calculateTimeFactor(LocalDate eventDate) {
        long daysUntilEvent = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), eventDate);
        
        // Last-minute pricing
        if (daysUntilEvent <= 1) return 1.4;
        if (daysUntilEvent <= 3) return 1.2;
        if (daysUntilEvent <= 7) return 1.1;
        
        // Early bird discount
        if (daysUntilEvent > 30) return 0.9;
        if (daysUntilEvent > 60) return 0.8;
        
        return 1.0;
    }
    
    private double calculatePopularityFactor(int eventId) {
        double avgRating = getAverageRating(eventId);
        double totalReviews = getTotalReviews(eventId);
        
        // Higher rating and more reviews increase price
        double ratingFactor = 0.8 + (avgRating / 5.0) * 0.4; // 0.8 to 1.2
        double reviewFactor = Math.min(1.2, 1.0 + (totalReviews / 50.0) * 0.2); // Cap at 1.2
        
        return ratingFactor * reviewFactor;
    }
    
    private double calculateTrendFactor(List<Integer> historicalData) {
        if (historicalData.size() < 2) return 1.0;
        
        // Simple linear trend calculation
        int n = historicalData.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        
        for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += historicalData.get(i);
            sumXY += i * historicalData.get(i);
            sumX2 += i * i;
        }
        
        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double trendFactor = 1.0 + (slope / historicalData.get(0)); // Relative to first value
        
        return Math.max(0.5, Math.min(1.5, trendFactor)); // Clamp between 0.5 and 1.5
    }
    
    // Database helper methods
    private List<Evenement> getAllEvents() {
        List<Evenement> events = new ArrayList<>();
        String sql = "SELECT * FROM evenement ORDER BY date";
        
        try (Connection cnx = getConnection();
             Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            while (rs.next()) {
                Evenement e = new Evenement();
                e.setIdEvenement(rs.getInt("idEvenement"));
                e.setTitre(rs.getString("titre"));
                e.setDescription(rs.getString("description"));
                e.setDate(rs.getDate("date").toLocalDate());
                e.setLocalisation(rs.getString("localisation"));
                events.add(e);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all events: " + e.getMessage());
        }
        
        return events;
    }
    
    private List<Integer> getUserAttendedEvents(int userId) {
        List<Integer> eventIds = new ArrayList<>();
        String sql = "SELECT DISTINCT idEvenement FROM reservation WHERE utilisateur_id = ?";
        
        try (Connection cnx = getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    eventIds.add(rs.getInt("idEvenement"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting user events: " + e.getMessage());
        }
        
        return eventIds;
    }
    
    private Evenement getEventById(int eventId) {
        String sql = "SELECT * FROM evenement WHERE idEvenement = ?";
        
        try (Connection cnx = getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Evenement e = new Evenement();
                    e.setIdEvenement(rs.getInt("idEvenement"));
                    e.setTitre(rs.getString("titre"));
                    e.setDescription(rs.getString("description"));
                    e.setDate(rs.getDate("date").toLocalDate());
                    e.setLocalisation(rs.getString("localisation"));
                    return e;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting event by ID: " + e.getMessage());
        }
        
        return null;
    }
    
    private double getBasePrice(int eventId) {
        // Default base price - in real system this would come from database
        return 50.0; // Base price in currency units
    }
    
    private int getCurrentReservationCount(int eventId) {
        String sql = "SELECT COUNT(*) FROM reservation WHERE idEvenement = ?";
        
        try (Connection cnx = getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting reservation count: " + e.getMessage());
        }
        
        return 0;
    }
    
    private int getMaxCapacity(int eventId) {
        // Default capacity - in real system this would come from database
        return 100; // Default capacity
    }
    
    private double getAverageRating(int eventId) {
        String sql = "SELECT AVG(note) FROM " + AVIS_SCHEMA + ".avis WHERE idReservation IN " +
                     "(SELECT idReservation FROM reservation WHERE idEvenement = ?)";
        
        try (Connection cnx = getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting average rating: " + e.getMessage());
        }
        
        return 3.0; // Default rating
    }
    
    private double getTotalReviews(int eventId) {
        String sql = "SELECT COUNT(*) FROM " + AVIS_SCHEMA + ".avis WHERE idReservation IN " +
                     "(SELECT idReservation FROM reservation WHERE idEvenement = ?)";
        
        try (Connection cnx = getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting total reviews: " + e.getMessage());
        }
        
        return 0.0;
    }
    
    private List<Integer> getHistoricalAttendance(int eventId) {
        List<Integer> attendance = new ArrayList<>();
        // This would typically come from historical data
        // For now, return some sample data
        attendance.add(45);
        attendance.add(52);
        attendance.add(48);
        attendance.add(58);
        attendance.add(62);
        
        return attendance;
    }
    
    private Connection getConnection() {
        if (connection == null) {
            connection = Database.getInstance().getConnection();
        }
        return connection;
    }
}
