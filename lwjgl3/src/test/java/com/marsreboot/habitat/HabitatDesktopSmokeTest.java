package com.marsreboot.habitat;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.Window;

/** Real GL/Scene2D smoke check with frame-driven input; does not certify OS keyboard delivery. */
public final class HabitatDesktopSmokeTest extends ApplicationAdapter {
    private HabitatScreen screen;
    private HabitatWorld world;
    private InputProcessor processor;
    private HabitatUi ui;
    private int phase;
    private int assertions;
    private static Throwable failure;

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Mars - desktop smoke check");
        config.setWindowedMode(1280, 720);
        config.setForegroundFPS(60);
        new Lwjgl3Application(new HabitatDesktopSmokeTest(), config);
        if (failure != null) throw new AssertionError("Desktop smoke failed", failure);
    }

    @Override public void create() {
        screen = new HabitatScreen();
        world = field(screen, "world", HabitatWorld.class);
        ui = field(screen, "ui", HabitatUi.class);
        screen.show();
        screen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        processor = Gdx.input.getInputProcessor();
    }

    @Override public void render() {
        try {
            switch (phase++) {
                case 0 -> {
                    press(Keys.E); frame(0);
                    check(world.mode() == HabitatWorld.Mode.EXPLORING, "far E ignored");
                    walk(Keys.D, 0.5f);
                    near(world.x(), 250, "screen dispatch moves astronaut");
                    walk(Keys.W, 0.9f);
                    check(world.nearbyFacility() == HabitatWorld.Facility.PANEL, "panel hint available");
                    press(Keys.E); frame(0);
                    check(world.mode() == HabitatWorld.Mode.INSPECTION, "inspection state");
                    check(field(ui, "inspection", Window.class).isVisible(), "inspection visible");
                    snapshot("inspection");
                }
                case 1 -> {
                    float x = world.x(); walk(Keys.D, 1);
                    near(world.x(), x, "inspection locks motion");
                    press(Keys.ESCAPE); frame(0);
                    check(ui.stage().getKeyboardFocus() == null, "inspection releases focus");
                    walk(Keys.D, 150f / HabitatWorld.SPEED);
                    walk(Keys.W, 118f / HabitatWorld.SPEED);
                    walk(Keys.D, 350f / HabitatWorld.SPEED);
                    walk(Keys.S, 15f / HabitatWorld.SPEED);
                    press(Keys.E); frame(0);
                    check(world.mode() == HabitatWorld.Mode.TERMINAL, "terminal reachable");
                    TextArea editor = field(ui, "editor", TextArea.class);
                    check(ui.stage().getKeyboardFocus() == editor, "terminal focuses TextArea");
                    String before = editor.getText();
                    processor.keyTyped('w'); frame(0);
                    check(!editor.getText().equals(before), "typing reaches editor");
                    float xTerminal = world.x(), yTerminal = world.y();
                    walk(Keys.D, 0.5f); walk(Keys.W, 0.5f);
                    near(world.x(), xTerminal, "editor locks horizontal movement");
                    near(world.y(), yTerminal, "editor locks vertical movement");
                    snapshot("terminal");
                }
                case 2 -> {
                    TextArea editor = field(ui, "editor", TextArea.class);
                    String draft = editor.getText();
                    press(Keys.ESCAPE); frame(0);
                    check(world.mode() == HabitatWorld.Mode.EXPLORING, "Esc exits focused editor");
                    check(ui.stage().getKeyboardFocus() == null, "editor focus released");
                    press(Keys.E); frame(0);
                    check(editor.getText().equals(draft), "draft preserved on reopen");
                    Gdx.graphics.setWindowedMode(1440, 900);
                }
                case 3 -> { frame(0); checkResize(); snapshot("terminal-1440x900"); Gdx.graphics.setWindowedMode(900, 600); }
                case 4 -> { frame(0); checkResize(); snapshot("terminal-900x600"); Gdx.graphics.setWindowedMode(640, 480); }
                case 5 -> {
                    frame(0); checkResize(); snapshot("terminal-640x480");
                    press(Keys.ESCAPE); frame(0);
                    float x = world.x(); walk(Keys.A, 0.1f);
                    near(world.x(), x - 18, "controls restored after terminal and resize");
                    screen.hide();
                    check(Gdx.input.getInputProcessor() == null, "hide releases processor");
                    System.out.println("PASS: " + assertions + " real desktop/Scene2D assertions; OS held keys not simulated.");
                    Gdx.app.exit();
                }
                default -> frame(0);
            }
        } catch (Throwable error) { failure = error; error.printStackTrace(); Gdx.app.exit(); }
    }

    private void checkResize() {
        Stage stage = ui.stage();
        Window window = field(ui, "terminal", Window.class);
        near(stage.getViewport().getWorldWidth(), 960, "UI virtual width stable");
        near(stage.getViewport().getWorldHeight(), 600, "UI virtual height stable");
        check(window.getX() >= 0 && window.getRight() <= 960 && window.getY() >= 0 && window.getTop() <= 600,
            "exit window remains inside UI canvas");
        check(stage.getKeyboardFocus() == field(ui, "editor", TextArea.class), "resize retains editor focus");
    }
    private void press(int key) { processor.keyDown(key); processor.keyUp(key); }
    private void walk(int key, float seconds) { processor.keyDown(key); frame(seconds); processor.keyUp(key); }
    private void frame(float delta) { screen.render(delta); }
    private void snapshot(String name) {
        Pixmap pixmap = Pixmap.createFromFrameBuffer(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
        try { PixmapIO.writePNG(Gdx.files.local("../lwjgl3/build/reports/habitat/" + name + ".png"), pixmap, -1, true); }
        finally { pixmap.dispose(); }
    }
    private void check(boolean value, String message) { if (!value) throw new AssertionError(message); assertions++; }
    private void near(float actual, float expected, String message) { check(Math.abs(actual - expected) < 0.03f, message + ": " + actual); }
    private static <T> T field(Object target, String name, Class<T> type) {
        try { var field = target.getClass().getDeclaredField(name); field.setAccessible(true); return type.cast(field.get(target)); }
        catch (ReflectiveOperationException error) { throw new AssertionError(error); }
    }
    @Override public void resize(int width, int height) { if (screen != null) screen.resize(width, height); }
    @Override public void dispose() { if (screen != null) screen.dispose(); }
}
