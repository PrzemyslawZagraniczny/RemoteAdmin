package com.praca.remoteadmin.Connection;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.praca.remoteadmin.Brudnopis.Multithreading;
import com.praca.remoteadmin.Model.Computer;
import com.praca.remoteadmin.Model.LabRoom;
import com.praca.remoteadmin.Model.StatusType;
import com.praca.remoteadmin.Utils.DataFormatEnum;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
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
    public static String defaultCommand = "sudo printenv SUDO_USER";//"set|grep SSH";//"ls -al";
    public static String defaultLogin = "";
    public static String defaultPassword = "";

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


        switch(df) {

            case JSON:
                ObjectMapper mapper = new ObjectMapper();

                // Read JSON file and convert to java object
                InputStream fileInputStream = null;
                try {
                    fileInputStream = new FileInputStream("data.json");
                    LabRoom []sale = mapper.readValue(fileInputStream, LabRoom[].class);
                    data = FXCollections.observableArrayList(sale);
                    fileInputStream.close();
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (JsonMappingException e) {
                    throw new RuntimeException(e);
                } catch (JsonParseException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case TXT:
            default:
                data = FXCollections.observableArrayList();
                try {
                    List<String> list;

                    try {
                        list = Files.readAllLines(new File("workstations.txt").toPath());
                    } catch (IOException ex) {
                        return data;
                    }
                    int i = 1;
                    for (String address : list) {
                        //TODO: w przyszłości plik utworzyć jako csv i zapisać więcej wartości (np. nazwe stacji albo sali)
                        address = address.trim();
                        if (address.length() < 1) continue;
                        //ignoruj linie zaczynające się od znaku #
                        if (address.startsWith("#")) continue;
                        LabRoom sala = new LabRoom();
                        sala.getComputers().add(new Computer("Komputer " + i, address, StatusType.UNKNOWN));
                        data = FXCollections.observableArrayList(sala);
                        i++;
                    }

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
        }
        return data;
    }
}
