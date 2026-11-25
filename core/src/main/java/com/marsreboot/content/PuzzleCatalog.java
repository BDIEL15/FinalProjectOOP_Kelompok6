package com.marsreboot.content;

import java.util.ArrayList;
import java.util.List;

public final class PuzzleCatalog {

    private PuzzleCatalog() {
    }

    public static PuzzleDefinition createPuzzle1() {
        List<TileFixture> tiles = new ArrayList<>();

        for (int x = 0; x < 2; x++) {
            tiles.add(new TileFixture(
                x, 0, true,
                TileFixture.CropState.RIPE,
                false
            ));
        }

        return createHarvestPuzzle("puzzle-1", 2, tiles, 2, 6);
    }

    public static PuzzleDefinition createPuzzle2() {
        List<TileFixture> tiles = new ArrayList<>();

        for (int x = 0; x < 4; x++) {
            tiles.add(new TileFixture(
                x, 0, true,
                TileFixture.CropState.RIPE,
                false
            ));
        }

        return createHarvestPuzzle("puzzle-2", 4, tiles, 4, 12);
    }

    public static PuzzleDefinition createPuzzle3() {
        List<TileFixture> tiles = new ArrayList<>();

        for (int x = 0; x < 4; x++) {
            TileFixture.CropState crop =
                x % 2 == 0
                    ? TileFixture.CropState.RIPE
                    : TileFixture.CropState.YOUNG;

            tiles.add(new TileFixture(
                x, 0, true, crop, false
            ));
        }

        return createHarvestPuzzle("puzzle-3", 4, tiles, 2, 12);
    }

    public static PuzzleDefinition createPuzzle4() {
        List<ScenarioFixture> scenarios = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            List<TileFixture> tiles = new ArrayList<>();

            for (int x = 0; x < 4; x++) {
                boolean needsWater = x == i || x == i + 2;

                tiles.add(new TileFixture(
                    x, 0, true,
                    TileFixture.CropState.EMPTY,
                    needsWater
                ));
            }

            scenarios.add(new ScenarioFixture(
                "p4-" + (i == 0 ? "a" : "b"),
                4, 1,
                0, 0,
                2,
                12,
                1024,
                tiles
            ));
        }

        return new PuzzleDefinition(
            "puzzle-4",
            1,
            1,
            scenarios,
            new ScenarioGoal(0, 2)
        );
    }

    private static PuzzleDefinition createHarvestPuzzle(
        String id,
        int width,
        List<TileFixture> tiles,
        int target,
        int actionLimit
    ) {
        ScenarioFixture scenario = new ScenarioFixture(
            id.replace("puzzle-", "p") + "-a",
            width,
            1,
            0,
            0,
            0,
            actionLimit,
            1024,
            tiles
        );

        return new PuzzleDefinition(
            id,
            1,
            1,
            List.of(scenario),
            new ScenarioGoal(target, 0)
        );
    }
}
