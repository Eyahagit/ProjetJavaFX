package Services;

import Models.Utilisateur;
import utils.mydb;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.*;

public class LoginController {

    @FXML private TextField txtEmail;
    @FXML private PasswordField txtMotDePasse;
    @FXML private Label lblMessage;

    @FXML
    private void handleConnexion() {
        String email = txtEmail.getText().trim();
        String mdp = txtMotDePasse.getText().trim();

        if (email.isEmpty() || mdp.isEmpty()) {
            lblMessage.setText("Email et mot de passe requis");
            return;
        }

        try {
            System.out.println("🔐 Attempting login with: " + email);
            mydb db = mydb.getInstance();
            Connection cnx = db.getConnection();
            if (cnx == null || cnx.isClosed()) {
                String err = db.getLastError();
                System.err.println("❌ Database connection failed: " + err);
                lblMessage.setText(err == null ? "Connexion impossible à la base de données" : ("Erreur BD: " + err));
                return;
            }

            // Ensure tables exist and add sample data if needed
            ensureDatabaseSetup(cnx);

            System.out.println("✅ Database connected, checking credentials...");
            String sql = "SELECT * FROM utilisateur WHERE email = ? AND mot_de_passe = ?";
            try (PreparedStatement ps = cnx.prepareStatement(sql)) {
                ps.setString(1, email);
                ps.setString(2, mdp);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Utilisateur user = new Utilisateur();
                        user.setId(rs.getInt("id"));
                        user.setNom(rs.getString("nom"));
                        user.setEmail(rs.getString("email"));

                        UserSession.setUser(user);
                        System.out.println("✅ Login successful for: " + user.getNom());

                        // Ouvrir la fenêtre principale
                        Stage stage = new Stage();
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainView.fxml"));
                        stage.setScene(new Scene(loader.load()));
                        stage.setTitle("Gestion Événements - " + user.getNom());
                        stage.show();

                        // Fermer la fenêtre de connexion
                        ((Stage) txtEmail.getScene().getWindow()).close();
                    } else {
                        System.out.println("❌ Invalid credentials for: " + email);
                        lblMessage.setText("Email ou mot de passe incorrect\n\nUtilisateurs de test:\nadmin@test.com / admin123\njohn@test.com / 123456");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            lblMessage.setText("Erreur: " + e.getMessage());
        }
    }

    private void ensureDatabaseSetup(Connection cnx) {
        try {
            // Create utilisateur table if it doesn't exist
            try (Statement st = cnx.createStatement()) {
                st.execute("CREATE TABLE IF NOT EXISTS utilisateur (" +
                           "id INT AUTO_INCREMENT PRIMARY KEY," +
                           "nom VARCHAR(100) NOT NULL," +
                           "email VARCHAR(100) UNIQUE NOT NULL," +
                           "mot_de_passe VARCHAR(100) NOT NULL" +
                           ")");
            }

            // Create evenement table if it doesn't exist
            try (Statement st = cnx.createStatement()) {
                st.execute("CREATE TABLE IF NOT EXISTS evenement (" +
                           "idEvenement INT AUTO_INCREMENT PRIMARY KEY," +
                           "titre VARCHAR(200) NOT NULL," +
                           "description TEXT," +
                           "date DATE NOT NULL," +
                           "localisation VARCHAR(200)" +
                           ")");
            }

            // Add sample users if none exist
            try (Statement st = cnx.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM utilisateur")) {
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
                    
                    System.out.println("✅ Sample users added!");
                }
            }

            // Add sample events if none exist
            try (Statement st = cnx.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM evenement")) {
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

        } catch (SQLException e) {
            System.err.println("❌ Database setup error: " + e.getMessage());
        }
    }

    @FXML
    private void handleQuitter() {
        System.exit(0);
    }
}