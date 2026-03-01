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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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

        // Initialiser les cartes
        setupCards();

        System.out.println("=== Initialisation terminée ===\n");
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

        setupCard(responsabiliteCard, "⚖️ Responsabilité & biblioteque",
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
    }

    // ========== GESTION DE L'UTILISATEUR ==========
    public void setUser(users user) {
        System.out.println("\n=== HomeController.setUser ===");

        if (user == null) {
            System.err.println("❌ Utilisateur reçu est null!");
            return;
        }

        this.currentUser = user;
        System.out.println("✅ Utilisateur reçu: " + user.getEmail());
        System.out.println("   Rôle: " + user.getRole());
        System.out.println("   Nom: " + user.getName() + " " + user.getSecond_name());

        // Mise à jour des labels avec vérification de nullité
        try {
            if (userNameLabel != null) {
                String fullName = user.getName() + " " + user.getSecond_name();
                userNameLabel.setText(fullName);
                System.out.println("✅ userNameLabel mis à jour");
            } else {
                System.err.println("❌ userNameLabel est null!");
            }

            if (welcomeLabel != null) {
                welcomeLabel.setText("Bienvenue, " + user.getName() + " !");
                System.out.println("✅ welcomeLabel mis à jour");
            } else {
                System.err.println("❌ welcomeLabel est null!");
            }

            if (userRoleLabel != null) {
                String roleText = "";
                String roleColor = "";

                switch(user.getRole()) {
                    case "admin":
                        roleText = "👑 Administrateur";
                        roleColor = "#9B59B6";
                        break;
                    case "doctor":
                        roleText = "👨‍⚕️ Docteur";
                        roleColor = "#E667AF";
                        break;
                    case "patient":
                        roleText = "🩺 Patient";
                        roleColor = "#5FB49C";
                        break;
                    default:
                        roleText = "👤 Utilisateur";
                        roleColor = "#666";
                }

                userRoleLabel.setText(roleText);
                userRoleLabel.setStyle("-fx-text-fill:black " + roleColor + "; -fx-font-weight: bold;");
            } else {
                System.err.println("❌ userRoleLabel est null!");
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la mise à jour des labels: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("=== Fin setUser ===\n");
    }

    // ========== REDIRECTION VERS LE DASHBOARD ==========
    @FXML
    private void goToDashboard() {
        System.out.println("\n=== Redirection vers dashboard ===");

        if (currentUser == null) {
            showAlert("Erreur", "Aucun utilisateur connecté");
            return;
        }

        try {
            String fxmlFile;
            String title;

            switch(currentUser.getRole()) {
                case "admin":
                    fxmlFile = "/admin_dashboard.fxml";
                    title = "Admin Dashboard";
                    System.out.println("🔍 Redirection admin");
                    break;
                case "doctor":
                    fxmlFile = "/doctor_dashboard.fxml";
                    title = "Doctor Dashboard";
                    System.out.println("🔍 Redirection docteur");
                    break;
                case "patient":
                    fxmlFile = "/patient_dashboard.fxml";
                    title = "Patient Dashboard";
                    System.out.println("🔍 Redirection patient");
                    break;
                default:
                    showAlert("Erreur", "Rôle non reconnu: " + currentUser.getRole());
                    return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            // Passer l'utilisateur au contrôleur approprié
            Object controller = loader.getController();

            if (controller instanceof AdminDashboardController && "admin".equals(currentUser.getRole())) {
                admin adminUser = serviceAdmin.getById(currentUser.getId());
                if (adminUser != null) {
                    ((AdminDashboardController) controller).setAdminData(adminUser);
                    System.out.println("✅ Admin chargé: " + adminUser.getName());
                } else {
                    System.err.println("❌ Admin non trouvé pour ID: " + currentUser.getId());
                }

            } else if (controller instanceof DoctorDashboardController && "doctor".equals(currentUser.getRole())) {
                doctor doctorUser = serviceDoctor.getById(currentUser.getId());
                if (doctorUser != null) {
                    ((DoctorDashboardController) controller).setDoctorData(doctorUser);
                    System.out.println("✅ Docteur chargé: " + doctorUser.getName());
                } else {
                    System.err.println("❌ Docteur non trouvé pour ID: " + currentUser.getId());
                }

            } else if (controller instanceof PatientDashboardController && "patient".equals(currentUser.getRole())) {
                patient patientUser = servicePatient.getById(currentUser.getId());
                if (patientUser != null) {
                    ((PatientDashboardController) controller).setPatientData(patientUser);
                    System.out.println("✅ Patient chargé: " + patientUser.getName());
                } else {
                    System.out.println("⚠️ Patient non trouvé, utilisation de l'utilisateur générique");
                    ((PatientDashboardController) controller).setUser(currentUser);
                }
            }

            Stage stage = (Stage) goToDashboardButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();

            System.out.println("✅ Redirection réussie vers " + title);

        } catch (IOException e) {
            System.err.println("❌ Erreur IO: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Fichier FXML non trouvé: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Erreur inattendue: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'accéder au dashboard: " + e.getMessage());
        }
    }
    // ========== GESTION DU PROFIL ==========
    @FXML
    private void goToProfile() {
        System.out.println("\n=== Accès au profil utilisateur ===");

        if (currentUser == null) {
            showAlert("Erreur", "Aucun utilisateur connecté");
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

            System.out.println("✅ Fenêtre de profil ouverte");

        } catch (IOException e) {
            System.err.println("❌ Erreur IO: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le profil: " + e.getMessage());
        }
    }
    // ========== GESTION DES CARTES ==========
    @FXML
    private void goToCabinet() {
        showModuleInfo("Psychologue & Cabinet", "cabinet");
    }

    @FXML
    private void goToForum() {
        System.out.println("\n=== Accès au module Forum ===");

        try {
            // Charger le fichier FXML du module Forum
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main_forum.fxml"));
            Parent root = loader.load();

            // Créer une nouvelle fenêtre pour le module
            Stage forumStage = new Stage();
            forumStage.setTitle("Gestion Forum - GrowMind");
            forumStage.setScene(new Scene(root));

            // Optionnel : passer l'utilisateur connecté au contrôleur du forum
            // Object controller = loader.getController();
            // if (controller instanceof ForumController) {
            //     ((ForumController) controller).setUser(currentUser);
            // }

            forumStage.show();

            System.out.println("✅ Module Forum ouvert avec succès");

        } catch (IOException e) {
            System.err.println("❌ Erreur chargement module Forum: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le module Forum.\n" + e.getMessage());
        }
    }
    @FXML
    private void goToEvent() {
        showModuleInfo("Événement", "event");
    }

    @FXML
    private void goToSante() {
        showModuleInfo("Santé & Bien-être", "sante");
    }

    @FXML
    private void goToResponsabilite() {


        try {
            // Charger le fichier FXML du module Forum
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/userressource.fxml"));
            Parent root = loader.load();

            // Créer une nouvelle fenêtre pour le module
            Stage forumStage = new Stage();
            forumStage.setTitle("Gestion Forum - GrowMind");
            forumStage.setScene(new Scene(root));

            // Optionnel : passer l'utilisateur connecté au contrôleur du forum
            // Object controller = loader.getController();
            // if (controller instanceof ForumController) {
            //     ((ForumController) controller).setUser(currentUser);
            // }

            forumStage.show();

            System.out.println("✅ Module Forum ouvert avec succès");

        } catch (IOException e) {
            System.err.println("❌ Erreur chargement module Forum: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le module Forum.\n" + e.getMessage());
        }
    }

    private void showModuleInfo(String moduleName, String moduleId) {
        System.out.println("🔍 Accès au module: " + moduleName);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Module " + moduleName);
        alert.setHeaderText(null);
        alert.setContentText("Le module \"" + moduleName + "\" sera bientôt disponible !\n\n" +
                "Cette fonctionnalité est en cours de développement.\n" +
                "Revenez plus tard pour découvrir cette nouvelle fonctionnalité.");
        alert.showAndWait();
    }

    // ========== GESTION DU PROFIL ==========

    // ========== DÉCONNEXION ==========
    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/log_in.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login - GrowMind");
            stage.show();
            System.out.println("✅ Logout successful");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page de connexion: " + e.getMessage());
        }
    }

    // ========== UTILITAIRES ==========
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}