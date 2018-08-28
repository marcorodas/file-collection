package pe.mrodas.controller;


import javafx.stage.Stage;

public class WorkspaceDialogTest extends TestBase{

    public WorkspaceDialogTest() {
        super(240);
    }

    @Override
    public void startTest(Stage stage) {
        new WorkspaceDialog().showAndWait().ifPresent(System.out::println);
    }
}