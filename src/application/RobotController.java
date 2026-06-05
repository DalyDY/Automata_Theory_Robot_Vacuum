package application;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.geometry.Pos;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class RobotController implements Initializable {

    // JavaFX fields wired to Robot.fxml
    @FXML private GridPane boardGrid;
    @FXML private FlowPane energyPane;
    @FXML private Label carryingLabel;
    @FXML private Label directionLabel;
    @FXML private TextArea commandBox;
    @FXML private Label statusLabel;

    private static final int GRID_SIZE = 8; // 8 columns, 8 rows
    private static final int PIP_SIZE  = 22; // size of each energy pip square

    private StackPane[][] cells;    // holds a reference to every grid cell
    private boolean[][] objects;  // tracks which cells have an object on them
    private RobotFA fa;       // the finite automaton / robot state


    // ── Frame: a snapshot of the robot + grid at one moment in time ─────
    // We pre-compute every frame before animating so that the animation is
    // just "show frame 0, wait, show frame 1, wait, ..." with no DFA calls.
    private static class Frame {

        RobotFA fa; // copy of the robot state at this moment
        boolean[][] objects; // copy of the object grid at this moment
        String cmd; // which command produced this frame

        Frame(RobotFA source, boolean[][] sourceObjects, String command) {

            // Copy every field of the RobotFA manually
            this.fa = new RobotFA();
            this.fa.x = source.x;
            this.fa.y = source.y;
            this.fa.dir = source.dir;
            this.fa.energy = source.energy;
            this.fa.turns = source.turns;
            this.fa.obj = source.obj;

            // Copy the 2D objects array row by row
            this.objects = new boolean[GRID_SIZE][GRID_SIZE];
            for (int i = 0; i < GRID_SIZE; i++) {
                this.objects[i] = Arrays.copyOf(sourceObjects[i], GRID_SIZE);
            }

            this.cmd = command;
        }
    }


    // Startup
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fa = new RobotFA();
        objects = makeObjects();
        mapBoardCells();
        redrawScene();
        statusLabel.setText("Enter commands then VALIDATE");
    }

    // Place three objects on the starting grid
    private boolean[][] makeObjects() {
        boolean[][] arr = new boolean[GRID_SIZE][GRID_SIZE];
        arr[2][1] = true;
        arr[6][5] = true;
        arr[1][7] = true;
        return arr;
    }


    // Grid helpers

    // Read every StackPane child from the GridPane and store it in cells[][]
    private void mapBoardCells() {
        cells = new StackPane[GRID_SIZE][GRID_SIZE];

        for (Node node : boardGrid.getChildren()) {
            if (node instanceof StackPane) {
                StackPane cell = (StackPane) node;

                // getColumnIndex / getRowIndex can return null for index 0
                Integer colObj = GridPane.getColumnIndex(cell);
                Integer rowObj = GridPane.getRowIndex(cell);
                int col = (colObj == null) ? 0 : colObj;
                int row = (rowObj == null) ? 0 : rowObj;

                cells[col][row] = cell;
            }
        }
    }

    // Convert logical (x, y) where y=0 is the bottom row into the visual
    // grid row index where row 0 is the top.
    private StackPane getCell(int x, int y) {
        int visualRow = GRID_SIZE - 1 - y;
        return cells[x][visualRow];
    }

    // Remove all drawn content from every cell
    private void clearBoard() {
        for (int col = 0; col < GRID_SIZE; col++) {
            for (int row = 0; row < GRID_SIZE; row++) {
                if (cells[col][row] != null) {
                    cells[col][row].getChildren().clear();
                }
            }
        }
    }

    // Redraw everything: labels, objects, robot
    private void redrawScene() {
        clearBoard();

        // Draw the (x,y) coordinate label on every cell
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                addCoordLabel(getCell(x, y), x, y);
            }
        }

        // Draw objects that are still on the grid
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                if (objects[x][y]) {
                    ObjectDrawer.drawObject(getCell(x, y));
                }
            }
        }

        // Draw the robot at its current position
        RobotDrawer.drawRobot(getCell(fa.x, fa.y), directionToString(fa.dir));

        updateSidebar();
    }

    // Add a small grey (x,y) label at the bottom of a cell
    private void addCoordLabel(StackPane cell, int x, int y) {
        if (cell == null) {
            return;
        }
        Text label = new Text("(" + x + "," + y + ")");
        label.setFont(Font.font(9));
        label.setFill(Color.web("#9ca3af"));
        StackPane.setAlignment(label, Pos.BOTTOM_CENTER);
        label.setTranslateY(-2);
        cell.getChildren().add(label);
    }

    // Refresh the energy pips, carrying status, and direction label
    private void updateSidebar() {

        // Rebuild the energy pip squares
        energyPane.getChildren().clear();
        for (int i = 0; i < fa.energy; i++) {
            Region pip = new Region();
            pip.setPrefSize(PIP_SIZE, PIP_SIZE);
            pip.setStyle("-fx-background-color: #08c548; -fx-background-radius: 3;");
            energyPane.getChildren().add(pip);
        }

        // Show carrying status
        if (fa.obj == Obj.H0 || fa.obj == Obj.H1) {
            carryingLabel.setText("Carrying");
        } else {
            carryingLabel.setText("Empty");
        }

        // Show direction
        directionLabel.setText(directionToString(fa.dir));
    }

    // Convert Dir enum to a readable string
    private String directionToString(Dir d) {
        if (d == Dir.N) { 
            return "NORTH"; 
        }
        if (d == Dir.E) { 
            return "EAST";  
        }
        if (d == Dir.S) { 
            return "SOUTH"; 
        }
        return "WEST";
    }


    //Button handlers — each one appends a token to the command box
    @FXML private void onStart() { 
        commandBox.appendText("START ");    
    }

    @FXML private void onStop() { 
        commandBox.appendText("STOP ");     
    }

    @FXML private void onMoveForward() { 
        commandBox.appendText("F ");        
    }

    @FXML private void onMoveBackward() { 
        commandBox.appendText("B ");        
    }

    @FXML private void onTurnLeft() { 
        commandBox.appendText("L ");        
    }

    @FXML private void onTurnRight() { 
        commandBox.appendText("R ");        
    }

    @FXML private void onPickUp() { 
        commandBox.appendText("P ");        
    }

    @FXML private void onDrop() { 
        commandBox.appendText("D ");     
    }

    @FXML private void onRecharge() { 
        commandBox.appendText("RECHARGE "); 
    }


    // Validate button
    @FXML
    private void onValidate() {
        String raw = commandBox.getText().trim();
        if (raw.isEmpty()) {
            statusLabel.setText("No commands entered.");
            return;
        }

        // Split the text into individual command tokens
        String[] commands = raw.split("\\s+");

        // Use a fresh RobotFA and a fresh object grid for the validation run
        RobotFA simFa   = new RobotFA();
        boolean[][] simObjs = makeObjects();
        boolean moved   = false;

        // Build the list of frames as we validate each command
        List<Frame> frames = new ArrayList<Frame>();
        frames.add(new Frame(simFa, simObjs, "INIT"));  // the very first frame (before any command)

        for (int i = 0; i < commands.length; i++) {
            String cmd = commands[i];

            if (cmd.isEmpty()) {
                continue;
            }

            // Grid-level checks (the DFA itself does not know about physical objects)
            if (cmd.equals("P")) {
                if (!simObjs[simFa.x][simFa.y]) {
                    statusLabel.setText("Invalid: no object at (" + simFa.x + "," + simFa.y + ") to pick up.");
                    return;
                }
            }
            if (cmd.equals("D")) {
                if (simObjs[simFa.x][simFa.y]) {
                    statusLabel.setText("Invalid: cell (" + simFa.x + "," + simFa.y + ") already has an object.");
                    return;
                }
            }

            // Ask the DFA to process this command
            boolean ok = simFa.step(cmd);
            if (!ok) {
                statusLabel.setText(buildErrorMessage(cmd, simFa));
                return;
            }

            // Update the object grid to reflect picks and drops
            if (cmd.equals("P")) {
                simObjs[simFa.x][simFa.y] = false; // object was picked up
            }
            if (cmd.equals("D")) {
                simObjs[simFa.x][simFa.y] = true; // object was dropped here
            }
            if (cmd.equals("F") || cmd.equals("B")) {
                moved = true;
            }

            // Save a snapshot of the state after this command
            frames.add(new Frame(simFa, simObjs, cmd));
        }

        // Final checks after all commands have been processed
        if (!simFa.isAccepted()) {
            if (!moved) {
                statusLabel.setText("Invalid: must include at least one movement.");
            } else {
                statusLabel.setText("Invalid: sequence must end with STOP after completing a pick-drop.");
            }
            return;
        }

        // All good — reset to the start and play the animation
        statusLabel.setText("Valid! Running animation...");
        playFrames(frames, 0);
    }

    // Build a human-readable error message for a rejected command
    private String buildErrorMessage(String cmd, RobotFA state) {

        if (cmd.equals("START")) {
            return "Invalid: START has already been issued.";
        }

        if (cmd.equals("STOP")) {
            if (state.obj != Obj.N1) {
                return "Invalid: must complete a pick-drop task before STOP.";
            }
            return "Invalid: must perform at least one movement before STOP.";
        }

        if (cmd.equals("F") || cmd.equals("B")) {
            if (state.energy == 0) {
                return "Invalid: no energy left — use RECHARGE first.";
            }
            return "Invalid: movement would go outside the grid.";
        }

        if (cmd.equals("L") || cmd.equals("R")) {
            return "Invalid: cannot make more than 2 consecutive turns (or clockwise loop detected).";
        }

        if (cmd.equals("P")) {
            return "Invalid: already carrying an object — drop it first.";
        }

        if (cmd.equals("D")) {
            return "Invalid: not carrying anything to drop.";
        }

        if (cmd.equals("RECHARGE")) {
            return "Invalid: can only recharge when energy is exactly 0.";
        }

        return "Invalid command: \"" + cmd + "\".";
    }


    // Animation
    // We step through the pre-built list of frames one at a time.
    // After showing each frame we wait 600 ms before moving to the next.
    private void playFrames(List<Frame> frames, int index) {

        // All frames shown — animation done
        if (index >= frames.size()) {
            statusLabel.setText("Animation complete!");
            return;
        }

        Frame f = frames.get(index);

        // Restore the robot state from this frame's snapshot
        fa.x = f.fa.x;
        fa.y = f.fa.y;
        fa.dir = f.fa.dir;
        fa.energy = f.fa.energy;
        fa.turns = f.fa.turns;
        fa.obj = f.fa.obj;

        // Restore the object grid from this frame's snapshot
        for (int i = 0; i < GRID_SIZE; i++) {
            objects[i] = Arrays.copyOf(f.objects[i], GRID_SIZE);
        }

        redrawScene();

        // Show the current step in the status bar (skip the initial "INIT" frame)
        if (!f.cmd.equals("INIT")) {
            statusLabel.setText("Step " + index + " / " + (frames.size() - 1) + ":  " + f.cmd);
        }

        // Wait 600 ms then show the next frame
        PauseTransition pause = new PauseTransition(Duration.millis(600));
        pause.setOnFinished(event -> playFrames(frames, index + 1));
        pause.play();
    }


    // Reset button 
    @FXML
    private void onClear() {
        fa = new RobotFA();
        objects = makeObjects();
        commandBox.clear();
        statusLabel.setText("Cleared.");
        redrawScene();
    }
}
