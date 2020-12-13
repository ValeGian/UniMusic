package it.unipi.dii.inginf.lsmdb.unimusic.frontend.gui;

import it.unipi.dii.inginf.lsmdb.unimusic.frontend.MiddlewareConnector;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.Song;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.entities.User;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class userPageController implements Initializable {
    private static MiddlewareConnector connector = MiddlewareConnector.getInstance();
    private static User userToDisplay;

    @FXML private AnchorPane parentPane;

    @FXML private TextField followsYou;
    @FXML private TextField userCompleteName;

    @FXML private Button followButton;

    public static void getUserPage(User user) throws IOException {
        try {
            userToDisplay = connector.getUser(user);
        } catch (ActionNotCompletedException e) {
            throw new IOException(e.getMessage());
        }
        App.setRoot("userPage");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        displayUserInformation();
        initializeButton();
    }

    private void displayUserInformation() {
        if(userToDisplay.getUsername().equals(connector.getLoggedUser().getUsername()))
            parentPane.getChildren().remove(followsYou);
        else {
            if(connector.isFollowedBy(userToDisplay)) {
                followsYou.setVisible(true);
            }
        }

        userCompleteName.setText(userToDisplay.getFirstName() + " " + userToDisplay.getLastName());
    }

    private void initializeButton() {
        if(userToDisplay.getUsername().equals(connector.getLoggedUser().getUsername()))
            parentPane.getChildren().remove(followButton);
        else {
            if(connector.follows(userToDisplay)) {
                followButton.setText("Unfollow");
                followButton.setStyle("-fx-background-color: red; -fx-font-weight: bold");
                followButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        try {
                            connector.unfollow(userToDisplay);
                            followButton.setText("Follow");
                            followButton.setStyle("-fx-background-color: green; -fx-font-weight: bold");
                        } catch (ActionNotCompletedException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                followButton.setText("Follow");
                followButton.setStyle("-fx-background-color: green; -fx-font-weight: bold");
                followButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        try {
                            connector.follow(userToDisplay);
                            followButton.setText("Unfollow");
                            followButton.setStyle("-fx-background-color: red; -fx-font-weight: bold");
                        } catch (ActionNotCompletedException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }

    }
}
