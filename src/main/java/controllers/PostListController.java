package controllers;

import entities.ForumPost;
import services.ServiceForumPost;
import utils.BadWordsFilter;
import utils.Translator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class PostListController {

    @FXML private VBox discussionsContainer;
    @FXML private Label lblTotalPosts;
    @FXML private Button btnRetour;
    @FXML private ComboBox<String> filterCategory;
    @FXML private ComboBox<String> filterRole;
    @FXML private TextField searchField;
    @FXML private Button btnTriPopularite;
    @FXML private Button btnTriRecents;
    @FXML private Button btnTraduire;
    @FXML private Button btnNouveauPost;

    private ServiceForumPost service;
    private MainForumController mainController;
    private ObservableList<ForumPost> allPosts;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private boolean isFrench = true;

    @FXML
    public void initialize() {
        service = new ServiceForumPost();
        setupFilters();
        loadPosts();
    }

    public void setMainController(MainForumController controller) {
        this.mainController = controller;
    }

    public boolean isFrench() {
        return isFrench;
    }

    private void loadPosts() {
        allPosts = service.recuperer();
        afficherCartes(allPosts);
        updateTotalLabel();
    }

    private void updateTotalLabel() {
        String text = "📊 " + allPosts.size() + " discussion" + (allPosts.size() > 1 ? "s" : "");
        lblTotalPosts.setText(text);
    }

    private void afficherCartes(ObservableList<ForumPost> posts) {
        discussionsContainer.getChildren().clear();
        for (ForumPost post : posts) {
            VBox carte = createCarte(post);
            discussionsContainer.getChildren().add(carte);
        }
    }

    private VBox createCarte(ForumPost post) {
        VBox carte = new VBox(15);
        carte.setStyle("-fx-background-color: white; -fx-background-radius: 25; " +
                "-fx-padding: 20; -fx-border-color: #E2E8F0; -fx-border-width: 1; " +
                "-fx-border-radius: 25; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 3);");

        carte.setOnMouseEntered(e ->
                carte.setStyle("-fx-background-color: white; -fx-background-radius: 25; " +
                        "-fx-padding: 20; -fx-border-color: #667eea; -fx-border-width: 2; " +
                        "-fx-border-radius: 25; -fx-effect: dropshadow(gaussian, rgba(102,126,234,0.2), 15, 0, 0, 5);")
        );
        carte.setOnMouseExited(e ->
                carte.setStyle("-fx-background-color: white; -fx-background-radius: 25; " +
                        "-fx-padding: 20; -fx-border-color: #E2E8F0; -fx-border-width: 1; " +
                        "-fx-border-radius: 25; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 3);")
        );

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Circle avatar = new Circle(25);
        if (post.getRole().contains("Médecin")) {
            avatar.setFill(javafx.scene.paint.Color.web("#27AE60"));
        } else if (post.getRole().contains("Thérapeute")) {
            avatar.setFill(javafx.scene.paint.Color.web("#2980B9"));
        } else {
            avatar.setFill(javafx.scene.paint.Color.web("#E74C3C"));
        }
        avatar.setOpacity(0.2);

        StackPane avatarContainer = new StackPane(avatar);
        Label avatarIcon = new Label(getRoleIcon(post.getRole()));
        avatarIcon.setStyle("-fx-font-size: 24px;");
        avatarContainer.getChildren().add(avatarIcon);

        VBox authorInfo = new VBox(3);
        Label nomLabel = new Label(post.getNom());
        nomLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: #1A202C;");

        HBox roleBox = new HBox(10);
        roleBox.setAlignment(Pos.CENTER_LEFT);

        Label roleLabel = new Label(post.getRole());
        roleLabel.setStyle(getRoleStyle(post.getRole()));

        Label badgeLabel = new Label(post.getBadge());
        badgeLabel.setStyle("-fx-background-color: #EDF2F7; -fx-padding: 4 12; " +
                "-fx-background-radius: 20; -fx-text-fill: #4A5568; " +
                "-fx-font-size: 12px; -fx-font-weight: 700;");

        roleBox.getChildren().addAll(roleLabel, badgeLabel);
        authorInfo.getChildren().addAll(nomLabel, roleBox);

        header.getChildren().addAll(avatarContainer, authorInfo);

        Label dateLabel = new Label(post.getDateCreation().format(formatter));
        dateLabel.setStyle("-fx-text-fill: #718096; -fx-font-size: 13px; -fx-font-style: italic;");

        HBox dateBox = new HBox();
        dateBox.setAlignment(Pos.CENTER_RIGHT);
        dateBox.getChildren().add(dateLabel);

        VBox contentBox = new VBox(10);
        contentBox.setStyle("-fx-padding: 10 0 10 50;");

        Label contenuLabel = new Label(post.getContenu());
        contenuLabel.setWrapText(true);
        contenuLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #2D3748;");

        String categorieText = post.getCategorie();
        if (!isFrench) {
            categorieText = Translator.translate(categorieText, "en");
        }

        Label categorieLabel = new Label(getCategorieIcon(post.getCategorie()) + " " +
                categorieText.replaceAll("[😟🧠😢🧘💤💪👨‍👩‍👧💼🍎🏃]", "").trim());
        categorieLabel.setStyle("-fx-background-color: " + getCategorieCouleur(post.getCategorie()) + "; " +
                "-fx-padding: 6 18; -fx-background-radius: 25; -fx-text-fill: white; " +
                "-fx-font-size: 13px; -fx-font-weight: 700;");

        contentBox.getChildren().addAll(contenuLabel, categorieLabel);

        HBox statsBox = new HBox(25);
        statsBox.setAlignment(Pos.CENTER_LEFT);
        statsBox.setStyle("-fx-padding: 10 0 0 50;");

        Label likesLabel = new Label("👍 " + post.getLikes());
        likesLabel.setStyle("-fx-text-fill: #27AE60; -fx-font-weight: 700;");

        Label dislikesLabel = new Label("👎 " + post.getDislikes());
        dislikesLabel.setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: 700;");

        Label vuesLabel = new Label("👁️ " + post.getVues());
        vuesLabel.setStyle("-fx-text-fill: #718096;");

        statsBox.getChildren().addAll(likesLabel, dislikesLabel, vuesLabel);

        HBox actionsBox = new HBox(10);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);
        actionsBox.setStyle("-fx-padding: 15 0 0 0;");

        String voirText = isFrench ? "👁️ Voir" : "👁️ View";
        Button btnView = new Button(voirText);
        btnView.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: 700; " +
                "-fx-background-radius: 30; -fx-padding: 8 25; -fx-cursor: hand;");
        btnView.setOnAction(e -> handleVoirDetails(post));

        String modifierText = isFrench ? "✏️ Modifier" : "✏️ Edit";
        Button btnEdit = new Button(modifierText);
        btnEdit.setStyle("-fx-background-color: #F39C12; -fx-text-fill: white; -fx-font-weight: 700; " +
                "-fx-background-radius: 30; -fx-padding: 8 25; -fx-cursor: hand;");
        btnEdit.setOnAction(e -> handleModifier(post));

        String supprimerText = isFrench ? "🗑️ Supprimer" : "🗑️ Delete";
        Button btnDelete = new Button(supprimerText);
        btnDelete.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-weight: 700; " +
                "-fx-background-radius: 30; -fx-padding: 8 25; -fx-cursor: hand;");
        btnDelete.setOnAction(e -> handleSupprimer(post));

        Button btnLike = new Button("👍 " + post.getLikes());
        btnLike.setStyle("-fx-background-color: #E8F5E9; -fx-text-fill: #27AE60; -fx-font-weight: 700; " +
                "-fx-background-radius: 30; -fx-padding: 8 20; -fx-cursor: hand; " +
                "-fx-border-color: #A5D6A5; -fx-border-width: 1; -fx-border-radius: 30;");
        btnLike.setOnAction(e -> {
            service.incrementerLike(post.getIdPost());
            post.setLikes(post.getLikes() + 1);
            btnLike.setText("👍 " + post.getLikes());
            likesLabel.setText("👍 " + post.getLikes());
        });

        Button btnDislike = new Button("👎 " + post.getDislikes());
        btnDislike.setStyle("-fx-background-color: #FFEBEE; -fx-text-fill: #E74C3C; -fx-font-weight: 700; " +
                "-fx-background-radius: 30; -fx-padding: 8 20; -fx-cursor: hand; " +
                "-fx-border-color: #FFCDD2; -fx-border-width: 1; -fx-border-radius: 30;");
        btnDislike.setOnAction(e -> {
            service.incrementerDislike(post.getIdPost());
            post.setDislikes(post.getDislikes() + 1);
            btnDislike.setText("👎 " + post.getDislikes());
            dislikesLabel.setText("👎 " + post.getDislikes());
        });

        actionsBox.getChildren().addAll(btnView, btnEdit, btnDelete, btnLike, btnDislike);

        HBox headerRow = new HBox();
        headerRow.getChildren().addAll(header, dateBox);
        HBox.setHgrow(header, Priority.ALWAYS);

        carte.getChildren().addAll(headerRow, contentBox, statsBox, actionsBox);

        return carte;
    }

    private String getRoleIcon(String role) {
        if (role.contains("Médecin")) return "👨‍⚕️";
        if (role.contains("Thérapeute")) return "🧘";
        return "🧑";
    }

    private String getRoleStyle(String role) {
        if (role.contains("Médecin")) {
            return "-fx-background-color: #E8F5E9; -fx-text-fill: #27AE60; -fx-font-weight: 700; " +
                    "-fx-background-radius: 20; -fx-padding: 4 15; -fx-font-size: 13px;";
        } else if (role.contains("Thérapeute")) {
            return "-fx-background-color: #E5F0FF; -fx-text-fill: #2980B9; -fx-font-weight: 700; " +
                    "-fx-background-radius: 20; -fx-padding: 4 15; -fx-font-size: 13px;";
        } else {
            return "-fx-background-color: #FFE5E5; -fx-text-fill: #E74C3C; -fx-font-weight: 700; " +
                    "-fx-background-radius: 20; -fx-padding: 4 15; -fx-font-size: 13px;";
        }
    }

    private String getCategorieIcon(String categorie) {
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

    private String getCategorieCouleur(String categorie) {
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

    private void setupFilters() {
        if (filterCategory != null) {
            filterCategory.setItems(FXCollections.observableArrayList(
                    "Toutes", "😟 Anxiété", "🧠 Stress", "😢 Dépression", "🧘 Méditation",
                    "💤 Sommeil", "💪 Estime de soi", "💬 Général"
            ));
            filterCategory.setValue("Toutes");
            filterCategory.setOnAction(e -> applyFilters());
        }

        if (filterRole != null) {
            filterRole.setItems(FXCollections.observableArrayList(
                    "Tous", "🧑 Patient", "👨‍⚕️ Médecin", "🧘 Thérapeute"
            ));
            filterRole.setValue("Tous");
            filterRole.setOnAction(e -> applyFilters());
        }

        if (searchField != null) {
            searchField.textProperty().addListener((obs, old, nv) -> applyFilters());
        }
    }

    // ✅ MÉTHODE APPLY FILTERS CORRIGÉE
    private void applyFilters() {
        if (allPosts == null || allPosts.isEmpty()) {
            return;
        }

        String cat = "Toutes";
        String role = "Tous";

        if (filterCategory != null && filterCategory.getValue() != null) {
            cat = filterCategory.getValue();
        }
        if (filterRole != null && filterRole.getValue() != null) {
            role = filterRole.getValue();
        }

        String search = (searchField != null && searchField.getText() != null)
                ? searchField.getText().toLowerCase() : "";

        ObservableList<ForumPost> filtered = FXCollections.observableArrayList();

        for (ForumPost post : allPosts) {
            String postCat = post.getCategorie().replaceAll("[😟🧠😢🧘💤💪👨‍👩‍👧💼🍎🏃]", "").trim();
            String filterCat = cat.replaceAll("[😟🧠😢🧘💤💪👨‍👩‍👧💼🍎🏃]", "").trim();

            String postRole = post.getRole().replaceAll("[🧑👨‍⚕️🧘]", "").trim();
            String filterRole = role.replaceAll("[🧑👨‍⚕️🧘]", "").trim();

            boolean matchCat = cat.equals("Toutes") || cat.equals("All") || postCat.equalsIgnoreCase(filterCat);
            boolean matchRole = role.equals("Tous") || role.equals("All") || postRole.equalsIgnoreCase(filterRole);
            boolean matchSearch = search.isEmpty() ||
                    post.getNom().toLowerCase().contains(search) ||
                    post.getContenu().toLowerCase().contains(search);

            if (matchCat && matchRole && matchSearch) {
                filtered.add(post);
            }
        }

        if (filtered.isEmpty()) {
            afficherCartes(allPosts);
            lblTotalPosts.setText("📊 " + allPosts.size() + " discussion(s) - Aucun résultat pour ce filtre");
        } else {
            afficherCartes(filtered);
            lblTotalPosts.setText("📊 " + filtered.size() + " discussion" + (filtered.size() > 1 ? "s" : ""));
        }
    }

    @FXML
    private void handleNouveauPost() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/post_form.fxml"));
            VBox page = loader.load();

            PostFormController controller = loader.getController();
            controller.setService(service);
            controller.setPostListController(this);
            controller.setMode(PostFormController.Mode.AJOUT);

            Stage stage = new Stage();
            stage.setTitle(isFrench ? "Nouvelle discussion - GrowMind" : "New discussion - GrowMind");
            stage.setScene(new Scene(page));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleTriPopularite() {
        ObservableList<ForumPost> trie = service.recupererTrieParPopularite();
        afficherCartes(trie);
        String text = "📊 " + trie.size() + " discussion(s) - " + (isFrench ? "Triés par popularité" : "Sorted by popularity");
        lblTotalPosts.setText(text);
    }

    @FXML
    private void handleTriRecents() {
        ObservableList<ForumPost> recents = service.recuperer();
        afficherCartes(recents);
        String text = "📊 " + recents.size() + " discussion(s) - " + (isFrench ? "Triés par date" : "Sorted by date");
        lblTotalPosts.setText(text);
    }

    // ✅ MÉTHODE HANDLE TOGGLE LANGUE CORRIGÉE
    @FXML
    private void handleToggleLangue() {
        isFrench = !isFrench;
        if (btnTraduire != null) {
            btnTraduire.setText((isFrench ? "🇫🇷 Français" : "🇬🇧 English"));
        }

        translateInterface();
        refreshTable();
    }

    // ✅ MÉTHODE TRANSLATE INTERFACE CORRIGÉE
    private void translateInterface() {
        String targetLang = isFrench ? "fr" : "en";

        if (btnTriPopularite != null) {
            btnTriPopularite.setText(Translator.translate("populaires", targetLang));
        }
        if (btnTriRecents != null) {
            btnTriRecents.setText(Translator.translate("récents", targetLang));
        }
        if (btnRetour != null) {
            btnRetour.setText(Translator.translate("retour", targetLang));
        }
        if (btnNouveauPost != null) {
            btnNouveauPost.setText(isFrench ? "➕ Nouvelle discussion" : "➕ New discussion");
        }

        if (filterCategory != null) {
            filterCategory.setPromptText(Translator.translate("filtrer", targetLang));
        }
        if (filterRole != null) {
            filterRole.setPromptText(Translator.translate("filtrer", targetLang));
        }

        if (searchField != null) {
            searchField.setPromptText(Translator.translate("rechercher une discussion", targetLang));
        }

        translateComboBoxItems();
    }

    // ✅ MÉTHODE TRANSLATE COMBOBOX ITEMS CORRIGÉE
    private void translateComboBoxItems() {
        if (filterCategory != null) {
            ObservableList<String> items = FXCollections.observableArrayList();

            if (isFrench) {
                items.addAll("Toutes", "😟 Anxiété", "🧠 Stress", "😢 Dépression",
                        "🧘 Méditation", "💤 Sommeil", "💪 Estime de soi", "💬 Général");
            } else {
                items.addAll("All", "😟 Anxiety", "🧠 Stress", "😢 Depression",
                        "🧘 Meditation", "💤 Sleep", "💪 Self-esteem", "💬 General");
            }

            filterCategory.setItems(items);
            filterCategory.setValue(items.get(0));
        }

        if (filterRole != null) {
            ObservableList<String> items = FXCollections.observableArrayList();

            if (isFrench) {
                items.addAll("Tous", "🧑 Patient", "👨‍⚕️ Médecin", "🧘 Thérapeute");
            } else {
                items.addAll("All", "🧑 Patient", "👨‍⚕️ Doctor", "🧘 Therapist");
            }

            filterRole.setItems(items);
            filterRole.setValue(items.get(0));
        }
    }

    private void handleVoirDetails(ForumPost post) {
        try {
            service.incrementerVue(post.getIdPost());
            post.setVues(post.getVues() + 1);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/post_details.fxml"));
            VBox page = loader.load();

            PostDetailsController controller = loader.getController();
            controller.setService(service);
            controller.setPost(post);

            Stage stage = new Stage();
            stage.setTitle(isFrench ? "Détails - " + post.getNom() : "Details - " + post.getNom());
            stage.setScene(new Scene(page, 700, 800));
            stage.showAndWait();

            refreshTable();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir les détails");
        }
    }

    private void handleModifier(ForumPost post) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/post_form.fxml"));
            VBox page = loader.load();

            PostFormController controller = loader.getController();
            controller.setService(service);
            controller.setPostListController(this);
            controller.setMode(PostFormController.Mode.MODIFICATION);
            controller.setPost(post);

            Stage stage = new Stage();
            stage.setTitle(isFrench ? "Modifier - GrowMind" : "Edit - GrowMind");
            stage.setScene(new Scene(page));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleSupprimer(ForumPost post) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(isFrench ? "Confirmation" : "Confirmation");
        alert.setHeaderText(isFrench ? "Supprimer cette discussion ?" : "Delete this discussion?");
        alert.setContentText(isFrench ? "Cette action est irréversible." : "This action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                service.supprimer(post);
                loadPosts();
                if (mainController != null) mainController.refreshStatistics();
            }
        });
    }

    @FXML
    private void handleRetour() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main_forum.fxml"));
            AnchorPane dashboard = loader.load();
            Stage stage = (Stage) btnRetour.getScene().getWindow();
            stage.setScene(new Scene(dashboard, 1300, 750));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshTable() {
        loadPosts();
        applyFilters();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}