package com.marsreboot;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.marsreboot.habitat.HabitatUi;
import com.marsreboot.habitat.ScoreBackendClient;
import java.util.List;

/** Halaman awal untuk masuk, memilih setelan, atau memulai permainan. */
public final class HomeScreen extends ScreenAdapter {
    private static final Color CYAN = Color.valueOf("38BDF8");
    private static final Color WHITE = Color.valueOf("F8FAFC");
    private static final Color MUTED = Color.valueOf("CBD5E1");

    private final MarsRebootGame game;
    private final ScoreBackendClient scoreBackend;
    private final Stage stage = new Stage(new ExtendViewport(960f, 600f));
    private final Skin skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
    private final BitmapFont bodyFont = loadFont("ui/ui_sans.fnt", 0.50f);
    private final BitmapFont titleFont = loadFont("ui/ui_sans_bold.fnt", 0.68f);
    private final TextField usernameField;
    private final TextField passwordField;
    private final Label statusLabel;
    private final Label boardStatusLabel;
    private final Table leaderboardRows = new Table();
    private final TextButton loginButton;
    private final TextButton registerButton;
    private final TextButton logoutButton;
    private final TextButton startButton;
    private final TextButton motionButton;
    private final TextButton contrastButton;
    private final TextButton previewButton;

    public HomeScreen(MarsRebootGame game, ScoreBackendClient scoreBackend) {
        this.game = game;
        this.scoreBackend = scoreBackend;
        HabitatUi.loadSettings();

        TextFieldStyle fieldStyle = new TextFieldStyle(skin.get(TextFieldStyle.class));
        fieldStyle.font = bodyFont;
        fieldStyle.fontColor = WHITE;
        fieldStyle.focusedFontColor = WHITE;
        usernameField = new TextField("", fieldStyle);
        usernameField.setMessageText("Nama akun (3–32 karakter)");
        usernameField.setMaxLength(32);
        passwordField = new TextField("", fieldStyle);
        passwordField.setMessageText("Kata sandi (minimal 8 karakter)");
        passwordField.setMaxLength(72);
        passwordField.setPasswordMode(true);
        passwordField.setPasswordCharacter('*');

        loginButton = button("Masuk", true);
        registerButton = button("Daftar", false);
        logoutButton = button("Keluar akun", false);
        startButton = button("Mulai misi", true);
        motionButton = button("", false);
        contrastButton = button("", false);
        previewButton = button("", false);

        statusLabel = label("Masuk untuk menyimpan skor. Kamu juga bisa bermain sebagai tamu.", MUTED, bodyFont);
        statusLabel.setWrap(true);
        boardStatusLabel = label("Tekan Muat Skor untuk melihat pemain teratas.", MUTED, bodyFont);
        boardStatusLabel.setWrap(true);

        buildPage();
        refreshAccountButtons();
        refreshSettingButtons();
    }

    private void buildPage() {
        Table root = new Table();
        root.setFillParent(true);
        root.setBackground(box(Color.valueOf("0A111CEB")));
        root.pad(22f, 28f, 18f, 28f);
        stage.addActor(root);

        Table heading = new Table();
        heading.add(label("MARS REBOOT", CYAN, titleFont)).left().row();
        heading.add(label("Pulihkan habitat Mars, satu perintah pada satu waktu.", MUTED, bodyFont)).left();
        root.add(heading).growX().left().padBottom(16f).row();

        Table main = new Table();
        main.add(makeAccountCard()).grow().minWidth(360f).padRight(12f);
        main.add(makeLeaderboardCard()).grow().minWidth(360f);
        root.add(main).grow().row();

        Table settings = new Table();
        settings.setBackground(box(Color.valueOf("111C2BEF")));
        settings.pad(10f, 12f, 10f, 12f);
        settings.add(label("SETELAN", CYAN, bodyFont)).left().padRight(12f);
        settings.add(motionButton).growX().height(32f).padRight(6f);
        settings.add(contrastButton).growX().height(32f).padRight(6f);
        settings.add(previewButton).growX().height(32f);
        root.add(settings).growX().padTop(12f);
    }

    private Table makeAccountCard() {
        Table card = card();
        card.add(label("AKUN PEMAIN", CYAN, bodyFont)).left().growX().row();
        Label accountHint = label("Masuk untuk menyimpan hasil. Belum punya akun? Daftar di sini.", MUTED, bodyFont);
        accountHint.setWrap(true);
        card.add(accountHint)
                .growX().minWidth(0).left().padTop(8f).padBottom(12f).row();
        card.add(usernameField).growX().height(38f).padBottom(8f).row();
        card.add(passwordField).growX().height(38f).padBottom(10f).row();

        Table accountButtons = new Table();
        accountButtons.add(loginButton).growX().height(36f).padRight(6f);
        accountButtons.add(registerButton).growX().height(36f);
        card.add(accountButtons).growX().row();
        card.add(logoutButton).growX().height(32f).padTop(6f).row();

        card.add(statusLabel).growX().minWidth(0).left().padTop(10f).padBottom(10f).row();
        card.add(startButton).growX().height(42f).padBottom(6f).row();
        card.add(label("Tanpa akun? Mulai tetap bisa, skor tidak disimpan.", MUTED, bodyFont))
                .growX().minWidth(0).left();

        loginButton.addListener(onClick(this::login));
        registerButton.addListener(onClick(this::register));
        logoutButton.addListener(onClick(this::logout));
        startButton.addListener(onClick(game::startMission));
        motionButton.addListener(onClick(() -> {
            HabitatUi.setReducedMotion(!HabitatUi.isReducedMotion());
            refreshSettingButtons();
        }));
        contrastButton.addListener(onClick(() -> {
            HabitatUi.setHighContrastFocus(!HabitatUi.isHighContrastFocus());
            refreshSettingButtons();
        }));
        previewButton.addListener(onClick(() -> {
            HabitatUi.setOutpostRestored(!HabitatUi.isOutpostRestored());
            refreshSettingButtons();
        }));
        return card;
    }

