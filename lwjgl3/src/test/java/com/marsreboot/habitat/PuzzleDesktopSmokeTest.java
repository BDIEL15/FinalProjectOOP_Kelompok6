package com.marsreboot.habitat;

import com.badlogic.gdx.*;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.marsreboot.runtime.RunCoordinator.Status;

/** Real GL, FollowCamera, world Rover click, resizable simple Terminal, smooth replay, and Puzzle 1 flow verification. */
public final class PuzzleDesktopSmokeTest extends ApplicationAdapter {
    private HabitatScreen screen;
    private HabitatUi ui;
    private HabitatWorld world;
    private Viewport worldViewport;
    private InputProcessor input;
    private TextArea editor;
    private Window terminal;
    private int phase, assertions;
    private boolean checkedP4Intermediate;
    private long phaseStart;
    private static Throwable failure;
    private static final String REFERENCE = "harvest()\nmove('east')\nharvest()\nprint(get_pos())";

    public static void main(String[] args) {
        var config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Mars - Python puzzle desktop check");
        config.setWindowedMode(1280, 720);
        config.setForegroundFPS(60);
        new Lwjgl3Application(new PuzzleDesktopSmokeTest(), config);
        if (failure != null) throw new AssertionError("Puzzle desktop smoke failed", failure);
    }

    @Override
    public void create() {
        screen = new HabitatScreen();
        screen.show();
        screen.resize(1280, 720);
        ui = field(screen, "ui", HabitatUi.class);
        world = field(screen, "world", HabitatWorld.class);
        worldViewport = field(screen, "viewport", Viewport.class);
        input = Gdx.input.getInputProcessor();
        editor = ui.stage().getRoot().findActor("puzzleEditor");
        terminal = field(ui, "terminal", Window.class);
        phaseStart = System.nanoTime();
    }

