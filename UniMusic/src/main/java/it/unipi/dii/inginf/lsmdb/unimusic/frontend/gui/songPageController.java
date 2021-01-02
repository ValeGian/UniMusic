package it.unipi.dii.inginf.lsmdb.unimusic.frontend.gui;

import it.unipi.dii.inginf.lsmdb.unimusic.frontend.MiddlewareConnector;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Playlist;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Song;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.ResourceBundle;

public class songPageController implements Initializable {
    private static MiddlewareConnector connector = MiddlewareConnector.getInstance();
    private static Song songToDisplay;

    @FXML private Button favouriteButton;
    @FXML private Button playlistButton;
    @FXML private Button likeButton;
    @FXML private Button closeListButton;
    @FXML private Button youtubeButton;
    @FXML private Button spotifyButton;
    @FXML private Button geniusButton;

    @FXML private TextField titleText;
    @FXML private TextField artistText;
    @FXML private TextField albumText;
    @FXML private TextField releasedYearText;
    @FXML private TextField genreText;

    @FXML private Label ratingLabel;
    @FXML private Label likeLabel;
    @FXML private Label youtubeUrlText;
    @FXML private Label spotifyUrlText;
    @FXML private Label geniusUrlText;

    @FXML private ImageView imageAlbum;
    @FXML private ImageView favouriteImg;
    @FXML private ImageView likeImg;

    @FXML private ScrollPane scrollList;
    @FXML private ListView playlistList;


    /**
     * @param song The song to be displayed in the page.
     * @throws IOException
     */
    public static void getSongPage(Song song) throws IOException {
        songToDisplay = connector.getSongById(song);
        App.setRoot("songPage");

    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setAlbumImage();
        displaySongInformation();
        initializeButton();
        setUrls();
    }


    /**
     * Set the image of the album in the page.
     */
    private void setAlbumImage() {
        Image songImage;
        try {
            songImage = new Image(songToDisplay.getAlbum().getImage(),310,0,true,true,true);

            if(songImage.isError()) {
                throw new Exception();
            }

        }catch(Exception ex){
            String filePath = "file:src/main/resources/it/unipi/dii/inginf/lsmdb/unimusic/frontend/gui/img/empty.jpg";
            songImage = new Image(filePath,310,0,true,true,true);
        }

        imageAlbum.setImage(songImage);
    }


    /**
     * Add all song's information.
     */
    private void displaySongInformation(){

        titleText.setText(songToDisplay.getTitle());

        String artists = "Artist: " + songToDisplay.getArtist();
        if(songToDisplay.getFeaturedArtists().size() != 0) {
            artists +=" (feat ";
            for (String artist : songToDisplay.getFeaturedArtists())
                artists += artist + ", ";
            artists = artists.substring(0, artists.length()-2) + ")";
        }
        artistText.setText(artists);

        albumText.setText("Album: " + songToDisplay.getAlbum().getTitle());

        String releasedYear = (songToDisplay.getReleaseYear() == 0)?"Release year unknown":("Released year: " + songToDisplay.getReleaseYear());
        releasedYearText.setText(releasedYear);

        genreText.setText("Genre: " + songToDisplay.getGenre());
    }


    /**
     * Initializes all the actions associated to the button in the page.
     */
    private void initializeButton() {

        likeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) { handleLike(); }});

        favouriteButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) { handleFavourite(); }});

        playlistButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) { handlePlaylist(); }});

        List<Playlist> allUserPlaylist = connector.getUserPlaylists();

        for(Playlist plToAdd: allUserPlaylist)
            playlistList.getItems().add(getPlaylistButton(plToAdd));

        closeListButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                closeListButton.setVisible(false);
                scrollList.setVisible(false);
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

    private void handlePlaylist() {
        closeListButton.setVisible(true);
        scrollList.setVisible(true);
    }

    private Node getPlaylistButton(Playlist plToAdd) {

        Button buttonPlaylist = new Button(plToAdd.getName());buttonPlaylist.setStyle("-fx-background-color: transparent");buttonPlaylist.setTextAlignment(TextAlignment.LEFT);

        buttonPlaylist.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    connector.addSong(plToAdd, songToDisplay);
                    Button thisButton = (Button) actionEvent.getSource();
                    thisButton.setText("Successfully added to " + thisButton.getText());
                    thisButton.setDisable(true);
                } catch (ActionNotCompletedException e) {
                    e.printStackTrace();
                }
            }
        });

        return buttonPlaylist;
    }

    private void handleLike() {
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
            ex.printStackTrace();
        }
    }

    private void handleFavourite() {
        try {
            if (favouriteImg.getImage().getUrl().endsWith("nonHeart.png")) {
                favouriteImg.setImage(new Image("file:src/main/resources/it/unipi/dii/inginf/lsmdb/unimusic/frontend/gui/img/heart.png"));
                connector.addSongToFavourites(songToDisplay);
            }else{
                System.out.println("OK2!");
                favouriteImg.setImage(new Image("file:src/main/resources/it/unipi/dii/inginf/lsmdb/unimusic/frontend/gui/img/nonHeart.png"));
                connector.removeSongFromFavourites(songToDisplay);
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }


    /**
     * Set the spots to other web site to display where is possible to listen to this song.
     */
    private void setUrls() {

        youtubeUrlText.setText("YOUTUBE URL:   " + songToDisplay.getYoutubeMediaURL());
        youtubeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent actionEvent) { openUrl(songToDisplay.getYoutubeMediaURL()); }});

        spotifyUrlText.setText("SPOTIFY URL:   " + songToDisplay.getSpotifyMediaURL());
        spotifyButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent actionEvent) { openUrl(songToDisplay.getSpotifyMediaURL()); }});

        geniusUrlText.setText("GENIUS URL:   " + songToDisplay.getGeniusMediaURL());
        geniusButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent actionEvent) { openUrl(songToDisplay.getGeniusMediaURL()); }});

    }

    private void openUrl(String URL) {
        try {
            Desktop.getDesktop().browse(new URI(URL));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
