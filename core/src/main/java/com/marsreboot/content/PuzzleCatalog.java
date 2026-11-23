
package com.marsreboot.content;

import java.util.List;

public final class PuzzleCatalog {
    private PuzzleCatalog() {
    }

    public static PuzzleDefinition createPuzzle1() {

        TileFixture firstTile = new TileFixture(
            0, 0, true,
            TileFixture.CropState.RIPE,
            false
        );

        TileFixture secondTile = new TileFixture(
            1, 0, true,
            TileFixture.CropState.RIPE,
            false
        );

        ScenarioFixture scenario = new ScenarioFixture(
            "p1-a",
            2,
            1,
            0,
            0,
            0,
            List.of(firstTile, secondTile)
        );

        return new PuzzleDefinition(
            "puzzle-1",
            1,
            1,
            List.of(scenario)
        );
    }
}
