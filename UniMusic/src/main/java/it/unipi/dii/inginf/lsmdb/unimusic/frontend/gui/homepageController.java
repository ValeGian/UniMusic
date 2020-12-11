package it.unipi.dii.inginf.lsmdb.unimusic.frontend.gui;

import it.unipi.dii.inginf.lsmdb.unimusic.frontend.MiddlewareConnector;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Song;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.User;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class homepageController implements Initializable {
    private MiddlewareConnector connector;

    @FXML private ScrollPane scrollPane;
    @FXML private VBox verticalScroll;

    @FXML private ScrollPane hotSongsScroll;
    @FXML private HBox hotSongsPane;

    @FXML private ScrollPane suggPlaylistsScroll;
    @FXML private HBox suggPlaylistsPane;

    @FXML private ScrollPane suggUsersScroll;
    @FXML private HBox suggUsersPane;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        connector = MiddlewareConnector.getInstance();

        scrollPane.setFitToWidth(true);
        verticalScroll.setSpacing(75);

        hotSongsScroll.setFitToHeight(true); hotSongsScroll.setMinViewportHeight(340);
        hotSongsPane.setSpacing(5);

        suggPlaylistsScroll.setFitToHeight(true); suggPlaylistsScroll.setMinViewportHeight(340);
        suggPlaylistsPane.setSpacing(5);

        suggUsersScroll.setFitToHeight(true); suggUsersScroll.setMinViewportHeight(100);
        suggUsersPane.setSpacing(20);

        displayHotSongs();
        displaySuggPlaylists();
        displaySuggUsers();

    }

    //--------------------------------------------------------------------------------------------------------

    private void displayHotSongs() {
        List<Song> hotSongs = connector.getHotSongs();
        if(hotSongs.size() == 0)
            displayEmpty(hotSongsPane);
        else {
            for(Song song: hotSongs) {
                hotSongsPane.getChildren().add(createSongPreview(song));
            }
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
        List<User> suggUsers = connector.getSuggestedUsers();
        if(suggUsers.size() == 0)
            displayEmpty(suggUsersPane);
        else {
            for(User user: suggUsers) {
                suggUsersPane.getChildren().add(createUserPreview(user));
            }
        }
    }

    //--------------------------------------------------------------------------------------------------------

    private void displayEmpty(Pane pane) {
        pane.getChildren().clear();

        Text emptyText = new Text("<EMPTY>");
        emptyText.setStyle("-fx-font-size: 28");
        emptyText.getStyleClass().add("text-id");

        pane.getChildren().add(emptyText);
    }

    private Button createSongPreview(Song song) {
        Button songPreview = new Button(); songPreview.setStyle("-fx-background-color: transparent");
        Image songImage;
        VBox songGraphic = new VBox(5);
        try {
            songImage = new Image(
                    song.getAlbum().getImage(),
                    App.previewImageWidth,
                    0,
                    true,
                    true,
                    true
            );

            if(songImage.isError()) {
                throw new Exception();
            }

        }catch(Exception ex){
            songImage = new Image(
                    "file:src/main/resources/it/unipi/dii/inginf/lsmdb/unimusic/frontend/gui/img/empty.jpg",
                    App.previewImageWidth,
                    0,
                    true,
                    true,
                    true
            );
        }

        ImageView songImageView = new ImageView(songImage);
        Text title = new Text(song.getTitle()); title.setWrappingWidth(App.previewImageWidth); title.setFill(Color.WHITE);
        Text artist = new Text(song.getArtist()); artist.setWrappingWidth(App.previewImageWidth); artist.setFill(Color.GRAY);

        songGraphic.getChildren().addAll(songImageView, title, artist);

        songPreview.setGraphic(songGraphic);
        return songPreview;
    }

    private Button createUserPreview(User user) {
        Button songPreview = new Button(); songPreview.setStyle("-fx-background-color: transparent");

        Text username = new Text(user.getUsername()); username.setFill(Color.WHITE);

        songPreview.setGraphic(username);
        return songPreview;
    }
}