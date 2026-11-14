package com.marsreboot.backend.score;

import com.marsreboot.backend.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.Comparator;
import java.util.List;

@Service
public class ScoreService {
    private final ScoreRepository scores;
    private final UserRepository users;
    public ScoreService(ScoreRepository scores, UserRepository users) { this.scores = scores; this.users = users; }

    @Transactional
    public ScoreView submit(String username, ScoreRequest request) {
        Integer actionCount = request.metrics().get("actionCount");
        if (actionCount == null || !request.metrics().containsKey("waterUsed") || !actionCount.equals(request.score())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "metrics harus memuat actionCount dan waterUsed; score harus sama dengan actionCount");
        }
        Long userId = userId(username);
        var prior = scores.findByUserIdAndSubmissionId(userId, request.submissionId());
        if (prior.isPresent()) {
            ScoreEntity existing = prior.get();
            if (!samePayload(existing, request)) throw new ResponseStatusException(HttpStatus.CONFLICT, "submissionId sudah dipakai untuk skor berbeda");
            return ScoreView.from(existing);
        }
        return ScoreView.from(scores.save(new ScoreEntity(userId, request)));
    }

    @Transactional(readOnly = true)
    public List<ScoreView> mine(String username) {
        return scores.findAllByUserIdOrderBySubmittedAtDesc(userId(username)).stream().map(ScoreView::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ScoreView> personalBest(String username, String puzzleId, String rulesVersion, String fixtureVersion) {
        return scores.findAllByUserIdAndPuzzleIdAndRulesVersionAndFixtureVersion(userId(username), puzzleId, rulesVersion, fixtureVersion)
                .stream().min(Comparator.comparingInt(ScoreEntity::getScore).thenComparing(ScoreEntity::getSubmittedAt))
                .map(ScoreView::from).stream().toList();
    }

    private Long userId(String username) {
        return users.findByUsername(username).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesi tidak valid")).getId();
    }
    private boolean samePayload(ScoreEntity old, ScoreRequest request) {
        return old.getPuzzleId().equals(request.puzzleId()) && old.getRulesVersion().equals(request.rulesVersion())
                && old.getFixtureVersion().equals(request.fixtureVersion()) && old.getScore() == request.score()
                && old.getMetrics().equals(request.metrics());
    }
}
