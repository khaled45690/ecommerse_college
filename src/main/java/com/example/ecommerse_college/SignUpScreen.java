package com.example.ecommerse_college;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class SignUpScreen {
    private final App mainApp;
    private final GridPane gridPane;

    public SignUpScreen(App mainApp) {
                this.mainApp = mainApp;
        this.gridPane = new GridPane();
        initialize();
    }

    private void initialize() {
        mainApp.setTitle("Sign UP");
         Label name = new Label("User Name : ");
         Label password_2 = new Label("Password : ");
         TextField nameField = new TextField();
         PasswordField passwordField_2 = new PasswordField();
         ToggleGroup tg = new ToggleGroup();
         RadioButton r1 = new RadioButton("Admin");
         r1.setToggleGroup(tg);
         RadioButton r2 = new RadioButton("User");
         r2.setToggleGroup(tg);
         CheckBox cb = new CheckBox("I agree -----");
         Button sign_up = new Button("Sign Up");
         Button sign_in = new Button("Go back to sign in");
         sign_up.setOnAction((e2) -> {
            Alert alert2;
            if (!cb.isSelected()) {
               alert2 = new Alert(AlertType.WARNING, "You must agree to proceed.", new ButtonType[0]);
               alert2.show();
            } else {
               alert2 = new Alert(AlertType.INFORMATION, "You Signed Up", new ButtonType[0]);
               alert2.show();
            //    stage.show();
            }
         });
         gridPane.add(name, 0, 0);
         gridPane.add(password_2, 0, 2);
         gridPane.add(r1, 0, 3);
         gridPane.add(cb, 0, 5);
         gridPane.add(nameField, 1, 0);
         gridPane.add(passwordField_2, 1, 2);
         gridPane.add(r2, 1, 3);
         gridPane.add(sign_up, 0, 6);
         gridPane.add(sign_in, 1, 6);
         gridPane.setHgap(10.0);
         gridPane.setVgap(10.0);
         gridPane.setAlignment(Pos.CENTER);
        // Set action to switch scenes
        sign_in.setOnAction(e -> {
            SignInScreen signInScreen = new SignInScreen(mainApp);
            Scene signInScreenScene = new Scene(signInScreen.getView(), 400, 300);
            mainApp.switchScene(signInScreenScene);
        });

        // gridPane.getChildren().addAll(label, button);
    }


       public GridPane getView() {
        return gridPane;
    }
}