package com.softwaretesting.adapters.ui.presenter;

import com.softwaretesting.adapters.ui.LibGdxApplication;
import com.softwaretesting.adapters.ui.dto.UserRankingDTO;
import com.softwaretesting.adapters.ui.screen.LoginScreen;
import com.softwaretesting.adapters.ui.view.RankingView;
import com.softwaretesting.core.application.service.SimulationService;
import com.softwaretesting.core.application.service.UserService;
import com.softwaretesting.core.domain.model.Simulation;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Lógica de negócio para a tela de ranking.
 */
public class RankingPresenter {

    private final RankingView rankingView;
    private final LibGdxApplication application;

    public RankingPresenter(RankingView rankingView, LibGdxApplication application) {
        this.rankingView = rankingView;
        this.application = application;
    }

    public void loadRanking() {
        // obtém os dados de ranking de usuários
        try {
            UserService userService = application.getDatabaseFactory().getUserService();
            SimulationService simulationService = application.getDatabaseFactory().getSimulationService();

            List<UserRankingDTO> data = userService.getAllUsers().stream()
                .map(user -> {
                    try {
                        // cria o DTO de ranking do usuário
                        return new UserRankingDTO(
                            user.getUsername(),
                            user.getAvatarUrl(),
                            user.getScore(),
                            simulationService.getUserSimulationCount(user.getId()),
                            String.format("%.2f%%", userService.getUserAverageScore(simulationService, user))
                        );
                    } catch (SQLException e) {
                        throw new RuntimeException("Error fetching user data", e);
                    }
                })
                .filter(user -> !user.name().equals("admin")) // filtra usuário admin
                .toList();

            // ordena a lista de usuários por pontuação em ordem decrescente
            List<UserRankingDTO> sortedByScore = data.stream()
                .sorted(Comparator.comparingInt(UserRankingDTO::score).reversed())
                .toList();

            // adiciona a posição de cada usuário no ranking
            List<UserRankingDTO> ranking = new ArrayList<>();
            AtomicInteger rank = new AtomicInteger(1);
            sortedByScore.forEach(currentUser ->
                ranking.add(new UserRankingDTO(currentUser, rank.getAndIncrement()))
            );

            // calcula o total de simulações e o número de simulações bem-sucedidas
            var allSimulations = simulationService.getAllSimulations();
            int totalSimulations = allSimulations.isEmpty() ? 1 : allSimulations.size();
            double totalWellSucceededSimulations = allSimulations.stream()
                .filter(Simulation::isSuccessful)
                .count();

            // calcula a média de pontuação total
            double totalAverageScore = totalWellSucceededSimulations / totalSimulations * 100;

            // envia os dados para a view
            rankingView.displayRanking(ranking);
            rankingView.displayTotalSimulations(simulationService.getAllSimulations().size());
            rankingView.displayTotalAverageScore(totalAverageScore);
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getUserButton() {
        // retorna texto do botão conforme o tipo de usuário logado
        return application.getCurrentUser().isAdmin() ? "Admin Panel" : "User Profile";
    }

    public void onUserLogout() {
        application.setCurrentUser(null);
        application.navigateTo(new LoginScreen(application));
    }

    public void navigateBack() {
        application.navigateBack();
    }
}
