package com.marsreboot.robot;

public record WaterCommand() implements RobotCommand {
    @Override public boolean execute(RobotSimulation simulation) { return simulation.water(); }
}

