import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

public class CheckDatabase {
    public static void main(String[] args) {
        String URL = "jdbc:mysql://127.0.0.1:3307/doua?useSSL=false&allowPublicKeyRetrieval=true";
        String USERNAME = "root";
        String PASSWORD = "";
        
        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            System.out.println("Connected to database");
            
            // Check if evenement table exists and show its structure
            try (Statement stmt = conn.createStatement()) {
                System.out.println("\nChecking evenement table...");
                try (ResultSet rs = stmt.executeQuery("SHOW TABLES LIKE 'evenement'")) {
                    if (rs.next()) {
                        System.out.println("evenement table exists");
                        
                        // Show table structure
                        try (ResultSet rs2 = stmt.executeQuery("DESCRIBE evenement")) {
                            System.out.println("\nTable structure:");
                            while (rs2.next()) {
                                System.out.println("  - " + rs2.getString(1) + " | " + rs2.getString(2) + " | " + rs2.getString(3));
                            }
                        }
                        
                        // Count events
                        try (ResultSet rs3 = stmt.executeQuery("SELECT COUNT(*) FROM evenement")) {
                            if (rs3.next()) {
                                int count = rs3.getInt(1);
                                System.out.println("\nTotal events in database: " + count);
                                
                                if (count > 0) {
                                    // Show all events
                                    try (ResultSet rs4 = stmt.executeQuery("SELECT idEvenement, titre, description, date, localisation FROM evenement ORDER BY idEvenement")) {
                                        System.out.println("\nEvents found:");
                                        while (rs4.next()) {
                                            System.out.println("  ID: " + rs4.getInt("idEvenement"));
                                            System.out.println("  Titre: " + rs4.getString("titre"));
                                            System.out.println("  Description: " + rs4.getString("description"));
                                            System.out.println("  Date: " + rs4.getString("date"));
                                            System.out.println("  Localisation: " + rs4.getString("localisation"));
                                            System.out.println("  ---");
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        System.out.println("evenement table does not exist");
                        
                        // Show all tables
                        try (ResultSet rs5 = stmt.executeQuery("SHOW TABLES")) {
                            System.out.println("\nAvailable tables:");
                            while (rs5.next()) {
                                System.out.println("  - " + rs5.getString(1));
                            }
                        }
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
