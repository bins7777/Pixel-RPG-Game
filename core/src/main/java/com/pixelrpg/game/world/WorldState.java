package com.pixelrpg.game.world;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.pixelrpg.game.config.MapCatalog;
import com.pixelrpg.game.config.MapDefinition;

/**
 * Mutable world/map state for the active gameplay screen.
 */
public class WorldState {
    public TiledMap map;
    public OrthogonalTiledMapRenderer mapRenderer;

    public final Array<Rectangle> walls = new Array<>();
    public final Array<Polygon> wallPolygons = new Array<>();
    public final Array<Rectangle> lightAreaRects = new Array<>();
    public final Array<Ellipse> lightAreaEllipses = new Array<>();
    public final Array<float[]> lightAreaPolygons = new Array<>();
    public final Array<MapLayer> torchAnimationFrames = new Array<>();
    public final Array<MapLayer> npcAnimationFrames = new Array<>();

    public Rectangle mapBounds = new Rectangle(-2000, -2000, 4000, 4000);
    public MapCatalog mapCatalog;
    public MapDefinition activeMap;
    public String activeMapId;
    public int[] backgroundLayerIndices = new int[0];
    public int[] foregroundLayerIndices = new int[0];
    public int torchAnimationFrameIndex = 0;
    public float torchAnimationTimer = 0f;
    public int npcAnimationFrameIndex = 0;
    public float npcAnimationTimer = 0f;
}
