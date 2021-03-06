package pe.mrodas.controller;

import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Dimension2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import pe.mrodas.helper.ExceptionAlert;

public abstract class BaseController {

    public interface Runnable {
        void run() throws Exception;
    }

    private static String appTitle, appStyle;
    private static List<String> appIcons;
    private static boolean debugMode;
    private static BiFunction<Throwable, Boolean, Alert> alertExceptionHandler;

    private final String fxmlFile;
    private final List<String> styleFiles = new ArrayList<>();

    private Dimension2D dimension;
    private Stage stage, owner;
    private String title;
    private Consumer<FXMLLoader> onFxmlLoaded;
    private boolean isResizable;

    /**
     * Constructor must be public. If fx:controller is provided in fxml file,
     * the controller shouldn't have parameters.
     * Specify fxml file using super() constructor.
     *
     * @param fxmlFile fxml path (begins with '/').
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

    public BaseController setDebugMode(boolean debugMode) {
        BaseController.debugMode = debugMode;
        return this;
    }

    public BaseController setAlertExceptionHandler(BiFunction<Throwable, Boolean, Alert> alertExceptionHandler) {
        BaseController.alertExceptionHandler = alertExceptionHandler;
        return this;
    }

    public void setStyle(String cssFile) {
        if (cssFile != null && !styleFiles.contains(cssFile)) {
            styleFiles.add(cssFile);
        }
    }

    public BaseController setOwner(Stage owner) {
        this.owner = owner;
        return this;
    }

    public BaseController setOwner(ActionEvent event) {
        this.owner = this.getStage(event);
        return this;
    }

    public BaseController setAppTitle(String appTitle) {
        BaseController.appTitle = appTitle;
        return this;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    private String getTitle() {
        return Stream.of(appTitle, title)
                .filter(s -> s != null && !s.trim().isEmpty())
                .collect(Collectors.joining(" - "));
    }

    public void setOnFxmlLoaded(Consumer<FXMLLoader> onFxmlLoaded) {
        this.onFxmlLoaded = onFxmlLoaded;
    }

    <T> void setOnControllerReady(Consumer<T> onControllerReady) {
        this.onFxmlLoaded = loader -> onControllerReady.accept(loader.getController());
    }

    private void setAppIcons(Stage stage) {
        if (appIcons != null && stage.getIcons().isEmpty()) {
            appIcons.stream().map(Image::new).forEach(stage.getIcons()::add);
        }
    }

    private Parent getRoot() throws IOException {
        URL url = this.getClass().getResource(fxmlFile);
        FXMLLoader loader = new FXMLLoader(url);
        if (!this.hasController(url)) {
            loader.setController(this);
        }
        Parent parent = loader.load();
        if (onFxmlLoaded != null) {
            onFxmlLoaded.accept(loader);
        }
        return parent;
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

    Stage prepareStage(Node node) throws IOException {
        return this.prepareStage(this.getRoot(), node == null ? null : this.getStage(node));
    }

    public Stage prepareStage(Stage mStage) throws IOException {
        return this.prepareStage(this.getRoot(), mStage);
    }

    Stage prepareStage() throws IOException {
        return this.prepareStage(this.getRoot(), null);
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

    public <T> Dialog<T> dialogCustom(Dialog<T> dialog) {
        this.setAppIcons((Stage) dialog.getDialogPane().getScene().getWindow());
        return dialog;
    }

    private Optional<ButtonType> dialog(Alert.AlertType type, String... msjs) {
        String txt = Stream.of(msjs).collect(Collectors.joining("\n"));
        Alert alert = new Alert(type, txt);
        alert.setHeaderText(null);
        return this.dialogCustom(alert).showAndWait();
    }

    void dialogInfo(String... msjs) {
        this.dialog(Alert.AlertType.INFORMATION, msjs);
    }

    void dialogWarning(String... msjs) {
        this.dialog(Alert.AlertType.WARNING, msjs);
    }

    Optional<ButtonType> dialogConfirm(String... msjs) {
        return this.dialog(Alert.AlertType.CONFIRMATION, msjs);
    }

    void onServiceFailed(WorkerStateEvent state) {
        Throwable e = state.getSource().getException();
        this.showExceptionAlert(e);
    }

    void handle(Runnable runnable, Consumer<Exception> onError) {
        try {
            runnable.run();
        } catch (Exception e) {
            onError.accept(e);
        }
    }

    void handle(Runnable runnable) {
        this.handle(runnable, this::showExceptionAlert);
    }

    private void showExceptionAlert(Throwable e) {
        Alert alert = alertExceptionHandler == null
                ? new ExceptionAlert(e, debugMode)
                : alertExceptionHandler.apply(e, debugMode);
        alert.setHeaderText(null);
        this.dialogCustom(alert).showAndWait();
    }

    private Stage getStage(Node node) {
        return (Stage) node.getScene().getWindow();
    }

    Stage getStage(ActionEvent e) {
        return this.getStage((Node) e.getSource());
    }

    void closeWindow() {
        this.stage.close();
    }

    public boolean isPrimaryDoubleClick(MouseEvent e) {
        return e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2;
    }
}
