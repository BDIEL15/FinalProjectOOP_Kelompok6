package com.marsreboot.backend.score;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "score_submissions", uniqueConstraints = @UniqueConstraint(name = "uk_score_submission_owner_id", columnNames = {"user_id", "submission_id"}))
public class ScoreEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "user_id", nullable = false) private Long userId;
    @Column(name = "submission_id", nullable = false, length = 36) private UUID submissionId;
    @Column(nullable = false, length = 64) private String puzzleId;
    @Column(nullable = false, length = 32) private String rulesVersion;
    @Column(nullable = false, length = 32) private String fixtureVersion;
    @Column(nullable = false) private int score;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "score_metrics", joinColumns = @JoinColumn(name = "submission_pk"))
    @MapKeyColumn(name = "metric_key", length = 32)
    @Column(name = "metric_value", nullable = false)
    private Map<String, Integer> metrics = new HashMap<>();
    @Column(nullable = false) private Instant submittedAt;

    protected ScoreEntity() {}
    public ScoreEntity(Long userId, ScoreRequest request) {
        this.userId = userId; this.submissionId = request.submissionId(); this.puzzleId = request.puzzleId();
        this.rulesVersion = request.rulesVersion(); this.fixtureVersion = request.fixtureVersion();
        this.score = request.score(); this.metrics = new HashMap<>(request.metrics()); this.submittedAt = Instant.now();
    }
    public UUID getSubmissionId() { return submissionId; }
    public Long getUserId() { return userId; }
    public String getPuzzleId() { return puzzleId; }
    public String getRulesVersion() { return rulesVersion; }
    public String getFixtureVersion() { return fixtureVersion; }
    public int getScore() { return score; }
    public Map<String, Integer> getMetrics() { return metrics; }
    public Instant getSubmittedAt() { return submittedAt; }
}
