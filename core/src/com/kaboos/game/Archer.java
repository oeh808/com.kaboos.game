package com.kaboos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;

public class Archer extends Player {
    public TextureRegion arrow;
    public TextureRegion superArrow;


    public Archer(World world, Camera camera, int[] spawnPoint){
        super(world,camera,"elf", spawnPoint);
        settingAttackAnimations();
        settingAbilityAnimations();

        attackSound = Gdx.audio.newSound(Gdx.files.internal("Player/arrow.ogg"));
        abilitySound = Gdx.audio.newSound(Gdx.files.internal("Player/arrow.ogg"));

        maxAbilityCooldown = 175;

        attackDamage = 15;
        abilityDamage = 35;

        attackW = 21;
        attackH = 7;
    }

    @Override
    public void attack() {
        Body currentArrow;
        float clickX = Gdx.input.getX();
        float clickY = Gdx.input.getY();
        Vector3 clickDirection = new Vector3(clickX,clickY,0);
        camera.unproject(clickDirection);
       /*We subtract the click coords. from the mid point of the origin while subtracting the height of the projectile to compensate for
       accuracy issues that showed up.*/
        clickDirection.sub(sprite.getX() + sprite.getWidth()/2, sprite.getY() + sprite.getHeight()/2 - attackW /4f,0);
        clickDirection.nor();
        float attackAngle = (float) Math.atan2(clickDirection.y,clickDirection.x);

        lastAttack=100/attackSpeed;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set((sprite.getX() + sprite.getWidth()/4 + clickDirection.x*(sprite.getWidth()/3f)) / Constants.P2M,
                (sprite.getY() + sprite.getHeight()/4 + clickDirection.y*(sprite.getHeight()/3f)) / Constants.P2M);
        bodyDef.fixedRotation = true;
        attackProjectiles.add(world.createBody(bodyDef));
        currentArrow = attackProjectiles.get(attackProjectiles.size() - 1);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(attackW / 4 / Constants.P2M,
                attackH / 5 / Constants.P2M,
                currentArrow.getPosition().set(attackW / 4 / Constants.P2M, attackH / 4 / Constants.P2M),
                attackAngle);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0.1f;
        fixtureDef.filter.categoryBits = Constants.PLAYER_PROJECTILE;
        fixtureDef.filter.maskBits = (short) (Constants.WALL | Constants.ENEMIES);
        currentArrow.createFixture(fixtureDef);

        currentArrow.setLinearVelocity(clickDirection.x*1.2f,clickDirection.y*1.2f);

        shape.dispose();
    }

    @Override
    public void ability() {
        Body currentArrow;
        float clickX = Gdx.input.getX();
        float clickY = Gdx.input.getY();
        Vector3 clickDirection = new Vector3(clickX,clickY,0);
        camera.unproject(clickDirection);
       /*We subtract the click coords. from the mid point of the origin while subtracting the height of the projectile to compensate for
       accuracy issues that showed up.*/
        clickDirection.sub(sprite.getX() + sprite.getWidth()/2, sprite.getY() + sprite.getHeight()/2 - attackH /4f,0);
        clickDirection.nor();
        float attackAngle = (float) Math.atan2(clickDirection.y,clickDirection.x);

        abilityCooldown = maxAbilityCooldown;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set((sprite.getX() + sprite.getWidth()/4 + clickDirection.x*(sprite.getWidth()/3f)) / Constants.P2M,
                (sprite.getY() + sprite.getHeight()/4 + clickDirection.y*(sprite.getHeight()/3f)) / Constants.P2M);
        bodyDef.fixedRotation = true;
        abilityProjectiles.add(world.createBody(bodyDef));
        currentArrow = abilityProjectiles.get(abilityProjectiles.size() - 1);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(attackW / 4 / Constants.P2M,
                attackH / 5 / Constants.P2M,
                currentArrow.getPosition().set(attackW / 4 / Constants.P2M, attackH / 4 / Constants.P2M),
                attackAngle);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0.1f;
        fixtureDef.filter.categoryBits = Constants.PLAYER_PROJECTILE;
        fixtureDef.filter.maskBits = (Constants.WALL | Constants.ENEMIES);
        currentArrow.createFixture(fixtureDef);

        currentArrow.setLinearVelocity(clickDirection.x*0.8f,clickDirection.y*0.8f);

        shape.dispose();
    }

    @Override
    public void drawProjectiles(Kaboos game, float elapsedTime) {
        float x;
        float vectorX;
        float y;
        float vectorY;
        if (!attackProjectiles.isEmpty()){
            for (int i=0; i<attackProjectiles.size();i++) {
                x = attackProjectiles.get(i).getPosition().x * Constants.P2M;
                vectorX = attackProjectiles.get(i).getLinearVelocity().x * Constants.P2M;
                y = attackProjectiles.get(i).getPosition().y * Constants.P2M;
                vectorY = attackProjectiles.get(i).getLinearVelocity().y * Constants.P2M;
                game.batch.draw(arrow, x, y, attackW /4f, attackH /4f,
                        attackW /2f, attackH /2f,1,1,(float) Math.toDegrees(Math.atan2(vectorY,vectorX)));
            }
        }

        if (!abilityProjectiles.isEmpty()){
            for (int i=0; i<abilityProjectiles.size();i++) {
                x = abilityProjectiles.get(i).getPosition().x * Constants.P2M;
                vectorX = abilityProjectiles.get(i).getLinearVelocity().x * Constants.P2M;
                y = abilityProjectiles.get(i).getPosition().y * Constants.P2M;
                vectorY = abilityProjectiles.get(i).getLinearVelocity().y * Constants.P2M;
                game.batch.draw(superArrow, x, y, attackW /4f, attackH /4f,
                        attackW /2f, attackH /2f,1,1,(float) Math.toDegrees(Math.atan2(vectorY,vectorX)));
            }
        }
    }

    @Override
    public void settingAttackAnimations() {
        arrow = new TextureRegion(new Texture("Player/arrow.png"));
    }

    public void settingAbilityAnimations(){
        superArrow = new TextureRegion(new Texture("Player/super_arrow.png"));
    }
}
