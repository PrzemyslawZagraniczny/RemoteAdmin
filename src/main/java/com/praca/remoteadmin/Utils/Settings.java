package com.praca.remoteadmin.Utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.praca.remoteadmin.Connection.ConnectionHelper;
import javafx.collections.FXCollections;

import java.io.*;

//klasa opcji ustawień
public class Settings implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer sshTm = 10000;          //10000 ms domyślnie
    private Integer sudoTm = 30000;          //10000 ms domyślnie
    private Long bufferSize = 1024 * 1000L;    //1MB domyślnego bufora dla każdej z konsol
    private Integer pingDelay = 10000;         //10 sek domyślnie

    public Long getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(Long bufferSize) {
        this.bufferSize = bufferSize;
    }

    public Integer getPingDelay() {
        return pingDelay;
    }

    public void setPingDelay(Integer pingDelay) {
        this.pingDelay = pingDelay;
    }

    private Boolean historyOn = true;    //czy zapamiętywać historię poleceń

    public Boolean getHistoryOn() {
        return historyOn;
    }

    public void setHistoryOn(Boolean historyOn) {
        this.historyOn = historyOn;
    }

    public Settings() {}

    public Settings(int sshConnectionTimeOut, int sudoConnectionTimeOut) {
        sshTm = sshConnectionTimeOut;
        sudoTm = sudoConnectionTimeOut;
    }

    public static void loadSettings() {
        ObjectMapper objectMapper = new ObjectMapper();
        File fin = null;

        try {

            fin = new File("settings.json");

            if(!fin.exists() || fin.length() <= 0) {
                File fout = new File("settings.json");
                Settings settings = new Settings(ConnectionHelper.sshConnectionTimeOut,ConnectionHelper.sudoConnectionTimeOut);
                objectMapper.writeValue(fout, settings);
            }
            else {
                Settings settings = objectMapper.readValue(fin, Settings.class);
                ConnectionHelper.sshConnectionTimeOut = settings.sshTm;
                ConnectionHelper.sudoConnectionTimeOut = settings.sudoTm;
                ConnectionHelper.historySave = settings.historyOn;
                ConnectionHelper.pingDelay = settings.pingDelay;
                ConnectionHelper.bufferSize = settings.bufferSize;
            }

        } catch (IOException e) {
            ConnectionHelper.log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    public static void saveData() {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            File fout = new File("settings.json");
            Settings settings = new Settings(ConnectionHelper.sshConnectionTimeOut,ConnectionHelper.sudoConnectionTimeOut);
            objectMapper.writeValue(fout, settings);

        } catch (IOException e) {
            ConnectionHelper.log.error(e.getMessage());
            e.printStackTrace();
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
