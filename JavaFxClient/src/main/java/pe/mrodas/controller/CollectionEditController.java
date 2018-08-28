package pe.mrodas.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Service;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import lombok.Getter;
import org.controlsfx.control.GridView;
import pe.mrodas.MainApp;
import pe.mrodas.helper.FileHelper;
import pe.mrodas.helper.GridCellImage;
import pe.mrodas.worker.ServiceMoveFilesTo;
import pe.mrodas.worker.ServiceReadFiles;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

public class CollectionEditController {
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

    static final String PATH = "edit";
    @Getter
    private final SimpleObjectProperty<ConfigCtrl> configProperty = new SimpleObjectProperty<>();
    private CollectionController parent;
    private File selectedFile;
    private ServiceReadFiles serviceReadFiles;
    private ServiceMoveFilesTo serviceMoveFilesToStage, serviceMoveFilesToTrash;

    public void initialize() {
        configProperty.addListener((o, old, config) -> {
            parent = config.getParent();
            this.setServicesMoveTo();
            this.setServiceReadFiles(config.getFileFilter());
            this.bindService(serviceReadFiles);
            serviceReadFiles.restart();
        });
        gridFiles.setCellFactory(param -> new GridCellImage(this::onImageClick).setPathToClipboardOnDoubleClick());
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
        serviceMoveFilesToStage = parent.getServiceMoveFilesTo(CollectionStageController.PATH);
        serviceMoveFilesToStage.setFileNameBuilder(FileHelper::getMD5);
        serviceMoveFilesToStage.setOnSucceeded(e -> this.onSuccessMove(CollectionStageController.PATH, e));
        serviceMoveFilesToTrash = parent.getServiceMoveFilesTo(CollectionTrashController.PATH);
        serviceMoveFilesToTrash.setFileNameBuilder(FileHelper::getMD5);
        serviceMoveFilesToTrash.setOnSucceeded(e -> this.onSuccessMove(CollectionTrashController.PATH, e));
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

    private void onSuccessMove(String path, WorkerStateEvent e) {
        gridFiles.getItems().remove(selectedFile);
        imageView.setImage(null);
        selectedFile = null;
        toolbar.setDisable(true);
        parent.addToGrid(path, (ServiceMoveFilesTo) e.getSource());
    }

    private void bindService(Service<?> service) {
        if (service.getOnFailed() == null) {
            service.setOnFailed(parent::onServiceFailed);
        }
        splitPane.disableProperty().bind(service.runningProperty());
        progressController.bindService(service);
    }

    @FXML
    private void btnOpenOnClick(ActionEvent e) {
        MainApp.openFile.accept(selectedFile);
    }

    @FXML
    public void btnRefreshOnClick(ActionEvent e) {
        imageView.setImage(null);
        CollectionController.setImageView(imageView, selectedFile, gridFiles.getHeight(), splitPane);
    }

    @FXML
    public void btnStageOnClick(ActionEvent e) {
        serviceMoveFilesToStage.setSourceFile(selectedFile);
        this.bindService(serviceMoveFilesToStage);
        serviceMoveFilesToStage.restart();
    }

    @FXML
    public void btnTrashOnClick(ActionEvent e) {
        serviceMoveFilesToTrash.setSourceFile(selectedFile);
        this.bindService(serviceMoveFilesToTrash);
        serviceMoveFilesToTrash.restart();
    }

    void addToGrid(File file) {
        gridFiles.getItems().add(file);
    }
}
