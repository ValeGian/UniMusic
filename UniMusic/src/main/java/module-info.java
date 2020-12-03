module it.unipi.dii.inginf.lsmdb.unimusic.frontend.gui {
    requires javafx.controls;
    requires javafx.fxml;

    opens it.unipi.dii.inginf.lsmdb.unimusic.frontend.gui to javafx.fxml;
    exports it.unipi.dii.inginf.lsmdb.unimusic.frontend.gui;
}