package com.softwaretesting.core.domain.model;

import net.jqwik.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Suíte de testes para a classe Guardian.
 * <p>
 * Esta classe valida o comportamento da entidade Guardian, uma especialização de Creature
 * com a habilidade única de absorver clusters. Os testes cobrem os construtores, a lógica de
 * absorção e o contrato do objeto para garantir a robustez da classe.
 * A suíte combina testes baseados em exemplos para cobertura de MC/DC e testes baseados em propriedades
 * com jqwik para garantir a máxima robustez.
 */
@DisplayName("Testes da Classe Guardian")
class GuardianTest {

    /**
     * Testes baseados em exemplos, projetados para cobrir todas as branches lógicas (MC/DC)
     * e validar cenários específicos e casos de fronteira de forma explícita.
     */
    @Nested
    @DisplayName("Testes Baseados em Exemplos (MC/DC e Fronteira)")
    class ExampleBasedTests {
        private Creature testCreature1;
        private Creature testCreature2;

        @BeforeEach
        void setUp() {
            testCreature1 = new Creature(100, 10.0);
            testCreature2 = new Creature(200, 20.0);
        }

        @Nested
        @DisplayName("Construtor e Estado Inicial")
        class ConstructorAndInitialStateTests {

            /**
             * Testa o construtor principal, que deve sempre inicializar um Guardião com zero moedas.
             * <p>
             * <b>Teste de Domínio:</b> Valida a regra de negócio fundamental de que um Guardião,
             * ao ser criado da maneira padrão, sempre começa com 0 moedas.
             */
            @Test
            @DisplayName("Deve criar um Guardião com 0 moedas e a posição inicial correta")
            void shouldCreateGuardianWithZeroCoinsAndCorrectPosition() {
                double initialPosition = 150.0;
                Guardian guardian = new Guardian(initialPosition);

                assertThat(guardian.getCoins()).isZero();
                assertThat(guardian.getPosition()).isEqualTo(initialPosition);
                assertThat(guardian.getTargetPosition()).isEqualTo(initialPosition);
            }

            /**
             * Testa o construtor secundário, que permite definir uma quantidade inicial de moedas.
             * <p>
             * Este teste garante que o construtor herdado funcione como esperado, permitindo flexibilidade.
             * @param initialCoins A quantidade inicial de moedas a ser testada.
             */
            @ParameterizedTest(name = "Deve criar Guardião com {0} moedas via construtor secundário")
            @ValueSource(ints = {0, 100, 500})
            @DisplayName("Deve criar um Guardião com a quantidade de moedas especificada")
            void shouldCreateGuardianWithSpecifiedCoins(int initialCoins) {
                double initialPosition = 200.0;
                Guardian guardian = new Guardian(initialCoins, initialPosition);

                assertThat(guardian.getCoins()).isEqualTo(initialCoins);
                assertThat(guardian.getPosition()).isEqualTo(initialPosition);
            }

            @Test
            @DisplayName("toString deve identificar a classe como 'Guardian'")
            void toStringShouldIdentifyClassAsGuardian() {
                Guardian guardian = new Guardian(0, 150.789);
                String expectedString = String.format("Guardian[id=%d]{moedas=0, posicao=150,79}", guardian.getId());
                assertThat(guardian.toString()).isEqualTo(expectedString);
            }
        }

        @Nested
        @DisplayName("Lógica de Interação")
        class InteractionLogicTests {

            /**
             * Testa se o Guardião absorve corretamente todas as moedas de um cluster.
             * <p>
             * <b>Teste de Domínio e MC/DC:</b> Valida a principal habilidade do Guardião. Cobre o caminho
             * TRUE da decisão 'amount > 0' no metodo 'addCoins' herdado.
             */
            @Test
            @DisplayName("absorbCluster deve adicionar todas as moedas do cluster ao Guardião")
            void absorbClusterShouldAddAllClusterCoinsToGuardian() {
                Guardian guardian = new Guardian(50, 100.0);
                Cluster cluster = new Cluster(testCreature1, testCreature2); // Cluster tem 300 moedas
                long initialGuardianCoins = guardian.getCoins();
                long clusterCoins = cluster.getCoins();

                guardian.absorbCluster(cluster);

                assertThat(guardian.getCoins()).isEqualTo(initialGuardianCoins + clusterCoins);
            }

