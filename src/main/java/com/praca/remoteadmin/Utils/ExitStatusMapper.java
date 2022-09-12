package com.praca.remoteadmin.Utils;

import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExitStatusMapper {
    private static Map<Integer, String> exitStatusMap = new HashMap<>();
    private static ExitStatusMapper instance = new ExitStatusMapper();
    private ExitStatusMapper() {
        List<String> list = null;

        try {
            list = Files.readAllLines(new File("exitCodes.csv").toPath());
        } catch (IOException ex) {
        }
        for(String linia : list) {
            String []para = linia.split(";");
            exitStatusMap.put(Integer.parseInt(para[0]), para[1]);
        }
        exitStatusMap.put(-1, "FAILED");
    }
    public static String fromExitCode(int code) {
        if(exitStatusMap.containsKey(code))
            return exitStatusMap.get(code);
        else
            return "Nieznany kod";
    }

}
