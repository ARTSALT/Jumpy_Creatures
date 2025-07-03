package com.softwaretesting.adapters.ui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.softwaretesting.adapters.ui.LibGdxApplication;
import com.softwaretesting.adapters.ui.dto.SimulationSummaryDTO;
import com.softwaretesting.adapters.ui.presenter.UserPresenter;
import com.softwaretesting.adapters.ui.view.UserView;

import java.util.List;

/**
 * Tela principal do usuário, onde são exibidas as informações do usuário,
 * como nome, avatar, pontuação, lista de simulações e opções de navegação para estatísticas e ranking.
 * Implementa a interface UserView para capturar as ações do usuário.
 */
public class UserScreen extends ScreenTemplate implements UserView {

    private final Label usernameLabel;
    private final Label scoreLabel;
    private final Label totalSimsLabel;
    private final Label avgSuccessLabel;
    private final Image avatarImage;
    private final Table simulationListTable;    // tabela interna para a lista de simulações
    private final TextButton runSimButton;      // botão para iniciar uma nova simulação

    private final UserPresenter presenter;

    public UserScreen(LibGdxApplication application) {
        super(application);

        this.presenter = new UserPresenter(this, application);

        table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // header
        Table headerTable = createHeader("User Profile");
        table.add(headerTable).growX();
        table.row();

        // layout principal de 2 colunas
        Table contentTable = new Table();
        table.add(contentTable).grow();

        Table leftColumn = new Table();
        Table rightColumn = new Table();

        // define o layout das colunas
        contentTable.add(leftColumn).grow().pad(20);
        contentTable.add(rightColumn).grow().pad(20);

        // =================================================================================================
        // coluna da esquerda
        avatarImage = new Image();
        usernameLabel = new Label("", skin, "title", Color.WHITE);
        scoreLabel = new Label("", skin, "font", Color.BLACK);
        totalSimsLabel = new Label("", skin, "font", Color.BLACK);
        avgSuccessLabel = new Label("", skin, "font", Color.BLACK);
        runSimButton = new TextButton("Run New Simulation", skin);

        // caixa de informações do usuário
        Table infoBox = new Table();
        infoBox.setBackground(skin.getDrawable("round-white"));
        infoBox.setColor(Color.LIGHT_GRAY);
        infoBox.add(new Label("Score:", skin)).pad(1).padRight(15);
        infoBox.add(scoreLabel).pad(1);
        infoBox.row();
        infoBox.add(new Label("Total simulations executed:", skin)).pad(1).padRight(15);
        infoBox.add(totalSimsLabel).pad(1);
        infoBox.row();
        infoBox.add(new Label("Average of successful simulations:", skin)).pad(1).padRight(15);
        infoBox.add(avgSuccessLabel).pad(1);

        leftColumn.add(avatarImage).size(200).padBottom(20);
        leftColumn.row();
        leftColumn.add(usernameLabel).padBottom(30);
        leftColumn.row();
        leftColumn.add(infoBox).width(475).height(100).padBottom(40);
        leftColumn.row();
        leftColumn.add(runSimButton).width(250).height(50);

        // =================================================================================================
        // coluna da direita
        Label simOverviewLabel = new Label("Simulation Overview", skin, "title", Color.WHITE);

        simulationListTable = new Table(); // tabela que conterá os cards das simulações
        ScrollPane scrollPane = new ScrollPane(simulationListTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false); // desabilita rolagem horizontal

        rightColumn.add(simOverviewLabel).align(Align.left).padBottom(20);
        rightColumn.row();
        rightColumn.add(scrollPane).grow(); // faz a lista ocupar o espaço restante'

        createListeners();

        presenter.loadUserInfo();
    }

    @Override
    public void displayUserInfo(String username, int score, int numSimulations) {
        usernameLabel.setText(username);
        scoreLabel.setText(score);
        totalSimsLabel.setText(numSimulations);
    }

    @Override
    public void setUserProfileImage(String path) {
        avatarImage.setDrawable(new TextureRegionDrawable(new Texture(path)));
    }

    @Override
    public void setAverageSuccessRate(double avg) {
        avgSuccessLabel.setText(String.format("%.2f%%", avg));
    }

    @Override
    public void displaySimulationList(List<SimulationSummaryDTO> simulations) {
        simulationListTable.clear();    // limpa a lista antiga
        simulationListTable.top();      // alinha os novos itens no topo

        // verifica se há simulações para exibir
        for (SimulationSummaryDTO sim : simulations) {
            Table card = createSimulationCard(sim);
            simulationListTable.add(card).growX().pad(5).padBottom(10);
            simulationListTable.row();
        }
    }

    private Table createSimulationCard(SimulationSummaryDTO simulationDTO) {
        Table card = new Table();
        card.setBackground(skin.getDrawable("round-white"));
        card.pad(15);
        card.align(Align.left);

        card.add(new Label(simulationDTO.name(), skin, "title"))
            .colspan(2).align(Align.left).padBottom(10);
        card.row();
        card.add(new Label("Date: " + simulationDTO.date(), skin)).align(Align.left);
        card.add(new Label("Zombies: " + simulationDTO.zombies(), skin)).align(Align.right);
        card.row();
        card.add(new Label("Iterations: " + simulationDTO.iterations(), skin)).align(Align.left);
        card.add(new Label(simulationDTO.wasSuccessful() ? "SUCCESS" : "FAILED", skin, "font",
            simulationDTO.wasSuccessful() ? Color.GREEN : Color.RED)).align(Align.right);

        card.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                presenter.onSimulationClicked(simulationDTO.id());
            }
        });

        return card;
    }

    @Override
    public void showDetailedSimulationView(int simulationId) {
        // cria um pop-up para mostrar os detalhes da simulação
        new Dialog("Simulation " + simulationId, skin) {
            {
                text("Detailed information about the simulation goes here.");
                button("Close");
            }
        }.show(stage);
    }

    private void createListeners() {
        // listener para o pressionamento da tecla ESC
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == com.badlogic.gdx.Input.Keys.ESCAPE) {
                    presenter.onUserLogout();
                    return true;
                }
                return false;
            }
        });

        rankingButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                presenter.onRankingButtonClicked();
            }
        });

        logoutButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                presenter.onUserLogout();
            }
        });

        // listener para o botão de nova simulação
        runSimButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                presenter.onRunNewSimulationClicked();
            }
        });

        // listener para o clique no avatar
        avatarImage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                presenter.onAvatarClicked();
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                // mouse hover no avatar, muda o cursor para a "mãozinha"
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Hand);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                // mouse sai do avatar, volta o cursor para a seta padrão
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
            }
        });
    }
}
