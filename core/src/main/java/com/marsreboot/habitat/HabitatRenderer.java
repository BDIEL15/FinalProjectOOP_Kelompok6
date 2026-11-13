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
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;
import java.util.ArrayList;
import java.util.List;

/**
 * Outdoor Mars Outpost 2D top-down 3/4 pixel-art renderer (MewnBase x Pathway style).
 * Uses 2x crisp pixel-art sprites (TextureFilter.Nearest), smooth radial 2D lighting,
 * animated 4-direction astronaut sprite sheet, and Hi-DPI world badges while keeping
 * authoritative colliders in {@link HabitatWorld} 100% intact.
 */
public final class HabitatRenderer implements Disposable {
    private static final Color COL_PERIMETER_ROCK = Color.valueOf("3A160D");
    private static final Color COL_PERIMETER_HI = Color.valueOf("9B4529");
    private static final Color COL_PIPE_TRENCH = Color.valueOf("1B0D0A");
    private static final Color COL_PIPE_RIM = Color.valueOf("334155");
    private static final Color COL_PIPE_OFF = Color.valueOf("475569");
    private static final Color COL_WATER_CYAN = Color.valueOf("22D3EE");
    private static final Color COL_MARS_ACCENT = Color.valueOf("F59E0B");
    private static final Color COL_HYDRO_GREEN = Color.valueOf("34D399");
    private static final Color COL_BADGE_BG = new Color(0.05f, 0.08f, 0.13f, 0.92f);
    private static final Color COL_BADGE_ACTIVE_BG = new Color(0.06f, 0.19f, 0.28f, 0.96f);
    private static final Color COL_TEXT_MAIN = Color.valueOf("F8FAFC");
    private static final Color COL_TEXT_MUTED = Color.valueOf("94A3B8");

    private final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font;
    private final Texture fontTexture;
    private final GlyphLayout layout = new GlyphLayout();
    private final List<Texture> ownedTextures = new ArrayList<>();

    private final Texture[] marsSoilVariants = new Texture[4];
    private final Texture floorTile;
    private final Texture craterSm;
    private final Texture craterLg;
    private final Texture rockA;
    private final Texture rockB;
    private final Texture cargoCrate;
    private final Texture walkwayH;
    private final Texture walkwayV;

    private final Texture habAirlockTexture;
    private final Texture solarDustyTexture;
    private final Texture solarCleanTexture;
    private final Texture partitionOffTexture;
    private final Texture partitionOnTexture;
    private final Texture panelTexture;
    private final Texture terminalTexture;
    private final Texture pumpStationTexture;
    private final Texture bioDomeDeckOffTexture;
    private final Texture bioDomeDeckOnTexture;
    private final Texture bioDomeCanopyTexture;
    private final Texture gardenDryTexture;
    private final Texture gardenRestoredTexture;
    private final Texture roverTexture;
    private final TextureRegion[][] astronautFrames;
    private final Texture lightGlowTexture;

    private float animTime;
    private float prevAstroX = Float.NaN;
    private float prevAstroY = Float.NaN;
    private int astroDir = 0; // 0:Down, 1:Up, 2:Left, 3:Right
    private float walkTimer = 0f;
    private boolean isWalking = false;

