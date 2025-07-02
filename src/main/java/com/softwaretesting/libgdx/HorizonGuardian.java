package com.softwaretesting.libgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.softwaretesting.simulation.entity.Creature;
import com.softwaretesting.simulation.entity.Guardian;
import com.softwaretesting.simulation.entity.ParabolicMovement;

import java.util.Arrays;

public class HorizonGuardian {

    public enum State {
        IDLE,
        JUMPING,
        ATTACKING,
        FINISHED
    }

    // referência à criatura associada a este zumbi
    private final Creature creature;

    // spritesheet e texturas
    private static Texture spriteSheet;
    private static Texture coinTexture;

    // animações do zumbi
    private static Animation<TextureRegion> attacking;
    private static Animation<TextureRegion> jumpingUp;
    private static Animation<TextureRegion> jumpingDown;
    private static Animation<TextureRegion> landing;
    private static Animation<TextureRegion> beingHit;
    private static Animation<TextureRegion> dying;

    private HorizonGuardian.State currentState;
    private float stateTime = 0f;

    // audio
    private static Sound attackSound;

    // retângulos para colisão entre zumbis
    private final Rectangle guardianRectangle;

    // classe que gerencia o salto em um movimento parabólico
    private ParabolicMovement parm;

    // sprite com posição e tamanho
    private final Sprite sprite;
    private final Sprite coinSprite;
    private final float y;
    private boolean flip = false;

    // sistema de texto
    private static BitmapFont font;
    private String statusText = null;
    private Color statusColor = null;
    private float statusTimer = 0f;

    /**
     * Cria guadião associado a uma criatura.
     * A posição inicial do sprite é baseada na posição da criatura.
     *
     * @param creature A criatura associada ao guardião.
     */
    public HorizonGuardian(Creature creature) {
        this.creature = creature;

        if (spriteSheet == null) {
            throw new IllegalStateException("SpriteSheet não carregado. Use Guardian.loadResources() para carregar.");
        }

        // posição inicial do sprite é baseada na posição da criatura
        float initialX = (float) this.creature.getPosition();
        this.y = Gdx.graphics.getHeight() / 9f;

        sprite = new Sprite(spriteSheet);
        sprite.setBounds(initialX, y, 350, 350);

        guardianRectangle = new Rectangle();
        currentState = HorizonGuardian.State.IDLE;

        parm = new ParabolicMovement(
                new Vector2(sprite.getX(), sprite.getY()),
                new Vector2(sprite.getX(), sprite.getY())
        );

        coinSprite = new Sprite(coinTexture);
        coinSprite.setBounds(sprite.getX() + sprite.getWidth() / 2f - 50f, sprite.getY() + 60f, 50, 60);
    }

    /**
     * Lógica do guardião, atualiza o estado e a posição do sprite.
     * Deve ser chamado a cada frame.
     */
    public void logic() {
        stateTime += Gdx.graphics.getDeltaTime();

        switch(currentState) {
            case IDLE:
                currentState = HorizonGuardian.State.JUMPING;
                stateTime = 0;
                jump((float) creature.getTargetPosition());
                break;

            case JUMPING:
                if (sprite.getY() == y) {
                    creature.updatePosition();
                    currentState = HorizonGuardian.State.ATTACKING;
                    stateTime = 0;
                    attackSound.play();
                }
                break;
            case ATTACKING:
                if (attacking.isAnimationFinished(stateTime)) {
                    currentState = HorizonGuardian.State.FINISHED;
                    stateTime = 0;
                }
                break;
            case FINISHED:
                break;
        }

        coinSprite.setPosition(sprite.getX() + sprite.getWidth() / 2f - 50f, sprite.getY() + 60f);
    }

    /**
     * Verifica se a base do retângulo de colisão deste guardião toca o centro da
     * base do retângulo de um zumbi.
     *
     * @param zombie O zumbi para checar a colisão.
     * @return true se houver colisão, false caso contrário.
     */
    public boolean collidesWith(Zombie zombie) {
        Rectangle thisRect = this.getGuardianRectangle();
        Rectangle otherRect = zombie.getZombieRectangle();

        // Ponto central da base do outro guardião
        Vector2 otherBaseCenter = new Vector2(
                otherRect.x + otherRect.width / 2,
                otherRect.y
        );

        // Checa se o ponto central da base do zumbi está dentro do retângulo deste guardião
        return thisRect.contains(otherBaseCenter);
    }

    /**
     * Inicia o salto do zumbi para uma posição alvo.
     */
    public void jump(float targetX) {
        Vector2 startPoint = new Vector2(sprite.getX(), sprite.getY());
        Vector2 endPoint = new Vector2(targetX, y);
        parm = new ParabolicMovement(startPoint, endPoint);
    }

