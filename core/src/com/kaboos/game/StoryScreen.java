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

public class StoryScreen implements Screen {
    private final Kaboos game;
    private final Skin skin;
    private final Stage stage;
    private final OrthographicCamera camera;
    private final float buttonX = Gdx.graphics.getWidth()/2 - (Gdx.graphics.getWidth()/10)/2;
    private float buttonY = (Gdx.graphics.getHeight() - Gdx.graphics.getHeight()/12);

    public StoryScreen(Kaboos game){
        this.game = game;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        skin = new Skin(Gdx.files.internal("uiskin.json"));
        stage = new Stage();

        title();
        story(0);
        start();
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

    public void title(){
        final Label title;
        title = new Label("Story",skin,"default");
        title.setWidth(Gdx.graphics.getWidth()/6);
        title.setHeight(Gdx.graphics.getHeight()/7);
        title.setPosition(Gdx.graphics.getWidth()/2 - title.getWidth()/2, Gdx.graphics.getHeight() - title.getHeight());
        title.setAlignment(Align.center);
        title.setFontScale(2);
        stage.addActor(title);
        buttonY = buttonY - title.getHeight()*1.1f;
    }

    public void story(int n){
        final Label text;
        if (n == 0) {
            text = new Label("There exists stories about an ancient, great evil that had the world under its dominion, bringing misery" +
                    " and doom to all.\n The myths were not conclusive on how or why it disappeared, only the name Ginger Beard was common in these tales,\n" +
                    " but more importantly was the fact that it might return.\n",skin,"default");
        } else if (n == 1) {
            text = new Label("Ginger Beard's followers plan to herald his return, only you can stop them.\n "+playerBackground(), skin, "default");
        } else if (n == 2) {
            text = new Label("Good Luck!", skin, "default");
        }else{
            return;
        }
        text.setWidth(Gdx.graphics.getWidth()/2);
        text.setHeight(Gdx.graphics.getHeight()/12);
        text.setPosition(buttonX*0.5f, buttonY);
        text.setFontScale(1.1f);
        stage.addActor(text);
        buttonY = buttonY - text.getHeight();

        story(n+1);
    }

    public String playerBackground(){
        switch (Constants.playerType){
            case "Knight":
                return "You are a veteran knight that spent their life defending their kingdom from man and monster alike,\n your experience and martial prowess" +
                        " will be tested.";
            case "Archer":
                return "You are a legendary marksman fabled to have hunted the most dangerous of monsters, your archery skills are second to none.\n" +
                        "Your aim and speed can cut down any threat.";
            default:
                return "You are a powerful wizard with mastery over the elements, use fire and ice to destroy those that stand in your way and\n end" +
                        " this threat before it gets out of hand.";
        }
    }

    public void start(){
        final TextButton button;
        button = new TextButton("Start", skin, "default");
        button.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                if (Constants.soundOn){
                    Constants.button.play(Constants.soundVolume);
                }
                Constants.menuMusic.pause();
                Constants.menuMusic.setPosition(0);
                game.setScreen(new GameScreen(game));
                dispose();
            }
        });
        button.setWidth(Gdx.graphics.getWidth()/14);
        button.setHeight(Gdx.graphics.getHeight()/14);
        button.setPosition(stage.getActors().get(0).getX()*1.1f, buttonY);
        stage.addActor(button);
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
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        });
        button.setWidth(Gdx.graphics.getWidth()/14);
        button.setHeight(Gdx.graphics.getHeight()/14);
        button.setPosition(0, 0);
        stage.addActor(button);
    }
}
