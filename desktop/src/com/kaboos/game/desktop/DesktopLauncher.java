package com.kaboos.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.kaboos.game.Kaboos;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Kaboos";
		config.width = 160;
		config.height = 176;
		config.fullscreen = true;
		new LwjglApplication(new Kaboos(), config);
	}
}
