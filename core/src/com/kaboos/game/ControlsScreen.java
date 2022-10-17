package com.kaboos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;

public class ControlsScreen implements Screen {
    private final Kaboos game;
    private final Skin skin;
    private final Stage stage;
    private final OrthographicCamera camera;
    private final float buttonX = Gdx.graphics.getWidth()/2 - (Gdx.graphics.getWidth()/10)/2;
    private float buttonY = (Gdx.graphics.getHeight() - Gdx.graphics.getHeight()/12);

    public ControlsScreen(Kaboos game){
        this.game = game;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        skin = new Skin(Gdx.files.internal("uiskin.json"));
        stage = new Stage();

        title();
        controls(0);
        back();

        Gdx.input.setInputProcessor(stage);
    }
    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        stage.draw();
        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }

    public void controls(int n) {
        final Label text;
        final TextButton button;
        if (n == 0) {
            button = new TextButton("W", skin, "default");
            text = new Label("Move player character upwards.", skin, "default");
        } else if (n == 1) {
            button = new TextButton("A", skin, "default");
            text = new Label("Move player character left.", skin, "default");
        } else if (n == 2) {
            button = new TextButton("S", skin, "default");
            text = new Label("Move player character down.", skin, "default");
        } else if (n == 3) {
            button = new TextButton("D", skin, "default");
            text = new Label("Move player character right.", skin, "default");
        } else if (n==4) {
            button = new TextButton("Left Click", skin, "default");
            text = new Label(attackText()+" ("+Constants.playerType+").", skin, "default");
        } else if (n==5){
            button = new TextButton("Right Click", skin, "default");
            text = new Label(abilityText()+" ("+Constants.playerType+").", skin, "default");
        } else if (n==6){
            button = new TextButton("Escape", skin, "default");
            text = new Label("Pauses the game/Unpauses the game.", skin, "default");
        }else if (n==7){
            button = new TextButton("Space", skin, "default");
            text = new Label("Advances to the next room (Cannot be used until all waves of enemies are defeated in the current room).", skin, "default");
        } else{
            return;
        }
        button.setWidth(Gdx.graphics.getWidth()/20);
        button.setHeight(Gdx.graphics.getWidth()/20);
        button.setPosition(buttonX*0.5f, buttonY);

        text.setWidth(Gdx.graphics.getWidth()/10);
        text.setHeight(Gdx.graphics.getHeight()/12);
        text.setPosition(buttonX*0.5f + button.getWidth(), buttonY);
        stage.addActor(button);
        stage.addActor(text);
        buttonY = buttonY - text.getHeight()*1.1f;
        controls(n + 1);
    }

    public void title(){
        final Label title;
        title = new Label("Controls",skin,"default");
        title.setWidth(Gdx.graphics.getWidth()/6);
        title.setHeight(Gdx.graphics.getHeight()/7);
        title.setPosition(Gdx.graphics.getWidth()/2 - title.getWidth()/2, Gdx.graphics.getHeight() - title.getHeight());
        title.setAlignment(Align.center);
        title.setFontScale(2);
        stage.addActor(title);
        buttonY = buttonY - title.getHeight()*1.1f;
    }

    public void back(){
        final TextButton button;
        button = new TextButton("Back", skin, "default");
        button.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                if (Constants.soundOn){
                    Constants.button.play(Constants.soundVolume);
                }
                game.setScreen(new CharacterSelectScreen(game));
                dispose();
            }
        });
        button.setWidth(Gdx.graphics.getWidth()/14);
        button.setHeight(Gdx.graphics.getHeight()/14);
        button.setPosition(0, 0);
        stage.addActor(button);
    }

    public String attackText(){
        switch (Constants.playerType){
            case "Knight":
                return "Thrusts a sword from the player character's position to the mouse cursor.";
            case "Archer":
                return "Launches an arrow from the player character's position to the mouse cursor.";
            default:
                return "Launches a fireball from the player character's position to the mouse cursor.";
        }
    }

    public String abilityText(){
        switch (Constants.playerType){
            case "Knight":
                return "Causes the player character to dash towards the mouse cursor, can be interrupted by movement.";
            case "Archer":
                return "Launches a slower, stronger arrow from the player character's position to the mouse cursor.";
            default:
                return "Launches an icicle from the player character's position to the mouse cursor, slows down enemies hit for a limited time.";
        }
    }
}
