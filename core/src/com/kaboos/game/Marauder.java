package com.kaboos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Marauder extends Enemy{
    public TextureRegion machete;
    private int attackDuration = 12;
    private Vector2 attackSpriteDirection = new Vector2();
    private float attackX;
    private float attackY;

    public Marauder(Player player, Room room, World world, Camera camera, String enemyType, int[] spawnPoint) {
        super(player, room, world, camera, enemyType,spawnPoint);

        settingAttackAnimation();

        health = 55;
        MaxHealth = 55;
        attackSpeed = 1;
        if (Constants.difficulty.equals("Kaboos")){
            attackDamage = 40;
            movementSpeed = 1.2f;
            baseMovementSpeed = 1.2f;
        }else{
            if (Constants.difficulty.equals("Easy")){
                health = 40;
                MaxHealth = 40;
                movementSpeed = 0.7f;
                baseMovementSpeed = 0.7f;
            }else{
                movementSpeed = 0.9f;
                baseMovementSpeed = 0.9f;
            }
            attackDamage = 25;
        }

        attackSound  = Gdx.audio.newSound(Gdx.files.internal("Enemy/machete.wav"));
        attackW = 22*1.1f;
        attackH = 5*1.4f;

    }
    @Override
    public void attack() {
        Body currentMachete;

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
        currentMachete = attackProjectiles.get(attackProjectiles.size() - 1);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(attackW / 4 / Constants.P2M,
                attackH / 5 / Constants.P2M,
                currentMachete.getPosition().set(attackW / 4 / Constants.P2M, attackH / 4 / Constants.P2M),
                attackAngle);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0.1f;
        fixtureDef.filter.categoryBits = Constants.ENEMY_PROJECTILE;
        fixtureDef.filter.maskBits = (Constants.WALL | Constants.PLAYER);
        currentMachete.createFixture(fixtureDef);

        currentMachete.setLinearVelocity(attackDirection.x*0.75f,attackDirection.y*0.75f);

        attackSpriteDirection = currentMachete.getLinearVelocity();
        attackX = currentMachete.getPosition().x * Constants.P2M + attackSpriteDirection.x*2;
        attackY = currentMachete.getPosition().y * Constants.P2M + attackSpriteDirection.y*2;

        attackDuration = 12;

        shape.dispose();

    }

    @Override
    public void drawProjectiles(Kaboos game, float elapsedTime) {
        float vectorX;
        float vectorY;
        if (attackDuration>0) {
            vectorX = attackSpriteDirection.x;
            attackX = attackX + vectorX;
            vectorY = attackSpriteDirection.y;
            attackY = attackY + vectorY;

            game.batch.draw(machete, attackX, attackY, attackW / 4f, attackH / 4f,
                    attackW / 2f, attackH / 2f, 1, 1, (float) Math.toDegrees(Math.atan2(vectorY, vectorX)));

            attackDuration--;
            if (attackDuration == 0 && !attackProjectiles.isEmpty()) {
                toDestroy.add(attackProjectiles.get(0));
                attackProjectiles.remove(0);
            }
        }

    }

    @Override
    public void settingAttackAnimation() {
        machete = new TextureRegion(new Texture("Enemy/machete.png"));
    }

    @Override
    public void followPath(float delta) {
        if (path.getCount()<3){
            if (lastAttack==0){
                attackSound();
                attack();
            }
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
