package com.example.ecommerse_college;

import java.math.BigDecimal;
import java.sql.SQLException;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/**
 * Simple Add Product screen: name + price + submit. Uses Database.addProduct(...)
 */
public class AddProductScreen {

    private final App mainApp;
    private final Database.User currentUser;
    private final GridPane root;

    public AddProductScreen(App mainApp, Database.User currentUser) {
        this.mainApp = mainApp;
        this.currentUser = currentUser;
        this.root = new GridPane();
        initialize();
    }

    private void initialize() {
        mainApp.setTitle("Add Product");
        root.setHgap(10);
        root.setVgap(10);
        root.setPadding(new Insets(12));
        root.setAlignment(Pos.CENTER);

        Label nameLabel = new Label("Name:");
        TextField nameField = new TextField();
        nameField.setPromptText("Product name");

        Label priceLabel = new Label("Price:");
        TextField priceField = new TextField();
        priceField.setPromptText("e.g. 9.99");

        Button submit = new Button("Add");
        Button cancel = new Button("Cancel");

        root.add(nameLabel, 0, 0);
        root.add(nameField, 1, 0);
        root.add(priceLabel, 0, 1);
        root.add(priceField, 1, 1);
        root.add(submit, 0, 2);
        root.add(cancel, 1, 2);

        submit.setOnAction(e -> {
            String name = nameField.getText();
            String priceText = priceField.getText();

            if (name == null || name.trim().isEmpty()) {
                Alert a = new Alert(AlertType.WARNING, "Name is required", new ButtonType[0]);
                a.show();
                return;
            }
            BigDecimal price;
            try {
                price = new BigDecimal(priceText.trim());
            } catch (Exception ex) {
                Alert a = new Alert(AlertType.WARNING, "Invalid price format", new ButtonType[0]);
                a.show();
                return;
            }

            try {
                boolean ok = Database.addProduct(name.trim(), price);
                if (ok) {
                    Alert s = new Alert(AlertType.INFORMATION, "Product added", new ButtonType[0]);
                    s.show();
                    // go back to main screen and refresh
                    MainScreen main = new MainScreen(mainApp, currentUser);
                    Scene mainScene = new Scene(main.getView(), 800, 600);
                    mainApp.switchScene(mainScene);
                } else {
                    Alert f = new Alert(AlertType.ERROR, "Failed to add product", new ButtonType[0]);
                    f.show();
                }
            } catch (SQLException ex) {
                Alert err = new Alert(AlertType.ERROR, "Database error: " + ex.getMessage(), new ButtonType[0]);
                err.show();
                ex.printStackTrace();
            }
        });

        cancel.setOnAction(e -> {
            // go back to main screen without changes
            MainScreen main = new MainScreen(mainApp, currentUser);
            Scene mainScene = new Scene(main.getView(), 800, 600);
            mainApp.switchScene(mainScene);
        });
    }

    public GridPane getView() {
        return root;
    }
}
