package pe.mrodas.helper;

import com.jfoenix.controls.JFXAutoCompletePopup;
import com.jfoenix.controls.JFXSpinner;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * https://stackoverflow.com/questions/37378973/implement-tags-bar-in-javafx
 * https://stackoverflow.com/questions/36861056/javafx-textfield-auto-suggestions
 */
public class TagBar<T> extends HBox {
    private final Function<T, String> toString;
    private final JFXAutoCompletePopup<T> autoCompletePopup = new JFXAutoCompletePopup<>();
    @Getter
    private final ObservableList<T> tags = FXCollections.observableArrayList();
    @Getter
    private final TextField inputTextField = new TextField();
    @Setter
    private Runnable onSuggestionIsSelected;

    private TagBar(Function<T, String> toString) {
        this.toString = toString;
        getStyleClass().setAll("tag-bar");
        getStylesheets().add("styles/TagBar.css");
        tags.addListener(this::onChanged);
        autoCompletePopup.setSuggestionsCellFactory(param -> new ListCell<T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && item != null) {
                    this.setGraphic(new Text(toString.apply(item)));
                }
            }
        });
        autoCompletePopup.setSelectionHandler(e -> {
            tags.add(e.getObject());
            inputTextField.clear();
            if (onSuggestionIsSelected != null) {
                onSuggestionIsSelected.run();
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

    public TagBar(Function<T, String> toString, List<T> suggestionList) {
        this(toString);
        autoCompletePopup.getSuggestions().setAll(suggestionList);
        inputTextField.textProperty().addListener((observable, oldHint, newHint) -> {
            if (newHint != null && !newHint.trim().isEmpty()) {
                if (oldHint == null || !newHint.trim().toLowerCase().equals(oldHint.trim().toLowerCase())) {
                    autoCompletePopup.filter(t -> toString.apply(t).equals(newHint));
                    if (autoCompletePopup.getFilteredSuggestions().isEmpty()) {
                        autoCompletePopup.hide();
                    } else {
                        autoCompletePopup.show(inputTextField);
                    }
                }
            }
        });
    }

    public interface SugestionProvider<T> {
        List<T> apply(String hint) throws Exception;
    }

    @Data
    private class SuggestionService extends Service<List<T>> {
        private final SugestionProvider<T> suggestionProvider;
        @Setter
        private String hint;

        @Override
        protected Task<List<T>> createTask() {
            return new Task<List<T>>() {
                @Override
                protected List<T> call() throws Exception {
                    return suggestionProvider.apply(hint);
                }
            };
        }
    }

    public TagBar(Function<T, String> toString, SugestionProvider<T> suggestionProvider) {
        this(toString);
        JFXSpinner spinner = new JFXSpinner();
        MaterialDesignIconView iconView = new MaterialDesignIconView(MaterialDesignIcon.WIFI_OFF);
        iconView.setSize("20");
        SuggestionService service = new SuggestionService(suggestionProvider);
        service.setOnSucceeded(event -> {
            getChildren().remove(iconView);
            getChildren().remove(spinner);
            List<T> suggestions = service.getValue().stream()
                    .filter(t -> !tags.contains(t))
                    .collect(Collectors.toList());
            autoCompletePopup.getSuggestions().setAll(suggestions);
            if (suggestions.isEmpty()) {
                autoCompletePopup.hide();
            } else {
                autoCompletePopup.show(inputTextField);
            }
        });
        service.setOnFailed(event -> {
            getChildren().remove(spinner);
            if (!getChildren().contains(iconView)) {
                getChildren().add(iconView);
            }
        });
        service.setOnRunning(event -> {
            getChildren().remove(iconView);
            if (!getChildren().contains(spinner)) {
                getChildren().add(spinner);
            }
        });
        inputTextField.textProperty().addListener((observable, oldHint, newHint) -> {
            if (newHint != null && !newHint.trim().isEmpty()) {
                if (oldHint == null || !newHint.trim().toLowerCase().equals(oldHint.trim().toLowerCase())) {
                    autoCompletePopup.getSuggestions().clear();
                    service.setHint(newHint);
                    service.restart();
                }
            }
        });
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
                    getChildren().addAll(change.getFrom(), change.getAddedSubList().stream().map(Tag::new).collect(Collectors.toList()));
                }
            }
        }
    }

    private class Tag extends HBox {

        Tag(T tag) {
            getStyleClass().setAll("tag");
            Button removeButton = new Button();
            removeButton.setPadding(Insets.EMPTY);
            MaterialDesignIconView iconView = new MaterialDesignIconView(MaterialDesignIcon.CLOSE_CIRCLE);
            removeButton.setGraphic(iconView);
            removeButton.setOnAction((evt) -> tags.remove(tag));
            Text text = new Text(toString.apply(tag));
            getChildren().addAll(text, removeButton);
        }
    }
}
