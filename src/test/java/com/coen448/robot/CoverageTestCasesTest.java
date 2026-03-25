package com.coen448.robot;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

class CoverageTestCasesTest {

    @ParameterizedTest(name = "{0}-{1}-{2}")
    @CsvFileSource(resources = "/coverage_test_cases.csv", numLinesToSkip = 1, nullValues = {"<null>"})
    void runCoverageCsvCases(
        String id,
        String suite,
        String op,
        String input1,
        String input2,
        String input3,
        String expected,
        String assertion,
        String coverageTargets
    ) {
        assertNotNull(id);
        assertNotNull(coverageTargets);

        switch (suite) {
            case "PARSER" -> runParserCase(input1, expected, assertion);
            case "FLOOR" -> runFloorCase(op, input1, input2, input3, expected, assertion);
            case "SIM" -> runSimulatorCase(op, input1, input2, expected, assertion);
            default -> throw new IllegalArgumentException("Unknown suite: " + suite);
        }
    }

    @Test
    void directionRobotEnumAndRecordCoverage() {
        assertEquals("north", Direction.NORTH.toString());
        assertEquals(Direction.EAST, Direction.NORTH.turnRight());
        assertEquals(Direction.WEST, Direction.NORTH.turnLeft());
        assertEquals(Direction.SOUTH, Direction.EAST.turnRight());
        assertEquals(Direction.NORTH, Direction.EAST.turnLeft());
        assertEquals(Direction.WEST, Direction.SOUTH.turnRight());
        assertEquals(Direction.EAST, Direction.SOUTH.turnLeft());
        assertEquals(Direction.NORTH, Direction.WEST.turnRight());
        assertEquals(Direction.SOUTH, Direction.WEST.turnLeft());
        assertEquals(1, Direction.EAST.dx());
        assertEquals(-1, Direction.SOUTH.dy());

        assertEquals("up", PenState.UP.toString());
        assertEquals("down", PenState.DOWN.toString());

        Command command = new Command(CommandType.Init, 7);
        assertEquals(CommandType.Init, command.type());
        assertEquals(7, command.arg());

        assertTrue(CommandType.valueOf("Pen_Up") == CommandType.Pen_Up);
        assertTrue(CommandType.values().length >= 10);

        Robot robot = new Robot();
        assertEquals(0, robot.getX());
        assertEquals(0, robot.getY());
        assertEquals(Direction.NORTH, robot.getFacing());
        assertEquals(PenState.UP, robot.getPen());

        robot.setPen(PenState.DOWN);
        robot.turnRight();
        robot.turnLeft();
        robot.setPosition(2, 1);

        assertEquals(2, robot.getX());
        assertEquals(1, robot.getY());
        assertEquals(Direction.NORTH, robot.getFacing());
        assertEquals(PenState.DOWN, robot.getPen());

        robot.reset();
        assertEquals(0, robot.getX());
        assertEquals(0, robot.getY());
        assertEquals(Direction.NORTH, robot.getFacing());
        assertEquals(PenState.UP, robot.getPen());
    }

