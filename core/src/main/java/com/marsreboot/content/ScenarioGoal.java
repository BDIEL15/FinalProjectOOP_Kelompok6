package com.marsreboot.content;

public class ScenarioGoal {
    private final int requiredHarvestCount;
    private final int requiredWaterCount;

    public ScenarioGoal(
        int requiredHarvestCount,
        int requiredWaterCount
    ) {
        if (requiredHarvestCount < 0 || requiredWaterCount < 0) {
            throw new IllegalArgumentException(
                "Goal cannot be negative."
            );
        }

        this.requiredHarvestCount = requiredHarvestCount;
        this.requiredWaterCount = requiredWaterCount;
    }

    public int getRequiredHarvestCount() {
        return requiredHarvestCount;
    }

    public int getRequiredWaterCount() {
        return requiredWaterCount;
    }
}
