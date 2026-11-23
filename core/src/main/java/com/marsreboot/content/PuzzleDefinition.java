
package com.marsreboot.content;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PuzzleDefinition {
    private final String puzzleId;
    private final int rulesVersion;
    private final int fixtureVersion;
    private final List<ScenarioFixture> scenarios;

    public PuzzleDefinition(
        String puzzleId,
        int rulesVersion,
        int fixtureVersion,
        List<ScenarioFixture> scenarios) {
        if (puzzleId == null || puzzleId.isBlank()) {
            throw new IllegalArgumentException("Puzzle ID cannot be empty.");
        }

        if (rulesVersion < 1 || fixtureVersion < 1) {
            throw new IllegalArgumentException("Versions must be positive.");
        }

        if (scenarios == null || scenarios.isEmpty()) {
            throw new IllegalArgumentException("Puzzle must have at least one scenario.");
        }

        Set<String> scenarioIds = new HashSet<>();

        for (ScenarioFixture scenario : scenarios) {
            if (scenario == null) {
                throw new IllegalArgumentException("Scenario cannot be null.");
            }

            if (!scenarioIds.add(scenario.getScenarioId())) {
                throw new IllegalArgumentException("Duplicate scenario ID.");
            }
        }

        this.puzzleId = puzzleId;
        this.rulesVersion = rulesVersion;
        this.fixtureVersion = fixtureVersion;
        this.scenarios = List.copyOf(scenarios);
    }

    public String getPuzzleId() {
        return puzzleId;
    }

    public int getRulesVersion() {
        return rulesVersion;
    }

    public int getFixtureVersion() {
        return fixtureVersion;
    }

    public List<ScenarioFixture> getScenarios() {
        return scenarios;
    }
}
