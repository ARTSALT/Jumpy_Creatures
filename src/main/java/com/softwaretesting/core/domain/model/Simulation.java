package com.softwaretesting.core.domain.model;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Representa uma simulação de criaturas em um ambiente com um horizonte.
 * Suporta modo de iteração (todos de uma vez) e modo de turno (um por vez).
 */
public class Simulation {

    // --- ESTADO E PROPRIEDADES GERAIS ---
    private Long id;
    private final RandomProvider randomProvider;
    private final double factor;
    private final List<Creature> creatures;
    private final double creatureWidth;
    private final int maxIterations;
    public boolean isFinished;
    private boolean isSuccessful;

    // --- ESTADO PARA MODO ITERAÇÃO ---
    public enum SimulationState { READY_TO_PREPARE, READY_TO_UPDATE, READY_TO_RESOLVE, FINISHED }
    private SimulationState currentState = SimulationState.READY_TO_PREPARE;
    private int currentIteration = 0;

    // --- ESTADO PARA MODO TURNO ---
    private int turnBasedRound = 0;
    private int turnBasedCreatureIndex = -1;
    private boolean guardianHasActedInRound = false;

    // --- INFORMAÇÕES DO USUÁRIO ---
    private User user;
    private String name;
    private LocalDateTime createdAt;

    public Simulation(int numCreatures, double creatureWidth, int horizonWidth, int maxIterations,
                      RandomProvider randomProvider) {
        if (numCreatures <= 1) throw new IllegalArgumentException("Number of creatures must be greater than 1.");
        if (creatureWidth <= 0) throw new IllegalArgumentException("Creature width must be positive.");
        if (horizonWidth <= 0) throw new IllegalArgumentException("Horizon width must be positive.");
        if (maxIterations <= 0) throw new IllegalArgumentException("Max iterations must be positive.");

        this.randomProvider = randomProvider;
        this.factor = horizonWidth / 1_000_000.0;
        this.creatures = new ArrayList<>(numCreatures + 1);
        this.creatureWidth = creatureWidth;
        this.maxIterations = maxIterations;

        for (int i = 0; i < numCreatures; i++) {
            creatures.add(new Creature(1_000_000, horizonWidth / 2.0));
        }
        this.creatures.add(new Guardian(0.0));
    }

    public Simulation(int numCreatures, int iterations, int horizonWidth) {
        this(numCreatures, 1.0, horizonWidth, iterations, (min, max) -> {
            Random random = new Random();
            return min + (max - min) * random.nextDouble();
        });
    }

    public Simulation(int numCreatures, int iterations, int horizonWidth, RandomProvider randomProvider) {
        this(numCreatures, 1.0, horizonWidth, iterations, randomProvider);
    }

    // --- LÓGICA PARA O MODO DE TURNOS INDIVIDUAIS ---

    public Optional<Creature> processNextCreatureInTurn() {
        if (isFinished) return Optional.empty();

        if (guardianHasActedInRound) {
            turnBasedRound++;
            turnBasedCreatureIndex = -1;
            guardianHasActedInRound = false;
        }

        if (turnBasedRound >= maxIterations) {
            this.isFinished = true;
            return Optional.empty();
        }

        int searchIndex = turnBasedCreatureIndex + 1;
        while (searchIndex < creatures.size()) {
            Creature nextCreature = creatures.get(searchIndex);
            if (!(nextCreature instanceof Guardian)) {
                turnBasedCreatureIndex = searchIndex;
                Creature activeCreature = creatures.get(turnBasedCreatureIndex);
                activeCreature.resetTurnDelta();
                calculateTargetPosition(activeCreature);
                return Optional.of(activeCreature);
            }
            searchIndex++;
        }

        for (int i = 0; i < creatures.size(); i++) {
            Creature creature = creatures.get(i);
            if (creature instanceof Guardian) {
                turnBasedCreatureIndex = i;
                guardianHasActedInRound = true;
                creature.resetTurnDelta();
                calculateTargetPosition(creature);
                return Optional.of(creature);
            }
        }

        guardianHasActedInRound = true;
        return processNextCreatureInTurn();
    }

    public void resolveTurnFor(Creature creature) {
        if (isFinished || creature == null) return;
        creature.commitPosition();

        Optional<Cluster> newClusterOpt = handleCollisionsFor(creature);

        // Se a criatura que agiu foi um guardião, ela não rouba
        if (creature instanceof Guardian) {
            checkEndCondition();
            printResults();
            return;
        }

        // Se um novo cluster foi formado, é ele quem rouba
        if (newClusterOpt.isPresent()) {
            findAndStealFromClosest(newClusterOpt.get());
        } else {
            // Se não, a criatura original rouba (se ela ainda existir)
            if (creatures.contains(creature)) {
                findAndStealFromClosest(creature);
            }
        }

        checkEndCondition();
        printResults();
    }

