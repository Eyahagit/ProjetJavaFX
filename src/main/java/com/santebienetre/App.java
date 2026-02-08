package com.santebienetre;

import com.santebienetre.util.DatabaseInit;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * Main Application class for Application Santé & Bien-être.
 * Initializes the database and launches the JavaFX UI.
 * 
 * @author Application Santé & Bien-être
 * @version 1.0
 */
public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        DatabaseInit.initialize();

        Parent root = FXMLLoader.load(Objects.requireNonNull(
                getClass().getResource("/fxml/GestionSanteBienEtre.fxml")));

        Scene scene = new Scene(root, 900, 750);
        stage.setTitle("Application Santé & Bien-être - Gestion Santé & Bien-être");
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
