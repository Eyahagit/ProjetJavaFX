package Controllers;

import Models.Psychologue;
import Models.RendezVous;
import Service.ServicePsychologue;
import Service.ServiceRendezVous;
import Service.StripePaymentService;  // ← NOUVEAU IMPORT
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.awt.Desktop;  // ← NOUVEAU IMPORT
import java.net.URI;      // ← NOUVEAU IMPORT

public class AjouterRendezVousController {

    @FXML private DatePicker datePicker;
    @FXML private TextField txtHeure;
    @FXML private TextField txtStatut;
    @FXML private TextField txtTypeCons;
    @FXML private ComboBox<Psychologue> comboPsychologue;

    // ========== CHAMPS PATIENT ==========
    @FXML private TextField txtNomPatient;
    @FXML private TextField txtPrenomPatient;
    @FXML private TextField txtTelephonePatient;

    @FXML private ListView<RendezVous> listView;
    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> comboFiltre;
    @FXML private ComboBox<String> comboTri;
    @FXML private Label lblTotal;
    @FXML private Label lblStatus;

    private ServiceRendezVous service = new ServiceRendezVous();
    private RendezVous selected;
    private ObservableList<RendezVous> masterData = FXCollections.observableArrayList();

    // ========== NOUVEAU SERVICE STRIPE ==========
    private StripePaymentService stripeService;

    // Liste des statuts valides
    private final List<String> STATUTS_VALIDES = Arrays.asList(
            "confirmé", "annulé", "reporté", "en attente", "terminé"
    );

    // Constante pour le téléphone
    private static final String TELEPHONE_REGEX = "^[0-9]{8}$";

