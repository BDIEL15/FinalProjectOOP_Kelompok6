package com.marsreboot.backend.score;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/scores")
@Validated
public class ScoreController {
    private final ScoreService scores;
    public ScoreController(ScoreService scores) { this.scores = scores; }

    @PostMapping
    public ScoreView submit(Authentication auth, @Valid @RequestBody ScoreRequest request) {
        return scores.submit(auth.getName(), request);
    }
    @GetMapping("/me")
    public List<ScoreView> mine(Authentication auth) { return scores.mine(auth.getName()); }
    @GetMapping("/me/best")
    public List<ScoreView> best(Authentication auth,
            @RequestParam @NotBlank @Pattern(regexp = "[A-Za-z0-9_.:-]{1,64}") String puzzleId,
            @RequestParam @NotBlank @Pattern(regexp = "[A-Za-z0-9_.:-]{1,32}") String rulesVersion,
            @RequestParam @NotBlank @Pattern(regexp = "[A-Za-z0-9_.:-]{1,32}") String fixtureVersion) {
        return scores.personalBest(auth.getName(), puzzleId, rulesVersion, fixtureVersion);
    }
}
