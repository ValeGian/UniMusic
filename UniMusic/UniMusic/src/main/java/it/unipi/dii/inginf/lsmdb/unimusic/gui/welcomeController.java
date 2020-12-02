package it.unipi.dii.inginf.lsmdb.unimusic.gui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class welcomeController implements Initializable {
    @FXML private TextField ageActionTarget;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ageActionTarget.setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                String text = ageActionTarget.getText();
                if(!text.matches("\\d")) {
                    ageActionTarget.setText(text.replaceAll("[^\\d]", ""));
                }
            }
        });
    }
}
