package Controllers;

import Models.Evenement;
import Models.users;
import Services.EvenementAI;
import utils.Database;
import utils.SessionManager;
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
import java.util.logging.Logger;
import java.util.logging.Level;

public class EvenementFXController {

    private static final Logger LOGGER = Logger.getLogger(EvenementFXController.class.getName());

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

    // Nouveaux éléments pour l'utilisateur connecté
    @FXML
    private Label lblUserWelcome;
    @FXML
    private Label lblUserRole;
    @FXML
    private Label lblUserName;
    @FXML
    private Label lblDate;
    @FXML
    private Button btnHome;

    private EvenementControlleur service;
    private EvenementAI evenementAI;
    private ObservableList<Evenement> data = FXCollections.observableArrayList();

    // Utilisateur connecté
    private users currentUser;

    @FXML
    public void initialize() {
        try {
            LOGGER.info("=== Initialisation EvenementFXController ===");

            // Afficher la date du jour
            updateCurrentDate();

            // Récupérer l'utilisateur connecté depuis la session
            currentUser = SessionManager.getInstance().getCurrentUser();

            // Configurer l'affichage utilisateur
            configureUserDisplay();

            // Initialiser la connexion et les services
            Connection conn = Database.getInstance().getConnection();
            if (conn != null && !conn.isClosed()) {
                service = new EvenementControlleur(conn);
                evenementAI = new EvenementAI(conn);
                afficherEvenements();
                setupModernEventCards();
                setupSearchFunctionality();
                updateStats();
                LOGGER.info("✅ EvenementFXController initialized successfully!");
            } else {
                LOGGER.severe("❌ Failed to initialize database connection");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ Error initializing controller: " + e.getMessage(), e);
        }
    }

    private void updateCurrentDate() {
        if (lblDate != null) {
            lblDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }
    }

    private void configureUserDisplay() {
        if (currentUser == null) {
            currentUser = SessionManager.getInstance().getCurrentUser();
        }

        if (currentUser != null) {
            String fullName = currentUser.getName() + " " + currentUser.getSecond_name();
            String role = currentUser.getRole();

            if (lblUserWelcome != null) {
                lblUserWelcome.setText("Bienvenue, " + fullName + " !");
            }
            if (lblUserName != null) {
                lblUserName.setText(fullName);
            }
            if (lblUserRole != null) {
                String roleText = getRoleDisplay(role);
                lblUserRole.setText(roleText);
                String color = getRoleColor(role);
                lblUserRole.setStyle("-fx-background-color: " + color + "; -fx-padding: 5 15; -fx-background-radius: 20; -fx-text-fill: white; -fx-font-weight: bold;");
            }
            LOGGER.info("✅ Utilisateur connecté: " + fullName + " (" + role + ")");
        } else {
            if (lblUserWelcome != null) lblUserWelcome.setText("Bienvenue, Invité !");
            if (lblUserName != null) lblUserName.setText("Invité");
            if (lblUserRole != null) {
                lblUserRole.setText("👤 Invité");
                lblUserRole.setStyle("-fx-background-color: #95a5a6; -fx-padding: 5 15; -fx-background-radius: 20; -fx-text-fill: white; -fx-font-weight: bold;");
            }
            LOGGER.info("⚠️ Aucun utilisateur connecté - Mode invité");
        }
    }

    public void setCurrentUser(users user) {
        if (user != null) {
            this.currentUser = user;
            SessionManager.getInstance().setCurrentUser(user);
            configureUserDisplay();
            LOGGER.info("✅ Utilisateur défini: " + user.getName() + " " + user.getSecond_name());
        }
    }

    public void setUser(users user) {
        setCurrentUser(user);
    }

    private String getRoleDisplay(String role) {
        if (role == null) return "👤 Invité";
        switch(role.toLowerCase()) {
            case "admin": return "👑 Administrateur";
            case "doctor": return "👨‍⚕️ Médecin";
            case "patient": return "👤 Patient";
            default: return "";
        }
    }

    private String getRoleColor(String role) {
        if (role == null) return "#95a5a6";
        switch(role.toLowerCase()) {
            case "admin": return "#9b59b6";
            case "doctor": return "#3498db";
            case "patient": return "#2ecc71";
            default: return "#95a5a6";
        }
    }

    @FXML
    private void handleHome() {
        LOGGER.info("=== Retour à l'accueil depuis Gestion des Événements ===");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/home.fxml"));
            Scene scene = new Scene(loader.load());

            HomeController homeController = loader.getController();
            if (homeController != null && currentUser != null) {
                homeController.setUser(currentUser);
                LOGGER.info("✅ Utilisateur transmis au HomeController: " + currentUser.getName());
            }

            Stage stage = (Stage) btnHome.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Accueil - GrowMind");
            stage.show();

            LOGGER.info("✅ Retour à l'accueil réussi");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ Erreur retour accueil: " + e.getMessage(), e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de retourner à l'accueil.\n" + e.getMessage());
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

            private final Label titleLabel = new Label();
            private final Label descriptionLabel = new Label();
            private final Label dateLabel = new Label();
            private final Label locationLabel = new Label();
            private final Label priceLabel = new Label();
            private final Label statusBadge = new Label();
            private final Label attendeesLabel = new Label();

            private final Button btnReserve = new Button("Réserver");
            private final Button btnDetails = new Button("Détails");

            private final VBox dateBox = new VBox(2);
            private final Label dayLabel = new Label();
            private final Label monthLabel = new Label();

            {
                // Configuration du style des cartes
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

                // Effets de survol
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

                // Bloc date
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

                // Styles des labels
                titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                titleLabel.setWrapText(true);
                titleLabel.setMaxWidth(300);

                descriptionLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d;");
                descriptionLabel.setWrapText(true);
                descriptionLabel.setMaxWidth(300);
                descriptionLabel.setMaxHeight(40);

                dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #667eea; -fx-font-weight: bold;");
                locationLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #5FB49C;");

                priceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #f39c12;");
                attendeesLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #95a5a6;");

                // Styles des boutons
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

                // Organisation des éléments
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

                // Actions des boutons
                btnReserve.setOnAction(e -> {
                    Evenement event = getItem();
                    if (event != null && !isEventPassed(event)) {
                        openReservationWindow(event);
                    } else if (isEventPassed(event)) {
                        showAlert(Alert.AlertType.INFORMATION, "Information", "Cet événement est déjà passé et n'accepte plus de réservations.");
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
                        dayLabel.setText(String.valueOf(evenement.getDate().getDayOfMonth()));
                        monthLabel.setText(evenement.getDate().format(DateTimeFormatter.ofPattern("MMM")));

                        titleLabel.setText(evenement.getTitre());
                        String desc = evenement.getDescription();
                        if (desc != null && desc.length() > 100) {
                            desc = desc.substring(0, 97) + "...";
                        }
                        descriptionLabel.setText(desc != null ? desc : "");

                        dateLabel.setText("📅 " + evenement.getDate().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
                        locationLabel.setText("📍 " + (evenement.getLocalisation() != null ? evenement.getLocalisation() : ""));

                        double price = evenementAI != null ?
                                evenementAI.calculateDynamicPrice(evenement.getIdEvenement()) : 50.0;
                        priceLabel.setText(String.format("💰 %.0f TND", price));

                        int attendees = 15 + (int)(Math.random() * 30);
                        attendeesLabel.setText("👥 " + attendees + " participants");

                        // Gestion du statut
                        if (isEventPassed(evenement)) {
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
                        LOGGER.log(Level.WARNING, "❌ Error updating event card: " + e.getMessage(), e);
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

            // Header
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

            // Grille d'informations
            GridPane infoGrid = new GridPane();
            infoGrid.setHgap(20);
            infoGrid.setVgap(15);
            infoGrid.setStyle("-fx-padding: 20 0 20 0;");

            Label statusIcon = new Label(getStatusIcon(evenement));
            statusIcon.setStyle("-fx-font-size: 18px;");
            Label statusInfo = new Label(getStatusText(evenement));
            statusInfo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + getStatusColor(evenement) + ";");
            infoGrid.add(statusIcon, 0, 0);
            infoGrid.add(statusInfo, 1, 0);

            Label locationIcon = new Label("📍");
            locationIcon.setStyle("-fx-font-size: 18px;");
            Label locationInfo = new Label(evenement.getLocalisation());
            locationInfo.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
            locationInfo.setWrapText(true);
            infoGrid.add(locationIcon, 0, 1);
            infoGrid.add(locationInfo, 1, 1);

            Label priceIcon = new Label("💰");
            priceIcon.setStyle("-fx-font-size: 18px;");
            double price = evenementAI != null ?
                    evenementAI.calculateDynamicPrice(evenement.getIdEvenement()) : 50.0;
            Label priceInfo = new Label(String.format("%.0f TND", price));
            priceInfo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #f39c12;");
            infoGrid.add(priceIcon, 0, 2);
            infoGrid.add(priceInfo, 1, 2);

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

            Node closeButton = dialog.getDialogPane().lookupButton(ButtonType.CLOSE);
            if (closeButton != null) {
                closeButton.setVisible(false);
            }

            dialog.showAndWait();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur affichage détails: " + e.getMessage(), e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'afficher les détails: " + e.getMessage());
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

    private void updateStats() {
        try {
            if (lblTotalEvents != null) {
                lblTotalEvents.setText(String.valueOf(data.size()));
            }
            if (lblTotalReservations != null) {
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
            LOGGER.log(Level.WARNING, "❌ Error updating stats: " + e.getMessage(), e);
        }
    }

    @FXML
    private void handleListClick() {
        Evenement selected = listEvenements.getSelectionModel().getSelectedItem();
        if (selected == null) {
            LOGGER.warning("❌ No event selected");
            return;
        }
        fillForm(selected);
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
            LOGGER.log(Level.SEVERE, "❌ Error opening reservations: " + e.getMessage(), e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre des réservations");
        }
    }

    @FXML
    private void afficherEvenements() {
        try {
            Connection conn = Database.getInstance().getConnection();
            if (conn != null && !conn.isClosed()) {
                service = new EvenementControlleur(conn);
                List<Evenement> events = service.afficher();
                LOGGER.info("✅ Loaded " + events.size() + " events");

                data.clear();
                data.addAll(events);
                listEvenements.setItems(data);

                lblCount.setText("Total: " + events.size() + " événement(s)");
                updateStats();
            } else {
                LOGGER.severe("❌ Cannot get database connection");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ Error loading events: " + e.getMessage(), e);
        }
    }

    @FXML
    private void ajouterEvenement(ActionEvent event) {
        try {
            if (txtTitre.getText().trim().isEmpty() ||
                    txtDescription.getText().trim().isEmpty() ||
                    datePicker.getValue() == null ||
                    txtLocalisation.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Tous les champs sont obligatoires");
                return;
            }

            Evenement e = new Evenement();
            e.setTitre(txtTitre.getText().trim());
            e.setDescription(txtDescription.getText().trim());
            e.setDate(datePicker.getValue());
            e.setLocalisation(txtLocalisation.getText().trim());

            Connection conn = Database.getInstance().getConnection();
            if (conn != null && !conn.isClosed()) {
                service = new EvenementControlleur(conn);
                service.ajouter(e);
                afficherEvenements();
                clearFields();
                LOGGER.info("✅ Event added successfully");
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Événement ajouté avec succès");
            } else {
                LOGGER.severe("❌ Cannot get database connection for adding event");
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "❌ Error adding event: " + ex.getMessage(), ex);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ajouter l'événement: " + ex.getMessage());
        }
    }

    @FXML
    private void modifierEvenement(ActionEvent event) {
        Evenement selected = listEvenements.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Choisissez un événement à modifier");
            return;
        }

        try {
            selected.setTitre(txtTitre.getText().trim());
            selected.setDescription(txtDescription.getText().trim());
            selected.setDate(datePicker.getValue());
            selected.setLocalisation(txtLocalisation.getText().trim());

            Connection conn = Database.getInstance().getConnection();
            if (conn != null && !conn.isClosed()) {
                service = new EvenementControlleur(conn);
                service.modifier(selected);
                afficherEvenements();
                clearFields();
                LOGGER.info("✅ Event updated successfully");
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Événement modifié avec succès");
            } else {
                LOGGER.severe("❌ Cannot get database connection for updating event");
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "❌ Error updating event: " + ex.getMessage(), ex);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de modifier l'événement: " + ex.getMessage());
        }
    }

    @FXML
    private void supprimerEvenement(ActionEvent event) {
        Evenement selected = listEvenements.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Choisissez un événement à supprimer");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'événement ?");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer \"" + selected.getTitre() + "\" ?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                Connection conn = Database.getInstance().getConnection();
                if (conn != null && !conn.isClosed()) {
                    service = new EvenementControlleur(conn);
                    service.supprimer(selected.getIdEvenement());
                    afficherEvenements();
                    clearFields();
                    LOGGER.info("✅ Event deleted successfully");
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Événement supprimé avec succès");
                } else {
                    LOGGER.severe("❌ Cannot get database connection for deleting event");
                }
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "❌ Error deleting event: " + ex.getMessage(), ex);
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer l'événement: " + ex.getMessage());
            }
        }
    }

    @FXML
    private void refreshEvenements() {
        LOGGER.info("🔄 Manual refresh triggered");
        afficherEvenements();
    }

    private void clearFields() {
        txtTitre.clear();
        txtDescription.clear();
        datePicker.setValue(null);
        txtLocalisation.clear();
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

    private void openReservationWindow(Evenement evenement) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ReservationView.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Réservation - " + evenement.getTitre());
            stage.initModality(Modality.APPLICATION_MODAL);

            ReservationController controller = loader.getController();
            controller.setEventData(evenement);

            // Passer l'utilisateur connecté
            if (controller != null && currentUser != null) {
                try {
                    controller.getClass().getMethod("setCurrentUser", users.class).invoke(controller, currentUser);
                } catch (Exception ex) {
                    // Ignorer si la méthode n'existe pas
                }
            }

            stage.showAndWait();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ Error opening reservation: " + e.getMessage(), e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre de réservation: " + e.getMessage());
        }
    }

