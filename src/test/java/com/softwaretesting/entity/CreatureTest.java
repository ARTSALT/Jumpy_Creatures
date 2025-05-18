package com.softwaretesting.entity;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class CreatureTest {

    @Test
    public void testCreature() {
        Creature creature = new Creature(1000000, 0.5);
        assertEquals(1000000, creature.getCoins());
        assertEquals(0.5, creature.getPosition(), 0.01);

        Creature creature2 = new Creature(0, 0);
        assertEquals(0, creature2.getCoins());
        assertEquals(0, creature2.getPosition(), 0.01);

        Creature creature3 = new Creature(-500000, -0.75);
        assertEquals(-500000, creature3.getCoins());
        assertEquals(-0.75, creature3.getPosition(), 0.01);

        Creature creature4 = new Creature(10);
        assertEquals(10, creature4.getPosition(), 0.01);

        creature4.setCoins(250000);
        creature4.setPosition(11);
        assertEquals(250000, creature4.getCoins());
        assertEquals(11, creature4.getPosition(), 0.01);
    }

    @Test
    public void testStealCoins() {
        Creature creature1 = new Creature(1000000, 0.5);
        Creature creature2 = new Creature(500000, 0.3);

        creature1.stealCoins(creature2);

        assertEquals(1250000, creature1.getCoins());
        assertEquals(250000, creature2.getCoins());
    }
}
