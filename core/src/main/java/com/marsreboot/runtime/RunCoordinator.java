package com.marsreboot.runtime;

import com.marsreboot.robot.RobotSimulation;
import com.marsreboot.robot.RobotSimulation.Snapshot;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/** Render-thread controller; each worker owns an independent simulation and process. */
public final class RunCoordinator implements AutoCloseable {
    public enum Status { READY, PREPARING, RUNNING, REPLAYING, SUCCESS, INCOMPLETE, ERROR, LIMIT_EXCEEDED, TIMEOUT, STOPPED }
    public record Execution(Status status, Snapshot snapshot, List<Snapshot> trace, String output, String message, boolean reusable, List<Scenario> scenarios) {
        public Execution { trace = List.copyOf(trace); scenarios = List.copyOf(scenarios); }
        public Execution(Status status,Snapshot snapshot,List<Snapshot> trace,String output,String message,boolean reusable) {
            this(status,snapshot,trace,output,message,reusable,List.of());
        }
        public Execution(Status status, Snapshot snapshot, List<Snapshot> trace, String output, String message) {
            this(status, snapshot, trace, output, message, true);
        }
    }
    public record Scenario(String id, Execution result) { }
    public record View(long generation, Status status, Snapshot snapshot, String output, String message,
                       boolean canRun, boolean canStop, boolean completed, PuzzleDefinition puzzle, String scenarioId, int scenarioNumber,
                       int scenarioCount, int scenariosPassed, int totalActions, int totalWaterUsed) { }
    @FunctionalInterface public interface Bridge {
        Execution execute(String source, long generation, AtomicBoolean cancelled, Runnable ready);
        default Bridge forPuzzle(PuzzleDefinition puzzle) {
            if (puzzle != PuzzleDefinition.ONE) throw new UnsupportedOperationException("Bridge does not support puzzle");
            return this;
        }
    }
    private final Bridge bridge;
    private PuzzleDefinition puzzle = PuzzleDefinition.ONE;
    private final java.util.Set<PuzzleDefinition> completedPuzzles = java.util.EnumSet.noneOf(PuzzleDefinition.class);
    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "mars-python-run"); t.setDaemon(true); return t;
    });
    private long generation;
    private Future<Execution> future;
    private AtomicBoolean cancellation;
    private volatile boolean ready;
    private boolean closed, completed, blocked;
    private Status status = Status.READY;
    private int activeScenario, scenariosPassed, totalActions, totalWaterUsed;
    private List<Scenario> replayScenarios = List.of();
    private Snapshot visible = initial();
    private String output = "", message = "Panen dua tanaman matang. Maksimal 6 aksi.";
    private Execution replay;
    private boolean stageFinished;
    private int replayIndex;
    private float replayTime;

    public RunCoordinator(Bridge bridge) { this.bridge = bridge; }
    private Snapshot initial() { return new RobotSimulation(puzzle.fixture(activeScenario)).snapshot(); }
    public boolean isCompleted(PuzzleDefinition p) { return completedPuzzles.contains(p); }
    public boolean selectPuzzle(PuzzleDefinition selected) {
        if (closed || blocked || future != null || replay != null || (selected == PuzzleDefinition.TWO && !isCompleted(PuzzleDefinition.ONE)) || (selected == PuzzleDefinition.THREE && !isCompleted(PuzzleDefinition.TWO)) || (selected == PuzzleDefinition.FOUR && !isCompleted(PuzzleDefinition.THREE))) return false;
        puzzle = java.util.Objects.requireNonNull(selected);
        completed = isCompleted(selected);
        reset();
        message = puzzle.instruction() + " Maksimal " + puzzle.actionLimit() + " aksi.";
        return true;
    }

    public boolean run(String source) {
        if (closed || blocked || future != null || replay != null) return false;
        if (source.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 16 * 1024) {
            status = Status.ERROR; message = "Source melebihi 16 KiB; draf tetap tersedia."; return false;
        }
        generation++;
        activeScenario = scenariosPassed = totalActions = totalWaterUsed = 0;
        visible = initial(); output = ""; ready = false;
        status = Status.PREPARING; message = "Menyiapkan Python bundel...";
        cancellation = new AtomicBoolean();
        long id = generation;
        AtomicBoolean token = cancellation;
        PuzzleDefinition selectedPuzzle = puzzle;
        future = worker.submit(() -> bridge.forPuzzle(selectedPuzzle).execute(source, id, token, () -> ready = true));
        return true;
    }

    public void stop() {
        if (future == null && replay == null) return;
        invalidate(); status = Status.STOPPED; message = "Dihentikan. Source dan tampilan terakhir dipertahankan.";
    }
    public void reset() {
        invalidate(); activeScenario = scenariosPassed = totalActions = totalWaterUsed = 0;
        visible = initial(); output = ""; status = Status.READY;
        message = blocked ? "Proses Python belum berhenti. Tutup game sebelum mencoba lagi." : "Fixture direset. Draf dan pencapaian sesi tetap tersedia.";
    }
    private void invalidate() {
        generation++;
        if (cancellation != null) cancellation.set(true);
        replay = null; replayIndex = 0; replayTime = 0; stageFinished = false;
    }

    /** Polls one bounded result, then presents action snapshots independent of simulation timing. */
    public void update(float delta, boolean reducedMotion) {
        if (closed) return;
        if (future != null) {
            if (!future.isDone()) {
                if (status == Status.PREPARING && ready) { status = Status.RUNNING; message = "Menjalankan kode Python..."; }
                return;
            }
            try {
                Execution result = future.get();
                blocked |= !result.reusable();
                if (blocked) message = "Proses Python belum berhenti. Tutup game sebelum mencoba lagi.";
                if (!cancellation.get()) {
                    replay = result; replayIndex = 0; replayTime = 0; stageFinished = false;
                    replayScenarios = result.scenarios().isEmpty() ? List.of(new Scenario(puzzle.scenarioId(),result)) : result.scenarios();
                    output = result.output(); status = Status.REPLAYING; message = "Memutar hasil percobaan...";
                }
            } catch (InterruptedException error) {
                Thread.currentThread().interrupt(); status = Status.ERROR; message = "Eksekusi terinterupsi.";
            } catch (ExecutionException error) {
                if (!cancellation.get()) { status = Status.ERROR; message = "Runtime gagal: " + error.getCause(); }
            } finally { future = null; }
        }
        if (replay == null) return;
        // Each scenario is replayed separately; an intermediate pass never unlocks progress.
        Execution stage = replayScenarios.get(activeScenario).result();
        if (stageFinished) {
            replayTime += Math.max(0, Math.min(delta, 1));
            if (replayTime >= .35f) {
                activeScenario++; replayIndex = 0; replayTime = 0; stageFinished = false;
                visible = initial(); message = "Memutar skenario " + (activeScenario + 1) + "/" + puzzle.scenarioCount() + ".";
            }
            return;
        }
        replayTime += Math.max(0, Math.min(delta, 1));
        while (replayIndex < stage.trace().size() && replayTime >= 0.35f) {
            visible = stage.trace().get(replayIndex++); replayTime -= 0.35f;
        }
        if (replayIndex == stage.trace().size()) {
            visible = stage.snapshot();
            totalActions += visible.actionCount(); totalWaterUsed += visible.waterUsed();
            if (stage.status() == Status.SUCCESS) scenariosPassed++;
            if (activeScenario + 1 < replayScenarios.size()) {
                // Render the final state of this scenario before restoring the next fixture.
                stageFinished = true; replayTime = 0;
                return;
            }
            status = replay.status(); message = replay.message();
            if (status == Status.SUCCESS && scenariosPassed != puzzle.scenarioCount()) {
                status = Status.ERROR; message = "Hasil belum memuat seluruh skenario wajib.";
            }
            if (status == Status.SUCCESS) { completed = true; completedPuzzles.add(puzzle); }
            replay = null;
        }
    }
    public View view() {
        return new View(generation, status, visible, output, message,
            !closed && !blocked && future == null && replay == null, !closed && (replay != null || (future != null && !cancellation.get())), completed, puzzle, puzzle.scenarioId(activeScenario), activeScenario+1,
            puzzle.scenarioCount(), scenariosPassed, totalActions, totalWaterUsed);
    }
    @Override public void close() { if (closed) return; invalidate(); closed = true; worker.shutdown(); }
}
