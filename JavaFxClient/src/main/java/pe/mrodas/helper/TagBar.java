package pe.mrodas.helper;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * https://stackoverflow.com/questions/37378973/implement-tags-bar-in-javafx
 */
public class TagBar extends HBox {
    private final ObservableList<String> tags;
    private final TextField inputTextField;

    public ObservableList<String> getTags() {
        return tags;
    }

    public void add(String s) {
        if (!tags.contains(s)) {
            tags.add(s);
        }
    }

    public TagBar() {
        getStyleClass().setAll("tag-bar");
        getStylesheets().add("styles/TagBar.css");
        tags = FXCollections.observableArrayList();
        inputTextField = new TextField();
        inputTextField.setPromptText("Search by tag...");
        inputTextField.setOnAction(evt -> {
            String text = inputTextField.getText().trim();
            if (!text.isEmpty() && !tags.contains(text)) {
                tags.add(text);
                inputTextField.clear();
            }
        });

        inputTextField.prefHeightProperty().bind(this.heightProperty());
        HBox.setHgrow(inputTextField, Priority.ALWAYS);
        inputTextField.setBackground(null);

        tags.addListener((ListChangeListener.Change<? extends String> change) -> {
            while (change.next()) {
                if (change.wasPermutated()) {
                    ArrayList<Node> newSublist = new ArrayList<>(change.getTo() - change.getFrom());
                    for (int i = change.getFrom(), end = change.getTo(); i < end; i++) {
                        newSublist.add(null);
                    }
                    for (int i = change.getFrom(), end = change.getTo(); i < end; i++) {
                        newSublist.set(change.getPermutation(i), getChildren().get(i));
                    }
                    getChildren().subList(change.getFrom(), change.getTo()).clear();
                    getChildren().addAll(change.getFrom(), newSublist);
                } else {
                    if (change.wasRemoved()) {
                        getChildren().subList(change.getFrom(), change.getFrom() + change.getRemovedSize()).clear();
                    }
                    if (change.wasAdded()) {
                        getChildren().addAll(change.getFrom(), change.getAddedSubList().stream().map(Tag::new).collect(Collectors.toList()));
                    }
                }
            }
        });
        getChildren().add(inputTextField);
    }

    private class Tag extends HBox {

        public Tag(String tag) {
            getStyleClass().setAll("tag");
            Button removeButton = new Button();
            removeButton.setPadding(Insets.EMPTY);
            MaterialDesignIconView iconView = new MaterialDesignIconView(MaterialDesignIcon.CLOSE_CIRCLE);
            removeButton.setGraphic(iconView);
            removeButton.setOnAction((evt) -> tags.remove(tag));
            Text text = new Text(tag);
            getChildren().addAll(text, removeButton);
        }
    }
}
