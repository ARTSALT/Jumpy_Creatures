package com.softwaretesting.core.domain.model;

import net.jqwik.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Suíte de testes para a classe Cluster.
 * <p>
 * Esta classe valida o comportamento da entidade Cluster, que é uma especialização de Creature.
 * Os testes cobrem a criação de clusters, a adição de novos membros e as regras de negócio associadas,
 * como a soma de moedas e a prevenção de membros duplicados.
 * A suíte combina testes baseados em exemplos para cobertura de MC/DC e testes baseados em propriedades
 * com jqwik para garantir a máxima robustez.
 */
@DisplayName("Testes da Classe Cluster")
class ClusterTest {

    /**
     * Testes baseados em exemplos, projetados para cobrir todas as branches lógicas (MC/DC)
     * e validar cenários específicos e casos de fronteira de forma explícita.
     */
    @Nested
    @DisplayName("Testes Baseados em Exemplos (MC/DC e Fronteira)")
    class ExampleBasedTests {
        private Creature creature1;
        private Creature creature2;

        @BeforeEach
        void setUp() {
            creature1 = new Creature(100, 50.0);
            creature2 = new Creature(200, 75.0);
        }

        @Nested
        @DisplayName("Construtor e Estado Inicial")
        class ConstructorAndInitialStateTests {
            /**
             * Testa se o construtor cria um cluster com o estado inicial correto.
             * <p>
             * <b>Teste de Domínio:</b> Valida a regra de negócio fundamental da criação de um cluster.
             */
            @Test
            @DisplayName("Deve criar um cluster com a soma das moedas e a posição da primeira criatura")
            void shouldCreateClusterWithSumOfCoinsAndCorrectPosition() {
                Cluster cluster = new Cluster(creature1, creature2);
                assertThat(cluster.getCoins()).isEqualTo(creature1.getCoins() + creature2.getCoins());
                assertThat(cluster.getPosition()).isEqualTo(creature1.getPosition());
                assertThat(cluster.getMembers()).hasSize(2).containsExactly(creature1, creature2);
            }

            @Test
            @DisplayName("toString deve identificar a classe como 'Cluster'")
            void toStringShouldIdentifyClassAsCluster() {
                Cluster cluster = new Cluster(creature1, creature2);
                String expectedString = String.format("Cluster[id=%d]{moedas=300, posicao=50,00}", cluster.getId());
                assertThat(cluster.toString()).isEqualTo(expectedString);
            }
        }

        @Nested
        @DisplayName("Método addMember")
        class AddMemberTests {
            /**
             * Testa se a adição de um membro nulo lança a exceção apropriada.
             * <p>
             * <b>Teste de Fronteira e MC/DC:</b> Cobre o caminho TRUE da decisão "creature == null".
             */
            @Test
            @DisplayName("Deve lançar IllegalArgumentException ao tentar adicionar um membro nulo")
            void shouldThrowExceptionWhenAddingNullMember() {
                Cluster cluster = new Cluster(creature1, creature2);
                assertThatThrownBy(() -> cluster.addMember(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("A criatura não pode ser nula.");
            }

            /**
             * Testa a adição bem-sucedida de uma nova criatura a um cluster.
             * <p>
             * <b>Teste de Domínio e MC/DC:</b> Cobre o caminho onde a criatura não é nula e
             * ainda não é membro do cluster.
             */
            @Test
            @DisplayName("Deve adicionar um novo membro e somar suas moedas")
            void shouldAddValidNewMemberAndSumItsCoins() {
                Cluster cluster = new Cluster(creature1, creature2);
                Creature newMember = new Creature(500, 10.0);
                long initialClusterCoins = cluster.getCoins();
                cluster.addMember(newMember);
                assertThat(cluster.getMembers()).hasSize(3).contains(creature1, creature2, newMember);
                assertThat(cluster.getCoins()).isEqualTo(initialClusterCoins + newMember.getCoins());
            }

            /**
             * Testa a tentativa de adicionar uma criatura que já faz parte do cluster.
             * <p>
             * <b>Teste de Domínio e MC/DC:</b> Cobre o caminho onde a criatura não é nula, mas já está
             * contida na lista de membros.
             */
            @Test
            @DisplayName("Não deve adicionar um membro duplicado nem alterar as moedas")
            void shouldNotAddDuplicateMemberOrChangeCoins() {
                Cluster cluster = new Cluster(creature1, creature2);
                long initialClusterCoins = cluster.getCoins();
                int initialMemberCount = cluster.getMembers().size();
                cluster.addMember(creature1);
                assertThat(cluster.getMembers()).hasSize(initialMemberCount);
                assertThat(cluster.getCoins()).isEqualTo(initialClusterCoins);
            }
        }
    }

    /**
     * Testes baseados em propriedades que usam o framework jqwik para validar
     * regras universais (propriedades) sobre a classe Cluster para uma vasta
     * gama de entradas aleatórias, aumentando a robustez contra casos de borda inesperados.
     */
    @Nested
    @DisplayName("Testes Baseados em Propriedade (jqwik)")
    class PropertyBasedTests {

        /**
         * <b>Propriedade:</b> A criação de um cluster, para quaisquer duas criaturas,
         * deve sempre resultar em um total de moedas igual à soma das moedas das criaturas
         * fundadoras e a posição deve ser a da primeira criatura.
         */
        @Property
        @DisplayName("O construtor deve sempre somar as moedas e definir a posição corretamente")
        void constructorShouldAlwaysSumCoinsAndSetPositionCorrectly(
            @ForAll("creatureProvider") Creature c1,
            @ForAll("creatureProvider") Creature c2
        ) {
            Cluster cluster = new Cluster(c1, c2);
            assertThat(cluster.getCoins()).isEqualTo(c1.getCoins() + c2.getCoins());
            assertThat(cluster.getPosition()).isEqualTo(c1.getPosition());
            assertThat(cluster.getMembers()).containsExactly(c1, c2);
        }

        /**
         * <b>Propriedade:</b> Adicionar um novo membro válido a um cluster deve sempre
         * resultar em um total de moedas igual à soma das moedas originais do cluster
         * mais as moedas do novo membro. Esta é uma propriedade de invariância da soma.
         */
        @Property
        @DisplayName("Adicionar um membro deve sempre resultar na soma correta de moedas")
        void addingMemberShouldAlwaysResultInCorrectCoinSum(
            @ForAll("clusterProvider") Cluster cluster,
            @ForAll("creatureProvider") Creature newMember
        ) {
            // Garante que o novo membro não seja um dos membros fundadores do cluster
            // para evitar o caso de "membro duplicado" e testar apenas a lógica de adição.
            Assume.that(!cluster.getMembers().contains(newMember));

            long coinsBefore = cluster.getCoins();
            int membersBefore = cluster.getMembers().size();

            cluster.addMember(newMember);

            assertThat(cluster.getCoins()).isEqualTo(coinsBefore + newMember.getCoins());
            assertThat(cluster.getMembers()).hasSize(membersBefore + 1).contains(newMember);
        }

        /**
         * Fornece criaturas com valores aleatórios para serem usadas nos testes de propriedade.
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
    }
}
