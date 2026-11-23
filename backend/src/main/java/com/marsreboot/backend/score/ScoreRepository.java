package com.marsreboot.backend.score;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Query skor ke database — nama method langsung jadi SQL oleh Spring JPA. */
public interface ScoreRepository extends JpaRepository<ScoreEntity, Long> {
    // Dipakai untuk cek duplikasi sebelum menyimpan kiriman baru.
    Optional<ScoreEntity> findByUserIdAndSubmissionId(Long userId, UUID submissionId);
    List<ScoreEntity> findAllByUserIdOrderBySubmittedAtDesc(Long userId);
    List<ScoreEntity> findAllByUserIdAndPuzzleIdAndRulesVersionAndFixtureVersion(
        Long userId, String puzzleId, String rulesVersion, String fixtureVersion
    );
    List<ScoreEntity> findAllByPuzzleIdAndRulesVersionAndFixtureVersionOrderByScoreAscSubmittedAtAsc(
        String puzzleId, String rulesVersion, String fixtureVersion
    );
}
