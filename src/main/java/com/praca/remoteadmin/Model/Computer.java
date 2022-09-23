package com.praca.remoteadmin.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.praca.remoteadmin.Connection.ConnectionHelper;
import com.praca.remoteadmin.Utils.ExitStatusMapper;
import javafx.beans.property.*;

import java.awt.*;

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

    public void setObserver(ISaveDataObserver observer) {
        this.observer = observer;
    }

    @JsonIgnore
    LabRoom parent = null;
    @JsonIgnore
    private ISaveDataObserver observer = null;
    @JsonIgnore
    private SimpleStringProperty  status = new SimpleStringProperty(StatusType.UNKNOWN+"");
    @JsonIgnore
    //stan maszyny (czy jest włączona (ACTIVE), nieaktywna (OFFLINE)
    private StatusType stat = StatusType.UNKNOWN;

    //czy maszyna zostala wybrana do poczenia (ustawiane checkboxem)
    private SimpleBooleanProperty selected = new SimpleBooleanProperty(true);//new SimpleBooleanProperty
    @JsonIgnore
    private SimpleStringProperty cmdExitStatus = new SimpleStringProperty("");

    public double getProgressStatus() {
        return progressStatus.get();
    }

    public SimpleDoubleProperty progressStatusProperty() {
        return progressStatus;
    }

    public void setProgressStatus(double progressStatus) {
        this.progressStatus.set(progressStatus);
    }

    @JsonIgnore
    private SimpleDoubleProperty progressStatus = new SimpleDoubleProperty(0);

    public String getCmdExitStatus() {
        return cmdExitStatus.get();
    }

    public SimpleStringProperty cmdExitStatusProperty() {
        return cmdExitStatus;
    }

    public void setCmdExitStatus(int cmdExitStatus) {
        this.cmdExitStatus.set(ExitStatusMapper.fromExitCode(cmdExitStatus) +" ("+ cmdExitStatus+")");

        if (EventQueue.isDispatchThread()) {
        } else {

            if(cmdExitStatus != 0) {
//                Platform.runLater(() -> {
//                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
//                    alert.setTitle("Uwaga!");
//                    alert.setHeaderText(getAddress());
//                    alert.setContentText("Maszyna wróciła status wykonania polecenia <<" + cmdExitStatus + ">>");
//                    alert.show();
//                });
            }

        }
    }

    public Computer() {
        addSelectedListener();

    }

    public Computer(LabRoom parent, String sName, String sAddress, StatusType stat) {
        this.parent = parent;

        this.name.set(sName);
        this.address.set(sAddress);
        this.stat = stat;
        status.set(stat.toString());
        addSelectedListener();
    }

    private void addSelectedListener() {
        selected.addListener(observable -> {
            if(observer != null) {
                observer.saveData();
            }

            ConnectionHelper.log.info("User "+(selected.get() ?"selected":"unselected")+" host <<"+getAddress()+">>");
        });
        selected.addListener(change -> {
            if(parent != null ) {
                if (selected.get())
                    parent.numberOfComputersProperty().set(parent.numberOfComputersProperty().get() + 1);
                else
                    parent.numberOfComputersProperty().set(parent.numberOfComputersProperty().get() - 1);
                parent.computerStatusProperty().set(parent.numberOfComputersProperty().get() + "/" + parent.getComputers().size());
            }
        });
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

        ConnectionHelper.log.info("User "+(selected ?"selected":"unselected")+" host <<"+getAddress()+">>");
    }

    public StatusType getStat() {
        return stat;
    }


    public void setStat(StatusType stat) {
        this.stat = stat;
        status.set(stat.toString());
    }

    public void setParent(LabRoom labRoom) {
        parent = labRoom;
    }
}
