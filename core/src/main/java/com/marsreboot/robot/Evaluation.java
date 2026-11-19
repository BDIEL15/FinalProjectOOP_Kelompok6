package com.marsreboot.robot;

import java.util.Objects;
import com.marsreboot.robot.RobotSimulation.Outcome;

/** Evaluation cannot fabricate lifecycle/error states. */
public record Evaluation(Outcome outcome, String message) {
    public Evaluation {
        if (outcome != Outcome.SUCCESS && outcome != Outcome.INCOMPLETE)
            throw new IllegalArgumentException("Evaluation must be SUCCESS or INCOMPLETE.");
        Objects.requireNonNull(message, "message");
    }
}