    @FXML
    private void addSampleEvents(ActionEvent event) {
        try {
            // Créer quelques événements de test
            Evenement e1 = new Evenement();
            e1.setTitre("Atelier Méditation");
            e1.setDescription("Apprenez les bases de la méditation");
            e1.setDate(LocalDate.now().plusDays(5));
            e1.setLocalisation("Salle Zen");

            Evenement e2 = new Evenement();
            e2.setTitre("Conférence sur le stress");
            e2.setDescription("Comment gérer son stress au quotidien");
            e2.setDate(LocalDate.now().plusDays(12));
            e2.setLocalisation("Amphithéâtre");

            Connection conn = Database.getInstance().getConnection();
            if (conn != null && !conn.isClosed()) {
                EvenementControlleur service = new EvenementControlleur(conn);
                service.ajouter(e1);
                service.ajouter(e2);
                afficherEvenements();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Événements de test ajoutés");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur ajout événements test: " + e.getMessage(), e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ajouter les événements de test");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Méthodes vides pour les effets hover (requises par le FXML)
    @FXML private void hoverAjouter() {}
    @FXML private void exitAjouter() {}
    @FXML private void hoverModifier() {}
    @FXML private void exitModifier() {}
    @FXML private void hoverSupprimer() {}
    @FXML private void exitSupprimer() {}
}