package com.badlogic.drop.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;

public class Zombie {

    public enum ZombieState {
        IDLE,
        JUMPING,
        ATTACKING,
        FINISHED
    }

    // Moedas e localização do zumbi
    private int coins;
    private double position;
    private double targetPosition;

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

    private ZombieState currentState;
    private float stateTime = 0f;

    // audio
    private static Sound attackSound;

    // retângulos para colisão entre zumbis
    private Rectangle zombieRectangle;

    // classe que gerencia o salto em um movimento parabólico
    private ParabolicMovement parm;

    // sprite com posição e tamanho
    private final Sprite sprite;
    private final Sprite coinSprite;
    private final float x;
    private final float y;
    private boolean flip = false;

    public Zombie() {
        this(1000000, 0);
    }

    public Zombie(int coins, double position) {
        this.coins = coins;
        this.position = position;

        // retângulos para colisão
        zombieRectangle = new Rectangle();

        if (spriteSheet == null) {
            throw new IllegalStateException("SpriteSheet não carregado. Use Zombie.loadResources() para carregar.");
        }

        // carrega o sprite a partir da textura
        sprite = new Sprite(spriteSheet);
        x = (float) this.position;
        y = Gdx.graphics.getHeight() / 9f;

        // define posição e tamanho do sprite
        sprite.setBounds(x, y,
            350, 350);

        // retângulos para colisão
        zombieRectangle = new Rectangle();

        currentState = ZombieState.IDLE;

        // cria o movimento parabólico
        parm = new ParabolicMovement(
            new Vector2(sprite.getX(), sprite.getY()),
            new Vector2(sprite.getX() + 100, sprite.getY()));

        // define a posição e o tamanho do sprite da moeda
        coinSprite = new Sprite(coinTexture);
        coinSprite.setBounds(sprite.getX() + sprite.getWidth() / 2f - 50f, sprite.getY() + 60f,
            50, 60);
    }

    public void logic() {
        stateTime += Gdx.graphics.getDeltaTime();

        switch(currentState) {
            case IDLE:
                // transição para jumping
                currentState = ZombieState.JUMPING;
                stateTime = 0;
                jump((float) targetPosition);
                break;

            case JUMPING:
                // transição para attacking
                if (sprite.getY() == y) {
                    currentState = ZombieState.ATTACKING;
                    stateTime = 0;
                    attackSound.play();
                }
                break;

            case ATTACKING:
                // transição para finished
                if (attacking.isAnimationFinished(stateTime)) {
                    currentState = ZombieState.FINISHED;
                    stateTime = 0;
                }
                break;

            case FINISHED:

                break;
        }

        // atualiza a posição da moeda
        coinSprite.setPosition(sprite.getX() + sprite.getWidth() / 2f - 50f, sprite.getY() + 60f);
    }

    // Recebe as coordenadas finais do salto
    public void jump(float x) {
        Vector2 endPoint = new Vector2(x, y);
        Vector2 startPoint = new Vector2(sprite.getX(), sprite.getY());

        // Cria uma nova instância de ParabolicMovement
        parm = new ParabolicMovement(startPoint, endPoint);
    }

    public void draw(SpriteBatch spriteBatch) {
        // atualiza stateTime
        if (currentState != ZombieState.IDLE && currentState != ZombieState.FINISHED) {
            stateTime += Gdx.graphics.getDeltaTime();
        }

        if (currentState == ZombieState.JUMPING) {
            parm.update(Gdx.graphics.getDeltaTime());
            sprite.setPosition(parm.getPosition().x, parm.getPosition().y);
        }

        TextureRegion currentFrame = getFrame();
        sprite.setRegion(currentFrame);

        // Define a orientação do sprite
        if (currentState == ZombieState.JUMPING) {
            flip = !(parm.getEndPoint().x > parm.getStartPoint().x);
        }

        sprite.setFlip(flip, false);
        sprite.setSize(350, 350);

        // desenha zumbi e as moedas
        sprite.draw(spriteBatch);
        coinSprite.draw(spriteBatch);
    }

    // redefine o estado do zumbi
    public void reset() {
        currentState = ZombieState.IDLE;
        stateTime = 0f;
        flip = false;
        parm = new ParabolicMovement(
            new Vector2(sprite.getX(), sprite.getY()),
            new Vector2((float) targetPosition, y));
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

    // métodos estáticos
    public static void loadResources(String spritesheetPath, String audioPath) {
        if (Zombie.spriteSheet != null) {
            Zombie.spriteSheet.dispose();
        }
        Zombie.spriteSheet = new Texture(Gdx.files.internal(spritesheetPath));

        // separa o spritesheet em 4 keyframes de animação
        TextureRegion[][] keyframes = TextureRegion.split(spriteSheet,
            spriteSheet.getWidth() / 8, spriteSheet.getHeight() / 4);

        // cada keyframe tem tamanho 8, mas a maioria das animações tem menos que 8 frames
        TextureRegion[] attackingFrames = Arrays.copyOfRange(keyframes[0], 0, 4);   // 4 frames
        TextureRegion[] jumpingUpFrames = Arrays.copyOfRange(keyframes[1], 0, 4);   // 4 frames de subida
        TextureRegion[] jumpingDownFrames = Arrays.copyOfRange(keyframes[1], 4, 5); // 1 frames de descida
        TextureRegion[] landingFrames = Arrays.copyOfRange(keyframes[1], 5, 8);     // 3 frames de aterrissagem
        TextureRegion[] beingHitFrames = Arrays.copyOfRange(keyframes[2], 0, 3);    // 3 frames
        TextureRegion[] dyingFrames = Arrays.copyOfRange(keyframes[3], 0, 5);       // 5 frames

        // cria as animações
        attacking = new Animation<>(0.1f, attackingFrames);
        jumpingUp = new Animation<>(0.1f, jumpingUpFrames);
        jumpingDown = new Animation<>(0.1f, jumpingDownFrames);
        landing = new Animation<>(0.1f, landingFrames);
        beingHit = new Animation<>(0.1f, beingHitFrames);
        dying = new Animation<>(0.1f, dyingFrames);

        // carrega o som de ataque
        attackSound = Gdx.audio.newSound(Gdx.files.internal(audioPath));

        // carrega a textura da moeda
        if (coinTexture != null) {
            coinTexture.dispose();
        }
        coinTexture = new Texture(Gdx.files.internal("coin.png"));
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
    }

    // getters e setters
    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public double getPosition() {
        return position;
    }

    public void setPosition(double position) {
        this.position = position;
    }

    public void setTargetPosition(double targetPosition) {
        this.targetPosition = targetPosition;
    }

    public Rectangle getZombieRectangle() {
        zombieRectangle.setPosition(sprite.getX() + 100f, sprite.getY() + 40f);
        zombieRectangle.setSize(sprite.getWidth() * 0.4f, sprite.getHeight() * 0.8f);
        return zombieRectangle;
    }

    public boolean isProcessing() {
        return currentState != ZombieState.FINISHED;
    }

    public Sprite getSprite() {
        return sprite;
    }
}
