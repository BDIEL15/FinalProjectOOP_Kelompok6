package com.marsreboot.habitat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
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
 * Scene2D HUD, inspection overlay, and floating draggable terminal placeholder.
 * No fake Run button or code execution; draft survives closing within the active screen.
 */
public final class HabitatUi implements Disposable {
    private static final Color COL_TEXT_PRIMARY = Color.valueOf("F1F5F9");
    private static final Color COL_TEXT_SECONDARY = Color.valueOf("CBD5E1");
    private static final Color COL_TEXT_MUTED = Color.valueOf("94A3B8");
    private static final Color COL_CYAN = Color.valueOf("67C5D6");
    private static final Color COL_MARS = Color.valueOf("CB795B");
    private static final Color COL_GREEN = Color.valueOf("8DBD75");
    private static final Color COL_ALERT = Color.valueOf("E06C53");

    private final HabitatWorld world;
    private final Stage stage = new Stage(new FitViewport(960, 600));
    private final Skin skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
    private final List<Texture> ownedTextures = new ArrayList<>();

    private final Label prompt;
    private final Label modeBadge;
    private final Label focusStatusLabel;
    private final Label lineNumbersLabel;
    private final Table bottomPromptBox;
    private final Drawable promptIdleBg;
    private final Drawable promptNearBg;
    private final Drawable promptModalBg;

    private final Window inspection;
    private final Window terminal;
    private final TextArea editor;
    private HabitatWorld.Mode shown;

