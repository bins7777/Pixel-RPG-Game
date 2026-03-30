package com.pixelrpg.game.ui;

import java.io.File;
import java.util.Locale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.pixelrpg.game.entities.PlayerLevelStats;
import com.pixelrpg.game.settings.GameSettingsService;

/**
 * Draws the fixed HUD and top spawn notification panel.
 */
public class HudRenderer {
    private static final String PROJECT_ASSETS_DIRECTORY = "core/assets";
    private static final String VILLAGE_SPAWN_NOTIFICATION_TITLE = "Notification";
    private static final String VILLAGE_SPAWN_NOTIFICATION_MESSAGE = "Welcome to the Village.";
    private static final float VILLAGE_SPAWN_NOTIFICATION_DURATION_SECONDS = 6f;
    private static final float QUEST_GUIDANCE_NOTIFICATION_DURATION_SECONDS = 5f;
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
    private static final float HUD_AVATAR_EXTRA_Y_OFFSET = -7f;
    private static final float HUD_BAR_SCALE = 1.30f;
    private static final float HUD_BAR_BACKGROUND_WIDTH_SCALE = 0.99f;
    private static final float HUD_BAR_BORDER_SCALE = 1.10f;
    private static final float HUD_BAR_BORDER_X_OFFSET = 15f;
    private static final float HUD_BAR_X_OFFSET = -10f;
    private static final float HUD_BAR_Y_OFFSET = -40f;
    private static final float HUD_MANA_BAR_Y_OFFSET = 3f;
    private static final float HUD_OVERLAY_X_OFFSET = -10f;
    private static final float HUD_OVERLAY_Y_OFFSET = -45f;
    private static final float HUD_OVERLAY_EXTRA_Y_OFFSET = -7f;
    private static final float HUD_HEALTH_BAR_X_OFFSET = 10f;
    private static final float HUD_HEALTH_BACKGROUND_Y_OFFSET = 10f;
    private static final float HUD_HEALTH_FILL_BORDER_X_OFFSET = -3f;
    private static final float HUD_HEALTH_FILL_BORDER_Y_OFFSET = 10f;
    private static final float HUD_LEVEL_TEXT_SCALE_MULTIPLIER = 1.15f;
    private static final float HUD_GLOBAL_Y_OFFSET = -150f;
    private static final float HUD_LETTERBOX_BAR_HEIGHT = 150f;
    private static final String BERRY_ROTUNDA_FONT_PATH = "fonts/Berry Rotunda.ttf";
    private static final int VILLAGE_NOTIFICATION_TITLE_FONT_SIZE = 20;
    private static final int VILLAGE_NOTIFICATION_MESSAGE_FONT_SIZE = 13;
    private static final String MAIN_MENU_PANEL_PATH = "ui/menu/Big_Panel.png";
    private static final String MAIN_MENU_PANEL_1_PATH = "ui/menu/Panel1.png";
    private static final String MAIN_MENU_PANEL_2_PATH = "ui/menu/Panel2.png";
    private static final String MAIN_MENU_PANEL_3_PATH = "ui/menu/Panel3.png";
    private static final String MAIN_MENU_PANEL_4_PATH = "ui/menu/Panel4.png";
    private static final String MAIN_MENU_TAB_SELECTED_PATH = "ui/menu/Line Select Current.png";
    private static final String MAIN_MENU_CLOSE_PATH = "ui/menu/Button Close Normal.png";
    private static final float MAIN_MENU_MAX_WIDTH_RATIO = 0.88f;
    private static final float MAIN_MENU_MAX_HEIGHT_RATIO = 0.80f;
    private static final float MAIN_MENU_SCALE = 0.80f;
    private static final int MAIN_MENU_TITLE_FONT_SIZE = 16;
    private static final int MAIN_MENU_TAB_FONT_SIZE = 18;
    private static final int MAIN_MENU_BODY_FONT_SIZE = 15;
    private static final String[] MAIN_MENU_TABS = {"Game", "Settings"};
    private static final String[] GAME_MENU_ACTIONS = {"Resume", "Main Menu", "Reload Save", "Exit Game"};
    private static final String[] SETTINGS_MENU_ITEMS = {"Music Volume", "SFX Volume", "Fullscreen", "VSync"};
    private static final int MAIN_MENU_SETTINGS_STEP_PERCENT = 10;
    private static final float GAME_MENU_GROUP_X_OFFSET = -15f;
    private static final float MAIN_MENU_CHOICE_TEXT_SCALE = 0.7f;
    private static final float SETTINGS_SLIDER_TRACK_HEIGHT_RATIO = 0.22f;
    private static final float SETTINGS_SLIDER_TRACK_MIN_WIDTH = 74f;
    private static final float SETTINGS_SLIDER_TRACK_WIDTH_RATIO = 0.30f;
    private static final float SETTINGS_SLIDER_RIGHT_PADDING = 12f;
    private static final float SETTINGS_SLIDER_VALUE_GAP = 10f;
    private static final float SETTINGS_SLIDER_VALUE_MIN_WIDTH = 40f;
    private static final float SETTINGS_SLIDER_THUMB_WIDTH = 10f;
    private static final float SETTINGS_SLIDER_THUMB_HEIGHT_RATIO = 1.9f;
    private static final String QUEST_PANEL_PATH = "ui/objectives/Panel Open.png";
    private static final String QUEST_PANEL_CLOSED_PATH = "ui/objectives/Panel Closed.png";
    private static final String QUEST_PANEL_ICON_OPEN_PATH = "ui/objectives/Icons/IconOpen.png";
    private static final String QUEST_PANEL_ICON_CLOSE_PATH = "ui/objectives/Icons/IconClose.png";
    private static final float QUEST_PANEL_MAX_HEIGHT_RATIO = 1f;
    private static final float QUEST_PANEL_SIDE_MARGIN = 0f;
    private static final float QUEST_PANEL_VERTICAL_OFFSET = 0f;
    private static final float QUEST_PANEL_TEXT_LEFT_PADDING = 92f;
    private static final float QUEST_PANEL_TEXT_RIGHT_PADDING = 20f;
    private static final int QUEST_PANEL_HEADER_FONT_SIZE = 15;
    private static final int QUEST_PANEL_QUEST_TITLE_FONT_SIZE = 13;
    private static final int QUEST_PANEL_PROMPT_FONT_SIZE = 13;
    private static final float QUEST_PANEL_HEADER_TOP_OFFSET = 15f;
    private static final float QUEST_PANEL_HEADER_SCALE = 1.05f;
    private static final float QUEST_PANEL_QUEST_TITLE_BASELINE_TOP_OFFSET = 96f;
    private static final float QUEST_PANEL_QUEST_TITLE_BOX_HEIGHT = 46f;
    private static final float QUEST_PANEL_QUEST_TITLE_SCALE = 1.0f;
    private static final float QUEST_PANEL_OBJECTIVE_TEXT_BASELINE_TOP_OFFSET = 146f;
    private static final float QUEST_PANEL_OBJECTIVE_TEXT_BOTTOM_OFFSET = 52f;
    private static final float QUEST_PANEL_OBJECTIVE_TEXT_SCALE = 0.92f;
    private static final float QUEST_PANEL_AREA_BASELINE_OFFSET_Y = 108f;
    private static final float QUEST_PANEL_AREA_TEXT_SCALE = 1.0f;
    private static final float QUEST_PANEL_ICON_OFFSET_X = 22f;
    private static final float QUEST_PANEL_ICON_OFFSET_Y = 16f;
    private static final float QUEST_PANEL_HEADER_CENTER_X_RATIO = 0.51f;
    private static final float QUEST_PANEL_CLOSED_HEIGHT_RATIO = 0.90f;
    private static final float QUEST_PANEL_TOGGLE_HITBOX_WIDTH = 60f;
    private static final float QUEST_PANEL_TOGGLE_HITBOX_HEIGHT = 44f;
    private static final float QUEST_PANEL_CLOSED_X_OFFSET = -6f;
    private static final float BOSS_HUD_BAR_WIDTH = 320f;
    private static final float BOSS_HUD_BAR_TOP_MARGIN = 18f;
    private static final float BOSS_HUD_BAR_HEIGHT_SCALE = 1.0f;
    private static final float CHAT_PANEL_WIDTH = 360f;
    private static final float CHAT_PANEL_HEIGHT = 172f;
    private static final float CHAT_PANEL_LEFT_MARGIN = 18f;
    private static final float CHAT_PANEL_BOTTOM_MARGIN = 18f;
    private static final float CHAT_PADDING = 10f;
    private static final float CHAT_BORDER_THICKNESS = 2f;
    private static final float CHAT_MESSAGE_LINE_HEIGHT = 20f;
    private static final int CHAT_VISIBLE_MESSAGES = 5;
    private static final int CHAT_MAX_MESSAGES = 40;
    private static final int CHAT_MAX_INPUT_LENGTH = 96;
    private static final float CHAT_INPUT_HEIGHT = 30f;
    private static final float CHAT_PANEL_ALPHA = 0.76f;
    private static final float CHAT_INPUT_ALPHA = 0.58f;
    private static final float CHAT_CURSOR_BLINK_SECONDS = 0.5f;

    private final OrthographicCamera uiCamera;
    private BitmapFont uiFont;
    private BitmapFont uiNotificationTitleFont;
    private BitmapFont uiNotificationMessageFont;
    private BitmapFont uiMainMenuTitleFont;
    private BitmapFont uiMainMenuTabFont;
    private BitmapFont uiMainMenuBodyFont;
    private BitmapFont uiQuestPanelHeaderFont;
    private BitmapFont uiQuestPanelQuestTitleFont;
    private BitmapFont uiQuestPanelPromptFont;
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
    private Texture uiMainMenuPanelTex;
    private Texture uiMainMenuPanel1Tex;
    private Texture uiMainMenuPanel2Tex;
    private Texture uiMainMenuPanel3Tex;
    private Texture uiMainMenuPanel4Tex;
    private Texture uiMainMenuTabSelectedTex;
    private Texture uiMainMenuCloseTex;
    private Texture uiQuestPanelTex;
    private Texture uiQuestPanelClosedTex;
    private Texture uiQuestPanelIconOpenTex;
    private Texture uiQuestPanelIconCloseTex;

