package Controllers;

import Models.Reservation;
import Models.users;
import Services.AvisAI;
import Services.AvisSchema;
import Services.ReservationAI;
import javafx.fxml.FXMLLoader;
import utils.Database;
import utils.SessionManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Priority;

import java.sql.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.logging.Level;

public class MesReservationsController {

    private static final Logger LOGGER = Logger.getLogger(MesReservationsController.class.getName());
    private static final String AVIS_SCHEMA = "doua";

    @FXML private ListView<Reservation> listViewReservations;
    @FXML private Label lblCountReservations;
    @FXML private Button btnFermer;
    @FXML private Button btnDonnerAvis;

    // Labels pour les informations utilisateur
    @FXML private Label lblUserWelcome;
    @FXML private Label lblUserRole;
    @FXML private Label lblUserName;
    @FXML private Label lblDate;
    @FXML private Button btnHome;

    private ReservationControlleur reservationService;
    private ReservationAI reservationAI;
    private AvisAI avisAI;
    private ObservableList<Reservation> reservationsData = FXCollections.observableArrayList();
    private Connection cnx;

    // Utilisateur connecté
    private users currentUser;

    @FXML
    public void initialize() {
        LOGGER.info("=== Initialisation MesReservationsController ===");

        try {
            // Afficher la date du jour
            updateCurrentDate();

            // Récupérer l'utilisateur connecté depuis la session
            currentUser = SessionManager.getInstance().getCurrentUser();

            // Configurer l'affichage utilisateur
            configureUserDisplay();

            cnx = Database.getInstance().getConnection();
            reservationService = new ReservationControlleur(cnx);
            reservationAI = new ReservationAI();
            avisAI = new AvisAI();

            chargerReservations();
            setupCellFactory();

            LOGGER.info("✅ MesReservationsController initialized successfully!");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ Error initializing controller: " + e.getMessage(), e);
        }
    }

