package com.praca.remoteadmin.Utils;


import com.praca.remoteadmin.Connection.ConnectionHelper;
import javafx.scene.control.ComboBox;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

//klasa zarządza wpisami historycznymi do konsoli
public class HistoryLog {
    static LinkedList<String> histList = new LinkedList<>();
    private String strCmd;      //komenda

    private HistoryLog(String cmd) {
        strCmd = cmd;
    }

    public static boolean addCommand(ComboBox<String> cb, String cmd ) {

        histList.addFirst(cmd);
        try {
            FileWriter fileWritter = new FileWriter(new File("history.log"),true);
            BufferedWriter bw = new BufferedWriter(fileWritter);
            bw.write(cmd + System.lineSeparator());
            bw.close();
            cb.getItems().add(0, cmd);
            cb.getSelectionModel().select(-1);
        } catch (IOException ex) {
            ConnectionHelper.log.error(ex.getMessage());
            return false;
        }

        return true;
    }
    public static boolean loadData(ComboBox<String> cb) {
        List<String> list = null;
        try {
            list = Files.readAllLines(new File("history.log").toPath());
        } catch (IOException ex) {
            ConnectionHelper.log.error(ex.getMessage());
            return false;
        }
        if (cb != null) {
            cb.getItems().removeAll();
            for(String linia : list) {
                String cmd = linia;
                histList.addFirst(cmd);
                cb.getItems().add(cmd);
            }

        }
        return true;
    }

}
