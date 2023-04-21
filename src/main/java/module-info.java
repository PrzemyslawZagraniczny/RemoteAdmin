module com.praca.remoteadmin {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.desktop;
    requires jsch;
    requires org.junit.jupiter.api;
    requires log4j;
    requires com.fasterxml.jackson.databind;
    requires annotations;
    requires jacpfx.JavaFX;

    opens com.praca.remoteadmin to javafx.fxml;
    exports com.praca.remoteadmin;
    exports com.praca.remoteadmin.Controller;
    exports com.praca.remoteadmin.Model;
    opens com.praca.remoteadmin.Controller to javafx.fxml;
    opens com.praca.remoteadmin.Model to javafx.fxml;
    exports com.praca.remoteadmin.Brudnopis;
    opens com.praca.remoteadmin.Brudnopis to javafx.fxml;
    exports com.praca.remoteadmin.GUI;
    opens com.praca.remoteadmin.GUI to javafx.fxml;
    exports com.praca.remoteadmin.Utils;
    opens com.praca.remoteadmin.Utils to javafx.fxml;
}