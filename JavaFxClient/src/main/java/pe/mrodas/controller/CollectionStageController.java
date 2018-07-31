package pe.mrodas.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;
import org.controlsfx.control.SegmentedButton;
import pe.mrodas.MainApp;
import pe.mrodas.entity.Root;
import pe.mrodas.entity.Tag;
import pe.mrodas.helper.GuiHelper;
import pe.mrodas.worker.TaskGetCategories;
import pe.mrodas.worker.TaskGetFiles;
import pe.mrodas.worker.TaskMoveFiles;

import java.io.File;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.function.Consumer;


public class CollectionStageController {

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
    public HBox tagButtons;
    @FXML
    public Button btnUpload;
    @FXML
    public GridView<File> gridUpload;
    @FXML
    public Label lblNumFilesUpload;
    @FXML
    public Label lblTotal;

    @FXML
    private ProgressController progressController;

    private final SimpleObjectProperty<Root> rootProperty = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<List<String>> extensionsProperty = new SimpleObjectProperty<>();
    private final BaseController baseController = new CollectionController();
    private final String path = "stage";
    private Service<List<Tag>> serviceGetCategories;
    private Service<List<File>> serviceGetFiles;
    private File selectedFile;
    private Tag selectedTag;

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
        rootProperty.addListener((o, old, root) -> this.onRootReady(root));
        extensionsProperty.addListener((o, old, extensions) -> this.onExtensionsReady(extensions));
        gridUpload.setItems(FXCollections.observableArrayList());
        gridUpload.itemsProperty().get().addListener((ListChangeListener<? super File>) c -> {
            int size = c.getList().size();
            lblNumFilesUpload.setText(String.valueOf(size));
        });
        gridUpload.setCellFactory(param -> new GridCellImage(this::setImageView, file -> {
            gridFiles.getItems().add(0, file);
            gridUpload.getItems().remove(file);
            if (gridUpload.getItems().isEmpty()) {
                btnUpload.setDisable(true);
            }
        }));
        System.out.println("Collection Stage!");
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
            String len = String.valueOf(files.size());
            lblTotal.setText(len);
            gridFiles.setItems(FXCollections.observableArrayList(files));
            gridFiles.itemsProperty().get().addListener((ListChangeListener<? super File>) c -> {
                int size = c.getList().size();
                lblTotal.setText(String.valueOf(size));
            });
            gridFiles.setCellFactory(param -> new GridCellImage(this::setImageView, file -> {
                gridUpload.getItems().add(file);
                gridFiles.getItems().remove(file);
                if (selectedTag != null) {
                    btnUpload.setDisable(false);
                }
            }));
        });
        serviceGetFiles.setOnFailed(baseController::onServiceFailed);
        splitPane.disableProperty().bind(serviceGetFiles.runningProperty());
        progressController.setService(serviceGetFiles);
        serviceGetFiles.start();
    }

    private void setImageView(File file) {
        if (!file.equals(selectedFile)) {
            Image image = GuiHelper.getImageFromFile(file, null);
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
    }

    private DoubleBinding getDividerWidth(Image image, double maxHeight) {
        DoubleBinding splitWidth = splitPane.widthProperty().subtract(10d);
        DoubleProperty splitPercentage = splitPane.getDividers().get(0).positionProperty();
        DoubleBinding dividerWidth = Bindings.subtract(1d, splitPercentage).multiply(splitWidth);
        return Bindings.min(maxHeight * image.getWidth() / image.getHeight(), dividerWidth);
    }

    class GridCellImage extends GridCell<File> {
        private final Consumer<File> onMouseSingleClick, onMouseDoubleClick;

        GridCellImage(Consumer<File> onMouseSingleClick, Consumer<File> onMouseDoubleClick) {
            this.onMouseSingleClick = onMouseSingleClick;
            this.onMouseDoubleClick = onMouseDoubleClick;
            this.setOnMouseClicked(this::onMouseClick);
        }

        void onMouseClick(MouseEvent e) {
            int clickCount = e.getClickCount();
            if (clickCount > 0) {
                File item = this.getItem();
                if (clickCount == 1) {
                    onMouseSingleClick.accept(item);
                } else if (onMouseDoubleClick != null) {
                    onMouseDoubleClick.accept(item);
                }
            }
        }

        @Override
        protected void updateItem(File file, boolean empty) {
            super.updateItem(file, empty);
            if (!empty && file != null) {
                Image image = GuiHelper.getImageFromFile(file, 100);
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
            List<Tag> tags = serviceGetCategories.getValue();
            ToggleButton[] buttons = tags.stream().map(tag -> {
                ToggleButton toggle = new ToggleButton(tag.getName());
                toggle.setUserData(tag);
                return toggle;
            }).toArray(ToggleButton[]::new);
            SegmentedButton groupBtns = new SegmentedButton(buttons);
            tagButtons.getChildren().setAll(groupBtns);
            groupBtns.getToggleGroup().selectedToggleProperty().addListener(this::onCategorySelectionChange);
        });
        serviceGetCategories.setOnFailed(baseController::onServiceFailed);
        splitPane.disableProperty().bind(serviceGetCategories.runningProperty());
        progressController.setService(serviceGetCategories);
        serviceGetCategories.start();
    }

    private void onCategorySelectionChange(ObservableValue<? extends Toggle> o, Toggle old, Toggle toggle) {
        btnUpload.setDisable(toggle == null || gridUpload.getItems().isEmpty());
        selectedTag = (toggle == null) ? null : (Tag) toggle.getUserData();
    }

    @FXML
    public void btnGetFilesOnClick(ActionEvent actionEvent) {
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Images", extensionsProperty.get());
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(extFilter);
        chooser.setTitle("Import Images");
        List<File> files = chooser.showOpenMultipleDialog(baseController.getStage(actionEvent));
        if (files != null && !files.isEmpty()) {
            Service<Void> service = new Service<Void>() {
                @Override
                protected Task<Void> createTask() {
                    return new TaskMoveFiles(files, MainApp.getSession().getWorkingDir(), path);
                }
            };
            service.setOnSucceeded(event -> serviceGetFiles.restart());
            service.setOnFailed(baseController::onServiceFailed);
            splitPane.disableProperty().bind(service.runningProperty());
            progressController.setService(service);
            service.restart();
        }
    }

    @FXML
    public void btnEditOnClick(ActionEvent e) {
    }

    @FXML
    public void btnTrashOnClick(ActionEvent e) {
    }

    @FXML
    public void btnUploadOnClick(ActionEvent e) {
    }

    @FXML
    public void btnGetFromUrlOnClick(ActionEvent event) {
    }

}
