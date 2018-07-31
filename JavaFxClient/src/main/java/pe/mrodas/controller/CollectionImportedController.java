package pe.mrodas.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.controlsfx.control.GridView;
import pe.mrodas.helper.TagBar;

public class CollectionImportedController {

    @FXML
    public SplitPane splitPane;
    @FXML
    public GridView gridFiles;
    @FXML
    public BorderPane imageContainer;
    @FXML
    public ImageView imageView;
    @FXML
    public ToolBar toolbar;
    @FXML
    public HBox tagButtons;
    @FXML
    public HBox tagContainer;
    @FXML
    private ProgressController progressController;

    private TagBar tagBar = new TagBar();

    public void initialize() {
        System.out.println("Collection Imported!");
        tagContainer.getChildren().add(1, tagBar);
    }

    public void btnEditOnClick(ActionEvent event) {
    }

    public void btnTrashOnClick(ActionEvent event) {
    }
}
