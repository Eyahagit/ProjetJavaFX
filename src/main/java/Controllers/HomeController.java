package Controllers;

import Models.*;
import Services.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class HomeController implements Initializable {

    // ========== ÉLÉMENTS FXML ==========
    @FXML private Label welcomeLabel;
    @FXML private Label dateLabel;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private Button logoutButton;
    @FXML private Button profileButton;
    @FXML private Button goToDashboardButton;

    // Cards pour les différentes gestions
    @FXML private VBox cabinetCard;
    @FXML private VBox forumCard;
    @FXML private VBox eventCard;
    @FXML private VBox santeCard;
    @FXML private VBox responsabiliteCard;

    // ========== SERVICES ==========
    private final ServiceUser serviceUser = new ServiceUser();
    private final ServiceAdmin serviceAdmin = new ServiceAdmin();
    private final ServiceDoctor serviceDoctor = new ServiceDoctor();
    private final ServicePatient servicePatient = new ServicePatient();

    // ========== VARIABLES ==========
    private users currentUser;
    private Stage primaryStage; // Pour stocker la fenêtre principale

    // ========== INITIALISATION ==========
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("\n=== Initialisation HomeController ===");

        // Afficher la date du jour
        try {
            LocalDate now = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy");
            dateLabel.setText(now.format(formatter));
            System.out.println("✅ Date définie: " + now.format(formatter));
        } catch (Exception e) {
            System.err.println("❌ Erreur date: " + e.getMessage());
        }

        // Vérifier si un utilisateur est déjà en session
        users sessionUser = SessionManager.getInstance().getCurrentUser();
        if (sessionUser != null) {
            setUser(sessionUser);
            System.out.println("✅ Utilisateur récupéré depuis la session: " + sessionUser.getName());
        }

        // Initialiser les cartes
        setupCards();

        System.out.println("=== Initialisation terminée ===\n");
    }

    /**
     * Définit la fenêtre principale
     */
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    // ========== CONFIGURATION DES CARTES ==========
    private void setupCards() {
        setupCard(cabinetCard, "🏥 Psychologue & Cabinet",
                "Gérer les consultations, rendez-vous et dossiers médicaux",
                "#4A6FA5", "cabinet");

        setupCard(forumCard, "💬  Forum",
                "Animer les discussions, modérer les messages",
                "#5FB49C", "forum");

        setupCard(eventCard, "📅  Événement",
                "Organiser des ateliers, conférences et événements",
                "#E667AF", "event");

        setupCard(santeCard, "🌿 Santé & Bien-être",
                "Suivre les activités, conseils et programmes",
                "#9B59B6", "sante");

        setupCard(responsabiliteCard, "⚖️ Responsabilité & bibliothèque",
                "Gérer les engagements et responsabilités",
                "#F39C12", "responsabilite");
    }

    private void setupCard(VBox card, String title, String description, String color, String moduleName) {
        if (card == null) {
            System.err.println("❌ Carte " + moduleName + " est null!");
            return;
        }

        // Style de base
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5); " +
                "-fx-padding: 20; -fx-cursor: hand;");

        // Créer le contenu
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666; -fx-wrap-text: true;");
        descLabel.setMaxWidth(200);

        // Vider la carte et ajouter le nouveau contenu
        card.getChildren().clear();
        card.getChildren().addAll(titleLabel, new Label(" "), descLabel);

        // Effets de survol
        card.setOnMouseEntered(e ->
                card.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 15; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 8); " +
                        "-fx-padding: 20; -fx-cursor: hand;")
        );

        card.setOnMouseExited(e ->
                card.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5); " +
                        "-fx-padding: 20; -fx-cursor: hand;")
        );

        // Ajouter l'événement de clic
        card.setOnMouseClicked(e -> handleCardClick(moduleName));
    }

    // ========== GESTION DES CLICS SUR LES CARTES ==========
    private void handleCardClick(String moduleName) {
        if (currentUser == null) {
            showAlert("Non connecté", "Veuillez vous connecter pour accéder à ce module.");
            return;
        }

        switch(moduleName) {
            case "cabinet":
                goToCabinet();
                break;
            case "forum":
                goToForum();
                break;
            case "event":
                goToEvent();
                break;
            case "sante":
                goToSante();
                break;
            case "responsabilite":
                goToResponsabilite();
                break;
            default:
                showModuleInfo(moduleName, moduleName);
        }
    }

    // ========== GESTION DE L'UTILISATEUR ==========
    public void setUser(users user) {
        System.out.println("\n=== HomeController.setUser ===");


        this.currentUser = user;
        SessionManager.getInstance().setCurrentUser(user);

        System.out.println("✅ Utilisateur défini: " + user.getEmail());
        System.out.println("   Rôle: " + user.getRole());
        System.out.println("   Nom: " + user.getName() + " " + user.getSecond_name());

        updateUserDisplay();
        System.out.println("=== Fin setUser ===\n");
    }

    private void updateUserDisplay() {
        try {
            if (currentUser != null) {
                if (userNameLabel != null) {
                    String fullName = currentUser.getName() + " " + currentUser.getSecond_name();
                    userNameLabel.setText(fullName.trim());
                }

                if (welcomeLabel != null) {
                    welcomeLabel.setText("Bienvenue, " + currentUser.getName() + " !");
                }

                if (userRoleLabel != null) {
                    String roleText = "";
                    String roleColor = "";

                    switch(currentUser.getRole().toLowerCase()) {
                        case "admin":
                            roleText = "👑 Administrateur";
                            roleColor = "#9B59B6";
                            break;
                        case "medecin":
                            roleText = "👨‍⚕️ Médecin";
                            roleColor = "#E667AF";
                            break;
                        case "patient":
                            roleText = "🩺 Patient";
                            roleColor = "#5FB49C";
                            break;
                    }

                    userRoleLabel.setText(roleText);
                    userRoleLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                }

                if (profileButton != null) profileButton.setVisible(true);
                if (goToDashboardButton != null) goToDashboardButton.setVisible(true);
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la mise à jour des labels: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========== REDIRECTION VERS LE DASHBOARD ==========
    @FXML
    private void goToDashboard() {
        System.out.println("\n=== Redirection vers dashboard ===");

        if (currentUser == null || "guest".equals(currentUser.getRole())) {
            showAlert("Accès refusé", "Veuillez vous connecter pour accéder au dashboard.");
            return;
        }

        try {
            String fxmlFile;
            String title;

            switch(currentUser.getRole()) {
                case "admin":
                    fxmlFile = "/admin_dashboard.fxml";
                    title = "Admin Dashboard";
                    break;
                case "doctor":
                    fxmlFile = "/doctor_dashboard.fxml";
                    title = "Doctor Dashboard";
                    break;
                case "patient":
                    fxmlFile = "/patient_dashboard.fxml";
                    title = "Patient Dashboard";
                    break;
                default:
                    showAlert("Erreur", "Rôle non reconnu: " + currentUser.getRole());
                    return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            Object controller = loader.getController();

            if (controller instanceof AdminDashboardController && "admin".equals(currentUser.getRole())) {
                admin adminUser = serviceAdmin.getById(currentUser.getId());
                if (adminUser != null) {
                    ((AdminDashboardController) controller).setAdminData(adminUser);
                }
            } else if (controller instanceof DoctorDashboardController && "doctor".equals(currentUser.getRole())) {
                doctor doctorUser = serviceDoctor.getById(currentUser.getId());
                if (doctorUser != null) {
                    ((DoctorDashboardController) controller).setDoctorData(doctorUser);
                }
            } else if (controller instanceof PatientDashboardController && "patient".equals(currentUser.getRole())) {
                patient patientUser = servicePatient.getById(currentUser.getId());
                if (patientUser != null) {
                    ((PatientDashboardController) controller).setPatientData(patientUser);
                } else {
                    ((PatientDashboardController) controller).setUser(currentUser);
                }
            }

            // Remplacer la scène actuelle au lieu de créer une nouvelle fenêtre
            Stage stage = (Stage) goToDashboardButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();

            System.out.println("✅ Redirection réussie vers " + title);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Fichier FXML non trouvé: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'accéder au dashboard: " + e.getMessage());
        }
    }

    // ========== GESTION DU PROFIL ==========
    @FXML
    private void goToProfile() {
        System.out.println("\n=== Accès au profil utilisateur ===");

        if (currentUser == null || "guest".equals(currentUser.getRole())) {
            showAlert("Accès refusé", "Veuillez vous connecter pour accéder à votre profil.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile.fxml"));
            Parent root = loader.load();

            ProfileController controller = loader.getController();
            controller.setUser(currentUser);

            Stage stage = new Stage();
            stage.setTitle("Mon Profil - " + currentUser.getName());
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le profil: " + e.getMessage());
        }
    }

    // ========== GESTION DES MODULES ==========
    @FXML
    private void goToCabinet() {
        if (currentUser == null) {
            showAlert("Non connecté", "Veuillez vous connecter pour accéder au module Cabinet.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MenuPrincipal.fxml"));
            Parent root = loader.load();

            Object controller = loader.getController();
            try {
                controller.getClass().getMethod("setCurrentUser", users.class).invoke(controller, currentUser);
            } catch (Exception e) {
                System.out.println("ℹ️ Le contrôleur n'a pas de méthode setCurrentUser");
            }

            // Remplacer la scène actuelle au lieu de créer une nouvelle fenêtre
            Stage stage = (Stage) cabinetCard.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion Cabinet - GrowMind");
            stage.show();

            System.out.println("✅ Module Cabinet ouvert dans la même fenêtre");

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le module Cabinet.\n" + e.getMessage());
        }
    }

    @FXML
    private void goToForum() {
        if (currentUser == null) {
            showAlert("Non connecté", "Veuillez vous connecter pour accéder au Forum.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main_forum.fxml"));
            Parent root = loader.load();

            MainForumController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            // Remplacer la scène actuelle
            Stage stage = (Stage) forumCard.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Forum - GrowMind");
            stage.show();

            System.out.println("✅ Module Forum ouvert dans la même fenêtre");

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le module Forum.\n" + e.getMessage());
        }
    }

    @FXML
    private void goToEvent() {
        if (currentUser == null) {
            showAlert("Non connecté", "Veuillez vous connecter pour accéder aux Événements.");
            return;
        }

        System.out.println("\n=== Navigation vers Gestion des Événements ===");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainView.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur - remplacez "MainViewController" par le nom réel
            Object controller = loader.getController();

            // Si vous connaissez le type exact, faites un cast direct
            EvenementFXController eventController = (EvenementFXController) controller;
             eventController.setCurrentUser(currentUser);

            // Méthode générique (fonctionne quel que soit le type)
            if (controller != null && currentUser != null) {
                try {
                    controller.getClass().getMethod("setCurrentUser", users.class).invoke(controller, currentUser);
                } catch (Exception e) {
                    System.out.println("⚠️ Impossible de passer l'utilisateur: " + e.getMessage());
                }
            }

            Stage stage = (Stage) eventCard.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des Événements - GrowMind");
            stage.show();

            System.out.println("✅ Navigation réussie");

        } catch (IOException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le module Événements.\n" + e.getMessage());
        }
    }

    @FXML
    private void goToSante() {
        if (currentUser == null) {
            showAlert("Non connecté", "Veuillez vous connecter pour accéder à Santé & Bien-être.");
            return;
        }

        System.out.println("\n=== Navigation vers Gestion Santé & Bien-être ===");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionSanteBienEtre.fxml"));
            Parent root = loader.load();

            // Remplacer "GestionSanteBienEtreController" par le nom réel de votre contrôleur
            Object controller = loader.getController();

            // Si vous connaissez le type exact du contrôleur, faites un cast direct
            GestionSanteBienEtreController santeController = (GestionSanteBienEtreController) controller;
             santeController.setCurrentUser(currentUser);

            // Méthode générique par réflexion (plus sûre)
            if (controller != null && currentUser != null) {
                try {
                    controller.getClass().getMethod("setCurrentUser", users.class).invoke(controller, currentUser);
                } catch (NoSuchMethodException e) {
                    try {
                        controller.getClass().getMethod("setUser", users.class).invoke(controller, currentUser);
                    } catch (Exception ex) {
                        // Ignorer si pas de méthode
                    }
                } catch (Exception e) {
                    // Ignorer
                }
            }

            Stage stage = (Stage) santeCard.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion Santé & Bien-être - GrowMind");
            stage.show();

            System.out.println("✅ Navigation réussie");

        } catch (IOException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le module Santé & Bien-être.\n" + e.getMessage());
        }
    }

    @FXML
    private void goToResponsabilite() {
        if (currentUser == null) {
            showAlert("Non connecté", "Veuillez vous connecter pour accéder à Responsabilité.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/userressource.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur et le caster directement
            UserRessourceController controller = loader.getController();
            controller.setCurrentUser(currentUser); // Appel direct sans réflexion

            // Remplacer la scène actuelle
            Stage stage = (Stage) responsabiliteCard.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion Ressources - GrowMind");
            stage.show();

            System.out.println("✅ Module Ressources ouvert - Utilisateur: " + currentUser.getName());

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le module Ressources.\n" + e.getMessage());
        }
    }

    private void showModuleInfo(String moduleName, String moduleId) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Module " + moduleName);
        alert.setHeaderText(null);
        alert.setContentText("Le module \"" + moduleName + "\" sera bientôt disponible !\n\n" +
                "Cette fonctionnalité est en cours de développement.");
        alert.showAndWait();
    }

    // ========== DÉCONNEXION ==========
    @FXML
    private void handleLogout(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Déconnexion");
        confirm.setHeaderText("Confirmation de déconnexion");
        confirm.setContentText("Êtes-vous sûr de vouloir vous déconnecter ?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                // Vider la session
                SessionManager.getInstance().logout();

                // Retour à la page de login dans la MÊME fenêtre
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/log_in.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("GrowMind - Connexion");

                // Garder la même taille/fenêtre
                stage.show();

                System.out.println("✅ Déconnexion réussie - Retour à la page de login");

            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible de charger la page de connexion: " + e.getMessage());
            }
        }
    }

    // ========== UTILITAIRES ==========
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}