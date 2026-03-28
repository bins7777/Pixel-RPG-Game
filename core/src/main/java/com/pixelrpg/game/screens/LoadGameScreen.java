package com.pixelrpg.game.screens;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.pixelrpg.game.PixelRpgGame;
import com.pixelrpg.game.entities.PlayerLevelStats;
import com.pixelrpg.game.save.SaveGameData;
import com.pixelrpg.game.save.SaveGameService;

public class LoadGameScreen extends ScreenAdapter {
    private static final float REFERENCE_WIDTH = 1920f;
    private static final float REFERENCE_HEIGHT = 1080f;

    private static final String BACKGROUND_PATH = "ui/main_menu/BQ.png";
    private static final String TITLE_PATH = "ui/main_menu/Overlay Title Game.png";
    private static final String BUTTON_NORMAL_PATH = "ui/main_menu/Button Normal.png";
    private static final String BUTTON_HOVER_PATH = "ui/main_menu/Button Hover.png";
    private static final String FONT_PATH = "fonts/Berry Rotunda.ttf";
    private static final String BACK_SOUND_PATH = "sounds/menu/Menu-2.MP3";
    private static final float BACK_ACTION_SOUND_DELAY_SECONDS = 0.6f;
    private static final float SCREEN_FADE_DURATION_SECONDS = 0.7f;

    private static final float TITLE_WIDTH = 897f;
    private static final float TITLE_HEIGHT = 141f;
    private static final float TITLE_Y = 648f;
    private static final float TITLE_X_OFFSET = -43f;
    private static final float TITLE_TEXT_CENTER_Y = 718.37f;
    private static final int TITLE_FONT_SIZE = 47;
    private static final float TITLE_TEXT_MAX_WIDTH = TITLE_WIDTH * 0.76f;

    private static final float SAVE_ROW_WIDTH = 640f;
    private static final float SAVE_ROW_HEIGHT = 78f;
    private static final float SAVE_ROW_START_Y = 470f;
    private static final float SAVE_ROW_SPACING = 108f;
    private static final float DELETE_BUTTON_WIDTH = 138f;
    private static final float DELETE_BUTTON_HEIGHT = 42f;
    private static final float DELETE_BUTTON_RIGHT_PADDING = 18f;
    private static final float SAVE_TEXT_RIGHT_PADDING = 18f;
    private static final float BACK_BUTTON_WIDTH = 269f;
    private static final float BACK_BUTTON_HEIGHT = 54f;
    private static final float BACK_BUTTON_Y = 150f;