    public HabitatUi(HabitatWorld world) {
        this.world = world;
        BitmapFont font = skin.getFont("default");
        font.setUseIntegerPositions(true);

        LabelStyle primaryStyle = new LabelStyle(font, COL_TEXT_PRIMARY);
        LabelStyle secondaryStyle = new LabelStyle(font, COL_TEXT_SECONDARY);
        LabelStyle mutedStyle = new LabelStyle(font, COL_TEXT_MUTED);
        LabelStyle cyanStyle = new LabelStyle(font, COL_CYAN);
        LabelStyle marsStyle = new LabelStyle(font, COL_MARS);
        LabelStyle greenStyle = new LabelStyle(font, COL_GREEN);
        LabelStyle alertStyle = new LabelStyle(font, COL_ALERT);

        promptIdleBg = createBoxDrawable("16222F", "334858", 1, 8, 8, 3, 3);
        promptNearBg = createBoxDrawable("102A3A", "67C5D6", 2, 8, 8, 3, 3);
        promptModalBg = createBoxDrawable("2A1B17", "CB795B", 1, 8, 8, 3, 3);

        prompt = new Label("", primaryStyle);
        modeBadge = new Label("MODE: EKSPLORASI", greenStyle);
        focusStatusLabel = new Label("FOKUS: EDITOR AKTIF", cyanStyle);
        lineNumbersLabel = new Label("1\n2\n3", mutedStyle);
        lineNumbersLabel.setAlignment(Align.topRight);

        // 1. Non-intrusive Top & Bottom HUD bars (leaves y=40..560 floor completely unobstructed)
        Table hud = new Table();
        hud.setFillParent(true);

        Table topBar = new Table();
        topBar.setBackground(createBoxDrawable("111A24", "2A3F54", 1, 10, 10, 4, 4));
        topBar.add(new Label("mars_reboot.py", primaryStyle)).left().padRight(8);

        Table sceneTag = new Table();
        sceneTag.setBackground(createBoxDrawable("182C3C", "67C5D6", 1, 6, 6, 2, 2));
        sceneTag.add(new Label("HABITAT | PLACEHOLDER", cyanStyle));
        topBar.add(sceneTag).left().padRight(12);

        topBar.add(new Label("Tujuan: Periksa Panel Air [E], lalu tinjau Terminal Robot [E]", secondaryStyle))
            .left().expandX();

        Table modeBox = new Table();
        modeBox.setBackground(createBoxDrawable("162432", "385268", 1, 8, 8, 2, 2));
        modeBox.add(modeBadge);
        topBar.add(modeBox).right();

        Table bottomBar = new Table();
        bottomBar.setBackground(createBoxDrawable("111A24", "2A3F54", 1, 10, 10, 4, 4));
        bottomPromptBox = new Table();
        bottomPromptBox.setBackground(promptIdleBg);
        bottomPromptBox.add(prompt).left();
        bottomBar.add(bottomPromptBox).left().expandX();

        Table controlsBox = new Table();
        controlsBox.setBackground(createBoxDrawable("16222F", "334858", 1, 8, 8, 2, 2));
        controlsBox.add(new Label("[WASD/Panah] Gerak   [E] Interaksi   [Esc] Tutup Panel", secondaryStyle));
        bottomBar.add(controlsBox).right().padLeft(10);

        hud.top();
        hud.add(topBar).growX().height(34).row();
        hud.add().expand().fill().row();
        hud.add(bottomBar).growX().height(34);
        stage.addActor(hud);

        // 2. Inspection Modal Window (Water Panel)
        WindowStyle inspectionWinStyle = new WindowStyle(
            font,
            COL_TEXT_PRIMARY,
            createWindowDrawable("14202C", "1C2E40", "67C5D6", 2, 30)
        );
        inspection = new Window("  INSPEKSI FASILITAS  |  PANEL AIR KEBUN", inspectionWinStyle);
        inspection.setModal(true);
        inspection.setMovable(false);
        inspection.getTitleTable().padLeft(6).padTop(4);
        inspection.padTop(38).padLeft(16).padRight(16).padBottom(14);

        Table inspMetaRow = new Table();
        Table inspTypeBadge = new Table();
        inspTypeBadge.setBackground(createBoxDrawable("102A3A", "67C5D6", 1, 8, 8, 2, 2));
        inspTypeBadge.add(new Label("STATUS FASILITAS (PLACEHOLDER)", cyanStyle));
        inspMetaRow.add(inspTypeBadge).left().expandX();

        Table inspFocusBadge = new Table();
        inspFocusBadge.setBackground(createBoxDrawable("1E2D3D", "67C5D6", 1, 8, 8, 2, 2));
        inspFocusBadge.add(new Label("FOKUS KEYBOARD: PANEL INSPEKSI [Esc]", primaryStyle));
        inspMetaRow.add(inspFocusBadge).right();
        inspection.add(inspMetaRow).growX().padBottom(10).row();

        inspection.add(buildInfoCard(
            "1. Aliran Air Kebun Belum Pulih  |  Kandidat Jatah: 2 Unit",
            "Persediaan air darurat terbatas. Menyiram petak yang sudah lembap tetap membuang air.",
            cyanStyle, secondaryStyle, "0F1A24", "67C5D6"
        )).growX().padBottom(8).row();

        inspection.add(buildInfoCard(
            "2. Kondisi Bed Hidroponik: 2 Petak Kering [!] & 2 Lembap [~]",
            "Periksa kondisi petak sebelum menyiram. Jatah air dan aturan puzzle final belum ditetapkan.",
            marsStyle, secondaryStyle, "0F1A24", "CB795B"
        )).growX().padBottom(8).row();

        inspection.add(buildInfoCard(
            "3. Catatan Sistem & Kandidat Sensor",
            "Kandidat API: needs_water(), water(), can_harvest(), harvest(). Ini informasi inspeksi, bukan evaluasi puzzle.",
            greenStyle, mutedStyle, "0F1A24", "385268"
        )).growX().padBottom(12).row();

        Table inspFooter = new Table();
        inspFooter.add(new Label("Gerak astronaut nonaktif selama panel terbuka.", mutedStyle)).left().expandX();
        inspFooter.add(closeButton()).right();
        inspection.add(inspFooter).growX();

        inspection.setSize(590, 384);
        inspection.setPosition(185, 108);

        // 3. Floating Draggable Terminal Window (The Farmer Was Replaced style: left side, world visible on right)
        WindowStyle terminalWinStyle = new WindowStyle(
            font,
            COL_TEXT_PRIMARY,
            createWindowDrawable("111A24", "1B2A3A", "CB795B", 2, 30)
        );
        terminal = new Window("  terminal.py  |  KONSOL ROBOT SEMENTARA (geser judul)", terminalWinStyle);
        terminal.setModal(true);
        terminal.setMovable(true);
        terminal.getTitleTable().padLeft(6).padTop(4);
        terminal.padTop(36).padLeft(14).padRight(14).padBottom(14);

        // Status & non-execution banner
        Table statusStrip = new Table();
        Table noExecBadge = new Table();
        noExecBadge.setBackground(createBoxDrawable("381A16", "E06C53", 1, 6, 6, 2, 2));
        noExecBadge.add(new Label("[!] Kode belum dieksekusi", alertStyle));
        statusStrip.add(noExecBadge).left().padRight(6);

        Table runtimeBadge = new Table();
        runtimeBadge.setBackground(createBoxDrawable("172330", "385268", 1, 6, 6, 2, 2));
        runtimeBadge.add(new Label("Robot/Python belum aktif", secondaryStyle));
        statusStrip.add(runtimeBadge).left().expandX();

        Table focusBadge = new Table();
        focusBadge.setBackground(createBoxDrawable("102A3A", "67C5D6", 1, 6, 6, 2, 2));
        focusBadge.add(focusStatusLabel);
        statusStrip.add(focusBadge).right();
        terminal.add(statusStrip).growX().padBottom(8).row();

        // Compact mission & API reference strip inside terminal
        Table briefBox = new Table();
        briefBox.setBackground(createBoxDrawable("16222F", "2A3F54", 1, 8, 8, 5, 5));
        Label briefTitle = new Label("Temuan Inspeksi: Aliran air kebun belum pulih (cek petak dulu).", primaryStyle);
        briefTitle.setWrap(true);
        briefBox.add(briefTitle).left().growX().row();
        Label briefApi = new Label("Kandidat API: move(dir) | can_harvest() | harvest() | needs_water() | water()", cyanStyle);
        briefApi.setWrap(true);
        briefBox.add(briefApi).left().growX().padTop(2);
        terminal.add(briefBox).growX().padBottom(8).row();

        // Editor file header bar
        Table editorHeader = new Table();
        editorHeader.setBackground(createBoxDrawable("182635", "334858", 1, 8, 8, 3, 3));
        editorHeader.add(new Label(">_ draf_solusi.py", cyanStyle)).left().expandX();
        editorHeader.add(new Label("Draf bertahan di sesi ini | Tab ditunda", mutedStyle)).right();
        terminal.add(editorHeader).growX().row();

        // Custom high-contrast TextArea style with crisp 2px cyan focus border
        TextFieldStyle editorStyle = new TextFieldStyle(
            font,
            COL_TEXT_PRIMARY,
            createFlatDrawable("67C5D6", 2, 16),
            createFlatDrawable("1E4E68", 4, 16),
            createBoxDrawable("0B1219", "2A3F54", 1, 8, 8, 6, 6)
        );
        editorStyle.focusedBackground = createBoxDrawable("0D1620", "67C5D6", 2, 8, 8, 6, 6);
        editorStyle.disabledBackground = createBoxDrawable("090E14", "1E293B", 1, 8, 8, 6, 6);
        editorStyle.focusedFontColor = COL_TEXT_PRIMARY;
        editorStyle.messageFont = font;
        editorStyle.messageFontColor = COL_TEXT_MUTED;

        editor = new TextArea("# Draf sementara\n# Kode belum dieksekusi.\n", editorStyle);
        editor.setFocusTraversal(false); // Tab editing is deliberately deferred; Esc always exits.
        editor.setCursorPosition(editor.getText().length());

        Table editorContainer = new Table();
        Table gutter = new Table();
        gutter.setBackground(createBoxDrawable("0E1720", "243442", 1, 6, 6, 6, 6));
        gutter.top().right();
        gutter.add(lineNumbersLabel).top().right();
        editorContainer.add(gutter).width(32).fillY();
        editorContainer.add(editor).grow().minHeight(195);
        terminal.add(editorContainer).grow().padBottom(8).row();

        // Passive console notice bar (honest non-execution state)
        Table consoleBox = new Table();
        consoleBox.setBackground(createBoxDrawable("0D141C", "243442", 1, 8, 8, 4, 4));
        Label consoleLabel = new Label(
            "STATUS: Belum ada tombol Run/evaluasi. Gerak astronaut nonaktif saat mengetik.",
            mutedStyle
        );
        consoleLabel.setWrap(true);
        consoleBox.add(consoleLabel).left().growX();
        terminal.add(consoleBox).growX().padBottom(10).row();

        // Terminal footer with drag hint & clear exit button
        Table termFooter = new Table();
        termFooter.add(new Label("Geser judul window | Kebun terlihat di kanan", secondaryStyle))
            .left().expandX();
        termFooter.add(closeButton()).right();
        terminal.add(termFooter).growX();

        // Default left position (36..592, 54..546) leaves right world area (x=600..920) clearly visible
        terminal.setSize(556, 492);
        terminal.setPosition(36, 54);

        stage.addActor(inspection);
        stage.addActor(terminal);
        inspection.setVisible(false);
        terminal.setVisible(false);
        sync();
    }

