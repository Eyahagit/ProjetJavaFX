package services;

import Modele.Reservation;
import utiles.mydb;
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
import java.sql.DatabaseMetaData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// AvisSchema class to handle dynamic column names
class AvisSchema {
    String reservationCol;
    String utilisateurCol;
    String noteCol;
    String commentaireCol;
    String dateCol;
}

public class MesReservationsController {

    private static final String AVIS_SCHEMA = "doua";

    @FXML private ListView<Reservation> listViewReservations;
    @FXML private Label lblCountReservations;
    @FXML private Button btnFermer;
    @FXML private Button btnDonnerAvis;

    private ReservationControlleur reservationService;
    private ReservationAI reservationAI;
    private AvisAI avisAI;
    private ObservableList<Reservation> reservationsData = FXCollections.observableArrayList();
    private Connection cnx;

    @FXML
    public void initialize() {
        cnx = mydb.getInstance().getConnection();
        reservationService = new ReservationControlleur(cnx);
        reservationAI = new ReservationAI();
        avisAI = new AvisAI();
        chargerReservations();

        // Personnaliser l'affichage avec design amélioré
        listViewReservations.setCellFactory(lv -> new ListCell<Reservation>() {
            @Override
            protected void updateItem(Reservation r, boolean empty) {
                super.updateItem(r, empty);

                if (empty || r == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Créer un HBox pour un meilleur design
                    HBox cellBox = new HBox(15);
                    cellBox.setAlignment(Pos.CENTER_LEFT);
                    cellBox.setPadding(new Insets(12, 10, 12, 15));
                    cellBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; -fx-border-color: #e0e0e0; -fx-border-radius: 10;");

                    // Icône calendrier
                    Label iconLabel = new Label("📅");
                    iconLabel.setStyle("-fx-font-size: 22px;");

                    // Détails de la réservation
                    VBox detailsBox = new VBox(5);

                    Label dateLabel = new Label("Date: " + r.getDateReservation().toLocalDate());
                    dateLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 14px;");

                    Label infoLabel = new Label("👥 " + r.getNombrePersonnes() + " personne(s) • 📧 " + r.getEmail());
                    infoLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");
                    
                    // AI-powered fraud detection indicator
                    try {
                        boolean isSuspicious = reservationAI.isSuspicious(r);
                        double fraudProbability = reservationAI.calculateFraudProbability(r);
                        
                        Label aiLabel = new Label();
                        if (isSuspicious) {
                            aiLabel.setText("⚠️ Suspicious (" + String.format("%.1f%%", fraudProbability * 100) + ")");
                            aiLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11px; -fx-font-weight: bold;");
                        } else {
                            aiLabel.setText("✅ Verified (" + String.format("%.1f%%", (1-fraudProbability) * 100) + ")");
                            aiLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 11px; -fx-font-weight: bold;");
                        }
                        detailsBox.getChildren().addAll(dateLabel, infoLabel, aiLabel);
                    } catch (Exception e) {
                        detailsBox.getChildren().addAll(dateLabel, infoLabel);
                    }

                    // AI action buttons
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

                    cellBox.getChildren().addAll(contentBox);

                    setGraphic(cellBox);
                    setText(null);
                }
            }
        });
    }

    private void chargerReservations() {
        try {
            int userId = UserSession.getId();
            List<Reservation> list = reservationService.getByUtilisateur(userId);
            reservationsData.setAll(list);
            listViewReservations.setItems(reservationsData);
            lblCountReservations.setText(list.size() + " réservation(s)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDonnerAvis() {
        Reservation selected = listViewReservations.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("⚠️ Attention");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner une réservation");
            alert.showAndWait();
            return;
        }

        if (cnx == null) {
            alert("Erreur", "Connexion base de données indisponible");
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
            if (result != btnEnvoyer) {
                return;
            }

            int note = spinnerNote.getValue();
            String commentaire = txtCommentaire.getText() == null ? "" : txtCommentaire.getText().trim();

            try {
                ensureAvisTable();
                upsertAvis(selected.getIdReservation(), UserSession.getId(), note, commentaire);
                
                // Perform AI analysis on the saved review
                double sentimentScore = avisAI.analyzeSentiment(commentaire);
                
                Alert ok = new Alert(Alert.AlertType.INFORMATION);
                ok.setTitle("✅ Merci");
                ok.setHeaderText(null);
                ok.setContentText("Votre avis a été enregistré.\n\n🧠 Analyse de sentiment: " + getSentimentDescription(sentimentScore));
                ok.showAndWait();
                
                // Show detailed sentiment analysis
                showSentimentAnalysis(commentaire);
            } catch (SQLException e) {
                e.printStackTrace();
                alert("Erreur", "Impossible d'enregistrer votre avis: " + e.getMessage());
            }
        });
    }

