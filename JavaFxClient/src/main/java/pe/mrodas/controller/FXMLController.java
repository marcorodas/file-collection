package pe.mrodas.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class FXMLController extends BaseController {

    @FXML
    private Label label;

    public FXMLController() {
        super("/fxml/Scene.fxml", true);
    }

    @FXML
    private void handleButtonAction(ActionEvent e) {
        System.out.println("You clicked me!");
        label.setText("Hello World!");
    }

    @Override
    public void initialize() {
        //TODO
    }
}
