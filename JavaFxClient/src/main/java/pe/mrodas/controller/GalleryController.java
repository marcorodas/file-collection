package pe.mrodas.controller;

import com.jfoenix.controls.JFXProgressBar;
import javafx.beans.property.BooleanProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import lombok.Setter;
import pe.mrodas.helper.GuiFxHelper;
import pe.mrodas.helper.TaskImportFiles;

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

    private ImportFilesService importFilesService;

    private class ImportFilesService extends Service<Void> {

        @Setter
        private List<File> fileList;

        @Override
        protected Task<Void> createTask() {
            return new TaskImportFiles(fileList);
        }
    }

    public GalleryController() {
        super("/fxml/Gallery.fxml");
    }

    @Override
    public void initialize() {
        this.setServices();
    }

    private void setServices() {
        importFilesService = new ImportFilesService();
        BooleanProperty[] properties = new BooleanProperty[]{
                txtService.visibleProperty(), txtService.managedProperty(),
                progressBar.visibleProperty(), progressBar.managedProperty(),
                separator.visibleProperty(), separator.managedProperty()
        };
        for (BooleanProperty property : properties) {
            property.bind(importFilesService.runningProperty());
        }
        txtService.textProperty().bind(importFilesService.messageProperty());
        progressBar.progressProperty().bind(importFilesService.progressProperty());
    }

    @FXML
    public void btnClearOnClick(ActionEvent actionEvent) {
        System.out.println("Hola");
    }

    @FXML
    public void btnNoTagsOnClick(ActionEvent actionEvent) {
    }

    @FXML
    public void btnMenuOnClick(ActionEvent actionEvent) {
    }

    @FXML
    public void btnImportOnClick(ActionEvent actionEvent) {
        String[] extensions = new String[]{
                "*.jpg", "*.jpeg", "*.gif", "*.png", "*.bmp"
        };
        ExtensionFilter extFilter = new ExtensionFilter("Images", extensions);
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(extFilter);
        chooser.setTitle("Importar Imágenes");
        List<File> selectedFiles = chooser.showOpenMultipleDialog(GuiFxHelper.getOwner(actionEvent));
        importFilesService.setFileList(selectedFiles);
        importFilesService.restart();
    }
}
