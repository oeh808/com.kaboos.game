package com.kaboos.game;

import com.badlogic.gdx.*;
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

public abstract class Player implements InputProcessor {
    protected TextureRegion[] framesIdleRight = new TextureRegion[4];
    protected TextureRegion[] framesIdleLeft = new TextureRegion[4];
    protected TextureRegion[] framesRunLeft = new TextureRegion[4];
    protected TextureRegion[] framesRunRight = new TextureRegion[4];
    protected TextureRegion[][] tempFrames;
    protected Animation idleAnimation,idleAnimationRight,idleAnimationLeft;
    protected Animation runAnimation,runAnimationRight,runAnimationLeft;

    protected int health;
    protected int maxHealth;
    protected int lives;
    public Texture fullHeart,emptyHeart;

    protected int abilityCooldown = 0;
    protected int maxAbilityCooldown = 100;

    public Sprite sprite;
    public String playerType;

    public ArrayList<Body> attackProjectiles = new ArrayList<>();
    public ArrayList<Body> abilityProjectiles = new ArrayList<>();

    private final ShapeRenderer healthBar = new ShapeRenderer();
    private final ShapeRenderer resourceBar = new ShapeRenderer();

    public Body body;
    protected World world;
    protected Camera camera;
    final protected int attackSpeed = 4;
    public int lastAttack=0;
    protected CharacterTile tile;

    protected float attackW;
    protected float attackH;

    protected int attackDamage;
    protected int abilityDamage; //Knight ability does not deal damage

    protected Sound attackSound;
    protected Sound abilitySound;

    protected Boolean movementModifierActive = false;

    public ArrayList<Body> toDestroy = new ArrayList<>();

   public Player(World world, Camera camera, String playerType, int[] spawnPoint) {
       //Initializing health
       health = 100;
       maxHealth = 100;
       fullHeart = new Texture("ui_heart_full.png");
       emptyHeart = new Texture("ui_heart_empty.png");

       lives = 3;

       this.playerType = playerType;

       sprite = new Sprite(new Texture("Player/"+ playerType +"_m_idle_anim_f1.png"));
       sprite.setPosition(spawnPoint[0], spawnPoint[1]);
       this.world = world;
       this.camera = camera;
       createBody();
       settingMovementAnimations();
       Gdx.input.setInputProcessor(this);
    }

    public void movement(){//This method controls the outcome of player movement and sets attack direction as well
        if(Gdx.input.isKeyPressed(Input.Keys.A)){
            body.setLinearVelocity(-50/Constants.P2M,body.getLinearVelocity().y);
            runAnimation = runAnimationLeft;
            idleAnimation = idleAnimationLeft;
        }else if(Gdx.input.isKeyPressed(Input.Keys.D)){
            body.setLinearVelocity(50/Constants.P2M,body.getLinearVelocity().y);
            runAnimation = runAnimationRight;
            idleAnimation = idleAnimationRight;
        }else{
            if (!movementModifierActive){
                body.setLinearVelocity(0,body.getLinearVelocity().y);
            }
        }

        if(Gdx.input.isKeyPressed(Input.Keys.W)){
            body.setLinearVelocity(body.getLinearVelocity().x,55/Constants.P2M);
        }else if(Gdx.input.isKeyPressed(Input.Keys.S)){
            body.setLinearVelocity(body.getLinearVelocity().x,-55/Constants.P2M);
        }else{
            if (!movementModifierActive){
                body.setLinearVelocity(body.getLinearVelocity().x,0);
            }
        }
    }

    //Creates the player's body given a world
    public void createBody(){
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
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
        fixtureDef.filter.categoryBits= Constants.PLAYER;
        fixtureDef.filter.maskBits= (short) (Constants.WALL|Constants.ENEMIES|Constants.ENEMY_PROJECTILE|Constants.ITEM);
        body.createFixture(fixtureDef);
        shape.dispose();

        tile = new CharacterTile(body,sprite);
    }

    public abstract void attack();

   public abstract void ability();

   public abstract void drawProjectiles(Kaboos game, float elapsedTime);

    public abstract void settingAttackAnimations();

