package com.marsreboot.backend.score;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Map;
import java.util.UUID;

public record ScoreRequest(
        @NotNull UUID submissionId,
        @NotBlank @Pattern(regexp = "[A-Za-z0-9_.:-]{1,64}") String puzzleId,
        @NotBlank @Pattern(regexp = "[A-Za-z0-9_.:-]{1,32}") String rulesVersion,
        @NotBlank @Pattern(regexp = "[A-Za-z0-9_.:-]{1,32}") String fixtureVersion,
        @NotNull @Min(0) Integer score,
        @NotNull @Size(max = 12) Map<@Pattern(regexp = "[A-Za-z][A-Za-z0-9_]{0,31}") String, @NotNull @Min(0) Integer> metrics) {}
