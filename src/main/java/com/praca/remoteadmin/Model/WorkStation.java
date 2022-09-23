package com.praca.remoteadmin.Model;

public class WorkStation  extends Computer {
    public WorkStation(String sName, String sAddress, StatusType stat) {
        super(null, sName, sAddress, stat);
    }
    public WorkStation(String sAddress, StatusType stat) {
        super(null, "brak", sAddress, stat);
    }
}
