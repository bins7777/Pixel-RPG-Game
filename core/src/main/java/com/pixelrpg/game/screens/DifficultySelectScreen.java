package com.pixelrpg.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
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
import com.pixelrpg.game.GameDifficulty;
import com.pixelrpg.game.PixelRpgGame;

public class DifficultySelectScreen extends ScreenAdapter {
    private static final float REFERENCE_WIDTH = 1920f;
    private static final float REFERENCE_HEIGHT = 1080f;

    private static final String BACKGROUND_PATH = "ui/main_menu/BQ.png";
    private static final String TITLE_PATH = "ui/main_menu/Overlay Title Game.png";
    private static final String BUTTON_NORMAL_PATH = "ui/main_menu/Button Normal.png";
    private static final String BUTTON_HOVER_PATH = "ui/main_menu/Button Hover.png";
    private static final String FONT_PATH = "fonts/Berry Rotunda.ttf";

    private static final float SCREEN_FADE_DURATION_SECONDS = 0.7f;

    private static final float TITLE_WIDTH = 897f;
    private static final float TITLE_HEIGHT = 141f;
    private static final float TITLE_Y = 648f;
    private static final float TITLE_X_OFFSET = -43f;
    private static final float TITLE_TEXT_CENTER_Y = 718.37f;
    private static final int TITLE_FONT_SIZE = 36;

    private static final float BUTTON_WIDTH = 340f;
    private static final float BUTTON_HEIGHT = 54f;
    private static final float BUTTON_START_Y = 470f;
    private static final float BUTTON_SPACING = 76f;
    private static final float BACK_BUTTON_Y = 128f;

    private static final int PROMPT_FONT_SIZE = 22;
    private static final int BUTTON_FONT_SIZE = 20;
    private static final int META_FONT_SIZE = 15;

    private static final GameDifficulty[] DIFFICULTIES = GameDifficulty.values();
    private static final int BACK_INDEX = DIFFICULTIES.length;

    private final PixelRpgGame game;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final Viewport viewport = new FitViewport(REFERENCE_WIDTH, REFERENCE_HEIGHT, camera);
    private final Vector3 pointer = new Vector3();
    private final GlyphLayout layout = new GlyphLayout();
    private final Rectangle[] buttonBounds = new Rectangle[DIFFICULTIES.length + 1];
    private final String characterName;

    private SpriteBatch batch;
    private Texture backgroundTexture;
    private Texture titleTexture;
    private Texture buttonNormalTexture;
    private Texture buttonHoverTexture;
    private Texture fillTexture;
    private BitmapFont titleFont;
    private BitmapFont promptFont;
    private BitmapFont buttonFont;
    private BitmapFont metaFont;
    private InputAdapter inputProcessor;
    private Runnable queuedAction;
    private float fadeAlpha = 1f;
    private boolean fadeOutActive = false;
    private int hoveredButtonIndex = -1;
    private int selectedButtonIndex = 0;
    private int pressedButtonIndex = -1;

    public DifficultySelectScreen(PixelRpgGame game, String characterName, GameDifficulty initialDifficulty) {
        this.game = game;
        this.characterName = normalizeCharacterName(characterName);
        for (int i = 0; i < buttonBounds.length; i++) {
            buttonBounds[i] = new Rectangle();
        }
        if (initialDifficulty != null) {
            selectedButtonIndex = initialDifficulty.ordinal();
        }
        rebuildLayout();
    }

    @Override
    public void show() {
        loadAssets();
        installInputProcessor();
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        hoveredButtonIndex = buttonIndexAt(Gdx.input.getX(), Gdx.input.getY());
        if (hoveredButtonIndex >= 0) {
            selectedButtonIndex = hoveredButtonIndex;
        }
    }

