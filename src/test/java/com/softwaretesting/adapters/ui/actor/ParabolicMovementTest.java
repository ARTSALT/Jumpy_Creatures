package com.softwaretesting.adapters.ui.actor;

import com.badlogic.gdx.math.Vector2;
import net.jqwik.api.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

/**
 * Suíte de testes para a classe ParabolicMovement.
 * <p>
 * Esta classe valida a lógica do movimento parabólico, cobrindo os cálculos iniciais,
 * a progressão do movimento ao longo do tempo e os casos de fronteira.
 * A suíte combina testes baseados em exemplos para cobertura MC/DC e testes baseados
 * em propriedades com jqwik para garantir a máxima robustez.
 */
@DisplayName("Testes da Classe ParabolicMovement")
class ParabolicMovementTest {

    private static final float FLOAT_PRECISION = 0.01f;

    /**
     * Testes baseados em exemplos, projetados para cobrir todas as branches lógicas (MC/DC)
     * e validar cenários específicos e casos de fronteira de forma explícita.
     */
    @Nested
    @DisplayName("Testes Baseados em Exemplos (MC/DC e Fronteira)")
    class ExampleBasedTests {

        @Nested
        @DisplayName("Construtor e Estado Inicial")
        class ConstructorAndInitialStateTests {

            @Test
            @DisplayName("Deve calcular corretamente a velocidade para um salto para a direita")
            void shouldCalculateCorrectVelocityForRightwardJump() {
                Vector2 start = new Vector2(0, 0);
                Vector2 end = new Vector2(400, 0);
                ParabolicMovement movement = new ParabolicMovement(start, end);

                assertThat(movement.getStartPoint()).isEqualTo(start);
                assertThat(movement.getEndPoint()).isEqualTo(end);
                assertThat(movement.getPosition()).isEqualTo(start);
                // A velocidade X deve ser positiva
                assertThat(movement.velocity.x).isGreaterThan(0);
                // A velocidade Y inicial (para cima) deve ser positiva
                assertThat(movement.velocity.y).isGreaterThan(0);
            }

            @Test
            @DisplayName("Deve calcular corretamente a velocidade para um salto para a esquerda")
            void shouldCalculateCorrectVelocityForLeftwardJump() {
                Vector2 start = new Vector2(400, 0);
                Vector2 end = new Vector2(0, 0);
                ParabolicMovement movement = new ParabolicMovement(start, end);

                // A velocidade X deve ser negativa
                assertThat(movement.velocity.x).isLessThan(0);
                assertThat(movement.velocity.y).isGreaterThan(0);
            }

            @Test
            @DisplayName("[Fronteira] Deve ter velocidade zero para um salto de distância zero")
            void shouldHaveZeroVelocityForZeroDistanceJump() {
                Vector2 start = new Vector2(100, 50);
                ParabolicMovement movement = new ParabolicMovement(start, start);

                // Cobre o caminho `totalTime > 0 -> false` no construtor
                assertThat(movement.velocity.x).isEqualTo(0);
                assertThat(movement.velocity.y).isEqualTo(0);
                assertThat(movement.isFinished()).isTrue();
                assertThat(movement.getPosition()).isEqualTo(start);
                assertThat(movement.getDistanceX()).isEqualTo(0);
            }

            @Test
            @DisplayName("[Fronteira] A altura do salto deve ser fixada em 150f para distâncias muito curtas")
            void shouldClampJumpHeightToMinForShortDistances() {
                // Distância de 100 * 0.5f = 50f, que é menor que o mínimo de 150f
                Vector2 start = new Vector2(0, 0);
                Vector2 end = new Vector2(100, 0);
                ParabolicMovement movement = new ParabolicMovement(start, end);

                assertThat(movement.getJumpHeight()).isEqualTo(150f);
            }

            @Test
            @DisplayName("[Fronteira] A altura do salto deve ser fixada em 350f para distâncias muito longas")
            void shouldClampJumpHeightToMaxForLongDistances() {
                // Distância de 800 * 0.5f = 400f, que é maior que o máximo de 350f
                Vector2 start = new Vector2(0, 0);
                Vector2 end = new Vector2(800, 0);
                ParabolicMovement movement = new ParabolicMovement(start, end);

                assertThat(movement.getJumpHeight()).isEqualTo(350f);
            }
        }

        @Nested
        @DisplayName("Lógica de Atualização e Movimento")
        class UpdateAndMovementLogicTests {

