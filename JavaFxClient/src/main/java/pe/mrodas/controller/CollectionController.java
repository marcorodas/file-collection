package pe.mrodas.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Stage;
import lombok.Getter;
import pe.mrodas.entity.Root;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.List;

public class CollectionController extends BaseController {

    @FXML
    private CollectionStageController collectionStageController;

    @Getter
    private final SimpleObjectProperty<Root> rootProperty = new SimpleObjectProperty<>();
    @Getter
    private final SimpleObjectProperty<List<String>> extensionsProperty = new SimpleObjectProperty<>();

    public CollectionController() {
        super("/fxml/Collection.fxml");
    }

    Stage prepareStage(Root root, List<String> extensions, Node node) throws IOException {
        if (root == null) {
            throw new InvalidParameterException("root can't be null");
        }
        super.<CollectionController>setOnControllerReady(ctrl -> {
            ctrl.getExtensionsProperty().set(extensions);
            ctrl.getRootProperty().set(root);
        });
        return super.prepareStage(node);
    }

    @Override
    public void initialize() {
        extensionsProperty.addListener((o, old, extensions) -> collectionStageController.setExtensions(extensions));
        rootProperty.addListener((o, old, root) -> collectionStageController.setRoot(root));
    }
}
