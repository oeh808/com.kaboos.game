package com.kaboos.game;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.physics.box2d.Body;

public class CharacterTile {
    private int x;
    private int y;
    private Sprite sprite;

    public CharacterTile(Body body, Sprite sprite){
        this.sprite = sprite;
        setTile(body);
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    public void setTile(Body body){
        float xTemp = body.getPosition().x + (sprite.getWidth()/2f/Constants.P2M);
        float yTemp = body.getPosition().y + (sprite.getHeight()/12f/Constants.P2M);

        //Every tile is 16 x 16 so this will return the tile number
        x = (int) (xTemp/16f * Constants.P2M);
        y = (int) (yTemp/16f * Constants.P2M);
    }
}
