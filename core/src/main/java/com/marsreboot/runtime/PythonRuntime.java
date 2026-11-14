package com.marsreboot.runtime;

import com.badlogic.gdx.utils.JsonValue;
import com.marsreboot.robot.RobotSimulation;
import com.marsreboot.robot.RobotSimulation.Snapshot;
import com.marsreboot.runtime.RunCoordinator.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import static com.marsreboot.runtime.RuntimeProtocol.*;

/** One isolated CPython process per run. Isolation is lifecycle separation, not a security sandbox. */
public final class PythonRuntime implements Bridge {
    private final Path executable;
    private final long startupMillis, computeMillis;
    public PythonRuntime(Path executable) { this(executable, 15_000, 5_000); }
    public PythonRuntime(Path executable, long startupMillis, long computeMillis) {
        this.executable = executable.toAbsolutePath(); this.startupMillis = startupMillis; this.computeMillis = computeMillis;
    }
    public static PythonRuntime bundled() {
        String configured = System.getProperty("mars.pythonDir");
        if (configured != null) return new PythonRuntime(Path.of(configured, "python.exe"));
        try {
            Path location = Path.of(PythonRuntime.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (Files.isRegularFile(location)) return new PythonRuntime(location.getParent().resolve("runtime/python/python.exe"));
            // IntelliJ/direct launch: derive the repo from compiled classes, never from PATH.
            for (Path base = location; base != null; base = base.getParent()) {
                if (Files.isRegularFile(base.resolve("settings.gradle"))) {
                    return new PythonRuntime(base.resolve("build/python/windows-x64/python.exe"));
                }
            }
            throw new IOException("Jalankan preparePython dan tentukan -Dmars.pythonDir untuk lokasi build khusus.");
        } catch (Exception error) { throw new IllegalStateException("Lokasi runtime tidak ditemukan.", error); }
    }

    @Override public Execution execute(String source, long generation, AtomicBoolean cancelled, Runnable readyCallback) {
        RobotSimulation simulation = new RobotSimulation(RobotSimulation.Fixture.puzzleOne());
        List<Snapshot> trace = new ArrayList<>();
        Output output = new Output();
        Process process = null;
        Path scratch = null;
        ScheduledExecutorService watchdog = null;
        Thread stderrThread = null;
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        AtomicBoolean timedOut = new AtomicBoolean();
        boolean reusable = true;
        AtomicLong deadline = new AtomicLong(System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(startupMillis));
        Status status = Status.ERROR;
        String message = "Runtime belum siap.";
        boolean normal = false;
        try {
            if (source.getBytes(StandardCharsets.UTF_8).length > 16 * 1024) throw new IOException("Source melebihi 16 KiB.");
            if (!Files.isRegularFile(executable)) throw new IOException("Python bundel tidak ditemukan. Jalankan task preparePython.");
            scratch = Files.createTempDirectory("mars-p1-");
            Path runner = scratch.resolve("runner.py");
            try (InputStream resource = PythonRuntime.class.getResourceAsStream("/python/runner.py")) {
                if (resource == null) throw new IOException("Runner Python tidak ada dalam resource game.");
                Files.copy(resource, runner);
            }
            ProcessBuilder builder = new ProcessBuilder(executable.toString(), "-I", "-X", "utf8", "-u", "-B", runner.toString());
            builder.directory(scratch.toFile());
            Map<String,String> environment = builder.environment(); environment.clear();
            for (String key : List.of("SystemRoot", "WINDIR")) {
                String value = System.getenv(key); if (value != null) environment.put(key, value);
            }
            environment.put("TEMP", scratch.toString()); environment.put("TMP", scratch.toString());
            process = builder.start();
            Process child = process;
            stderrThread = new Thread(() -> {
                try (InputStream stream = child.getErrorStream()) {
                    for (int value; (value = stream.read()) != -1;) if (stderr.size() < 8192) stderr.write(value);
                } catch (IOException ignored) { /* Closing pipes is part of cancellation. */ }
            }, "mars-python-stderr");
            stderrThread.setDaemon(true); stderrThread.start();
            watchdog = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r,"mars-python-watchdog"); t.setDaemon(true); return t;
            });
            watchdog.scheduleWithFixedDelay(() -> {
                if (!child.isAlive()) return;
                if (cancelled.get() || System.nanoTime() >= deadline.get()) {
                    if (!cancelled.get()) timedOut.set(true);
                    terminate(child);
                }
            }, 0, 20, TimeUnit.MILLISECONDS);
            RuntimeProtocol protocol = new RuntimeProtocol(generation);
            JsonValue start = object(); start.addChild("source",new JsonValue(source));
            write(process, protocol.encode("start",start), 128 * 1024);
            boolean ready = false, ended = false;
            long requestId = 0;
            InputStream input = new BufferedInputStream(process.getInputStream());
            for (String line; (line = readLine(input,16 * 1024)) != null;) {
                if (cancelled.get() || timedOut.get()) break;
                JsonValue frame = protocol.decode(line), payload = frame.get("payload");
                String type = string(frame,"type");
                if (ended) throw new IOException("Frame setelah akhir program.");
                if (!ready && !type.equals("ready")) throw new IOException("Ready belum diterima.");
                switch (type) {
                    case "ready" -> {
                        if (ready) throw new IOException("Ready ganda.");
                        ready = true; deadline.set(System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(computeMillis)); readyCallback.run();
                    }
                    case "request" -> {
                        if (integer(payload,"requestId") != ++requestId) throw new IOException("Request ID tidak berurutan.");
                        JsonValue args = required(payload,"args");
                        if (!args.isArray()) throw new IOException("Argumen bukan array.");
                        String name = string(payload,"name");
                        int arity = name.equals("move") ? 1 : 0;
                        if (args.size != arity) throw new IOException("Jumlah argumen API tidak sah.");
                        JsonValue result;
                        switch (name) {
                            case "move" -> {
                                if (!args.get(0).isString()) throw new IOException("Arah harus string.");
                                result = new JsonValue(simulation.move(args.get(0).asString()));
                            }
                            case "harvest" -> result = new JsonValue(simulation.harvest());
                            case "can_harvest" -> result = new JsonValue(simulation.canHarvest());
                            case "get_pos" -> {
                                var position = simulation.getPos(); result = new JsonValue(JsonValue.ValueType.array);
                                result.addChild(new JsonValue((long)position.x())); result.addChild(new JsonValue((long)position.y()));
                            }
                            default -> throw new IOException("API tidak tersedia: " + name);
                        }
                        if (name.equals("move") || name.equals("harvest")) {
                            if (trace.size() >= 256) { status = Status.LIMIT_EXCEEDED; throw new IOException("Trace penuh."); }
                            trace.add(simulation.snapshot());
                        }
                        JsonValue response = object(); response.addChild("requestId",new JsonValue(requestId));
                        response.addChild("ok",new JsonValue(true)); response.addChild("value",result);
                        write(process, protocol.encode("response",response), 16 * 1024);
                    }
                    case "output" -> output.append(string(payload,"text"));
                    case "output_truncated" -> output.truncate();
                    case "error" -> {
                        String detail = string(payload,"message");
                        JsonValue errorLine = payload.get("line");
                        message = (errorLine != null && errorLine.isLong() ? "Baris " + errorLine.asLong() + ": " : "") + detail;
                        ended = true;
                    }
                    case "finished" -> { ended = true; normal = true; }
                    default -> throw new IOException("Jenis frame tidak dikenal: " + type);
                }
            }
            if (!process.waitFor(1500,TimeUnit.MILLISECONDS)) throw new IOException("Proses tidak selesai setelah EOF.");
            if (normal && process.exitValue() == 0 && !cancelled.get() && !timedOut.get()) {
                Snapshot finalState = simulation.finish();
                status = finalState.outcome() == RobotSimulation.Outcome.SUCCESS ? Status.SUCCESS : Status.INCOMPLETE;
                message = finalState.message();
            } else if (normal && process.exitValue() != 0) {
                message = "Proses keluar dengan kode " + process.exitValue() + "; hasil tidak diterima.";
            } else if (!ended && !cancelled.get() && !timedOut.get()) {
                message = "Runtime berhenti tanpa hasil normal.";
            }
        } catch (Exception error) {
            message = error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
            if (simulation.snapshot().outcome() == RobotSimulation.Outcome.LIMIT_EXCEEDED) status = Status.LIMIT_EXCEEDED;
        } finally {
            if (process != null) terminate(process);
            if (watchdog != null) watchdog.shutdownNow();
            if (stderrThread != null) try { stderrThread.join(1000); } catch (InterruptedException error) { Thread.currentThread().interrupt(); }
            if (process != null && process.isAlive()) { reusable = false; status = Status.ERROR; message = "Proses Python belum berhenti. Tutup game sebelum mencoba lagi."; }
            if (process != null) {
                for (Closeable stream : List.of(process.getInputStream(), process.getErrorStream(), process.getOutputStream())) {
                    try { stream.close(); } catch (IOException ignored) { }
                }
            }
            // Only files created by this invocation; never recurse over a player-controlled directory.
            if (scratch != null) {
                try { Files.deleteIfExists(scratch.resolve("runner.py")); Files.deleteIfExists(scratch); } catch (IOException ignored) { }
            }
        }
        if (cancelled.get()) { status = Status.STOPPED; message = "Percobaan dihentikan."; }
        else if (timedOut.get()) { status = Status.TIMEOUT; message = "Batas waktu Python terlampaui; periksa kondisi berhenti loop."; }
        if (!reusable) { status = Status.ERROR; message = "Proses Python belum berhenti. Tutup game sebelum mencoba lagi."; }
        if (status == Status.ERROR && stderr.size() > 0) message += "\n" + stderr.toString(StandardCharsets.UTF_8);
        return new Execution(status,simulation.snapshot(),trace,output.text(),message,reusable);
    }

    private static void write(Process process, String frame, int limit) throws IOException {
        byte[] bytes = (frame + "\n").getBytes(StandardCharsets.UTF_8);
        if (bytes.length > limit) throw new IOException("Frame kirim melebihi batas.");
        process.getOutputStream().write(bytes); process.getOutputStream().flush();
    }
    private static void terminate(Process process) {
        if (!process.isAlive()) return;
        try {
            process.destroy();
            if (!process.waitFor(500,TimeUnit.MILLISECONDS)) { process.destroyForcibly(); process.waitFor(1000,TimeUnit.MILLISECONDS); }
        } catch (InterruptedException error) { process.destroyForcibly(); Thread.currentThread().interrupt(); }
    }

    /** A second host-side cap, since transport data cannot be trusted to enforce its own limits. */
    private static final class Output {
        private final StringBuilder text = new StringBuilder();
        private int bytes, lines;
        private boolean truncated;
        void append(String value) {
            if (truncated) return;
            for (int i = 0; i < value.length();) {
                int cp = value.codePointAt(i); String character = new String(Character.toChars(cp));
                int size = character.getBytes(StandardCharsets.UTF_8).length;
                if (bytes + size > 8192 || lines >= 64) { truncate(); return; }
                text.append(character); bytes += size; if (cp == '\n') lines++; i += Character.charCount(cp);
            }
        }
        void truncate() { if (!truncated) { truncated = true; text.append("\n[Output dipotong pada batas.]\n"); } }
        String text() { return text.toString(); }
    }
}
