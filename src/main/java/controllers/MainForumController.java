package Controllers;

import Models.users;
import Services.ServiceForumPost;
import Services.*;
import Services.ServicePatient;
import Services.ServiceDoctor;
import Services.ServiceAdmin;
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
    @FXML private Label lblUserWelcome;
    @FXML private Label lblUserFullName;  // Nouveau label pour nom complet
    @FXML private Label lblPatientsCount;
    @FXML private Label lblMedecinsCount;
    @FXML private Label lblPostsCount;
    @FXML private Label lblReponsesCount;
    @FXML private TextField searchField;

    // Boutons de navigation
    @FXML private Button btnForum;
    @FXML private Button btnNouvelleDiscussion;
    @FXML private Button btnEspacePatient;
    @FXML private Button btnEspaceMedecin;
    @FXML private Button btnRessources;
    @FXML private Button btnAssistantIA;
    @FXML private Button btnUrgence;
    @FXML private Button btnSwitchRole;
    @FXML private Button btnRetourAccueil;
    @FXML private Button btnCommencerDiscussion;
    @FXML private Button btnVoirForum;
    @FXML private Button btnRechercher;

    // Utilisateur connecté
    private users currentUser;
    private boolean isMedecin = false;
    private boolean isAdmin = false;

    // Services
    private ServiceForumPost servicePost;
    private ServiceReponse serviceReponse;
    private ServicePatient servicePatient;
    private ServiceDoctor serviceDoctor;
    private ServiceAdmin serviceAdmin;

    @FXML
    public void initialize() {
        System.out.println("\n=== Initialisation MainForumController ===");

        // Initialiser les services
        servicePost = new ServiceForumPost();
        serviceReponse = new ServiceReponse();
        servicePatient = new ServicePatient();
        serviceDoctor = new ServiceDoctor();
        serviceAdmin = new ServiceAdmin();

        // Afficher la date
        lblDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        // NE PAS créer d'utilisateur par défaut - attendre setUser()
        System.out.println("=== En attente de l'utilisateur depuis la page de login ===\n");
    }

    /**
     * Configure l'interface en fonction du rôle de l'utilisateur
     */
    private void configureInterfaceByRole() {
        if (currentUser == null) return;

        String role = currentUser.getRole().toLowerCase();
        isAdmin = "admin".equals(role);
        isMedecin = "medecin".equals(role);

        System.out.println("🔧 Configuration interface pour rôle: " + role);

        // ADMIN : voit tous les boutons
        if (isAdmin) {
            setButtonsVisibility(true, true, true, true, true, true, true, true, true);
            System.out.println("✅ Mode Admin: tous les boutons visibles");
        }
        // MEDECIN : voit tous les boutons
        else if (isMedecin) {
            setButtonsVisibility(true, true, true, true, true, true, true, true, true);
            System.out.println("✅ Mode Médecin: accès complet");
        }
        // PATIENT : voit les boutons de base
        else if ("patient".equals(role)) {
            setButtonsVisibility(true, true, true, false, true, false, true, true, true);
            System.out.println("✅ Mode Patient: accès limité");
        }
    }

    /**
     * Contrôle la visibilité de tous les boutons
     */
    private void setButtonsVisibility(boolean forum, boolean nouvelleDiscussion,
                                      boolean espacePatient, boolean espaceMedecin,
                                      boolean ressources, boolean assistantIA,
                                      boolean urgence, boolean commencerDiscussion,
                                      boolean voirForum) {
        if (btnForum != null) btnForum.setVisible(forum);
        if (btnNouvelleDiscussion != null) btnNouvelleDiscussion.setVisible(nouvelleDiscussion);
        if (btnEspacePatient != null) btnEspacePatient.setVisible(espacePatient);
        if (btnEspaceMedecin != null) btnEspaceMedecin.setVisible(espaceMedecin);
        if (btnRessources != null) btnRessources.setVisible(ressources);
        if (btnAssistantIA != null) btnAssistantIA.setVisible(assistantIA);
        if (btnUrgence != null) btnUrgence.setVisible(urgence);
        if (btnCommencerDiscussion != null) btnCommencerDiscussion.setVisible(commencerDiscussion);
        if (btnVoirForum != null) btnVoirForum.setVisible(voirForum);
        if (btnRechercher != null) btnRechercher.setVisible(true);
    }

    /**
     * Met à jour l'affichage avec les informations de l'utilisateur
     */
    private void updateUserDisplay() {
        if (currentUser != null) {
            // Message de bienvenue
            String welcomeText = "Bienvenue, " + currentUser.getName() + " !";
            if (lblUserWelcome != null) {
                lblUserWelcome.setText(welcomeText);
            }

            // Nom complet (prénom + nom)
            if (lblUserFullName != null) {
                String fullName = currentUser.getName() + " " + currentUser.getSecond_name();
                lblUserFullName.setText(fullName.trim());
            }

            // Rôle avec icône
            String roleText = getRoleDisplay(currentUser.getRole());
            if (lblUserRole != null) {
                lblUserRole.setText(roleText);
            }

            // Déterminer si c'est un médecin
            isMedecin = "medecin".equals(currentUser.getRole());

            System.out.println("✅ Affichage utilisateur mis à jour: " + currentUser.getName() + " " + currentUser.getSecond_name() + " (" + currentUser.getRole() + ")");
        }
    }

    /**
     * Convertit le rôle en texte affichable avec icône
     */
    private String getRoleDisplay(String role) {
        if (role == null) return "";

        switch(role.toLowerCase()) {
            case "admin":
                return "👑 Administrateur";
            case "medecin":
                return "👨‍⚕️ Médecin";
            case "patient":
                return "🩺 Patient";
            default:
                return "";
        }
    }

    /**
     * Méthode principale pour recevoir l'utilisateur connecté
     */
    public void setCurrentUser(users user) {
        if (user != null) {
            this.currentUser = user;
            System.out.println("✅ Utilisateur défini dans MainForumController: " + user.getName() + " " + user.getSecond_name() + " (" + user.getEmail() + ")");

            // Mettre à jour l'affichage
            updateUserDisplay();

            // Configurer l'interface selon le rôle
            configureInterfaceByRole();

            // Charger les données spécifiques au rôle
            loadUserSpecificData();

            // Mettre à jour les statistiques
            updateStatistics();
        } else {
            System.err.println("❌ Tentative de définir un utilisateur null");
        }
    }

    /**
     * Alias pour setCurrentUser (pour compatibilité)
     */
    public void setUser(users user) {
        setCurrentUser(user);
    }

    /**
     * Récupère l'utilisateur courant
     */
    public users getCurrentUser() {
        return currentUser;
    }

    /**
     * Charge les données spécifiques au rôle de l'utilisateur
     */
    private void loadUserSpecificData() {
        if (currentUser == null) return;

        try {
            switch(currentUser.getRole().toLowerCase()) {
                case "patient":
                    System.out.println("📋 Mode Patient activé");
                    break;
                case "medecin":
                    System.out.println("📋 Mode Médecin activé");
                    break;
                case "admin":
                    System.out.println("📋 Mode Administrateur activé");
                    break;
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement données spécifiques: " + e.getMessage());
        }
    }

    /**
     * Met à jour les statistiques du forum
     */
    private void updateStatistics() {
        try {
            int totalPosts = servicePost.compter();
            lblPostsCount.setText(String.valueOf(totalPosts));

            lblPatientsCount.setText("1,254");
            lblMedecinsCount.setText("89");
            lblReponsesCount.setText("3,421");

        } catch (Exception e) {
            lblPostsCount.setText("0");
            lblReponsesCount.setText("0");
            lblPatientsCount.setText("0");
            lblMedecinsCount.setText("0");
            System.err.println("❌ Erreur mise à jour statistiques: " + e.getMessage());
        }
    }

    @FXML
    public void handleForum() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/post_list.fxml"));
            VBox postView = loader.load();

            PostListController controller = loader.getController();
            controller.setMainController(this);

            // Passer l'utilisateur
            if (controller != null && currentUser != null) {
                try {
                    controller.getClass().getMethod("setCurrentUser", users.class).invoke(controller, currentUser);
                } catch (Exception e) {
                    System.out.println("ℹ️ PostListController n'a pas de méthode setCurrentUser");
                }
            }

            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(new Scene(postView, 1300, 750));

            System.out.println("✅ Navigation vers la liste des posts");

        } catch (IOException e) {
            showError("Erreur", "Impossible de charger le forum: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleNouvelleDiscussion() {
        // Vérifier les droits
        if (currentUser == null) {
            showError("Accès refusé", "Vous devez être connecté pour créer une discussion.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/post_form.fxml"));
            VBox page = loader.load();

            PostFormController controller = loader.getController();
            controller.setService(servicePost);
            controller.setMainController(this);
            controller.setMode(PostFormController.Mode.AJOUT);

            // Passer l'utilisateur connecté pour pré-remplir le formulaire
            if (controller != null && currentUser != null) {
                controller.setCurrentUser(currentUser);
            }

            Stage stage = new Stage();
            stage.setTitle("Nouvelle discussion");
            stage.setScene(new Scene(page));
            stage.showAndWait();

            updateStatistics();

        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleEspacePatient() {
        if (currentUser != null) {
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
        if (currentUser != null && ("medecin".equals(currentUser.getRole()) || "admin".equals(currentUser.getRole()))) {
            showInfo("Espace Médecin",
                    "👨‍⚕️ Bienvenue dans votre espace professionnel\n\n" +
                            "Fonctionnalités disponibles :\n" +
                            "• 👨‍⚕️ Publier des conseils certifiés\n" +
                            "• 💬 Répondre aux patients\n" +
                            "• 🛡️ Modérer les contenus signalés\n" +
                            "• 🗑️ Supprimer les publications inappropriées");
        } else {
            showError("Accès refusé", "Seuls les médecins et administrateurs peuvent accéder à cet espace.");
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
        if (currentUser == null || "guest".equals(currentUser.getRole())) {
            showError("Accès refusé", "Vous devez être connecté pour utiliser l'assistant IA.");
            return;
        }

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

    @FXML
    private void handleRetourAccueil() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/home.fxml"));
            Parent root = loader.load();

            HomeController homeController = loader.getController();
            if (homeController != null && currentUser != null) {
                homeController.setUser(currentUser);
            }

            Stage stage = (Stage) btnRetourAccueil.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Accueil - GrowMind");
            stage.show();

            System.out.println("✅ Retour à l'accueil dans la même fenêtre");

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de retourner à l'accueil");
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