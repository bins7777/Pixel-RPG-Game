package com.pixelrpg.game.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Loads map definitions from JSON and provides ordered access.
 */
public final class MapCatalog {
    private final List<MapDefinition> orderedMaps;
    private final Map<String, MapDefinition> mapsById;
    private final String defaultMapId;

    private MapCatalog(List<MapDefinition> orderedMaps, String defaultMapId) {
        this.orderedMaps = orderedMaps;
        this.defaultMapId = defaultMapId;
        this.mapsById = new LinkedHashMap<>();
        for (MapDefinition def : orderedMaps) {
            mapsById.put(def.getId(), def);
        }
    }

    public static MapCatalog load(String catalogPath, float defaultPadding) {
        FileHandle handle = Gdx.files.internal(catalogPath);
        if (!handle.exists()) {
            throw new GdxRuntimeException("Map catalog not found: " + catalogPath);
        }

        JsonValue root = new JsonReader().parse(handle);
        JsonValue mapsNode = root.get("maps");
        if (mapsNode == null || !mapsNode.isArray() || mapsNode.size == 0) {
            throw new GdxRuntimeException("Map catalog has no maps: " + catalogPath);
        }

        List<MapDefinition> ordered = new ArrayList<>();
        Map<String, Boolean> seen = new LinkedHashMap<>();
        for (JsonValue entry = mapsNode.child; entry != null; entry = entry.next) {
            String id = entry.getString("id", "").trim();
            String path = entry.getString("path", "").trim();
            if (id.isEmpty() || path.isEmpty()) {
                throw new GdxRuntimeException("Map entry requires non-empty id and path in " + catalogPath);
            }
            if (seen.containsKey(id)) {
                throw new GdxRuntimeException("Duplicate map id in catalog: " + id);
            }
            seen.put(id, true);

            List<String> collisionLayers = readStringArray(entry, "collisionLayers");
            if (collisionLayers.isEmpty()) {
                collisionLayers = List.of("WALL", "WALLS");
            }

            List<String> spawnLayers = readStringArray(entry, "spawnLayers");
            if (spawnLayers.isEmpty()) {
                spawnLayers = List.of("SpawnPoint");
            }

            float padding = entry.getFloat("padding", defaultPadding);
            ordered.add(new MapDefinition(id, path, collisionLayers, spawnLayers, padding));
        }

        String configuredDefault = root.getString("defaultMap", ordered.get(0).getId());
        String defaultId = seen.containsKey(configuredDefault) ? configuredDefault : ordered.get(0).getId();
        if (!defaultId.equals(configuredDefault)) {
            Gdx.app.error("MapCatalog", "defaultMap '" + configuredDefault + "' not found; using '" + defaultId + "'");
        }

        return new MapCatalog(ordered, defaultId);
    }

    public MapDefinition getDefaultMap() {
        return mapsById.get(defaultMapId);
    }

    public MapDefinition getById(String id) {
        return mapsById.get(id);
    }

    public MapDefinition nextFrom(String id) {
        if (orderedMaps.isEmpty()) {
            return null;
        }

        int index = -1;
        for (int i = 0; i < orderedMaps.size(); i++) {
            if (orderedMaps.get(i).getId().equals(id)) {
                index = i;
                break;
            }
        }

        if (index < 0) {
            return orderedMaps.get(0);
        }
        return orderedMaps.get((index + 1) % orderedMaps.size());
    }

    private static List<String> readStringArray(JsonValue node, String key) {
        List<String> values = new ArrayList<>();
        JsonValue arr = node.get(key);
        if (arr == null || !arr.isArray()) {
            return values;
        }
        for (JsonValue entry = arr.child; entry != null; entry = entry.next) {
            String value = entry.asString();
            if (value != null) {
                String trimmed = value.trim();
                if (!trimmed.isEmpty()) {
                    values.add(trimmed);
                }
            }
        }
        return values;
    }
}
