package controllers;

import entities.ForumPost;
import services.ServiceForumPost;
import utils.BadWordsFilter;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.LocalDateTime;

public class PostFormController {

    public enum Mode { AJOUT, MODIFICATION }

    @FXML private Label lblTitre;
    @FXML private TextField txtNom;
    @FXML private ComboBox<String> cmbRole;
    @FXML private ComboBox<String> cmbCategorie;
    @FXML private TextArea txtContenu;
    @FXML private CheckBox chkArchive;
    @FXML private Button btnSauvegarder;
    @FXML private Button btnAnnuler;

    private ServiceForumPost service;
    private PostListController postListController;
    private MainForumController mainController;
    private ForumPost postCourant;
    private Mode modeCourant;
    private boolean isFrench = true;

    @FXML
    public void initialize() {
        cmbRole.setItems(FXCollections.observableArrayList(
                "🧑 Patient", "👨‍⚕️ Médecin", "🧘 Thérapeute"
        ));
        cmbRole.setValue("🧑 Patient");

        cmbCategorie.setItems(FXCollections.observableArrayList(
                "😟 Anxiété", "🧠 Stress", "😢 Dépression", "🧘 Méditation", "💬 Général"
        ));
        cmbCategorie.setValue("💬 Général");

        txtNom.setPromptText("Votre nom ou pseudo");
        txtContenu.setPromptText("Écrivez votre message ici...");
    }

    public void setService(ServiceForumPost service) {
        this.service = service;
    }

    public void setPostListController(PostListController controller) {
        this.postListController = controller;
        if (controller != null) {
            this.isFrench = controller.isFrench();
        }
    }

    public void setMainController(MainForumController controller) {
        this.mainController = controller;
    }

    public void setMode(Mode mode) {
        this.modeCourant = mode;
        if (mode == Mode.AJOUT) {
            lblTitre.setText(isFrench ? "➕ Nouvelle publication" : "➕ New post");
            btnSauvegarder.setText(isFrench ? "Publier" : "Publish");
            chkArchive.setVisible(false);
            txtNom.clear();
            txtContenu.clear();
        } else {
            lblTitre.setText(isFrench ? "✏️ Modifier la publication" : "✏️ Edit post");
            btnSauvegarder.setText(isFrench ? "Mettre à jour" : "Update");
            chkArchive.setVisible(true);
        }
    }

    public void setPost(ForumPost post) {
        this.postCourant = post;
        txtNom.setText(post.getNom());
        cmbRole.setValue(post.getRole());
        cmbCategorie.setValue(post.getCategorie());
        txtContenu.setText(post.getContenu());
        chkArchive.setSelected(post.isArchive());
    }

    @FXML
    private void handleSauvegarder() {
        String nom = txtNom.getText().trim();
        String contenu = txtContenu.getText().trim();

        if (nom.isEmpty()) {
            showError(isFrench ? "Le nom est requis" : "Name is required");
            txtNom.requestFocus();
            return;
        }

        if (nom.length() < 3) {
            showError(isFrench ? "Le nom doit contenir au moins 3 caractères" : "Name must be at least 3 characters");
            txtNom.requestFocus();
            return;
        }

        if (contenu.isEmpty()) {
            showError(isFrench ? "Le contenu est requis" : "Content is required");
            txtContenu.requestFocus();
            return;
        }

        if (contenu.length() < 10) {
            showError(isFrench ? "Le contenu doit contenir au moins 10 caractères" : "Content must be at least 10 characters");
            txtContenu.requestFocus();
            return;
        }

        // ✅ FILTRAGE AUTOMATIQUE AVEC ***
        String contenuFiltre = BadWordsFilter.filter(contenu);
        String nomFiltre = BadWordsFilter.filter(nom);

        if (!contenu.equals(contenuFiltre) || !nom.equals(nomFiltre)) {
            showInfo(isFrench ? "Information" : "Information",
                    isFrench ? "Des mots inappropriés ont été automatiquement remplacés par ***"
                            : "Inappropriate words have been automatically replaced with ***");
        }

        if (modeCourant == Mode.AJOUT) {
            ForumPost nouveauPost = new ForumPost(
                    nomFiltre,
                    cmbRole.getValue(),
                    cmbCategorie.getValue(),
                    contenuFiltre,
                    LocalDateTime.now(),
                    false
            );
            service.ajouter(nouveauPost);
            showSuccess(isFrench ? "Publication publiée avec succès !" : "Post published successfully!");
        } else {
            if (postCourant == null) {
                showError(isFrench ? "Publication non trouvée" : "Post not found");
                return;
            }
            postCourant.setNom(nomFiltre);
            postCourant.setRole(cmbRole.getValue());
            postCourant.setCategorie(cmbCategorie.getValue());
            postCourant.setContenu(contenuFiltre);
            postCourant.setArchive(chkArchive.isSelected());
            service.modifier(postCourant);
            showSuccess(isFrench ? "Publication modifiée avec succès !" : "Post updated successfully!");
        }

        if (postListController != null) {
            postListController.refreshTable();
        }

        if (mainController != null) {
            mainController.refreshStatistics();
        }

        fermerFenetre();
    }

    @FXML
    private void handleAnnuler() {
        fermerFenetre();
    }

    private void fermerFenetre() {
        ((Stage) btnSauvegarder.getScene().getWindow()).close();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(isFrench ? "Erreur" : "Error");
        alert.setHeaderText(null);
        alert.setContentText("❌ " + message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(isFrench ? "Succès" : "Success");
        alert.setHeaderText(null);
        alert.setContentText("✅ " + message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText("ℹ️ " + message);
        alert.showAndWait();
    }
}