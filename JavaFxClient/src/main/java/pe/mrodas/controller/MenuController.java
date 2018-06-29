package pe.mrodas.controller;

import de.jensd.fx.glyphs.GlyphIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import pe.mrodas.entity.Root;

import java.util.List;
import java.util.stream.Stream;

public class MenuController extends BaseController {

    private final List<Root> rootList;

    public MenuController(List<Root> rootList) {
        super(null);
        super.setTitle("Menu");
        this.rootList = rootList;
    }

    @Override
    public Stage prepareStage(Node node) {
        Button[] buttons = rootList.stream()
                .map(this::getButton)
                .toArray(Button[]::new);
        VBox root = new VBox(7, new HBox(7, buttons));
        root.setPadding(new Insets(7, 7, 7, 5));
        return super.prepareStage(root, node);
    }

    private Button getButton(Root root) {
        Button button = new Button(root.getName());
        try {
            String imageUrl = root.getImageUrl();
            double size = 100;
            Image image = new Image(imageUrl, size, size, true, true);
            button.setGraphic(new ImageView(image));
        } catch (Exception e) {
            GlyphIcon<?> iconView = new MaterialDesignIconView(MaterialDesignIcon.WIFI_OFF);
            iconView.setSize("40");
            button.setGraphic(iconView);
        }
        button.setContentDisplay(ContentDisplay.TOP);
        Runnable runnable = this.getRunnable(root, button);
        button.setOnAction(event -> super.handle(runnable));
        return button;
    }

    private Runnable getRunnable(Root root, Button button) {
        int idRoot = root.getIdRoot() == null ? 0 : root.getIdRoot();
        switch (Item.get(idRoot)) {
            case ANTIFUJIMORISMO:
                return () -> new GalleryController(root)
                        .prepareStage(button)
                        .show();
        }
        return () -> super.infoAlert("No Action Set");
    }

    @Override
    public void initialize() {
    }

    public enum Item {
        NONE(0), ANTIFUJIMORISMO(1), MEMES(2), COMPROBANTES(3);
        private int id;

        public int getId() {
            return id;
        }

        Item(int id) {
            this.id = id;
        }

        public static Item get(int id) {
            for (Item item : Item.values()) {
                if (item.getId() == id) {
                    return item;
                }
            }
            return NONE;
        }
    }
}
