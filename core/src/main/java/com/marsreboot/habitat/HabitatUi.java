package com.marsreboot.habitat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.marsreboot.runtime.RunCoordinator;
import com.marsreboot.runtime.RunCoordinator.Status;
import java.util.ArrayList;
import java.util.List;

/**
 * Rounded, non-boxy Mars Outpost HUD & Unified Resizable Rover Terminal:
 * - ExtendViewport(960, 600) with floating pill HUDs (no rigid edge-to-edge black bars)
 * - 3x supersampled anti-aliased rounded glass windows, pills, and buttons
 * - Minimalist movable & resizable Rover Terminal opened via direct Rover click or [E] nearby
 *   with expandable [?] API drawer and auto-collapsing [Log] output panel
 */
public final class HabitatUi implements Disposable {
    private static final Color COL_TEXT_PRIMARY = Color.valueOf("F8FAFC");
    private static final Color COL_TEXT_SECONDARY = Color.valueOf("CBD5E1");
    private static final Color COL_TEXT_MUTED = Color.valueOf("94A3B8");
    private static final Color COL_CYAN = Color.valueOf("38BDF8");
    private static final Color COL_AMBER = Color.valueOf("F59E0B");
    private static final Color COL_EMERALD = Color.valueOf("34D399");
    private static final Color COL_CORAL = Color.valueOf("F87171");

    public static final float TERMINAL_MIN_W = 350f;
    public static final float TERMINAL_MIN_H = 280f;

    private static boolean outpostRestored = false;
    private static boolean reducedMotion = false;
    private static boolean highContrastFocus = true;

    public static boolean isOutpostRestored() { return outpostRestored; }
    public static boolean isReducedMotion() { return reducedMotion; }
    public static void setOutpostRestored(boolean restored) { outpostRestored = restored; }

    public enum AuxOverlay { NONE, FIELD_LOG, MISSION_MAP, SETTINGS }

    private final HabitatWorld world;
    private final RunCoordinator coordinator;
    private final Stage stage = new Stage(new ExtendViewport(960f, 600f));
    private final Skin skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
    private final List<Texture> ownedTextures = new ArrayList<>();
    private final List<BitmapFont> ownedFonts = new ArrayList<>();

    private final BitmapFont uiFont;
    private final BitmapFont uiBoldFont;
    private final BitmapFont monoFont;
    private final BitmapFont hudFont;
    private final BitmapFont hudBoldFont;

    private final Label prompt;
    private final Label modeBadge;
    private final Label puzzleStats;
    private final Label goalSummaryLabel;
    private final Label goalStatusLabel;
    private final Label editorHeaderRangeLabel;
    private final Label lineNumbersLabel;
    private final Label consoleTitleLabel;
    private final Label consoleDetailLabel;
    private final ScrollPane consoleScroll;
    private final Table consoleBox;
    private final Cell<Table> consoleBoxCell;
    private final Table editorPanel;
    private final Table bottomPromptBox;
    private final Table successSummaryRow;
    private final Cell<Table> successSummaryRowCell;
    private final Label successSummaryLabel;

    private final Drawable promptIdleBg;
    private final Drawable promptNearBg;
    private final Drawable promptModalBg;
    private final Drawable consoleNormalBg;
    private final Drawable consoleSuccessBg;
    private final Drawable consoleErrorBg;
    private final Drawable consoleWarnBg;
    private final Drawable editorNormalBg;
    private final Drawable editorFocusHighBg;
    private final Drawable editorFocusStdBg;
    private final Drawable cardNeutralBg;
    private final Drawable cardCyanBg;
    private final Drawable cardEmeraldBg;
    private final Drawable cardAmberBg;

    private final TextFieldStyle editorStyle;
    private final TextButton reducedMotionBtn;
    private final TextButton highContrastBtn;
    private final TextButton previewVisualBtn;
    private final TextButton logToggleBtn;
    private final TextButton helpToggleBtn;

    private final Table p1MissionCard;
    private final Label p1MissionStatusLabel;
    private final Label p1MissionDetailLabel;
    private final LabelStyle cyanStyle;
    private final LabelStyle emeraldStyle;
    private final LabelStyle amberStyle;

    // Reflection fields inspected by desktop smoke tests
    private final Window inspection;
    private final Window terminal;
    private final TextArea editor;

    private final Window helpDrawer;
    private final Window fieldLogWindow;
    private final Window missionMapWindow;
    private final Window settingsWindow;

    private final TextButton runButton;
    private final TextButton stopButton;
    private final TextButton resetButton;
    private final TextButton retryButton;
    private final TextButton returnHabitatButton;

    private HabitatWorld.Mode shown;
    private AuxOverlay activeOverlay = AuxOverlay.NONE;
    private RunCoordinator.View shownPuzzle;
    private boolean helpDrawerOpen = false;
    private boolean logManualExpanded = false;

