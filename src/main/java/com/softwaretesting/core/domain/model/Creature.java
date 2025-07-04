package com.softwaretesting.core.domain.model;

import java.util.Objects;

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
            // ATUALIZADO: Registra o ganho de moedas.
            this.lastCoinsDelta = amount;
        }
    }

    /**
     * NOVO: Retorna a última variação de moedas.
     * @return A quantidade de moedas ganhas (positivo) ou perdidas (negativo).
     */
    public int getLastCoinsDelta() {
        return lastCoinsDelta;
    }

    /**
     * NOVO: Reseta a variação de moedas para o próximo turno.
     * Deve ser chamado no início de cada iteração.
     */
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
}
