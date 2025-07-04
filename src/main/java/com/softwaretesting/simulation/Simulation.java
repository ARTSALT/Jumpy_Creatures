package com.softwaretesting.simulation;

import com.softwaretesting.simulation.entity.Cluster;
import com.softwaretesting.simulation.entity.Creature;
import com.softwaretesting.simulation.entity.Guardian;

import java.util.*;

/**
 * Representa uma simulação de criaturas em um ambiente com um horizonte.
 * As criaturas interagem entre si formando clusters e roubando moedas.
 * O guardião é uma criatura especial que pode absorver clusters de criaturas.
 */
public class Simulation {

    private final RandomProvider randomProvider;
    private final double factor;
    private final List<Creature> creatures;
    private Guardian guardian;
    private final double creatureWidth;
    private final int maxIterations;
    private int currentIteration = 0;
    private boolean isSuccessful;

    /**
     * Cria uma nova simulação com os parâmetros especificados.
     * @param numCreatures Número de criaturas iniciais.
     * @param creatureWidth A "largura" de uma criatura para detecção de colisão.
     * @param horizonWidth A largura total do horizonte da simulação.
     * @param maxIterations O número máximo de iterações antes de a simulação parar.
     * @param randomProvider Um provedor de números aleatórios.
     */
    public Simulation(int numCreatures, double creatureWidth, int horizonWidth, int maxIterations,
                      RandomProvider randomProvider) {
        if (numCreatures <= 0) throw new IllegalArgumentException("Number of creatures must be positive.");
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

        this.guardian = new Guardian(0.0);
        this.creatures.add(this.guardian);
    }

    public Simulation(Guardian guardian, int numCreatures, double creatureWidth, int horizonWidth, int maxIterations,
                      RandomProvider randomProvider) {
        this(numCreatures, creatureWidth, horizonWidth, maxIterations, randomProvider);
        creatures.set(creatures.size() - 1, guardian);
        this.guardian = guardian;
    }

    public boolean prepareNextIteration() {
        this.currentIteration++;
        for (Creature creature : creatures) {
            calculateTargetPosition(creature);
        }
        return currentIteration < maxIterations && !isSimulationSuccessful();
    }

    /**
     * Executa a simulação até que as condições de sucesso sejam atendidas ou o número máximo de iterações seja alcançado.
     */
    public void executeNextIteration() {
        System.out.printf("\n----- Executando a Iteração %d -----\n", currentIteration);
        // todas as criaturas se movem para a posição alvo
        for (Creature creature : creatures) {
            creature.updatePosition();
        }
        // lida com colisões e ações
        handleCollisionsAndActions();

        // não verifica colisões na primeira iteração
        if (currentIteration > 1) {
            handleCollisionsAndActions();
        }

        // verifica se a simulação foi bem-sucedida
        if (isSimulationSuccessful()) {
            isSuccessful = true;
        }

        printResults(); // imprime o resultado da iteração
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
                if (toRemove.contains(c2)) continue;

                if (Math.abs(c1.getPosition() - c2.getPosition()) < creatureWidth / 2.0) {
                    resolveCollision(c1, c2, newClusters, toRemove);
                }
            }
        }

        this.creatures.removeAll(toRemove);
        this.creatures.addAll(newClusters);

        for (Creature c : newClusters) {
            if (c instanceof Cluster newCluster) {
                findAndStealFromClosest(newCluster);
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
        } else if (!(c1 instanceof Guardian) && !(c2 instanceof Guardian)) {
            Cluster cluster = new Cluster(c1, c2);
            toAdd.add(cluster);
            toRemove.add(c1);
            toRemove.add(c2);
        }
    }

    private void findAndStealFromClosest(Cluster cluster) {
        creatures.stream()
            .filter(c -> !(c instanceof Guardian) && !(c instanceof Cluster))
            .min(Comparator.comparingDouble(c -> Math.abs(c.getPosition() - cluster.getPosition())))
            .ifPresent(closest -> {
                System.out.printf("Novo Cluster (moedas: %d) roubando de Creature (moedas: %d)\n",
                    cluster.getCoins(), closest.getCoins());
                cluster.stealFrom(closest);
            });
    }

    private boolean isSimulationSuccessful() {
        long nonGuardianCount = creatures.stream().filter(c -> !(c instanceof Guardian)).count();

        System.out.println(nonGuardianCount);
        System.out.println(creatures.contains(guardian));

        if (nonGuardianCount == 0 && creatures.contains(guardian)) {
            return true;
        }
        if (nonGuardianCount == 1 && creatures.contains(guardian)) {
            Creature lastCreature = creatures.stream().filter(
                c -> !(c instanceof Guardian)).findFirst().orElseThrow();
            return guardian.getCoins() > lastCreature.getCoins();
        }
        return false;
    }

    public void printResults() {
        System.out.println("Estado atual:");
        for (Creature creature : creatures) {
            System.out.println(" - " + creature.toString());
        }
        System.out.printf("Total de criaturas no horizonte: %d\n", creatures.size());
    }

    private void calculateTargetPosition(Creature creature) {
        double r = randomProvider.nextDouble(-1, 1);
        double newPosition = creature.getPosition() + r * creature.getCoins() * factor;
        creature.setTargetPosition(newPosition);
    }

    public List<Creature> getCreatures() {
        return Collections.unmodifiableList(creatures);
    }

    public int getCurrentIteration() {
        return currentIteration;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }
}
