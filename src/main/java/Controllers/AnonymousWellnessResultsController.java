package Controllers;

import Models.AnonymousWellnessAssessment;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Contrôleur pour l'écran des résultats de bien-être anonyme
 */
public class AnonymousWellnessResultsController {
    
    @FXML private Label titleLabel;
    @FXML private Label overallScoreLabel;
    @FXML private Label stressLabel;
    @FXML private Label sleepLabel;
    @FXML private Label mentalLabel;
    @FXML private VBox recommendationsContainer;
    @FXML private Button backButton;
    @FXML private Button shareButton;
    @FXML private BarChart<String, Number> resultsChart;
    
    private AnonymousWellnessAssessment assessment;
    
    @FXML
    public void initialize() {
        setupChart();
        setupButtons();
    }
    
    /**
     * Configure les données d'évaluation
     */
    public void setAssessmentData(AnonymousWellnessAssessment assessment) {
        this.assessment = assessment;
        displayResults();
    }
    
    /**
     * Affiche les résultats
     */
    private void displayResults() {
        // Score global
        overallScoreLabel.setText(String.format("%.1f/100", assessment.getOverallWellnessScore()));
        overallScoreLabel.setStyle(getScoreStyle(assessment.getOverallWellnessScore()));
        
        // Scores détaillés
        stressLabel.setText(String.format("%s (%.1f/100)", 
            assessment.getStressCategory(), assessment.getStressLevel()));
        stressLabel.setStyle(getScoreStyle(assessment.getStressLevel()));
        
        sleepLabel.setText(String.format("%s (%.1f/100)", 
            assessment.getSleepCategory(), assessment.getSleepQuality()));
        sleepLabel.setStyle(getScoreStyle(assessment.getSleepQuality()));
        
        mentalLabel.setText(String.format("%s (%.1f/100)", 
            assessment.getMentalCategory(), assessment.getMentalState()));
        mentalLabel.setStyle(getScoreStyle(assessment.getMentalState()));
        
        // Recommandations
        displayRecommendations();
        
        // Graphique
        updateChart();
    }
    
    /**
     * Affiche les recommandations
     */
    private void displayRecommendations() {
        recommendationsContainer.getChildren().clear();
        
        if (assessment.getRecommendations() != null && !assessment.getRecommendations().isEmpty()) {
            Label recTitle = new Label("💡 Recommandations personnalisées");
            recTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #4A6FA5;");
            recommendationsContainer.getChildren().add(recTitle);
            
            for (String recommendation : assessment.getRecommendations()) {
                VBox recBox = new VBox(5);
                recBox.setStyle("-fx-background-color: #F0F8FF; -fx-background-radius: 10; -fx-padding: 15; -fx-margin: 5 0;");
                
                Label recLabel = new Label(recommendation);
                recLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333; -fx-wrap-text: true;");
                recLabel.setWrapText(true);
                
                recBox.getChildren().add(recLabel);
                recommendationsContainer.getChildren().add(recBox);
            }
        }
    }
    
    /**
     * Configure le graphique
     */
    private void setupChart() {
        resultsChart.setTitle("📊 Votre Profil de Bien-être");
        resultsChart.setLegendVisible(false);
        resultsChart.setAnimated(true);
        
        // Style du graphique
        resultsChart.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
    }
    
    /**
     * Met à jour le graphique avec les données
     */
    private void updateChart() {
        resultsChart.getData().clear();
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        
        series.getData().addAll(
            new XYChart.Data<>("Stress", assessment.getStressLevel()),
            new XYChart.Data<>("Sommeil", assessment.getSleepQuality()),
            new XYChart.Data<>("Santé Mentale", assessment.getMentalState())
        );
        
        resultsChart.getData().add(series);
        
        // Couleurs personnalisées
        for (XYChart.Data<String, Number> data : series.getData()) {
            String category = data.getXValue();
            double value = data.getYValue().doubleValue();
            
            if (value >= 80) {
                data.getNode().setStyle("-fx-background-color: #4CAF50;");
            } else if (value >= 60) {
                data.getNode().setStyle("-fx-background-color: #FFC107;");
            } else {
                data.getNode().setStyle("-fx-background-color: #F44336;");
            }
        }
    }
    
    /**
     * Configure les boutons
     */
    private void setupButtons() {
        backButton.setStyle("-fx-background-color: #E8F4F8; -fx-text-fill: #4A6FA5; -fx-font-weight: bold; -fx-background-radius: 25; -fx-padding: 15 30; -fx-font-size: 16px;");
        shareButton.setStyle("-fx-background-color: #5FB49C; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 25; -fx-padding: 15 30; -fx-font-size: 16px;");
        
        backButton.setOnAction(e -> handleBack());
        shareButton.setOnAction(e -> handleShare());
    }
    
    /**
     * Retour à l'écran principal
     */
    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionSanteBienEtre.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("GrowMind - Santé & Bien-être");
            stage.show();
            
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de retourner à l'écran principal : " + e.getMessage());
        }
    }
    
    /**
     * Partage les résultats
     */
    @FXML
    private void handleShare() {
        String resultsText = String.format(
            "🧠 Mon évaluation de bien-être GrowMind\\n" +
            "Score global: %.1f/100\\n" +
            "Stress: %s (%.1f/100)\\n" +
            "Sommeil: %s (%.1f/100)\\n" +
            "Santé mentale: %s (%.1f/100)\\n\\n" +
            "🌟 Découvrez votre bien-être sur GrowMind !",
            assessment.getOverallWellnessScore(),
            assessment.getStressCategory(), assessment.getStressLevel(),
            assessment.getSleepCategory(), assessment.getSleepQuality(),
            assessment.getMentalCategory(), assessment.getMentalState()
        );
        
        // Copier dans le presse-papiers
        javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
        javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
        content.putString(resultsText);
        clipboard.setContent(content);
        
        showAlert(Alert.AlertType.INFORMATION, "Résultats copiés", "Vos résultats ont été copiés dans le presse-papiers !");
    }
    
    /**
     * Retourne le style selon le score
     */
    private String getScoreStyle(double score) {
        if (score >= 80) {
            return "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;";
        } else if (score >= 60) {
            return "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #FFC107;";
        } else {
            return "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #F44336;";
        }
    }
    
    /**
     * Affiche une alerte
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
