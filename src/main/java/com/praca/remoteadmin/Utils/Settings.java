package com.praca.remoteadmin.Utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.praca.remoteadmin.Connection.ConnectionHelper;

import java.io.*;

//klasa opcji ustawień
public class Settings implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer sshTm = 10000;      //10000 ms domyślnie
    private Integer sudoTm = 30000;      //10000 ms domyślnie

    public Settings() {}

    public Settings(int sshConnectionTimeOut, int sudoConnectionTimeOut) {
        sshTm = sshConnectionTimeOut;
        sudoTm = sudoConnectionTimeOut;
    }

    public static void loadSettings() {
        ObjectMapper objectMapper = new ObjectMapper();
        InputStream fin = null;

        try {
            fin = new FileInputStream("settings.json");
            Settings settings = objectMapper.readValue(fin, Settings.class);
            ConnectionHelper.sshConnectionTimeOut = settings.sshTm;
            ConnectionHelper.sudoConnectionTimeOut = settings.sudoTm;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveData() {
        ObjectMapper objectMapper = new ObjectMapper();
        InputStream fin = null;

        try {
            Settings settings = new Settings(ConnectionHelper.sshConnectionTimeOut,ConnectionHelper.sudoConnectionTimeOut);
            objectMapper.writeValue(new File("settings.json"), settings);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public Integer getSshTm() {
        return sshTm;
    }

    public void setSshTm(Integer sshTm) {
        this.sshTm = sshTm;
    }

    public Integer getSudoTm() {
        return sudoTm;
    }

    public void setSudoTm(Integer sudoTm) {
        this.sudoTm = sudoTm;
    }
}
