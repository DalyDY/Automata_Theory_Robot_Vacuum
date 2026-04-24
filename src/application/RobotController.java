package application;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.control.TextArea;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class RobotController implements Initializable {

    @FXML private GridPane boardGrid;
    @FXML private FlowPane energyPane;
    @FXML private Label carryingLabel;
    @FXML private Label directionLabel;
    @FXML private Button btnMoveForward;
    @FXML private Button btnMoveBackward;
    @FXML private Button btnTurnLeft;
    @FXML private Button btnTurnRight;
    @FXML private Button btnPickUp;
    @FXML private Button btnDrop;
    @FXML private Button btnRecharge;
    @FXML private Button btnStart;
    @FXML private Button btnStop;
    @FXML private TextArea commandBox;
    @FXML private Label statusLabel;

    private static final int GRID_SIZE = 8;
    private static final int MAX_ENERGY = 3;
    private static final double PIP_SIZE = 26;

    private StackPane[][] cells;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mapBoardCells();
        drawStaticScene();
        updateSidebar();
    }

    private void mapBoardCells() {
        cells = new StackPane[GRID_SIZE][GRID_SIZE];
        List<Node> children = new ArrayList<>(boardGrid.getChildren());

        for (Node node : children) {
            if (!(node instanceof StackPane cell)) {
                continue;
            }

            Integer colIndex = GridPane.getColumnIndex(cell);
            Integer rowIndex = GridPane.getRowIndex(cell);

            int col = colIndex == null ? 0 : colIndex;
            int row = rowIndex == null ? 0 : rowIndex;

            if (col >= 0 && col < GRID_SIZE && row >= 0 && row < GRID_SIZE) {
                cells[col][row] = cell;
                cell.getChildren().clear();
            }
        }
    }

    private void drawStaticScene() {
        clearBoard();

        ObjectDrawer.drawObject(cells[2][1]);
        ObjectDrawer.drawObject(cells[6][5]);
        ObjectDrawer.drawObject(cells[1][7]);

        RobotDrawer.drawRobot(cells[4][3], "NORTH");
    }

    private void clearBoard() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (cells[col][row] != null) {
                    cells[col][row].getChildren().clear();
                }
            }
        }
    }

    private void updateSidebar() {
        energyPane.getChildren().clear();

        for (int i = 0; i < MAX_ENERGY; i++) {
            Region pip = new Region();
            pip.setPrefSize(PIP_SIZE, PIP_SIZE);
            pip.setStyle("-fx-background-radius: 5; -fx-background-color: #08c548;");
            energyPane.getChildren().add(pip);
        }

        carryingLabel.setText("Empty");
        directionLabel.setText("Forward");

        btnDrop.setDisable(true);
    }

    @FXML
    private void onMoveForward() {
    }

    @FXML
    private void onMoveBackward() {
    }

    @FXML
    private void onTurnLeft() {
    }

    @FXML
    private void onTurnRight() {
    }

    @FXML
    private void onPickUp() {
    }

    @FXML
    private void onDrop() {
    }

    @FXML
    private void onRecharge() {
    }

    @FXML
    private void onStart() {
    }

    @FXML
    private void onStop() {
    }

    @FXML private void onValidate() { 

    }

    @FXML private void onClear() { 
        
    }
}
