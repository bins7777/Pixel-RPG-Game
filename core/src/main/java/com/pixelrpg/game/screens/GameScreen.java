package com.pixelrpg.game.screens;

import java.util.Locale;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.maps.MapGroupLayer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.ShortArray;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.pixelrpg.game.config.MapCatalog;
import com.pixelrpg.game.config.MapDefinition;
import com.pixelrpg.game.entities.PlayerState;
import com.pixelrpg.game.helpers.ShaderFactory;
import com.pixelrpg.game.managers.DayNightCycleManager;
import com.pixelrpg.game.ui.HudRenderer;
import com.pixelrpg.game.world.WorldState;

/**
 * Minimal LibGDX game that loads the provided TMX world.map,
 * renders layers, and lets a player move with wall collisions.
 */
public class GameScreen extends ScreenAdapter {
    private static final String MAP_CATALOG_PATH = "config/maps.json";
    private static final String MAP_ID_VILLAGE = "village";
    private static final String MAP_ID_RESEARCH_LAB_FLOOR_1 = "research-lab-floor-1";
    private static final String MAP_ID_RESEARCH_LAB_FLOOR_2 = "research-lab-floor-2";
    private static final String MAP_ID_INN = "inn";
    private static final String MAP_ID_INN_ROOM = "inn-room";
    private static final String MAP_ID_DEMON_CASTLE = "demon-castle";
    private static final String FOREGROUND_LAYER_MARKER = "FRONT OF CHARACTR";
    private static final String FOREGROUND_LAYER_MARKER_ALT = "FRONT OF CHARACTER";
    private static final String FOREGROUND_ALWAYS_MARKER = "ALWAYS-INFRONT";
    private static final String DEPTH_SPLIT_GROUP_LAYER_NAME = "Table (Infront and Behind of character)";
    private static final String DEPTH_SPLIT_MARKER_LAYER_NAME = "BelowFront_AboveBehind";
    private static final String TORCH_ANIMATION_GROUP_NAME = "Torch Animation";
    private static final String TORCH_FRAME_PREFIX = "Torch ";
    private static final float TORCH_FRAME_DURATION_SECONDS = 0.1f;
    private static final String NPC_ANIMATION_GROUP_NAME = "NPC Animation";
    private static final String NPC_FRAME_PREFIX = "Scientist ";
    private static final float NPC_FRAME_DURATION_SECONDS = 0.12f;
    private static final float PLAYER_SIZE = 16f; // sprite base size
    private static final float HITBOX_WIDTH = 10f;
    private static final float HITBOX_HEIGHT = 14f;
    private static final float HITBOX_BOTTOM_OFFSET = 8f; // keep feet aligned when height changes
    private static final float SPRITE_FRAME_WIDTH = 240f;  // source frame size
    private static final float SPRITE_FRAME_HEIGHT = 128f; // source frame size
    private static final float PLAYER_SPEED = 57.6f;
    private static final float DASH_SPEED = 220f;
    private static final float DASH_DURATION = 0.108f;
    private static final float DASH_COOLDOWN = 0.6f;
    private static final int DASH_ATTACK_MOVE_FRAMES = 2;
    private static final float PLAYER_DRAW_SCALE = 1.0296f;
    private static final float PLAYER_SCALE_MULTIPLIER = 1.25f;
    private static final float PLAYER_DRAW_SCALE_X = 0.7f;
    private static final float PLAYER_DRAW_SCALE_Y = 1.5f;
    private static final float DEFAULT_ZOOM = 0.336f; // fixed zoom level (20% zoomed out)
    private static final float DEFAULT_MAP_PADDING = 160f;
    private static final int IDLE_FRAMES = 12;
    private static final int RUN_FRAMES = 8;
    private static final int ATTACK1_FRAMES = 3;
    private static final int DASH_ATTACK_FRAMES = 3;
    private static final int DIRECTION_FRAME_STEP = 1;
    private static final int SPRITE_TRIM_LEFT = 110;
    private static final int SPRITE_TRIM_TOP = 41;
    private static final int SPRITE_TRIM_WIDTH = 16;
    private static final int SPRITE_TRIM_HEIGHT = 39;
    private static final int ATTACK1_TRIM_LEFT = 100;
    private static final int ATTACK1_TRIM_TOP = 43;
    private static final int ATTACK1_TRIM_WIDTH = 104;
    private static final int ATTACK1_TRIM_HEIGHT = 39;
    private static final int ATTACK3_FRAMES = 5;
    private static final int ATTACK3_TRIM_LEFT = 91;
    private static final int ATTACK3_TRIM_TOP = 34;
    private static final int ATTACK3_TRIM_WIDTH = 74;
    private static final int ATTACK3_TRIM_HEIGHT = 48;
    private static final int DASH_ATTACK_TRIM_LEFT = 106;
    private static final int DASH_ATTACK_TRIM_TOP = 45;
    private static final int DASH_ATTACK_TRIM_WIDTH = 101;
    private static final int DASH_ATTACK_TRIM_HEIGHT = 37;
    private static final int UP_ATTACK_FRAMES = 4;
    private static final int DOWN_ATTACK_FRAMES = 4;
    private static final int IDLE_UP_ATTACK_FRAMES = 4;
    private static final int UP_ATTACK_TRIM_LEFT = 106;
    private static final int UP_ATTACK_TRIM_TOP = 7;
    private static final int UP_ATTACK_TRIM_WIDTH = 30;
    private static final int UP_ATTACK_TRIM_HEIGHT = 75;
    private static final int DOWN_ATTACK_TRIM_LEFT = 109;
    private static final int DOWN_ATTACK_TRIM_TOP = 39;
    private static final int DOWN_ATTACK_TRIM_WIDTH = 27;
    private static final int DOWN_ATTACK_TRIM_HEIGHT = 74;
    private static final int IDLE_UP_ATTACK_TRIM_LEFT = 106;
    private static final int IDLE_UP_ATTACK_TRIM_TOP = 7;
    private static final int IDLE_UP_ATTACK_TRIM_WIDTH = 30;
    private static final int IDLE_UP_ATTACK_TRIM_HEIGHT = 75;
    private static final float BODY_CENTER_X = SPRITE_TRIM_LEFT + SPRITE_TRIM_WIDTH / 2f;
    private static final float SHADOW_ALPHA = 0.35f;
    private static final float CHARACTER_SATURATION = 1.2f;
    private static final float ATTACK_COOLDOWN = 0.8f;
    private static final float ATTACK_FRAME_DURATION = 0.096f;
    private static final int VIGNETTE_SIZE = 1024;
    private static final float VIGNETTE_INNER = 0.28f;
    private static final float VIGNETTE_OUTER = 0.88f;
    private static final float VIGNETTE_MAX_ALPHA = 1f;
    private static final float NIGHT_VIGNETTE_MULTIPLIER = 1.9f;
    private static final float DAY_DURATION_SECONDS = 160f;
    private static final float SUNSET_DURATION_SECONDS = 32f;
    private static final float NIGHT_DURATION_SECONDS = 120f;
    private static final float SUNRISE_DURATION_SECONDS = 32f;
    private static final float NIGHT_TINT_R = 0f;
    private static final float NIGHT_TINT_G = 0f;
    private static final float NIGHT_TINT_B = 0f;
    private static final float NIGHT_TINT_MAX_ALPHA = 0.64f;
    private static final float NIGHT_WORLD_SATURATION = 0f; // full black & white at night
    private static final float NIGHT_CHARACTER_SATURATION = 2.2f; // stronger player-only color at night
    private static final int PLAYER_AURA_TEXTURE_SIZE = 128;
    private static final float PLAYER_AURA_RADIUS_X = 56f;
    private static final float PLAYER_AURA_RADIUS_Y = 48f;
    private static final float PLAYER_AURA_CENTER_Y_FACTOR = 0.58f;
    private static final float PLAYER_AURA_COLOR_R = 1f;
    private static final float PLAYER_AURA_COLOR_G = 0.78f;
    private static final float PLAYER_AURA_COLOR_B = 0.35f;
    private static final float PLAYER_AURA_MAX_ALPHA = 0.9f;
    private static final float PLAYER_COLOR_RESTORE_RADIUS_X = 36f;
    private static final float PLAYER_COLOR_RESTORE_RADIUS_Y = 30f;
    private static final float PLAYER_COLOR_RESTORE_CENTER_Y_FACTOR = 0.45f;
    private static final float LIGHT_TYPE_RECT = 0f;
    private static final float LIGHT_TYPE_ELLIPSE = 1f;
    private static final float LIGHT_TYPE_SOFT_ELLIPSE = 2f;
    private static final int MAX_LIGHT_AREAS = 16;
    private static final float DAY_NIGHT_SPEED_MIN = 0.25f;
    private static final float DAY_NIGHT_SPEED_MAX = 4f;
    private static final float DAY_NIGHT_SPEED_STEP = 0.25f;
    private static final float MAP_TRANSITION_FADE_OUT_SECONDS = 1.2f;
    private static final float MAP_TRANSITION_FADE_IN_SECONDS = 1.2f;
    private static final float QUEST_BLUR_TRANSITION_SECONDS = 1.0f;
    private static final String BAT_SLEEP_TEXTURE_PATH = "images/bat/Bat-Sleep.png";
    private static final int BAT_SLEEP_FRAMES = 3;
    private static final int BAT_FRAME_SIZE = 64;
    private static final float BAT_SLEEP_FRAME_DURATION = 0.18f;
    private static final float BAT_SLEEP_DRAW_SCALE = 0.5f;
    private static final float BAT_SPAWN_FORWARD_OFFSET = 20f;
    private static final float BAT_SPAWN_GROUND_OFFSET = -1f;

