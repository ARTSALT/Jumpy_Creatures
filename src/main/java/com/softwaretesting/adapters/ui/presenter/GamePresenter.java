package com.softwaretesting.adapters.ui.presenter;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.softwaretesting.adapters.ui.LibGdxApplication;
import com.softwaretesting.adapters.ui.screen.UserScreen;
import com.softwaretesting.adapters.ui.view.GameView;
import com.softwaretesting.adapters.ui.view.View;
import com.softwaretesting.core.application.service.SimulationService;
import com.softwaretesting.core.domain.model.*;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

public class GamePresenter {

    private final GameView view;
    private final LibGdxApplication application;
    private Simulation simulation;

    private enum PresenterState {
        READY_FOR_ACTION,
        ANIMATING_JUMP
    }
    private PresenterState currentState = PresenterState.READY_FOR_ACTION;

    private enum ExecutionMode {
        MANUAL,
        AUTOMATIC
    }
    private ExecutionMode executionMode = ExecutionMode.MANUAL;

    private Optional<Creature> activeCreature = Optional.empty();
    private Optional<Creature> arrowTarget = Optional.empty();

    private Integer selectedCreatureId;
    private boolean showColliders = false;

    public GamePresenter(GameView view, LibGdxApplication application) {
        this.view = view;
        this.application = application;
    }

