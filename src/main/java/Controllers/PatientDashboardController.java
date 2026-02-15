package Controllers;

import Models.*;
import Services.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

public class PatientDashboardController implements Initializable {

    @FXML private Label patientNameLabel;
    @FXML private Label welcomeLabel;
    @FXML private Label fullNameLabel;
    @FXML private Label emailLabel;
    @FXML private Label ageLabel;
    @FXML private Label genderLabel;
    @FXML private Label phoneLabel;
    @FXML private Label birthDateLabel;
    @FXML private Label bloodTypeLabel;
    @FXML private Label weightLabel;
    @FXML private Label heightLabel;
    @FXML private Label dateLabel;

    @FXML private ListView<doctor> doctorsListView;
    @FXML private Button logoutButton;
    @FXML private Button newAppointmentBtn;

    private patient currentPatient;
    private final ServicePatient servicePatient = new ServicePatient();
    private final ServiceDoctor serviceDoctor = new ServiceDoctor();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set current date
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        dateLabel.setText(now.format(formatter));

        // Setup doctors list
        setupDoctorsList();
    }

    private void setupDoctorsList() {
        doctorsListView.setCellFactory(lv -> new ListCell<doctor>() {
            @Override
            protected void updateItem(doctor doctor, boolean empty) {
                super.updateItem(doctor, empty);
                if (empty || doctor == null) {
                    setText(null);
                } else {
                    setText(doctor.getName() + " " + doctor.getSecond_name() + " - " + doctor.getSpecialty());
                }
            }
        });
    }

    public void setPatientData(patient patient) {
        this.currentPatient = patient;
        if (patient != null) {
            // Update labels
            String fullName = patient.getName() + " " + patient.getSecond_name();
            patientNameLabel.setText(fullName);
            welcomeLabel.setText("Welcome, " + fullName + "!");
            fullNameLabel.setText(fullName);
            emailLabel.setText(patient.getEmail());
            ageLabel.setText(String.valueOf(patient.getAge()));
            genderLabel.setText(patient.getGender());
            phoneLabel.setText(String.valueOf(patient.getPhone_number()));
            birthDateLabel.setText(patient.getBirth_date());
            bloodTypeLabel.setText(patient.getBlood_type());
            weightLabel.setText(String.valueOf(patient.getWeight()) + " kg");
            heightLabel.setText(String.valueOf(patient.getHeight()) + " cm");

            // Load doctors
            loadDoctors();
        }
    }

    private void loadDoctors() {
        try {
            List<doctor> doctors = serviceDoctor.recuperer();
            doctorsListView.setItems(FXCollections.observableArrayList(doctors));
            System.out.println("✅ Doctors loaded: " + doctors.size());
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur chargement docteurs: " + e.getMessage());
            e.printStackTrace();
        }
    }



    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/log_in.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login - GrowMind");
            stage.show();
            System.out.println("✅ Logout successful");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page de connexion: " + e.getMessage());
        }
    }
    private void loadDashboardData() {
        try {
            System.out.println("\n🔍 Chargement données dashboard patient");

            // Charger les docteurs disponibles
            List<doctor> allDoctors = serviceDoctor.recuperer();
            doctorsListView.setItems(FXCollections.observableArrayList(allDoctors));

            // Mettre à jour les statistiques (si tu veux)
            // Par exemple: nombre de docteurs disponibles
            // totalDoctorsLabel.setText(String.valueOf(allDoctors.size()));

            System.out.println("✅ Dashboard chargé - Docteurs disponibles: " + allDoctors.size());

        } catch (SQLException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            showAlert("Erreur", "Impossible de charger les données: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadDashboardData();
        showAlert("Succès", "Dashboard rafraîchi !");
    }

    @FXML
    private void handleEditProfile(ActionEvent event) {
        try {
            System.out.println("\n🔍 Édition profil patient - ID: " + currentPatient.getId());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edit_user.fxml"));
            Parent root = loader.load();

            EditUserController controller = loader.getController();

            ServiceUser serviceUser = new ServiceUser();
            users user = serviceUser.getById(currentPatient.getId());

            if (user != null) {
                System.out.println("✅ User trouvé: " + user.getName());
                controller.setUserData(user, "patient");

                controller.setOnSaveCallback(() -> {
                    // Recharger les données après modification
                    try {
                        currentPatient = servicePatient.getById(currentPatient.getId());
                        setPatientData(currentPatient);
                        loadDashboardData(); // Recharger la liste des docteurs
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
            } else {
                System.out.println("❌ User non trouvé pour ID: " + currentPatient.getId());
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
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}