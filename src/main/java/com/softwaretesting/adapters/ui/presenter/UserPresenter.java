package com.softwaretesting.adapters.ui.presenter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.softwaretesting.adapters.ui.LibGdxApplication;
import com.softwaretesting.adapters.ui.screen.GameScreen;
import com.softwaretesting.adapters.ui.screen.LoginScreen;
import com.softwaretesting.adapters.ui.screen.RankingScreen;
import com.softwaretesting.adapters.ui.screen.StatisticsScreen;
import com.softwaretesting.adapters.ui.view.UserView;
import com.softwaretesting.core.domain.model.User;

import java.awt.*;
import java.io.IOException;
import java.sql.SQLException;

public class UserPresenter {

    private final UserView userView;
    private final LibGdxApplication application;

    public UserPresenter(UserView userView, LibGdxApplication application) {
        this.userView = userView;
        this.application = application;
    }

    /**
     * Metodo chamado quando o usuário clica no botão de ranking.
     * Fecha a tela atual do usuário e abre a tela de ranking com todos os usuários.
     */
    public void onRankingButtonClicked() {
        userView.disposeScreen();
        application.setScreen(new RankingScreen(application));
    }

    /**
     * Metodo chamado quando o usuário clica no botão de nova simulação.
     * Fecha a tela atual do usuário e abre uma tela para a nova simulação.
     */
    public void onRunNewSimulationClicked() {
        userView.disposeScreen();
        application.setScreen(new GameScreen(application));
    }

    /**
     * Metodo chamado quando o usuário clica sobre um card de simulação na lista de simulações.
     * Fecha a tela atual do usuário e abre a tela de estatísticas, com detalhes específicos da simulação selecionada.
     */
    public void onSimulationClicked(Long id) {
        userView.disposeScreen();
        application.setScreen(new StatisticsScreen(application, id));
    }

    /**
     * Metodo chamado quando o usuário clica no botão de avatar.
     * Abre um diálogo para selecionar uma imagem de avatar e atualiza o usuário com a nova imagem.
     */
    public void onAvatarClicked() {
        Frame parent = new Frame();
        FileDialog dialog = new FileDialog(parent, "Select an Avatar Image", FileDialog.LOAD);

        // define o filtro para os arquivos de imagem
        dialog.setFile("*.png;*.jpg;*.jpeg");
        dialog.setVisible(true);

        String directory = dialog.getDirectory();
        String file = dialog.getFile();

        if (file != null) {
            final String fullPath = directory + file;
            Gdx.app.postRunnable(() -> {
                FileHandle fileHandle = new FileHandle(fullPath);
                User currentUser = application.getCurrentUser();
                currentUser.setAvatarUrl(fileHandle.path());
                try {
                    application.getDatabaseFactory().getUserService().update(currentUser);
                } catch (SQLException e) {
                    throw new RuntimeException("Error updating user avatar in database", e);
                } catch (IOException e) {
                    throw new RuntimeException("Error reading avatar file", e);
                }
                userView.displayUserInfo(currentUser);
            });
        }
        parent.dispose();
    }

    /**
     * Metodo chamado quando o usuário pressiona ESC.
     * Fecha a tela do usuário e retorna para a tela de login, desconectando o usuário atual.
     */
    public void onEscPressed() {
        // fecha a tela do usuário e retorna para a tela de login
        userView.disposeScreen();
        application.setCurrentUser(null);
        application.setScreen(new LoginScreen(application));
    }
}
