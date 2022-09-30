package com.praca.remoteadmin.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.praca.remoteadmin.Connection.ConnectionHelper;
import com.praca.remoteadmin.Connection.ConsoleCaptureOutput;
import com.praca.remoteadmin.Connection.SSH2Connector;
import com.praca.remoteadmin.Utils.ExitStatusMapper;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class Computer {
    private SimpleStringProperty name = new SimpleStringProperty();
    private SimpleStringProperty  address = new SimpleStringProperty();
    @JsonInclude
    private SimpleBooleanProperty selected = new SimpleBooleanProperty(false);

    public boolean isSelected() {
        return selected.get();
    }

    public SimpleBooleanProperty selectedProperty() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    @JsonIgnore
    AtomicBoolean bgCommand = new AtomicBoolean(false);

    @JsonIgnore
    AtomicBoolean abortCommand = new AtomicBoolean(false);

    public SSH2Connector getSshConnector() {
        return sshConnector;
    }

    public void setSshConnector(SSH2Connector sshConnector) {
        this.sshConnector = sshConnector;
    }

    @JsonIgnore
    private SimpleBooleanProperty update = new SimpleBooleanProperty(false);

    public void setUpdate(boolean update) {
        this.update.set(update);
    }

    @JsonIgnore
    SSH2Connector sshConnector = null;

    @JsonIgnore
    private ConsoleCaptureOutput out = null;

    public String getStatus() {
        return status.get();
    }

    public SimpleStringProperty statusProperty() {
        return status;
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public void setObserver(ISaveDataObserver observer) {
        this.observer = observer;
    }

    //do zapisu danych z SSH

    @JsonIgnore
    LabRoom parent = null;
    @JsonIgnore
    ObservableList<LabRoom> labs = null;

    public void setLabs(ObservableList<LabRoom> labs) {
        this.labs = labs;
    }

    @JsonIgnore
    private ISaveDataObserver observer = null;
    @JsonIgnore
    private SimpleStringProperty  status = new SimpleStringProperty(StatusType.UNKNOWN+"");
    @JsonIgnore
    //stan maszyny (czy jest włączona (ACTIVE), nieaktywna (OFFLINE)
    private StatusType stat = StatusType.UNKNOWN;

    //czy maszyna zostala wybrana do poczenia (ustawiane checkboxem)


    public boolean isPrintout() {
        return printout.get();
    }

    public SimpleBooleanProperty printoutProperty() {
        return printout;
    }

    public void setPrintout(boolean printout) {
        this.printout.set(printout);
    }

    @JsonInclude
    private SimpleBooleanProperty printout = new SimpleBooleanProperty(false);
    @JsonIgnore
    private SimpleStringProperty cmdExitStatus = new SimpleStringProperty("");

    public double getProgressStatus() {
        return progressStatus.get();
    }

    public SimpleDoubleProperty progressStatusProperty() {
        return progressStatus;
    }

    public void setProgressStatus(double progressStatus) {
        Platform.runLater(() -> this.progressStatus.set(progressStatus));
    }

    @JsonIgnore
    private SimpleDoubleProperty progressStatus = new SimpleDoubleProperty(0);

    public String getCmdExitStatus() {
        return cmdExitStatus.get();
    }

    public SimpleStringProperty cmdExitStatusProperty() {
        return cmdExitStatus;
    }

    public void setCmdExitStatus(int cmdExitStatus) {
        try {
            this.cmdExitStatus.set(ExitStatusMapper.fromExitCode(cmdExitStatus) + " (" + cmdExitStatus + ")");
        }catch(Exception e)
        {
            ConnectionHelper.log.error(e.getMessage());
        }
    }

    @JsonIgnore
    public ConsoleCaptureOutput getPs() {
        return out;
    }

    public Computer() {
        clearBuffer();
        addSelectedListener();
    }
    public void setOutputStream(ConsoleCaptureOutput out) {
        this.out = out;
    }
    public Computer(LabRoom parent, String sName, String sAddress, StatusType stat) {
        this.parent = parent;
        clearBuffer();

        this.name.set(sName);
        this.address.set(sAddress);
        this.stat = stat;
        status.set(stat.toString());
        addSelectedListener();
    }

    private void addSelectedListener() {
        printout.addListener(change -> {
            if (observer != null) {
                observer.saveData();
            }
        });

        selected.addListener(change -> {
            if(observer != null) {
                observer.saveData();
            }
            ConnectionHelper.log.info("User "+(selected.get() ?"selected":"unselected")+" host <<"+getAddress()+">>");
            if(parent != null ) {
                if (selected.get())
                    parent.numberOfComputersProperty().set(parent.numberOfComputersProperty().get() + 1);
                else
                    parent.numberOfComputersProperty().set(parent.numberOfComputersProperty().get() - 1);
                parent.computerStatusProperty().set(parent.numberOfComputersProperty().get() + "/" + parent.getComputers().size());
            }

        });
        update.addListener(change -> {
            if(printout.get())
            {
                if (out != null) {
                    writeToConsole();
                }
            }

        });
        printout.addListener(change -> {
            if(change == printout) {

            }
            if(!printout.get())     //symuluj radiobutton (co najmniej jeden musi być zaznaczony) ale we wszystkich salach
            {
                boolean exclusive = false;
                if (labs != null) {
                    for (LabRoom r : labs)
                        for (Computer c : r.getComputers()) {
                            if (c != this)
                                exclusive |= c.printout.get();
                        }
                }

                if(!exclusive) {
                    printout.set(true);
                }
            }
            else {                  //jeśli piszemy do okienka konsoli

                if (labs != null) {
                    for(LabRoom r : labs)
                        for (Computer c : r.getComputers()) {
                            if (c != this)
                                c.setPrintout(false);
                        }
                }
                if (out != null) {
                    writeToConsole();
                }
            }
        });
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getAddress() {
        return address.get();
    }

    public SimpleStringProperty addressProperty() {
        return address;
    }

    public void setAddress(String address) {
        this.address.set(address);
    }





    public StatusType getStat() {
        return stat;
    }


    public void setStat(StatusType stat) {
        this.stat = stat;
        switch(stat) {
            case OFFLINE:
            case DISCONNECTED:
            case TIME_OUT:
            case CONNECTING:
            case UNKNOWN:
                clearCmdExitStatus();
                break;
        }
        status.set(stat.toString());
    }

    public void setParent(LabRoom labRoom) {
        parent = labRoom;
    }

    public void clearBuffer() {
        if(out != null)
            out.clear();
    }

    public void writeAll(String str) {
            out.writeAll(str);
            //refresh();
            //writeToConsole();
    }

    public void write(byte[] buf, int i) {
        try {
            out.write(buf,0,i);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//          writeToConsole();
    }

    private void writeToConsole() {
        if(out != null && printout.get()) {
            out.printToConsole();
        }
    }

    public void refresh() {
        update.set(!update.get());
        //printout.set(printout.get());
    }

    public void clearCmdExitStatus() {
        cmdExitStatus.set("");
    }

    public void setCmdExitStatus(String message) {
        cmdExitStatus.set(message);
    }

    public void abortCommand() {
        abortCommand.set(true);
    }

    @JsonIgnore
    public boolean isAborted() {
        return abortCommand.get();
    }
    @JsonIgnore
    public boolean isBg() {
        return bgCommand.get();
    }
    public void bgCommand() {
        bgCommand.set(true);
    }

    public void resetFlags() {
        bgCommand.set(false);
        abortCommand.set(false);
    }
}
