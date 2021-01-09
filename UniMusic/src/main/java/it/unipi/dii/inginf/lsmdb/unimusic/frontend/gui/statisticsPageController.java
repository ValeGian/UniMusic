package it.unipi.dii.inginf.lsmdb.unimusic.frontend.gui;

import it.unipi.dii.inginf.lsmdb.unimusic.frontend.MiddlewareConnector;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Album;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Pair;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.ResourceBundle;

public class statisticsPageController implements Initializable {
    private static MiddlewareConnector connector = MiddlewareConnector.getInstance();
    private static final StatisticToShow defaultStatistic = StatisticToShow.POPULAR_ARTISTS;
    private static StatisticToShow statisticToShow = defaultStatistic;
    private static final int previewImageHeight = 150;

    @FXML private Button popularArtistsButton;
    @FXML private Button topAlbumForDecadeButton;
    @FXML private Button topFavouriteGenresButton;
    @FXML private Button topArtistsPerAgeRangeButton;

    @FXML private TextField totalSongsLabel;
    @FXML private TextField totalUsersLabel;
    @FXML private TextField totalPlaylistsLabel;

    @FXML private VBox statisticPane;

    private HBox firstFilter;
    private HBox secondFilter;
    private Button commitQuery;
    private VBox loadPane;

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

        topArtistsPerAgeRangeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                StatisticToShow oldStatistic = statisticToShow;
                statisticToShow = StatisticToShow.TOP_ARTIST_FOR_AGE_RANGE;
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
                break;
            case TOP_ARTIST_FOR_AGE_RANGE:
                topArtistsPerAgeRangeButton.setTextFill(Color.WHITE);
                displayTopArtistForAgeRange();
        }

        displayNumberStatistics();

        statisticToShow = defaultStatistic;
    }


    //--------------------------------------------------------------------------------------------------------


    private void displayNumberStatistics() {
        totalSongsLabel.setText("Total songs: " + ((connector.getTotalSongs() == -1)?"Error counting": connector.getTotalSongs()));
        totalUsersLabel.setText("Total users: " + ((connector.getTotalSongs() == -1)?"Error counting": connector.getTotalUsers()));
        totalPlaylistsLabel.setText("Total playlists: " + ((connector.getTotalSongs() == -1)?"Error counting": connector.getTotalPlaylists()));
    }


    private void displayPopularArtists() {
        Text hitThreshold = new Text("Hit Threshold: "); hitThreshold.setFill(Color.WHITE); hitThreshold.setStyle("-fx-font-weight: bold; -fx-font-size: 20px");
        TextField hitThresholdInput = new TextField("5"); hitThresholdInput.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-font-size: 18px; -fx-max-width: 100px");
        hitThresholdInput.setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                String text = hitThresholdInput.getText();
                if(!text.matches("[0-9]+")) {
                    hitThresholdInput.setText(text.replaceAll("[^\\d]", ""));
                }
            }
        });
        firstFilter = new HBox(5, hitThreshold, hitThresholdInput);

        Text limit = new Text("How many Artists: "); limit.setFill(Color.WHITE); limit.setStyle("-fx-font-weight: bold; -fx-font-size: 20px");
        TextField limitInput = new TextField("10"); limitInput.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-font-size: 18px; -fx-max-width: 50px");
        limitInput.setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                String text = limitInput.getText();
                if(!text.matches("[0-9]{1,2}?")) {
                    limitInput.setText(text.replaceAll("[^\\d]", ""));
                    text = limitInput.getText();
                    int substring = text.length() > 2 ? 2 : text.length();
                    limitInput.setText(text.substring(0, substring));
                }
            }
        });
        secondFilter = new HBox(5, limit, limitInput);

        loadPane = new VBox(10);
        commitQuery = new Button("GET RESULTS");
        commitQuery.setStyle("-fx-background-color: green; -fx-font-weight: bold; -fx-text-fill: white");
        commitQuery.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                loadTopArtists(Integer.parseInt(hitThresholdInput.getText()), Integer.parseInt(limitInput.getText()));
            }
        });

        HBox inputContainer = new HBox(30, commitQuery, firstFilter, secondFilter);
        statisticPane.getChildren().addAll(inputContainer, loadPane);

        loadTopArtists(Integer.parseInt(hitThresholdInput.getText()), Integer.parseInt(limitInput.getText()));
    }

    private void displayTopAlbumForDecade() {
        loadPane = new VBox(10);
        statisticPane.getChildren().addAll(loadPane);
        loadTopAlbumForDecade();
    }

    private void displayTopFavouriteGenres() {
        Text limit = new Text("How many Genres: "); limit.setFill(Color.WHITE); limit.setStyle("-fx-font-weight: bold; -fx-font-size: 20px");
        TextField limitInput = new TextField("10"); limitInput.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-font-size: 18px; -fx-max-width: 50px");
        limitInput.setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                String text = limitInput.getText();
                if(!text.matches("[0-9]{1,2}?")) {
                    limitInput.setText(text.replaceAll("[^\\d]", ""));
                    text = limitInput.getText();
                    int substring = text.length() > 2 ? 2 : text.length();
                    limitInput.setText(text.substring(0, substring));
                }
            }
        });
        firstFilter = new HBox(5, limit, limitInput);

        loadPane = new VBox(10);
        commitQuery = new Button("GET RESULTS");
        commitQuery.setStyle("-fx-background-color: green; -fx-font-weight: bold; -fx-text-fill: white");
        commitQuery.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                loadTopFavouriteGenres(Integer.parseInt(limitInput.getText()));
            }
        });

        HBox inputContainer = new HBox(30, commitQuery, firstFilter);
        statisticPane.getChildren().addAll(inputContainer, loadPane);

        loadTopFavouriteGenres(Integer.parseInt(limitInput.getText()));
    }

    private void displayTopArtistForAgeRange() {
        loadPane = new VBox(10);
        statisticPane.getChildren().addAll(loadPane);
        loadTopArtistForAgeRange();
    }

    //--------------------------------------------------------------------------------------------------------

    private void loadTopArtists(int hitThreshold, int limit) {
        loadPane.getChildren().clear();
        List<Pair<String, Integer>> topArtists = connector.getTopArtists(hitThreshold, limit);
        if(topArtists.size() == 0)
            displayEmpty(loadPane);
        else {
            int order = 1;
            for(Pair<String, Integer> artistStatistic: topArtists) {
                loadPane.getChildren().add(createTopArtistView(order++, artistStatistic));
            }
        }
    }

    private void loadTopAlbumForDecade() {
        loadPane.getChildren().clear();
        List<Pair<Integer, Pair<Album, Double>>> albumForDecade = connector.getTopAlbumForDecade();
        if(albumForDecade.size() == 0)
            displayEmpty(loadPane);
        else {
            for (Pair<Integer, Pair<Album, Double>> albumInfo: albumForDecade) {
                loadPane.getChildren().add(createTopAlbumView(
                        albumInfo.getKey(),
                        albumInfo.getValue().getKey(),
                        albumInfo.getValue().getValue()
                ));
            }
        }
    }

    private void loadTopFavouriteGenres(int limit) {
        loadPane.getChildren().clear();
        List<Pair<String, Integer>> favouriteGenres = connector.getTopFavouriteGenres(limit);
        if(favouriteGenres.size() == 0)
            displayEmpty(loadPane);
        else {
            int order = 1;
            for(Pair<String, Integer> genre: favouriteGenres) {
                loadPane.getChildren().add(createTopGenresView(order++, genre));
            }
        }
    }

    private void loadTopArtistForAgeRange() {
        loadPane.getChildren().clear();
        List<Pair<Integer, Pair<String, String>>> artistsForAgeRange = connector.getFavouriteArtistPerAgeRange();
        if(artistsForAgeRange.size() == 0)
            displayEmpty(loadPane);
        else {
            for (Pair<Integer, Pair<String, String>> artistInfo: artistsForAgeRange) {
                loadPane.getChildren().add(createTopArtistForAgeRangeView(
                        artistInfo.getKey(),
                        artistInfo.getValue().getKey(),
                        artistInfo.getValue().getValue()
                ));
            }
        }
    }

    private Text createTopArtistView(int order, Pair<String, Integer> artistStatistic) {
        Text artistNode = new Text(
                order+ "]  "
                +artistStatistic.getKey()+ "  "
                + "(" +artistStatistic.getValue()+ " HITS)"
        );
        artistNode.setFill(Color.WHITE); artistNode.setStyle("-fx-font-weight: bold; -fx-font-size: 20px");
        return artistNode;
    }

    private HBox createTopAlbumView(int decade, Album album, double avgRating) {
        Image albumImage;
        try {
            if(album.getImage() == null || album.getImage().equals(""))
                throw new Exception();

            albumImage = new Image(
                    album.getImage(),
                    0,
                    previewImageHeight,
                    true,
                    true,
                    true
            );

            if(albumImage.isError()) {
                throw new Exception();
            }

        }catch(Exception ex){
            albumImage = new Image(
                    "file:src/main/resources/it/unipi/dii/inginf/lsmdb/unimusic/frontend/gui/img/empty.jpg",
                    0,
                    previewImageHeight,
                    true,
                    true,
                    true
            );
        }

        ImageView albumImageView = new ImageView(albumImage);

        Text decadeText = new Text(decade+ "s"); decadeText.setFill(Color.WHITE); decadeText.setStyle("-fx-font-weight: bold; -fx-font-size: 24px");
        Text title = new Text(album.getTitle()); title.setFill(Color.WHITE); title.setStyle("-fx-font-weight: bold; -fx-font-size: 20px");
        Text rating = new Text("Average Rating: " +new DecimalFormat("0.00").format(avgRating)+ "%"); rating.setFill(Color.GRAY); title.setStyle("-fx-font-weight: bold; -fx-font-size: 18px");

        VBox albumInfo = new VBox(5, decadeText, title, rating);
        HBox albumGraphic = new HBox(10, albumImageView, albumInfo);
        return albumGraphic;
    }

    private Text createTopGenresView(int order, Pair<String, Integer> genre) {
        Text genreNode = new Text(
                order+ "]  " +genre.getKey()+ " "
                + "(Contained " +genre.getValue()+ " times in Playlists)"
        );
        genreNode.setFill(Color.WHITE); genreNode.setStyle("-fx-font-weight: bold; -fx-font-size: 20px");
        return genreNode;
    }

    private HBox createTopArtistForAgeRangeView(int decade, String artist, String popularity) {
        Text artistNode = new Text(decade + " - " +(decade + 9)+ "]  " + artist + "  ");
        artistNode.setFill(Color.WHITE); artistNode.setStyle("-fx-font-weight: bold; -fx-font-size: 20px");

        String[]presence = popularity.split(":");
        Text popularityNode = new Text("(Present " + presence[0] + " times on total " + presence[1] + ")");
        popularityNode.setFill(Color.GRAY); popularityNode.setStyle("-fx-font-weight: bold; -fx-font-size: 18px");
        return new HBox(0, artistNode, popularityNode);
    }

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
        TOP_FAVOURITE_GENRES,
        TOP_ARTIST_FOR_AGE_RANGE
    }
}
