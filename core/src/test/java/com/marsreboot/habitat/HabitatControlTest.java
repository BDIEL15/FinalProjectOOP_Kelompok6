package com.marsreboot.habitat;

import com.badlogic.gdx.Input.Keys;
import static com.marsreboot.habitat.HabitatWorld.*;

/** Headless regression suite for smooth astronaut physics, expanded outpost bounds, FollowCamera, and Rover click hit-testing. */
public final class HabitatControlTest {
    private static int passed;

    public static void main(String[] args) {
        smoothMovementAndTime();
        collisionAndExpandedBounds();
        interactionsFocusAndRoverClick();
        followCameraFramingAndClamping();
        inputEdges();
        System.out.println("PASS: " + passed + " habitat control assertions (no OpenGL).");
    }

    private static void smoothMovementAndTime() {
        float startX = new HabitatWorld().x();
        float startY = new HabitatWorld().y();

        HabitatWorld accel = new HabitatWorld();
        accel.move(1, 0, 0.1f);
        check(accel.x() > startX && accel.vx() > 0 && accel.vx() < SPEED,
            "smooth acceleration ramps below max speed on short press: " + accel.vx());

        accel.move(1, 0, 0.4f);
        check(accel.vx() > SPEED * 0.98f, "approaches steady top speed: " + accel.vx());

        float xBeforeCoast = accel.x();
        accel.move(0, 0, 0.45f);
        check(accel.x() > xBeforeCoast && accel.currentSpeed() == 0f,
            "smooth deceleration coasts gently and settles to complete rest: " + accel.currentSpeed());

        HabitatWorld diagonal = new HabitatWorld();
        diagonal.move(1, 1, 0.6f);
        float dx = diagonal.x() - startX;
        float dy = diagonal.y() - startY;
        near(dx, dy, 0.5f, "diagonal normalized equal X/Y displacement");

        // Frame-rate independence across 30, 60, and 144 FPS
        float[] finals = new float[3];
        int[] rates = {30, 60, 144};
        for (int idx = 0; idx < rates.length; idx++) {
            HabitatWorld timed = new HabitatWorld();
            int fps = rates[idx];
            for (int i = 0; i < fps; i++) timed.move(1, 0, 1f / fps);
            finals[idx] = timed.x();
        }
        near(finals[0], finals[1], 0.5f, "30fps matches 60fps");
        near(finals[1], finals[2], 0.5f, "60fps matches 144fps");

        HabitatWorld aliasWorld = new HabitatWorld();
        HabitatInput input = new HabitatInput(aliasWorld);
        input.keyDown(Keys.W);
        input.keyDown(Keys.UP);
        input.update(0.5f);
        HabitatWorld singleKey = new HabitatWorld();
        singleKey.move(0, 1, 0.5f);
        near(aliasWorld.y(), singleKey.y(), 0.05f, "aliases do not double speed");

        float steadyY = aliasWorld.y();
        input.keyDown(Keys.S);
        input.update(0.4f); // decelerates to rest while W+S cancel
        float settledY = aliasWorld.y();
        input.update(0.2f);
        near(aliasWorld.y(), settledY, 0.05f, "opposite keys cancel cleanly once settled");
        check(settledY >= steadyY, "momentum preserves direction before resting");

        HabitatWorld cleared = new HabitatWorld();
        HabitatInput cInput = new HabitatInput(cleared);
        cInput.keyDown(Keys.D);
        cInput.update(0.3f);
        cInput.clear();
        cInput.update(0.5f); // decays to stop without held input
        float afterCoast = cleared.x();
        cInput.update(0.3f);
        near(cleared.x(), afterCoast, 0.05f, "pause/hide clears held input cleanly");

        aliasWorld.move(1, 0, Float.NaN);
        near(aliasWorld.y(), settledY, 0.05f, "invalid delta ignored");
    }