    private final GameSettingsService settingsService;
    private String topNotificationTitle;
    private String topNotificationMessage;
    private float topNotificationTimer = 0f;
    private float topNotificationDuration = 0f;
    private boolean topNotificationPersistent = false;
    private boolean mainMenuVisible = false;
    private PauseMenuTab activePauseMenuTab = PauseMenuTab.GAME;
    private PauseMenuAction confirmationAction = PauseMenuAction.NONE;
    private int selectedGameActionIndex = 0;
    private int selectedSettingsIndex = 0;
    private int selectedConfirmationIndex = 0;
    private final Rectangle[] mainMenuTabBounds = new Rectangle[MAIN_MENU_TABS.length];
    private final Rectangle[] gameActionBounds = new Rectangle[GAME_MENU_ACTIONS.length];
    private final Rectangle[] settingsRowBounds = new Rectangle[SETTINGS_MENU_ITEMS.length];
    private final Rectangle[] settingsDecreaseBounds = new Rectangle[SETTINGS_MENU_ITEMS.length];
    private final Rectangle[] settingsIncreaseBounds = new Rectangle[SETTINGS_MENU_ITEMS.length];
    private final Rectangle[] settingsSliderTrackBounds = new Rectangle[SETTINGS_MENU_ITEMS.length];
    private final Rectangle[] settingsSliderValueBounds = new Rectangle[SETTINGS_MENU_ITEMS.length];
    private final Rectangle mainMenuPanelBounds = new Rectangle();
    private final Rectangle mainMenuLeftCardBounds = new Rectangle();
    private final Rectangle mainMenuRightCardBounds = new Rectangle();
    private final Rectangle mainMenuCenterCardBounds = new Rectangle();
    private final Rectangle confirmationBounds = new Rectangle();
    private final Rectangle mainMenuCloseBounds = new Rectangle();
    private final Rectangle confirmationYesBounds = new Rectangle();
    private final Rectangle confirmationNoBounds = new Rectangle();
    private final Array<String> chatMessages = new Array<>();
    private final StringBuilder chatDraft = new StringBuilder();
    private boolean chatOpen = false;
    private boolean chatCursorVisible = false;
    private float chatCursorTimer = 0f;
    private int activeSettingsSliderDragIndex = -1;
    private boolean questPanelExpanded = true;
    private boolean questPanelToggleVisible = false;
    private final Rectangle questPanelBounds = new Rectangle();
    private final Rectangle questPanelToggleBounds = new Rectangle();

    public HudRenderer(OrthographicCamera uiCamera, GameSettingsService settingsService) {
        this.uiCamera = uiCamera;
        this.settingsService = settingsService;
        for (int i = 0; i < mainMenuTabBounds.length; i++) {
            mainMenuTabBounds[i] = new Rectangle();
        }
        for (int i = 0; i < gameActionBounds.length; i++) {
            gameActionBounds[i] = new Rectangle();
        }
        for (int i = 0; i < settingsRowBounds.length; i++) {
            settingsRowBounds[i] = new Rectangle();
            settingsDecreaseBounds[i] = new Rectangle();
            settingsIncreaseBounds[i] = new Rectangle();
            settingsSliderTrackBounds[i] = new Rectangle();
            settingsSliderValueBounds[i] = new Rectangle();
        }
    }

    public void loadAssets() {
        uiFont = new BitmapFont();
        uiFont.getData().setScale(0.9f);
        uiNotificationTitleFont = loadUiFont(BERRY_ROTUNDA_FONT_PATH, VILLAGE_NOTIFICATION_TITLE_FONT_SIZE);
        uiNotificationMessageFont = loadUiFont(BERRY_ROTUNDA_FONT_PATH, VILLAGE_NOTIFICATION_MESSAGE_FONT_SIZE);
        uiMainMenuTitleFont = loadUiFont(BERRY_ROTUNDA_FONT_PATH, MAIN_MENU_TITLE_FONT_SIZE);
        uiMainMenuTabFont = loadUiFont(BERRY_ROTUNDA_FONT_PATH, MAIN_MENU_TAB_FONT_SIZE);
        uiMainMenuBodyFont = loadUiFont(BERRY_ROTUNDA_FONT_PATH, MAIN_MENU_BODY_FONT_SIZE);
        uiQuestPanelHeaderFont = loadUiFont(BERRY_ROTUNDA_FONT_PATH, QUEST_PANEL_HEADER_FONT_SIZE);
        uiQuestPanelQuestTitleFont = loadUiFont(BERRY_ROTUNDA_FONT_PATH, QUEST_PANEL_QUEST_TITLE_FONT_SIZE);
        uiQuestPanelPromptFont = loadUiFont(BERRY_ROTUNDA_FONT_PATH, QUEST_PANEL_PROMPT_FONT_SIZE);
        uiLayout = new GlyphLayout();

        uiOverlayBackTex = loadRequiredTexture("ui/character_player/Overlay Border + Overlay Level.png");
        uiOverlayBorderTex = null;
        uiLevelBadgeTex = null;
        uiAvatarTex = loadRequiredTexture("ui/character_player/Avatar.png");
        uiAvatarCircleTex = loadRequiredTexture("ui/character_player/AvatarCircle.png");
        uiBarBackgroundTex = loadRequiredTexture("ui/character_player/ProgressBar/BqBar.png");
        uiBarBorderTex = loadRequiredTexture("ui/character_player/ProgressBar/Border.png");
        uiBarHealthTex = loadRequiredTexture("ui/character_player/ProgressBar/BarHealth.png");
        uiBarManaTex = loadRequiredTexture("ui/character_player/ProgressBar/BarMana.png");
        uiSpawnNotificationTex = loadRequiredTexture("ui/addons/message_notification_02/Panel + Border.png");
        uiBlackBarTex = createSolidTexture(1, 1, 0, 0, 0, 255);
        uiMainMenuPanelTex = loadOptionalTexture(MAIN_MENU_PANEL_PATH);
        uiMainMenuPanel1Tex = loadOptionalTexture(MAIN_MENU_PANEL_1_PATH);
        uiMainMenuPanel2Tex = loadOptionalTexture(MAIN_MENU_PANEL_2_PATH);
        uiMainMenuPanel3Tex = loadOptionalTexture(MAIN_MENU_PANEL_3_PATH);
        uiMainMenuPanel4Tex = loadOptionalTexture(MAIN_MENU_PANEL_4_PATH);
        uiMainMenuTabSelectedTex = loadOptionalTexture(MAIN_MENU_TAB_SELECTED_PATH);
        uiMainMenuCloseTex = loadOptionalTexture(MAIN_MENU_CLOSE_PATH);
        uiQuestPanelTex = loadOptionalTexture(QUEST_PANEL_PATH);
        uiQuestPanelClosedTex = loadOptionalTexture(QUEST_PANEL_CLOSED_PATH);
        uiQuestPanelIconOpenTex = loadOptionalTexture(QUEST_PANEL_ICON_OPEN_PATH);
        uiQuestPanelIconCloseTex = loadOptionalTexture(QUEST_PANEL_ICON_CLOSE_PATH);

        uiOverlayBackTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        uiAvatarTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        uiAvatarCircleTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        uiBarBackgroundTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        uiBarBorderTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        uiBarHealthTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        uiBarManaTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        uiSpawnNotificationTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        uiBlackBarTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        setNearestFilter(uiMainMenuPanelTex);
        setNearestFilter(uiMainMenuPanel1Tex);
        setNearestFilter(uiMainMenuPanel2Tex);
        setNearestFilter(uiMainMenuPanel3Tex);
        setNearestFilter(uiMainMenuPanel4Tex);
        setNearestFilter(uiMainMenuTabSelectedTex);
        setNearestFilter(uiMainMenuCloseTex);
        setNearestFilter(uiQuestPanelTex);
        setNearestFilter(uiQuestPanelClosedTex);
        setNearestFilter(uiQuestPanelIconOpenTex);
        setNearestFilter(uiQuestPanelIconCloseTex);
    }

    public void resize(int width, int height) {
        if (uiCamera != null) {
            uiCamera.setToOrtho(false, width, height);
        }
    }

    public void showVillageSpawnNotification() {
        showNotification(
                VILLAGE_SPAWN_NOTIFICATION_TITLE,
                VILLAGE_SPAWN_NOTIFICATION_MESSAGE,
                VILLAGE_SPAWN_NOTIFICATION_DURATION_SECONDS);
    }

    public void hideVillageSpawnNotification() {
        clearNotification();
    }

    public void showNotification(String title, String message, float durationSeconds) {
        if (sameNotification(title, message)) {
            return;
        }
        topNotificationTitle = title;
        topNotificationMessage = message;
        topNotificationDuration = Math.max(0f, durationSeconds);
        topNotificationTimer = topNotificationDuration;
        topNotificationPersistent = false;
    }

    public void showPersistentNotification(String title, String message) {
        if (sameNotification(title, message)) {
            return;
        }
        topNotificationTitle = title;
        topNotificationMessage = message;
        topNotificationDuration = QUEST_GUIDANCE_NOTIFICATION_DURATION_SECONDS;
        topNotificationTimer = topNotificationDuration;
        topNotificationPersistent = false;
    }

    public void clearNotification() {
        topNotificationTitle = null;
        topNotificationMessage = null;
        topNotificationTimer = 0f;
        topNotificationDuration = 0f;
        topNotificationPersistent = false;
    }

    public void openMainMenu() {
        mainMenuVisible = true;
        activePauseMenuTab = PauseMenuTab.GAME;
        selectedGameActionIndex = 0;
        selectedConfirmationIndex = 0;
        confirmationAction = PauseMenuAction.NONE;
        activeSettingsSliderDragIndex = -1;
    }

    public void closeMainMenu() {
        mainMenuVisible = false;
        confirmationAction = PauseMenuAction.NONE;
        activeSettingsSliderDragIndex = -1;
    }

    public void toggleMainMenu() {
        if (mainMenuVisible) {
            closeMainMenu();
        } else {
            openMainMenu();
        }
    }

    public boolean isMainMenuVisible() {
        return mainMenuVisible;
    }

    public boolean isMainMenuConfirmationVisible() {
        return confirmationAction != PauseMenuAction.NONE;
    }

    public void cancelMainMenuConfirmation() {
        confirmationAction = PauseMenuAction.NONE;
        selectedConfirmationIndex = 0;
        activeSettingsSliderDragIndex = -1;
    }

    public void selectPreviousMainMenuItem() {
        if (confirmationAction != PauseMenuAction.NONE) {
            selectedConfirmationIndex = wrapIndex(selectedConfirmationIndex - 1, 2);
            return;
        }
        switch (activePauseMenuTab) {
            case GAME -> selectedGameActionIndex = wrapIndex(selectedGameActionIndex - 1, GAME_MENU_ACTIONS.length);
            case SETTINGS -> selectedSettingsIndex = wrapIndex(selectedSettingsIndex - 1, SETTINGS_MENU_ITEMS.length);
            default -> {
            }
        }
    }

