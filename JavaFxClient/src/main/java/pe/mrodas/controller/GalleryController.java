package pe.mrodas.controller;

import com.jfoenix.controls.JFXProgressBar;
import javafx.beans.property.BooleanProperty;
import javafx.concurrent.Service;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;
import pe.mrodas.entity.MenuEnum;
import pe.mrodas.worker.ServiceImportFiles;

import java.io.File;
import java.util.List;

public class GalleryController extends BaseController {

    @FXML
    public TextField txtSearchTag;
    @FXML
    public Label lblSelectedTags;
    @FXML
    public JFXProgressBar progressBar;
    @FXML
    public Text txtService;
    @FXML
    public Separator separator;

    private ServiceImportFiles serviceImportFiles;
    private final MenuEnum selected;

    public GalleryController(MenuEnum selected) {
        super("/fxml/Gallery.fxml");
        this.selected = selected;
    }

    @Override
    public void initialize() {
        this.setServices();
    }

    private void setServices() {
        serviceImportFiles = new ServiceImportFiles();
        this.bindProgressProperties(serviceImportFiles);
        txtService.textProperty().bind(serviceImportFiles.messageProperty());
        progressBar.progressProperty().bind(serviceImportFiles.progressProperty());
    }

    private void bindProgressProperties(Service<?> service) {
        BooleanProperty[] properties = new BooleanProperty[]{
                txtService.visibleProperty(), txtService.managedProperty(),
                progressBar.visibleProperty(), progressBar.managedProperty(),
                separator.visibleProperty(), separator.managedProperty()
        };
        for (BooleanProperty property : properties) {
            property.bind(service.runningProperty());
        }
    }

    private List<File> getStageImportFiles(Window window) {
        String[] extensions = new String[]{"*.jpg", "*.jpeg", "*.gif", "*.png", "*.bmp"};
        ExtensionFilter extFilter = new ExtensionFilter("Images", extensions);
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(extFilter);
        chooser.setTitle("Importar Imágenes");
        return chooser.showOpenMultipleDialog(window);
    }

    @FXML
    public void btnStageImportOnClick(ActionEvent actionEvent) {
        Window window = ((Node) actionEvent.getSource()).getScene().getWindow();
        List<File> selectedFiles = this.getStageImportFiles(window);
        serviceImportFiles.setFileList(selectedFiles);
        serviceImportFiles.restart();
    }
}