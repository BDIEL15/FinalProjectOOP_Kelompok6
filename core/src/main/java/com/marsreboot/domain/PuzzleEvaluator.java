
package com.marsreboot.domain;

import com.marsreboot.content.ScenarioGoal;
import com.marsreboot.content.ScenarioFixture;

public class PuzzleEvaluator {
    public boolean evaluate(
        ScenarioFixture fixture,
        ScenarioGoal goal,
        int harvestedCount,
        int wateredCount
    ) {
        if (fixture == null || goal == null) {
            throw new IllegalArgumentException(
                "Fixture and goal cannot be null."
            );
        }

        if (harvestedCount < 0 || wateredCount < 0) {
            throw new IllegalArgumentException(
                "Action counts cannot be negative."
            );
        }

        return harvestedCount >= goal.getRequiredHarvestCount()
            && wateredCount >= goal.getRequiredWaterCount();
    }
}
