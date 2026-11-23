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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.marsreboot.robot.RobotSimulation;
import com.marsreboot.runtime.RunCoordinator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Crisp 2x Pixel Art (TextureFilter.Nearest) renderer for the expanded 1680x1040 Mars Outpost
 * (MewnBase x Pathway aesthetic) combined with smooth presentation-layer animations:
 * - Crisp Martian regolith, craters, rocks, catwalks, Hab Airlock, Solar Wing, Conduit Spine & Pump Station
 * - Octagonal crisp pixel-art Bio-Dome conservatory + geodesic pixel canopy + hydroponic crop sprites
 * - Smooth rover glide/heading interpolation, pixel scanner beam, square pixel particles & additive 2D glow
 *   strictly reading deterministic snapshots from RunCoordinator.View
 */
public final class HabitatRenderer implements Disposable {
    private static final Color COL_CANYON_OUTER = Color.valueOf("2E1109");
    private static final Color COL_CANYON_RIM = Color.valueOf("9C472B");
    private static final Color COL_PIPE_DARK = Color.valueOf("111A24");
    private static final Color COL_PIPE_HI = Color.valueOf("475569");
    private static final Color COL_PIPE_OFF = Color.valueOf("334155");
    private static final Color COL_CYAN = Color.valueOf("22D3EE");
    private static final Color COL_AMBER = Color.valueOf("F59E0B");
    private static final Color COL_EMERALD = Color.valueOf("34D399");
    private static final Color COL_CORAL = Color.valueOf("F87171");
    private static final Color COL_BADGE_BG = new Color(0.05f, 0.08f, 0.14f, 0.90f);
    private static final Color COL_BADGE_ACTIVE = new Color(0.05f, 0.19f, 0.28f, 0.94f);
    private static final Color COL_TEXT_MAIN = Color.valueOf("F8FAFC");
    private static final Color COL_TEXT_MUTED = Color.valueOf("94A3B8");

    private static final class WorldParticle {
        float x, y, vx, vy, age, life, size;
        Color color = new Color();
    }

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
    private final Texture pumpStationTexture;
    private final Texture bioDomeDeckOffTexture;
    private final Texture bioDomeDeckOnTexture;
    private final Texture bioDomeCanopyTexture;
    private final Texture roverTexture;
    private final TextureRegion roverRegion;
    private final TextureRegion[][] astronautFrames;
    private final TextureRegion[] cropFrames; // 0:RIPE, 1:YOUNG, 2:EMPTY
    private final Texture lightGlowTexture;

    private final List<WorldParticle> particles = new ArrayList<>();
    private final Map<RobotSimulation.Position, RobotSimulation.Crop> prevCrops = new HashMap<>();
    private final float[] harvestPopTimer = new float[8];
    private final java.util.Set<RobotSimulation.Position> prevDry = new java.util.HashSet<>();
    private final float[] wetFlashTimer = new float[8];

    private float animTime;
    private int astroDir = 0; // 0:Down, 1:Up, 2:Left, 3:Right
    private float walkTimer = 0f;
    private float dustSpawnTimer = 0f;

    // Smooth Rover presentation state
    private long trackedGeneration = -1;
    private String trackedScenario = "";
    private float visualRoverCellX;
    private float visualRoverCellY;
    private float visualRoverAngleDeg = 0f;
    private float scanBeamTimer;
    private float beamFromX, beamFromY, beamToX, beamToY;
    private RunCoordinator.Status prevStatus = RunCoordinator.Status.READY;
    private float statusPulseTimer;
    private final Color statusPulseColor = new Color(COL_CYAN);
    private float harvestTagTimer;
    private float harvestTagX, harvestTagY;

    public HabitatRenderer() {
        for (int i = 0; i < 4; i++) {
            marsSoilVariants[i] = loadPixel("habitat/mars_soil_" + i + ".png", 64, 64, Color.valueOf("6B2E1E"));
        }
        floorTile = loadPixel("habitat/floor_tile.png", 64, 64, Color.valueOf("662D1D"));
        craterSm = loadPixel("habitat/mars_crater_sm.png", 64, 40, Color.valueOf("4A1E13"));
        craterLg = loadPixel("habitat/mars_crater_lg.png", 96, 56, Color.valueOf("4A1E13"));
        rockA = loadPixel("habitat/mars_rock_a.png", 44, 36, Color.valueOf("8C4029"));
        rockB = loadPixel("habitat/mars_rock_b.png", 36, 28, Color.valueOf("8C4029"));
        cargoCrate = loadPixel("habitat/cargo_crate.png", 32, 32, Color.valueOf("334155"));
        walkwayH = loadPixel("habitat/walkway_h.png", 64, 40, Color.valueOf("273549"));
        walkwayV = loadPixel("habitat/walkway_v.png", 44, 64, Color.valueOf("273549"));

        habAirlockTexture = loadPixel("habitat/hab_airlock.png", 288, 224, Color.valueOf("CBD5E1"));
        solarDustyTexture = loadPixel("habitat/solar_array_dusty.png", 176, 88, Color.valueOf("78350F"));
        solarCleanTexture = loadPixel("habitat/solar_array_clean.png", 176, 88, Color.valueOf("1D4ED8"));
        partitionOffTexture = loadPixel("habitat/partition_wall.png", 64, 400, Color.valueOf("3B495E"));
        partitionOnTexture = loadPixel("habitat/partition_wall_restored.png", 64, 400, COL_CYAN);
        panelTexture = loadPixel("habitat/water_panel.png", 200, 80, COL_CYAN);
        pumpStationTexture = loadPixel("habitat/pump_station.png", 192, 176, Color.valueOf("334155"));
        bioDomeDeckOffTexture = loadPixel("habitat/biodome_deck.png", 600, 390, Color.valueOf("1E293B"));
        bioDomeDeckOnTexture = loadPixel("habitat/biodome_deck_restored.png", 600, 390, Color.valueOf("1E293B"));
        bioDomeCanopyTexture = loadPixel("habitat/biodome_canopy.png", 600, 390, new Color(0.2f, 0.75f, 0.97f, 0.25f));

        roverTexture = loadPixel("habitat/mars_rover.png", 72, 64, COL_AMBER);
        roverRegion = new TextureRegion(roverTexture);

        Texture astroSheet = loadPixel("habitat/astronaut_eva_sheet.png", 192, 224, COL_TEXT_MAIN);
        astronautFrames = TextureRegion.split(astroSheet, 48, 56);

        Texture cropSheet = loadPixel("habitat/hydro_crops.png", 192, 64, COL_EMERALD);
        cropFrames = TextureRegion.split(cropSheet, 64, 64)[0];

        lightGlowTexture = loadSmooth("habitat/light_glow.png", 128, 128, Color.WHITE);

        FileHandle fntHandle = resolveAsset("ui/ui_sans_bold.fnt");
        FileHandle pngHandle = resolveAsset("ui/ui_sans_bold.png");
        if (fntHandle.exists() && pngHandle.exists()) {
            fontTexture = new Texture(pngHandle, true);
            fontTexture.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
            font = new BitmapFont(fntHandle, new TextureRegion(fontTexture), false);
            font.setUseIntegerPositions(false);
            font.getData().setScale(0.32f);
        } else {
            fontTexture = null;
            font = new BitmapFont();
            font.setUseIntegerPositions(true);
        }
    }

