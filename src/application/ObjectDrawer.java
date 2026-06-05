package application;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public final class ObjectDrawer {

    private static final double ITEM_SIZE = 20;

    private ObjectDrawer() {
    }

    public static void drawObject(StackPane cell) {
        if (cell == null) {
            return;
        }

        Rectangle object = new Rectangle(ITEM_SIZE, ITEM_SIZE, Color.web("#f5a000"));
        object.setArcWidth(5);
        object.setArcHeight(5);

        cell.getChildren().add(object);
    }
}
