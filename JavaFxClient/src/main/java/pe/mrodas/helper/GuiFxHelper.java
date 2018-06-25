package pe.mrodas.helper;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import lombok.experimental.UtilityClass;

@UtilityClass
public class GuiFxHelper {

    public boolean isPrimaryDoubleClick(MouseEvent e){
        return e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2;
    }

}
