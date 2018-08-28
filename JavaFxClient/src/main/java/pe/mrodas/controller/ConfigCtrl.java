package pe.mrodas.controller;

import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import lombok.Data;
import org.controlsfx.control.SegmentedButton;
import pe.mrodas.MainApp;
import pe.mrodas.entity.Root;
import pe.mrodas.entity.Tag;

import java.io.FileFilter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Data
class ConfigCtrl {
    private CollectionController parent;
    private List<Tag> categories, domains;
    private final Root root;
    private final MediaType mediaType;
    private final FileFilter fileFilter;
    private final FileChooser.ExtensionFilter extensionFilter;

    ConfigCtrl(Root root) {
        this.root = root;
        mediaType = MediaType.get(root.getMediaType());
        List<String> pointExtensions = new ArrayList<>();
        List<String> asteriskExtensions = new ArrayList<>();
        String[] extensions = mediaType.getExtensions().split(",");
        for (String extension : extensions) {
            pointExtensions.add("." + extension);
            asteriskExtensions.add("*." + extension);
        }
        fileFilter = file -> pointExtensions.stream()
                .anyMatch(extension -> file.getName().endsWith(extension));
        String name = mediaType.name();
        String description = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        extensionFilter = new FileChooser.ExtensionFilter(description, asteriskExtensions);
    }

    void checkRoot() {
        if (root == null) {
            throw new InvalidParameterException("root can't be null");
        }
        if ((root.getIdRoot() == null ? 0 : root.getIdRoot()) <= 0) {
            throw new InvalidParameterException("idRoot must be greater than zero");
        }
    }

    Path getPath(String folder) {
        return Paths.get(MainApp.getSession().getWorkingDir(), root.getName(), folder);
    }

    void buildCategoryButtons(Pane container, Consumer<Tag> onCategorySelected) {
        ToggleButton[] buttons = categories.stream().map(tag -> {
            ToggleButton toggle = new ToggleButton(tag.getName());
            toggle.setUserData(tag);
            return toggle;
        }).toArray(ToggleButton[]::new);
        SegmentedButton segButton = new SegmentedButton(buttons);
        container.getChildren().setAll(segButton);
        segButton.getToggleGroup().selectedToggleProperty().addListener((obs, oldToggle, toggle) -> {
            Tag tag = toggle == null ? null : (Tag) toggle.getUserData();
            onCategorySelected.accept(tag);
        });
    }
}