    public HabitatUi(HabitatWorld world, RunCoordinator coordinator) {
        this.world = world;
        this.coordinator = coordinator;

        // Clear ~18 UI-unit body typography + 15 UI-unit top/bottom pills
        uiFont = loadBitmapFont("ui/ui_sans.fnt", "ui/ui_sans.png", 0.50f, false);
        uiBoldFont = loadBitmapFont("ui/ui_sans_bold.fnt", "ui/ui_sans_bold.png", 0.50f, false);
        monoFont = loadBitmapFont("ui/ui_mono.fnt", "ui/ui_mono.png", 0.53f, false);
        hudFont = loadBitmapFont("ui/ui_sans.fnt", "ui/ui_sans.png", 0.43f, false);
        hudBoldFont = loadBitmapFont("ui/ui_sans_bold.fnt", "ui/ui_sans_bold.png", 0.43f, false);

        LabelStyle bodyStyle = new LabelStyle(uiFont, COL_TEXT_PRIMARY);
        LabelStyle mutedStyle = new LabelStyle(uiFont, COL_TEXT_SECONDARY);
        LabelStyle boldStyle = new LabelStyle(uiBoldFont, COL_TEXT_PRIMARY);
        LabelStyle hudStyle = new LabelStyle(hudFont, COL_TEXT_PRIMARY);
        LabelStyle hudMutedStyle = new LabelStyle(hudFont, COL_TEXT_MUTED);
        LabelStyle hudBoldStyle = new LabelStyle(hudBoldFont, COL_TEXT_PRIMARY);
        LabelStyle monoStyle = new LabelStyle(monoFont, COL_TEXT_MUTED);

        cyanStyle = new LabelStyle(uiBoldFont, COL_CYAN);
        emeraldStyle = new LabelStyle(uiBoldFont, COL_EMERALD);
        amberStyle = new LabelStyle(uiBoldFont, COL_AMBER);
        LabelStyle consoleTitleStyle = new LabelStyle(uiBoldFont, COL_CYAN);

        Drawable topPillBg = makeRoundedBox(28, 12, Color.valueOf("091220E6"), Color.valueOf("334155"), 1.5f);
        Drawable statusPillBg = makeRoundedBox(26, 12, Color.valueOf("0C192CE6"), COL_CYAN, 1.5f);
        promptIdleBg = makeRoundedBox(28, 14, Color.valueOf("091220E0"), Color.valueOf("334155"), 1.5f);
        promptNearBg = makeRoundedBox(28, 14, Color.valueOf("071D2EEB"), COL_CYAN, 1.8f);
        promptModalBg = makeRoundedBox(28, 14, Color.valueOf("21150CEB"), COL_AMBER, 1.8f);

        cardNeutralBg = makeRoundedBox(26, 11, Color.valueOf("0E1828EB"), Color.valueOf("334155"), 1.5f);
        cardCyanBg = makeRoundedBox(26, 11, Color.valueOf("081C2EEB"), COL_CYAN, 1.6f);
        cardEmeraldBg = makeRoundedBox(26, 11, Color.valueOf("06221BE8"), COL_EMERALD, 1.6f);
        cardAmberBg = makeRoundedBox(26, 11, Color.valueOf("22170BE8"), COL_AMBER, 1.6f);

        editorNormalBg = makeRoundedBox(26, 10, Color.valueOf("070F1CF2"), Color.valueOf("283A52"), 1.5f);
        editorFocusStdBg = makeRoundedBox(26, 10, Color.valueOf("07101DF4"), COL_CYAN, 2.0f);
        editorFocusHighBg = makeRoundedBox(26, 10, Color.valueOf("091527F6"), Color.valueOf("7DD3FC"), 2.5f);

        consoleNormalBg = makeRoundedBox(26, 10, Color.valueOf("08121FED"), Color.valueOf("334155"), 1.5f);
        consoleSuccessBg = makeRoundedBox(26, 10, Color.valueOf("06211AEE"), COL_EMERALD, 1.8f);
        consoleErrorBg = makeRoundedBox(26, 10, Color.valueOf("220B10EE"), COL_CORAL, 1.8f);
        consoleWarnBg = makeRoundedBox(26, 10, Color.valueOf("211509EE"), COL_AMBER, 1.8f);

        WindowStyle glassWindowStyle = new WindowStyle(
            uiBoldFont, COL_TEXT_PRIMARY,
            makeRoundedWindow(Color.valueOf("09111FEB"), Color.valueOf("38BDF8"), 14, 34)
        );
        WindowStyle warmWindowStyle = new WindowStyle(
            uiBoldFont, COL_TEXT_PRIMARY,
            makeRoundedWindow(Color.valueOf("0B111CEB"), COL_AMBER, 14, 34)
        );

        TextButtonStyle hudPillBtnStyle = makeButtonStyle(
            hudBoldFont, COL_TEXT_PRIMARY,
            Color.valueOf("122036EC"), Color.valueOf("1B3352F4"), Color.valueOf("264972FF"),
            Color.valueOf("3B526E"), COL_CYAN, 11
        );
        TextButtonStyle softBtnStyle = makeButtonStyle(
            uiBoldFont, COL_TEXT_PRIMARY,
            Color.valueOf("16243AEF"), Color.valueOf("203759F5"), Color.valueOf("294B7AFF"),
            Color.valueOf("475569"), COL_CYAN, 10
        );
        TextButtonStyle primaryEmeraldStyle = makeButtonStyle(
            uiBoldFont, Color.valueOf("042219"),
            COL_EMERALD, Color.valueOf("6EE7B7"), Color.valueOf("10B981"),
            Color.valueOf("A7F3D0"), Color.WHITE, 10
        );
        TextButtonStyle primaryCyanStyle = makeButtonStyle(
            uiBoldFont, Color.valueOf("051B2C"),
            COL_CYAN, Color.valueOf("7DD3FC"), Color.valueOf("0284C7"),
            Color.valueOf("BAE6FD"), Color.WHITE, 10
        );
        TextButtonStyle stopAmberStyle = makeButtonStyle(
            uiBoldFont, Color.valueOf("FFFBEB"),
            Color.valueOf("B45309"), Color.valueOf("D97706"), Color.valueOf("92400E"),
            COL_AMBER, Color.WHITE, 10
        );

        Drawable cursorDrawable = makeSolidColor(Color.valueOf("38BDF8"), 2, 18);
        Drawable selectionDrawable = makeSolidColor(new Color(0.22f, 0.74f, 0.97f, 0.35f), 2, 18);
        editorStyle = new TextFieldStyle(
            monoFont, COL_TEXT_PRIMARY, cursorDrawable, selectionDrawable, editorFocusHighBg
        );
        editorStyle.fontColor = COL_TEXT_PRIMARY;
        editorStyle.focusedFontColor = COL_TEXT_PRIMARY;

        ScrollPaneStyle scrollStyle = new ScrollPaneStyle();
        scrollStyle.vScroll = makeSolidColor(new Color(0.18f, 0.25f, 0.36f, 0.45f), 5, 16);
        scrollStyle.vScrollKnob = makeRoundedBox(8, 4, COL_CYAN, COL_CYAN, 0.5f);

        // --- Floating HUD Pills (Top & Bottom; world remains breathable!) ---
        Table root = new Table();
        root.setFillParent(true);
        root.top().left();
        stage.addActor(root);

        Table topLeftPill = new Table();
        topLeftPill.setBackground(topPillBg);
        topLeftPill.pad(6f, 14f, 6f, 14f);
        Label brandLabel = new Label("MARS_REBOOT.PY", new LabelStyle(hudBoldFont, COL_CYAN));
        puzzleStats = new Label("M1-P1 | PANEN 0/2 | AKSI 0/6", hudBoldStyle);
        topLeftPill.add(brandLabel).padRight(10f);
        topLeftPill.add(puzzleStats);

        Table topRightBar = new Table();
        topRightBar.defaults().space(6f);
        TextButton missionBtn = new TextButton("[M] Misi", hudPillBtnStyle);
        missionBtn.addListener(onAction(() -> toggleAuxOverlay(AuxOverlay.MISSION_MAP)));
        TextButton logBtn = new TextButton("[J] Log", hudPillBtnStyle);
        logBtn.addListener(onAction(() -> toggleAuxOverlay(AuxOverlay.FIELD_LOG)));
        TextButton settingsBtn = new TextButton("[O] Setelan", hudPillBtnStyle);
        settingsBtn.addListener(onAction(() -> toggleAuxOverlay(AuxOverlay.SETTINGS)));

        Table badgeBox = new Table();
        badgeBox.setBackground(statusPillBg);
        badgeBox.pad(5f, 12f, 5f, 12f);
        modeBadge = new Label("OUTPOST", new LabelStyle(hudBoldFont, COL_EMERALD));
        badgeBox.add(modeBadge);

        topRightBar.add(logBtn).height(28f);
        topRightBar.add(missionBtn).height(28f);
        topRightBar.add(settingsBtn).height(28f);
        topRightBar.add(badgeBox).height(28f);

        Table topRow = new Table();
        topRow.pad(10f, 14f, 0f, 14f);
        topRow.add(topLeftPill).left().expandX();
        topRow.add(topRightBar).right();
        root.add(topRow).growX().row();
        root.add().expand().row();

        bottomPromptBox = new Table();
        bottomPromptBox.setBackground(promptIdleBg);
        bottomPromptBox.pad(7f, 18f, 7f, 18f);
        prompt = new Label("Jelajahi Outpost [WASD] • Klik Rover atau [E] saat dekat", hudStyle);
        bottomPromptBox.add(prompt);

        Table bottomRow = new Table();
        bottomRow.pad(0f, 14f, 10f, 14f);
        bottomRow.add(bottomPromptBox).center();
        root.add(bottomRow).growX();

        // --- 1. Panel Telemetri Inspection Modal ---
        inspection = createBaseWindow(" PANEL TELEMETRI BIO-DOME | P1", warmWindowStyle, 560f, 340f);
        inspection.setPosition(200f, 130f);

        Table inspectIntro = new Table();
        inspectIntro.setBackground(cardCyanBg);
        inspectIntro.pad(10f, 14f, 10f, 14f);
        inspectIntro.add(wrapLabel("Misi 1: Panen 2 tanaman matang di Bio-Dome Timur memakai Rover.", boldStyle))
            .growX().minWidth(0).left();
        inspection.add(inspectIntro).growX().minWidth(0).pad(4f, 10f, 8f, 10f).row();

        Table inspectBody = new Table();
        inspectBody.setBackground(cardNeutralBg);
        inspectBody.pad(12f, 14f, 12f, 14f);
        inspectBody.add(wrapLabel("• Lokasi Target: petak (0,0) dan (1,0) di dalam kubah Bio-Dome.", bodyStyle))
            .growX().minWidth(0).left().padBottom(6f).row();
        inspectBody.add(wrapLabel("• Cara Kerja: berjalan ke Bio-Dome Timur lalu klik langsung pada Rover untuk menulis kode Python (maksimal 6 aksi).", mutedStyle))
            .growX().minWidth(0).left();
        inspection.add(inspectBody).growX().minWidth(0).pad(0f, 10f, 10f, 10f).row();

        Table inspectBtns = new Table();
        TextButton inspectCloseBtn = new TextButton("Kembali ke Outpost [Esc]", primaryCyanStyle);
        inspectCloseBtn.addListener(onAction(world::closePanel));
        TextButton inspectGoRoverBtn = new TextButton("Buka Editor Rover", softBtnStyle);
        inspectGoRoverBtn.addListener(onAction(() -> {
            world.closePanel();
            world.openRoverTerminal();
        }));
        inspectBtns.add(inspectGoRoverBtn).height(34f).padRight(10f);
        inspectBtns.add(inspectCloseBtn).height(34f);
        inspection.add(inspectBtns).right().pad(2f, 10f, 10f, 10f);

        // --- 2. Unified Simple & Resizable Rover Terminal Window ---
        terminal = new Window(" ROVER BIO-DOME | PUZZLE 1", glassWindowStyle);
        terminal.setName("roverTerminal");
        terminal.setVisible(false);
        terminal.setMovable(true);
        terminal.setResizable(true);
        terminal.setResizeBorder(14);
        terminal.setSize(430f, 400f);
        terminal.setPosition(16f, 82f);
        terminal.setKeepWithinStage(false);
        terminal.pad(36f, 10f, 8f, 10f);

        helpToggleBtn = new TextButton("? API", hudPillBtnStyle);
        helpToggleBtn.setName("apiHelpToggle");
        helpToggleBtn.addListener(onAction(() -> setHelpDrawerOpen(!helpDrawerOpen)));
        TextButton termCloseBtn = new TextButton("X", hudPillBtnStyle);
        termCloseBtn.setName("roverTerminalClose");
        termCloseBtn.addListener(onAction(world::closePanel));
        terminal.getTitleTable().add(helpToggleBtn).height(24f).padRight(6f);
        terminal.getTitleTable().add(termCloseBtn).width(26f).height(24f).padRight(4f);

        // 2a. Compact 1-Line Objective Strip
        Table goalStrip = new Table();
        goalStrip.setBackground(cardNeutralBg);
        goalStrip.pad(7f, 12f, 7f, 12f);
        goalSummaryLabel = new Label("Target: Panen 2/2 • Aksi 0/6", bodyStyle);
        goalStatusLabel = new Label("SIAP", emeraldStyle);
        goalStatusLabel.setAlignment(Align.right);
        goalStrip.add(goalSummaryLabel).growX().minWidth(0).left();
        goalStrip.add(goalStatusLabel).minWidth(72f).right().padLeft(10f).padRight(6f);
        terminal.add(goalStrip).growX().minWidth(0).padBottom(6f).row();

        // 2b. Flexible Scrollable Code Editor + Live Gutter
        editorPanel = new Table();
        editorPanel.setBackground(editorFocusHighBg);
        editorPanel.pad(6f, 10f, 6f, 10f);

        Table editorHeader = new Table();
        editorHeaderRangeLabel = new Label("Baris 1-6 / 6", hudMutedStyle);
        Label editorLangTag = new Label("Python • Edit kode lalu Run", hudMutedStyle);
        editorHeader.add(editorLangTag).growX().minWidth(0).left();
        editorHeader.add(editorHeaderRangeLabel).right();
        editorPanel.add(editorHeader).growX().minWidth(0).padBottom(4f).row();

        lineNumbersLabel = new Label("1\n2\n3\n4", monoStyle);
        lineNumbersLabel.setAlignment(Align.topRight);

        editor = new TextArea(
            "harvest()\nmove(\"east\")\nharvest()",
            editorStyle
        );
        editor.setName("puzzleEditor");
        editor.setPrefRows(5);

        Table editorBody = new Table();
        editorBody.add(lineNumbersLabel).width(28f).top().padRight(8f).padTop(4f);
        editorBody.add(editor).grow().minWidth(0).minHeight(90f);
        editorPanel.add(editorBody).grow().minWidth(0);

        editor.addListener(new InputListener() {
            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
                int maxLine = Math.max(0, editor.getLines() - 1);
                int targetLine = Math.max(0, Math.min(maxLine, editor.getCursorLine() + (amountY > 0 ? 2 : -2)));
                editor.moveCursorLine(targetLine);
                updateVisibleEditorGutter();
                return true;
            }
        });

