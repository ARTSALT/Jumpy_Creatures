package com.badlogic.drop;

import com.badlogic.drop.entity.Zombie;
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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Arrays;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    SpriteBatch spriteBatch;
    ShapeRenderer shapeRenderer;

    // a janela do jogo é dividida em duas viewports
    private Viewport gameViewport;
    private Viewport uiViewport;

    // texturas
    private Texture backgroundTexture;
    private Texture buttonsTexture;
    private TextureRegion playButton;

    // efeitos sonoros e música
    private Music music;

    Zombie z;

    // inicializa os recursos do jogo
    @Override
    public void create() {
        spriteBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        z = new Zombie();

        // define a viewport do jogo e da interface
        gameViewport = new ExtendViewport(Gdx.graphics.getWidth() * 0.8f, Gdx.graphics.getHeight());
        uiViewport = new ScreenViewport();

        // carrega a textura do background
        backgroundTexture = new Texture("graveyard_background.png");

        // carrega a textura dos botões
        buttonsTexture = new Texture("buttons.png");
        playButton = TextureRegion.split(
            buttonsTexture, buttonsTexture.getWidth()/4, buttonsTexture.getHeight()/3)[0][0];

        // carrega áudio e música
        music = Gdx.audio.newMusic(Gdx.files.internal("graveyard_trap.mp3"));
        music.setLooping(true);
        music.setVolume(.5f);   // 50% do volume original
        music.play();
    }

    @Override
    public void resize(int width, int height) {
        float gameWidth = width * 0.8f;

        gameViewport.update((int) gameWidth, height, true);
        uiViewport.update(width - (int) gameWidth, height, true);

        // posiciona a viewport da interface na direita
        uiViewport.setScreenX((int) gameWidth);
        uiViewport.setScreenY(0);
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
        renderUI();
    }

    // renderiza a interface do usuário
    private void renderUI() {
        uiViewport.apply();

        // desenha o fundo da interface
        shapeRenderer.setProjectionMatrix(uiViewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1);
        shapeRenderer.rect(0, 0, uiViewport.getScreenWidth(), uiViewport.getScreenHeight());
        shapeRenderer.end();

        // desenha o botão de play
        float buttonWidth = playButton.getRegionWidth() * 0.6f;
        float buttonHeight = playButton.getRegionHeight() * 0.6f;
        float buttonX = uiViewport.getScreenWidth() / 2f - buttonWidth / 2f;
        float buttonY = uiViewport.getScreenHeight() / 2f - buttonHeight / 3f;
        spriteBatch.setProjectionMatrix(uiViewport.getCamera().combined);
        spriteBatch.begin();
        spriteBatch.draw(playButton, buttonX, buttonY,
            buttonWidth, buttonHeight);
        spriteBatch.end();
    }

    // função de controle de entrada do usuário
    private void input() {
        // verifica se o usuário pressionou a tecla ESC para sair
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
    }

    // função de lógica do jogo
    private void logic() {
        z.logic();

    }

    // função de desenho, a ordem é importante
    private void draw() {
        float worldWidth = gameViewport.getWorldWidth();
        float worldHeight = gameViewport.getWorldHeight();

        // desenha o background
        spriteBatch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight);

        //spriteBatch.draw(currentFrame, worldWidth/4, worldHeight/4, worldWidth/4, worldHeight/4);
        z.draw(spriteBatch);
    }

    @Override
    public void dispose() {
        spriteBatch.dispose();
        shapeRenderer.dispose();
        backgroundTexture.dispose();
        buttonsTexture.dispose();
        music.dispose();
    }
}
