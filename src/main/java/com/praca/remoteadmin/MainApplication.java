package com.praca.remoteadmin;

import com.praca.remoteadmin.Connection.ConnectionHelper;
import com.praca.remoteadmin.Controller.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("View/main1.fxml"));

        Parent parent = fxmlLoader.load();
        if(parent == null)
        {
            System.err.println("Błąd pliku FXML");
            ConnectionHelper.log.info("Błąd pliku FXML");
        }

        Scene scene = new Scene(parent, 1024, 768);
        stage.setMinWidth(640);
        stage.setMinHeight(610);
        //stage.setMaxHeight(768);
        scene.getStylesheets().add
                (MainApplication.class.getResource("css/main.css").toExternalForm());
        stage.setTitle("RemoteAdmin");

        stage.setScene(scene);
        stage.show();
        ((MainController) fxmlLoader.getController()).setStage(stage);
        ConnectionHelper.log.info("App successfully started.");
    }

    @Override
    public void stop(){

        ConnectionHelper.log.info("App successfully closed.");
        ConnectionHelper.log.info("************************");
    }
    public static void main(String[] args) throws FileNotFoundException {

        //TODO: Usuń w wersji finalnej!!!!
        //wczytuje dane z pliku (TYMCZASOWO)
        try {
            FileInputStream fis=new FileInputStream("pass.txt");
            Scanner s=new Scanner(fis);
            if(s.hasNextLine())
                ConnectionHelper.defaultLogin = s.nextLine().trim();
            if(s.hasNextLine())
                ConnectionHelper.defaultPassword = s.nextLine().trim();
            s.close();
        }
        catch (FileNotFoundException e) {
            //nie rób nic, brak pliku pozostawia pola puste
        }
        launch();
    }
}