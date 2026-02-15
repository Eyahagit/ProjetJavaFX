package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialize DB and Static User
        Main.initDatabaseAndStaticUser();

        // Load Admin Dashboard by default
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/userressource.fxml")));
        primaryStage.setTitle("GrowMind - Admin Dashboard");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
