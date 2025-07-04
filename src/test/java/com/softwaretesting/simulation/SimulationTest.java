package com.softwaretesting.simulation;

import com.softwaretesting.simulation.entity.Cluster;
import com.softwaretesting.simulation.entity.Creature;
import com.softwaretesting.simulation.entity.Guardian;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@DisplayName("Testes da Classe Simulation")
class SimulationTest {

    // Classe utilitária para testes que usam reflexão para acessar estado privado.
    public class TestUtils {
        public static void setCreatures(Simulation simulation, List<Creature> creatures) throws Exception {
            Field field = Simulation.class.getDeclaredField("creatures");
            field.setAccessible(true);
            field.set(simulation, creatures);
        }
    }

    @Mock
    private RandomProvider randomProvider;

    private Simulation simulation;

    // Parâmetros padrão para a simulação
    private final int NUM_CREATURES = 10;
    private final double CREATURE_WIDTH = 10.0;
    private final int HORIZON_WIDTH = 1000;
    private final int MAX_ITERATIONS = 10;

    @BeforeEach
    void setUp() {
        // Configuração padrão para a maioria dos testes
        simulation = new Simulation(NUM_CREATURES, CREATURE_WIDTH, HORIZON_WIDTH, MAX_ITERATIONS, randomProvider);
    }

    @Nested
    @DisplayName("Construtor e Inicialização")
    class ConstructorTests {

        @Test
        @DisplayName("Deve criar a simulação com o número correto de criaturas e um guardião")
        void shouldCreateSimulationWithCorrectInitialState() {
            assertThat(simulation.getCreatures()).hasSize(NUM_CREATURES + 1);
            assertThat(simulation.getCreatures().stream().filter(c -> c instanceof Guardian).count()).isEqualTo(1);
            assertThat(simulation.getCreatures().stream().filter(c -> c instanceof Creature && !(c instanceof Guardian)).count()).isEqualTo(NUM_CREATURES);
            assertThat(simulation.getCurrentIteration()).isZero();
            assertThat(simulation.isSuccessful()).isFalse();
        }

        @Test
        @DisplayName("Deve lançar exceção para número de criaturas não positivo")
        void shouldThrowExceptionForNonPositiveCreatureCount() {
            assertThatThrownBy(() -> new Simulation(0, CREATURE_WIDTH, HORIZON_WIDTH, MAX_ITERATIONS, randomProvider))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> new Simulation(-1, CREATURE_WIDTH, HORIZON_WIDTH, MAX_ITERATIONS, randomProvider))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Fluxo da Iteração")
    class IterationFlowTests {

        @Test
        @DisplayName("prepareNextIteration deve avançar a iteração e calcular novas posições alvo")
        void prepareNextIterationShouldAdvanceIterationAndCalculateTargets() {
            // Arrange: Mock do provedor aleatório para retornar um valor previsível
            when(randomProvider.nextDouble(-1, 1)).thenReturn(0.5);

            // Act: Prepara a próxima iteração
            boolean shouldContinue = simulation.prepareNextIteration();

            // Assert:
            assertThat(shouldContinue).isTrue();
            assertThat(simulation.getCurrentIteration()).isEqualTo(1);
            // Verifica se a posição alvo de uma criatura foi calculada corretamente
            Creature firstCreature = simulation.getCreatures().get(0);
            double expectedPosition = 500.0 + 0.5 * 1_000_000 * (1000 / 1_000_000.0);
            assertThat(firstCreature.getTargetPosition()).isEqualTo(expectedPosition);
        }

        @Test
        @DisplayName("prepareNextIteration deve retornar false quando o máximo de iterações for atingido")
        void prepareNextIterationShouldReturnFalseAtMaxIterations() {
            // Arrange: Cria uma simulação que dura apenas 1 iteração
            Simulation shortSimulation = new Simulation(1, 10.0, 100, 1, randomProvider);

            // Act & Assert: A primeira chamada prepara a iteração 1, mas a próxima verificação falha
            assertThat(shortSimulation.prepareNextIteration()).isFalse();
            assertThat(shortSimulation.getCurrentIteration()).isEqualTo(1);
        }

        @Test
        @DisplayName("executeNextIteration deve mover as criaturas para suas posições alvo")
        void executeNextIterationShouldMoveCreaturesToTarget() {
            // Arrange
            when(randomProvider.nextDouble(-1, 1)).thenReturn(0.1);
            simulation.prepareNextIteration();
            Creature creature = simulation.getCreatures().get(0);
            double targetPosition = creature.getTargetPosition();

            // Act
            simulation.executeNextIteration();

            // Assert
            assertThat(creature.getPosition()).isEqualTo(targetPosition);
        }
    }

    @Nested
    @DisplayName("Lógica de Colisão e Ações")
    class CollisionLogicTests {

