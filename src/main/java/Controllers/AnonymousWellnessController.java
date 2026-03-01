package Controllers;

import Services.AnonymousSessionService;
import Services.AnonymousWellnessAssessmentAI;
import Models.AnonymousWellnessAssessment;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur pour l'écran de bien-être anonyme
 */
public class AnonymousWellnessController {
    
    @FXML private Label titleLabel;
    @FXML private VBox questionsContainer;
    @FXML private Button submitButton;
    @FXML private Button backButton;
    @FXML private ProgressIndicator progressIndicator;
    
    private AnonymousSessionService sessionService;
    private AnonymousWellnessAssessmentAI assessmentAI;
    private String sessionId;
    
    // Questions du quiz de bien-être
    private final Map<String, String> wellnessQuestions = new HashMap<>();
    private final Map<String, Slider> questionSliders = new HashMap<>();
    
    @FXML
    public void initialize() {
        sessionService = new AnonymousSessionService();
        assessmentAI = new AnonymousWellnessAssessmentAI();
        
        // Créer une session anonyme
        sessionId = sessionService.createAnonymousSession();
        
        // Initialiser les questions
        initializeQuestions();
        
        // Créer l'interface des questions
        createQuestionInterface();
        
        // Configurer les boutons
        setupButtons();
    }
    
    /**
     * Initialise les questions de bien-être
     */
    private void initializeQuestions() {
        // Questions sur le stress
        wellnessQuestions.put("stress_frequency", "À quelle fréquence vous sentez-vous stressé ?");
        wellnessQuestions.put("stress_intensity", "Quelle est l'intensité de votre stress ?");
        wellnessQuestions.put("work_stress", "Comment évaluez-vous votre stress au travail ?");
        wellnessQuestions.put("personal_stress", "Comment évaluez-vous votre stress personnel ?");
        wellnessQuestions.put("anxiety_level", "Quel est votre niveau d'anxiété général ?");
        
        // Questions sur le sommeil
        wellnessQuestions.put("sleep_duration", "Combien d'heures dormez-vous par nuit ?");
        wellnessQuestions.put("sleep_quality", "Comment évaluez-vous la qualité de votre sommeil ?");
        wellnessQuestions.put("sleep_consistency", "Votre horaire de sommeil est-il régulier ?");
        wellnessQuestions.put("fall_asleep_time", "Combien de temps pour vous endormir ?");
        wellnessQuestions.put("night_awakenings", "Combien de réveils nocturnes ?");
        
        // Questions sur la santé mentale
        wellnessQuestions.put("mood_level", "Comment évaluez-vous votre humeur générale ?");
        wellnessQuestions.put("energy_level", "Quel est votre niveau d'énergie ?");
        wellnessQuestions.put("concentration", "Comment évaluez-vous votre capacité de concentration ?");
        wellnessQuestions.put("social_interaction", "Comment évaluez-vous vos interactions sociales ?");
        wellnessQuestions.put("life_satisfaction", "Quel est votre niveau de satisfaction de vie ?");
    }
    
    /**
     * Crée l'interface des questions
     */
    private void createQuestionInterface() {
        questionsContainer.getChildren().clear();
        questionSliders.clear();
        
        // Style pour les questions
        String questionStyle = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #4A6FA5;";
        String sliderStyle = "-fx-accent: #5FB49C;";
        
        for (Map.Entry<String, String> entry : wellnessQuestions.entrySet()) {
            String questionId = entry.getKey();
            String questionText = entry.getValue();
            
            VBox questionBox = new VBox(10);
            questionBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
            
            Label questionLabel = new Label(questionText);
            questionLabel.setStyle(questionStyle);
            
            Slider slider = new Slider(1, 5, 3);
            slider.setStyle(sliderStyle);
            slider.setShowTickLabels(true);
            slider.setShowTickMarks(true);
            slider.setMajorTickUnit(1);
            slider.setMinorTickCount(0);
            slider.setSnapToTicks(true);
            
            // Labels pour les valeurs
            HBox valueLabels = new HBox(10);
            valueLabels.setStyle("-fx-alignment: center;");
            Label minLabel = new Label("Très faible");
            Label maxLabel = new Label("Très élevé");
            minLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
            maxLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
            valueLabels.getChildren().addAll(minLabel, maxLabel);
            
            questionBox.getChildren().addAll(questionLabel, slider, valueLabels);
            questionsContainer.getChildren().add(questionBox);
            
            questionSliders.put(questionId, slider);
        }
    }
    
    /**
     * Configure les boutons
     */
    private void setupButtons() {
        submitButton.setStyle("-fx-background-color: #5FB49C; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 25; -fx-padding: 15 30; -fx-font-size: 16px;");
        backButton.setStyle("-fx-background-color: #E8F4F8; -fx-text-fill: #4A6FA5; -fx-font-weight: bold; -fx-background-radius: 25; -fx-padding: 15 30; -fx-font-size: 16px;");
        
        submitButton.setOnAction(e -> handleSubmit());
        backButton.setOnAction(e -> handleBack());
    }
    
    /**
     * Gère la soumission du questionnaire
     */
    @FXML
    private void handleSubmit() {
        try {
            // Afficher l'indicateur de progression
            progressIndicator.setVisible(true);
            submitButton.setDisable(true);
            
            // Collecter les réponses
            Map<String, Integer> responses = new HashMap<>();
            for (Map.Entry<String, Slider> entry : questionSliders.entrySet()) {
                responses.put(entry.getKey(), (int) entry.getValue().getValue());
            }
            
            // Évaluer le bien-être
            AnonymousWellnessAssessment assessment = assessmentAI.assessAnonymousWellness(sessionId, responses);
            
            // Afficher les résultats
            showResults(assessment);
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de l'évaluation : " + e.getMessage());
        } finally {
            progressIndicator.setVisible(false);
            submitButton.setDisable(false);
        }
    }
    
    /**
     * Affiche les résultats de l'évaluation
     */
    private void showResults(AnonymousWellnessAssessment assessment) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AnonymousWellnessResults.fxml"));
            Parent root = loader.load();
            
            AnonymousWellnessResultsController controller = loader.getController();
            controller.setAssessmentData(assessment);
            
            Stage stage = (Stage) submitButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Résultats - GrowMind");
            stage.show();
            
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'afficher les résultats : " + e.getMessage());
        }
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
