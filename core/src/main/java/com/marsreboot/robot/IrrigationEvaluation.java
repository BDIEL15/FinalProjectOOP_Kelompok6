package com.marsreboot.robot;

import static com.marsreboot.robot.RobotSimulation.*;

public final class IrrigationEvaluation implements EvaluationStrategy {
    @Override public Evaluation evaluate(Snapshot snapshot) {
        boolean complete = snapshot.remainingTargets().isEmpty();
        return new Evaluation(complete ? Outcome.SUCCESS : Outcome.INCOMPLETE,
            complete ? "Semua target penyiraman selesai." : "Masih ada petak kering yang harus disiram.");
    }
}

