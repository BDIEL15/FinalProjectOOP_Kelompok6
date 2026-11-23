package com.marsreboot.habitat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import java.util.EnumMap;
import java.util.Map;

/**
 * Small SFX player. Pure presentation: never affects simulation. Fails silent when audio is
 * unavailable (headless, no device) so gameplay and tests are unaffected.
 */
public final class SoundBank {
    public enum Sfx {
        CLICK("sfx_click", 0.5f), WATER("sfx_water", 0.7f), HARVEST("sfx_harvest", 0.7f),
        SUCCESS("sfx_success", 0.8f), FAIL("sfx_fail", 0.6f);

        final String file;
        final float volume;

        Sfx(String file, float volume) {
            this.file = file;
            this.volume = volume;
        }
    }

    private static final Map<Sfx, Sound> SOUNDS = new EnumMap<>(Sfx.class);
    private static boolean loaded;
    private static boolean muted;

    private SoundBank() {}

    public static boolean isMuted() { return muted; }

    public static void setMuted(boolean value) { muted = value; }

    public static void play(Sfx sfx) {
        if (muted) return;
        load();
        Sound sound = SOUNDS.get(sfx);
        if (sound != null) sound.play(sfx.volume);
    }

    private static void load() {
        if (loaded) return;
        loaded = true;
        if (Gdx.audio == null || Gdx.files == null) return;
        for (Sfx sfx : Sfx.values()) {
            try {
                String path = "audio/" + sfx.file + ".wav";
                FileHandle handle = Gdx.files.internal(path);
                if (!handle.exists()) handle = Gdx.files.local("assets/" + path);
                if (handle.exists()) SOUNDS.put(sfx, Gdx.audio.newSound(handle));
            } catch (RuntimeException ignored) {
                // No audio device or bad file: stay silent.
            }
        }
    }

    public static void dispose() {
        for (Sound s : SOUNDS.values()) s.dispose();
        SOUNDS.clear();
        loaded = false;
    }
}
