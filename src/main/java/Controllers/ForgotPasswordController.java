package Controllers;

import Services.EmailService;
import Services.TwilioVerifyService;
import Services.ServiceUser;
import utils.Database;
import utils.PasswordUtils;
import utils.PhoneFormatter;
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

    // ========== ÉLÉMENTS FXML ==========

    // Boîte de choix initiale
    @FXML private VBox step1Box;

    // Étape 1 : Email
    @FXML private VBox emailStepBox;
    @FXML private TextField emailField;
    @FXML private Button sendCodeBtn;

    // Étape 1 : SMS
    @FXML private VBox smsStepBox;
    @FXML private TextField phoneField;
    @FXML private Button sendSmsCodeBtn;

    // Étape 2 : Code (commun)
    @FXML private VBox codeStepBox;
    @FXML private TextField codeField;
    @FXML private Button verifyCodeBtn;
    @FXML private Button resendCodeBtn;
    @FXML private Label timerLabel;

    // Étape 3 : Nouveau mot de passe (commun)
    @FXML private VBox passwordStepBox;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button resetPasswordBtn;

    // Éléments communs
    @FXML private Label statusLabel;
    @FXML private Label methodLabel;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Button backToLoginBtn;

    // ========== SERVICES ==========

    private EmailService emailService;
    private TwilioVerifyService twilioService;
    private ServiceUser userService;
    private Database database;

    // ========== VARIABLES D'ÉTAT ==========

    private String generatedCode;
    private String userEmail;
    private String currentPhone;
    private String currentMethod; // "EMAIL" ou "SMS"
    private Timer timer;
    private int timeLeft = 900; // 15 minutes par défaut
    private boolean isProcessing = false;

    // ========== INITIALISATION ==========

    @FXML
    public void initialize() {
        // Initialiser les services
        emailService = EmailService.getInstance();
        twilioService = TwilioVerifyService.getInstance();
        userService = new ServiceUser();
        database = Database.getInstance();

        // Afficher l'écran de choix
        showChoiceScreen();

        // Initialiser les composants UI
        loadingIndicator.setVisible(false);
        timerLabel.setVisible(false);
        resendCodeBtn.setDisable(true);
        methodLabel.setText("");

        // Configurer les validations
        setupValidation();

    }

    private void showChoiceScreen() {
        // Afficher uniquement la boîte de choix
        step1Box.setVisible(true);
        step1Box.setManaged(true);

        emailStepBox.setVisible(false);
        emailStepBox.setManaged(false);
        smsStepBox.setVisible(false);
        smsStepBox.setManaged(false);
        codeStepBox.setVisible(false);
        codeStepBox.setManaged(false);
        passwordStepBox.setVisible(false);
        passwordStepBox.setManaged(false);
    }


    private void setupValidation() {
        // Validation email
        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateEmail();
        });

        // Validation téléphone
        phoneField.textProperty().addListener((obs, oldVal, newVal) -> {
            validatePhone();
        });

        // Auto-vérification quand 6 chiffres sont tapés
        codeField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() == 6) {
                verifyCode();
            }
        });
    }

    private void validateEmail() {
        String email = emailField.getText();
        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            emailField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
        } else {
            emailField.setStyle("");
        }
    }

    private void validatePhone() {
        String phone = phoneField.getText();
        if (!phone.isEmpty()) {
            String formatted = PhoneFormatter.formatToInternational(phone);
            if (!PhoneFormatter.isValidPhone(formatted)) {
                phoneField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            } else {
                phoneField.setStyle("");
            }
        }
    }

    // ========== MÉTHODES DE SÉLECTION ==========

    @FXML
    private void selectEmailMethod() {
        currentMethod = "EMAIL";
        methodLabel.setText("📧 Méthode choisie : Email");
        methodLabel.setStyle("-fx-text-fill: #5FB49C; -fx-font-weight: bold;");

        step1Box.setVisible(false);
        step1Box.setManaged(false);
        emailStepBox.setVisible(true);
        emailStepBox.setManaged(true);
        smsStepBox.setVisible(false);
        smsStepBox.setManaged(false);
    }

    @FXML
    private void selectSmsMethod() {
        currentMethod = "SMS";
        methodLabel.setText("📱 Méthode choisie : SMS");
        methodLabel.setStyle("-fx-text-fill: #5FB49C; -fx-font-weight: bold;");

        step1Box.setVisible(false);
        step1Box.setManaged(false);
        smsStepBox.setVisible(true);
        smsStepBox.setManaged(true);
        emailStepBox.setVisible(false);
        emailStepBox.setManaged(false);
    }

    // ========== MÉTHODES D'ENVOI ==========

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

                            // Passer à l'étape 2
                            emailStepBox.setVisible(false);
                            emailStepBox.setManaged(false);
                            codeStepBox.setVisible(true);
                            codeStepBox.setManaged(true);

                            timeLeft = 900; // 15 minutes
                            startTimer();
                            resendCodeBtn.setDisable(false);
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
    private void handleSendSmsCode() {
        if (isProcessing) return;

        String rawPhone = phoneField.getText().trim();
        String formattedPhone = PhoneFormatter.formatToInternational(rawPhone);

        if (!PhoneFormatter.isValidPhone(formattedPhone)) {
            showAlert(Alert.AlertType.WARNING, "Téléphone invalide",
                    "Veuillez entrer un numéro de téléphone valide (ex: +21650123456)");
            return;
        }

        isProcessing = true;
        loadingIndicator.setVisible(true);
        sendSmsCodeBtn.setDisable(true);
        statusLabel.setText("⏳ Vérification du numéro...");

        new Thread(() -> {
            boolean exists = userExistsByPhone(formattedPhone);

            javafx.application.Platform.runLater(() -> {
                if (!exists) {
                    loadingIndicator.setVisible(false);
                    sendSmsCodeBtn.setDisable(false);
                    statusLabel.setText("❌ Numéro non trouvé");
                    showAlert(Alert.AlertType.ERROR, "Compte introuvable",
                            "Aucun compte n'est associé à ce numéro de téléphone.");
                    isProcessing = false;
                    return;
                }

                currentPhone = formattedPhone;
                statusLabel.setText("⏳ Envoi du code SMS...");

                new Thread(() -> {
                    boolean sent = twilioService.sendVerificationCode(formattedPhone);

                    javafx.application.Platform.runLater(() -> {
                        loadingIndicator.setVisible(false);
                        isProcessing = false;

                        if (sent) {
                            statusLabel.setStyle("-fx-text-fill: green;");
                            statusLabel.setText("✅ Code envoyé au " +
                                    PhoneFormatter.maskPhone(formattedPhone));

                            // Passer à l'étape 2
                            smsStepBox.setVisible(false);
                            smsStepBox.setManaged(false);
                            codeStepBox.setVisible(true);
                            codeStepBox.setManaged(true);

                            timeLeft = 300; // 5 minutes
                            startTimer();
                            resendCodeBtn.setDisable(false);
                        } else {
                            sendSmsCodeBtn.setDisable(false);
                            statusLabel.setStyle("-fx-text-fill: red;");
                            statusLabel.setText("❌ Échec de l'envoi.");
                        }
                    });
                }).start();
            });
        }).start();
    }

    // ========== VÉRIFICATION DU CODE ==========

    @FXML
    private void handleVerifyCode() {
        verifyCode();
    }

    private void verifyCode() {
        String enteredCode = codeField.getText().trim();

        if (enteredCode.isEmpty() || enteredCode.length() != 6) {
            showAlert(Alert.AlertType.WARNING, "Code invalide",
                    "Veuillez entrer le code à 6 chiffres.");
            return;
        }

        loadingIndicator.setVisible(true);
        verifyCodeBtn.setDisable(true);

        new Thread(() -> {
            boolean valid = false;

            if ("EMAIL".equals(currentMethod)) {
                valid = enteredCode.equals(generatedCode);
            } else if ("SMS".equals(currentMethod)) {
                valid = twilioService.verifyCode(currentPhone, enteredCode);
            }

            final boolean isValid = valid;

            javafx.application.Platform.runLater(() -> {
                loadingIndicator.setVisible(false);
                verifyCodeBtn.setDisable(false);

                if (isValid) {
                    stopTimer();
                    statusLabel.setStyle("-fx-text-fill: green;");
                    statusLabel.setText("✅ Code vérifié !");

                    // Passer à l'étape 3
                    codeStepBox.setVisible(false);
                    codeStepBox.setManaged(false);
                    passwordStepBox.setVisible(true);
                    passwordStepBox.setManaged(true);
                } else {
                    statusLabel.setStyle("-fx-text-fill: red;");
                    statusLabel.setText("❌ Code incorrect");
                    showAlert(Alert.AlertType.ERROR, "Code incorrect",
                            "Le code saisi n'est pas valide.");
                }
            });
        }).start();
    }

    // ========== RÉINITIALISATION DU MOT DE PASSE ==========

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
            boolean updated = false;

            if ("EMAIL".equals(currentMethod)) {
                updated = updatePassword(userEmail, newPass);
            } else if ("SMS".equals(currentMethod)) {
                updated = updatePasswordByPhone(currentPhone, newPass);
            }

            final boolean isUpdated = updated;

            javafx.application.Platform.runLater(() -> {
                loadingIndicator.setVisible(false);
                resetPasswordBtn.setDisable(false);
                isProcessing = false;

                if (isUpdated) {
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
                            "Impossible de mettre à jour le mot de passe.");
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
                if ("EMAIL".equals(currentMethod)) {
                    handleSendCode();
                } else if ("SMS".equals(currentMethod)) {
                    handleSendSmsCode();
                }
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

    // ========== MÉTHODES UTILITAIRES ==========

    private String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    private void startTimer() {
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
                        resendCodeBtn.setDisable(false);
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

    // ========== MÉTHODES BASE DE DONNÉES ==========

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
        String hashedPassword = PasswordUtils.hashPassword(newPassword);

        try {
            conn = database.getConnection();
            if (conn == null) return false;

            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, hashedPassword);
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
    private boolean userExistsByPhone(String phone) {
        // Enlever l'indicatif et convertir en int
        String phoneWithoutCode = phone.replace("+216", "");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            int phoneNumber = Integer.parseInt(phoneWithoutCode);

            // Utiliser l'instance stockée (comme dans userExists)
            conn = database.getConnection();
            if (conn == null) return false;

            String query = "SELECT id FROM users WHERE phone_number = ?";
            pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, phoneNumber);
            rs = pstmt.executeQuery();

            boolean exists = rs.next();

            if (exists) {
                System.out.println("✅ Téléphone trouvé: " + phoneNumber);
            } else {
                System.out.println("❌ Téléphone non trouvé: " + phoneNumber);
            }
            return exists;

        } catch (NumberFormatException e) {
            System.err.println("❌ Numéro invalide: " + phoneWithoutCode);
            return false;
        } catch (SQLException e) {
            System.err.println("❌ Erreur userExistsByPhone: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            // Ne fermez PAS la connexion ! Mais fermez les autres ressources
            try { if (rs != null) rs.close(); } catch (SQLException e) { }
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) { }
            // Ne fermez PAS conn.close() ici
        }
    }

    private boolean updatePasswordByPhone(String phone, String newPassword) {
        String phoneWithoutCode = phone.replace("+216", "");

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            int phoneNumber = Integer.parseInt(phoneWithoutCode);
            String hashedPassword = PasswordUtils.hashPassword(newPassword);

            // Utiliser l'instance stockée (comme dans updatePassword)
            conn = database.getConnection();
            if (conn == null) return false;

            String query = "UPDATE users SET password = ? WHERE phone_number = ?";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, hashedPassword);
            pstmt.setInt(2, phoneNumber);

            int rowsAffected = pstmt.executeUpdate();
            System.out.println("✅ Lignes modifiées (téléphone): " + rowsAffected);

            if (rowsAffected > 0) {
                System.out.println("✅ Mot de passe mis à jour pour téléphone: " + phoneNumber);
                return true;
            } else {
                System.out.println("❌ Aucune mise à jour pour téléphone: " + phoneNumber);
                return false;
            }

        } catch (NumberFormatException e) {
            System.err.println("❌ Numéro invalide: " + phoneWithoutCode);
            return false;
        } catch (SQLException e) {
            System.err.println("❌ Erreur updatePasswordByPhone: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            // Ne fermez PAS la connexion ! Mais fermez pstmt
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