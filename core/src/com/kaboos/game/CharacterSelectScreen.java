package com.kaboos.game;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;

public class CharacterSelectScreen implements Screen, ApplicationListener {
    private final Kaboos game;
    private Skin skin;
    private Stage stage;
    private OrthographicCamera camera;
    private float buttonX = Gdx.graphics.getWidth()/2 - (Gdx.graphics.getWidth()/10)/2;
    private float buttonY = (Gdx.graphics.getHeight() - Gdx.graphics.getHeight()/12);
    private SelectBox selectedPlayerType;

    public CharacterSelectScreen(final Kaboos game){
        this.game = game;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        stage = new Stage();

        title();
        controls();
        selectedPlayerType = characterSelection();
        back();

        Gdx.input.setInputProcessor(stage);
    }
    @Override
    public void create() {

    }

    @Override
    public void render() {

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

        if (Constants.playerType != selectedPlayerType.getSelected().toString()) {
            if (Constants.soundOn){
                Constants.select.play(Constants.soundVolume);
            }
            Constants.playerType = selectedPlayerType.getSelected().toString();
        }
    }

    @Override
    public void resize(int i, int i1) {

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

    public void title(){
        final Label title;
        title = new Label("Character Select & Controls",skin,"default");
        title.setWidth(Gdx.graphics.getWidth()/6);
        title.setHeight(Gdx.graphics.getHeight()/7);
        title.setPosition(Gdx.graphics.getWidth()/2 - title.getWidth()/2, Gdx.graphics.getHeight() - title.getHeight());
        title.setAlignment(Align.center);
        title.setFontScale(2);
        stage.addActor(title);
        buttonY = buttonY - title.getHeight()*1.1f;
    }

    public SelectBox characterSelection(){
        final SelectBox<String> box;
        box = new SelectBox<String>(skin, "default");
        String[] difficulty = new String[]{"Wizard","Archer","Knight"};
        box.setItems(difficulty);
        box.setSelected(Constants.playerType);

        box.setWidth(Gdx.graphics.getWidth()/10);
        box.setHeight(Gdx.graphics.getHeight()/12);
        box.setPosition(buttonX, buttonY);
        box.setAlignment(Align.center);
        //box.setDebug(true);

        stage.addActor(box);
        buttonY = buttonY - box.getHeight()*1.1f;
        return(box);
    }

    public void controls(){
        final TextButton button;
        button = new TextButton("Controls", skin, "default");
        button.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                if (Constants.soundOn){
                    Constants.button.play(Constants.soundVolume);
                }
                game.setScreen(new ControlsScreen(game));
                dispose();
            }
        });
        button.setWidth(Gdx.graphics.getWidth()/10);
        button.setHeight(Gdx.graphics.getHeight()/12);
        button.setPosition(buttonX, buttonY);
        //box.setDebug(true);

        stage.addActor(button);
        buttonY = buttonY - button.getHeight()*1.1f;
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
