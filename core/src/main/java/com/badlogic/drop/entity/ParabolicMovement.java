package com.badlogic.drop.entity;

import com.badlogic.gdx.math.Vector2;

public class ParabolicMovement {
    private Vector2 startPoint; // Ponto inicial
    private Vector2 endPoint;   // Ponto final
    private Vector2 position;   // Posição atual do sprite
    private Vector2 velocity;   // Velocidade inicial
    private float gravity;      // Gravidade
    private float dt;         // Tempo acumulado

    public ParabolicMovement(Vector2 startPoint, Vector2 endPoint, float jumpHeight, float gravity) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.gravity = gravity;
        this.position = new Vector2(startPoint);
        this.dt = 0;

        // Calcula a velocidade inicial
        float distanceX = endPoint.x - startPoint.x;
        float timeToApex = (float) Math.sqrt((2 * jumpHeight) / gravity);
        float totalTime = timeToApex * 2;
        float velocityX = distanceX / totalTime;
        float velocityY = gravity * timeToApex;

        this.velocity = new Vector2(velocityX, velocityY);
    }

    public void update(float deltaTime) {
        // Atualiza o tempo
        dt += deltaTime;

        // Atualiza a posição
        position.x = startPoint.x + velocity.x * dt;
        position.y = startPoint.y + velocity.y * dt - 0.5f * gravity * dt * dt;

        // Verifica se o sprite atingiu o ponto final
        if (position.x >= endPoint.x) {
            position.set(endPoint);
            System.out.println("Reached the end point: " + position.toString());
        }
    }

    public Vector2 getPosition() {
        return position;
    }
}
