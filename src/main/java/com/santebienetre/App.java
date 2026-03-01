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

        Scene scene = new Scene(root, 1200, 900);
        stage.setTitle("🌿 GrowMind - Santé & Bien-être");
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(800);
        
        // Forcer l'affichage au premier plan
        stage.setIconified(false);
        stage.toFront();
        stage.show();
        stage.requestFocus();
        
        System.out.println("✅ GrowMind - Santé & Bien-être lancé avec succès!");
        System.out.println("🌿 Design moderne avec logo et navigation activés");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
