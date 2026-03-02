package Controllers;

import Models.Evenement;
import Models.users;
import utils.Database;
import utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReservationController {

    @FXML private Label lblEventInfo;
    @FXML private TextField txtNom;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelephone;
    @FXML private TextField txtNbPlaces;
    @FXML private Label lblMessage;

    private Evenement currentEvent;
    private users currentUser;

    public void setEventData(Evenement event) {
        this.currentEvent = event;
        lblEventInfo.setText("📅 " + event.getTitre() + "\n" +
                "📍 " + event.getLocalisation() + "\n" +
                "📆 " + event.getDate().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
    }

    public void setCurrentUser(users user) {
        this.currentUser = user;
        if (user != null) {
            txtNom.setText(user.getName() + " " + user.getSecond_name());
            txtEmail.setText(user.getEmail());
        }
    }

    @FXML
    private void handleReservation() {
        String nom = txtNom.getText().trim();
        String email = txtEmail.getText().trim();
        String telephone = txtTelephone.getText().trim();
        String nbPlacesStr = txtNbPlaces.getText().trim();

        // Validation
        if (nom.isEmpty() || email.isEmpty() || telephone.isEmpty() || nbPlacesStr.isEmpty()) {
            lblMessage.setText("⚠️ Tous les champs sont requis");
            return;
        }

        int nbPlaces;
        try {
            nbPlaces = Integer.parseInt(nbPlacesStr);
            if (nbPlaces < 1) {
                lblMessage.setText("⚠️ Le nombre de places doit être au moins 1");
                return;
            }
        } catch (NumberFormatException e) {
            lblMessage.setText("⚠️ Nombre de places invalide");
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            lblMessage.setText("⚠️ Email invalide");
            return;
        }

        // Récupérer l'utilisateur connecté
        if (currentUser == null) {
            currentUser = SessionManager.getInstance().getCurrentUser();
        }
        int userId = (currentUser != null) ? currentUser.getId() : 1;

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet generatedKeys = null;

        try {
            // ⭐ Récupérer la connexion UNIQUE
            conn = Database.getInstance().getConnection();

            // ⚠️ Vérifier si la connexion est valide
            if (conn == null || conn.isClosed()) {
                lblMessage.setText("❌ Erreur de connexion à la base de données");
                return;
            }

            String sql = "INSERT INTO reservation (idEvenement, utilisateur_id, nom, email, telephone, nombre_personnes, date_reservation) VALUES (?, ?, ?, ?, ?, ?, ?)";

            ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);

            ps.setInt(1, currentEvent.getIdEvenement());
            ps.setInt(2, userId);
            ps.setString(3, nom);
            ps.setString(4, email);
            ps.setString(5, telephone);
            ps.setInt(6, nbPlaces);
            ps.setTimestamp(7, java.sql.Timestamp.valueOf(LocalDateTime.now()));

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int reservationId = generatedKeys.getInt(1);
                    lblMessage.setStyle("-fx-text-fill: green;");
                    lblMessage.setText("✅ Réservation #" + reservationId + " confirmée!");

                    txtNom.setDisable(true);
                    txtEmail.setDisable(true);
                    txtTelephone.setDisable(true);
                    txtNbPlaces.setDisable(true);
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            lblMessage.setText("❌ Erreur: " + e.getMessage());
        } finally {
            // ⭐ IMPORTANT: Fermer ONLY le PreparedStatement et ResultSet, PAS la connexion !
            try { if (generatedKeys != null) generatedKeys.close(); } catch (Exception e) {}
            try { if (ps != null) ps.close(); } catch (Exception e) {}
            // ⚠️ NE PAS fermer conn ici !
        }
    }

    @FXML
    private void handleAnnuler() {
        Stage stage = (Stage) txtNom.getScene().getWindow();
        stage.close();
    }
}