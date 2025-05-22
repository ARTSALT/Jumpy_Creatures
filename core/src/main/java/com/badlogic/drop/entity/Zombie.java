package com.badlogic.drop.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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

    // Moedas e localização do zumbi
    private int coins;
    private double position;

    // spritesheet e texturas
    private static Texture spriteSheet;
    private static Texture coinTexture;

    // animações do zumbi
    private static Animation<TextureRegion> attacking;
    private static Animation<TextureRegion> jumping;
    private static Animation<TextureRegion> beingHit;
    private static Animation<TextureRegion> dying;

    private float stateTime = 0f;
    private boolean playAttackAnimation = false;
    private boolean playJumpAnimation = false;
    private boolean playBeingHitAnimation = false;
    private boolean playDyingAnimation = false;

    // audio
    private static Sound attackSound;

    // retângulos para colisão entre zumbis
    private Rectangle zombieRectangle;

    // classe que gerencia o salto em um movimento parabólico
    private ParabolicMovement parm;

    // sprite com posição e tamanho
    private final Sprite sprite;
    private final Sprite coinSprite;
    private float x;
    private float y;

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

        // cria o movimento parabólico
        parm = new ParabolicMovement(
            new Vector2(sprite.getX(), sprite.getY()),
            new Vector2(sprite.getX() + 100, sprite.getY()),
            200,
            500
        );

        // define a posição e o tamanho do sprite da moeda
        coinSprite = new Sprite(coinTexture);
        coinSprite.setBounds(sprite.getX() + sprite.getWidth() / 2f - 50f, sprite.getY() + 60f,
            50, 60);
    }

    public void logic() {
        System.out.println(Gdx.graphics.getDeltaTime());
        // verifica se a tecla 'A' foi pressionada
        if (Gdx.input.isKeyJustPressed(Input.Keys.A) && !playAttackAnimation) {
            playAttackAnimation = true; // ativa a animação de ataque
            stateTime = 0;              // reinicia o tempo da animação
            attackSound.play();         // toca o som de ataque
        }

        // reinicia a animação de ataque se ela já terminou
        if (playAttackAnimation && attacking.isAnimationFinished(stateTime)) {
            playAttackAnimation = false;
        }

        // Para a animação de salto
        if (playJumpAnimation && sprite.getY() == y) {
            System.out.println("Jump finished");
            playJumpAnimation = false; // desativa a animação de pulo
            jump(sprite.getX() - 100, sprite.getY());
        }

        // verifica se a tecla 'SPACE' foi pressionada
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && !playJumpAnimation) {
            playJumpAnimation = true; // ativa a animação de pulo
            stateTime = 0;            // reinicia o tempo da animação
        }

        // atualiza a posição da moeda
        coinSprite.setPosition(sprite.getX() + sprite.getWidth() / 2f - 50f, sprite.getY() + 60f);
    }

    public void jump(float x, float y) {
        Vector2 endPoint = new Vector2(x, y);
        Vector2 startPoint = new Vector2(sprite.getX(), sprite.getY());

        // Cria uma nova instância de ParabolicMovement
        parm = new ParabolicMovement(startPoint, endPoint, 200, 500);
    }

    public void draw(SpriteBatch spriteBatch) {
        // desenha a animação
        if (playAttackAnimation) {
            stateTime += Gdx.graphics.getDeltaTime();
        }

        if (playJumpAnimation) {
            stateTime += Gdx.graphics.getDeltaTime();
            parm.update(Gdx.graphics.getDeltaTime());
            sprite.setPosition(parm.getPosition().x, parm.getPosition().y);
        }

        TextureRegion currentFrame = playAttackAnimation ?
            attacking.getKeyFrame(stateTime, false) :
            attacking.getKeyFrames()[0];

        sprite.setRegion(currentFrame);
        sprite.setSize(350, 350);

        // Desenha o sprite
        sprite.draw(spriteBatch);
        coinSprite.draw(spriteBatch);
    }

    public TextureRegion getSprite() {
        if (playAttackAnimation) {
            return attacking.getKeyFrame(stateTime, false);
        } else if (playJumpAnimation) {
            return jumping.getKeyFrame(stateTime, false);
        } else {
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
        TextureRegion[] jumpingFrames = Arrays.copyOfRange(keyframes[0], 0, 8);     // 8 frames
        TextureRegion[] beingHitFrames = Arrays.copyOfRange(keyframes[0], 0, 3);    // 3 frames
        TextureRegion[] dyingFrames = Arrays.copyOfRange(keyframes[0], 0, 5);       // 5 frames

        // cria as animações
        attacking = new Animation<>(0.1f, attackingFrames);
        jumping = new Animation<>(0.1f, jumpingFrames);
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

    public Rectangle getZombieRectangle() {
        zombieRectangle.setPosition(sprite.getX() + 100f, sprite.getY() + 40f);
        zombieRectangle.setSize(sprite.getWidth() * 0.4f, sprite.getHeight() * 0.8f);
        return zombieRectangle;
    }

    public boolean isProcessing() {
        return playAttackAnimation || playJumpAnimation || playBeingHitAnimation || playDyingAnimation;
    }
}