    @Override
    public void render() {
        try {
            screen.render(Gdx.graphics.getDeltaTime());
            if ((System.nanoTime() - phaseStart) / 1e9 > 14) {
                throw new AssertionError("phase timed out: " + phase + " " + screen.puzzleView());
            }
            switch (phase) {
                case 0 -> {
                    // 1. Mission Map BEFORE success + Telemetry Panel inspection + Camera/Exploration to Bio-Dome
                    press(Keys.M);
                    screen.render(0);
                    Label p1Status = field(ui, "p1MissionStatusLabel", Label.class);
                    check(p1Status.getText().toString().contains("TERSEDIA"), "mission map shows P1 TERSEDIA before success");
                    capture("p1-mission-before");
                    press(Keys.ESCAPE);
                    screen.render(0);

                    walkSteps(Keys.D, 0.95f);
                    walkSteps(Keys.W, 2.15f);
                    walkSteps(Keys.D, 0.55f);
                    frameSteps(0.3f);
                    press(Keys.E);
                    screen.render(0);
                    check(world.mode() == HabitatWorld.Mode.INSPECTION, "inspection opened");
                    capture("p1-inspection");
                    press(Keys.ESCAPE);
                    screen.render(0);
                    check(world.mode() == HabitatWorld.Mode.EXPLORING, "Esc closes inspection");

                    // Walk to Bio-Dome Conservatory on East sector (wide northern corridor y~745 -> south to 460)
                    walkSteps(Keys.D, 2.15f);
                    walkSteps(Keys.S, 1.55f);
                    frameSteps(0.75f); // camera settles smoothly framing Bio-Dome
                    check(world.nearbyFacility() == HabitatWorld.Facility.ROVER, "astronaut near Bio-Dome Rover bay");
                    capture("p1-biodome-explore");

                    // Verify simple default terminal has Log auto-collapsed until output/run exists, and opens via Rover click!
                    clickWorldRover(0, 0);
                    frameSteps(0.75f);
                    check(world.mode() == HabitatWorld.Mode.TERMINAL, "clicking Rover opens unified coding terminal");
                    check(ui.stage().getKeyboardFocus() == editor, "terminal focuses editor");
                    Table consoleBox = field(ui, "consoleBox", Table.class);
                    check(!consoleBox.isVisible(), "simple default terminal collapses idle console output");
                    check(terminal.isResizable(), "terminal supports interactive resize");
                    check(terminal.getWidth() <= 460f && terminal.getHeight() <= 430f, "compact default terminal keeps world open");

                    assertions += PythonEditorDesktopChecks.run(editor, input);
                    editor.setText(REFERENCE);
                    click("runPuzzle");
                    editor.setText("# draf baru saat Run\n1/0");
                    next();
                }
                case 1 -> {
                    if (screen.puzzleView().status() != Status.SUCCESS) break;
                    // Let smooth rover glide, harvest beam, & spore burst finish cleanly before capture
                    frameSteps(0.35f);
                    check(screen.puzzleView().snapshot().harvestCount() == 2, "clicked Run executes captured Python");
                    check(screen.puzzleView().snapshot().actionCount() == 3, "Java metrics visible");
                    check(screen.puzzleView().completed(), "progress after replay");
                    check(editor.getText().contains("draf baru"), "editing during run preserved");
                    Actor retryBtn = ui.stage().getRoot().findActor("retryPuzzle");
                    Actor returnBtn = ui.stage().getRoot().findActor("returnHabitatPuzzle");
                    check(retryBtn != null && retryBtn.isVisible(), "Coba lagi visible after replay success");
                    check(returnBtn != null && returnBtn.isVisible(), "Kembali visible after replay success");
                    capture("p1-success");

                    // Coba lagi performs Reset while preserving code & session completion
                    click("retryPuzzle");
                    screen.render(0);
                    check(screen.puzzleView().status() == Status.READY, "Coba lagi resets status to READY");
                    check(screen.puzzleView().snapshot().remainingTargets().size() == 2, "Coba lagi restores crop markers");
                    check(screen.puzzleView().completed(), "Coba lagi preserves session completion");
                    check(editor.getText().contains("draf baru"), "Coba lagi preserves editor code");

                    editor.setText("while True: pass");
                    click("runPuzzle");
                    next();
                }
                case 2 -> {
                    if (screen.puzzleView().status() != Status.RUNNING) break;
                    click("stopPuzzle");
                    check(screen.puzzleView().status() == Status.STOPPED, "clicked Stop works");
                    check(editor.getText().equals("while True: pass"), "Stop preserves source");
                    next();
                }
                case 3 -> {
                    if (!screen.puzzleView().canRun()) break;
                    editor.setText("1/0");
                    click("runPuzzle");
                    next();
                }
                case 4 -> {
                    if (screen.puzzleView().status() != Status.ERROR) break;
                    check(screen.puzzleView().message().contains("ZeroDivisionError"), "real error displayed");
                    check(screen.puzzleView().completed(), "error after pass preserves session completion");
                    capture("p1-error");

                    // Mission Map after failed retry retains SELESAI
                    press(Keys.ESCAPE);
                    screen.render(0);
                    press(Keys.M);
                    screen.render(0);
                    Label p1Status = field(ui, "p1MissionStatusLabel", Label.class);
                    check(p1Status.getText().toString().contains("SELESAI"), "mission map keeps P1 SELESAI after failed retry");
                    capture("p1-mission-after");
                    press(Keys.ESCAPE);
                    screen.render(0);

                    // Settings Preview Visual toggle
                    press(Keys.O);
                    screen.render(0);
                    click("previewVisualBtn");
                    screen.render(0);
                    check(HabitatUi.isOutpostRestored(), "settings preview toggle works");
                    capture("p1-settings-preview");
                    click("previewVisualBtn");
                    screen.render(0);
                    check(!HabitatUi.isOutpostRestored(), "settings preview toggle resets");
                    press(Keys.ESCAPE);
                    screen.render(0);

                    // Reopen by pressing E near Bio-Dome, test resize drag + long code/output + [?] API drawer
                    press(Keys.E);
                    screen.render(0);
                    float w0 = terminal.getWidth();
                    Vector2 cStart = ui.stage().stageToScreenCoordinates(new Vector2(terminal.getRight() - 4f, terminal.getY() + 4f));
                    Vector2 cEnd = ui.stage().stageToScreenCoordinates(new Vector2(terminal.getRight() + 90f, Math.max(46f, terminal.getY() - 32f)));
                    input.touchDown(Math.round(cStart.x), Math.round(cStart.y), 0, Buttons.LEFT);
                    input.touchDragged(Math.round(cEnd.x), Math.round(cEnd.y), 0);
                    input.touchUp(Math.round(cEnd.x), Math.round(cEnd.y), 0, Buttons.LEFT);
                    screen.render(0);
                    check(terminal.getWidth() > w0 + 40f, "interactive corner resize expands terminal cleanly");

                    StringBuilder longCode = new StringBuilder();
                    for (int i = 1; i <= 26; i++) {
                        longCode.append("# baris komentar panjang ke-").append(i).append('\n');
                    }
                    longCode.append("for i in range(25):\n    print('baris output ke', i)\nharvest()\n");
                    editor.setText(longCode.toString());
                    editor.setCursorPosition(editor.getText().length());
                    click("runPuzzle");
                    next();
                }
                case 5 -> {
                    if (screen.puzzleView().status() != Status.INCOMPLETE) break;
                    Actor runBtn = ui.stage().getRoot().findActor("runPuzzle");
                    Actor logBtn = ui.stage().getRoot().findActor("logToggleBtn");
                    Vector2 runStage = runBtn.localToStageCoordinates(new Vector2(0f, 0f));
                    Vector2 logStage = logBtn.localToStageCoordinates(new Vector2(0f, 0f));
                    check(runBtn.isVisible() && logBtn.isVisible()
                        && runStage.y >= terminal.getY() && logStage.y >= terminal.getY(),
                        "action buttons not pushed off terminal by long output");
                    capture("p1-long-code-output");

                    // Toggle [?] API popover + drag terminal slightly
                    click("apiHelpToggle");
                    terminal.setSize(430f, 400f);
                    terminal.setPosition(34f, 88f);
                    screen.render(0);
                    capture("p1-help-dragged");
                    click("apiHelpToggle");
                    terminal.setPosition(16f, 82f);

                    editor.setText("import time\ntime.sleep(.35)\n" + REFERENCE);
                    click("runPuzzle");
                    press(Keys.ESCAPE);
                    screen.render(0);
                    next();
                }
                case 6 -> {
                    if (screen.puzzleView().status() != Status.SUCCESS) break;
                    check(ui.stage().getKeyboardFocus() == null, "Esc releases focus while Run continues");
                    press(Keys.E);
                    frameSteps(0.5f);
                    check(ui.stage().getKeyboardFocus() == editor, "reopen restores editor focus");
                    check(editor.getText().contains("time.sleep"), "Esc/reopen preserves draft");
                    next();
                    Gdx.graphics.setWindowedMode(1440, 900);
                }
                case 7 -> {
                    frameSteps(0.15f);
                    capture("p1-1440x900");
                    next();
                    Gdx.graphics.setWindowedMode(900, 600);
                }
                case 8 -> {
                    frameSteps(0.15f);
                    capture("p1-900x600");
                    next();
                    Gdx.graphics.setWindowedMode(640, 480);
                }
                case 9 -> {
                    frameSteps(0.15f);
                    capture("p1-640x480");
                    check(ui.stage().getKeyboardFocus() == editor, "resize keeps focus");
                    click("returnHabitatPuzzle");
                    screen.render(0);
                    check(world.mode() == HabitatWorld.Mode.EXPLORING, "Kembali returns cleanly to exploring");
                    press(Keys.E); screen.render(0); next();
                }
                case 10 -> {
                    click("selectPuzzle2"); screen.render(0);
                    check(screen.puzzleView().puzzle().number() == 2, "P2 selection unlocked after P1");
                    check(screen.puzzleView().snapshot().remainingTargets().size() == 4, "P2 displays four targets");
                    PythonEditorDesktopChecks.typeLoopReference(editor, input); assertions++;
                    click("runPuzzle"); next();
                }
                case 11 -> {
                    if (screen.puzzleView().status() != Status.SUCCESS) break;
                    check(screen.puzzleView().snapshot().harvestCount() == 4, "P2 four harvests visible");
                    check(screen.puzzleView().snapshot().actionCount() == 7, "P2 seven actions");
                    check(field(ui,"successSummaryLabel",Label.class).getText().toString().contains("4 dipanen"), "P2 success summary is dynamic");
                    capture("p2-success");
                    String p2Draft = editor.getText();
                    click("selectPuzzle1"); screen.render(0);
                    check(screen.puzzleView().puzzle().number() == 1 && screen.puzzleView().completed(), "P1 completion preserved");
                    check(editor.getText().contains("time.sleep"), "P1 draft restored");
                    click("selectPuzzle2"); screen.render(0);
                    check(editor.getText().equals(p2Draft) && screen.puzzleView().completed(), "P2 draft and completion restored");
                    click("selectPuzzle3"); screen.render(0);
                    check(screen.puzzleView().puzzle().number()==3,"P3 unlock and selector");
                    check(screen.puzzleView().snapshot().remainingTargets().size()==2,"P3 only two ripe targets");
                    editor.setText(com.marsreboot.runtime.PuzzleDefinition.TWO.reference());click("runPuzzle");next();
                }
                case 12 -> {
                    if(screen.puzzleView().status()!=Status.INCOMPLETE)break;
                    check(!screen.puzzleView().completed() && screen.puzzleView().snapshot().invalidHarvestCount()==2,"young attempt fails without progress");
                    check(field(ui,"consoleTitleLabel",Label.class).getText().toString().contains("muda"),"young violation has specific result title");
                    capture("p3-young-violation");
                    editor.setText(com.marsreboot.runtime.PuzzleDefinition.THREE.reference());click("runPuzzle");next();
                }
                case 13 -> {
                    if(screen.puzzleView().status()!=Status.SUCCESS)break;
                    check(screen.puzzleView().snapshot().actionCount()==5,"sensor solution costs five actions");
                    check(screen.puzzleView().snapshot().harvestCount()==2,"P3 ripe harvest count");
                    check(field(ui,"successSummaryLabel",Label.class).getText().toString().contains("2 dipanen"),"P3 two target summary");
                    capture("p3-success");
                    String p3Draft=editor.getText();click("selectPuzzle2");screen.render(0);
                    check(screen.puzzleView().completed(),"P2 completion survives P3");
                    click("selectPuzzle3");screen.render(0);
                    check(screen.puzzleView().completed() && editor.getText().equals(p3Draft),"P3 draft/completion survive switching");
                    click("selectPuzzle4");screen.render(0);
                    check(screen.puzzleView().puzzle().number()==4 && screen.puzzleView().scenarioCount()==2,"P4 unlocked, two cases");
                    check(screen.puzzleView().snapshot().waterRemaining()==2,"P4 starts with two water");
                    capture("p4-dry-start");
                    editor.setText("water()\nmove('east')\nmove('east')\nwater()\nmove('east')");
                    click("runPuzzle");next();
                }
                case 14 -> {
                    if(screen.puzzleView().status()!=Status.INCOMPLETE)break;
                    check(!screen.puzzleView().completed() && screen.puzzleView().scenariosPassed()==1,"hardcode does not pass P4");
                    check(screen.puzzleView().scenarioId().equals("p4-b"),"second case failure displayed");
                    capture("p4-hardcode-failure");click("resetPuzzle");screen.render(0);
                    check(screen.puzzleView().scenarioNumber()==1 && screen.puzzleView().snapshot().waterRemaining()==2,"P4 reset fixture/water");
                    editor.setText(com.marsreboot.runtime.PuzzleDefinition.FOUR.reference());click("runPuzzle");next();
                }
                case 15 -> {
                    if(screen.puzzleView().status()==Status.REPLAYING && screen.puzzleView().scenarioNumber()==2 && !checkedP4Intermediate) {
                        check(!screen.puzzleView().completed() && screen.puzzleView().scenariosPassed()==1,"first scenario replay never grants completion");
                        checkedP4Intermediate=true;
                    }
                    if(screen.puzzleView().status()!=Status.SUCCESS)break;
                    check(checkedP4Intermediate,"second scenario replay observed");
                    check(screen.puzzleView().completed() && screen.puzzleView().scenariosPassed()==2,"P4 all scenarios pass");
                    check(screen.puzzleView().totalActions()==10 && screen.puzzleView().totalWaterUsed()==4,"P4 totals shown");
                    check(screen.puzzleView().snapshot().dryCells().isEmpty(),"all required cells moist");
                    check(field(ui,"successSummaryLabel",Label.class).getText().toString().contains("10 aksi"),"P4 success summary");
                    capture("p4-success");click("selectPuzzle3");screen.render(0);
                    check(screen.puzzleView().completed() && editor.getText().contains("can_harvest"),"P3 draft/progress preserved after P4");
                    click("selectPuzzle4");screen.render(0);
                    check(screen.puzzleView().completed() && editor.getText().contains("needs_water"),"P4 draft/progress preserved");
                    System.out.println("PASS: " + assertions + " Python puzzle desktop assertions; P1-P4, editor, real CPython and OpenGL.");
                    Gdx.app.exit();
                }
            }
        } catch (Throwable error) {
            failure = error;
            error.printStackTrace();
            Gdx.app.exit();
        }
    }

