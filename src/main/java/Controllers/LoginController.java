package Controllers;

import Models.users;
import Models.admin;
import Models.doctor;
import Models.patient;
import Services.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import utils.GoogleAuthUtil;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberCheckBox;
    @FXML private Hyperlink forgotPasswordLink;
    @FXML private Button loginButton;
    @FXML private Hyperlink signUpLink;
    @FXML private Button googleLoginBtn;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label statusLabel;

    private final ServiceUser serviceUser = new ServiceUser();
    private final ServiceAdmin serviceAdmin = new ServiceAdmin();
    private final ServiceDoctor serviceDoctor = new ServiceDoctor();
    private final ServicePatient servicePatient = new ServicePatient();

    private users currentUser;
    private GoogleAuthService googleAuthService;

    @FXML
    public void initialize() {
        // Initialisation du service Google Auth
        googleAuthService = GoogleAuthService.getInstance();

        // Cacher les indicateurs de chargement au démarrage
        loadingIndicator.setVisible(false);
        statusLabel.setText("");

        System.out.println("✅ LoginController initialisé");
    }

    @FXML
    private void handleLogin() {
        // Validation des champs
        if (emailField.getText().isEmpty() || passwordField.getText().isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs");
            return;
        }

        String email = emailField.getText();
        String password = passwordField.getText();

        try {
            // Utiliser la méthode authenticate avec BCrypt
            users user = serviceUser.authenticate(email, password);

            if (user != null) {
                // CONNEXION RÉUSSIE
                System.out.println("✅ Login successful: " + user.getEmail() + " - Role: " + user.getRole());

                currentUser = user;

                // Sauvegarder la session si "Remember me" est coché
                if (rememberCheckBox.isSelected()) {
                    saveUserSession(user);
                }

                // ✅ REDIRIGER VERS LA PAGE D'ACCUEIL (HOME)
                goToHome();

            } else {
                // CONNEXION ÉCHOUÉE
                showAlert("Erreur", "Email ou mot de passe incorrect");
                passwordField.clear();
            }

        } catch (SQLException e) {
            // Gestion spéciale pour compte bloqué
            if (e.getMessage() != null && e.getMessage().contains("bloqué")) {
                showAlert("⛔ Compte bloqué", e.getMessage());
            } else {
                showAlert("Erreur", "Erreur de connexion à la base de données: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }

    // ✅ NOUVELLE MÉTHODE POUR ALLER VERS HOME
    private void goToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/home.fxml"));
            Parent root = loader.load();

            // Passer l'utilisateur au HomeController
            HomeController homeController = loader.getController();
            homeController.setUser(currentUser);

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Accueil - GrowMind");
            stage.show();

            System.out.println("✅ Redirection vers Home réussie");

        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la page d'accueil: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void goToSignUp() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sign_in.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) signUpLink.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Inscription - GrowMind");
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la page d'inscription: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void goToForgetPassword() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ForgotPassword.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) forgotPasswordLink.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Mot de passe oublié - GrowMind");
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la page de mot de passe oublié: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleForgotPassword() {
        goToForgetPassword();
    }

    @FXML
    private void handleGoogleLogin() {
        loadingIndicator.setVisible(true);
        statusLabel.setText("⏳ Connexion avec Google...");

        new Thread(() -> {
            GoogleAuthUtil googleAuth = GoogleAuthUtil.getInstance();

            // Forcer le changement de compte
            googleAuth.forceAccountChoice();

            // Authentification avec port aléatoire
            boolean authSuccess = googleAuth.authenticate(true);

            if (authSuccess) {
                users user = googleAuthService.authenticateWithGoogle();

                javafx.application.Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);

                    if (user != null) {
                        statusLabel.setStyle("-fx-text-fill: green;");
                        statusLabel.setText("✅ Connecté: " + user.getEmail());

                        // ✅ REDIRIGER VERS HOME AUSSI POUR GOOGLE
                        currentUser = user;
                        goToHome();

                    } else {
                        statusLabel.setStyle("-fx-text-fill: red;");
                        statusLabel.setText("❌ Échec création utilisateur");
                        showAlert("Erreur", "Impossible de créer l'utilisateur dans la BD.");
                    }
                });
            } else {
                javafx.application.Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    statusLabel.setStyle("-fx-text-fill: red;");
                    statusLabel.setText("❌ Échec authentification Google");
                    showAlert("Erreur", "Impossible de se connecter avec Google.");
                });
            }
        }).start();
    }

    private void saveUserSession(users user) {
        System.out.println("💾 Session saved for: " + user.getEmail());
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}