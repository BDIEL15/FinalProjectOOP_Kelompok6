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
 * Smooth, organic 2D top-down 3/4 renderer for the expanded 1680x1040 Mars Outpost:
 * - Curved Bio-Dome conservatory complex with botanical pods and irrigation rings
 * - Anti-aliased rounded capsules, soft radial 2D lights, and EVA dust trail
 * - Smooth presentation-layer rover glide, wheel heading rotation, scanner harvest beam, and spore particles
 *   strictly reading deterministic snapshots from RunCoordinator.View
 */
public final class HabitatRenderer implements Disposable {
    private static final Color COL_CANYON_OUTER = Color.valueOf("2E1109");
    private static final Color COL_CANYON_RIM = Color.valueOf("9C472B");
    private static final Color COL_PIPE_DARK = Color.valueOf("182230");
    private static final Color COL_PIPE_OFF = Color.valueOf("475569");
    private static final Color COL_CYAN = Color.valueOf("22D3EE");
    private static final Color COL_AMBER = Color.valueOf("F59E0B");
    private static final Color COL_EMERALD = Color.valueOf("34D399");
    private static final Color COL_CORAL = Color.valueOf("F87171");
    private static final Color COL_PILL_BG = new Color(0.05f, 0.08f, 0.14f, 0.88f);
    private static final Color COL_PILL_ACTIVE = new Color(0.05f, 0.19f, 0.28f, 0.94f);
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
    private final Texture lightGlowTexture;

    private final List<WorldParticle> particles = new ArrayList<>();
    private final Map<RobotSimulation.Position, RobotSimulation.Crop> prevCrops = new HashMap<>();
    private final float[] harvestPopTimer = new float[8];

    private float animTime;
    private int astroDir = 0; // 0:Down, 1:Up, 2:Left, 3:Right
    private float walkTimer = 0f;
    private float dustSpawnTimer = 0f;

