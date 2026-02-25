package Controllers;

import Models.*;
import Services.*;
import utils.PasswordUtils; // ← NOUVEL IMPORT
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class AdminAddUserController implements Initializable {

    @FXML private Label adminNameLabel;

    // Role selection
    @FXML private ComboBox<String> roleBox;

    // Common fields
    @FXML private TextField nameField;
    @FXML private TextField secondNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField ageField;
    @FXML private ComboBox<String> genderCombo;
    @FXML private TextField phoneField;
    @FXML private DatePicker dateBirth;

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

    @FXML private Button addBtn;
    @FXML private Button cancelBtn;

    private final ServiceUser serviceUser = new ServiceUser();
    private final ServicePatient servicePatient = new ServicePatient();
    private final ServiceDoctor serviceDoctor = new ServiceDoctor();
    private final ServiceAdmin serviceAdmin = new ServiceAdmin();

    private Runnable onUserAddedCallback;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize role combo box
        if (roleBox != null) {
            roleBox.getItems().addAll("patient", "doctor", "admin");

            // Listener for role selection
            roleBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                showRoleSpecificFields(newVal);
            });
        }

        // Initialize gender combo box
        if (genderCombo != null) {
            genderCombo.getItems().addAll("Male", "Female", "Other");
        }

        // Hide all specific fields initially
        hideAllSpecificFields();

        // Set admin name (can be passed from dashboard)
        adminNameLabel.setText("Admin");
    }

    private void hideAllSpecificFields() {
        patientFields.setVisible(false);
        patientFields.setManaged(false);
        doctorFields.setVisible(false);
        doctorFields.setManaged(false);
        adminFields.setVisible(false);
        adminFields.setManaged(false);
    }

    private void showRoleSpecificFields(String role) {
        hideAllSpecificFields();

        if (role != null) {
            switch (role) {
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
    }

    public void setAdminName(String name) {
        adminNameLabel.setText(name);
    }

    public void setOnUserAddedCallback(Runnable callback) {
        this.onUserAddedCallback = callback;
    }

    @FXML
    private void handleAddUser() {
        try {
            // ============ VALIDATION DES CHAMPS COMMUNS ============
            if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
                showAlert("Erreur", "Le nom est obligatoire");
                nameField.requestFocus();
                return;
            }

            if (secondNameField.getText() == null || secondNameField.getText().trim().isEmpty()) {
                showAlert("Erreur", "Le prénom est obligatoire");
                secondNameField.requestFocus();
                return;
            }

            if (emailField.getText() == null || emailField.getText().trim().isEmpty()) {
                showAlert("Erreur", "L'email est obligatoire");
                emailField.requestFocus();
                return;
            }

            if (passwordField.getText() == null || passwordField.getText().length() < 6) {
                showAlert("Erreur", "Le mot de passe doit contenir au moins 6 caractères");
                passwordField.requestFocus();
                return;
            }

            String role = roleBox.getValue();
            if (role == null) {
                showAlert("Erreur", "Veuillez sélectionner un rôle");
                roleBox.requestFocus();
                return;
            }

            String gender = genderCombo.getValue();
            if (gender == null) {
                showAlert("Erreur", "Veuillez sélectionner un genre");
                genderCombo.requestFocus();
                return;
            }

            if (dateBirth.getValue() == null) {
                showAlert("Erreur", "La date de naissance est obligatoire");
                dateBirth.requestFocus();
                return;
            }

            if (dateBirth.getValue().isAfter(LocalDate.now())) {
                showAlert("Erreur", "La date de naissance ne peut pas être dans le futur");
                dateBirth.requestFocus();
                return;
            }

            int age;
            int phone;
            try {
                age = Integer.parseInt(ageField.getText().trim());
                if (age < 18 || age > 120) {
                    showAlert("Erreur", "L'âge doit être entre 18 et 120 ans");
                    ageField.requestFocus();
                    return;
                }

                phone = Integer.parseInt(phoneField.getText().trim());
                if (String.valueOf(phone).length() < 8) {
                    showAlert("Erreur", "Le téléphone doit contenir au moins 8 chiffres");
                    phoneField.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("Erreur", "Âge et téléphone doivent être des nombres valides");
                return;
            }

            // 🔐 HACHER LE MOT DE PASSE AVANT DE LE STOCKER
            String plainPassword = passwordField.getText();
            String hashedPassword = PasswordUtils.hashPassword(plainPassword);

            // ============ CRÉATION SELON LE RÔLE ============
            switch (role) {
                case "patient":
                    // Validation patient
                    if (bloodTypeField.getText().trim().isEmpty()) {
                        showAlert("Erreur", "Le groupe sanguin est obligatoire");
                        bloodTypeField.requestFocus();
                        return;
                    }

                    double weight, height;
                    try {
                        weight = Double.parseDouble(weightField.getText().trim());
                        height = Double.parseDouble(heightField.getText().trim());

                        if (weight < 20 || weight > 300) {
                            showAlert("Erreur", "Le poids doit être entre 20 et 300 kg");
                            weightField.requestFocus();
                            return;
                        }

                        if (height < 50 || height > 250) {
                            showAlert("Erreur", "La taille doit être entre 50 et 250 cm");
                            heightField.requestFocus();
                            return;
                        }
                    } catch (NumberFormatException e) {
                        showAlert("Erreur", "Poids et taille doivent être des nombres");
                        return;
                    }

                    patient p = new patient();
                    p.setName(nameField.getText().trim());
                    p.setSecond_name(secondNameField.getText().trim());
                    p.setAge(age);
                    p.setGender(gender);
                    p.setPhone_number(phone);
                    p.setBirth_date(dateBirth.getValue().toString());
                    p.setEmail(emailField.getText().trim());
                    p.setPassword(hashedPassword); // ← MOT DE PASSE HACHÉ
                    p.setRole(role);
                    p.setBlood_type(bloodTypeField.getText().trim());
                    p.setWeight(weight);
                    p.setHeight(height);

                    serviceUser.ajouter(p);
                    servicePatient.ajouter(p);

                    System.out.println("✅ Patient ajouté avec mot de passe crypté");
                    break;

                case "doctor":
                    // Validation doctor
                    if (specialtyField.getText().trim().isEmpty()) {
                        showAlert("Erreur", "La spécialité est obligatoire");
                        specialtyField.requestFocus();
                        return;
                    }

                    int experience;
                    double price;
                    try {
                        experience = Integer.parseInt(experienceField.getText().trim());
                        if (experience < 0 || experience > 50) {
                            showAlert("Erreur", "L'expérience doit être entre 0 et 50 ans");
                            experienceField.requestFocus();
                            return;
                        }

                        price = Double.parseDouble(priceField.getText().trim());
                        if (price < 0) {
                            showAlert("Erreur", "Le tarif ne peut pas être négatif");
                            priceField.requestFocus();
                            return;
                        }
                    } catch (NumberFormatException e) {
                        showAlert("Erreur", "Expérience et tarif doivent être des nombres");
                        return;
                    }

                    doctor d = new doctor();
                    d.setName(nameField.getText().trim());
                    d.setSecond_name(secondNameField.getText().trim());
                    d.setAge(age);
                    d.setGender(gender);
                    d.setPhone_number(phone);
                    d.setBirth_date(dateBirth.getValue().toString());
                    d.setEmail(emailField.getText().trim());
                    d.setPassword(hashedPassword); // ← MOT DE PASSE HACHÉ
                    d.setRole(role);
                    d.setSpecialty(specialtyField.getText().trim());
                    d.setExperience(experience);
                    d.setDiplome(diplomaField.getText().trim());
                    d.setDisponible(availableCheckBox.isSelected());
                    d.setTarifConsultation(price);
                    d.setActif(activeCheckBox.isSelected());

                    serviceUser.ajouter(d);
                    serviceDoctor.ajouter(d);

                    System.out.println("✅ Docteur ajouté avec mot de passe crypté");
                    break;

                case "admin":
                    admin a = new admin();
                    a.setName(nameField.getText().trim());
                    a.setSecond_name(secondNameField.getText().trim());
                    a.setAge(age);
                    a.setGender(gender);
                    a.setPhone_number(phone);
                    a.setBirth_date(dateBirth.getValue().toString());
                    a.setEmail(emailField.getText().trim());
                    a.setPassword(hashedPassword); // ← MOT DE PASSE HACHÉ
                    a.setRole(role);
                    a.setActif(adminActiveCheckBox.isSelected());

                    serviceUser.ajouter(a);
                    serviceAdmin.ajouter(a);

                    System.out.println("✅ Admin ajouté avec mot de passe crypté");
                    break;
            }

            showAlert("Succès", "Utilisateur ajouté avec succès !");

            if (onUserAddedCallback != null) {
                onUserAddedCallback.run();
            }

            clearFields();
            closeWindow();

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez vérifier les champs numériques");
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de l'ajout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearFields() {
        nameField.clear();
        secondNameField.clear();
        emailField.clear();
        passwordField.clear();
        ageField.clear();
        genderCombo.setValue(null);
        phoneField.clear();
        dateBirth.setValue(null);
        roleBox.setValue(null);

        bloodTypeField.clear();
        weightField.clear();
        heightField.clear();

        specialtyField.clear();
        experienceField.clear();
        diplomaField.clear();
        priceField.clear();
        availableCheckBox.setSelected(false);
        activeCheckBox.setSelected(false);

        adminActiveCheckBox.setSelected(false);

        hideAllSpecificFields();
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