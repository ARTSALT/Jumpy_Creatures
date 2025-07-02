package com.softwaretesting.adapters.ui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.softwaretesting.core.domain.model.User;
import com.softwaretesting.core.domain.model.Simulation;
import com.softwaretesting.core.domain.model.Creature;
import com.softwaretesting.adapters.ui.LibGdxApplication;
import com.softwaretesting.adapters.ui.actor.ZombieActor;

import java.util.Random;

public class GameScreen extends ScreenTemplate implements Screen {
    LibGdxApplication application;
    User user;

    // classe que gerencia a simulação
    Simulation simulation;

    // processa um zumbi por vez
    ZombieActor currentZombieActor;
    ZombieActor selectedZombieActor;
    boolean showCollider;

    // a janela do jogo é dividida em duas viewports
    private final Viewport gameViewport;
    private final Viewport uiViewport;

    // texturas
    private final Texture backgroundTexture;
    private final Texture buttonsTexture;
    private final Texture arrowTexture;
    private final TextureRegion playButton;
    private final Rectangle playButtonBounds;

    // animação da seta
    private final Animation<TextureRegion> arrowAnimation;
    private float arrowTime;

    // interface do usuário
    private final Stage uiStage;
    private final TextField nameInput;

    // efeitos sonoros e música
    private final Music music;

    private float cameraZoom = 1.0f;

