/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cafeteriaapp.resources.controllers;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Paint;

/**
 *
 * @author DELL
 */
public class DashboardController implements Initializable {

    @FXML
    private Pane centerPane;
    @FXML
    private FontAwesomeIconView homeBtn, walletBtn, ordersBtn, profileBtn, cartBtn;

    private FontAwesomeIconView activeBtn = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.loadView("home-view.fxml");
        setActiveButton(homeBtn);
    }

    private void setActiveButton(FontAwesomeIconView btn) {
        if (activeBtn != null) {
            activeBtn.setFill(Paint.valueOf("#888888")); // inactive color
        }
        btn.setFill(Paint.valueOf("#FFFFFF")); // active color
        activeBtn = btn;
    }

    private void loadView(String fxmlFile) {
        try {
            Parent view = FXMLLoader.load(
                    getClass().getResource("../views/" + fxmlFile)
            );
            centerPane.getChildren().setAll(view);

            // Make the loaded view fill the pane
            if (view instanceof Region region) {
                region.prefWidthProperty().bind(centerPane.widthProperty());
                region.prefHeightProperty().bind(centerPane.heightProperty());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleHomeBtn(MouseEvent event) {
        loadView("home-view.fxml");
        setActiveButton(homeBtn);
    }

    @FXML
    private void handleWalletBtn(MouseEvent event) {
        loadView("wallet-view.fxml");
        setActiveButton(walletBtn);
    }

    @FXML
    private void handleCartBtn(MouseEvent event) {
        loadView("cart-view.fxml");
        setActiveButton(cartBtn);
    }

    @FXML
    private void handleOrdersBtn(MouseEvent event) {
        loadView("order-history.fxml");
        setActiveButton(ordersBtn);
    }

    @FXML
    private void handleProfileBtn(MouseEvent event) {
        loadView("profile-view.fxml");
        setActiveButton(profileBtn);
    }
}
