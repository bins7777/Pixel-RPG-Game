package com.pixelrpg.game.desktop;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.Paths;
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
        String configuredIntroVideoPath = props.getProperty("introVideoPath", "");
        String introVideoPath = resolveVideoPath(configuredIntroVideoPath);
        logVideoPath("Intro", configuredIntroVideoPath, introVideoPath);
        String configuredEndingVideoPath = props.getProperty("endingVideoPath", "");
        String endingVideoPath = resolveVideoPath(configuredEndingVideoPath);
        logVideoPath("Ending", configuredEndingVideoPath, endingVideoPath);

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

    private static String resolveVideoPath(String configuredPath) {
        if (configuredPath == null) {
            return "";
        }

        String expanded = expandVideoPath(configuredPath.trim());
        if (expanded.isBlank()) {
            return "";
        }

        File configuredFile = new File(expanded);
        if (configuredFile.isFile()) {
            return configuredFile.getAbsolutePath();
        }

        Path configured = Paths.get(expanded).normalize();
        Path userDir = Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        Path resolved = resolveRelativePath(userDir, configured);
        if (resolved != null) {
            return resolved.toString();
        }

        String bundled = resolveBundledResource(expanded);
        if (!bundled.isBlank()) {
            return bundled;
        }

        return configured.isAbsolute() ? configured.toString() : expanded;
    }

    private static Path resolveRelativePath(Path startDir, Path configuredPath) {
        Path currentDir = startDir;
        while (currentDir != null) {
            Path fromCurrentDir = currentDir.resolve(configuredPath).normalize();
            if (Files.isRegularFile(fromCurrentDir)) {
                return fromCurrentDir;
            }

            Path fromAssetsDir = currentDir.resolve("core").resolve("assets").resolve(configuredPath).normalize();
            if (Files.isRegularFile(fromAssetsDir)) {
                return fromAssetsDir;
            }

            currentDir = currentDir.getParent();
        }
        return null;
    }

    private static String resolveBundledResource(String resourcePath) {
        URL resource = DesktopLauncher.class.getClassLoader().getResource(resourcePath);
        if (resource == null) {
            return "";
        }

        try {
            if ("file".equalsIgnoreCase(resource.getProtocol())) {
                return Path.of(resource.toURI()).toFile().getAbsolutePath();
            }

            String fileName = Paths.get(resourcePath).getFileName().toString();
            String suffix = "";
            int extensionIndex = fileName.lastIndexOf('.');
            if (extensionIndex >= 0) {
                suffix = fileName.substring(extensionIndex);
            }
            Path tempFile = Files.createTempFile("pixelrpg-video-", suffix);
            tempFile.toFile().deleteOnExit();
            try (InputStream in = resource.openStream()) {
                Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }
            return tempFile.toAbsolutePath().toString();
        } catch (Exception ignored) {
            return "";
        }
    }

    private static String expandVideoPath(String rawPath) {
        if (rawPath == null || rawPath.isBlank()) {
            return "";
        }

        String result = rawPath;
        String userHome = System.getProperty("user.home", "");
        String userName = System.getProperty("user.name", "");
        String userProfile = System.getenv("USERPROFILE");

        if (!userHome.isEmpty()) {
            result = result.replace("${user.home}", userHome);
        }
        if (!userName.isEmpty()) {
            result = result.replace("${user.name}", userName);
            result = result.replace("/Users/user/", "/Users/" + userName + "/");
            result = result.replace("\\Users\\user\\", "\\Users\\" + userName + "\\");
        }
        if (userProfile != null && !userProfile.isBlank()) {
            result = result.replace("%USERPROFILE%", userProfile);
        }
        return result;
    }

    private static void logVideoPath(String label, String configuredPath, String resolvedPath) {
        String normalizedConfiguredPath = configuredPath == null ? "" : configuredPath.trim();
        if (normalizedConfiguredPath.isEmpty()) {
            System.out.println("[" + label + "] No video configured.");
            return;
        }

        boolean exists = false;
        try {
            exists = Files.isRegularFile(Paths.get(resolvedPath));
        } catch (Exception ignored) {
            exists = false;
        }

        System.out.println("[" + label + "] Configured video path: " + normalizedConfiguredPath);
        System.out.println("[" + label + "] Resolved video path: " + resolvedPath);
        System.out.println("[" + label + "] Resolved file exists: " + exists);
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
