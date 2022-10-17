package com.kaboos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

import java.util.ArrayList;

public abstract class Enemy {
    protected Integer health;
    protected Integer MaxHealth;
    private final ShapeRenderer healthBar = new ShapeRenderer();
    public boolean destroyed = false;
    public Sprite sprite;
    public Body body;
    private final TextureRegion[] framesIdleRight = new TextureRegion[4];
    private final TextureRegion[] framesIdleLeft = new TextureRegion[4];
    private final TextureRegion[] framesRunLeft = new TextureRegion[4];
    private final TextureRegion[] framesRunRight = new TextureRegion[4];
    public Animation idleAnimation,idleAnimationLeft,idleAnimationRight;
    public Animation runAnimation,runAnimationLeft,runAnimationRight;
    protected int attackSpeed;
    public int lastAttack = 0;
    private boolean isMoving = false;
    protected CharacterTile tile;
    protected Player target;
    protected Tile subTarget;
    public int deathTimer = 5;

    private final String enemyType;

    protected GraphPath<Tile> path = new DefaultGraphPath<>();
    protected Pathfinding pathFinder;
    protected World world;
    protected Camera camera;

    protected float attackW;
    protected float attackH;
    public int attackDamage;
    protected Sound attackSound;

    protected Vector2 distanceToPlayer;
    protected boolean isColliding=false;

    protected float movementSpeed;
    protected float baseMovementSpeed;
    private boolean isSlowed = false;
    private int slowDuration = 0;

    public ArrayList<Body> attackProjectiles = new ArrayList<Body>();

    public ArrayList<Body> toDestroy = new ArrayList<>();

    public Enemy(Player player, Room room, World world,Camera camera, String enemyType,int[] spawnPoint){
        this.world = world;
        this.camera = camera;

        healthBar.setAutoShapeType(true);
        this.enemyType = enemyType;
        sprite = new Sprite(new Texture("Enemy/"+enemyType+"_idle_anim_f0.png"));

        sprite.setPosition(spawnPoint[0],spawnPoint[1]);

        settingAnimations();

        target = player;
        pathFinder = new Pathfinding(room);
        subTarget = pathFinder.getGraph().getTile(target.getTile().getX(),target.getTile().getY());
        createBody();
    }

    public void settingAnimations(){
        for (int i=0;i<4;i++){//creating idle animation frames
            framesIdleRight[i] = new TextureRegion(new Texture(Gdx.files.internal("Enemy/"+enemyType+"_idle_anim_f"+i+".png")));
            framesIdleLeft[i] = new TextureRegion(new Texture(Gdx.files.internal("Enemy/"+enemyType+"_idle_anim_f"+i+".png")));
            framesIdleLeft[i].flip(true,false);
        }
        //frames are cast to Object[] to suppress the non-varargs call warning
        idleAnimationLeft = new Animation(1f/8f, (Object[]) framesIdleLeft);
        idleAnimationRight = new Animation(1f/8f, (Object[]) framesIdleRight);
        idleAnimation = idleAnimationRight;

        for (int i=0;i<4;i++){//creating running animation frames
            framesRunRight[i] = new TextureRegion(new Texture(Gdx.files.internal("Enemy/"+enemyType+"_run_anim_f"+i+".png")));
            framesRunLeft[i] = new TextureRegion(new Texture(Gdx.files.internal("Enemy/"+enemyType+"_run_anim_f"+i+".png")));
            framesRunLeft[i].flip(true,false);
        }
        runAnimationLeft = new Animation(1f/8f, (Object[]) framesRunLeft);
        runAnimationRight = new Animation(1f/8f, (Object[]) framesRunRight);
        runAnimation = runAnimationRight;
    }

    public void createBody(){
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set((sprite.getX())/ Constants.P2M,
                (sprite.getY())/ Constants.P2M);
        bodyDef.fixedRotation = true;
        body = world.createBody(bodyDef);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(sprite.getWidth()/4 / Constants.P2M,
                sprite.getHeight()/5 / Constants.P2M,
                body.getPosition().set(sprite.getWidth()/2/Constants.P2M,sprite.getHeight()/3/Constants.P2M),
                0);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.filter.categoryBits= Constants.ENEMIES;
        fixtureDef.filter.maskBits= (short) (Constants.WALL | Constants.PLAYER | Constants.PLAYER_PROJECTILE);
        body.createFixture(fixtureDef);
        body.setLinearDamping(5f);
        shape.dispose();

        tile = new CharacterTile(body,sprite);
        path = pathFinder.getPath(pathFinder.getGraph().getTile(tile.getX(), tile.getY()),subTarget);
    }

