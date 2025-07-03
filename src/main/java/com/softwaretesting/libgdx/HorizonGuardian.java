package com.softwaretesting.libgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.softwaretesting.simulation.entity.Creature;
import com.softwaretesting.simulation.entity.Guardian;
import com.softwaretesting.simulation.entity.ParabolicMovement;

import java.util.Arrays;

public class HorizonGuardian extends Zombie{

    // animações do guardião
    private static Animation<TextureRegion> attacking;
    private static Animation<TextureRegion> jumpingUp;
    private static Animation<TextureRegion> jumpingDown;
    private static Animation<TextureRegion> landing;

    // audio
    private static Sound attackSound;

    /**
     * Cria um zumbi associado a uma criatura.
     * A posição inicial do sprite é baseada na posição da criatura.
     *
     * @param creature A criatura associada a este zumbi.
     */
    public HorizonGuardian(Guardian creature) {
        super(creature);
    }

    /**
     * Verifica se a base do retângulo de colisão deste guardião toca o centro da
     * base do retângulo de um zumbi.
     *
     * @param zombie O zumbi para checar a colisão.
     * @return true se houver colisão, false caso contrário.
     */
    public boolean collidesWith(Zombie zombie) {
        Rectangle thisRect = this.getRectangle();
        Rectangle otherRect = zombie.getRectangle();

        // Ponto central da base do outro guardião
        Vector2 otherBaseCenter = new Vector2(
                otherRect.x + otherRect.width / 2,
                otherRect.y
        );

        // Checa se o ponto central da base do zumbi está dentro do retângulo deste guardião
        return thisRect.contains(otherBaseCenter);
    }

    public static void loadResources(String spritesheetPath, String audioPath, BitmapFont font) {
        if (HorizonGuardian.spriteSheet != null) {
            HorizonGuardian.spriteSheet.dispose();
        }

        HorizonGuardian.spriteSheet = new Texture(Gdx.files.internal(spritesheetPath));

        TextureRegion[][] keyframes = TextureRegion.split(spriteSheet,
                spriteSheet.getWidth() / 6, spriteSheet.getHeight() / 3);

        // cada keyframe tem tamanho 8, mas a maioria das animações tem menos que 8 frames
        TextureRegion[] attackingFrames = Arrays.copyOfRange(keyframes[0], 0, 5); // 4 frames
        TextureRegion[] jumpingUpFrames = Arrays.copyOfRange(keyframes[1], 0, 3); // 4 frames de subida
        TextureRegion[] jumpingDownFrames = Arrays.copyOfRange(keyframes[1], 3, 5); // 1 frames de descida
        TextureRegion[] landingFrames = Arrays.copyOfRange(keyframes[1], 5, 6); // 3 frames de aterrissagem

        attacking = new Animation<>(0.15f, attackingFrames);
        jumpingUp = new Animation<>(0.1f, jumpingUpFrames);
        jumpingDown = new Animation<>(0.1f, jumpingDownFrames);
        landing = new Animation<>(0.1f, landingFrames);
        attackSound = Gdx.audio.newSound(Gdx.files.internal(audioPath));
    }
}
