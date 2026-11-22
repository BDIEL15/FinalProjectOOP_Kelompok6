package com.marsreboot.backend.score;

import com.marsreboot.backend.user.UserRepository;
import com.marsreboot.backend.user.UserEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Validasi, cek duplikasi, dan persistensi skor. */
@Service
public class ScoreService {

    private final ScoreRepository scoreRepository;
    private final UserRepository userRepository;

    public ScoreService(ScoreRepository scoreRepository, UserRepository userRepository) {
        this.scoreRepository = scoreRepository;
        this.userRepository = userRepository;
    }

    /**
     * Simpan skor baru, atau kembalikan yang lama kalau submissionId sudah ada dengan isi sama.
     * submissionId berbeda tapi isi beda → 409, bukan duplikasi yang sah.
     */
    @Transactional
    public ScoreView submit(String username, ScoreRequest request) {
        Integer actionCount = request.getMetrics().get("actionCount");

        // score harus sama dengan actionCount, keduanya wajib ada.
        if (actionCount == null
                || !request.getMetrics().containsKey("waterUsed")
                || !actionCount.equals(request.getScore())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "metrics harus memuat actionCount dan waterUsed; score harus sama dengan actionCount");
        }

        Long userId = cariIdPengguna(username);
        Optional<ScoreEntity> existingScore = scoreRepository.findByUserIdAndSubmissionId(
                userId,
                request.getSubmissionId()
        );

        if (existingScore.isPresent()) {
            // Kiriman ulang yang sah — jangan buat duplikat.
            if (!isiSama(existingScore.get(), request)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "submissionId sudah dipakai untuk skor berbeda");
            }
            return ScoreView.from(existingScore.get());
        }

        ScoreEntity newScore = new ScoreEntity(userId, request);
        ScoreEntity savedScore = scoreRepository.save(newScore);
        return ScoreView.from(savedScore);
    }

    @Transactional(readOnly = true)
    public List<ScoreView> mine(String username) {
        Long userId = cariIdPengguna(username);
        List<ScoreEntity> savedScores = scoreRepository.findAllByUserIdOrderBySubmittedAtDesc(userId);
        List<ScoreView> result = new ArrayList<>();

        for (ScoreEntity score : savedScores) {
            result.add(ScoreView.from(score));
        }

        return result;
    }

    /** Kembalikan kosong jika belum pernah menyelesaikan puzzle ini. */
    @Transactional(readOnly = true)
    public List<ScoreView> personalBest(String username, String puzzleId, String rulesVersion, String fixtureVersion) {
        Long userId = cariIdPengguna(username);
        List<ScoreEntity> puzzleScores = scoreRepository.findAllByUserIdAndPuzzleIdAndRulesVersionAndFixtureVersion(
                userId,
                puzzleId,
                rulesVersion,
                fixtureVersion
        );
        ScoreEntity bestScore = null;

        for (ScoreEntity score : puzzleScores) {
            if (bestScore == null || lebihBaik(score, bestScore)) {
                bestScore = score;
            }
        }

        if (bestScore == null) {
            return List.of();
        }

        return List.of(ScoreView.from(bestScore));
    }

    /** Ambil sampai 10 pemain, masing-masing hanya memakai skor terbaiknya. */
    @Transactional(readOnly = true)
    public List<LeaderboardEntry> leaderboard(String puzzleId, String rulesVersion, String fixtureVersion) {
        List<ScoreEntity> scores = scoreRepository
                .findAllByPuzzleIdAndRulesVersionAndFixtureVersionOrderByScoreAscSubmittedAtAsc(
                        puzzleId, rulesVersion, fixtureVersion);
        List<LeaderboardEntry> result = new ArrayList<>();
        Set<Long> playerIds = new HashSet<>();

        for (ScoreEntity score : scores) {
            Long playerId = score.getUserId();

            if (!playerIds.add(playerId)) {
                continue;
            }

            userRepository.findById(playerId).ifPresent(user ->
                    result.add(new LeaderboardEntry(result.size() + 1, user.getUsername(), score.getScore())));

            if (result.size() == 10) {
                break;
            }
        }

        return result;
    }

    private Long cariIdPengguna(String username) {
        Optional<UserEntity> user = userRepository.findByUsername(username);

        if (user.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesi tidak valid");
        }

        return user.get().getId();
    }

    // Skor lebih kecil menang; kalau sama, yang lebih dulu dikirim menang.
    private boolean lebihBaik(ScoreEntity a, ScoreEntity b) {
        if (a.getScore() != b.getScore()) {
            return a.getScore() < b.getScore();
        }

        return a.getSubmittedAt().isBefore(b.getSubmittedAt());
    }

    private boolean isiSama(ScoreEntity lama, ScoreRequest baru) {
        if (!lama.getPuzzleId().equals(baru.getPuzzleId())) {
            return false;
        }
        if (!lama.getRulesVersion().equals(baru.getRulesVersion())) {
            return false;
        }
        if (!lama.getFixtureVersion().equals(baru.getFixtureVersion())) {
            return false;
        }
        if (lama.getScore() != baru.getScore()) {
            return false;
        }
        if (!lama.getMetrics().equals(baru.getMetrics())) {
            return false;
        }

        return true;
    }
}
