package com.pixelrpg.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.pixelrpg.game.intro.IntroPlaybackService;
import com.pixelrpg.game.settings.GameSettingsService;
import com.pixelrpg.game.screens.CharacterNameScreen;
import com.pixelrpg.game.screens.DifficultySelectScreen;
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
    private static final GameDifficulty DEFAULT_NEW_GAME_DIFFICULTY = GameDifficulty.NORMAL;
    private static final String NEW_GAME_START_MAP_ID = "inn-room";
    private static final String NEW_GAME_START_SPAWN_LAYER = "FIRST-SPAWN";

    private final IntroPlaybackService introPlaybackService;
    private final IntroPlaybackService endingPlaybackService;
    private final GameSettingsService settingsService = new GameSettingsService();
    private String pendingNewGameCharacterName = DEFAULT_CHARACTER_NAME;
    private GameDifficulty pendingNewGameDifficulty = DEFAULT_NEW_GAME_DIFFICULTY;
    private Music persistentMusic;
    private float persistentMusicBaseVolume = 1f;

    public PixelRpgGame() {
        this(IntroPlaybackService.NO_OP, IntroPlaybackService.NO_OP);
    }

    public PixelRpgGame(IntroPlaybackService introPlaybackService) {
        this(introPlaybackService, IntroPlaybackService.NO_OP);
    }

    public PixelRpgGame(IntroPlaybackService introPlaybackService, IntroPlaybackService endingPlaybackService) {
        this.introPlaybackService = introPlaybackService == null
                ? IntroPlaybackService.NO_OP
                : introPlaybackService;
        this.endingPlaybackService = endingPlaybackService == null
                ? IntroPlaybackService.NO_OP
                : endingPlaybackService;
    }

    @Override
    public void create() {
        settingsService.initialize();
        showMainMenu();
    }

    public GameSettingsService getSettingsService() {
        return settingsService;
    }

    public void showMainMenu() {
        stopPersistentMusic(true);
        replaceScreen(new MainMenuScreen(this));
    }

    public void startGame() {
        startNewGame();
    }

    public void confirmNewGameCharacterName(String characterName) {
        pendingNewGameCharacterName = normalizeCharacterName(characterName);
        replaceScreen(new DifficultySelectScreen(
                this,
                pendingNewGameCharacterName,
                pendingNewGameDifficulty));
    }

    public void confirmNewGameDifficulty(GameDifficulty difficulty) {
        pendingNewGameDifficulty = difficulty == null ? DEFAULT_NEW_GAME_DIFFICULTY : difficulty;
        if (introPlaybackService.isAvailable()) {
            replaceScreen(new IntroScreen(this, introPlaybackService));
            return;
        }
        startGameAfterIntro();
    }

    public void showLoadGame() {
        stopPersistentMusic(true);
        replaceScreen(new LoadGameScreen(this));
    }

    public void loadGame(String slotId) {
        stopPersistentMusic(true);
        SaveGameData saveData = SaveGameService.load(slotId);
        if (saveData == null) {
            showLoadGame();
            return;
        }
        replaceScreen(new GameScreen(this, saveData));
    }

    public void startNewGame() {
        pendingNewGameCharacterName = DEFAULT_CHARACTER_NAME;
        pendingNewGameDifficulty = DEFAULT_NEW_GAME_DIFFICULTY;
        stopPersistentMusic(true);
        replaceScreen(new CharacterNameScreen(this, ""));
    }

    public void returnToCharacterNameSelection() {
        replaceScreen(new CharacterNameScreen(this, pendingNewGameCharacterName));
    }

    public void startGameAfterIntro() {
        stopPersistentMusic(true);
        replaceScreen(createNewGameScreen(pendingNewGameCharacterName));
        pendingNewGameCharacterName = DEFAULT_CHARACTER_NAME;
        pendingNewGameDifficulty = DEFAULT_NEW_GAME_DIFFICULTY;
    }

    public void handoffPersistentMusic(Music music, float baseVolume) {
        if (persistentMusic != null && persistentMusic != music) {
            try {
                persistentMusic.stop();
            } catch (Exception ignored) {
            }
            try {
                persistentMusic.dispose();
            } catch (Exception ignored) {
            }
        }
        persistentMusic = music;
        persistentMusicBaseVolume = baseVolume <= 0f ? 1f : baseVolume;
    }

    public void playEndingVideo() {
        if (endingPlaybackService.isAvailable()) {
            replaceScreen(new IntroScreen(
                    this,
                    endingPlaybackService,
                    new Runnable() {
                        @Override
                        public void run() {
                            showMainMenu();
                        }
                    },
                    false));
            return;
        }
        showMainMenu();
    }

    @Override
    public void render() {
        applyPersistentMusicVolume();
        super.render();
    }

    private void applyPersistentMusicVolume() {
        if (persistentMusic == null) {
            return;
        }
        settingsService.applyMusicVolume(persistentMusic, persistentMusicBaseVolume);
    }

    private void stopPersistentMusic(boolean dispose) {
        if (persistentMusic == null) {
            return;
        }
        try {
            persistentMusic.stop();
        } catch (Exception ignored) {
        }
        if (dispose) {
            try {
                persistentMusic.dispose();
            } catch (Exception ignored) {
            }
        }
        persistentMusic = null;
    }

    private void replaceScreen(Screen nextScreen) {
        Screen previousScreen = getScreen();
        setScreen(nextScreen);
        if (previousScreen != null) {
            previousScreen.dispose();
        }
    }

    private GameScreen createNewGameScreen(String characterName) {
        return new GameScreen(this, NEW_GAME_START_MAP_ID, NEW_GAME_START_SPAWN_LAYER,
                normalizeCharacterName(characterName), pendingNewGameDifficulty);
    }

    private String normalizeCharacterName(String characterName) {
        if (characterName == null) {
            return DEFAULT_CHARACTER_NAME;
        }
        String normalized = characterName.trim().replaceAll("\\s+", " ");
        return normalized.isEmpty() ? DEFAULT_CHARACTER_NAME : normalized;
    }
}
