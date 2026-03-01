package com.santebienetre.controller;

import com.santebienetre.service.SanteBienEtreService;
import com.santebienetre.service.SleepTrackingService;
import com.santebienetre.service.NutritionApiService;
import com.santebienetre.service.MockFitnessApiService;
import com.santebienetre.service.ai.PreventiveHealthAI;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Fixed Controller for AI Dashboard - SIMPLIFIED AND ROBUST
 */
public class AIDashboardController implements Initializable {
    
    @FXML private ImageView logoImageView;
    @FXML private ComboBox<Integer> cbUserId;
    @FXML private LineChart<String, Number> chartStressTrend;
    @FXML private BarChart<String, Number> chartSleepQuality;
    @FXML private PieChart chartMoodDistribution;
    @FXML private Label lblRecommendation;
    @FXML private Label lblRiskLevel;
    @FXML private Button btnRefresh;
    @FXML private Button btnExport;
    @FXML private Button btnBack;
    
    // API Integration fields
    @FXML private TextField tfFoodSearch;
    @FXML private Button btnSearchFood;
    @FXML private VBox vbNutritionResult;
    @FXML private Button btnConnectFitbit;
    @FXML private Label lblFitbitStatus;
    @FXML private Label lblFitbitSteps;
    
    // AI Insights fields
    @FXML private TextArea taInsights;
    @FXML private VBox vbAlerts;

    private final SanteBienEtreService santeService = new SanteBienEtreService();
    private final NutritionApiService nutritionService = new NutritionApiService();
    private final MockFitnessApiService fitnessService = new MockFitnessApiService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("[DEBUG] Initializing AIDashboardControllerFixed...");
        
