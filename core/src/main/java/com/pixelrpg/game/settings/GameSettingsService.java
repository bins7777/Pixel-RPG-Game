package com.pixelrpg.game.settings;

import java.io.Reader;
import java.util.Properties;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

public final class GameSettingsService {
    private static final String PREFERENCES_NAME = "pixelrpg-settings";
    private static final String KEY_MUSIC_VOLUME = "musicVolumePercent";
    private static final String KEY_SFX_VOLUME = "sfxVolumePercent";
    private static final String KEY_FULLSCREEN = "fullscreen";
    private static final String KEY_VSYNC = "vsync";
    private static final String DESKTOP_CONFIG_PATH = "config/desktop.properties";

    private int defaultWindowWidth = 1280;
    private int defaultWindowHeight = 720;
    private boolean defaultVsync = true;
    private GameSettings settings = new GameSettings(
            GameSettings.DEFAULT_VOLUME_PERCENT,
            GameSettings.DEFAULT_VOLUME_PERCENT,
            false,
            true);
    private boolean initialized = false;

    public void initialize() {
        if (initialized) {
            applyDisplaySettings();
            return;
        }

        loadDesktopConfigDefaults();
        Preferences preferences = preferences();
        boolean fullscreenDefault = Gdx.graphics != null && Gdx.graphics.isFullscreen();
        settings = new GameSettings(
                preferences.contains(KEY_MUSIC_VOLUME)
                        ? preferences.getInteger(KEY_MUSIC_VOLUME, GameSettings.DEFAULT_VOLUME_PERCENT)
                        : GameSettings.DEFAULT_VOLUME_PERCENT,
                preferences.contains(KEY_SFX_VOLUME)
                        ? preferences.getInteger(KEY_SFX_VOLUME, GameSettings.DEFAULT_VOLUME_PERCENT)
                        : GameSettings.DEFAULT_VOLUME_PERCENT,
                preferences.contains(KEY_FULLSCREEN)
                        ? preferences.getBoolean(KEY_FULLSCREEN, fullscreenDefault)
                        : fullscreenDefault,
                preferences.contains(KEY_VSYNC)
                        ? preferences.getBoolean(KEY_VSYNC, defaultVsync)
                        : defaultVsync);
        initialized = true;
        applyDisplaySettings();
    }

    public GameSettings getSettings() {
        return settings;
    }

    public int getMusicVolumePercent() {
        return settings.musicVolumePercent;
    }

    public int getSfxVolumePercent() {
        return settings.sfxVolumePercent;
    }

    public boolean isFullscreen() {
        return settings.fullscreen;
    }

    public boolean isVsyncEnabled() {
        return settings.vsync;
    }

    public void stepMusicVolume(int deltaPercent) {
        setMusicVolumePercent(settings.musicVolumePercent + deltaPercent);
    }

    public void stepSfxVolume(int deltaPercent) {
        setSfxVolumePercent(settings.sfxVolumePercent + deltaPercent);
    }

    public void setMusicVolumePercent(int value) {
        settings = settings.withMusicVolumePercent(value);
        save();
    }

    public void setSfxVolumePercent(int value) {
        settings = settings.withSfxVolumePercent(value);
        save();
    }

    public boolean toggleFullscreen() {
        boolean requested = !settings.fullscreen;
        if (!applyFullscreen(requested)) {
            return false;
        }
        settings = settings.withFullscreen(requested);
        save();
        return true;
    }

    public void toggleVsync() {
        settings = settings.withVsync(!settings.vsync);
        applyVsync();
        save();
    }

    public long playSound(Sound sound, float baseVolume) {
        if (sound == null) {
            return -1L;
        }
        return sound.play(scaleSfxVolume(baseVolume));
    }

    public void playMusicFromStart(Music music, float baseVolume, boolean looping) {
        if (music == null) {
            return;
        }
        music.stop();
        music.setLooping(looping);
        music.setPosition(0f);
        applyMusicVolume(music, baseVolume);
        music.play();
    }

    public void applyMusicVolume(Music music, float baseVolume) {
        if (music == null) {
            return;
        }
        music.setVolume(scaleMusicVolume(baseVolume));
    }

    public float scaleMusicVolume(float baseVolume) {
        return clampVolume(baseVolume) * settings.musicVolumeScale();
    }

    public float scaleSfxVolume(float baseVolume) {
        return clampVolume(baseVolume) * settings.sfxVolumeScale();
    }

    private void applyDisplaySettings() {
        applyVsync();
        applyFullscreen(settings.fullscreen);
    }

    private void applyVsync() {
        if (Gdx.graphics != null) {
            Gdx.graphics.setVSync(settings.vsync);
        }
    }

    private boolean applyFullscreen(boolean fullscreen) {
        if (Gdx.graphics == null) {
            return false;
        }
        Graphics graphics = Gdx.graphics;
        if (fullscreen) {
            if (!graphics.supportsDisplayModeChange()) {
                return false;
            }
            Graphics.DisplayMode displayMode = graphics.getDisplayMode();
            return displayMode != null && graphics.setFullscreenMode(displayMode);
        }
        return graphics.setWindowedMode(defaultWindowWidth, defaultWindowHeight);
    }

    private void save() {
        Preferences preferences = preferences();
        preferences.putInteger(KEY_MUSIC_VOLUME, settings.musicVolumePercent);
        preferences.putInteger(KEY_SFX_VOLUME, settings.sfxVolumePercent);
        preferences.putBoolean(KEY_FULLSCREEN, settings.fullscreen);
        preferences.putBoolean(KEY_VSYNC, settings.vsync);
        preferences.flush();
    }

    private Preferences preferences() {
        return Gdx.app.getPreferences(PREFERENCES_NAME);
    }

    private void loadDesktopConfigDefaults() {
        FileHandle file = Gdx.files.internal(DESKTOP_CONFIG_PATH);
        if (file == null || !file.exists()) {
            return;
        }

        Properties properties = new Properties();
        try (Reader reader = file.reader("UTF-8")) {
            properties.load(reader);
            defaultWindowWidth = parseInt(properties.getProperty("width"), defaultWindowWidth);
            defaultWindowHeight = parseInt(properties.getProperty("height"), defaultWindowHeight);
            defaultVsync = parseBoolean(properties.getProperty("vsync"), defaultVsync);
        } catch (Exception ignored) {
            // Keep fallbacks when config loading fails.
        }
    }

    private int parseInt(String value, int fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private boolean parseBoolean(String value, boolean fallback) {
        if (value == null) {
            return fallback;
        }
        String normalized = value.trim().toLowerCase();
        if ("true".equals(normalized)) {
            return true;
        }
        if ("false".equals(normalized)) {
            return false;
        }
        return fallback;
    }

    private float clampVolume(float value) {
        return Math.max(0f, Math.min(1f, value));
    }
}
