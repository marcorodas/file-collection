package pe.mrodas.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import lombok.Data;
import lombok.Getter;
import org.controlsfx.control.SegmentedButton;

import pe.mrodas.MainApp;
import pe.mrodas.entity.Root;
import pe.mrodas.entity.Tag;
import pe.mrodas.helper.GuiHelper;
import pe.mrodas.worker.ServiceMoveFilesTo;

public class CollectionController extends BaseController {

    @Data
    static class Config {
        private final CollectionController parent;
        private final Root root;
        private final List<String> extensions;
        private final SimpleObjectProperty<List<Tag>> tagListProperty = new SimpleObjectProperty<>();
    }

    @FXML
    private CollectionStageController collectionStageController;
    @FXML
    private CollectionImportedController collectionImportedController;
    @FXML
    private CollectionEditController collectionEditController;
    @FXML
    private CollectionTrashController collectionTrashController;

    @Getter
    private final SimpleObjectProperty<Config> configProperty = new SimpleObjectProperty<>();
    private Path rootDir;

    public CollectionController() {
        super("/fxml/Collection.fxml");
    }

    Stage prepareStage(Root root, List<String> extensions, Node node) throws IOException {
        this.checkRoot(root);
        super.<CollectionController>setOnControllerReady(ctrl -> {
            ctrl.setRootDir(root);
            Config config = new Config(ctrl, root, extensions);
            ctrl.getConfigProperty().set(config);
        });
        return super.prepareStage(node);
    }

    private void checkRoot(Root root) {
        if (root == null) {
            throw new InvalidParameterException("root can't be null");
        }
        if ((root.getIdRoot() == null ? 0 : root.getIdRoot()) <= 0) {
            throw new InvalidParameterException("idRoot must be greater than zero");
        }
    }

    private void setRootDir(Root root) {
        rootDir = Paths.get(MainApp.getSession().getWorkingDir(), root.getName());
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
        return rootDir.resolve(folder);
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

    void buildCategoryButtons(HBox btnsContainer, Consumer<Tag> onTagSelected) {
        configProperty.get().getTagListProperty().addListener((o, old, tagList) -> {
            ToggleButton[] buttons = tagList.stream().map(tag -> {
                ToggleButton toggle = new ToggleButton(tag.getName());
                toggle.setUserData(tag);
                return toggle;
            }).toArray(ToggleButton[]::new);
            SegmentedButton groupBtns = new SegmentedButton(buttons);
            btnsContainer.getChildren().setAll(groupBtns);
            groupBtns.getToggleGroup().selectedToggleProperty().addListener((obs, oldToggle, toggle) -> {
                Tag tag = toggle == null ? null : (Tag) toggle.getUserData();
                onTagSelected.accept(tag);
            });
        });
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
