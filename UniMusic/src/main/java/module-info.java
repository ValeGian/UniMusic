module it.unipi.dii.inginf.lsmdb.unimusic.frontend.gui {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.mongodb.driver.sync.client;
    requires org.neo4j.driver;

    opens it.unipi.dii.inginf.lsmdb.unimusic.frontend.gui to javafx.fxml;
    exports it.unipi.dii.inginf.lsmdb.unimusic.frontend.gui;
}