package pe.mrodas.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import lombok.Getter;
import pe.mrodas.helper.GuiHelper;
import pe.mrodas.worker.ServiceMoveFilesTo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CollectionController extends BaseController {

    @FXML
    private CollectionStageController collectionStageController;
    @FXML
    private CollectionImportedController collectionImportedController;
    @FXML
    private CollectionEditController collectionEditController;
    @FXML
    private CollectionTrashController collectionTrashController;

    @Getter
    private final SimpleObjectProperty<ConfigCtrl> configProperty = new SimpleObjectProperty<>();

    public CollectionController() {
        super("/fxml/Collection.fxml");
    }

    Stage prepareStage(ConfigCtrl config, Node node) throws IOException {
        config.checkRoot();
        super.<CollectionController>setOnControllerReady(ctrl -> {
            config.setParent(ctrl);
            ctrl.getConfigProperty().set(config);
        });
        return super.prepareStage(node);
    }

    @Override
    public void initialize() {
        configProperty.addListener((o, old, config) -> {
            collectionStageController.getConfigProperty().set(config);
            collectionImportedController.getConfigProperty().set(config);
            collectionEditController.getConfigProperty().set(config);
            collectionTrashController.getConfigProperty().set(config);
        });
    }

    Path getPath(String folder) {
        return configProperty.get().getPath(folder);
    }

    void setNumFiles(Label label, int size) {
        String lbl = String.format("%d file", size);
        label.setText(size == 1 ? lbl : lbl.concat("s"));
    }

    ServiceMoveFilesTo getServiceMoveFilesTo(String targetPath) {
        ServiceMoveFilesTo serviceMoveFilesTo = new ServiceMoveFilesTo(this.getPath(targetPath));
        if (CollectionEditController.PATH.equals(targetPath)) {
            serviceMoveFilesTo.setFileNameBuilder(filePath -> {
                String pattern = "yyyymmddhhmmss";
                return new SimpleDateFormat(pattern).format(new Date());
            });
        }
        return serviceMoveFilesTo;
    }

    void addToGrid(String targetPath, ServiceMoveFilesTo service) {
        File file = service.getValue().get(0).toFile();
        switch (targetPath) {
            case CollectionStageController.PATH:
                collectionStageController.addToGrid(file);
                break;
            case CollectionImportedController.PATH:
                collectionImportedController.addToGrid(file);
                break;
            case CollectionEditController.PATH:
                collectionEditController.addToGrid(file);
                break;
            case CollectionTrashController.PATH:
                collectionTrashController.addToGrid(file);
                break;
        }
    }

    void updateImportedFilesGrid(int idCategory) {
        collectionImportedController.updateImportedFilesGrid(idCategory);
    }

    static boolean setImageView(ImageView imageView, File file, double height, SplitPane splitPane) {
        Image image = GuiHelper.getImageFromFile(file, null);
        if (image == null) {
            return false;
        }
        imageView.setImage(image);
        imageView.fitWidthProperty().unbind();
        DoubleBinding dividerWidth = getDividerWidth(splitPane, image, height - 10d);
        imageView.fitWidthProperty().bind(dividerWidth);
        return true;
    }

    private static DoubleBinding getDividerWidth(SplitPane splitPane, Image image, double maxHeight) {
        DoubleBinding splitWidth = splitPane.widthProperty().subtract(10d);
        DoubleProperty splitPercentage = splitPane.getDividers().get(0).positionProperty();
        DoubleBinding dividerWidth = Bindings.subtract(1d, splitPercentage).multiply(splitWidth);
        return Bindings.min(maxHeight * image.getWidth() / image.getHeight(), dividerWidth);
    }

}
