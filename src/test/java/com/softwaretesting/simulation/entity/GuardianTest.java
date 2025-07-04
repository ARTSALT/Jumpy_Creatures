package com.softwaretesting.simulation.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes da Classe Guardian")
class GuardianTest {

    @Nested
    @DisplayName("Construtor")
    class ConstructorTests {

        @Test
        @DisplayName("Deve criar um Guardião com 0 moedas e a posição inicial correta")
        void shouldCreateGuardianWithZeroCoinsAndCorrectPosition() {
            // Arrange: Define uma posição inicial para o guardião.
            double initialPosition = 150.0;

            // Act: Cria uma nova instância do Guardião.
            Guardian guardian = new Guardian(initialPosition);

            // Assert: Verifica se o estado inicial está correto.
            // O construtor do Guardião sempre define 0 moedas por padrão.
            assertThat(guardian.getCoins()).isZero();
            assertThat(guardian.getPosition()).isEqualTo(initialPosition);
            assertThat(guardian.getTargetPosition()).isEqualTo(initialPosition); // Posição alvo deve ser igual à inicial.
        }
    }

    @Nested
    @DisplayName("Método absorbCluster()")
    class AbsorbClusterTests {

        @Test
        @DisplayName("Deve absorver todas as moedas de um cluster e adicioná-las ao seu total")
        void shouldAbsorbAllCoinsFromCluster() {
            // Arrange: Cria um guardião (começa com 0 moedas) e um cluster com moedas.
            Guardian guardian = new Guardian(50.0);
            Cluster cluster = new Cluster(new Creature(100, 100.0), new Creature(150, 200.0));

            // Act: O guardião absorve o cluster.
            guardian.absorbCluster(cluster);

            // Assert: As moedas do guardião devem ser iguais às moedas que o cluster possuía.
            assertThat(guardian.getCoins()).isEqualTo(250);
        }

        @Test
        @DisplayName("Deve manter o total de moedas inalterado ao absorver um cluster com 0 moedas (Fronteira)")
        void shouldNotChangeCoinsWhenAbsorbingEmptyCluster() {
            // Arrange: Cria um guardião e um cluster sem moedas.
            Guardian guardian = new Guardian(50.0);
            Cluster clusterWithZeroCoins = new Cluster(new Creature(0, 100.0), new Creature(0, 200.0));

            // Act: O guardião absorve o cluster vazio.
            guardian.absorbCluster(clusterWithZeroCoins);

            // Assert: O total de moedas do guardião deve permanecer 0.
            assertThat(guardian.getCoins()).isZero();
        }

        @Test
        @DisplayName("Deve acumular moedas corretamente após múltiplas absorções")
        void shouldCorrectlyAccumulateCoinsFromMultipleClusters() {
            // Arrange: Cria um guardião e dois clusters com moedas.
            Guardian guardian = new Guardian(50.0);
            Cluster cluster1 = new Cluster(new Creature(100, 100.0), new Creature(150, 200.0));
            Cluster cluster2 = new Cluster(new Creature(100, 100.0), new Creature(150, 200.0));

            // Act: O guardião absorve ambos os clusters em sequência.
            guardian.absorbCluster(cluster1);
            guardian.absorbCluster(cluster2);

            // Assert: O total de moedas do guardião deve ser a soma das moedas de ambos os clusters.
            assertThat(guardian.getCoins()).isEqualTo(250 + 250); // 250 de cada cluster
        }
    }
}