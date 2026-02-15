package org.example.Controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.Models.Ressource;
import org.example.Services.ressourceService;

import java.time.LocalDate;

public class AddRessourceController {

    @FXML
    private TextField titreField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private ComboBox<String> typeComboBox;

    @FXML
    private ComboBox<String> categorieComboBox;

    @FXML
    private TextField localisationField; // Can be used for content/author mapping if needed

    private final ressourceService service = new ressourceService();
    private RessourceFXController mainController;

    @FXML
    public void initialize() {
        typeComboBox.setItems(FXCollections.observableArrayList(
                "formation", "article", "video", "image", "evenement"));
        categorieComboBox.setItems(FXCollections.observableArrayList(
                "santé", "bien-etre", "developement personel", "motivation"));
    }

    public void setMainController(RessourceFXController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void handleSauvegarder() {
        if (isValidInput()) {
            Ressource r = new Ressource();
            r.setTitle(titreField.getText());
            r.setDescription(descriptionArea.getText());
            r.setType(typeComboBox.getValue());
            r.setCategory(categorieComboBox.getValue());
            r.setContent(localisationField.getText()); // Using localisation as content/link for now
            r.setAuthor("Admin"); // Default author
            r.setDateCreation(LocalDate.now());
            r.setStatus("Active");

            service.create(r);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Ressource ajoutée avec succès !");
            if (mainController != null) {
                mainController.refreshList();
            }
            closeWindow();
        }
    }

    @FXML
    private void handleAnnuler() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) titreField.getScene().getWindow();
        stage.close();
    }

    private boolean isValidInput() {
        String errorMessage = "";

        if (titreField.getText() == null || titreField.getText().isEmpty()) {
            errorMessage += "Titre invalide !\n";
        }
        if (descriptionArea.getText() == null || descriptionArea.getText().isEmpty()) {
            errorMessage += "Description invalide !\n";
        }
        if (typeComboBox.getValue() == null) {
            errorMessage += "Type invalide !\n";
        }
        if (categorieComboBox.getValue() == null) {
            errorMessage += "Catégorie invalide !\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            showAlert(Alert.AlertType.ERROR, "Champs Invalides", errorMessage);
            return false;
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
