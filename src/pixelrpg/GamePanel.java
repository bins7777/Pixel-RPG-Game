package pixelrpg;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Very small Swing-based game panel that runs the loop,
 * handles input and renders the Tiled map.
 */
public class GamePanel extends JPanel implements KeyListener, Runnable {
    private static final int VIEW_WIDTH = 960;
    private static final int VIEW_HEIGHT = 640;
    private static final float PLAYER_SPEED = 115f; // pixels per second
    private static final float PLAYER_ACCEL = 520f; // pixels per second^2
    private static final float PLAYER_DECEL = 640f; // pixels per second^2
    private static final float PLAYER_SIZE = 14f * 1.25f;

    private final TiledMap map;
    private float playerX;
    private float playerY;
    private float playerVelX;
    private float playerVelY;

    private final Set<Integer> pressedKeys = new HashSet<>();
    private volatile boolean running;
    private Thread loopThread;

    // Turn on if you want to see collision rectangles.
    private final boolean debugWalls = false;

    public GamePanel(Path mapPath) throws Exception {
        this.map = new TiledMap(mapPath);
        if (map.getSpawnPoint() != null) {
            playerX = (float) map.getSpawnPoint().getX();
            playerY = (float) map.getSpawnPoint().getY();
        } else {
            Rectangle2D bounds = map.getPixelBounds();
            playerX = (float) (bounds.getX() + bounds.getWidth() / 2f);
            playerY = (float) (bounds.getY() + bounds.getHeight() / 2f);
        }

        setPreferredSize(new Dimension(VIEW_WIDTH, VIEW_HEIGHT));
        setFocusable(true);
        setBackground(new Color(12, 12, 16));
        addKeyListener(this);
        setDoubleBuffered(true);
    }

    public void start() {
        if (running) {
            return;
        }
        running = true;
        loopThread = new Thread(this, "pixel-rpg-loop");
        loopThread.start();
    }

    @Override
    public void run() {
        long last = System.nanoTime();
        while (running) {
            long now = System.nanoTime();
            float deltaSeconds = (now - last) / 1_000_000_000f;
            last = now;

            update(deltaSeconds);
            repaint();

            try {
                Thread.sleep(2);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
                running = false;
            }
        }
    }

    private void update(float deltaSeconds) {
        float inputX = 0;
        float inputY = 0;
        if (isPressed(KeyEvent.VK_W) || isPressed(KeyEvent.VK_UP)) {
            inputY -= 1;
        }
        if (isPressed(KeyEvent.VK_S) || isPressed(KeyEvent.VK_DOWN)) {
            inputY += 1;
        }
        if (isPressed(KeyEvent.VK_A) || isPressed(KeyEvent.VK_LEFT)) {
            inputX -= 1;
        }
        if (isPressed(KeyEvent.VK_D) || isPressed(KeyEvent.VK_RIGHT)) {
            inputX += 1;
        }

        if (inputX != 0 && inputY != 0) {
            float inv = (float) (1 / Math.sqrt(2));
            inputX *= inv;
            inputY *= inv;
        }

        float targetVelX = inputX * PLAYER_SPEED;
        float targetVelY = inputY * PLAYER_SPEED;
        float accelX = targetVelX == 0f ? PLAYER_DECEL : PLAYER_ACCEL;
        float accelY = targetVelY == 0f ? PLAYER_DECEL : PLAYER_ACCEL;
        playerVelX = approach(playerVelX, targetVelX, accelX * deltaSeconds);
        playerVelY = approach(playerVelY, targetVelY, accelY * deltaSeconds);

        float nextX = playerX + playerVelX * deltaSeconds;
        float nextY = playerY + playerVelY * deltaSeconds;
        float halfSize = PLAYER_SIZE / 2f;

        // Move on X axis then Y axis to keep collision simple.
        Rectangle2D.Float hitboxX = new Rectangle2D.Float(nextX - halfSize, playerY - halfSize, PLAYER_SIZE, PLAYER_SIZE);
        if (!collides(hitboxX)) {
            playerX = nextX;
        } else {
            playerVelX = 0f;
        }

        Rectangle2D.Float hitboxY = new Rectangle2D.Float(playerX - halfSize, nextY - halfSize, PLAYER_SIZE, PLAYER_SIZE);
        if (!collides(hitboxY)) {
            playerY = nextY;
        } else {
            playerVelY = 0f;
        }

        clampToMap();
    }

