package Controllers;

import Models.Cabinet;
import Models.Psychologue;
import Services.ServiceCabinet;
import Services.ServicePsychologue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;

import java.sql.SQLDataException;
import java.util.List;
import java.util.regex.Pattern;

public class AjouterPsychologueController {

    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private TextField txtSpecialite;
    @FXML private TextField txtDiplome;
    @FXML private TextField txtExperience;
    @FXML private TextField txtTarif;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelephone;
    @FXML private ComboBox<Cabinet> comboCabinet;  // ← MODIFIÉ

    @FXML private ListView<Psychologue> listPsychologue;
    @FXML private Label lblCount;

    // Nouveaux composants
    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> comboTri;
    @FXML private ComboBox<String> comboFiltreCabinet;
    @FXML private Button btnActualiser;

    private ServicePsychologue service = new ServicePsychologue();
    private Psychologue selectedPsychologue;
    private ObservableList<Psychologue> masterData = FXCollections.observableArrayList();

    // Regex pour validation
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    private static final String TELEPHONE_REGEX = "^[0-9]{8}$";
    private static final String NOM_REGEX = "^[A-Za-z\\s\\-]{2,50}$";
    private static final String SPECIALITE_REGEX = "^[A-Za-z\\s\\-]{3,100}$";
    private static final String DIPLOME_REGEX = "^[A-Za-z0-9\\s\\-]{3,100}$";

