package com.marsreboot.content;

import java.util.ArrayList;
import java.util.List;

public class IrrigationPuzzleFactory
    extends PuzzleFactory<IrrigationPuzzleConfig> {

    @Override
    public PuzzleDefinition createPuzzle(IrrigationPuzzleConfig config) {
        List<ScenarioFixture> scenarios = new ArrayList<>();

        for (int i = 0; i < config.dryTilePositions().size(); i++) {
            List<TileFixture> tiles = new ArrayList<>();

            for (int y = 0; y < config.height(); y++) {
                for (int x = 0; x < config.width(); x++) {
                    int position = y * config.width() + x;

                    boolean needsWater =
                        config.dryTilePositions().get(i).contains(position);

                    tiles.add(new TileFixture(
                        x,
                        y,
                        true,
                        TileFixture.CropState.EMPTY,
                        needsWater
                    ));
                }
            }

            scenarios.add(new ScenarioFixture(
                config.puzzleId().replace("puzzle-", "p") + "-"
                    + (char) ('a' + i),
                config.width(),
                config.height(),
                0,
                0,
                config.initialWaterSupply(),
                config.actionLimit(),
                config.apiCallLimit(),
                tiles
            ));
        }

        return new PuzzleDefinition(
            config.puzzleId(),
            config.rulesVersion(),
            config.fixtureVersion(),
            scenarios,
            new ScenarioGoal(0, config.dryTilePositions().get(0).size())
        );
    }
}
