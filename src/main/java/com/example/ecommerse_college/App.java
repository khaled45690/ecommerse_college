package com.example.ecommerse_college;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("Sign in");

        // Start with Scene 1
        SignInScreen scene1View = new SignInScreen(this);
        Scene scene1 = new Scene(scene1View.getView(), 400, 300); // Create the scene
        primaryStage.setScene(scene1);
        primaryStage.show();
    }

    // Method to switch scenes
    public void switchScene(Scene newScene) {
        if (primaryStage != null) {
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