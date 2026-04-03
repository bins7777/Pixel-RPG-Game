package com.pixelrpg.game.screens;

import java.io.Reader;
import java.util.Properties;

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
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.pixelrpg.game.PixelRpgGame;

public class MainMenuScreen extends ScreenAdapter {
    private static final float REFERENCE_WIDTH = 1920f;
    private static final float REFERENCE_HEIGHT = 1080f;

    private static final String BACKGROUND_PATH = "ui/main_menu/BQ.png";
    private static final String TITLE_PATH = "ui/main_menu/Overlay Title Game.png";
    private static final String BUTTON_NORMAL_PATH = "ui/main_menu/Button Normal.png";
    private static final String BUTTON_HOVER_PATH = "ui/main_menu/Button Hover.png";
    private static final String FONT_PATH = "fonts/Berry Rotunda.ttf";
    private static final String DESKTOP_CONFIG_PATH = "config/desktop.properties";
    private static final String DEFAULT_TITLE_TEXT = "Pixel RPG";
    private static final String MENU_CONFIRM_SOUND_PATH = "sounds/menu/Menu-1.MP3";
    private static final float MENU_ACTION_SOUND_DELAY_SECONDS = 0.6f;
    private static final float SCREEN_FADE_DURATION_SECONDS = 0.7f;

    private static final float TITLE_WIDTH = 897f;
    private static final float TITLE_HEIGHT = 141f;
    private static final float TITLE_Y = 648f;
    private static final int TITLE_FONT_SIZE = 47;
    private static final float TITLE_TEXT_MAX_WIDTH = TITLE_WIDTH * 0.76f;
    private static final float TITLE_TEXT_CENTER_Y = 718.37f;

    private static final float BUTTON_WIDTH = 269f;
    private static final float BUTTON_HEIGHT = 54f;
    private static final float BUTTON_START_Y = 478f;
    private static final float BUTTON_SPACING = 76f;
    private static final int BUTTON_FONT_SIZE = 20;

    private final PixelRpgGame game;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final Viewport viewport = new FitViewport(REFERENCE_WIDTH, REFERENCE_HEIGHT, camera);
    private final Vector3 pointer = new Vector3();
    private final GlyphLayout layout = new GlyphLayout();
    private final String titleText;
    private final Rectangle[] buttonBounds = new Rectangle[3];
    private final MenuItem[] menuItems = {
            new MenuItem("Start Game", MenuAction.START_GAME),
            new MenuItem("Load Game", MenuAction.LOAD_GAME),
            new MenuItem("Exit", MenuAction.EXIT)
    };

    private SpriteBatch batch;
    private Texture backgroundTexture;
    private Texture titleTexture;
    private Texture buttonNormalTexture;
    private Texture buttonHoverTexture;
    private Texture fadeTexture;
    private BitmapFont titleFont;
    private BitmapFont buttonFont;
    private Music confirmSound;
    private InputAdapter inputProcessor;
    private int selectedIndex = 0;
    private int hoveredIndex = -1;
    private int pressedIndex = -1;
    private MenuAction queuedAction;
    private float queuedActionDelay = 0f;
    private float fadeAlpha = 1f;
    private boolean fadeOutActive = false;

    public MainMenuScreen(PixelRpgGame game) {
        this.game = game;
        this.titleText = resolveTitleText();
        for (int i = 0; i < buttonBounds.length; i++) {
            buttonBounds[i] = new Rectangle();
        }
        rebuildLayout();
    }

    @Override
    public void show() {
        loadAssets();
        installInputProcessor();
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        hoveredIndex = buttonIndexAt(Gdx.input.getX(), Gdx.input.getY());
        if (hoveredIndex >= 0) {
            selectedIndex = hoveredIndex;
        }
    }

