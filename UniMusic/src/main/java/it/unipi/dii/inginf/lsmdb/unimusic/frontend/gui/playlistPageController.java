package it.unipi.dii.inginf.lsmdb.unimusic.frontend.gui;

import it.unipi.dii.inginf.lsmdb.unimusic.frontend.MiddlewareConnector;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Playlist;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Song;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.User;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class playlistPageController implements Initializable {
    private static Playlist playlistToDisplay;
    private static MiddlewareConnector connector = MiddlewareConnector.getInstance();

    @FXML private TextField titleText;
    @FXML private Label authorText;
    @FXML private ImageView imagePlaylist;
    @FXML private ImageView binImage;
    @FXML private ImageView modifyImage;

    @FXML private VBox songListBox;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        displayPlaylistInformation();

        //showing the information of every song in the playlist
        List<Song> songList = connector.getPlaylistSongs(playlistToDisplay);
        for (Song song: songList)
            songListBox.getChildren().add(getSongRecord(song));

        songListBox.setSpacing(5);
        songListBox.setAlignment(Pos.CENTER); songListBox.setFillWidth(true);
        //if the author of the playlist is the logged user, it has the possibility to delete the playlist
        if (playlistToDisplay.getAuthor().equals(connector.getLoggedUser().getUsername()) && !playlistToDisplay.isFavourite()) {
            modifyImage.setVisible(true);
            modifyImage.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>()
            {@Override public void handle(MouseEvent mouseEvent) { modifyPlaylistName();}});

            binImage.setVisible(true);
            binImage.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent event) {
                    connector.deletePlaylist(playlistToDisplay);
                    try {
                        App.setRoot("homepage");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void modifyPlaylistName() {
        if(modifyImage.getImage().getUrl().equals("file:src/main/resources/it/unipi/dii/inginf/lsmdb/unimusic/frontend/gui/img/modify.png")) {
            modifyImage.setImage(new Image("file:src/main/resources/it/unipi/dii/inginf/lsmdb/unimusic/frontend/gui/img/confirm.png"));
            titleText.setText("Insert the new Playlist name...");
            titleText.setEditable(true);
        }else{
            modifyImage.setImage(new Image("file:src/main/resources/it/unipi/dii/inginf/lsmdb/unimusic/frontend/gui/img/modify.png"));
            titleText.setEditable(false);
            connector.setPlaylistName(playlistToDisplay, titleText.getText());
        }
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
        //adding the event to move to the song page by clicking it
        authorText.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                try {
                    userPageController.getUserPage(new User(playlistToDisplay.getAuthor()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                event.consume();
            }
        });
        setPlaylistImage();

    }

    private void setPlaylistImage() {
        Image playlistImage;
        try {
            if (playlistToDisplay.getUrlImage() == null || playlistToDisplay.getUrlImage().equals(""))
                throw new Exception();
            playlistImage = new Image(
                    playlistToDisplay.getUrlImage(),
                    310,
                    0,
                    true,
                    true,
                    true
            );
        }
        catch(Exception ex) {
            playlistImage = new Image(
                    "file:src/main/resources/it/unipi/dii/inginf/lsmdb/unimusic/frontend/gui/img/empty.jpg",
                    310,
                    0,
                    true,
                    true,
                    true
            );
        }

        imagePlaylist.setImage(playlistImage);
    }

    private HBox getSongRecord(Song song){
        HBox songBox;

        Label title = new Label(song.getTitle()); title.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        Label artist = new Label(song.getArtist()); artist.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        ImageView songImageView = new ImageView();
        setSongImage(songImageView, song);

        title.setMinWidth(600); title.setTextAlignment(TextAlignment.CENTER);
        artist.setMinWidth(340); artist.setTextAlignment(TextAlignment.CENTER);

        ImageView heartImageView = new ImageView(); heartImageView.setStyle("-fx-cursor: hand;");
        heartImageView.setPickOnBounds(true);
        if(connector.isFavouriteSong(song)){
            heartImageView.setImage(new Image("file:src/main/resources/it/unipi/dii/inginf/lsmdb/unimusic/frontend/gui/img/heart.png"));
        }else{
            heartImageView.setImage(new Image("file:src/main/resources/it/unipi/dii/inginf/lsmdb/unimusic/frontend/gui/img/nonHeart.png"));
        }
        //Image heartImage = new Image("file:src/main/resources/it/unipi/dii/inginf/lsmdb/unimusic/frontend/gui/img/heart.png");
        //heartImageView.setImage(heartImage); heartImageView.setY(60);

        //adding the possibility to click on the heart to add a song to favourites
        heartImageView.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                try {
                    if (heartImageView.getImage().getUrl().endsWith("nonHeart.png")) {
                        connector.addSongToFavourites(song);
                        heartImageView.setImage(new Image("file:src/main/resources/it/unipi/dii/inginf/lsmdb/unimusic/frontend/gui/img/heart.png"));
                    }else{
                        connector.removeSongFromFavourites(song);
                        heartImageView.setImage(new Image("file:src/main/resources/it/unipi/dii/inginf/lsmdb/unimusic/frontend/gui/img/nonHeart.png"));
                    }
                }catch(Exception ex){
                    try {
                        connector.addSongToFavourite(connector.getLoggedUser(), song);
                    } catch (ActionNotCompletedException e) {
                        e.printStackTrace();
                    }
                } 
                event.consume();
            }
        });


        HBox songInformationBox = new HBox(songImageView, title, artist); songInformationBox.setStyle("-fx-cursor: hand;");
        songInformationBox.setAlignment(Pos.CENTER); songInformationBox.setSpacing(5);
        //adding the event to move to the song page by clicking it
        songInformationBox.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

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

        //if the user is the owner of the playlist he can remove songs from the playlist
        if (playlistToDisplay.getAuthor().equals(connector.getLoggedUser().getUsername()) && !playlistToDisplay.isFavourite()) {
            ImageView removeSongImageView = new ImageView(); removeSongImageView.setStyle("-fx-cursor: hand;");
            removeSongImageView.setImage(new Image("file:src/main/resources/it/unipi/dii/inginf/lsmdb/unimusic/frontend/gui/img/delete.png"));

            songBox = new HBox(heartImageView, songInformationBox, removeSongImageView);

            removeSongImageView.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent event) {
                    connector.deleteSongFromPlaylist(song, playlistToDisplay);
                    songListBox.getChildren().remove(songBox);
                }
            });
        }
        else
            songBox = new HBox(heartImageView, songInformationBox);

        songBox.setStyle("-fx-background-color: transparent");
        songBox.setAlignment(Pos.CENTER); songBox.setSpacing(10);
        
        return songBox;
    }

    private void setSongImage(ImageView imageView, Song song) {
        Image songImage;

        songImage = new Image(
                "file:src/main/resources/it/unipi/dii/inginf/lsmdb/unimusic/frontend/gui/img/empty.jpg",
                50,
                0,
                true,
                true,
                true
        );

        try {
            songImage = new Image(
                    song.getAlbum().getImage(),
                    50,
                    0,
                    true,
                    true,
                    true
            );

            if(songImage.isError()) {
                throw new Exception();
            }

        }catch(Exception ex){

        }
        imageView.setImage(songImage);


    }
}