    public void selectNextMainMenuItem() {
        if (confirmationAction != PauseMenuAction.NONE) {
            selectedConfirmationIndex = wrapIndex(selectedConfirmationIndex + 1, 2);
            return;
        }
        switch (activePauseMenuTab) {
            case GAME -> selectedGameActionIndex = wrapIndex(selectedGameActionIndex + 1, GAME_MENU_ACTIONS.length);
            case SETTINGS -> selectedSettingsIndex = wrapIndex(selectedSettingsIndex + 1, SETTINGS_MENU_ITEMS.length);
            default -> {
            }
        }
    }

    public void selectPreviousMainMenuTab() {
        if (confirmationAction != PauseMenuAction.NONE) {
            selectedConfirmationIndex = wrapIndex(selectedConfirmationIndex - 1, 2);
            return;
        }
        activeSettingsSliderDragIndex = -1;
        activePauseMenuTab = PauseMenuTab.values()[wrapIndex(activePauseMenuTab.ordinal() - 1, PauseMenuTab.values().length)];
    }

    public void selectNextMainMenuTab() {
        if (confirmationAction != PauseMenuAction.NONE) {
            selectedConfirmationIndex = wrapIndex(selectedConfirmationIndex + 1, 2);
            return;
        }
        activeSettingsSliderDragIndex = -1;
        activePauseMenuTab = PauseMenuTab.values()[wrapIndex(activePauseMenuTab.ordinal() + 1, PauseMenuTab.values().length)];
    }

    public void adjustSelectedMainMenuSetting(int direction) {
        if (direction == 0) {
            return;
        }
        if (confirmationAction != PauseMenuAction.NONE) {
            selectedConfirmationIndex = wrapIndex(selectedConfirmationIndex + direction, 2);
            return;
        }
        if (activePauseMenuTab != PauseMenuTab.SETTINGS || settingsService == null) {
            activePauseMenuTab = PauseMenuTab.values()[wrapIndex(activePauseMenuTab.ordinal() + direction, PauseMenuTab.values().length)];
            return;
        }
        switch (selectedSettingsIndex) {
            case 0 -> settingsService.stepMusicVolume(direction * MAIN_MENU_SETTINGS_STEP_PERCENT);
            case 1 -> settingsService.stepSfxVolume(direction * MAIN_MENU_SETTINGS_STEP_PERCENT);
            case 2 -> settingsService.toggleFullscreen();
            case 3 -> settingsService.toggleVsync();
            default -> {
            }
        }
    }

    public PauseMenuAction activateSelectedMainMenuItem() {
        if (!mainMenuVisible) {
            return PauseMenuAction.NONE;
        }
        if (confirmationAction != PauseMenuAction.NONE) {
            PauseMenuAction action = selectedConfirmationIndex == 0 ? confirmationAction : PauseMenuAction.NONE;
            confirmationAction = PauseMenuAction.NONE;
            return action;
        }
        if (activePauseMenuTab == PauseMenuTab.GAME) {
            return activateGameMenuAction(selectedGameActionIndex);
        }
        if (activePauseMenuTab == PauseMenuTab.SETTINGS) {
            adjustSelectedMainMenuSetting(1);
        }
        return PauseMenuAction.NONE;
    }

    public PauseMenuAction handleMainMenuClick(float screenX, float screenY) {
        if (!mainMenuVisible || uiCamera == null) {
            return PauseMenuAction.NONE;
        }
        updateMainMenuLayout();
        float worldX = screenX;
        float worldY = uiCamera.viewportHeight - screenY;

        if (mainMenuCloseBounds.contains(worldX, worldY)) {
            cancelMainMenuConfirmation();
            return PauseMenuAction.RESUME;
        }
        if (confirmationAction != PauseMenuAction.NONE) {
            if (confirmationYesBounds.contains(worldX, worldY)) {
                PauseMenuAction action = confirmationAction;
                confirmationAction = PauseMenuAction.NONE;
                selectedConfirmationIndex = 0;
                return action;
            }
            if (confirmationNoBounds.contains(worldX, worldY)) {
                cancelMainMenuConfirmation();
            }
            return PauseMenuAction.NONE;
        }

        for (int i = 0; i < mainMenuTabBounds.length; i++) {
            if (mainMenuTabBounds[i].contains(worldX, worldY)) {
                activeSettingsSliderDragIndex = -1;
                activePauseMenuTab = PauseMenuTab.values()[i];
                return PauseMenuAction.NONE;
            }
        }

        if (activePauseMenuTab == PauseMenuTab.GAME) {
            activeSettingsSliderDragIndex = -1;
            for (int i = 0; i < gameActionBounds.length; i++) {
                if (gameActionBounds[i].contains(worldX, worldY)) {
                    selectedGameActionIndex = i;
                    return activateGameMenuAction(i);
                }
            }
            return PauseMenuAction.NONE;
        }

        if (activePauseMenuTab == PauseMenuTab.SETTINGS) {
            for (int i = 0; i < settingsRowBounds.length; i++) {
                if (!settingsRowBounds[i].contains(worldX, worldY)) {
                    continue;
                }
                selectedSettingsIndex = i;
                if (isSliderSetting(i)) {
                    activeSettingsSliderDragIndex = i;
                    updateDraggedSettingsSlider(worldX);
                } else {
                    activeSettingsSliderDragIndex = -1;
                    adjustSelectedMainMenuSetting(1);
                }
                break;
            }
        }
        return PauseMenuAction.NONE;
    }

    public boolean handleQuestPanelClick(float screenX, float screenY) {
        if (mainMenuVisible || uiCamera == null || !questPanelToggleVisible) {
            return false;
        }
        float worldX = screenX;
        float worldY = uiCamera.viewportHeight - screenY;
        if (!questPanelToggleBounds.contains(worldX, worldY)) {
            return false;
        }
        questPanelExpanded = !questPanelExpanded;
        return true;
    }

    public void handleMainMenuDrag(float screenX, float screenY) {
        if (!mainMenuVisible || uiCamera == null || activeSettingsSliderDragIndex < 0 || settingsService == null) {
            return;
        }
        float worldX = screenX;
        @SuppressWarnings("unused")
        float worldY = uiCamera.viewportHeight - screenY;
        updateMainMenuLayout();
        updateDraggedSettingsSlider(worldX);
    }

    public void endMainMenuDrag() {
        activeSettingsSliderDragIndex = -1;
    }

    public void openChat() {
        closeMainMenu();
        chatOpen = true;
        chatCursorVisible = true;
        chatCursorTimer = 0f;
    }

    public void closeChat(boolean clearDraft) {
        chatOpen = false;
        if (clearDraft) {
            chatDraft.setLength(0);
        }
        chatCursorVisible = false;
        chatCursorTimer = 0f;
    }

    public boolean isChatOpen() {
        return chatOpen;
    }

    public void appendChatCharacter(char character) {
        if (!chatOpen || Character.isISOControl(character) || chatDraft.length() >= CHAT_MAX_INPUT_LENGTH) {
            return;
        }
        chatDraft.append(character);
        chatCursorVisible = true;
        chatCursorTimer = 0f;
    }

    public void removeLastChatCharacter() {
        if (!chatOpen || chatDraft.length() == 0) {
            return;
        }
        chatDraft.setLength(chatDraft.length() - 1);
        chatCursorVisible = true;
        chatCursorTimer = 0f;
    }

    public String submitChatDraft() {
        String message = chatDraft.toString().trim();
        chatDraft.setLength(0);
        if (message.isEmpty()) {
            return null;
        }
        chatMessages.add("You: " + message);
        if (chatMessages.size > CHAT_MAX_MESSAGES) {
            chatMessages.removeRange(0, chatMessages.size - CHAT_MAX_MESSAGES - 1);
        }
        return message;
    }

    // Backward compatibility for existing call sites.
    public void toggleQuestTab() {
        toggleMainMenu();
    }

    public boolean isQuestTabVisible() {
        return isMainMenuVisible();
    }

    public void update(float delta) {
        if (!topNotificationPersistent && topNotificationTimer > 0f) {
            topNotificationTimer = Math.max(0f, topNotificationTimer - delta);
        }
        if (!chatOpen) {
            chatCursorVisible = false;
            chatCursorTimer = 0f;
            return;
        }
        chatCursorTimer += Math.max(0f, delta);
        if (chatCursorTimer >= CHAT_CURSOR_BLINK_SECONDS) {
            chatCursorTimer = 0f;
            chatCursorVisible = !chatCursorVisible;
        }
    }

