package com.softwaretesting.core.domain.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Simulation {
    private final RandomProvider randomProvider;
    private int iterations;                     // número de iterações
    private final int horizonWidth;             // largura do horizonte
    private final List<Creature> creatures;     // lista de criaturas
    private Iterator<Creature> iterator;        // iterador para percorrer a lista de criaturas
    private final double factor;                // fator de conversão de moedas para a largura do horizonte

    /**
     * Cria uma nova simulação com o número de criaturas e a largura do horizonte especificados.
     * @param numCreatures número de criaturas
     * @param iterations número de iterações
     * @param horizonWidth largura do horizonte
     * @param randomProvider provedor de números aleatórios
     */
    public Simulation(int numCreatures, int iterations, int horizonWidth, RandomProvider randomProvider) {
        if (numCreatures <= 0) {
            throw new IllegalArgumentException("O número de criaturas não pode ser negativo ou zero.");
        }

        if (iterations <= 0) {
            throw new IllegalArgumentException("O número de iterações não pode ser negativo ou zero.");
        }

        if (horizonWidth <= 0) {
            throw new IllegalArgumentException("A largura do horizonte não pode ser negativa ou zero.");
        }

        this.randomProvider = randomProvider;
        this.horizonWidth = horizonWidth;
        this.factor = horizonWidth / 1000000.0;
        this.creatures = new ArrayList<>(numCreatures);

        // gera 'numCreatures' criaturas
        for (int i = 0; i < numCreatures; i++) {
            // cria uma nova criatura com 1000000 moedas e posição inicial na metade da largura do horizonte
            creatures.add(new Creature(1000000, horizonWidth / 2f));
        }

        iterator = creatures.iterator();
    }

    // processa todas as criaturas de uma vez
    public void run() {
        for (int i = 0; i < iterations; i++) {
            for (Creature creature : creatures) {
                // desloca a criatura proporcionalmente a quantidade de moedas e a largura do horizonte
                creature.setTargetPosition((creature.getPosition() + generateRandom() * creature.getCoins()) * factor);
            }
        }
    }

    // processa as criaturas por um número de iterações
    public void run(int iterations) {
        this.iterations = iterations;
        run();
    }

    // gera um número aleatório entre -1 e 1
    private double generateRandom() { return randomProvider.nextDouble(-1, 1); }

    public void printResults() {
        System.out.println("\n================================\n");
        System.out.println("Iteração " + iterations);
        for (int i = 0; i < creatures.size(); i++) {
            Creature creature = creatures.get(i);
            System.out.println("Criatura " + i + ": " + creature.getCoins() + " moedas, horizonte: "
                + creature.getPosition());
        }
    }

    // getters
    public int getIterations() {
        return iterations;
    }

    public int getHorizonWidth() {
        return horizonWidth;
    }

    public List<Creature> getCreatures() {
        return creatures;
    }

    // processa a criatura atual e a retorna
    public Creature process() {
        if (!iterator.hasNext()) {
            if (creatures.isEmpty()) {
                return null;
            }
            iterator = creatures.iterator();    // reinicia o iterador
            printResults();                     // imprime os resultados da iteração
        }

        Creature creature = iterator.next();
        creature.setTargetPosition((creature.getPosition() + generateRandom() * creature.getCoins()) * factor);

        return creature;
    }
}