    @Test
    void floorConstructorExceptionCoverage() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new Floor(0));
        assertTrue(ex.getMessage().contains("Grid size must be positive"));
    }

    @Test
    void mainInteractiveAndPrivatePathsCoverage() throws Exception {
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;

        ByteArrayInputStream in = new ByteArrayInputStream(
            String.join("\n",
                "BAD",
                "I 3",
                "D",
                "M 1",
                "U",
                "R",
                "L",
                "P",
                "C",
                "H",
                "Q"
            ).concat("\n").getBytes(StandardCharsets.UTF_8)
        );

        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outBytes, true, StandardCharsets.UTF_8);

        try {
            System.setIn(in);
            System.setOut(out);
            Main.main(new String[0]);
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }

        String output = outBytes.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Error: Invalid command format"));
        assertTrue(output.contains("Initialization successful. Floor size: 3x3."));
        assertTrue(output.contains("Pen Placed."));
        assertTrue(output.contains("Move accepted. Steps moved: 1."));
        assertTrue(output.contains("Pen Lifted."));
        assertTrue(output.contains("Turned Right. Facing: east."));
        assertTrue(output.contains("Turned Left. Facing: north."));
        assertTrue(output.contains("Printing floor:"));
        assertTrue(output.contains("Displaying status:"));

        Method executeAndPrint = Main.class.getDeclaredMethod("executeAndPrint", Simulator.class, Command.class);
        executeAndPrint.setAccessible(true);
        assertDoesNotThrow(() -> executeAndPrint.invoke(null, new Simulator(), new Command(CommandType.Quit, null)));
        assertDoesNotThrow(() -> executeAndPrint.invoke(null, new Simulator(), new Command(CommandType.History, null)));

        Method replay = Main.class.getDeclaredMethod("replay", List.class, CommandParser.class);
        replay.setAccessible(true);
        assertDoesNotThrow(() -> replay.invoke(null, List.of("Q", "H", "I 2", "D", "M 1"), new CommandParser()));
    }

    private static void runParserCase(String input, String expected, String assertion) {
        CommandParser parser = new CommandParser();

        if ("THROWS".equals(assertion)) {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> parser.parse(input));
            assertTrue(ex.getMessage().contains(expected));
            return;
        }

        Command cmd = parser.parse(input);
        String[] parts = expected.split("\\|", -1);
        CommandType expectedType = CommandType.valueOf(parts[0]);
        Integer expectedArg = "null".equals(parts[1]) ? null : Integer.valueOf(parts[1]);

        assertEquals(expectedType, cmd.type());
        assertEquals(expectedArg, cmd.arg());
    }

    private static void runFloorCase(String op, String input1, String input2, String input3, String expected, String assertion) {
        int n = Integer.parseInt(input1);
        Floor floor = new Floor(n);

        switch (op) {
            case "IN_BOUNDS" -> {
                boolean result = floor.inBounds(Integer.parseInt(input2), Integer.parseInt(input3));
                assertEquals(Boolean.parseBoolean(expected), result);
            }
            case "MARK_AND_CHECK" -> {
                int x = Integer.parseInt(input2);
                int y = Integer.parseInt(input3);
                if ("THROWS".equals(assertion)) {
                    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> floor.mark(x, y));
                    assertTrue(ex.getMessage().contains(expected));
                } else {
                    floor.mark(x, y);
                    assertEquals(Boolean.parseBoolean(expected), floor.isMarked(x, y));
                }
            }
            case "IS_MARKED" -> {
                int x = Integer.parseInt(input2);
                int y = Integer.parseInt(input3);
                IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> floor.isMarked(x, y));
                assertTrue(ex.getMessage().contains(expected));
            }
            case "CLEAR_AND_CHECK" -> {
                int x = Integer.parseInt(input2);
                int y = Integer.parseInt(input3);
                floor.mark(x, y);
                floor.clear();
                assertEquals(Boolean.parseBoolean(expected), floor.isMarked(x, y));
            }
            case "RENDER_CONTAINS" -> {
                int x = Integer.parseInt(input2);
                int y = Integer.parseInt(input3);
                floor.mark(x, y);
                assertTrue(floor.renderWithIndices().contains(expected));
                assertEquals(n, floor.size());
            }
            default -> throw new IllegalArgumentException("Unknown floor op: " + op);
        }
    }

    private static void runSimulatorCase(String op, String input1, String input2, String expected, String assertion) {
        Simulator sim = new Simulator();

        switch (op) {
            case "REQUIRE_INIT" -> {
                IllegalStateException ex = assertThrows(IllegalStateException.class, sim::requireInit);
                assertTrue(ex.getMessage().contains(expected));
            }
            case "IS_INITIALIZED" -> {
                boolean shouldInit = Boolean.parseBoolean(input1);
                if (shouldInit) {
                    sim.initialize(Integer.parseInt(input2));
                }
                assertEquals(Boolean.parseBoolean(expected), sim.isInitialized());
            }
            case "MOVE_STATUS" -> {
                applyScript(sim, input1);
                assertEquals(expected, sim.statusString());
            }
            case "FLOOR_CONTAINS" -> {
                applyScript(sim, input1);
                assertTrue(sim.floorString().contains(expected));
            }
            case "MOVE_NEGATIVE" -> {
                String[] parts = input1.split("\\|", -1);
                sim.initialize(Integer.parseInt(parts[0].substring(2)));
                int negative = Integer.parseInt(parts[1]);
                IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> sim.move(negative));
                assertTrue(ex.getMessage().contains(expected));
            }
            case "TURN_STATUS" -> {
                applyScript(sim, input1);
                assertEquals(expected, sim.statusString());
                assertNotNull(sim.snapshotRobot());
            }
            default -> throw new IllegalArgumentException("Unknown simulator op: " + op);
        }

        if ("CONTAINS".equals(assertion)) {
            assertFalse(expected.isBlank());
        }
    }

    private static void applyScript(Simulator sim, String script) {
        String[] commands = script.split("\\|");
        for (String raw : commands) {
            String token = raw.trim();
            if (token.isEmpty()) {
                continue;
            }

            if (token.startsWith("I:")) {
                sim.initialize(Integer.parseInt(token.substring(2)));
            } else if (token.equals("D")) {
                sim.penDown();
            } else if (token.equals("U")) {
                sim.penUp();
            } else if (token.equals("R")) {
                sim.turnRight();
            } else if (token.equals("L")) {
                sim.turnLeft();
            } else if (token.startsWith("M:")) {
                sim.move(Integer.parseInt(token.substring(2)));
            } else {
                throw new IllegalArgumentException("Unknown script token: " + token);
            }
        }
    }
}
