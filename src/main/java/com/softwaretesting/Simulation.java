package com.softwaretesting;

import com.softwaretesting.entity.Creature;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Simulation {
    private static final Random random = new Random();
    private int iterations;                     // número de iterações
    private final int horizonWidth;             // largura do horizonte
    private final List<Creature> creatures;     // lista de criaturas
    private final double factor;                // fator de conversão de moedas para a largura do horizonte

    public Simulation(int iterations, int numCreatures, int horizonWidth) {
        if (iterations < 0 || numCreatures < 0 || horizonWidth < 0) {
            throw new IllegalArgumentException("Os parâmetros não podem ser negativos.");
        }

        if (iterations == 0 || numCreatures == 0 || horizonWidth == 0) {
            throw new IllegalArgumentException("Os parâmetros não podem ser zero.");
        }

        if (numCreatures > horizonWidth) {
            throw new IllegalArgumentException("O número de criaturas não pode ser maior que a largura do horizonte.");
        }

        this.iterations = iterations;
        this.horizonWidth = horizonWidth;

        this.factor = horizonWidth / 1000000.0;

        this.creatures = new ArrayList<>(numCreatures);

        // gera 'numCreatures' criaturas, cada uma com 1000000 moedas inicialmente
        // e posição inicial sendo a metade da largura do horizonte
        for (int i = 0; i < numCreatures; i++) {
            this.creatures.add(new Creature(0.5 * horizonWidth));
        }
    }

    public void run() {
        for (int i = 0; i < iterations; i++) {
            for (Creature creature : creatures) {
                // desloca a criatura proporcionalmente a quantidade de moedas e a largura do horizonte
                creature.setPosition(creature.getPosition() + generateRandom() * creature.getCoins() * factor);
            }
        }
    }

    public void run(int iterations) {
        this.iterations = iterations;
        run();
    }

    // gera um número aleatório entre -1 e 1
    private double generateRandom() { return random.nextDouble(-1, 1); }

    public void printResults() {
        for (int i = 0; i < creatures.size(); i++) {
            System.out.println("Creature " + i + ": " + creatures.get(i).getPosition());
        }
    }
}
