package com.praca.remoteadmin.Controller;

import com.jcraft.jsch.JSchException;
import com.praca.remoteadmin.Connection.ConnectionHelper;
import com.praca.remoteadmin.Connection.ConsoleCaptureOutput;
import com.praca.remoteadmin.Connection.IGenericConnector;
import com.praca.remoteadmin.Connection.SSH2Connector;
import com.praca.remoteadmin.GUI.MessageBoxTask;
import com.praca.remoteadmin.Model.*;
import com.praca.remoteadmin.Utils.AddComputersDialog;
import com.praca.remoteadmin.Utils.HistoryLog;
import com.praca.remoteadmin.Utils.Settings;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import static com.praca.remoteadmin.Utils.DataFormatEnum.JSON;

public class MainController implements ISaveDataObserver {
    @FXML
    public MenuItem btQuit;

    //settings Pane
    @FXML
    public CheckBox chkHistorySave;
    @FXML
    public TextField txtSudoTm;
    @FXML
    public TextField txtSshTm;


    //rooms Pane
    @FXML
    public TableView<LabRoom> tableRooms;
    @FXML
    public CheckBox chkSelectAllRooms;
    @FXML
    public TableColumn<LabRoom,String> roomNameCol;
    @FXML
    public TableColumn<LabRoom, Boolean> selectRoomCol;
    @FXML
    public TableColumn<LabRoom, String>  noCompInRoomCol;
    @FXML
    public TableColumn<LabRoom,Double> progressRoomCol;
    @FXML
    public TextField txtNewLabRoom;


    //computers Pane
    @FXML
    public CheckBox chkSelectAll;
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
    public TableView<Computer> tabelka;

    @FXML
    public Button btnRmComputers;

    //login Pane
    @FXML
    public PasswordField passwordField;
    @FXML
    public TextField loginField;


    @FXML
    public ComboBox<LabRoom> cbSala;
    @FXML
    public TextArea consoleOutput;
    @FXML
    public ComboBox<String> cmdLine;

    public Button btConnect;
    public Button btnExecCmd;
    public TabPane tabPane;

    //kontener na sale laboratoryjne z komputerami
    ObservableList<LabRoom> sale = null;//FXCollections.observableArrayList(new LabRoom(1,"Sala A"), new LabRoom(2,"Sala B"));