    @FXML
    public void initialize() {
        try {
            // Restreindre l'heure au format HH:MM
            txtHeure.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*:?\\d*")) {
                    txtHeure.setText(newValue.replaceAll("[^\\d:]", ""));
                }
                if (newValue.length() == 2 && !newValue.contains(":")) {
                    txtHeure.setText(newValue + ":");
                }
            });

            // Restreindre téléphone aux chiffres
            txtTelephonePatient.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    txtTelephonePatient.setText(newValue.replaceAll("[^\\d]", ""));
                }
            });

            // Auto-complétion pour le statut
            txtStatut.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.isEmpty()) {
                    String lowerCase = newValue.toLowerCase();
                    for (String statut : STATUTS_VALIDES) {
                        if (statut.startsWith(lowerCase)) {
                            txtStatut.setText(statut);
                            txtStatut.positionCaret(statut.length());
                            break;
                        }
                    }
                }
            });

            // Initialiser les ComboBox
            comboFiltre.setItems(FXCollections.observableArrayList(
                    "Tous", "Confirmé", "Annulé", "Reporté", "En attente", "Terminé"
            ));
            comboFiltre.setValue("Tous");

            comboTri.setItems(FXCollections.observableArrayList(
                    "Date (récent)", "Date (ancien)", "Heure (croissant)", "Heure (décroissant)",
                    "Statut", "Type", "Psychologue", "Patient"
            ));
            comboTri.setValue("Date (récent)");

            // Charger les psychologues
            chargerPsychologues();

            // ========== INITIALISER STRIPE ==========
            stripeService = new StripePaymentService();

            // Charger les données
            loadData();

            // Configurer l'affichage personnalisé
            setupListViewCellFactory();

            // Configuration recherche et tri
            setupSearchAndSort();

            // Sélection dans le ListView
            listView.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldSelection, newSelection) -> {
                        if (newSelection != null) {
                            selected = newSelection;
                            remplirChamps(selected);
                            if (lblStatus != null) {
                                lblStatus.setText("✅ Rendez-vous #" + selected.getIdRdv() + " sélectionné");
                            }
                        }
                    }
            );

            // Message de bienvenue
            if (lblStatus != null) {
                lblStatus.setText("✅ Prêt - " + masterData.size() + " rendez-vous chargés");
            }

        } catch (Exception e) {
            showAlert("Erreur", "❌ Erreur d'initialisation: " + e.getMessage(), AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void chargerPsychologues() {
        try {
            ServicePsychologue servicePsychologue = new ServicePsychologue();
            List<Psychologue> psychologues = servicePsychologue.recupererAvecCabinet();
            ObservableList<Psychologue> items = FXCollections.observableArrayList(psychologues);
            comboPsychologue.setItems(items);

            comboPsychologue.setCellFactory(lv -> new ListCell<Psychologue>() {
                @Override
                protected void updateItem(Psychologue p, boolean empty) {
                    super.updateItem(p, empty);
                    if (empty || p == null) {
                        setText(null);
                    } else {
                        setText(p.getNom() + " " + p.getPrenom() + " - " + p.getSpecialite());
                    }
                }
            });

            comboPsychologue.setButtonCell(new ListCell<Psychologue>() {
                @Override
                protected void updateItem(Psychologue p, boolean empty) {
                    super.updateItem(p, empty);
                    if (empty || p == null) {
                        setText(null);
                    } else {
                        setText(p.getNom() + " " + p.getPrenom());
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadData() {
        masterData.clear();
        masterData.addAll(service.recupererAvecDetails());
        listView.setItems(masterData);
        lblTotal.setText(String.valueOf(masterData.size()));
    }

    private void setupListViewCellFactory() {
        listView.setCellFactory(lv -> new ListCell<RendezVous>() {
            @Override
            protected void updateItem(RendezVous r, boolean empty) {
                super.updateItem(r, empty);

                if (empty || r == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox container = new HBox(15);
                    container.setAlignment(Pos.CENTER_LEFT);
                    container.setStyle("-fx-padding: 12; -fx-background-color: transparent; -fx-border-color: transparent transparent #f0f0f0 transparent;");

                    Label statutBadge = new Label(r.getStatut());
                    String statutStyle = "-fx-background-radius: 15; -fx-padding: 5 12; -fx-font-weight: bold; -fx-min-width: 80; -fx-alignment: center; ";

                    switch (r.getStatut().toLowerCase()) {
                        case "confirmé":
                            statutStyle += "-fx-background-color: #27ae60; -fx-text-fill: white;";
                            break;
                        case "annulé":
                            statutStyle += "-fx-background-color: #e74c3c; -fx-text-fill: white;";
                            break;
                        case "reporté":
                            statutStyle += "-fx-background-color: #f39c12; -fx-text-fill: white;";
                            break;
                        case "terminé":
                            statutStyle += "-fx-background-color: #3498db; -fx-text-fill: white;";
                            break;
                        default:
                            statutStyle += "-fx-background-color: #7f8c8d; -fx-text-fill: white;";
                    }
                    statutBadge.setStyle(statutStyle);

                    VBox infoBox = new VBox(8);
                    infoBox.setPrefWidth(500);

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    Label dateLabel = new Label("📅 " + sdf.format(r.getDateRdv()) + " à " + r.getHeure());
                    dateLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

                    HBox lignePatient = new HBox(10);
                    lignePatient.setAlignment(Pos.CENTER_LEFT);

                    String patientInfo = "👤 " + r.getNomCompletPatient();
                    if (r.getTelephonePatient() != null && !r.getTelephonePatient().isEmpty()) {
                        patientInfo += " - 📞 " + r.getTelephonePatient();
                    }
                    Label patientLabel = new Label(patientInfo);
                    patientLabel.setStyle("-fx-text-fill: #34495e; -fx-font-weight: bold;");

                    if (r.isRappelEnvoye()) {
                        Label rappelBadge = new Label("✅ Rappel envoyé");
                        rappelBadge.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 2 8; -fx-font-size: 11px;");
                        lignePatient.getChildren().addAll(patientLabel, rappelBadge);
                    } else {
                        lignePatient.getChildren().add(patientLabel);
                    }

                    HBox ligne1 = new HBox(10);
                    ligne1.setAlignment(Pos.CENTER_LEFT);

                    String psychologueInfo;
                    if (r.getNomPsychologue() != null && r.getPrenomPsychologue() != null) {
                        psychologueInfo = "🧠 Dr. " + r.getNomPsychologue() + " " + r.getPrenomPsychologue();
                        if (r.getSpecialitePsychologue() != null) {
                            psychologueInfo += " (" + r.getSpecialitePsychologue() + ")";
                        }
                    } else {
                        psychologueInfo = "🧠 Psychologue #" + r.getIdPsychologue();
                    }

                    Label psychoLabel = new Label(psychologueInfo);
                    psychoLabel.setStyle("-fx-text-fill: #34495e;");
                    ligne1.getChildren().add(psychoLabel);

                    HBox ligne2 = new HBox(10);
                    ligne2.setAlignment(Pos.CENTER_LEFT);

                    String cabinetInfo;
                    if (r.getNomCabinet() != null) {
                        cabinetInfo = "🏢 " + r.getNomCabinet();
                        if (r.getVilleCabinet() != null) {
                            cabinetInfo += " (" + r.getVilleCabinet() + ")";
                        }
                    } else {
                        cabinetInfo = "🏢 Cabinet associé";
                    }

                    Label cabinetLabel = new Label(cabinetInfo);
                    cabinetLabel.setStyle("-fx-text-fill: #7f8c8d;");
                    ligne2.getChildren().add(cabinetLabel);

                    Label typeLabel = new Label("📋 " + r.getTypeCons());
                    typeLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");

                    infoBox.getChildren().addAll(dateLabel, lignePatient, ligne1, ligne2, typeLabel);
                    container.getChildren().addAll(statutBadge, infoBox);

                    setGraphic(container);
                }
            }
        });
    }

    private void setupSearchAndSort() {
        txtRecherche.textProperty().addListener((observable, oldValue, newValue) -> filterList());
        comboFiltre.valueProperty().addListener((observable, oldValue, newValue) -> filterList());
        comboTri.valueProperty().addListener((observable, oldValue, newValue) -> sortList());
    }

    private void filterList() {
        String searchText = txtRecherche.getText().toLowerCase();
        String statutFilter = comboFiltre.getValue();

        ObservableList<RendezVous> filtered = FXCollections.observableArrayList();

        for (RendezVous r : masterData) {
            boolean matchSearch = true;
            boolean matchStatut = true;

            if (!searchText.isEmpty()) {
                matchSearch = (r.getNomPatient() != null && r.getNomPatient().toLowerCase().contains(searchText)) ||
                        (r.getPrenomPatient() != null && r.getPrenomPatient().toLowerCase().contains(searchText)) ||
                        (r.getTelephonePatient() != null && r.getTelephonePatient().contains(searchText)) ||
                        (r.getNomPsychologue() != null && r.getNomPsychologue().toLowerCase().contains(searchText)) ||
                        (r.getPrenomPsychologue() != null && r.getPrenomPsychologue().toLowerCase().contains(searchText)) ||
                        (r.getNomCabinet() != null && r.getNomCabinet().toLowerCase().contains(searchText)) ||
                        (r.getVilleCabinet() != null && r.getVilleCabinet().toLowerCase().contains(searchText)) ||
                        r.getTypeCons().toLowerCase().contains(searchText) ||
                        r.getHeure().contains(searchText) ||
                        new SimpleDateFormat("dd/MM/yyyy").format(r.getDateRdv()).contains(searchText);
            }

            if (!statutFilter.equals("Tous")) {
                matchStatut = r.getStatut().equalsIgnoreCase(statutFilter);
            }

            if (matchSearch && matchStatut) {
                filtered.add(r);
            }
        }

        listView.setItems(filtered);
        lblTotal.setText(String.valueOf(filtered.size()));
        sortList();
    }

    private void sortList() {
        String critere = comboTri.getValue();
        ObservableList<RendezVous> items = listView.getItems();

        if (critere != null && items != null) {
            switch (critere) {
                case "Date (récent)":
                    items.sort((r1, r2) -> r2.getDateRdv().compareTo(r1.getDateRdv()));
                    break;
                case "Date (ancien)":
                    items.sort((r1, r2) -> r1.getDateRdv().compareTo(r2.getDateRdv()));
                    break;
                case "Heure (croissant)":
                    items.sort((r1, r2) -> r1.getHeure().compareTo(r2.getHeure()));
                    break;
                case "Heure (décroissant)":
                    items.sort((r1, r2) -> r2.getHeure().compareTo(r1.getHeure()));
                    break;
                case "Statut":
                    items.sort((r1, r2) -> r1.getStatut().compareTo(r2.getStatut()));
                    break;
                case "Type":
                    items.sort((r1, r2) -> r1.getTypeCons().compareTo(r2.getTypeCons()));
                    break;
                case "Psychologue":
                    items.sort((r1, r2) -> {
                        String nom1 = (r1.getNomPsychologue() != null ? r1.getNomPsychologue() : "");
                        String nom2 = (r2.getNomPsychologue() != null ? r2.getNomPsychologue() : "");
                        return nom1.compareTo(nom2);
                    });
                    break;
                case "Patient":
                    items.sort((r1, r2) -> {
                        String nom1 = r1.getNomCompletPatient();
                        String nom2 = r2.getNomCompletPatient();
                        return nom1.compareTo(nom2);
                    });
                    break;
            }
        }
    }

    private void remplirChamps(RendezVous r) {
        if (r != null) {
            if (r.getDateRdv() != null) {
                java.sql.Date sqlDate = (java.sql.Date) r.getDateRdv();
                datePicker.setValue(sqlDate.toLocalDate());
            }

            txtHeure.setText(r.getHeure() != null ? r.getHeure() : "");

            String statut = r.getStatut();
            if (statut != null && !statut.isEmpty()) {
                txtStatut.setText(statut.substring(0, 1).toUpperCase() + statut.substring(1).toLowerCase());
            } else {
                txtStatut.setText("");
            }

            txtTypeCons.setText(r.getTypeCons() != null ? r.getTypeCons() : "");

            txtNomPatient.setText(r.getNomPatient() != null ? r.getNomPatient() : "");
            txtPrenomPatient.setText(r.getPrenomPatient() != null ? r.getPrenomPatient() : "");
            txtTelephonePatient.setText(r.getTelephonePatient() != null ? r.getTelephonePatient() : "");

            for (Psychologue p : comboPsychologue.getItems()) {
                if (p.getIdPsychologue() == r.getIdPsychologue()) {
                    comboPsychologue.setValue(p);
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
        if (datePicker.getValue() == null) {
            showAlert("Erreur de saisie", "❌ Veuillez sélectionner une date", AlertType.ERROR);
            datePicker.requestFocus();
            return false;
        }

        if (txtHeure.getText().trim().isEmpty()) {
            showAlert("Erreur de saisie", "❌ Veuillez saisir l'heure", AlertType.ERROR);
            txtHeure.requestFocus();
            return false;
        }

        if (txtStatut.getText().trim().isEmpty()) {
            showAlert("Erreur de saisie", "❌ Veuillez saisir le statut", AlertType.ERROR);
            txtStatut.requestFocus();
            return false;
        }

        if (txtTypeCons.getText().trim().isEmpty()) {
            showAlert("Erreur de saisie", "❌ Veuillez saisir le type de consultation", AlertType.ERROR);
            txtTypeCons.requestFocus();
            return false;
        }

        if (comboPsychologue.getValue() == null) {
            showAlert("Erreur de saisie", "❌ Veuillez sélectionner un psychologue", AlertType.ERROR);
            comboPsychologue.requestFocus();
            return false;
        }

        if (txtNomPatient.getText().trim().isEmpty()) {
            showAlert("Erreur de saisie", "❌ Le nom du patient est obligatoire", AlertType.ERROR);
            txtNomPatient.requestFocus();
            return false;
        }

        if (txtPrenomPatient.getText().trim().isEmpty()) {
            showAlert("Erreur de saisie", "❌ Le prénom du patient est obligatoire", AlertType.ERROR);
            txtPrenomPatient.requestFocus();
            return false;
        }

        if (txtTelephonePatient.getText().trim().isEmpty()) {
            showAlert("Erreur de saisie", "❌ Le téléphone du patient est obligatoire", AlertType.ERROR);
            txtTelephonePatient.requestFocus();
            return false;
        }

        String telephone = txtTelephonePatient.getText().trim();
        if (!telephone.matches(TELEPHONE_REGEX)) {
            showAlert("Erreur de saisie", "❌ Téléphone invalide (8 chiffres)", AlertType.ERROR);
            txtTelephonePatient.requestFocus();
            return false;
        }

        String heure = txtHeure.getText().trim();
        if (!heure.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$")) {
            showAlert("Erreur de saisie",
                    "❌ Format d'heure invalide\nUtilisez le format HH:MM (ex: 14:30)",
                    AlertType.ERROR);
            txtHeure.requestFocus();
            return false;
        }

        String statut = txtStatut.getText().trim().toLowerCase();
        if (!STATUTS_VALIDES.contains(statut)) {
            showAlert("Erreur de saisie",
                    "❌ Statut invalide\nStatuts acceptés: Confirmé, Annulé, Reporté, En attente, Terminé",
                    AlertType.ERROR);
            txtStatut.requestFocus();
            return false;
        }

        LocalDate dateSelectionnee = datePicker.getValue();
        if (dateSelectionnee.isBefore(LocalDate.now())) {
            showAlert("Erreur de saisie", "❌ La date ne peut pas être dans le passé", AlertType.ERROR);
            datePicker.requestFocus();
            return false;
        }

        return true;
    }

    @FXML
    private void ajouter() {
        if (!validerChamps()) {
            return;
        }

        try {
            Date date = Date.from(
                    datePicker.getValue()
                            .atStartOfDay(ZoneId.systemDefault())
                            .toInstant()
            );

            RendezVous r = new RendezVous(
                    date,
                    txtHeure.getText().trim(),
                    txtStatut.getText().trim().toLowerCase(),
                    txtTypeCons.getText().trim(),
                    comboPsychologue.getValue().getIdPsychologue()
            );

            r.setNomPatient(txtNomPatient.getText().trim());
            r.setPrenomPatient(txtPrenomPatient.getText().trim());
            r.setTelephonePatient(txtTelephonePatient.getText().trim());
            r.setRappelEnvoye(false);

            service.ajouter(r);
            loadData();
            clear();
            showAlert("Succès", "✅ Rendez-vous ajouté avec succès !", AlertType.INFORMATION);
            if (lblStatus != null) {
                lblStatus.setText("✅ Rendez-vous ajouté - " + LocalDate.now());
            }

        } catch (Exception e) {
            showAlert("Erreur", "❌ Erreur lors de l'ajout: " + e.getMessage(), AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void modifier() {
        if (selected == null) {
            showAlert("Erreur", "❌ Veuillez sélectionner un rendez-vous", AlertType.ERROR);
            return;
        }

        if (!validerChamps()) {
            return;
        }

        try {
            Date date = Date.from(
                    datePicker.getValue()
                            .atStartOfDay(ZoneId.systemDefault())
                            .toInstant()
            );

            selected.setDateRdv(date);
            selected.setHeure(txtHeure.getText().trim());
            selected.setStatut(txtStatut.getText().trim().toLowerCase());
            selected.setTypeCons(txtTypeCons.getText().trim());
            selected.setIdPsychologue(comboPsychologue.getValue().getIdPsychologue());

            selected.setNomPatient(txtNomPatient.getText().trim());
            selected.setPrenomPatient(txtPrenomPatient.getText().trim());
            selected.setTelephonePatient(txtTelephonePatient.getText().trim());

            service.modifier(selected);
            loadData();
            clear();
            showAlert("Succès", "✅ Rendez-vous modifié avec succès !", AlertType.INFORMATION);

        } catch (Exception e) {
            showAlert("Erreur", "❌ Erreur lors de la modification: " + e.getMessage(), AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void supprimer() {
        if (selected == null) {
            showAlert("Erreur", "❌ Veuillez sélectionner un rendez-vous", AlertType.ERROR);
            return;
        }

        Alert confirmation = new Alert(AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer le rendez-vous");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer ce rendez-vous ?");

        if (confirmation.showAndWait().get() == ButtonType.OK) {
            try {
                service.supprimer(selected);
                loadData();
                clear();
                showAlert("Succès", "✅ Rendez-vous supprimé avec succès !", AlertType.INFORMATION);
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
        comboFiltre.setValue("Tous");
        comboTri.setValue("Date (récent)");
        filterList();
        showAlert("Succès", "✅ Liste actualisée", AlertType.INFORMATION);
    }

    // ========== NOUVELLE MÉTHODE POUR LE PAIEMENT STRIPE ==========
    @FXML
    private void handleStripePaiement() {
        // 1. Vérifier qu'un rendez-vous est sélectionné
        if (selected == null) {
            showAlert("Erreur", "❌ Veuillez sélectionner un rendez-vous", AlertType.ERROR);
            return;
        }

        try {
            // 2. Montant pour le test (à modifier selon tes besoins)
            double montant = 50.0;

            // 3. Afficher une alerte de progression
            Alert loadingAlert = new Alert(AlertType.INFORMATION);
            loadingAlert.setTitle("Paiement Stripe");
            loadingAlert.setHeaderText("Initialisation du paiement...");
            loadingAlert.setContentText("Connexion à Stripe en cours");
            loadingAlert.show();

            // 4. Créer la session de paiement
            String payUrl = stripeService.creerSessionPaiement(selected, montant);

            loadingAlert.close();

            // 5. Traiter le résultat
            if (payUrl != null && !payUrl.isEmpty()) {
                // Ouvrir dans le navigateur par défaut
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI(payUrl));

                    showAlert("Succès",
                            "✅ Redirection vers Stripe\n\n" +
                                    "Patient: " + selected.getNomCompletPatient() + "\n" +
                                    "Montant: " + montant + " USD\n" +
                                    "Type: " + selected.getTypeCons() + "\n\n" +
                                    "Utilise la carte de test: 4242 4242 4242 4242",
                            AlertType.INFORMATION);
                } else {
                    showAlert("URL de paiement",
                            "Copiez ce lien dans votre navigateur :\n\n" + payUrl,
                            AlertType.INFORMATION);
                }

                System.out.println("\n💳 PAIEMENT STRIPE INITIÉ");
                System.out.println("Patient: " + selected.getNomCompletPatient());
                System.out.println("Rendez-vous ID: " + selected.getIdRdv());
                System.out.println("Montant: " + montant + " USD");
                System.out.println("URL: " + payUrl);
                System.out.println("---\n");

            } else {
                showAlert("Erreur", "❌ Échec de la création du paiement Stripe\nVérifie la console pour plus de détails", AlertType.ERROR);
            }

        } catch (Exception e) {
            showAlert("Erreur", "❌ " + e.getMessage(), AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void clear() {
        datePicker.setValue(null);
        txtHeure.clear();
        txtStatut.clear();
        txtTypeCons.clear();
        comboPsychologue.setValue(null);

        txtNomPatient.clear();
        txtPrenomPatient.clear();
        txtTelephonePatient.clear();

        selected = null;
        listView.getSelectionModel().clearSelection();
    }

    @FXML
    private void annuler() {
        clear();
    }
}