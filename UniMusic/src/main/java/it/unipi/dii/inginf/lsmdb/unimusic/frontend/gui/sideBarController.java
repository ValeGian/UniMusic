package it.unipi.dii.inginf.lsmdb.unimusic.frontend.gui;

import it.unipi.dii.inginf.lsmdb.unimusic.frontend.MiddlewareConnector;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.PrivilegeLevel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class sideBarController implements Initializable {
    private MiddlewareConnector connector;

    @FXML private AnchorPane parentPane;

    @FXML private Button home;
    @FXML private Button favourites;
    @FXML private Button addPlaylist;
    @FXML private Button personalProfile;
    @FXML private Button statistics;
    @FXML private Button logout;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        connector = MiddlewareConnector.getInstance();

        home.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    App.setRoot("homepage");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        if ( connector.getLoggedUser().getPrivilegeLevel() == null
                || connector.getLoggedUser().getPrivilegeLevel() != PrivilegeLevel.ADMIN
        ) {
            parentPane.getChildren().remove(statistics);
        } else {
            statistics.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    try {
                        App.setRoot("statistics");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        personalProfile.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    userPageController.getUserPage(MiddlewareConnector.getInstance().getLoggedUser());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        logout.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                connector.logout();

                try {
                    App.setRoot("welcome");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
