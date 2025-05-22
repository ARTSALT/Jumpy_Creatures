package com.badlogic.drop.entity;

import com.badlogic.gdx.math.Vector2;

public class ParabolicMovement {
    private Vector2 startPoint; // Ponto inicial
    private Vector2 endPoint;   // Ponto final
    private Vector2 position;   // Posição atual do sprite
    private Vector2 velocity;   // Velocidade inicial
    private final float gravity = 500f;      // Gravidade
    private float dt;           // Tempo acumulado
    private float jumpHeight;   // Altura do salto

    public ParabolicMovement(Vector2 startPoint, Vector2 endPoint) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.position = new Vector2(startPoint);
        this.jumpHeight = 300f;
        this.dt = 0;

        // Calcula a velocidade inicial
        float distanceX = endPoint.x - startPoint.x;

        this.jumpHeight = Math.max(200f, Math.min(400f, distanceX * 2f));

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
        position.y = startPoint.y + (velocity.y * dt) - (0.5f * gravity * dt * dt);

        // Verifica se o sprite atingiu o ponto final
        if (endPoint.x > startPoint.x) {
            // Se o sprite estiver se movendo para a direita
            if (position.x >= endPoint.x) {
                position.set(endPoint);
                System.out.println("Reached the end point: " + position.toString());
            }
        } else {
            // Se o sprite estiver se movendo para a esquerda
            if (position.x <= endPoint.x) {
                position.set(endPoint);
                System.out.println("Reached the end point: " + position.toString());
            }
        }
    }

    public void setMovement(Vector2 startPoint, Vector2 endPoint) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.position = new Vector2(startPoint);

        // Calcula a distância percorrida
        float distanceX = endPoint.x - startPoint.x;
        this.jumpHeight = Math.max(150f, Math.min(400f, distanceX * 0.3f));

        // Demora até chegar ao ápice do salto
        float timeToApex = (float) Math.sqrt((2 * jumpHeight) / gravity);
        float totalTime = timeToApex * 2;
        float velocityX = distanceX / totalTime;
        float velocityY = gravity * timeToApex;

        this.velocity = new Vector2(velocityX, velocityY);
    }

    public Vector2 getPosition() {
        return position;
    }

    public float getJumpHeight() {
        return jumpHeight;
    }

    public Vector2 getEndPoint() {
        return endPoint;
    }

    public Vector2 getStartPoint() {
        return startPoint;
    }

    public float getDistanceX() {
        return endPoint.x - startPoint.x;
    }
}
