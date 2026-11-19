package com.marsreboot.robot;

import static com.marsreboot.robot.RobotSimulation.*;

public final class HarvestEvaluation implements EvaluationStrategy {
    @Override public Evaluation evaluate(Snapshot snapshot) {
        boolean complete = snapshot.remainingTargets().isEmpty();
        return new Evaluation(complete ? Outcome.SUCCESS : Outcome.INCOMPLETE,
            complete ? "Semua target panen selesai." : "Masih ada target panen.");
    }
}

