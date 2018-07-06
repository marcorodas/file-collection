package pe.mrodas.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import org.controlsfx.control.GridView;
import pe.mrodas.entity.Root;
import pe.mrodas.entity.Tag;
import pe.mrodas.worker.TaskGetCategories;

import java.security.InvalidParameterException;
import java.util.List;


public class CollectionStageController {

    @FXML
    public Label lblDragStatus;
    @FXML
    public ListView<Tag> listCategories;
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

    private final SimpleObjectProperty<Root> rootProperty = new SimpleObjectProperty<>();
    private Service<List<Tag>> serviceGetCategories;

    public void setRoot(Root root) {
        this.checkIdRoot(root);
        rootProperty.set(root);
    }

    private void checkIdRoot(Root root) {
        if (root == null) {
            throw new InvalidParameterException("root can't be null");
        }
        if ((root.getIdRoot() == null ? 0 : root.getIdRoot()) <= 0) {
            throw new InvalidParameterException("idRoot must be greater than zero");
        }
    }

    public void initialize() {
        listCategories.addEventFilter(MouseEvent.MOUSE_PRESSED, Event::consume);
        rootProperty.addListener((o, old, root) -> this.onRootReady(root));
    }

    private void onRootReady(Root root) {
        serviceGetCategories = new Service<List<Tag>>() {
            @Override
            protected Task<List<Tag>> createTask() {
                return new TaskGetCategories(root.getIdRoot());
            }
        };
        serviceGetCategories.setOnSucceeded(event -> {
            List<Tag> categories = serviceGetCategories.getValue();
            listCategories.setItems(FXCollections.observableArrayList(categories));
            listCategories.setCellFactory(list -> new ListCellTag());
        });
        serviceGetCategories.setOnFailed(new CollectionController()::onServiceFailed);
        splitPane.disableProperty().bind(serviceGetCategories.runningProperty());
        progressController.setService(serviceGetCategories);
        serviceGetCategories.start();
    }

    static class ListCellTag extends ListCell<Tag> {
        @Override
        protected void updateItem(Tag item, boolean empty) {
            super.updateItem(item, empty);
            if (!empty && item != null) {
                Text text = new Text(item.getName());
                this.setGraphic(text);
            }
        }
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
