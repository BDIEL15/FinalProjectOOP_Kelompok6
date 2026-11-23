package com.marsreboot.content;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScenarioFixture {

    private final String scenarioId;
    private final int width;
    private final int height;
    private final int robotStartX;
    private final int robotStartY;
    private final int initialWaterSupply;
    private final List<TileFixture> tiles;

    public ScenarioFixture(
        String scenarioId,
        int width,
        int height,
        int robotStartX,
        int robotStartY,
        int initialWaterSupply,
        List<TileFixture> tiles
    ) {
        if (scenarioId == null || scenarioId.isBlank()) {
            throw new IllegalArgumentException("Scenario ID cannot be empty.");
        }

        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("Grid dimensions must be positive.");
        }

        if (robotStartX < 0 || robotStartX >= width
            || robotStartY < 0 || robotStartY >= height) {
            throw new IllegalArgumentException("Robot position is outside the grid.");
        }

        if (initialWaterSupply < 0) {
            throw new IllegalArgumentException("Water supply cannot be negative.");
        }

        if (tiles == null) {
            throw new IllegalArgumentException("Tiles cannot be null.");
        }

        Set<String> occupiedPositions = new HashSet<>();

        for (TileFixture tile : tiles) {
            if (tile == null) {
                throw new IllegalArgumentException("Tile cannot be null.");
            }

            if (tile.getX() >= width || tile.getY() >= height) {
                throw new IllegalArgumentException("Tile is outside the grid.");
            }

            String position = tile.getX() + "," + tile.getY();

            if (!occupiedPositions.add(position)) {
                throw new IllegalArgumentException("Duplicate tile position.");
            }
        }

        this.scenarioId = scenarioId;
        this.width = width;
        this.height = height;
        this.robotStartX = robotStartX;
        this.robotStartY = robotStartY;
        this.initialWaterSupply = initialWaterSupply;
        this.tiles = List.copyOf(tiles);
    }

    public String getScenarioId() {
        return scenarioId;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getRobotStartX() {
        return robotStartX;
    }

    public int getRobotStartY() {
        return robotStartY;
    }

    public int getInitialWaterSupply() {
        return initialWaterSupply;
    }

    public List<TileFixture> getTiles() {
        return tiles;
    }
}