    @FXML
    public void initialize() {
        try {
            // Restreindre les champs numériques
            txtExperience.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    txtExperience.setText(newValue.replaceAll("[^\\d]", ""));
                }
            });

            txtTarif.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*\\.?\\d*")) {
                    txtTarif.setText(newValue.replaceAll("[^\\d.]", ""));
                }
            });

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

            // Initialiser les ComboBox de tri
            comboTri.setItems(FXCollections.observableArrayList(
                    "Nom (A-Z)", "Nom (Z-A)", "Prénom (A-Z)", "Prénom (Z-A)",
                    "Spécialité", "Expérience (décroissant)", "Expérience (croissant)",
                    "Tarif (décroissant)", "Tarif (croissant)", "Cabinet ID"
            ));
            comboTri.setValue("Nom (A-Z)");

            // Initialiser le filtre cabinet
            comboFiltreCabinet.setItems(FXCollections.observableArrayList("Tous"));
            comboFiltreCabinet.setValue("Tous");

            // Charger les cabinets dans la ComboBox
            chargerCabinets();

            // Charger les données
            loadData();

            // Configurer l'affichage personnalisé
            setupListViewCellFactory();

            // Configuration recherche et tri
            setupSearchAndSort();

            // Sélection dans le ListView
            listPsychologue.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldSelection, newSelection) -> {
                        if (newSelection != null) {
                            selectedPsychologue = newSelection;
                            remplirChamps(selectedPsychologue);
                        }
                    }
            );

        } catch (SQLDataException e) {
            showAlert("Erreur", "❌ Erreur de chargement des données: " + e.getMessage(), AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // NOUVELLE MÉTHODE
    private void chargerCabinets() {
        ServiceCabinet serviceCabinet = new ServiceCabinet();
        List<Cabinet> cabinets = serviceCabinet.recuperer();

        ObservableList<Cabinet> cabinetItems = FXCollections.observableArrayList(cabinets);
        comboCabinet.setItems(cabinetItems);

        // Personnaliser l'affichage
        comboCabinet.setCellFactory(lv -> new ListCell<Cabinet>() {
            @Override
            protected void updateItem(Cabinet c, boolean empty) {
                super.updateItem(c, empty);
                if (empty || c == null) {
                    setText(null);
                } else {
                    setText(c.getNomcabinet() + " (" + c.getVille() + ")");
                }
            }
        });

        comboCabinet.setButtonCell(new ListCell<Cabinet>() {
            @Override
            protected void updateItem(Cabinet c, boolean empty) {
                super.updateItem(c, empty);
                if (empty || c == null) {
                    setText(null);
                } else {
                    setText(c.getNomcabinet() + " (" + c.getVille() + ")");
                }
            }
        });

    }

    private void setupListViewCellFactory() {
        listPsychologue.setCellFactory(lv -> new ListCell<Psychologue>() {
            @Override
            protected void updateItem(Psychologue p, boolean empty) {
                super.updateItem(p, empty);

                if (empty || p == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox container = new HBox(15);
                    container.setAlignment(Pos.CENTER_LEFT);
                    container.setStyle("-fx-padding: 12; -fx-background-color: transparent; -fx-border-color: transparent transparent #f0f0f0 transparent;");

                    Label idLabel = new Label("#" + p.getIdPsychologue());
                    idLabel.setStyle("-fx-background-color: #5FB49C; -fx-text-fill: white; -fx-background-radius: 15; -fx-padding: 5 12; -fx-font-weight: bold; -fx-min-width: 50; -fx-alignment: center;");

                    VBox infoBox = new VBox(8);
                    Label nomComplet = new Label(p.getNom() + " " + p.getPrenom());
                    nomComplet.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

                    HBox ligne1 = new HBox(10);
                    ligne1.setAlignment(Pos.CENTER_LEFT);
                    Label specialiteLabel = new Label("🔬 " + p.getSpecialite());
                    specialiteLabel.setStyle("-fx-text-fill: #34495e;");
                    Label diplomeLabel = new Label("🎓 " + p.getDiplome());
                    diplomeLabel.setStyle("-fx-text-fill: #34495e;");
                    ligne1.getChildren().addAll(specialiteLabel, new Label("|"), diplomeLabel);

                    HBox ligne2 = new HBox(10);
                    ligne2.setAlignment(Pos.CENTER_LEFT);
                    Label emailLabel = new Label("✉️ " + p.getEmail());
                    emailLabel.setStyle("-fx-text-fill: #7f8c8d;");
                    Label telLabel = new Label("📞 " + p.getTelephone());
                    telLabel.setStyle("-fx-text-fill: #7f8c8d;");
                    ligne2.getChildren().addAll(emailLabel, new Label("•"), telLabel);

                    HBox ligne3 = new HBox(15);
                    ligne3.setAlignment(Pos.CENTER_LEFT);

                    Label expLabel = new Label("⭐ " + p.getExperience() + " ans");
                    expLabel.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 3 10;");

                    Label tarifLabel = new Label("💰 " + p.getTarif() + " DT");
                    tarifLabel.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 3 10;");

                    String cabinetText;
                    if (p.getNomCabinet() != null && !p.getNomCabinet().isEmpty()) {
                        cabinetText = p.getNomCabinet() + " (" + p.getVilleCabinet() + ")";
                    } else {
                        cabinetText = "Cabinet #" + p.getIdCabinet();
                    }

                    Label cabinetLabel = new Label("🏢 " + cabinetText);
                    cabinetLabel.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 3 10;");

                    ligne3.getChildren().addAll(expLabel, tarifLabel, cabinetLabel);
                    infoBox.getChildren().addAll(nomComplet, ligne1, ligne2, ligne3);
                    container.getChildren().addAll(idLabel, infoBox);

                    setGraphic(container);
                }
            }
        });
    }

    private void setupSearchAndSort() {
        txtRecherche.textProperty().addListener((observable, oldValue, newValue) -> filterList());
        comboTri.valueProperty().addListener((observable, oldValue, newValue) -> sortList());
        comboFiltreCabinet.valueProperty().addListener((observable, oldValue, newValue) -> filterList());
    }

    private void filterList() {
        String searchText = txtRecherche.getText().toLowerCase();
        String cabinetFilter = comboFiltreCabinet.getValue();

        ObservableList<Psychologue> filtered = FXCollections.observableArrayList();

        for (Psychologue p : masterData) {
            boolean matchSearch = true;
            boolean matchCabinet = true;

            if (!searchText.isEmpty()) {
                matchSearch = p.getNom().toLowerCase().contains(searchText) ||
                        p.getPrenom().toLowerCase().contains(searchText) ||
                        p.getSpecialite().toLowerCase().contains(searchText) ||
                        p.getDiplome().toLowerCase().contains(searchText) ||
                        p.getEmail().toLowerCase().contains(searchText) ||
                        p.getTelephone().contains(searchText);
            }

            if (cabinetFilter != null && !cabinetFilter.equals("Tous")) {
                try {
                    int cabinetId = Integer.parseInt(cabinetFilter);
                    matchCabinet = p.getIdCabinet() == cabinetId;
                } catch (NumberFormatException e) {
                    matchCabinet = true;
                }
            }

            if (matchSearch && matchCabinet) {
                filtered.add(p);
            }
        }

        listPsychologue.setItems(filtered);
        lblCount.setText(String.valueOf(filtered.size()));
        sortList();
    }

    private void sortList() {
        String critere = comboTri.getValue();
        ObservableList<Psychologue> items = listPsychologue.getItems();

        if (critere != null && items != null) {
            switch (critere) {
                case "Nom (A-Z)":
                    items.sort((p1, p2) -> p1.getNom().compareToIgnoreCase(p2.getNom()));
                    break;
                case "Nom (Z-A)":
                    items.sort((p1, p2) -> p2.getNom().compareToIgnoreCase(p1.getNom()));
                    break;
                case "Prénom (A-Z)":
                    items.sort((p1, p2) -> p1.getPrenom().compareToIgnoreCase(p2.getPrenom()));
                    break;
                case "Prénom (Z-A)":
                    items.sort((p1, p2) -> p2.getPrenom().compareToIgnoreCase(p1.getPrenom()));
                    break;
                case "Spécialité":
                    items.sort((p1, p2) -> p1.getSpecialite().compareToIgnoreCase(p2.getSpecialite()));
                    break;
                case "Expérience (décroissant)":
                    items.sort((p1, p2) -> Integer.compare(p2.getExperience(), p1.getExperience()));
                    break;
                case "Expérience (croissant)":
                    items.sort((p1, p2) -> Integer.compare(p1.getExperience(), p2.getExperience()));
                    break;
                case "Tarif (décroissant)":
                    items.sort((p1, p2) -> Double.compare(p2.getTarif(), p1.getTarif()));
                    break;
                case "Tarif (croissant)":
                    items.sort((p1, p2) -> Double.compare(p1.getTarif(), p2.getTarif()));
                    break;
                case "Cabinet ID":
                    items.sort((p1, p2) -> Integer.compare(p1.getIdCabinet(), p2.getIdCabinet()));
                    break;
            }
        }
    }

    private void updateCabinetFilter() {
        ObservableList<String> cabinetIds = FXCollections.observableArrayList("Tous");
        for (Psychologue p : masterData) {
            String id = String.valueOf(p.getIdCabinet());
            if (!cabinetIds.contains(id)) {
                cabinetIds.add(id);
            }
        }
        comboFiltreCabinet.setItems(cabinetIds);
        comboFiltreCabinet.setValue("Tous");
    }

    private void loadData() throws SQLDataException {
        masterData.clear();
        masterData.addAll(service.recupererAvecCabinet());
        listPsychologue.setItems(masterData);
        lblCount.setText(String.valueOf(masterData.size()));
        updateCabinetFilter();
    }

    private void remplirChamps(Psychologue p) {
        if (p != null) {
            txtNom.setText(p.getNom());
            txtPrenom.setText(p.getPrenom());
            txtSpecialite.setText(p.getSpecialite());
            txtDiplome.setText(p.getDiplome());
            txtExperience.setText(String.valueOf(p.getExperience()));
            txtTarif.setText(String.valueOf(p.getTarif()));
            txtEmail.setText(p.getEmail());
            txtTelephone.setText(p.getTelephone());

            // MODIFIÉ : Sélectionner le cabinet dans la ComboBox
            for (Cabinet c : comboCabinet.getItems()) {
                if (c.getIdCabinet() == p.getIdCabinet()) {
                    comboCabinet.setValue(c);
                    break;
                }
            }
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
        if (txtNom.getText().trim().isEmpty()) {
            showAlert("Erreur", "❌ Le nom est obligatoire", AlertType.ERROR);
            txtNom.requestFocus();
            return false;
        }

        if (txtPrenom.getText().trim().isEmpty()) {
            showAlert("Erreur", "❌ Le prénom est obligatoire", AlertType.ERROR);
            txtPrenom.requestFocus();
            return false;
        }

        if (txtSpecialite.getText().trim().isEmpty()) {
            showAlert("Erreur", "❌ La spécialité est obligatoire", AlertType.ERROR);
            txtSpecialite.requestFocus();
            return false;
        }

        if (txtDiplome.getText().trim().isEmpty()) {
            showAlert("Erreur", "❌ Le diplôme est obligatoire", AlertType.ERROR);
            txtDiplome.requestFocus();
            return false;
        }

        if (txtExperience.getText().trim().isEmpty()) {
            showAlert("Erreur", "❌ L'expérience est obligatoire", AlertType.ERROR);
            txtExperience.requestFocus();
            return false;
        }

        if (txtTarif.getText().trim().isEmpty()) {
            showAlert("Erreur", "❌ Le tarif est obligatoire", AlertType.ERROR);
            txtTarif.requestFocus();
            return false;
        }

        if (txtEmail.getText().trim().isEmpty()) {
            showAlert("Erreur", "❌ L'email est obligatoire", AlertType.ERROR);
            txtEmail.requestFocus();
            return false;
        }

        if (txtTelephone.getText().trim().isEmpty()) {
            showAlert("Erreur", "❌ Le téléphone est obligatoire", AlertType.ERROR);
            txtTelephone.requestFocus();
            return false;
        }

        // MODIFIÉ : Validation du ComboBox
        if (comboCabinet.getValue() == null) {
            showAlert("Erreur", "❌ Veuillez sélectionner un cabinet", AlertType.ERROR);
            comboCabinet.requestFocus();
            return false;
        }

        String nom = txtNom.getText().trim();
        if (!Pattern.matches(NOM_REGEX, nom)) {
            showAlert("Erreur", "❌ Nom invalide (2-50 lettres, tirets)", AlertType.ERROR);
            txtNom.requestFocus();
            return false;
        }

        String prenom = txtPrenom.getText().trim();
        if (!Pattern.matches(NOM_REGEX, prenom)) {
            showAlert("Erreur", "❌ Prénom invalide (2-50 lettres, tirets)", AlertType.ERROR);
            txtPrenom.requestFocus();
            return false;
        }

        String specialite = txtSpecialite.getText().trim();
        if (!Pattern.matches(SPECIALITE_REGEX, specialite)) {
            showAlert("Erreur", "❌ Spécialité invalide", AlertType.ERROR);
            txtSpecialite.requestFocus();
            return false;
        }

        String diplome = txtDiplome.getText().trim();
        if (!Pattern.matches(DIPLOME_REGEX, diplome)) {
            showAlert("Erreur", "❌ Diplôme invalide", AlertType.ERROR);
            txtDiplome.requestFocus();
            return false;
        }

        try {
            int experience = Integer.parseInt(txtExperience.getText().trim());
            if (experience < 0 || experience > 60) {
                showAlert("Erreur", "❌ Expérience doit être entre 0 et 60 ans", AlertType.ERROR);
                txtExperience.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur", "❌ Expérience doit être un nombre", AlertType.ERROR);
            txtExperience.requestFocus();
            return false;
        }

        try {
            double tarif = Double.parseDouble(txtTarif.getText().trim());
            if (tarif < 20 || tarif > 500) {
                showAlert("Erreur", "❌ Tarif doit être entre 20 et 500 DT", AlertType.ERROR);
                txtTarif.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur", "❌ Tarif doit être un nombre", AlertType.ERROR);
            txtTarif.requestFocus();
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
    private void ajouterPsychologue() {
        if (!validerChamps()) {
            return;
        }

        try {
            Psychologue p = new Psychologue(
                    txtNom.getText().trim(),
                    txtPrenom.getText().trim(),
                    txtSpecialite.getText().trim(),
                    txtDiplome.getText().trim(),
                    Integer.parseInt(txtExperience.getText().trim()),
                    Double.parseDouble(txtTarif.getText().trim()),
                    txtEmail.getText().trim().toLowerCase(),
                    txtTelephone.getText().trim(),
                    comboCabinet.getValue().getIdCabinet()  // MODIFIÉ
            );

            service.ajouter(p);
            loadData();
            clear();
            showAlert("Succès", "✅ Psychologue ajouté avec succès !", AlertType.INFORMATION);

        } catch (Exception e) {
            showAlert("Erreur", "❌ Erreur lors de l'ajout: " + e.getMessage(), AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void modifierPsychologue() {
        if (selectedPsychologue == null) {
            showAlert("Erreur", "❌ Veuillez sélectionner un psychologue", AlertType.ERROR);
            return;
        }

        if (!validerChamps()) {
            return;
        }

        try {
            selectedPsychologue.setNom(txtNom.getText().trim());
            selectedPsychologue.setPrenom(txtPrenom.getText().trim());
            selectedPsychologue.setSpecialite(txtSpecialite.getText().trim());
            selectedPsychologue.setDiplome(txtDiplome.getText().trim());
            selectedPsychologue.setExperience(Integer.parseInt(txtExperience.getText().trim()));
            selectedPsychologue.setTarif(Double.parseDouble(txtTarif.getText().trim()));
            selectedPsychologue.setEmail(txtEmail.getText().trim().toLowerCase());
            selectedPsychologue.setTelephone(txtTelephone.getText().trim());
            selectedPsychologue.setIdCabinet(comboCabinet.getValue().getIdCabinet());  // MODIFIÉ

            service.modifier(selectedPsychologue);
            loadData();
            clear();
            showAlert("Succès", "✅ Psychologue modifié avec succès !", AlertType.INFORMATION);

        } catch (Exception e) {
            showAlert("Erreur", "❌ Erreur lors de la modification: " + e.getMessage(), AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void supprimerPsychologue() {
        if (selectedPsychologue == null) {
            showAlert("Erreur", "❌ Veuillez sélectionner un psychologue", AlertType.ERROR);
            return;
        }

        Alert confirmation = new Alert(AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer le psychologue");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer " +
                selectedPsychologue.getNom() + " " +
                selectedPsychologue.getPrenom() + " ?");

        if (confirmation.showAndWait().get() == ButtonType.OK) {
            try {
                service.supprimer(selectedPsychologue);
                loadData();
                clear();
                showAlert("Succès", "✅ Psychologue supprimé avec succès !", AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Erreur", "❌ Erreur lors de la suppression: " + e.getMessage(), AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void actualiser() {
        try {
            loadData();
            txtRecherche.clear();
            comboTri.setValue("Nom (A-Z)");
            comboFiltreCabinet.setValue("Tous");
            filterList();
            showAlert("Succès", "✅ Liste actualisée", AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Erreur", "❌ Erreur d'actualisation", AlertType.ERROR);
        }
    }

    @FXML
    private void clear() {
        txtNom.clear();
        txtPrenom.clear();
        txtSpecialite.clear();
        txtDiplome.clear();
        txtExperience.clear();
        txtTarif.clear();
        txtEmail.clear();
        txtTelephone.clear();
        comboCabinet.setValue(null);  // AJOUTÉ
        selectedPsychologue = null;
        listPsychologue.getSelectionModel().clearSelection();
    }

    @FXML
    private void annuler() {
        clear();
    }
}