package com.marsreboot.habitat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Representative 2D top-down 3/4 pixel-art habitat renderer.
 * Reads authoritative state from {@link HabitatWorld}; never alters colliders or controls.
 */
public final class HabitatRenderer implements Disposable {
    private static final Color COL_HULL_SHADOW = new Color(0.05f, 0.07f, 0.10f, 0.65f);
    private static final Color COL_HULL_OUTER = Color.valueOf("0F172A");
    private static final Color COL_HULL_TOP = Color.valueOf("DCE4EC");
    private static final Color COL_HULL_BEVEL = Color.valueOf("9FB0C0");
    private static final Color COL_HULL_FASCIA = Color.valueOf("526475");
    private static final Color COL_FLOOR_SEAM = Color.valueOf("141E26");
    private static final Color COL_PIPE_TRENCH = Color.valueOf("0E1720");
    private static final Color COL_WATER_CYAN = Color.valueOf("67C5D6");
    private static final Color COL_MARS_ACCENT = Color.valueOf("CB795B");
    private static final Color COL_HYDRO_GREEN = Color.valueOf("8DBD75");
    private static final Color COL_BADGE_BG = new Color(0.06f, 0.10f, 0.15f, 0.92f);
    private static final Color COL_BADGE_ACTIVE_BG = new Color(0.08f, 0.20f, 0.28f, 0.96f);
    private static final Color COL_TEXT_MAIN = Color.valueOf("F1F5F9");
    private static final Color COL_TEXT_MUTED = Color.valueOf("94A3B8");

    private final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final GlyphLayout layout = new GlyphLayout();

    private final Texture marsSoilTile;
    private final Texture floorTile;
    private final Texture partitionTexture;
    private final Texture panelTexture;
    private final Texture terminalTexture;
    private final Texture gardenTexture;
    private final Texture astronautTexture;

    public HabitatRenderer() {
        marsSoilTile = loadNearest("habitat/mars_soil_tile.png", 32, 32, Color.valueOf("4A2016"));
        floorTile = loadNearest("habitat/floor_tile.png", 32, 32, Color.valueOf("1E2B36"));
        partitionTexture = loadNearest("habitat/partition_wall.png", 32, 200, Color.valueOf("9FB0C0"));
        panelTexture = loadNearest("habitat/water_panel.png", 100, 40, COL_WATER_CYAN);
        terminalTexture = loadNearest("habitat/robot_terminal.png", 100, 52, COL_MARS_ACCENT);
        gardenTexture = loadNearest("habitat/hydro_garden.png", 180, 100, COL_HYDRO_GREEN);
        astronautTexture = loadNearest("habitat/astronaut_eva.png", 24, 28, COL_TEXT_MAIN);
        font.setUseIntegerPositions(true);
    }

    private static Texture loadNearest(String path, int fallbackW, int fallbackH, Color fallbackColor) {
        FileHandle handle = Gdx.files.internal(path);
        if (!handle.exists()) {
            handle = Gdx.files.local("assets/" + path);
        }
        Texture texture;
        if (handle.exists()) {
            texture = new Texture(handle);
        } else {
            Pixmap pixmap = new Pixmap(fallbackW, fallbackH, Pixmap.Format.RGBA8888);
            pixmap.setColor(fallbackColor);
            pixmap.fill();
            texture = new Texture(pixmap);
            pixmap.dispose();
        }
        texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        return texture;
    }

