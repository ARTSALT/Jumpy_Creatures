package com.softwaretesting.adapters.ui.screen;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.softwaretesting.adapters.ui.LibGdxApplication;
import com.softwaretesting.adapters.ui.presenter.AdminPresenter;
import com.softwaretesting.adapters.ui.view.AdminView;
import com.softwaretesting.core.domain.model.Simulation;
import com.softwaretesting.core.domain.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tela de administração que exibe todos os usuários e simulações do sistema.
 * Permite ao administrador excluir usuários e simulações.
 */
public class AdminScreen extends ScreenTemplate implements AdminView {

    private final AdminPresenter presenter;

    private final Table usersTable;       // tabela interna para a lista de usuários
    private final Table simulationsTable; // tabela interna para a lista de simulações

    // mapa para rastrear elementos por ID
    private final Map<Long, Actor> userRowMap = new HashMap<>();
    private final Map<Long, Actor> simulationRowMap = new HashMap<>();

    public AdminScreen(LibGdxApplication application) {
        super(application);

        this.presenter = new AdminPresenter(this, application);

        // tabela principal
        table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // header
        Table headerTable = createHeader("Admin Panel");
        table.add(headerTable).colspan(2).growX();
        table.row();

        // main composta por 2 colunas
        // coluna de usuários na esquerda
        Table usersContainer = new Table();
        usersContainer.pad(10);
        Label usersTitle = new Label("Users", skin, "title", Color.WHITE);
        usersTable = new Table();
        ScrollPane usersScrollPane = new ScrollPane(usersTable, skin);
        usersScrollPane.setFadeScrollBars(false);
        usersContainer.add(usersTitle).padBottom(10);
        usersContainer.row();
        usersContainer.add(usersScrollPane).grow();
        table.add(usersContainer).grow();

        // coluna de simulações na direita
        Table simulationsContainer = new Table();
        simulationsContainer.pad(10);
        Label simulationsTitle = new Label("Simulations", skin, "title", Color.WHITE);
        simulationsTable = new Table();
        ScrollPane simulationsScrollPane = new ScrollPane(simulationsTable, skin);
        simulationsScrollPane.setFadeScrollBars(false);
        simulationsContainer.add(simulationsTitle).padBottom(10);
        simulationsContainer.row();
        simulationsContainer.add(simulationsScrollPane).grow();
        table.add(simulationsContainer).grow();

        createListeners();

        // carrega os dados e preenche as tabelas
        presenter.onAdminScreenLoaded();
    }

    @Override
    public void showUsersList(List<User> users) {
        usersTable.clear();
        userRowMap.clear();
        usersTable.top(); // alinha o conteúdo da tabela no topo

        for (User user : users) {
            Actor userRow = createUserCard(user);
            usersTable.add(userRow).growX().pad(5).row();
            userRowMap.put(user.getId(), userRow);
        }
    }

    @Override
    public void removeUserFromList(Long userId) {
        Actor rowToRemove = userRowMap.get(userId);
        if (rowToRemove != null) {
            rowToRemove.remove();
            userRowMap.remove(userId);
        }
    }

    @Override
    public void showSimulationsList(List<Simulation> simulations) {
        simulationsTable.clear();
        simulationRowMap.clear();
        simulationsTable.top(); // alinha o conteúdo da tabela no topo

        for (Simulation simulation : simulations) {
            Actor simRow = createSimulationCard(simulation);
            simulationsTable.add(simRow).growX().pad(5).row();
            simulationRowMap.put(simulation.getId(), simRow);
        }
    }

    @Override
    public void removeSimulationFromList(Long simulationId) {
        Actor rowToRemove = simulationRowMap.get(simulationId);
        if (rowToRemove != null) {
            rowToRemove.remove();
            simulationRowMap.remove(simulationId);
        }
    }

    private Table createUserCard(User user) {
        Table row = new Table();

        Label nameLabel = new Label(user.getUsername() + " (Score: " + user.getScore() + ")", skin);
        TextButton deleteButton = new TextButton("X", skin);
        deleteButton.setColor(Color.RED);

        row.add(nameLabel).expandX().align(Align.left).padLeft(10);
        row.add(deleteButton).align(Align.right).padRight(10);

        deleteButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                presenter.onDeleteUserClicked(user);
            }
        });
        return row;
    }

    private Table createSimulationCard(Simulation simulation) {
        Table row = new Table();

        Label infoLabel = new Label("ID: " + simulation.getId() + " - User: " + simulation.getId(), skin);
        TextButton deleteButton = new TextButton("X", skin);
        deleteButton.setColor(Color.RED);

        row.add(infoLabel).expandX().align(Align.left).padLeft(10);
        row.add(deleteButton).align(Align.right).padRight(10);

        deleteButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                presenter.onDeleteSimulationClicked(simulation.getId());
            }
        });
        return row;
    }

    private void createListeners() {
        // listener para o pressionamento da tecla ESC
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
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
    }
}
