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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class MainApp extends Application {

    private static boolean debugMode = true;
    private static Session session;

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
        RestClient.setBaseUrl("http://localhost:9090/file-collection/rest/");
        RestClient.setServerErrorHandler(this::onServerError);
        Stream<String> icons = Stream.of("16x16", "32x32", "96x96")
                .map(s -> String.format("images/favicon-%s.png", s));
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

    private Alert onException(Throwable e, Boolean debugMode) {
        boolean debug = Boolean.TRUE.equals(debugMode);
        if (debug) {
            if (e instanceof RestServerException) {
                ((RestServerException) e).setPackageFilter("pe.mrodas");
            }
            Logger.getLogger("File Collector").log(Level.SEVERE, e.getClass().getName(), e);
        }
        return new ExceptionAlert(e, debug);
    }

    private RestServerException onServerError(String modelName, String url, ResponseBody body) {
        try {
            ApiError apiError = new Gson().fromJson(body.string(), ApiError.class);
            return new RestServerException(modelName, url, apiError.getMessage())
                    .setServerTrace(apiError.getTrace());
        } catch (IOException e) {
            return new RestServerException("Error Reading Server Error:\n" + e.getMessage(), e, modelName, url);
        }
    }

}
