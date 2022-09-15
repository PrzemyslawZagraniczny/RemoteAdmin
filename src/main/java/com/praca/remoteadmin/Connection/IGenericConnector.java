package com.praca.remoteadmin.Connection;

import com.jcraft.jsch.JSchException;
import com.praca.remoteadmin.Model.Computer;

import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;

public interface IGenericConnector {
    boolean openConnection(String sLogin, String sPassword, Computer comp) throws JSchException;
    void setErrorStream(OutputStream out);
    void setOutputStream(ConsoleCaptureOutput out);
    void execCommand(String cmd, CountDownLatch latch);
    void disconnect();
}
