package pe.mrodas.controller;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Dimension2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.dialog.ProgressDialog;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class BaseController {

    public interface Runnable {
        void run() throws Exception;
    }

    private static String appTitle, appStyle;
    private static Stream<String> appIcons;

    private final FXMLLoader loader;
    private final boolean controllerInFxml;
    private Dimension2D dimension;
    private final List<String> styleFiles = new ArrayList<>();
    private Stage stage, owner;
    private String title;

    public static void config(String appTitle, String appStyle, Stream<String> appIcons) {
        BaseController.appTitle = appTitle;
        BaseController.appStyle = appStyle;
        BaseController.appIcons = appIcons;
    }

    public BaseController(String fxmlFile) {
        this(fxmlFile, false);
    }

    public BaseController(String fxmlFile, boolean controllerInFxml) {
        URL url = this.getClass().getResource(fxmlFile);
        loader = new FXMLLoader(url);
        this.controllerInFxml = controllerInFxml;
        if (appStyle != null) {
            this.styleFiles.add(appStyle);
        }
    }

    /**
     * An instance of the FXMLLoader class looks for this method and calls it,
     * when the contents of the associated FXML document had been completely loaded
     */
    public abstract void initialize();

    public BaseController setDimension(double width, double height) {
        this.dimension = new Dimension2D(width, height);
        return this;
    }

    public BaseController setStyle(String cssFile) {
        if (!styleFiles.contains(cssFile)) {
            styleFiles.add(cssFile);
        }
        return this;
    }

    public BaseController setOwner(Stage owner) {
        this.owner = owner;
        return this;
    }

    public BaseController setTitle(String title) {
        this.title = title;
        return this;
    }

    private String getTitle() {
        return Stream.of(appTitle, title)
                .filter(s -> s != null && !s.trim().isEmpty())
                .collect(Collectors.joining(" - "));
    }

    private void setAppIcons(Stage stage) {
        if (appIcons != null && stage.getIcons().isEmpty()) {
            appIcons.map(Image::new).forEach(stage.getIcons()::add);
        }
    }

    public Stage prepareStage(Stage mStage) throws IOException {
        if (stage == null) {
            stage = mStage == null ? new Stage() : mStage;
            if (owner != null) {
                stage.initModality(Modality.WINDOW_MODAL);
                stage.initOwner(owner);
            }
            if (!controllerInFxml) {
                loader.setController(this);
            }
            Scene scene = dimension == null
                    ? new Scene(loader.load())
                    : new Scene(loader.load(), dimension.getWidth(), dimension.getHeight());
            styleFiles.forEach(scene.getStylesheets()::add);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setTitle(this.getTitle());
            this.setAppIcons(stage);
        }
        return stage;
    }

    public Stage prepareStage() throws IOException {
        return this.prepareStage(new Stage());
    }

    public Stage prepareStage(BaseController base) throws IOException {
        return this.prepareStage(base == null ? null : base.stage);
    }

    public void setCursorBusy(boolean isBusy) {
        if (stage != null) {
            stage.getScene().setCursor(isBusy ? Cursor.WAIT : Cursor.DEFAULT);
        }
    }

    private Alert getAlert(Alert.AlertType type, String contentText) {
        Alert alert = new Alert(type, contentText);
        this.setAppIcons((Stage) alert.getDialogPane().getScene().getWindow());
        return alert;
    }

    void alertInfo(String... msjs) {
        String txt = Stream.of(msjs).collect(Collectors.joining(""));
        this.getAlert(Alert.AlertType.INFORMATION, txt).showAndWait();
    }

    void alertError(Throwable ex, String... msjs) {
        String join = msjs == null ? "" : Stream.of(msjs).collect(Collectors.joining(", "));
        String exMsj = ex.getMessage() == null ? ex.toString() : ex.getMessage();
        this.getAlert(Alert.AlertType.ERROR, join.isEmpty() ? exMsj : (exMsj + ": " + join))
                .showAndWait();
    }

    <T> void showProgressDialog(String title, Task<T> task, Consumer<T> onSuccess) {
        task.setOnSucceeded(event -> onSuccess.accept(task.getValue()));
        task.setOnFailed(event -> {
            this.alertError(task.getException());
            this.stage.close();
        });
        ProgressDialog dialog = new ProgressDialog(task);
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setGraphic(null);
        this.setAppIcons((Stage) dialog.getDialogPane().getScene().getWindow());
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    void handle(Runnable runnable, Consumer<Exception> onError) {
        try {
            runnable.run();
        } catch (Exception e) {
            onError.accept(e);
        }
    }

    void handle(Runnable runnable) {
        this.handle(runnable, e -> this.alertError(e));
    }
}
