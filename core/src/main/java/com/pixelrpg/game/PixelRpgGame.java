package com.pixelrpg.game;

import com.badlogic.gdx.Game;
import com.pixelrpg.game.screens.GameScreen;

/**
 * Application bootstrap. Gameplay runs inside Screen classes.
 */
public class PixelRpgGame extends Game {
    @Override
    public void create() {
        setScreen(new GameScreen());
    }
}
