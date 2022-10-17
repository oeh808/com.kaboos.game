package com.kaboos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.ArrayList;


public class GameScreen implements Screen {
    private final Kaboos game;
    private float elapsedTime;
    private Player player;
    private final ArrayList<Enemy> enemies = new ArrayList<>();
    private final ArrayList<Body> enemyBodies = new ArrayList<>();
    private final ArrayList<Item> items = new ArrayList<>();
    private final ArrayList<Body> itemBodies = new ArrayList<>();
    private final OrthographicCamera camera;
    private final World world;
    private final Box2DDebugRenderer debugRenderer;
    private Matrix4 debugMatrix;
    private final Room room;
    private final RoomGenerator gen = new RoomGenerator();
    private final ArrayList<int[]> spawnPoints;
    private final ArrayList<int[]> spawnPointsPrime = new ArrayList<>();
    private int spawnTimer = 30;
    private int waveNum = 0;
    private long startTime = TimeUtils.millis();
    private long timePassed = 0;

    public ArrayList<Body> toDestroy = new ArrayList<>();
    private final ArrayList<Enemy> toRemove = new ArrayList<>();
    private final ArrayList<Item> toAdd = new ArrayList<>();

    private final int enemyCount;


    public GameScreen(final Kaboos game){
        this.game = game;
        Constants.combatMusic.setLooping(true);
        Constants.roomNumber = 1;

        world = new World(new Vector2(0, 0), true);
        //this.room = new Room(new TmxMapLoader().load("map1.tmx"));
        this.room = new Room(gen.getRoom());
        room.createInvisibleWalls(world);
        spawnPoints = gen.getSpawnPoints();
        spawnPointsPrime.addAll(spawnPoints);

        debugRenderer = new Box2DDebugRenderer();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.gameWidth, Constants.gameHeight);

        room.tiledMapRenderer.setView(camera);
        room.createMapObjects(world);

