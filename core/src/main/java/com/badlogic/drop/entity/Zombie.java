package com.badlogic.drop.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;

public class Zombie {

    // Moedas e localização do zumbi
    private int coins;
    private double location;

    // spritesheet e texturas
    Texture spriteSheet;
    TextureRegion[][] keyframes;
    TextureRegion[] attackingFrames;
    TextureRegion[] jumpingFrames;
    TextureRegion[] beingHitFrames;
    TextureRegion[] dyingFrames;

    // animação do zumbi
    Animation<TextureRegion> attacking;
    Animation<TextureRegion> jumping;
    float stateTime = 0f;
    boolean playAttackAnimation = false;
    boolean playJumpAnimation = false;

    // audio
    private Sound attackSound;

    // retângulos para colisão entre zumbis
    Rectangle zombieRectangle;

    ParabolicMovement parm;

    Sprite sprite;
    float x;
    float y;

    public Zombie(int coins, double location) {
        this.coins = coins;
        this.location = location;

        // carrega o spritesheet completo
        Texture spriteSheet = new Texture("zombie_spritesheet.png");

        // separa o spritesheet em 4 keyframes de animação
        TextureRegion[][] keyframes = TextureRegion.split(spriteSheet,
            spriteSheet.getWidth() / 8, spriteSheet.getHeight() / 4);

        // cada keyframe tem tamanho 8, mas a maioria das animações tem menos que 8 frames
        TextureRegion[] attackingFrames = Arrays.copyOfRange(keyframes[0], 0, 4);   // 4 frames
        TextureRegion[] jumpingFrames = Arrays.copyOfRange(keyframes[0], 0, 8);     // 8 frames
        TextureRegion[] beingHitFrames = Arrays.copyOfRange(keyframes[0], 0, 3);    // 3 frames
        TextureRegion[] dyingFrames = Arrays.copyOfRange(keyframes[0], 0, 5);       // 5 frames

        sprite = new Sprite(spriteSheet);

        attacking = new Animation<>(0.1f, attackingFrames);
        jumping = new Animation<>(0.1f, jumpingFrames);

        // retângulos para colisão
        zombieRectangle = new Rectangle();
    }

    public Zombie() {
        this.coins = 1000000;
        // this.location = location;

        x = 100;
        y = 100;

        // carrega o spritesheet completo
        spriteSheet = new Texture("zombie_spritesheet.png");

        // separa o spritesheet em 4 keyframes de animação
        keyframes = TextureRegion.split(spriteSheet,
            spriteSheet.getWidth() / 8, spriteSheet.getHeight() / 4);

        // cada keyframe tem tamanho 8, mas a maioria das animações tem menos que 8 frames
        attackingFrames = Arrays.copyOfRange(keyframes[0], 0, 4);   // 4 frames
        jumpingFrames = Arrays.copyOfRange(keyframes[0], 0, 8);     // 8 frames
        beingHitFrames = Arrays.copyOfRange(keyframes[0], 0, 3);    // 3 frames
        dyingFrames = Arrays.copyOfRange(keyframes[0], 0, 5);       // 5 frames

        attacking = new Animation<>(0.1f, attackingFrames);
        jumping = new Animation<>(0.1f, jumpingFrames);

        // carrega o som de ataque
        attackSound = Gdx.audio.newSound(Gdx.files.internal("zombie_attack.mp3"));

        // retângulos para colisão
        zombieRectangle = new Rectangle();

        sprite = new Sprite();
        sprite.setPosition(x, y);

        parm = new ParabolicMovement(
            new Vector2(sprite.getX(), sprite.getY()),
            new Vector2(sprite.getX() + 100, sprite.getY()),
            200,
            500
        );
    }

    public void logic() {
        // verifica se a tecla 'A' foi pressionada
        if (Gdx.input.isKeyJustPressed(Input.Keys.A) && !playAttackAnimation) {
            playAttackAnimation = true; // ativa a animação de ataque
            stateTime = 0;              // reinicia o tempo da animação
            attackSound.play();         // toca o som de ataque
        }

        // reinicia a animação de ataque se ela já terminou
        if (playAttackAnimation && attacking.isAnimationFinished(stateTime)) {
            playAttackAnimation = false;
            sprite.setY(y);
        }

        // Para a animação de salto
        if (playJumpAnimation && sprite.getY() == (y + sprite.getHeight())) {
            playJumpAnimation = false; // desativa a animação de pulo
        }

        // verifica se a tecla 'SPACE' foi pressionada
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE) && !playJumpAnimation) {
            playJumpAnimation = true; // ativa a animação de pulo
            stateTime = 0;            // reinicia o tempo da animação
        }
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
        sprite.setSize(x, y);

        // Desenha o sprite
        sprite.draw(spriteBatch);
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public double getLocation() {
        return location;
    }

    public void setLocation(double location) {
        this.location = location;
    }
}
