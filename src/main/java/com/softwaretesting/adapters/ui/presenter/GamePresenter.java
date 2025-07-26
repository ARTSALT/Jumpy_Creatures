package com.softwaretesting.adapters.ui.presenter;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.softwaretesting.adapters.ui.LibGdxApplication;
import com.softwaretesting.adapters.ui.screen.UserScreen;
import com.softwaretesting.adapters.ui.view.GameView;
import com.softwaretesting.core.application.service.SimulationService;
import com.softwaretesting.core.domain.model.Creature;
import com.softwaretesting.core.domain.model.RandomProvider;
import com.softwaretesting.core.domain.model.Simulation;
import com.softwaretesting.core.domain.model.User;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

public class GamePresenter {

    private final GameView view;
    private final LibGdxApplication application;
    private Simulation simulation;

    private enum PresenterState {
        READY_FOR_ACTION, // Esperando o usuário pressionar 'P'
        ANIMATING_JUMP    // Animação de pulo em andamento, esperando ela terminar
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
            int numZumbis = Integer.parseInt(numZombiesText);
            RandomProvider randomProvider = (min, max) -> new Random().nextDouble() * (max - min) + min;
            simulation = new Simulation(numZumbis, 225, 1000, 100, randomProvider);
            simulation.setName(name.isEmpty() ? "Simulation " + LocalDateTime.now() : name);

            view.synchronizeActors(simulation.getCreatures());
            view.showMessage("Press 'P' to start the simulation.", GameView.MessageType.INFO);
            currentState = PresenterState.READY_FOR_ACTION;
            arrowTarget = simulation.peekNextCreatureInTurn();
        } catch (Exception e) {
            view.showMessage("Invalid input.", GameView.MessageType.ERROR);
        }
    }

    public void onUpdate(float deltaTime) {
        if (simulation == null) return;

        if (view.isAnyActorOffScreen()) {
            OrthographicCamera camera = view.getGameCamera();
            view.setGameCameraZoom(camera.zoom * 1.005f);
        }

        if (currentState == PresenterState.ANIMATING_JUMP && view.areAnimationsFinished()) {
            // A animação visual terminou. Resolvemos a lógica para a criatura ativa.
            activeCreature.ifPresent(simulation::resolveTurnFor);
            view.synchronizeActors(simulation.getCreatures());

            activeCreature = Optional.empty();

            if (simulation.isFinished()) {
                endGame(simulation.isSuccessful() ? "SUCCESS!" : "Iteration limit reached.", simulation.isSuccessful());
                return;
            }

            arrowTarget = simulation.peekNextCreatureInTurn();

            currentState = PresenterState.READY_FOR_ACTION;
            view.showMessage("Ready for next turn. Press 'P'.", GameView.MessageType.INFO);
        }

        // Se estivermos em modo automático e prontos para a próxima ação, avança o turno.
        if (executionMode == ExecutionMode.AUTOMATIC && currentState == PresenterState.READY_FOR_ACTION) {
            advanceTurn();
        }
    }

    public void onEnterPressed() {
        if (executionMode == ExecutionMode.AUTOMATIC) return; // Já está no modo

        executionMode = ExecutionMode.AUTOMATIC;
        view.showMessage("Auto-run enabled. Press 'P' to pause.", GameView.MessageType.INFO);
    }

    /**
     * Chamado quando o usuário pressiona 'P'.
     * Agora tem dupla função: avançar no modo manual ou pausar no modo automático.
     */
    public void onAdvanceSimulationStep() {
        if (simulation == null || simulation.isFinished()) return;

        // Se estiver no modo automático, o 'P' serve para pausar.
        if (executionMode == ExecutionMode.AUTOMATIC) {
            executionMode = ExecutionMode.MANUAL;
            view.showMessage("Auto-run paused. Press 'P' to advance manually.", GameView.MessageType.INFO);
            return;
        }

        // Se estiver no modo manual, avança um turno.
        if (currentState == PresenterState.READY_FOR_ACTION) {
            advanceTurn();
        } else {
            view.showMessage("Wait for the current animation to finish!", GameView.MessageType.WARNING);
        }
    }

    /**
     * Contém a lógica de avanço de turno, agora
     * chamado tanto pelo modo manual ('P') quanto pelo automático (onUpdate).
     */
    private void advanceTurn() {
        activeCreature = simulation.processNextCreatureInTurn();
        arrowTarget = activeCreature;

        activeCreature.ifPresent(creature -> {
            view.startJumpAnimationFor(creature);
            currentState = PresenterState.ANIMATING_JUMP;
            if (executionMode == ExecutionMode.MANUAL) {
                view.showMessage(creature.getClass().getSimpleName() + " " + creature.getId() + " is jumping...", GameView.MessageType.INFO);
            }
        });

        if (activeCreature.isEmpty() && !simulation.isFinished()) {
            endGame("Limit reached.", false);
        }
    }

    private void endGame(String finalMessage, boolean success) {
        // Garante que o estado do presenter seja resetado para evitar novas ações.
        currentState = PresenterState.READY_FOR_ACTION;
        activeCreature = Optional.empty(); // Limpa a criatura ativa

        // Pega o usuário logado na aplicação.
        User currentUser = application.getCurrentUser();

        // Se houver um usuário, associa-o à simulação e tenta salvar no banco de dados.
        if (currentUser != null) {
            simulation.setUser(currentUser);
            simulation.setCreatedAt(LocalDateTime.now());

            try {
                // Obtém o serviço de simulação e registra o resultado.
                SimulationService simulationService = application.getDatabaseFactory().getSimulationService();
                simulationService.register(simulation);
                System.out.println("Simulation saved to database for user: " + currentUser.getUsername());
            } catch (SQLException e) {
                // Em caso de erro, registra no console.
                System.err.println("Failed to save simulation to database: " + e.getMessage());
            }
        }

        // Chama a View para exibir a tela de "Game Over".
        view.showGameOver(success, finalMessage);
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
        if (!isGameRunning()) return;
        OrthographicCamera camera = view.getGameCamera();
        float newZoom = Math.max(0.1f, camera.zoom + amount);
        view.setGameCameraZoom(newZoom);
    }

    public void onExit() {
        view.stopMusic();
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
        return simulation != null && simulation.getCurrentState() != Simulation.SimulationState.FINISHED;
    }

    public Optional<Creature> getArrowTarget() {
        return arrowTarget;
    }
}
