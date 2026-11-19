package com.marsreboot.robot;

import com.marsreboot.runtime.PuzzleDefinition;
import java.util.function.Function;
import static com.marsreboot.robot.RobotSimulation.*;

/** Public command/evaluation behavior; no renderer, Python or reflection. */
public final class PatternBehaviorTest {
    private static int checks;
    public static void main(String[] args) {
        var robot = new RobotSimulation(Fixture.puzzleOne());
        check(new HarvestCommand().execute(robot), "command harvest succeeds");
        check(new MoveCommand("east").execute(robot), "command moves east");
        check(new HarvestCommand().execute(robot), "second harvest succeeds");
        check(robot.snapshot().actionCount() == 3 && robot.snapshot().remainingTargets().isEmpty(), "three actions clear goals");
        check(robot.finish(new HarvestEvaluation()).outcome() == Outcome.SUCCESS, "harvest strategy accepts complete goals");
        check(new RobotSimulation(Fixture.puzzleOne()).finish(new HarvestEvaluation()).outcome() == Outcome.INCOMPLETE, "partial harvest fails");
        commandParity();
        evaluationPolicies();
        System.out.println("PASS: " + checks + " command/strategy behavior assertions.");
    }
    private static void commandParity() {
        var command = new RobotSimulation(PuzzleDefinition.THREE.fixture());
        var direct = new RobotSimulation(PuzzleDefinition.THREE.fixture());
        compare(new MoveCommand("west"), r -> r.move("west"), command, direct);
        check(command.snapshot().actionCount() == 1 && command.snapshot().robot().x() == 0, "blocked move spends one action");
        compare(new HarvestCommand(), RobotSimulation::harvest, command, direct);
        compare(new HarvestCommand(), RobotSimulation::harvest, command, direct);
        compare(new MoveCommand("east"), r -> r.move("east"), command, direct);
        compare(new HarvestCommand(), RobotSimulation::harvest, command, direct);
        check(command.snapshot().invalidHarvestCount() == 1, "young harvest tracked through command");
        compare(new MoveCommand("invalid"), r -> r.move("invalid"), command, direct);
        check(command.snapshot().outcome() == Outcome.ERROR, "invalid direction freezes domain");
        compare(new HarvestCommand(), RobotSimulation::harvest, command, direct);
        var wet = new RobotSimulation(PuzzleDefinition.FOUR.fixture());
        var wetDirect = new RobotSimulation(PuzzleDefinition.FOUR.fixture());
        compare(new WaterCommand(), RobotSimulation::water, wet, wetDirect);
        compare(new WaterCommand(), RobotSimulation::water, wet, wetDirect);
        compare(new WaterCommand(), RobotSimulation::water, wet, wetDirect);
        check(wet.snapshot().waterRemaining() == 0 && wet.snapshot().actionCount() == 3, "moist and empty water charged");
        var limited = new RobotSimulation(Fixture.puzzleOne());
        var limitedDirect = new RobotSimulation(Fixture.puzzleOne());
        for (int i = 0; i < 7; i++) compare(new MoveCommand("west"), r -> r.move("west"), limited, limitedDirect);
        check(limited.snapshot().outcome() == Outcome.LIMIT_EXCEEDED, "commands honor action cap");
        compare(new MoveCommand(null), r -> r.move(null), new RobotSimulation(Fixture.puzzleOne()), new RobotSimulation(Fixture.puzzleOne()));
    }
    private static void compare(RobotCommand command, Function<RobotSimulation, Boolean> action, RobotSimulation receiver, RobotSimulation direct) {
        check(attempt(() -> command.execute(receiver)).equals(attempt(() -> action.apply(direct))), "command return/error matches direct action");
        check(receiver.snapshot().equals(direct.snapshot()), "command state and accounting match direct action");
    }
    private static String attempt(java.util.concurrent.Callable<Boolean> action) {
        try { return "value:" + action.call(); }
        catch (Exception error) { return error.getClass().getName() + ":" + error.getMessage(); }
    }
    private static void evaluationPolicies() {
        for (var puzzle : PuzzleDefinition.values()) {
            var partial = new RobotSimulation(puzzle.fixture());
            var result = partial.finish(puzzle.evaluationStrategy());
            check(result.outcome() == Outcome.INCOMPLETE, "partial fails for " + puzzle);
            check(partial.finish(s -> { throw new AssertionError("must not reevaluate finished state"); }).equals(result), "finish idempotent");
        }
        var young = new RobotSimulation(PuzzleDefinition.THREE.fixture());
        young.harvest(); young.move("east"); young.harvest(); young.move("east"); young.harvest();
        var before = young.snapshot();
        check(new HarvestEvaluation().evaluate(before).outcome() == Outcome.SUCCESS, "base policy accepts completed targets");
        check(PuzzleDefinition.THREE.evaluationStrategy().evaluate(before).outcome() == Outcome.INCOMPLETE, "strict policy rejects young attempt");
        check(young.snapshot().equals(before), "evaluation is read only");
        check(young.finish(true).outcome() == Outcome.INCOMPLETE, "legacy strict overload preserved");
        var irrigated = new RobotSimulation(PuzzleDefinition.FOUR.fixture());
        irrigated.water(); irrigated.move("east"); irrigated.move("east"); irrigated.water();
        var expected = new IrrigationEvaluation().evaluate(irrigated.snapshot());
        check(expected.outcome() == Outcome.SUCCESS && expected.message().equals("Semua target penyiraman selesai."), "irrigation success message");
        check(irrigated.finish(true).message().equals(expected.message()), "legacy strict water delegates to water policy");
        var error = new RobotSimulation(Fixture.puzzleOne());
        attempt(() -> new MoveCommand("bad").execute(error));
        check(error.finish(s -> { throw new AssertionError("error must not evaluate"); }).outcome() == Outcome.ERROR, "error cannot become success");
        var limit = new RobotSimulation(Fixture.puzzleOne());
        for (int i = 0; i < 7; i++) attempt(() -> new MoveCommand("west").execute(limit));
        check(limit.finish(s -> { throw new AssertionError("limit must not evaluate"); }).outcome() == Outcome.LIMIT_EXCEEDED, "limit cannot become success");
        try { new Evaluation(Outcome.ERROR, "bad"); throw new AssertionError("invalid evaluation accepted"); }
        catch (IllegalArgumentException expectedError) { checks++; }
    }
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
        checks++;
    }
}
