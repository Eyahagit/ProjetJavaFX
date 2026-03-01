package Controllers;

import Models.SanteBienEtre;
import Models.users;
import Services.SanteBienEtreService;
//import com.sun.javafx.tk.quantum.PaintRenderJob;
import utils.SessionManager;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.logging.Logger;
import java.util.logging.Level;

public class GestionSanteBienEtreController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(GestionSanteBienEtreController.class.getName());

    // Constantes
    private static final int DEFAULT_USER_ID = 1;
    private static final int STRESS_MIN = 1;
    private static final int STRESS_MAX = 10;
    private static final int SOMMEIL_MIN = 1;
    private static final int SOMMEIL_MAX = 10;
    private static final int NUTRITION_MAX_LENGTH = 255;
    private static final int ACTIVITE_MAX_LENGTH = 255;
    private static final int DEVELOPPEMENT_MAX_LENGTH = 500;

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
    @FXML private Label lblCount;
    @FXML private Label lblUserWelcome;
    @FXML private Label lblUserRole;
    @FXML private Label lblUserName;
    @FXML private Label lblDate;
    @FXML private Button btnHome; // Ajoutez cette ligne avec les autres @FXML

    private final SanteBienEtreService service = new SanteBienEtreService();
    private final ObservableList<SanteBienEtre> dataList = FXCollections.observableArrayList();
    private int currentPageUserId = DEFAULT_USER_ID;
    private boolean showAllMode = false;
    private users currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("=== Initialisation GestionSanteBienEtreController ===");

        // Afficher la date du jour
        updateCurrentDate();

        // Récupérer l'utilisateur connecté depuis la session
        currentUser = SessionManager.getInstance().getCurrentUser();

        // Configurer l'affichage utilisateur
        configureUserDisplay();

        // Initialiser les contrôles
        setupControls();

        // Charger le logo
        loadLogo();

        LOGGER.info("=== Initialisation terminée ===\n");
    }

    private void updateCurrentDate() {
        if (lblDate != null) {
            lblDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }
    }

    private void configureUserDisplay() {
        // Récupérer l'utilisateur depuis la session si pas déjà fait
        if (currentUser == null) {
            currentUser = SessionManager.getInstance().getCurrentUser();
        }

        if (currentUser != null) {
            String fullName = currentUser.getName() + " " + currentUser.getSecond_name();
            String role = currentUser.getRole();

            // Message de bienvenue personnalisé avec le nom complet
            if (lblUserWelcome != null) {
                lblUserWelcome.setText("Bienvenue, " + fullName + " !");
            }

            // Nom d'utilisateur complet
            if (lblUserName != null) {
                lblUserName.setText(fullName);
            }

            // Rôle avec style et icône
            if (lblUserRole != null) {
                String roleText = getRoleDisplay(role);
                lblUserRole.setText(roleText);
                String color = getRoleColor(role);
                lblUserRole.setStyle("-fx-background-color: " + color + "; -fx-padding: 5 15; -fx-background-radius: 20; -fx-text-fill: white; -fx-font-weight: bold;");
            }

            // Pré-remplir l'ID utilisateur avec l'ID de l'utilisateur connecté
            if (tfUserId != null) {
                tfUserId.setText(String.valueOf(currentUser.getId()));
            }

            LOGGER.info("✅ Utilisateur connecté: " + fullName + " (" + role + ")");
        } else {
            // Cas où aucun utilisateur n'est connecté (mode invité)
            if (lblUserWelcome != null) {
                lblUserWelcome.setText("Bienvenue sur GrowMind");
            }
            if (lblUserName != null) {
                lblUserName.setText("Visiteur");
            }
            if (lblUserRole != null) {
                lblUserRole.setText("👤 Invité");
                lblUserRole.setStyle("-fx-background-color: #95a5a6; -fx-padding: 5 15; -fx-background-radius: 20; -fx-text-fill: white; -fx-font-weight: bold;");
            }
            if (tfUserId != null) {
                tfUserId.setText(String.valueOf(DEFAULT_USER_ID));
            }

            LOGGER.info("⚠️ Aucun utilisateur connecté - Mode invité");
        }
    }

    public void setCurrentUser(users user) {
        if (user != null) {
            this.currentUser = user;
            SessionManager.getInstance().setCurrentUser(user);
            configureUserDisplay(); // Mettre à jour l'affichage immédiatement
            LOGGER.info("✅ Utilisateur défini: " + user.getName() + " " + user.getSecond_name());
        }
    }

    public void setUser(users user) {
        setCurrentUser(user);
    }

    private String getRoleDisplay(String role) {
        if (role == null) return "👤 Invité";
        switch(role.toLowerCase()) {
            case "admin": return "👑 Administrateur";
            case "doctor": return "👨‍⚕️ Médecin";
            case "patient": return "👤 Patient";
            default: return "👤 Utilisateur";
        }
    }

    private String getRoleColor(String role) {
        if (role == null) return "#95a5a6";
        switch(role.toLowerCase()) {
            case "admin": return "#9b59b6";
            case "doctor": return "#3498db";
            case "patient": return "#2ecc71";
            default: return "#95a5a6";
        }
    }

    private void setupControls() {
        // Filtre pour les chiffres
        setupUserIdField();

        // Spinners
        setupSpinners();

        // Limites de caractères
        setupTextLimits();

        // ComboBox humeur
        setupHumeurComboBox();

        dpDateSuivi.setValue(LocalDate.now());

        // Configuration des colonnes du tableau
        setupTableColumns();

        // Charger les données
        refreshData();

        // Listener de sélection
        setupSelectionListener();

        // Checkbox tout afficher
        setupShowAllCheckbox();

        // Pagination
        setupPagination();

        // Preview des recommandations
        setupRecommendationsListeners();

        showPreviewRecommendations();
    }

    private void setupUserIdField() {
        Pattern digitsOnly = Pattern.compile("\\d*");
        UnaryOperator<TextFormatter.Change> digitsFilter = change -> {
            String text = change.getControlNewText();
            return digitsOnly.matcher(text).matches() ? change : null;
        };
        tfUserId.setTextFormatter(new TextFormatter<>(digitsFilter));
    }

    private void setupSpinners() {
        spNiveauStress.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(STRESS_MIN, STRESS_MAX, 5));
        spQualiteSommeil.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(SOMMEIL_MIN, SOMMEIL_MAX, 6));
        spNiveauStress.setEditable(true);
        spQualiteSommeil.setEditable(true);
    }

    private void setupTextLimits() {
        tfNutrition.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().length() <= NUTRITION_MAX_LENGTH ? change : null));
        tfActivite.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().length() <= ACTIVITE_MAX_LENGTH ? change : null));
        tfDeveloppement.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().length() <= DEVELOPPEMENT_MAX_LENGTH ? change : null));
    }

    private void setupHumeurComboBox() {
        cbHumeur.setItems(FXCollections.observableArrayList(
                "Très bien", "Bien", "Moyen", "Stressé", "Fatigué"
        ));
    }

    private void setupTableColumns() {
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

        // Formatage de la date
        colDate.setCellFactory(column -> new TableCell<SanteBienEtre, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                }
            }
        });

        tableView.setItems(dataList);
        tableView.setPlaceholder(new Label("Aucune donnée disponible"));
    }

    private void setupSelectionListener() {
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) fillForm(newVal);
        });
    }

    private void setupShowAllCheckbox() {
        chkShowAll.setOnAction(e -> {
            showAllMode = chkShowAll.isSelected();
            refreshData();
        });
    }

    private void setupRecommendationsListeners() {
        cbHumeur.valueProperty().addListener((o, a, b) -> showPreviewRecommendations());
        spNiveauStress.valueProperty().addListener((o, a, b) -> showPreviewRecommendations());
        spQualiteSommeil.valueProperty().addListener((o, a, b) -> showPreviewRecommendations());
    }

    private void refreshData() {
        dataList.clear();
        List<SanteBienEtre> list;

        if (showAllMode) {
            list = service.getAllSanteBienEtre();
        } else {
            int userId = getUserId();
            list = service.getSanteBienEtreByUser(userId);
        }

        dataList.addAll(list);
        tableView.setItems(dataList);
        tableView.refresh();

        if (lblCount != null) {
            lblCount.setText(dataList.size() + " enregistrement(s)");
        }

        LOGGER.info("✅ Données chargées: " + dataList.size() + " enregistrements");
        updateStressChart(list);
    }

    private void setupPagination() {
        pagination.setPageCount(1);
        pagination.setCurrentPageIndex(0);
    }

    private void updateStressChart(List<SanteBienEtre> records) {
        lineChartStress.getData().clear();
        if (records == null || records.isEmpty()) return;

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Niveau de stress");

        records.sort((a, b) -> a.getDateSuivi().compareTo(b.getDateSuivi()));

        for (SanteBienEtre record : records) {
            series.getData().add(new XYChart.Data<>(
                    record.getDateSuivi().format(DateTimeFormatter.ofPattern("dd/MM")),
                    record.getNiveauStress()
            ));
        }

        lineChartStress.getData().add(series);
    }

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

        // Validation User ID
        String rawUserId = tfUserId.getText();
        if (rawUserId == null || rawUserId.trim().isEmpty()) {
            errors.append("- ID Utilisateur est obligatoire\n");
        } else {
            try {
                int userId = Integer.parseInt(rawUserId);
                if (userId <= 0) errors.append("- ID Utilisateur doit être > 0\n");
            } catch (NumberFormatException e) {
                errors.append("- ID Utilisateur doit être un nombre\n");
            }
        }

        // Validation Humeur
        if (cbHumeur.getValue() == null) {
            errors.append("- Humeur est obligatoire\n");
        }

        // Validation Date
        if (dpDateSuivi.getValue() == null) {
            errors.append("- Date de suivi est obligatoire\n");
        }

        // Validation Stress
        Integer stress = spNiveauStress.getValue();
        if (stress == null || stress < STRESS_MIN || stress > STRESS_MAX) {
            errors.append("- Niveau de stress doit être entre " + STRESS_MIN + " et " + STRESS_MAX + "\n");
        }

        // Validation Sommeil
        Integer sommeil = spQualiteSommeil.getValue();
        if (sommeil == null || sommeil < SOMMEIL_MIN || sommeil > SOMMEIL_MAX) {
            errors.append("- Qualité du sommeil doit être entre " + SOMMEIL_MIN + " et " + SOMMEIL_MAX + "\n");
        }

        // Validation Nutrition
        String nutrition = tfNutrition.getText();
        if (nutrition == null || nutrition.trim().isEmpty()) {
            errors.append("- Nutrition est obligatoire\n");
        }

        // Validation Activité
        String activite = tfActivite.getText();
        if (activite == null || activite.trim().isEmpty()) {
            errors.append("- Activité physique est obligatoire\n");
        }

        // Validation Développement
        String dev = tfDeveloppement.getText();
        if (dev == null || dev.trim().isEmpty()) {
            errors.append("- Développement personnel est obligatoire\n");
        }

        return errors.length() == 0 ? null : errors.toString();
    }

    private void showPreviewRecommendations() {
        SanteBienEtre preview = buildFromForm();
        if (preview != null) {
            taRecommandations.setText(service.generateRecommandations(preview));
        }
    }

    private SanteBienEtre buildFromForm() {
        try {
            int userId = getUserId();
            String humeur = cbHumeur.getValue();
            if (humeur == null) return null;

            int stress = spNiveauStress.getValue();
            int sommeil = spQualiteSommeil.getValue();
            LocalDate date = dpDateSuivi.getValue() != null ? dpDateSuivi.getValue() : LocalDate.now();

            // Création de l'entité avec les valeurs du formulaire
            SanteBienEtre entity = new SanteBienEtre();

            entity.setUserId(userId);
            entity.setHumeur(humeur);
            entity.setNiveauStress(stress);
            entity.setQualiteSommeil(sommeil);
            entity.setNutrition(tfNutrition.getText());
            entity.setActivitePhysique(tfActivite.getText());
            entity.setDeveloppementPersonnel(tfDeveloppement.getText());
            entity.setDateSuivi(date);

            return entity;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erreur buildFromForm", e);
            return null;
        }
    }

    @FXML
    private void handleAdd() {
        String validation = validateFormForSave();
        if (validation != null) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", validation);
            return;
        }

        SanteBienEtre s = buildFromForm();
        if (s == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de créer l'entité");
            return;
        }

        int id = service.addSanteBienEtre(s);
        if (id > 0) {
            s.setId(id);
            dataList.add(0, s);
            tableView.refresh();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Enregistrement ajouté avec succès (ID: " + id + ")");
            handleClear();
            refreshData();
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ajouter l'enregistrement");
        }
    }

    @FXML
    private void handleUpdate() {
        SanteBienEtre selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez un enregistrement à modifier");
            return;
        }

        String validation = validateFormForSave();
        if (validation != null) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", validation);
            return;
        }

        SanteBienEtre s = buildFromForm();
        if (s == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de créer l'entité");
            return;
        }

        s.setId(selected.getId());
        s.setRecommandations(taRecommandations.getText());

        if (service.updateSanteBienEtre(s)) {
            int index = dataList.indexOf(selected);
            dataList.set(index, s);
            tableView.refresh();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Enregistrement modifié avec succès");
            handleClear();
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de modifier l'enregistrement");
        }
    }

    @FXML
    private void handleDelete() {
        SanteBienEtre selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez un enregistrement à supprimer");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer cet enregistrement ?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (service.deleteSanteBienEtre(selected.getId())) {
                dataList.remove(selected);
                tableView.refresh();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Enregistrement supprimé");
                handleClear();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer l'enregistrement");
            }
        }
    }

    @FXML
    private void handleClear() {
        if (currentUser != null) {
            tfUserId.setText(String.valueOf(currentUser.getId()));
        } else {
            tfUserId.setText(String.valueOf(DEFAULT_USER_ID));
        }
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
        String rawUserId = tfUserId.getText();
        if (rawUserId == null || rawUserId.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention", "ID Utilisateur est obligatoire pour filtrer");
            return;
        }

        LocalDate start = dpDateDebut.getValue();
        LocalDate end = dpDateFin.getValue();

        if (start == null || end == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez les dates de début et fin");
            return;
        }

        if (start.isAfter(end)) {
            showAlert(Alert.AlertType.WARNING, "Attention", "La date de début doit être avant la date de fin");
            return;
        }

        dataList.clear();
        int userId = getUserId();
        List<SanteBienEtre> list = service.getSanteBienEtreByDateRange(userId, start, end);
        dataList.addAll(list);
        tableView.refresh();
        updateStressChart(list);
    }

    @FXML
    private void handleShowAll() {
        refreshData();
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
        openNewWindow("/AIDashboard.fxml", "🤖 Dashboard IA Avancé - GrowMind", 1200, 800);
    }

    @FXML
    private void handleOpenSommeil() {
        openNewWindow("/GestionSommeil.fxml", "🌙 Gestion Sommeil - GrowMind", 800, 600);
    }

    @FXML
    private void handleOpenWellness() {
        openNewWindow("/AnonymousWellness.fxml", "🧠 Évaluation Bien-être - GrowMind", 800, 600);
    }

    @FXML
    private void handleOpenChatbot() {
        openNewWindow("/WellnessChatbot.fxml", "🤖 Assistant Bien-être IA - GrowMind", 900, 700);
    }

    private void openNewWindow(String fxmlPath, String title, int width, int height) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                throw new IOException("Fichier " + fxmlPath + " introuvable");
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root, width, height));
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur ouverture fenêtre: " + fxmlPath, e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir: " + e.getMessage());
        }
    }
    @FXML
    private void handleHome() {
        System.out.println("\n=== Retour à l'accueil depuis Gestion Santé & Bien-être ===");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/home.fxml"));
            Parent root = loader.load();

            // Passer l'utilisateur connecté au HomeController
            HomeController homeController = loader.getController();
            if (homeController != null && currentUser != null) {
                homeController.setUser(currentUser);
                System.out.println("✅ Utilisateur transmis au HomeController: " + currentUser.getName());
            }

            Stage stage = (Stage) btnHome.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Accueil - GrowMind");
            stage.show();

            System.out.println("✅ Retour à l'accueil réussi");

        } catch (IOException e) {
            System.err.println("❌ Erreur retour accueil: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de retourner à l'accueil.\n" + e.getMessage());
        }
    }

    private void loadLogo() {
        try {
            Image logo = new Image(getClass().getResourceAsStream("/images/logo.jpeg"));
            if (!logo.isError()) {
                logoImageView.setImage(logo);
                logoImageView.setFitWidth(600);
                logoImageView.setFitHeight(140);
                logoImageView.setPreserveRatio(true);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erreur chargement logo", e);
        }
    }
}