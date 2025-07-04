package com.softwaretesting.simulation.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Testes da Classe Cluster")
class ClusterTest {

    private Creature creature1;
    private Creature creature2;

    @BeforeEach
    void setUp() {
        // Arrange: Criaturas base para os testes
        creature1 = new Creature(100, 50.0);
        creature2 = new Creature(50, 60.0);
    }

    @Nested
    @DisplayName("Construtor")
    class ConstructorTests {

        @Test
        @DisplayName("Deve criar um Cluster com a soma das moedas e a posição da primeira criatura")
        void shouldCreateClusterWithCorrectInitialState() {
            // Act: Cria um novo cluster a partir das duas criaturas.
            Cluster cluster = new Cluster(creature1, creature2);

            // Assert: Verifica se o estado inicial do cluster está correto.
            assertThat(cluster.getCoins()).isEqualTo(150); // 100 + 50
            assertThat(cluster.getPosition()).isEqualTo(50.0); // Posição da creature1
            assertThat(cluster.getMembers()).containsExactlyInAnyOrder(creature1, creature2);
        }
    }

    @Nested
    @DisplayName("Método addMember()")
    class AddMemberTests {

        private Cluster cluster;

        @BeforeEach
        void setUp() {
            cluster = new Cluster(creature1, creature2);
        }

        @Test
        @DisplayName("Deve adicionar um novo membro e somar suas moedas ao total do cluster")
        void shouldAddMemberAndIncreaseTotalCoins() {
            // Arrange: Cria uma nova criatura para ser adicionada.
            Creature newMember = new Creature(25, 80.0);
            int initialCoins = cluster.getCoins();

            // Act: Adiciona a nova criatura ao cluster.
            cluster.addMember(newMember);

            // Assert: Verifica se o estado do cluster foi atualizado corretamente.
            assertThat(cluster.getCoins()).isEqualTo(initialCoins + 25);
            assertThat(cluster.getMembers()).contains(newMember);
            assertThat(cluster.getMembers()).hasSize(3);
        }

        @Test
        @DisplayName("Não deve adicionar um membro que já existe no cluster")
        void shouldNotAddExistingMemberAgain() {
            // Arrange: Guarda o estado inicial do cluster.
            int initialCoins = cluster.getCoins();
            int initialMemberCount = cluster.getMembers().size();

            // Act: Tenta adicionar a 'creature1' novamente.
            cluster.addMember(creature1);

            // Assert: O estado do cluster não deve ter mudado.
            assertThat(cluster.getCoins()).isEqualTo(initialCoins);
            assertThat(cluster.getMembers()).hasSize(initialMemberCount);
        }

        @Test
        @DisplayName("Deve lançar exceção ao tentar adicionar um membro nulo (Fronteira)")
        void shouldThrowExceptionWhenAddingNullMember() {
            // Act & Assert: Verifica se a exceção correta é lançada.
            assertThatThrownBy(() -> cluster.addMember(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("A criatura não pode ser nula.");
        }
    }

    @Nested
    @DisplayName("Método stealFrom()")
    class StealFromTests {

        private Cluster cluster;

        @BeforeEach
        void setUp() {
            cluster = new Cluster(creature1, creature2); // Cluster começa com 150 moedas
        }

        @Test
        @DisplayName("Deve roubar metade das moedas de uma criatura com quantidade par")
        void shouldStealHalfCoinsFromCreatureWithEvenCoins() {
            // Arrange: Cria uma vítima com 200 moedas.
            Creature victim = new Creature(200, 10.0);

            // Act: O cluster rouba da vítima.
            cluster.stealFrom(victim);

            // Assert:
            assertThat(cluster.getCoins()).isEqualTo(150 + 100); // Cluster ganha 100
            assertThat(victim.getCoins()).isEqualTo(100);        // Vítima perde 100
        }

        @Test
        @DisplayName("Deve lidar com divisão inteira ao roubar de criatura com quantidade ímpar")
        void shouldHandleIntegerDivisionWhenStealingFromCreatureWithOddCoins() {
            // Arrange: Cria uma vítima com 201 moedas.
            Creature victim = new Creature(201, 10.0);

            // Act: O cluster rouba da vítima (201 / 2 = 100).
            cluster.stealFrom(victim);

            // Assert:
            assertThat(cluster.getCoins()).isEqualTo(150 + 100); // Cluster ganha 100
            assertThat(victim.getCoins()).isEqualTo(101);        // Vítima fica com 201 - 100
        }

        @ParameterizedTest(name = "Roubando de vítima com {0} moedas, deve roubar {1} moedas (Fronteira)")
        @CsvSource({
                "1, 0", // Vítima com 1 moeda (1 / 2 = 0)
                "0, 0"  // Vítima com 0 moedas (0 / 2 = 0)
        })
        void shouldCorrectlyHandleStealingFromCreatureWithOneOrZeroCoins(int victimInitialCoins, int expectedStolen) {
            // Arrange
            Creature victim = new Creature(victimInitialCoins, 10.0);
            int clusterInitialCoins = cluster.getCoins();

            // Act
            cluster.stealFrom(victim);

            // Assert
            assertThat(cluster.getCoins()).isEqualTo(clusterInitialCoins + expectedStolen);
            assertThat(victim.getCoins()).isEqualTo(victimInitialCoins - expectedStolen);
        }

        @Test
        @DisplayName("Um cluster roubando de si mesmo não deve alterar seu total de moedas")
        void aClusterStealingFromItselfShouldNotChangeItsTotalCoins() {
            // Arrange
            int initialCoins = cluster.getCoins(); // 150 moedas

            // Act:
            // 1. cluster.halveCoins() -> moedas do cluster vão para 75, retorna 75
            // 2. cluster.addCoins(75) -> moedas do cluster vão para 75 + 75 = 150
            cluster.stealFrom(cluster);

            // Assert:
            assertThat(cluster.getCoins()).isEqualTo(initialCoins);
        }
    }
}