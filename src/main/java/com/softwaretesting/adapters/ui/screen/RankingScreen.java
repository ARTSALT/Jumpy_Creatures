package com.softwaretesting.adapters.ui.screen;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.softwaretesting.adapters.ui.LibGdxApplication;
import com.softwaretesting.adapters.ui.dto.UserRankingDTO;
import com.softwaretesting.adapters.ui.presenter.RankingPresenter;
import com.softwaretesting.adapters.ui.view.RankingView;

import java.util.List;

public class RankingScreen extends ScreenTemplate implements RankingView {

    private RankingPresenter presenter;

    public RankingScreen(LibGdxApplication application) {
        super(application);

        this.presenter = new RankingPresenter(this, application);

        // adiciona listener para sair da aplicação ao pressionar ESC
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    presenter.navigateBack();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void displayRanking(List<UserRankingDTO> ranking) {
    }
}
