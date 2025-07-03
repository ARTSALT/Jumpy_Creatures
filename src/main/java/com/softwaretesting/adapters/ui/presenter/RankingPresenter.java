package com.softwaretesting.adapters.ui.presenter;

import com.softwaretesting.adapters.ui.LibGdxApplication;
import com.softwaretesting.adapters.ui.dto.UserRankingDTO;
import com.softwaretesting.adapters.ui.screen.LoginScreen;
import com.softwaretesting.adapters.ui.view.RankingView;

import java.util.List;

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
        List<UserRankingDTO> ranking = List.of(
            new UserRankingDTO(1, "Alice", "images/default_avatar.png", 1500, 10, "75.0"),
            new UserRankingDTO(2, "Bob", "images/default_avatar.png", 1400, 8, "70.0"),
            new UserRankingDTO(3, "Charlie", "images/default_avatar.png", 1300, 12, "65.0"),
            new UserRankingDTO(4, "David", "images/default_avatar.png", 1200, 5, "60.0")
        );

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
