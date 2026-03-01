package org.example.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.Models.Ressource;
import org.example.Services.ressourceService;
import org.example.Services.CloudinaryService;
import org.example.Services.MailService;

import javafx.geometry.Pos;
import org.controlsfx.control.Notifications;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;

public class AjouterRessourceCompletController {

    @FXML
    private TextField txtTitre;
    @FXML
    private TextArea txtDescription;
    @FXML
    private ComboBox<String> comboType;
    @FXML
    private ComboBox<String> comboCategory;
    @FXML
    private DatePicker datePicker;
    @FXML
    private TextField txtLocalisation;
    @FXML
    private TextField txtAuthor;
    @FXML
    private ComboBox<String> comboStatus;

    private final ressourceService service = new ressourceService();
    private CloudinaryService cloudinaryService;

    @FXML
    public void initialize() {
        comboType.getItems().addAll("formation", "article", "video", "image", "pdf", "evenement");
        comboCategory.getItems().addAll("santé", "bien-etre", "developement personel", "motivation");
        comboStatus.getItems().addAll("Active", "Draft");
        datePicker.setValue(LocalDate.now());
    }

    @FXML
    private void ajouterRessource() {
        String title = txtTitre.getText();
        String description = txtDescription.getText();
        String author = txtAuthor != null ? txtAuthor.getText() : "";
        LocalDate date = datePicker != null ? datePicker.getValue() : null;

        StringBuilder errors = new StringBuilder();

        if (title == null || title.trim().isEmpty()) {
            errors.append("- Le titre est obligatoire.\n");
        }
        if (description == null || description.trim().isEmpty()) {
            errors.append("- La description est obligatoire.\n");
        }
        if (comboType.getValue() == null) {
            errors.append("- Le type est obligatoire.\n");
        }
        if (comboCategory.getValue() == null) {
            errors.append("- La catégorie est obligatoire.\n");
        }
        if (date == null) {
            errors.append("- La date est obligatoire.\n");
        }
        if (author == null || author.trim().isEmpty()) {
            errors.append("- L'auteur est obligatoire.\n");
        }

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Erreurs de validation",
                    "Veuillez corriger les erreurs suivantes :\n\n" + errors.toString());
            return;
        }

        Ressource r = new Ressource();
        r.setTitle(title.trim());
        r.setDescription(description.trim());
        r.setType(comboType.getValue());
        r.setCategory(comboCategory.getValue());
        r.setContent(txtLocalisation != null ? txtLocalisation.getText() : null);
        r.setAuthor(author.trim());
        r.setDateCreation(date);
        r.setStatus(comboStatus.getValue() != null ? comboStatus.getValue() : "Active");

        service.create(r);
        clearForm();
        showSuccessNotification("✅ Succès", "Ressource ajoutée avec succès !");
        sendNewResourceNotification(r);
    }

    private void clearForm() {
        txtTitre.setText("");
        txtDescription.setText("");
        txtLocalisation.setText("");
        txtAuthor.setText("");
        datePicker.setValue(LocalDate.now());
        comboType.setValue(null);
        comboCategory.setValue(null);
        comboStatus.setValue("Active");
    }

    @FXML
    private void uploadImageToCloudinary() {
        javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
        chooser.setTitle("Choisir une image ou vidéo");
        chooser.getExtensionFilters().addAll(
                new javafx.stage.FileChooser.ExtensionFilter(
                        "Tous médias (images, vidéos, PDF)",
                        "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp",
                        "*.mp4", "*.webm", "*.mov", "*.avi",
                        "*.pdf"),
                new javafx.stage.FileChooser.ExtensionFilter("Images/Vidéos", "*.png", "*.jpg", "*.jpeg", "*.gif",
                        "*.webp", "*.mp4", "*.webm", "*.mov", "*.avi"),
                new javafx.stage.FileChooser.ExtensionFilter("PDF", "*.pdf"),
                new javafx.stage.FileChooser.ExtensionFilter("Tous les fichiers", "*.*"));
        File file = chooser.showOpenDialog(txtTitre.getScene().getWindow());
        if (file == null)
            return;
        try {
            if (cloudinaryService == null)
                cloudinaryService = new CloudinaryService();
            String url = cloudinaryService.upload(file);
            if (url != null && !url.isBlank()) {
                // SAVE LOCALLY as well
                try {
                    Path uploadsDir = Paths.get("uploads");
                    if (!Files.exists(uploadsDir)) {
                        Files.createDirectories(uploadsDir);
                    }
                    Path destPath = uploadsDir.resolve(file.getName());
                    Files.copy(file.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    System.err.println("Erreur sauvegarde locale: " + e.getMessage());
                }

                txtLocalisation.setText(url);
                showSuccessNotification("Upload", "Lien copié ET sauvegarde locale dans /uploads effectuee.");
            } else {
                showAlert(Alert.AlertType.WARNING, "Upload", "Aucune URL reçue.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur upload",
                    e.getMessage() != null ? e.getMessage() : "Upload échoué.");
        }
    }

    @FXML
    private void handleAnnuler() {
        clearForm();
    }

    private void showSuccessNotification(String title, String text) {
        Notifications.create()
                .title(title)
                .text(text)
                .position(Pos.BOTTOM_RIGHT)
                .showInformation();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void sendNewResourceNotification(Ressource r) {
        Thread mailThread = new Thread(() -> {
            try {
                String dateStr = r.getDateCreation() != null ? r.getDateCreation().toString() : null;
                String html = MailService.buildNewResourceEmailHtml(
                        r.getTitle(), r.getDescription(), r.getType(), r.getCategory(), r.getAuthor(), dateStr);
                MailService.sendEmail(
                        MailService.DEFAULT_TO_EMAIL,
                        "Nouvelle ressource ajoutée - GrowMind",
                        html,
                        true);
            } catch (Exception e) {
                System.err.println("Email non envoyé : " + e.getMessage());
            }
        });
        mailThread.setDaemon(true);
        mailThread.start();
    }
}
