package com.santebienetre.controller;

import com.santebienetre.service.ai.PublicWellnessChatbot;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Contrôleur pour le chatbot IA de bien-être
 */
public class WellnessChatbotController {
    
    @FXML private Label titleLabel;
    @FXML private VBox chatContainer;
    @FXML private ScrollPane chatScrollPane;
    @FXML private TextArea messageInput;
    @FXML private Button sendButton;
    @FXML private Button backButton;
    @FXML private Button clearButton;
    @FXML private Label typingLabel;
    @FXML private ImageView logoImageView;
    
    private PublicWellnessChatbot chatbot;
    private String sessionId;
    private List<ChatMessage> messages;
    
    @FXML
    public void initialize() {
        chatbot = new PublicWellnessChatbot();
        sessionId = "session_" + System.currentTimeMillis();
        messages = new ArrayList<>();
        
        setupChatInterface();
        addWelcomeMessage();
        setupButtons();
        loadLogo();
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
        } catch (Exception ignored) {
            // Keep silent
        }
    }
    
    /**
     * Configure l'interface du chat
     */
    private void setupChatInterface() {
        chatScrollPane.setFitToWidth(true);
        chatScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        chatScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        chatContainer.setSpacing(10);
        chatContainer.setStyle("-fx-padding: 10;");
        
        // Style du message input
        messageInput.setPromptText("Tapez votre message ici...");
        messageInput.setWrapText(true);
        messageInput.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 20; -fx-padding: 10;");
        
        // Boutons stylés
        sendButton.setStyle("-fx-background-color: #5FB49C; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 10 20; -fx-font-weight: bold;");
        backButton.setStyle("-fx-background-color: #E8F4F8; -fx-text-fill: #4A6FA5; -fx-background-radius: 20; -fx-padding: 10 20; -fx-font-weight: bold;");
        clearButton.setStyle("-fx-background-color: #FFE8E8; -fx-text-fill: #D32F2F; -fx-background-radius: 20; -fx-padding: 10 20; -fx-font-weight: bold;");
        
        // Label de frappe
        typingLabel.setTextFill(Color.GRAY);
        typingLabel.setFont(Font.font("Arial", 11));
    }
    
    /**
     * Ajoute le message de bienvenue
     */
    private void addWelcomeMessage() {
        ChatMessage welcome = new ChatMessage(
            "🤖 Bienvenue ! Je suis votre assistant bien-être IA. Je suis là pour vous écouter 24/7 de manière anonyme et confidentielle.\n\n" +
            "Je peux vous aider avec :\n" +
            "• Gestion du stress et de l'anxiété\n" +
            "• Amélioration du sommeil\n" +
            "• Humeur et énergie\n" +
            "• Relations sociales\n\n" +
            "Comment vous sentez-vous aujourd'hui ?",
            true,
            LocalDateTime.now()
        );
        
        messages.add(welcome);
        displayMessage(welcome);
    }
    
    /**
     * Configure les boutons
     */
    private void setupButtons() {
        sendButton.setOnAction(e -> sendMessage());
        backButton.setOnAction(e -> handleBack());
        clearButton.setOnAction(e -> clearChat());
        
        // Permettre l'envoi avec Entrée
        messageInput.setOnKeyPressed(e -> {
            if (e.getCode().toString().equals("ENTER") && e.isShiftDown()) {
                sendMessage();
            }
        });
    }
    
    /**
     * Envoie un message
     */
    @FXML
    private void sendMessage() {
        String userMessage = messageInput.getText().trim();
        if (userMessage.isEmpty()) return;
        
        // Ajouter le message utilisateur
        ChatMessage userChatMessage = new ChatMessage(userMessage, false, LocalDateTime.now());
        messages.add(userChatMessage);
        displayMessage(userChatMessage);
        
        // Vider le champ
        messageInput.clear();
        
        // Afficher "en train d'écrire..."
        showTypingIndicator();
        
        // Simuler le traitement IA (délai réaliste)
        new Thread(() -> {
            try {
                Thread.sleep(1000 + (int)(Math.random() * 1500)); // 1-2.5 secondes
                
                // Obtenir la réponse IA
                PublicWellnessChatbot.ChatResponse response = chatbot.respondToUser(userMessage, sessionId);
                
                // Masquer l'indicateur de frappe
                javafx.application.Platform.runLater(() -> {
                    hideTypingIndicator();
                    
                    // Ajouter la réponse IA
                    ChatMessage botMessage = new ChatMessage(
                        response.getMessage(),
                        true,
                        LocalDateTime.now()
                    );
                    
                    messages.add(botMessage);
                    displayMessage(botMessage);
                    
                    // Afficher les actions suggérées si crise
                    if (response.isCrisis()) {
                        displayCrisisActions(response.getSuggestedActions());
                    } else if (!response.getSuggestedActions().isEmpty()) {
                        displaySuggestedActions(response.getSuggestedActions());
                    }
                    
                    // Afficher le niveau de confiance
                    if (response.getConfidence() < 0.5) {
                        displayConfidenceWarning();
                    }
                });
                
            } catch (InterruptedException e) {
                javafx.application.Platform.runLater(this::hideTypingIndicator);
            }
        }).start();
    }
    
    /**
     * Affiche un message dans le chat
     */
    private void displayMessage(ChatMessage message) {
        VBox messageBox = new VBox(5);
        messageBox.setMaxWidth(Double.MAX_VALUE);
        
        // En-tête avec timestamp
        HBox headerBox = new HBox();
        headerBox.setStyle(message.isBot() ? "-fx-alignment: center-left;" : "-fx-alignment: center-right;");
        
        Label timestampLabel = new Label(message.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm")));
        timestampLabel.setFont(Font.font("Arial", 10));
        timestampLabel.setTextFill(Color.GRAY);
        
        Label senderLabel = new Label(message.isBot() ? "🤖 Assistant Bien-être" : "👤 Vous");
        senderLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        senderLabel.setTextFill(message.isBot() ? Color.web("#5FB49C") : Color.web("#4A6FA5"));
        
        if (message.isBot()) {
            headerBox.getChildren().addAll(senderLabel, new Label("  "), timestampLabel);
        } else {
            headerBox.getChildren().addAll(timestampLabel, new Label("  "), senderLabel);
        }
        
        // Contenu du message
        Text messageText = new Text(message.getContent());
        messageText.setWrappingWidth(350);
        messageText.setFont(Font.font("Arial", 13));
        
        VBox messageBubble = new VBox(5);
        messageBubble.getChildren().addAll(messageText);
        
        if (message.isBot()) {
            messageBubble.setStyle("-fx-background-color: #F0F8FF; -fx-background-radius: 15; -fx-padding: 12; -fx-border-color: #E3F2FD; -fx-border-radius: 15;");
            messageBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        } else {
            messageBubble.setStyle("-fx-background-color: #E8F4F8; -fx-background-radius: 15; -fx-padding: 12; -fx-border-color: #D1E7DD; -fx-border-radius: 15;");
            messageBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        }
        
        messageBox.getChildren().addAll(headerBox, messageBubble);
        chatContainer.getChildren().add(messageBox);
        
        // Scroller vers le bas
        javafx.application.Platform.runLater(() -> {
            chatScrollPane.setVvalue(1.0);
        });
    }
    
    /**
     * Affiche l'indicateur de frappe
     */
    private void showTypingIndicator() {
        typingLabel.setText("🤖 L'assistant bien-être écrit...");
        typingLabel.setVisible(true);
    }
    
    /**
     * Masque l'indicateur de frappe
     */
    private void hideTypingIndicator() {
        typingLabel.setVisible(false);
    }
    
    /**
     * Affiche les actions suggérées en cas de crise
     */
    private void displayCrisisActions(List<String> actions) {
        VBox crisisBox = new VBox(10);
        crisisBox.setStyle("-fx-background-color: #FFEBEE; -fx-background-radius: 10; -fx-padding: 15; -fx-border-color: #F44336; -fx-border-radius: 10;");
        
        Label crisisTitle = new Label("🚨 URGENCE - Ressources disponibles");
        crisisTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        crisisTitle.setTextFill(Color.web("#D32F2F"));
        
        for (String action : actions) {
            Button actionButton = new Button(action);
            actionButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 16;");
            actionButton.setMaxWidth(Double.MAX_VALUE);
            actionButton.setOnAction(e -> {
                showAlert(Alert.AlertType.WARNING, "Ressource d'urgence", 
                    "Cette ressource est disponible immédiatement. N'hésitez pas à l'utiliser.");
            });
            crisisBox.getChildren().add(actionButton);
        }
        
        chatContainer.getChildren().add(crisisBox);
        chatScrollPane.setVvalue(1.0);
    }
    
    /**
     * Affiche les actions suggérées normales
     */
    private void displaySuggestedActions(List<String> actions) {
        HBox actionsBox = new HBox(10);
        actionsBox.setStyle("-fx-padding: 10 0;");
        
        for (String action : actions) {
            Button actionButton = new Button(action);
            actionButton.setStyle("-fx-background-color: #5FB49C; -fx-text-fill: white; -fx-background-radius: 15; -fx-padding: 6 12; -fx-font-size: 11;");
            actionButton.setOnAction(e -> {
                messageInput.setText(action.replace("🧘‍♀️ ", "").replace("🚶‍♀️ ", "").replace("📝 ", "").replace("😴 ", "").replace("📱 ", "").replace("🌿 ", "").replace("🎵 ", "").replace("👥 ", "").replace("🏃‍♀️ ", "").replace("💬 ", "").replace("🧠 ", "").replace("📖 ", ""));
                messageInput.requestFocus();
            });
            actionsBox.getChildren().add(actionButton);
        }
        
        chatContainer.getChildren().add(actionsBox);
        chatScrollPane.setVvalue(1.0);
    }
    
    /**
     * Affiche un avertissement de confiance
     */
    private void displayConfidenceWarning() {
        Label warningLabel = new Label("💡 Je ne suis pas sûr de comprendre. Pouvez-vous reformuler ?");
        warningLabel.setStyle("-fx-background-color: #FFF3E0; -fx-background-radius: 10; -fx-padding: 10; -fx-text-fill: #F57C00;");
        warningLabel.setFont(Font.font("Arial", 11));
        
        chatContainer.getChildren().add(warningLabel);
        chatScrollPane.setVvalue(1.0);
    }
    
    /**
     * Vide le chat
     */
    @FXML
    private void clearChat() {
        chatContainer.getChildren().clear();
        messages.clear();
        addWelcomeMessage();
    }
    
    /**
     * Retour à l'écran principal
     */
    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/GestionSanteBienEtre.fxml"));
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
    
    /**
     * Classe interne pour les messages du chat
     */
    private static class ChatMessage {
        private String content;
        private boolean isBot;
        private LocalDateTime timestamp;
        
        public ChatMessage(String content, boolean isBot, LocalDateTime timestamp) {
            this.content = content;
            this.isBot = isBot;
            this.timestamp = timestamp;
        }
        
        public String getContent() { return content; }
        public boolean isBot() { return isBot; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
}
