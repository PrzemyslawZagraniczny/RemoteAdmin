package com.praca.remoteadmin.Connection;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.praca.remoteadmin.Brudnopis.Multithreading;
import com.praca.remoteadmin.Model.Computer;
import com.praca.remoteadmin.Model.LabRoom;
import com.praca.remoteadmin.Model.StatusType;
import com.praca.remoteadmin.Utils.DataFormatEnum;
import com.praca.remoteadmin.Utils.DataLoaderFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.log4j.Logger;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;


/*
Klasa pomocnicz służąca do:
 -pobierania danych o serwerach z pliku workstation.txt
 */
public class ConnectionHelper {

    public static final SecureRandom rnd = new SecureRandom ();
    public static Logger log = Logger.getLogger(Multithreading.class.getName());
    public static int sshConnectionTimeOut = 10000;    //10 sekund
    public static int sudoConnectionTimeOut = 10000;    //10 sekund
    public static boolean bRSAKeyFingerprintIgnore = true;
    public static String defaultCommand = "sudo ./tmp.sh";//"ls -al";//"sudo printenv SUDO_USER";//"set|grep SSH";//"ls -al";
    public static String defaultLogin = "";
    public static String defaultPassword = "";
    public static boolean historySave = false;
    public static int mask[] = {127, 191, 256}  ;    //maski dla klas A, B, C
    public static long bufferSize = 1024 * 100L;
    public static int pingDelay;
    public static int pingTimeout = 5000;   //[ms]

    //uczyń konstruktor prywatnym aby uniemożliwić utworzenie instancji obiektu tej klasy
    private ConnectionHelper() {
    }


    static ObservableList<LabRoom> data = null;
//            FXCollections.observableArrayList(
//            new Computer("Komputer 1", "192.168.42.141", StatusType.UNKNOWN)
//            new Computer("Komputer 2", "192.168.42.144", StatusType.UNKNOWN),
//            new Computer("Komputer 3", "192.168.42.145", StatusType.UNKNOWN),
//            new Computer("Komputer 4", "192.168.42.146", StatusType.UNKNOWN),
//            new Computer("Komputer 5", "192.168.42.147", StatusType.UNKNOWN)
//    );

    //upewnij się, że nie ma równoległego wywołania funkcji odczytu bo obiekt data jest statyczny!
    public static synchronized ObservableList<LabRoom> loadData(DataFormatEnum df) {
        if(data != null) return data;

        return DataLoaderFactory.loadData(df);
    }

    public static void saveData(ObservableList<LabRoom> sale) {
        DataLoaderFactory.saveData(sale);
    }
}
