package Controllers;

import Models.users;
import Models.admin;
import Models.doctor;
import Models.patient;
import Services.ServiceUser;
import Services.ServiceAdmin;
import Services.ServiceDoctor;
import Services.ServicePatient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberCheckBox;
    @FXML private Hyperlink forgotPasswordLink;
    @FXML private Button loginButton;
    @FXML private Hyperlink signUpLink;

    private final ServiceUser serviceUser = new ServiceUser();
    private final ServiceAdmin serviceAdmin = new ServiceAdmin();
    private final ServiceDoctor serviceDoctor = new ServiceDoctor();
    private final ServicePatient servicePatient = new ServicePatient();

    private users currentUser;


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
            // VÉRIFIER SI L'UTILISATEUR EXISTE DANS LA BASE
            users user = serviceUser.getByEmail(email);

            if (user != null && user.getPassword().equals(password)) {
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
        // Charger le FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin_dashboard.fxml"));
        Parent root = loader.load();

        // Récupérer le controller
        AdminDashboardController adminController = loader.getController();

        // Récupérer l'admin complet avec ses informations
        admin adminUser = serviceAdmin.getById(currentUser.getId());

        // Passer les données au dashboard
        if (adminUser != null) {
            adminController.setAdminData(adminUser);
            System.out.println("✅ Admin data loaded: " + adminUser.getName() + " " + adminUser.getSecond_name());
        } else {
            // Si pas trouvé dans table admin, créer un objet admin basique
            admin tempAdmin = new admin();
            tempAdmin.setId(currentUser.getId());
            tempAdmin.setName(currentUser.getName());
            tempAdmin.setSecond_name(currentUser.getSecond_name());
            tempAdmin.setEmail(currentUser.getEmail());
            tempAdmin.setRole(currentUser.getRole());
            tempAdmin.setActif(true);
            adminController.setAdminData(tempAdmin);
        }

        // Afficher la scène
        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Admin Dashboard - GrowMind");
        stage.show();
    }

    private void loadDoctorDashboard() throws IOException, SQLException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/doctor_dashboard.fxml"));
        Parent root = loader.load();

        // ✅ Récupérer le controller
        DoctorDashboardController doctorController = loader.getController();

        // ✅ Récupérer le docteur complet avec ses informations
        doctor doctorUser = serviceDoctor.getById(currentUser.getId());

        // ✅ Passer les données au dashboard
        doctorController.setDoctorData(doctorUser);

        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Doctor Dashboard - GrowMind");
        stage.show();
    }

    private void loadPatientDashboard() throws IOException, SQLException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/patient_dashboard.fxml"));
        Parent root = loader.load();

        // ✅ Récupérer le controller
        PatientDashboardController patientController = loader.getController();

        // ✅ Récupérer le patient complet avec ses informations
        patient patientUser = servicePatient.getById(currentUser.getId());

        // ✅ Passer les données au dashboard
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
            stage.setTitle("User Management - GrowMind");
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la page d'inscription: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleForgotPassword() {
        showAlert("Mot de passe oublié", "Veuillez contacter l'administrateur pour réinitialiser votre mot de passe.\nEmail: admin@growmind.com");
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