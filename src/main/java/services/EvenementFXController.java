package services;

import Modele.Evenement;
import Modele.Reservation;
import utiles.mydb;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
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

        // Configurer la cellule personnalisée pour la ListView
        listEvenements.setCellFactory(param -> new ListCell<>() {
            private final HBox hbox = new HBox(10);
            private final VBox vbox = new VBox(5);
            private final TextFlow textFlow = new TextFlow();
            private final Text titreText = new Text();
            private final Text detailsText = new Text();
            private final HBox actionsBox = new HBox(5);
            private final Button btnShow = new Button("👁️");
            private final Button btnEdit = new Button("✏️");
            private final Button btnDelete = new Button("🗑️");
            private final Button btnReserver = new Button("📅");

            {
                // Configuration du layout
                titreText.setFont(Font.font("System", 14));
                titreText.setStyle("-fx-font-weight: bold; -fx-fill: #1565c0;");

                detailsText.setFont(Font.font("System", 12));
                detailsText.setStyle("-fx-fill: #666666;");

                textFlow.getChildren().addAll(titreText, new Text("\n"), detailsText);
                vbox.getChildren().add(textFlow);

                // Style des boutons d'action
                btnShow.setStyle("-fx-background-color: #3A7CA5; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-min-width: 40; -fx-min-height: 30;");
                btnEdit.setStyle("-fx-background-color: #5FB49C; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-min-width: 40; -fx-min-height: 30;");
                btnDelete.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-min-width: 40; -fx-min-height: 30;");
                btnReserver.setStyle("-fx-background-color: #9B59B6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-min-width: 40; -fx-min-height: 30;");

                // Tooltips
                btnShow.setTooltip(new Tooltip("Afficher les détails"));
                btnEdit.setTooltip(new Tooltip("Modifier l'événement"));
                btnDelete.setTooltip(new Tooltip("Supprimer l'événement"));
                btnReserver.setTooltip(new Tooltip("Réserver cet événement"));

                // Ajouter les boutons
                actionsBox.getChildren().addAll(btnShow, btnEdit, btnDelete, btnReserver);
                actionsBox.setStyle("-fx-alignment: center-left;");

                // Configuration du HBox principal
                hbox.getChildren().addAll(vbox, actionsBox);
                hbox.setStyle("-fx-alignment: center-left; -fx-padding: 10; -fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-border-color: #e0e0e0; -fx-border-radius: 8;");

                // Gestionnaires d'événements pour les boutons
                btnShow.setOnAction(e -> {
                    Evenement ev = getItem();
                    if (ev != null) {
                        showPopup(ev);
                    }
                });

                btnEdit.setOnAction(e -> {
                    Evenement ev = getItem();
                    if (ev != null) {
                        fillForm(ev);
                        listEvenements.getSelectionModel().select(ev);
                    }
                });

                btnDelete.setOnAction(e -> {
                    Evenement ev = getItem();
                    if (ev != null) {
                        confirmDelete(ev);
                    }
                });

                btnReserver.setOnAction(e -> {
                    Evenement ev = getItem();
                    if (ev != null) {
                        openReservationWindow(ev);
                    }
                });

                // Effets hover
                btnShow.setOnMouseEntered(e -> btnShow.setStyle("-fx-background-color: #2B6A9A; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-min-width: 40; -fx-min-height: 30;"));
                btnShow.setOnMouseExited(e -> btnShow.setStyle("-fx-background-color: #3A7CA5; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-min-width: 40; -fx-min-height: 30;"));

                btnEdit.setOnMouseEntered(e -> btnEdit.setStyle("-fx-background-color: #4AA189; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-min-width: 40; -fx-min-height: 30;"));
                btnEdit.setOnMouseExited(e -> btnEdit.setStyle("-fx-background-color: #5FB49C; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-min-width: 40; -fx-min-height: 30;"));

                btnDelete.setOnMouseEntered(e -> btnDelete.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-min-width: 40; -fx-min-height: 30;"));
                btnDelete.setOnMouseExited(e -> btnDelete.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-min-width: 40; -fx-min-height: 30;"));

                btnReserver.setOnMouseEntered(e -> btnReserver.setStyle("-fx-background-color: #8E44AD; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-min-width: 40; -fx-min-height: 30;"));
                btnReserver.setOnMouseExited(e -> btnReserver.setStyle("-fx-background-color: #9B59B6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-min-width: 40; -fx-min-height: 30;"));
            }

            @Override
            protected void updateItem(Evenement evenement, boolean empty) {
                super.updateItem(evenement, empty);

                if (empty || evenement == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    titreText.setText(evenement.getTitre());

                    try {
                        int nbReservations = reservationService.countByEvenement(evenement.getIdEvenement());
                        detailsText.setText(
                                "📅 " + formatter.format(evenement.getDate()) +
                                        " | 📍 " + evenement.getLocalisation() +
                                        " | 👥 " + nbReservations + " réservation(s)"
                        );
                    } catch (SQLException e) {
                        detailsText.setText(
                                "📅 " + formatter.format(evenement.getDate()) +
                                        " | 📍 " + evenement.getLocalisation()
                        );
                    }

                    hbox.prefWidthProperty().bind(listEvenements.widthProperty().subtract(30));
                    setGraphic(hbox);
                    setStyle("-fx-background-color: transparent; -fx-padding: 5;");
                }
            }
        });

        setupInputValidation();
        setupCharacterCounters();
        setupDateValidation();

        afficherEvenements();
        updateCount();
    }

    // ================== VALIDATION METHODS ==================
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
        if (lblCharCountTitre != null) {
            updateCharCounter(lblCharCountTitre, txtTitre.getText().length(), 100);
        }
        if (lblCharCountDesc != null) {
            updateCharCounter(lblCharCountDesc, txtDescription.getText().length(), 500);
        }
        if (lblCharCountLocalisation != null) {
            updateCharCounter(lblCharCountLocalisation, txtLocalisation.getText().length(), 100);
        }
    }

    private void setupDateValidation() {
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();
                setDisable(empty || date.isBefore(today));
            }
        });

        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateDate();
        });
    }

    private void updateCharCounter(Label counterLabel, int currentLength, int maxLength) {
        if (counterLabel != null) {
            counterLabel.setText(currentLength + "/" + maxLength);
            if (currentLength > maxLength) {
                counterLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            } else if (currentLength == 0) {
                counterLabel.setStyle("-fx-text-fill: #95a5a6;");
            } else {
                counterLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            }
        }
    }

    private boolean validateTitre() {
        String titre = txtTitre.getText().trim();
        boolean isValid = true;

        if (titre.isEmpty()) {
            showFieldError(txtTitre, "Le titre est obligatoire");
            isValid = false;
        } else if (titre.length() < 3) {
            showFieldError(txtTitre, "Le titre doit contenir au moins 3 caractères");
            isValid = false;
        } else if (titre.length() > 100) {
            showFieldError(txtTitre, "Le titre ne doit pas dépasser 100 caractères");
            isValid = false;
        } else if (containsDangerousCharacters(titre)) {
            showFieldError(txtTitre, "Caractères spéciaux dangereux détectés");
            isValid = false;
        } else {
            clearFieldError(txtTitre);
        }
        return isValid;
    }

    private boolean validateDescription() {
        String description = txtDescription.getText().trim();
        boolean isValid = true;

        if (description.isEmpty()) {
            showFieldError(txtDescription, "La description est obligatoire");
            isValid = false;
        } else if (description.length() < 10) {
            showFieldError(txtDescription, "La description doit contenir au moins 10 caractères");
            isValid = false;
        } else if (description.length() > 500) {
            showFieldError(txtDescription, "La description ne doit pas dépasser 500 caractères");
            isValid = false;
        } else if (containsDangerousCharacters(description)) {
            showFieldError(txtDescription, "Caractères spéciaux dangereux détectés");
            isValid = false;
        } else {
            clearFieldError(txtDescription);
        }
        return isValid;
    }

    private boolean validateDate() {
        boolean isValid = true;

        if (datePicker.getValue() == null) {
            showDateError("Veuillez sélectionner une date");
            isValid = false;
        } else {
            LocalDate selectedDate = datePicker.getValue();
            LocalDate today = LocalDate.now();

            if (selectedDate.isBefore(today)) {
                showDateError("La date ne peut pas être dans le passé");
                isValid = false;
            } else if (selectedDate.isAfter(today.plusYears(1))) {
                showDateError("La date ne peut pas être plus d'un an dans le futur");
                isValid = false;
            } else {
                clearDateError();
            }
        }
        return isValid;
    }

    private boolean validateLocalisation() {
        String localisation = txtLocalisation.getText().trim();
        boolean isValid = true;

        if (localisation.isEmpty()) {
            showFieldError(txtLocalisation, "La localisation est obligatoire");
            isValid = false;
        } else if (localisation.length() < 3) {
            showFieldError(txtLocalisation, "La localisation doit contenir au moins 3 caractères");
            isValid = false;
        } else if (localisation.length() > 100) {
            showFieldError(txtLocalisation, "La localisation ne doit pas dépasser 100 caractères");
            isValid = false;
        } else if (containsDangerousCharacters(localisation)) {
            showFieldError(txtLocalisation, "Caractères spéciaux dangereux détectés");
            isValid = false;
        } else {
            clearFieldError(txtLocalisation);
        }
        return isValid;
    }

    private boolean containsDangerousCharacters(String text) {
        String[] dangerousChars = {";", "<", ">", "=", "'", "\"", "\\", "/", "*"};
        for (String dangerousChar : dangerousChars) {
            if (text.contains(dangerousChar)) {
                return true;
            }
        }
        return false;
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

    private void showFieldSuccess(TextField field) {
        field.setStyle(
                "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-color: #27ae60;" +
                        "-fx-border-width: 2;" +
                        "-fx-background-color: #f0fff0;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 12;"
        );
        Tooltip tooltip = new Tooltip("✅ Valide");
        tooltip.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        field.setTooltip(tooltip);
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

    // ================== ACTIONS ==================
    @FXML
    private void afficherEvenements() {
        try {
            List<Evenement> evenements = service.afficher();
            data.setAll(evenements);
            listEvenements.setItems(data);
            updateCount();
            System.out.println("✅ " + evenements.size() + " événement(s) chargé(s)");
        } catch (Exception e) {
            alert("Erreur", "Impossible de charger les événements: " + e.getMessage());
        }
    }

    @FXML
    private void ajouterEvenement(ActionEvent event) {
        if (!validateAllFields()) {
            alert("Erreur de validation", "Veuillez corriger les erreurs dans le formulaire");
            return;
        }

        try {
            String titre = sanitizeInput(txtTitre.getText().trim());
            String description = sanitizeInput(txtDescription.getText().trim());
            String localisation = sanitizeInput(txtLocalisation.getText().trim());

            Evenement e = new Evenement();
            e.setTitre(titre);
            e.setDescription(description);
            e.setDate(datePicker.getValue());
            e.setLocalisation(localisation);

            service.ajouter(e);
            afficherEvenements();
            clearFields();
            showSuccessMessage("✅ Événement ajouté avec succès !");
        } catch (Exception e) {
            alert("Erreur", "Impossible d'ajouter l'événement: " + e.getMessage());
        }
    }

    @FXML
    private void modifierEvenement(ActionEvent event) {
        Evenement selected = listEvenements.getSelectionModel().getSelectedItem();

        if (selected == null) {
            alert("Sélection manquante", "Veuillez choisir un événement dans la liste.");
            return;
        }

        if (!validateAllFields()) {
            alert("Erreur de validation", "Veuillez corriger les erreurs dans le formulaire");
            return;
        }

        try {
            String titre = sanitizeInput(txtTitre.getText().trim());
            String description = sanitizeInput(txtDescription.getText().trim());
            String localisation = sanitizeInput(txtLocalisation.getText().trim());

            selected.setTitre(titre);
            selected.setDescription(description);
            selected.setDate(datePicker.getValue());
            selected.setLocalisation(localisation);

            service.modifier(selected);
            afficherEvenements();
            clearFields();
            showSuccessMessage("✅ Événement modifié avec succès !");
        } catch (Exception e) {
            alert("Erreur", "Impossible de modifier l'événement: " + e.getMessage());
        }
    }

    private String sanitizeInput(String input) {
        return input.replace("'", "''")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace(";", ",")
                .replace("\\", "/");
    }

    @FXML
    private void supprimerEvenement(ActionEvent event) {
        Evenement selected = listEvenements.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alert("Sélection manquante", "Veuillez choisir un événement dans la liste.");
            return;
        }
        confirmDelete(selected);
    }

    // ================== RÉSERVATION - FORMULAIRE MODERNE AVEC VALIDATION 10/10 ==================
    private void openReservationWindow(Evenement evenement) {
        try {
            Stage stage = new Stage();
            stage.setTitle("📅 Réserver - " + evenement.getTitre());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.setWidth(700);
            stage.setHeight(800);

            BorderPane mainPane = new BorderPane();
            mainPane.setStyle("-fx-background-color: linear-gradient(135deg, #667eea 0%, #764ba2 100%);");

            // ========== HEADER ==========
            VBox headerBox = new VBox(12);
            headerBox.setPadding(new Insets(25, 35, 20, 35));
            headerBox.setStyle("-fx-background-color: linear-gradient(to right, #4A6FA5, #5FB49C); -fx-background-radius: 0 0 25 25; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0.5, 0, 3);");

            HBox topDecoration = new HBox(12);
            topDecoration.setAlignment(Pos.CENTER);
            topDecoration.getChildren().addAll(
                    new Label("🌿") {{ setStyle("-fx-font-size: 28px; -fx-text-fill: rgba(255,255,255,0.9);"); }},
                    new Label("RÉSERVER VOTRE PLACE") {{ setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0.3, 1, 1);"); }},
                    new Label("🌿") {{ setStyle("-fx-font-size: 28px; -fx-text-fill: rgba(255,255,255,0.9);"); }}
            );

            HBox eventCard = new HBox(15);
            eventCard.setAlignment(Pos.CENTER_LEFT);
            eventCard.setPadding(new Insets(15, 20, 15, 20));
            eventCard.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 15; -fx-border-color: rgba(255,255,255,0.3); -fx-border-width: 1.5;");

            Label iconEvent = new Label("🎯");
            iconEvent.setStyle("-fx-font-size: 40px; -fx-text-fill: white;");

            VBox eventInfo = new VBox(5);
            Label eventTitle = new Label(evenement.getTitre());
            eventTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

            HBox eventDetails = new HBox(20);
            eventDetails.getChildren().addAll(
                    new Label("📅 " + formatter.format(evenement.getDate())) {{ setStyle("-fx-font-size: 13px; -fx-text-fill: white; -fx-font-weight: 500;"); }},
                    new Label("📍 " + evenement.getLocalisation()) {{ setStyle("-fx-font-size: 13px; -fx-text-fill: white; -fx-font-weight: 500;"); }}
            );

            eventInfo.getChildren().addAll(eventTitle, eventDetails);
            eventCard.getChildren().addAll(iconEvent, eventInfo);
            headerBox.getChildren().addAll(topDecoration, eventCard);

            // ========== CENTRE - CARTE BLANCHE ==========
            VBox centerCard = new VBox(20);
            centerCard.setPadding(new Insets(30, 40, 30, 40));
            centerCard.setStyle("-fx-background-color: rgba(255,255,255,0.95); -fx-background-radius: 25; -fx-effect: dropshadow(gaussian, rgba(74,111,165,0.2), 20, 0.3, 0, 8); -fx-border-color: #4A6FA5; -fx-border-radius: 25; -fx-border-width: 2;");

            // ========== TITRE SECTION ==========
            Label sectionTitle = new Label("📝 VOS INFORMATIONS");
            sectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #4A6FA5;");

            // ========== GRILLE FORMULAIRE ==========
            GridPane formGrid = new GridPane();
            formGrid.setVgap(15);
            formGrid.setHgap(20);
            formGrid.setPadding(new Insets(10, 0, 10, 0));

            String labelStyle = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #4A6FA5;";

            // NOM
            Label lblNom = new Label("👤 Nom complet *");
            lblNom.setStyle(labelStyle);
            TextField txtNom = createEnhancedTextField("Votre nom", 350);

            // EMAIL
            Label lblEmail = new Label("📧 Email *");
            lblEmail.setStyle(labelStyle);
            TextField txtEmail = createEnhancedTextField("votre@email.com", 350);

            // TÉLÉPHONE
            Label lblTelephone = new Label("📱 Téléphone *");
            lblTelephone.setStyle(labelStyle);
            TextField txtTelephone = createEnhancedPhoneField();

            // NOMBRE DE PERSONNES
            Label lblPersonnes = new Label("👥 Nombre de personnes");
            lblPersonnes.setStyle(labelStyle);
            HBox personnesBox = new HBox(12);
            personnesBox.setAlignment(Pos.CENTER_LEFT);
            Spinner<Integer> spinnerPersonnes = new Spinner<>(1, 10, 1);
            spinnerPersonnes.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #d1e7f3; -fx-background-color: #f8fdff; -fx-font-size: 14px; -fx-pref-width: 80; -fx-pref-height: 40;");
            personnesBox.getChildren().add(spinnerPersonnes);

            formGrid.add(lblNom, 0, 0); formGrid.add(txtNom, 1, 0);
            formGrid.add(lblEmail, 0, 1); formGrid.add(txtEmail, 1, 1);
            formGrid.add(lblTelephone, 0, 2); formGrid.add(txtTelephone, 1, 2);
            formGrid.add(lblPersonnes, 0, 3); formGrid.add(personnesBox, 1, 3);

            // ========== VALIDATION EN TEMPS RÉEL ==========
            setupRealtimeValidation(txtNom, txtEmail, txtTelephone);

            // ========== MESSAGE ==========
            VBox messageBox = new VBox(10);
            messageBox.setPadding(new Insets(15, 0, 5, 0));
            Label lblMessage = new Label("💬 Message (optionnel)");
            lblMessage.setStyle(labelStyle);
            TextArea txtMessage = new TextArea();
            txtMessage.setPromptText("Un message pour l'organisateur...");
            txtMessage.setWrapText(true);
            txtMessage.setPrefRowCount(2);
            txtMessage.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #d1e7f3; -fx-background-color: #f8fdff; -fx-font-size: 14px; -fx-padding: 10;");
            messageBox.getChildren().addAll(lblMessage, txtMessage);

            // ========== BOUTONS ==========
            HBox buttonBox = new HBox(20);
            buttonBox.setAlignment(Pos.CENTER);
            buttonBox.setPadding(new Insets(20, 0, 10, 0));

            Button btnAnnuler = new Button("Annuler");
            btnAnnuler.setStyle("-fx-background-color: linear-gradient(to right, #E667AF, #D3549C); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 12; -fx-padding: 12 30; -fx-cursor: hand;");

            Button btnConfirmer = new Button("✓ Confirmer la réservation");
            btnConfirmer.setStyle("-fx-background-color: linear-gradient(to right, #5FB49C, #4AA189); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 12; -fx-padding: 12 30; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(95,180,156,0.4), 10, 0.4, 0, 4);");

            // Hover effets
            btnAnnuler.setOnMouseEntered(e -> btnAnnuler.setStyle("-fx-background-color: linear-gradient(to right, #D3549C, #C0397B); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 12; -fx-padding: 12 30; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(211,84,156,0.5), 12, 0.5, 0, 5);"));
            btnAnnuler.setOnMouseExited(e -> btnAnnuler.setStyle("-fx-background-color: linear-gradient(to right, #E667AF, #D3549C); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 12; -fx-padding: 12 30; -fx-cursor: hand;"));

            btnConfirmer.setOnMouseEntered(e -> btnConfirmer.setStyle("-fx-background-color: linear-gradient(to right, #4AA189, #3A7CA5); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 12; -fx-padding: 12 30; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(74,161,137,0.5), 12, 0.5, 0, 5);"));
            btnConfirmer.setOnMouseExited(e -> btnConfirmer.setStyle("-fx-background-color: linear-gradient(to right, #5FB49C, #4AA189); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 12; -fx-padding: 12 30; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(95,180,156,0.4), 10, 0.4, 0, 4);"));

            buttonBox.getChildren().addAll(btnAnnuler, btnConfirmer);

            // ========== FOOTER ==========
            HBox footerBox = new HBox();
            footerBox.setAlignment(Pos.CENTER);
            footerBox.setPadding(new Insets(15, 0, 5, 0));
            footerBox.getChildren().add(new Label("🌿 Prenez soin de vous • GrowMind 🌿") {{
                setStyle("-fx-font-size: 12px; -fx-font-style: italic; -fx-text-fill: #7f8c8d;");
            }});

            // ========== ASSEMBLAGE ==========
            centerCard.getChildren().addAll(sectionTitle, formGrid, messageBox, buttonBox, footerBox);
            mainPane.setTop(headerBox);
            mainPane.setCenter(centerCard);
            BorderPane.setMargin(centerCard, new Insets(25, 35, 30, 35));

            // ========== ACTIONS ==========
            btnAnnuler.setOnAction(e -> stage.close());

            btnConfirmer.setOnAction(e -> {
                if (validateReservation(txtNom, txtEmail, txtTelephone)) {
                    try {
                        Reservation reservation = new Reservation(
                                evenement.getIdEvenement(),
                                UserSession.getId(),
                                txtNom.getText().trim(),
                                txtEmail.getText().trim(),
                                txtTelephone.getText().trim().replaceAll("\\s+", ""),
                                spinnerPersonnes.getValue()
                        );

                        reservationService.ajouter(reservation);
                        showReservationSuccess(reservation, evenement);
                        stage.close();
                        afficherEvenements();

                    } catch (SQLException ex) {
                        alert("Erreur", "Impossible d'enregistrer la réservation.");
                    }
                }
            });

            Scene scene = new Scene(mainPane, 700, 800);
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            alert("Erreur", "Impossible d'ouvrir le formulaire");
            e.printStackTrace();
        }
    }

    // ========== CHAMP DE TEXTE AMÉLIORÉ ==========
    private TextField createEnhancedTextField(String prompt, int width) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle(
                "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-color: #d1e7f3;" +
                        "-fx-background-color: #f8fdff;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 12;" +
                        "-fx-pref-width: " + width + "px;" +
                        "-fx-pref-height: 45px;"
        );
        return tf;
    }

    // ========== CHAMP TÉLÉPHONE SPÉCIAL ==========
    private TextField createEnhancedPhoneField() {
        TextField tf = new TextField();
        tf.setPromptText("06 12 34 56 78");
        tf.setStyle(
                "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-color: #d1e7f3;" +
                        "-fx-background-color: #f8fdff;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 12;" +
                        "-fx-pref-width: 350px;" +
                        "-fx-pref-height: 45px;"
        );

        tf.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText().replaceAll("\\s+", "");
            if (newText.matches("\\d*") && newText.length() <= 10) {
                return change;
            }
            return null;
        }));

        return tf;
    }

    // ========== VALIDATION RÉSERVATION 10/10 ==========
    private boolean validateReservation(TextField nom, TextField email, TextField telephone) {
        boolean isValid = true;

        resetFieldStyle(nom);
        resetFieldStyle(email);
        resetFieldStyle(telephone);

        // VALIDATION NOM
        String nomValue = nom.getText().trim();
        if (nomValue.isEmpty()) {
            showFieldError(nom, "❌ Le nom est obligatoire");
            isValid = false;
        } else if (nomValue.length() < 2) {
            showFieldError(nom, "❌ Le nom doit contenir au moins 2 caractères");
            isValid = false;
        } else if (!nomValue.matches("^[a-zA-ZÀ-ÿ\\s\\-']+$")) {
            showFieldError(nom, "❌ Caractères non autorisés (lettres, espaces, tirets)");
            isValid = false;
        } else {
            showFieldSuccess(nom);
        }

        // VALIDATION EMAIL
        String emailValue = email.getText().trim().toLowerCase();
        String emailRegex = "^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$";

        if (emailValue.isEmpty()) {
            showFieldError(email, "❌ L'email est obligatoire");
            isValid = false;
        } else if (!emailValue.matches(emailRegex)) {
            showFieldError(email, "❌ Format d'email invalide");
            isValid = false;
        } else {
            showFieldSuccess(email);
        }

        // VALIDATION TÉLÉPHONE
        String telValue = telephone.getText().trim().replaceAll("\\s+", "");

        if (telValue.isEmpty()) {
            showFieldError(telephone, "❌ Le téléphone est obligatoire");
            isValid = false;
        } else if (!telValue.matches("^[0-9]+$")) {
            showFieldError(telephone, "❌ Uniquement des chiffres");
            isValid = false;
        } else if (telValue.length() < 8) {
            showFieldError(telephone, "❌ Minimum 8 chiffres");
            isValid = false;
        } else if (telValue.length() > 10) {
            showFieldError(telephone, "❌ Maximum 10 chiffres");
            isValid = false;
        } else {
            showFieldSuccess(telephone);
        }

        return isValid;
    }

    // ========== VALIDATION EN TEMPS RÉEL ==========
    private void setupRealtimeValidation(TextField nom, TextField email, TextField telephone) {
        nom.textProperty().addListener((obs, old, newValue) -> {
            if (!newValue.trim().isEmpty() && newValue.trim().length() >= 2) {
                showFieldSuccess(nom);
            }
        });

        email.textProperty().addListener((obs, old, newValue) -> {
            String emailRegex = "^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$";
            if (!newValue.trim().isEmpty() && newValue.trim().toLowerCase().matches(emailRegex)) {
                showFieldSuccess(email);
            }
        });

        telephone.textProperty().addListener((obs, old, newValue) -> {
            String tel = newValue.trim().replaceAll("\\s+", "");
            if (!tel.isEmpty() && tel.matches("^[0-9]{8,10}$")) {
                showFieldSuccess(telephone);
            }
        });
    }

    // ========== RÉINITIALISATION STYLE ==========
    private void resetFieldStyle(TextField field) {
        field.setStyle(
                "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-color: #d1e7f3;" +
                        "-fx-background-color: #f8fdff;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 12;" +
                        "-fx-pref-width: 350px;" +
                        "-fx-pref-height: 45px;"
        );
        field.setTooltip(null);
    }

    // ========== SUCCÈS RÉSERVATION ==========
    private void showReservationSuccess(Reservation reservation, Evenement evenement) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("✅ Réservation confirmée !");
        alert.setHeaderText("Merci " + reservation.getNom() + " !");

        String message = String.format(
                "🎯 Événement : %s\n" +
                        "📅 Date : %s\n" +
                        "📍 Lieu : %s\n" +
                        "👥 Personnes : %d\n\n" +
                        "🌿 Un email de confirmation a été envoyé à :\n%s\n\n" +
                        "⏰ Présentez-vous 15 minutes avant le début.",
                evenement.getTitre(),
                formatter.format(evenement.getDate()),
                evenement.getLocalisation(),
                reservation.getNombrePersonnes(),
                reservation.getEmail()
        );

        TextArea textArea = new TextArea(message);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(10);
        textArea.setStyle("-fx-background-color: #f0f9f6; -fx-font-size: 14px; -fx-padding: 15;");

        alert.getDialogPane().setContent(textArea);
        alert.getDialogPane().setPrefSize(500, 400);
        alert.showAndWait();
    }

    // ================== AUTRES MÉTHODES ==================
    private void showPopup(Evenement e) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("📋 Détails de l'Événement");
        alert.setHeaderText(e.getTitre());
        alert.getDialogPane().setStyle("-fx-background-color: #f8f9fa;");

        String content = "📍 ID: " + e.getIdEvenement() + "\n\n" +
                "🎯 Titre: " + e.getTitre() + "\n\n" +
                "📝 Description: " + e.getDescription() + "\n\n" +
                "📅 Date: " + formatter.format(e.getDate()) + "\n\n" +
                "🏢 Localisation: " + e.getLocalisation();

        alert.setContentText(content);
        alert.setResizable(true);
        alert.getDialogPane().setPrefSize(400, 300);
        alert.showAndWait();
    }

    private void confirmDelete(Evenement e) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("⚠️ Confirmation de suppression");
        alert.setHeaderText("Supprimer l'événement ?");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer :\n\n" +
                "• " + e.getTitre() + "\n" +
                "• Date: " + formatter.format(e.getDate()) + "\n" +
                "• Localisation: " + e.getLocalisation() + "\n\n" +
                "Cette action est irréversible !");
        alert.getDialogPane().setStyle("-fx-background-color: #fff5f5;");

        alert.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    service.supprimer(e.getIdEvenement());
                    afficherEvenements();
                    showSuccessMessage("✅ Événement supprimé avec succès !");
                } catch (Exception ex) {
                    alert("Erreur", "Impossible de supprimer l'événement: " + ex.getMessage());
                }
            }
        });
    }

    private void fillForm(Evenement e) {
        txtTitre.setText(e.getTitre());
        txtDescription.setText(e.getDescription());
        datePicker.setValue(e.getDate());
        txtLocalisation.setText(e.getLocalisation());

        if (lblCharCountTitre != null) updateCharCounter(lblCharCountTitre, e.getTitre().length(), 100);
        if (lblCharCountDesc != null) updateCharCounter(lblCharCountDesc, e.getDescription().length(), 500);
        if (lblCharCountLocalisation != null) updateCharCounter(lblCharCountLocalisation, e.getLocalisation().length(), 100);

        txtTitre.requestFocus();
        txtTitre.selectAll();
    }

    private void clearFields() {
        txtTitre.clear();
        txtDescription.clear();
        datePicker.setValue(null);
        txtLocalisation.clear();
        listEvenements.getSelectionModel().clearSelection();

        clearFieldError(txtTitre);
        clearFieldError(txtDescription);
        clearDateError();
        clearFieldError(txtLocalisation);

        if (lblCharCountTitre != null) updateCharCounter(lblCharCountTitre, 0, 100);
        if (lblCharCountDesc != null) updateCharCounter(lblCharCountDesc, 0, 500);
        if (lblCharCountLocalisation != null) updateCharCounter(lblCharCountLocalisation, 0, 100);
    }

    private void alert(String titre, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("⚠️ " + titre);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.getDialogPane().setStyle("-fx-background-color: #fff8e1;");
        a.showAndWait();
    }

    private void showSuccessMessage(String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("✅ Succès");
        a.setHeaderText(null);
        a.setContentText(message);
        a.getDialogPane().setStyle("-fx-background-color: #f0f9f6;");
        a.showAndWait();
    }

    private void updateCount() {
        if (lblCount != null) {
            lblCount.setText(data.size() + " événement(s)");
        }
    }

    @FXML
    private void handleListClick() {
        Evenement e = listEvenements.getSelectionModel().getSelectedItem();
        if (e != null) {
            fillForm(e);
        }
    }

    // ================== HOVER EFFECTS ==================
    @FXML
    private void hoverAjouter() {
        btnAjouter.setStyle("-fx-background-color: linear-gradient(to right, #4AA189, #3A7CA5); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 12; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(74,161,137,0.5), 12, 0.5, 0, 5);");
    }

    @FXML
    private void exitAjouter() {
        btnAjouter.setStyle("-fx-background-color: linear-gradient(to right, #5FB49C, #4AA189); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 12; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(95,180,156,0.4), 10, 0.4, 0, 4);");
    }

    @FXML
    private void hoverModifier() {
        btnModifier.setStyle("-fx-background-color: linear-gradient(to right, #3A5F8A, #2B4A75); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 12; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(58,95,138,0.5), 12, 0.5, 0, 5);");
    }

    @FXML
    private void exitModifier() {
        btnModifier.setStyle("-fx-background-color: linear-gradient(to right, #4A6FA5, #3A5F8A); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 12; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(74,111,165,0.4), 10, 0.4, 0, 4);");
    }

    @FXML
    private void hoverSupprimer() {
        btnSupprimer.setStyle("-fx-background-color: linear-gradient(to right, #D3549C, #C0397B); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 12; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(211,84,156,0.5), 12, 0.5, 0, 5);");
    }

    @FXML
    private void exitSupprimer() {
        btnSupprimer.setStyle("-fx-background-color: linear-gradient(to right, #E667AF, #D3549C); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 12; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(230,103,175,0.4), 10, 0.4, 0, 4);");
    }
}