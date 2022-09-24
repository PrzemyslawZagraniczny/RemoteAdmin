package com.praca.remoteadmin.Utils;


import com.praca.remoteadmin.Connection.ConnectionHelper;
import javafx.scene.control.ComboBox;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

    public static boolean addCommand(ComboBox<String> cb, String hash, String cmd ) {

        histList.addFirst(cmd);
        try {
            FileWriter fileWritter = new FileWriter(new File(".history"+hash+".log"),true);
            BufferedWriter bw = new BufferedWriter(fileWritter);
            bw.write(cmd + System.lineSeparator());
            bw.close();
            //cb.getItems().add(0, cmd);
            //cb.getSelectionModel().select(-1);
        } catch (IOException ex) {
            ConnectionHelper.log.error(ex.getMessage());
            System.err.println(ex.getMessage());
            return false;
        }

        return true;
    }

    public static String calcHashForHistoryLog(String login) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
            byte[] digiest = md.digest(login.getBytes());
            BigInteger no = new BigInteger(1, digiest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            ConnectionHelper.log.error(e.getMessage());
            System.err.println(e.getMessage());
        }
        return null;
    }

    public static boolean loadData(ComboBox<String> cb, String hash) {
        List<String> list = null;
        try {
            File f = new File(".history"+hash+".log");
            if(f.exists())
                list = Files.readAllLines(f.toPath());
            else {
                f.createNewFile();
                return true;
            }
        } catch (IOException ex) {
            ConnectionHelper.log.error(ex.getMessage());
            System.err.println(ex.getMessage());
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