        terminal.add(editorPanel).grow().minWidth(0).minHeight(110f).padBottom(6f).row();

        // 2c. Compact Control Bar: Run, Stop, Reset, [Log] toggle
        Table actionBar = new Table();
        actionBar.defaults().height(32f);
        runButton = new TextButton("> Run", primaryEmeraldStyle);
        runButton.setName("runPuzzle");
        runButton.addListener(onAction(() -> {
            coordinator.run(editor.getText());
            stage.setKeyboardFocus(editor);
        }));

        stopButton = new TextButton("Stop", stopAmberStyle);
        stopButton.setName("stopPuzzle");
        stopButton.addListener(onAction(() -> {
            coordinator.stop();
            stage.setKeyboardFocus(editor);
        }));

        resetButton = new TextButton("Reset", softBtnStyle);
        resetButton.setName("resetPuzzle");
        resetButton.addListener(onAction(() -> {
            coordinator.reset();
            stage.setKeyboardFocus(editor);
        }));

        logToggleBtn = new TextButton("Log", hudPillBtnStyle);
        logToggleBtn.setName("logToggleBtn");
        logToggleBtn.addListener(onAction(() -> {
            logManualExpanded = !logManualExpanded;
            syncPuzzle();
        }));

        actionBar.add(runButton).minWidth(86f).padRight(6f);
        actionBar.add(stopButton).minWidth(68f).padRight(6f);
        actionBar.add(resetButton).minWidth(72f);
        actionBar.add().expandX();
        actionBar.add(logToggleBtn).minWidth(68f);
        terminal.add(actionBar).growX().minWidth(0).padBottom(4f).row();

