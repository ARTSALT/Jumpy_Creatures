package com.softwaretesting.adapters.ui.actor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.Texture;
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

    private final Sprite sprite;
    private ParabolicMovement parm;
    private final Rectangle zombieRectangle;
    private final float floorY;

    private String statusText;
    private Color statusColor;
    private float statusTimer = 0f;
    private int lastKnownCoins;
    private final GlyphLayout glyphLayout;

    private static Texture coinTexture;
    private final Sprite coinSprite;

    public ZombieActor(Creature creature, float floorY) {
        if (spriteSheet == null) {
            throw new IllegalStateException("Resources not loaded. Call ZombieActor.loadResources() first.");
        }
        this.creature = creature;
        this.floorY = floorY;
        this.sprite = new Sprite(attackingAnimation.getKeyFrames()[0]);
        this.sprite.setSize(350, 350);
        this.sprite.setPosition((float) creature.getPosition(), this.floorY);
        this.zombieRectangle = new Rectangle();

        // Define a posição e o tamanho do sprite da moeda
        coinSprite = new Sprite(coinTexture);
        coinSprite.setSize(50, 60);
        this.lastKnownCoins = creature.getCoins();

        this.glyphLayout = new GlyphLayout();
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
                break;
        }

        if (statusTimer > 0) {
            statusTimer -= deltaTime;
            if (statusTimer <= 0) {
                statusText = null;
            }
        }

        // A posição da base da pilha de moedas é sempre atualizada, mas não desenhada aqui
        coinSprite.setPosition(sprite.getX() + sprite.getWidth() / 2f - 50f, sprite.getY() + 80f);
    }

    /**
     * Calcula quantos sprites de moeda devem ser exibidos.
     * @return O número de sprites a serem desenhados conforme a quantidade de moedas do zumbi.
     */
    private int calculateCoinSprites() {
        final int COINS_PER_SPRITE = 200_000;
        int currentCoins = creature.getCoins();

        if (currentCoins <= 0) {
            return 0;
        }

        // Calcula o número de sprites usando divisão inteira.
        int numSprites = currentCoins / COINS_PER_SPRITE;

        // Se o cálculo resultou em 0, mas o zumbi ainda tem moedas, mostra 1 sprite
        if (numSprites == 0) {
            return 1;
        }

        return numSprites;
    }

    public void draw(SpriteBatch spriteBatch) {
        sprite.setRegion(getFrame());
        sprite.setFlip(flip, false);
        sprite.draw(spriteBatch);
        int numCoinSprites = calculateCoinSprites();
        float coinStackOffsetY = 15f;
        for (int i = 0; i < numCoinSprites; i++) {
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
        return switch (currentState) {
            case JUMPING -> jumpAnimation.getKeyFrame(stateTime, false);
            case ATTACKING -> attackingAnimation.getKeyFrame(stateTime, false);
            default -> jumpAnimation.getKeyFrames()[0];
        };
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
        int newCoins = creature.getCoins();
        // Compara a quantidade de moedas atual com a última que vimos.
        if (newCoins != this.lastKnownCoins) {
            int delta = newCoins - this.lastKnownCoins;
            // Se houver diferença, ativa o texto de status.
            setStatusText(String.format("%+d", delta), delta > 0 ? Color.GREEN : Color.RED);
        }
        // Atualiza a última quantidade de moedas vista.
        this.lastKnownCoins = newCoins;

        this.creature = creature;
        if (currentState == ZombieState.IDLE) {
            this.sprite.setPosition((float) creature.getPosition(), this.floorY);
        }
    }

    public int getId() {
        return creature.getId();
    }

    public Creature getCreature() {
        return creature;
    }

    public Rectangle getZombieRectangle() {
        zombieRectangle.set(sprite.getX() + 100f, sprite.getY(), sprite.getWidth() * 0.4f, sprite.getHeight() * 0.9f);
        return zombieRectangle;
    }

    public float getSpriteWidth() {
        return this.sprite.getWidth();
    }

    public float getSpriteHeight() {
        return this.sprite.getHeight();
    }

    public float getX() {
        return this.sprite.getX();
    }

    public float getY() {
        return this.sprite.getY();
    }

    public static BitmapFont getFont() {
        if (font == null) {
            throw new IllegalStateException("Font not loaded. Call ZombieActor.loadResources() first.");
        }
        return font;
    }

    public static Texture getCoinTexture() {
        if (coinTexture == null) {
            throw new IllegalStateException("Coin texture not loaded. Call ZombieActor.loadResources() first.");
        }
        return coinTexture;
    }

    public boolean isAnimationFinished() {
        return currentState == ZombieState.IDLE;
    }

    public static Sound getAttackSound() { return attackSound; }

    public static void loadResources(String spritesheetPath, String audioPath, BitmapFont font) {
        if (spriteSheet != null) {
            return;
        }
        spriteSheet = new Texture(Gdx.files.internal(spritesheetPath));
        ZombieActor.font = font;

        TextureRegion[][] keyframes = TextureRegion.split(spriteSheet, spriteSheet.getWidth() / 8, spriteSheet.getHeight() / 4);

        attackingAnimation = new Animation<>(0.15f, Arrays.copyOfRange(keyframes[0], 0, 4));
        jumpAnimation = new Animation<>(0.15f, Arrays.copyOfRange(keyframes[1], 0, 8));

        attackSound = Gdx.audio.newSound(Gdx.files.internal(audioPath));

        // Carrega a textura da moeda
        coinTexture = new Texture(Gdx.files.internal("images/coin.png"));
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
        // Descarta a textura da moeda
        if (coinTexture != null) {
            coinTexture.dispose();
            coinTexture = null;
        }
    }
}
