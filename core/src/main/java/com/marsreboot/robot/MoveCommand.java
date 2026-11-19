package com.marsreboot.robot;

/** Direction is checked by the receiver so invalid calls retain their domain cost/error. */
public record MoveCommand(String direction) implements RobotCommand {
    @Override public boolean execute(RobotSimulation simulation) { return simulation.move(direction); }
}

