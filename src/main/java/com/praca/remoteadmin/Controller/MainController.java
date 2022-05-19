package com.praca.remoteadmin.Controller;

import com.jcraft.jsch.JSchException;
import com.praca.remoteadmin.Connection.ConnectionHelper;
import com.praca.remoteadmin.Connection.ConsoleCaptureOutput;
import com.praca.remoteadmin.Connection.SSH2Connector;
import com.praca.remoteadmin.Model.CmdType;
import com.praca.remoteadmin.Model.Computer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

public class MainController {
    @FXML
    public MenuItem btQuit;

    final Set<CommandCallable> sshSessions = new HashSet<CommandCallable>();
    @FXML
    public TableColumn<Computer, String> statusCol;
    @FXML
    public TableColumn<Computer,String> addressCol;
    @FXML
    public TableColumn<Computer, Boolean> selectCol;
    @FXML
    public TableColumn<Computer, Integer> cmdStatCol;
    @FXML
    public TableColumn<Computer,Double> progressCol;
    @FXML
    public TableView<Computer> table;
    @FXML
    public PasswordField passwordField;
    @FXML
    public TextField loginField;
    public TextArea consoleOutput;
    public TextField cmdLine;

    public Button btConnect;



    public void onQuit(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void onSelectedAction(Event event) {
    }
    @FXML
    public void initialize() {
        consoleOutput.autosize();
        cmdLine.setText(ConnectionHelper.defaultCommand);
        loginField.setText(ConnectionHelper.defaultLogin);
        passwordField.setText(ConnectionHelper.defaultPassword);


        statusCol.setCellValueFactory(
                new PropertyValueFactory<Computer, String>("status")
        );
        selectCol.setCellFactory(
                new Callback<TableColumn<Computer,Boolean>,TableCell<Computer,Boolean>>(){
                    @Override public
                    TableCell<Computer,Boolean> call( TableColumn<Computer,Boolean> p ){
                        return new CheckBoxTableCell<>();
                    }
                });
        progressCol.setCellFactory(
                new Callback<TableColumn<Computer,Double>,TableCell<Computer,Double>>(){
                    @Override public
                    TableCell call( TableColumn p ){
                        return new ProgressBarTableCell<>();
                    }
                });

        addressCol.setCellValueFactory(
                new PropertyValueFactory<>("address")
        );

        selectCol.setCellValueFactory(
                new PropertyValueFactory<>("selected")
        );
        cmdStatCol.setCellValueFactory(
                new PropertyValueFactory<>("cmdExitStatus")
        );
        progressCol.setCellValueFactory(
                new PropertyValueFactory<>("progressStatus")
        );


        table.getItems().addAll(ConnectionHelper.getComputers());
    }
    @FXML
    public void OnLogingIn(ActionEvent actionEvent) {
        String sLogin = loginField.getText().trim();
        String sPassword = passwordField.getText().trim();
        //TODO:
        //szyfruj hasło i login zaraz po przejęciu od użytkownika oraz zeruj ich wartości w polach
    }

    public void onExecuteCommand(ActionEvent actionEvent)  {

        //TODO: Pilnie zmień kod. Połaczenie musi być wykonywane osobno

//        data.get(1).setStat(StatusType.ACTIVE);
//        if( true) return;
        execParallel(CmdType.SENDING_CMD);
    }

    private void execParallel(CmdType cmdType) {
        new Thread(() -> exec(cmdType)).start();
    }


    private void exec(CmdType cmdType) {
        for (CommandCallable comp:sshSessions) {
            comp.setCmdType(cmdType);
        }
        ExecutorService executorService = Executors.newFixedThreadPool(sshSessions.size());
        List<Future<Computer>> futures = null;
        try {
            futures = executorService.invokeAll(sshSessions);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

//        for(Future<Computer> future : futures){
//            try {
//                if(future.get().getCmdExitStatus() != 0) {
//                    Alert alert = new Alert(Alert.AlertType.ERROR);
//                    alert.setTitle("Uwaga!");
//                    alert.setHeaderText(future.get().getAddress());
//                    alert.setContentText("Maszyna wróciła status wykonania polecenia <<"+future.get().getCmdExitStatus()+">>");
//                    alert.show();
//                }
//
//
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            } catch (ExecutionException e) {
//                throw new RuntimeException(e);
//            }
//        }

        switch (cmdType) {
            case DISCONNECTING:         //posprzątaj ale dopiero po zamknięciu wszystkich połączeń
                for (CommandCallable comp:sshSessions) {
                    comp.comp.setProgressStatus(0);
                }
                sshSessions.clear();
                btConnect.setDisable(!true);
                break;
            case CONNECTING:
                btConnect.setDisable(!true);
                break;
            case NONE:
            default:
                break;
        }

        System.out.println("KONIEC!!!!");

        executorService.shutdown();
    }

    public void onConsolClear(ActionEvent actionEvent) {
        consoleOutput.clear();
    }

    public void onConect(ActionEvent actionEvent) {

        btConnect.setDisable(true);
        if(sshSessions.size() > 0) {    //tzn. jesteśmy połączeni

            btConnect.setText("Połącz");

            //najpier pozamykacj wszystkie połączenia
            execParallel(CmdType.DISCONNECTING);

            return;
        }
        btConnect.setText("Rozłącz");
        for (Computer comp: ConnectionHelper.getComputers()) {
            sshSessions.add(new CommandCallable(comp, loginField.getText(), passwordField.getText()));
        }
        execParallel(CmdType.CONNECTING);

    }

    //TODO: klasę Callable do utworzenia połączenia
    //klasa do rónoległego wykonywania poleceń SSH na każdej z maszyn zdalnych
    class CommandCallable implements Callable<com.praca.remoteadmin.Model.Computer> {
        private com.praca.remoteadmin.Model.Computer comp;

        private String pass;
        private String login;
        private SSH2Connector conn = null;

        private CmdType cmdType = CmdType.NONE;

        public CmdType getCmdType() {
            return cmdType;
        }

        public void setCmdType(CmdType cmdType) {
            this.cmdType = cmdType;
        }

        public CommandCallable(Computer comp, String login, String pass) {
            this.comp = comp;
            this.pass = pass;
            this.login = login;
        }



        private void connect() {
            if(conn == null) {
                conn = new SSH2Connector();

                    conn.openConnection(login, pass,  this.comp);
                    conn.setErrorStream(System.err);
                    conn.setOutputStream(new ConsoleCaptureOutput(consoleOutput));
                    //conn.setOutputStream(System.out);

            }
        }

        @Override
        public com.praca.remoteadmin.Model.Computer call() throws Exception {
            switch (cmdType) {
                case CONNECTING:
                    connect();
                    break;
                case SENDING_CMD:
                    sndCommand();
                    break;
                case DISCONNECTING:
                    disconnect();
                    break;
                case NONE:
                default:

            }


            return comp;
        }


        //wysyłanie komendy
        private boolean sndCommand() {
            if(!comp.isSelected())
                return true;

            try {

                String cmd = cmdLine.getText().trim();
                conn.execCommand(cmd);
            } catch (Exception e) {
                System.err.println(e.getMessage());
                throw new RuntimeException(e);
            }
            return false;
        }

        public void disconnect() {
            conn.disconnect();
        }
    }
}