    private void clampToMap() {
        Rectangle2D bounds = map.getPixelBounds();
        float halfSize = PLAYER_SIZE / 2f;
        float minX = (float) bounds.getX() + halfSize;
        float minY = (float) bounds.getY() + halfSize;
        float maxX = (float) (bounds.getX() + bounds.getWidth() - halfSize);
        float maxY = (float) (bounds.getY() + bounds.getHeight() - halfSize);
        playerX = Math.max(minX, Math.min(playerX, maxX));
        playerY = Math.max(minY, Math.min(playerY, maxY));
    }

    private boolean collides(Rectangle2D.Float rect) {
        for (Rectangle2D.Float wall : map.getWalls()) {
            if (rect.intersects(wall)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

        Point camera = calculateCamera();
        drawMap(g2, camera);
        drawPlayer(g2, camera);
        if (debugWalls) {
            drawWalls(g2, camera);
        }
    }

    private Point calculateCamera() {
        Rectangle2D bounds = map.getPixelBounds();
        int minX = (int) Math.floor(bounds.getX());
        int minY = (int) Math.floor(bounds.getY());
        int maxX = (int) Math.ceil(bounds.getX() + bounds.getWidth());
        int maxY = (int) Math.ceil(bounds.getY() + bounds.getHeight());

        int viewWidth = getViewWidth();
        int viewHeight = getViewHeight();
        int desiredX = (int) (playerX - viewWidth / 2f);
        int desiredY = (int) (playerY - viewHeight / 2f);

        int clampedX = clamp(desiredX, minX, Math.max(minX, maxX - viewWidth));
        int clampedY = clamp(desiredY, minY, Math.max(minY, maxY - viewHeight));
        return new Point(clampedX, clampedY);
    }

    private void drawMap(Graphics2D g2, Point camera) {
        int viewWidth = getViewWidth();
        int viewHeight = getViewHeight();
        int offsetTilesX = map.getMinTileX();
        int offsetTilesY = map.getMinTileY();
        int tileW = map.getTileWidth();
        int tileH = map.getTileHeight();

        for (TiledMap.TileLayer layer : map.getLayers()) {
            for (int y = 0; y < layer.getHeight(); y++) {
                int worldY = (y + offsetTilesY) * tileH;
                int screenY = worldY - camera.y;
                if (screenY > viewHeight || screenY + tileH < 0) {
                    continue;
                }

                for (int x = 0; x < layer.getWidth(); x++) {
                    int gid = layer.getTileAt(x, y);
                    if (gid == 0) {
                        continue;
                    }

                    int worldX = (x + offsetTilesX) * tileW;
                    int screenX = worldX - camera.x;
                    if (screenX > viewWidth || screenX + tileW < 0) {
                        continue;
                    }

                    var img = map.getTileImage(gid);
                    if (img == null) {
                        continue;
                    }
                    g2.drawImage(img, screenX, screenY, null);
                }
            }
        }
    }

    private int getViewWidth() {
        int width = getWidth();
        return width > 0 ? width : VIEW_WIDTH;
    }

    private int getViewHeight() {
        int height = getHeight();
        return height > 0 ? height : VIEW_HEIGHT;
    }

    private void drawPlayer(Graphics2D g2, Point camera) {
        float halfSize = PLAYER_SIZE / 2f;
        int drawSize = Math.round(PLAYER_SIZE);
        int sx = (int) (playerX - halfSize - camera.x);
        int sy = (int) (playerY - halfSize - camera.y);

        g2.setColor(new Color(52, 136, 235));
        g2.fillRect(sx, sy, drawSize, drawSize);
        g2.setColor(new Color(18, 52, 122));
        g2.drawRect(sx, sy, drawSize, drawSize);
    }

    private void drawWalls(Graphics2D g2, Point camera) {
        g2.setColor(new Color(255, 64, 64, 90));
        for (Rectangle2D.Float wall : map.getWalls()) {
            int sx = (int) (wall.x - camera.x);
            int sy = (int) (wall.y - camera.y);
            g2.fillRect(sx, sy, (int) wall.width, (int) wall.height);
        }
    }

    private boolean isPressed(int keyCode) {
        return pressedKeys.contains(keyCode);
    }

    private static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    private static float approach(float current, float target, float maxDelta) {
        float delta = target - current;
        if (Math.abs(delta) <= maxDelta) {
            return target;
        }
        return current + Math.signum(delta) * maxDelta;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used.
    }

    @Override
    public void keyPressed(KeyEvent e) {
        pressedKeys.add(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        pressedKeys.remove(e.getKeyCode());
    }
}
