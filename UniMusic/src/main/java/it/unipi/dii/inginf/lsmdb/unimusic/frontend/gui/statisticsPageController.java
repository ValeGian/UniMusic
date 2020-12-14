package it.unipi.dii.inginf.lsmdb.unimusic.frontend.gui;

import it.unipi.dii.inginf.lsmdb.unimusic.frontend.MiddlewareConnector;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class statisticsPageController implements Initializable {
    private static MiddlewareConnector connector = MiddlewareConnector.getInstance();
    private static final StatisticToShow defaultStatistic = StatisticToShow.POPULAR_ARTISTS;
    private static StatisticToShow statisticToShow = defaultStatistic;
    private static final int previewImageHeight = 100;

    @FXML private AnchorPane parentPane;

    @FXML private Button popularArtistsButton;
    @FXML private Button topAlbumForDecadeButton;
    @FXML private Button topFavouriteGenresButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeButtons();
        displayStatisticToShow();
    }

    //--------------------------------------------------------------------------------------------------------

    private void initializeButtons() {
        popularArtistsButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                StatisticToShow oldStatistic = statisticToShow;
                statisticToShow = StatisticToShow.POPULAR_ARTISTS;
                try {
                    App.setRoot("statistics");
                } catch (IOException e) {
                    statisticToShow = oldStatistic;
                    e.printStackTrace();
                }
            }
        });

        topAlbumForDecadeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                StatisticToShow oldStatistic = statisticToShow;
                statisticToShow = StatisticToShow.TOP_ALBUM_FOR_DECADE;
                try {
                    App.setRoot("statistics");
                } catch (IOException e) {
                    statisticToShow = oldStatistic;
                    e.printStackTrace();
                }
            }
        });

        topFavouriteGenresButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                StatisticToShow oldStatistic = statisticToShow;
                statisticToShow = StatisticToShow.TOP_FAVOURITE_GENRES;
                try {
                    App.setRoot("statistics");
                } catch (IOException e) {
                    statisticToShow = oldStatistic;
                    e.printStackTrace();
                }
            }
        });

    }

    private void displayStatisticToShow() {
        switch (statisticToShow) {
            case POPULAR_ARTISTS:
                popularArtistsButton.setTextFill(Color.WHITE);
                displayPopularArtists();
                break;
            case TOP_ALBUM_FOR_DECADE:
                topAlbumForDecadeButton.setTextFill(Color.WHITE);
                displayTopAlbumForDecade();
                break;
            case TOP_FAVOURITE_GENRES:
                topFavouriteGenresButton.setTextFill(Color.WHITE);
                displayTopFavouriteGenres();
        }

        statisticToShow = defaultStatistic;
    }


    //--------------------------------------------------------------------------------------------------------

    private void displayPopularArtists() {

    }

    private void displayTopAlbumForDecade() {

    }

    private void displayTopFavouriteGenres() {

    }

    //--------------------------------------------------------------------------------------------------------

    private void displayEmpty(Pane pane) {
        pane.getChildren().clear();

        Text emptyText = new Text("<EMPTY>");
        emptyText.setStyle("-fx-font-size: 28");
        emptyText.getStyleClass().add("text-id");

        pane.getChildren().add(emptyText);
    }

    private enum StatisticToShow {
        POPULAR_ARTISTS,
        TOP_ALBUM_FOR_DECADE,
        TOP_FAVOURITE_GENRES
    }
}
