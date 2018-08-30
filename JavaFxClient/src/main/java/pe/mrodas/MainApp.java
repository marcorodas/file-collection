package pe.mrodas;

import com.google.gson.Gson;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import okhttp3.ResponseBody;
import pe.mrodas.controller.LoginController;
import pe.mrodas.entity.Session;
import pe.mrodas.helper.ExceptionAlert;
import pe.mrodas.model.RestClient;
import pe.mrodas.model.RestServerException;
import pe.mrodas.rest.ApiError;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class MainApp extends Application {

    private static boolean debugMode = true;
    private static Session session = new Session();

    public static Consumer<File> openFile;

    public static void setSession(Session session) {
        MainApp.session = session;
    }

    public static Session getSession() {
        return MainApp.session;
    }

    public static boolean debugMode() {
        return debugMode;
    }

    @Override
    public void start(Stage stage) throws Exception {
        String url = this.getUrl();
        RestClient.setBaseUrl(url.endsWith("/") ? url : url.concat("/"));
        RestClient.setServerErrorHandler(this::onServerError);
        Stream<String> icons = Stream.of("16x16", "32x32", "96x96")
                .map(s -> String.format("images/favicon-%s.png", s));
        openFile = this::open;
        new LoginController()
                .setAlertExceptionHandler(this::onException)
                .setAppTitle("File Collector")
                .setAppStyle("/styles/Styles.css")
                .setAppIcons(icons)
                .prepareStage(stage).show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private String getUrl() {
        ClassLoader loader = this.getClass().getClassLoader();
        try (InputStream file = loader.getResourceAsStream("url.properties")) {
            Properties urlProperties = new Properties();
            urlProperties.load(file);
            return urlProperties.getProperty("url");
        } catch (Exception e) {
            return "http://localhost:9090/file-collection/rest/";
        }
    }

    private void open(File file) {
        this.getHostServices().showDocument(file.getAbsolutePath());
    }

    private Alert onException(Throwable e, Boolean debugMode) {
        boolean debug = Boolean.TRUE.equals(debugMode);
        if (debug) {
            Logger.getLogger("File Collector").log(Level.SEVERE, e.getClass().getName(), e);
        }
        return new ExceptionAlert(e, debug);
    }

    private RestServerException onServerError(String modelName, String url, ResponseBody body) {
        try {
            ApiError apiError = new Gson().fromJson(body.string(), ApiError.class);
            return new RestServerException(modelName, url, apiError.getMessage())
                    .setServerTrace(apiError.getTrace("pe.mrodas"));
        } catch (IOException e) {
            return new RestServerException("Error Reading Server Error:\n" + e.getMessage(), e, modelName, url);
        }
    }

}
