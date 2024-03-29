package com.praca.remoteadmin.Connection;


import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

public class ConsoleCaptureInput extends InputStream {
    TextField input = null;
    String str = "";
    int c = 0;
    BlockingQueue<String> q = new LinkedBlockingQueue<String>();
    public ConsoleCaptureInput(TextField input) {
        this.input = input;
        input.clear();

        input.setOnKeyReleased(e -> {
            if (e.getCode().equals(KeyCode.ENTER)) {            //akceptuj komende tylko po kliku na ENTER
                q.add(input.getText()+"\0");
//                try {
//                    //System.in.read((input.getText()+'\n').getBytes(StandardCharsets.UTF_8));
//                } catch (IOException ex) {
//                    throw new RuntimeException(ex);
//                }
                input.setText("");
                str += "";
                c = 0;

            }
        });
    }

    @Override
    public int read() throws IOException {
        if(c >= str.length()) {
            try {
                str = q.take();
                c = 0;
            } catch (InterruptedException e) {
                ConnectionHelper.log.error("Application interrupted during reading in ConsoleCaptureInput object:\n"+e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
        System.out.print(str.charAt(c));
        return str.charAt(c++);
    }
}