    private void findAndStealFromClosest(Creature stealer) {
        creatures.stream()
            .filter(c -> c.getId() != stealer.getId() && !(c instanceof Guardian))
            .min(Comparator.comparingDouble(c -> Math.abs(c.getPosition() - stealer.getPosition())))
            .ifPresent(closest -> {
                System.out.printf("%s %d está roubando de %s %d.\n",
                    stealer.getClass().getSimpleName(), stealer.getId(),
                    closest.getClass().getSimpleName(), closest.getId());
                stealer.stealFrom(closest);
            });
    }

    private Optional<Cluster> handleCollisionsFor(Creature movedCreature) {
        Optional<Creature> collisionOpt = creatures.stream()
            .filter(c -> c.getId() != movedCreature.getId() &&
                Math.abs(c.getPosition() - movedCreature.getPosition()) < creatureWidth / 2.0)
            .findFirst();

        if (collisionOpt.isEmpty()) return Optional.empty();

        Creature otherCreature = collisionOpt.get();
        System.out.printf("--- COLISÃO DETECTADA: %s %d colidiu com %s %d ---\n",
            movedCreature.getClass().getSimpleName(), movedCreature.getId(),
            otherCreature.getClass().getSimpleName(), otherCreature.getId());

        // Caso 1: Guardião colide com Cluster (Guardião se moveu)
        if (movedCreature instanceof Guardian && otherCreature instanceof Cluster) {
            ((Guardian) movedCreature).absorbCluster((Cluster) otherCreature);
            creatures.remove(otherCreature);
            return Optional.empty();
        }

        // Caso 2: Cluster colide com Guardião (Cluster se moveu)
        if (otherCreature instanceof Guardian && movedCreature instanceof Cluster) {
            ((Guardian) otherCreature).absorbCluster((Cluster) movedCreature);
            creatures.remove(movedCreature);
            return Optional.empty();
        }

        // Caso 3: Cluster absorve uma Creature
        if (otherCreature instanceof Cluster) {
            ((Cluster) otherCreature).addMember(movedCreature);
            creatures.remove(movedCreature);
            return Optional.empty();
        }

        // Caso 4: Duas Creatures normais formam um novo Cluster
        if (!(movedCreature instanceof Guardian) && !(otherCreature instanceof Guardian)) {
            Cluster newCluster = new Cluster(movedCreature, otherCreature);
            System.out.printf("Criado novo Cluster %d a partir de %d e %d.\n", newCluster.getId(), movedCreature.getId(), otherCreature.getId());

            int activeIndex = creatures.indexOf(movedCreature);
            if (activeIndex != -1) {
                creatures.set(activeIndex, newCluster);
            } else {
                creatures.add(newCluster); // Fallback
            }

            creatures.remove(otherCreature);
            turnBasedCreatureIndex = creatures.indexOf(newCluster);
            return Optional.of(newCluster);
        }

        return Optional.empty();
    }

    public Optional<Creature> peekNextCreatureInTurn() {
        if (isFinished) return Optional.empty();

        // Se o guardião já agiu, o próximo será a primeira criatura do novo round
        if (guardianHasActedInRound) {
            return creatures.stream().filter(c -> !(c instanceof Guardian)).findFirst();
        }

        // Procura a próxima criatura saltitante a partir do índice atual
        int searchIndex = turnBasedCreatureIndex + 1;
        while (searchIndex < creatures.size()) {
            Creature nextCreature = creatures.get(searchIndex);
            if (!(nextCreature instanceof Guardian)) {
                return Optional.of(nextCreature); // Encontrou, retorna
            }
            searchIndex++;
        }

        // Se não encontrou, significa que todas já agiram, então o próximo é o guardião
        return creatures.stream().filter(c -> c instanceof Guardian).findFirst();
    }

    // modo de iteração (todos de uma vez)

    public void prepare() {
        if (currentState != SimulationState.READY_TO_PREPARE) return;
        if (currentIteration >= maxIterations) {
            this.currentState = SimulationState.FINISHED;
            return;
        }
        this.currentIteration++;
        System.out.printf("\n----- Preparando a Iteração %d -----\n", this.currentIteration);
        for (Creature creature : creatures) {
            creature.resetTurnDelta();
            calculateTargetPosition(creature);
        }
        this.currentState = SimulationState.READY_TO_UPDATE;
    }

    public void update() {
        if (currentState != SimulationState.READY_TO_UPDATE) return;
        System.out.println("----- Atualizando Posições -----");
        for (Creature creature : creatures) {
            creature.commitPosition();
        }
        this.currentState = SimulationState.READY_TO_RESOLVE;
    }

