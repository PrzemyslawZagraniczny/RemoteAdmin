package com.praca.remoteadmin;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;

/*
    Dialog wspomaga dodawanie komputerów do ustawionej pracowni.
    Po kliku na <<OK>>  komputery zostają dodane do aktywnej pracowni
    i zapisane w pliku 'data.json'

 */
public class AddComputerDialog extends Pane {

    private final ObjectProperty<Exception> exception = null;

    public ObjectProperty<Exception> exceptionProperty() {
        return exception ;
    }

    public final Exception getException() {
        return exceptionProperty().get();
    }

    public final void setException(Exception exception) {
        exceptionProperty().set(exception);
    }

    @FXML
    private TextArea stackTrace;
    @FXML
    private Label message;

    public AddComputerDialog() throws Exception {
        //System.out.println(AddComputerDialog.class.getResource("../View/").getPath());
        System.out.println(getClass().getResource("View/computer_add.fxml").getPath());
        FXMLLoader loader = new FXMLLoader(getClass().getResource("View/computer_add.fxml"));

//        FXMLLoader loader = new FXMLLoader(getClass().getResource("../View/computer_add.fxml"));
        //loader.setRoot(this);
        loader.setController(this);


    }
    public static void main(String[] args) throws InterruptedException {

        try {
            Platform.startup(() -> {
                Stage dialog = new Stage();
                dialog.initStyle(StageStyle.UTILITY);
                Scene scene = null;
                try {
                    scene = new Scene(new AddComputerDialog());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                dialog.setScene(scene);
                dialog.show();
            });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
