package com.softwaretesting.simulation;

import com.softwaretesting.simulation.entity.Creature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.of;

/**
 * Testes unitários para a classe Simulation.
 * Verifica o comportamento da simulação de criaturas (zumbis) em um ambiente controlado.
 */
public class SimulationTest {

    @Test
    public void testSimulation() {
        int numCreatures = 5;
        int horizonWidth = 1000;

        Simulation simulation = new Simulation(numCreatures, horizonWidth);
        simulation.run();
        simulation.printResults();

        // Verifica se o número de criaturas é igual ao esperado
        assertThat(numCreatures).isEqualTo(simulation.getCreatures().size());

        // Verifica se a posição das criaturas está dentro do intervalo esperado
        for (Creature creature : simulation.getCreatures()) {
            assertThat(creature.getPosition() <= horizonWidth
                && creature.getPosition() >= (horizonWidth * -1)).isTrue();
        }
    }

    @ParameterizedTest(name = "T{index}: numCreatures={0}, horizonWidth={1}")
    @MethodSource("generator")
    public void simFlashTest(int numCreatures, int numIterations, int horizonWidth,
                             Class<? extends Throwable> expectedException) {
        // verifica se algum argumento lança a exceção esperada
        if (expectedException != null) {
            assertThatThrownBy(() -> new Simulation(numCreatures, horizonWidth)).isInstanceOf(expectedException);
        } else {
            Simulation simulation = new Simulation(numCreatures, horizonWidth);
            simulation.run(numIterations);
            simulation.printResults();

            // Verifica se o número de criaturas é igual ao esperado
            assertThat(numCreatures).isEqualTo(simulation.getCreatures().size());

            // Verifica se a posição das criaturas está dentro do intervalo esperado
            for (Creature creature : simulation.getCreatures()) {
                assertThat(creature.getPosition()).isBetween((double) -horizonWidth, (double) horizonWidth);
            }
        }
    }

    static Stream<Arguments> generator() {
        return Stream.of(
            of(15, 5, 1000, null), // T1 ALL > 0
            of(15, 5, -1000, IllegalArgumentException.class), // T2 HORIZONWIDTH < 0
            of(15, 0, 1000, IllegalArgumentException.class), // T5 HORIZONWIDTH = 0
            of(15, -5, 1000, IllegalArgumentException.class), // T3 NUMCREATURES < 0
            of(0, 5, 1000, IllegalArgumentException.class) // T4 NUMCREATURES = 0
        );
    }

    @Test
    public void testGetters() {
        int iterations = 2;
        int numCreatures = 5;
        int horizonWidth = 1000;

        Simulation simulation = new Simulation(numCreatures, horizonWidth);
        simulation.run(iterations);

        assertThat(iterations).isEqualTo(simulation.getIterations());
        assertThat(numCreatures).isEqualTo(simulation.getCreatures().size());
        assertThat(horizonWidth).isEqualTo(simulation.getHorizonWidth());
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
        assertThat(numCreatures).isEqualTo(simulation.getCreatures().size());

        // Verifica se a posição das criaturas está dentro do intervalo esperado
        for (Creature creature : simulation.getCreatures()) {
            assertThat(creature.getPosition() <= horizonWidth
                && creature.getPosition() >= (horizonWidth * -1)).isTrue();
        }

        assertThat(iterations - 1).isEqualTo(simulation.getIterations());
    }
}
