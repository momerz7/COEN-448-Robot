package com.coen448.robot;


public class Robot {

    private int x;
    private int y;
    private Direction facing;
    private PenState pen;
    


    public Robot() {
        reset();
    } 

    public void reset(){
        this.x = 0;
        this.y =0;
        this.facing = Direction.NORTH;
        this.pen= PenState.UP;

    }

    public int getX(){
        return this.x;  
    }
    public int getY(){
        return this.y;
    }  
    public Direction getFacing(){
        return this.facing;
    }
    public PenState getPen(){
        return this.pen;
    }

    public void setPen(PenState pen){
        this.pen = pen;
    }

    public void turnRight(){
        this.facing=this.facing.turnRight();
    }
    public void turnLeft(){
        this.facing=this.facing.turnLeft();
    }

    void setPosition(int newX,int newY){

        this.x = newX;
        this.y = newY;
    }

}
