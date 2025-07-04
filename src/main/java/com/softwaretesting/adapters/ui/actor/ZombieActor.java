package com.softwaretesting.adapters.ui.actor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.softwaretesting.core.domain.model.Creature;
import com.softwaretesting.core.domain.model.ParabolicMovement;

import java.util.Arrays;

public class ZombieActor {

    private enum ZombieState {
        IDLE,
        JUMPING,
        ATTACKING
    }

    private Creature creature;
    private static Texture spriteSheet;
    private static BitmapFont font;

    private static Animation<TextureRegion> jumpAnimation;
    private static Animation<TextureRegion> attackingAnimation;
    private static Sound attackSound;

    private float stateTime = 0f;
    private ZombieState currentState = ZombieState.IDLE;
    private boolean flip = false;

    private final com.badlogic.gdx.graphics.g2d.Sprite sprite;
    private ParabolicMovement parm;
    private final Rectangle zombieRectangle;
    private final float floorY;

    private String statusText;
    private Color statusColor;
    private float statusTimer = 0f;
    private int lastSeenDelta = 0;

    public ZombieActor(Creature creature, float floorY) {
        if (spriteSheet == null) {
            throw new IllegalStateException("Resources not loaded. Call ZombieActor.loadResources() first.");
        }
        this.creature = creature;
        this.floorY = floorY;
        this.sprite = new com.badlogic.gdx.graphics.g2d.Sprite(attackingAnimation.getKeyFrames()[0]);
        this.sprite.setSize(350, 350);
        this.sprite.setPosition((float) creature.getPosition(), this.floorY);
        this.zombieRectangle = new Rectangle();
    }

    public void update(float deltaTime) {
        stateTime += deltaTime;

        switch (currentState) {
            case JUMPING:
                if (parm != null) {
                    parm.update(deltaTime);
                    sprite.setPosition(parm.getPosition().x, parm.getPosition().y);

                    if (parm.isFinished()) {
                        currentState = ZombieState.ATTACKING;
                        stateTime = 0;
                        attackSound.play();
                        // *** CORREÇÃO: Posiciona o sprite no ponto final do pulo, não na posição antiga do 'creature'. ***
                        sprite.setPosition(parm.getEndPoint().x, parm.getEndPoint().y);
                    }
                }
                break;
            case ATTACKING:
                if (attackingAnimation.isAnimationFinished(stateTime)) {
                    currentState = ZombieState.IDLE;
                    stateTime = 0;
                }
                break;
            case IDLE:
                // No estado IDLE, o zumbi fica parado, aguardando o próximo comando de pulo.
                break;
        }

        if (statusTimer > 0) {
            statusTimer -= deltaTime;
            if (statusTimer <= 0) {
                statusText = null;
            }
        }
    }

    public void draw(SpriteBatch spriteBatch) {
        TextureRegion currentFrame = getFrame();
        sprite.setRegion(currentFrame);
        sprite.setFlip(flip, false);
        sprite.draw(spriteBatch);

        int currentDelta = creature.getLastCoinsDelta();
        if (currentDelta != 0 && currentDelta != lastSeenDelta) {
            setStatusText(String.format("%+d", currentDelta), currentDelta > 0 ? Color.GREEN : Color.RED);
            this.lastSeenDelta = currentDelta;
        } else if (currentDelta == 0) {
            this.lastSeenDelta = 0;
        }

        if (statusText != null && statusTimer > 0) {
            font.setColor(statusColor);
            font.draw(spriteBatch, statusText, sprite.getX() + 130, sprite.getY() + sprite.getHeight() + 50);
        }
    }

    private TextureRegion getFrame() {
        switch (currentState) {
            case JUMPING:
                return jumpAnimation.getKeyFrame(stateTime, false);
            case ATTACKING:
                return attackingAnimation.getKeyFrame(stateTime, false);
            case IDLE:
            default:
                return attackingAnimation.getKeyFrames()[0];
        }
    }

    public void startJump() {
        if (currentState != ZombieState.IDLE) return;

        Vector2 startPoint = new Vector2((float) creature.getPosition(), this.floorY);
        Vector2 endPoint = new Vector2((float) creature.getTargetPosition(), this.floorY);

        this.parm = new ParabolicMovement(startPoint, endPoint);
        this.stateTime = 0;
        this.currentState = ZombieState.JUMPING;
        this.flip = endPoint.x < startPoint.x;
    }

    private void setStatusText(String text, Color color) {
        this.statusText = text;
        this.statusColor = color;
        this.statusTimer = 2.0f;
    }

    public void updateData(Creature creature) {
        this.creature = creature;
        if (currentState == ZombieState.IDLE) {
            this.sprite.setPosition((float) creature.getPosition(), this.floorY);
        }
    }

    public int getId() {
        return creature.getId();
    }

    public Rectangle getZombieRectangle() {
        zombieRectangle.set(sprite.getX() + 100f, sprite.getY(), sprite.getWidth() * 0.4f, sprite.getHeight() * 0.9f);
        return zombieRectangle;
    }

    public boolean isAnimationFinished() {
        return currentState == ZombieState.IDLE;
    }

    public static void loadResources(String spritesheetPath, String audioPath, BitmapFont font) {
        if (spriteSheet != null) {
            return;
        }
        spriteSheet = new Texture(Gdx.files.internal(spritesheetPath));
        ZombieActor.font = font;

        TextureRegion[][] keyframes = TextureRegion.split(spriteSheet, spriteSheet.getWidth() / 8, spriteSheet.getHeight() / 4);

        attackingAnimation = new Animation<>(0.15f, Arrays.copyOfRange(keyframes[0], 0, 4));
        jumpAnimation = new Animation<>(0.1f, Arrays.copyOfRange(keyframes[1], 0, 8));

        attackSound = Gdx.audio.newSound(Gdx.files.internal(audioPath));
    }

    public static void unloadResources() {
        if (spriteSheet != null) {
            spriteSheet.dispose();
            spriteSheet = null;
        }
        if (attackSound != null) {
            attackSound.dispose();
            attackSound = null;
        }
    }
}
