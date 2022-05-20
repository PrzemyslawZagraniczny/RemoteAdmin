package com.praca.remoteadmin.GUI;

import javafx.application.Platform;
import javafx.scene.control.Button;

public class ButtonStateObserver  {
    Button btn;

    public ButtonStateObserver(Button btn) {
        this.btn = btn;
    }
    public void update(){

        Platform.runLater(() -> {
            synchronized (btn) {    //zapewni indywidualny dostęp do działania
                btn.setDisable(false);
            }
        });
    }
}
