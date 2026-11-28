package com.marsreboot.content;

import java.util.ArrayList;
import java.util.List;

public class HarvestPuzzleFactory
    extends PuzzleFactory<HarvestPuzzleConfig> {

    @Override
    public PuzzleDefinition createPuzzle(HarvestPuzzleConfig config) {
        List<TileFixture> tiles = new ArrayList<>();

        for (int x = 0; x < config.width(); x++) {
            tiles.add(new TileFixture(
                x,
                0,
                true,
                config.cropLayout().get(x),
                false
            ));
        }

        ScenarioFixture scenario = new ScenarioFixture(
            config.puzzleId().replace("puzzle-", "p") + "-a",
            config.width(),
            1,
            0,
            0,
            0,
            config.actionLimit(),
            1024,
            tiles
        );

        return new PuzzleDefinition(
            config.puzzleId(),
            config.rulesVersion(),
            config.fixtureVersion(),
            List.of(scenario),
            new ScenarioGoal(config.target(), 0)
        );
    }
}
