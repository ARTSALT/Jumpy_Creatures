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

    // animação do zumbi
    Animation<TextureRegion> attacking;
    Animation<TextureRegion> jumping;
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
        shapeRenderer = new ShapeRenderer();

        // define a viewport do jogo e da interface
        gameViewport = new ExtendViewport(Gdx.graphics.getWidth() * 0.8f, Gdx.graphics.getHeight());
        uiViewport = new ScreenViewport();

        backgroundTexture = new Texture("graveyard_background.png");

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

        attacking = new Animation<>(0.1f, attackingFrames);
        jumping = new Animation<>(0.1f, jumpingFrames);

        buttonsTexture = new Texture("buttons.png");
        playButton = TextureRegion.split(
            buttonsTexture, buttonsTexture.getWidth()/4, buttonsTexture.getHeight()/3)[0][0];

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

        spriteBatch.draw(currentFrame, worldWidth/4, worldHeight/4, worldWidth/4, worldHeight/4);
    }

    @Override
    public void dispose() {
        spriteBatch.dispose();
        shapeRenderer.dispose();
        backgroundTexture.dispose();
        buttonsTexture.dispose();
        attackSound.dispose();
        music.dispose();
    }
}
