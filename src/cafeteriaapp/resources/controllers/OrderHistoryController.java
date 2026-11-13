package cafeteriaapp.resources.controllers;

import cafeteriaapp.DatabaseConnection;
import cafeteriaapp.Session;
import cafeteriaapp.User;
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
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class OrderHistoryController implements Initializable {

    @FXML private VBox ordersContainer;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        new Thread(this::loadOrders).start();
    }

    private void loadOrders() {
        User user = Session.getInstance().getCurrentUser();
        if (user == null) return;

        List<VBox> cards = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnect()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT id, total, created_at FROM orders WHERE user_id = ? ORDER BY created_at DESC");
            stmt.setInt(1, user.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int orderId = rs.getInt("id");
                double total = rs.getDouble("total");
                String date = rs.getTimestamp("created_at").toString().substring(0, 16);

                // Get order items
                PreparedStatement itemStmt = conn.prepareStatement(
                        "SELECT m.name, oi.quantity, oi.unit_price FROM order_items oi "
                        + "JOIN menu_items m ON m.id = oi.menu_item_id WHERE oi.order_id = ?");
                itemStmt.setInt(1, orderId);
                ResultSet ir = itemStmt.executeQuery();

                List<String> lines = new ArrayList<>();
                while (ir.next()) {
                    lines.add(String.format("%s x%d — ₦%,.2f",
                            ir.getString("name"), ir.getInt("quantity"),
                            ir.getDouble("unit_price") * ir.getInt("quantity")));
                }

                VBox card = buildOrderCard(orderId, date, total, lines);
                cards.add(card);
            }

        } catch (SQLException ex) {
            Logger.getLogger(OrderHistoryController.class.getName()).log(Level.SEVERE, null, ex);
        }

        Platform.runLater(() -> {
            ordersContainer.getChildren().clear();
            if (cards.isEmpty()) {
                Label empty = new Label("No orders yet. Go place your first order!");
                empty.setStyle("-fx-text-fill: #888; -fx-font-size: 14px;");
                ordersContainer.getChildren().add(empty);
            } else {
                ordersContainer.getChildren().addAll(cards);
            }
        });
    }

    private VBox buildOrderCard(int orderId, String date, double total, List<String> lines) {
        VBox card = new VBox(6);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 14;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.07),6,0,0,2);");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label orderNum = new Label("Order #" + orderId);
        orderNum.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");
        Label dateLabel = new Label(date);
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888; -fx-padding: 0 0 0 10;");
        HBox spacer = new HBox(); HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        Label totalLabel = new Label(String.format("₦%,.2f", total));
        totalLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #3b5bdb;");
        header.getChildren().addAll(orderNum, dateLabel, spacer, totalLabel);

        card.getChildren().add(header);
        for (String line : lines) {
            Label l = new Label(line);
            l.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");
            card.getChildren().add(l);
        }
        return card;
    }
}
