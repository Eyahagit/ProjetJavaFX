package controllers;

import entities.Reponse;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.format.DateTimeFormatter;

public class ReponseDetailsController {

    @FXML private Label lblAuteur;
    @FXML private Label lblDate;
    @FXML private Label lblIdPost;
    @FXML private TextArea txtContenu;
    @FXML private Button btnFermer;

    private Reponse reponse;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @FXML
    public void initialize() {
        txtContenu.setEditable(false);
    }

    public void setReponse(Reponse reponse) {
        this.reponse = reponse;
        afficherDetails();
    }

    private void afficherDetails() {
        lblAuteur.setText(reponse.getAuteur());
        lblDate.setText(reponse.getDateReponse().format(formatter));
        lblIdPost.setText("Publication #" + reponse.getIdPost());
        txtContenu.setText(reponse.getContenu());
    }

    @FXML
    private void handleFermer() {
        ((Stage) btnFermer.getScene().getWindow()).close();
    }
}