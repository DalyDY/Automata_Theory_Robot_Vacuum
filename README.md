# Automata Robot

A JavaFX simulation of a robot moving on an 8×8 grid, controlled by a sequence of commands that are validated using a Finite Automaton (FA).

---

## What It Does

You build a command sequence by clicking buttons (or typing directly into the command box). When you press **Validate**, the program checks the sequence against a set of rules using an FA. If the sequence is valid, the robot animates step by step across the grid.

---

## Project Structure

```
Automata_Robot/
├── src/application/
│   ├── Main.java              — launches the JavaFX app
│   ├── RobotFA.java           — the Finite Automaton (all rules live here)
│   ├── RobotController.java   — handles buttons, validation, and animation
│   ├── RobotDrawer.java       — draws the robot circle on the grid
│   ├── ObjectDrawer.java      — draws pickup objects on the grid
│   ├── Robot.fxml             — the UI layout
│   └── application.css        — styling
├── lib/                       — JavaFX SDK goes here
└── .vscode/
    └── launch.json            — VS Code run configuration
```

---

## Requirements

- **Java 17** or higher
- **JavaFX SDK 25.0.1** — download from [https://openjfx.io](https://openjfx.io)

---

## Setup (VS Code)

1. Download and unzip the JavaFX SDK.
2. Open `launch.json` and update the `--module-path` to point to your JavaFX `lib` folder:
   ```
   "--module-path", "C:\\path\\to\\javafx-sdk-25.0.1\\lib"
   ```
3. Open the project folder in VS Code.
4. Run via the **Run and Debug** panel using the **Main** configuration.

---

## How to Use

1. Click **Start** first — every valid sequence must begin with `START`.
2. Use the buttons to build your command sequence:
   | Button | Token | What it does |
   |---|---|---|
   | Start | `START` | Begins the sequence |
   | Move Forward | `F` | Move one cell in the facing direction |
   | Move Backward | `B` | Move one cell in the opposite direction |
   | Turn Left | `L` | Rotate 90° counter-clockwise |
   | Turn Right | `R` | Rotate 90° clockwise |
   | Pick Up | `P` | Pick up the object at the current cell |
   | Drop | `D` | Drop the carried object at the current cell |
   | Recharge | `RECHARGE` | Refill energy to 3 (only when energy = 0) |
   | Stop | `STOP` | Ends the sequence |
3. Press **Validate** to check and animate the sequence.
4. Press **Reset Command** to clear and start over.

---

## Rules (enforced by the FA)

| # | Rule |
|---|---|
| 1 | The sequence must start with `START` and end with `STOP` |
| 2 | The robot must make at least one movement (`F` or `B`) |
| 3 | `P` (pick) must come before `D` (drop) — cannot pick twice without dropping |
| 4 | At least one full pick-drop task must be completed before `STOP` |
| 5 | No more than 2 consecutive turns (`L` or `R`) in a row |
| 6 | Movement costs 1 energy — the robot cannot move when energy is 0 |
| + | `RECHARGE` is only allowed when energy is exactly 0 |
| + | The clockwise loop `(F → R) × 4` is not allowed |

---

## Grid

- The grid is **8 columns × 8 rows**, coordinates `(x, y)`.
- `(0, 0)` is the **bottom-left** corner — where the robot starts, facing **North**.
- `x` increases to the right (0–7), `y` increases upward (0–7).
- Three objects are placed on the grid at the start: `(2,1)`, `(6,5)`, and `(1,7)`.

---

## Example Valid Sequence

```
START F F R F P F F D STOP
```

This moves the robot forward twice, turns right, moves forward, picks up an object, moves forward twice, drops the object, then stops.

---

## FA Design Summary

The `RobotFA` class implements the automaton. Each call to `step(cmd)` transitions the FA based on the current state and the command received. The FA tracks:

- Whether `START` has been issued
- Whether at least one move has occurred
- The robot's position, direction, and energy
- The pick/drop state using the `Obj` enum (`N0 → H0 → N1`)
- Consecutive turn count (max 2)
- A running suffix string for clockwise loop detection

The FA only accepts (returns `true` for `STOP`) when all rules are satisfied.
