package com.pixelrpg.game.desktop;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.pixelrpg.game.PixelRpgGame;

public class DesktopLauncher {
    private static final String CONFIG_RESOURCE = "config/desktop.properties";

    public static void main(String[] args) {
        Properties props = loadProperties();

        String title = props.getProperty("title", "Pixel RPG (LibGDX)");
        int width = parseInt(props.getProperty("width"), 1280);
        int height = parseInt(props.getProperty("height"), 720);
        boolean resizable = parseBoolean(props.getProperty("resizable"), true);
        boolean vsync = parseBoolean(props.getProperty("vsync"), true);
        int foregroundFps = parseInt(props.getProperty("foregroundFps"), 60);
        String introVideoPath = resolveIntroVideoPath(props.getProperty("introVideoPath", "").trim());
        String endingVideoPath = resolveIntroVideoPath(props.getProperty("endingVideoPath", "").trim());

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle(title);
        config.setWindowedMode(width, height);
        config.setDecorated(true);
        config.setResizable(resizable);
        config.useVsync(vsync);
        config.setForegroundFPS(foregroundFps);
        // Reserve stencil bits for LightAreas masking during night rendering.
        config.setBackBufferConfig(8, 8, 8, 8, 16, 8, 0);
        JavaFxIntroPlaybackService introPlaybackService =
                new JavaFxIntroPlaybackService(introVideoPath, title, width, height);
        JavaFxIntroPlaybackService endingPlaybackService =
                new JavaFxIntroPlaybackService(endingVideoPath, title, width, height);
        new Lwjgl3Application(new PixelRpgGame(introPlaybackService, endingPlaybackService), config);
    }

    private static String resolveIntroVideoPath(String configuredPath) {
        if (configuredPath == null) {
            return "";
        }
        String normalizedPath = configuredPath.trim();
        if (normalizedPath.isBlank()) {
            return "";
        }

        File configuredFile = new File(normalizedPath);
        if (configuredFile.isFile()) {
            return configuredFile.getAbsolutePath();
        }

        URL resource = DesktopLauncher.class.getClassLoader().getResource(normalizedPath);
        if (resource == null) {
            return normalizedPath;
        }

        try {
            if ("file".equalsIgnoreCase(resource.getProtocol())) {
                return Path.of(resource.toURI()).toFile().getAbsolutePath();
            }

            String fileName = Path.of(normalizedPath).getFileName().toString();
            String suffix = "";
            int extensionIndex = fileName.lastIndexOf('.');
            if (extensionIndex >= 0) {
                suffix = fileName.substring(extensionIndex);
            }
            Path tempFile = Files.createTempFile("pixelrpg-intro-", suffix);
            tempFile.toFile().deleteOnExit();
            try (InputStream in = resource.openStream()) {
                Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }
            return tempFile.toAbsolutePath().toString();
        } catch (Exception ignored) {
            return normalizedPath;
        }
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
