package pixelrpg;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Small entry point that boots the swing window and starts the game loop.
 */
public class GameLauncher {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Path mapPath = resolveMapPath(args);
            GamePanel panel;
            try {
                panel = new GamePanel(mapPath);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(
                        null,
                        "Failed to load the map: " + e.getMessage(),
                        "Pixel RPG Starter",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            JFrame frame = new JFrame("Pixel RPG Starter");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            panel.start();
        });
    }

    private static Path resolveMapPath(String[] args) {
        List<Path> checkedPaths = new ArrayList<>();

        if (args != null && args.length > 0 && args[0] != null && !args[0].trim().isEmpty()) {
            Path cliPath = Paths.get(args[0].trim()).toAbsolutePath().normalize();
            checkedPaths.add(cliPath);
            if (Files.exists(cliPath)) {
                return cliPath;
            }
        }

        Path directMap = firstExisting(checkedPaths,
                Paths.get("DSA GAME", "village.tmx"),
                Paths.get("core", "assets", "maps", "village.tmx"),
                Paths.get("DSA GAME", "Easy.tmx"),
                Paths.get("Files Ready(delete after editing)", "Easy.tmx"),
                Paths.get("Easy.tmx"));
        if (directMap != null) {
            return directMap;
        }

        Path probeRoot = Paths.get("").toAbsolutePath().normalize();
        for (int i = 0; i < 6 && probeRoot != null; i++) {
            Path found = firstExisting(checkedPaths,
                    probeRoot.resolve(Paths.get("DSA GAME", "village.tmx")),
                    probeRoot.resolve(Paths.get("core", "assets", "maps", "village.tmx")),
                    probeRoot.resolve(Paths.get("DSA GAME", "Easy.tmx")),
                    probeRoot.resolve(Paths.get("Files Ready(delete after editing)", "Easy.tmx")),
                    probeRoot.resolve("Easy.tmx"));
            if (found != null) {
                return found;
            }
            probeRoot = probeRoot.getParent();
        }

        StringBuilder message = new StringBuilder("Map not found. Checked:");
        for (Path checkedPath : checkedPaths) {
            message.append(System.lineSeparator()).append(" - ").append(checkedPath);
        }
        throw new IllegalArgumentException(message.toString());
    }

    private static Path firstExisting(List<Path> checkedPaths, Path... candidates) {
        for (Path candidate : candidates) {
            Path normalized = candidate.toAbsolutePath().normalize();
            checkedPaths.add(normalized);
            if (Files.exists(normalized)) {
                return normalized;
            }
        }
        return null;
    }
}
