package services;

import entities.AlerteUrgence;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;

public class ServiceUrgence {

    private Connection connection;

    public ServiceUrgence() {
        this.connection = DatabaseConnection.getConnection();
    }

    public void enregistrerAlerte(AlerteUrgence alerte) {
        String query = "INSERT INTO alertes_urgence (user_id, nom, message, localisation, date_alerte, statut) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, alerte.getUserId());
            pstmt.setString(2, alerte.getNom());
            pstmt.setString(3, alerte.getMessage());
            pstmt.setString(4, alerte.getLocalisation());
            pstmt.setTimestamp(5, Timestamp.valueOf(alerte.getDateAlerte()));
            pstmt.setString(6, alerte.getStatut());

            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                alerte.setId(rs.getInt(1));
            }

            System.out.println("✅ Alerte enregistrée en base avec ID: " + alerte.getId());

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'enregistrement de l'alerte");
            e.printStackTrace();
        }
    }

    public ObservableList<AlerteUrgence> getAllAlertes() {
        ObservableList<AlerteUrgence> alertes = FXCollections.observableArrayList();
        String query = "SELECT * FROM alertes_urgence ORDER BY date_alerte DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                AlerteUrgence alerte = new AlerteUrgence(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("nom"),
                        rs.getString("message"),
                        rs.getString("localisation"),
                        rs.getTimestamp("date_alerte").toLocalDateTime(),
                        rs.getString("statut")
                );
                alertes.add(alerte);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return alertes;
    }

    public int compterAlertes() {
        String query = "SELECT COUNT(*) FROM alertes_urgence";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }
}