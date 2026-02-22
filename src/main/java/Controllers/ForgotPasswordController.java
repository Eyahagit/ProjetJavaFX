package Controllers;

import Services.EmailService;
import utils.Database;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class ForgotPasswordController {

    @FXML private TextField emailField;
    @FXML private TextField codeField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML private Button sendCodeBtn;
    @FXML private Button verifyCodeBtn;
    @FXML private Button resetPasswordBtn;
    @FXML private Button backToLoginBtn;

    @FXML private Label statusLabel;
    @FXML private Label timerLabel;
    @FXML private ProgressIndicator loadingIndicator;

    @FXML private VBox step1Box;
    @FXML private VBox step2Box;
    @FXML private VBox step3Box;

    private EmailService emailService;
    private Database database;
    private String generatedCode;
    private String userEmail;
    private Timer timer;
    private int timeLeft = 900;

    private boolean isProcessing = false; // Pour éviter les doubles clics

    @FXML
    public void initialize() {
        emailService = EmailService.getInstance();
        database = Database.getInstance();
        showStep(1);
        loadingIndicator.setVisible(false);
        timerLabel.setVisible(false);
        setupValidation();

        // Tester la connexion au démarrage
        testConnection();
    }

    private void testConnection() {
        try {
            Connection conn = database.getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Connexion BD OK");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur de connexion: " + e.getMessage());
        }
    }

    private void setupValidation() {
        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateEmail();
        });

        codeField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() == 6) {
                verifyCode();
            }
        });
    }

    private void validateEmail() {
        String email = emailField.getText();
        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            emailField.setStyle("-fx-border-color: red;");
        } else {
            emailField.setStyle("");
        }
    }

    @FXML
    private void handleSendCode() {
        if (isProcessing) return;

        String email = emailField.getText().trim();

        if (email.isEmpty() || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert(Alert.AlertType.WARNING, "Email invalide",
                    "Veuillez entrer une adresse email valide.");
            return;
        }

        isProcessing = true;
        loadingIndicator.setVisible(true);
        sendCodeBtn.setDisable(true);
        statusLabel.setText("⏳ Vérification de l'email...");

        new Thread(() -> {
            boolean exists = userExists(email);

            javafx.application.Platform.runLater(() -> {
                if (!exists) {
                    loadingIndicator.setVisible(false);
                    sendCodeBtn.setDisable(false);
                    statusLabel.setText("❌ Email non trouvé");
                    showAlert(Alert.AlertType.ERROR, "Compte introuvable",
                            "Aucun compte n'est associé à cette adresse email.");
                    isProcessing = false;
                    return;
                }

                generatedCode = generateCode();
                userEmail = email;
                statusLabel.setText("⏳ Envoi du code...");

                new Thread(() -> {
                    boolean sent = emailService.sendResetCode(email, generatedCode);

                    javafx.application.Platform.runLater(() -> {
                        loadingIndicator.setVisible(false);
                        isProcessing = false;

                        if (sent) {
                            statusLabel.setStyle("-fx-text-fill: green;");
                            statusLabel.setText("✅ Code envoyé à " + email);
                            showStep(2);
                            startTimer();
                        } else {
                            sendCodeBtn.setDisable(false);
                            statusLabel.setStyle("-fx-text-fill: red;");
                            statusLabel.setText("❌ Échec de l'envoi.");
                        }
                    });
                }).start();
            });
        }).start();
    }

    @FXML
    private void handleVerifyCode() {
        verifyCode();
    }

    private void verifyCode() {
        String enteredCode = codeField.getText().trim();

        if (enteredCode.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Code requis",
                    "Veuillez entrer le code de vérification.");
            return;
        }

        if (enteredCode.equals(generatedCode)) {
            stopTimer();
            statusLabel.setStyle("-fx-text-fill: green;");
            statusLabel.setText("✅ Code vérifié !");
            showStep(3);
        } else {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("❌ Code incorrect");
            showAlert(Alert.AlertType.ERROR, "Code incorrect",
                    "Le code saisi n'est pas valide.");
        }
    }

    @FXML
    private void handleResetPassword() {
        if (isProcessing) return;

        String newPass = newPasswordField.getText();
        String confirmPass = confirmPasswordField.getText();

        if (newPass.isEmpty() || confirmPass.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs requis",
                    "Veuillez remplir tous les champs.");
            return;
        }

        if (newPass.length() < 6) {
            showAlert(Alert.AlertType.WARNING, "Mot de passe trop court",
                    "Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            showAlert(Alert.AlertType.WARNING, "Mots de passe différents",
                    "Les mots de passe ne correspondent pas.");
            return;
        }

        isProcessing = true;
        resetPasswordBtn.setDisable(true);
        loadingIndicator.setVisible(true);
        statusLabel.setText("⏳ Mise à jour...");

        new Thread(() -> {
            boolean updated = updatePassword(userEmail, newPass);

            javafx.application.Platform.runLater(() -> {
                loadingIndicator.setVisible(false);
                resetPasswordBtn.setDisable(false);
                isProcessing = false;

                if (updated) {
                    // UNE SEULE ALERTE
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Succès");
                    alert.setHeaderText(null);
                    alert.setContentText("✅ Mot de passe réinitialisé avec succès !");
                    alert.showAndWait();

                    handleBackToLogin();
                } else {
                    statusLabel.setStyle("-fx-text-fill: red;");
                    statusLabel.setText("❌ Échec de la mise à jour");
                    showAlert(Alert.AlertType.ERROR, "Erreur",
                            "Impossible de mettre à jour le mot de passe. Vérifiez votre connexion.");
                }
            });
        }).start();
    }

    @FXML
    private void handleResendCode() {
        if (isProcessing) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Renvoyer le code");
        confirm.setHeaderText("Confirmation");
        confirm.setContentText("Voulez-vous renvoyer un nouveau code ?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                handleSendCode();
            }
        });
    }

    @FXML
    private void handleBackToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Log_in.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backToLoginBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de retourner à la connexion: " + e.getMessage());
        }
    }

    private void showStep(int step) {
        step1Box.setVisible(step == 1);
        step1Box.setManaged(step == 1);
        step2Box.setVisible(step == 2);
        step2Box.setManaged(step == 2);
        step3Box.setVisible(step == 3);
        step3Box.setManaged(step == 3);
    }

    private String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    private void startTimer() {
        timeLeft = 900;
        timerLabel.setVisible(true);

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timeLeft--;
                javafx.application.Platform.runLater(() -> {
                    int minutes = timeLeft / 60;
                    int seconds = timeLeft % 60;
                    timerLabel.setText(String.format("⏱️ %02d:%02d", minutes, seconds));

                    if (timeLeft <= 0) {
                        timer.cancel();
                        timerLabel.setVisible(false);
                        statusLabel.setStyle("-fx-text-fill: red;");
                        statusLabel.setText("⌛ Code expiré");
                        codeField.setDisable(true);
                        verifyCodeBtn.setDisable(true);
                    }
                });
            }
        }, 0, 1000);
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timerLabel.setVisible(false);
        }
    }

    // ========== MÉTHODES BD CORRIGÉES ==========

    private boolean userExists(String email) {
        String query = "SELECT id FROM users WHERE email = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = database.getConnection();
            if (conn == null) return false;

            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, email);
            rs = pstmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            System.err.println("❌ Erreur userExists: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            // Ne fermez PAS la connexion ici ! Laissez Database la gérer
            try { if (rs != null) rs.close(); } catch (SQLException e) { }
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) { }
            // Ne fermez PAS conn.close() ici
        }
    }

    private boolean updatePassword(String email, String newPassword) {
        String query = "UPDATE users SET password = ? WHERE email = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = database.getConnection();
            if (conn == null) return false;

            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, newPassword);
            pstmt.setString(2, email);

            int rowsAffected = pstmt.executeUpdate();
            System.out.println("✅ Lignes modifiées: " + rowsAffected);
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur updatePassword: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            // Ne fermez PAS la connexion ici ! Laissez Database la gérer
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) { }
            // Ne fermez PAS conn.close() ici
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}