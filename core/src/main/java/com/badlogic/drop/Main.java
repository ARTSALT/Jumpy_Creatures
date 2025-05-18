package com.badlogic.drop;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    SpriteBatch spriteBatch;
    FitViewport viewport;

    // texturas
    private Texture backgroundTexture;

    // animação do zumbi
    Animation<TextureRegion> attacking;
    float stateTime = 0f;
    boolean playAttackAnimation = false;

    // efeitos sonoros e música
    private Sound attackSound;
    private Music music;

    // retângulos para colisão entre zumbis
    Rectangle zombieRectangle;

    // inicializa os recursos do jogo
    @Override
    public void create() {
        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(8, 5);   // viewport com 8 unidades de largura e 5 de altura

        backgroundTexture = new Texture("graveyard_background.png");

        Texture spriteSheet = new Texture("attacking.png");
        TextureRegion[][] tmp = TextureRegion.split(spriteSheet,
            spriteSheet.getWidth()/4, spriteSheet.getHeight());
        TextureRegion[] frames = new TextureRegion[4];

        System.arraycopy(tmp[0], 0, frames, 0, 4);

        attacking = new Animation<>(0.15f, frames);

        // retângulos para colisão
        zombieRectangle = new Rectangle();

        // carrega áudio e música
        attackSound = Gdx.audio.newSound(Gdx.files.internal("zombie_attack.mp3"));

        music = Gdx.audio.newMusic(Gdx.files.internal("graveyard_trap.mp3"));
        music.setLooping(true);
        music.setVolume(.5f);   // 50% do volume original
        music.play();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render() {
        input();    // controla a entrada do usuário
        logic();    // atualiza estado do jogo

        // limpa a tela e prepara o desenho
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();

        draw();     // desenha os elementos do jogo

        spriteBatch.end();
    }

    // função de controle de entrada do usuário
    private void input() {
        // verifica se o usuário pressionou a tecla ESC para sair
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }

        // verifica se a tecla 'A' foi pressionada
        if (Gdx.input.isKeyJustPressed(Input.Keys.A) && !playAttackAnimation) {
            playAttackAnimation = true; // ativa a animação de ataque
            stateTime = 0;              // reinicia o tempo da animação
            attackSound.play();         // toca o som de ataque
        }
    }

    // função de lógica do jogo
    private void logic() {
        // reinicia a animação de ataque se ela já terminou
        if (playAttackAnimation && attacking.isAnimationFinished(stateTime)) {
            playAttackAnimation = false;
        }
    }

    // função de desenho, a ordem é importante
    private void draw() {
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        // desenha o background
        spriteBatch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight);

        // desenha a animação
        if (playAttackAnimation) {
            stateTime += Gdx.graphics.getDeltaTime();
        }

        TextureRegion currentFrame = playAttackAnimation ?
            attacking.getKeyFrame(stateTime, false) :
            attacking.getKeyFrames()[0];

        spriteBatch.draw(currentFrame, 0, 0, worldWidth/4, worldHeight/4);
    }

    @Override
    public void dispose() {
        spriteBatch.dispose();
        backgroundTexture.dispose();
        attackSound.dispose();
        music.dispose();
    }
}
