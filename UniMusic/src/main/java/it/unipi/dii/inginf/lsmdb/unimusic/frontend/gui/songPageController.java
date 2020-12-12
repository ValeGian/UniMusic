package it.unipi.dii.inginf.lsmdb.unimusic.frontend.gui;

import it.unipi.dii.inginf.lsmdb.unimusic.frontend.MiddlewareConnector;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Song;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;

public class songPageController implements Initializable {
    private static MiddlewareConnector connector = MiddlewareConnector.getInstance();
    private static Song songToDisplay;

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


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
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

        likeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    int numLike = Integer.parseInt(likeLabel.getText().split(": ")[1]);
                    if (likeImg.getImage().getUrl().endsWith("nonLike.png")) {
                        connector.likeSong(songToDisplay);
                        likeImg.setImage(new Image("file:src/main/resources/it/unipi/dii/inginf/lsmdb/unimusic/frontend/gui/img/like.png"));
                        likeLabel.setText("Like: " + (numLike + 1));
                    }else{
                        connector.deleteLike(songToDisplay);
                        likeImg.setImage(new Image("file:src/main/resources/it/unipi/dii/inginf/lsmdb/unimusic/frontend/gui/img/nonLike.png"));
                        likeLabel.setText("Like: " + (numLike - 1));
                    }
                }catch(Exception ex){

                }
            }
        });

        favouriteButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    if (favouriteImg.getImage().getUrl().endsWith("nonHeart.png")) {
                        connector.addSongToFavourites(songToDisplay);
                        favouriteImg.setImage(new Image("file:src/main/resources/it/unipi/dii/inginf/lsmdb/unimusic/frontend/gui/img/heart.png"));
                    }else{
                        connector.removeSongFromFavourites(songToDisplay);
                        favouriteImg.setImage(new Image("file:src/main/resources/it/unipi/dii/inginf/lsmdb/unimusic/frontend/gui/img/nonHeart.png"));
                    }
                }catch(Exception ex){

                }
            }
        });

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

        DecimalFormat formatter = new DecimalFormat();
        formatter.setMaximumFractionDigits(2);

        likeLabel.setText("Like: " + songToDisplay.getLikeCount());
        ratingLabel.setText("Rating: " + formatter.format(songToDisplay.getRating()));
    }

    private void setAlbumImage() {
        Image songImage;
        try {
            songImage = new Image(
                    songToDisplay.getAlbum().getImage(),
                    310,
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
                    310,
                    0,
                    true,
                    true,
                    true
            );
        }

        imageAlbum.setImage(songImage);
    }

    private void displaySongInformation(){

        titleText.setText(songToDisplay.getTitle());
        artistText.setText(songToDisplay.getArtist());
        albumText.setText(songToDisplay.getAlbum().getTitle());

        String releasedYear = (songToDisplay.getReleaseYear() == 0)?"Release year unknown":("" + songToDisplay.getReleaseYear());
        releasedYearText.setText(releasedYear);

        genreText.setText(songToDisplay.getGenre());
    }

    public static void getSongPage(Song song) throws IOException {
        songToDisplay = connector.getSongById(song);
        App.setRoot("songPage");

    }
}
