package com.softwaretesting.adapters.ui.presenter;

import com.softwaretesting.adapters.ui.LibGdxApplication;
import com.softwaretesting.adapters.ui.screen.LoginScreen;
import com.softwaretesting.adapters.ui.screen.UserScreen;
import com.softwaretesting.adapters.ui.view.RankingView;

public class RankingPresenter {

    private RankingView rankingView;
    private LibGdxApplication application;

    public RankingPresenter(RankingView rankingView, LibGdxApplication application) {
        this.rankingView = rankingView;
        this.application = application;
    }

    public void navigateBack() {
        rankingView.disposeScreen();
        application.setScreen(new UserScreen(application));
    }
}
