package com.marsreboot.robot;

import java.util.Objects;
import static com.marsreboot.robot.RobotSimulation.*;

/** Adds the young-harvest rule to the selected goal policy, including legacy callers. */
public final class MatureHarvestEvaluation implements EvaluationStrategy {
    private final EvaluationStrategy goals;
    public MatureHarvestEvaluation(EvaluationStrategy goals) { this.goals = Objects.requireNonNull(goals); }
    @Override public Evaluation evaluate(Snapshot snapshot) {
        if (snapshot.invalidHarvestCount() > 0)
            return new Evaluation(Outcome.INCOMPLETE,
                "Tidak lulus: mencoba memanen tanaman muda. Gunakan can_harvest() sebelum harvest().");
        return goals.evaluate(snapshot);
    }
}

