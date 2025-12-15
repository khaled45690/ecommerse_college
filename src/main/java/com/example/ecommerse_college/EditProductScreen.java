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
 * Screen to edit or remove a product.
 */
public class EditProductScreen {

    private final App mainApp;
    private final Database.User currentUser;
    private final Database.Product product;
    private final GridPane root;

    public EditProductScreen(App mainApp, Database.User currentUser, Database.Product product) {
        this.mainApp = mainApp;
        this.currentUser = currentUser;
        this.product = product;
        this.root = new GridPane();
        initialize();
    }

    private void initialize() {
        mainApp.setTitle("Edit Product");
        root.setHgap(10);
        root.setVgap(10);
        root.setPadding(new Insets(12));
        root.setAlignment(Pos.CENTER);

        Label nameLabel = new Label("Name:");
        TextField nameField = new TextField(product.name);

        Label priceLabel = new Label("Price:");
        TextField priceField = new TextField(product.price.toPlainString());

        Button update = new Button("Update");
        Button remove = new Button("Remove");
        Button cancel = new Button("Cancel");

        root.add(nameLabel, 0, 0);
        root.add(nameField, 1, 0);
        root.add(priceLabel, 0, 1);
        root.add(priceField, 1, 1);
        root.add(update, 0, 2);
        root.add(remove, 1, 2);
        root.add(cancel, 0, 3);

        update.setOnAction(e -> {
            String name = nameField.getText();
            String priceText = priceField.getText();
            if (name == null || name.trim().isEmpty()) {
                new Alert(AlertType.WARNING, "Name required", new ButtonType[0]).show();
                return;
            }
            BigDecimal price;
            try {
                price = new BigDecimal(priceText.trim());
            } catch (Exception ex) {
                new Alert(AlertType.WARNING, "Invalid price", new ButtonType[0]).show();
                return;
            }
            try {
                boolean ok = Database.updateProduct(product.id, name.trim(), price);
                if (ok) {
                    new Alert(AlertType.INFORMATION, "Updated", new ButtonType[0]).show();
                    MainScreen main = new MainScreen(mainApp, currentUser);
                    Scene mainScene = new Scene(main.getView(), 800, 600);
                    mainApp.switchScene(mainScene);
                } else {
                    new Alert(AlertType.ERROR, "Failed to update", new ButtonType[0]).show();
                }
            } catch (SQLException ex) {
                new Alert(AlertType.ERROR, "DB error: " + ex.getMessage(), new ButtonType[0]).show();
                ex.printStackTrace();
            }
        });

        remove.setOnAction(e -> {
            try {
                boolean ok = Database.deleteProduct(product.id);
                if (ok) {
                    new Alert(AlertType.INFORMATION, "Removed", new ButtonType[0]).show();
                    MainScreen main = new MainScreen(mainApp, currentUser);
                    Scene mainScene = new Scene(main.getView(), 800, 600);
                    mainApp.switchScene(mainScene);
                } else {
                    new Alert(AlertType.ERROR, "Failed to remove", new ButtonType[0]).show();
                }
            } catch (SQLException ex) {
                new Alert(AlertType.ERROR, "DB error: " + ex.getMessage(), new ButtonType[0]).show();
                ex.printStackTrace();
            }
        });

        cancel.setOnAction(e -> {
            MainScreen main = new MainScreen(mainApp, currentUser);
            Scene mainScene = new Scene(main.getView(), 800, 600);
            mainApp.switchScene(mainScene);
        });
    }

    public GridPane getView() {
        return root;
    }
}
