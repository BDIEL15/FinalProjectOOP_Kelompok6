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
 * Outdoor Mars Outpost 2D top-down 3/4 pixel-art renderer (MewnBase x Pathway style).
 * Renders open Martian regolith, Hab Airlock + Solar Array, Central Conduit Spine,
 * Open Hydroponic Bio-Dome Platform (with partial transparent dome ribs, water tank & Rover),
 * and Pump Station while keeping authoritative colliders in {@link HabitatWorld} 100% intact.
 */
public final class HabitatRenderer implements Disposable {
    private static final Color COL_SHADOW_LONG = new Color(0.11f, 0.04f, 0.03f, 0.62f);
    private static final Color COL_PERIMETER_ROCK = Color.valueOf("3D1910");
    private static final Color COL_PERIMETER_HI = Color.valueOf("8A3F2B");
    private static final Color COL_DECK_OUTER = Color.valueOf("283240");
    private static final Color COL_DECK_SURFACE = Color.valueOf("3A4656");
    private static final Color COL_DECK_RIM = Color.valueOf("94A3B8");
    private static final Color COL_PIPE_TRENCH = Color.valueOf("1B0E0B");
    private static final Color COL_PIPE_OFF = Color.valueOf("475569");
    private static final Color COL_WATER_CYAN = Color.valueOf("22D3EE");
    private static final Color COL_MARS_ACCENT = Color.valueOf("F59E0B");
    private static final Color COL_HYDRO_GREEN = Color.valueOf("34D399");
    private static final Color COL_BADGE_BG = new Color(0.06f, 0.09f, 0.14f, 0.92f);
    private static final Color COL_BADGE_ACTIVE_BG = new Color(0.08f, 0.20f, 0.29f, 0.96f);
    private static final Color COL_TEXT_MAIN = Color.valueOf("F8FAFC");
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
    private final Texture gardenDryTexture;
    private final Texture gardenRestoredTexture;
    private final Texture habAirlockTexture;
    private final Texture pumpStationTexture;
    private final Texture roverTexture;
    private final Texture astronautTexture;

    private float animTime;

