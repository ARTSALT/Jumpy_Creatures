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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.softwaretesting.adapters.ui.LibGdxApplication;
import com.softwaretesting.adapters.ui.actor.ClusterActor;
import com.softwaretesting.adapters.ui.actor.GuardianActor;
import com.softwaretesting.adapters.ui.actor.ZombieActor;
import com.softwaretesting.adapters.ui.presenter.GamePresenter;
import com.softwaretesting.adapters.ui.view.GameView;
import com.softwaretesting.core.domain.model.Cluster;
import com.softwaretesting.core.domain.model.Creature;
import com.softwaretesting.core.domain.model.Guardian;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GameScreen extends ScreenTemplate implements Screen, GameView {

    private final GamePresenter presenter;
    private final Map<Integer, ZombieActor> zombieActors = new HashMap<>();
    private final Map<Integer, ClusterActor> clusterActors = new HashMap<>();
    private GuardianActor guardianActor;

    private final Viewport gameViewport;
    private final Viewport uiViewport;

    private final Table setupTable;
    private final Label selectedZombieInfoLabel;
    private final Label messageLabel;

    private final Texture backgroundTexture;
    private final Texture upperBackgroundTexture;
    private final Texture lowerBackgroundTexture;

    private final Music music;
    private final float floorY;

    private final Texture arrowTexture;
    private final Animation<TextureRegion> arrowAnimation;
    private float arrowTime;

    public GameScreen(LibGdxApplication application) {
        super(application);
        this.presenter = new GamePresenter(this, application);

        gameViewport = new ExtendViewport(1920, 1080);
        uiViewport = new ScreenViewport();
        stage.setViewport(uiViewport);

        this.floorY = 280.f;

        backgroundTexture = new Texture(Gdx.files.internal("images/background.png"));
        upperBackgroundTexture = new Texture(Gdx.files.internal("images/upper_background.png"));
        lowerBackgroundTexture = new Texture(Gdx.files.internal("images/lower_background.png"));

        upperBackgroundTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        lowerBackgroundTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        music = Gdx.audio.newMusic(Gdx.files.internal("audio/graveyard_trap.mp3"));
        music.setLooping(true);
        music.setVolume(0.5f);

        messageLabel = new Label("", skin, "font", Color.YELLOW);
        stage.addActor(messageLabel);

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

        stage.clear();
        this.setupTable = new Table();
        setupTable.setFillParent(true);
        setupTable.center();
        setupTable.setBackground(skin.getDrawable("window"));
        stage.addActor(setupTable);

        TextField simNameInput = new TextField("", skin);
        simNameInput.setMessageText("Simulation Name");
        TextField numZombiesInput = new TextField("", skin);
        numZombiesInput.setMessageText("# Zombies (e.g., 10)");
        numZombiesInput.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        TextButton playButton = new TextButton("Run Simulation", skin);

        setupTable.add(new Label("Simulation Name:", skin)).pad(10).left();
        setupTable.add(simNameInput).width(300).height(40).pad(10).row();
        setupTable.add(new Label("# Zombies:", skin)).pad(10).left();
        setupTable.add(numZombiesInput).width(300).height(40).pad(10).row();
        setupTable.add(playButton).colspan(2).width(300).height(50).pad(20);

        selectedZombieInfoLabel = new Label("", skin, "font", Color.WHITE);
        selectedZombieInfoLabel.setPosition(20, 20);
        stage.addActor(selectedZombieInfoLabel);

        createListeners(playButton, simNameInput, numZombiesInput);
        ZombieActor.loadResources("images/zombie_spritesheet.png", "audio/zombie_attack.mp3", application.getFont());
        GuardianActor.loadResources("images/guardian_spritesheet.png");
        ClusterActor.loadResources("images/cluster_spritesheet.png");
    }

    private void createListeners(TextButton playButton, TextField nameInput, TextField numInput) {
        playButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                presenter.onPlayClicked(nameInput.getText(), numInput.getText());
                if (presenter.isGameRunning()) {
                    setupTable.setVisible(false);
                    if (!music.isPlaying()) music.play();
                }
            }
        });

        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                return switch (keycode) {
                    case Input.Keys.ESCAPE -> {
                        presenter.onExit();
                        yield true;
                    }
                    case Input.Keys.C -> {
                        presenter.onToggleColliders();
                        yield true;
                    }
                    case Input.Keys.LEFT -> {
                        presenter.onManualZoom(0.02f);
                        yield true;
                    }
                    case Input.Keys.RIGHT -> {
                        presenter.onManualZoom(-0.02f);
                        yield true;
                    }
                    case Input.Keys.P -> {
                        presenter.onAdvanceSimulationStep();
                        yield true;
                    }
                    case Input.Keys.ENTER -> {
                        presenter.onEnterPressed();
                        yield true;
                    }
                    case Input.Keys.END -> {
                        presenter.onEndPressed();
                        yield true;
                    }
                    default -> false;
                };
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (!presenter.isGameRunning() || button != Input.Buttons.LEFT) return false;

                Vector3 worldCoords = gameViewport.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));

                Optional<ZombieActor> clickedZombie = zombieActors.values().stream()
                    .filter(actor -> actor.getZombieRectangle().contains(worldCoords.x, worldCoords.y))
                    .findFirst();

                if (clickedZombie.isPresent()) {
                    presenter.onZombieSelected(clickedZombie.get().getId());
                    return true;
                }

                Optional<ClusterActor> clickedCluster = clusterActors.values().stream()
                    .filter(actor -> actor.getClusterRectangle().contains(worldCoords.x, worldCoords.y)).findFirst();
                if (clickedCluster.isPresent()) {
                    presenter.onZombieSelected(clickedCluster.get().getId());
                    return true;
                }

                if (guardianActor != null && guardianActor.getGuardianRectangle().contains(worldCoords.x, worldCoords.y)) {
                    presenter.onZombieSelected(guardianActor.getId());
                    return true;
                }

                presenter.onBackgroundClicked();
                return false;
            }

            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
                if (!presenter.isGameRunning()) return false;

                // amountY será -1 para scroll para cima (zoom in) e 1 para scroll para baixo (zoom out)
                // Multiplicamos por um fator para controlar a sensibilidade do zoom.
                float zoomAmount = amountY * 0.1f;
                presenter.onManualZoom(zoomAmount);
                return true;
            }
        });
    }

    @Override
    public void render(float deltaTime) {
        ScreenUtils.clear(Color.BLACK);

        presenter.onUpdate(deltaTime);
        zombieActors.values().forEach(actor -> actor.update(deltaTime));
        clusterActors.values().forEach(actor -> actor.update(deltaTime));
        if (guardianActor != null) {
            guardianActor.update(deltaTime);
        }

        gameViewport.apply();
        spriteBatch.setProjectionMatrix(gameViewport.getCamera().combined);
        spriteBatch.begin();
        drawGameBackground();
        zombieActors.values().forEach(actor -> actor.draw(spriteBatch));
        clusterActors.values().forEach(actor -> actor.draw(spriteBatch));
        if (guardianActor != null) {
            guardianActor.draw(spriteBatch);
        }
        drawArrowIndicator(deltaTime);
        spriteBatch.end();

        if (presenter.areCollidersVisible() || presenter.getSelectedCreature().isPresent()) {
            drawDebugGraphics();
        }

        uiViewport.apply();
        stage.act(deltaTime);
        updateUI();
        stage.draw();
    }

    private void drawArrowIndicator(float deltaTime) {
        Optional<Creature> arrowTargetOpt = presenter.getArrowTarget();
        if (arrowTargetOpt.isEmpty()) return;

        Creature targetCreature = arrowTargetOpt.get();
        arrowTime += deltaTime;
        TextureRegion frame = arrowAnimation.getKeyFrame(arrowTime, true);

        float arrowX = 0, arrowY = 0;
        boolean targetFound = false;

        if (targetCreature instanceof Guardian && guardianActor != null) {
            arrowX = guardianActor.getX() + guardianActor.getSpriteWidth() / 2 - 50f;
            arrowY = guardianActor.getY() + guardianActor.getSpriteHeight() + 50f;
            targetFound = true;
        } else {
            ZombieActor actor = zombieActors.get(targetCreature.getId());
            if (actor != null) {
                arrowX = actor.getX() + actor.getSpriteWidth() / 2 - 50f;
                arrowY = actor.getY() + actor.getSpriteHeight() + 50f;
                targetFound = true;
            }
        }

        if (targetFound) {
            spriteBatch.draw(frame, arrowX, arrowY, 100, 100);
        }
    }

    private void updateUI() {
        Optional<Creature> selected = presenter.getSelectedCreature();
        if (selected.isPresent()) {
            Creature creature = selected.get();
            String type = creature.getClass().getSimpleName();
            if (creature instanceof Cluster) {
                type = "Cluster (" + ((Cluster) creature).getMembers().size() + " members)";
            }
            selectedZombieInfoLabel.setText(String.format("Selected: %s | Coins: %d", type, creature.getCoins()));
        } else if (presenter.isGameRunning()) {
            selectedZombieInfoLabel.setText("Click on a creature to select it.");
        } else {
            selectedZombieInfoLabel.setText("");
        }
    }

    private void drawGameBackground() {
        OrthographicCamera camera = (OrthographicCamera) gameViewport.getCamera();

        // --- Lógica de Repetição Horizontal (comum a todos os backgrounds) ---
        float tileWidth = backgroundTexture.getWidth();
        float visibleWorldWidth = gameViewport.getWorldWidth() * camera.zoom;
        float centerTileX = MathUtils.floor(camera.position.x / tileWidth) * tileWidth;
        int tilesToEachSide = (int)Math.ceil(visibleWorldWidth / tileWidth) + 1;

        // --- Lógica de Repetição Vertical ---
        float visibleWorldHeight = gameViewport.getWorldHeight() * camera.zoom;
        float cameraBottomY = camera.position.y - visibleWorldHeight / 2f;
        float cameraTopY = camera.position.y + visibleWorldHeight / 2f;

        // 1. Desenha o background principal
        float mainBgHeight = backgroundTexture.getHeight();
        for (int i = -tilesToEachSide; i <= tilesToEachSide; i++) {
            float backgroundX = centerTileX + (i * tileWidth);
            spriteBatch.draw(backgroundTexture, backgroundX, 0, tileWidth, mainBgHeight);
        }

        // 2. Desenha o background SUPERIOR (céu) para preencher o espaço acima
        float upperBgHeight = upperBackgroundTexture.getHeight();

        for (float y = mainBgHeight; y < cameraTopY; y += upperBgHeight) {
            for (int i = -tilesToEachSide; i <= tilesToEachSide; i++) {
                float backgroundX = centerTileX + (i * tileWidth);
                spriteBatch.draw(upperBackgroundTexture, backgroundX, y, tileWidth, upperBgHeight);
            }
        }

        // 3. Desenha o background INFERIOR (chão) para preencher o espaço abaixo
        float lowerBgHeight = lowerBackgroundTexture.getHeight();

        float startY = MathUtils.floor(cameraBottomY / lowerBgHeight) * lowerBgHeight;
        for (float y = startY; y < 0; y += lowerBgHeight) {
            for (int i = -tilesToEachSide; i <= tilesToEachSide; i++) {
                float backgroundX = centerTileX + (i * tileWidth);
                spriteBatch.draw(lowerBackgroundTexture, backgroundX, y, tileWidth, lowerBgHeight);
            }
        }
    }

    private void drawDebugGraphics() {
        shapeRenderer.setProjectionMatrix(gameViewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        if (presenter.areCollidersVisible()) {
            shapeRenderer.setColor(Color.RED);
            zombieActors.values().forEach(actor -> shapeRenderer.rect(actor.getZombieRectangle().x, actor.getZombieRectangle().y, actor.getZombieRectangle().width, actor.getZombieRectangle().height));
            clusterActors.values().forEach(actor -> shapeRenderer.rect(actor.getClusterRectangle().x, actor.getClusterRectangle().y, actor.getClusterRectangle().width, actor.getClusterRectangle().height));
            if (guardianActor != null) {
                shapeRenderer.rect(guardianActor.getGuardianRectangle().x, guardianActor.getGuardianRectangle().y, guardianActor.getGuardianRectangle().width, guardianActor.getGuardianRectangle().height);
            }
        }

        presenter.getSelectedCreature().ifPresent(creature -> {
            shapeRenderer.setColor(Color.CYAN);
            if (creature instanceof Guardian && guardianActor != null) {
                Rectangle rect = guardianActor.getGuardianRectangle();
                shapeRenderer.rect(rect.x - 2, rect.y - 2, rect.width + 4, rect.height + 4);
            } else {
                ZombieActor actor = zombieActors.get(creature.getId());
                if (actor != null) {
                    Rectangle rect = actor.getZombieRectangle();
                    shapeRenderer.rect(rect.x - 2, rect.y - 2, rect.width + 4, rect.height + 4);
                }
            }
        });

        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height, true);
        uiViewport.update(width, height, true);
        messageLabel.setPosition(20, height - 40);
    }

    @Override
    public void synchronizeActors(List<Creature> creatures) {
        List<Integer> creatureIds = creatures.stream().map(Creature::getId).toList();
        zombieActors.keySet().removeIf(id -> !creatureIds.contains(id));
        clusterActors.keySet().removeIf(id -> !creatureIds.contains(id));

        Optional<Creature> guardianModel = creatures.stream().filter(c -> c instanceof Guardian).findFirst();

        if (guardianModel.isPresent()) {
            if (this.guardianActor == null) {
                this.guardianActor = new GuardianActor(guardianModel.get(), floorY);
            } else {
                this.guardianActor.updateData(guardianModel.get());
            }
        } else {
            this.guardianActor = null;
        }

        for (Creature creature : creatures) {
            if (!(creature instanceof Guardian)) {
                if (creature instanceof Cluster) {
                    clusterActors.computeIfAbsent(creature.getId(), id -> new ClusterActor(creature, floorY))
                        .updateData(creature);
                } else {
                    zombieActors.computeIfAbsent(creature.getId(), id -> new ZombieActor(creature, floorY))
                        .updateData(creature);
                }
            }
        }
    }

    @Override
    public void startJumpAnimationFor(Creature creature) {
        switch (creature) {
            case null -> {}
            case Guardian ignored -> {
                if (guardianActor != null) guardianActor.startJump();
            }
            case Cluster ignored -> {
                ClusterActor actor = clusterActors.get(creature.getId());
                if (actor != null) actor.startJump();
            }
            default -> {
                ZombieActor actor = zombieActors.get(creature.getId());
                if (actor != null) actor.startJump();
            }
        }
    }

    @Override
    public void startAttackAnimationFor(Creature creature) {
        if (creature instanceof Cluster) {
            ClusterActor actor = clusterActors.get(creature.getId());
            if (actor != null) {
                actor.startAttack();
            }
        }
    }

    @Override
    public boolean areAnimationsFinished() {
        for (ZombieActor actor : zombieActors.values()) {
            if (!actor.isAnimationFinished()) {
                return false;
            }
        }
        for (ClusterActor actor : clusterActors.values()) {
            if (!actor.isAnimationFinished()) return false;
        }
        return guardianActor == null || guardianActor.isAnimationFinished();
    }

    @Override
    public boolean isAnyActorOffScreen() {
        OrthographicCamera camera = (OrthographicCamera) gameViewport.getCamera();
        Rectangle viewportBounds = new Rectangle(
            camera.position.x - camera.viewportWidth * camera.zoom / 2,
            camera.position.y - camera.viewportHeight * camera.zoom / 2,
            camera.viewportWidth * camera.zoom,
            camera.viewportHeight * camera.zoom
        );

        for (ZombieActor actor : zombieActors.values()) {
            if (!viewportBounds.contains(actor.getZombieRectangle())) {
                return true;
            }
        }

        for (ClusterActor actor : clusterActors.values()) {
            if (!viewportBounds.contains(actor.getClusterRectangle())) {
                return true;
            }
        }

        return guardianActor != null && !viewportBounds.contains(guardianActor.getGuardianRectangle());
    }

    @Override
    public void showGameOver(boolean success, String finalMessage) {
        Gdx.input.setInputProcessor(stage);
        Dialog dialog = new Dialog("Simulation Over", skin) {
            {
                text(finalMessage).setColor(success ? Color.GREEN : Color.YELLOW);
                button("Back to Menu", true);
            }
            @Override
            protected void result(Object object) {
                presenter.onExit();
            }
        };
        dialog.show(stage);
    }

    @Override
    public OrthographicCamera getGameCamera() {
        return (OrthographicCamera) gameViewport.getCamera();
    }

    @Override
    public void setGameCameraZoom(float zoom) {
        getGameCamera().zoom = zoom;
    }

    @Override
    public void showMessage(String message, MessageType type) {
        messageLabel.setText(message);
    }

    @Override
    public void stopMusic() {
        music.stop();
    }

    @Override
    public void dispose() {
        super.dispose();
        backgroundTexture.dispose();
        upperBackgroundTexture.dispose();
        lowerBackgroundTexture.dispose();
        arrowTexture.dispose();
        music.dispose();
        ZombieActor.unloadResources();
        ClusterActor.unloadResources();
        GuardianActor.unloadResources();
    }
}