    public HabitatRenderer() {
        for (int i = 0; i < 4; i++) {
            marsSoilVariants[i] = loadNearest("habitat/mars_soil_" + i + ".png", 64, 64, Color.valueOf("6B2E1E"));
        }
        floorTile = loadNearest("habitat/floor_tile.png", 64, 64, Color.valueOf("7A3624"));
        craterSm = loadNearest("habitat/mars_crater_sm.png", 64, 40, Color.valueOf("542013"));
        craterLg = loadNearest("habitat/mars_crater_lg.png", 96, 56, Color.valueOf("542013"));
        rockA = loadNearest("habitat/mars_rock_a.png", 44, 36, Color.valueOf("944026"));
        rockB = loadNearest("habitat/mars_rock_b.png", 36, 28, Color.valueOf("944026"));
        cargoCrate = loadNearest("habitat/cargo_crate.png", 32, 32, Color.valueOf("475569"));
        walkwayH = loadNearest("habitat/walkway_h.png", 64, 40, Color.valueOf("334155"));
        walkwayV = loadNearest("habitat/walkway_v.png", 44, 64, Color.valueOf("334155"));

        habAirlockTexture = loadNearest("habitat/hab_airlock.png", 288, 224, Color.valueOf("CBD5E1"));
        solarDustyTexture = loadNearest("habitat/solar_array_dusty.png", 176, 88, Color.valueOf("78350F"));
        solarCleanTexture = loadNearest("habitat/solar_array_clean.png", 176, 88, Color.valueOf("1D4ED8"));
        partitionOffTexture = loadNearest("habitat/partition_wall.png", 64, 400, Color.valueOf("3B495B"));
        partitionOnTexture = loadNearest("habitat/partition_wall_restored.png", 64, 400, COL_WATER_CYAN);
        panelTexture = loadNearest("habitat/water_panel.png", 200, 80, COL_WATER_CYAN);
        terminalTexture = loadNearest("habitat/robot_terminal.png", 200, 104, COL_MARS_ACCENT);
        pumpStationTexture = loadNearest("habitat/pump_station.png", 192, 176, Color.valueOf("475569"));
        bioDomeDeckOffTexture = loadNearest("habitat/biodome_deck.png", 576, 304, Color.valueOf("384556"));
        bioDomeDeckOnTexture = loadNearest("habitat/biodome_deck_restored.png", 576, 304, Color.valueOf("384556"));
        bioDomeCanopyTexture = loadNearest("habitat/biodome_canopy.png", 576, 304, new Color(0.2f, 0.75f, 0.97f, 0.25f));
        gardenDryTexture = loadNearest("habitat/hydro_garden.png", 360, 200, COL_MARS_ACCENT);
        gardenRestoredTexture = loadNearest("habitat/hydro_garden_restored.png", 360, 200, COL_HYDRO_GREEN);
        roverTexture = loadNearest("habitat/mars_rover.png", 72, 64, COL_MARS_ACCENT);

        Texture astroSheet = loadNearest("habitat/astronaut_eva_sheet.png", 192, 224, COL_TEXT_MAIN);
        astronautFrames = TextureRegion.split(astroSheet, 48, 56);

        lightGlowTexture = loadLinear("habitat/light_glow.png", 128, 128);

        FileHandle fntHandle = resolveAsset("ui/ui_sans_bold.fnt");
        FileHandle pngHandle = resolveAsset("ui/ui_sans_bold.png");
        if (fntHandle.exists() && pngHandle.exists()) {
            fontTexture = new Texture(pngHandle, true);
            fontTexture.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
            font = new BitmapFont(fntHandle, new TextureRegion(fontTexture), false);
            font.setUseIntegerPositions(false);
            font.getData().setScale(0.37f);
        } else {
            fontTexture = null;
            font = new BitmapFont();
            font.setUseIntegerPositions(true);
        }
    }

    private static FileHandle resolveAsset(String path) {
        FileHandle handle = Gdx.files.internal(path);
        if (!handle.exists()) {
            handle = Gdx.files.local("assets/" + path);
        }
        return handle;
    }

    private Texture loadNearest(String path, int fallbackW, int fallbackH, Color fallbackColor) {
        FileHandle handle = resolveAsset(path);
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
        ownedTextures.add(texture);
        return texture;
    }

    private Texture loadLinear(String path, int fallbackW, int fallbackH) {
        FileHandle handle = resolveAsset(path);
        Texture texture;
        if (handle.exists()) {
            texture = new Texture(handle, true);
            texture.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
        } else {
            Pixmap pixmap = new Pixmap(fallbackW, fallbackH, Pixmap.Format.RGBA8888);
            pixmap.setColor(1f, 1f, 1f, 0.25f);
            pixmap.fill();
            texture = new Texture(pixmap);
            pixmap.dispose();
            texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        }
        ownedTextures.add(texture);
        return texture;
    }