        @Test
        @DisplayName("Deve formar um Cluster quando duas Criaturas colidem")
        void shouldFormClusterOnCreatureCollision() throws Exception {
            // Arrange: Cria uma simulação com 2 criaturas e 1 guardião
            Simulation sim = new Simulation(2, CREATURE_WIDTH, HORIZON_WIDTH, MAX_ITERATIONS, randomProvider);
            List<Creature> creatures = new ArrayList<>(sim.getCreatures());
            Creature c1 = creatures.get(0);
            Creature c2 = creatures.get(1);

            // Força a colisão definindo posições próximas
            c1.setTargetPosition(100.0);
            c2.setTargetPosition(100.1);
            c1.updatePosition();
            c2.updatePosition();

            // Act: Executa a lógica de colisão
            sim.executeNextIteration();

            // Assert: As duas criaturas devem ser substituídas por um cluster
            List<Creature> finalCreatures = sim.getCreatures();
            assertThat(finalCreatures).hasSize(2); // O Cluster e o Guardião
            assertThat(finalCreatures.stream().anyMatch(c -> c instanceof Cluster)).isTrue();
        }

        @Test
        @DisplayName("Guardião deve absorver Cluster em uma colisão")
        void shouldGuardianAbsorbClusterOnCollision() throws Exception {
            // Arrange: Prepara um cenário com um guardião e um cluster próximos
            Guardian guardian = new Guardian(100.0); // Posição 100.0
            Cluster cluster = new Cluster(new Creature(100, 100.2), new Creature(50, 200.0)); // Posição 100.2
            int initialGuardianCoins = guardian.getCoins();
            int clusterCoins = cluster.getCoins();

            List<Creature> creatureList = new ArrayList<>();
            creatureList.add(guardian);
            creatureList.add(cluster);
            TestUtils.setCreatures(simulation, creatureList); // Usa reflexão para injetar o estado

            // Act
            simulation.executeNextIteration();

            // Assert: O guardião deve ter absorvido o cluster
            assertThat(simulation.getCreatures()).hasSize(1).contains(guardian);
            assertThat(guardian.getCoins()).isEqualTo(initialGuardianCoins + clusterCoins);
        }

        @Test
        @DisplayName("Cluster deve roubar da Criatura mais próxima após ser formado")
        void shouldNewClusterStealFromClosestCreature() throws Exception {
            // Arrange: 3 criaturas e 1 guardião
            Simulation sim = new Simulation(3, CREATURE_WIDTH, HORIZON_WIDTH, MAX_ITERATIONS, randomProvider);
            Creature c1 = new Creature(1000, 100.0);
            Creature c2 = new Creature(1000, 100.1); // Irão colidir
            Creature victim = new Creature(500, 110.0); // Vítima mais próxima
            Guardian guardian = new Guardian(500.0); // Longe

            TestUtils.setCreatures(sim, new ArrayList<>(List.of(c1, c2, victim, guardian)));

            // Act
            sim.executeNextIteration();

            // Assert: Um cluster é formado e ele rouba da vítima
            assertThat(sim.getCreatures().stream().anyMatch(c -> c instanceof Cluster)).isTrue();
            assertThat(victim.getCoins()).isEqualTo(250); // Vítima perde metade das moedas

            Cluster newCluster = (Cluster) sim.getCreatures().stream().filter(c -> c instanceof Cluster).findFirst().get();
            // Moedas do cluster = (c1 + c2) + (vítima / 2)
            assertThat(newCluster.getCoins()).isEqualTo(1000 + 1000 + 250);
        }
    }

    @Nested
    @DisplayName("Condições de Sucesso")
    class SuccessConditionTests {

        @Test
        @DisplayName("Deve ser bem-sucedida se Guardião tiver mais moedas que a última criatura")
        void shouldBeSuccessfulIfGuardianHasMoreCoinsThanLastCreature() throws Exception {
            // Arrange: Guardião (rico) e uma criatura (pobre)
            Guardian guardian = new Guardian(0.0);
            guardian.addCoins(2000001);
            Creature lastCreature = new Creature(1000, 10.0);
            Simulation simulation = new Simulation(guardian,2, 10.0, 1000,
                    10, randomProvider);

            // Act
            simulation.executeNextIteration();

            // Assert
            assertThat(simulation.isSuccessful()).isTrue();
        }

        @Test
        @DisplayName("NÃO deve ser bem-sucedida se Guardião tiver menos moedas que a última criatura")
        void shouldNotBeSuccessfulIfGuardianHasFewerCoins() throws Exception {
            // Arrange: Guardião (pobre) e uma criatura (rica)
            Guardian guardian = new Guardian(0.0);
            guardian.addCoins(999);
            Creature lastCreature = new Creature(1000, 10.0);
            TestUtils.setCreatures(simulation, new ArrayList<>(List.of(guardian, lastCreature)));

            // Act
            simulation.executeNextIteration();

            // Assert
            assertThat(simulation.isSuccessful()).isFalse();
        }
    }
}