    public HabitatRenderer() {
        marsSoilTile = loadNearest("habitat/mars_soil_tile.png", 32, 32, Color.valueOf("6B2E1E"));
        floorTile = loadNearest("habitat/floor_tile.png", 32, 32, Color.valueOf("7A3624"));
        partitionTexture = loadNearest("habitat/partition_wall.png", 32, 200, Color.valueOf("3B495B"));
        panelTexture = loadNearest("habitat/water_panel.png", 100, 40, COL_WATER_CYAN);
        terminalTexture = loadNearest("habitat/robot_terminal.png", 100, 52, COL_MARS_ACCENT);
        gardenDryTexture = loadNearest("habitat/hydro_garden.png", 180, 100, COL_MARS_ACCENT);
        gardenRestoredTexture = loadNearest("habitat/hydro_garden_restored.png", 180, 100, COL_HYDRO_GREEN);
        habAirlockTexture = loadNearest("habitat/hab_airlock.png", 144, 112, Color.valueOf("CBD5E1"));
        pumpStationTexture = loadNearest("habitat/pump_station.png", 96, 88, Color.valueOf("475569"));
        roverTexture = loadNearest("habitat/mars_rover.png", 36, 32, COL_MARS_ACCENT);
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
        boolean restored = HabitatUi.isOutpostRestored();
        boolean reducedMotion = HabitatUi.isReducedMotion();
        if (!reducedMotion) {
            animTime += Gdx.graphics.getDeltaTime();
        }
        float pulse = reducedMotion ? 1.0f : (0.82f + 0.18f * (float) Math.sin(animTime * 3.2f));
        HabitatWorld.Facility nearby = world.nearbyFacility();

        // 1. Tiled Outdoor Martian Regolith across entire canvas + Outpost Yard
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        for (int y = 0; y < (int) HabitatWorld.HEIGHT; y += 32) {
            for (int x = 0; x < (int) HabitatWorld.WIDTH; x += 32) {
                batch.draw(marsSoilTile, x, y, 32, 32);
            }
        }
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

        // 2. Outdoor Martian terrain details (sand dunes, craters, rocks with Pathway shadows, tire tracks, perimeter ridge)
        enableSafeAlphaBlend();
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        // Subtle sand dune bands across outpost ground
        shapes.setColor(0.36f, 0.14f, 0.09f, 0.32f);
        shapes.rect(44, 92, 420, 38);
        shapes.rect(520, 74, 380, 32);
        shapes.rect(180, 468, 520, 28);

        // Perimeter Martian canyon rock ridge & outpost boundary markers around FLOOR(40,40,880,520)
        shapes.setColor(COL_PERIMETER_ROCK);
        shapes.rect(32, 34, 896, 6);
        shapes.rect(32, 560, 896, 6);
        shapes.rect(32, 34, 8, 532);
        shapes.rect(920, 34, 8, 532);
        shapes.setColor(COL_PERIMETER_HI);
        for (int rx = 64; rx <= 880; rx += 96) {
            shapes.rect(rx, 558, 28, 4);
            shapes.rect(rx + 24, 36, 28, 4);
        }

        // Martian craters (outer rim + deep shadow pit)
        drawCrater(116, 102, 34, 16);
        drawCrater(392, 96, 28, 13);
        drawCrater(856, 88, 38, 17);
        drawCrater(436, 508, 26, 12);

        // Martian surface boulders with long directional shadows (Pathway style)
        drawMarsRock(72, 296, 18, 11);
        drawMarsRock(348, 240, 16, 10);
        drawMarsRock(548, 488, 20, 12);
        drawMarsRock(868, 312, 18, 11);

        // Rover tire tracks in Martian dust connecting Hab -> Conduit Spine -> Bio-Dome Platform
        shapes.setColor(0.26f, 0.10f, 0.06f, 0.55f);
        for (int tx = 140; tx <= 456; tx += 18) {
            shapes.rect(tx, 122, 10, 3);
            shapes.rect(tx, 132, 10, 3);
        }
        for (int tx = 516; tx <= 580; tx += 18) {
            shapes.rect(tx, 196, 10, 3);
            shapes.rect(tx, 206, 10, 3);
        }

        // 3. Outdoor Metallic Grated Walkways & Industrial Pipe Network
        // Walkway from Hab Airlock (top-left) -> Astronaut Spawn (160,200) -> Solar/Water Panel (240,400)
        drawWalkwayStrip(96, 360, 24, 86);
        drawWalkwayStrip(96, 360, 144, 20);
        // Walkways through north & south passages of Central Conduit Spine
        drawWalkwayStrip(456, 368, 80, 18);
        drawWalkwayStrip(456, 132, 80, 18);

        // Industrial Pipe Trenches & Conduits (Hab -> Panel -> Spine -> Bio-Dome & Terminal -> Pump Station)
        Color activePipeCol = restored ? COL_WATER_CYAN : COL_PIPE_OFF;
        Color bioPipeCol = restored ? COL_HYDRO_GREEN : Color.valueOf("64748B");

        // Pipe 1: Hab Airlock (168,476) -> Solar/Water Panel (290,420)
        drawPipeSegment(168, 472, 126, 10, activePipeCol, restored);
        drawPipeSegment(284, 436, 10, 44, activePipeCol, restored);

        // Pipe 2: Solar/Water Panel (290,400) -> Spine -> Terminal (770,400) -> Bio-Dome Platform (680,260)
        drawPipeSegment(284, 372, 10, 28, activePipeCol, restored);
        drawPipeSegment(284, 372, 492, 10, activePipeCol, restored);
        drawPipeSegment(766, 372, 10, 28, activePipeCol, restored);
        drawPipeSegment(664, 282, 10, 92, bioPipeCol, restored);

        // Pipe 3: Terminal (820,426) -> Pump Station (868,462)
        drawPipeSegment(818, 422, 56, 10, activePipeCol, restored);
        drawPipeSegment(864, 422, 10, 42, activePipeCol, restored);

        // 4. Open Hydroponic Bio-Dome Base Platform (Opsi A) around GARDEN(620, 160, 180, 100)
        // Visual raised metallic deck (574, 134, 288, 152) with long Pathway directional shadow
        shapes.setColor(COL_SHADOW_LONG);
        shapes.rect(588, 120, 292, 154);

        shapes.setColor(COL_DECK_OUTER);
        shapes.rect(574, 134, 288, 152);
        shapes.setColor(COL_DECK_SURFACE);
        shapes.rect(578, 138, 280, 144);
        shapes.setColor(COL_DECK_RIM);
        shapes.rect(574, 134, 288, 3);
        shapes.rect(574, 283, 288, 3);
        shapes.rect(574, 134, 3, 152);
        shapes.rect(859, 134, 3, 152);

        // Access steps on west & north sides of Bio-Dome deck
        shapes.setColor(Color.valueOf("64748B"));
        shapes.rect(562, 188, 12, 44);
        shapes.rect(690, 286, 48, 10);

        // Cylindrical Water Tank on left side of Bio-Dome deck (584, 156, 28, 96)
        shapes.setColor(Color.valueOf("1E293B"));
        shapes.rect(584, 156, 28, 96);
        shapes.setColor(Color.valueOf("94A3B8"));
        shapes.rect(586, 158, 24, 92);
        shapes.setColor(restored ? COL_WATER_CYAN : COL_MARS_ACCENT);
        shapes.rect(590, 166, 16, restored ? 74 : 36);

        // Rover Dock Pad on right side of Bio-Dome deck (808, 158, 46, 92)
        shapes.setColor(Color.valueOf("1E293B"));
        shapes.rect(808, 158, 46, 92);
        shapes.setColor(COL_MARS_ACCENT);
        shapes.rect(810, 160, 42, 2);
        shapes.rect(810, 246, 42, 2);

        // Solar Array beside Hab Airlock (52, 376, 86, 42) - Dusty vs Clean
        shapes.setColor(COL_SHADOW_LONG);
        shapes.rect(60, 368, 86, 42);
        shapes.setColor(Color.valueOf("1E293B"));
        shapes.rect(52, 376, 86, 42);
        shapes.setColor(restored ? Color.valueOf("2563EB") : Color.valueOf("78350F"));
        shapes.rect(56, 380, 78, 34);
        shapes.setColor(restored ? Color.valueOf("93C5FD") : Color.valueOf("B45309"));
        shapes.rect(80, 380, 2, 34);
        shapes.rect(108, 380, 2, 34);
        shapes.rect(56, 396, 78, 2);

        // Long directional shadows under Hab Airlock, Pump Station, Spine, Panel, Terminal
        shapes.setColor(COL_SHADOW_LONG);
        shapes.rect(60, 430, 132, 96);
        shapes.rect(836, 444, 80, 72);
        shapes.rect(HabitatWorld.PARTITION.x() + 10, HabitatWorld.PARTITION.y() - 8,
            HabitatWorld.PARTITION.width(), HabitatWorld.PARTITION.height());
        shapes.rect(HabitatWorld.Facility.PANEL.bounds.x() + 8, HabitatWorld.Facility.PANEL.bounds.y() - 6,
            HabitatWorld.Facility.PANEL.bounds.width(), HabitatWorld.Facility.PANEL.bounds.height());
        shapes.rect(HabitatWorld.Facility.TERMINAL.bounds.x() + 8, HabitatWorld.Facility.TERMINAL.bounds.y() - 6,
            HabitatWorld.Facility.TERMINAL.bounds.width(), HabitatWorld.Facility.TERMINAL.bounds.height());

        // 5. Pathway-inspired soft 2D point lighting
        drawSoftGlow(116, 476, 95, 65, restored ? COL_WATER_CYAN : COL_MARS_ACCENT, 0.12f * pulse);
        drawSoftGlow(290, 420, 96, 62, COL_WATER_CYAN, (nearby == HabitatWorld.Facility.PANEL ? 0.22f : 0.12f) * pulse);
        drawSoftGlow(770, 426, 104, 68, COL_MARS_ACCENT, (nearby == HabitatWorld.Facility.TERMINAL ? 0.22f : 0.12f) * pulse);
        drawSoftGlow(710, 210, 155, 92, restored ? COL_HYDRO_GREEN : COL_MARS_ACCENT, (restored ? 0.18f : 0.09f) * pulse);

        // Interactive facility brackets
        drawInteractionZone(HabitatWorld.Facility.PANEL.bounds, COL_WATER_CYAN,
            nearby == HabitatWorld.Facility.PANEL);
        drawInteractionZone(HabitatWorld.Facility.TERMINAL.bounds, COL_MARS_ACCENT,
            nearby == HabitatWorld.Facility.TERMINAL);

        // Position ring & connection line under astronaut
        float ax = world.x();
        float ay = world.y();
        shapes.setColor(0.22f, 0.83f, 0.93f, nearby != null ? 0.60f : 0.32f);
        shapes.rect(ax - 2, ay - 2, HabitatWorld.PLAYER_WIDTH + 4, 6);

        if (nearby != null && world.mode() == HabitatWorld.Mode.EXPLORING) {
            HabitatWorld.Box b = nearby.bounds;
            float cx = ax + HabitatWorld.PLAYER_WIDTH * 0.5f;
            float cy = ay + HabitatWorld.PLAYER_HEIGHT * 0.5f;
            float tx = Math.max(b.x(), Math.min(b.right(), cx));
            float ty = Math.max(b.y(), Math.min(b.top(), cy));
            shapes.setColor(COL_WATER_CYAN.r, COL_WATER_CYAN.g, COL_WATER_CYAN.b, 0.78f);
            shapes.rectLine(cx, cy, tx, ty, 2f);
        }
        shapes.end();

        // 6. Crisp 1:1 Pixel-Art Sprites
        batch.begin();
        // Outdoor decorative outpost modules (outside walking path or in corners)
        batch.draw(habAirlockTexture, 48, 436, 136, 104);
        batch.draw(pumpStationTexture, 826, 450, 84, 76);

        // Authoritative colliders
        batch.draw(partitionTexture,
            HabitatWorld.PARTITION.x(), HabitatWorld.PARTITION.y(),
            HabitatWorld.PARTITION.width(), HabitatWorld.PARTITION.height());
        batch.draw(restored ? gardenRestoredTexture : gardenDryTexture,
            HabitatWorld.GARDEN.x(), HabitatWorld.GARDEN.y(),
            HabitatWorld.GARDEN.width(), HabitatWorld.GARDEN.height());
        batch.draw(roverTexture, 813, 188, 36, 32);
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

        // 7. Partial Transparent Bio-Dome Ribs (drawn over garden so it reads as an Open Bio-Dome without hiding plants)
        enableSafeAlphaBlend();
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.73f, 0.90f, 0.99f, 0.28f);
        shapes.rect(584, 272, 268, 4);
        shapes.rect(584, 144, 268, 4);
        shapes.rect(658, 144, 3, 132);
        shapes.rect(762, 144, 3, 132);

