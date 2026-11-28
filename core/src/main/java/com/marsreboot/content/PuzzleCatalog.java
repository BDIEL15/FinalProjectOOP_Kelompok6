package com.marsreboot.content;

import java.util.List;

public final class PuzzleCatalog {

    private static final HarvestPuzzleFactory HARVEST_FACTORY =
        new HarvestPuzzleFactory();

    private static final IrrigationPuzzleFactory IRRIGATION_FACTORY =
        new IrrigationPuzzleFactory();

    private PuzzleCatalog() {
    }

    public static PuzzleDefinition createPuzzle(
        String puzzleId,
        int rulesVersion,
        int fixtureVersion
    ) {
        return switch (puzzleId) {
            case "puzzle-1" -> HARVEST_FACTORY.createPuzzle(
                new HarvestPuzzleConfig(
                    puzzleId,
                    rulesVersion,
                    fixtureVersion,
                    2,
                    2,
                    6,
                    List.of(
                        TileFixture.CropState.RIPE,
                        TileFixture.CropState.RIPE
                    )
                )
            );

            case "puzzle-2" -> HARVEST_FACTORY.createPuzzle(
                new HarvestPuzzleConfig(
                    puzzleId,
                    rulesVersion,
                    fixtureVersion,
                    4,
                    4,
                    12,
                    List.of(
                        TileFixture.CropState.RIPE,
                        TileFixture.CropState.RIPE,
                        TileFixture.CropState.RIPE,
                        TileFixture.CropState.RIPE
                    )
                )
            );

            case "puzzle-3" -> HARVEST_FACTORY.createPuzzle(
                new HarvestPuzzleConfig(
                    puzzleId,
                    rulesVersion,
                    fixtureVersion,
                    4,
                    2,
                    12,
                    List.of(
                        TileFixture.CropState.RIPE,
                        TileFixture.CropState.YOUNG,
                        TileFixture.CropState.RIPE,
                        TileFixture.CropState.YOUNG
                    )
                )
            );

            case "puzzle-4" -> IRRIGATION_FACTORY.createPuzzle(
                new IrrigationPuzzleConfig(
                    puzzleId,
                    rulesVersion,
                    fixtureVersion,
                    4,
                    1,
                    2,
                    12,
                    1024,
                    List.of(
                        List.of(0, 2),
                        List.of(1, 3)
                    )
                )
            );

            case "puzzle-5" -> IRRIGATION_FACTORY.createPuzzle(
                new IrrigationPuzzleConfig(
                    puzzleId,
                    rulesVersion,
                    fixtureVersion,
                    2,
                    2,
                    2,
                    8,
                    1024,
                    List.of(
                        List.of(0, 3),
                        List.of(1, 2)
                    )
                )
            );

            default -> throw new IllegalArgumentException(
                "Unknown puzzle: " + puzzleId
            );
        };
    }
}