        addPlayer();
        if (Constants.difficulty.equals("Easy")){
            enemyCount = 2;
        }else if (Constants.difficulty.equals("Medium")){
            enemyCount = 3;
        }else {
            enemyCount = 4;
        }
        if (Constants.musicOn){
            Constants.combatMusic.setVolume(Constants.musicVolume);//Volume is in a range of 0-1
            Constants.combatMusic.play();
        }
    }

    public GameScreen(final Kaboos game, int playerLives, int playerHealth, long elapsedTime){
        this.game = game;

        world = new World(new Vector2(0, 0), true);
        //this.room = new Room(new TmxMapLoader().load("map1.tmx"));
        this.room = new Room(gen.getRoom());
        room.createInvisibleWalls(world);
        spawnPoints = gen.getSpawnPoints();
        spawnPointsPrime.addAll(spawnPoints);

        debugRenderer = new Box2DDebugRenderer();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.gameWidth, Constants.gameHeight);

        room.tiledMapRenderer.setView(camera);
        room.createMapObjects(world);

        addPlayer();
        player.setHealth(playerHealth);
        player.setLives(playerLives);
        if (Constants.difficulty.equals("Easy")){
            enemyCount = 2;
        }else if (Constants.difficulty.equals("Medium")){
            enemyCount = 3;
        }else {
            enemyCount = 4;
        }
    }

    @Override
    public void render(float delta){
        //Setting up
        world.step(1/60f, 6, 2);

        collisions();

        for (Item item : toAdd){
            items.add(item);
            itemBodies.add(item.body);
        }
        toAdd.clear();

        toDestroy.addAll(player.toDestroy);
        for (Enemy enemy : enemies) {
            toDestroy.addAll(enemy.toDestroy);
        }

        if (!toDestroy.isEmpty()){
            for (Body body : toDestroy) {
                world.destroyBody(body);
            }
            player.toDestroy.clear();
            for (Enemy enemy : enemies) {
                enemy.toDestroy.clear();
            }
            toDestroy.clear();
        }

        for (Enemy enemy : toRemove){
            enemy.attackSound.dispose();
            enemies.remove(enemy);
            enemyBodies.remove(enemy.body);
        }
        if (enemies.isEmpty()){
            toRemove.clear();
            if (spawnTimer==0 && waveNum<3){
                if (Constants.roomNumber<5){
                    generateEnemies(enemyCount);
                }else if (Constants.roomNumber<10){
                    generateEnemies(enemyCount + 1);
                }else{//last 5 rooms
                    generateEnemies(enemyCount + 2);
                }
                spawnTimer=30;
                waveNum++;
            }else{
                spawnTimer--;
            }
        }
        ScreenUtils.clear(1, 1, 1, 1);
        //camera.update();
        game.batch.setProjectionMatrix(camera.combined); //Tells the batch to use camera coords.
        debugMatrix = game.batch.getProjectionMatrix().cpy().scale(Constants.P2M, Constants.P2M, 0);
        elapsedTime+=delta;

        //Drawing
        game.batch.begin();
        room.renderFirstLayer();
        room.renderFirstLayer();
        player.drawSprite(game, elapsedTime);
        player.drawProjectiles(game,elapsedTime);

        for (Enemy enemy : enemies) {
            enemy.drawSprite(game, elapsedTime, delta);
            enemy.drawProjectiles(game, elapsedTime);
        }

        for (Item item : items){
            item.drawSprite(game);
        }
        game.batch.end();

        game.batch.begin();
        room.renderSecondLayer();
        ui();
        game.batch.end();
        //Updating player movement
        player.update();

        for (Enemy enemy : enemies) {
            enemy.healthBar();
        }

        for (Enemy enemy : enemies){
            if (enemy.destroyed){
                enemy.toDestroy.addAll(enemy.attackProjectiles);
                enemy.attackProjectiles.clear();
                if (enemy.deathTimer==0){
                    toRemove.add(enemy);
                    int min = 1;
                    int max = 100;
                    if ((int)Math.floor(Math.random()*(max-min+1)+min)<11){//10% chance of enemy dropping health potion
                        final HealthPotion temp = new HealthPotion(world,camera, new int[]{(int) enemy.sprite.getX(), (int) enemy.sprite.getY()});
                        toAdd.add(temp);
                    }
                }else{
                    enemy.deathTimer--;
                }
            }
        }
        player.healthBar();
        player.resourceBar();

        //debugRenderer.render(world, debugMatrix);

        if ((player.getLives()==0 && player.getHealth()==0)){
            game.setScreen(new GameOverScreen(game,false));
            dispose();
        }else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){//Pause the game
            if (Constants.soundOn){
                Constants.button.play(Constants.soundVolume);
            }
            Constants.gameState = this;
            game.setScreen(new PauseScreen(game));
        }else if (enemies.isEmpty() && waveNum==3){
            if (Gdx.input.isKeyPressed(Input.Keys.SPACE)){//Proceed to next level
                if (Constants.roomNumber==15){
                    game.setScreen(new GameOverScreen(game,true));
                }else{
                    Constants.roomNumber++;
                    game.setScreen(new GameScreen(game,player.getLives(),player.getHealth(),timePassed));
                }
                dispose();
            }
        }
        updateTimer();
    }

    public void ui(){
        float sizeOfHeart = 16/2f;
        int numOfHearts;
        if (player.getLives()==1){
            numOfHearts = 1;
        }else if (player.getLives()==2){
            numOfHearts = 2;
        }else{
            numOfHearts = 3;
        }
        switch(player.getLives()){
            case 0:
                numOfHearts = 0;
                break;
            case 1:
                game.batch.draw(player.fullHeart,0,0,sizeOfHeart,sizeOfHeart);
                break;
            case 2:
                game.batch.draw(player.fullHeart,0,0,sizeOfHeart,sizeOfHeart);
                game.batch.draw(player.fullHeart,sizeOfHeart,0,sizeOfHeart,sizeOfHeart);
                break;
            default:
                game.batch.draw(player.fullHeart,0,0,sizeOfHeart,sizeOfHeart);
                game.batch.draw(player.fullHeart,sizeOfHeart,0,sizeOfHeart,sizeOfHeart);
                game.batch.draw(player.fullHeart,sizeOfHeart*2,0,sizeOfHeart,sizeOfHeart);
        }
        while(numOfHearts<3){
            game.batch.draw(player.emptyHeart,sizeOfHeart*numOfHearts,0,sizeOfHeart,sizeOfHeart);
            numOfHearts++;
        }
        game.font.draw(game.batch, "Level : " +  Constants.roomNumber + " - " + waveNum,sizeOfHeart*3,game.font.getData().lineHeight);
        game.font.draw(game.batch, "Timer : " +  timePassed/1000,sizeOfHeart*3,game.font.getData().lineHeight *2 );
    }

    public void collisions(){//Collision filtering method
        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                Body bodyA = contact.getFixtureA().getBody();
                Body bodyB = contact.getFixtureB().getBody();
                //Dealing with Projectiles
                if(player.attackProjectiles.contains(bodyA))
                {
                    player.toDestroy.add(bodyA);

                    player.attackProjectiles.remove(bodyA);
                    if (enemyBodies.contains(bodyB)){
                        getEnemy(bodyB).takeDamage(player.attackDamage);
                    }
                }else if(player.attackProjectiles.contains(bodyB)){
                    player.toDestroy.add(bodyB);
                    player.attackProjectiles.remove(bodyB);
                    if (enemyBodies.contains(bodyA)){
                        getEnemy(bodyA).takeDamage(player.attackDamage);
                    }
                }else if(player.abilityProjectiles.contains(bodyA)){
                    player.toDestroy.add(bodyA);
                    player.abilityProjectiles.remove(bodyA);
                    if (enemyBodies.contains(bodyB)){
                        getEnemy(bodyB).takeDamage(player.abilityDamage);
                        if (Constants.playerType.equals("Wizard")){
                            Wizard temp = (Wizard) player;
                            getEnemy(bodyB).setSlowed(temp.getMaxSlowDuration());
                        }
                    }
                }else if(player.abilityProjectiles.contains(bodyB)){
                    player.toDestroy.add(bodyB);
                    player.abilityProjectiles.remove(bodyB);
                    if (enemyBodies.contains(bodyA)){
                        getEnemy(bodyA).takeDamage(player.abilityDamage);
                        if (Constants.playerType.equals("Wizard")){
                            Wizard temp = (Wizard) player;
                            getEnemy(bodyA).setSlowed(temp.getMaxSlowDuration());
                        }
                    }
                }else if(getEnemy2(bodyA)!=null){//Checks if the projectile body belongs to an enemy
                    Enemy enemy = getEnemy2(bodyA);
                    enemy.toDestroy.add(bodyA);
                    enemy.attackProjectiles.remove(bodyA);
                    if (bodyB==player.body){
                        player.takeDamage(enemy.attackDamage);
                    }

                }else if(getEnemy2(bodyB)!=null) {
                    Enemy enemy = getEnemy2(bodyB);
                    enemy.toDestroy.add(bodyB);
                    enemy.attackProjectiles.remove(bodyB);
                    if (bodyA == player.body) {
                        player.takeDamage(enemy.attackDamage);
                    }
                }else if (enemyBodies.contains(bodyA) && room.walls.contains(bodyB)){
                    getEnemy(bodyA).setColliding(true);
                }else if(enemyBodies.contains(bodyB) && room.walls.contains(bodyA)){
                    getEnemy(bodyB).setColliding(true);
                }else if (itemBodies.contains(bodyA)){
                    Item temp = getItem(bodyA);
                    if (temp!=null){
                        temp.itemEffect(player);
                        toDestroy.add(bodyA);
                        items.remove(temp);
                    }
                }else if (itemBodies.contains(bodyB)){
                    Item temp = getItem(bodyB);
                    if (temp!=null){
                        temp.itemEffect(player);
                        toDestroy.add(bodyB);
                        items.remove(temp);
                    }
                }
            }

            @Override
            public void endContact(Contact contact) {
                Body bodyA = contact.getFixtureA().getBody();
                Body bodyB = contact.getFixtureB().getBody();

                if (enemyBodies.contains(bodyA) && room.walls.contains(bodyB)){
                    getEnemy(bodyA).setColliding(false);
                }else if(enemyBodies.contains(bodyB) && room.walls.contains(bodyA)){
                    getEnemy(bodyB).setColliding(false);
                }
            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {
            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {
            }
        });
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void show() {

    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }
    @Override
    public void dispose() {
        player.dispose();
        world.dispose();
    }

    public void addPlayer(){
        int[] playerSpawnPoint;
        playerSpawnPoint = spawnPoints.get(0);

        switch (Constants.playerType){
            case "Knight":
                player = new Knight(world,camera,playerSpawnPoint);
                break;
            case "Archer":
                player = new Archer(world,camera,playerSpawnPoint);
                break;
            default:
                player = new Wizard(world,camera,playerSpawnPoint);
        }
        int max = spawnPoints.size()/2;

        if (Constants.difficulty.equals("Easy")){//Only adds 1 item on easy mode
            int[] itemSpawnPoint = new int[2];
            itemSpawnPoint[0] = playerSpawnPoint[0] + 32;
            itemSpawnPoint[1] = playerSpawnPoint[1];
            items.add(new HealthPotion(world,camera,itemSpawnPoint));
            itemBodies.add(items.get(0).body);
        }

        if (max > 0) {
            spawnPoints.subList(0, max).clear();
        }
    }

    public void addEnemy(){
        final Enemy enemy;
        int[] enemySpawnPoint;
        int min=0;
        int max=spawnPoints.size()-1;
        int point = (int)Math.floor(Math.random()*(max-min+1)+min);

        enemySpawnPoint = spawnPoints.get(point);

        max = 10;

        int temp = (int)Math.floor(Math.random()*(max-min+1)+min);

        if (temp<7){//60% chance
            enemy = new Cultist(player,room,world,camera,"necromancer",enemySpawnPoint);
        }else if (temp<9) {
            enemy = new Marauder(player,room,world,camera,"masked_orc",enemySpawnPoint);
        }else{
            enemy = new Muddy(player,room,world,camera,"muddy",enemySpawnPoint);
        }
        enemies.add(enemy);
        enemyBodies.add(enemy.body);
        spawnPoints.remove(point);
    }

    public Enemy getEnemy(Body enemyBody){
        for (Enemy enemy : enemies) {
            if (enemy.body == enemyBody) {
                return enemy;
            }
        }
        return null;
    }

    public Enemy getEnemy2(Body projectileBody){
        for (Enemy enemy : enemies) {
            if (enemy.attackProjectiles.contains(projectileBody)) {
                return enemy;
            }
        }
        return null;
    }

    public void safeSpawn(){
        int x = player.getTile().getX()*16;
        int y = player.getTile().getY()*16;
        int deltaX;
        int deltaY;

        ArrayList<int[]> toRemain = new ArrayList<>();

        for (int[] spawnPoint : spawnPoints){
            deltaX = Math.abs(spawnPoint[0] - x)/16;
            deltaY = Math.abs(spawnPoint[1] - y)/16;
            if (deltaX > 2 && deltaY > 2){
                toRemain.add(spawnPoint);
            }
        }

        spawnPoints.retainAll(toRemain);
    }

    public void generateEnemies(int count){
        if (waveNum<1){
            for (int i=0;i<count;i++){
                addEnemy();
            }
        }else{
            safeSpawn();
            for (int i=0;i<count;i++){
                addEnemy();
            }
        }
        spawnPoints.clear();
        spawnPoints.addAll(spawnPointsPrime);
    }

    public Item getItem(Body itemBody){
        for (Item item : items) {
            if (item.body == itemBody) {
                return item;
            }
        }
        return null;
    }

    public Player getPlayer(){
        return player;
    }

    public void updateTimer(){
        timePassed = TimeUtils.timeSinceMillis(startTime);
    }
}
