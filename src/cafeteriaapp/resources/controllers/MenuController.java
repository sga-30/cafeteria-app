package cafeteriaapp.resources.controllers;

import cafeteriaapp.CartManager;
import cafeteriaapp.DatabaseConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MenuController implements Initializable {

    @FXML private VBox menuListContainer;
    @FXML private TextField searchField;

    private final List<MenuItem> allItems = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        new Thread(() -> {
            loadFromDatabase();
        }).start();

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String q = newVal == null ? "" : newVal.trim().toLowerCase();
            List<MenuItem> filtered = q.isEmpty() ? allItems
                    : allItems.stream()
                              .filter(i -> i.getName().toLowerCase().contains(q)
                                       || i.getCategory().toLowerCase().contains(q))
                              .toList();
            renderItems(filtered);
        });
    }

    private void loadFromDatabase() {
        try (Connection conn = DatabaseConnection.getConnect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT id, name, category, quantity, price FROM menu_items ORDER BY category, name")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                allItems.add(new MenuItem(rs.getInt("id"), rs.getString("name"),
                        rs.getString("category"), rs.getInt("quantity"), rs.getDouble("price")));
            }
            renderItems(allItems);
        } catch (SQLException ex) {
            Logger.getLogger(MenuController.class.getName()).log(Level.SEVERE, null, ex);
            Platform.runLater(() -> {
                Label err = new Label("Could not load menu. Check DB connection.");
                err.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
                menuListContainer.getChildren().add(err);
            });
        }
    }

    private void renderItems(List<MenuItem> items) {
        Platform.runLater(() -> {
            menuListContainer.getChildren().clear();
            if (items.isEmpty()) {
                Label empty = new Label("No items match your search.");
                empty.getStyleClass().add("empty-label");
                empty.setMaxWidth(Double.MAX_VALUE);
                empty.setAlignment(Pos.CENTER);
                menuListContainer.getChildren().add(empty);
                return;
            }
            String lastCat = null;
            for (MenuItem item : items) {
                if (!item.getCategory().equals(lastCat)) {
                    Label cat = new Label(item.getCategory());
                    cat.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e; -fx-padding: 14 0 4 4;");
                    menuListContainer.getChildren().add(cat);
                    lastCat = item.getCategory();
                }
                menuListContainer.getChildren().add(buildCard(item));
            }
        });
    }

    private VBox buildCard(MenuItem item) {
        VBox wrapper = new VBox();
        wrapper.getStyleClass().add("menu-item-wrapper");

        HBox card = new HBox(14);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 12; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 6, 0, 0, 2);");

        StackPane imageBox = new StackPane();
        imageBox.getStyleClass().add("item-image-container");
        imageBox.setMinSize(64, 64); imageBox.setMaxSize(64, 64);
        Label emoji = new Label(getCategoryEmoji(item.getCategory()));
        emoji.setStyle("-fx-font-size: 26px;");
        imageBox.getChildren().add(emoji);

        VBox info = new VBox(4);
        info.getStyleClass().add("item-info");
        HBox.setHgrow(info, Priority.ALWAYS);
        Label name = new Label(item.getName());
        name.getStyleClass().add("item-name");
        name.setWrapText(true); name.setMaxWidth(200);
        Label qty = new Label("In stock: " + item.getQuantity());
        qty.getStyleClass().add("item-quantity");
        info.getChildren().addAll(name, qty);

        VBox right = new VBox(6);
        right.setAlignment(Pos.CENTER_RIGHT);
        Label price = new Label(String.format("\u20a6%,.2f", item.getPrice()));
        price.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");

        Button addBtn = new Button("Add  +");
        addBtn.getStyleClass().add("add-btn");
        if (item.getQuantity() <= 0) {
            addBtn.setDisable(true); addBtn.setText("Out of Stock");
        } else {
            addBtn.setOnAction(e -> {
                CartManager.getInstance().addItem(item.getId(), item.getName(), item.getPrice());
                addBtn.setText("Added \u2713");
                addBtn.setStyle("-fx-background-color:#28a745;-fx-border-color:#28a745;-fx-text-fill:white;"
                        + "-fx-border-radius:20;-fx-background-radius:20;-fx-font-size:13px;"
                        + "-fx-font-weight:bold;-fx-padding:7 18 7 18;-fx-cursor:hand;-fx-min-width:90;");
                new Thread(() -> {
                    try { Thread.sleep(900); } catch (InterruptedException ignored) {}
                    Platform.runLater(() -> {
                        addBtn.setText("Add  +");
                        addBtn.setStyle("");
                        addBtn.getStyleClass().setAll("add-btn");
                    });
                }).start();
            });
        }
        right.getChildren().addAll(price, addBtn);
        card.getChildren().addAll(imageBox, info, right);
        wrapper.getChildren().add(card);
        return wrapper;
    }

    private String getCategoryEmoji(String cat) {
        return switch (cat) {
            case "Drinks" -> "\uD83E\uDD64";
            case "Main Meal" -> "\uD83C\uDF5B";
            case "Swallow" -> "\uD83E\uDEB5";
            case "Protein" -> "\uD83C\uDF57";
            case "Snacks" -> "\uD83E\uDD50";
            default -> "\uD83C\uDF7D";
        };
    }

    public static class MenuItem {
        private final int id;
        private final String name, category;
        private final int quantity;
        private final double price;
        public MenuItem(int id, String name, String category, int quantity, double price) {
            this.id=id; this.name=name; this.category=category; this.quantity=quantity; this.price=price;
        }
        public int getId() { return id; }
        public String getName() { return name; }
        public String getCategory() { return category; }
        public int getQuantity() { return quantity; }
        public double getPrice() { return price; }
    }
}
