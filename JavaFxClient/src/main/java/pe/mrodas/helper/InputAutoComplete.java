package pe.mrodas.helper;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.jfoenix.controls.JFXAutoCompletePopup;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.events.JFXAutoCompleteEvent;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import lombok.Setter;

public class InputAutoComplete<T> {

    public interface SuggestionProvider<T> {
        List<T> apply(String hint) throws Exception;
    }

    class SuggestionService extends Service<List<T>> {
        private final SuggestionProvider<T> suggestionProvider;
        @Setter
        private String hint;

        SuggestionService(SuggestionProvider<T> suggestionProvider) {
            this.suggestionProvider = suggestionProvider;
        }

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

    private final JFXAutoCompletePopup<T> autoCompletePopup = new JFXAutoCompletePopup<>();
    private final TextField inputTextField;
    private final Function<T, String> toString;
    private SuggestionService service;

    public InputAutoComplete(TextField inputTextField, Function<T, String> toString) {
        this.inputTextField = inputTextField;
        this.toString = toString;
        autoCompletePopup.setSuggestionsCellFactory(param -> new ListCell<T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && item != null) {
                    this.setGraphic(new Text(toString.apply(item)));
                }
            }
        });
    }

    public void setSelectionHandler(EventHandler<JFXAutoCompleteEvent<T>> selectionHandler) {
        autoCompletePopup.setSelectionHandler(selectionHandler);
    }

    private boolean checkTextChange(String oldHint, String newHint) {
        return newHint != null && !newHint.trim().isEmpty() &&
                !newHint.trim().equalsIgnoreCase(oldHint == null ? null : oldHint.trim());
    }

    public void setSuggestionList(List<T> suggestionList) {
        if (service == null && autoCompletePopup.getSuggestions().isEmpty()) {
            autoCompletePopup.getSuggestions().setAll(suggestionList);
            inputTextField.textProperty().addListener((o, oldHint, newHint) -> {
                if (this.checkTextChange(oldHint, newHint)) {
                    autoCompletePopup.filter(t -> toString.apply(t).equals(newHint));
                    if (autoCompletePopup.getFilteredSuggestions().isEmpty()) {
                        autoCompletePopup.hide();
                    } else {
                        autoCompletePopup.show(inputTextField);
                    }
                }
            });
        }
    }

    public void setSuggestionProvider(Pane spinnerHolder, SuggestionProvider<T> suggestionProvider, Predicate<T> serviceResultFilter) {
        if (service == null) {
            this.setService(spinnerHolder, suggestionProvider, serviceResultFilter);
            inputTextField.textProperty().addListener((o, oldHint, newHint) -> {
                if (this.checkTextChange(oldHint, newHint)) {
                    autoCompletePopup.getSuggestions().clear();
                    service.setHint(newHint);
                    service.restart();
                }
            });
        }
    }

    private void setService(Pane spinnerHolder, SuggestionProvider<T> suggestionProvider, Predicate<T> serviceResultFilter) {
        JFXSpinner spinner = new JFXSpinner();
        spinner.setPrefHeight(25);
        MaterialDesignIconView iconView = new MaterialDesignIconView(MaterialDesignIcon.WIFI_OFF);
        iconView.setSize("20");
        service = new SuggestionService(suggestionProvider);
        service.setOnSucceeded(event -> {
            spinnerHolder.getChildren().remove(iconView);
            spinnerHolder.getChildren().remove(spinner);
            List<T> suggestions = service.getValue().stream()
                    .filter(serviceResultFilter)
                    .collect(Collectors.toList());
            autoCompletePopup.getSuggestions().setAll(suggestions);
            if (suggestions.isEmpty()) {
                autoCompletePopup.hide();
            } else {
                autoCompletePopup.show(inputTextField);
            }
        });
        service.setOnFailed(event -> {
            spinnerHolder.getChildren().remove(spinner);
            if (!spinnerHolder.getChildren().contains(iconView)) {
                spinnerHolder.getChildren().add(iconView);
            }
        });
        service.setOnRunning(event -> {
            spinnerHolder.getChildren().remove(iconView);
            if (!spinnerHolder.getChildren().contains(spinner)) {
                spinnerHolder.getChildren().add(spinner);
            }
        });
    }

}
