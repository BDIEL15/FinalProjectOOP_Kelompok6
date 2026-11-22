package com.marsreboot.robot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Deterministic, single-owner Java state for the candidate harvest scenario.
 * No rendering, Python interpreter, wall clock, or persistent progress lives here.
 */
public final class RobotSimulation {
    public record Position(int x, int y) { }
    public enum Crop { EMPTY, YOUNG, RIPE }
    public enum Outcome { ACTIVE, SUCCESS, INCOMPLETE, ERROR, LIMIT_EXCEEDED }

    /** Defensive copies keep resets and separate simulations independent. */
    public record Fixture(int width, int height, Position start,
                          Map<Position, Crop> crops, Set<Position> blocked,
                          Set<Position> harvestTargets, int actionLimit, int apiCallLimit) {
        public Fixture {
            Objects.requireNonNull(start, "start");
            crops = Map.copyOf(crops);
            blocked = Set.copyOf(blocked);
            harvestTargets = Set.copyOf(harvestTargets);
            if (width <= 0 || height <= 0 || actionLimit <= 0 || apiCallLimit <= 0) {
                throw new IllegalArgumentException("Dimensions and limits must be positive.");
            }
            if (!inside(start, width, height) || blocked.contains(start)) {
                throw new IllegalArgumentException("Robot start must be inside a passable cell.");
            }
            for (Position position : blocked) {
                if (!inside(position, width, height)) {
                    throw new IllegalArgumentException("Blocked cell outside fixture.");
                }
            }
            for (Position position : crops.keySet()) {
                if (!inside(position, width, height) || blocked.contains(position)) {
                    throw new IllegalArgumentException("Crop must be inside a passable cell.");
                }
            }
            if (harvestTargets.isEmpty()) {
                throw new IllegalArgumentException("Harvest fixture needs at least one target.");
            }
            for (Position position : harvestTargets) {
                if (crops.get(position) != Crop.RIPE) {
                    throw new IllegalArgumentException("Each harvest target must initially be ripe.");
                }
            }
        }

        /** Candidate p1-a from RULES; not a claim that the fixture is final. */
        public static Fixture puzzleOne() {
            Position first = new Position(0, 0);
            Position second = new Position(1, 0);
            return new Fixture(2, 1, first, Map.of(first, Crop.RIPE, second, Crop.RIPE),
                Set.of(), Set.of(first, second), 6, 1024);
        }
    }

    /** Immutable presentation/evaluation data; reading it never spends a game request. */
    public record Snapshot(Position robot, Map<Position, Crop> crops,
                           Set<Position> remainingTargets, int actionCount, int apiCallCount,
                           int moveCount, int harvestCount, int invalidHarvestCount,
                           Outcome outcome, String message) {
        public Snapshot {
            crops = Map.copyOf(crops);
            remainingTargets = Set.copyOf(remainingTargets);
        }
    }

    private final Fixture fixture;
    private final Map<Position, Crop> crops = new HashMap<>();
    private final Set<Position> remainingTargets = new HashSet<>();
    private Position robot;
    private int actionCount;
    private int apiCallCount;
    private int moveCount;
    private int harvestCount;
    private int invalidHarvestCount;
    private Outcome outcome;
    private String message;

    public RobotSimulation(Fixture fixture) {
        this.fixture = Objects.requireNonNull(fixture, "fixture");
        reset();
    }

    public boolean move(String direction) {
        beginCall();
        int dx = 0;
        int dy = 0;
        if (direction == null) throw apiError("Direction must be north/east/south/west.");
        switch (direction) {
            case "north" -> dy = 1;
            case "east" -> dx = 1;
            case "south" -> dy = -1;
            case "west" -> dx = -1;
            default -> throw apiError("Unknown direction: " + direction);
        }
        chargeAction();
        Position next = new Position(robot.x() + dx, robot.y() + dy);
        if (!inside(next, fixture.width(), fixture.height()) || fixture.blocked().contains(next)) {
            message = "Jalur terhalang atau di luar grid.";
            return false;
        }
        robot = next;
        moveCount++;
        message = "Robot bergerak " + direction + ".";
        return true;
    }

    public boolean harvest() {
        beginCall();
        chargeAction();
        Crop crop = crops.getOrDefault(robot, Crop.EMPTY);
        if (crop != Crop.RIPE) {
            if (crop == Crop.YOUNG) invalidHarvestCount++;
            message = crop == Crop.YOUNG ? "Tanaman belum matang." : "Tidak ada tanaman siap panen.";
            return false;
        }
        crops.put(robot, Crop.EMPTY);
        remainingTargets.remove(robot);
        harvestCount++;
        message = "Tanaman dipanen.";
        return true;
    }

    public boolean canHarvest() {
        beginCall();
        return crops.getOrDefault(robot, Crop.EMPTY) == Crop.RIPE;
    }

    public Position getPos() {
        beginCall();
        return robot;
    }

    /** Only normal completion evaluates the current harvest targets, never an intermediate action. */
    public Snapshot finish() {
        if (outcome == Outcome.ACTIVE) {
            outcome = remainingTargets.isEmpty() ? Outcome.SUCCESS : Outcome.INCOMPLETE;
            message = outcome == Outcome.SUCCESS ? "Semua target panen selesai." : "Masih ada target panen.";
        }
        return snapshot();
    }

    public void reset() {
        robot = fixture.start();
        crops.clear();
        crops.putAll(fixture.crops());
        remainingTargets.clear();
        remainingTargets.addAll(fixture.harvestTargets());
        actionCount = apiCallCount = moveCount = harvestCount = invalidHarvestCount = 0;
        outcome = Outcome.ACTIVE;
        message = "Siap menjalankan percobaan baru.";
    }

    public Snapshot snapshot() {
        return new Snapshot(robot, crops, remainingTargets, actionCount, apiCallCount,
            moveCount, harvestCount, invalidHarvestCount, outcome, message);
    }

    private void beginCall() {
        if (outcome != Outcome.ACTIVE) throw new IllegalStateException("Scenario already finished; reset before calling API.");
        if (apiCallCount >= fixture.apiCallLimit()) throw limitError("Batas panggilan API tercapai.");
        apiCallCount++;
    }

    private void chargeAction() {
        if (actionCount >= fixture.actionLimit()) throw limitError("Batas aksi tercapai.");
        actionCount++;
    }

    private IllegalStateException limitError(String reason) {
        outcome = Outcome.LIMIT_EXCEEDED;
        message = reason;
        return new IllegalStateException(reason);
    }

    private IllegalArgumentException apiError(String reason) {
        outcome = Outcome.ERROR;
        message = reason;
        return new IllegalArgumentException(reason);
    }

    private static boolean inside(Position position, int width, int height) {
        return position.x() >= 0 && position.x() < width && position.y() >= 0 && position.y() < height;
    }
}
