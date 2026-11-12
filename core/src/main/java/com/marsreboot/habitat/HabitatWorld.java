package com.marsreboot.habitat;

import java.util.List;

/** Astronaut exploration only. No robot rules, puzzle costs, or rendering dependencies. */
public final class HabitatWorld {
    public static final float WIDTH = 960, HEIGHT = 600;
    public static final float PLAYER_WIDTH = 24, PLAYER_HEIGHT = 28, SPEED = 180;
    public static final float INTERACTION_RANGE = 60;
    public record Box(float x, float y, float width, float height) {
        public float right() { return x + width; }
        public float top() { return y + height; }
    }
    public enum Mode { EXPLORING, INSPECTION, TERMINAL }
    public enum Facility {
        PANEL("Panel air", new Box(240, 400, 100, 40)),
        TERMINAL("Terminal", new Box(720, 400, 100, 52));
        public final String label;
        public final Box bounds;
        Facility(String label, Box bounds) { this.label = label; this.bounds = bounds; }
    }
    public static final Box FLOOR = new Box(40, 40, 880, 520);
    public static final Box PARTITION = new Box(480, 160, 32, 200);
    public static final Box GARDEN = new Box(620, 160, 180, 100);
    private final List<Box> colliders = List.of(PARTITION, GARDEN,
        Facility.PANEL.bounds, Facility.TERMINAL.bounds);
    private float x = 160, y = 200;
    private Mode mode = Mode.EXPLORING;

    public float x() { return x; }
    public float y() { return y; }
    public Mode mode() { return mode; }
    public List<Box> colliders() { return colliders; }

    public void move(float horizontal, float vertical, float delta) {
        if (mode != Mode.EXPLORING || !Float.isFinite(delta) || delta <= 0) return;
        float length = (float) Math.hypot(horizontal, vertical);
        if (!Float.isFinite(length) || length == 0) return;
        float distance = SPEED * delta / Math.max(1, length);
        moveX(horizontal * distance);
        moveY(vertical * distance);
    }

    // Axis sweeps clamp at the first surface, even if one frame spans an entire wall.
    private void moveX(float dx) {
        float next = Math.max(FLOOR.x, Math.min(FLOOR.right() - PLAYER_WIDTH, x + dx));
        for (Box box : colliders) {
            if (y < box.top() && y + PLAYER_HEIGHT > box.y) {
                if (dx > 0 && x + PLAYER_WIDTH <= box.x) next = Math.min(next, box.x - PLAYER_WIDTH);
                if (dx < 0 && x >= box.right()) next = Math.max(next, box.right());
            }
        }
        x = next;
    }

    private void moveY(float dy) {
        float next = Math.max(FLOOR.y, Math.min(FLOOR.top() - PLAYER_HEIGHT, y + dy));
        for (Box box : colliders) {
            if (x < box.right() && x + PLAYER_WIDTH > box.x) {
                if (dy > 0 && y + PLAYER_HEIGHT <= box.y) next = Math.min(next, box.y - PLAYER_HEIGHT);
                if (dy < 0 && y >= box.top()) next = Math.max(next, box.top());
            }
        }
        y = next;
    }

    public Facility nearbyFacility() {
        Facility nearest = null;
        float best = INTERACTION_RANGE * INTERACTION_RANGE;
        float cx = x + PLAYER_WIDTH / 2, cy = y + PLAYER_HEIGHT / 2;
        for (Facility facility : Facility.values()) {
            Box b = facility.bounds;
            float dx = cx - Math.max(b.x, Math.min(b.right(), cx));
            float dy = cy - Math.max(b.y, Math.min(b.top(), cy));
            float distance = dx * dx + dy * dy;
            if (distance <= best) { nearest = facility; best = distance; }
        }
        return nearest;
    }

    public void interact() {
        if (mode != Mode.EXPLORING) return;
        Facility target = nearbyFacility();
        if (target != null) mode = target == Facility.PANEL ? Mode.INSPECTION : Mode.TERMINAL;
    }

    public void closePanel() { mode = Mode.EXPLORING; }
}
