package Services;

import utils.mydb;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class DatabaseSchemaUpdater {
    
    public static void updateSchemaWithAI() {
        try (Connection cnx = mydb.getInstance().getConnection();
             Statement st = cnx.createStatement()) {
            
            System.out.println("Updating database schema with AI features...");
            
            // Update evenement table
            updateEvenementTable(st);
            
            // Update reservation table  
            updateReservationTable(st);
            
            // Update avis table
            updateAvisTable(st);
            
            // Update utilisateur table
            updateUtilisateurTable(st);
            
            System.out.println("Database schema updated successfully!");
            
        } catch (SQLException e) {
            System.err.println("Error updating database schema: " + e.getMessage());
        }
    }
    
    private static void updateEvenementTable(Statement st) throws SQLException {
        System.out.println("Updating evenement table...");
        
        // Add AI-related columns to evenement table
        try {
            st.execute("ALTER TABLE evenement ADD COLUMN popularity_score DOUBLE DEFAULT 0");
        } catch (SQLException e) {
            if (!e.getMessage().contains("Duplicate column name")) {
                System.err.println("Error adding popularity_score: " + e.getMessage());
            }
        }
        
        try {
            st.execute("ALTER TABLE evenement ADD COLUMN predicted_attendance INT DEFAULT 0");
        } catch (SQLException e) {
            if (!e.getMessage().contains("Duplicate column name")) {
                System.err.println("Error adding predicted_attendance: " + e.getMessage());
            }
        }
        
        try {
            st.execute("ALTER TABLE evenement ADD COLUMN dynamic_price DOUBLE DEFAULT 50.0");
        } catch (SQLException e) {
            if (!e.getMessage().contains("Duplicate column name")) {
                System.err.println("Error adding dynamic_price: " + e.getMessage());
            }
        }
        
        try {
            st.execute("ALTER TABLE evenement ADD COLUMN base_price DOUBLE DEFAULT 50.0");
        } catch (SQLException e) {
            if (!e.getMessage().contains("Duplicate column name")) {
                System.err.println("Error adding base_price: " + e.getMessage());
            }
        }
        
        try {
            st.execute("ALTER TABLE evenement ADD COLUMN max_capacity INT DEFAULT 100");
        } catch (SQLException e) {
            if (!e.getMessage().contains("Duplicate column name")) {
                System.err.println("Error adding max_capacity: " + e.getMessage());
            }
        }
        
        try {
            st.execute("ALTER TABLE evenement ADD COLUMN venue_layout VARCHAR(50) DEFAULT 'standard'");
        } catch (SQLException e) {
            if (!e.getMessage().contains("Duplicate column name")) {
                System.err.println("Error adding venue_layout: " + e.getMessage());
            }
        }
    }
    
    private static void updateReservationTable(Statement st) throws SQLException {
        System.out.println("Updating reservation table...");
        
        // Add AI-related columns to reservation table
        try {
            st.execute("ALTER TABLE reservation ADD COLUMN fraud_probability DOUBLE DEFAULT 0");
        } catch (SQLException e) {
            if (!e.getMessage().contains("Duplicate column name")) {
                System.err.println("Error adding fraud_probability: " + e.getMessage());
            }
        }
        
        try {
            st.execute("ALTER TABLE reservation ADD COLUMN is_suspicious BOOLEAN DEFAULT FALSE");
        } catch (SQLException e) {
            if (!e.getMessage().contains("Duplicate column name")) {
                System.err.println("Error adding is_suspicious: " + e.getMessage());
            }
        }
        
        try {
            st.execute("ALTER TABLE reservation ADD COLUMN allocated_seats TEXT");
        } catch (SQLException e) {
            if (!e.getMessage().contains("Duplicate column name")) {
                System.err.println("Error adding allocated_seats: " + e.getMessage());
            }
        }
        
        try {
            st.execute("ALTER TABLE reservation ADD COLUMN seating_preference VARCHAR(20) DEFAULT 'auto'");
        } catch (SQLException e) {
            if (!e.getMessage().contains("Duplicate column name")) {
                System.err.println("Error adding seating_preference: " + e.getMessage());
            }
        }
    }
    
    private static void updateAvisTable(Statement st) throws SQLException {
        System.out.println("Updating avis table...");
        
        // Ensure avis table exists first
        st.execute("CREATE TABLE IF NOT EXISTS doua.avis (" +
                   "idAvis INT AUTO_INCREMENT PRIMARY KEY," +
                   "idReservation INT NOT NULL," +
                   "utilisateur_id INT NOT NULL," +
                   "note INT NOT NULL," +
                   "commentaire TEXT NULL," +
                   "date_avis TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                   "UNIQUE KEY unique_reservation_user (idReservation, utilisateur_id)" +
                   ")");
        
        // Add AI-related columns to avis table
        try {
            st.execute("ALTER TABLE doua.avis ADD COLUMN sentiment_score DOUBLE DEFAULT 0");
        } catch (SQLException e) {
            if (!e.getMessage().contains("Duplicate column name")) {
                System.err.println("Error adding sentiment_score: " + e.getMessage());
            }
        }
        
        try {
            st.execute("ALTER TABLE doua.avis ADD COLUMN authenticity_score DOUBLE DEFAULT 1.0");
        } catch (SQLException e) {
            if (!e.getMessage().contains("Duplicate column name")) {
                System.err.println("Error adding authenticity_score: " + e.getMessage());
            }
        }
        
        try {
            st.execute("ALTER TABLE doua.avis ADD COLUMN review_category VARCHAR(20) DEFAULT 'General'");
        } catch (SQLException e) {
            if (!e.getMessage().contains("Duplicate column name")) {
                System.err.println("Error adding review_category: " + e.getMessage());
            }
        }
        
        try {
            st.execute("ALTER TABLE doua.avis ADD COLUMN is_verified BOOLEAN DEFAULT FALSE");
        } catch (SQLException e) {
            if (!e.getMessage().contains("Duplicate column name")) {
                System.err.println("Error adding is_verified: " + e.getMessage());
            }
        }
    }
    
    private static void updateUtilisateurTable(Statement st) throws SQLException {
        System.out.println("Updating utilisateur table...");
        
        // Add AI-related columns to utilisateur table
        try {
            st.execute("ALTER TABLE utilisateur ADD COLUMN user_segment VARCHAR(20) DEFAULT 'Regular'");
        } catch (SQLException e) {
            if (!e.getMessage().contains("Duplicate column name")) {
                System.err.println("Error adding user_segment: " + e.getMessage());
            }
        }
        
        try {
            st.execute("ALTER TABLE utilisateur ADD COLUMN preference_vector TEXT");
        } catch (SQLException e) {
            if (!e.getMessage().contains("Duplicate column name")) {
                System.err.println("Error adding preference_vector: " + e.getMessage());
            }
        }
        
        try {
            st.execute("ALTER TABLE utilisateur ADD COLUMN churn_probability DOUBLE DEFAULT 0");
        } catch (SQLException e) {
            if (!e.getMessage().contains("Duplicate column name")) {
                System.err.println("Error adding churn_probability: " + e.getMessage());
            }
        }
        
        try {
            st.execute("ALTER TABLE utilisateur ADD COLUMN lifetime_value DOUBLE DEFAULT 0");
        } catch (SQLException e) {
            if (!e.getMessage().contains("Duplicate column name")) {
                System.err.println("Error adding lifetime_value: " + e.getMessage());
            }
        }
        
        try {
            st.execute("ALTER TABLE utilisateur ADD COLUMN risk_score DOUBLE DEFAULT 0");
        } catch (SQLException e) {
            if (!e.getMessage().contains("Duplicate column name")) {
                System.err.println("Error adding risk_score: " + e.getMessage());
            }
        }
    }
    
    // Method to initialize default values
    public static void initializeDefaultValues() {
        try (Connection cnx = mydb.getInstance().getConnection();
             Statement st = cnx.createStatement()) {
            
            System.out.println("Initializing default AI values...");
            
            // Set default values for existing events
            st.execute("UPDATE evenement SET popularity_score = 0 WHERE popularity_score IS NULL");
            st.execute("UPDATE evenement SET predicted_attendance = 50 WHERE predicted_attendance IS NULL");
            st.execute("UPDATE evenement SET dynamic_price = 50.0 WHERE dynamic_price IS NULL");
            st.execute("UPDATE evenement SET base_price = 50.0 WHERE base_price IS NULL");
            st.execute("UPDATE evenement SET max_capacity = 100 WHERE max_capacity IS NULL");
            
            // Set default values for existing reservations
            st.execute("UPDATE reservation SET fraud_probability = 0 WHERE fraud_probability IS NULL");
            st.execute("UPDATE reservation SET is_suspicious = FALSE WHERE is_suspicious IS NULL");
            
            // Set default values for existing reviews
            st.execute("UPDATE doua.avis SET sentiment_score = 0 WHERE sentiment_score IS NULL");
            st.execute("UPDATE doua.avis SET authenticity_score = 1.0 WHERE authenticity_score IS NULL");
            st.execute("UPDATE doua.avis SET review_category = 'General' WHERE review_category IS NULL");
            
            // Set default values for existing users
            st.execute("UPDATE utilisateur SET user_segment = 'Regular' WHERE user_segment IS NULL");
            st.execute("UPDATE utilisateur SET churn_probability = 0 WHERE churn_probability IS NULL");
            st.execute("UPDATE utilisateur SET lifetime_value = 0 WHERE lifetime_value IS NULL");
            st.execute("UPDATE utilisateur SET risk_score = 0 WHERE risk_score IS NULL");
            
            System.out.println("Default values initialized successfully!");
            
        } catch (SQLException e) {
            System.err.println("Error initializing default values: " + e.getMessage());
        }
    }
}