    public void settingMovementAnimations(){
        for (int i=0;i<4;i++){//creating idle animation frames
            framesIdleRight[i] = new TextureRegion(new Texture(Gdx.files.internal("Player/"+playerType+"_m_idle_anim_f"+i+".png")));
            framesIdleLeft[i] = new TextureRegion(new Texture(Gdx.files.internal("Player/"+playerType+"_m_idle_anim_f"+i+".png")));
            framesIdleLeft[i].flip(true,false);
        }
        idleAnimationRight = new Animation(1f/8f, framesIdleRight);
        idleAnimationLeft = new Animation(1f/8f, framesIdleLeft);
        idleAnimation = idleAnimationRight;

        for (int i=0;i<4;i++){//creating running animation frames
            framesRunRight[i] = new TextureRegion(new Texture(Gdx.files.internal("Player/"+playerType+"_m_run_anim_f"+i+".png")));
            framesRunLeft[i] = new TextureRegion(new Texture(Gdx.files.internal("Player/"+playerType+"_m_run_anim_f"+i+".png")));
            framesRunLeft[i].flip(true,false);
        }
        runAnimationRight = new Animation(1f/8f,framesRunRight);
        runAnimationLeft = new Animation(1f/8f,framesRunLeft);
        runAnimation = runAnimationRight;
    }
    public void drawSprite(Kaboos game, float elapsedTime){
        if (!body.getLinearVelocity().isZero()){
            game.batch.draw((TextureRegion) getRun().getKeyFrame(elapsedTime,true), sprite.getX(), sprite.getY());
        }else{
            game.batch.draw((TextureRegion) getIdle().getKeyFrame(elapsedTime,true), sprite.getX(), sprite.getY());
        }
    }

    public Animation getIdle(){
       return idleAnimation;
    }

    public Animation getRun(){
       return runAnimation;
    }

    public int getHealth(){
        return this.health;
    }

    public void setHealth(int hp){
        health = hp;
    }

    public void heal(int num){
        if (health+num>maxHealth){
            if (lives==3){//full lives
                health = maxHealth;
            }else {//player has lost lives
                lives ++;
                health = (health + num - maxHealth);
            }
        }else{
            health = health + num;
        }
    }

    public int getLives(){return this.lives;}

    public void setLives(int lives){
        this.lives = lives;
    }

    public void takeDamage(int num){
        if (health-num<=0) {
            if (lives>1){
                lives--;
                health = maxHealth;
            }else{
                if (lives==1){
                    lives--;
                }
                health = 0;
                //death
            }
        }else{
            health = health - num;
        }
    }

    public void update(){
        sprite.setPosition(body.getPosition().x * Constants.P2M, body.getPosition().y * Constants.P2M);
       movement();
       if(lastAttack>0){
           lastAttack--;
       }
       if(abilityCooldown>0){
           abilityCooldown--;
       }
       tile.setTile(body);
    }

    public CharacterTile getTile(){
        return tile;
    }

    public void healthBar(){

        //Health is normalized for creating the health bar for ease of control and coding
        float normalizedHealth = (float)health/maxHealth;
        healthBar.setProjectionMatrix(camera.combined);
        healthBar.begin(ShapeRenderer.ShapeType.Filled);
        healthBar.rect(sprite.getX(),sprite.getY()+sprite.getHeight()+1,16*normalizedHealth,4,
                Color.RED,Color.RED,Color.RED,Color.RED);
        healthBar.end();
    }

    public void resourceBar(){
        //Health is normalized for creating the health bar for ease of control and coding
        float normalizedResource = (float)(maxAbilityCooldown - abilityCooldown)/maxAbilityCooldown;
        resourceBar.setProjectionMatrix(camera.combined);
        resourceBar.begin(ShapeRenderer.ShapeType.Filled);
        resourceBar.rect(sprite.getX(),sprite.getY()+sprite.getHeight()-4,16*normalizedResource,4,
                Color.BLUE,Color.BLUE,Color.BLUE,Color.BLUE);
        resourceBar.end();
    }

    public void dispose(){
        attackSound.dispose();
        abilitySound.dispose();
    }

    @Override
    public boolean keyDown(int key) {
        return false;
    }

    @Override
    public boolean keyUp(int key) {
        return false;
    }

    @Override
    public boolean keyTyped(char c) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
       if (button==Input.Buttons.LEFT && lastAttack==0) {
           //Check volumes later
           if (Constants.soundOn){
               attackSound.play(Constants.soundVolume);
           }
           attack();
       }else if (button==Input.Buttons.RIGHT && abilityCooldown==0){
           if (Constants.soundOn){
               abilitySound.play(Constants.soundVolume);
           }
           ability();
       }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int i, int i1, int i2) {
        return false;
    }

    @Override
    public boolean mouseMoved(int i, int i1) {
        return false;
    }

    @Override
    public boolean scrolled(float v, float v1) {
        return false;
    }

    public Vector2 getPosition() {
        return body.getPosition();
    }
}
