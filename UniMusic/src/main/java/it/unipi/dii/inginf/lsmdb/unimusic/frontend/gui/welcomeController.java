package it.unipi.dii.inginf.lsmdb.unimusic.frontend.gui;

import it.unipi.dii.inginf.lsmdb.unimusic.frontend.MiddlewareConnector;
import it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception.ActionNotCompletedException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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
    @FXML private ComboBox countryBox;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        connector = MiddlewareConnector.getInstance();

        String[] countries = {"Afghanistan","Albania","Algeria","Andorra","Angola","Anguilla","Antigua-Barbuda","Argentina","Armenia","Aruba","Australia","Austria","Azerbaijan","Bahamas","Bahrain","Bangladesh","Barbados","Belarus","Belgium","Belize","Benin","Bermuda","Bhutan","Bolivia","Bosnia-Herzegovina","Botswana","Brazil","British Virgin Islands","Brunei","Bulgaria","Burkina Faso","Burundi","Cambodia","Cameroon","Cape Verde","Cayman Islands","Chad","Chile","China","Colombia","Congo","Cook Islands","Costa Rica","Cote D Ivoire","Croatia","Cruise Ship","Cuba","Cyprus","Czech Republic","Denmark","Djibouti","Dominica","Dominican Republic","Ecuador","Egypt","El Salvador","Equatorial Guinea","Estonia","Ethiopia","Falkland Islands","Faroe Islands","Fiji","Finland","France","French Polynesia","French West Indies","Gabon","Gambia","Georgia","Germany","Ghana","Gibraltar","Greece","Greenland","Grenada","Guam","Guatemala","Guernsey","Guinea","Guinea Bissau","Guyana","Haiti","Honduras","Hong Kong","Hungary","Iceland","India","Indonesia","Iran","Iraq","Ireland","Isle of Man","Israel","Italy","Jamaica","Japan","Jersey","Jordan","Kazakhstan","Kenya","Kuwait","Kyrgyz Republic","Laos","Latvia","Lebanon","Lesotho","Liberia","Libya","Liechtenstein","Lithuania","Luxembourg","Macau","Macedonia","Madagascar","Malawi","Malaysia","Maldives","Mali","Malta","Mauritania","Mauritius","Mexico","Moldova","Monaco","Mongolia","Montenegro","Montserrat","Morocco","Mozambique","Namibia","Nepal","Netherlands","Netherlands Antilles","New Caledonia","New Zealand","Nicaragua","Niger","Nigeria","Norway","Oman","Pakistan","Palestine","Panama","Papua New Guinea","Paraguay","Peru","Philippines","Poland","Portugal","Puerto Rico","Qatar","Reunion","Romania","Russia","Rwanda","Saint Pierre-Miquelon","Samoa","San Marino","Satellite","Saudi Arabia","Senegal","Serbia","Seychelles","Sierra Leone","Singapore","Slovakia","Slovenia","South Africa","South Korea","Spain","Sri Lanka","St Kitts-Nevis","St Lucia","St Vincent","St. Lucia","Sudan","Suriname","Swaziland","Sweden","Switzerland","Syria","Taiwan","Tajikistan","Tanzania","Thailand","Timor L'Este","Togo","Tonga","Trinidad-Tobago","Tunisia","Turkey","Turkmenistan","Turks-Caicos","Uganda","Ukraine","United Arab Emirates","United Kingdom","Uruguay","Uzbekistan","Venezuela","Vietnam","Virgin Islands (US)","Yemen","Zambia","Zimbabwe"};
        ObservableList<String> countriesObservable = FXCollections.observableArrayList(countries);

        countryBox.setItems(countriesObservable);

        regAge.setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                String text = regAge.getText();
                if(!text.matches("[0-9]{1,2}?")) {
                    regAge.setText(text.replaceAll("[^\\d]", ""));
                    text = regAge.getText();
                    int substring = text.length() > 2 ? 2 : text.length();
                    regAge.setText(text.substring(0, substring));
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
                String country;
                clear();

                if(username.equals("")
                || password.equals("")
                || firstName.equals("")
                || lastName.equals("")
                || age.equals("")
                || countryBox.getSelectionModel().isEmpty()) {
                    registerMessage.setStyle("-fx-text-fill: red; -fx-background-color: transparent");
                    registerMessage.setText("You have to fill all the fields!");
                } else {
                    try {
                        country = countryBox.getValue().toString();
                        connector.registerUser(username, password, firstName, lastName, Integer.parseInt(age), country);
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