    private Table buildInfoCard(String title, String body, LabelStyle titleStyle, LabelStyle bodyStyle,
                                String bgHex, String borderHex) {
        Table card = new Table();
        card.setBackground(createBoxDrawable(bgHex, borderHex, 1, 10, 10, 7, 7));
        card.add(new Label(title, titleStyle)).left().growX().padBottom(3).row();
        Label bodyLabel = new Label(body, bodyStyle);
        bodyLabel.setWrap(true);
        card.add(bodyLabel).left().growX();
        return card;
    }

    private TextButton closeButton() {
        BitmapFont font = skin.getFont("default");
        TextButtonStyle btnStyle = new TextButtonStyle(
            createBoxDrawable("1A2D3D", "67C5D6", 1, 10, 10, 5, 5),
            createBoxDrawable("0F293A", "67C5D6", 2, 10, 10, 5, 5),
            createBoxDrawable("1A2D3D", "67C5D6", 1, 10, 10, 5, 5),
            font
        );
        btnStyle.over = createBoxDrawable("233D52", "BAE6FD", 2, 10, 10, 5, 5);
        btnStyle.focused = createBoxDrawable("1F374B", "67C5D6", 2, 10, 10, 5, 5);
        btnStyle.fontColor = COL_TEXT_PRIMARY;
        btnStyle.overFontColor = Color.WHITE;
        btnStyle.downFontColor = COL_CYAN;
        btnStyle.focusedFontColor = Color.WHITE;

        TextButton button = new TextButton("Kembali ke habitat [Esc]", btnStyle);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                world.closePanel();
            }
        });
        return button;
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
        int size = 12;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.valueOf(fillHex));
        pixmap.fill();
        if (borderThickness > 0) {
            pixmap.setColor(Color.valueOf(borderHex));
            for (int i = 0; i < borderThickness; i++) {
                pixmap.drawRectangle(i, i, size - i * 2, size - i * 2);
            }
        }
        Texture texture = new Texture(pixmap);
        texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        pixmap.dispose();
        ownedTextures.add(texture);

        int split = Math.max(2, borderThickness + 1);
        NinePatch patch = new NinePatch(texture, split, split, split, split);
        NinePatchDrawable drawable = new NinePatchDrawable(patch);
        drawable.setLeftWidth(padLeft);
        drawable.setRightWidth(padRight);
        drawable.setTopHeight(padTop);
        drawable.setBottomHeight(padBottom);
        return drawable;
    }

    private Drawable createWindowDrawable(String bodyHex, String headerHex, String borderHex,
                                          int borderThickness, int headerHeight) {
        int w = 24;
        int h = headerHeight + 16;
        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.valueOf(bodyHex));
        pixmap.fill();
        pixmap.setColor(Color.valueOf(headerHex));
        pixmap.fillRectangle(0, 0, w, headerHeight);
        pixmap.setColor(Color.valueOf(borderHex));
        pixmap.fillRectangle(0, headerHeight - 2, w, 2);
        for (int i = 0; i < borderThickness; i++) {
            pixmap.drawRectangle(i, i, w - i * 2, h - i * 2);
        }
        Texture texture = new Texture(pixmap);
        texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        pixmap.dispose();
        ownedTextures.add(texture);

        int sideSplit = Math.max(3, borderThickness + 1);
        NinePatch patch = new NinePatch(texture, sideSplit, sideSplit, headerHeight + 2, sideSplit);
        return new NinePatchDrawable(patch);
    }

    public Stage stage() {
        return stage;
    }

    public void sync() {
        HabitatWorld.Mode mode = world.mode();
        if (shown != mode) {
            stage.unfocusAll();
            inspection.setVisible(mode == HabitatWorld.Mode.INSPECTION);
            terminal.setVisible(mode == HabitatWorld.Mode.TERMINAL);
            if (mode == HabitatWorld.Mode.TERMINAL) {
                terminal.toFront();
                stage.setKeyboardFocus(editor);
            }
            if (mode == HabitatWorld.Mode.INSPECTION) {
                inspection.toFront();
                stage.setKeyboardFocus(inspection);
            }
            shown = mode;
        }

        // Update dynamic mode badge & focus indicator
        if (mode == HabitatWorld.Mode.EXPLORING) {
            modeBadge.setText("MODE: EKSPLORASI");
            modeBadge.setColor(COL_GREEN);
        } else if (mode == HabitatWorld.Mode.INSPECTION) {
            modeBadge.setText("MODE: INSPEKSI PANEL");
            modeBadge.setColor(COL_CYAN);
        } else {
            modeBadge.setText("MODE: TERMINAL EDITOR");
            modeBadge.setColor(COL_MARS);
        }

        boolean editorFocused = stage.getKeyboardFocus() == editor;
        focusStatusLabel.setText(editorFocused ? "FOKUS: EDITOR AKTIF" : "FOKUS: TERMINAL [KLIK EDITOR]");
        focusStatusLabel.setColor(editorFocused ? COL_CYAN : COL_TEXT_SECONDARY);

        // Update line numbers gutter to match editor line count
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
            prompt.setText("Panel fokus aktif (gerak astronaut nonaktif)  |  Tekan [Esc] untuk kembali ke habitat");
        } else if (nearby == null) {
            bottomPromptBox.setBackground(promptIdleBg);
            prompt.setText("Dekati PANEL AIR (kiri) atau TERMINAL ROBOT (kanan) untuk berinteraksi.");
        } else {
            bottomPromptBox.setBackground(promptNearBg);
            prompt.setText("[E] " + (nearby == HabitatWorld.Facility.PANEL
                ? "Periksa panel air (lihat status aliran & kondisi kebun)"
                : "Buka terminal sementara (tulis draf kode di terminal.py)"));
        }
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        // Fixed virtual canvas keeps both windows, text and exit control inside every aspect ratio.
        inspection.setPosition(
            Math.max(0, Math.min(960 - inspection.getWidth(), inspection.getX())),
            Math.max(0, Math.min(600 - inspection.getHeight(), inspection.getY()))
        );
        terminal.setPosition(
            Math.max(0, Math.min(960 - terminal.getWidth(), terminal.getX())),
            Math.max(0, Math.min(600 - terminal.getHeight(), terminal.getY()))
        );
    }

    public void draw(float delta) {
        stage.getViewport().apply();
        // Clamp window dragging inside safe virtual viewport bounds so controls never clip off-screen
        terminal.setPosition(
            Math.max(8, Math.min(960 - terminal.getWidth() - 8, terminal.getX())),
            Math.max(38, Math.min(562 - terminal.getHeight(), terminal.getY()))
        );
        stage.act(Math.min(delta, 0.1f));
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        for (Texture texture : ownedTextures) {
            texture.dispose();
        }
        ownedTextures.clear();
    }
}
