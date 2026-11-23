package com.marsreboot.backend.score;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/** Data skor yang dikirim kembali dari backend ke game. */
public class ScoreView {

    private UUID submissionId;
    private String puzzleId;
    private String rulesVersion;
    private String fixtureVersion;
    private int score;
    private Map<String, Integer> metrics;
    private Instant submittedAt;

    public ScoreView(UUID submissionId, String puzzleId, String rulesVersion,
                     String fixtureVersion, int score, Map<String, Integer> metrics,
                     Instant submittedAt) {
        this.submissionId = submissionId;
        this.puzzleId = puzzleId;
        this.rulesVersion = rulesVersion;
        this.fixtureVersion = fixtureVersion;
        this.score = score;
        this.metrics = metrics;
        this.submittedAt = submittedAt;
    }

    public static ScoreView from(ScoreEntity entity) {
        return new ScoreView(
                entity.getSubmissionId(),
                entity.getPuzzleId(),
                entity.getRulesVersion(),
                entity.getFixtureVersion(),
                entity.getScore(),
                Map.copyOf(entity.getMetrics()),
                entity.getSubmittedAt()
        );
    }

    public UUID getSubmissionId() {
        return submissionId;
    }

    public String getPuzzleId() {
        return puzzleId;
    }

    public String getRulesVersion() {
        return rulesVersion;
    }

    public String getFixtureVersion() {
        return fixtureVersion;
    }

    public int getScore() {
        return score;
    }

    public Map<String, Integer> getMetrics() {
        return metrics;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }
}
