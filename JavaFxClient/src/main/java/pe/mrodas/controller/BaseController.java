package pe.mrodas.controller;

import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Dimension2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import pe.mrodas.helper.ExceptionAlert;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private Alert setAlertIcons(Alert alert) {
        this.setAppIcons((Stage) alert.getDialogPane().getScene().getWindow());
        return alert;
    }

    void infoAlert(String... msjs) {
        String txt = Stream.of(msjs).collect(Collectors.joining(""));
        Alert alert = new Alert(Alert.AlertType.INFORMATION, txt);
        this.setAlertIcons(alert).showAndWait();
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
        Alert exceptionAlert = alertExceptionHandler == null
                ? new ExceptionAlert(e, debugMode)
                : alertExceptionHandler.apply(e, debugMode);
        this.setAlertIcons(exceptionAlert).showAndWait();
    }

    public boolean isPrimaryDoubleClick(MouseEvent e) {
        return e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2;
    }
}
