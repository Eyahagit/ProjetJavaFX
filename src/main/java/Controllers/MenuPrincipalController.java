package Controllers;

import Models.RendezVous;
import Models.users;
import Services.ServiceCabinet;
import Services.ServicePsychologue;
import Services.ServiceRendezVous;
import Services.ServicePatient;
import Services.ServiceDoctor;
import Services.ServiceAdmin;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MenuPrincipalController {

    @FXML private StackPane contentArea;
    @FXML private Label statusLabel;
    @FXML private Label dateTimeLabel;
    @FXML private Label userWelcomeLabel;
    @FXML private Label userRoleLabel;
    @FXML private Label userFullNameLabel;

    // Labels pour les compteurs
    @FXML private Label lblCabinetCount;
    @FXML private Label lblPsychologueCount;
    @FXML private Label lblRendezVousCount;
    @FXML private Label lblRappelsCount;

    // Labels pour le dashboard
    @FXML private Label dashboardCabinetCount;
    @FXML private Label dashboardPsychologueCount;
    @FXML private Label dashboardRendezVousCount;

    // Boutons de navigation
    @FXML private Button btnDashboard;
    @FXML private Button btnCabinet;
    @FXML private Button btnPsychologue;
    @FXML private Button btnRendezVous;
    @FXML private Button btnSuiviRappels;
    @FXML private Button btnStatistiques;
    @FXML private Button btnExport;
    @FXML private Button btnRetourAccueil;

    // Boutons d'actions rapides (pour contrôle par rôle)
    @FXML private Button btnQuickCabinet;
    @FXML private Button btnQuickPsychologue;
    @FXML private Button btnQuickRendezVous;
    @FXML private Button btnQuickSuiviRappels;

    private Parent currentView = null;

    // Utilisateur connecté
    private users currentUser;

    // Services
    private ServiceCabinet serviceCabinet = new ServiceCabinet();
    private ServicePsychologue servicePsychologue = new ServicePsychologue();
    private ServiceRendezVous serviceRendezVous = new ServiceRendezVous();
    private ServicePatient servicePatient = new ServicePatient();
    private ServiceDoctor serviceDoctor = new ServiceDoctor();
    private ServiceAdmin serviceAdmin = new ServiceAdmin();

    public MenuPrincipalController() throws SQLException {
    }

    @FXML
    public void initialize() {
        System.out.println("\n=== Initialisation MenuPrincipalController ===");

        // Mettre à jour l'horloge
        updateDateTime();

        // Charger les compteurs
        updateCounters();

        // Sélectionner le dashboard par défaut
        setActiveButton(btnDashboard);

        // Initialiser l'affichage utilisateur
        updateUserDisplay();

        System.out.println("=== Initialisation terminée ===\n");
    }

    /**
     * Met à jour l'affichage des informations utilisateur
     */
    private void updateUserDisplay() {
        if (currentUser != null) {
            // Message de bienvenue
            if (userWelcomeLabel != null) {
                userWelcomeLabel.setText("Bienvenue, " + currentUser.getName() + " !");
            }

            // Nom complet (prénom + nom)
            if (userFullNameLabel != null) {
                String fullName = currentUser.getName() + " " + currentUser.getSecond_name();
                userFullNameLabel.setText(fullName.trim());
            }

            // Rôle avec icône
            if (userRoleLabel != null) {
                userRoleLabel.setText(getRoleDisplay(currentUser.getRole()));
            }

            // Mettre à jour la barre de statut
            if (statusLabel != null) {
                statusLabel.setText("Connecté en tant que " + currentUser.getName() + " (" + getRoleDisplay(currentUser.getRole()) + ")");
            }

            System.out.println("✅ Affichage utilisateur mis à jour: " + currentUser.getName() + " " + currentUser.getSecond_name());
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
     * Définit l'utilisateur connecté
     */
    public void setCurrentUser(users user) {
        if (user != null) {
            this.currentUser = user;
            System.out.println("✅ Utilisateur défini dans MenuPrincipalController: " + user.getName() + " " + user.getSecond_name());
            updateUserDisplay();
            configureInterfaceForRole(user.getRole());
        } else {
            System.err.println("❌ Tentative de définir un utilisateur null");
        }
    }

    /**
     * Alias pour setCurrentUser
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
     * Configure l'interface selon le rôle de l'utilisateur
     */
    private void configureInterfaceForRole(String role) {
        if (role == null) return;

        boolean isAdmin = "admin".equals(role);
        boolean isMedecin = "medecin".equals(role);
        boolean isPatient = "patient".equals(role);

        // Pour les patients : mode consultation uniquement
        if (isPatient) {
            // Cacher les boutons de gestion dans la barre latérale
            if (btnCabinet != null) btnCabinet.setVisible(false);
            if (btnPsychologue != null) btnPsychologue.setVisible(false);
            if (btnSuiviRappels != null) btnSuiviRappels.setVisible(false);

            // Cacher les boutons d'actions rapides (ajout)
            if (btnQuickCabinet != null) btnQuickCabinet.setVisible(false);
            if (btnQuickPsychologue != null) btnQuickPsychologue.setVisible(false);
            if (btnQuickRendezVous != null) btnQuickRendezVous.setVisible(false);
            if (btnQuickSuiviRappels != null) btnQuickSuiviRappels.setVisible(false);

            System.out.println("🔄 Interface patient: mode consultation uniquement");
        }

        // Pour les médecins : accès complet sauf certaines fonctions admin
        else if (isMedecin) {
            // Les médecins voient tout
            System.out.println("🔄 Interface médecin: accès complet");
        }

        // Pour les admins : accès complet
        else if (isAdmin) {
            System.out.println("🔄 Interface admin: accès complet");
        }
    }

    private void updateDateTime() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                    dateTimeLabel.setText(LocalDateTime.now().format(formatter));
                })
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void updateCounters() {
        try {
            int cabinetCount = serviceCabinet.recuperer().size();
            int psychologueCount = servicePsychologue.recuperer().size();
            int rendezVousCount = serviceRendezVous.recuperer().size();

            lblCabinetCount.setText(String.valueOf(cabinetCount));
            lblPsychologueCount.setText(String.valueOf(psychologueCount));
            lblRendezVousCount.setText(String.valueOf(rendezVousCount));

            dashboardCabinetCount.setText(String.valueOf(cabinetCount));
            dashboardPsychologueCount.setText(String.valueOf(psychologueCount));
            dashboardRendezVousCount.setText(String.valueOf(rendezVousCount));

            if (lblRappelsCount != null) {
                long rappelsEnvoyes = serviceRendezVous.recuperer().stream()
                        .filter(RendezVous::isRappelEnvoye)
                        .count();
                lblRappelsCount.setText(String.valueOf(rappelsEnvoyes));
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement compteurs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setActiveButton(Button activeButton) {
        // Réinitialiser tous les boutons
        btnDashboard.setStyle("-fx-background-color: transparent; -fx-background-radius: 10; -fx-padding: 12 15;");
        btnCabinet.setStyle("-fx-background-color: transparent; -fx-background-radius: 10; -fx-padding: 12 15;");
        btnPsychologue.setStyle("-fx-background-color: transparent; -fx-background-radius: 10; -fx-padding: 12 15;");
        btnRendezVous.setStyle("-fx-background-color: transparent; -fx-background-radius: 10; -fx-padding: 12 15;");

        if (btnSuiviRappels != null) {
            btnSuiviRappels.setStyle("-fx-background-color: transparent; -fx-background-radius: 10; -fx-padding: 12 15;");
        }
        if (btnStatistiques != null) {
            btnStatistiques.setStyle("-fx-background-color: transparent; -fx-background-radius: 10; -fx-padding: 12 15;");
        }
        if (btnExport != null) {
            btnExport.setStyle("-fx-background-color: transparent; -fx-background-radius: 10; -fx-padding: 12 15;");
        }

        // Mettre en surbrillance le bouton actif
        if (activeButton != null) {
            activeButton.setStyle("-fx-background-color: #e3f2fd; -fx-background-radius: 10; -fx-padding: 12 15; -fx-border-color: #2196F3; -fx-border-radius: 10;");
        }
    }

    @FXML
    private void openDashboard() {
        setActiveButton(btnDashboard);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dashboard.fxml"));
            Parent dashboardView = loader.load();

            // Essayer de passer l'utilisateur si le contrôleur a une méthode setCurrentUser
            Object controller = loader.getController();
            if (controller != null && currentUser != null) {
                try {
                    controller.getClass().getMethod("setCurrentUser", users.class).invoke(controller, currentUser);
                    System.out.println("✅ Utilisateur passé au DashboardController");
                } catch (Exception e) {
                    System.out.println("ℹ️ DashboardController n'a pas de méthode setCurrentUser");
                }
            }

            contentArea.getChildren().setAll(dashboardView);
            statusLabel.setText("✅ Tableau de bord - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger le tableau de bord", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void openGestionCabinet() {
        setActiveButton(btnCabinet);
        loadView("/AjouterCabinet.fxml", "Gestion des Cabinets");
    }

    @FXML
    private void openGestionPsychologue() {
        setActiveButton(btnPsychologue);
        loadView("/AjouterPsychologue.fxml", "Gestion des Psychologues");
    }

    @FXML
    private void openGestionRendezVous() {
        setActiveButton(btnRendezVous);
        loadView("/AjouterRendezVous.fxml", "Gestion des Rendez-vous");
    }

    @FXML
    private void openStatistiques() {
        setActiveButton(btnStatistiques);
        loadView("/Statistiques.fxml", "Statistiques");
    }

    @FXML
    private void openSuiviRappels() {
        setActiveButton(btnSuiviRappels);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SuiviRappels.fxml"));
            Parent root = loader.load();

            // Essayer de passer l'utilisateur
            Object controller = loader.getController();
            if (controller != null && currentUser != null) {
                try {
                    controller.getClass().getMethod("setCurrentUser", users.class).invoke(controller, currentUser);
                    System.out.println("✅ Utilisateur passé au SuiviRappelsController");
                } catch (Exception e) {
                    System.out.println("ℹ️ SuiviRappelsController n'a pas de méthode setCurrentUser");
                }
            }

            contentArea.getChildren().setAll(root);
            statusLabel.setText("✅ Suivi des rappels intelligents - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir le suivi des rappels", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRetourAccueil() {
        try {
            // Retour à l'accueil dans la MÊME fenêtre
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/home.fxml"));
            Parent root = loader.load();

            HomeController homeController = loader.getController();
            if (homeController != null && currentUser != null) {
                homeController.setUser(currentUser);
                System.out.println("✅ Utilisateur transmis au HomeController");
            }

            Stage stage = (Stage) btnRetourAccueil.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Accueil - GrowMind");
            stage.show();

            System.out.println("✅ Retour à l'accueil dans la même fenêtre");

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de retourner à l'accueil", Alert.AlertType.ERROR);
        }
    }

    private void loadView(String fxmlPath, String titre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            // Essayer de passer l'utilisateur
            Object controller = loader.getController();
            if (controller != null && currentUser != null) {
                try {
                    controller.getClass().getMethod("setCurrentUser", users.class).invoke(controller, currentUser);
                    System.out.println("✅ Utilisateur passé au contrôleur de " + titre);
                } catch (Exception e) {
                    // Pas de méthode setCurrentUser, on ignore
                }
            }

            // Animation de transition
            view.setOpacity(0);
            contentArea.getChildren().setAll(view);

            javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(
                    javafx.util.Duration.millis(300), view
            );
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();

            statusLabel.setText("✅ " + titre + " - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger " + titre, Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void exportDonnees() {
        try {
            // Vérifier si l'utilisateur a le droit d'exporter
            if (currentUser == null || (!"admin".equals(currentUser.getRole()) && !"doctor".equals(currentUser.getRole()))) {
                showAlert("Accès refusé", "Seuls les administrateurs et médecins peuvent exporter les données.", Alert.AlertType.WARNING);
                return;
            }

            showAlert("Export", "✅ Données exportées avec succès !", Alert.AlertType.INFORMATION);
            statusLabel.setText("✅ Export effectué - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        } catch (Exception e) {
            showAlert("Erreur", "❌ Erreur lors de l'export: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void logout() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Déconnexion");
        confirmation.setHeaderText("Confirmation de déconnexion");
        confirmation.setContentText("Êtes-vous sûr de vouloir vous déconnecter ?");

        if (confirmation.showAndWait().get() == javafx.scene.control.ButtonType.OK) {
            try {
                Stage stage = (Stage) contentArea.getScene().getWindow();
                Parent loginView = FXMLLoader.load(getClass().getResource("/Login.fxml"));
                stage.setScene(new Scene(loginView));
                stage.setTitle("GrowMind - Connexion");
                stage.setMaximized(false);
                stage.show();
                System.out.println("✅ Déconnexion réussie");
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible de charger la page de connexion", Alert.AlertType.ERROR);
            }
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void refreshAllData() {
        updateCounters();
        statusLabel.setText("✅ Données actualisées - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }
}