    public void takeDamage(int num){
        if (this.health-num<=0) {
            health = 0;
            //death
            toDestroy.add(body);
            destroyed=true;
        }else{
            this.health = health - num;
        }
    }

    public void healthBar(){
        //Health is normalized for creating the health bar for ease of control and coding
        float normalizedHealth = (float)health/MaxHealth;
        healthBar.setProjectionMatrix(camera.combined);
        healthBar.begin(ShapeRenderer.ShapeType.Filled);
        healthBar.rect(sprite.getX(),sprite.getY()+sprite.getHeight()-4,16*normalizedHealth,4,
                Color.RED,Color.RED,Color.RED,Color.RED);
        healthBar.end();
    }

    public void drawSprite(Kaboos game, float elapsedTime, float delta){
        if (!destroyed){
            if (isSlowed()){
                game.batch.setColor(com.badlogic.gdx.graphics.Color.BLUE);
            }
            if (isMoving()){
                game.batch.draw((TextureRegion) runAnimation.getKeyFrame(elapsedTime,true), sprite.getX(), sprite.getY());
            }else{
                game.batch.draw((TextureRegion) idleAnimation.getKeyFrame(elapsedTime,true), sprite.getX(), sprite.getY());
            }
            update(delta);
            game.batch.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        }
    }

    public void attackSound(){
        if (Constants.soundOn){
            attackSound.play(Constants.soundVolume);
        }
    }

    public abstract void attack();

    public abstract void drawProjectiles(Kaboos game, float elapsedTime);

    public abstract void settingAttackAnimation();

    public abstract void followPath(float delta);

    public void setPath(GraphPath<Tile> path){
        this.path = path;
    }

    public Vector2 getPosition() {
        return path.get(0).getPosition();
    }

    public void updatePathFinding(float delta){
        tile.setTile(body);
        subTarget = pathFinder.getGraph().getTile(target.getTile().getX(),target.getTile().getY());
        path = pathFinder.getPath(pathFinder.getGraph().getTile(tile.getX(), tile.getY()),subTarget);
        followPath(delta);
    }

    public void update(float delta){
        sprite.setPosition(body.getPosition().x * Constants.P2M, body.getPosition().y * Constants.P2M);
        Vector2 origin = new Vector2(sprite.getX()+sprite.getWidth()/2,sprite.getY()+sprite.getHeight()/2 - attackW /4);
        Vector2 goal = new Vector2(target.sprite.getX()+sprite.getWidth()/2,target.sprite.getY()+sprite.getHeight()/2);
        distanceToPlayer = goal.sub(origin);

        if(lastAttack>0){
            lastAttack--;
        }
        if (slowDuration>0){
            slowDuration--;
        }else{
            movementSpeed = baseMovementSpeed;
            isSlowed = false;
        }
        updatePathFinding(delta);

        if (Math.abs(body.getLinearVelocity().x)>10/Constants.P2M || Math.abs(body.getLinearVelocity().y)>10/Constants.P2M){
            isMoving=true;
            if (body.getLinearVelocity().x>0){
                idleAnimation = idleAnimationRight;
                runAnimation = runAnimationRight;
            }else if (body.getLinearVelocity().x<0){
                idleAnimation = idleAnimationLeft;
                runAnimation = runAnimationLeft;
            }
        }else{
            isMoving=false;
        }
    }

    public boolean isMoving(){
        return isMoving;
    }

    public void setColliding(Boolean b){
        isColliding=b;
    }

    public void setSlowed(int maxSlowDuration){
        isSlowed = true;
        slowDuration = maxSlowDuration;
        movementSpeed = movementSpeed * 0.6f;
    }

    public boolean isSlowed(){
        return isSlowed;
    }

    public CharacterTile getTile(){
        return tile;
    }
}
