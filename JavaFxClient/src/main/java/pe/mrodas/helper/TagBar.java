package pe.mrodas.helper;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import lombok.Getter;
import lombok.Setter;

/**
 * https://stackoverflow.com/questions/37378973/implement-tags-bar-in-javafx
 * https://stackoverflow.com/questions/36861056/javafx-textfield-auto-suggestions
 */
public class TagBar<T> extends HBox {
    private final Function<T, String> toString;
    @Getter
    private final ObservableList<T> tags = FXCollections.observableArrayList();
    @Getter
    private final TextField inputTextField = new TextField();
    private final InputAutoComplete<T> inputAutoComplete;
    @Setter
    private Runnable onTagsUpdated;

    public TagBar(Function<T, String> toString) {
        this.toString = toString;
        getStyleClass().setAll("tag-bar");
        getStylesheets().add("styles/TagBar.css");
        tags.addListener(this::onChanged);
        inputAutoComplete = new InputAutoComplete<>(inputTextField, toString);
        inputTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (Boolean.FALSE.equals(newValue)) {
                inputTextField.clear();
            }
        });
        inputAutoComplete.setSelectionHandler(e -> {
            tags.add(e.getObject());
            inputTextField.clear();
            if (onTagsUpdated != null) {
                onTagsUpdated.run();
            }
        });
        inputTextField.setPromptText("Search by tag...");
        inputTextField.setOnKeyPressed(e -> {
            if (inputTextField.getText().isEmpty() && e.getCode() == KeyCode.BACK_SPACE && !tags.isEmpty()) {
                tags.remove(tags.size() - 1);
            } else if (e.getCode() == KeyCode.ESCAPE) {
                inputTextField.clear();
            }
        });
        inputTextField.prefHeightProperty().bind(this.heightProperty());
        HBox.setHgrow(inputTextField, Priority.ALWAYS);
        inputTextField.setBackground(null);
        getChildren().add(inputTextField);
    }

    public TagBar<T> setSuggestionList(List<T> suggestionList) {
        inputAutoComplete.setSuggestionList(suggestionList);
        return this;
    }

    public TagBar<T> setSuggestionProvider(InputAutoComplete.SuggestionProvider<T> suggestionProvider) {
        inputAutoComplete.setSuggestionProvider(this, suggestionProvider, tag -> !tags.contains(tag));
        return this;
    }

    private void onChanged(ListChangeListener.Change<? extends T> change) {
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
                    getChildren().addAll(change.getFrom(), change.getAddedSubList().stream().map(tag -> {
                        Element element = new Element(toString.apply(tag));
                        return element.setOnRemoveClick(event -> {
                            tags.remove(tag);
                            if (onTagsUpdated != null) {
                                onTagsUpdated.run();
                            }
                        });
                    }).collect(Collectors.toList()));
                }
            }
        }
    }

    public static class Element extends HBox {

        private final Button removeButton = new Button();

        public Element(String text) {
            this.getStyleClass().setAll("tag");
            removeButton.setPadding(Insets.EMPTY);
            MaterialDesignIconView iconView = new MaterialDesignIconView(MaterialDesignIcon.CLOSE_CIRCLE);
            removeButton.setGraphic(iconView);
            this.getChildren().addAll(new Text(text), removeButton);
        }

        public Element setOnRemoveClick(EventHandler<ActionEvent> onRemove) {
            removeButton.setOnAction(onRemove);
            return this;
        }
    }
}