    private void updateCurrentDate() {
        if (lblDate != null) {
            lblDate.setText(java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
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
            default: return "👤 Utilisateur";
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
        LOGGER.info("=== Retour à l'accueil depuis Mes Réservations ===");

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

    private void setupCellFactory() {
        listViewReservations.setCellFactory(lv -> new ListCell<Reservation>() {
            @Override
            protected void updateItem(Reservation r, boolean empty) {
                super.updateItem(r, empty);

                if (empty || r == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    try {
                        HBox cellBox = new HBox(15);
                        cellBox.setAlignment(Pos.CENTER_LEFT);
                        cellBox.setPadding(new Insets(12, 10, 12, 15));
                        cellBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; -fx-border-color: #e0e0e0; -fx-border-radius: 10;");

                        Label iconLabel = new Label("📅");
                        iconLabel.setStyle("-fx-font-size: 22px;");

                        VBox detailsBox = new VBox(5);

                        Label dateLabel = new Label("Date: " + r.getDateReservation().toLocalDate());
                        dateLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 14px;");

                        Label infoLabel = new Label("👥 " + r.getNombrePersonnes() + " personne(s) • 📧 " + r.getEmail());
                        infoLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");

                        detailsBox.getChildren().addAll(dateLabel, infoLabel);

                        HBox aiButtonsBox = new HBox(8);
                        aiButtonsBox.setAlignment(Pos.CENTER_RIGHT);

                        Button btnSeatAllocation = new Button("🪑");
                        btnSeatAllocation.setStyle("-fx-background-color: #9B59B6; -fx-text-fill: white; -fx-background-radius: 8; -fx-min-width: 30; -fx-min-height: 30; -fx-cursor: hand;");
                        btnSeatAllocation.setTooltip(new Tooltip("Seat Allocation AI"));
                        btnSeatAllocation.setOnAction(e -> showSeatAllocation(r));

                        Button btnFraudAnalysis = new Button("🔍");
                        btnFraudAnalysis.setStyle("-fx-background-color: #E67E22; -fx-text-fill: white; -fx-background-radius: 8; -fx-min-width: 30; -fx-min-height: 30; -fx-cursor: hand;");
                        btnFraudAnalysis.setTooltip(new Tooltip("Fraud Analysis"));
                        btnFraudAnalysis.setOnAction(e -> showFraudAnalysis(r));

                        aiButtonsBox.getChildren().addAll(btnSeatAllocation, btnFraudAnalysis);

                        HBox contentBox = new HBox(10);
                        contentBox.setAlignment(Pos.CENTER_LEFT);
                        HBox.setHgrow(detailsBox, Priority.ALWAYS);
                        contentBox.getChildren().addAll(iconLabel, detailsBox, aiButtonsBox);

                        cellBox.getChildren().add(contentBox);

                        setGraphic(cellBox);
                        setText(null);
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Error updating reservation cell", e);
                    }
                }
            }
        });
    }

    private void chargerReservations() {
        try {
            int userId = getCurrentUserId();
            List<Reservation> list = reservationService.getByUtilisateur(userId);
            reservationsData.setAll(list);
            listViewReservations.setItems(reservationsData);
            lblCountReservations.setText(list.size() + " réservation(s)");
            LOGGER.info("✅ Chargé " + list.size() + " réservations");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌ Error loading reservations: " + e.getMessage(), e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les réservations: " + e.getMessage());
        }
    }
    private int getCurrentUserId() {
        // Récupérer l'utilisateur depuis SessionManager si currentUser est null
        if (currentUser == null) {
            currentUser = SessionManager.getInstance().getCurrentUser();
        }

        if (currentUser != null) {
            return currentUser.getId();
        }

        // Fallback : ID par défaut
        LOGGER.warning("⚠️ Aucun utilisateur connecté, utilisation de l'ID par défaut: 1");
        return 1;
    }

    @FXML
    private void handleDonnerAvis() {
        Reservation selected = listViewReservations.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner une réservation");
            return;
        }

        if (cnx == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Connexion base de données indisponible");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Donner mon avis");
        dialog.setHeaderText(null);

        ButtonType btnEnvoyer = new ButtonType("Envoyer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnEnvoyer, ButtonType.CANCEL);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle(
                "-fx-background-color: white;"
                        + "-fx-background-radius: 18;"
                        + "-fx-border-color: #4A6FA5;"
                        + "-fx-border-radius: 18;"
                        + "-fx-border-width: 2;"
                        + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 16, 0.2, 0, 6);"
                        + "-fx-padding: 10;"
        );

        Button envoyerButton = (Button) dialogPane.lookupButton(btnEnvoyer);
        if (envoyerButton != null) {
            envoyerButton.setStyle(
                    "-fx-background-color: linear-gradient(from 0% 0% to 100% 0%, #5FB49C, #4AA189);"
                            + "-fx-text-fill: white;"
                            + "-fx-font-weight: bold;"
                            + "-fx-background-radius: 12;"
                            + "-fx-padding: 8 22;"
                            + "-fx-cursor: hand;"
            );
        }

        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        if (cancelButton != null) {
            cancelButton.setStyle(
                    "-fx-background-color: linear-gradient(from 0% 0% to 100% 0%, #E667AF, #D3549C);"
                            + "-fx-text-fill: white;"
                            + "-fx-font-weight: bold;"
                            + "-fx-background-radius: 12;"
                            + "-fx-padding: 8 22;"
                            + "-fx-cursor: hand;"
            );
        }

        Spinner<Integer> spinnerNote = new Spinner<>(1, 5, 5);
        spinnerNote.setEditable(false);
        spinnerNote.setPrefWidth(100);

        TextArea txtCommentaire = new TextArea();
        txtCommentaire.setPromptText("Votre commentaire...");
        txtCommentaire.setWrapText(true);
        txtCommentaire.setPrefRowCount(5);
        txtCommentaire.setStyle(
                "-fx-background-radius: 12;"
                        + "-fx-border-radius: 12;"
                        + "-fx-border-color: #e0e0e0;"
                        + "-fx-padding: 8;"
        );

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        Label header = new Label("Veuillez saisir votre avis");
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #4A6FA5;");

        Label lblNote = new Label("Note (1-5) :");
        lblNote.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");

        Label lblCom = new Label("Commentaire :");
        lblCom.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");

        grid.add(header, 0, 0, 2, 1);
        grid.add(lblNote, 0, 1);
        GridPane.setMargin(spinnerNote, new Insets(0, 0, 0, 10));
        grid.add(spinnerNote, 1, 1);
        grid.add(lblCom, 0, 2);
        grid.add(txtCommentaire, 0, 3, 2, 1);

        dialogPane.setContent(grid);

        dialog.showAndWait().ifPresent(result -> {
            if (result != btnEnvoyer) return;

            int note = spinnerNote.getValue();
            String commentaire = txtCommentaire.getText() == null ? "" : txtCommentaire.getText().trim();

            try {
                ensureAvisTable();
                upsertAvis(selected.getIdReservation(), getCurrentUserId(), note, commentaire);

                double sentimentScore = avisAI.analyzeSentiment(commentaire);

                showAlert(Alert.AlertType.INFORMATION, "Merci",
                        "Votre avis a été enregistré.\n\n🧠 Analyse de sentiment: " + getSentimentDescription(sentimentScore));

                showSentimentAnalysis(commentaire);
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "❌ Error saving review: " + e.getMessage(), e);
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'enregistrer votre avis: " + e.getMessage());
            }
        });
    }

    private void ensureAvisTable() throws SQLException {
        try (Connection freshConn = Database.getInstance().getConnection()) {
            if (freshConn == null || freshConn.isClosed()) {
                throw new SQLException("Cannot get database connection for avis table");
            }

            String avisTable = AVIS_SCHEMA + ".avis";
            try (Statement st = freshConn.createStatement()) {
                st.execute(
                        "CREATE TABLE IF NOT EXISTS " + avisTable + " ("
                                + "idAvis INT AUTO_INCREMENT PRIMARY KEY,"
                                + "idReservation INT NULL,"
                                + "utilisateur_id INT NULL,"
                                + "note INT NULL,"
                                + "commentaire TEXT NULL,"
                                + "date_avis TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP"
                                + ") ENGINE=InnoDB"
                );
                LOGGER.info("✅ Avis table ensured successfully");
            }
        }
    }

    private void upsertAvis(int reservationId, int utilisateurId, int note, String commentaire) throws SQLException {
        try (Connection freshConn = Database.getInstance().getConnection()) {
            String avisTable = AVIS_SCHEMA + ".avis";

            String sql = "INSERT INTO " + avisTable + " (idReservation, utilisateur_id, note, commentaire) " +
                    "VALUES (?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE note = VALUES(note), commentaire = VALUES(commentaire), date_avis = CURRENT_TIMESTAMP";

            try (PreparedStatement ps = freshConn.prepareStatement(sql)) {
                ps.setInt(1, reservationId);
                ps.setInt(2, utilisateurId);
                ps.setInt(3, note);
                ps.setString(4, commentaire);
                ps.executeUpdate();
                LOGGER.info("✅ Avis saved successfully");
            }
        }
    }

    @FXML
    private void handleFermer() {
        Stage stage = (Stage) listViewReservations.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ================== AI FEATURES ==================
    private void showSeatAllocation(Reservation reservation) {
        try {
            String allocatedSeats = reservationAI.allocateIntelligentSeats(
                    reservation.getIdEvenement(),
                    reservation.getNombrePersonnes()
            );

            Stage stage = new Stage();
            stage.setTitle("🪑 Seat Allocation AI");
            stage.initModality(Modality.APPLICATION_MODAL);

            BorderPane mainPane = new BorderPane();
            mainPane.setStyle("-fx-background-color: linear-gradient(135deg, #9B59B6 0%, #8E44AD 100%);");

            VBox centerBox = new VBox(20);
            centerBox.setPadding(new Insets(30, 40, 30, 40));
            centerBox.setStyle("-fx-background-color: white; -fx-background-radius: 25; -fx-border-color: #9B59B6; -fx-border-width: 2;");
            centerBox.setAlignment(Pos.CENTER);

            Label titleLabel = new Label("🪑 INTELLIGENT SEAT ALLOCATION");
            titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #9B59B6;");

            Label infoLabel = new Label("For " + reservation.getNombrePersonnes() + " person(s):");
            infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #424242;");

            Label seatsLabel = new Label(allocatedSeats);
            seatsLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-background-color: #f8f9fa; -fx-background-radius: 10; -fx-padding: 15;");

            Label algorithmLabel = new Label("AI Algorithm: Proximity-based grouping with venue optimization");
            algorithmLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d; -fx-font-style: italic;");

            Button btnClose = new Button("Close");
            btnClose.setStyle("-fx-background-color: #9B59B6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 30; -fx-background-radius: 12;");
            btnClose.setOnAction(e -> stage.close());

            centerBox.getChildren().addAll(titleLabel, infoLabel, seatsLabel, algorithmLabel, btnClose);

            mainPane.setCenter(centerBox);
            BorderPane.setMargin(centerBox, new Insets(25));

            stage.setScene(new Scene(mainPane, 500, 400));
            stage.show();

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Seat allocation error", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Unable to calculate seat allocation: " + e.getMessage());
        }
    }

    private void showFraudAnalysis(Reservation reservation) {
        try {
            boolean isSuspicious = reservationAI.isSuspicious(reservation);
            double fraudProbability = reservationAI.calculateFraudProbability(reservation);

            Stage stage = new Stage();
            stage.setTitle("🔍 Fraud Analysis");
            stage.initModality(Modality.APPLICATION_MODAL);

            BorderPane mainPane = new BorderPane();
            mainPane.setStyle("-fx-background-color: linear-gradient(135deg, #E67E22 0%, #D35400 100%);");

            VBox centerBox = new VBox(20);
            centerBox.setPadding(new Insets(30, 40, 30, 40));
            centerBox.setStyle("-fx-background-color: white; -fx-background-radius: 25; -fx-border-color: #E67E22; -fx-border-width: 2;");

            Label titleLabel = new Label("🔍 FRAUD DETECTION ANALYSIS");
            titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #E67E22;");

            VBox riskBox = new VBox(10);
            riskBox.setStyle("-fx-background-color: #fff3cd; -fx-background-radius: 15; -fx-padding: 20;");
            riskBox.setAlignment(Pos.CENTER);

            Label riskTitle = new Label("Risk Score");
            riskTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #856404;");

            Label riskScore = new Label(String.format("%.1f%%", fraudProbability * 100));
            String riskColor = fraudProbability > 0.7 ? "#dc3545" : fraudProbability > 0.3 ? "#ffc107" : "#28a745";
            riskScore.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + riskColor + ";");

            Label riskStatus = new Label(isSuspicious ? "⚠️ HIGH RISK" : "✅ LOW RISK");
            String statusColor = isSuspicious ? "#dc3545" : "#28a745";
            riskStatus.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + statusColor + ";");

            riskBox.getChildren().addAll(riskTitle, riskScore, riskStatus);

            VBox factorsBox = new VBox(8);
            factorsBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 15; -fx-padding: 20;");

            Label factorsTitle = new Label("Analysis Factors:");
            factorsTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #424242;");

            Label[] factors = {
                    new Label("• Group size pattern analysis"),
                    new Label("• Booking timing evaluation"),
                    new Label("• Email domain verification"),
                    new Label("• User history assessment"),
                    new Label("• Phone number validation")
            };

            for (Label f : factors) {
                f.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
            }

            factorsBox.getChildren().add(factorsTitle);
            factorsBox.getChildren().addAll(factors);

            Button btnClose = new Button("Close");
            btnClose.setStyle("-fx-background-color: #E67E22; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 30; -fx-background-radius: 12;");
            btnClose.setOnAction(e -> stage.close());

            centerBox.getChildren().addAll(titleLabel, riskBox, factorsBox, btnClose);

            mainPane.setCenter(centerBox);
            BorderPane.setMargin(centerBox, new Insets(25));

            stage.setScene(new Scene(mainPane, 600, 500));
            stage.show();

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Fraud analysis error", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Unable to perform fraud analysis: " + e.getMessage());
        }
    }

    private void showSentimentAnalysis(String commentaire) {
        try {
            double sentimentScore = avisAI.analyzeSentiment(commentaire);

            Stage stage = new Stage();
            stage.setTitle("🧠 Sentiment Analysis");
            stage.initModality(Modality.APPLICATION_MODAL);

            BorderPane mainPane = new BorderPane();
            mainPane.setStyle("-fx-background-color: linear-gradient(135deg, #3498db 0%, #2980b9 100%);");

            VBox centerBox = new VBox(20);
            centerBox.setPadding(new Insets(30, 40, 30, 40));
            centerBox.setStyle("-fx-background-color: white; -fx-background-radius: 25; -fx-border-color: #3498db; -fx-border-width: 2;");
            centerBox.setAlignment(Pos.CENTER);

            Label titleLabel = new Label("🧠 SENTIMENT ANALYSIS");
            titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #3498db;");

            String sentimentColor = sentimentScore > 0.2 ? "#27ae60" : sentimentScore < -0.2 ? "#e74c3c" : "#f39c12";
            Label sentimentLabel = new Label(getSentimentDescription(sentimentScore));
            sentimentLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + sentimentColor + ";");

            Label scoreLabel = new Label("Score: " + String.format("%.2f", sentimentScore));
            scoreLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #424242;");

            ProgressBar sentimentBar = new ProgressBar((sentimentScore + 1) / 2);
            sentimentBar.setStyle("-fx-accent: " + sentimentColor + ";");
            sentimentBar.setPrefWidth(200);

            Button btnClose = new Button("Close");
            btnClose.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 30; -fx-background-radius: 12;");
            btnClose.setOnAction(e -> stage.close());

            centerBox.getChildren().addAll(titleLabel, sentimentLabel, scoreLabel, sentimentBar, btnClose);

            mainPane.setCenter(centerBox);
            BorderPane.setMargin(centerBox, new Insets(25));

            stage.setScene(new Scene(mainPane, 450, 350));
            stage.show();

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Sentiment analysis error", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Unable to analyze sentiment: " + e.getMessage());
        }
    }

    private String getSentimentDescription(double sentimentScore) {
        if (sentimentScore > 0.5) return "😊 Very Positive";
        if (sentimentScore > 0.2) return "🙂 Positive";
        if (sentimentScore > -0.2) return "😐 Neutral";
        if (sentimentScore > -0.5) return "🙁 Negative";
        return "😞 Very Negative";
    }
}