package com.kaboos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Muddy extends Enemy{
    private TextureRegion[] framesLightning = new TextureRegion[4];
    private Animation lightningAnimation;
    private float projectileSpeedModifier;

    public Muddy(Player player, Room room, World world, Camera camera, String enemyType, int[] spawnPoint) {
        super(player, room, world, camera, enemyType,spawnPoint);
        settingAttackAnimation();

        health = 50;
        MaxHealth = 50;
        attackDamage = 30;
        attackSpeed = 1;
        if (Constants.difficulty.equals("Kaboos")){
            projectileSpeedModifier = 1.25f;
            movementSpeed = 0.75f;
            baseMovementSpeed = 0.75f;
        }else{
            if (Constants.difficulty.equals("Easy")){
                health = 35;
                MaxHealth = 35;
                movementSpeed = 0.35f;
                baseMovementSpeed = 0.35f;
            }else{
                movementSpeed = 0.5f;
                baseMovementSpeed = 0.5f;
            }
            projectileSpeedModifier = 1;
        }

        attackSound  = Gdx.audio.newSound(Gdx.files.internal("Enemy/lightning.mp3"));
        attackW = 30;
        attackH = 10;
    }

    @Override
    public void attack() {
        Body currentBolt;

        Vector2 attackDirection = distanceToPlayer;

        attackDirection.nor();
        float attackAngle = (float) Math.atan2(attackDirection.y,attackDirection.x);

        lastAttack=100/attackSpeed;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set((sprite.getX() + sprite.getWidth()/4) / Constants.P2M,
                (sprite.getY() + sprite.getHeight()/4) / Constants.P2M);
        bodyDef.fixedRotation = true;
        attackProjectiles.add(world.createBody(bodyDef));
        currentBolt = attackProjectiles.get(attackProjectiles.size() - 1);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(attackW / 4 / Constants.P2M,
                attackH / 5 / Constants.P2M,
                currentBolt.getPosition().set(attackW / 4 / Constants.P2M, attackH / 4 / Constants.P2M),
                attackAngle);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0.1f;
        fixtureDef.filter.categoryBits = Constants.ENEMY_PROJECTILE;
        fixtureDef.filter.maskBits = (Constants.WALL | Constants.PLAYER);
        currentBolt.createFixture(fixtureDef);

        currentBolt.setLinearVelocity(attackDirection.x * 0.75f * projectileSpeedModifier,attackDirection.y * 0.75f * projectileSpeedModifier);

        shape.dispose();

    }

    @Override
    public void drawProjectiles(Kaboos game, float elapsedTime) {
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
                game.batch.draw((TextureRegion) lightningAnimation.getKeyFrame(elapsedTime, true), x, y, attackW / 4f, attackH / 4f,
                        attackW / 2f, attackH / 2f, 1, 1, (float) Math.toDegrees(Math.atan2(vectorY, vectorX)));
            }
        }
    }

    @Override
    public void settingAttackAnimation() {
        TextureRegion[][] tempFrames = TextureRegion.split(new Texture("Enemy/lightning.png"),64,76);
        System.arraycopy(tempFrames[0], 0, framesLightning, 0, 4);
        lightningAnimation = new Animation(1/8f, framesLightning);
    }

    @Override
    public void followPath(float delta) {
        if (lastAttack==0){
            attackSound();
            attack();
        }

        if (path.getCount()>1){
            float deltaX = (path.get(1).getX() - tile.getX()) * movementSpeed;
            float deltaY = (path.get(1).getY() - tile.getY()) * movementSpeed;
            Vector2 force;
            if (isColliding){
                if ((int)Math.floor(Math.random()*(10-1+1)+1)>8){
                    force = new Vector2((deltaY - deltaX) * 2, (deltaX - deltaY) * 2);
                }else{
                    if (deltaX!=0){//trying to move in x direction
                        if ((int)Math.floor(Math.random()*(2))==1){
                            force = new Vector2(-deltaX * 2, deltaX * 5);
                        }else{
                            force = new Vector2(-deltaX * 2, -deltaX * 5);
                        }
                    }else{//trying to move in y direction
                        if ((int)Math.floor(Math.random()*(2))==1){
                            force = new Vector2(deltaY * 5, -deltaY * 2);
                        }else{
                            force = new Vector2(-deltaY * 5, -deltaY * 2);
                        }
                    }
                }
            }else{
                force = new Vector2(deltaX, deltaY);
            }
            //force is used instead of setting velocity as it is less rigid than adjusting velocities
            force.scl(delta);
            body.applyForceToCenter(force, body.isAwake());
        }
    }
}