    /**
     * Desenha o estado atual do zumbi na tela, a cada frame.
     */
    public void draw(SpriteBatch spriteBatch) {
        if (currentState == HorizonGuardian.State.JUMPING) {
            parm.update(Gdx.graphics.getDeltaTime());
            sprite.setPosition(parm.getPosition().x, parm.getPosition().y);
        }

        TextureRegion currentFrame = getFrame();
        sprite.setRegion(currentFrame);

        if (currentState == HorizonGuardian.State.JUMPING) {
            flip = !(parm.getEndPoint().x > parm.getStartPoint().x);
        }

        sprite.setFlip(flip, false);
        sprite.setSize(350, 350);
        sprite.draw(spriteBatch);
        coinSprite.draw(spriteBatch);

        // atualiza o timer do texto
        if (statusTimer > 0) {
            statusTimer -= Gdx.graphics.getDeltaTime();

            if (statusTimer <= 0) {
                statusText = null;
                statusColor = null;
            }
        }

        // desenha o texto se existir
        if (statusText != null && statusColor != null) {
            font.setColor(statusColor);
            font.draw(spriteBatch, statusText,
                    sprite.getX() + 30f,
                    sprite.getY() + sprite.getHeight());
        }
    }

    public void reset() {
        currentState = HorizonGuardian.State.IDLE;
        stateTime = 0f;
        flip = false;
        sprite.setX((float) creature.getPosition());
    }

    public TextureRegion getFrame() {
        switch(currentState) {
            case ATTACKING:
                return attacking.getKeyFrame(stateTime, false);

            case JUMPING:
                if (sprite.getY() < parm.getJumpHeight() - 1f &&
                        sprite.getX() < parm.getStartPoint().x + (parm.getDistanceX() * 0.4f)) {
                    return jumpingUp.getKeyFrame(stateTime, false);
                } else if (sprite.getY() < parm.getJumpHeight() - 1f &&
                        sprite.getX() >= parm.getStartPoint().x + (parm.getDistanceX() * 0.4f)) {
                    return jumpingDown.getKeyFrame(stateTime, false);
                } else {
                    return landing.getKeyFrame(stateTime, false);
                }

            case IDLE:
            case FINISHED:
            default:
                return attacking.getKeyFrames()[0];
        }
    }

    public Creature getCreature() {
        return this.creature;
    }

    public void steal(Zombie otherZombie) {
        int stolenCoins = this.creature.stealFrom(otherZombie.getCreature());

        this.setStatusText("+" + stolenCoins, Color.GREEN);
        otherZombie.setStatusText("-" + stolenCoins, Color.RED);
    }

    public int getCoins() {
        return creature.getCoins();
    }

    public double getPosition() {
        return creature.getPosition();
    }

    public Sprite getSprite() {
        return sprite;
    }

    public Rectangle getGuardianRectangle() {
        guardianRectangle.setPosition(sprite.getX() + 100f, sprite.getY() + 40f);
        guardianRectangle.setSize(sprite.getWidth() * 0.4f, sprite.getHeight() * 0.8f);
        return guardianRectangle;
    }

    public boolean finishedProcessing() {
        return currentState == HorizonGuardian.State.FINISHED;
    }

    private void setStatusText(String text, Color color) {
        this.statusText = text;
        this.statusColor = color;
        this.statusTimer = 3f; // 3 segundos de exibição
    }

    public static void loadResources(String spritesheetPath, String audioPath, BitmapFont font) {
        if (HorizonGuardian.spriteSheet != null) {
            HorizonGuardian.spriteSheet.dispose();
        }

        HorizonGuardian.spriteSheet = new Texture(Gdx.files.internal(spritesheetPath));

        TextureRegion[][] keyframes = TextureRegion.split(spriteSheet,
                spriteSheet.getWidth() / 8, spriteSheet.getHeight() / 4);

        // cada keyframe tem tamanho 8, mas a maioria das animações tem menos que 8 frames
        TextureRegion[] attackingFrames = Arrays.copyOfRange(keyframes[0], 0, 4); // 4 frames
        TextureRegion[] jumpingUpFrames = Arrays.copyOfRange(keyframes[1], 0, 4); // 4 frames de subida
        TextureRegion[] jumpingDownFrames = Arrays.copyOfRange(keyframes[1], 4, 5); // 1 frames de descida
        TextureRegion[] landingFrames = Arrays.copyOfRange(keyframes[1], 5, 8); // 3 frames de aterrissagem
        TextureRegion[] beingHitFrames = Arrays.copyOfRange(keyframes[2], 0, 3); // 3 frames
        TextureRegion[] dyingFrames = Arrays.copyOfRange(keyframes[3], 0, 5); // 5 frames

        attacking = new Animation<>(0.15f, attackingFrames);
        jumpingUp = new Animation<>(0.1f, jumpingUpFrames);
        jumpingDown = new Animation<>(0.1f, jumpingDownFrames);
        landing = new Animation<>(0.1f, landingFrames);
        beingHit = new Animation<>(0.1f, beingHitFrames);
        dying = new Animation<>(0.1f, dyingFrames);
        attackSound = Gdx.audio.newSound(Gdx.files.internal(audioPath));

        if (coinTexture != null) {
            coinTexture.dispose();
        }

        coinTexture = new Texture(Gdx.files.internal("images/coin.png"));
        HorizonGuardian.font = font;
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

        if (coinTexture != null) {
            coinTexture.dispose();
            coinTexture = null;
        }

        if (font != null) {
            font.dispose();
            font = null;
        }
    }
}
