package pe.mrodas.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Service;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.FileFilter;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import lombok.Getter;
import org.controlsfx.control.GridView;

import pe.mrodas.entity.Tag;
import pe.mrodas.worker.ServiceGetCategories;
import pe.mrodas.worker.ServiceGetImageFromUrl;
import pe.mrodas.worker.ServiceMoveFiles;
import pe.mrodas.worker.ServiceReadFiles;
import pe.mrodas.worker.ServiceUploadFiles;


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
    public VBox uploadContainer;
    @FXML
    private ProgressController progressController;

    private static final String PATH = "stage";
    @Getter
    private final SimpleObjectProperty<CollectionController.Config> configProperty = new SimpleObjectProperty<>();
    private CollectionController parent;
    private Service<List<Tag>> serviceGetCategories;
    private ServiceReadFiles serviceReadFiles;
    private ServiceGetImageFromUrl serviceGetImageFromUrl;
    private ServiceUploadFiles serviceUploadFiles;
    private ServiceMoveFiles serviceMoveFiles;
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
            this.setServiceReadFiles(config.getExtensions());
            this.setServiceGetCategories(config);
            this.setServiceGetImageFromUrl();
            this.setServiceUploadFiles();
            this.setServiceMoveFiles();
            this.bindService(serviceReadFiles);
            serviceReadFiles.restart();
        });
        setImageView = file -> {
            if (!file.equals(selectedFile)) {
                if (CollectionController.setImageView(imageView, file, gridFiles.getHeight(), splitPane)) {
                    toolbar.setDisable(false);
                    selectedFile = file;
                }
            }
        };
        gridFiles.setCellFactory(param -> new CollectionController.GridCellImage(setImageView, (file, e) -> {
            gridUpload.getItems().add(file);
            gridFiles.getItems().remove(file);
            if (selectedTag != null) {
                btnUpload.setDisable(false);
            }
        }));
        gridUpload.setItems(FXCollections.observableArrayList());
        gridUpload.itemsProperty().get().addListener((ListChangeListener<? super File>) c -> {
            int size = c.getList().size();
            lblNumFilesUpload.setText(String.valueOf(size));
        });
        gridUpload.setCellFactory(param -> new CollectionController.GridCellImage(setImageView, (file, e) -> {
            gridFiles.getItems().add(0, file);
            gridUpload.getItems().remove(file);
            if (gridUpload.getItems().isEmpty()) {
                btnUpload.setDisable(true);
            }
        }));
    }

    private void setServiceReadFiles(List<String> extensions) {
        FileFilter filter = (extensions == null) ? null : file -> extensions.stream()
                .map(s -> s.replace("*", ""))
                .anyMatch(s -> file.getName().endsWith(s));
        serviceReadFiles = new ServiceReadFiles(parent.getPath(PATH), filter);
        serviceReadFiles.setOnSucceeded(event -> {
            List<File> files = serviceReadFiles.getValue();
            if (!gridUpload.getItems().isEmpty()) {
                files = files.stream()
                        .filter(file -> !gridUpload.getItems().contains(file))
                        .collect(Collectors.toList());
            }
            String len = String.valueOf(files.size());
            lblTotal.setText(len);
            gridFiles.setItems(FXCollections.observableArrayList(files));
            gridFiles.itemsProperty().get().addListener((ListChangeListener<? super File>) c -> {
                int size = gridFiles.getItems().size();
                lblTotal.setText(String.valueOf(size));
            });
            if (tagButtons.getChildren().isEmpty()) {
                this.bindService(serviceGetCategories);
                serviceGetCategories.restart();
            }
        });
    }

    private void setServiceGetCategories(CollectionController.Config config) {
        serviceGetCategories = new ServiceGetCategories(config.getRoot().getIdRoot());
        serviceGetCategories.setOnSucceeded(event -> {
            List<Tag> tags = serviceGetCategories.getValue();
            config.getTagListProperty().set(tags);
        });
    }

    private void setServiceGetImageFromUrl() {
        serviceGetImageFromUrl = new ServiceGetImageFromUrl(parent.getPath(PATH));
        serviceGetImageFromUrl.setOnSucceeded(event -> {
            Path downloadedImg = serviceGetImageFromUrl.getValue();
            gridFiles.getItems().add(0, downloadedImg.toFile());
        });
        serviceGetImageFromUrl.setOnFailed(event -> {
            Throwable exception = serviceGetImageFromUrl.getException();
            if (exception instanceof FileAlreadyExistsException) {
                parent.dialogWarning("File Already Exists!", exception.getMessage());
            } else {
                parent.onServiceFailed(event);
            }
        });
    }

    private void setServiceUploadFiles() {
        serviceUploadFiles = new ServiceUploadFiles(parent.getPath(CollectionImportedController.PATH));
        serviceUploadFiles.setOnSucceeded(event -> this.clearGridUpload(null));
        serviceUploadFiles.setOnFailed(event -> {
            List<String> uploadedFileNames = serviceUploadFiles.getUploadedFileNames();
            this.clearGridUpload(uploadedFileNames);
            parent.onServiceFailed(event);
        });
        uploadContainer.disableProperty().bind(serviceUploadFiles.runningProperty());
    }

    private void setServiceMoveFiles() {
        serviceMoveFiles = new ServiceMoveFiles(parent.getPath(PATH));
        serviceMoveFiles.setOnSucceeded(event -> {
            this.bindService(serviceReadFiles);
            serviceReadFiles.restart();
        });
    }

    private void clearGridUpload(List<String> uploadedFileNames) {
        String selectedFileName = selectedFile.getName();
        boolean selectedFileUploaded;
        if (uploadedFileNames == null) {
            selectedFileUploaded = gridUpload.getItems().stream()
                    .anyMatch(file -> file.getName().equals(selectedFileName));
            gridUpload.getItems().clear();
        } else {
            selectedFileUploaded = uploadedFileNames.stream()
                    .anyMatch(selectedFileName::equals);
            gridUpload.getItems().removeIf(file -> {
                String fileName = file.getName();
                return uploadedFileNames.contains(fileName);
            });
        }
        btnUpload.setDisable(gridUpload.getItems().isEmpty());
        if (selectedFileUploaded) {
            imageView.setImage(null);
            selectedFile = null;
            toolbar.setDisable(true);
        }
    }

    private void bindService(Service<?> service) {
        if (service.getOnFailed() == null) {
            service.setOnFailed(parent::onServiceFailed);
        }
        splitPane.disableProperty().bind(service.runningProperty());
        progressController.bindService(service);
    }

    @FXML
    public void btnGetFilesOnClick(ActionEvent actionEvent) {
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Images", configProperty.get().getExtensions());
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(extFilter);
        chooser.setTitle("Import Images");
        List<File> files = chooser.showOpenMultipleDialog(parent.getStage(actionEvent));
        if (files != null && !files.isEmpty()) {
            serviceMoveFiles.setSourceFiles(files);
            this.bindService(serviceMoveFiles);
            serviceMoveFiles.restart();
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
        serviceUploadFiles.config(gridUpload.getItems(), selectedTag.getIdTag());
        this.bindService(serviceUploadFiles);
        serviceUploadFiles.restart();
    }

    @FXML
    public void btnGetFromUrlOnClick(ActionEvent event) {
        parent.dialogInputText(dialog -> {
            dialog.setTitle("Image URL");
            dialog.setContentText("URL:");
            TextField textField = dialog.getEditor();
            textField.setPromptText("Enter a valid URL");
            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, e -> {
                try {
                    serviceGetImageFromUrl.setImageUrl(textField.getText());
                } catch (Exception ex) {
                    e.consume();
                    parent.dialogWarning(ex.getMessage());
                }
            });
        }).ifPresent(result -> {
            this.bindService(serviceGetImageFromUrl);
            serviceGetImageFromUrl.restart();
        });
    }

}
