package com.praca.remoteadmin.Utils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.praca.remoteadmin.Connection.ConnectionHelper;
import com.praca.remoteadmin.GUI.MessageBoxTask;
import com.praca.remoteadmin.Model.Computer;
import com.praca.remoteadmin.Model.LabRoom;
import com.praca.remoteadmin.Model.StatusType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;

import java.io.*;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;

enum roomLoad{}

//Loader danych o salach i komputerach (domyślnie zaimplementowano format JSON)
public class DataLoaderFactory {
    public static synchronized ObservableList<LabRoom> loadData(DataFormatEnum df) {


        ObservableList<LabRoom> data = null;
        switch (df) {
                case JSON:
                    ObjectMapper mapper = new ObjectMapper();

                    try {

                        File file = new File("data.json");
                        if(!file.exists()) {
                            if(!file.createNewFile()) {
                                //TODO: komunikat o tym, że ni da sie utworzyc pliku
                            }
                            return  FXCollections.observableArrayList();
                        }
                        else {
                            if(file.length() <= 0)      // pusty plik nie ma co mapować
                                return  FXCollections.observableArrayList();
                            LabRoom[] sale = (LabRoom[])mapper.readValue(file, LabRoom[].class);
                            data = FXCollections.observableArrayList(sale);
                        }
                        break;

                    } catch (JsonMappingException var10) {
                        ConnectionHelper.log.error(var10);
                        new MessageBoxTask( "Plik <<data.json>> posiada uszkodzone dane.","Uwaga", Alert.AlertType.ERROR).run();
                        var10.printStackTrace();
                    } catch (JsonParseException var11) {
                        ConnectionHelper.log.error(var11);
                        var11.printStackTrace();
                    } catch (IOException var12) {
                        ConnectionHelper.log.error(var12);
                        var12.printStackTrace();

                    }
                case TXT:
                default:
                    data = FXCollections.observableArrayList();

                    try {
                        List list;
                        try {
                            list = Files.readAllLines((new File("workstations.txt")).toPath());
                        } catch (IOException var8) {
                            return data;
                        }

                        int i = 1;
                        Iterator var5 = list.iterator();

                        while(var5.hasNext()) {
                            String address = (String)var5.next();
                            address = address.trim();
                            if (address.length() >= 1 && !address.startsWith("#")) {
                                LabRoom sala = new LabRoom();
                                sala.getComputers().add(new Computer(sala, "Komputer " + i, address, StatusType.UNKNOWN));
                                data = FXCollections.observableArrayList(new LabRoom[]{sala});
                                ++i;
                            }
                        }
                    } catch (Exception var13) {
                        throw new RuntimeException(var13);
                    }
            }

            return data;
        }
    public static void saveData(ObservableList<LabRoom> data) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            File fout = new File("data.json");
            objectMapper.writeValue(fout, data);

        } catch (IOException e) {
            ConnectionHelper.log.error(e.getMessage());
            e.printStackTrace();
        }

    }

}
