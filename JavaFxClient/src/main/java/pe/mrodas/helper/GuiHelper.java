package pe.mrodas.helper;

import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@UtilityClass
public class GuiHelper {
    public Image getImageFromFile(File file, Integer size) {
        try (FileInputStream stream = new FileInputStream(file)) {
            return size == null ? new Image(stream)
                    : new Image(stream, size, size, true, true);
        } catch (IOException e) {
            return null;
        }
    }

    public void consumeMousePressed(Node node){
        node.addEventFilter(MouseEvent.MOUSE_PRESSED, Event::consume);
    }
}
