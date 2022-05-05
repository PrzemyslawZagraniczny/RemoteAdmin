package com.praca.remoteadmin.Model;

public class WorkStation  extends Computer {
    public WorkStation(String sName, String sAddress, StatusType stat) {
        super(sName, sAddress, stat);
    }
    public WorkStation(String sAddress, StatusType stat) {
        super("brak", sAddress, stat);
    }
}
