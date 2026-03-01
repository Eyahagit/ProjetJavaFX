package org.example.Controllers;

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
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import org.example.Models.Favori;
import org.example.Models.Ressource;
import org.example.Models.Evaluation;
import org.example.Services.ressourceService;
import org.example.Services.favoriService;
import org.example.Services.evaluationService;
import org.example.utils.StaticUser;
import org.example.utils.StarRatingHelper;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.stage.FileChooser;

public class UserRessourceController {

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> filterType;
    @FXML
    private ComboBox<String> filterCategory;
    @FXML
    private FlowPane cardsContainer;

    private final ressourceService ressourceService = new ressourceService();
    private final favoriService favoriService = new favoriService();
    private final evaluationService evaluationService = new evaluationService();

    private ObservableList<Ressource> allRessources = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        filterType.getItems().addAll("Tous", "formation", "article", "video", "image", "pdf", "evenement");
        filterType.getSelectionModel().selectFirst();
        filterType.setOnAction(e -> applyFilters());

        filterCategory.getItems().addAll("Tous", "santé", "bien-etre", "developement personel", "motivation");
        filterCategory.getSelectionModel().selectFirst();
        filterCategory.setOnAction(e -> applyFilters());

        refreshList();
    }

    public void refreshList() {
        allRessources.setAll(ressourceService.findAll());
        applyFilters();
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
                        || r.getDescription().toLowerCase().contains(search))
                .collect(Collectors.toList());

        displayCards(filtered);
    }

    @FXML
    private void handleSearch() {
        applyFilters();
    }

    /**
     * Load an icon from resources; tries /icons/ then root, and alternate filenames
     * (rate, favourite).
     */
    private ImageView loadIcon(String name, double size) {
        String[] paths;
        if ("star.png".equals(name)) {
            paths = new String[] { "/icons/rate.png", "/rate.png", "/icons/star.png", "/star.png" };
        } else if ("heart.png".equals(name) || "heart_empty.png".equals(name)) {
            paths = new String[] { "/icons/favourite.png", "/favourite.png", "/icons/" + name, "/" + name };
        } else if ("download.png".equals(name)) {
            paths = new String[] { "/icons/download.png", "/download.png" };
        } else {
            paths = new String[] { "/icons/" + name, "/" + name };
        }
        for (String path : paths) {
            try {
                java.io.InputStream is = getClass().getResourceAsStream(path);
                if (is == null)
                    continue;
                Image img = new Image(is);
                if (img.isError())
                    continue;
                ImageView iv = new ImageView(img);
                iv.setFitWidth(size);
                iv.setFitHeight(size);
                iv.setPreserveRatio(true);
                iv.setSmooth(true);
                return iv;
            } catch (Exception e) {
                // try next path
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
        VBox card = new VBox(10);
        card.setStyle(
                "-fx-background-color: white; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 1); -fx-background-radius: 10; -fx-pref-width: 250; -fx-min-height: 300;");

        Label title = new Label(r.getTitle());
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        title.setWrapText(true);

        Label typeCat = new Label(r.getType() + " | " + r.getCategory());
        typeCat.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d; -fx-font-style: italic;");

        Label ratingStars = new Label();
        java.util.List<Evaluation> evals = evaluationService.findByRessourceId(r.getId());
        if (!evals.isEmpty()) {
            double avg = evals.stream().mapToInt(Evaluation::getNote).average().orElse(0);
            ratingStars
                    .setText(StarRatingHelper.toStarStringFromAverage(avg) + " (" + String.format("%.1f", avg) + ")");
        } else {
            ratingStars.setText(StarRatingHelper.toStarString(0) + " (—)");
        }
        ratingStars.setStyle("-fx-font-size: 14px; -fx-text-fill: #F1C40F;");

        Label desc = new Label(r.getDescription());
        desc.setWrapText(true);
        desc.setStyle("-fx-text-fill: #34495e;");
        desc.setPrefHeight(60);

        // ── Image preview (if type == "image") ────────────────────────────────
        String type = r.getType() != null ? r.getType().toLowerCase() : "";
        String contentUrl = r.getContent();

        if ("image".equals(type) && contentUrl != null && !contentUrl.isBlank()) {
            try {
                Image img = new Image(contentUrl, 220, 150, true, true, true);
                ImageView imageView = new ImageView(img);
                imageView.setFitWidth(220);
                imageView.setFitHeight(150);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
                imageView.setStyle("-fx-background-radius: 8;");
                card.getChildren().add(imageView);
            } catch (Exception ignored) {
            }
        } else if ("video".equals(type) && contentUrl != null && !contentUrl.isBlank()) {
            try {
                Media media = new Media(contentUrl);
                MediaPlayer mediaPlayer = new MediaPlayer(media);
                MediaView mediaView = new MediaView(mediaPlayer);
                mediaView.setFitWidth(220);
                mediaView.setFitHeight(150);
                mediaView.setPreserveRatio(true);

                Button playBtn = new Button("▶");
                playBtn.setStyle(
                        "-fx-background-color: rgba(0,0,0,0.5); -fx-text-fill: white; -fx-shape: 'M 0 0 L 10 5 L 0 10 Z'; -fx-min-width: 40; -fx-min-height: 40; -fx-cursor: hand;");
                playBtn.setOnAction(e -> {
                    if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                        mediaPlayer.pause();
                        playBtn.setText("▶");
                        playBtn.setStyle(
                                "-fx-background-color: rgba(0,0,0,0.5); -fx-text-fill: white; -fx-min-width: 40; -fx-min-height: 40; -fx-cursor: hand;");
                    } else {
                        mediaPlayer.play();
                        playBtn.setText("||");
                        playBtn.setStyle(
                                "-fx-background-color: rgba(0,0,0,0.3); -fx-text-fill: white; -fx-min-width: 40; -fx-min-height: 40; -fx-cursor: hand;");
                    }
                });

                StackPane videoStack = new StackPane(mediaView, playBtn);
                videoStack.setStyle("-fx-background-color: black; -fx-background-radius: 8;");
                videoStack.setPrefSize(220, 150);
                card.getChildren().add(videoStack);

                // Stop media when card is removed or scene changes (basic cleanup)
                card.sceneProperty().addListener((obs, oldScene, newScene) -> {
                    if (newScene == null)
                        mediaPlayer.stop();
                });
            } catch (Exception e) {
                Label errorLabel = new Label("Vidéo non disponible");
                errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 10px;");
                card.getChildren().add(errorLabel);
            }
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Buttons — icon-only, square, no truncated text
        HBox actions = new HBox(10);
        actions.setAlignment(javafx.geometry.Pos.CENTER);

        final String btnStyle = "-fx-cursor: hand; -fx-background-radius: 12; -fx-padding: 10; -fx-min-width: 44; -fx-min-height: 44; -fx-max-width: 44; -fx-max-height: 44; -fx-content-display: graphic-only;";
        final double iconSize = 24;

        Button btnAddEval = new Button();
        ImageView addIcon = loadIcon("add.png", iconSize);
        if (addIcon != null) {
            btnAddEval.setGraphic(addIcon);
            btnAddEval.setText(null);
            btnAddEval.setTooltip(new Tooltip("Ajouter une évaluation"));
        } else {
            btnAddEval.setText("+");
            btnAddEval.setTooltip(new Tooltip("Ajouter"));
        }
        btnAddEval.setStyle(btnStyle
                + " -fx-background-color: #5FB49C; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px;");
        btnAddEval.setOnAction(e -> openEvaluationModal(r));

        Button btnEval = new Button();
        ImageView starIcon = loadIcon("star.png", iconSize);
        if (starIcon != null) {
            btnEval.setGraphic(starIcon);
            btnEval.setText(null);
            btnEval.setTooltip(new Tooltip("Voir les évaluations"));
        } else {
            btnEval.setText("☆");
            btnEval.setTooltip(new Tooltip("Évaluer"));
        }
        btnEval.setStyle(btnStyle
                + " -fx-background-color: #F1C40F; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px;");
        btnEval.setOnAction(e -> openEvaluationList(r));

        Button btnFav = new Button();
        btnFav.setStyle(btnStyle);
        updateFavButton(btnFav, r);
        btnFav.setOnAction(e -> toggleFavorite(r, btnFav));

        // 💾 NEW: Local Download Button
        Button btnDownload = new Button();
        btnDownload.setStyle(btnStyle + " -fx-background-color: #34495e; -fx-text-fill: white;");
        ImageView downloadIcon = loadIcon("download.png", iconSize);
        if (downloadIcon != null) {
            btnDownload.setGraphic(downloadIcon);
        } else {
            btnDownload.setText("⬇");
            btnDownload
                    .setStyle(btnStyle + " -fx-background-color: #34495e; -fx-text-fill: white; -fx-font-size: 18px;");
        }
        btnDownload.setTooltip(new Tooltip("Télécharger sur mon PC"));
        btnDownload.setOnAction(e -> handleDownload(r));

        actions.getChildren().addAll(btnAddEval, btnEval, btnFav, btnDownload);

        // ── PDF download button (if type == "pdf") ────────────────────────────
        if ("pdf".equals(type) && contentUrl != null && !contentUrl.isBlank()) {
            Button btnPdf = new Button("📄 Télécharger PDF");
            btnPdf.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-weight: bold;"
                    + " -fx-font-size: 13px; -fx-background-radius: 10; -fx-cursor: hand; -fx-padding: 8 16;");
            btnPdf.setMaxWidth(Double.MAX_VALUE);
            btnPdf.setOnAction(e -> {
                try {
                    String finalUrl = contentUrl;
                    // If it's a Cloudinary URL but not identified as pdf in the path, try to fix it
                    if (finalUrl.contains("cloudinary.com") && !finalUrl.toLowerCase().contains("/raw/")
                            && finalUrl.toLowerCase().endsWith(".pdf")) {
                        // Cloudinary sometimes needs /raw/ instead of /image/ for PDFs if not uploaded
                        // as auto
                        // But since we fixed CloudinaryService to use auto, this is just for
                        // existing/legacy ones
                    }

                    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        Desktop.getDesktop().browse(new URI(finalUrl));
                    } else {
                        new ProcessBuilder("cmd", "/c", "start", "", finalUrl.replace("&", "^&")).start();
                    }
                } catch (Exception ex) {
                    new Alert(Alert.AlertType.ERROR,
                            "Impossible d’ouvrir le PDF : " + ex.getMessage()).showAndWait();
                }
            });
            card.getChildren().addAll(title, typeCat, ratingStars, desc, spacer, btnPdf, actions);
        } else {
            card.getChildren().addAll(title, typeCat, ratingStars, desc, spacer, actions);
        }
        return card;
    }

    private void updateFavButton(Button btn, Ressource r) {
        final String baseStyle = "-fx-cursor: hand; -fx-background-radius: 12; -fx-padding: 10; -fx-min-width: 44; -fx-min-height: 44; -fx-max-width: 44; -fx-max-height: 44; -fx-content-display: graphic-only;";
        final double iconSize = 24;

        if (!StaticUser.isSet()) {
            btn.setDisable(true);
            btn.setStyle(baseStyle + " -fx-background-color: #e0e0e0; -fx-opacity: 0.7;");
            return;
        }
        boolean isFav = favoriService.findByUserIdAndRessourceId(StaticUser.getId(), r.getId()).isPresent();
        ImageView heartIcon = loadIcon(isFav ? "heart.png" : "heart_empty.png", iconSize);
        if (heartIcon != null) {
            btn.setGraphic(heartIcon);
            btn.setText(null);
            btn.setTooltip(new Tooltip(isFav ? "Retirer des favoris" : "Ajouter aux favoris"));
        } else {
            btn.setGraphic(null);
            btn.setText(isFav ? "♥" : "♡");
            btn.setStyle(baseStyle + " -fx-font-size: 18px; -fx-content-display: center;");
            btn.setTooltip(new Tooltip(isFav ? "Retirer des favoris" : "Ajouter aux favoris"));
        }
        if (isFav) {
            btn.setStyle(baseStyle + " -fx-background-color: #E667AF; -fx-text-fill: white; -fx-font-weight: bold;");
        } else {
            btn.setStyle(baseStyle
                    + " -fx-background-color: transparent; -fx-border-color: #bdc3c7; -fx-border-radius: 12; -fx-border-width: 2;");
        }
    }

    private void toggleFavorite(Ressource r, Button btn) {
        if (!StaticUser.isSet())
            return;

        int userId = StaticUser.getId();
        Optional<Favori> existing = favoriService.findByUserIdAndRessourceId(userId, r.getId());
        if (existing.isPresent()) {
            favoriService.delete(existing.get().getId());
        } else {
            favoriService.create(new Favori(userId, r, LocalDate.now()));
        }
        updateFavButton(btn, r);
    }

    private void openEvaluationList(Ressource r) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/evaluationsList.fxml"));
            Parent root = loader.load();

            EvaluationsListController controller = loader.getController();
            controller.setRessource(r);

            Stage stage = (Stage) cardsContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openEvaluationModal(Ressource r) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/evaluationModal.fxml"));
            Parent root = loader.load();

            EvaluationModalController controller = loader.getController();
            controller.setRessource(r, this);

            Stage stage = new Stage();
            stage.setTitle("Ajouter une évaluation - " + r.getTitle());
            stage.setScene(new Scene(root));
            stage.showAndWait();

            refreshList(); // Refresh after adding evaluation
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void switchToAdmin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/main.fxml"));
            Stage stage = (Stage) cardsContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("GrowMind - Admin Dashboard");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDownload(Ressource r) {
        String urlStr = r.getContent();
        if (urlStr == null || urlStr.isBlank()) {
            new Alert(Alert.AlertType.WARNING, "Pas de lien pour télécharger cette ressource.").show();
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Sauvegarder la ressource");

        // Robust extension detection for Cloudinary URLs
        String extension = ".dat";
        String lowerUrl = urlStr.toLowerCase();
        if (lowerUrl.endsWith(".pdf") || lowerUrl.contains("/pdf/") || lowerUrl.contains("/raw/"))
            extension = ".pdf";
        else if (lowerUrl.endsWith(".mp4") || lowerUrl.contains("/video/"))
            extension = ".mp4";
        else if (lowerUrl.endsWith(".jpg") || lowerUrl.endsWith(".jpeg") || lowerUrl.contains("/image/"))
            extension = ".jpg";
        else if (lowerUrl.endsWith(".png"))
            extension = ".png";

        chooser.setInitialFileName(r.getTitle().replaceAll("[\\\\/:*?\"<>|]", "_") + extension);

        File dest = chooser.showSaveDialog(cardsContainer.getScene().getWindow());
        if (dest != null) {
            new Thread(() -> {
                try (InputStream in = new URL(urlStr).openStream()) {
                    Files.copy(in, Paths.get(dest.toURI()), StandardCopyOption.REPLACE_EXISTING);
                    javafx.application.Platform.runLater(() -> {
                        new Alert(Alert.AlertType.INFORMATION, "Téléchargement réussi : " + dest.getName()).show();
                    });
                } catch (Exception ex) {
                    javafx.application.Platform.runLater(() -> {
                        new Alert(Alert.AlertType.ERROR, "Erreur lors du téléchargement : " + ex.getMessage()).show();
                    });
                }
            }).start();
        }
    }
}
