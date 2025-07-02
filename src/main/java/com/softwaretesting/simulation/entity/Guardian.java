package com.softwaretesting.simulation.entity;

import com.softwaretesting.simulation.entity.Creature;

public class Guardian {
    // moedas e posição da criatura
    private int coins;
    private double position;
    private double targetPosition;

    /**
     * Cria uma nova criatura com a quantidade de moedas e a posição inicial especificadas.
     * @param coins A quantidade de moedas da criatura, não pode ser negativa.
     * @param initialPosition A posição inicial da criatura.
     */
    public Guardian(int coins, double initialPosition) {
        if (coins < 0) {
            throw new IllegalArgumentException("A quantidade de moedas não pode ser negativa.");
        }

        this.coins = coins;
        this.position = initialPosition;
        this.targetPosition = initialPosition;
    }

    /**
     * Define a nova posição da criatura após um salto/movimento.
     */
    public void updatePosition() {
        this.position = this.targetPosition;
    }

    /**
     * Reduz as moedas da criatura pela metade e retorna a quantidade removida.
     * @return O número de moedas que foram roubadas.
     */
    int halveCoins() {
        int half = coins / 2;
        coins -= half;
        return half;
    }

    /**
     * Rouba metade das moedas de outra criatura.
     * @param otherCreature A criatura da qual roubar.
     * @return O número de moedas roubadas.
     */
    public int stealFrom(Creature otherCreature) {
        int stolenCoins = otherCreature.halveCoins();
        this.coins += stolenCoins;
        return stolenCoins;
    }

    /**
     * Rouba todas as moedas de um cluster de criaturas.
     */
    public int killCluster(Creature otherCreature) {
        this.coins += otherCreature.getCoins();
        otherCreature.setCoins(0);
        return this.coins;
    }

    public int getCoins() {
        return coins;
    }

    public double getPosition() {
        return position;
    }

    public double getTargetPosition() {
        return targetPosition;
    }

    public void setTargetPosition(double targetPosition) {
        this.targetPosition = targetPosition;
    }
}
