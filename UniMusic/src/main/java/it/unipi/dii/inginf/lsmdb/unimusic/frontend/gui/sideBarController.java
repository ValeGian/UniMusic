package it.unipi.dii.inginf.lsmdb.unimusic.frontend.gui;

import it.unipi.dii.inginf.lsmdb.unimusic.frontend.MiddlewareConnector;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Playlist;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;

import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.PrivilegeLevel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;



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

        favourites.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    playlistPageController.getPlaylistPage(connector.getFavourite());
                } catch (ActionNotCompletedException | IOException e) {
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

        addPlaylist.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                AnchorPane background;
                AnchorPane addPlaylistPane;
                Label labelName = new Label("Name of the playlist:");
                TextField nameForm = new TextField(); nameForm.setStyle("-fx-background-color: white; -fx-font-weight: bold; " +
                                                                        "-fx-font-size: 16px; -fx-text-fill: black;");
                nameForm.setMinWidth(480);
                Label labelUrlImage = new Label("Image of the playlist (optional):");
                TextField urlImageForm = new TextField(); urlImageForm.setStyle("-fx-background-color: white; -fx-font-weight: bold; " +
                                                                                "-fx-font-size: 16px; -fx-text-fill: black;");
                urlImageForm.setMinWidth(480);

                Button cancelButton = new Button("Cancel");
                Button addButton = new Button("Add");
                HBox buttons = new HBox(addButton, cancelButton); buttons.setAlignment(Pos.CENTER);
                buttons.setSpacing(20);

                VBox addPlaylistBox = new VBox(labelName, nameForm, labelUrlImage, urlImageForm, buttons);
                addPlaylistBox.setPrefSize(500,200); addPlaylistBox.setAlignment(Pos.CENTER);

                addPlaylistPane = new AnchorPane(addPlaylistBox); addPlaylistPane.toFront();
                addPlaylistPane.setPrefSize(500,200); addPlaylistPane.setStyle("-fx-background-color: #424242;\n" +
                                                                                     "  -fx-background-radius: 5px, 5px, 5px, 5px;");
                addPlaylistPane.setLayoutX(350); addPlaylistPane.setLayoutY(300);
                addPlaylistBox.setSpacing(10);

                background = new AnchorPane();
                background.setStyle("-fx-background-color: #494949; -fx-opacity: 0.7;");
                background.setPrefSize(1300, 750); background.toFront();
                parentPane.getChildren().addAll(background, addPlaylistPane);

                cancelButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        parentPane.getChildren().remove(background);
                        parentPane.getChildren().remove(addPlaylistPane);
                    }
                });

                addButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        Playlist playlist = new Playlist(connector.getLoggedUser().getUsername(), nameForm.getText());
                        if (!urlImageForm.getText().equals(""))
                            playlist.setUrlImage(urlImageForm.getText());

                        try {
                            connector.createPlaylist(playlist);
                        } catch (ActionNotCompletedException e) {
                            e.printStackTrace();
                        }
                        parentPane.getChildren().remove(background);
                        parentPane.getChildren().remove(addPlaylistPane);
                    }
                });
            }
        });
    }
}
