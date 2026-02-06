package com.coen448.robot;

public class Command {
    public record Command(CommandType type, Integer arg) {}
}
