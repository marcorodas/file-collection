package pe.mrodas.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class MenuController extends BaseController {

    public MenuController() {
        super("/fxml/Menu.fxml", true);
        super.setTitle("Menu");
    }

    @Override
    public void initialize() {

    }

    @FXML
    public void btnImportOnClick(ActionEvent actionEvent) {

    }

    @FXML
    public void btnGalleryOnClick(ActionEvent actionEvent) {
        super.handle(() -> new GalleryController().prepareStage(this).show());
    }

}
