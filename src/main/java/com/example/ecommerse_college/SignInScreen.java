package com.example.ecommerse_college;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;


public class SignInScreen {

    private final App mainApp;
    private final GridPane gridPane;

    public SignInScreen(App mainApp) {
        this.mainApp = mainApp;
        this.gridPane = new GridPane();
        initialize();
    }

    private void initialize() {
        gridPane.setPadding(new Insets(20, 20, 20, 20));
        gridPane.setVgap(10);
        gridPane.setHgap(10);

        Label label = new Label("Welcome to SignIn");
        GridPane.setConstraints(label, 0, 0);

        Button button = new Button("Go to Scene SignUp");
        GridPane.setConstraints(button, 0, 1);

        // Set action to switch scenes
        button.setOnAction(e -> {
            SignUpScreen signUpScreen = new SignUpScreen(mainApp);
            Scene signUpScreenScene = new Scene(signUpScreen.getView(), 400, 300);
            mainApp.switchScene(signUpScreenScene);
        });

        gridPane.getChildren().addAll(label, button);
    }

    public GridPane getView() {
        return gridPane;
    }
}