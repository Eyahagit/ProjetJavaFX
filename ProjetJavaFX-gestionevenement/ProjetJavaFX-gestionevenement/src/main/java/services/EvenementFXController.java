package services;

import Modele.Evenement;
import utiles.mydb;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Node;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class EvenementFXController {

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
    private Label lblCount;
    @FXML
    private Button btnAjouter;
    @FXML
    private Button btnModifier;
    @FXML
    private Button btnSupprimer;
    @FXML
    private Button btnRefresh;
    @FXML
    private TextField txtSearch;
    @FXML
    private Label lblTotalEvents;
    @FXML
    private Label lblTotalReservations;
    @FXML
    private Label lblEventsMois;
    @FXML
    private Label lblMoyenneAvis;

    private EvenementControlleur service;
    private EvenementAI evenementAI;
    private ObservableList<Evenement> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        try {
            Connection conn = mydb.getInstance().getConnection();
            if (conn != null && !conn.isClosed()) {
                service = new EvenementControlleur(conn);
                evenementAI = new EvenementAI(conn);
                afficherEvenements();
                setupModernEventCards();
                setupSearchFunctionality();
                updateStats();
                System.out.println("✅ EvenementFXController initialized successfully!");
            } else {
                System.err.println("❌ Failed to initialize database connection");
            }
        } catch (Exception e) {
            System.err.println("❌ Error initializing controller: " + e.getMessage());
        }
    }

    private void setupSearchFunctionality() {
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filterEvents(newValue);
        });
    }

    private void filterEvents(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            listEvenements.setItems(data);
            lblCount.setText("Total: " + data.size() + " événement(s)");
            return;
        }

        ObservableList<Evenement> filteredList = FXCollections.observableArrayList();
        String lowerCaseFilter = searchText.toLowerCase().trim();

        for (Evenement event : data) {
            if (event.getTitre().toLowerCase().contains(lowerCaseFilter) ||
                    event.getDescription().toLowerCase().contains(lowerCaseFilter) ||
                    event.getLocalisation().toLowerCase().contains(lowerCaseFilter)) {
                filteredList.add(event);
            }
        }

        listEvenements.setItems(filteredList);
        lblCount.setText("Résultats: " + filteredList.size() + " événement(s)");
    }

    private void setupModernEventCards() {
        listEvenements.setCellFactory(param -> new ListCell<Evenement>() {
            private final HBox mainContainer = new HBox(20);
            private final VBox contentSection = new VBox(12);
            private final HBox topRow = new HBox(10);
            private final HBox metaRow = new HBox(15);
            private final HBox actionsRow = new HBox(10);

            // Éléments visuels
            private final Label titleLabel = new Label();
            private final Label descriptionLabel = new Label();
            private final Label dateLabel = new Label();
            private final Label locationLabel = new Label();
            private final Label priceLabel = new Label();
            private final Label statusBadge = new Label();
            private final Label attendeesLabel = new Label();

            // Boutons
            private final Button btnReserve = new Button("Réserver");
            private final Button btnDetails = new Button("Détails");

            // Bloc date
            private final VBox dateBox = new VBox(2);
            private final Label dayLabel = new Label();
            private final Label monthLabel = new Label();

            {
                // Configuration du conteneur principal
                mainContainer.setAlignment(Pos.CENTER_LEFT);
                mainContainer.setPadding(new Insets(15, 20, 15, 15));
                mainContainer.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-background-radius: 20;" +
                                "-fx-border-radius: 20;" +
                                "-fx-border-color: transparent;" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);" +
                                "-fx-cursor: hand;"
                );

                // Effet hover
                mainContainer.setOnMouseEntered(e ->
                        mainContainer.setStyle(
                                "-fx-background-color: white;" +
                                        "-fx-background-radius: 20;" +
                                        "-fx-border-radius: 20;" +
                                        "-fx-border-color: #667eea;" +
                                        "-fx-border-width: 2;" +
                                        "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.3), 20, 0, 0, 8);" +
                                        "-fx-cursor: hand;"
                        )
                );

                mainContainer.setOnMouseExited(e ->
                        mainContainer.setStyle(
                                "-fx-background-color: white;" +
                                        "-fx-background-radius: 20;" +
                                        "-fx-border-radius: 20;" +
                                        "-fx-border-color: transparent;" +
                                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);" +
                                        "-fx-cursor: hand;"
                        )
                );

                // Design du bloc date
                dateBox.setAlignment(Pos.CENTER);
                dateBox.setPrefWidth(70);
                dateBox.setPrefHeight(70);
                dateBox.setStyle(
                        "-fx-background-color: linear-gradient(135deg, #667eea, #764ba2);" +
                                "-fx-background-radius: 15;" +
                                "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.3), 10, 0, 0, 3);"
                );

                dayLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
                monthLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: rgba(255,255,255,0.9);");
                dateBox.getChildren().addAll(dayLabel, monthLabel);

                // Style des titres
                titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                titleLabel.setWrapText(true);
                titleLabel.setMaxWidth(300);

                descriptionLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d;");
                descriptionLabel.setWrapText(true);
                descriptionLabel.setMaxWidth(300);
                descriptionLabel.setMaxHeight(40);

                // Style des métadonnées
                dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #667eea; -fx-font-weight: bold;");
                locationLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #5FB49C;");

                // Prix
                priceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #f39c12;");

                // Participants
                attendeesLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #95a5a6;");

                // Style des boutons
                btnReserve.setStyle(
                        "-fx-background-color: #5FB49C;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-weight: bold;" +
                                "-fx-font-size: 12px;" +
                                "-fx-background-radius: 25;" +
                                "-fx-padding: 8 20;" +
                                "-fx-cursor: hand;"
                );

                btnDetails.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-text-fill: #667eea;" +
                                "-fx-font-weight: bold;" +
                                "-fx-font-size: 12px;" +
                                "-fx-border-color: #667eea;" +
                                "-fx-border-radius: 25;" +
                                "-fx-padding: 7 19;" +
                                "-fx-cursor: hand;"
                );

                // Organisation des sections
                topRow.getChildren().addAll(titleLabel, statusBadge);
                topRow.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(titleLabel, Priority.ALWAYS);

                metaRow.getChildren().addAll(dateLabel, locationLabel);
                metaRow.setAlignment(Pos.CENTER_LEFT);

                actionsRow.getChildren().addAll(btnReserve, btnDetails);
                actionsRow.setAlignment(Pos.CENTER_LEFT);

                contentSection.getChildren().addAll(topRow, descriptionLabel, metaRow, priceLabel, attendeesLabel, actionsRow);

                mainContainer.getChildren().addAll(dateBox, contentSection);
                HBox.setHgrow(contentSection, Priority.ALWAYS);

                // Action handlers
                btnReserve.setOnAction(e -> {
                    Evenement event = getItem();
                    if (event != null && !isEventPassed(event)) {
                        openReservationWindow(event);
                    } else if (isEventPassed(event)) {
                        showAlert("Information", "Cet événement est déjà passé et n'accepte plus de réservations.");
                    }
                });

                btnDetails.setOnAction(e -> {
                    Evenement event = getItem();
                    if (event != null) {
                        showEventDetails(event);
                    }
                });

                mainContainer.setOnMouseClicked(e -> {
                    Evenement event = getItem();
                    if (event != null) {
                        fillForm(event);
                    }
                });

                setPrefHeight(160);
            }

            @Override
            protected void updateItem(Evenement evenement, boolean empty) {
                super.updateItem(evenement, empty);

                if (empty || evenement == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    try {
                        // Formatage de la date
                        dayLabel.setText(String.valueOf(evenement.getDate().getDayOfMonth()));
                        monthLabel.setText(evenement.getDate().format(DateTimeFormatter.ofPattern("MMM")));

                        // Titre et description
                        titleLabel.setText(evenement.getTitre());
                        String desc = evenement.getDescription();
                        if (desc != null && desc.length() > 100) {
                            desc = desc.substring(0, 97) + "...";
                        }
                        descriptionLabel.setText(desc != null ? desc : "");

                        // Date et lieu
                        dateLabel.setText("📅 " + evenement.getDate().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
                        locationLabel.setText("📍 " + (evenement.getLocalisation() != null ? evenement.getLocalisation() : ""));

                        // Prix dynamique
                        double price = evenementAI != null ?
                                evenementAI.calculateDynamicPrice(evenement.getIdEvenement()) : 50.0;
                        priceLabel.setText(String.format("💰 %.0f TND", price));

                        // Simulation du nombre de participants
                        int attendees = 15 + (int)(Math.random() * 30);
                        attendeesLabel.setText("👥 " + attendees + " participants");

                        // Gestion du statut avec couleurs
                        if (isEventPassed(evenement)) {
                            // Événement passé - ROUGE
                            statusBadge.setText("📅 Passé");
                            statusBadge.setStyle(
                                    "-fx-background-radius: 20;" +
                                            "-fx-padding: 5 15;" +
                                            "-fx-font-size: 11px;" +
                                            "-fx-font-weight: bold;" +
                                            "-fx-background-color: #ff6b6b;" +
                                            "-fx-text-fill: white;"
                            );
                            btnReserve.setDisable(true);
                            btnReserve.setStyle(
                                    "-fx-background-color: #e0e0e0;" +
                                            "-fx-text-fill: #95a5a6;" +
                                            "-fx-font-weight: bold;" +
                                            "-fx-font-size: 12px;" +
                                            "-fx-background-radius: 25;" +
                                            "-fx-padding: 8 20;" +
                                            "-fx-cursor: default;"
                            );
                        } else if (isEventToday(evenement)) {
                            // Événement aujourd'hui - VERT
                            statusBadge.setText("🔥 Aujourd'hui");
                            statusBadge.setStyle(
                                    "-fx-background-radius: 20;" +
                                            "-fx-padding: 5 15;" +
                                            "-fx-font-size: 11px;" +
                                            "-fx-font-weight: bold;" +
                                            "-fx-background-color: #5FB49C;" +
                                            "-fx-text-fill: white;"
                            );
                            btnReserve.setDisable(false);
                            btnReserve.setStyle(
                                    "-fx-background-color: #5FB49C;" +
                                            "-fx-text-fill: white;" +
                                            "-fx-font-weight: bold;" +
                                            "-fx-font-size: 12px;" +
                                            "-fx-background-radius: 25;" +
                                            "-fx-padding: 8 20;" +
                                            "-fx-cursor: hand;"
                            );
                        } else {
                            // Événement futur - BLEU
                            statusBadge.setText("📅 À venir");
                            statusBadge.setStyle(
                                    "-fx-background-radius: 20;" +
                                            "-fx-padding: 5 15;" +
                                            "-fx-font-size: 11px;" +
                                            "-fx-font-weight: bold;" +
                                            "-fx-background-color: #667eea;" +
                                            "-fx-text-fill: white;"
                            );
                            btnReserve.setDisable(false);
                            btnReserve.setStyle(
                                    "-fx-background-color: #5FB49C;" +
                                            "-fx-text-fill: white;" +
                                            "-fx-font-weight: bold;" +
                                            "-fx-font-size: 12px;" +
                                            "-fx-background-radius: 25;" +
                                            "-fx-padding: 8 20;" +
                                            "-fx-cursor: hand;"
                            );
                        }

                        setGraphic(mainContainer);
                        setText(null);

                    } catch (Exception e) {
                        System.err.println("❌ Error updating event card: " + e.getMessage());
                    }
                }
            }
        });
    }

    private void showEventDetails(Evenement evenement) {
        try {
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Détails de l'événement");
            dialog.setHeaderText(null);

            VBox content = new VBox(20);
            content.setStyle("-fx-background-color: white; -fx-padding: 25; -fx-background-radius: 20;");
            content.setPrefWidth(500);

            // En-tête avec statut
            HBox header = new HBox(15);
            header.setAlignment(Pos.CENTER_LEFT);

            VBox dateBigBox = new VBox(5);
            dateBigBox.setAlignment(Pos.CENTER);
            dateBigBox.setPrefWidth(80);
            dateBigBox.setPrefHeight(80);
            dateBigBox.setStyle(
                    "-fx-background-color: linear-gradient(135deg, #667eea, #764ba2);" +
                            "-fx-background-radius: 15;"
            );

            Label dayBig = new Label(String.valueOf(evenement.getDate().getDayOfMonth()));
            dayBig.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: white;");

            Label monthBig = new Label(evenement.getDate().format(DateTimeFormatter.ofPattern("MMMM yyyy")));
            monthBig.setStyle("-fx-font-size: 14px; -fx-text-fill: rgba(255,255,255,0.9);");

            dateBigBox.getChildren().addAll(dayBig, monthBig);

            Label titleBig = new Label(evenement.getTitre());
            titleBig.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
            titleBig.setWrapText(true);

            header.getChildren().addAll(dateBigBox, titleBig);

            // Informations détaillées
            GridPane infoGrid = new GridPane();
            infoGrid.setHgap(20);
            infoGrid.setVgap(15);
            infoGrid.setStyle("-fx-padding: 20 0 20 0;");

            // Statut
            Label statusIcon = new Label(getStatusIcon(evenement));
            statusIcon.setStyle("-fx-font-size: 18px;");
            Label statusInfo = new Label(getStatusText(evenement));
            statusInfo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + getStatusColor(evenement) + ";");
            infoGrid.add(statusIcon, 0, 0);
            infoGrid.add(statusInfo, 1, 0);

            // Lieu
            Label locationIcon = new Label("📍");
            locationIcon.setStyle("-fx-font-size: 18px;");
            Label locationInfo = new Label(evenement.getLocalisation());
            locationInfo.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
            locationInfo.setWrapText(true);
            infoGrid.add(locationIcon, 0, 1);
            infoGrid.add(locationInfo, 1, 1);

            // Prix
            Label priceIcon = new Label("💰");
            priceIcon.setStyle("-fx-font-size: 18px;");
            double price = evenementAI != null ?
                    evenementAI.calculateDynamicPrice(evenement.getIdEvenement()) : 50.0;
            Label priceInfo = new Label(String.format("%.0f TND", price));
            priceInfo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #f39c12;");
            infoGrid.add(priceIcon, 0, 2);
            infoGrid.add(priceInfo, 1, 2);

            // Participants
            Label attendeesIcon = new Label("👥");
            attendeesIcon.setStyle("-fx-font-size: 18px;");
            int attendees = 15 + (int)(Math.random() * 30);
            Label attendeesInfo = new Label(attendees + " personnes participeront");
            attendeesInfo.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
            infoGrid.add(attendeesIcon, 0, 3);
            infoGrid.add(attendeesInfo, 1, 3);

            // Description
            Separator separator = new Separator();
            separator.setStyle("-fx-background-color: #e0e0e0;");

            Label descTitle = new Label("Description");
            descTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #4A6FA5;");

            Label descContent = new Label(evenement.getDescription());
            descContent.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d;");
            descContent.setWrapText(true);

            // Boutons d'action
            HBox actionButtons = new HBox(15);
            actionButtons.setAlignment(Pos.CENTER);
            actionButtons.setStyle("-fx-padding: 20 0 0 0;");

            Button reserveBtn = new Button("Réserver maintenant");
            reserveBtn.setStyle(
                    "-fx-background-color: #5FB49C;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 14px;" +
                            "-fx-background-radius: 25;" +
                            "-fx-padding: 12 30;" +
                            "-fx-cursor: hand;"
            );

            Button closeBtn = new Button("Fermer");
            closeBtn.setStyle(
                    "-fx-background-color: #e0e0e0;" +
                            "-fx-text-fill: #7f8c8d;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 14px;" +
                            "-fx-background-radius: 25;" +
                            "-fx-padding: 12 30;" +
                            "-fx-cursor: hand;"
            );

            // Désactiver la réservation si l'événement est passé
            if (isEventPassed(evenement)) {
                reserveBtn.setDisable(true);
                reserveBtn.setStyle(
                        "-fx-background-color: #e0e0e0;" +
                                "-fx-text-fill: #95a5a6;" +
                                "-fx-font-weight: bold;" +
                                "-fx-font-size: 14px;" +
                                "-fx-background-radius: 25;" +
                                "-fx-padding: 12 30;" +
                                "-fx-cursor: default;"
                );
            }

            reserveBtn.setOnAction(e -> {
                dialog.close();
                openReservationWindow(evenement);
            });

            closeBtn.setOnAction(e -> dialog.close());

            actionButtons.getChildren().addAll(reserveBtn, closeBtn);

            // Assemblage final
            content.getChildren().addAll(
                    header,
                    infoGrid,
                    separator,
                    descTitle,
                    descContent,
                    actionButtons
            );

            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().setStyle("-fx-background-color: transparent;");
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            // Cacher le bouton close par défaut
            Node closeButton = dialog.getDialogPane().lookupButton(ButtonType.CLOSE);
            if (closeButton != null) {
                closeButton.setVisible(false);
            }

            dialog.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'afficher les détails: " + e.getMessage());
        }
    }

    private String getStatusIcon(Evenement event) {
        if (isEventPassed(event)) return "🔴";
        if (isEventToday(event)) return "🟢";
        return "🔵";
    }

    private String getStatusText(Evenement event) {
        if (isEventPassed(event)) return "Événement passé";
        if (isEventToday(event)) return "Événement aujourd'hui !";
        return "Événement à venir";
    }

    private String getStatusColor(Evenement event) {
        if (isEventPassed(event)) return "#ff6b6b";
        if (isEventToday(event)) return "#5FB49C";
        return "#667eea";
    }

    private boolean isEventPassed(Evenement event) {
        return event.getDate().isBefore(LocalDate.now());
    }

    private boolean isEventToday(Evenement event) {
        return event.getDate().equals(LocalDate.now());
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateStats() {
        try {
            if (lblTotalEvents != null) {
                lblTotalEvents.setText(String.valueOf(data.size()));
            }
            if (lblTotalReservations != null) {
                // Vous pouvez remplacer par le vrai nombre de réservations
                lblTotalReservations.setText(String.valueOf(data.size() * 3));
            }
            if (lblEventsMois != null) {
                long eventsThisMonth = data.stream()
                        .filter(e -> e.getDate().getMonth() == LocalDate.now().getMonth())
                        .count();
                lblEventsMois.setText(String.valueOf(eventsThisMonth));
            }
            if (lblMoyenneAvis != null) {
                lblMoyenneAvis.setText("4.5");
            }
        } catch (Exception e) {
            System.err.println("❌ Error updating stats: " + e.getMessage());
        }
    }

    @FXML
    private void handleListClick() {
        Evenement selected = listEvenements.getSelectionModel().getSelectedItem();
        if (selected == null) {
            System.out.println("❌ No event selected");
            return;
        }
        fillForm(selected);
    }

    @FXML
    private void addSampleEvents(ActionEvent event) {
        System.out.println("ℹ️ addSampleEvents: disabled in restored controller");
    }

    @FXML
    private void handleMesReservations(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MesReservations.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Mes Réservations");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            System.err.println("❌ Error opening reservations: " + e.getMessage());
            alert("Erreur", "Impossible d'ouvrir la fenêtre des réservations");
        }
    }

    @FXML
    private void hoverAjouter() {}

    @FXML
    private void exitAjouter() {}

    @FXML
    private void hoverModifier() {}

    @FXML
    private void exitModifier() {}

    @FXML
    private void hoverSupprimer() {}

    @FXML
    private void exitSupprimer() {}

    @FXML
    private void afficherEvenements() {
        try {
            Connection conn = mydb.getInstance().getConnection();
            if (conn != null && !conn.isClosed()) {
                service = new EvenementControlleur(conn);
                List<Evenement> events = service.afficher();
                System.out.println("✅ Loaded " + events.size() + " events");

                data.clear();
                data.addAll(events);
                listEvenements.setItems(data);

                lblCount.setText("Total: " + events.size() + " événement(s)");
                updateStats();
            } else {
                System.err.println("❌ Cannot get database connection");
            }
        } catch (Exception e) {
            System.err.println("❌ Error loading events: " + e.getMessage());
        }
    }

    @FXML
    private void ajouterEvenement(ActionEvent event) {
        try {
            Evenement e = new Evenement();
            e.setTitre(txtTitre.getText().trim());
            e.setDescription(txtDescription.getText().trim());
            e.setDate(datePicker.getValue());
            e.setLocalisation(txtLocalisation.getText().trim());

            Connection conn = mydb.getInstance().getConnection();
            if (conn != null && !conn.isClosed()) {
                service = new EvenementControlleur(conn);
                service.ajouter(e);
                afficherEvenements();
                clearFields();
                System.out.println("✅ Event added successfully");
            } else {
                System.err.println("❌ Cannot get database connection for adding event");
            }
        } catch (Exception ex) {
            System.err.println("❌ Error adding event: " + ex.getMessage());
        }
    }

    @FXML
    private void modifierEvenement(ActionEvent event) {
        Evenement selected = listEvenements.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alert("Sélection", "Choisissez un événement");
            return;
        }

        try {
            selected.setTitre(txtTitre.getText().trim());
            selected.setDescription(txtDescription.getText().trim());
            selected.setDate(datePicker.getValue());
            selected.setLocalisation(txtLocalisation.getText().trim());

            Connection conn = mydb.getInstance().getConnection();
            if (conn != null && !conn.isClosed()) {
                service = new EvenementControlleur(conn);
                service.modifier(selected);
                afficherEvenements();
                clearFields();
                System.out.println("✅ Event updated successfully");
            } else {
                System.err.println("❌ Cannot get database connection for updating event");
            }
        } catch (Exception ex) {
            System.err.println("❌ Error updating event: " + ex.getMessage());
        }
    }

    @FXML
    private void supprimerEvenement(ActionEvent event) {
        Evenement selected = listEvenements.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alert("Sélection", "Choisissez un événement");
            return;
        }

        try {
            Connection conn = mydb.getInstance().getConnection();
            if (conn != null && !conn.isClosed()) {
                service = new EvenementControlleur(conn);
                service.supprimer(selected.getIdEvenement());
                afficherEvenements();
                clearFields();
                System.out.println("✅ Event deleted successfully");
            } else {
                System.err.println("❌ Cannot get database connection for deleting event");
            }
        } catch (Exception ex) {
            System.err.println("❌ Error deleting event: " + ex.getMessage());
        }
    }

    @FXML
    private void refreshEvenements() {
        System.out.println("🔄 Manual refresh triggered");
        afficherEvenements();
    }

    private void clearFields() {
        txtTitre.clear();
        txtDescription.clear();
        datePicker.setValue(null);
        txtLocalisation.clear();
    }

    private void alert(String titre, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(titre);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void showPopup(Evenement evenement) {
        showEventDetails(evenement);
    }

    private void fillForm(Evenement evenement) {
        javafx.application.Platform.runLater(() -> {
            if (txtTitre != null) {
                txtTitre.setText(evenement.getTitre());
            }
            if (txtDescription != null) {
                txtDescription.setText(evenement.getDescription());
            }
            if (datePicker != null) {
                datePicker.setValue(evenement.getDate());
            }
            if (txtLocalisation != null) {
                txtLocalisation.setText(evenement.getLocalisation());
            }
            listEvenements.getSelectionModel().select(evenement);
        });
    }

    private void confirmDelete(Evenement evenement) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'événement");
        confirm.setContentText("Voulez-vous vraiment supprimer \"" + evenement.getTitre() + "\" ?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                Connection conn = mydb.getInstance().getConnection();
                if (conn != null && !conn.isClosed()) {
                    service = new EvenementControlleur(conn);
                    service.supprimer(evenement.getIdEvenement());
                    afficherEvenements();
                    clearFields();
                    System.out.println("✅ Event deleted from card");
                }
            } catch (Exception e) {
                System.err.println("❌ Error deleting event from card: " + e.getMessage());
            }
        }
    }

    private void openReservationWindow(Evenement evenement) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ReservationView.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Réservation - " + evenement.getTitre());
            stage.initModality(Modality.APPLICATION_MODAL);

            ReservationController controller = loader.getController();
            controller.setEventData(evenement);

            stage.showAndWait();
        } catch (Exception e) {
            System.err.println("❌ Error opening reservation: " + e.getMessage());
            e.printStackTrace();
            alert("Erreur", "Impossible d'ouvrir la fenêtre de réservation: " + e.getMessage());
        }
    }

    private void showRecommendations(Evenement evenement) {
        // Gardez votre méthode existante
    }

    private void showDynamicPricing(Evenement evenement) {
        // Gardez votre méthode existante
    }
}