    @Override
    public void render(float delta) {
        if (queuedAction != null) {
            queuedActionDelay -= delta;
            if (queuedActionDelay <= 0f) {
                runQueuedAction();
                return;
            }
        }

        if (fadeOutActive) {
            fadeAlpha = Math.min(1f, fadeAlpha + delta / SCREEN_FADE_DURATION_SECONDS);
        } else if (fadeAlpha > 0f) {
            fadeAlpha = Math.max(0f, fadeAlpha - delta / SCREEN_FADE_DURATION_SECONDS);
        }

        int liveHoverIndex = buttonIndexAt(Gdx.input.getX(), Gdx.input.getY());
        if (liveHoverIndex >= 0) {
            hoveredIndex = liveHoverIndex;
            selectedIndex = liveHoverIndex;
        } else {
            hoveredIndex = -1;
        }

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply(true);
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        batch.draw(backgroundTexture, 0f, 0f, REFERENCE_WIDTH, REFERENCE_HEIGHT);

        float titleX = (REFERENCE_WIDTH - TITLE_WIDTH) * 0.5f - 43f;
        batch.draw(titleTexture, titleX, TITLE_Y, TITLE_WIDTH, TITLE_HEIGHT);
        drawCenteredTitleText(titleText, REFERENCE_WIDTH * 0.5f, TITLE_TEXT_CENTER_Y);

        for (int i = 0; i < menuItems.length; i++) {
            Rectangle bounds = buttonBounds[i];
            boolean hovered = hoveredIndex == i;
            boolean active = (hoveredIndex >= 0 ? hoveredIndex : selectedIndex) == i;
            Texture texture = hovered && buttonHoverTexture != null ? buttonHoverTexture : buttonNormalTexture;
            batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);
            drawCenteredText(
                    buttonFont,
                    menuItems[i].label,
                    bounds.x + bounds.width * 0.5f,
                    bounds.y + bounds.height * 0.5f + 3f,
                    active ? 0.96f : 0.87f,
                    active ? 0.96f : 0.87f,
                    active ? 0.96f : 0.87f,
                    1f);
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
        if (batch != null) {
            batch.dispose();
        }
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
        if (titleTexture != null) {
            titleTexture.dispose();
        }
        if (buttonNormalTexture != null) {
            buttonNormalTexture.dispose();
        }
        if (buttonHoverTexture != null) {
            if (buttonHoverTexture != buttonNormalTexture) {
                buttonHoverTexture.dispose();
            }
        }
        if (fadeTexture != null) {
            fadeTexture.dispose();
        }
        if (titleFont != null) {
            titleFont.dispose();
        }
        if (buttonFont != null) {
            buttonFont.dispose();
        }
        if (confirmSound != null) {
            confirmSound.dispose();
        }
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
        titleFont = loadFont(FONT_PATH, TITLE_FONT_SIZE);
        titleFont.setUseIntegerPositions(false);
        buttonFont = loadFont(FONT_PATH, BUTTON_FONT_SIZE);
        buttonFont.setUseIntegerPositions(false);
        confirmSound = loadOptionalMusic(MENU_CONFIRM_SOUND_PATH);
        fadeAlpha = 1f;
        fadeOutActive = false;
    }

