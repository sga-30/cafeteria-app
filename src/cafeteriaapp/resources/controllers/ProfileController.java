package cafeteriaapp.resources.controllers;

import cafeteriaapp.DatabaseConnection;
import cafeteriaapp.SceneManager;
import cafeteriaapp.Session;
import cafeteriaapp.User;
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
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class ProfileController implements Initializable {

    @FXML private Label nameLabel;
    @FXML private Label emailLabel;
    @FXML private Label balanceLabel;
    @FXML private TextField topUpField;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        refreshUI();
    }

    private void refreshUI() {
        User user = Session.getInstance().getCurrentUser();
        if (user == null) return;
        nameLabel.setText(user.getFirstName() + " " + user.getLastName());
        emailLabel.setText(user.getEmail());
        balanceLabel.setText(String.format("₦%,.2f", user.getAccountBalance()));
    }

    @FXML
    private void handleTopUp() {
        String text = topUpField.getText().trim();
        double amount;
        try {
            amount = Double.parseDouble(text);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert("Invalid Amount", "Please enter a valid positive amount.");
            return;
        }

        User user = Session.getInstance().getCurrentUser();
        new Thread(() -> {
            try (Connection conn = DatabaseConnection.getConnect()) {
                PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE users SET account_balance = account_balance + ? WHERE id = ? RETURNING account_balance");
                stmt.setDouble(1, amount);
                stmt.setInt(2, user.getId());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    double newBalance = rs.getDouble(1);
                    user.setAccountBalance(newBalance);
                    Platform.runLater(() -> {
                        topUpField.clear();
                        refreshUI();
                        showAlert("Success", String.format("₦%,.2f added to your wallet.", amount));
                    });
                }
            } catch (SQLException ex) {
                Logger.getLogger(ProfileController.class.getName()).log(Level.SEVERE, null, ex);
                Platform.runLater(() -> showAlert("Error", "Top-up failed: " + ex.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleLogout() {
        Session.getInstance().logout();
        try {
            SceneManager.switchTo("resources/views/login.fxml", false);
        } catch (IOException ex) {
            Logger.getLogger(ProfileController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setTitle(title);
        a.showAndWait();
    }
}
