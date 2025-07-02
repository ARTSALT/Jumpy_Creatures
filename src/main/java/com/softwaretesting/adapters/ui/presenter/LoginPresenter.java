package com.softwaretesting.adapters.ui.presenter;

import com.badlogic.gdx.Gdx;
import com.softwaretesting.adapters.ui.LibGdxApplication;
import com.softwaretesting.adapters.ui.screen.UserScreen;
import com.softwaretesting.adapters.ui.view.LoginView;
import com.softwaretesting.adapters.ui.view.View;
import com.softwaretesting.core.domain.model.User;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Apresentador de Login que contém a lógica de negócio para o processo de login e registro.
 * Ele interage com a view de login e o serviço de usuário para autenticar e registrar usuários.
 */
public class LoginPresenter {

    private final LoginView loginView;
    private final LibGdxApplication application;

    public LoginPresenter(LoginView loginView, LibGdxApplication application) {
        this.loginView = loginView;
        this.application = application;
    }

    /**
     * Metodo chamado quando o botão de login é clicado.
     * Captura o nome de usuário e senha da view, tenta autenticar o usuário
     * e, se bem-sucedido, navega para a tela do painel do usuário.
     */
    public void onLoginButtonClicked() {
        String username = loginView.getUsername();
        String password = loginView.getPassword();

        try {
            User user = new User(username, password);
            Optional<User> loggedInUser = application.getDatabaseFactory().getUserService().login(user);

            // login bem-sucedido, navega para a tela do usuário
            if (loggedInUser.isPresent()) {
                loginView.disposeScreen();
                application.setCurrentUser(loggedInUser.get());
                application.setScreen(new UserScreen(application));
            } else {
                // se falhar, mostra erro na view
                loginView.showMessage("Invalid username or password.", View.MessageType.ERROR);
            }
        } catch (IllegalArgumentException e) {
            loginView.showMessage("Error: " + e.getMessage(), View.MessageType.ERROR);
        } catch (SQLException e) {
            loginView.showMessage("Database error during login.", View.MessageType.ERROR);
            Gdx.app.error("LoginPresenter", "Database error", e);
        } catch (IOException e) {
            loginView.showMessage("Error reading user data.", View.MessageType.ERROR);
            Gdx.app.error("LoginPresenter", "IO error", e);
        } catch (Exception e) {
            loginView.showMessage("An unexpected error occurred.", View.MessageType.ERROR);
            Gdx.app.error("LoginPresenter", "Unexpected error", e);
        }
    }

    /**
     * Metodo chamado quando o botão de registro é clicado.
     * Captura o nome de usuário e senha da view, tenta registrar o usuário
     * e, se bem-sucedido, exibe uma mensagem de sucesso.
     * Usuários registrados com sucesso podem fazer login posteriormente.
     */
    public void onRegisterButtonClicked() {
        String username = loginView.getUsername();
        String password = loginView.getPassword();

        try {
            User user = new User(username, password);
            application.getDatabaseFactory().getUserService().register(user);
            loginView.showMessage("User registered successfully.", View.MessageType.SUCCESS);
        } catch (IllegalArgumentException e) {
            loginView.showMessage("Error: " + e.getMessage(), View.MessageType.ERROR);
        } catch (SQLException e) {
            loginView.showMessage("Database error during registration.", View.MessageType.ERROR);
            Gdx.app.error("LoginPresenter", "Database error", e);
        } catch (IOException e) {
            loginView.showMessage("Error reading user data.", View.MessageType.ERROR);
            Gdx.app.error("LoginPresenter", "IO error", e);
        } catch (Exception e) {
            loginView.showMessage("An unexpected error occurred.", View.MessageType.ERROR);
            Gdx.app.error("LoginPresenter", "Unexpected error", e);
        }
    }
}
