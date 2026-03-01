package Services;

import Models.Reservation;
import utils.mydb;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class ReservationAI {
    
    public boolean isSuspicious(Reservation reservation) {
        double avgGroupSize = getAverageGroupSize(reservation.getIdEvenement());
        double avgAdvanceTime = getAverageBookingAdvance(reservation.getIdEvenement());
        int userRecentReservations = getUserRecentReservations(reservation.getUtilisateurId());
        
        // Check for multiple risk factors
        int riskScore = 0;
        
        // Unusually large group
        if (reservation.getNombrePersonnes() > avgGroupSize * 3) {
            riskScore += 2;
        }
        
        // Very last-minute booking
        long hoursUntilEvent = getHoursUntilEvent(reservation.getIdEvenement());
        if (hoursUntilEvent > 0 && hoursUntilEvent < avgAdvanceTime * 0.1) {
            riskScore += 2;
        }
        
        // Multiple recent reservations from same user
        if (userRecentReservations > 5) {
            riskScore += 3;
        }
        
        // Suspicious email pattern
        if (isSuspiciousEmail(reservation.getEmail())) {
            riskScore += 2;
        }
        
        // Invalid phone number format
        if (!isValidPhoneNumber(reservation.getTelephone())) {
            riskScore += 1;
        }
        
        return riskScore >= 3; // Flag as suspicious if 3+ risk factors
    }
    
    public int predictDemand(int eventId, LocalDateTime targetDate) {
        double seasonality = getSeasonalityFactor(eventId, targetDate);
        double trend = getTrendFactor(eventId);
        double baseDemand = getBaseDemand(eventId);
        double dayOfWeekFactor = getDayOfWeekFactor(targetDate);
        double timeOfDayFactor = getTimeOfDayFactor(targetDate);
        
        double predictedDemand = baseDemand * seasonality * trend * dayOfWeekFactor * timeOfDayFactor;
        
        return (int) Math.max(1, Math.round(predictedDemand));
    }
    
    public String allocateIntelligentSeats(int eventId, int numberOfPeople) {
        // Simulate intelligent seat allocation
        String venueLayout = getVenueLayout(eventId);
        List<String> availableSeats = getAvailableSeats(eventId);
        
        if (availableSeats.size() < numberOfPeople) {
            return "Insufficient seats available";
        }
        
        // Group seats together when possible
        List<String> allocatedSeats = new ArrayList<>();
        String currentSection = "";
        
        for (String seat : availableSeats) {
            String section = seat.substring(0, 1); // Assume format like "A1", "A2", "B1", etc.
            
            if (allocatedSeats.isEmpty()) {
                currentSection = section;
                allocatedSeats.add(seat);
            } else if (section.equals(currentSection) && allocatedSeats.size() < numberOfPeople) {
                allocatedSeats.add(seat);
            } else if (allocatedSeats.size() >= numberOfPeople) {
                break;
            }
        }
        
        // If we couldn't find enough seats in one section, take any available seats
        if (allocatedSeats.size() < numberOfPeople) {
            allocatedSeats.clear();
            for (int i = 0; i < Math.min(numberOfPeople, availableSeats.size()); i++) {
                allocatedSeats.add(availableSeats.get(i));
            }
        }
        
        return String.join(", ", allocatedSeats);
    }
    
    public double calculateFraudProbability(Reservation reservation) {
        double probability = 0.0;
        
        // Factor 1: Unusual booking pattern
        double patternScore = calculatePatternScore(reservation);
        probability += patternScore * 0.3;
        
        // Factor 2: User history
        double userScore = calculateUserRiskScore(reservation.getUtilisateurId());
        probability += userScore * 0.4;
        
        // Factor 3: Event-specific risk
        double eventScore = calculateEventRiskScore(reservation.getIdEvenement());
        probability += eventScore * 0.2;
        
        // Factor 4: Temporal patterns
        double temporalScore = calculateTemporalRiskScore(reservation);
        probability += temporalScore * 0.1;
        
        return Math.min(1.0, Math.max(0.0, probability));
    }
    
    private double calculatePatternScore(Reservation reservation) {
        double score = 0.0;
        
        // Check group size anomaly
        double avgGroupSize = getAverageGroupSize(reservation.getIdEvenement());
        if (reservation.getNombrePersonnes() > avgGroupSize * 2.5) {
            score += 0.3;
        }
        
        // Check booking timing
        long hoursUntilEvent = getHoursUntilEvent(reservation.getIdEvenement());
        if (hoursUntilEvent > 0 && hoursUntilEvent < 2) { // Less than 2 hours before event
            score += 0.4;
        }
        
        // Check email domain
        String emailDomain = reservation.getEmail().split("@")[1].toLowerCase();
        if (isDisposableEmailDomain(emailDomain)) {
            score += 0.5;
        }
        
        return Math.min(1.0, score);
    }
    
    private double calculateUserRiskScore(int userId) {
        // Check user's reservation history
        int totalReservations = getUserTotalReservations(userId);
        int cancelledReservations = getUserCancelledReservations(userId);
        int suspiciousReservations = getUserSuspiciousReservations(userId);
        
        if (totalReservations == 0) return 0.1; // New user, low risk
        
        double cancellationRate = (double) cancelledReservations / totalReservations;
        double suspiciousRate = (double) suspiciousReservations / totalReservations;
        
        double score = cancellationRate * 0.5 + suspiciousRate * 0.8;
        
        return Math.min(1.0, score);
    }
    
    private double calculateEventRiskScore(int eventId) {
        // High-demand events are more attractive to fraudsters
        double demandRatio = getCurrentDemandRatio(eventId);
        double eventPrice = getEventPrice(eventId);
        
        double score = 0.0;
        if (demandRatio > 0.9) score += 0.3; // Very high demand
        if (eventPrice > 100) score += 0.2; // Expensive event
        
        return Math.min(1.0, score);
    }
    
    private double calculateTemporalRiskScore(Reservation reservation) {
        double score = 0.0;
        
        // Multiple reservations in short time period
        int recentReservations = getUserReservationsInLastHour(reservation.getUtilisateurId());
        if (recentReservations > 3) {
            score += 0.6;
        }
        
        // Booking at unusual hours (e.g., 2-4 AM)
        int hour = reservation.getDateReservation().getHour();
        if (hour >= 2 && hour <= 4) {
            score += 0.2;
        }
        
        return Math.min(1.0, score);
    }
    
    // Database helper methods
    private double getAverageGroupSize(int eventId) {
        String sql = "SELECT AVG(nombrePersonnes) FROM reservation WHERE idEvenement = ?";
        
        try (Connection cnx = mydb.getInstance().getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting average group size: " + e.getMessage());
        }
        
        return 2.0; // Default average
    }
    
    private double getAverageBookingAdvance(int eventId) {
        String sql = "SELECT AVG(TIMESTAMPDIFF(HOUR, dateReservation, " +
                     "(SELECT date FROM evenement WHERE idEvenement = ?))) " +
                     "FROM reservation WHERE idEvenement = ?";
        
        try (Connection cnx = mydb.getInstance().getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            
            ps.setInt(1, eventId);
            ps.setInt(2, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting average booking advance: " + e.getMessage());
        }
        
        return 72.0; // Default: 3 days in hours
    }
    
    private int getUserRecentReservations(int userId) {
        String sql = "SELECT COUNT(*) FROM reservation WHERE utilisateur_id = ? " +
                     "AND dateReservation >= DATE_SUB(NOW(), INTERVAL 24 HOUR)";
        
        try (Connection cnx = mydb.getInstance().getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting user recent reservations: " + e.getMessage());
        }
        
        return 0;
    }
    
    private boolean isSuspiciousEmail(String email) {
        if (email == null || !email.contains("@")) return true;
        
        String[] suspiciousPatterns = {
            "test", "fake", "temp", "throwaway", "disposable"
        };
        
        String emailLower = email.toLowerCase();
        for (String pattern : suspiciousPatterns) {
            if (emailLower.contains(pattern)) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) return false;
        
        // Simple validation - remove non-digits and check length
        String digits = phone.replaceAll("[^0-9]", "");
        return digits.length() >= 8 && digits.length() <= 15;
    }
    
    private long getHoursUntilEvent(int eventId) {
        String sql = "SELECT TIMESTAMPDIFF(HOUR, NOW(), date) FROM evenement WHERE idEvenement = ?";
        
        try (Connection cnx = mydb.getInstance().getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting hours until event: " + e.getMessage());
        }
        
        return -1; // Event in the past or error
    }
    
    private double getSeasonalityFactor(int eventId, LocalDateTime targetDate) {
        // Simple seasonality based on month
        int month = targetDate.getMonthValue();
        
        // Higher demand in summer months (June-August) and December
        if (month >= 6 && month <= 8) return 1.3;
        if (month == 12) return 1.4;
        if (month >= 3 && month <= 5) return 1.1; // Spring
        if (month >= 9 && month <= 11) return 0.9; // Fall
        return 0.8; // Winter (except December)
    }
    
    private double getTrendFactor(int eventId) {
        // Simple trend based on recent reservation growth
        List<Integer> recentReservations = getRecentReservationCounts(eventId);
        if (recentReservations.size() < 2) return 1.0;
        
        double recent = recentReservations.get(recentReservations.size() - 1);
        double previous = recentReservations.get(recentReservations.size() - 2);
        
        return previous == 0 ? 1.0 : Math.max(0.5, Math.min(1.5, recent / previous));
    }
    
    private double getBaseDemand(int eventId) {
        // Get average daily reservations for this event type
        String sql = "SELECT COUNT(*) / 30.0 FROM reservation " +
                     "WHERE idEvenement = ? AND dateReservation >= DATE_SUB(NOW(), INTERVAL 30 DAY)";
        
        try (Connection cnx = mydb.getInstance().getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting base demand: " + e.getMessage());
        }
        
        return 5.0; // Default base demand
    }
    
    private double getDayOfWeekFactor(LocalDateTime dateTime) {
        int dayOfWeek = dateTime.getDayOfWeek().getValue();
        
        // Weekends have higher demand
        if (dayOfWeek == 6 || dayOfWeek == 7) return 1.4; // Saturday, Sunday
        if (dayOfWeek == 5) return 1.2; // Friday
        return 1.0; // Monday-Thursday
    }
    
    private double getTimeOfDayFactor(LocalDateTime dateTime) {
        int hour = dateTime.getHour();
        
        // Evening events are more popular
        if (hour >= 18 && hour <= 22) return 1.3;
        if (hour >= 12 && hour <= 17) return 1.1;
        return 0.9; // Late night or morning
    }
    
    private String getVenueLayout(int eventId) {
        // In real system, this would come from database
        return "standard_theater";
    }
    
    private List<String> getAvailableSeats(int eventId) {
        List<String> seats = new ArrayList<>();
        // Simulate available seats
        for (char row = 'A'; row <= 'F'; row++) {
            for (int num = 1; num <= 20; num++) {
                seats.add(row + String.valueOf(num));
            }
        }
        return seats;
    }
    
    private boolean isDisposableEmailDomain(String domain) {
        String[] disposableDomains = {
            "10minutemail.com", "tempmail.org", "guerrillamail.com",
            "mailinator.com", "throwaway.email"
        };
        
        for (String disposable : disposableDomains) {
            if (domain.contains(disposable)) {
                return true;
            }
        }
        
        return false;
    }
    
    private int getUserTotalReservations(int userId) {
        String sql = "SELECT COUNT(*) FROM reservation WHERE utilisateur_id = ?";
        
        try (Connection cnx = mydb.getInstance().getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting user total reservations: " + e.getMessage());
        }
        
        return 0;
    }
    
    private int getUserCancelledReservations(int userId) {
        // In a real system, you'd track cancellations
        return 0; // Placeholder
    }
    
    private int getUserSuspiciousReservations(int userId) {
        // Check user's history of suspicious reservations
        return 0; // Placeholder
    }
    
    private int getUserReservationsInLastHour(int userId) {
        String sql = "SELECT COUNT(*) FROM reservation WHERE utilisateur_id = ? " +
                     "AND dateReservation >= DATE_SUB(NOW(), INTERVAL 1 HOUR)";
        
        try (Connection cnx = mydb.getInstance().getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting reservations in last hour: " + e.getMessage());
        }
        
        return 0;
    }
    
    private double getCurrentDemandRatio(int eventId) {
        int currentReservations = getCurrentReservationCount(eventId);
        int maxCapacity = getMaxCapacity(eventId);
        
        return maxCapacity == 0 ? 0.0 : (double) currentReservations / maxCapacity;
    }
    
    private double getEventPrice(int eventId) {
        // In real system, this would come from database
        return 50.0; // Default price
    }
    
    private int getCurrentReservationCount(int eventId) {
        String sql = "SELECT COUNT(*) FROM reservation WHERE idEvenement = ?";
        
        try (Connection cnx = mydb.getInstance().getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting current reservation count: " + e.getMessage());
        }
        
        return 0;
    }
    
    private int getMaxCapacity(int eventId) {
        // In real system, this would come from database
        return 100; // Default capacity
    }
    
    private List<Integer> getRecentReservationCounts(int eventId) {
        List<Integer> counts = new ArrayList<>();
        String sql = "SELECT COUNT(*) FROM reservation WHERE idEvenement = ? " +
                     "AND dateReservation >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
                     "GROUP BY DATE(dateReservation) ORDER BY dateReservation";
        
        try (Connection cnx = mydb.getInstance().getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    counts.add(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting recent reservation counts: " + e.getMessage());
        }
        
        return counts;
    }
}
