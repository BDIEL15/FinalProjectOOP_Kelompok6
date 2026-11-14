package com.marsreboot.habitat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.marsreboot.runtime.PythonRuntime;
import com.marsreboot.runtime.RunCoordinator;

/**
 * Lifecycle, smooth follow camera framing, world click-to-rover routing, and UI layering.
 */
public final class HabitatScreen extends ScreenAdapter {
    private final HabitatWorld world = new HabitatWorld();
    private final ExtendViewport viewport = new ExtendViewport(HabitatWorld.CAMERA_WIDTH, HabitatWorld.CAMERA_HEIGHT);
    private final FollowCamera followCamera = new FollowCamera();
    private final HabitatRenderer renderer = new HabitatRenderer();
    private final RunCoordinator coordinator = new RunCoordinator(PythonRuntime.bundled());
    private final HabitatUi ui = new HabitatUi(world, coordinator);
    private final HabitatInput input = new HabitatInput(world, viewport, ui, coordinator::view, renderer);
    private final InputMultiplexer processors = new InputMultiplexer(input, ui.stage());

    @Override
    public void show() {
        Gdx.input.setInputProcessor(processors);
        followCamera.update(world, 0f, viewport.getWorldWidth(), viewport.getWorldHeight(), true);
    }

    @Override
    public void render(float delta) {
        input.update(delta);
        coordinator.update(delta, HabitatUi.isReducedMotion());
        ui.sync();
        followCamera.update(world, delta, viewport.getWorldWidth(), viewport.getWorldHeight(), HabitatUi.isReducedMotion());
        viewport.getCamera().position.set(followCamera.camX(), followCamera.camY(), 0f);
        viewport.getCamera().update();

        ScreenUtils.clear(0.14f, 0.07f, 0.05f, 1f);
        renderer.draw(
            world,
            viewport,
            coordinator.view(),
            delta,
            input.isRoverHovered(),
            input.isPanelHovered(),
            followCamera
        );
        ui.draw(delta);
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        viewport.update(width, height, false);
        followCamera.update(world, 0f, viewport.getWorldWidth(), viewport.getWorldHeight(), true);
        viewport.getCamera().position.set(followCamera.camX(), followCamera.camY(), 0f);
        viewport.getCamera().update();
        ui.resize(width, height);
    }

    @Override
    public void pause() { input.clear(); }

    @Override
    public void hide() {
        input.clear();
        coordinator.close();
        if (Gdx.input.getInputProcessor() == processors) {
            Gdx.input.setInputProcessor(null);
        }
        ui.stage().unfocusAll();
    }

    public RunCoordinator.View puzzleView() { return coordinator.view(); }

    @Override
    public void dispose() {
        hide();
        ui.dispose();
        renderer.dispose();
    }
}
