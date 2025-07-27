package com.softwaretesting.adapters.ui.actor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.softwaretesting.core.domain.model.Cluster;
import com.softwaretesting.core.domain.model.Creature;
import com.softwaretesting.core.domain.model.ParabolicMovement;

import java.util.Arrays;
import java.util.Objects;

public class ClusterActor {

    private enum ClusterState {
        IDLE,
        JUMPING,
        ATTACKING
    }

    private Creature creature;
    private static Texture spriteSheet;
    private static BitmapFont font;
    private static Texture coinTexture;
    private static Sound attackSound;

    private static Animation<TextureRegion> attackingAnimation;
    private static Animation<TextureRegion> jumpAnimation;

    private float stateTime = 0f;
    private ClusterState currentState = ClusterState.IDLE;
    private boolean flip;

    private final Sprite sprite;
    private ParabolicMovement parm;
    private final Rectangle clusterRectangle;
    private final float floorY;

    private String statusText;
    private Color statusColor;
    private float statusTimer = 0f;
    private int lastKnownCoins;
    private final GlyphLayout glyphLayout;
    private final Sprite coinSprite;

    public ClusterActor(Creature creature, float floorY) {
        if (spriteSheet == null || attackingAnimation == null) {
            throw new IllegalStateException("Resources for ClusterActor not loaded correctly. Call ClusterActor.loadResources() first.");
        }
        this.creature = creature;
        this.floorY = floorY;
        this.sprite = new Sprite(attackingAnimation.getKeyFrames()[0]);
        this.sprite.setSize(350, 350);
        this.sprite.setPosition((float) creature.getPosition(), this.floorY);
        this.clusterRectangle = new Rectangle();
        this.glyphLayout = new GlyphLayout();
        this.coinSprite = new Sprite(coinTexture);
        this.coinSprite.setSize(50, 60);
        this.lastKnownCoins = creature.getCoins();
        this.flip = creature.getTargetPosition() < creature.getPosition();
    }

    public void startAttack() {
        if (currentState != ClusterState.IDLE) return;

        currentState = ClusterState.ATTACKING;
        stateTime = 0;
        attackSound.play();
    }

    public void update(float deltaTime) {
        stateTime += deltaTime;

        switch (currentState) {
            case JUMPING:
                if (parm != null) {
                    parm.update(deltaTime);
                    sprite.setPosition(parm.getPosition().x, parm.getPosition().y);
                    if (parm.isFinished()) {
                        currentState = ClusterState.ATTACKING;
                        stateTime = 0;
                        attackSound.play();
                        sprite.setPosition(parm.getEndPoint().x, parm.getEndPoint().y);
                    }
                }
                break;
            case ATTACKING:
                if (attackingAnimation.isAnimationFinished(stateTime)) {
                    currentState = ClusterState.IDLE;
                    stateTime = 0;
                }
                break;
            case IDLE:
                break;
        }

        if (statusTimer > 0) {
            statusTimer -= deltaTime;
            if (statusTimer <= 0) statusText = null;
        }
    }

    private int calculateCoinSprites() {
        final int COINS_PER_SPRITE = 200_000;
        int currentCoins = creature.getCoins();
        if (currentCoins <= 0) return 0;
        int numSprites = currentCoins / COINS_PER_SPRITE;
        return (numSprites == 0) ? 1 : numSprites;
    }

    public void draw(SpriteBatch spriteBatch) {
        TextureRegion currentFrame = getFrame();
        if (currentFrame == null) return;
        sprite.setRegion(currentFrame);
        sprite.setFlip(flip, false);
        sprite.draw(spriteBatch);
        int numCoinSprites = calculateCoinSprites();
        float coinStackOffsetY = 15f;
        for (int i = 0; i < numCoinSprites; i++) {
            coinSprite.setPosition(sprite.getX() + sprite.getWidth() / 2f - 50f, sprite.getY() + 60f + (i * coinStackOffsetY));
            coinSprite.draw(spriteBatch);
        }

        if (statusText != null && statusTimer > 0) {
            font.setColor(statusColor);
            glyphLayout.setText(font, statusText);
            float textX = sprite.getX() + (sprite.getWidth() - glyphLayout.width) / 2;
            float textY = sprite.getY() + sprite.getHeight() + glyphLayout.height + 20;
            font.draw(spriteBatch, glyphLayout, textX, textY);
        }
    }