        // 2d. Contextual Collapsible Output & Post-Success Row
        consoleBox = new Table();
        consoleBox.setClip(true);
        consoleBox.setBackground(consoleNormalBg);
        consoleBox.pad(7f, 10f, 7f, 10f);

        consoleTitleLabel = new Label("Siap dijalankan", consoleTitleStyle);
        consoleBox.add(consoleTitleLabel).growX().minWidth(0).left().padBottom(3f).row();

        consoleDetailLabel = wrapLabel("", mutedStyle);
        consoleScroll = new ScrollPane(consoleDetailLabel, scrollStyle);
        consoleScroll.setScrollingDisabled(true, false);
        consoleScroll.setFadeScrollBars(false);
        consoleScroll.setOverscroll(false, false);
        consoleBox.add(consoleScroll).grow().minWidth(0).minHeight(0f).row();

        successSummaryRow = new Table();
        successSummaryLabel = wrapLabel("2 tanaman dipanen!", emeraldStyle);
        retryButton = new TextButton("Coba lagi", softBtnStyle);
        retryButton.setName("retryPuzzle");
        retryButton.addListener(onAction(() -> {
            coordinator.reset();
            stage.setKeyboardFocus(editor);
        }));
        returnHabitatButton = new TextButton("Kembali", primaryCyanStyle);
        returnHabitatButton.setName("returnHabitatPuzzle");
        returnHabitatButton.addListener(onAction(world::closePanel));
        successSummaryRow.add(successSummaryLabel).growX().minWidth(0).left();
        successSummaryRow.add(retryButton).height(28f).padRight(6f);
        successSummaryRow.add(returnHabitatButton).height(28f);
        successSummaryRowCell = consoleBox.add(successSummaryRow).growX().minWidth(0);

        consoleBoxCell = terminal.add(consoleBox).growX().minWidth(0);
        terminal.row();

        // 2e. Subtle Bottom Resize Handle Indicator
        Table gripBar = new Table();
        Label gripHint = new Label("Tarik tepi/sudut untuk resize", new LabelStyle(hudFont, COL_TEXT_MUTED));
        Image gripIcon = new Image(makeResizeGripDrawable());
        gripBar.add(gripHint).left().expandX();
        gripBar.add(gripIcon).right().size(14f, 14f);
        terminal.add(gripBar).growX().minWidth(0).padTop(3f);

