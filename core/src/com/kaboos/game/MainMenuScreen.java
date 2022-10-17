package com.kaboos.game;


import com.badlogic.gdx.ApplicationListener;
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

public class MainMenuScreen implements Screen, ApplicationListener {
    private final Kaboos game;
    private final Skin skin;
    private final Stage stage;
    private final float buttonX = Gdx.graphics.getWidth() /2 - (Gdx.graphics.getWidth()/6)/2;
    private float buttonY = (Gdx.graphics.getHeight() - Gdx.graphics.getHeight()/7);

    OrthographicCamera camera;

    public MainMenuScreen(final Kaboos game)  {
        this.game = game;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        skin = new Skin(Gdx.files.internal("uiskin.json"));
        stage = new Stage();

        title();
        mainMenu(0);
        Gdx.input.setInputProcessor(stage);

        Constants.menuMusic.setLooping(true);
    }

    @Override
    public void show() {
        if (Constants.musicOn){
            Constants.menuMusic.setVolume(Constants.musicVolume);//Volume is in a range of 0-1
            Constants.menuMusic.play();
        }
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
    public void create() {

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void render() {

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
        stage.dispose();
        skin.dispose();
    }

    /*
    Recursive function to create the main menu buttons
    */
    public void mainMenu(int n){
        final TextButton button;
        if (n==0){
            button = new TextButton("Start Game", skin, "default");
            button.addListener(new ClickListener(){
                @Override
                public void clicked(InputEvent event, float x, float y){
                    if (Constants.soundOn){
                        Constants.button.play(Constants.soundVolume);
                    }
                    game.setScreen(new StoryScreen(game));
                    dispose();
                }
            });
        }else if(n==1){
            button = new TextButton("Character Select & Controls", skin, "default");
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
        }else if(n==2){
            button = new TextButton("Options", skin, "default");
            button.addListener(new ClickListener(){
                @Override
                public void clicked(InputEvent event, float x, float y){
                    if (Constants.soundOn){
                        Constants.button.play(Constants.soundVolume);
                    }
                    game.setScreen(new OptionsScreen(game));
                    dispose();
                }
            });
        }else if(n==3){
            button = new TextButton("Exit Game", skin, "default");
            button.addListener(new ClickListener(){
                @Override
                public void clicked(InputEvent event, float x, float y){
                    if (Constants.soundOn){
                        Constants.button.play(Constants.soundVolume);
                    }
                    Gdx.app.exit();
                    dispose();
                }
            });
        }else{
            return;
        }

        button.setWidth(Gdx.graphics.getWidth()/6);
        button.setHeight(Gdx.graphics.getHeight()/7);
        button.setPosition(buttonX, buttonY);
        stage.addActor(button);
        buttonY = buttonY - button.getHeight()*1.1f;
        mainMenu(n+1);
    }
    public void title(){
        final Label title;
        title = new Label("Kaboos \nRogue-Like 2D Game",skin,"default");
        title.setWidth(Gdx.graphics.getWidth()/6);
        title.setHeight(Gdx.graphics.getHeight()/7);
        title.setPosition(buttonX, buttonY);
        title.setAlignment(Align.center);
        title.setFontScale(2);
        stage.addActor(title);
        buttonY = buttonY - title.getHeight()*1.1f;
    }
}

