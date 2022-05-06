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

    opens com.praca.remoteadmin to javafx.fxml;
    exports com.praca.remoteadmin;
    exports com.praca.remoteadmin.Controller;
    exports com.praca.remoteadmin.Model;
    opens com.praca.remoteadmin.Controller to javafx.fxml;
    opens com.praca.remoteadmin.Model to javafx.fxml;
}