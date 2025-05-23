package com.badlogic.drop;

import com.badlogic.drop.entity.Zombie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.of;

public class SimulationTest {

    @Test
    public void testSimulation() {
        int numCreatures = 5;
        int horizonWidth = 1000;

        Simulation simulation = new Simulation(numCreatures, horizonWidth);
        simulation.run();
        simulation.printResults();

        // Verifica se o número de criaturas é igual ao esperado
        assertEquals(numCreatures, simulation.getCreatures().size());

        // Verifica se a posição das criaturas está dentro do intervalo esperado
        for (Zombie creature : simulation.getCreatures()) {
            assertTrue(creature.getPosition() <= horizonWidth && creature.getPosition() >= (horizonWidth * -1));
        }
    }

    @ParameterizedTest
    @MethodSource("generator")
    public void simFlashTest(int numCreatures, int horizonWidth, Exception expectedException) {
        if (expectedException != null) {
            assertThrows(
                IllegalArgumentException.class,
                () -> {
                    Simulation simulation = new Simulation(numCreatures, horizonWidth);
                    simulation.run();
                    simulation.printResults();
                }
            );
        } else {
            Simulation simulation = new Simulation(numCreatures, horizonWidth);
            simulation.run();
            simulation.printResults();

            // Verifica se o número de criaturas é igual ao esperado
            assertEquals(numCreatures, simulation.getCreatures().size());

            // Verifica se a posição das criaturas está dentro do intervalo esperado
            for (Zombie creature : simulation.getCreatures()) {
                assertTrue(creature.getPosition() <= horizonWidth && creature.getPosition() >= (horizonWidth * -1));
            }
        }
    }

    static Stream<Arguments> generator() {
        return Stream.of(
            of(15, 5, 1000, null), // T1 ALL > 0
            of(15, 5, -1000, new IllegalArgumentException()), // T2 HORIZONWIDTH < 0
            of(15, 0, 1000, new IllegalArgumentException()), // T5 HORIZONWIDTH = 0
            of(15, -5, 1000, new IllegalArgumentException()), // T3 NUMCREATURES < 0
            of(0, 5, 1000, new IllegalArgumentException()) // T4 NUMCREATURES = 0
        );
    }

    @Test
    public void testGetters() {
        int iterations = 2;
        int numCreatures = 5;
        int horizonWidth = 1000;

        Simulation simulation = new Simulation(numCreatures, horizonWidth);

        assertEquals(iterations, simulation.getIterations());
        assertEquals(numCreatures, simulation.getCreatures().size());
        assertEquals(horizonWidth, simulation.getHorizonWidth());
    }

    @Test
    public void testAltRun() {
        int iterations = 2;
        int numCreatures = 5;
        int horizonWidth = 1000;

        Simulation simulation = new Simulation(numCreatures, horizonWidth);
        simulation.run(iterations - 1);
        simulation.printResults();

        // Verifica se o número de criaturas é igual ao esperado
        assertEquals(numCreatures, simulation.getCreatures().size());

        // Verifica se a posição das criaturas está dentro do intervalo esperado
        for (Zombie creature : simulation.getCreatures()) {
            assertTrue(creature.getPosition() <= horizonWidth && creature.getPosition() >= (horizonWidth * -1));
        }

        assertEquals(iterations - 1, simulation.getIterations());
    }
}
