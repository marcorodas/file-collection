package pe.mrodas.controller;

import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import pe.mrodas.helper.JFXValidator;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class WorkspaceDialog extends Dialog<String> {

    private final JFXTextField txtWorkspace = new JFXTextField();

    WorkspaceDialog() {
        super.setTitle("Select a Workspace");
        super.setHeaderText("Choose a Workspace Folder");
        Label label = new Label();
        label.getStyleClass().addAll("alert", "warning", "dialog-pane");
        super.setGraphic(label);
        this.buildContent();
        this.buildButtons();
        Platform.runLater(txtWorkspace::requestFocus);
    }

    private void buildButtons() {
        ButtonType btnSave = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        super.getDialogPane().getButtonTypes().addAll(btnSave, ButtonType.CANCEL);
        Node buttonSave = super.getDialogPane().lookupButton(btnSave);
        buttonSave.setDisable(true);
        buttonSave.addEventFilter(ActionEvent.ACTION, event -> {
            if (!txtWorkspace.validate()) {
                event.consume();
            }
        });
        txtWorkspace.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean isEmpty = newValue.trim().isEmpty();
            buttonSave.setDisable(isEmpty);
        });
        super.setResultConverter(btn -> {
            if (btn == btnSave) {
                return txtWorkspace.getText().trim();
            }
            return null;
        });
    }

    private void buildContent() {
        txtWorkspace.setLabelFloat(true);
        txtWorkspace.setMinWidth(350);
        txtWorkspace.setPromptText("Workspace Folder");
        txtWorkspace.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                this.btnBrowseOnClick(null);
            }
        });
        txtWorkspace.getValidators().add(new JFXValidator.Directory());
        VBox.setMargin(txtWorkspace, new Insets(15, 0, 0, 0));
        Button button = new Button("Browse...");
        button.setPrefWidth(80);
        button.setOnAction(this::btnBrowseOnClick);
        HBox btnContainer = new HBox(button);
        btnContainer.setAlignment(Pos.CENTER_RIGHT);
        VBox vBox = new VBox(txtWorkspace, btnContainer);
        vBox.setSpacing(20);
        vBox.setPadding(new Insets(5, 10, 25, 10));
        super.getDialogPane().setContent(vBox);
    }

    private void btnBrowseOnClick(ActionEvent e) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Workspace Selection");
        if (!txtWorkspace.getText().isEmpty()) {
            Path path = Paths.get(txtWorkspace.getText());
            if (Files.isDirectory(path)) {
                chooser.setInitialDirectory(path.toFile());
            }
        }
        Window window = super.getDialogPane().getScene().getWindow();
        File file = chooser.showDialog(window);
        if (file != null) {
            txtWorkspace.setText(file.getAbsolutePath());
            txtWorkspace.validate();
        }
    }

}
