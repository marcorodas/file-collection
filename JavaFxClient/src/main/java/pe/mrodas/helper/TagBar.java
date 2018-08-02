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
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import lombok.Getter;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * https://stackoverflow.com/questions/37378973/implement-tags-bar-in-javafx
 */
public class TagBar extends HBox {
    @Getter
    private final ObservableList<String> tags = FXCollections.observableArrayList();
    @Getter
    private final TextField inputTextField = new TextField();

    public TagBar() {
        getStyleClass().setAll("tag-bar");
        getStylesheets().add("styles/TagBar.css");
        tags.addListener(this::onChanged);
        inputTextField.setPromptText("Search by tag...");
        inputTextField.setOnAction(e -> {
            String text = inputTextField.getText().trim();
            if (!text.isEmpty() && !tags.contains(text)) {
                tags.add(text);
                inputTextField.clear();
            }
        });
        inputTextField.setOnKeyPressed(e -> {
            if (inputTextField.getText().isEmpty() && e.getCode() == KeyCode.BACK_SPACE && !tags.isEmpty()) {
                tags.remove(tags.size() - 1);
            } else if (e.getCode() == KeyCode.ESCAPE) {
                inputTextField.clear();
            }
        });
        inputTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (Boolean.FALSE.equals(newValue)) {
                inputTextField.clear();
            }
        });
        inputTextField.prefHeightProperty().bind(this.heightProperty());
        HBox.setHgrow(inputTextField, Priority.ALWAYS);
        inputTextField.setBackground(null);
        getChildren().add(inputTextField);
    }

    private void onChanged(ListChangeListener.Change<? extends String> change) {
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
    }

    public void add(String s) {
        if (!tags.contains(s)) {
            tags.add(s);
        }
    }

    public void setAutoCompletion(Function<String, List<String>> hintToSuggestionList) {
        this.setAutoCompletion(hintToSuggestionList, null);
    }

    public void setAutoCompletion(Function<String, List<String>> hintToSuggestionList, Runnable onAutoCompleted) {
        AutoCompletionBinding<String> autoCompletion = TextFields.bindAutoCompletion(inputTextField, param -> {
            String hint = param.getUserText();
            return hintToSuggestionList.apply(hint);
        });
        autoCompletion.setOnAutoCompleted(event -> {
            this.add(event.getCompletion());
            inputTextField.clear();
            if (onAutoCompleted != null) {
                onAutoCompleted.run();
            }
        });
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
