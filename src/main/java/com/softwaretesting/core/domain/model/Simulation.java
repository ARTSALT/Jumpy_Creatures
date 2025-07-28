package com.softwaretesting.core.domain.model;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Representa uma simulação de criaturas em um ambiente com um horizonte.
 * Suporta modo de iteração (todos de uma vez) e modo de turno (um por vez).
 */
public class Simulation {

    private Long id;
    private final RandomProvider randomProvider;
    private final double factor;
    private final List<Creature> creatures;
    private final double creatureWidth;
    private final int maxIterations;
    private final int initialNumCreatures;
    public boolean isFinished;
    private boolean isSuccessful;
    private String finalMessage = "Simulation ended unexpectedly.";
    private int currentIteration = 0;
    private int turnBasedCreatureIndex = -1;
    private boolean guardianHasActedInRound = false;
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
        this.initialNumCreatures = numCreatures;

        for (int i = 0; i < numCreatures; i++) {
            double initialPosition = randomProvider.nextDouble(-horizonWidth, horizonWidth);
            creatures.add(new Creature(1_000_000, initialPosition));
        }
        this.creatures.add(new Guardian(0.0));
    }

    public Simulation(int numCreatures, int maxIterations, int horizonWidth) {
        this(numCreatures, 1.0, horizonWidth, maxIterations, (min, max) -> {
            Random random = new Random();
            return min + (max - min) * random.nextDouble();
        });
    }

    public Simulation(int numCreatures, int maxIterations, int horizonWidth, RandomProvider randomProvider) {
        this(numCreatures, 1.0, horizonWidth, maxIterations, randomProvider);
    }

    private record CollisionResult(boolean collisionOccurred, boolean entityWasAbsorbed, Optional<Creature> newEntity) {
        public static CollisionResult noCollision() {
            return new CollisionResult(false, false, Optional.empty());
        }
        public static CollisionResult entityAbsorbed() {
            return new CollisionResult(true, true, Optional.empty());
        }
        public static CollisionResult newEntityCreated(Creature newEntity) {
            return new CollisionResult(true, false, Optional.of(newEntity));
        }
        public static CollisionResult entityModified(Creature modifiedEntity) {
            return new CollisionResult(true, false, Optional.of(modifiedEntity));
        }
    }

    // métodos de processamento por turnos

    public void runToEnd() {
        System.out.println("\n--- EXECUTANDO SIMULAÇÃO ATÉ O FIM ---");
        while (!isFinished()) {
            Optional<Creature> activeCreatureOpt = processNextCreatureInTurn();
            if (activeCreatureOpt.isEmpty()) {
                break;
            }
            Creature activeCreature = activeCreatureOpt.get();
            resolveTurnFor(activeCreature);
        }
        System.out.println("--- EXECUÇÃO RÁPIDA FINALIZADA ---");
    }

    public Optional<Creature> processNextCreatureInTurn() {
        if (isFinished) return Optional.empty();

        if (guardianHasActedInRound) {
            currentIteration++;
            turnBasedCreatureIndex = -1;
            guardianHasActedInRound = false;
        }

        if (currentIteration >= maxIterations) {
            this.finalMessage = "Simulation Over: Iteration limit reached.";
            this.isFinished = true;
            this.isSuccessful = false;
            printResults();
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

        Creature currentEntity = creature;
        boolean wasAbsorbed = false;

        while (true) {
            CollisionResult result = handleCollisionsFor(currentEntity);

            if (!result.collisionOccurred()) {
                break;
            }

            if (result.entityWasAbsorbed()) {
                wasAbsorbed = true;
                break;
            }

            currentEntity = result.newEntity().orElseThrow();
        }

        if (!wasAbsorbed && !(currentEntity instanceof Guardian)) {
            findAndStealFromClosest(currentEntity);
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

    private CollisionResult handleCollisionsFor(Creature movedCreature) {
        Optional<Creature> collisionOpt = creatures.stream()
            .filter(c -> c.getId() != movedCreature.getId() &&
                Math.abs(c.getPosition() - movedCreature.getPosition()) < creatureWidth / 2.0)
            .findFirst();

        if (collisionOpt.isEmpty()) return CollisionResult.noCollision();

        Creature otherCreature = collisionOpt.get();
        System.out.printf("--- COLISÃO DETECTADA: %s %d colidiu com %s %d ---\n",
            movedCreature.getClass().getSimpleName(), movedCreature.getId(),
            otherCreature.getClass().getSimpleName(), otherCreature.getId());

        // Caso 1: Guardião colide com Cluster (qualquer ordem)
        if ((movedCreature instanceof Guardian && otherCreature instanceof Cluster) ||
            (movedCreature instanceof Cluster && otherCreature instanceof Guardian)) {

            Guardian guardian = (movedCreature instanceof Guardian) ? (Guardian) movedCreature
                : (Guardian) otherCreature;
            Cluster cluster = (movedCreature instanceof Cluster) ? (Cluster) movedCreature
                : (Cluster) otherCreature;

            System.out.printf("Guardião %d absorveu Cluster %d.\n", guardian.getId(), cluster.getId());
            guardian.absorbCluster(cluster);
            creatures.remove(cluster);
            return CollisionResult.entityAbsorbed();
        }

        // Caso 2: Criatura (que se moveu) é absorvida por um Cluster (parado)
        if (otherCreature instanceof Cluster) {
            System.out.printf("Cluster %d absorveu %s %d.\n",
                otherCreature.getId(), movedCreature.getClass().getSimpleName(), movedCreature.getId());
            ((Cluster) otherCreature).addMember(movedCreature);
            creatures.remove(movedCreature);
            return CollisionResult.entityAbsorbed();
        }

        // Caso 3: Cluster (que se moveu) absorve outra criatura
        if (movedCreature instanceof Cluster) {
            System.out.printf("Cluster %d absorveu %s %d.\n",
                movedCreature.getId(), otherCreature.getClass().getSimpleName(), otherCreature.getId());
            ((Cluster) movedCreature).addMember(otherCreature);
            creatures.remove(otherCreature);
            return CollisionResult.entityModified(movedCreature);
        }

        // Caso 4: Duas Creatures normais formam um novo Cluster
        if (!(movedCreature instanceof Guardian) && !(otherCreature instanceof Guardian)) {
            Cluster newCluster = new Cluster(movedCreature, otherCreature);
            System.out.printf("Criado novo Cluster %d a partir de %d e %d.\n",
                newCluster.getId(), movedCreature.getId(), otherCreature.getId());
            int activeIndex = creatures.indexOf(movedCreature);
            if (activeIndex != -1) {
                creatures.set(activeIndex, newCluster);
            } else {
                creatures.add(newCluster);
            }
            creatures.remove(otherCreature);
            turnBasedCreatureIndex = creatures.indexOf(newCluster);
            return CollisionResult.newEntityCreated(newCluster);
        }

        return CollisionResult.noCollision();
    }

    public Optional<Creature> peekNextCreatureInTurn() {
        if (isFinished) return Optional.empty();
        if (guardianHasActedInRound) {
            return creatures.stream().filter(c -> !(c instanceof Guardian)).findFirst();
        }
        int searchIndex = turnBasedCreatureIndex + 1;
        while (searchIndex < creatures.size()) {
            Creature nextCreature = creatures.get(searchIndex);
            if (!(nextCreature instanceof Guardian)) {
                return Optional.of(nextCreature);
            }
            searchIndex++;
        }
        return creatures.stream().filter(c -> c instanceof Guardian).findFirst();
    }

    // métodos auxiliares

    private void checkEndCondition() {
        if (creatures.size() == 1 && creatures.getFirst() instanceof Guardian) {
            this.isFinished = true;
            this.isSuccessful = true;
            this.finalMessage = "SUCCESS! Only the guardian remains.";
            return;
        }
        if (creatures.size() == 2) {
            Guardian guardian = (Guardian) creatures.stream().filter(
                c -> c instanceof Guardian).findFirst().orElse(null);
            Creature other = creatures.stream().filter(
                c -> !(c instanceof Guardian)).findFirst().orElse(null);
            if (Objects.requireNonNull(other).getClass() == Creature.class) {
                this.isFinished = true;
                if (Objects.requireNonNull(guardian).getCoins() > other.getCoins()) {
                    this.isSuccessful = true;
                    this.finalMessage = "SUCCESS! The guardian has more coins than the final creature.";
                } else {
                    this.isSuccessful = false;
                    this.finalMessage = "DEFEAT! The final creature has more coins than the guardian.";
                }
            }
        }
    }

    private void calculateTargetPosition(Creature creature) {
        double r = randomProvider.nextDouble(-1, 1);
        double newPosition = creature.getPosition() + r * creature.getCoins() * factor;
        creature.setTargetPosition(newPosition);
    }

    public void printResults() {
        System.out.printf("\n--- Iteração: %d ---\n", currentIteration);
        System.out.println("Estado atual:");
        long totalCoinsInPlay = 0;
        for (Creature creature : creatures) {
            if (!(creature instanceof Guardian)) {
                totalCoinsInPlay += creature.getCoins();
            }
            System.out.println(" - " + creature);
        }
        System.out.printf("Total de criaturas no horizonte: %d\n", creatures.size());
        System.out.printf(">>> Soma total de moedas (excluindo guardião): %,d\n", totalCoinsInPlay);
    }

    // getters e setters

    public boolean isFinished() { return isFinished; }
    public List<Creature> getCreatures() { return creatures; }
    public int getIterations() { return currentIteration; }
    public void setIterations(int iterations) { this.currentIteration = iterations; }
    public boolean isSuccessful() { return isSuccessful; }
    public void setSuccessful(boolean successful) { this.isSuccessful = successful; }
    public double getCreatureWidth() { return creatureWidth; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getFinalMessage() { return finalMessage; }
    public int getInitialNumCreatures() { return initialNumCreatures; }
}
