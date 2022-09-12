package com.praca.remoteadmin.Model;

import java.util.LinkedList;
import java.util.List;

public class MainGroup {
    List<LabRoom> labs = new LinkedList<>();

    public List<LabRoom> getLabs() {
        return labs;
    }

    public void setLabs(List<LabRoom> labs) {
        this.labs = labs;
    }
}
