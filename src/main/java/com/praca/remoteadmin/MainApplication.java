package com.praca.remoteadmin;

import com.praca.remoteadmin.Connection.ConnectionHelper;
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
        ConnectionHelper.log.info("App successfully started.");
    }
    @Override
    public void stop(){
        ConnectionHelper.log.info("App successfully closed.");
        ConnectionHelper.log.info("************************");
        // Save file
    }
    public static void main(String[] args) {
        launch();
    }
}