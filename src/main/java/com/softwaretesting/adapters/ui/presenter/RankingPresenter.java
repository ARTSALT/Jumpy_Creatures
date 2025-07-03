package com.softwaretesting.adapters.ui.presenter;

import com.softwaretesting.adapters.ui.LibGdxApplication;
import com.softwaretesting.adapters.ui.dto.UserRankingDTO;
import com.softwaretesting.adapters.ui.screen.LoginScreen;
import com.softwaretesting.adapters.ui.view.RankingView;

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
        List<UserRankingDTO> data =List.of(
            new UserRankingDTO("Alice", "images/default_avatar.png", 1500, 10, "75.0"),
            new UserRankingDTO("Bob", "images/default_avatar.png", 1400, 8, "70.0"),
            new UserRankingDTO("Charlie", "images/default_avatar.png", 1300, 12, "65.0"),
            new UserRankingDTO("David", "images/default_avatar.png", 1200, 5, "60.0"),
            new UserRankingDTO("Ellie", "images/default_avatar.png", 1500, 10, "75.0"),
            new UserRankingDTO("Fabio", "images/default_avatar.png", 1400, 8, "70.0"),
            new UserRankingDTO("Gerald", "images/default_avatar.png", 1300, 12, "65.0"),
            new UserRankingDTO("Harold", "images/default_avatar.png", 1200, 5, "60.0"),
            new UserRankingDTO("Illya", "images/default_avatar.png", 1500, 10, "75.0"),
            new UserRankingDTO("Jorge", "images/default_avatar.png", 1400, 8, "70.0"),
            new UserRankingDTO("Kira", "images/default_avatar.png", 1300, 12, "65.0"),
            new UserRankingDTO("L", "images/default_avatar.png", 1200, 5, "60.0")
        );

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

        // envia os dados para a view
        rankingView.displayRanking(ranking);
        rankingView.displayTotalSimulations(100);
        rankingView.displayTotalAverageScore(75.5);
    }

    public void onUserLogout() {
        application.setCurrentUser(null);
        application.navigateTo(new LoginScreen(application));
    }

    public void navigateBack() {
        application.navigateBack();
    }
}
