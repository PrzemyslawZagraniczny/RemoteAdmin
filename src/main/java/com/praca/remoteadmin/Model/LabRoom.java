package com.praca.remoteadmin.Model;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;

import java.util.LinkedList;
import java.util.List;

public class LabRoom  {
    public void setComputers(List<Computer> computers) {
        this.computers = computers;
        compsObsrv = FXCollections.observableArrayList(computers);
        //numberOfComputers.bind(Bindings.size(compsObsrv));
        numberOfComputers.bind(new IntegerBinding() {
            @Override
            protected int computeValue() {
                int retVal = 0;
                for(Computer c : compsObsrv) {
                    retVal = c.isSelected()?retVal+1:retVal;
                }
                return retVal;
            }
        });
    }

    public void setName(String name) {
        this.name = name;
    }

    private List<Computer> computers = null;
    ObservableList<Computer> compsObsrv = null;
    private int id;
    private String name;
    private SimpleBooleanProperty selected = new SimpleBooleanProperty(true);//new SimpleBooleanProperty
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

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    public LabRoom() {
        computers = new LinkedList<>();
        selected.set(true);
        compsObsrv = FXCollections.observableArrayList(computers);

        numberOfComputers.multiply(computers.size());
        selected.addListener(change -> {
            for(Computer c : computers) {
                c.setSelected(selected.get());
            }
        });

    }
    public LabRoom(int id, String name) {
        this.id = id;
        this.name = name;
        selected.set(true);
        selected.addListener(change -> {
            for(Computer c : computers) {
                c.setSelected(selected.get());
            }
        });


        computers = new LinkedList<>();
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

}
