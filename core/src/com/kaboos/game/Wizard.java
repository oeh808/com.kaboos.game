package com.kaboos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;

public class Wizard extends Player{
    public Animation fireballAnimation;
    public Animation icicleAnimation;
    protected TextureRegion[] framesFireball = new TextureRegion[4];
    protected TextureRegion[] framesIcicle = new TextureRegion[8];
    private int maxSlowDuration = 70;

    public Wizard(World world, Camera camera, int[] spawnPoint) {
        super(world, camera, "wizzard", spawnPoint);
        settingAttackAnimations();
        settingAbilityAnimations();

        attackSound = Gdx.audio.newSound(Gdx.files.internal("Player/fireball.wav"));
        abilitySound = Gdx.audio.newSound(Gdx.files.internal("Player/icicle.wav"));

        attackDamage = 25;
        abilityDamage = 12;

        attackW = 20;
        attackH = 20;
    }

    //The wizard class is unique in that its attacks are animated as well.public Animation fireballAnimation;
    //This method controls player attacks by creating fireballs and setting an attack rate
    @Override
    public void attack(){
        Body currentFireball;
        float clickX = Gdx.input.getX();
        float clickY = Gdx.input.getY();
        Vector3 clickDirection = new Vector3(clickX,clickY,0);
        camera.unproject(clickDirection);
       /*We subtract the click coords. from the mid point of the origin while subtracting the height of the projectile to compensate for
       accuracy issues that showed up.*/
        clickDirection.sub(sprite.getX() + sprite.getWidth()/2, sprite.getY() + sprite.getHeight()/2 - attackH /4f,0);
        clickDirection.nor();
        float attackAngle = (float) Math.atan2(clickDirection.y,clickDirection.x);

        lastAttack=100/attackSpeed;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set((sprite.getX() + sprite.getWidth()/4 + clickDirection.x*(sprite.getWidth()/3f)) / Constants.P2M,
                (sprite.getY() + sprite.getHeight()/4 + clickDirection.y*(sprite.getHeight()/3f)) / Constants.P2M);
        bodyDef.fixedRotation = true;
        attackProjectiles.add(world.createBody(bodyDef));
        currentFireball = attackProjectiles.get(attackProjectiles.size() - 1);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(attackW / 4 / Constants.P2M,
                attackH / 4 / Constants.P2M,
                currentFireball.getPosition().set(attackW / 4 / Constants.P2M, attackH / 4 / Constants.P2M),
                attackAngle);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0.1f;
        fixtureDef.filter.categoryBits = Constants.PLAYER_PROJECTILE;
        fixtureDef.filter.maskBits = Constants.WALL | Constants.ENEMIES;
        currentFireball.createFixture(fixtureDef);

        currentFireball.setLinearVelocity(clickDirection.x,clickDirection.y);

        shape.dispose();
    }

    @Override
    public void ability(){
        Body currentIcicle;
        float clickX = Gdx.input.getX();
        float clickY = Gdx.input.getY();
        Vector3 clickDirection = new Vector3(clickX,clickY,0);
        camera.unproject(clickDirection);

        abilityCooldown = maxAbilityCooldown;
        clickDirection.sub(sprite.getX() + sprite.getWidth()/2, sprite.getY() + sprite.getHeight()/2 - attackW*0.75f/4f,0);
        clickDirection.nor();
        float attackAngle = (float) Math.atan2(clickDirection.y,clickDirection.x);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set((sprite.getX() + sprite.getWidth()/4 + clickDirection.x*(sprite.getWidth()/3f)) / Constants.P2M,
                (sprite.getY() + sprite.getHeight()/4 + clickDirection.y*(sprite.getHeight()/3f)) / Constants.P2M);
        bodyDef.fixedRotation = true;
        abilityProjectiles.add(world.createBody(bodyDef));
        currentIcicle = abilityProjectiles.get(abilityProjectiles.size() - 1);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(attackW*0.75f / 4 / Constants.P2M,
                attackH*0.75f / 4 / Constants.P2M,
                currentIcicle.getPosition().set(attackW*0.75f / 4 / Constants.P2M, attackH*0.75f / 4 / Constants.P2M),
                attackAngle);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0.1f;
        fixtureDef.filter.categoryBits = Constants.PLAYER_PROJECTILE;
        fixtureDef.filter.maskBits = Constants.WALL | Constants.ENEMIES;
        currentIcicle.createFixture(fixtureDef);

        currentIcicle.setLinearVelocity(clickDirection.x,clickDirection.y);

        shape.dispose();
    }

    @Override
    public void drawProjectiles(Kaboos game, float elapsedTime){
        float x;
        float vectorX;
        float y;
        float vectorY;
        if (!attackProjectiles.isEmpty()){
            for (Body attackProjectile : attackProjectiles) {
                x = attackProjectile.getPosition().x * Constants.P2M;
                vectorX = attackProjectile.getLinearVelocity().x * Constants.P2M;
                y = attackProjectile.getPosition().y * Constants.P2M;
                vectorY = attackProjectile.getLinearVelocity().y * Constants.P2M;
                game.batch.draw((TextureRegion) fireballAnimation.getKeyFrame(elapsedTime, true), x, y, 25 / 4f, 25 / 4f,
                        30 / 2f, 30 / 2f, 1, 1, (float) Math.toDegrees(Math.atan2(vectorY, vectorX)));
            }
        }
        if (!abilityProjectiles.isEmpty()){
            for (Body abilityProjectile : abilityProjectiles) {
                x = abilityProjectile.getPosition().x * Constants.P2M;
                vectorX = abilityProjectile.getLinearVelocity().x * Constants.P2M;
                y = abilityProjectile.getPosition().y * Constants.P2M;
                vectorY = abilityProjectile.getLinearVelocity().y * Constants.P2M;
                game.batch.draw((TextureRegion) icicleAnimation.getKeyFrame(elapsedTime, true), x, y, attackW / 4f, attackH / 4f,
                        attackW / 2f, attackH / 2f, 1, 1, (float) Math.toDegrees(Math.atan2(vectorY, vectorX)));
            }
        }
    }

    @Override
    public void settingAttackAnimations(){
        int index = 0;
        tempFrames = TextureRegion.split(new Texture("Player/fireballX.png"),29,29);
        //Creating fireball animation frames
        for (int i=0;i<2;i++){
            for (int j=0;j<2;j++){
                framesFireball[index] = tempFrames[i][j];
                index++;
            }
        }
        fireballAnimation = new Animation(1/8f, framesFireball);
    }

    public void settingAbilityAnimations(){
        int index = 0;
        tempFrames = TextureRegion.split(new Texture("Player/icicle.png"),64,64);
        for (int i=0;i<8;i++){
            framesIcicle[index] = tempFrames[0][i];
            index++;
        }
        icicleAnimation = new Animation(1/8f, framesIcicle);
    }

    public int getMaxSlowDuration(){
        return maxSlowDuration;
    }
}
