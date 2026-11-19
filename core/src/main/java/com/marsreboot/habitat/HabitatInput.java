package com.marsreboot.habitat;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.marsreboot.runtime.RunCoordinator;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Input handler uniting smooth WASD exploration, [Esc]/[E] keyboard focus isolation,
 * and direct pointer click / hover on the Bio-Dome Rover in world coordinates.
 */
public final class HabitatInput extends InputAdapter {
    private final HabitatWorld world;
    private final Viewport worldViewport;
    private final HabitatUi ui;
    private final Supplier<RunCoordinator.View> viewSupplier;
    private final HabitatRenderer renderer;

    private final Set<Integer> held = new HashSet<>();
    private final Vector2 stageTmp = new Vector2();
    private final Vector3 worldTmp = new Vector3();

    private boolean suppressInteractionText;
    private boolean roverHovered;
    private boolean panelHovered;

    public HabitatInput(HabitatWorld world) {
        this(world, null, null, null, null);
    }

    public HabitatInput(
        HabitatWorld world,
        Viewport worldViewport,
        HabitatUi ui,
        Supplier<RunCoordinator.View> viewSupplier,
        HabitatRenderer renderer
    ) {
        this.world = world;
        this.worldViewport = worldViewport;
        this.ui = ui;
        this.viewSupplier = viewSupplier;
        this.renderer = renderer;
    }

    public boolean isRoverHovered() { return roverHovered; }
    public boolean isPanelHovered() { return panelHovered; }

    @Override
    public boolean keyDown(int keycode) {
        boolean fresh = held.add(keycode);
        if (keycode == Keys.ESCAPE) {
            if (fresh) {
                world.closePanel();
                roverHovered = false;
                panelHovered = false;
            }
            return true;
        }
        if (keycode == Keys.E && world.mode() == HabitatWorld.Mode.EXPLORING) {
            suppressInteractionText = true;
            if (fresh) world.interact();
            return true;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        held.remove(keycode);
        if (keycode == Keys.E) suppressInteractionText = false;
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return character == 27 || (suppressInteractionText && (character == 'e' || character == 'E'));
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button != Buttons.LEFT || worldViewport == null || world.mode() != HabitatWorld.Mode.EXPLORING) {
            return false;
        }
        if (ui != null && (ui.hasOpenOverlay() || isPointerOverUi(screenX, screenY))) {
            return false;
        }
        unprojectToWorld(screenX, screenY);
        if (checkRoverHit(worldTmp.x, worldTmp.y)) {
            held.clear();
            suppressInteractionText = false;
            roverHovered = false;
            panelHovered = false;
            world.openRoverTerminal();
            return true;
        }
        if (HabitatWorld.Facility.PANEL.bounds.contains(worldTmp.x, worldTmp.y, 14f)) {
            held.clear();
            suppressInteractionText = false;
            roverHovered = false;
            panelHovered = false;
            world.openPanelInspection();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        if (worldViewport == null || world.mode() != HabitatWorld.Mode.EXPLORING) {
            roverHovered = false;
            panelHovered = false;
            return false;
        }
        if (ui != null && (ui.hasOpenOverlay() || isPointerOverUi(screenX, screenY))) {
            roverHovered = false;
            panelHovered = false;
            return false;
        }
        unprojectToWorld(screenX, screenY);
        roverHovered = checkRoverHit(worldTmp.x, worldTmp.y);
        panelHovered = !roverHovered && HabitatWorld.Facility.PANEL.bounds.contains(worldTmp.x, worldTmp.y, 14f);
        return false;
    }

    private boolean isPointerOverUi(int screenX, int screenY) {
        stageTmp.set(screenX, screenY);
        ui.stage().screenToStageCoordinates(stageTmp);
        return ui.stage().hit(stageTmp.x, stageTmp.y, true) != null;
    }

    private void unprojectToWorld(int screenX, int screenY) {
        worldViewport.apply();
        worldTmp.set(screenX, screenY, 0f);
        worldViewport.unproject(worldTmp);
    }

    private boolean checkRoverHit(float worldX, float worldY) {
        float rx = 0f;
        float ry = 0f;
        if (renderer != null) {
            rx = renderer.visualRoverCellX();
            ry = renderer.visualRoverCellY();
        } else if (viewSupplier != null) {
            var pos = viewSupplier.get().snapshot().robot();
            rx = pos.x();
            ry = pos.y();
        }
        return HabitatWorld.isRoverHit(worldX, worldY, rx, ry, viewSupplier == null ? 2 : viewSupplier.get().puzzle().columns());
    }

    public void update(float delta) {
        int horizontal = axis(Keys.D, Keys.RIGHT) - axis(Keys.A, Keys.LEFT);
        int vertical = axis(Keys.W, Keys.UP) - axis(Keys.S, Keys.DOWN);
        world.move(horizontal, vertical, delta);
    }

    private int axis(int first, int second) { return held.contains(first) || held.contains(second) ? 1 : 0; }

    public void clear() {
        held.clear();
        suppressInteractionText = false;
        roverHovered = false;
        panelHovered = false;
    }
}