    private static void collisionAndExpandedBounds() {
        check(WIDTH > CAMERA_WIDTH && HEIGHT > CAMERA_HEIGHT,
            "expanded outpost world exceeds single camera view");

        HabitatWorld airlockTest = new HabitatWorld();
        airlockTest.move(0, 1, 5f); // from x=220, y=320 walks north into HAB_AIRLOCK(110..300, y=480)
        near(airlockTest.y(), HAB_AIRLOCK.y() - PLAYER_HEIGHT, 0.05f, "airlock dome blocks upward approach");

        HabitatWorld partTest = new HabitatWorld();
        partTest.move(0, 1, 0.45f);
        partTest.move(1, 0, 5f);
        near(partTest.x(), PARTITION.x() - PLAYER_WIDTH, 0.05f, "cannot tunnel through conduit partition");
        partTest.move(0, 1, 0.5f);
        check(partTest.y() > 400f, "slides smoothly upward along partition wall");

        HabitatWorld bounds = new HabitatWorld();
        bounds.move(-1, 0, 10f); near(bounds.x(), FLOOR.x(), 0.05f, "west bound");
        bounds.move(0, -1, 10f); near(bounds.y(), FLOOR.y(), 0.05f, "south bound");
        bounds.move(1, 0, 15f); near(bounds.x(), FLOOR.right() - PLAYER_WIDTH, 0.05f, "east bound");
        bounds.move(0, 1, 15f); near(bounds.y(), FLOOR.top() - PLAYER_HEIGHT, 0.05f, "north bound");

        // Block below Bio-Dome GARDEN(960, 360, 360, 200)
        HabitatWorld garden = new HabitatWorld();
        garden.move(0, -1, 1.5f); // drops below GARDEN.y(360)
        garden.move(1, 0, 5f);    // travels east under x ~ 1050
        garden.move(0, 1, 4f);    // pushes upward against GARDEN south face
        near(garden.y(), GARDEN.y() - PLAYER_HEIGHT, 0.05f, "bio-dome garden solid from south");
    }

    private static HabitatWorld nearPanel() {
        HabitatWorld world = new HabitatWorld();
        world.move(1, 0, 1.0f);  // clear east of HAB_AIRLOCK(110..300) & SOLAR_WING(150..370)
        world.move(0, 1, 1.95f); // walk north to corridor beside PANEL(510, 680)
        world.move(1, 0, 0.55f); // approach west edge of PANEL
        world.move(0, 0, 0.45f);
        return world;
    }

    private static HabitatWorld walkToBioDomePerimeter() {
        HabitatWorld world = new HabitatWorld();
        world.move(0, -1, 0.8f); // south below partition
        world.move(1, 0, 3.6f);  // east to x ~ 900..940 approaching Bio-Dome west side
        float step = Math.max(0.05f, (GARDEN.x() - PLAYER_WIDTH - 24f - world.x()) / SPEED);
        world.move(1, 0, step);
        world.move(0, 1, 1.2f);
        return world;
    }

    private static void interactionsFocusAndRoverClick() {
        HabitatWorld world = new HabitatWorld();
        check(world.nearbyFacility() == null, "no remote interaction from spawn");
        world.interact();
        check(world.mode() == Mode.EXPLORING, "cannot interact remotely with E");

        HabitatWorld panel = nearPanel();
        check(panel.nearbyFacility() == Facility.PANEL, "panel prompt available when near");
        panel.interact();
        check(panel.mode() == Mode.INSPECTION, "E near telemetry panel opens INSPECTION");
        float px = panel.x(), py = panel.y();
        panel.move(1, 1, 1f);
        near(panel.x(), px, 0.01f, "inspection freezes X");
        near(panel.y(), py, 0.01f, "inspection freezes Y");
        panel.closePanel();
        panel.move(0, -1, 0.3f);
        check(panel.y() < py, "closing panel restores movement");

        HabitatWorld bio = walkToBioDomePerimeter();
        check(bio.nearbyFacility() == Facility.ROVER, "Bio-Dome Rover facility reachable");
        bio.interact();
        check(bio.mode() == Mode.TERMINAL, "E near Bio-Dome opens unified Rover Terminal");
        bio.closePanel();
        check(bio.mode() == Mode.EXPLORING, "close restores EXPLORING");

        // Direct Rover click hit-test in world coordinates
        Box roverBox = HabitatWorld.roverBayBox(0, 0, 2);
        check(HabitatWorld.isRoverHit(roverBox.centerX(), roverBox.centerY(), 0, 0, 2),
            "clicking center of Rover hits");
        check(!HabitatWorld.isRoverHit(120f, 120f, 0, 0, 2),
            "clicking distant Martian regolith misses Rover");
        world.openRoverTerminal();
        check(world.mode() == Mode.TERMINAL && world.currentSpeed() == 0f,
            "direct Rover click opens TERMINAL and stops movement");
    }

