package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import Service.ServiceCabinet;   // Ton service
import java.sql.Connection;
import utils.MyDatabase;              // Singleton pour la DB
import utils.MyDatabase;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Charger l'interface Login.fxml
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MenuPrincipal.fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage.setTitle("Connexion - GrowMind");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        /*try {
            // Charger le driver MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Driver MySQL chargé !");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // Connexion à la base de données
        Connection connection = MyDatabase.getInstance().getConnection();

        // Création du service (prêt à être utilisé plus tard dans l'app)
        ServiceCabinet sc = new ServiceCabinet(connection);*/

        // Lancement de l'application JavaFX
        launch(args);
    }
}
