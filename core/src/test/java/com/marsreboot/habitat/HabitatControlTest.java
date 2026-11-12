package com.marsreboot.habitat;

import com.badlogic.gdx.Input.Keys;
import static com.marsreboot.habitat.HabitatWorld.*;

/** Small executable regression suite, intentionally requiring no new test/runtime dependencies. */
public final class HabitatControlTest {
    private static int passed;
    public static void main(String[] args) {
        movementAndTime();
        collisionAndBounds();
        interactionsAndFocus();
        inputEdges();
        System.out.println("PASS: " + passed + " habitat control assertions (no OpenGL).");
    }

    private static void movementAndTime() {
        HabitatWorld cardinal = new HabitatWorld();
        cardinal.move(1, 0, 0.5f);
        near(cardinal.x(), 250, "180 units/second");
        HabitatWorld diagonal = new HabitatWorld();
        diagonal.move(1, 1, 0.5f);
        near((float) Math.hypot(diagonal.x() - 160, diagonal.y() - 200), 90, "diagonal normalized");
        for (int fps : new int[] {30, 60, 144}) {
            HabitatWorld timed = new HabitatWorld();
            for (int i = 0; i < fps; i++) timed.move(1, 0, 1f / fps);
            near(timed.x(), 340, "frame independent at " + fps);
        }
        HabitatWorld aliasWorld = new HabitatWorld();
        HabitatInput input = new HabitatInput(aliasWorld);
        input.keyDown(Keys.W); input.keyDown(Keys.UP); input.update(0.5f);
        near(aliasWorld.y(), 290, "aliases do not double speed");
        input.keyDown(Keys.S); input.update(0.5f);
        near(aliasWorld.y(), 290, "opposite keys cancel");
        input.clear(); input.update(1);
        near(aliasWorld.y(), 290, "pause/hide clears held input");
        aliasWorld.move(1, 0, Float.NaN);
        near(aliasWorld.x(), 160, "invalid delta ignored");
    }

    private static void collisionAndBounds() {
        HabitatWorld world = new HabitatWorld();
        world.move(1, 0, 10);
        near(world.x(), PARTITION.x() - PLAYER_WIDTH, "cannot tunnel through partition");
        world.move(0, 1, 0.5f);
        near(world.y(), 290, "slides along wall");
        world.move(-1, 0, 100);
        near(world.x(), FLOOR.x(), "left bound");
        world.move(0, -1, 100);
        near(world.y(), FLOOR.y(), "bottom bound");
        world.move(1, 0, 100);
        near(world.x(), FLOOR.right() - PLAYER_WIDTH, "right bound");
        world.move(0, 1, 100);
        near(world.y(), FLOOR.top() - PLAYER_HEIGHT, "top bound");
        world.move(-1, 0, 100);
        world.move(0, -1, (532 - 300) / SPEED);
        world.move(1, 0, 100);
        near(world.x(), PARTITION.x() - PLAYER_WIDTH, "partition blocks horizontal sweep");
        HabitatWorld otherSide = new HabitatWorld();
        otherSide.move(0, -1, 1);
        otherSide.move(1, 0, 10);
        otherSide.move(0, 1, (300 - FLOOR.y()) / SPEED);
        otherSide.move(-1, 0, 10);
        near(otherSide.x(), PARTITION.right(), "partition blocks from right");
        HabitatWorld garden = new HabitatWorld();
        garden.move(0, -1, 1);
        garden.move(1, 0, (650 - 160) / SPEED);
        garden.move(0, 1, 10);
        near(garden.y(), GARDEN.y() - PLAYER_HEIGHT, "garden solid from below");
        HabitatWorld panel = nearPanel();
        panel.move(0, 1, 10);
        near(panel.y(), Facility.PANEL.bounds.y() - PLAYER_HEIGHT, "panel solid");
    }

    private static HabitatWorld nearPanel() {
        HabitatWorld world = new HabitatWorld();
        world.move(1, 0, (270 - 160) / SPEED);
        world.move(0, 1, (350 - 200) / SPEED);
        return world;
    }

    private static void interactionsAndFocus() {
        HabitatWorld world = new HabitatWorld();
        check(world.nearbyFacility() == null, "no far prompt");
        world.interact(); check(world.mode() == Mode.EXPLORING, "cannot interact remotely");
        world = nearPanel();
        check(world.nearbyFacility() == Facility.PANEL, "panel prompt in range");
        world.interact(); check(world.mode() == Mode.INSPECTION, "inspection opens");
        float x = world.x(), y = world.y();
        world.move(1, 1, 1);
        near(world.x(), x, "inspection blocks X"); near(world.y(), y, "inspection blocks Y");
        world.closePanel(); world.move(1, 0, 0.1f);
        near(world.x(), x + 18, "movement restored");
        world = new HabitatWorld();
        world.move(0, 1, (350 - 200) / SPEED);
        check(world.nearbyFacility() == null, "outside range still unavailable");
        world.move(0, 1, (480 - 350) / SPEED);
        world.move(1, 0, (750 - 160) / SPEED);
        world.move(0, -1, (480 - 465) / SPEED);
        check(world.nearbyFacility() == Facility.TERMINAL, "terminal reachable around partition");
        world.interact(); check(world.mode() == Mode.TERMINAL, "terminal opens");
        x = world.x(); y = world.y(); world.move(-1, -1, 1);
        near(world.x(), x, "terminal blocks X"); near(world.y(), y, "terminal blocks Y");
        world.closePanel(); world.move(-1, 0, 0.1f);
        near(world.x(), x - 18, "terminal close restores movement");
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
        input.keyUp(Keys.E); input.keyUp(Keys.ESCAPE); input.keyDown(Keys.E);
        check(world.mode() == Mode.INSPECTION, "new E press opens again");
        input.keyUp(Keys.E);
        check(!input.keyTyped('e'), "normal editor E remains editable");
        input.keyDown(Keys.D); input.update(1);
        near(world.x(), 270, "held movement blocked while modal");
        input.keyDown(Keys.ESCAPE); input.update(0.1f);
        near(world.x(), 288, "held control resumes after close");
    }

    private static void near(float actual, float expected, String message) {
        check(Math.abs(actual - expected) < 0.02f, message + ": " + actual + " expected " + expected);
    }
    private static void check(boolean success, String message) {
        if (!success) throw new AssertionError(message);
        passed++;
    }
}
