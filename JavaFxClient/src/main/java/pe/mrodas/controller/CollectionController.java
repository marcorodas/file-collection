package pe.mrodas.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import lombok.Data;
import lombok.Getter;
import org.controlsfx.control.GridCell;
import org.controlsfx.control.SegmentedButton;
import pe.mrodas.entity.Root;
import pe.mrodas.entity.Tag;
import pe.mrodas.helper.GuiHelper;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.function.Consumer;

public class CollectionController extends BaseController {

    @FXML
    private CollectionStageController collectionStageController;
    @FXML
    private CollectionImportedController collectionImportedController;

    @Getter
    private final SimpleObjectProperty<Config> configProperty = new SimpleObjectProperty<>();

    public CollectionController() {
        super("/fxml/Collection.fxml");
    }

    Stage prepareStage(Root root, List<String> extensions, Node node) throws IOException {
        if (root == null) {
            throw new InvalidParameterException("root can't be null");
        }
        if ((root.getIdRoot() == null ? 0 : root.getIdRoot()) <= 0) {
            throw new InvalidParameterException("idRoot must be greater than zero");
        }
        super.<CollectionController>setOnControllerReady(ctrl -> {
            Config config = new Config(ctrl, root, extensions);
            ctrl.getConfigProperty().set(config);
        });
        return super.prepareStage(node);
    }

    @Override
    public void initialize() {
        configProperty.addListener((o, old, config) -> {
            collectionStageController.getConfigProperty().set(config);
            collectionImportedController.getConfigProperty().set(config);
        });
    }

    @Data
    static class Config {
        private final CollectionController parent;
        private final Root root;
        private final List<String> extensions;
        private final SimpleObjectProperty<List<Tag>> tagListProperty = new SimpleObjectProperty<>();

        void buildTagButtons(HBox btnsContainer, Consumer<Tag> onTagSelected) {
            tagListProperty.addListener((o, old, tagList) -> {
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

    static class GridCellImage extends GridCell<File> {
        private final Consumer<File> onMouseSingleClick, onMouseDoubleClick;

        GridCellImage(Consumer<File> onMouseSingleClick, Consumer<File> onMouseDoubleClick) {
            this.onMouseSingleClick = onMouseSingleClick;
            this.onMouseDoubleClick = onMouseDoubleClick;
            this.setOnMouseClicked(this::onMouseClick);
        }

        void onMouseClick(MouseEvent e) {
            int clickCount = e.getClickCount();
            if (clickCount > 0) {
                File item = this.getItem();
                if (clickCount == 1) {
                    onMouseSingleClick.accept(item);
                } else if (onMouseDoubleClick != null) {
                    onMouseDoubleClick.accept(item);
                }
            }
        }

        @Override
        protected void updateItem(File file, boolean empty) {
            super.updateItem(file, empty);
            if (!empty && file != null) {
                Image image = GuiHelper.getImageFromFile(file, 100);
                if (image != null) {
                    this.setGraphic(new ImageView(image));
                }
            }
        }
    }
}
