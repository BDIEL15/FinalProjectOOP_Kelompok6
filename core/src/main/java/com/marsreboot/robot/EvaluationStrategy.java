package com.marsreboot.robot;

/** Pure evaluation of an immutable domain snapshot; never mutates or spends actions. */
@FunctionalInterface
public interface EvaluationStrategy {
    Evaluation evaluate(RobotSimulation.Snapshot snapshot);
}

