package com.praca.remoteadmin.Controller;

import com.praca.remoteadmin.Connection.ConnectionHelper;
import com.praca.remoteadmin.Connection.ConsoleCaptureOutput;
import com.praca.remoteadmin.Connection.SSH2Connector;
import com.praca.remoteadmin.GUI.MessageBoxTask;
import com.praca.remoteadmin.Model.CmdType;
import com.praca.remoteadmin.Model.Computer;
import com.praca.remoteadmin.Model.StatusType;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.util.Comparator;
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
    public Button btnExecCmd;
    public TabPane tabPane;


    public void onQuit(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void onSelectedAction(Event event) {
        //TODO: Zaimplementuj
        throw new UnsupportedOperationException();
    }

    @FXML
    public void initialize() {
        btnExecCmd.setDisable(true);
        tabPane.getSelectionModel().select(1);      //ustaw domy??lnie drug?? zak??adk?? na starcie
        consoleOutput.autosize();
        cmdLine.setText(ConnectionHelper.defaultCommand);
        loginField.setText(ConnectionHelper.defaultLogin);
        passwordField.setText(ConnectionHelper.defaultPassword);

        //powi??zanie kolumn tabelki z properties dla obiektu Computer

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
        //szyfruj has??o i login zaraz po przej??ciu od u??ytkownika oraz zeruj ich warto??ci w polach
    }

    public void onExecuteCommand(ActionEvent actionEvent)  {

        //TODO: Pilnie zmie?? kod. Po??aczenie musi by?? wykonywane osobno

//        data.get(1).setStat(StatusType.ACTIVE);
//        if( true) return;
        btnExecCmd.setDisable(true);

        execParallel(CmdType.SENDING_CMD);
    }

    private void execParallel(CmdType cmdType) {
        new Thread(() -> exec(cmdType)).start();
    }


    private void exec(CmdType cmdType) {
        Set<CommandCallable> sshMachines = sshSessions;
        int cntSelected = 0;
        synchronized (sshMachines) {        //synchronizuj na maszynach (gdyby kto?? pr??bowa?? klikn???? checkboxa obok maszyny)
            for (CommandCallable comp : sshMachines) {
                if (comp.comp.isSelected()) {
                    comp.setCmdType(cmdType);
                    cntSelected++;
                }
            }

            CountDownLatch latch = new CountDownLatch(cntSelected);
            ExecutorService executorService = Executors.newFixedThreadPool(cntSelected);
            List<Future<Computer>> futures = null;

            for (CommandCallable comp : sshMachines) {
                if (comp.comp.isSelected()) {
                    comp.setCmdType(cmdType);
                    comp.setLatch(latch);
                }
            }

            try {
                futures = executorService.invokeAll(sshMachines);
            } catch (InterruptedException e) {
                ConnectionHelper.log.error(e.getMessage());
                Thread.currentThread().interrupt();
            }

//        for(Future<Computer> future : futures){
//            try {
//                if(future.get().getCmdExitStatus() != 0) {
//                    Alert alert = new Alert(Alert.AlertType.ERROR);
//                    alert.setTitle("Uwaga!");
//                    alert.setHeaderText(future.get().getAddress());
//                    alert.setContentText("Maszyna wr??ci??a status wykonania polecenia <<"+future.get().getCmdExitStatus()+">>");
//                    alert.show();
//                }
//
//
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            } catch (ExecutionException e) {
//                Thread.currentThread().interrupt();
//            }
//        }
            try {
                latch.await();
            } catch (InterruptedException e) {
                ConnectionHelper.log.error(e.getMessage());
                Thread.currentThread().interrupt();
            }
            switch (cmdType) {
                case DISCONNECTING:         //posprz??taj ale dopiero po zamkni??ciu wszystkich po????cze??
                    for (CommandCallable comp : sshMachines) {
                        comp.comp.setProgressStatus(0);
                    }
                    sshSessions.clear();

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            btnExecCmd.setDisable(true);
                            btConnect.setDisable(!true);
                        }
                    });

                    ConnectionHelper.log.info("Disconnected from <<"+cntSelected+">> hosts.");
                    break;
                case CONNECTING: {
                    cntSelected = 0;
                    int cntConnected = 0;

                    synchronized (sshSessions) {        //synchronizuj na maszynach (gdyby kto?? pr??bowa?? klikn???? checkboxa obok maszyny)
                            for (CommandCallable comp : sshSessions) {
                                if (comp.comp.isSelected()) {
                                    cntSelected++;
                                }
                                if ( comp.comp.getStat() == StatusType.CONNECTED)
                                    cntConnected++;
                            }
                        }
                        ConnectionHelper.log.info("Established connection to " + cntConnected+"/"+cntSelected + " hosts.");
                        if(cntConnected <= 0) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    btConnect.setDisable(!true);
                                    btConnect.setText("Po????cz");
                                    new Thread(new MessageBoxTask("Uwaga")).start();
                                }
                            });
                        }
                        else {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    btConnect.setDisable(!true);
                                    btnExecCmd.setDisable(!true);
                                }
                            });
                        }
                    }
                    break;
                case SENDING_CMD:
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            btnExecCmd.setDisable(!true);                        }
                    });
                    break;
                case NONE:
                default:
                    break;
            }
            executorService.shutdown();
        }
    }

    //klik na przycisk "Czy???? konsol??"
    public void onConsolClear(ActionEvent actionEvent) {
        consoleOutput.clear();
    }

    //klik na przycisk "Po????cz"
    public void onConect(ActionEvent actionEvent) {

        btConnect.setDisable(true);
        if(sshSessions.size() > 0) {    //tzn. jeste??my po????czeni
            btConnect.setText("Po????cz");
            //najpierw pozamyka?? wszystkie po????czenia
            execParallel(CmdType.DISCONNECTING);
            return;
        }
        btConnect.setText("Roz????cz");

        for (Computer comp: ConnectionHelper.getComputers()) {
            sshSessions.add(new CommandCallable(comp, loginField.getText(), passwordField.getText()));
        }
        ConnectionHelper.log.info("Trying to establish connection with hosts...");
        execParallel(CmdType.CONNECTING);
    }

    //TODO: klas?? Callable do utworzenia po????czenia
    //klasa do r??noleg??ego wykonywania polece?? SSH na ka??dej z maszyn zdalnych
    class CommandCallable implements Callable<com.praca.remoteadmin.Model.Computer> {
        private com.praca.remoteadmin.Model.Computer comp;
        private CountDownLatch latch;

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
                conn.setErrorStream(System.err);
                conn.setOutputStream(new ConsoleCaptureOutput(consoleOutput));
                boolean ret = conn.openConnection(login, pass,  this.comp);
                latch.countDown();
            }
        }

        @Override
        public com.praca.remoteadmin.Model.Computer call() throws Exception {
            switch (cmdType) {
                case CONNECTING:
                    ConnectionHelper.log.info("Connecting to <<"+comp.getAddress()+">>...");
                    connect();
                    break;
                case SENDING_CMD:
                    ConnectionHelper.log.info("Querrying <<"+comp.getAddress()+">>...");
                    sndCommand();
                    break;
                case DISCONNECTING:
                    ConnectionHelper.log.info("Disconnecting with <<"+comp.getAddress()+">>...");
                    disconnect();
                    break;
                case NONE:
                default:

            }


            return comp;
        }


        //wysy??anie komendy na zdarzenie kliku na przycisk >>
        private synchronized boolean sndCommand() {
            if(!comp.isSelected())
                return true;
            try {
                String cmd = cmdLine.getText().trim();
                conn.execCommand(cmd, latch);
            } catch (Exception e) {
                ConnectionHelper.log.error(e.getMessage());
                Thread.currentThread().interrupt();
            }
            return false;
        }

        public void disconnect() {
            conn.disconnect();
            latch.countDown();
        }

        public void setLatch(CountDownLatch latch) {
            this.latch = latch;
        }
    }
}