    private static void followCameraFramingAndClamping() {
        FollowCamera cam = new FollowCamera();
        HabitatWorld world = new HabitatWorld();
        cam.update(world, 0f, CAMERA_WIDTH, CAMERA_HEIGHT, true);
        float initX = cam.camX(), initY = cam.camY();
        check(initX >= CAMERA_WIDTH * 0.5f && initX <= WIDTH - CAMERA_WIDTH * 0.5f,
            "initial camera clamped horizontally");
        check(initY >= CAMERA_HEIGHT * 0.5f && initY <= HEIGHT - CAMERA_HEIGHT * 0.5f,
            "initial camera clamped vertically");

        // Walk right past deadzone; camera lags smoothly then follows
        world.move(1, 0, 1.5f);
        cam.update(world, 0.05f, CAMERA_WIDTH, CAMERA_HEIGHT, false);
        check(cam.camX() > initX, "follow camera moves smoothly east with player");
        check(cam.camX() < world.x(), "smooth follow does not teleport rigidly past player");

        // Force astronaut to far east edge -> camera must clamp strictly inside map
        world.move(1, 0, 10f);
        cam.update(world, 1f, CAMERA_WIDTH, CAMERA_HEIGHT, true);
        check(cam.camX() <= WIDTH - CAMERA_WIDTH * 0.5f + 0.01f,
            "camera clamps right edge cleanly with zero void");

        // Entering TERMINAL shifts camera focus to frame Bio-Dome on unobstructed right side
        world.openRoverTerminal();
        cam.update(world, 1f, CAMERA_WIDTH, CAMERA_HEIGHT, true);
        float terminalViewLeft = cam.camX() - CAMERA_WIDTH * 0.5f;
        float terminalViewRight = cam.camX() + CAMERA_WIDTH * 0.5f;
        check(GARDEN.x() >= terminalViewLeft && GARDEN.right() <= terminalViewRight,
            "smart terminal framing keeps Bio-Dome GARDEN comfortably inside viewport");
        check(GARDEN.centerX() > cam.camX(),
            "smart terminal framing places Bio-Dome on right half away from left terminal");
    }

    private static void inputEdges() {
        HabitatWorld world = nearPanel();
        HabitatInput input = new HabitatInput(world);
        input.keyDown(Keys.E);
        check(world.mode() == Mode.INSPECTION, "E edge opens");
        check(input.keyTyped('e'), "opening E does not leak to editor");
        input.keyDown(Keys.ESCAPE);
        check(world.mode() == Mode.EXPLORING, "Esc closes before UI handles key");
        input.keyDown(Keys.E);
        check(world.mode() == Mode.EXPLORING, "held E cannot reopen after Esc");
        input.keyUp(Keys.E);
        input.keyUp(Keys.ESCAPE);
        input.keyDown(Keys.E);
        check(world.mode() == Mode.INSPECTION, "new E press opens again");
        input.keyUp(Keys.E);
        check(!input.keyTyped('e'), "normal editor E remains editable");
    }

    private static void near(float actual, float expected, float eps, String message) {
        check(Math.abs(actual - expected) <= eps, message + ": " + actual + " expected " + expected);
    }

    private static void check(boolean success, String message) {
        if (!success) throw new AssertionError(message);
        passed++;
    }
}
