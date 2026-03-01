package utiles;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseSetup {
    
    public static void main(String[] args) {
        try {
            mydb db = mydb.getInstance();
            Connection cnx = db.getConnection();
            
            if (cnx == null) {
                System.err.println("❌ Database connection failed: " + db.getLastError());
                return;
            }
            
            System.out.println("✅ Database connected successfully!");
            
            // Check if utilisateur table exists
            try (Statement st = cnx.createStatement()) {
                ResultSet rs = st.executeQuery("SHOW TABLES LIKE 'utilisateur'");
                if (!rs.next()) {
                    System.out.println("📝 Creating utilisateur table...");
                    st.execute("CREATE TABLE utilisateur (" +
                               "id INT AUTO_INCREMENT PRIMARY KEY," +
                               "nom VARCHAR(100) NOT NULL," +
                               "email VARCHAR(100) UNIQUE NOT NULL," +
                               "mot_de_passe VARCHAR(100) NOT NULL" +
                               ")");
                }
            }
            
            // Check if evenement table exists
            try (Statement st = cnx.createStatement()) {
                ResultSet rs = st.executeQuery("SHOW TABLES LIKE 'evenement'");
                if (!rs.next()) {
                    System.out.println("📝 Creating evenement table...");
                    st.execute("CREATE TABLE evenement (" +
                               "idEvenement INT AUTO_INCREMENT PRIMARY KEY," +
                               "titre VARCHAR(200) NOT NULL," +
                               "description TEXT," +
                               "date DATE NOT NULL," +
                               "localisation VARCHAR(200)" +
                               ")");
                }
            }
            
            // Add sample users if none exist
            try (Statement st = cnx.createStatement()) {
                ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM utilisateur");
                if (rs.next() && rs.getInt(1) == 0) {
                    System.out.println("👥 Adding sample users...");
                    
                    try (PreparedStatement ps = cnx.prepareStatement(
                        "INSERT INTO utilisateur (nom, email, mot_de_passe) VALUES (?, ?, ?)")) {
                        
                        ps.setString(1, "Admin User");
                        ps.setString(2, "admin@test.com");
                        ps.setString(3, "admin123");
                        ps.executeUpdate();
                        
                        ps.setString(1, "John Doe");
                        ps.setString(2, "john@test.com");
                        ps.setString(3, "123456");
                        ps.executeUpdate();
                        
                        ps.setString(1, "Jane Smith");
                        ps.setString(2, "jane@test.com");
                        ps.setString(3, "password");
                        ps.executeUpdate();
                    }
                    
                    System.out.println("✅ Sample users added:");
                    System.out.println("   - admin@test.com / admin123");
                    System.out.println("   - john@test.com / 123456");
                    System.out.println("   - jane@test.com / password");
                }
            }
            
            // Add sample events if none exist
            try (Statement st = cnx.createStatement()) {
                ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM evenement");
                if (rs.next() && rs.getInt(1) == 0) {
                    System.out.println("📅 Adding sample events...");
                    
                    try (PreparedStatement ps = cnx.prepareStatement(
                        "INSERT INTO evenement (titre, description, date, localisation) VALUES (?, ?, ?, ?)")) {
                        
                        ps.setString(1, "Conférence Tech 2024");
                        ps.setString(2, "Une conférence sur les dernières technologies de l'IA et du cloud computing");
                        ps.setDate(3, java.sql.Date.valueOf("2024-03-15"));
                        ps.setString(4, "Tunis, Palais des Congrès");
                        ps.executeUpdate();
                        
                        ps.setString(1, "Workshop JavaFX");
                        ps.setString(2, "Apprenez à créer des applications desktop modernes avec JavaFX");
                        ps.setDate(3, java.sql.Date.valueOf("2024-03-20"));
                        ps.setString(4, "Sfax, Technopole");
                        ps.executeUpdate();
                        
                        ps.setString(1, "Meetup Développeurs");
                        ps.setString(2, "Rencontre informelle entre développeurs pour partager nos expériences");
                        ps.setDate(3, java.sql.Date.valueOf("2024-03-25"));
                        ps.setString(4, "Sousse, Espacenet");
                        ps.executeUpdate();
                    }
                    
                    System.out.println("✅ Sample events added!");
                }
            }
            
            System.out.println("🎉 Database setup completed successfully!");
            
        } catch (Exception e) {
            System.err.println("❌ Database setup failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
