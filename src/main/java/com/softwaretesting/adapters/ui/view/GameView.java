package com.softwaretesting.adapters.ui.view;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.softwaretesting.core.domain.model.Creature;

import java.util.List;

public interface GameView extends View {

    void synchronizeActors(List<Creature> creatures);

    void startJumpAnimationFor(int creatureId);

    boolean isActorAnimationFinished(int creatureId);

    boolean isAnyActorOffScreen();

    void showGameOver(boolean success, String finalMessage);

    OrthographicCamera getGameCamera();

    void setGameCameraZoom(float zoom);
}
