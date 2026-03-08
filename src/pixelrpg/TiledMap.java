package pixelrpg;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal TMX/TSX loader that supports the structures used by the provided map.
 */
public class TiledMap {
    private final List<Tileset> tilesets;
    private final List<TileLayer> layers;
    private final List<Rectangle2D.Float> walls;
    private final Rectangle2D.Float pixelBounds;
    private final Point2D.Float spawnPoint;
    private final Map<Integer, BufferedImage> tileCache = new HashMap<>();

    private final int minTileX;
    private final int minTileY;
    private final int maxTileX;
    private final int maxTileY;

    private final int tileWidth;
    private final int tileHeight;

    public TiledMap(Path mapPath) throws Exception {
        if (!Files.exists(mapPath)) {
            throw new IllegalArgumentException("Map not found: " + mapPath);
        }

        Document mapDoc = parseXml(mapPath);
        Element mapElement = mapDoc.getDocumentElement();
        this.tileWidth = Integer.parseInt(mapElement.getAttribute("tilewidth"));
        this.tileHeight = Integer.parseInt(mapElement.getAttribute("tileheight"));

        Path baseDir = mapPath.toAbsolutePath().getParent();
        this.tilesets = loadTilesets(mapElement, baseDir);

        Bounds bounds = computeBounds(mapElement);
        this.minTileX = bounds.minX;
        this.minTileY = bounds.minY;
        this.maxTileX = bounds.maxX;
        this.maxTileY = bounds.maxY;

        this.layers = loadLayers(mapElement, bounds);
        this.walls = loadWalls(mapElement);
        this.spawnPoint = loadSpawnPoint(mapElement);
        this.pixelBounds = new Rectangle2D.Float(
                bounds.minX * tileWidth,
                bounds.minY * tileHeight,
                bounds.getWidth() * tileWidth,
                bounds.getHeight() * tileHeight
        );
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public int getMinTileX() {
        return minTileX;
    }

    public int getMinTileY() {
        return minTileY;
    }

    public List<TileLayer> getLayers() {
        return Collections.unmodifiableList(layers);
    }

    public List<Rectangle2D.Float> getWalls() {
        return Collections.unmodifiableList(walls);
    }

    public Point2D.Float getSpawnPoint() {
        return spawnPoint;
    }

    public Rectangle2D.Float getPixelBounds() {
        return pixelBounds;
    }

    public BufferedImage getTileImage(int gid) {
        if (gid <= 0) {
            return null;
        }
        return tileCache.computeIfAbsent(gid, this::sliceTile);
    }

    private BufferedImage sliceTile(int gid) {
        Tileset ts = findTileset(gid);
        if (ts == null) {
            return null;
        }
        int localId = gid - ts.firstGid;
        if (localId < 0 || localId >= ts.tileCount) {
            return null;
        }

        int col = localId % ts.columns;
        int row = localId / ts.columns;
        int x = ts.margin + col * (ts.tileWidth + ts.spacing);
        int y = ts.margin + row * (ts.tileHeight + ts.spacing);

        if (x + ts.tileWidth > ts.image.getWidth() || y + ts.tileHeight > ts.image.getHeight()) {
            return null;
        }
        return ts.image.getSubimage(x, y, ts.tileWidth, ts.tileHeight);
    }

    private Tileset findTileset(int gid) {
        Tileset chosen = null;
        for (Tileset ts : tilesets) {
            if (gid >= ts.firstGid) {
                chosen = ts;
            } else {
                break;
            }
        }
        return chosen;
    }

    private List<Tileset> loadTilesets(Element mapElement, Path baseDir) throws Exception {
        NodeList tilesetNodes = mapElement.getElementsByTagName("tileset");
        List<Tileset> list = new ArrayList<>();
        for (int i = 0; i < tilesetNodes.getLength(); i++) {
            Element ref = (Element) tilesetNodes.item(i);
            String source = ref.getAttribute("source");
            int firstGid = Integer.parseInt(ref.getAttribute("firstgid"));

            Path tsxPath = baseDir.resolve(source).normalize();
            Document tsxDoc = parseXml(tsxPath);
            Element tsx = tsxDoc.getDocumentElement();

            Tileset ts = new Tileset(firstGid);
            ts.name = tsx.getAttribute("name");
            ts.tileWidth = Integer.parseInt(tsx.getAttribute("tilewidth"));
            ts.tileHeight = Integer.parseInt(tsx.getAttribute("tileheight"));
            ts.tileCount = Integer.parseInt(tsx.getAttribute("tilecount"));
            ts.columns = Integer.parseInt(tsx.getAttribute("columns"));
            ts.margin = parseAttributeOrDefault(tsx, "margin", 0);
            ts.spacing = parseAttributeOrDefault(tsx, "spacing", 0);

            Element img = (Element) tsx.getElementsByTagName("image").item(0);
            String imgSource = img.getAttribute("source");
            Path imagePath = tsxPath.getParent().resolve(imgSource).normalize();
            ts.image = ImageIO.read(imagePath.toFile());

            list.add(ts);
        }
        list.sort(Comparator.comparingInt(t -> t.firstGid));
        return list;
    }

    private Bounds computeBounds(Element mapElement) {
        NodeList layerNodes = mapElement.getElementsByTagName("layer");
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        boolean found = false;

        for (int i = 0; i < layerNodes.getLength(); i++) {
            Element layer = (Element) layerNodes.item(i);
            NodeList chunkNodes = layer.getElementsByTagName("chunk");
            if (chunkNodes.getLength() == 0) {
                int w = Integer.parseInt(layer.getAttribute("width"));
                int h = Integer.parseInt(layer.getAttribute("height"));
                minX = Math.min(minX, 0);
                minY = Math.min(minY, 0);
                maxX = Math.max(maxX, w);
                maxY = Math.max(maxY, h);
                found = true;
                continue;
            }

            for (int c = 0; c < chunkNodes.getLength(); c++) {
                Element chunk = (Element) chunkNodes.item(c);
                int cx = Integer.parseInt(chunk.getAttribute("x"));
                int cy = Integer.parseInt(chunk.getAttribute("y"));
                int cw = Integer.parseInt(chunk.getAttribute("width"));
                int ch = Integer.parseInt(chunk.getAttribute("height"));
                minX = Math.min(minX, cx);
                minY = Math.min(minY, cy);
                maxX = Math.max(maxX, cx + cw);
                maxY = Math.max(maxY, cy + ch);
                found = true;
            }
        }

        if (!found) {
            throw new IllegalStateException("No tile data found in map.");
        }

        return new Bounds(minX, minY, maxX, maxY);
    }

    private List<TileLayer> loadLayers(Element mapElement, Bounds bounds) {
        NodeList layerNodes = mapElement.getElementsByTagName("layer");
        List<TileLayer> list = new ArrayList<>();
        for (int i = 0; i < layerNodes.getLength(); i++) {
            Element layerElement = (Element) layerNodes.item(i);
            TileLayer layer = new TileLayer(layerElement.getAttribute("name"), bounds.getWidth(), bounds.getHeight());

            NodeList chunkNodes = layerElement.getElementsByTagName("chunk");
            if (chunkNodes.getLength() > 0) {
                for (int c = 0; c < chunkNodes.getLength(); c++) {
                    Element chunk = (Element) chunkNodes.item(c);
                    fillChunk(layer, chunk, bounds);
                }
            } else {
                // Non-infinite map support.
                Element data = (Element) layerElement.getElementsByTagName("data").item(0);
                int layerWidth = Integer.parseInt(layerElement.getAttribute("width"));
                int layerHeight = Integer.parseInt(layerElement.getAttribute("height"));
                String raw = data.getTextContent().trim();
                if (!raw.isEmpty()) {
                    String[] tokens = raw.split("[\\s,]+");
                    int expected = layerWidth * layerHeight;
                    if (tokens.length < expected) {
                        throw new IllegalStateException("Layer " + layer.getName() + " has fewer tiles than expected.");
                    }
                    int idx = 0;
                    for (int y = 0; y < layerHeight; y++) {
                        for (int x = 0; x < layerWidth; x++) {
                            int gid = parseGid(tokens[idx++]);
                            layer.setTile(x - bounds.minX, y - bounds.minY, gid);
                        }
                    }
                }
            }

            list.add(layer);
        }
        return list;
    }

    private void fillChunk(TileLayer layer, Element chunk, Bounds bounds) {
        int chunkX = Integer.parseInt(chunk.getAttribute("x"));
        int chunkY = Integer.parseInt(chunk.getAttribute("y"));
        int chunkW = Integer.parseInt(chunk.getAttribute("width"));
        int chunkH = Integer.parseInt(chunk.getAttribute("height"));

        String raw = chunk.getTextContent().trim();
        if (raw.isEmpty()) {
            return;
        }
        String[] tokens = raw.split("[\\s,]+");
        int expected = chunkW * chunkH;
        if (tokens.length < expected) {
            throw new IllegalStateException("Chunk has fewer tiles than expected.");
        }

        int idx = 0;
        for (int y = 0; y < chunkH; y++) {
            for (int x = 0; x < chunkW; x++) {
                int gid = parseGid(tokens[idx++]);
                int destX = chunkX + x - bounds.minX;
                int destY = chunkY + y - bounds.minY;
                layer.setTile(destX, destY, gid);
            }
        }
    }

    private List<Rectangle2D.Float> loadWalls(Element mapElement) {
        List<Rectangle2D.Float> list = new ArrayList<>();
        NodeList groups = mapElement.getElementsByTagName("objectgroup");
        for (int i = 0; i < groups.getLength(); i++) {
            Element group = (Element) groups.item(i);
            if (!"WALLS".equalsIgnoreCase(group.getAttribute("name"))) {
                continue;
            }
            NodeList objects = group.getElementsByTagName("object");
            for (int o = 0; o < objects.getLength(); o++) {
                Element obj = (Element) objects.item(o);
                float x = Float.parseFloat(obj.getAttribute("x"));
                float y = Float.parseFloat(obj.getAttribute("y"));
                float w = obj.hasAttribute("width") ? Float.parseFloat(obj.getAttribute("width")) : 0f;
                float h = obj.hasAttribute("height") ? Float.parseFloat(obj.getAttribute("height")) : 0f;
                list.add(new Rectangle2D.Float(x, y, w, h));
            }
        }
        return list;
    }

    private Point2D.Float loadSpawnPoint(Element mapElement) {
        NodeList groups = mapElement.getElementsByTagName("objectgroup");
        for (int i = 0; i < groups.getLength(); i++) {
            Element group = (Element) groups.item(i);
            if (!"SpawnPoint".equalsIgnoreCase(group.getAttribute("name"))) {
                continue;
            }
            NodeList objects = group.getElementsByTagName("object");
            if (objects.getLength() == 0) {
                continue;
            }
            Element obj = (Element) objects.item(0);
            float x = Float.parseFloat(obj.getAttribute("x"));
            float y = Float.parseFloat(obj.getAttribute("y"));
            float w = obj.hasAttribute("width") ? Float.parseFloat(obj.getAttribute("width")) : 0f;
            float h = obj.hasAttribute("height") ? Float.parseFloat(obj.getAttribute("height")) : 0f;
            return new Point2D.Float(x + w / 2f, y + h / 2f);
        }
        return null;
    }

    private Document parseXml(Path path) throws Exception {
        var factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        var builder = factory.newDocumentBuilder();
        return builder.parse(path.toFile());
    }

    private int parseAttributeOrDefault(Element element, String name, int defaultValue) {
        if (element.hasAttribute(name)) {
            return Integer.parseInt(element.getAttribute(name));
        }
        return defaultValue;
    }

    private int parseGid(String token) {
        long raw = Long.parseUnsignedLong(token);
        return (int) (raw & 0x1FFFFFFFL);
    }

    private static final class Tileset {
        private final int firstGid;
        private String name;
        private int tileWidth;
        private int tileHeight;
        private int columns;
        private int tileCount;
        private int margin;
        private int spacing;
        private BufferedImage image;

        private Tileset(int firstGid) {
            this.firstGid = firstGid;
        }
    }

    public static final class TileLayer {
        private final String name;
        private final int width;
        private final int height;
        private final int[][] tiles;

        private TileLayer(String name, int width, int height) {
            this.name = name;
            this.width = width;
            this.height = height;
            this.tiles = new int[height][width];
        }

        public String getName() {
            return name;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public int getTileAt(int x, int y) {
            return tiles[y][x];
        }

        private void setTile(int x, int y, int gid) {
            if (x < 0 || x >= width || y < 0 || y >= height) {
                return;
            }
            tiles[y][x] = gid;
        }
    }

    private static final class Bounds {
        private final int minX;
        private final int minY;
        private final int maxX;
        private final int maxY;

        private Bounds(int minX, int minY, int maxX, int maxY) {
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
        }

        private int getWidth() {
            return maxX - minX;
        }

        private int getHeight() {
            return maxY - minY;
        }
    }
}
