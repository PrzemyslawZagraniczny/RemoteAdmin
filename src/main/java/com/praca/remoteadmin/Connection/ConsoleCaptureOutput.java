package com.praca.remoteadmin.Connection;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputControl;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class ConsoleCaptureOutput extends OutputStream {
    TextArea txt = null;
    //StringBuffer buffer = new StringBuffer();
    ByteArrayOutputStream bs = new ByteArrayOutputStream(1000);

    public boolean checkForPass(String s1) {
        int indx = bs.toString().lastIndexOf(s1);
        if( indx == bs.toString().length() - s1.length()) { //upewnij się, że s1 jest na końcu stringu
            return true;
        }
        return false;
    }
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
        try {
            bs.write(str.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void printToConsole() {
        Platform.runLater(() -> {
            synchronized (txt) {
                txt.setText(bs.toString());//txt.appendText(str);
                txt.requestFocus();
                txt.end();
            }
        });
    }

    @Override
    public void write(int b) {
        bs.write(b);
    }

    public void clear() {
        if(txt != null)
            txt.clear();
        bs = new ByteArrayOutputStream(1024*10);
    }

    public String getString() {
        return bs.toString();
    }
}