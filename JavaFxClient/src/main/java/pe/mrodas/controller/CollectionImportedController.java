package pe.mrodas.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.Setter;
import org.controlsfx.control.GridView;
import pe.mrodas.MainApp;
import pe.mrodas.entity.Tag;
import pe.mrodas.helper.TagBar;
import pe.mrodas.model.FileModel;
import pe.mrodas.model.RestClient;
import pe.mrodas.model.TagModel;
import pe.mrodas.worker.TaskGetFiles;

import java.io.File;
import java.io.FileFilter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CollectionImportedController {

    @FXML
    public SplitPane splitPane;
    @FXML
    public GridView<File> gridFiles;
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
    public VBox topPane;
    @FXML
    private ProgressController progressController;

    @Getter
    private final SimpleObjectProperty<CollectionController.Config> configProperty = new SimpleObjectProperty<>();
    private final String path = "cache";
    private final ServiceGetFileFilter serviceGetFileFilter = new ServiceGetFileFilter();
    private final ServiceGetLocalFiles serviceGetLocalFiles = new ServiceGetLocalFiles();
    private CollectionController parent;
    private TagBar<Tag> tagBar;
    private File selectedFile;
    private Tag selectedTag;

    public void initialize() {
        configProperty.addListener((o, old, config) -> {
            parent = config.getParent();
            this.setServiceGetFileFilter();
            this.setServiceGetLocalFiles();
            config.buildTagButtons(tagButtons, tag -> {
                tagContainer.setDisable(tag == null);
                if (tag != null && !tag.equals(selectedTag)) {
                    selectedTag = tag;
                    serviceGetFileFilter.setTagsId(Collections.singletonList(tag));
                    //serviceGetFileFilter.restart();
                }
            });
            tagBar = new TagBar<>(Tag::getName, hint -> {
                Integer idRoot = config.getRoot().getIdRoot();
                List<Tag> tags = RestClient.execute(TagModel.class, tagModel -> tagModel.getTagSuggestions(idRoot, hint));
                if (selectedTag != null) {/*
                    return tags.stream()
                            .filter(tag -> !tag.getIdTag().equals(selectedTag.getIdTag()))
                            .collect(Collectors.toList());*/
                }
                return tags;
            });
            tagContainer.getChildren().add(1, tagBar);
            tagBar.setOnSuggestionIsSelected(() -> {
                serviceGetFileFilter.setTagsId(tagBar.getTags());
                //serviceGetFileFilter.restart();
            });
        });
        gridFiles.setCellFactory(param -> new CollectionController.GridCellImage(file -> {
            if (!file.equals(selectedFile)) {
                if (CollectionController.setImageView(imageView, file, gridFiles.getHeight(), splitPane)) {
                    toolbar.setDisable(false);
                    selectedFile = file;
                }
            }
        }, null));
    }

    private void setServiceGetFileFilter() {
        serviceGetFileFilter.setOnSucceeded(event -> {
            List<String> fileList = serviceGetFileFilter.getValue();
            serviceGetLocalFiles.setFilter(file -> {
                String fileName = file.getName();
                return fileList.contains(fileName);
            });
            serviceGetLocalFiles.restart();
        });
        serviceGetFileFilter.setOnFailed(parent::onServiceFailed);
        topPane.disableProperty().bind(serviceGetFileFilter.runningProperty());
        splitPane.disableProperty().bind(serviceGetFileFilter.runningProperty());
        progressController.bindService(serviceGetFileFilter);
    }

    private void setServiceGetLocalFiles() {
        serviceGetLocalFiles.setOnSucceeded(event -> {
            List<File> files = serviceGetLocalFiles.getValue();
            gridFiles.setItems(FXCollections.observableArrayList(files));
        });
        serviceGetLocalFiles.setOnFailed(parent::onServiceFailed);
        topPane.disableProperty().bind(serviceGetLocalFiles.runningProperty());
        splitPane.disableProperty().bind(serviceGetLocalFiles.runningProperty());
        progressController.bindService(serviceGetLocalFiles);
    }

    class ServiceGetFileFilter extends Service<List<String>> {
        private List<Integer> tagsId;

        void setTagsId(List<Tag> tagList) {
            tagsId = tagList.stream().map(Tag::getIdTag).collect(Collectors.toList());
        }

        @Override
        protected Task<List<String>> createTask() {
            return new Task<List<String>>() {
                @Override
                protected List<String> call() throws Exception {
                    return RestClient.execute(FileModel.class, fileModel -> fileModel.getFiles(tagsId));
                }
            };
        }
    }

    class ServiceGetLocalFiles extends Service<List<File>> {
        @Setter
        private FileFilter filter;

        @Override
        protected Task<List<File>> createTask() {
            String workingDir = MainApp.getSession().getWorkingDir();
            return new TaskGetFiles(workingDir, path, filter);
        }
    }

    public void btnEditOnClick(ActionEvent event) {
    }

    public void btnTrashOnClick(ActionEvent event) {
    }
}