        // Optional atmospheric Martian dust motes when Reduced Motion is off
        if (!reducedMotion) {
            shapes.setColor(0.98f, 0.75f, 0.55f, 0.30f);
            for (int i = 0; i < 8; i++) {
                float dx = (110 * i + animTime * (22 + i * 3)) % 860 + 48;
                float dy = (70 * i + 95) % 480 + 52;
                shapes.rect(dx, dy, 3, 2);
            }
        }
        shapes.end();

        // 8. High-contrast Outdoor Outpost World Badges
        drawWorldBadge("HAB AIRLOCK", 116, 544,
            restored ? COL_HYDRO_GREEN : COL_MARS_ACCENT, COL_TEXT_MAIN, false);
        drawWorldBadge(restored ? "SOLAR: 100% AKTIF" : "SOLAR: DEBU 78%", 95, 362,
            restored ? COL_WATER_CYAN : COL_MARS_ACCENT, COL_TEXT_MAIN, false);
        drawWorldBadge("POMPA UTAMA [M3]", 868, 536,
            Color.valueOf("64748B"), COL_TEXT_MUTED, false);

        boolean panelNear = nearby == HabitatWorld.Facility.PANEL;
        boolean terminalNear = nearby == HabitatWorld.Facility.TERMINAL;

        HabitatWorld.Box panelBox = HabitatWorld.Facility.PANEL.bounds;
        boolean astroAbovePanel = panelNear && ay >= panelBox.top() - 8;
        float panelLabelY = astroAbovePanel ? panelBox.y() - 18 : panelBox.top() + 18;
        drawWorldBadge(
            panelNear ? "[E] INSPEKSI PANEL SURYA & AIR" : "PANEL SURYA & AIR [E]",
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
            terminalNear ? "[E] BUKA TERMINAL LAPANGAN" : "TERMINAL BIO-DOME [E]",
            termBox.x() + termBox.width() * 0.5f,
            termLabelY,
            terminalNear ? COL_WATER_CYAN : COL_MARS_ACCENT,
            terminalNear ? COL_WATER_CYAN : COL_TEXT_MAIN,
            terminalNear
        );

