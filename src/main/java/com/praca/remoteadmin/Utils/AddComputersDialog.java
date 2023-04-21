package com.praca.remoteadmin.Utils;

import com.praca.remoteadmin.Connection.ConnectionHelper;
import com.praca.remoteadmin.Model.Computer;
import com.praca.remoteadmin.Model.ISaveDataObserver;
import com.praca.remoteadmin.Model.LabRoom;
import com.praca.remoteadmin.Model.StatusType;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.UnaryOperator;


public class AddComputersDialog {

    private final Stage stage;

    public ObservableList<Computer> getComps() {
        return comps;
    }

    //lista komputerów do zwrotu
    private ObservableList<Computer> comps = FXCollections.observableArrayList();
    Node btnOk = null;

    TableView<Computer> table = new TableView<>();
    TableColumn<Computer, Boolean> selectCol = new TableColumn<>("Dodaj");
    TableColumn<Computer, String> addressCol = new TableColumn<>("Adres IP");

    TableCell<Computer, Boolean> addCell = new TableCell<>();
    TableCell<Computer, String>  addressCell = new TableCell<>();

    private Label addressLabel = new javafx.scene.control.Label("");
    private Label summarizeLabel = new javafx.scene.control.Label("");

    protected String increaseIPAddress(String sIPAddress) {
        long ipAddres = convertFromString(sIPAddress);
        ipAddres++;
        if((ipAddres % 256) == 0)
            ipAddres++;
        return convertToString(ipAddres);
    }


    private String convertToString(long ipAddres) {
        String sIPAddress = "";
        sIPAddress += (ipAddres>>24 & 0xFF) + ".";
        sIPAddress += (ipAddres>>16 & 0xFF) + ".";
        sIPAddress += (ipAddres>>8 & 0xFF) + ".";
        sIPAddress += (ipAddres & 0xFF);

        return sIPAddress;
    }

    private long convertFromString(String sIPAddress) {
        long ip = 0;
        String []addressSplit = sIPAddress.split("\\.");
        if((addressSplit.length == 4 && Integer.parseInt(addressSplit[3]) > 0)) {
            for(int i = 0; i < 3; i++,ip <<= 8) {
                try {
                    ip |= Integer.parseInt(addressSplit[i]);
                }
                catch (NumberFormatException e) {
                    System.out.println(addressSplit[i]);
                }
            }
            try {
                ip |= Integer.parseInt(addressSplit[3]);
            }
            catch (NumberFormatException e) {
                System.out.println(addressSplit[3]);
            }
        }
        else
            throw new RuntimeException("Nieprawidłowy adres IP");
        return ip;
    }

