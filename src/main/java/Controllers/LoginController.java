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
            // ✅ NOUVEAU : Utiliser la méthode authenticate avec BCrypt
            users user = serviceUser.authenticate(email, password);

            if (user != null) {
                // CONNEXION RÉUSSIE
                System.out.println("✅ Login successful: " + user.getEmail() + " - Role: " + user.getRole());

                currentUser = user;

                // Sauvegarder la session si "Remember me" est coché
                if (rememberCheckBox.isSelected()) {
                    saveUserSession(user);
                }

                // Rediriger vers la page appropriée selon le rôle
                redirectToRolePage(user.getRole());

            } else {
                // CONNEXION ÉCHOUÉE
                showAlert("Erreur", "Email ou mot de passe incorrect");
                passwordField.clear();
            }

        } catch (SQLException e) {
            showAlert("Erreur", "Erreur de connexion à la base de données: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void redirectToRolePage(String role) {
        try {
            switch (role.toLowerCase()) {
                case "patient":
                    loadPatientDashboard();
                    break;
                case "doctor":
                    loadDoctorDashboard();
                    break;
                case "admin":
                    loadAdminDashboard();
                    break;
                default:
                    showAlert("Erreur", "Rôle non reconnu: " + role);
                    break;
            }
        } catch (IOException | SQLException e) {
            showAlert("Erreur", "Impossible de charger la page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadAdminDashboard() throws IOException, SQLException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin_dashboard.fxml"));
        Parent root = loader.load();

        AdminDashboardController adminController = loader.getController();

        admin adminUser = serviceAdmin.getById(currentUser.getId());

        if (adminUser != null) {
            adminController.setAdminData(adminUser);
            System.out.println("✅ Admin data loaded: " + adminUser.getName() + " " + adminUser.getSecond_name());
        } else {
            admin tempAdmin = new admin();
            tempAdmin.setId(currentUser.getId());
            tempAdmin.setName(currentUser.getName());
            tempAdmin.setSecond_name(currentUser.getSecond_name());
            tempAdmin.setEmail(currentUser.getEmail());
            tempAdmin.setRole(currentUser.getRole());
            tempAdmin.setActif(true);
            adminController.setAdminData(tempAdmin);
        }

        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Admin Dashboard - GrowMind");
        stage.show();
    }

    private void loadDoctorDashboard() throws IOException, SQLException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/doctor_dashboard.fxml"));
        Parent root = loader.load();

        DoctorDashboardController doctorController = loader.getController();

        doctor doctorUser = serviceDoctor.getById(currentUser.getId());
        doctorController.setDoctorData(doctorUser);

        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Doctor Dashboard - GrowMind");
        stage.show();
    }

    private void loadPatientDashboard() throws IOException, SQLException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/patient_dashboard.fxml"));
        Parent root = loader.load();

        PatientDashboardController patientController = loader.getController();

        patient patientUser = servicePatient.getById(currentUser.getId());
        patientController.setPatientData(patientUser);

        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Patient Dashboard - GrowMind");
        stage.show();
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
        goToForgetPassword(); // Rediriger vers la même méthode que le lien
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
                        redirectBasedOnRole(user);
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

    private void redirectBasedOnRole(users user) {
        try {
            String fxmlFile;
            String title;

            switch(user.getRole()) {
                case "admin":
                    fxmlFile = "/admin_dashboard.fxml";
                    title = "Admin Dashboard";
                    break;
                case "doctor":
                    fxmlFile = "/doctor_dashboard.fxml";
                    title = "Doctor Dashboard";
                    break;
                case "patient":
                default:
                    fxmlFile = "/patient_dashboard.fxml";
                    title = "Patient Dashboard";
                    break;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            // Passer l'utilisateur au controller suivant
            Object controller = loader.getController();
            if (controller instanceof PatientDashboardController) {
                ((PatientDashboardController) controller).setUser(user);
            } else if (controller instanceof DoctorDashboardController) {
                ((DoctorDashboardController) controller).setUser(user);
            } else if (controller instanceof AdminDashboardController) {
                ((AdminDashboardController) controller).setUser(user);
            }

            Stage stage = (Stage) googleLoginBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le tableau de bord.");
        }
    }

    private void saveUserSession(users user) {
        // TODO: Sauvegarder la session (Préférences, fichier, etc.)
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