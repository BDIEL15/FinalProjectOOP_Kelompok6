package com.marsreboot;

import com.badlogic.gdx.Game;
import com.marsreboot.habitat.HabitatScreen;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class MarsRebootGame extends Game {
    @Override
    public void create() {
        setScreen(new HabitatScreen());
    }

    @Override
    public void dispose() {
        if (screen != null) {
            screen.hide();
            screen.dispose();
        }
    }
}
