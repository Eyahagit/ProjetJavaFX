package org.example.Controllers;

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
<<<<<<< HEAD
import java.util.List;
=======
>>>>>>> 7d8812e180519f847fbc6852e6afbef657ee5ca4

public class StatsController {

    @FXML
    private VBox chartsContainer;

    private final StatService statService = new StatService();
    private Runnable onBack;

<<<<<<< HEAD
    /**
     * When stats are shown in the right panel, call this so "Retour" goes back to
     * list instead of loading main.
     */
=======
    /** When stats are shown in the right panel, call this so "Retour" goes back to list instead of loading main. */
>>>>>>> 7d8812e180519f847fbc6852e6afbef657ee5ca4
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
<<<<<<< HEAD
            PieChart.Data slice = new PieChart.Data(label + " (" + e.getValue().intValue() + ")",
                    e.getValue().intValue());
=======
            PieChart.Data slice = new PieChart.Data(label + " (" + e.getValue().intValue() + ")", e.getValue().intValue());
>>>>>>> 7d8812e180519f847fbc6852e6afbef657ee5ca4
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
<<<<<<< HEAD

        // ----- 4) AREA: Moyenne d'évaluation par ressource -----
        List<Map.Entry<String, Number>> topAverages = statService.getAverageEvaluationPerRessource(10);
        CategoryAxis xAxisAvg = new CategoryAxis();
        xAxisAvg.setLabel("Ressource");
        xAxisAvg.setAnimated(false);
        // Rotate labels so long titles don't overlap as easily
        xAxisAvg.setTickLabelRotation(45);

        NumberAxis yAxisAvg = new NumberAxis(0, 5, 1);
        yAxisAvg.setLabel("Moyenne (sur 5)");
        yAxisAvg.setAnimated(false);

        AreaChart<String, Number> areaAvg = new AreaChart<>(xAxisAvg, yAxisAvg);
        areaAvg.setTitle("Moyenne des évaluations par ressource (Top 10)");
        areaAvg.setLegendVisible(false);
        areaAvg.setAnimated(false);
        areaAvg.setPrefSize(600, 360);

        XYChart.Series<String, Number> avgSeries = new XYChart.Series<>();
        avgSeries.setName("Moyenne");

        for (Map.Entry<String, Number> e : topAverages) {
            String res = e.getKey() == null || e.getKey().isEmpty() ? "(inconnu)" : e.getKey();
            XYChart.Data<String, Number> dataNode = new XYChart.Data<>(res, e.getValue());
            avgSeries.getData().add(dataNode);
        }

        // Add a dummy point if only 1 data point is present, so the curve actually
        // draws a line
        if (topAverages.size() == 1) {
            avgSeries.getData()
                    .add(new XYChart.Data<>(topAverages.get(0).getKey() + " ", topAverages.get(0).getValue()));
        }

        areaAvg.getData().add(avgSeries);

        // Style the AreaChart line and filled area to be pretty
        javafx.application.Platform.runLater(() -> {
            javafx.scene.Node fill = areaAvg.lookup(".chart-series-area-fill");
            if (fill != null) {
                fill.setStyle("-fx-fill: linear-gradient(to bottom, rgba(243,156,18,0.7), rgba(241,196,15,0.1));");
            }
            javafx.scene.Node lineArea = areaAvg.lookup(".chart-series-area-line");
            if (lineArea != null) {
                lineArea.setStyle(
                        "-fx-stroke: #F39C12; -fx-stroke-width: 3px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);");
            }
            // Style the data points and add standard Tooltips
            for (XYChart.Data<String, Number> data : avgSeries.getData()) {
                javafx.scene.Node node = data.getNode();
                if (node != null) {
                    node.setStyle(
                            "-fx-background-color: #F1C40F, white; -fx-background-insets: 0, 2; -fx-background-radius: 5px; -fx-padding: 5px;");
                    javafx.scene.control.Tooltip t = new javafx.scene.control.Tooltip(
                            data.getXValue().trim() + " : " + data.getYValue() + " / 5");
                    t.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
                    javafx.scene.control.Tooltip.install(node, t);
                }
            }
        });

        wrapInCard("Moyenne des évaluations attribuées pour chaque ressource", areaAvg);

        // ----- 5) LISTE DÉTAILLÉE: Toutes les ressources et leurs moyennes -----
        VBox listContainer = new VBox(10);
        listContainer.setStyle("-fx-padding: 10;");

        List<Map.Entry<String, Number>> allAverages = statService.getAverageEvaluationPerRessource(100);
        for (Map.Entry<String, Number> e : allAverages) {
            String res = e.getKey() == null || e.getKey().isEmpty() ? "(inconnu)" : e.getKey();
            double avg = e.getValue().doubleValue();

            javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(15);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            row.setStyle(
                    "-fx-background-color: #f8fdff; -fx-padding: 12 20; -fx-background-radius: 10; -fx-border-color: #d1e7f3; -fx-border-radius: 10;");

            javafx.scene.control.Label lblName = new javafx.scene.control.Label("📖 " + res);
            lblName.setStyle(
                    "-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-pref-width: 400;");

            javafx.scene.control.Label lblStars = new javafx.scene.control.Label(
                    org.example.utils.StarRatingHelper.toStarStringFromAverage(avg));
            lblStars.setStyle("-fx-font-size: 16px; -fx-text-fill: #F1C40F;");

            javafx.scene.control.Label lblNote = new javafx.scene.control.Label(String.format("%.1f / 5", avg));
            lblNote.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d; -fx-font-weight: bold;");

            row.getChildren().addAll(lblName, lblStars, lblNote);
            listContainer.getChildren().add(row);
        }

        if (allAverages.isEmpty()) {
            javafx.scene.control.Label emptyLbl = new javafx.scene.control.Label("Aucune ressource trouvée.");
            emptyLbl.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");
            listContainer.getChildren().add(emptyLbl);
        }

        wrapInCard("Détail des moyennes pour chaque ressource", listContainer);
=======
>>>>>>> 7d8812e180519f847fbc6852e6afbef657ee5ca4
    }

    private void wrapInCard(String cardTitle, javafx.scene.Node chart) {
        VBox card = new VBox(12);
<<<<<<< HEAD
        card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.98); -fx-background-radius: 15; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0, 0, 4); -fx-border-color: #4A6FA5; -fx-border-radius: 15; -fx-border-width: 1;");
=======
        card.setStyle("-fx-background-color: rgba(255,255,255,0.98); -fx-background-radius: 15; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0, 0, 4); -fx-border-color: #4A6FA5; -fx-border-radius: 15; -fx-border-width: 1;");
>>>>>>> 7d8812e180519f847fbc6852e6afbef657ee5ca4
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
