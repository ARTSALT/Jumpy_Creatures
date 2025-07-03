package com.softwaretesting.simulation.entity;

/**
 * Representa uma criatura no ambiente da simulação.
 * Cada criatura possui uma quantidade de moedas e uma posição no horizonte.
 * As criaturas podem roubar moedas umas das outras e atualizar sua posição por saltos.
 */
public class Creature {

    // moedas e posição da criatura
    int coins;
    private double position;
    private double targetPosition;

    /**
     * Cria uma nova criatura com a quantidade de moedas e a posição inicial especificadas.
     * @param coins A quantidade de moedas da criatura, não pode ser negativa.
     * @param initialPosition A posição inicial da criatura.
     */
    public Creature(int coins, double initialPosition) {
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

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
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
