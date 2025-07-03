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


}
