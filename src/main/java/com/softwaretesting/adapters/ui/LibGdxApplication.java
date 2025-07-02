package com.softwaretesting.adapters.ui;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.softwaretesting.adapters.persistence.DatabaseFactory;
import com.softwaretesting.adapters.ui.screen.LoginScreen;
import com.softwaretesting.core.domain.model.User;

import java.util.Stack;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class LibGdxApplication extends Game {

    // aplicação usa o banco de dados para persistência de usuários e simulações
    private final DatabaseFactory databaseFactory;
    private User user;    // usuário atual da aplicação, pode ser nulo se não houver um usuário logado

    // variáveis compartilhadas por toda a aplicação
    private SpriteBatch spriteBatch;
    private ShapeRenderer shapeRenderer;
    private Skin skin;
    private BitmapFont font;

    // pilha para navegação entre telas
    private final Stack<Screen> navigationStack = new Stack<>();

    public LibGdxApplication(DatabaseFactory databaseFactory) {
        this.databaseFactory = databaseFactory;
    }

    @Override
    public void create() {
        spriteBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        skin = new Skin(Gdx.files.internal("skin/plain-james-ui.json"));
        font = skin.getFont("title");

        setScreen(new LoginScreen(this));
    }

    @Override
    public void dispose() {
        // libera os recursos utilizados
        while (!navigationStack.isEmpty()) {
            navigationStack.pop().dispose();
        }
        if (getScreen() != null) {
            getScreen().dispose();
        }

        spriteBatch.dispose();
        shapeRenderer.dispose();
        skin.dispose();
        font.dispose();
        super.dispose();
    }

    /**
     * Navega para uma nova tela, empilhando a tela atual no histórico.
     * @param newScreen A nova instância da tela para a qual navegar.
     */
    public void navigateTo(Screen newScreen) {
        Screen oldScreen = getScreen();
        if (oldScreen != null) {
            navigationStack.push(oldScreen);
        }
        setScreen(newScreen);
    }

    /**
     * Volta para a tela anterior na pilha de navegação.
     * A tela atual será descartada (dispose).
     */
    public void navigateBack() {
        if (!navigationStack.isEmpty()) {
            Screen currentScreen = getScreen();
            Screen previousScreen = navigationStack.pop();

            // libera recursos da tela atual
            if (currentScreen != null) {
                currentScreen.dispose();
            }

            setScreen(previousScreen);
        } else {
            // se não houver para onde voltar, fecha a aplicação
            Gdx.app.exit();
        }
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

    public DatabaseFactory getDatabaseFactory() {
        return databaseFactory;
    }

    public User getCurrentUser() {
        return user;
    }

    public void setCurrentUser(User user) {
        this.user = user;
    }
}
