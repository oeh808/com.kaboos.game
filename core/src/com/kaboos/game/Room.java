package com.kaboos.game;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.physics.box2d.*;

import java.util.ArrayList;

public class Room {
    TiledMap tiledMap;
    TiledMapRenderer tiledMapRenderer;
    int [] layer1 = new int[2];
    int [] layer2 = new int[1];
    ArrayList<Body> walls = new ArrayList<Body>();
    Body [] boundaries = new Body[4];

    public Room(TiledMap map){
        tiledMap = map;
        MapLayers mapLayers = tiledMap.getLayers();
        layer1[0] = mapLayers.getIndex("Ground");
        layer1[1] = mapLayers.getIndex("Middle");
        layer2[0] = mapLayers.getIndex("Top");
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
    }


    public void createMapObjects(World world) {//Creates the collision objects from the map file
        MapLayer collisionObjectLayer = tiledMap.getLayers().get(2);
        MapObjects objects = collisionObjectLayer.getObjects();

        int i=0;
        for (RectangleMapObject rectangleObject : objects.getByType(RectangleMapObject.class)) {
            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyDef.BodyType.StaticBody;
            bodyDef.position.set((rectangleObject.getRectangle().getX())/ Constants.P2M,
                    (rectangleObject.getRectangle().getY())/ Constants.P2M);
            walls.add(world.createBody(bodyDef));
            PolygonShape shape = new PolygonShape();
            shape.setAsBox((rectangleObject.getRectangle().getWidth()/2)/ Constants.P2M,
                    (rectangleObject.getRectangle().getHeight()/2)/ Constants.P2M,
                    walls.get(i).getPosition().set
                            (rectangleObject.getRectangle().getWidth()/2/Constants.P2M,
                            rectangleObject.getRectangle().getHeight()/2/Constants.P2M),
                    0);
            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.shape = shape;
            fixtureDef.density = 1f;
            fixtureDef.filter.categoryBits= Constants.WALL;
            fixtureDef.filter.maskBits= (short) (Constants.PLAYER | Constants.ENEMIES | Constants.PLAYER_PROJECTILE | Constants.ENEMY_PROJECTILE);
            walls.get(i).createFixture(fixtureDef);
            i++;
            shape.dispose();
        }
    }

    public void renderFirstLayer(){
        tiledMapRenderer.render(layer1);
    }

    public void renderSecondLayer(){
        tiledMapRenderer.render(layer2);
    }

    public void createInvisibleWalls(World world){
        BodyDef [] bodydefs = new BodyDef[4];
        FixtureDef [] fixtureDefs = new FixtureDef[4];
        PolygonShape [] shapes = new PolygonShape[4];
        //This loop places the horizontal walls in the first two cells and the vertical walls in the last two cells
        for (int i=0;i<bodydefs.length/2;i++){
            bodydefs[i] = new BodyDef();
            bodydefs[i+2] = new BodyDef();
            bodydefs[i].type = BodyDef.BodyType.StaticBody;
            bodydefs[i+2].type = BodyDef.BodyType.StaticBody;
            bodydefs[i].position.set(160/2/ Constants.P2M, 176/2*i/ Constants.P2M);//(Horizontal)Positions one wall at y=0 and the other at y=176
            bodydefs[i+2].position.set(160/2*i/ Constants.P2M, 176/2/ Constants.P2M);//(Vertical)Positions on wall at x=0 and the other at x=160
            boundaries[i] = world.createBody(bodydefs[i]);
            boundaries[i+2] = world.createBody(bodydefs[i+2]);
            shapes[i] = new PolygonShape();
            shapes[i+2] = new PolygonShape();
            //Need to take a look at this method
            shapes[i].setAsBox(160/Constants.P2M,0.1f/Constants.P2M,
                    boundaries[i].getPosition().set(160/2/ Constants.P2M,176/2*i/Constants.P2M),0);
            shapes[i+2].setAsBox(0.1f/Constants.P2M,176/Constants.P2M,
                    boundaries[i+2].getPosition().set(160/2*i/ Constants.P2M,176/2/ Constants.P2M),0);
        }
        for (int i=0; i<fixtureDefs.length;i++){
            fixtureDefs[i] = new FixtureDef();
            fixtureDefs[i].shape = shapes[i];
            fixtureDefs[i].density = 1f;
            fixtureDefs[i].filter.categoryBits = Constants.WALL;
            fixtureDefs[i].filter.maskBits = (short) (Constants.PLAYER | Constants.ENEMIES | Constants.PLAYER_PROJECTILE | Constants.ENEMY_PROJECTILE);
            boundaries[i].createFixture(fixtureDefs[i]);
        }
    }
}
