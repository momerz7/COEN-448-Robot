package com.coen448.robot;


import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args){

        Simulator sim = new Simulator();
        CommandParser parser = new CommandParser();
        List<String> history = new ArrayList<>();

        try(Scanner sc = new Scanner(System.in)){

            while(true){
                System.out.print("Enter command: ");
                String line = sc.nextLine();

                try {

                    Command cmd = parser.parse(line);
                    if(cmd.type()==CommandType.History){

                        replay(history,parser);
                        continue;

                    }

                    history.add(line.trim());

                    if(cmd.type()==CommandType.Quit){ break ;}

                    executeAndPrint(sim,cmd);
                    
                } catch (Exception e) {
                    System.out.println("Error: "+e.getMessage());
                }
            }

        }

    }


    private static void replay(List<String> history, CommandParser parser){

        Simulator replaysim = new Simulator();

        for (String pastLine : history){
            Command pastCmd = parser.parse(pastLine);

            if(pastCmd.type()==CommandType.Quit || pastCmd.type() == CommandType.History){
                continue;
            }

            executeAndPrint(replaysim,pastCmd);
        }

    }

    private static void executeAndPrint(Simulator sim, Command cmd){

        switch(cmd.type()){

            case Pen_Up -> {
                sim.penUp();
                System.out.println("Pen Lifted.");
            }
            case Pen_Down -> {
                sim.penDown();
                System.out.println("Pen Placed.");
            }
            case Right -> {
                sim.turnRight();
                System.out.println("Turned Right. Facing: " + sim.snapshotRobot().getFacing().toString().toLowerCase()+".");
            }
            case Left -> {
                sim.turnLeft();
                System.out.println("Turned Left. Facing: " + sim.snapshotRobot().getFacing().toString().toLowerCase()+".");
            }
            case Move -> {
                sim.move(cmd.arg());
                System.out.println("Move accepted. Steps moved: " + cmd.arg() +".");
            }
            case Print -> {
                System.out.println("Printing floor:");
                System.out.print(sim.floorString());
            }
            case Status -> {
                System.out.println("Displaying status:");
                System.out.println(sim.statusString());
            }
            case Quit -> {/*handled in main loop */ }
            case Init -> {
                sim.initialize(cmd.arg());
                System.out.println("Initialization successful. Floor size: " + cmd.arg() + "x" + cmd.arg() +".");
            }
            case History -> {/*handled in main loop */ }

        }

    }
    
}
