package com.softwaretesting.simulation.entity;

import com.softwaretesting.simulation.entity.Creature;

public class Guardian extends Creature{

    /**
     * Cria uma nova criatura com a quantidade de moedas e a posição inicial especificadas.
     *
     * @param coins           A quantidade de moedas da criatura, não pode ser negativa.
     * @param initialPosition A posição inicial da criatura.
     */
    public Guardian(int coins, double initialPosition) {
        super(coins, initialPosition);
    }

    /**
     * Rouba todas as moedas de um cluster de criaturas.
     */
    public int killCluster(Creature otherCreature) {
        this.coins += otherCreature.getCoins();
        otherCreature.setCoins(0);
        return this.coins;
    }
}
