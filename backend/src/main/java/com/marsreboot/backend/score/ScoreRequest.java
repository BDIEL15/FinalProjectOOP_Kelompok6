package com.marsreboot.backend.score;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Map;
import java.util.UUID;

/** Data skor yang dikirim game ke backend. */
public class ScoreRequest {

    @NotNull
    private UUID submissionId;

    @NotBlank
    @Pattern(regexp = "[A-Za-z0-9_.:-]{1,64}")
    private String puzzleId;

    @NotBlank
    @Pattern(regexp = "[A-Za-z0-9_.:-]{1,32}")
    private String rulesVersion;

    @NotBlank
    @Pattern(regexp = "[A-Za-z0-9_.:-]{1,32}")
    private String fixtureVersion;

    @NotNull
    @Min(0)
    private Integer score;

    // Contoh isi: actionCount dan waterUsed.
    @NotNull
    @Size(max = 12)
    private Map<@Pattern(regexp = "[A-Za-z][A-Za-z0-9_]{0,31}") String, @NotNull @Min(0) Integer> metrics;

    public ScoreRequest() {
        // Constructor kosong dipakai Jackson saat membaca JSON.
    }

    public UUID getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(UUID submissionId) {
        this.submissionId = submissionId;
    }

    public String getPuzzleId() {
        return puzzleId;
    }

    public void setPuzzleId(String puzzleId) {
        this.puzzleId = puzzleId;
    }

    public String getRulesVersion() {
        return rulesVersion;
    }

    public void setRulesVersion(String rulesVersion) {
        this.rulesVersion = rulesVersion;
    }

    public String getFixtureVersion() {
        return fixtureVersion;
    }

    public void setFixtureVersion(String fixtureVersion) {
        this.fixtureVersion = fixtureVersion;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Map<String, Integer> getMetrics() {
        return metrics;
    }

    public void setMetrics(Map<String, Integer> metrics) {
        this.metrics = metrics;
    }
}
