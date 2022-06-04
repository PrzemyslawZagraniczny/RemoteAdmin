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
        tabPane.getSelectionModel().select(1);      //ustaw domyślnie drugą zakładkę na starcie
        consoleOutput.autosize();
        cmdLine.setText(ConnectionHelper.defaultCommand);
        loginField.setText(ConnectionHelper.defaultLogin);
        passwordField.setText(ConnectionHelper.defaultPassword);

        //powiązanie kolumn tabelki z properties dla obiektu Computer

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

        table.setRowFactory(tv -> {
            TableRow<Computer> row = new TableRow<>();

            // use EasyBind to access the valueProperty of the itemProperty of the cell:
//            row.disableProperty().bind(new BooleanBinding() {
//                @Override
//                protected boolean computeValue() {
//                    return row.getItem().getStat() != StatusType.OFFLINE;
//                }
//            });
//            row.disableProperty().bind(

//                    EasyBind.select(row.itemProperty()) // start at itemProperty of row
//                            .selectObject(Computer::statusProperty)  // map to valueProperty of item, if item non-null
//                            .map(x -> x.ACTIVE < 5) // map to BooleanBinding via intValue of value < 5
//                            .orElse(false)); // value to use if item was null

            // it's also possible to do this with the standard API, but there are lots of
            // superfluous warnings sent to standard out:
            //row.disableProperty().bind( Bindings.selectBoolean(row.itemProperty(), "selected"));
            //row.setDisable(!true);

            return row ;
        });
//        table.sortPolicyProperty().set(new Callback<TableView<Computer>, Boolean>() {
//            @Override
//            public Boolean call(TableView<Computer> computerTableView) {
//                computerTableView.getItems().sort(new Comparator<Computer>() {
//                    @Override
//                    public int compare(Computer o1, Computer o2) {
//
//                        //return o1.getAddress().compareTo(o2.getAddress());
//                        return o1.getStat() != StatusType.OFFLINE?1:0;
//                    }
//                });
//                return null;
//            }
//        });

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
        btnExecCmd.setDisable(true);

        execParallel(CmdType.SENDING_CMD);
    }

    private void execParallel(CmdType cmdType) {
        new Thread(() -> exec(cmdType)).start();
    }


    private void exec(CmdType cmdType) {
        Set<CommandCallable> sshMachines = sshSessions;
        int cntSelected = 0;
        synchronized (sshMachines) {        //synchronizuj na maszynach (gdyby ktoś próbował kliknąć checkboxa obok maszyny)
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
//                    alert.setContentText("Maszyna wróciła status wykonania polecenia <<"+future.get().getCmdExitStatus()+">>");
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
                case DISCONNECTING:         //posprzątaj ale dopiero po zamknięciu wszystkich połączeń
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

                    synchronized (sshSessions) {        //synchronizuj na maszynach (gdyby ktoś próbował kliknąć checkboxa obok maszyny)
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
                                    btConnect.setText("Połącz");
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

    //klik na przycisk "Czyść konsolę"
    public void onConsolClear(ActionEvent actionEvent) {
        consoleOutput.clear();
    }

    //klik na przycisk "Połącz"
    public void onConect(ActionEvent actionEvent) {

        btConnect.setDisable(true);
        if(sshSessions.size() > 0) {    //tzn. jesteśmy połączeni
            btConnect.setText("Połącz");
            //najpierw pozamykać wszystkie połączenia
            execParallel(CmdType.DISCONNECTING);
            return;
        }
        btConnect.setText("Rozłącz");

        for (Computer comp: ConnectionHelper.getComputers()) {
            sshSessions.add(new CommandCallable(comp, loginField.getText(), passwordField.getText()));
        }
        ConnectionHelper.log.info("Trying to establish connection with hosts...");
        execParallel(CmdType.CONNECTING);
    }

    //TODO: klasę Callable do utworzenia połączenia
    //klasa do rónoległego wykonywania poleceń SSH na każdej z maszyn zdalnych
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


        //wysyłanie komendy na zdarzenie kliku na przycisk >>
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
