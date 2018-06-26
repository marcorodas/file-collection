package pe.mrodas;

import com.google.gson.Gson;
import javafx.application.Application;
import javafx.stage.Stage;
import pe.mrodas.controller.LoginController;
import pe.mrodas.entity.Session;
import pe.mrodas.model.RestClient;
import pe.mrodas.rest.ApiError;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;


public class MainApp extends Application {

    private static final Session SESSION = new Session();

    public static Session session() {
        return SESSION;
    }

    @Override
    public void start(Stage stage) throws Exception {
        RestClient.setBaseUrl("http://localhost:9090/file-collection/rest/");
        RestClient.setErrorHandler(info -> MainApp.onError(info, true));
        Stream<String> icons = Stream.of("16x16", "32x32", "96x96")
                .map(s -> String.format("images/favicon-%s.png", s));
        new LoginController()
                .setAppTitle("File Collector")
                .setAppStyle("/styles/Styles.css")
                .setAppIcons(icons)
                .prepareStage(stage).show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static String onError(RestClient.ErrorInfo info, boolean showLog) {
        ApiError apiError = new Gson().fromJson(info.getBodyResponse(), ApiError.class);
        if (showLog && apiError.getTrace() != null) {
            String logTemplate = "%s\n\tUrl: %s\n\t%s";
            String log = String.format(logTemplate, info.getModelClass(), info.getUrl(), apiError.getTrace());
            Logger.getLogger("File Collector").log(Level.SEVERE, log);
        }
        return apiError.getMessage();
    }

}
