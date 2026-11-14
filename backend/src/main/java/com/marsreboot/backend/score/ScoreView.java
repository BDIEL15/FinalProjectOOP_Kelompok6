package com.marsreboot.backend.score;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ScoreView(UUID submissionId, String puzzleId, String rulesVersion, String fixtureVersion, int score, Map<String, Integer> metrics, Instant submittedAt) {
    public static ScoreView from(ScoreEntity entity) {
        return new ScoreView(entity.getSubmissionId(), entity.getPuzzleId(), entity.getRulesVersion(), entity.getFixtureVersion(), entity.getScore(), Map.copyOf(entity.getMetrics()), entity.getSubmittedAt());
    }
}
