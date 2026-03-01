package com.santebienetre.controller;

import com.santebienetre.model.SanteBienEtre;
import com.santebienetre.service.SanteBienEtreService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

/**
 * Controller for the Gestion Santé & Bien-être view.
 * Handles CRUD operations, filtering, and stress chart display.
 * 
 * @author Application Santé & Bien-être
 * @version 1.0
 */
public class GestionSanteBienEtreController implements Initializable {

    @FXML private TextField tfUserId;
    @FXML private ComboBox<String> cbHumeur;
    @FXML private Spinner<Integer> spNiveauStress;
    @FXML private Spinner<Integer> spQualiteSommeil;
    @FXML private DatePicker dpDateSuivi;
    @FXML private TextField tfNutrition;
    @FXML private TextField tfActivite;
    @FXML private TextField tfDeveloppement;
    @FXML private TextArea taRecommandations;
    @FXML private TableView<SanteBienEtre> tableView;
    @FXML private TableColumn<SanteBienEtre, Integer> colId;
    @FXML private TableColumn<SanteBienEtre, Integer> colUserId;
    @FXML private TableColumn<SanteBienEtre, String> colHumeur;
    @FXML private TableColumn<SanteBienEtre, Integer> colStress;
    @FXML private TableColumn<SanteBienEtre, Integer> colSommeil;
    @FXML private TableColumn<SanteBienEtre, String> colNutrition;
    @FXML private TableColumn<SanteBienEtre, String> colActivite;
    @FXML private TableColumn<SanteBienEtre, String> colDeveloppement;
    @FXML private TableColumn<SanteBienEtre, LocalDate> colDate;
    @FXML private TableColumn<SanteBienEtre, String> colRecommandations;
    @FXML private Pagination pagination;
    @FXML private DatePicker dpDateDebut;
    @FXML private DatePicker dpDateFin;
    @FXML private LineChart<String, Number> lineChartStress;
    @FXML private ImageView logoImageView;
    @FXML private CheckBox chkShowAll;

    private final SanteBienEtreService service = new SanteBienEtreService();
    private final ObservableList<SanteBienEtre> dataList = FXCollections.observableArrayList();
    private int currentPageUserId = DEFAULT_USER_ID;
    private final int ITEMS_PER_PAGE = 10;
    private boolean showAllMode = false;

    /** Default user ID for demo (can be changed by user) */
    private static final int DEFAULT_USER_ID = 1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Pattern digitsOnly = Pattern.compile("\\d*");
        UnaryOperator<TextFormatter.Change> digitsFilter = change -> {
            String text = change.getControlNewText();
            return digitsOnly.matcher(text).matches() ? change : null;
        };
        tfUserId.setTextFormatter(new TextFormatter<>(digitsFilter));