    private static void enableSafeAlphaBlend() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        // Preserve destination framebuffer alpha = 1.0 so framebuffer PNG captures stay opaque
        Gdx.gl.glBlendFuncSeparate(
            GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA,
            GL20.GL_ONE, GL20.GL_ONE
        );
    }

    public void draw(HabitatWorld world, Viewport viewport) {
        viewport.apply();
        HabitatWorld.Facility nearby = world.nearbyFacility();

        // 1. Tiled Mars exterior & interior modular floor
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        for (int y = 0; y < (int) HabitatWorld.HEIGHT; y += 32) {
            for (int x = 0; x < (int) HabitatWorld.WIDTH; x += 32) {
                batch.draw(marsSoilTile, x, y, 32, 32);
            }
        }
        batch.end();

        // 2. 3/4 Habitat hull shell around FLOOR (40, 40, 880, 520)
        enableSafeAlphaBlend();
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        // Exterior hull drop shadow
        shapes.setColor(COL_HULL_SHADOW);
        shapes.rect(32, 30, 904, 536);

        // Outer hull structural shell
        shapes.setColor(COL_HULL_OUTER);
        shapes.rect(26, 34, 908, 532);
        shapes.setColor(COL_HULL_FASCIA);
        shapes.rect(28, 36, 904, 8);
        shapes.setColor(COL_HULL_TOP);
        shapes.rect(28, 42, 904, 522);
        shapes.setColor(COL_HULL_BEVEL);
        shapes.rect(36, 38, 888, 4);
        shapes.rect(36, 558, 888, 4);

        // Hull expansion joints along top/bottom walls
        shapes.setColor(COL_HULL_FASCIA);
        for (int hx = 96; hx <= 864; hx += 64) {
            shapes.rect(hx, 560, 2, 4);
            shapes.rect(hx, 36, 2, 6);
        }

        // Interior floor base fill before tile overlay
        shapes.setColor(COL_FLOOR_SEAM);
        shapes.rect(HabitatWorld.FLOOR.x(), HabitatWorld.FLOOR.y(),
            HabitatWorld.FLOOR.width(), HabitatWorld.FLOOR.height());
        shapes.end();

        // Draw crisp floor tiles inside FLOOR (40, 40, 880, 520)
        batch.begin();
        int floorRight = (int) HabitatWorld.FLOOR.right();
        int floorTop = (int) HabitatWorld.FLOOR.top();
        for (int y = (int) HabitatWorld.FLOOR.y(); y < floorTop; y += 32) {
            int drawH = Math.min(32, floorTop - y);
            for (int x = (int) HabitatWorld.FLOOR.x(); x < floorRight; x += 32) {
                int drawW = Math.min(32, floorRight - x);
                batch.draw(floorTile, x, y, drawW, drawH, 0, 0, drawW, drawH, false, false);
            }
        }
        batch.end();

        // 3. Floor conduits, walkways, Pathway-style soft 2D lighting, and interaction range rings
        enableSafeAlphaBlend();
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        // North interior wall bevel strip (non-colliding visual trim along y=552..560)
        shapes.setColor(0.08f, 0.12f, 0.16f, 0.55f);
        shapes.rect(40, 552, 880, 8);

        // Walkway guide strips at north & south passages around PARTITION (480, 160, 32, 200)
        shapes.setColor(0.20f, 0.29f, 0.36f, 0.45f);
        shapes.rect(468, 366, 56, 4);
        shapes.rect(468, 150, 56, 4);

        // Recessed floor conduit connecting Water Panel (290,400) -> Garden (660,260) & Terminal (770,400)
        shapes.setColor(COL_PIPE_TRENCH);
        shapes.rect(286, 372, 10, 28);
        shapes.rect(286, 372, 488, 10);
        shapes.rect(766, 372, 10, 28);
        shapes.rect(656, 260, 10, 114);

        // Active water pipe (cyan) and hydro nutrient line (green)
        shapes.setColor(COL_WATER_CYAN);
        shapes.rect(289, 375, 4, 25);
        shapes.rect(289, 375, 482, 4);
        shapes.rect(769, 375, 4, 25);
        shapes.setColor(COL_HYDRO_GREEN);
        shapes.rect(659, 260, 4, 115);

        // Pathway-inspired soft 2D point lighting (layered low-alpha pools)
        drawSoftGlow(290, 420, 96, 62, COL_WATER_CYAN, nearby == HabitatWorld.Facility.PANEL ? 0.20f : 0.11f);
        drawSoftGlow(770, 426, 100, 68, COL_MARS_ACCENT, nearby == HabitatWorld.Facility.TERMINAL ? 0.20f : 0.11f);
        drawSoftGlow(770, 426, 68, 46, COL_WATER_CYAN, nearby == HabitatWorld.Facility.TERMINAL ? 0.14f : 0.08f);
        drawSoftGlow(710, 210, 130, 80, COL_HYDRO_GREEN, 0.09f);

        // Interactive facility floor pads & corner brackets
        drawInteractionZone(HabitatWorld.Facility.PANEL.bounds, COL_WATER_CYAN,
            nearby == HabitatWorld.Facility.PANEL);
        drawInteractionZone(HabitatWorld.Facility.TERMINAL.bounds, COL_MARS_ACCENT,
            nearby == HabitatWorld.Facility.TERMINAL);

        // Contact shadows directly under solid colliders
        shapes.setColor(0.04f, 0.06f, 0.09f, 0.55f);
        shapes.rect(HabitatWorld.PARTITION.x() + 3, HabitatWorld.PARTITION.y() - 4,
            HabitatWorld.PARTITION.width(), HabitatWorld.PARTITION.height());
        shapes.rect(HabitatWorld.GARDEN.x() + 4, HabitatWorld.GARDEN.y() - 5,
            HabitatWorld.GARDEN.width(), HabitatWorld.GARDEN.height());
        shapes.rect(HabitatWorld.Facility.PANEL.bounds.x() + 3, HabitatWorld.Facility.PANEL.bounds.y() - 4,
            HabitatWorld.Facility.PANEL.bounds.width(), HabitatWorld.Facility.PANEL.bounds.height());
        shapes.rect(HabitatWorld.Facility.TERMINAL.bounds.x() + 3, HabitatWorld.Facility.TERMINAL.bounds.y() - 4,
            HabitatWorld.Facility.TERMINAL.bounds.width(), HabitatWorld.Facility.TERMINAL.bounds.height());

        // Subtle position ring under astronaut (exact bounds: world.x, world.y, 24, 28)
        float ax = world.x();
        float ay = world.y();
        shapes.setColor(0.40f, 0.77f, 0.84f, nearby != null ? 0.55f : 0.30f);
        shapes.rect(ax - 2, ay - 2, HabitatWorld.PLAYER_WIDTH + 4, 6);

        // Connection line when astronaut is within interaction range
        if (nearby != null && world.mode() == HabitatWorld.Mode.EXPLORING) {
            HabitatWorld.Box b = nearby.bounds;
            float cx = ax + HabitatWorld.PLAYER_WIDTH * 0.5f;
            float cy = ay + HabitatWorld.PLAYER_HEIGHT * 0.5f;
            float tx = Math.max(b.x(), Math.min(b.right(), cx));
            float ty = Math.max(b.y(), Math.min(b.top(), cy));
            shapes.setColor(COL_WATER_CYAN.r, COL_WATER_CYAN.g, COL_WATER_CYAN.b, 0.75f);
            shapes.rectLine(cx, cy, tx, ty, 2f);
        }
        shapes.end();

        // 4. Crisp 1:1 pixel-art sprites aligned with authoritative colliders
        batch.begin();
        batch.draw(partitionTexture,
            HabitatWorld.PARTITION.x(), HabitatWorld.PARTITION.y(),
            HabitatWorld.PARTITION.width(), HabitatWorld.PARTITION.height());
        batch.draw(gardenTexture,
            HabitatWorld.GARDEN.x(), HabitatWorld.GARDEN.y(),
            HabitatWorld.GARDEN.width(), HabitatWorld.GARDEN.height());
        batch.draw(panelTexture,
            HabitatWorld.Facility.PANEL.bounds.x(), HabitatWorld.Facility.PANEL.bounds.y(),
            HabitatWorld.Facility.PANEL.bounds.width(), HabitatWorld.Facility.PANEL.bounds.height());
        batch.draw(terminalTexture,
            HabitatWorld.Facility.TERMINAL.bounds.x(), HabitatWorld.Facility.TERMINAL.bounds.y(),
            HabitatWorld.Facility.TERMINAL.bounds.width(), HabitatWorld.Facility.TERMINAL.bounds.height());
        batch.draw(astronautTexture,
            ax, ay,
            HabitatWorld.PLAYER_WIDTH, HabitatWorld.PLAYER_HEIGHT);
        batch.end();

        // 5. High-contrast world badges & collision-free labels
        boolean panelNear = nearby == HabitatWorld.Facility.PANEL;
        boolean terminalNear = nearby == HabitatWorld.Facility.TERMINAL;

        HabitatWorld.Box panelBox = HabitatWorld.Facility.PANEL.bounds;
        boolean astroAbovePanel = panelNear && ay >= panelBox.top() - 8;
        float panelLabelY = astroAbovePanel ? panelBox.y() - 18 : panelBox.top() + 18;
        drawWorldBadge(
            panelNear ? "[E] PERIKSA PANEL AIR" : "PANEL AIR  [E]",
            panelBox.x() + panelBox.width() * 0.5f,
            panelLabelY,
            panelNear ? COL_WATER_CYAN : Color.valueOf("385268"),
            panelNear ? COL_WATER_CYAN : COL_TEXT_MAIN,
            panelNear
        );

        HabitatWorld.Box termBox = HabitatWorld.Facility.TERMINAL.bounds;
        boolean astroAboveTerminal = terminalNear && ay >= termBox.top() - 8;
        float termLabelY = astroAboveTerminal ? termBox.y() - 18 : termBox.top() + 18;
        drawWorldBadge(
            terminalNear ? "[E] BUKA TERMINAL (terminal.py)" : "TERMINAL ROBOT  [E]",
            termBox.x() + termBox.width() * 0.5f,
            termLabelY,
            terminalNear ? COL_WATER_CYAN : COL_MARS_ACCENT,
            terminalNear ? COL_WATER_CYAN : COL_TEXT_MAIN,
            terminalNear
        );

        drawWorldBadge(
            "KEBUN: 2 Kering [!] | 2 Lembap [~]",
            HabitatWorld.GARDEN.x() + 100,
            HabitatWorld.GARDEN.y() - 16,
            COL_HYDRO_GREEN,
            COL_TEXT_MAIN,
            false
        );

        drawWorldBadge(
            "SEKAT MODUL",
            HabitatWorld.PARTITION.x() + 16,
            HabitatWorld.PARTITION.y() - 16,
            Color.valueOf("475569"),
            COL_TEXT_MUTED,
            false
        );

        // Place astronaut badge on the side away from the nearby facility so labels never overlap
        float astroTagX = Math.max(84, Math.min(876, ax + HabitatWorld.PLAYER_WIDTH * 0.5f));
        float astroTagY;
        if (astroAbovePanel || astroAboveTerminal) {
            astroTagY = Math.min(546, ay + HabitatWorld.PLAYER_HEIGHT + 14);
        } else {
            astroTagY = ay >= 72 ? ay - 14 : ay + HabitatWorld.PLAYER_HEIGHT + 14;
        }
        drawWorldBadge(
            "ASTRONAUT",
            astroTagX,
            astroTagY,
            Color.valueOf("475569"),
            COL_TEXT_MAIN,
            false
        );

        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawSoftGlow(float cx, float cy, float rx, float ry, Color color, float maxAlpha) {
        int steps = 4;
        for (int i = steps; i >= 1; i--) {
            float scale = i / (float) steps;
            float w = rx * 2f * scale;
            float h = ry * 2f * scale;
            shapes.setColor(color.r, color.g, color.b, maxAlpha * (1.1f - scale * 0.75f) / steps * 1.8f);
            shapes.rect(cx - w * 0.5f, cy - h * 0.5f, w, h);
        }
    }

    private void drawInteractionZone(HabitatWorld.Box bounds, Color accent, boolean active) {
        float pad = 10f;
        float x = bounds.x() - pad;
        float y = bounds.y() - pad;
        float w = bounds.width() + pad * 2f;
        float h = bounds.height() + pad * 2f;

        if (active) {
            shapes.setColor(accent.r, accent.g, accent.b, 0.16f);
            shapes.rect(x - 4, y - 4, w + 8, h + 8);
        }

        float alpha = active ? 0.95f : 0.48f;
        float len = active ? 14f : 10f;
        float thick = active ? 3f : 2f;
        shapes.setColor(accent.r, accent.g, accent.b, alpha);

        // Bottom-left bracket
        shapes.rect(x, y, len, thick);
        shapes.rect(x, y, thick, len);
        // Bottom-right bracket
        shapes.rect(x + w - len, y, len, thick);
        shapes.rect(x + w - thick, y, thick, len);
        // Top-left bracket
        shapes.rect(x, y + h - thick, len, thick);
        shapes.rect(x, y + h - len, thick, len);
        // Top-right bracket
        shapes.rect(x + w - len, y + h - thick, len, thick);
        shapes.rect(x + w - thick, y + h - len, thick, len);
    }

    private void drawWorldBadge(String text, float centerX, float centerY, Color borderCol, Color textCol, boolean active) {
        layout.setText(font, text);
        float padX = 8f;
        float padY = 5f;
        float bw = layout.width + padX * 2f;
        float bh = layout.height + padY * 2f;
        float bx = Math.round(centerX - bw * 0.5f);
        float by = Math.round(centerY - bh * 0.5f);

        enableSafeAlphaBlend();
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(borderCol.r, borderCol.g, borderCol.b, active ? 0.95f : 0.70f);
        shapes.rect(bx - (active ? 2 : 1), by - (active ? 2 : 1), bw + (active ? 4 : 2), bh + (active ? 4 : 2));
        shapes.setColor(active ? COL_BADGE_ACTIVE_BG : COL_BADGE_BG);
        shapes.rect(bx, by, bw, bh);
        shapes.end();

        batch.begin();
        font.setColor(textCol);
        font.draw(batch, text, Math.round(bx + padX), Math.round(by + bh - padY));
        font.setColor(Color.WHITE);
        batch.end();
    }

    @Override
    public void dispose() {
        shapes.dispose();
        batch.dispose();
        font.dispose();
        marsSoilTile.dispose();
        floorTile.dispose();
        partitionTexture.dispose();
        panelTexture.dispose();
        terminalTexture.dispose();
        gardenTexture.dispose();
        astronautTexture.dispose();
    }
}
