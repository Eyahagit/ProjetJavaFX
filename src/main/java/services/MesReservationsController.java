package services;

import Modele.Reservation;
import utiles.mydb;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.util.List;

public class MesReservationsController {

    @FXML private ListView<Reservation> listViewReservations;
    @FXML private Label lblCountReservations;
    @FXML private Button btnFermer;
    @FXML private Button btnDonnerAvis;

    private ReservationControlleur reservationService;
    private ObservableList<Reservation> reservationsData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        reservationService = new ReservationControlleur(mydb.getInstance().getConnection());
        chargerReservations();

        // Personnaliser l'affichage avec design amélioré
        listViewReservations.setCellFactory(lv -> new ListCell<Reservation>() {
            @Override
            protected void updateItem(Reservation r, boolean empty) {
                super.updateItem(r, empty);

                if (empty || r == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Créer un HBox pour un meilleur design
                    HBox cellBox = new HBox(15);
                    cellBox.setAlignment(Pos.CENTER_LEFT);
                    cellBox.setPadding(new Insets(12, 10, 12, 15));
                    cellBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; -fx-border-color: #e0e0e0; -fx-border-radius: 10;");

                    // Icône calendrier
                    Label iconLabel = new Label("📅");
                    iconLabel.setStyle("-fx-font-size: 22px;");

                    // Détails de la réservation
                    VBox detailsBox = new VBox(5);

                    Label dateLabel = new Label("Date: " + r.getDateReservation().toLocalDate());
                    dateLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 14px;");

                    Label infoLabel = new Label("👥 " + r.getNombrePersonnes() + " personne(s) • 📧 " + r.getEmail());
                    infoLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");

                    detailsBox.getChildren().addAll(dateLabel, infoLabel);

                    cellBox.getChildren().addAll(iconLabel, detailsBox);

                    setGraphic(cellBox);
                    setText(null);
                }
            }
        });
    }

    private void chargerReservations() {
        try {
            int userId = UserSession.getId();
            List<Reservation> list = reservationService.getByUtilisateur(userId);
            reservationsData.setAll(list);
            listViewReservations.setItems(reservationsData);
            lblCountReservations.setText(list.size() + " réservation(s)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDonnerAvis() {
        Reservation selected = listViewReservations.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("⚠️ Attention");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner une réservation");
            alert.showAndWait();
            return;
        }
        alert("Information", "Fonctionnalité à venir");
    }

    @FXML
    private void handleFermer() {
        ((Stage) listViewReservations.getScene().getWindow()).close();
    }

    private void alert(String titre, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("⚠️ " + titre);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}