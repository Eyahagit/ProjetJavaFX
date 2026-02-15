package org.example.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.example.Models.Evaluation;
import org.example.Models.Ressource;
import org.example.Models.User;
import org.example.Services.evaluationService;
import org.example.utils.StaticUser;

import java.time.LocalDate;

public class EvaluationModalController {

    @FXML
    private Label lblResourceTitle;
    @FXML
    private HBox ratingBox;
    @FXML
    private TextArea txtCommentaire;
    @FXML
    private Label lblRatingError;
    @FXML
    private Label lblCommentError;

    private int currentRating = 0;
    private Ressource ressource;
    private Evaluation existingEvaluation;
    private final evaluationService service = new evaluationService();
    private UserRessourceController parentController;

    @FXML
    public void initialize() {
        createStars();
    }

    public void setRessource(Ressource r, UserRessourceController parent) {
        this.ressource = r;
        this.parentController = parent;
        lblResourceTitle.setText(r.getTitle());
    }

    public void setExistingEvaluation(Evaluation e) {
        this.existingEvaluation = e;
        if (e != null) {
            setRating(e.getNote());
            txtCommentaire.setText(e.getCommentaire());
        }
    }

    private void createStars() {
        ratingBox.getChildren().clear();
        for (int i = 1; i <= 5; i++) {
            Button star = new Button("★");
            star.setStyle(
                    "-fx-background-color: transparent; -fx-font-size: 24px; -fx-text-fill: #bdc3c7; -fx-cursor: hand; -fx-padding: 0;");
            int ratingValue = i;
            star.setOnAction(e -> setRating(ratingValue));
            ratingBox.getChildren().add(star);
        }
    }

    private void setRating(int rating) {
        currentRating = rating;
        for (int i = 0; i < 5; i++) {
            Button star = (Button) ratingBox.getChildren().get(i);
            if (i < rating) {
                star.setStyle(
                        "-fx-background-color: transparent; -fx-font-size: 24px; -fx-text-fill: #F1C40F; -fx-cursor: hand; -fx-padding: 0;");
            } else {
                star.setStyle(
                        "-fx-background-color: transparent; -fx-font-size: 24px; -fx-text-fill: #bdc3c7; -fx-cursor: hand; -fx-padding: 0;");
            }
        }
    }

    @FXML
    private void handleSubmit() {
        boolean valid = true;
        if (currentRating == 0) {
            lblRatingError.setVisible(true);
            valid = false;
        } else {
            lblRatingError.setVisible(false);
        }

        if (txtCommentaire.getText() == null || txtCommentaire.getText().trim().isEmpty()) {
            lblCommentError.setVisible(true);
            valid = false;
        } else {
            lblCommentError.setVisible(false);
        }

        if (!valid)
            return;

        User user = StaticUser.get();
        if (existingEvaluation != null) {
            existingEvaluation.setNote(currentRating);
            existingEvaluation.setCommentaire(txtCommentaire.getText());
            existingEvaluation.setDateEvaluation(LocalDate.now());
            service.update(existingEvaluation);
        } else {
            Evaluation newEval = new Evaluation(user, ressource, currentRating, txtCommentaire.getText(),
                    LocalDate.now());
            service.create(newEval);
        }

        closeWindow();
        if (parentController != null)
            parentController.refreshList(); // Or just refresh specific card but refreshList is easier
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) txtCommentaire.getScene().getWindow();
        stage.close();
    }
}