    public AddComputersDialog(ObservableList<LabRoom> sale, LabRoom room, ISaveDataObserver observer, Stage stage) {

        this.stage = stage;
        int row = 0;
        TextField nameTextField = new TextField("");
        TextField ipTextField = new TextField("");
        TextField ipTextField2 = new TextField("");
        javafx.scene.control.Dialog<String> dialog = new Dialog<>();


        Button btnAddCompyterByName = new Button();
        btnAddCompyterByName.setText("Dodaj");
        btnAddCompyterByName.setOnAction(actionEvent -> {
            String name = nameTextField.getText();
            if(name.trim().length() <= 0) return;
            Computer comp = new Computer(room, "Komputer", name, StatusType.UNKNOWN);
            comp.setSelected(true);
            comps.add(comp);
            table.getItems().add(comp);
            btnOk.setDisable(false);
        });

        Button btnAddIPAddress = new Button();
        btnAddIPAddress.setText("Dodaj++");
        btnAddIPAddress.setDisable(true);
        btnAddIPAddress.setTooltip(new Tooltip("Dodaje komputer o zadanym adresie IP oraz inkrementuje adres w polu edycji adresu."));
        btnAddIPAddress.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                Computer comp = new Computer(room, "Komputer", ipTextField.getText(), StatusType.UNKNOWN);
                comp.setSelected(true);
                comps.add(comp);
                table.getItems().add(comp);
                ipTextField.setText(increaseIPAddress(ipTextField.getText()));
                btnOk.setDisable(false);
            }
        });


        Button btnAddIPAddressRange = new Button();
        btnAddIPAddressRange.setText("Dodaj Zakres");
        btnAddIPAddressRange.setDisable(false);
        btnAddIPAddressRange.setTooltip(new Tooltip("Dodaje komputery z zakresu podanych adresów IP."));
        btnAddIPAddressRange.setOnAction(actionEvent -> {
            long addressStart = convertFromString(ipTextField.getText());
            long addressStop = convertFromString(ipTextField2.getText());
            for(long i = addressStart; i <= addressStop; i++) {
                Computer comp = new Computer(room, "Komputer", convertToString(i), StatusType.UNKNOWN);
                comp.setSelected(true);
                comps.add(comp);
                table.getItems().add(comp);
            }
            if (!comps.isEmpty())
                btnOk.setDisable(false);
        });

        String regex = IPv32RegEx();
        final UnaryOperator<TextFormatter.Change> ipAddressFilter = c -> {
            String text = c.getControlNewText();

            if  (text.matches(regex)) {
                String []addressSplit = text.split("\\.");
                btnAddIPAddress.setDisable(!(addressSplit.length == 4 && Integer.parseInt(addressSplit[3]) > 0));
                btnAddIPAddressRange.setDisable(btnAddIPAddress.isDisable());
                if((text.length() - text.lastIndexOf('.')) == 4 && !c.isDeleted())
                    Platform.runLater(() -> {
                        try {
                            Thread.sleep(5);
                        } catch (InterruptedException e) {
                            ConnectionHelper.log.error(e.getMessage());
                            e.printStackTrace();
                        }

                        if(text.lastIndexOf('.') == text.indexOf('.') && text.indexOf('.') >= 0) {
                            int klasaSieci = Integer.parseInt(text.substring(0, text.indexOf('.')));
                            if (klasaSieci < 127 ) {
                                addressLabel.setText("Klasa A");
                            } else if (klasaSieci  < 192 ) {
                                addressLabel.setText("Klasa B");
                            } else
                                addressLabel.setText("Klasa C");
                        }

                        ipTextField.appendText(".");
                    });
                return c ;
            } else {
                return null ;
            }
        };
        final UnaryOperator<TextFormatter.Change> ipAddressFilter2 = c -> {
            String text = c.getControlNewText();

            if  (text.matches(regex)) {
                String []addressSplit = text.split("\\.");
                btnAddIPAddressRange.setDisable(!(addressSplit.length == 4 && Integer.parseInt(addressSplit[3]) > 0) && !btnAddIPAddress.isDisabled());

                if((text.length() - text.lastIndexOf('.')) == 4 && !c.isDeleted())
                    Platform.runLater(() -> {
                        try {
                            Thread.sleep(5);
                        } catch (InterruptedException e) {
                            ConnectionHelper.log.error(e.getMessage());
                            e.printStackTrace();
                        }
                        ipTextField2.appendText(".");
                    });
                return c ;
            } else {
                return null ;
            }
        };

        ipTextField.setText("192.168.042.140");
        ipTextField.setTextFormatter(new TextFormatter<>(ipAddressFilter));
        ipTextField2.setTextFormatter(new TextFormatter<>(ipAddressFilter2));

        ipTextField2.setText("192.168.042.150");

        dialog.setTitle("RemoteAdmin");
        dialog.setHeaderText("Dodaj komputery do pracowni: <<"+room.getName()+">>");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK );
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL );
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(10));



        grid.add(addressLabel, 0, row);

        nameTextField.setPromptText("Wprowadź nazwę DNS skomputera");
        grid.add(nameTextField, 0, (++row));
        grid.add(btnAddCompyterByName, 1, row);

        grid.add(new Label("Od:"), 0, (++row));
        grid.add(ipTextField, 0, (++row));
        grid.add(btnAddIPAddress, 1, row);

        grid.add(new Label("Do:"), 0, (++row));
        grid.add(ipTextField2, 0, (++row));
        grid.add(btnAddIPAddressRange, 1, row);

        selectCol.setMinWidth(50);
        selectCol.setMaxWidth(100);
        selectCol.setEditable(true);

        addressCol.setMinWidth(250);
        addressCol.setMaxWidth(400);

        table.getColumns().add(selectCol);
        table.getColumns().add(addressCol);
        table.setId("dlg_table");
        table.setEditable(true);

        table.setTooltip(new Tooltip("Lista komputerów do dodania."+System.lineSeparator()+"Aby usunąć komputer z listy wystarczy usunąć zaznaczenie w pierwszej kolumnie."));
        addressCol.setCellValueFactory( new PropertyValueFactory<>("address") );
        selectCol.setCellValueFactory( new PropertyValueFactory<>("selected") );
        selectCol.setCellFactory(
            new Callback<TableColumn<Computer,Boolean>,TableCell<Computer,Boolean>>(){
                @Override public
                TableCell<Computer,Boolean> call( TableColumn<Computer,Boolean> p ){
                    return new CheckBoxTableCell<>();
                }
            });

        grid.add(new Label("Tabela komputerów do dodania:"), 0, (++row));
        grid.add(table, 0,(++row));
        Button btnClear = new Button("Wyczyść tabelkę");

        btnClear.setOnAction(actionEvent -> Platform.runLater(() -> {
            table.getItems().clear();
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK );
            comps.clear();
        }));
        grid.add(btnClear, 1,row);

        Button btnFilterIPs = new Button("Filtruj dodatkowo komputery PINGiem");
        btnFilterIPs.setOnAction(actionEvent -> {
            btnFilterIPs.setDisable(true);
            //sprawdzamy pingiem dostępnąść maszyn

                Platform.runLater(() -> {
                    dialog.getDialogPane().setCursor(Cursor.WAIT);
                });
                new Thread(() -> {
                    try {
                        pingCheck(comps);
                    } catch (InterruptedException | ExecutionException e) {
                        ConnectionHelper.log.error(e.getMessage());
                        throw new RuntimeException(e);
                    } finally {
                            Platform.runLater(() -> {
                                btnFilterIPs.setDisable(false);
                                dialog.getDialogPane().setCursor(Cursor.DEFAULT);
                            });

                        }
                }).start();
        });

        btnFilterIPs.setTooltip(new Tooltip("Sprawdza komendą PING dostępność komputera i w jej braku wyłacza go z listy dodawanych."));
        grid.add(btnFilterIPs, 0, (++row));
        grid.add(summarizeLabel, 0, (++row));

        btnOk = dialog.getDialogPane().lookupButton(ButtonType.OK);

        btnOk.setDisable(true);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK)
                return "OK";
            return "FALSE";
        });


        //dialog.show();
        Optional<String> result = dialog.showAndWait();


        if(result.isPresent() ) {
            if(result.get().compareTo("OK") != 0) return;
            for(Computer c : comps)
                if(c.isSelected()) {
                    System.out.println(c.getAddress());
                    room.appendComputer(c);
                    c.setLabs(sale);
                    c.setParent(room);
                }
        }
        observer.saveData();
    }

    //sprawdza pingiem dostępność wpisanych adresów
    private void pingCheck(ObservableList<Computer> comps) throws InterruptedException, ExecutionException {

        ExecutorService executorService = Executors.newFixedThreadPool(comps.size());
        Set<Callable<Boolean>> callables = new HashSet<>();
        Platform.runLater(() -> summarizeLabel.setText(""));

        comps.forEach( (c) -> {
            callables.add( () -> {
                boolean bRet = false;
                try {
                    bRet = Ping.ping(c.getAddress());
                    boolean finalBRet = bRet;
                    Platform.runLater(() -> c.setSelected(finalBRet));
                } catch (IOException e) {
                    ConnectionHelper.log.error(e.getMessage());
                    //e.printStackTrace();
                    Platform.runLater(() -> c.setSelected(false));
                }
                return bRet;
            });
        });

        List<Future<Boolean>> futures = executorService.invokeAll(callables);

        int iSuccess = 0;
        int iFailed = 0;
        for(Future<Boolean> future : futures){
            int tmp = future.get() == true ? iSuccess++ : iFailed++;
            System.out.println(future.get());
        }
        executorService.shutdown();

        int finalISuccess = iSuccess;
        Platform.runLater(() -> {
            summarizeLabel.setText(finalISuccess +"/"+(comps.size()) + " maszyn jest dostępnych w tej chwili");
        });
    }
    private String IPv32RegEx() {
        return "(\\d{0,3})(\\.(\\d{0,3})){0,3}";
    }
}

