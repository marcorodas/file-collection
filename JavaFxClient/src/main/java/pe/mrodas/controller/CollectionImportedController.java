package pe.mrodas.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import org.controlsfx.control.GridView;
import org.controlsfx.control.PopOver;

import pe.mrodas.entity.FileItem;
import pe.mrodas.entity.Tag;
import pe.mrodas.helper.FileHelper;
import pe.mrodas.helper.TagBar;
import pe.mrodas.model.FileModel;
import pe.mrodas.model.RestClient;
import pe.mrodas.model.TagModel;
import pe.mrodas.worker.ServiceGetMissingFiles;
import pe.mrodas.worker.ServiceReadFiles;

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
    static final String PATH = "cache";
    private ServiceGetFileFilter serviceGetFileFilter;
    private ServiceReadFiles serviceReadFiles, serviceGetExistingFiles;
    private ServiceGetMissingFiles serviceGetMissingFiles;
    private CollectionController parent;
    private TagBar<Tag> tagBar;
    private File selectedFile;
    private Tag selectedTag;

    public void initialize() {
        configProperty.addListener((o, old, config) -> {
            parent = config.getParent();
            this.setServices();
            config.buildTagButtons(tagButtons, this::onCategoryIsSelected);
            tagBar = new TagBar<>(Tag::getName, hint -> {
                Integer idRoot = config.getRoot().getIdRoot();
                List<Tag> tags = RestClient.execute(TagModel.class, tagModel -> tagModel.getTagSuggestions(idRoot, hint)).body();
                if (selectedTag != null) {/*
                    return tags.stream()
                            .filter(tag -> !tag.getIdTag().equals(selectedTag.getIdTag()))
                            .collect(Collectors.toList());*/
                }
                return tags;
            });
            tagContainer.getChildren().add(1, tagBar);
            tagBar.setOnSuggestionIsSelected(this::onTagIsAdded);
        });
        gridFiles.setCellFactory(param -> new CollectionController.GridCellImage(this::onImageSingleClick, this::onImageDoubleClick));
    }

    private void onTagIsAdded() {
        List<Integer> tagsId = tagBar.getTags().stream()
                .map(Tag::getIdTag)
                .collect(Collectors.toList());
        tagsId.add(selectedTag.getIdTag());
        this.serviceGetFileFilterRestart(tagsId);
    }

    private void onCategoryIsSelected(Tag tag) {
        tagContainer.setDisable(tag == null);
        if (tag == null) {
            gridFiles.getItems().clear();
            toolbar.setDisable(true);
            imageView.setImage(null);
        } else {
            selectedTag = tag;
            List<Integer> tagsId = Collections.singletonList(tag.getIdTag());
            this.serviceGetFileFilterRestart(tagsId);
        }
    }

    private void serviceGetFileFilterRestart(List<Integer> tagsId) {
        serviceGetFileFilter.setTagsId(tagsId);
        this.bindService(serviceGetFileFilter);
        serviceGetFileFilter.restart();
    }

    private void onImageSingleClick(File file) {
        if (!file.equals(selectedFile)) {
            if (CollectionController.setImageView(imageView, file, gridFiles.getHeight() - toolbar.getHeight(), splitPane)) {
                toolbar.setDisable(false);
                selectedFile = file;
            }
        }
    }

    private void onImageDoubleClick(File file, MouseEvent e) {
        ClipboardContent content = new ClipboardContent();
        content.putString(file.getAbsolutePath());
        Clipboard.getSystemClipboard().setContent(content);
        Label text = new Label("Image Path Copied!");
        VBox box = new VBox(text);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(0, 20, 0, 20));
        PopOver popOver = new PopOver(box);
        popOver.setDetachable(false);
        popOver.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
        popOver.show((Node) e.getSource(), -2);
    }

    private void setServices() {
        serviceGetFileFilter = new ServiceGetFileFilter();
        serviceGetFileFilter.setOnSucceeded(event -> {
            List<String> fileList = serviceGetFileFilter.getValue();
            serviceReadFiles.setFilter(file -> {
                String fileName = file.getName();
                return fileList.contains(fileName);
            });
            this.bindService(serviceReadFiles);
            serviceReadFiles.restart();
        });
        this.bindService(serviceGetFileFilter);
        serviceGetFileFilter.setOnFailed(parent::onServiceFailed);
        Path currentPath = parent.getPath(PATH);
        serviceReadFiles = new ServiceReadFiles(currentPath, null);
        serviceReadFiles.setOnSucceeded(event -> {
            List<File> files = serviceReadFiles.getValue();
            gridFiles.setItems(FXCollections.observableArrayList(files));
        });
        serviceReadFiles.setOnFailed(parent::onServiceFailed);
        serviceGetExistingFiles = new ServiceReadFiles(currentPath, null);
        serviceGetExistingFiles.setOnFailed(parent::onServiceFailed);
        serviceGetExistingFiles.setOnSucceeded(e -> {
            List<String> md5List = serviceGetExistingFiles.getValue().stream()
                    .map(file -> FileHelper.getName(file.getName()))
                    .collect(Collectors.toList());
            serviceGetMissingFiles.setMd5List(md5List);
            this.bindService(serviceGetMissingFiles);
            serviceGetMissingFiles.restart();
        });
        Integer idRoot = parent.getConfigProperty().get().getRoot().getIdRoot();
        serviceGetMissingFiles = new ServiceGetMissingFiles(currentPath, idRoot);
        serviceGetMissingFiles.setOnFailed(parent::onServiceFailed);
        serviceGetMissingFiles.setOnSucceeded(event -> {
            this.bindService(serviceReadFiles);
            serviceReadFiles.restart();
        });
    }

    private void bindService(Service<?> service) {
        topPane.disableProperty().bind(service.runningProperty());
        splitPane.disableProperty().bind(service.runningProperty());
        progressController.bindService(service);
    }

    class ServiceGetFileFilter extends Service<List<String>> {
        @Setter
        private List<Integer> tagsId;

        @Override
        protected Task<List<String>> createTask() {
            return new Task<List<String>>() {
                @Override
                protected List<String> call() throws Exception {
                    super.updateMessage("Getting Files...");
                    List<FileItem> fileItems = RestClient.execute(FileModel.class, fileModel ->
                            fileModel.getFiles(tagsId)).body();
                    if (fileItems == null) {
                        return new ArrayList<>();
                    }
                    return fileItems.stream()
                            .map(item -> String.format("%s.%s", item.getMd5(), item.getExtension()))
                            .collect(Collectors.toList());
                }
            };
        }
    }

    @FXML
    public void btnMissingOnClick(ActionEvent event) {
        this.bindService(serviceGetExistingFiles);
        serviceGetExistingFiles.restart();
    }

    @FXML
    public void btnEditOnClick(ActionEvent event) {
    }

    @FXML
    public void btnTrashOnClick(ActionEvent event) {
    }
}
