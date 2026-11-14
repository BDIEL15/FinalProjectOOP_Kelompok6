package com.marsreboot.robot;

import static com.marsreboot.robot.RobotSimulation.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Executable behavior checks, matching the dependency-free core test convention. */
public final class RobotSimulationTest {
    private static int passed;

    public static void main(String[] args) {
        puzzleOneReference();
        partialAndReset();
        movementAndFailedHarvests();
        sensorsAndSnapshots();
        limits();
        invalidApiAndTerminalState();
        invalidFixtures();
        deterministicRuns();
        System.out.println("PASS: " + passed + " robot simulation assertions (no libGDX/Python).");
    }

    private static void puzzleOneReference() {
        RobotSimulation robot = new RobotSimulation(Fixture.puzzleOne());
        check(robot.getPos().equals(new Position(0, 0)), "start at first crop");
        check(robot.canHarvest(), "first crop ripe");
        check(robot.harvest(), "harvest first crop");
        check(!robot.canHarvest(), "harvest empties cell");
        check(robot.move("east"), "move east");
        check(robot.harvest(), "harvest second crop");
        check(robot.snapshot().outcome() == Outcome.ACTIVE, "goals alone do not finish program");
        Snapshot result = robot.finish();
        check(result.outcome() == Outcome.SUCCESS, "normal end passes puzzle one");
        check(result.actionCount() == 3, "reference costs three actions");
        check(result.moveCount() == 1 && result.harvestCount() == 2, "reference metrics");
        check(result.remainingTargets().isEmpty(), "no targets left");
    }

    private static void partialAndReset() {
        RobotSimulation robot = new RobotSimulation(Fixture.puzzleOne());
        check(robot.finish().outcome() == Outcome.INCOMPLETE, "empty program incomplete");
        robot.reset();
        robot.harvest();
        check(robot.finish().outcome() == Outcome.INCOMPLETE, "one crop is not success");
        robot.reset();
        Snapshot reset = robot.snapshot();
        check(reset.robot().equals(new Position(0, 0)), "reset position");
        check(reset.remainingTargets().size() == 2, "reset restores targets");
        check(reset.actionCount() == 0 && reset.apiCallCount() == 0, "reset counters");
        check(reset.outcome() == Outcome.ACTIVE && robot.canHarvest(), "reset restores crops and active state");
    }

    private static void movementAndFailedHarvests() {
        Position target = new Position(0, 0);
        RobotSimulation robot = new RobotSimulation(new Fixture(3, 3, new Position(1, 1),
            Map.of(target, Crop.RIPE, new Position(1, 1), Crop.YOUNG),
            Set.of(new Position(2, 1)), Set.of(target), 20, 100));
        check(!robot.harvest(), "young crop cannot be harvested");
        check(robot.snapshot().invalidHarvestCount() == 1, "young harvest recorded for future evaluator");
        check(robot.snapshot().crops().get(new Position(1, 1)) == Crop.YOUNG, "young crop preserved");
        check(!robot.move("east"), "blocked cell does not move robot");
        check(robot.snapshot().robot().equals(new Position(1, 1)), "blocked move keeps position");
        check(robot.move("north"), "north increases y");
        check(!robot.move("north"), "top boundary does not wrap");
        check(robot.move("west"), "west decreases x");
        check(robot.move("south") && robot.move("south"), "south decreases y");
        check(robot.snapshot().robot().equals(target), "all four directions reach expected cell");
        check(robot.harvest(), "target harvest succeeds");
        check(!robot.harvest(), "empty crop cannot be harvested twice");
        check(robot.snapshot().actionCount() == 9, "legal failed actions also cost one action");
        check(robot.snapshot().moveCount() == 4 && robot.snapshot().harvestCount() == 1, "success counters exclude failures");
        for (String direction : new String[] {"west", "south"}) {
            RobotSimulation edge = new RobotSimulation(Fixture.puzzleOne());
            check(!edge.move(direction), "lower boundary " + direction);
            check(edge.snapshot().robot().equals(new Position(0, 0)), "lower boundary preserves position");
        }
    }