            /**
             * Cobre a condição: (velocity.x > 0) ? position.x >= endPoint.x : position.x <= endPoint.x
             * com o caso: (velocity.x > 0) -> TRUE e (position.x >= endPoint.x) -> TRUE
             */
            @Test
            @DisplayName("[MC/DC] Deve travar a posição no ponto final ao ultrapassar em um salto para a direita")
            void shouldClampPositionToEndPointWhenOvershootingRightward() {
                Vector2 start = new Vector2(0, 0);
                Vector2 end = new Vector2(100, 0);
                ParabolicMovement movement = new ParabolicMovement(start, end);

                // Simula um deltaTime gigante que com certeza ultrapassará o alvo
                movement.update(100f);

                assertThat(movement.getPosition()).isEqualTo(end);
                assertThat(movement.isFinished()).isTrue();
            }

            /**
             * Cobre a condição: (velocity.x > 0) ? position.x >= endPoint.x : position.x <= endPoint.x
             * com o caso: (velocity.x > 0) -> FALSE e (position.x <= endPoint.x) -> TRUE
             */
            @Test
            @DisplayName("[MC/DC] Deve travar a posição no ponto final ao ultrapassar em um salto para a esquerda")
            void shouldClampPositionToEndPointWhenOvershootingLeftward() {
                Vector2 start = new Vector2(100, 0);
                Vector2 end = new Vector2(0, 0);
                ParabolicMovement movement = new ParabolicMovement(start, end);

                // Simula um deltaTime gigante
                movement.update(100f);

                assertThat(movement.getPosition()).isEqualTo(end);
                assertThat(movement.isFinished()).isTrue();
            }

            /**
             * Cobre a condição: if (finishedX) -> FALSE
             * <p>
             * Este teste valida o caminho de execução mais comum do metodo 'update': um único
             * passo de tempo que move a criatura, mas não o suficiente para que ela atinja
             * ou ultrapasse seu destino horizontal. Isso garante que a lógica de travamento
             * no ponto final não seja acionada prematuramente.
             */
            @Test
            @DisplayName("[MC/DC] update não deve travar a posição se o ponto final não for alcançado")
            void updateShouldNotClampPositionIfEndPointIsNotReached() {
                // Setup: Um salto para a direita
                Vector2 start = new Vector2(0, 0);
                Vector2 end = new Vector2(200, 0);
                ParabolicMovement movement = new ParabolicMovement(start, end);

                // Executa um pequeno passo de tempo
                movement.update(0.016f);

                // A posição mudou e não é mais o ponto de partida
                assertThat(movement.getPosition()).isNotEqualTo(start);

                // A posição ainda não é o ponto final
                assertThat(movement.getPosition()).isNotEqualTo(end);

                // O movimento, portanto, não está finalizado
                assertThat(movement.isFinished()).isFalse();
            }

            /**
             * Cobre a condição: if (finishedX) -> FALSE para um salto para a esquerda.
             * <p>
             * Este teste valida o caminho onde a velocidade X é negativa (salto para a esquerda)
             * e a posição atual ainda não atingiu o ponto final (position.x > endPoint.x).
             * Isso força a segunda parte do operador ternário a ser avaliada como 'false',
             * cobrindo a última branch que faltava para a cobertura MC/DC completa.
             */
            @Test
            @DisplayName("[MC/DC] update não deve travar a posição em um salto para a esquerda se o ponto final não for alcançado")
            void updateShouldNotClampPositionOnLeftwardJumpIfNotFinished() {
                // Um salto para a esquerda
                Vector2 start = new Vector2(200, 0);
                Vector2 end = new Vector2(0, 0);
                ParabolicMovement movement = new ParabolicMovement(start, end);

                // Action: Executa um pequeno passo de tempo
                movement.update(0.016f);

                // A posição mudou e não é mais o ponto de partida
                assertThat(movement.getPosition()).isNotEqualTo(start);

                // A posição ainda não é o ponto final
                assertThat(movement.getPosition()).isNotEqualTo(end);

                // O movimento, portanto, não está finalizado
                assertThat(movement.isFinished()).isFalse();
            }

            @Test
            @DisplayName("isFinished deve retornar false no início de um movimento válido")
            void isFinishedShouldBeFalseAtTheStartOfValidMovement() {
                ParabolicMovement movement = new ParabolicMovement(new Vector2(0, 0), new Vector2(100, 0));
                assertThat(movement.isFinished()).isFalse();
            }
        }

