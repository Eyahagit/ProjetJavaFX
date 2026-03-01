package Esprit.tn;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.sql.Connection;
import utils.mydb;              // ← AJOUTEZ CETTE LIGNE
import Services.EvenementControlleur; // ← AJOUTEZ CETTE LIGNE

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage.setTitle("Connexion - GrowMind");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Driver MySQL chargé !");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Connection cnx = mydb.getInstance().getConnection();
        EvenementControlleur ec = new EvenementControlleur(cnx); // ← Utilisé plus bas

        launch(args);
    }
}