    @Override
    public void render(float delta) {
        if (queuedAction != null) {
            fadeAlpha = Math.min(1f, fadeAlpha + delta / SCREEN_FADE_DURATION_SECONDS);
            if (fadeAlpha >= 0.999f) {
                Runnable action = queuedAction;
                queuedAction = null;
                action.run();
                return;
            }
        } else if (!fadeOutActive && fadeAlpha > 0f) {
            fadeAlpha = Math.max(0f, fadeAlpha - delta / SCREEN_FADE_DURATION_SECONDS);
        }

        int liveHoverIndex = buttonIndexAt(Gdx.input.getX(), Gdx.input.getY());
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
        drawCenteredText(titleFont, "Choose Difficulty",
                REFERENCE_WIDTH * 0.5f, TITLE_TEXT_CENTER_Y,
                0.92f, 0.92f, 0.92f, 1f);

        drawCenteredText(promptFont, buildPromptText(),
                REFERENCE_WIDTH * 0.5f, 588f,
                0.90f, 0.88f, 0.84f, 1f);

        for (int i = 0; i < DIFFICULTIES.length; i++) {
            drawButton(i, DIFFICULTIES[i].getLabel());
        }
        drawButton(BACK_INDEX, "Back");

        GameDifficulty selectedDifficulty = getSelectedDifficulty();
        if (selectedDifficulty != null) {
            drawCenteredText(metaFont, selectedDifficulty.getDescription(),
                    REFERENCE_WIDTH * 0.5f, 208f,
                    0.88f, 0.84f, 0.76f, 1f);
        }

        if (fadeAlpha > 0f) {
            batch.setColor(0f, 0f, 0f, fadeAlpha);
            batch.draw(fillTexture, 0f, 0f, REFERENCE_WIDTH, REFERENCE_HEIGHT);
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
        if (buttonHoverTexture != null && buttonHoverTexture != buttonNormalTexture) {
            buttonHoverTexture.dispose();
        }
        if (fillTexture != null) {
            fillTexture.dispose();
        }
        if (titleFont != null) {
            titleFont.dispose();
        }
        if (promptFont != null) {
            promptFont.dispose();
        }
        if (buttonFont != null) {
            buttonFont.dispose();
        }
        if (metaFont != null) {
            metaFont.dispose();
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
        fillTexture = createSolidTexture();
        titleFont = loadFont(FONT_PATH, TITLE_FONT_SIZE);
        promptFont = loadFont(FONT_PATH, PROMPT_FONT_SIZE);
        buttonFont = loadFont(FONT_PATH, BUTTON_FONT_SIZE);
        metaFont = loadFont(FONT_PATH, META_FONT_SIZE);
        titleFont.setUseIntegerPositions(false);
        promptFont.setUseIntegerPositions(false);
        buttonFont.setUseIntegerPositions(false);
        metaFont.setUseIntegerPositions(false);
        fadeAlpha = 1f;
        fadeOutActive = false;
    }

    private void installInputProcessor() {
        inputProcessor = new InputAdapter() {
            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                hoveredButtonIndex = buttonIndexAt(screenX, screenY);
                if (hoveredButtonIndex >= 0) {
                    selectedButtonIndex = hoveredButtonIndex;
                }
                return hoveredButtonIndex >= 0;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (button != Input.Buttons.LEFT || queuedAction != null) {
                    return false;
                }
                pressedButtonIndex = buttonIndexAt(screenX, screenY);
                return pressedButtonIndex >= 0;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if (button != Input.Buttons.LEFT || queuedAction != null) {
                    return false;
                }
                int releasedButtonIndex = buttonIndexAt(screenX, screenY);
                boolean handled = pressedButtonIndex >= 0 && pressedButtonIndex == releasedButtonIndex;
                if (handled) {
                    triggerSelectedAction(releasedButtonIndex);
                }
                pressedButtonIndex = -1;
                return handled;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (queuedAction != null) {
                    return false;
                }
                if (keycode == Input.Keys.ESCAPE) {
                    queueBackAction();
                    return true;
                }
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
                    triggerSelectedAction(selectedButtonIndex);
                    return true;
                }
                if (keycode == Input.Keys.UP || keycode == Input.Keys.W) {
                    selectedButtonIndex = wrapIndex(selectedButtonIndex - 1);
                    hoveredButtonIndex = -1;
                    return true;
                }
                if (keycode == Input.Keys.DOWN || keycode == Input.Keys.S || keycode == Input.Keys.TAB) {
                    selectedButtonIndex = wrapIndex(selectedButtonIndex + 1);
                    hoveredButtonIndex = -1;
                    return true;
                }
                return false;
            }
        };
        Gdx.input.setInputProcessor(inputProcessor);
    }

