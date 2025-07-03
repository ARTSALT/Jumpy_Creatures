package com.softwaretesting.libgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.softwaretesting.simulation.entity.Creature;

import java.util.Arrays;

public class ClusterActor extends Zombie{

    // animações do guardião
    private static Animation<TextureRegion> attacking;
    private static Animation<TextureRegion> jumpingUp;
    private static Animation<TextureRegion> jumpingDown;
    private static Animation<TextureRegion> landing;

    // audio
    private static Sound attackSound;

    /**
     * Cria um cluster associado a uma criatura.
     * A posição inicial do sprite é baseada na posição da criatura.
     *
     * @param creature A criatura associada a este cluster.
     */
    public ClusterActor(Creature creature) {
        super(creature);
    }

    public static void loadResources(String spritesheetPath, String audioPath) {
        if (ClusterActor.spriteSheet != null) {
            ClusterActor.spriteSheet.dispose();
        }

        ClusterActor.spriteSheet = new Texture(Gdx.files.internal(spritesheetPath));

        TextureRegion[][] keyframes = TextureRegion.split(spriteSheet,
                spriteSheet.getWidth() / 11, spriteSheet.getHeight() / 2);

        // cada keyframe tem tamanho 8, mas a maioria das animações tem menos que 8 frames
        TextureRegion[] attackingFrames = Arrays.copyOfRange(keyframes[0], 0, 6); // 4 frames
        TextureRegion[] jumpingUpFrames = Arrays.copyOfRange(keyframes[1], 0, 7); // 4 frames de subida
        TextureRegion[] jumpingDownFrames = Arrays.copyOfRange(keyframes[1], 7, 8); // 1 frames de descida
        TextureRegion[] landingFrames = Arrays.copyOfRange(keyframes[1], 8, 11); // 3 frames de aterrissagem

        attacking = new Animation<>(0.15f, attackingFrames);
        jumpingUp = new Animation<>(0.1f, jumpingUpFrames);
        jumpingDown = new Animation<>(0.1f, jumpingDownFrames);
        landing = new Animation<>(0.1f, landingFrames);
        attackSound = Gdx.audio.newSound(Gdx.files.internal(audioPath));
    }
}
