package com.softwaretesting.libgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.softwaretesting.database.User;
import com.softwaretesting.database.UserService;

import java.sql.SQLException;

public class LoginScreen implements Screen {

    private final Stage stage;

    public LoginScreen(final LibGdxApplication application, final UserService userService) {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage); // permite que a UI receba eventos de input
        Skin skin = application.getSkin(); // obtém a skin da aplicação

        // cria uma tabela para organizar os elementos da UI
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // adiciona listener para sair da aplicação ao pressionar ESC
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    Gdx.app.exit();
                    return true;
                }
                return false;
            }
        });

        // cria os widgets da UI
        final Label titleLabel = new Label("Jumpy Creatures Simulator", skin, "title", Color.WHITE);
        final Label userLabel = new Label("Username:", skin, "font", Color.WHITE);
        final TextField userText = new TextField("", skin);
        final Label passwordLabel = new Label("Password:", skin, "font", Color.WHITE);
        final TextField passwordText = new TextField("", skin);
        passwordText.setPasswordMode(true);
        passwordText.setPasswordCharacter('*');
        final TextButton loginButton = new TextButton("Login", skin);
        final Label messageLabel = new Label("", skin, "font", Color.WHITE);

        // adiciona um listener ao botão de login
        loginButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String username = userText.getText();
                String password = passwordText.getText();
                try {
                    User user = new User(username, password);

                    // tenta fazer o login do usuário
                    try {
                        if (userService.login(user).isPresent()) {
                            // passa o usuário autenticado para a próxima tela
                            application.setScreen(new GameScreen(application, user));
                            dispose(); // libera recursos da tela de login
                        } else {
                            messageLabel.setText("Invalid username or password.");
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException("Database error during login", e);
                    }
                } catch (IllegalArgumentException e) {
                    messageLabel.setText("Error: " + e.getMessage());
                    messageLabel.setColor(Color.RED);
                }
            }
        });

        // adiciona os widgets à tabela
        table.add(titleLabel).colspan(2).padBottom(40);
        table.row();
        table.add(userLabel).right().padRight(10);
        table.add(userText).width(200).padBottom(10);
        table.row();
        table.add(passwordLabel).right().padRight(10);
        table.add(passwordText).width(200).padBottom(20);
        table.row();
        table.add(loginButton).colspan(2).padBottom(10);
        table.row();
        table.add(messageLabel).colspan(2);
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        // limpa a tela
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // atualiza e desenha a UI
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
    }
}
