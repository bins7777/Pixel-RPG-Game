package com.pixelrpg.game.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable map definition loaded from asset config.
 */
public final class MapDefinition {
    private final String id;
    private final String mapPath;
    private final List<String> collisionLayers;
    private final List<String> spawnLayers;
    private final float mapPadding;

    public MapDefinition(
            String id,
            String mapPath,
            List<String> collisionLayers,
            List<String> spawnLayers,
            float mapPadding
    ) {
        this.id = id;
        this.mapPath = mapPath;
        this.collisionLayers = Collections.unmodifiableList(new ArrayList<>(collisionLayers));
        this.spawnLayers = Collections.unmodifiableList(new ArrayList<>(spawnLayers));
        this.mapPadding = mapPadding;
    }

    public String getId() {
        return id;
    }

    public String getMapPath() {
        return mapPath;
    }

    public List<String> getCollisionLayers() {
        return collisionLayers;
    }

    public List<String> getSpawnLayers() {
        return spawnLayers;
    }

    public float getMapPadding() {
        return mapPadding;
    }
}
