import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.function.DoubleUnaryOperator;

/**
 * Generates the project-owned SFX as 16-bit mono 22.05 kHz WAV (no third-party audio).
 * Run: java tools/GenerateSfx.java assets/audio
 */
public final class GenerateSfx {
    private static final int RATE = 22050;

    public static void main(String[] args) throws IOException {
        Path dir = Path.of(args.length > 0 ? args[0] : "assets/audio");
        Files.createDirectories(dir);
        write(dir, "sfx_click", 0.05, t -> sine(1300, t) * env(t, 0.05, 0.002));
        write(dir, "sfx_water", 0.30, t -> {
            double f = 700 - 900 * t;
            double bubble = sine(f, t) * (0.6 + 0.4 * sine(24, t));
            return bubble * env(t, 0.30, 0.01) * 0.8;
        });
        write(dir, "sfx_harvest", 0.22, t -> {
            double f = t < 0.09 ? 660 : 990;
            return (sine(f, t) + 0.3 * sine(f * 2, t)) * env(t, 0.22, 0.005) * 0.6;
        });
        double[] notes = {523.25, 659.25, 783.99, 1046.5};
        write(dir, "sfx_success", 0.70, t -> {
            int i = Math.min(3, (int) (t / 0.13));
            double lt = t - i * 0.13;
            double e = Math.exp(-3.0 * lt);
            return (sine(notes[i], t) + 0.25 * sine(notes[i] * 2, t)) * e * 0.55;
        });
        write(dir, "sfx_fail", 0.34, t -> {
            double f = 220 - 90 * t;
            double square = Math.signum(sine(f, t)) * 0.35 + sine(f * 0.5, t) * 0.4;
            return square * env(t, 0.34, 0.01) * 0.6;
        });
    }

    private static double sine(double hz, double t) { return Math.sin(2 * Math.PI * hz * t); }

    /** Linear attack, then linear decay to zero at the end. */
    private static double env(double t, double len, double attack) {
        double a = Math.min(1.0, t / attack);
        return a * Math.max(0.0, 1.0 - t / len);
    }

    private static void write(Path dir, String name, double seconds, DoubleUnaryOperator f) throws IOException {
        int n = (int) (seconds * RATE);
        ByteBuffer data = ByteBuffer.allocate(n * 2).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < n; i++) {
            double v = Math.max(-1.0, Math.min(1.0, f.applyAsDouble(i / (double) RATE)));
            data.putShort((short) Math.round(v * 0.8 * Short.MAX_VALUE));
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteBuffer h = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN);
        h.put("RIFF".getBytes()).putInt(36 + n * 2).put("WAVE".getBytes());
        h.put("fmt ".getBytes()).putInt(16).putShort((short) 1).putShort((short) 1)
            .putInt(RATE).putInt(RATE * 2).putShort((short) 2).putShort((short) 16);
        h.put("data".getBytes()).putInt(n * 2);
        out.write(h.array());
        out.write(data.array());
        Files.write(dir.resolve(name + ".wav"), out.toByteArray());
    }
}
