package com.softwaretesting.simulation.entity;

import com.softwaretesting.core.domain.model.Creature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Testes da Classe Creature")
class CreatureTest {

    @Nested
    @DisplayName("Construtor")
    class ConstructorTests {

        @Test
        @DisplayName("Deve lançar exceção para quantidade de moedas negativa (Fronteira/MC-DC)")
        void shouldThrowExceptionForNegativeCoins() {
            int invalidCoins = -1;

            // Verifica se a exceção IllegalArgumentException é lançada
            // Este teste satisfaz a condição TRUE para o MC/DC da decisão "coins < 0"
            assertThatThrownBy(
                () -> new Creature(invalidCoins, 100.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A quantidade de moedas não pode ser negativa.");
        }

        @ParameterizedTest(name = "Deve criar criatura com 0 moedas (Fronteira/MC-DC)")
        @ValueSource(ints = {0, 1, 1000}) // 0 (fronteira), 1 (logo após a fronteira), 1000 (valor típico)
        @DisplayName("Deve criar criatura com moedas não-negativas")
        void shouldConstructSuccessfullyForNonNegativeCoins(int validCoins) {
            // O caso com 0 satisfaz a condição FALSE para o MC/DC da decisão "coins < 0"
            Creature creature = new Creature(validCoins, 50.0);

            assertThat(creature.getCoins()).isEqualTo(validCoins);
            assertThat(creature.getPosition()).isEqualTo(50.0);
            assertThat(creature.getTargetPosition()).isEqualTo(50.0); // Posição alvo deve ser igual à inicial
        }
    }

    @Nested
    @DisplayName("Método stealFrom()")
    class StealFromTests {

        @Test
        @DisplayName("Deve roubar metade das moedas de uma criatura com quantidade par")
        void shouldCorrectlyStealFromCreatureWithEvenCoins() {
            Creature thief = new Creature(10, 0);
            Creature victim = new Creature(100, 50); // Quantidade par

            int stolenCoins = thief.stealFrom(victim);

            assertThat(stolenCoins).isEqualTo(50);
            assertThat(thief.getCoins()).isEqualTo(10 + 50); // Ladrão ganha 50
            assertThat(victim.getCoins()).isEqualTo(50);   // Vítima perde 50
        }

        @Test
        @DisplayName("Deve lidar com divisão inteira ao roubar de criatura com quantidade ímpar")
        void shouldHandleIntegerDivisionWhenStealingFromCreatureWithOddCoins() {
            Creature thief = new Creature(10, 0);
            Creature victim = new Creature(101, 50); // Quantidade ímpar

            // 101 / 2 = 50 (divisão inteira). A vítima perde 50
            int stolenCoins = thief.stealFrom(victim);

            assertThat(stolenCoins).isEqualTo(50);
            assertThat(thief.getCoins()).isEqualTo(10 + 50); // Ladrão ganha 50
            assertThat(victim.getCoins()).isEqualTo(51);   // Vítima fica com 101 - 50 = 51
        }

        @ParameterizedTest(name = "Roubando de vítima com {0} moedas, deve roubar {1} moedas (Fronteira)")
        @CsvSource({
            "1, 0", // Fronteira: vítima com 1 moeda (1/2 = 0)
            "0, 0"  // Fronteira: vítima com 0 moedas (0/2 = 0)
        })
        void shouldCorrectlyHandleStealingFromCreatureWithOneOrZeroCoins(int victimInitialCoins, int expectedStolen) {
            Creature thief = new Creature(10, 0);
            Creature victim = new Creature(victimInitialCoins, 50);

            int stolenCoins = thief.stealFrom(victim);

            assertThat(stolenCoins).isEqualTo(expectedStolen);
            assertThat(thief.getCoins()).isEqualTo(10 + expectedStolen);
            assertThat(victim.getCoins()).isEqualTo(victimInitialCoins - expectedStolen);
        }

        @Test
        @DisplayName("Uma criatura roubando de si mesma não deve alterar o total de moedas")
        void aCreatureStealingFromItselfShouldNotChangeItsTotalCoins() {
            Creature creature = new Creature(100, 0);

            // halveCoins() -> coins = 50, retorna 50
            // this.coins += 50 -> coins = 50 + 50 = 100
            int stolenCoins = creature.stealFrom(creature);

            assertThat(stolenCoins).isEqualTo(50);
            assertThat(creature.getCoins()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("Gerenciamento de Posição")
    class PositionManagementTests {

        @Test
        @DisplayName("setTargetPosition deve atualizar a posição alvo sem mudar a posição atual")
        void shouldSetTargetPositionWithoutChangingCurrentPosition() {
            Creature creature = new Creature(100, 50.0);

            creature.setTargetPosition(200.0);

            assertThat(creature.getTargetPosition()).isEqualTo(200.0);
            assertThat(creature.getPosition()).isEqualTo(50.0); // Posição atual não deve mudar
        }

        @Test
        @DisplayName("updatePosition deve mover a posição atual para a posição alvo")
        void shouldUpdatePositionToTargetPosition() {
            Creature creature = new Creature(100, 50.0);
            creature.setTargetPosition(-25.5);

            creature.updatePosition();

            assertThat(creature.getPosition()).isEqualTo(-25.5);
            // A posição alvo e a atual agora devem ser iguais
            assertThat(creature.getPosition()).isEqualTo(creature.getTargetPosition());
        }
    }
}