        @Nested
        @DisplayName("Método setMovement")
        class SetMovementTests {
            @Test
            @DisplayName("setMovement deve reiniciar o estado para um novo salto")
            void setMovementShouldResetStateForNewJump() {
                Vector2 start1 = new Vector2(0, 0);
                Vector2 end1 = new Vector2(100, 0);
                ParabolicMovement movement = new ParabolicMovement(start1, end1);
                movement.update(100f); // Finaliza o primeiro movimento
                assertThat(movement.isFinished()).isTrue();

                Vector2 start2 = new Vector2(50, 50);
                Vector2 end2 = new Vector2(-200, 50);

                // Ação: Define um novo movimento
                movement.setMovement(start2, end2);

                // Assert: Verifica se o estado foi reiniciado
                assertThat(movement.isFinished()).isFalse();
                assertThat(movement.getPosition()).isEqualTo(start2);
                assertThat(movement.getStartPoint()).isEqualTo(start2);
                assertThat(movement.getEndPoint()).isEqualTo(end2);
                assertThat(movement.velocity.x).isLessThan(0); // Nova velocidade calculada
            }
        }
    }

    /**
     * Testes baseados em propriedades que usam o framework jqwik para validar
     * regras universais (propriedades) sobre a classe ParabolicMovement para uma vasta
     * gama de entradas aleatórias, aumentando a robustez contra casos de borda inesperados.
     */
    @Nested
    @DisplayName("Testes Baseados em Propriedade (jqwik)")
    class PropertyBasedTests {

        /**
         * <b>Propriedade:</b> Para qualquer ponto de início e fim, após tempo suficiente,
         * a posição final do movimento deve ser sempre igual ao ponto final.
         * Esta é a propriedade mais crítica da classe, garantindo que o movimento sempre chega ao seu destino.
         */
        @Property
        @DisplayName("O movimento deve sempre terminar no ponto final após tempo suficiente")
        void movementShouldAlwaysFinishAtTheEndPoint(
            @ForAll("vectorProvider") Vector2 startPoint,
            @ForAll("vectorProvider") Vector2 endPoint
        ) {
            ParabolicMovement movement = new ParabolicMovement(startPoint, endPoint);

            // Simula um tempo grande o suficiente para garantir a conclusão do movimento
            movement.update(10f);

            assertThat(movement.isFinished()).isTrue();
            assertThat(movement.getPosition().x).isCloseTo(endPoint.x, offset(FLOAT_PRECISION));
            assertThat(movement.getPosition().y).isCloseTo(endPoint.y, offset(FLOAT_PRECISION));
        }

        /**
         * <b>Propriedade:</b> A altura máxima alcançada durante o salto deve ser aproximadamente
         * igual à altura inicial mais a altura do salto calculada (`jumpHeight`).
         * Isso valida a corretude da física vertical do movimento.
         */
        @Property
        @DisplayName("A altura máxima do salto deve ser consistente com o jumpHeight calculado")
        void maximumHeightShouldBeConsistentWithJumpHeight(
            @ForAll("vectorProvider") Vector2 startPoint,
            @ForAll("vectorProvider") Vector2 endPoint
        ) {
            // Evita o caso de distância zero onde a física não se aplica
            Assume.that(!startPoint.equals(endPoint));

            ParabolicMovement movement = new ParabolicMovement(startPoint, endPoint);
            float expectedApexY = startPoint.y + movement.getJumpHeight();

            // Simula o movimento passo a passo para encontrar a altura máxima alcançada
            float timeToApex = movement.velocity.y / 500f;
            movement.update(timeToApex);

            assertThat(movement.getPosition().y).isCloseTo(expectedApexY, offset(FLOAT_PRECISION));
        }

        /**
         * Fornece vetores 2D com valores aleatórios para serem usados nos testes de propriedade.
         * @return Um gerador de vetores arbitrários (Arbitrary).
         */
        @Provide
        Arbitrary<Vector2> vectorProvider() {
            Arbitrary<Float> x = Arbitraries.floats().between(-1000f, 1000f);
            Arbitrary<Float> y = Arbitraries.floats().between(0f, 500f); // Y geralmente é o chão ou acima
            return Combinators.combine(x, y).as(Vector2::new);
        }
    }
}
