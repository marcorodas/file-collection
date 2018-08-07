package pe.mrodas.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import lombok.Getter;
import org.controlsfx.control.GridView;
import pe.mrodas.MainApp;
import pe.mrodas.entity.Tag;
import pe.mrodas.model.RestClient;
import pe.mrodas.model.TagModel;
import pe.mrodas.worker.TaskGetFiles;
import pe.mrodas.worker.TaskMoveFiles;

import java.io.File;
import java.io.FileFilter;
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

    @Getter
    private final SimpleObjectProperty<CollectionController.Config> configProperty = new SimpleObjectProperty<>();
    private CollectionController parent;
    private final String path = "stage";
    private Service<List<Tag>> serviceGetCategories;
    private Service<List<File>> serviceGetFiles;
    private File selectedFile;
    private Tag selectedTag;
    private Consumer<File> setImageView;

    public void initialize() {
        configProperty.addListener((o, old, config) -> {
            parent = config.getParent();
            config.buildTagButtons(tagButtons, tag -> {
                btnUpload.setDisable(tag == null || gridUpload.getItems().isEmpty());
                selectedTag = tag;
            });
            this.setServiceGetFiles(config.getExtensions());
            this.setServiceGetCategories(config);
            serviceGetFiles.start();
            serviceGetCategories.start();
        });
        setImageView = file -> {
            if (!file.equals(selectedFile)) {
                if (CollectionController.setImageView(imageView, file, gridFiles.getHeight(), splitPane)) {
                    toolbar.setDisable(false);
                    selectedFile = file;
                }
            }
        };
        gridUpload.setItems(FXCollections.observableArrayList());
        gridUpload.itemsProperty().get().addListener((ListChangeListener<? super File>) c -> {
            int size = c.getList().size();
            lblNumFilesUpload.setText(String.valueOf(size));
        });
        gridUpload.setCellFactory(param -> new CollectionController.GridCellImage(setImageView, file -> {
            gridFiles.getItems().add(0, file);
            gridUpload.getItems().remove(file);
            if (gridUpload.getItems().isEmpty()) {
                btnUpload.setDisable(true);
            }
        }));
    }

    private void setServiceGetFiles(List<String> extensions) {
        FileFilter filter = (extensions == null) ? null : file -> extensions.stream()
                .map(s -> s.replace("*", ""))
                .anyMatch(s -> file.getName().endsWith(s));
        serviceGetFiles = new Service<List<File>>() {
            @Override
            protected Task<List<File>> createTask() {
                return new TaskGetFiles(MainApp.getSession().getWorkingDir(), path, filter);
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
            gridFiles.setCellFactory(param -> new CollectionController.GridCellImage(setImageView, file -> {
                gridUpload.getItems().add(file);
                gridFiles.getItems().remove(file);
                if (selectedTag != null) {
                    btnUpload.setDisable(false);
                }
            }));
        });
        serviceGetFiles.setOnFailed(parent::onServiceFailed);
        splitPane.disableProperty().bind(serviceGetFiles.runningProperty());
        progressController.bindService(serviceGetFiles);
    }

    private void setServiceGetCategories(CollectionController.Config config) {
        serviceGetCategories = new Service<List<Tag>>() {
            @Override
            protected Task<List<Tag>> createTask() {
                return new TaskGetCategories(config.getRoot().getIdRoot());
            }
        };
        serviceGetCategories.setOnSucceeded(event -> {
            List<Tag> tags = serviceGetCategories.getValue();
            config.getTagListProperty().set(tags);
        });
        serviceGetCategories.setOnFailed(parent::onServiceFailed);
        splitPane.disableProperty().bind(serviceGetCategories.runningProperty());
        progressController.bindService(serviceGetCategories);
    }


    @FXML
    public void btnGetFilesOnClick(ActionEvent actionEvent) {
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Images", configProperty.get().getExtensions());
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(extFilter);
        chooser.setTitle("Import Images");
        List<File> files = chooser.showOpenMultipleDialog(parent.getStage(actionEvent));
        if (files != null && !files.isEmpty()) {
            Service<Void> service = new Service<Void>() {
                @Override
                protected Task<Void> createTask() {
                    return new TaskMoveFiles(files, MainApp.getSession().getWorkingDir(), path);
                }
            };
            service.setOnSucceeded(event -> serviceGetFiles.restart());
            service.setOnFailed(parent::onServiceFailed);
            splitPane.disableProperty().bind(service.runningProperty());
            progressController.bindService(service);
            service.restart();
        }
    }

    public static class TaskGetCategories extends Task<List<Tag>> {
        private final int idRoot;

        public TaskGetCategories(int idRoot) {
            this.idRoot = idRoot;
        }

        @Override
        protected List<Tag> call() throws Exception {
            super.updateMessage("Getting categories...");
            return RestClient.execute(TagModel.class, model -> model.getCategories(idRoot));
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
