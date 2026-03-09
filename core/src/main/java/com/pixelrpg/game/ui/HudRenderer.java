package com.pixelrpg.game.ui;

import java.util.Locale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

/**
 * Draws the fixed HUD and top spawn notification panel.
 */
public class HudRenderer {
    private static final String VILLAGE_SPAWN_NOTIFICATION_TITLE = "Notification";
    private static final String VILLAGE_SPAWN_NOTIFICATION_MESSAGE = "Badingggg si GOJAR";
    private static final float VILLAGE_SPAWN_NOTIFICATION_DURATION_SECONDS = 6f;
    private static final float VILLAGE_SPAWN_NOTIFICATION_FADE_SECONDS = 1f;
    private static final float VILLAGE_SPAWN_NOTIFICATION_TOP_MARGIN = 16f;
    private static final float VILLAGE_SPAWN_NOTIFICATION_SIDE_MARGIN = 24f;
    private static final float VILLAGE_SPAWN_NOTIFICATION_TEXT_DOWN_OFFSET = 15f;
    private static final float VILLAGE_SPAWN_NOTIFICATION_MESSAGE_UP_OFFSET = 15f;
    private static final float VILLAGE_SPAWN_NOTIFICATION_TEXT_X_OFFSET = -6f;
    private static final float HUD_OVERLAY_SCALE = 1.25f;
    private static final float HUD_AVATAR_VISUAL_SCALE = 1.1875f;
    private static final float HUD_AVATAR_SCALE = 0.3465f;
    private static final float HUD_AVATAR_X_OFFSET = -34f;
    private static final float HUD_AVATAR_Y_OFFSET = -25f;
    private static final float HUD_BAR_SCALE = 1.30f;
    private static final float HUD_BAR_BACKGROUND_WIDTH_SCALE = 0.99f;
    private static final float HUD_BAR_BORDER_SCALE = 1.10f;
    private static final float HUD_BAR_BORDER_X_OFFSET = 15f;
    private static final float HUD_BAR_X_OFFSET = -10f;
    private static final float HUD_BAR_Y_OFFSET = -40f;
    private static final float HUD_MANA_BAR_Y_OFFSET = 3f;
    private static final float HUD_OVERLAY_X_OFFSET = -10f;
    private static final float HUD_OVERLAY_Y_OFFSET = -45f;
    private static final float HUD_HEALTH_BAR_X_OFFSET = 10f;
    private static final float HUD_HEALTH_BACKGROUND_Y_OFFSET = 10f;
    private static final float HUD_HEALTH_FILL_BORDER_X_OFFSET = -3f;
    private static final float HUD_HEALTH_FILL_BORDER_Y_OFFSET = 10f;
    private static final float HUD_GLOBAL_Y_OFFSET = -150f;
    private static final float HUD_LETTERBOX_BAR_HEIGHT = 150f;
    private static final String BERRY_ROTUNDA_FONT_PATH = "fonts/Berry Rotunda.ttf";
    private static final int VILLAGE_NOTIFICATION_TITLE_FONT_SIZE = 20;
    private static final int VILLAGE_NOTIFICATION_MESSAGE_FONT_SIZE = 13;

    private final OrthographicCamera uiCamera;
    private BitmapFont uiFont;
    private BitmapFont uiNotificationTitleFont;
    private BitmapFont uiNotificationMessageFont;
    private GlyphLayout uiLayout;

    private Texture uiOverlayBackTex;
    private Texture uiOverlayBorderTex;
    private Texture uiLevelBadgeTex;
    private Texture uiAvatarTex;
    private Texture uiAvatarCircleTex;
    private Texture uiBarBackgroundTex;
    private Texture uiBarBorderTex;
    private Texture uiBarHealthTex;
    private Texture uiBarManaTex;
    private Texture uiSpawnNotificationTex;
    private Texture uiBlackBarTex;

    private float villageSpawnNotificationTimer = 0f;

    public HudRenderer(OrthographicCamera uiCamera) {
        this.uiCamera = uiCamera;
    }

