package org.example.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.Models.Ressource;
import org.example.Services.ressourceService;
import org.example.Services.CloudinaryService;
import org.example.Services.MailService;

import javafx.scene.control.cell.PropertyValueFactory;
import javafx.geometry.Pos;
import java.time.LocalDate;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.controlsfx.control.Notifications;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class RessourceFXController {

    @FXML
    private TextField txtTitre; // Should be read-only or removed if we strictly follow "Ajouter opens new
                                // interface"
    @FXML
    private TextArea txtDescription;
    @FXML
    private TextField txtLocalisation;
    @FXML
    private TextField txtAuthor;
    @FXML
    private DatePicker datePicker;
    @FXML
    private ComboBox<String> comboType;
    @FXML
    private ComboBox<String> comboCategory;
    @FXML
    private ComboBox<String> comboStatus;

    @FXML
    private Button btnAjouter;
    @FXML
    private Button btnModifier;
    @FXML
    private Button btnSupprimer;

    @FXML
    private Button btnFloatingAdd;

    @FXML
    private TableView<Ressource> listEvenements; // Mapped to the TableView in FXML

    @FXML
    private TableColumn<Ressource, String> colTitre;

    @FXML
    private TableColumn<Ressource, String> colDescription;

    @FXML
    private TableColumn<Ressource, String> colType;

    @FXML
    private TableColumn<Ressource, String> colCategory;

    @FXML
    private TableColumn<Ressource, LocalDate> colDate;

    @FXML
    private TableColumn<Ressource, String> colAuthor;

    @FXML
    private TextField searchField;

    @FXML
    private Label lblCount;

    @FXML
    private Button btnStat;

    @FXML
    private Button btnUploadImage;

    @FXML
    private VBox listPanel;
    @FXML
    private VBox statsPanel;

    @FXML
    private VBox formPanel;

    private final ressourceService service = new ressourceService();
    private CloudinaryService cloudinaryService; // lazy init so app runs without config

    private ObservableList<Ressource> ressourceList = FXCollections.observableArrayList();
    private ObservableList<Ressource> filteredList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        comboType.getItems().addAll("formation", "article", "video", "image", "pdf", "evenement");
        comboCategory.getItems().addAll("santé", "bien-etre", "developement personel", "motivation");
        comboStatus.getItems().addAll("Active", "Draft");

        // Initialize Table Columns
        colTitre.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
        colAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));

        refreshList();

        // Add selection listener
        listEvenements.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                showDetails(newValue);
            }
        });
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().toLowerCase();

        if (query.isEmpty()) {
            listEvenements.setItems(ressourceList);
            lblCount.setText(ressourceList.size() + " ressource(s)");
            return;
        }

        List<Ressource> filtered = ressourceList.stream()
                .filter(r -> r.getTitle().toLowerCase().contains(query) ||
                        r.getDescription().toLowerCase().contains(query) ||
                        r.getType().toLowerCase().contains(query) ||
                        r.getCategory().toLowerCase().contains(query))
                .toList();

        filteredList.setAll(filtered);
        listEvenements.setItems(filteredList);
        lblCount.setText(filteredList.size() + " ressource(s)");
    }

    public void refreshList() {
        List<Ressource> list = service.findAll();
        ressourceList.setAll(list);
        listEvenements.setItems(ressourceList);
        lblCount.setText(list.size() + " ressource(s)");
    }

    private void showDetails(Ressource r) {
        if (txtTitre != null)
            txtTitre.setText(r.getTitle());
        if (txtDescription != null)
            txtDescription.setText(r.getDescription());
        if (txtLocalisation != null)
            txtLocalisation.setText(r.getContent());
        if (txtAuthor != null)
            txtAuthor.setText(r.getAuthor());
        if (datePicker != null)
            datePicker.setValue(r.getDateCreation());
        if (comboType != null)
            comboType.setValue(r.getType());
        if (comboCategory != null)
            comboCategory.setValue(r.getCategory());
        if (comboStatus != null)
            comboStatus.setValue(r.getStatus() != null ? r.getStatus() : "Active");
    }

    private boolean containsBadWord(String text) {
        return org.example.utils.BadWordFilter.containsBadWord(text);
    }

    private void showSuccessNotification(String title, String text) {
        Notifications.create()
                .title(title)
                .text(text)
                .position(Pos.BOTTOM_RIGHT)
                .showInformation();
    }

    private void showWarningNotification(String title, String text) {
        Notifications.create()
                .title(title)
                .text(text)
                .position(Pos.BOTTOM_RIGHT)
                .showWarning();
    }

    /** Sends email notification in background so UI doesn't block. */
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
                e.printStackTrace();
                javafx.application.Platform.runLater(
                        () -> showWarningNotification("Email", "Notification email non envoyée : " + e.getMessage()));
            }
        });
        mailThread.setDaemon(true);
        mailThread.start();
    }

    @FXML
    private void ajouterEvenement() {
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
        if (txtAuthor != null && (author == null || author.trim().isEmpty())) {
            errors.append("- L'auteur est obligatoire.\n");
        }

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Erreurs de validation",
                    "Veuillez corriger les erreurs suivantes :\n\n" + errors.toString());
            return;
        }

        if (containsBadWord(title) || containsBadWord(description)) {
            org.example.utils.BadWordFilter.showBadWordNotifications();
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
        refreshList();
        clearForm();
        showSuccessNotification("✅ Succès", "Ressource ajoutée avec succès !");
        sendNewResourceNotification(r);
    }

    private void clearForm() {
        txtTitre.setText("");
        txtDescription.setText("");
        txtLocalisation.setText("");
        if (txtAuthor != null)
            txtAuthor.setText("Admin");
        datePicker.setValue(LocalDate.now());
        comboType.setValue(null);
        comboCategory.setValue(null);
        comboStatus.setValue("Active");
    }

    @FXML
    private void modifierEvenement() {
        Ressource selected = listEvenements.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Veuillez sélectionner une ressource à modifier.");
            return;
        }
        String title = txtTitre.getText();
        String description = txtDescription.getText();
        if (containsBadWord(title) || containsBadWord(description)) {
            org.example.utils.BadWordFilter.showBadWordNotifications();
            return;
        }
        selected.setTitle(title);
        selected.setDescription(description);
        selected.setType(comboType.getValue());
        selected.setCategory(comboCategory.getValue());
        selected.setContent(txtLocalisation.getText());
        selected.setAuthor(txtAuthor != null ? txtAuthor.getText() : selected.getAuthor());
        if (datePicker.getValue() != null)
            selected.setDateCreation(datePicker.getValue());
        selected.setStatus(comboStatus.getValue() != null ? comboStatus.getValue() : "Active");
        service.update(selected);
        refreshList();
        showSuccessNotification("✅ Succès", "Ressource modifiée avec succès !");
    }

    @FXML
    private void supprimerEvenement() {
        Ressource selected = listEvenements.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Veuillez sélectionner une ressource à supprimer.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer la ressource ?");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer : " + selected.getTitle() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            service.delete(selected.getId());
            refreshList();
            showSuccessNotification("✅ Succès", "Ressource supprimée avec succès !");
        }
    }

    // Hover effects (can be empty if handled by CSS or generic handlers,
    // but FXML refs them so they need to exist)
    @FXML
    private void hoverAjouter() {
    }

    @FXML
    private void exitAjouter() {
    }

    @FXML
    private void hoverModifier() {
    }

    @FXML
    private void exitModifier() {
    }

    @FXML
    private void hoverSupprimer() {
    }

    @FXML
    private void exitSupprimer() {
    }

    @FXML
    private void handleListClick() {
        Ressource r = listEvenements.getSelectionModel().getSelectedItem();
        if (r != null) {
            txtTitre.setText(r.getTitle());
            txtDescription.setText(r.getDescription());
            comboType.setValue(r.getType());
            comboCategory.setValue(r.getCategory());
            datePicker.setValue(r.getDateCreation());
            txtLocalisation.setText(r.getContent());
            txtAuthor.setText(r.getAuthor());
            comboStatus.setValue(r.getStatus());
        }
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
                new javafx.stage.FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp"),
                new javafx.stage.FileChooser.ExtensionFilter("Vidéos", "*.mp4", "*.webm", "*.mov", "*.avi"),
                new javafx.stage.FileChooser.ExtensionFilter("PDF", "*.pdf"),
                new javafx.stage.FileChooser.ExtensionFilter("Tous les fichiers", "*.*"));
        File file = chooser.showOpenDialog(txtLocalisation.getScene().getWindow());
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
                showAlert(Alert.AlertType.INFORMATION, "Upload réussi",
                        "Lien Cloudinary copié ET sauvegarde locale dans /uploads effectuee.");
            } else {
                showAlert(Alert.AlertType.WARNING, "Upload", "Aucune URL reçue.");
            }
        } catch (IllegalStateException e) {
            showAlert(Alert.AlertType.WARNING, "Cloudinary non configuré", e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur upload",
                    e.getMessage() != null ? e.getMessage() : "Upload échoué.");
        }
    }

    @FXML
    private void openStats() {
        try {
            if (statsPanel.getChildren().isEmpty()) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/statsContent.fxml"));
                Parent statsRoot = loader.load();
                StatsController statsCtrl = loader.getController();
                statsCtrl.setOnBack(this::showListPanel);
                statsPanel.getChildren().setAll(statsRoot);
            }
            formPanel.setVisible(false);
            formPanel.setManaged(false);
            listPanel.setVisible(false);
            listPanel.setManaged(false);
            statsPanel.setVisible(true);
            statsPanel.setManaged(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void showListPanel() {
        statsPanel.setVisible(false);
        statsPanel.setManaged(false);
        formPanel.setVisible(true);
        formPanel.setManaged(true);
        listPanel.setVisible(true);
        listPanel.setManaged(true);
    }

    @FXML
    private void switchToUser() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/userressource.fxml"));
            Stage stage = (Stage) listEvenements.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("GrowMind - Vue Utilisateur");
        } catch (IOException e) {
            e.printStackTrace();
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
