package com.softwaretesting.adapters.ui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
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
import com.softwaretesting.adapters.ui.actor.GuardianActor;
import com.softwaretesting.adapters.ui.actor.ZombieActor;
import com.softwaretesting.adapters.ui.presenter.GamePresenter;
import com.softwaretesting.adapters.ui.view.GameView;
import com.softwaretesting.core.domain.model.Creature;
import com.softwaretesting.core.domain.model.Guardian;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GameScreen extends ScreenTemplate implements Screen, GameView {

    private final GamePresenter presenter;
    private final Map<Integer, ZombieActor> zombieActors = new HashMap<>();
    private final Map<Integer, GuardianActor> guardianActors = new HashMap<>();

    private final Viewport gameViewport;
    private final Viewport uiViewport;

    private final Table setupTable;
    private final Label selectedZombieInfoLabel;

    private final Texture backgroundTexture;
    private final Music music;
    private final float floorY;

    public GameScreen(LibGdxApplication application) {
        super(application);
        this.presenter = new GamePresenter(this, application);

        gameViewport = new ExtendViewport(1920, 1080);
        uiViewport = new ScreenViewport();
        stage.setViewport(uiViewport);

        this.floorY = 280.f;

        backgroundTexture = new Texture(Gdx.files.internal("images/background.png"));
        music = Gdx.audio.newMusic(Gdx.files.internal("audio/graveyard_trap.mp3"));
        music.setLooping(true);
        music.setVolume(0.5f);
        if (!music.isPlaying()) music.play();

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
    }

    private void createListeners(TextButton playButton, TextField nameInput, TextField numInput) {
        playButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                presenter.onPlayClicked(nameInput.getText(), numInput.getText());
                if (presenter.isGameRunning()) {
                    setupTable.setVisible(false);
                }
            }
        });

        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                switch (keycode) {
                    case Input.Keys.ESCAPE:
                        presenter.onExit();
                        return true;
                    case Input.Keys.C:
                        presenter.onToggleColliders();
                        return true;
                    case Input.Keys.LEFT:
                        presenter.onManualZoom(0.02f);
                        return true;
                    case Input.Keys.RIGHT:
                        presenter.onManualZoom(-0.02f);
                        return true;
                }
                return false;
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (!presenter.isGameRunning()) return false;

                if (button == Input.Buttons.LEFT) {
                    Vector3 worldCoords = gameViewport.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));

                    Optional<ZombieActor> clickedActor = zombieActors.values().stream()
                        .filter(actor -> actor.getZombieRectangle().contains(worldCoords.x, worldCoords.y))
                        .findFirst();

                    if (clickedActor.isPresent()) {
                        presenter.onZombieSelected(clickedActor.get().getId());
                    } else {
                        presenter.onBackgroundClicked();
                    }
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void render(float deltaTime) {
        ScreenUtils.clear(Color.BLACK);

        presenter.onUpdate(deltaTime);
        zombieActors.values().forEach(actor -> actor.update(deltaTime));
        guardianActors.values().forEach(actor -> actor.update(deltaTime));

        gameViewport.apply();
        spriteBatch.setProjectionMatrix(gameViewport.getCamera().combined);
        spriteBatch.begin();
        drawGameBackground();
        zombieActors.values().forEach(actor -> actor.draw(spriteBatch));
        guardianActors.values().forEach(actor -> actor.draw(spriteBatch));
        spriteBatch.end();

        if (presenter.areCollidersVisible() || presenter.getSelectedCreature().isPresent()) {
            drawDebugGraphics();
        }

        uiViewport.apply();
        stage.act(deltaTime);
        updateUI();
        stage.draw();
    }

    private void updateUI() {
        Optional<Creature> selected = presenter.getSelectedCreature();
        if (selected.isPresent()) {
            Creature creature = selected.get();
            selectedZombieInfoLabel.setText(String.format("Selected: %s | Coins: %d", creature.getClass().getSimpleName(), creature.getCoins()));
        } else if (presenter.isGameRunning()) {
            selectedZombieInfoLabel.setText("Click on a zombie to select it.");
        } else {
            selectedZombieInfoLabel.setText("");
        }
    }

    private void drawGameBackground() {
        OrthographicCamera camera = (OrthographicCamera) gameViewport.getCamera();
        float tileWidth = backgroundTexture.getWidth();
        float visibleWorldWidth = gameViewport.getWorldWidth() * camera.zoom;

        float centerTileX = MathUtils.floor(camera.position.x / tileWidth) * tileWidth;
        int tilesToEachSide = (int)Math.ceil(visibleWorldWidth / tileWidth) + 1;

        for (int i = -tilesToEachSide; i <= tilesToEachSide; i++) {
            float backgroundX = centerTileX + (i * tileWidth);
            spriteBatch.draw(backgroundTexture, backgroundX, 0, tileWidth, gameViewport.getWorldHeight());
        }
    }

    private void drawDebugGraphics() {
        shapeRenderer.setProjectionMatrix(gameViewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        if (presenter.areCollidersVisible()) {
            shapeRenderer.setColor(Color.RED);
            for (ZombieActor actor : zombieActors.values()) {
                shapeRenderer.rect(actor.getZombieRectangle().x, actor.getZombieRectangle().y, actor.getZombieRectangle().width, actor.getZombieRectangle().height);
            }
        }

        presenter.getSelectedCreature().ifPresent(creature -> {
            ZombieActor actor = zombieActors.get(creature.getId());
            if (actor != null) {
                shapeRenderer.setColor(Color.CYAN);
                Rectangle rect = actor.getZombieRectangle();
                shapeRenderer.rect(rect.x - 2, rect.y - 2, rect.width + 4, rect.height + 4);
            }
        });

        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height, true);
        uiViewport.update(width, height, true);
    }

    @Override
    public void synchronizeActors(List<Creature> creatures) {
        zombieActors.clear();
        guardianActors.clear();

        for (Creature creature : creatures) {
            if (creature instanceof Guardian) {
                guardianActors.computeIfAbsent(creature.getId(), id -> new GuardianActor(creature, floorY));
            } else {
                zombieActors.computeIfAbsent(creature.getId(), id -> new ZombieActor(creature, floorY))
                    .updateData(creature);
            }
        }
    }

    @Override
    public void startJumpAnimationFor(int creatureId) {
        ZombieActor actor = zombieActors.get(creatureId);
        if (actor != null) {
            actor.startJump();
        }
    }

    @Override
    public boolean isActorAnimationFinished(int creatureId) {
        ZombieActor actor = zombieActors.get(creatureId);
        if (actor != null) {
            return actor.isAnimationFinished();
        }
        return true;
    }

    /**
     * Verifica se algum ator visual está fora da câmera
     */
    @Override
    public boolean isAnyActorOffScreen() {
        OrthographicCamera camera = (OrthographicCamera) gameViewport.getCamera();
        // Define os limites visíveis da câmera
        Rectangle viewportBounds = new Rectangle(
            camera.position.x - camera.viewportWidth * camera.zoom / 2,
            camera.position.y - camera.viewportHeight * camera.zoom / 2,
            camera.viewportWidth * camera.zoom,
            camera.viewportHeight * camera.zoom
        );

        // Verifica cada ZombieActor
        for (ZombieActor actor : zombieActors.values()) {
            if (!viewportBounds.overlaps(actor.getZombieRectangle())) {
                return true; // Encontrou um ator fora da tela
            }
        }
        return false; // Todos os atores estão dentro da tela
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
    public void dispose() {
        super.dispose();
        backgroundTexture.dispose();
        music.dispose();
        ZombieActor.unloadResources();
        GuardianActor.unloadResources();
    }
}
