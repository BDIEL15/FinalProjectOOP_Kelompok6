package com.marsreboot.content;

import java.util.List;

public record IrrigationPuzzleConfig(
    String puzzleId,
    int rulesVersion,
    int fixtureVersion,
    int width,
    int height,
    int initialWaterSupply,
    int actionLimit,
    int apiCallLimit,
    List<List<Integer>> dryTilePositions
) {
    public IrrigationPuzzleConfig {
        if (puzzleId == null || puzzleId.isBlank()) {
            throw new IllegalArgumentException("Puzzle ID cannot be empty.");
        }

        if (rulesVersion < 1 || fixtureVersion < 1
            || width < 1 || height < 1
            || initialWaterSupply < 0
            || actionLimit < 1 || apiCallLimit < 1) {
            throw new IllegalArgumentException("Invalid irrigation configuration.");
        }

        if (dryTilePositions == null || dryTilePositions.isEmpty()) {
            throw new IllegalArgumentException("At least one scenario is required.");
        }

        for (List<Integer> positions : dryTilePositions) {
            if (positions == null) {
                throw new IllegalArgumentException("Dry tile positions cannot be null.");
            }

            for (Integer position : positions) {
                if (position == null || position < 0 || position >= width * height) {
                    throw new IllegalArgumentException("Invalid dry tile position.");
                }
            }
        }

        dryTilePositions = dryTilePositions.stream()
            .map(List::copyOf)
            .toList();
    }
}