    public void loadAssets() {
        uiFont = new BitmapFont();
        uiFont.getData().setScale(0.9f);
        uiNotificationTitleFont = loadUiFont(BERRY_ROTUNDA_FONT_PATH, VILLAGE_NOTIFICATION_TITLE_FONT_SIZE);
        uiNotificationMessageFont = loadUiFont(BERRY_ROTUNDA_FONT_PATH, VILLAGE_NOTIFICATION_MESSAGE_FONT_SIZE);
        uiLayout = new GlyphLayout();

        uiOverlayBackTex = new Texture(Gdx.files.internal("ui/character_player/Overlay Border + Overlay Level.png"));
        uiOverlayBorderTex = null;
        uiLevelBadgeTex = null;
        uiAvatarTex = new Texture(Gdx.files.internal("ui/character_player/Avatar.png"));
        uiAvatarCircleTex = new Texture(Gdx.files.internal("ui/character_player/AvatarCircle.png"));
        uiBarBackgroundTex = new Texture(Gdx.files.internal("ui/character_player/ProgressBar/BqBar.png"));
        uiBarBorderTex = new Texture(Gdx.files.internal("ui/character_player/ProgressBar/Border.png"));
        uiBarHealthTex = new Texture(Gdx.files.internal("ui/character_player/ProgressBar/BarHealth.png"));
        uiBarManaTex = new Texture(Gdx.files.internal("ui/character_player/ProgressBar/BarMana.png"));
        uiSpawnNotificationTex = new Texture(Gdx.files.internal("ui/addons/message_notification_02/Panel + Border.png"));
        uiBlackBarTex = createSolidTexture(1, 1, 0, 0, 0, 255);

        uiOverlayBackTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        uiAvatarTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        uiAvatarCircleTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        uiBarBackgroundTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        uiBarBorderTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        uiBarHealthTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        uiBarManaTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        uiSpawnNotificationTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        uiBlackBarTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    public void resize(int width, int height) {
        if (uiCamera != null) {
            uiCamera.setToOrtho(false, width, height);
        }
    }

    public void showVillageSpawnNotification() {
        villageSpawnNotificationTimer = VILLAGE_SPAWN_NOTIFICATION_DURATION_SECONDS;
    }

    public void hideVillageSpawnNotification() {
        villageSpawnNotificationTimer = 0f;
    }

    public void update(float delta) {
        if (villageSpawnNotificationTimer <= 0f) {
            return;
        }
        villageSpawnNotificationTimer = Math.max(0f, villageSpawnNotificationTimer - delta);
    }

    public void draw(SpriteBatch batch, float currentHealth, float maxHealth, float currentMana, float maxMana, int playerLevel) {
        if (uiCamera == null || uiOverlayBackTex == null || uiBarBackgroundTex == null) {
            return;
        }
        uiCamera.update();
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        batch.setShader(null);
        batch.setColor(1f, 1f, 1f, 1f);

        float hudX = 16f;
        float baseHudY = uiCamera.viewportHeight - uiOverlayBackTex.getHeight() - 16f + HUD_GLOBAL_Y_OFFSET;
        float overlayDrop = 8f;
        float overlayY = baseHudY - overlayDrop;
        float overlayH = uiOverlayBackTex.getHeight();
        float overlayX = hudX + HUD_OVERLAY_X_OFFSET;
        float overlayDrawY = overlayY + HUD_OVERLAY_Y_OFFSET;
        float overlayDrawWidth = uiOverlayBackTex.getWidth() * HUD_OVERLAY_SCALE;
        float overlayDrawHeight = uiOverlayBackTex.getHeight() * HUD_OVERLAY_SCALE;

        float barX = hudX + 36f + HUD_BAR_X_OFFSET;
        float barWidth = 329f * HUD_BAR_SCALE;
        float barBackgroundWidth = barWidth * HUD_BAR_BACKGROUND_WIDTH_SCALE;
        float barHeight = uiBarBackgroundTex.getHeight() * HUD_BAR_SCALE;
        float borderWidth = barWidth * HUD_BAR_BORDER_SCALE;
        float borderHeight = barHeight * HUD_BAR_BORDER_SCALE;
        float borderInsetX = (borderWidth - barWidth) * 0.5f;
        float borderInsetY = (borderHeight - barHeight) * 0.5f;
        float healthY = baseHudY + 74f + HUD_BAR_Y_OFFSET;
        float manaY = baseHudY + 44f + HUD_BAR_Y_OFFSET;
        float healthPercent = maxHealth > 0f ? currentHealth / maxHealth : 0f;
        float manaPercent = maxMana > 0f ? currentMana / maxMana : 0f;

        // Layer 0: extra base frame at the absolute bottom.
        batch.draw(uiOverlayBackTex, overlayX, overlayDrawY, overlayDrawWidth, overlayDrawHeight);

        // Layer 1: base frame.
        batch.draw(uiOverlayBackTex, overlayX, overlayDrawY, overlayDrawWidth, overlayDrawHeight);

        float manaOffsetX = 5f * HUD_BAR_SCALE;
        float healthBarX = barX + HUD_HEALTH_BAR_X_OFFSET;
        float healthBackgroundY = healthY + HUD_HEALTH_BACKGROUND_Y_OFFSET;
        float healthFillBorderX = healthBarX + HUD_HEALTH_FILL_BORDER_X_OFFSET;
        float healthFillBorderY = healthY + HUD_HEALTH_FILL_BORDER_Y_OFFSET;
        float manaBarY = manaY + HUD_MANA_BAR_Y_OFFSET;
        // Layer 2: bar backgrounds (BqBar).
        drawBarBackground(batch, healthBarX, healthBackgroundY, barBackgroundWidth, barHeight);
        drawBarBackground(batch, barX + manaOffsetX, manaBarY, barBackgroundWidth, barHeight);

        // Layer 3: health/mana fills.
        drawBarFill(batch, healthFillBorderX, healthFillBorderY, barWidth, barHeight, healthPercent, uiBarHealthTex);
        drawBarFill(batch, barX + manaOffsetX, manaBarY, barWidth, barHeight, manaPercent, uiBarManaTex);

        // Layer 4: bar borders.
        drawBarBorder(batch, healthFillBorderX - borderInsetX + HUD_BAR_BORDER_X_OFFSET, healthFillBorderY - borderInsetY, borderWidth, borderHeight);
        drawBarBorder(batch, barX + manaOffsetX - borderInsetX + HUD_BAR_BORDER_X_OFFSET, manaBarY - borderInsetY, borderWidth, borderHeight);

        float avatarX = hudX + 33f + HUD_AVATAR_X_OFFSET;
        float avatarWidth = uiAvatarTex != null ? uiAvatarTex.getWidth() * HUD_AVATAR_SCALE * HUD_AVATAR_VISUAL_SCALE : 0f;
        float avatarHeight = uiAvatarTex != null ? uiAvatarTex.getHeight() * HUD_AVATAR_SCALE * HUD_AVATAR_VISUAL_SCALE : 0f;
        float avatarY = baseHudY + (overlayH - avatarHeight) / 2f + HUD_AVATAR_Y_OFFSET;
        // Layer 5: avatar.
        if (uiAvatarTex != null) {
            batch.draw(uiAvatarTex, avatarX, avatarY, avatarWidth, avatarHeight);
        }
        // Layer 6: avatar circle.
        if (uiAvatarCircleTex != null) {
            float circleWidth = uiAvatarCircleTex.getWidth() * HUD_AVATAR_VISUAL_SCALE;
            float circleHeight = uiAvatarCircleTex.getHeight() * HUD_AVATAR_VISUAL_SCALE;
            float circleX = avatarX + (avatarWidth - circleWidth) * 0.5f;
            float circleY = avatarY + (avatarHeight - circleHeight) * 0.5f;
            batch.draw(uiAvatarCircleTex, circleX, circleY, circleWidth, circleHeight);
        }

        float levelX = hudX + 12f;
        float levelY = overlayY + 20f;

        // Layer 7: text.
        if (uiFont != null && uiLayout != null) {
            uiFont.setColor(1f, 1f, 1f, 1f);
            String healthText = String.format(Locale.US, "%s / %s",
                    formatValue((int) currentHealth), formatValue((int) maxHealth));
            uiLayout.setText(uiFont, healthText);
            float healthTextX = healthBarX + (barWidth - uiLayout.width) / 2f + 40f;
            float healthTextY = healthY + barHeight - 8f;
            uiFont.draw(batch, uiLayout, healthTextX, healthTextY);

            String manaText = String.format(Locale.US, "%s / %s",
                    formatValue((int) currentMana), formatValue((int) maxMana));
            uiLayout.setText(uiFont, manaText);
            float manaTextX = barX + manaOffsetX + (barWidth - uiLayout.width) / 2f + 40f;
            float manaTextY = manaY + barHeight - 8f;
            uiFont.draw(batch, uiLayout, manaTextX, manaTextY);

            uiFont.setColor(0.55f, 0.8f, 0.4f, 1f);
            String levelText = Integer.toString(playerLevel);
            uiLayout.setText(uiFont, levelText);
            float badgeWidth = uiLevelBadgeTex != null ? uiLevelBadgeTex.getWidth() : 33f;
            float badgeHeight = uiLevelBadgeTex != null ? uiLevelBadgeTex.getHeight() : 44f;
            float levelTextX = levelX + (badgeWidth - uiLayout.width) / 2f;
            float levelTextY = levelY + (badgeHeight + uiLayout.height) / 2f;
            uiFont.draw(batch, uiLayout, levelTextX, levelTextY);
        }

        // Layer 8: top frame (same texture) to cap the stack.
        batch.draw(uiOverlayBackTex, overlayX, overlayDrawY, overlayDrawWidth, overlayDrawHeight);

        drawVillageSpawnNotification(batch);
        drawLetterboxBars(batch);
        batch.end();
    }

    public void dispose() {
        if (uiFont != null) uiFont.dispose();
        if (uiNotificationTitleFont != null) uiNotificationTitleFont.dispose();
        if (uiNotificationMessageFont != null) uiNotificationMessageFont.dispose();
        if (uiOverlayBackTex != null) uiOverlayBackTex.dispose();
        if (uiOverlayBorderTex != null) uiOverlayBorderTex.dispose();
        if (uiLevelBadgeTex != null) uiLevelBadgeTex.dispose();
        if (uiAvatarTex != null) uiAvatarTex.dispose();
        if (uiAvatarCircleTex != null) uiAvatarCircleTex.dispose();
        if (uiBarBackgroundTex != null) uiBarBackgroundTex.dispose();
        if (uiBarBorderTex != null) uiBarBorderTex.dispose();
        if (uiBarHealthTex != null) uiBarHealthTex.dispose();
        if (uiBarManaTex != null) uiBarManaTex.dispose();
        if (uiSpawnNotificationTex != null) uiSpawnNotificationTex.dispose();
        if (uiBlackBarTex != null) uiBlackBarTex.dispose();
    }

    private void drawBarBackground(SpriteBatch batch, float x, float y, float width, float height) {
        if (uiBarBackgroundTex != null) {
            batch.draw(uiBarBackgroundTex, x, y, width, height);
        }
    }

    private void drawBarFill(SpriteBatch batch, float x, float y, float width, float height, float percent, Texture fill) {
        float clamped = Math.max(0f, Math.min(1f, percent));
        if (fill != null && clamped > 0f) {
            batch.draw(fill, x, y, width * clamped, height);
        }
    }

    private void drawBarBorder(SpriteBatch batch, float x, float y, float width, float height) {
        if (uiBarBorderTex != null) {
            batch.draw(uiBarBorderTex, x, y, width, height);
        }
    }

    private void drawLetterboxBars(SpriteBatch batch) {
        if (uiBlackBarTex == null || uiCamera == null) {
            return;
        }
        float barHeight = Math.min(HUD_LETTERBOX_BAR_HEIGHT, uiCamera.viewportHeight * 0.5f);
        float width = uiCamera.viewportWidth;
        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(uiBlackBarTex, 0f, uiCamera.viewportHeight - barHeight, width, barHeight);
        batch.draw(uiBlackBarTex, 0f, 0f, width, barHeight);
    }

    private void drawVillageSpawnNotification(SpriteBatch batch) {
        if (villageSpawnNotificationTimer <= 0f
                || uiCamera == null
                || uiSpawnNotificationTex == null
                || uiNotificationTitleFont == null
                || uiNotificationMessageFont == null
                || uiLayout == null) {
            return;
        }

        float maxWidth = Math.max(1f, uiCamera.viewportWidth - VILLAGE_SPAWN_NOTIFICATION_SIDE_MARGIN * 2f);
        float panelWidth = Math.min(uiSpawnNotificationTex.getWidth(), maxWidth);
        float scale = panelWidth / uiSpawnNotificationTex.getWidth();
        float panelHeight = uiSpawnNotificationTex.getHeight() * scale;
        float panelX = (uiCamera.viewportWidth - panelWidth) * 0.5f;
        float panelY = uiCamera.viewportHeight - panelHeight - VILLAGE_SPAWN_NOTIFICATION_TOP_MARGIN + HUD_GLOBAL_Y_OFFSET;
        float alpha = villageSpawnNotificationAlpha();
        if (alpha <= 0f) {
            return;
        }

        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(uiSpawnNotificationTex, panelX, panelY, panelWidth, panelHeight);

        float centerX = panelX + panelWidth * 0.5f;
        float textCenterX = centerX + VILLAGE_SPAWN_NOTIFICATION_TEXT_X_OFFSET;
        float titleY = panelY + panelHeight - (24f * scale) - VILLAGE_SPAWN_NOTIFICATION_TEXT_DOWN_OFFSET;
        float messageY = panelY + panelHeight - (74f * scale)
                - VILLAGE_SPAWN_NOTIFICATION_TEXT_DOWN_OFFSET
                + VILLAGE_SPAWN_NOTIFICATION_MESSAGE_UP_OFFSET;

        drawCenteredText(batch, uiNotificationTitleFont, VILLAGE_SPAWN_NOTIFICATION_TITLE, textCenterX, titleY,
                0.8902f, 0.5216f, 0f, 1f, alpha);
        drawCenteredText(batch, uiNotificationMessageFont, VILLAGE_SPAWN_NOTIFICATION_MESSAGE, textCenterX, messageY,
                0.83f, 0.81f, 0.75f, 1f, alpha);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private float villageSpawnNotificationAlpha() {
        float fadeSeconds = Math.min(VILLAGE_SPAWN_NOTIFICATION_FADE_SECONDS,
                VILLAGE_SPAWN_NOTIFICATION_DURATION_SECONDS * 0.5f);
        if (fadeSeconds <= 0f) {
            return 1f;
        }
        float elapsed = VILLAGE_SPAWN_NOTIFICATION_DURATION_SECONDS - villageSpawnNotificationTimer;
        float fadeIn = clamp01(elapsed / fadeSeconds);
        float fadeOut = clamp01(villageSpawnNotificationTimer / fadeSeconds);
        return Math.min(fadeIn, fadeOut);
    }

    private void drawCenteredText(SpriteBatch batch, BitmapFont font, String text, float centerX, float baselineY,
                                  float r, float g, float b, float a, float alpha) {
        uiLayout.setText(font, text);
        float textX = centerX - uiLayout.width * 0.5f;
        font.setColor(0f, 0f, 0f, 0.85f * alpha);
        font.draw(batch, text, textX + 1f, baselineY - 1f);
        font.setColor(r, g, b, a * alpha);
        font.draw(batch, text, textX, baselineY);
        font.setColor(1f, 1f, 1f, 1f);
    }

    private BitmapFont loadUiFont(String fontPath, int size) {
        if (!Gdx.files.internal(fontPath).exists()) {
            Gdx.app.error("HudRenderer", "Missing font: " + fontPath + ". Using default BitmapFont.");
            return new BitmapFont();
        }

        FreeTypeFontGenerator generator = null;
        try {
            generator = new FreeTypeFontGenerator(Gdx.files.internal(fontPath));
            FreeTypeFontGenerator.FreeTypeFontParameter params = new FreeTypeFontGenerator.FreeTypeFontParameter();
            params.size = size;
            params.magFilter = Texture.TextureFilter.Linear;
            params.minFilter = Texture.TextureFilter.Linear;
            return generator.generateFont(params);
        } finally {
            if (generator != null) {
                generator.dispose();
            }
        }
    }

    private String formatValue(int value) {
        return String.format(Locale.US, "%,d", value);
    }

    private Texture createSolidTexture(int width, int height, int r, int g, int b, int a) {
        Pixmap pixmap = null;
        try {
            pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
            pixmap.setColor(r / 255f, g / 255f, b / 255f, a / 255f);
            pixmap.fill();
            return new Texture(pixmap);
        } finally {
            if (pixmap != null) {
                pixmap.dispose();
            }
        }
    }

    private static float clamp01(float value) {
        return Math.max(0f, Math.min(1f, value));
    }
}
