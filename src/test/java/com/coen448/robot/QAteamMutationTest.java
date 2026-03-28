package com.coen448.robot;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class QAteamMutationTest {

    private final CommandParser parser = new CommandParser();

    /* Test for Initialization  */

    @Test
    void testIntialization(){
        Command command = parser.parse("I 10");
        assertEquals(CommandType.Init, command.type());
        assertEquals(10,command.arg());
    }

    @Test
    void testIntializationNoSpace(){
        Command command = parser.parse("I10");
        assertEquals(CommandType.Init, command.type());
        assertEquals(10,command.arg());
    }

    @Test
    void testIntializationLowerCase(){
        Command command = parser.parse("i 10");
        assertEquals(CommandType.Init, command.type());
        assertEquals(10,command.arg());
    }

    @Test
    void testInitializationZero(){ assertThrows(IllegalArgumentException.class, () -> parser.parse("I 0")); }

    @Test
    void testInitializationNegative(){ assertThrows(IllegalArgumentException.class, () -> parser.parse("I -10")); }


    /* Test for Movement */

    @Test
    void testMove(){
        Command command = parser.parse("M 5");
        assertEquals(CommandType.Move, command.type());
        assertEquals(5,command.arg());
    }

    @Test
    void testMoveNoSpace(){
        Command command = parser.parse("M5");
        assertEquals(CommandType.Move, command.type());
        assertEquals(5,command.arg());
    }

    @Test
    void testMoveLowerCase(){
        Command command = parser.parse("m 5");
        assertEquals(CommandType.Move, command.type());
        assertEquals(5,command.arg());
    }

    @Test
    void testMoveZero(){
        Command command = parser.parse("M 0");
        assertEquals(CommandType.Move, command.type());
        assertEquals(0,command.arg());
    }

    @Test
    void testMoveNegative(){ assertThrows(IllegalArgumentException.class, () -> parser.parse("M -5")); }

    /* Test for commands */

    @Test
    void testPenUp(){
        Command command = parser.parse("U");
        assertEquals(CommandType.Pen_Up, command.type());
        assertNull(command.arg());
    }

    @Test
    void testPenDown(){
        Command command = parser.parse("D");
        assertEquals(CommandType.Pen_Down, command.type());
        assertNull(command.arg());
    }

    @Test
    void testTurnRight(){
        Command command = parser.parse("R");
        assertEquals(CommandType.Right, command.type());
        assertNull(command.arg());
    }

    @Test
    void testTurnLeft(){
        Command command = parser.parse("L");
        assertEquals(CommandType.Left, command.type());
        assertNull(command.arg());
    }

    @Test
    void testPrint(){
        Command command = parser.parse("P");
        assertEquals(CommandType.Print, command.type());
        assertNull(command.arg());
    }

    @Test
    void testStatus(){
        Command command = parser.parse("C");
        assertEquals(CommandType.Status, command.type());
        assertNull(command.arg());
    }

    @Test
    void testQuit(){
        Command command = parser.parse("Q");
        assertEquals(CommandType.Quit, command.type());
        assertNull(command.arg());
    }

    @Test
    void testHistory(){
        Command command = parser.parse("H");
        assertEquals(CommandType.History, command.type());
        assertNull(command.arg());
    }

    /* Test for invalid commands */

    @Test
    void testNullInput(){assertThrows(IllegalArgumentException.class, () -> parser.parse(null));}

    @Test
    void testEmptyInput(){assertThrows(IllegalArgumentException.class, () -> parser.parse("   "));}

    @Test
    void testInvalidInput(){assertThrows(IllegalArgumentException.class, () -> parser.parse("M 5 7"));}

}
