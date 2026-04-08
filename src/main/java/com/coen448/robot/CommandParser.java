package com.coen448.robot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandParser {
    private static final Pattern SINGLE_CHAR_COMMAND = Pattern.compile("^[UuDdRrLlPpCcQqHh]$");

    private static final Pattern INIT_COMMAND = Pattern.compile("^([IiMm])( ?)(\\d+)$");

    public Command parse(String line) {
        if (line == null) throw new IllegalArgumentException("Command cannot be null");
        String s = line.trim();

        if (s.isEmpty()) throw new IllegalArgumentException("Command cannot be empty");

        if (SINGLE_CHAR_COMMAND.matcher(s).matches()) {
            char c = Character.toUpperCase(s.charAt(0));
            return switch (c) {
                case 'U' -> new Command(CommandType.Pen_Up, null);
                case 'D' -> new Command(CommandType.Pen_Down, null);
                case 'R' -> new Command(CommandType.Right, null);
                case 'L' -> new Command(CommandType.Left, null);
                case 'P' -> new Command(CommandType.Print, null);
                case 'C' -> new Command(CommandType.Status, null);
                case 'Q' -> new Command(CommandType.Quit, null);
                case 'H' -> new Command(CommandType.History, null);
                default -> throw new IllegalArgumentException("Unknown command: " + s);
            };
        }

        Matcher m = INIT_COMMAND.matcher(s);
        if (m.matches()) {
            char c = Character.toUpperCase(m.group(1).charAt(0));
            int value = Integer.parseInt(m.group(3));

            if (c == 'I') {
                if (value <= 0) throw new IllegalArgumentException("Grid size must be non-negative");
                return new Command(CommandType.Init, value);
            } else {
                if (value <= 0) throw new IllegalArgumentException("Move distance must be non-negative");
                return new Command(CommandType.Move, value);
            }
        }

        throw new IllegalArgumentException("Invalid command format: " + s);
    }

}
