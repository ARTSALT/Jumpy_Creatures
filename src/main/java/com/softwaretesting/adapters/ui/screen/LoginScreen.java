package com.softwaretesting.adapters.ui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.softwaretesting.adapters.ui.LibGdxApplication;
import com.softwaretesting.adapters.ui.presenter.LoginPresenter;
import com.softwaretesting.adapters.ui.view.LoginView;

/**
 * Tela de login da aplicação, onde o usuário pode inserir seu nome de usuário e senha.
 * Possui botões para realizar o login e cadastrar um novo usuário.
 * Implementa a interface LoginView para capturar as informações do usuário.
 */
public class LoginScreen extends ScreenTemplate implements LoginView {

    private final TextField userText;
    private final TextField passwordText;
    private final LoginPresenter presenter;

    public LoginScreen(final LibGdxApplication application) {
        super(application); // chama o construtor de ScreenTemplate para inicializar os componentes comuns

        // inicializa o presenter com a view e aplicação
        this.presenter = new LoginPresenter(this, application);

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

        // cria os widgets de login, como título, campo de nome de usuário, senha e botões
        final Label titleLabel = new Label("Jumpy Creatures Simulator", skin, "title", Color.WHITE);
        final Label userLabel = new Label("Username:", skin, "font", Color.WHITE);
        final Label passwordLabel = new Label("Password:", skin, "font", Color.WHITE);

        userText = new TextField("", skin);
        passwordText = new TextField("", skin);
        passwordText.setPasswordMode(true);
        passwordText.setPasswordCharacter('*');

        // botões de login e cadastro
        final TextButton loginButton = new TextButton("Login", skin);
        final TextButton registerButton = new TextButton("Register", skin);

        // adiciona listener para capturar o clique no botão de login
        loginButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                presenter.onLoginButtonClicked();
            }
        });

        // adiciona listener para capturar o clique no botão de cadastro
        registerButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                presenter.onRegisterButtonClicked();
            }
        });

        // adiciona listener para capturar o Enter pressionado no campo de senha
        passwordText.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ENTER) {
                    presenter.onLoginButtonClicked();
                    return true;
                }
                return false;
            }
        });

        // adiciona os widgets à tabela
        table.add(titleLabel).colspan(2).padBottom(100);
        table.row();
        table.add(userLabel).right().padRight(10);
        table.add(userText).width(200).padBottom(10);
        table.row();
        table.add(passwordLabel).right().padRight(10);
        table.add(passwordText).width(200).padBottom(20);
        table.row();
        table.add(loginButton).colspan(2).padTop(20);
        table.row();
        table.add(registerButton).colspan(2).padTop(20).padBottom(20);
        table.row();
        table.add(messageLabel).colspan(2);
    }

    @Override
    public String getUsername() {
        return userText.getText();
    }

    @Override
    public String getPassword() {
        return passwordText.getText();
    }
}
