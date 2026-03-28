package com.pixelrpg.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.pixelrpg.game.intro.IntroPlaybackService;
import com.pixelrpg.game.screens.CharacterNameScreen;
import com.pixelrpg.game.screens.GameScreen;
import com.pixelrpg.game.screens.IntroScreen;
import com.pixelrpg.game.screens.LoadGameScreen;
import com.pixelrpg.game.screens.MainMenuScreen;
import com.pixelrpg.game.save.SaveGameData;
import com.pixelrpg.game.save.SaveGameService;

/**
 * Application bootstrap. Gameplay runs inside Screen classes.
 */
public class PixelRpgGame extends Game {
    private static final String DEFAULT_CHARACTER_NAME = "Character";
    private static final String NEW_GAME_START_MAP_ID = "inn-room";
    private static final String NEW_GAME_START_SPAWN_LAYER = "FIRST-SPAWN";

    private final IntroPlaybackService introPlaybackService;
    private String pendingNewGameCharacterName = DEFAULT_CHARACTER_NAME;

    public PixelRpgGame() {
        this(IntroPlaybackService.NO_OP);
    }

    public PixelRpgGame(IntroPlaybackService introPlaybackService) {
        this.introPlaybackService = introPlaybackService == null
                ? IntroPlaybackService.NO_OP
                : introPlaybackService;
    }

    @Override
    public void create() {
        showMainMenu();
    }

    public void showMainMenu() {
        replaceScreen(new MainMenuScreen(this));
    }

    public void startGame() {
        replaceScreen(new CharacterNameScreen(this));
    }

    public void confirmNewGameCharacterName(String characterName) {
        pendingNewGameCharacterName = normalizeCharacterName(characterName);
        if (introPlaybackService.isAvailable()) {
            replaceScreen(new IntroScreen(this, introPlaybackService));
            return;
        }
        startGameAfterIntro();
    }

    public void showLoadGame() {
        replaceScreen(new LoadGameScreen(this));
    }

    public void loadGame(String slotId) {
        SaveGameData saveData = SaveGameService.load(slotId);
        if (saveData == null) {
            showLoadGame();
            return;
        }
        replaceScreen(new GameScreen(saveData));
    }

    public void startNewGame() {
        startGame();
    }

    public void startGameAfterIntro() {
        replaceScreen(createNewGameScreen(pendingNewGameCharacterName));
        pendingNewGameCharacterName = DEFAULT_CHARACTER_NAME;
    }

    private void replaceScreen(Screen nextScreen) {
        Screen previousScreen = getScreen();
        setScreen(nextScreen);
        if (previousScreen != null) {
            previousScreen.dispose();
        }
    }

    private GameScreen createNewGameScreen(String characterName) {
        return new GameScreen(NEW_GAME_START_MAP_ID, NEW_GAME_START_SPAWN_LAYER,
                normalizeCharacterName(characterName));
    }

    private String normalizeCharacterName(String characterName) {
        if (characterName == null) {
            return DEFAULT_CHARACTER_NAME;
        }
        String normalized = characterName.trim().replaceAll("\\s+", " ");
        return normalized.isEmpty() ? DEFAULT_CHARACTER_NAME : normalized;
    }
}
