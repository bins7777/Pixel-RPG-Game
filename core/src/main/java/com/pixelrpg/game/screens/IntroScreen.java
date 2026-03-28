package com.pixelrpg.game.screens;

import java.util.Locale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.pixelrpg.game.PixelRpgGame;
import com.pixelrpg.game.intro.IntroPlaybackService;

public class IntroScreen extends ScreenAdapter {
    private static final String FONT_PATH = "fonts/Berry Rotunda.ttf";
    private static final int HINT_FONT_SIZE = 12;
    private static final float HOLD_TO_SKIP_SECONDS = 3f;

    private final PixelRpgGame game;
    private final IntroPlaybackService introPlaybackService;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final GlyphLayout layout = new GlyphLayout();

    private SpriteBatch batch;
    private BitmapFont hintFont;
    private Pixmap framePixmap;
    private Texture frameTexture;
    private boolean completionQueued = false;
    private boolean skipRequested = false;
    private float skipHoldSeconds = 0f;

    public IntroScreen(PixelRpgGame game, IntroPlaybackService introPlaybackService) {
        this.game = game;
        this.introPlaybackService = introPlaybackService == null
                ? IntroPlaybackService.NO_OP
                : introPlaybackService;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        hintFont = loadFont(FONT_PATH, HINT_FONT_SIZE);
        hintFont.setUseIntegerPositions(false);
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        boolean started = introPlaybackService.start(new Runnable() {
            @Override
            public void run() {
                queueCompletion();
            }
        });
        if (!started) {
            queueCompletion();
        }
    }

    @Override
    public void render(float delta) {
        updateSkipInput(delta);
        uploadFrameIfNeeded();

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        if (frameTexture != null) {
            drawFrameTexture();
        } else {
            drawCenteredText("Loading...");
        }
        drawSkipHint();
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, Math.max(1, width), Math.max(1, height));
    }

    @Override
    public void hide() {
        introPlaybackService.dispose();
    }

    @Override
    public void dispose() {
        hide();
        if (batch != null) {
            batch.dispose();
        }
        if (hintFont != null) {
            hintFont.dispose();
        }
        if (frameTexture != null) {
            frameTexture.dispose();
        }
        if (framePixmap != null) {
            framePixmap.dispose();
        }
    }

    private void updateSkipInput(float delta) {
        if (skipRequested) {
            return;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)
                || Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            skipHoldSeconds = Math.min(HOLD_TO_SKIP_SECONDS, skipHoldSeconds + delta);
            if (skipHoldSeconds >= HOLD_TO_SKIP_SECONDS) {
                skipRequested = true;
                introPlaybackService.skip();
            }
            return;
        }

        skipHoldSeconds = 0f;
    }

    private BitmapFont loadFont(String fontPath, int size) {
        if (!Gdx.files.internal(fontPath).exists()) {
            BitmapFont fallbackFont = new BitmapFont();
            fallbackFont.getData().setScale(1.15f);
            return fallbackFont;
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

    private void uploadFrameIfNeeded() {
        if (!introPlaybackService.isReady()) {
            return;
        }

        int frameWidth = Math.max(1, introPlaybackService.getFrameWidth());
        int frameHeight = Math.max(1, introPlaybackService.getFrameHeight());
        if (framePixmap == null || framePixmap.getWidth() != frameWidth || framePixmap.getHeight() != frameHeight) {
            if (frameTexture != null) {
                frameTexture.dispose();
            }
            if (framePixmap != null) {
                framePixmap.dispose();
            }
            framePixmap = new Pixmap(frameWidth, frameHeight, Pixmap.Format.RGBA8888);
            frameTexture = new Texture(framePixmap);
            frameTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }

        if (introPlaybackService.uploadCurrentFrame(framePixmap) && frameTexture != null) {
            frameTexture.draw(framePixmap, 0, 0);
        }
    }

    private void drawFrameTexture() {
        float screenWidth = camera.viewportWidth;
        float screenHeight = camera.viewportHeight;
        float textureWidth = frameTexture.getWidth();
        float textureHeight = frameTexture.getHeight();
        float scale = Math.min(screenWidth / textureWidth, screenHeight / textureHeight);
        float drawWidth = textureWidth * scale;
        float drawHeight = textureHeight * scale;
        float drawX = (screenWidth - drawWidth) * 0.5f;
        float drawY = (screenHeight - drawHeight) * 0.5f;
        batch.draw(frameTexture, drawX, drawY, drawWidth, drawHeight);
    }

    private void drawCenteredText(String text) {
        layout.setText(hintFont, text);
        float x = (camera.viewportWidth - layout.width) * 0.5f;
        float y = (camera.viewportHeight + layout.height) * 0.5f;
        hintFont.setColor(0.92f, 0.92f, 0.92f, 1f);
        hintFont.draw(batch, text, x, y);
    }

    private void drawSkipHint() {
        String hint = buildSkipHint();
        layout.setText(hintFont, hint);
        float x = (camera.viewportWidth - layout.width) * 0.5f;
        float y = 52f;
        hintFont.setColor(0f, 0f, 0f, 0.75f);
        hintFont.draw(batch, hint, x + 2f, y - 2f);
        hintFont.setColor(0.92f, 0.92f, 0.92f, 1f);
        hintFont.draw(batch, hint, x, y);
        hintFont.setColor(1f, 1f, 1f, 1f);
    }

    private String buildSkipHint() {
        if (skipRequested) {
            return "Skipping...";
        }
        if (skipHoldSeconds > 0f) {
            float remainingSeconds = Math.max(0f, HOLD_TO_SKIP_SECONDS - skipHoldSeconds);
            return String.format(Locale.US, "Hold Space or left click to skip (%.1fs)", remainingSeconds);
        }
        return "Hold Space or left click for 3 seconds to skip";
    }

    private void queueCompletion() {
        if (completionQueued) {
            return;
        }
        completionQueued = true;
        if (Gdx.app == null) {
            return;
        }
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                game.startGameAfterIntro();
            }
        });
    }
}
