package com.marsreboot.habitat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** Lifecycle and wiring; UI and world each own their viewport/resources. */
public final class HabitatScreen extends ScreenAdapter {
    private final HabitatWorld world = new HabitatWorld();
    private final HabitatInput input = new HabitatInput(world);
    private final HabitatRenderer renderer = new HabitatRenderer();
    private final FitViewport viewport = new FitViewport(HabitatWorld.WIDTH, HabitatWorld.HEIGHT);
    private final HabitatUi ui = new HabitatUi(world);
    private final InputMultiplexer processors = new InputMultiplexer(input, ui.stage());

    @Override public void show() { Gdx.input.setInputProcessor(processors); }

    @Override public void render(float delta) {
        input.update(delta);
        ui.sync();
        ScreenUtils.clear(0.18f, 0.10f, 0.08f, 1);
        renderer.draw(world, viewport);
        ui.draw(delta);
    }

    @Override public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        viewport.update(width, height, true);
        ui.resize(width, height);
    }

    @Override public void pause() { input.clear(); }
    @Override public void hide() {
        input.clear();
        if (Gdx.input.getInputProcessor() == processors) Gdx.input.setInputProcessor(null);
        ui.stage().unfocusAll();
    }
    @Override public void dispose() { hide(); ui.dispose(); renderer.dispose(); }
}
