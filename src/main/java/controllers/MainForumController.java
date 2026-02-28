package Controllers;

import Models.users;
import Services.ServiceForumPost;
import Services.ServiceReponse;
import utils.TwilioUtil;
import utils.GeolocationUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MainForumController {

    @FXML private AnchorPane rootPane;
    @FXML private Label lblDate;
    @FXML private Label lblUserRole;
    @FXML private Label lblPatientsCount;
    @FXML private Label lblMedecinsCount;
    @FXML private Label lblPostsCount;
    @FXML private Label lblReponsesCount;
    @FXML private TextField searchField;

    // ✅ Changé pour utiliser le modèle users
    private users currentUser;
    private boolean isMedecin = false;
    private ServiceForumPost servicePost;
    private ServiceReponse serviceReponse;

    @FXML
    public void initialize() {
        servicePost = new ServiceForumPost();
        serviceReponse = new ServiceReponse();

        lblDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        // Initialiser avec un utilisateur par défaut si nécessaire
        if (currentUser == null) {
            currentUser = new users();
            currentUser.setRole("patient");
            currentUser.setName("Invité");
        }

        updateRoleDisplay();

        updateStatistics();
    }

    /**
     * Met à jour l'affichage du rôle
     */
    private void updateRoleDisplay() {
        if (currentUser != null) {
            String roleText = getRoleDisplay(currentUser.getRole());
            lblUserRole.setText(roleText);
        }
    }

    /**
     * Convertit le rôle en texte affichable
     */
    private String getRoleDisplay(String role) {
        if (role == null) return "👤 Invité";

        switch(role) {
            case "admin": return "👑 Administrateur";
            case "doctor": return "👨‍⚕️ Médecin";
            case "patient": return "👤 Patient";
            default: return "👤 Utilisateur";
        }
    }

    private void updateStatistics() {
        try {
            int totalPosts = servicePost.compter();
            lblPostsCount.setText(String.valueOf(totalPosts));
            lblPatientsCount.setText("1,254"); // À remplacer par des données réelles
            lblMedecinsCount.setText("89");    // À remplacer par des données réelles

            //int totalReponses = serviceReponse.compter();
            //lblReponsesCount.setText(String.valueOf(totalReponses));
        } catch (Exception e) {
            lblPostsCount.setText("0");
            lblReponsesCount.setText("0");
        }
    }

    @FXML
    public void handleForum() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/post_list.fxml"));
            VBox postView = loader.load();

            Controllers.PostListController controller = loader.getController();
            controller.setMainController(this);

            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(new Scene(postView, 1300, 750));
        } catch (IOException e) {
            showError("Erreur", "Impossible de charger le forum");
        }
    }

    @FXML
    public void handleNouvelleDiscussion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/post_form.fxml"));
            VBox page = loader.load();

            Controllers.PostFormController controller = loader.getController();
            controller.setService(servicePost);
            controller.setMainController(this);
            controller.setMode(Controllers.PostFormController.Mode.AJOUT);

            Stage stage = new Stage();
            stage.setTitle("Nouvelle discussion");
            stage.setScene(new Scene(page));
            stage.showAndWait();

            updateStatistics();
        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir le formulaire");
        }
    }

    @FXML
    public void handleEspacePatient() {
        if (currentUser != null) {
            currentUser.setRole("patient");
            isMedecin = false;
            updateRoleDisplay();

            showInfo("Espace Patient",
                    "👥 Bienvenue dans votre espace personnel\n\n" +
                            "Fonctionnalités disponibles :\n" +
                            "• 📝 Créer des publications\n" +
                            "• 💬 Répondre aux discussions\n" +
                            "• ❤️ Liker les conseils utiles\n" +
                            "• ⚠️ Signaler les contenus inappropriés");
        }
    }

    @FXML
    public void handleEspaceMedecin() {
        if (currentUser != null) {
            currentUser.setRole("doctor");
            isMedecin = true;
            updateRoleDisplay();

            showInfo("Espace Médecin",
                    "👨‍⚕️ Bienvenue dans votre espace professionnel\n\n" +
                            "Fonctionnalités disponibles :\n" +
                            "• 👨‍⚕️ Publier des conseils certifiés\n" +
                            "• 💬 Répondre aux patients\n" +
                            "• 🛡️ Modérer les contenus signalés\n" +
                            "• 🗑️ Supprimer les publications inappropriées");
        }
    }

    @FXML
    public void handleSwitchRole() {
        if (isMedecin) {
            handleEspacePatient();
        } else {
            handleEspaceMedecin();
        }
    }

    @FXML
    public void handleRessources() {
        showInfo("📚 Ressources recommandées",
                "Applications de bien-être :\n\n" +
                        "🧘 Petit BamBou - Méditation guidée\n" +
                        "🌿 Mindfulness - Pleine conscience\n" +
                        "😌 Calm - Relaxation et sommeil\n" +
                        "💤 Headspace - Méditation pour débutants\n\n" +
                        "📖 Livres recommandés :\n" +
                        "• 'Le piège du bonheur' - Russ Harris\n" +
                        "• 'Méditer pour ne plus déprimer' - Mark Williams\n" +
                        "• 'L'anti-régime' - Michel Freud");
    }

    @FXML
    public void handleUrgence() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("🆘 AIDE D'URGENCE");
        dialog.setHeaderText("Êtes-vous en situation de détresse ?");

        dialog.getDialogPane().setStyle("-fx-background-color: #fff5f5; -fx-border-color: #E53E3E; -fx-border-width: 2; -fx-border-radius: 10;");

        ButtonType btnSMS = new ButtonType("📱 Envoyer alerte SMS", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnAppel = new ButtonType("📞 Appeler le 3114", ButtonBar.ButtonData.OTHER);
        ButtonType btnAnnuler = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().addAll(btnSMS, btnAppel, btnAnnuler);

        VBox content = new VBox(15);
        content.setStyle("-fx-padding: 20;");

        Label message = new Label(
                "🌿 Vous n'êtes pas seul(e).\n\n" +
                        "➡️ **SMS** : Une alerte sera envoyée à notre équipe\n" +
                        "➡️ **Appel** : Ligne d'écoute immédiate 3114"
        );
        message.setWrapText(true);
        message.setStyle("-fx-font-size: 14px; -fx-text-fill: #2d3748;");

        CheckBox chkLocalisation = new CheckBox("Partager ma localisation (recommandé)");
        chkLocalisation.setStyle("-fx-font-weight: bold; -fx-text-fill: #E53E3E;");

        TextArea txtMessage = new TextArea();
        txtMessage.setPromptText("Message personnalisé (optionnel)...");
        txtMessage.setPrefRowCount(3);

        content.getChildren().addAll(message, chkLocalisation, txtMessage);
        dialog.getDialogPane().setContent(content);

        dialog.showAndWait().ifPresent(response -> {
            if (response == btnSMS) {
                envoyerAlerteSMS(
                        txtMessage.getText().trim(),
                        chkLocalisation.isSelected()
                );
            } else if (response == btnAppel) {
                appelUrgence3114();
            }
        });
    }

    private void envoyerAlerteSMS(String messagePerso, boolean partagerLocalisation) {
        try {
            String nom = (currentUser != null && currentUser.getName() != null) ?
                    currentUser.getName() : "Anonyme";

            String localisation = "";
            if (partagerLocalisation) {
                try {
                    localisation = GeolocationUtil.getLocalisationParIP();
                } catch (Exception e) {
                    localisation = "Localisation non disponible";
                }
            }

            String message = messagePerso.isEmpty() ?
                    "Besoin d'aide immédiate" : messagePerso;

            TwilioUtil.envoyerAlerteSMS(nom, message, localisation);
            journaliserAlerte(nom, message, localisation);

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("✅ Alerte envoyée");
            success.setHeaderText("Votre demande d'urgence a été transmise");
            success.setContentText("Un professionnel va vous contacter dans les plus brefs délais.\n\n🌿 Prenez soin de vous.");
            success.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'envoyer l'alerte: " + e.getMessage());
        }
    }

    private void appelUrgence3114() {
        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI("tel:3114"));
        } catch (Exception e) {
            showError("Erreur", "Impossible de lancer l'appel.\nComposez le 3114 manuellement.");
        }
    }

    private void journaliserAlerte(String nom, String message, String localisation) {
        System.out.println("📝 ALERTE ENREGISTRÉE - " + java.time.LocalDateTime.now());
        System.out.println("   Nom: " + nom);
        System.out.println("   Message: " + message);
        System.out.println("   Localisation: " + localisation);
    }

    @FXML
    public void handleSearch() {
        String query = searchField.getText();
        if (!query.isEmpty()) {
            showInfo("Recherche", "🔍 Recherche de : " + query + "\n\nFonctionnalité à venir !");
        }
    }

    @FXML
    public void handleAssistantIA() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gemini_chat.fxml"));
            VBox chatView = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Assistant IA - GrowMind");
            stage.setScene(new Scene(chatView, 450, 700));
            stage.show();
        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir l'assistant IA");
            e.printStackTrace();
        }
    }

    // ========== RETOUR À L'ACCUEIL ==========

    @FXML
    public void handleRetourAccueil() {
        System.out.println("\n=== Retour à l'accueil depuis le forum ===");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/home.fxml"));
            Parent root = loader.load();

            HomeController homeController = loader.getController();
            if (homeController != null && currentUser != null) {
                homeController.setUser(currentUser);
                System.out.println("✅ Utilisateur transmis au HomeController");
            }

            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Accueil - GrowMind");
            stage.setMaximized(true);
            stage.show();

            System.out.println("✅ Retour à l'accueil réussi");

        } catch (IOException e) {
            System.err.println("❌ Erreur retour accueil: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur", "Impossible de retourner à l'accueil.\n" + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Erreur inattendue: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur", "Erreur inattendue: " + e.getMessage());
        }
    }

    /**
     * Reçoit l'utilisateur connecté depuis HomeController
     */
    public void setUser(users user) {
        this.currentUser = user;
        if (user != null) {
            updateRoleDisplay();
            System.out.println("✅ Utilisateur reçu dans MainForumController: " + user.getEmail());
        }
    }

    public void refreshStatistics() {
        updateStatistics();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText("❌ " + message);
        alert.showAndWait();
    }
}