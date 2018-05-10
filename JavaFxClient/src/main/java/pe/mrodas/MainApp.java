package pe.mrodas;

import javafx.application.Application;
import javafx.stage.Stage;
import pe.mrodas.controller.BaseController;
import pe.mrodas.controller.FXMLController;

import java.net.URL;
import java.util.stream.Stream;


public class MainApp extends Application {

    private static String workingDir = "./";

    public static String getWorkingDir(){
        return workingDir;
    }

    @Override
    public void start(Stage stage) throws Exception {
        Stream<String> icons = Stream.of("16x16", "32x32", "96x96")
                .map(s -> String.format("images/favicon-%s.png", s));
        BaseController.config("Meme App", "/styles/Styles.css", icons);
        URL url = this.getClass().getClassLoader().getResource("app.properties");
        workingDir = url == null ? "./target/" : "./";
        new FXMLController()
                .setTitle("JavaFX and Maven")
                .prepareStage(stage)
                .show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
