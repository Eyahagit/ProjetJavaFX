package Controllers;

import Models.RendezVous;  // ← NOUVEL IMPORT
import Services.ServiceCabinet;
import Services.ServicePsychologue;
import Services.ServiceRendezVous;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MenuPrincipalController {

    @FXML private StackPane contentArea;
    @FXML private Label statusLabel;
    @FXML private Label dateTimeLabel;

    @FXML private Label lblCabinetCount;
    @FXML private Label lblPsychologueCount;
    @FXML private Label lblRendezVousCount;

    // ========== NOUVEAU COMPTEUR POUR LES RAPPELS ==========
    @FXML private Label lblRappelsCount;

    @FXML private Label dashboardCabinetCount;
    @FXML private Label dashboardPsychologueCount;
    @FXML private Label dashboardRendezVousCount;

    @FXML private Button btnDashboard;
    @FXML private Button btnCabinet;
    @FXML private Button btnPsychologue;
    @FXML private Button btnRendezVous;
    @FXML private Button btnSuiviRappels;
    @FXML private Button btnRetourAccueil;// ← DÉJÀ PRÉSENT

    private Parent currentView = null;

    // Services pour récupérer les compteurs
    private ServiceCabinet serviceCabinet = new ServiceCabinet();
    private ServicePsychologue servicePsychologue = new ServicePsychologue();
    private ServiceRendezVous serviceRendezVous = new ServiceRendezVous();

    @FXML
    public void initialize() {
        // Mettre à jour l'horloge
        updateDateTime();

        // Charger les compteurs
        updateCounters();

        // Sélectionner le dashboard par défaut
        setActiveButton(btnDashboard);
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

    // ========== METHODE MODIFIÉE POUR INCLURE LE COMPTEUR RAPPELS ==========
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

            // ========== NOUVEAU : Mettre à jour le compteur de rappels ==========
            if (lblRappelsCount != null) {
                // Compter les rendez-vous qui ont reçu un rappel
                long rappelsEnvoyes = serviceRendezVous.recuperer().stream()
                        .filter(RendezVous::isRappelEnvoye)
                        .count();
                lblRappelsCount.setText(String.valueOf(rappelsEnvoyes));
            }

        } catch (Exception e) {
            System.err.println("Erreur chargement compteurs: " + e.getMessage());
        }
    }

    // ========== METHODE MODIFIÉE POUR INCLURE LE BOUTON SUIVI RAPPELS ==========
    private void setActiveButton(Button activeButton) {
        // Réinitialiser tous les boutons
        btnDashboard.setStyle("-fx-background-color: transparent; -fx-background-radius: 10; -fx-padding: 12 15;");
        btnCabinet.setStyle("-fx-background-color: transparent; -fx-background-radius: 10; -fx-padding: 12 15;");
        btnPsychologue.setStyle("-fx-background-color: transparent; -fx-background-radius: 10; -fx-padding: 12 15;");
        btnRendezVous.setStyle("-fx-background-color: transparent; -fx-background-radius: 10; -fx-padding: 12 15;");

        // ========== NOUVEAU : Réinitialiser aussi le bouton suivi rappels ==========
        if (btnSuiviRappels != null) {
            btnSuiviRappels.setStyle("-fx-background-color: transparent; -fx-background-radius: 10; -fx-padding: 12 15;");
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
            // Afficher le dashboard (contenu par défaut)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dashboard.fxml"));
            Parent dashboardView = loader.load();
            contentArea.getChildren().setAll(dashboardView);
            statusLabel.setText("✅ Tableau de bord - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger le tableau de bord", Alert.AlertType.ERROR);
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
        loadView("/Statistiques.fxml", "Statistiques");
    }

    // ========== MÉTHODE POUR OUVRIR LE SUIVI DES RAPPELS (DÉJÀ PRÉSENTE) ==========
    @FXML
    private void openSuiviRappels() {
        setActiveButton(btnSuiviRappels);  // ← AJOUTÉ pour la surbrillance
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/SuiviRappels.fxml"));
            contentArea.getChildren().setAll(root);
            statusLabel.setText("✅ Suivi des rappels intelligents");
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir le suivi des rappels", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRetourAccueil() {
        try {
            // Récupérer la fenêtre actuelle
            Stage currentStage = (Stage) btnRetourAccueil.getScene().getWindow();

            // Charger le FXML de l'accueil
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/home.fxml"));
            Parent root = loader.load();

            // Créer la nouvelle scène
            Scene homeScene = new Scene(root);

            // Remplacer la scène actuelle
            currentStage.setScene(homeScene);
            currentStage.setTitle("Accueil - GrowMind");

            // Optionnel : garder la taille actuelle au lieu de maximiser
            // currentStage.sizeToScene(); // Ajuste à la taille de la nouvelle scène

            // PAS DE currentStage.show() ! La fenêtre reste visible

            System.out.println("✅ Retour à l'accueil réussi");

        } catch (IOException e) {
            System.err.println("❌ Erreur retour accueil: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible de retourner à l'accueil: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    private void loadView(String fxmlPath, String titre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            // Animation de transition
            view.setOpacity(0);
            contentArea.getChildren().setAll(view);

            // Animation fade in
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
            // Simulation d'export
            showAlert("Export", "✅ Données exportées avec succès !", Alert.AlertType.INFORMATION);
            statusLabel.setText("✅ Export effectué - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        } catch (Exception e) {
            showAlert("Erreur", "❌ Erreur lors de l'export", Alert.AlertType.ERROR);
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
                // Retour à l'écran de login
                Stage stage = (Stage) contentArea.getScene().getWindow();
                Parent loginView = FXMLLoader.load(getClass().getResource("/Login.fxml"));
                stage.setScene(new Scene(loginView));
                stage.setTitle("GrowMind - Connexion");
            } catch (IOException e) {
                e.printStackTrace();
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