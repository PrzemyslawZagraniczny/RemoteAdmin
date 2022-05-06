package com.praca.remoteadmin.Model;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

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

    public int getCmdExitStatus() {
        return cmdExitStatus.get();
    }

    public SimpleIntegerProperty cmdExitStatusProperty() {
        return cmdExitStatus;
    }

    public void setCmdExitStatus(int cmdExitStatus) {
        this.cmdExitStatus.set(cmdExitStatus);
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
    }

    public String getStat() {
        return stat.toString();
    }


    public void setStat(StatusType stat) {
        this.stat = stat;
        status.set(stat.toString());
    }
}
