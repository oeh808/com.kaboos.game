package com.kaboos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;

public class Knight extends Player{
    public TextureRegion sword;
    private int attackDuration = 8;
    private int chargeDuration = 10;
    private Vector2 attackSpriteDirection = new Vector2();
    private float attackX;
    private float attackY;

    public Knight (World world, Camera camera, int[] spawnPoint){
        super(world,camera,"knight", spawnPoint);
        settingAttackAnimations();

        attackSound = Gdx.audio.newSound(Gdx.files.internal("Player/sword.wav"));
        abilitySound = Gdx.audio.newSound(Gdx.files.internal("Player/dash.mp3"));

        maxHealth = 175;
        health = maxHealth;

        attackDamage = 25;

        attackW = 30 * 1.3f;
        attackH = 12;
    }

    @Override
    public void attack() {
            Body currentSword;
            float clickX = Gdx.input.getX();
            float clickY = Gdx.input.getY();
            Vector3 clickDirection = new Vector3(clickX,clickY,0);
            camera.unproject(clickDirection);

            clickDirection.sub(sprite.getX() + sprite.getWidth()/2, sprite.getY() + sprite.getHeight()/2 - attackH /4f,0);
            clickDirection.nor();
            float attackAngle = (float) Math.atan2(clickDirection.y,clickDirection.x);

            lastAttack=100/attackSpeed;
            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyDef.BodyType.DynamicBody;
            bodyDef.position.set((sprite.getX() + clickDirection.x*(sprite.getWidth()/2f)) / Constants.P2M,
                    (sprite.getY() + sprite.getHeight()/5 + clickDirection.y*(sprite.getHeight()/2f)) / Constants.P2M);
            bodyDef.fixedRotation = true;
            attackProjectiles.add(world.createBody(bodyDef));
            currentSword = attackProjectiles.get(attackProjectiles.size() - 1);
            PolygonShape shape = new PolygonShape();
            shape.setAsBox(attackW / 4 / Constants.P2M,
                    attackH / 4 / Constants.P2M,
                    currentSword.getPosition().set(attackW / 4 / Constants.P2M, attackH / 4 / Constants.P2M),
                    attackAngle);
            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.shape = shape;
            fixtureDef.density = 0.1f;
            fixtureDef.filter.categoryBits = Constants.PLAYER_PROJECTILE;
            fixtureDef.filter.maskBits = (Constants.WALL | Constants.ENEMIES);
            currentSword.createFixture(fixtureDef);

            currentSword.setLinearVelocity(clickDirection.x,clickDirection.y);
            attackSpriteDirection = currentSword.getLinearVelocity();
            attackX = currentSword.getPosition().x * Constants.P2M + attackSpriteDirection.x*2;
            attackY = currentSword.getPosition().y * Constants.P2M + attackSpriteDirection.y*2;

            attackDuration = 8;

            shape.dispose();
    }

    @Override
    public void ability() {
        float clickX = Gdx.input.getX();
        float clickY = Gdx.input.getY();
        Vector3 clickDirection = new Vector3(clickX,clickY,0);
        camera.unproject(clickDirection);

        clickDirection.sub(sprite.getX() + sprite.getWidth()/2, sprite.getY() + sprite.getHeight()/2 - 20/4f,0);
        clickDirection.nor();

        abilityCooldown = maxAbilityCooldown;
        Vector2 direction = new Vector2(clickDirection.x*2f,clickDirection.y*2f);

        if (clickDirection.x>0){ //Charging to the right
            runAnimation = runAnimationRight;
            idleAnimation = idleAnimationRight;
        }else{
            runAnimation = runAnimationLeft;
            idleAnimation = idleAnimationLeft;
        }

        movementModifierActive = true;

        body.applyForceToCenter(direction,true);
    }

    @Override
    public void update(){
        super.update();
        if (chargeDuration==0){
            movementModifierActive=false;
            chargeDuration=10;
        }else if (movementModifierActive){
            chargeDuration--;
        }
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

            game.batch.draw(sword, attackX, attackY, attackW / 4f, attackH / 4f,
                    attackW / 2f, attackH / 2f, 1, 1, (float) Math.toDegrees(Math.atan2(vectorY, vectorX)));

            attackDuration--;
            if (attackDuration == 0 && !attackProjectiles.isEmpty()) {
                toDestroy.add(attackProjectiles.get(0));
                attackProjectiles.remove(0);
            }
        }
    }

    @Override
    public void settingAttackAnimations() {
        sword = new TextureRegion(new Texture("Player/sword.png"));
    }
}
