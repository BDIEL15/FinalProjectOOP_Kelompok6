package com.marsreboot.content;

public class TileFixture {

    public enum CropState {
        EMPTY,
        YOUNG,
        RIPE
    }

    private final int x;
    private final int y;
    private final boolean passable;
    private final CropState cropState;
    private final boolean needsWater;

    public TileFixture(
        int x,
        int y,
        boolean passable,
        CropState cropState,
        boolean needsWater
    ) {
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("Tile coordinates cannot be negative.");
        }

        if (cropState == null) {
            throw new IllegalArgumentException("Crop state cannot be null.");
        }

        this.x = x;
        this.y = y;
        this.passable = passable;
        this.cropState = cropState;
        this.needsWater = needsWater;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isPassable() {
        return passable;
    }

    public CropState getCropState() {
        return cropState;
    }

    public boolean needsWater() {
        return needsWater;
    }
}