    private Table makeLeaderboardCard() {
        Table card = card();
        Table titleRow = new Table();
        titleRow.add(label("PAPAN SKOR P1", CYAN, bodyFont)).growX().left();
        TextButton refreshButton = button("Muat Skor", false);
        refreshButton.addListener(onClick(this::loadLeaderboard));
        titleRow.add(refreshButton).height(32f).right();
        card.add(titleRow).growX().row();
        card.add(label("10 skor terbaik • jumlah aksi paling sedikit menang", MUTED, bodyFont))
                .growX().left().padTop(8f).padBottom(10f).row();

        leaderboardRows.add(boardStatusLabel).growX().minWidth(0).left().pad(8f).row();
        ScrollPane list = new ScrollPane(leaderboardRows, skin);
        list.setFadeScrollBars(false);
        card.add(list).grow().minHeight(210f);
        return card;
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Isi nama akun dan kata sandi dulu.");
            return;
        }

        try {
            scoreBackend.login(username, password);
            passwordField.setText("");
            statusLabel.setText("Berhasil masuk sebagai " + scoreBackend.getUsername() + ".");
            refreshAccountButtons();
        } catch (Exception error) {
            statusLabel.setText("Gagal masuk: " + error.getMessage());
        }
    }

    private void register() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Isi nama akun dan kata sandi dulu.");
            return;
        }

        try {
            scoreBackend.register(username, password);
            statusLabel.setText("Akun berhasil dibuat. Sekarang tekan Masuk.");
        } catch (Exception error) {
            statusLabel.setText("Gagal daftar: " + error.getMessage());
        }
    }

    private void logout() {
        try {
            scoreBackend.logout();
            statusLabel.setText("Kamu sudah keluar dari akun.");
            refreshAccountButtons();
        } catch (Exception error) {
            statusLabel.setText("Gagal keluar: " + error.getMessage());
        }
    }

    private void loadLeaderboard() {
        try {
            List<String> rows = scoreBackend.getLeaderboard();
            leaderboardRows.clearChildren();

            if (rows.isEmpty()) {
                leaderboardRows.add(label("Belum ada skor P1 tersimpan.", MUTED, bodyFont))
                        .growX().left().pad(8f);
                return;
            }

            for (String row : rows) {
                Label scoreRow = label(row, WHITE, bodyFont);
                leaderboardRows.add(scoreRow).growX().left().pad(8f).row();
            }
        } catch (Exception error) {
            boardStatusLabel.setText("Papan skor belum bisa dimuat: " + error.getMessage());
            leaderboardRows.clearChildren();
            leaderboardRows.add(boardStatusLabel).growX().minWidth(0).left().pad(8f);
        }
    }

    private void refreshAccountButtons() {
        boolean loggedIn = scoreBackend.isLoggedIn();
        loginButton.setDisabled(loggedIn);
        registerButton.setDisabled(loggedIn);
        logoutButton.setDisabled(!loggedIn);
        usernameField.setDisabled(loggedIn);
        passwordField.setDisabled(loggedIn);
        startButton.setText(loggedIn ? "Mulai misi sebagai " + scoreBackend.getUsername() : "Mulai misi");
    }

    private void refreshSettingButtons() {
        motionButton.setText("Kurangi animasi: " + onOff(HabitatUi.isReducedMotion()));
        contrastButton.setText("Fokus kontras: " + onOff(HabitatUi.isHighContrastFocus()));
        previewButton.setText("Preview habitat: " + onOff(HabitatUi.isOutpostRestored()));
    }

    private String onOff(boolean value) {
        return value ? "AKTIF" : "MATI";
    }

    private Table card() {
        Table table = new Table();
        table.setBackground(box(Color.valueOf("111C2BEF")));
        table.pad(18f);
        return table;
    }

    private TextButton button(String text, boolean primary) {
        TextButtonStyle style = new TextButtonStyle(skin.get(TextButtonStyle.class));
        style.font = bodyFont;
        style.fontColor = primary ? Color.valueOf("071827") : WHITE;
        style.downFontColor = style.fontColor;
        style.overFontColor = style.fontColor;
        style.disabledFontColor = Color.valueOf("718096");
        style.up = box(primary ? CYAN : Color.valueOf("26364B"));
        style.over = box(primary ? Color.valueOf("7DD3FC") : Color.valueOf("344A64"));
        style.down = box(primary ? Color.valueOf("0284C7") : Color.valueOf("1B293B"));
        style.disabled = box(Color.valueOf("1A2432"));
        return new TextButton(text, style);
    }

    private Label label(String text, Color color, BitmapFont font) {
        Label.LabelStyle style = new Label.LabelStyle(font, color);
        Label label = new Label(text, style);
        label.setAlignment(Align.left);
        return label;
    }

    private Drawable box(Color color) {
        return skin.newDrawable("white", color);
    }

    private BitmapFont loadFont(String path, float scale) {
        BitmapFont font = new BitmapFont(Gdx.files.internal(path));
        font.setUseIntegerPositions(false);
        font.getData().setScale(scale);
        return font;
    }

    private static ChangeListener onClick(Runnable action) {
        return new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                action.run();
            }
        };
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        stage.getViewport().apply();
        stage.act(Math.min(delta, 0.1f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void hide() {
        if (Gdx.input.getInputProcessor() == stage) {
            Gdx.input.setInputProcessor(null);
        }
    }

    @Override
    public void dispose() {
        hide();
        stage.dispose();
        skin.dispose();
        bodyFont.dispose();
        titleFont.dispose();
    }
}
