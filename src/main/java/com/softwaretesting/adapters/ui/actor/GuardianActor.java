package com.softwaretesting.adapters.ui.actor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.softwaretesting.core.domain.model.Creature;

public class GuardianActor {

    private static Texture guardianTexture;
    private Sprite sprite;

    public GuardianActor(Creature creature, float floorY) {
        if (guardianTexture == null) {
            throw new IllegalStateException("Guardian resources not loaded. Call GuardianActor.loadResources() first.");
        }

        // split 5x3 texture into 15 frames and get first for the sprite
        this.sprite = new Sprite(guardianTexture, 0, 10, 86, 86);
        this.sprite.setSize(300, 300);
        this.sprite.setPosition((float) creature.getPosition(), floorY + 20);
    }

    public void update(float deltaTime) {

    }

    public void draw(SpriteBatch spriteBatch) {
        sprite.draw(spriteBatch);
    }

    public static void loadResources(String texturePath) {
        if (guardianTexture == null) {
            guardianTexture = new Texture(Gdx.files.internal(texturePath));
        }
    }

    public static void unloadResources() {
        if (guardianTexture != null) {
            guardianTexture.dispose();
            guardianTexture = null;
        }
    }
}
