package pe.mrodas;


import javafx.application.Application;
import javafx.stage.Stage;
import pe.mrodas.controller.LoginController;
import pe.mrodas.entity.Session;
import pe.mrodas.model.RestClient;

import java.util.stream.Stream;


public class MainApp extends Application {

    private static final Session SESSION = new Session();

    public static Session session() {
        return SESSION;
    }

    @Override
    public void start(Stage stage) throws Exception {
        RestClient.setBaseUrl("http://localhost:9090/file-collection/rest/");
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

}
