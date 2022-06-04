package com.praca.remoteadmin;

import com.praca.remoteadmin.Connection.SSH2Connector;
import com.praca.remoteadmin.Model.Computer;
import com.praca.remoteadmin.Model.StatusType;
import com.praca.remoteadmin.Model.WorkStation;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

class SHH2Test {


    @Test
    void SSH2Test() {
        try {
            List<String> list;

            try {
                list = Files.readAllLines(new File("workstations.txt").toPath());
            } catch (IOException ex) {
                return data;
            }
            int i = 1;
            for (String address: list) {
                SSH2Connector conn = new SSH2Connector();
                Computer comp = new WorkStation("Komputer "+i, address, StatusType.CONNECTED);
                if (conn == null) {
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
                i++;
            }
    }
}