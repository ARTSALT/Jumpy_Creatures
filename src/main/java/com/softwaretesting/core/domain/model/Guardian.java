package com.softwaretesting.core.domain.model;

/**
 * Representa um Guardião no ambiente da simulação.
 * O Guardião é uma criatura especial que pode absorver moedas de clusters e eliminá-los.
 */
public class Guardian extends Creature {

    /**
     * Cria um novo Guardião com a quantidade de moedas e a posição inicial especificadas.
     * @param initialPosition A posição inicial da criatura.
     */
    public Guardian(double initialPosition) {
        super(0, initialPosition);
    }

    /**
     * Absorve todas as moedas de um cluster, efetivamente eliminando-o.
     * @param cluster O cluster a ser absorvido.
     */
    public void absorbCluster(Cluster cluster) {
        this.addCoins(cluster.getCoins());
    }
}
