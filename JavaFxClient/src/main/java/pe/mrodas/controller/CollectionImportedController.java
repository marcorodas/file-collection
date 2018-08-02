package pe.mrodas.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.controlsfx.control.GridView;
import pe.mrodas.helper.TagBar;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
        this.setAutoComplete(0);
        tagBar.getInputTextField().setOnAction(null);
        tagContainer.getChildren().add(1, tagBar);
    }

    private void setAutoComplete(int idRoot) {
        tagBar.setAutoCompletion(hint -> {
            try {
                //List<String> list = RestClient.execute(TagModel.class, tagModel -> tagModel.getTagSuggestions(idRoot, hint));
                List<String> list = Arrays.asList("cono", "chupador", "ericito");
                return list.stream()
                        .filter(s -> !tagBar.getTags().contains(s))
                        .collect(Collectors.toList());
            } catch (Exception e) {
                return null;
            }
        }, () -> {
            //Refresh gridFiles
        });
    }

    public void btnEditOnClick(ActionEvent event) {
    }

    public void btnTrashOnClick(ActionEvent event) {
    }
}
