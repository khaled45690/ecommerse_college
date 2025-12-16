package com.example.ecommerse_college;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

/**
 * Checkout screen shows selected products and total. Simple back button.
 */
public class CheckoutScreen {

    private final App mainApp;
    private final Database.User currentUser;
    private final List<Database.Product> products;
    private final BorderPane root;

    public CheckoutScreen(App mainApp, Database.User currentUser, List<Database.Product> products) {
        this.mainApp = mainApp;
        this.currentUser = currentUser;
        this.products = products;
        this.root = new BorderPane();
        initialize();
    }

    private void initialize() {
        mainApp.setTitle("Checkout");
        Label title = new Label("Checkout");
        title.setFont(new Font(18));
        HBox top = new HBox(title);
        top.setPadding(new Insets(10));
        root.setTop(top);

        VBox center = new VBox();
        center.setPadding(new Insets(10));
        center.setSpacing(8);

        BigDecimal total = BigDecimal.ZERO;
        DecimalFormat df = new DecimalFormat("0.00");
        for (Database.Product p : products) {
            Label l = new Label(p.name + " — " + df.format(p.price));
            center.getChildren().add(l);
            total = total.add(p.price);
        }

        Label totalLabel = new Label("Total: " + df.format(total));
        totalLabel.setStyle("-fx-font-weight: bold;");
        center.getChildren().add(totalLabel);

        root.setCenter(center);
        Button back = new Button("Back");
        back.setOnAction(e -> {
            MainScreen main = new MainScreen(mainApp, currentUser);
            javafx.scene.Scene mainScene = new javafx.scene.Scene(main.getView(), 800, 600);
            mainApp.switchScene(mainScene);
        });

        Button save = new Button("Save Checkout");
        save.setOnAction(e -> {
            try {
                Database.Checkout saved = Database.saveCheckout(products);
                if (saved != null) {
                    Alert a = new Alert(Alert.AlertType.INFORMATION);
                    a.setTitle("Saved");
                    a.setHeaderText("Checkout saved");
                    a.setContentText("Saved at: " + (saved.date != null ? saved.date.toString() : "(no timestamp)"));
                    a.showAndWait();
                    // return to main
                    MainScreen main = new MainScreen(mainApp, currentUser);
                    javafx.scene.Scene mainScene = new javafx.scene.Scene(main.getView(), 800, 600);
                    mainApp.switchScene(mainScene);
                } else {
                    Alert a = new Alert(Alert.AlertType.ERROR);
                    a.setTitle("Error");
                    a.setHeaderText("Save failed");
                    a.setContentText("Failed to save checkout.");
                    a.showAndWait();
                }
            } catch (Exception ex) {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setTitle("Error");
                a.setHeaderText("Save failed");
                a.setContentText(ex.getMessage());
                a.showAndWait();
            }
        });

        HBox bottom = new HBox(8, back, save);
        bottom.setPadding(new Insets(10));
        bottom.setAlignment(Pos.CENTER_RIGHT);
        root.setBottom(bottom);
    }

    public BorderPane getView() {
        return root;
    }
}
