package com.marsreboot;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.marsreboot.habitat.ScoreBackendClient;
import com.marsreboot.habitat.HabitatScreen;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class MarsRebootGame extends Game {
    private final ScoreBackendClient scoreBackend = new ScoreBackendClient();

    @Override
    public void create() {
        setScreen(new TitleScreen(this::showHome));
    }

    /** Title screen -> Home (login/tamu/setelan) -> Habitat. */
    private void showHome() {
        Screen title = getScreen();
        setScreen(new HomeScreen(this, scoreBackend));
        if (title != null) title.dispose();
    }

    public void startMission() {
        Screen oldScreen = getScreen();
        setScreen(new HabitatScreen(scoreBackend));

        if (oldScreen != null) {
            oldScreen.dispose();
        }
    }

    public void requestExitConfirmation() {
        Gdx.app.postRunnable(() -> {
            if (getScreen() instanceof ExitConfirmationHost host) {
                host.showExitConfirmation();
            } else {
                Gdx.app.exit();
            }
        });
    }

    @Override
    public void dispose() {
        if (screen != null) {
            screen.hide();
            screen.dispose();
        }
    }
}
