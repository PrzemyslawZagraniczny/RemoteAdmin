package com.praca.remoteadmin.Connection;

import com.jcraft.jsch.*;
import com.praca.remoteadmin.Model.Computer;
import com.praca.remoteadmin.Model.StatusType;
import javafx.scene.control.Alert;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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


    @Override
    public boolean openConnection(String sLogin, String sPassword, Computer comp) throws JSchException {


        this.sLogin = sLogin;
        this.sPassword = sPassword;
        this.computer = comp;

        try {
            session = jsch.getSession(sLogin, comp.getAddress(), iPort);
        }catch (NullPointerException e) {
            //TODO:
            //jakiś bardziej czytelny komunikat może
            System.err.println(e.getMessage());
        }

        UserInfo ui=new SSHUserInfo();
        session.setUserInfo(ui);
        session.connect();


        try {
            if( !session.isConnected()) {
                //TODO: zdefiniuj własny wyjątek
                computer.setStat(StatusType.OFFLINE);
                throw new ExceptionInInitializerError("Połączenie nie zostało ustanowione!");
            }
            computer.setStat(StatusType.ACTIVE);


        }catch (NullPointerException e) {
            //TODO: jakiś bardziej czytelny komunikat może
            System.err.println("Najpier trzeba wywołać metodę <<openConnection>>");
            System.err.println(e.getMessage());
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
                    System.out.println("exit-status: "+channel.getExitStatus());
                    break;
                }
                try{Thread.sleep(100);}catch(Exception ee){
                    ee.printStackTrace();
                }
            }
        }catch (NullPointerException e) {
            //TODO: jakiś bardziej czytelny komunikat może
            System.err.println("Najpierw trzeba wywołać metodę <<openConnection>>");
            System.err.println(e.getMessage());
        } catch (JSchException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
        }catch (NullPointerException e) {
            System.err.println(e.getMessage());
        }
    }

    public class SSHUserInfo implements UserInfo, UIKeyboardInteractive {
        public String getPassword(){ return passwd; }
        public boolean promptYesNo(String str){
            Object[] options={ "Tak", "Nie" };
            int foo= JOptionPane.showOptionDialog(null,
                    str,
                    "Uwaga",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
            return foo==0;
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
}
