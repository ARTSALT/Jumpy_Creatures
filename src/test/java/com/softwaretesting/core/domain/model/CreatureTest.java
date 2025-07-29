package com.softwaretesting.core.domain.model;

import net.jqwik.api.*;
import net.jqwik.api.constraints.DoubleRange;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Suíte de testes para a classe Creature.
 * <p>
 * Esta classe valida o comportamento, as regras de negócio e os casos de fronteira
 * da entidade Creature. Os testes são estruturados para cobrir a lógica do construtor,
 * interações (roubo), gerenciamento de estado (moedas e posição) e métodos utilitários,
 * garantindo 100% de cobertura MC/DC para todas as decisões lógicas.
 * Inclui testes baseados em exemplos e testes baseados em propriedades (com jqwik) para máxima robustez.
 */
@DisplayName("Testes da Classe Creature")
class CreatureTest {

    /**
     * Testes baseados em exemplos, projetados para cobrir todas as branches lógicas (MC/DC)
     * e validar cenários específicos e casos de fronteira de forma explícita.
     */
    @Nested
    @DisplayName("Testes Baseados em Exemplos (MC/DC e Fronteira)")
    class ExampleBasedTests {

        @Nested
        @DisplayName("Construtor")
        class ConstructorTests {
            @Test
            @DisplayName("Deve lançar exceção para quantidade de moedas negativa")
            void shouldThrowExceptionForNegativeCoins() {
                assertThatThrownBy(() -> new Creature(-1, 100.0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("A quantidade de moedas não pode ser negativa.");
            }

            @ParameterizedTest(name = "Deve criar criatura com {0} moedas")
            @ValueSource(ints = {0, 1, 1000})
            @DisplayName("Deve criar criatura com moedas não-negativas")
            void shouldConstructSuccessfullyForNonNegativeCoins(int validCoins) {
                Creature creature = new Creature(validCoins, 50.0);
                assertThat(creature.getCoins()).isEqualTo(validCoins);
            }
        }

        @Nested
        @DisplayName("Manipulação de Moedas")
        class CoinManagementTests {
            @ParameterizedTest(name = "Adicionando {0} moedas a 100, resulta em {1}")
            @CsvSource({ "50, 150", "0, 100", "-50, 100" })
            @DisplayName("addCoins deve adicionar apenas valores positivos")
            void addCoinsShouldOnlyAddPositiveValues(int amount, int expectedFinalCoins) {
                Creature creature = new Creature(100, 0);
                creature.addCoins(amount);
                assertThat(creature.getCoins()).isEqualTo(expectedFinalCoins);
            }

            @Test
            @DisplayName("stealFrom deve transferir metade das moedas corretamente")
            void shouldCorrectlyStealFromCreature() {
                Creature thief = new Creature(10, 0);
                Creature victim = new Creature(100, 50);
                int stolenCoins = thief.stealFrom(victim);
                assertThat(stolenCoins).isEqualTo(50);
                assertThat(thief.getCoins()).isEqualTo(60);
                assertThat(victim.getCoins()).isEqualTo(50);
            }

            @ParameterizedTest(name = "setCoins deve definir a quantidade de moedas para {0}")
            @ValueSource(ints = {1000, 0, -50})
            @DisplayName("setCoins deve atualizar a quantidade de moedas")
            void setCoinsShouldUpdateTheAmountOfCoins(int coinsToSet) {
                Creature creature = new Creature(100, 0);
                creature.setCoins(coinsToSet);
                assertThat(creature.getCoins()).isEqualTo(coinsToSet);
            }
        }

        @Nested
        @DisplayName("Rastreamento de Delta de Moedas")
        class CoinDeltaTrackingTests {
            @Test
            @DisplayName("halveCoins deve registrar um delta negativo")
            void halveCoinsShouldRegisterNegativeDelta() {
                Creature creature = new Creature(100, 0);
                creature.halveCoins();
                assertThat(creature.getLastCoinsDelta()).isEqualTo(-50);
            }

            @Test
            @DisplayName("addCoins deve registrar um delta positivo")
            void addCoinsShouldRegisterPositiveDelta() {
                Creature creature = new Creature(100, 0);
                creature.addCoins(75);
                assertThat(creature.getLastCoinsDelta()).isEqualTo(75);
            }

            @Test
            @DisplayName("resetTurnDelta deve zerar a última variação de moedas")
            void resetTurnDeltaShouldZeroOutLastCoinDelta() {
                Creature creature = new Creature(100, 0);
                creature.addCoins(50);
                assertThat(creature.getLastCoinsDelta()).isEqualTo(50);
                creature.resetTurnDelta();
                assertThat(creature.getLastCoinsDelta()).isZero();
            }
        }

        @Nested
        @DisplayName("Gerenciamento de Posição")
        class PositionManagementTests {
            @Test
            @DisplayName("updatePosition deve mover a posição atual para a posição alvo")
            void shouldUpdatePositionToTargetPosition() {
                Creature creature = new Creature(100, 50.0);
                creature.setTargetPosition(-25.5);
                creature.commitPosition();
                assertThat(creature.getPosition()).isEqualTo(-25.5);
            }

            @Test
            @DisplayName("setPosition deve lançar exceção para valores negativos")
            void setPositionShouldThrowExceptionForNegativeValue() {
                Creature creature = new Creature(100, 0);
                assertThatThrownBy(() -> creature.setPosition(-1.0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("A posição não pode ser negativa.");
            }

            @Test
            @DisplayName("setPosition deve funcionar para um valor positivo")
            void setPositionShouldWorkForPositiveValue() {
                Creature creature = new Creature(100, 0);
                creature.setPosition(123.45);
                assertThat(creature.getPosition()).isEqualTo(123.45);
            }
        }

        @Nested
        @DisplayName("Contrato de Objeto (equals, hashCode, toString)")
        class ObjectContractTests {
            @Test
            @DisplayName("equals deve ser reflexivo e retornar true para o mesmo objeto")
            void equalsShouldBeReflexiveAndConsistent() {
                Creature creature1 = new Creature(100, 10);
                assertThat(creature1).isEqualTo(creature1);
            }

            @Test
            @DisplayName("equals deve retornar falso para nulo ou classes diferentes")
            void equalsShouldReturnFalseForNullOrDifferentClasses() {
                Creature creature1 = new Creature(1, 100, 10);
                assertThat(creature1.equals(null)).isFalse();
                assertThat(creature1.equals(new Object())).isFalse();
            }

            @Test
            @DisplayName("equals deve retornar falso para criaturas com IDs diferentes")
            void equalsShouldReturnFalseForDifferentCreatureInstances() {
                Creature creature1 = new Creature(100, 10);
                Creature creature2 = new Creature(100, 10);
                assertThat(creature1).isNotEqualTo(creature2);
            }

            @Test
            @DisplayName("equals deve retornar true para instâncias diferentes com mesmo ID")
            void equalsShouldReturnTrueForDifferentInstancesWithSameId() {
                Creature creature1 = new Creature(99, 100, 10);
                Creature creature2 = new Creature(99, 200, 20);
                assertThat(creature1).isEqualTo(creature2);
            }

            @Test
            @DisplayName("hashCode deve retornar o mesmo valor para objetos com o mesmo ID")
            void hashCodeShouldBeEqualForObjectsWithSameId() {
                Creature creature1 = new Creature(99, 100, 10);
                Creature creature2 = new Creature(99, 200, 20);
                assertThat(creature1.hashCode()).isEqualTo(creature2.hashCode());
            }

            @Test
            @DisplayName("toString deve conter o nome da classe, ID, moedas e posição")
            void toStringShouldContainAllRelevantInfo() {
                Creature creature = new Creature(100, 50.55);
                String result = creature.toString();

                assertThat(result).startsWith("Zombie[");
                assertThat(result).contains("id=" + creature.getId());
                assertThat(result).contains("moedas=100");
                assertThat(result).contains("posicao=50,55");
            }

            @Test
            @DisplayName("toString deve usar 'Zombie' como fallback se o nome da classe for vazio")
            void toStringShouldUseFallbackForEmptyClassName() {
                Creature creatureWithEmptyName = new Creature(100, 50.0) {};
                String expected = String.format("Zombie[id=%d]{moedas=100, posicao=50,00}", creatureWithEmptyName.getId());
                assertThat(creatureWithEmptyName.toString()).isEqualTo(expected);
            }

            @Test
            @DisplayName("getTargetPosition deve retornar a posição inicial após a construção")
            void getTargetPositionShouldReturnInitialPositionAfterConstruction() {
                Creature creature = new Creature(100, 123.45);
                assertThat(creature.getTargetPosition()).isEqualTo(123.45);
            }
        }
    }

    /**
     * Testes baseados em propriedades que usam o framework jqwik para gerar
     * centenas de casos de teste aleatórios, validando regras universais (propriedades)
     * sobre a classe Creature, o que aumenta a robustez contra casos de borda inesperados.
     */
    @Nested
    @DisplayName("Testes Baseados em Propriedade (jqwik)")
    class PropertyBasedTests {

        /**
         * <b>Propriedade:</b> A quantidade total de moedas entre duas criaturas deve permanecer a mesma
         * antes e depois de um roubo. Esta é uma importante "propriedade de invariância".
         * Valida que moedas não são criadas ou destruídas durante a transação.
         */
        @Property
        @DisplayName("A quantidade total de moedas deve ser invariante após o roubo")
        void totalCoinsShouldBeInvariantAfterSteal(
            @ForAll("creatureProvider") Creature thief,
            @ForAll("creatureProvider") Creature victim
        ) {
            Assume.that(!thief.equals(victim));

            long totalCoinsBefore = (long) thief.getCoins() + victim.getCoins();
            thief.stealFrom(victim);
            long totalCoinsAfter = (long) thief.getCoins() + victim.getCoins();

            assertThat(totalCoinsAfter).isEqualTo(totalCoinsBefore);
        }

        /**
         * <b>Propriedade:</b> Para qualquer posição inicial e qualquer posição alvo, após
         * chamar `updatePosition`, a posição atual deve ser igual à posição alvo.
         */
        @Property
        @DisplayName("updatePosition deve sempre mover a criatura para a posição alvo")
        void updatePositionShouldAlwaysMoveToTargetPosition(
            @ForAll @DoubleRange(min = 0, max = 1000) double initialPosition,
            @ForAll @DoubleRange(min = -500, max = 1500) double targetPosition
        ) {
            Creature creature = new Creature(100, initialPosition);
            creature.setTargetPosition(targetPosition);
            creature.commitPosition();
            assertThat(creature.getPosition()).isEqualTo(targetPosition);
        }

        /**
         * <b>Propriedade:</b> Definir a posição com qualquer valor negativo deve sempre
         * lançar uma exceção. Isso fortalece o teste de fronteira ao testar muitos
         * valores negativos diferentes.
         */
        @Property
        @DisplayName("setPosition deve sempre falhar para qualquer valor negativo")
        void setPositionShouldAlwaysFailForAnyNegativeValue(
            @ForAll @DoubleRange(min = -1000, max = -0.00001) double negativePosition
        ) {
            Creature creature = new Creature(100, 50);
            assertThatThrownBy(() -> creature.setPosition(negativePosition))
                .isInstanceOf(IllegalArgumentException.class);
        }

        /**
         * Fornece criaturas com valores aleatórios para serem usadas nos testes de propriedade.
         * Ensina ao jqwik como instanciar objetos do tipo Creature.
         * @return Um gerador de criaturas arbitrárias (Arbitrary).
         */
        @Provide
        Arbitrary<Creature> creatureProvider() {
            Arbitrary<Integer> coins = Arbitraries.integers().between(0, 1_000_000);
            Arbitrary<Double> position = Arbitraries.doubles().between(0, 1000.0);
            return Combinators.combine(coins, position).as(Creature::new);
        }
    }
}
