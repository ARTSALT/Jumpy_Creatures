package com.softwaretesting.core.domain.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa um cluster de criaturas que colidiram.
 * Um cluster é formado por duas ou mais criaturas que se encontram próximas no horizonte.
 * O cluster acumula as moedas das criaturas envolvidas e pode roubar moedas de outras criaturas.
 */
public class Cluster extends Creature {

    // a lista de membros do cluster
    private final List<Creature> members;
    public boolean firstIteration = true;

    /**
     * Cria um novo cluster a partir de duas criaturas que colidiram.
     * O cluster é formado na posição da primeira criatura.
     */
    public Cluster(Creature creature1, Creature creature2) {
        super(creature1.getCoins() + creature2.getCoins(), creature1.getPosition());

        this.members = new ArrayList<>();
        this.members.add(creature1);
        this.members.add(creature2);
    }

    /**
     * Adiciona uma nova criatura ao cluster.
     * @param creature A criatura a ser adicionada.
     */
    public void addMember(Creature creature) {
        if (creature == null) {
            throw new IllegalArgumentException("A criatura não pode ser nula.");
        }

        if (!this.members.contains(creature)) {
            this.members.add(creature);
            this.addCoins(creature.getCoins());
        }
    }

    public List<Creature> getMembers() {
        return members;
    }
}
