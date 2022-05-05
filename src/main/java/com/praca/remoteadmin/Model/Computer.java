package com.praca.remoteadmin.Model;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Computer {
    private String name = new String();
    private String  address = new String();
    //stan maszyny (czy jest włączona (ACTIVE), nieaktywna (OFFLINE)
    private StatusType stat = StatusType.UNKNOWN;
    //czy maszyna zostala wybrana do poczenia (ustawiane checkboxem)
    private Boolean selected = (false);//new SimpleBooleanProperty

    public Computer() {
    }

    public Computer(String sName, String sAddress, StatusType stat) {
        this.name = (sName);
        this.address = (sAddress);
        this.stat = stat;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public String getStat() {
        return stat.toString();
    }


    public void setStat(StatusType stat) {
        this.stat = stat;
    }
}
