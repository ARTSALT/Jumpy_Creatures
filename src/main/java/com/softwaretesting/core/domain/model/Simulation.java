package com.softwaretesting.core.domain.model;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Representa uma simulação de criaturas em um ambiente com um horizonte.
 * As criaturas interagem entre si formando clusters e roubando moedas.
 * O guardião é uma criatura especial que pode absorver clusters de criaturas.
 */
public class Simulation {

    /**
     * Enum para controlar o estado da simulação numa única iteração.
     * Facilita a execução granular para interfaces gráficas.
     */
    public enum SimulationState {
        READY_TO_PREPARE, // Esperando o início de uma nova iteração
        READY_TO_UPDATE,  // Calcula posições-alvo das criaturas, pronto para o movimento
        READY_TO_RESOLVE, // Criaturas movidas, pronto para resolver colisões
        FINISHED          // Simulação terminada (sucesso ou limite de iterações)
    }

    private Long id;
    private final RandomProvider randomProvider;
    private final double factor;
    private final List<Creature> creatures;
    private final double creatureWidth;
    private final double horizonWidth;
    private final int maxIterations;
    private int currentIteration = 0;
    private SimulationState currentState = SimulationState.READY_TO_PREPARE;
    private boolean isSuccessful;
    private User user;
    private String name;
    private LocalDateTime createdAt;

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

        this.horizonWidth = horizonWidth;
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

    /**
     * Prepara a próxima iteração. Incrementa o contador e calcula as posições-alvo.
     * Transita o estado para READY_TO_UPDATE.
     */
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

    /**
     * Executa o movimento das criaturas para suas posições-alvo.
     * Transita o estado para READY_TO_RESOLVE.
     */
    public void update() {
        if (currentState != SimulationState.READY_TO_UPDATE) return;
        System.out.println("----- Atualizando Posições -----");
        for (Creature creature : creatures) {
            creature.updatePosition();
        }
        this.currentState = SimulationState.READY_TO_RESOLVE;
    }

    /**
     * Resolve as colisões e outras ações da iteração.
     * Verifica se a simulação terminou e transita o estado para READY_TO_PREPARE ou FINISHED.
     */
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

    /**
     * Executa um ciclo completo de iteração (prepare, update, resolve).
     * @return true se a iteração foi executada com sucesso, false se a simulação já terminou e não pôde executar.
     */
    public boolean executeNextIteration() {
        if (currentState == SimulationState.FINISHED) {
            return false;
        }
        if (currentState == SimulationState.READY_TO_PREPARE && currentIteration >= maxIterations) {
            this.currentState = SimulationState.FINISHED;
            return false;
        }

        if (currentState == SimulationState.READY_TO_PREPARE) {
            prepare();
        }
        if (currentState == SimulationState.READY_TO_UPDATE) {
            update();
        }

        resolve();

        return true;
    }

    /**
     * Lida com colisões entre criaturas e executa ações apropriadas.
     */
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
            Cluster newCluster = (Cluster) newCreature;
            findAndStealFromClosest(newCluster);
        }
    }

    /**
     * Resolve a colisão entre duas criaturas.
     * Dependendo do tipo de criaturas, elas podem formar um cluster, roubar moedas ou ser absorvidas por um guardião.
     * @param c1 A primeira criatura envolvida na colisão.
     * @param c2 A segunda criatura envolvida na colisão.
     * @param toAdd Lista de clusters a serem adicionados após a resolução da colisão.
     * @param toRemove Conjunto de criaturas a serem removidas após a resolução da colisão.
     */
    private void resolveCollision(Creature c1, Creature c2,
                          List<Creature> toAdd, Set<Creature> toRemove) {
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

    /**
     * Encontra a criatura mais próxima de um cluster e tenta roubar moedas dela.
     * Se não houver criaturas disponíveis, nada acontece.
     * @param cluster O cluster do qual roubar moedas.
     */
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

    /**
     * Verifica se a simulação foi bem-sucedida.
     * A simulação é considerada bem-sucedida quando restarem apenas o guardião e uma criatura saltitante
     * onde o guardião possui mais moedas.
     * Ou restar apenas o guardião.
     * @return true se a simulação foi bem-sucedida, false caso contrário.
     */
    private boolean isSimulationSuccessful() {
        if (creatures.size() == 1 && creatures.get(0) instanceof Guardian) {
            System.out.println("Simulação bem-sucedida: apenas o guardião permanece.");
            return true;
        }

        if (creatures.size() == 2) {
            Creature c1 = creatures.get(0);
            Creature c2 = creatures.get(1);
            if (c1 instanceof Guardian && c2 != null && c1.getCoins() > c2.getCoins()) {
                System.out.println("Simulação bem-sucedida: guardião com mais moedas que a criatura restante.");
                return true;
            } else if (c2 instanceof Guardian && c1 != null && c2.getCoins() > c1.getCoins()) {
                System.out.println("Simulação bem-sucedida: guardião com mais moedas que a criatura restante.");
                return true;
            }
        }

        return false;
    }

    /**
     * Imprime o estado atual da simulação no terminal, incluindo as criaturas e o total de criaturas no horizonte.
     */
    public void printResults() {
        System.out.println("Estado atual:");
        for (Creature creature : creatures) {
            System.out.println(" - " + creature.toString());
        }
        System.out.printf("Total de criaturas no horizonte: %d\n", creatures.size());
    }

    /**
     * Calcula a nova posição alvo para uma criatura com base em sua posição atual e moedas.
     * A nova posição é calculada como uma variação aleatória proporcional às moedas da criatura
     * conforme a fórmula do requisito: xi ← xi + rgi
     * @param creature A criatura para a qual calcular a nova posição alvo.
     */
    private void calculateTargetPosition(Creature creature) {
        double r = randomProvider.nextDouble(-1, 1);
        double newPosition = creature.getPosition() + r * creature.getCoins() * factor;
        creature.setTargetPosition(newPosition);
    }

    /**
     * Define o número da iteração atual.
     * A lógica de iteração agora é controlada internamente pelos métodos
     * {@link #prepare()} e {@link #executeNextIteration()}.
     */
    public void run(int iteration) {
        this.currentIteration = iteration;
    }

    public SimulationState getCurrentState() { return currentState; }

    public List<Creature> getCreatures() {
        return creatures;
    }

    public int getIterations() {
        return currentIteration;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public double getHorizonWidth() {
        return horizonWidth;
    }

    public void setCurrentState(SimulationState simulationState) {
        this.currentState = simulationState;
    }
}