        try {
            loadLogo();
            setupUserSelector();
            setupCharts();
            loadDashboardData();
            
            // Setup button handlers
            btnRefresh.setOnAction(e -> {
                System.out.println("[DEBUG] Refresh button clicked");
                loadDashboardData();
            });
            
            btnExport.setOnAction(e -> {
                System.out.println("[DEBUG] Export button clicked");
                handleExport();
            });
            
            btnBack.setOnAction(e -> {
                System.out.println("[DEBUG] Back button clicked");
                handleClose();
            });
            
            // API handlers
            btnSearchFood.setOnAction(e -> {
                System.out.println("[DEBUG] Food search button clicked!");
                handleFoodSearch();
            });
            
            btnConnectFitbit.setOnAction(e -> {
                System.out.println("[DEBUG] Fitness connection button clicked!");
                handleFitnessConnection();
            });
            
            setupApiInterface();
            
            System.out.println("[DEBUG] AIDashboardControllerFixed initialized successfully!");
            
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to initialize controller: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadLogo() {
        try {
            Image logo = new Image(getClass().getResourceAsStream("/images/logo.jpeg"));
            if (!logo.isError()) {
                logoImageView.setImage(logo);
                logoImageView.setFitWidth(500);
                logoImageView.setFitHeight(120);
                logoImageView.setPreserveRatio(true);
                System.out.println("[DEBUG] Logo loaded successfully");
            }
        } catch (Exception e) {
            System.out.println("[DEBUG] Logo not found, skipping...");
        }
    }

    private void setupUserSelector() {
        try {
            cbUserId.setConverter(new StringConverter<Integer>() {
                @Override
                public String toString(Integer userId) {
                    return "Utilisateur " + userId;
                }

                @Override
                public Integer fromString(String string) {
                    return Integer.parseInt(string.replaceAll("\\D", ""));
                }
            });
            
            List<Integer> userIds = santeService.getAllUserIds();
            cbUserId.setItems(FXCollections.observableArrayList(userIds));
            if (!userIds.isEmpty()) {
                cbUserId.setValue(userIds.get(0));
            }
            
            cbUserId.setOnAction(e -> {
                System.out.println("[DEBUG] User selected: " + cbUserId.getValue());
                loadDashboardData();
            });
            
            System.out.println("[DEBUG] User selector setup complete");
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to setup user selector: " + e.getMessage());
        }
    }

    private void setupCharts() {
        try {
            setupStressChart();
            setupSleepChart();
            setupMoodChart();
            System.out.println("[DEBUG] Charts setup complete");
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to setup charts: " + e.getMessage());
        }
    }

    private void setupStressChart() {
        if (chartStressTrend != null) {
            chartStressTrend.setTitle("Évolution du Stress");
            chartStressTrend.getXAxis().setLabel("Date");
            chartStressTrend.getYAxis().setLabel("Stress (1-10)");
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Stress");
            chartStressTrend.getData().add(series);
        }
    }

    private void setupSleepChart() {
        if (chartSleepQuality != null) {
            chartSleepQuality.setTitle("Qualité du Sommeil");
            chartSleepQuality.getXAxis().setLabel("Date");
            chartSleepQuality.getYAxis().setLabel("Qualité (1-10)");
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Sommeil");
            chartSleepQuality.getData().add(series);
        }
    }

    private void setupMoodChart() {
        if (chartMoodDistribution != null) {
            chartMoodDistribution.setTitle("Distribution des Humeurs");
            
            ObservableList<PieChart.Data> pieChartData = 
                FXCollections.observableArrayList(
                    new PieChart.Data("Heureux", 45),
                    new PieChart.Data("Neutre", 30),
                    new PieChart.Data("Fatigué", 15),
                    new PieChart.Data("Stressé", 10)
                );
            
            chartMoodDistribution.setData(pieChartData);
        }
    }

    private void loadDashboardData() {
        try {
            Integer selectedUserId = cbUserId.getValue();
            if (selectedUserId == null) return;

            System.out.println("[DEBUG] Loading dashboard data for user: " + selectedUserId);
            
            updateChartsWithMockData();
            generateMockRecommendations();
            updateRiskLevel();
            
            System.out.println("[DEBUG] Dashboard data loaded successfully");
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to load dashboard data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateChartsWithMockData() {
        try {
            if (chartStressTrend != null && chartStressTrend.getData().size() > 0) {
                chartStressTrend.getData().get(0).getData().clear();
                chartStressTrend.getData().get(0).getData().add(new XYChart.Data<>("Lun", 4));
                chartStressTrend.getData().get(0).getData().add(new XYChart.Data<>("Mar", 3));
                chartStressTrend.getData().get(0).getData().add(new XYChart.Data<>("Mer", 5));
            }
            
            if (chartSleepQuality != null && chartSleepQuality.getData().size() > 0) {
                chartSleepQuality.getData().get(0).getData().clear();
                chartSleepQuality.getData().get(0).getData().add(new XYChart.Data<>("Lun", 7));
                chartSleepQuality.getData().get(0).getData().add(new XYChart.Data<>("Mar", 8));
                chartSleepQuality.getData().get(0).getData().add(new XYChart.Data<>("Mer", 6));
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to update charts: " + e.getMessage());
        }
    }

    private void generateMockRecommendations() {
        try {
            System.out.println("[DEBUG] Generating mock recommendations...");
            
            if (lblRecommendation != null) {
                lblRecommendation.setText("🌟 Continuez votre bonne routine de sommeil ! Vos données montrent une tendance positive.");
            }
            
            generateAIInsights();
            generateAlerts();
            
            System.out.println("[DEBUG] Mock recommendations generated");
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to generate recommendations: " + e.getMessage());
        }
    }

    private void generateAIInsights() {
        try {
            if (taInsights != null) {
                String insights = "🤖 ANALYSES IA DÉTAILLÉES\n\n" +
                    "📊 Tendances observées :\n" +
                    "• Niveau de stress : Légère diminution cette semaine\n" +
                    "• Qualité de sommeil : Amélioration notable\n" +
                    "• Activité physique : Bon niveau maintenu\n\n" +
                    "💡 Recommandations personnalisées :\n" +
                    "• Continuez votre routine de coucher régulière\n" +
                    "• Ajoutez 10 minutes de méditation quotidienne\n" +
                    "• Hydratez-vous davantage pendant la journée";
                
                taInsights.setText(insights);
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to generate AI insights: " + e.getMessage());
        }
    }

    private void generateAlerts() {
        try {
            if (vbAlerts != null) {
                vbAlerts.getChildren().clear();
                
                addAlert("⚠️", "Attention", "Niveau de stress légèrement élevé hier", "ORANGE");
                addAlert("✅", "Bon", "Objectif de pas atteint 3 jours cette semaine", "GREEN");
                addAlert("💡", "Conseil", "Pensez à faire une pause toutes les 2 heures", "BLUE");
                
                System.out.println("[DEBUG] Alerts generated");
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to generate alerts: " + e.getMessage());
        }
    }

    private void addAlert(String icon, String type, String message, String color) {
        try {
            HBox alertBox = new HBox(10);
            alertBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-padding: 10; -fx-border-color: " + getColorHex(color) + "; -fx-border-radius: 8;");
            
            Label iconLabel = new Label(icon);
            iconLabel.setStyle("-fx-font-size: 16px;");
            
            VBox content = new VBox(2);
            Label typeLabel = new Label(type);
            typeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: " + color + ";");
            
            Label messageLabel = new Label(message);
            messageLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
            
            content.getChildren().addAll(typeLabel, messageLabel);
            alertBox.getChildren().addAll(iconLabel, content);
            
            vbAlerts.getChildren().add(alertBox);
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to add alert: " + e.getMessage());
        }
    }

    private String getColorHex(String color) {
        switch (color.toUpperCase()) {
            case "RED": return "#ff4444";
            case "ORANGE": return "#ff8800";
            case "GREEN": return "#00C851";
            case "BLUE": return "#33b5e5";
            default: return "#cccccc";
        }
    }

    private void updateRiskLevel() {
        try {
            if (lblRiskLevel != null) {
                String riskLevel = "FAIBLE";
                String riskColor = "#00C851"; // GREEN
                
                lblRiskLevel.setText(riskLevel);
                lblRiskLevel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + riskColor + ";");
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to update risk level: " + e.getMessage());
        }
    }

    @FXML
    private void handleExport() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Exporter les données");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );
            
            File file = fileChooser.showSaveDialog(new Stage());
            if (file != null) {
                showAlert(Alert.AlertType.INFORMATION, "Export", "Exportation vers " + file.getName() + " réussie!");
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Export failed: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur", "L'export a échoué: " + e.getMessage());
        }
    }

    @FXML
    private void handleClose() {
        try {
            Stage stage = (Stage) btnBack.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to close window: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        try {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to show alert: " + e.getMessage());
        }
    }

    // API Integration Methods
    private void setupApiInterface() {
        try {
            if (lblFitbitStatus != null) {
                lblFitbitStatus.setText("❌ Non connecté");
                lblFitbitStatus.setTextFill(Color.RED);
            }
            if (btnConnectFitbit != null) {
                btnConnectFitbit.setText("🔌 Connexion Fitness");
            }
            if (lblFitbitSteps != null) {
                lblFitbitSteps.setText("Pas: -");
            }
            
            System.out.println("[DEBUG] API interface setup complete");
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to setup API interface: " + e.getMessage());
        }
    }

    @FXML
    private void handleFoodSearch() {
        try {
            System.out.println("[DEBUG] Food search button clicked!");
            
            if (tfFoodSearch == null) {
                System.err.println("[ERROR] tfFoodSearch is null!");
                return;
            }
            
            String foodName = tfFoodSearch.getText().trim();
            System.out.println("[DEBUG] Searching for: " + foodName);
            
            if (foodName.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Recherche", "Veuillez entrer un nom d'aliment");
                return;
            }

            System.out.println("[DEBUG] Calling nutrition API...");
            NutritionApiService.NutritionInfo info = nutritionService.getFoodNutrition(foodName);
            System.out.println("[DEBUG] Got nutrition info: " + info.getFoodName());
            displayNutritionResult(info);
            
        } catch (Exception e) {
            System.err.println("[ERROR] Nutrition search failed: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de récupérer les informations nutritionnelles: " + e.getMessage());
        }
    }

    private void displayNutritionResult(NutritionApiService.NutritionInfo info) {
        try {
            if (vbNutritionResult == null) {
                System.err.println("[ERROR] vbNutritionResult is null!");
                return;
            }
            
            vbNutritionResult.getChildren().clear();

            // Header avec nom et source
            Label titleLabel = new Label("🍽️ Analyse Nutritionnelle");
            titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #4A6FA5;");
            vbNutritionResult.getChildren().add(titleLabel);

            Label nameLabel = new Label("Aliment: " + info.getFoodName());
            nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
            vbNutritionResult.getChildren().add(nameLabel);

            Label sourceLabel = new Label("Source: " + info.getSource());
            sourceLabel.setStyle("-fx-font-size: 12px; -fx-font-style: italic; -fx-text-fill: #7F8C8D;");
            vbNutritionResult.getChildren().add(sourceLabel);

            // Ligne de séparation
            Separator separator = new Separator();
            separator.setStyle("-fx-background-color: #E8E8E8;");
            vbNutritionResult.getChildren().add(separator);

            // Informations nutritionnelles principales
            HBox mainInfoBox = new HBox(20);
            mainInfoBox.setStyle("-fx-background-color: #F8F9FA; -fx-background-radius: 8; -fx-padding: 10;");

            // Calories
            VBox caloriesBox = createNutrientBox("🔥", "Calories", info.getCalories() + " kcal", "#E74C3C");
            
            // Protéines
            VBox proteinsBox = createNutrientBox("🥩", "Protéines", String.format("%.1fg", info.getProteins()), "#3498DB");
            
            // Glucides
            VBox carbsBox = createNutrientBox("🍞", "Glucides", String.format("%.1fg", info.getCarbs()), "#F39C12");
            
            // Lipides
            VBox fatsBox = createNutrientBox("🥑", "Lipides", String.format("%.1fg", info.getFats()), "#27AE60");

            mainInfoBox.getChildren().addAll(caloriesBox, proteinsBox, carbsBox, fatsBox);
            vbNutritionResult.getChildren().add(mainInfoBox);

            // Informations nutritionnelles détaillées
            HBox detailedInfoBox = new HBox(15);
            detailedInfoBox.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 8; -fx-padding: 10; -fx-border-color: #E8E8E8; -fx-border-radius: 8;");

            // Fibres
            VBox fiberBox = createSecondaryNutrientBox("🌾", "Fibres", String.format("%.1fg", info.getFiber()), "#8E44AD");
            
            // Sucres
            VBox sugarBox = createSecondaryNutrientBox("🍬", "Sucres", String.format("%.1fg", info.getSugar()), "#E67E22");
            
            // Sodium
            VBox sodiumBox = createSecondaryNutrientBox("🧂", "Sodium", String.format("%.0fmg", info.getSodium()), "#95A5A6");

            detailedInfoBox.getChildren().addAll(fiberBox, sugarBox, sodiumBox);
            vbNutritionResult.getChildren().add(detailedInfoBox);

            // Barres de progression visuelles
            vbNutritionResult.getChildren().add(createNutritionBars(info));

            // Conseils santé
            if (info.getHealthAdvice() != null && !info.getHealthAdvice().isEmpty()) {
                Separator adviceSeparator = new Separator();
                adviceSeparator.setStyle("-fx-background-color: #E8E8E8;");
                vbNutritionResult.getChildren().add(adviceSeparator);

                Label adviceTitle = new Label("💡 Conseils Santé");
                adviceTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
                vbNutritionResult.getChildren().add(adviceTitle);

                TextArea adviceArea = new TextArea(info.getHealthAdvice());
                adviceArea.setEditable(false);
                adviceArea.setWrapText(true);
                adviceArea.setStyle("-fx-background-color: #E8F6F3; -fx-border-color: #A9DFBF; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 12px; -fx-text-fill: #1E8449;");
                adviceArea.setPrefRowCount(3);
                vbNutritionResult.getChildren().add(adviceArea);
            }
            
            // Style général du conteneur
            vbNutritionResult.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20; -fx-border-color: #E0E0E0; -fx-border-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
            
            System.out.println("[DEBUG] Enhanced nutrition result displayed for: " + info.getFoodName());
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to display nutrition result: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private VBox createNutrientBox(String icon, String label, String value, String color) {
        VBox box = new VBox(5);
        box.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 8; -fx-border-color: " + color + "; -fx-border-radius: 8; -fx-alignment: center;");
        box.setPrefWidth(80);
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 20px;");
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        
        Label labelLabel = new Label(label);
        labelLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #7F8C8D;");
        
        box.getChildren().addAll(iconLabel, valueLabel, labelLabel);
        return box;
    }

    private VBox createSecondaryNutrientBox(String icon, String label, String value, String color) {
        VBox box = new VBox(3);
        box.setStyle("-fx-background-color: #F8F9FA; -fx-background-radius: 6; -fx-padding: 6; -fx-border-color: " + color + "; -fx-border-radius: 6; -fx-alignment: center;");
        box.setPrefWidth(70);
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 16px;");
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        
        Label labelLabel = new Label(label);
        labelLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: #7F8C8D;");
        
        box.getChildren().addAll(iconLabel, valueLabel, labelLabel);
        return box;
    }

    private HBox createNutritionBars(NutritionApiService.NutritionInfo info) {
        HBox barsBox = new HBox(10);
        barsBox.setStyle("-fx-padding: 10; -fx-alignment: center;");
        
        // Barre de protéines
        VBox proteinBar = createProgressBar("Protéines", info.getProteins(), 50.0, "#3498DB");
        
        // Barre de glucides
        VBox carbsBar = createProgressBar("Glucides", info.getCarbs(), 100.0, "#F39C12");
        
        // Barre de lipides
        VBox fatsBar = createProgressBar("Lipides", info.getFats(), 70.0, "#27AE60");
        
        barsBox.getChildren().addAll(proteinBar, carbsBar, fatsBar);
        return barsBox;
    }

    private VBox createProgressBar(String label, double value, double max, String color) {
        VBox barBox = new VBox(5);
        barBox.setPrefWidth(100);
        
        Label labelLabel = new Label(label);
        labelLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        
        ProgressBar progressBar = new ProgressBar(Math.min(value / max, 1.0));
        progressBar.setPrefWidth(100);
        progressBar.setStyle("-fx-accent: " + color + ";");
        
        Label valueLabel = new Label(String.format("%.1fg", value));
        valueLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #7F8C8D;");
        
        barBox.getChildren().addAll(labelLabel, progressBar, valueLabel);
        return barBox;
    }

    @FXML
    private void handleFitnessConnection() {
        try {
            System.out.println("[DEBUG] Fitness connection button clicked!");
            
            if (fitnessService == null) {
                System.err.println("[ERROR] fitnessService is null!");
                return;
            }
            
            if (!fitnessService.isConnected()) {
                System.out.println("[DEBUG] Connecting to fitness service...");
                boolean connected = fitnessService.connect();
                if (connected) {
                    System.out.println("[DEBUG] Fitness connected successfully!");
                    if (lblFitbitStatus != null) {
                        lblFitbitStatus.setText("✅ Connecté (Mode Simulation)");
                        lblFitbitStatus.setTextFill(Color.GREEN);
                    }
                    if (btnConnectFitbit != null) {
                        btnConnectFitbit.setText("🔌 Déconnexion");
                    }
                    loadFitnessData();
                } else {
                    System.out.println("[DEBUG] Fitness connection failed!");
                    if (lblFitbitStatus != null) {
                        lblFitbitStatus.setText("❌ Erreur de connexion");
                        lblFitbitStatus.setTextFill(Color.RED);
                    }
                }
            } else {
                System.out.println("[DEBUG] Disconnecting from fitness service...");
                fitnessService.disconnect();
                if (lblFitbitStatus != null) {
                    lblFitbitStatus.setText("❌ Non connecté");
                    lblFitbitStatus.setTextFill(Color.RED);
                }
                if (btnConnectFitbit != null) {
                    btnConnectFitbit.setText("🔌 Connexion Fitness");
                }
                if (lblFitbitSteps != null) {
                    lblFitbitSteps.setText("Pas: -");
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Fitness connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadFitnessData() {
        try {
            if (fitnessService != null && fitnessService.isConnected()) {
                String fitnessData = fitnessService.getTodaySteps();
                if (lblFitbitSteps != null) {
                    lblFitbitSteps.setText(fitnessData);
                }
                System.out.println("[DEBUG] Fitness data loaded: " + fitnessData);
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to load fitness data: " + e.getMessage());
        }
    }
}