    public void onQuit(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void onSelectedAction(Event event) {
        //TODO: Zaimplementuj
        throw new UnsupportedOperationException();
    }

    @FXML
    public void initialize() {
        Settings.loadSettings();
        //maska do wprowadzania tylko cyfr (1 - 1999999999)
        final UnaryOperator<TextFormatter.Change> digitsOnlyFilter = c -> {
            String text = c.getControlNewText();
            if  (text.matches("[0-9]{0,7}")) {
                return c ;
            } else {
                return null ;
            }
        };
        txtSudoTm.setTextFormatter(new TextFormatter<>(digitsOnlyFilter));
        txtSshTm.setTextFormatter(new TextFormatter<>(digitsOnlyFilter));
        txtSudoTm.setText(ConnectionHelper.sudoConnectionTimeOut+"");
        txtSshTm.setText(ConnectionHelper.sshConnectionTimeOut +"");
        chkHistorySave.setSelected(ConnectionHelper.historySave);
        HistoryLog.loadData(cmdLine);

        btnExecCmd.setDisable(true);
        tabPane.getSelectionModel().select(1);      //ustaw domyślnie drugą zakładkę na starcie
        consoleOutput.autosize();
        cmdLine.getItems().add(0,ConnectionHelper.defaultCommand);
        cmdLine.getSelectionModel().select(0);
        loginField.setText(ConnectionHelper.defaultLogin);
        passwordField.setText(ConnectionHelper.defaultPassword);

        //powiązanie kolumn tabelki z properties dla obiektu Computer

        roomNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        selectRoomCol.setCellValueFactory(new PropertyValueFactory<>("selected"));
        noCompInRoomCol.setCellValueFactory(new PropertyValueFactory<>("computerStatus"));
        selectRoomCol.setCellFactory( p -> new CheckBoxTableCell<>());

        statusCol.setCellValueFactory( new PropertyValueFactory<>("status") );
        addressCol.setCellValueFactory( new PropertyValueFactory<>("address") );
        selectCol.setCellValueFactory(new PropertyValueFactory<>("selected") );
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




        cmdStatCol.setCellValueFactory(
                new PropertyValueFactory<>("cmdExitStatus")
        );
        progressCol.setCellValueFactory(
                new PropertyValueFactory<>("progressStatus")
        );


        sale = ConnectionHelper.loadData(JSON);

        if(sale == null) {
            //TODO: błąd wczytania (do logera i na ekran)
            return;
        }
        //dodaj observera do każdego elementu
        for(LabRoom room : sale) {
            room.setObserver(this);
            for (Computer comp : room.getComputers()) {
                comp.setObserver(this);
            }
        }
        cbSala.setItems(sale);
        cbSala.setEditable(false);
        if (sale.size() <= 0) return;

        cbSala.getSelectionModel().select(sale.get(0));
        LabRoom room = sale.get(0);
        if(room != null && room.getComputers() != null)
            tabelka.getItems().addAll(room.getComputers());
        else
            System.err.println("Room == null!!");

        tableRooms.getItems().addAll(sale);

    }

    @FXML
    public void OnLogingIn(ActionEvent actionEvent) {
        String sLogin = loginField.getText().trim();
        String sPassword = passwordField.getText().trim();
        //TODO:
        //szyfruj hasło i login zaraz po przejęciu od użytkownika oraz zeruj ich wartości w polach
    }

    //uruchamia przesłanie polecenia Shell'a poprez SSH do zdalnych maszyn
    public void onExecuteCommand(ActionEvent actionEvent)  {

        //        data.get(1).setStat(StatusType.ACTIVE);
        //        if( true) return;
        btnExecCmd.setDisable(true);
        String cmd = cmdLine.getSelectionModel().getSelectedItem().trim();
        cmdLine.getItems().add(0, cmd);
        if(checkIfSudoCommand(cmd)) {

            SSH2Connector.setSudoPassword();
            getAdminPass();
        }
        execParallel(CmdType.SENDING_CMD);
    }

    private void getAdminPass() {
        final FutureTask query = new FutureTask(new Callable() {
            @Override
            public Object call() throws Exception {
                return SSH2Connector.setSudoPassword();
            }
        });
        Platform.runLater(query);

        try {
            if(query.get() != null)
                System.out.println("Pass validated");
        } catch (InterruptedException e) {
            ConnectionHelper.log.error(e.getMessage());
        } catch (ExecutionException e) {
            ConnectionHelper.log.error(e.getMessage());
        }
    }


    private boolean checkIfSudoCommand(String cmd) {
        if(Pattern.matches("^sudo.*", cmd) )
            return true;
        else
            return false;
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
                            cntConnected = 0;
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
                            btnExecCmd.setDisable(!true);
                        }
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

        //HistoryLog.addCommand(cmdLine,cmdLine.getSelectionModel().getSelectedItem());
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

        //sale = ConnectionHelper.loadData(JSON);
        if(sale == null) {
            //TODO: błąd wczytania (do logera i na ekran)
            return;
        }
        //LabRoom room = sale.get(cbSala.getSelectionModel().getSelectedIndex());
        for(LabRoom room : sale) {
            for (Computer comp : room.getComputers()) {
                sshSessions.add(new CommandCallable(comp, loginField.getText(), passwordField.getText()));
            }
        }
        ConnectionHelper.log.info("Trying to establish connection with hosts...");
        execParallel(CmdType.CONNECTING);
    }

    public void salaSelect(ActionEvent actionEvent) {
        int indx = cbSala.getSelectionModel().getSelectedIndex();
        tabelka.getItems().clear();
        tabelka.getItems().addAll(sale.get(indx).getComputers());
        tabelka.refresh();
    }
    public void tableRoomsRefresh() {
        tableRooms.getItems().clear();
        tableRooms.getItems().addAll(sale);
        tableRooms.refresh();
    }


    public void selectAllComputers(ActionEvent actionEvent) {
        LabRoom room = sale.get(cbSala.getSelectionModel().getSelectedIndex());
        boolean bNewState = chkSelectAll.isSelected();
        //chkSelectAll.setStyle("-fx-color: rgba(255,255,255,.5);-fx-opacity: 0.60;");
        for (Computer comp: room.getComputers()) {
            if(comp.getStat() == StatusType.CONNECTED) {
                //TODO: rozłącz i wyłącz z kolejnych zadań

            }
            else {

            }
            comp.setSelected(bNewState);

        }
    }

