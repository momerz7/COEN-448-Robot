package com.coen448.robot;

//Executes commands against Robot + Floor (no consle I/O here).
//Designed to be unit-testable

public class Simulator {
    
    private final Robot robot = new Robot();
    private Floor floor = null;

    public boolean isInitialized(){ return floor != null;}

    public void initialize(int n){

        this.floor = new Floor(n);
        this.robot.reset();
    }

    public void requireInit(){
        if (floor==null) {throw new IllegalStateException("System not initialized. Use I n first.");}
    }

    public void penUp(){
        requireInit();
        robot.setPen(PenState.UP);
    }

    public void penDown(){
        requireInit();
        robot.setPen(PenState.DOWN);
    }

    public void turnLeft(){
        requireInit();
        robot.turnLeft();;
    }

    public void turnRight(){
        requireInit();
        robot.turnRight();
    }

    public String statusString(){
        requireInit();
        return String.format("Position: %d, %d - Pen: %s - Facing: %s",
            robot.getX(), robot.getY(), robot.getPen(), robot.getFacing()
        );
    }

    public String floorString(){
        requireInit();
        return floor.renderWithIndices();
    }

    public Robot snapshotRobot(){
        //getters might be better for tests
        return robot;
    }
    
    /**
     * Move forward s spaces, where s is a non-negative integer.
     * Boundary-safe: if the next step leaves the grid then stop early
     */

    public void move(int s){

        requireInit();
        if (s<0){throw new IllegalArgumentException("M s requires s >=0");}

        if(robot.getPen() == PenState.DOWN) { floor.mark(robot.getX(), robot.getY());}

        for(int i=0; i<s; i++){

            int nx = robot.getX() + robot.getFacing().dx();
            int ny = robot.getY() + robot.getFacing().dy();

            if(!floor.inBounds(nx, ny)){ break;}

            robot.setPosition(nx,ny);

            if(robot.getPen()==PenState.DOWN) {floor.mark(nx,ny);}

        }

    }

}
