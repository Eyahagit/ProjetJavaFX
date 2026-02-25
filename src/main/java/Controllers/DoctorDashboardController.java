package Controllers;

import Models.*;
import Services.*;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class DoctorDashboardController implements Initializable {

    @FXML private Label doctorNameLabel;
    @FXML private Label welcomeLabel;
    @FXML private Label dateLabel;
    @FXML private Label fullNameLabel;
    @FXML private Label emailLabel;
    @FXML private Label specialtyLabel;
    @FXML private Label experienceLabel;
    @FXML private Label diplomaLabel;
    @FXML private Label statusLabel;
    @FXML private Label totalPatientsLabel;
    @FXML private Label activePatientsLabel;
    @FXML private Label consultationsLabel;
    @FXML private Label TarifLabel;
    @FXML private ListView<patient> patientsListView;
    @FXML private Button logoutButton;
    @FXML private Button refreshBtn;
    @FXML private Button editProfileBtn;

    private doctor currentDoctor;
    private final ServiceUser serviceUser = new ServiceUser();
    private final ServiceDoctor serviceDoctor = new ServiceDoctor();
    private final ServicePatient servicePatient = new ServicePatient();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        setupPatientsList();
    }

    private void setupPatientsList() {
        patientsListView.setCellFactory(lv -> new ListCell<patient>() {
            @Override
            protected void updateItem(patient patient, boolean empty) {
                super.updateItem(patient, empty);
                if (empty || patient == null) {
                    setText(null);
                } else {
                    setText(patient.getName() + " " + patient.getSecond_name() +
                            " | Blood: " + patient.getBlood_type() +
                            " | " + patient.getWeight() + "kg, " + patient.getHeight() + "cm");
                }
            }
        });
    }

    public void setDoctorData(doctor doctor) {
        this.currentDoctor = doctor;
        if (doctor != null) {
            String fullName = "Dr. " + doctor.getName() + " " + doctor.getSecond_name();
            doctorNameLabel.setText(fullName);
            welcomeLabel.setText("Welcome, " + fullName + "!");
            fullNameLabel.setText(fullName);
            emailLabel.setText(doctor.getEmail());
            specialtyLabel.setText(doctor.getSpecialty());
            experienceLabel.setText(doctor.getExperience() + " years");
            diplomaLabel.setText(doctor.getDiplome());
            TarifLabel.setText(String.valueOf(doctor.getTarifConsultation()));

            // Status with color
            if (doctor.isActif()) {
                statusLabel.setText("🟢 Active");
                statusLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
            } else {
                statusLabel.setText("🔴 Inactive");
                statusLabel.setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
            }

            loadDashboardData();
        }
    }

    private void loadDashboardData() {
        try {
            System.out.println("\n🔍 Chargement données dashboard doctor");
            // Load all patients
            List<patient> allPatients = servicePatient.recuperer();
            totalPatientsLabel.setText(String.valueOf(allPatients.size()));

            // Compter les patients actifs (si tu as un champ actif)
            long activeCount = allPatients.size(); // Temporaire
            activePatientsLabel.setText(String.valueOf(activeCount));

            consultationsLabel.setText("0");
            patientsListView.setItems(FXCollections.observableArrayList(allPatients));

            System.out.println("✅ Dashboard chargé - Patients: " + allPatients.size());

        } catch (SQLException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            showAlert("Erreur", "Impossible de charger les données: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadDashboardData();
        showAlert("Success", "Dashboard refreshed!");
    }

    @FXML
    private void handleEditProfile(ActionEvent event) {
        try {
            System.out.println("\n🔍 Édition profil doctor - ID: " + currentDoctor.getId());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edit_user.fxml"));
            Parent root = loader.load();

            EditUserController controller = loader.getController();

            ServiceUser serviceUser = new ServiceUser();
            users user = serviceUser.getById(currentDoctor.getId());

            if (user != null) {
                System.out.println("✅ User trouvé: " + user.getName());
                controller.setUserData(user, "doctor");

                controller.setOnSaveCallback(() -> {
                    // ✅ Recharger les données sans boucle infinie
                    try {
                        currentDoctor = serviceDoctor.getById(currentDoctor.getId());
                        setDoctorData(currentDoctor);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
            } else {
                System.out.println("❌ User non trouvé pour ID: " + currentDoctor.getId());
                showAlert("Erreur", "Utilisateur non trouvé");
                return;
            }

            Stage stage = new Stage();
            stage.setTitle("Edit Profile");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException | SQLException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/log_in.fxml"))));
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Cannot load login page: " + e.getMessage());
        }
    }
    public void setUser(users user) {
        System.out.println("✅ Docteur connecté: " + user.getEmail());
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}