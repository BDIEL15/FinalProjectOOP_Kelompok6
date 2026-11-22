package com.marsreboot.habitat;

import java.util.List;

/**
 * Astronaut exploration, smooth velocity physics, outpost colliders, and Rover click hit-testing.
 * Contains no Python simulation rules, action costs, or rendering dependencies.
 */
public final class HabitatWorld {
    public static final float WIDTH = 1680f, HEIGHT = 1040f;
    public static final float CAMERA_WIDTH = 720f, CAMERA_HEIGHT = 450f;
    public static final float PLAYER_WIDTH = 28f, PLAYER_HEIGHT = 32f;
    public static final float SPEED = 210f;
    public static final float ACCEL_RATE = 18f, DECEL_RATE = 15f;
    public static final float INTERACTION_RANGE = 82f;
    private static final float FIXED_STEP = 1f / 120f;

    public record Box(float x, float y, float width, float height) {
        public float right() { return x + width; }
        public float top() { return y + height; }
        public float centerX() { return x + width * 0.5f; }
        public float centerY() { return y + height * 0.5f; }
        public boolean contains(float px, float py, float pad) {
            return px >= x - pad && px <= right() + pad && py >= y - pad && py <= top() + pad;
        }
    }

    public enum Mode { EXPLORING, INSPECTION, TERMINAL }

    public enum Facility {
        PANEL("Panel Telemetri & Bio-Dome", new Box(510f, 680f, 96f, 52f)),
        ROVER("Rover Bio-Dome", new Box(960f, 360f, 360f, 200f));

        public final String label;
        public final Box bounds;

        Facility(String label, Box bounds) {
            this.label = label;
            this.bounds = bounds;
        }
    }

    public static final Box FLOOR = new Box(64f, 64f, 1552f, 912f);
    public static final Box HAB_AIRLOCK = new Box(110f, 480f, 190f, 140f);
    public static final Box SOLAR_WING = new Box(150f, 760f, 220f, 92f);
    public static final Box PARTITION = new Box(650f, 340f, 44f, 300f);
    public static final Box PUMP_STATION = new Box(1390f, 760f, 140f, 116f);
    public static final Box GARDEN = Facility.ROVER.bounds;

    private final List<Box> colliders = List.of(
        HAB_AIRLOCK,
        SOLAR_WING,
        PARTITION,
        PUMP_STATION,
        GARDEN,
        Facility.PANEL.bounds
    );

    private float x = 220f, y = 320f;
    private float vx, vy;
    private float facingX = 1f, facingY = 0f;
    private Mode mode = Mode.EXPLORING;

    public float x() { return x; }
    public float y() { return y; }
    public float vx() { return vx; }
    public float vy() { return vy; }
    public float facingX() { return facingX; }
    public float facingY() { return facingY; }
    public float currentSpeed() { return (float) Math.hypot(vx, vy); }
    public Mode mode() { return mode; }
    public List<Box> colliders() { return colliders; }

    public void move(float horizontal, float vertical, float delta) {
        if (!Float.isFinite(delta) || delta <= 0f) return;
        if (mode != Mode.EXPLORING) {
            vx = 0f;
            vy = 0f;
            return;
        }
        float inputLen = (float) Math.hypot(horizontal, vertical);
        float targetVx = 0f;
        float targetVy = 0f;
        if (Float.isFinite(inputLen) && inputLen > 0f) {
            float scale = SPEED / Math.max(1f, inputLen);
            targetVx = horizontal * scale;
            targetVy = vertical * scale;
            facingX = horizontal / inputLen;
            facingY = vertical / inputLen;
        }

        // Sub-step exponential acceleration & deceleration so curves stay frame-rate independent.
        float remaining = Math.min(delta, 10f);
        float rate = inputLen > 0f ? ACCEL_RATE : DECEL_RATE;
        while (remaining > 1e-6f) {
            float step = Math.min(remaining, FIXED_STEP);
            float alpha = 1f - (float) Math.exp(-rate * step);
            vx += (targetVx - vx) * alpha;
            vy += (targetVy - vy) * alpha;
            if (inputLen == 0f && Math.hypot(vx, vy) < 0.8f) {
                vx = 0f;
                vy = 0f;
                break;
            }
            moveX(vx * step);
            moveY(vy * step);
            remaining -= step;
        }
    }

