package pe.mrodas.controller;


import javafx.stage.Stage;

public class GalleryControllerTest extends TestBase {

    public GalleryControllerTest() {
        super(240);
    }

    @Override
    public void startTest(Stage stage) throws Exception {
        new GalleryController(menuEnum).prepareStage(new Stage()).show();
    }
}