package Controllers;

import Models.*;
import Services.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.sql.SQLException;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.LocalDate;

public class usersController {

    private final ServiceUser serviceUser = new ServiceUser();
    private final ServiceDoctor serviceDoctor = new ServiceDoctor();
    private final ServiceAdmin serviceAdmin = new ServiceAdmin();
    private final ServicePatient servicePatient = new ServicePatient();


    // ============= CHAMPS UTILISATEUR =============
    @FXML private TextField nameField;
    @FXML private TextField secondNameField;
    @FXML private TextField ageField;
    @FXML private TextField genderField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private DatePicker dateBirth;
    @FXML private ComboBox<String> roleBox;

    // ============= CHAMPS PATIENT =============
    @FXML private VBox panePatient;
    @FXML private TextField txtBloodType;
    @FXML private TextField txtWeight;
    @FXML private TextField txtHeight;

    // ============= CHAMPS DOCTOR =============
    @FXML private VBox paneDoctor;
    @FXML private TextField txtSpecialty;
    @FXML private TextField txtExperience;
    @FXML private TextField txtDiplome;
    @FXML private CheckBox chkDisponible;
    @FXML private TextField txtTarif;
    @FXML private CheckBox chkActif;

    // ============= CHAMPS ADMIN =============
    @FXML private VBox paneAdmin;
    @FXML private CheckBox chkActifA;