    private static void enableSafeAlphaBlend() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        // Preserve destination framebuffer alpha = 1.0 so PNG captures stay opaque
        Gdx.gl.glBlendFuncSeparate(
            GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA,
            GL20.GL_ONE, GL20.GL_ONE
        );
    }

    public void draw(HabitatWorld world, Viewport viewport) {
        viewport.apply();
        boolean restored = HabitatUi.isOutpostRestored();
        boolean reducedMotion = HabitatUi.isReducedMotion();
        float dt = Gdx.graphics.getDeltaTime();
        if (!reducedMotion) {
            animTime += dt;
        }
        float pulse = reducedMotion ? 1.0f : (0.85f + 0.15f * (float) Math.sin(animTime * 3.0f));
        HabitatWorld.Facility nearby = world.nearbyFacility();

        float ax = world.x();
        float ay = world.y();
        updateAstronautAnimation(ax, ay, dt);

        // 1. Multi-variant Tiled Outdoor Martian Regolith + Compacted Courtyard Dust
        batch.setColor(Color.WHITE);
        batch.setBlendFunctionSeparate(
            GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA,
            GL20.GL_ONE, GL20.GL_ONE
        );
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        for (int gy = 0; gy < (int) HabitatWorld.HEIGHT; gy += 32) {
            for (int gx = 0; gx < (int) HabitatWorld.WIDTH; gx += 32) {
                int variant = Math.floorMod((gx / 32) * 73856093 ^ (gy / 32) * 19349663, 4);
                batch.draw(marsSoilVariants[variant], gx, gy, 32, 32);
            }
        }
        int floorRight = (int) HabitatWorld.FLOOR.right();
        int floorTop = (int) HabitatWorld.FLOOR.top();
        for (int gy = (int) HabitatWorld.FLOOR.y(); gy < floorTop; gy += 32) {
            int drawH = Math.min(32, floorTop - gy);
            for (int gx = (int) HabitatWorld.FLOOR.x(); gx < floorRight; gx += 32) {
                int drawW = Math.min(32, floorRight - gx);
                batch.draw(floorTile, gx, gy, drawW, drawH, 0, 0, drawW * 2, drawH * 2, false, false);
            }
        }
        // Pixel-art Martian Craters on open regolith
        batch.draw(craterLg, 68, 78, 56, 32);
        batch.draw(craterSm, 360, 82, 36, 22);
        batch.draw(craterLg, 820, 68, 56, 32);
        batch.draw(craterSm, 408, 494, 36, 22);
        batch.end();

        // 2. Perimeter Canyon Rim, Tire Tracks, and Industrial Pipe Conduits
        enableSafeAlphaBlend();
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        // Canyon Perimeter Framing around walkable FLOOR(40,40,880,520)
        shapes.setColor(COL_PERIMETER_ROCK);
        shapes.rect(32, 33, 896, 7);
        shapes.rect(32, 560, 896, 7);
        shapes.rect(32, 33, 8, 534);
        shapes.rect(920, 33, 8, 534);
        shapes.setColor(COL_PERIMETER_HI);
        shapes.rect(38, 560, 884, 2);
        shapes.rect(38, 38, 884, 2);
        for (int rx = 64; rx <= 880; rx += 88) {
            shapes.rect(rx, 557, 24, 3);
            shapes.rect(rx + 28, 36, 24, 3);
        }

        // Rover tire tracks in Martian dust connecting Hab -> Conduit Spine -> Bio-Dome Platform
        shapes.setColor(0.24f, 0.09f, 0.05f, 0.52f);
        for (int tx = 132; tx <= 456; tx += 14) {
            shapes.rect(tx, 120, 8, 3);
            shapes.rect(tx, 130, 8, 3);
        }
        for (int tx = 518; tx <= 576; tx += 14) {
            shapes.rect(tx, 198, 8, 3);
            shapes.rect(tx, 208, 8, 3);
        }

        // Shaded Industrial Pipe Network connecting Hab -> Panel -> Spine -> Bio-Dome & Terminal -> Pump Station
        Color activePipeCol = restored ? COL_WATER_CYAN : COL_PIPE_OFF;
        Color bioPipeCol = restored ? COL_HYDRO_GREEN : Color.valueOf("64748B");
        drawPipeSegment(172, 470, 122, 12, activePipeCol, restored);
        drawPipeSegment(284, 438, 12, 44, activePipeCol, restored);
        drawPipeSegment(284, 372, 12, 28, activePipeCol, restored);
        drawPipeSegment(284, 372, 492, 12, activePipeCol, restored);
        drawPipeSegment(766, 372, 12, 28, activePipeCol, restored);
        drawPipeSegment(662, 282, 12, 92, bioPipeCol, restored);
        drawPipeSegment(816, 422, 58, 12, activePipeCol, restored);
        drawPipeSegment(862, 422, 12, 44, activePipeCol, restored);

        // Animated coolant pulses along pipe network when restored
        if (restored && !reducedMotion) {
            float flowX = 288 + ((animTime * 90f) % 480f);
            shapes.setColor(0.88f, 0.99f, 1.0f, 0.95f);
            shapes.rect(flowX, 375, 14, 6);
            float flowY = 372 - ((animTime * 60f) % 80f);
            shapes.rect(665, flowY, 6, 10);
        }
        shapes.end();

        // 3. Textured Grated Walkways & All 2x Crisp Pixel-Art Structures & Props
        batch.begin();
        // Walkways
        drawTiledWalkwayV(batch, 94, 360, 22, 82);
        drawTiledWalkwayH(batch, 94, 360, 148, 20);
        drawTiledWalkwayH(batch, 456, 366, 80, 20);
        drawTiledWalkwayH(batch, 456, 132, 80, 20);

        // Faceted Martian Surface Boulders (Pathway style)
        batch.draw(rockA, 68, 294, 22, 18);
        batch.draw(rockB, 344, 238, 18, 14);
        batch.draw(rockA, 544, 488, 22, 18);
        batch.draw(rockB, 868, 308, 18, 14);

        // Hab Airlock Dome + Cargo Crates + Dusty/Clean Solar Array
        batch.draw(habAirlockTexture, 48, 434, 144, 112);
        batch.draw(cargoCrate, 198, 492, 18, 18);
        batch.draw(cargoCrate, 202, 472, 16, 16);
        batch.draw(restored ? solarCleanTexture : solarDustyTexture, 52, 374, 88, 44);

        // Booster Pump Station (Mission 3)
        batch.draw(pumpStationTexture, 820, 444, 96, 88);

        // Raised Bio-Dome Base Platform (surrounds authoritative GARDEN)
        batch.draw(restored ? bioDomeDeckOnTexture : bioDomeDeckOffTexture, 574, 134, 288, 152);

        // Authoritative Colliders (1:1 aligned to HabitatWorld.Box)
        batch.draw(restored ? partitionOnTexture : partitionOffTexture,
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

        // Animated 4-Direction Astronaut Sprite aligned 1:1 to PLAYER_WIDTH x PLAYER_HEIGHT (24x28)
        int frameIdx = isWalking ? ((int) (walkTimer * 8f) % 4) : 0;
        TextureRegion astroRegion = astronautFrames[astroDir][frameIdx];
        batch.draw(astroRegion, ax, ay, HabitatWorld.PLAYER_WIDTH, HabitatWorld.PLAYER_HEIGHT);

        // Partial Transparent Bio-Dome Geodesic Canopy Overhang
        batch.draw(bioDomeCanopyTexture, 574, 134, 288, 152);
        batch.end();

        // 4. Smooth Additive Radial 2D Lighting (Pathway style) & Interactive Brackets
        batch.setBlendFunctionSeparate(
            GL20.GL_SRC_ALPHA, GL20.GL_ONE,
            GL20.GL_ZERO, GL20.GL_ONE
        );
        batch.begin();
        drawRadialGlow(batch, 120, 488, 170, 130, restored ? COL_WATER_CYAN : COL_MARS_ACCENT, 0.28f * pulse);
        drawRadialGlow(batch, 290, 420, 160, 110, COL_WATER_CYAN, (nearby == HabitatWorld.Facility.PANEL ? 0.45f : 0.24f) * pulse);
        drawRadialGlow(batch, 770, 426, 170, 120, COL_MARS_ACCENT, (nearby == HabitatWorld.Facility.TERMINAL ? 0.45f : 0.24f) * pulse);
        drawRadialGlow(batch, 718, 210, 290, 175, restored ? COL_HYDRO_GREEN : COL_MARS_ACCENT, (restored ? 0.34f : 0.16f) * pulse);
        drawRadialGlow(batch, ax + 12, ay + 14, 72, 72, COL_WATER_CYAN, nearby != null ? 0.30f : 0.16f);
        batch.end();
        batch.setColor(Color.WHITE);
        batch.setBlendFunctionSeparate(
            GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA,
            GL20.GL_ONE, GL20.GL_ONE
        );

        enableSafeAlphaBlend();
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        drawInteractionZone(HabitatWorld.Facility.PANEL.bounds, COL_WATER_CYAN,
            nearby == HabitatWorld.Facility.PANEL);
        drawInteractionZone(HabitatWorld.Facility.TERMINAL.bounds, COL_MARS_ACCENT,
            nearby == HabitatWorld.Facility.TERMINAL);

        // Ground interaction tether when astronaut is within reach
        if (nearby != null && world.mode() == HabitatWorld.Mode.EXPLORING) {
            HabitatWorld.Box b = nearby.bounds;
            float cx = ax + HabitatWorld.PLAYER_WIDTH * 0.5f;
            float cy = ay + HabitatWorld.PLAYER_HEIGHT * 0.5f;
            float tx = Math.max(b.x(), Math.min(b.right(), cx));
            float ty = Math.max(b.y(), Math.min(b.top(), cy));
            shapes.setColor(COL_WATER_CYAN.r, COL_WATER_CYAN.g, COL_WATER_CYAN.b, 0.80f);
            shapes.rectLine(cx, cy, tx, ty, 2f);
        }

        // Subtle Martian dust motes when Reduced Motion is off
        if (!reducedMotion) {
            shapes.setColor(0.98f, 0.78f, 0.60f, 0.28f);
            for (int i = 0; i < 9; i++) {
                float dx = (108 * i + animTime * (20 + i * 3)) % 860 + 48;
                float dy = (72 * i + 88) % 470 + 52;
                shapes.rect(dx, dy, 2, 2);
            }
        }
        shapes.end();

        // 5. Crisp Hi-DPI World Badges
        drawWorldBadge("HAB AIRLOCK", 120, 548,
            restored ? COL_HYDRO_GREEN : COL_MARS_ACCENT, COL_TEXT_MAIN, false);
        drawWorldBadge(restored ? "SOLAR: 100% AKTIF" : "SOLAR: DEBU 78%", 96, 358,
            restored ? COL_WATER_CYAN : COL_MARS_ACCENT, COL_TEXT_MAIN, false);
        drawWorldBadge("POMPA UTAMA [M3]", 868, 538,
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
            116,
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

    private void updateAstronautAnimation(float ax, float ay, float dt) {
        if (Float.isNaN(prevAstroX)) {
            prevAstroX = ax;
            prevAstroY = ay;
            return;
        }
        float dx = ax - prevAstroX;
        float dy = ay - prevAstroY;
        float distSq = dx * dx + dy * dy;
        if (distSq > 0.01f) {
            isWalking = true;
            walkTimer += Math.max(dt, 0.016f);
            if (Math.abs(dx) >= Math.abs(dy)) {
                astroDir = dx > 0 ? 3 : 2;
            } else {
                // Note: world Y grows upward in libGDX
                astroDir = dy > 0 ? 1 : 0;
            }
        } else {
            isWalking = false;
            walkTimer = 0f;
        }
        prevAstroX = ax;
        prevAstroY = ay;
    }

    private void drawTiledWalkwayH(SpriteBatch batch, float x, float y, float w, float h) {
        float seg = 32f;
        for (float cur = x; cur < x + w; cur += seg) {
            float dw = Math.min(seg, x + w - cur);
            batch.draw(walkwayH, cur, y, dw, h, 0, 0, Math.round(dw * 2), 40, false, false);
        }
    }

    private void drawTiledWalkwayV(SpriteBatch batch, float x, float y, float w, float h) {
        float seg = 32f;
        for (float cur = y; cur < y + h; cur += seg) {
            float dh = Math.min(seg, y + h - cur);
            batch.draw(walkwayV, x, cur, w, dh, 0, 0, 44, Math.round(dh * 2), false, false);
        }
    }

    private void drawPipeSegment(float x, float y, float w, float h, Color pipeCol, boolean glowing) {
        shapes.setColor(COL_PIPE_TRENCH);
        shapes.rect(x, y, w, h);
        shapes.setColor(COL_PIPE_RIM);
        shapes.rect(x + 1, y + 1, w - 2, h - 2);
        shapes.setColor(pipeCol);
        if (w >= h) {
            shapes.rect(x + 2, y + 3, w - 4, h - 6);
            shapes.setColor(glowing ? 0.78f : 0.45f, glowing ? 0.98f : 0.52f, glowing ? 1.0f : 0.60f, 0.80f);
            shapes.rect(x + 3, y + h - 5, w - 6, 2);
        } else {
            shapes.rect(x + 3, y + 2, w - 6, h - 4);
            shapes.setColor(glowing ? 0.78f : 0.45f, glowing ? 0.98f : 0.52f, glowing ? 1.0f : 0.60f, 0.80f);
            shapes.rect(x + 3, y + 3, 2, h - 6);
        }
    }

    private void drawRadialGlow(SpriteBatch batch, float cx, float cy, float w, float h, Color col, float alpha) {
        batch.setColor(col.r, col.g, col.b, alpha);
        batch.draw(lightGlowTexture, cx - w * 0.5f, cy - h * 0.5f, w, h);
        batch.setColor(Color.WHITE);
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

        float alpha = active ? 0.95f : 0.52f;
        float len = active ? 14f : 10f;
        float thick = active ? 2.5f : 1.8f;
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
        float padY = 4.5f;
        float bw = layout.width + padX * 2f;
        float bh = layout.height + padY * 2f;
        float bx = Math.round(centerX - bw * 0.5f);
        float by = Math.round(centerY - bh * 0.5f);

        enableSafeAlphaBlend();
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(borderCol.r, borderCol.g, borderCol.b, active ? 0.96f : 0.72f);
        shapes.rect(bx - (active ? 1.5f : 1f), by - (active ? 1.5f : 1f), bw + (active ? 3f : 2f), bh + (active ? 3f : 2f));
        shapes.setColor(active ? COL_BADGE_ACTIVE_BG : COL_BADGE_BG);
        shapes.rect(bx, by, bw, bh);
        shapes.end();

        batch.begin();
        font.setColor(textCol);
        font.draw(batch, text, bx + padX, by + bh - padY);
        font.setColor(Color.WHITE);
        batch.end();
    }

    @Override
    public void dispose() {
        shapes.dispose();
        batch.dispose();
        font.dispose();
        if (fontTexture != null) {
            fontTexture.dispose();
        }
        for (Texture texture : ownedTextures) {
            texture.dispose();
        }
        ownedTextures.clear();
    }
}
