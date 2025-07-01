package com.softwaretesting.libgdx;

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
import com.softwaretesting.database.User;
import com.softwaretesting.simulation.Simulation;
import com.softwaretesting.simulation.entity.Creature;

import java.util.Random;

public class GameScreen implements Screen {
    LibGdxApplication application;
    User user;

    // classe que gerencia a simulação
    Simulation simulation;

    // processa um zumbi por vez
    Zombie currentZombie;
    Zombie selectedZombie;
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
    public GameScreen(LibGdxApplication application, User user) {
        this.application = application;
        this.user = user;

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
        uiStage = new Stage(uiViewport, application.getSpriteBatch());
        Gdx.input.setInputProcessor(uiStage);

        // filtra o campo de texto para aceitar apenas dígitos
        TextField.TextFieldFilter digitsOnlyFilter =
            (textField, c) -> Character.isDigit(c);

        nameInput = new TextField("", application.getSkin());
        nameInput.setMessageText("Zumbis");
        nameInput.setTextFieldFilter(digitsOnlyFilter);

        uiStage.addActor(nameInput);

        // carrega recursos do zumbi
        Zombie.loadResources("images/zombie_spritesheet.png",
            "audio/zombie_attack.mp3", application.getSkin().getFont("title"));
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
        application.getSpriteBatch().setProjectionMatrix(gameViewport.getCamera().combined);

        // renderiza o jogo
        application.getSpriteBatch().begin();
        draw();
        application.getSpriteBatch().end();

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
        application.getShapeRenderer().setProjectionMatrix(uiViewport.getCamera().combined);
        application.getShapeRenderer().begin(ShapeRenderer.ShapeType.Filled);
        application.getShapeRenderer().setColor(0.2f, 0.2f, 0.2f, 1);
        application.getShapeRenderer().rect(0, 0, uiViewport.getScreenWidth(), uiViewport.getScreenHeight());
        application.getShapeRenderer().end();

        // botão de play
        float buttonWidth = playButton.getRegionWidth() * 0.6f;
        float buttonHeight = playButton.getRegionHeight() * 0.6f;
        float buttonX = uiViewport.getScreenWidth() / 2f - buttonWidth / 2f;
        float buttonY = uiViewport.getScreenHeight() / 2f - buttonHeight / 3f;

        // atualiza o retângulo do botão
        playButtonBounds.set(buttonX, buttonY, buttonWidth, buttonHeight);

        // desenha o botão
        application.getSpriteBatch().setProjectionMatrix(uiViewport.getCamera().combined);
        application.getSpriteBatch().begin();
        application.getSpriteBatch().draw(playButton, buttonX, buttonY, buttonWidth, buttonHeight);
        application.getSpriteBatch().end();

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
                Zombie zombie = new Zombie(c);

                // verifica se o retângulo do zumbi contém as coordenadas do mouse
                if (zombie.getZombieRectangle().contains(worldCoords.x, worldCoords.y)) {
                    selectedZombie = zombie;
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
                    currentZombie = new Zombie(simulation.process());
                    //currentZombie.reset();
                } catch (Exception e) {
                    System.err.println("Erro na caixa de entrada: " + e.getMessage());
                }
            }
        }

        // processa o próximo zumbi
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            if (simulation != null && currentZombie.finishedProcessing()) {
                currentZombie = new Zombie(simulation.process());
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
        if (currentZombie != null) {
            // atualiza a lógica do zumbi atual
            currentZombie.logic();

            // verifica se o zumbi passou das bordas da tela
            if (isOutOfBounds(currentZombie)) {
                cameraZoom *= 1.05f;    // zoom out

                // atualiza a câmera
                ((OrthographicCamera) gameViewport.getCamera()).zoom = cameraZoom;
                gameViewport.getCamera().update();
            }

            // processa o próximo zumbi
            if (currentZombie.finishedProcessing()) {
                // encontra o zumbi mais próximo
                Zombie closestZombie = getClosestZombie();

                if (closestZombie != null) {
                    currentZombie.steal(closestZombie);
                }

                currentZombie = new Zombie(simulation.process());
                //currentZombie.reset();
            }
        }
    }

    private Zombie getClosestZombie() {
        Zombie closestZombie = null;
        double minDist = Double.MAX_VALUE;

        for (Creature c : simulation.getCreatures()) {
            Zombie z = new Zombie(c);
            if (z.equals(currentZombie)) continue;

            double dist = Math.abs(z.getSprite().getX() - currentZombie.getSprite().getX());
            if (dist < minDist) {
                minDist = dist;
                closestZombie = z;
            }
        }

        return closestZombie;
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
            application.getSpriteBatch().draw(backgroundTexture, backgroundX, backgroundY);
        }

        if (simulation != null) {
            for (Creature c : simulation.getCreatures()) {
                Zombie z = new Zombie(c);
                z.draw(application.getSpriteBatch());

                // desenha o zumbi
                z.draw(application.getSpriteBatch());
            }
        }

        // desenha o retângulo de colisão
        if (selectedZombie != null) {
            application.getFont().setColor(Color.WHITE);
            application.getFont().draw(application.getSpriteBatch(),
                "Moedas do zumbi selecionado: " + String.format("%,d", selectedZombie.getCoins()),
                0f, gameViewport.getWorldHeight() - 20f);
            application.getSpriteBatch().end();

            application.getShapeRenderer().setProjectionMatrix(gameViewport.getCamera().combined);
            application.getShapeRenderer().begin(ShapeRenderer.ShapeType.Line);
            application.getShapeRenderer().setColor(Color.RED);
            application.getShapeRenderer().rect(selectedZombie.getZombieRectangle().x,
                selectedZombie.getZombieRectangle().y,
                selectedZombie.getZombieRectangle().width,
                selectedZombie.getZombieRectangle().height);
            application.getShapeRenderer().end();

            application.getSpriteBatch().begin();
        } else {
            application.getFont().setColor(Color.WHITE);
            application.getFont().draw(application.getSpriteBatch(),
                "Selecione um zumbi com o mouse",
                0f, 0f);
        }

        // desenha a seta sobre o zumbi atual
        if (currentZombie != null) {
            float arrowX = currentZombie.getSprite().getX() + currentZombie.getSprite().getWidth() / 2f - 50f;
            float arrowY = currentZombie.getSprite().getY() + currentZombie.getSprite().getHeight() + 10f;
            TextureRegion frame = arrowAnimation.getKeyFrame(arrowTime += Gdx.graphics.getDeltaTime(), true);
            application.getSpriteBatch().draw(frame, arrowX, arrowY, 100, 100);

            if (arrowTime > arrowAnimation.getAnimationDuration()) {
                arrowTime = 0;
            }
        }
    }

    // Metodo auxiliar para verificar se um zumbi está fora dos limites
    private boolean isOutOfBounds(Zombie creature) {
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
        application.getSpriteBatch().dispose();
        application.getShapeRenderer().dispose();
        backgroundTexture.dispose();
        buttonsTexture.dispose();
        music.dispose();
        uiStage.dispose();
        Zombie.unloadResources();
        arrowTexture.dispose();
        application.getFont().dispose();
    }
}