    @FXML
    public void initialize() {
        // Remplir les rôles
        if (roleBox != null) {
            roleBox.getItems().addAll("patient", "doctor", "admin");

            // ✅ AJOUT 1: Listener pour afficher les sections selon le rôle
            roleBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                showRoleSpecificFields(newVal);
            });
        }

        // Initialiser toutes les sections comme cachées
        showRoleSpecificFields(null);
    }

    // ✅ AJOUT 2: Méthode pour afficher/masquer les sections
    private void showRoleSpecificFields(String role) {
        // Cacher toutes les sections
        panePatient.setVisible(false);
        panePatient.setManaged(false);
        paneDoctor.setVisible(false);
        paneDoctor.setManaged(false);
        paneAdmin.setVisible(false);
        paneAdmin.setManaged(false);

        // Afficher la section correspondante
        if (role != null) {
            switch (role) {
                case "patient":
                    panePatient.setVisible(true);
                    panePatient.setManaged(true);
                    break;
                case "doctor":
                    paneDoctor.setVisible(true);
                    paneDoctor.setManaged(true);
                    break;
                case "admin":
                    paneAdmin.setVisible(true);
                    paneAdmin.setManaged(true);
                    break;
            }
        }
    }

    @FXML
    private void addUser() {
        try {
            // ============ VALIDATION DES CHAMPS COMMUNS ============

            // Nom
            if (nameField.getText().trim().isEmpty()) {
                showAlert("Erreur de saisie", "Le nom ne peut pas être vide");
                nameField.requestFocus();
                nameField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                return;
            } else {
                nameField.setStyle("");
            }

            // Prénom (second name)
            if (secondNameField.getText().trim().isEmpty()) {
                showAlert("Erreur de saisie", "Le prénom ne peut pas être vide");
                secondNameField.requestFocus();
                secondNameField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                return;
            } else {
                secondNameField.setStyle("");
            }

            // Email avec regex
            String email = emailField.getText().trim();
            String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
            if (!email.matches(emailRegex)) {
                showAlert("Erreur de saisie", "Format d'email invalide (ex: nom@domaine.com)");
                emailField.requestFocus();
                emailField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                return;
            } else {
                emailField.setStyle("");
            }

            // Mot de passe
            String password = passwordField.getText();
            if (password.length() < 6) {
                showAlert("Erreur de saisie", "Le mot de passe doit contenir au moins 6 caractères");
                passwordField.requestFocus();
                passwordField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                return;
            } else {
                passwordField.setStyle("");
            }

            // Rôle
            if (roleBox.getValue() == null) {
                showAlert("Erreur de saisie", "Veuillez sélectionner un rôle");
                roleBox.requestFocus();
                roleBox.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                return;
            } else {
                roleBox.setStyle("");
            }

            // Date de naissance
            if (dateBirth.getValue() == null) {
                showAlert("Erreur de saisie", "La date de naissance est obligatoire");
                dateBirth.requestFocus();
                dateBirth.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                return;
            } else if (dateBirth.getValue().isAfter(LocalDate.now())) {
                showAlert("Erreur de saisie", "La date de naissance ne peut pas être dans le futur");
                dateBirth.requestFocus();
                dateBirth.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                return;
            } else {
                dateBirth.setStyle("");
            }

            // Âge - validation nombre et intervalle
            int age = 0;
            try {
                age = Integer.parseInt(ageField.getText().trim());
                if (age < 18 || age > 120) {
                    showAlert("Erreur de saisie", "L'âge doit être compris entre 18 et 120 ans");
                    ageField.requestFocus();
                    ageField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("Erreur de saisie", "L'âge doit être un nombre valide");
                ageField.requestFocus();
                ageField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                return;
            }
            ageField.setStyle("");

            // Genre
            if (genderField.getText().trim().isEmpty()) {
                showAlert("Erreur de saisie", "Le genre est obligatoire");
                genderField.requestFocus();
                genderField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                return;
            } else {
                genderField.setStyle("");
            }

            // Téléphone
            int phone = 0;
            try {
                phone = Integer.parseInt(phoneField.getText().trim());
                if (String.valueOf(phone).length() < 8) {
                    showAlert("Erreur de saisie", "Le numéro de téléphone doit contenir au moins 8 chiffres");
                    phoneField.requestFocus();
                    phoneField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("Erreur de saisie", "Le téléphone doit être un nombre valide");
                phoneField.requestFocus();
                phoneField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                return;
            }
            phoneField.setStyle("");

            String role = roleBox.getValue();

            // ============= VALIDATION SPÉCIFIQUE SELON LE RÔLE =============
            switch (role) {
                case "patient":
                    // Groupe sanguin
                    if (txtBloodType.getText().trim().isEmpty()) {
                        showAlert("Erreur de saisie", "Le groupe sanguin est obligatoire");
                        txtBloodType.requestFocus();
                        txtBloodType.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                        return;
                    }
                    txtBloodType.setStyle("");

                    // Poids
                    double weight = 0;
                    try {
                        weight = Double.parseDouble(txtWeight.getText().trim());
                        if (weight < 20 || weight > 300) {
                            showAlert("Erreur de saisie", "Le poids doit être entre 20 et 300 kg");
                            txtWeight.requestFocus();
                            txtWeight.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                            return;
                        }
                    } catch (NumberFormatException e) {
                        showAlert("Erreur de saisie", "Le poids doit être un nombre valide");
                        txtWeight.requestFocus();
                        txtWeight.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                        return;
                    }
                    txtWeight.setStyle("");

                    // Taille
                    double height = 0;
                    try {
                        height = Double.parseDouble(txtHeight.getText().trim());
                        if (height < 50 || height > 250) {
                            showAlert("Erreur de saisie", "La taille doit être entre 50 et 250 cm");
                            txtHeight.requestFocus();
                            txtHeight.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                            return;
                        }
                    } catch (NumberFormatException e) {
                        showAlert("Erreur de saisie", "La taille doit être un nombre valide");
                        txtHeight.requestFocus();
                        txtHeight.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                        return;
                    }
                    txtHeight.setStyle("");

                    patient p = new patient();
                    p.setName(nameField.getText().trim());
                    p.setSecond_name(secondNameField.getText().trim());
                    p.setAge(age);
                    p.setGender(genderField.getText().trim());
                    p.setPhone_number(phone);
                    p.setBirth_date(dateBirth.getValue().toString());
                    p.setEmail(email);
                    p.setPassword(password);
                    p.setRole(role);
                    p.setBlood_type(txtBloodType.getText().trim());
                    p.setWeight(weight);
                    p.setHeight(height);

                    serviceUser.ajouter(p);
                    servicePatient.ajouter(p);
                    break;

                case "doctor":
                    // Spécialité
                    if (txtSpecialty.getText().trim().isEmpty()) {
                        showAlert("Erreur de saisie", "La spécialité est obligatoire");
                        txtSpecialty.requestFocus();
                        txtSpecialty.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                        return;
                    }
                    txtSpecialty.setStyle("");

                    // Diplôme
                    if (txtDiplome.getText().trim().isEmpty()) {
                        showAlert("Erreur de saisie", "Le diplôme est obligatoire");
                        txtDiplome.requestFocus();
                        txtDiplome.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                        return;
                    }
                    txtDiplome.setStyle("");

                    // Expérience
                    int experience = 0;
                    try {
                        experience = Integer.parseInt(txtExperience.getText().trim());
                        if (experience < 0 || experience > 50) {
                            showAlert("Erreur de saisie", "L'expérience doit être entre 0 et 50 ans");
                            txtExperience.requestFocus();
                            txtExperience.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                            return;
                        }
                    } catch (NumberFormatException e) {
                        showAlert("Erreur de saisie", "L'expérience doit être un nombre valide");
                        txtExperience.requestFocus();
                        txtExperience.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                        return;
                    }
                    txtExperience.setStyle("");

                    // Tarif
                    double tarif = 0;
                    try {
                        tarif = Double.parseDouble(txtTarif.getText().trim());
                        if (tarif < 0) {
                            showAlert("Erreur de saisie", "Le tarif ne peut pas être négatif");
                            txtTarif.requestFocus();
                            txtTarif.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                            return;
                        }
                    } catch (NumberFormatException e) {
                        showAlert("Erreur de saisie", "Le tarif doit être un nombre valide");
                        txtTarif.requestFocus();
                        txtTarif.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                        return;
                    }
                    txtTarif.setStyle("");

                    doctor d = new doctor();
                    d.setName(nameField.getText().trim());
                    d.setSecond_name(secondNameField.getText().trim());
                    d.setAge(age);
                    d.setGender(genderField.getText().trim());
                    d.setPhone_number(phone);
                    d.setBirth_date(dateBirth.getValue().toString());
                    d.setEmail(email);
                    d.setPassword(password);
                    d.setRole(role);
                    d.setSpecialty(txtSpecialty.getText().trim());
                    d.setExperience(experience);
                    d.setDiplome(txtDiplome.getText().trim());
                    d.setDisponible(chkDisponible.isSelected());
                    d.setTarifConsultation(tarif);
                    d.setActif(chkActif.isSelected());

                    serviceUser.ajouter(d);
                    serviceDoctor.ajouter(d);
                    break;

                case "admin":
                    admin a = new admin();
                    a.setName(nameField.getText().trim());
                    a.setSecond_name(secondNameField.getText().trim());
                    a.setAge(age);
                    a.setGender(genderField.getText().trim());
                    a.setPhone_number(phone);
                    a.setBirth_date(dateBirth.getValue().toString());
                    a.setEmail(email);
                    a.setPassword(password);
                    a.setRole(role);
                    a.setActif(chkActifA.isSelected());

                    serviceUser.ajouter(a);
                    serviceAdmin.ajouter(a);
                    break;

                default:
                    showAlert("Erreur", "Rôle non valide");
                    return;
            }

            clearFields();
            showAlert("Succès", "Utilisateur ajouté avec succès !");
            System.out.println("✅ " + role + " added successfully");

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez vérifier les champs numériques");
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    @FXML
    private void updateUser() {
        showAlert("Info", "Fonctionnalité de modification à implémenter");
    }

    @FXML
    private void deleteUser() {
        showAlert("Info", "Fonctionnalité de suppression à implémenter");
    }

    private void clearFields() {
        // Effacer les champs utilisateur
        nameField.clear();
        secondNameField.clear();
        ageField.clear();
        genderField.clear();
        phoneField.clear();
        emailField.clear();
        passwordField.clear();
        dateBirth.setValue(null);
        roleBox.setValue(null);

        // ✅ AJOUT 3: Effacer les champs spécifiques
        txtBloodType.clear();
        txtWeight.clear();
        txtHeight.clear();
        txtSpecialty.clear();
        txtExperience.clear();
        txtDiplome.clear();
        chkDisponible.setSelected(false);
        txtTarif.clear();
        chkActif.setSelected(false);
        chkActifA.setSelected(false);

        // Cacher toutes les sections
        showRoleSpecificFields(null);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void goToLogin(ActionEvent actionEvent) {
        try {
            // Charger le fichier FXML de la page de login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/log_in.fxml"));
            Parent root = loader.load();

            // Récupérer la fenêtre actuelle
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

            // Changer la scène
            stage.setScene(new Scene(root));
            stage.setTitle("Login - GrowMind");
            stage.show();

            System.out.println("✅ Redirection vers login.fxml");

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page de connexion: " + e.getMessage());
        }
    }
}