package com.badlogic.drop;

import com.badlogic.drop.entity.Zombie;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Simulation {
    private static final Random random = new Random();
    private int iterations;                     // número de iterações
    private final int horizonWidth;             // largura do horizonte
    private final List<Zombie> creatures;       // lista de criaturas
    private Iterator<Zombie> iterator;          // iterador para percorrer a lista de criaturas
    private final double factor;                // fator de conversão de moedas para a largura do horizonte

    /**
     * Cria uma nova simulação com o número de criaturas e a largura do horizonte especificados.
     * @param numCreatures número de criaturas
     * @param horizonWidth largura do horizonte
     */
    public Simulation(int numCreatures, int horizonWidth) {
        if (numCreatures <= 0) {
            throw new IllegalArgumentException("O número de criaturas não pode ser negativo ou zero.");
        }

        if (horizonWidth <= 0) {
            throw new IllegalArgumentException("A largura do horizonte não pode ser negativa ou zero.");
        }

        this.horizonWidth = horizonWidth;
        this.factor = horizonWidth / 1000000.0;
        this.creatures = new ArrayList<>(numCreatures);

        // gera 'numCreatures' criaturas
        for (int i = 0; i < numCreatures; i++) {
            // cria uma nova criatura com 1000000 moedas e posição inicial na metade da largura do horizonte
            creatures.add(new Zombie(1000000, horizonWidth / 2f));
        }

        iterator = creatures.iterator();
    }

    // processa todas as criaturas de uma vez
    public void run() {
        for (int i = 0; i < iterations; i++) {
            for (Zombie creature : creatures) {
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
    private double generateRandom() { return random.nextDouble(-1, 1); }

    public void printResults() {
        for (int i = 0; i < creatures.size(); i++) {
            Zombie creature = creatures.get(i);
            System.out.println("Creature " + i + ": " + creature.getCoins() + " coins, position: " + creature.getPosition());
        }
    }

    // getters
    public int getIterations() {
        return iterations;
    }

    public int getHorizonWidth() {
        return horizonWidth;
    }

    public List<Zombie> getCreatures() {
        return creatures;
    }

    // processa a criatura atual e a retorna
    public Zombie process() {
        if (!iterator.hasNext()) {
            if (creatures.isEmpty()) {
                return null;
            }
            iterator = creatures.iterator();    // reinicia o iterador
        }

        Zombie creature = iterator.next();
        creature.setTargetPosition((creature.getPosition() + generateRandom() * creature.getCoins()) * factor);
        creature.reset();

        return creature;
    }
}
