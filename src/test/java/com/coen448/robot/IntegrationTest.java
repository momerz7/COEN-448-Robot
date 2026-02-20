package com.coen448.robot;

public class IntegrationTest {

    private static void runTest(String... inputs){

        Simulator sim = new Simulator();
        CommandParser parser = new CommandParser();

        for(String command : inputs){
        System.out.println("Command: " + command);

            try {
                Command cmd = parser.parse(command);

                if(cmd.type()==CommandType.History){
                    System.out.println("History cannnot be replayed in integration test");
                    continue;
                }

                executeTests(sim,cmd);
            
            } catch (Exception e) {
                
                System.out.println("Error: "+e.getMessage());

            }
        }

    }

    private static void executeTests(Simulator sim, Command cmd){

        switch(cmd.type()){

            case Init -> {
                sim.initialize(cmd.arg());
                System.out.println("Initialization successful. Floor size: " + cmd.arg() + "x" + cmd.arg() +".");
            }
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
            
            default -> {}
            
        }   

    }

    public static void main(String[] args){

        System.out.println("---Integration Test: Initialization---");
        runTest("I 10", "C");

        System.out.println("---Integration Test: Bad Initialization---");
        runTest("I 0", "I -5", "I abc");

        System.out.println("---Integration Test: Movement---");
        runTest("I 10", "D", "M 5", "C");

        System.out.println("---Integration Test: Turning---");
        runTest("I 10", "R", "L", "R", "R", "C");

        System.out.println("---Integration Test: Marking---");
        runTest("I 10", "D", "M 3", "P");

        System.out.println("---Integration Test: Print and Status---");
        runTest("I 10", "P", "C");

        System.out.println("---Integration Test: Initialization---");
        runTest("I 10","C", "D", "M 4", "R", "M 3", "U","M 2", "L", "R", "C","M 20", "P");

    }

}
