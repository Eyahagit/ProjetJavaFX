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

               // ✅ TEST DIRECT SANS UTILISER LES SERVICES
               System.out.println("\n===== CHARGEMENT DIRECT DEPUIS LA DB =====");

               // 1. Connexion directe à la DB
               Connection conn = Database.getInstance().getConnection();

               // 2. Compter les users
               String sqlUsers = "SELECT COUNT(*) as total FROM users";
               try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sqlUsers)) {
                   if (rs.next()) {
                       int count = rs.getInt("total");
                       totalUsersLabel.setText(String.valueOf(count));
                       System.out.println("✅ Users: " + count);
                   }
               }

               // 3. Compter les patients
               String sqlPatients = "SELECT COUNT(*) as total FROM patients";
               try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sqlPatients)) {
                   if (rs.next()) {
                       int count = rs.getInt("total");
                       totalPatientsLabel.setText(String.valueOf(count));
                       activePatientsLabel.setText(String.valueOf(count));
                       inactivePatientsLabel.setText("0");
                       System.out.println("✅ Patients: " + count);
                   }
               }

               // 4. Compter les doctors
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

               // 5. Compter les admins
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

               // 6. Récupérer les listes pour les ListViews
               // Patients
               String sqlPatientsList = "SELECT p.*, u.name, u.second_name FROM patients p JOIN users u ON p.id_user = u.id";
               List<patient> patientList = new ArrayList<>();
               try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sqlPatientsList)) {
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
               System.out.println("✅ Patients list: " + patientList.size());

               // Doctors
               String sqlDoctorsList = "SELECT d.*, u.name, u.second_name FROM doctors d JOIN users u ON d.id_user = u.id";
               List<doctor> doctorList = new ArrayList<>();
               try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sqlDoctorsList)) {
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

               // ✅ NOUVEAU: Admins list
               String sqlAdminsList = "SELECT a.*, u.name, u.second_name FROM admins a JOIN users u ON a.id_user = u.id";
               List<admin> adminList = new ArrayList<>();
               try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sqlAdminsList)) {
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

               System.out.println("===== FIN CHARGEMENT =====\n");

           } catch (SQLException e) {
               System.err.println("❌ ERREUR: " + e.getMessage());
               e.printStackTrace();
               showAlert("Erreur", "Erreur de chargement: " + e.getMessage());
           }
       }
       /*@Override
       public void initialize(URL url, ResourceBundle resourceBundle) {
           LocalDate now = LocalDate.now();
           DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
           dateLabel.setText(now.format(formatter));

           setupListViews();
           loadDashboardData();  // ✅ Maintenant ça marche !
       }*/
       private void setupListViews() {
           // Custom cell factory for doctors list
           doctorsListView.setCellFactory(lv -> new ListCell<doctor>() {
               private final HBox content = new HBox(10);
               private final Label nameLabel = new Label();
               private final Label specialtyLabel = new Label();
               private final Label statusLabel = new Label();
               private final Button editBtn = new Button("✏️");
               private final Button deleteBtn = new Button("🗑️");
               private final HBox buttonsBox = new HBox(5);

               {
                   // Style des boutons
                   editBtn.setStyle("-fx-background-color: #FFA500; -fx-text-fill: white; -fx-font-size: 12px; -fx-background-radius: 5; -fx-padding: 3 8; -fx-cursor: hand;");
                   deleteBtn.setStyle("-fx-background-color: #FF4444; -fx-text-fill: white; -fx-font-size: 12px; -fx-background-radius: 5; -fx-padding: 3 8; -fx-cursor: hand;");

                   buttonsBox.getChildren().addAll(editBtn, deleteBtn);

                   // Ajouter les boutons au content
                   content.getChildren().addAll(nameLabel, specialtyLabel, statusLabel, buttonsBox);
                   content.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                   content.setSpacing(15);

                   nameLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 180;");
                   specialtyLabel.setStyle("-fx-text-fill: #666; -fx-min-width: 150;");
                   statusLabel.setStyle("-fx-min-width: 100;");

                   // Pour que les boutons soient à droite
                   HBox.setHgrow(buttonsBox, javafx.scene.layout.Priority.ALWAYS);
                   buttonsBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
               }

               @Override
               protected void updateItem(doctor doctor, boolean empty) {
                   super.updateItem(doctor, empty);
                   if (empty || doctor == null) {
                       setGraphic(null);
                   } else {
                       nameLabel.setText(doctor.getName() + " " + doctor.getSecond_name());
                       specialtyLabel.setText(doctor.getSpecialty());
                       statusLabel.setText(doctor.isActif() ? "🟢 Active" : "🔴 Inactive");
                       statusLabel.setStyle(doctor.isActif() ?
                               "-fx-text-fill: #2e7d32; -fx-font-weight: bold; -fx-min-width: 100;" :
                               "-fx-text-fill: #c62828; -fx-font-weight: bold; -fx-min-width: 100;");

                       editBtn.setOnAction(event -> handleEditDoctor(doctor));
                       deleteBtn.setOnAction(event -> handleDeleteDoctor(doctor));

                       setGraphic(content);
                   }
               }
           });

           // Custom cell factory for patients list
           patientsListView.setCellFactory(lv -> new ListCell<patient>() {
               private final HBox content = new HBox(10);
               private final Label nameLabel = new Label();
               private final Label bloodLabel = new Label();
               private final Label statusLabel = new Label();
               private final Button editBtn = new Button("✏️");
               private final Button deleteBtn = new Button("🗑️");
               private final HBox buttonsBox = new HBox(5);

               {
                   // Style des boutons
                   editBtn.setStyle("-fx-background-color: #FFA500; -fx-text-fill: white; -fx-font-size: 12px; -fx-background-radius: 5; -fx-padding: 3 8; -fx-cursor: hand;");
                   deleteBtn.setStyle("-fx-background-color: #FF4444; -fx-text-fill: white; -fx-font-size: 12px; -fx-background-radius: 5; -fx-padding: 3 8; -fx-cursor: hand;");

                   buttonsBox.getChildren().addAll(editBtn, deleteBtn);

                   // Ajouter les boutons au content
                   content.getChildren().addAll(nameLabel, bloodLabel, statusLabel, buttonsBox);
                   content.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                   content.setSpacing(15);

                   nameLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 180;");
                   bloodLabel.setStyle("-fx-text-fill: #666; -fx-min-width: 100;");
                   statusLabel.setStyle("-fx-min-width: 100;");

                   // Pour que les boutons soient à droite
                   HBox.setHgrow(buttonsBox, javafx.scene.layout.Priority.ALWAYS);
                   buttonsBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
               }

               @Override
               protected void updateItem(patient patient, boolean empty) {
                   super.updateItem(patient, empty);
                   if (empty || patient == null) {
                       setGraphic(null);
                   } else {
                       nameLabel.setText(patient.getName() + " " + patient.getSecond_name());
                       bloodLabel.setText("Blood: " + patient.getBlood_type());

                       // ✅ Pour les patients (actif par défaut)
                       boolean isActive = true;

                       statusLabel.setText(isActive ? "🟢 Active" : "🔴 Inactive");
                       statusLabel.setStyle(isActive ?
                               "-fx-text-fill: #2e7d32; -fx-font-weight: bold;" :
                               "-fx-text-fill: #c62828; -fx-font-weight: bold;");

                       editBtn.setOnAction(event -> handleEditPatient(patient));
                       deleteBtn.setOnAction(event -> handleDeletePatient(patient));

                       setGraphic(content);
                   }
               }
           });

           // ✅ NOUVEAU: Custom cell factory for admins list
           adminsListView.setCellFactory(lv -> new ListCell<admin>() {
               private final HBox content = new HBox(10);
               private final Label nameLabel = new Label();
               private final Label statusLabel = new Label();
               private final Button editBtn = new Button("✏️");
               private final Button deleteBtn = new Button("🗑️");
               private final HBox buttonsBox = new HBox(5);

               {
                   // Style des boutons
                   editBtn.setStyle("-fx-background-color: #FFA500; -fx-text-fill: white; -fx-font-size: 12px; -fx-background-radius: 5; -fx-padding: 3 8; -fx-cursor: hand;");
                   deleteBtn.setStyle("-fx-background-color: #FF4444; -fx-text-fill: white; -fx-font-size: 12px; -fx-background-radius: 5; -fx-padding: 3 8; -fx-cursor: hand;");

                   buttonsBox.getChildren().addAll(editBtn, deleteBtn);

                   // Ajouter les boutons au content
                   content.getChildren().addAll(nameLabel, statusLabel, buttonsBox);
                   content.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                   content.setSpacing(15);

                   nameLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 250;");
                   statusLabel.setStyle("-fx-min-width: 100;");

                   // Pour que les boutons soient à droite
                   HBox.setHgrow(buttonsBox, javafx.scene.layout.Priority.ALWAYS);
                   buttonsBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
               }

               @Override
               protected void updateItem(admin admin, boolean empty) {
                   super.updateItem(admin, empty);
                   if (empty || admin == null) {
                       setGraphic(null);
                   } else {
                       nameLabel.setText(admin.getName() + " " + admin.getSecond_name());
                       statusLabel.setText(admin.isActif() ? "🟢 Active" : "🔴 Inactive");
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

        public void setAdminData(admin admin) {
            this.currentAdmin = admin;
            if (admin != null) {
                String fullName = admin.getName() + " " + admin.getSecond_name();
                adminNameLabel.setText(fullName);
                welcomeMessageLabel.setText("Welcome, " + fullName + "!");
                adminFullNameLabel.setText(fullName);
                adminEmailLabel.setText(admin.getEmail());
                adminStatusLabel.setText(admin.isActif() ? "Active" : "Inactive");

                System.out.println("✅ Admin logged in: " + fullName + " (" + admin.getEmail() + ")");
            }
        }

        /*private void loadDashboardData() {
            try {
                // Total counts
                //List<users> allUsers = serviceUser.recuperer();
                int totalUsers = serviceUser.countUsers();
                List<patient> allPatients = servicePatient.recuperer();
                List<doctor> allDoctors = serviceDoctor.recuperer();
                List<admin> allAdmins = serviceAdmin.recuperer();

                //totalUsersLabel.setText(String.valueOf(allUsers.size()));
                totalUsersLabel.setText(String.valueOf(totalUsers));
                totalPatientsLabel.setText(String.valueOf(allPatients.size()));
                totalDoctorsLabel.setText(String.valueOf(allDoctors.size()));
                totalAdminsLabel.setText(String.valueOf(allAdmins.size()));

                // Active/Inactive counts for doctors
                long activeDoctors = allDoctors.stream().filter(doctor::isActif).count();
                long inactiveDoctors = allDoctors.size() - activeDoctors;
                activeDoctorsLabel.setText(String.valueOf(activeDoctors));
                inactiveDoctorsLabel.setText(String.valueOf(inactiveDoctors));

                // Active/Inactive counts for admins
                long activeAdmins = allAdmins.stream().filter(admin::isActif).count();
                long inactiveAdmins = allAdmins.size() - activeAdmins;
                activeAdminsLabel.setText(String.valueOf(activeAdmins));
                inactiveAdminsLabel.setText(String.valueOf(inactiveAdmins));

                // Pour les patients, si vous avez un champ actif
                // Sinon, mettez tous comme actifs par défaut
                activePatientsLabel.setText(String.valueOf(allPatients.size()));
                inactivePatientsLabel.setText("0");

                // Populate ListViews
                ObservableList<doctor> doctorsObservable = FXCollections.observableArrayList(allDoctors);
                ObservableList<patient> patientsObservable = FXCollections.observableArrayList(allPatients);

                doctorsListView.setItems(doctorsObservable);
                patientsListView.setItems(patientsObservable);

            } catch (SQLException e) {
                showAlert("Erreur", "Erreur lors du chargement des données: " + e.getMessage());
                e.printStackTrace();
            }
        }*/
        // Méthode pour rafraîchir les données
        private void refreshData() {
            try {
                System.out.println("🔄 Rafraîchissement des données...");
                Connection conn = Database.getInstance().getConnection();

                // Patients
                String sqlPatientsList = "SELECT p.*, u.name, u.second_name FROM patients p JOIN users u ON p.id_user = u.id";
                List<patient> patientList = new ArrayList<>();
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sqlPatientsList)) {
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
                System.out.println("✅ Patients chargés: " + patientList.size());

                // Doctors
                String sqlDoctorsList = "SELECT d.*, u.name, u.second_name FROM doctors d JOIN users u ON d.id_user = u.id";
                List<doctor> doctorList = new ArrayList<>();
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sqlDoctorsList)) {
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
                System.out.println("✅ Doctors chargés: " + doctorList.size());

                // Admins
                String sqlAdminsList = "SELECT a.*, u.name, u.second_name FROM admins a JOIN users u ON a.id_user = u.id";
                List<admin> adminList = new ArrayList<>();
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sqlAdminsList)) {
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
                System.out.println("✅ Admins chargés: " + adminList.size());

                // ✅ AJOUT ICI - MISE À JOUR DU PROFIL ADMIN CONNECTÉ
                if (currentAdmin != null) {
                    try {
                        // Recharger l'admin depuis la base
                        admin updatedAdmin = serviceAdmin.getById(currentAdmin.getId());
                        if (updatedAdmin != null) {
                            currentAdmin = updatedAdmin;

                            // Mettre à jour tous les labels du profil
                            String fullName = currentAdmin.getName() + " " + currentAdmin.getSecond_name();
                            adminNameLabel.setText(fullName);
                            welcomeMessageLabel.setText("Welcome, " + fullName + "!");
                            adminFullNameLabel.setText(fullName);
                            adminEmailLabel.setText(currentAdmin.getEmail());
                            adminStatusLabel.setText(currentAdmin.isActif() ? "Active" : "Inactive");

                            System.out.println("✅ Profil admin mis à jour: " + fullName);
                        }
                    } catch (SQLException e) {
                        System.err.println("❌ Erreur mise à jour profil admin: " + e.getMessage());
                    }
                }

            } catch (SQLException e) {
                System.err.println("❌ Erreur refresh: " + e.getMessage());
                e.printStackTrace();
                showAlert("Erreur", "Erreur lors du rafraîchissement: " + e.getMessage());
            }
        }

        @FXML
        private void handleRefreshData(ActionEvent event) {
            refreshData();
            showAlert("Succès", "Données rafraîchies avec succès !");
        }


        private void handleEditDoctor(doctor doctor) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/edit_user.fxml"));
                Parent root = loader.load();

                EditUserController controller = loader.getController();

                // Récupérer l'utilisateur complet
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
                            // Recharger les données
                            refreshData();
                            showAlert("Succès", "Docteur supprimé avec succès");
                        } catch (SQLException e) {
                            showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage());
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
                            // Recharger les données
                            refreshData();
                            showAlert("Succès", "Patient supprimé avec succès");
                        } catch (SQLException e) {
                            showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage());
                        }
                    }
                });
            }
        }

        private void handleEditAdmin(admin admin) {
            try {
                System.out.println("\n🔍 Édition profil admin - ID: " + admin.getId());

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/edit_user.fxml"));
                Parent root = loader.load();

                EditUserController controller = loader.getController();

                // Récupérer l'utilisateur complet
                users user = serviceUser.getById(admin.getId());

                if (user != null) {
                    System.out.println("✅ User trouvé: " + user.getName());
                    controller.setUserData(user, "admin");

                    // ✅ CALLBACK SPÉCIFIQUE POUR ADMIN
                    controller.setOnSaveCallback(() -> {
                        System.out.println("🔄 Callback admin exécuté - Rechargement des données");

                        // 1. Recharger toutes les données
                        refreshData();

                        // 2. Forcer le rafraîchissement de la liste admin
                        Platform.runLater(() -> {
                            adminsListView.refresh();
                            System.out.println("✅ Liste admin rafraîchie");
                        });
                    });
                } else {
                    System.out.println("❌ User non trouvé pour ID: " + admin.getId());
                    showAlert("Erreur", "Utilisateur non trouvé");
                    return;
                }

                Stage stage = new Stage();
                stage.setTitle("Edit Admin");
                stage.setScene(new Scene(root));
                stage.show();

            } catch (IOException | SQLException e) {
                System.err.println("❌ Erreur: " + e.getMessage());
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
                            showAlert("Succès", "Admin supprimé avec succès");
                        } catch (SQLException e) {
                            showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage());
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

                // Callback pour rafraîchir après ajout
                controller.setOnUserAddedCallback(() -> {
                    Platform.runLater(() -> {
                        refreshData();
                        System.out.println("✅ Dashboard rafraîchi après ajout");
                    });
                });

                Stage stage = new Stage();
                stage.setTitle("Add New User - Admin");
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

        private void navigateTo(String fxmlPath, String title, ActionEvent event) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent root = loader.load();
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle(title + " - GrowMind");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible de charger la page: " + e.getMessage());
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