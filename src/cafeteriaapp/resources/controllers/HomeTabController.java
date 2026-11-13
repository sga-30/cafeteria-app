/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cafeteriaapp.resources.controllers;

import cafeteriaapp.SceneManager;
import cafeteriaapp.Session;
import cafeteriaapp.User;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

/**
 *
 * @author DELL
 */
public class HomeTabController implements Initializable {

    @FXML
    private Label greetingsLabel;
    @FXML
    private Label accountBalanceLabel;
    @FXML
    private GridPane menuPane;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        User user = Session.getInstance().getCurrentUser();
        greetingsLabel.textProperty().bind(
                Bindings.createStringBinding(
                        () -> {

                            return user != null
                                    ? "Welcome " + user.getLastName()
                                    : "Welcome";
                        },
                        Session.getInstance().currentUserProperty()
                )
        );

        accountBalanceLabel.textProperty().bind(
                Bindings.createStringBinding(
                        () -> {
                            return user != null
                                    ? String.format("₦%,.2f", user.getAccountBalance())
                                    : "₦0.00";
                        }
                )
        );
    }

    @FXML
    private void navigateToMenuPage() {
        try {
            SceneManager.switchTo(
                    "resources/views/menu.fxml",
                    false
            );
        } catch (IOException ex) {
            Logger.getLogger(HomeTabController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
