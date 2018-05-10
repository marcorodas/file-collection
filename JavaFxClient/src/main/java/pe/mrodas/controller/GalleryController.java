package pe.mrodas.controller;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import pe.mrodas.entity.FileItem;
import pe.mrodas.entity.Tag;
import pe.mrodas.helper.TaskGetImages;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GalleryController extends BaseController {

    public TextField txtSearchTag;
    public Label lblSelectedTags;

    private List<FileItem> fileItems;
    private List<Tag> tagList = new ArrayList<>();

    GalleryController() {
        super("fxml/Gallery.fxml", true);

        super.showProgressDialog("Working Directory", new TaskGetImages(), this::onTaskGetImagesSucess);
    }

    private void onTaskGetImagesSucess(List<FileItem> fileItems) {
        this.fileItems = fileItems;
    }

    @Override
    public void initialize() {

    }

    private void updateTagsLabel() {
        String lblTags = tagList.isEmpty() ? "None" : tagList.stream().map(Tag::getName)
                .collect(Collectors.joining("; "));
        lblSelectedTags.setText("Selected tags: " + lblTags);
    }

    public void btnClearOnClick(ActionEvent actionEvent) {
    }

    public void btnNoTagsOnClick(ActionEvent actionEvent) {
    }

    public void btnMenuOnClick(ActionEvent actionEvent) {

    }
}
