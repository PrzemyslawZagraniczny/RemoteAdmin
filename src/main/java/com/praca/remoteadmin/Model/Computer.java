package com.praca.remoteadmin.Model;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.scene.control.Alert;

import java.awt.*;
import java.util.TimerTask;

public class Computer {
    private SimpleStringProperty name = new SimpleStringProperty();
    private SimpleStringProperty  address = new SimpleStringProperty();

    public String getStatus() {
        return status.get();
    }

    public SimpleStringProperty statusProperty() {
        return status;
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    private SimpleStringProperty  status = new SimpleStringProperty();
    //stan maszyny (czy jest włączona (ACTIVE), nieaktywna (OFFLINE)
    private StatusType stat = StatusType.UNKNOWN;
    //czy maszyna zostala wybrana do poczenia (ustawiane checkboxem)
    private SimpleBooleanProperty selected = new SimpleBooleanProperty(true);//new SimpleBooleanProperty
    private SimpleIntegerProperty cmdExitStatus = new SimpleIntegerProperty(-1);

    public double getProgressStatus() {
        return progressStatus.get();
    }

    public SimpleDoubleProperty progressStatusProperty() {
        return progressStatus;
    }

    public void setProgressStatus(double progressStatus) {
        this.progressStatus.set(progressStatus);
    }

    private SimpleDoubleProperty progressStatus = new SimpleDoubleProperty(0);

    public int getCmdExitStatus() {
        return cmdExitStatus.get();
    }

    public SimpleIntegerProperty cmdExitStatusProperty() {
        return cmdExitStatus;
    }

    public void setCmdExitStatus(int cmdExitStatus) {
        this.cmdExitStatus.set(cmdExitStatus);

        if (EventQueue.isDispatchThread()) {
        } else {

            if(cmdExitStatus != 0) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Uwaga!");
                    alert.setHeaderText(getAddress());
                    alert.setContentText("Maszyna wróciła status wykonania polecenia <<" + cmdExitStatus + ">>");
                    alert.show();
                });
            }

        }
    }

    public Computer() {
    }

    public Computer(String sName, String sAddress, StatusType stat) {
        this.name.set(sName);
        this.address.set(sAddress);
        this.stat = stat;
        status.set(stat.toString());
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getAddress() {
        return address.get();
    }

    public SimpleStringProperty addressProperty() {
        return address;
    }

    public void setAddress(String address) {
        this.address.set(address);
    }

    public boolean isSelected() {
        return selected.get();
    }

    public SimpleBooleanProperty selectedProperty() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
        System.out.println(selected);
    }

    public String getStat() {
        return stat.toString();
    }


    public void setStat(StatusType stat) {
        this.stat = stat;
        status.set(stat.toString());
    }
}
