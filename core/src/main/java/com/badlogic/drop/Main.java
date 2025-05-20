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
import com.badlogic.gdx.utils.viewport.Viewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    SpriteBatch spriteBatch;

    // a janela do jogo é dividida em duas viewports
    private Viewport gameViewport;
    private Viewport uiViewport;

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

        // define a viewport do jogo e da interface
        gameViewport = new FitViewport(Gdx.graphics.getWidth() * 0.8f, Gdx.graphics.getHeight());
        uiViewport = new FitViewport(Gdx.graphics.getWidth() * 0.2f, Gdx.graphics.getHeight());

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
        float gameWidth = width * 0.8f;     // jogo com 80% da largura
        float uiWidth = width * 0.2f;       // interface com 20% da largura

        // atualiza as viewports
        gameViewport.update((int)gameWidth, height, true);
        uiViewport.update((int)uiWidth, height, true);

        // posiciona a viewport da interface na direita
        uiViewport.setScreenX((int)gameWidth);
    }

    @Override
    public void render() {
        input();    // controla a entrada do usuário
        logic();    // atualiza estado do jogo

        // limpa a tela e prepara o desenho
        ScreenUtils.clear(Color.BLACK);
        gameViewport.apply();
        spriteBatch.setProjectionMatrix(gameViewport.getCamera().combined);

        // renderiza o jogo
        spriteBatch.begin();
        draw();
        spriteBatch.end();

        // renderiza a interface
        uiViewport.apply();
        spriteBatch.setProjectionMatrix(uiViewport.getCamera().combined);

        spriteBatch.begin();
        renderUI();
        spriteBatch.end();
    }

    // renderiza a interface do usuário
    private void renderUI() {

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
        float worldWidth = gameViewport.getWorldWidth();
        float worldHeight = gameViewport.getWorldHeight();

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