    private enum MapTransitionPhase {
        NONE,
        FADING_OUT,
        FADING_IN
    }
    private OrthographicCamera camera;
    private OrthographicCamera uiCamera;
    private SpriteBatch batch;
    private TextureRegion playerRegion; // fallback
    private Texture shadowTex;
    private TextureRegion shadowRegion;
    private Texture idleTex;
    private Texture runTex;
    private Texture attack1Tex;
    private Texture attack3Tex;
    private Texture dashAttackTex;
    private Texture vignetteTex;
    private Texture dayNightOverlayTex;
    private Texture playerAuraTex;
    private HudRenderer hudRenderer;
    private Texture jumpUpAttackTex;
    private Texture jumpDownAttackTex;
    private Texture idleUpAttackTex;
    private Texture batSleepTex;
    private Sound attack1Sfx;
    private Sound attack3Sfx;
    private Sound attackSpecialSfx;
    private ShaderProgram saturationShader;
    private ShaderProgram lightAreaMaskShader;
    private ShaderProgram screenBlurShader;
    private FrameBuffer questBlurFrameBuffer;
    private TextureRegion questBlurFrameRegion;
    private int questBlurBufferWidth = 0;
    private int questBlurBufferHeight = 0;
    private float questBlurProgress = 0f;
    private Animation<TextureRegion> idleAnim;
    private Animation<TextureRegion> runAnim;
    private Animation<TextureRegion> attack1Anim;
    private Animation<TextureRegion> attack3Anim;
    private Animation<TextureRegion> dashAttackAnim;
    private Animation<TextureRegion> jumpUpAttackAnim;
    private Animation<TextureRegion> jumpDownAttackAnim;
    private Animation<TextureRegion> idleUpAttackAnim;
    private Animation<TextureRegion> batSleepAnim;
    private Animation<TextureRegion> activeAttackAnim;
    private final WorldState world = new WorldState();
    private final PlayerState player = new PlayerState();
    private final EarClippingTriangulator triangulator = new EarClippingTriangulator();
    private final Polygon playerCollisionPolygon = new Polygon();
    private final float[] playerCollisionVertices = new float[8];
    private DayNightCycleManager dayNightCycle;
    private MapTransitionPhase mapTransitionPhase = MapTransitionPhase.NONE;
    private float mapTransitionTimer = 0f;
    private String pendingTransitionMapId;
    private String pendingTransitionSpawnLayer;
    private int depthSplitGroupLayerIndex = -1;
    private MapLayer depthSplitMarkerLayer;
    private InputProcessor previousInputProcessor;
    private InputProcessor activeInputProcessor;
    private final Vector2 batSleepPosition = new Vector2();
    private boolean batSpawned = false;
    private float batSleepStateTime = 0f;
    private final InputAdapter chatInputAdapter = new InputAdapter() {
        @Override
        public boolean keyTyped(char character) {
            if (hudRenderer == null || !hudRenderer.isChatOpen()) {
                return false;
            }
            if (character == '\b') {
                hudRenderer.removeLastChatCharacter();
                return true;
            }
            if (character == '\r' || character == '\n') {
                return true;
            }
            if (Character.isISOControl(character)) {
                return true;
            }
            hudRenderer.appendChatCharacter(character);
            return true;
        }
    };

