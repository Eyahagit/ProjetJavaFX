package Controllers;

import Models.Cabinet;
import Service.ServiceCabinet;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;

import java.sql.SQLDataException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class AjouterCabinetController {

    @FXML private TextField txtNom;
    @FXML private TextField txtAdresse;
    @FXML private TextField txtVille;
    @FXML private TextField txtTelephone;
    @FXML private TextField txtEmail;
    @FXML private TextArea txtDescription;
    @FXML private ComboBox<String> comboStatus;
    @FXML private ListView<Cabinet> listCabinet;
    @FXML private Label lblCount;

    // Nouveaux composants pour recherche et tri
    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> comboFiltreStatus;
    @FXML private ComboBox<String> comboTri;
    @FXML private Button btnActualiser;

    private ServiceCabinet service = new ServiceCabinet();
    private Cabinet selectedCabinet;
    private ObservableList<Cabinet> masterData = FXCollections.observableArrayList();

    // Liste des status valides
    private final List<String> STATUS_VALIDES = Arrays.asList("Actif", "Inactif", "En attente", "Fermé");

    // Regex pour validation
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    private static final String TELEPHONE_REGEX = "^[0-9]{8}$";
    private static final String NOM_REGEX = "^[A-Za-z0-9\\s\\-&']{3,50}$";
    private static final String VILLE_REGEX = "^[A-Za-z\\s\\-]{2,50}$";

    @FXML
    public void initialize() {
        // Initialiser les ComboBox
        comboStatus.setItems(FXCollections.observableArrayList(STATUS_VALIDES));
        comboStatus.setValue("Actif");

        comboFiltreStatus.setItems(FXCollections.observableArrayList("Tous"));
        comboFiltreStatus.getItems().addAll(STATUS_VALIDES);
        comboFiltreStatus.setValue("Tous");

        comboTri.setItems(FXCollections.observableArrayList(
                "Nom (A-Z)", "Nom (Z-A)", "Ville (A-Z)", "Ville (Z-A)",
                "Status", "Date création (récent)", "Date création (ancien)"
        ));
        comboTri.setValue("Nom (A-Z)");

        // Restreindre téléphone aux chiffres
        txtTelephone.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtTelephone.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        // Auto-complétion email en minuscules
        txtEmail.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                txtEmail.setText(newValue.toLowerCase());
            }
        });

        // Charger les données
        loadData();

        // Configurer l'affichage personnalisé
        setupListViewCellFactory();

        // Configuration recherche et tri
        setupSearchAndSort();

        // Sélection dans le ListView
        listCabinet.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        selectedCabinet = newSelection;
                        remplirChamps(selectedCabinet);
                    }
                }
        );
    }

    private void setupListViewCellFactory() {
        listCabinet.setCellFactory(lv -> new ListCell<Cabinet>() {
            @Override
            protected void updateItem(Cabinet cabinet, boolean empty) {
                super.updateItem(cabinet, empty);

                if (empty || cabinet == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox container = new HBox(15);
                    container.setAlignment(Pos.CENTER_LEFT);
                    container.setStyle("-fx-padding: 10; -fx-background-color: transparent;");

                    // Badge ID
                    Label idLabel = new Label("ID: " + cabinet.getIdCabinet());
                    idLabel.setStyle("-fx-background-color: #5FB49C; " +
                            "-fx-text-fill: white; " +
                            "-fx-background-radius: 15; " +
                            "-fx-padding: 5 12; " +
                            "-fx-font-weight: bold;");

                    VBox infoBox = new VBox(5);

                    // Nom
                    Label nomLabel = new Label(cabinet.getNomcabinet());
                    nomLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

                    // Adresse et Ville
                    HBox ligne1 = new HBox(10);
                    ligne1.setAlignment(Pos.CENTER_LEFT);

                    Label adresseLabel = new Label("📍 " + cabinet.getAdresse());
                    adresseLabel.setStyle("-fx-text-fill: #34495e;");

                    Label villeLabel = new Label("🏙️ " + cabinet.getVille());
                    villeLabel.setStyle("-fx-text-fill: #34495e;");

                    ligne1.getChildren().addAll(adresseLabel, new Label("|"), villeLabel);

                    // Contact
                    HBox ligne2 = new HBox(10);
                    ligne2.setAlignment(Pos.CENTER_LEFT);

                    Label telLabel = new Label("📞 " + cabinet.getTelephone());
                    telLabel.setStyle("-fx-text-fill: #7f8c8d;");

                    Label emailLabel = new Label("✉️ " + cabinet.getEmail());
                    emailLabel.setStyle("-fx-text-fill: #7f8c8d;");

                    ligne2.getChildren().addAll(telLabel, new Label("•"), emailLabel);

                    // Status
                    Label statusLabel = new Label(cabinet.getStatus());
                    String statusStyle = "-fx-background-radius: 10; -fx-padding: 3 10; -fx-font-size: 12px; -fx-font-weight: bold; ";

                    switch (cabinet.getStatus()) {
                        case "Actif":
                            statusStyle += "-fx-background-color: #27ae60; -fx-text-fill: white;";
                            break;
                        case "Inactif":
                            statusStyle += "-fx-background-color: #e74c3c; -fx-text-fill: white;";
                            break;
                        case "En attente":
                            statusStyle += "-fx-background-color: #f39c12; -fx-text-fill: white;";
                            break;
                        case "Fermé":
                            statusStyle += "-fx-background-color: #3498db; -fx-text-fill: white;";
                            break;
                        default:
                            statusStyle += "-fx-background-color: #7f8c8d; -fx-text-fill: white;";
                    }
                    statusLabel.setStyle(statusStyle);

                    // Description (si non vide)
                    if (cabinet.getDescription() != null && !cabinet.getDescription().isEmpty()) {
                        Label descLabel = new Label("📝 " + cabinet.getDescription());
                        descLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic; -fx-font-size: 11px;");
                        infoBox.getChildren().addAll(nomLabel, ligne1, ligne2, descLabel);
                    } else {
                        infoBox.getChildren().addAll(nomLabel, ligne1, ligne2);
                    }

                    container.getChildren().addAll(idLabel, infoBox, statusLabel);
                    setGraphic(container);
                }
            }
        });
    }

    private void setupSearchAndSort() {
        txtRecherche.textProperty().addListener((observable, oldValue, newValue) -> filterList());
        comboFiltreStatus.valueProperty().addListener((observable, oldValue, newValue) -> filterList());
        comboTri.valueProperty().addListener((observable, oldValue, newValue) -> sortList());
    }

    private void filterList() {
        String searchText = txtRecherche.getText().toLowerCase();
        String statusFilter = comboFiltreStatus.getValue();

        ObservableList<Cabinet> filtered = FXCollections.observableArrayList();

        for (Cabinet c : masterData) {
            boolean matchSearch = true;
            boolean matchStatus = true;

            // Filtre recherche
            if (!searchText.isEmpty()) {
                matchSearch = c.getNomcabinet().toLowerCase().contains(searchText) ||
                        c.getAdresse().toLowerCase().contains(searchText) ||
                        c.getVille().toLowerCase().contains(searchText) ||
                        c.getEmail().toLowerCase().contains(searchText) ||
                        String.valueOf(c.getTelephone()).contains(searchText) ||
                        (c.getDescription() != null && c.getDescription().toLowerCase().contains(searchText));
            }

            // Filtre status
            if (!statusFilter.equals("Tous")) {
                matchStatus = c.getStatus().equals(statusFilter);
            }

            if (matchSearch && matchStatus) {
                filtered.add(c);
            }
        }

        listCabinet.setItems(filtered);
        lblCount.setText(String.valueOf(filtered.size()));
        sortList();
    }

    private void sortList() {
        String critere = comboTri.getValue();
        ObservableList<Cabinet> items = listCabinet.getItems();

        if (critere != null && items != null) {
            switch (critere) {
                case "Nom (A-Z)":
                    items.sort((c1, c2) -> c1.getNomcabinet().compareToIgnoreCase(c2.getNomcabinet()));
                    break;
                case "Nom (Z-A)":
                    items.sort((c1, c2) -> c2.getNomcabinet().compareToIgnoreCase(c1.getNomcabinet()));
                    break;
                case "Ville (A-Z)":
                    items.sort((c1, c2) -> c1.getVille().compareToIgnoreCase(c2.getVille()));
                    break;
                case "Ville (Z-A)":
                    items.sort((c1, c2) -> c2.getVille().compareToIgnoreCase(c1.getVille()));
                    break;
                case "Status":
                    items.sort((c1, c2) -> c1.getStatus().compareTo(c2.getStatus()));
                    break;
            }
        }
    }

    private void loadData() {
        masterData.clear();
        masterData.addAll(service.recuperer());
        listCabinet.setItems(masterData);
        lblCount.setText(String.valueOf(masterData.size()));
    }

    private void remplirChamps(Cabinet c) {
        if (c != null) {
            txtNom.setText(c.getNomcabinet());
            txtAdresse.setText(c.getAdresse());
            txtVille.setText(c.getVille());
            txtTelephone.setText(String.valueOf(c.getTelephone()));
            txtEmail.setText(c.getEmail());
            txtDescription.setText(c.getDescription());
            comboStatus.setValue(c.getStatus());
        }
    }

    private void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean validerChamps() {
        // Vérification champs vides
        if (txtNom.getText().trim().isEmpty()) {
            showAlert("Erreur", "❌ Le nom du cabinet est obligatoire", AlertType.ERROR);
            txtNom.requestFocus();
            return false;
        }

        if (txtAdresse.getText().trim().isEmpty()) {
            showAlert("Erreur", "❌ L'adresse est obligatoire", AlertType.ERROR);
            txtAdresse.requestFocus();
            return false;
        }

        if (txtVille.getText().trim().isEmpty()) {
            showAlert("Erreur", "❌ La ville est obligatoire", AlertType.ERROR);
            txtVille.requestFocus();
            return false;
        }

        if (txtTelephone.getText().trim().isEmpty()) {
            showAlert("Erreur", "❌ Le téléphone est obligatoire", AlertType.ERROR);
            txtTelephone.requestFocus();
            return false;
        }

        if (txtEmail.getText().trim().isEmpty()) {
            showAlert("Erreur", "❌ L'email est obligatoire", AlertType.ERROR);
            txtEmail.requestFocus();
            return false;
        }

        if (comboStatus.getValue() == null) {
            showAlert("Erreur", "❌ Le status est obligatoire", AlertType.ERROR);
            comboStatus.requestFocus();
            return false;
        }

        // Validation format
        String nom = txtNom.getText().trim();
        if (!Pattern.matches(NOM_REGEX, nom)) {
            showAlert("Erreur", "❌ Nom invalide (3-50 caractères, lettres, chiffres, -&')", AlertType.ERROR);
            txtNom.requestFocus();
            return false;
        }

        String ville = txtVille.getText().trim();
        if (!Pattern.matches(VILLE_REGEX, ville)) {
            showAlert("Erreur", "❌ Ville invalide (2-50 caractères, lettres uniquement)", AlertType.ERROR);
            txtVille.requestFocus();
            return false;
        }

        String telephone = txtTelephone.getText().trim();
        if (!Pattern.matches(TELEPHONE_REGEX, telephone)) {
            showAlert("Erreur", "❌ Téléphone invalide (8 chiffres)", AlertType.ERROR);
            txtTelephone.requestFocus();
            return false;
        }

        String email = txtEmail.getText().trim();
        if (!Pattern.matches(EMAIL_REGEX, email)) {
            showAlert("Erreur", "❌ Email invalide", AlertType.ERROR);
            txtEmail.requestFocus();
            return false;
        }

        return true;
    }

    @FXML
    private void ajouterCabinet() {
        if (!validerChamps()) {
            return;
        }

        try {
            Cabinet c = new Cabinet(
                    txtNom.getText().trim(),
                    txtAdresse.getText().trim(),
                    txtVille.getText().trim(),
                    Integer.parseInt(txtTelephone.getText().trim()),
                    txtEmail.getText().trim().toLowerCase(),
                    txtDescription.getText().trim(),
                    comboStatus.getValue()
            );

            service.ajouter(c);
            loadData();
            clear();
            showAlert("Succès", "✅ Cabinet ajouté avec succès !", AlertType.INFORMATION);

        } catch (Exception e) {
            showAlert("Erreur", "❌ Erreur lors de l'ajout: " + e.getMessage(), AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void modifierCabinet() throws SQLDataException {
        if (selectedCabinet == null) {
            showAlert("Erreur", "❌ Veuillez sélectionner un cabinet", AlertType.ERROR);
            return;
        }

        if (!validerChamps()) {
            return;
        }

        try {
            selectedCabinet.setNomcabinet(txtNom.getText().trim());
            selectedCabinet.setAdresse(txtAdresse.getText().trim());
            selectedCabinet.setVille(txtVille.getText().trim());
            selectedCabinet.setTelephone(Integer.parseInt(txtTelephone.getText().trim()));
            selectedCabinet.setEmail(txtEmail.getText().trim().toLowerCase());
            selectedCabinet.setDescription(txtDescription.getText().trim());
            selectedCabinet.setStatus(comboStatus.getValue());

            service.modifier(selectedCabinet);
            loadData();
            clear();
            showAlert("Succès", "✅ Cabinet modifié avec succès !", AlertType.INFORMATION);

        } catch (Exception e) {
            showAlert("Erreur", "❌ Erreur lors de la modification: " + e.getMessage(), AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void supprimerCabinet() throws SQLDataException {
        if (selectedCabinet == null) {
            showAlert("Erreur", "❌ Veuillez sélectionner un cabinet", AlertType.ERROR);
            return;
        }

        Alert confirmation = new Alert(AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer le cabinet");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer " + selectedCabinet.getNomcabinet() + " ?");

        if (confirmation.showAndWait().get() == ButtonType.OK) {
            try {
                service.supprimer(selectedCabinet);
                loadData();
                clear();
                showAlert("Succès", "✅ Cabinet supprimé avec succès !", AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Erreur", "❌ Erreur lors de la suppression: " + e.getMessage(), AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void actualiser() {
        loadData();
        txtRecherche.clear();
        comboFiltreStatus.setValue("Tous");
        comboTri.setValue("Nom (A-Z)");
        filterList();
    }

    @FXML
    private void clear() {
        txtNom.clear();
        txtAdresse.clear();
        txtVille.clear();
        txtTelephone.clear();
        txtEmail.clear();
        txtDescription.clear();
        comboStatus.setValue("Actif");
        selectedCabinet = null;
        listCabinet.getSelectionModel().clearSelection();
    }

    @FXML
    private void annuler() {
        clear();
    }
}