    private void drawButton(int index, String label) {
        Rectangle bounds = buttonBounds[index];
        boolean hovered = hoveredButtonIndex == index;
        boolean active = (hoveredButtonIndex >= 0 ? hoveredButtonIndex : selectedButtonIndex) == index;
        Texture buttonTexture = hovered && buttonHoverTexture != null ? buttonHoverTexture : buttonNormalTexture;
        batch.draw(buttonTexture, bounds.x, bounds.y, bounds.width, bounds.height);
        drawCenteredText(buttonFont, label,
                bounds.x + bounds.width * 0.5f,
                bounds.y + bounds.height * 0.5f + 3f,
                active ? 0.96f : 0.87f,
                active ? 0.96f : 0.87f,
                active ? 0.96f : 0.87f,
                1f);
    }

    private void drawCenteredText(BitmapFont font, String text, float centerX, float centerY,
                                  float r, float g, float b, float a) {
        if (font == null || text == null) {
            return;
        }
        layout.setText(font, text);
        float textX = centerX - layout.width * 0.5f;
        float textY = centerY + layout.height * 0.5f;

        font.setColor(0f, 0f, 0f, Math.min(1f, 0.9f * a));
        font.draw(batch, text, textX + 2f, textY - 2f);
        font.setColor(r, g, b, a);
        font.draw(batch, text, textX, textY);
        font.setColor(1f, 1f, 1f, 1f);
    }

    private void rebuildLayout() {
        float buttonX = (REFERENCE_WIDTH - BUTTON_WIDTH) * 0.5f;
        for (int i = 0; i < DIFFICULTIES.length; i++) {
            float y = BUTTON_START_Y - BUTTON_SPACING * i;
            buttonBounds[i].set(buttonX, y, BUTTON_WIDTH, BUTTON_HEIGHT);
        }
        buttonBounds[BACK_INDEX].set(buttonX, BACK_BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT);
    }

    private void triggerSelectedAction(int buttonIndex) {
        if (buttonIndex == BACK_INDEX) {
            queueBackAction();
            return;
        }
        queueConfirmAction(DIFFICULTIES[buttonIndex]);
    }

    private void queueConfirmAction(final GameDifficulty difficulty) {
        fadeOutActive = true;
        queuedAction = new Runnable() {
            @Override
            public void run() {
                game.confirmNewGameDifficulty(difficulty);
            }
        };
    }

    private void queueBackAction() {
        fadeOutActive = true;
        queuedAction = new Runnable() {
            @Override
            public void run() {
                game.returnToCharacterNameSelection();
            }
        };
    }

    private GameDifficulty getSelectedDifficulty() {
        if (selectedButtonIndex < 0 || selectedButtonIndex >= DIFFICULTIES.length) {
            return null;
        }
        return DIFFICULTIES[selectedButtonIndex];
    }

    private String buildPromptText() {
        if (characterName.isBlank()) {
            return "Choose how you want this run to feel.";
        }
        return characterName + ", choose how you want this run to feel.";
    }

    private String normalizeCharacterName(String rawName) {
        if (rawName == null) {
            return "";
        }
        return rawName.trim().replaceAll("\\s+", " ");
    }

    private int wrapIndex(int index) {
        return (index % buttonBounds.length + buttonBounds.length) % buttonBounds.length;
    }

    private int buttonIndexAt(int screenX, int screenY) {
        pointer.set(screenX, screenY, 0f);
        viewport.unproject(pointer);
        for (int i = 0; i < buttonBounds.length; i++) {
            if (buttonBounds[i].contains(pointer.x, pointer.y)) {
                return i;
            }
        }
        return -1;
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

    private Texture createSolidTexture() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(1f, 1f, 1f, 1f);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return texture;
    }
}
