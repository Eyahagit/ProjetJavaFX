package Controllers;

import Models.RendezVous;
import Service.ServiceRendezVous;
import Service.SmartReminderService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SuiviRappelsController {

    @FXML private TableView<RendezVous> tableView;
    @FXML private TableColumn<RendezVous, Integer> colId;
    @FXML private TableColumn<RendezVous, String> colPatient;
    @FXML private TableColumn<RendezVous, String> colDate;
    @FXML private TableColumn<RendezVous, String> colHeure;
    @FXML private TableColumn<RendezVous, String> colStatut;
    @FXML private TableColumn<RendezVous, String> colPsychologue;
    @FXML private TableColumn<RendezVous, Boolean> colRappel;

    @FXML private Label lblTotalRdv;
    @FXML private Label lblRappelsEnvoyes;
    @FXML private Label lblTauxOuverture;
    @FXML private Label lblTauxAbsenteisme;
    @FXML private Label lblDateDuJour;

    @FXML private PieChart pieChartStatuts;
    @FXML private BarChart<String, Number> barChartRappels;
    @FXML private LineChart<String, Number> lineChartTendance;

    @FXML private ComboBox<String> comboFiltrePeriode;

    private ServiceRendezVous serviceRdv;
    private SmartReminderService reminderService;
    private ObservableList<RendezVous> rendezVousList;

    @FXML
    public void initialize() {
        serviceRdv = new ServiceRendezVous();
        reminderService = new SmartReminderService();
        rendezVousList = FXCollections.observableArrayList();

        setupTable();
        setupCharts();
        setupFilters();
        loadData();

        lblDateDuJour.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idRdv"));

        colPatient.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getNomCompletPatient()
                )
        );

        colDate.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        new java.text.SimpleDateFormat("dd/MM/yyyy").format(cellData.getValue().getDateRdv())
                )
        );

        colHeure.setCellValueFactory(new PropertyValueFactory<>("heure"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        colPsychologue.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getNomPsychologue() + " " +
                                cellData.getValue().getPrenomPsychologue()
                )
        );

        colRappel.setCellValueFactory(new PropertyValueFactory<>("rappelEnvoye"));
        colRappel.setCellFactory(col -> new TableCell<RendezVous, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "✅ Oui" : "❌ Non");
                }
            }
        });

        tableView.setItems(rendezVousList);
    }

    private void setupCharts() {
        pieChartStatuts.setTitle("Statut des rendez-vous");
        barChartRappels.setTitle("Rappels envoyés par jour");
        barChartRappels.setAnimated(false);
        lineChartTendance.setTitle("Évolution de l'absentéisme");
        lineChartTendance.setAnimated(false);
    }

    private void setupFilters() {
        comboFiltrePeriode.setItems(FXCollections.observableArrayList(
                "Cette semaine", "Ce mois", "3 derniers mois", "Cette année", "Tout"
        ));
        comboFiltrePeriode.setValue("Ce mois");
        comboFiltrePeriode.setOnAction(e -> loadData());
    }

    private void loadData() {
        rendezVousList.clear();
        rendezVousList.addAll(serviceRdv.recupererAvecDetails());

        // Filtrer selon la période
        String periode = comboFiltrePeriode.getValue();
        if (!"Tout".equals(periode)) {
            rendezVousList = filterByPeriode(rendezVousList, periode);
        }

        tableView.setItems(rendezVousList);
        updateStats();
        updateCharts();
    }

    private ObservableList<RendezVous> filterByPeriode(ObservableList<RendezVous> list, String periode) {
        LocalDate now = LocalDate.now();
        LocalDate debut;

        switch (periode) {
            case "Cette semaine":
                debut = now.minusDays(7);
                break;
            case "Ce mois":
                debut = now.minusDays(30);
                break;
            case "3 derniers mois":
                debut = now.minusDays(90);
                break;
            case "Cette année":
                debut = now.minusDays(365);
                break;
            default:
                return list;
        }

        ObservableList<RendezVous> filtered = FXCollections.observableArrayList();
        for (RendezVous r : list) {
            try {
                // Vérification que la date n'est pas null
                if (r.getDateRdv() == null) {
                    continue;
                }

                // Conversion en LocalDate
                LocalDate rdvDate = new java.sql.Date(r.getDateRdv().getTime()).toLocalDate();
                if (rdvDate.isAfter(debut) || rdvDate.isEqual(debut)) {
                    filtered.add(r);
                }
            } catch (Exception e) {
                System.err.println("Erreur de conversion de date pour le rendez-vous ID: " + r.getIdRdv());
                e.printStackTrace();
            }
        }
        return filtered;
    }

    private void updateStats() {
        try {
            Map<String, Object> stats = reminderService.getGlobalStats();

            lblTotalRdv.setText(String.valueOf(stats.get("total")));
            lblRappelsEnvoyes.setText(String.valueOf(stats.get("rappelsEnvoyes")));

            long total = (long) stats.get("total");
            long confirmes = (long) stats.get("confirmes");
            long annules = (long) stats.get("annules");

            if (total > 0) {
                lblTauxOuverture.setText(String.format("%.1f%%", (double) confirmes / total * 100));
                lblTauxAbsenteisme.setText(String.format("%.1f%%", (double) annules / total * 100));
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour des stats: " + e.getMessage());
        }
    }

    private void updateCharts() {
        try {
            // Pie Chart
            Map<String, Object> stats = reminderService.getGlobalStats();
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                    new PieChart.Data("Confirmés", (long) stats.get("confirmes")),
                    new PieChart.Data("Annulés", (long) stats.get("annules")),
                    new PieChart.Data("En attente", (long) stats.get("enAttente")),
                    new PieChart.Data("Terminés", (long) stats.get("termines"))
            );
            pieChartStatuts.setData(pieData);

            // Bar Chart des rappels par jour
            XYChart.Series<String, Number> rappelsSeries = new XYChart.Series<>();
            rappelsSeries.setName("Rappels envoyés");

            Map<String, Integer> rappelsParJour = new HashMap<>();
            for (RendezVous r : rendezVousList) {
                if (r.isRappelEnvoye() && r.getDateRappel() != null) {
                    String jour = new java.text.SimpleDateFormat("dd/MM").format(r.getDateRappel());
                    rappelsParJour.put(jour, rappelsParJour.getOrDefault(jour, 0) + 1);
                }
            }

            rappelsParJour.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(e -> rappelsSeries.getData().add(new XYChart.Data<>(e.getKey(), e.getValue())));

            barChartRappels.getData().clear();
            if (!rappelsSeries.getData().isEmpty()) {
                barChartRappels.getData().add(rappelsSeries);
            }

            // Line Chart tendance absentéisme
            XYChart.Series<String, Number> tendanceSeries = new XYChart.Series<>();
            tendanceSeries.setName("Taux d'absentéisme (%)");

            // Simuler des données pour les 5 dernières semaines
            Random rand = new Random();
            for (int i = 4; i >= 0; i--) {
                LocalDate semaineDate = LocalDate.now().minusWeeks(i);
                String semaineLabel = semaineDate.format(DateTimeFormatter.ofPattern("dd/MM"));
                // Valeur simulée entre 5% et 30%
                double valeur = 10 + rand.nextDouble() * 20;
                tendanceSeries.getData().add(new XYChart.Data<>(semaineLabel, valeur));
            }

            lineChartTendance.getData().clear();
            lineChartTendance.getData().add(tendanceSeries);

        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour des graphiques: " + e.getMessage());
        }
    }

    @FXML
    private void handleEnvoyerRappel() {
        RendezVous selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Sélection", "Veuillez sélectionner un rendez-vous");
            return;
        }

        try {
            reminderService.scheduleSmartReminder(selected);
            showAlert("Succès", "Rappel intelligent programmé !");
            loadData();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'envoi du rappel: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleActualiser() {
        loadData();
        showAlert("Info", "Données actualisées");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}