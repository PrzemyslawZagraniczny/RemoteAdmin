package com.praca.remoteadmin.Controller;

import com.praca.remoteadmin.Model.Computer;
import com.praca.remoteadmin.Model.StatusType;
import com.praca.remoteadmin.Model.WorkStation;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class MainController {
    @FXML
    public MenuItem btQuit;

    @FXML
    public TableColumn<Computer, String> statusCol;
    @FXML
    public TableColumn<Computer,String> addressCol;
    @FXML
    public TableColumn<Computer,Boolean> selectCol;
    @FXML
    public TableView<Computer> table;



    public void onQuit(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void onSelectedAction(Event event) {
    }
    @FXML
    public void initialize() {
        System.out.println("WCHODZI!!!!!");
        statusCol.setCellValueFactory(
                new PropertyValueFactory<>("stat")
        );
        addressCol.setCellValueFactory(
                new PropertyValueFactory<>("address")
        );
        selectCol.setCellValueFactory(
                new PropertyValueFactory<>("selected")
        );
        final ObservableList<Computer> data = FXCollections.observableArrayList(
                new Computer("Jacob", "198.164.42.141", StatusType.ACTIVE),
                new Computer("Isabella", "198.164.42.144", StatusType.ACTIVE),
                new Computer("Ethan", "198.164.42.145", StatusType.OFFLINE),
                new Computer("Emma", "198.164.42.146", StatusType.ACTIVE),
                new Computer("Michael", "198.164.42.147", StatusType.OFFLINE)
        );


        table.getItems().addAll(data);
    }
}
