package pe.mrodas.controller;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Dimension2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.dialog.ProgressDialog;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
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

    public interface OnError {
        void handle(Exception ex);
    }

    public interface Handler<T> {
        void accept(T t) throws Exception;
    }

    private static String appTitle, appStyle;
    private static List<String> appIcons;

    private final String fxmlFile;

    private Dimension2D dimension;
    private final List<String> styleFiles = new ArrayList<>();
    private Stage stage, owner;
    private String title;

    private boolean isResizable;

    /**
     * Constructor must be public
     *
     * @param fxmlFile fxml path (begins with '/'). fx:controller is specified in fxml
     */
    public BaseController(String fxmlFile) {
        this.fxmlFile = fxmlFile;
        this.setStyle(appStyle);
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

    public void setResizable(boolean resizable) {
        isResizable = resizable;
    }

    public BaseController setAppIcons(Stream<String> appIcons) {
        BaseController.appIcons = appIcons.collect(Collectors.toList());
        return this;
    }

    public BaseController setAppStyle(String appStyle) {
        BaseController.appStyle = appStyle;
        this.setStyle(appStyle);
        return this;
    }

    public BaseController setStyle(String cssFile) {
        if (cssFile != null && !styleFiles.contains(cssFile)) {
            styleFiles.add(cssFile);
        }
        return this;
    }

    public BaseController setOwner(Stage owner) {
        this.owner = owner;
        return this;
    }

    public BaseController setAppTitle(String appTitle) {
        BaseController.appTitle = appTitle;
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
            appIcons.stream().map(Image::new).forEach(stage.getIcons()::add);
        }
    }

    private Parent getRoot() throws IOException {
        URL url = this.getClass().getResource(fxmlFile);
        if (this.hasController(url)) {
            return FXMLLoader.load(url);
        }
        FXMLLoader loader = new FXMLLoader(url);
        loader.setController(this);
        return loader.load();
    }

    private boolean hasController(URL url) throws IOException {
        try (InputStream inputStream = url.openStream()) {
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().parse(inputStream);
            return doc.getDocumentElement().hasAttribute("fx:controller");
        } catch (ParserConfigurationException | SAXException e) {
            return false;
        }
    }

    public Stage prepareStage(Node node) throws IOException {
        return this.prepareStage((Stage) node.getScene().getWindow());
    }

    public Stage prepareStage(Stage mStage) throws IOException {
        return this.prepareStage(this.getRoot(), mStage);
    }

    public Stage prepareStage(Parent root, Node node) {
        return this.prepareStage(root, (Stage) node.getScene().getWindow());
    }

    public Stage prepareStage(Parent root, Stage mStage) {
        if (stage == null) {
            stage = mStage == null ? new Stage() : mStage;
            if (owner != null) {
                stage.initModality(Modality.WINDOW_MODAL);
                stage.initOwner(owner);
            }
            Scene scene = dimension == null
                    ? new Scene(root)
                    : new Scene(root, dimension.getWidth(), dimension.getHeight());
            styleFiles.forEach(scene.getStylesheets()::add);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setResizable(isResizable);
            stage.sizeToScene();
            stage.setTitle(this.getTitle());
            this.setAppIcons(stage);
        }
        return stage;
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

    void onServiceFailed(WorkerStateEvent e) {
        this.alertError(e.getSource().getException());
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

    void handle(Runnable runnable, OnError onError) {
        try {
            runnable.run();
        } catch (Exception e) {
            onError.handle(e);
        }
    }

    void handle(Runnable runnable) {
        this.handle(runnable, e -> this.alertError(e));
    }

    void setOnAction(Button button, Handler<ActionEvent> handler) {
        button.setOnAction(e -> {
            try {
                handler.accept(e);
            } catch (Exception ex) {
                this.alertError(ex);
                ex.printStackTrace();
            }
        });
    }
}
