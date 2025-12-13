package com.example.ecommerse_college;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
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
      mainApp.setTitle("Sign In");
      Label Name_1 = new Label("User Name");
      Label password_1 = new Label("Password");
      TextField NameField = new TextField();
      PasswordField passwordField = new PasswordField();
      NameField.setPromptText("Enter your name");
      passwordField.setPromptText("Enter your password");
      Button signin = new Button("Sign in");
      Button signup = new Button("Sign up");
      gridPane.add(Name_1, 0, 0);
      gridPane.add(NameField, 1, 0);
      gridPane.add(password_1, 0, 1);
      gridPane.add(passwordField, 1, 1);
      gridPane.add(signin, 0, 2);
      gridPane.add(signup, 1, 2);
      gridPane.setVgap(10.0);
      gridPane.setHgap(10.0);
      gridPane.setAlignment(Pos.CENTER);
      signin.setOnAction((event) -> {
         Alert a1 = new Alert(AlertType.INFORMATION, "Welcome ", new ButtonType[0]);
         a1.setTitle("Signed In");
         Alert a2 = new Alert(AlertType.ERROR, "Wrong email or password", new ButtonType[0]);
         a2.setTitle("Invalid");
         if (NameField.getText().equals("jou") && passwordField.getText().equals("123")) {
            a1.show();
            GridPane g3 = new GridPane();
            g3.setAlignment(Pos.CENTER);
            g3.setVgap(10.0);
            g3.setHgap(10.0);
            // stage.setScene(sc3);
            // stage.setTitle("Sign in");
            // stage.show();
         } else {
            a2.show();
         }

      });
        // // Set action to switch scenes
        signup.setOnAction(e -> {
            SignUpScreen signUpScreen = new SignUpScreen(mainApp);
            Scene signUpScreenScene = new Scene(signUpScreen.getView(), 400, 300);
            mainApp.switchScene(signUpScreenScene);
        });

        // gridPane.getChildren().addAll(label, button);
    }

    public GridPane getView() {
        return gridPane;
    }
}