package com.marsreboot.backend.score;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScoreRepository extends JpaRepository<ScoreEntity, Long> {
    Optional<ScoreEntity> findByUserIdAndSubmissionId(Long userId, UUID submissionId);
    List<ScoreEntity> findAllByUserIdOrderBySubmittedAtDesc(Long userId);
    List<ScoreEntity> findAllByUserIdAndPuzzleIdAndRulesVersionAndFixtureVersion(Long userId, String puzzleId, String rulesVersion, String fixtureVersion);
}
