package pe.mrodas.controller;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import pe.mrodas.MainApp;
import pe.mrodas.entity.Root;
import pe.mrodas.entity.Tag;
import pe.mrodas.worker.ServiceGetCategories;
import pe.mrodas.worker.ServiceSaveConfig;

import java.util.List;


public class MenuController extends BaseController {
    @FXML
    private FlowPane content;
    @FXML
    private Label lblNoButtons;
    @FXML
    private ProgressController progressController;

    private final ServiceGetCategories serviceGetCategories = new ServiceGetCategories();

    public MenuController() {
        super("/fxml/Menu.fxml");
        super.setTitle("Menu");
    }

    @Override
    public void initialize() {
        Button[] buttons = MainApp.getSession().getRootList().stream()
                .map(this::getButton)
                .toArray(Button[]::new);
        if (buttons.length == 0) {
            content.setVisible(false);
            content.setManaged(false);
        } else {
            lblNoButtons.setVisible(false);
            lblNoButtons.setManaged(false);
            for (Button button : buttons) {
                content.getChildren().add(button);
            }
        }
        this.bindService(serviceGetCategories);
        if (buttons.length == 1) {
            Platform.runLater(() -> buttons[0].fire());
        }
    }

    private void setBtnGraphic(Button button, String url) {
        int size = 100;
        Image image = new Image(url, size, size, false, true, true);
        image.progressProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() == 1) {
                button.setGraphic(new ImageView(image));
            }
        });
        ProgressBar progressBar = new ProgressBar();
        progressBar.setPrefWidth(80);
        progressBar.progressProperty().bind(image.progressProperty());
        StackPane pane = new StackPane(progressBar);
        pane.setStyle("-fx-border-color: gray; -fx-border-style: dashed");
        pane.setPrefSize(size, size);
        button.setGraphic(pane);
    }

    private Button getButton(Root root) {
        Button button = new Button(root.getName());
        button.setUserData(root);
        button.setPrefHeight(140);
        button.setContentDisplay(ContentDisplay.TOP);
        button.setOnAction(this::btnActionOnClick);
        this.setBtnGraphic(button, root.getImageUrl());
        return button;
    }

    private void btnActionOnClick(ActionEvent event) {
        if (MainApp.getSession().getWorkingDir() == null) {
            Dialog<String> dialogCustom = super.dialogCustom(new WorkspaceDialog());
            dialogCustom.showAndWait().ifPresent(workingDir -> {
                ServiceSaveConfig serviceSaveConfig = new ServiceSaveConfig(true);
                serviceSaveConfig.setWorkingDir(workingDir);
                serviceSaveConfig.setOnSucceeded(e -> {
                    MainApp.getSession().setWorkingDir(workingDir);
                    this.bindService(serviceGetCategories);
                    this.showController(event);
                });
                this.bindService(serviceSaveConfig);
                serviceSaveConfig.restart();
            });
        } else {
            this.showController(event);
        }
    }

    private void showController(ActionEvent event) {
        Root root = (Root) ((Button) event.getSource()).getUserData();
        ConfigCtrl config = new ConfigCtrl(root);
        switch (config.getMediaType()) {
            case IMAGE:
                serviceGetCategories.setIdRoot(root.getIdRoot());
                serviceGetCategories.setOnSucceeded(e -> {
                    List<Tag> categories = serviceGetCategories.getValue();
                    config.setCategories(categories);
                    this.handle(() -> new CollectionController()
                            .prepareStage(config, content)
                            .show());
                });
                serviceGetCategories.restart();
                break;
            default:
                super.dialogInfo("No Action Set");
        }
    }

    private void bindService(Service<?> service) {
        service.setOnFailed(super::onServiceFailed);
        content.disableProperty().bind(service.runningProperty());
        progressController.bindService(service);
    }

    enum Dimension {
        UNIDIMENSIONAL(1), BIDIMENSIONAL(2), NONE(0);
        private final int dim;

        Dimension(int dim) {
            this.dim = dim;
        }
    }
}
