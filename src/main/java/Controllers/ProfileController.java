package Controllers;

import Models.*;
import Services.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ProfileController implements Initializable {

    // ========== ÉLÉMENTS FXML COMMUNS ==========
    @FXML private Label profileTitleLabel;
    @FXML private Label fullNameLabel;
    @FXML private Label emailLabel;
    @FXML private Label roleLabel;
    @FXML private Label ageLabel;
    @FXML private Label genderLabel;
    @FXML private Label phoneLabel;
    @FXML private Label birthDateLabel;
    @FXML private Label statusLabel;
    @FXML private Button closeButton;

    // ========== ÉLÉMENTS SPÉCIFIQUES PATIENT ==========
    @FXML private VBox patientFields;
    @FXML private Label bloodTypeLabel;
    @FXML private Label weightLabel;
    @FXML private Label heightLabel;

    // ========== ÉLÉMENTS SPÉCIFIQUES DOCTEUR ==========
    @FXML private VBox doctorFields;
    @FXML private Label specialtyLabel;
    @FXML private Label experienceLabel;
    @FXML private Label diplomaLabel;
    @FXML private Label priceLabel;
    @FXML private Label availabilityLabel;
    @FXML private Label doctorStatusLabel;

    // ========== ÉLÉMENTS SPÉCIFIQUES ADMIN ==========
    @FXML private VBox adminFields;
    @FXML private Label adminStatusLabel;

    // ========== SERVICES ==========
    private final ServiceUser serviceUser = new ServiceUser();
    private final ServicePatient servicePatient = new ServicePatient();
    private final ServiceDoctor serviceDoctor = new ServiceDoctor();
    private final ServiceAdmin serviceAdmin = new ServiceAdmin();

    private users currentUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Cacher tous les champs spécifiques au début
        hideAllSpecificFields();
    }

    private void hideAllSpecificFields() {
        if (patientFields != null) {
            patientFields.setVisible(false);
            patientFields.setManaged(false);
        }
        if (doctorFields != null) {
            doctorFields.setVisible(false);
            doctorFields.setManaged(false);
        }
        if (adminFields != null) {
            adminFields.setVisible(false);
            adminFields.setManaged(false);
        }
    }

    public void setUser(users user) {
        this.currentUser = user;

        if (user == null) return;

        // Informations communes
        fullNameLabel.setText(user.getName() + " " + user.getSecond_name());
        emailLabel.setText(user.getEmail());
        roleLabel.setText(getRoleDisplay(user.getRole()));
        ageLabel.setText(user.getAge() > 0 ? String.valueOf(user.getAge()) : "Non renseigné");
        genderLabel.setText(user.getGender() != null && !user.getGender().isEmpty() ? user.getGender() : "Non renseigné");
        phoneLabel.setText(user.getPhone_number() > 0 ? String.valueOf(user.getPhone_number()) : "Non renseigné");
        birthDateLabel.setText(user.getBirth_date() != null && !user.getBirth_date().isEmpty() ? user.getBirth_date() : "Non renseigné");

        // Afficher les champs spécifiques selon le rôle
        loadRoleSpecificData(user);
    }

    private String getRoleDisplay(String role) {
        switch(role) {
            case "admin": return "👑 Administrateur";
            case "doctor": return "👨‍⚕️ Docteur";
            case "patient": return "🩺 Patient";
            default: return "👤 Utilisateur";
        }
    }

    private void loadRoleSpecificData(users user) {
        hideAllSpecificFields();

        try {
            switch(user.getRole()) {
                case "patient":
                    loadPatientData(user.getId());
                    profileTitleLabel.setText("👤 Profil Patient");
                    profileTitleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #5FB49C;");
                    patientFields.setVisible(true);
                    patientFields.setManaged(true);
                    break;

                case "doctor":
                    loadDoctorData(user.getId());
                    profileTitleLabel.setText("👨‍⚕️ Profil Docteur");
                    profileTitleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #E667AF;");
                    doctorFields.setVisible(true);
                    doctorFields.setManaged(true);
                    break;

                case "admin":
                    loadAdminData(user.getId());
                    profileTitleLabel.setText("👑 Profil Administrateur");
                    profileTitleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #9B59B6;");
                    adminFields.setVisible(true);
                    adminFields.setManaged(true);
                    break;
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les données spécifiques: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadPatientData(int userId) throws SQLException {
        patient patient = servicePatient.getById(userId);
        if (patient != null) {
            bloodTypeLabel.setText(patient.getBlood_type() != null ? patient.getBlood_type() : "Non renseigné");
            weightLabel.setText(patient.getWeight() > 0 ? patient.getWeight() + " kg" : "Non renseigné");
            heightLabel.setText(patient.getHeight() > 0 ? patient.getHeight() + " cm" : "Non renseigné");

            // Statut (actif si non bloqué)
            boolean isBlocked = serviceUser.isUserBlocked(userId);
            statusLabel.setText(isBlocked ? "🔴 Bloqué" : "🟢 Actif");
            statusLabel.setStyle(isBlocked ? "-fx-text-fill: #c62828;" : "-fx-text-fill: #2e7d32;");
        }
    }

    private void loadDoctorData(int userId) throws SQLException {
        doctor doctor = serviceDoctor.getById(userId);
        if (doctor != null) {
            specialtyLabel.setText(doctor.getSpecialty() != null ? doctor.getSpecialty() : "Non renseigné");
            experienceLabel.setText(doctor.getExperience() > 0 ? doctor.getExperience() + " ans" : "Non renseigné");
            diplomaLabel.setText(doctor.getDiplome() != null ? doctor.getDiplome() : "Non renseigné");
            priceLabel.setText(doctor.getTarifConsultation() > 0 ? doctor.getTarifConsultation() + " DT" : "Non renseigné");

            boolean isAvailable = doctor.isDisponible();
            availabilityLabel.setText(isAvailable ? "✅ Disponible" : "❌ Non disponible");
            availabilityLabel.setStyle(isAvailable ? "-fx-text-fill: #2e7d32;" : "-fx-text-fill: #c62828;");

            boolean isActive = doctor.isActif();
            doctorStatusLabel.setText(isActive ? "🟢 Actif" : "🔴 Inactif");
            doctorStatusLabel.setStyle(isActive ? "-fx-text-fill: #2e7d32;" : "-fx-text-fill: #c62828;");

            // Statut général (bloqué ou non)
            boolean isBlocked = serviceUser.isUserBlocked(userId);
            statusLabel.setText(isBlocked ? "🔴 Bloqué" : "🟢 Actif");
            statusLabel.setStyle(isBlocked ? "-fx-text-fill: #c62828;" : "-fx-text-fill: #2e7d32;");
        }
    }

    private void loadAdminData(int userId) throws SQLException {
        admin admin = serviceAdmin.getById(userId);
        if (admin != null) {
            boolean isActive = admin.isActif();
            adminStatusLabel.setText(isActive ? "🟢 Actif" : "🔴 Inactif");
            adminStatusLabel.setStyle(isActive ? "-fx-text-fill: #2e7d32;" : "-fx-text-fill: #c62828;");

            // Statut général (bloqué ou non)
            boolean isBlocked = serviceUser.isUserBlocked(userId);
            statusLabel.setText(isBlocked ? "🔴 Bloqué" : "🟢 Actif");
            statusLabel.setStyle(isBlocked ? "-fx-text-fill: #c62828;" : "-fx-text-fill: #2e7d32;");
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}