    private void clickWorldRover(int cellX, int cellY) {
        HabitatWorld.Box box = HabitatWorld.roverBayBox(cellX, cellY, 2);
        Vector3 proj = worldViewport.project(new Vector3(box.centerX(), box.centerY(), 0f));
        int sx = Math.round(proj.x);
        int sy = Gdx.graphics.getHeight() - Math.round(proj.y);
        input.touchDown(sx, sy, 0, Buttons.LEFT);
        input.touchUp(sx, sy, 0, Buttons.LEFT);
    }

    private void click(String name) {
        Actor button = ui.stage().getRoot().findActor(name);
        check(button != null, "missing actor: " + name);
        Vector2 point = button.localToStageCoordinates(new Vector2(button.getWidth() / 2, button.getHeight() / 2));
        ui.stage().stageToScreenCoordinates(point);
        input.touchDown(Math.round(point.x), Math.round(point.y), 0, Buttons.LEFT);
        input.touchUp(Math.round(point.x), Math.round(point.y), 0, Buttons.LEFT);
    }

    private void next() { phase++; phaseStart = System.nanoTime(); }
    private void press(int key) { input.keyDown(key); input.keyUp(key); }

    private void walkSteps(int key, float seconds) {
        input.keyDown(key);
        frameSteps(seconds);
        input.keyUp(key);
    }

    private void frameSteps(float seconds) {
        float step = 1f / 60f;
        int count = Math.max(1, Math.round(seconds / step));
        for (int i = 0; i < count; i++) {
            screen.render(step);
        }
    }

    private void capture(String name) {
        Pixmap image = Pixmap.createFromFrameBuffer(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
        try {
            PixmapIO.writePNG(Gdx.files.absolute(System.getProperty("mars.reportDir", "../lwjgl3/build/reports/puzzle") + "/" + name + ".png"), image, -1, true);
        } finally {
            image.dispose();
        }
    }

    private void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
        assertions++;
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
