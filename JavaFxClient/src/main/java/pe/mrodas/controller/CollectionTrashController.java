package pe.mrodas.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Service;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import lombok.Getter;
import org.controlsfx.control.GridView;
import pe.mrodas.helper.GridCellImage;
import pe.mrodas.worker.ServiceMoveFilesTo;
import pe.mrodas.worker.ServiceReadFiles;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

public class CollectionTrashController {
    @FXML
    public ToolBar toolbar;
    @FXML
    public ImageView imageView;
    @FXML
    public BorderPane imageContainer;
    @FXML
    public GridView<File> gridFiles;
    @FXML
    public SplitPane splitPane;
    @FXML
    public Label lblTotal;
    @FXML
    private ProgressController progressController;

    static final String PATH = "trash";
    @Getter
    private final SimpleObjectProperty<ConfigCtrl> configProperty = new SimpleObjectProperty<>();
    private CollectionController parent;
    private File selectedFile;
    private ServiceReadFiles serviceReadFiles;
    private ServiceMoveFilesTo serviceMoveFilesToStage;

    public void initialize() {
        configProperty.addListener((o, old, config) -> {
            parent = config.getParent();
            this.setServicesMoveTo();
            this.setServiceReadFiles(config.getFileFilter());
            this.bindService(serviceReadFiles);
            serviceReadFiles.restart();
        });
        gridFiles.setCellFactory(param -> new GridCellImage(this::onImageClick));
    }

    private void onImageClick(File file) {
        if (!file.equals(selectedFile)) {
            if (CollectionController.setImageView(imageView, file, gridFiles.getHeight(), splitPane)) {
                toolbar.setDisable(false);
                selectedFile = file;
            }
        }
    }

    private void setServicesMoveTo() {
        String path = CollectionStageController.PATH;
        serviceMoveFilesToStage = parent.getServiceMoveFilesTo(path);
        serviceMoveFilesToStage.setOnSucceeded(e -> {
            gridFiles.getItems().remove(selectedFile);
            this.clearImageView();
            parent.addToGrid(path, (ServiceMoveFilesTo) e.getSource());
        });
    }

    private void clearImageView() {
        imageView.setImage(null);
        selectedFile = null;
        toolbar.setDisable(true);
    }

    private void setServiceReadFiles(FileFilter filter) {
        serviceReadFiles = new ServiceReadFiles(parent.getPath(PATH), filter);
        serviceReadFiles.setOnSucceeded(event -> {
            List<File> files = serviceReadFiles.getValue();
            parent.setNumFiles(lblTotal, files.size());
            gridFiles.setItems(FXCollections.observableArrayList(files));
            gridFiles.itemsProperty().get().addListener((ListChangeListener<? super File>) c -> {
                int size = gridFiles.getItems().size();
                parent.setNumFiles(lblTotal, size);
            });
        });
    }

    private void bindService(Service<?> service) {
        if (service.getOnFailed() == null) {
            service.setOnFailed(parent::onServiceFailed);
        }
        splitPane.disableProperty().bind(service.runningProperty());
        progressController.bindService(service);
    }

    @FXML
    public void btnStageOnClick(ActionEvent e) {
        serviceMoveFilesToStage.setSourceFile(selectedFile);
        this.bindService(serviceMoveFilesToStage);
        serviceMoveFilesToStage.restart();
    }

    @FXML
    public void btnDeleteOnClick(ActionEvent e) {
        parent.dialogConfirm("Are you sure you want to delete?").ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                gridFiles.getItems().remove(selectedFile);
                if (selectedFile.delete()) {
                    this.clearImageView();
                } else {
                    parent.dialogWarning("Unable to delete the file!");
                }
            }
        });
    }

    void addToGrid(File file) {
        gridFiles.getItems().add(file);
    }
}