    public void sshTmChanged(ActionEvent actionEvent) {
        saveSettings();
    }


    public void sudoTmChanged(ActionEvent actionEvent) {
        saveSettings();
    }

    @FXML
    private void saveSettings() {
        ConnectionHelper.sudoConnectionTimeOut = Integer.parseInt(txtSudoTm.getText());
        ConnectionHelper.sshConnectionTimeOut = Integer.parseInt(txtSshTm.getText());
        ConnectionHelper.historySave = chkHistorySave.isSelected();
        Settings.saveData();
    }

    public void addRoomAction(ActionEvent actionEvent) {
        String sName = txtNewLabRoom.getText().trim();
        if(sName.length() <= 0) return;
        sale.add(new LabRoom(sale.size()+1, sName));
        saveData();
        tableRoomsRefresh();
    }

    public void rmRoomAction(ActionEvent actionEvent) {
        List<LabRoom> list = new ArrayList<>();

        if(sale != null) {
            for(LabRoom room : sale) {
                if(room.isSelected())
                    list.add(room);
            }
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Uwaga!");
        alert.setHeaderText("Poniższa komenda usunie <<"+(list.size())+">> pracowni wraz z ich komputerami!");
        alert.setContentText("Kontynuować?");
        Optional<ButtonType> result = alert.showAndWait();
        if(result.isPresent()) {
            if(result.get() == ButtonType.OK) {
                sale.removeAll(list);
                saveData();
                tableRoomsRefresh();
            }
        }
    }

    //"Dodaj komputery"
    public void addCompAction(ActionEvent actionEvent) {
        AddComputersDialog dlg = new AddComputersDialog(sale.get(cbSala.getSelectionModel().getSelectedIndex()), this);
        salaSelect(null);       //dla odświeżenia widoku tabelki

    }

    public void setHistorySave(ActionEvent actionEvent) {
        saveSettings();
    }

    //implementacja funkcji observera do aktualizacji pliku data.json
    @Override
    public boolean saveData() {
        ConnectionHelper.saveData(sale);
        return true;
    }

    //usuwa zaznaczone komputery w aktywnej sali
    public void rmCompAction(ActionEvent actionEvent) {

        LabRoom room = cbSala.getSelectionModel().getSelectedItem();
        List<Computer> list = new ArrayList<>();
        if(room != null) {
            for(Computer c : room.getComputers()) {
                if(!c.isSelected())
                    list.add(c);
            }
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Uwaga!");
        alert.setHeaderText("Poniższa komenda usunie z pracowni <<"+(room.getComputers().size() - list.size())+">> komputerów.");
        alert.setContentText("Kontynuować?");
        Optional<ButtonType> result = alert.showAndWait();
        if(result.isPresent()) {
            if(result.get() == ButtonType.OK) {
                room.setComputers(list);
                saveData();
                salaSelect(null);
            }
        }
    }

    //TODO: klasę Callable do utworzenia połączenia
    //klasa do rónoległego wykonywania poleceń SSH na każdej z maszyn zdalnych
    class CommandCallable implements Callable<com.praca.remoteadmin.Model.Computer> {
        private com.praca.remoteadmin.Model.Computer comp;
        private CountDownLatch latch;

        private String pass;
        private String login;
        private IGenericConnector conn = null;

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
                try {
                    boolean ret = conn.openConnection(login, pass,  this.comp);
                } catch (JSchException e) {
                    throw new RuntimeException(e);
                }
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
            if(!comp.isSelected() || conn == null)
                return false;
            try {
                String cmd = cmdLine.getSelectionModel().getSelectedItem().trim();
                conn.execCommand(cmd, latch);
                return true;
            } catch (Exception e) {
                ConnectionHelper.log.error(e.getMessage());
                //Thread.currentThread().interrupt();
            }
            return false;
        }

        public void disconnect() {
            if(!comp.isSelected() || conn == null)
                return;
            conn.disconnect();
            latch.countDown();
        }

        public void setLatch(CountDownLatch latch) {
            this.latch = latch;
        }
    }
}
