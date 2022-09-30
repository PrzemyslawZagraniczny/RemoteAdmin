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
import com.praca.remoteadmin.Utils.Ping;
import com.praca.remoteadmin.Utils.Settings;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import static com.praca.remoteadmin.Model.StatusType.*;
import static com.praca.remoteadmin.Utils.DataFormatEnum.JSON;
import static java.lang.Thread.MIN_PRIORITY;

public class MainController implements ISaveDataObserver, Runnable {

    AtomicBoolean openSession = new AtomicBoolean(false);
    //aktualizacja statusu maszyny z OFFLINE/TIMEOUT do CONNECTED
    BlockingQueue<CommandCallable> sshReconnect = new LinkedBlockingQueue<>();
    @FXML
    public MenuItem btQuit;

    //settings Pane
    @FXML
    public TextField txtPingDelay;
    @FXML
    public TextField txtBufferSize;

    @FXML
    public CheckBox chkHistorySave;
    @FXML
    public TextField txtSudoTm;
    @FXML
    public TextField txtSshTm;
    @FXML
    public PasswordField passwordField;
    @FXML
    public TextField loginField;

    //rooms Pane
    @FXML
    public TableView<LabRoom> tableRooms;
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

    @FXML
    public Button btAbortCommand;
    @FXML
    public TextField txtArgs;

    final Set<CommandCallable> sshSessions = new HashSet<CommandCallable>();
    @FXML
    public TableColumn<Computer, String> statusCol;
    @FXML
    public TableColumn<Computer,String> addressCol;
    @FXML
    public TableColumn<Computer, Boolean> printCol;
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



    @FXML
    public ComboBox<LabRoom> cbSala;
    @FXML
    public TextArea consoleOutput;
    @FXML
    public ComboBox<String> cmdLine;

    public Button btConnect;
    public Button btnExecCmd;
    public TabPane tabPane;

    public String command;      //zmienna do przechowania komendy
    //kontener na sale laboratoryjne z komputerami
    ObservableList<LabRoom> sale = null;//FXCollections.observableArrayList(new LabRoom(1,"Sala A"), new LabRoom(2,"Sala B"));
    private Thread connectionDispatchThread = null;     //looper do sprawdzania statusu
    private Thread pingThread = null;               //wątek do wznawiania połączenia
    private Stage stage = null;


    public void onQuit(ActionEvent actionEvent) {
        Platform.exit();
    }



