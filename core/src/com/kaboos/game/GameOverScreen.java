package com.kaboos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;

public class GameOverScreen implements Screen {
    private final Kaboos game;
    private final Skin skin;
    private final Stage stage;
    private final OrthographicCamera camera;
    private final float buttonX = Gdx.graphics.getWidth()/2 - (Gdx.graphics.getWidth()/10)/2;
    private float buttonY = (Gdx.graphics.getHeight() - Gdx.graphics.getHeight()/12);
    private final boolean victory;

    public GameOverScreen(Kaboos game, boolean victory){
        this.game = game;
        this.victory = victory;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        skin = new Skin(Gdx.files.internal("uiskin.json"));
        stage = new Stage();

        title();
        returnToMenu();

        Gdx.input.setInputProcessor(stage);
    }
    @Override
    public void show() {
        Constants.combatMusic.pause();
        Constants.combatMusic.setPosition(0);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
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

    public void title(){
        final Label title;
        if (victory){
            title = new Label("You have stopped Ginger Beard's return! \nCompleted on : " + Constants.difficulty,skin,"default");
        }else{
            title = new Label("Game Over! \nRoom Number : " +  Constants.roomNumber,skin,"default");
        }
        title.setWidth(Gdx.graphics.getWidth()/6);
        title.setHeight(Gdx.graphics.getHeight()/7);
        title.setPosition(Gdx.graphics.getWidth()/2 - title.getWidth()/2, Gdx.graphics.getHeight()/2 + title.getHeight());
        title.setAlignment(Align.center);
        title.setFontScale(2);
        stage.addActor(title);
        buttonY = title.getY();
    }

    public void returnToMenu(){
        final TextButton button;
        button = new TextButton("Return to Main Menu", skin, "default");
        button.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                if (Constants.soundOn){
                    Constants.button.setVolume(Constants.button.play(),Constants.soundVolume);
                }
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        });
        button.setWidth(Gdx.graphics.getWidth()/10);
        button.setHeight(Gdx.graphics.getHeight()/12);
        button.setPosition(buttonX,  buttonY*0.8f);
        //box.setDebug(true);

        stage.addActor(button);
        buttonY = buttonY - button.getHeight()*1.1f;
    }
}