    private void ensureAvisTable() throws SQLException {
        // Use fresh connection from mydb to avoid connection closed errors
        Connection freshConn = mydb.getInstance().getConnection();
        if (freshConn == null || freshConn.isClosed()) {
            throw new SQLException("Cannot get database connection for avis table");
        }
        
        String avisTable = AVIS_SCHEMA + ".avis";
        try (Statement st = freshConn.createStatement()) {
            try {
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
            } catch (SQLException e) {
                if (isBrokenTableError(e)) {
                    repairAvisTable();
                } else {
                    throw e;
                }
            }

            safeExecute(st, "ALTER TABLE " + avisTable + " ADD COLUMN idReservation INT NULL");
            safeExecute(st, "ALTER TABLE " + avisTable + " ADD COLUMN reservation_id INT NULL");
            safeExecute(st, "ALTER TABLE " + avisTable + " ADD COLUMN utilisateur_id INT NULL");
            safeExecute(st, "ALTER TABLE " + avisTable + " ADD COLUMN note INT NULL");
            safeExecute(st, "ALTER TABLE " + avisTable + " ADD COLUMN commentaire TEXT NULL");
            safeExecute(st, "ALTER TABLE " + avisTable + " ADD COLUMN date_avis TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP");

            safeExecute(st, "CREATE UNIQUE INDEX uk_avis_res_user ON " + avisTable + "(idReservation, utilisateur_id)");
            safeExecute(st, "CREATE UNIQUE INDEX uk_avis_res_user2 ON " + avisTable + "(reservation_id, utilisateur_id)");
            System.out.println("✅ Avis table ensured successfully");
        }
    }

    private void repairAvisTable() throws SQLException {
        // Use fresh connection from mydb to avoid connection closed errors
        Connection freshConn = mydb.getInstance().getConnection();
        if (freshConn == null || freshConn.isClosed()) {
            throw new SQLException("Cannot get database connection for avis table repair");
        }
        
        String avisTable = AVIS_SCHEMA + ".avis";
        try (Statement st = freshConn.createStatement()) {
            safeExecute(st, "SET FOREIGN_KEY_CHECKS=0");

            try {
                st.execute("DROP TABLE " + avisTable);
            } catch (SQLException ignored) {
                safeExecute(st, "DROP TABLE IF EXISTS " + avisTable);
            }

            st.execute(
                    "CREATE TABLE " + avisTable + " ("
                            + "idAvis INT AUTO_INCREMENT PRIMARY KEY,"
                            + "idReservation INT NOT NULL,"
                            + "utilisateur_id INT NOT NULL,"
                            + "note INT NOT NULL,"
                            + "commentaire TEXT NULL,"
                            + "date_avis TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,"
                            + "UNIQUE KEY uk_avis_res_user (idReservation, utilisateur_id)"
                            + ") ENGINE=InnoDB"
            );

            safeExecute(st, "SET FOREIGN_KEY_CHECKS=1");
            System.out.println("✅ Avis table repaired successfully");
        }
    }

    private boolean isBrokenTableError(SQLException e) {
        if (e.getErrorCode() == 1932) {
            return true;
        }
        String msg = e.getMessage();
        return msg != null && msg.toLowerCase().contains("doesn't exist in engine");
    }

    private void safeExecute(Statement st, String sql) {
        try {
            st.execute(sql);
        } catch (SQLException ignored) {
        }
    }

    private void upsertAvis(int reservationId, int utilisateurId, int note, String commentaire) throws SQLException {
        try {
            upsertAvisOnce(reservationId, utilisateurId, note, commentaire);
        } catch (SQLException e) {
            if (!isBrokenTableError(e)) {
                throw e;
            }

            repairAvisTable();
            upsertAvisOnce(reservationId, utilisateurId, note, commentaire);
        }
    }

