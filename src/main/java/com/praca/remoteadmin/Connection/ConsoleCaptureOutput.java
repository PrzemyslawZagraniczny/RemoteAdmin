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
            synchronized (txt) {
                txt.setWrapText(true);
                txt.setEditable(false);
                txt.clear();
            }
        });
    }

    public void writeAll(String str) {
        buffer.append(str);

    }

    public void printToConsole() {
        Platform.runLater(() -> {
            synchronized (txt) {
                txt.setText(buffer.toString());//txt.appendText(str);
                txt.requestFocus();
                txt.end();
            }
        });
    }

    @Override
    public void write(int b) {
        buffer.append((char)b);
    }

    public void clear() {
        if(txt != null)
            txt.clear();
        buffer = new StringBuffer();
    }
}