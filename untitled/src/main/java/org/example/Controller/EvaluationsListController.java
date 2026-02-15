package org.example.Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.Models.Evaluation;
import org.example.Models.Ressource;
import org.example.Models.User; // Assuming User model has meaningful toString or getters
import org.example.Services.evaluationService;
import org.example.utils.StaticUser;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

public class EvaluationsListController {

    @FXML
    private Label lblResourceTitle;
    @FXML
    private TableView<Evaluation> tableEvaluations;
    @FXML
    private TableColumn<Evaluation, String> colUser; // Assuming we map user name
    @FXML
    private TableColumn<Evaluation, Integer> colNote;
    @FXML
    private TableColumn<Evaluation, String> colComment;
    @FXML
    private TableColumn<Evaluation, LocalDate> colDate;

    private Ressource currentRessource;
    private final evaluationService service = new evaluationService();
    private ObservableList<Evaluation> evaluationList = FXCollections.observableArrayList();

    public void setRessource(Ressource r) {
        this.currentRessource = r;
        lblResourceTitle.setText("Ressource : " + r.getTitle());
        refreshList();
    }

    @FXML
    public void initialize() {
        colNote.setCellValueFactory(new PropertyValueFactory<>("note"));
        colComment.setCellValueFactory(new PropertyValueFactory<>("commentaire"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateEvaluation"));

        // For User column, we might need a custom cell value factory if User is an
        // object
        colUser.setCellValueFactory(cellData -> {
            User u = cellData.getValue().getUser();
            return new javafx.beans.property.SimpleStringProperty(
                    u != null ? u.getNom() + " " + u.getPrenom() : "Anonyme");
        });
    }

    private void refreshList() {
        if (currentRessource != null) {
            evaluationList.setAll(service.findByRessourceId(currentRessource.getId()));
            tableEvaluations.setItems(evaluationList);
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/userressource.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tableEvaluations.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDelete() {
        Evaluation selected = tableEvaluations.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Erreur", "Veuillez sélectionner une évaluation à supprimer.");
            return;
        }

        // Check permission: only delete own evaluation (or admin logic if implemented)
        if (StaticUser.isSet() && selected.getUser().getId() != StaticUser.get().getId()) {
            showAlert("Non autorisé", "Vous ne pouvez supprimer que vos propres évaluations.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer l'évaluation ?");
        alert.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            service.delete(selected.getId());
            refreshList();
        }
    }

    @FXML
    private void handleEdit() {
        Evaluation selected = tableEvaluations.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Erreur", "Veuillez sélectionner une évaluation à modifier.");
            return;
        }

        if (StaticUser.isSet() && selected.getUser().getId() != StaticUser.get().getId()) {
            showAlert("Non autorisé", "Vous ne pouvez modifier que vos propres évaluations.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/evaluationModal.fxml"));
            Parent root = loader.load();

            EvaluationModalController controller = loader.getController();
            controller.setRessource(currentRessource, null); // Parent null because we will refresh manually
            controller.setExistingEvaluation(selected);

            Stage stage = new Stage();
            stage.setTitle("Modifier l'évaluation");
            stage.setScene(new Scene(root));
            stage.showAndWait(); // Wait for it to close

            refreshList(); // Refresh after edit
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}
