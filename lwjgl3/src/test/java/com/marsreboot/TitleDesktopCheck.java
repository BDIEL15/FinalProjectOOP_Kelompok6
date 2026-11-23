package com.marsreboot;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import java.util.concurrent.atomic.AtomicInteger;

/** Real GL check of the title screen: story reveal timing, screenshots, and Enter/click handoff. */
public final class TitleDesktopCheck extends ApplicationAdapter {
    private final AtomicInteger started = new AtomicInteger();
    private TitleScreen screen;
    private float elapsed;
    private int shots;
    private static Throwable failure;

    public static void main(String[] args) {
        var config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Mars - title desktop check");
        config.setWindowedMode(960, 600);
        config.setForegroundFPS(60);
        new Lwjgl3Application(new TitleDesktopCheck(), config);
        if (failure != null) throw new AssertionError("Title desktop check failed", failure);
        System.out.println("PASS: title screen story timing, screenshots and Enter handoff (real OpenGL).");
    }

    @Override
    public void create() {
        screen = new TitleScreen(started::incrementAndGet);
        screen.show();
        screen.resize(960, 600);
    }

    @Override
    public void render() {
        try {
            float dt = Gdx.graphics.getDeltaTime();
            elapsed += dt;
            screen.render(dt);
            if (shots == 0 && elapsed > 0.4f) { capture("title-start"); shots = 1; }
            if (shots == 1 && elapsed > 4.8f) {
                check(TitleScreen.visibleStoryLines(4.8f) == 3 && TitleScreen.visibleStoryLines(0f) == 0, "story reveals one line per step");
                capture("title-ready");
                shots = 2;
                check(started.get() == 0, "does not start before input");
                Gdx.input.getInputProcessor().keyDown(Keys.ENTER);
                check(started.get() == 1, "Enter starts exactly once");
                Gdx.input.getInputProcessor().touchDown(10, 10, 0, 0);
                check(started.get() == 1, "second input does not start twice");
                Gdx.app.exit();
            }
        } catch (Throwable t) {
            failure = t;
            Gdx.app.exit();
        }
    }

    private static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }

    private void capture(String name) {
        Pixmap image = Pixmap.createFromFrameBuffer(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
        try {
            PixmapIO.writePNG(Gdx.files.absolute(System.getProperty("mars.reportDir", "../lwjgl3/build/reports/puzzle") + "/" + name + ".png"), image, -1, true);
        } finally {
            image.dispose();
        }
    }

    @Override
    public void dispose() {
        screen.dispose();
    }
}
