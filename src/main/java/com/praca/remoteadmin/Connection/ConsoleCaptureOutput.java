package com.praca.remoteadmin.Connection;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputControl;

import java.awt.*;
import java.io.OutputStream;
import java.io.PrintStream;

public class ConsoleCaptureOutput extends OutputStream {
    TextArea txt = null;
    StringBuffer buffer = new StringBuffer();

    public ConsoleCaptureOutput(TextArea ta) {
        this.txt = ta;
        Platform.runLater(() ->{
            txt.setWrapText(true);
            txt.setEditable(false);
            ta.clear();
        });
    }


    public void writeAll(String str) {
        Platform.runLater(() -> {
            txt.appendText(str);
            txt.requestFocus();
            txt.end();
        });
    }

        @Override
    public void write(int b) {
        char c = (char)((b + 256) % 256);
        buffer.append(c);
//        if(c == '$')
        {
            buffer = new StringBuffer();
        }

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                txt.appendText(c+"");
                txt.requestFocus();
                txt.end();
            }
        });
    }

    public void clear() {
        txt.clear();
    }
}