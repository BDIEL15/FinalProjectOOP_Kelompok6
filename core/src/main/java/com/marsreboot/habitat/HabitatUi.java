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
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
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
import com.badlogic.gdx.utils.viewport.FitViewport;
import java.util.ArrayList;
import java.util.List;

/**
 * Sci-Fi Game HUD and complete UI overlays for the Outdoor Mars Outpost:
 * - Top/Bottom Sci-Fi HUD with Before/After toggle [B], Field Log [J], Mission Map 1-9 [M], Settings [O]
 * - Outdoor Solar & Water Panel Inspection Modal
 * - Upgraded Floating Draggable Terminal (The Farmer Was Replaced style) + Error/Timeout/Stopped states
 * - 3-Step Progressive Help Drawer (FR-020)
 * - Field Log Astronaut Modal, Mission Map 1-9 Modal, and Settings & Accessibility Modal (UI-05)
 */
public final class HabitatUi implements Disposable {
    private static final Color COL_TEXT_PRIMARY = Color.valueOf("F8FAFC");
    private static final Color COL_TEXT_SECONDARY = Color.valueOf("CBD5E1");
    private static final Color COL_TEXT_MUTED = Color.valueOf("94A3B8");
    private static final Color COL_CYAN = Color.valueOf("38BDF8");
    private static final Color COL_MARS = Color.valueOf("F59E0B");
    private static final Color COL_GREEN = Color.valueOf("34D399");
    private static final Color COL_ALERT = Color.valueOf("F87171");

    private static boolean outpostRestored = false;
    private static boolean reducedMotion = false;
    private static boolean highContrastFocus = true;

    public static boolean isOutpostRestored() { return outpostRestored; }
    public static boolean isReducedMotion() { return reducedMotion; }
    public static void setOutpostRestored(boolean restored) { outpostRestored = restored; }

    public enum AuxOverlay { NONE, FIELD_LOG, MISSION_MAP, SETTINGS }
    public enum TerminalState { NORMAL, ERROR, TIMEOUT, STOPPED }

    private final HabitatWorld world;
    private final Stage stage = new Stage(new FitViewport(960, 600));
    private final Skin skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
    private final List<Texture> ownedTextures = new ArrayList<>();
    private final List<BitmapFont> ownedFonts = new ArrayList<>();

    private final BitmapFont uiFont;
    private final BitmapFont uiBoldFont;
    private final BitmapFont monoFont;

    private final Label prompt;
    private final Label modeBadge;
    private final Label focusStatusLabel;
    private final Label lineNumbersLabel;
    private final Label consoleTitleLabel;
    private final Label consoleDetailLabel;
    private final Table consoleBox;
    private final Table bottomPromptBox;
    private final Drawable promptIdleBg;
    private final Drawable promptNearBg;
    private final Drawable promptModalBg;
    private final Drawable consoleNormalBg;
    private final Drawable consoleErrorBg;
    private final Drawable consoleWarnBg;

    private final TextButton beforeAfterBtn;
    private final TextButton step3UnlockBtn;
    private final Label step3BodyLabel;
    private final TextButton reducedMotionBtn;
    private final TextButton highContrastBtn;
    private final TextButton outpostStateSettingsBtn;

    // Exact reflection fields verified by HabitatDesktopSmokeTest
    private final Window inspection;
    private final Window terminal;
    private final TextArea editor;

    // Additional UI screens from approved Outdoor Mars Outpost design (07..11)
    private final Window helpDrawer;
    private final Window fieldLogWindow;
    private final Window missionMapWindow;
    private final Window settingsWindow;

    private HabitatWorld.Mode shown;
    private AuxOverlay activeOverlay = AuxOverlay.NONE;
    private TerminalState terminalState = TerminalState.NORMAL;
    private boolean helpDrawerOpen = false;
    private boolean step3Unlocked = false;

