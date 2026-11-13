package cafeteriaapp.resources.controllers;

import cafeteriaapp.DatabaseConnection;
import cafeteriaapp.SceneManager;
import cafeteriaapp.Session;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ManagerDashboardController implements Initializable {

    // Table
    @FXML private TableView<MenuRow> menuTable;
    @FXML private TableColumn<MenuRow, Integer> colId;
    @FXML private TableColumn<MenuRow, String> colName;
    @FXML private TableColumn<MenuRow, String> colCategory;
    @FXML private TableColumn<MenuRow, Integer> colQty;
    @FXML private TableColumn<MenuRow, Double> colPrice;
    @FXML private TableColumn<MenuRow, String> colDesc;

    // Form fields
    @FXML private TextField nameField;
    @FXML private ChoiceBox<String> categoryBox;
    @FXML private TextField priceField;
    @FXML private TextField qtyField;
    @FXML private TextArea descField;

    private final ObservableList<MenuRow> rows = FXCollections.observableArrayList();
    private MenuRow selectedRow = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().id).asObject());
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name));
        colCategory.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().category));
        colQty.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().quantity).asObject());
        colPrice.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().price).asObject());
        colDesc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().description));

        categoryBox.getItems().addAll("Drinks", "Main Meal", "Swallow", "Protein", "Snacks");
        categoryBox.setValue("Drinks");

        menuTable.setItems(rows);
        menuTable.getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> {
            selectedRow = nv;
            if (nv != null) populateForm(nv);
        });

        new Thread(this::loadTable).start();
    }

    private void loadTable() {
        rows.clear();
        try (Connection conn = DatabaseConnection.getConnect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT id, name, category, quantity, price, description FROM menu_items ORDER BY category, name")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                rows.add(new MenuRow(rs.getInt(1), rs.getString(2), rs.getString(3),
                        rs.getInt(4), rs.getDouble(5), rs.getString(6)));
            }
        } catch (SQLException ex) {
            Logger.getLogger(ManagerDashboardController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void populateForm(MenuRow row) {
        nameField.setText(row.name);
        categoryBox.setValue(row.category);
        priceField.setText(String.valueOf(row.price));
        qtyField.setText(String.valueOf(row.quantity));
        descField.setText(row.description);
    }

    private void clearForm() {
        nameField.clear(); priceField.clear(); qtyField.clear(); descField.clear();
        categoryBox.setValue("Drinks"); selectedRow = null;
        menuTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleAdd() {
        String name = nameField.getText().trim();
        String cat = categoryBox.getValue();
        String desc = descField.getText().trim();
        double price; int qty;
        try { price = Double.parseDouble(priceField.getText().trim()); }
        catch (NumberFormatException e) { showAlert("Validation", "Enter a valid price."); return; }
        try { qty = Integer.parseInt(qtyField.getText().trim()); }
        catch (NumberFormatException e) { showAlert("Validation", "Enter a valid quantity."); return; }
        if (name.isEmpty()) { showAlert("Validation", "Name is required."); return; }

        new Thread(() -> {
            try (Connection conn = DatabaseConnection.getConnect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO menu_items (name, category, quantity, price, description) VALUES (?,?,?,?,?)")) {
                stmt.setString(1, name); stmt.setString(2, cat);
                stmt.setInt(3, qty); stmt.setDouble(4, price); stmt.setString(5, desc);
                stmt.executeUpdate();
                Platform.runLater(() -> { loadTable(); clearForm(); showAlert("Success", "Item added."); });
            } catch (SQLException ex) {
                Logger.getLogger(ManagerDashboardController.class.getName()).log(Level.SEVERE, null, ex);
                Platform.runLater(() -> showAlert("Error", ex.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleEdit() {
        if (selectedRow == null) { showAlert("Select Item", "Select a row to edit."); return; }
        String name = nameField.getText().trim();
        String cat = categoryBox.getValue();
        String desc = descField.getText().trim();
        double price; int qty;
        try { price = Double.parseDouble(priceField.getText().trim()); }
        catch (NumberFormatException e) { showAlert("Validation", "Enter a valid price."); return; }
        try { qty = Integer.parseInt(qtyField.getText().trim()); }
        catch (NumberFormatException e) { showAlert("Validation", "Enter a valid quantity."); return; }
        if (name.isEmpty()) { showAlert("Validation", "Name is required."); return; }

        int id = selectedRow.id;
        new Thread(() -> {
            try (Connection conn = DatabaseConnection.getConnect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE menu_items SET name=?,category=?,quantity=?,price=?,description=? WHERE id=?")) {
                stmt.setString(1, name); stmt.setString(2, cat);
                stmt.setInt(3, qty); stmt.setDouble(4, price);
                stmt.setString(5, desc); stmt.setInt(6, id);
                stmt.executeUpdate();
                Platform.runLater(() -> { loadTable(); clearForm(); showAlert("Success", "Item updated."); });
            } catch (SQLException ex) {
                Logger.getLogger(ManagerDashboardController.class.getName()).log(Level.SEVERE, null, ex);
                Platform.runLater(() -> showAlert("Error", ex.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleDelete() {
        if (selectedRow == null) { showAlert("Select Item", "Select a row to delete."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete \"" + selectedRow.name + "\"?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                int id = selectedRow.id;
                new Thread(() -> {
                    try (Connection conn = DatabaseConnection.getConnect();
                         PreparedStatement stmt = conn.prepareStatement("DELETE FROM menu_items WHERE id=?")) {
                        stmt.setInt(1, id);
                        stmt.executeUpdate();
                        Platform.runLater(() -> { loadTable(); clearForm(); showAlert("Deleted", "Item removed."); });
                    } catch (SQLException ex) {
                        Logger.getLogger(ManagerDashboardController.class.getName()).log(Level.SEVERE, null, ex);
                        Platform.runLater(() -> showAlert("Error", ex.getMessage()));
                    }
                }).start();
            }
        });
    }

    @FXML
    private void handleClear() { clearForm(); }

    @FXML
    private void handleLogout() {
        Session.getInstance().logout();
        try { SceneManager.switchTo("resources/views/login.fxml", false); }
        catch (IOException ex) { Logger.getLogger(ManagerDashboardController.class.getName()).log(Level.SEVERE, null, ex); }
    }

    private void showAlert(String title, String msg) {
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
            a.setTitle(title); a.showAndWait();
        });
    }

    public static class MenuRow {
        public final int id; public final String name, category, description;
        public final int quantity; public final double price;
        public MenuRow(int id, String name, String cat, int qty, double price, String desc) {
            this.id=id; this.name=name; this.category=cat;
            this.quantity=qty; this.price=price; this.description=desc==null?"":desc;
        }
    }
}
