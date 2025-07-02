package com.softwaretesting.adapters.ui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.softwaretesting.adapters.ui.LibGdxApplication;
import com.softwaretesting.adapters.ui.view.View;

/**
 * Classe base para todas as telas da aplicação.
 * Implementa a interface View e define os métodos comuns para renderizar, redimensionar a tela,
 * exibir mensagens e liberar recursos.
 */
public class ScreenTemplate implements Screen, View {

    // atributos comuns para todas as telas
    protected LibGdxApplication application;    // referência à aplicação principal
    protected SpriteBatch spriteBatch;          // batch para renderização de sprites
    protected ShapeRenderer shapeRenderer;      // renderer para formas geométricas
    protected Skin skin;                        // skin para estilização dos widgets
    protected BitmapFont font;                  // fonte para renderização de texto
    protected Stage stage;                      // stage para gerenciar os atores da UI
    protected Table table;                      // tabela para organizar os widgets
    protected Label messageLabel;               // rótulo para exibir feedback ao usuário

    /**
     * Construtor padrão que inicializa os componentes comuns.
     */
    public ScreenTemplate(LibGdxApplication application) {
        this.application = application;
        this.spriteBatch = application.getSpriteBatch();
        this.shapeRenderer = application.getShapeRenderer();
        this.skin = application.getSkin();
        this.font = application.getFont();

        // inicializa o stage e o rótulo de mensagem
        stage = new Stage(new ScreenViewport());
        messageLabel = new Label("", skin, "font", Color.WHITE);

        // cria a stage para a UI
        Gdx.input.setInputProcessor(stage);
        table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        stage.addActor(messageLabel);
    }

    @Override
    public void showMessage(String message, MessageType type) {
        messageLabel.setText(message);
        switch (type) {
            case INFO:
                messageLabel.setColor(Color.WHITE);
                break;
            case ERROR:
                messageLabel.setColor(Color.RED);
                break;
            case WARNING:
                messageLabel.setColor(Color.YELLOW);
                break;
            case SUCCESS:
                messageLabel.setColor(Color.GREEN);
                break;
        }
    }

    @Override
    public void disposeScreen() {
        dispose();
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        // limpa a tela
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // atualiza e desenha a UI
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}
}
