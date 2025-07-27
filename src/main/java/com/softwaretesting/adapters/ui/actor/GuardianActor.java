package com.softwaretesting.adapters.ui.actor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.softwaretesting.core.domain.model.Creature;
import com.softwaretesting.core.domain.model.ParabolicMovement;

import java.util.Arrays;

public class GuardianActor {

    private enum GuardianState {
        IDLE,
        JUMPING
    }

    private Creature creature;
    private static Texture spriteSheet;
    private static BitmapFont font;
    private static Texture coinTexture;

    private static Animation<TextureRegion> idleAnimation;
    private static Animation<TextureRegion> jumpAnimation;

    private float stateTime = 0f;
    private GuardianState currentState = GuardianState.IDLE;
    private boolean flip = false;

    private final Sprite sprite;
    private final Sprite coinSprite;
    private ParabolicMovement parm;
    private final Rectangle guardianRectangle;
    private final float floorY;

    private String statusText;
    private Color statusColor;
    private float statusTimer = 0f;
    private int lastKnownCoins;
    private final GlyphLayout glyphLayout;

    public GuardianActor(Creature creature, float floorY) {
        if (spriteSheet == null || idleAnimation == null) {
            throw new IllegalStateException("Resources for GuardianActor not loaded correctly. Call GuardianActor.loadResources() first.");
        }
        this.creature = creature;
        this.floorY = floorY;
        this.sprite = new Sprite(idleAnimation.getKeyFrames()[0]);
        this.sprite.setSize(350, 350);
        this.sprite.setPosition((float) creature.getPosition(), this.floorY);
        this.guardianRectangle = new Rectangle();
        this.coinSprite = new Sprite(coinTexture);
        this.coinSprite.setSize(50, 60);
        this.lastKnownCoins = creature.getCoins();
        this.glyphLayout = new GlyphLayout();
    }

    public void update(float deltaTime) {
        stateTime += deltaTime;

        if (currentState == GuardianState.JUMPING && parm != null) {
            parm.update(deltaTime);
            sprite.setPosition(parm.getPosition().x, parm.getPosition().y);

            if (parm.isFinished()) {
                currentState = GuardianState.IDLE;
                stateTime = 0;
                sprite.setPosition(parm.getEndPoint().x, parm.getEndPoint().y);
            }
        }

        // Atualiza o timer do texto de status
        if (statusTimer > 0) {
            statusTimer -= deltaTime;
            if (statusTimer <= 0) {
                statusText = null;
            }
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
        sprite.setRegion(getFrame());
        sprite.setFlip(flip, false);
        sprite.draw(spriteBatch);
        int numCoinSprites = calculateCoinSprites();
        float coinStackOffsetY = 15f;
        for (int i = 0; i < numCoinSprites; i++) {
            coinSprite.setX(sprite.getX() + sprite.getWidth() / 2 - 0.225f * sprite.getWidth());
            coinSprite.setY(sprite.getY() + 50f + (i * coinStackOffsetY));
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
        Animation<TextureRegion> animation = (currentState == GuardianState.JUMPING) ? jumpAnimation : idleAnimation;
        if (animation == null) animation = idleAnimation;
        return (animation != null) ? animation.getKeyFrame(stateTime, false) : null;
    }

    private void setStatusText(String text, Color color) {
        this.statusText = text;
        this.statusColor = color;
        this.statusTimer = 2.0f; // Duração do texto na tela
    }

    public void startJump() {
        if (currentState != GuardianState.IDLE || creature.getCoins() == 0) return;

        Vector2 startPoint = new Vector2((float) creature.getPosition(), this.floorY);
        Vector2 endPoint = new Vector2((float) creature.getTargetPosition(), this.floorY);

        this.parm = new ParabolicMovement(startPoint, endPoint);
        this.stateTime = 0;
        this.currentState = GuardianState.JUMPING;
        this.flip = endPoint.x < startPoint.x;
    }

    public void updateData(Creature creature) {
        int newCoins = creature.getCoins();
        if (newCoins != this.lastKnownCoins) {
            int delta = newCoins - this.lastKnownCoins;
            setStatusText(String.format("%+d", delta), delta > 0 ? Color.GREEN : Color.RED);
        }
        this.lastKnownCoins = newCoins;

        this.creature = creature;
        if (currentState == GuardianState.IDLE) {
            this.sprite.setPosition((float) creature.getPosition(), this.floorY);
        }
    }

    // --- Getters e outros métodos ---
    public int getId() { return creature.getId(); }
    public boolean isAnimationFinished() { return currentState == GuardianState.IDLE; }
    public float getX() { return this.sprite.getX(); }
    public float getY() { return this.sprite.getY(); }
    public float getSpriteWidth() { return this.sprite.getWidth(); }
    public float getSpriteHeight() { return this.sprite.getHeight(); }
    public Rectangle getGuardianRectangle() {
        guardianRectangle.set(sprite.getX() + 100f, sprite.getY(), sprite.getWidth() * 0.4f, sprite.getHeight() * 0.9f);
        return guardianRectangle;
    }

    public static void loadResources(String spritesheetPath) {
        if (spriteSheet != null) return;
        try {
            spriteSheet = new Texture(Gdx.files.internal(spritesheetPath));
            GuardianActor.font = ZombieActor.getFont();
            GuardianActor.coinTexture = ZombieActor.getCoinTexture();

            int frameWidth = 80;
            int frameHeight = 86;
            TextureRegion[][] keyframes = TextureRegion.split(spriteSheet, frameWidth, frameHeight);

            idleAnimation = new Animation<>(0.2f, Arrays.copyOfRange(keyframes[0], 0, 1));
            jumpAnimation = new Animation<>(0.15f, Arrays.copyOfRange(keyframes[1], 0, 6));
        } catch (Exception e) {
            Gdx.app.error("GuardianActor", "Failed to load resources for guardian.", e);
        }
    }

    public static void unloadResources() {
        if (spriteSheet != null) {
            spriteSheet.dispose();
            spriteSheet = null;
        }
    }
}
