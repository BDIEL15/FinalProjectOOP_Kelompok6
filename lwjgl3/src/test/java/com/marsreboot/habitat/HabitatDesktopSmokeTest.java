package com.marsreboot.habitat;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.viewport.Viewport;

/** Real GL/Scene2D smoke check for FollowCamera, smooth movement, direct Rover click, and resizable Terminal. */
public final class HabitatDesktopSmokeTest extends ApplicationAdapter {
    private HabitatScreen screen;
    private HabitatWorld world;
    private Viewport worldViewport;
    private FollowCamera followCamera;
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

    @Override
    public void create() {
        screen = new HabitatScreen();
        world = field(screen, "world", HabitatWorld.class);
        worldViewport = field(screen, "viewport", Viewport.class);
        followCamera = field(screen, "followCamera", FollowCamera.class);
        ui = field(screen, "ui", HabitatUi.class);
        screen.show();
        screen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        processor = Gdx.input.getInputProcessor();
    }

    @Override
    public void render() {
        try {
            switch (phase++) {
                case 0 -> {
                    press(Keys.E); frame(0);
                    check(world.mode() == HabitatWorld.Mode.EXPLORING, "far E ignored");
                    check(worldViewport.getWorldWidth() < HabitatWorld.WIDTH
                        && worldViewport.getWorldHeight() < HabitatWorld.HEIGHT,
                        "camera does not reveal entire expanded map at once");

                    // Walk North-East above Airlock/Solar to North side of Telemetry Panel (x~505, y~745)
                    float camStartX = followCamera.camX();
                    walkSteps(Keys.D, 0.95f);
                    walkSteps(Keys.W, 2.15f);
                    walkSteps(Keys.D, 0.55f);
                    // Wait for coast settle
                    frameSteps(0.4f);
                    check(followCamera.camX() > camStartX, "follow camera tracked player east");
                    check(world.nearbyFacility() == HabitatWorld.Facility.PANEL,
                        "telemetry panel reachable: x=" + world.x() + ", y=" + world.y());
                    press(Keys.E); frame(0);
                    check(world.mode() == HabitatWorld.Mode.INSPECTION, "inspection state");
                    check(field(ui, "inspection", Window.class).isVisible(), "inspection visible");
                    snapshot("inspection");
                }
                case 1 -> {
                    float x = world.x();
                    walkSteps(Keys.D, 0.4f);
                    near(world.x(), x, 0.05f, "inspection locks motion");
                    press(Keys.ESCAPE); frame(0);

                    // Walk East over wide open Northern Corridor (above PANEL & PARTITION), then South to Bio-Dome West
                    walkSteps(Keys.D, 2.15f);
                    walkSteps(Keys.S, 1.55f);
                    frameSteps(0.6f); // allow camera to glide right onto Bio-Dome
                    check(world.nearbyFacility() == HabitatWorld.Facility.ROVER,
                        "astronaut reached Bio-Dome Rover bay: x=" + world.x() + ", y=" + world.y());
                    snapshot("outpost-biodome");

                    // Click directly on the Rover inside Bio-Dome in world coordinates!
                    HabitatWorld.Box roverBox = HabitatWorld.roverBayBox(0, 0, 2);
                    Vector3 screenPt = worldViewport.project(new Vector3(roverBox.centerX(), roverBox.centerY(), 0f));
                    int sx = Math.round(screenPt.x);
                    int sy = Gdx.graphics.getHeight() - Math.round(screenPt.y);
                    processor.touchDown(sx, sy, 0, Buttons.LEFT);
                    processor.touchUp(sx, sy, 0, Buttons.LEFT);
                    frameSteps(0.8f); // let smart framing pan Bio-Dome clearly right of terminal
                    check(world.mode() == HabitatWorld.Mode.TERMINAL, "direct click on Rover opens Terminal");

                    TextArea editor = field(ui, "editor", TextArea.class);
                    check(ui.stage().getKeyboardFocus() == editor, "terminal focuses TextArea");
                    String before = editor.getText();
                    processor.keyTyped('w'); frame(0);
                    check(!editor.getText().equals(before), "typing reaches editor");
                    float xTerminal = world.x(), yTerminal = world.y();
                    walkSteps(Keys.D, 0.3f); walkSteps(Keys.W, 0.3f);
                    near(world.x(), xTerminal, 0.05f, "editor locks horizontal movement");
                    near(world.y(), yTerminal, 0.05f, "editor locks vertical movement");

                    // Drag bottom-right corner grip of Terminal to verify interactive resize!
                    Window term = field(ui, "terminal", Window.class);
                    float initW = term.getWidth(), initH = term.getHeight();
                    Vector2 cornerStage = new Vector2(term.getRight() - 4f, term.getY() + 4f);
                    Vector2 endStage = new Vector2(cornerStage.x + 80f, Math.max(48f, cornerStage.y - 36f));
                    Vector2 pStart = ui.stage().stageToScreenCoordinates(new Vector2(cornerStage));
                    Vector2 pEnd = ui.stage().stageToScreenCoordinates(new Vector2(endStage));
                    processor.touchDown(Math.round(pStart.x), Math.round(pStart.y), 0, Buttons.LEFT);
                    processor.touchDragged(Math.round(pEnd.x), Math.round(pEnd.y), 0);
                    processor.touchUp(Math.round(pEnd.x), Math.round(pEnd.y), 0, Buttons.LEFT);
                    frame(0);
                    check(term.getWidth() > initW + 30f, "dragging terminal corner widens terminal: " + term.getWidth());
                    check(term.getHeight() > initH + 15f, "dragging terminal corner resizes height: " + term.getHeight());
                    snapshot("terminal");
                }
                case 2 -> {
                    TextArea editor = field(ui, "editor", TextArea.class);
                    String draft = editor.getText();
                    press(Keys.ESCAPE); frame(0);
                    check(world.mode() == HabitatWorld.Mode.EXPLORING, "Esc exits focused editor");
                    check(ui.stage().getKeyboardFocus() == null, "editor focus released");
                    press(Keys.E); frame(0);
                    check(world.mode() == HabitatWorld.Mode.TERMINAL, "E reopens terminal near Bio-Dome: near=" + world.nearbyFacility());
                    check(editor.getText().equals(draft), "draft preserved on reopen");
                    Gdx.graphics.setWindowedMode(1440, 900);
                }
                case 3 -> { frame(0.05f); checkResize(); snapshot("terminal-1440x900"); Gdx.graphics.setWindowedMode(900, 600); }
                case 4 -> { frame(0.05f); checkResize(); snapshot("terminal-900x600"); Gdx.graphics.setWindowedMode(640, 480); }
                case 5 -> {
                    frame(0.05f); checkResize(); snapshot("terminal-640x480");
                    press(Keys.ESCAPE); frame(0);
                    float x = world.x();
                    walkSteps(Keys.A, 0.25f);
                    check(world.x() < x - 10f, "controls restored after terminal and resize");
                    screen.hide();
                    check(Gdx.input.getInputProcessor() == null, "hide releases processor");
                    System.out.println("PASS: " + assertions + " real desktop/Scene2D assertions; OS held keys not simulated.");
                    Gdx.app.exit();
                }
                default -> frame(0);
            }
        } catch (Throwable error) {
            failure = error;
            error.printStackTrace();
            Gdx.app.exit();
        }
    }

