package com.badlogic.drop;

import com.badlogic.drop.entity.Zombie;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    // classe que gerencia a simulação
    Simulation simulation;

    // processa um zumbi por vez
    Zombie currentZombie;

    // renderizador de sprites e formas
    SpriteBatch spriteBatch;
    ShapeRenderer shapeRenderer;

    // a janela do jogo é dividida em duas viewports
    private Viewport gameViewport;
    private Viewport uiViewport;

    // texturas
    private Texture backgroundTexture;
    private Texture buttonsTexture;
    private TextureRegion playButton;
    private Rectangle playButtonBounds;

    // interface do usuário
    private Stage uiStage;
    private TextField nameInput;

    // efeitos sonoros e música
    private Music music;

    private float cameraZoom = 1.0f;

    // inicializa os recursos do jogo
    @Override
    public void create() {
        spriteBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        // define a viewport do jogo e da interface
        gameViewport = new ExtendViewport(Gdx.graphics.getWidth() * 0.8f, Gdx.graphics.getHeight(),
            Integer.MAX_VALUE, Gdx.graphics.getHeight());
        uiViewport = new ScreenViewport();

        // carrega a textura do background
        backgroundTexture = new Texture("background.png");
        backgroundTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        // carrega a textura dos botões
        buttonsTexture = new Texture("buttons.png");
        playButton = TextureRegion.split(
            buttonsTexture, buttonsTexture.getWidth()/4, buttonsTexture.getHeight()/3)[0][0];
        playButtonBounds = new Rectangle(
            uiViewport.getWorldWidth() / 2 - playButton.getRegionWidth() / 2f,
            uiViewport.getWorldHeight() / 2 - playButton.getRegionHeight() / 2f,
            playButton.getRegionWidth(), playButton.getRegionHeight());

        // carrega áudio e música
        music = Gdx.audio.newMusic(Gdx.files.internal("graveyard_trap.mp3"));
        music.setLooping(true);
        music.setVolume(.5f);   // 50% do volume original
        music.play();

        // inicializa o Stage e os componentes da interface
        uiStage = new Stage(uiViewport, spriteBatch);
        Gdx.input.setInputProcessor(uiStage);

        Skin skin = new Skin(Gdx.files.internal("skin/plain-james-ui.json"));

        // filtra o campo de texto para aceitar apenas dígitos
        TextField.TextFieldFilter digitsOnlyFilter =
            (textField, c) -> Character.isDigit(c);

        nameInput = new TextField("", skin);
        nameInput.setMessageText("Zumbis");
        nameInput.setTextFieldFilter(digitsOnlyFilter);

        uiStage.addActor(nameInput);

        // carrega recursos do zumbi
        Zombie.loadResources("zombie_spritesheet.png", "zombie_attack.mp3");
    }

    @Override
    public void resize(int width, int height) {
        float gameWidth = width * 0.8f;

        gameViewport.update((int) gameWidth, height, true);
        uiViewport.update(width - (int) gameWidth, height, true);

        // posiciona a viewport da interface na direita
        uiViewport.setScreenX((int) gameWidth);
        uiViewport.setScreenY(0);

        // aplica o zoom na câmera conforme a largura da viewport
        if (gameViewport.getWorldWidth() > gameViewport.getWorldHeight()) {
            cameraZoom = gameViewport.getWorldWidth() / gameViewport.getWorldHeight();
        } else {
            cameraZoom = gameViewport.getWorldHeight() / gameViewport.getWorldWidth();
        }

        // atualiza a câmera com o novo zoom
        OrthographicCamera camera = (OrthographicCamera) gameViewport.getCamera();
        camera.viewportWidth = gameViewport.getWorldWidth();
        camera.viewportHeight = gameViewport.getWorldHeight();
        camera.position.set(gameViewport.getWorldWidth() / 2f,
            gameViewport.getWorldHeight() / 2f, 0);
        camera.zoom = cameraZoom;
        camera.update();
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
        uiStage.act(Gdx.graphics.getDeltaTime());
        uiStage.draw();
    }

    // renderiza a interface do usuário
    private void renderUI() {
        uiViewport.apply();

        // background da interface
        shapeRenderer.setProjectionMatrix(uiViewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1);
        shapeRenderer.rect(0, 0, uiViewport.getScreenWidth(), uiViewport.getScreenHeight());
        shapeRenderer.end();

        // botão de play
        float buttonWidth = playButton.getRegionWidth() * 0.6f;
        float buttonHeight = playButton.getRegionHeight() * 0.6f;
        float buttonX = uiViewport.getScreenWidth() / 2f - buttonWidth / 2f;
        float buttonY = uiViewport.getScreenHeight() / 2f - buttonHeight / 3f;

        // atualiza o retângulo do botão
        playButtonBounds.set(buttonX, buttonY, buttonWidth, buttonHeight);

        // desenha o botão
        spriteBatch.setProjectionMatrix(uiViewport.getCamera().combined);
        spriteBatch.begin();
        spriteBatch.draw(playButton, buttonX, buttonY, buttonWidth, buttonHeight);
        spriteBatch.end();

        // desenha a caixa de texto acima do botão
        nameInput.setSize(buttonWidth * 1.5f, 40);
        nameInput.setPosition(buttonX - buttonWidth * 0.2f, buttonY + buttonHeight + 10);
    }

    // função de controle de entrada do usuário
    private void input() {
        // verifica se a tecla ESC foi pressionada e fecha o jogo
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }

        // aplica o zoom na câmera com as setas
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            cameraZoom += 0.01f;
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            cameraZoom -= 0.01f;
        }

        // atualiza a câmera com o novo zoom
        ((OrthographicCamera) gameViewport.getCamera()).zoom = cameraZoom;
        gameViewport.getCamera().update();

        // verifica se o botão de play foi clicado
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            int mouseX = Gdx.input.getX();
            int mouseY = Gdx.input.getY();

            Vector3 worldCoords = uiViewport.unproject(new Vector3(mouseX, mouseY, 0));
            if (playButtonBounds.contains(worldCoords.x, worldCoords.y)) {
                int numZumbis = Integer.parseInt(nameInput.getText());
                System.out.println("Botão clicado!");
                System.out.println("Número de zumbis: " + numZumbis);

                // inicializa a simulação com o número de zumbis
                simulation = new Simulation(numZumbis, (int) gameViewport.getWorldWidth());
            }
        }

        if (Gdx.input.isKeyPressed(Input.Keys.P)) {
            if (simulation != null) {
                currentZombie = simulation.process();
            }
        }
    }

    // função de lógica do jogo
    private void logic() {
        if (currentZombie != null) {
            // atualiza a lógica do zumbi
            currentZombie.logic();
        }
    }

    // função de desenho, a ordem é importante
    private void draw() {
        OrthographicCamera camera = (OrthographicCamera) gameViewport.getCamera();

        // calcula a posição do background
        float visibleWidth = camera.viewportWidth * camera.zoom;
        float backgroundY = camera.position.y - backgroundTexture.getHeight() / 2f;
        float tileWidth = backgroundTexture.getWidth();
        float centerTileX = MathUtils.floor(camera.position.x / tileWidth) * tileWidth;
        int tilesToEachSide = (int)Math.ceil(visibleWidth / tileWidth) + 1;

        // desenha o background continuamente conforme o zoom da câmera
        for (int i = -tilesToEachSide; i <= tilesToEachSide; i++) {
            float backgroundX = centerTileX + i * tileWidth;
            spriteBatch.draw(backgroundTexture, backgroundX, backgroundY);
        }

        if (currentZombie != null) {
            // desenha o zumbi
            currentZombie.draw(spriteBatch);
        }
    }

    @Override
    public void dispose() {
        spriteBatch.dispose();
        shapeRenderer.dispose();
        backgroundTexture.dispose();
        buttonsTexture.dispose();
        music.dispose();
        uiStage.dispose();
        Zombie.unloadResources();
    }
}
