package org.example.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.Services.StatService;

import java.io.IOException;
import java.util.Map;

public class StatsController {

    @FXML
    private VBox chartsContainer;

    private final StatService statService = new StatService();
    private Runnable onBack;

    /** When stats are shown in the right panel, call this so "Retour" goes back to list instead of loading main. */
    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }

    @FXML
    public void initialize() {
        buildCharts();
    }

    private void buildCharts() {
        chartsContainer.getChildren().clear();

        // ----- 1) PIE: Ressources par type (no user data) -----
        Map<String, Number> byType = statService.getRessourcesByType();
        PieChart pie = new PieChart();
        pie.setTitle("Répartition par type de ressource");
        pie.setLegendSide(javafx.geometry.Side.RIGHT);
        pie.setPrefSize(600, 320);
        for (Map.Entry<String, Number> e : byType.entrySet()) {
            String label = e.getKey() == null || e.getKey().isEmpty() ? "(sans type)" : e.getKey();
            PieChart.Data slice = new PieChart.Data(label + " (" + e.getValue().intValue() + ")", e.getValue().intValue());
            pie.getData().add(slice);
        }
        wrapInCard("Répartition des ressources par type de contenu (formation, article, vidéo, événement…)", pie);

        // ----- 2) BAR: Ressources par catégorie – chaque barre a son nom -----
        Map<String, Number> byCat = statService.getRessourcesByCategory();
        CategoryAxis xAxisBar = new CategoryAxis();
        xAxisBar.setLabel("Catégorie");
        NumberAxis yAxisBar = new NumberAxis();
        yAxisBar.setLabel("Nombre de ressources");
        BarChart<String, Number> bar = new BarChart<>(xAxisBar, yAxisBar);
        bar.setTitle("Nombre de ressources par catégorie");
        bar.setLegendVisible(false);
        bar.setPrefSize(600, 320);
        bar.setCategoryGap(25);
        XYChart.Series<String, Number> barSeries = new XYChart.Series<>();
        barSeries.setName("Ressources");
        for (Map.Entry<String, Number> e : byCat.entrySet()) {
            String cat = e.getKey() == null || e.getKey().isEmpty() ? "(sans catégorie)" : e.getKey();
            barSeries.getData().add(new XYChart.Data<>(cat, e.getValue()));
        }
        bar.getData().add(barSeries);
        wrapInCard("Nombre de ressources par catégorie (santé, bien-être, développement personnel…)", bar);

        // ----- 3) LINE: Évaluations dans le temps -----
        Map<String, Number> byMonth = statService.getEvaluationsByMonth();
        CategoryAxis xAxisLine = new CategoryAxis();
        xAxisLine.setLabel("Mois (année-mois)");
        NumberAxis yAxisLine = new NumberAxis();
        yAxisLine.setLabel("Nombre d'évaluations");
        LineChart<String, Number> line = new LineChart<>(xAxisLine, yAxisLine);
        line.setTitle("Évolution des évaluations dans le temps");
        line.setLegendVisible(false);
        line.setPrefSize(600, 320);
        line.setCreateSymbols(true);
        XYChart.Series<String, Number> lineSeries = new XYChart.Series<>();
        lineSeries.setName("Évaluations");
        for (Map.Entry<String, Number> e : byMonth.entrySet())
            lineSeries.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
        line.getData().add(lineSeries);
        wrapInCard("Évolution du nombre d'évaluations mois par mois", line);
    }

    private void wrapInCard(String cardTitle, javafx.scene.Node chart) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: rgba(255,255,255,0.98); -fx-background-radius: 15; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0, 0, 4); -fx-border-color: #4A6FA5; -fx-border-radius: 15; -fx-border-width: 1;");
        javafx.scene.control.Label title = new javafx.scene.control.Label(cardTitle);
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        title.setWrapText(true);
        card.getChildren().addAll(title, chart);
        chartsContainer.getChildren().add(card);
    }

    @FXML
    private void handleBack() {
        if (onBack != null) {
            onBack.run();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) chartsContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("GrowMind - Admin Dashboard");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
