package com.softwaretesting.adapters.ui.presenter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.softwaretesting.adapters.ui.LibGdxApplication;
import com.softwaretesting.adapters.ui.dto.SimulationSummaryDTO;
import com.softwaretesting.adapters.ui.screen.GameScreen;
import com.softwaretesting.adapters.ui.screen.LoginScreen;
import com.softwaretesting.adapters.ui.screen.RankingScreen;
import com.softwaretesting.adapters.ui.screen.StatisticsScreen;
import com.softwaretesting.adapters.ui.view.UserView;
import com.softwaretesting.core.application.service.SimulationService;
import com.softwaretesting.core.application.service.UserService;
import com.softwaretesting.core.domain.model.Simulation;
import com.softwaretesting.core.domain.model.User;

import java.awt.*;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class UserPresenter {

    private final UserView userView;
    private final LibGdxApplication application;

    public UserPresenter(UserView userView, LibGdxApplication application) {
        this.userView = userView;
        this.application = application;
    }

    /**
     * Carrega as informações do usuário logado e as simulações realizadas.
     * Obtém o usuário atual, suas simulações e calcula a média de sucesso.
     * Exibe as informações na tela do usuário.
     */
    public void loadUserInfo() {
        try {
            // usuário logado
            User user = application.getCurrentUser();
            // serviço dos usuários
            UserService userService = application.getDatabaseFactory().getUserService();
            // serviço das simulações
            SimulationService simulationService = application.getDatabaseFactory().getSimulationService();

            // obtém a lista de todas as simulações e as simulações bem-sucedidas do usuário
            List<Simulation> allSims = simulationService.getSimulations(user);
            List<Simulation> successfulSims = simulationService.getSuccessfulSimulations(user);

            double avgSuccess = 0.0;
            int allSimsCount = 0;
            if (allSims != null && !allSims.isEmpty()) {
                allSimsCount = allSims.size();
                if (successfulSims != null && !successfulSims.isEmpty()) {
                    avgSuccess = successfulSims.size() / (double) allSims.size() * 100;
                }
            }

            user.setScore(successfulSims != null ? successfulSims.size() : 0);
            userService.update(user);

            // exibe as informações do usuário na view
            userView.displayUserInfo(user.getUsername(), user.getScore(), allSimsCount);
            userView.setUserProfileImage(user.getAvatarUrl());
            userView.setAverageSuccessRate(avgSuccess);
            userView.displaySimulationList(allSims != null ? allSims.stream()
                .map(simulation -> new SimulationSummaryDTO(
                    simulation.getId(),
                    simulation.getName(),
                    simulation.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
                    simulation.getInitialNumCreatures(),
                    simulation.getIterations(),
                    simulation.isSuccessful()))
                .toList() : null);
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching successful simulations", e);
        } catch (IOException e) {
            throw new RuntimeException("Error reading user avatar", e);
        }
    }

    /**
     * Metodo chamado quando o usuário clica no botão de ranking.
     * Fecha a tela atual do usuário e abre a tela de ranking com todos os usuários.
     */
    public void onRankingButtonClicked() {
        application.navigateTo(new RankingScreen(application));
    }

    /**
     * Metodo chamado quando o usuário clica no botão de nova simulação.
     * Fecha a tela atual do usuário e abre uma tela para a nova simulação.
     */
    public void onRunNewSimulationClicked() {
        application.navigateTo(new GameScreen(application));
    }

    /**
     * Metodo chamado quando o usuário clica sobre um card de simulação na lista de simulações.
     * Fecha a tela atual do usuário e abre a tela de estatísticas, com detalhes específicos da simulação selecionada.
     */
    public void onSimulationClicked(Long id) {
        application.navigateTo(new StatisticsScreen(application, id));
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
                userView.setUserProfileImage(fileHandle.path());
            });
        }
        parent.dispose();
    }

    /**
     * Metodo chamado quando o usuário pressiona ESC ou pressiona o botão de logout.
     * Fecha a tela do usuário e retorna para a tela de login, desconectando o usuário atual.
     */
    public void onUserLogout() {
        // fecha a tela do usuário e retorna para a tela de login
        application.setCurrentUser(null);
        application.navigateTo(new LoginScreen(application));
    }
}
