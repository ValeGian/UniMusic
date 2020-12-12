package it.unipi.dii.inginf.lsmdb.unimusic.frontend.gui;

import it.unipi.dii.inginf.lsmdb.unimusic.frontend.MiddlewareConnector;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Song;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;


public class searchBarController implements Initializable {
    private static MiddlewareConnector connector = MiddlewareConnector.getInstance();

    @FXML private Button searchButton;
    @FXML private Button closeSearchButton;
    @FXML private TextField searchInput;

    @FXML private CheckBox titleCheck;
    @FXML private CheckBox artistCheck;
    @FXML private CheckBox albumCheck;

    @FXML private AnchorPane anchorSearch;

    @FXML private VBox resultBox;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeSearch();
        initializeCheckBox();
    }

    private void initializeSearch() {
        searchButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                String partialInput = searchInput.getText();
                String attributeField = findSelected();
                try {
                    List<Song> songsFiltered = connector.filterSong(partialInput, attributeField);
                    displaySong(songsFiltered);
                } catch (ActionNotCompletedException e) {
                    return;
                }
            }
        });

        closeSearchButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                resultBox.getChildren().clear();
                anchorSearch.setVisible(false);
            }
        });

    }

    private void displaySong(List<Song> songsFiltered) {

       resultBox.getChildren().clear();

       if(songsFiltered.size() != 0) {
           for (Song song : songsFiltered)
               resultBox.getChildren().addAll(createSongPreview(song), new Separator());
           if(songsFiltered.size() < 4) {
               TextField fill = new TextField("No more result found"); fill.setStyle("-fx-background-color: black;"); fill.setMinHeight((4 - songsFiltered.size()) * 150);fill.setMinWidth(500); fill.setAlignment(Pos.CENTER);
               resultBox.getChildren().add(fill);
           }
       }else{
           TextField empty = new TextField("No result found"); empty.setStyle("-fx-background-color: black;");empty.setMinWidth(500);empty.setPrefHeight(500); empty.setAlignment(Pos.CENTER);
           resultBox.getChildren().add(empty);
       }

        anchorSearch.setVisible(true);
    }


    private Button createSongPreview(Song song) {
        Button songPreview = new Button(); songPreview.setStyle("-fx-background-color: black"); songPreview.setMinWidth(500);
        songPreview.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                try {
                    songPageController.getSongPage(song);
                } catch (IOException e) {

                }
            }
        });

        Image songImage;
        HBox songGraphic = new HBox(2);
        try {
            songImage = new Image(
                    song.getAlbum().getImage(),
                    0,
                    100,
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
                    100,
                    140,
                    true,
                    true,
                    true
            );
        }

        ImageView songImageView = new ImageView(songImage);

        Text title = new Text(song.getTitle()); title.setWrappingWidth(300); title.setFill(Color.WHITE);
        Text artist = new Text(song.getArtist()); artist.setWrappingWidth(300);artist.setFill(Color.GRAY);

        VBox information = new VBox(title, artist);information.setPadding(new Insets(20, 0, 20, 20));

        songGraphic.getChildren().addAll(songImageView, information);

        songPreview.setGraphic(songGraphic);
        return songPreview;
    }


    private String findSelected() {
        if(artistCheck.isSelected())
            return artistCheck.getText();
        else if(albumCheck.isSelected())
            return albumCheck.getText();
        else
            return titleCheck.getText();
    }

    private void initializeCheckBox() {

        titleCheck.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                artistCheck.setSelected(false);
                albumCheck.setSelected(false);
            }
        });

        artistCheck.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                titleCheck.setSelected(false);
                albumCheck.setSelected(false);
            }
        });

        albumCheck.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                titleCheck.setSelected(false);
                artistCheck.setSelected(false);
            }
        });

    }
}