    @Override
    public void show() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 960, 640); // standard LibGDX Y-up
        camera.zoom = DEFAULT_ZOOM; // locked zoom
        uiCamera = new OrthographicCamera();
        uiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch = new SpriteBatch();
        hudRenderer = new HudRenderer(uiCamera);
        hudRenderer.loadAssets();
        dayNightCycle = new DayNightCycleManager(
                DAY_DURATION_SECONDS,
                SUNSET_DURATION_SECONDS,
                NIGHT_DURATION_SECONDS,
                SUNRISE_DURATION_SECONDS,
                DAY_NIGHT_SPEED_MIN,
                DAY_NIGHT_SPEED_MAX);
        playerRegion = createPlayerRegion();
        shadowRegion = createShadowRegion();
        saturationShader = ShaderFactory.createSaturationShader("PixelRpgGame");
        lightAreaMaskShader = ShaderFactory.createLightAreaMaskShader("PixelRpgGame", MAX_LIGHT_AREAS);
        screenBlurShader = ShaderFactory.createScreenBlurShader("PixelRpgGame");
        recreateQuestBlurFrameBuffer(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
        loadAnimations();
        vignetteTex = createVignetteTexture(VIGNETTE_SIZE, VIGNETTE_INNER, VIGNETTE_OUTER, VIGNETTE_MAX_ALPHA);
        dayNightOverlayTex = createSolidTexture(1f, 1f, 1f, 1f);
        playerAuraTex = createPlayerAuraTexture(PLAYER_AURA_TEXTURE_SIZE);

        loadMapCatalog();
        loadMap();
        centerCameraOnPlayer();
        installInputProcessor();
    }

    @Override
    public void render(float delta) {
        player.stateTime += delta;
        if (batSpawned) {
            batSleepStateTime += delta;
        }
        updateDayNightCycle(delta);
        updateMapTransition(delta);
        if (isMapTransitionActive()) {
            player.moving = false;
        } else {
            updateAttack(delta);
            updateDash(delta);
            handleInput(delta);
        }
        updateTorchAnimation(delta);
        updateNpcAnimation(delta);
        hudRenderer.update(delta);
        updateQuestBlur(delta);
        clampCamera();

        if (world.mapRenderer == null) {
            return;
        }

        float nightBlend = currentNightBlend();
        float worldSaturation = lerp(1f, NIGHT_WORLD_SATURATION, nightBlend);
        int[] backgroundRenderLayers = activeBackgroundRenderLayers();
        int[] foregroundRenderLayers = activeForegroundRenderLayers();
        renderWorldWithOptionalQuestBlur(nightBlend, worldSaturation, backgroundRenderLayers, foregroundRenderLayers);
        hudRenderer.draw(batch, player.currentHealth, player.maxHealth, player.currentMana, player.maxMana, player.playerLevel);
        drawMapTransitionOverlay();
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height); // keep Y-up consistent
        camera.zoom = DEFAULT_ZOOM;
        recreateQuestBlurFrameBuffer(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
        if (hudRenderer != null) {
            hudRenderer.resize(width, height);
        }
    }

    @Override
    public void hide() {
        restoreInputProcessor();
    }

    @Override
    public void dispose() {
        restoreInputProcessor();
        unloadMap();
        if (batch != null) batch.dispose();
        if (playerRegion != null && playerRegion.getTexture() != null) playerRegion.getTexture().dispose();
        if (shadowTex != null) shadowTex.dispose();
        if (idleTex != null) idleTex.dispose();
        if (runTex != null) runTex.dispose();
        if (attack1Tex != null) attack1Tex.dispose();
        if (attack3Tex != null) attack3Tex.dispose();
        if (dashAttackTex != null) dashAttackTex.dispose();
        if (vignetteTex != null) vignetteTex.dispose();
        if (dayNightOverlayTex != null) dayNightOverlayTex.dispose();
        if (playerAuraTex != null) playerAuraTex.dispose();
        if (jumpUpAttackTex != null) jumpUpAttackTex.dispose();
        if (jumpDownAttackTex != null) jumpDownAttackTex.dispose();
        if (idleUpAttackTex != null) idleUpAttackTex.dispose();
        if (batSleepTex != null) batSleepTex.dispose();
        if (attack1Sfx != null) attack1Sfx.dispose();
        if (attack3Sfx != null) attack3Sfx.dispose();
        if (attackSpecialSfx != null) attackSpecialSfx.dispose();
        if (saturationShader != null) saturationShader.dispose();
        if (lightAreaMaskShader != null) lightAreaMaskShader.dispose();
        if (screenBlurShader != null) screenBlurShader.dispose();
        disposeQuestBlurFrameBuffer();
        if (hudRenderer != null) hudRenderer.dispose();
    }

    private void installInputProcessor() {
        previousInputProcessor = Gdx.input.getInputProcessor();
        if (previousInputProcessor == null) {
            activeInputProcessor = chatInputAdapter;
        } else {
            InputMultiplexer multiplexer = new InputMultiplexer();
            multiplexer.addProcessor(chatInputAdapter);
            multiplexer.addProcessor(previousInputProcessor);
            activeInputProcessor = multiplexer;
        }
        Gdx.input.setInputProcessor(activeInputProcessor);
    }

    private void restoreInputProcessor() {
        if (Gdx.input.getInputProcessor() == activeInputProcessor) {
            Gdx.input.setInputProcessor(previousInputProcessor);
        }
        previousInputProcessor = null;
        activeInputProcessor = null;
    }

    private void loadMapCatalog() {
        world.mapCatalog = MapCatalog.load(MAP_CATALOG_PATH, DEFAULT_MAP_PADDING);
        world.activeMap = world.mapCatalog.getDefaultMap();
        world.activeMapId = world.activeMap.getId();
    }

    private void loadMap() {
        loadMap(world.activeMapId, null);
    }

    private void loadMap(String mapId) {
        loadMap(mapId, null);
    }

    private void loadMap(String mapId, String preferredSpawnLayer) {
        MapDefinition target = world.mapCatalog.getById(mapId);
        if (target == null) {
            Gdx.app.error("PixelRpgGame", "Unknown world.map id '" + mapId + "', using default.");
            target = world.mapCatalog.getDefaultMap();
        }

        unloadMap();

        TmxMapLoader loader = new TmxMapLoader();
        TmxMapLoader.Parameters params = new TmxMapLoader.Parameters();
        params.flipY = true; // flip to LibGDX's Y-up world
        try {
            world.map = loader.load(target.getMapPath(), params);
        } catch (RuntimeException primaryError) {
            MapDefinition fallback = world.mapCatalog.getDefaultMap();
            if (fallback != null && !fallback.getId().equals(target.getId())) {
                Gdx.app.error("PixelRpgGame",
                        "Failed to load world.map '" + target.getId() + "' from '" + target.getMapPath()
                                + "'. Falling back to '" + fallback.getId() + "'.",
                        primaryError);
                world.map = loader.load(fallback.getMapPath(), params);
                target = fallback;
            } else {
                throw primaryError;
            }
        }
        world.mapRenderer = new OrthogonalTiledMapRenderer(world.map, 1f);
        world.activeMap = target;
        world.activeMapId = target.getId();

        extractWalls();
        extractLightAreas();
        extractRenderLayerOrder();
        configureTorchAnimation();
        configureNpcAnimation();
        extractBoundsFromChunks();
        placePlayerAtSpawn(preferredSpawnLayer);
        clearSpawnedBat();
    }

    private void unloadMap() {
        if (world.map != null) {
            world.map.dispose();
            world.map = null;
        }
        if (world.mapRenderer != null) {
            world.mapRenderer.dispose();
            world.mapRenderer = null;
        }
        world.walls.clear();
        world.wallPolygons.clear();
        world.lightAreaRects.clear();
        world.lightAreaEllipses.clear();
        world.lightAreaPolygons.clear();
        world.torchAnimationFrames.clear();
        world.torchAnimationFrameIndex = 0;
        world.torchAnimationTimer = 0f;
        world.npcAnimationFrames.clear();
        world.npcAnimationFrameIndex = 0;
        world.npcAnimationTimer = 0f;
        world.backgroundLayerIndices = new int[0];
        world.foregroundLayerIndices = new int[0];
        depthSplitGroupLayerIndex = -1;
        depthSplitMarkerLayer = null;
        clearSpawnedBat();
    }

    private void reloadCurrentMap() {
        loadMap(world.activeMapId);
    }

    private void loadNextMap() {
        MapDefinition next = world.mapCatalog.nextFrom(world.activeMapId);
        if (next != null) {
            loadMap(next.getId());
        }
    }

    private void extractWalls() {
        world.walls.clear();
        world.wallPolygons.clear();

        if (world.activeMap == null) {
            return;
        }

        for (String layerName : world.activeMap.getCollisionLayers()) {
            collectCollisionLayersByName(world.map.getLayers(), layerName);
        }
    }

    private void extractLightAreas() {
        world.lightAreaRects.clear();
        world.lightAreaEllipses.clear();
        world.lightAreaPolygons.clear();

        MapLayer lightLayer = findLayerByName("LightAreas");
        if (lightLayer == null) {
            return;
        }

        for (MapObject object : lightLayer.getObjects()) {
            if (object instanceof RectangleMapObject rectObj) {
                world.lightAreaRects.add(new Rectangle(rectObj.getRectangle()));
            } else if (object instanceof EllipseMapObject ellipseObj) {
                world.lightAreaEllipses.add(new Ellipse(ellipseObj.getEllipse()));
            } else if (object instanceof PolygonMapObject polyObj) {
                float[] vertices = polyObj.getPolygon().getTransformedVertices();
                if (vertices != null && vertices.length >= 6) {
                    world.lightAreaPolygons.add(vertices.clone());
                }
            }
        }
    }

    private void extractRenderLayerOrder() {
        if (world.map == null) {
            world.backgroundLayerIndices = new int[0];
            world.foregroundLayerIndices = new int[0];
            return;
        }

        MapLayers layers = world.map.getLayers();
        IntArray background = new IntArray(layers.getCount());
        IntArray foreground = new IntArray();

        for (int i = 0; i < layers.getCount(); i++) {
            MapLayer layer = layers.get(i);
            if (isForegroundRenderLayer(layer)) {
                foreground.add(i);
            } else {
                background.add(i);
            }
        }

        world.backgroundLayerIndices = background.toArray();
        world.foregroundLayerIndices = foreground.toArray();
        configureDepthSplitLayer();
    }

    private void configureDepthSplitLayer() {
        depthSplitGroupLayerIndex = -1;
        depthSplitMarkerLayer = null;
        if (world.map == null) {
            return;
        }

        MapLayer groupLayer = findLayerByName(DEPTH_SPLIT_GROUP_LAYER_NAME);
        if (groupLayer == null) {
            return;
        }
        int topLevelIndex = world.map.getLayers().getIndex(groupLayer);
        if (topLevelIndex < 0) {
            return;
        }

        MapLayer markerLayer = findLayerByName(DEPTH_SPLIT_MARKER_LAYER_NAME);
        if (markerLayer == null) {
            return;
        }

        depthSplitGroupLayerIndex = topLevelIndex;
        depthSplitMarkerLayer = markerLayer;
    }

    private int[] activeBackgroundRenderLayers() {
        return applyDepthSplitLayer(world.backgroundLayerIndices, false);
    }

    private int[] activeForegroundRenderLayers() {
        return applyDepthSplitLayer(world.foregroundLayerIndices, true);
    }

    private int[] applyDepthSplitLayer(int[] baseLayers, boolean foregroundPass) {
        if (baseLayers == null) {
            return null;
        }
        if (depthSplitGroupLayerIndex < 0 || depthSplitMarkerLayer == null) {
            return baseLayers;
        }

        boolean renderDepthSplitInFront = isPlayerAboveDepthSplitMarker();
        boolean shouldContain = foregroundPass ? renderDepthSplitInFront : !renderDepthSplitInFront;
        boolean contains = containsLayerIndex(baseLayers, depthSplitGroupLayerIndex);
        if (contains == shouldContain) {
            return baseLayers;
        }
        return shouldContain
                ? insertLayerIndexSorted(baseLayers, depthSplitGroupLayerIndex)
                : removeLayerIndex(baseLayers, depthSplitGroupLayerIndex);
    }

    private boolean isPlayerAboveDepthSplitMarker() {
        if (depthSplitMarkerLayer == null) {
            return false;
        }

        float playerCenterX = player.bounds.x + player.bounds.width / 2f;
        Float markerY = findDepthSplitMarkerY(playerCenterX);
        if (markerY == null) {
            return false;
        }

        // Compare with feet position so the split point tracks table occlusion naturally.
        return player.bounds.y > markerY;
    }

    private Float findDepthSplitMarkerY(float worldX) {
        float nearestDistanceX = Float.MAX_VALUE;
        Float nearestY = null;
        for (MapObject object : depthSplitMarkerLayer.getObjects()) {
            Rectangle bounds = objectBounds(depthSplitMarkerLayer, object);
            if (bounds == null) {
                continue;
            }
            float distanceX = horizontalDistanceToRange(worldX, bounds.x, bounds.x + bounds.width);
            if (distanceX < nearestDistanceX) {
                nearestDistanceX = distanceX;
                nearestY = bounds.y + bounds.height / 2f;
            }
        }
        return nearestY;
    }

    private float horizontalDistanceToRange(float x, float minX, float maxX) {
        if (x < minX) {
            return minX - x;
        }
        if (x > maxX) {
            return x - maxX;
        }
        return 0f;
    }

    private boolean containsLayerIndex(int[] layers, int target) {
        for (int layer : layers) {
            if (layer == target) {
                return true;
            }
        }
        return false;
    }

    private int[] removeLayerIndex(int[] layers, int target) {
        int count = 0;
        for (int layer : layers) {
            if (layer != target) {
                count++;
            }
        }
        if (count == layers.length) {
            return layers;
        }
        int[] result = new int[count];
        int out = 0;
        for (int layer : layers) {
            if (layer != target) {
                result[out++] = layer;
            }
        }
        return result;
    }

    private int[] insertLayerIndexSorted(int[] layers, int target) {
        if (containsLayerIndex(layers, target)) {
            return layers;
        }
        int[] result = new int[layers.length + 1];
        int out = 0;
        boolean inserted = false;
        for (int layer : layers) {
            if (!inserted && target < layer) {
                result[out++] = target;
                inserted = true;
            }
            result[out++] = layer;
        }
        if (!inserted) {
            result[out] = target;
        }
        return result;
    }

    private void configureTorchAnimation() {
        world.torchAnimationFrames.clear();
        world.torchAnimationFrameIndex = 0;
        world.torchAnimationTimer = 0f;

        MapLayer layer = findLayerByName(TORCH_ANIMATION_GROUP_NAME);
        if (!(layer instanceof MapGroupLayer torchGroup)) {
            return;
        }

        MapLayer[] orderedFrames = new MapLayer[32];
        int maxFrame = 0;
        for (MapLayer frameLayer : torchGroup.getLayers()) {
            int frameNumber = parseTorchFrameNumber(frameLayer.getName());
            if (frameNumber <= 0 || frameNumber > orderedFrames.length) {
                continue;
            }
            orderedFrames[frameNumber - 1] = frameLayer;
            if (frameNumber > maxFrame) {
                maxFrame = frameNumber;
            }
        }

        for (int i = 0; i < maxFrame; i++) {
            MapLayer frameLayer = orderedFrames[i];
            if (frameLayer == null) {
                continue;
            }
            frameLayer.setVisible(false);
            world.torchAnimationFrames.add(frameLayer);
        }

        if (world.torchAnimationFrames.size > 0) {
            world.torchAnimationFrames.first().setVisible(true);
        }
    }

    private void configureNpcAnimation() {
        world.npcAnimationFrames.clear();
        world.npcAnimationFrameIndex = 0;
        world.npcAnimationTimer = 0f;

        MapLayer layer = findLayerByName(NPC_ANIMATION_GROUP_NAME);
        if (!(layer instanceof MapGroupLayer npcGroup)) {
            return;
        }

        MapLayer[] orderedFrames = new MapLayer[32];
        int maxFrame = 0;
        for (MapLayer frameLayer : npcGroup.getLayers()) {
            int frameNumber = parseNpcFrameNumber(frameLayer.getName());
            if (frameNumber <= 0 || frameNumber > orderedFrames.length) {
                continue;
            }
            orderedFrames[frameNumber - 1] = frameLayer;
            if (frameNumber > maxFrame) {
                maxFrame = frameNumber;
            }
        }

        for (int i = 0; i < maxFrame; i++) {
            MapLayer frameLayer = orderedFrames[i];
            if (frameLayer == null) {
                continue;
            }
            frameLayer.setVisible(false);
            world.npcAnimationFrames.add(frameLayer);
        }

        if (world.npcAnimationFrames.size > 0) {
            world.npcAnimationFrames.first().setVisible(true);
        }
    }

    private int parseTorchFrameNumber(String layerName) {
        if (layerName == null) {
            return -1;
        }
        String trimmed = layerName.trim();
        if (!trimmed.regionMatches(true, 0, TORCH_FRAME_PREFIX, 0, TORCH_FRAME_PREFIX.length())) {
            return -1;
        }
        String framePart = trimmed.substring(TORCH_FRAME_PREFIX.length()).trim();
        try {
            return Integer.parseInt(framePart);
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private int parseNpcFrameNumber(String layerName) {
        if (layerName == null) {
            return -1;
        }
        String trimmed = layerName.trim();
        if (!trimmed.regionMatches(true, 0, NPC_FRAME_PREFIX, 0, NPC_FRAME_PREFIX.length())) {
            return -1;
        }
        String framePart = trimmed.substring(NPC_FRAME_PREFIX.length()).trim();
        try {
            return Integer.parseInt(framePart);
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private boolean isForegroundRenderLayer(MapLayer layer) {
        if (layer == null) {
            return false;
        }
        if (hasForegroundMarker(layer.getName())) {
            return true;
        }
        if (layer instanceof MapGroupLayer groupLayer) {
            for (MapLayer nestedLayer : groupLayer.getLayers()) {
                if (isForegroundRenderLayer(nestedLayer)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasForegroundMarker(String layerName) {
        if (layerName == null || layerName.isBlank()) {
            return false;
        }
        String upperName = layerName.toUpperCase(Locale.ROOT);
        return upperName.contains(FOREGROUND_LAYER_MARKER)
                || upperName.contains(FOREGROUND_LAYER_MARKER_ALT)
                || upperName.contains(FOREGROUND_ALWAYS_MARKER);
    }

    private void collectCollisionLayersByName(MapLayers layers, String targetName) {
        for (MapLayer layer : layers) {
            if (targetName.equalsIgnoreCase(layer.getName())) {
                addCollisionObjects(layer);
            }
            if (layer instanceof MapGroupLayer groupLayer) {
                collectCollisionLayersByName(groupLayer.getLayers(), targetName);
            }
        }
    }

    private void addCollisionObjects(MapLayer layer) {
        for (MapObject object : layer.getObjects()) {
            if (object instanceof RectangleMapObject rectObj) {
                Rectangle r = new Rectangle(rectObj.getRectangle());
                world.walls.add(r);
            } else if (object instanceof PolygonMapObject polyObj) {
                addPolygonCollision(polyObj.getPolygon());
            }
        }
    }

    private MapLayer findLayerByName(String name) {
        return findLayerByName(world.map.getLayers(), name);
    }

    private MapLayer findLayerByName(MapLayers layers, String name) {
        for (MapLayer layer : layers) {
            if (name.equalsIgnoreCase(layer.getName())) {
                return layer;
            }
            if (layer instanceof MapGroupLayer groupLayer) {
                MapLayer nested = findLayerByName(groupLayer.getLayers(), name);
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }

    private void addPolygonCollision(Polygon polygon) {
        float[] vertices = polygon.getTransformedVertices();
        if (vertices == null || vertices.length < 6) {
            return;
        }

        if (vertices.length == 6) {
            world.wallPolygons.add(new Polygon(vertices.clone()));
            return;
        }

        ShortArray indices = triangulator.computeTriangles(vertices);
        if (indices.size < 3) {
            world.walls.add(new Rectangle(polygon.getBoundingRectangle()));
            return;
        }

        for (int i = 0; i + 2 < indices.size; i += 3) {
            int i1 = indices.get(i) * 2;
            int i2 = indices.get(i + 1) * 2;
            int i3 = indices.get(i + 2) * 2;
            world.wallPolygons.add(new Polygon(new float[] {
                    vertices[i1], vertices[i1 + 1],
                    vertices[i2], vertices[i2 + 1],
                    vertices[i3], vertices[i3 + 1]
            }));
        }
    }

    private void extractBoundsFromChunks() {
        try {
            MapProperties props = world.map.getProperties();
            Object infiniteProp = props.get("infinite");
            boolean isInfinite = false;
            if (infiniteProp instanceof Boolean b) {
                isInfinite = b;
            } else if (infiniteProp instanceof Integer i) {
                isInfinite = i == 1;
            }

            int tileWidth = props.get("tilewidth", Integer.class);
            int tileHeight = props.get("tileheight", Integer.class);

            if (!isInfinite) {
                int w = props.get("width", Integer.class);
                int h = props.get("height", Integer.class);
                world.mapBounds = new Rectangle(0, 0, w * tileWidth, h * tileHeight);
            } else {
                XmlReader reader = new XmlReader();
                Element root = reader.parse(Gdx.files.internal(world.activeMap.getMapPath()));
                int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
                int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
                boolean found = false;

                Array<Element> layers = root.getChildrenByNameRecursively("layer");
                for (Element layer : layers) {
                    Element data = layer.getChildByName("data");
                    if (data == null) {
                        continue;
                    }
                    Array<Element> chunks = data.getChildrenByName("chunk");
                    for (Element chunk : chunks) {
                        int cx = chunk.getIntAttribute("x");
                        int cy = chunk.getIntAttribute("y");
                        int cw = chunk.getIntAttribute("width");
                        int ch = chunk.getIntAttribute("height");
                        minX = Math.min(minX, cx);
                        minY = Math.min(minY, cy);
                        maxX = Math.max(maxX, cx + cw);
                        maxY = Math.max(maxY, cy + ch);
                        found = true;
                    }
                }

                if (!found) {
                    int w = props.get("width", Integer.class);
                    int h = props.get("height", Integer.class);
                    minX = 0;
                    minY = 0;
                    maxX = w;
                    maxY = h;
                }

                world.mapBounds = new Rectangle(
                        minX * tileWidth,
                        minY * tileHeight,
                        (maxX - minX) * tileWidth,
                        (maxY - minY) * tileHeight
                );
            }

            float padding = world.activeMap != null ? world.activeMap.getMapPadding() : DEFAULT_MAP_PADDING;
            world.mapBounds = new Rectangle(
                    world.mapBounds.x - padding,
                    world.mapBounds.y - padding,
                    world.mapBounds.width + padding * 2f,
                    world.mapBounds.height + padding * 2f
            );
        } catch (Exception e) {
            Gdx.app.error("PixelRpgGame", "Failed to compute world.map bounds from chunks for " + world.activeMap.getMapPath(), e);
            // Keep fallback bounds if parsing fails.
        }
    }

    private void placePlayerAtSpawn(String preferredSpawnLayer) {
        Vector2 spawn = new Vector2(world.mapBounds.x + world.mapBounds.width / 2f, world.mapBounds.y + world.mapBounds.height / 2f);

        MapLayer layer = null;
        if (preferredSpawnLayer != null && !preferredSpawnLayer.isBlank()) {
            layer = findLayerByName(preferredSpawnLayer);
        }
        if (layer == null && world.activeMap != null) {
            for (String layerName : world.activeMap.getSpawnLayers()) {
                layer = findLayerByName(layerName);
                if (layer != null) {
                    break;
                }
            }
        }
        if (layer != null) {
            Vector2 layerSpawn = extractLayerSpawn(layer);
            if (layerSpawn != null) {
                spawn.set(layerSpawn);
            }
        }

        // If spawn is inside a wall, nudge to nearest free spot.
        Vector2 spawnBottom = new Vector2(spawn.x, spawn.y - HITBOX_BOTTOM_OFFSET);
        Vector2 free = findNearestFreeSpot(spawnBottom);
        float halfW = HITBOX_WIDTH / 2f;
        player.bounds.set(free.x - halfW, free.y, HITBOX_WIDTH, HITBOX_HEIGHT);
        onPlayerSpawned();
    }

    private void onPlayerSpawned() {
        if (hudRenderer == null) {
            return;
        }
        if (MAP_ID_VILLAGE.equals(world.activeMapId)) {
            hudRenderer.showVillageSpawnNotification();
            return;
        }
        hudRenderer.hideVillageSpawnNotification();
    }

    private void updateAttack(float delta) {
        if (player.attackCooldownTimer > 0f) {
            player.attackCooldownTimer = Math.max(0f, player.attackCooldownTimer - delta);
        }
        if (!player.attacking || activeAttackAnim == null) {
            return;
        }
        player.attackTimer += delta;
        if (activeAttackAnim.isAnimationFinished(player.attackTimer)) {
            if (player.dashAttackFollowupQueued && activeAttackAnim == dashAttackAnim && attack3Anim != null) {
                player.dashAttackFollowupQueued = false;
                activeAttackAnim = attack3Anim;
                player.attacking = true;
                player.attackTimer = 0f;
                player.attackCooldownTimer = ATTACK_COOLDOWN;
                player.attackComboStep = 0;
                if (attack3Sfx != null) {
                    attack3Sfx.play();
                }
                return;
            }
            player.dashAttackFollowupQueued = false;
            player.attacking = false;
            player.attackTimer = 0f;
            activeAttackAnim = null;
        }
    }

    private void updateDash(float delta) {
        if (player.dashCooldownTimer > 0f) {
            player.dashCooldownTimer = Math.max(0f, player.dashCooldownTimer - delta);
        }
        if (!player.dashing) {
            return;
        }
        player.dashTimer += delta;
        if (player.dashAttackActive && player.attackTimer >= player.dashAttackMoveDuration) {
            player.dashing = false;
            player.dashTimer = 0f;
            player.dashAttackActive = false;
            player.dashAttackMoveDuration = 0f;
            return;
        }
        float step = DASH_SPEED * delta;
        boolean moved = false;
        if (player.dashDirX != 0f) {
            moved |= tryMove(player.dashDirX * step, 0);
        }
        if (player.dashDirY != 0f) {
            moved |= tryMove(0, player.dashDirY * step);
        }
        player.moving = moved || (player.dashDirX != 0f || player.dashDirY != 0f);
        if (player.dashAttackActive) {
            if (!moved || player.attackTimer >= player.dashAttackMoveDuration) {
                player.dashing = false;
                player.dashTimer = 0f;
                player.dashAttackActive = false;
                player.dashAttackMoveDuration = 0f;
            }
            return;
        }
        if (player.dashTimer >= DASH_DURATION) {
            player.dashing = false;
            player.dashTimer = 0f;
            player.dashAttackActive = false;
            player.dashAttackMoveDuration = 0f;
        }
    }

    private void updateTorchAnimation(float delta) {
        if (world.torchAnimationFrames.size <= 1) {
            return;
        }

        world.torchAnimationTimer += delta;
        while (world.torchAnimationTimer >= TORCH_FRAME_DURATION_SECONDS) {
            world.torchAnimationTimer -= TORCH_FRAME_DURATION_SECONDS;
            world.torchAnimationFrames.get(world.torchAnimationFrameIndex).setVisible(false);
            world.torchAnimationFrameIndex = (world.torchAnimationFrameIndex + 1) % world.torchAnimationFrames.size;
            world.torchAnimationFrames.get(world.torchAnimationFrameIndex).setVisible(true);
        }
    }

    private void updateNpcAnimation(float delta) {
        if (world.npcAnimationFrames.size <= 1) {
            return;
        }

        world.npcAnimationTimer += delta;
        while (world.npcAnimationTimer >= NPC_FRAME_DURATION_SECONDS) {
            world.npcAnimationTimer -= NPC_FRAME_DURATION_SECONDS;
            world.npcAnimationFrames.get(world.npcAnimationFrameIndex).setVisible(false);
            world.npcAnimationFrameIndex = (world.npcAnimationFrameIndex + 1) % world.npcAnimationFrames.size;
            world.npcAnimationFrames.get(world.npcAnimationFrameIndex).setVisible(true);
        }
    }

    private void updateDayNightCycle(float delta) {
        if (dayNightCycle != null) {
            dayNightCycle.update(delta);
        }
    }

    private void forceDayTime() {
        if (dayNightCycle != null) {
            dayNightCycle.forceDayTime();
        }
    }

    private void forceNightTime() {
        if (dayNightCycle != null) {
            dayNightCycle.forceNightTime();
        }
    }

    private void adjustDayNightSpeed(float deltaScale) {
        if (dayNightCycle != null) {
            dayNightCycle.adjustSpeed(deltaScale);
        }
    }

    private void setDayNightSpeed(float scale) {
        if (dayNightCycle != null) {
            dayNightCycle.setSpeed(scale);
        }
    }

    private void handleInput(float delta) {
        if (hudRenderer != null && hudRenderer.isChatOpen()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                String submittedMessage = hudRenderer.submitChatDraft();
                hudRenderer.closeChat(false);
                handleChatCommand(submittedMessage);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                hudRenderer.closeChat(true);
            }
            player.moving = false;
            centerCameraOnPlayer();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && hudRenderer != null) {
            hudRenderer.openChat();
            player.moving = false;
            centerCameraOnPlayer();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
            reloadCurrentMap();
            centerCameraOnPlayer();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F6)) {
            loadNextMap();
            centerCameraOnPlayer();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F7)) {
            forceDayTime();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F8)) {
            forceNightTime();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT_BRACKET)) {
            adjustDayNightSpeed(DAY_NIGHT_SPEED_STEP);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT_BRACKET)) {
            adjustDayNightSpeed(-DAY_NIGHT_SPEED_STEP);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSLASH)) {
            setDayNightSpeed(1f);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.M) && hudRenderer != null) {
            hudRenderer.toggleMainMenu();
        }

        boolean leftPressed = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);
        boolean upPressed = Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP);
        boolean downPressed = Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN);
        boolean leftMovePressed = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean rightMovePressed = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        boolean horizPressed = leftMovePressed || rightMovePressed;
        boolean dashPressed = Gdx.input.isKeyJustPressed(Input.Keys.SPACE);
        boolean interactPressed = Gdx.input.isKeyJustPressed(Input.Keys.E);
        if (interactPressed && tryHandleMapInteraction()) {
            centerCameraOnPlayer();
            return;
        }
        boolean dashAttackPressed = interactPressed;

        if (dashAttackPressed && !player.attacking && !player.dashing && player.attackCooldownTimer <= 0f && player.dashCooldownTimer <= 0f) {
            player.dashAttackFollowupQueued = false;
            if (dashAttackAnim != null) {
                activeAttackAnim = dashAttackAnim;
                player.attacking = true;
                player.attackTimer = 0f;
                player.attackCooldownTimer = ATTACK_COOLDOWN;
                player.attackComboStep = 0;
                player.dashAttackFollowupQueued = attack3Anim != null;
                if (attackSpecialSfx != null) {
                    attackSpecialSfx.play();
                }
            }
            startDashAttackFromLastMove();
        }

        if (leftPressed && !player.attacking && player.attackCooldownTimer <= 0f) {
            Sound attackSound = null;
            if (upPressed) {
                activeAttackAnim = jumpUpAttackAnim;
                attackSound = attackSpecialSfx;
            } else if (downPressed) {
                activeAttackAnim = jumpDownAttackAnim;
                attackSound = attackSpecialSfx;
            } else if (!horizPressed) {
                activeAttackAnim = idleUpAttackAnim;
                attackSound = attackSpecialSfx;
            } else if (player.attackComboStep >= 2 && attack3Anim != null) {
                activeAttackAnim = attack3Anim;
                player.attackComboStep = 0;
                attackSound = attack3Sfx;
            } else {
                activeAttackAnim = attack1Anim;
                if (player.attackComboStep < 2) {
                    player.attackComboStep += 1;
                }
                attackSound = attack1Sfx;
            }
            if (activeAttackAnim != null) {
                player.attacking = true;
                player.attackTimer = 0f;
                player.attackCooldownTimer = ATTACK_COOLDOWN;
                if (attackSound != null) {
                    attackSound.play();
                }
            }
        }

        if (dashPressed && !player.attacking && !player.dashing && player.dashCooldownTimer <= 0f) {
            startDashFromLastMove();
        }

        if (player.dashing) {
            centerCameraOnPlayer();
            return;
        }

        float dx = 0;
        float dy = 0;
        if (upPressed) {
            dy += 1;
        }
        if (downPressed) {
            dy -= 1;
        }
        if (leftMovePressed) {
            dx -= 1;
        }
        if (rightMovePressed) {
            dx += 1;
        }

        if (dx != 0 && dy != 0) {
            float inv = (float) (1 / Math.sqrt(2));
            dx *= inv;
            dy *= inv;
        }

        if (dx != 0 || dy != 0) {
            player.lastMoveX = dx;
            player.lastMoveY = dy;
        }

        float step = PLAYER_SPEED * delta;
        player.moving = false;
        if (tryMove(dx * step, 0)) {
            player.moving = player.moving || dx != 0;
            if (dx != 0) player.facingLeft = dx < 0;
        }
        if (tryMove(0, dy * step)) {
            player.moving = player.moving || dy != 0;
        }
        // zoom locked
        centerCameraOnPlayer();
    }

    private void handleChatCommand(String submittedMessage) {
        if (submittedMessage == null || submittedMessage.isBlank() || world.mapCatalog == null) {
            return;
        }
        String normalized = submittedMessage.trim();
        if (normalized.length() >= 2
                && ((normalized.startsWith("\"") && normalized.endsWith("\""))
                || (normalized.startsWith("'") && normalized.endsWith("'")))) {
            normalized = normalized.substring(1, normalized.length() - 1).trim();
        }
        if (normalized.isEmpty()) {
            return;
        }

        String[] tokens = normalized.split("\\s+");
        if (tokens.length < 2) {
            return;
        }

        String command = tokens[0].toLowerCase(Locale.ROOT);
        if ("/to".equals(command)) {
            String destination = tokens[1].toLowerCase(Locale.ROOT);
            if ("castle".equals(destination)) {
                teleportToMap(MAP_ID_DEMON_CASTLE, "Spawn");
            } else if ("village".equals(destination)) {
                teleportToMap(MAP_ID_VILLAGE, null);
            }
            return;
        }

        if ("/spawn".equals(command) && "bat".equalsIgnoreCase(tokens[1])) {
            spawnBatInFrontOfPlayer();
        }
    }

    private void teleportToMap(String mapId, String preferredSpawnLayer) {
        if (mapId == null || mapId.isBlank() || world.mapCatalog == null) {
            return;
        }
        if (world.mapCatalog.getById(mapId) == null) {
            Gdx.app.error("PixelRpgGame",
                    "Chat teleport failed: unknown map id '" + mapId + "'.");
            return;
        }
        loadMap(mapId, preferredSpawnLayer);
        centerCameraOnPlayer();
    }

    private void spawnBatInFrontOfPlayer() {
        if (batSleepAnim == null) {
            return;
        }
        float directionX = player.facingLeft ? -1f : 1f;
        if (player.lastMoveX < 0f) {
            directionX = -1f;
        } else if (player.lastMoveX > 0f) {
            directionX = 1f;
        }
        float spawnX = player.bounds.x + player.bounds.width * 0.5f + directionX * BAT_SPAWN_FORWARD_OFFSET;
        float spawnY = player.bounds.y + BAT_SPAWN_GROUND_OFFSET;
        batSleepPosition.set(spawnX, spawnY);
        batSleepStateTime = 0f;
        batSpawned = true;
    }

    private void clearSpawnedBat() {
        batSpawned = false;
        batSleepStateTime = 0f;
    }

    private boolean tryHandleMapInteraction() {
        return tryMapTransition(
                MAP_ID_VILLAGE,
                "ENTRANCE-Research-Lab",
                MAP_ID_RESEARCH_LAB_FLOOR_1,
                "EXIT-Research-Lab")
                || tryMapTransition(
                        MAP_ID_RESEARCH_LAB_FLOOR_1,
                        "EXIT-Research-Lab",
                        MAP_ID_VILLAGE,
                        "ENTRANCE-Research-Lab")
                || tryMapTransition(
                        MAP_ID_VILLAGE,
                        "ENTRANCE-Inn",
                        MAP_ID_INN,
                        "EXIT-Inn")
                || tryMapTransition(
                        MAP_ID_INN,
                        "EXIT-Inn",
                        MAP_ID_VILLAGE,
                        "ENTRANCE-Inn")
                || tryMapTransition(
                        MAP_ID_INN,
                        "ENTRANCE-Inn-Room",
                        MAP_ID_INN_ROOM,
                        "EXIT-Inn-Room")
                || tryMapTransition(
                        MAP_ID_INN_ROOM,
                        "EXIT-Inn-Room",
                        MAP_ID_INN,
                        "ENTRANCE-Inn-Room")
                || tryMapTransition(
                        MAP_ID_RESEARCH_LAB_FLOOR_1,
                        "ENTRANCE-Research-Lab-Floor-2",
                        MAP_ID_RESEARCH_LAB_FLOOR_2,
                        "EXIT-Research-Lab-Floor-2")
                || tryMapTransition(
                        MAP_ID_RESEARCH_LAB_FLOOR_2,
                        "EXIT-Research-Lab-Floor-2",
                        MAP_ID_RESEARCH_LAB_FLOOR_1,
                        "ENTRANCE-Research-Lab-Floor-2");
    }

    private boolean tryMapTransition(String fromMapId, String triggerLayerName, String toMapId, String targetSpawnLayer) {
        if (world.activeMapId == null || !world.activeMapId.equals(fromMapId)) {
            return false;
        }
        if (!isPlayerWithinObjectLayer(triggerLayerName)) {
            return false;
        }
        startMapTransition(toMapId, targetSpawnLayer);
        return true;
    }

    private void startMapTransition(String toMapId, String targetSpawnLayer) {
        if (toMapId == null || toMapId.isBlank()) {
            return;
        }
        if (isMapTransitionActive()) {
            return;
        }
        pendingTransitionMapId = toMapId;
        pendingTransitionSpawnLayer = targetSpawnLayer;
        mapTransitionPhase = MapTransitionPhase.FADING_OUT;
        mapTransitionTimer = 0f;
    }

    private boolean isMapTransitionActive() {
        return mapTransitionPhase != MapTransitionPhase.NONE;
    }

    private void updateMapTransition(float delta) {
        if (mapTransitionPhase == MapTransitionPhase.NONE) {
            return;
        }

        mapTransitionTimer += Math.max(0f, delta);

        if (mapTransitionPhase == MapTransitionPhase.FADING_OUT) {
            if (mapTransitionTimer < MAP_TRANSITION_FADE_OUT_SECONDS) {
                return;
            }

            String toMapId = pendingTransitionMapId;
            String targetSpawnLayer = pendingTransitionSpawnLayer;
            pendingTransitionMapId = null;
            pendingTransitionSpawnLayer = null;

            if (toMapId != null) {
                loadMap(toMapId, targetSpawnLayer);
                centerCameraOnPlayer();
            }

            mapTransitionPhase = MapTransitionPhase.FADING_IN;
            mapTransitionTimer = 0f;
            return;
        }

        if (mapTransitionTimer >= MAP_TRANSITION_FADE_IN_SECONDS) {
            mapTransitionPhase = MapTransitionPhase.NONE;
            mapTransitionTimer = 0f;
        }
    }

    private void drawMapTransitionOverlay() {
        if (dayNightOverlayTex == null || uiCamera == null) {
            return;
        }
        float alpha = mapTransitionAlpha();
        if (alpha <= 0.001f) {
            return;
        }

        uiCamera.update();
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        batch.setShader(null);
        batch.setColor(0f, 0f, 0f, alpha);
        batch.draw(dayNightOverlayTex, 0f, 0f, uiCamera.viewportWidth, uiCamera.viewportHeight);
        batch.setColor(1f, 1f, 1f, 1f);
        batch.end();
    }

    private float mapTransitionAlpha() {
        if (mapTransitionPhase == MapTransitionPhase.FADING_OUT) {
            if (MAP_TRANSITION_FADE_OUT_SECONDS <= 0f) {
                return 1f;
            }
            return clamp01(mapTransitionTimer / MAP_TRANSITION_FADE_OUT_SECONDS);
        }
        if (mapTransitionPhase == MapTransitionPhase.FADING_IN) {
            if (MAP_TRANSITION_FADE_IN_SECONDS <= 0f) {
                return 0f;
            }
            return clamp01(1f - (mapTransitionTimer / MAP_TRANSITION_FADE_IN_SECONDS));
        }
        return 0f;
    }

    private boolean isPlayerWithinObjectLayer(String layerName) {
        MapLayer layer = findLayerByName(layerName);
        if (layer == null) {
            return false;
        }
        for (MapObject object : layer.getObjects()) {
            if (doesPlayerOverlap(layer, object)) {
                return true;
            }
        }
        return false;
    }

    private boolean doesPlayerOverlap(MapLayer layer, MapObject object) {
        Rectangle bounds = objectBounds(layer, object);
        if (bounds == null) {
            return false;
        }
        if (bounds.width <= 0f && bounds.height <= 0f) {
            return player.bounds.contains(bounds.x, bounds.y);
        }
        return Intersector.overlaps(player.bounds, bounds);
    }

    private Rectangle objectBounds(MapLayer layer, MapObject object) {
        float offsetX = layer != null ? layer.getRenderOffsetX() : 0f;
        float offsetY = layer != null ? layer.getRenderOffsetY() : 0f;
        if (object instanceof RectangleMapObject rectObj) {
            Rectangle rect = rectObj.getRectangle();
            return new Rectangle(rect.x + offsetX, rect.y + offsetY, rect.width, rect.height);
        }
        if (object instanceof PolygonMapObject polyObj) {
            Rectangle bounds = polyObj.getPolygon().getBoundingRectangle();
            return new Rectangle(bounds.x + offsetX, bounds.y + offsetY, bounds.width, bounds.height);
        }
        if (object instanceof EllipseMapObject ellipseObj) {
            Ellipse ellipse = ellipseObj.getEllipse();
            return new Rectangle(ellipse.x + offsetX, ellipse.y + offsetY, ellipse.width, ellipse.height);
        }

        MapProperties properties = object.getProperties();
        Float x = properties.get("x", Float.class);
        Float y = properties.get("y", Float.class);
        if (x == null || y == null) {
            return null;
        }
        return new Rectangle(x + offsetX, y + offsetY, 0f, 0f);
    }

    private Vector2 extractLayerSpawn(MapLayer layer) {
        if (layer == null || layer.getObjects().getCount() == 0) {
            return null;
        }

        float offsetX = layer.getRenderOffsetX();
        float offsetY = layer.getRenderOffsetY();
        MapObject obj = layer.getObjects().get(0);
        if (obj instanceof RectangleMapObject rectObj) {
            Rectangle r = rectObj.getRectangle();
            return new Vector2(r.x + r.width / 2f + offsetX, r.y + r.height / 2f + offsetY);
        }

        Float x = obj.getProperties().get("x", Float.class);
        Float y = obj.getProperties().get("y", Float.class);
        if (x != null && y != null) {
            return new Vector2(x + offsetX, y + offsetY);
        }
        return null;
    }

    private void startDashFromLastMove() {
        player.dashAttackActive = false;
        player.dashAttackMoveDuration = 0f;
        float dx = player.lastMoveX;
        float dy = player.lastMoveY;
        if (dx == 0f && dy == 0f) {
            dx = player.facingLeft ? -1f : 1f;
        }
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len == 0f) {
            return;
        }
        player.dashDirX = dx / len;
        player.dashDirY = dy / len;
        if (player.dashDirX != 0f) player.facingLeft = player.dashDirX < 0f;
        player.dashing = true;
        player.dashTimer = 0f;
        player.dashCooldownTimer = DASH_COOLDOWN;
    }

    private void startDashAttackFromLastMove() {
        float dx = player.lastMoveX;
        float dy = player.lastMoveY;
        if (dx == 0f && dy == 0f) {
            dx = player.facingLeft ? -1f : 1f;
        }
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len == 0f) {
            return;
        }
        player.dashDirX = dx / len;
        player.dashDirY = dy / len;
        if (player.dashDirX != 0f) player.facingLeft = player.dashDirX < 0f;
        player.dashing = true;
        player.dashTimer = 0f;
        player.dashCooldownTimer = DASH_COOLDOWN;
        player.dashAttackActive = true;
        if (dashAttackAnim != null) {
            float frameDuration = dashAttackAnim.getFrameDuration();
            float animDuration = dashAttackAnim.getAnimationDuration();
            player.dashAttackMoveDuration = Math.min(animDuration, frameDuration * DASH_ATTACK_MOVE_FRAMES);
        } else {
            player.dashAttackMoveDuration = DASH_DURATION;
        }
    }

    private boolean tryMove(float dx, float dy) {
        if (dx == 0 && dy == 0) return false;
        Rectangle next = new Rectangle(player.bounds);
        next.x += dx;
        next.y += dy;
        if (collides(next)) {
            return false;
        }
        player.bounds.set(next);
        return true;
    }

    private boolean collides(Rectangle rect) {
        for (Rectangle wall : world.walls) {
            if (Intersector.overlaps(rect, wall)) {
                return true;
            }
        }

        if (world.wallPolygons.size > 0) {
            playerCollisionVertices[0] = rect.x;
            playerCollisionVertices[1] = rect.y;
            playerCollisionVertices[2] = rect.x + rect.width;
            playerCollisionVertices[3] = rect.y;
            playerCollisionVertices[4] = rect.x + rect.width;
            playerCollisionVertices[5] = rect.y + rect.height;
            playerCollisionVertices[6] = rect.x;
            playerCollisionVertices[7] = rect.y + rect.height;
            playerCollisionPolygon.setVertices(playerCollisionVertices);

            for (Polygon wall : world.wallPolygons) {
                if (Intersector.overlapConvexPolygons(playerCollisionPolygon, wall)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Vector2 findNearestFreeSpot(Vector2 start) {
        float step = 16f; // tile-sized steps
        float halfW = HITBOX_WIDTH / 2f;
        Rectangle test = new Rectangle(0, 0, HITBOX_WIDTH, HITBOX_HEIGHT);

        // try the starting spot first
        test.set(start.x - halfW, start.y, HITBOX_WIDTH, HITBOX_HEIGHT);
        if (!collides(test)) return new Vector2(start);

        for (int radius = 1; radius <= 8; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    if (Math.abs(dx) != radius && Math.abs(dy) != radius) continue; // perimeter only
                    float nx = start.x + dx * step;
                    float ny = start.y + dy * step;
                    test.set(nx - halfW, ny, HITBOX_WIDTH, HITBOX_HEIGHT);
                    if (!collides(test)) {
                        return new Vector2(nx, ny);
                    }
                }
            }
        }
        return new Vector2(start);
    }

    private void centerCameraOnPlayer() {
        camera.position.set(player.bounds.x + player.bounds.width / 2f, player.bounds.y + player.bounds.height / 2f, 0);
    }

    private void clampCamera() {
        float halfW = camera.viewportWidth / 2f;
        float halfH = camera.viewportHeight / 2f;

        float minX = world.mapBounds.x + halfW;
        float minY = world.mapBounds.y + halfH;
        float maxX = world.mapBounds.x + world.mapBounds.width - halfW;
        float maxY = world.mapBounds.y + world.mapBounds.height - halfH;

        if (minX > maxX) {
            camera.position.x = world.mapBounds.x + world.mapBounds.width / 2f;
        } else {
            camera.position.x = Math.min(Math.max(camera.position.x, minX), maxX);
        }

        if (minY > maxY) {
            camera.position.y = world.mapBounds.y + world.mapBounds.height / 2f;
        } else {
            camera.position.y = Math.min(Math.max(camera.position.y, minY), maxY);
        }
    }

    private void loadAnimations() {
        idleTex = new Texture(Gdx.files.internal("images/huntress/1 - Idle.png"));
        runTex = new Texture(Gdx.files.internal("images/huntress/2 - Run.png"));
        attack1Tex = new Texture(Gdx.files.internal("images/huntress/3 - Attack 1.png"));
        attack3Tex = new Texture(Gdx.files.internal("images/huntress/4 - Attack 3.png"));
        dashAttackTex = new Texture(Gdx.files.internal("images/huntress/12 - Dash Attack.png"));
        jumpUpAttackTex = new Texture(Gdx.files.internal("images/huntress/5 - Jump Up Attack.png"));
        jumpDownAttackTex = new Texture(Gdx.files.internal("images/huntress/6 - Jump Down Attack.png"));
        idleUpAttackTex = new Texture(Gdx.files.internal("images/huntress/7 - Idle Up Attack.png"));
        attack1Sfx = Gdx.audio.newSound(Gdx.files.internal("sounds/attack 1.mp3"));
        attack3Sfx = Gdx.audio.newSound(Gdx.files.internal("sounds/attack 3.mp3"));
        attackSpecialSfx = Gdx.audio.newSound(Gdx.files.internal("sounds/attacks.mp3"));
        idleTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        runTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        attack1Tex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        attack3Tex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        dashAttackTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        jumpUpAttackTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        jumpDownAttackTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        idleUpAttackTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        // Trim transparent margins to avoid clipping artifacts during animation.
        idleAnim = buildAnimation(idleTex, IDLE_FRAMES / DIRECTION_FRAME_STEP,
                (int) SPRITE_FRAME_WIDTH, (int) SPRITE_FRAME_HEIGHT, 0.12f, 0, DIRECTION_FRAME_STEP);
        runAnim = buildAnimation(runTex, RUN_FRAMES / DIRECTION_FRAME_STEP,
                (int) SPRITE_FRAME_WIDTH, (int) SPRITE_FRAME_HEIGHT, 0.08f, 0, DIRECTION_FRAME_STEP);
        attack1Anim = buildAnimation(attack1Tex, ATTACK1_FRAMES,
                (int) SPRITE_FRAME_WIDTH, (int) SPRITE_FRAME_HEIGHT, ATTACK_FRAME_DURATION, 0, 1);
        attack1Anim.setPlayMode(Animation.PlayMode.NORMAL);
        attack3Anim = buildAnimation(attack3Tex, ATTACK3_FRAMES,
                (int) SPRITE_FRAME_WIDTH, (int) SPRITE_FRAME_HEIGHT, ATTACK_FRAME_DURATION, 0, 1);
        attack3Anim.setPlayMode(Animation.PlayMode.NORMAL);
        dashAttackAnim = buildAnimation(dashAttackTex, DASH_ATTACK_FRAMES,
                (int) SPRITE_FRAME_WIDTH, (int) SPRITE_FRAME_HEIGHT, ATTACK_FRAME_DURATION, 0, 1);
        dashAttackAnim.setPlayMode(Animation.PlayMode.NORMAL);
        jumpUpAttackAnim = buildAnimation(jumpUpAttackTex, UP_ATTACK_FRAMES,
                (int) SPRITE_FRAME_WIDTH, (int) SPRITE_FRAME_HEIGHT, ATTACK_FRAME_DURATION, 0, 1);
        jumpUpAttackAnim.setPlayMode(Animation.PlayMode.NORMAL);
        jumpDownAttackAnim = buildAnimation(jumpDownAttackTex, DOWN_ATTACK_FRAMES,
                (int) SPRITE_FRAME_WIDTH, (int) SPRITE_FRAME_HEIGHT, ATTACK_FRAME_DURATION, 0, 1);
        jumpDownAttackAnim.setPlayMode(Animation.PlayMode.NORMAL);
        idleUpAttackAnim = buildAnimation(idleUpAttackTex, IDLE_UP_ATTACK_FRAMES,
                (int) SPRITE_FRAME_WIDTH, (int) SPRITE_FRAME_HEIGHT, ATTACK_FRAME_DURATION, 0, 1);
        idleUpAttackAnim.setPlayMode(Animation.PlayMode.NORMAL);
        loadBatAssets();
    }

    private void loadBatAssets() {
        try {
            batSleepTex = new Texture(Gdx.files.internal(BAT_SLEEP_TEXTURE_PATH));
            batSleepTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            batSleepAnim = buildRawAnimation(
                    batSleepTex,
                    BAT_SLEEP_FRAMES,
                    BAT_FRAME_SIZE,
                    BAT_FRAME_SIZE,
                    BAT_SLEEP_FRAME_DURATION);
        } catch (RuntimeException error) {
            batSleepAnim = null;
            batSpawned = false;
            Gdx.app.error("PixelRpgGame", "Failed to load bat sleep texture from '" + BAT_SLEEP_TEXTURE_PATH + "'.", error);
        }
    }

    private TextureRegion createPlayerRegion() {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(0.2f, 0.6f, 0.95f, 1f);
        pm.fill();
        Texture tex = new Texture(pm);
        pm.dispose();
        return new TextureRegion(tex);
    }

    private TextureRegion createShadowRegion() {
        int size = 32;
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setColor(0f, 0f, 0f, 1f);
        pm.fillCircle(size / 2, size / 2, size / 2);
        shadowTex = new Texture(pm);
        shadowTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pm.dispose();
        return new TextureRegion(shadowTex);
    }

    private Texture createSolidTexture(float r, float g, float b, float a) {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(r, g, b, a);
        pm.fill();
        Texture tex = new Texture(pm);
        pm.dispose();
        return tex;
    }

    private Texture createVignetteTexture(int size, float innerRadius, float outerRadius, float maxAlpha) {
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        int center = size / 2;
        float maxDist = center;
        float range = Math.max(outerRadius - innerRadius, 0.001f);

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = x + 0.5f - center;
                float dy = y + 0.5f - center;
                float dist = (float) Math.sqrt(dx * dx + dy * dy) / maxDist;
                float t = smoothStep((dist - innerRadius) / range);
                float alpha = t * maxAlpha;
                pm.setColor(0f, 0f, 0f, alpha);
                pm.drawPixel(x, y);
            }
        }

        Texture texture = new Texture(pm);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pm.dispose();
        return texture;
    }

    private Texture createPlayerAuraTexture(int size) {
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        int center = size / 2;
        float maxDist = center;

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = x + 0.5f - center;
                float dy = y + 0.5f - center;
                float dist = (float) Math.sqrt(dx * dx + dy * dy) / maxDist;
                float alpha = (1f - smoothStep(dist)) * PLAYER_AURA_MAX_ALPHA;
                pm.setColor(1f, 1f, 1f, alpha);
                pm.drawPixel(x, y);
            }
        }

        Texture texture = new Texture(pm);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pm.dispose();
        return texture;
    }

    private void drawVignette() {
        if (vignetteTex == null || uiCamera == null) {
            return;
        }
        float nightBlend = currentNightBlend();
        float vignetteAlpha = clamp01(nightBlend * NIGHT_VIGNETTE_MULTIPLIER);
        if (vignetteAlpha <= 0.001f) {
            return;
        }
        uiCamera.update();
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        batch.setShader(null);
        batch.setColor(1f, 1f, 1f, vignetteAlpha);
        batch.draw(vignetteTex, 0f, 0f, uiCamera.viewportWidth, uiCamera.viewportHeight);
        batch.setColor(1f, 1f, 1f, 1f);
        batch.end();
    }

    private void drawPlayerColorAura(float nightBlend) {
        if (playerAuraTex == null || nightBlend <= 0.001f) {
            return;
        }

        float alpha = clamp01(nightBlend);
        if (alpha <= 0.001f) {
            return;
        }

        float centerX = player.bounds.x + player.bounds.width * 0.5f;
        float centerY = player.bounds.y + player.bounds.height * PLAYER_AURA_CENTER_Y_FACTOR;
        float drawW = PLAYER_AURA_RADIUS_X * 2f;
        float drawH = PLAYER_AURA_RADIUS_Y * 2f;

        batch.setShader(null);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        batch.setColor(PLAYER_AURA_COLOR_R, PLAYER_AURA_COLOR_G, PLAYER_AURA_COLOR_B, alpha);
        batch.draw(playerAuraTex, centerX - drawW * 0.5f, centerY - drawH * 0.5f, drawW, drawH);
        batch.setColor(1f, 1f, 1f, 1f);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    private void updateQuestBlur(float delta) {
        boolean mainMenuVisible = hudRenderer != null && hudRenderer.isMainMenuVisible();
        if (!mainMenuVisible) {
            questBlurProgress = 0f;
            return;
        }
        if (QUEST_BLUR_TRANSITION_SECONDS <= 0f) {
            questBlurProgress = 1f;
            return;
        }

        float step = Math.max(0f, delta) / QUEST_BLUR_TRANSITION_SECONDS;
        questBlurProgress = Math.min(1f, questBlurProgress + step);
    }

    private void renderWorldWithOptionalQuestBlur(float nightBlend, float worldSaturation,
                                                  int[] backgroundRenderLayers, int[] foregroundRenderLayers) {
        if (questBlurProgress <= 0.001f || screenBlurShader == null) {
            renderWorldScene(nightBlend, worldSaturation, backgroundRenderLayers, foregroundRenderLayers);
            return;
        }

        ensureQuestBlurFrameBufferUpToDate();
        if (questBlurFrameBuffer == null || questBlurFrameRegion == null) {
            renderWorldScene(nightBlend, worldSaturation, backgroundRenderLayers, foregroundRenderLayers);
            return;
        }

        questBlurFrameBuffer.begin();
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        renderWorldScene(nightBlend, worldSaturation, backgroundRenderLayers, foregroundRenderLayers);
        questBlurFrameBuffer.end();

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        uiCamera.update();
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        batch.setShader(screenBlurShader);
        float texelX = questBlurBufferWidth > 0 ? 1f / questBlurBufferWidth : 0f;
        float texelY = questBlurBufferHeight > 0 ? 1f / questBlurBufferHeight : 0f;
        screenBlurShader.setUniformf("u_texelSize", texelX, texelY);
        screenBlurShader.setUniformf("u_blurAmount", questBlurProgress);
        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(questBlurFrameRegion, 0f, 0f, uiCamera.viewportWidth, uiCamera.viewportHeight);
        batch.setShader(null);
        batch.end();
    }

    private void renderWorldScene(float nightBlend, float worldSaturation,
                                  int[] backgroundRenderLayers, int[] foregroundRenderLayers) {
        camera.update();
        world.mapRenderer.setView(camera);
        renderMapWithSaturation(backgroundRenderLayers, worldSaturation);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        float characterSaturation = lerp(CHARACTER_SATURATION, NIGHT_CHARACTER_SATURATION, nightBlend);
        drawPlayer(characterSaturation);
        drawSpawnedBat(characterSaturation);
        batch.end();

        renderMapWithSaturation(foregroundRenderLayers, worldSaturation);
        drawDayNightOverlay();
        drawVignette();
    }

    private void ensureQuestBlurFrameBufferUpToDate() {
        int width = Gdx.graphics.getBackBufferWidth();
        int height = Gdx.graphics.getBackBufferHeight();
        if (width <= 0 || height <= 0) {
            return;
        }
        if (questBlurFrameBuffer == null || width != questBlurBufferWidth || height != questBlurBufferHeight) {
            recreateQuestBlurFrameBuffer(width, height);
        }
    }

    private void recreateQuestBlurFrameBuffer(int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }
        disposeQuestBlurFrameBuffer();
        questBlurFrameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
        Texture colorTexture = questBlurFrameBuffer.getColorBufferTexture();
        colorTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        questBlurFrameRegion = new TextureRegion(colorTexture);
        questBlurFrameRegion.flip(false, true);
        questBlurBufferWidth = width;
        questBlurBufferHeight = height;
    }

    private void disposeQuestBlurFrameBuffer() {
        if (questBlurFrameBuffer != null) {
            questBlurFrameBuffer.dispose();
            questBlurFrameBuffer = null;
        }
        questBlurFrameRegion = null;
        questBlurBufferWidth = 0;
        questBlurBufferHeight = 0;
    }

    private void renderMapWithSaturation(float saturation) {
        renderMapWithSaturation(null, saturation);
    }

    private void renderMapWithSaturation(int[] layerIndices, float saturation) {
        if (layerIndices != null && layerIndices.length == 0) {
            return;
        }
        if (saturationShader != null) {
            var mapBatch = world.mapRenderer.getBatch();
            mapBatch.setShader(saturationShader);
            saturationShader.setUniformf("u_saturation", saturation);
        }
        if (layerIndices == null) {
            world.mapRenderer.render();
        } else {
            world.mapRenderer.render(layerIndices);
        }
        if (saturationShader != null) {
            world.mapRenderer.getBatch().setShader(null);
        }
    }

    private void drawLightAreaColorRestore(float nightBlend) {
        drawLightAreaColorRestore(nightBlend, null);
    }

    private void drawLightAreaColorRestore(float nightBlend, int[] layerIndices) {
        if (nightBlend <= 0.001f || lightAreaMaskShader == null || !hasColorRestoreAreas()) {
            return;
        }
        if (layerIndices != null && layerIndices.length == 0) {
            return;
        }

        camera.update();
        world.mapRenderer.setView(camera);
        var mapBatch = world.mapRenderer.getBatch();
        mapBatch.setShader(lightAreaMaskShader);
        applyLightAreaUniforms(lightAreaMaskShader);
        if (layerIndices == null) {
            world.mapRenderer.render();
        } else {
            world.mapRenderer.render(layerIndices);
        }
        mapBatch.setShader(null);
    }

    private boolean hasLightAreas() {
        return world.lightAreaRects.size > 0 || world.lightAreaEllipses.size > 0 || world.lightAreaPolygons.size > 0;
    }

    private boolean hasPlayerColorRestoreArea() {
        return player.bounds.width > 0f && player.bounds.height > 0f;
    }

    private boolean hasColorRestoreAreas() {
        return hasLightAreas() || hasPlayerColorRestoreArea();
    }

    private void applyLightAreaUniforms(ShaderProgram shader) {
        int count = 0;
        count = applyPlayerColorRestoreUniform(shader, count);
        for (Rectangle rect : world.lightAreaRects) {
            if (count >= MAX_LIGHT_AREAS) break;
            shader.setUniformf("u_lightAreas[" + count + "]", rect.x, rect.y, rect.width, rect.height);
            shader.setUniformf("u_lightTypes[" + count + "]", LIGHT_TYPE_RECT);
            count++;
        }
        for (Ellipse ellipse : world.lightAreaEllipses) {
            if (count >= MAX_LIGHT_AREAS) break;
            shader.setUniformf("u_lightAreas[" + count + "]", ellipse.x, ellipse.y, ellipse.width, ellipse.height);
            shader.setUniformf("u_lightTypes[" + count + "]", LIGHT_TYPE_ELLIPSE);
            count++;
        }
        for (float[] vertices : world.lightAreaPolygons) {
            if (count >= MAX_LIGHT_AREAS) break;
            Rectangle bounds = polygonBounds(vertices);
            if (bounds == null) {
                continue;
            }
            shader.setUniformf("u_lightAreas[" + count + "]", bounds.x, bounds.y, bounds.width, bounds.height);
            shader.setUniformf("u_lightTypes[" + count + "]", LIGHT_TYPE_RECT);
            count++;
        }
        for (int i = count; i < MAX_LIGHT_AREAS; i++) {
            shader.setUniformf("u_lightAreas[" + i + "]", 0f, 0f, 0f, 0f);
            shader.setUniformf("u_lightTypes[" + i + "]", LIGHT_TYPE_RECT);
        }
        shader.setUniformf("u_lightAreaCount", (float) count);
    }

    private int applyPlayerColorRestoreUniform(ShaderProgram shader, int count) {
        if (count >= MAX_LIGHT_AREAS || !hasPlayerColorRestoreArea()) {
            return count;
        }
        float centerX = player.bounds.x + player.bounds.width * 0.5f;
        float centerY = player.bounds.y + player.bounds.height * PLAYER_COLOR_RESTORE_CENTER_Y_FACTOR;
        float width = PLAYER_COLOR_RESTORE_RADIUS_X * 2f;
        float height = PLAYER_COLOR_RESTORE_RADIUS_Y * 2f;
        shader.setUniformf("u_lightAreas[" + count + "]",
                centerX - width * 0.5f,
                centerY - height * 0.5f,
                width,
                height);
        shader.setUniformf("u_lightTypes[" + count + "]", LIGHT_TYPE_SOFT_ELLIPSE);
        return count + 1;
    }

    private Rectangle polygonBounds(float[] vertices) {
        if (vertices == null || vertices.length < 6) {
            return null;
        }
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        for (int i = 0; i + 1 < vertices.length; i += 2) {
            float x = vertices[i];
            float y = vertices[i + 1];
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
        }
        return new Rectangle(minX, minY, Math.max(0f, maxX - minX), Math.max(0f, maxY - minY));
    }

    private void drawDayNightOverlay() {
        if (dayNightOverlayTex == null || uiCamera == null) {
            return;
        }

        float nightBlend = currentNightBlend();
        if (nightBlend <= 0.001f) {
            return;
        }

        float alpha = nightBlend * NIGHT_TINT_MAX_ALPHA;
        uiCamera.update();
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        batch.setShader(null);
        batch.setColor(NIGHT_TINT_R, NIGHT_TINT_G, NIGHT_TINT_B, alpha);
        batch.draw(dayNightOverlayTex, 0f, 0f, uiCamera.viewportWidth, uiCamera.viewportHeight);
        batch.setColor(1f, 1f, 1f, 1f);
        batch.end();
    }

    private float currentNightBlend() {
        if (dayNightCycle == null) {
            return 0f;
        }
        return dayNightCycle.nightBlend();
    }

    private Animation<TextureRegion> buildAnimation(Texture sheet, int frames, int frameWidth, int frameHeight, float frameDuration) {
        return buildAnimation(sheet, frames, frameWidth, frameHeight, frameDuration, 0, 1);
    }

    private Animation<TextureRegion> buildRawAnimation(Texture sheet, int frames, int frameWidth, int frameHeight, float frameDuration) {
        TextureRegion[][] split = TextureRegion.split(sheet, frameWidth, frameHeight);
        Array<TextureRegion> list = new Array<>(frames);
        for (TextureRegion[] row : split) {
            for (TextureRegion region : row) {
                list.add(new TextureRegion(region));
                if (list.size >= frames) {
                    return new Animation<>(frameDuration, list, Animation.PlayMode.LOOP);
                }
            }
        }
        return new Animation<>(frameDuration, list, Animation.PlayMode.LOOP);
    }

    private Animation<TextureRegion> buildAnimation(Texture sheet, int frames, int frameWidth, int frameHeight,
                                                    float frameDuration, int startIndex, int frameStep) {
        TextureRegion[][] split = TextureRegion.split(sheet, frameWidth, frameHeight);
        Array<TextureRegion> list = new Array<>(frames);
        int index = 0;
        for (TextureRegion[] row : split) {
            for (TextureRegion region : row) {
                if (index >= startIndex && ((index - startIndex) % frameStep == 0)) {
                    list.add(trimFrame(region));
                    if (list.size >= frames) break;
                }
                index++;
            }
            if (list.size >= frames) break;
        }
        return new Animation<>(frameDuration, list, Animation.PlayMode.LOOP);
    }

    private TextureRegion trimFrame(TextureRegion region) {
        if (jumpUpAttackTex != null && region.getTexture() == jumpUpAttackTex) {
            return new TextureRegion(region, UP_ATTACK_TRIM_LEFT, UP_ATTACK_TRIM_TOP,
                    UP_ATTACK_TRIM_WIDTH, UP_ATTACK_TRIM_HEIGHT);
        }
        if (jumpDownAttackTex != null && region.getTexture() == jumpDownAttackTex) {
            return new TextureRegion(region, DOWN_ATTACK_TRIM_LEFT, DOWN_ATTACK_TRIM_TOP,
                    DOWN_ATTACK_TRIM_WIDTH, DOWN_ATTACK_TRIM_HEIGHT);
        }
        if (idleUpAttackTex != null && region.getTexture() == idleUpAttackTex) {
            return new TextureRegion(region, IDLE_UP_ATTACK_TRIM_LEFT, IDLE_UP_ATTACK_TRIM_TOP,
                    IDLE_UP_ATTACK_TRIM_WIDTH, IDLE_UP_ATTACK_TRIM_HEIGHT);
        }
        if (attack3Tex != null && region.getTexture() == attack3Tex) {
            return new TextureRegion(region, ATTACK3_TRIM_LEFT, ATTACK3_TRIM_TOP,
                    ATTACK3_TRIM_WIDTH, ATTACK3_TRIM_HEIGHT);
        }
        if (dashAttackTex != null && region.getTexture() == dashAttackTex) {
            return new TextureRegion(region, DASH_ATTACK_TRIM_LEFT, DASH_ATTACK_TRIM_TOP,
                    DASH_ATTACK_TRIM_WIDTH, DASH_ATTACK_TRIM_HEIGHT);
        }
        if (attack1Tex != null && region.getTexture() == attack1Tex) {
            return new TextureRegion(region, ATTACK1_TRIM_LEFT, ATTACK1_TRIM_TOP,
                    ATTACK1_TRIM_WIDTH, ATTACK1_TRIM_HEIGHT);
        }
        return new TextureRegion(region, SPRITE_TRIM_LEFT, SPRITE_TRIM_TOP, SPRITE_TRIM_WIDTH, SPRITE_TRIM_HEIGHT);
    }

    private TextureRegion currentFrame() {
        if (player.attacking && activeAttackAnim != null) {
            return activeAttackAnim.getKeyFrame(player.attackTimer);
        }
        return (player.moving ? runAnim : idleAnim).getKeyFrame(player.stateTime);
    }

    private void drawPlayer(float saturation) {
        TextureRegion frame = currentFrame();
        float drawW = frame.getRegionWidth();
        float drawH = frame.getRegionHeight();

        // scale sprite to collision box size
        float scaleX = (PLAYER_SIZE / SPRITE_TRIM_WIDTH) * PLAYER_DRAW_SCALE * PLAYER_DRAW_SCALE_X * PLAYER_SCALE_MULTIPLIER;
        float scaleY = (PLAYER_SIZE / SPRITE_TRIM_HEIGHT) * PLAYER_DRAW_SCALE * PLAYER_DRAW_SCALE_Y * PLAYER_SCALE_MULTIPLIER;

        float originX = bodyCenterInRegion(frame);
        float originY = 0f;
        float drawX = player.bounds.x + player.bounds.width / 2f - originX;
        float drawY = player.bounds.y - originY;
        float flipX = player.facingLeft ? -1f : 1f;

        float shadowW = HITBOX_WIDTH * 1.05f;
        float shadowH = HITBOX_HEIGHT * 0.35f;
        float shadowX = player.bounds.x + player.bounds.width / 2f - shadowW / 2f;
        float shadowY = player.bounds.y - shadowH * 0.25f;
        batch.setColor(1f, 1f, 1f, SHADOW_ALPHA);
        batch.draw(shadowRegion, shadowX, shadowY, shadowW, shadowH);
        batch.setColor(1f, 1f, 1f, 1f);

        if (saturationShader != null) {
            batch.setShader(saturationShader);
            saturationShader.setUniformf("u_saturation", saturation);
        }
        batch.draw(frame, drawX, drawY, originX, originY,
                drawW, drawH,
                scaleX * flipX, scaleY, 0f);
        if (saturationShader != null) {
            batch.setShader(null);
        }
    }

    private void drawSpawnedBat(float saturation) {
        if (!batSpawned || batSleepAnim == null) {
            return;
        }
        TextureRegion frame = batSleepAnim.getKeyFrame(batSleepStateTime, true);
        float drawW = frame.getRegionWidth() * BAT_SLEEP_DRAW_SCALE;
        float drawH = frame.getRegionHeight() * BAT_SLEEP_DRAW_SCALE;
        float drawX = batSleepPosition.x - drawW * 0.5f;
        float drawY = batSleepPosition.y;

        if (saturationShader != null) {
            batch.setShader(saturationShader);
            saturationShader.setUniformf("u_saturation", saturation);
        }
        batch.draw(frame, drawX, drawY, drawW, drawH);
        if (saturationShader != null) {
            batch.setShader(null);
        }
    }

    private static float clamp01(float value) {
        return Math.max(0f, Math.min(1f, value));
    }

    private static float smoothStep(float t) {
        float x = clamp01(t);
        return x * x * (3f - 2f * x);
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * clamp01(t);
    }

    private float bodyCenterInRegion(TextureRegion frame) {
        int frameWidth = (int) SPRITE_FRAME_WIDTH;
        int frameX = (frame.getRegionX() / frameWidth) * frameWidth;
        float bodyCenterX = BODY_CENTER_X + frameX;
        return bodyCenterX - frame.getRegionX();
    }
}






