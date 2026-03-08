package com.pixelrpg.game.desktop;

import java.io.InputStream;
import java.util.Properties;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.pixelrpg.game.PixelRpgGame;

public class DesktopLauncher {
    private static final String CONFIG_RESOURCE = "config/desktop.properties";

    public static void main(String[] args) {
        Properties props = loadProperties();

        String title = props.getProperty("title", "Pixel RPG (LibGDX)");
        boolean vsync = parseBoolean(props.getProperty("vsync"), true);
        int foregroundFps = parseInt(props.getProperty("foregroundFps"), 60);

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle(title);
        var displayMode = Lwjgl3ApplicationConfiguration.getDisplayMode();
        config.setWindowedMode(displayMode.width, displayMode.height);
        config.setDecorated(false); // borderless fullscreen window
        config.setResizable(false);
        config.setMaximized(true);
        config.useVsync(vsync);
        config.setForegroundFPS(foregroundFps);
        // Reserve stencil bits for LightAreas masking during night rendering.
        config.setBackBufferConfig(8, 8, 8, 8, 16, 8, 0);
        new Lwjgl3Application(new PixelRpgGame(), config);
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream in = DesktopLauncher.class.getClassLoader().getResourceAsStream(CONFIG_RESOURCE)) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception ignored) {
            // Keep defaults if config is missing or malformed.
        }
        return props;
    }

    private static int parseInt(String value, int fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static boolean parseBoolean(String value, boolean fallback) {
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
}
