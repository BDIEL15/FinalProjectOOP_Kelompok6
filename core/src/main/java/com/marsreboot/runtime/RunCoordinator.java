package com.marsreboot.runtime;

import com.marsreboot.robot.RobotSimulation;
import com.marsreboot.robot.RobotSimulation.Snapshot;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/** Render-thread controller; each worker owns an independent simulation and process. */
public final class RunCoordinator implements AutoCloseable {
    public enum Status { READY, PREPARING, RUNNING, REPLAYING, SUCCESS, INCOMPLETE, ERROR, LIMIT_EXCEEDED, TIMEOUT, STOPPED }
    public record Execution(Status status, Snapshot snapshot, List<Snapshot> trace, String output, String message, boolean reusable) {
        public Execution { trace = List.copyOf(trace); }
        public Execution(Status status, Snapshot snapshot, List<Snapshot> trace, String output, String message) {
            this(status, snapshot, trace, output, message, true);
        }
    }
    public record View(long generation, Status status, Snapshot snapshot, String output, String message,
                       boolean canRun, boolean canStop, boolean completed) { }
    @FunctionalInterface public interface Bridge {
        Execution execute(String source, long generation, AtomicBoolean cancelled, Runnable ready);
    }
    private final Bridge bridge;
    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "mars-python-run"); t.setDaemon(true); return t;
    });
    private long generation;
    private Future<Execution> future;
    private AtomicBoolean cancellation;
    private volatile boolean ready;
    private boolean closed, completed, blocked;
    private Status status = Status.READY;
    private Snapshot visible = initial();
    private String output = "", message = "Panen dua tanaman matang. Maksimal 6 aksi.";
    private Execution replay;
    private int replayIndex;
    private float replayTime;

    public RunCoordinator(Bridge bridge) { this.bridge = bridge; }
    private static Snapshot initial() { return new RobotSimulation(RobotSimulation.Fixture.puzzleOne()).snapshot(); }

    public boolean run(String source) {
        if (closed || blocked || future != null || replay != null) return false;
        if (source.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 16 * 1024) {
            status = Status.ERROR; message = "Source melebihi 16 KiB; draf tetap tersedia."; return false;
        }
        generation++;
        visible = initial(); output = ""; ready = false;
        status = Status.PREPARING; message = "Menyiapkan Python bundel...";
        cancellation = new AtomicBoolean();
        long id = generation;
        AtomicBoolean token = cancellation;
        future = worker.submit(() -> bridge.execute(source, id, token, () -> ready = true));
        return true;
    }

    public void stop() {
        if (future == null && replay == null) return;
        invalidate(); status = Status.STOPPED; message = "Dihentikan. Source dan tampilan terakhir dipertahankan.";
    }
    public void reset() {
        invalidate(); visible = initial(); output = ""; status = Status.READY;
        message = blocked ? "Proses Python belum berhenti. Tutup game sebelum mencoba lagi." : "Fixture direset. Draf dan pencapaian sesi tetap tersedia.";
    }
    private void invalidate() {
        generation++;
        if (cancellation != null) cancellation.set(true);
        replay = null; replayIndex = 0; replayTime = 0;
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
                    replay = result; replayIndex = 0; replayTime = 0;
                    output = result.output(); status = Status.REPLAYING; message = "Memutar hasil percobaan...";
                }
            } catch (InterruptedException error) {
                Thread.currentThread().interrupt(); status = Status.ERROR; message = "Eksekusi terinterupsi.";
            } catch (ExecutionException error) {
                if (!cancellation.get()) { status = Status.ERROR; message = "Runtime gagal: " + error.getCause(); }
            } finally { future = null; }
        }
        if (replay == null) return;
        // Reduced motion removes interpolation, not the opportunity to Stop a replay.
        replayTime += Math.max(0, Math.min(delta, 1));
        while (replayIndex < replay.trace().size() && replayTime >= 0.35f) {
            visible = replay.trace().get(replayIndex++); replayTime -= 0.35f;
        }
        if (replayIndex == replay.trace().size()) {
            visible = replay.snapshot(); status = replay.status(); message = replay.message();
            if (status == Status.SUCCESS) completed = true;
            replay = null;
        }
    }
    public View view() {
        return new View(generation, status, visible, output, message,
            !closed && !blocked && future == null && replay == null, !closed && (replay != null || (future != null && !cancellation.get())), completed);
    }
    @Override public void close() { if (closed) return; invalidate(); closed = true; worker.shutdown(); }
}
