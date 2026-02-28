package org.example.Controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.example.Models.Ressource;
import org.example.Services.ressourceService;
import org.example.Services.MailService;

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

    @FXML
    private Button btnSauvegarder;
    @FXML
    private Button btnAnnuler;

    private final ressourceService service = new ressourceService();
    private RessourceFXController mainController;

    @FXML
    public void initialize() {
        applyIcon(btnSauvegarder, "save.png", "Sauvegarder", 20);
        applyIcon(btnAnnuler, "cancel.png", "Annuler", 20);

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
            try {
                String dateStr = r.getDateCreation() != null ? r.getDateCreation().toString() : null;
                String html = MailService.buildNewResourceEmailHtml(
                        r.getTitle(), r.getDescription(), r.getType(), r.getCategory(), r.getAuthor(), dateStr);
                MailService.sendEmail(
                        MailService.DEFAULT_TO_EMAIL,
                        "Nouvelle ressource ajoutée - GrowMind",
                        html,
                        true
                );
            } catch (Exception e) {
                System.out.println("⚠️ Email non envoyé : " + e.getMessage());
            }

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

    private void applyIcon(Button btn, String iconName, String tooltipText, double size) {
        if (btn == null) return;
        for (String path : new String[] { "/icons/" + iconName, "/" + iconName }) {
            try {
                java.io.InputStream is = getClass().getResourceAsStream(path);
                if (is == null) continue;
                Image img = new Image(is);
                if (img.isError()) continue;
                ImageView iv = new ImageView(img);
                iv.setFitWidth(size);
                iv.setFitHeight(size);
                iv.setPreserveRatio(true);
                iv.setSmooth(true);
                btn.setGraphic(iv);
                btn.setText(null);
                btn.setTooltip(new Tooltip(tooltipText));
                btn.setContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);
                btn.setMinWidth(44);
                btn.setMaxWidth(44);
                btn.setMinHeight(44);
                btn.setMaxHeight(44);
                return;
            } catch (Exception ignored) {}
        }
    }
}
