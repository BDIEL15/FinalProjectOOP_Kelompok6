package com.marsreboot.habitat;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import java.util.HashSet;
import java.util.Set;

/** First processor in the multiplexer: Esc always works, including inside TextArea. */
public final class HabitatInput extends InputAdapter {
    private final HabitatWorld world;
    private final Set<Integer> held = new HashSet<>();
    private boolean suppressInteractionText;

    public HabitatInput(HabitatWorld world) { this.world = world; }

    @Override public boolean keyDown(int keycode) {
        boolean fresh = held.add(keycode);
        if (keycode == Keys.ESCAPE) {
            if (fresh) world.closePanel();
            return true;
        }
        if (keycode == Keys.E && world.mode() == HabitatWorld.Mode.EXPLORING) {
            suppressInteractionText = true;
            if (fresh) world.interact();
            return true;
        }
        return false;
    }

    @Override public boolean keyUp(int keycode) {
        held.remove(keycode);
        if (keycode == Keys.E) suppressInteractionText = false;
        return false;
    }

    @Override public boolean keyTyped(char character) {
        return character == 27 || (suppressInteractionText && (character == 'e' || character == 'E'));
    }

    public void update(float delta) {
        int horizontal = axis(Keys.D, Keys.RIGHT) - axis(Keys.A, Keys.LEFT);
        int vertical = axis(Keys.W, Keys.UP) - axis(Keys.S, Keys.DOWN);
        world.move(horizontal, vertical, delta);
    }

    private int axis(int first, int second) { return held.contains(first) || held.contains(second) ? 1 : 0; }
    public void clear() { held.clear(); suppressInteractionText = false; }
}
