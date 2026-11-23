package com.marsreboot;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle;
import com.badlogic.gdx.utils.Align;

/** Modal confirmation shown before closing the game window. */
public final class ExitConfirmationDialog extends Dialog {

    public ExitConfirmationDialog(
            WindowStyle windowStyle,
            LabelStyle messageStyle,
            TextButtonStyle continueStyle,
            TextButtonStyle exitStyle) {
        super("KELUAR DARI GAME?", windowStyle);

        getTitleLabel().setColor(Color.valueOf("38BDF8"));

        Label message = new Label(
                "Skor yang sudah tersimpan tetap aman.\nYakin mau keluar?",
                messageStyle);
        message.setAlignment(Align.center);
        message.setWrap(true);
        getContentTable().add(message).width(420f).pad(18f);

        getButtonTable().defaults().height(42f).pad(8f, 6f, 10f, 6f);
        button("Lanjut bermain", Boolean.FALSE, continueStyle);
        button("Keluar", Boolean.TRUE, exitStyle);
        key(Keys.ESCAPE, Boolean.FALSE);

        setModal(true);
        setMovable(false);
        setResizable(false);
        setKeepWithinStage(true);
        setSize(500f, 205f);
    }

    public void open(Stage stage) {
        if (hasParent()) {
            toFront();
            return;
        }

        show(stage);
        if (getButtonTable().getChildren().size > 0) {
            stage.setKeyboardFocus(getButtonTable().getChildren().first());
        }
    }

    @Override
    protected void result(Object result) {
        if (Boolean.TRUE.equals(result)) {
            Gdx.app.exit();
        }
    }
}
