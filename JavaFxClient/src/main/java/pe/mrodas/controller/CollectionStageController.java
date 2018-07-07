package pe.mrodas.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import lombok.Getter;
import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;
import pe.mrodas.MainApp;
import pe.mrodas.entity.Root;
import pe.mrodas.entity.Tag;
import pe.mrodas.worker.TaskGetCategories;
import pe.mrodas.worker.TaskGetFiles;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.function.BiFunction;


public class CollectionStageController {

    @FXML
    public Label lblDragStatus;
    @FXML
    public ListView<Tag> listCategories;
    @FXML
    public SplitPane splitPane;
    @FXML
    public GridView<File> gridFiles;
    @FXML
    public ImageView imageView;
    @FXML
    public BorderPane imageContainer;
    @FXML
    public ToolBar toolbar;

    @FXML
    private ProgressController progressController;

    private final SimpleObjectProperty<Root> rootProperty = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<List<String>> extensionsProperty = new SimpleObjectProperty<>();
    private final EventHandler<WorkerStateEvent> handler = new CollectionController()::onServiceFailed;
    private final String path = "stage";
    private Service<List<Tag>> serviceGetCategories;
    private Service<List<File>> serviceGetFiles;
    private File selectedFile;

    void setRoot(Root root) {
        if (root == null) {
            throw new InvalidParameterException("root can't be null");
        }
        if ((root.getIdRoot() == null ? 0 : root.getIdRoot()) <= 0) {
            throw new InvalidParameterException("idRoot must be greater than zero");
        }
        rootProperty.set(root);
    }

    void setExtensions(List<String> extensions) {
        extensionsProperty.set(extensions);
    }

    public void initialize() {
        listCategories.addEventFilter(MouseEvent.MOUSE_PRESSED, Event::consume);
        imageView.imageProperty().addListener(this::onImageIsSet);
        rootProperty.addListener((o, old, root) -> this.onRootReady(root));
        extensionsProperty.addListener((o, old, extensions) -> this.onExtensionsReady(extensions));
    }

    private void onImageIsSet(ObservableValue<? extends Image> o, Image old, Image image) {

    }

    private void onExtensionsReady(List<String> extensions) {
        serviceGetFiles = new Service<List<File>>() {
            @Override
            protected Task<List<File>> createTask() {
                return new TaskGetFiles(MainApp.getSession().getWorkingDir(), path, extensions.stream());
            }
        };
        serviceGetFiles.setOnSucceeded(event -> {
            List<File> files = serviceGetFiles.getValue();
            gridFiles.setItems(FXCollections.observableArrayList(files));
            gridFiles.setCellFactory(this::getGridCellFactory);
        });
        serviceGetFiles.setOnFailed(handler);
        splitPane.disableProperty().bind(serviceGetFiles.runningProperty());
        progressController.setService(serviceGetFiles);
        serviceGetFiles.start();
    }

    private GridCell<File> getGridCellFactory(GridView<File> gridView) {
        GridCellImage cell = new GridCellImage();
        cell.setOnMouseClicked(e -> {
            File file = gridFiles.getItems().get(cell.getIndex());
            if (!file.equals(selectedFile)) {
                Image image = cell.getImageCreator().apply(file, null);
                if (image != null) {
                    imageView.setImage(image);
                    imageView.fitWidthProperty().unbind();
                    double maxHeight = gridFiles.getHeight() - 10d;
                    DoubleBinding dividerWidth = this.getDividerWidth(image, maxHeight);
                    imageView.fitWidthProperty().bind(dividerWidth);
                    toolbar.setDisable(false);
                    selectedFile = file;
                }
            }
        });
        return cell;
    }

    private DoubleBinding getDividerWidth(Image image, double maxHeight) {
        DoubleBinding splitWidth = splitPane.widthProperty().subtract(10d);
        DoubleProperty splitPercentage = splitPane.getDividers().get(0).positionProperty();
        DoubleBinding dividerWidth = Bindings.subtract(1d, splitPercentage).multiply(splitWidth);
        return Bindings.min(maxHeight * image.getWidth() / image.getHeight(), dividerWidth);
    }

    class GridCellImage extends GridCell<File> {
        @Getter
        private final BiFunction<File, Integer, Image> imageCreator = (file, size) -> {
            try (FileInputStream stream = new FileInputStream(file)) {
                return size == null ? new Image(stream)
                        : new Image(stream, size, size, true, true);
            } catch (IOException e) {
                return null;
            }
        };

        @Override
        protected void updateItem(File file, boolean empty) {
            super.updateItem(file, empty);
            if (!empty && file != null) {
                Image image = imageCreator.apply(file, 100);
                if (image != null) {
                    this.setGraphic(new ImageView(image));
                }
            }
        }
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
        serviceGetCategories.setOnFailed(handler);
        splitPane.disableProperty().bind(serviceGetCategories.runningProperty());
        progressController.setService(serviceGetCategories);
        serviceGetCategories.start();
    }

    class ListCellTag extends ListCell<Tag> {
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