    public void onPlayClicked(String name, String numZombiesText) {
        if (isGameRunning()) return;
        try {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Simulation name cannot be empty.");
            }
            if (numZombiesText == null || numZombiesText.isBlank()) {
                throw new IllegalArgumentException("Number of zombies cannot be empty.");
            }

            int numZumbis = Integer.parseInt(numZombiesText);
            if (numZumbis > 1000) {
                throw new IllegalArgumentException("Number of zombies cannot exceed 1000.");
            }

            RandomProvider randomProvider = (min, max) -> new Random().nextDouble() * (max - min) + min;
            simulation = new Simulation(numZumbis, 225, 1000, 100, randomProvider);
            simulation.setName(name);

            view.synchronizeActors(simulation.getCreatures());
            view.showMessage("Press 'P' to start. Press ENTER for auto-run.", GameView.MessageType.INFO);
            currentState = PresenterState.READY_FOR_ACTION;
            executionMode = ExecutionMode.MANUAL;
            arrowTarget = simulation.peekNextCreatureInTurn();
        } catch (IllegalArgumentException e) {
            view.showMessage("Error: " + e.getMessage(), View.MessageType.ERROR);
        }
    }

    public void onUpdate(float deltaTime) {
        if (simulation == null || simulation.isFinished()) return;

        if (view.isAnyActorOffScreen()) {
            OrthographicCamera camera = view.getGameCamera();
            view.setGameCameraZoom(camera.zoom * 1.005f);
        }

        if (currentState == PresenterState.ANIMATING_JUMP && view.areAnimationsFinished()) {
            activeCreature.ifPresent(simulation::resolveTurnFor);
            view.synchronizeActors(simulation.getCreatures());
            activeCreature = Optional.empty();
            if (simulation.isFinished()) {
                endGame(simulation.getFinalMessage(), simulation.isSuccessful());
                return;
            }
            arrowTarget = simulation.peekNextCreatureInTurn();
            currentState = PresenterState.READY_FOR_ACTION;
            if (executionMode == ExecutionMode.MANUAL) {
                view.showMessage("Ready for next turn. Press 'P'.", GameView.MessageType.INFO);
            }
        }

        if (executionMode == ExecutionMode.AUTOMATIC && currentState == PresenterState.READY_FOR_ACTION) {
            advanceTurn();
        }
    }

    public void onEnterPressed() {
        if (executionMode == ExecutionMode.AUTOMATIC || simulation == null || simulation.isFinished()) return;
        executionMode = ExecutionMode.AUTOMATIC;
        view.showMessage("Auto-run enabled. Press 'P' to pause.", GameView.MessageType.INFO);
    }

    public void onEndPressed() {
        if (simulation == null || simulation.isFinished()) return;
        this.executionMode = ExecutionMode.MANUAL;
        this.currentState = PresenterState.READY_FOR_ACTION;
        this.activeCreature = Optional.empty();
        this.arrowTarget = Optional.empty();
        view.showMessage("Fast-forwarding to the end...", GameView.MessageType.INFO);
        simulation.runToEnd();
        view.synchronizeActors(simulation.getCreatures());
        endGame(simulation.getFinalMessage(), simulation.isSuccessful());
    }

    public void onAdvanceSimulationStep() {
        if (simulation == null || simulation.isFinished()) return;
        if (executionMode == ExecutionMode.AUTOMATIC) {
            executionMode = ExecutionMode.MANUAL;
            view.showMessage("Auto-run paused. Press 'P' to advance manually.", GameView.MessageType.INFO);
            return;
        }
        if (currentState == PresenterState.READY_FOR_ACTION) {
            advanceTurn();
        } else {
            view.showMessage("Wait for the current animation to finish!", GameView.MessageType.WARNING);
        }
    }

    private void advanceTurn() {
        if (simulation.isFinished()) return;
        activeCreature = simulation.processNextCreatureInTurn();
        arrowTarget = activeCreature;
        activeCreature.ifPresent(creature -> {
            view.startJumpAnimationFor(creature);
            currentState = PresenterState.ANIMATING_JUMP;
            if (executionMode == ExecutionMode.MANUAL) {
                view.showMessage(creature.getClass().getSimpleName() + " " + creature.getId() + " is jumping...", GameView.MessageType.INFO);
            }
        });
        if (activeCreature.isEmpty() && simulation.isFinished()) {
            endGame(simulation.getFinalMessage(), simulation.isSuccessful());
        }
    }

    private void endGame(String finalMessage, boolean success) {
        currentState = PresenterState.READY_FOR_ACTION;
        executionMode = ExecutionMode.MANUAL;
        activeCreature = Optional.empty();
        arrowTarget = Optional.empty();
        User currentUser = application.getCurrentUser();
        if (currentUser != null) {
            simulation.setUser(currentUser);
            simulation.setCreatedAt(LocalDateTime.now());
            try {
                SimulationService simulationService = application.getDatabaseFactory().getSimulationService();
                simulationService.register(simulation);
                User userS = simulationService.getSimulationUser(simulation.getId());
                application.getDatabaseFactory().getUserService().updateUserScore(simulationService, userS);
                System.out.println("Simulation saved to database for user: " + currentUser.getUsername());
            } catch (SQLException e) {
                System.err.println("Failed to save simulation to database: " + e.getMessage());
            } catch (IOException e) {
                System.err.println("Failed to update user score: " + e.getMessage());
            }
        }
        view.showGameOver(success, finalMessage);
    }

    // --- Outros métodos (onZombieSelected, getArrowTarget, etc.) ---
    public void onZombieSelected(int creatureId) {
        if (this.selectedCreatureId != null && this.selectedCreatureId == creatureId) {
            this.selectedCreatureId = null;
        } else {
            this.selectedCreatureId = creatureId;
        }
    }
    public void onBackgroundClicked() { this.selectedCreatureId = null; }
    public void onToggleColliders() { this.showColliders = !this.showColliders; }
    public void onManualZoom(float amount) {
        if (!isGameRunning()) return;
        OrthographicCamera camera = view.getGameCamera();
        float newZoom = Math.max(0.1f, camera.zoom + amount);
        view.setGameCameraZoom(newZoom);
    }
    public void onExit() { view.stopMusic(); application.navigateTo(new UserScreen(application)); }
    public Optional<Creature> getSelectedCreature() {
        if (selectedCreatureId == null || simulation == null) return Optional.empty();
        return simulation.getCreatures().stream().filter(c -> c.getId() == selectedCreatureId).findFirst();
    }
    public boolean areCollidersVisible() { return this.showColliders; }
    public boolean isGameRunning() { return simulation != null && !simulation.isFinished(); }
    public Optional<Creature> getArrowTarget() { return arrowTarget; }
}
