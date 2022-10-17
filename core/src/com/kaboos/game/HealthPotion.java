package com.kaboos.game;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.physics.box2d.World;

public class HealthPotion extends Item{

    public HealthPotion(World world, Camera camera, int[] spawnPoint){
        super(world,camera,"flask_red",spawnPoint);
    }

    @Override
    public void itemEffect(Player player) {
        if (Constants.soundOn){
            Constants.potionSound.play(Constants.soundVolume);
        }
        player.heal((int) (player.maxHealth * 0.5f));
    }
}
