package pe.mrodas.controller;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.controlsfx.control.GridView;

public class CollectionStageController {

    @FXML
    public Label lblDragStatus;
    @FXML
    public ListView listCategories;
    @FXML
    public SplitPane splitPane;
    @FXML
    public GridView gridFiles;
    @FXML
    public StackPane imageContainer;
    @FXML
    public ImageView imageView;
    @FXML
    private ProgressController progressController;

    public void initialize(){
        listCategories.addEventFilter(MouseEvent.MOUSE_PRESSED, Event::consume);
    }

    @FXML
    public void btnGetFilesOnClick(ActionEvent actionEvent) {
    }

    @FXML
    public void btnEditOnClick(ActionEvent actionEvent) {
    }

    @FXML
    public void btnTrashOnClick(ActionEvent actionEvent) {
    }
}
