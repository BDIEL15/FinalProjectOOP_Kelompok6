package com.marsreboot.robot;

public record HarvestCommand() implements RobotCommand {
    @Override public boolean execute(RobotSimulation simulation) { return simulation.harvest(); }
}