        drawWorldBadge(
            restored
                ? "BIO-DOME BASE (4x4): PULIH & MENGALIR [OK]"
                : "BIO-DOME BASE (4x4): 2 Kering [!] | Rover Siaga",
            718,
            118,
            restored ? COL_HYDRO_GREEN : COL_MARS_ACCENT,
            COL_TEXT_MAIN,
            false
        );

        drawWorldBadge(
            "SPINE PIPA",
            HabitatWorld.PARTITION.x() + 16,
            HabitatWorld.PARTITION.y() - 16,
            Color.valueOf("475569"),
            COL_TEXT_MUTED,
            false
        );

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

    private void drawCrater(float cx, float cy, float rx, float ry) {
        shapes.setColor(0.58f, 0.25f, 0.16f, 0.65f);
        shapes.rect(cx - rx, cy - ry, rx * 2, ry * 2);
        shapes.setColor(0.28f, 0.11f, 0.07f, 0.72f);
        shapes.rect(cx - rx + 4, cy - ry + 3, rx * 2 - 8, ry * 2 - 6);
    }

    private void drawMarsRock(float x, float y, float w, float h) {
        shapes.setColor(COL_SHADOW_LONG);
        shapes.rect(x + 6, y - 4, w + 4, h);
        shapes.setColor(Color.valueOf("9A4830"));
        shapes.rect(x, y, w, h);
        shapes.setColor(Color.valueOf("B85D43"));
        shapes.rect(x + 2, y + h - 3, w - 4, 3);
    }

