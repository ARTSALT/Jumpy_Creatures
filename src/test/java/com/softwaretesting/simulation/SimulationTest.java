//package com.softwaretesting.simulation;
//
//import com.softwaretesting.core.domain.model.Simulation;
//import com.softwaretesting.core.domain.model.Creature;
//import com.softwaretesting.core.domain.model.RandomProvider;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.Arguments;
//import org.junit.jupiter.params.provider.MethodSource;
//
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//import java.util.stream.Stream;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//
//@DisplayName("Testes da Classe Simulation")
//class SimulationTest {
//
//    // Provedor random "fake" que será usado em todos os testes.
//    private PredictableRandomProvider fakeRandom;
//
//    /**
//     * Cria uma instância do provedor fake antes de cada teste.
//     */
//    @BeforeEach
//    void setUp() {
//        fakeRandom = new PredictableRandomProvider();
//    }
//
//    /**
//     * Implementação de RandomProvider para testes.
//     * Ela nos dá controle total sobre o número "aleatório" que será gerado.
//     */
//    static class PredictableRandomProvider implements RandomProvider {
//        private double valueToReturn;
//
//        public void setValueToReturn(double value) {
//            this.valueToReturn = value;
//        }
//
//        @Override
//        public double nextDouble(double min, double max) {
//            return this.valueToReturn;
//        }
//    }
//
//    @Nested
//    @DisplayName("Testes de Construtor (Domínio, Fronteira e MC/DC)")
//    class ConstructorTests {
//
//        @Test
//        @DisplayName("Deve criar a simulação com sucesso com parâmetros válidos")
//        void shouldCreateSimulationSuccessfullyWithValidParameters() {
//            int numCreatures = 10;
//            int iterations = 5;
//            int horizonWidth = 1000;
//
//            Simulation simulation = new Simulation(numCreatures, iterations, horizonWidth, fakeRandom);
//
//            assertThat(simulation.getCreatures()).hasSize(numCreatures);
//            assertThat(simulation.getHorizonWidth()).isEqualTo(horizonWidth);
//            assertThat(simulation.getIterations()).isZero();
//
//            for (Creature creature : simulation.getCreatures()) {
//                assertThat(creature).isNotNull();
//                assertThat(creature.getCoins()).isEqualTo(1_000_000);
//                assertThat(creature.getPosition()).isEqualTo(horizonWidth / 2.0);
//            }
//        }
//
//        @ParameterizedTest(name = "Falha com numCreatures={0}, iterations={1}, horizonWidth={2}")
//        @MethodSource("com.softwaretesting.simulation.SimulationTest#invalidConstructorArgumentsProvider")
//        @DisplayName("Deve lançar exceção para argumentos inválidos no construtor")
//        void shouldThrowExceptionForInvalidConstructorArguments(
//            int numCreatures, int iterations, int horizonWidth, String expectedMessage) {
//
//            assertThatThrownBy(() -> new Simulation(numCreatures, iterations, horizonWidth, fakeRandom))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessage(expectedMessage);
//        }
//    }
//
//    static Stream<Arguments> invalidConstructorArgumentsProvider() {
//        return Stream.of(
//            Arguments.of(0, 10, 1000, "O número de criaturas não pode ser negativo ou zero."),
//            Arguments.of(-1, 10, 1000, "O número de criaturas não pode ser negativo ou zero."),
//            Arguments.of(10, 0, 1000, "O número de iterações não pode ser negativo ou zero."),
//            Arguments.of(10, -1, 1000, "O número de iterações não pode ser negativo ou zero."),
//            Arguments.of(10, 10, 0, "A largura do horizonte não pode ser negativa ou zero."),
//            Arguments.of(10, 10, -5, "A largura do horizonte não pode ser negativa ou zero.")
//        );
//    }
//
//    @Nested
//    @DisplayName("Testes de Integração (Interação com Creature)")
//    class IntegrationTests {
//
//        @Test
//        @DisplayName("run() deve atualizar apenas a targetPosition das criaturas, não a posição atual")
//        void runShouldOnlyUpdateTargetPositionOfCreatures() {
//            fakeRandom.setValueToReturn(0.8); // Define um valor qualquer para garantir que o alvo mude
//            Simulation simulation = new Simulation(5, 1, 1000, fakeRandom);
//            List<Creature> creatures = simulation.getCreatures();
//            double initialPosition = creatures.getFirst().getPosition();
//            double initialTargetPosition = creatures.getFirst().getTargetPosition();
//            assertThat(initialPosition).isEqualTo(initialTargetPosition);
//
//            simulation.run(1);
//
//            assertThat(creatures.getFirst().getPosition()).isEqualTo(initialPosition);
//            assertThat(creatures.getFirst().getTargetPosition()).isNotEqualTo(initialTargetPosition);
//        }
//
//        @Test
//        @DisplayName("process() deve iterar pelas criaturas e reiniciar o iterador no final")
//        void processShouldIterateThroughCreaturesAndResetIterator() {
//            fakeRandom.setValueToReturn(0.1); // Define um valor para a chamada a process()
//            int numCreatures = 3;
//            Simulation simulation = new Simulation(numCreatures, 1, 1000, fakeRandom);
//            Set<Creature> processedCreatures = new HashSet<>();
//
//            for (int i = 0; i < numCreatures; i++) {
//                Creature processed = simulation.process();
//                assertThat(processed).isNotNull();
//                assertThat(processedCreatures.add(processed)).isTrue();
//            }
//            assertThat(processedCreatures).hasSize(numCreatures);
//
//            Creature firstCreatureAgain = simulation.process();
//            Creature expectedFirstCreature = simulation.getCreatures().getFirst();
//
//            assertThat(firstCreatureAgain).isSameAs(expectedFirstCreature);
//        }
//
//        @Test
//        @DisplayName("process() deve retornar null se a lista de criaturas estiver vazia")
//        void processShouldReturnNullForEmptyCreatureList() {
//            Simulation simulation = new Simulation(1, 1, 100, fakeRandom);
//            simulation.getCreatures().clear();
//
//            Creature result = simulation.process();
//
//            assertThat(result).isNull();
//        }
//
//        @Test
//        @DisplayName("Deve calcular a posição alvo corretamente com um fator aleatório positivo")
//        void shouldCalculateTargetPositionCorrectlyWithPositiveRandomFactor() {
//            fakeRandom.setValueToReturn(0.5);
//            Simulation simulation = new Simulation(1, 1, 1000, fakeRandom);
//            Creature creature = simulation.getCreatures().getFirst();
//            double factor = 1000.0 / 1_000_000.0;
//            double expectedTargetPosition = (500 + 0.5 * 1_000_000) * factor;
//
//            simulation.run(1);
//
//            assertThat(creature.getTargetPosition()).isEqualTo(expectedTargetPosition);
//        }
//
//        @Test
//        @DisplayName("Deve calcular a posição alvo corretamente com um fator aleatório negativo")
//        void shouldCalculateTargetPositionCorrectlyWithNegativeRandomFactor() {
//            fakeRandom.setValueToReturn(-1.0);
//            Simulation simulation = new Simulation(1, 1, 1000, fakeRandom);
//            Creature creature = simulation.getCreatures().getFirst();
//            double factor = 1000.0 / 1_000_000.0;
//            double expectedTargetPosition = (500 + (-1.0) * 1_000_000) * factor;
//
//            simulation.run(1);
//
//            assertThat(creature.getTargetPosition()).isEqualTo(expectedTargetPosition);
//        }
//    }
//
//    @Nested
//    @DisplayName("Testes de Estado e Getters")
//    class StateAndGetterTests {
//
//        @Test
//        @DisplayName("run(int) deve atualizar corretamente o número de iterações")
//        void runWithParameterShouldUpdateIterations() {
//            Simulation simulation = new Simulation(5, 1, 1000, fakeRandom);
//
//            simulation.run(10);
//
//            assertThat(simulation.getIterations()).isEqualTo(10);
//        }
//    }
//}
