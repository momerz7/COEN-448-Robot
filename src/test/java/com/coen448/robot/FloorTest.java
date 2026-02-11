package com.coen448.robot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class FloorTest {

    /* Test for constructor */
    @Test
    void testConstructor(){
        Floor floor = new Floor(5);
        assertEquals(5, floor.size());
    }

    @Test
    void testConstructorZero(){ assertThrows(IllegalArgumentException.class, () -> new Floor(0));}

    @Test
    void testCOnstructorNegative(){ assertThrows(IllegalArgumentException.class, () -> new Floor(-5));}

    /* Test for inBounds */
    @Test
    void testInBounds(){
        Floor floor = new Floor(5);
        assertTrue(floor.inBounds(0,0));
        assertTrue(floor.inBounds(4,4));
    }

    @Test
    void testNotInBounds(){
        Floor floor = new Floor(5);
        assertFalse(floor.inBounds(-1,0));
        assertFalse(floor.inBounds(0,-1));
        assertFalse(floor.inBounds(5,0));
        assertFalse(floor.inBounds(0,5));
    }

    /* Test for mark */

    @Test
    void testMark(){
        Floor floor = new Floor(5);
        floor.mark(2,3);
        assertTrue(floor.isMarked(2,3));
    }

    @Test
    void testMarkOutOfBounds(){
        Floor floor = new Floor(5);
        assertThrows(IllegalArgumentException.class, () -> floor.mark(10,10));
    }

    @Test
    void testIsMarkOutOfBounds(){
        Floor floor = new Floor(5);
        assertThrows(IllegalArgumentException.class, () -> floor.isMarked(-1,0));
    }


    /* Test for clear */
    @Test
    void testClear(){
        Floor floor = new Floor(5);
        floor.mark(1,1);
        floor.mark(2,2);
        floor.clear();
        assertFalse(floor.isMarked(1, 1));
        assertFalse(floor.isMarked(2, 2));
    }

    /* Test for render indices */

    @Test
    void testRenderWithIndices(){
        Floor floor = new Floor(3);
        String out = floor.renderWithIndices();
        assertTrue(out.contains("0"));
        assertTrue(out.contains("1"));
        assertTrue(out.contains("2"));
    }

    @Test
    void testRenderMarkedCells(){
        Floor floor = new Floor(3);
        floor.mark(1,1);
        String out =floor.renderWithIndices();
        assertTrue(out.contains("*"));
    }



}


