package org.example.Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.example.Models.Favori;
import org.example.Models.Ressource;
import org.example.Models.User;
import org.example.Services.ressourceService;
import org.example.Services.favoriService;
import org.example.utils.StaticUser;

import java.io.IOException;
import java.time.LocalDate;
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

    private final ressourceService ressourceService = new ressourceService();
    private final favoriService favoriService = new favoriService();

    private ObservableList<Ressource> allRessources = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        filterType.getItems().addAll("Tous", "formation", "article", "video", "image", "evenement");
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

        Label desc = new Label(r.getDescription());
        desc.setWrapText(true);
        desc.setStyle("-fx-text-fill: #34495e;");
        desc.setPrefHeight(60); // Limit height visually

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Buttons
        HBox actions = new HBox(10);
        actions.setAlignment(javafx.geometry.Pos.CENTER);

        Button btnEval = new Button("⭐ Évaluer");
        btnEval.setStyle(
                "-fx-background-color: #F1C40F; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 15; -fx-padding: 8 15; -fx-font-size: 13px;");
        btnEval.setOnAction(e -> openEvaluationList(r));

        Button btnAddEval = new Button("➕ Ajouter");
        btnAddEval.setStyle(
                "-fx-background-color: #5FB49C; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 15; -fx-padding: 8 15; -fx-font-size: 13px;");
        btnAddEval.setOnAction(e -> openEvaluationModal(r));

        Button btnFav = new Button();
        updateFavButton(btnFav, r);
        btnFav.setOnAction(e -> toggleFavorite(r, btnFav));

        actions.getChildren().addAll(btnAddEval, btnEval, btnFav);

        card.getChildren().addAll(title, typeCat, desc, spacer, actions);
        return card;
    }

    private void updateFavButton(Button btn, Ressource r) {
        if (!StaticUser.isSet()) {
            btn.setDisable(true);
            return;
        }
        boolean isFav = favoriService.findByUserIdAndRessourceId(StaticUser.get().getId(), r.getId()).isPresent();
        if (isFav) {
            btn.setText("❤️");
            btn.setStyle(
                    "-fx-background-color: #E667AF; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 15; -fx-padding: 8 15; -fx-font-size: 13px;");
        } else {
            btn.setText("🤍");
            btn.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 15; -fx-padding: 8 15; -fx-font-size: 13px; -fx-border-color: #bdc3c7; -fx-border-radius: 15; -fx-border-width: 2;");
        }
    }

    private void toggleFavorite(Ressource r, Button btn) {
        User user = StaticUser.get();
        if (user == null)
            return;

        Optional<Favori> existing = favoriService.findByUserIdAndRessourceId(user.getId(), r.getId());
        if (existing.isPresent()) {
            favoriService.delete(existing.get().getId());
        } else {
            favoriService.create(new Favori(user, r, LocalDate.now()));
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
}
