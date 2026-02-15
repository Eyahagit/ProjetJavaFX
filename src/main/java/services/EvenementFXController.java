package services;

import Modele.Evenement;
import Modele.Reservation;
import utiles.mydb;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EvenementFXController {

    // ================== UI ==================
    @FXML
    private ListView<Evenement> listEvenements;
    @FXML
    private TextField txtTitre;
    @FXML
    private TextArea txtDescription;
    @FXML
    private DatePicker datePicker;
    @FXML
    private TextField txtLocalisation;
    @FXML
    private Button btnAjouter;
    @FXML
    private Button btnModifier;
    @FXML
    private Button btnSupprimer;
    @FXML
    private Label lblCount;
    @FXML
    private Label lblCharCountDesc;
    @FXML
    private Label lblCharCountTitre;
    @FXML
    private Label lblCharCountLocalisation;
    @FXML private Label lblTotalEvents;
    @FXML private Label lblTotalReservations;
    @FXML private Label lblEventsMois;
    @FXML private Label lblMoyenneAvis;
    @FXML private Button btnMesReservations;
    @FXML private TextField txtSearch;

    // ================== SERVICE ==================
    private EvenementControlleur service;
    private ReservationControlleur reservationService;
    private ObservableList<Evenement> data = FXCollections.observableArrayList();
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ================== INITIALIZE ==================
    @FXML
    public void initialize() {
        System.out.println("✅ EvenementFXController initialisé !");

        Connection cnx = mydb.getInstance().getConnection();
        service = new EvenementControlleur(cnx);
        reservationService = new ReservationControlleur(cnx);

        setupListView();
        setupInputValidation();
        setupCharacterCounters();
        setupDateValidation();
        setupRecherche();
        setupRealtimeUpdate();

        afficherEvenements();
        updateCount();
    }

    private void setupListView() {
        listEvenements.setCellFactory(param -> new ListCell<>() {
            private final HBox cardBox = new HBox(15);
            private final VBox contentBox = new VBox(8);
            private final HBox topRow = new HBox(10);
            private final HBox bottomRow = new HBox(10);
            private final HBox actionsBox = new HBox(8);
            private final Label lblTitre = new Label();
            private final Label lblStatut = new Label();
            private final Label lblDetails = new Label();
            private final Label lblReservations = new Label();
            private final Button btnShow = new Button("👁️");
            private final Button btnEdit = new Button("✏️");
            private final Button btnDelete = new Button("🗑️");
            private final Button btnReserver = new Button("📅");

            {
                cardBox.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-border-color: #e0e7ff; -fx-border-radius: 15; -fx-border-width: 1; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0.3, 0, 2); -fx-padding: 15;");
                cardBox.setAlignment(Pos.CENTER_LEFT);

                cardBox.setOnMouseEntered(e -> cardBox.setStyle("-fx-background-color: #f8faff; -fx-background-radius: 15; -fx-border-color: #4A6FA5; -fx-border-radius: 15; -fx-border-width: 2; -fx-effect: dropshadow(gaussian, rgba(74,111,165,0.2), 12, 0.5, 0, 4); -fx-padding: 15;"));
                cardBox.setOnMouseExited(e -> cardBox.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-border-color: #e0e7ff; -fx-border-radius: 15; -fx-border-width: 1; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0.3, 0, 2); -fx-padding: 15;"));

                lblTitre.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
                lblTitre.setFont(Font.font("Segoe UI", 18));

                lblStatut.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 4 12;");
                lblDetails.setStyle("-fx-font-size: 14px; -fx-text-fill: #475569;");
                lblReservations.setStyle("-fx-font-size: 13px; -fx-text-fill: #4A6FA5; -fx-font-weight: bold;");

                String btnBaseStyle = "-fx-background-radius: 10; -fx-min-width: 38; -fx-min-height: 38; -fx-cursor: hand; -fx-font-size: 16px;";
                btnShow.setStyle(btnBaseStyle + "-fx-background-color: #3A7CA5; -fx-text-fill: white;");
                btnEdit.setStyle(btnBaseStyle + "-fx-background-color: #5FB49C; -fx-text-fill: white;");
                btnDelete.setStyle(btnBaseStyle + "-fx-background-color: #e74c3c; -fx-text-fill: white;");
                btnReserver.setStyle(btnBaseStyle + "-fx-background-color: #9B59B6; -fx-text-fill: white;");

                btnShow.setTooltip(new Tooltip("Voir les détails"));
                btnEdit.setTooltip(new Tooltip("Modifier l'événement"));
                btnDelete.setTooltip(new Tooltip("Supprimer l'événement"));
                btnReserver.setTooltip(new Tooltip("Réserver"));

                btnShow.setOnAction(e -> { Evenement ev = getItem(); if (ev != null) showPopup(ev); });
                btnEdit.setOnAction(e -> { Evenement ev = getItem(); if (ev != null) { fillForm(ev); listEvenements.getSelectionModel().select(ev); } });
                btnDelete.setOnAction(e -> { Evenement ev = getItem(); if (ev != null) confirmDelete(ev); });
                btnReserver.setOnAction(e -> { Evenement ev = getItem(); if (ev != null) openReservationWindow(ev); });

                btnShow.setOnMouseEntered(e -> btnShow.setStyle(btnBaseStyle + "-fx-background-color: #2B6A9A; -fx-text-fill: white;"));
                btnEdit.setOnMouseEntered(e -> btnEdit.setStyle(btnBaseStyle + "-fx-background-color: #4AA189; -fx-text-fill: white;"));
                btnDelete.setOnMouseEntered(e -> btnDelete.setStyle(btnBaseStyle + "-fx-background-color: #c0392b; -fx-text-fill: white;"));
                btnReserver.setOnMouseEntered(e -> btnReserver.setStyle(btnBaseStyle + "-fx-background-color: #8E44AD; -fx-text-fill: white;"));

                actionsBox.getChildren().addAll(btnShow, btnEdit, btnDelete, btnReserver);
                actionsBox.setAlignment(Pos.CENTER_RIGHT);
                HBox.setHgrow(actionsBox, Priority.ALWAYS);
            }

            @Override
            protected void updateItem(Evenement evenement, boolean empty) {
                super.updateItem(evenement, empty);
                if (empty || evenement == null) {
                    setGraphic(null);
                } else {
                    DateAPI.StatutEvenement statut = DateAPI.getStatut(evenement.getDate());
                    String couleur = DateAPI.getCouleurPourStatut(statut);
                    String emoji = DateAPI.getEmojiPourStatut(statut);

                    lblTitre.setText(evenement.getTitre());
                    lblTitre.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + couleur + ";");

                    String statutText = "";
                    String statutColor = "";
                    switch(statut) {
                        case PASSE: statutText = "⌛ Passé"; statutColor = "#e74c3c"; break;
                        case AUJOURDHUI: statutText = "🔴 Aujourd'hui"; statutColor = "#27ae60"; break;
                        case FUTUR: statutText = "📅 À venir"; statutColor = "#3498db"; break;
                    }
                    lblStatut.setText(statutText);
                    lblStatut.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-background-color: " + statutColor + "20; -fx-text-fill: " + statutColor + "; -fx-background-radius: 20; -fx-padding: 4 12;");

                    lblDetails.setText("📍 " + evenement.getLocalisation());

                    try {
                        int nbReservations = reservationService.countByEvenement(evenement.getIdEvenement());
                        lblReservations.setText("👥 " + nbReservations + " réservation" + (nbReservations > 1 ? "s" : ""));
                    } catch (SQLException e) {
                        lblReservations.setText("👥 0 réservation");
                    }

                    topRow.getChildren().clear();
                    topRow.getChildren().addAll(lblTitre, lblStatut);
                    topRow.setAlignment(Pos.CENTER_LEFT);
                    topRow.setSpacing(15);

                    bottomRow.getChildren().clear();
                    bottomRow.getChildren().addAll(lblDetails, lblReservations);
                    bottomRow.setAlignment(Pos.CENTER_LEFT);
                    bottomRow.setSpacing(20);

                    contentBox.getChildren().clear();
                    contentBox.getChildren().addAll(topRow, bottomRow);

                    cardBox.getChildren().clear();
                    cardBox.getChildren().addAll(contentBox, actionsBox);

                    setGraphic(cardBox);
                }
            }
        });
    }

    private void setupRealtimeUpdate() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(60), event -> {
            listEvenements.refresh();
            updateStats();
            System.out.println("🔄 Mise à jour temps réel: " + LocalDateTime.now());
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void setupInputValidation() {
        txtTitre.textProperty().addListener((observable, oldValue, newValue) -> {
            validateTitre();
            updateCharCounter(lblCharCountTitre, newValue.length(), 100);
        });
        txtDescription.textProperty().addListener((observable, oldValue, newValue) -> {
            validateDescription();
            updateCharCounter(lblCharCountDesc, newValue.length(), 500);
        });
        txtLocalisation.textProperty().addListener((observable, oldValue, newValue) -> {
            validateLocalisation();
            updateCharCounter(lblCharCountLocalisation, newValue.length(), 100);
        });
    }

    private void setupCharacterCounters() {
        if (lblCharCountTitre != null) updateCharCounter(lblCharCountTitre, txtTitre.getText().length(), 100);
        if (lblCharCountDesc != null) updateCharCounter(lblCharCountDesc, txtDescription.getText().length(), 500);
        if (lblCharCountLocalisation != null) updateCharCounter(lblCharCountLocalisation, txtLocalisation.getText().length(), 100);
    }

    private void setupDateValidation() {
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> validateDate());
    }

    private void setupRecherche() {
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            String recherche = newValue.toLowerCase().trim();
            if (recherche.isEmpty()) {
                listEvenements.setItems(data);
            } else {
                ObservableList<Evenement> filtered = FXCollections.observableArrayList();
                for (Evenement e : data) {
                    if (e.getTitre().toLowerCase().contains(recherche) || e.getLocalisation().toLowerCase().contains(recherche)) {
                        filtered.add(e);
                    }
                }
                listEvenements.setItems(filtered);
            }
            updateCount();
        });
    }

    private void updateCharCounter(Label counterLabel, int currentLength, int maxLength) {
        if (counterLabel != null) {
            counterLabel.setText(currentLength + "/" + maxLength);
            if (currentLength > maxLength) counterLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            else if (currentLength == 0) counterLabel.setStyle("-fx-text-fill: #95a5a6;");
            else counterLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        }
    }

    private boolean validateTitre() {
        String titre = txtTitre.getText().trim();
        if (titre.isEmpty()) { showFieldError(txtTitre, "Le titre est obligatoire"); return false; }
        if (titre.length() < 3) { showFieldError(txtTitre, "Min 3 caractères"); return false; }
        if (titre.length() > 100) { showFieldError(txtTitre, "Max 100 caractères"); return false; }
        clearFieldError(txtTitre); return true;
    }

    private boolean validateDescription() {
        String desc = txtDescription.getText().trim();
        if (desc.isEmpty()) { showFieldError(txtDescription, "Description obligatoire"); return false; }
        if (desc.length() < 10) { showFieldError(txtDescription, "Min 10 caractères"); return false; }
        if (desc.length() > 500) { showFieldError(txtDescription, "Max 500 caractères"); return false; }
        clearFieldError(txtDescription); return true;
    }

    private boolean validateDate() {
        if (datePicker.getValue() == null) { showDateError("Date requise"); return false; }
        LocalDate selected = datePicker.getValue();
        LocalDate today = LocalDate.now();
        if (selected.isBefore(today)) { showDateError("Date passée"); return false; }
        if (selected.isAfter(today.plusYears(1))) { showDateError("Max 1 an"); return false; }
        clearDateError(); return true;
    }

    private boolean validateLocalisation() {
        String loc = txtLocalisation.getText().trim();
        if (loc.isEmpty()) { showFieldError(txtLocalisation, "Localisation requise"); return false; }
        if (loc.length() < 3) { showFieldError(txtLocalisation, "Min 3 caractères"); return false; }
        if (loc.length() > 100) { showFieldError(txtLocalisation, "Max 100 caractères"); return false; }
        clearFieldError(txtLocalisation); return true;
    }

    private boolean validateAllFields() {
        return validateTitre() && validateDescription() && validateDate() && validateLocalisation();
    }

    private void showFieldError(TextField field, String message) {
        field.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #e74c3c; -fx-border-width: 2; -fx-background-color: #ffe6e6; -fx-font-size: 14px; -fx-padding: 12;");
        showTooltip(field, message);
    }

    private void showFieldError(TextArea field, String message) {
        field.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #e74c3c; -fx-border-width: 2; -fx-background-color: #ffe6e6; -fx-font-size: 14px; -fx-padding: 12;");
        showTooltip(field, message);
    }

    private void showDateError(String message) {
        datePicker.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #e74c3c; -fx-border-width: 2; -fx-background-color: #ffe6e6; -fx-font-size: 14px;");
        showTooltip(datePicker, message);
    }

    private void showTooltip(Control control, String message) {
        Tooltip tooltip = new Tooltip(message);
        tooltip.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");
        control.setTooltip(tooltip);
    }

    private void clearFieldError(TextField field) {
        field.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #d1e7f3; -fx-background-color: #f8fdff; -fx-font-size: 14px; -fx-padding: 12;");
        field.setTooltip(null);
    }

    private void clearFieldError(TextArea field) {
        field.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #d1e7f3; -fx-background-color: #f8fdff; -fx-font-size: 14px; -fx-padding: 12;");
        field.setTooltip(null);
    }

    private void clearDateError() {
        datePicker.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #d1e7f3; -fx-background-color: #f8fdff; -fx-font-size: 14px;");
        datePicker.setTooltip(null);
    }

    @FXML
    private void afficherEvenements() {
        try {
            data.setAll(service.afficher());
            listEvenements.setItems(data);
            updateCount();
            updateStats();
        } catch (Exception e) {
            alert("Erreur", "Impossible de charger les événements");
        }
    }

    @FXML
    private void ajouterEvenement(ActionEvent event) {
        if (!validateAllFields()) { alert("Validation", "Corrigez les erreurs"); return; }
        try {
            Evenement e = new Evenement();
            e.setTitre(txtTitre.getText().trim());
            e.setDescription(txtDescription.getText().trim());
            e.setDate(datePicker.getValue());
            e.setLocalisation(txtLocalisation.getText().trim());
            service.ajouter(e);
            afficherEvenements();
            clearFields();
            showSuccessMessage("✅ Événement ajouté");
        } catch (Exception e) { alert("Erreur", e.getMessage()); }
    }

    @FXML
    private void modifierEvenement(ActionEvent event) {
        Evenement selected = listEvenements.getSelectionModel().getSelectedItem();
        if (selected == null) { alert("Sélection", "Choisissez un événement"); return; }
        if (!validateAllFields()) { alert("Validation", "Corrigez les erreurs"); return; }
        try {
            selected.setTitre(txtTitre.getText().trim());
            selected.setDescription(txtDescription.getText().trim());
            selected.setDate(datePicker.getValue());
            selected.setLocalisation(txtLocalisation.getText().trim());
            service.modifier(selected);
            afficherEvenements();
            clearFields();
            showSuccessMessage("✅ Événement modifié");
        } catch (Exception e) { alert("Erreur", e.getMessage()); }
    }

    @FXML
    private void supprimerEvenement(ActionEvent event) {
        Evenement selected = listEvenements.getSelectionModel().getSelectedItem();
        if (selected == null) { alert("Sélection", "Choisissez un événement"); return; }
        confirmDelete(selected);
    }

    private void openReservationWindow(Evenement evenement) {
        if (DateAPI.getStatut(evenement.getDate()) == DateAPI.StatutEvenement.PASSE) {
            alert("❌ Événement passé", "Impossible de réserver");
            return;
        }
        try {
            Stage stage = new Stage();
            stage.setTitle("📅 Réserver - " + evenement.getTitre());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setWidth(700); stage.setHeight(800);

            BorderPane mainPane = new BorderPane();
            mainPane.setStyle("-fx-background-color: linear-gradient(135deg, #667eea 0%, #764ba2 100%);");

            VBox headerBox = new VBox(12);
            headerBox.setPadding(new Insets(25, 35, 20, 35));
            headerBox.setStyle("-fx-background-color: linear-gradient(to right, #4A6FA5, #5FB49C); -fx-background-radius: 0 0 25 25;");

            HBox topDecoration = new HBox(12);
            topDecoration.setAlignment(Pos.CENTER);
            topDecoration.getChildren().addAll(
                    new Label("🌿") {{ setStyle("-fx-font-size: 28px; -fx-text-fill: rgba(255,255,255,0.9);"); }},
                    new Label("RÉSERVER VOTRE PLACE") {{ setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;"); }},
                    new Label("🌿") {{ setStyle("-fx-font-size: 28px; -fx-text-fill: rgba(255,255,255,0.9);"); }}
            );

            VBox centerCard = new VBox(20);
            centerCard.setPadding(new Insets(30, 40, 30, 40));
            centerCard.setStyle("-fx-background-color: white; -fx-background-radius: 25; -fx-border-color: #4A6FA5; -fx-border-width: 2;");

            Label sectionTitle = new Label("📝 VOS INFORMATIONS");
            sectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #4A6FA5;");

            GridPane formGrid = new GridPane();
            formGrid.setVgap(15); formGrid.setHgap(20);

            String labelStyle = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #4A6FA5;";

            Label lblNom = new Label("👤 Nom complet *");
            lblNom.setStyle(labelStyle);
            TextField txtNom = new TextField();
            txtNom.setPromptText("Votre nom");
            txtNom.setStyle("-fx-background-radius: 10; -fx-padding: 12; -fx-pref-width: 350px;");

            Label lblEmail = new Label("📧 Email *");
            lblEmail.setStyle(labelStyle);
            TextField txtEmail = new TextField();
            txtEmail.setPromptText("votre@email.com");
            txtEmail.setStyle("-fx-background-radius: 10; -fx-padding: 12; -fx-pref-width: 350px;");

            Label lblTelephone = new Label("📱 Téléphone *");
            lblTelephone.setStyle(labelStyle);
            TextField txtTelephone = new TextField();
            txtTelephone.setPromptText("06 12 34 56 78");
            txtTelephone.setStyle("-fx-background-radius: 10; -fx-padding: 12; -fx-pref-width: 350px;");

            Label lblPersonnes = new Label("👥 Nombre de personnes");
            lblPersonnes.setStyle(labelStyle);
            Spinner<Integer> spinnerPersonnes = new Spinner<>(1, 10, 1);
            spinnerPersonnes.setStyle("-fx-background-radius: 10; -fx-pref-width: 80;");

            formGrid.add(lblNom, 0, 0); formGrid.add(txtNom, 1, 0);
            formGrid.add(lblEmail, 0, 1); formGrid.add(txtEmail, 1, 1);
            formGrid.add(lblTelephone, 0, 2); formGrid.add(txtTelephone, 1, 2);
            formGrid.add(lblPersonnes, 0, 3); formGrid.add(spinnerPersonnes, 1, 3);

            HBox buttonBox = new HBox(20);
            buttonBox.setAlignment(Pos.CENTER);
            Button btnAnnuler = new Button("Annuler");
            btnAnnuler.setStyle("-fx-background-color: #E667AF; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 30; -fx-background-radius: 12;");
            Button btnConfirmer = new Button("✓ Confirmer");
            btnConfirmer.setStyle("-fx-background-color: #5FB49C; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 30; -fx-background-radius: 12;");

            btnAnnuler.setOnAction(e -> stage.close());
            btnConfirmer.setOnAction(e -> {
                if (txtNom.getText().trim().isEmpty() || txtEmail.getText().trim().isEmpty() || txtTelephone.getText().trim().isEmpty()) {
                    alert("Erreur", "Tous les champs sont obligatoires");
                    return;
                }
                try {
                    Reservation r = new Reservation(evenement.getIdEvenement(), UserSession.getId(),
                            txtNom.getText().trim(), txtEmail.getText().trim(),
                            txtTelephone.getText().trim(), spinnerPersonnes.getValue());
                    reservationService.ajouter(r);
                    showSuccessMessage("✅ Réservation confirmée !");
                    stage.close();
                    afficherEvenements();
                } catch (SQLException ex) { alert("Erreur", "Impossible d'enregistrer"); }
            });

            buttonBox.getChildren().addAll(btnAnnuler, btnConfirmer);

            centerCard.getChildren().addAll(sectionTitle, formGrid, buttonBox);
            mainPane.setTop(headerBox);
            mainPane.setCenter(centerCard);
            BorderPane.setMargin(centerCard, new Insets(25));

            stage.setScene(new Scene(mainPane, 700, 800));
            stage.show();
        } catch (Exception e) { alert("Erreur", "Impossible d'ouvrir"); }
    }

    @FXML
    private void handleMesReservations() {
        try {
            Stage stage = new Stage();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/MesReservations.fxml"))));
            stage.setTitle("📋 Mes Réservations");
            stage.show();
        } catch (Exception e) { alert("Erreur", "Impossible d'ouvrir"); }
    }

    private void updateStats() {
        try {
            lblTotalEvents.setText(String.valueOf(data.size()));
            int totalRes = 0;
            for (Evenement e : data) totalRes += reservationService.countByEvenement(e.getIdEvenement());
            lblTotalReservations.setText(String.valueOf(totalRes));
            LocalDate now = LocalDate.now();
            int mois = 0;
            for (Evenement e : data) if (e.getDate().getMonth() == now.getMonth() && e.getDate().getYear() == now.getYear()) mois++;
            lblEventsMois.setText(String.valueOf(mois));
            lblMoyenneAvis.setText("0.0");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void showPopup(Evenement e) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("📋 Détails");
        alert.setHeaderText(e.getTitre());
        alert.setContentText("📍 " + e.getLocalisation() + "\n📅 " + formatter.format(e.getDate()) + "\n\n📝 " + e.getDescription());
        alert.showAndWait();
    }

    private void confirmDelete(Evenement e) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("⚠️ Confirmation");
        alert.setHeaderText("Supprimer ?");
        alert.setContentText(e.getTitre());
        alert.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try { service.supprimer(e.getIdEvenement()); afficherEvenements(); showSuccessMessage("✅ Supprimé"); }
                catch (Exception ex) { alert("Erreur", "Impossible de supprimer"); }
            }
        });
    }

    private void fillForm(Evenement e) {
        txtTitre.setText(e.getTitre());
        txtDescription.setText(e.getDescription());
        datePicker.setValue(e.getDate());
        txtLocalisation.setText(e.getLocalisation());
    }

    private void clearFields() {
        txtTitre.clear(); txtDescription.clear(); datePicker.setValue(null); txtLocalisation.clear();
        listEvenements.getSelectionModel().clearSelection();
    }

    private void alert(String titre, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("⚠️ " + titre); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    private void showSuccessMessage(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("✅ Succès"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    private void updateCount() {
        if (lblCount != null) lblCount.setText(data.size() + " événement(s)");
    }

    @FXML private void handleListClick() {
        Evenement e = listEvenements.getSelectionModel().getSelectedItem();
        if (e != null) fillForm(e);
    }

    @FXML private void hoverAjouter() { btnAjouter.setStyle("-fx-background-color: linear-gradient(to right, #4AA189, #3A7CA5); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 12; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(74,161,137,0.5), 12, 0.5, 0, 5);"); }
    @FXML private void exitAjouter() { btnAjouter.setStyle("-fx-background-color: linear-gradient(to right, #5FB49C, #4AA189); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 12; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(95,180,156,0.4), 10, 0.4, 0, 4);"); }
    @FXML private void hoverModifier() { btnModifier.setStyle("-fx-background-color: linear-gradient(to right, #3A5F8A, #2B4A75); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 12; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(58,95,138,0.5), 12, 0.5, 0, 5);"); }
    @FXML private void exitModifier() { btnModifier.setStyle("-fx-background-color: linear-gradient(to right, #4A6FA5, #3A5F8A); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 12; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(74,111,165,0.4), 10, 0.4, 0, 4);"); }
    @FXML private void hoverSupprimer() { btnSupprimer.setStyle("-fx-background-color: linear-gradient(to right, #D3549C, #C0397B); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 12; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(211,84,156,0.5), 12, 0.5, 0, 5);"); }
    @FXML private void exitSupprimer() { btnSupprimer.setStyle("-fx-background-color: linear-gradient(to right, #E667AF, #D3549C); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 12; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(230,103,175,0.4), 10, 0.4, 0, 4);"); }
}