    public void draw(SpriteBatch batch, float currentHealth, float maxHealth, float currentMana, float maxMana, int playerLevel,
                     String menuQuestTitle, String menuQuestObjective, String currentRegionName, boolean showQuestPanel,
                     float bossCurrentHealth, float bossMaxHealth, float bossHudAlpha) {
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
        float overlayDrawY = overlayY + HUD_OVERLAY_Y_OFFSET + HUD_OVERLAY_EXTRA_Y_OFFSET;
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
        float avatarY = baseHudY + (overlayH - avatarHeight) / 2f + HUD_AVATAR_Y_OFFSET + HUD_AVATAR_EXTRA_Y_OFFSET;
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
        String healthTextFront = null;
        float healthTextFrontX = 0f;
        float healthTextFrontY = 0f;
        String levelTextFront = null;
        float levelTextFrontX = 0f;
        float levelTextFrontY = 0f;

        // Layer 7: text.
        if (uiFont != null && uiLayout != null) {
            uiFont.setColor(1f, 1f, 1f, 1f);
            String healthText = String.format(Locale.US, "%s / %s",
                    formatValue((int) currentHealth), formatValue((int) maxHealth));
            uiLayout.setText(uiFont, healthText);
            float healthTextX = healthBarX + (barWidth - uiLayout.width) / 2f + 40f;
            float healthTextY = healthY + barHeight - 2f;
            healthTextFront = healthText;
            healthTextFrontX = healthTextX;
            healthTextFrontY = healthTextY;

            String manaText = String.format(Locale.US, "%s / %s",
                    formatValue((int) currentMana), formatValue((int) maxMana));
            uiLayout.setText(uiFont, manaText);
            float manaTextX = barX + manaOffsetX + (barWidth - uiLayout.width) / 2f + 40f;
            float manaTextY = manaY + barHeight - 8f;
            uiFont.draw(batch, uiLayout, manaTextX, manaTextY);

            uiFont.setColor(0.55f, 0.8f, 0.4f, 1f);
            String levelText = Integer.toString(playerLevel);
            float baseUiFontScaleX = uiFont.getData().scaleX;
            float baseUiFontScaleY = uiFont.getData().scaleY;
            uiFont.getData().setScale(baseUiFontScaleX * HUD_LEVEL_TEXT_SCALE_MULTIPLIER,
                    baseUiFontScaleY * HUD_LEVEL_TEXT_SCALE_MULTIPLIER);
            uiLayout.setText(uiFont, levelText);
            float badgeWidth = uiLevelBadgeTex != null ? uiLevelBadgeTex.getWidth() : 33f;
            float badgeHeight = uiLevelBadgeTex != null ? uiLevelBadgeTex.getHeight() : 44f;
            float levelTextX = levelX + (badgeWidth - uiLayout.width) / 2f - 7f;
            float levelTextY = levelY + (badgeHeight + uiLayout.height) / 2f - 40f;
            levelTextFront = levelText;
            levelTextFrontX = levelTextX;
            levelTextFrontY = levelTextY;
            uiFont.getData().setScale(baseUiFontScaleX, baseUiFontScaleY);
        }

        // Layer 8: top frame (same texture) to cap the stack.
        batch.draw(uiOverlayBackTex, overlayX, overlayDrawY, overlayDrawWidth, overlayDrawHeight);

        drawTopNotification(batch);
        drawLetterboxBars(batch);
        drawBossHudBar(batch, bossCurrentHealth, bossMaxHealth, bossHudAlpha);
        questPanelToggleVisible = false;
        questPanelBounds.set(0f, 0f, 0f, 0f);
        questPanelToggleBounds.set(0f, 0f, 0f, 0f);
        if (showQuestPanel) {
            drawQuestPanel(batch, menuQuestTitle, menuQuestObjective, currentRegionName);
        }
        // Layer 9: HP text in front of all HUD layers.
        if (healthTextFront != null && uiFont != null && uiLayout != null) {
            uiFont.setColor(1f, 1f, 1f, 1f);
            uiLayout.setText(uiFont, healthTextFront);
            uiFont.draw(batch, uiLayout, healthTextFrontX, healthTextFrontY);
        }
        if (levelTextFront != null && uiFont != null && uiLayout != null) {
            float baseUiFontScaleX = uiFont.getData().scaleX;
            float baseUiFontScaleY = uiFont.getData().scaleY;
            uiFont.getData().setScale(baseUiFontScaleX * HUD_LEVEL_TEXT_SCALE_MULTIPLIER,
                    baseUiFontScaleY * HUD_LEVEL_TEXT_SCALE_MULTIPLIER);
            uiFont.setColor(0.55f, 0.8f, 0.4f, 1f);
            uiLayout.setText(uiFont, levelTextFront);
            uiFont.draw(batch, uiLayout, levelTextFrontX, levelTextFrontY);
            uiFont.setColor(1f, 1f, 1f, 1f);
            uiFont.getData().setScale(baseUiFontScaleX, baseUiFontScaleY);
        }
        drawMainMenu(batch, currentHealth, maxHealth, currentMana, maxMana, playerLevel,
                menuQuestTitle, menuQuestObjective, currentRegionName);
        drawChat(batch);
        batch.end();
    }

    private void drawBossHudBar(SpriteBatch batch, float bossCurrentHealth, float bossMaxHealth, float bossHudAlpha) {
        if (batch == null
                || uiCamera == null
                || uiBarBackgroundTex == null
                || uiBarBorderTex == null
                || uiBarHealthTex == null
                || bossMaxHealth <= 0f
                || bossCurrentHealth < 0f
                || bossHudAlpha <= 0f) {
            return;
        }

        float barWidth = BOSS_HUD_BAR_WIDTH;
        float barHeight = uiBarBackgroundTex.getHeight() * HUD_BAR_SCALE * BOSS_HUD_BAR_HEIGHT_SCALE;
        float borderWidth = barWidth * HUD_BAR_BORDER_SCALE;
        float borderHeight = barHeight * HUD_BAR_BORDER_SCALE;
        float borderInsetX = (borderWidth - barWidth) * 0.5f;
        float borderInsetY = (borderHeight - barHeight) * 0.5f;
        float x = (uiCamera.viewportWidth - barWidth) * 0.5f;
        float y = uiCamera.viewportHeight
                - Math.min(HUD_LETTERBOX_BAR_HEIGHT, uiCamera.viewportHeight * 0.5f)
                - BOSS_HUD_BAR_TOP_MARGIN
                - barHeight;

        float clampedAlpha = clamp01(bossHudAlpha);
        Color previousColor = batch.getColor();
        batch.setColor(previousColor.r, previousColor.g, previousColor.b, previousColor.a * clampedAlpha);
        drawBarBackground(batch, x, y, barWidth, barHeight);
        drawBarFill(batch, x, y, barWidth, barHeight, bossCurrentHealth / bossMaxHealth, uiBarHealthTex);
        drawBarBorder(batch, x - borderInsetX + HUD_BAR_BORDER_X_OFFSET, y - borderInsetY, borderWidth, borderHeight);
        batch.setColor(previousColor);
    }

