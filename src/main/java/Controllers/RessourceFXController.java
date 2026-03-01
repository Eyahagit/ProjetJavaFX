package Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import Models.Ressource;
import Models.users;
import Services.ressourceService;
import Services.CloudinaryService;
import Services.MailService;
import utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class RessourceFXController {

    // Form fields
    @FXML private TextField txtTitre;
    @FXML private TextArea txtDescription;
    @FXML private TextField txtLocalisation;
    @FXML private TextField txtAuthor;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> comboType;
    @FXML private ComboBox<String> comboCategory;
    @FXML private ComboBox<String> comboStatus;

    // Buttons
    @FXML private Button btnAjouter;
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;
    @FXML private Button btnClear;
    @FXML private Button btnUploadImage;
    @FXML private Button btnStat;
    @FXML private Button btnSwitchToUser;
    @FXML private Button btnHome;
    @FXML private Button btnGoToRespo; // Nouveau bouton pour aller vers main_respo_bib

    // Table
    @FXML private TableView<Ressource> listEvenements;
    @FXML private TableColumn<Ressource, String> colTitre;
    @FXML private TableColumn<Ressource, String> colDescription;
    @FXML private TableColumn<Ressource, String> colType;
    @FXML private TableColumn<Ressource, String> colCategory;
    @FXML private TableColumn<Ressource, LocalDate> colDate;
    @FXML private TableColumn<Ressource, String> colAuthor;

    // Search and stats
    @FXML private TextField searchField;
    @FXML private Label lblCount;
    @FXML private Label lblDate;

    // Panels
    @FXML private VBox listPanel;
    @FXML private VBox statsPanel;
    @FXML private VBox formPanel;

    // Services
    private final ressourceService service = new ressourceService();
    private CloudinaryService cloudinaryService;

    // Data
    private ObservableList<Ressource> ressourceList = FXCollections.observableArrayList();
    private FilteredList<Ressource> filteredData;

    // User
    private users currentUser;

    @FXML
    public void initialize() {
        System.out.println("\n=== Initialisation RessourceFXController ===");

        // Afficher la date
        if (lblDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            lblDate.setText(LocalDate.now().format(formatter));
        }

        // Récupérer l'utilisateur connecté
        currentUser = SessionManager.getInstance().getCurrentUser();

        // Initialiser les ComboBox
        setupComboBoxes();

        // Initialiser les colonnes du tableau
        setupTableColumns();

        // Charger les données
        refreshList();

        // Configurer la recherche
        setupSearchFilter();

        // Ajouter un listener de sélection
        listEvenements.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                showDetails(newVal);
                System.out.println("✅ Ressource sélectionnée: " + newVal.getTitle());
            }
        });

        // Vérifier que les données sont bien chargées
        listEvenements.setItems(ressourceList);

        System.out.println("=== Initialisation terminée ===\n");
    }

    /**
     * Définit l'utilisateur connecté
     */
    public void setCurrentUser(users user) {
        if (user != null) {
            this.currentUser = user;
            SessionManager.getInstance().setCurrentUser(user);
            System.out.println("✅ Utilisateur défini dans RessourceFXController: " + user.getName() + " " + user.getSecond_name());

            // Mettre à jour l'auteur par défaut avec le nom de l'utilisateur connecté
            if (txtAuthor != null) {
                txtAuthor.setText(user.getName() + " " + user.getSecond_name());
            }
        }
    }

    /**
     * Alias pour setCurrentUser
     */
    public void setUser(users user) {
        setCurrentUser(user);
    }

    private void setupComboBoxes() {
        comboType.getItems().addAll("formation", "article", "video", "image", "evenement", "livre", "podcast");
        comboCategory.getItems().addAll("santé", "bien-etre", "developpement personnel", "motivation", "psychologie", "méditation");
        comboStatus.getItems().addAll("Active", "Draft", "Archivé");

        // Valeurs par défaut
        comboStatus.setValue("Active");
        if (currentUser != null && txtAuthor != null) {
            txtAuthor.setText(currentUser.getName() + " " + currentUser.getSecond_name());
        } else if (txtAuthor != null) {
            txtAuthor.setText("Admin");
        }
        if (datePicker != null) datePicker.setValue(LocalDate.now());
    }

    private void setupTableColumns() {
        colTitre.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
        colAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
    }

    private void setupSearchFilter() {
        filteredData = new FilteredList<>(ressourceList, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(ressource -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                return ressource.getTitle().toLowerCase().contains(lowerCaseFilter)
                        || ressource.getDescription().toLowerCase().contains(lowerCaseFilter)
                        || ressource.getType().toLowerCase().contains(lowerCaseFilter)
                        || ressource.getCategory().toLowerCase().contains(lowerCaseFilter);
            });

            SortedList<Ressource> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(listEvenements.comparatorProperty());
            listEvenements.setItems(sortedData);
            updateCount();
        });
    }

    @FXML
    private void handleHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/home.fxml"));
            Parent root = loader.load();

            HomeController homeController = loader.getController();
            if (homeController != null && currentUser != null) {
                homeController.setUser(currentUser);
            }

            Stage stage = (Stage) btnHome.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Accueil - GrowMind");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de retourner à l'accueil");
        }
    }

    @FXML
    private void handleSearch() {
        // La recherche est déjà gérée par le listener
    }

    public void refreshList() {
        try {
            List<Ressource> list = service.findAll();
            System.out.println("📊 Nombre de ressources chargées: " + list.size());

            // Afficher la première ressource pour vérifier
            if (!list.isEmpty()) {
                Ressource first = list.get(0);
                System.out.println("   Titre: " + first.getTitle());
                System.out.println("   Description: " + first.getDescription());
                System.out.println("   Type: " + first.getType());
                System.out.println("   Catégorie: " + first.getCategory());
                System.out.println("   Date: " + first.getDateCreation());
                System.out.println("   Auteur: " + first.getAuthor());
            }

            ressourceList.setAll(list);
            listEvenements.setItems(ressourceList);
            listEvenements.refresh();

            updateCount();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateCount() {
        int count = listEvenements.getItems().size();
        lblCount.setText(count + " ressource" + (count > 1 ? "s" : ""));
    }

    private void showDetails(Ressource r) {
        txtTitre.setText(r.getTitle());
        txtDescription.setText(r.getDescription());
        txtLocalisation.setText(r.getContent());
        txtAuthor.setText(r.getAuthor());
        datePicker.setValue(r.getDateCreation());
        comboType.setValue(r.getType());
        comboCategory.setValue(r.getCategory());
        comboStatus.setValue(r.getStatus() != null ? r.getStatus() : "Active");
    }

    @FXML
    private void clearForm() {
        txtTitre.clear();
        txtDescription.clear();
        txtLocalisation.clear();
        if (currentUser != null) {
            txtAuthor.setText(currentUser.getName() + " " + currentUser.getSecond_name());
        } else {
            txtAuthor.setText("Admin");
        }
        datePicker.setValue(LocalDate.now());
        comboType.setValue(null);
        comboCategory.setValue(null);
        comboStatus.setValue("Active");
        listEvenements.getSelectionModel().clearSelection();
    }

    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        if (txtTitre.getText() == null || txtTitre.getText().trim().isEmpty()) {
            errors.append("- Le titre est obligatoire.\n");
        }
        if (txtDescription.getText() == null || txtDescription.getText().trim().isEmpty()) {
            errors.append("- La description est obligatoire.\n");
        }
        if (comboType.getValue() == null) {
            errors.append("- Le type est obligatoire.\n");
        }
        if (comboCategory.getValue() == null) {
            errors.append("- La catégorie est obligatoire.\n");
        }
        if (datePicker.getValue() == null) {
            errors.append("- La date est obligatoire.\n");
        }
        if (txtAuthor.getText() == null || txtAuthor.getText().trim().isEmpty()) {
            errors.append("- L'auteur est obligatoire.\n");
        }

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Erreurs de validation",
                    "Veuillez corriger les erreurs suivantes :\n\n" + errors.toString());
            return false;
        }
        return true;
    }

    @FXML
    private void ajouterEvenement() {
        if (!validateForm()) return;

        Ressource r = new Ressource();
        r.setTitle(txtTitre.getText().trim());
        r.setDescription(txtDescription.getText().trim());
        r.setType(comboType.getValue());
        r.setCategory(comboCategory.getValue());
        r.setContent(txtLocalisation.getText());
        r.setAuthor(txtAuthor.getText().trim());
        r.setDateCreation(datePicker.getValue());
        r.setStatus(comboStatus.getValue());

        service.create(r);
        refreshList();
        clearForm();
        showInfo("✅ Succès", "Ressource ajoutée avec succès !");
    }

    @FXML
    private void modifierEvenement() {
        Ressource selected = listEvenements.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Veuillez sélectionner une ressource à modifier.");
            return;
        }

        selected.setTitle(txtTitre.getText().trim());
        selected.setDescription(txtDescription.getText().trim());
        selected.setType(comboType.getValue());
        selected.setCategory(comboCategory.getValue());
        selected.setContent(txtLocalisation.getText());
        selected.setAuthor(txtAuthor.getText().trim());
        if (datePicker.getValue() != null) {
            selected.setDateCreation(datePicker.getValue());
        }
        selected.setStatus(comboStatus.getValue());

        service.update(selected);
        refreshList();
        showInfo("✅ Succès", "Ressource modifiée avec succès !");
    }

    @FXML
    private void supprimerEvenement() {
        Ressource selected = listEvenements.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Veuillez sélectionner une ressource à supprimer.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer la ressource ?");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer : " + selected.getTitle() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            service.delete(selected.getId());
            refreshList();
            clearForm();
            showInfo("✅ Succès", "Ressource supprimée avec succès !");
        }
    }

    @FXML
    private void uploadImageToCloudinary() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choisir une image");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = chooser.showOpenDialog(txtLocalisation.getScene().getWindow());
        if (file == null) return;

        try {
            if (cloudinaryService == null) {
                cloudinaryService = new CloudinaryService();
            }
            String url = cloudinaryService.upload(file);
            if (url != null && !url.isBlank()) {
                txtLocalisation.setText(url);
                showInfo("Upload réussi", "Lien Cloudinary copié dans le champ.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur upload", e.getMessage());
        }
    }

    @FXML
    private void openStats() {
        showInfo("Statistiques", "Fonctionnalité à venir...");
    }

    @FXML
    private void switchToUser() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/userressource.fxml"));
            Parent root = loader.load();

            UserRessourceController controller = loader.getController();
            if (controller != null && currentUser != null) {
                controller.setCurrentUser(currentUser);
            }

            Stage stage = (Stage) btnSwitchToUser.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Espace Utilisateur - Ressources");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la vue utilisateur");
        }
    }

    /**
     * Gère la navigation vers main_respo_bib.fxml
     */
    @FXML
    private void handleGoToRessource() {
        System.out.println("\n=== Navigation vers Responsabilité & Bibliothèque ===");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main_respo_bib.fxml"));
            Parent root = loader.load();

            // Passer l'utilisateur connecté
            Object controller = loader.getController();
            if (controller != null && currentUser != null) {
                try {
                    controller.getClass().getMethod("setCurrentUser", users.class).invoke(controller, currentUser);
                    System.out.println("✅ Utilisateur transmis");
                } catch (NoSuchMethodException e) {
                    try {
                        controller.getClass().getMethod("setUser", users.class).invoke(controller, currentUser);
                        System.out.println("✅ Utilisateur transmis via setUser");
                    } catch (Exception ex) {
                        System.out.println("⚠️ Impossible de transmettre l'utilisateur: " + ex.getMessage());
                    }
                } catch (Exception e) {
                    System.out.println("⚠️ Impossible de transmettre l'utilisateur: " + e.getMessage());
                }
            }

            Stage stage = (Stage) btnGoToRespo.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Responsabilité & Bibliothèque - GrowMind");
            stage.show();

            System.out.println("✅ Navigation réussie");

        } catch (IOException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page.\n" + e.getMessage());
        }
    }

    @FXML
    private void handleListClick() {
        // Géré par le listener
    }

    // Méthodes pour les effets hover
    @FXML private void hoverAjouter() {
        if (btnAjouter != null) btnAjouter.setStyle("-fx-background-color: #4AA189; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;");
    }
    @FXML private void exitAjouter() {
        if (btnAjouter != null) btnAjouter.setStyle("-fx-background-color: #5FB49C; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;");
    }
    @FXML private void hoverModifier() {
        if (btnModifier != null) btnModifier.setStyle("-fx-background-color: #3A5F8A; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;");
    }
    @FXML private void exitModifier() {
        if (btnModifier != null) btnModifier.setStyle("-fx-background-color: #4A6FA5; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;");
    }
    @FXML private void hoverSupprimer() {
        if (btnSupprimer != null) btnSupprimer.setStyle("-fx-background-color: #D3549C; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;");
    }
    @FXML private void exitSupprimer() {
        if (btnSupprimer != null) btnSupprimer.setStyle("-fx-background-color: #E667AF; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}