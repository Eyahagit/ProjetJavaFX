package Controllers;

import entities.ForumPost;
import Models.users;
import Services.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class PostDetailsController {

    @FXML private Label lblNom;
    @FXML private Label lblRole;
    @FXML private Label lblCategorie;
    @FXML private Label lblDateCreation;
    @FXML private Label lblStatut;
    @FXML private Label lblLikes;
    @FXML private Label lblDislikes;
    @FXML private Label lblVues;
    @FXML private Label lblBadge;
    @FXML private TextArea txtContenu;
    @FXML private Button btnFermer;
    @FXML private Button btnLike;
    @FXML private Button btnDislike;
    @FXML private Button btnRepondre;

    private ForumPost post;
    private ServiceForumPost servicefp;
    private users currentUser; // Ajout de l'utilisateur courant
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @FXML
    public void initialize() {
        txtContenu.setEditable(false);
        txtContenu.setWrapText(true);
        txtContenu.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; -fx-padding: 15; -fx-font-size: 14px;");
    }

    public void setPost(ForumPost post) {
        this.post = post;
        afficherDetails();
    }

    public void setService(ServiceForumPost service) {
        this.servicefp = service;
    }

    /**
     * Définit l'utilisateur courant
     */
    public void setCurrentUser(users user) {
        this.currentUser = user;
        System.out.println("✅ Utilisateur défini dans PostDetailsController: " +
                (user != null ? user.getEmail() : "null"));
    }

    private void afficherDetails() {
        lblNom.setText(post.getNom());
        lblRole.setText(getRoleDisplay(post.getRole()));

        String icone = getIconeCategorie(post.getCategorie());
        String couleur = getCouleurCategorie(post.getCategorie());
        lblCategorie.setText(icone + " " + post.getCategorie().replaceAll("[😟🧠😢🧘💤💪👨‍👩‍👧💼🍎🏃]", "").trim());
        lblCategorie.setStyle("-fx-background-color: " + couleur + "; -fx-text-fill: white; -fx-padding: 5 15; -fx-background-radius: 20;");

        lblDateCreation.setText(post.getDateCreation().format(formatter));
        txtContenu.setText(post.getContenu());

        lblLikes.setText("❤️ " + post.getLikes());
        lblDislikes.setText("👎 " + post.getDislikes());
        lblVues.setText("👁️ " + post.getVues());
        lblBadge.setText(post.getBadge());

        styliserBadge();

        if (post.isArchive()) {
            lblStatut.setText("Archivé");
            lblStatut.setStyle("-fx-background-color: #F39C12; -fx-text-fill: white; -fx-padding: 5 15; -fx-background-radius: 20; -fx-font-weight: bold;");
        } else {
            lblStatut.setText("Actif");
            lblStatut.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white; -fx-padding: 5 15; -fx-background-radius: 20; -fx-font-weight: bold;");
        }

        if (btnLike != null) {
            btnLike.setText("👍 " + post.getLikes());
        }
        if (btnDislike != null) {
            btnDislike.setText("👎 " + post.getDislikes());
        }
    }

    /**
     * Convertit le rôle en texte affichable
     */
    private String getRoleDisplay(String role) {
        if (role == null) return "";
        switch(role.toLowerCase()) {
            case "patient": return "🩺 Patient";
            case "medecin": return "👨‍⚕️ Médecin";
            case "admin": return "👑 Administrateur";
            default: return "";
        }
    }

    private void styliserBadge() {
        String badge = post.getBadge();
        String couleur;

        if (badge.contains("🏆")) couleur = "#FFD700";
        else if (badge.contains("⭐")) couleur = "#C0C0C0";
        else if (badge.contains("🩺") || badge.contains("🌟")) couleur = "#CD7F32";
        else if (badge.contains("💫")) couleur = "#3498DB";
        else couleur = "#2ECC71";

        lblBadge.setStyle("-fx-background-color: " + couleur + "; -fx-text-fill: white; -fx-padding: 5 15; -fx-background-radius: 20; -fx-font-weight: bold;");
    }

    private String getIconeCategorie(String categorie) {
        if (categorie.contains("Anxiété")) return "😟";
        if (categorie.contains("Stress")) return "🧠";
        if (categorie.contains("Dépression")) return "😢";
        if (categorie.contains("Méditation")) return "🧘";
        if (categorie.contains("Sommeil")) return "💤";
        if (categorie.contains("Estime")) return "💪";
        if (categorie.contains("Famille")) return "👨‍👩‍👧";
        if (categorie.contains("Travail")) return "💼";
        if (categorie.contains("Alimentation")) return "🍎";
        if (categorie.contains("Sport")) return "🏃";
        return "💬";
    }

    private String getCouleurCategorie(String categorie) {
        if (categorie.contains("Anxiété")) return "#DC2626";
        if (categorie.contains("Stress")) return "#D97706";
        if (categorie.contains("Dépression")) return "#2563EB";
        if (categorie.contains("Méditation")) return "#059669";
        if (categorie.contains("Sommeil")) return "#4F46E5";
        if (categorie.contains("Estime")) return "#B45309";
        if (categorie.contains("Famille")) return "#DB2777";
        if (categorie.contains("Travail")) return "#7C3AED";
        if (categorie.contains("Alimentation")) return "#16A34A";
        if (categorie.contains("Sport")) return "#EA580C";
        return "#6B7280";
    }

    @FXML
    private void handleLike() {
        if (servicefp != null && post != null) {
            servicefp.incrementerLike(post.getIdPost());
            post.setLikes(post.getLikes() + 1);
            lblLikes.setText("❤️ " + post.getLikes());
            if (btnLike != null) {
                btnLike.setText("👍 " + post.getLikes());
            }
        }
    }

    @FXML
    private void handleDislike() {
        if (servicefp != null && post != null) {
            servicefp.incrementerDislike(post.getIdPost());
            post.setDislikes(post.getDislikes() + 1);
            lblDislikes.setText("👎 " + post.getDislikes());
            if (btnDislike != null) {
                btnDislike.setText("👎 " + post.getDislikes());
            }
        }
    }

    @FXML
    private void handleRepondre() {
        // Vérifier si l'utilisateur est connecté
        if (currentUser == null) {
            showAlert("Accès refusé", "Vous devez être connecté pour répondre à une discussion.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/reponse_form.fxml"));
            VBox page = loader.load();

            ReponseFormController controller = loader.getController();

            // Passer l'utilisateur courant au formulaire de réponse
            if (controller != null) {
                controller.setCurrentUser(currentUser);
                controller.setPost(post);
            }

            Stage stage = new Stage();
            stage.setTitle("Répondre à " + post.getNom());
            stage.setScene(new Scene(page));
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire de réponse");
        }
    }

    @FXML
    private void handleFermer() {
        ((Stage) btnFermer.getScene().getWindow()).close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}