    public HabitatUi(HabitatWorld world) {
        this.world = world;
        outpostRestored = false;
        reducedMotion = false;

        BitmapFont fallbackFont = skin.getFont("default");
        fallbackFont.setUseIntegerPositions(true);
        uiFont = loadHiDpiFont("ui/ui_sans.fnt", "ui/ui_sans.png", 0.37f, fallbackFont);
        uiBoldFont = loadHiDpiFont("ui/ui_sans_bold.fnt", "ui/ui_sans_bold.png", 0.37f, fallbackFont);
        monoFont = loadHiDpiFont("ui/ui_mono.fnt", "ui/ui_mono.png", 0.36f, fallbackFont);

        LabelStyle primaryStyle = new LabelStyle(uiFont, COL_TEXT_PRIMARY);
        LabelStyle boldPrimaryStyle = new LabelStyle(uiBoldFont, COL_TEXT_PRIMARY);
        LabelStyle secondaryStyle = new LabelStyle(uiFont, COL_TEXT_SECONDARY);
        LabelStyle mutedStyle = new LabelStyle(uiFont, COL_TEXT_MUTED);
        LabelStyle cyanStyle = new LabelStyle(uiBoldFont, COL_CYAN);
        LabelStyle marsStyle = new LabelStyle(uiBoldFont, COL_MARS);
        LabelStyle greenStyle = new LabelStyle(uiBoldFont, COL_GREEN);
        LabelStyle alertStyle = new LabelStyle(uiBoldFont, COL_ALERT);
        LabelStyle monoCyanStyle = new LabelStyle(monoFont, COL_CYAN);
        LabelStyle monoMutedStyle = new LabelStyle(monoFont, COL_TEXT_MUTED);

        promptIdleBg = createBoxDrawable("131C28", "334155", 1, 8, 8, 3, 3);
        promptNearBg = createBoxDrawable("0F293A", "38BDF8", 2, 8, 8, 3, 3);
        promptModalBg = createBoxDrawable("261710", "F59E0B", 1, 8, 8, 3, 3);
        consoleNormalBg = createBoxDrawable("090D14", "1E293B", 1, 8, 8, 5, 5);
        consoleErrorBg = createBoxDrawable("221014", "EF4444", 2, 8, 8, 5, 5);
        consoleWarnBg = createBoxDrawable("23180C", "F59E0B", 2, 8, 8, 5, 5);

        prompt = new Label("", boldPrimaryStyle);
        modeBadge = new Label("MODE: OUTPOST", greenStyle);
        focusStatusLabel = new Label("FOKUS: EDITOR AKTIF", cyanStyle);
        lineNumbersLabel = new Label("1\n2\n3", monoMutedStyle);
        lineNumbersLabel.setAlignment(Align.topRight);

        // 1. Sci-Fi Top & Bottom HUD Bars (leaves y=40..560 outdoor Mars yard 100% unobstructed)
        Table hud = new Table();
        hud.setFillParent(true);

        Table topBar = new Table();
        topBar.setBackground(createBoxDrawable("0F1722", "334155", 1, 8, 8, 3, 3));
        topBar.add(new Label("MARS_REBOOT.PY", cyanStyle)).left().padRight(6);

        Table statsChip = new Table();
        statsChip.setBackground(createBoxDrawable("162232", "334155", 1, 5, 5, 2, 2));
        statsChip.add(new Label("M1-P2 | AKSI 00/30 | AIR 2/2", secondaryStyle));
        topBar.add(statsChip).left().padRight(6);

        beforeAfterBtn = smallHudButton("[B] BEFORE", "261C14", "F59E0B", () -> toggleBeforeAfter());
        topBar.add(beforeAfterBtn).left().padRight(6);

        topBar.add().expandX();

        topBar.add(smallHudButton("[J] Log", "162232", "38BDF8", () -> toggleOverlay(AuxOverlay.FIELD_LOG)))
            .right().padRight(4);
        topBar.add(smallHudButton("[M] Misi", "162232", "38BDF8", () -> toggleOverlay(AuxOverlay.MISSION_MAP)))
            .right().padRight(4);
        topBar.add(smallHudButton("[O] Setelan", "162232", "38BDF8", () -> toggleOverlay(AuxOverlay.SETTINGS)))
            .right().padRight(6);

        Table modeBox = new Table();
        modeBox.setBackground(createBoxDrawable("142230", "385268", 1, 5, 5, 2, 2));
        modeBox.add(modeBadge);
        topBar.add(modeBox).right();

        Table bottomBar = new Table();
        bottomBar.setBackground(createBoxDrawable("0F1722", "334155", 1, 8, 8, 3, 3));
        bottomPromptBox = new Table();
        bottomPromptBox.setBackground(promptIdleBg);
        bottomPromptBox.add(prompt).left();
        bottomBar.add(bottomPromptBox).left().expandX();

        Table controlsBox = new Table();
        controlsBox.setBackground(createBoxDrawable("141E2B", "334155", 1, 6, 6, 2, 2));
        controlsBox.add(new Label("[WASD] Gerak  [E] Aksi  [B/J/M/O] Menu  [Esc] Tutup", secondaryStyle));
        bottomBar.add(controlsBox).right().padLeft(6);

        hud.top();
        hud.add(topBar).growX().height(34).row();
        hud.add().expand().fill().row();
        hud.add(bottomBar).growX().height(34);
        stage.addActor(hud);

        // 2. Outdoor Solar & Water Panel Inspection Modal Window
        WindowStyle cyanWinStyle = new WindowStyle(
            uiBoldFont, COL_TEXT_PRIMARY, createWindowDrawable("101925", "16263A", "38BDF8", 2, 30)
        );
        inspection = new Window("  INSPEKSI OUTPOST MARS  |  PANEL SURYA & TANGKI AIR", cyanWinStyle);
        inspection.setModal(true);
        inspection.setMovable(false);
        inspection.getTitleTable().padLeft(6).padTop(4);
        inspection.padTop(38).padLeft(16).padRight(16).padBottom(14);

        Table inspMetaRow = new Table();
        Table inspTypeBadge = new Table();
        inspTypeBadge.setBackground(createBoxDrawable("0F293A", "38BDF8", 1, 8, 8, 2, 2));
        inspTypeBadge.add(new Label("TEMUAN LAPANGAN OUTDOOR (TERCATAT KE FIELD LOG [J])", cyanStyle));
        inspMetaRow.add(inspTypeBadge).left().expandX();

        Table inspFocusBadge = new Table();
        inspFocusBadge.setBackground(createBoxDrawable("1E293B", "38BDF8", 1, 8, 8, 2, 2));
        inspFocusBadge.add(new Label("FOKUS: INSPEKSI [Esc]", primaryStyle));
        inspMetaRow.add(inspFocusBadge).right();
        inspection.add(inspMetaRow).growX().padBottom(10).row();

        inspection.add(buildInfoCard(
            "1. Panel Surya Hab (Outdoor): Tertutup Debu Mars 78%",
            "Daya cadangan terbatas (maks 30 aksi per run). Tekan [B] di HUD untuk preview kondisi setelah pulih.",
            marsStyle, secondaryStyle, "0C131D", "F59E0B"
        )).growX().padBottom(8).row();

        inspection.add(buildInfoCard(
            "2. Platform Bio-Dome (4x4): 2 Petak Kering [!] & Jatah Air 2 Unit",
            "Tangki air eksternal di sisi barat Bio-Dome hanya memuat 2 unit air. Hindari batu merah di jalur tengah.",
            cyanStyle, secondaryStyle, "0C131D", "38BDF8"
        )).growX().padBottom(8).row();

        inspection.add(buildInfoCard(
            "3. Jalur Pipa Timur -> Stasiun Pompa Utama (Misi 3)",
            "Pipa air menuju Stasiun Pompa Utama akan menyala otomatis setelah Misi 1 & 2 di Bio-Dome selesai.",
            greenStyle, mutedStyle, "0C131D", "34D399"
        )).growX().padBottom(12).row();

        Table inspFooter = new Table();
        inspFooter.add(new Label("Temuan ini dapat dibuka kembali lewat [J] Field Log saat coding.", mutedStyle))
            .left().expandX();
        inspFooter.add(closeButton("Kembali ke Outpost [Esc]", () -> world.closePanel())).right();
        inspection.add(inspFooter).growX();

        inspection.setSize(610, 392);
        inspection.setPosition(175, 104);

        // 3. Upgraded Sci-Fi Floating Draggable Terminal Window (08_scifi_floating_terminal_hud.png)
        WindowStyle terminalWinStyle = new WindowStyle(
            uiBoldFont, COL_TEXT_PRIMARY, createWindowDrawable("0D141E", "152336", "38BDF8", 2, 30)
        );
        terminal = new Window("  ::: TERMINAL LAPANGAN BIO-DOME  |  M1-P2 (geser judul)", terminalWinStyle);
        terminal.setModal(true);
        terminal.setMovable(true);
        terminal.getTitleTable().padLeft(6).padTop(4);
        terminal.padTop(36).padLeft(12).padRight(12).padBottom(12);

        // Top status & Help Drawer toggle strip
        Table statusStrip = new Table();
        Table noExecBadge = new Table();
        noExecBadge.setBackground(createBoxDrawable("2A1612", "F59E0B", 1, 5, 5, 2, 2));
        noExecBadge.add(new Label("[!] Preview UI", marsStyle));
        statusStrip.add(noExecBadge).left().padRight(5);

        Table focusBadge = new Table();
        focusBadge.setBackground(createBoxDrawable("0F293A", "38BDF8", 1, 5, 5, 2, 2));
        focusBadge.add(focusStatusLabel);
        statusStrip.add(focusBadge).left().expandX();

        TextButton helpBtn = smallHudButton("? BANTUAN (3)", "1E3A5F", "38BDF8", () -> toggleHelpDrawer());
        statusStrip.add(helpBtn).right();
        terminal.add(statusStrip).growX().padBottom(6).row();

        // Quick-Ref API Chips row (08_scifi_floating_terminal_hud.png)
        Table chipsRow = new Table();
        chipsRow.left();
        chipsRow.add(new Label("API:", mutedStyle)).padRight(5);
        for (String chip : new String[] {"move()", "turn_left()", "water()", "scan()"}) {
            Table chipBox = new Table();
            chipBox.setBackground(createBoxDrawable("132238", "334155", 1, 5, 5, 2, 2));
            chipBox.add(new Label(chip, monoCyanStyle));
            chipsRow.add(chipBox).padRight(4);
        }
        terminal.add(chipsRow).growX().padBottom(6).row();

        // Editor header bar
        Table editorHeader = new Table();
        editorHeader.setBackground(createBoxDrawable("162232", "334155", 1, 6, 6, 3, 3));
        editorHeader.add(new Label(">_ solusi_biodome.py", monoCyanStyle)).left().expandX();
        editorHeader.add(new Label("Draf aman | Esc keluar", mutedStyle)).right();
        terminal.add(editorHeader).growX().row();

        TextFieldStyle editorStyle = new TextFieldStyle(
            monoFont,
            COL_TEXT_PRIMARY,
            createFlatDrawable("38BDF8", 2, 16),
            createFlatDrawable("1E4E68", 4, 16),
            createBoxDrawable("090D14", "1E293B", 1, 8, 8, 6, 6)
        );
        editorStyle.focusedBackground = createBoxDrawable("0B131E", "38BDF8", 2, 8, 8, 6, 6);
        editorStyle.disabledBackground = createBoxDrawable("090E14", "1E293B", 1, 8, 8, 6, 6);
        editorStyle.focusedFontColor = COL_TEXT_PRIMARY;
        editorStyle.messageFont = monoFont;
        editorStyle.messageFontColor = COL_TEXT_MUTED;

        editor = new TextArea(
            "# Pulihkan bed hidroponik Bio-Dome\nfor i in range(2):\n    move()\n    water()\n",
            editorStyle
        );
        editor.setFocusTraversal(false);
        editor.setCursorPosition(editor.getText().length());

        Table editorContainer = new Table();
        Table gutter = new Table();
        gutter.setBackground(createBoxDrawable("0D131D", "243442", 1, 6, 6, 6, 6));
        gutter.top().right();
        gutter.add(lineNumbersLabel).top().right();
        editorContainer.add(gutter).width(32).fillY();
        editorContainer.add(editor).grow().minHeight(170);
        terminal.add(editorContainer).grow().padBottom(6).row();

        // Interactive Terminal State Switcher (lets user inspect Normal, Error, Timeout, Stopped from 09)
        Table stateBar = new Table();
        stateBar.left();
        stateBar.add(new Label("State:", mutedStyle)).padRight(5);
        stateBar.add(smallHudButton("Normal", "142636", "38BDF8", () -> setTerminalState(TerminalState.NORMAL))).padRight(4);
        stateBar.add(smallHudButton("Error", "3B1219", "F87171", () -> setTerminalState(TerminalState.ERROR))).padRight(4);
        stateBar.add(smallHudButton("Timeout", "2E1F0F", "F59E0B", () -> setTerminalState(TerminalState.TIMEOUT))).padRight(4);
        stateBar.add(smallHudButton("Stopped", "1E293B", "94A3B8", () -> setTerminalState(TerminalState.STOPPED)));
        terminal.add(stateBar).growX().padBottom(6).row();

        // Output / Error Feedback Box (08 & 09)
        consoleBox = new Table();
        consoleBox.setBackground(consoleNormalBg);
        consoleTitleLabel = new Label("OUTPUT TERMINAL (PREVIEW UI)", cyanStyle);
        consoleTitleLabel.setWrap(true);
        consoleDetailLabel = new Label(
            "Draf kode aman. Klik tombol State di atas atau [? BANTUAN (3)] untuk cek UI.",
            secondaryStyle
        );
        consoleDetailLabel.setWrap(true);
        consoleBox.add(consoleTitleLabel).width(492).left().padBottom(2).row();
        consoleBox.add(consoleDetailLabel).width(492).left();
        terminal.add(consoleBox).growX().padBottom(8).row();

        Table termFooter = new Table();
        termFooter.add(new Label("Geser judul | Bio-Dome terlihat di kanan", secondaryStyle))
            .left().expandX();
        termFooter.add(closeButton("Kembali [Esc]", () -> world.closePanel())).right();
        terminal.add(termFooter).growX();

        terminal.setSize(540, 512);
        terminal.setPosition(22, 42);

        // 4. Progressive 3-Step Help Drawer (09_terminal_error_and_help_drawer.png · FR-020)
        helpDrawer = new Window("  DRAWER BANTUAN BERTAHAP (3 LANGKAH · FR-020)", cyanWinStyle);
        helpDrawer.setModal(false);
        helpDrawer.setMovable(true);
        helpDrawer.getTitleTable().padLeft(6).padTop(4);
        helpDrawer.padTop(36).padLeft(12).padRight(12).padBottom(12);

        helpDrawer.add(buildInfoCard(
            "LANGKAH 1 · MASALAH",
            "Tanaman di bed timur Bio-Dome belum terjangkau jika rover keluar batas platform.",
            cyanStyle, secondaryStyle, "0C131D", "38BDF8"
        )).growX().padBottom(8).row();

        helpDrawer.add(buildInfoCard(
            "LANGKAH 2 · SENSOR TERKAIT",
            "Field Log [J]: Grid Bio-Dome berukuran 4x4 (0..3) dan jatah air 2 unit. Cek scan() sebelum water().",
            marsStyle, secondaryStyle, "0C131D", "F59E0B"
        )).growX().padBottom(8).row();

        Table step3Card = new Table();
        step3Card.setBackground(createBoxDrawable("0C131D", "34D399", 1, 10, 10, 7, 7));
        step3Card.add(new Label("LANGKAH 3 · CONTOH POLA", greenStyle)).left().growX().padBottom(3).row();
        step3BodyLabel = new Label("Terkunci agar tidak langsung membocorkan pola. Klik tombol di bawah untuk membuka.", mutedStyle);
        step3BodyLabel.setWrap(true);
        step3Card.add(step3BodyLabel).left().growX().padBottom(6).row();
        step3UnlockBtn = smallHudButton("BUKA PETUNJUK LANGKAH 3", "112822", "34D399", () -> toggleStep3());
        step3Card.add(step3UnlockBtn).left();
        helpDrawer.add(step3Card).growX().padBottom(10).row();

        helpDrawer.add(closeButton("Tutup Bantuan", () -> setHelpDrawerOpen(false))).right();

        helpDrawer.setSize(372, 460);
        helpDrawer.setPosition(568, 68);
        helpDrawer.setVisible(false);

        // 5. Field Log Astronaut Modal [J] (10_field_log_and_mission_map.png)
        WindowStyle goldWinStyle = new WindowStyle(
            uiBoldFont, COL_TEXT_PRIMARY, createWindowDrawable("101723", "241C12", "F59E0B", 2, 30)
        );
        fieldLogWindow = new Window("  FIELD LOG ASTRONAUT [J]  |  CATATAN INSPEKSI OUTPOST", goldWinStyle);
        fieldLogWindow.setModal(true);
        fieldLogWindow.setMovable(true);
        fieldLogWindow.getTitleTable().padLeft(6).padTop(4);
        fieldLogWindow.padTop(38).padLeft(16).padRight(16).padBottom(14);

        fieldLogWindow.add(buildInfoCard(
            "PANEL SURYA HAB (OUTDOOR)",
            "Debu menutup 78% permukaan. Daya cadangan hanya cukup untuk maks 30 aksi awal.",
            marsStyle, secondaryStyle, "0C131D", "F59E0B"
        )).growX().padBottom(8).row();
        fieldLogWindow.add(buildInfoCard(
            "TANGKI AIR BIO-DOME BASE",
            "Jatah air tersedia: 2 unit per run. Jangan boros memanggil water() di petak tanah kosong.",
            cyanStyle, secondaryStyle, "0C131D", "38BDF8"
        )).growX().padBottom(8).row();
        fieldLogWindow.add(buildInfoCard(
            "SENSOR ROVER (M1-P2)",
            "Batu merah di (1, 2) memblokir jalur tengah. Putari lewat jalur selatan sebelum naik.",
            alertStyle, secondaryStyle, "0C131D", "F87171"
        )).growX().padBottom(8).row();
        fieldLogWindow.add(buildInfoCard(
            "PIPA TIMUR -> STASIUN POMPA UTAMA",
            "Terkunci hingga Misi 1 & 2 selesai. Terhubung ke Stasiun Pompa Utama (Misi 3).",
            mutedStyle, secondaryStyle, "0C131D", "64748B"
        )).growX().padBottom(12).row();

        Table flFooter = new Table();
        flFooter.add(new Label("Tekan [J] atau [Esc] untuk menutup Field Log.", mutedStyle)).left().expandX();
        flFooter.add(closeButton("Tutup Field Log [Esc]", () -> closeAuxOverlays())).right();
        fieldLogWindow.add(flFooter).growX();
        fieldLogWindow.setSize(620, 440);
        fieldLogWindow.setPosition(170, 80);
        fieldLogWindow.setVisible(false);

        // 6. Mission Map & Scores Modal [M] (10_field_log_and_mission_map.png · Puzzle 1-9)
        missionMapWindow = new Window("  PETA MISI OUTPOST & REKOR SKOR [M]  |  PUZZLE 1-9", cyanWinStyle);
        missionMapWindow.setModal(true);
        missionMapWindow.setMovable(true);
        missionMapWindow.getTitleTable().padLeft(6).padTop(4);
        missionMapWindow.padTop(38).padLeft(16).padRight(16).padBottom(14);

        missionMapWindow.add(new Label("MISI 1 · BIO-DOME DASAR (FASE DEMO P1-P3)", cyanStyle)).left().padBottom(4).row();
        Table m1Row = new Table();
        m1Row.add(missionCard("P1 · Uji Rover", "SELESAI", "Rekor: 6 aksi · 1 air", greenStyle, "34D399")).growX().padRight(6);
        m1Row.add(missionCard("P2 · Siram Bed", "AKTIF", "Target: <=12 aksi", cyanStyle, "38BDF8")).growX().padRight(6);
        m1Row.add(missionCard("P3 · Hindari Batu", "TERBUKA", "Rekor: Belum", marsStyle, "F59E0B")).growX();
        missionMapWindow.add(m1Row).growX().padBottom(10).row();

        missionMapWindow.add(new Label("MISI 2 · IRIGASI & SENSOR (P4-P6)", secondaryStyle)).left().padBottom(4).row();
        Table m2Row = new Table();
        m2Row.add(missionCard("P4 · Scan Tanaman", "TERBUKA (DEMO)", "Rekor: Belum", marsStyle, "F59E0B")).growX().padRight(6);
        m2Row.add(missionCard("P5 · Loop Irigasi", "TERKUNCI", "Selesaikan P4", mutedStyle, "475569")).growX().padRight(6);
        m2Row.add(missionCard("P6 · Jalur Ganda", "TERKUNCI", "Selesaikan P5", mutedStyle, "475569")).growX();
        missionMapWindow.add(m2Row).growX().padBottom(10).row();

        missionMapWindow.add(new Label("MISI 3 · STASIUN POMPA UTAMA (P7-P9)", mutedStyle)).left().padBottom(4).row();
        Table m3Row = new Table();
        m3Row.add(missionCard("P7 · Pipa Utama", "TERKUNCI", "Buka di Misi 3", mutedStyle, "475569")).growX().padRight(6);
        m3Row.add(missionCard("P8 · Katup Tekanan", "TERKUNCI", "Buka di Misi 3", mutedStyle, "475569")).growX().padRight(6);
        m3Row.add(missionCard("P9 · Pemulihan", "TERKUNCI", "Buka di Misi 3", mutedStyle, "475569")).growX();
        missionMapWindow.add(m3Row).growX().padBottom(12).row();

        Table mmFooter = new Table();
        mmFooter.add(new Label("Sinkronisasi rekor Spring Boot menunggu kontrak M-09.", mutedStyle)).left().expandX();
        mmFooter.add(closeButton("Tutup Peta Misi [Esc]", () -> closeAuxOverlays())).right();
        missionMapWindow.add(mmFooter).growX();
        missionMapWindow.setSize(680, 460);
        missionMapWindow.setPosition(140, 70);
        missionMapWindow.setVisible(false);

        // 7. Settings & Accessibility Modal [O] (11_settings_modal.png · UI-05)
        settingsWindow = new Window("  PENGATURAN SISTEM & AKSESIBILITAS [O]", cyanWinStyle);
        settingsWindow.setModal(true);
        settingsWindow.setMovable(true);
        settingsWindow.getTitleTable().padLeft(6).padTop(4);
        settingsWindow.padTop(38).padLeft(16).padRight(16).padBottom(14);

        settingsWindow.add(new Label("AUDIO & AMBIENCE PERMUKAAN MARS (PREVIEW UI)", cyanStyle)).left().padBottom(6).row();
        settingsWindow.add(audioSliderRow("Master Volume", "80%", 0.80f, primaryStyle, mutedStyle)).growX().padBottom(6).row();
        settingsWindow.add(audioSliderRow("SFX Rover & Pipa Air", "85%", 0.85f, primaryStyle, mutedStyle)).growX().padBottom(6).row();
        settingsWindow.add(audioSliderRow("Ambience Angin Mars", "55%", 0.55f, primaryStyle, mutedStyle)).growX().padBottom(12).row();

        settingsWindow.add(new Label("AKSESIBILITAS & VISUAL OUTPOST (UI-05)", greenStyle)).left().padBottom(6).row();

        Table rmRow = new Table();
        rmRow.setBackground(createBoxDrawable("0C131D", "334155", 1, 10, 10, 6, 6));
        rmRow.add(new Label("Reduced Motion (Matikan partikel debu & animasi kilau)", primaryStyle)).left().expandX();
        reducedMotionBtn = smallHudButton("OFF", "334155", "94A3B8", () -> {
            reducedMotion = !reducedMotion;
            refreshSettingsLabels();
        });
        rmRow.add(reducedMotionBtn).right();
        settingsWindow.add(rmRow).growX().padBottom(6).row();

        Table hcRow = new Table();
        hcRow.setBackground(createBoxDrawable("0C131D", "334155", 1, 10, 10, 6, 6));
        hcRow.add(new Label("High-Contrast Focus Ring (Bingkai fokus 2px cyan)", primaryStyle)).left().expandX();
        highContrastBtn = smallHudButton("ON", "065F46", "34D399", () -> {
            highContrastFocus = !highContrastFocus;
            refreshSettingsLabels();
        });
        hcRow.add(highContrastBtn).right();
        settingsWindow.add(hcRow).growX().padBottom(6).row();

        Table oaRow = new Table();
        oaRow.setBackground(createBoxDrawable("0C131D", "334155", 1, 10, 10, 6, 6));
        oaRow.add(new Label("Preview Dunia Outpost Mars (Before Mati vs After Pulih)", primaryStyle)).left().expandX();
        outpostStateSettingsBtn = smallHudButton("BEFORE (MATI)", "261C14", "F59E0B", () -> toggleBeforeAfter());
        oaRow.add(outpostStateSettingsBtn).right();
        settingsWindow.add(oaRow).growX().padBottom(10).row();

        settingsWindow.add(buildInfoCard(
            "AKUN SPRING BOOT: abdiel_01 (MODE PREVIEW LOKAL)",
            "Autentikasi & penyimpanan rekor ke server Spring Boot menunggu implementasi kontrak M-09.",
            marsStyle, mutedStyle, "0C131D", "334155"
        )).growX().padBottom(12).row();

        Table setFooter = new Table();
        setFooter.add(new Label("Tekan [O] atau [Esc] untuk menutup Pengaturan.", mutedStyle)).left().expandX();
        setFooter.add(closeButton("Tutup Pengaturan [Esc]", () -> closeAuxOverlays())).right();
        settingsWindow.add(setFooter).growX();
        settingsWindow.setSize(620, 456);
        settingsWindow.setPosition(170, 72);
        settingsWindow.setVisible(false);

        stage.addActor(inspection);
        stage.addActor(terminal);
        stage.addActor(helpDrawer);
        stage.addActor(fieldLogWindow);
        stage.addActor(missionMapWindow);
        stage.addActor(settingsWindow);
        inspection.setVisible(false);
        terminal.setVisible(false);

        // Stage keyboard listener for [B], [J], [M], [O] when not typing inside TextArea
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (stage.getKeyboardFocus() == editor && world.mode() == HabitatWorld.Mode.TERMINAL) {
                    return false;
                }
                if (keycode == Keys.B) {
                    toggleBeforeAfter();
                    return true;
                }
                if (keycode == Keys.J) {
                    toggleOverlay(AuxOverlay.FIELD_LOG);
                    return true;
                }
                if (keycode == Keys.M) {
                    toggleOverlay(AuxOverlay.MISSION_MAP);
                    return true;
                }
                if (keycode == Keys.O) {
                    toggleOverlay(AuxOverlay.SETTINGS);
                    return true;
                }
                return false;
            }
        });

        sync();
    }

    private void toggleBeforeAfter() {
        outpostRestored = !outpostRestored;
        refreshSettingsLabels();
        if (world.mode() == HabitatWorld.Mode.TERMINAL) {
            stage.setKeyboardFocus(editor);
        }
    }

    private void refreshSettingsLabels() {
        beforeAfterBtn.setText(outpostRestored ? "[B] AFTER" : "[B] BEFORE");
        outpostStateSettingsBtn.setText(outpostRestored ? "AFTER (PULIH)" : "BEFORE (MATI)");
        reducedMotionBtn.setText(reducedMotion ? "ON (AKTIF)" : "OFF");
        highContrastBtn.setText(highContrastFocus ? "ON (AKTIF)" : "OFF");
    }

    public void toggleOverlay(AuxOverlay target) {
        if (world.mode() != HabitatWorld.Mode.EXPLORING) {
            return;
        }
        activeOverlay = (activeOverlay == target) ? AuxOverlay.NONE : target;
        updateAuxOverlayVisibility();
    }

    public void closeAuxOverlays() {
        activeOverlay = AuxOverlay.NONE;
        updateAuxOverlayVisibility();
    }

    private void updateAuxOverlayVisibility() {
        boolean exploring = world.mode() == HabitatWorld.Mode.EXPLORING;
        fieldLogWindow.setVisible(exploring && activeOverlay == AuxOverlay.FIELD_LOG);
        missionMapWindow.setVisible(exploring && activeOverlay == AuxOverlay.MISSION_MAP);
        settingsWindow.setVisible(exploring && activeOverlay == AuxOverlay.SETTINGS);
        if (fieldLogWindow.isVisible()) fieldLogWindow.toFront();
        if (missionMapWindow.isVisible()) missionMapWindow.toFront();
        if (settingsWindow.isVisible()) settingsWindow.toFront();
    }

    public void setTerminalState(TerminalState state) {
        this.terminalState = state;
        switch (state) {
            case NORMAL -> {
                consoleBox.setBackground(consoleNormalBg);
                consoleTitleLabel.setText("OUTPUT TERMINAL (PREVIEW UI - CPYTHON BELUM AKTIF)");
                consoleTitleLabel.setColor(COL_CYAN);
                consoleDetailLabel.setText("Draf kode aman. Gunakan tombol Preview State di atas atau [? Bantuan 3-Langkah].");
            }
            case ERROR -> {
                consoleBox.setBackground(consoleErrorBg);
                consoleTitleLabel.setText("[!] BoundaryError pada baris 2: move()  (KODE TETAP AMAN)");
                consoleTitleLabel.setColor(COL_ALERT);
                consoleDetailLabel.setText("Rover mencapai tepi platform Bio-Dome di (3, 0). Saran: gunakan turn_left() sebelum move().");
            }
            case TIMEOUT -> {
                consoleBox.setBackground(consoleWarnBg);
                consoleTitleLabel.setText("[!] TIMEOUT 2.0s: Loop dihentikan oleh Watchdog  (KODE TETAP AMAN)");
                consoleTitleLabel.setColor(COL_MARS);
                consoleDetailLabel.setText("Eksekusi melampaui batas 2.0 detik. Periksa kondisi berhenti pada loop Anda.");
            }
            case STOPPED -> {
                consoleBox.setBackground(consoleNormalBg);
                consoleTitleLabel.setText("■ STOPPED: Percobaan dihentikan pengguna  (SOURCE TETAP UTUH)");
                consoleTitleLabel.setColor(COL_TEXT_SECONDARY);
                consoleDetailLabel.setText("Snapshot terakhir dipertahankan. Klik Reset atau lanjutkan edit kode di atas.");
            }
        }
        if (world.mode() == HabitatWorld.Mode.TERMINAL) {
            stage.setKeyboardFocus(editor);
        }
    }

    public void setHelpDrawerOpen(boolean open) {
        this.helpDrawerOpen = open;
        helpDrawer.setVisible(open && world.mode() == HabitatWorld.Mode.TERMINAL);
        if (helpDrawer.isVisible()) {
            helpDrawer.toFront();
        }
        if (world.mode() == HabitatWorld.Mode.TERMINAL) {
            stage.setKeyboardFocus(editor);
        }
    }

    private void toggleHelpDrawer() {
        setHelpDrawerOpen(!helpDrawerOpen);
    }

    private void toggleStep3() {
        step3Unlocked = !step3Unlocked;
        step3BodyLabel.setText(step3Unlocked
            ? "Pola hemat aksi: move(); move(); turn_left(); move(); water()"
            : "Terkunci agar tidak langsung membocorkan pola. Klik tombol di bawah untuk membuka.");
        step3UnlockBtn.setText(step3Unlocked ? "SEMBUNYIKAN LANGKAH 3" : "BUKA PETUNJUK LANGKAH 3");
        if (world.mode() == HabitatWorld.Mode.TERMINAL) {
            stage.setKeyboardFocus(editor);
        }
    }

    private Table missionCard(String title, String status, String record, LabelStyle titleStyle, String borderHex) {
        Table card = new Table();
        card.setBackground(createBoxDrawable("0C131D", borderHex, 1, 8, 8, 6, 6));
        card.add(new Label(title, titleStyle)).left().growX().padBottom(3).row();
        card.add(new Label("Status: " + status, new LabelStyle(uiFont, COL_TEXT_SECONDARY)))
            .left().growX().padBottom(2).row();
        card.add(new Label(record, new LabelStyle(uiFont, COL_TEXT_MUTED))).left().growX();
        return card;
    }

    private Table audioSliderRow(String label, String pct, float ratio, LabelStyle labelStyle, LabelStyle pctStyle) {
        Table row = new Table();
        row.setBackground(createBoxDrawable("0C131D", "243442", 1, 8, 8, 5, 5));
        row.add(new Label(label, labelStyle)).width(210).left();

        int barW = 260;
        int fillW = Math.round(barW * ratio);
        Table barTrack = new Table();
        barTrack.setBackground(createBoxDrawable("1E293B", "334155", 1, 0, 0, 0, 0));
        Table barFill = new Table();
        barFill.setBackground(createFlatDrawable("22D3EE", fillW, 8));
        barTrack.add(barFill).width(fillW).height(8).left().expandX();
        row.add(barTrack).width(barW).height(10).padRight(12);
        row.add(new Label(pct, pctStyle)).right();
        return row;
    }

    private Table buildInfoCard(String title, String body, LabelStyle titleStyle, LabelStyle bodyStyle,
                                String bgHex, String borderHex) {
        Table card = new Table();
        card.setBackground(createBoxDrawable(bgHex, borderHex, 1, 10, 10, 6, 6));
        card.add(new Label(title, titleStyle)).left().growX().padBottom(2).row();
        Label bodyLabel = new Label(body, bodyStyle);
        bodyLabel.setWrap(true);
        card.add(bodyLabel).left().growX();
        return card;
    }

    private TextButton smallHudButton(String text, String bgHex, String borderHex, Runnable onClick) {
        TextButtonStyle style = new TextButtonStyle(
            createBoxDrawable(bgHex, borderHex, 1, 7, 7, 2, 2),
            createBoxDrawable("0F293A", "38BDF8", 2, 7, 7, 2, 2),
            createBoxDrawable(bgHex, borderHex, 1, 7, 7, 2, 2),
            uiBoldFont
        );
        style.over = createBoxDrawable("1E3A5F", "BAE6FD", 1, 7, 7, 2, 2);
        style.fontColor = COL_TEXT_PRIMARY;
        style.overFontColor = Color.WHITE;
        TextButton btn = new TextButton(text, style);
        btn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onClick.run();
            }
        });
        return btn;
    }

    private TextButton closeButton(String label, Runnable onClose) {
        TextButtonStyle btnStyle = new TextButtonStyle(
            createBoxDrawable("1A2D3D", "38BDF8", 1, 10, 10, 4, 4),
            createBoxDrawable("0F293A", "38BDF8", 2, 10, 10, 4, 4),
            createBoxDrawable("1A2D3D", "38BDF8", 1, 10, 10, 4, 4),
            uiBoldFont
        );
        btnStyle.over = createBoxDrawable("233D52", "BAE6FD", 2, 10, 10, 4, 4);
        btnStyle.focused = createBoxDrawable("1F374B", "38BDF8", 2, 10, 10, 4, 4);
        btnStyle.fontColor = COL_TEXT_PRIMARY;
        btnStyle.overFontColor = Color.WHITE;
        btnStyle.downFontColor = COL_CYAN;
        btnStyle.focusedFontColor = Color.WHITE;

        TextButton button = new TextButton(label, btnStyle);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onClose.run();
            }
        });
        return button;
    }

    private BitmapFont loadHiDpiFont(String fntPath, String pngPath, float scale, BitmapFont fallback) {
        var fntHandle = Gdx.files.internal(fntPath);
        if (!fntHandle.exists()) fntHandle = Gdx.files.local("assets/" + fntPath);
        var pngHandle = Gdx.files.internal(pngPath);
        if (!pngHandle.exists()) pngHandle = Gdx.files.local("assets/" + pngPath);
        if (!fntHandle.exists() || !pngHandle.exists()) {
            return fallback;
        }
        Texture fontTex = new Texture(pngHandle, true);
        fontTex.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
        ownedTextures.add(fontTex);
        BitmapFont loaded = new BitmapFont(fntHandle, new TextureRegion(fontTex), false);
        loaded.setUseIntegerPositions(false);
        loaded.getData().setScale(scale);
        ownedFonts.add(loaded);
        return loaded;
    }

    private Drawable createFlatDrawable(String hex, int w, int h) {
        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.valueOf(hex));
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        pixmap.dispose();
        ownedTextures.add(texture);
        return new TextureRegionDrawable(new TextureRegion(texture));
    }

    private Drawable createBoxDrawable(String fillHex, String borderHex, int borderThickness,
                                       int padLeft, int padRight, int padTop, int padBottom) {
        // 3x supersampled sci-fi box texture so borders & rounded corner highlights stay silky crisp
        int scale = 3;
        int size = 14 * scale;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        Color fill = Color.valueOf(fillHex);
        Color border = Color.valueOf(borderHex);
        pixmap.setColor(0, 0, 0, 0);
        pixmap.fill();

        // Fill body with 2px chamfered corner cuts in 3x space
        pixmap.setColor(fill);
        pixmap.fillRectangle(1, 1, size - 2, size - 2);
        // Subtle top inner sheen
        pixmap.setColor(Math.min(1f, fill.r + 0.06f), Math.min(1f, fill.g + 0.08f), Math.min(1f, fill.b + 0.11f), 0.95f);
        pixmap.fillRectangle(scale, scale, size - scale * 2, scale);

        if (borderThickness > 0) {
            int t = borderThickness * scale;
            pixmap.setColor(border);
            for (int i = 0; i < t; i++) {
                pixmap.drawRectangle(i, i, size - i * 2, size - i * 2);
            }
        }
        Texture texture = new Texture(pixmap, true);
        texture.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
        pixmap.dispose();
        ownedTextures.add(texture);

        int split = Math.max(2, borderThickness + 2) * scale;
        NinePatch patch = new NinePatch(texture, split, split, split, split);
        patch.scale(1f / scale, 1f / scale);
        NinePatchDrawable drawable = new NinePatchDrawable(patch);
        drawable.setLeftWidth(padLeft);
        drawable.setRightWidth(padRight);
        drawable.setTopHeight(padTop);
        drawable.setBottomHeight(padBottom);
        return drawable;
    }

    private Drawable createWindowDrawable(String bodyHex, String headerHex, String borderHex,
                                          int borderThickness, int headerHeight) {
        int scale = 3;
        int w = 24 * scale;
        int h = (headerHeight + 16) * scale;
        int hh = headerHeight * scale;
        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        Color body = Color.valueOf(bodyHex);
        Color header = Color.valueOf(headerHex);
        Color border = Color.valueOf(borderHex);

        pixmap.setColor(body);
        pixmap.fill();
        pixmap.setColor(header);
        pixmap.fillRectangle(0, 0, w, hh);
        // Subtle top inner glass specular line
        pixmap.setColor(border.r, border.g, border.b, 0.35f);
        pixmap.fillRectangle(0, scale * borderThickness, w, scale);
        // Header divider line
        pixmap.setColor(border);
        pixmap.fillRectangle(0, hh - 2 * scale, w, 2 * scale);

        int t = borderThickness * scale;
        for (int i = 0; i < t; i++) {
            pixmap.drawRectangle(i, i, w - i * 2, h - i * 2);
        }
        Texture texture = new Texture(pixmap, true);
        texture.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
        pixmap.dispose();
        ownedTextures.add(texture);

        int sideSplit = Math.max(3, borderThickness + 2) * scale;
        NinePatch patch = new NinePatch(texture, sideSplit, sideSplit, (headerHeight + 2) * scale, sideSplit);
        patch.scale(1f / scale, 1f / scale);
        return new NinePatchDrawable(patch);
    }

    public Stage stage() {
        return stage;
    }

    public void sync() {
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
                if (helpDrawer.isVisible()) helpDrawer.toFront();
                stage.setKeyboardFocus(editor);
            }
            if (mode == HabitatWorld.Mode.INSPECTION) {
                inspection.toFront();
                stage.setKeyboardFocus(inspection);
            }
            shown = mode;
        }

        refreshSettingsLabels();
        if (mode == HabitatWorld.Mode.EXPLORING) {
            modeBadge.setText(activeOverlay == AuxOverlay.NONE ? "OUTPOST" : activeOverlay.name());
            modeBadge.setColor(COL_GREEN);
        } else if (mode == HabitatWorld.Mode.INSPECTION) {
            modeBadge.setText("INSPEKSI");
            modeBadge.setColor(COL_CYAN);
        } else {
            modeBadge.setText("TERMINAL");
            modeBadge.setColor(COL_MARS);
        }

        boolean editorFocused = stage.getKeyboardFocus() == editor;
        focusStatusLabel.setText(editorFocused ? "FOKUS: EDITOR" : "FOKUS: TERMINAL");
        focusStatusLabel.setColor(editorFocused ? COL_CYAN : COL_TEXT_SECONDARY);

        String text = editor.getText();
        int lines = 1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') lines++;
        }
        int displayLines = Math.max(8, lines);
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= displayLines; i++) {
            if (i > 1) sb.append('\n');
            sb.append(i);
        }
        lineNumbersLabel.setText(sb.toString());

        HabitatWorld.Facility nearby = world.nearbyFacility();
        if (mode != HabitatWorld.Mode.EXPLORING) {
            bottomPromptBox.setBackground(promptModalBg);
            prompt.setText("Panel aktif (gerak terkunci) | Tekan [Esc] untuk kembali");
        } else if (activeOverlay != AuxOverlay.NONE) {
            bottomPromptBox.setBackground(promptModalBg);
            prompt.setText("Overlay " + activeOverlay.name() + " aktif | Tekan [Esc] untuk tutup");
        } else if (nearby == null) {
            bottomPromptBox.setBackground(promptIdleBg);
            prompt.setText("Outpost Mars: dekati PANEL SURYA (kiri) atau TERMINAL (kanan).");
        } else {
            bottomPromptBox.setBackground(promptNearBg);
            prompt.setText("[E] " + (nearby == HabitatWorld.Facility.PANEL
                ? "Inspeksi Panel Surya & Air"
                : "Buka Floating Terminal Bio-Dome"));
        }
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        clampWindow(inspection);
        clampWindow(terminal);
        clampWindow(helpDrawer);
        clampWindow(fieldLogWindow);
        clampWindow(missionMapWindow);
        clampWindow(settingsWindow);
    }

    private void clampWindow(Window w) {
        w.setPosition(
            Math.max(0, Math.min(960 - w.getWidth(), w.getX())),
            Math.max(0, Math.min(600 - w.getHeight(), w.getY()))
        );
    }

    public void draw(float delta) {
        stage.getViewport().apply();
        terminal.setPosition(
            Math.max(8, Math.min(960 - terminal.getWidth() - 8, terminal.getX())),
            Math.max(36, Math.min(564 - terminal.getHeight(), terminal.getY()))
        );
        stage.act(Math.min(delta, 0.1f));
        stage.draw();
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
