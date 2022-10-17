package com.kaboos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class Constants {
    public static final short PLAYER = 0x0001; // 0001 in binary
    public static final short WALL = 0x0002; // 0010 in binary
    public static final short ENEMIES = 0x0004;
    public static final short PLAYER_PROJECTILE = 0x0008;
    public static final short ENEMY_PROJECTILE = 0x0010;
    public static final short ITEM = 0x0012;
    public static final float P2M = 100f;//Rate of conversion from Pixels to Metres.
    public static float soundVolume = 1;
    public static float musicVolume = 1;
    public static boolean musicOn = true;
    public static boolean soundOn = true;
    public static String difficulty = "Easy";//Default Difficulty
    public static int gameWidth = 160;//Width of the game world in pixels
    public static int gameHeight = 176;//Height of the game world in pixels
    public static String playerType = "Wizard";//Default Class
    public static int roomNumber;
    public static GameScreen gameState;

    public static final Music menuMusic = Gdx.audio.newMusic(Gdx.files.internal("Music/Mystical_Theme.mp3"));;
    public static Music combatMusic = Gdx.audio.newMusic(Gdx.files.internal("Music/combat.ogg"));;

    public static final Sound button = Gdx.audio.newSound(Gdx.files.internal("Menu/button.wav"));
    public static final Sound select = Gdx.audio.newSound(Gdx.files.internal("Menu/select.wav"));
    public static final Sound toggle = Gdx.audio.newSound(Gdx.files.internal("Menu/toggle.wav"));

    public static final Sound potionSound = Gdx.audio.newSound(Gdx.files.internal("Items/potion.mp3"));
}
