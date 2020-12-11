package it.unipi.dii.inginf.lsmdb.unimusic.frontend.gui;

import it.unipi.dii.inginf.lsmdb.unimusic.frontend.MiddlewareConnector;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class welcomeController implements Initializable {
    private MiddlewareConnector connector;

    @FXML private TextField logUsername;
    @FXML private TextField logPassword;
    @FXML private Button loginButton;
    @FXML private TextField loginMessage;

    @FXML private TextField regFirstName;
    @FXML private TextField regLastName;
    @FXML private TextField regAge;
    @FXML private TextField regUsername;
    @FXML private TextField regPassword;
    @FXML private Button registerButton;
    @FXML private TextField registerMessage;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        connector = MiddlewareConnector.getInstance();

        regAge.setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                String text = regAge.getText();
                if(!text.matches("\\d")) {
                    regAge.setText(text.replaceAll("[^\\d]", ""));
                }
            }
        });

        loginButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                String username = logUsername.getText();
                String password = logPassword.getText();

                if(connector.loginUser(username, password)) {
                    try {
                        App.setRoot("homepage");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    clear();
                    loginMessage.setStyle("-fx-text-fill: #ff0000; -fx-background-color: transparent");
                    loginMessage.setText("Not been able to login, retry!");
                }
            }
        });

        registerButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                String username = regUsername.getText();
                String password = regPassword.getText();
                String firstName = regFirstName.getText();
                String lastName = regLastName.getText();
                String age = regAge.getText();

                clear();

                if(username.equals("")
                || password.equals("")
                || firstName.equals("")
                || lastName.equals("")
                ||age.equals("")) {
                    registerMessage.setStyle("-fx-text-fill: red; -fx-background-color: transparent");
                    registerMessage.setText("You have to fill all the fields!");
                } else {
                    try {
                        connector.registerUser(username, password, firstName, lastName, Integer.parseInt(age));
                        registerMessage.setStyle("-fx-text-fill: green; -fx-background-color: transparent");
                        registerMessage.setText("You have succesfully registered!");

                    } catch (ActionNotCompletedException e) {
                        registerMessage.setStyle("-fx-text-fill: red; -fx-background-color: transparent");
                        if (e.getCode() == 11000) {
                            registerMessage.setText("The username already exists!");
                        } else {
                            registerMessage.setText("It's been impossible to register, retry!");
                        }
                    }
                }
            }
        });
    }

    private void clear() {
        regUsername.setText("");
        regPassword.setText("");
        regFirstName.setText("");
        regLastName.setText("");
        regAge.setText("");registerMessage.setText(""); registerMessage.setStyle("-fx-background-color: transparent");

        logUsername.setText("");
        logPassword.setText("");
        loginMessage.setText(""); loginMessage.setStyle("-fx-background-color: transparent");

    }
}
