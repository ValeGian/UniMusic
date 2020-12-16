package it.unipi.dii.inginf.lsmdb.unimusic.frontend.gui;

import it.unipi.dii.inginf.lsmdb.unimusic.frontend.MiddlewareConnector;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Song;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.User;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;


public class searchBarController implements Initializable {
    private static MiddlewareConnector connector = MiddlewareConnector.getInstance();

    private static CheckBoxEnum checkBoxSelected;

    @FXML private Button searchButton;
    @FXML private Button closeSearchButton;
    @FXML private TextField searchInput;

    @FXML private CheckBox titleCheck;
    @FXML private CheckBox artistCheck;
    @FXML private CheckBox albumCheck;
    @FXML private CheckBox userCheck;

    @FXML private AnchorPane anchorSearch;

    @FXML private VBox resultBox;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeSearch();
        initializeCheckBox();
    }


    /**
     * initialize actions associated to the search and the close button.
     */
    private void initializeSearch() {

        searchInput.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            handleSearchInput();
        });

        searchButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                handleSearchInput();
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

    public void handleSearchInput(){
        String partialInput = searchInput.getText();
        if(partialInput.equals("")){
            displayNoInputInserted();
            return;
        }
        String attributeField = checkBoxSelected.toString();
        if(attributeField == null)
            return;
        try {
            if (!attributeField.equals("Username")) {
                List<Song> songsFiltered = connector.filterSong(partialInput, attributeField);
                displaySong(songsFiltered);
            } else{
                List<User> userFiltered = connector.getUsersByPartialInput(partialInput);
                displayUser(userFiltered);
            }
        } catch (ActionNotCompletedException ex) {
            return;
        }
    }


    /**
     * Initialize all the actions associated to the checkbox.
     */
    private void initializeCheckBox() {

        if(checkBoxSelected == null)
            checkBoxSelected = CheckBoxEnum.Title;

        switch (checkBoxSelected) {
            case Title:
                titleCheck.setSelected(true);break;
            case Album:
                albumCheck.setSelected(true);
                break;
            case Username:
                userCheck.setSelected(true);
                break;
            default:
                artistCheck.setSelected(true);

        }

        titleCheck.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                handleChechBox(CheckBoxEnum.Title, actionEvent);
            }
        });

        artistCheck.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                handleChechBox(CheckBoxEnum.Artist, actionEvent);
            }
        });

        albumCheck.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                handleChechBox(CheckBoxEnum.Album, actionEvent);
            }
        });

        userCheck.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                handleChechBox(CheckBoxEnum.Username, actionEvent);
            }
        });

    }

    private void handleChechBox(CheckBoxEnum checkBoxSel, ActionEvent actionEvent) {
        checkBoxSelected = checkBoxSel;
        titleCheck.setSelected(false);
        artistCheck.setSelected(false);
        albumCheck.setSelected(false);
        userCheck.setSelected(false);

        CheckBox checkBox = (CheckBox) actionEvent.getSource();
        checkBox.setSelected(true);
    }


    /**
     * Display the list of songs.
     * @param songsFiltered Is the list of songs that will be shown.
     */
    private void displaySong(List<Song> songsFiltered) {

        resultBox.getChildren().clear();

        if(songsFiltered.size() != 0) {
            for (Song song : songsFiltered)
                resultBox.getChildren().addAll(createSongPreview(song), new Separator());
            if(songsFiltered.size() < 4)
                displayNoMoreResult(songsFiltered.size());
        }else
            displayEmpty();

        anchorSearch.setVisible(true);
    }


    /**
     * Display the list of users.
     * @param userFiltered Is the list of users that will be shown.
     */
    private void displayUser(List<User> userFiltered) {

        resultBox.getChildren().clear();

        if(userFiltered.size() != 0) {
            for (User user : userFiltered)
                resultBox.getChildren().addAll(createUserPreview(user), new Separator());
            if(userFiltered.size() < 4)
                displayNoMoreResult(userFiltered.size());
        }else
            displayEmpty();

        anchorSearch.setVisible(true);
    }


    /**
     * Display the message of empty result when no user/song is found according to the user's input.
     */
    public void displayEmpty(){
        TextField empty = new TextField("No result found"); empty.setStyle("-fx-background-color: black;");empty.setMinWidth(500);empty.setPrefHeight(500); empty.setAlignment(Pos.CENTER);
        resultBox.getChildren().add(empty);

    }


    /**
     * Display a generic text in the result box.
     * @param text The text to be displayed.
     */
    public void displayText(String text){
        resultBox.getChildren().clear();
        TextField fill = new TextField(text); fill.setStyle("-fx-background-color: black;"); fill.setMinHeight(500);fill.setMinWidth(500); fill.setAlignment(Pos.CENTER);
        resultBox.getChildren().add(fill);
        anchorSearch.setVisible(true);
    }


    public void displayNoInputInserted(){
        displayText("Insert something...");
    }


    public void displayNoMoreResult(int size){
        TextField fill = new TextField("No more result found"); fill.setStyle("-fx-background-color: black;"); fill.setMinHeight((4 - size) * 150);fill.setMinWidth(500); fill.setAlignment(Pos.CENTER);
        resultBox.getChildren().add(fill);
    }


    /**
     * @param song The song to be shown.
     * @return The section that contains all the preview's information as a button.
     */
    private Button createSongPreview(Song song) {

        Button songPreview = new Button(); songPreview.setStyle("-fx-background-color: black"); songPreview.setMinWidth(500);
        songPreview.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    songPageController.getSongPage(song);
                } catch (IOException e) {}
            }
        });

        Image songImage;
        try {
            songImage = new Image(song.getAlbum().getImage(),0,100,true,true,true);

            if(songImage.isError()) {
                throw new Exception();
            }

        }catch(Exception ex){
            String filePath = "file:src/main/resources/it/unipi/dii/inginf/lsmdb/unimusic/frontend/gui/img/empty.jpg";
            songImage = new Image(filePath,100,140,true,true,true);
        }

        ImageView songImageView = new ImageView(songImage);

        Text title = new Text(song.getTitle()); title.setWrappingWidth(300); title.setFill(Color.WHITE);
        Text artist = new Text(song.getArtist()); artist.setWrappingWidth(300);artist.setFill(Color.GRAY);

        VBox information = new VBox(title, artist);information.setPadding(new Insets(20, 0, 20, 20));

        HBox songGraphic = new HBox(2, songImageView, information);

        songPreview.setGraphic(songGraphic);
        return songPreview;
    }


    /**
     * @param user The user to be shown.
     * @return The section that contains all the preview's information as a button.
     */
    public Button createUserPreview(User user) {

        Button userPreview = new Button(); userPreview.setStyle("-fx-background-color: black"); userPreview.setMinWidth(500);

        userPreview.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                try {
                    userPageController.getUserPage(user);
                    throw new IOException();
                } catch (IOException e) {

                }
            }
        });

        String filePath = "file:src/main/resources/it/unipi/dii/inginf/lsmdb/unimusic/frontend/gui/img/user.png";
        Image userImage = new Image(filePath,140,0,true,true,true);
        ImageView songImageView = new ImageView(userImage);

        Text username = new Text(user.getUsername()); username.setFill(Color.WHITE);

        HBox userGraphic = new HBox(20, songImageView, username);
        userPreview.setGraphic(userGraphic);

        return userPreview;
    }


    private enum CheckBoxEnum{
        Title,
        Artist,
        Album,
        Username
    }


}
