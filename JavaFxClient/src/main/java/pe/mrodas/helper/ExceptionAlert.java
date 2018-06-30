package pe.mrodas.helper;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionAlert extends Alert {

    public ExceptionAlert(Throwable exception, boolean debugMode) {
        super(AlertType.ERROR);
        this.setHeaderText(exception.getClass().getSimpleName());
        this.setContentText(exception.getMessage());
        if (debugMode) {
            this.setExpandableContent(this.getTrace(exception));
        }
    }

    private void setExpandableContent(String trace) {
        Label label = new Label("The exception stacktrace was:");
        TextArea textArea = this.getTextArea(trace);
        GridPane expContent = this.getGridPane(label, textArea);
        this.getDialogPane().setExpandableContent(expContent);
        this.getDialogPane().setExpanded(true);
    }

    private GridPane getGridPane(Label label, TextArea textArea) {
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        GridPane gridPane = new GridPane();
        gridPane.setMaxWidth(Double.MAX_VALUE);
        gridPane.add(label, 0, 0);
        gridPane.add(textArea, 0, 1);
        gridPane.setPrefWidth(600);
        return gridPane;
    }

    private TextArea getTextArea(String exception) {
        TextArea textArea = new TextArea(exception);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        return textArea;
    }

    private String getTrace(Throwable exception) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        String stackTrace = sw.toString();
        pw.close();
        return stackTrace;
    }
}
