package com.softwaretesting.libgdx;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.softwaretesting.database.UserService;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class LibGdxApplication extends Game {

    // serviço de usuário para autenticação e gerenciamento de usuários
    private final UserService userService;

    // variáveis compartilhadas por todas as telas
    private SpriteBatch spriteBatch;
    private ShapeRenderer shapeRenderer;
    private Skin skin;
    private BitmapFont font;

    public LibGdxApplication(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void create() {
        spriteBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        skin = new Skin(Gdx.files.internal("skin/plain-james-ui.json"));
        font = skin.getFont("title");

        setScreen(new LoginScreen(this, userService));
    }

    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }

    public ShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }

    public Skin getSkin() {
        return skin;
    }

    public BitmapFont getFont() {
        return font;
    }
}
