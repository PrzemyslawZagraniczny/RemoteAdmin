package com.praca.remoteadmin.Connection;

import com.jcraft.jsch.*;
import com.praca.remoteadmin.Model.Computer;
import com.praca.remoteadmin.Model.StatusType;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
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
    private CountDownLatch latch = null;


    @Override
    public boolean openConnection(String sLogin, String sPassword, Computer comp)  {


        this.sLogin = sLogin;
        this.sPassword = sPassword;
        this.computer = comp;

        try {
            session = jsch.getSession(sLogin, comp.getAddress(), iPort);
        }catch (NullPointerException e) {
            //TODO: jakiś bardziej czytelny komunikat może
            System.err.println(e.getMessage());
            ConnectionHelper.log.error(e.getMessage());
        } catch (JSchException e) {
            ConnectionHelper.log.error(e.getMessage());
            throw new RuntimeException(e);
        }

        UserInfo ui=new SSHUserInfo();
        session.setUserInfo(ui);

        TimerTask tt = new TimerHelper(ConnectionHelper.shhConnectionTimeOut);
        java.util.Timer tim = new java.util.Timer();
        tim.schedule(tt, 0,1000);

        try {
            session.setTimeout(ConnectionHelper.shhConnectionTimeOut);
            session.connect();
        } catch (JSchException e) {
            tim.cancel();
            computer.setStat(StatusType.OFFLINE);
            computer.setProgressStatus(0);
            ConnectionHelper.log.error("Connection with <<" + comp.getAddress()+">> failed!");
            ConnectionHelper.log.error(e.getMessage());
            try {
                this.err.write( e.getMessage().getBytes(Charset.forName("UTF-8")));
            } catch (IOException ex) {
                ConnectionHelper.log.error(e.getMessage());
                throw new RuntimeException(ex);
            }
            return false;
        }
        tim.cancel();

        try {
            if( !session.isConnected()) {
                //TODO: zdefiniuj własny wyjątek
                computer.setStat(StatusType.OFFLINE);
                computer.setProgressStatus(0);
                ConnectionHelper.log.error("Connection with <<" + comp.getAddress()+">> failed!");
                //throw new ExceptionInInitializerError("Connection not established!");
            }
            computer.setStat(StatusType.CONNECTED);
            ConnectionHelper.log.info("Successfully connected with <<"+computer.getAddress()+">>");
        }catch (NullPointerException e) {
            //TODO: jakiś bardziej czytelny komunikat może
            System.err.println("First call method <<openConnection>>");
            System.err.println(e.getMessage());
            ConnectionHelper.log.error("First call method <<openConnection>>");
            ConnectionHelper.log.error(e.getMessage());
            return channel.isConnected();
        }

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

    @Override
    public void execCommand(String cmd) {
        new Thread(() -> exec(cmd)).start();
    }

    private void exec(String cmd) {
        try {
            channel=session.openChannel("exec");
            ((ChannelExec)channel).setCommand(cmd);
            channel.setInputStream(null);
            ((ChannelExec)channel).setErrStream(err);
//            ((ChannelExec)channel).setOutputStream(out);
            InputStream in=channel.getInputStream();
            this.out.writeAll(">"+cmd+System.lineSeparator());

            channel.connect();
            byte[] tmp=new byte[1024];
            while(true){
                while(in.available()>0){
                    int i=in.read(tmp, 0, 1024);
                    if(i<0)break;
                    this.out.writeAll(new String(tmp));
                }
                if(channel.isClosed()){
                    if(in.available()>0) continue;
                    //dodaj znak nowej lini na koniec
                    this.out.writeAll(new String(System.lineSeparator()));
                    //wpisujemy do tabelki wyjściowy exit status dla polecenia
                    computer.setCmdExitStatus(channel.getExitStatus());
                    ConnectionHelper.log.info("Querrying <<"+computer.getAddress()+">> exit-status:" + channel.getExitStatus());
                    latch.countDown();
                    break;
                }
                try{Thread.sleep(100);}catch(Exception ee){
                    ConnectionHelper.log.error(ee.getMessage());
                    ee.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        }catch (NullPointerException e) {
            //TODO: jakiś bardziej czytelny komunikat może
            System.err.println(e.getMessage());
            ConnectionHelper.log.error("First call method <<openConnection>>");
            ConnectionHelper.log.error(e.getMessage());
        } catch (JSchException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {

        }finally {
            channel.disconnect();
        }
    }

    @Override
    public void disconnect() {
        try {
            session.disconnect();
            session = null;
            channel = null;
            computer.setStat(StatusType.OFFLINE);
            computer.setProgressStatus(0);
            ConnectionHelper.log.info("Successfully disconnected from <<"+computer.getAddress()+">>");
        }catch (NullPointerException e) {
            System.err.println(e.getMessage());
            ConnectionHelper.log.error(e.getMessage());
        }
    }

    public void execCommand(String cmd, CountDownLatch latch) {
        this.latch = latch;
        execCommand(cmd);
    }

    public class SSHUserInfo implements UserInfo, UIKeyboardInteractive {
        public String getPassword(){ return passwd; }


        public boolean promptYesNo(String str){
            if(checkIfIgnore(str))  //sprawdza czy ignorowac komunikat
                return true;
            Object[] options={ "Tak", "Nie", "Tak (Ignoruj przyszłe)" };
            int retVal= JOptionPane.showOptionDialog(null,
                    str,
                    "Uwaga!",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
            if(retVal == 2) {
                //tzn zapisz komunikat do ignorowania w przyszłości
                //TODO: THREAD Safe Alert!!!!
                //ConnectionHelper.bRSAKeyFingerprintIgnore = true;
            }
            ConnectionHelper.log.warn(str+" ANSWER"+((retVal == 2 || retVal==0)?"Yes":"No"));
            return retVal==0 || retVal == 2;
            //TODO: Alert działa na osobnym procesie przez co nie może wrócić natychmiastowo wyniku zadbaj aby to zsynchronizować

//            ExecutorService executorService = Executors.newFixedThreadPool(1);
//            MessageBoxTask task = new MessageBoxTask(str);
//            executorService.execute(task);
//            boolean retVal = true;
//            try {
//                retVal = task.get();
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            } catch (ExecutionException e) {
//                throw new RuntimeException(e);
//            }
//            executorService.shutdown();

//            Platform.runLater(new Runnable() {
//                @Override
//                public void run() {
//                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//                    alert.setTitle("Uwaga!");
//                    alert.setHeaderText("");
//                    alert.setContentText(str);
//                    System.out.println("promptYesNo OK.");
//                    AtomicBoolean retVal = new AtomicBoolean(false);
//                    alert.showAndWait().ifPresent(rs -> {
//                        if (rs == ButtonType.OK) {
//                            System.out.println("Pressed OK.");
//                            retVal.set(true);
//                        } else
//                            retVal.set(false);
//                    });
//                    System.out.println("return OK.");
//                    //return retVal.get();                }
//                }
//            });
        }

        private boolean checkIfIgnore(String str) {

            if(ConnectionHelper.bRSAKeyFingerprintIgnore) {
                String regex = "The authenticity of host '(.)*' can't be established.\n" +
                        "RSA key fingerprint is (.)*.\n" +          //([a-f]|[0-9]|:)
                        "Are you sure you want to continue connecting?";

                Pattern r = Pattern.compile(regex);

                // Now create matcher object.
                Matcher m = r.matcher(str);

                if (m.find())
                    return true;
            }
            return false;
        }

        String passwd;
        JTextField passwordField=(JTextField)new JPasswordField(20);

        public String getPassphrase(){ return null; }
        public boolean promptPassphrase(String message){ return true; }
        public boolean promptPassword(String message){
            passwd = sPassword;
            return true;
//            Object[] ob={passwordField};
//            int result=
//                    JOptionPane.showConfirmDialog(null, ob, message,
//                            JOptionPane.OK_CANCEL_OPTION);
//            if(result==JOptionPane.OK_OPTION){
//                passwd=passwordField.getText();
//                return true;
//            }
//            else{
//                return false;
//            }
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
            System.out.println("Wartość: "+(i/maxTime));
        }
    }
}
