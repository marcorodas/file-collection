package pe.mrodas.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Service;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
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
import pe.mrodas.helper.FileHelper;
import pe.mrodas.helper.GridCellImage;
import pe.mrodas.worker.ServiceGetCategories;
import pe.mrodas.worker.ServiceGetImageFromUrl;
import pe.mrodas.worker.ServiceMoveFilesTo;
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

    static final String PATH = "stage";
    @Getter
    private final SimpleObjectProperty<CollectionController.Config> configProperty = new SimpleObjectProperty<>();
    private CollectionController parent;
    private Service<List<Tag>> serviceGetCategories;
    private ServiceReadFiles serviceReadFiles;
    private ServiceGetImageFromUrl serviceGetImageFromUrl;
    private ServiceUploadFiles serviceUploadFiles;
    private ServiceMoveFilesTo serviceMoveFiles, serviceMoveFilesToEdit, serviceMoveFilesToTrash;
    private Dialog<String> urlDialog;
    private File selectedFile;
    private Tag selectedCategory;

    public void initialize() {
        configProperty.addListener((o, old, config) -> {
            parent = config.getParent();
            parent.buildCategoryButtons(tagButtons, category -> {
                btnUpload.setDisable(category == null || gridUpload.getItems().isEmpty());
                selectedCategory = category;
            });
            this.setServiceReadFiles(config.getExtensions());
            this.setServiceGetCategories(config);
            this.setServiceGetImageFromUrl();
            this.setServiceUploadFiles();
            this.setServiceMoveFiles();
            this.buildUrlDialog();
            this.bindService(serviceReadFiles);
            this.configGrids();
            serviceReadFiles.restart();
        });
    }

    private void configGrids() {
        Consumer<File> setImageView = file -> {
            if (!file.equals(selectedFile)) {
                if (CollectionController.setImageView(imageView, file, gridFiles.getHeight(), splitPane)) {
                    toolbar.setDisable(false);
                    selectedFile = file;
                }
            }
        };
        gridFiles.setCellFactory(param -> new GridCellImage(setImageView, (file, e) -> {
            gridUpload.getItems().add(file);
            gridFiles.getItems().remove(file);
            if (selectedCategory != null) {
                btnUpload.setDisable(false);
            }
        }));
        gridUpload.setItems(FXCollections.observableArrayList());
        gridUpload.itemsProperty().get().addListener((ListChangeListener<? super File>) c -> {
            int size = c.getList().size();
            parent.setNumFiles(lblNumFilesUpload, size);
        });
        gridUpload.setCellFactory(param -> new GridCellImage(setImageView, (file, e) -> {
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
            parent.setNumFiles(lblTotal, files.size());
            gridFiles.setItems(FXCollections.observableArrayList(files));
            gridFiles.itemsProperty().get().addListener((ListChangeListener<? super File>) c -> {
                int size = gridFiles.getItems().size();
                parent.setNumFiles(lblTotal, size);
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
        serviceUploadFiles.setOnSucceeded(event -> {
            List<String> uploadedFileNames = gridUpload.getItems().stream().map(File::getName)
                    .collect(Collectors.toList());
            this.onUploadFinished(uploadedFileNames);
        });
        serviceUploadFiles.setOnFailed(event -> {
            List<String> uploadedFileNames = serviceUploadFiles.getUploadedFileNames();
            this.onUploadFinished(uploadedFileNames);
            parent.onServiceFailed(event);
        });
        uploadContainer.disableProperty().bind(serviceUploadFiles.runningProperty());
    }

    private void setServiceMoveFiles() {
        serviceMoveFiles = parent.getServiceMoveFilesTo(PATH);
        serviceMoveFiles.setFileNameBuilder(FileHelper::getMD5);
        serviceMoveFiles.setOnSucceeded(event -> {
            this.bindService(serviceReadFiles);
            serviceReadFiles.restart();
        });
        serviceMoveFilesToEdit = parent.getServiceMoveFilesTo(CollectionEditController.PATH);
        serviceMoveFilesToEdit.setOnSucceeded(e -> this.onSuccessMove(CollectionEditController.PATH, e));
        serviceMoveFilesToTrash = parent.getServiceMoveFilesTo(CollectionTrashController.PATH);
        serviceMoveFilesToTrash.setOnSucceeded(e -> this.onSuccessMove(CollectionTrashController.PATH, e));
    }

    private void onSuccessMove(String path, WorkerStateEvent e) {
        gridFiles.getItems().remove(selectedFile);
        this.clearImageView();
        parent.addToGrid(path, (ServiceMoveFilesTo) e.getSource());
    }

    private void clearImageView() {
        imageView.setImage(null);
        selectedFile = null;
        toolbar.setDisable(true);
    }

    private void buildUrlDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText(null);
        dialog.setTitle("Image URL");
        dialog.setContentText("URL:");
        TextField textField = dialog.getEditor();
        textField.setPromptText("Enter a valid URL");
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, e -> {
            try {
                String url = textField.getText();
                serviceGetImageFromUrl.setImageUrl(url);
            } catch (Exception ex) {
                e.consume();
                parent.dialogWarning(ex.getMessage());
            }
        });
        urlDialog = parent.dialogCustom(dialog);
    }

    private void onUploadFinished(List<String> uploadedFileNames) {
        boolean selectedFileUploaded = uploadedFileNames.stream()
                .anyMatch(selectedFile.getName()::equals);
        if (selectedFileUploaded) {
            this.clearImageView();
        }
        if (gridUpload.getItems().size() == uploadedFileNames.size()) {
            gridUpload.getItems().clear();
        } else {
            gridUpload.getItems().removeIf(file -> uploadedFileNames.contains(file.getName()));
        }
        parent.updateImportedFilesGrid(selectedCategory.getIdTag());
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
        serviceMoveFilesToEdit.setSourceFile(selectedFile);
        this.bindService(serviceMoveFilesToEdit);
        serviceMoveFilesToEdit.restart();
    }

    @FXML
    public void btnTrashOnClick(ActionEvent e) {
        serviceMoveFilesToTrash.setSourceFile(selectedFile);
        this.bindService(serviceMoveFilesToTrash);
        serviceMoveFilesToTrash.restart();
    }

    @FXML
    public void btnUploadOnClick(ActionEvent e) {
        serviceUploadFiles.config(gridUpload.getItems(), selectedCategory.getIdTag());
        this.bindService(serviceUploadFiles);
        serviceUploadFiles.restart();
    }

    @FXML
    public void btnGetFromUrlOnClick(ActionEvent e) {
        urlDialog.showAndWait().ifPresent(result -> {
            this.bindService(serviceGetImageFromUrl);
            serviceGetImageFromUrl.restart();
            ((TextInputDialog) urlDialog).getEditor().clear();
        });
    }

    void addToGrid(File file) {
        gridFiles.getItems().add(file);
    }
}
