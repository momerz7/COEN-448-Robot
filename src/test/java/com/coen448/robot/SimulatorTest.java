package com.coen448.robot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class SimulatorTest {

    @Test
    void testInitialization(){
        Simulator sim = new Simulator();
        sim.initialize(5);
        Robot rob = sim.snapshotRobot();
        assertEquals(0, rob.getX());
        assertEquals(0, rob.getY());
        assertEquals(Direction.NORTH, rob.getFacing());
        assertEquals(PenState.UP, rob.getPen());

    }

    @Test
    void testPenDown(){
        Simulator sim = new Simulator();
        sim.initialize(5);
        sim.penDown();
        assertEquals(PenState.DOWN, sim.snapshotRobot().getPen());
        sim.penUp();
        assertEquals(PenState.UP, sim.snapshotRobot().getPen());

    }

    @Test
    void testLeftRight(){
        Simulator sim = new Simulator();
        sim.initialize(5);
        sim.turnRight();
        assertEquals(Direction.EAST, sim.snapshotRobot().getFacing());
        sim.turnLeft();
        assertEquals(Direction.NORTH, sim.snapshotRobot().getFacing());

    }

    @Test
    void testWithinBounds(){
        Simulator sim = new Simulator();
        sim.initialize(5);
        sim.move(3);
        Robot rob= sim.snapshotRobot();
        assertEquals(0, rob.getX());
        assertEquals(3, rob.getY());
    }

    @Test
    void testStopsAtBoundary(){
        Simulator sim = new Simulator();
        sim.initialize(5);
        sim.move(10);
        Robot rob = sim.snapshotRobot();
        assertEquals(0, rob.getX());
        assertEquals(4, rob.getY());

    }

    @Test void testStatusString(){
        Simulator sim = new Simulator();
        sim.initialize(5);
        String status = sim.statusString();
        assertTrue(status.contains("Position: 0, 0"));
        assertTrue(status.contains("Pen: up"));
        assertTrue(status.contains("Facing: north"));
    }

    @Test
    void testMarkingPenDown(){
        Simulator sim = new Simulator();
        sim.initialize(5);
        sim.penDown();
        sim.move(2);
        Floor floor = new Floor(5);
        String out = sim.floorString();
        assertTrue(out.contains("*"));
    }
    
}
