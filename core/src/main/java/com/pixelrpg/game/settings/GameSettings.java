package com.pixelrpg.game.settings;

public final class GameSettings {
    public static final int DEFAULT_VOLUME_PERCENT = 100;

    public final int musicVolumePercent;
    public final int sfxVolumePercent;
    public final boolean fullscreen;
    public final boolean vsync;

    public GameSettings(int musicVolumePercent, int sfxVolumePercent, boolean fullscreen, boolean vsync) {
        this.musicVolumePercent = clampVolumePercent(musicVolumePercent);
        this.sfxVolumePercent = clampVolumePercent(sfxVolumePercent);
        this.fullscreen = fullscreen;
        this.vsync = vsync;
    }

    public GameSettings withMusicVolumePercent(int value) {
        return new GameSettings(value, sfxVolumePercent, fullscreen, vsync);
    }

    public GameSettings withSfxVolumePercent(int value) {
        return new GameSettings(musicVolumePercent, value, fullscreen, vsync);
    }

    public GameSettings withFullscreen(boolean value) {
        return new GameSettings(musicVolumePercent, sfxVolumePercent, value, vsync);
    }

    public GameSettings withVsync(boolean value) {
        return new GameSettings(musicVolumePercent, sfxVolumePercent, fullscreen, value);
    }

    public float musicVolumeScale() {
        return musicVolumePercent / 100f;
    }

    public float sfxVolumeScale() {
        return sfxVolumePercent / 100f;
    }

    public static int clampVolumePercent(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
