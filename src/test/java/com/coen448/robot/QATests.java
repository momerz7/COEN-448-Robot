package com.coen448.robot;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.MethodSource;

class QATests {

    private final CommandParser parser = new CommandParser();

    // ---------- Code Coverage Testing ----------

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

    // ---------- Mutation Testing ----------

    @Test
    void testIntialization() {
        Command command = parser.parse("I 10");
        assertEquals(CommandType.Init, command.type());
        assertEquals(10, command.arg());
    }

    @Test
    void testIntializationNoSpace() {
        Command command = parser.parse("I10");
        assertEquals(CommandType.Init, command.type());
        assertEquals(10, command.arg());
    }

    @Test
    void testIntializationLowerCase() {
        Command command = parser.parse("i 10");
        assertEquals(CommandType.Init, command.type());
        assertEquals(10, command.arg());
    }

    @Test
    void testInitializationZero() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse("I 0"));
    }

    @Test
    void testInitializationNegative() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse("I -10"));
    }

    @Test
    void testMove() {
        Command command = parser.parse("M 5");
        assertEquals(CommandType.Move, command.type());
        assertEquals(5, command.arg());
    }

    @Test
    void testMoveNoSpace() {
        Command command = parser.parse("M5");
        assertEquals(CommandType.Move, command.type());
        assertEquals(5, command.arg());
    }

    @Test
    void testMoveLowerCase() {
        Command command = parser.parse("m 5");
        assertEquals(CommandType.Move, command.type());
        assertEquals(5, command.arg());
    }

    @Test
    void testMoveZero() {
        Command command = parser.parse("M 0");
        assertEquals(CommandType.Move, command.type());
        assertEquals(0, command.arg());
    }

    @Test
    void testMoveNegative() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse("M -5"));
    }

    @Test
    void testPenUp() {
        Command command = parser.parse("U");
        assertEquals(CommandType.Pen_Up, command.type());
        assertNull(command.arg());
    }

    @Test
    void testPenDown() {
        Command command = parser.parse("D");
        assertEquals(CommandType.Pen_Down, command.type());
        assertNull(command.arg());
    }

    @Test
    void testTurnRight() {
        Command command = parser.parse("R");
        assertEquals(CommandType.Right, command.type());
        assertNull(command.arg());
    }

    @Test
    void testTurnLeft() {
        Command command = parser.parse("L");
        assertEquals(CommandType.Left, command.type());
        assertNull(command.arg());
    }

    @Test
    void testPrint() {
        Command command = parser.parse("P");
        assertEquals(CommandType.Print, command.type());
        assertNull(command.arg());
    }

    @Test
    void testStatus() {
        Command command = parser.parse("C");
        assertEquals(CommandType.Status, command.type());
        assertNull(command.arg());
    }

    @Test
    void testQuit() {
        Command command = parser.parse("Q");
        assertEquals(CommandType.Quit, command.type());
        assertNull(command.arg());
    }

    @Test
    void testHistory() {
        Command command = parser.parse("H");
        assertEquals(CommandType.History, command.type());
        assertNull(command.arg());
    }

    @Test
    void testNullInput() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(null));
    }

    @Test
    void testEmptyInput() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse("   "));
    }

    @Test
    void testInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse("M 5 7"));
    }

    // ---------- Data Flow-Control Testing ----------

    @Test
    @DisplayName("AD1: line def reaches null-guard use and throws")
    void ad1_lineNull_guardUse() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(null));
    }

    @Test
    @DisplayName("AD2: s def reaches isEmpty use and throws")
    void ad2_sDef_reachesEmptyGuard() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse("   "));
    }

    @Test
    @DisplayName("AD3: c def (single-char branch) reaches switch use")
    void ad3_singleChar_cDef_usedBySwitch() {
        Command cmd = parser.parse("u");
        assertEquals(CommandType.Pen_Up, cmd.type());
        assertEquals(null, cmd.arg());
    }

    @Test
    @DisplayName("AD4: m/c/value defs (init branch) all reach uses")
    void ad4_init_defsAllUsed() {
        Command cmd = parser.parse("I 7");
        assertEquals(CommandType.Init, cmd.type());
        assertEquals(7, cmd.arg());
    }

    @Test
    @DisplayName("AD5: value def reaches move return use")
    void ad5_move_valueUsed() {
        Command cmd = parser.parse("M0");
        assertEquals(CommandType.Move, cmd.type());
        assertEquals(0, cmd.arg());
    }

    static Stream<Arguments> singleCharCases() {
        return Stream.of(
                Arguments.of("U", CommandType.Pen_Up),
                Arguments.of("D", CommandType.Pen_Down),
                Arguments.of("R", CommandType.Right),
                Arguments.of("L", CommandType.Left),
                Arguments.of("P", CommandType.Print),
                Arguments.of("C", CommandType.Status),
                Arguments.of("Q", CommandType.Quit),
                Arguments.of("H", CommandType.History)
        );
    }

    @ParameterizedTest(name = "AU1-{index}: parse({0}) -> {1}")
    @MethodSource("singleCharCases")
    void au1_allSingleCharSwitchUses(String input, CommandType expectedType) {
        Command cmd = parser.parse(input.toLowerCase());
        assertEquals(expectedType, cmd.type());
        assertEquals(null, cmd.arg());
    }

    @Test
    @DisplayName("AU2: m.group and Integer.parseInt uses for Init")
    void au2_initGroupUses() {
        Command cmd = parser.parse("i9");
        assertEquals(CommandType.Init, cmd.type());
        assertEquals(9, cmd.arg());
    }

    @Test
    @DisplayName("AU3: m.group and Integer.parseInt uses for Move")
    void au3_moveGroupUses() {
        Command cmd = parser.parse("m 12");
        assertEquals(CommandType.Move, cmd.type());
        assertEquals(12, cmd.arg());
    }

    @Test
    @DisplayName("AU4: s def reaches invalid-format throw use")
    void au4_invalidFormat_sUseAtTerminalThrow() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse("X9"));
    }

    @Test
    @DisplayName("DU1: s@13 -> use@17 (single-char matcher)")
    void du1_s13_to_17() {
        Command cmd = parser.parse("Q");
        assertEquals(CommandType.Quit, cmd.type());
    }

    @Test
    @DisplayName("DU2: s@13 -> use@32 (init matcher)")
    void du2_s13_to_32() {
        Command cmd = parser.parse("I1");
        assertEquals(CommandType.Init, cmd.type());
    }

    @Test
    @DisplayName("DU3: c@18 -> use@19 -> case@27")
    void du3_c18_singleBranch_toHistoryCase() {
        Command cmd = parser.parse("h");
        assertEquals(CommandType.History, cmd.type());
    }

    @Test
    @DisplayName("DU4: m@32 -> use@33, c@34 -> use@37 (I-branch)")
    void du4_m32_and_c34_to_initDecision() {
        Command cmd = parser.parse("I2");
        assertEquals(CommandType.Init, cmd.type());
        assertEquals(2, cmd.arg());
    }

    @Test
    @DisplayName("DU5: value@35 -> use@39 (Init return)")
    void du5_value35_to_initReturn() {
        Command cmd = parser.parse("I3");
        assertEquals(CommandType.Init, cmd.type());
        assertEquals(3, cmd.arg());
    }

    @Test
    @DisplayName("DU6: value@35 -> use@42 (Move return)")
    void du6_value35_to_moveReturn() {
        Command cmd = parser.parse("M4");
        assertEquals(CommandType.Move, cmd.type());
        assertEquals(4, cmd.arg());
    }

    @Test
    @DisplayName("DU7: s@13 -> use@46 (invalid terminal throw)")
    void du7_s13_to_46() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse("I-1"));
    }

    private CommandType integratedPlanP(String input) {
        Command cmd = parser.parse(input);
        return cmd.type();
    }

    @Test
    @DisplayName("AX7-P1: T_P is adequate for integrated P (type-level behavior)")
    void ax7_p1_typeLevelAdequacyForProgramP() {
        assertEquals(CommandType.Pen_Up, integratedPlanP("u"));
        assertEquals(CommandType.Init, integratedPlanP("I5"));
        assertEquals(CommandType.Move, integratedPlanP("M0"));
        assertEquals(CommandType.History, integratedPlanP("h"));
        assertThrows(IllegalArgumentException.class, () -> integratedPlanP("bad"));
    }

    @Test
    @DisplayName("AX7-Q1: T_Q adds component checks (arg and normalization) for Q=parse")
    void ax7_q1_componentAdequacyForQ() {
        Command init = parser.parse("  i 15  ");
        Command move = parser.parse("m9");

        assertEquals(CommandType.Init, init.type());
        assertEquals(15, init.arg());

        assertEquals(CommandType.Move, move.type());
        assertEquals(9, move.arg());
    }
}