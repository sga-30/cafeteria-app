package cafeteriaapp.resources.controllers;

import cafeteriaapp.CartItem;
import cafeteriaapp.CartManager;
import cafeteriaapp.DatabaseConnection;
import cafeteriaapp.Session;
import cafeteriaapp.User;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class CartController implements Initializable {

    @FXML private VBox cartItemsContainer;
    @FXML private Label totalLabel;
    @FXML private Label emptyLabel;
    @FXML private Button checkoutBtn;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        renderCart();
        CartManager.getInstance().getItems().addListener((ListChangeListener<CartItem>) c -> renderCart());
    }

    private void renderCart() {
        Platform.runLater(() -> {
            cartItemsContainer.getChildren().clear();
            var items = CartManager.getInstance().getItems();
            if (items.isEmpty()) {
                emptyLabel.setVisible(true);
                checkoutBtn.setDisable(true);
                totalLabel.setText("Total: ₦0.00");
            } else {
                emptyLabel.setVisible(false);
                checkoutBtn.setDisable(false);
                for (CartItem item : items) {
                    cartItemsContainer.getChildren().add(buildRow(item));
                }
                totalLabel.setText(String.format("Total: \u20a6%,.2f", CartManager.getInstance().getTotal()));
            }
        });
    }

    private HBox buildRow(CartItem item) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 10;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 4, 0, 0, 1);");

        Label name = new Label(item.getName());
        name.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        HBox.setHgrow(name, Priority.ALWAYS);
        name.setWrapText(true); name.setMaxWidth(200);

        Button minus = new Button("-");
        minus.setStyle("-fx-background-color: #e2e8f0; -fx-border-radius: 20; -fx-background-radius: 20;"
                + "-fx-font-weight: bold; -fx-cursor: hand; -fx-min-width: 28; -fx-min-height: 28;");

        Label qtyLabel = new Label(String.valueOf(item.getQuantity()));
        qtyLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-min-width: 24; -fx-alignment: center;");
        item.quantityProperty().addListener((obs, ov, nv) -> {
            qtyLabel.setText(nv.toString());
            totalLabel.setText(String.format("Total: \u20a6%,.2f", CartManager.getInstance().getTotal()));
        });

        Button plus = new Button("+");
        plus.setStyle(minus.getStyle());

        minus.setOnAction(e -> {
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
            } else {
                CartManager.getInstance().removeItem(item);
            }
        });
        plus.setOnAction(e -> item.setQuantity(item.getQuantity() + 1));

        Label subtotal = new Label(String.format("\u20a6%,.2f", item.getSubtotal()));
        subtotal.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-min-width: 80; -fx-alignment: center-right;");
        item.quantityProperty().addListener((obs, ov, nv) ->
                subtotal.setText(String.format("\u20a6%,.2f", item.getSubtotal())));

        Button remove = new Button("✕");
        remove.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-border-radius: 6;"
                + "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-weight: bold;");
        remove.setOnAction(e -> CartManager.getInstance().removeItem(item));

        row.getChildren().addAll(name, minus, qtyLabel, plus, subtotal, remove);
        return row;
    }

    @FXML
    private void handleCheckout() {
        User user = Session.getInstance().getCurrentUser();
        if (user == null) { showAlert("Error", "Not logged in."); return; }

        double total = CartManager.getInstance().getTotal();
        if (user.getAccountBalance() < total) {
            showAlert("Insufficient Balance",
                    String.format("Your balance (₦%,.2f) is less than the total (₦%,.2f). Please top up your wallet.", user.getAccountBalance(), total));
            return;
        }

        new Thread(() -> {
            try (Connection conn = DatabaseConnection.getConnect()) {
                conn.setAutoCommit(false);

                // Deduct balance
                PreparedStatement updateBalance = conn.prepareStatement(
                        "UPDATE users SET account_balance = account_balance - ? WHERE id = ?");
                updateBalance.setDouble(1, total);
                updateBalance.setInt(2, user.getId());
                updateBalance.executeUpdate();

                // Insert order
                PreparedStatement orderStmt = conn.prepareStatement(
                        "INSERT INTO orders (user_id, total) VALUES (?, ?) RETURNING id");
                orderStmt.setInt(1, user.getId());
                orderStmt.setDouble(2, total);
                ResultSet rs = orderStmt.executeQuery();
                rs.next();
                int orderId = rs.getInt(1);

                // Insert order items & reduce stock
                for (CartItem item : CartManager.getInstance().getItems()) {
                    PreparedStatement itemStmt = conn.prepareStatement(
                            "INSERT INTO order_items (order_id, menu_item_id, quantity, unit_price) VALUES (?,?,?,?)");
                    itemStmt.setInt(1, orderId);
                    itemStmt.setInt(2, item.getMenuItemId());
                    itemStmt.setInt(3, item.getQuantity());
                    itemStmt.setDouble(4, item.getUnitPrice());
                    itemStmt.executeUpdate();

                    PreparedStatement stockStmt = conn.prepareStatement(
                            "UPDATE menu_items SET quantity = quantity - ? WHERE id = ?");
                    stockStmt.setInt(1, item.getQuantity());
                    stockStmt.setInt(2, item.getMenuItemId());
                    stockStmt.executeUpdate();
                }

                conn.commit();

                // Update user balance in session
                user.setAccountBalance(user.getAccountBalance() - total);

                StringBuilder receipt = new StringBuilder();
                receipt.append(String.format("Order #%d\n", orderId));
                receipt.append("─────────────────────────\n");
                for (CartItem item : CartManager.getInstance().getItems()) {
                    receipt.append(String.format("%-22s x%d  ₦%,.2f\n",
                            item.getName().length() > 22 ? item.getName().substring(0, 19) + "..." : item.getName(),
                            item.getQuantity(), item.getSubtotal()));
                }
                receipt.append("─────────────────────────\n");
                receipt.append(String.format("TOTAL: ₦%,.2f\n", total));
                receipt.append(String.format("Balance remaining: ₦%,.2f", user.getAccountBalance()));

                String receiptStr = receipt.toString();

                CartManager.getInstance().clear();

                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Order Placed!");
                    alert.setHeaderText("Your order has been placed successfully.");
                    alert.setContentText(receiptStr);
                    alert.showAndWait();
                });

            } catch (SQLException ex) {
                Logger.getLogger(CartController.class.getName()).log(Level.SEVERE, null, ex);
                Platform.runLater(() -> showAlert("Error", "Failed to place order: " + ex.getMessage()));
            }
        }).start();
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        a.setTitle(title);
        a.showAndWait();
    }
}