    private void drawWalkwayStrip(float x, float y, float w, float h) {
        shapes.setColor(Color.valueOf("263240"));
        shapes.rect(x, y, w, h);
        shapes.setColor(Color.valueOf("64748B"));
        shapes.rect(x, y, w, 2);
        shapes.rect(x, y + h - 2, w, 2);
    }

    private void drawPipeSegment(float x, float y, float w, float h, Color pipeCol, boolean glowing) {
        shapes.setColor(COL_PIPE_TRENCH);
        shapes.rect(x, y, w, h);
        shapes.setColor(pipeCol);
        if (w >= h) {
            shapes.rect(x + 2, y + 3, w - 4, h - 6);
            if (glowing) {
                shapes.setColor(0.80f, 0.98f, 1.0f, 0.85f);
                shapes.rect(x + 4, y + 4, w - 8, 2);
            }
        } else {
            shapes.rect(x + 3, y + 2, w - 6, h - 4);
            if (glowing) {
                shapes.setColor(0.80f, 0.98f, 1.0f, 0.85f);
                shapes.rect(x + 4, y + 4, 2, h - 8);
            }
        }
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
            shapes.setColor(accent.r, accent.g, accent.b, 0.18f);
            shapes.rect(x - 4, y - 4, w + 8, h + 8);
        }

        float alpha = active ? 0.95f : 0.50f;
        float len = active ? 14f : 10f;
        float thick = active ? 3f : 2f;
        shapes.setColor(accent.r, accent.g, accent.b, alpha);

        shapes.rect(x, y, len, thick);
        shapes.rect(x, y, thick, len);
        shapes.rect(x + w - len, y, len, thick);
        shapes.rect(x + w - thick, y, thick, len);
        shapes.rect(x, y + h - thick, len, thick);
        shapes.rect(x, y + h - len, thick, len);
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
        shapes.setColor(borderCol.r, borderCol.g, borderCol.b, active ? 0.95f : 0.72f);
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
        gardenDryTexture.dispose();
        gardenRestoredTexture.dispose();
        habAirlockTexture.dispose();
        pumpStationTexture.dispose();
        roverTexture.dispose();
        astronautTexture.dispose();
    }
}
