package com.softwaretesting.adapters.ui.screen;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.softwaretesting.adapters.ui.LibGdxApplication;
import com.softwaretesting.adapters.ui.dto.UserRankingDTO;
import com.softwaretesting.adapters.ui.presenter.RankingPresenter;
import com.softwaretesting.adapters.ui.view.RankingView;

import java.util.List;

public class RankingScreen extends ScreenTemplate implements RankingView {

    private final RankingPresenter presenter;
    private final Table rankingTable;
    private final Table footerTable;

    public RankingScreen(LibGdxApplication application) {
        super(application);
        this.presenter = new RankingPresenter(this, application);

        table.clear();

        // cabeçalho principal
        Table screenHeader = createHeader("Ranking");
        Table buttonGroup = (Table) screenHeader.getChild(1);
        buttonGroup.removeActor(rankingButton);
        table.add(screenHeader).growX();
        table.row();

        // cabeçalho da lista
        Table listHeader = createListHeader();
        table.add(listHeader).growX().pad(22).padBottom(5).padTop(10);
        table.row();

        // área de conteúdo
        rankingTable = new Table();
        rankingTable.setBackground(skin.getDrawable("round-dark-gray"));
        ScrollPane scrollPane = new ScrollPane(rankingTable, skin);
        scrollPane.setFadeScrollBars(false);
        table.add(scrollPane).grow().padLeft(20).padRight(20);
        table.row();

        // footer com informações gerais
        footerTable = new Table();
        footerTable.pad(20);
        table.add(footerTable).growX().bottom();

        createListeners();
        presenter.loadRanking();
    }

    private Table createListHeader() {
        Table header = new Table();
        header.setBackground(skin.getDrawable("round-dark-gray"));

        // define as colunas do cabeçalho da lista
        header.add(new Label("#", skin, "font", Color.WHITE))
            .width(80).padLeft(20).align(Align.center);
        header.add(new Label("Player", skin, "font", Color.WHITE))
            .expandX().padLeft(28).align(Align.left);
        header.add(new Label("Score", skin, "font", Color.WHITE))
            .width(150).align(Align.left);
        header.add(new Label("Sims Run", skin, "font", Color.WHITE))
            .width(150).align(Align.left);
        header.add(new Label("Average", skin, "font", Color.WHITE))
            .width(150).align(Align.left);

        return header;
    }

    private Table createUserRow(UserRankingDTO user) {
        Table row = new Table();

        row.add(new Label(String.valueOf(user.position()), skin, "font", Color.WHITE))
            .width(80).pad(20).align(Align.center);

        // player (avatar + nome)
        Table playerCell = new Table();
        playerCell.add(new Image(new Texture(user.avatarUrl()))).size(40);
        playerCell.add(new Label(user.name(), skin, "font", Color.WHITE)).padLeft(10);
        row.add(playerCell).expandX().align(Align.left);

        // pontuação
        row.add(new Label(String.valueOf(user.score()), skin, "font", Color.WHITE))
            .width(150).align(Align.center);

        // quantidade de simulações
        row.add(new Label(String.valueOf(user.numSimulations()), skin, "font", Color.WHITE))
            .width(150).align(Align.center);

        row.add(new Label(user.averageScore(), skin, "font", Color.WHITE))
            .width(150).align(Align.center);

        return row;
    }

    @Override
    public void displayRanking(List<UserRankingDTO> ranking) {
        rankingTable.clear();
        rankingTable.top();

        for (UserRankingDTO user : ranking) {
            Table userRow = createUserRow(user);
            if (!rankingTable.getChildren().isEmpty()) {
                rankingTable.add(new Image(skin.newDrawable("white", Color.DARK_GRAY)))
                    .height(1).growX().padTop(5).padBottom(5);
                rankingTable.row();
            }
            rankingTable.add(userRow).growX();
            rankingTable.row();
        }
    }

    @Override
    public void displayTotalSimulations(int totalSimulations) {
        footerTable.clear();
        footerTable.add(new Label("Total Simulations in System: " + totalSimulations, skin,
            "font", Color.WHITE)).expandX().left();
    }

    @Override
    public void displayTotalAverageScore(double totalAverageScore) {
        footerTable.add(new Label("System-wide Average Score: " + String.format("%.2f", totalAverageScore),
            skin, "font", Color.WHITE)).expandX().right();
    }

    private void createListeners() {
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
