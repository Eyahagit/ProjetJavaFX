package services;

import Modele.Evenement;
import utiles.mydb;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.format.DateTimeFormatter;

public class ReservationController {

    @FXML private Label lblEventInfo;
    @FXML private TextField txtNom;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelephone;
    @FXML private TextField txtNbPlaces;
    @FXML private Label lblMessage;

    private Evenement currentEvent;
    private int currentUserId = 1; // Default user ID for demo

    public void setEventData(Evenement event) {
        this.currentEvent = event;
        lblEventInfo.setText("📅 " + event.getTitre() + "\n" +
                           "📍 " + event.getLocalisation() + "\n" +
                           "📆 " + event.getDate().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
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

        // Email validation simple
        if (!email.contains("@") || !email.contains(".")) {
            lblMessage.setText("⚠️ Email invalide");
            return;
        }

        try {
            // Enregistrer la réservation avec la structure correcte de la table
            Connection conn = mydb.getInstance().getConnection();
            if (conn != null && !conn.isClosed()) {
                
                // Utiliser la table existante avec la structure correcte
                String sql = "INSERT INTO reservation (idEvenement, utilisateur_id, nom, email, telephone, nombre_personnes, date_reservation) VALUES (?, ?, ?, ?, ?, ?, NOW())";
                try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, currentEvent.getIdEvenement());
                    ps.setInt(2, currentUserId);
                    ps.setString(3, nom);
                    ps.setString(4, email);
                    ps.setString(5, telephone);
                    ps.setInt(6, nbPlaces);

                    int rowsAffected = ps.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        // Récupérer l'ID de la réservation
                        try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                int reservationId = generatedKeys.getInt(1);
                                lblMessage.setStyle("-fx-text-fill: green; -fx-font-size: 12px;");
                                lblMessage.setText("✅ Réservation #" + reservationId + " confirmée pour " + nbPlaces + " personne(s)!");
                                
                                // Désactiver le formulaire
                                txtNom.setDisable(true);
                                txtEmail.setDisable(true);
                                txtTelephone.setDisable(true);
                                txtNbPlaces.setDisable(true);
                                
                                System.out.println("✅ Reservation created: ID=" + reservationId + 
                                                 ", Event=" + currentEvent.getTitre() + 
                                                 ", Places=" + nbPlaces);
                            }
                        }
                    } else {
                        lblMessage.setText("❌ Erreur lors de la réservation");
                    }
                }
            } else {
                lblMessage.setText("❌ Erreur de connexion à la base de données");
            }
        } catch (Exception e) {
            System.err.println("❌ Error creating reservation: " + e.getMessage());
            e.printStackTrace();
            lblMessage.setText("❌ Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void handleAnnuler() {
        // Fermer la fenêtre de réservation
        Stage stage = (Stage) txtNom.getScene().getWindow();
        stage.close();
    }
}
