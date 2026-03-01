package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.controlsfx.control.Rating;
import Models.Evaluation;
import Models.Ressource;
import Models.users;
import Services.evaluationService;
import utils.StaticUser;

import java.time.LocalDate;

public class EvaluationModalController {

    @FXML
    private Label lblResourceTitle;
    @FXML
    private Rating ratingControl;
    @FXML
    private TextArea txtCommentaire;
    @FXML
    private Label lblRatingError;
    @FXML
    private Label lblCommentError;

    @FXML
    private Button btnSubmit;
    @FXML
    private Button btnCancel;

    private Ressource ressource;
    private Evaluation existingEvaluation;
    private final evaluationService service = new evaluationService();
    private UserRessourceController parentController;

    // Utilisateur connecté
    private users currentUser;

    @FXML
    public void initialize() {
        applyIcon(btnSubmit, "submit.png", "Soumettre", 20);
        applyIcon(btnCancel, "cancel.png", "Annuler", 20);
        if (ratingControl != null) {
            ratingControl.setMax(5);
            ratingControl.setRating(0);
        }
    }

    /**
     * Définit l'utilisateur connecté
     */
    public void setCurrentUser(users user) {
        this.currentUser = user;
    }

    public void setRessource(Ressource r, UserRessourceController parent) {
        this.ressource = r;
        this.parentController = parent;
        lblResourceTitle.setText(r.getTitle());
    }

    public void setExistingEvaluation(Evaluation e) {
        this.existingEvaluation = e;
        if (e != null) {
            if (ratingControl != null)
                ratingControl.setRating(e.getNote());
            txtCommentaire.setText(e.getCommentaire());
        }
    }

    @FXML
    private void handleSubmit() {
        boolean valid = true;
        int note = ratingControl != null ? (int) Math.round(ratingControl.getRating()) : 0;
        if (note < 1 || note > 5) {
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

        int userId = StaticUser.isSet() ? StaticUser.getId() : 0;
        if (existingEvaluation != null) {
            existingEvaluation.setNote(note);
            existingEvaluation.setCommentaire(txtCommentaire.getText().trim());
            existingEvaluation.setDateEvaluation(LocalDate.now());
            service.update(existingEvaluation);
        } else {
            Evaluation newEval = new Evaluation(userId, ressource, note, txtCommentaire.getText().trim(),
                    LocalDate.now());
            service.create(newEval);
        }

        closeWindow();
        if (parentController != null)
            parentController.refreshList();
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) txtCommentaire.getScene().getWindow();
        stage.close();
    }

    private void applyIcon(Button btn, String iconName, String tooltipText, double size) {
        if (btn == null)
            return;
        for (String path : new String[] { "/icons/" + iconName, "/" + iconName }) {
            try {
                java.io.InputStream is = getClass().getResourceAsStream(path);
                if (is == null)
                    continue;
                Image img = new Image(is);
                if (img.isError())
                    continue;
                ImageView iv = new ImageView(img);
                iv.setFitWidth(size);
                iv.setFitHeight(size);
                iv.setPreserveRatio(true);
                iv.setSmooth(true);
                btn.setGraphic(iv);
                btn.setText(null);
                btn.setTooltip(new Tooltip(tooltipText));
                btn.setContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);
                btn.setMinWidth(44);
                btn.setMaxWidth(44);
                btn.setMinHeight(44);
                btn.setMaxHeight(44);
                return;
            } catch (Exception ignored) {
            }
        }
    }
}