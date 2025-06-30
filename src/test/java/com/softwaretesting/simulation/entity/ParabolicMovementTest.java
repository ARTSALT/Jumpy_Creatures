package com.softwaretesting.simulation.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Teste da classe ParabolicMovement")
public class ParabolicMovementTest {
    // Testes unitários para a classe ParabolicMovement
    // Implementar testes para verificar o comportamento do movimento parabólico

    @Test
    @DisplayName("Deve criar movimento parabólico com parâmetros válidos")
    void construtor_ShouldCreateParabolicMovementWithValidParameters() {
        Vector2 startPoint = new Vector2(100, 200);
        Vector2 endPoint = new Vector2(300, 200);

        float distanceX = endPoint.x - startPoint.x; // velocidade inicial
        float gravity = 500f; // Gravidade

        float jumpHeight = Math.max(200f, Math.min(400f, distanceX * 2f));
        float timeToApex = (float) Math.sqrt((2 * jumpHeight) / gravity);
        float totalTime = timeToApex * 2;
        Vector2 velocity = new Vector2(distanceX / totalTime, gravity * timeToApex);

        ParabolicMovement movement = new ParabolicMovement(startPoint, endPoint);

        assertThat(movement).isNotNull(); // Checa se o objeto foi criado com sucesso
        assertThat(movement.getStartPoint()).isEqualTo(startPoint); // Verifica se o ponto inicial está correto
        assertThat(movement.getEndPoint()).isEqualTo(endPoint); // Verifica se o ponto final está correto
        assertThat(movement.getVelocity()).isEqualTo(velocity);
        assertEquals(startPoint, movement.getPosition()); // Verifica se a posição inicial iguala o ponto inicial
    }

    @Test
    @DisplayName("Deve ajustar a altura do salto com base na distância")
    void constructor_ShouldAdjustJumpHeightBasedOnDistance() {
        float distanceX;
        float expectedJumpHeight;

        for (int i = 1; i < 4; i++) {
            Vector2 startPoint = new Vector2(100, 200);
            Vector2 endPoint = new Vector2(i * 150, 200); // Distância maior

            ParabolicMovement movement = new ParabolicMovement(startPoint, endPoint);

            distanceX = endPoint.x - startPoint.x; // velocidade inicial
            expectedJumpHeight = Math.max(200f, Math.min(400f, distanceX * 2f));

            assertEquals(movement.getJumpHeight(), expectedJumpHeight); // Verifica se a altura do salto foi ajustada corretamente
        }
    }

    @Test
    @DisplayName("Deve atualizar a posição do movimento parabólico corretamente")
    void update_ShouldUpdateParabolicMovementPositionCorrectly() {
        Vector2 startPoint = new Vector2(100, 200);
        Vector2 endPoint = new Vector2(300, 200);
        ParabolicMovement movement = new ParabolicMovement(startPoint, endPoint);

        // Atualiza a posição com um deltaTime de 0,1 segundos
        movement.update(0.1f);

        // Verifica se a posição foi atualizada corretamente
        Vector2 expectedPosition = new Vector2(startPoint.x + movement.getVelocity().x * Gdx.graphics.getDeltaTime(),
                startPoint.y + (movement.getVelocity().y * 0.1f) - (0.5f * movement.getGravity() * 0.1f * 0.1f));

        assertThat(movement.getPosition()).isEqualTo(expectedPosition);
    }

    @Test
    @DisplayName("Deve definir um novo movimento parabólico corretamente")
    void setMovement_ShouldSetNewParabolicMovementCorrectly() {
        Vector2 startPoint = new Vector2(100, 200);
        Vector2 endPoint = new Vector2(300, 200);
        ParabolicMovement movement = new ParabolicMovement(startPoint, endPoint);

        Vector2 newStartPoint = new Vector2(150, 250);
        Vector2 newEndPoint = new Vector2(350, 250);

        movement.setMovement(newStartPoint, newEndPoint);

        assertThat(movement.getStartPoint()).isEqualTo(newStartPoint); // Verifica se o novo ponto inicial foi definido corretamente
        assertThat(movement.getEndPoint()).isEqualTo(newEndPoint); // Verifica se o novo ponto final foi definido corretamente
        assertEquals(newStartPoint, movement.getPosition()); // Verifica se a posição foi atualizada para o novo ponto inicial
    }
}
