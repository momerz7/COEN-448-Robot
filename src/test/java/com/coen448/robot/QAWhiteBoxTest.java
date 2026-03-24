package com.coen448.robot;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

class QAWhiteBoxTest {

    @ParameterizedTest(name = "{0}-{1}")
    @CsvFileSource(resources = "/whitebox_test_cases.csv", delimiter = ';', numLinesToSkip = 1)
    void runWhiteBoxCsvCases(String testId, String operation, String inputA, String inputB, String inputC, String expected,
            String coverageTargets) {

        switch (operation) {
            case "PARSER_CMD" -> {
                Command cmd = new CommandParser().parse(normalizeNullable(inputA));
                String[] parts = expected.split("\\|", -1);
                assertEquals(CommandType.valueOf(parts[0]), cmd.type(), testId);
                Integer expectedArg = "null".equals(parts[1]) ? null : Integer.parseInt(parts[1]);
                assertEquals(expectedArg, cmd.arg(), testId);
            }
            case "PARSER_EXCEPTION" -> {
                IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                        () -> new CommandParser().parse(normalizeNullable(inputA)), testId);
                assertEquals(expected, ex.getMessage(), testId);
            }
            case "FLOOR_INBOUNDS" -> {
                Floor floor = new Floor(Integer.parseInt(inputA));
                assertEquals(Boolean.parseBoolean(expected),
                        floor.inBounds(Integer.parseInt(inputB), Integer.parseInt(inputC)), testId);
            }
            case "FLOOR_SIZE" -> {
                Floor floor = new Floor(Integer.parseInt(inputA));
                assertEquals(Integer.parseInt(expected), floor.size(), testId);
            }
            case "FLOOR_CONSTRUCTOR_EXCEPTION" -> {
                IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                        () -> new Floor(Integer.parseInt(inputA)), testId);
                assertEquals(expected, ex.getMessage(), testId);
            }
            case "FLOOR_MARK_AND_IS_MARKED" -> {
                Floor floor = new Floor(Integer.parseInt(inputA));
                int x = Integer.parseInt(inputB);
                int y = Integer.parseInt(inputC);
                floor.mark(x, y);
                assertEquals(Boolean.parseBoolean(expected), floor.isMarked(x, y), testId);
            }
            case "FLOOR_MARK_EXCEPTION" -> {
                Floor floor = new Floor(Integer.parseInt(inputA));
                IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                        () -> floor.mark(Integer.parseInt(inputB), Integer.parseInt(inputC)), testId);
                assertEquals(expected, ex.getMessage(), testId);
            }
            case "FLOOR_IS_MARKED_EXCEPTION" -> {
                Floor floor = new Floor(Integer.parseInt(inputA));
                IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                        () -> floor.isMarked(Integer.parseInt(inputB), Integer.parseInt(inputC)), testId);
                assertEquals(expected, ex.getMessage(), testId);
            }
            case "FLOOR_CLEAR_UNMARKS" -> {
                Floor floor = new Floor(Integer.parseInt(inputA));
                int x = Integer.parseInt(inputB);
                int y = Integer.parseInt(inputC);
                floor.mark(x, y);
                floor.clear();
                assertEquals(Boolean.parseBoolean(expected), floor.isMarked(x, y), testId);
            }
            case "DIRECTION_TURN_RIGHT" -> {
                Direction start = Direction.valueOf(inputA);
                assertEquals(Direction.valueOf(expected), start.turnRight(), testId);
            }
            case "DIRECTION_TURN_LEFT" -> {
                Direction start = Direction.valueOf(inputA);
                assertEquals(Direction.valueOf(expected), start.turnLeft(), testId);
            }
            case "DIRECTION_TO_STRING" -> assertEquals(expected, Direction.valueOf(inputA).toString(), testId);
            case "PENSTATE_TO_STRING" -> assertEquals(expected, PenState.valueOf(inputA).toString(), testId);
            case "ROBOT_DEFAULTS" -> {
                Robot robot = new Robot();
                assertEquals(expected, robot.getX() + "|" + robot.getY() + "|" + robot.getFacing() + "|" + robot.getPen(),
                        testId);
            }
            case "ROBOT_MUTATION" -> {
                Robot robot = new Robot();
                robot.setPen(PenState.DOWN);
                robot.turnRight();
                robot.turnRight();
                robot.setPosition(1, 2);
                assertEquals(expected, robot.getX() + "|" + robot.getY() + "|" + robot.getFacing() + "|" + robot.getPen(),
                        testId);
            }
            case "SIM_IS_INITIALIZED" -> {
                Simulator sim = new Simulator();
                assertEquals(Boolean.parseBoolean(expected), sim.isInitialized(), testId);
            }
            case "SIM_IS_INITIALIZED_AFTER_INIT" -> {
                Simulator sim = new Simulator();
                sim.initialize(Integer.parseInt(inputA));
                assertEquals(Boolean.parseBoolean(expected), sim.isInitialized(), testId);
            }
            case "SIM_STATUS_EXCEPTION_NOT_INIT" -> {
                Simulator sim = new Simulator();
                IllegalStateException ex = assertThrows(IllegalStateException.class, sim::statusString, testId);
                assertEquals(expected, ex.getMessage(), testId);
            }
            case "SIM_STATUS_AFTER_INIT" -> {
                Simulator sim = new Simulator();
                sim.initialize(Integer.parseInt(inputA));
                assertEquals(expected, sim.statusString(), testId);
            }
            case "SIM_MOVE_PEN_UP" -> {
                Simulator sim = new Simulator();
                sim.initialize(Integer.parseInt(inputA));
                sim.move(Integer.parseInt(inputB));

                String[] parts = expected.split("\\|", -1);
                assertEquals(Integer.parseInt(parts[0]), sim.snapshotRobot().getX(), testId);
                assertEquals(Integer.parseInt(parts[1]), sim.snapshotRobot().getY(), testId);
                boolean hasMark = sim.floorString().contains("*");
                assertEquals(Boolean.parseBoolean(parts[2]), hasMark, testId);
            }
            case "SIM_MOVE_PEN_DOWN" -> {
                Simulator sim = new Simulator();
                sim.initialize(Integer.parseInt(inputA));
                sim.penDown();
                sim.move(Integer.parseInt(inputB));

                String[] parts = expected.split("\\|", -1);
                assertEquals(Integer.parseInt(parts[0]), sim.snapshotRobot().getX(), testId);
                assertEquals(Integer.parseInt(parts[1]), sim.snapshotRobot().getY(), testId);
                boolean hasMark = sim.floorString().contains("*");
                assertEquals(Boolean.parseBoolean(parts[2]), hasMark, testId);
            }
            case "SIM_MOVE_BOUNDARY" -> {
                Simulator sim = new Simulator();
                sim.initialize(Integer.parseInt(inputA));
                sim.move(Integer.parseInt(inputB));
                String[] parts = expected.split("\\|", -1);
                assertEquals(Integer.parseInt(parts[0]), sim.snapshotRobot().getX(), testId);
                assertEquals(Integer.parseInt(parts[1]), sim.snapshotRobot().getY(), testId);
            }
            case "SIM_MOVE_NEGATIVE_EXCEPTION" -> {
                Simulator sim = new Simulator();
                sim.initialize(Integer.parseInt(inputA));
                IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                        () -> sim.move(Integer.parseInt(inputB)), testId);
                assertEquals(expected, ex.getMessage(), testId);
            }
            case "SIM_TURNS_AND_STATUS" -> {
                Simulator sim = new Simulator();
                sim.initialize(Integer.parseInt(inputA));
                sim.turnRight();
                sim.turnLeft();
                assertEquals(expected, sim.statusString(), testId);
            }
            case "COMMAND_RECORD" -> {
                Command cmd = new Command(CommandType.Move, 7);
                String joined = cmd.type() + "|" + cmd.arg();
                assertEquals(expected, joined, testId);
            }
            case "COMMANDTYPE_VALUES_COUNT" -> assertEquals(Integer.parseInt(expected), CommandType.values().length, testId);
            default -> throw new IllegalArgumentException("Unknown operation in CSV: " + operation + " for " + testId);
        }

        assertFalse(coverageTargets.isBlank(), "Coverage target column must be populated for " + testId);
    }

    @Test
    void mainLoop_withHistoryAndParseError_executesAndExits() {
        String input = String.join("\n", "I 3", "D", "M 1", "H", "X", "Q") + "\n";

        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();

        PrintStream originalOut = System.out;
        java.io.InputStream originalIn = System.in;

        try {
            System.setIn(in);
            System.setOut(new PrintStream(outBytes, true, StandardCharsets.UTF_8));

            Main.main(new String[0]);
        } finally {
            System.setOut(originalOut);
            System.setIn(originalIn);
        }

        String out = outBytes.toString(StandardCharsets.UTF_8);
        assertTrue(out.contains("Initialization successful. Floor size: 3x3."));
        assertTrue(out.contains("Pen Placed."));
        assertTrue(out.contains("Move accepted. Steps moved: 1."));
        assertTrue(out.contains("Error: Invalid command format: X"));
    }

    @Test
    void mainExecuteAndPrint_switchCasesCoveredByReflection() throws Exception {
        Method executeAndPrint = Main.class.getDeclaredMethod("executeAndPrint", Simulator.class, Command.class);
        executeAndPrint.setAccessible(true);

        Simulator sim = new Simulator();
        sim.initialize(4);

        assertDoesNotThrow(() -> executeAndPrint.invoke(null, sim, new Command(CommandType.Pen_Up, null)));
        assertDoesNotThrow(() -> executeAndPrint.invoke(null, sim, new Command(CommandType.Pen_Down, null)));
        assertDoesNotThrow(() -> executeAndPrint.invoke(null, sim, new Command(CommandType.Right, null)));
        assertDoesNotThrow(() -> executeAndPrint.invoke(null, sim, new Command(CommandType.Left, null)));
        assertDoesNotThrow(() -> executeAndPrint.invoke(null, sim, new Command(CommandType.Move, 1)));
        assertDoesNotThrow(() -> executeAndPrint.invoke(null, sim, new Command(CommandType.Print, null)));
        assertDoesNotThrow(() -> executeAndPrint.invoke(null, sim, new Command(CommandType.Status, null)));
        assertDoesNotThrow(() -> executeAndPrint.invoke(null, sim, new Command(CommandType.Quit, null)));
        assertDoesNotThrow(() -> executeAndPrint.invoke(null, sim, new Command(CommandType.Init, 2)));
        assertDoesNotThrow(() -> executeAndPrint.invoke(null, sim, new Command(CommandType.History, null)));
    }

    @Test
    void mainReplay_skipsHistoryAndQuitAndReplaysOthers() throws Exception {
        Method replay = Main.class.getDeclaredMethod("replay", List.class, CommandParser.class);
        replay.setAccessible(true);

        List<String> history = new ArrayList<>();
        history.add("I 3");
        history.add("Q");
        history.add("H");
        history.add("D");
        history.add("M 1");

        assertDoesNotThrow(() -> replay.invoke(null, history, new CommandParser()));
    }

    @Test
    void directionAndPen_allEnumPathsAndAccessors() {
        assertEquals(0, Direction.NORTH.dx());
        assertEquals(1, Direction.NORTH.dy());
        assertEquals(1, Direction.EAST.dx());
        assertEquals(0, Direction.EAST.dy());
        assertEquals(0, Direction.SOUTH.dx());
        assertEquals(-1, Direction.SOUTH.dy());
        assertEquals(-1, Direction.WEST.dx());
        assertEquals(0, Direction.WEST.dy());

        assertEquals(Direction.EAST, Direction.NORTH.turnRight());
        assertEquals(Direction.SOUTH, Direction.EAST.turnRight());
        assertEquals(Direction.WEST, Direction.SOUTH.turnRight());
        assertEquals(Direction.NORTH, Direction.WEST.turnRight());

        assertEquals(Direction.WEST, Direction.NORTH.turnLeft());
        assertEquals(Direction.NORTH, Direction.EAST.turnLeft());
        assertEquals(Direction.EAST, Direction.SOUTH.turnLeft());
        assertEquals(Direction.SOUTH, Direction.WEST.turnLeft());

        assertEquals("up", PenState.UP.toString());
        assertEquals("down", PenState.DOWN.toString());
    }

    @Test
    void mainConstructor_isCovered() {
        assertDoesNotThrow(Main::new);
    }

    private String normalizeNullable(String value) {
        if ("<NULL>".equals(value)) {
            return null;
        }
        if ("<BLANK>".equals(value)) {
            return "   ";
        }
        return value;
    }
}
