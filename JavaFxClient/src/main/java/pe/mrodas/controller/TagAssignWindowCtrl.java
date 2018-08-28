package pe.mrodas.controller;

import javafx.beans.property.BooleanProperty;
import javafx.concurrent.Service;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.controlsfx.control.GridView;
import pe.mrodas.entity.Root;
import pe.mrodas.entity.Tag;
import pe.mrodas.entity.TagListsToSave;
import pe.mrodas.helper.InputAutoComplete;
import pe.mrodas.helper.TagBar;
import pe.mrodas.model.RestClient;
import pe.mrodas.model.TagModel;
import pe.mrodas.worker.ServiceSaveTagList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

class TagAssignWindowCtrl {

    private final VBox vBoxCategories, vBoxTag;
    private final FlowPane flowTags;
    private VBox vBoxImageProperties;
    private TagNewWindowCtrl tagWindowCtrl;
    private TextField txtSearchTag;

    private int selectedCategoryId;
    private Consumer<Service<?>> bindService;
    private ConfigCtrl config;
    private EventHandler<ActionEvent> btnCancelOnClick;

    private final ServiceSaveTagList serviceSaveTagList = new ServiceSaveTagList();
    private final List<Integer> categoriesId = new ArrayList<>();
    private List<Tag> selectedTags;
    private List<Integer> selectedTagsId, selectedCategoriesId;
    private File selectedFile;

    TagAssignWindowCtrl(VBox vBoxCategories, VBox vBoxTag, FlowPane flowTags) {
        this.vBoxCategories = vBoxCategories;
        this.vBoxTag = vBoxTag;
        this.flowTags = flowTags;
        this.flowTags.getStylesheets().add("styles/TagBar.css");
        this.flowTags.setOnMouseClicked(event -> txtSearchTag.clear());
    }

    BooleanProperty setContainer(VBox vBoxImageProperties) {
        this.vBoxImageProperties = vBoxImageProperties;
        this.vBoxImageProperties.setVisible(false);
        return this.vBoxImageProperties.visibleProperty();
    }

    void setConfig(ConfigCtrl config, EventHandler<ActionEvent> btnCancelOnClick, Consumer<Service<?>> bindService) {
        this.config = config;
        this.btnCancelOnClick = btnCancelOnClick;
        this.bindService = bindService;
        serviceSaveTagList.setOnFailed(config.getParent()::onServiceFailed);
        for (Tag category : config.getCategories()) {
            categoriesId.add(category.getIdTag());
            CheckBox checkBox = new CheckBox(category.getName());
            checkBox.setUserData(category);
            vBoxCategories.getChildren().add(checkBox);
        }
    }

