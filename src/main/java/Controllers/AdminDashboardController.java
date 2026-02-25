package Controllers;

import Models.*;
import Services.*;
import javafx.application.Platform;
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
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Optional;

import utils.Database;
import javafx.geometry.Pos;
import javafx.scene.layout.Priority;

public class AdminDashboardController implements Initializable {
    private Connection connection;

    @FXML private Label adminNameLabel;
    @FXML private Label welcomeMessageLabel;
    @FXML private Label adminFullNameLabel;
    @FXML private Label adminEmailLabel;
    @FXML private Label adminStatusLabel;
    @FXML private Label dateLabel;

    // Statistics Labels
    @FXML private Label totalUsersLabel;
    @FXML private Label totalPatientsLabel;
    @FXML private Label totalDoctorsLabel;
    @FXML private Label totalAdminsLabel;

    // Active/Inactive Labels
    @FXML private Label activePatientsLabel;
    @FXML private Label inactivePatientsLabel;
    @FXML private Label activeDoctorsLabel;
    @FXML private Label inactiveDoctorsLabel;
    @FXML private Label activeAdminsLabel;
    @FXML private Label inactiveAdminsLabel;

    // List Views
    @FXML private ListView<doctor> doctorsListView;
    @FXML private ListView<patient> patientsListView;
    @FXML private ListView<admin> adminsListView;

    @FXML private Button logoutButton;
    @FXML private Button manageUsersBtn;
    @FXML private Button managePatientsBtn;
    @FXML private Button manageDoctorsBtn;
    @FXML private Button reportsBtn;
    @FXML private Button settingsBtn;

    private final ServiceUser serviceUser = new ServiceUser();
    private final ServicePatient servicePatient = new ServicePatient();
    private final ServiceDoctor serviceDoctor = new ServiceDoctor();
    private final ServiceAdmin serviceAdmin = new ServiceAdmin();

    private admin currentAdmin;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Set current date
            LocalDate now = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            dateLabel.setText(now.format(formatter));

            // Configure ListViews
            setupListViews();

