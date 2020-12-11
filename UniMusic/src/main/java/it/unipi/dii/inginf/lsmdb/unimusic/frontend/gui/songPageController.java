package it.unipi.dii.inginf.lsmdb.unimusic.frontend.gui;

import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Song;
import javafx.fxml.Initializable;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class songPageController implements Initializable {
    private Song songToDisplay;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void getSongPage(Song song) throws IOException {
        songToDisplay = song;
        App.setRoot("songPage");

    }
}
