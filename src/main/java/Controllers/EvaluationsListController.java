package Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import Models.Evaluation;
import Models.Ressource;
import Models.users;
import Services.evaluationService;
import utils.StaticUser;
import utils.StarRatingHelper;
import utils.SessionManager;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

public class EvaluationsListController {

    @FXML
    private Label lblResourceTitle;
    @FXML
    private TableView<Evaluation> tableEvaluations;
    @FXML
    private TableColumn<Evaluation, String> colUser;
    @FXML
    private TableColumn<Evaluation, Integer> colNote;
    @FXML
    private TableColumn<Evaluation, String> colComment;
    @FXML
    private TableColumn<Evaluation, LocalDate> colDate;

    @FXML
    private Button btnBack;
    @FXML
    private Button btnEdit;
    @FXML
    private Button btnDelete;

    @FXML
    private Label lblAppName;
    @FXML
    private Label lblGestionTitle;
    @FXML
    private Label lblStats;
    @FXML
    private Button btnHome;

    private Ressource currentRessource;
    private final evaluationService service = new evaluationService();
    private ObservableList<Evaluation> evaluationList = FXCollections.observableArrayList();

    // Utilisateur connecté
    private users currentUser;

    @FXML
    public void initialize() {
        System.out.println("\n=== Initialisation EvaluationsListController ===");

        // Récupérer l'utilisateur depuis la session
        currentUser = SessionManager.getInstance().getCurrentUser();

        applyIcon(btnBack, "back.png", "Retour", 20);
        applyIcon(btnEdit, "edit.png", "Modifier", 20);
        applyIcon(btnDelete, "delete.png", "Supprimer", 20);

        if (btnHome != null) {
            applyIcon(btnHome, "home.png", "Accueil", 20);
        }

        colNote.setCellValueFactory(new PropertyValueFactory<>("note"));
        colNote.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer note, boolean empty) {
                super.updateItem(note, empty);
                setText(empty || note == null ? "" : StarRatingHelper.toStarString(note));
                setStyle("-fx-font-size: 14px; -fx-text-fill: #F1C40F;");
            }
        });
        colComment.setCellValueFactory(new PropertyValueFactory<>("commentaire"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateEvaluation"));

        colUser.setCellValueFactory(cellData -> {
            int userId = cellData.getValue().getUserId();
            return new javafx.beans.property.SimpleStringProperty("Utilisateur " + userId);
        });

        // Définir le titre de la gestion
        if (lblGestionTitle != null) {
            lblGestionTitle.setText("⚖️ Évaluations");
        }

        System.out.println("=== Initialisation terminée ===\n");
    }

    /**
     * Définit l'utilisateur connecté
     */
    public void setCurrentUser(users user) {
        if (user != null) {
            this.currentUser = user;
            SessionManager.getInstance().setCurrentUser(user);
            System.out.println("✅ Utilisateur défini dans EvaluationsListController: " + user.getName());
        }
    }

    public void setRessource(Ressource r) {
        this.currentRessource = r;
        lblResourceTitle.setText("Ressource : " + r.getTitle());
        refreshList();
    }

    private void refreshList() {
        if (currentRessource != null) {
            evaluationList.setAll(service.findByRessourceId(currentRessource.getId()));
            tableEvaluations.setItems(evaluationList);
            updateStats();
        }
    }

    /**
     * Met à jour les statistiques
     */
    private void updateStats() {
        if (lblStats != null) {
            int count = evaluationList.size();
            lblStats.setText(count + " évaluation" + (count > 1 ? "s" : ""));
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/userressource.fxml"));
            Parent root = loader.load();

            // Passer l'utilisateur au contrôleur
            UserRessourceController controller = loader.getController();
            if (controller != null && currentUser != null) {
                controller.setCurrentUser(currentUser);
            }

            Stage stage = (Stage) tableEvaluations.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleHome() {
        System.out.println("\n=== Retour à l'accueil depuis Évaluations ===");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/home.fxml"));
            Parent root = loader.load();

            // Passer l'utilisateur connecté au HomeController
            HomeController homeController = loader.getController();
            if (homeController != null && currentUser != null) {
                homeController.setUser(currentUser);
                System.out.println("✅ Utilisateur transmis au HomeController");
            }

            Stage stage = (Stage) tableEvaluations.getScene().getWindow();
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

    @FXML
    private void handleDelete() {
        Evaluation selected = tableEvaluations.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Erreur", "Veuillez sélectionner une évaluation à supprimer.");
            return;
        }

        if (StaticUser.isSet() && selected.getUserId() != StaticUser.getId()) {
            showAlert("Non autorisé", "Vous ne pouvez supprimer que vos propres évaluations.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer l'évaluation ?");
        alert.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            service.delete(selected.getId());
            refreshList();
        }
    }

    @FXML
    private void handleEdit() {
        Evaluation selected = tableEvaluations.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Erreur", "Veuillez sélectionner une évaluation à modifier.");
            return;
        }

        if (StaticUser.isSet() && selected.getUserId() != StaticUser.getId()) {
            showAlert("Non autorisé", "Vous ne pouvez modifier que vos propres évaluations.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/evaluationModal.fxml"));
            Parent root = loader.load();

            EvaluationModalController controller = loader.getController();
            controller.setRessource(currentRessource, null);
            controller.setExistingEvaluation(selected);

            // Passer l'utilisateur connecté
            if (controller != null && currentUser != null) {
                controller.setCurrentUser(currentUser);
            }

            Stage stage = new Stage();
            stage.setTitle("Modifier l'évaluation");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            refreshList();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }

    private void applyIcon(Button btn, String iconName, String tooltipText, double size) {
        if (btn == null)
            return;
        for (String path : new String[] { "/icons/" + iconName, "/" + iconName }) {
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
                btn.setGraphic(iv);
                btn.setText(null);
                btn.setTooltip(new Tooltip(tooltipText));
                btn.setContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);
                btn.setMinWidth(44);
                btn.setMaxWidth(44);
                btn.setMinHeight(44);
                btn.setMaxHeight(44);
                return;
            } catch (Exception ignored) {
            }
        }
    }
}