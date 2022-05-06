package com.praca.remoteadmin.Controller;

import com.jcraft.jsch.JSchException;
import com.praca.remoteadmin.Connection.ConnectionHelper;
import com.praca.remoteadmin.Connection.ConsoleCaptureOutput;
import com.praca.remoteadmin.Connection.IGenericConnector;
import com.praca.remoteadmin.Connection.SSH2Connector;
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
import javafx.scene.control.*;
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
    @FXML
    public PasswordField passwordField;
    @FXML
    public TextField loginField;
    public TextArea consoleOutput;
    public TextField cmdLine;
    public TableColumn cmdStatCol;
    private IGenericConnector conn = null;


    public void onQuit(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void onSelectedAction(Event event) {
    }
    @FXML
    public void initialize() {
        consoleOutput.autosize();
        cmdLine.setText("set|grep SSH");
        loginField.setText("przemek");
        passwordField.setText("przemek123");


        statusCol.setCellValueFactory(
                new PropertyValueFactory<>("status")
        );
        addressCol.setCellValueFactory(
                new PropertyValueFactory<>("address")
        );
        selectCol.setCellValueFactory(
                new PropertyValueFactory<>("selected")
        );
        cmdStatCol.setCellValueFactory(
                new PropertyValueFactory<>("cmdExitStatus")
        );


        table.getItems().addAll(ConnectionHelper.getComputers());
    }
    @FXML
    public void OnLogingIn(ActionEvent actionEvent) {
        String sLogin = loginField.getText().trim();
        String sPassword = passwordField.getText().trim();
        //TODO:
        //szyfruj hasło i login zaraz po przejęciu od użytkownika oraz zeruj ich wartości w polach
    }

    public void onExecuteCommand(ActionEvent actionEvent) {

        //TODO: Pilnie zmień kod. Połaczenie musi być wykonywane osobno

//        data.get(1).setStat(StatusType.ACTIVE);
//        if( true) return;

        Computer comp = ConnectionHelper.getComputers().get(0);
        if(conn == null) {
            conn = new SSH2Connector();


            try {
                conn.openConnection(loginField.getText(), passwordField.getText(), comp);
                conn.setErrorStream(System.err);
                conn.setOutputStream(new ConsoleCaptureOutput(consoleOutput));
                //conn.setOutputStream(System.out);
            } catch (JSchException e) {
                System.err.println(e.getMessage());
                throw new RuntimeException(e);
            }
        }
        try {

            String cmd = cmdLine.getText().trim();
            conn.execCommand(cmd);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            throw new RuntimeException(e);
        }

    }

    public void onConsolClear(ActionEvent actionEvent) {
        consoleOutput.clear();
    }
}
