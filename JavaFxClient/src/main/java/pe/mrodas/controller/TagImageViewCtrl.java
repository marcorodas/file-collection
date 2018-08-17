package pe.mrodas.controller;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import java.io.File;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;
import org.controlsfx.control.PopOver;

import pe.mrodas.entity.Tag;
import pe.mrodas.helper.FileHelper;
import pe.mrodas.model.RestClient;
import pe.mrodas.model.TagModel;

class TagImageViewCtrl {
    private final TagAssignWindowCtrl assignTagWinCtrl;
    private final ImageView imageView;
    private final BiFunction<File, ImageView, Boolean> setFileToImageView;
    private final ToolBar toolbar;
    private final Service<List<Tag>> serviceGetTags = new Service<List<Tag>>() {
        @Override
        protected Task<List<Tag>> createTask() {
            return new Task<List<Tag>>() {
                @Override
                protected List<Tag> call() throws Exception {
                    super.updateMessage("Getting Tags...");
                    return RestClient.execute(TagModel.class, tagModel -> tagModel.getFileTags(md5)).body();
                }
            };
        }
    };
    private String md5;
    private File selectedFile;
    private List<Tag> selectedTags;

    private Consumer<Service<?>> bindService;

    TagImageViewCtrl(TagAssignWindowCtrl assignTagWinCtrl, ImageView imageView, ToolBar toolbar, BiFunction<File, ImageView, Boolean> setFileToImageView) {
        this.assignTagWinCtrl = assignTagWinCtrl;
        this.imageView = imageView;
        this.toolbar = toolbar;
        this.setFileToImageView = setFileToImageView;
    }

    TagAssignWindowCtrl setConfig(CollectionController.Config config, EventHandler<ActionEvent> btnCancelOnClick, Consumer<Service<?>> bindService) {
        this.bindService = bindService;
        serviceGetTags.setOnFailed(config.getParent()::onServiceFailed);
        assignTagWinCtrl.setConfig(config, btnCancelOnClick, bindService);
        return assignTagWinCtrl;
    }

    void setSelectedCategory(Tag category) {
        imageView.setImage(null);
        toolbar.setDisable(true);
        assignTagWinCtrl.setSelectedCategoryId(category.getIdTag());
    }

    GridCell<File> getCellFactory(GridView<File> param) {
        return new CollectionController.GridCellImage(this::onImageSingleClick, this::onImageDoubleClick);
    }

    private void onImageSingleClick(File file) {
        if (!file.equals(selectedFile)) {
            if (setFileToImageView.apply(file, imageView)) {
                toolbar.setDisable(false);
                selectedFile = file;
                assignTagWinCtrl.setSelectedFile(file);
                selectedTags = null;
            }
        }
    }

    private void onImageDoubleClick(File file, MouseEvent e) {
        ClipboardContent content = new ClipboardContent();
        content.putString(file.getAbsolutePath());
        Clipboard.getSystemClipboard().setContent(content);
        Label text = new Label("Image Path Copied!");
        VBox box = new VBox(text);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(0, 20, 0, 20));
        PopOver popOver = new PopOver(box);
        popOver.setDetachable(false);
        popOver.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
        popOver.show((Node) e.getSource(), -2);
    }

    void onSelectSegmentedBtn(boolean isBtnCategories) {
        if (selectedTags == null) {
            md5 = FileHelper.getName(selectedFile.getName());
            assignTagWinCtrl.clear(isBtnCategories);
            serviceGetTags.setOnSucceeded(event -> {
                selectedTags = serviceGetTags.getValue();
                assignTagWinCtrl.setSelectedTags(selectedTags);
                assignTagWinCtrl.setImageProperties(isBtnCategories);
            });
            bindService.accept(serviceGetTags);
            serviceGetTags.restart();
        } else {
            assignTagWinCtrl.setImageProperties(isBtnCategories);
        }
    }
}
