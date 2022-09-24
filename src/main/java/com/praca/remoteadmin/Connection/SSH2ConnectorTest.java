package com.praca.remoteadmin.Connection;

import com.praca.remoteadmin.Model.LabRoom;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.praca.remoteadmin.Utils.DataFormatEnum.JSON;
import static org.junit.jupiter.api.Assertions.*;

class SSH2ConnectorTest {

    private ObservableList<LabRoom> sale;

    @BeforeEach
    void setUp() {
        sale = ConnectionHelper.loadData(JSON);
    }

    @Test
    void onConnection() {

    }

    @Test
    void execCommand() {
    }
}