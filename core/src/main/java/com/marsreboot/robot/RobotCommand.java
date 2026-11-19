package com.marsreboot.robot;

/** One robot action. Domain validation and accounting remain in the receiver. */
public interface RobotCommand {
    boolean execute(RobotSimulation simulation);
}

