package pe.mrodas.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import pe.mrodas.MainApp;
import pe.mrodas.entity.Root;


public class MenuController extends BaseController {

    @FXML
    private FlowPane content;
    @FXML
    private Label lblNoButtons;

    public MenuController() {
        super("/fxml/Menu.fxml");
        super.setTitle("Menu");
    }

    @Override
    public void initialize() {
        Button[] buttons = MainApp.getSession().getRootList().stream()
                .map(this::getButton)
                .toArray(Button[]::new);
        if (buttons.length == 0) {
            content.setVisible(false);
            content.setManaged(false);
        } else {
            lblNoButtons.setVisible(false);
            lblNoButtons.setManaged(false);
            for (Button button : buttons) {
                content.getChildren().add(button);
            }
        }
    }

    private void setImageBtn(String url, Button button) {
        int size = 100;
        Image image = new Image(url, size, size, false, true, true);
        image.progressProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() == 1) {
                button.setGraphic(new ImageView(image));
            }
        });
        ProgressBar progressBar = new ProgressBar();
        progressBar.setPrefWidth(80);
        progressBar.progressProperty().bind(image.progressProperty());
        StackPane pane = new StackPane(progressBar);
        pane.setStyle("-fx-border-color: gray; -fx-border-style: dashed");
        pane.setPrefSize(size, size);
        button.setGraphic(pane);
    }

    private Button getButton(Root root) {
        Button button = new Button(root.getName());
        button.setPrefHeight(140);
        this.setImageBtn(root.getImageUrl(), button);
        button.setContentDisplay(ContentDisplay.TOP);
        button.setOnAction(event -> this.handle(() -> {
            if (MainApp.getSession().getWorkingDir() == null) {
                new WorkspaceController()
                        .setOnSaveSuccess(() -> this.getBtnAction(root))
                        .setOwner(event)
                        .prepareStage()
                        .show();
            } else {
                this.getBtnAction(root);
            }
        }));
        return button;
    }

    private void getBtnAction(Root root) throws Exception {
        Integer id = root.getIdRoot();
        switch (Item.get(id)) {
            case ANTIFUJIMORISMO:
                new CollectionController()
                        .prepareStage(root, content)
                        .show();
                break;
            default:
                super.dialogInfo("No Action Set");
        }
    }

    public enum Item {
        NONE, ANTIFUJIMORISMO, MEMES, COMPROBANTES;

        public static Item get(Integer id) {
            switch (id == null ? 0 : id) {
                case 1:
                    return ANTIFUJIMORISMO;
                case 2:
                    return MEMES;
                case 3:
                    return COMPROBANTES;
            }
            return NONE;
        }
    }
}
