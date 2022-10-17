package com.kaboos.game;

import com.badlogic.gdx.Game;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Kaboos extends Game {

	public BitmapFont font;
	public SpriteBatch batch;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		font = new BitmapFont(); // use libGDX's default Arial font
		font.getData().setScale(0.4f, 0.4f);
		this.setScreen(new MainMenuScreen(this));
	}

	@Override
	public void render () {
		super.render();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		font.dispose();
	}
}
