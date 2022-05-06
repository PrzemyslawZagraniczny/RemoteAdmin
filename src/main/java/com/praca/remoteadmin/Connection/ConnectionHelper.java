package com.praca.remoteadmin.Connection;

import com.praca.remoteadmin.Model.Computer;
import com.praca.remoteadmin.Model.StatusType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/*
Klasa pomocnicz słóżąca do:
 -pobierania danych o serwerach z pliku workstation.txt
 */
public class ConnectionHelper {
    //uczyń konstruktor prywatnym aby uniemożliwić utworzenie instancji obiektu tej klasy
    private ConnectionHelper() {
    }

    static ObservableList<Computer> data = null;
//            FXCollections.observableArrayList(
//            new Computer("Komputer 1", "192.168.42.141", StatusType.UNKNOWN)
//            new Computer("Komputer 2", "192.168.42.144", StatusType.UNKNOWN),
//            new Computer("Komputer 3", "192.168.42.145", StatusType.UNKNOWN),
//            new Computer("Komputer 4", "192.168.42.146", StatusType.UNKNOWN),
//            new Computer("Komputer 5", "192.168.42.147", StatusType.UNKNOWN)
//    );

    //upewnij się, że nie ma równoległego wywołania funkcji odczytu bo obiekt data jest statyczny!
    public static synchronized ObservableList<Computer> getComputers() {
        if(data != null) return data;
        data = FXCollections.observableArrayList();
        try {
            List<String> list;

            try {
                list = Files.readAllLines(new File("workstations.txt").toPath());
            } catch (IOException ex) {
                return data;
            }
            int i = 1;
            for (String address: list
                 ) {
                //TODO: w przyszłości plik utworzyć jako csv i zapisać więcej wartości (np. nazwe stacji albo sali)
                data.add(new Computer("Komputer " + i, address, StatusType.UNKNOWN));
                i++;
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return data;
    }
}
