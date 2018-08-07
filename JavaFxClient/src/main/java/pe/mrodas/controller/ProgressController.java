package pe.mrodas.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;

public class ProgressController {
    @FXML
    public Separator separator;
    @FXML
    public Label lblReady;
    @FXML
    private Label lblServiceMessage;
    @FXML
    private ProgressBar progressBar;

    private final SimpleObjectProperty<Service<?>> serviceObjectProperty = new SimpleObjectProperty<>();

    void bindService(Service<?> service) {
        serviceObjectProperty.set(service);
    }

    public void initialize() {
        serviceObjectProperty.addListener((observable, oldService, service) -> {
            lblReady.visibleProperty().bind(service.runningProperty().not());
            lblReady.managedProperty().bind(service.runningProperty().not());
            lblServiceMessage.visibleProperty().bind(service.runningProperty());
            progressBar.visibleProperty().bind(service.runningProperty());
            lblServiceMessage.textProperty().bind(service.messageProperty());
            progressBar.progressProperty().bind(service.progressProperty());
        });
    }
}
