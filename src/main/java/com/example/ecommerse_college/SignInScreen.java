package com.example.ecommerse_college;

import java.sql.SQLException;

import com.example.ecommerse_college.Database.User;

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
         Alert a2 = new Alert(AlertType.ERROR, "Wrong username or password", new ButtonType[0]);
         a2.setTitle("Invalid");
         try {
            User user =  Database.getUserByCredentials(NameField.getText(), passwordField.getText());
            if (user != null) {
                a1.setContentText("Welcome " + user.userName);
                a1.show();
                // Switch to main screen after successful login
                MainScreen main = new MainScreen(mainApp, user);
                Scene mainScene = new Scene(main.getView(), 800, 600);
                mainApp.switchScene(mainScene);
            } else {
                a2.show();
            }
        } catch (SQLException e) {
            Alert a3 = new Alert(AlertType.ERROR, "Database error: " + e.getMessage(), new ButtonType[0]);
            a3.setTitle("Database Error");
            a3.show();
            e.printStackTrace();
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