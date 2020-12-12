package it.unipi.dii.inginf.lsmdb.unimusic.frontend.gui;

import it.unipi.dii.inginf.lsmdb.unimusic.frontend.MiddlewareConnector;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Song;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;


public class searchBarController implements Initializable {
    private static MiddlewareConnector connector = MiddlewareConnector.getInstance();

    @FXML private Button searchButton;
    @FXML private TextField searchInput;

    @FXML private CheckBox songCheck;
    @FXML private CheckBox artistCheck;
    @FXML private CheckBox albumCheck;

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
    }

    private void displaySong(List<Song> songsFiltered) {

       resultBox.getChildren().clear();
        for(Song song: songsFiltered)
            resultBox.getChildren().add(getLabel(song));

        Button closeButton = new Button("CLOSE");
        closeButton.defaultButtonProperty();
        closeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                System.out.println("PROVO BOTTONE");
                resultBox.getChildren().clear();
            }
        });

        resultBox.getChildren().add(closeButton);
        resultBox.setVisible(true);
    }

    private Label getLabel(Song song) {
        Label label = new Label();
        label.setText(song.getTitle()); label.setTextFill(Color.WHITE);
        return label;
    }

    private String findSelected() {
        if(artistCheck.isSelected())
            return artistCheck.getText();
        else if(albumCheck.isSelected())
            return albumCheck.getText();
        else
            return songCheck.getText();
    }

    private void initializeCheckBox() {

        songCheck.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                artistCheck.setSelected(false);
                albumCheck.setSelected(false);
            }
        });

        artistCheck.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                songCheck.setSelected(false);
                albumCheck.setSelected(false);
            }
        });

        albumCheck.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                songCheck.setSelected(false);
                artistCheck.setSelected(false);
            }
        });

    }
}
