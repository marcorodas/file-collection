package pe.mrodas.controller;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.jfoenix.controls.JFXTextField;

import pe.mrodas.MainApp;
import pe.mrodas.entity.Config;
import pe.mrodas.entity.Environment;
import pe.mrodas.model.ConfigModel;
import pe.mrodas.model.RestClient;

public class WorkspaceController extends BaseController {

    class ServiceSaveConfig extends Service<Void> {

        private final boolean isNew;
        private final Config config;

        ServiceSaveConfig(boolean isNew, Config config) {
            this.isNew = isNew;
            this.config = config;
        }

        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    super.updateMessage("Saving config...");
                    return RestClient.execute(ConfigModel.class, model ->
                            isNew ? model.insert(config) : model.update(config)).body();
                }
            };
        }
    }

    @FXML
    private VBox content;
    @FXML
    private JFXTextField txtWorkspace;
    @FXML
    private ProgressController progressController;

    private final Config config = new Config().setEnvironment(Environment.get());
    private final Service<Void> serviceSaveConfig = new ServiceSaveConfig(true, config);


    WorkspaceController() {
        super("/fxml/Workspace.fxml");
        this.setTitle("Workspace Selection");
    }

    @Override
    public void initialize() {
        content.disableProperty().bind(serviceSaveConfig.runningProperty());
        progressController.bindService(serviceSaveConfig);
        serviceSaveConfig.setOnFailed(super::onServiceFailed);
    }

    WorkspaceController setOnSaveSuccess(Runnable runOnOkClick) {
        serviceSaveConfig.setOnSucceeded(event -> {
            MainApp.getSession().setWorkingDir(txtWorkspace.getText());
            super.closeWindow();
            super.handle(runOnOkClick);
        });
        return this;
    }

    @FXML
    public void txtWorkspaceOnKeyPressed(KeyEvent e) {
        if (e.getCode() == KeyCode.ENTER) {
            this.btnBrowseOnClick(null);
        }
    }

    @FXML
    public void btnBrowseOnClick(ActionEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Workspace Selection");
        if (!txtWorkspace.getText().isEmpty()) {
            Path path = Paths.get(txtWorkspace.getText());
            if (Files.isDirectory(path)) {
                chooser.setInitialDirectory(path.toFile());
            }
        }
        File file = chooser.showDialog(super.getStage(event));
        if (file != null) {
            txtWorkspace.setText(file.getAbsolutePath());
        }
    }

    @FXML
    public void btnOkOnClick(ActionEvent e) {
        String workspace = txtWorkspace.getText();
        if (Files.isDirectory(Paths.get(workspace))) {
            config.setWorkingDir(workspace);
            serviceSaveConfig.restart();
        } else {
            super.dialogWarning("Invalid directory!\n" + workspace);
        }
    }

    @FXML
    public void btnCancelOnClick(ActionEvent event) {
        super.getStage(event).close();
    }
}