    public float visualRoverCellX() { return visualRoverCellX; }
    public float visualRoverCellY() { return visualRoverCellY; }

    public void draw(
        HabitatWorld world,
        Viewport viewport,
        RunCoordinator.View puzzle,
        float delta,
        boolean roverHovered,
        boolean panelHovered,
        FollowCamera camera
    ) {
        viewport.apply();
        boolean restored = HabitatUi.isOutpostRestored();
        boolean reducedMotion = HabitatUi.isReducedMotion();
        float dt = Float.isFinite(delta) && delta > 0f ? Math.min(delta, 0.1f) : 0f;
        if (!reducedMotion) {
            animTime += dt;
        }
        float pulse = reducedMotion ? 1.0f : (0.86f + 0.14f * (float) Math.sin(animTime * 2.8f));
        HabitatWorld.Facility nearby = world.nearbyFacility();

        updateAstronautAnimation(world, dt, reducedMotion);
        updateRoverPresentation(puzzle, dt, reducedMotion, camera);
        updateParticles(dt);

        // 1. Crisp 2x Pixel Martian Regolith + Courtyard Terrain across 1680x1040 (64x64 tiles)
        batch.setColor(Color.WHITE);
        batch.setBlendFunctionSeparate(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, GL20.GL_ONE, GL20.GL_ONE);
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        for (int gy = 0; gy < (int) HabitatWorld.HEIGHT; gy += 64) {
            for (int gx = 0; gx < (int) HabitatWorld.WIDTH; gx += 64) {
                int variant = Math.floorMod((gx / 64) * 73856093 ^ (gy / 64) * 19349663, 4);
                batch.draw(marsSoilVariants[variant], gx, gy, 64, 64);
            }
        }
        int floorRight = (int) HabitatWorld.FLOOR.right();
        int floorTop = (int) HabitatWorld.FLOOR.top();
        for (int gy = (int) HabitatWorld.FLOOR.y(); gy < floorTop; gy += 64) {
            int drawH = Math.min(64, floorTop - gy);
            for (int gx = (int) HabitatWorld.FLOOR.x(); gx < floorRight; gx += 64) {
                int drawW = Math.min(64, floorRight - gx);
                batch.draw(floorTile, gx, gy, drawW, drawH);
            }
        }

        // Crisp 2x Pixel Craters around perimeter & courtyard dunes
        batch.draw(craterLg, 92, 110, 96, 56);
        batch.draw(craterSm, 460, 130, 64, 40);
        batch.draw(craterLg, 790, 116, 96, 56);
        batch.draw(craterSm, 440, 845, 64, 40);
        batch.draw(craterLg, 1320, 118, 96, 56);
        batch.draw(craterSm, 1420, 560, 64, 40);
        batch.end();

        // 2. Stepped Pixel Canyon Rim, Rover Tire Marks & Industrial Coolant Conduits
        enableSafeAlphaBlend();
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        float fx = HabitatWorld.FLOOR.x();
        float fy = HabitatWorld.FLOOR.y();
        float fw = HabitatWorld.FLOOR.width();
        float fh = HabitatWorld.FLOOR.height();
        shapes.setColor(COL_CANYON_OUTER);
        shapes.rect(fx - 8f, fy - 8f, fw + 16f, 8f);
        shapes.rect(fx - 8f, fy + fh, fw + 16f, 8f);
        shapes.rect(fx - 8f, fy, 8f, fh);
        shapes.rect(fx + fw, fy, 8f, fh);
        shapes.setColor(COL_CANYON_RIM);
        shapes.rect(fx - 4f, fy - 4f, fw + 8f, 4f);
        shapes.rect(fx - 4f, fy + fh, fw + 8f, 4f);
        shapes.rect(fx - 4f, fy, 4f, fh);
        shapes.rect(fx + fw, fy, 4f, fh);

        // Pixel rover tire tracks curving from Courtyard toward Bio-Dome steps
        Color trackCol = new Color(0.24f, 0.10f, 0.06f, 0.50f);
        shapes.setColor(trackCol);
        for (int i = 0; i < 20; i++) {
            float tx = Math.round(290f + i * 30f);
            float ty = Math.round(320f - (float) Math.sin(i * 0.25f) * 30f);
            shapes.rect(tx, ty + 4f, 12f, 4f);
            shapes.rect(tx + 4f, ty - 6f, 12f, 4f);
        }

        // Stepped industrial conduits (pixel-aligned pipes with joints)
        boolean bioDomeLit = restored || puzzle.completed();
        Color mainPipeCol = restored ? COL_CYAN : COL_PIPE_OFF;
        Color bioPipeCol = bioDomeLit ? COL_EMERALD : Color.valueOf("64748B");

        drawPixelPipeH(250, 550, 558, 12, mainPipeCol, restored);
        drawPixelPipeV(558, 550, 680, 12, mainPipeCol, restored);
        drawPixelPipeH(260, 800, 558, 10, mainPipeCol, restored);
        drawPixelPipeV(558, 720, 800, 10, mainPipeCol, restored);
        drawPixelPipeH(558, 706, 672, 12, mainPipeCol, restored);
        drawPixelPipeV(672, 460, 706, 12, mainPipeCol, restored);
        drawPixelPipeH(672, 460, 864, 14, bioPipeCol, bioDomeLit);
        drawPixelPipeH(1394, 460, 1460, 12, mainPipeCol, restored);
        drawPixelPipeV(1460, 460, 780, 12, mainPipeCol, restored);

        // Pipe corner flange collars
        drawPipeFlange(558, 550);
        drawPipeFlange(558, 800);
        drawPipeFlange(558, 706);
        drawPipeFlange(672, 706);
        drawPipeFlange(672, 460);
        drawPipeFlange(1460, 460);

        // Animated square energy flow blocks along active conduit
        if ((restored || bioDomeLit) && !reducedMotion) {
            float flow = ((int) (animTime * 90f)) % 184;
            shapes.setColor(0.84f, 0.99f, 1f, 0.95f);
            shapes.rect(Math.round(676f + flow), 457f, 8f, 6f);
        }

        // Pixel navigation guide squares toward Bio-Dome entrance
        for (int b = 0; b < 5; b++) {
            float bx = 370f + b * 124f;
            float by = 290f + (b > 2 ? (b - 2) * 48f : 0f);
            float beaconPulse = reducedMotion ? 0.65f : (0.35f + 0.55f * Math.max(0f, (float) Math.sin(animTime * 3.5f - b * 0.7f)));
            shapes.setColor(COL_PIPE_DARK);
            shapes.rect(bx - 5f, by - 5f, 10f, 10f);
            shapes.setColor(COL_CYAN.r, COL_CYAN.g, COL_CYAN.b, beaconPulse);
            shapes.rect(bx - 3f, by - 3f, 6f, 6f);
        }
        shapes.end();

        // 3. Pixel Grated Walkways, Rocks, Crates, Outpost Structures & Bio-Dome
        batch.begin();
        batch.setColor(Color.WHITE);

        drawTiledWalkwayV(batch, 198, 355, 44, 130);
        drawTiledWalkwayH(batch, 250, 700, 260, 40);
        drawTiledWalkwayH(batch, 710, 442, 160, 40);
        drawTiledWalkwayV(batch, 1120, 620, 44, 140);

        batch.draw(rockA, 380, 240, 44, 36);
        batch.draw(rockB, 560, 250, 36, 28);
        batch.draw(rockA, 780, 740, 44, 36);
        batch.draw(rockB, 1440, 290, 36, 28);
        batch.draw(rockA, 920, 810, 44, 36);
        batch.draw(cargoCrate, 315, 485, 32, 32);
        batch.draw(cargoCrate, 350, 492, 32, 32);

        batch.draw(habAirlockTexture, HabitatWorld.HAB_AIRLOCK.x(), HabitatWorld.HAB_AIRLOCK.y(),
            HabitatWorld.HAB_AIRLOCK.width(), HabitatWorld.HAB_AIRLOCK.height());
        batch.draw(restored ? solarCleanTexture : solarDustyTexture,
            HabitatWorld.SOLAR_WING.x(), HabitatWorld.SOLAR_WING.y(),
            HabitatWorld.SOLAR_WING.width(), HabitatWorld.SOLAR_WING.height());

        batch.draw(restored ? partitionOnTexture : partitionOffTexture,
            HabitatWorld.PARTITION.x(), HabitatWorld.PARTITION.y(),
            HabitatWorld.PARTITION.width(), HabitatWorld.PARTITION.height());

        batch.draw(panelTexture,
            HabitatWorld.Facility.PANEL.bounds.x(), HabitatWorld.Facility.PANEL.bounds.y(),
            HabitatWorld.Facility.PANEL.bounds.width(), HabitatWorld.Facility.PANEL.bounds.height());

        batch.draw(pumpStationTexture,
            HabitatWorld.PUMP_STATION.x(), HabitatWorld.PUMP_STATION.y(),
            HabitatWorld.PUMP_STATION.width(), HabitatWorld.PUMP_STATION.height());

        // Crisp 2x Pixel Octagonal Bio-Dome Deck
        float domeX = HabitatWorld.GARDEN.x() - 120f;
        float domeY = HabitatWorld.GARDEN.y() - 95f;
        float domeW = HabitatWorld.GARDEN.width() + 240f; // 600
        float domeH = HabitatWorld.GARDEN.height() + 190f; // 390
        batch.draw(bioDomeLit ? bioDomeDeckOnTexture : bioDomeDeckOffTexture, domeX, domeY, domeW, domeH);

        // Pixel Dust Trail + Astronaut EVA Sprite (48x56 frames)
        batch.end();
        drawParticles();
        batch.begin();
        batch.setColor(Color.WHITE);
        int frameIdx = world.currentSpeed() > 12f ? ((int) (walkTimer * 9f) % 4) : 0;
        TextureRegion astroRegion = astronautFrames[astroDir][frameIdx];
        float drawAX = world.x() - 6f;
        float drawAY = world.y() - 4f;
        batch.draw(astroRegion, drawAX, drawAY, HabitatWorld.PLAYER_WIDTH + 12f, HabitatWorld.PLAYER_HEIGHT + 12f);

        // Geodesic Glass Rim Overlay
        batch.setColor(1f, 1f, 1f, 0.72f);
        batch.draw(bioDomeCanopyTexture, domeX, domeY, domeW, domeH);
        batch.setColor(Color.WHITE);
        batch.end();

        // 4. Pathway-style Soft Additive 2D Point Glows
        batch.setBlendFunctionSeparate(GL20.GL_SRC_ALPHA, GL20.GL_ONE, GL20.GL_ZERO, GL20.GL_ONE);
        batch.begin();
        drawRadialGlow(batch, HabitatWorld.HAB_AIRLOCK.centerX(), HabitatWorld.HAB_AIRLOCK.centerY(),
            260, 200, restored ? COL_EMERALD : COL_AMBER, 0.24f * pulse);
        drawRadialGlow(batch, HabitatWorld.Facility.PANEL.bounds.centerX(), HabitatWorld.Facility.PANEL.bounds.centerY(),
            210, 150, COL_CYAN, (nearby == HabitatWorld.Facility.PANEL || panelHovered ? 0.48f : 0.22f) * pulse);

        boolean p1Active = puzzle.status() == RunCoordinator.Status.PREPARING
            || puzzle.status() == RunCoordinator.Status.RUNNING
            || puzzle.status() == RunCoordinator.Status.REPLAYING;
        Color domeGlowCol = bioDomeLit ? COL_EMERALD : (p1Active ? COL_CYAN : COL_AMBER);
        drawRadialGlow(batch, HabitatWorld.GARDEN.centerX(), HabitatWorld.GARDEN.centerY(),
            520, 360, domeGlowCol, (bioDomeLit ? 0.34f : (p1Active ? 0.26f : 0.16f)) * pulse);

        drawRadialGlow(batch, world.x() + HabitatWorld.PLAYER_WIDTH * 0.5f,
            world.y() + HabitatWorld.PLAYER_HEIGHT * 0.5f, 110, 110, COL_CYAN, 0.18f);
        batch.end();
        batch.setColor(Color.WHITE);
        batch.setBlendFunctionSeparate(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, GL20.GL_ONE, GL20.GL_ONE);

        // 5. Pixel L-Corner Interaction Brackets & Ambient Martian Pixel Dust Motes
        enableSafeAlphaBlend();
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        if (nearby == HabitatWorld.Facility.PANEL || panelHovered) {
            drawPixelCornerBrackets(HabitatWorld.Facility.PANEL.bounds, COL_CYAN, 8f, 16f, 3f, 0.92f);
        }
        if (nearby == HabitatWorld.Facility.ROVER) {
            drawPixelCornerBrackets(HabitatWorld.GARDEN, COL_CYAN, 8f, 22f, 3f, 0.72f * pulse);
        }

        if (!reducedMotion) {
            shapes.setColor(0.98f, 0.78f, 0.62f, 0.28f);
            for (int i = 0; i < 16; i++) {
                float dx = Math.round(((180f * i + animTime * (18f + (i % 5) * 4f)) % (HabitatWorld.WIDTH - 120f)) + 60f);
                float dy = Math.round(((110f * i + 95f) % (HabitatWorld.HEIGHT - 120f)) + 60f);
                shapes.rect(dx, dy, 3f, 3f);
            }
        }
        shapes.end();

        // 6. Crisp Pixel Hydroponic Beds + Smooth Rover Presentation & Scanner FX
        drawBioDomePuzzle(puzzle, roverHovered, nearby == HabitatWorld.Facility.ROVER, world.mode(), pulse);

        // 7. Sleek Contextual World Tags
        drawWorldBadge("AIRLOCK HAB", HabitatWorld.HAB_AIRLOCK.centerX(), HabitatWorld.HAB_AIRLOCK.y() - 16f,
            restored ? COL_EMERALD : COL_AMBER, COL_TEXT_MAIN, false);

        if (nearby == HabitatWorld.Facility.PANEL || panelHovered) {
            drawWorldBadge("[E] / Klik Panel Telemetri",
                HabitatWorld.Facility.PANEL.bounds.centerX(),
                HabitatWorld.Facility.PANEL.bounds.top() + 18f,
                COL_CYAN, COL_CYAN, true);
        }

        if (puzzle.completed()) {
            drawWorldBadge(puzzle.puzzle().watering() ? "IRIGASI BIO-DOME SELESAI" : "PANEN BIO-DOME SELESAI", HabitatWorld.GARDEN.centerX(), HabitatWorld.GARDEN.top() + 34f,
                COL_EMERALD, COL_EMERALD, true);
        } else if (world.mode() == HabitatWorld.Mode.EXPLORING) {
            String bioTag = (roverHovered || nearby == HabitatWorld.Facility.ROVER)
                ? "Klik Rover / [E] Buka Editor"
                : "BIO-DOME P" + puzzle.puzzle().number() + " (" + puzzle.snapshot().remainingTargets().size() + "/" + puzzle.puzzle().targetCount() + " target)";
            drawWorldBadge(bioTag, HabitatWorld.GARDEN.centerX(), HabitatWorld.GARDEN.top() + 34f,
                (roverHovered || nearby == HabitatWorld.Facility.ROVER) ? COL_CYAN : COL_AMBER,
                (roverHovered || nearby == HabitatWorld.Facility.ROVER) ? COL_CYAN : COL_TEXT_MAIN,
                roverHovered || nearby == HabitatWorld.Facility.ROVER);
        }

        if (harvestTagTimer > 0f) {
            float alpha = Math.min(1f, harvestTagTimer * 2.2f);
            float liftY = (0.75f - harvestTagTimer) * 28f;
            drawWorldBadge("+1 PANEN", harvestTagX, harvestTagY + liftY,
                new Color(COL_EMERALD.r, COL_EMERALD.g, COL_EMERALD.b, alpha),
                new Color(COL_TEXT_MAIN.r, COL_TEXT_MAIN.g, COL_TEXT_MAIN.b, alpha), true);
        }

        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawBioDomePuzzle(
        RunCoordinator.View view,
        boolean roverHovered,
        boolean roverNearby,
        HabitatWorld.Mode mode,
        float pulse
    ) {
        int cols = view.puzzle().columns();
        boolean completed = view.completed();
        boolean running = view.status() == RunCoordinator.Status.PREPARING
            || view.status() == RunCoordinator.Status.RUNNING
            || view.status() == RunCoordinator.Status.REPLAYING;

        enableSafeAlphaBlend();
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        // Stepped crisp steel hydroponic bay frame inside Bio-Dome
        Color frameColor = completed ? COL_EMERALD : (running ? COL_CYAN : Color.valueOf("334155"));
        float gx = HabitatWorld.GARDEN.x(), gy = HabitatWorld.GARDEN.y();
        float gw = HabitatWorld.GARDEN.width(), gh = HabitatWorld.GARDEN.height();
        drawBevelRect(gx, gy, gw, gh, 6f, frameColor);
        drawBevelRect(gx + 3f, gy + 3f, gw - 6f, gh - 6f, 5f, Color.valueOf("0B131E"));

        // Stepped rail track connecting cell (0,0) and cell (1,0)
        HabitatWorld.Box c0 = HabitatWorld.hydroCellBox(0, 0, cols);
        HabitatWorld.Box c1 = HabitatWorld.hydroCellBox(1, 0, cols);
        shapes.setColor(Color.valueOf("1E293B"));
        shapes.rect(c0.centerX(), c0.centerY() - 5f, c1.centerX() - c0.centerX(), 10f);
        shapes.setColor(Color.valueOf("334155"));
        for (float rx = c0.centerX() + 12f; rx < c1.centerX() - 8f; rx += 14f) {
            shapes.rect(rx, c0.centerY() - 7f, 4f, 14f);
        }

        for (int cell = 0; cell < cols; cell++) {
            HabitatWorld.Box box = HabitatWorld.hydroCellBox(cell, 0, cols);
            boolean hasRoverHere = view.snapshot().robot().x() == cell;
            RobotSimulation.Crop crop = view.snapshot().crops()
                .getOrDefault(new RobotSimulation.Position(cell, 0), RobotSimulation.Crop.EMPTY);

            // Pixel-bevel planter tray frame + rich furrowed soil
            boolean dry = view.puzzle().watering() && view.snapshot().dryCells().contains(new RobotSimulation.Position(cell,0));
            Color podRim = view.puzzle().watering() ? (dry ? COL_AMBER : COL_CYAN) : hasRoverHere ? COL_CYAN
                : (crop == RobotSimulation.Crop.EMPTY ? COL_EMERALD : Color.valueOf("475569"));
            drawBevelRect(box.x(), box.y(), box.width(), box.height(), 4f, podRim);
            drawBevelRect(box.x() + 2f, box.y() + 2f, box.width() - 4f, box.height() - 4f, 3f, view.puzzle().watering() ? Color.valueOf(dry ? "4D2C17" : "153F4A") : Color.valueOf("21130E"));

            // Stepped furrow stripes across hydroponic bed
            shapes.setColor(crop == RobotSimulation.Crop.EMPTY ? Color.valueOf("160C09") : Color.valueOf("341D14"));
            for (int r = 0; r < 3; r++) {
                shapes.rect(box.x() + 10f, box.y() + 16f + r * 26f, box.width() - 20f, 8f);
            }

            if (view.puzzle().watering()) {
                if (dry) {
                    // Cracked parched soil: stepped zig-zag fissures + dust specks
                    shapes.setColor(Color.valueOf("1F1108"));
                    float cx = box.x() + 14f;
                    for (int s = 0; s < 6; s++) {
                        float zx = cx + s * 12f;
                        float zy = box.y() + 24f + (s % 2 == 0 ? 0f : 6f) + (s > 2 ? 18f : 0f);
                        shapes.rect(zx, zy, 8f, 3f);
                        shapes.rect(zx + (s % 2 == 0 ? 6f : 0f), zy + 3f, 3f, 8f);
                    }
                    shapes.setColor(Color.valueOf("B45309"));
                    shapes.rect(box.x() + 10f, box.y() + 12f, 4f, 4f);
                    shapes.rect(box.right() - 18f, box.y() + 40f, 4f, 4f);
                } else {
                    // Moist: cyan sheen rows + droplet pixels
                    shapes.setColor(COL_CYAN.r, COL_CYAN.g, COL_CYAN.b, 0.22f);
                    for (int r = 0; r < 3; r++) {
                        shapes.rect(box.x() + 10f, box.y() + 18f + r * 26f, box.width() - 20f, 3f);
                    }
                    shapes.setColor(Color.valueOf("A5F3FC"));
                    shapes.rect(box.x() + 16f, box.y() + 62f, 4f, 6f);
                    shapes.rect(box.right() - 22f, box.y() + 38f, 4f, 6f);
                }
                if (wetFlashTimer[Math.min(cell, wetFlashTimer.length - 1)] > 0f) {
                    float f = wetFlashTimer[Math.min(cell, wetFlashTimer.length - 1)] / 0.6f;
                    shapes.setColor(COL_CYAN.r, COL_CYAN.g, COL_CYAN.b, f * 0.45f);
                    shapes.rect(box.x(), box.y(), box.width(), box.height());
                }
            }

            // Left crisp Rover docking bay plate inside plot
            float padX = box.x() + 34f;
            float padY = box.y() + 50f;
            drawBevelRect(padX - 20f, padY - 18f, 40f, 36f, 4f,
                hasRoverHere ? new Color(COL_CYAN.r, COL_CYAN.g, COL_CYAN.b, 0.30f) : Color.valueOf("182434"));
            if (hasRoverHere) {
                drawPixelCornerBrackets(new HabitatWorld.Box(padX - 22f, padY - 20f, 44f, 40f), COL_CYAN, 0f, 7f, 2f, 0.9f);
            }

            // Right planter socket rim (pixel square bevel)
            float plantX = cols <= 2 ? box.x() + 90f : box.centerX();
            float plantY = cols <= 2 ? box.y() + 52f : box.y() + 80f;
            drawBevelRect(plantX - 22f, plantY - 22f, 44f, 44f, 5f, Color.valueOf("152232"));
            drawBevelRect(plantX - 18f, plantY - 18f, 36f, 36f, 4f,
                crop == RobotSimulation.Crop.EMPTY ? Color.valueOf("1A100C") : Color.valueOf("261610"));

            // Pixel harvest expansion square pulse
            if (harvestPopTimer[cell] > 0f) {
                float p = 1f - harvestPopTimer[cell] / 0.45f;
                float ringAlpha = 1f - p;
                float rad = 18f + p * 22f;
                drawSquareOutline(plantX - rad, plantY - rad, rad * 2f, rad * 2f, 3f,
                    new Color(COL_EMERALD.r, COL_EMERALD.g, COL_EMERALD.b, ringAlpha));
            }

            // Coordinate plate below bed
            drawBevelRect(box.centerX() - 26f, gy + 10f, 52f, 20f, 3f, Color.valueOf("08101A"));

            // Failure feedback: outline plots the run left undone (still dry / ripe and unharvested)
            boolean failedRun = view.status() == RunCoordinator.Status.INCOMPLETE
                || view.status() == RunCoordinator.Status.LIMIT_EXCEEDED;
            boolean undone = view.puzzle().watering() ? dry : crop == RobotSimulation.Crop.RIPE;
            if (failedRun && undone) {
                float a = 0.55f + 0.4f * pulse;
                drawSquareOutline(box.x() - 3f, box.y() - 3f, box.width() + 6f, box.height() + 6f, 3f,
                    new Color(COL_CORAL.r, COL_CORAL.g, COL_CORAL.b, a));
                drawPixelCornerBrackets(box, COL_CORAL, 6f, 10f, 3f, a);
            }
        }

        // Smooth Rover position in world coordinates
        HabitatWorld.Box roverBox = HabitatWorld.roverBayBox(visualRoverCellX, visualRoverCellY, cols);
        float roverCx = roverBox.centerX();
        float roverCy = roverBox.centerY();

        if (statusPulseTimer > 0f) {
            float prog = 1f - statusPulseTimer / 0.65f;
            float rad = 24f + prog * 30f;
            drawSquareOutline(roverCx - rad, roverCy - rad, rad * 2f, rad * 2f, 3f,
                new Color(statusPulseColor.r, statusPulseColor.g, statusPulseColor.b, (1f - prog) * 0.9f));
        }

        if ((roverHovered || roverNearby) && mode == HabitatWorld.Mode.EXPLORING) {
            float pad = 28f + 2f * pulse;
            HabitatWorld.Box hoverBox = new HabitatWorld.Box(roverCx - pad, roverCy - pad, pad * 2f, pad * 2f);
            shapes.setColor(COL_CYAN.r, COL_CYAN.g, COL_CYAN.b, roverHovered ? 0.20f : 0.10f);
            shapes.rect(hoverBox.x(), hoverBox.y(), hoverBox.width(), hoverBox.height());
            drawPixelCornerBrackets(hoverBox, COL_CYAN, 0f, 10f, 2.5f, roverHovered ? 0.95f : 0.72f);
        }

        // Pixel scanner harvest laser beam during replay
        if (scanBeamTimer > 0f) {
            float beamAlpha = Math.min(1f, scanBeamTimer / 0.22f);
            shapes.setColor(COL_EMERALD.r, COL_EMERALD.g, COL_EMERALD.b, beamAlpha * 0.48f);
            shapes.rectLine(beamFromX, beamFromY, beamToX, beamToY, 8f);
            shapes.setColor(0.85f, 1f, 0.96f, beamAlpha * 0.95f);
            shapes.rectLine(beamFromX, beamFromY, beamToX, beamToY, 3f);
            float spark = 8f + (1f - beamAlpha) * 8f;
            shapes.setColor(COL_EMERALD.r, COL_EMERALD.g, COL_EMERALD.b, beamAlpha * 0.85f);
            shapes.rect(beamToX - spark * 0.5f, beamToY - spark * 0.5f, spark, spark);
        }

        shapes.end();

        // Draw crisp 2x pixel crops + rotated East-facing pixel Mars Rover + coordinate labels
        batch.begin();
        batch.setColor(Color.WHITE);
        for (int cell = 0; cell < cols; cell++) {
            HabitatWorld.Box box = HabitatWorld.hydroCellBox(cell, 0, cols);
            RobotSimulation.Crop crop = view.snapshot().crops()
                .getOrDefault(new RobotSimulation.Position(cell, 0), RobotSimulation.Crop.EMPTY);
            float plantX = cols <= 2 ? box.x() + 90f : box.centerX();
            float plantY = cols <= 2 ? box.y() + 52f : box.y() + 80f;
            float breathe = HabitatUi.isReducedMotion() ? 1f : (1f + 0.03f * (float) Math.sin(animTime * 2.5f + cell));
            float popScale = harvestPopTimer[cell] > 0f ? (1f + harvestPopTimer[cell] * 0.40f) : 1f;
            float size = 48f * (crop == RobotSimulation.Crop.EMPTY ? 1f : breathe * popScale);
            TextureRegion cropSprite = crop == RobotSimulation.Crop.RIPE ? cropFrames[0]
                : (crop == RobotSimulation.Crop.YOUNG ? cropFrames[1] : cropFrames[2]);
            batch.draw(cropSprite, plantX - size * 0.5f, plantY - size * 0.5f, size, size);
        }

        float rW = 52f, rH = 46f;
        batch.draw(
            roverRegion,
            roverCx - rW * 0.5f, roverCy - rH * 0.5f,
            rW * 0.5f, rH * 0.5f,
            rW, rH,
            1f, 1f,
            visualRoverAngleDeg
        );

        for (int cell = 0; cell < cols; cell++) {
            HabitatWorld.Box box = HabitatWorld.hydroCellBox(cell, 0, cols);
            boolean hasRoverHere = view.snapshot().robot().x() == cell;
            String coord = "(" + cell + ",0)";
            layout.setText(font, coord);
            font.setColor(hasRoverHere ? COL_CYAN : COL_TEXT_MAIN);
            font.draw(batch, coord, box.centerX() - layout.width * 0.5f, gy + 26f);
            if (view.puzzle().watering()) {
                boolean dry = view.snapshot().dryCells().contains(new RobotSimulation.Position(cell,0));
                String moisture = dry ? "KERING" : "LEMBAP";
                layout.setText(font,moisture); font.setColor(dry ? COL_AMBER : COL_CYAN);
                font.draw(batch,moisture,box.centerX()-layout.width*0.5f,box.top()+18f);
            }
        }
        font.setColor(Color.WHITE);
        batch.end();
    }

    private void updateAstronautAnimation(HabitatWorld world, float dt, boolean reducedMotion) {
        float speed = world.currentSpeed();
        if (speed > 14f) {
            float speedFactor = Math.min(1.2f, speed / HabitatWorld.SPEED);
            walkTimer += dt * speedFactor;
            float fx = world.vx();
            float fy = world.vy();
            if (Math.abs(fx) >= Math.abs(fy)) {
                astroDir = fx > 0f ? 3 : 2;
            } else {
                astroDir = fy > 0f ? 1 : 0;
            }
            if (!reducedMotion) {
                dustSpawnTimer -= dt;
                if (dustSpawnTimer <= 0f) {
                    dustSpawnTimer = 0.085f;
                    spawnParticle(
                        world.x() + HabitatWorld.PLAYER_WIDTH * 0.5f + MathUtils.random(-4f, 4f),
                        world.y() + 4f,
                        -world.vx() * 0.14f + MathUtils.random(-8f, 8f),
                        MathUtils.random(4f, 14f),
                        0.38f,
                        5f,
                        new Color(0.84f, 0.55f, 0.38f, 0.52f)
                    );
                }
            }
        } else {
            walkTimer = 0f;
            dustSpawnTimer = 0f;
        }
    }

    private void updateRoverPresentation(
        RunCoordinator.View view,
        float dt,
        boolean reducedMotion,
        FollowCamera camera
    ) {
        var snap = view.snapshot();
        float targetX = snap.robot().x();
        float targetY = snap.robot().y();

        if (trackedGeneration != view.generation() || !trackedScenario.equals(view.scenarioId()) || view.status() == RunCoordinator.Status.READY) {
            boolean freshRun = trackedGeneration != view.generation() || !trackedScenario.equals(view.scenarioId());
            trackedGeneration = view.generation();
            trackedScenario = view.scenarioId();
            if (freshRun) {
                harvestTagTimer = 0f;
                java.util.Arrays.fill(harvestPopTimer, 0f);
                java.util.Arrays.fill(wetFlashTimer, 0f);
                prevDry.clear();
                prevDry.addAll(snap.dryCells());
                visualRoverCellX = targetX;
                visualRoverCellY = targetY;
                visualRoverAngleDeg = 0f;
                scanBeamTimer = 0f;
                statusPulseTimer = 0f;
                prevCrops.clear();
                prevCrops.putAll(snap.crops());
            }
        }

        float dx = targetX - visualRoverCellX;
        float dy = targetY - visualRoverCellY;
        if (reducedMotion || dt <= 0f) {
            visualRoverCellX = targetX;
            visualRoverCellY = targetY;
        } else {
            float alpha = 1f - (float) Math.exp(-11f * dt);
            visualRoverCellX += dx * alpha;
            visualRoverCellY += dy * alpha;
            if (Math.hypot(dx, dy) > 0.03f) {
                float desiredAngle = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;
                visualRoverAngleDeg = lerpAngleDeg(visualRoverAngleDeg, desiredAngle, 1f - (float) Math.exp(-14f * dt));
            } else {
                visualRoverAngleDeg = lerpAngleDeg(visualRoverAngleDeg, 0f, 1f - (float) Math.exp(-6f * dt));
            }
        }

        for (Map.Entry<RobotSimulation.Position, RobotSimulation.Crop> entry : snap.crops().entrySet()) {
            RobotSimulation.Position pos = entry.getKey();
            RobotSimulation.Crop cur = entry.getValue();
            RobotSimulation.Crop prev = prevCrops.get(pos);
            if (prev == RobotSimulation.Crop.RIPE && cur == RobotSimulation.Crop.EMPTY) {
                int idx = Math.max(0, Math.min(harvestPopTimer.length - 1, pos.x()));
                harvestPopTimer[idx] = 0.45f;
                SoundBank.play(SoundBank.Sfx.HARVEST);
                HabitatWorld.Box pod = HabitatWorld.hydroCellBox(pos.x(), pos.y(), 2);
                HabitatWorld.Box rover = HabitatWorld.roverBayBox(visualRoverCellX, visualRoverCellY, view.puzzle().columns());
                beamFromX = rover.centerX() + 14f;
                beamFromY = rover.centerY();
                beamToX = view.puzzle().columns() <= 2 ? pod.x() + 90f : pod.centerX();
                beamToY = view.puzzle().columns() <= 2 ? pod.y() + 52f : pod.y() + 80f;
                scanBeamTimer = 0.36f;
                harvestTagTimer = 0.75f;
                harvestTagX = beamToX;
                harvestTagY = beamToY + 26f;
                if (camera != null && !reducedMotion) {
                    camera.addTrauma(0.16f);
                }
                if (!reducedMotion) {
                    for (int p = 0; p < 10; p++) {
                        float ang = p * (MathUtils.PI2 / 10f) + MathUtils.random(-0.2f, 0.2f);
                        float spd = MathUtils.random(22f, 48f);
                        spawnParticle(
                            beamToX, beamToY,
                            MathUtils.cos(ang) * spd, MathUtils.sin(ang) * spd + 12f,
                            0.52f, 5f,
                            p % 3 == 0 ? COL_AMBER : COL_EMERALD
                        );
                    }
                }
            }
        }
        prevCrops.clear();
        prevCrops.putAll(snap.crops());

        // Dry -> wet transition: splash burst + flash on that plot
        for (RobotSimulation.Position p : prevDry) {
            if (!snap.dryCells().contains(p)) {
                int idx = Math.max(0, Math.min(wetFlashTimer.length - 1, p.x()));
                wetFlashTimer[idx] = 0.6f;
                SoundBank.play(SoundBank.Sfx.WATER);
                if (!reducedMotion) {
                    HabitatWorld.Box pod = HabitatWorld.hydroCellBox(p.x(), p.y(), view.puzzle().columns());
                    for (int k = 0; k < 8; k++) {
                        float ang = MathUtils.PI * (0.15f + 0.7f * k / 7f);
                        float spd = MathUtils.random(26f, 52f);
                        spawnParticle(pod.centerX(), pod.y() + 70f,
                            MathUtils.cos(ang) * spd * 0.6f, MathUtils.sin(ang) * spd,
                            0.5f, 4f, k % 2 == 0 ? COL_CYAN : Color.valueOf("A5F3FC"));
                    }
                }
            }
        }
        prevDry.clear();
        prevDry.addAll(snap.dryCells());

        if (view.status() != prevStatus) {
            prevStatus = view.status();
            if (view.status() == RunCoordinator.Status.SUCCESS) {
                statusPulseColor.set(COL_EMERALD);
                statusPulseTimer = 0.65f;
                SoundBank.play(SoundBank.Sfx.SUCCESS);
                if (!reducedMotion) {
                    // Restoration burst: emerald/cyan sparks rising from the whole Bio-Dome bed
                    HabitatWorld.Box g = HabitatWorld.GARDEN;
                    for (int k = 0; k < 28; k++) {
                        spawnParticle(g.x() + MathUtils.random(12f, g.width() - 12f), g.y() + MathUtils.random(20f, g.height() * 0.6f),
                            MathUtils.random(-14f, 14f), MathUtils.random(28f, 64f),
                            MathUtils.random(0.7f, 1.2f), MathUtils.random(3f, 5f),
                            k % 3 == 0 ? COL_AMBER : (k % 3 == 1 ? COL_EMERALD : COL_CYAN));
                    }
                    if (camera != null) camera.addTrauma(0.10f);
                }
            } else if (view.status() == RunCoordinator.Status.ERROR || view.status() == RunCoordinator.Status.TIMEOUT) {
                statusPulseColor.set(COL_CORAL);
                statusPulseTimer = 0.65f;
                SoundBank.play(SoundBank.Sfx.FAIL);
            } else if (view.status() == RunCoordinator.Status.INCOMPLETE || view.status() == RunCoordinator.Status.LIMIT_EXCEEDED) {
                statusPulseColor.set(COL_AMBER);
                statusPulseTimer = 0.65f;
                SoundBank.play(SoundBank.Sfx.FAIL);
            }
        }

        if (dt > 0f) {
            for (int i = 0; i < wetFlashTimer.length; i++) {
                if (wetFlashTimer[i] > 0f) wetFlashTimer[i] = Math.max(0f, wetFlashTimer[i] - dt);
            }
            if (scanBeamTimer > 0f) scanBeamTimer = Math.max(0f, scanBeamTimer - dt);
            if (statusPulseTimer > 0f) statusPulseTimer = Math.max(0f, statusPulseTimer - dt);
            if (harvestTagTimer > 0f) harvestTagTimer = Math.max(0f, harvestTagTimer - dt);
            for (int i = 0; i < harvestPopTimer.length; i++) {
                if (harvestPopTimer[i] > 0f) {
                    harvestPopTimer[i] = Math.max(0f, harvestPopTimer[i] - dt);
                }
            }
        }
    }

    private static float lerpAngleDeg(float current, float target, float alpha) {
        float diff = ((target - current + 540f) % 360f) - 180f;
        return current + diff * alpha;
    }

    private void spawnParticle(float x, float y, float vx, float vy, float life, float size, Color color) {
        if (particles.size() >= 64) return;
        WorldParticle p = new WorldParticle();
        p.x = x; p.y = y; p.vx = vx; p.vy = vy;
        p.age = 0f; p.life = life; p.size = size;
        p.color.set(color);
        particles.add(p);
    }

    private void updateParticles(float dt) {
        if (dt <= 0f || particles.isEmpty()) return;
        Iterator<WorldParticle> it = particles.iterator();
        while (it.hasNext()) {
            WorldParticle p = it.next();
            p.age += dt;
            if (p.age >= p.life) {
                it.remove();
            } else {
                p.x += p.vx * dt;
                p.y += p.vy * dt;
                p.vx *= (1f - 2.2f * dt);
            }
        }
    }

    private void drawParticles() {
        if (particles.isEmpty()) return;
        enableSafeAlphaBlend();
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (WorldParticle p : particles) {
            float alpha = Math.max(0f, 1f - p.age / p.life) * p.color.a;
            float sz = Math.max(2f, Math.round(p.size * (1f - 0.3f * (p.age / p.life))));
            shapes.setColor(p.color.r, p.color.g, p.color.b, alpha);
            shapes.rect(Math.round(p.x - sz * 0.5f), Math.round(p.y - sz * 0.5f), sz, sz);
        }
        shapes.end();
    }

    private void drawTiledWalkwayH(SpriteBatch batch, float x, float y, float w, float h) {
        float seg = 64f;
        for (float cur = x; cur < x + w; cur += seg) {
            float dw = Math.min(seg, x + w - cur);
            batch.draw(walkwayH, cur, y, dw, h);
        }
    }

    private void drawTiledWalkwayV(SpriteBatch batch, float x, float y, float w, float h) {
        float seg = 64f;
        for (float cur = y; cur < y + h; cur += seg) {
            float dh = Math.min(seg, y + h - cur);
            batch.draw(walkwayV, x, cur, w, dh);
        }
    }

    private void drawPixelPipeH(float x1, float y, float x2, float thick, Color pipeCol, boolean glowing) {
        float minX = Math.min(x1, x2), w = Math.abs(x2 - x1);
        shapes.setColor(COL_PIPE_DARK);
        shapes.rect(minX - 2f, y - thick * 0.5f - 2f, w + 4f, thick + 4f);
        shapes.setColor(pipeCol);
        shapes.rect(minX, y - thick * 0.5f, w, thick);
        shapes.setColor(glowing ? new Color(0.84f, 0.99f, 1f, 0.85f) : COL_PIPE_HI);
        shapes.rect(minX, y + thick * 0.5f - 3f, w, 2f);
    }

    private void drawPixelPipeV(float x, float y1, float y2, float thick, Color pipeCol, boolean glowing) {
        float minY = Math.min(y1, y2), h = Math.abs(y2 - y1);
        shapes.setColor(COL_PIPE_DARK);
        shapes.rect(x - thick * 0.5f - 2f, minY - 2f, thick + 4f, h + 4f);
        shapes.setColor(pipeCol);
        shapes.rect(x - thick * 0.5f, minY, thick, h);
        shapes.setColor(glowing ? new Color(0.84f, 0.99f, 1f, 0.85f) : COL_PIPE_HI);
        shapes.rect(x - thick * 0.5f + 1f, minY, 2f, h);
    }

    private void drawPipeFlange(float x, float y) {
        shapes.setColor(COL_PIPE_DARK);
        shapes.rect(x - 10f, y - 10f, 20f, 20f);
        shapes.setColor(Color.valueOf("475569"));
        shapes.rect(x - 8f, y - 8f, 16f, 16f);
        shapes.setColor(Color.valueOf("94A3B8"));
        shapes.rect(x - 4f, y - 4f, 8f, 8f);
    }

    private void drawBevelRect(float x, float y, float w, float h, float cut, Color col) {
        float c = Math.min(cut, Math.min(w, h) * 0.4f);
        shapes.setColor(col);
        shapes.rect(x + c, y, Math.max(0f, w - c * 2f), h);
        shapes.rect(x, y + c, Math.max(0f, w), Math.max(0f, h - c * 2f));
    }

    private void drawSquareOutline(float x, float y, float w, float h, float t, Color col) {
        shapes.setColor(col);
        shapes.rect(x, y, w, t);
        shapes.rect(x, y + h - t, w, t);
        shapes.rect(x, y, t, h);
        shapes.rect(x + w - t, y, t, h);
    }

    private void drawPixelCornerBrackets(HabitatWorld.Box bounds, Color accent, float pad, float leg, float thick, float alpha) {
        float x = bounds.x() - pad;
        float y = bounds.y() - pad;
        float r = bounds.right() + pad;
        float top = bounds.top() + pad;
        shapes.setColor(accent.r, accent.g, accent.b, alpha);
        shapes.rect(x, y, leg, thick);
        shapes.rect(x, y, thick, leg);
        shapes.rect(r - leg, y, leg, thick);
        shapes.rect(r - thick, y, thick, leg);
        shapes.rect(x, top - thick, leg, thick);
        shapes.rect(x, top - leg, thick, leg);
        shapes.rect(r - leg, top - thick, leg, thick);
        shapes.rect(r - thick, top - leg, thick, leg);
    }

    private void drawRadialGlow(SpriteBatch batch, float cx, float cy, float w, float h, Color col, float alpha) {
        batch.setColor(col.r, col.g, col.b, alpha);
        batch.draw(lightGlowTexture, cx - w * 0.5f, cy - h * 0.5f, w, h);
        batch.setColor(Color.WHITE);
    }

    private void drawWorldBadge(String text, float centerX, float centerY, Color borderCol, Color textCol, boolean active) {
        layout.setText(font, text);
        float padX = 11f;
        float padY = 6f;
        float bw = layout.width + padX * 2f;
        float bh = layout.height + padY * 2f;
        float bx = Math.round(centerX - bw * 0.5f);
        float by = Math.round(centerY - bh * 0.5f);

        enableSafeAlphaBlend();
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        drawBevelRect(bx - 2f, by - 2f, bw + 4f, bh + 4f, 4f,
            new Color(borderCol.r, borderCol.g, borderCol.b, active ? 0.94f : 0.65f));
        drawBevelRect(bx, by, bw, bh, 3f, active ? COL_BADGE_ACTIVE : COL_BADGE_BG);
        shapes.end();

        batch.begin();
        font.setColor(textCol);
        font.draw(batch, text, bx + padX, by + bh - padY);
        font.setColor(Color.WHITE);
        batch.end();
    }

    private static FileHandle resolveAsset(String path) {
        FileHandle handle = Gdx.files.internal(path);
        if (!handle.exists()) {
            handle = Gdx.files.local("assets/" + path);
        }
        return handle;
    }

    private Texture loadPixel(String path, int fallbackW, int fallbackH, Color fallbackColor) {
        FileHandle handle = resolveAsset(path);
        Texture texture;
        if (handle.exists()) {
            texture = new Texture(handle, false);
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

    private Texture loadSmooth(String path, int fallbackW, int fallbackH, Color fallbackColor) {
        FileHandle handle = resolveAsset(path);
        Texture texture;
        if (handle.exists()) {
            texture = new Texture(handle, false);
        } else {
            Pixmap pixmap = new Pixmap(fallbackW, fallbackH, Pixmap.Format.RGBA8888);
            pixmap.setColor(fallbackColor);
            pixmap.fill();
            texture = new Texture(pixmap);
            pixmap.dispose();
        }
        texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        ownedTextures.add(texture);
        return texture;
    }

    private static void enableSafeAlphaBlend() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFuncSeparate(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, GL20.GL_ONE, GL20.GL_ONE);
    }

    @Override
    public void dispose() {
        shapes.dispose();
        batch.dispose();
        font.dispose();
        if (fontTexture != null) fontTexture.dispose();
        for (Texture texture : ownedTextures) {
            texture.dispose();
        }
        ownedTextures.clear();
    }
}
