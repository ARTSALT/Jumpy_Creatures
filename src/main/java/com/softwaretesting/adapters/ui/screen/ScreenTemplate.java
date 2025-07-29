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
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
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
    protected TextButton rankingButton;         // botão para acessar o ranking
    protected TextButton logoutButton;          // botão para logout

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
        table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        stage.addActor(messageLabel);
    }

    /**
     * Cria o cabeçalho da tela com o título e os botões de navegação.
     * @param titleText Texto do título a ser exibido no cabeçalho.
     * @return Uma tabela contendo o cabeçalho configurado.
     */
    protected Table createHeader(String titleText) {
        Table header = new Table();
        header.setBackground(skin.getDrawable("checkbox"));

        Label title = new Label(titleText, skin, "title", Color.WHITE);

        rankingButton = new TextButton("Ranking", skin);
        logoutButton = new TextButton("Logout", skin);

        Table buttonGroup = new Table();
        buttonGroup.add(rankingButton).padRight(10);
        buttonGroup.add(logoutButton);

        header.add(title).expandX().align(Align.left).pad(15);
        header.add(buttonGroup).align(Align.right).pad(15);

        return header;
    }

    /**
     * Exibe uma mensagem na tela com o tipo especificado.
     * @param message A mensagem a ser exibida.
     * @param type O tipo da mensagem (INFO, ERROR, WARNING, SUCCESS).
     */
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
    public void show() { Gdx.input.setInputProcessor(stage); }

    /**
     * Renderiza a tela, atualizando o estado da UI e desenhando os componentes.
     * Este metodo é chamado a cada frame pelo LibGDX.
     * @param delta Tempo desde o último frame, usado para animações e atualizações.
     */
    @Override
    public void render(float delta) {
        // limpa a tela
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // atualiza e desenha a UI
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    /**
     * Redimensiona a tela quando a janela é redimensionada.
     * Atualiza o viewport do stage para manter a proporção correta.
     * @param width Nova largura da tela.
     * @param height Nova altura da tela.
     */
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);

        if (table != null) {
            table.invalidateHierarchy();
        }
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
