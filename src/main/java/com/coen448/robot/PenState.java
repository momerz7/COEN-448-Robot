package com.coen448.robot;

public enum PenState {
    
    UP,DOWN;

    @Override
    public String toString(){
        return name().toLowerCase();
    }

}