    // Smooth Rover presentation state
    private long trackedGeneration = -1;
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
            marsSoilVariants[i] = loadSmooth("habitat/mars_soil_" + i + ".png", 128, 128, Color.valueOf("6B2E1E"));
        }
        floorTile = loadSmooth("habitat/floor_tile.png", 128, 128, Color.valueOf("662D1D"));
        craterSm = loadSmooth("habitat/mars_crater_sm.png", 96, 64, Color.valueOf("4A1E13"));
        craterLg = loadSmooth("habitat/mars_crater_lg.png", 144, 96, Color.valueOf("4A1E13"));
        rockA = loadSmooth("habitat/mars_rock_a.png", 64, 56, Color.valueOf("8C4029"));
        rockB = loadSmooth("habitat/mars_rock_b.png", 56, 48, Color.valueOf("8C4029"));
        cargoCrate = loadSmooth("habitat/cargo_crate.png", 48, 48, Color.valueOf("334155"));
        walkwayH = loadSmooth("habitat/walkway_h.png", 64, 40, Color.valueOf("273549"));
        walkwayV = loadSmooth("habitat/walkway_v.png", 40, 64, Color.valueOf("273549"));

        habAirlockTexture = loadSmooth("habitat/hab_airlock.png", 240, 190, Color.valueOf("CBD5E1"));
        solarDustyTexture = loadSmooth("habitat/solar_array_dusty.png", 220, 110, Color.valueOf("78350F"));
        solarCleanTexture = loadSmooth("habitat/solar_array_clean.png", 220, 110, Color.valueOf("1D4ED8"));
        partitionOffTexture = loadSmooth("habitat/partition_wall.png", 64, 240, Color.valueOf("3B495E"));
        partitionOnTexture = loadSmooth("habitat/partition_wall_restored.png", 64, 240, COL_CYAN);
        panelTexture = loadSmooth("habitat/water_panel.png", 110, 60, COL_CYAN);
        pumpStationTexture = loadSmooth("habitat/pump_station.png", 160, 140, Color.valueOf("334155"));
        bioDomeDeckOffTexture = loadSmooth("habitat/biodome_deck.png", 480, 320, Color.valueOf("1E293B"));
        bioDomeDeckOnTexture = loadSmooth("habitat/biodome_deck_restored.png", 480, 320, Color.valueOf("1E293B"));
        bioDomeCanopyTexture = loadSmooth("habitat/biodome_canopy.png", 480, 320, new Color(0.2f, 0.75f, 0.97f, 0.25f));

        roverTexture = loadSmooth("habitat/mars_rover.png", 128, 128, COL_AMBER);
        roverRegion = new TextureRegion(roverTexture);

        Texture astroSheet = loadSmooth("habitat/astronaut_eva_sheet.png", 256, 256, COL_TEXT_MAIN);
        astronautFrames = TextureRegion.split(astroSheet, 64, 64);

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

        // 1. Expanded Martian Regolith + Courtyard Terrain across 1680x1040
        batch.setColor(Color.WHITE);
        batch.setBlendFunctionSeparate(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, GL20.GL_ONE, GL20.GL_ONE);
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        for (int gy = 0; gy < (int) HabitatWorld.HEIGHT; gy += 128) {
            for (int gx = 0; gx < (int) HabitatWorld.WIDTH; gx += 128) {
                int variant = Math.floorMod((gx / 128) * 73856093 ^ (gy / 128) * 19349663, 4);
                batch.draw(marsSoilVariants[variant], gx, gy, 128, 128);
            }
        }
        int floorRight = (int) HabitatWorld.FLOOR.right();
        int floorTop = (int) HabitatWorld.FLOOR.top();
        for (int gy = (int) HabitatWorld.FLOOR.y(); gy < floorTop; gy += 128) {
            int drawH = Math.min(128, floorTop - gy);
            for (int gx = (int) HabitatWorld.FLOOR.x(); gx < floorRight; gx += 128) {
                int drawW = Math.min(128, floorRight - gx);
                batch.draw(floorTile, gx, gy, drawW, drawH);
            }
        }

        // Organic craters around the perimeter and open dunes
        batch.draw(craterLg, 92, 110, 148, 92);
        batch.draw(craterSm, 460, 130, 96, 62);
        batch.draw(craterLg, 790, 116, 152, 94);
        batch.draw(craterSm, 440, 845, 102, 64);
        batch.draw(craterLg, 1320, 118, 150, 92);
        batch.draw(craterSm, 1420, 560, 96, 60);
        batch.end();

        // 2. Soft Canyon Bounds, Rover Track Breadcrumbs, & Curved Coolant Conduits
        enableSafeAlphaBlend();
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        // Smooth rounded canyon perimeter collar (hollow rim around walkable FLOOR)
        float bx1 = HabitatWorld.FLOOR.x() - 4f;
        float by1 = HabitatWorld.FLOOR.y() - 4f;
        float bx2 = HabitatWorld.FLOOR.right() + 4f;
        float by2 = HabitatWorld.FLOOR.top() + 4f;
        drawCapsulePipe(bx1, by1, bx2, by1, 4f, COL_CANYON_RIM, false);
        drawCapsulePipe(bx1, by2, bx2, by2, 4f, COL_CANYON_RIM, false);
        drawCapsulePipe(bx1, by1, bx1, by2, 4f, COL_CANYON_RIM, false);
        drawCapsulePipe(bx2, by1, bx2, by2, 4f, COL_CANYON_RIM, false);

        // Subtle rover guidance tracks curving from Airlock Courtyard -> Bio-Dome Conservatory
        Color trackCol = new Color(0.24f, 0.10f, 0.06f, 0.45f);
        for (int i = 0; i < 16; i++) {
            float tx = 290f + i * 38f;
            float ty = 320f - (float) Math.sin(i * 0.25f) * 34f;
            fillRoundedRect(tx, ty + 4f, 18f, 4.5f, 2.2f, trackCol);
            fillRoundedRect(tx, ty - 8f, 18f, 4.5f, 2.2f, trackCol);
        }

        // Curved coolant & nutrient pipeline network across outpost zones
        boolean bioDomeLit = restored || puzzle.completed();
        Color mainPipeCol = restored ? COL_CYAN : COL_PIPE_OFF;
        Color bioPipeCol = bioDomeLit ? COL_EMERALD : Color.valueOf("64748B");

        drawCapsulePipe(250f, 550f, 558f, 550f, 6f, mainPipeCol, restored);
        drawCapsulePipe(558f, 550f, 558f, 680f, 6f, mainPipeCol, restored);
        drawCapsulePipe(260f, 800f, 558f, 800f, 5f, mainPipeCol, restored);
        drawCapsulePipe(558f, 800f, 558f, 720f, 5f, mainPipeCol, restored);
        drawCapsulePipe(558f, 706f, 672f, 706f, 6f, mainPipeCol, restored);
        drawCapsulePipe(672f, 706f, 672f, 490f, 6f, mainPipeCol, restored);
        drawCapsulePipe(672f, 460f, 870f, 460f, 7f, bioPipeCol, bioDomeLit);
        drawCapsulePipe(1380f, 460f, 1460f, 460f, 6f, mainPipeCol, restored);
        drawCapsulePipe(1460f, 460f, 1460f, 780f, 6f, mainPipeCol, restored);

        // Animated flow pulses along active conduits
        if ((restored || bioDomeLit) && !reducedMotion) {
            float flow = (animTime * 95f) % 190f;
            shapes.setColor(0.84f, 0.99f, 1f, 0.9f);
            shapes.circle(675f + flow, 460f, 4f, 12);
        }

        // Subtle animated navigation beacons from Airlock Plaza to Bio-Dome Entrance
        for (int b = 0; b < 5; b++) {
            float bx = 370f + b * 124f;
            float by = 290f + (b > 2 ? (b - 2) * 48f : 0f);
            float beaconPulse = reducedMotion ? 0.6f : (0.35f + 0.45f * Math.max(0f, (float) Math.sin(animTime * 3.5f - b * 0.7f)));
            shapes.setColor(COL_CYAN.r, COL_CYAN.g, COL_CYAN.b, beaconPulse);
            shapes.circle(bx, by, 4.5f, 14);
        }
        shapes.end();

        // 3. Textured Grated Walkways, Boulders, Outpost Structures & Expanded Bio-Dome Conservatory
        batch.begin();
        batch.setColor(Color.WHITE);

        // Grated outpost walkways framing natural exploration path
        drawTiledWalkwayV(batch, 198, 355, 42, 130);
        drawTiledWalkwayH(batch, 250, 700, 260, 38);
        drawTiledWalkwayH(batch, 710, 442, 178, 38);
        drawTiledWalkwayV(batch, 1120, 620, 42, 140);

        // Smooth rounded Martian boulders & supply pods
        batch.draw(rockA, 380, 240, 48, 42);
        batch.draw(rockB, 560, 250, 42, 36);
        batch.draw(rockA, 780, 740, 52, 44);
        batch.draw(rockB, 1440, 290, 44, 38);
        batch.draw(rockA, 920, 810, 48, 40);
        batch.draw(cargoCrate, 315, 485, 36, 36);
        batch.draw(cargoCrate, 354, 495, 32, 32);

        // West Outpost Buildings: Hab Airlock Dome & North-West Solar Wing
        batch.draw(habAirlockTexture, HabitatWorld.HAB_AIRLOCK.x(), HabitatWorld.HAB_AIRLOCK.y(),
            HabitatWorld.HAB_AIRLOCK.width(), HabitatWorld.HAB_AIRLOCK.height());
        batch.draw(restored ? solarCleanTexture : solarDustyTexture,
            HabitatWorld.SOLAR_WING.x(), HabitatWorld.SOLAR_WING.y(),
            HabitatWorld.SOLAR_WING.width(), HabitatWorld.SOLAR_WING.height());

        // Central Conduit Spine
        batch.draw(restored ? partitionOnTexture : partitionOffTexture,
            HabitatWorld.PARTITION.x(), HabitatWorld.PARTITION.y(),
            HabitatWorld.PARTITION.width(), HabitatWorld.PARTITION.height());

        // Telemetry & Water Panel Relay
        batch.draw(panelTexture,
            HabitatWorld.Facility.PANEL.bounds.x(), HabitatWorld.Facility.PANEL.bounds.y(),
            HabitatWorld.Facility.PANEL.bounds.width(), HabitatWorld.Facility.PANEL.bounds.height());

        // North-East Booster Pump Dome
        batch.draw(pumpStationTexture,
            HabitatWorld.PUMP_STATION.x(), HabitatWorld.PUMP_STATION.y(),
            HabitatWorld.PUMP_STATION.width(), HabitatWorld.PUMP_STATION.height());

        // Expansive Circular-Oval Bio-Dome Conservatory enclosing GARDEN(960, 360, 360, 200)
        float domeX = HabitatWorld.GARDEN.x() - 120f;
        float domeY = HabitatWorld.GARDEN.y() - 95f;
        float domeW = HabitatWorld.GARDEN.width() + 240f; // 600
        float domeH = HabitatWorld.GARDEN.height() + 190f; // 390
        batch.draw(bioDomeLit ? bioDomeDeckOnTexture : bioDomeDeckOffTexture, domeX, domeY, domeW, domeH);

        // Soft Dust Trail + Astronaut EVA Sprite (drawn on top of walkways, beside dome walls)
        batch.end();
        drawParticles();
        batch.begin();
        batch.setColor(Color.WHITE);
        int frameIdx = world.currentSpeed() > 12f ? ((int) (walkTimer * 9f) % 4) : 0;
        TextureRegion astroRegion = astronautFrames[astroDir][frameIdx];
        float drawAX = world.x() - 6f;
        float drawAY = world.y() - 4f;
        batch.draw(astroRegion, drawAX, drawAY, HabitatWorld.PLAYER_WIDTH + 12f, HabitatWorld.PLAYER_HEIGHT + 12f);

        // Translucent peripheral skylight dome arcs around Bio-Dome
        batch.setColor(1f, 1f, 1f, 0.55f);
        batch.draw(bioDomeCanopyTexture, domeX, domeY, domeW, domeH);
        batch.setColor(Color.WHITE);
        batch.end();

        // 4. Smooth Additive Radial Lighting
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

        // 5. Rounded Interaction Rings (no harsh corners!)
        enableSafeAlphaBlend();
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        if (nearby == HabitatWorld.Facility.PANEL || panelHovered) {
            drawRoundedInteractionHalo(HabitatWorld.Facility.PANEL.bounds, COL_CYAN, 16f, 0.85f);
        }
        if (nearby == HabitatWorld.Facility.ROVER) {
            drawRoundedInteractionHalo(HabitatWorld.GARDEN, COL_CYAN, 16f, 0.45f * pulse);
        }

        // Subtle Martian ambient dust motes around camera focus
        if (!reducedMotion) {
            shapes.setColor(0.98f, 0.78f, 0.62f, 0.24f);
            for (int i = 0; i < 16; i++) {
                float dx = ((180f * i + animTime * (18f + (i % 5) * 4f)) % (HabitatWorld.WIDTH - 120f)) + 60f;
                float dy = ((110f * i + 95f) % (HabitatWorld.HEIGHT - 120f)) + 60f;
                shapes.circle(dx, dy, 1.8f, 8);
            }
        }
        shapes.end();

        // 6. Active Bio-Dome Hydroponic Pods + Smooth Rover Replay & Clickable Highlight
        drawBioDomePuzzle(puzzle, roverHovered, nearby == HabitatWorld.Facility.ROVER, world.mode(), pulse);

        // 7. Minimal Contextual Rounded World Pills (no cluttered text walls over map)
        drawWorldPill("AIRLOCK HAB", HabitatWorld.HAB_AIRLOCK.centerX(), HabitatWorld.HAB_AIRLOCK.y() - 16f,
            restored ? COL_EMERALD : COL_AMBER, COL_TEXT_MAIN, false);

        if (nearby == HabitatWorld.Facility.PANEL || panelHovered) {
            drawWorldPill("[E] / Klik Panel Telemetri",
                HabitatWorld.Facility.PANEL.bounds.centerX(),
                HabitatWorld.Facility.PANEL.bounds.top() + 18f,
                COL_CYAN, COL_CYAN, true);
        }

        if (puzzle.completed()) {
            drawWorldPill("PANEN BIO-DOME SELESAI", HabitatWorld.GARDEN.centerX(), HabitatWorld.GARDEN.top() + 36f,
                COL_EMERALD, COL_EMERALD, true);
        } else if (world.mode() == HabitatWorld.Mode.EXPLORING) {
            String bioTag = (roverHovered || nearby == HabitatWorld.Facility.ROVER)
                ? "Klik Rover / [E] Buka Editor"
                : "BIO-DOME P1 (" + puzzle.snapshot().remainingTargets().size() + "/2 target)";
            drawWorldPill(bioTag, HabitatWorld.GARDEN.centerX(), HabitatWorld.GARDEN.top() + 36f,
                (roverHovered || nearby == HabitatWorld.Facility.ROVER) ? COL_CYAN : COL_AMBER,
                (roverHovered || nearby == HabitatWorld.Facility.ROVER) ? COL_CYAN : COL_TEXT_MAIN,
                roverHovered || nearby == HabitatWorld.Facility.ROVER);
        }

        if (harvestTagTimer > 0f) {
            float alpha = Math.min(1f, harvestTagTimer * 2.2f);
            float liftY = (0.75f - harvestTagTimer) * 28f;
            drawWorldPill("+1 PANEN", harvestTagX, harvestTagY + liftY,
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
        int cols = 2;
        boolean completed = view.completed();
        boolean running = view.status() == RunCoordinator.Status.PREPARING
            || view.status() == RunCoordinator.Status.RUNNING
            || view.status() == RunCoordinator.Status.REPLAYING;

        enableSafeAlphaBlend();
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        // Soft glowing hydroponic pad tray inside Bio-Dome
        Color frameColor = completed ? COL_EMERALD : (running ? COL_CYAN : Color.valueOf("334155"));
        fillRoundedRect(HabitatWorld.GARDEN.x(), HabitatWorld.GARDEN.y(),
            HabitatWorld.GARDEN.width(), HabitatWorld.GARDEN.height(), 28f, frameColor);
        fillRoundedRect(HabitatWorld.GARDEN.x() + 2.5f, HabitatWorld.GARDEN.y() + 2.5f,
            HabitatWorld.GARDEN.width() - 5f, HabitatWorld.GARDEN.height() - 5f, 26f, Color.valueOf("0E1722"));

        // Track trajectory trace connecting cell (0,0) and cell (1,0)
        HabitatWorld.Box c0 = HabitatWorld.hydroCellBox(0, 0, cols);
        HabitatWorld.Box c1 = HabitatWorld.hydroCellBox(1, 0, cols);
        fillCapsule(c0.centerX(), c0.centerY(), c1.centerX(), c1.centerY(), 6f, Color.valueOf("1E293B"));

        for (int cell = 0; cell < cols; cell++) {
            HabitatWorld.Box box = HabitatWorld.hydroCellBox(cell, 0, cols);
            boolean hasRoverHere = view.snapshot().robot().x() == cell;
            RobotSimulation.Crop crop = view.snapshot().crops()
                .getOrDefault(new RobotSimulation.Position(cell, 0), RobotSimulation.Crop.EMPTY);

            // Organic rounded hydroponic pod (zero blocky sharp boxes)
            Color podRim = hasRoverHere ? COL_CYAN
                : (crop == RobotSimulation.Crop.EMPTY ? COL_EMERALD : Color.valueOf("475569"));
            fillRoundedRect(box.x(), box.y(), box.width(), box.height(), 18f, podRim);
            fillRoundedRect(box.x() + 2.2f, box.y() + 2.2f, box.width() - 4.4f, box.height() - 4.4f, 16f,
                Color.valueOf("1F1410"));

            // Smooth curved irrigated soil furrow arcs
            shapes.setColor(crop == RobotSimulation.Crop.EMPTY ? Color.valueOf("170F0C") : Color.valueOf("301D16"));
            for (int r = 0; r < 3; r++) {
                fillCapsule(box.x() + 14f, box.y() + 22f + r * 28f, box.right() - 14f, box.y() + 22f + r * 28f,
                    5f, shapes.getColor());
            }

            // Left circular Rover docking pad inside pod
            float padX = box.x() + 34f;
            float padY = box.y() + 50f;
            shapes.setColor(hasRoverHere ? new Color(COL_CYAN.r, COL_CYAN.g, COL_CYAN.b, 0.24f) : new Color(0.12f, 0.18f, 0.27f, 0.72f));
            shapes.circle(padX, padY, 22f, 20);

            // Right circular botanical planter socket
            float plantX = box.x() + 90f;
            float plantY = box.y() + 54f;
            shapes.setColor(Color.valueOf("162232"));
            shapes.circle(plantX, plantY, 24f, 24);
            shapes.setColor(crop == RobotSimulation.Crop.EMPTY ? Color.valueOf("1E130F") : Color.valueOf("2A1A14"));
            shapes.circle(plantX, plantY, 20f, 24);

            // Smooth Botanical Crops (curved leaves & glowing golden produce)
            float breathe = HabitatUi.isReducedMotion() ? 1f : (1f + 0.04f * (float) Math.sin(animTime * 2.5f + cell));
            float popScale = harvestPopTimer[cell] > 0f ? (1f + harvestPopTimer[cell] * 0.45f) : 1f;
            if (crop == RobotSimulation.Crop.RIPE) {
                drawRipeBotanicalPlant(plantX, plantY, breathe * popScale);
            } else if (crop == RobotSimulation.Crop.YOUNG) {
                drawYoungSprout(plantX, plantY, breathe);
            } else {
                // Cleanly harvested dark moist crater + subtle green ring
                shapes.setColor(Color.valueOf("472B1F"));
                shapes.circle(plantX, plantY, 10f, 16);
                shapes.setColor(COL_EMERALD.r, COL_EMERALD.g, COL_EMERALD.b, 0.55f);
                drawRing(plantX, plantY, 18f, 2f);
            }

            // Expanding harvest ring pop
            if (harvestPopTimer[cell] > 0f) {
                float p = 1f - harvestPopTimer[cell] / 0.45f;
                float ringAlpha = 1f - p;
                shapes.setColor(COL_EMERALD.r, COL_EMERALD.g, COL_EMERALD.b, ringAlpha);
                drawRing(plantX, plantY, 18f + p * 26f, 2.6f);
            }

            // Rounded coordinate capsule strictly below each bed
            fillRoundedRect(box.centerX() - 28f, HabitatWorld.GARDEN.y() + 12f, 56f, 22f, 11f,
                Color.valueOf("0A111B"));
        }

        // Smooth Rover position in world coordinates
        HabitatWorld.Box roverBox = HabitatWorld.roverBayBox(visualRoverCellX, visualRoverCellY, cols);
        float roverCx = roverBox.centerX();
        float roverCy = roverBox.centerY();

        // Status sonar pulse (on error / limit / complete)
        if (statusPulseTimer > 0f) {
            float prog = 1f - statusPulseTimer / 0.65f;
            shapes.setColor(statusPulseColor.r, statusPulseColor.g, statusPulseColor.b, (1f - prog) * 0.85f);
            drawRing(roverCx, roverCy, 24f + prog * 34f, 3f);
        }

        // Hover / nearby clickable halo around rover
        if ((roverHovered || roverNearby) && mode == HabitatWorld.Mode.EXPLORING) {
            float haloR = 32f + 2f * pulse;
            shapes.setColor(COL_CYAN.r, COL_CYAN.g, COL_CYAN.b, roverHovered ? 0.24f : 0.14f);
            shapes.circle(roverCx, roverCy, haloR, 28);
            shapes.setColor(COL_CYAN.r, COL_CYAN.g, COL_CYAN.b, roverHovered ? 0.95f : 0.72f);
            drawRing(roverCx, roverCy, haloR, 2.2f);
        }

        // Harvest Scanner / Pincer Laser Beam during harvest() replay
        if (scanBeamTimer > 0f) {
            float beamAlpha = Math.min(1f, scanBeamTimer / 0.22f);
            Color beamOuter = new Color(COL_EMERALD.r, COL_EMERALD.g, COL_EMERALD.b, beamAlpha * 0.45f);
            Color beamCore = new Color(0.85f, 1f, 0.96f, beamAlpha * 0.92f);
            fillCapsule(beamFromX, beamFromY, beamToX, beamToY, 6f, beamOuter);
            fillCapsule(beamFromX, beamFromY, beamToX, beamToY, 2.2f, beamCore);
            shapes.setColor(COL_EMERALD.r, COL_EMERALD.g, COL_EMERALD.b, beamAlpha * 0.8f);
            shapes.circle(beamToX, beamToY, 9f + (1f - beamAlpha) * 10f, 18);
        }

        shapes.end();

        // Draw rotated smooth Mars Rover sprite + coordinate text
        batch.begin();
        batch.setColor(Color.WHITE);
        float rSize = 54f;
        batch.draw(
            roverRegion,
            roverCx - rSize * 0.5f, roverCy - rSize * 0.5f,
            rSize * 0.5f, rSize * 0.5f,
            rSize, rSize,
            1f, 1f,
            visualRoverAngleDeg
        );

        for (int cell = 0; cell < cols; cell++) {
            HabitatWorld.Box box = HabitatWorld.hydroCellBox(cell, 0, cols);
            boolean hasRoverHere = view.snapshot().robot().x() == cell;
            String coord = "(" + cell + ",0)";
            layout.setText(font, coord);
            font.setColor(hasRoverHere ? COL_CYAN : COL_TEXT_MAIN);
            font.draw(batch, coord, box.centerX() - layout.width * 0.5f, HabitatWorld.GARDEN.y() + 28f);
        }
        font.setColor(Color.WHITE);
        batch.end();
    }

    private void drawRipeBotanicalPlant(float cx, float cy, float scale) {
        shapes.setColor(Color.valueOf("065F46"));
        shapes.circle(cx, cy, 16f * scale, 20);
        shapes.setColor(Color.valueOf("10B981"));
        for (int l = 0; l < 5; l++) {
            double ang = l * (Math.PI * 2.0 / 5.0) + 0.3;
            shapes.circle(
                cx + (float) Math.cos(ang) * 9f * scale,
                cy + (float) Math.sin(ang) * 9f * scale,
                8.5f * scale, 16
            );
        }
        shapes.setColor(COL_EMERALD);
        shapes.circle(cx, cy, 8f * scale, 16);
        shapes.setColor(COL_AMBER);
        shapes.circle(cx - 5f * scale, cy + 4f * scale, 4.5f * scale, 14);
        shapes.circle(cx + 6f * scale, cy - 3f * scale, 4.2f * scale, 14);
        shapes.setColor(Color.valueOf("FDE68A"));
        shapes.circle(cx - 6f * scale, cy + 5f * scale, 1.8f * scale, 10);
        shapes.circle(cx + 5f * scale, cy - 2f * scale, 1.6f * scale, 10);
    }

    private void drawYoungSprout(float cx, float cy, float scale) {
        shapes.setColor(Color.valueOf("10B981"));
        shapes.circle(cx, cy, 5f * scale, 14);
        shapes.setColor(COL_EMERALD);
        shapes.circle(cx - 6f * scale, cy + 3f * scale, 4.5f * scale, 12);
        shapes.circle(cx + 6f * scale, cy + 3f * scale, 4.5f * scale, 12);
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
            // Spawn gentle EVA dust puffs beneath boots while walking
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
                        3.8f,
                        new Color(0.84f, 0.55f, 0.38f, 0.45f)
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

        if (trackedGeneration != view.generation() || view.status() == RunCoordinator.Status.READY) {
            boolean freshRun = trackedGeneration != view.generation();
            trackedGeneration = view.generation();
            if (freshRun) {
                visualRoverCellX = targetX;
                visualRoverCellY = targetY;
                visualRoverAngleDeg = 0f;
                scanBeamTimer = 0f;
                statusPulseTimer = 0f;
                prevCrops.clear();
                prevCrops.putAll(snap.crops());
            }
        }

        // Smooth glide toward current snapshot rover coordinates
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
                // Gently orient toward east-facing plant pod when parked
                visualRoverAngleDeg = lerpAngleDeg(visualRoverAngleDeg, 0f, 1f - (float) Math.exp(-6f * dt));
            }
        }

        // Detect crop transitions (RIPE -> EMPTY) to fire smooth scanner laser + particle burst
        for (Map.Entry<RobotSimulation.Position, RobotSimulation.Crop> entry : snap.crops().entrySet()) {
            RobotSimulation.Position pos = entry.getKey();
            RobotSimulation.Crop cur = entry.getValue();
            RobotSimulation.Crop prev = prevCrops.get(pos);
            if (prev == RobotSimulation.Crop.RIPE && cur == RobotSimulation.Crop.EMPTY) {
                int idx = Math.max(0, Math.min(harvestPopTimer.length - 1, pos.x()));
                harvestPopTimer[idx] = 0.45f;
                HabitatWorld.Box pod = HabitatWorld.hydroCellBox(pos.x(), pos.y(), 2);
                HabitatWorld.Box rover = HabitatWorld.roverBayBox(visualRoverCellX, visualRoverCellY, 2);
                beamFromX = rover.centerX() + 14f;
                beamFromY = rover.centerY();
                beamToX = pod.x() + 90f;
                beamToY = pod.y() + 54f;
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
                            0.52f, 3.6f,
                            p % 3 == 0 ? COL_AMBER : COL_EMERALD
                        );
                    }
                }
            }
        }
        prevCrops.clear();
        prevCrops.putAll(snap.crops());

        // Trigger status sonar pulse on runtime transitions
        if (view.status() != prevStatus) {
            prevStatus = view.status();
            if (view.status() == RunCoordinator.Status.SUCCESS) {
                statusPulseColor.set(COL_EMERALD);
                statusPulseTimer = 0.65f;
            } else if (view.status() == RunCoordinator.Status.ERROR || view.status() == RunCoordinator.Status.TIMEOUT) {
                statusPulseColor.set(COL_CORAL);
                statusPulseTimer = 0.65f;
            } else if (view.status() == RunCoordinator.Status.INCOMPLETE || view.status() == RunCoordinator.Status.LIMIT_EXCEEDED) {
                statusPulseColor.set(COL_AMBER);
                statusPulseTimer = 0.65f;
            }
        }

        if (dt > 0f) {
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
            shapes.setColor(p.color.r, p.color.g, p.color.b, alpha);
            shapes.circle(p.x, p.y, p.size * (1f - 0.3f * (p.age / p.life)), 12);
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

    private void drawCapsulePipe(float x1, float y1, float x2, float y2, float radius, Color pipeCol, boolean glowing) {
        fillCapsule(x1, y1, x2, y2, radius + 3f, COL_PIPE_DARK);
        fillCapsule(x1, y1, x2, y2, radius, pipeCol);
        if (glowing) {
            fillCapsule(x1, y1, x2, y2, radius * 0.45f, new Color(0.84f, 0.99f, 1f, 0.85f));
        }
    }

    private void fillCapsule(float x1, float y1, float x2, float y2, float radius, Color col) {
        shapes.setColor(col);
        shapes.rectLine(x1, y1, x2, y2, radius * 2f);
        shapes.circle(x1, y1, radius, 14);
        shapes.circle(x2, y2, radius, 14);
    }

    private void fillRoundedRect(float x, float y, float w, float h, float r, Color col) {
        float rad = Math.min(r, Math.min(w, h) * 0.5f);
        shapes.setColor(col);
        shapes.rect(x + rad, y, Math.max(0f, w - rad * 2f), h);
        shapes.rect(x, y + rad, rad, Math.max(0f, h - rad * 2f));
        shapes.rect(x + w - rad, y + rad, rad, Math.max(0f, h - rad * 2f));
        shapes.arc(x + rad, y + rad, rad, 180f, 90f, 12);
        shapes.arc(x + w - rad, y + rad, rad, 270f, 90f, 12);
        shapes.arc(x + w - rad, y + h - rad, rad, 0f, 90f, 12);
        shapes.arc(x + rad, y + h - rad, rad, 90f, 90f, 12);
    }

    private void drawRing(float cx, float cy, float radius, float thickness) {
        int segs = 28;
        for (int i = 0; i < segs; i++) {
            float a1 = i * (MathUtils.PI2 / segs);
            float a2 = (i + 1) * (MathUtils.PI2 / segs);
            shapes.rectLine(
                cx + MathUtils.cos(a1) * radius, cy + MathUtils.sin(a1) * radius,
                cx + MathUtils.cos(a2) * radius, cy + MathUtils.sin(a2) * radius,
                thickness
            );
        }
    }

    private void drawRoundedInteractionHalo(HabitatWorld.Box bounds, Color accent, float pad, float alpha) {
        shapes.setColor(accent.r, accent.g, accent.b, alpha * 0.14f);
        fillRoundedRect(bounds.x() - pad, bounds.y() - pad, bounds.width() + pad * 2f, bounds.height() + pad * 2f, 24f, shapes.getColor());
    }

    private void drawRadialGlow(SpriteBatch batch, float cx, float cy, float w, float h, Color col, float alpha) {
        batch.setColor(col.r, col.g, col.b, alpha);
        batch.draw(lightGlowTexture, cx - w * 0.5f, cy - h * 0.5f, w, h);
        batch.setColor(Color.WHITE);
    }

    private void drawWorldPill(String text, float centerX, float centerY, Color borderCol, Color textCol, boolean active) {
        layout.setText(font, text);
        float padX = 12f;
        float padY = 6f;
        float bw = layout.width + padX * 2f;
        float bh = layout.height + padY * 2f;
        float bx = Math.round(centerX - bw * 0.5f);
        float by = Math.round(centerY - bh * 0.5f);
        float rad = bh * 0.5f;

        enableSafeAlphaBlend();
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        fillRoundedRect(bx - 1.5f, by - 1.5f, bw + 3f, bh + 3f, rad + 1.5f,
            new Color(borderCol.r, borderCol.g, borderCol.b, active ? 0.94f : 0.62f));
        fillRoundedRect(bx, by, bw, bh, rad, active ? COL_PILL_ACTIVE : COL_PILL_BG);
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
