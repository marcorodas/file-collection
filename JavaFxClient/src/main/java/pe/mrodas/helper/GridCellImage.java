package pe.mrodas.helper;

import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import java.io.File;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.controlsfx.control.GridCell;
import org.controlsfx.control.PopOver;

public class GridCellImage extends GridCell<File> {
    private final Consumer<File> onMouseSingleClick;
    private BiConsumer<File, MouseEvent> onMouseDoubleClick;

    public GridCellImage(Consumer<File> onMouseSingleClick, BiConsumer<File, MouseEvent> onMouseDoubleClick) {
        this.onMouseSingleClick = onMouseSingleClick;
        this.onMouseDoubleClick = onMouseDoubleClick;
        this.setOnMouseClicked(this::onMouseClick);
    }

    public GridCellImage(Consumer<File> onMouseSingleClick) {
        this(onMouseSingleClick, null);
    }

    public GridCellImage setPathToClipboardOnDoubleClick() {
        this.onMouseDoubleClick = this::copyImagePath;
        return this;
    }

    public GridCellImage setContextMenuCopyImagePath() {
        MenuItem item = new MenuItem("Copy image path");
        item.setOnAction(e -> this.copyImagePath(this.getItem(), e));
        ContextMenu menu = new ContextMenu(item);
        this.setOnContextMenuRequested(e -> menu.show(this, e.getScreenX(), e.getScreenY()));
        return this;
    }

    private void onMouseClick(MouseEvent e) {
        int clickCount = e.getClickCount();
        if (clickCount > 0) {
            File item = this.getItem();
            if (clickCount == 1) {
                onMouseSingleClick.accept(item);
            } else if (onMouseDoubleClick != null) {
                onMouseDoubleClick.accept(item, e);
            }
        }
    }

    private void copyImagePath(File file, Event e) {
        ClipboardContent content = new ClipboardContent();
        content.putString(file.getAbsolutePath());
        Clipboard.getSystemClipboard().setContent(content);
        Label text = new Label("Image Path Copied!");
        VBox box = new VBox(text);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(0, 20, 0, 20));
        PopOver popOver = new PopOver(box);
        popOver.setDetachable(false);
        popOver.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
        popOver.show(this, -2);
    }

    @Override
    protected void updateItem(File file, boolean empty) {
        super.updateItem(file, empty);
        if (!empty && file != null) {
            Image image = GuiHelper.getImageFromFile(file, 100);
            if (image != null) {
                this.setGraphic(new ImageView(image));
            }
        }
    }
}
