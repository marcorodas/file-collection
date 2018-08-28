package pe.mrodas.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Service;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;
import org.controlsfx.control.GridView;
import org.controlsfx.control.SegmentedButton;
import pe.mrodas.entity.Tag;
import pe.mrodas.helper.FileHelper;
import pe.mrodas.helper.TagBar;
import pe.mrodas.worker.*;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CollectionImportedController {

    @FXML
    public SplitPane splitPane;
    @FXML
    public GridView<File> gridFiles;
    @FXML
    public BorderPane imageContainer;
    @FXML
    public ImageView imageView;
    @FXML
    public ToolBar toolbar;
    @FXML
    public HBox categoriesButtons;
    @FXML
    public HBox tagContainer;
    @FXML
    public VBox topPane;
    @FXML
    public Label lblTotal;
    @FXML
    public VBox vBoxImageProperties;
    @FXML
    public Label lblTitle;
    @FXML
    public VBox vBoxCategories;
    @FXML
    public VBox vBoxTag;
    @FXML
    public TextField txtSearchTag;
    @FXML
    public FlowPane flowTags;
    @FXML
    public SegmentedButton segButton;
    @FXML
    public ToggleButton btnCategories;
    @FXML
    public ToggleButton btnTag;
    @FXML
    public ToolBar topToolBar;
    @FXML
    public CheckBox chkUntagged;
    @FXML
    public HBox spinnerHolder;
    @FXML
    public VBox vBoxNewTag;
    @FXML
    public TextField txtNewTag;
    @FXML
    public Label lblSearch;
    @FXML
    public Button btnFilter;
    @FXML
    private ProgressController progressController;

    @Getter
    private final SimpleObjectProperty<ConfigCtrl> configProperty = new SimpleObjectProperty<>();
    static final String PATH = "cache";
    private ServiceGetFileNames serviceGetFileNames;
    private ServiceReadFiles serviceReadFiles, serviceGetExistingFiles;
    private ServiceGetMissingFiles serviceGetMissingFiles;
    private ServiceDeleteFileFromDB serviceDeleteFileFromDB = new ServiceDeleteFileFromDB();
    private ServiceMoveFilesTo serviceMoveFilesToEdit, serviceMoveFilesToTrash;
    private CollectionController parent;
    private TagBar<Tag> tagBar;
    private Tag selectedCategory;

    private TagImageViewCtrl tagImageViewCtrl;
    private TagAssignWindowCtrl tagAssignWinCtrl;

    public void initialize() {
        configProperty.addListener((o, old, config) -> {
            parent = config.getParent();
            config.buildCategoryButtons(categoriesButtons, this::onCategoryIsSelected);
            tagImageViewCtrl.setConfig(config, this::btnCancelOnClick, this::bindService)
                    .setInputAutoCompeteTags(txtSearchTag, spinnerHolder)
                    .setNewTagWindow(config.getRoot().getIdRoot(), vBoxNewTag, txtNewTag);
            this.setServices();
            tagBar = new TagBar<>(Tag::getName).setSuggestionProvider(hint -> {
                List<Tag> tags = tagAssignWinCtrl.getTagList(config.getRoot(), hint, true);
                if (selectedCategory != null) {
                    return tags == null ? null : tags.stream()
                            .filter(tag -> !tag.getIdTag().equals(selectedCategory.getIdTag()))
                            .collect(Collectors.toList());
                }
                return tags;
            });
            tagContainer.getChildren().add(1, tagBar);
            tagBar.setOnTagsUpdated(this::onTagsUpdated);
        });
        tagAssignWinCtrl = new TagAssignWindowCtrl(vBoxCategories, vBoxTag, flowTags);
        tagAssignWinCtrl.setContainer(vBoxImageProperties).addListener((observable, oldValue, isVisible) -> {
            topToolBar.setDisable(isVisible);
            tagContainer.setDisable(isVisible);
            gridFiles.setDisable(isVisible);
            toolbar.setDisable(isVisible);
        });
        tagImageViewCtrl = new TagImageViewCtrl(tagAssignWinCtrl, imageView, toolbar, (file, imageView) -> {
            double height = gridFiles.getHeight() - toolbar.getHeight();
            return CollectionController.setImageView(imageView, file, height, splitPane);
        });
        segButton.getToggleGroup().selectedToggleProperty().addListener((o, old, selected) -> {
            tagAssignWinCtrl.setVisible(selected != null);
            if (selected != null) {
                ToggleButton button = (ToggleButton) selected;
                lblTitle.setText(button.getText());
                txtSearchTag.clear();
                boolean isCategory = btnCategories.equals(selected);
                if (!isCategory) {
                    txtNewTag.requestFocus();
                }
                tagAssignWinCtrl.showCategories(isCategory);
                tagImageViewCtrl.onSelectSegmentedBtn(isCategory);
            }
        });
        chkUntagged.selectedProperty().addListener((observable, oldValue, isChecked) -> this.onTagsUpdated());
        gridFiles.setCellFactory(tagImageViewCtrl::getCellFactory);
    }

    private void onTagsUpdated() {
        boolean isChecked = chkUntagged.isSelected();
        tagBar.setDisable(isChecked);
        lblSearch.setDisable(isChecked);
        btnFilter.setDisable(isChecked);
        if (isChecked) {
            tagBar.setOpacity(0.4);
            serviceGetFileNames.setCategoryId(selectedCategory.getIdTag());
            this.bindService(serviceGetFileNames);
            serviceGetFileNames.restart();
        } else {
            tagBar.setOpacity(1);
            List<Integer> tagsId = tagBar.getTags().stream()
                    .map(Tag::getIdTag)
                    .collect(Collectors.toList());
            tagsId.add(selectedCategory.getIdTag());
            this.serviceGetFileFilterRestart(tagsId);
        }
    }

    private void onCategoryIsSelected(Tag category) {
        tagContainer.setDisable(category == null);
        tagBar.getTags().clear();
        gridFiles.getItems().clear();
        chkUntagged.setSelected(false);
        if (category != null) {
            selectedCategory = category;
            tagImageViewCtrl.setSelectedCategory(category);
            List<Integer> tagsId = Collections.singletonList(category.getIdTag());
            this.serviceGetFileFilterRestart(tagsId);
        }
    }

    private void serviceGetFileFilterRestart(List<Integer> tagsId) {
        serviceGetFileNames.setTagsId(tagsId);
        this.bindService(serviceGetFileNames);
        serviceGetFileNames.restart();
    }

    private void setServices() {
        serviceGetFileNames = new ServiceGetFileNames();
        serviceGetFileNames.setOnSucceeded(event -> {
            List<String> fileList = serviceGetFileNames.getValue();
            serviceReadFiles.setFilter(file -> {
                String fileName = file.getName();
                return fileList.contains(fileName);
            });
            this.bindService(serviceReadFiles);
            serviceReadFiles.restart();
        });
        this.bindService(serviceGetFileNames);
        serviceGetFileNames.setOnFailed(parent::onServiceFailed);
        serviceGetFileNames.setOnRunning(event -> parent.setNumFiles(lblTotal, 0));
        Path currentPath = parent.getPath(PATH);
        serviceReadFiles = new ServiceReadFiles(currentPath, null);
        serviceReadFiles.setOnSucceeded(event -> {
            List<File> files = serviceReadFiles.getValue();
            parent.setNumFiles(lblTotal, files.size());
            gridFiles.setItems(FXCollections.observableArrayList(files));
        });
        serviceReadFiles.setOnFailed(parent::onServiceFailed);
        serviceGetExistingFiles = new ServiceReadFiles(currentPath, null);
        serviceGetExistingFiles.setOnFailed(parent::onServiceFailed);
        serviceGetExistingFiles.setOnSucceeded(e -> {
            List<String> md5List = serviceGetExistingFiles.getValue().stream()
                    .map(file -> FileHelper.getName(file.getName()))
                    .collect(Collectors.toList());
            serviceGetMissingFiles.setMd5List(md5List);
            this.bindService(serviceGetMissingFiles);
            serviceGetMissingFiles.restart();
        });
        Integer idRoot = parent.getConfigProperty().get().getRoot().getIdRoot();
        serviceGetMissingFiles = new ServiceGetMissingFiles(currentPath, idRoot);
        serviceGetMissingFiles.setOnFailed(parent::onServiceFailed);
        serviceGetMissingFiles.setOnRunning(event -> parent.setNumFiles(lblTotal, 0));
        serviceGetMissingFiles.setOnSucceeded(event -> {
            this.bindService(serviceReadFiles);
            serviceReadFiles.restart();
        });
        serviceMoveFilesToEdit = parent.getServiceMoveFilesTo(CollectionEditController.PATH);
        serviceMoveFilesToEdit.setOnSucceeded(e -> {
            parent.addToGrid(CollectionEditController.PATH, (ServiceMoveFilesTo) e.getSource());
        });
        serviceMoveFilesToTrash = parent.getServiceMoveFilesTo(CollectionTrashController.PATH);
        serviceMoveFilesToTrash.setOnSucceeded(e -> {
            parent.addToGrid(CollectionTrashController.PATH, (ServiceMoveFilesTo) e.getSource());
        });
    }

    private void bindService(Service<?> service) {
        if (service.getOnFailed() == null) {
            service.setOnFailed(parent::onServiceFailed);
        }
        topPane.disableProperty().bind(service.runningProperty());
        splitPane.disableProperty().bind(service.runningProperty());
        progressController.bindService(service);
    }

    void updateImportedFilesGrid(int idCategory) {
        if (selectedCategory != null && selectedCategory.getIdTag() == idCategory) {
            this.onTagsUpdated();
        }
    }

    @FXML
    public void btnSaveOnClick(ActionEvent e) {
        Toggle selected = segButton.getToggleGroup().getSelectedToggle();
        if (selected == null) {
            return;
        }
        if (btnCategories.equals(selected)) {
            tagAssignWinCtrl.saveCategories(gridFiles);
        } else {
            tagAssignWinCtrl.saveTags(gridFiles, (categoriesId, idTagsToSave) -> {
                List<Integer> tagBarIds = tagBar.getTags().stream()
                        .map(Tag::getIdTag).filter(id -> !categoriesId.contains(id))
                        .collect(Collectors.toList());
                return idTagsToSave.isEmpty()
                        ? (chkUntagged.isSelected() || tagBarIds.isEmpty())
                        : idTagsToSave.containsAll(tagBarIds);
            });
        }
    }

    @FXML
    public void btnCancelOnClick(ActionEvent e) {
        segButton.getButtons().forEach(btn -> {
            if (btn.isSelected()) {
                btn.setSelected(false);
            }
        });
    }

    @FXML
    public void btnNewTagOnClick(ActionEvent e) {
        txtNewTag.setText(txtSearchTag.getText());
        vBoxNewTag.setVisible(true);
    }

    @FXML
    public void btnSaveNewTagOnClick(ActionEvent e) {
        tagAssignWinCtrl.getTagWindowCtrl().save();
    }

    @FXML
    public void btnCancelNewTagOnClick(ActionEvent e) {
        vBoxNewTag.setVisible(false);
    }

    @FXML
    public void btnNoFilterOnClick(ActionEvent e) {
        tagBar.getTags().clear();
        this.onTagsUpdated();
    }

    @FXML
    public void btnMissingOnClick(ActionEvent e) {
        this.bindService(serviceGetExistingFiles);
        serviceGetExistingFiles.restart();
    }

    private void dbDeleteAndMoveFile(ServiceMoveFilesTo serviceMoveFilesTo) {
        File selectedFile = tagImageViewCtrl.getSelectedFile();
        String name = FileHelper.getName(selectedFile.getName());
        serviceDeleteFileFromDB.setMd5(name);
        serviceDeleteFileFromDB.setOnSucceeded(e -> {
            tagImageViewCtrl.clean();
            gridFiles.getItems().remove(selectedFile);
            serviceMoveFilesTo.setSourceFile(selectedFile);
            this.bindService(serviceMoveFilesTo);
            serviceMoveFilesTo.restart();
        });
        this.bindService(serviceDeleteFileFromDB);
        serviceDeleteFileFromDB.restart();
    }

    @FXML
    private void btnEditOnClick(ActionEvent e) {
        this.dbDeleteAndMoveFile(serviceMoveFilesToEdit);
    }

    @FXML
    private void btnTrashOnClick(ActionEvent event) {
        this.dbDeleteAndMoveFile(serviceMoveFilesToTrash);
    }

    void addToGrid(File file) {
        gridFiles.getItems().add(file);
    }
}
