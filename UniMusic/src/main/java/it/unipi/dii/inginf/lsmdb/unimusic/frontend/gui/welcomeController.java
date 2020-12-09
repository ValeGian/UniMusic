package it.unipi.dii.inginf.lsmdb.unimusic.frontend.gui;

import it.unipi.dii.inginf.lsmdb.unimusic.frontend.MiddlewareConnector;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class welcomeController implements Initializable {
    private MiddlewareConnector connector;

    @FXML private TextField logUsername;
    @FXML private TextField logPassword;
    @FXML private Button loginButton;

    @FXML private TextField regFirstName;
    @FXML private TextField regLastName;
    @FXML private TextField regAge;
    @FXML private TextField regUsername;
    @FXML private TextField regPassword;
    @FXML private Button registerButton;

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
                    System.out.println("YEAH!");
                } else {
                    System.out.println("NOPE!");
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

                if(username.equals("")
                || password.equals("")
                || firstName.equals("")
                || lastName.equals("")
                ||age.equals("")) {
                    System.out.println("Vuoti!");
                } else if(connector.registerUser(username, password, firstName, lastName, Integer.parseInt(age))) {
                    regUsername.setText("");
                    regPassword.setText("");
                    regFirstName.setText("");
                    regLastName.setText("");
                    regAge.setText("");
                } else {
                    System.out.println("Not Connected");
                }
            }
        });
    }
}
