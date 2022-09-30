package com.praca.remoteadmin.Connection;

import com.jcraft.jsch.*;
import com.praca.remoteadmin.Model.Computer;
import com.praca.remoteadmin.Model.StatusType;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.GridPane;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SSH2Connector implements IGenericConnector{

    JSch jsch=new JSch();
    private Session session = null;

    private String sLogin = null;
    private String sPassword = null;

    private int iPort = 22;
    private Computer computer = null;
    private Channel channel = null;

    private ConsoleCaptureOutput out = null;
    private OutputStream err = null;
    public static String sudo_pass = "";

    public static void resetPass() {
        sudo_pass = "";
    }


    @Override
    public boolean openConnection(String sLogin, String sPassword, Computer comp)  {


        this.sLogin = sLogin;
        this.sPassword = sPassword;
        this.computer = comp;

        comp.resetFlags();
        try {
            session = jsch.getSession(sLogin, comp.getAddress(), iPort);
        }catch (NullPointerException e) {
            e.printStackTrace();
            ConnectionHelper.log.error(e.getMessage());
            return false;
        } catch (JSchException e) {
            e.printStackTrace();
            ConnectionHelper.log.error(e.getMessage());
            return false;
        }

        UserInfo ui=new SSHUserInfo();
        session.setUserInfo(ui);

        //pętla do wypełniania progressbar tabelki w czasie nawiązywania połączenia
        TimerTask tt = new TimerHelper(ConnectionHelper.sshConnectionTimeOut);
        java.util.Timer tim = new java.util.Timer();
        tim.schedule(tt, 1000,1000);

        try {
            //session.setTimeout(ConnectionHelper.sshConnectionTimeOut);
            session.connect(ConnectionHelper.sshConnectionTimeOut);
        } catch (JSchException e) {
            tim.cancel();

            Platform.runLater(() -> {
                computer.setProgressStatus(0);
                //computer.setCmdExitStatus(-1);
                String oldStat = computer.getStatus();
                setStatusToErrorMessage(e);
//                new Thread(() -> {
//                    try {
//                        Thread.sleep(5000);
//                        Platform.runLater(() -> computer.setStatus(oldStat));
//                    } catch (InterruptedException ex) {
//                        ConnectionHelper.log.error(ex.getMessage());
//                        ex.printStackTrace();
//                    }
//                }).start();
                // computer.setStat(StatusType.UNKNOWN);
                //computer.setCmdExitStatus(e.getMessage());
            });
            ConnectionHelper.log.error("Connection with <<" + comp.getAddress()+">> failed!");
            ConnectionHelper.log.error(e.getMessage());

            try {
                this.err.write( e.getMessage().getBytes(Charset.forName("UTF-8")));
            } catch (IOException ex) {
                ConnectionHelper.log.error(e.getMessage());
                computer.setCmdExitStatus(110);
                //throw new RuntimeException(ex);
            }
            return false;
        }
        tim.cancel();

        try {

            if( !session.isConnected()) {
                //TODO: zdefiniuj własny wyjątek
                computer.setStat(StatusType.OFFLINE);
                computer.setCmdExitStatus(110);
                //computer.setProgressStatus(0);
                ConnectionHelper.log.error("Connection with <<" + comp.getAddress()+">> failed!");
                //throw new ExceptionInInitializerError("Connection not established!");
            }
            else { //połączenie udane
                computer.setStat(StatusType.CONNECTED);
                computer.clearCmdExitStatus();
                ConnectionHelper.log.info("Successfully connected with <<"+computer.getAddress()+">>");
                return true;
            }
         }catch (NullPointerException e) {
            e.printStackTrace();
            ConnectionHelper.log.error(e.getMessage());
            return false;
        }

        computer.refresh();
        return false;
    }

    @Override
    public void setErrorStream(OutputStream err) {
        this.err = err;
    }

    @Override
    public void setOutputStream(ConsoleCaptureOutput out) {
        this.out = out;
    }


    public static synchronized String setSudoPassword(){
        //aby nie powtarzać dla każdej maszyny
        //synchronized (sudo_pass)
        {
            if (sudo_pass.length() > 0)
                return null;

            javafx.scene.control.Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("RemoteAdmin");
            dialog.setHeaderText("Podaj hasło dla komendy <<sudo>>");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

            PasswordField password = new PasswordField();
            password.setPromptText("Hasło");
            grid.add(new javafx.scene.control.Label("Hasło:"), 0, 1);

            grid.add(password, 1, 1);

            Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.setDisable(true);

            password.textProperty().addListener((observable, oldValue, newValue) -> {
                okButton.setDisable(newValue.trim().isEmpty());
            });

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(btn -> password.getText());

            Optional<String> result = dialog.showAndWait();

            if (result.isPresent()){
                sudo_pass = result.get();

                return sudo_pass.isEmpty()?null:sudo_pass ;
            }
            else
                return null;
        }

    }
    @Override
    public void execCommand(String cmd, CountDownLatch latch) {
        boolean bUnInterrupted = true;
        InputStream in = null;
        int timeOut = 0;
        try {
            //if(channel == null || channel.isClosed())
            if(!session.isConnected())
            {
                latch.countDown();
                return;
            }

            //resetuj progres przed kolejnym zadaniem
            computer.setProgressStatus(0);
            channel = session.openChannel("exec");
            //channel.setInputStream(System.in);
            channel.setInputStream(null);
            //computer.clearBuffer();     //przed poleceniem resetuj bufory zapisu
            computer.clearCmdExitStatus();
            computer.writeAll(">" + cmd + System.lineSeparator());
            computer.refresh();
            ((ChannelExec) channel).setErrStream(computer.getPs(), false);

            timeOut = ConnectionHelper.sshConnectionTimeOut;
            if (!checkIfSudoCommand(cmd)) {
                ((ChannelExec) channel).setCommand(cmd);

                ((ChannelExec) channel).setOutputStream(computer.getPs());
                //((ChannelExec) channel).setOutputStream(null);
                channel.connect(timeOut);

            } else {
                timeOut = ConnectionHelper.sudoConnectionTimeOut;
            }

            channel.setInputStream(null);
            in = channel.getInputStream();
            final int sleepTm = 100;
            byte[] tmp=new byte[1024];
            while(bUnInterrupted){
                try {

                    while(in.available()>0){
                        //wchodzimy tutaj gdy skrypt pracuje interaktywnie
                        int i=in.read(tmp, 0, 1024);
                        if(i<0)break;
                        computer.write(tmp, i);    //zapisuj do bufora konkretnego komputera a nie do TextArea
                        //System.out.println("<<"+computer.getAddress()+">>");
                        //System.out.write(tmp, 0 , i);
                        computer.refresh();
                    }
                    if(channel.isClosed()){
                        if(in.available()>0) continue;
                        //dodaj znak nowej lini na koniec
                        //computer.writeAll(System.lineSeparator());    //zapisuj do bufora konkretnego komputera a nie do TextArea
                        //wpisujemy do tabelki wyjściowy exit status dla polecenia
                        computer.setCmdExitStatus(channel.getExitStatus());
                        ConnectionHelper.log.info("Querrying <<"+computer.getAddress()+">> exit-status:" + channel.getExitStatus());
                        latch.countDown();
                        break;
                    }
                    if(computer.isAborted()) {
                        latch.countDown();
                        String sOut = "Task aborted by User";
                        if(computer.getStat() != StatusType.CONNECTED)
                            sOut = "Task Aborted. Computer Off-line";
                        ConnectionHelper.log.error(sOut+".");
                        channel.disconnect();

                        computer.setCmdExitStatus(sOut);
                        computer.setProgressStatus(0);
                        computer.resetFlags();
                        //computer.setStat(StatusType.DISCONNECTED);
                        return;
                    }
                    if(computer.isBg()) {
                        latch.countDown();
                        computer.setProgressStatus(0);
                        ConnectionHelper.log.error("Task put to Bg by User.");
                        computer.resetFlags();
                        //computer.setCmdExitStatus("");
                        return;
                    }

                    Thread.sleep(sleepTm);
                    //computer.setProgressStatus(computer.getProgressStatus() + sleepTm/(double)timeOut * (-1.0 * dir));
                    computer.setProgressStatus(-1);
                }catch (NullPointerException e) {
//                    e.printStackTrace();
                    ConnectionHelper.log.error(e.getMessage());
                    latch.countDown();
                    bUnInterrupted = false;
                    computer.setProgressStatus(0);
                    if (channel != null)
                        computer.setCmdExitStatus(channel.getExitStatus());
                } catch (IOException e) {
                    ConnectionHelper.log.error(e.getMessage());
                    e.printStackTrace();
                    computer.setProgressStatus(0);
                    computer.setCmdExitStatus(110);
                    latch.countDown();
                    bUnInterrupted = false;
                    if (channel != null)
                        computer.setCmdExitStatus(channel.getExitStatus());
                } catch (InterruptedException e) {
                    ConnectionHelper.log.error(e.getMessage());
                    e.printStackTrace();
                    latch.countDown();
                    bUnInterrupted = false;
                    computer.setProgressStatus(0);
                    computer.setCmdExitStatus(-1);
                    if (channel != null)
                        computer.setCmdExitStatus(channel.getExitStatus());
                }
            }
        } catch (JSchException e) {
            ConnectionHelper.log.error(e.getMessage());
            computer.setProgressStatus(0);
            computer.setCmdExitStatus(e.getMessage());
            setStatusToErrorMessage(e);
            e.printStackTrace();
            latch.countDown();
            return;
        } catch (IOException e) {
            ConnectionHelper.log.error(e.getMessage());
            computer.setProgressStatus(0);
            computer.setCmdExitStatus(-1);
            e.printStackTrace();
            latch.countDown();
            return;
        }




        computer.setProgressStatus(100);
        if(computer != null) {
            if (channel != null) {
                computer.setCmdExitStatus(channel.getExitStatus());
                if(channel.getExitStatus() != 0)
                    computer.setProgressStatus(0);
            }

            computer.refresh();
        }

    }

    private void setStatusToErrorMessage(JSchException e) {
        if(e.getLocalizedMessage().split(":").length>1)
            computer.setStatus(e.getLocalizedMessage().split(":")[1]);
        else
            computer.setStatus(e.getLocalizedMessage());
    }

    //sprawdz czy to jest komenda sudo
    private boolean checkIfSudoCommand(String cmd) {


        boolean bRet = false;
        cmd = cmd.trim();

        //jeśli zdefinoowano lokalny program typu sudo to wzorzec "zostawi go w spokoju"
        Pattern pattern = Pattern.compile("^(usr/bin/sudo |sudo )(.*)");
        Matcher match = pattern.matcher(cmd);
        if(match.find())
        {
            if(match.groupCount() != 2) {
                ConnectionHelper.log.error("Nie rozpoznane polecenie typu SUDO.");
            }
            else
                cmd = match.group(2);
            bRet = true;
        }
        else
            return false;

        ((ChannelExec)channel).setCommand("sudo -S -p '' "+cmd);
        channel.setOutputStream(null);
        try {
            channel.connect(ConnectionHelper.sshConnectionTimeOut);
        } catch (JSchException e) {
            ConnectionHelper.log.error(e.getMessage());
            e.printStackTrace();
        }
        passArgs(sudo_pass);

        return bRet;
    }

    @Override
    public void disconnect() {
        try {
            if(session.isConnected())
                session.disconnect();

            session = null;
            channel = null;
            //computer.clearBuffer();         //usuń historię wyników z poleceń
            computer.setStat(StatusType.DISCONNECTED);
            computer.setProgressStatus(0);
            ConnectionHelper.log.info("Successfully disconnected from <<"+computer.getAddress()+">>");
        }catch (NullPointerException e) {
            e.printStackTrace();
            ConnectionHelper.log.error(e.getMessage());
        }
    }

    @Override
    public void passArgs(String args) {
        try {

            OutputStream out = channel.getOutputStream();

            out.write((args+"\n").getBytes());
            out.flush();

        } catch (IOException e) {
            ConnectionHelper.log.error(e.getMessage());
        }
    }

    @Override
    public boolean isOpened() {
        if(session != null)
            return session.isConnected();
        return false;
    }


    public class SSHUserInfo implements UserInfo, UIKeyboardInteractive {
        public String getPassword(){ return passwd; }


        public boolean promptYesNo(String str){
            return true;
        }

        String passwd;
        JTextField passwordField=(JTextField)new JPasswordField(20);

        public String getPassphrase(){ return null; }
        public boolean promptPassphrase(String message){ return true; }
        public boolean promptPassword(String message){
            passwd = sPassword;
            return true;
        }
        public void showMessage(String message){
            JOptionPane.showMessageDialog(null, message);
        }
        final GridBagConstraints gbc =
                new GridBagConstraints(0,0,1,1,1,1,
                        GridBagConstraints.NORTHWEST,
                        GridBagConstraints.NONE,
                        new Insets(0,0,0,0),0,0);
        private Container panel;
        public String[] promptKeyboardInteractive(String destination,
                                                  String name,
                                                  String instruction,
                                                  String[] prompt,
                                                  boolean[] echo){
            panel = new JPanel();
            panel.setLayout(new GridBagLayout());

            gbc.weightx = 1.0;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.gridx = 0;
            panel.add(new JLabel(instruction), gbc);
            gbc.gridy++;

            gbc.gridwidth = GridBagConstraints.RELATIVE;

            JTextField[] texts=new JTextField[prompt.length];
            for(int i=0; i<prompt.length; i++){
                gbc.fill = GridBagConstraints.NONE;
                gbc.gridx = 0;
                gbc.weightx = 1;
                panel.add(new JLabel(prompt[i]),gbc);

                gbc.gridx = 1;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.weighty = 1;
                if(echo[i]){
                    texts[i]=new JTextField(20);
                }
                else{
                    texts[i]=new JPasswordField(20);
                }
                panel.add(texts[i], gbc);
                gbc.gridy++;
            }
//            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//            alert.setTitle("Uwaga!");
//            alert.setHeaderText(destination+": "+name);
//            //alert.setContentText("Maszyna wróciła status wykonania polecenia <<"+future.get().getCmdExitStatus()+">>");
//            alert.show();

            if(JOptionPane.showConfirmDialog(null, panel,
                    destination+": "+name,
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE)
                    ==JOptionPane.OK_OPTION){
                String[] response=new String[prompt.length];
                for(int i=0; i<prompt.length; i++){
                    response[i]=texts[i].getText();
                }
                return response;
            }
            else{
                return null;  // cancel
            }
        }
    }
    private class TimerHelper extends TimerTask {
        double i = 0;
        double maxTime = 0;    //max czas oczeiwania na połączenie w [ms]

        public TimerHelper(int shhConnectionTimeOut) {
            maxTime = shhConnectionTimeOut;
        }

        public void run() {
            i += 1000;
            computer.setProgressStatus(i/maxTime);
            //System.out.println("Wartość: "+(i/maxTime));
        }
    }
}
