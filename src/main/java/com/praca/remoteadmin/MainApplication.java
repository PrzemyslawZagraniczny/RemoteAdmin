package com.praca.remoteadmin;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("View/main1.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1024, 768);
        stage.setMinWidth(640);
        stage.setMinHeight(610);
        stage.setMaxHeight(768);
        scene.getStylesheets().add
                (MainApplication.class.getResource("css/main.css").toExternalForm());
        stage.setTitle("RemoteAdmin");

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}