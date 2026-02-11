package com.coen448.robot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class RobotTest {
    
    @Test
    void testInitialization(){

        Robot rob = new Robot();
        assertEquals(0, rob.getX());
        assertEquals(0, rob.getY());
        assertEquals(Direction.NORTH, rob.getFacing());
        assertEquals(PenState.UP, rob.getPen());

    }

    @Test
    void testLeft(){
        Robot rob = new Robot();
        rob.turnLeft();
        assertEquals(Direction.WEST,rob.getFacing());
        rob.turnLeft();
        assertEquals(Direction.SOUTH,rob.getFacing());
    }

    @Test
    void testRight(){
        Robot rob = new Robot();
        rob.turnRight();
        assertEquals(Direction.EAST,rob.getFacing());
        rob.turnRight();
        assertEquals(Direction.SOUTH,rob.getFacing());
    }

    @Test
    void testPenDown(){
        Robot rob = new Robot();
        rob.setPen(PenState.DOWN);
        assertEquals(PenState.DOWN, rob.getPen());
    }

    @Test 
    void testPosition(){
        Robot rob = new Robot();
        rob.setPosition(3, 4);
        assertEquals(3,rob.getX());
        assertEquals(4,rob.getY());
    }

}
