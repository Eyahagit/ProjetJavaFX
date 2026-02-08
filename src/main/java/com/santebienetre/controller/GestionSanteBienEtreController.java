package com.santebienetre.controller;

import com.santebienetre.model.SanteBienEtre;
import com.santebienetre.service.SanteBienEtreService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

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
    @FXML private TextField tfActivitePhysique;
    @FXML private TextField tfDeveloppementPersonnel;
    @FXML private TextArea taRecommandations;
    @FXML private TableView<SanteBienEtre> tableView;
    @FXML private TableColumn<SanteBienEtre, Integer> colId;
    @FXML private TableColumn<SanteBienEtre, Integer> colUserId;
    @FXML private TableColumn<SanteBienEtre, String> colHumeur;
    @FXML private TableColumn<SanteBienEtre, Integer> colStress;
    @FXML private TableColumn<SanteBienEtre, Integer> colSommeil;
    @FXML private TableColumn<SanteBienEtre, String> colNutrition;
    @FXML private TableColumn<SanteBienEtre, String> colActivite;
    @FXML private TableColumn<SanteBienEtre, LocalDate> colDate;
    @FXML private DatePicker dpDateDebut;
    @FXML private DatePicker dpDateFin;
    @FXML private LineChart<String, Number> lineChartStress;

    private final SanteBienEtreService service = new SanteBienEtreService();
    private final ObservableList<SanteBienEtre> dataList = FXCollections.observableArrayList();

    /** Default user ID for demo (can be changed by user) */
    private static final int DEFAULT_USER_ID = 1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateSuivi"));

        tableView.setItems(dataList);

        // Load table selection into form for update
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                fillForm(newVal);
            }
        });

        // Update recommendations preview when key fields change
        cbHumeur.valueProperty().addListener((o, a, b) -> showPreviewRecommendations());
        spNiveauStress.valueProperty().addListener((o, a, b) -> showPreviewRecommendations());
        spQualiteSommeil.valueProperty().addListener((o, a, b) -> showPreviewRecommendations());

        // Load initial data and recommendations preview
        loadData();
        showPreviewRecommendations();
    }

    /** Loads all records into TableView */
    private void loadData() {
        dataList.clear();
        List<SanteBienEtre> list = service.getAllSanteBienEtre();
        dataList.addAll(list);
        updateStressChart(list);
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
    private void updateStressChart(List<SanteBienEtre> list) {
        lineChartStress.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Niveau de stress");
        for (SanteBienEtre s : list) {
            String dateStr = s.getDateSuivi() != null ? s.getDateSuivi().toString() : "";
            series.getData().add(new XYChart.Data<>(dateStr, s.getNiveauStress()));
        }
        lineChartStress.getData().add(series);
    }

    /** Fills the form with selected record data */
    private void fillForm(SanteBienEtre s) {
        tfUserId.setText(String.valueOf(s.getUserId()));
        cbHumeur.setValue(s.getHumeur());
        spNiveauStress.getValueFactory().setValue(s.getNiveauStress());
        spQualiteSommeil.getValueFactory().setValue(s.getQualiteSommeil());
        dpDateSuivi.setValue(s.getDateSuivi());
        tfNutrition.setText(s.getNutrition());
        tfActivitePhysique.setText(s.getActivitePhysique());
        tfDeveloppementPersonnel.setText(s.getDeveloppementPersonnel());
        taRecommandations.setText(s.getRecommandations());
    }

    private int getUserId() {
        try {
            return Integer.parseInt(tfUserId.getText().trim());
        } catch (NumberFormatException e) {
            return DEFAULT_USER_ID;
        }
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
                    tfNutrition.getText(), tfActivitePhysique.getText(), tfDeveloppementPersonnel.getText()
            );
            entity.setDateSuivi(date);
            return entity;
        } catch (Exception e) {
            return null;
        }
    }

    @FXML
    private void handleAdd() {
        SanteBienEtre s = buildFromForm();
        if (s == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez remplir tous les champs obligatoires (Humeur, etc.).");
            return;
        }
        int id = service.addSanteBienEtre(s);
        if (id > 0) {
            s.setId(id);
            s.setDateSuivi(dpDateSuivi.getValue());
            s.setRecommandations(service.generateRecommandations(s));
            dataList.add(0, s);
            updateStressChart(dataList);
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
        SanteBienEtre s = buildFromForm();
        if (s == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez remplir tous les champs obligatoires.");
            return;
        }
        s.setId(selected.getId());
        s.setDateSuivi(dpDateSuivi.getValue());
        if (service.updateSanteBienEtre(s)) {
            int idx = dataList.indexOf(selected);
            s.setRecommandations(service.generateRecommandations(s));
            dataList.set(idx, s);
            updateStressChart(dataList);
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
            dataList.remove(selected);
            updateStressChart(dataList);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Enregistrement supprimé.");
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
        tfActivitePhysique.clear();
        tfDeveloppementPersonnel.clear();
        taRecommandations.clear();
        tableView.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleFilter() {
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
}
