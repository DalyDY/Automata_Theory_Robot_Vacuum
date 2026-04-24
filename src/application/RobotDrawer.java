package application;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public final class RobotDrawer {

    private static final double BODY_RADIUS = 20;
    private static final double EYE_RADIUS = 5;
    private static final double EYE_OFFSET = 7;

    private RobotDrawer() {
    }

    public static void drawRobot(StackPane cell, String direction) {
        if (cell == null) {
            return;
        }

        Circle body = new Circle(BODY_RADIUS, Color.web("#1f2940"));
        Circle eye = new Circle(EYE_RADIUS, Color.web("#4a90e2"));

        switch (direction.toUpperCase()) {
            case "SOUTH" -> eye.setTranslateY(EYE_OFFSET);
            case "WEST" -> eye.setTranslateX(-EYE_OFFSET);
            case "EAST" -> eye.setTranslateX(EYE_OFFSET);
            default -> eye.setTranslateY(-EYE_OFFSET);
        }

        cell.getChildren().addAll(body, eye);
    }
}
