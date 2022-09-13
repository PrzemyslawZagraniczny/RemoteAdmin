package com.praca.remoteadmin.Utils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.praca.remoteadmin.Model.Computer;
import com.praca.remoteadmin.Model.LabRoom;
import com.praca.remoteadmin.Model.StatusType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

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
                    InputStream fileInputStream = null;

                    try {
                        fileInputStream = new FileInputStream("data.json");
                        LabRoom[] sale = (LabRoom[])mapper.readValue(fileInputStream, LabRoom[].class);
                        data = FXCollections.observableArrayList(sale);
                        fileInputStream.close();
                        break;
                    } catch (FileNotFoundException var9) {
                        throw new RuntimeException(var9);
                    } catch (JsonMappingException var10) {
                        throw new RuntimeException(var10);
                    } catch (JsonParseException var11) {
                        throw new RuntimeException(var11);
                    } catch (IOException var12) {
                        throw new RuntimeException(var12);
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
                                sala.getComputers().add(new Computer("Komputer " + i, address, StatusType.UNKNOWN));
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
}
