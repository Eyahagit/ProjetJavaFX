package Controllers;

import Models.users;
import entities.ForumPost;
import entities.Reponse;
import Services.*;
import utils.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.LocalDateTime;

public class ReponseFormController {

    @FXML private TextArea txtContenu;
    @FXML private Button btnSauvegarder;
    @FXML private Button btnAnnuler;

    private ServiceReponse service;
    private ForumPost postCourant;
    private ReponseListController reponseListController;
    private boolean isFrench = true;
    private users currentUser;

    @FXML
    public void initialize() {
        txtContenu.setPromptText("Écrivez votre réponse ici...");
    }

    public void setCurrentUser(users user) {
        this.currentUser = user;
        System.out.println("✅ Utilisateur défini dans PostDetailsController: " +
                (user != null ? user.getEmail() : "null"));
    }
    public void setPost(ForumPost post) {
        this.postCourant = post;
    }

    public void setReponseListController(ReponseListController controller) {
        this.reponseListController = controller;
        if (controller != null) {
            this.isFrench = controller.isFrench();  // ✅ Maintenant ça marche !
        }
    }

    @FXML
    private void handleSauvegarder() {
        String contenu = txtContenu.getText().trim();

        if (contenu.isEmpty()) {
            showError(isFrench ? "La réponse ne peut pas être vide" : "Reply cannot be empty");
            txtContenu.requestFocus();
            return;
        }

        if (contenu.length() < 5) {
            showError(isFrench ? "La réponse doit contenir au moins 5 caractères" : "Reply must be at least 5 characters");
            txtContenu.requestFocus();
            return;
        }

        // Filtrage automatique des mots grossiers
        String contenuFiltre = BadWordsFilter.filter(contenu);

        if (!contenu.equals(contenuFiltre)) {
            showInfo(isFrench ? "Information" : "Information",
                    isFrench ? "Des mots inappropriés ont été automatiquement remplacés par ***"
                            : "Inappropriate words have been automatically replaced with ***");
        }

        Reponse nouvelleReponse = new Reponse(
                postCourant.getIdPost(),
                "Utilisateur",
                contenuFiltre,
                LocalDateTime.now()
        );

        service.ajouter(nouvelleReponse);

        if (reponseListController != null) {
            reponseListController.refreshTable();
        }

        showSuccess(isFrench ? "Réponse publiée avec succès !" : "Reply published successfully!");
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