    private static void sensorsAndSnapshots() {
        Position first = new Position(0, 0), second = new Position(1, 0);
        Map<Position, Crop> inputCrops = new HashMap<>(Map.of(first, Crop.RIPE, second, Crop.RIPE));
        Set<Position> inputBlocked = new HashSet<>();
        Set<Position> inputTargets = new HashSet<>(Set.of(first, second));
        Fixture fixture = new Fixture(2, 1, first, inputCrops, inputBlocked, inputTargets, 6, 1024);
        inputCrops.clear(); inputBlocked.add(first); inputTargets.clear();
        RobotSimulation robot = new RobotSimulation(fixture);
        check(robot.canHarvest() && robot.getPos().equals(first), "fixture input mutations do not affect simulation");
        check(robot.snapshot().apiCallCount() == 2 && robot.snapshot().actionCount() == 0, "sensors count requests but not actions");
        Snapshot before = robot.snapshot();
        robot.snapshot();
        check(robot.snapshot().equals(before), "presentation snapshot is not a player API request");
        robot.harvest();
        check(before.crops().get(first) == Crop.RIPE && before.remainingTargets().size() == 2, "old snapshot cannot change with live state");
        expect(UnsupportedOperationException.class, () -> before.crops().put(first, Crop.EMPTY), "snapshot crops immutable");
        expect(UnsupportedOperationException.class, () -> before.remainingTargets().clear(), "snapshot targets immutable");
        expect(UnsupportedOperationException.class, () -> fixture.crops().clear(), "fixture immutable");
        RobotSimulation independent = new RobotSimulation(fixture);
        check(independent.canHarvest() && independent.snapshot().remainingTargets().size() == 2, "instances sharing fixture are isolated");
    }

    private static void limits() {
        Fixture base = Fixture.puzzleOne();
        RobotSimulation exact = new RobotSimulation(withLimits(base, 3, 10));
        exact.harvest(); exact.move("east"); exact.harvest();
        check(exact.finish().outcome() == Outcome.SUCCESS, "exact action limit may succeed on normal finish");
        RobotSimulation limited = new RobotSimulation(withLimits(base, 2, 10));
        limited.harvest(); limited.move("east");
        Snapshot before = limited.snapshot();
        expect(IllegalStateException.class, () -> limited.harvest(), "over-budget action aborts");
        check(limited.snapshot().crops().equals(before.crops()) && limited.snapshot().robot().equals(before.robot()), "over-budget action does not mutate world");
        check(limited.snapshot().actionCount() == 2 && limited.snapshot().apiCallCount() == 3, "over-budget request counted, rejected action not charged");
        check(limited.finish().outcome() == Outcome.LIMIT_EXCEEDED, "finish cannot promote limit failure");
        limited.reset();
        check(limited.harvest() && limited.snapshot().actionCount() == 1, "reset recovers from limit");
        RobotSimulation sensors = new RobotSimulation(withLimits(base, 6, 2));
        sensors.getPos(); sensors.canHarvest();
        expect(IllegalStateException.class, () -> sensors.canHarvest(), "sensor-only loop bounded by API calls");
        check(sensors.snapshot().apiCallCount() == 2 && sensors.snapshot().actionCount() == 0, "request counter bounded at limit");
        check(sensors.finish().outcome() == Outcome.LIMIT_EXCEEDED, "sensor overflow not success");
    }

