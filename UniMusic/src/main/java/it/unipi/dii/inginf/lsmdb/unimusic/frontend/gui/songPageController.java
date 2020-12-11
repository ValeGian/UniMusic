package it.unipi.dii.inginf.lsmdb.unimusic.frontend.gui;

import it.unipi.dii.inginf.lsmdb.unimusic.frontend.MiddlewareConnector;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Song;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class songPageController implements Initializable {
    private MiddlewareConnector connector;
    private Song songToDisplay;

    @FXML private Button favouriteButton;
    @FXML private Button playlistButton;
    @FXML private Button likeButton;

    @FXML private TextField titleText;
    @FXML private TextField artistText;
    @FXML private TextField albumText;
    @FXML private TextField releasedYearText;
    @FXML private TextField genreText;

    @FXML private Label ratingLabel;
    @FXML private Label likeLabel;

    @FXML private TextField youtubeUrlText;
    @FXML private TextField spotifyUrlText;
    @FXML private TextField geniusUrlText;

    @FXML private ImageView imageAlbum;
    @FXML private ImageView favouriteImg;
    @FXML private ImageView likeImg;

    public songPageController() {
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        connector = MiddlewareConnector.getInstance();

        initializeThePage();
    }

    private void initializeThePage(){

        setAlbumImage();
        displaySongInformation();
        initializeButton();
        setUrls();
    }

    private void setUrls() {

        youtubeUrlText.setText("YOUTUBE URL:   " + songToDisplay.getYoutubeMediaURL());
        spotifyUrlText.setText("SPOTIFY URL:   " + songToDisplay.getSpotifyMediaURL());
        geniusUrlText.setText("GENIUS URL:   " + songToDisplay.getGeniusMediaURL());
    }

    private void initializeButton() {

        if(connector.isLikedSong(songToDisplay)){
            likeImg.setImage(new Image("file:src/main/resources/it/unipi/dii/inginf/lsmdb/unimusic/frontend/gui/img/like.png"));
        }else{
            likeImg.setImage(new Image("file:src/main/resources/it/unipi/dii/inginf/lsmdb/unimusic/frontend/gui/img/nonLike.png"));
        }

        if(connector.isFavouriteSong(songToDisplay)){
            favouriteImg.setImage(new Image("file:src/main/resources/it/unipi/dii/inginf/lsmdb/unimusic/frontend/gui/img/heart.png"));
        }else{
            favouriteImg.setImage(new Image("file:src/main/resources/it/unipi/dii/inginf/lsmdb/unimusic/frontend/gui/img/nonHeart.png"));
        }

        likeLabel.setText("Like: " + songToDisplay.getLikeCount());
        ratingLabel.setText("Rating:" + songToDisplay.getRating());
    }

    private void setAlbumImage() {
    }

    private void displaySongInformation(){

        titleText.setText(songToDisplay.getTitle());
        artistText.setText(songToDisplay.getArtist());
        albumText.setText(songToDisplay.getAlbum().getTitle());
        releasedYearText.setText("" + songToDisplay.getReleaseYear());
        genreText.setText(songToDisplay.getGenre());
    }

    public void getSongPage(Song song) throws IOException {
        songToDisplay = song;
        System.out.println(song.getYoutubeMediaURL());
        System.out.println("sdjfbjkdsbf");
        App.setRoot("songPage");

    }
}
