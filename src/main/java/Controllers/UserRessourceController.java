package Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import Models.Favori;
import Models.Ressource;
import Models.Evaluation;
import Models.users;
import Services.ressourceService;
import Services.favoriService;
import Services.evaluationService;
import utils.StaticUser;
import utils.StarRatingHelper;
import utils.SessionManager;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserRessourceController {

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> filterType;
    @FXML
    private ComboBox<String> filterCategory;
    @FXML
    private FlowPane cardsContainer;

    // Éléments FXML pour l'interface
    @FXML
    private Button btnHome;
    @FXML
    private Button btnGoToRespo;
    @FXML
    private Label lblGestionTitle;
    @FXML
    private Label lblStats;
    @FXML
    private Label lblUserWelcome;
    @FXML
    private Label lblUserRole;
    @FXML
    private Label lblTotalCount;
    @FXML
    private Label lblDate;
    @FXML
    private Label lblUserName;

    private final ressourceService ressourceService = new ressourceService();
    private final favoriService favoriService = new favoriService();
    private final evaluationService evaluationService = new evaluationService();

    private ObservableList<Ressource> allRessources = FXCollections.observableArrayList();

    // Utilisateur connecté
    private users currentUser;

    @FXML
    public void initialize() {
        System.out.println("\n=== Initialisation UserRessourceController ===");

        // Afficher la date du jour
        if (lblDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            lblDate.setText(LocalDate.now().format(formatter));
        }

        // Récupérer l'utilisateur depuis la session
        currentUser = SessionManager.getInstance().getCurrentUser();

        // Configurer l'affichage utilisateur
        configureUserDisplay();

        // Initialiser les filtres
        setupFilters();

        // Définir le titre de la gestion
        if (lblGestionTitle != null) {
            lblGestionTitle.setText("⚖️ Responsabilité & Bibliothèque");
        }

        refreshList();

        System.out.println("=== Initialisation terminée ===\n");
    }

    /**
     * Configure les filtres de recherche
     */
    private void setupFilters() {
        // Types de ressources
        filterType.getItems().addAll("Tous", "formation", "article", "video", "image", "evenement", "livre", "podcast");
        filterType.setValue("Tous");
        filterType.setOnAction(e -> applyFilters());

        // Catégories
        filterCategory.getItems().addAll("Tous", "santé", "bien-etre", "developpement personnel", "motivation", "psychologie", "méditation");
        filterCategory.setValue("Tous");
        filterCategory.setOnAction(e -> applyFilters());

        // Recherche en temps réel
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }

    /**
     * Configure l'affichage des informations utilisateur
     */
    private void configureUserDisplay() {
        if (currentUser != null) {
            // Message de bienvenue complet
            if (lblUserWelcome != null) {
                lblUserWelcome.setText("Bienvenue, " + currentUser.getName() + " " + currentUser.getSecond_name() + " !");
            }

            // Nom d'utilisateur
            if (lblUserName != null) {
                lblUserName.setText(currentUser.getName() + " " + currentUser.getSecond_name());
            }

            // Rôle avec style
            if (lblUserRole != null) {
                String roleText = getRoleDisplay(currentUser.getRole());
                lblUserRole.setText(roleText);

                // Ajouter une couleur selon le rôle
                String color = getRoleColor(currentUser.getRole());
                lblUserRole.setStyle("-fx-background-color: " + color + "; -fx-padding: 5 15; -fx-background-radius: 20; -fx-text-fill: white; -fx-font-weight: bold;");
            }

            System.out.println("✅ Utilisateur connecté: " + currentUser.getName() + " " + currentUser.getSecond_name() + " (" + currentUser.getRole() + ")");
        } else {
            if (lblUserWelcome != null) {
                lblUserWelcome.setText("Bienvenue, Invité !");
            }
            if (lblUserName != null) {
                lblUserName.setText("Invité");
            }
            if (lblUserRole != null) {
                lblUserRole.setText("👤 Invité");
                lblUserRole.setStyle("-fx-background-color: #95a5a6; -fx-padding: 5 15; -fx-background-radius: 20; -fx-text-fill: white; -fx-font-weight: bold;");
            }
        }
    }

    /**
     * Convertit le rôle en texte affichable
     */
    private String getRoleDisplay(String role) {
        if (role == null) return "👤 Invité";
        switch(role.toLowerCase()) {
            case "admin": return "👑 Administrateur";
            case "doctor": return "👨‍⚕️ Médecin";
            case "patient": return "👤 Patient";
            default: return "👤 Utilisateur";
        }
    }

    /**
     * Retourne la couleur associée au rôle
     */
    private String getRoleColor(String role) {
        if (role == null) return "#95a5a6";
        switch(role.toLowerCase()) {
            case "admin": return "#9b59b6";
            case "doctor": return "#3498db";
            case "patient": return "#2ecc71";
            default: return "#95a5a6";
        }
    }

    /**
     * Définit l'utilisateur connecté (appelé depuis d'autres contrôleurs)
     */
    public void setCurrentUser(users user) {
        if (user != null) {
            this.currentUser = user;
            SessionManager.getInstance().setCurrentUser(user);
            configureUserDisplay();
            System.out.println("✅ Utilisateur défini dans UserRessourceController: " + user.getName() + " " + user.getSecond_name());
        }
    }

    /**
     * Alias pour setCurrentUser
     */
    public void setUser(users user) {
        setCurrentUser(user);
    }

    /**
     * Gère le retour à l'accueil
     */
    @FXML
    private void handleHome() {
        System.out.println("\n=== Retour à l'accueil depuis Ressources ===");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/home.fxml"));
            Parent root = loader.load();

            // Passer l'utilisateur connecté au HomeController
            HomeController homeController = loader.getController();
            if (homeController != null && currentUser != null) {
                homeController.setUser(currentUser);
                System.out.println("✅ Utilisateur transmis au HomeController");
            }

            Stage stage = (Stage) btnHome.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Accueil - GrowMind");
            stage.show();

            System.out.println("✅ Retour à l'accueil réussi");

        } catch (IOException e) {
            System.err.println("❌ Erreur retour accueil: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible de retourner à l'accueil");
        }
    }

    public void refreshList() {
        try {
            allRessources.setAll(ressourceService.findAll());
            updateStats();
            applyFilters();
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du rafraîchissement: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    private void handleGoToRessource() {
        System.out.println("\n=== Navigation vers Responsabilité & Bibliothèque ===");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main_respo_bib.fxml"));
            Parent root = loader.load();

            // Passer l'utilisateur connecté
            Object controller = loader.getController();
            if (controller != null && currentUser != null) {
                try {
                    controller.getClass().getMethod("setCurrentUser", users.class).invoke(controller, currentUser);
                    System.out.println("✅ Utilisateur transmis");
                } catch (Exception e) {
                    System.out.println("⚠️ Impossible de transmettre l'utilisateur");
                }
            }

            Stage stage = (Stage) btnHome.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Responsabilité & Bibliothèque - GrowMind");
            stage.show();

            System.out.println("✅ Navigation réussie");

        } catch (IOException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page.\n" + e.getMessage());
        }
    }
    /**
     * Met à jour les statistiques
     */
    private void updateStats() {
        if (lblStats != null) {
            int count = allRessources.size();
            lblStats.setText(count + " ressource" + (count > 1 ? "s" : ""));
        }
        if (lblTotalCount != null) {
            lblTotalCount.setText(String.valueOf(allRessources.size()));
        }
    }

    private void applyFilters() {
        String type = filterType.getValue();
        String cat = filterCategory.getValue();
        String search = searchField != null && searchField.getText() != null ? searchField.getText().toLowerCase() : "";

        List<Ressource> filtered = allRessources.stream()
                .filter(r -> "Tous".equals(type) || type == null || type.isEmpty()
                        || type.equalsIgnoreCase(r.getType()))
                .filter(r -> "Tous".equals(cat) || cat == null || cat.isEmpty()
                        || cat.equalsIgnoreCase(r.getCategory()))
                .filter(r -> search.isEmpty() || r.getTitle().toLowerCase().contains(search)
                        || r.getDescription().toLowerCase().contains(search)
                        || r.getType().toLowerCase().contains(search)
                        || r.getCategory().toLowerCase().contains(search))
                .collect(Collectors.toList());

        displayCards(filtered);
    }

    @FXML
    private void handleSearch() {
        applyFilters();
    }

    /**
     * Charge une icône depuis les ressources
     */
    private ImageView loadIcon(String name, double size) {
        String[] paths;
        if ("star.png".equals(name)) {
            paths = new String[] { "/icons/rate.png", "/rate.png", "/icons/star.png", "/star.png" };
        } else if ("heart.png".equals(name) || "heart_empty.png".equals(name)) {
            paths = new String[] { "/icons/favourite.png", "/favourite.png", "/icons/" + name, "/" + name };
        } else if ("add.png".equals(name)) {
            paths = new String[] { "/icons/add.png", "/add.png", "/icons/plus.png", "/plus.png" };
        } else {
            paths = new String[] { "/icons/" + name, "/" + name };
        }

        for (String path : paths) {
            try {
                java.io.InputStream is = getClass().getResourceAsStream(path);
                if (is == null) continue;
                Image img = new Image(is);
                if (img.isError()) continue;
                ImageView iv = new ImageView(img);
                iv.setFitWidth(size);
                iv.setFitHeight(size);
                iv.setPreserveRatio(true);
                iv.setSmooth(true);
                return iv;
            } catch (Exception e) {
                // Essayer le chemin suivant
            }
        }
        return null;
    }

    private void displayCards(List<Ressource> resources) {
        cardsContainer.getChildren().clear();
        for (Ressource r : resources) {
            cardsContainer.getChildren().add(createCard(r));
        }
    }

    private Node createCard(Ressource r) {
        // Carte principale
        VBox card = new VBox(12);
        card.setStyle(
                "-fx-background-color: white; " +
                        "-fx-padding: 20; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3); " +
                        "-fx-background-radius: 15; " +
                        "-fx-pref-width: 280; " +
                        "-fx-min-height: 350; " +
                        "-fx-border-color: #e0e0e0; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 15;"
        );

        // Effet de survol
        card.setOnMouseEntered(e ->
                card.setStyle(card.getStyle() +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 5); " +
                        "-fx-scale-x: 1.02; -fx-scale-y: 1.02; " +
                        "-fx-transition: all 0.3s ease;"
                )
        );
        card.setOnMouseExited(e ->
                card.setStyle(
                        "-fx-background-color: white; " +
                                "-fx-padding: 20; " +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3); " +
                                "-fx-background-radius: 15; " +
                                "-fx-pref-width: 280; " +
                                "-fx-min-height: 350; " +
                                "-fx-border-color: #e0e0e0; " +
                                "-fx-border-width: 1; " +
                                "-fx-border-radius: 15;"
                )
        );

        // Titre
        Label title = new Label(r.getTitle());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        title.setWrapText(true);
        title.setMaxWidth(260);

        // Badges type et catégorie
        HBox typeBox = new HBox(10);
        typeBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label typeBadge = new Label(r.getType());
        typeBadge.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;");

        Label categoryBadge = new Label(r.getCategory());
        categoryBadge.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;");

        typeBox.getChildren().addAll(typeBadge, categoryBadge);

        // Note avec étoiles
        HBox ratingBox = new HBox(5);
        ratingBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label ratingStars = new Label();
        List<Evaluation> evals = evaluationService.findByRessourceId(r.getId());
        int evalCount = evals.size();

        if (!evals.isEmpty()) {
            double avg = evals.stream().mapToInt(Evaluation::getNote).average().orElse(0);
            ratingStars.setText(StarRatingHelper.toStarStringFromAverage(avg));
        } else {
            ratingStars.setText("☆☆☆☆☆");
        }
        ratingStars.setStyle("-fx-font-size: 16px; -fx-text-fill: #f39c12;");

        Label evalCountLabel = new Label("(" + evalCount + " avis)");
        evalCountLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");

        ratingBox.getChildren().addAll(ratingStars, evalCountLabel);

        // Description
        Label desc = new Label(r.getDescription());
        desc.setWrapText(true);
        desc.setStyle("-fx-text-fill: #34495e; -fx-font-size: 13px; -fx-line-spacing: 2;");
        desc.setMaxHeight(60);
        desc.setPrefHeight(60);

        // Séparateur
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 5 0;");

        // Boutons d'action
        HBox actions = new HBox(10);
        actions.setAlignment(javafx.geometry.Pos.CENTER);

        final String btnBaseStyle = "-fx-cursor: hand; -fx-background-radius: 25; -fx-padding: 8 12; " +
                "-fx-font-size: 12px; -fx-font-weight: bold; -fx-min-width: 80;";

        // Bouton Ajouter évaluation
        Button btnAddEval = new Button("➕ Évaluer");
        btnAddEval.setStyle(btnBaseStyle + "-fx-background-color: #5FB49C; -fx-text-fill: white;");
        btnAddEval.setOnAction(e -> openEvaluationModal(r));

        // Bouton Voir évaluations
        Button btnViewEval = new Button("⭐ Avis");
        btnViewEval.setStyle(btnBaseStyle + "-fx-background-color: #f39c12; -fx-text-fill: white;");
        btnViewEval.setOnAction(e -> openEvaluationList(r));

        // Bouton Favori
        Button btnFav = new Button();
        btnFav.setStyle(btnBaseStyle);
        updateFavButton(btnFav, r);
        btnFav.setOnAction(e -> toggleFavorite(r, btnFav));

        actions.getChildren().addAll(btnAddEval, btnViewEval, btnFav);

        // Assembler la carte
        card.getChildren().addAll(title, typeBox, ratingBox, desc, separator, actions);

        return card;
    }

    private void updateFavButton(Button btn, Ressource r) {
        final String baseStyle = "-fx-cursor: hand; -fx-background-radius: 25; -fx-padding: 8 12; " +
                "-fx-font-size: 12px; -fx-font-weight: bold; -fx-min-width: 80;";

        if (!StaticUser.isSet() || currentUser == null) {
            btn.setDisable(true);
            btn.setText("🔒");
            btn.setStyle(baseStyle + "-fx-background-color: #95a5a6; -fx-text-fill: white;");
            btn.setTooltip(new Tooltip("Connectez-vous pour ajouter aux favoris"));
            return;
        }

        boolean isFav = favoriService.findByUserIdAndRessourceId(StaticUser.getId(), r.getId()).isPresent();

        if (isFav) {
            btn.setText("❤️ Favori");
            btn.setStyle(baseStyle + "-fx-background-color: #c0392b; -fx-text-fill: white;");
            btn.setTooltip(new Tooltip("Retirer des favoris"));
        } else {
            btn.setText("🤍 Favori");
            btn.setStyle(baseStyle + "-fx-background-color: #e74c3c; -fx-text-fill: white;");
            btn.setTooltip(new Tooltip("Ajouter aux favoris"));
        }
    }

    private void toggleFavorite(Ressource r, Button btn) {
        if (!StaticUser.isSet() || currentUser == null) {
            showAlert("Non connecté", "Veuillez vous connecter pour gérer vos favoris.");
            return;
        }

        int userId = StaticUser.getId();
        Optional<Favori> existing = favoriService.findByUserIdAndRessourceId(userId, r.getId());

        if (existing.isPresent()) {
            favoriService.delete(existing.get().getId());
            showInfo("Favori", "Ressource retirée des favoris");
        } else {
            Favori nouveauFavori = new Favori(userId, r, LocalDate.now());
            favoriService.create(nouveauFavori);
            showInfo("Favori", "Ressource ajoutée aux favoris");
        }
        updateFavButton(btn, r);
    }

    private void openEvaluationList(Ressource r) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/evaluationsList.fxml"));
            Parent root = loader.load();

            EvaluationsListController controller = loader.getController();
            controller.setRessource(r);

            // Passer l'utilisateur connecté
            if (controller != null && currentUser != null) {
                controller.setCurrentUser(currentUser);
            }

            Stage stage = (Stage) cardsContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Évaluations - " + r.getTitle());

        } catch (IOException e) {
            System.err.println("❌ Erreur ouverture liste évaluations: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la liste des évaluations");
        }
    }

    private void openEvaluationModal(Ressource r) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/evaluationModal.fxml"));
            Parent root = loader.load();

            EvaluationModalController controller = loader.getController();
            controller.setRessource(r, this);

            // Passer l'utilisateur connecté
            if (controller != null && currentUser != null) {
                controller.setCurrentUser(currentUser);
            }

            Stage stage = new Stage();
            stage.setTitle("Ajouter une évaluation - " + r.getTitle());
            stage.setScene(new Scene(root));
            stage.showAndWait();

            refreshList(); // Rafraîchir après ajout

        } catch (IOException e) {
            System.err.println("❌ Erreur ouverture modal évaluation: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire d'évaluation");
        }
    }

    /**
     * Affiche une alerte d'erreur
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText("❌ " + message);
        alert.showAndWait();
    }

    /**
     * Affiche une alerte d'information
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText("✅ " + message);
        alert.showAndWait();
    }
}