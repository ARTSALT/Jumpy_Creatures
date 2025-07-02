package com.softwaretesting.adapters.ui.presenter;

import com.softwaretesting.adapters.ui.LibGdxApplication;
import com.softwaretesting.adapters.ui.screen.LoginScreen;
import com.softwaretesting.adapters.ui.view.RankingView;

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

    public void onUserLogout() {
        application.setCurrentUser(null);
        application.navigateTo(new LoginScreen(application));
    }

    public void navigateBack() {
        application.navigateBack();
    }
}
