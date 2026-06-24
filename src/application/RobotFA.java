package application;

// The two enums below define the possible values for direction and object-carrying state.
// Dir  = which way the robot is facing
// Obj  = what pick/drop state the robot is in
//   N0 = never picked anything yet
//   H0 = currently holding (first pick)
//   N1 = dropped at least once (pick-drop task done)
//   H1 = currently holding again (second pick onwards)
enum Dir {
    N, E, S, W
}

enum Obj {
    N0, H0, N1, H1
}

public class RobotFA {

    // Robot state
    public int x = 0; // column (0 = left, 7 = right)
    public int y = 0; // row (0 = bottom, 7 = top)
    public Dir dir = Dir.N; // facing direction
    public int energy = 3; // energy units remaining (max 3)
    public int turns = 0; // consecutive turn counter (max 2)
    public Obj obj = Obj.N0; // pick/drop state

    // Internal flags (package-visible so RobotController.Frame can copy them)
    boolean started = false; // has START been issued?
    boolean accepted = false; // has the sequence been accepted (valid STOP)?
    boolean moved = false; // has the robot moved at least once?
    String loop = ""; // tracks the last few commands for loop detection

    // Main method: process one command
    // Returns true if the command was valid, false if it broke a rule.
    public boolean step(String cmd) {

        // Once the sequence is accepted we reject everything
        if (accepted) {
            return false;
        }

        // Before START, only START is allowed (Rule 1)
        if (!started) {
            if (cmd.equals("START")) {
                started = true;
                return true;
            }
            return false;
        }

        // Handle STOP (Rule 1, 2, 4)
        if (cmd.equals("STOP")) {
            // Must have moved at least once (Rule 2)
            if (!moved) {
                return false;
            }
            // Must have completed a full pick-drop task (Rule 4)
            if (obj != Obj.N1) {
                return false;
            }
            accepted = true;
            return true;
        }

        // Route to the right method based on the command
        if (cmd.equals("F") || cmd.equals("B")) {
            return move(cmd);
        } else if (cmd.equals("L") || cmd.equals("R")) {
            return turn(cmd);
        } else if (cmd.equals("P")) {
            return pick();
        } else if (cmd.equals("D")) {
            return drop();
        } else if (cmd.equals("RECHARGE")) {
            return recharge();
        } else {
            // Unknown command
            return false;
        }
    }

    // Getters used by RobotController
    public boolean isAccepted() {
        return accepted;
    }

    public boolean isStarted() {
        return started;
    }

    // Move forward (F) or backward (B)
    private boolean move(String cmd) {

        // Rule 6: cannot move without energy
        if (energy == 0) {
            return false;
        }

        // Work out which direction the robot actually travels
        Dir moveDir;
        if (cmd.equals("F")) {
            moveDir = dir;
        } else {
            moveDir = opposite(dir);
        }

        // Calculate the new position
        int nx = x;
        int ny = y;

        if (moveDir == Dir.N) {
            ny = ny + 1;
        } else if (moveDir == Dir.S) {
            ny = ny - 1;
        } else if (moveDir == Dir.E) {
            nx = nx + 1;
        } else {
            nx = nx - 1;
        }

        // Reject if the new position is outside the 8x8 grid
        if (nx < 0 || nx >= 8 || ny < 0 || ny >= 8) {
            return false;
        }

        // Apply the move
        x = nx;
        y = ny;
        energy = energy - 1;
        turns = 0; // moving resets the consecutive-turn counter
        moved = true;

        if (!updateLoop(cmd)) {
            return false;
        }

        return true;
    }

    // Turn left (L) or right (R)
    private boolean turn(String cmd) {

        // Rule 5: at most 2 consecutive turns
        if (turns >= 2) {
            return false;
        }

        if (cmd.equals("L")) {
            dir = turnLeft(dir);
        } else {
            dir = turnRight(dir);
        }
        turns = turns + 1;

        if (!updateLoop(cmd)) {
            return false;
        }

        // Extra rule: the sequence (Forward then Right) repeated 4 times is a
        // full clockwise loop and is not allowed
        // if (loop.equals("FRFRFRFR")) {
        // return false;
        // }

        return true;
    }

    // Pick up an object
    private boolean pick() {

        // Rule 3: cannot pick up if already carrying something
        if (obj == Obj.H0 || obj == Obj.H1) {
            return false;
        }

        // Move to the next carrying state
        if (obj == Obj.N0) {
            obj = Obj.H0; // first ever pick
        } else {
            obj = Obj.H1; // second or later pick
        }

        turns = 0;
        loop = "";
        return true;
    }

    // Drop an object
    private boolean drop() {

        // Rule 3: cannot drop if not carrying anything
        if (obj == Obj.N0 || obj == Obj.N1) {
            return false;
        }

        obj = Obj.N1; // dropped at least once — pick-drop task complete
        turns = 0;
        loop = "";
        return true;
    }

    // Recharge energy
    private boolean recharge() {

        // Extra rule: can only recharge when energy is exactly 0
        if (energy != 0) {
            return false;
        }

        energy = 3;
        turns = 0;
        // loop = "";
        return true;
    }

    // Loop tracker
    // Returns false if a full clockwise loop is detected
    private boolean updateLoop(String cmd) {
        if (cmd.equals("STOP") || cmd.equals("L") || cmd.equals("P") || cmd.equals("D") || cmd.equals("B")) {
            loop = "";
            return true;
        }

        if (cmd.equals("RECHARGE")) {
            return true; // ignored, loop continues
        }

        if (cmd.equals("F") || cmd.equals("R")) {
            loop += cmd;
            if (loop.contains("FRFRFRFR")) {
                return false; // 🚫 reject
            }
        }

        return true;
    }

    // Direction helpers

    // Returns the direction 90 degrees to the left
    private Dir turnLeft(Dir d) {
        if (d == Dir.N) {
            return Dir.W;
        }
        if (d == Dir.W) {
            return Dir.S;
        }
        if (d == Dir.S) {
            return Dir.E;
        }
        return Dir.N; // E -> N
    }

    // Returns the direction 90 degrees to the right
    private Dir turnRight(Dir d) {
        if (d == Dir.N) {
            return Dir.E;
        }
        if (d == Dir.E) {
            return Dir.S;
        }
        if (d == Dir.S) {
            return Dir.W;
        }
        return Dir.N; // W -> N
    }

    // Returns the opposite direction (used for moving backward)
    private Dir opposite(Dir d) {
        if (d == Dir.N) {
            return Dir.S;
        }
        if (d == Dir.S) {
            return Dir.N;
        }
        if (d == Dir.E) {
            return Dir.W;
        }
        return Dir.E; // W -> E
    }
}
