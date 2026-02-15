package Controllers;

import Models.*;
import Services.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class EditUserController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private Label roleLabel;
    @FXML private TextField idField;

    // Champs communs
    @FXML private TextField nameField;
    @FXML private TextField secondNameField;
    @FXML private TextField emailField;
    @FXML private TextField ageField;
    @FXML private ComboBox<String> genderCombo;
    @FXML private TextField phoneField;

    // Patient fields
    @FXML private VBox patientFields;
    @FXML private TextField bloodTypeField;
    @FXML private TextField weightField;
    @FXML private TextField heightField;

    // Doctor fields
    @FXML private VBox doctorFields;
    @FXML private TextField specialtyField;
    @FXML private TextField experienceField;
    @FXML private TextField diplomaField;
    @FXML private TextField priceField;
    @FXML private CheckBox availableCheckBox;
    @FXML private CheckBox activeCheckBox;

    // Admin fields
    @FXML private VBox adminFields;
    @FXML private CheckBox adminActiveCheckBox;

    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;

    private String currentRole;
    private users currentUser;
    private patient currentPatient;
    private doctor currentDoctor;
    private admin currentAdmin;

    private final ServiceUser serviceUser = new ServiceUser();
    private final ServicePatient servicePatient = new ServicePatient();
    private final ServiceDoctor serviceDoctor = new ServiceDoctor();
    private final ServiceAdmin serviceAdmin = new ServiceAdmin();

    private Runnable onSaveCallback;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialiser le ComboBox de genre
        if (genderCombo != null) {
            genderCombo.getItems().addAll("Male", "Female", "Other");
        }
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

    public void setUserData(users user, String role) {
        System.out.println("\n===== setUserData =====");
        System.out.println("User ID: " + user.getId());
        System.out.println("User Name: " + user.getName());
        System.out.println("User Role: " + role);
        System.out.println("User Gender: " + user.getGender());
        System.out.println("User Age: " + user.getAge());
        System.out.println("User Phone: " + user.getPhone_number());

        this.currentUser = user;
        this.currentRole = role;

        // Afficher les champs selon le rôle
        showFieldsForRole(role);

        // Remplir les champs communs
        idField.setText(String.valueOf(user.getId()));
        nameField.setText(user.getName());
        secondNameField.setText(user.getSecond_name());
        emailField.setText(user.getEmail());
        ageField.setText(String.valueOf(user.getAge()));

        // ✅ Gestion du genre
        if (user.getGender() != null && !user.getGender().isEmpty()) {
            genderCombo.setValue(user.getGender());
        } else {
            genderCombo.setValue("Male"); // Valeur par défaut
            System.out.println("⚠️ Genre non défini, valeur par défaut: Male");
        }

        phoneField.setText(String.valueOf(user.getPhone_number()));

        // Changer la couleur du titre selon le rôle
        switch(role) {
            case "patient":
                titleLabel.setText("✏️ Edit Patient");
                titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #5FB49C;");
                loadPatientData(user.getId());
                break;
            case "doctor":
                titleLabel.setText("✏️ Edit Doctor");
                titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #E667AF;");
                loadDoctorData(user.getId());
                break;
            case "admin":
                titleLabel.setText("✏️ Edit Admin");
                titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #9B59B6;");
                loadAdminData(user.getId());
                break;
        }
    }

    private void showFieldsForRole(String role) {
        hideAllSpecificFields();

        switch(role) {
            case "patient":
                patientFields.setVisible(true);
                patientFields.setManaged(true);
                break;
            case "doctor":
                doctorFields.setVisible(true);
                doctorFields.setManaged(true);
                break;
            case "admin":
                adminFields.setVisible(true);
                adminFields.setManaged(true);
                break;
        }
    }

    private void loadPatientData(int userId) {
        try {
            System.out.println("🔍 Chargement patient ID: " + userId);
            currentPatient = servicePatient.getById(userId);
            if (currentPatient != null) {
                System.out.println("✅ Patient chargé: " + currentPatient.getName());
                System.out.println("   Email: " + currentPatient.getEmail());
                System.out.println("   Blood: " + currentPatient.getBlood_type());

                bloodTypeField.setText(currentPatient.getBlood_type());
                weightField.setText(String.valueOf(currentPatient.getWeight()));
                heightField.setText(String.valueOf(currentPatient.getHeight()));
            } else {
                System.out.println("❌ Patient non trouvé pour ID: " + userId);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur chargement patient: " + e.getMessage());
            showAlert("Erreur", "Erreur chargement patient: " + e.getMessage());
        }
    }

    private void loadDoctorData(int userId) {
        try {
            System.out.println("\n🔍 Chargement doctor ID: " + userId);
            currentDoctor = serviceDoctor.getById(userId);

            if (currentDoctor != null) {
                System.out.println("✅ Doctor chargé avec succès:");
                System.out.println("   ID: " + currentDoctor.getId());
                System.out.println("   Nom: " + currentDoctor.getName());
                System.out.println("   Spécialité: " + currentDoctor.getSpecialty());
                System.out.println("   Expérience: " + currentDoctor.getExperience());
                System.out.println("   Diplôme: " + currentDoctor.getDiplome());
                System.out.println("   Tarif: " + currentDoctor.getTarifConsultation());
                System.out.println("   Disponible: " + currentDoctor.isDisponible());
                System.out.println("   Actif: " + currentDoctor.isActif());

                specialtyField.setText(currentDoctor.getSpecialty());
                experienceField.setText(String.valueOf(currentDoctor.getExperience()));
                diplomaField.setText(currentDoctor.getDiplome());
                priceField.setText(String.valueOf(currentDoctor.getTarifConsultation()));
                availableCheckBox.setSelected(currentDoctor.isDisponible());
                activeCheckBox.setSelected(currentDoctor.isActif());
            } else {
                System.out.println("❌ Doctor non trouvé pour ID: " + userId);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Erreur chargement docteur: " + e.getMessage());
        }
    }

    private void loadAdminData(int userId) {
        try {
            System.out.println("\n🔍 Chargement admin ID: " + userId);
            currentAdmin = serviceAdmin.getById(userId);

            if (currentAdmin != null) {
                System.out.println("✅ Admin chargé avec succès:");
                System.out.println("   ID: " + currentAdmin.getId());
                System.out.println("   Nom: " + currentAdmin.getName());
                System.out.println("   Email: " + currentAdmin.getEmail());
                System.out.println("   Age: " + currentAdmin.getAge());
                System.out.println("   Genre: " + currentAdmin.getGender());
                System.out.println("   Téléphone: " + currentAdmin.getPhone_number());
                System.out.println("   Actif: " + currentAdmin.isActif());

                adminActiveCheckBox.setSelected(currentAdmin.isActif());
            } else {
                System.out.println("❌ Admin non trouvé pour ID: " + userId);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Erreur chargement admin: " + e.getMessage());
        }
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    @FXML
    private void handleSave() {
        try {
            // ============ VALIDATION DES CHAMPS COMMUNS ============
            if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
                showAlert("Erreur", "Le nom ne peut pas être vide");
                nameField.requestFocus();
                return;
            }

            if (secondNameField.getText() == null || secondNameField.getText().trim().isEmpty()) {
                showAlert("Erreur", "Le prénom ne peut pas être vide");
                secondNameField.requestFocus();
                return;
            }

            if (emailField.getText() == null || emailField.getText().trim().isEmpty()) {
                showAlert("Erreur", "L'email ne peut pas être vide");
                emailField.requestFocus();
                return;
            }

            // ✅ VÉRIFICATION CRITIQUE POUR GENDER
            String gender = genderCombo.getValue();
            if (gender == null || gender.trim().isEmpty()) {
                showAlert("Erreur", "Veuillez sélectionner un genre");
                genderCombo.requestFocus();
                return;
            }

            if (ageField.getText() == null || ageField.getText().trim().isEmpty()) {
                showAlert("Erreur", "L'âge ne peut pas être vide");
                ageField.requestFocus();
                return;
            }

            if (phoneField.getText() == null || phoneField.getText().trim().isEmpty()) {
                showAlert("Erreur", "Le téléphone ne peut pas être vide");
                phoneField.requestFocus();
                return;
            }

            // ============ VALIDATION DES CHAMPS NUMÉRIQUES ============
            int age;
            int phone;
            try {
                age = Integer.parseInt(ageField.getText().trim());
                phone = Integer.parseInt(phoneField.getText().trim());

                if (age < 18 || age > 120) {
                    showAlert("Erreur", "L'âge doit être entre 18 et 120 ans");
                    ageField.requestFocus();
                    return;
                }

                if (String.valueOf(phone).length() < 8) {
                    showAlert("Erreur", "Le téléphone doit contenir au moins 8 chiffres");
                    phoneField.requestFocus();
                    return;
                }

            } catch (NumberFormatException e) {
                showAlert("Erreur", "Âge et téléphone doivent être des nombres valides");
                return;
            }

            // ============ VALIDATION SPÉCIFIQUE SELON LE RÔLE ============
            switch(currentRole) {
                case "patient":
                    if (bloodTypeField.getText() == null || bloodTypeField.getText().trim().isEmpty()) {
                        showAlert("Erreur", "Le groupe sanguin est obligatoire");
                        bloodTypeField.requestFocus();
                        return;
                    }
                    try {
                        Double.parseDouble(weightField.getText().trim());
                        Double.parseDouble(heightField.getText().trim());
                    } catch (NumberFormatException e) {
                        showAlert("Erreur", "Poids et taille doivent être des nombres");
                        return;
                    }
                    break;

                case "doctor":
                    if (specialtyField.getText() == null || specialtyField.getText().trim().isEmpty()) {
                        showAlert("Erreur", "La spécialité est obligatoire");
                        specialtyField.requestFocus();
                        return;
                    }
                    try {
                        int exp = Integer.parseInt(experienceField.getText().trim());
                        double price = Double.parseDouble(priceField.getText().trim());

                        if (exp < 0 || exp > 50) {
                            showAlert("Erreur", "L'expérience doit être entre 0 et 50 ans");
                            experienceField.requestFocus();
                            return;
                        }

                        if (price < 0) {
                            showAlert("Erreur", "Le tarif ne peut pas être négatif");
                            priceField.requestFocus();
                            return;
                        }
                    } catch (NumberFormatException e) {
                        showAlert("Erreur", "Expérience et tarif doivent être des nombres");
                        return;
                    }
                    break;
            }

            // ============ MISE À JOUR DES CHAMPS COMMUNS ============
            System.out.println("\n===== DÉBUT SAUVEGARDE =====");
            System.out.println("Rôle: " + currentRole);
            System.out.println("ID utilisateur: " + currentUser.getId());

            currentUser.setName(nameField.getText().trim());
            currentUser.setSecond_name(secondNameField.getText().trim());
            currentUser.setEmail(emailField.getText().trim());
            currentUser.setAge(age);
            currentUser.setGender(gender);
            currentUser.setPhone_number(phone);

            // Sauvegarder l'utilisateur
            System.out.println("🔍 Appel serviceUser.modifier()...");
            serviceUser.modifier(currentUser);
            System.out.println("✅ serviceUser.modifier() exécuté");

            // ============ SAUVEGARDE SPÉCIFIQUE ============
            switch(currentRole) {
                case "patient":
                    if (currentPatient != null) {
                        System.out.println("🔍 Patient avant modif: " + currentPatient.getBlood_type());

                        // ✅ AJOUTE CES LIGNES - mettre à jour les champs communs
                        currentPatient.setName(nameField.getText().trim());
                        currentPatient.setSecond_name(secondNameField.getText().trim());
                        currentPatient.setEmail(emailField.getText().trim());
                        currentPatient.setAge(age);
                        currentPatient.setGender(gender);
                        currentPatient.setPhone_number(phone);

                        currentPatient.setBlood_type(bloodTypeField.getText().trim());
                        currentPatient.setWeight(Double.parseDouble(weightField.getText().trim()));
                        currentPatient.setHeight(Double.parseDouble(heightField.getText().trim()));

                        System.out.println("🔍 Patient après modif: " + currentPatient.getBlood_type());
                        System.out.println("   Nom maintenant: " + currentPatient.getName());

                        servicePatient.modifier(currentPatient);
                    }
                    break;

                case "doctor":
                    if (currentDoctor != null) {
                        System.out.println("🔍 Doctor avant modif: " + currentDoctor.getSpecialty());

                        // ✅ Met à jour TOUTES les infos du doctor
                        currentDoctor.setName(nameField.getText().trim());           // ← IMPORTANT !
                        currentDoctor.setSecond_name(secondNameField.getText().trim());
                        currentDoctor.setEmail(emailField.getText().trim());
                        currentDoctor.setAge(age);
                        currentDoctor.setGender(gender);
                        currentDoctor.setPhone_number(phone);

                        currentDoctor.setSpecialty(specialtyField.getText().trim());
                        currentDoctor.setExperience(Integer.parseInt(experienceField.getText().trim()));
                        currentDoctor.setDiplome(diplomaField.getText().trim());
                        currentDoctor.setTarifConsultation(Double.parseDouble(priceField.getText().trim()));
                        currentDoctor.setDisponible(availableCheckBox.isSelected());
                        currentDoctor.setActif(activeCheckBox.isSelected());

                        System.out.println("🔍 Doctor après modif: " + currentDoctor.getSpecialty());
                        System.out.println("   Nom maintenant: " + currentDoctor.getName()); // ← Vérifie

                        serviceDoctor.modifier(currentDoctor);
                    }
                    break;

                case "admin":
                    if (currentAdmin != null) {
                        System.out.println("\n🔍 ADMIN avant modif:");
                        System.out.println("   ID: " + currentAdmin.getId());
                        System.out.println("   Nom: " + currentAdmin.getName());
                        System.out.println("   Email: " + currentAdmin.getEmail());
                        System.out.println("   Age: " + currentAdmin.getAge());
                        System.out.println("   Genre: " + currentAdmin.getGender());
                        System.out.println("   Téléphone: " + currentAdmin.getPhone_number());
                        System.out.println("   Actif: " + currentAdmin.isActif());

                        // ✅ Met à jour TOUS les champs communs
                        currentAdmin.setName(nameField.getText().trim());
                        currentAdmin.setSecond_name(secondNameField.getText().trim());
                        currentAdmin.setEmail(emailField.getText().trim());
                        currentAdmin.setAge(age);
                        currentAdmin.setGender(gender);
                        currentAdmin.setPhone_number(phone);

                        // ✅ Met à jour le champ spécifique
                        currentAdmin.setActif(adminActiveCheckBox.isSelected());

                        System.out.println("\n🔍 ADMIN après modif:");
                        System.out.println("   Nom: " + currentAdmin.getName());
                        System.out.println("   Email: " + currentAdmin.getEmail());
                        System.out.println("   Age: " + currentAdmin.getAge());
                        System.out.println("   Genre: " + currentAdmin.getGender());
                        System.out.println("   Téléphone: " + currentAdmin.getPhone_number());
                        System.out.println("   Actif: " + currentAdmin.isActif());

                        serviceAdmin.modifier(currentAdmin);
                    }
                    break;
            }

            System.out.println("✅ Sauvegarde terminée avec succès");

            showAlert("Succès", "Modifications enregistrées avec succès");

            if (onSaveCallback != null) {
                System.out.println("🔍 Exécution du callback...");
                onSaveCallback.run();
            }

            closeWindow();

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez vérifier les champs numériques");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la sauvegarde: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
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