    // inicializa os recursos do jogo
    public GameScreen(LibGdxApplication application) {
        super(application);

        this.application = application;
        this.user = application.getCurrentUser();

        // define a viewport do jogo e da interface
        gameViewport = new ExtendViewport(Gdx.graphics.getWidth() * 0.8f, Gdx.graphics.getHeight(),
            Integer.MAX_VALUE, Gdx.graphics.getHeight());
        uiViewport = new ScreenViewport();

        // carrega a textura do background
        backgroundTexture = new Texture("images/background.png");
        backgroundTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        // carrega a textura dos botões
        buttonsTexture = new Texture("images/buttons.png");
        playButton = TextureRegion.split(
            buttonsTexture, buttonsTexture.getWidth()/4, buttonsTexture.getHeight()/3)[0][0];
        playButtonBounds = new Rectangle(
            uiViewport.getWorldWidth() / 2 - playButton.getRegionWidth() / 2f,
            uiViewport.getWorldHeight() / 2 - playButton.getRegionHeight() / 2f,
            playButton.getRegionWidth(), playButton.getRegionHeight());

        // carrega a textura e animação da seta
        arrowTexture = new Texture("images/arrow_sheet.png");
        TextureRegion[][] arrowFrames = TextureRegion.split(
            arrowTexture, arrowTexture.getWidth() / 5, arrowTexture.getHeight() / 4);
        TextureRegion[] allFrames = new TextureRegion[5 * 4];
        int index = 0;
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 5; col++) {
                allFrames[index++] = arrowFrames[row][col];
            }
        }
        arrowAnimation = new Animation<>(0.1f, allFrames);

        // carrega áudio e música
        music = Gdx.audio.newMusic(Gdx.files.internal("audio/graveyard_trap.mp3"));
        music.setLooping(true);
        music.setVolume(.5f);   // 50% do volume original
        music.play();

        // inicializa o Stage e os componentes da interface
        uiStage = new Stage(uiViewport, spriteBatch);
        Gdx.input.setInputProcessor(uiStage);

        // filtra o campo de texto para aceitar apenas dígitos
        TextField.TextFieldFilter digitsOnlyFilter =
            (textField, c) -> Character.isDigit(c);

        nameInput = new TextField("", skin);
        nameInput.setMessageText("Zumbis");
        nameInput.setTextFieldFilter(digitsOnlyFilter);

        uiStage.addActor(nameInput);

        // carrega recursos do zumbi
        ZombieActor.loadResources("images/zombie_spritesheet.png",
            "audio/zombie_attack.mp3", skin.getFont("title"));
    }

    @Override
    public void show() {}

    @Override
    public void render(float deltaTime) {
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
        uiStage.act(deltaTime);
        uiStage.draw();
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
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

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

        // processa a entrada com ENTER
        boolean enterPressed = Gdx.input.isKeyJustPressed(Input.Keys.ENTER);

        // captura clique do mouse
        boolean mouseClick = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);

        // verifica se o mouse pressionou sobre algum zumbi
        if (mouseClick && simulation != null) {
            int mouseX = Gdx.input.getX();
            int mouseY = Gdx.input.getY();
            Vector3 worldCoords = gameViewport.unproject(new Vector3(mouseX, mouseY, 0));

            for (Creature c : simulation.getCreatures()) {
                ZombieActor zombieActor = new ZombieActor(c);

                // verifica se o retângulo do zumbi contém as coordenadas do mouse
                if (zombieActor.getZombieRectangle().contains(worldCoords.x, worldCoords.y)) {
                    selectedZombieActor = zombieActor;
                    break;
                }
            }
        }

        // verifica se o botão de play foi clicado ou se a tecla ENTER foi pressionada
        if (mouseClick || enterPressed) {
            int mouseX = Gdx.input.getX();
            int mouseY = Gdx.input.getY();
            Vector3 worldCoords = uiViewport.unproject(new Vector3(mouseX, mouseY, 0));

            if (playButtonBounds.contains(worldCoords.x, worldCoords.y)
                || enterPressed) {
                String input = nameInput.getText();
                try {
                    int numZumbis = Integer.parseInt(input);
                    System.out.println("Simulação iniciada");
                    System.out.println("Número de zumbis: " + numZumbis);

                    // inicializa a simulação com o número de zumbis
                    simulation = new Simulation(numZumbis, 1, (int) gameViewport.getWorldWidth(),
                        (max, min) -> new Random().nextDouble() * (max - min) + min);
                    currentZombieActor = new ZombieActor(simulation.process());
                    //currentZombie.reset();
                } catch (Exception e) {
                    System.err.println("Erro na caixa de entrada: " + e.getMessage());
                }
            }
        }

        // processa o próximo zumbi
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            if (simulation != null && currentZombieActor.finishedProcessing()) {
                currentZombieActor = new ZombieActor(simulation.process());
                //currentZombie.reset();
            }
        }

        // mostra o retângulo de colisão
        if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
            showCollider = !showCollider;
        }
    }

    // função de lógica do jogo
    private void logic() {
        if (currentZombieActor != null) {
            // atualiza a lógica do zumbi atual
            currentZombieActor.logic();

            // verifica se o zumbi passou das bordas da tela
            if (isOutOfBounds(currentZombieActor)) {
                cameraZoom *= 1.05f;    // zoom out

                // atualiza a câmera
                ((OrthographicCamera) gameViewport.getCamera()).zoom = cameraZoom;
                gameViewport.getCamera().update();
            }

            // processa o próximo zumbi
            if (currentZombieActor.finishedProcessing()) {
                // encontra o zumbi mais próximo
                ZombieActor closestZombieActor = getClosestZombie();

                if (closestZombieActor != null) {
                    currentZombieActor.steal(closestZombieActor);
                }

                currentZombieActor = new ZombieActor(simulation.process());
                //currentZombie.reset();
            }
        }
    }

    private ZombieActor getClosestZombie() {
        ZombieActor closestZombieActor = null;
        double minDist = Double.MAX_VALUE;

        for (Creature c : simulation.getCreatures()) {
            ZombieActor z = new ZombieActor(c);
            if (z.equals(currentZombieActor)) continue;

            double dist = Math.abs(z.getSprite().getX() - currentZombieActor.getSprite().getX());
            if (dist < minDist) {
                minDist = dist;
                closestZombieActor = z;
            }
        }

        return closestZombieActor;
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

        if (simulation != null) {
            for (Creature c : simulation.getCreatures()) {
                ZombieActor z = new ZombieActor(c);
                z.draw(spriteBatch);

                // desenha o zumbi
                z.draw(spriteBatch);
            }
        }

        // desenha o retângulo de colisão
        if (selectedZombieActor != null) {
            font.setColor(Color.WHITE);
            font.draw(spriteBatch,
                "Moedas do zumbi selecionado: " + String.format("%,d", selectedZombieActor.getCoins()),
                0f, gameViewport.getWorldHeight() - 20f);
            spriteBatch.end();

            shapeRenderer.setProjectionMatrix(gameViewport.getCamera().combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(selectedZombieActor.getZombieRectangle().x,
                selectedZombieActor.getZombieRectangle().y,
                selectedZombieActor.getZombieRectangle().width,
                selectedZombieActor.getZombieRectangle().height);
            shapeRenderer.end();

            spriteBatch.begin();
        } else {
            font.setColor(Color.WHITE);
            font.draw(spriteBatch,
                "Selecione um zumbi com o mouse",
                0f, 0f);
        }

        // desenha a seta sobre o zumbi atual
        if (currentZombieActor != null) {
            float arrowX = currentZombieActor.getSprite().getX() + currentZombieActor.getSprite().getWidth() / 2f - 50f;
            float arrowY = currentZombieActor.getSprite().getY() + currentZombieActor.getSprite().getHeight() + 10f;
            TextureRegion frame = arrowAnimation.getKeyFrame(arrowTime += Gdx.graphics.getDeltaTime(), true);
            spriteBatch.draw(frame, arrowX, arrowY, 100, 100);

            if (arrowTime > arrowAnimation.getAnimationDuration()) {
                arrowTime = 0;
            }
        }
    }

    // Metodo auxiliar para verificar se um zumbi está fora dos limites
    private boolean isOutOfBounds(ZombieActor creature) {
        Rectangle zombieRect = creature.getZombieRectangle();
        Rectangle viewportBounds = new Rectangle(
            gameViewport.getCamera().position.x - (gameViewport.getWorldWidth() * cameraZoom) / 2,
            gameViewport.getCamera().position.y - (gameViewport.getWorldHeight() * cameraZoom) / 2,
            gameViewport.getWorldWidth() * cameraZoom,
            gameViewport.getWorldHeight() * cameraZoom
        );

        return !viewportBounds.contains(zombieRect);
    }

    @Override
    public void dispose() {
        spriteBatch.dispose();
        shapeRenderer.dispose();
        backgroundTexture.dispose();
        buttonsTexture.dispose();
        music.dispose();
        uiStage.dispose();
        ZombieActor.unloadResources();
        arrowTexture.dispose();
        font.dispose();
    }
}