    TagAssignWindowCtrl setInputAutoCompeteTags(TextField txtSearchTag, HBox spinnerHolder) {
        this.txtSearchTag = txtSearchTag;
        InputAutoComplete<Tag> autoComplete = new InputAutoComplete<>(this.txtSearchTag, Tag::getName);
        this.txtSearchTag.setPromptText("Search tag...");
        this.txtSearchTag.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                this.txtSearchTag.clear();
            }
        });
        autoComplete.setSelectionHandler(e -> {
            TagBar.Element tagElement = this.getTagElement(e.getObject());
            flowTags.getChildren().add(tagElement);
            this.txtSearchTag.clear();
        });
        Predicate<Tag> notAlreadyInPane = tag -> {
            Integer idTag = tag.getIdTag();
            return flowTags.getChildren().stream()
                    .map(node -> (Tag) node.getUserData())
                    .noneMatch(tagInPane -> tagInPane.getIdTag().equals(idTag));
        };
        autoComplete.setSuggestionProvider(spinnerHolder, hint ->
                this.getTagList(config.getRoot(), hint, false), notAlreadyInPane);
        return this;
    }

    void setNewTagWindow(Integer idRoot, VBox vBoxNewTag, TextField txtNewTag) {
        tagWindowCtrl = new TagNewWindowCtrl(idRoot, vBoxNewTag, txtNewTag);
        tagWindowCtrl.setConfig(config.getParent()::onServiceFailed, bindService, config.getParent()::dialogWarning, tag -> {
            boolean isCategory = categoriesId.contains(tag.getIdTag());
            if (!isCategory) {
                TagBar.Element tagElement = this.getTagElement(tag);
                flowTags.getChildren().add(tagElement);
                txtSearchTag.clear();
            }
            return isCategory;
        });
        vBoxNewTag.visibleProperty().addListener((o, old, isVisible) -> vBoxImageProperties.setDisable(isVisible));
    }

    TagNewWindowCtrl getTagWindowCtrl() {
        return tagWindowCtrl;
    }

    List<Tag> getTagList(Root root, String hint, boolean includeCat) throws Exception {
        return RestClient.execute(TagModel.class, tagModel -> {
            Integer idRoot = root.getIdRoot();
            return tagModel.getTagSuggestions(idRoot, hint, includeCat);
        }).body();
    }

    void setSelectedCategoryId(Integer idTag) {
        selectedCategoryId = idTag;
    }

    void setSelectedFile(File file) {
        this.selectedFile = file;
    }

    void setVisible(boolean value) {
        vBoxImageProperties.setVisible(value);
    }

    void setSelectedTags(List<Tag> selectedTags) {
        this.selectedTags = selectedTags;
        this.selectedTagsId = new ArrayList<>();
        this.selectedCategoriesId = new ArrayList<>();
        for (Tag selectedTag : selectedTags) {
            Integer idTag = selectedTag.getIdTag();
            selectedTagsId.add(idTag);
            if (categoriesId.contains(idTag)) {
                selectedCategoriesId.add(idTag);
            }
        }
    }

    void setImageProperties(boolean isCategories) {
        if (isCategories) {
            for (Node node : vBoxCategories.getChildren()) {
                Integer idCategory = ((Tag) node.getUserData()).getIdTag();
                ((CheckBox) node).setSelected(selectedTagsId.contains(idCategory));
            }
        } else {
            HBox[] tagBoxes = selectedTags.stream()
                    .filter(tag -> !categoriesId.contains(tag.getIdTag()))
                    .map(this::getTagElement).toArray(HBox[]::new);
            flowTags.getChildren().setAll(tagBoxes);
        }
    }

    void showCategories(boolean isCategories) {
        vBoxCategories.setVisible(isCategories);
        vBoxCategories.setManaged(isCategories);
        vBoxTag.setVisible(!isCategories);
        vBoxTag.setManaged(!isCategories);
    }

    void clear(boolean isCategories) {
        if (isCategories) {
            vBoxCategories.getChildren().forEach(node -> ((CheckBox) node).setSelected(false));
        } else {
            flowTags.getChildren().clear();
        }
    }

    private TagBar.Element getTagElement(Tag tag) {
        TagBar.Element element = new TagBar.Element(tag.getName());
        element.setUserData(tag);
        return element.setOnRemoveClick(event -> flowTags.getChildren().remove(element));
    }

    void saveCategories(GridView<File> gridFiles) {
        List<Integer> idCatsToSave = new ArrayList<>();
        for (Node node : vBoxCategories.getChildren()) {
            boolean chkSelected = ((CheckBox) node).isSelected();
            if (chkSelected) {
                Integer idCategory = ((Tag) node.getUserData()).getIdTag();
                idCatsToSave.add(idCategory);
            }
        }
        if (idCatsToSave.isEmpty()) {
            config.getParent().dialogWarning("Must select at least one Category!");
        } else {
            TagListsToSave tagListsToSave = new TagListsToSave().setTags(selectedCategoriesId, idCatsToSave);
            if (tagListsToSave.noChanges()) {
                btnCancelOnClick.handle(null);
            } else {
                serviceSaveTagList.config(selectedFile, tagListsToSave);
                serviceSaveTagList.setOnSucceeded(event -> {
                    boolean currentFileInGrid = idCatsToSave.contains(selectedCategoryId);
                    if (!currentFileInGrid) {
                        gridFiles.getItems().remove(selectedFile);
                    }
                    for (Node node : vBoxCategories.getChildren()) {
                        this.updateSelectedTags(tagListsToSave, (Tag) node.getUserData());
                    }
                    btnCancelOnClick.handle(null);
                });
                bindService.accept(serviceSaveTagList);
                serviceSaveTagList.restart();
            }
        }
    }


    void saveTags(GridView<File> gridFiles, BiFunction<List<Integer>, List<Integer>, Boolean> checkImageInGrid) {
        List<Integer> idTagsToSave = new ArrayList<>();
        List<Tag> tagsToSave = new ArrayList<>();
        for (Node node : flowTags.getChildren()) {
            Tag tag = (Tag) node.getUserData();
            tagsToSave.add(tag);
            idTagsToSave.add(tag.getIdTag());
        }
        List<Integer> idTagsSelected = selectedTagsId.stream()
                .filter(id -> !categoriesId.contains(id))
                .collect(Collectors.toList());
        TagListsToSave tagListsToSave = new TagListsToSave().setTags(idTagsSelected, idTagsToSave);
        if (tagListsToSave.noChanges()) {
            btnCancelOnClick.handle(null);
        } else {
            serviceSaveTagList.config(selectedFile, tagListsToSave);
            serviceSaveTagList.setOnSucceeded(event -> {
                boolean currentFileInGrid = checkImageInGrid.apply(categoriesId, idTagsToSave);
                if (!currentFileInGrid) {
                    gridFiles.getItems().remove(selectedFile);
                }
                for (Tag tag : tagsToSave) {
                    this.updateSelectedTags(tagListsToSave, tag);
                }
                btnCancelOnClick.handle(null);
            });
            bindService.accept(serviceSaveTagList);
            serviceSaveTagList.restart();
        }
    }

    private void updateSelectedTags(TagListsToSave tagListsToSave, Tag tag) {
        int id = tag.getIdTag();
        if (tagListsToSave.getIdTagsToAdd().contains(id)) {
            selectedTags.add(tag);
        } else if (tagListsToSave.getIdTagsToDelete().contains(id)) {
            selectedTags.remove(tag);
        }
    }
}