        spNiveauStress.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 5));
        spQualiteSommeil.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 6));
        spNiveauStress.setEditable(true);
        spQualiteSommeil.setEditable(true);

        tfNutrition.setTextFormatter(new TextFormatter<>(change -> {
            return change.getControlNewText().length() <= 255 ? change : null;
        }));
        tfActivite.setTextFormatter(new TextFormatter<>(change -> {
            return change.getControlNewText().length() <= 255 ? change : null;
        }));
        tfDeveloppement.setTextFormatter(new TextFormatter<>(change -> {
            return change.getControlNewText().length() <= 500 ? change : null;
        }));

        // Initialize ComboBox mood options
        cbHumeur.setItems(FXCollections.observableArrayList(
                "Très bien", "Bien", "Moyen", "Stressé", "Fatigué"
        ));

        // Set default date
        dpDateSuivi.setValue(LocalDate.now());
        tfUserId.setText(String.valueOf(DEFAULT_USER_ID));

        // Bind TableView columns to entity properties
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colHumeur.setCellValueFactory(new PropertyValueFactory<>("humeur"));
        colStress.setCellValueFactory(new PropertyValueFactory<>("niveauStress"));
        colSommeil.setCellValueFactory(new PropertyValueFactory<>("qualiteSommeil"));
        colNutrition.setCellValueFactory(new PropertyValueFactory<>("nutrition"));
        colActivite.setCellValueFactory(new PropertyValueFactory<>("activitePhysique"));
        colDeveloppement.setCellValueFactory(new PropertyValueFactory<>("developpementPersonnel"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateSuivi"));
        colRecommandations.setCellValueFactory(new PropertyValueFactory<>("recommandations"));

        tableView.setItems(dataList);

        // Load table selection into form for update
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                fillForm(newVal);
            }
        });

        // Setup checkbox "Tout afficher"
        chkShowAll.setOnAction(e -> {
            showAllMode = chkShowAll.isSelected();
            refreshCurrentView();
        });

        // Setup pagination
        setupPagination();

        // Update recommendations preview when key fields change
        cbHumeur.valueProperty().addListener((o, a, b) -> showPreviewRecommendations());
        spNiveauStress.valueProperty().addListener((o, a, b) -> showPreviewRecommendations());
        spQualiteSommeil.valueProperty().addListener((o, a, b) -> showPreviewRecommendations());

        // Load initial data and recommendations preview
        refreshCurrentView();
        showPreviewRecommendations();
        
        // Load GrowMind logo
        loadLogo();
    }

    /** Loads all records into TableView */
    private void loadData() {
        dataList.clear();
        List<SanteBienEtre> list = service.getAllSanteBienEtre();
        dataList.addAll(list);
        System.err.println("[DEBUG] SantéBienEtre loaded " + list.size() + " records from DB.");
        System.err.println("[DEBUG] ObservableList now has " + dataList.size() + " items.");
        tableView.setItems(dataList);
        tableView.refresh();
        Platform.runLater(() -> {
            System.err.println("[DEBUG] TableView visible rows after refresh: " + tableView.getItems().size());
            System.err.println("[DEBUG] TableView columns count: " + tableView.getColumns().size());
            System.err.println("[DEBUG] TableView height: " + tableView.getHeight());
            System.err.println("[DEBUG] TableView visible: " + tableView.isVisible());
            tableView.requestLayout();
            tableView.autosize();
        });
        updateStressChart(list);
    }

    /** Setup pagination for user navigation */
    private void setupPagination() {
        if (showAllMode) {
            pagination.setPageCount(1);
            pagination.setCurrentPageIndex(0);
            return;
        }

        List<Integer> userIds = service.getAllUserIds();
        if (userIds.isEmpty()) {
            pagination.setPageCount(1);
            return;
        }
        
        int totalUsers = userIds.size();
        int currentIndex = userIds.indexOf(currentPageUserId);
        if (currentIndex == -1) {
            currentPageUserId = userIds.get(0);
            currentIndex = 0;
        }

        pagination.setPageCount(totalUsers);
        pagination.setCurrentPageIndex(currentIndex);

        pagination.setPageFactory((pageIndex) -> {
            if (pageIndex >= 0 && pageIndex < userIds.size()) {
                int targetUserId = userIds.get(pageIndex);
                currentPageUserId = targetUserId;
                loadUserData(targetUserId);
            }
            // Return a simple container to avoid recreating tableView
            return new VBox();
        });
    }

    /** Load data for a specific user */
    private void loadUserData(int userId) {
        dataList.clear();
        List<SanteBienEtre> list = service.getSanteBienEtreByUser(userId);
        dataList.addAll(list);
        System.err.println("[DEBUG] Loaded " + list.size() + " records for user " + userId);
        tableView.setItems(dataList);
        tableView.refresh();
        updateStressChart(list);
    }

    private void refreshCurrentView() {
        if (showAllMode) {
            loadData();
        } else {
            setupPagination();
            loadUserData(currentPageUserId);
        }
        tableView.refresh();
        tableView.requestLayout();
    }

    /** Loads records filtered by date range */
    private void loadFilteredData(LocalDate start, LocalDate end) {
        int userId = getUserId();
        dataList.clear();
        List<SanteBienEtre> list = service.getSanteBienEtreByDateRange(userId, start, end);
        dataList.addAll(list);
        updateStressChart(list);
    }

    /** Updates the stress evolution LineChart */
    private void updateStressChart(List<SanteBienEtre> records) {
        lineChartStress.getData().clear();
        if (records == null || records.isEmpty()) {
            System.err.println("[DEBUG] No records to display in chart");
            return;
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Niveau de stress");

        // Sort by date
        records.sort((a, b) -> a.getDateSuivi().compareTo(b.getDateSuivi()));

        for (SanteBienEtre record : records) {
            series.getData().add(new XYChart.Data<>(
                record.getDateSuivi().toString(),
                record.getNiveauStress()
            ));
        }

        lineChartStress.getData().add(series);
        System.err.println("[DEBUG] Chart updated with " + records.size() + " data points");
    }

    /** Fills the form with selected record data */
    private void fillForm(SanteBienEtre s) {
        tfUserId.setText(String.valueOf(s.getUserId()));
        cbHumeur.setValue(s.getHumeur());
        spNiveauStress.getValueFactory().setValue(s.getNiveauStress());
        spQualiteSommeil.getValueFactory().setValue(s.getQualiteSommeil());
        dpDateSuivi.setValue(s.getDateSuivi());
        tfNutrition.setText(s.getNutrition());
        tfActivite.setText(s.getActivitePhysique());
        tfDeveloppement.setText(s.getDeveloppementPersonnel());
        taRecommandations.setText(s.getRecommandations());
    }

    private int getUserId() {
        try {
            return Integer.parseInt(tfUserId.getText().trim());
        } catch (NumberFormatException e) {
            return DEFAULT_USER_ID;
        }
    }

    private String validateFormForSave() {
        StringBuilder errors = new StringBuilder();

        String rawUserId = tfUserId.getText() != null ? tfUserId.getText().trim() : "";
        if (rawUserId.isEmpty()) {
            errors.append("- ID Utilisateur est obligatoire\n");
        } else {
            try {
                int userId = Integer.parseInt(rawUserId);
                if (userId <= 0) errors.append("- ID Utilisateur doit être > 0\n");
            } catch (NumberFormatException e) {
                errors.append("- ID Utilisateur doit être un nombre\n");
            }
        }

        String humeur = cbHumeur.getValue();
        if (humeur == null || humeur.trim().isEmpty()) {
            errors.append("- Humeur est obligatoire\n");
        }

        LocalDate date = dpDateSuivi.getValue();
        if (date == null) {
            errors.append("- Date de suivi est obligatoire\n");
        }

        Integer stress = spNiveauStress.getValue();
        if (stress == null || stress < 1 || stress > 10) {
            errors.append("- Niveau de stress doit être entre 1 et 10\n");
        }

        Integer sommeil = spQualiteSommeil.getValue();
        if (sommeil == null || sommeil < 1 || sommeil > 10) {
            errors.append("- Qualité du sommeil doit être entre 1 et 10\n");
        }

        String nutrition = tfNutrition.getText() != null ? tfNutrition.getText().trim() : "";
        if (nutrition.isEmpty()) {
            errors.append("- Nutrition est obligatoire\n");
        }
        if (nutrition.length() > 255) {
            errors.append("- Nutrition dépasse 255 caractères\n");
        }
        String activite = tfActivite.getText() != null ? tfActivite.getText().trim() : "";
        if (activite.isEmpty()) {
            errors.append("- Activité physique est obligatoire\n");
        }
        if (activite.length() > 255) {
            errors.append("- Activité physique dépasse 255 caractères\n");
        }
        String dev = tfDeveloppement.getText() != null ? tfDeveloppement.getText().trim() : "";
        if (dev.isEmpty()) {
            errors.append("- Développement personnel est obligatoire\n");
        }
        if (dev.length() > 500) {
            errors.append("- Développement personnel dépasse 500 caractères\n");
        }

        return errors.length() == 0 ? null : errors.toString();
    }

    /** Shows preview recommendations based on current form values */
    private void showPreviewRecommendations() {
        SanteBienEtre preview = buildFromForm();
        if (preview != null) {
            taRecommandations.setText(service.generateRecommandations(preview));
        }
    }

    /** Builds entity from form (id may be 0 for new record) */
    private SanteBienEtre buildFromForm() {
        try {
            int userId = getUserId();
            String humeur = cbHumeur.getValue();
            if (humeur == null || humeur.isEmpty()) return null;
            int stress = spNiveauStress.getValue();
            int sommeil = spQualiteSommeil.getValue();
            LocalDate date = dpDateSuivi.getValue() != null ? dpDateSuivi.getValue() : LocalDate.now();
            SanteBienEtre entity = new SanteBienEtre(
                    userId, humeur, stress, sommeil,
                    tfNutrition.getText(), tfActivite.getText(), tfDeveloppement.getText()
            );
            entity.setDateSuivi(date);
            return entity;
        } catch (Exception e) {
            return null;
        }
    }

    @FXML
    private void handleAdd() {
        String validation = validateFormForSave();
        if (validation != null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", validation);
            return;
        }
        SanteBienEtre s = buildFromForm();
        if (s == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez remplir tous les champs obligatoires (Humeur, etc.).");
            return;
        }
        int id = service.addSanteBienEtre(s);
        if (id > 0) {
            refreshCurrentView();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Enregistrement ajouté avec succès.");
            handleClear();
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ajouter l'enregistrement.");
        }
    }

    @FXML
    private void handleUpdate() {
        SanteBienEtre selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez un enregistrement à modifier.");
            return;
        }
        String validation = validateFormForSave();
        if (validation != null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", validation);
            return;
        }
        SanteBienEtre s = buildFromForm();
        if (s == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez remplir tous les champs obligatoires.");
            return;
        }
        s.setId(selected.getId());
        s.setDateSuivi(dpDateSuivi.getValue());
        if (service.updateSanteBienEtre(s)) {
            refreshCurrentView();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Enregistrement modifié avec succès.");
            handleClear();
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de modifier l'enregistrement.");
        }
    }

    @FXML
    private void handleDelete() {
        SanteBienEtre selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez un enregistrement à supprimer.");
            return;
        }
        if (service.deleteSanteBienEtre(selected.getId())) {
            refreshCurrentView();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Enregistrement supprimé avec succès.");
            handleClear();
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer l'enregistrement.");
        }
    }

    @FXML
    private void handleClear() {
        tfUserId.setText(String.valueOf(DEFAULT_USER_ID));
        cbHumeur.setValue(null);
        spNiveauStress.getValueFactory().setValue(5);
        spQualiteSommeil.getValueFactory().setValue(6);
        dpDateSuivi.setValue(LocalDate.now());
        tfNutrition.clear();
        tfActivite.clear();
        tfDeveloppement.clear();
        taRecommandations.clear();
        tableView.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleFilter() {
        String rawUserId = tfUserId.getText() != null ? tfUserId.getText().trim() : "";
        if (rawUserId.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention", "ID Utilisateur est obligatoire pour filtrer.");
            return;
        }
        try {
            int userId = Integer.parseInt(rawUserId);
            if (userId <= 0) {
                showAlert(Alert.AlertType.WARNING, "Attention", "ID Utilisateur doit être > 0.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Attention", "ID Utilisateur doit être un nombre.");
            return;
        }
        LocalDate start = dpDateDebut.getValue();
        LocalDate end = dpDateFin.getValue();
        if (start == null || end == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez les dates de début et fin.");
            return;
        }
        if (start.isAfter(end)) {
            showAlert(Alert.AlertType.WARNING, "Attention", "La date de début doit être avant la date de fin.");
            return;
        }
        loadFilteredData(start, end);
    }

    @FXML
    private void handleShowAll() {
        loadData();
        dpDateDebut.setValue(null);
        dpDateFin.setValue(null);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleOpenAIDashboard() {
        System.out.println("[DEBUG] Dashboard IA button clicked!");
        try {
            System.out.println("[DEBUG] Loading AIDashboard.fxml...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AIDashboard.fxml"));
            Parent root = loader.load();
            System.out.println("[DEBUG] FXML loaded successfully!");
            
            Stage stage = new Stage();
            stage.setTitle("🤖 Dashboard IA Avancé - GrowMind");
            stage.setScene(new Scene(root));
            stage.setMinWidth(1200);
            stage.setMinHeight(800);
            stage.show();
            System.out.println("[DEBUG] Dashboard IA window opened!");
        } catch (IOException | RuntimeException e) {
            System.err.println("[ERROR] Failed to open Dashboard IA: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le Dashboard IA.\n" + e.getMessage());
        }
    }
    
    @FXML
    private void handleOpenSommeil() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/GestionSommeil.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("🌙 Gestion Sommeil - GrowMind");
            stage.setScene(new Scene(root));
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            stage.show();
        } catch (IOException | RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir l'écran Sommeil.");
        }
    }
    
    @FXML
    private void handleOpenWellness() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AnonymousWellness.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("🧠 Évaluation Bien-être - GrowMind");
            stage.setScene(new Scene(root));
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            stage.show();
        } catch (IOException | RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir l'écran d'évaluation de bien-être.");
        }
    }
    
    @FXML
    private void handleOpenChatbot() {
        try {
            URL fxmlUrl = getClass().getResource("/fxml/WellnessChatbot.fxml");
            if (fxmlUrl == null) {
                throw new IOException("Ressource /fxml/WellnessChatbot.fxml introuvable dans classpath");
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("🤖 Assistant Bien-être IA - GrowMind");
            stage.setScene(new Scene(root));
            stage.setMinWidth(900);
            stage.setMinHeight(700);
            stage.show();
        } catch (IOException | RuntimeException e) {
            System.err.println("[ERROR] Failed to open chatbot: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("[ERROR] Root cause: " + e.getCause().getClass().getName() + ": " + e.getCause().getMessage());
            }
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le chatbot IA.\n" + e.getMessage());
        }
    }
    
    /** Load GrowMind logo */
    private void loadLogo() {
        try {
            // Force reload by adding timestamp to URL
            Image logo = new Image(getClass().getResourceAsStream("/images/logo.jpeg"));
            if (logo.isError()) {
                System.err.println("[DEBUG] Failed to load logo image");
            } else {
                logoImageView.setImage(logo);
                logoImageView.setFitWidth(600);
                logoImageView.setFitHeight(140);
                logoImageView.setPreserveRatio(true);
                System.err.println("[DEBUG] GrowMind logo loaded successfully with size 600x140");
            }
        } catch (Exception e) {
            System.err.println("[DEBUG] Exception loading logo: " + e.getMessage());
        }
    }
}
