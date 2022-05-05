package com.praca.remoteadmin.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class HelloController {
    public Text actiontarget;
    @FXML
    private Label welcomeText;
    @FXML
    private Label witajText;

    @FXML
    TextField txtImie;

    private int x = 1;
    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!" + x++);
    }
    @FXML
    protected void onWitajButtonClick() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Uwaga!");
        alert.setHeaderText("Look, an Information Dialog");
        alert.setContentText("Witaj " + txtImie.getText().trim());

        alert.showAndWait().ifPresent(rs -> {
            if (rs == ButtonType.OK) {
                System.out.println("Pressed OK.");
            }
        });
    }

    public void handleSubmitButtonAction(ActionEvent actionEvent) {
        actiontarget.setText( actionEvent.getEventType().getName());
    }
}