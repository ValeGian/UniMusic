module it.unipi.dii.inginf.lsmdb.unimusic.gui {
    requires javafx.controls;
    requires javafx.fxml;

    opens it.unipi.dii.inginf.lsmdb.unimusic.gui to javafx.fxml;
    exports it.unipi.dii.inginf.lsmdb.unimusic.gui;
}