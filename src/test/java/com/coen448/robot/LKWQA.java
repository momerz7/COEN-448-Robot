package com.coen448.robot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LKWQA {

    private final CommandParser parser = new CommandParser();

    // ---------- All-Definitions (AD) tests ----------

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

    // ---------- All-Uses (AU) tests ----------

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

    // ---------- All-DU tests ----------

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

    // ---------- Weyuker Axiom 7 (Antidecomposition) tests ----------

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