    private static final int SAVE_TITLE_FONT_SIZE = 24;
    private static final int SAVE_META_FONT_SIZE = 16;
    private static final int BACK_BUTTON_FONT_SIZE = 20;
    private static final DateTimeFormatter SAVE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.US);

    private final PixelRpgGame game;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final Viewport viewport = new FitViewport(REFERENCE_WIDTH, REFERENCE_HEIGHT, camera);
    private final Vector3 pointer = new Vector3();
    private final GlyphLayout layout = new GlyphLayout();
    private final Array<LoadButton> buttons = new Array<>();

    private SpriteBatch batch;
    private Texture backgroundTexture;
    private Texture titleTexture;
    private Texture buttonNormalTexture;
    private Texture buttonHoverTexture;
    private Texture fadeTexture;
    private BitmapFont titleFont;
    private BitmapFont saveTitleFont;
    private BitmapFont saveMetaFont;
    private BitmapFont backButtonFont;
    private Music backSound;
    private InputAdapter inputProcessor;
    private int hoveredButtonIndex = -1;
    private int hoveredDeleteIndex = -1;
    private int selectedButtonIndex = 0;
    private int pressedButtonIndex = -1;
    private boolean pressedDeleteButton = false;
    private Runnable queuedAction;
    private float queuedActionDelay = 0f;
    private float fadeAlpha = 1f;
    private boolean fadeOutActive = false;

    public LoadGameScreen(PixelRpgGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        loadAssets();
        rebuildButtons();
        installInputProcessor();
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        hoveredButtonIndex = buttonIndexAt(Gdx.input.getX(), Gdx.input.getY());
        hoveredDeleteIndex = deleteButtonIndexAt(Gdx.input.getX(), Gdx.input.getY());
        if (hoveredButtonIndex >= 0) {
            selectedButtonIndex = hoveredButtonIndex;
        }
    }

    @Override
    public void render(float delta) {
        if (queuedAction != null) {
            queuedActionDelay -= delta;
            if (queuedActionDelay <= 0f) {
                Runnable action = queuedAction;
                queuedAction = null;
                queuedActionDelay = 0f;
                action.run();
                return;
            }
        }

        if (fadeOutActive) {
            fadeAlpha = Math.min(1f, fadeAlpha + delta / SCREEN_FADE_DURATION_SECONDS);
        } else if (fadeAlpha > 0f) {
            fadeAlpha = Math.max(0f, fadeAlpha - delta / SCREEN_FADE_DURATION_SECONDS);
        }

        int liveHoverIndex = buttonIndexAt(Gdx.input.getX(), Gdx.input.getY());
        hoveredDeleteIndex = deleteButtonIndexAt(Gdx.input.getX(), Gdx.input.getY());
        if (liveHoverIndex >= 0) {
            hoveredButtonIndex = liveHoverIndex;
            selectedButtonIndex = liveHoverIndex;
        } else {
            hoveredButtonIndex = -1;
        }

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply(true);
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        batch.draw(backgroundTexture, 0f, 0f, REFERENCE_WIDTH, REFERENCE_HEIGHT);

        float titleX = (REFERENCE_WIDTH - TITLE_WIDTH) * 0.5f + TITLE_X_OFFSET;
        batch.draw(titleTexture, titleX, TITLE_Y, TITLE_WIDTH, TITLE_HEIGHT);
        drawCenteredText(titleFont, "Load Game", REFERENCE_WIDTH * 0.5f, TITLE_TEXT_CENTER_Y, 0.92f, 0.92f, 0.92f, 1f);

        boolean hasSaveEntries = false;
        for (int i = 0; i < buttons.size; i++) {
            LoadButton button = buttons.get(i);
            boolean hovered = hoveredButtonIndex == i;
            boolean active = (hoveredButtonIndex >= 0 ? hoveredButtonIndex : selectedButtonIndex) == i;
            Texture texture = hovered && buttonHoverTexture != null ? buttonHoverTexture : buttonNormalTexture;
            batch.draw(texture, button.bounds.x, button.bounds.y, button.bounds.width, button.bounds.height);

            if (button.saveData != null) {
                hasSaveEntries = true;
                drawSaveButtonText(button, active);
                drawDeleteButton(button, hoveredDeleteIndex == i);
            } else {
                drawCenteredText(backButtonFont, button.label,
                        button.bounds.x + button.bounds.width * 0.5f,
                        button.bounds.y + button.bounds.height * 0.5f + 3f,
                        active ? 0.96f : 0.87f,
                        active ? 0.96f : 0.87f,
                        active ? 0.96f : 0.87f,
                        1f);
            }
        }

        if (!hasSaveEntries) {
            drawCenteredText(saveTitleFont, "No saves available yet.",
                    REFERENCE_WIDTH * 0.5f, 390f, 0.88f, 0.88f, 0.88f, 1f);
            drawCenteredText(saveMetaFont, "Start the game once and an autosave will appear here.",
                    REFERENCE_WIDTH * 0.5f, 350f, 0.72f, 0.72f, 0.72f, 1f);
        }
        if (fadeAlpha > 0f && fadeTexture != null) {
            batch.setColor(0f, 0f, 0f, fadeAlpha);
            batch.draw(fadeTexture, 0f, 0f, REFERENCE_WIDTH, REFERENCE_HEIGHT);
            batch.setColor(1f, 1f, 1f, 1f);
        }
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void hide() {
        if (Gdx.input.getInputProcessor() == inputProcessor) {
            Gdx.input.setInputProcessor(null);
        }
    }

    @Override
    public void dispose() {
        hide();
        if (batch != null) batch.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (titleTexture != null) titleTexture.dispose();
        if (buttonNormalTexture != null) buttonNormalTexture.dispose();
        if (buttonHoverTexture != null && buttonHoverTexture != buttonNormalTexture) buttonHoverTexture.dispose();
        if (fadeTexture != null) fadeTexture.dispose();
        if (titleFont != null) titleFont.dispose();
        if (saveTitleFont != null) saveTitleFont.dispose();
        if (saveMetaFont != null) saveMetaFont.dispose();
        if (backButtonFont != null) backButtonFont.dispose();
        if (backSound != null) backSound.dispose();
    }

    private void loadAssets() {
        if (batch != null) {
            return;
        }
        batch = new SpriteBatch();
        backgroundTexture = loadTexture(BACKGROUND_PATH);
        titleTexture = loadTexture(TITLE_PATH);
        buttonNormalTexture = loadTexture(BUTTON_NORMAL_PATH);
        buttonHoverTexture = Gdx.files.internal(BUTTON_HOVER_PATH).exists()
                ? loadTexture(BUTTON_HOVER_PATH)
                : buttonNormalTexture;
        fadeTexture = createFadeTexture();
        titleFont = loadFont(TITLE_FONT_SIZE);
        saveTitleFont = loadFont(SAVE_TITLE_FONT_SIZE);
        saveMetaFont = loadFont(SAVE_META_FONT_SIZE);
        backButtonFont = loadFont(BACK_BUTTON_FONT_SIZE);
        backSound = loadOptionalMusic(BACK_SOUND_PATH);
        titleFont.setUseIntegerPositions(false);
        saveTitleFont.setUseIntegerPositions(false);
        saveMetaFont.setUseIntegerPositions(false);
        backButtonFont.setUseIntegerPositions(false);
        fadeAlpha = 1f;
        fadeOutActive = false;
    }

    private void rebuildButtons() {
        buttons.clear();
        Array<SaveGameData> saves = SaveGameService.listSaves();
        float saveX = (REFERENCE_WIDTH - SAVE_ROW_WIDTH) * 0.5f;
        int visibleSaves = Math.min(4, saves.size);
        for (int i = 0; i < visibleSaves; i++) {
            float y = SAVE_ROW_START_Y - SAVE_ROW_SPACING * i;
            Rectangle bounds = new Rectangle(saveX, y, SAVE_ROW_WIDTH, SAVE_ROW_HEIGHT);
            buttons.add(new LoadButton(
                    bounds,
                    createDeleteButtonBounds(bounds),
                    saves.get(i),
                    saves.get(i).displayName));
        }

        float backX = (REFERENCE_WIDTH - BACK_BUTTON_WIDTH) * 0.5f;
        buttons.add(new LoadButton(
                new Rectangle(backX, BACK_BUTTON_Y, BACK_BUTTON_WIDTH, BACK_BUTTON_HEIGHT),
                null,
                null,
                "Back"));

        selectedButtonIndex = 0;
        hoveredButtonIndex = -1;
        hoveredDeleteIndex = -1;
        pressedButtonIndex = -1;
        pressedDeleteButton = false;
    }

    private void installInputProcessor() {
        inputProcessor = new InputAdapter() {
            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                hoveredButtonIndex = buttonIndexAt(screenX, screenY);
                hoveredDeleteIndex = deleteButtonIndexAt(screenX, screenY);
                if (hoveredButtonIndex >= 0) {
                    selectedButtonIndex = hoveredButtonIndex;
                }
                return hoveredButtonIndex >= 0;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (button != Input.Buttons.LEFT) {
                    return false;
                }
                pressedButtonIndex = buttonIndexAt(screenX, screenY);
                if (pressedButtonIndex >= 0) {
                    selectedButtonIndex = pressedButtonIndex;
                    pressedDeleteButton = deleteButtonContains(buttons.get(pressedButtonIndex), screenX, screenY);
                    return true;
                }
                pressedDeleteButton = false;
                return false;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if (button != Input.Buttons.LEFT) {
                    return false;
                }
                int releaseIndex = buttonIndexAt(screenX, screenY);
                boolean handled = pressedButtonIndex >= 0 && pressedButtonIndex == releaseIndex;
                if (handled) {
                    LoadButton releasedButton = buttons.get(releaseIndex);
                    if (pressedDeleteButton && deleteButtonContains(releasedButton, screenX, screenY)) {
                        queueDeleteAction(releasedButton);
                    } else {
                        queueButtonAction(releasedButton);
                    }
                }
                pressedButtonIndex = -1;
                pressedDeleteButton = false;
                return handled;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (buttons.size == 0) {
                    return false;
                }
                if (keycode == Input.Keys.UP || keycode == Input.Keys.W) {
                    hoveredButtonIndex = -1;
                    selectedButtonIndex = wrapIndex(selectedButtonIndex - 1);
                    return true;
                }
                if (keycode == Input.Keys.DOWN || keycode == Input.Keys.S) {
                    hoveredButtonIndex = -1;
                    selectedButtonIndex = wrapIndex(selectedButtonIndex + 1);
                    return true;
                }
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
                    queueButtonAction(buttons.get(selectedButtonIndex));
                    return true;
                }
                if (keycode == Input.Keys.FORWARD_DEL) {
                    queueDeleteAction(buttons.get(selectedButtonIndex));
                    return true;
                }
                if (keycode == Input.Keys.ESCAPE) {
                    queueBackAction();
                    return true;
                }
                return false;
            }
        };
        Gdx.input.setInputProcessor(inputProcessor);
    }

    private void queueButtonAction(LoadButton button) {
        if (queuedAction != null) {
            return;
        }
        if (button.saveData == null) {
            queueBackAction();
            return;
        }
        queuedAction = new Runnable() {
            @Override
            public void run() {
                game.loadGame(button.saveData.slotId);
            }
        };
        queuedActionDelay = SCREEN_FADE_DURATION_SECONDS;
        fadeOutActive = true;
    }

    private void queueDeleteAction(LoadButton button) {
        if (queuedAction != null || button.saveData == null) {
            return;
        }
        queuedAction = new Runnable() {
            @Override
            public void run() {
                if (SaveGameService.delete(button.saveData.slotId)) {
                    rebuildButtons();
                    hoveredButtonIndex = buttonIndexAt(Gdx.input.getX(), Gdx.input.getY());
                    hoveredDeleteIndex = deleteButtonIndexAt(Gdx.input.getX(), Gdx.input.getY());
                    if (hoveredButtonIndex >= 0) {
                        selectedButtonIndex = hoveredButtonIndex;
                    }
                }
            }
        };
        queuedActionDelay = 0f;
    }

    private void drawSaveButtonText(LoadButton button, boolean active) {
        float textLeft = button.bounds.x + 26f;
        float titleBaseline = button.bounds.y + button.bounds.height * 0.68f;
        float metaBaseline = button.bounds.y + button.bounds.height * 0.35f;
        float titleRight = button.deleteBounds != null
                ? button.deleteBounds.x - SAVE_TEXT_RIGHT_PADDING
                : button.bounds.x + button.bounds.width - 24f;

        drawLeftText(saveTitleFont, button.label, textLeft, titleBaseline,
                active ? 0.96f : 0.88f, active ? 0.96f : 0.88f, active ? 0.96f : 0.88f, 1f);
        drawRightText(saveMetaFont, formatSavedAt(button.saveData), titleRight, titleBaseline,
                0.74f, 0.74f, 0.74f, 1f);
        drawLeftText(saveMetaFont, formatSaveDetails(button.saveData), textLeft, metaBaseline,
                0.72f, 0.72f, 0.72f, 1f);
    }

    private String formatSaveDetails(SaveGameData saveData) {
        int displayLevel = PlayerLevelStats.clampLevel(saveData.playerLevel);
        int displayMaxHealth = Math.round(PlayerLevelStats.maxHealthForLevel(displayLevel));
        int displayMaxStamina = Math.round(PlayerLevelStats.maxStaminaForLevel(displayLevel));
        int displayCurrentHealth = Math.round(scaleSavedResourceForDisplay(
                saveData.currentHealth, saveData.maxHealth, displayMaxHealth));
        int displayCurrentStamina = Math.round(scaleSavedResourceForDisplay(
                saveData.currentMana, saveData.maxMana, displayMaxStamina));
        return "Map: " + prettifyMapId(saveData.mapId)
                + "  HP: " + displayCurrentHealth + "/" + displayMaxHealth
                + "  ST: " + displayCurrentStamina + "/" + displayMaxStamina
                + "  Lv " + displayLevel;
    }

    private float scaleSavedResourceForDisplay(float savedCurrentValue, float savedMaxValue, float displayMaxValue) {
        if (savedCurrentValue <= 0f) {
            return 0f;
        }
        if (savedMaxValue > 0f) {
            return displayMaxValue * Math.max(0f, Math.min(1f, savedCurrentValue / savedMaxValue));
        }
        return Math.min(displayMaxValue, savedCurrentValue);
    }

    private void drawDeleteButton(LoadButton button, boolean hovered) {
        if (button.deleteBounds == null) {
            return;
        }
        Texture texture = hovered && buttonHoverTexture != null ? buttonHoverTexture : buttonNormalTexture;
        batch.draw(texture, button.deleteBounds.x, button.deleteBounds.y, button.deleteBounds.width, button.deleteBounds.height);
        drawCenteredText(backButtonFont, "Delete",
                button.deleteBounds.x + button.deleteBounds.width * 0.5f,
                button.deleteBounds.y + button.deleteBounds.height * 0.5f + 3f,
                hovered ? 0.96f : 0.82f,
                hovered ? 0.48f : 0.32f,
                hovered ? 0.48f : 0.32f,
                1f);
    }

    private String formatSavedAt(SaveGameData saveData) {
        if (saveData.savedAtEpochMillis <= 0L) {
            return "";
        }
        return SAVE_TIME_FORMATTER.format(
                Instant.ofEpochMilli(saveData.savedAtEpochMillis).atZone(ZoneId.systemDefault()));
    }

    private String prettifyMapId(String mapId) {
        if (mapId == null || mapId.isBlank()) {
            return "Unknown";
        }
        String[] parts = mapId.replace('_', '-').split("-");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }
        return builder.toString();
    }

    private int buttonIndexAt(int screenX, int screenY) {
        pointer.set(screenX, screenY, 0f);
        viewport.unproject(pointer);
        for (int i = 0; i < buttons.size; i++) {
            if (buttons.get(i).bounds.contains(pointer.x, pointer.y)) {
                return i;
            }
        }
        return -1;
    }

    private int deleteButtonIndexAt(int screenX, int screenY) {
        pointer.set(screenX, screenY, 0f);
        viewport.unproject(pointer);
        for (int i = 0; i < buttons.size; i++) {
            Rectangle deleteBounds = buttons.get(i).deleteBounds;
            if (deleteBounds != null && deleteBounds.contains(pointer.x, pointer.y)) {
                return i;
            }
        }
        return -1;
    }

    private boolean deleteButtonContains(LoadButton button, int screenX, int screenY) {
        if (button.deleteBounds == null) {
            return false;
        }
        pointer.set(screenX, screenY, 0f);
        viewport.unproject(pointer);
        return button.deleteBounds.contains(pointer.x, pointer.y);
    }

    private int wrapIndex(int index) {
        return (index % buttons.size + buttons.size) % buttons.size;
    }

    private void drawCenteredText(BitmapFont font, String text, float centerX, float centerY, float r, float g, float b, float a) {
        if (font == null || text == null) {
            return;
        }
        float originalScaleX = font.getData().scaleX;
        float originalScaleY = font.getData().scaleY;
        if (font == titleFont) {
            font.getData().setScale(1f);
            layout.setText(font, text);
            float fitScale = layout.width > TITLE_TEXT_MAX_WIDTH ? TITLE_TEXT_MAX_WIDTH / layout.width : 1f;
            font.getData().setScale(fitScale);
        }
        layout.setText(font, text);
        float textX = centerX - layout.width * 0.5f;
        float textY = centerY + layout.height * 0.5f;
        drawText(font, text, textX, textY, r, g, b, a);
        font.getData().setScale(originalScaleX, originalScaleY);
    }

    private void drawLeftText(BitmapFont font, String text, float x, float baselineY, float r, float g, float b, float a) {
        drawText(font, text, x, baselineY, r, g, b, a);
    }

    private void drawRightText(BitmapFont font, String text, float rightX, float baselineY, float r, float g, float b, float a) {
        layout.setText(font, text);
        drawText(font, text, rightX - layout.width, baselineY, r, g, b, a);
    }

    private void drawText(BitmapFont font, String text, float x, float y, float r, float g, float b, float a) {
        font.setColor(0f, 0f, 0f, 0.9f * a);
        font.draw(batch, text, x + 2f, y - 2f);
        font.setColor(r, g, b, a);
        font.draw(batch, text, x, y);
        font.setColor(1f, 1f, 1f, 1f);
    }

    private Texture loadTexture(String path) {
        Texture texture = new Texture(Gdx.files.internal(path));
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return texture;
    }

    private BitmapFont loadFont(int size) {
        if (!Gdx.files.internal(FONT_PATH).exists()) {
            return new BitmapFont();
        }
        FreeTypeFontGenerator generator = null;
        try {
            generator = new FreeTypeFontGenerator(Gdx.files.internal(FONT_PATH));
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = size;
            parameter.minFilter = Texture.TextureFilter.Linear;
            parameter.magFilter = Texture.TextureFilter.Linear;
            return generator.generateFont(parameter);
        } finally {
            if (generator != null) {
                generator.dispose();
            }
        }
    }

    private void queueBackAction() {
        if (queuedAction != null) {
            return;
        }
        queuedAction = new Runnable() {
            @Override
            public void run() {
                game.showMainMenu();
            }
        };
        queuedActionDelay = Math.max(BACK_ACTION_SOUND_DELAY_SECONDS, SCREEN_FADE_DURATION_SECONDS);
        fadeOutActive = true;
        playMusic(backSound);
    }

    private Music loadOptionalMusic(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        try {
            if (!Gdx.files.internal(path).exists()) {
                return null;
            }
            Music music = Gdx.audio.newMusic(Gdx.files.internal(path));
            music.setLooping(false);
            return music;
        } catch (Exception error) {
            Gdx.app.error("LoadGameScreen", "Failed to load sound: " + path, error);
            return null;
        }
    }

    private void playMusic(Music music) {
        if (music != null) {
            if (game != null && game.getSettingsService() != null) {
                game.getSettingsService().playMusicFromStart(music, 1f, false);
                return;
            }
            music.stop();
            music.setPosition(0f);
            music.setVolume(1f);
            music.play();
        }
    }

    private Texture createFadeTexture() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(1f, 1f, 1f, 1f);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return texture;
    }

    private Rectangle createDeleteButtonBounds(Rectangle saveBounds) {
        float x = saveBounds.x + saveBounds.width - DELETE_BUTTON_WIDTH - DELETE_BUTTON_RIGHT_PADDING;
        float y = saveBounds.y + (saveBounds.height - DELETE_BUTTON_HEIGHT) * 0.5f;
        return new Rectangle(x, y, DELETE_BUTTON_WIDTH, DELETE_BUTTON_HEIGHT);
    }

    private static final class LoadButton {
        private final Rectangle bounds;
        private final Rectangle deleteBounds;
        private final SaveGameData saveData;
        private final String label;

        private LoadButton(Rectangle bounds, Rectangle deleteBounds, SaveGameData saveData, String label) {
            this.bounds = bounds;
            this.deleteBounds = deleteBounds;
            this.saveData = saveData;
            this.label = label;
        }
    }
}