    private TextureRegion getFrame() {
        Animation<TextureRegion> animation;
        if (Objects.requireNonNull(currentState) == ClusterState.JUMPING) {
            animation = jumpAnimation;
        } else {
            animation = attackingAnimation;
        }
        if (currentState == ClusterState.IDLE) {
            return animation.getKeyFrames()[0];
        }
        return (animation != null) ? animation.getKeyFrame(stateTime, false) : null;
    }

    public void startJump() {
        if (currentState != ClusterState.IDLE) return;
        Vector2 startPoint = new Vector2((float) creature.getPosition(), this.floorY);
        Vector2 endPoint = new Vector2((float) creature.getTargetPosition(), this.floorY);
        this.parm = new ParabolicMovement(startPoint, endPoint);
        this.stateTime = 0;
        this.currentState = ClusterState.JUMPING;
        this.flip = endPoint.x < startPoint.x;
    }

    private void setStatusText(String text, Color color) {
        this.statusText = text;
        this.statusColor = color;
        this.statusTimer = 2.0f;
    }

    public void updateData(Creature creature) {
        int newCoins = creature.getCoins();
        if (newCoins != this.lastKnownCoins) {
            int delta = newCoins - this.lastKnownCoins;
            setStatusText(String.format("%+d", delta), delta > 0 ? Color.GREEN : Color.RED);
        }
        this.lastKnownCoins = newCoins;

        this.creature = creature;
        if (currentState == ClusterState.IDLE) {
            this.sprite.setPosition((float) creature.getPosition(), this.floorY);
        }
        if (creature instanceof Cluster) {
            int memberCount = ((Cluster) creature).getMembers().size();
            float baseSize = 350f;
            if (memberCount > 2) {
                float scaleFactor = 1.0f + (memberCount - 2) * 0.15f;
                float newSize = baseSize * scaleFactor;
                sprite.setSize(newSize, newSize);
            } else {
                sprite.setSize(baseSize, baseSize);
            }
        }
    }

    public int getId() { return creature.getId(); }
    public boolean isAnimationFinished() { return currentState == ClusterState.IDLE; }
    public float getX() { return this.sprite.getX(); }
    public float getY() { return this.sprite.getY(); }
    public float getSpriteWidth() { return this.sprite.getWidth(); }
    public float getSpriteHeight() { return this.sprite.getHeight(); }
    public Rectangle getClusterRectangle() {
        float width = sprite.getWidth() * 0.4f;
        float height = sprite.getHeight() * 0.9f;
        clusterRectangle.set(sprite.getX() + sprite.getWidth() * 0.3f, sprite.getY(), width, height);
        return clusterRectangle;
    }

    public static void loadResources(String spritesheetPath) {
        if (spriteSheet != null) return;
        try {
            spriteSheet = new Texture(Gdx.files.internal(spritesheetPath));
            ClusterActor.font = ZombieActor.getFont();
            ClusterActor.coinTexture = ZombieActor.getCoinTexture();
            ClusterActor.attackSound = ZombieActor.getAttackSound();

            int frameWidth = 128;
            int frameHeight = 128;
            TextureRegion[][] keyframes = TextureRegion.split(spriteSheet, frameWidth, frameHeight);

            // Animação de ataque: linha 0, 6 frames
            if (keyframes.length > 0 && keyframes[0].length >= 6) {
                attackingAnimation = new Animation<>(0.1f, Arrays.copyOfRange(keyframes[0], 0, 6));
            }

            // Animação de pulo: linha 1, 11 frames
            if (keyframes.length > 1 && keyframes[1].length >= 11) {
                jumpAnimation = new Animation<>(0.15f, Arrays.copyOfRange(keyframes[1], 0, 11));
            }

            if (attackingAnimation == null && keyframes.length > 0) attackingAnimation = new Animation<>(0.1f, keyframes[0][0]);
            if (jumpAnimation == null) jumpAnimation = attackingAnimation;

        } catch (Exception e) {
            Gdx.app.error("ClusterActor", "Failed to load resources for cluster.", e);
        }
    }

    public static void unloadResources() {
        if (spriteSheet != null) {
            spriteSheet.dispose();
            spriteSheet = null;
        }
    }
}
