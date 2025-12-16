package com.example.ecommerse_college;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

import com.example.ecommerse_college.Database.Product;
import com.example.ecommerse_college.Database.User;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;

/**
 * MainScreen: shows products in a simple card layout. If logged-in user is admin,
 * shows Add button and Edit buttons on cards.
 */
public class MainScreen {

    private final App mainApp;
    private final User currentUser;
    private final BorderPane root;
    private final Set<Integer> selectedIds = new HashSet<>();
    private final Map<Integer, Node> cardNodes = new HashMap<>();
    private final Map<Integer, Product> productMap = new HashMap<>();
    private Button checkoutBtn;

    public MainScreen(App mainApp, User currentUser) {
        this.mainApp = mainApp;
        this.currentUser = currentUser;
        this.root = new BorderPane();
        initialize();
    }

    private void initialize() {
        // Top bar with title and optional Add button
        mainApp.setTitle("Products");
        HBox top = new HBox();
        top.setPadding(new Insets(10));
        top.setSpacing(10);
        top.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Products");
        title.getStyleClass().add("title");
        top.getChildren().add(title);
        // Checkout button always available, enabled when selection not empty
        checkoutBtn = new Button("Checkout");
        checkoutBtn.setDisable(true);
        checkoutBtn.setOnAction(e -> onCheckout());
        // spacer
        HBox spacer = new HBox();
        spacer.setPrefWidth(20);
        top.getChildren().add(spacer);

        if (currentUser != null && currentUser.isAdmin) {
            Button addBtn = new Button("Add Product");
            addBtn.setOnAction(e -> onAddProduct());
            addBtn.getStyleClass().add("add-button");
            top.getChildren().add(addBtn);
        }

        top.getChildren().add(checkoutBtn);

        // Checkout history button (always available)
        Button historyBtn = new Button("Checkout History");
        historyBtn.setOnAction(e -> onCheckoutHistory());
        top.getChildren().add(historyBtn);

        root.setTop(top);

        // Center: either "no products" or a flow of product cards
        refreshCenter();
    }

    private void refreshCenter() {
        // clear previous selection and nodes
        selectedIds.clear();
        cardNodes.clear();
        productMap.clear();

        List<Product> products;
        try {
            products = Database.getAllProducts();
        } catch (Exception e) {
            Label err = new Label("Failed to load products: " + e.getMessage());
            err.setWrapText(true);
            root.setCenter(err);
            return;
        }

        if (products.isEmpty()) {
            Label none = new Label("No products available");
            none.setPadding(new Insets(20));
            root.setCenter(none);
            return;
        }

        FlowPane flow = new FlowPane();
        flow.setPadding(new Insets(10));
        flow.setHgap(10);
        flow.setVgap(10);

        for (Product p : products) {
            Node card = createProductCard(p);
            // track node for toggling selection style
            cardNodes.put(p.id, card);
            productMap.put(p.id, p);
            flow.getChildren().add(card);
        }

        ScrollPane sp = new ScrollPane(flow);
        sp.setFitToWidth(true);
        root.setCenter(sp);
    }

    private Node createProductCard(Product p) {
        VBox card = new VBox();
        card.setPadding(new Insets(8));
        card.setSpacing(6);
        card.getStyleClass().add("product-card");
        card.setPrefWidth(160);

        Label name = new Label(p.name);
        name.getStyleClass().add("product-name");
        DecimalFormat df = new DecimalFormat("0.00");
        df.setRoundingMode(RoundingMode.HALF_UP);
        Label price = new Label(df.format(p.price));
        price.getStyleClass().add("product-price");

        card.getChildren().addAll(name, price);

        // Make the whole card clickable: toggle selection and call onProductClicked
        card.addEventHandler(MouseEvent.MOUSE_CLICKED, ev -> {
            // toggle selection in set
            if (selectedIds.contains(p.id)) {
                selectedIds.remove(p.id);
                card.getStyleClass().remove("selected");
            } else {
                selectedIds.add(p.id);
                if (!card.getStyleClass().contains("selected")) card.getStyleClass().add("selected");
            }
            // enable/disable checkout button based on selection
            if (checkoutBtn != null) checkoutBtn.setDisable(selectedIds.isEmpty());
            onProductClicked(p);
        });

        // If admin, show Edit button under the card
        if (currentUser != null && currentUser.isAdmin) {
            Button edit = new Button("Edit");
            edit.getStyleClass().add("edit-button");
            edit.setOnAction(e -> {
                // open edit screen
                EditProductScreen editScreen = new EditProductScreen(mainApp, currentUser, p);
                javafx.scene.Scene editScene = new javafx.scene.Scene(editScreen.getView(), 400, 250);
                mainApp.switchScene(editScene);
            });
            HBox hb = new HBox(edit);
            hb.setAlignment(Pos.CENTER_RIGHT);
            card.getChildren().add(hb);
        }

        return card;
    }

    // Placeholder: called when a product card is clicked
    private void onProductClicked(Product p) {
        System.out.println("Product clicked: " + p);
        // Later: open product details or select for cart
    }


    // Placeholder: called when Add pressed for admin
    private void onAddProduct() {
        // Open the Add Product screen
        AddProductScreen add = new AddProductScreen(mainApp, currentUser);
        javafx.scene.Scene addScene = new javafx.scene.Scene(add.getView(), 400, 250);
        mainApp.switchScene(addScene);
    }

    // Called when Checkout pressed: collect selected products and open CheckoutScreen
    private void onCheckout() {
        List<Product> selected = new ArrayList<>();
        for (Integer id : selectedIds) {
            Product p = productMap.get(id);
            if (p != null) selected.add(p);
        }
        CheckoutScreen cs = new CheckoutScreen(mainApp, currentUser, selected);
        javafx.scene.Scene scene = new javafx.scene.Scene(cs.getView(), 800, 600);
        mainApp.switchScene(scene);
    }

    // Navigate to checkout history screen
    private void onCheckoutHistory() {
        try {
            CheckoutHistoryScreen ch = new CheckoutHistoryScreen(mainApp, currentUser);
            javafx.scene.Scene s = new javafx.scene.Scene(ch.getView(), 800, 600);
            mainApp.switchScene(s);
        } catch (Exception e) {
            Label err = new Label("Failed to open checkout history: " + e.getMessage());
            err.setPadding(new Insets(10));
            root.setCenter(err);
        }
    }

    public BorderPane getView() {
        return root;
    }
}
