package pe.mrodas.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

import lombok.Getter;
import org.controlsfx.control.GridView;

public class CollectionTrashController {
    @FXML
    public ToolBar toolbar;
    @FXML
    public ImageView imageView;
    @FXML
    public BorderPane imageContainer;
    @FXML
    public GridView gridFiles;
    @FXML
    public SplitPane splitPane;
    @FXML
    private ProgressController progressController;

    public static final String PATH = "trash";
    @Getter
    private final SimpleObjectProperty<CollectionController.Config> configProperty = new SimpleObjectProperty<>();
    private CollectionController parent;

    public void initialize() {
        configProperty.addListener((o, old, config) -> {
            parent = config.getParent();
        });
    }

    @FXML
    public void btnStageOnClick(ActionEvent event) {
    }

    @FXML
    public void btnDeleteOnClick(ActionEvent event) {
    }
}
