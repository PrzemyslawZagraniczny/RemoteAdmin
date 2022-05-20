package com.praca.remoteadmin.GUI;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.concurrent.atomic.AtomicBoolean;

//okienko Alertu do pracy w osobnym wątku
public class MessageBoxTask extends Task<Boolean> {

    private final String str;
    private final Alert.AlertType type;
    private final String sTitle;

    //domyślny alert to OK/Cancel
    public MessageBoxTask(String str) {
        this.str = str;
        this.type = Alert.AlertType.CONFIRMATION;
        this.sTitle = "Uwaga!";
    }

    public MessageBoxTask(String str, String sTitle, Alert.AlertType type) {
        this.str = str;
        this.type = type;
        this.sTitle = sTitle;
    }

    @Override
    protected Boolean call() throws Exception {
        Alert alert = new Alert(type);
        alert.setTitle(sTitle);
        alert.setHeaderText("");
        alert.setContentText(str);
        System.out.println("promptYesNo OK.");
        AtomicBoolean retVal = new AtomicBoolean(false);
        alert.showAndWait().ifPresent(rs -> {
            if (rs == ButtonType.OK) {
                System.out.println("Pressed OK.");
                retVal.set(true);
            } else
                retVal.set(false);
        });

        return retVal.get();
    }
}