    private void installInputProcessor() {
        inputProcessor = new InputAdapter() {
            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                hoveredIndex = buttonIndexAt(screenX, screenY);
                if (hoveredIndex >= 0) {
                    selectedIndex = hoveredIndex;
                }
                return hoveredIndex >= 0;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (button != Input.Buttons.LEFT) {
                    return false;
                }
                pressedIndex = buttonIndexAt(screenX, screenY);
                if (pressedIndex >= 0) {
                    selectedIndex = pressedIndex;
                    return true;
                }
                return false;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if (button != Input.Buttons.LEFT) {
                    return false;
                }
                int releaseIndex = buttonIndexAt(screenX, screenY);
                boolean handled = pressedIndex >= 0 && pressedIndex == releaseIndex;
                if (handled) {
                    beginQueuedAction(menuItems[releaseIndex].action);
                }
                pressedIndex = -1;
                return handled;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.UP || keycode == Input.Keys.W) {
                    hoveredIndex = -1;
                    selectedIndex = wrapIndex(selectedIndex - 1);
                    return true;
                }
                if (keycode == Input.Keys.DOWN || keycode == Input.Keys.S) {
                    hoveredIndex = -1;
                    selectedIndex = wrapIndex(selectedIndex + 1);
                    return true;
                }
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
                    beginQueuedAction(menuItems[selectedIndex].action);
                    return true;
                }
                if (keycode == Input.Keys.ESCAPE) {
                    beginQueuedAction(MenuAction.EXIT);
                    return true;
                }
                return false;
            }
        };
        Gdx.input.setInputProcessor(inputProcessor);
    }

    private void rebuildLayout() {
        float x = (REFERENCE_WIDTH - BUTTON_WIDTH) * 0.5f;
        for (int i = 0; i < buttonBounds.length; i++) {
            float y = BUTTON_START_Y - BUTTON_SPACING * i;
            buttonBounds[i].set(x, y, BUTTON_WIDTH, BUTTON_HEIGHT);
        }
    }

    private String resolveTitleText() {
        Properties properties = new Properties();
        if (Gdx.files.internal(DESKTOP_CONFIG_PATH).exists()) {
            try (Reader reader = Gdx.files.internal(DESKTOP_CONFIG_PATH).reader()) {
                properties.load(reader);
            } catch (Exception ignored) {
                // Fall back to the default menu title when config loading fails.
            }
        }

        String configuredTitle = properties.getProperty("title", DEFAULT_TITLE_TEXT).trim();
        if (configuredTitle.endsWith("(LibGDX)")) {
            configuredTitle = configuredTitle.substring(0, configuredTitle.length() - "(LibGDX)".length()).trim();
        }
        return configuredTitle.isEmpty() ? DEFAULT_TITLE_TEXT : configuredTitle;
    }

    private void drawCenteredTitleText(String text, float centerX, float centerY) {
        if (titleFont == null || text == null || text.isEmpty()) {
            return;
        }

        float originalScaleX = titleFont.getData().scaleX;
        float originalScaleY = titleFont.getData().scaleY;
        titleFont.getData().setScale(1f);
        layout.setText(titleFont, text);
        float fitScale = layout.width > TITLE_TEXT_MAX_WIDTH ? TITLE_TEXT_MAX_WIDTH / layout.width : 1f;
        titleFont.getData().setScale(fitScale);
        drawCenteredText(titleFont, text, centerX, centerY, 0.92f, 0.92f, 0.92f, 1f);
        titleFont.getData().setScale(originalScaleX, originalScaleY);
    }

    private void drawCenteredText(BitmapFont font, String text, float centerX, float centerY, float r, float g, float b, float a) {
        layout.setText(font, text);
        float textX = centerX - layout.width * 0.5f;
        float textY = centerY + layout.height * 0.5f;

        font.setColor(0f, 0f, 0f, 0.9f * a);
        font.draw(batch, text, textX + 2f, textY - 2f);
        font.setColor(r, g, b, a);
        font.draw(batch, text, textX, textY);
        font.setColor(1f, 1f, 1f, 1f);
    }

    private int buttonIndexAt(int screenX, int screenY) {
        pointer.set(screenX, screenY, 0f);
        viewport.unproject(pointer);
        float worldX = pointer.x;
        float worldY = pointer.y;
        for (int i = 0; i < buttonBounds.length; i++) {
            if (buttonBounds[i].contains(worldX, worldY)) {
                return i;
            }
        }
        return -1;
    }

    private int wrapIndex(int index) {
        int size = menuItems.length;
        return (index % size + size) % size;
    }

    private void runQueuedAction() {
        MenuAction action = queuedAction;
        queuedAction = null;
        queuedActionDelay = 0f;
        if (action == MenuAction.START_GAME) {
            game.startNewGame();
            return;
        }
        if (action == MenuAction.LOAD_GAME) {
            game.showLoadGame();
            return;
        }
        Gdx.app.exit();
    }

    private void beginQueuedAction(MenuAction action) {
        if (queuedAction != null) {
            return;
        }
        queuedAction = action;
        queuedActionDelay = Math.max(MENU_ACTION_SOUND_DELAY_SECONDS, SCREEN_FADE_DURATION_SECONDS);
        fadeOutActive = true;
        playMusic(confirmSound);
    }

    private Texture loadTexture(String path) {
        Texture texture = new Texture(Gdx.files.internal(path));
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return texture;
    }

    private BitmapFont loadFont(String fontPath, int size) {
        if (!Gdx.files.internal(fontPath).exists()) {
            return new BitmapFont();
        }

        FreeTypeFontGenerator generator = null;
        try {
            generator = new FreeTypeFontGenerator(Gdx.files.internal(fontPath));
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
            Gdx.app.error("MainMenuScreen", "Failed to load sound: " + path, error);
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

    private enum MenuAction {
        START_GAME,
        LOAD_GAME,
        EXIT
    }

    private static final class MenuItem {
        private final String label;
        private final MenuAction action;

        private MenuItem(String label, MenuAction action) {
            this.label = label;
            this.action = action;
        }
    }
}
