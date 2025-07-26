package com.softwaretesting.core.domain.model;

import java.util.Objects;

/**
 * Representa uma criatura no ambiente da simulação.
 * As criaturas podem ter moedas, se mover e interagir com outras criaturas.
 * Cada criatura tem um identificador único, quantidade de moedas e posição no horizonte.
 */
public class Creature {

    private static int nextId = 0;
    private final int id;
    protected int coins;
    protected double position;
    protected double targetPosition;
    private int lastCoinsDelta = 0;

    public Creature(int coins, double initialPosition) {
        if (coins < 0) {
            throw new IllegalArgumentException("A quantidade de moedas não pode ser negativa.");
        }
        this.id = nextId++;
        this.coins = coins;
        this.position = initialPosition;
        this.targetPosition = initialPosition;
    }

    /**
     * Construtor para uso exclusivo em testes. Permite a injeção de um ID.
     * @param id O ID a ser atribuído à criatura.
     * @param coins A quantidade inicial de moedas.
     * @param initialPosition A posição inicial.
     */
    Creature(int id, int coins, double initialPosition) {
        this.id = id;
        this.coins = coins;
        this.position = initialPosition;
        this.targetPosition = initialPosition;
    }

    public int getId() {
        return id;
    }

    public void updatePosition() {
        this.position = this.targetPosition;
    }

    public int halveCoins() {
        int half = coins / 2;
        coins -= half;
        this.lastCoinsDelta = -half;
        return half;
    }

    public void addCoins(int amount) {
        if (amount > 0) {
            this.coins += amount;
            this.lastCoinsDelta = amount;
        }
    }

    public int getLastCoinsDelta() {
        return lastCoinsDelta;
    }

    public void resetTurnDelta() {
        this.lastCoinsDelta = 0;
    }

    // Getters e Setters
    public int getCoins() { return coins; }
    public void setCoins(int coins) { this.coins = coins; }
    public double getPosition() { return position; }
    public double getTargetPosition() { return targetPosition; }
    public void setTargetPosition(double targetPosition) { this.targetPosition = targetPosition; }

    @Override
    public String toString() {
        String type = this.getClass().getSimpleName();
        if(type.isEmpty()) type = "Creature";
        return String.format("%s[id=%d]{moedas=%d, posicao=%.2f}", type, id, coins, position);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Creature creature = (Creature) o;
        return id == creature.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public void setPosition(double i) {
        if (i < 0) {
            throw new IllegalArgumentException("A posição não pode ser negativa.");
        }
        this.position = i;
        this.targetPosition = i;
    }

    public int stealFrom(Creature otherCreature) {
        int stolenCoins = otherCreature.halveCoins();
        this.addCoins(stolenCoins);
        return stolenCoins;
    }
}
