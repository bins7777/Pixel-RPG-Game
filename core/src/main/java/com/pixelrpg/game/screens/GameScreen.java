package com.pixelrpg.game.screens;

import java.util.Arrays;
import java.util.Locale;

import com.badlogic.gdx.assets.loaders.resolvers.AbsoluteFileHandleResolver;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
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
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
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
    private static final String MAP_ID_BOSS_ARENA_PHASE_2 = "boss-arena-phase-2";
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
    private static final String ENTRANCE_DOOR_ANIMATION_GROUP_NAME = "DOOR-Entrance-Animation";
    private static final String ENTRANCE_DOOR_FRONT_ANIMATION_GROUP_NAME = "DOOR-Entrance-Animation-Front ALWAYS-INFRONT";
    private static final String ENTRANCE_DOOR_TRIGGER_LAYER_NAME = "Entrance-Door-AutoOpen-Close";
    private static final String DOOR_FRAME_PREFIX = "DOOR-";
    private static final float ENTRANCE_DOOR_FRAME_DURATION_SECONDS = 0.1f;
    private static final float ENTRANCE_DOOR_ENTRY_CROSS_MARGIN = 4f;
    private static final String ROOM1_DOOR_ANIMATION_GROUP_NAME = "DOOR-Room-1-Animation";
    private static final String ROOM1_DOOR_FRONT_ANIMATION_GROUP_NAME = "DOOR-Room-1-Animation-Front ALWAYS-INFRONT";
    private static final String ROOM1_DOOR_TRIGGER_LAYER_NAME = "Door-Close-Room-1";
    private static final String ROOM1_DOOR_CLOSED_COLLISION_LAYER_NAME = "WALL-COLLISION-IF-CLOSED";
    private static final String FIRST_BOSS_DOOR_ANIMATION_GROUP_NAME = "DOOR-FirstBoss-Double-Animation-1";
    private static final String FIRST_BOSS_DOOR_FRONT_ANIMATION_GROUP_NAME = "DOOR-FirstBoss-Front-Double-Animation-1 ALWAYS-INFRONT";
    private static final String FIRST_BOSS_DOOR_FRONT_ANIMATION_GROUP_NAME_ALT = "DOOR-FirstBoss-Front-Double-Animation-1";
    private static final String FIRST_BOSS_DOOR_TRIGGER_LAYER_NAME = "Door-Closed(open-after-defeating)";
    private static final String BOSS_1_DEFEAT_DOOR_ANIMATION_GROUP_NAME = "DOOR2-Front-Double-Animation-1";
    private static final String BOSS_1_GROUP_LAYER_NAME = "BOSS-1";
    private static final String BOSS_2_GROUP_LAYER_NAME = "BOSS-2";
    private static final String BOSS_FIGHT_ZONE_LAYER_NAME = "Fight-Zone";
    private static final String BOSS_WALKABLE_LAYER_NAME = "WALKABLE";
    private static final String BOSS_PHASE_2_BOSS_LAYER_NAME = "Phase-2-Boss";
    private static final String BOSS_ARENA_PHASE_2_DEMON_SPAWN_LAYER_NAME = "Boss-Spawn";
    private static final String GOLEM_1_GROUP_LAYER_NAME = "Golem-1";
    private static final String GOLEM_1_OBJECT_LAYER_NAME = "Golem-1";
    private static final String GOLEM_1_OBJECT_LAYER_NAME_ALT = "Golem1";
    private static final String GOLEM_1_OBJECT_LAYER_NAME_ALT_2 = "Golem1 object layer";
    private static final String GOLEM_1_OBJECT_LAYER_NAME_ALT_3 = "Golem-1 object layer";
    private static final String GOLEM_1_WALKABLE_LAYER_NAME = "WALKABLE-Golem";
    private static final String CHEST_REWARD_GROUP_LAYER_NAME = "CHEST-Reward";
    private static final String CHEST_REWARD_OBJECT_LAYER_NAME = "Chest-Reward";
    private static final String ROOM1_GROUP_LAYER_NAME = "Room-1";
    private static final String ROOM1_BATS_SPAWN_LAYER_NAME = "Bats-Spawn";
    private static final float ROOM1_DOOR_FRAME_DURATION_SECONDS = 0.1f;
    private static final float ROOM1_DOOR_TRIGGER_PAUSE_SECONDS = 0.2f;
    private static final float FIRST_BOSS_DOOR_FRAME_DURATION_SECONDS = 0.1f;
    private static final float FIRST_BOSS_DOOR_TRIGGER_PAUSE_SECONDS = 0.2f;
    private static final float BOSS_1_DEFEAT_DOOR_FRAME_DURATION_SECONDS = 0.1f;
    private static final float BOSS_1_DEFEAT_DOOR_OPEN_DELAY_SECONDS = 4f;
    private static final float BOSS_1_INTRO_STEP_DOWN_SECONDS = 0.6f;
    private static final float BOSS_1_INTRO_IDLE_SECONDS = 5f;
    private static final float BOSS_1_INTRO_CAMERA_FOCUS_SECONDS = 3f;
    private static final float BOSS_1_INTRO_CAMERA_RETURN_SECONDS = 1f;
    private static final float BOSS_FIGHT_ZONE_DARK_MAX_ALPHA = 0.95f;
    private static final float BOSS_FIGHT_ZONE_DARK_TRANSITION_SECONDS = 3f;
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
    private static final float POST_DASH_MOVEMENT_LOCK_SECONDS = 0.2f;
    private static final float DASH_ATTACK_TELEPORT_TILES = 4f;
    private static final float DEFAULT_TILE_SIZE = 16f;
    private static final float TELEPORT_CAMERA_SMOOTH_DURATION = 0.25f;
    private static final float TELEPORT_CAMERA_SMOOTH_SPEED = 14f;
    private static final float PLAYER_DRAW_SCALE = 1.0296f;
    private static final float PLAYER_SCALE_MULTIPLIER = 1.25f;
    private static final float PLAYER_DRAW_SCALE_X = 0.7f;
    private static final float PLAYER_DRAW_SCALE_Y = 1.5f;
    private static final float DEFAULT_ZOOM = 0.336f; // fixed zoom level (20% zoomed out)
    private static final float BOSS_ARENA_PHASE_2_ATTACK_ZOOM_MULTIPLIER = 1.5f;
    private static final float BOSS_ARENA_PHASE_2_ATTACK_ZOOM_SMOOTH_SPEED = 8f;
    private static final float DEFAULT_MAP_PADDING = 160f;
    private static final int IDLE_FRAMES = 12;
    private static final int RUN_FRAMES = 8;
    private static final int ATTACK1_FRAMES = 3;
    private static final int DASH_ATTACK_FRAMES = 3;
    private static final int SPETIAL_DASH_FRAMES = 5;
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
    private static final int SPETIAL_DASH_TRIM_LEFT = 106;
    private static final int SPETIAL_DASH_TRIM_TOP = 41;
    private static final int SPETIAL_DASH_TRIM_WIDTH = 101;
    private static final int SPETIAL_DASH_TRIM_HEIGHT = 41;
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
    private static final float PLAYER_HIT_FRAME_DURATION = 0.09f;
    private static final float PLAYER_HAZARD_HIT_REACTION_SECONDS = 0.8f;
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
    private static final String BAT_WAKEUP_TEXTURE_PATH = "images/bat/Bat-WakeUp.png";
    private static final String BAT_IDLE_FLY_TEXTURE_PATH = "images/bat/Bat-IdleFly.png";
    private static final String BAT_RUN_TEXTURE_PATH = "images/bat/Bat-Run.png";
    private static final String BAT_ATTACK1_TEXTURE_PATH = "images/bat/Bat-Attack1.png";
    private static final String BAT_ATTACK2_TEXTURE_PATH = "images/bat/Bat-Attack2.png";
    private static final String BAT_HURT_TEXTURE_PATH = "images/bat/Bat-Hurt.png";
    private static final String BAT_DIE_TEXTURE_PATH = "images/bat/Bat-Die.png";
    private static final int BAT_SLEEP_FRAMES = 3;
    private static final int BAT_WAKEUP_FRAMES = 16;
    private static final int BAT_IDLE_FLY_FRAMES = 9;
    private static final int BAT_RUN_FRAMES = 8;
    private static final int BAT_ATTACK1_FRAMES = 8;
    private static final int BAT_ATTACK2_FRAMES = 11;
    private static final int BAT_HURT_FRAMES = 5;
    private static final int BAT_DIE_FRAMES = 12;
    private static final int BAT_FRAME_SIZE = 64;
    private static final float BAT_SLEEP_FRAME_DURATION = 0.18f;
    private static final float BAT_WAKEUP_FRAME_DURATION = 0.08f;
    private static final float BAT_IDLE_FLY_FRAME_DURATION = 0.1f;
    private static final float BAT_RUN_FRAME_DURATION = 0.1f;
    private static final float BAT_ATTACK_FRAME_DURATION = 0.08f;
    private static final float BAT_HURT_FRAME_DURATION = 0.08f;
    private static final float BAT_DIE_FRAME_DURATION = 0.08f;
    private static final float BAT_SLEEP_DRAW_SCALE = 0.5f;
    private static final float BAT_IDLE_FLY_TO_CHASE_SECONDS = 0.5f;
    private static final float BAT_CHASE_SPEED = 45f;
    private static final float BAT_CHASE_STOP_DISTANCE = 2f;
    private static final float BAT_ATTACK_RANGE = 18f;
    private static final float BAT_PLAYER_HIT_RANGE = 26f;
    private static final float BAT_PLAYER_HIT_COOLDOWN_SECONDS = 0.2f;
    private static final float BAT_MAX_HEALTH = 100f;
    private static final float BAT_HIT_DAMAGE = 15f;
    private static final float BAT_HURT_SFX_VOLUME = 0.85f;
    private static final float BAT_HP_BAR_WIDTH = 16f;
    private static final float BAT_HP_BAR_HEIGHT = 2f;
    private static final float BAT_HP_BAR_OFFSET_Y = 8f;
    private static final float BAT_SPAWN_FORWARD_OFFSET = 20f;
    private static final float BAT_SPAWN_GROUND_OFFSET = -1f;
    private static final String EVIL_WIZARD_IDLE_TEXTURE_PATH = "images/enemies/evil-wizard-2/Idle.png";
    private static final String EVIL_WIZARD_RUN_TEXTURE_PATH = "images/enemies/evil-wizard-2/Run.png";
    private static final String EVIL_WIZARD_ATTACK1_TEXTURE_PATH = "images/enemies/evil-wizard-2/Attack1.png";
    private static final String EVIL_WIZARD_ATTACK2_TEXTURE_PATH = "images/enemies/evil-wizard-2/Attack2.png";
    private static final String EVIL_WIZARD_HURT_TEXTURE_PATH = "images/enemies/evil-wizard-2/Take hit.png";
    private static final String EVIL_WIZARD_DIE_TEXTURE_PATH = "images/enemies/evil-wizard-2/Death.png";
    private static final float EVIL_WIZARD_IDLE_FRAME_DURATION = 0.12f;
    private static final float EVIL_WIZARD_RUN_FRAME_DURATION = 0.1f;
    private static final float EVIL_WIZARD_ATTACK_FRAME_DURATION = 0.09f;
    private static final float EVIL_WIZARD_HURT_FRAME_DURATION = 0.09f;
    private static final float EVIL_WIZARD_DIE_FRAME_DURATION = 0.09f;
    private static final float EVIL_WIZARD_DRAW_SCALE = 1.3f;
    private static final float EVIL_WIZARD_HITBOX_WIDTH_SCALE = 0.125f;
    private static final float EVIL_WIZARD_HITBOX_HEIGHT_SCALE = 0.225f;
    private static final float EVIL_WIZARD_HITBOX_DOWN_OFFSET_TILES = 1.5f;
    private static final float EVIL_WIZARD_DEPTH_BASIS_OFFSET_TILES = 5f;
    private static final float BOSS_1_DEPTH_BASIS_ADJUST_TILES = -1.6f;
    private static final float BOSS_2_SHADOW_OFFSET_ADJUST_TILES = -1.5f;
    private static final float EVIL_WIZARD_SHADOW_WIDTH_TILES = 2.4f;
    private static final float EVIL_WIZARD_SHADOW_HEIGHT_TILES = 0.8f;
    private static final float EVIL_WIZARD_SHADOW_DOWN_OFFSET_TILES = 0.1f;
    private static final float EVIL_WIZARD_HP_BAR_DOWN_OFFSET_TILES = 10f;
    private static final float EVIL_WIZARD_CHASE_SPEED = 42f;
    private static final float EVIL_WIZARD_CHASE_STOP_DISTANCE = 4f;
    private static final float EVIL_WIZARD_ATTACK_RANGE = DEFAULT_TILE_SIZE * 7f;
    private static final float EVIL_WIZARD_ATTACK_INTERVAL_SECONDS = 5f;
    private static final float EVIL_WIZARD_ATTACK_SFX_VOLUME = 1f;
    private static final float EVIL_WIZARD_PRE_EXPLOSION_WARNING_SECONDS = 4.4f;
    private static final float EVIL_WIZARD_PRE_EXPLOSION_SFX_VOLUME = 1f;
    private static final float EVIL_WIZARD_PRE_EXPLOSION_SFX_START_SECONDS = 3.4f;
    private static final float EVIL_WIZARD_MAX_HEALTH = 100f;
    private static final float EVIL_WIZARD_HIT_DAMAGE = 15f;
    private static final float EVIL_WIZARD_PLAYER_HIT_COOLDOWN_SECONDS = 0.2f;
    private static final float EVIL_WIZARD_ATTACK_HAZARD_DAMAGE = EVIL_WIZARD_HIT_DAMAGE;
    private static final float EVIL_WIZARD_ATTACK_HAZARD_EXTRA_SECONDS = 2f;
    private static final float EVIL_WIZARD_ATTACK_HAZARD_EXPLOSION_WINDOW_SECONDS = 0.45f;
    private static final float EVIL_WIZARD_ATTACK_HAZARD_WARNING_LEAD_BUFFER_SECONDS = 0.05f;
    private static final float EVIL_WIZARD_ATTACK_OVERLAY_TRANSITION_SECONDS = 1f;
    private static final int EVIL_WIZARD_SAFE_CIRCLE_MIN_COUNT = 3;
    private static final int EVIL_WIZARD_SAFE_CIRCLE_MAX_COUNT = 4;
    private static final float EVIL_WIZARD_SAFE_CIRCLE_RADIUS_TILES = 1.008f;
    private static final float EVIL_WIZARD_SAFE_CIRCLE_MIN_SEPARATION_TILES = 2.2f;
    private static final float EVIL_WIZARD_ATTACK_OVERLAY_ALPHA = 0.92f;
    private static final float EVIL_WIZARD_ATTACK_OVERLAY_R = 0.22f;
    private static final float EVIL_WIZARD_ATTACK_OVERLAY_G = 0.03f;
    private static final float EVIL_WIZARD_ATTACK_OVERLAY_B = 0.36f;
    private static final float EVIL_WIZARD_SAFE_CIRCLE_ALPHA = 0.62f;
    private static final float EVIL_WIZARD_SAFE_CIRCLE_R = 0.88f;
    private static final float EVIL_WIZARD_SAFE_CIRCLE_G = 0.96f;
    private static final float EVIL_WIZARD_SAFE_CIRCLE_B = 1f;
    private static final float EVIL_WIZARD_SAFE_CIRCLE_HOLE_STRIP_HEIGHT = 4f;
    private static final String GOLEM_1_IDLE_TEXTURE_ABSOLUTE_PATH =
            "C:/Users/user/Desktop/PixelRPG-Game/DSA GAME/ENEMY-Assets/Golem_1/Orange/White_Swoosh_VFX/Golem_1_idle.png";
    private static final String GOLEM_1_WALK_TEXTURE_ABSOLUTE_PATH =
            "C:/Users/user/Desktop/PixelRPG-Game/DSA GAME/ENEMY-Assets/Golem_1/Orange/White_Swoosh_VFX/Golem_1_walk.png";
    private static final String GOLEM_1_ATTACK_TEXTURE_ABSOLUTE_PATH =
            "C:/Users/user/Desktop/PixelRPG-Game/DSA GAME/ENEMY-Assets/Golem_1/Orange/White_Swoosh_VFX/Golem_1_attack.png";
    private static final String GOLEM_1_HURT_TEXTURE_ABSOLUTE_PATH =
            "C:/Users/user/Desktop/PixelRPG-Game/DSA GAME/ENEMY-Assets/Golem_1/Orange/White_Swoosh_VFX/Golem_1_hurt.png";
    private static final String GOLEM_1_DIE_TEXTURE_ABSOLUTE_PATH =
            "C:/Users/user/Desktop/PixelRPG-Game/DSA GAME/ENEMY-Assets/Golem_1/Orange/White_Swoosh_VFX/Golem_1_die.png";
    private static final String GOLEM_1_EXPLOSION_TEXTURE_ABSOLUTE_PATH =
            "C:/Users/user/Desktop/PixelRPG-Game/DSA GAME/ENEMY-Assets/Golem_1/Orange/White_Swoosh_VFX/Golem_explosion.png";
    private static final String BOSS_ARENA_PHASE_2_DEMON_IDLE_FRAMES_DIR_ABSOLUTE_PATH =
            "C:/Users/user/Desktop/PixelRPG-Game/DSA GAME/ENEMY-Assets/boss_demon_slime_FREE_v1.0/individual sprites/01_demon_idle";
    private static final String BOSS_ARENA_PHASE_2_DEMON_WALK_FRAMES_DIR_ABSOLUTE_PATH =
            "C:/Users/user/Desktop/PixelRPG-Game/DSA GAME/ENEMY-Assets/boss_demon_slime_FREE_v1.0/individual sprites/02_demon_walk";
    private static final String BOSS_ARENA_PHASE_2_DEMON_ATTACK_FRAMES_DIR_ABSOLUTE_PATH =
            "C:/Users/user/Desktop/PixelRPG-Game/DSA GAME/ENEMY-Assets/boss_demon_slime_FREE_v1.0/individual sprites/03_demon_cleave";
    private static final String BOSS_ARENA_PHASE_2_DEMON_HURT_FRAMES_DIR_ABSOLUTE_PATH =
            "C:/Users/user/Desktop/PixelRPG-Game/DSA GAME/ENEMY-Assets/boss_demon_slime_FREE_v1.0/individual sprites/04_demon_take_hit";
    private static final String BOSS_ARENA_PHASE_2_DEMON_IDLE_TEXTURE_ABSOLUTE_PATH =
            "C:/Users/user/Desktop/PixelRPG-Game/DSA GAME/ENEMY-Assets/boss_demon_slime_FREE_v1.0/spritesheets/demon_slime_FREE_v1.0_288x160_spritesheet.png";
    private static final String CHEST_IDLE_TEXTURE_ABSOLUTE_PATH =
            "C:/Users/user/Desktop/PixelRPG-Game/DSA GAME/ORIGINAL ASSETS/The dungeon (2)/The dungeon/Chest/Skull_chest_idle.png";
    private static final String CHEST_OPEN_TEXTURE_ABSOLUTE_PATH =
            "C:/Users/user/Desktop/PixelRPG-Game/DSA GAME/ORIGINAL ASSETS/The dungeon (2)/The dungeon/Chest/Skull_chest_open.png";
    private static final float CHEST_IDLE_FRAME_DURATION = 0.12f;
    private static final float CHEST_OPEN_FRAME_DURATION = 0.12f;
    private static final float CHEST_OPEN_TRIGGER_RANGE_TILES = 5f;
    private static final float CHEST_DEFAULT_DRAW_SIZE = 32f;
    private static final float CHEST_DRAW_SCALE = 2f;
    private static final int GOLEM_1_IDLE_FRAMES = 8;
    private static final int GOLEM_1_WALK_FRAMES = 10;
    private static final int GOLEM_1_ATTACK_FRAMES = 11;
    private static final int GOLEM_1_HURT_FRAMES = 4;
    private static final int GOLEM_1_EXPLOSION_FRAMES = 9;
    private static final int GOLEM_1_FRAME_WIDTH = 90;
    private static final int GOLEM_1_FRAME_HEIGHT = 64;
    private static final int GOLEM_1_EXPLOSION_FRAME_WIDTH = 32;
    private static final int GOLEM_1_EXPLOSION_FRAME_HEIGHT = 32;
    private static final float GOLEM_1_IDLE_FRAME_DURATION = 0.12f;
    private static final float GOLEM_1_WALK_FRAME_DURATION = 0.1f;
    private static final float GOLEM_1_ATTACK_FRAME_DURATION = 0.09f;
    private static final float GOLEM_1_HURT_FRAME_DURATION = 0.08f;
    private static final float GOLEM_1_DIE_FRAME_DURATION = 0.08f;
    private static final float GOLEM_1_EXPLOSION_FRAME_DURATION = 0.04f;
    private static final float GOLEM_1_DRAW_SCALE = 2f;
    private static final float GOLEM_1_HITBOX_WIDTH_SCALE = 0.6f;
    private static final float GOLEM_1_HITBOX_HEIGHT_SCALE = 0.5f;
    private static final float GOLEM_1_HITBOX_DOWN_OFFSET_TILES = 2.1f;
    private static final float GOLEM_1_EXPLOSION_DRAW_SCALE = 2f;
    private static final float GOLEM_1_SHADOW_WIDTH_TILES = 3f;
    private static final float GOLEM_1_SHADOW_HEIGHT_TILES = 1f;
    private static final float GOLEM_1_SHADOW_DOWN_OFFSET_TILES = 4f;
    private static final float GOLEM_1_SHADOW_ALPHA = 0.7f;
    private static final float GOLEM_1_CHASE_SPEED = 32f;
    private static final float GOLEM_1_CHASE_STOP_DISTANCE = 3f;
    private static final float GOLEM_1_ATTACK_RANGE = DEFAULT_TILE_SIZE * 15f;
    private static final float GOLEM_1_MAX_HEALTH = 100f;
    private static final float GOLEM_1_HIT_DAMAGE = EVIL_WIZARD_HIT_DAMAGE;
    private static final float GOLEM_1_HP_BAR_DOWN_OFFSET_TILES = 3.5f;
    private static final float GOLEM_1_ATTACK_COOLDOWN_SECONDS = 3f;
    private static final float GOLEM_1_EXPLOSION_DELAY_SECONDS = 0.7f;
    private static final float GOLEM_1_EXPLOSION_CHAIN_STEP_TILES = 2.5f;
    private static final float GOLEM_1_EXPLOSION_CHAIN_MAX_DISTANCE_TILES = 15f;
    private static final float GOLEM_1_EXPLOSION_CHAIN_STEP_DELAY_SECONDS = 0.3f;
    private static final float GOLEM_1_EXPLOSION_SOURCE_OFFSET_Y_TILES = -2f;
    private static final float GOLEM_1_EXPLOSION_DAMAGE = EVIL_WIZARD_HIT_DAMAGE;
    private static final float GOLEM_1_EXPLOSION_HIT_RADIUS_TILES = 2.4f;
    private static final float GOLEM_1_EXPLOSION_CAMERA_SHAKE_DURATION_SECONDS = 0.16f;
    private static final float GOLEM_1_EXPLOSION_CAMERA_SHAKE_PER_BURST = 1.4f;
    private static final float GOLEM_1_EXPLOSION_CAMERA_SHAKE_MAX = 3.2f;
    private static final float GOLEM_1_EXPLOSION_CAMERA_SHAKE_DAMPING_PER_SECOND = 12f;
    private static final float GOLEM_1_EXPLOSION_SFX_VOLUME = 1f;
    private static final float GOLEM_1_PLAYER_HIT_COOLDOWN_SECONDS = 0.2f;
    private static final int BOSS_ARENA_PHASE_2_DEMON_IDLE_FRAMES = 6;
    private static final int BOSS_ARENA_PHASE_2_DEMON_FRAME_WIDTH = 48;
    private static final int BOSS_ARENA_PHASE_2_DEMON_FRAME_HEIGHT = 32;
    private static final float BOSS_ARENA_PHASE_2_DEMON_IDLE_FRAME_DURATION = 0.12f;
    private static final float BOSS_ARENA_PHASE_2_DEMON_WALK_FRAME_DURATION = 0.09f;
    private static final float BOSS_ARENA_PHASE_2_DEMON_ATTACK_FRAME_DURATION = 0.08f;
    private static final float BOSS_ARENA_PHASE_2_DEMON_HURT_FRAME_DURATION = 0.08f;
    private static final float BOSS_ARENA_PHASE_2_DEMON_DRAW_SCALE = 2f;
    private static final float BOSS_ARENA_PHASE_2_DEMON_HITBOX_WIDTH_SCALE = 0.189f;
    private static final float BOSS_ARENA_PHASE_2_DEMON_HITBOX_HEIGHT_SCALE = 0.45f;
    private static final float BOSS_ARENA_PHASE_2_DEMON_HITBOX_RIGHT_OFFSET_TILES = 0.3f;
    private static final float BOSS_ARENA_PHASE_2_DEMON_HITBOX_DOWN_OFFSET_TILES = 5.8f;
    private static final float BOSS_ARENA_PHASE_2_DEMON_SPAWN_OFFSET_X_TILES = -5f;
    private static final float BOSS_ARENA_PHASE_2_DEMON_SPAWN_OFFSET_Y_TILES = 4f;
    private static final float BOSS_ARENA_PHASE_2_DEMON_CHASE_SPEED = 30f;
    private static final float BOSS_ARENA_PHASE_2_DEMON_CHASE_STOP_DISTANCE_TILES = 1.5f;
    private static final float BOSS_ARENA_PHASE_2_DEMON_ATTACK_RANGE = DEFAULT_TILE_SIZE * 20f;
    private static final float BOSS_ARENA_PHASE_2_DEMON_PLAYER_HIT_COOLDOWN_SECONDS = 0.2f;
    private static final float BOSS_ARENA_PHASE_2_DETECTION_CENTER_DOWN_OFFSET_TILES = 7.5f;
    private static final float ENEMY_HITBOX_DEBUG_FILL_ALPHA = 0.26f;
    private static final float ENEMY_HITBOX_DEBUG_OUTLINE_ALPHA = 0.95f;
    private static final float ENEMY_HITBOX_DEBUG_OUTLINE_THICKNESS = 1.5f;
    private static final float PLAYER_ATTACK_AREA_ALPHA = 0.26f;
    private static final float BOSS_ARENA_PHASE_2_DETECTION_DEBUG_ALPHA = 0.16f;
    private static final float BOSS_ARENA_PHASE_2_DETECTION_DEBUG_LINE_ALPHA = 0.75f;
    private static final float BOSS_ARENA_PHASE_2_DETECTION_DEBUG_LINE_THICKNESS = 1.5f;
    private static final float BOSS_ARENA_PHASE_2_DETECTION_CHARACTER_MARKER_RADIUS_TILES = 0.7f;
    private static final float BOSS_ARENA_PHASE_2_DETECTION_DEMON_MARKER_RADIUS_TILES = 0.45f;
    private static final float PLAYER_DASH_ATTACK_HIT_HEIGHT_SCALE = 0.5f;
    private static final float PLAYER_LEFT_CLICK_HIT_RADIUS_SCALE = 0.8f;
    private static final float PLAYER_E_ATTACK_HIT_RADIUS_SCALE = 0.8f;
    private static final float PLAYER_ATTACK_MELEE_R = 1f;
    private static final float PLAYER_ATTACK_MELEE_G = 0.82f;
    private static final float PLAYER_ATTACK_MELEE_B = 0.18f;
    private static final float PLAYER_ATTACK_DASH_E_R = 0.18f;
    private static final float PLAYER_ATTACK_DASH_E_G = 0.94f;
    private static final float PLAYER_ATTACK_DASH_E_B = 1f;
    private static final float PLAYER_ATTACK_DASH_RIGHT_R = 1f;
    private static final float PLAYER_ATTACK_DASH_RIGHT_G = 0.34f;
    private static final float PLAYER_ATTACK_DASH_RIGHT_B = 0.92f;
    private static final String PLAYER_HIT_TEXTURE_INTERNAL_PATH = "images/huntress/14 - hit.png";
    private static final String PLAYER_HIT_TEXTURE_ABSOLUTE_PATH =
            "C:/Users/user/Desktop/PixelRPG-Game/SHADOW Series - The Blind Huntress/Sprite Sheet/14 - hit.png";
    private static final String EVIL_WIZARD_ATTACK1_SFX_ABSOLUTE_PATH =
            "C:/Users/user/Desktop/PixelRPG-Game/DSA GAME/ENEMY-Assets/EVil Wizard 2/wizard1.mp3";
    private static final String EVIL_WIZARD_ATTACK2_SFX_ABSOLUTE_PATH =
            "C:/Users/user/Desktop/PixelRPG-Game/DSA GAME/ENEMY-Assets/EVil Wizard 2/wizard2.mp3";
    private static final String EVIL_WIZARD_PRE_EXPLOSION_SFX_ABSOLUTE_PATH =
            "C:/Users/user/Desktop/PixelRPG-Game/DSA GAME/ENEMY-Assets/EVil Wizard 2/wizard-explosion.mp3";
    private static final String GOLEM_1_EXPLOSION_SFX_ABSOLUTE_PATH =
            "C:/Users/user/Desktop/PixelRPG-Game/DSA GAME/ENEMY-Assets/Golem_1/Orange/White_Swoosh_VFX/Golem_explosion.mp3";

    private enum MapTransitionPhase {
        NONE,
        FADING_OUT,
        FADING_IN
    }

    private enum BatAnimationState {
        SLEEPING,
        WAKING_UP,
        IDLE_FLY,
        CHASING,
        ATTACKING,
        HURT,
        DIE
    }

    private enum EvilWizardAnimationState {
        IDLE,
        RUN,
        ATTACKING,
        HURT,
        DIE
    }

    private enum Boss1IntroPhase {
        NONE,
        STEP_DOWN,
        HOLD_IDLE,
        FOCUS_WIZARD,
        RETURN_TO_PLAYER
    }

    private static final class BatInstance {
        final Vector2 position = new Vector2();
        BatAnimationState state = BatAnimationState.SLEEPING;
        BatAnimationState stateBeforeHurt = BatAnimationState.SLEEPING;
        float stateTime = 0f;
        float playerHitCooldownTimer = 0f;
        boolean useAttack1Next = true;
        boolean usingAttack1 = true;
        float maxHealth = BAT_MAX_HEALTH;
        float health = BAT_MAX_HEALTH;
        int lastDashAttackHitId = -1;
    }

    private static final class EvilWizardInstance {
        final Vector2 position = new Vector2();
        Rectangle zoneBounds;
        float depthBasisOffsetTiles = EVIL_WIZARD_DEPTH_BASIS_OFFSET_TILES;
        float shadowDownOffsetTiles = EVIL_WIZARD_SHADOW_DOWN_OFFSET_TILES;
        float stateTime = 0f;
        EvilWizardAnimationState state = EvilWizardAnimationState.IDLE;
        EvilWizardAnimationState stateBeforeHurt = EvilWizardAnimationState.IDLE;
        boolean facingLeft = false;
        boolean useAttack1Next = true;
        boolean usingAttack1 = true;
        float maxHealth = EVIL_WIZARD_MAX_HEALTH;
        float health = EVIL_WIZARD_MAX_HEALTH;
        float playerHitCooldownTimer = 0f;
        float attackCooldownTimer = 0f;
        int lastDashAttackHitId = -1;
    }

    private static final class Golem1Instance {
        final Vector2 position = new Vector2();
        final Array<Golem1ExplosionInstance> explosions = new Array<>();
        Rectangle zoneBounds;
        float stateTime = 0f;
        float chaseSpeed = GOLEM_1_CHASE_SPEED;
        float maxHealth = GOLEM_1_MAX_HEALTH;
        float health = GOLEM_1_MAX_HEALTH;
        float attackCooldownTimer = 0f;
        float playerHitCooldownTimer = 0f;
        float attackStateTime = 0f;
        float hurtStateTime = 0f;
        float dieStateTime = 0f;
        boolean facingLeft = false;
        boolean moving = false;
        boolean attacking = false;
        boolean hurting = false;
        boolean dying = false;
        int lastDashAttackHitId = -1;
    }

    private static final class Golem1ExplosionInstance {
        final Vector2 position = new Vector2();
        float delayTimer = 0f;
        float stateTime = 0f;
        boolean active = false;
    }

    private static final class RewardChestInstance {
        final Vector2 position = new Vector2();
        float drawWidth = CHEST_DEFAULT_DRAW_SIZE;
        float drawHeight = CHEST_DEFAULT_DRAW_SIZE;
        float stateTime = 0f;
        float openStateTime = 0f;
        boolean opening = false;
        boolean opened = false;
    }

    private static final class BossArenaPhase2DemonInstance {
        final Vector2 position = new Vector2();
        float stateTime = 0f;
        float attackStateTime = 0f;
        float hurtStateTime = 0f;
        float playerHitCooldownTimer = 0f;
        boolean facingLeft = false;
        boolean moving = false;
        boolean attacking = false;
        boolean hurting = false;
        int lastDashAttackHitId = -1;
    }

    private static final class NpcFrameEntry {
        final int frameNumber;
        final MapLayer layer;

        NpcFrameEntry(int frameNumber, MapLayer layer) {
            this.frameNumber = frameNumber;
            this.layer = layer;
        }
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
    private Texture spetialDashTex;
    private Texture vignetteTex;
    private Texture dayNightOverlayTex;
    private Texture playerAuraTex;
    private HudRenderer hudRenderer;
    private Texture jumpUpAttackTex;
    private Texture jumpDownAttackTex;
    private Texture idleUpAttackTex;
    private Texture playerHitTex;
    private Texture batSleepTex;
    private Texture batWakeUpTex;
    private Texture batIdleFlyTex;
    private Texture batRunTex;
    private Texture batAttack1Tex;
    private Texture batAttack2Tex;
    private Texture batHurtTex;
    private Texture batDieTex;
    private Texture evilWizardIdleTex;
    private Texture evilWizardRunTex;
    private Texture evilWizardAttack1Tex;
    private Texture evilWizardAttack2Tex;
    private Texture evilWizardHurtTex;
    private Texture evilWizardDieTex;
    private Texture golem1IdleTex;
    private Texture golem1WalkTex;
    private Texture golem1AttackTex;
    private Texture golem1HurtTex;
    private Texture golem1DieTex;
    private Texture golem1ExplosionTex;
    private Texture bossArenaPhase2DemonIdleTex;
    private Texture chestIdleTex;
    private Texture chestOpenTex;
    private Sound attack1Sfx;
    private Sound attack3Sfx;
    private Sound attackSpecialSfx;
    private Sound batHurtSfx;
    private Sound evilWizardAttack1Sfx;
    private Sound evilWizardAttack2Sfx;
    private Sound golem1ExplosionSfx;
    private Music evilWizardPreExplosionSfx;
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
    private Animation<TextureRegion> spetialDashAnim;
    private Animation<TextureRegion> jumpUpAttackAnim;
    private Animation<TextureRegion> jumpDownAttackAnim;
    private Animation<TextureRegion> idleUpAttackAnim;
    private Animation<TextureRegion> playerHitAnim;
    private Animation<TextureRegion> batSleepAnim;
    private Animation<TextureRegion> batWakeUpAnim;
    private Animation<TextureRegion> batIdleFlyAnim;
    private Animation<TextureRegion> batRunAnim;
    private Animation<TextureRegion> batAttack1Anim;
    private Animation<TextureRegion> batAttack2Anim;
    private Animation<TextureRegion> batHurtAnim;
    private Animation<TextureRegion> batDieAnim;
    private Animation<TextureRegion> evilWizardIdleAnim;
    private Animation<TextureRegion> evilWizardRunAnim;
    private Animation<TextureRegion> evilWizardAttack1Anim;
    private Animation<TextureRegion> evilWizardAttack2Anim;
    private Animation<TextureRegion> evilWizardHurtAnim;
    private Animation<TextureRegion> evilWizardDieAnim;
    private Animation<TextureRegion> golem1IdleAnim;
    private Animation<TextureRegion> golem1WalkAnim;
    private Animation<TextureRegion> golem1AttackAnim;
    private Animation<TextureRegion> golem1HurtAnim;
    private Animation<TextureRegion> golem1DieAnim;
    private Animation<TextureRegion> golem1ExplosionAnim;
    private Animation<TextureRegion> bossArenaPhase2DemonIdleAnim;
    private Animation<TextureRegion> bossArenaPhase2DemonWalkAnim;
    private Animation<TextureRegion> bossArenaPhase2DemonAttackAnim;
    private Animation<TextureRegion> bossArenaPhase2DemonHurtAnim;
    private Animation<TextureRegion> chestIdleAnim;
    private Animation<TextureRegion> chestOpenAnim;
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
    private final Array<BatInstance> bats = new Array<>();
    private final Array<EvilWizardInstance> evilWizards = new Array<>();
    private final Array<Golem1Instance> golem1s = new Array<>();
    private final Array<BossArenaPhase2DemonInstance> bossArenaPhase2Demons = new Array<>();
    private final Array<Texture> bossArenaPhase2DemonIdleFrameTextures = new Array<>();
    private final Array<Texture> bossArenaPhase2DemonWalkFrameTextures = new Array<>();
    private final Array<Texture> bossArenaPhase2DemonAttackFrameTextures = new Array<>();
    private final Array<Texture> bossArenaPhase2DemonHurtFrameTextures = new Array<>();
    private final Array<RewardChestInstance> rewardChests = new Array<>();
    private final Array<MapLayer> entranceDoorAnimationFrames = new Array<>();
    private final Array<MapLayer> entranceDoorFrontAnimationFrames = new Array<>();
    private MapLayer entranceDoorTriggerLayer;
    private final Rectangle entranceDoorTriggerBounds = new Rectangle();
    private final Rectangle entranceDoorClosedCollisionBounds = new Rectangle();
    private int entranceDoorAnimationFrameIndex = 0;
    private float entranceDoorAnimationTimer = 0f;
    private int entranceDoorAnimationDirection = 0;
    private int entranceDoorEntrySide = 0;
    private boolean entranceDoorAnimationPlaying = false;
    private boolean entranceDoorAwaitingEntryCross = false;
    private boolean entranceDoorLockedClosed = false;
    private boolean entranceDoorCollisionEnabled = false;
    private final Array<MapLayer> room1DoorAnimationFrames = new Array<>();
    private final Array<MapLayer> room1DoorFrontAnimationFrames = new Array<>();
    private MapLayer room1DoorTriggerLayer;
    private final Rectangle room1DoorClosedCollisionBounds = new Rectangle();
    private int room1DoorAnimationFrameIndex = 0;
    private float room1DoorAnimationTimer = 0f;
    private float room1DoorPauseTimer = 0f;
    private boolean room1DoorClosing = false;
    private boolean room1DoorOpening = false;
    private boolean room1DoorClosed = false;
    private boolean room1DoorCollisionEnabled = false;
    private boolean room1DoorAutoOpenPending = false;
    private boolean room1DoorBattleCompleted = false;
    private final Array<MapLayer> firstBossDoorAnimationFrames = new Array<>();
    private final Array<MapLayer> firstBossDoorFrontAnimationFrames = new Array<>();
    private MapLayer firstBossDoorTriggerLayer;
    private final Rectangle firstBossDoorTriggerBounds = new Rectangle();
    private final Rectangle firstBossDoorClosedCollisionBounds = new Rectangle();
    private int firstBossDoorAnimationFrameIndex = 0;
    private float firstBossDoorAnimationTimer = 0f;
    private float firstBossDoorPauseTimer = 0f;
    private boolean firstBossDoorClosing = false;
    private boolean firstBossDoorClosed = false;
    private boolean firstBossDoorCollisionEnabled = false;
    private boolean firstBossDoorTriggered = false;
    private boolean firstBossDoorTriggerPrimed = false;
    private boolean wasPlayerWithinFirstBossDoorTrigger = false;
    private final Array<MapLayer> boss1DefeatDoorAnimationFrames = new Array<>();
    private int boss1DefeatDoorAnimationFrameIndex = 0;
    private float boss1DefeatDoorAnimationTimer = 0f;
    private float boss1DefeatDoorOpenDelayTimer = 0f;
    private boolean boss1DefeatDoorOpening = false;
    private boolean boss1DefeatDoorOpened = false;
    private boolean boss1DefeatDoorTriggered = false;
    private Boss1IntroPhase boss1IntroPhase = Boss1IntroPhase.NONE;
    private float boss1IntroTimer = 0f;
    private boolean boss1IntroPlayed = false;
    private final Vector2 boss1IntroCameraTarget = new Vector2();
    private MapLayer bossFightZoneLayer;
    private final Rectangle bossFightZoneBounds = new Rectangle();
    private boolean bossFightZoneDarkMode = false;
    private float bossFightZoneDarkTransitionTimer = 0f;
    private MapLayer golemFightZoneLayer;
    private final Rectangle golemFightZoneBounds = new Rectangle();
    private boolean golemFightZoneDarkMode = false;
    private float golemFightZoneDarkTransitionTimer = 0f;
    private float cameraTeleportSmoothTimer = 0f;
    private float postDashMovementLockTimer = 0f;
    private final Vector2 cameraTeleportTarget = new Vector2();
    private boolean cameraTeleportTargetActive = false;
    private float cameraShakeTimer = 0f;
    private float cameraShakeStrength = 0f;
    private final Vector2 cameraShakeOffset = new Vector2();
    private final Vector2 dashAttackPlannedPosition = new Vector2();
    private boolean dashAttackPlannedPositionValid = false;
    private final Vector2 dashAttackSweepStart = new Vector2();
    private final Vector2 dashAttackSweepEnd = new Vector2();
    private float dashAttackSweepLastProgress = 0f;
    private boolean dashAttackSweepActive = false;
    private int dashAttackHitSequence = 0;
    private int activeDashAttackHitId = -1;
    private boolean evilWizardAttackHazardActive = false;
    private final Rectangle evilWizardAttackHazardZoneBounds = new Rectangle();
    private float evilWizardAttackHazardTimer = 0f;
    private float evilWizardAttackHazardElapsedTimer = 0f;
    private boolean evilWizardAttackHazardExplosionTriggered = false;
    private boolean evilWizardPreExplosionWarningPlayed = false;
    private final Array<Vector2> evilWizardSafeCircleCenters = new Array<>();
    private float playerHazardHitReactionTimer = 0f;
    private float playerHazardHitStateTime = 0f;
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
        clearCameraShakeOffsetFromCameraPosition();
        if (postDashMovementLockTimer > 0f) {
            postDashMovementLockTimer = Math.max(0f, postDashMovementLockTimer - delta);
        }
        updatePlayerHazardHitReaction(delta);
        player.stateTime += delta;
        updateBats(delta);
        updateEvilWizards(delta);
        updateGolem1s(delta);
        updateBossArenaPhase2Demons(delta);
        updateRewardChests(delta);
        updateEvilWizardAttackHazard(delta);
        updateDayNightCycle(delta);
        updateMapTransition(delta);
        updateEntranceDoorAnimation(delta);
        updateRoom1DoorAnimation(delta);
        updateFirstBossDoorAnimation(delta);
        updateBoss1DefeatDoorAnimation(delta);
        updateBossFightZoneDarkMode(delta);
        updateGolemFightZoneDarkMode(delta);
        updateBoss1IntroSequence(delta);
        if (isMapTransitionActive()
                || isEntranceDoorInputBlocked()
                || isRoom1DoorInputBlocked()
                || isFirstBossDoorInputBlocked()
                || isBoss1IntroActive()
                || isPlayerHazardHitReactionActive()) {
            if (!isBoss1IntroStepDownActive()) {
                player.moving = false;
            }
        } else {
            updateAttack(delta);
            updateDash(delta);
            if (postDashMovementLockTimer > 0f || isDashAttackAnimationActive()) {
                player.moving = false;
                centerCameraOnPlayer();
            } else {
                handleInput(delta);
            }
        }
        updateBatHurtFromPlayer();
        updateEvilWizardHurtFromPlayer();
        updateGolem1HurtFromPlayer();
        updateBossArenaPhase2DemonHurtFromPlayer();
        updateTorchAnimation(delta);
        updateNpcAnimation(delta);
        hudRenderer.update(delta);
        updateQuestBlur(delta);
        updateBossArenaPhase2AttackZoom(delta);
        clampCamera();
        updateAndApplyExplosionCameraShake(delta);

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
        if (spetialDashTex != null) spetialDashTex.dispose();
        if (vignetteTex != null) vignetteTex.dispose();
        if (dayNightOverlayTex != null) dayNightOverlayTex.dispose();
        if (playerAuraTex != null) playerAuraTex.dispose();
        if (jumpUpAttackTex != null) jumpUpAttackTex.dispose();
        if (jumpDownAttackTex != null) jumpDownAttackTex.dispose();
        if (idleUpAttackTex != null) idleUpAttackTex.dispose();
        if (playerHitTex != null) playerHitTex.dispose();
        if (batSleepTex != null) batSleepTex.dispose();
        if (batWakeUpTex != null) batWakeUpTex.dispose();
        if (batIdleFlyTex != null) batIdleFlyTex.dispose();
        if (batRunTex != null) batRunTex.dispose();
        if (batAttack1Tex != null) batAttack1Tex.dispose();
        if (batAttack2Tex != null) batAttack2Tex.dispose();
        if (batHurtTex != null) batHurtTex.dispose();
        if (batDieTex != null) batDieTex.dispose();
        if (evilWizardIdleTex != null) evilWizardIdleTex.dispose();
        if (evilWizardRunTex != null) evilWizardRunTex.dispose();
        if (evilWizardAttack1Tex != null) evilWizardAttack1Tex.dispose();
        if (evilWizardAttack2Tex != null) evilWizardAttack2Tex.dispose();
        if (evilWizardHurtTex != null) evilWizardHurtTex.dispose();
        if (evilWizardDieTex != null) evilWizardDieTex.dispose();
        if (golem1IdleTex != null) golem1IdleTex.dispose();
        if (golem1WalkTex != null) golem1WalkTex.dispose();
        if (golem1AttackTex != null) golem1AttackTex.dispose();
        if (golem1HurtTex != null) golem1HurtTex.dispose();
        if (golem1DieTex != null) golem1DieTex.dispose();
        if (golem1ExplosionTex != null) golem1ExplosionTex.dispose();
        disposeBossArenaPhase2DemonFrameTextures();
        if (bossArenaPhase2DemonIdleTex != null) bossArenaPhase2DemonIdleTex.dispose();
        if (chestIdleTex != null) chestIdleTex.dispose();
        if (chestOpenTex != null) chestOpenTex.dispose();
        if (attack1Sfx != null) attack1Sfx.dispose();
        if (attack3Sfx != null) attack3Sfx.dispose();
        if (attackSpecialSfx != null) attackSpecialSfx.dispose();
        if (batHurtSfx != null) batHurtSfx.dispose();
        if (evilWizardAttack1Sfx != null) evilWizardAttack1Sfx.dispose();
        if (evilWizardAttack2Sfx != null) evilWizardAttack2Sfx.dispose();
        if (golem1ExplosionSfx != null) golem1ExplosionSfx.dispose();
        if (evilWizardPreExplosionSfx != null) evilWizardPreExplosionSfx.dispose();
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

        TmxMapLoader.Parameters params = new TmxMapLoader.Parameters();
        params.flipY = true; // flip to LibGDX's Y-up world
        try {
            world.map = loadTiledMap(target.getMapPath(), params);
        } catch (RuntimeException primaryError) {
            MapDefinition fallback = world.mapCatalog.getDefaultMap();
            if (fallback != null && !fallback.getId().equals(target.getId())) {
                Gdx.app.error("PixelRpgGame",
                        "Failed to load world.map '" + target.getId() + "' from '" + target.getMapPath()
                                + "'. Falling back to '" + fallback.getId() + "'.",
                        primaryError);
                world.map = loadTiledMap(fallback.getMapPath(), params);
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
        configureEntranceDoorAnimation();
        configureRoom1DoorAnimation();
        configureFirstBossDoorAnimation();
        configureBoss1DefeatDoorAnimation();
        configureBossFightZone();
        configureGolemFightZone();
        extractBoundsFromChunks();
        placePlayerAtSpawn(preferredSpawnLayer);
        configureRoom1BatSpawns();
        configurePhase2BossWizards();
        configureGolem1Spawns();
        configureBossArenaPhase2Demons();
        configureRewardChestSpawns();
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
        entranceDoorAnimationFrames.clear();
        entranceDoorFrontAnimationFrames.clear();
        entranceDoorTriggerLayer = null;
        entranceDoorTriggerBounds.set(0f, 0f, 0f, 0f);
        entranceDoorClosedCollisionBounds.set(0f, 0f, 0f, 0f);
        entranceDoorAnimationFrameIndex = 0;
        entranceDoorAnimationTimer = 0f;
        entranceDoorAnimationDirection = 0;
        entranceDoorEntrySide = 0;
        entranceDoorAnimationPlaying = false;
        entranceDoorAwaitingEntryCross = false;
        entranceDoorLockedClosed = false;
        entranceDoorCollisionEnabled = false;
        room1DoorAnimationFrames.clear();
        room1DoorFrontAnimationFrames.clear();
        room1DoorTriggerLayer = null;
        room1DoorClosedCollisionBounds.set(0f, 0f, 0f, 0f);
        room1DoorAnimationFrameIndex = 0;
        room1DoorAnimationTimer = 0f;
        room1DoorPauseTimer = 0f;
        room1DoorClosing = false;
        room1DoorOpening = false;
        room1DoorClosed = false;
        room1DoorCollisionEnabled = false;
        room1DoorAutoOpenPending = false;
        room1DoorBattleCompleted = false;
        firstBossDoorAnimationFrames.clear();
        firstBossDoorFrontAnimationFrames.clear();
        firstBossDoorTriggerLayer = null;
        firstBossDoorTriggerBounds.set(0f, 0f, 0f, 0f);
        firstBossDoorClosedCollisionBounds.set(0f, 0f, 0f, 0f);
        firstBossDoorAnimationFrameIndex = 0;
        firstBossDoorAnimationTimer = 0f;
        firstBossDoorPauseTimer = 0f;
        firstBossDoorClosing = false;
        firstBossDoorClosed = false;
        firstBossDoorCollisionEnabled = false;
        firstBossDoorTriggered = false;
        firstBossDoorTriggerPrimed = false;
        wasPlayerWithinFirstBossDoorTrigger = false;
        boss1DefeatDoorAnimationFrames.clear();
        boss1DefeatDoorAnimationFrameIndex = 0;
        boss1DefeatDoorAnimationTimer = 0f;
        boss1DefeatDoorOpenDelayTimer = 0f;
        boss1DefeatDoorOpening = false;
        boss1DefeatDoorOpened = false;
        boss1DefeatDoorTriggered = false;
        resetBoss1IntroState();
        bossFightZoneLayer = null;
        bossFightZoneBounds.set(0f, 0f, 0f, 0f);
        bossFightZoneDarkMode = false;
        bossFightZoneDarkTransitionTimer = 0f;
        golemFightZoneLayer = null;
        golemFightZoneBounds.set(0f, 0f, 0f, 0f);
        golemFightZoneDarkMode = false;
        golemFightZoneDarkTransitionTimer = 0f;
        world.backgroundLayerIndices = new int[0];
        world.foregroundLayerIndices = new int[0];
        depthSplitGroupLayerIndex = -1;
        depthSplitMarkerLayer = null;
        clearEvilWizardAttackHazard();
        clearPlayerHazardHitReaction();
        clearSpawnedBat();
        clearSpawnedEvilWizards();
        clearSpawnedGolem1();
        clearSpawnedBossArenaPhase2Demons();
        clearSpawnedRewardChests();
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

        Array<NpcFrameEntry> frameEntries = new Array<>();
        for (MapLayer frameLayer : npcGroup.getLayers()) {
            int frameNumber = parseNpcFrameNumber(frameLayer.getName());
            if (frameNumber <= 0 || frameNumber > 32) {
                continue;
            }
            boolean replaced = false;
            for (int i = 0; i < frameEntries.size; i++) {
                NpcFrameEntry entry = frameEntries.get(i);
                if (entry.frameNumber == frameNumber) {
                    frameEntries.set(i, new NpcFrameEntry(frameNumber, frameLayer));
                    replaced = true;
                    break;
                }
            }
            if (!replaced) {
                frameEntries.add(new NpcFrameEntry(frameNumber, frameLayer));
            }
        }
        bubbleSortNpcFrameEntries(frameEntries);

        for (int i = 0; i < frameEntries.size; i++) {
            NpcFrameEntry entry = frameEntries.get(i);
            entry.layer.setVisible(false);
            world.npcAnimationFrames.add(entry.layer);
        }

        if (world.npcAnimationFrames.size > 0) {
            world.npcAnimationFrames.first().setVisible(true);
        }
    }

    private void bubbleSortNpcFrameEntries(Array<NpcFrameEntry> frameEntries) {
        if (frameEntries == null || frameEntries.size < 2) {
            return;
        }
        for (int end = frameEntries.size - 1; end > 0; end--) {
            boolean swapped = false;
            for (int i = 0; i < end; i++) {
                NpcFrameEntry left = frameEntries.get(i);
                NpcFrameEntry right = frameEntries.get(i + 1);
                if (left.frameNumber > right.frameNumber) {
                    frameEntries.set(i, right);
                    frameEntries.set(i + 1, left);
                    swapped = true;
                }
            }
            if (!swapped) {
                break;
            }
        }
    }

    private boolean isEntranceDoorInputBlocked() {
        return entranceDoorAnimationPlaying && entranceDoorAnimationDirection > 0;
    }

    private boolean isRoom1DoorInputBlocked() {
        return room1DoorPauseTimer > 0f || room1DoorClosing;
    }

    private boolean isFirstBossDoorInputBlocked() {
        return firstBossDoorPauseTimer > 0f || firstBossDoorClosing;
    }

    private boolean isBoss1IntroActive() {
        return boss1IntroPhase != Boss1IntroPhase.NONE;
    }

    private boolean isBoss1IntroStepDownActive() {
        return boss1IntroPhase == Boss1IntroPhase.STEP_DOWN;
    }

    private boolean isPlayerHazardHitReactionActive() {
        return playerHazardHitReactionTimer > 0f;
    }

    private void updatePlayerHazardHitReaction(float delta) {
        if (!isPlayerHazardHitReactionActive()) {
            return;
        }
        float safeDelta = Math.max(0f, delta);
        playerHazardHitReactionTimer = Math.max(0f, playerHazardHitReactionTimer - safeDelta);
        playerHazardHitStateTime += safeDelta;
    }

    private void triggerPlayerHazardHitReaction() {
        playerHazardHitReactionTimer = PLAYER_HAZARD_HIT_REACTION_SECONDS;
        playerHazardHitStateTime = 0f;
        player.attacking = false;
        player.attackTimer = 0f;
        player.dashing = false;
        player.dashTimer = 0f;
        player.dashAttackActive = false;
        player.dashAttackMoveDuration = 0f;
        player.dashAttackFollowupQueued = false;
        player.moving = false;
        activeAttackAnim = null;
        dashAttackPlannedPositionValid = false;
        clearDashAttackSweepState();
    }

    private void clearPlayerHazardHitReaction() {
        playerHazardHitReactionTimer = 0f;
        playerHazardHitStateTime = 0f;
    }

    private void configureEntranceDoorAnimation() {
        entranceDoorAnimationFrames.clear();
        entranceDoorFrontAnimationFrames.clear();
        entranceDoorTriggerLayer = null;
        entranceDoorTriggerBounds.set(0f, 0f, 0f, 0f);
        entranceDoorClosedCollisionBounds.set(0f, 0f, 0f, 0f);
        entranceDoorAnimationFrameIndex = 0;
        entranceDoorAnimationTimer = 0f;
        entranceDoorAnimationDirection = 0;
        entranceDoorEntrySide = 0;
        entranceDoorAnimationPlaying = false;
        entranceDoorAwaitingEntryCross = false;
        entranceDoorLockedClosed = false;
        entranceDoorCollisionEnabled = false;

        if (!MAP_ID_DEMON_CASTLE.equals(world.activeMapId)) {
            return;
        }

        entranceDoorTriggerLayer = findLayerByName(ENTRANCE_DOOR_TRIGGER_LAYER_NAME);
        Rectangle triggerBounds = objectLayerBounds(entranceDoorTriggerLayer);
        if (triggerBounds != null) {
            entranceDoorTriggerBounds.set(triggerBounds);
            entranceDoorClosedCollisionBounds.set(triggerBounds);
        }

        appendDoorFramesFromGroup(findLayerByName(ENTRANCE_DOOR_ANIMATION_GROUP_NAME), entranceDoorAnimationFrames);
        appendDoorFramesFromGroup(findLayerByName(ENTRANCE_DOOR_FRONT_ANIMATION_GROUP_NAME), entranceDoorFrontAnimationFrames);

        if (entranceDoorFrameCount() > 0) {
            entranceDoorAnimationFrameIndex = 0;
            setEntranceDoorFrameVisible(entranceDoorAnimationFrameIndex);
        }
    }

    private void updateEntranceDoorAnimation(float delta) {
        if (!MAP_ID_DEMON_CASTLE.equals(world.activeMapId) || entranceDoorFrameCount() == 0) {
            return;
        }

        if (!entranceDoorAnimationPlaying && !entranceDoorLockedClosed) {
            if (!entranceDoorAwaitingEntryCross && isPlayerWithinObjectLayer(entranceDoorTriggerLayer)) {
                startEntranceDoorOpening();
            } else if (entranceDoorAwaitingEntryCross && hasPlayerEnteredThroughEntranceDoor()) {
                startEntranceDoorClosing();
            }
        }

        if (!entranceDoorAnimationPlaying) {
            return;
        }

        entranceDoorAnimationTimer += Math.max(0f, delta);
        while (entranceDoorAnimationTimer >= ENTRANCE_DOOR_FRAME_DURATION_SECONDS) {
            entranceDoorAnimationTimer -= ENTRANCE_DOOR_FRAME_DURATION_SECONDS;
            int frameCount = entranceDoorFrameCount();
            int nextIndex = entranceDoorAnimationFrameIndex + entranceDoorAnimationDirection;
            if (nextIndex < 0 || nextIndex >= frameCount) {
                entranceDoorAnimationPlaying = false;
                entranceDoorAnimationTimer = 0f;
                if (entranceDoorAnimationDirection > 0) {
                    entranceDoorAwaitingEntryCross = true;
                    entranceDoorAnimationFrameIndex = frameCount - 1;
                } else {
                    entranceDoorAwaitingEntryCross = false;
                    entranceDoorLockedClosed = true;
                    entranceDoorCollisionEnabled = entranceDoorClosedCollisionBounds.width > 0f
                            && entranceDoorClosedCollisionBounds.height > 0f;
                    entranceDoorAnimationFrameIndex = 0;
                }
                break;
            }

            entranceDoorAnimationFrameIndex = nextIndex;
            setEntranceDoorFrameVisible(entranceDoorAnimationFrameIndex);
        }
    }

    private void startEntranceDoorOpening() {
        if (entranceDoorFrameCount() == 0) {
            return;
        }

        entranceDoorAnimationPlaying = true;
        entranceDoorAnimationDirection = 1;
        entranceDoorAnimationFrameIndex = 0;
        entranceDoorAnimationTimer = 0f;
        entranceDoorCollisionEnabled = false;
        entranceDoorLockedClosed = false;
        entranceDoorAwaitingEntryCross = false;
        entranceDoorEntrySide = inferEntranceDoorEntrySide();
        setEntranceDoorFrameVisible(entranceDoorAnimationFrameIndex);
        stopPlayerForDoorAction();
    }

    private void startEntranceDoorClosing() {
        int frameCount = entranceDoorFrameCount();
        if (frameCount == 0) {
            return;
        }

        entranceDoorAnimationPlaying = true;
        entranceDoorAnimationDirection = -1;
        entranceDoorAnimationTimer = 0f;
        entranceDoorCollisionEnabled = false;
        entranceDoorAnimationFrameIndex = Math.min(
                Math.max(entranceDoorAnimationFrameIndex, 0),
                frameCount - 1);
        setEntranceDoorFrameVisible(entranceDoorAnimationFrameIndex);
    }

    private void configureRoom1DoorAnimation() {
        room1DoorAnimationFrames.clear();
        room1DoorFrontAnimationFrames.clear();
        room1DoorTriggerLayer = null;
        room1DoorClosedCollisionBounds.set(0f, 0f, 0f, 0f);
        room1DoorAnimationFrameIndex = 0;
        room1DoorAnimationTimer = 0f;
        room1DoorPauseTimer = 0f;
        room1DoorClosing = false;
        room1DoorOpening = false;
        room1DoorClosed = false;
        room1DoorCollisionEnabled = false;
        room1DoorAutoOpenPending = false;
        room1DoorBattleCompleted = false;

        if (!MAP_ID_DEMON_CASTLE.equals(world.activeMapId)) {
            return;
        }

        room1DoorTriggerLayer = findLayerByName(ROOM1_DOOR_TRIGGER_LAYER_NAME);
        MapLayer room1DoorClosedCollisionLayer = findLayerInGroup(
                ROOM1_DOOR_FRONT_ANIMATION_GROUP_NAME,
                ROOM1_DOOR_CLOSED_COLLISION_LAYER_NAME);
        Rectangle collisionBounds = objectLayerBounds(room1DoorClosedCollisionLayer);
        if (collisionBounds == null) {
            collisionBounds = objectLayerBounds(room1DoorTriggerLayer);
        }
        if (collisionBounds != null) {
            room1DoorClosedCollisionBounds.set(collisionBounds);
        }
        appendDoorFramesFromGroup(findLayerByName(ROOM1_DOOR_ANIMATION_GROUP_NAME), room1DoorAnimationFrames);
        appendDoorFramesFromGroup(findLayerByName(ROOM1_DOOR_FRONT_ANIMATION_GROUP_NAME), room1DoorFrontAnimationFrames);
        if (room1DoorFrameCount() == 0) {
            return;
        }

        int visibleIndex = findVisibleDoorFrameIndex(room1DoorAnimationFrames);
        if (visibleIndex < 0) {
            visibleIndex = findVisibleDoorFrameIndex(room1DoorFrontAnimationFrames);
        }
        room1DoorAnimationFrameIndex = visibleIndex >= 0
                ? visibleIndex
                : room1DoorFrameCount() - 1;
        setRoom1DoorFrameVisible(room1DoorAnimationFrameIndex);
        room1DoorClosed = room1DoorAnimationFrameIndex == 0;
        room1DoorCollisionEnabled = room1DoorClosed && hasRoom1DoorCollisionBounds();
    }

    private void configureFirstBossDoorAnimation() {
        firstBossDoorAnimationFrames.clear();
        firstBossDoorFrontAnimationFrames.clear();
        firstBossDoorTriggerLayer = null;
        firstBossDoorTriggerBounds.set(0f, 0f, 0f, 0f);
        firstBossDoorClosedCollisionBounds.set(0f, 0f, 0f, 0f);
        firstBossDoorAnimationFrameIndex = 0;
        firstBossDoorAnimationTimer = 0f;
        firstBossDoorPauseTimer = 0f;
        firstBossDoorClosing = false;
        firstBossDoorClosed = false;
        firstBossDoorCollisionEnabled = false;
        firstBossDoorTriggered = false;
        firstBossDoorTriggerPrimed = false;
        wasPlayerWithinFirstBossDoorTrigger = false;
        resetBoss1IntroState();

        if (!MAP_ID_DEMON_CASTLE.equals(world.activeMapId)) {
            return;
        }

        // Use only the finite-map first-boss folder:
        // DOOR-FirstBoss-Double-Animation-1 / Door-1..Door-5 / Door-Closed(open-after-defeating)
        MapLayer firstBossDoorGroup = findLayerByName(FIRST_BOSS_DOOR_ANIMATION_GROUP_NAME);
        firstBossDoorTriggerLayer = findLayerInGroup(
                FIRST_BOSS_DOOR_ANIMATION_GROUP_NAME,
                FIRST_BOSS_DOOR_TRIGGER_LAYER_NAME);
        appendDoorFramesFromGroup(firstBossDoorGroup, firstBossDoorAnimationFrames);
        if (firstBossDoorFrameCount() == 0) {
            return;
        }

        Rectangle triggerBounds = objectLayerBounds(firstBossDoorTriggerLayer);
        if (triggerBounds != null && triggerBounds.width > 0f && triggerBounds.height > 0f) {
            firstBossDoorTriggerBounds.set(triggerBounds);
            firstBossDoorClosedCollisionBounds.set(triggerBounds);
        } else {
            Rectangle visualBounds = doorAnimationVisualBounds(firstBossDoorAnimationFrames);
            if (visualBounds != null && visualBounds.width > 0f && visualBounds.height > 0f) {
                firstBossDoorTriggerBounds.set(visualBounds);
                firstBossDoorClosedCollisionBounds.set(visualBounds);
            }
        }

        // The first-boss door should start open (Door-1) and close on trigger.
        firstBossDoorAnimationFrameIndex = 0;
        setFirstBossDoorFrameVisible(firstBossDoorAnimationFrameIndex);
        firstBossDoorClosed = false;
        firstBossDoorCollisionEnabled = false;
        firstBossDoorTriggered = false;
        firstBossDoorTriggerPrimed = false;
        wasPlayerWithinFirstBossDoorTrigger = false;
    }

    private void configureBoss1DefeatDoorAnimation() {
        boss1DefeatDoorAnimationFrames.clear();
        boss1DefeatDoorAnimationFrameIndex = 0;
        boss1DefeatDoorAnimationTimer = 0f;
        boss1DefeatDoorOpenDelayTimer = 0f;
        boss1DefeatDoorOpening = false;
        boss1DefeatDoorOpened = false;
        boss1DefeatDoorTriggered = false;

        if (!MAP_ID_DEMON_CASTLE.equals(world.activeMapId)) {
            return;
        }

        appendDoorFramesFromGroup(
                findLayerByName(BOSS_1_DEFEAT_DOOR_ANIMATION_GROUP_NAME),
                boss1DefeatDoorAnimationFrames);
        if (boss1DefeatDoorFrameCount() == 0) {
            return;
        }

        // Keep this door closed until Boss-1 is defeated, then play Door-1 -> Door-8 once.
        boss1DefeatDoorAnimationFrameIndex = 0;
        setBoss1DefeatDoorFrameVisible(boss1DefeatDoorAnimationFrameIndex);
    }

    private void configureBossFightZone() {
        bossFightZoneLayer = null;
        bossFightZoneBounds.set(0f, 0f, 0f, 0f);
        bossFightZoneDarkMode = false;
        bossFightZoneDarkTransitionTimer = 0f;
        if (!MAP_ID_DEMON_CASTLE.equals(world.activeMapId)) {
            return;
        }
        bossFightZoneLayer = findLayerInGroup(BOSS_1_GROUP_LAYER_NAME, BOSS_FIGHT_ZONE_LAYER_NAME);
        Rectangle bounds = objectLayerBounds(bossFightZoneLayer);
        if (bounds != null && bounds.width > 0f && bounds.height > 0f) {
            bossFightZoneBounds.set(bounds);
        }
    }

    private void configureGolemFightZone() {
        golemFightZoneLayer = null;
        golemFightZoneBounds.set(0f, 0f, 0f, 0f);
        golemFightZoneDarkMode = false;
        golemFightZoneDarkTransitionTimer = 0f;
        if (!MAP_ID_DEMON_CASTLE.equals(world.activeMapId)) {
            return;
        }
        golemFightZoneLayer = resolveGolem1WalkableLayer();
        Rectangle bounds = objectLayerBounds(golemFightZoneLayer);
        if (bounds != null && bounds.width > 0f && bounds.height > 0f) {
            golemFightZoneBounds.set(bounds);
        }
    }

    private void configureRoom1BatSpawns() {
        clearSpawnedBat();
        if (!MAP_ID_DEMON_CASTLE.equals(world.activeMapId) || batSleepAnim == null) {
            return;
        }

        MapLayer spawnLayer = findLayerInGroup(ROOM1_GROUP_LAYER_NAME, ROOM1_BATS_SPAWN_LAYER_NAME);
        if (spawnLayer == null) {
            return;
        }

        for (MapObject object : spawnLayer.getObjects()) {
            Vector2 spawn = extractObjectSpawn(spawnLayer, object);
            if (spawn != null) {
                addBatAt(spawn.x, spawn.y);
            }
        }

        if (bats.size == 0) {
            Vector2 layerSpawn = extractLayerSpawn(spawnLayer);
            if (layerSpawn != null) {
                addBatAt(layerSpawn.x, layerSpawn.y);
            }
        }
    }

    private void configureGolem1Spawns() {
        clearSpawnedGolem1();
        if (golem1WalkAnim == null && golem1IdleAnim == null && golem1AttackAnim == null) {
            return;
        }

        MapLayer spawnLayer = resolveGolem1SpawnLayer();
        if (spawnLayer == null) {
            return;
        }
        Rectangle zoneBounds = hasValidBossZoneBounds(golemFightZoneBounds)
                ? new Rectangle(golemFightZoneBounds)
                : null;

        for (MapObject object : spawnLayer.getObjects()) {
            Rectangle bounds = objectBounds(spawnLayer, object);
            if (bounds == null) {
                continue;
            }
            float spawnX = bounds.x + bounds.width * 0.5f;
            float spawnY = bounds.y + bounds.height * 0.5f;
            addGolem1At(spawnX, spawnY, zoneBounds);
        }

        if (golem1s.size == 0) {
            Vector2 layerSpawn = extractLayerSpawn(spawnLayer);
            if (layerSpawn != null) {
                addGolem1At(layerSpawn.x, layerSpawn.y, zoneBounds);
            }
        }
    }

    private void configureBossArenaPhase2Demons() {
        clearSpawnedBossArenaPhase2Demons();
        if (!MAP_ID_BOSS_ARENA_PHASE_2.equals(world.activeMapId)
                || (bossArenaPhase2DemonIdleAnim == null
                && bossArenaPhase2DemonWalkAnim == null
                && bossArenaPhase2DemonAttackAnim == null
                && bossArenaPhase2DemonHurtAnim == null)) {
            return;
        }

        MapLayer spawnLayer = resolveBossArenaPhase2DemonSpawnLayer();
        if (spawnLayer == null) {
            return;
        }

        for (MapObject object : spawnLayer.getObjects()) {
            Vector2 spawn = extractObjectSpawn(spawnLayer, object);
            if (spawn != null) {
                addBossArenaPhase2DemonAt(spawn.x, spawn.y);
            }
        }

        if (bossArenaPhase2Demons.size == 0) {
            Vector2 layerSpawn = extractLayerSpawn(spawnLayer);
            if (layerSpawn != null) {
                addBossArenaPhase2DemonAt(layerSpawn.x, layerSpawn.y);
            }
        }
    }

    private MapLayer resolveBossArenaPhase2DemonSpawnLayer() {
        // Prefer top-level Boss-Spawn so map object placement is deterministic.
        MapLayer layer = findLayerByName(BOSS_ARENA_PHASE_2_DEMON_SPAWN_LAYER_NAME);
        if (layer != null) {
            return layer;
        }
        return findLayerInGroup(BOSS_2_GROUP_LAYER_NAME, BOSS_ARENA_PHASE_2_DEMON_SPAWN_LAYER_NAME);
    }

    private void configureRewardChestSpawns() {
        clearSpawnedRewardChests();
        if (chestIdleAnim == null && chestOpenAnim == null) {
            return;
        }
        MapLayer spawnLayer = resolveRewardChestSpawnLayer();
        if (spawnLayer == null) {
            return;
        }

        for (MapObject object : spawnLayer.getObjects()) {
            Rectangle bounds = objectBounds(spawnLayer, object);
            if (bounds == null) {
                continue;
            }
            float spawnX = bounds.x + bounds.width * 0.5f;
            float spawnY = bounds.y + bounds.height * 0.5f;
            addRewardChestAt(spawnX, spawnY, bounds.width, bounds.height);
        }

        if (rewardChests.size == 0) {
            Vector2 layerSpawn = extractLayerSpawn(spawnLayer);
            if (layerSpawn != null) {
                addRewardChestAt(layerSpawn.x, layerSpawn.y, 0f, 0f);
            }
        }
    }

    private MapLayer resolveRewardChestSpawnLayer() {
        MapLayer layer = findLayerInGroup(CHEST_REWARD_GROUP_LAYER_NAME, CHEST_REWARD_OBJECT_LAYER_NAME);
        if (layer != null) {
            return layer;
        }
        return findLayerByName(CHEST_REWARD_OBJECT_LAYER_NAME);
    }

    private MapLayer resolveGolem1WalkableLayer() {
        MapLayer layer = findLayerInGroup(GOLEM_1_GROUP_LAYER_NAME, GOLEM_1_WALKABLE_LAYER_NAME);
        if (layer != null) {
            return layer;
        }
        return findLayerByName(GOLEM_1_WALKABLE_LAYER_NAME);
    }

    private MapLayer resolveGolem1SpawnLayer() {
        MapLayer layer = findLayerInGroup(GOLEM_1_GROUP_LAYER_NAME, GOLEM_1_OBJECT_LAYER_NAME);
        if (layer != null) {
            return layer;
        }
        layer = findLayerInGroup(GOLEM_1_GROUP_LAYER_NAME, GOLEM_1_OBJECT_LAYER_NAME_ALT);
        if (layer != null) {
            return layer;
        }
        layer = findLayerByName(GOLEM_1_OBJECT_LAYER_NAME);
        if (layer != null) {
            return layer;
        }
        layer = findLayerByName(GOLEM_1_OBJECT_LAYER_NAME_ALT);
        if (layer != null) {
            return layer;
        }
        layer = findLayerByName(GOLEM_1_OBJECT_LAYER_NAME_ALT_2);
        if (layer != null) {
            return layer;
        }
        return findLayerByName(GOLEM_1_OBJECT_LAYER_NAME_ALT_3);
    }

    private void configurePhase2BossWizards() {
        clearSpawnedEvilWizards();
        if (!MAP_ID_DEMON_CASTLE.equals(world.activeMapId)
                || (evilWizardIdleAnim == null
                && evilWizardRunAnim == null
                && evilWizardAttack1Anim == null
                && evilWizardAttack2Anim == null)) {
            return;
        }
        addPhase2BossWizardsFromGroup(BOSS_1_GROUP_LAYER_NAME);
        addPhase2BossWizardsFromGroup(BOSS_2_GROUP_LAYER_NAME);
    }

    private void addPhase2BossWizardsFromGroup(String groupLayerName) {
        MapLayer spawnLayer = findLayerInGroup(groupLayerName, BOSS_PHASE_2_BOSS_LAYER_NAME);
        if (spawnLayer == null) {
            return;
        }
        Rectangle zoneBounds = resolveBossWizardZoneBounds(groupLayerName);
        float depthBasisOffsetTiles = EVIL_WIZARD_DEPTH_BASIS_OFFSET_TILES;
        float shadowDownOffsetTiles = EVIL_WIZARD_SHADOW_DOWN_OFFSET_TILES;
        if (BOSS_1_GROUP_LAYER_NAME.equals(groupLayerName)) {
            depthBasisOffsetTiles += BOSS_1_DEPTH_BASIS_ADJUST_TILES;
        } else if (BOSS_2_GROUP_LAYER_NAME.equals(groupLayerName)) {
            shadowDownOffsetTiles += BOSS_2_SHADOW_OFFSET_ADJUST_TILES;
        }
        for (MapObject object : spawnLayer.getObjects()) {
            Vector2 spawn = extractObjectSpawn(spawnLayer, object);
            if (spawn != null) {
                addEvilWizardAt(spawn.x, spawn.y, zoneBounds, depthBasisOffsetTiles, shadowDownOffsetTiles);
            }
        }
    }

    private void addBatAt(float x, float y) {
        BatInstance bat = new BatInstance();
        bat.position.set(x, y);
        bats.add(bat);
    }

    private void addGolem1At(float x, float y, Rectangle zoneBounds) {
        Golem1Instance golem = new Golem1Instance();
        golem.position.set(x, y);
        if (hasValidBossZoneBounds(zoneBounds)) {
            golem.zoneBounds = new Rectangle(zoneBounds);
            clampGolem1ToZone(golem);
        }
        // Small per-golem speed variance so each instance chases independently.
        golem.chaseSpeed = GOLEM_1_CHASE_SPEED * (0.9f + (float) Math.random() * 0.2f);
        Animation<TextureRegion> baseAnimation = golem1WalkAnim != null
                ? golem1WalkAnim
                : (golem1IdleAnim != null ? golem1IdleAnim : golem1AttackAnim);
        if (baseAnimation != null) {
            float cycle = baseAnimation.getAnimationDuration();
            if (cycle > 0f) {
                golem.stateTime = (float) (Math.random() * cycle);
            }
        }
        golem1s.add(golem);
    }

    private void addBossArenaPhase2DemonAt(float x, float y) {
        BossArenaPhase2DemonInstance demon = new BossArenaPhase2DemonInstance();
        float spawnOffsetX = BOSS_ARENA_PHASE_2_DEMON_SPAWN_OFFSET_X_TILES * DEFAULT_TILE_SIZE;
        float spawnOffsetY = BOSS_ARENA_PHASE_2_DEMON_SPAWN_OFFSET_Y_TILES * DEFAULT_TILE_SIZE;
        demon.position.set(x + spawnOffsetX, y + spawnOffsetY);
        demon.facingLeft = true;
        demon.attacking = false;
        demon.attackStateTime = 0f;
        demon.hurting = false;
        demon.hurtStateTime = 0f;
        demon.playerHitCooldownTimer = 0f;
        demon.lastDashAttackHitId = -1;
        Animation<TextureRegion> spawnAnimation = bossArenaPhase2DemonIdleAnim != null
                ? bossArenaPhase2DemonIdleAnim
                : (bossArenaPhase2DemonWalkAnim != null
                ? bossArenaPhase2DemonWalkAnim
                : (bossArenaPhase2DemonAttackAnim != null
                ? bossArenaPhase2DemonAttackAnim
                : bossArenaPhase2DemonHurtAnim));
        if (spawnAnimation != null) {
            float cycle = spawnAnimation.getAnimationDuration();
            if (cycle > 0f) {
                demon.stateTime = (float) (Math.random() * cycle);
            }
        }
        bossArenaPhase2Demons.add(demon);
    }

    private void addRewardChestAt(float x, float y, float drawWidth, float drawHeight) {
        RewardChestInstance chest = new RewardChestInstance();
        chest.position.set(x, y);
        chest.drawWidth = drawWidth > 0f ? drawWidth : defaultChestDrawWidth();
        chest.drawHeight = drawHeight > 0f ? drawHeight : defaultChestDrawHeight();
        rewardChests.add(chest);
    }

    private float defaultChestDrawWidth() {
        TextureRegion frame = firstChestReferenceFrame();
        if (frame != null && frame.getRegionWidth() > 0) {
            return frame.getRegionWidth();
        }
        return CHEST_DEFAULT_DRAW_SIZE;
    }

    private float defaultChestDrawHeight() {
        TextureRegion frame = firstChestReferenceFrame();
        if (frame != null && frame.getRegionHeight() > 0) {
            return frame.getRegionHeight();
        }
        return CHEST_DEFAULT_DRAW_SIZE;
    }

    private TextureRegion firstChestReferenceFrame() {
        if (chestIdleAnim != null) {
            TextureRegion idleFrame = chestIdleAnim.getKeyFrame(0f, true);
            if (idleFrame != null) {
                return idleFrame;
            }
        }
        if (chestOpenAnim != null) {
            return chestOpenAnim.getKeyFrame(0f, false);
        }
        return null;
    }

    private void addEvilWizardAt(
            float x,
            float y,
            Rectangle zoneBounds,
            float depthBasisOffsetTiles,
            float shadowDownOffsetTiles) {
        EvilWizardInstance wizard = new EvilWizardInstance();
        wizard.position.set(x, y);
        wizard.depthBasisOffsetTiles = depthBasisOffsetTiles;
        wizard.shadowDownOffsetTiles = shadowDownOffsetTiles;
        if (hasValidBossZoneBounds(zoneBounds)) {
            wizard.zoneBounds = new Rectangle(zoneBounds);
            clampEvilWizardToZone(wizard);
        }
        if (evilWizardIdleAnim != null) {
            float cycle = evilWizardIdleAnim.getAnimationDuration();
            if (cycle > 0f) {
                wizard.stateTime = (float) (Math.random() * cycle);
            }
        }
        evilWizards.add(wizard);
    }

    private void updateEvilWizards(float delta) {
        if (evilWizards.size == 0 || delta <= 0f) {
            return;
        }
        if (!boss1IntroPlayed
                && !isBoss1IntroActive()
                && MAP_ID_DEMON_CASTLE.equals(world.activeMapId)
                && bossFightZoneLayer != null
                && isPlayerWithinObjectLayer(bossFightZoneLayer)) {
            startBoss1IntroSequence();
        }
        if (isBoss1IntroActive()) {
            setBoss1WizardsToIdleDuringIntro(delta);
            return;
        }
        for (int i = evilWizards.size - 1; i >= 0; i--) {
            EvilWizardInstance wizard = evilWizards.get(i);
            if (wizard.state == EvilWizardAnimationState.DIE) {
                if (evilWizardDieAnim != null) {
                    float dieDuration = Math.max(0f, evilWizardDieAnim.getAnimationDuration());
                    wizard.stateTime = Math.min(dieDuration, wizard.stateTime + delta);
                } else {
                    wizard.stateTime += delta;
                }
                continue;
            }
            if (wizard.health <= 0f) {
                setEvilWizardState(wizard, EvilWizardAnimationState.DIE);
                wizard.stateTime += delta;
                continue;
            }
            if (wizard.playerHitCooldownTimer > 0f) {
                wizard.playerHitCooldownTimer = Math.max(0f, wizard.playerHitCooldownTimer - delta);
            }
            if (wizard.attackCooldownTimer > 0f) {
                wizard.attackCooldownTimer = Math.max(0f, wizard.attackCooldownTimer - delta);
            }
            clampEvilWizardToZone(wizard);

            if (wizard.state == EvilWizardAnimationState.HURT) {
                if (evilWizardHurtAnim == null || evilWizardHurtAnim.isAnimationFinished(wizard.stateTime)) {
                    setEvilWizardState(wizard, wizard.stateBeforeHurt);
                } else {
                    wizard.stateTime += delta;
                    continue;
                }
            }

            boolean chasing = shouldEvilWizardChasePlayer(wizard);
            if (!chasing) {
                setEvilWizardState(wizard, EvilWizardAnimationState.IDLE);
                wizard.stateTime += delta;
                continue;
            }

            updateEvilWizardFacingTowardPlayer(wizard);

            if (wizard.state == EvilWizardAnimationState.ATTACKING) {
                Animation<TextureRegion> attackAnimation = activeEvilWizardAttackAnimation(wizard);
                if (attackAnimation == null) {
                    setEvilWizardState(wizard, EvilWizardAnimationState.RUN);
                } else if (attackAnimation.isAnimationFinished(wizard.stateTime)) {
                    if (isEvilWizardWithinAttackRange(wizard)) {
                        wizard.attackCooldownTimer = EVIL_WIZARD_ATTACK_INTERVAL_SECONDS;
                        setEvilWizardState(wizard, EvilWizardAnimationState.IDLE);
                    } else {
                        setEvilWizardState(wizard, EvilWizardAnimationState.RUN);
                    }
                }
                wizard.stateTime += delta;
                continue;
            }

            if (isEvilWizardWithinAttackRange(wizard)) {
                if (wizard.attackCooldownTimer <= 0f) {
                    startEvilWizardAttack(wizard);
                } else {
                    setEvilWizardState(wizard, EvilWizardAnimationState.IDLE);
                }
                wizard.stateTime += delta;
                continue;
            }

            boolean moved = updateEvilWizardChaseMovement(wizard, delta);
            setEvilWizardState(wizard, moved ? EvilWizardAnimationState.RUN : EvilWizardAnimationState.IDLE);
            wizard.stateTime += delta;
        }
    }

    private void updateGolem1s(float delta) {
        if (golem1s.size == 0 || delta <= 0f) {
            return;
        }
        float safeDelta = Math.max(0f, delta);
        float playerTargetX = player.bounds.x + player.bounds.width * 0.5f;
        float playerTargetY = player.bounds.y + player.bounds.height * 0.5f;
        float stopDistanceSq = GOLEM_1_CHASE_STOP_DISTANCE * GOLEM_1_CHASE_STOP_DISTANCE;
        float attackRangeSq = GOLEM_1_ATTACK_RANGE * GOLEM_1_ATTACK_RANGE;
        for (int i = 0; i < golem1s.size; i++) {
            Golem1Instance golem = golem1s.get(i);
            if (golem.playerHitCooldownTimer > 0f) {
                golem.playerHitCooldownTimer = Math.max(0f, golem.playerHitCooldownTimer - safeDelta);
            }
            if (golem.attackCooldownTimer > 0f) {
                golem.attackCooldownTimer = Math.max(0f, golem.attackCooldownTimer - safeDelta);
            }
            if (!golem.dying && golem.health <= 0f) {
                startGolem1Dying(golem);
            }
            if (golem.dying) {
                golem.moving = false;
                golem.attacking = false;
                golem.attackStateTime = 0f;
                golem.hurting = false;
                golem.hurtStateTime = 0f;
                golem.explosions.clear();
                if (golem1DieAnim != null) {
                    float dieDuration = Math.max(0f, golem1DieAnim.getAnimationDuration());
                    golem.dieStateTime = Math.min(dieDuration, golem.dieStateTime + safeDelta);
                } else {
                    golem.dieStateTime += safeDelta;
                }
                golem.stateTime += safeDelta;
                continue;
            }
            if (golem.hurting) {
                golem.hurtStateTime += safeDelta;
                if (golem1HurtAnim == null || golem1HurtAnim.isAnimationFinished(golem.hurtStateTime)) {
                    golem.hurting = false;
                    golem.hurtStateTime = 0f;
                } else {
                    golem.moving = false;
                    golem.stateTime += safeDelta;
                    continue;
                }
            }
            if (golem.attacking) {
                golem.attackStateTime += safeDelta;
                if (golem1AttackAnim == null
                        || golem1AttackAnim.isAnimationFinished(golem.attackStateTime)) {
                    golem.attacking = false;
                    golem.attackStateTime = 0f;
                }
            }
            updateGolem1ExplosionLine(golem, safeDelta);
            clampGolem1ToZone(golem);
            if (!shouldGolem1ChasePlayer(golem)) {
                golem.moving = false;
                golem.stateTime += safeDelta;
                continue;
            }
            float dx = playerTargetX - golem.position.x;
            float dy = playerTargetY - golem.position.y;
            if (Math.abs(dx) > 0.001f) {
                golem.facingLeft = dx < 0f;
            }
            float distanceSq = dx * dx + dy * dy;
            if (distanceSq <= attackRangeSq) {
                golem.moving = false;
                if (golem.attackCooldownTimer <= 0f
                        && !hasPendingOrActiveGolem1Explosions(golem)
                        && !golem.attacking) {
                    golem.attackCooldownTimer = GOLEM_1_ATTACK_COOLDOWN_SECONDS;
                    golem.attacking = true;
                    golem.attackStateTime = 0f;
                    queueGolem1ExplosionLine(golem, dx, dy);
                }
                golem.stateTime += safeDelta;
                continue;
            }
            if (golem.attacking) {
                golem.moving = false;
                golem.stateTime += safeDelta;
                continue;
            }
            boolean moved = false;
            if (distanceSq > stopDistanceSq) {
                float prevX = golem.position.x;
                float prevY = golem.position.y;
                float distance = (float) Math.sqrt(distanceSq);
                float maxStep = golem.chaseSpeed * safeDelta;
                float moveScale = distance > maxStep && distance > 0f ? maxStep / distance : 1f;
                golem.position.x += dx * moveScale;
                golem.position.y += dy * moveScale;
                clampGolem1ToZone(golem);
                moved = Math.abs(golem.position.x - prevX) > 0.001f
                        || Math.abs(golem.position.y - prevY) > 0.001f;
            }
            golem.moving = moved;
            golem.stateTime += safeDelta;
        }
    }

    private void updateRewardChests(float delta) {
        if (rewardChests.size == 0 || delta <= 0f) {
            return;
        }
        float safeDelta = Math.max(0f, delta);
        boolean canOpenChest = isAllBossesDefeatedForChestReward();
        float triggerRange = CHEST_OPEN_TRIGGER_RANGE_TILES * DEFAULT_TILE_SIZE;
        float triggerRangeSq = triggerRange * triggerRange;
        float playerCenterX = player.bounds.x + player.bounds.width * 0.5f;
        float playerCenterY = player.bounds.y + player.bounds.height * 0.5f;
        for (int i = 0; i < rewardChests.size; i++) {
            RewardChestInstance chest = rewardChests.get(i);
            chest.stateTime += safeDelta;
            if (chest.opened) {
                continue;
            }
            if (chest.opening) {
                chest.openStateTime += safeDelta;
                if (chestOpenAnim == null || chestOpenAnim.isAnimationFinished(chest.openStateTime)) {
                    chest.opening = false;
                    chest.opened = true;
                    chest.openStateTime = chestOpenAnim != null
                            ? Math.max(0f, chestOpenAnim.getAnimationDuration())
                            : 0f;
                }
                continue;
            }
            if (!canOpenChest) {
                continue;
            }
            float dx = playerCenterX - chest.position.x;
            float dy = playerCenterY - chest.position.y;
            if (dx * dx + dy * dy <= triggerRangeSq) {
                if (chestOpenAnim == null) {
                    chest.opened = true;
                } else {
                    chest.opening = true;
                    chest.openStateTime = 0f;
                }
            }
        }
    }

    private void updateBossArenaPhase2Demons(float delta) {
        if (bossArenaPhase2Demons.size == 0 || delta <= 0f) {
            return;
        }
        float safeDelta = Math.max(0f, delta);
        float playerCenterX = player.bounds.x + player.bounds.width * 0.5f;
        float playerCenterY = player.bounds.y + player.bounds.height * 0.5f;
        float stopDistance = BOSS_ARENA_PHASE_2_DEMON_CHASE_STOP_DISTANCE_TILES * DEFAULT_TILE_SIZE;
        float stopDistanceSq = stopDistance * stopDistance;
        float attackRangeSq = BOSS_ARENA_PHASE_2_DEMON_ATTACK_RANGE * BOSS_ARENA_PHASE_2_DEMON_ATTACK_RANGE;
        for (int i = 0; i < bossArenaPhase2Demons.size; i++) {
            BossArenaPhase2DemonInstance demon = bossArenaPhase2Demons.get(i);
            if (demon.playerHitCooldownTimer > 0f) {
                demon.playerHitCooldownTimer = Math.max(0f, demon.playerHitCooldownTimer - safeDelta);
            }
            float detectionCenterX = bossArenaPhase2DemonDetectionCenterX(demon);
            float detectionCenterY = bossArenaPhase2DemonDetectionCenterY(demon);
            float dx = playerCenterX - detectionCenterX;
            float dy = playerCenterY - detectionCenterY;
            float distanceSq = dx * dx + dy * dy;
            demon.moving = false;
            if (Math.abs(dx) > 0.001f) {
                demon.facingLeft = dx < 0f;
            }

            if (demon.hurting) {
                demon.attackStateTime = 0f;
                demon.attacking = false;
                demon.hurtStateTime += safeDelta;
                if (bossArenaPhase2DemonHurtAnim == null
                        || bossArenaPhase2DemonHurtAnim.isAnimationFinished(demon.hurtStateTime)) {
                    demon.hurting = false;
                    demon.hurtStateTime = 0f;
                }
                demon.stateTime += safeDelta;
                continue;
            }

            if (demon.attacking) {
                demon.attackStateTime += safeDelta;
                if (bossArenaPhase2DemonAttackAnim == null
                        || bossArenaPhase2DemonAttackAnim.isAnimationFinished(demon.attackStateTime)) {
                    demon.attacking = false;
                    demon.attackStateTime = 0f;
                }
            }

            if (distanceSq <= attackRangeSq && bossArenaPhase2DemonAttackAnim != null) {
                if (!demon.attacking) {
                    demon.attacking = true;
                    demon.attackStateTime = 0f;
                }
                demon.stateTime += safeDelta;
                continue;
            }

            if (!demon.attacking && distanceSq > stopDistanceSq) {
                float distance = (float) Math.sqrt(distanceSq);
                if (distance > 0.0001f) {
                    float maxStep = BOSS_ARENA_PHASE_2_DEMON_CHASE_SPEED * safeDelta;
                    float remainingAfterStop = Math.max(0f, distance - stopDistance);
                    float step = Math.min(maxStep, remainingAfterStop);
                    if (step > 0f) {
                        demon.position.x += dx / distance * step;
                        demon.position.y += dy / distance * step;
                        demon.moving = true;
                    }
                }
            }
            demon.stateTime += safeDelta;
        }
    }

    private void updateGolem1ExplosionLine(Golem1Instance golem, float delta) {
        if (golem == null || golem.explosions.size == 0) {
            return;
        }
        if (golem1ExplosionAnim == null) {
            golem.explosions.clear();
            return;
        }
        float safeDelta = Math.max(0f, delta);
        for (int i = golem.explosions.size - 1; i >= 0; i--) {
            Golem1ExplosionInstance burst = golem.explosions.get(i);
            if (!burst.active) {
                burst.delayTimer = Math.max(0f, burst.delayTimer - safeDelta);
                if (burst.delayTimer <= 0f) {
                    burst.active = true;
                    burst.stateTime = 0f;
                    triggerGolemExplosionCameraShake();
                    playGolem1ExplosionSfx();
                    applyGolem1ExplosionHitToPlayer(burst);
                }
            }
            if (!burst.active) {
                continue;
            }
            burst.stateTime += safeDelta;
            if (golem1ExplosionAnim.isAnimationFinished(burst.stateTime)) {
                golem.explosions.removeIndex(i);
            }
        }
    }

    private boolean hasPendingOrActiveGolem1Explosions(Golem1Instance golem) {
        return golem != null && golem.explosions.size > 0;
    }

    private void queueGolem1ExplosionLine(Golem1Instance golem, float towardPlayerX, float towardPlayerY) {
        if (golem == null) {
            return;
        }
        golem.explosions.clear();

        float directionX = towardPlayerX;
        float directionY = towardPlayerY;
        float directionLengthSq = directionX * directionX + directionY * directionY;
        if (directionLengthSq <= 0.0001f) {
            directionX = golem.facingLeft ? -1f : 1f;
            directionY = 0f;
            directionLengthSq = 1f;
        }
        float invDirectionLength = 1f / (float) Math.sqrt(directionLengthSq);
        directionX *= invDirectionLength;
        directionY *= invDirectionLength;

        float maxDistance = GOLEM_1_EXPLOSION_CHAIN_MAX_DISTANCE_TILES * DEFAULT_TILE_SIZE;
        float stepDistance = GOLEM_1_EXPLOSION_CHAIN_STEP_TILES * DEFAULT_TILE_SIZE;
        float sourceOffsetY = GOLEM_1_EXPLOSION_SOURCE_OFFSET_Y_TILES * DEFAULT_TILE_SIZE;
        float sourceX = golem.position.x;
        float sourceY = golem.position.y + sourceOffsetY;
        int maxStepIndex = Math.max(0, (int) Math.floor(maxDistance / stepDistance));

        for (int step = 0; step <= maxStepIndex; step++) {
            float distance = step * stepDistance;
            float burstX = sourceX + directionX * distance;
            float burstY = sourceY + directionY * distance;

            Golem1ExplosionInstance burst = new Golem1ExplosionInstance();
            burst.position.set(burstX, burstY);
            burst.delayTimer = GOLEM_1_EXPLOSION_DELAY_SECONDS + step * GOLEM_1_EXPLOSION_CHAIN_STEP_DELAY_SECONDS;
            burst.stateTime = 0f;
            burst.active = false;
            golem.explosions.add(burst);
        }

        if (golem.explosions.size == 0) {
            Golem1ExplosionInstance fallbackBurst = new Golem1ExplosionInstance();
            fallbackBurst.position.set(golem.position);
            fallbackBurst.delayTimer = GOLEM_1_EXPLOSION_DELAY_SECONDS;
            golem.explosions.add(fallbackBurst);
        }
    }

    private boolean shouldGolem1ChasePlayer(Golem1Instance golem) {
        return MAP_ID_DEMON_CASTLE.equals(world.activeMapId)
                && golem != null
                && hasValidBossZoneBounds(golem.zoneBounds)
                && Intersector.overlaps(player.bounds, golem.zoneBounds);
    }

    private void clampGolem1ToZone(Golem1Instance golem) {
        if (golem == null || !hasValidBossZoneBounds(golem.zoneBounds)) {
            return;
        }
        float minX = golem.zoneBounds.x;
        float minY = golem.zoneBounds.y;
        float maxX = golem.zoneBounds.x + golem.zoneBounds.width;
        float maxY = golem.zoneBounds.y + golem.zoneBounds.height;
        golem.position.x = Math.max(minX, Math.min(golem.position.x, maxX));
        golem.position.y = Math.max(minY, Math.min(golem.position.y, maxY));
    }

    private void setBoss1WizardsToIdleDuringIntro(float delta) {
        float safeDelta = Math.max(0f, delta);
        for (int i = evilWizards.size - 1; i >= 0; i--) {
            EvilWizardInstance wizard = evilWizards.get(i);
            if (!isWizardInsideBoss1Zone(wizard)) {
                continue;
            }
            if (wizard.state == EvilWizardAnimationState.DIE) {
                if (evilWizardDieAnim != null) {
                    float dieDuration = Math.max(0f, evilWizardDieAnim.getAnimationDuration());
                    wizard.stateTime = Math.min(dieDuration, wizard.stateTime + safeDelta);
                } else {
                    wizard.stateTime += safeDelta;
                }
                continue;
            }
            if (wizard.health <= 0f) {
                setEvilWizardState(wizard, EvilWizardAnimationState.DIE);
                wizard.stateTime += safeDelta;
                continue;
            }
            if (wizard.playerHitCooldownTimer > 0f) {
                wizard.playerHitCooldownTimer = Math.max(0f, wizard.playerHitCooldownTimer - safeDelta);
            }
            setEvilWizardState(wizard, EvilWizardAnimationState.IDLE);
            wizard.stateTime += safeDelta;
        }
    }

    private Rectangle resolveBossWizardZoneBounds(String groupLayerName) {
        Rectangle walkableBounds = objectLayerBounds(findLayerInGroup(groupLayerName, BOSS_WALKABLE_LAYER_NAME));
        if (hasValidBossZoneBounds(walkableBounds)) {
            return walkableBounds;
        }
        Rectangle fightZoneBounds = objectLayerBounds(findLayerInGroup(groupLayerName, BOSS_FIGHT_ZONE_LAYER_NAME));
        if (hasValidBossZoneBounds(fightZoneBounds)) {
            return fightZoneBounds;
        }
        return null;
    }

    private boolean hasValidBossZoneBounds(Rectangle zoneBounds) {
        return zoneBounds != null && zoneBounds.width > 0f && zoneBounds.height > 0f;
    }

    private boolean shouldEvilWizardChasePlayer(EvilWizardInstance wizard) {
        return MAP_ID_DEMON_CASTLE.equals(world.activeMapId)
                && wizard != null
                && hasValidBossZoneBounds(wizard.zoneBounds)
                && Intersector.overlaps(player.bounds, wizard.zoneBounds);
    }

    private void clampEvilWizardToZone(EvilWizardInstance wizard) {
        if (wizard == null || !hasValidBossZoneBounds(wizard.zoneBounds)) {
            return;
        }
        float minX = wizard.zoneBounds.x;
        float minY = wizard.zoneBounds.y;
        float maxX = wizard.zoneBounds.x + wizard.zoneBounds.width;
        float maxY = wizard.zoneBounds.y + wizard.zoneBounds.height;
        wizard.position.x = Math.max(minX, Math.min(wizard.position.x, maxX));
        wizard.position.y = Math.max(minY, Math.min(wizard.position.y, maxY));
    }

    private boolean updateEvilWizardChaseMovement(EvilWizardInstance wizard, float delta) {
        if (wizard == null || delta <= 0f || !hasValidBossZoneBounds(wizard.zoneBounds)) {
            return false;
        }
        float minX = wizard.zoneBounds.x;
        float minY = wizard.zoneBounds.y;
        float maxX = wizard.zoneBounds.x + wizard.zoneBounds.width;
        float maxY = wizard.zoneBounds.y + wizard.zoneBounds.height;
        float playerLeft = player.bounds.x;
        float playerRight = player.bounds.x + player.bounds.width;
        float playerBottom = player.bounds.y;
        float playerTop = player.bounds.y + player.bounds.height;
        float targetX = Math.max(playerLeft, Math.min(wizard.position.x, playerRight));
        float targetY = Math.max(playerBottom, Math.min(wizard.position.y, playerTop));
        targetX = Math.max(minX, Math.min(targetX, maxX));
        targetY = Math.max(minY, Math.min(targetY, maxY));
        float maxStep = EVIL_WIZARD_CHASE_SPEED * delta;
        float stopDistanceSq = EVIL_WIZARD_CHASE_STOP_DISTANCE * EVIL_WIZARD_CHASE_STOP_DISTANCE;
        float dx = targetX - wizard.position.x;
        float dy = targetY - wizard.position.y;
        float distanceSq = dx * dx + dy * dy;
        if (distanceSq <= stopDistanceSq) {
            return false;
        }
        float distance = (float) Math.sqrt(distanceSq);
        float step = Math.min(maxStep, Math.max(0f, distance - EVIL_WIZARD_CHASE_STOP_DISTANCE));
        if (step <= 0f || distance <= 0f) {
            return false;
        }
        float moveX = (dx / distance) * step;
        float moveY = (dy / distance) * step;
        if (Math.abs(moveX) <= 0.0001f && Math.abs(moveY) <= 0.0001f) {
            return false;
        }
        float nextX = wizard.position.x + moveX;
        float nextY = wizard.position.y + moveY;
        float clampedX = Math.max(minX, Math.min(nextX, maxX));
        float clampedY = Math.max(minY, Math.min(nextY, maxY));
        float movedX = clampedX - wizard.position.x;
        float movedY = clampedY - wizard.position.y;
        if (Math.abs(movedX) <= 0.0001f && Math.abs(movedY) <= 0.0001f) {
            return false;
        }
        wizard.position.x = clampedX;
        wizard.position.y = clampedY;
        return true;
    }

    private void setEvilWizardState(EvilWizardInstance wizard, EvilWizardAnimationState state) {
        if (wizard == null || state == null || wizard.state == state) {
            return;
        }
        wizard.state = state;
        wizard.stateTime = 0f;
    }

    private void updateEvilWizardFacingTowardPlayer(EvilWizardInstance wizard) {
        if (wizard == null) {
            return;
        }
        float playerCenterX = player.bounds.x + player.bounds.width * 0.5f;
        float dx = playerCenterX - wizard.position.x;
        if (Math.abs(dx) > 0.001f) {
            wizard.facingLeft = dx < 0f;
        }
    }

    private boolean isEvilWizardWithinAttackRange(EvilWizardInstance wizard) {
        if (wizard == null) {
            return false;
        }
        float playerLeft = player.bounds.x;
        float playerRight = player.bounds.x + player.bounds.width;
        float playerBottom = player.bounds.y;
        float playerTop = player.bounds.y + player.bounds.height;
        float targetX = Math.max(playerLeft, Math.min(wizard.position.x, playerRight));
        float targetY = Math.max(playerBottom, Math.min(wizard.position.y, playerTop));
        float dx = targetX - wizard.position.x;
        float dy = targetY - wizard.position.y;
        return (dx * dx + dy * dy) <= (EVIL_WIZARD_ATTACK_RANGE * EVIL_WIZARD_ATTACK_RANGE);
    }

    private void startEvilWizardAttack(EvilWizardInstance wizard) {
        if (wizard == null) {
            return;
        }
        Animation<TextureRegion> attack1 = evilWizardAttack1Anim;
        Animation<TextureRegion> attack2 = evilWizardAttack2Anim;
        if (attack1 == null && attack2 == null) {
            return;
        }

        if (attack1 != null && attack2 != null) {
            wizard.usingAttack1 = wizard.useAttack1Next;
            wizard.useAttack1Next = !wizard.useAttack1Next;
        } else {
            wizard.usingAttack1 = attack1 != null;
            wizard.useAttack1Next = !wizard.usingAttack1;
        }
        playEvilWizardAttackSfx(wizard.usingAttack1);
        Animation<TextureRegion> selectedAttack = activeEvilWizardAttackAnimation(wizard);
        float attackDuration = selectedAttack != null
                ? Math.max(0f, selectedAttack.getAnimationDuration())
                : EVIL_WIZARD_ATTACK_FRAME_DURATION * 4f;
        startEvilWizardAttackHazard(wizard, attackDuration);
        setEvilWizardState(wizard, EvilWizardAnimationState.ATTACKING);
    }

    private void playEvilWizardAttackSfx(boolean attack1) {
        Sound attackSfx = attack1 ? evilWizardAttack1Sfx : evilWizardAttack2Sfx;
        if (attackSfx != null) {
            attackSfx.play(EVIL_WIZARD_ATTACK_SFX_VOLUME);
        }
    }

    private Animation<TextureRegion> activeEvilWizardAttackAnimation(EvilWizardInstance wizard) {
        if (wizard == null) {
            return null;
        }
        if (wizard.usingAttack1) {
            return evilWizardAttack1Anim != null ? evilWizardAttack1Anim : evilWizardAttack2Anim;
        }
        return evilWizardAttack2Anim != null ? evilWizardAttack2Anim : evilWizardAttack1Anim;
    }

    private void startEvilWizardAttackHazard(EvilWizardInstance wizard, float durationSeconds) {
        if (wizard == null) {
            return;
        }
        Rectangle zoneBounds = null;
        if (hasValidBossZoneBounds(wizard.zoneBounds)) {
            zoneBounds = wizard.zoneBounds;
        } else if (bossFightZoneBounds.width > 0f && bossFightZoneBounds.height > 0f) {
            zoneBounds = bossFightZoneBounds;
        }
        if (zoneBounds == null || zoneBounds.width <= 0f || zoneBounds.height <= 0f) {
            return;
        }
        evilWizardAttackHazardActive = true;
        evilWizardAttackHazardZoneBounds.set(zoneBounds);
        float minimumWarningWindowDuration =
                EVIL_WIZARD_ATTACK_HAZARD_EXPLOSION_WINDOW_SECONDS
                        + EVIL_WIZARD_PRE_EXPLOSION_WARNING_SECONDS
                        + EVIL_WIZARD_ATTACK_HAZARD_WARNING_LEAD_BUFFER_SECONDS;
        evilWizardAttackHazardTimer = Math.max(
                Math.max(0.15f, durationSeconds + EVIL_WIZARD_ATTACK_HAZARD_EXTRA_SECONDS),
                minimumWarningWindowDuration);
        evilWizardAttackHazardElapsedTimer = 0f;
        evilWizardAttackHazardExplosionTriggered = false;
        evilWizardPreExplosionWarningPlayed = false;
        generateEvilWizardSafeCircles(evilWizardAttackHazardZoneBounds);
    }

    private void updateEvilWizardAttackHazard(float delta) {
        if (!evilWizardAttackHazardActive) {
            return;
        }
        float safeDelta = Math.max(0f, delta);
        float previousTimer = evilWizardAttackHazardTimer;
        evilWizardAttackHazardElapsedTimer += safeDelta;
        evilWizardAttackHazardTimer = Math.max(0f, previousTimer - safeDelta);

        float preExplosionWarningTriggerTimer =
                EVIL_WIZARD_ATTACK_HAZARD_EXPLOSION_WINDOW_SECONDS + EVIL_WIZARD_PRE_EXPLOSION_WARNING_SECONDS;
        if (!evilWizardPreExplosionWarningPlayed
                && evilWizardAttackHazardTimer <= preExplosionWarningTriggerTimer) {
            evilWizardPreExplosionWarningPlayed = true;
            playEvilWizardPreExplosionWarningSfx();
        }

        if (!evilWizardAttackHazardExplosionTriggered
                && evilWizardAttackHazardTimer <= EVIL_WIZARD_ATTACK_HAZARD_EXPLOSION_WINDOW_SECONDS) {
            evilWizardAttackHazardExplosionTriggered = true;
            applyEvilWizardAttackHazardExplosionDamage();
        }

        if (evilWizardAttackHazardTimer <= 0f) {
            clearEvilWizardAttackHazard();
        }
    }

    private void applyEvilWizardAttackHazardExplosionDamage() {
        if (!evilWizardAttackHazardActive || player.currentHealth <= 0f) {
            return;
        }
        if (!isPlayerInsideEvilWizardAttackZone() || isPlayerInsideEvilWizardSafeCircle()) {
            return;
        }
        player.currentHealth = Math.max(0f, player.currentHealth - EVIL_WIZARD_ATTACK_HAZARD_DAMAGE);
        triggerPlayerHazardHitReaction();
    }

    private void playEvilWizardPreExplosionWarningSfx() {
        if (evilWizardPreExplosionSfx != null) {
            evilWizardPreExplosionSfx.stop();
            evilWizardPreExplosionSfx.setLooping(false);
            evilWizardPreExplosionSfx.setVolume(EVIL_WIZARD_PRE_EXPLOSION_SFX_VOLUME);
            float startAt = Math.max(0f, EVIL_WIZARD_PRE_EXPLOSION_SFX_START_SECONDS);
            if (startAt > 0f) {
                try {
                    evilWizardPreExplosionSfx.setPosition(startAt);
                } catch (RuntimeException ignored) {
                    evilWizardPreExplosionSfx.setPosition(0f);
                }
            } else {
                evilWizardPreExplosionSfx.setPosition(0f);
            }
            evilWizardPreExplosionSfx.play();
        }
    }

    private boolean isPlayerInsideEvilWizardAttackZone() {
        if (!evilWizardAttackHazardActive
                || evilWizardAttackHazardZoneBounds.width <= 0f
                || evilWizardAttackHazardZoneBounds.height <= 0f) {
            return false;
        }
        float centerX = player.bounds.x + player.bounds.width * 0.5f;
        float centerY = player.bounds.y + player.bounds.height * 0.5f;
        return evilWizardAttackHazardZoneBounds.contains(centerX, centerY);
    }

    private boolean isPlayerInsideEvilWizardSafeCircle() {
        if (evilWizardSafeCircleCenters.size == 0) {
            return false;
        }
        float radius = EVIL_WIZARD_SAFE_CIRCLE_RADIUS_TILES * DEFAULT_TILE_SIZE;
        float radiusSq = radius * radius;
        float centerX = player.bounds.x + player.bounds.width * 0.5f;
        float centerY = player.bounds.y + player.bounds.height * 0.5f;
        for (int i = 0; i < evilWizardSafeCircleCenters.size; i++) {
            Vector2 safeCenter = evilWizardSafeCircleCenters.get(i);
            float dx = centerX - safeCenter.x;
            float dy = centerY - safeCenter.y;
            if (dx * dx + dy * dy <= radiusSq) {
                return true;
            }
        }
        return false;
    }

    private void generateEvilWizardSafeCircles(Rectangle zoneBounds) {
        evilWizardSafeCircleCenters.clear();
        if (zoneBounds == null || zoneBounds.width <= 0f || zoneBounds.height <= 0f) {
            return;
        }
        float radius = EVIL_WIZARD_SAFE_CIRCLE_RADIUS_TILES * DEFAULT_TILE_SIZE;
        float minSeparation = EVIL_WIZARD_SAFE_CIRCLE_MIN_SEPARATION_TILES * DEFAULT_TILE_SIZE;
        float minSeparationSq = minSeparation * minSeparation;
        float allowedWidth = zoneBounds.width - radius * 2f;
        float allowedHeight = zoneBounds.height - radius * 2f;
        int targetCount = EVIL_WIZARD_SAFE_CIRCLE_MIN_COUNT
                + (int) (Math.random() * (EVIL_WIZARD_SAFE_CIRCLE_MAX_COUNT - EVIL_WIZARD_SAFE_CIRCLE_MIN_COUNT + 1));
        if (allowedWidth <= 0f || allowedHeight <= 0f) {
            evilWizardSafeCircleCenters.add(new Vector2(
                    zoneBounds.x + zoneBounds.width * 0.5f,
                    zoneBounds.y + zoneBounds.height * 0.5f));
            return;
        }

        int attempts = 0;
        int maxAttempts = targetCount * 40;
        while (evilWizardSafeCircleCenters.size < targetCount && attempts < maxAttempts) {
            attempts++;
            float x = zoneBounds.x + radius + (float) Math.random() * allowedWidth;
            float y = zoneBounds.y + radius + (float) Math.random() * allowedHeight;
            boolean tooClose = false;
            for (int i = 0; i < evilWizardSafeCircleCenters.size; i++) {
                Vector2 existing = evilWizardSafeCircleCenters.get(i);
                float dx = x - existing.x;
                float dy = y - existing.y;
                if (dx * dx + dy * dy < minSeparationSq) {
                    tooClose = true;
                    break;
                }
            }
            if (!tooClose) {
                evilWizardSafeCircleCenters.add(new Vector2(x, y));
            }
        }

        if (evilWizardSafeCircleCenters.size == 0) {
            evilWizardSafeCircleCenters.add(new Vector2(
                    zoneBounds.x + zoneBounds.width * 0.5f,
                    zoneBounds.y + zoneBounds.height * 0.5f));
        }
    }

    private void clearEvilWizardAttackHazard() {
        evilWizardAttackHazardActive = false;
        evilWizardAttackHazardZoneBounds.set(0f, 0f, 0f, 0f);
        evilWizardAttackHazardTimer = 0f;
        evilWizardAttackHazardElapsedTimer = 0f;
        evilWizardAttackHazardExplosionTriggered = false;
        evilWizardPreExplosionWarningPlayed = false;
        evilWizardSafeCircleCenters.clear();
    }

    private void updateEvilWizardHurtFromPlayer() {
        if (evilWizards.size == 0
                || !player.attacking
                || activeAttackAnim == null
                || isTeleportDashAnimation(activeAttackAnim)) {
            return;
        }
        float hitRange = playerBatHitRange();
        float hitRangeSq = hitRange * hitRange;
        for (int i = 0; i < evilWizards.size; i++) {
            EvilWizardInstance wizard = evilWizards.get(i);
            if (wizard.health <= 0f
                    || wizard.playerHitCooldownTimer > 0f
                    || wizard.state == EvilWizardAnimationState.HURT
                    || !isEvilWizardWithinPlayerAttackRange(wizard, hitRangeSq)) {
                continue;
            }
            applyHitToEvilWizardFromPlayer(wizard);
        }
    }

    private void applyHitToEvilWizardFromPlayer(EvilWizardInstance wizard) {
        if (wizard == null
                || wizard.health <= 0f
                || wizard.playerHitCooldownTimer > 0f
                || wizard.state == EvilWizardAnimationState.HURT
                || wizard.state == EvilWizardAnimationState.DIE) {
            return;
        }
        wizard.health = Math.max(0f, wizard.health - EVIL_WIZARD_HIT_DAMAGE);
        if (wizard.health <= 0f) {
            setEvilWizardState(wizard, EvilWizardAnimationState.DIE);
            wizard.playerHitCooldownTimer = 0f;
            return;
        }
        wizard.stateBeforeHurt = wizard.state;
        setEvilWizardState(wizard, EvilWizardAnimationState.HURT);
        wizard.playerHitCooldownTimer = EVIL_WIZARD_PLAYER_HIT_COOLDOWN_SECONDS;
    }

    private void updateGolem1HurtFromPlayer() {
        if (golem1s.size == 0
                || !player.attacking
                || activeAttackAnim == null
                || isTeleportDashAnimation(activeAttackAnim)) {
            return;
        }
        float hitRange = playerBatHitRange();
        float hitRangeSq = hitRange * hitRange;
        for (int i = 0; i < golem1s.size; i++) {
            Golem1Instance golem = golem1s.get(i);
            if (golem.health <= 0f
                    || golem.dying
                    || golem.playerHitCooldownTimer > 0f
                    || golem.hurting
                    || !isGolem1WithinPlayerAttackRange(golem, hitRangeSq)) {
                continue;
            }
            applyHitToGolem1FromPlayer(golem);
        }
    }

    private void applyHitToGolem1FromPlayer(Golem1Instance golem) {
        if (golem == null || golem.dying || golem.playerHitCooldownTimer > 0f) {
            return;
        }
        golem.health = Math.max(0f, golem.health - GOLEM_1_HIT_DAMAGE);
        golem.moving = false;
        golem.attacking = false;
        golem.attackStateTime = 0f;
        if (golem.health <= 0f) {
            startGolem1Dying(golem);
            golem.playerHitCooldownTimer = 0f;
            return;
        }
        if (golem1HurtAnim != null) {
            golem.hurting = true;
            golem.hurtStateTime = 0f;
        }
        golem.playerHitCooldownTimer = GOLEM_1_PLAYER_HIT_COOLDOWN_SECONDS;
    }

    private void updateBossArenaPhase2DemonHurtFromPlayer() {
        if (bossArenaPhase2Demons.size == 0
                || !player.attacking
                || activeAttackAnim == null
                || isTeleportDashAnimation(activeAttackAnim)) {
            return;
        }
        float hitRange = playerBatHitRange();
        float hitRangeSq = hitRange * hitRange;
        for (int i = 0; i < bossArenaPhase2Demons.size; i++) {
            BossArenaPhase2DemonInstance demon = bossArenaPhase2Demons.get(i);
            if (demon.hurting
                    || demon.playerHitCooldownTimer > 0f
                    || !isBossArenaPhase2DemonWithinPlayerAttackRange(demon, hitRangeSq)) {
                continue;
            }
            applyHitToBossArenaPhase2DemonFromPlayer(demon);
        }
    }

    private void applyHitToBossArenaPhase2DemonFromPlayer(BossArenaPhase2DemonInstance demon) {
        if (demon == null || demon.hurting || demon.playerHitCooldownTimer > 0f) {
            return;
        }
        demon.moving = false;
        demon.attacking = false;
        demon.attackStateTime = 0f;
        if (bossArenaPhase2DemonHurtAnim != null) {
            demon.hurting = true;
            demon.hurtStateTime = 0f;
        }
        demon.playerHitCooldownTimer = BOSS_ARENA_PHASE_2_DEMON_PLAYER_HIT_COOLDOWN_SECONDS;
    }

    private void startGolem1Dying(Golem1Instance golem) {
        if (golem == null || golem.dying) {
            return;
        }
        golem.health = 0f;
        golem.dying = true;
        golem.dieStateTime = 0f;
        golem.moving = false;
        golem.attacking = false;
        golem.attackStateTime = 0f;
        golem.hurting = false;
        golem.hurtStateTime = 0f;
        golem.attackCooldownTimer = 0f;
        golem.explosions.clear();
    }

    private boolean isGolem1WithinPlayerAttackRange(Golem1Instance golem, float hitRangeSq) {
        if (golem == null) {
            return false;
        }
        float playerCenterX = player.bounds.x + player.bounds.width * 0.5f;
        float playerCenterY = player.bounds.y + player.bounds.height * 0.5f;
        float centerX = golem1HitboxCenterX(golem);
        float centerY = golem1HitboxCenterY(golem);
        float halfWidth = golem1HitboxHalfWidth();
        float halfHeight = golem1HitboxHalfHeight();
        float left = centerX - halfWidth;
        float right = centerX + halfWidth;
        float bottom = centerY - halfHeight;
        float top = centerY + halfHeight;
        float closestX = Math.max(left, Math.min(playerCenterX, right));
        float closestY = Math.max(bottom, Math.min(playerCenterY, top));
        float dx = playerCenterX - closestX;
        float dy = playerCenterY - closestY;
        return (dx * dx + dy * dy) <= hitRangeSq;
    }

    private boolean isBossArenaPhase2DemonWithinPlayerAttackRange(BossArenaPhase2DemonInstance demon, float hitRangeSq) {
        if (demon == null) {
            return false;
        }
        float playerCenterX = player.bounds.x + player.bounds.width * 0.5f;
        float playerCenterY = player.bounds.y + player.bounds.height * 0.5f;
        float centerX = bossArenaPhase2DemonHitboxCenterX(demon);
        float centerY = bossArenaPhase2DemonHitboxCenterY(demon);
        float halfWidth = bossArenaPhase2DemonHitboxHalfWidth();
        float halfHeight = bossArenaPhase2DemonHitboxHalfHeight();
        float left = centerX - halfWidth;
        float right = centerX + halfWidth;
        float bottom = centerY - halfHeight;
        float top = centerY + halfHeight;
        float closestX = Math.max(left, Math.min(playerCenterX, right));
        float closestY = Math.max(bottom, Math.min(playerCenterY, top));
        float dx = playerCenterX - closestX;
        float dy = playerCenterY - closestY;
        return (dx * dx + dy * dy) <= hitRangeSq;
    }

    private float bossArenaPhase2DemonDetectionCenterX(BossArenaPhase2DemonInstance demon) {
        return demon != null ? demon.position.x : 0f;
    }

    private float bossArenaPhase2DemonDetectionCenterY(BossArenaPhase2DemonInstance demon) {
        if (demon == null) {
            return 0f;
        }
        return demon.position.y - BOSS_ARENA_PHASE_2_DETECTION_CENTER_DOWN_OFFSET_TILES * DEFAULT_TILE_SIZE;
    }

    private float bossArenaPhase2DemonHitboxCenterX(BossArenaPhase2DemonInstance demon) {
        if (demon == null) {
            return 0f;
        }
        float rightOffset = BOSS_ARENA_PHASE_2_DEMON_HITBOX_RIGHT_OFFSET_TILES * DEFAULT_TILE_SIZE;
        return demon.position.x + rightOffset;
    }

    private float bossArenaPhase2DemonHitboxCenterY(BossArenaPhase2DemonInstance demon) {
        if (demon == null) {
            return 0f;
        }
        float downOffset = BOSS_ARENA_PHASE_2_DEMON_HITBOX_DOWN_OFFSET_TILES * DEFAULT_TILE_SIZE;
        return demon.position.y - downOffset;
    }

    private boolean isEvilWizardWithinPlayerAttackRange(EvilWizardInstance wizard, float hitRangeSq) {
        if (wizard == null) {
            return false;
        }
        float playerCenterX = player.bounds.x + player.bounds.width * 0.5f;
        float playerCenterY = player.bounds.y + player.bounds.height * 0.5f;
        float centerX = evilWizardHitboxCenterX(wizard);
        float centerY = evilWizardHitboxCenterY(wizard);
        float halfWidth = evilWizardHitboxHalfWidth();
        float halfHeight = evilWizardHitboxHalfHeight();
        float left = centerX - halfWidth;
        float right = centerX + halfWidth;
        float bottom = centerY - halfHeight;
        float top = centerY + halfHeight;
        float closestX = Math.max(left, Math.min(playerCenterX, right));
        float closestY = Math.max(bottom, Math.min(playerCenterY, top));
        float dx = playerCenterX - closestX;
        float dy = playerCenterY - closestY;
        return (dx * dx + dy * dy) <= hitRangeSq;
    }

    private float evilWizardHitboxCenterX(EvilWizardInstance wizard) {
        return wizard != null ? wizard.position.x : 0f;
    }

    private float evilWizardHitboxCenterY(EvilWizardInstance wizard) {
        if (wizard == null) {
            return 0f;
        }
        float downOffset = EVIL_WIZARD_HITBOX_DOWN_OFFSET_TILES * DEFAULT_TILE_SIZE;
        return wizard.position.y - downOffset;
    }

    private float evilWizardHitboxHalfWidth() {
        float frameSize = evilWizardFrameSize() * EVIL_WIZARD_DRAW_SCALE;
        float hitboxSize = frameSize * EVIL_WIZARD_HITBOX_WIDTH_SCALE;
        return hitboxSize * 0.5f;
    }

    private float evilWizardHitboxHalfHeight() {
        float frameSize = evilWizardFrameSize() * EVIL_WIZARD_DRAW_SCALE;
        float hitboxSize = frameSize * EVIL_WIZARD_HITBOX_HEIGHT_SCALE;
        return hitboxSize * 0.5f;
    }

    private float golem1HitboxHalfWidth() {
        float frameSize = golem1FrameSize() * GOLEM_1_DRAW_SCALE;
        float hitboxSize = frameSize * GOLEM_1_HITBOX_WIDTH_SCALE;
        return hitboxSize * 0.5f;
    }

    private float golem1HitboxHalfHeight() {
        float frameSize = golem1FrameSize() * GOLEM_1_DRAW_SCALE;
        float hitboxSize = frameSize * GOLEM_1_HITBOX_HEIGHT_SCALE;
        return hitboxSize * 0.5f;
    }

    private float bossArenaPhase2DemonHitboxHalfWidth() {
        float frameWidth = bossArenaPhase2DemonFrameWidth() * BOSS_ARENA_PHASE_2_DEMON_DRAW_SCALE;
        float hitboxWidth = frameWidth * BOSS_ARENA_PHASE_2_DEMON_HITBOX_WIDTH_SCALE;
        return hitboxWidth * 0.5f;
    }

    private float bossArenaPhase2DemonHitboxHalfHeight() {
        float frameHeight = bossArenaPhase2DemonFrameHeight() * BOSS_ARENA_PHASE_2_DEMON_DRAW_SCALE;
        float hitboxHeight = frameHeight * BOSS_ARENA_PHASE_2_DEMON_HITBOX_HEIGHT_SCALE;
        return hitboxHeight * 0.5f;
    }

    private float golem1HitboxCenterX(Golem1Instance golem) {
        return golem != null ? golem.position.x : 0f;
    }

    private float golem1HitboxCenterY(Golem1Instance golem) {
        if (golem == null) {
            return 0f;
        }
        float downOffset = GOLEM_1_HITBOX_DOWN_OFFSET_TILES * DEFAULT_TILE_SIZE;
        return golem.position.y - downOffset;
    }

    private float evilWizardFrameSize() {
        if (evilWizardIdleTex != null) {
            return evilWizardIdleTex.getHeight();
        }
        if (evilWizardRunTex != null) {
            return evilWizardRunTex.getHeight();
        }
        if (evilWizardAttack1Tex != null) {
            return evilWizardAttack1Tex.getHeight();
        }
        if (evilWizardAttack2Tex != null) {
            return evilWizardAttack2Tex.getHeight();
        }
        if (evilWizardHurtTex != null) {
            return evilWizardHurtTex.getHeight();
        }
        return DEFAULT_TILE_SIZE;
    }

    private float golem1FrameSize() {
        if (golem1IdleTex != null) {
            return golem1IdleTex.getHeight();
        }
        if (golem1WalkTex != null) {
            return golem1WalkTex.getHeight();
        }
        if (golem1AttackTex != null) {
            return golem1AttackTex.getHeight();
        }
        if (golem1HurtTex != null) {
            return golem1HurtTex.getHeight();
        }
        if (golem1DieTex != null) {
            return golem1DieTex.getHeight();
        }
        return GOLEM_1_FRAME_HEIGHT;
    }

    private float bossArenaPhase2DemonFrameWidth() {
        TextureRegion referenceFrame = firstBossArenaPhase2DemonReferenceFrame();
        if (referenceFrame != null && referenceFrame.getRegionWidth() > 0) {
            return referenceFrame.getRegionWidth();
        }
        return BOSS_ARENA_PHASE_2_DEMON_FRAME_WIDTH;
    }

    private float bossArenaPhase2DemonFrameHeight() {
        TextureRegion referenceFrame = firstBossArenaPhase2DemonReferenceFrame();
        if (referenceFrame != null && referenceFrame.getRegionHeight() > 0) {
            return referenceFrame.getRegionHeight();
        }
        return BOSS_ARENA_PHASE_2_DEMON_FRAME_HEIGHT;
    }

    private TextureRegion firstBossArenaPhase2DemonReferenceFrame() {
        if (bossArenaPhase2DemonIdleAnim != null) {
            return bossArenaPhase2DemonIdleAnim.getKeyFrame(0f, true);
        }
        if (bossArenaPhase2DemonWalkAnim != null) {
            return bossArenaPhase2DemonWalkAnim.getKeyFrame(0f, true);
        }
        if (bossArenaPhase2DemonAttackAnim != null) {
            return bossArenaPhase2DemonAttackAnim.getKeyFrame(0f, false);
        }
        if (bossArenaPhase2DemonHurtAnim != null) {
            return bossArenaPhase2DemonHurtAnim.getKeyFrame(0f, false);
        }
        return null;
    }

    private void triggerRoom1BatsWakeUp() {
        if (bats.size == 0 || batWakeUpAnim == null) {
            return;
        }

        for (int i = 0; i < bats.size; i++) {
            BatInstance bat = bats.get(i);
            if (bat.state == BatAnimationState.SLEEPING) {
                bat.state = BatAnimationState.WAKING_UP;
                bat.stateTime = 0f;
            }
        }
    }

    private void updateBats(float delta) {
        if (bats.size == 0) {
            return;
        }

        for (int i = bats.size - 1; i >= 0; i--) {
            BatInstance bat = bats.get(i);
            bat.stateTime += delta;
            if (bat.playerHitCooldownTimer > 0f) {
                bat.playerHitCooldownTimer = Math.max(0f, bat.playerHitCooldownTimer - delta);
            }

            if (bat.state == BatAnimationState.DIE) {
                if (batDieAnim == null || batDieAnim.isAnimationFinished(bat.stateTime)) {
                    bats.removeIndex(i);
                }
                continue;
            }
            if (bat.state == BatAnimationState.HURT) {
                if (batHurtAnim == null || batHurtAnim.isAnimationFinished(bat.stateTime)) {
                    bat.state = bat.stateBeforeHurt;
                    bat.stateTime = 0f;
                }
                continue;
            }
            if (bat.state == BatAnimationState.WAKING_UP) {
                if (batWakeUpAnim != null && batWakeUpAnim.isAnimationFinished(bat.stateTime)) {
                    bat.state = BatAnimationState.IDLE_FLY;
                    bat.stateTime = 0f;
                }
                continue;
            }
            if (bat.state == BatAnimationState.IDLE_FLY) {
                if (bat.stateTime >= BAT_IDLE_FLY_TO_CHASE_SECONDS) {
                    bat.state = BatAnimationState.CHASING;
                    bat.stateTime = 0f;
                }
                continue;
            }
            if (bat.state == BatAnimationState.CHASING) {
                updateBatChaseMovement(bat, delta);
                if (isBatWithinAttackRange(bat)) {
                    startBatAttack(bat);
                }
                continue;
            }
            if (bat.state == BatAnimationState.ATTACKING) {
                Animation<TextureRegion> activeAttack = activeBatAttackAnimation(bat);
                if (activeAttack == null) {
                    bat.state = BatAnimationState.CHASING;
                    bat.stateTime = 0f;
                } else if (activeAttack.isAnimationFinished(bat.stateTime)) {
                    if (isBatWithinAttackRange(bat)) {
                        startBatAttack(bat);
                    } else {
                        bat.state = BatAnimationState.CHASING;
                        bat.stateTime = 0f;
                    }
                }
            }
        }
    }

    private void updateBatChaseMovement(BatInstance bat, float delta) {
        if (delta <= 0f || bat == null) {
            return;
        }

        float playerCenterX = player.bounds.x + player.bounds.width * 0.5f;
        float playerCenterY = player.bounds.y + player.bounds.height * 0.5f;
        float maxStep = BAT_CHASE_SPEED * delta;
        float stopDistanceSq = BAT_CHASE_STOP_DISTANCE * BAT_CHASE_STOP_DISTANCE;
        float dx = playerCenterX - bat.position.x;
        float dy = playerCenterY - bat.position.y;
        float distanceSq = dx * dx + dy * dy;
        if (distanceSq <= stopDistanceSq) {
            return;
        }
        float distance = (float) Math.sqrt(distanceSq);
        float step = Math.min(maxStep, Math.max(0f, distance - BAT_CHASE_STOP_DISTANCE));
        if (step <= 0f || distance <= 0f) {
            return;
        }
        bat.position.x += (dx / distance) * step;
        bat.position.y += (dy / distance) * step;
    }

    private boolean isBatWithinAttackRange(BatInstance bat) {
        if (bat == null) {
            return false;
        }
        float playerCenterX = player.bounds.x + player.bounds.width * 0.5f;
        float playerCenterY = player.bounds.y + player.bounds.height * 0.5f;
        float dx = playerCenterX - bat.position.x;
        float dy = playerCenterY - bat.position.y;
        return (dx * dx + dy * dy) <= (BAT_ATTACK_RANGE * BAT_ATTACK_RANGE);
    }

    private void startBatAttack(BatInstance bat) {
        if (bat == null) {
            return;
        }
        Animation<TextureRegion> attack1 = batAttack1Anim;
        Animation<TextureRegion> attack2 = batAttack2Anim;
        if (attack1 == null && attack2 == null) {
            return;
        }

        if (attack1 != null && attack2 != null) {
            bat.usingAttack1 = bat.useAttack1Next;
            bat.useAttack1Next = !bat.useAttack1Next;
        } else {
            bat.usingAttack1 = attack1 != null;
            bat.useAttack1Next = !bat.usingAttack1;
        }

        bat.state = BatAnimationState.ATTACKING;
        bat.stateTime = 0f;
    }

    private Animation<TextureRegion> activeBatAttackAnimation(BatInstance bat) {
        if (bat == null) {
            return null;
        }
        if (bat.usingAttack1) {
            return batAttack1Anim != null ? batAttack1Anim : batAttack2Anim;
        }
        return batAttack2Anim != null ? batAttack2Anim : batAttack1Anim;
    }

    private void updateBatHurtFromPlayer() {
        if (bats.size == 0
                || !player.attacking
                || activeAttackAnim == null
                || isTeleportDashAnimation(activeAttackAnim)) {
            return;
        }

        float hitRange = playerBatHitRange();
        float hitRangeSq = hitRange * hitRange;
        for (int i = 0; i < bats.size; i++) {
            BatInstance bat = bats.get(i);
            if (bat.state == BatAnimationState.DIE
                    || bat.health <= 0f
                    || bat.playerHitCooldownTimer > 0f
                    || !isBatWithinPlayerAttackRange(bat, hitRangeSq)) {
                continue;
            }
            applyDamageToBatFromPlayer(bat);
        }
    }

    private void applyDamageToBatFromPlayer(BatInstance bat) {
        if (bat == null
                || bat.state == BatAnimationState.DIE
                || bat.health <= 0f
                || bat.playerHitCooldownTimer > 0f) {
            return;
        }
        bat.health = Math.max(0f, bat.health - BAT_HIT_DAMAGE);
        if (batHurtSfx != null) {
            batHurtSfx.play(BAT_HURT_SFX_VOLUME);
        }
        if (bat.health <= 0f) {
            bat.state = BatAnimationState.DIE;
            bat.stateTime = 0f;
            bat.playerHitCooldownTimer = 0f;
            return;
        }
        if (batHurtAnim != null) {
            if (bat.state != BatAnimationState.HURT) {
                bat.stateBeforeHurt = bat.state;
            }
            bat.state = BatAnimationState.HURT;
            bat.stateTime = 0f;
        }
        bat.playerHitCooldownTimer = BAT_PLAYER_HIT_COOLDOWN_SECONDS;
    }

    private boolean isBatWithinPlayerAttackRange(BatInstance bat, float hitRangeSq) {
        if (bat == null) {
            return false;
        }
        float playerCenterX = player.bounds.x + player.bounds.width * 0.5f;
        float playerCenterY = player.bounds.y + player.bounds.height * 0.5f;
        float dx = playerCenterX - bat.position.x;
        float dy = playerCenterY - bat.position.y;
        return (dx * dx + dy * dy) <= hitRangeSq;
    }

    private float playerBatHitRange() {
        if (isTeleportDashAnimation(activeAttackAnim)) {
            return playerDashAttackHitRadius();
        }
        float leftClickBaseRange = BAT_PLAYER_HIT_RANGE;
        if (activeAttackAnim == attack3Anim) {
            leftClickBaseRange += 4f;
        }
        return leftClickBaseRange * PLAYER_LEFT_CLICK_HIT_RADIUS_SCALE;
    }

    private float playerDashAttackHitRadius() {
        float baseRadius = (BAT_PLAYER_HIT_RANGE + 8f) * PLAYER_DASH_ATTACK_HIT_HEIGHT_SCALE;
        if (activeAttackAnim == dashAttackAnim) {
            return baseRadius * PLAYER_E_ATTACK_HIT_RADIUS_SCALE;
        }
        return baseRadius;
    }

    private void updateRoom1DoorAnimation(float delta) {
        if (!MAP_ID_DEMON_CASTLE.equals(world.activeMapId)
                || room1DoorFrameCount() == 0) {
            return;
        }

        if (!room1DoorBattleCompleted
                && room1DoorClosed
                && !room1DoorClosing
                && !room1DoorOpening
                && room1DoorAutoOpenPending
                && bats.size == 0) {
            startRoom1DoorOpening();
        }

        if (!room1DoorBattleCompleted
                && !room1DoorClosed
                && !room1DoorClosing
                && !room1DoorOpening
                && room1DoorPauseTimer <= 0f
                && isPlayerWithinObjectLayer(room1DoorTriggerLayer)) {
            stopPlayerForDoorAction();
            room1DoorPauseTimer = ROOM1_DOOR_TRIGGER_PAUSE_SECONDS;
        }

        if (room1DoorPauseTimer > 0f) {
            room1DoorPauseTimer = Math.max(0f, room1DoorPauseTimer - Math.max(0f, delta));
            if (room1DoorPauseTimer > 0f) {
                return;
            }
            startRoom1DoorClosing();
        }

        if (room1DoorClosing) {
            room1DoorAnimationTimer += Math.max(0f, delta);
            while (room1DoorAnimationTimer >= ROOM1_DOOR_FRAME_DURATION_SECONDS) {
                room1DoorAnimationTimer -= ROOM1_DOOR_FRAME_DURATION_SECONDS;
                int nextIndex = room1DoorAnimationFrameIndex - 1;
                if (nextIndex < 0) {
                    room1DoorAnimationFrameIndex = 0;
                    setRoom1DoorFrameVisible(room1DoorAnimationFrameIndex);
                    room1DoorClosing = false;
                    room1DoorClosed = true;
                    room1DoorCollisionEnabled = hasRoom1DoorCollisionBounds();
                    room1DoorAutoOpenPending = true;
                    triggerRoom1BatsWakeUp();
                    room1DoorAnimationTimer = 0f;
                    break;
                }
                room1DoorAnimationFrameIndex = nextIndex;
                setRoom1DoorFrameVisible(room1DoorAnimationFrameIndex);
                if (room1DoorAnimationFrameIndex == 0) {
                    room1DoorClosing = false;
                    room1DoorClosed = true;
                    room1DoorCollisionEnabled = hasRoom1DoorCollisionBounds();
                    room1DoorAutoOpenPending = true;
                    triggerRoom1BatsWakeUp();
                    room1DoorAnimationTimer = 0f;
                    break;
                }
            }
        }

        if (room1DoorOpening) {
            int frameCount = room1DoorFrameCount();
            room1DoorAnimationTimer += Math.max(0f, delta);
            while (room1DoorAnimationTimer >= ROOM1_DOOR_FRAME_DURATION_SECONDS) {
                room1DoorAnimationTimer -= ROOM1_DOOR_FRAME_DURATION_SECONDS;
                int nextIndex = room1DoorAnimationFrameIndex + 1;
                if (nextIndex >= frameCount) {
                    room1DoorAnimationFrameIndex = frameCount - 1;
                    setRoom1DoorFrameVisible(room1DoorAnimationFrameIndex);
                    room1DoorOpening = false;
                    room1DoorClosed = false;
                    room1DoorAutoOpenPending = false;
                    room1DoorBattleCompleted = true;
                    room1DoorAnimationTimer = 0f;
                    break;
                }
                room1DoorAnimationFrameIndex = nextIndex;
                setRoom1DoorFrameVisible(room1DoorAnimationFrameIndex);
                if (room1DoorAnimationFrameIndex == frameCount - 1) {
                    room1DoorOpening = false;
                    room1DoorClosed = false;
                    room1DoorAutoOpenPending = false;
                    room1DoorBattleCompleted = true;
                    room1DoorAnimationTimer = 0f;
                    break;
                }
            }
        }
    }

    private void startRoom1DoorClosing() {
        int frameCount = room1DoorFrameCount();
        if (frameCount == 0 || room1DoorClosed) {
            return;
        }
        room1DoorOpening = false;
        room1DoorClosing = true;
        room1DoorAnimationTimer = 0f;
        room1DoorAutoOpenPending = false;
        room1DoorCollisionEnabled = false;
        room1DoorAnimationFrameIndex = Math.min(
                Math.max(room1DoorAnimationFrameIndex, 0),
                frameCount - 1);
        setRoom1DoorFrameVisible(room1DoorAnimationFrameIndex);
    }

    private void startRoom1DoorOpening() {
        int frameCount = room1DoorFrameCount();
        if (frameCount == 0 || !room1DoorClosed) {
            return;
        }
        room1DoorOpening = true;
        room1DoorClosing = false;
        room1DoorAnimationTimer = 0f;
        room1DoorCollisionEnabled = false;
        room1DoorAnimationFrameIndex = Math.min(
                Math.max(room1DoorAnimationFrameIndex, 0),
                frameCount - 1);
        setRoom1DoorFrameVisible(room1DoorAnimationFrameIndex);
    }

    private void updateFirstBossDoorAnimation(float delta) {
        if (!MAP_ID_DEMON_CASTLE.equals(world.activeMapId)
                || firstBossDoorFrameCount() == 0) {
            return;
        }

        boolean playerInTrigger = isPlayerWithinFirstBossDoorTrigger();
        if (!firstBossDoorTriggerPrimed) {
            // Ignore the very first sample so spawn placement does not auto-trigger the door.
            firstBossDoorTriggerPrimed = true;
            wasPlayerWithinFirstBossDoorTrigger = playerInTrigger;
        } else if (!firstBossDoorTriggered
                && !firstBossDoorClosed
                && !firstBossDoorClosing
                && firstBossDoorPauseTimer <= 0f
                && playerInTrigger
                && !wasPlayerWithinFirstBossDoorTrigger) {
            stopPlayerForDoorAction();
            firstBossDoorPauseTimer = FIRST_BOSS_DOOR_TRIGGER_PAUSE_SECONDS;
            firstBossDoorTriggered = true;
            startBoss1IntroSequence();
        }
        wasPlayerWithinFirstBossDoorTrigger = playerInTrigger;

        if (firstBossDoorPauseTimer > 0f) {
            firstBossDoorPauseTimer = Math.max(0f, firstBossDoorPauseTimer - Math.max(0f, delta));
            if (firstBossDoorPauseTimer > 0f) {
                return;
            }
            startFirstBossDoorClosing();
        }

        if (!firstBossDoorClosing) {
            return;
        }

        firstBossDoorAnimationTimer += Math.max(0f, delta);
        if (firstBossDoorAnimationTimer < FIRST_BOSS_DOOR_FRAME_DURATION_SECONDS) {
            return;
        }
        firstBossDoorAnimationTimer = 0f;

        int frameCount = firstBossDoorFrameCount();
        int nextIndex = firstBossDoorAnimationFrameIndex + 1;
        if (nextIndex >= frameCount) {
            firstBossDoorAnimationFrameIndex = frameCount - 1;
            setFirstBossDoorFrameVisible(firstBossDoorAnimationFrameIndex);
            firstBossDoorClosing = false;
            firstBossDoorClosed = true;
            firstBossDoorCollisionEnabled = hasFirstBossDoorCollisionBounds();
            return;
        }
        firstBossDoorAnimationFrameIndex = nextIndex;
        setFirstBossDoorFrameVisible(firstBossDoorAnimationFrameIndex);
        if (firstBossDoorAnimationFrameIndex == frameCount - 1) {
            firstBossDoorClosing = false;
            firstBossDoorClosed = true;
            firstBossDoorCollisionEnabled = hasFirstBossDoorCollisionBounds();
        }
    }

    private void updateBoss1DefeatDoorAnimation(float delta) {
        if (!MAP_ID_DEMON_CASTLE.equals(world.activeMapId)
                || boss1DefeatDoorFrameCount() == 0) {
            return;
        }

        float safeDelta = Math.max(0f, delta);
        if (!boss1DefeatDoorTriggered && isBoss1Defeated()) {
            boss1DefeatDoorTriggered = true;
            boss1DefeatDoorOpenDelayTimer = BOSS_1_DEFEAT_DOOR_OPEN_DELAY_SECONDS;
        }
        if (!boss1DefeatDoorOpening && boss1DefeatDoorTriggered && !boss1DefeatDoorOpened) {
            boss1DefeatDoorOpenDelayTimer = Math.max(0f, boss1DefeatDoorOpenDelayTimer - safeDelta);
            if (boss1DefeatDoorOpenDelayTimer <= 0f) {
                startBoss1DefeatDoorOpening();
            }
        }
        if (!boss1DefeatDoorOpening) {
            return;
        }

        boss1DefeatDoorAnimationTimer += safeDelta;
        while (boss1DefeatDoorAnimationTimer >= BOSS_1_DEFEAT_DOOR_FRAME_DURATION_SECONDS) {
            boss1DefeatDoorAnimationTimer -= BOSS_1_DEFEAT_DOOR_FRAME_DURATION_SECONDS;
            int nextIndex = boss1DefeatDoorAnimationFrameIndex + 1;
            if (nextIndex >= boss1DefeatDoorFrameCount()) {
                boss1DefeatDoorAnimationFrameIndex = boss1DefeatDoorFrameCount() - 1;
                setBoss1DefeatDoorFrameVisible(boss1DefeatDoorAnimationFrameIndex);
                boss1DefeatDoorOpening = false;
                boss1DefeatDoorOpened = true;
                break;
            }
            boss1DefeatDoorAnimationFrameIndex = nextIndex;
            setBoss1DefeatDoorFrameVisible(boss1DefeatDoorAnimationFrameIndex);
            if (boss1DefeatDoorAnimationFrameIndex == boss1DefeatDoorFrameCount() - 1) {
                boss1DefeatDoorOpening = false;
                boss1DefeatDoorOpened = true;
                break;
            }
        }
    }

    private void resetBoss1IntroState() {
        boss1IntroPhase = Boss1IntroPhase.NONE;
        boss1IntroTimer = 0f;
        boss1IntroPlayed = false;
        boss1IntroCameraTarget.set(0f, 0f);
    }

    private void startBoss1IntroSequence() {
        if (!MAP_ID_DEMON_CASTLE.equals(world.activeMapId) || boss1IntroPlayed) {
            return;
        }
        boss1IntroPlayed = true;
        boss1IntroPhase = Boss1IntroPhase.STEP_DOWN;
        boss1IntroTimer = BOSS_1_INTRO_STEP_DOWN_SECONDS;
        stopPlayerForDoorAction();
        player.moving = false;
    }

    private void updateBoss1IntroSequence(float delta) {
        if (!isBoss1IntroActive() || camera == null) {
            return;
        }
        float safeDelta = Math.max(0f, delta);
        float playerTargetX = player.bounds.x + player.bounds.width * 0.5f;
        float playerTargetY = player.bounds.y + player.bounds.height * 0.5f;
        if (boss1IntroPhase == Boss1IntroPhase.STEP_DOWN) {
            float step = PLAYER_SPEED * safeDelta;
            tryMove(0f, -step);
            player.lastMoveX = 0f;
            player.lastMoveY = -1f;
            player.moving = true;
            smoothCameraToward(playerTargetX, playerTargetY, safeDelta);
            boss1IntroTimer = Math.max(0f, boss1IntroTimer - safeDelta);
            if (boss1IntroTimer <= 0f) {
                player.moving = false;
                boss1IntroPhase = Boss1IntroPhase.HOLD_IDLE;
                boss1IntroTimer = BOSS_1_INTRO_IDLE_SECONDS;
            }
            return;
        }
        if (boss1IntroPhase == Boss1IntroPhase.HOLD_IDLE) {
            smoothCameraToward(playerTargetX, playerTargetY, safeDelta);
            boss1IntroTimer = Math.max(0f, boss1IntroTimer - safeDelta);
            if (boss1IntroTimer <= 0f) {
                if (resolveBoss1IntroCameraTarget()) {
                    boss1IntroPhase = Boss1IntroPhase.FOCUS_WIZARD;
                    boss1IntroTimer = BOSS_1_INTRO_CAMERA_FOCUS_SECONDS;
                } else {
                    boss1IntroPhase = Boss1IntroPhase.RETURN_TO_PLAYER;
                    boss1IntroTimer = BOSS_1_INTRO_CAMERA_RETURN_SECONDS;
                }
            }
            return;
        }
        if (boss1IntroPhase == Boss1IntroPhase.FOCUS_WIZARD) {
            smoothCameraToward(boss1IntroCameraTarget.x, boss1IntroCameraTarget.y, safeDelta);
            boss1IntroTimer = Math.max(0f, boss1IntroTimer - safeDelta);
            if (boss1IntroTimer <= 0f) {
                boss1IntroPhase = Boss1IntroPhase.RETURN_TO_PLAYER;
                boss1IntroTimer = BOSS_1_INTRO_CAMERA_RETURN_SECONDS;
            }
            return;
        }
        if (boss1IntroPhase == Boss1IntroPhase.RETURN_TO_PLAYER) {
            smoothCameraToward(playerTargetX, playerTargetY, safeDelta);
            boss1IntroTimer = Math.max(0f, boss1IntroTimer - safeDelta);
            if (boss1IntroTimer <= 0f) {
                boss1IntroPhase = Boss1IntroPhase.NONE;
                camera.position.set(playerTargetX, playerTargetY, 0f);
            }
        }
    }

    private boolean resolveBoss1IntroCameraTarget() {
        for (int i = 0; i < evilWizards.size; i++) {
            EvilWizardInstance wizard = evilWizards.get(i);
            if (wizard == null || wizard.health <= 0f || wizard.state == EvilWizardAnimationState.DIE) {
                continue;
            }
            if (isWizardInsideBoss1Zone(wizard)) {
                boss1IntroCameraTarget.set(wizard.position);
                return true;
            }
        }
        if (bossFightZoneBounds.width > 0f && bossFightZoneBounds.height > 0f) {
            boss1IntroCameraTarget.set(
                    bossFightZoneBounds.x + bossFightZoneBounds.width * 0.5f,
                    bossFightZoneBounds.y + bossFightZoneBounds.height * 0.5f);
            return true;
        }
        return false;
    }

    private boolean isWizardInsideBoss1Zone(EvilWizardInstance wizard) {
        if (wizard == null || bossFightZoneBounds.width <= 0f || bossFightZoneBounds.height <= 0f) {
            return false;
        }
        if (hasValidBossZoneBounds(wizard.zoneBounds)) {
            return Intersector.overlaps(wizard.zoneBounds, bossFightZoneBounds);
        }
        return bossFightZoneBounds.contains(wizard.position.x, wizard.position.y);
    }

    private void smoothCameraToward(float targetX, float targetY, float delta) {
        float alpha = 1f - (float) Math.exp(-TELEPORT_CAMERA_SMOOTH_SPEED * Math.max(0f, delta));
        camera.position.x = lerp(camera.position.x, targetX, alpha);
        camera.position.y = lerp(camera.position.y, targetY, alpha);
        camera.position.z = 0f;
    }

    private void updateBossFightZoneDarkMode(float delta) {
        if (!MAP_ID_DEMON_CASTLE.equals(world.activeMapId) || bossFightZoneLayer == null) {
            return;
        }
        if (isBoss1Defeated()) {
            bossFightZoneDarkMode = false;
            bossFightZoneDarkTransitionTimer = 0f;
            return;
        }
        if (!bossFightZoneDarkMode && isPlayerWithinObjectLayer(bossFightZoneLayer)) {
            bossFightZoneDarkMode = true;
        }
        if (!boss1IntroPlayed
                && !isBoss1IntroActive()
                && isPlayerWithinObjectLayer(bossFightZoneLayer)) {
            startBoss1IntroSequence();
        }
        if (!bossFightZoneDarkMode) {
            return;
        }
        if (BOSS_FIGHT_ZONE_DARK_TRANSITION_SECONDS <= 0f) {
            bossFightZoneDarkTransitionTimer = 1f;
            return;
        }
        bossFightZoneDarkTransitionTimer = Math.min(
                BOSS_FIGHT_ZONE_DARK_TRANSITION_SECONDS,
                bossFightZoneDarkTransitionTimer + Math.max(0f, delta));
    }

    private void updateGolemFightZoneDarkMode(float delta) {
        if (!MAP_ID_DEMON_CASTLE.equals(world.activeMapId) || golemFightZoneLayer == null) {
            return;
        }
        if (isGolemFightCleared()) {
            golemFightZoneDarkMode = false;
            golemFightZoneDarkTransitionTimer = 0f;
            return;
        }
        boolean playerInside = isPlayerWithinObjectLayer(golemFightZoneLayer);
        if (!playerInside) {
            golemFightZoneDarkMode = false;
            golemFightZoneDarkTransitionTimer = 0f;
            return;
        }
        golemFightZoneDarkMode = true;
        if (BOSS_FIGHT_ZONE_DARK_TRANSITION_SECONDS <= 0f) {
            golemFightZoneDarkTransitionTimer = 1f;
            return;
        }
        golemFightZoneDarkTransitionTimer = Math.min(
                BOSS_FIGHT_ZONE_DARK_TRANSITION_SECONDS,
                golemFightZoneDarkTransitionTimer + Math.max(0f, delta));
    }

    private boolean isBoss1Defeated() {
        return !hasLivingEvilWizardInZone(bossFightZoneBounds);
    }

    private boolean isBoss2Defeated() {
        Rectangle boss2ZoneBounds = resolveBossWizardZoneBounds(BOSS_2_GROUP_LAYER_NAME);
        return !hasLivingEvilWizardInZone(boss2ZoneBounds);
    }

    private boolean isGolemFightCleared() {
        return !hasLivingGolem1InZone(golemFightZoneBounds);
    }

    private boolean isAllBossesDefeatedForChestReward() {
        return isBoss1Defeated()
                && isBoss2Defeated()
                && isGolemFightCleared();
    }

    private boolean hasLivingEvilWizardInZone(Rectangle zoneBounds) {
        if (zoneBounds == null || zoneBounds.width <= 0f || zoneBounds.height <= 0f) {
            return false;
        }
        for (int i = 0; i < evilWizards.size; i++) {
            EvilWizardInstance wizard = evilWizards.get(i);
            if (wizard == null || wizard.health <= 0f) {
                continue;
            }
            if (hasValidBossZoneBounds(wizard.zoneBounds)) {
                if (Intersector.overlaps(wizard.zoneBounds, zoneBounds)) {
                    return true;
                }
                continue;
            }
            if (zoneBounds.contains(wizard.position.x, wizard.position.y)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasLivingGolem1InZone(Rectangle zoneBounds) {
        if (zoneBounds == null || zoneBounds.width <= 0f || zoneBounds.height <= 0f) {
            return false;
        }
        for (int i = 0; i < golem1s.size; i++) {
            Golem1Instance golem = golem1s.get(i);
            if (golem == null || golem.health <= 0f || golem.dying) {
                continue;
            }
            if (hasValidBossZoneBounds(golem.zoneBounds)) {
                if (Intersector.overlaps(golem.zoneBounds, zoneBounds)) {
                    return true;
                }
                continue;
            }
            if (zoneBounds.contains(golem.position.x, golem.position.y)) {
                return true;
            }
        }
        return false;
    }

    private boolean isBossFightZoneCollisionActive() {
        if (!bossFightZoneDarkMode
                || bossFightZoneBounds.width <= 0f
                || bossFightZoneBounds.height <= 0f) {
            return false;
        }
        if (BOSS_FIGHT_ZONE_DARK_TRANSITION_SECONDS <= 0f) {
            return true;
        }
        return bossFightZoneDarkMode
                && bossFightZoneDarkTransitionTimer >= BOSS_FIGHT_ZONE_DARK_TRANSITION_SECONDS;
    }

    private boolean isWithinBossFightZone(Rectangle rect) {
        if (rect == null) {
            return false;
        }
        float centerX = rect.x + rect.width * 0.5f;
        float centerY = rect.y + rect.height * 0.5f;
        return bossFightZoneBounds.contains(centerX, centerY);
    }

    private void startFirstBossDoorClosing() {
        int frameCount = firstBossDoorFrameCount();
        if (frameCount == 0 || firstBossDoorClosed) {
            return;
        }
        firstBossDoorClosing = true;
        firstBossDoorAnimationTimer = 0f;
        firstBossDoorCollisionEnabled = false;
        // First-boss trigger should play from Door-1 up to the last frame.
        firstBossDoorAnimationFrameIndex = 0;
        setFirstBossDoorFrameVisible(firstBossDoorAnimationFrameIndex);
    }

    private void startBoss1DefeatDoorOpening() {
        int frameCount = boss1DefeatDoorFrameCount();
        if (frameCount == 0 || boss1DefeatDoorOpened) {
            return;
        }
        boss1DefeatDoorOpening = true;
        boss1DefeatDoorAnimationTimer = 0f;
        boss1DefeatDoorOpenDelayTimer = 0f;
        boss1DefeatDoorAnimationFrameIndex = 0;
        setBoss1DefeatDoorFrameVisible(boss1DefeatDoorAnimationFrameIndex);
    }

    private boolean isPlayerWithinFirstBossDoorTrigger() {
        return isPlayerWithinObjectLayer(firstBossDoorTriggerLayer);
    }

    private boolean isPlayerWithinFirstBossDoorTriggerFallback() {
        if (firstBossDoorTriggerLayer == null) {
            return false;
        }
        float mapPixelHeight = activeMapPixelHeight();
        float recursiveOffsetX = layerOffsetX(firstBossDoorTriggerLayer);
        float recursiveOffsetY = layerOffsetY(firstBossDoorTriggerLayer);
        float renderOffsetX = firstBossDoorTriggerLayer.getRenderOffsetX();
        float renderOffsetY = firstBossDoorTriggerLayer.getRenderOffsetY();
        for (MapObject object : firstBossDoorTriggerLayer.getObjects()) {
            Rectangle localBounds = objectLocalBounds(object);
            if (localBounds == null) {
                continue;
            }
            if (overlapsPlayerStrict(localBounds)) {
                return true;
            }
            Rectangle recursiveBounds = new Rectangle(
                    localBounds.x + recursiveOffsetX,
                    localBounds.y + recursiveOffsetY,
                    localBounds.width,
                    localBounds.height);
            if (overlapsPlayerStrict(recursiveBounds)) {
                return true;
            }
            Rectangle renderBounds = new Rectangle(
                    localBounds.x + renderOffsetX,
                    localBounds.y + renderOffsetY,
                    localBounds.width,
                    localBounds.height);
            if (overlapsPlayerStrict(renderBounds)) {
                return true;
            }
            if (mapPixelHeight > 0f) {
                Rectangle flippedLocalBounds = new Rectangle(
                        localBounds.x,
                        mapPixelHeight - localBounds.y - localBounds.height,
                        localBounds.width,
                        localBounds.height);
                if (overlapsPlayerStrict(flippedLocalBounds)) {
                    return true;
                }
                Rectangle flippedRecursiveBounds = new Rectangle(
                        flippedLocalBounds.x + recursiveOffsetX,
                        flippedLocalBounds.y + recursiveOffsetY,
                        flippedLocalBounds.width,
                        flippedLocalBounds.height);
                if (overlapsPlayerStrict(flippedRecursiveBounds)) {
                    return true;
                }
                Rectangle flippedRenderBounds = new Rectangle(
                        flippedLocalBounds.x + renderOffsetX,
                        flippedLocalBounds.y + renderOffsetY,
                        flippedLocalBounds.width,
                        flippedLocalBounds.height);
                if (overlapsPlayerStrict(flippedRenderBounds)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean overlapsPlayerStrict(Rectangle bounds) {
        if (bounds == null) {
            return false;
        }
        if (bounds.width <= 0f && bounds.height <= 0f) {
            return player.bounds.contains(bounds.x, bounds.y);
        }
        return Intersector.overlaps(player.bounds, bounds);
    }

    private float activeMapPixelHeight() {
        if (world.map == null) {
            return 0f;
        }
        MapProperties properties = world.map.getProperties();
        Integer mapHeightTiles = properties.get("height", Integer.class);
        Integer tileHeight = properties.get("tileheight", Integer.class);
        if (mapHeightTiles == null || mapHeightTiles <= 0 || tileHeight == null || tileHeight <= 0) {
            return 0f;
        }
        return mapHeightTiles * tileHeight;
    }

    private boolean isPlayerNearFirstBossDoorByAxis() {
        Rectangle bounds = firstBossDoorTriggerBounds;
        if (bounds.width <= 0f || bounds.height <= 0f) {
            return false;
        }
        float playerCenterX = player.bounds.x + player.bounds.width * 0.5f;
        float playerCenterY = player.bounds.y + player.bounds.height * 0.5f;
        float xMargin = Math.max(24f, bounds.width * 0.2f);
        boolean withinX = playerCenterX >= bounds.x - xMargin
                && playerCenterX <= bounds.x + bounds.width + xMargin;
        if (!withinX) {
            return false;
        }
        float yMargin = 700f;
        boolean nearPrimaryY = playerCenterY >= bounds.y - yMargin
                && playerCenterY <= bounds.y + bounds.height + yMargin;
        if (nearPrimaryY) {
            return true;
        }
        float mapPixelHeight = activeMapPixelHeight();
        if (mapPixelHeight <= 0f) {
            return false;
        }
        float mirroredY = mapPixelHeight - playerCenterY;
        return mirroredY >= bounds.y - yMargin
                && mirroredY <= bounds.y + bounds.height + yMargin;
    }

    private boolean overlapsPlayerExpanded(Rectangle bounds) {
        if (bounds == null) {
            return false;
        }
        float expandX = 8f;
        float expandDown = 96f;
        float expandUp = 16f;
        Rectangle expanded = new Rectangle(
                bounds.x - expandX,
                bounds.y - expandDown,
                bounds.width + expandX * 2f,
                bounds.height + expandDown + expandUp);
        if (expanded.width <= 0f && expanded.height <= 0f) {
            return player.bounds.contains(expanded.x, expanded.y);
        }
        return Intersector.overlaps(player.bounds, expanded);
    }

    private boolean hasRoom1DoorCollisionBounds() {
        return room1DoorClosedCollisionBounds.width > 0f && room1DoorClosedCollisionBounds.height > 0f;
    }

    private boolean hasFirstBossDoorCollisionBounds() {
        return firstBossDoorClosedCollisionBounds.width > 0f && firstBossDoorClosedCollisionBounds.height > 0f;
    }

    private int room1DoorFrameCount() {
        return Math.max(room1DoorAnimationFrames.size, room1DoorFrontAnimationFrames.size);
    }

    private int firstBossDoorFrameCount() {
        return Math.max(firstBossDoorAnimationFrames.size, firstBossDoorFrontAnimationFrames.size);
    }

    private int boss1DefeatDoorFrameCount() {
        return boss1DefeatDoorAnimationFrames.size;
    }

    private void setRoom1DoorFrameVisible(int frameIndex) {
        setDoorFrameArrayVisible(room1DoorAnimationFrames, frameIndex);
        setDoorFrameArrayVisible(room1DoorFrontAnimationFrames, frameIndex);
    }

    private void setFirstBossDoorFrameVisible(int frameIndex) {
        setDoorFrameArrayVisible(firstBossDoorAnimationFrames, frameIndex);
        setDoorFrameArrayVisible(firstBossDoorFrontAnimationFrames, frameIndex);
    }

    private void setBoss1DefeatDoorFrameVisible(int frameIndex) {
        setDoorFrameArrayVisible(boss1DefeatDoorAnimationFrames, frameIndex);
    }

    private int entranceDoorFrameCount() {
        return Math.max(entranceDoorAnimationFrames.size, entranceDoorFrontAnimationFrames.size);
    }

    private void setEntranceDoorFrameVisible(int frameIndex) {
        setDoorFrameArrayVisible(entranceDoorAnimationFrames, frameIndex);
        setDoorFrameArrayVisible(entranceDoorFrontAnimationFrames, frameIndex);
    }

    private void setDoorFrameArrayVisible(Array<MapLayer> frames, int frameIndex) {
        for (int i = 0; i < frames.size; i++) {
            frames.get(i).setVisible(i == frameIndex);
        }
    }

    private int findVisibleDoorFrameIndex(Array<MapLayer> frames) {
        for (int i = 0; i < frames.size; i++) {
            if (frames.get(i).isVisible()) {
                return i;
            }
        }
        return -1;
    }

    private void stopPlayerForDoorAction() {
        player.attacking = false;
        player.attackTimer = 0f;
        player.dashing = false;
        player.dashTimer = 0f;
        player.dashAttackActive = false;
        player.dashAttackMoveDuration = 0f;
        player.dashAttackFollowupQueued = false;
        player.moving = false;
        cameraTeleportSmoothTimer = 0f;
        postDashMovementLockTimer = 0f;
        cameraTeleportTargetActive = false;
        dashAttackPlannedPositionValid = false;
        clearDashAttackSweepState();
        clearPlayerHazardHitReaction();
        activeAttackAnim = null;
    }

    private void appendDoorFramesFromGroup(MapLayer layer, Array<MapLayer> outFrames) {
        if (!(layer instanceof MapGroupLayer doorGroup)) {
            return;
        }

        MapLayer[] orderedFrames = new MapLayer[16];
        int maxFrame = 0;
        for (MapLayer frameLayer : doorGroup.getLayers()) {
            int frameNumber = parseDoorFrameNumber(frameLayer.getName());
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
            if (frameLayer != null) {
                outFrames.add(frameLayer);
            }
        }
    }

    private boolean hasPlayerEnteredThroughEntranceDoor() {
        if (entranceDoorTriggerBounds.width <= 0f || entranceDoorTriggerBounds.height <= 0f) {
            return !isPlayerWithinObjectLayer(entranceDoorTriggerLayer);
        }
        float playerCenterX = player.bounds.x + player.bounds.width / 2f;
        float left = entranceDoorTriggerBounds.x;
        float right = left + entranceDoorTriggerBounds.width;
        if (entranceDoorEntrySide < 0) {
            return playerCenterX > right + ENTRANCE_DOOR_ENTRY_CROSS_MARGIN;
        }
        if (entranceDoorEntrySide > 0) {
            return playerCenterX < left - ENTRANCE_DOOR_ENTRY_CROSS_MARGIN;
        }
        return !isPlayerWithinObjectLayer(entranceDoorTriggerLayer);
    }

    private int inferEntranceDoorEntrySide() {
        if (entranceDoorTriggerBounds.width <= 0f || entranceDoorTriggerBounds.height <= 0f) {
            if (player.lastMoveX < 0f) {
                return 1;
            }
            return -1;
        }
        float playerCenterX = player.bounds.x + player.bounds.width / 2f;
        float triggerCenterX = entranceDoorTriggerBounds.x + entranceDoorTriggerBounds.width / 2f;
        if (playerCenterX < triggerCenterX) {
            return -1;
        }
        if (playerCenterX > triggerCenterX) {
            return 1;
        }
        if (player.lastMoveX < 0f) {
            return 1;
        }
        return -1;
    }

    private Rectangle objectLayerBounds(MapLayer layer) {
        if (layer == null || layer.getObjects().getCount() == 0) {
            return null;
        }
        Rectangle bounds = null;
        float minX = 0f;
        float minY = 0f;
        float maxX = 0f;
        float maxY = 0f;
        for (MapObject object : layer.getObjects()) {
            Rectangle rect = objectBounds(layer, object);
            if (rect == null || rect.width <= 0f || rect.height <= 0f) {
                continue;
            }
            if (bounds == null) {
                bounds = new Rectangle(rect);
                minX = rect.x;
                minY = rect.y;
                maxX = rect.x + rect.width;
                maxY = rect.y + rect.height;
                continue;
            }
            minX = Math.min(minX, rect.x);
            minY = Math.min(minY, rect.y);
            maxX = Math.max(maxX, rect.x + rect.width);
            maxY = Math.max(maxY, rect.y + rect.height);
        }
        if (bounds == null) {
            return null;
        }
        bounds.set(minX, minY, maxX - minX, maxY - minY);
        return bounds;
    }

    private Rectangle doorAnimationVisualBounds(Array<MapLayer> frames) {
        Rectangle bounds = null;
        float minX = 0f;
        float minY = 0f;
        float maxX = 0f;
        float maxY = 0f;
        for (int i = 0; i < frames.size; i++) {
            Rectangle layerBounds = tiledLayerBounds(frames.get(i));
            if (layerBounds == null || layerBounds.width <= 0f || layerBounds.height <= 0f) {
                continue;
            }
            if (bounds == null) {
                bounds = new Rectangle(layerBounds);
                minX = layerBounds.x;
                minY = layerBounds.y;
                maxX = layerBounds.x + layerBounds.width;
                maxY = layerBounds.y + layerBounds.height;
            } else {
                minX = Math.min(minX, layerBounds.x);
                minY = Math.min(minY, layerBounds.y);
                maxX = Math.max(maxX, layerBounds.x + layerBounds.width);
                maxY = Math.max(maxY, layerBounds.y + layerBounds.height);
            }
        }
        if (bounds == null) {
            return null;
        }
        bounds.set(minX, minY, maxX - minX, maxY - minY);
        return bounds;
    }

    private Rectangle tiledLayerBounds(MapLayer layer) {
        if (!(layer instanceof TiledMapTileLayer tileLayer)) {
            return null;
        }
        int width = tileLayer.getWidth();
        int height = tileLayer.getHeight();
        if (width <= 0 || height <= 0) {
            return null;
        }

        int minTileX = width;
        int minTileY = height;
        int maxTileX = -1;
        int maxTileY = -1;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                TiledMapTileLayer.Cell cell = tileLayer.getCell(x, y);
                if (cell == null || cell.getTile() == null) {
                    continue;
                }
                minTileX = Math.min(minTileX, x);
                minTileY = Math.min(minTileY, y);
                maxTileX = Math.max(maxTileX, x);
                maxTileY = Math.max(maxTileY, y);
            }
        }
        if (maxTileX < minTileX || maxTileY < minTileY) {
            return null;
        }

        float tileWidth = tileLayer.getTileWidth();
        float tileHeight = tileLayer.getTileHeight();
        float offsetX = layerOffsetX(tileLayer);
        float offsetY = layerOffsetY(tileLayer);
        float x = offsetX + minTileX * tileWidth;
        float y = offsetY + minTileY * tileHeight;
        float boundsWidth = (maxTileX - minTileX + 1) * tileWidth;
        float boundsHeight = (maxTileY - minTileY + 1) * tileHeight;
        return new Rectangle(x, y, boundsWidth, boundsHeight);
    }

    private int parseDoorFrameNumber(String layerName) {
        if (layerName == null) {
            return -1;
        }
        String trimmed = layerName.trim();
        if (!trimmed.regionMatches(true, 0, DOOR_FRAME_PREFIX, 0, DOOR_FRAME_PREFIX.length())) {
            return -1;
        }
        String framePart = trimmed.substring(DOOR_FRAME_PREFIX.length()).trim();
        try {
            return Integer.parseInt(framePart);
        } catch (NumberFormatException ignored) {
            return -1;
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
        float offsetX = layerOffsetX(layer);
        float offsetY = layerOffsetY(layer);
        for (MapObject object : layer.getObjects()) {
            if (object instanceof RectangleMapObject rectObj) {
                Rectangle r = new Rectangle(rectObj.getRectangle());
                r.x += offsetX;
                r.y += offsetY;
                world.walls.add(r);
            } else if (object instanceof PolygonMapObject polyObj) {
                float[] vertices = polyObj.getPolygon().getTransformedVertices();
                if (vertices == null || vertices.length < 6) {
                    continue;
                }
                float[] shifted = vertices.clone();
                if (offsetX != 0f || offsetY != 0f) {
                    for (int i = 0; i + 1 < shifted.length; i += 2) {
                        shifted[i] += offsetX;
                        shifted[i + 1] += offsetY;
                    }
                }
                addPolygonCollision(new Polygon(shifted));
            }
        }
    }

    private MapLayer findLayerByName(String name) {
        return findLayerByName(world.map.getLayers(), name);
    }

    private MapLayer findLayerInGroup(String groupLayerName, String layerName) {
        MapLayer groupLayer = findLayerByName(groupLayerName);
        if (!(groupLayer instanceof MapGroupLayer group)) {
            return null;
        }
        return findLayerByName(group.getLayers(), layerName);
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
                FileHandle mapFile = resolveMapFile(world.activeMap != null ? world.activeMap.getMapPath() : null);
                if (mapFile == null || !mapFile.exists()) {
                    throw new IllegalStateException("Map file not found for bounds extraction.");
                }
                Element root = reader.parse(mapFile);
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
        cameraTeleportSmoothTimer = 0f;
        postDashMovementLockTimer = 0f;
        cameraTeleportTargetActive = false;
        dashAttackPlannedPositionValid = false;
        clearDashAttackSweepState();
        onPlayerSpawned();
    }

    private void onPlayerSpawned() {
        clearPlayerHazardHitReaction();
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
        if (isTeleportDashAnimation(activeAttackAnim)) {
            updateDashAttackDamageDuringAnimation();
        }
        if (activeAttackAnim.isAnimationFinished(player.attackTimer)) {
            if (isTeleportDashAnimation(activeAttackAnim)) {
                teleportPlayerAfterDashAttack();
                postDashMovementLockTimer = Math.max(postDashMovementLockTimer, POST_DASH_MOVEMENT_LOCK_SECONDS);
                clearDashAttackSweepState();
            }
            player.dashAttackFollowupQueued = false;
            player.attacking = false;
            player.attackTimer = 0f;
            activeAttackAnim = null;
            player.moving = false;
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
        boolean rightPressed = Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT);
        boolean upPressed = Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP);
        boolean downPressed = Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN);
        boolean leftMovePressed = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean rightMovePressed = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        float dashInputX = 0f;
        float dashInputY = 0f;
        if (upPressed) {
            dashInputY += 1f;
        }
        if (downPressed) {
            dashInputY -= 1f;
        }
        if (leftMovePressed) {
            dashInputX -= 1f;
        }
        if (rightMovePressed) {
            dashInputX += 1f;
        }
        if (dashInputX != 0f && dashInputY != 0f) {
            float inv = (float) (1f / Math.sqrt(2));
            dashInputX *= inv;
            dashInputY *= inv;
        }
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
                if (attackSpecialSfx != null) {
                    attackSpecialSfx.play();
                }
                startDashAttack(dashInputX, dashInputY);
            }
        }

        if (isDashAttackAnimationActive()) {
            player.moving = false;
            centerCameraOnPlayer();
            return;
        }

        if (rightPressed && !player.attacking && !player.dashing && player.attackCooldownTimer <= 0f && player.dashCooldownTimer <= 0f) {
            activeAttackAnim = spetialDashAnim;
            if (activeAttackAnim != null) {
                player.attacking = true;
                player.attackTimer = 0f;
                player.attackCooldownTimer = ATTACK_COOLDOWN;
                player.attackComboStep = 0;
                if (attackSpecialSfx != null) {
                    attackSpecialSfx.play();
                }
                startDashAttack(dashInputX, dashInputY);
            }
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
            } else if ("boss".equals(destination)) {
                teleportToMap(MAP_ID_BOSS_ARENA_PHASE_2, "SPAWN");
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
        clearSpawnedBat();
        addBatAt(spawnX, spawnY);
    }

    private void clearSpawnedBat() {
        bats.clear();
    }

    private void clearSpawnedGolem1() {
        golem1s.clear();
    }

    private void clearSpawnedBossArenaPhase2Demons() {
        bossArenaPhase2Demons.clear();
    }

    private void disposeBossArenaPhase2DemonFrameTextures() {
        for (int i = 0; i < bossArenaPhase2DemonIdleFrameTextures.size; i++) {
            Texture frameTexture = bossArenaPhase2DemonIdleFrameTextures.get(i);
            if (frameTexture != null) {
                frameTexture.dispose();
            }
        }
        bossArenaPhase2DemonIdleFrameTextures.clear();
        for (int i = 0; i < bossArenaPhase2DemonWalkFrameTextures.size; i++) {
            Texture frameTexture = bossArenaPhase2DemonWalkFrameTextures.get(i);
            if (frameTexture != null) {
                frameTexture.dispose();
            }
        }
        bossArenaPhase2DemonWalkFrameTextures.clear();
        for (int i = 0; i < bossArenaPhase2DemonAttackFrameTextures.size; i++) {
            Texture frameTexture = bossArenaPhase2DemonAttackFrameTextures.get(i);
            if (frameTexture != null) {
                frameTexture.dispose();
            }
        }
        bossArenaPhase2DemonAttackFrameTextures.clear();
        for (int i = 0; i < bossArenaPhase2DemonHurtFrameTextures.size; i++) {
            Texture frameTexture = bossArenaPhase2DemonHurtFrameTextures.get(i);
            if (frameTexture != null) {
                frameTexture.dispose();
            }
        }
        bossArenaPhase2DemonHurtFrameTextures.clear();
    }

    private void clearSpawnedRewardChests() {
        rewardChests.clear();
    }

    private com.badlogic.gdx.maps.tiled.TiledMap loadTiledMap(String mapPath, TmxMapLoader.Parameters params) {
        try {
            return loadTiledMapUnchecked(mapPath, params);
        } catch (RuntimeException primaryLoadError) {
            String finiteFallbackPath = toFiniteMapPath(mapPath);
            if (finiteFallbackPath == null) {
                throw primaryLoadError;
            }
            FileHandle finiteFallbackFile = resolveMapFile(finiteFallbackPath);
            if (finiteFallbackFile == null || !finiteFallbackFile.exists()) {
                throw primaryLoadError;
            }
            Gdx.app.log(
                    "PixelRpgGame",
                    "Map load failed for '" + mapPath + "'. Trying finite fallback '" + finiteFallbackPath + "'.");
            try {
                return loadTiledMapUnchecked(finiteFallbackPath, params);
            } catch (RuntimeException fallbackLoadError) {
                fallbackLoadError.addSuppressed(primaryLoadError);
                throw fallbackLoadError;
            }
        }
    }

    private com.badlogic.gdx.maps.tiled.TiledMap loadTiledMapUnchecked(String mapPath, TmxMapLoader.Parameters params) {
        if (isAbsoluteMapPath(mapPath)) {
            TmxMapLoader absoluteLoader = new TmxMapLoader(new AbsoluteFileHandleResolver());
            return absoluteLoader.load(mapPath, params);
        }
        TmxMapLoader loader = new TmxMapLoader();
        return loader.load(mapPath, params);
    }

    private String toFiniteMapPath(String mapPath) {
        if (mapPath == null || mapPath.isBlank()) {
            return null;
        }
        String normalized = mapPath.trim();
        String lower = normalized.toLowerCase(Locale.ROOT);
        if (!lower.endsWith(".tmx") || lower.endsWith("_finite.tmx")) {
            return null;
        }
        return normalized.substring(0, normalized.length() - 4) + "_finite.tmx";
    }

    private FileHandle resolveMapFile(String mapPath) {
        if (mapPath == null || mapPath.isBlank()) {
            return null;
        }
        if (isAbsoluteMapPath(mapPath)) {
            return Gdx.files.absolute(mapPath);
        }
        return Gdx.files.internal(mapPath);
    }

    private boolean isAbsoluteMapPath(String mapPath) {
        if (mapPath == null) {
            return false;
        }
        String normalized = mapPath.trim();
        if (normalized.length() < 3) {
            return false;
        }
        char first = normalized.charAt(0);
        char second = normalized.charAt(1);
        char third = normalized.charAt(2);
        if (first == '\\' && second == '\\') {
            return true;
        }
        return Character.isLetter(first) && second == ':' && (third == '/' || third == '\\');
    }

    private void clearSpawnedEvilWizards() {
        evilWizards.clear();
        clearEvilWizardAttackHazard();
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
        return isPlayerWithinObjectLayer(layer);
    }

    private boolean isPlayerWithinObjectLayer(MapLayer layer) {
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
        float offsetX = layerOffsetX(layer);
        float offsetY = layerOffsetY(layer);
        Rectangle localBounds = objectLocalBounds(object);
        if (localBounds == null) {
            return null;
        }
        return new Rectangle(
                localBounds.x + offsetX,
                localBounds.y + offsetY,
                localBounds.width,
                localBounds.height);
    }

    private Rectangle objectLocalBounds(MapObject object) {
        if (object instanceof RectangleMapObject rectObj) {
            Rectangle rect = rectObj.getRectangle();
            return new Rectangle(rect.x, rect.y, rect.width, rect.height);
        }
        if (object instanceof PolygonMapObject polyObj) {
            Rectangle bounds = polyObj.getPolygon().getBoundingRectangle();
            return new Rectangle(bounds.x, bounds.y, bounds.width, bounds.height);
        }
        if (object instanceof EllipseMapObject ellipseObj) {
            Ellipse ellipse = ellipseObj.getEllipse();
            return new Rectangle(ellipse.x, ellipse.y, ellipse.width, ellipse.height);
        }

        MapProperties properties = object.getProperties();
        Float x = properties.get("x", Float.class);
        Float y = properties.get("y", Float.class);
        if (x == null || y == null) {
            return null;
        }
        return new Rectangle(x, y, 0f, 0f);
    }

    private Vector2 extractLayerSpawn(MapLayer layer) {
        if (layer == null || layer.getObjects().getCount() == 0) {
            return null;
        }

        float offsetX = layerOffsetX(layer);
        float offsetY = layerOffsetY(layer);
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

    private Vector2 extractObjectSpawn(MapLayer layer, MapObject object) {
        Rectangle bounds = objectBounds(layer, object);
        if (bounds == null) {
            return null;
        }
        float x = bounds.x + bounds.width * 0.5f;
        float y = bounds.y + bounds.height * 0.5f;
        return new Vector2(x, y);
    }

    private float layerOffsetX(MapLayer layer) {
        float total = 0f;
        for (MapLayer current = layer; current != null; current = current.getParent()) {
            total += current.getOffsetX();
        }
        return total;
    }

    private float layerOffsetY(MapLayer layer) {
        float total = 0f;
        for (MapLayer current = layer; current != null; current = current.getParent()) {
            total += current.getOffsetY();
        }
        return total;
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

    private void startDashAttack(float inputX, float inputY) {
        float dx = inputX;
        float dy = inputY;
        if (dx == 0f && dy == 0f) {
            dx = player.lastMoveX;
            dy = player.lastMoveY;
        }
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
        player.dashing = false;
        player.dashTimer = 0f;
        player.dashCooldownTimer = DASH_COOLDOWN;
        player.dashAttackActive = false;
        player.dashAttackMoveDuration = 0f;

        Vector2 plannedPosition = computeDashTeleportDestination(
                player.bounds.x,
                player.bounds.y,
                player.dashDirX,
                player.dashDirY);
        dashAttackSweepStart.set(player.bounds.x, player.bounds.y);
        dashAttackSweepEnd.set(plannedPosition);
        dashAttackSweepLastProgress = 0f;
        dashAttackSweepActive = true;
        activeDashAttackHitId = ++dashAttackHitSequence;
        dashAttackPlannedPosition.set(plannedPosition);
        dashAttackPlannedPositionValid = true;
        float cameraTargetX = plannedPosition.x + player.bounds.width * 0.5f;
        float cameraTargetY = plannedPosition.y + player.bounds.height * 0.5f;
        Animation<TextureRegion> dashAnimation = activeAttackAnim;
        float dashAnimDuration = dashAnimation != null ? dashAnimation.getAnimationDuration() : DASH_DURATION;
        startCameraTeleportSmoothing(cameraTargetX, cameraTargetY, dashAnimDuration + TELEPORT_CAMERA_SMOOTH_DURATION);
    }

    private void teleportPlayerAfterDashAttack() {
        float dx = player.dashDirX;
        float dy = player.dashDirY;
        if (dx == 0f && dy == 0f) {
            dx = player.facingLeft ? -1f : 1f;
        }
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len == 0f) {
            return;
        }
        dx /= len;
        dy /= len;
        if (dx != 0f) {
            player.facingLeft = dx < 0f;
        }

        float startX = player.bounds.x;
        float startY = player.bounds.y;
        boolean teleported = false;
        if (dashAttackPlannedPositionValid) {
            Rectangle plannedBounds = new Rectangle(player.bounds);
            plannedBounds.x = dashAttackPlannedPosition.x;
            plannedBounds.y = dashAttackPlannedPosition.y;
            if (!collides(plannedBounds)) {
                player.bounds.set(plannedBounds);
                teleported = player.bounds.x != startX || player.bounds.y != startY;
            }
        }
        if (!teleported) {
            float tileSize = resolveActiveMapTileSize();
            float teleportDistance = tileSize * DASH_ATTACK_TELEPORT_TILES;
            float subStepDistance = Math.max(2f, tileSize * 0.25f);
            float remainingDistance = teleportDistance;
            while (remainingDistance > 0f) {
                float stepDistance = Math.min(subStepDistance, remainingDistance);
                if (!tryMove(dx * stepDistance, dy * stepDistance)) {
                    break;
                }
                teleported = true;
                remainingDistance -= stepDistance;
            }
        }
        dashAttackPlannedPositionValid = false;
        if (teleported) {
            float cameraTargetX = player.bounds.x + player.bounds.width * 0.5f;
            float cameraTargetY = player.bounds.y + player.bounds.height * 0.5f;
            startCameraTeleportSmoothing(cameraTargetX, cameraTargetY, TELEPORT_CAMERA_SMOOTH_DURATION);
        }
    }

    private void updateDashAttackDamageDuringAnimation() {
        if (!dashAttackSweepActive || !isTeleportDashAnimation(activeAttackAnim)) {
            return;
        }
        float animationDuration = activeAttackAnim.getAnimationDuration();
        if (animationDuration <= 0f) {
            return;
        }
        float progress = clamp01(player.attackTimer / animationDuration);
        if (progress <= dashAttackSweepLastProgress) {
            return;
        }

        float segmentStartX = lerp(dashAttackSweepStart.x, dashAttackSweepEnd.x, dashAttackSweepLastProgress);
        float segmentStartY = lerp(dashAttackSweepStart.y, dashAttackSweepEnd.y, dashAttackSweepLastProgress);
        float segmentEndX = lerp(dashAttackSweepStart.x, dashAttackSweepEnd.x, progress);
        float segmentEndY = lerp(dashAttackSweepStart.y, dashAttackSweepEnd.y, progress);
        applyDashAttackSweepDamage(segmentStartX, segmentStartY, segmentEndX, segmentEndY);
        dashAttackSweepLastProgress = progress;
    }

    private void applyDashAttackSweepDamage(float startBoundsX, float startBoundsY, float endBoundsX, float endBoundsY) {
        if (bats.size == 0 && evilWizards.size == 0 && golem1s.size == 0 && bossArenaPhase2Demons.size == 0) {
            return;
        }
        float startCenterX = startBoundsX + player.bounds.width * 0.5f;
        float startCenterY = startBoundsY + player.bounds.height * 0.5f;
        float endCenterX = endBoundsX + player.bounds.width * 0.5f;
        float endCenterY = endBoundsY + player.bounds.height * 0.5f;
        float hitRadius = playerDashAttackHitRadius();
        float hitRadiusSq = hitRadius * hitRadius;
        for (int i = 0; i < bats.size; i++) {
            BatInstance bat = bats.get(i);
            if (bat.lastDashAttackHitId == activeDashAttackHitId) {
                continue;
            }
            float distanceSq = pointSegmentDistanceSquared(
                    bat.position.x,
                    bat.position.y,
                    startCenterX,
                    startCenterY,
                    endCenterX,
                    endCenterY);
            if (distanceSq <= hitRadiusSq) {
                bat.lastDashAttackHitId = activeDashAttackHitId;
                applyDamageToBatFromPlayer(bat);
            }
        }

        for (int i = 0; i < evilWizards.size; i++) {
            EvilWizardInstance wizard = evilWizards.get(i);
            if (wizard.lastDashAttackHitId == activeDashAttackHitId
                    || wizard.health <= 0f
                    || wizard.state == EvilWizardAnimationState.DIE) {
                continue;
            }
            float wizardHitboxCenterX = evilWizardHitboxCenterX(wizard);
            float wizardHitboxCenterY = evilWizardHitboxCenterY(wizard);
            float wizardHitboxWidth = evilWizardHitboxHalfWidth() * 2f;
            float wizardHitboxHeight = evilWizardHitboxHalfHeight() * 2f;
            float wizardHitboxAverageSize = (wizardHitboxWidth + wizardHitboxHeight) * 0.5f;
            float wizardHitRadius = hitRadius + wizardHitboxAverageSize * 0.25f;
            float wizardHitRadiusSq = wizardHitRadius * wizardHitRadius;
            float distanceSq = pointSegmentDistanceSquared(
                    wizardHitboxCenterX,
                    wizardHitboxCenterY,
                    startCenterX,
                    startCenterY,
                    endCenterX,
                    endCenterY);
            if (distanceSq <= wizardHitRadiusSq) {
                wizard.lastDashAttackHitId = activeDashAttackHitId;
                applyHitToEvilWizardFromPlayer(wizard);
            }
        }

        for (int i = 0; i < golem1s.size; i++) {
            Golem1Instance golem = golem1s.get(i);
            if (golem.lastDashAttackHitId == activeDashAttackHitId || golem.dying || golem.health <= 0f) {
                continue;
            }
            float golemHitboxWidth = golem1HitboxHalfWidth() * 2f;
            float golemHitboxHeight = golem1HitboxHalfHeight() * 2f;
            float golemHitboxAverageSize = (golemHitboxWidth + golemHitboxHeight) * 0.5f;
            float golemHitRadius = hitRadius + golemHitboxAverageSize * 0.25f;
            float golemHitRadiusSq = golemHitRadius * golemHitRadius;
            float golemHitboxCenterX = golem1HitboxCenterX(golem);
            float golemHitboxCenterY = golem1HitboxCenterY(golem);
            float distanceSq = pointSegmentDistanceSquared(
                    golemHitboxCenterX,
                    golemHitboxCenterY,
                    startCenterX,
                    startCenterY,
                    endCenterX,
                    endCenterY);
            if (distanceSq <= golemHitRadiusSq) {
                golem.lastDashAttackHitId = activeDashAttackHitId;
                applyHitToGolem1FromPlayer(golem);
            }
        }

        for (int i = 0; i < bossArenaPhase2Demons.size; i++) {
            BossArenaPhase2DemonInstance demon = bossArenaPhase2Demons.get(i);
            if (demon.lastDashAttackHitId == activeDashAttackHitId
                    || demon.hurting
                    || demon.playerHitCooldownTimer > 0f) {
                continue;
            }
            float demonHitboxWidth = bossArenaPhase2DemonHitboxHalfWidth() * 2f;
            float demonHitboxHeight = bossArenaPhase2DemonHitboxHalfHeight() * 2f;
            float demonHitboxAverageSize = (demonHitboxWidth + demonHitboxHeight) * 0.5f;
            float demonHitRadius = hitRadius + demonHitboxAverageSize * 0.25f;
            float demonHitRadiusSq = demonHitRadius * demonHitRadius;
            float demonHitboxCenterX = bossArenaPhase2DemonHitboxCenterX(demon);
            float demonHitboxCenterY = bossArenaPhase2DemonHitboxCenterY(demon);
            float distanceSq = pointSegmentDistanceSquared(
                    demonHitboxCenterX,
                    demonHitboxCenterY,
                    startCenterX,
                    startCenterY,
                    endCenterX,
                    endCenterY);
            if (distanceSq <= demonHitRadiusSq) {
                demon.lastDashAttackHitId = activeDashAttackHitId;
                applyHitToBossArenaPhase2DemonFromPlayer(demon);
            }
        }
    }

    private void clearDashAttackSweepState() {
        dashAttackSweepActive = false;
        dashAttackSweepLastProgress = 0f;
        activeDashAttackHitId = -1;
    }

    private static float pointSegmentDistanceSquared(
            float px, float py,
            float x1, float y1,
            float x2, float y2) {
        float segX = x2 - x1;
        float segY = y2 - y1;
        float segLenSq = segX * segX + segY * segY;
        if (segLenSq <= 0.0001f) {
            float dx = px - x1;
            float dy = py - y1;
            return dx * dx + dy * dy;
        }
        float t = ((px - x1) * segX + (py - y1) * segY) / segLenSq;
        t = clamp01(t);
        float closestX = x1 + segX * t;
        float closestY = y1 + segY * t;
        float dx = px - closestX;
        float dy = py - closestY;
        return dx * dx + dy * dy;
    }

    private void startCameraTeleportSmoothing(float targetX, float targetY, float durationSeconds) {
        cameraTeleportTarget.set(targetX, targetY);
        cameraTeleportTargetActive = true;
        cameraTeleportSmoothTimer = Math.max(cameraTeleportSmoothTimer, durationSeconds);
    }

    private Vector2 computeDashTeleportDestination(float startX, float startY, float dirX, float dirY) {
        Rectangle simulatedBounds = new Rectangle(player.bounds);
        simulatedBounds.x = startX;
        simulatedBounds.y = startY;

        float tileSize = resolveActiveMapTileSize();
        float teleportDistance = tileSize * DASH_ATTACK_TELEPORT_TILES;
        float subStepDistance = Math.max(2f, tileSize * 0.25f);
        float remainingDistance = teleportDistance;
        while (remainingDistance > 0f) {
            float stepDistance = Math.min(subStepDistance, remainingDistance);
            Rectangle nextBounds = new Rectangle(simulatedBounds);
            nextBounds.x += dirX * stepDistance;
            nextBounds.y += dirY * stepDistance;
            if (collides(nextBounds)) {
                break;
            }
            simulatedBounds.set(nextBounds);
            remainingDistance -= stepDistance;
        }
        return new Vector2(simulatedBounds.x, simulatedBounds.y);
    }

    private float resolveActiveMapTileSize() {
        if (world.map == null) {
            return DEFAULT_TILE_SIZE;
        }
        MapProperties properties = world.map.getProperties();
        Integer tileWidth = properties.get("tilewidth", Integer.class);
        Integer tileHeight = properties.get("tileheight", Integer.class);
        if (tileWidth != null && tileWidth > 0 && tileHeight != null && tileHeight > 0) {
            return (tileWidth + tileHeight) * 0.5f;
        }
        if (tileWidth != null && tileWidth > 0) {
            return tileWidth;
        }
        if (tileHeight != null && tileHeight > 0) {
            return tileHeight;
        }
        return DEFAULT_TILE_SIZE;
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
        if (isBossFightZoneCollisionActive() && !isWithinBossFightZone(rect)) {
            return true;
        }

        for (Rectangle wall : world.walls) {
            if (Intersector.overlaps(rect, wall)) {
                return true;
            }
        }

        if (entranceDoorCollisionEnabled
                && entranceDoorClosedCollisionBounds.width > 0f
                && entranceDoorClosedCollisionBounds.height > 0f
                && Intersector.overlaps(rect, entranceDoorClosedCollisionBounds)) {
            return true;
        }

        if (room1DoorCollisionEnabled
                && hasRoom1DoorCollisionBounds()
                && Intersector.overlaps(rect, room1DoorClosedCollisionBounds)) {
            return true;
        }

        if (firstBossDoorCollisionEnabled
                && hasFirstBossDoorCollisionBounds()
                && Intersector.overlaps(rect, firstBossDoorClosedCollisionBounds)) {
            return true;
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
        float targetX = player.bounds.x + player.bounds.width / 2f;
        float targetY = player.bounds.y + player.bounds.height / 2f;
        if (cameraTeleportSmoothTimer > 0f) {
            if (cameraTeleportTargetActive) {
                targetX = cameraTeleportTarget.x;
                targetY = cameraTeleportTarget.y;
            }
            float delta = Gdx.graphics.getDeltaTime();
            float alpha = 1f - (float) Math.exp(-TELEPORT_CAMERA_SMOOTH_SPEED * delta);
            camera.position.x = lerp(camera.position.x, targetX, alpha);
            camera.position.y = lerp(camera.position.y, targetY, alpha);
            camera.position.z = 0f;
            cameraTeleportSmoothTimer = Math.max(0f, cameraTeleportSmoothTimer - delta);
            if (cameraTeleportSmoothTimer <= 0f) {
                cameraTeleportTargetActive = false;
                camera.position.x = player.bounds.x + player.bounds.width / 2f;
                camera.position.y = player.bounds.y + player.bounds.height / 2f;
            }
            return;
        }
        camera.position.set(targetX, targetY, 0f);
    }

    private void clampCamera() {
        float halfW = camera.viewportWidth * camera.zoom / 2f;
        float halfH = camera.viewportHeight * camera.zoom / 2f;

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

    private void updateBossArenaPhase2AttackZoom(float delta) {
        if (camera == null) {
            return;
        }
        float targetZoom = DEFAULT_ZOOM;
        if (MAP_ID_BOSS_ARENA_PHASE_2.equals(world.activeMapId) && isAnyBossArenaPhase2DemonAttacking()) {
            targetZoom = DEFAULT_ZOOM * BOSS_ARENA_PHASE_2_ATTACK_ZOOM_MULTIPLIER;
        }
        float safeDelta = Math.max(0f, delta);
        float alpha = 1f - (float) Math.exp(-BOSS_ARENA_PHASE_2_ATTACK_ZOOM_SMOOTH_SPEED * safeDelta);
        camera.zoom = lerp(camera.zoom, targetZoom, alpha);
        if (Math.abs(camera.zoom - targetZoom) < 0.0005f) {
            camera.zoom = targetZoom;
        }
    }

    private boolean isAnyBossArenaPhase2DemonAttacking() {
        for (int i = 0; i < bossArenaPhase2Demons.size; i++) {
            BossArenaPhase2DemonInstance demon = bossArenaPhase2Demons.get(i);
            if (demon != null && demon.attacking) {
                return true;
            }
        }
        return false;
    }

    private void clearCameraShakeOffsetFromCameraPosition() {
        if (camera == null) {
            return;
        }
        if (cameraShakeOffset.x == 0f && cameraShakeOffset.y == 0f) {
            return;
        }
        camera.position.x -= cameraShakeOffset.x;
        camera.position.y -= cameraShakeOffset.y;
        cameraShakeOffset.setZero();
    }

    private void updateAndApplyExplosionCameraShake(float delta) {
        if (camera == null) {
            return;
        }
        float safeDelta = Math.max(0f, delta);
        if (cameraShakeTimer > 0f) {
            cameraShakeTimer = Math.max(0f, cameraShakeTimer - safeDelta);
        }
        if (cameraShakeStrength > 0f) {
            cameraShakeStrength = Math.max(
                    0f,
                    cameraShakeStrength - GOLEM_1_EXPLOSION_CAMERA_SHAKE_DAMPING_PER_SECOND * safeDelta);
        }
        if (cameraShakeTimer <= 0f || cameraShakeStrength <= 0f) {
            cameraShakeTimer = 0f;
            cameraShakeStrength = 0f;
            cameraShakeOffset.setZero();
            return;
        }

        float offsetX = (((float) Math.random()) * 2f - 1f) * cameraShakeStrength;
        float offsetY = (((float) Math.random()) * 2f - 1f) * cameraShakeStrength;
        cameraShakeOffset.set(offsetX, offsetY);
        camera.position.x += offsetX;
        camera.position.y += offsetY;
    }

    private void triggerGolemExplosionCameraShake() {
        cameraShakeTimer = Math.max(cameraShakeTimer, GOLEM_1_EXPLOSION_CAMERA_SHAKE_DURATION_SECONDS);
        cameraShakeStrength = Math.min(
                GOLEM_1_EXPLOSION_CAMERA_SHAKE_MAX,
                cameraShakeStrength + GOLEM_1_EXPLOSION_CAMERA_SHAKE_PER_BURST);
    }

    private void playGolem1ExplosionSfx() {
        if (golem1ExplosionSfx != null) {
            golem1ExplosionSfx.play(GOLEM_1_EXPLOSION_SFX_VOLUME);
        }
    }

    private void applyGolem1ExplosionHitToPlayer(Golem1ExplosionInstance burst) {
        if (burst == null || player.currentHealth <= 0f || isPlayerHazardHitReactionActive()) {
            return;
        }
        float radius = GOLEM_1_EXPLOSION_HIT_RADIUS_TILES * DEFAULT_TILE_SIZE;
        float radiusSq = radius * radius;
        float playerCenterX = player.bounds.x + player.bounds.width * 0.5f;
        float playerCenterY = player.bounds.y + player.bounds.height * 0.5f;
        float dx = playerCenterX - burst.position.x;
        float dy = playerCenterY - burst.position.y;
        if (dx * dx + dy * dy > radiusSq) {
            return;
        }

        player.currentHealth = Math.max(0f, player.currentHealth - GOLEM_1_EXPLOSION_DAMAGE);
        triggerPlayerHazardHitReaction();
    }

    private void loadAnimations() {
        idleTex = new Texture(Gdx.files.internal("images/huntress/1 - Idle.png"));
        runTex = new Texture(Gdx.files.internal("images/huntress/2 - Run.png"));
        attack1Tex = new Texture(Gdx.files.internal("images/huntress/3 - Attack 1.png"));
        attack3Tex = new Texture(Gdx.files.internal("images/huntress/4 - Attack 3.png"));
        dashAttackTex = new Texture(Gdx.files.internal("images/huntress/12 - Dash Attack.png"));
        spetialDashTex = new Texture(Gdx.files.internal("images/huntress/13 - spetial dash.png"));
        jumpUpAttackTex = new Texture(Gdx.files.internal("images/huntress/5 - Jump Up Attack.png"));
        jumpDownAttackTex = new Texture(Gdx.files.internal("images/huntress/6 - Jump Down Attack.png"));
        idleUpAttackTex = new Texture(Gdx.files.internal("images/huntress/7 - Idle Up Attack.png"));
        playerHitTex = loadPlayerHitTexture();
        attack1Sfx = Gdx.audio.newSound(Gdx.files.internal("sounds/attack 1.mp3"));
        attack3Sfx = Gdx.audio.newSound(Gdx.files.internal("sounds/attack 3.mp3"));
        attackSpecialSfx = Gdx.audio.newSound(Gdx.files.internal("sounds/attacks.mp3"));
        batHurtSfx = Gdx.audio.newSound(Gdx.files.internal("sounds/Bat-hurt.mp3"));
        evilWizardAttack1Sfx = loadOptionalAbsoluteSound(EVIL_WIZARD_ATTACK1_SFX_ABSOLUTE_PATH);
        evilWizardAttack2Sfx = loadOptionalAbsoluteSound(EVIL_WIZARD_ATTACK2_SFX_ABSOLUTE_PATH);
        golem1ExplosionSfx = loadOptionalAbsoluteSound(GOLEM_1_EXPLOSION_SFX_ABSOLUTE_PATH);
        evilWizardPreExplosionSfx = loadOptionalAbsoluteMusic(EVIL_WIZARD_PRE_EXPLOSION_SFX_ABSOLUTE_PATH);
        idleTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        runTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        attack1Tex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        attack3Tex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        dashAttackTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        spetialDashTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        jumpUpAttackTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        jumpDownAttackTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        idleUpAttackTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        if (playerHitTex != null) {
            playerHitTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        }

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
        spetialDashAnim = buildAnimation(spetialDashTex, SPETIAL_DASH_FRAMES,
                (int) SPRITE_FRAME_WIDTH, (int) SPRITE_FRAME_HEIGHT, ATTACK_FRAME_DURATION, 0, 1);
        spetialDashAnim.setPlayMode(Animation.PlayMode.NORMAL);
        jumpUpAttackAnim = buildAnimation(jumpUpAttackTex, UP_ATTACK_FRAMES,
                (int) SPRITE_FRAME_WIDTH, (int) SPRITE_FRAME_HEIGHT, ATTACK_FRAME_DURATION, 0, 1);
        jumpUpAttackAnim.setPlayMode(Animation.PlayMode.NORMAL);
        jumpDownAttackAnim = buildAnimation(jumpDownAttackTex, DOWN_ATTACK_FRAMES,
                (int) SPRITE_FRAME_WIDTH, (int) SPRITE_FRAME_HEIGHT, ATTACK_FRAME_DURATION, 0, 1);
        jumpDownAttackAnim.setPlayMode(Animation.PlayMode.NORMAL);
        idleUpAttackAnim = buildAnimation(idleUpAttackTex, IDLE_UP_ATTACK_FRAMES,
                (int) SPRITE_FRAME_WIDTH, (int) SPRITE_FRAME_HEIGHT, ATTACK_FRAME_DURATION, 0, 1);
        idleUpAttackAnim.setPlayMode(Animation.PlayMode.NORMAL);
        if (playerHitTex != null) {
            int hitFrameCount = Math.max(1, playerHitTex.getWidth() / (int) SPRITE_FRAME_WIDTH);
            playerHitAnim = buildAnimation(
                    playerHitTex,
                    hitFrameCount,
                    (int) SPRITE_FRAME_WIDTH,
                    (int) SPRITE_FRAME_HEIGHT,
                    PLAYER_HIT_FRAME_DURATION,
                    0,
                    1);
            playerHitAnim.setPlayMode(Animation.PlayMode.NORMAL);
        } else {
            playerHitAnim = null;
        }
        loadBatAssets();
        loadEvilWizardAssets();
        loadGolem1Assets();
        loadBossArenaPhase2DemonAssets();
        loadChestAssets();
    }

    private Texture loadOptionalAbsoluteTexture(String absolutePath) {
        if (absolutePath == null || absolutePath.isBlank()) {
            return null;
        }
        try {
            return new Texture(Gdx.files.absolute(absolutePath));
        } catch (RuntimeException error) {
            if (Gdx.app != null) {
                Gdx.app.error("PixelRpgGame", "Failed to load texture: " + absolutePath, error);
            }
            return null;
        }
    }

    private Animation<TextureRegion> loadOptionalAbsoluteFrameAnimation(
            String absoluteDirectoryPath,
            float frameDuration,
            Array<Texture> ownedTextures) {
        if (absoluteDirectoryPath == null || absoluteDirectoryPath.isBlank()) {
            return null;
        }
        FileHandle directory = Gdx.files.absolute(absoluteDirectoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            return null;
        }

        FileHandle[] frameFiles = directory.list(".png");
        if (frameFiles == null || frameFiles.length == 0) {
            return null;
        }
        Arrays.sort(frameFiles, this::compareFrameFiles);

        Array<TextureRegion> frames = new Array<>(frameFiles.length);
        for (FileHandle frameFile : frameFiles) {
            try {
                Texture frameTexture = new Texture(frameFile);
                frameTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                ownedTextures.add(frameTexture);
                frames.add(new TextureRegion(frameTexture));
            } catch (RuntimeException error) {
                if (Gdx.app != null) {
                    Gdx.app.error("PixelRpgGame", "Failed to load frame texture: " + frameFile.path(), error);
                }
            }
        }
        if (frames.size == 0) {
            return null;
        }
        return new Animation<>(frameDuration, frames, Animation.PlayMode.LOOP);
    }

    private int compareFrameFiles(FileHandle left, FileHandle right) {
        int leftIndex = parseTrailingFrameIndex(left.nameWithoutExtension());
        int rightIndex = parseTrailingFrameIndex(right.nameWithoutExtension());
        if (leftIndex >= 0 && rightIndex >= 0 && leftIndex != rightIndex) {
            return Integer.compare(leftIndex, rightIndex);
        }
        return left.name().compareToIgnoreCase(right.name());
    }

    private int parseTrailingFrameIndex(String name) {
        if (name == null || name.isBlank()) {
            return -1;
        }
        int end = name.length() - 1;
        while (end >= 0 && Character.isDigit(name.charAt(end))) {
            end--;
        }
        if (end == name.length() - 1) {
            return -1;
        }
        String digits = name.substring(end + 1);
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private Sound loadOptionalAbsoluteSound(String absolutePath) {
        if (absolutePath == null || absolutePath.isBlank()) {
            return null;
        }
        try {
            return Gdx.audio.newSound(Gdx.files.absolute(absolutePath));
        } catch (RuntimeException error) {
            if (Gdx.app != null) {
                Gdx.app.error("PixelRpgGame", "Failed to load sound: " + absolutePath, error);
            }
            return null;
        }
    }

    private Music loadOptionalAbsoluteMusic(String absolutePath) {
        if (absolutePath == null || absolutePath.isBlank()) {
            return null;
        }
        try {
            return Gdx.audio.newMusic(Gdx.files.absolute(absolutePath));
        } catch (RuntimeException error) {
            if (Gdx.app != null) {
                Gdx.app.error("PixelRpgGame", "Failed to load music: " + absolutePath, error);
            }
            return null;
        }
    }

    private Texture loadPlayerHitTexture() {
        RuntimeException absoluteError = null;
        try {
            return new Texture(Gdx.files.absolute(PLAYER_HIT_TEXTURE_ABSOLUTE_PATH));
        } catch (RuntimeException error) {
            absoluteError = error;
        }

        try {
            return new Texture(Gdx.files.internal(PLAYER_HIT_TEXTURE_INTERNAL_PATH));
        } catch (RuntimeException internalError) {
            if (Gdx.app != null) {
                Gdx.app.error(
                        "PixelRpgGame",
                        "Failed to load player hit texture from absolute path '"
                                + PLAYER_HIT_TEXTURE_ABSOLUTE_PATH
                                + "' and fallback internal path '"
                                + PLAYER_HIT_TEXTURE_INTERNAL_PATH + "'.",
                        absoluteError != null ? absoluteError : internalError);
            }
            return null;
        }
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
            batWakeUpTex = new Texture(Gdx.files.internal(BAT_WAKEUP_TEXTURE_PATH));
            batWakeUpTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            batWakeUpAnim = buildRawAnimation(
                    batWakeUpTex,
                    BAT_WAKEUP_FRAMES,
                    BAT_FRAME_SIZE,
                    BAT_FRAME_SIZE,
                    BAT_WAKEUP_FRAME_DURATION);
            batIdleFlyTex = new Texture(Gdx.files.internal(BAT_IDLE_FLY_TEXTURE_PATH));
            batIdleFlyTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            batIdleFlyAnim = buildRawAnimation(
                    batIdleFlyTex,
                    BAT_IDLE_FLY_FRAMES,
                    BAT_FRAME_SIZE,
                    BAT_FRAME_SIZE,
                    BAT_IDLE_FLY_FRAME_DURATION);
            batRunTex = new Texture(Gdx.files.internal(BAT_RUN_TEXTURE_PATH));
            batRunTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            batRunAnim = buildRawAnimation(
                    batRunTex,
                    BAT_RUN_FRAMES,
                    BAT_FRAME_SIZE,
                    BAT_FRAME_SIZE,
                    BAT_RUN_FRAME_DURATION);
            batAttack1Tex = new Texture(Gdx.files.internal(BAT_ATTACK1_TEXTURE_PATH));
            batAttack1Tex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            batAttack1Anim = buildRawAnimation(
                    batAttack1Tex,
                    BAT_ATTACK1_FRAMES,
                    BAT_FRAME_SIZE,
                    BAT_FRAME_SIZE,
                    BAT_ATTACK_FRAME_DURATION);
            batAttack2Tex = new Texture(Gdx.files.internal(BAT_ATTACK2_TEXTURE_PATH));
            batAttack2Tex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            batAttack2Anim = buildRawAnimation(
                    batAttack2Tex,
                    BAT_ATTACK2_FRAMES,
                    BAT_FRAME_SIZE,
                    BAT_FRAME_SIZE,
                    BAT_ATTACK_FRAME_DURATION);
            batHurtTex = new Texture(Gdx.files.internal(BAT_HURT_TEXTURE_PATH));
            batHurtTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            batHurtAnim = buildRawAnimation(
                    batHurtTex,
                    BAT_HURT_FRAMES,
                    BAT_FRAME_SIZE,
                    BAT_FRAME_SIZE,
                    BAT_HURT_FRAME_DURATION);
            batDieTex = new Texture(Gdx.files.internal(BAT_DIE_TEXTURE_PATH));
            batDieTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            batDieAnim = buildRawAnimation(
                    batDieTex,
                    BAT_DIE_FRAMES,
                    BAT_FRAME_SIZE,
                    BAT_FRAME_SIZE,
                    BAT_DIE_FRAME_DURATION);
        } catch (RuntimeException error) {
            batSleepAnim = null;
            batWakeUpAnim = null;
            batIdleFlyAnim = null;
            batRunAnim = null;
            batAttack1Anim = null;
            batAttack2Anim = null;
            batHurtAnim = null;
            batDieAnim = null;
            clearSpawnedBat();
            Gdx.app.error(
                    "PixelRpgGame",
                    "Failed to load bat textures from '"
                            + BAT_SLEEP_TEXTURE_PATH + "', '"
                            + BAT_WAKEUP_TEXTURE_PATH + "', or '"
                            + BAT_IDLE_FLY_TEXTURE_PATH + "', '"
                            + BAT_RUN_TEXTURE_PATH + "', '"
                            + BAT_ATTACK1_TEXTURE_PATH + "', or '"
                            + BAT_ATTACK2_TEXTURE_PATH + "', or '"
                            + BAT_HURT_TEXTURE_PATH + "', or '"
                            + BAT_DIE_TEXTURE_PATH + "'.",
                    error);
        }
    }

    private void loadEvilWizardAssets() {
        try {
            evilWizardIdleTex = new Texture(Gdx.files.internal(EVIL_WIZARD_IDLE_TEXTURE_PATH));
            evilWizardIdleTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            int frameSize = Math.max(1, evilWizardIdleTex.getHeight());
            int frameCount = Math.max(1, evilWizardIdleTex.getWidth() / frameSize);
            evilWizardIdleAnim = buildRawAnimation(
                    evilWizardIdleTex,
                    frameCount,
                    frameSize,
                    frameSize,
                    EVIL_WIZARD_IDLE_FRAME_DURATION);
            evilWizardRunTex = new Texture(Gdx.files.internal(EVIL_WIZARD_RUN_TEXTURE_PATH));
            evilWizardRunTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            int runFrameSize = Math.max(1, evilWizardRunTex.getHeight());
            int runFrameCount = Math.max(1, evilWizardRunTex.getWidth() / runFrameSize);
            evilWizardRunAnim = buildRawAnimation(
                    evilWizardRunTex,
                    runFrameCount,
                    runFrameSize,
                    runFrameSize,
                    EVIL_WIZARD_RUN_FRAME_DURATION);
            evilWizardAttack1Tex = new Texture(Gdx.files.internal(EVIL_WIZARD_ATTACK1_TEXTURE_PATH));
            evilWizardAttack1Tex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            int attack1FrameSize = Math.max(1, evilWizardAttack1Tex.getHeight());
            int attack1FrameCount = Math.max(1, evilWizardAttack1Tex.getWidth() / attack1FrameSize);
            evilWizardAttack1Anim = buildRawAnimation(
                    evilWizardAttack1Tex,
                    attack1FrameCount,
                    attack1FrameSize,
                    attack1FrameSize,
                    EVIL_WIZARD_ATTACK_FRAME_DURATION);
            evilWizardAttack2Tex = new Texture(Gdx.files.internal(EVIL_WIZARD_ATTACK2_TEXTURE_PATH));
            evilWizardAttack2Tex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            int attack2FrameSize = Math.max(1, evilWizardAttack2Tex.getHeight());
            int attack2FrameCount = Math.max(1, evilWizardAttack2Tex.getWidth() / attack2FrameSize);
            evilWizardAttack2Anim = buildRawAnimation(
                    evilWizardAttack2Tex,
                    attack2FrameCount,
                    attack2FrameSize,
                    attack2FrameSize,
                    EVIL_WIZARD_ATTACK_FRAME_DURATION);
            evilWizardHurtTex = new Texture(Gdx.files.internal(EVIL_WIZARD_HURT_TEXTURE_PATH));
            evilWizardHurtTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            int hurtFrameSize = Math.max(1, evilWizardHurtTex.getHeight());
            int hurtFrameCount = Math.max(1, evilWizardHurtTex.getWidth() / hurtFrameSize);
            evilWizardHurtAnim = buildRawAnimation(
                    evilWizardHurtTex,
                    hurtFrameCount,
                    hurtFrameSize,
                    hurtFrameSize,
                    EVIL_WIZARD_HURT_FRAME_DURATION);
            evilWizardDieTex = new Texture(Gdx.files.internal(EVIL_WIZARD_DIE_TEXTURE_PATH));
            evilWizardDieTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            int dieFrameSize = Math.max(1, evilWizardDieTex.getHeight());
            int dieFrameCount = Math.max(1, evilWizardDieTex.getWidth() / dieFrameSize);
            evilWizardDieAnim = buildRawAnimation(
                    evilWizardDieTex,
                    dieFrameCount,
                    dieFrameSize,
                    dieFrameSize,
                    EVIL_WIZARD_DIE_FRAME_DURATION);
        } catch (RuntimeException error) {
            evilWizardIdleAnim = null;
            evilWizardRunAnim = null;
            evilWizardAttack1Anim = null;
            evilWizardAttack2Anim = null;
            evilWizardHurtAnim = null;
            evilWizardDieAnim = null;
            clearSpawnedEvilWizards();
            Gdx.app.error(
                    "PixelRpgGame",
                    "Failed to load evil wizard textures from '"
                            + EVIL_WIZARD_IDLE_TEXTURE_PATH + "', '"
                            + EVIL_WIZARD_RUN_TEXTURE_PATH + "', '"
                            + EVIL_WIZARD_ATTACK1_TEXTURE_PATH + "', or '"
                            + EVIL_WIZARD_ATTACK2_TEXTURE_PATH + "', or '"
                            + EVIL_WIZARD_HURT_TEXTURE_PATH + "', or '"
                            + EVIL_WIZARD_DIE_TEXTURE_PATH + "'.",
                    error);
        }
    }

    private void loadGolem1Assets() {
        golem1IdleTex = loadOptionalAbsoluteTexture(GOLEM_1_IDLE_TEXTURE_ABSOLUTE_PATH);
        golem1WalkTex = loadOptionalAbsoluteTexture(GOLEM_1_WALK_TEXTURE_ABSOLUTE_PATH);
        golem1AttackTex = loadOptionalAbsoluteTexture(GOLEM_1_ATTACK_TEXTURE_ABSOLUTE_PATH);
        golem1HurtTex = loadOptionalAbsoluteTexture(GOLEM_1_HURT_TEXTURE_ABSOLUTE_PATH);
        golem1DieTex = loadOptionalAbsoluteTexture(GOLEM_1_DIE_TEXTURE_ABSOLUTE_PATH);
        golem1ExplosionTex = loadOptionalAbsoluteTexture(GOLEM_1_EXPLOSION_TEXTURE_ABSOLUTE_PATH);
        if (golem1IdleTex == null
                && golem1WalkTex == null
                && golem1AttackTex == null
                && golem1HurtTex == null
                && golem1DieTex == null) {
            golem1IdleAnim = null;
            golem1WalkAnim = null;
            golem1AttackAnim = null;
            golem1HurtAnim = null;
            golem1DieAnim = null;
            golem1ExplosionAnim = null;
            clearSpawnedGolem1();
            return;
        }
        if (golem1IdleTex != null) {
            golem1IdleTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            golem1IdleAnim = buildRawAnimation(
                    golem1IdleTex,
                    GOLEM_1_IDLE_FRAMES,
                    GOLEM_1_FRAME_WIDTH,
                    GOLEM_1_FRAME_HEIGHT,
                    GOLEM_1_IDLE_FRAME_DURATION);
        } else {
            golem1IdleAnim = null;
        }
        if (golem1WalkTex != null) {
            golem1WalkTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            golem1WalkAnim = buildRawAnimation(
                    golem1WalkTex,
                    GOLEM_1_WALK_FRAMES,
                    GOLEM_1_FRAME_WIDTH,
                    GOLEM_1_FRAME_HEIGHT,
                    GOLEM_1_WALK_FRAME_DURATION);
        } else {
            golem1WalkAnim = null;
        }
        if (golem1AttackTex != null) {
            golem1AttackTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            golem1AttackAnim = buildRawAnimation(
                    golem1AttackTex,
                    GOLEM_1_ATTACK_FRAMES,
                    GOLEM_1_FRAME_WIDTH,
                    GOLEM_1_FRAME_HEIGHT,
                    GOLEM_1_ATTACK_FRAME_DURATION);
            golem1AttackAnim.setPlayMode(Animation.PlayMode.NORMAL);
        } else {
            golem1AttackAnim = null;
        }
        if (golem1HurtTex != null) {
            golem1HurtTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            golem1HurtAnim = buildRawAnimation(
                    golem1HurtTex,
                    GOLEM_1_HURT_FRAMES,
                    GOLEM_1_FRAME_WIDTH,
                    GOLEM_1_FRAME_HEIGHT,
                    GOLEM_1_HURT_FRAME_DURATION);
            golem1HurtAnim.setPlayMode(Animation.PlayMode.NORMAL);
        } else {
            golem1HurtAnim = null;
        }
        if (golem1DieTex != null) {
            golem1DieTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            int dieCols = Math.max(1, golem1DieTex.getWidth() / GOLEM_1_FRAME_WIDTH);
            int dieRows = Math.max(1, golem1DieTex.getHeight() / GOLEM_1_FRAME_HEIGHT);
            int dieFrames = Math.max(1, dieCols * dieRows);
            golem1DieAnim = buildRawAnimation(
                    golem1DieTex,
                    dieFrames,
                    GOLEM_1_FRAME_WIDTH,
                    GOLEM_1_FRAME_HEIGHT,
                    GOLEM_1_DIE_FRAME_DURATION);
            golem1DieAnim.setPlayMode(Animation.PlayMode.NORMAL);
        } else {
            golem1DieAnim = null;
        }
        if (golem1ExplosionTex != null) {
            golem1ExplosionTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            golem1ExplosionAnim = buildRawAnimation(
                    golem1ExplosionTex,
                    GOLEM_1_EXPLOSION_FRAMES,
                    GOLEM_1_EXPLOSION_FRAME_WIDTH,
                    GOLEM_1_EXPLOSION_FRAME_HEIGHT,
                    GOLEM_1_EXPLOSION_FRAME_DURATION);
            golem1ExplosionAnim.setPlayMode(Animation.PlayMode.NORMAL);
        } else {
            golem1ExplosionAnim = null;
        }
    }

    private void loadChestAssets() {
        chestIdleTex = loadOptionalAbsoluteTexture(CHEST_IDLE_TEXTURE_ABSOLUTE_PATH);
        chestOpenTex = loadOptionalAbsoluteTexture(CHEST_OPEN_TEXTURE_ABSOLUTE_PATH);
        if (chestIdleTex == null && chestOpenTex == null) {
            chestIdleAnim = null;
            chestOpenAnim = null;
            clearSpawnedRewardChests();
            return;
        }

        if (chestIdleTex != null) {
            chestIdleTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            int frameSize = Math.max(1, chestIdleTex.getHeight());
            int frameCount = Math.max(1, chestIdleTex.getWidth() / frameSize);
            chestIdleAnim = buildRawAnimation(
                    chestIdleTex,
                    frameCount,
                    frameSize,
                    frameSize,
                    CHEST_IDLE_FRAME_DURATION);
        } else {
            chestIdleAnim = null;
        }

        if (chestOpenTex != null) {
            chestOpenTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            int frameSize = Math.max(1, chestOpenTex.getHeight());
            int frameCount = Math.max(1, chestOpenTex.getWidth() / frameSize);
            chestOpenAnim = buildRawAnimation(
                    chestOpenTex,
                    frameCount,
                    frameSize,
                    frameSize,
                    CHEST_OPEN_FRAME_DURATION);
            chestOpenAnim.setPlayMode(Animation.PlayMode.NORMAL);
        } else {
            chestOpenAnim = null;
        }
    }

    private void loadBossArenaPhase2DemonAssets() {
        disposeBossArenaPhase2DemonFrameTextures();
        if (bossArenaPhase2DemonIdleTex != null) {
            bossArenaPhase2DemonIdleTex.dispose();
            bossArenaPhase2DemonIdleTex = null;
        }

        bossArenaPhase2DemonIdleAnim = loadOptionalAbsoluteFrameAnimation(
                BOSS_ARENA_PHASE_2_DEMON_IDLE_FRAMES_DIR_ABSOLUTE_PATH,
                BOSS_ARENA_PHASE_2_DEMON_IDLE_FRAME_DURATION,
                bossArenaPhase2DemonIdleFrameTextures);
        bossArenaPhase2DemonWalkAnim = loadOptionalAbsoluteFrameAnimation(
                BOSS_ARENA_PHASE_2_DEMON_WALK_FRAMES_DIR_ABSOLUTE_PATH,
                BOSS_ARENA_PHASE_2_DEMON_WALK_FRAME_DURATION,
                bossArenaPhase2DemonWalkFrameTextures);
        bossArenaPhase2DemonAttackAnim = loadOptionalAbsoluteFrameAnimation(
                BOSS_ARENA_PHASE_2_DEMON_ATTACK_FRAMES_DIR_ABSOLUTE_PATH,
                BOSS_ARENA_PHASE_2_DEMON_ATTACK_FRAME_DURATION,
                bossArenaPhase2DemonAttackFrameTextures);
        bossArenaPhase2DemonHurtAnim = loadOptionalAbsoluteFrameAnimation(
                BOSS_ARENA_PHASE_2_DEMON_HURT_FRAMES_DIR_ABSOLUTE_PATH,
                BOSS_ARENA_PHASE_2_DEMON_HURT_FRAME_DURATION,
                bossArenaPhase2DemonHurtFrameTextures);
        if (bossArenaPhase2DemonAttackAnim != null) {
            bossArenaPhase2DemonAttackAnim.setPlayMode(Animation.PlayMode.NORMAL);
        }
        if (bossArenaPhase2DemonHurtAnim != null) {
            bossArenaPhase2DemonHurtAnim.setPlayMode(Animation.PlayMode.NORMAL);
        }
        if (bossArenaPhase2DemonIdleAnim != null
                || bossArenaPhase2DemonWalkAnim != null
                || bossArenaPhase2DemonAttackAnim != null
                || bossArenaPhase2DemonHurtAnim != null) {
            return;
        }

        bossArenaPhase2DemonIdleTex = loadOptionalAbsoluteTexture(BOSS_ARENA_PHASE_2_DEMON_IDLE_TEXTURE_ABSOLUTE_PATH);
        if (bossArenaPhase2DemonIdleTex == null) {
            bossArenaPhase2DemonIdleAnim = null;
            bossArenaPhase2DemonWalkAnim = null;
            bossArenaPhase2DemonAttackAnim = null;
            bossArenaPhase2DemonHurtAnim = null;
            clearSpawnedBossArenaPhase2Demons();
            return;
        }
        bossArenaPhase2DemonIdleTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        bossArenaPhase2DemonIdleAnim = buildRawAnimation(
                bossArenaPhase2DemonIdleTex,
                BOSS_ARENA_PHASE_2_DEMON_IDLE_FRAMES,
                BOSS_ARENA_PHASE_2_DEMON_FRAME_WIDTH,
                BOSS_ARENA_PHASE_2_DEMON_FRAME_HEIGHT,
                BOSS_ARENA_PHASE_2_DEMON_IDLE_FRAME_DURATION);
        bossArenaPhase2DemonWalkAnim = null;
        bossArenaPhase2DemonAttackAnim = null;
        bossArenaPhase2DemonHurtAnim = null;
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
        // Keep fight-zone darkness behind all actors (player, wizards, golems).
        drawBossFightZoneDarknessOverlay();
        drawGolemFightZoneDarknessOverlay();
        drawEvilWizardAttackHazardOverlay();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        float characterSaturation = lerp(CHARACTER_SATURATION, NIGHT_CHARACTER_SATURATION, nightBlend);
        drawEvilWizards(characterSaturation, true);
        drawGolem1s(characterSaturation, true);
        drawBossArenaPhase2Demons(characterSaturation, true);
        drawRewardChests(characterSaturation, true);
        drawPlayer(characterSaturation);
        drawSpawnedBat(characterSaturation);
        drawRewardChests(characterSaturation, false);
        drawBossArenaPhase2Demons(characterSaturation, false);
        drawGolem1s(characterSaturation, false);
        drawEvilWizards(characterSaturation, false);
        // Explosion effects should sit above both character and golems.
        drawGolem1Explosions(characterSaturation);
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

    private void drawBossFightZoneDarknessOverlay() {
        if (!bossFightZoneDarkMode
                || dayNightOverlayTex == null
                || camera == null
                || bossFightZoneBounds.width <= 0f
                || bossFightZoneBounds.height <= 0f) {
            return;
        }
        float transitionProgress = BOSS_FIGHT_ZONE_DARK_TRANSITION_SECONDS <= 0f
                ? 1f
                : clamp01(bossFightZoneDarkTransitionTimer / BOSS_FIGHT_ZONE_DARK_TRANSITION_SECONDS);
        float smoothProgress = transitionProgress * transitionProgress * (3f - 2f * transitionProgress);
        float overlayAlpha = BOSS_FIGHT_ZONE_DARK_MAX_ALPHA * smoothProgress;
        if (overlayAlpha <= 0.001f) {
            return;
        }

        float viewWidth = camera.viewportWidth * camera.zoom;
        float viewHeight = camera.viewportHeight * camera.zoom;
        float viewLeft = camera.position.x - viewWidth * 0.5f;
        float viewBottom = camera.position.y - viewHeight * 0.5f;
        float viewRight = viewLeft + viewWidth;
        float viewTop = viewBottom + viewHeight;

        float zoneLeft = bossFightZoneBounds.x;
        float zoneBottom = bossFightZoneBounds.y;
        float zoneRight = zoneLeft + bossFightZoneBounds.width;
        float zoneTop = zoneBottom + bossFightZoneBounds.height;

        float innerLeft = Math.max(viewLeft, zoneLeft);
        float innerRight = Math.min(viewRight, zoneRight);
        float innerBottom = Math.max(viewBottom, zoneBottom);
        float innerTop = Math.min(viewTop, zoneTop);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setShader(null);
        batch.setColor(0f, 0f, 0f, overlayAlpha);

        if (innerLeft >= innerRight || innerBottom >= innerTop) {
            batch.draw(dayNightOverlayTex, viewLeft, viewBottom, viewWidth, viewHeight);
        } else {
            drawOverlayRect(viewLeft, viewBottom, innerLeft - viewLeft, viewHeight);
            drawOverlayRect(innerRight, viewBottom, viewRight - innerRight, viewHeight);
            drawOverlayRect(innerLeft, viewBottom, innerRight - innerLeft, innerBottom - viewBottom);
            drawOverlayRect(innerLeft, innerTop, innerRight - innerLeft, viewTop - innerTop);
        }

        batch.setColor(1f, 1f, 1f, 1f);
        batch.end();
    }

    private void drawGolemFightZoneDarknessOverlay() {
        if (!golemFightZoneDarkMode
                || dayNightOverlayTex == null
                || camera == null
                || golemFightZoneBounds.width <= 0f
                || golemFightZoneBounds.height <= 0f) {
            return;
        }
        float transitionProgress = BOSS_FIGHT_ZONE_DARK_TRANSITION_SECONDS <= 0f
                ? 1f
                : clamp01(golemFightZoneDarkTransitionTimer / BOSS_FIGHT_ZONE_DARK_TRANSITION_SECONDS);
        float smoothProgress = transitionProgress * transitionProgress * (3f - 2f * transitionProgress);
        float overlayAlpha = BOSS_FIGHT_ZONE_DARK_MAX_ALPHA * smoothProgress;
        if (overlayAlpha <= 0.001f) {
            return;
        }

        float viewWidth = camera.viewportWidth * camera.zoom;
        float viewHeight = camera.viewportHeight * camera.zoom;
        float viewLeft = camera.position.x - viewWidth * 0.5f;
        float viewBottom = camera.position.y - viewHeight * 0.5f;
        float viewRight = viewLeft + viewWidth;
        float viewTop = viewBottom + viewHeight;

        float zoneLeft = golemFightZoneBounds.x;
        float zoneBottom = golemFightZoneBounds.y;
        float zoneRight = zoneLeft + golemFightZoneBounds.width;
        float zoneTop = zoneBottom + golemFightZoneBounds.height;

        float innerLeft = Math.max(viewLeft, zoneLeft);
        float innerRight = Math.min(viewRight, zoneRight);
        float innerBottom = Math.max(viewBottom, zoneBottom);
        float innerTop = Math.min(viewTop, zoneTop);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setShader(null);
        batch.setColor(0f, 0f, 0f, overlayAlpha);

        if (innerLeft >= innerRight || innerBottom >= innerTop) {
            batch.draw(dayNightOverlayTex, viewLeft, viewBottom, viewWidth, viewHeight);
        } else {
            drawOverlayRect(viewLeft, viewBottom, innerLeft - viewLeft, viewHeight);
            drawOverlayRect(innerRight, viewBottom, viewRight - innerRight, viewHeight);
            drawOverlayRect(innerLeft, viewBottom, innerRight - innerLeft, innerBottom - viewBottom);
            drawOverlayRect(innerLeft, innerTop, innerRight - innerLeft, viewTop - innerTop);
        }

        batch.setColor(1f, 1f, 1f, 1f);
        batch.end();
    }

    private void drawOverlayRect(float x, float y, float width, float height) {
        if (width <= 0f || height <= 0f) {
            return;
        }
        batch.draw(dayNightOverlayTex, x, y, width, height);
    }

    private void drawEvilWizardAttackHazardOverlay() {
        if (!evilWizardAttackHazardActive
                || dayNightOverlayTex == null
                || camera == null
                || evilWizardAttackHazardZoneBounds.width <= 0f
                || evilWizardAttackHazardZoneBounds.height <= 0f) {
            return;
        }
        float viewWidth = camera.viewportWidth * camera.zoom;
        float viewHeight = camera.viewportHeight * camera.zoom;
        float viewLeft = camera.position.x - viewWidth * 0.5f;
        float viewBottom = camera.position.y - viewHeight * 0.5f;
        float viewRight = viewLeft + viewWidth;
        float viewTop = viewBottom + viewHeight;

        float zoneLeft = evilWizardAttackHazardZoneBounds.x;
        float zoneBottom = evilWizardAttackHazardZoneBounds.y;
        float zoneRight = zoneLeft + evilWizardAttackHazardZoneBounds.width;
        float zoneTop = zoneBottom + evilWizardAttackHazardZoneBounds.height;

        float drawLeft = Math.max(viewLeft, zoneLeft);
        float drawRight = Math.min(viewRight, zoneRight);
        float drawBottom = Math.max(viewBottom, zoneBottom);
        float drawTop = Math.min(viewTop, zoneTop);
        if (drawLeft >= drawRight || drawBottom >= drawTop) {
            return;
        }

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setShader(null);
        float transitionAlpha = EVIL_WIZARD_ATTACK_OVERLAY_TRANSITION_SECONDS <= 0f
                ? 1f
                : clamp01(evilWizardAttackHazardElapsedTimer / EVIL_WIZARD_ATTACK_OVERLAY_TRANSITION_SECONDS);
        float explosionProgress = evilWizardAttackHazardExplosionProgress();
        float explosionBlend = smoothStep(explosionProgress);
        float overlayAlpha = EVIL_WIZARD_ATTACK_OVERLAY_ALPHA * transitionAlpha;
        float overlayR = lerp(EVIL_WIZARD_ATTACK_OVERLAY_R, Math.min(1f, EVIL_WIZARD_ATTACK_OVERLAY_R + 0.5f), explosionBlend);
        float overlayG = lerp(EVIL_WIZARD_ATTACK_OVERLAY_G, Math.min(1f, EVIL_WIZARD_ATTACK_OVERLAY_G + 0.14f), explosionBlend);
        float overlayB = lerp(EVIL_WIZARD_ATTACK_OVERLAY_B, Math.min(1f, EVIL_WIZARD_ATTACK_OVERLAY_B + 0.45f), explosionBlend);
        float explodedAlpha = clamp01(overlayAlpha + 0.36f * explosionBlend);
        batch.setColor(
                overlayR,
                overlayG,
                overlayB,
                explodedAlpha);
        drawHazardRectWithSafeCircleHoles(drawLeft, drawBottom, drawRight, drawTop);
        drawEvilWizardAttackExplosionPulse(drawLeft, drawBottom, drawRight, drawTop, explosionProgress);

        batch.setColor(1f, 1f, 1f, 1f);
        batch.end();
    }

    private float evilWizardAttackHazardExplosionProgress() {
        if (!evilWizardAttackHazardActive || !evilWizardAttackHazardExplosionTriggered) {
            return 0f;
        }
        if (EVIL_WIZARD_ATTACK_HAZARD_EXPLOSION_WINDOW_SECONDS <= 0f) {
            return 1f;
        }
        return 1f - clamp01(evilWizardAttackHazardTimer / EVIL_WIZARD_ATTACK_HAZARD_EXPLOSION_WINDOW_SECONDS);
    }

    private void drawEvilWizardAttackExplosionPulse(float left, float bottom, float right, float top, float progress) {
        if (progress <= 0f || right <= left || top <= bottom) {
            return;
        }
        float smoothProgress = smoothStep(progress);
        float fadeOut = 1f - smoothProgress;

        float flashPhase = clamp01(progress / 0.35f);
        float flashAlpha = (1f - flashPhase) * 0.72f;
        if (flashAlpha > 0.001f) {
            batch.setColor(
                    Math.min(1f, EVIL_WIZARD_ATTACK_OVERLAY_R + 0.7f),
                    Math.min(1f, EVIL_WIZARD_ATTACK_OVERLAY_G + 0.24f),
                    Math.min(1f, EVIL_WIZARD_ATTACK_OVERLAY_B + 0.62f),
                    flashAlpha);
            drawHazardRectWithSafeCircleHoles(left, bottom, right, top);
        }

        drawCenteredExplosionLayer(
                left, bottom, right, top,
                clamp01(progress * 1.35f),
                0.88f * fadeOut,
                Math.min(1f, EVIL_WIZARD_ATTACK_OVERLAY_R + 0.62f),
                Math.min(1f, EVIL_WIZARD_ATTACK_OVERLAY_G + 0.2f),
                Math.min(1f, EVIL_WIZARD_ATTACK_OVERLAY_B + 0.55f));

        float trailingProgress = clamp01((progress - 0.12f) / 0.88f);
        float trailingFade = 1f - smoothStep(trailingProgress);
        drawCenteredExplosionLayer(
                left, bottom, right, top,
                trailingProgress,
                0.62f * trailingFade,
                Math.min(1f, EVIL_WIZARD_ATTACK_OVERLAY_R + 0.46f),
                Math.min(1f, EVIL_WIZARD_ATTACK_OVERLAY_G + 0.15f),
                Math.min(1f, EVIL_WIZARD_ATTACK_OVERLAY_B + 0.44f));

        float coreProgress = clamp01(progress / 0.55f);
        float coreFade = 1f - smoothStep(coreProgress);
        drawCenteredExplosionLayer(
                left, bottom, right, top,
                0.38f + coreProgress * 0.2f,
                0.42f * coreFade,
                Math.min(1f, EVIL_WIZARD_ATTACK_OVERLAY_R + 0.78f),
                Math.min(1f, EVIL_WIZARD_ATTACK_OVERLAY_G + 0.3f),
                Math.min(1f, EVIL_WIZARD_ATTACK_OVERLAY_B + 0.72f));
    }

    private void drawCenteredExplosionLayer(float left, float bottom, float right, float top,
                                            float progress, float alpha,
                                            float red, float green, float blue) {
        if (alpha <= 0.001f || right <= left || top <= bottom) {
            return;
        }
        float centerX = (left + right) * 0.5f;
        float centerY = (bottom + top) * 0.5f;
        float clampedProgress = clamp01(progress);
        float halfWidth = (right - left) * 0.5f * clampedProgress;
        float halfHeight = (top - bottom) * 0.5f * clampedProgress;
        if (halfWidth <= 0.001f || halfHeight <= 0.001f) {
            return;
        }
        float pulseLeft = centerX - halfWidth;
        float pulseRight = centerX + halfWidth;
        float pulseBottom = centerY - halfHeight;
        float pulseTop = centerY + halfHeight;
        batch.setColor(red, green, blue, clamp01(alpha));
        drawHazardRectWithSafeCircleHoles(pulseLeft, pulseBottom, pulseRight, pulseTop);
    }

    private void drawHazardRectWithSafeCircleHoles(float left, float bottom, float right, float top) {
        if (right <= left || top <= bottom) {
            return;
        }
        if (evilWizardSafeCircleCenters.size == 0) {
            batch.draw(dayNightOverlayTex, left, bottom, right - left, top - bottom);
            return;
        }

        float radius = EVIL_WIZARD_SAFE_CIRCLE_RADIUS_TILES * DEFAULT_TILE_SIZE;
        float radiusSq = radius * radius;
        float stripHeight = Math.max(1f, EVIL_WIZARD_SAFE_CIRCLE_HOLE_STRIP_HEIGHT);

        float[] starts = new float[EVIL_WIZARD_SAFE_CIRCLE_MAX_COUNT];
        float[] ends = new float[EVIL_WIZARD_SAFE_CIRCLE_MAX_COUNT];

        for (float y = bottom; y < top; y += stripHeight) {
            float stripTop = Math.min(top, y + stripHeight);
            float yMid = (y + stripTop) * 0.5f;
            int intervalCount = 0;

            for (int i = 0; i < evilWizardSafeCircleCenters.size && intervalCount < EVIL_WIZARD_SAFE_CIRCLE_MAX_COUNT; i++) {
                Vector2 center = evilWizardSafeCircleCenters.get(i);
                float dy = Math.abs(yMid - center.y);
                if (dy >= radius) {
                    continue;
                }
                float spanX = (float) Math.sqrt(Math.max(0f, radiusSq - dy * dy));
                float start = Math.max(left, center.x - spanX);
                float end = Math.min(right, center.x + spanX);
                if (end <= start) {
                    continue;
                }
                starts[intervalCount] = start;
                ends[intervalCount] = end;
                intervalCount++;
            }

            if (intervalCount == 0) {
                batch.draw(dayNightOverlayTex, left, y, right - left, stripTop - y);
                continue;
            }

            for (int i = 1; i < intervalCount; i++) {
                float keyStart = starts[i];
                float keyEnd = ends[i];
                int j = i - 1;
                while (j >= 0 && starts[j] > keyStart) {
                    starts[j + 1] = starts[j];
                    ends[j + 1] = ends[j];
                    j--;
                }
                starts[j + 1] = keyStart;
                ends[j + 1] = keyEnd;
            }

            float cursor = left;
            int idx = 0;
            while (idx < intervalCount) {
                float holeStart = starts[idx];
                float holeEnd = ends[idx];
                idx++;
                while (idx < intervalCount && starts[idx] <= holeEnd) {
                    holeEnd = Math.max(holeEnd, ends[idx]);
                    idx++;
                }
                if (holeStart > cursor) {
                    batch.draw(dayNightOverlayTex, cursor, y, holeStart - cursor, stripTop - y);
                }
                cursor = Math.max(cursor, holeEnd);
            }
            if (cursor < right) {
                batch.draw(dayNightOverlayTex, cursor, y, right - cursor, stripTop - y);
            }
        }
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
        if (spetialDashTex != null && region.getTexture() == spetialDashTex) {
            return new TextureRegion(region, SPETIAL_DASH_TRIM_LEFT, SPETIAL_DASH_TRIM_TOP,
                    SPETIAL_DASH_TRIM_WIDTH, SPETIAL_DASH_TRIM_HEIGHT);
        }
        if (attack1Tex != null && region.getTexture() == attack1Tex) {
            return new TextureRegion(region, ATTACK1_TRIM_LEFT, ATTACK1_TRIM_TOP,
                    ATTACK1_TRIM_WIDTH, ATTACK1_TRIM_HEIGHT);
        }
        return new TextureRegion(region, SPRITE_TRIM_LEFT, SPRITE_TRIM_TOP, SPRITE_TRIM_WIDTH, SPRITE_TRIM_HEIGHT);
    }

    private TextureRegion currentFrame() {
        if (isPlayerHazardHitReactionActive() && playerHitAnim != null) {
            return playerHitAnim.getKeyFrame(playerHazardHitStateTime, false);
        }
        if (player.attacking && activeAttackAnim != null) {
            return activeAttackAnim.getKeyFrame(player.attackTimer);
        }
        return (player.moving ? runAnim : idleAnim).getKeyFrame(player.stateTime);
    }

    private boolean isDashAttackAnimationActive() {
        return player.attacking && isTeleportDashAnimation(activeAttackAnim);
    }

    private boolean isTeleportDashAnimation(Animation<TextureRegion> animation) {
        return animation != null && (animation == dashAttackAnim || animation == spetialDashAnim);
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
        if (!isDashAttackAnimationActive()) {
            batch.setColor(1f, 1f, 1f, SHADOW_ALPHA);
            batch.draw(shadowRegion, shadowX, shadowY, shadowW, shadowH);
            batch.setColor(1f, 1f, 1f, 1f);
        }

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

    private void drawGolem1s(float saturation, boolean drawBehindPlayer) {
        if (golem1s.size == 0
                || (golem1WalkAnim == null
                && golem1IdleAnim == null
                && golem1AttackAnim == null
                && golem1HurtAnim == null
                && golem1DieAnim == null)) {
            return;
        }

        float playerDepthBasisY = player.bounds.y;
        if (shadowRegion != null) {
            float shadowW = GOLEM_1_SHADOW_WIDTH_TILES * DEFAULT_TILE_SIZE;
            float shadowH = GOLEM_1_SHADOW_HEIGHT_TILES * DEFAULT_TILE_SIZE;
            float shadowDownOffset = GOLEM_1_SHADOW_DOWN_OFFSET_TILES * DEFAULT_TILE_SIZE;
            batch.setShader(null);
            batch.setColor(1f, 1f, 1f, GOLEM_1_SHADOW_ALPHA);
            for (int i = 0; i < golem1s.size; i++) {
                Golem1Instance golem = golem1s.get(i);
                float golemDepthBasisY = golem.position.y;
                boolean golemBehindPlayer = golemDepthBasisY > playerDepthBasisY;
                if (golemBehindPlayer != drawBehindPlayer) {
                    continue;
                }
                float shadowX = golem.position.x - shadowW * 0.5f;
                float shadowY = golemDepthBasisY - shadowH * 0.25f - shadowDownOffset;
                batch.draw(shadowRegion, shadowX, shadowY, shadowW, shadowH);
            }
            batch.setColor(1f, 1f, 1f, 1f);
        }
        if (saturationShader != null) {
            batch.setShader(saturationShader);
            saturationShader.setUniformf("u_saturation", saturation);
        }
        for (int i = 0; i < golem1s.size; i++) {
            Golem1Instance golem = golem1s.get(i);
            float golemDepthBasisY = golem.position.y;
            boolean golemBehindPlayer = golemDepthBasisY > playerDepthBasisY;
            if (golemBehindPlayer != drawBehindPlayer) {
                continue;
            }

            Animation<TextureRegion> activeAnimation;
            boolean loop = true;
            if (golem.dying && golem1DieAnim != null) {
                activeAnimation = golem1DieAnim;
                loop = false;
            } else if (golem.hurting && golem1HurtAnim != null) {
                activeAnimation = golem1HurtAnim;
                loop = false;
            } else if (golem.attacking && golem1AttackAnim != null) {
                activeAnimation = golem1AttackAnim;
                loop = false;
            } else {
                activeAnimation = golem.moving && golem1WalkAnim != null
                        ? golem1WalkAnim
                        : (golem1IdleAnim != null ? golem1IdleAnim : golem1WalkAnim);
            }
            if (activeAnimation == null) {
                continue;
            }
            float animationTime;
            if (golem.dying) {
                animationTime = golem.dieStateTime;
            } else if (golem.hurting) {
                animationTime = golem.hurtStateTime;
            } else if (golem.attacking) {
                animationTime = golem.attackStateTime;
            } else {
                animationTime = golem.stateTime;
            }
            TextureRegion frame = activeAnimation.getKeyFrame(animationTime, loop);
            float drawW = frame.getRegionWidth() * GOLEM_1_DRAW_SCALE;
            float drawH = frame.getRegionHeight() * GOLEM_1_DRAW_SCALE;
            float drawX = golem.position.x - drawW * 0.5f;
            float drawY = golem.position.y - drawH * 0.5f;
            float originX = drawW * 0.5f;
            float originY = drawH * 0.5f;
            float scaleX = golem.facingLeft ? -1f : 1f;
            batch.draw(frame, drawX, drawY, originX, originY, drawW, drawH, scaleX, 1f, 0f);
        }
        if (saturationShader != null) {
            batch.setShader(null);
        }

        if (dayNightOverlayTex != null) {
            for (int i = 0; i < golem1s.size; i++) {
                Golem1Instance golem = golem1s.get(i);
                if (golem.dying || golem.health <= 0f) {
                    continue;
                }
                float golemDepthBasisY = golem.position.y;
                boolean golemBehindPlayer = golemDepthBasisY > playerDepthBasisY;
                if (golemBehindPlayer != drawBehindPlayer) {
                    continue;
                }
                float hpRatio = golem.maxHealth > 0f ? golem.health / golem.maxHealth : 0f;
                hpRatio = clamp01(hpRatio);
                float frameSize = golem1FrameSize() * GOLEM_1_DRAW_SCALE;
                float hpX = golem.position.x - BAT_HP_BAR_WIDTH * 0.5f;
                float hpY = golem.position.y
                        + frameSize * 0.5f
                        + BAT_HP_BAR_OFFSET_Y
                        - GOLEM_1_HP_BAR_DOWN_OFFSET_TILES * DEFAULT_TILE_SIZE;
                batch.setColor(0f, 0f, 0f, 0.85f);
                batch.draw(dayNightOverlayTex, hpX - 0.5f, hpY - 0.5f,
                        BAT_HP_BAR_WIDTH + 1f, BAT_HP_BAR_HEIGHT + 1f);
                batch.setColor(0.15f, 0.15f, 0.15f, 1f);
                batch.draw(dayNightOverlayTex, hpX, hpY, BAT_HP_BAR_WIDTH, BAT_HP_BAR_HEIGHT);
                if (hpRatio > 0f) {
                    batch.setColor(0.2f, 0.95f, 0.2f, 1f);
                    batch.draw(dayNightOverlayTex, hpX, hpY, BAT_HP_BAR_WIDTH * hpRatio, BAT_HP_BAR_HEIGHT);
                }
            }
            batch.setColor(1f, 1f, 1f, 1f);
        }
    }

    private void drawRewardChests(float saturation, boolean drawBehindPlayer) {
        if (rewardChests.size == 0 || (chestIdleAnim == null && chestOpenAnim == null)) {
            return;
        }
        float playerDepthBasisY = player.bounds.y;
        if (saturationShader != null) {
            batch.setShader(saturationShader);
            saturationShader.setUniformf("u_saturation", saturation);
        }
        for (int i = 0; i < rewardChests.size; i++) {
            RewardChestInstance chest = rewardChests.get(i);
            float chestDepthBasisY = chest.position.y;
            boolean chestBehindPlayer = chestDepthBasisY > playerDepthBasisY;
            if (chestBehindPlayer != drawBehindPlayer) {
                continue;
            }
            TextureRegion frame = resolveRewardChestFrame(chest);
            if (frame == null) {
                continue;
            }
            float drawW = (chest.drawWidth > 0f ? chest.drawWidth : frame.getRegionWidth()) * CHEST_DRAW_SCALE;
            float drawH = (chest.drawHeight > 0f ? chest.drawHeight : frame.getRegionHeight()) * CHEST_DRAW_SCALE;
            float drawX = chest.position.x - drawW * 0.5f;
            float drawY = chest.position.y - drawH * 0.5f;
            batch.draw(frame, drawX, drawY, drawW, drawH);
        }
        if (saturationShader != null) {
            batch.setShader(null);
        }
    }

    private void drawBossArenaPhase2Demons(float saturation, boolean drawBehindPlayer) {
        if (bossArenaPhase2Demons.size == 0
                || (bossArenaPhase2DemonIdleAnim == null
                && bossArenaPhase2DemonWalkAnim == null
                && bossArenaPhase2DemonAttackAnim == null
                && bossArenaPhase2DemonHurtAnim == null)) {
            return;
        }
        float playerDepthBasisY = player.bounds.y;
        if (saturationShader != null) {
            batch.setShader(saturationShader);
            saturationShader.setUniformf("u_saturation", saturation);
        }
        for (int i = 0; i < bossArenaPhase2Demons.size; i++) {
            BossArenaPhase2DemonInstance demon = bossArenaPhase2Demons.get(i);
            float demonDepthBasisY = demon.position.y;
            boolean demonBehindPlayer = demonDepthBasisY > playerDepthBasisY;
            if (demonBehindPlayer != drawBehindPlayer) {
                continue;
            }
            boolean loop = true;
            Animation<TextureRegion> activeAnim;
            if (demon.hurting && bossArenaPhase2DemonHurtAnim != null) {
                activeAnim = bossArenaPhase2DemonHurtAnim;
                loop = false;
            } else if (demon.attacking && bossArenaPhase2DemonAttackAnim != null) {
                activeAnim = bossArenaPhase2DemonAttackAnim;
                loop = false;
            } else if (demon.moving && bossArenaPhase2DemonWalkAnim != null) {
                activeAnim = bossArenaPhase2DemonWalkAnim;
            } else if (bossArenaPhase2DemonIdleAnim != null) {
                activeAnim = bossArenaPhase2DemonIdleAnim;
            } else {
                if (bossArenaPhase2DemonWalkAnim != null) {
                    activeAnim = bossArenaPhase2DemonWalkAnim;
                } else if (bossArenaPhase2DemonAttackAnim != null) {
                    activeAnim = bossArenaPhase2DemonAttackAnim;
                } else {
                    activeAnim = bossArenaPhase2DemonHurtAnim;
                }
            }
            if (activeAnim == null) {
                continue;
            }
            float animationTime;
            if (demon.hurting) {
                animationTime = demon.hurtStateTime;
            } else if (demon.attacking) {
                animationTime = demon.attackStateTime;
            } else {
                animationTime = demon.stateTime;
            }
            TextureRegion frame = activeAnim.getKeyFrame(animationTime, loop);
            if (frame == null) {
                continue;
            }
            float drawW = frame.getRegionWidth() * BOSS_ARENA_PHASE_2_DEMON_DRAW_SCALE;
            float drawH = frame.getRegionHeight() * BOSS_ARENA_PHASE_2_DEMON_DRAW_SCALE;
            float drawX = demon.position.x - drawW * 0.5f;
            float drawY = demon.position.y - drawH * 0.5f;
            float originX = drawW * 0.5f;
            float originY = drawH * 0.5f;
            // Demon idle source art is right-facing, so invert to match logical facing.
            float scaleX = demon.facingLeft ? 1f : -1f;
            batch.draw(frame, drawX, drawY, originX, originY, drawW, drawH, scaleX, 1f, 0f);
        }
        if (saturationShader != null) {
            batch.setShader(null);
        }
    }

    private TextureRegion resolveRewardChestFrame(RewardChestInstance chest) {
        if (chest == null) {
            return null;
        }
        if ((chest.opening || chest.opened) && chestOpenAnim != null) {
            return chestOpenAnim.getKeyFrame(chest.openStateTime, false);
        }
        if (chestIdleAnim != null) {
            return chestIdleAnim.getKeyFrame(chest.stateTime, true);
        }
        if (chestOpenAnim != null) {
            return chestOpenAnim.getKeyFrame(chest.openStateTime, false);
        }
        return null;
    }

    private void drawGolem1Explosions(float saturation) {
        if (golem1s.size == 0 || golem1ExplosionAnim == null) {
            return;
        }

        if (saturationShader != null) {
            batch.setShader(saturationShader);
            saturationShader.setUniformf("u_saturation", saturation);
        }
        for (int i = 0; i < golem1s.size; i++) {
            Golem1Instance golem = golem1s.get(i);
            for (int j = 0; j < golem.explosions.size; j++) {
                Golem1ExplosionInstance burst = golem.explosions.get(j);
                if (!burst.active) {
                    continue;
                }
                TextureRegion frame = golem1ExplosionAnim.getKeyFrame(burst.stateTime, false);
                float drawW = frame.getRegionWidth() * GOLEM_1_EXPLOSION_DRAW_SCALE;
                float drawH = frame.getRegionHeight() * GOLEM_1_EXPLOSION_DRAW_SCALE;
                float drawX = burst.position.x - drawW * 0.5f;
                float drawY = burst.position.y - drawH * 0.5f;
                batch.draw(frame, drawX, drawY, drawW, drawH);
            }
        }
        if (saturationShader != null) {
            batch.setShader(null);
        }
    }

    private void drawBossArenaPhase2DetectionDebug() {
        if (bossArenaPhase2Demons.size == 0) {
            return;
        }
        if (dayNightOverlayTex == null && shadowRegion == null) {
            return;
        }

        batch.setShader(null);
        float playerCenterX = player.bounds.x + player.bounds.width * 0.5f;
        float playerCenterY = player.bounds.y + player.bounds.height * 0.5f;

        float characterMarkerRadius = BOSS_ARENA_PHASE_2_DETECTION_CHARACTER_MARKER_RADIUS_TILES * DEFAULT_TILE_SIZE;
        float characterMarkerDiameter = characterMarkerRadius * 2f;
        batch.setColor(0.12f, 1f, 0.58f, 0.75f);
        if (shadowRegion != null) {
            batch.draw(
                    shadowRegion,
                    playerCenterX - characterMarkerRadius,
                    playerCenterY - characterMarkerRadius,
                    characterMarkerDiameter,
                    characterMarkerDiameter);
        } else if (dayNightOverlayTex != null) {
            batch.draw(
                    dayNightOverlayTex,
                    playerCenterX - characterMarkerRadius,
                    playerCenterY - characterMarkerRadius,
                    characterMarkerDiameter,
                    characterMarkerDiameter);
        }

        float detectionRadius = BOSS_ARENA_PHASE_2_DEMON_ATTACK_RANGE;
        float detectionDiameter = detectionRadius * 2f;
        float demonMarkerRadius = BOSS_ARENA_PHASE_2_DETECTION_DEMON_MARKER_RADIUS_TILES * DEFAULT_TILE_SIZE;
        float demonMarkerDiameter = demonMarkerRadius * 2f;
        float lineThickness = Math.max(1f, BOSS_ARENA_PHASE_2_DETECTION_DEBUG_LINE_THICKNESS);

        for (int i = 0; i < bossArenaPhase2Demons.size; i++) {
            BossArenaPhase2DemonInstance demon = bossArenaPhase2Demons.get(i);
            if (demon == null) {
                continue;
            }
            float centerX = bossArenaPhase2DemonDetectionCenterX(demon);
            float centerY = bossArenaPhase2DemonDetectionCenterY(demon);

            batch.setColor(1f, 0.15f, 0.12f, BOSS_ARENA_PHASE_2_DETECTION_DEBUG_ALPHA);
            if (shadowRegion != null) {
                batch.draw(
                        shadowRegion,
                        centerX - detectionRadius,
                        centerY - detectionRadius,
                        detectionDiameter,
                        detectionDiameter);
            } else if (dayNightOverlayTex != null) {
                batch.draw(
                        dayNightOverlayTex,
                        centerX - detectionRadius,
                        centerY - detectionRadius,
                        detectionDiameter,
                        detectionDiameter);
            }

            batch.setColor(1f, 0.7f, 0.15f, 0.8f);
            if (shadowRegion != null) {
                batch.draw(
                        shadowRegion,
                        centerX - demonMarkerRadius,
                        centerY - demonMarkerRadius,
                        demonMarkerDiameter,
                        demonMarkerDiameter);
            } else if (dayNightOverlayTex != null) {
                batch.draw(
                        dayNightOverlayTex,
                        centerX - demonMarkerRadius,
                        centerY - demonMarkerRadius,
                        demonMarkerDiameter,
                        demonMarkerDiameter);
            }

            TextureRegion lineRegion = shadowRegion != null ? shadowRegion : playerRegion;
            if (lineRegion != null) {
                float dx = playerCenterX - centerX;
                float dy = playerCenterY - centerY;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);
                if (distance > 0.001f) {
                    float angleDeg = (float) Math.toDegrees(Math.atan2(dy, dx));
                    batch.setColor(1f, 0.24f, 0.2f, BOSS_ARENA_PHASE_2_DETECTION_DEBUG_LINE_ALPHA);
                    batch.draw(
                            lineRegion,
                            centerX,
                            centerY - lineThickness * 0.5f,
                            0f,
                            lineThickness * 0.5f,
                            distance,
                            lineThickness,
                            1f,
                            1f,
                            angleDeg);
                }
            }
        }

        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void drawPlayerAttackHitAreaPreview() {
        if (!player.attacking || activeAttackAnim == null) {
            return;
        }
        if (dayNightOverlayTex == null && shadowRegion == null) {
            return;
        }
        batch.setShader(null);
        float hitRadius = playerBatHitRange();
        float diameter = hitRadius * 2f;

        if (isTeleportDashAnimation(activeAttackAnim)) {
            float startCenterX = dashAttackSweepStart.x + player.bounds.width * 0.5f;
            float startCenterY = dashAttackSweepStart.y + player.bounds.height * 0.5f;
            float endCenterX = dashAttackSweepEnd.x + player.bounds.width * 0.5f;
            float endCenterY = dashAttackSweepEnd.y + player.bounds.height * 0.5f;
            float segX = endCenterX - startCenterX;
            float segY = endCenterY - startCenterY;
            float segLength = (float) Math.sqrt(segX * segX + segY * segY);
            float angleDeg = (float) Math.toDegrees(Math.atan2(segY, segX));
            float midX = (startCenterX + endCenterX) * 0.5f;
            float midY = (startCenterY + endCenterY) * 0.5f;
            float drawX = midX - segLength * 0.5f;
            float drawY = midY - hitRadius;

            if (activeAttackAnim == dashAttackAnim) {
                batch.setColor(
                        PLAYER_ATTACK_DASH_E_R,
                        PLAYER_ATTACK_DASH_E_G,
                        PLAYER_ATTACK_DASH_E_B,
                        PLAYER_ATTACK_AREA_ALPHA);
            } else {
                batch.setColor(
                        PLAYER_ATTACK_DASH_RIGHT_R,
                        PLAYER_ATTACK_DASH_RIGHT_G,
                        PLAYER_ATTACK_DASH_RIGHT_B,
                        PLAYER_ATTACK_AREA_ALPHA);
            }
            if (playerRegion != null) {
                batch.draw(playerRegion,
                        drawX,
                        drawY,
                        segLength * 0.5f,
                        hitRadius,
                        segLength,
                        diameter,
                        1f,
                        1f,
                        angleDeg);
            }
            if (shadowRegion != null) {
                batch.draw(shadowRegion, startCenterX - hitRadius, startCenterY - hitRadius, diameter, diameter);
                batch.draw(shadowRegion, endCenterX - hitRadius, endCenterY - hitRadius, diameter, diameter);
            }
            batch.setColor(1f, 1f, 1f, 1f);
            return;
        }

        float playerCenterX = player.bounds.x + player.bounds.width * 0.5f;
        float playerCenterY = player.bounds.y + player.bounds.height * 0.5f;
        batch.setColor(
                PLAYER_ATTACK_MELEE_R,
                PLAYER_ATTACK_MELEE_G,
                PLAYER_ATTACK_MELEE_B,
                PLAYER_ATTACK_AREA_ALPHA);
        if (shadowRegion != null) {
            batch.draw(shadowRegion, playerCenterX - hitRadius, playerCenterY - hitRadius, diameter, diameter);
        } else if (dayNightOverlayTex != null) {
            batch.draw(dayNightOverlayTex, playerCenterX - hitRadius, playerCenterY - hitRadius, diameter, diameter);
        }
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void drawEnemyHitboxesDebug() {
        if (dayNightOverlayTex == null) {
            return;
        }
        batch.setShader(null);

        for (int i = 0; i < evilWizards.size; i++) {
            EvilWizardInstance wizard = evilWizards.get(i);
            if (wizard.state == EvilWizardAnimationState.DIE) {
                continue;
            }
            float halfWidth = evilWizardHitboxHalfWidth();
            float halfHeight = evilWizardHitboxHalfHeight();
            float centerX = evilWizardHitboxCenterX(wizard);
            float centerY = evilWizardHitboxCenterY(wizard);
            float left = centerX - halfWidth;
            float bottom = centerY - halfHeight;
            float width = halfWidth * 2f;
            float height = halfHeight * 2f;
            drawDebugHitboxRect(left, bottom, width, height, 1f, 0.12f, 0.12f);
        }

        for (int i = 0; i < golem1s.size; i++) {
            Golem1Instance golem = golem1s.get(i);
            float halfWidth = golem1HitboxHalfWidth();
            float halfHeight = golem1HitboxHalfHeight();
            float centerX = golem1HitboxCenterX(golem);
            float centerY = golem1HitboxCenterY(golem);
            float left = centerX - halfWidth;
            float bottom = centerY - halfHeight;
            float width = halfWidth * 2f;
            float height = halfHeight * 2f;
            drawDebugHitboxRect(left, bottom, width, height, 1f, 0.58f, 0.05f);
        }

        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void drawBossArenaPhase2HitboxDebug() {
        if (dayNightOverlayTex == null || bossArenaPhase2Demons.size == 0) {
            return;
        }
        batch.setShader(null);
        for (int i = 0; i < bossArenaPhase2Demons.size; i++) {
            BossArenaPhase2DemonInstance demon = bossArenaPhase2Demons.get(i);
            float halfWidth = bossArenaPhase2DemonHitboxHalfWidth();
            float halfHeight = bossArenaPhase2DemonHitboxHalfHeight();
            float centerX = bossArenaPhase2DemonHitboxCenterX(demon);
            float centerY = bossArenaPhase2DemonHitboxCenterY(demon);
            float left = centerX - halfWidth;
            float bottom = centerY - halfHeight;
            float width = halfWidth * 2f;
            float height = halfHeight * 2f;
            drawDebugHitboxRect(left, bottom, width, height, 0.95f, 0.16f, 0.9f);
        }
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void drawDebugHitboxRect(float x, float y, float width, float height, float r, float g, float b) {
        float outline = Math.max(1f, ENEMY_HITBOX_DEBUG_OUTLINE_THICKNESS);
        batch.setColor(r, g, b, ENEMY_HITBOX_DEBUG_FILL_ALPHA);
        batch.draw(dayNightOverlayTex, x, y, width, height);

        batch.setColor(r, g, b, ENEMY_HITBOX_DEBUG_OUTLINE_ALPHA);
        batch.draw(dayNightOverlayTex, x, y, width, outline);
        batch.draw(dayNightOverlayTex, x, y + height - outline, width, outline);
        batch.draw(dayNightOverlayTex, x, y, outline, height);
        batch.draw(dayNightOverlayTex, x + width - outline, y, outline, height);
    }

    private void drawSpawnedBat(float saturation) {
        if (bats.size == 0) {
            return;
        }

        if (saturationShader != null) {
            batch.setShader(saturationShader);
            saturationShader.setUniformf("u_saturation", saturation);
        }

        for (int i = 0; i < bats.size; i++) {
            BatInstance bat = bats.get(i);
            Animation<TextureRegion> activeBatAnimation = batSleepAnim;
            boolean loop = true;
            if (bat.state == BatAnimationState.WAKING_UP && batWakeUpAnim != null) {
                activeBatAnimation = batWakeUpAnim;
                loop = false;
            } else if (bat.state == BatAnimationState.IDLE_FLY && batIdleFlyAnim != null) {
                activeBatAnimation = batIdleFlyAnim;
            } else if (bat.state == BatAnimationState.CHASING && batRunAnim != null) {
                activeBatAnimation = batRunAnim;
            } else if (bat.state == BatAnimationState.DIE && batDieAnim != null) {
                activeBatAnimation = batDieAnim;
                loop = false;
            } else if (bat.state == BatAnimationState.HURT && batHurtAnim != null) {
                activeBatAnimation = batHurtAnim;
                loop = false;
            } else if (bat.state == BatAnimationState.ATTACKING) {
                Animation<TextureRegion> attackAnimation = activeBatAttackAnimation(bat);
                if (attackAnimation != null) {
                    activeBatAnimation = attackAnimation;
                    loop = false;
                }
            }
            if (activeBatAnimation == null) {
                continue;
            }

            TextureRegion frame = activeBatAnimation.getKeyFrame(bat.stateTime, loop);
            float drawW = frame.getRegionWidth() * BAT_SLEEP_DRAW_SCALE;
            float drawH = frame.getRegionHeight() * BAT_SLEEP_DRAW_SCALE;
            float drawX = bat.position.x - drawW * 0.5f;
            float drawY = bat.position.y;
            batch.draw(frame, drawX, drawY, drawW, drawH);
        }
        if (saturationShader != null) {
            batch.setShader(null);
        }

        if (dayNightOverlayTex != null) {
            for (int i = 0; i < bats.size; i++) {
                BatInstance bat = bats.get(i);
                if (bat.state == BatAnimationState.DIE) {
                    continue;
                }
                float hpRatio = bat.maxHealth > 0f ? bat.health / bat.maxHealth : 0f;
                hpRatio = clamp01(hpRatio);
                float hpX = bat.position.x - BAT_HP_BAR_WIDTH * 0.5f;
                float hpY = bat.position.y + BAT_FRAME_SIZE * BAT_SLEEP_DRAW_SCALE + BAT_HP_BAR_OFFSET_Y;

                batch.setColor(0f, 0f, 0f, 0.85f);
                batch.draw(dayNightOverlayTex, hpX - 0.5f, hpY - 0.5f,
                        BAT_HP_BAR_WIDTH + 1f, BAT_HP_BAR_HEIGHT + 1f);
                batch.setColor(0.15f, 0.15f, 0.15f, 1f);
                batch.draw(dayNightOverlayTex, hpX, hpY, BAT_HP_BAR_WIDTH, BAT_HP_BAR_HEIGHT);
                if (hpRatio > 0f) {
                    batch.setColor(0.2f, 0.95f, 0.2f, 1f);
                    batch.draw(dayNightOverlayTex, hpX, hpY, BAT_HP_BAR_WIDTH * hpRatio, BAT_HP_BAR_HEIGHT);
                }
            }
            batch.setColor(1f, 1f, 1f, 1f);
        }
    }

    private void drawEvilWizards(float saturation, boolean drawBehindPlayer) {
        if (evilWizards.size == 0
                || (evilWizardIdleAnim == null
                && evilWizardRunAnim == null
                && evilWizardAttack1Anim == null
                && evilWizardAttack2Anim == null
                && evilWizardDieAnim == null)) {
            return;
        }
        float playerDepthBasisY = player.bounds.y;
        if (shadowRegion != null) {
            float shadowW = EVIL_WIZARD_SHADOW_WIDTH_TILES * DEFAULT_TILE_SIZE;
            float shadowH = EVIL_WIZARD_SHADOW_HEIGHT_TILES * DEFAULT_TILE_SIZE;
            batch.setShader(null);
            batch.setColor(1f, 1f, 1f, SHADOW_ALPHA);
            for (int i = 0; i < evilWizards.size; i++) {
                EvilWizardInstance wizard = evilWizards.get(i);
                if (wizard.state == EvilWizardAnimationState.DIE) {
                    continue;
                }
                float wizardDepthBasisY = wizard.position.y
                        - wizard.depthBasisOffsetTiles * DEFAULT_TILE_SIZE;
                boolean wizardBehindPlayer = wizardDepthBasisY > playerDepthBasisY;
                if (wizardBehindPlayer != drawBehindPlayer) {
                    continue;
                }
                float shadowDownOffset = wizard.shadowDownOffsetTiles * DEFAULT_TILE_SIZE;
                float shadowX = wizard.position.x - shadowW * 0.5f;
                float shadowY = wizardDepthBasisY - shadowH * 0.25f - shadowDownOffset;
                batch.draw(shadowRegion, shadowX, shadowY, shadowW, shadowH);
            }
            batch.setColor(1f, 1f, 1f, 1f);
        }
        if (saturationShader != null) {
            batch.setShader(saturationShader);
            saturationShader.setUniformf("u_saturation", saturation);
        }
        for (int i = 0; i < evilWizards.size; i++) {
            EvilWizardInstance wizard = evilWizards.get(i);
            float wizardDepthBasisY = wizard.position.y
                    - wizard.depthBasisOffsetTiles * DEFAULT_TILE_SIZE;
            boolean wizardBehindPlayer = wizardDepthBasisY > playerDepthBasisY;
            if (wizardBehindPlayer != drawBehindPlayer) {
                continue;
            }
            Animation<TextureRegion> activeAnimation = evilWizardIdleAnim;
            boolean loop = true;
            if (wizard.state == EvilWizardAnimationState.ATTACKING) {
                Animation<TextureRegion> attackAnimation = activeEvilWizardAttackAnimation(wizard);
                if (attackAnimation != null) {
                    activeAnimation = attackAnimation;
                    loop = false;
                }
            } else if (wizard.state == EvilWizardAnimationState.HURT && evilWizardHurtAnim != null) {
                activeAnimation = evilWizardHurtAnim;
                loop = false;
            } else if (wizard.state == EvilWizardAnimationState.DIE && evilWizardDieAnim != null) {
                activeAnimation = evilWizardDieAnim;
                loop = false;
            } else if (wizard.state == EvilWizardAnimationState.RUN && evilWizardRunAnim != null) {
                activeAnimation = evilWizardRunAnim;
            }
            if (activeAnimation == null) {
                activeAnimation = evilWizardRunAnim;
            }
            if (activeAnimation == null) {
                activeAnimation = activeEvilWizardAttackAnimation(wizard);
                loop = false;
            }
            if (activeAnimation == null) {
                activeAnimation = evilWizardDieAnim;
                loop = false;
            }
            if (activeAnimation == null) {
                continue;
            }
            TextureRegion frame = activeAnimation.getKeyFrame(wizard.stateTime, loop);
            float drawW = frame.getRegionWidth() * EVIL_WIZARD_DRAW_SCALE;
            float drawH = frame.getRegionHeight() * EVIL_WIZARD_DRAW_SCALE;
            float drawX = wizard.position.x - drawW * 0.5f;
            float drawY = wizard.position.y - drawH * 0.5f;
            float originX = drawW * 0.5f;
            float originY = drawH * 0.5f;
            float scaleX = wizard.facingLeft ? -1f : 1f;
            batch.draw(frame, drawX, drawY, originX, originY, drawW, drawH, scaleX, 1f, 0f);
        }
        if (saturationShader != null) {
            batch.setShader(null);
        }

        if (dayNightOverlayTex != null) {
            for (int i = 0; i < evilWizards.size; i++) {
                EvilWizardInstance wizard = evilWizards.get(i);
                float wizardDepthBasisY = wizard.position.y
                        - wizard.depthBasisOffsetTiles * DEFAULT_TILE_SIZE;
                boolean wizardBehindPlayer = wizardDepthBasisY > playerDepthBasisY;
                if (wizardBehindPlayer != drawBehindPlayer) {
                    continue;
                }
                float hpRatio = wizard.maxHealth > 0f ? wizard.health / wizard.maxHealth : 0f;
                hpRatio = clamp01(hpRatio);
                float frameSize = evilWizardFrameSize() * EVIL_WIZARD_DRAW_SCALE;
                float hpX = wizard.position.x - BAT_HP_BAR_WIDTH * 0.5f;
                float hpY = wizard.position.y
                        + frameSize * 0.5f
                        + BAT_HP_BAR_OFFSET_Y
                        - EVIL_WIZARD_HP_BAR_DOWN_OFFSET_TILES * DEFAULT_TILE_SIZE;

                batch.setColor(0f, 0f, 0f, 0.85f);
                batch.draw(dayNightOverlayTex, hpX - 0.5f, hpY - 0.5f,
                        BAT_HP_BAR_WIDTH + 1f, BAT_HP_BAR_HEIGHT + 1f);
                batch.setColor(0.15f, 0.15f, 0.15f, 1f);
                batch.draw(dayNightOverlayTex, hpX, hpY, BAT_HP_BAR_WIDTH, BAT_HP_BAR_HEIGHT);
                if (hpRatio > 0f) {
                    batch.setColor(0.2f, 0.95f, 0.2f, 1f);
                    batch.draw(dayNightOverlayTex, hpX, hpY, BAT_HP_BAR_WIDTH * hpRatio, BAT_HP_BAR_HEIGHT);
                }
            }
            batch.setColor(1f, 1f, 1f, 1f);
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