    private void checkResize() {
        Stage stage = ui.stage();
        Window window = field(ui, "terminal", Window.class);
        float sw = stage.getViewport().getWorldWidth();
        float sh = stage.getViewport().getWorldHeight();
        check(sw >= 960f && sh >= 600f, "ExtendViewport fills window without black bars: " + sw + "x" + sh);
        check(window.getX() >= 0 && window.getRight() <= sw && window.getY() >= 0 && window.getTop() <= sh,
            "terminal remains clamped inside UI canvas");
        check(stage.getKeyboardFocus() == field(ui, "editor", TextArea.class), "resize retains editor focus");
    }

    private void press(int key) { processor.keyDown(key); processor.keyUp(key); }

    private void walkSteps(int key, float seconds) {
        processor.keyDown(key);
        frameSteps(seconds);
        processor.keyUp(key);
    }

    private void frameSteps(float seconds) {
        float step = 1f / 60f;
        int count = Math.max(1, Math.round(seconds / step));
        for (int i = 0; i < count; i++) {
            frame(step);
        }
    }

    private void frame(float delta) { screen.render(delta); }

    private void snapshot(String name) {
        Pixmap pixmap = Pixmap.createFromFrameBuffer(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
        try { PixmapIO.writePNG(Gdx.files.local("../lwjgl3/build/reports/habitat/" + name + ".png"), pixmap, -1, true); }
        finally { pixmap.dispose(); }
    }

    private void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
        assertions++;
    }

    private void near(float actual, float expected, float eps, String message) {
        check(Math.abs(actual - expected) <= eps, message + ": " + actual + " vs " + expected);
    }

    private static <T> T field(Object target, String name, Class<T> type) {
        try {
            var field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return type.cast(field.get(target));
        } catch (ReflectiveOperationException error) {
            throw new AssertionError(error);
        }
    }

    @Override public void resize(int width, int height) { if (screen != null) screen.resize(width, height); }
    @Override public void dispose() { if (screen != null) screen.dispose(); }
}
