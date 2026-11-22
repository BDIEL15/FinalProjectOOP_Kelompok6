package com.marsreboot.backend.score;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/** Endpoint skor — menerima request, lempar ke service, balas dengan JSON. */
@RestController
@RequestMapping("/api/scores")
@Validated
public class ScoreController {

    private final ScoreService scoreService;

    public ScoreController(ScoreService scoreService) {
        this.scoreService = scoreService;
    }

    /** POST /api/scores — simpan skor untuk akun yang sedang login. */
    @PostMapping
    public ScoreView submit(Authentication auth, @Valid @RequestBody ScoreRequest request) {
        return scoreService.submit(auth.getName(), request);
    }

    /** GET /api/scores/me — semua skor milik akun ini, terbaru dulu. */
    @GetMapping("/me")
    public List<ScoreView> mine(Authentication auth) {
        return scoreService.mine(auth.getName());
    }

    /** GET /api/scores/me/best — skor terbaik untuk puzzle dan versi aturan tertentu. */
    @GetMapping("/me/best")
    public List<ScoreView> best(
            Authentication auth,
            @RequestParam @NotBlank @Pattern(regexp = "[A-Za-z0-9_.:-]{1,64}") String puzzleId,
            @RequestParam @NotBlank @Pattern(regexp = "[A-Za-z0-9_.:-]{1,32}") String rulesVersion,
            @RequestParam @NotBlank @Pattern(regexp = "[A-Za-z0-9_.:-]{1,32}") String fixtureVersion) {
        return scoreService.personalBest(auth.getName(), puzzleId, rulesVersion, fixtureVersion);
    }

    /** GET /api/scores/leaderboard — 10 skor terbaik dari pemain berbeda. */
    @GetMapping("/leaderboard")
    public List<LeaderboardEntry> leaderboard(
            @RequestParam @NotBlank @Pattern(regexp = "[A-Za-z0-9_.:-]{1,64}") String puzzleId,
            @RequestParam @NotBlank @Pattern(regexp = "[A-Za-z0-9_.:-]{1,32}") String rulesVersion,
            @RequestParam @NotBlank @Pattern(regexp = "[A-Za-z0-9_.:-]{1,32}") String fixtureVersion) {
        return scoreService.leaderboard(puzzleId, rulesVersion, fixtureVersion);
    }
}
