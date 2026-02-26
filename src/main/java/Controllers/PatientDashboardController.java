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
    @FXML private Button homeButton;
    private users loggedInUser;

    private patient currentPatient;
    private users currentUser; // Pour les utilisateurs Google
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

    /**
     * Pour les patients normaux (avec toutes les données médicales)
     */
    public void setPatientData(patient patient) {
        this.currentPatient = patient;
        this.currentUser = null;

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

            System.out.println("✅ Patient data loaded: " + patient.getEmail());
        }
    }

    /**
     * Pour les utilisateurs Google (données limitées)
     */
    public void setUser(users user) {
        this.currentUser = user;
        this.currentPatient = null;

        System.out.println("✅ Utilisateur Google reçu dans dashboard: " + user.getEmail());
        System.out.println("   Name: '" + user.getName() + "'");
        System.out.println("   Second name: '" + user.getSecond_name() + "'");
        System.out.println("   Age: " + user.getAge());
        System.out.println("   Gender: '" + user.getGender() + "'");

        // Afficher les informations disponibles
        String fullName;
        if (user.getName() != null && !user.getName().isEmpty()) {
            fullName = user.getName() + " " + (user.getSecond_name() != null ? user.getSecond_name() : "");
        } else {
            // Si pas de nom, utiliser l'email
            fullName = user.getEmail().split("@")[0];
        }

        patientNameLabel.setText(fullName.trim());
        welcomeLabel.setText("Welcome, " + fullName.trim() + "!");
        fullNameLabel.setText(fullName.trim());
        emailLabel.setText(user.getEmail());

        // Gestion correcte des valeurs
        if (user.getAge() > 0) {
            ageLabel.setText(String.valueOf(user.getAge()));
        } else {
            ageLabel.setText("Non renseigné");
        }

        if (user.getGender() != null && !user.getGender().isEmpty() && !user.getGender().equals("0")) {
            genderLabel.setText(user.getGender());
        } else {
            genderLabel.setText("Non renseigné");
        }

        if (user.getPhone_number() > 0) {
            phoneLabel.setText(String.valueOf(user.getPhone_number()));
        } else {
            phoneLabel.setText("Non renseigné");
        }

        if (user.getBirth_date() != null && !user.getBirth_date().isEmpty()) {
            birthDateLabel.setText(user.getBirth_date());
        } else {
            birthDateLabel.setText("Non renseigné");
        }

        // Informations médicales (toujours vides pour Google)
        bloodTypeLabel.setText("À compléter");
        weightLabel.setText("À compléter");
        heightLabel.setText("À compléter");

        // Load doctors
        loadDoctors();
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
    @FXML
    private void handleHome() {
        System.out.println("\n=== Retour à l'accueil ===");

        if (loggedInUser == null) {
            System.err.println("❌ loggedInUser est null - tentative de récupération");

            // Tentative de récupération si loggedInUser est null
            if (currentPatient != null) {
                try {
                    ServiceUser serviceUser = new ServiceUser();
                    loggedInUser = serviceUser.getById(currentPatient.getId());
                    System.out.println("✅ loggedInUser récupéré depuis currentPatient: " + loggedInUser.getEmail());
                } catch (SQLException e) {
                    showAlert("Erreur", "Impossible de récupérer les informations utilisateur");
                    e.printStackTrace();
                    return;
                }
            } else if (currentUser != null) {
                loggedInUser = currentUser;
                System.out.println("✅ loggedInUser récupéré depuis currentUser: " + loggedInUser.getEmail());
            } else {
                showAlert("Erreur", "Aucun utilisateur connecté");
                return;
            }
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/home.fxml"));
            Parent root = loader.load();

            HomeController homeController = loader.getController();
            homeController.setUser(loggedInUser);

            Stage stage = (Stage) homeButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Accueil - GrowMind");
            stage.show();

            System.out.println("✅ Retour à l'accueil réussi pour: " + loggedInUser.getEmail());

        } catch (IOException e) {
            System.err.println("❌ Erreur IO: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible de retourner à l'accueil: " + e.getMessage());
        }
    }

    private void loadDashboardData() {
        try {
            System.out.println("\n🔍 Chargement données dashboard patient");
            List<doctor> allDoctors = serviceDoctor.recuperer();
            doctorsListView.setItems(FXCollections.observableArrayList(allDoctors));
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
            int userId;
            if (currentPatient != null) {
                userId = currentPatient.getId();
                System.out.println("\n🔍 Édition profil patient - ID: " + userId);
            } else if (currentUser != null) {
                userId = currentUser.getId();
                System.out.println("\n🔍 Édition profil Google - ID: " + userId);
            } else {
                showAlert("Erreur", "Aucun utilisateur connecté");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edit_user.fxml"));
            Parent root = loader.load();

            EditUserController controller = loader.getController();

            ServiceUser serviceUser = new ServiceUser();
            users user = serviceUser.getById(userId);

            if (user != null) {
                System.out.println("✅ User trouvé: " + user.getName());
                controller.setUserData(user, "patient");

                controller.setOnSaveCallback(() -> {
                    // Recharger les données après modification
                    try {
                        if (currentPatient != null) {
                            currentPatient = servicePatient.getById(userId);
                            setPatientData(currentPatient);
                        } else {
                            // Pour Google, on recharge juste les infos
                            users updatedUser = serviceUser.getById(userId);
                            setUser(updatedUser);
                        }
                        loadDashboardData();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
            } else {
                System.out.println("❌ User non trouvé pour ID: " + userId);
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