    public void resolve() {
        if (currentState != SimulationState.READY_TO_RESOLVE) return;
        System.out.println("----- Resolvendo Colisões e Ações -----");
        handleCollisionsAndActions();
        if (isSimulationSuccessful()) {
            isSuccessful = true;
            this.currentState = SimulationState.FINISHED;
        } else if (currentIteration >= maxIterations) {
            this.currentState = SimulationState.FINISHED;
        } else {
            this.currentState = SimulationState.READY_TO_PREPARE;
        }
        printResults();
    }

    public boolean executeNextIteration() {
        if (currentState == SimulationState.FINISHED) return false;
        if (currentState == SimulationState.READY_TO_PREPARE && currentIteration >= maxIterations) {
            this.currentState = SimulationState.FINISHED;
            return false;
        }
        if (currentState == SimulationState.READY_TO_PREPARE) prepare();
        if (currentState == SimulationState.READY_TO_UPDATE) update();
        resolve();
        return true;
    }

    private void handleCollisionsAndActions() {
        List<Creature> newClusters = new ArrayList<>();
        Set<Creature> toRemove = new HashSet<>();
        List<Creature> creaturesView = new ArrayList<>(this.creatures);
        for (int i = 0; i < creaturesView.size(); i++) {
            Creature c1 = creaturesView.get(i);
            if (toRemove.contains(c1)) continue;
            for (int j = i + 1; j < creaturesView.size(); j++) {
                Creature c2 = creaturesView.get(j);
                if (Math.abs(c1.getPosition() - c2.getPosition()) < creatureWidth / 2.0) {
                    resolveCollision(c1, c2, newClusters, toRemove);
                }
            }
        }
        this.creatures.removeAll(toRemove);
        this.creatures.addAll(newClusters);
        for (Creature newCreature : newClusters) {
            if (newCreature instanceof Cluster) {
                findAndStealFromClosest((Cluster) newCreature);
            }
        }
    }

    private void resolveCollision(Creature c1, Creature c2, List<Creature> toAdd, Set<Creature> toRemove) {
        if (c1 instanceof Guardian && c2 instanceof Cluster) {
            ((Guardian) c1).absorbCluster((Cluster) c2);
            toRemove.add(c2);
        } else if (c2 instanceof Guardian && c1 instanceof Cluster) {
            ((Guardian) c2).absorbCluster((Cluster) c1);
            toRemove.add(c1);
        } else if (c1 instanceof Cluster) {
            ((Cluster) c1).addMember(c2);
            toRemove.add(c2);
        } else if (c2 instanceof Cluster) {
            ((Cluster) c2).addMember(c1);
            toRemove.add(c1);
        } else if (!(c1 instanceof Guardian)) {
            Cluster cluster = new Cluster(c1, c2);
            toAdd.add(cluster);
            toRemove.add(c1);
            toRemove.add(c2);
        }
    }

    // métodos auxiliares

    private boolean isSimulationSuccessful() {
        if (creatures.size() == 1 && creatures.get(0) instanceof Guardian) {
            return true;
        }
        if (creatures.size() == 2) {
            Creature c1 = creatures.get(0);
            Creature c2 = creatures.get(1);
            if ((c1 instanceof Guardian && c1.getCoins() > c2.getCoins()) ||
                (c2 instanceof Guardian && c2.getCoins() > c1.getCoins())) {
                System.out.println("Simulação bem-sucedida: guardião com mais moedas que a criatura restante.");
                return true;
            }
        }
        return false;
    }

    private void checkEndCondition() {
        if (isSimulationSuccessful()) {
            this.isFinished = true;
            this.isSuccessful = true;
        }
    }

    public void printResults() {
        System.out.println("Estado atual:");
        long totalCoinsInPlay = 0;
        for (Creature creature : creatures) {
            if (!(creature instanceof Guardian)) {
                totalCoinsInPlay += creature.getCoins();
            }
            System.out.println(" - " + creature.toString());
        }
        System.out.printf("Total de criaturas no horizonte: %d\n", creatures.size());
        System.out.printf(">>> Soma total de moedas (excluindo guardião): %,d\n", totalCoinsInPlay);
    }

    private void calculateTargetPosition(Creature creature) {
        double r = randomProvider.nextDouble(-1, 1);
        double newPosition = creature.getPosition() + r * creature.getCoins() * factor;
        creature.setTargetPosition(newPosition);
    }

    // --- Getters e Setters ---
    public boolean isFinished() { return isFinished; }
    public List<Creature> getCreatures() { return creatures; }
    public SimulationState getCurrentState() { return currentState; }
    public int getIterations() { return currentIteration; }
    public boolean isSuccessful() { return isSuccessful; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}