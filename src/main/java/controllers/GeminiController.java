package controllers;

import services.ServiceGemini;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class GeminiController {

    @FXML private VBox chatContainer;
    @FXML private TextArea txtMessage;
    @FXML private Button btnEnvoyer;
    @FXML private ScrollPane scrollPane;
    @FXML private Label lblTyping;
    @FXML private ProgressIndicator progressIndicator;

    private ServiceGemini service;
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private volatile boolean isLoading = false;
    private Thread animationThread;

    @FXML
    public void initialize() {
        service = new ServiceGemini();

        // Message de bienvenue
        ajouterMessageBot("👋 Bonjour ! Je suis votre assistant IA GrowMind. Je peux vous aider sur :\n\n" +
                "• 😟 Anxiété • ⚡ Stress • 😴 Sommeil • 💙 Dépression • 💑 Relations\n" +
                "• 🧘 Méditation • 🏃 Exercice • 🕊️ Deuil • 🍎 Alimentation\n\n" +
                "Posez-moi votre question !");

        // Configuration de l'envoi avec Entrée
        txtMessage.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER") && !event.isShiftDown()) {
                event.consume();
                handleEnvoyer();
            }
        });

        // Focus automatique sur le champ de texte
        txtMessage.requestFocus();
    }

    @FXML
    private void handleEnvoyer() {
        String message = txtMessage.getText().trim();
        if (message.isEmpty() || isLoading) return;

        // Ajouter le message de l'utilisateur
        ajouterMessageUtilisateur(message);
        txtMessage.clear();

        // Mode chargement
        setLoading(true);
        lblTyping.setVisible(true);

        // Simuler un délai pour l'effet de réflexion
        new Thread(() -> {
            try {
                Thread.sleep(500); // Petit délai pour l'effet
                String reponse = service.sendPrompt(message);

                Platform.runLater(() -> {
                    ajouterMessageBot(reponse);
                    setLoading(false);
                    lblTyping.setVisible(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    ajouterMessageBot("❌ Désolé, une erreur est survenue. Veuillez réessayer.");
                    setLoading(false);
                    lblTyping.setVisible(false);
                });
            }
        }).start();
    }

    private void ajouterMessageUtilisateur(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_RIGHT);
        messageBox.setPadding(new Insets(5, 10, 5, 50));

        VBox messageContent = new VBox(3);
        messageContent.setAlignment(Pos.CENTER_RIGHT);

        // Heure
        Label timeLabel = new Label(LocalTime.now().format(timeFormatter));
        timeLabel.setStyle("-fx-text-fill: #95A5A6; -fx-font-size: 10px;");

        // Bulle de message
        Text text = new Text(message);
        text.setStyle("-fx-fill: white;");
        TextFlow textFlow = new TextFlow(text);
        textFlow.setStyle("-fx-background-color: #667eea; -fx-background-radius: 15 15 5 15; -fx-padding: 10;");
        textFlow.setMaxWidth(400);

        messageContent.getChildren().addAll(textFlow, timeLabel);
        messageBox.getChildren().add(messageContent);

        Platform.runLater(() -> {
            chatContainer.getChildren().add(messageBox);
            scrollToBottom();
        });
    }

    private void ajouterMessageBot(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_LEFT);
        messageBox.setPadding(new Insets(5, 50, 5, 10));

        // Avatar du bot
        Label avatar = new Label("🤖");
        avatar.setStyle("-fx-font-size: 28px; -fx-padding: 0 10 0 0;");

        VBox messageContent = new VBox(3);

        // Heure
        Label timeLabel = new Label(LocalTime.now().format(timeFormatter));
        timeLabel.setStyle("-fx-text-fill: #95A5A6; -fx-font-size: 10px;");

        // Bulle de message
        Text text = new Text(message);
        text.setStyle("-fx-fill: #2C3E50;");
        TextFlow textFlow = new TextFlow(text);
        textFlow.setStyle("-fx-background-color: #F0F2F5; -fx-background-radius: 15 15 15 5; -fx-padding: 10;");
        textFlow.setMaxWidth(400);

        messageContent.getChildren().addAll(textFlow, timeLabel);
        messageBox.getChildren().addAll(avatar, messageContent);

        Platform.runLater(() -> {
            chatContainer.getChildren().add(messageBox);
            scrollToBottom();
        });
    }

    private void scrollToBottom() {
        Platform.runLater(() -> {
            scrollPane.setVvalue(1.0);
        });
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
        btnEnvoyer.setDisable(loading);
        progressIndicator.setVisible(loading);

        if (loading) {
            startTypingAnimation();
        } else {
            stopTypingAnimation();
        }
    }

    private void startTypingAnimation() {
        animationThread = new Thread(() -> {
            String[] dots = {"", ".", "..", "..."};
            int i = 0;
            while (isLoading) {
                final int index = i % 4;
                Platform.runLater(() -> lblTyping.setText("🤖 L'assistant réfléchit" + dots[index]));
                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    break;
                }
                i++;
            }
        });
        animationThread.setDaemon(true);
        animationThread.start();
    }

    private void stopTypingAnimation() {
        if (animationThread != null) {
            animationThread.interrupt();
        }
        Platform.runLater(() -> lblTyping.setText(""));
    }

    @FXML
    private void handleNouvelleConversation() {
        chatContainer.getChildren().clear();
        ajouterMessageBot("👋 Bonjour ! Je suis votre assistant IA GrowMind. Comment puis-je vous aider aujourd'hui ?");
    }

    @FXML
    private void handleFermer() {
        ((Stage) chatContainer.getScene().getWindow()).close();
    }
}