    // Axis sweeps clamp at the first surface, even if one frame spans an entire wall.
    private void moveX(float dx) {
        if (dx == 0f) return;
        float prevX = x;
        float next = Math.max(FLOOR.x, Math.min(FLOOR.right() - PLAYER_WIDTH, x + dx));
        for (Box box : colliders) {
            if (y < box.top() && y + PLAYER_HEIGHT > box.y) {
                if (dx > 0 && x + PLAYER_WIDTH <= box.x) next = Math.min(next, box.x - PLAYER_WIDTH);
                if (dx < 0 && x >= box.right()) next = Math.max(next, box.right());
            }
        }
        x = next;
        if (Math.abs(x - prevX) < Math.abs(dx) * 0.25f) {
            vx = 0f;
        }
    }

    private void moveY(float dy) {
        if (dy == 0f) return;
        float prevY = y;
        float next = Math.max(FLOOR.y, Math.min(FLOOR.top() - PLAYER_HEIGHT, y + dy));
        for (Box box : colliders) {
            if (x < box.right() && x + PLAYER_WIDTH > box.x) {
                if (dy > 0 && y + PLAYER_HEIGHT <= box.y) next = Math.min(next, box.y - PLAYER_HEIGHT);
                if (dy < 0 && y >= box.top()) next = Math.max(next, box.top());
            }
        }
        y = next;
        if (Math.abs(y - prevY) < Math.abs(dy) * 0.25f) {
            vy = 0f;
        }
    }

    public Facility nearbyFacility() {
        Facility nearest = null;
        float best = INTERACTION_RANGE * INTERACTION_RANGE;
        float cx = x + PLAYER_WIDTH * 0.5f, cy = y + PLAYER_HEIGHT * 0.5f;
        for (Facility facility : Facility.values()) {
            Box b = facility.bounds;
            float dx = cx - Math.max(b.x, Math.min(b.right(), cx));
            float dy = cy - Math.max(b.y, Math.min(b.top(), cy));
            float distance = dx * dx + dy * dy;
            if (distance <= best) {
                nearest = facility;
                best = distance;
            }
        }
        return nearest;
    }

    public void interact() {
        if (mode != Mode.EXPLORING) return;
        Facility target = nearbyFacility();
        if (target == Facility.PANEL) {
            openPanelInspection();
        } else if (target == Facility.ROVER) {
            openRoverTerminal();
        }
    }

    public void openRoverTerminal() {
        if (mode != Mode.EXPLORING) return;
        vx = 0f;
        vy = 0f;
        mode = Mode.TERMINAL;
    }

    public void openPanelInspection() {
        if (mode != Mode.EXPLORING) return;
        vx = 0f;
        vy = 0f;
        mode = Mode.INSPECTION;
    }

    public void closePanel() {
        vx = 0f;
        vy = 0f;
        mode = Mode.EXPLORING;
    }

    /** Shared ground-truth grid bed metrics inside Bio-Dome GARDEN so renderer, click hit-tests, and tests match. */
    public static Box hydroCellBox(int cellX, int cellY, int cols) {
        int safeCols = Math.max(1, cols);
        float cellW = safeCols <= 2 ? 124f : 82f;
        float cellH = 104f;
        float gap = safeCols <= 2 ? 28f : 12f;
        float totalW = safeCols * cellW + (safeCols - 1) * gap;
        float startX = GARDEN.x + (GARDEN.width - totalW) * 0.5f;
        float startY = GARDEN.y + 52f + cellY * (cellH + 14f);
        return new Box(startX + cellX * (cellW + gap), startY, cellW, cellH);
    }

    /** Shared visual/clickable Rover bay bounds for continuous (fractional or integer) rover cell position. */
    public static Box roverBayBox(float cellX, float cellY, int cols) {
        int intCell = Math.max(0, Math.min(Math.max(1, cols) - 1, Math.round(cellX)));
        Box baseLeft = hydroCellBox(0, Math.round(cellY), cols);
        Box right = hydroCellBox(Math.min(1, Math.max(1, cols) - 1), Math.round(cellY), cols);
        float stepX = cols > 1 ? (right.x - baseLeft.x) : 0f;
        float xPos = baseLeft.x + cellX * stepX + 10f;
        float yPos = baseLeft.y + 24f;
        return new Box(xPos, yPos, 54f, 54f);
    }

    public static boolean isRoverHit(float worldX, float worldY, float roverCellX, float roverCellY, int cols) {
        return roverBayBox(roverCellX, roverCellY, cols).contains(worldX, worldY, 22f);
    }
}