    @FXML
    public void initialize() {
        sale = ConnectionHelper.loadData(JSON);

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
        //dodaje obłsugę klawiszy UP/DOWN w okienku wpisywania poleceń do przewijania historii komend
        cmdLine.getEditor().setOnKeyPressed(keyEvent -> {
            switch(keyEvent.getCode()) {
                case ENTER:
                    //to wywołanie dubluje komendę
                    //onExecuteCommand(null);
                    break;
                case UP: {
                    int indx = cmdLine.getSelectionModel().getSelectedIndex();
                    indx++;
                    if (indx < 0)
                        indx = 0;
                    if (indx >= cmdLine.getItems().size())
                        indx = cmdLine.getItems().size() - 1;
                    cmdLine.getSelectionModel().select(indx);
                }
                break;
                case DOWN: {
                    int indx = cmdLine.getSelectionModel().getSelectedIndex();
                    indx--;
                    if (indx < 0)
                        indx = 0;
                    cmdLine.getSelectionModel().select(indx);
                    break;
                }
                default:
                    break;

            }
        });
        txtPingDelay.setTextFormatter(new TextFormatter<>(digitsOnlyFilter));
        txtBufferSize.setTextFormatter(new TextFormatter<>(digitsOnlyFilter));
        txtSudoTm.setTextFormatter(new TextFormatter<>(digitsOnlyFilter));
        txtSshTm.setTextFormatter(new TextFormatter<>(digitsOnlyFilter));
        txtSudoTm.setText(ConnectionHelper.sudoConnectionTimeOut+"");
        txtSshTm.setText(ConnectionHelper.sshConnectionTimeOut +"");
        txtPingDelay.setText(ConnectionHelper.pingDelay+"");
        txtBufferSize.setText(ConnectionHelper.bufferSize+"");
        chkHistorySave.setSelected(ConnectionHelper.historySave);

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
        printCol.setCellValueFactory(new PropertyValueFactory<>("printout") );
        selectCol.setEditable(true);
        selectCol.setCellFactory(
                new Callback<TableColumn<Computer,Boolean>,TableCell<Computer,Boolean>>(){
                    @Override public
                    TableCell<Computer,Boolean> call( TableColumn<Computer,Boolean> p ){
                        return new CheckBoxTableCell<>();
                    }
                });
        printCol.setCellFactory(
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



        tableRooms.getItems().addAll(sale);

        if(sale == null) {
            //TODO: błąd wczytania (do logera i na ekran)
            return;
        }
        //dodaj observera do każdego elementu
        for(LabRoom room : sale) {
            room.setObserver(this);
            for (Computer comp : room.getComputers()) {
                comp.setObserver(this);
                comp.setLabs(sale);
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



    }

    @FXML
    public void OnLogingIn(ActionEvent actionEvent) {
        String sLogin = loginField.getText().trim();
        String sPassword = passwordField.getText().trim();
        //TODO:
        //szyfruj hasło i login zaraz po przejęciu od użytkownika oraz zeruj ich wartości w polach
    }

    public void onExecuteCommand(ActionEvent actionEvent)  {
        SSH2Connector.resetPass();
        command = cmdLine.getSelectionModel().getSelectedItem();
        if(command == null || command.length() <= 0) return;


        if(checkIfSudoCommand(command)) {

            //SSH2Connector.sudo_pass = "793691235!";
            if(SSH2Connector.setSudoPassword() == null)
                return;
        }
        btnExecCmd.setDisable(true);
        //blokuj zaznaczanie maszyn w trakcie otwartej sesji SSH
        selectCol.setEditable(false);
        selectRoomCol.setEditable(false);
        chkSelectAll.setDisable(true);



        String hash = HistoryLog.calcHashForHistoryLog(loginField.getText());
        HistoryLog.loadData(cmdLine, hash);
        HistoryLog.addCommand(cmdLine,hash, command);
        cmdLine.getItems().add(0, command);
        cmdLine.getSelectionModel().select(-1);


        execParallel(CmdType.SENDING_CMD, sshSessions);
    }


    private boolean checkIfSudoCommand(String cmd) {
        if(Pattern.matches("^sudo.*", cmd) )
            return true;
        else
            return false;
    }

    private void execParallel(CmdType cmdType, Set<CommandCallable> sshs) {
        new Thread(() -> exec(cmdType, sshs)).start();
    }


    private void exec(CmdType cmdType,Set<CommandCallable> ssh) {
        Set<CommandCallable> sshMachines = ssh;
        int cntSelected = 0;
        //synchronized (sshMachines)
        {        //synchronizuj na maszynach (gdyby ktoś próbował kliknąć checkboxa obok maszyny)
            for (CommandCallable comp : sshMachines) {
                if (comp.comp.isSelected() &&
                        ( comp.comp.getStat() == StatusType.CONNECTED ||     //tylko jeśli maszyna zaznaczona i połączona
                        cmdType != CmdType.SENDING_CMD )) {    //tylko jeśli maszyna zaznaczona i połączona albo w trybie łączenia/rozłączania

                    comp.setCmdType(cmdType);
                    cntSelected++;
                }
            }

            if(cntSelected <= 0) {
                postDisconnect();
                return;
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
                    postDisconnect();
                    synchronized (sshSessions) {
                        sshSessions.clear();
                    }
                    ConnectionHelper.log.info("Disconnected from <<"+cntSelected+">> hosts.");
                    break;
                case CONNECTING: {
                    cntSelected = 0;
                    int cntConnected = 0;
                    synchronized (ssh)
                    {        //synchronizuj na maszynach (gdyby ktoś próbował kliknąć checkboxa obok maszyny)
                        for (CommandCallable comp : ssh) {
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
                        postDisconnect();
                    }
                    else {
                        //wątek Pinga aktywnych maszyn
                        pingThread = new Thread(this);
                        pingThread.setPriority(MIN_PRIORITY);
                        pingThread.start();

                        Platform.runLater(() -> {
                            consoleOutput.clear();              //czyść okienko konsoli wyjścia
                            btnExecCmd.setDisable(false);       //gdyby było przyblokowane
                            btConnect.setDisable(false);
                            selectCol.setEditable(!true);
                            selectRoomCol.setEditable(false);
                            chkSelectAll.setDisable(true);

                        });
                    }
                }
                break;
                case SENDING_CMD:
                    Platform.runLater(() -> btnExecCmd.setDisable(!true));
                    break;
                case NONE:
                default:
                    break;
            }
            executorService.shutdown();
        }
    }

    //klik na przycisk "Czyść konsolę"
    public void onConsoleClear(ActionEvent actionEvent) {

        //HistoryLog.addCommand(cmdLine,cmdLine.getSelectionModel().getSelectedItem());
        for(LabRoom lr : sale)
            for(Computer c : lr.getComputers()) {
                c.clearBuffer();        //usuń zapis out w sesji
            }
        consoleOutput.clear();
    }

    //klik na przycisk "Połącz"
    public void onConnect(ActionEvent actionEvent) {
        String login = loginField.getText();
        String pass = passwordField.getText();
        if (credentialsCheck(login, pass)) return;

        btConnect.setText("Rozłącz");
        String hash = HistoryLog.calcHashForHistoryLog(login);
        HistoryLog.loadData(cmdLine, hash);

        if(sale == null) {
            ConnectionHelper.log.error("LabRooms are undefined!!!");
            return;
        }        //najpierw sprawdź czy dane logowania są obecne

        btConnect.setDisable(true);
        if(sshSessions.size() > 0) {    //tzn. jesteśmy połączeni

            //najpierw pozamykać wszystkie połączenia
            execParallel(CmdType.DISCONNECTING, sshSessions);
            return;
        }




        //LabRoom room = sale.get(cbSala.getSelectionModel().getSelectedIndex());
        for(LabRoom room : sale) {
            for (Computer comp : room.getComputers()) {
                sshSessions.add(new CommandCallable(comp, login, pass));
            }
        }
        ConnectionHelper.log.info("Trying to establish connection with hosts...");
        execParallel(CmdType.CONNECTING, sshSessions);
        selectCol.setEditable(false);
        selectRoomCol.setEditable(false);
        chkSelectAll.setDisable(true);

        openSession.set(true);


        //wątek dla reconnecting komputerów, które zmieniły status z offline na online albo timeout/online
        connectionDispatchThread = new Thread(() -> {
            while(openSession.get()) {
                try {
                    CommandCallable cc = sshReconnect.poll();
                    if(cc == null) {
                        Thread.sleep(ConnectionHelper.pingDelay);
                        continue;
                    }
                    synchronized (cc) {
                        cc.comp.setStat(StatusType.CONNECTING);
                    }
                        HashSet<CommandCallable> hs = new HashSet<>();
                        hs.add(cc);

                    if (credentialsCheck(login, pass)) return;
                    //new Thread(() -> cc.connect()).start();


                    execParallel(CmdType.CONNECTING, hs);

                } catch (InterruptedException ex) {
                    ConnectionHelper.log.error(ex.getMessage());
                    ex.printStackTrace();

                }
            }
        });
        connectionDispatchThread.setPriority(MIN_PRIORITY);
        connectionDispatchThread.start();
    }

    //sprawdza czy jest podany login i jak nie to wyświetla odpowiedni komunikat
    private boolean credentialsCheck(String login, String pass) {
        if(login.length() == 0 || pass.length() == 0) {
            tabPane.getSelectionModel().select(2);      //jeśli nie podano hasła przejdź do zakładki ustawień
//            if(loginField.getText().length() == 0)
            {
                Tooltip tt = new Tooltip("Aby sie zalogować trzeba podać login i hasło");
                //loginField.setTooltip(tt);
                tt.setId("alert_tooltip");
                tt.setShowDelay(Duration.seconds(1));
                //tt.setShowDuration(Duration.seconds(5));
                TimerTask timTask = new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> tt.hide());
                    }
                };
                new Timer().schedule(timTask, 4000, 10);

                Bounds bounds = loginField.localToScreen(loginField.getBoundsInLocal());
                tt.show(loginField, bounds.getCenterX(), bounds.getCenterY());
            }
            return true;
        }
        return false;
    }

    private void postDisconnect() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                openSession.set(false);             //zamyka wątek cyklicznego powtórnego łączenia
                btConnect.setDisable(false);
                btConnect.setText("Połącz");
                cmdLine.getItems().clear();         //usuń historię gdy nie jesteśmy połączeni
                consoleOutput.clear();              //czyść okienko konsoli wyjścia
                btnExecCmd.setDisable(false);       //gdyby było przyblokowane
                selectCol.setEditable(!false);
                selectRoomCol.setEditable(!false);
                chkSelectAll.setDisable(!true);

                for (CommandCallable cc : sshSessions) {
                    cc.comp.clearCmdExitStatus();
                }
                synchronized (sshSessions) {
                    sshSessions.clear();
                }
            }
        });
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
        ConnectionHelper.pingDelay = Integer.parseInt(txtPingDelay.getText());
        ConnectionHelper.bufferSize = Long.parseLong(txtBufferSize.getText());
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
        if(sale.size() <= 0 ) {
            new MessageBoxTask("Najpierw trzeba utworzyć choć jedną pracownię, do której można dodać komputery.", "Uwaga", Alert.AlertType.INFORMATION).run();
            return;
        }
        AddComputersDialog dlg = new AddComputersDialog(sale, sale.get(cbSala.getSelectionModel().getSelectedIndex()), this);
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

    public void aboutProgram(ActionEvent actionEvent) {

        new MessageBoxTask(
                "Program do zdalnej administracji sieci komputerowej."+
                        System.lineSeparator()+"Autor: Przemysław Zagraniczny",
                "RemoteAdmin", Alert.AlertType.INFORMATION)
                .run();
    }

    public void onCommandLineKeyTyped(KeyEvent keyEvent) {


    }

    //metoda przywoływana do sprawdzania połaczenia z maszynami (w trybie Idle) co "jakiś czas"
    @Override
    public void run() {
        while(openSession.get()) {
            try {
                Thread.sleep(ConnectionHelper.pingDelay);
                if(sshSessions != null)
                    synchronized (sshSessions)
                    {
                        for (CommandCallable cc : sshSessions) {
                            //synchronized (cc)
                            {
                                if (!cc.comp.isSelected()) continue;
                                boolean bOnline = Ping.ping(cc.comp.getAddress());
                                if (cc.comp.getStat() == StatusType.CONNECTED) {     //sprawdź czy połączone nadal są połączone
                                    if (!bOnline) {
                                        cc.comp.abortCommand();
//                                        cc.disconnect();
                                        cc.comp.setCmdExitStatus("Computer off-line.");
                                        cc.comp.setProgressStatus(0);
                                        cc.comp.setStat(OFFLINE);                   //computer is down
                                    }
                                    if(!cc.conn.isOpened())
                                        cc.comp.setStat(DISCONNECTED);                   //computer is down
                                } else {                                             //oraz czy offline nadal są offline
                                    if (bOnline) {
                                        if (cc.comp.getStat() != StatusType.CONNECTING) {     //sprawdź czy połączone nadal są połączone
                                            if (!sshReconnect.contains(cc))
                                                sshReconnect.add(cc);
                                            cc.comp.setStat(ONLINE);
                                        }

                                    } else {
                                        cc.comp.setStat(OFFLINE);                   //computer is down
                                    }
                                }
                            }
                        }
                    }
                if(sshReconnect.size() > 0) {
                    System.out.println("Connecting comps..." );
                    //new Thread(() -> exec(CmdType.CONNECTING, sshReconnect)).start();
                }
            } catch (InterruptedException | IOException e) {
                ConnectionHelper.log.error(e.getMessage());
                throw new RuntimeException(e);
            }
        }

    }

    public void onPassArgs(ActionEvent actionEvent) {
        String args = txtArgs.getText().trim();
        if(args.length() <=0) return;
        synchronized (sshSessions) {
            for (CommandCallable cc : sshSessions) {
                if( cc.comp.isSelected() && cc.comp.getStat() == StatusType.CONNECTED) {
                    cc.conn.passArgs(args);
                }
            }
        }
    }

    public void onAbortCommand(ActionEvent actionEvent) {
        if(sshSessions != null) {
            //synchronized (sshSessions)
            {
                for (CommandCallable cc : sshSessions) {
                    if (cc.comp.isSelected())
                        cc.comp.abortCommand();
                }
                //postDisconnect();
            }

        }
    }

    public void onBgCommand(ActionEvent actionEvent) {
        if(sshSessions != null) {
            synchronized (sshSessions) {
                for (CommandCallable cc : sshSessions) {
                    if (cc.comp.isSelected())
                        cc.comp.bgCommand();
                }
            }
            btnExecCmd.setDisable(false);
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        stage.setOnCloseRequest(windowEvent -> {
            synchronized(sshSessions) {
                if (sshSessions.size() > 0) {    //tzn. jesteśmy połączeni
                    //najpierw pozamykać wszystkie połączenia
                    execParallel(CmdType.DISCONNECTING, sshSessions);
                }
                openSession.set(false);
            }
        });
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

        private boolean connect() {
            boolean ret = false;
            if(conn == null) {
                conn = new SSH2Connector();
            }
            ConsoleCaptureOutput out = new ConsoleCaptureOutput(consoleOutput);
            conn.setErrorStream(out);
            comp.setOutputStream(out);
            comp.setSshConnector((SSH2Connector) conn);
            try {
                ret = conn.openConnection(login, pass,  this.comp);
            } catch (JSchException e) {
                ConnectionHelper.log.error(e.getMessage());
                e.printStackTrace();
            }
            latch.countDown();
            return ret;
        }

        @Override
        public com.praca.remoteadmin.Model.Computer call() throws Exception {
            switch (cmdType) {
                case CONNECTING:
                    ConnectionHelper.log.info("Connecting to <<"+comp.getAddress()+">>...");
                    connect();
                    //if(!connect()) postDisconnect();
                    break;
                case SENDING_CMD:
                    //ConnectionHelper.log.info("Querrying <<"+comp.getAddress()+">>...");
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
        private boolean sndCommand() {
            if(!comp.isSelected() || conn == null)
                return false;
            try {
                String cmd = command;
                System.out.println(">>"+comp.getAddress()+" is running " + command);
                conn.execCommand(cmd, latch);
                System.out.println(">>"+ comp.getAddress()+" finnished " + command);
                return true;
            } catch (Exception e) {
                ConnectionHelper.log.error(e.getMessage());
                //Thread.currentThread().interrupt();
            }
            return false;
        }

        public void disconnect() {
            if(comp == null || !comp.isSelected() || conn == null)
                return;
            if((comp.getStat() == CONNECTED || comp.getStat() == CONNECTING)) {
                conn.disconnect();
            }
            comp.clearBuffer();
            latch.countDown();
        }

        public void setLatch(CountDownLatch latch) {
            this.latch = latch;
        }
    }
}