        // Focus glow sync
        stage.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                boolean focused = event.getTarget() == editor;
                editorPanel.setBackground(focused
                    ? (highContrastFocus ? editorFocusHighBg : editorFocusStdBg)
                    : editorNormalBg);
                return false;
            }
        });

        // --- 3. Optional Floating [?] API Popover beside Terminal ---
        helpDrawer = createBaseWindow(" REFERENSI PYTHON API", glassWindowStyle, 336f, 294f);
        Table apiBody = new Table();
        apiBody.setBackground(cardNeutralBg);
        apiBody.pad(10f, 12f, 10f, 12f);
        apiBody.add(wrapLabel("harvest() — Panen petak aktif (1 aksi)", bodyStyle)).growX().minWidth(0).left().padBottom(6f).row();
        apiBody.add(wrapLabel("move(\"east\"|\"west\"|\"north\"|\"south\") — Gerak rover (1 aksi)", bodyStyle)).growX().minWidth(0).left().padBottom(6f).row();
        apiBody.add(wrapLabel("can_harvest() — Cek tanaman matang (gratis)", mutedStyle)).growX().minWidth(0).left().padBottom(6f).row();
        apiBody.add(wrapLabel("get_pos() — Koordinat rover (x, y) (gratis)", mutedStyle)).growX().minWidth(0).left();
        helpDrawer.add(apiBody).growX().minWidth(0).pad(4f, 10f, 8f, 10f).row();
        TextButton closeApiBtn = new TextButton("Tutup [?]", softBtnStyle);
        closeApiBtn.addListener(onAction(() -> {
            helpDrawerOpen = false;
            helpDrawer.setVisible(false);
        }));
        helpDrawer.add(closeApiBtn).height(30f).right().pad(0f, 10f, 8f, 10f);

        // --- 4. Field Log [J] ---
        fieldLogWindow = createBaseWindow(" CATATAN LAPANGAN", glassWindowStyle, 520f, 300f);
        Table logCard = new Table();
        logCard.setBackground(cardNeutralBg);
        logCard.pad(12f, 14f, 12f, 14f);
        logCard.add(wrapLabel("• Lokasi Target: kompleks Bio-Dome sektor Timur.", bodyStyle)).growX().minWidth(0).left().padBottom(6f).row();
        logCard.add(wrapLabel("• Encounter Rover: klik langsung Rover di Bio-Dome untuk membuka editor kode.", mutedStyle)).growX().minWidth(0).left().padBottom(6f).row();
        logCard.add(wrapLabel("• Kode dijalankan Python bundel terisolasi; batas Puzzle 1 adalah 6 aksi.", mutedStyle)).growX().minWidth(0).left();
        fieldLogWindow.add(logCard).growX().minWidth(0).pad(4f, 10f, 10f, 10f).row();
        TextButton closeLogBtn = new TextButton("Tutup [Esc]", primaryCyanStyle);
        closeLogBtn.addListener(onAction(() -> closeAuxOverlays()));
        fieldLogWindow.add(closeLogBtn).height(32f).right().pad(0f, 10f, 8f, 10f);

        // --- 5. Mission Map [M] ---
        missionMapWindow = createBaseWindow(" PETA MISI OUTPOST", glassWindowStyle, 600f, 380f);
        p1MissionCard = new Table();
        p1MissionCard.setBackground(cardCyanBg);
        p1MissionCard.pad(10f, 12f, 10f, 12f);
        p1MissionStatusLabel = new Label("P1 • TERSEDIA", cyanStyle);
        p1MissionDetailLabel = wrapLabel("Panen dua tanaman matang Bio-Dome (0,0) & (1,0).", mutedStyle);
        p1MissionCard.add(p1MissionStatusLabel).growX().minWidth(0).left().row();
        p1MissionCard.add(p1MissionDetailLabel).growX().minWidth(0).left().padTop(4f);
        missionMapWindow.add(p1MissionCard).growX().minWidth(0).pad(4f, 10f, 8f, 10f).row();

        Table pGrid = new Table();
        pGrid.defaults().pad(4f).growX().minWidth(0);
        for (int p = 2; p <= 9; p++) {
            Table c = new Table();
            c.setBackground(cardNeutralBg);
            c.pad(7f, 10f, 7f, 10f);
            c.add(new Label("P" + p + " • BELUM TERSEDIA", hudMutedStyle)).left().growX().minWidth(0);
            pGrid.add(c);
            if ((p - 1) % 2 == 0) pGrid.row();
        }
        missionMapWindow.add(pGrid).growX().minWidth(0).pad(0f, 6f, 8f, 6f).row();
        TextButton closeMissionBtn = new TextButton("Tutup [Esc]", primaryCyanStyle);
        closeMissionBtn.addListener(onAction(() -> closeAuxOverlays()));
        missionMapWindow.add(closeMissionBtn).height(32f).right().pad(0f, 10f, 8f, 10f);

        // --- 6. Settings [O] ---
        settingsWindow = createBaseWindow(" SETELAN & AKSESIBILITAS", glassWindowStyle, 480f, 290f);
        Table optCard = new Table();
        optCard.setBackground(cardNeutralBg);
        optCard.pad(10f, 12f, 10f, 12f);
        optCard.defaults().height(34f).padBottom(8f);

        reducedMotionBtn = new TextButton("", softBtnStyle);
        reducedMotionBtn.addListener(onAction(() -> { reducedMotion = !reducedMotion; refreshSettingsLabels(); }));
        highContrastBtn = new TextButton("", softBtnStyle);
        highContrastBtn.addListener(onAction(() -> {
            highContrastFocus = !highContrastFocus;
            refreshSettingsLabels();
        }));
        previewVisualBtn = new TextButton("", softBtnStyle);
        previewVisualBtn.setName("previewVisualBtn");
        previewVisualBtn.addListener(onAction(() -> {
            outpostRestored = !outpostRestored;
            refreshSettingsLabels();
        }));

        optCard.add(reducedMotionBtn).growX().minWidth(0).row();
        optCard.add(highContrastBtn).growX().minWidth(0).row();
        optCard.add(previewVisualBtn).growX().minWidth(0).padBottom(0f);
        settingsWindow.add(optCard).growX().minWidth(0).pad(4f, 10f, 10f, 10f).row();
        TextButton closeSettingsBtn = new TextButton("Tutup [Esc]", primaryCyanStyle);
        closeSettingsBtn.addListener(onAction(() -> closeAuxOverlays()));
        settingsWindow.add(closeSettingsBtn).height(32f).right().pad(0f, 10f, 8f, 10f);

        stage.addActor(inspection);
        stage.addActor(terminal);
        stage.addActor(helpDrawer);
        stage.addActor(fieldLogWindow);
        stage.addActor(missionMapWindow);
        stage.addActor(settingsWindow);

        // Global shortcuts [M] [J] [O] when terminal not focused
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (world.mode() == HabitatWorld.Mode.EXPLORING) {
                    if (keycode == Keys.M) { toggleAuxOverlay(AuxOverlay.MISSION_MAP); return true; }
                    if (keycode == Keys.J) { toggleAuxOverlay(AuxOverlay.FIELD_LOG); return true; }
                    if (keycode == Keys.O) { toggleAuxOverlay(AuxOverlay.SETTINGS); return true; }
                }
                return false;
            }
        });

        refreshSettingsLabels();
        syncPuzzle();
    }

    public Stage stage() { return stage; }
    public boolean hasOpenOverlay() { return activeOverlay != AuxOverlay.NONE; }

    public void setHelpDrawerOpen(boolean open) {
        helpDrawerOpen = open;
        helpDrawer.setVisible(world.mode() == HabitatWorld.Mode.TERMINAL && helpDrawerOpen);
        positionHelpDrawerNextToTerminal();
        if (helpDrawer.isVisible()) helpDrawer.toFront();
    }

    public void toggleAuxOverlay(AuxOverlay overlay) {
        if (world.mode() != HabitatWorld.Mode.EXPLORING) return;
        activeOverlay = (activeOverlay == overlay) ? AuxOverlay.NONE : overlay;
        updateAuxOverlayVisibility();
    }

    public void closeAuxOverlays() {
        activeOverlay = AuxOverlay.NONE;
        updateAuxOverlayVisibility();
    }

    private void updateAuxOverlayVisibility() {
        fieldLogWindow.setVisible(activeOverlay == AuxOverlay.FIELD_LOG);
        missionMapWindow.setVisible(activeOverlay == AuxOverlay.MISSION_MAP);
        settingsWindow.setVisible(activeOverlay == AuxOverlay.SETTINGS);
        if (fieldLogWindow.isVisible()) { centerWindow(fieldLogWindow); fieldLogWindow.toFront(); }
        if (missionMapWindow.isVisible()) { centerWindow(missionMapWindow); missionMapWindow.toFront(); }
        if (settingsWindow.isVisible()) { centerWindow(settingsWindow); settingsWindow.toFront(); }
    }

    public void sync() {
        syncPuzzle();
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE) && activeOverlay != AuxOverlay.NONE) {
            closeAuxOverlays();
        }
        HabitatWorld.Mode mode = world.mode();
        if (shown != mode) {
            if (mode != HabitatWorld.Mode.EXPLORING) {
                activeOverlay = AuxOverlay.NONE;
            }
            stage.unfocusAll();
            inspection.setVisible(mode == HabitatWorld.Mode.INSPECTION);
            terminal.setVisible(mode == HabitatWorld.Mode.TERMINAL);
            helpDrawer.setVisible(mode == HabitatWorld.Mode.TERMINAL && helpDrawerOpen);
            updateAuxOverlayVisibility();
            if (mode == HabitatWorld.Mode.TERMINAL) {
                terminal.toFront();
                positionHelpDrawerNextToTerminal();
                if (helpDrawer.isVisible()) helpDrawer.toFront();
                stage.setKeyboardFocus(editor);
            } else if (mode == HabitatWorld.Mode.INSPECTION) {
                centerWindow(inspection);
                inspection.toFront();
                stage.setKeyboardFocus(inspection);
            }
            shown = mode;
        }

        if (mode == HabitatWorld.Mode.EXPLORING) {
            modeBadge.setText(activeOverlay == AuxOverlay.NONE ? "OUTPOST" : activeOverlay.name());
            modeBadge.setColor(COL_EMERALD);
        } else if (mode == HabitatWorld.Mode.INSPECTION) {
            modeBadge.setText("TELEMETRI");
            modeBadge.setColor(COL_CYAN);
        } else {
            modeBadge.setText("ROVER IDE");
            modeBadge.setColor(COL_AMBER);
        }

        editorPanel.setBackground(stage.getKeyboardFocus() == editor
            ? (highContrastFocus ? editorFocusHighBg : editorFocusStdBg)
            : editorNormalBg);
        updateVisibleEditorGutter();

        HabitatWorld.Facility nearby = world.nearbyFacility();
        if (mode != HabitatWorld.Mode.EXPLORING) {
            bottomPromptBox.setBackground(promptModalBg);
            prompt.setText(mode == HabitatWorld.Mode.TERMINAL
                ? "Editor Rover aktif (tarik sudut untuk resize) | [Esc] kembali ke Outpost"
                : "Panel Telemetri aktif | Tekan [Esc] untuk kembali");
        } else if (activeOverlay != AuxOverlay.NONE) {
            bottomPromptBox.setBackground(promptModalBg);
            prompt.setText("Overlay aktif | Tekan [Esc] untuk tutup");
        } else if (nearby == HabitatWorld.Facility.ROVER) {
            bottomPromptBox.setBackground(promptNearBg);
            prompt.setText("Klik Rover atau tekan [E] untuk edit kode Python");
        } else if (nearby == HabitatWorld.Facility.PANEL) {
            bottomPromptBox.setBackground(promptNearBg);
            prompt.setText("[E] atau klik Panel Telemetri untuk melihat briefing P1");
        } else {
            bottomPromptBox.setBackground(promptIdleBg);
            prompt.setText("[WASD] Jelajah Outpost • Ikuti jalur menuju Bio-Dome Timur & klik Rover");
        }
    }

    private void syncPuzzle() {
        RunCoordinator.View view = coordinator.view();
        shownPuzzle = view;
        var snap = view.snapshot();

        int harvests = snap.harvestCount();
        int actions = snap.actionCount();
        int maxActions = 6;
        puzzleStats.setText("M1-P1 | PANEN " + harvests + "/2 | AKSI " + actions + "/" + maxActions);
        goalSummaryLabel.setText("Panen " + harvests + "/2 • Aksi " + actions + "/" + maxActions);

        Status st = view.status();
        updateStatusBadge(st, view.completed());
        updateMissionMapCard(st, view.completed(), harvests, actions, maxActions);

        runButton.setDisabled(!view.canRun());
        stopButton.setDisabled(!view.canStop());
        runButton.getColor().a = view.canRun() ? 1f : 0.45f;
        stopButton.getColor().a = view.canStop() ? 1f : 0.45f;

        boolean hasOutput = view.output() != null && !view.output().isBlank();
        boolean hasNotableState = st != Status.READY;
        boolean showLog = logManualExpanded || hasOutput || hasNotableState;

        logToggleBtn.setText(showLog ? "Log [-]" : (hasOutput || view.completed() ? "Log •" : "Log [+]"));

        if (!showLog) {
            consoleBox.setVisible(false);
            consoleBoxCell.height(0f);
        } else {
            consoleBox.setVisible(true);
            float baseH = Math.max(96f, Math.min(140f, terminal.getHeight() * 0.28f));
            float boxH = st == Status.SUCCESS ? Math.max(114f, baseH) : baseH;
            consoleBoxCell.height(boxH);
        }

        String title;
        String hint;
        switch (st) {
            case PREPARING -> {
                consoleBox.setBackground(consoleNormalBg);
                consoleTitleLabel.setColor(COL_CYAN);
                title = "Menyiapkan Python bundel...";
                hint = "Tunggu sebentar, runtime sedang dijalankan.";
            }
            case RUNNING -> {
                consoleBox.setBackground(consoleNormalBg);
                consoleTitleLabel.setColor(COL_CYAN);
                title = "Menjalankan kode Python...";
                hint = "Klik Stop bila loop terlalu lama.";
            }
            case REPLAYING -> {
                consoleBox.setBackground(consoleNormalBg);
                consoleTitleLabel.setColor(COL_CYAN);
                title = "Memutar aksi rover secara halus...";
                hint = "Amati rover bergerak dan memanen di Bio-Dome kanan.";
            }
            case SUCCESS -> {
                consoleBox.setBackground(consoleSuccessBg);
                consoleTitleLabel.setColor(COL_EMERALD);
                title = "Sukses! " + harvests + "/2 tanaman dipanen (" + actions + "/" + maxActions + " aksi).";
                hint = "Pencapaian sesi P1 tercatat.";
            }
            case INCOMPLETE -> {
                consoleBox.setBackground(consoleWarnBg);
                consoleTitleLabel.setColor(COL_AMBER);
                title = "Target belum selesai (" + harvests + "/2 dipanen).";
                hint = "Pastikan rover memanen di (0,0), pindah east, lalu harvest() di (1,0).";
            }
            case LIMIT_EXCEEDED -> {
                consoleBox.setBackground(consoleWarnBg);
                consoleTitleLabel.setColor(COL_AMBER);
                title = "Batas aksi terlampaui (" + actions + "/" + maxActions + ").";
                hint = "Kurangi langkah gerak/panen agar <= 6 aksi.";
            }
            case ERROR -> {
                consoleBox.setBackground(consoleErrorBg);
                consoleTitleLabel.setColor(COL_CORAL);
                title = "Terjadi Error pada Script Python";
                hint = view.message();
            }
            case TIMEOUT -> {
                consoleBox.setBackground(consoleErrorBg);
                consoleTitleLabel.setColor(COL_CORAL);
                title = "Waktu Eksekusi Habis (Timeout)";
                hint = view.message();
            }
            case STOPPED -> {
                consoleBox.setBackground(consoleWarnBg);
                consoleTitleLabel.setColor(COL_AMBER);
                title = "Eksekusi Dihentikan";
                hint = "Draf kode Anda tetap aman untuk diedit ulang.";
            }
            default -> {
                consoleBox.setBackground(consoleNormalBg);
                consoleTitleLabel.setColor(COL_CYAN);
                title = "Log Percobaan";
                hint = view.message();
            }
        }

        consoleTitleLabel.setText(title);
        String detailText = hasOutput ? hint + "\n" + view.output().stripTrailing() : hint;
        consoleDetailLabel.setText(detailText);
        consoleDetailLabel.setColor(st == Status.ERROR || st == Status.TIMEOUT ? COL_CORAL : COL_TEXT_SECONDARY);

        boolean showSuccessActions = st == Status.SUCCESS;
        successSummaryRow.setVisible(showSuccessActions);
        successSummaryRowCell.height(showSuccessActions ? 32f : 0f).padTop(showSuccessActions ? 4f : 0f);
        successSummaryLabel.setText("2 dipanen • " + actions + " aksi");
    }

    private void updateStatusBadge(Status st, boolean completed) {
        switch (st) {
            case PREPARING, RUNNING, REPLAYING -> {
                goalStatusLabel.setText("JALAN");
                goalStatusLabel.setColor(COL_CYAN);
            }
            case SUCCESS -> {
                goalStatusLabel.setText("SELESAI");
                goalStatusLabel.setColor(COL_EMERALD);
            }
            case ERROR, TIMEOUT -> {
                goalStatusLabel.setText("ERROR");
                goalStatusLabel.setColor(COL_CORAL);
            }
            case INCOMPLETE, LIMIT_EXCEEDED, STOPPED -> {
                goalStatusLabel.setText("CEK");
                goalStatusLabel.setColor(COL_AMBER);
            }
            default -> {
                goalStatusLabel.setText(completed ? "LULUS" : "SIAP");
                goalStatusLabel.setColor(completed ? COL_EMERALD : COL_TEXT_PRIMARY);
            }
        }
    }

    private void updateMissionMapCard(Status st, boolean completed, int harvests, int actions, int maxActions) {
        boolean running = st == Status.PREPARING || st == Status.RUNNING || st == Status.REPLAYING;
        if (completed) {
            p1MissionCard.setBackground(cardEmeraldBg);
            p1MissionStatusLabel.setStyle(emeraldStyle);
            p1MissionStatusLabel.setText("P1 • SELESAI (SESI)");
            p1MissionDetailLabel.setText("Dua tanaman dipanen. Terakhir: " + harvests + "/2, " + actions + "/" + maxActions + " aksi.");
        } else if (running) {
            p1MissionCard.setBackground(cardAmberBg);
            p1MissionStatusLabel.setStyle(amberStyle);
            p1MissionStatusLabel.setText("P1 • SEDANG DIJALANKAN");
            p1MissionDetailLabel.setText("Simulasi rover berjalan (" + actions + "/" + maxActions + " aksi).");
        } else {
            p1MissionCard.setBackground(cardCyanBg);
            p1MissionStatusLabel.setStyle(cyanStyle);
            p1MissionStatusLabel.setText("P1 • TERSEDIA");
            p1MissionDetailLabel.setText("Klik Rover di Bio-Dome Timur untuk memulai puzzle.");
        }
    }

    private void updateVisibleEditorGutter() {
        int totalLines = Math.max(1, editor.getLines());
        int showing = Math.max(1, editor.getLinesShowing());
        int firstLine = Math.max(0, Math.min(totalLines - 1, editor.getFirstLineShowing()));
        int endLine = Math.min(totalLines, firstLine + showing);

        StringBuilder sb = new StringBuilder();
        for (int i = firstLine + 1; i <= endLine; i++) {
            if (sb.length() > 0) sb.append('\n');
            sb.append(i);
        }
        lineNumbersLabel.setText(sb.toString());
        editorHeaderRangeLabel.setText("Baris " + (firstLine + 1) + "-" + endLine + " / " + totalLines);
    }

    private void refreshSettingsLabels() {
        reducedMotionBtn.setText("Kurangi Animasi Kamera & Partikel: " + (reducedMotion ? "AKTIF" : "MATI"));
        highContrastBtn.setText("Bingkai Fokus Kontras Tinggi: " + (highContrastFocus ? "AKTIF" : "STANDAR"));
        previewVisualBtn.setText("Preview Visual Outpost Pulih (Kosmetik): " + (outpostRestored ? "AKTIF" : "MATI"));
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        clampTerminalWindow();
        clampWindow(inspection);
        clampWindow(helpDrawer);
        clampWindow(fieldLogWindow);
        clampWindow(missionMapWindow);
        clampWindow(settingsWindow);
        positionHelpDrawerNextToTerminal();
    }

    public void draw(float delta) {
        stage.getViewport().apply();
        clampTerminalWindow();
        positionHelpDrawerNextToTerminal();
        editorPanel.setBackground(stage.getKeyboardFocus() == editor
            ? (highContrastFocus ? editorFocusHighBg : editorFocusStdBg)
            : editorNormalBg);
        updateVisibleEditorGutter();
        stage.act(Math.min(delta, 0.1f));
        stage.draw();
    }

    private void clampTerminalWindow() {
        float sw = stage.getViewport().getWorldWidth();
        float sh = stage.getViewport().getWorldHeight();
        float maxW = Math.max(TERMINAL_MIN_W, sw - 24f);
        float maxH = Math.max(TERMINAL_MIN_H, sh - 76f);
        float w = Math.max(TERMINAL_MIN_W, Math.min(maxW, terminal.getWidth()));
        float h = Math.max(TERMINAL_MIN_H, Math.min(maxH, terminal.getHeight()));
        if (Math.abs(w - terminal.getWidth()) > 0.5f || Math.abs(h - terminal.getHeight()) > 0.5f) {
            terminal.setSize(w, h);
        }
        float x = Math.max(10f, Math.min(sw - w - 10f, terminal.getX()));
        float y = Math.max(44f, Math.min(sh - h - 42f, terminal.getY()));
        terminal.setPosition(x, y);
    }

    private void positionHelpDrawerNextToTerminal() {
        if (!helpDrawer.isVisible()) return;
        float sw = stage.getViewport().getWorldWidth();
        float rightX = terminal.getRight() + 10f;
        float targetX = (rightX + helpDrawer.getWidth() <= sw - 10f)
            ? rightX
            : Math.max(10f, terminal.getX() - helpDrawer.getWidth() - 10f);
        helpDrawer.setPosition(targetX, terminal.getTop() - helpDrawer.getHeight());
        clampWindow(helpDrawer);
    }

    private void centerWindow(Window w) {
        float sw = stage.getViewport().getWorldWidth();
        float sh = stage.getViewport().getWorldHeight();
        w.setPosition(Math.round((sw - w.getWidth()) * 0.5f), Math.round((sh - w.getHeight()) * 0.5f));
        clampWindow(w);
    }

    private void clampWindow(Window w) {
        float sw = stage.getViewport().getWorldWidth();
        float sh = stage.getViewport().getWorldHeight();
        w.setPosition(
            Math.max(8f, Math.min(sw - w.getWidth() - 8f, w.getX())),
            Math.max(8f, Math.min(sh - w.getHeight() - 8f, w.getY()))
        );
    }

    private Window createBaseWindow(String title, WindowStyle style, float width, float height) {
        Window w = new Window(title, style);
        w.setVisible(false);
        w.setMovable(true);
        w.setKeepWithinStage(false);
        w.setSize(width, height);
        w.pad(36f, 10f, 10f, 10f);
        return w;
    }

    private Label wrapLabel(String text, LabelStyle style) {
        Label label = new Label(text, style);
        label.setWrap(true);
        return label;
    }

    private static ChangeListener onAction(Runnable action) {
        return new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                action.run();
            }
        };
    }

    private BitmapFont loadBitmapFont(String fntPath, String pngPath, float scale, boolean integerPos) {
        BitmapFont font = new BitmapFont(
            Gdx.files.internal(fntPath),
            new TextureRegion(loadUiTexture(pngPath)),
            false
        );
        font.setUseIntegerPositions(integerPos);
        font.getData().setScale(scale);
        ownedFonts.add(font);
        return font;
    }

    private Texture loadUiTexture(String path) {
        Texture texture = new Texture(Gdx.files.internal(path), true);
        texture.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
        ownedTextures.add(texture);
        return texture;
    }

    private Drawable makeSolidColor(Color color, int w, int h) {
        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        ownedTextures.add(texture);
        return new TextureRegionDrawable(new TextureRegion(texture));
    }

    private TextButtonStyle makeButtonStyle(
        BitmapFont font, Color fontCol, Color upFill, Color overFill, Color downFill,
        Color upBorder, Color focusBorder, int radius
    ) {
        TextButtonStyle style = new TextButtonStyle();
        style.font = font;
        style.fontColor = fontCol;
        style.overFontColor = fontCol;
        style.downFontColor = fontCol;
        style.up = makeRoundedBox(radius * 2 + 12, radius, upFill, upBorder, 1.5f);
        style.over = makeRoundedBox(radius * 2 + 12, radius, overFill, focusBorder, 1.8f);
        style.down = makeRoundedBox(radius * 2 + 12, radius, downFill, focusBorder, 2.0f);
        style.focused = style.over;
        return style;
    }

    /** 3x supersampled SDF anti-aliased rounded rectangle NinePatch (zero jagged boxy edges on resize!). */
    private Drawable makeRoundedBox(int logicalMinSize, int logicalRadius, Color fill, Color border, float borderWidth) {
        int scale = 3;
        int minSize = Math.max(logicalRadius * 2 + 8, logicalMinSize);
        int size = minSize * scale;
        float radius = logicalRadius * scale;
        float bw = borderWidth * scale;

        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        paintSignedRoundedRect(pm, size, size, radius, bw, fill, border, 0, null);
        Texture tex = new Texture(pm, true);
        tex.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
        pm.dispose();
        ownedTextures.add(tex);

        int split = Math.min(size / 2 - 2, (int) Math.ceil(radius + bw) + 2);
        NinePatch patch = new NinePatch(tex, split, split, split, split);
        patch.scale(1f / scale, 1f / scale);
        return new NinePatchDrawable(patch);
    }

    private Drawable makeRoundedWindow(Color fill, Color border, int logicalRadius, int logicalHeaderH) {
        int scale = 3;
        int radius = logicalRadius * scale;
        int headerH = logicalHeaderH * scale;
        int w = (logicalRadius * 2 + 16) * scale;
        int h = headerH + (logicalRadius + 16) * scale;
        float bw = 1.6f * scale;

        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        Color headerTint = new Color(
            Math.min(1f, fill.r + 0.04f),
            Math.min(1f, fill.g + 0.06f),
            Math.min(1f, fill.b + 0.10f),
            Math.min(1f, fill.a + 0.05f)
        );
        paintSignedRoundedRect(pm, w, h, radius, bw, fill, border, headerH, headerTint);
        Texture tex = new Texture(pm, true);
        tex.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
        pm.dispose();
        ownedTextures.add(tex);

        int sideSplit = radius + 6;
        int bottomSplit = radius + 6;
        int topSplit = headerH + 6;
        NinePatch patch = new NinePatch(tex, sideSplit, sideSplit, topSplit, bottomSplit);
        patch.scale(1f / scale, 1f / scale);
        return new NinePatchDrawable(patch);
    }

    private static void paintSignedRoundedRect(
        Pixmap pm, int w, int h, float radius, float borderWidth,
        Color fill, Color border, int headerHeight, Color headerTint
    ) {
        float halfW = (w - 2f) * 0.5f;
        float halfH = (h - 2f) * 0.5f;
        float cx = w * 0.5f - 0.5f;
        float cy = h * 0.5f - 0.5f;
        float r = Math.min(radius, Math.min(halfW, halfH));
        float innerW = halfW - r;
        float innerH = halfH - r;

        for (int py = 0; py < h; py++) {
            for (int px = 0; px < w; px++) {
                float dx = Math.abs(px - cx) - innerW;
                float dy = Math.abs(py - cy) - innerH;
                float dist = (float) Math.hypot(Math.max(dx, 0f), Math.max(dy, 0f)) + Math.min(Math.max(dx, dy), 0f) - r;

                float edgeCoverage = clamp01(0.6f - dist);
                if (edgeCoverage <= 0f) {
                    pm.drawPixel(px, py, 0);
                    continue;
                }
                float borderBlend = borderWidth > 0f ? clamp01((dist + borderWidth + 0.5f) / 1.4f) : 0f;
                Color baseFill = (headerHeight > 0 && py < headerHeight && headerTint != null) ? headerTint : fill;
                if (headerHeight > 0 && Math.abs(py - headerHeight) <= 1.2f && dist < -borderWidth) {
                    baseFill = border;
                }

                float outR = baseFill.r + (border.r - baseFill.r) * borderBlend;
                float outG = baseFill.g + (border.g - baseFill.g) * borderBlend;
                float outB = baseFill.b + (border.b - baseFill.b) * borderBlend;
                float outA = (baseFill.a + (border.a - baseFill.a) * borderBlend) * edgeCoverage;
                pm.drawPixel(px, py, Color.rgba8888(outR, outG, outB, outA));
            }
        }
    }

    private Drawable makeResizeGripDrawable() {
        int s = 28;
        Pixmap pm = new Pixmap(s, s, Pixmap.Format.RGBA8888);
        pm.setColor(new Color(COL_CYAN.r, COL_CYAN.g, COL_CYAN.b, 0.78f));
        for (int k = 0; k < 3; k++) {
            int offset = 4 + k * 7;
            pm.drawLine(offset, s - 4, s - 4, offset);
            pm.drawLine(offset + 1, s - 4, s - 4, offset + 1);
        }
        Texture tex = new Texture(pm);
        tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        pm.dispose();
        ownedTextures.add(tex);
        return new TextureRegionDrawable(new TextureRegion(tex));
    }

    private static float clamp01(float v) {
        return v < 0f ? 0f : (v > 1f ? 1f : v);
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        for (BitmapFont bf : ownedFonts) {
            bf.dispose();
        }
        ownedFonts.clear();
        for (Texture texture : ownedTextures) {
            texture.dispose();
        }
        ownedTextures.clear();
    }
}