            // Charger les données
            loadDashboardData();

        } catch (SQLException e) {
            System.err.println("❌ ERREUR: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Erreur de chargement: " + e.getMessage());
        }
    }

    private void setupListViews() {
        // Custom cell factory for doctors list avec boutons de blocage
        doctorsListView.setCellFactory(lv -> new ListCell<doctor>() {
            private final HBox content = new HBox(10);
            private final Label nameLabel = new Label();
            private final Label specialtyLabel = new Label();
            private final Label statusLabel = new Label();
            private final Button blockBtn = new Button("🔒 Bloquer");
            private final Button unblockBtn = new Button("🔓 Débloquer");
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final HBox buttonsBox = new HBox(5);

            {
                // Style des boutons
                blockBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11px; -fx-background-radius: 5; -fx-padding: 3 8; -fx-cursor: hand;");
                unblockBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 11px; -fx-background-radius: 5; -fx-padding: 3 8; -fx-cursor: hand;");
                editBtn.setStyle("-fx-background-color: #FFA500; -fx-text-fill: white; -fx-font-size: 12px; -fx-background-radius: 5; -fx-padding: 3 8; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #FF4444; -fx-text-fill: white; -fx-font-size: 12px; -fx-background-radius: 5; -fx-padding: 3 8; -fx-cursor: hand;");

                content.getChildren().addAll(nameLabel, specialtyLabel, statusLabel, buttonsBox);
                content.setAlignment(Pos.CENTER_LEFT);
                content.setSpacing(15);

                nameLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 180;");
                specialtyLabel.setStyle("-fx-text-fill: #666; -fx-min-width: 150;");
                statusLabel.setStyle("-fx-min-width: 100;");

                HBox.setHgrow(buttonsBox, Priority.ALWAYS);
                buttonsBox.setAlignment(Pos.CENTER_RIGHT);
            }

            @Override
            protected void updateItem(doctor doctor, boolean empty) {
                super.updateItem(doctor, empty);
                if (empty || doctor == null) {
                    setGraphic(null);
                } else {
                    nameLabel.setText(doctor.getName() + " " + doctor.getSecond_name());
                    specialtyLabel.setText(doctor.getSpecialty());

                    // Vérifier le statut de blocage
                    try {
                        users user = serviceUser.getById(doctor.getId());
                        boolean isBlocked = user != null && user.isBlocked();

                        statusLabel.setText(isBlocked ? "🔴 Bloqué" : (doctor.isActif() ? "🟢 Actif" : "🟡 Inactif"));
                        statusLabel.setStyle(isBlocked ? "-fx-text-fill: #c62828; -fx-font-weight: bold;" :
                                (doctor.isActif() ? "-fx-text-fill: #2e7d32; -fx-font-weight: bold;" :
                                        "-fx-text-fill: #ff9800; -fx-font-weight: bold;"));

                        // Vider et reconstruire les boutons
                        buttonsBox.getChildren().clear();

                        if (isBlocked) {
                            unblockBtn.setOnAction(event -> handleUnblockUser(doctor.getId(), doctor.getName()));
                            buttonsBox.getChildren().addAll(unblockBtn, editBtn, deleteBtn);
                        } else {
                            blockBtn.setOnAction(event -> handleBlockUser(doctor.getId(), doctor.getName()));
                            buttonsBox.getChildren().addAll(blockBtn, editBtn, deleteBtn);
                        }

                    } catch (SQLException e) {
                        statusLabel.setText("⚠️ Erreur");
                        buttonsBox.getChildren().clear();
                        buttonsBox.getChildren().addAll(editBtn, deleteBtn);
                    }

                    editBtn.setOnAction(event -> handleEditDoctor(doctor));
                    deleteBtn.setOnAction(event -> handleDeleteDoctor(doctor));

                    setGraphic(content);
                }
            }
        });

        // Custom cell factory for patients list avec boutons de blocage
        patientsListView.setCellFactory(lv -> new ListCell<patient>() {
            private final HBox content = new HBox(10);
            private final Label nameLabel = new Label();
            private final Label bloodLabel = new Label();
            private final Label statusLabel = new Label();
            private final Button blockBtn = new Button("🔒 Bloquer");
            private final Button unblockBtn = new Button("🔓 Débloquer");
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final HBox buttonsBox = new HBox(5);

            {
                blockBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11px; -fx-background-radius: 5; -fx-padding: 3 8; -fx-cursor: hand;");
                unblockBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 11px; -fx-background-radius: 5; -fx-padding: 3 8; -fx-cursor: hand;");
                editBtn.setStyle("-fx-background-color: #FFA500; -fx-text-fill: white; -fx-font-size: 12px; -fx-background-radius: 5; -fx-padding: 3 8; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #FF4444; -fx-text-fill: white; -fx-font-size: 12px; -fx-background-radius: 5; -fx-padding: 3 8; -fx-cursor: hand;");

                content.getChildren().addAll(nameLabel, bloodLabel, statusLabel, buttonsBox);
                content.setAlignment(Pos.CENTER_LEFT);
                content.setSpacing(15);

                nameLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 180;");
                bloodLabel.setStyle("-fx-text-fill: #666; -fx-min-width: 100;");
                statusLabel.setStyle("-fx-min-width: 100;");

                HBox.setHgrow(buttonsBox, Priority.ALWAYS);
                buttonsBox.setAlignment(Pos.CENTER_RIGHT);
            }

            @Override
            protected void updateItem(patient patient, boolean empty) {
                super.updateItem(patient, empty);
                if (empty || patient == null) {
                    setGraphic(null);
                } else {
                    nameLabel.setText(patient.getName() + " " + patient.getSecond_name());
                    bloodLabel.setText("Blood: " + patient.getBlood_type());

                    try {
                        users user = serviceUser.getById(patient.getId());
                        boolean isBlocked = user != null && user.isBlocked();

                        statusLabel.setText(isBlocked ? "🔴 Bloqué" : "🟢 Actif");
                        statusLabel.setStyle(isBlocked ? "-fx-text-fill: #c62828; -fx-font-weight: bold;" :
                                "-fx-text-fill: #2e7d32; -fx-font-weight: bold;");

                        buttonsBox.getChildren().clear();

                        if (isBlocked) {
                            unblockBtn.setOnAction(event -> handleUnblockUser(patient.getId(), patient.getName()));
                            buttonsBox.getChildren().addAll(unblockBtn, editBtn, deleteBtn);
                        } else {
                            blockBtn.setOnAction(event -> handleBlockUser(patient.getId(), patient.getName()));
                            buttonsBox.getChildren().addAll(blockBtn, editBtn, deleteBtn);
                        }

                    } catch (SQLException e) {
                        statusLabel.setText("⚠️ Erreur");
                        buttonsBox.getChildren().clear();
                        buttonsBox.getChildren().addAll(editBtn, deleteBtn);
                    }

                    editBtn.setOnAction(event -> handleEditPatient(patient));
                    deleteBtn.setOnAction(event -> handleDeletePatient(patient));

                    setGraphic(content);
                }
            }
        });

        // Custom cell factory for admins list (pas de blocage pour les admins)
        adminsListView.setCellFactory(lv -> new ListCell<admin>() {
            private final HBox content = new HBox(10);
            private final Label nameLabel = new Label();
            private final Label statusLabel = new Label();
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final HBox buttonsBox = new HBox(5);

            {
                editBtn.setStyle("-fx-background-color: #FFA500; -fx-text-fill: white; -fx-font-size: 12px; -fx-background-radius: 5; -fx-padding: 3 8; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #FF4444; -fx-text-fill: white; -fx-font-size: 12px; -fx-background-radius: 5; -fx-padding: 3 8; -fx-cursor: hand;");

                buttonsBox.getChildren().addAll(editBtn, deleteBtn);
                content.getChildren().addAll(nameLabel, statusLabel, buttonsBox);
                content.setAlignment(Pos.CENTER_LEFT);
                content.setSpacing(15);

                nameLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 250;");
                statusLabel.setStyle("-fx-min-width: 100;");

                HBox.setHgrow(buttonsBox, Priority.ALWAYS);
                buttonsBox.setAlignment(Pos.CENTER_RIGHT);
            }

            @Override
            protected void updateItem(admin admin, boolean empty) {
                super.updateItem(admin, empty);
                if (empty || admin == null) {
                    setGraphic(null);
                } else {
                    nameLabel.setText(admin.getName() + " " + admin.getSecond_name());
                    statusLabel.setText(admin.isActif() ? "🟢 Actif" : "🔴 Inactif");
                    statusLabel.setStyle(admin.isActif() ?
                            "-fx-text-fill: #2e7d32; -fx-font-weight: bold;" :
                            "-fx-text-fill: #c62828; -fx-font-weight: bold;");

                    editBtn.setOnAction(event -> handleEditAdmin(admin));
                    deleteBtn.setOnAction(event -> handleDeleteAdmin(admin));

                    setGraphic(content);
                }
            }
        });
    }

    // ========== NOUVELLES MÉTHODES POUR LE BLOCAGE ==========

    /**
     * Gère le blocage d'un utilisateur
     */
    private void handleBlockUser(int userId, String userName) {
        try {
            boolean blocked = serviceUser.blockUser(userId);
            if (blocked) {
                showAlert("Succès", "Utilisateur bloqué avec succès");
                refreshData();
            } else {
                showAlert("Erreur", "Échec du blocage");
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du blocage: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleUnblockUser(int userId, String userName) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("🔓 Débloquer l'utilisateur");
        confirm.setHeaderText("Débloquer: " + userName);
        confirm.setContentText("Voulez-vous vraiment débloquer cet utilisateur ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean unblocked = serviceUser.unblockUser(userId);
                    if (unblocked) {
                        showAlert("Succès", "Utilisateur débloqué avec succès");
                        refreshData();
                    } else {
                        showAlert("Erreur", "Échec du déblocage");
                    }
                } catch (SQLException e) {
                    showAlert("Erreur", "Erreur lors du déblocage: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    // ========== CHARGEMENT DES DONNÉES ==========

    private void loadDashboardData() throws SQLException {
        System.out.println("\n===== CHARGEMENT DASHBOARD =====");
        Connection conn = Database.getInstance().getConnection();

        // Compter les users
        String sqlUsers = "SELECT COUNT(*) as total FROM users";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sqlUsers)) {
            if (rs.next()) {
                int count = rs.getInt("total");
                totalUsersLabel.setText(String.valueOf(count));
                System.out.println("✅ Users: " + count);
            }
        }

        // Compter les patients
        String sqlPatients = "SELECT COUNT(*) as total FROM patients";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sqlPatients)) {
            if (rs.next()) {
                int count = rs.getInt("total");
                totalPatientsLabel.setText(String.valueOf(count));
                System.out.println("✅ Patients: " + count);
            }
        }

        // Compter les doctors avec actifs
        String sqlDoctors = "SELECT COUNT(*) as total, SUM(actif) as actifs FROM doctors";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sqlDoctors)) {
            if (rs.next()) {
                int total = rs.getInt("total");
                int actifs = rs.getInt("actifs");
                totalDoctorsLabel.setText(String.valueOf(total));
                activeDoctorsLabel.setText(String.valueOf(actifs));
                inactiveDoctorsLabel.setText(String.valueOf(total - actifs));
                System.out.println("✅ Doctors: " + total + " (Actifs: " + actifs + ")");
            }
        }

        // Compter les admins
        String sqlAdmins = "SELECT COUNT(*) as total, SUM(actif) as actifs FROM admins";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sqlAdmins)) {
            if (rs.next()) {
                int total = rs.getInt("total");
                int actifs = rs.getInt("actifs");
                totalAdminsLabel.setText(String.valueOf(total));
                activeAdminsLabel.setText(String.valueOf(actifs));
                inactiveAdminsLabel.setText(String.valueOf(total - actifs));
                System.out.println("✅ Admins: " + total + " (Actifs: " + actifs + ")");
            }
        }

        // Charger les listes
        loadPatientsList(conn);
        loadDoctorsList(conn);
        loadAdminsList(conn);

        System.out.println("===== FIN CHARGEMENT =====\n");
    }

    private void loadPatientsList(Connection conn) throws SQLException {
        String sql = "SELECT p.*, u.name, u.second_name FROM patients p JOIN users u ON p.id_user = u.id";
        List<patient> patientList = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                patient p = new patient();
                p.setId(rs.getInt("id_user"));
                p.setName(rs.getString("name"));
                p.setSecond_name(rs.getString("second_name"));
                p.setBlood_type(rs.getString("blood_type"));
                patientList.add(p);
            }
        }
        patientsListView.setItems(FXCollections.observableArrayList(patientList));
        activePatientsLabel.setText(String.valueOf(patientList.size()));
        inactivePatientsLabel.setText("0");
        System.out.println("✅ Patients list: " + patientList.size());
    }

    private void loadDoctorsList(Connection conn) throws SQLException {
        String sql = "SELECT d.*, u.name, u.second_name FROM doctors d JOIN users u ON d.id_user = u.id";
        List<doctor> doctorList = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                doctor d = new doctor();
                d.setId(rs.getInt("id_user"));
                d.setName(rs.getString("name"));
                d.setSecond_name(rs.getString("second_name"));
                d.setSpecialty(rs.getString("specialty"));
                d.setActif(rs.getBoolean("actif"));
                doctorList.add(d);
            }
        }
        doctorsListView.setItems(FXCollections.observableArrayList(doctorList));
        System.out.println("✅ Doctors list: " + doctorList.size());
    }

    private void loadAdminsList(Connection conn) throws SQLException {
        String sql = "SELECT a.*, u.name, u.second_name FROM admins a JOIN users u ON a.id_user = u.id";
        List<admin> adminList = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                admin a = new admin();
                a.setId(rs.getInt("id_user"));
                a.setName(rs.getString("name"));
                a.setSecond_name(rs.getString("second_name"));
                a.setActif(rs.getBoolean("actif"));
                adminList.add(a);
            }
        }
        adminsListView.setItems(FXCollections.observableArrayList(adminList));
        System.out.println("✅ Admins list: " + adminList.size());
    }

    public void setAdminData(admin admin) {
        this.currentAdmin = admin;
        if (admin != null) {
            String fullName = admin.getName() + " " + admin.getSecond_name();
            adminNameLabel.setText(fullName);
            welcomeMessageLabel.setText("Welcome, " + fullName + "!");
            adminFullNameLabel.setText(fullName);
            adminEmailLabel.setText(admin.getEmail());
            adminStatusLabel.setText(admin.isActif() ? "Active" : "Inactive");
            System.out.println("✅ Admin logged in: " + fullName);
        }
    }

    private void refreshData() {
        try {
            System.out.println("🔄 Rafraîchissement des données...");
            Connection conn = Database.getInstance().getConnection();
            loadPatientsList(conn);
            loadDoctorsList(conn);
            loadAdminsList(conn);
            System.out.println("✅ Données rafraîchies");
        } catch (SQLException e) {
            System.err.println("❌ Erreur refresh: " + e.getMessage());
            showAlert("Erreur", "Erreur lors du rafraîchissement: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefreshData(ActionEvent event) {
        refreshData();
        showAlert("Succès", "Données rafraîchies !");
    }

    // ========== GESTION DES ÉDITIONS ==========

    private void handleEditDoctor(doctor doctor) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edit_user.fxml"));
            Parent root = loader.load();
            EditUserController controller = loader.getController();
            users user = serviceUser.getById(doctor.getId());
            if (user != null) {
                controller.setUserData(user, "doctor");
                controller.setOnSaveCallback(this::refreshData);
            }
            Stage stage = new Stage();
            stage.setTitle("Edit Doctor");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    private void handleDeleteDoctor(doctor doctor) {
        if (doctor != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation");
            confirm.setHeaderText("Supprimer le docteur");
            confirm.setContentText("Voulez-vous vraiment supprimer " + doctor.getName() + " " + doctor.getSecond_name() + " ?");
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        serviceDoctor.supprimer(doctor);
                        refreshData();
                        showAlert("Succès", "Docteur supprimé");
                    } catch (SQLException e) {
                        showAlert("Erreur", "Erreur: " + e.getMessage());
                    }
                }
            });
        }
    }

    private void handleEditPatient(patient patient) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edit_user.fxml"));
            Parent root = loader.load();
            EditUserController controller = loader.getController();
            users user = serviceUser.getById(patient.getId());
            if (user != null) {
                controller.setUserData(user, "patient");
                controller.setOnSaveCallback(this::refreshData);
            }
            Stage stage = new Stage();
            stage.setTitle("Edit Patient");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    private void handleDeletePatient(patient patient) {
        if (patient != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation");
            confirm.setHeaderText("Supprimer le patient");
            confirm.setContentText("Voulez-vous vraiment supprimer " + patient.getName() + " " + patient.getSecond_name() + " ?");
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        servicePatient.supprimer(patient);
                        refreshData();
                        showAlert("Succès", "Patient supprimé");
                    } catch (SQLException e) {
                        showAlert("Erreur", "Erreur: " + e.getMessage());
                    }
                }
            });
        }
    }

    private void handleEditAdmin(admin admin) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edit_user.fxml"));
            Parent root = loader.load();
            EditUserController controller = loader.getController();
            users user = serviceUser.getById(admin.getId());
            if (user != null) {
                controller.setUserData(user, "admin");
                controller.setOnSaveCallback(() -> {
                    refreshData();
                    Platform.runLater(() -> adminsListView.refresh());
                });
            }
            Stage stage = new Stage();
            stage.setTitle("Edit Admin");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    private void handleDeleteAdmin(admin admin) {
        if (admin != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation");
            confirm.setHeaderText("Supprimer l'admin");
            confirm.setContentText("Voulez-vous vraiment supprimer " + admin.getName() + " " + admin.getSecond_name() + " ?");
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        serviceAdmin.supprimer(admin);
                        refreshData();
                        showAlert("Succès", "Admin supprimé");
                    } catch (SQLException e) {
                        showAlert("Erreur", "Erreur: " + e.getMessage());
                    }
                }
            });
        }
    }

    @FXML
    private void handleAddUser(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin_add_user.fxml"));
            Parent root = loader.load();
            AdminAddUserController controller = loader.getController();
            controller.setAdminName(adminNameLabel.getText());
            controller.setOnUserAddedCallback(() -> Platform.runLater(this::refreshData));
            Stage stage = new Stage();
            stage.setTitle("Add New User");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
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

    public void setUser(users user) {
        System.out.println("✅ Admin connecté: " + user.getEmail());
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}