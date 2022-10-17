package com.kaboos.game;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.*;

public abstract class Item {
    protected Sprite sprite;
    protected Body body;
    protected TextureRegion itemTexture;
    protected World world;
    protected Camera camera;
    protected String itemType;
    protected int[] spawnPoint;

    public Item(World world, Camera camera, String itemType, int[] spawnPoint){
        this.world = world;
        this.camera = camera;
        this.itemType = itemType;
        this.spawnPoint = spawnPoint;

        sprite = new Sprite(new Texture("Items/"+ itemType +".png"));
        sprite.setPosition(spawnPoint[0], spawnPoint[1]);

        createBody();
        settingTexture();
    }

    public void createBody(){
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set((sprite.getX())/ Constants.P2M,
                (sprite.getY())/ Constants.P2M);
        bodyDef.fixedRotation = true;
        body = world.createBody(bodyDef);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(sprite.getWidth()/3 / Constants.P2M,
                sprite.getHeight()/4 / Constants.P2M,
                body.getPosition().set(sprite.getWidth()/2/Constants.P2M,sprite.getHeight()/3/Constants.P2M),
                0);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.filter.categoryBits= Constants.ITEM;
        fixtureDef.filter.maskBits= Constants.PLAYER;
        body.createFixture(fixtureDef);
        shape.dispose();
    }

    public void drawSprite(Kaboos game){
        game.batch.draw(itemTexture,sprite.getX(),sprite.getY());
    }

    public void settingTexture(){
        itemTexture = new TextureRegion(new Texture("Items/"+ itemType +".png"));
    }

    public abstract void itemEffect(Player player);
}
