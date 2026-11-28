package com.marsreboot.content;

import java.util.List;

public record HarvestPuzzleConfig(
    String puzzleId,
    int rulesVersion,
    int fixtureVersion,
    int width,
    int target,
    int actionLimit,
    List<TileFixture.CropState> cropLayout
) {
    public HarvestPuzzleConfig {
        if (puzzleId == null || puzzleId.isBlank()) {
            throw new IllegalArgumentException("Puzzle ID cannot be empty.");
        }

        if (width < 1 || target < 0 || actionLimit < 1) {
            throw new IllegalArgumentException("Invalid puzzle configuration.");
        }

        if (cropLayout == null || cropLayout.size() != width) {
            throw new IllegalArgumentException("Crop layout must match puzzle width.");
        }

        cropLayout = List.copyOf(cropLayout);
    }
}
