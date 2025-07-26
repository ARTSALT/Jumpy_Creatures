package com.softwaretesting.core.domain.model;

import com.badlogic.gdx.math.Vector2;

public class ParabolicMovement {
    private Vector2 startPoint;
    private Vector2 endPoint;
    private Vector2 position;
    Vector2 velocity;
    private final float gravity = 900f;
    private float dt;
    private float jumpHeight;

    public ParabolicMovement(Vector2 startPoint, Vector2 endPoint) {
        if (startPoint.equals(endPoint)) {
            this.startPoint = startPoint;
            this.endPoint = endPoint;
            this.position = new Vector2(startPoint);
            this.velocity = new Vector2(0, 0);
            this.jumpHeight = 0;
            this.dt = 0;
            return;
        }

        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.position = new Vector2(startPoint);
        this.dt = 0;

        float distanceX = endPoint.x - startPoint.x;
        this.jumpHeight = Math.max(150f, Math.min(350f, Math.abs(distanceX) * 0.5f));

        float timeToApex = (float) Math.sqrt((2 * jumpHeight) / gravity);
        float totalTime = timeToApex * 2;

        float velocityX = distanceX / totalTime;
        float velocityY = gravity * timeToApex;

        this.velocity = new Vector2(velocityX, velocityY);
    }

    public void update(float deltaTime) {
        dt += deltaTime;

        position.x = startPoint.x + velocity.x * dt;
        position.y = startPoint.y + (velocity.y * dt) - (0.5f * gravity * dt * dt);

        // Trava a posição no ponto final se o movimento horizontal for concluído
        boolean finishedX = (velocity.x > 0) ? position.x >= endPoint.x : position.x <= endPoint.x;
        if (finishedX) {
            position.set(endPoint);
        }
    }

    /**
     * Verifica se o movimento parabólico foi concluído.
     * @return true se a posição atual é igual ao ponto final, false caso contrário.
     */
    public boolean isFinished() {
        return position.equals(endPoint);
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
