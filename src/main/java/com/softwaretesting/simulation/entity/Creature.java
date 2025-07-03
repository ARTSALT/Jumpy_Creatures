package com.softwaretesting.simulation.entity;

/**
 * Representa uma criatura no ambiente da simulação.
 * Cada criatura possui uma quantidade de moedas e uma posição no horizonte.
 * As criaturas podem roubar moedas umas das outras e atualizar sua posição por saltos.
 */
public class Creature {

    // moedas e posição da criatura
    protected int coins;
    protected double position;
    protected double targetPosition;

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
     * Reduz as moedas da criatura pela metade e adiciona a quantidade roubada.
     * @return O número de moedas que foram roubadas.
     */
    public int halveCoins() {
        int half = coins / 2;
        coins -= half;
        return half;
    }

    /**
     * Adiciona uma quantidade de moedas à criatura.
     * @param amount A quantidade a ser adicionada.
     */
    public void addCoins(int amount) {
        if (amount > 0) {
            this.coins += amount;
        }
    }

    // getters e setters
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

    @Override
    public String toString() {
        String type = this.getClass().getSimpleName();
        if(type.isEmpty()) type = "Creature";
        return String.format("%s{moedas=%d, posicao=%.2f}", type, coins, position);
    }
}
