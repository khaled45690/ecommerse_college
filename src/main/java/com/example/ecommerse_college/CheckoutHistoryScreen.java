package com.example.ecommerse_college;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.math.BigDecimal;

/**
 * Screen to display all saved checkouts from the `checkouts` table.
 */
public class CheckoutHistoryScreen {

    private final App mainApp;
    private final Database.User currentUser;
    private final BorderPane root;

    public CheckoutHistoryScreen(App mainApp, Database.User currentUser) {
        this.mainApp = mainApp;
        this.currentUser = currentUser;
        this.root = new BorderPane();
        initialize();
    }

    private void initialize() {
        mainApp.setTitle("Checkout History");
        Label title = new Label("Checkout History");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        HBox top = new HBox(title);
        top.setPadding(new Insets(10));
        root.setTop(top);

        VBox list = new VBox(8);
        list.setPadding(new Insets(10));

        try {
            List<Database.Checkout> rows = Database.getAllCheckouts();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            if (rows.isEmpty()) {
                list.getChildren().add(new Label("No checkouts saved."));
            } else {
                for (Database.Checkout c : rows) {
                    VBox item = new VBox(6);
                    item.setPadding(new Insets(8));
                    item.setStyle("-fx-border-color: #ddd; -fx-border-radius: 4; -fx-background-color: #fafafa;");
                    Label id = new Label("#" + c.id + " — " + (c.date != null ? c.date.format(fmt) : "(no date)"));
                    id.setStyle("-fx-font-weight: bold;");
                    item.getChildren().add(id);

                    // Parse products JSON into simple entries
                    List<ProductRow> products = parseProductsJson(c.productsJson);
                    VBox productsBox = new VBox(4);
                    BigDecimal total = BigDecimal.ZERO;
                    for (ProductRow pr : products) {
                        HBox row = new HBox(8);
                        Label nameLbl = new Label(pr.name);
                        nameLbl.setMaxWidth(Double.MAX_VALUE);
                        HBox.setHgrow(nameLbl, Priority.ALWAYS);
                        Label priceLbl = new Label(pr.price.toPlainString());
                        priceLbl.setStyle("-fx-font-weight: bold;");
                        row.getChildren().addAll(nameLbl, priceLbl);
                        productsBox.getChildren().add(row);
                        total = total.add(pr.price);
                    }

                    if (products.isEmpty()) {
                        Label raw = new Label(c.productsJson == null ? "" : c.productsJson);
                        raw.setWrapText(true);
                        item.getChildren().add(raw);
                    } else {
                        item.getChildren().add(productsBox);
                        Label totalLbl = new Label("Total: " + total.toPlainString());
                        totalLbl.setStyle("-fx-font-weight: bold;");
                        item.getChildren().add(totalLbl);
                    }

                    list.getChildren().add(item);
                }
            }
        } catch (Exception e) {
            list.getChildren().add(new Label("Failed to load checkouts: " + e.getMessage()));
        }

        ScrollPane sp = new ScrollPane(list);
        sp.setFitToWidth(true);
        root.setCenter(sp);

        Button back = new Button("Back");
        back.setOnAction(e -> {
            MainScreen main = new MainScreen(mainApp, currentUser);
            javafx.scene.Scene scene = new javafx.scene.Scene(main.getView(), 800, 600);
            mainApp.switchScene(scene);
        });
        HBox bottom = new HBox(back);
        bottom.setPadding(new Insets(10));
        bottom.setAlignment(Pos.CENTER_RIGHT);
        root.setBottom(bottom);
    }

    public BorderPane getView() {
        return root;
    }

    // Simple product row holder used for rendering parsed JSON
    private static class ProductRow {
        final int id;
        final String name;
        final BigDecimal price;

        ProductRow(int id, String name, BigDecimal price) {
            this.id = id;
            this.name = name;
            this.price = price;
        }
    }