    private void upsertAvisOnce(int reservationId, int utilisateurId, int note, String commentaire) throws SQLException {
        // Use fresh connection from mydb to avoid connection closed errors
        Connection freshConn = mydb.getInstance().getConnection();
        if (freshConn == null || freshConn.isClosed()) {
            throw new SQLException("Cannot get database connection for avis");
        }
        
        AvisSchema schema = resolveAvisSchema();
        if (schema == null) {
            throw new SQLException("Table 'avis' introuvable");
        }

        String avisTable = AVIS_SCHEMA + ".avis";

        String where = schema.reservationCol + " = ? AND " + schema.utilisateurCol + " = ?";
        String updateSql = "UPDATE " + avisTable + " SET " + schema.noteCol + " = ?, " + schema.commentaireCol
                + " = ?, " + schema.dateCol + " = CURRENT_TIMESTAMP WHERE " + where;

        try (PreparedStatement ps = freshConn.prepareStatement(updateSql)) {
            ps.setInt(1, note);
            ps.setString(2, commentaire);
            ps.setInt(3, reservationId);
            ps.setInt(4, utilisateurId);
            int updated = ps.executeUpdate();
            if (updated > 0) {
                System.out.println("✅ Avis updated successfully");
                return;
            }
        }

        String insertSql = "INSERT INTO " + avisTable + " (" + schema.reservationCol + ", " + schema.utilisateurCol + ", "
                + schema.noteCol + ", " + schema.commentaireCol + ") VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = freshConn.prepareStatement(insertSql)) {
            ps.setInt(1, reservationId);
            ps.setInt(2, utilisateurId);
            ps.setInt(3, note);
            ps.setString(4, commentaire);
            ps.executeUpdate();
            System.out.println("✅ Avis inserted successfully");
        }
    }

    private AvisSchema resolveAvisSchema() throws SQLException {
        try {
            return resolveAvisSchemaOnce();
        } catch (SQLException e) {
            if (!isBrokenTableError(e)) {
                throw e;
            }
            repairAvisTable();
            return resolveAvisSchemaOnce();
        }
    }

    private AvisSchema resolveAvisSchemaOnce() throws SQLException {
        // Use fresh connection from mydb to avoid connection closed errors
        Connection freshConn = mydb.getInstance().getConnection();
        if (freshConn == null || freshConn.isClosed()) {
            throw new SQLException("Cannot get database connection for avis schema");
        }
        
        DatabaseMetaData meta = freshConn.getMetaData();
        Set<String> cols = new HashSet<>();
        try (ResultSet rs = meta.getColumns(AVIS_SCHEMA, null, "avis", null)) {
            while (rs.next()) {
                String name = rs.getString("COLUMN_NAME");
                if (name != null) {
                    cols.add(name.toLowerCase());
                }
            }
        }

        if (cols.isEmpty()) {
            return null;
        }

        AvisSchema s = new AvisSchema();
        s.reservationCol = cols.contains("idreservation") ? "idReservation" : (cols.contains("reservation_id") ? "reservation_id" : null);
        s.utilisateurCol = cols.contains("utilisateur_id") ? "utilisateur_id" : (cols.contains("idutilisateur") ? "idUtilisateur" : null);
        s.noteCol = cols.contains("note") ? "note" : null;
        s.commentaireCol = cols.contains("commentaire") ? "commentaire" : null;
        s.dateCol = cols.contains("date_avis") ? "date_avis" : (cols.contains("dateavis") ? "dateAvis" : "date_avis");

        if (s.reservationCol == null || s.utilisateurCol == null || s.noteCol == null || s.commentaireCol == null) {
            return null;
        }

        return s;
    }

    @FXML
    private void handleFermer() {
        ((Stage) listViewReservations.getScene().getWindow()).close();
    }

    private void alert(String titre, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("⚠️ " + titre);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
    
    // ================== AI FEATURES ==================
    private void showSeatAllocation(Reservation reservation) {
        try {
            String allocatedSeats = reservationAI.allocateIntelligentSeats(reservation.getIdEvenement(), reservation.getNombrePersonnes());
            
            Stage stage = new Stage();
            stage.setTitle("🪑 Seat Allocation AI");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setWidth(500); stage.setHeight(400);
            
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
            alert("Error", "Unable to calculate seat allocation: " + e.getMessage());
        }
    }
    
    private void showFraudAnalysis(Reservation reservation) {
        try {
            boolean isSuspicious = reservationAI.isSuspicious(reservation);
            double fraudProbability = reservationAI.calculateFraudProbability(reservation);
            
            Stage stage = new Stage();
            stage.setTitle("🔍 Fraud Analysis");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setWidth(600); stage.setHeight(500);
            
            BorderPane mainPane = new BorderPane();
            mainPane.setStyle("-fx-background-color: linear-gradient(135deg, #E67E22 0%, #D35400 100%);");
            
            VBox centerBox = new VBox(20);
            centerBox.setPadding(new Insets(30, 40, 30, 40));
            centerBox.setStyle("-fx-background-color: white; -fx-background-radius: 25; -fx-border-color: #E67E22; -fx-border-width: 2;");
            
            Label titleLabel = new Label("🔍 FRAUD DETECTION ANALYSIS");
            titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #E67E22;");
            
            // Risk score visualization
            VBox riskBox = new VBox(10);
            riskBox.setStyle("-fx-background-color: #fff3cd; -fx-background-radius: 15; -fx-padding: 20;");
            riskBox.setAlignment(Pos.CENTER);
            
            Label riskTitle = new Label("Risk Score");
            riskTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #856404;");
            
            Label riskScore = new Label(String.format("%.1f%%", fraudProbability * 100));
            riskScore.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + 
                (fraudProbability > 0.7 ? "#dc3545" : fraudProbability > 0.3 ? "#ffc107" : "#28a745") + ";");
            
            Label riskStatus = new Label(isSuspicious ? "⚠️ HIGH RISK" : "✅ LOW RISK");
            riskStatus.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + 
                (isSuspicious ? "#dc3545" : "#28a745") + ";");
            
            riskBox.getChildren().addAll(riskTitle, riskScore, riskStatus);
            
            // Analysis factors
            VBox factorsBox = new VBox(8);
            factorsBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 15; -fx-padding: 20;");
            
            Label factorsTitle = new Label("Analysis Factors:");
            factorsTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #424242;");
            
            Label factor1 = new Label("• Group size pattern analysis");
            Label factor2 = new Label("• Booking timing evaluation");
            Label factor3 = new Label("• Email domain verification");
            Label factor4 = new Label("• User history assessment");
            Label factor5 = new Label("• Phone number validation");
            
            factor1.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
            factor2.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
            factor3.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
            factor4.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
            factor5.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
            
            factorsBox.getChildren().addAll(factorsTitle, factor1, factor2, factor3, factor4, factor5);
            
            Button btnClose = new Button("Close");
            btnClose.setStyle("-fx-background-color: #E67E22; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 30; -fx-background-radius: 12;");
            btnClose.setOnAction(e -> stage.close());
            
            centerBox.getChildren().addAll(titleLabel, riskBox, factorsBox, btnClose);
            
            mainPane.setCenter(centerBox);
            BorderPane.setMargin(centerBox, new Insets(25));
            
            stage.setScene(new Scene(mainPane, 600, 500));
            stage.show();
            
        } catch (Exception e) {
            alert("Error", "Unable to perform fraud analysis: " + e.getMessage());
        }
    }
    
    private void showSentimentAnalysis(String commentaire) {
        try {
            double sentimentScore = avisAI.analyzeSentiment(commentaire);
            String sentimentDesc = getSentimentDescription(sentimentScore);
            
            Stage stage = new Stage();
            stage.setTitle("🧠 Sentiment Analysis");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setWidth(450); stage.setHeight(350);
            
            BorderPane mainPane = new BorderPane();
            mainPane.setStyle("-fx-background-color: linear-gradient(135deg, #3498db 0%, #2980b9 100%);");
            
            VBox centerBox = new VBox(20);
            centerBox.setPadding(new Insets(30, 40, 30, 40));
            centerBox.setStyle("-fx-background-color: white; -fx-background-radius: 25; -fx-border-color: #3498db; -fx-border-width: 2;");
            centerBox.setAlignment(Pos.CENTER);
            
            Label titleLabel = new Label("🧠 SENTIMENT ANALYSIS");
            titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #3498db;");
            
            Label sentimentLabel = new Label(sentimentDesc);
            sentimentLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + 
                (sentimentScore > 0.2 ? "#27ae60" : sentimentScore < -0.2 ? "#e74c3c" : "#f39c12") + ";");
            
            Label scoreLabel = new Label("Score: " + String.format("%.2f", sentimentScore));
            scoreLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #424242;");
            
            ProgressBar sentimentBar = new ProgressBar((sentimentScore + 1) / 2); // Convert [-1,1] to [0,1]
            sentimentBar.setStyle("-fx-accent: " + 
                (sentimentScore > 0.2 ? "#27ae60" : sentimentScore < -0.2 ? "#e74c3c" : "#f39c12") + ";");
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
            alert("Error", "Unable to analyze sentiment: " + e.getMessage());
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