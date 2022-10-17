package com.kaboos.game;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.Objects;

public class OptionsScreen implements Screen, ApplicationListener {
    private final Kaboos game;
    private final Skin skin;
    private final Stage stage;
    private final OrthographicCamera camera;
    private final float buttonX = Gdx.graphics.getWidth()/2 - (Gdx.graphics.getWidth()/10)/2;
    private float buttonY = (Gdx.graphics.getHeight() - Gdx.graphics.getHeight()/12);
    private final SelectBox difficultyChoice;

    public OptionsScreen(final Kaboos game){
        this.game = game;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        stage = new Stage();

        title();
        optionsAudio(0);
        difficultyChoice = optionsDifficulty();
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
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
        game.batch.end();
        //Difficulty is set for the game if it is ever changed in the options
        if (Constants.difficulty!=difficultyChoice.getSelected().toString()) {
            if (Constants.soundOn){
                Constants.select.play(Constants.soundVolume);
            }
            Constants.difficulty = difficultyChoice.getSelected().toString();
            if (Objects.equals(Constants.difficulty, "Kaboos")){
                Constants.combatMusic = Gdx.audio.newMusic(Gdx.files.internal("Music/combat2.ogg"));
            }else{
                Constants.combatMusic = Gdx.audio.newMusic(Gdx.files.internal("Music/combat.ogg"));
            }
        }
    }

    @Override
    public void create() {

    }

    @Override
    public void resize(int i, int i1) {

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
        skin.dispose();
        stage.dispose();
    }
    /*
        Recursive function to create the options buttons
        */
    public void optionsAudio(int n){
        final CheckBox box;
        final Slider slider;
        slider = new Slider(0f,100f,10f,false,skin);
        if (n==0){
            box = new CheckBox("Sound", skin, "default");
            box.setChecked(Constants.soundOn);
            box.addListener(new ClickListener(){
                @Override
                public void clicked(InputEvent event, float x, float y){
                    Constants.toggle.play(Constants.soundVolume);
                    Constants.soundOn = box.isChecked();
                }
            });
            slider.setVisualPercent(Constants.soundVolume);

            slider.addListener(new ClickListener(){
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {return true;}

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button){
                    Constants.soundVolume = slider.getVisualPercent();
                }
            });
        }else if(n==1){
            box = new CheckBox("Music", skin, "default");
            box.setChecked(Constants.musicOn);
            box.addListener(new ClickListener(){
                @Override
                public void clicked(InputEvent event, float x, float y){
                    if (!box.isChecked()){
                        Constants.menuMusic.pause();
                    }else{
                        Constants.menuMusic.play();
                    }
                    Constants.musicOn = box.isChecked();
                    if (Constants.soundOn){
                        Constants.toggle.play(Constants.soundVolume);
                    }
                }
            });

            slider.setVisualPercent(Constants.musicVolume);
            slider.addListener(new ClickListener(){
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {return true;}

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button){
                    Constants.musicVolume = slider.getVisualPercent();
                    Constants.menuMusic.setVolume(Constants.musicVolume);
                }
            });
        }else{
            return;
        }

        box.setWidth(Gdx.graphics.getWidth()/10);
        box.setHeight(Gdx.graphics.getHeight()/12);
        box.setPosition(buttonX, buttonY);
        //box.setDebug(true);
        stage.addActor(box);

        slider.setWidth(box.getWidth());
        slider.setHeight(box.getHeight()/4);
        buttonY = buttonY - slider.getHeight();
        slider.setPosition(buttonX, buttonY);
        //slider.setDebug(true);
        stage.addActor(slider);


        buttonY = buttonY - box.getHeight()*1.1f;
        optionsAudio(n+1);
    }

    public SelectBox optionsDifficulty(){
        final SelectBox<String> box;
        box = new SelectBox<String>(skin, "default");
        String[] difficulty = new String[]{"Easy","Medium","Hard","Kaboos"};
        box.setItems(difficulty);
        box.setSelected(Constants.difficulty);

        box.setWidth(Gdx.graphics.getWidth()/10);
        box.setHeight(Gdx.graphics.getHeight()/12);
        box.setPosition(buttonX, buttonY);
        box.setAlignment(Align.center);
        //box.setDebug(true);

        stage.addActor(box);
        buttonY = buttonY - box.getHeight()*1.1f;
        return(box);
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

    public void title(){
        final Label title;
        title = new Label("Options",skin,"default");
        title.setWidth(Gdx.graphics.getWidth()/6);
        title.setHeight(Gdx.graphics.getHeight()/7);
        title.setPosition(Gdx.graphics.getWidth()/2 - title.getWidth()/2, Gdx.graphics.getHeight() - title.getHeight());
        title.setAlignment(Align.center);
        title.setFontScale(2);
        stage.addActor(title);
        buttonY = buttonY - title.getHeight()*1.1f;
    }
}