            /**
             * Testa o caso de borda onde um Guardião absorve um cluster que não possui moedas.
             * <p>
             * <b>Teste de Fronteira e MC/DC:</b> Garante que a lógica funcione corretamente quando a
             * quantidade a ser adicionada é zero, cobrindo o caminho FALSE da decisão 'amount > 0'
             * no metodo 'addCoins' herdado.
             */
            @Test
            @DisplayName("absorbCluster deve funcionar corretamente com um cluster de zero moedas")
            void absorbClusterShouldHandleClusterWithZeroCoins() {
                Guardian guardian = new Guardian(50, 100.0);
                Creature c1 = new Creature(0, 10.0);
                Creature c2 = new Creature(0, 20.0);
                Cluster zeroCoinCluster = new Cluster(c1, c2);
                long initialGuardianCoins = guardian.getCoins();

                guardian.absorbCluster(zeroCoinCluster);

                assertThat(guardian.getCoins()).isEqualTo(initialGuardianCoins);
            }
        }
    }

    /**
     * Testes baseados em propriedades que usam o framework jqwik para validar
     * regras universais (propriedades) sobre a classe Guardian para uma vasta
     * gama de entradas aleatórias, aumentando a robustez contra casos de borda inesperados.
     */
    @Nested
    @DisplayName("Testes Baseados em Propriedade (jqwik)")
    class PropertyBasedTests {

        /**
         * <b>Propriedade:</b> Para qualquer Guardião e qualquer Cluster, após a absorção,
         * o total de moedas do Guardião deve ser exatamente a soma de suas moedas iniciais
         * mais as moedas do cluster. Esta é uma propriedade de invariância da soma.
         */
        @Property
        @DisplayName("Absorver um cluster deve sempre resultar na soma correta de moedas")
        void absorbClusterShouldAlwaysResultInCorrectCoinSum(
            @ForAll("guardianProvider") Guardian guardian,
            @ForAll("clusterProvider") Cluster cluster
        ) {
            long initialGuardianCoins = guardian.getCoins();
            long clusterCoins = cluster.getCoins();

            guardian.absorbCluster(cluster);

            assertThat(guardian.getCoins()).isEqualTo(initialGuardianCoins + clusterCoins);
        }

        /**
         * Fornece criaturas com valores aleatórios para serem usadas na criação de clusters.
         * @return Um gerador de criaturas arbitrárias (Arbitrary).
         */
        @Provide
        Arbitrary<Creature> creatureProvider() {
            Arbitrary<Integer> coins = Arbitraries.integers().between(0, 1_000_000);
            Arbitrary<Double> position = Arbitraries.doubles().between(0, 1000.0);
            return Combinators.combine(coins, position).as(Creature::new);
        }

        /**
         * Fornece clusters com valores aleatórios para serem usados nos testes de propriedade.
         * @return Um gerador de clusters arbitrários (Arbitrary).
         */
        @Provide
        Arbitrary<Cluster> clusterProvider() {
            return Combinators.combine(creatureProvider(), creatureProvider()).as(Cluster::new);
        }

        /**
         * Fornece guardiões com valores aleatórios para serem usados nos testes de propriedade.
         * @return Um gerador de guardiões arbitrários (Arbitrary).
         */
        @Provide
        Arbitrary<Guardian> guardianProvider() {
            Arbitrary<Integer> coins = Arbitraries.integers().between(0, 1_000_000);
            Arbitrary<Double> position = Arbitraries.doubles().between(0, 1000.0);
            return Combinators.combine(coins, position).as(Guardian::new);
        }
    }
}
