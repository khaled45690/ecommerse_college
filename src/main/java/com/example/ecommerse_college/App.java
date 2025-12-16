package com.example.ecommerse_college;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("Sign in");

        // Start with Scene 1
        SignInScreen scene1View = new SignInScreen(this);
        Scene scene1 = new Scene(scene1View.getView(), 400, 300); // Create the scene
        switchScene(scene1);
        primaryStage.show();
    }

    // Method to switch scenes
    public void switchScene(Scene newScene) {
        if (primaryStage != null) {
            // Load the application stylesheet from resources and apply to the scene
            try {
                String css = getClass().getResource("/com/example/ecommerse_college/styles.css").toExternalForm();
                newScene.getStylesheets().clear();
                newScene.getStylesheets().add(css);
            } catch (Exception e) {
                // If stylesheet is missing, continue without failing
                System.err.println("styles.css not found: " + e.getMessage());
            }
            primaryStage.setScene(newScene);
        }
    }
    // Method to switch scenes
    public void setTitle(String title) {
            primaryStage.setTitle(title);
    }

    public static void main(String[] args) {
        launch(args);
    }

}