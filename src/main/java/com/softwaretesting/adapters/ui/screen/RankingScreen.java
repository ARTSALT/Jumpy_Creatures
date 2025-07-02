package com.softwaretesting.adapters.ui.screen;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.softwaretesting.adapters.ui.LibGdxApplication;
import com.softwaretesting.adapters.ui.dto.UserRankingDTO;
import com.softwaretesting.adapters.ui.presenter.RankingPresenter;
import com.softwaretesting.adapters.ui.view.RankingView;

import java.util.List;

/**
 * Tela de Ranking que exibe a lista de usuários ordenados por pontuação.
 * Esta tela pode ser acessada por qualquer usuário e permite que ele veja
 * o ranking dos jogadores cadastrados.
 */
public class RankingScreen extends ScreenTemplate implements RankingView {

    private final RankingPresenter presenter;

    public RankingScreen(LibGdxApplication application) {
        super(application);

        this.presenter = new RankingPresenter(this, application);

        table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Table headerTable = createHeader("Ranking");
        Table buttonGroup = (Table)(headerTable.getChild(1));
        buttonGroup.removeActor(rankingButton); // remove o botão de ranking da tela de ranking
        table.add(headerTable).growX().top();

        createListeners();
    }

    @Override
    public void displayRanking(List<UserRankingDTO> ranking) {
    }

    private void createListeners() {
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

        logoutButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                presenter.onUserLogout();
                return true;
            }
        });
    }
}