    // Manual JSON parser that expects the simple JSON array produced by saveCheckout()
    private static List<ProductRow> parseProductsJson(String json) {
        List<ProductRow> out = new ArrayList<>();
        if (json == null) return out;
        int len = json.length();
        int i = 0;
        // skip whitespace
        i = skipWhitespace(json, i);
        if (i >= len || json.charAt(i) != '[') return out;
        i++; // skip '['
        i = skipWhitespace(json, i);

        while (i < len) {
            i = skipWhitespace(json, i);
            if (i < len && json.charAt(i) == ']') {
                // end of array
                return out;
            }
            if (i >= len || json.charAt(i) != '{') break; // unexpected
            // parse object
            i++; // skip '{'
            int id = -1;
            String name = "";
            String priceStr = "";

            while (i < len) {
                i = skipWhitespace(json, i);
                if (i < len && json.charAt(i) == '}') { i++; break; }
                // parse key string
                if (i >= len || json.charAt(i) != '"') { // invalid, try to recover
                    // skip until next quote or end
                    while (i < len && json.charAt(i) != '"' && json.charAt(i) != '}') i++;
                    if (i >= len || json.charAt(i) == '}') { if (i < len && json.charAt(i) == '}') i++; break; }
                }
                String key = parseQuotedString(json, i);
                if (key == null) break;
                // advance i past the quoted string
                i = nextAfterQuoted(json, i);
                i = skipWhitespace(json, i);
                if (i < len && json.charAt(i) == ':') i++; else break;
                i = skipWhitespace(json, i);
                // parse value
                if (i < len && json.charAt(i) == '"') {
                    String val = parseQuotedString(json, i);
                    if (val == null) val = "";
                    if ("name".equals(key)) name = val;
                    else if ("price".equals(key)) priceStr = val;
                    i = nextAfterQuoted(json, i);
                } else {
                    // parse unquoted token (number)
                    int start = i;
                    while (i < len && ",} \t\n\r".indexOf(json.charAt(i)) == -1) i++;
                    String token = json.substring(start, i).trim();
                    if ("id".equals(key)) {
                        try { id = Integer.parseInt(token); } catch (NumberFormatException ignored) { id = -1; }
                    } else if ("price".equals(key)) {
                        priceStr = token;
                    }
                }
                i = skipWhitespace(json, i);
                if (i < len && json.charAt(i) == ',') { i++; continue; }
            }

            BigDecimal price = BigDecimal.ZERO;
            try {
                if (priceStr != null && !priceStr.isEmpty()) price = new BigDecimal(priceStr);
            } catch (NumberFormatException ignored) {
                price = BigDecimal.ZERO;
            }
            out.add(new ProductRow(id, name == null ? "" : name, price));

            i = skipWhitespace(json, i);
            if (i < len && json.charAt(i) == ',') { i++; continue; }
            if (i < len && json.charAt(i) == ']') break;
        }
        return out;
    }

    private static int skipWhitespace(String s, int i) {
        int n = s.length();
        while (i < n) {
            char c = s.charAt(i);
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') i++; else break;
        }
        return i;
    }

    // Parse a quoted JSON string beginning at index i (pointing at the opening quote).
    // Returns the unescaped string, or null on error.
    private static String parseQuotedString(String s, int i) {
        int n = s.length();
        if (i >= n || s.charAt(i) != '"') return null;
        StringBuilder sb = new StringBuilder();
        i++; // skip '"'
        while (i < n) {
            char c = s.charAt(i);
            if (c == '"') { return sb.toString(); }
            if (c == '\\' && i + 1 < n) {
                char nx = s.charAt(i + 1);
                switch (nx) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/': sb.append('/'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    case 'u':
                      
                        if (i + 5 < n) {
                            String hex = s.substring(i + 2, i + 6);
                            try { int code = Integer.parseInt(hex, 16); sb.append((char) code); i += 5; break; } catch (NumberFormatException ex) { sb.append('?'); i += 5; break; }
                        } else { sb.append('?'); i = n; break; }
                    default:
                        sb.append(nx); break;
                }
                i += 2; continue;
            } else {
                sb.append(c);
                i++;
            }
        }
        return null; // unterminated string
    }

    // Return index just after the closing quote starting at i
    private static int nextAfterQuoted(String s, int i) {
        int n = s.length();
        if (i >= n || s.charAt(i) != '"') return i;
        i++;
        while (i < n) {
            char c = s.charAt(i);
            if (c == '"') return i + 1;
            if (c == '\\') i += 2; else i++;
        }
        return i;
    }
}
