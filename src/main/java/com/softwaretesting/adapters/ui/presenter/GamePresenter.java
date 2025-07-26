package com.softwaretesting.adapters.ui.presenter;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.softwaretesting.adapters.ui.LibGdxApplication;
import com.softwaretesting.adapters.ui.screen.UserScreen;
import com.softwaretesting.adapters.ui.view.GameView;
import com.softwaretesting.core.application.service.SimulationService;
import com.softwaretesting.core.domain.model.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

public class GamePresenter {

    private final GameView view;
    private final LibGdxApplication application;
    private Simulation simulation;

    private enum GameState {
        WAITING_TO_START,
        PREPARING_TURN,
        PROCESS_CREATURE,
        WAITING_FOR_ANIMATION,
        END_TURN,
        GAME_OVER
    }
    private GameState currentState = GameState.WAITING_TO_START;

    private int currentCreatureIndex = 0;
    private Creature activeCreature;

    private Integer selectedCreatureId;
    private boolean showColliders = false;

    public GamePresenter(GameView view, LibGdxApplication application) {
        this.view = view;
        this.application = application;
    }

    public void onPlayClicked(String name, String numZombiesText) {
        if (currentState != GameState.WAITING_TO_START) return;
        try {
            int numZumbis = Integer.parseInt(numZombiesText);
            if (numZumbis <= 0) {
                view.showMessage("Number of zombies must be positive.", GameView.MessageType.ERROR);
                return;
            }
            RandomProvider randomProvider = (min, max) -> new Random().nextDouble() * (max - min) + min;
            simulation = new Simulation(numZumbis, 350.0, 1000, 100, randomProvider);
            simulation.setName(name.isEmpty() ? "Simulation " + LocalDateTime.now() : name);
            view.synchronizeActors(simulation.getCreatures());
            currentState = GameState.PREPARING_TURN;
        } catch (Exception e) {
            view.showMessage("Invalid input.", GameView.MessageType.ERROR);
        }
    }

    public void onUpdate(float deltaTime) {
        if (simulation == null) return;

        switch (currentState) {
            case PREPARING_TURN:
//                if (simulation.prepareNextIteration()) {
//                    view.synchronizeActors(simulation.getCreatures());
//                    currentCreatureIndex = 0;
//                    currentState = GameState.PROCESS_CREATURE;
//                } else {
//                    endGame("Iteration limit reached.");
//                }
                break;

            case PROCESS_CREATURE:
                activeCreature = null;
                while (currentCreatureIndex < simulation.getCreatures().size()) {
                    Creature potentialCreature = simulation.getCreatures().get(currentCreatureIndex);
                    if (!(potentialCreature instanceof Guardian)) {
                        activeCreature = potentialCreature;
                        break;
                    }
                    currentCreatureIndex++;
                }

                if (activeCreature != null) {
                    view.startJumpAnimationFor(activeCreature.getId());
                    currentState = GameState.WAITING_FOR_ANIMATION;
                } else {
                    currentState = GameState.END_TURN;
                }
                break;

            case WAITING_FOR_ANIMATION:
                if (view.isAnyActorOffScreen()) {
                    OrthographicCamera camera = view.getGameCamera();
                    view.setGameCameraZoom(camera.zoom * 1.005f);
                }

                if (view.isActorAnimationFinished(activeCreature.getId())) {
                    currentCreatureIndex++;
                    currentState = GameState.PROCESS_CREATURE;
                }
                break;

            case END_TURN:
                simulation.executeNextIteration();
                view.synchronizeActors(simulation.getCreatures());
                if (simulation.isSuccessful()) {
                    endGame("SUCCESS!");
                } else if (simulation.getIterations() >= 100) {
                    endGame("Iteration limit reached.");
                } else {
                    currentState = GameState.PREPARING_TURN;
                }
                break;

            case GAME_OVER:
            case WAITING_TO_START:
                break;
        }
    }

    private void endGame(String finalMessage) {
        currentState = GameState.GAME_OVER;

        User currentUser = application.getCurrentUser();
        if(currentUser != null) {
            simulation.setUser(currentUser);
            simulation.setCreatedAt(LocalDateTime.now());

            try {
                SimulationService simulationService = application.getDatabaseFactory().getSimulationService();
                simulationService.register(simulation);
                System.out.println("Simulation saved to database.");
            } catch (SQLException e) {
                System.err.println("Failed to save simulation to database: " + e.getMessage());
            }
        }
        view.showGameOver(simulation.isSuccessful(), finalMessage);
    }

    public void onZombieSelected(int creatureId) {
        if (this.selectedCreatureId != null && this.selectedCreatureId == creatureId) {
            this.selectedCreatureId = null;
        } else {
            this.selectedCreatureId = creatureId;
        }
    }

    public void onBackgroundClicked() {
        this.selectedCreatureId = null;
    }

    public void onToggleColliders() {
        this.showColliders = !this.showColliders;
    }

    public void onManualZoom(float amount) {
        if (currentState == GameState.WAITING_TO_START) return;
        OrthographicCamera camera = view.getGameCamera();
        float newZoom = Math.max(0.1f, camera.zoom + amount);
        view.setGameCameraZoom(newZoom);
    }

    public void onExit() {
        application.navigateTo(new UserScreen(application));
    }

    public Optional<Creature> getSelectedCreature() {
        if (selectedCreatureId == null || simulation == null) {
            return Optional.empty();
        }
        return simulation.getCreatures().stream()
            .filter(c -> c.getId() == selectedCreatureId)
            .findFirst();
    }

    public boolean areCollidersVisible() {
        return this.showColliders;
    }

    public boolean isGameRunning() {
        return currentState != GameState.WAITING_TO_START && currentState != GameState.GAME_OVER;
    }
}
