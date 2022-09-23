package com.praca.remoteadmin.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.LinkedList;
import java.util.List;

public class LabRoom  {
    public void setComputers(List<Computer> computers) {
        this.computers = computers;
        for(Computer c : computers) c.setParent(this);
        if(computers != null)
            compsObsrv = FXCollections.observableArrayList(computers);
        else
            compsObsrv = FXCollections.observableArrayList();
        //numberOfComputers.bind(Bindings.size(compsObsrv));
        int retVal = 0;
        for(Computer c : compsObsrv) {
            retVal = c.isSelected()?retVal+1:retVal;
        }
        numberOfComputers.set(retVal);

//        numberOfComputers.bind(new IntegerBinding() {
//            @Override
//            protected int computeValue() {
//                int retVal = 0;
//                for(Computer c : compsObsrv) {
//                    retVal = c.isSelected()?retVal+1:retVal;
//                }
//                return retVal;
//            }
//        });

        int cnt = 0;
        for(Computer c : compsObsrv) {
            cnt += c.isSelected()?1:0;
        }
        computerStatus.set(cnt+"/"+compsObsrv.size());
        compsObsrv.addListener(new ListChangeListener<Computer>() {
            @Override
            public void onChanged(Change<? extends Computer> change) {
                int retVal = 0;
                for(Computer c : compsObsrv) {
                    retVal = c.isSelected()?retVal+1:retVal;
                }

                numberOfComputers.set(retVal);

            }
        });
    }

    public void setName(String name) {
        this.name = name;
    }

    private List<Computer> computers = null;
    @JsonIgnore
    SimpleStringProperty computerStatus = new SimpleStringProperty("");

    @JsonIgnore
    ObservableList<Computer> compsObsrv = null;
    private int id;
    private String name;
    private SimpleBooleanProperty selected = new SimpleBooleanProperty(true);//new SimpleBooleanProperty
    public void setObserver(ISaveDataObserver observer) {
        this.observer = observer;
    }

    @JsonIgnore
    private ISaveDataObserver observer = null;
    @JsonIgnore
    private SimpleIntegerProperty numberOfComputers = new SimpleIntegerProperty(0);

    public int getNumberOfComputers() {
        return numberOfComputers.get();
    }

    public SimpleIntegerProperty numberOfComputersProperty() {
        return numberOfComputers;
    }

    public void setNumberOfComputers(int numberOfComputers) {
        this.numberOfComputers.set(numberOfComputers);
    }

    public boolean isSelected() {
        return selected.get();
    }

    public SimpleBooleanProperty selectedProperty() {
        return selected;
    }

    public String getComputerStatus() {
        return computerStatus.get();
    }

    public SimpleStringProperty computerStatusProperty() {
        return computerStatus;
    }

    public void setComputerStatus(String computerStatus) {
        this.computerStatus.set(computerStatus);
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
        if(observer != null) {
            observer.saveData();
        }
    }

    public LabRoom() {
        computers = new LinkedList<>();
        selected.set(true);
        compsObsrv = FXCollections.observableArrayList(computers);

        //numberOfComputers.multiply(computers.size());
        selected.addListener(change -> {
            for(Computer c : computers) {
                c.setSelected(selected.get());
            }
        });


    }
    public LabRoom(int id, String name) {
        this.id = id;
        this.name = name;
        compsObsrv = FXCollections.observableArrayList();
        computers = new LinkedList<>();

        selected.set(true);
        selected.addListener(change -> {
            for(Computer c : computers) {
                c.setSelected(selected.get());
            }
        });

        //FXCollections.observableArrayList(new Computer(name + "Komputer 1", name + "192.168.42.141", StatusType.UNKNOWN),
//            new Computer(name + "Komputer 1", name + "192.168.42.141", StatusType.UNKNOWN),
//            new Computer(name + "Komputer 2", name + "192.168.42.144", StatusType.UNKNOWN),
//            new Computer(name + "Komputer 3", name + "192.168.42.145", StatusType.UNKNOWN),
//            new Computer(name + "Komputer 4", name + "192.168.42.146", StatusType.UNKNOWN),
//            new Computer(name + "Komputer 5", name + "192.168.42.147", StatusType.UNKNOWN));

    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        //return "Sala " + name ;
        return name;
    }

    public ObservableList<Computer>  getComputers() {
        return compsObsrv;
    }

    public void appendComputer(Computer c) {
        compsObsrv.add(c);
    }
}
