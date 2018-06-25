package pe.mrodas.controller;

import de.jensd.fx.glyphs.GlyphIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import pe.mrodas.entity.MenuEnum;
import pe.mrodas.helper.StrHelper;

import java.util.stream.Stream;

public class MenuController extends BaseController {

    public MenuController() {
        super(null);
        super.setTitle("Menu");
    }

    @Override
    public Stage prepareStage(Node node) {
        Button[] buttons = Stream.of(MenuEnum.values())
                .map(this::getButton)
                .toArray(Button[]::new);
        VBox root = new VBox(7, new HBox(7, buttons));
        root.setPadding(new Insets(7, 7, 7, 5));
        return super.prepareStage(root, node);
    }

    private Button getButton(MenuEnum item) {
        String name = StrHelper.charToUpperCase(0, item.name().toLowerCase());
        Button button = new Button(name);
        GlyphIcon<?> iconView = new MaterialDesignIconView();
        button.setGraphic(iconView);
        iconView.setSize("40");
        String url = null;
        switch (item) {
            case ANTIFUJIMORISMO:
                url = "";
                iconView.setGlyphName("PIG");
                break;
            case MEMES:
                iconView.setGlyphName("CAT");
                break;
            case COMPROBANTES:
                iconView.setGlyphName("RECEIPT");
                break;
        }
        super.setOnAction(button, e -> new GalleryController(item).prepareStage(button).show());
        return button;
    }

    @Override
    public void initialize() {
    }

}
