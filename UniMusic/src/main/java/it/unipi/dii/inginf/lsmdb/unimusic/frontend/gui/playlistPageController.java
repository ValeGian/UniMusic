package it.unipi.dii.inginf.lsmdb.unimusic.frontend.gui;

import it.unipi.dii.inginf.lsmdb.unimusic.frontend.MiddlewareConnector;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Playlist;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Song;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.User;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.ResourceBundle;

public class playlistPageController implements Initializable {
    private static Playlist playlistToDisplay;
    private static MiddlewareConnector connector = MiddlewareConnector.getInstance();

    @FXML private TextField titleText;
    @FXML private TextField authorText;
    @FXML private ImageView imagePlaylist;

    @FXML private VBox songListBox;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        displayPlaylistInformation();

    }

    public static void getPlaylistPage(Playlist playlist) throws IOException {
        try {
            playlistToDisplay = connector.getPlaylistById(playlist.getID());
        } catch (ActionNotCompletedException e) {
            e.printStackTrace();
        }
        App.setRoot("playlistPage");

    }

    private void displayPlaylistInformation(){
        titleText.setText(playlistToDisplay.getName());
        authorText.setText("Created by " + playlistToDisplay.getAuthor());
        setPlaylistImage();

        List<Song> songList = connector.getPlaylistSongs(playlistToDisplay);
        for (Song song: songList)
            songListBox.getChildren().add(getSongRecord(song));
    }

    private void setPlaylistImage() {
        Image playlistImage;
        if (playlistToDisplay.getUrlImage() != null)
            playlistImage = new Image(
                    playlistToDisplay.getUrlImage(),
                    310,
                    0,
                    true,
                    true,
                    true
            );
        else
            playlistImage = new Image(
                    "file:src/main/resources/it/unipi/dii/inginf/lsmdb/unimusic/frontend/gui/img/empty.jpg",
                    310,
                    0,
                    true,
                    true,
                    true
            );
        imagePlaylist.setImage(playlistImage);
    }

    private HBox getSongRecord(Song song){
        HBox songBox;

        TextField title = new TextField(song.getTitle()); title.setStyle("-fx-background-color: transparent");
        TextField artist = new TextField(song.getArtist()); artist.setStyle("-fx-background-color: transparent");
        ImageView songImageView = new ImageView();
        setSongImage(songImageView, song);

        title.setMinWidth(400); artist.setMinWidth(300);

        ImageView heartImageView = new ImageView();
        Image heartImage = new Image("file:src/main/resources/it/unipi/dii/inginf/lsmdb/unimusic/frontend/gui/img/heart.png");
        heartImageView.setImage(heartImage); heartImageView.setY(60);

        //adding the possibility to click on the heart to add a song to favourites
        heartImageView.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                connector.addSongToFavourite(connector.getLoggedUser(), song);
                event.consume();
            }
        });

        //adding the event to move to the song page by clicking it
        title.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                try {
                    songPageController.getSongPage(song);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                event.consume();
            }
        });
        songBox = new HBox(heartImageView, songImageView, title, artist); songBox.setStyle("-fx-background-color: transparent");
        songBox.setPrefHeight(120);

        return songBox;
    }

    private void setSongImage(ImageView imageView, Song song) {
        Image songImage;

        try {
            songImage = new Image(
                    song.getAlbum().getImage(),
                    90,
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
                    90,
                    0,
                    true,
                    true,
                    true
            );
        }

        imageView.setImage(songImage);
    }
}
