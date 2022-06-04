package com.praca.remoteadmin;

import com.praca.remoteadmin.Connection.SSH2Connector;
import com.praca.remoteadmin.Model.Computer;
import com.praca.remoteadmin.Model.StatusType;
import com.praca.remoteadmin.Model.WorkStation;
import org.junit.jupiter.api.Test;

class SHH2Test {


    @Test
    void SSH2Test() {
        SSH2Connector conn = new SSH2Connector();
        Computer comp = new WorkStation("Jacob", "192.168.42.141", StatusType.CONNECTED);
        if(conn == null) {
            conn = new SSH2Connector();


            conn.openConnection("przemek", "przemek123", comp);
            conn.setErrorStream(System.err);
            //conn.setOutputStream(new ConsoleCaptureOutput());

        }
        try {

            conn.execCommand("ls -A");
        } catch (Exception e) {
            System.err.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}