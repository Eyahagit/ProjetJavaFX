package services;

import Modele.Utilisateur;
import utiles.mydb;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.*;

public class LoginController {

    @FXML private TextField txtEmail;
    @FXML private PasswordField txtMotDePasse;
    @FXML private Label lblMessage;

    @FXML
    private void handleConnexion() {
        String email = txtEmail.getText().trim();
        String mdp = txtMotDePasse.getText().trim();

        if (email.isEmpty() || mdp.isEmpty()) {
            lblMessage.setText("Email et mot de passe requis");
            return;
        }

        try {
            Connection cnx = mydb.getInstance().getConnection();
            PreparedStatement ps = cnx.prepareStatement(
                    "SELECT * FROM utilisateur WHERE email = ? AND mot_de_passe = ?"
            );
            ps.setString(1, email);
            ps.setString(2, mdp);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Utilisateur user = new Utilisateur();
                user.setId(rs.getInt("id"));
                user.setNom(rs.getString("nom"));
                user.setEmail(rs.getString("email"));

                UserSession.setUser(user);

                // Ouvrir la fenêtre principale
                Stage stage = new Stage();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainView.fxml"));
                stage.setScene(new Scene(loader.load()));
                stage.setTitle("Gestion Événements - " + user.getNom());
                stage.show();

                // Fermer la fenêtre de connexion
                ((Stage) txtEmail.getScene().getWindow()).close();

            } else {
                lblMessage.setText("Email ou mot de passe incorrect");
            }

        } catch (Exception e) {
            e.printStackTrace();
            lblMessage.setText("Erreur de connexion");
        }
    }

    @FXML
    private void handleQuitter() {
        System.exit(0);
    }
}