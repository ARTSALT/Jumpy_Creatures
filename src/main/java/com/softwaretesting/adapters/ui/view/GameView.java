package com.softwaretesting.adapters.ui.view;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.softwaretesting.core.domain.model.Creature;

import java.util.List;

public interface GameView extends View {

    void synchronizeActors(List<Creature> creatures);

    /**
     * Inicia a animação de pulo para a criatura ativa especificada.
     * @param creature A criatura que deve começar a pular.
     */
    void startJumpAnimationFor(Creature creature);

    /**
     * Verifica se TODAS as animações de pulo em andamento terminaram.
     * @return true se todos os atores estiverem no estado IDLE.
     */
    boolean areAnimationsFinished();

    boolean isAnyActorOffScreen();

    void showGameOver(boolean success, String finalMessage);

    OrthographicCamera getGameCamera();

    void setGameCameraZoom(float zoom);

    void stopMusic();
}