    public void dispose() {
        if (uiFont != null) uiFont.dispose();
        if (uiNotificationTitleFont != null) uiNotificationTitleFont.dispose();
        if (uiNotificationMessageFont != null) uiNotificationMessageFont.dispose();
        if (uiMainMenuTitleFont != null) uiMainMenuTitleFont.dispose();
        if (uiMainMenuTabFont != null) uiMainMenuTabFont.dispose();
        if (uiMainMenuBodyFont != null) uiMainMenuBodyFont.dispose();
        if (uiQuestPanelHeaderFont != null) uiQuestPanelHeaderFont.dispose();
        if (uiQuestPanelQuestTitleFont != null) uiQuestPanelQuestTitleFont.dispose();
        if (uiQuestPanelPromptFont != null) uiQuestPanelPromptFont.dispose();
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
        if (uiMainMenuPanelTex != null) uiMainMenuPanelTex.dispose();
        if (uiMainMenuPanel1Tex != null) uiMainMenuPanel1Tex.dispose();
        if (uiMainMenuPanel2Tex != null) uiMainMenuPanel2Tex.dispose();
        if (uiMainMenuPanel3Tex != null) uiMainMenuPanel3Tex.dispose();
        if (uiMainMenuPanel4Tex != null) uiMainMenuPanel4Tex.dispose();
        if (uiMainMenuTabSelectedTex != null) uiMainMenuTabSelectedTex.dispose();
        if (uiMainMenuCloseTex != null) uiMainMenuCloseTex.dispose();
        if (uiQuestPanelTex != null) uiQuestPanelTex.dispose();
        if (uiQuestPanelClosedTex != null) uiQuestPanelClosedTex.dispose();
        if (uiQuestPanelIconOpenTex != null) uiQuestPanelIconOpenTex.dispose();
        if (uiQuestPanelIconCloseTex != null) uiQuestPanelIconCloseTex.dispose();
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

    public void drawLetterboxOverlay(SpriteBatch batch) {
        if (batch == null || uiCamera == null || uiBlackBarTex == null) {
            return;
        }
        uiCamera.update();
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        batch.setShader(null);
        batch.setColor(1f, 1f, 1f, 1f);
        drawLetterboxBars(batch);
        batch.end();
    }

    private void drawTopNotification(SpriteBatch batch) {
        if ((!topNotificationPersistent && topNotificationTimer <= 0f)
                || topNotificationTitle == null
                || topNotificationTitle.isBlank()
                || topNotificationMessage == null
                || topNotificationMessage.isBlank()
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
        float alpha = topNotificationAlpha();
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
        String summaryMessage = summarizeNotificationMessage(
                uiNotificationMessageFont,
                topNotificationMessage,
                Math.max(1f, panelWidth - 92f * scale));

        drawCenteredText(batch, uiNotificationTitleFont, topNotificationTitle, textCenterX, titleY,
                0.8902f, 0.5216f, 0f, 1f, alpha);
        drawCenteredText(batch, uiNotificationMessageFont, summaryMessage, textCenterX, messageY,
                0.83f, 0.81f, 0.75f, 1f, alpha);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void drawQuestPanel(SpriteBatch batch, String questTitle, String questObjective, String currentRegionName) {
        questPanelToggleVisible = false;
        questPanelBounds.set(0f, 0f, 0f, 0f);
        questPanelToggleBounds.set(0f, 0f, 0f, 0f);
        if (mainMenuVisible
                || uiCamera == null
                || uiQuestPanelTex == null
                || uiQuestPanelHeaderFont == null
                || uiQuestPanelQuestTitleFont == null
                || uiQuestPanelPromptFont == null
                || uiLayout == null
                || questTitle == null
                || questTitle.isBlank()
                || "No active quest".equalsIgnoreCase(questTitle.trim())) {
            return;
        }

        float maxHeight = uiCamera.viewportHeight * QUEST_PANEL_MAX_HEIGHT_RATIO;
        float maxWidth = Math.max(1f, uiCamera.viewportWidth - QUEST_PANEL_SIDE_MARGIN * 2f);
        float scale = Math.min(
                1f,
                Math.min(maxHeight / uiQuestPanelTex.getHeight(), maxWidth / uiQuestPanelTex.getWidth()));
        Texture panelTexture = questPanelExpanded || uiQuestPanelClosedTex == null ? uiQuestPanelTex : uiQuestPanelClosedTex;
        float openDrawHeight = uiQuestPanelTex.getHeight() * scale;
        float drawWidth = panelTexture.getWidth() * scale;
        float drawHeight = panelTexture.getHeight() * scale;
        float panelX = uiCamera.viewportWidth - drawWidth - QUEST_PANEL_SIDE_MARGIN
                + (questPanelExpanded ? 0f : QUEST_PANEL_CLOSED_X_OFFSET);
        float openPanelY = (uiCamera.viewportHeight - openDrawHeight) * 0.5f + QUEST_PANEL_VERTICAL_OFFSET;
        float panelTopY = openPanelY + openDrawHeight;
        float panelY = panelTopY - drawHeight;
        float panelCenterX = panelX + drawWidth * 0.5f;
        questPanelBounds.set(panelX, panelY, drawWidth, drawHeight);
        questPanelToggleBounds.set(
                panelX,
                panelTopY - QUEST_PANEL_TOGGLE_HITBOX_HEIGHT * scale,
                QUEST_PANEL_TOGGLE_HITBOX_WIDTH * scale,
                QUEST_PANEL_TOGGLE_HITBOX_HEIGHT * scale);
        questPanelToggleVisible = true;

        batch.setColor(1f, 1f, 1f, 0.97f);
        batch.draw(panelTexture, panelX, panelY, drawWidth, drawHeight);
        Texture iconTexture = questPanelExpanded ? uiQuestPanelIconOpenTex : uiQuestPanelIconCloseTex;
        if (iconTexture != null) {
            float iconWidth = iconTexture.getWidth() * scale;
            float iconHeight = iconTexture.getHeight() * scale;
            float iconX = panelX + QUEST_PANEL_ICON_OFFSET_X * scale;
            float iconY = panelTopY - (QUEST_PANEL_ICON_OFFSET_Y * scale) - iconHeight;
            batch.draw(iconTexture, iconX, iconY, iconWidth, iconHeight);
        }
        batch.setColor(1f, 1f, 1f, 1f);
        if (!questPanelExpanded) {
            return;
        }

        float contentX = panelX + QUEST_PANEL_TEXT_LEFT_PADDING * scale;
        float contentWidth = drawWidth - (QUEST_PANEL_TEXT_LEFT_PADDING + QUEST_PANEL_TEXT_RIGHT_PADDING) * scale;
        float questHeaderBaselineY = panelY + drawHeight - QUEST_PANEL_HEADER_TOP_OFFSET * scale;
        float questTitleBaselineY = panelY + drawHeight - QUEST_PANEL_QUEST_TITLE_BASELINE_TOP_OFFSET * scale;
        float objectiveTextBaselineY = panelY + drawHeight - QUEST_PANEL_OBJECTIVE_TEXT_BASELINE_TOP_OFFSET * scale;
        float objectiveTextBottomY = panelY + QUEST_PANEL_OBJECTIVE_TEXT_BOTTOM_OFFSET * scale;
        float regionBaselineY = panelY + QUEST_PANEL_AREA_BASELINE_OFFSET_Y * scale;

        drawMenuCenteredTextScaled(batch, uiQuestPanelHeaderFont, "Objectives",
                panelCenterX, questHeaderBaselineY,
                0.84f, 0.84f, 0.84f, 1f,
                QUEST_PANEL_HEADER_SCALE);
        drawWrappedMenuTextScaled(batch, uiQuestPanelHeaderFont, questTitle,
                new Rectangle(contentX, questTitleBaselineY - 30f * scale, contentWidth, QUEST_PANEL_QUEST_TITLE_BOX_HEIGHT * scale),
                questTitleBaselineY,
                Align.left,
                0.82f, 0.82f, 0.82f, 0.96f,
                QUEST_PANEL_QUEST_TITLE_SCALE);
        drawWrappedMenuTextScaled(batch, uiQuestPanelHeaderFont,
                questObjective == null || questObjective.isBlank() ? "Explore the world." : questObjective,
                new Rectangle(contentX, objectiveTextBottomY, contentWidth, objectiveTextBaselineY - objectiveTextBottomY),
                objectiveTextBaselineY,
                Align.left,
                0.54f, 0.77f, 0.26f, 1f,
                QUEST_PANEL_OBJECTIVE_TEXT_SCALE);
        if (currentRegionName != null && !currentRegionName.isBlank()) {
            drawWrappedMenuTextScaled(batch, uiQuestPanelHeaderFont, "Area: " + currentRegionName,
                    new Rectangle(contentX, panelY + 16f * scale, contentWidth, 28f * scale),
                    regionBaselineY,
                    Align.left,
                    0.68f, 0.68f, 0.68f, 1f,
                    QUEST_PANEL_AREA_TEXT_SCALE);
        }
    }

    private void drawMainMenu(SpriteBatch batch, float currentHealth, float maxHealth,
                              float currentStamina, float maxStamina, int playerLevel,
                              String menuQuestTitle, String menuQuestObjective, String currentRegionName) {
        if (!mainMenuVisible || uiMainMenuPanelTex == null || uiCamera == null || uiLayout == null) {
            return;
        }

        updateMainMenuLayout();

        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(
                uiMainMenuPanelTex,
                mainMenuPanelBounds.x,
                mainMenuPanelBounds.y,
                mainMenuPanelBounds.width,
                mainMenuPanelBounds.height);

        float menuCenterX = mainMenuPanelBounds.x + mainMenuPanelBounds.width * 0.5f;
        if (uiMainMenuTitleFont != null) {
            drawMenuCenteredText(batch, uiMainMenuTitleFont, "Pause",
                    menuCenterX, mainMenuPanelBounds.y + mainMenuPanelBounds.height * 0.935f + 8f,
                    0.84f, 0.84f, 0.84f, 1f);
        }

        for (int i = 0; i < MAIN_MENU_TABS.length; i++) {
            Rectangle tabBounds = mainMenuTabBounds[i];
            boolean active = activePauseMenuTab.ordinal() == i;
            if (active && uiMainMenuTabSelectedTex != null) {
                float selectedW = Math.max(tabBounds.width + 18f, uiMainMenuTabSelectedTex.getWidth() * 0.22f);
                float selectedH = uiMainMenuTabSelectedTex.getHeight() * 0.28f;
                float selectedX = tabBounds.x + (tabBounds.width - selectedW) * 0.5f;
                float selectedY = tabBounds.y + (tabBounds.height - selectedH) * 0.40f;
                batch.setColor(1f, 1f, 1f, 0.85f);
                batch.draw(uiMainMenuTabSelectedTex, selectedX, selectedY, selectedW, selectedH);
                batch.setColor(1f, 1f, 1f, 1f);
            }
            if (uiMainMenuTabFont != null) {
                drawMenuCenteredText(batch, uiMainMenuTabFont, MAIN_MENU_TABS[i],
                        tabBounds.x + tabBounds.width * 0.5f,
                        tabBounds.y + tabBounds.height * 0.70f,
                        active ? 0.83f : 0.69f,
                        active ? 0.26f : 0.69f,
                        active ? 0.20f : 0.69f,
                        1f);
            }
        }

        if (uiMainMenuCloseTex != null) {
            batch.draw(uiMainMenuCloseTex,
                    mainMenuCloseBounds.x,
                    mainMenuCloseBounds.y,
                    mainMenuCloseBounds.width,
                    mainMenuCloseBounds.height);
        }

        if (uiMainMenuPanel3Tex != null) {
            batch.setColor(1f, 1f, 1f, 0.95f);
            batch.draw(uiMainMenuPanel3Tex,
                    mainMenuLeftCardBounds.x,
                    mainMenuLeftCardBounds.y,
                    mainMenuLeftCardBounds.width,
                    mainMenuLeftCardBounds.height);
        }
        if (uiMainMenuPanel4Tex != null) {
            batch.setColor(1f, 1f, 1f, 0.95f);
            batch.draw(uiMainMenuPanel4Tex,
                    mainMenuRightCardBounds.x,
                    mainMenuRightCardBounds.y,
                    mainMenuRightCardBounds.width,
                    mainMenuRightCardBounds.height);
        }
        if (uiMainMenuPanel2Tex != null) {
            batch.setColor(1f, 1f, 1f, 0.82f);
            batch.draw(uiMainMenuPanel2Tex,
                    mainMenuCenterCardBounds.x,
                    mainMenuCenterCardBounds.y,
                    mainMenuCenterCardBounds.width,
                    mainMenuCenterCardBounds.height);
        }
        batch.setColor(1f, 1f, 1f, 1f);

        if (uiMainMenuBodyFont != null) {
            float bodyLineStep = 22f;
            String safeQuestTitle = menuQuestTitle == null || menuQuestTitle.isBlank() ? "No active quest" : menuQuestTitle;
            String safeQuestObjective = menuQuestObjective == null || menuQuestObjective.isBlank()
                    ? "Explore the world."
                    : menuQuestObjective;
            String safeRegion = currentRegionName == null || currentRegionName.isBlank() ? "Unknown" : currentRegionName;

            drawMenuCenteredText(batch, uiMainMenuBodyFont, "Region: " + safeRegion,
                    mainMenuCenterCardBounds.x + mainMenuCenterCardBounds.width * 0.5f,
                    mainMenuCenterCardBounds.y + mainMenuCenterCardBounds.height * 0.60f,
                    0.72f, 0.72f, 0.72f, 1f);
            drawMenuCenteredText(batch, uiMainMenuBodyFont, "Esc or M closes this menu",
                    mainMenuCenterCardBounds.x + mainMenuCenterCardBounds.width * 0.5f,
                    mainMenuCenterCardBounds.y + mainMenuCenterCardBounds.height * 0.35f,
                    0.66f, 0.66f, 0.66f, 1f);

            if (activePauseMenuTab == PauseMenuTab.GAME) {
                drawMenuCenteredText(batch, uiMainMenuBodyFont, "Game",
                        mainMenuLeftCardBounds.x + mainMenuLeftCardBounds.width * 0.5f + GAME_MENU_GROUP_X_OFFSET,
                        mainMenuLeftCardBounds.y + mainMenuLeftCardBounds.height - 26f,
                        0.78f, 0.78f, 0.78f, 1f);
                drawMenuCenteredText(batch, uiMainMenuBodyFont, "Hero",
                        mainMenuRightCardBounds.x + mainMenuRightCardBounds.width * 0.5f,
                        mainMenuRightCardBounds.y + mainMenuRightCardBounds.height - 26f,
                        0.83f, 0.73f, 0.73f, 1f);
                for (int i = 0; i < GAME_MENU_ACTIONS.length; i++) {
                    drawMenuRow(batch, gameActionBounds[i], GAME_MENU_ACTIONS[i],
                            selectedGameActionIndex == i && confirmationAction == PauseMenuAction.NONE,
                            i == GAME_MENU_ACTIONS.length - 1,
                            MAIN_MENU_CHOICE_TEXT_SCALE);
                }
                float heroBaseY = mainMenuRightCardBounds.y + mainMenuRightCardBounds.height - 62f;
                drawMenuCenteredText(batch, uiMainMenuBodyFont,
                        "Level " + playerLevel + " / " + PlayerLevelStats.MAX_LEVEL,
                        mainMenuRightCardBounds.x + mainMenuRightCardBounds.width * 0.5f,
                        heroBaseY,
                        0.78f, 0.66f, 0.66f, 1f);
                drawMenuCenteredText(batch, uiMainMenuBodyFont,
                        "Health: " + formatValue((int) currentHealth) + " / " + formatValue((int) maxHealth),
                        mainMenuRightCardBounds.x + mainMenuRightCardBounds.width * 0.5f,
                        heroBaseY - bodyLineStep,
                        0.78f, 0.66f, 0.66f, 1f);
                drawMenuCenteredText(batch, uiMainMenuBodyFont,
                        "Stamina: " + formatValue((int) currentStamina) + " / " + formatValue((int) maxStamina),
                        mainMenuRightCardBounds.x + mainMenuRightCardBounds.width * 0.5f,
                        heroBaseY - bodyLineStep * 2f,
                        0.78f, 0.66f, 0.66f, 1f);
                drawMenuCenteredText(batch, uiMainMenuBodyFont,
                        "Current Region: " + safeRegion,
                        mainMenuRightCardBounds.x + mainMenuRightCardBounds.width * 0.5f,
                        heroBaseY - bodyLineStep * 3f,
                        0.78f, 0.66f, 0.66f, 1f);
            } else {
                drawMenuCenteredText(batch, uiMainMenuBodyFont, "Settings",
                        mainMenuLeftCardBounds.x + mainMenuLeftCardBounds.width * 0.5f + GAME_MENU_GROUP_X_OFFSET,
                        mainMenuLeftCardBounds.y + mainMenuLeftCardBounds.height - 26f,
                        0.78f, 0.78f, 0.78f, 1f);
                drawMenuCenteredText(batch, uiMainMenuBodyFont, "Selected",
                        mainMenuRightCardBounds.x + mainMenuRightCardBounds.width * 0.5f,
                        mainMenuRightCardBounds.y + mainMenuRightCardBounds.height - 26f,
                        0.83f, 0.73f, 0.73f, 1f);
                for (int i = 0; i < SETTINGS_MENU_ITEMS.length; i++) {
                    String valueText = settingsValueText(i);
                    drawSettingsRow(batch, i, settingsRowBounds[i], SETTINGS_MENU_ITEMS[i], valueText,
                            selectedSettingsIndex == i && confirmationAction == PauseMenuAction.NONE,
                            MAIN_MENU_CHOICE_TEXT_SCALE);
                }
                drawMenuCenteredText(batch, uiMainMenuBodyFont, SETTINGS_MENU_ITEMS[selectedSettingsIndex],
                        mainMenuRightCardBounds.x + mainMenuRightCardBounds.width * 0.5f,
                        mainMenuRightCardBounds.y + mainMenuRightCardBounds.height - 62f,
                        0.79f, 0.73f, 0.58f, 1f);
                drawWrappedMenuText(batch, uiMainMenuBodyFont, settingsDescriptionText(selectedSettingsIndex),
                        new Rectangle(mainMenuRightCardBounds.x + 16f, mainMenuRightCardBounds.y + 30f,
                                mainMenuRightCardBounds.width - 32f, mainMenuRightCardBounds.height - 96f),
                        mainMenuRightCardBounds.y + mainMenuRightCardBounds.height - 94f,
                        Align.center,
                        0.72f, 0.72f, 0.72f, 1f);
            }
        }

        if (confirmationAction != PauseMenuAction.NONE) {
            drawMainMenuConfirmation(batch);
        }
    }

    private void updateMainMenuLayout() {
        if (uiCamera == null || uiMainMenuPanelTex == null || uiLayout == null) {
            return;
        }

        float maxWidth = uiCamera.viewportWidth * MAIN_MENU_MAX_WIDTH_RATIO;
        float maxHeight = uiCamera.viewportHeight * MAIN_MENU_MAX_HEIGHT_RATIO;
        float scale = Math.min(maxWidth / uiMainMenuPanelTex.getWidth(), maxHeight / uiMainMenuPanelTex.getHeight())
                * MAIN_MENU_SCALE;
        float drawWidth = uiMainMenuPanelTex.getWidth() * scale;
        float drawHeight = uiMainMenuPanelTex.getHeight() * scale;
        float drawX = (uiCamera.viewportWidth - drawWidth) * 0.5f;
        float drawY = (uiCamera.viewportHeight - drawHeight) * 0.5f;
        float menuCenterX = drawX + drawWidth * 0.5f;

        mainMenuPanelBounds.set(drawX, drawY, drawWidth, drawHeight);

        float tabBaselineY = drawY + drawHeight * 0.865f;
        float tabGap = 30f * scale;
        float totalTabsWidth = 0f;
        float[] tabWidths = new float[MAIN_MENU_TABS.length];
        for (int i = 0; i < MAIN_MENU_TABS.length; i++) {
            if (uiMainMenuTabFont != null) {
                uiLayout.setText(uiMainMenuTabFont, MAIN_MENU_TABS[i]);
                tabWidths[i] = uiLayout.width;
            } else {
                tabWidths[i] = 80f * scale;
            }
            totalTabsWidth += tabWidths[i];
            if (i < MAIN_MENU_TABS.length - 1) {
                totalTabsWidth += tabGap;
            }
        }
        float cursorX = menuCenterX - totalTabsWidth * 0.5f;
        for (int i = 0; i < MAIN_MENU_TABS.length; i++) {
            mainMenuTabBounds[i].set(cursorX - 10f, tabBaselineY - 18f, tabWidths[i] + 20f, 28f);
            cursorX += tabWidths[i] + (i < MAIN_MENU_TABS.length - 1 ? tabGap : 0f);
        }

        float closeW = uiMainMenuCloseTex != null ? uiMainMenuCloseTex.getWidth() * scale * 0.60f : 32f;
        float closeH = uiMainMenuCloseTex != null ? uiMainMenuCloseTex.getHeight() * scale * 0.60f : 32f;
        mainMenuCloseBounds.set(drawX + drawWidth - closeW - 16f * scale, drawY + drawHeight - closeH - 10f * scale, closeW, closeH);

        float cardY = drawY + drawHeight * 0.18f;
        float cardW = drawWidth * 0.25f;
        float cardH = drawHeight * 0.40f;
        float leftCardX = drawX + drawWidth * 0.10f;
        float rightCardX = drawX + drawWidth * 0.65f;
        mainMenuLeftCardBounds.set(leftCardX, cardY, cardW, cardH);
        mainMenuRightCardBounds.set(rightCardX, cardY, cardW, cardH);
        mainMenuCenterCardBounds.set(menuCenterX - drawWidth * 0.17f, drawY + drawHeight * 0.40f, drawWidth * 0.34f, drawHeight * 0.14f);

        float rowHeight = 34f * scale;
        float rowGap = 12f * scale;
        float gameRowWidth = (cardW - 36f * scale) * 0.75f;
        float gameRowX = leftCardX + (cardW - gameRowWidth) * 0.5f + GAME_MENU_GROUP_X_OFFSET;
        float gameStartY = cardY + cardH - 92f * scale;
        for (int i = 0; i < gameActionBounds.length; i++) {
            float rowY = gameStartY - i * (rowHeight + rowGap);
            gameActionBounds[i].set(gameRowX, rowY, gameRowWidth, rowHeight);
        }

        float settingsRowX = gameRowX;
        float settingsRowWidth = gameRowWidth;
        float settingsStartY = cardY + cardH - 92f * scale;
        float valueAreaWidth = Math.max(72f * scale, settingsRowWidth * 0.34f);
        for (int i = 0; i < settingsRowBounds.length; i++) {
            float rowY = settingsStartY - i * (rowHeight + rowGap);
            settingsRowBounds[i].set(settingsRowX, rowY, settingsRowWidth, rowHeight);
            settingsDecreaseBounds[i].set(settingsRowX + settingsRowWidth - valueAreaWidth, rowY, valueAreaWidth * 0.5f, rowHeight);
            settingsIncreaseBounds[i].set(settingsRowX + settingsRowWidth - valueAreaWidth * 0.5f, rowY, valueAreaWidth * 0.5f, rowHeight);
            float sliderTrackWidth = Math.max(SETTINGS_SLIDER_TRACK_MIN_WIDTH * scale, settingsRowWidth * SETTINGS_SLIDER_TRACK_WIDTH_RATIO);
            float sliderTrackHeight = Math.max(4f * scale, rowHeight * SETTINGS_SLIDER_TRACK_HEIGHT_RATIO);
            float sliderValueWidth = Math.max(SETTINGS_SLIDER_VALUE_MIN_WIDTH * scale, valueAreaWidth - sliderTrackWidth - SETTINGS_SLIDER_VALUE_GAP * scale);
            float sliderValueX = settingsRowX + settingsRowWidth - SETTINGS_SLIDER_RIGHT_PADDING * scale - sliderValueWidth;
            float sliderTrackX = sliderValueX - SETTINGS_SLIDER_VALUE_GAP * scale - sliderTrackWidth;
            float sliderTrackY = rowY + (rowHeight - sliderTrackHeight) * 0.5f;
            settingsSliderTrackBounds[i].set(sliderTrackX, sliderTrackY, sliderTrackWidth, sliderTrackHeight);
            settingsSliderValueBounds[i].set(sliderValueX, rowY, sliderValueWidth, rowHeight);
        }

        float confirmWidth = drawWidth * 0.42f;
        float confirmHeight = drawHeight * 0.28f;
        confirmationBounds.set(menuCenterX - confirmWidth * 0.5f, drawY + drawHeight * 0.32f, confirmWidth, confirmHeight);
        float buttonWidth = confirmWidth * 0.28f;
        float buttonHeight = 32f * scale;
        float buttonY = confirmationBounds.y + 24f * scale;
        confirmationYesBounds.set(
                confirmationBounds.x + confirmWidth * 0.18f,
                buttonY,
                buttonWidth,
                buttonHeight);
        confirmationNoBounds.set(
                confirmationBounds.x + confirmWidth * 0.54f,
                buttonY,
                buttonWidth,
                buttonHeight);
    }

    private PauseMenuAction activateGameMenuAction(int index) {
        PauseMenuAction action = switch (index) {
            case 0 -> PauseMenuAction.RESUME;
            case 1 -> PauseMenuAction.MAIN_MENU;
            case 2 -> PauseMenuAction.RELOAD_AUTOSAVE;
            case 3 -> PauseMenuAction.EXIT_GAME;
            default -> PauseMenuAction.NONE;
        };
        if (action == PauseMenuAction.RELOAD_AUTOSAVE || action == PauseMenuAction.EXIT_GAME) {
            confirmationAction = action;
            selectedConfirmationIndex = 1;
            return PauseMenuAction.NONE;
        }
        return action;
    }

    private void drawMenuRow(SpriteBatch batch, Rectangle bounds, String text, boolean selected, boolean danger, float textScale) {
        if (uiBlackBarTex == null || uiMainMenuBodyFont == null) {
            return;
        }
        float alpha = selected ? 0.78f : 0.42f;
        if (selected) {
            batch.setColor(danger ? 0.55f : 0.40f, danger ? 0.16f : 0.24f, danger ? 0.16f : 0.18f, alpha);
        } else {
            batch.setColor(0f, 0f, 0f, alpha);
        }
        batch.draw(uiBlackBarTex, bounds.x, bounds.y, bounds.width, bounds.height);
        batch.setColor(1f, 1f, 1f, 1f);
        drawMenuCenteredTextScaled(batch, uiMainMenuBodyFont, text,
                bounds.x + bounds.width * 0.5f,
                bounds.y + bounds.height * 0.68f,
                danger && selected ? 0.92f : 0.86f,
                selected ? (danger ? 0.48f : 0.75f) : 0.82f,
                selected ? (danger ? 0.40f : 0.66f) : 0.82f,
                1f,
                textScale);
    }

    private void drawSettingsRow(SpriteBatch batch, int index, Rectangle bounds, String label, String value, boolean selected, float textScale) {
        if (uiBlackBarTex == null || uiMainMenuBodyFont == null) {
            return;
        }
        batch.setColor(selected ? 0.40f : 0f, selected ? 0.24f : 0f, selected ? 0.18f : 0f, selected ? 0.78f : 0.42f);
        batch.draw(uiBlackBarTex, bounds.x, bounds.y, bounds.width, bounds.height);
        batch.setColor(1f, 1f, 1f, 1f);
        drawMenuTextScaled(batch, uiMainMenuBodyFont, label,
                bounds.x + 10f,
                bounds.y + bounds.height * 0.68f,
                0.82f, 0.82f, 0.82f, 1f, textScale);
        if (isSliderSetting(index)) {
            drawSettingsSlider(batch, index, selected, textScale);
            drawMenuTextScaled(batch, uiMainMenuBodyFont, value,
                    settingsSliderValueBounds[index].x,
                    bounds.y + bounds.height * 0.68f,
                    selected ? 0.90f : 0.76f,
                    selected ? 0.74f : 0.76f,
                    selected ? 0.58f : 0.76f,
                    1f,
                    textScale);
            return;
        }
        drawMenuTextScaled(batch, uiMainMenuBodyFont, value,
                bounds.x + bounds.width - 78f,
                bounds.y + bounds.height * 0.68f,
                selected ? 0.90f : 0.76f,
                selected ? 0.74f : 0.76f,
                selected ? 0.58f : 0.76f,
                1f,
                textScale);
    }

    private void drawSettingsSlider(SpriteBatch batch, int index, boolean selected, float textScale) {
        Rectangle trackBounds = settingsSliderTrackBounds[index];
        if (trackBounds == null || trackBounds.width <= 0f || trackBounds.height <= 0f || settingsService == null) {
            return;
        }

        float progress = isSliderSetting(index)
                ? clamp01(settingsVolumePercent(index) / 100f)
                : 0f;
        float filledWidth = trackBounds.width * progress;
        batch.setColor(0f, 0f, 0f, selected ? 0.72f : 0.52f);
        batch.draw(uiBlackBarTex, trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        batch.setColor(selected ? 0.92f : 0.74f, selected ? 0.70f : 0.62f, selected ? 0.34f : 0.44f, 0.95f);
        batch.draw(uiBlackBarTex, trackBounds.x, trackBounds.y, filledWidth, trackBounds.height);

        float thumbWidth = Math.min(trackBounds.width, Math.max(SETTINGS_SLIDER_THUMB_WIDTH, trackBounds.height * 1.2f));
        float thumbHeight = trackBounds.height * SETTINGS_SLIDER_THUMB_HEIGHT_RATIO;
        float thumbCenterX = trackBounds.x + filledWidth;
        if (progress <= 0f) {
            thumbCenterX = trackBounds.x;
        } else if (progress >= 1f) {
            thumbCenterX = trackBounds.x + trackBounds.width;
        }
        float thumbX = Math.max(trackBounds.x, Math.min(trackBounds.x + trackBounds.width - thumbWidth, thumbCenterX - thumbWidth * 0.5f));
        float thumbY = trackBounds.y + (trackBounds.height - thumbHeight) * 0.5f;

        batch.setColor(0f, 0f, 0f, 0.90f);
        batch.draw(uiBlackBarTex, thumbX - 1f, thumbY - 1f, thumbWidth + 2f, thumbHeight + 2f);
        batch.setColor(selected ? 0.96f : 0.88f, selected ? 0.84f : 0.76f, selected ? 0.56f : 0.66f, 1f);
        batch.draw(uiBlackBarTex, thumbX, thumbY, thumbWidth, thumbHeight);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void drawMainMenuConfirmation(SpriteBatch batch) {
        if (uiBlackBarTex == null || uiMainMenuBodyFont == null) {
            return;
        }
        batch.setColor(0f, 0f, 0f, 0.68f);
        batch.draw(uiBlackBarTex, 0f, 0f, uiCamera.viewportWidth, uiCamera.viewportHeight);
        batch.setColor(1f, 1f, 1f, 1f);

        Texture panel = uiMainMenuPanel1Tex != null ? uiMainMenuPanel1Tex : uiMainMenuPanel2Tex;
        if (panel != null) {
            batch.draw(panel, confirmationBounds.x, confirmationBounds.y, confirmationBounds.width, confirmationBounds.height);
        } else {
            batch.setColor(0f, 0f, 0f, 0.90f);
            batch.draw(uiBlackBarTex, confirmationBounds.x, confirmationBounds.y, confirmationBounds.width, confirmationBounds.height);
            batch.setColor(1f, 1f, 1f, 1f);
        }

        drawMenuCenteredText(batch, uiMainMenuBodyFont, confirmationTitleText(),
                confirmationBounds.x + confirmationBounds.width * 0.5f,
                confirmationBounds.y + confirmationBounds.height - 28f,
                0.82f, 0.78f, 0.72f, 1f);
        drawWrappedMenuText(batch, uiMainMenuBodyFont, confirmationMessageText(),
                new Rectangle(confirmationBounds.x + 18f, confirmationBounds.y + 58f, confirmationBounds.width - 36f, confirmationBounds.height - 96f),
                confirmationBounds.y + confirmationBounds.height - 64f,
                Align.center,
                0.74f, 0.74f, 0.74f, 1f);

        drawMenuRow(batch, confirmationYesBounds, "Yes", selectedConfirmationIndex == 0, false, 1f);
        drawMenuRow(batch, confirmationNoBounds, "No", selectedConfirmationIndex == 1, false, 1f);
    }

    private String settingsValueText(int index) {
        if (settingsService == null) {
            return "--";
        }
        return switch (index) {
            case 0 -> settingsService.getMusicVolumePercent() + "%";
            case 1 -> settingsService.getSfxVolumePercent() + "%";
            case 2 -> settingsService.isFullscreen() ? "On" : "Off";
            case 3 -> settingsService.isVsyncEnabled() ? "On" : "Off";
            default -> "";
        };
    }

    private String settingsDescriptionText(int index) {
        return switch (index) {
            case 0 -> "Adjust the volume for music-style playback and long audio cues.";
            case 1 -> "Adjust the volume for attacks, impacts, and other sound effects.";
            case 2 -> "Toggle fullscreen display immediately.";
            case 3 -> "Toggle vertical sync immediately.";
            default -> "";
        };
    }

    private String confirmationTitleText() {
        return switch (confirmationAction) {
            case RELOAD_AUTOSAVE -> "Reload Save?";
            case EXIT_GAME -> "Exit Game?";
            default -> "";
        };
    }

    private String confirmationMessageText() {
        return switch (confirmationAction) {
            case RELOAD_AUTOSAVE -> "Discard the current unsaved moment-to-moment progress and reload the current save.";
            case EXIT_GAME -> "Close the game now?";
            default -> "";
        };
    }

    private void drawWrappedMenuText(SpriteBatch batch, BitmapFont font, String text, Rectangle bounds,
                                     float baselineY, int align, float r, float g, float b, float a) {
        if (font == null || text == null || text.isBlank() || uiLayout == null) {
            return;
        }
        Color color = new Color(r, g, b, a);
        uiLayout.setText(font, text, color, bounds.width, align, true);
        font.setColor(0f, 0f, 0f, 0.85f * a);
        font.draw(batch, uiLayout, bounds.x + 1f, baselineY - 1f);
        font.setColor(r, g, b, a);
        font.draw(batch, uiLayout, bounds.x, baselineY);
        font.setColor(1f, 1f, 1f, 1f);
    }

    private void drawWrappedMenuTextScaled(SpriteBatch batch, BitmapFont font, String text, Rectangle bounds,
                                           float baselineY, int align, float r, float g, float b, float a,
                                           float scaleMultiplier) {
        if (font == null || text == null || text.isBlank() || uiLayout == null) {
            return;
        }
        float baseScaleX = font.getData().scaleX;
        float baseScaleY = font.getData().scaleY;
        font.getData().setScale(baseScaleX * scaleMultiplier, baseScaleY * scaleMultiplier);
        drawWrappedMenuText(batch, font, text, bounds, baselineY, align, r, g, b, a);
        font.getData().setScale(baseScaleX, baseScaleY);
    }

    private void drawChat(SpriteBatch batch) {
        if (!chatOpen || uiCamera == null || uiBlackBarTex == null || uiLayout == null) {
            return;
        }

        BitmapFont chatFont = uiMainMenuBodyFont != null ? uiMainMenuBodyFont : uiFont;
        if (chatFont == null) {
            return;
        }

        float panelWidth = Math.min(CHAT_PANEL_WIDTH, uiCamera.viewportWidth - CHAT_PANEL_LEFT_MARGIN * 2f);
        float panelHeight = Math.min(CHAT_PANEL_HEIGHT, uiCamera.viewportHeight - CHAT_PANEL_BOTTOM_MARGIN * 2f);
        float panelX = CHAT_PANEL_LEFT_MARGIN;
        float panelY = CHAT_PANEL_BOTTOM_MARGIN;
        float border = CHAT_BORDER_THICKNESS;
        float inputWidth = panelWidth - CHAT_PADDING * 2f;
        float inputX = panelX + CHAT_PADDING;
        float inputY = panelY + CHAT_PADDING;

        batch.setColor(0f, 0f, 0f, CHAT_PANEL_ALPHA);
        batch.draw(uiBlackBarTex, panelX, panelY, panelWidth, panelHeight);
        batch.setColor(1f, 1f, 1f, 0.95f);
        batch.draw(uiBlackBarTex, panelX, panelY, panelWidth, border);
        batch.draw(uiBlackBarTex, panelX, panelY + panelHeight - border, panelWidth, border);
        batch.draw(uiBlackBarTex, panelX, panelY, border, panelHeight);
        batch.draw(uiBlackBarTex, panelX + panelWidth - border, panelY, border, panelHeight);

        float titleY = panelY + panelHeight - CHAT_PADDING;
        drawMenuText(batch, chatFont, "Chat", panelX + CHAT_PADDING, titleY, 0.91f, 0.91f, 0.91f, 1f);

        float hintY = inputY + CHAT_INPUT_HEIGHT + 20f;
        drawMenuText(batch, chatFont, "Enter send | Esc close", inputX, hintY, 0.63f, 0.63f, 0.63f, 1f);

        float lineY = titleY - 26f;
        int start = Math.max(0, chatMessages.size - CHAT_VISIBLE_MESSAGES);
        for (int i = start; i < chatMessages.size; i++) {
            String message = fitText(chatFont, chatMessages.get(i), inputWidth);
            drawMenuText(batch, chatFont, message, inputX, lineY, 0.78f, 0.78f, 0.78f, 1f);
            lineY -= CHAT_MESSAGE_LINE_HEIGHT;
        }

        batch.setColor(0f, 0f, 0f, CHAT_INPUT_ALPHA);
        batch.draw(uiBlackBarTex, inputX, inputY, inputWidth, CHAT_INPUT_HEIGHT);
        batch.setColor(1f, 1f, 1f, 1f);

        String input = "> " + chatDraft;
        if (chatCursorVisible) {
            input += "_";
        }
        input = fitText(chatFont, input, inputWidth - 10f);
        drawMenuText(batch, chatFont, input, inputX + 5f, inputY + CHAT_INPUT_HEIGHT - 8f, 0.94f, 0.94f, 0.94f, 1f);
    }

    private float topNotificationAlpha() {
        if (topNotificationPersistent) {
            return 1f;
        }
        if (topNotificationDuration <= 0f) {
            return 1f;
        }
        float fadeSeconds = Math.min(VILLAGE_SPAWN_NOTIFICATION_FADE_SECONDS,
                topNotificationDuration * 0.5f);
        if (fadeSeconds <= 0f) {
            return 1f;
        }
        float elapsed = topNotificationDuration - topNotificationTimer;
        float fadeIn = clamp01(elapsed / fadeSeconds);
        float fadeOut = clamp01(topNotificationTimer / fadeSeconds);
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

    private void drawMenuCenteredText(SpriteBatch batch, BitmapFont font, String text, float centerX, float baselineY,
                                      float r, float g, float b, float a) {
        if (font == null || uiLayout == null || text == null) {
            return;
        }
        uiLayout.setText(font, text);
        float textX = centerX - uiLayout.width * 0.5f;
        drawMenuText(batch, font, text, textX, baselineY, r, g, b, a);
    }

    private void drawMenuCenteredTextScaled(SpriteBatch batch, BitmapFont font, String text, float centerX, float baselineY,
                                            float r, float g, float b, float a, float scaleMultiplier) {
        if (font == null || uiLayout == null || text == null) {
            return;
        }
        float baseScaleX = font.getData().scaleX;
        float baseScaleY = font.getData().scaleY;
        font.getData().setScale(baseScaleX * scaleMultiplier, baseScaleY * scaleMultiplier);
        uiLayout.setText(font, text);
        float textX = centerX - uiLayout.width * 0.5f;
        drawMenuText(batch, font, text, textX, baselineY, r, g, b, a);
        font.getData().setScale(baseScaleX, baseScaleY);
    }

    private void drawMenuText(SpriteBatch batch, BitmapFont font, String text, float x, float y,
                              float r, float g, float b, float a) {
        if (font == null || text == null) {
            return;
        }
        font.setColor(0f, 0f, 0f, 0.85f * a);
        font.draw(batch, text, x + 1f, y - 1f);
        font.setColor(r, g, b, a);
        font.draw(batch, text, x, y);
        font.setColor(1f, 1f, 1f, 1f);
    }

    private void drawMenuTextScaled(SpriteBatch batch, BitmapFont font, String text, float x, float y,
                                    float r, float g, float b, float a, float scaleMultiplier) {
        if (font == null || text == null) {
            return;
        }
        float baseScaleX = font.getData().scaleX;
        float baseScaleY = font.getData().scaleY;
        font.getData().setScale(baseScaleX * scaleMultiplier, baseScaleY * scaleMultiplier);
        drawMenuText(batch, font, text, x, y, r, g, b, a);
        font.getData().setScale(baseScaleX, baseScaleY);
    }

    private String fitText(BitmapFont font, String text, float maxWidth) {
        if (text == null || text.isEmpty() || maxWidth <= 0f || uiLayout == null) {
            return "";
        }
        uiLayout.setText(font, text);
        if (uiLayout.width <= maxWidth) {
            return text;
        }
        String ellipsis = "...";
        for (int len = text.length() - 1; len > 0; len--) {
            String candidate = text.substring(0, len) + ellipsis;
            uiLayout.setText(font, candidate);
            if (uiLayout.width <= maxWidth) {
                return candidate;
            }
        }
        return ellipsis;
    }

    private String summarizeNotificationMessage(BitmapFont font, String text, float maxWidth) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String normalized = text.replace('\n', ' ').replace('\r', ' ').trim();
        while (normalized.contains("  ")) {
            normalized = normalized.replace("  ", " ");
        }
        String[] prefixes = {
                "Objective Updated:",
                "Quest Updated:",
                "Quest Started:",
                "New Objective:",
                "Current Objective:"
        };
        for (String prefix : prefixes) {
            if (normalized.regionMatches(true, 0, prefix, 0, prefix.length())) {
                normalized = normalized.substring(prefix.length()).trim();
                break;
            }
        }
        String compact = normalized
                .replace("Use the interact key/button to ", "Interact to ")
                .replace("Bring the ", "Bring ")
                .replace("Deliver the ", "Deliver ")
                .replace("Go to the ", "Go to ")
                .replace("Return to the ", "Return to ")
                .replace("Speak with the ", "Speak with ")
                .replace("Defeat the ", "Defeat ")
                .replace("Investigate the ", "Investigate ")
                .replace("Activate the ", "Activate ")
                .replace("outside the cafe", "outside cafe")
                .replace("the cafe owner", "cafe owner")
                .replace("Research Lab Floor ", "Lab Floor ")
                .replace("Research Lab", "Lab")
                .replace("Herb-and-Spice", "Herb & Spice");
        String articleLight = compact
                .replace(" the ", " ")
                .replace(" a ", " ")
                .replace(" an ", " ");

        if (fitsText(font, normalized, maxWidth)) {
            return normalized;
        }
        if (fitsText(font, compact, maxWidth)) {
            return compact;
        }
        if (fitsText(font, articleLight, maxWidth)) {
            return articleLight;
        }
        return fitTextWholeWords(font, articleLight, maxWidth);
    }

    private boolean sameNotification(String title, String message) {
        if (title == null || message == null) {
            return false;
        }
        return title.equals(topNotificationTitle) && message.equals(topNotificationMessage);
    }

    private boolean fitsText(BitmapFont font, String text, float maxWidth) {
        if (font == null || text == null || text.isBlank() || maxWidth <= 0f || uiLayout == null) {
            return false;
        }
        uiLayout.setText(font, text);
        return uiLayout.width <= maxWidth;
    }

    private String fitTextWholeWords(BitmapFont font, String text, float maxWidth) {
        if (text == null || text.isBlank() || font == null || maxWidth <= 0f || uiLayout == null) {
            return "";
        }
        String[] words = text.trim().split("\\s+");
        if (words.length == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            String candidate = builder.length() == 0 ? word : builder + " " + word;
            uiLayout.setText(font, candidate);
            if (uiLayout.width > maxWidth) {
                break;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(word);
        }
        if (builder.length() > 0) {
            return builder.toString();
        }
        return words[0];
    }

    private int wrapIndex(int index, int size) {
        if (size <= 0) {
            return 0;
        }
        return (index % size + size) % size;
    }

    private Texture loadRequiredTexture(String path) {
        FileHandle handle = resolveAssetHandle(path);
        if (!handle.exists()) {
            throw new IllegalStateException("Missing required HUD texture: " + path);
        }
        return new Texture(handle);
    }

    private Texture loadOptionalTexture(String path) {
        FileHandle handle = resolveAssetHandle(path);
        if (!handle.exists()) {
            Gdx.app.error("HudRenderer", "Missing menu texture: " + path);
            return null;
        }
        return new Texture(handle);
    }

    private void setNearestFilter(Texture texture) {
        if (texture != null) {
            texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        }
    }

    private BitmapFont loadUiFont(String fontPath, int size) {
        FileHandle handle = resolveAssetHandle(fontPath);
        if (!handle.exists()) {
            Gdx.app.error("HudRenderer", "Missing font: " + fontPath + ". Using default BitmapFont.");
            return new BitmapFont();
        }

        FreeTypeFontGenerator generator = null;
        try {
            generator = new FreeTypeFontGenerator(handle);
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

    private FileHandle resolveAssetHandle(String path) {
        FileHandle internalHandle = Gdx.files.internal(path);
        if (internalHandle.exists()) {
            return internalHandle;
        }
        File fallbackFile = new File(PROJECT_ASSETS_DIRECTORY, path.replace("/", File.separator));
        if (fallbackFile.exists()) {
            return Gdx.files.absolute(fallbackFile.getAbsolutePath());
        }
        return internalHandle;
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

    private boolean isSliderSetting(int index) {
        return index == 0 || index == 1;
    }

    private int settingsVolumePercent(int index) {
        if (settingsService == null) {
            return 0;
        }
        return switch (index) {
            case 0 -> settingsService.getMusicVolumePercent();
            case 1 -> settingsService.getSfxVolumePercent();
            default -> 0;
        };
    }

    private void updateDraggedSettingsSlider(float worldX) {
        if (!isSliderSetting(activeSettingsSliderDragIndex) || settingsService == null) {
            return;
        }
        Rectangle trackBounds = settingsSliderTrackBounds[activeSettingsSliderDragIndex];
        if (trackBounds == null || trackBounds.width <= 0f) {
            return;
        }
        float progress = clamp01((worldX - trackBounds.x) / trackBounds.width);
        int percent = Math.round(progress * 100f);
        if (activeSettingsSliderDragIndex == 0) {
            settingsService.setMusicVolumePercent(percent);
        } else if (activeSettingsSliderDragIndex == 1) {
            settingsService.setSfxVolumePercent(percent);
        }
    }

    public enum PauseMenuAction {
        NONE,
        RESUME,
        MAIN_MENU,
        RELOAD_AUTOSAVE,
        EXIT_GAME
    }

    private enum PauseMenuTab {
        GAME,
        SETTINGS
    }
}
