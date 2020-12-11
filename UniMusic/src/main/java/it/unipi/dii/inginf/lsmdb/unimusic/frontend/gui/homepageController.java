package it.unipi.dii.inginf.lsmdb.unimusic.frontend.gui;

import it.unipi.dii.inginf.lsmdb.unimusic.frontend.MiddlewareConnector;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Song;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class homepageController implements Initializable {
    private MiddlewareConnector connector;

    @FXML private ScrollPane scrollPane;
    @FXML private VBox verticalScroll;

    @FXML private HBox hotSongsPane;

    @FXML private HBox suggPlaylistsPane;
    @FXML private HBox suggUsersPane;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        connector = MiddlewareConnector.getInstance();

        scrollPane.setFitToWidth(true);
        verticalScroll.setSpacing(300);

        displayHotSongs();
        displaySuggPlaylists();
        displaySuggUsers();

    }

    //--------------------------------------------------------------------------------------------------------

    private void displayHotSongs() {
        try {
            List<Song> hotSongs = connector.getHotSongs();
            displayEmpty(hotSongsPane);
        } catch (ActionNotCompletedException e) {

        }
    }

    private void displaySuggPlaylists() {
        /*try {
            List<Song> hotSongs = connector.getHotSongs();
        } catch (ActionNotCompletedException e) {

        }
         */
        displayEmpty(suggPlaylistsPane);
    }

    private void displaySuggUsers() {
        /*try {
            List<Song> hotSongs = connector.getHotSongs();
        } catch (ActionNotCompletedException e) {

        }
         */
        displayEmpty(suggUsersPane);
    }

    //--------------------------------------------------------------------------------------------------------

    private void displayEmpty(Pane pane) {
        pane.getChildren().clear();

        Text emptyText = new Text("<EMPTY>");
        emptyText.setStyle("-fx-font-size: 28");
        emptyText.getStyleClass().add("text-id");

        pane.getChildren().add(emptyText);
    }
}