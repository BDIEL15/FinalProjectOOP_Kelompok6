package com.marsreboot;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

/**
 * Short title/intro screen: Mars horizon, three lines of story, then hands over to the game.
 * Pure presentation; owns no game state and is skipped by the desktop tests (they build HabitatScreen directly).
 */
public final class TitleScreen extends ScreenAdapter {
    private static final Color SKY_TOP = Color.valueOf("0B0F1A");
    private static final Color SKY_LOW = Color.valueOf("3A1810");
    private static final Color CYAN = Color.valueOf("22D3EE");
    private static final Color TEXT = Color.valueOf("F8FAFC");
    private static final Color MUTED = Color.valueOf("CBD5E1");
    private static final String[] STORY = {
        "Badai debu memutus komunikasi. Habitat Anda rusak dan kebunnya layu.",
        "Satu hal masih menyala: rover pendamping, yang menurut perintah Python.",
        "Tulis kode. Pulihkan makanan, air, lalu pompa utama."
    };
    private static final float HORIZON_Y = 190f;
    private static final float STORY_STEP = 1.4f;

    private final Runnable onStart;
    private final ExtendViewport viewport = new ExtendViewport(960f, 600f);
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final GlyphLayout layout = new GlyphLayout();
    private final float[][] stars = new float[70][3];
    private Texture fontTexture;
    private BitmapFont font;
    private Texture soil;
    private Texture roverTexture;
    private float time;
    private boolean started;

    public TitleScreen(Runnable onStart) {
        this.onStart = onStart;
        java.util.Random rnd = new java.util.Random(7L);
        for (float[] s : stars) {
            s[0] = rnd.nextFloat();
            s[1] = rnd.nextFloat();
            s[2] = rnd.nextFloat() * MathUtils.PI2;
        }
        FileHandle fnt = resolve("ui/ui_sans_bold.fnt");
        FileHandle png = resolve("ui/ui_sans_bold.png");
        if (fnt.exists() && png.exists()) {
            fontTexture = new Texture(png, true);
            fontTexture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
            font = new BitmapFont(fnt, new TextureRegion(fontTexture), false);
            font.setUseIntegerPositions(false);
        } else {
            font = new BitmapFont();
        }
        FileHandle soilFile = resolve("habitat/mars_soil_0.png");
        if (soilFile.exists()) {
            soil = new Texture(soilFile, false);
            soil.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        }
        FileHandle roverFile = resolve("habitat/mars_rover.png");
        if (roverFile.exists()) {
            roverTexture = new Texture(roverFile, false);
            roverTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        }
    }

    private static FileHandle resolve(String path) {
        FileHandle handle = Gdx.files.internal(path);
        return handle.exists() ? handle : Gdx.files.local("assets/" + path);
    }

    /** Number of story lines visible at a given time since the screen opened. Pure, for testing. */
    static int visibleStoryLines(float seconds) {
        return Math.max(0, Math.min(STORY.length, (int) (seconds / STORY_STEP)));
    }

    private void start() {
        if (started) return;
        started = true;
        onStart.run();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
                    start();
                    return true;
                }
                return false;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                start();
                return true;
            }
        });
    }

    @Override
    public void render(float delta) {
        time += Math.min(delta, 0.1f);
        viewport.apply(true);
        float w = viewport.getWorldWidth();
        float h = viewport.getWorldHeight();
        ScreenUtils.clear(SKY_TOP);

        shapes.setProjectionMatrix(viewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        // Sky gradient from the horizon upward in stepped bands (pixel-art friendly)
        int bands = 12;
        for (int i = 0; i < bands; i++) {
            float t = i / (float) (bands - 1);
            shapes.setColor(SKY_LOW.cpy().lerp(SKY_TOP, t));
            float y0 = HORIZON_Y + (h - HORIZON_Y) * i / bands;
            shapes.rect(0f, y0, w, (h - HORIZON_Y) / bands + 1f);
        }
        for (float[] s : stars) {
            float twinkle = 0.45f + 0.55f * (0.5f + 0.5f * MathUtils.sin(time * 1.6f + s[2]));
            shapes.setColor(1f, 1f, 1f, twinkle);
            float sx = s[0] * w;
            float sy = HORIZON_Y + 60f + s[1] * (h - HORIZON_Y - 60f);
            shapes.rect(sx, sy, 3f, 3f);
        }
        shapes.setColor(Color.valueOf("5A2D22"));
        shapes.rect(0f, 0f, w, HORIZON_Y);
        shapes.end();

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        if (soil != null) {
            for (float x = 0f; x < w; x += 64f) {
                for (float y = 0f; y < HORIZON_Y; y += 64f) {
                    batch.draw(soil, x, y, 64f, 64f);
                }
            }
        }
        if (roverTexture != null) {
            float bob = MathUtils.sin(time * 2.2f) * 2f;
            batch.draw(roverTexture, w * 0.5f + 150f, 70f + bob, 108f, 96f);
        }

        font.getData().setScale(1.0f);
        font.setColor(TEXT);
        drawCentered("mars_reboot.py", w * 0.5f, h - 70f);
        font.getData().setScale(0.50f);
        font.setColor(CYAN);
        drawCentered("Program robotmu. Pulihkan habitat.", w * 0.5f, h - 135f);

        font.getData().setScale(0.42f);
        font.setColor(MUTED);
        int lines = visibleStoryLines(time);
        for (int i = 0; i < lines; i++) {
            float age = time - i * STORY_STEP;
            font.setColor(MUTED.r, MUTED.g, MUTED.b, Math.min(1f, age / 0.5f));
            drawCentered(STORY[i], w * 0.5f, h - 235f - i * 42f);
        }

        if (lines >= STORY.length) {
            float blink = 0.6f + 0.4f * MathUtils.sin(time * 4f);
            font.getData().setScale(0.52f);
            font.setColor(CYAN.r, CYAN.g, CYAN.b, blink);
            drawCentered("[Enter] atau klik untuk mulai", w * 0.5f, HORIZON_Y + 50f);
        } else {
            font.getData().setScale(0.36f);
            font.setColor(MUTED.r, MUTED.g, MUTED.b, 0.85f);
            drawCentered("[Enter] lewati", w * 0.5f, HORIZON_Y + 50f);
        }
        font.setColor(Color.WHITE);
        font.getData().setScale(1f);
        batch.end();
    }

    private void drawCentered(String text, float cx, float y) {
        layout.setText(font, text);
        font.draw(batch, text, cx - layout.width * 0.5f, y);
    }

    @Override
    public void resize(int width, int height) {
        if (width > 0 && height > 0) viewport.update(width, height, true);
    }

    @Override
    public void hide() {
        if (Gdx.input != null) Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        shapes.dispose();
        batch.dispose();
        font.dispose();
        if (fontTexture != null) fontTexture.dispose();
        if (soil != null) soil.dispose();
        if (roverTexture != null) roverTexture.dispose();
    }
}