    private static void invalidApiAndTerminalState() {
        RobotSimulation invalid = new RobotSimulation(Fixture.puzzleOne());
        Snapshot before = invalid.snapshot();
        expect(IllegalArgumentException.class, () -> invalid.move("diagonal"), "unknown direction is API error");
        check(invalid.snapshot().crops().equals(before.crops()) && invalid.snapshot().robot().equals(before.robot()), "invalid direction never mutates world");
        check(invalid.snapshot().actionCount() == 0 && invalid.snapshot().apiCallCount() == 1, "invalid argument is a request, not an action");
        check(invalid.finish().outcome() == Outcome.ERROR, "invalid argument freezes error outcome");
        invalid.reset();
        expect(IllegalArgumentException.class, () -> invalid.move(null), "null direction also API error");
        invalid.reset();
        invalid.harvest(); invalid.move("east"); invalid.harvest();
        expect(IllegalArgumentException.class, () -> invalid.move("bad"), "later error still fails completed targets");
        check(invalid.finish().outcome() == Outcome.ERROR, "goals before error do not award success");
        RobotSimulation finished = new RobotSimulation(Fixture.puzzleOne());
        finished.harvest(); finished.move("east"); finished.harvest();
        Snapshot result = finished.finish();
        check(finished.finish().equals(result), "finish idempotent");
        expect(IllegalStateException.class, () -> finished.move("west"), "actions after finish rejected");
        expect(IllegalStateException.class, () -> finished.harvest(), "harvest after finish rejected");
        expect(IllegalStateException.class, () -> finished.getPos(), "player sensor after finish rejected");
        expect(IllegalStateException.class, () -> finished.canHarvest(), "harvest sensor after finish rejected");
        check(finished.snapshot().equals(result), "late API calls do not change frozen result");
        finished.reset();
        check(finished.snapshot().equals(new RobotSimulation(Fixture.puzzleOne()).snapshot()), "reset after success restores full fixture");
    }

    private static void invalidFixtures() {
        Position first = new Position(0, 0);
        Map<Position, Crop> ripe = Map.of(first, Crop.RIPE);
        expect(IllegalArgumentException.class, () -> new Fixture(0, 1, first, ripe, Set.of(), Set.of(first), 6, 10), "zero width rejected");
        expect(IllegalArgumentException.class, () -> new Fixture(1, 1, first, ripe, Set.of(), Set.of(first), 0, 10), "zero action limit rejected");
        expect(IllegalArgumentException.class, () -> new Fixture(1, 1, first, ripe, Set.of(), Set.of(first), 6, 0), "zero API limit rejected");
        expect(IllegalArgumentException.class, () -> new Fixture(1, 1, new Position(-1, 0), ripe, Set.of(), Set.of(first), 6, 10), "start outside grid rejected");
        expect(IllegalArgumentException.class, () -> new Fixture(1, 1, first, ripe, Set.of(first), Set.of(first), 6, 10), "blocked start rejected");
        expect(IllegalArgumentException.class, () -> new Fixture(1, 1, first, ripe, Set.of(new Position(2, 0)), Set.of(first), 6, 10), "blocked cell outside grid rejected");
        expect(IllegalArgumentException.class, () -> new Fixture(1, 1, first, Map.of(new Position(1, 0), Crop.RIPE), Set.of(), Set.of(first), 6, 10), "crop outside grid rejected");
        expect(IllegalArgumentException.class, () -> new Fixture(2, 1, first, Map.of(first, Crop.RIPE, new Position(1, 0), Crop.RIPE), Set.of(new Position(1, 0)), Set.of(first), 6, 10), "crop on blocked cell rejected");
        expect(IllegalArgumentException.class, () -> new Fixture(1, 1, first, ripe, Set.of(), Set.of(), 6, 10), "empty harvest target set rejected");
        expect(IllegalArgumentException.class, () -> new Fixture(1, 1, first, Map.of(first, Crop.YOUNG), Set.of(), Set.of(first), 6, 10), "nonripe initial target rejected");
    }

    private static void deterministicRuns() {
        Snapshot baseline = null;
        RobotSimulation robot = new RobotSimulation(Fixture.puzzleOne());
        for (int attempt = 0; attempt < 5; attempt++) {
            robot.reset(); robot.harvest(); robot.move("east"); robot.harvest();
            Snapshot result = robot.finish();
            if (baseline == null) baseline = result;
            check(result.equals(baseline), "repeat/reset deterministic " + attempt);
        }
    }

    private static Fixture withLimits(Fixture base, int actionLimit, int apiLimit) {
        return new Fixture(base.width(), base.height(), base.start(), base.crops(), base.blocked(), base.harvestTargets(), actionLimit, apiLimit);
    }

    private static void expect(Class<? extends Throwable> type, Runnable action, String description) {
        try {
            action.run();
        } catch (Throwable failure) {
            check(type.isInstance(failure), description + " (unexpected " + failure + ")");
            return;
        }
        throw new AssertionError(description + " (no exception)");
    }

    private static void check(boolean condition, String description) {
        if (!condition) throw new AssertionError(description);
        passed++;
    }
}
