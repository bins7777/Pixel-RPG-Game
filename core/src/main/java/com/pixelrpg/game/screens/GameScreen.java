package com.pixelrpg.game.screens;

import java.io.File;
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
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.ShortArray;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.pixelrpg.game.PixelRpgGame;
import com.pixelrpg.game.config.MapCatalog;
import com.pixelrpg.game.config.MapDefinition;
import com.pixelrpg.game.entities.PlayerLevelStats;
import com.pixelrpg.game.entities.PlayerState;
import com.pixelrpg.game.helpers.ShaderFactory;
import com.pixelrpg.game.managers.DayNightCycleManager;
import com.pixelrpg.game.save.SaveGameData;
import com.pixelrpg.game.save.SaveGameService;
import com.pixelrpg.game.settings.GameSettingsService;
import com.pixelrpg.game.ui.HudRenderer;
import com.pixelrpg.game.world.WorldState;

/**
 * Minimal LibGDX game that loads the provided TMX world.map,
 * renders layers, and lets a player move with wall collisions.
 */
public class GameScreen extends ScreenAdapter {
    private static final String MAP_CATALOG_PATH = "config/maps.json";
    private static final String DEFAULT_CHARACTER_NAME = "Character";
    private static final float AUTO_SAVE_INTERVAL_SECONDS = 10f;
    private static final float SCREEN_FADE_DURATION_SECONDS = 0.7f;
    private static final String PROJECT_ROOT_ABSOLUTE_PATH = resolveProjectRootAbsolutePath();
    private static final String MAP_ID_VILLAGE = "village";
    private static final String MAP_ID_RESEARCH_LAB_FLOOR_1 = "research-lab-floor-1";
    private static final String MAP_ID_RESEARCH_LAB_FLOOR_2 = "research-lab-floor-2";
    private static final String MAP_ID_INN = "inn";
    private static final String MAP_ID_INN_ROOM = "inn-room";
    private static final String MAP_ID_DEMON_CASTLE = "demon-castle";
    private static final String MAP_ID_BOSS_ARENA_PHASE_2 = "boss-arena-phase-2";
    private static final int FIRST_QUEST_DISABLED = 0;
    private static final int FIRST_QUEST_TALK_TO_FATHER = 1;
    private static final int FIRST_QUEST_DELIVER_TO_CAFE = 2;
    private static final int FIRST_QUEST_COMPLETE = 3;
    private static final int SECOND_QUEST_DISABLED = 0;
    private static final int SECOND_QUEST_TALK_TO_CAFE_OWNER = 1;
    private static final int SECOND_QUEST_TAKE_FRAGMENT_TO_SCIENTIST = 2;
    private static final int SECOND_QUEST_RETRIEVE_ARCHIVE_NOTES = 3;
    private static final int SECOND_QUEST_RETURN_TO_SCIENTIST = 4;
    private static final int SECOND_QUEST_COMPLETE = 5;
    private static final int THIRD_QUEST_DISABLED = 0;
    private static final int THIRD_QUEST_RETURN_TO_FATHER = 1;
    private static final int THIRD_QUEST_MEET_FATHER_OUTSIDE = 2;
    private static final int THIRD_QUEST_MOVEMENT_TUTORIAL = 3;
    private static final int THIRD_QUEST_ATTACK_TUTORIAL = 4;
    private static final int THIRD_QUEST_COMBAT_TUTORIAL = 5;
    private static final int THIRD_QUEST_SPAR_WITH_FATHER = 6;
    private static final int THIRD_QUEST_COMPLETE = 7;
    private static final int FOURTH_QUEST_DISABLED = 0;
    private static final int FOURTH_QUEST_REPORT_TO_SCIENTIST = 1;
    private static final int FOURTH_QUEST_TALK_TO_CAFE_OWNER = 2;
    private static final int FOURTH_QUEST_EXAMINE_CAFE_MARK = 3;
    private static final int FOURTH_QUEST_CAFE_BATTLE = 4;
    private static final int FOURTH_QUEST_EXAMINE_CENTER_MARK = 5;
    private static final int FOURTH_QUEST_CENTER_BATTLE = 6;
    private static final int FOURTH_QUEST_EXAMINE_EDGE_MARK = 7;
    private static final int FOURTH_QUEST_EDGE_BATTLE = 8;
    private static final int FOURTH_QUEST_RETURN_TO_SCIENTIST = 9;
    private static final int FOURTH_QUEST_GO_TO_ARRAY = 10;
    private static final int FOURTH_QUEST_ACTIVATE_ARRAY_NODE_1 = 11;
    private static final int FOURTH_QUEST_ACTIVATE_ARRAY_NODE_2 = 12;
    private static final int FOURTH_QUEST_ACTIVATE_ARRAY_NODE_3 = 13;
    private static final int FOURTH_QUEST_ACTIVATE_ARRAY_CONSOLE = 14;
    private static final int FOURTH_QUEST_ARRAY_DEFENSE = 15;
    private static final int FOURTH_QUEST_GO_TO_BREACH = 16;
    private static final int FOURTH_QUEST_EXAMINE_BREACH = 17;
    private static final int FOURTH_QUEST_BREACH_BATTLE = 18;
    private static final int FOURTH_QUEST_RETURN_RIFT_CORE = 19;
    private static final int FOURTH_QUEST_EPILOGUE = 20;
    private static final int FOURTH_QUEST_COMPLETE = 21;
    private static final int FIFTH_QUEST_DISABLED = 0;
    private static final int FIFTH_QUEST_GO_TO_ABANDONED_VILLAGE = 1;
    private static final int FIFTH_QUEST_OUTER_RUINS_BATTLE = 2;
    private static final int FIFTH_QUEST_ADVANCE_TO_PORTAL = 3;
    private static final int FIFTH_QUEST_MID_RUINS_BATTLE = 4;
    private static final int FIFTH_QUEST_ANCHOR_1_BATTLE = 5;
    private static final int FIFTH_QUEST_ANCHOR_1_DESTROY = 6;
    private static final int FIFTH_QUEST_ANCHOR_2_BATTLE = 7;
    private static final int FIFTH_QUEST_ANCHOR_2_DESTROY = 8;
    private static final int FIFTH_QUEST_ANCHOR_3_BATTLE = 9;
    private static final int FIFTH_QUEST_ANCHOR_3_DESTROY = 10;
    private static final int FIFTH_QUEST_PORTAL_BOSS = 11;
    private static final int FIFTH_QUEST_COMPLETE = 12;
    private static final int SIXTH_QUEST_DISABLED = 0;
    private static final int SIXTH_QUEST_ENTER_PORTAL = 1;
    private static final int SIXTH_QUEST_OUTER_ASSAULT = 2;
    private static final int SIXTH_QUEST_SEAL_A_BATTLE = 3;
    private static final int SIXTH_QUEST_SEAL_A_DESTROY = 4;
    private static final int SIXTH_QUEST_SEAL_B_BATTLE = 5;
    private static final int SIXTH_QUEST_SEAL_B_DESTROY = 6;
    private static final int SIXTH_QUEST_THRONE_GUARDS = 7;
    private static final int SIXTH_QUEST_BOSS_PHASE_1 = 8;
    private static final int SIXTH_QUEST_FINAL_PHASE_INTRO = 9;
    private static final int SIXTH_QUEST_FINAL_PHASE_BATTLE = 10;
    private static final int SIXTH_QUEST_ENDING = 11;
    private static final int SIXTH_QUEST_COMPLETE = 12;
    private static final String FIRST_QUEST_FATHER_LAYER_NAME = "NPC-Father";
    private static final String FIRST_QUEST_CAFE_ENTRANCE_LAYER_NAME = "ENTRANCE-Cafe";
    private static final String FIRST_QUEST_NOTIFICATION_TITLE = "First Quest";
    private static final String SECOND_QUEST_NOTIFICATION_TITLE = "Ashes That Remain";
    private static final String THIRD_QUEST_NOTIFICATION_TITLE = "Lessons of Survival";
    private static final String FOURTH_QUEST_NOTIFICATION_TITLE = "The Mark Returns";
    private static final String FIFTH_QUEST_NOTIFICATION_TITLE = "Embers at the Gate";
    private static final String SIXTH_QUEST_NOTIFICATION_TITLE = "The Last Ember";
    private static final String NEXT_QUEST_NOTIFICATION_TITLE = "Next Quest";
    private static final String THIRD_QUEST_TRAINING_FATHER_LAYER_NAME = "NPC-QUEST-Warrior";
    private static final String THIRD_QUEST_TRAINING_AREA_LAYER_NAME = "Father-Training";
    private static final String THIRD_QUEST_LAB_EXIT_LAYER_NAME = "EXIT-Research-Lab";
    private static final String THIRD_QUEST_INN_ENTRANCE_LAYER_NAME = "ENTRANCE-Inn";
    private static final String THIRD_QUEST_INN_ROOM_ENTRANCE_LAYER_NAME = "ENTRANCE-Inn-Room";
    private static final String FIRST_QUEST_PANEL_TEXTURE_PATH = "ui/menu/Big_Panel.png";
    private static final String FIRST_QUEST_FATHER_DIALOGUE_BOX_TEXTURE_PATH = "ui/dialogue/Dialogue-Box.png";
    private static final String FIRST_QUEST_ITEM_TEXTURE_PATH = "ui/quests/Herb-and-Spice.png";
    private static final String SECOND_QUEST_FRAGMENT_TEXTURE_PATH = "ui/quests/Scorched-Fragment.png";
    private static final String FOURTH_QUEST_CONDENSED_CORE_TEXTURE_PATH = "ui/quests/Condensed-Core.png";
    private static final String THIRD_QUEST_FATHER_TEXTURE_PATH =
            "maps/NPC/GandalfHardcore FREE Warrior/GandalfHardcore Warrior.png";
    private static final String FIRST_QUEST_CAFE_NPC_IDLE_DIRECTORY =
            PROJECT_ROOT_ABSOLUTE_PATH + File.separator + "DSA GAME" + File.separator + "NPC"
                    + File.separator + "Idle1";
    private static final String QUEST_AUDIO_DIRECTORY_ABSOLUTE_PATH =
            PROJECT_ROOT_ABSOLUTE_PATH + File.separator + "DSA GAME" + File.separator + "GAME SFX";
    private static final String NPC_SOUNDS_DIRECTORY_ABSOLUTE_PATH =
            PROJECT_ROOT_ABSOLUTE_PATH + File.separator + "NPC-Sounds";
    private static final String VILLAGE_MUSIC_ABSOLUTE_PATH =
            QUEST_AUDIO_DIRECTORY_ABSOLUTE_PATH + File.separator + "Village-Music.mp3";
    private static final String SAD_VILLAGE_MUSIC_ABSOLUTE_PATH =
            QUEST_AUDIO_DIRECTORY_ABSOLUTE_PATH + File.separator + "Sad-Village-Music.mp3";
    private static final String VILLAGE_COMBAT_MUSIC_ABSOLUTE_PATH =
            QUEST_AUDIO_DIRECTORY_ABSOLUTE_PATH + File.separator + "Combat-music.mp3";
    private static final String CASTLE_MUSIC_ABSOLUTE_PATH =
            QUEST_AUDIO_DIRECTORY_ABSOLUTE_PATH + File.separator + "Castle-Music.mp3";
    private static final String DEMON_BATTLE_MUSIC_ABSOLUTE_PATH =
            QUEST_AUDIO_DIRECTORY_ABSOLUTE_PATH + File.separator + "Demon-Music.mp3";
    private static final String INDOOR_FOOTSTEP_LOOP_SFX_PATH = "sounds/Footstep sound.mp3";
    private static final String TYPEWRITER_LOOP_SFX_PATH = "sounds/Typewriter.mp3";
    private static final String FIFTH_QUEST_VILLAGER_SCREAM_SFX_PATH = "sounds/Villager-Scream.mp3";
    private static final String DEMON_FIRST_INTERACTION_SFX_PATH = "sounds/Demon-Neutral1.mp3";
    private static final String DEMON_NEUTRAL_2_SFX_PATH = "sounds/Demon-Neutral2.mp3";
    private static final String GUI_FONT_PATH = "fonts/Berry Rotunda.ttf";
    private static final int FIRST_QUEST_TITLE_FONT_SIZE = 22;
    private static final int FIRST_QUEST_BODY_FONT_SIZE = 16;
    private static final int FIRST_QUEST_HINT_FONT_SIZE = 13;
    private static final float FIRST_QUEST_INTERACT_RANGE = 48f;
    private static final float FIRST_QUEST_CAFE_NPC_FRAME_DURATION = 0.18f;
    private static final float FIRST_QUEST_CAFE_NPC_DRAW_SCALE = 0.55f;
    private static final float FIRST_QUEST_CAFE_NPC_Y_OFFSET = -14f;
    private static final float FIRST_QUEST_CAFE_NPC_INTERACT_RANGE = 42f;
    private static final float FIRST_QUEST_MARKER_BOB_SPEED = 3.2f;
    private static final float FIRST_QUEST_MARKER_BOB_AMPLITUDE = 6f;
    private static final float FIRST_QUEST_OVERLAY_DIM_ALPHA = 0.72f;
    private static final float FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS = 5f;
    private static final float FIRST_QUEST_DIALOGUE_TYPEWRITER_CHARACTERS_PER_SECOND = 36f;
    private static final String QUEST_SEAL_MARKER_TEXT = "{seal}";
    private static final float QUEST_ARROW_MARKER_SCALE = 1.5f;
    private static final float QUEST_ARROW_MARKER_OUTLINE_OFFSET = 2f;
    private static final float QUEST_SEAL_MARKER_SIZE = 30f;
    private static final float QUEST_SEAL_MARKER_CORE_SIZE = 13f;
    private static final float QUEST_SEAL_MARKER_RUNE_LENGTH = 12f;
    private static final float QUEST_SEAL_MARKER_RUNE_THICKNESS = 2.4f;
    private static final float QUEST_SEAL_MARKER_ORBIT_OFFSET = 10f;
    private static final float QUEST_SEAL_MARKER_ORBIT_SIZE = 3.2f;
    private static final float VILLAGE_MUSIC_BASE_VOLUME = 1f;
    private static final float VILLAGE_MUSIC_FADE_IN_SECONDS = 5f;
    private static final float VILLAGE_MUSIC_FADE_OUT_TO_COMBAT_SECONDS = 1f;
    private static final float VILLAGE_MUSIC_RETURN_FROM_COMBAT_FADE_IN_SECONDS = 1f;
    private static final float VILLAGE_COMBAT_MUSIC_BASE_VOLUME = 1f;
    private static final float CASTLE_MUSIC_BASE_VOLUME = 1f;
    private static final float DEMON_BATTLE_MUSIC_BASE_VOLUME = 1f;
    private static final float INDOOR_FOOTSTEP_LOOP_BASE_VOLUME = 1f;
    private static final float TYPEWRITER_LOOP_SFX_BASE_VOLUME = 1f;
    private static final float TYPEWRITER_LOOP_SFX_FADE_OUT_SECONDS = 0.3f;
    private static final float NPC_DIALOGUE_SFX_BASE_VOLUME = 1f;
    private static final float BAKERY_NPC_DIALOGUE_SFX_VOLUME_MULTIPLIER = 1.4f;
    private static final float SCIENTIST_NPC_DIALOGUE_SFX_VOLUME_MULTIPLIER = 2.6f;
    private static final float SCIENTIST_NEUTRAL_2_NPC_DIALOGUE_SFX_VOLUME_MULTIPLIER = 1.3f;
    private static final String SIXTH_QUEST_ENDING_MUSIC_ASSET_PATH = "music/ending/Ending-Waltz.mp3";
    private static final float SIXTH_QUEST_ENDING_MUSIC_BASE_VOLUME = 1f;
    private static final float SIXTH_QUEST_ENDING_MUSIC_FADE_OUT_SECONDS = 3f;
    private static final float SIXTH_QUEST_ENDING_MUSIC_FADE_IN_SECONDS = 3f;
    private static final float SIXTH_QUEST_ENDING_VIDEO_DELAY_SECONDS = 4f;
    private static final float SIXTH_QUEST_ENDING_VIDEO_FADE_SECONDS = 4f;
    private static final float BOSS_ARENA_FINAL_INTRO_AUTO_WALK_SECONDS = 1.5f;
    private static final float BOSS_ARENA_FINAL_INTRO_FOCUS_SECONDS = 5f;
    private static final float BOSS_ARENA_FINAL_INTRO_ZOOM_MULTIPLIER = 0.65f;
    private static final float BOSS_ARENA_FINAL_INTRO_CAMERA_Y_OFFSET_TILES = 4f;
    private static final float BOSS_ARENA_HUD_FADE_IN_SECONDS = 2f;
    private static final float DIALOGUE_CAMERA_ZOOM_MULTIPLIER = 0.05f;
    private static final float DIALOGUE_CAMERA_SMOOTH_SPEED = 8f;
    private static final float DIALOGUE_FOREGROUND_SCREEN_PADDING_X = 34f;
    private static final float DIALOGUE_FOREGROUND_SCREEN_PADDING_Y = 42f;
    private static final float DIALOGUE_FOREGROUND_FALLBACK_TARGET_WIDTH = 64f;
    private static final float DIALOGUE_FOREGROUND_FALLBACK_TARGET_HEIGHT = 80f;
    private static final float FIRST_QUEST_OVERLAY_HEADER_SCALE = 0.6f;
    private static final float FIRST_QUEST_DIALOGUE_HEADER_OFFSET_Y = -20f;
    private static final float FIRST_QUEST_FATHER_DIALOGUE_BOX_WIDTH_RATIO = 0.9f;
    private static final float FIRST_QUEST_FATHER_DIALOGUE_BOX_MAX_WIDTH = 1120f;
    private static final float FIRST_QUEST_FATHER_DIALOGUE_BOX_BOTTOM_MARGIN = 28f;
    private static final float FIRST_QUEST_FATHER_DIALOGUE_BOX_SPEAKER_CENTER_X_RATIO = 0.305f;
    private static final float FIRST_QUEST_FATHER_DIALOGUE_BOX_SPEAKER_BASELINE_FROM_TOP = 64f;
    private static final float FIRST_QUEST_FATHER_DIALOGUE_BOX_SPEAKER_SCALE_MULTIPLIER = 2f;
    private static final float FIRST_QUEST_FATHER_DIALOGUE_BOX_SPEAKER_OFFSET_Y = -10f;
    private static final float FIRST_QUEST_FATHER_DIALOGUE_BOX_TEXT_PADDING_LEFT = 92f;
    private static final float FIRST_QUEST_FATHER_DIALOGUE_BOX_TEXT_PADDING_RIGHT = 170f;
    private static final float FIRST_QUEST_FATHER_DIALOGUE_BOX_TEXT_TOP_FROM_TOP = 154f;
    private static final float FIRST_QUEST_FATHER_DIALOGUE_BOX_TEXT_SCALE_MULTIPLIER = 1.15f;
    private static final float FIRST_QUEST_FATHER_DIALOGUE_BOX_TEXT_OFFSET_X = 20f;
    private static final float FIRST_QUEST_FATHER_DIALOGUE_BOX_TEXT_OFFSET_Y = -40f;
    private static final float FIRST_QUEST_FATHER_DIALOGUE_BOX_HINT_RIGHT_PADDING = 186f;
    private static final float FIRST_QUEST_FATHER_DIALOGUE_BOX_HINT_BOTTOM_PADDING = 66f;
    private static final float FIRST_QUEST_FATHER_DIALOGUE_BOX_HINT_OFFSET_X = -35f;
    private static final float FIRST_QUEST_FATHER_DIALOGUE_BOX_HINT_OFFSET_Y = 15f;
    private static final float SIXTH_QUEST_PORTAL_CHOICE_BOX_WIDTH_RATIO = 0.9f;
    private static final float SIXTH_QUEST_PORTAL_CHOICE_BOX_MAX_WIDTH = 1120f;
    private static final float SIXTH_QUEST_PORTAL_CHOICE_BOX_BOTTOM_MARGIN = 28f;
    private static final float SIXTH_QUEST_PORTAL_CHOICE_TITLE_CENTER_X_RATIO = 0.305f;
    private static final float SIXTH_QUEST_PORTAL_CHOICE_TITLE_BASELINE_FROM_TOP = 64f;
    private static final float SIXTH_QUEST_PORTAL_CHOICE_TITLE_OFFSET_Y = -4f;
    private static final float SIXTH_QUEST_PORTAL_CHOICE_TEXT_PADDING_LEFT = 92f;
    private static final float SIXTH_QUEST_PORTAL_CHOICE_TEXT_PADDING_RIGHT = 170f;
    private static final float SIXTH_QUEST_PORTAL_CHOICE_TEXT_TOP_FROM_TOP = 154f;
    private static final float SIXTH_QUEST_PORTAL_CHOICE_TEXT_OFFSET_X = 20f;
    private static final float SIXTH_QUEST_PORTAL_CHOICE_TEXT_OFFSET_Y = -18f;
    private static final float SIXTH_QUEST_PORTAL_CHOICE_BUTTON_WIDTH = 156f;
    private static final float SIXTH_QUEST_PORTAL_CHOICE_BUTTON_HEIGHT = 42f;
    private static final float SIXTH_QUEST_PORTAL_CHOICE_BUTTON_GAP = 36f;
    private static final float SIXTH_QUEST_PORTAL_CHOICE_BUTTON_BOTTOM_PADDING = 62f;
    private static final float SIXTH_QUEST_PORTAL_CHOICE_HINT_RIGHT_PADDING = 160f;
    private static final float SIXTH_QUEST_PORTAL_CHOICE_HINT_BOTTOM_PADDING = 34f;
    private static final float SIXTH_QUEST_PORTAL_CHOICE_TITLE_EXTRA_OFFSET_Y = -8f;
    private static final float SIXTH_QUEST_PORTAL_CHOICE_TEXT_EXTRA_OFFSET_Y = -15f;
    private static final float SIXTH_QUEST_PORTAL_CHOICE_BUTTON_EXTRA_OFFSET_Y = 30f;
    private static final float SIXTH_QUEST_PORTAL_CHOICE_HINT_EXTRA_OFFSET_X = -120f;
    private static final float SIXTH_QUEST_PORTAL_CHOICE_HINT_EXTRA_OFFSET_Y = 40f;
    private static final float SIXTH_QUEST_PORTAL_CHOICE_BUTTON_TEXT_SCALE = 0.65f;
    private static final float FIRST_QUEST_ITEM_CARD_HEADER_OFFSET_Y = -10f;
    private static final float SECOND_QUEST_FRAGMENT_CARD_IMAGE_OFFSET_Y = -24f;
    private static final float FOURTH_QUEST_CONDENSED_CORE_CARD_IMAGE_OFFSET_Y = -89f;
    private static final float TECHNIQUE_LOCKED_NOTIFICATION_SECONDS = 2.8f;
    private static final int QUEST_LEVEL_CAP_DEFAULT = PlayerLevelStats.MIN_LEVEL;
    private static final int QUEST_2_LEVEL_CAP = 2;
    private static final int QUEST_3_LEVEL_CAP = 3;
    private static final int QUEST_4_LEVEL_CAP = 4;
    private static final int DASH_ATTACK_UNLOCK_LEVEL = 3;
    private static final int SPATIAL_DASH_UNLOCK_LEVEL = 4;
    private static final String SECOND_QUEST_ARCHIVE_LAYER_NAME = "Table 1";
    private static final String FOURTH_QUEST_LAB_ENTRANCE_LAYER_NAME = "ENTRANCE-Research-Lab";
    private static final String FOURTH_QUEST_LAB_FLOOR_2_LAYER_NAME = "ENTRANCE-Research-Lab-Floor-2";
    private static final String FOURTH_QUEST_LAB_FLOOR_2_EXIT_LAYER_NAME = "EXIT-Research-Lab-Floor-2";
    private static final String FOURTH_QUEST_VILLAGE_EDGE_LAYER_NAME = "ENTRANCE-Dungeon";
    private static final String FOURTH_QUEST_AREA_1_LAYER_NAME = "TMR-Area1";
    private static final String FOURTH_QUEST_AREA_2_LAYER_NAME = "TMR-Area2";
    private static final String FOURTH_QUEST_AREA_3_LAYER_NAME = "TMR-Area3";
    private static final String FIFTH_QUEST_PORTAL_LAYER_NAME = "ENTRANCE-Dungeon";
    private static final String FIFTH_QUEST_AREA_GROUP_LAYER_NAME = "Embers at the Gate";
    private static final String FIFTH_QUEST_AREA_LAYER_NAME = "EATG";
    private static final String FIFTH_QUEST_CAMERA_GROUP_LAYER_NAME = "CAMERA-EATG";
    private static final String FIFTH_QUEST_CAMERA_TRIGGER_LAYER_NAME = "Trigger";
    private static final String FIFTH_QUEST_CAMERA_1_LAYER_NAME = "Camera1";
    private static final String FIFTH_QUEST_CAMERA_2_LAYER_NAME = "Camera2";
    private static final String FIFTH_QUEST_CAMERA_3_LAYER_NAME = "Camera3";
    private static final String VILLAGE_PORTAL_GROUP_NAME = "PORTAL";
    private static final String FIFTH_QUEST_FIGHTING_AREA_LAYER_NAME = "FIGHTING AREA";
    private static final String VILLAGE_PORTAL_ACTIVE_LAYER_NAME = "PORTAL-Active";
    private static final String VILLAGE_PORTAL_INACTIVE_LAYER_NAME = "PORTAL-Inactive";
    private static final String VILLAGE_PORTAL_ACTUAL_LAYER_NAME = "Actual-Portal";
    private static final float VILLAGE_PORTAL_QUEST_MARKER_Y_OFFSET = -104f;
    private static final int FOURTH_QUEST_ENCOUNTER_NONE = 0;
    private static final int FOURTH_QUEST_ENCOUNTER_CAFE = 1;
    private static final int FOURTH_QUEST_ENCOUNTER_CENTER = 2;
    private static final int FOURTH_QUEST_ENCOUNTER_EDGE = 3;
    private static final int FOURTH_QUEST_ENCOUNTER_ARRAY = 4;
    private static final int FOURTH_QUEST_ENCOUNTER_BREACH = 5;
    private static final int FIFTH_QUEST_ENCOUNTER_NONE = 0;
    private static final int FIFTH_QUEST_ENCOUNTER_OUTER = 1;
    private static final int FIFTH_QUEST_ENCOUNTER_MID = 2;
    private static final int FIFTH_QUEST_ENCOUNTER_ANCHOR_1 = 3;
    private static final int FIFTH_QUEST_ENCOUNTER_ANCHOR_2 = 4;
    private static final int FIFTH_QUEST_ENCOUNTER_ANCHOR_3 = 5;
    private static final int FIFTH_QUEST_ENCOUNTER_COMMANDER = 6;
    private static final int SIXTH_QUEST_ENCOUNTER_OUTER = 101;
    private static final int SIXTH_QUEST_ENCOUNTER_SEAL_A = 102;
    private static final int SIXTH_QUEST_ENCOUNTER_SEAL_B = 103;
    private static final int SIXTH_QUEST_ENCOUNTER_THRONE_GUARDS = 104;
    private static final int SIXTH_QUEST_ENCOUNTER_BOSS_PHASE_1 = 105;
    private static final int SIXTH_QUEST_ENCOUNTER_BOSS_PHASE_2 = 106;
    private static final float FOURTH_QUEST_SITE_WIDTH = 56f;
    private static final float FOURTH_QUEST_SITE_HEIGHT = 42f;
    private static final float FOURTH_QUEST_BREACH_SITE_SIZE = 96f;
    private static final float FOURTH_QUEST_CORRUPTED_BAT_HEALTH = 52f;
    private static final float FOURTH_QUEST_CORRUPTED_BAT_DAMAGE = 14f;
    private static final float FOURTH_QUEST_CORRUPTED_BAT_SPEED = 40f;
    private static final float FOURTH_QUEST_CENTER_ELITE_HEALTH = 96f;
    private static final float FOURTH_QUEST_CENTER_ELITE_DAMAGE = 18f;
    private static final float FOURTH_QUEST_EDGE_ELITE_HEALTH = 124f;
    private static final float FOURTH_QUEST_EDGE_ELITE_DAMAGE = 20f;
    private static final float FOURTH_QUEST_BREACH_BOSS_HEALTH = 260f;
    private static final float FOURTH_QUEST_BREACH_BOSS_DAMAGE = 24f;
    private static final float FOURTH_QUEST_BREACH_BOSS_SPEED = 50f;
    private static final float FOURTH_QUEST_ARRAY_DEFENSE_SECONDS = 16f;
    private static final float FOURTH_QUEST_BREACH_CALLOUT_SECONDS = 2.7f;
    private static final float FIFTH_QUEST_OUTER_RUINS_WIDTH = 164f;
    private static final float FIFTH_QUEST_OUTER_RUINS_HEIGHT = 108f;
    private static final float FIFTH_QUEST_MID_RUINS_WIDTH = 152f;
    private static final float FIFTH_QUEST_MID_RUINS_HEIGHT = 118f;
    private static final float FIFTH_QUEST_PORTAL_ZONE_WIDTH = 130f;
    private static final float FIFTH_QUEST_PORTAL_ZONE_HEIGHT = 118f;
    private static final float FIFTH_QUEST_ANCHOR_SIZE = 42f;
    private static final float FIFTH_QUEST_DEMON_1_HEALTH = 55f;
    private static final float FIFTH_QUEST_DEMON_1_DAMAGE = 16f;
    private static final float FIFTH_QUEST_DEMON_1_SPEED = 36f;
    private static final float FIFTH_QUEST_DEMON_2_HEALTH = 80f;
    private static final float FIFTH_QUEST_DEMON_2_DAMAGE = 22f;
    private static final float FIFTH_QUEST_DEMON_2_SPEED = 40f;
    private static final float FIFTH_QUEST_DEMON_3_HEALTH = 110f;
    private static final float FIFTH_QUEST_DEMON_3_DAMAGE = 28f;
    private static final float FIFTH_QUEST_DEMON_3_SPEED = 44f;
    private static final float FIFTH_QUEST_COMMANDER_HEALTH = 210f;
    private static final float FIFTH_QUEST_COMMANDER_DAMAGE = 34f;
    private static final float FIFTH_QUEST_COMMANDER_SPEED = 48f;
    private static final float FIFTH_QUEST_DEMON_SPEED_MULTIPLIER = 0.6f;
    private static final float FIFTH_QUEST_SPAWN_MIN_DISTANCE_FROM_PLAYER = 64f;
    private static final float FIFTH_QUEST_SPAWN_MIN_DISTANCE_BETWEEN_DEMONS = 28f;
    private static final float FIFTH_QUEST_COMMANDER_CALLOUT_SECONDS = 2.3f;
    private static final float FIFTH_QUEST_INTRO_CAMERA_SEGMENT_SECONDS = 3f;
    private static final float FIFTH_QUEST_FIRST_ENCOUNTER_INTRO_SECONDS = 2.2f;
    private static final float FIFTH_QUEST_FIRST_ENCOUNTER_INTRO_ZOOM_MULTIPLIER = 0.72f;
    private static final float FIFTH_QUEST_ZOMBIE_SFX_COOLDOWN_SECONDS = 0.45f;
    private static final float FIFTH_QUEST_ZOMBIE_INTRO_SFX_VOLUME_MULTIPLIER = 1.05f;
    private static final float FIFTH_QUEST_ZOMBIE_ATTACK_SFX_VOLUME_MULTIPLIER = 0.85f;
    private static final float SIXTH_QUEST_OUTER_ASSAULT_WIDTH = 156f;
    private static final float SIXTH_QUEST_OUTER_ASSAULT_HEIGHT = 110f;
    private static final float SIXTH_QUEST_SEAL_WIDTH = 118f;
    private static final float SIXTH_QUEST_SEAL_HEIGHT = 104f;
    private static final float SIXTH_QUEST_THRONE_WIDTH = 148f;
    private static final float SIXTH_QUEST_THRONE_HEIGHT = 116f;
    private static final float SIXTH_QUEST_BOSS_PHASE_1_HEALTH = 520f;
    private static final float SIXTH_QUEST_BOSS_PHASE_1_DAMAGE = 30f;
    private static final float SIXTH_QUEST_BOSS_PHASE_1_SPEED = 48f;
    private static final float SIXTH_QUEST_BOSS_PHASE_2_HEALTH = 780f;
    private static final float SIXTH_QUEST_BOSS_PHASE_2_DAMAGE = 36f;
    private static final float SIXTH_QUEST_BOSS_PHASE_2_SPEED = 54f;
    private static final float SIXTH_QUEST_BOSS_PHASE_TRANSITION_HEALTH_RATIO = 0.38f;
    private static final float SIXTH_QUEST_BOSS_CALLOUT_SECONDS = 2.2f;
    private static final float THIRD_QUEST_TRAINING_ZONE_HALF_WIDTH = 120f;
    private static final float THIRD_QUEST_TRAINING_ZONE_HALF_HEIGHT = 86f;
    private static final float THIRD_QUEST_TRAINING_ZONE_Y_OFFSET = 26f;
    private static final float SIXTH_QUEST_ENDING_FATHER_OFFSET_X_TILES = 1f;
    private static final float SIXTH_QUEST_ENDING_FATHER_OFFSET_Y_TILES = 1f;
    private static final int SIXTH_QUEST_ENDING_FATHER_TALKING_FRAME_INDEX = 2;
    private static final String SIXTH_QUEST_ENDING_SCIENTIST_TEXTURE_PATH =
            "maps/ORIGINAL ASSETS/NPC/Scientist 5/NPC_Scientist_5.png";
    private static final float SIXTH_QUEST_ENDING_SCIENTIST_OFFSET_X_TILES = -3.5f;
    private static final float SIXTH_QUEST_ENDING_SCIENTIST_OFFSET_Y_TILES = 0f;
    private static final float SIXTH_QUEST_ENDING_SCIENTIST_DRAW_SCALE = 0.75f;
    private static final float SIXTH_QUEST_ENDING_SCIENTIST_FRAME_DURATION = 0.12f;
    private static final float SIXTH_QUEST_ENDING_NPC_FADE_OUT_SECONDS = 3f;
    private static final float FIFTH_QUEST_PORTAL_DIALOGUE_NPC_FADE_IN_SECONDS = 1.5f;
    private static final float FIFTH_QUEST_PORTAL_DIALOGUE_SCIENTIST_WIDTH = 57.96f;
    private static final float FIFTH_QUEST_PORTAL_DIALOGUE_SCIENTIST_HEIGHT = 64f;
    private static final float FIFTH_QUEST_PORTAL_DIALOGUE_FATHER_CENTER_OFFSET_X = 56f;
    private static final float FIFTH_QUEST_PORTAL_DIALOGUE_FATHER_BOTTOM_OFFSET_Y = 0f;
    private static final float FIFTH_QUEST_PORTAL_DIALOGUE_SCIENTIST_CENTER_OFFSET_X = -56f;
    private static final float FIFTH_QUEST_PORTAL_DIALOGUE_SCIENTIST_BOTTOM_OFFSET_Y = 0f;
    private static final float THIRD_QUEST_TRAINING_MARKER_REACH_DISTANCE = 22f;
    private static final int THIRD_QUEST_DUMMY_HITS_REQUIRED = 5;
    private static final int THIRD_QUEST_TUTORIAL_BAT_COUNT = 3;
    private static final float THIRD_QUEST_TUTORIAL_BAT_HEALTH = 40f;
    private static final float THIRD_QUEST_TUTORIAL_BAT_DAMAGE = 10f;
    private static final float THIRD_QUEST_TUTORIAL_BAT_SPEED = 34f;
    private static final float THIRD_QUEST_SPAR_FATHER_MAX_HEALTH = 140f;
    private static final float THIRD_QUEST_SPAR_FATHER_DAMAGE = 14f;
    private static final float THIRD_QUEST_SPAR_FATHER_ATTACK_RANGE = 24f;
    private static final float THIRD_QUEST_SPAR_FATHER_MOVE_SPEED = 52f;
    private static final float THIRD_QUEST_SPAR_FATHER_ATTACK_COOLDOWN = 1.05f;
    private static final float THIRD_QUEST_SPAR_FATHER_HIT_COOLDOWN = 0.24f;
    private static final int THIRD_QUEST_SPAR_HITS_TO_WIN = 6;
    private static final float THIRD_QUEST_SPAR_SURVIVE_SECONDS = 12f;
    private static final float THIRD_QUEST_SPAR_CALL_OUT_SECONDS = 2.8f;
    private static final String[] FIRST_QUEST_FATHER_DIALOGUE_LINES = {
            "You're awake. I need your help before the inn fills up.",
            "Take this Herb-and-Spice bundle to the bakery for me.",
            "Leave this room, pass through the inn, and head out to the village.",
            "You'll find the bakery there. Keep the bundle safe until you arrive."
    };
    private static final String[] SECOND_QUEST_CAFE_OWNER_SPEAKERS = {
            "Bakery Owner",
            "Bakery Owner",
            "Bakery Owner",
            "Character",
            "Bakery Owner",
            "Character",
            "Character",
            "Bakery Owner",
            "Bakery Owner"
    };
    private static final String[] SECOND_QUEST_CAFE_OWNER_DIALOGUE_LINES = {
            "Thank you, dear. But... before you go, there is something I need to show you.",
            "I found this near the bakery door just before you arrived.",
            "At first I thought it was only soot, but it was still warm.",
            "...That smell.",
            "You know it?",
            "I do.",
            "It smells like the night my village burned.",
            "Then take it to the scientist at the Research Lab.",
            "If anyone can make sense of it, he can."
    };
    private static final String[] SECOND_QUEST_SCIENTIST_INTRO_SPEAKERS = {
            "Scientist",
            "Scientist",
            "Character",
            "Scientist",
            "Character",
            "Character",
            "Scientist",
            "Scientist",
            "Character",
            "Scientist",
            "Scientist",
            "Scientist"
    };
    private static final String[] SECOND_QUEST_SCIENTIST_INTRO_DIALOGUE_LINES = {
            "Let me see it.",
            "...This is no ordinary ash.",
            "So I was right.",
            "You have seen this before.",
            "When I was a child.",
            "The same smell filled the air the night everything burned.",
            "This fragment carries demonic residue.",
            "Old, but active.",
            "Then the demon is moving again?",
            "I cannot prove that yet.",
            "But I may have records upstairs that match this trace.",
            "Go to the second floor and bring me the archive notes from the worktable."
    };
    private static final String[] SECOND_QUEST_SCIENTIST_RETURN_SPEAKERS = {
            "Scientist",
            "Scientist",
            "Character",
            "Scientist",
            "Character",
            "Scientist",
            "Scientist",
            "Scientist"
    };
    private static final String[] SECOND_QUEST_SCIENTIST_RETURN_DIALOGUE_LINES = {
            "These notes confirm it.",
            "The residue matches records from the same demonic outbreak that destroyed your first home.",
            "So it really is connected...",
            "Yes.",
            "And if this trace has reached the village, it may only be the beginning.",
            "Then I need to be ready.",
            "You will return here whenever you grow stronger. I can help you shape that strength.",
            "But not all techniques can be learned at once. Some require greater control."
    };
    private static final String[] THIRD_QUEST_SCIENTIST_SENDOFF_SPEAKERS = {
            "Scientist",
            "Scientist"
    };
    private static final String[] THIRD_QUEST_SCIENTIST_SENDOFF_DIALOGUE_LINES = {
            "You have the will to fight. Now you need the discipline to survive.",
            "Go to your father. If anyone can prepare you for what is coming, it is him."
    };
    private static final String[] THIRD_QUEST_FATHER_RETURN_SPEAKERS = {
            "Father",
            "Father",
            "Character",
            "Character",
            "Father",
            "Character",
            "Father",
            "Father",
            "Father",
            "Father",
            "Character",
            "Father"
    };
    private static final String[] THIRD_QUEST_FATHER_RETURN_DIALOGUE_LINES = {
            "You came back quieter than usual.",
            "That look on your face... I have only seen it once before.",
            "The scientist confirmed it.",
            "The same darkness that destroyed my old home has returned.",
            "...I was afraid that day would come.",
            "If the village is truly in danger, I cannot keep hiding behind peace.",
            "Then I will not stop you.",
            "But if you mean to fight what is coming, you will train first.",
            "Fear is not shameful.",
            "Hesitation is.",
            "Then teach me.",
            "Good. Meet me outside. Behind the inn. We begin there."
    };
    private static final String[] THIRD_QUEST_FATHER_TRAINING_INTRO_SPEAKERS = {
            "Father",
            "Father",
            "Father",
            "Father",
            "Father"
    };
    private static final String[] THIRD_QUEST_FATHER_TRAINING_INTRO_DIALOGUE_LINES = {
            "Listen carefully.",
            "You do not survive a battle by swinging wildly.",
            "You survive by staying calm, watching your enemy, and striking with purpose.",
            "First, move.",
            "A warrior rooted to the ground is already dead."
    };
    private static final String[] THIRD_QUEST_FATHER_AFTER_MOVEMENT_SPEAKERS = {
            "Father",
            "Father",
            "Father"
    };
    private static final String[] THIRD_QUEST_FATHER_AFTER_MOVEMENT_DIALOGUE_LINES = {
            "Good.",
            "Speed means little if you cannot control it.",
            "Now show me that you can strike."
    };
    private static final String[] THIRD_QUEST_FATHER_AFTER_ATTACK_SPEAKERS = {
            "Father",
            "Father",
            "Father",
            "Father",
            "Father"
    };
    private static final String[] THIRD_QUEST_FATHER_AFTER_ATTACK_DIALOGUE_LINES = {
            "Again.",
            "Do not swing out of panic.",
            "Every strike must have intent.",
            "A still target teaches your hands. A moving one teaches your instincts.",
            "Watch how they move. Do not rush in blindly."
    };
    private static final String[] THIRD_QUEST_FATHER_PRE_SPAR_SPEAKERS = {
            "Father",
            "Father",
            "Character",
            "Father",
            "Father",
            "Character",
            "Father"
    };
    private static final String[] THIRD_QUEST_FATHER_PRE_SPAR_DIALOGUE_LINES = {
            "Better. You are starting to fight with your head, not your fear.",
            "Because real combat is not just about striking. It is about committing at the right moment.",
            "But it still feels slow.",
            "Now you face me.",
            "I want you to stop thinking of yourself as that helpless child.",
            "You want me to fight you?",
            "Come at me."
    };
    private static final String[] THIRD_QUEST_FATHER_COMPLETE_SPEAKERS = {
            "Father",
            "Father",
            "Character",
            "Father",
            "Father",
            "Father",
            "Character",
            "Father",
            "Father"
    };
    private static final String[] THIRD_QUEST_FATHER_COMPLETE_DIALOGUE_LINES = {
            "That is enough.",
            "You are still inexperienced. But you are no longer powerless.",
            "If the demon comes... I want to be ready.",
            "Then keep moving forward.",
            "Not for revenge alone. For this village. For the life you still have.",
            "There is one more technique you are ready to learn.",
            "I understand.",
            "It is not graceful, but it closes distance and turns hesitation into force.",
            "Use it when hesitation would get you killed."
    };
    private static final String[] THIRD_QUEST_SPAR_CALLOUTS = {
            "Do not freeze.",
            "Watch my shoulders.",
            "Strike after I overextend.",
            "Good. Again."
    };
    private static final String[] FOURTH_QUEST_INTERRUPTION_SPEAKERS = {
            "Villager",
            "Villager",
            "Villager",
            "Father",
            "Character",
            "Father",
            "Father"
    };
    private static final String[] FOURTH_QUEST_INTERRUPTION_DIALOGUE_LINES = {
            "There you are!",
            "Something is wrong near the bakery!",
            "There are burn marks on the ground... and the air smells strange again!",
            "So it has begun.",
            "The mark returned...",
            "Go to the scientist.",
            "If this is connected to the demon, he needs to see it at once."
    };
    private static final String[] FOURTH_QUEST_SCIENTIST_REPORT_SPEAKERS = {
            "Scientist",
            "Character",
            "Character",
            "Scientist",
            "Scientist",
            "Character",
            "Scientist",
            "Scientist",
            "Scientist",
            "Father",
            "Scientist",
            "Character",
            "Scientist",
            "Scientist",
            "Scientist",
            "Scientist"
    };
    private static final String[] FOURTH_QUEST_SCIENTIST_REPORT_DIALOGUE_LINES = {
            "You returned quickly. That alone tells me this is not good news.",
            "A villager found fresh burn marks near the bakery.",
            "The smell is the same.",
            "Then the residue is spreading faster than I feared.",
            "The fragment you brought before was only a trace.",
            "Anchoring?",
            "Demonic incursions rarely begin with a full assault.",
            "They begin with signs. Residue. Heat. Corruption.",
            "Then they open a path.",
            "So this village is not merely being watched. It is being prepared.",
            "Yes. And if we do not disrupt the anchors now, the village may become the next battlefield.",
            "Tell me what to do.",
            "There may be more than one mark.",
            "I need fresh samples from every corrupted site before I can determine the source.",
            "Search the village. If you find a sigil, examine it.",
            "If corruption appears, destroy it and bring me what remains."
    };
    private static final String[] FOURTH_QUEST_CAFE_OWNER_SPEAKERS = {
            "Bakery Owner",
            "Bakery Owner",
            "Character"
    };
    private static final String[] FOURTH_QUEST_CAFE_OWNER_DIALOGUE_LINES = {
            "It spread farther since you left.",
            "The ground was only warm before. Now it looks burned from within.",
            "Stay back."
    };
    private static final String[] FOURTH_QUEST_CAFE_MARK_SPEAKERS = {
            "Character"
    };
    private static final String[] FOURTH_QUEST_CAFE_MARK_DIALOGUE_LINES = {
            "This is no accident..."
    };
    private static final String[] FOURTH_QUEST_CENTER_MARK_SPEAKERS = {
            "Villager",
            "Villager",
            "Character"
    };
    private static final String[] FOURTH_QUEST_CENTER_MARK_DIALOGUE_LINES = {
            "It appeared out of nowhere!",
            "The air burns when I get too close!",
            "Stand back. I will clear it."
    };
    private static final String[] FOURTH_QUEST_CENTER_AFTER_SPEAKERS = {
            "Character",
            "Villager",
            "Character"
    };
    private static final String[] FOURTH_QUEST_CENTER_AFTER_DIALOGUE_LINES = {
            "These marks are not random. They are being placed.",
            "If this is the same evil from years ago... will it destroy us too?",
            "Not while I can still fight."
    };
    private static final String[] FOURTH_QUEST_EDGE_MARK_SPEAKERS = {
            "Character",
            "Character"
    };
    private static final String[] FOURTH_QUEST_EDGE_MARK_DIALOGUE_LINES = {
            "The air is heavier here...",
            "This one feels stronger than the others. Stay sharp."
    };
    private static final String[] FOURTH_QUEST_EDGE_AFTER_SPEAKERS = {
            "Character"
    };
    private static final String[] FOURTH_QUEST_EDGE_AFTER_DIALOGUE_LINES = {
            "This was no wandering corruption. That thing came here with purpose."
    };
    private static final String[] FOURTH_QUEST_SAMPLE_ANALYSIS_SPEAKERS = {
            "Scientist",
            "Scientist",
            "Character",
            "Scientist",
            "Scientist",
            "Character",
            "Scientist",
            "Father",
            "Scientist",
            "Character",
            "Scientist"
    };
    private static final String[] FOURTH_QUEST_SAMPLE_ANALYSIS_DIALOGUE_LINES = {
            "Three samples. Three separate anchor points.",
            "...Yes. This is worse than I thought.",
            "What do they mean?",
            "They form a pattern. Not a random corruption pattern. A coordinated one.",
            "These marks were not meant to kill. They were meant to open.",
            "Open what?",
            "A path. A spatial breach from the demon's domain into the village.",
            "So the attack has already begun.",
            "Not fully. The breach is unstable. But if it matures, monsters will pour through.",
            "Then we destroy it now.",
            "First I need to confirm where it will emerge. My resonance array upstairs can map the breach, but it requires manual activation."
    };
    private static final String[] FOURTH_QUEST_ARRAY_SCAN_COMPLETE_SPEAKERS = {
            "Scientist",
            "Scientist",
            "Character",
            "Scientist",
            "Father"
    };
    private static final String[] FOURTH_QUEST_ARRAY_SCAN_COMPLETE_DIALOGUE_LINES = {
            "The source is confirmed.",
            "The breach point is forming near the outer road. And the energy signature extends beyond the village... all the way to the demon castle.",
            "So the castle truly is the source.",
            "Yes. But if you rush there now, the village may fall behind you.",
            "Then we close this breach first."
    };
    private static final String[] FOURTH_QUEST_BREACH_INTRO_SPEAKERS = {
            "Character",
            "Scientist",
            "Father"
    };
    private static final String[] FOURTH_QUEST_BREACH_INTRO_DIALOGUE_LINES = {
            "So this is the path it was opening...",
            "This is only a fragment of a full breach. But even this much is dangerous.",
            "Enough talking. It's here."
    };
    private static final String[] FOURTH_QUEST_BREACH_AFTER_SPEAKERS = {
            "Character",
            "Scientist",
            "Scientist"
    };
    private static final String[] FOURTH_QUEST_BREACH_AFTER_DIALOGUE_LINES = {
            "What is this?",
            "A condensed core of the breach itself.",
            "With this... I may be able to do more than study the corruption. I may be able to shape it."
    };
    private static final String[] FOURTH_QUEST_SPATIAL_UNLOCK_SPEAKERS = {
            "Scientist",
            "Scientist",
            "Character",
            "Scientist",
            "Father",
            "Scientist",
            "Scientist",
            "Character",
            "Scientist"
    };
    private static final String[] FOURTH_QUEST_SPATIAL_UNLOCK_DIALOGUE_LINES = {
            "This Rift Core is unstable. But it contains exactly what I need.",
            "The demon's power bends space around its servants, its strongholds, and its attacks. That is why ordinary movement will not be enough in the castle.",
            "Then give me what I need.",
            "This is dangerous. I cannot promise this technique will feel natural.",
            "She has come this far. Do it.",
            "Very well.",
            "The spatial current is unstable, but now you can ride it for a moment. Not a charge. Not a strike. A displacement.",
            "A step through space itself...",
            "Yes. Use it wisely."
    };
    private static final String[] FOURTH_QUEST_EPILOGUE_SPEAKERS = {
            "Scientist",
            "Father",
            "Character",
            "Father",
            "Character",
            "Scientist"
    };
    private static final String[] FOURTH_QUEST_EPILOGUE_DIALOGUE_LINES = {
            "The breach in the village was only the beginning. As long as the castle stands, more will follow.",
            "You know what lies ahead.",
            "Yes. If I wait, this village becomes another graveyard.",
            "Then go before fear has time to return.",
            "I will end this at the source.",
            "The path to the demon castle is open now. When you are ready, go."
    };
    private static final String[] FOURTH_QUEST_BREACH_CALLOUTS = {
            "Do not overcommit!",
            "Strike and move!",
            "Watch the gap after its attack!"
    };
    private static final String[] FIFTH_QUEST_LAB_ALARM_SPEAKERS = {
            "Villager",
            "Villager",
            "Villager",
            "Scientist",
            "Character",
            "Father",
            "Scientist",
            "Scientist",
            "Character",
            "Father"
    };
    private static final String[] FIFTH_QUEST_LAB_ALARM_DIALOGUE_LINES = {
            "The ruined village...!",
            "There are creatures gathering there!",
            "And the portal... it's alive!",
            "So the breach has shifted.",
            "The abandoned village...",
            "Then that is where the final path has opened.",
            "If demons are gathering near the portal, then the castle is no longer distant.",
            "It is already pressing against this world.",
            "Then I go now.",
            "Not to the castle yet. First you stop whatever is crossing through."
    };
    private static final String[] FIFTH_QUEST_RUINS_INTRO_SPEAKERS = {
            "Character",
            "Character",
            "Character",
            "Character"
    };
    private static final String[] FIFTH_QUEST_RUINS_INTRO_DIALOGUE_LINES = {
            "So many years, and I still remember every road here.",
            "This was home before the demon turned it into ash.",
            "I used to think leaving it behind would make this hurt less.",
            "It didn't."
    };
    private static final String[] FIFTH_QUEST_PORTAL_AFTER_SPEAKERS = {
            "Scientist",
            "Scientist",
            "Scientist",
            "Character",
            "Scientist",
            "Father",
            "Character"
    };
    private static final String[] FIFTH_QUEST_PORTAL_AFTER_DIALOGUE_LINES = {
            "The anchors are broken. That portal should have collapsed completely...",
            "But the energy is too concentrated. It can no longer spread outward.",
            "It has done the opposite. It has narrowed into a direct path.",
            "To the castle.",
            "Yes. A one-way opening to the source.",
            "Then the road ends here. And the final one begins.",
            "This is where it started. And this is where I stop running."
    };
    private static final String[] FIFTH_QUEST_COMMANDER_CALLOUTS = {
            "Keep moving!",
            "Do not let it corner you!",
            "Now! Strike!"
    };
    private static final String[] SIXTH_QUEST_PORTAL_BRIEFING_SPEAKERS = {
            "Scientist",
            "Scientist",
            "Father",
            "Character",
            "Father",
            "Father",
            "Father",
            "Character",
            "Scientist",
            "Scientist",
            "Father"
    };
    private static final String[] SIXTH_QUEST_PORTAL_BRIEFING_DIALOGUE_LINES = {
            "The anchors are gone. The portal is stable enough now.",
            "It will take you straight to the source.",
            "This is the last road.",
            "I know.",
            "You are not the frightened child I found in the ashes.",
            "You have a home now. A life.",
            "Fight for that.",
            "I will.",
            "Inside the castle, the space itself may be unstable.",
            "Use what you have learned. Strike hard. Move wisely.",
            "And come back alive."
    };
    private static final String[] SIXTH_QUEST_CASTLE_ARRIVAL_SPEAKERS = {
            "Character",
            "Demon",
            "Character",
            "Demon"
    };
    private static final String[] SIXTH_QUEST_CASTLE_ARRIVAL_DIALOGUE_LINES = {
            "So this is the source of it all...",
            "You survived the flames. How disappointing.",
            "I came to finish what you started.",
            "Then come, little ember. Let me see what grief has made of you."
    };
    private static final String[] SIXTH_QUEST_PHASE_1_INTRO_SPEAKERS = {
            "Demon",
            "Demon",
            "Character",
            "Demon",
            "Character",
            "Demon"
    };
    private static final String[] SIXTH_QUEST_PHASE_1_INTRO_DIALOGUE_LINES = {
            "I burned one village, and from its ashes you crawled toward me.",
            "Was it vengeance that kept you alive?",
            "No. It was the people who gave me a second home.",
            "Then I will burn that home as well.",
            "Not while I still stand.",
            "You carry ash, anger, and borrowed strength. And still you think you can kill me?"
    };
    private static final String[] SIXTH_QUEST_PHASE_1_TRANSITION_SPEAKERS = {
            "Demon",
            "Demon",
            "Demon"
    };
    private static final String[] SIXTH_QUEST_PHASE_1_TRANSITION_DIALOGUE_LINES = {
            "So this is the strength that survived my fire...",
            "Come, then.",
            "If you want the end... face me where fire has no limit."
    };
    private static final String[] SIXTH_QUEST_FINAL_PHASE_INTRO_SPEAKERS = {
            "Character"
    };
    private static final String[] SIXTH_QUEST_FINAL_PHASE_INTRO_DIALOGUE_LINES = {
            "No more running. No more ashes. This ends here."
    };
    private static final String[] SIXTH_QUEST_ENDING_SPEAKERS = {
            "Father",
            "Character",
            "Scientist",
            "Father",
            "Character",
            "Father",
            "Character",
            "Old Man"
    };
    private static final String[] SIXTH_QUEST_ENDING_DIALOGUE_LINES = {
            "You came back.",
            "It's over.",
            "The demonic signal is gone. The breach has fully collapsed.",
            "Then the dead can rest. And so can you.",
            "I thought this place would always be the end of my story. But it was only where the fire began.",
            "And now?",
            "Now I go home.",
            "The fire that once took everything from her was finally extinguished. From grief, she found strength. From ashes, she found a home. And from the last ember, a new dawn was born."
    };
    private static final String[] SIXTH_QUEST_PHASE_1_CALLOUTS = {
            "Too slow.",
            "Burn.",
            "Kneel.",
            "You are still only human."
    };
    private static final String[] SIXTH_QUEST_PHASE_2_CALLOUTS = {
            "I am fire eternal!",
            "You cannot outrun this flame.",
            "Kneel before the night.",
            "Burn with your past."
    };
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
    private static final String ROOM3_DOOR_ANIMATION_GROUP_NAME = "DOOR-Room-3-Animation";
    private static final String ROOM3_DOOR_TRIGGER_LAYER_NAME = "Door-Close(open-after-room-clear)";
    private static final String ROOM3_DOOR_TRIGGER_LAYER_NAME_ALT = "ThisIsTheAreaThatDontDoAnimation";
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
    private static final String BOSS_ARENA_PHASE_2_PLAYER_SPAWN_LAYER_NAME = "SPAWN";
    private static final String BOSS_ARENA_PHASE_2_EXPLOSION_WALKABLE_LAYER_NAME = "WALKABLE AREA";
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
    private static final float ROOM3_DOOR_FRAME_DURATION_SECONDS = 0.1f;
    private static final float ROOM3_DOOR_TRIGGER_PAUSE_SECONDS = 0.2f;
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
    private static final float MAX_LEVEL_MOVE_SPEED_MULTIPLIER = 1.2f;
    private static final float DASH_SPEED = 220f;
    private static final float DASH_DURATION = 0.108f;
    private static final float DASH_COOLDOWN = 0.6f;
    private static final float DASH_STAMINA_COST = 10f;
    private static final float STAMINA_REGEN_PER_SECOND = 3f;
    private static final float HEALTH_REGEN_PER_SECOND = 1.5f;
    private static final float POST_DASH_MOVEMENT_LOCK_SECONDS = 0.2f;
    private static final float DASH_ATTACK_TELEPORT_TILES = 4f;
    private static final float DASH_ATTACK_ABILITY_COOLDOWN_SECONDS = 3f;
    private static final float SPATIAL_DASH_ABILITY_COOLDOWN_SECONDS = 5f;
    private static final float DASH_ATTACK_DAMAGE_MULTIPLIER = 2.5f;
    private static final float SPATIAL_DASH_DAMAGE_MULTIPLIER = 4f;
    private static final float DEFAULT_TILE_SIZE = 16f;
    private static final float TELEPORT_CAMERA_SMOOTH_DURATION = 0.25f;
    private static final float TELEPORT_CAMERA_SMOOTH_SPEED = 14f;
    private static final int ABILITY_COOLDOWN_CIRCLE_TEXTURE_SIZE = 96;
    private static final float ABILITY_COOLDOWN_ICON_SIZE = 42f;
    private static final float ABILITY_COOLDOWN_ICON_GAP = 12f;
    private static final float ABILITY_COOLDOWN_MARGIN_RIGHT = 24f;
    private static final float ABILITY_COOLDOWN_MARGIN_BOTTOM = 28f;
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
    private static final float PLAYER_HAZARD_HIT_REACTION_SECONDS = 0.2f;
    private static final float PLAYER_DEATH_ANIMATION_SECONDS = 0.8f;
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
    private static final float VILLAGE_BAT_ATTACK_INTERVAL_SECONDS = 1.5f;
    private static final float BAT_PLAYER_HIT_COOLDOWN_SECONDS = 0.2f;
    private static final float BAT_MAX_HEALTH = 100f;
    private static final float BAT_HIT_DAMAGE = 15f;
    private static final float BAT_PLAYER_DAMAGE = 30f;
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
    private static final float EVIL_WIZARD_PLAYER_DAMAGE = 100f;
    private static final float EVIL_WIZARD_ATTACK_HAZARD_DAMAGE = EVIL_WIZARD_PLAYER_DAMAGE;
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
            PROJECT_ROOT_ABSOLUTE_PATH + "/DSA GAME/ENEMY-Assets/Golem_1/Orange/White_Swoosh_VFX/Golem_1_idle.png";
    private static final String GOLEM_1_WALK_TEXTURE_ABSOLUTE_PATH =
            PROJECT_ROOT_ABSOLUTE_PATH + "/DSA GAME/ENEMY-Assets/Golem_1/Orange/White_Swoosh_VFX/Golem_1_walk.png";
    private static final String GOLEM_1_ATTACK_TEXTURE_ABSOLUTE_PATH =
            PROJECT_ROOT_ABSOLUTE_PATH + "/DSA GAME/ENEMY-Assets/Golem_1/Orange/White_Swoosh_VFX/Golem_1_attack.png";
    private static final String GOLEM_1_HURT_TEXTURE_ABSOLUTE_PATH =
            PROJECT_ROOT_ABSOLUTE_PATH + "/DSA GAME/ENEMY-Assets/Golem_1/Orange/White_Swoosh_VFX/Golem_1_hurt.png";
    private static final String GOLEM_1_DIE_TEXTURE_ABSOLUTE_PATH =
            PROJECT_ROOT_ABSOLUTE_PATH + "/DSA GAME/ENEMY-Assets/Golem_1/Orange/White_Swoosh_VFX/Golem_1_die.png";
    private static final String GOLEM_1_EXPLOSION_TEXTURE_ABSOLUTE_PATH =
            PROJECT_ROOT_ABSOLUTE_PATH + "/DSA GAME/ENEMY-Assets/Golem_1/Orange/White_Swoosh_VFX/Golem_explosion.png";
    private static final String BOSS_ARENA_PHASE_2_DEMON_IDLE_FRAMES_DIR_ABSOLUTE_PATH =
            PROJECT_ROOT_ABSOLUTE_PATH + "/DSA GAME/ENEMY-Assets/boss_demon_slime_FREE_v1.0/individual sprites/01_demon_idle";
    private static final String BOSS_ARENA_PHASE_2_DEMON_WALK_FRAMES_DIR_ABSOLUTE_PATH =
            PROJECT_ROOT_ABSOLUTE_PATH + "/DSA GAME/ENEMY-Assets/boss_demon_slime_FREE_v1.0/individual sprites/02_demon_walk";
    private static final String BOSS_ARENA_PHASE_2_DEMON_ATTACK_FRAMES_DIR_ABSOLUTE_PATH =
            PROJECT_ROOT_ABSOLUTE_PATH + "/DSA GAME/ENEMY-Assets/boss_demon_slime_FREE_v1.0/individual sprites/03_demon_cleave";
    private static final String BOSS_ARENA_PHASE_2_DEMON_HURT_FRAMES_DIR_ABSOLUTE_PATH =
            PROJECT_ROOT_ABSOLUTE_PATH + "/DSA GAME/ENEMY-Assets/boss_demon_slime_FREE_v1.0/individual sprites/04_demon_take_hit";
    private static final String BOSS_ARENA_PHASE_2_DEMON_IDLE_TEXTURE_ABSOLUTE_PATH =
            PROJECT_ROOT_ABSOLUTE_PATH + "/DSA GAME/ENEMY-Assets/boss_demon_slime_FREE_v1.0/spritesheets/demon_slime_FREE_v1.0_288x160_spritesheet.png";
    private static final String BOSS_ARENA_PHASE_2_SKILL_START_FRAMES_DIR_ABSOLUTE_PATH =
            PROJECT_ROOT_ABSOLUTE_PATH + "/DSA GAME/ENEMY-Assets/boss_demon_slime_FREE_v1.0/VFX1/part1(start)/frames";
    private static final String BOSS_ARENA_PHASE_2_SKILL_LOOP_FRAMES_DIR_ABSOLUTE_PATH =
            PROJECT_ROOT_ABSOLUTE_PATH + "/DSA GAME/ENEMY-Assets/boss_demon_slime_FREE_v1.0/VFX1/part2(loop)/frames";
    private static final String BOSS_ARENA_PHASE_2_SKILL_END_FRAMES_DIR_ABSOLUTE_PATH =
            PROJECT_ROOT_ABSOLUTE_PATH + "/DSA GAME/ENEMY-Assets/boss_demon_slime_FREE_v1.0/VFX1/part3(end)/frames";
    private static final String BOSS_ARENA_PHASE_2_EXPLOSION_FRAMES_DIR_ABSOLUTE_PATH =
            PROJECT_ROOT_ABSOLUTE_PATH + "/DSA GAME/ENEMY-Assets/boss_demon_slime_FREE_v1.0/explosion pack 1/explosion pack 1/Explosions pack/explosion-1-d/Sprites";
    private static final String FIFTH_QUEST_DEMON_1_IDLE_TEXTURE_ABSOLUTE_PATH =
            PROJECT_ROOT_ABSOLUTE_PATH + "/DSA GAME/ENEMY-Assets/Demon_1/Idle.png";
    private static final String FIFTH_QUEST_DEMON_1_WALK_TEXTURE_ABSOLUTE_PATH =
            PROJECT_ROOT_ABSOLUTE_PATH + "/DSA GAME/ENEMY-Assets/Demon_1/Walk.png";
    private static final String FIFTH_QUEST_DEMON_1_ATTACK_TEXTURE_ABSOLUTE_PATH =
            PROJECT_ROOT_ABSOLUTE_PATH + "/DSA GAME/ENEMY-Assets/Demon_1/Attack.png";
    private static final String FIFTH_QUEST_DEMON_1_HURT_TEXTURE_ABSOLUTE_PATH =
            PROJECT_ROOT_ABSOLUTE_PATH + "/DSA GAME/ENEMY-Assets/Demon_1/Hurt.png";
    private static final String FIFTH_QUEST_DEMON_1_DEAD_TEXTURE_ABSOLUTE_PATH =
            PROJECT_ROOT_ABSOLUTE_PATH + "/DSA GAME/ENEMY-Assets/Demon_1/Dead.png";
    private static final String FIFTH_QUEST_DEMON_2_IDLE_TEXTURE_ABSOLUTE_PATH =
            PROJECT_ROOT_ABSOLUTE_PATH + "/DSA GAME/ENEMY-Assets/Demon_2/Idle.png";
    private static final String FIFTH_QUEST_DEMON_2_WALK_TEXTURE_ABSOLUTE_PATH =
            PROJECT_ROOT_ABSOLUTE_PATH + "/DSA GAME/ENEMY-Assets/Demon_2/Walk.png";
    private static final String FIFTH_QUEST_DEMON_2_ATTACK_TEXTURE_ABSOLUTE_PATH =
            PROJECT_ROOT_ABSOLUTE_PATH + "/DSA GAME/ENEMY-Assets/Demon_2/Attack.png";
    private static final String FIFTH_QUEST_DEMON_2_HURT_TEXTURE_ABSOLUTE_PATH =
            PROJECT_ROOT_ABSOLUTE_PATH + "/DSA GAME/ENEMY-Assets/Demon_2/Hurt.png";
    private static final String FIFTH_QUEST_DEMON_2_DEAD_TEXTURE_ABSOLUTE_PATH =
            PROJECT_ROOT_ABSOLUTE_PATH + "/DSA GAME/ENEMY-Assets/Demon_2/Dead.png";
    private static final String FIFTH_QUEST_DEMON_3_IDLE_TEXTURE_ABSOLUTE_PATH =
            PROJECT_ROOT_ABSOLUTE_PATH + "/DSA GAME/ENEMY-Assets/Demon_3/Idle.png";
    private static final String FIFTH_QUEST_DEMON_3_WALK_TEXTURE_ABSOLUTE_PATH =
            PROJECT_ROOT_ABSOLUTE_PATH + "/DSA GAME/ENEMY-Assets/Demon_3/Walk.png";
    private static final String FIFTH_QUEST_DEMON_3_ATTACK_TEXTURE_ABSOLUTE_PATH =
            PROJECT_ROOT_ABSOLUTE_PATH + "/DSA GAME/ENEMY-Assets/Demon_3/Attack.png";
    private static final String FIFTH_QUEST_DEMON_3_HURT_TEXTURE_ABSOLUTE_PATH =
            PROJECT_ROOT_ABSOLUTE_PATH + "/DSA GAME/ENEMY-Assets/Demon_3/Hurt.png";
    private static final String FIFTH_QUEST_DEMON_3_DEAD_TEXTURE_ABSOLUTE_PATH =
            PROJECT_ROOT_ABSOLUTE_PATH + "/DSA GAME/ENEMY-Assets/Demon_3/Dead.png";
    private static final String FIFTH_QUEST_ADULT_MAN_IDLE_DIRECTORY =
            PROJECT_ROOT_ABSOLUTE_PATH + "/DSA GAME/NPC/Idle2";
    private static final String FIFTH_QUEST_OLD_WOMAN_IDLE_DIRECTORY =
            PROJECT_ROOT_ABSOLUTE_PATH + "/DSA GAME/NPC/Idle3";
    private static final String CHEST_IDLE_TEXTURE_ABSOLUTE_PATH =
            PROJECT_ROOT_ABSOLUTE_PATH + "/DSA GAME/ORIGINAL ASSETS/The dungeon (2)/The dungeon/Chest/Skull_chest_idle.png";
    private static final String CHEST_OPEN_TEXTURE_ABSOLUTE_PATH =
            PROJECT_ROOT_ABSOLUTE_PATH + "/DSA GAME/ORIGINAL ASSETS/The dungeon (2)/The dungeon/Chest/Skull_chest_open.png";
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
    private static final float GOLEM_1_PLAYER_DAMAGE = 80f;
    private static final float GOLEM_1_HP_BAR_DOWN_OFFSET_TILES = 3.5f;
    private static final float GOLEM_1_ATTACK_COOLDOWN_SECONDS = 3f;
    private static final float GOLEM_1_EXPLOSION_DELAY_SECONDS = 0.7f;
    private static final float GOLEM_1_EXPLOSION_CHAIN_STEP_TILES = 2.5f;
    private static final float GOLEM_1_EXPLOSION_CHAIN_MAX_DISTANCE_TILES = 15f;
    private static final float GOLEM_1_EXPLOSION_CHAIN_STEP_DELAY_SECONDS = 0.3f;
    private static final float GOLEM_1_EXPLOSION_SOURCE_OFFSET_Y_TILES = -2f;
    private static final float GOLEM_1_EXPLOSION_DAMAGE = GOLEM_1_PLAYER_DAMAGE;
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
    private static final int BOSS_ARENA_PHASE_2_DEMON_ATTACK_HOLD_FRAME_INDEX = 8;
    private static final float BOSS_ARENA_PHASE_2_DEMON_ATTACK_HOLD_SECONDS = 0.5f;
    private static final float BOSS_ARENA_PHASE_2_DEMON_HURT_FRAME_DURATION = 0.08f;
    private static final float BOSS_ARENA_PHASE_2_DEMON_DRAW_SCALE = 2f;
    private static final float BOSS_ARENA_PHASE_2_DEMON_HITBOX_WIDTH_SCALE = 0.189f;
    private static final float BOSS_ARENA_PHASE_2_DEMON_HITBOX_HEIGHT_SCALE = 0.45f;
    private static final float BOSS_ARENA_PHASE_2_DEMON_HITBOX_RIGHT_OFFSET_TILES = 0.3f;
    private static final float BOSS_ARENA_PHASE_2_DEMON_HITBOX_DOWN_OFFSET_TILES = 5.8f;
    private static final float BOSS_ARENA_PHASE_2_DEMON_SPAWN_OFFSET_X_TILES = -5f;
    private static final float BOSS_ARENA_PHASE_2_DEMON_SPAWN_OFFSET_Y_TILES = 4f;
    private static final float BOSS_ARENA_PHASE_2_DEMON_CHASE_SPEED = 30f;
    private static final float BOSS_ARENA_PHASE_2_DEMON_CHASE_STOP_DISTANCE_TILES = 10f;
    private static final float BOSS_ARENA_PHASE_2_DEMON_ATTACK_RANGE = DEFAULT_TILE_SIZE * 25f;
    private static final float BOSS_ARENA_PHASE_2_DEMON_ATTACK_INTERVAL_SECONDS = 5f;
    private static final float BOSS_ARENA_PHASE_2_DEMON_PLAYER_HIT_COOLDOWN_SECONDS = 0.2f;
    private static final float BOSS_ARENA_PHASE_2_SKILL_START_FRAME_DURATION = 0.08f;
    private static final float BOSS_ARENA_PHASE_2_SKILL_LOOP_FRAME_DURATION = 0.08f;
    private static final float BOSS_ARENA_PHASE_2_SKILL_END_FRAME_DURATION = 0.08f;
    private static final float BOSS_ARENA_PHASE_2_SKILL_LOOP_SECONDS = 0.8f;
    private static final float BOSS_ARENA_PHASE_2_SKILL_COOLDOWN_SECONDS = 0.65f;
    private static final float BOSS_ARENA_PHASE_2_SKILL_DRAW_SCALE = 1.5f;
    private static final float BOSS_ARENA_PHASE_2_SKILL_OFFSET_Y_TILES = 0.5f;
    private static final float BOSS_ARENA_PHASE_2_EXPLOSION_FRAME_DURATION = 0.078f;
    private static final float BOSS_ARENA_PHASE_2_EXPLOSION_DRAW_SCALE = 1.14f;
    private static final int BOSS_ARENA_PHASE_2_EXPLOSION_SPOT_COUNT = 25;
    private static final float BOSS_ARENA_PHASE_2_EXPLOSION_CAST_DELAY_SECONDS = 1.5f;
    private static final float BOSS_ARENA_PHASE_2_EXPLOSION_CAST_DELAY_JITTER_SECONDS = 0f;
    private static final float BOSS_ARENA_PHASE_2_EXPLOSION_EDGE_MARGIN_TILES = 2f;
    private static final float BOSS_ARENA_PHASE_2_EXPLOSION_MIN_SEPARATION_TILES = 2.5f;
    private static final float BOSS_ARENA_PHASE_2_EXPLOSION_HIT_RADIUS_TILES = 2f;
    private static final float BOSS_ARENA_PHASE_2_EXPLOSION_MAX_DIAMETER_TILES = 9f;
    private static final float BOSS_ARENA_PHASE_2_EXPLOSION_SKILL_COOLDOWN_SECONDS = 5f;
    private static final float SIXTH_QUEST_PORTAL_EFFECT_LOOP_SECONDS = 3f;
    private static final float SIXTH_QUEST_PORTAL_EFFECT_DRAW_SCALE = 1.75f;
    private static final int FIFTH_QUEST_DEMON_FRAME_WIDTH = 128;
    private static final int FIFTH_QUEST_DEMON_FRAME_HEIGHT = 128;
    private static final float FIFTH_QUEST_DEMON_IDLE_FRAME_DURATION = 0.13f;
    private static final float FIFTH_QUEST_DEMON_WALK_FRAME_DURATION = 0.1f;
    private static final float FIFTH_QUEST_DEMON_ATTACK_FRAME_DURATION = 0.09f;
    private static final float FIFTH_QUEST_DEMON_HURT_FRAME_DURATION = 0.08f;
    private static final float FIFTH_QUEST_DEMON_DEAD_FRAME_DURATION = 0.11f;
    private static final float FIFTH_QUEST_DEMON_DRAW_SCALE = 0.62f;
    private static final float FIFTH_QUEST_DEMON_HITBOX_WIDTH_SCALE = 0.22f;
    private static final float FIFTH_QUEST_DEMON_HITBOX_HEIGHT_SCALE = 0.30f;
    private static final float FIFTH_QUEST_DEMON_HITBOX_DOWN_OFFSET_TILES = 1.8f;
    private static final float FIFTH_QUEST_DEMON_ATTACK_RANGE = DEFAULT_TILE_SIZE * 2.1f;
    private static final float FIFTH_QUEST_DEMON_PLAYER_HIT_COOLDOWN_SECONDS = 0.22f;
    private static final float FIFTH_QUEST_DEMON_ATTACK_COOLDOWN_SECONDS = 1.25f;
    private static final float FIFTH_QUEST_DEMON_SHADOW_WIDTH_TILES = 2.4f;
    private static final float FIFTH_QUEST_DEMON_SHADOW_HEIGHT_TILES = 0.78f;
    private static final float FIFTH_QUEST_DEMON_SHADOW_DOWN_OFFSET_TILES = 1.8f;
    private static final float FIFTH_QUEST_DEMON_SHADOW_ALPHA = 0.7f;
    private static final float FIFTH_QUEST_VILLAGER_DRAW_SCALE = 0.52f;
    private static final float FIFTH_QUEST_LAB_FATHER_OFFSET_X = -88f;
    private static final float FIFTH_QUEST_LAB_FATHER_OFFSET_Y = 6f;
    private static final float FIFTH_QUEST_OLD_WOMAN_X_SHIFT = 7.8f * DEFAULT_TILE_SIZE + 5f;
    private static final float FIFTH_QUEST_LAB_VILLAGER_FADE_IN_SECONDS = 2f;
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
    private static final String PLAYER_DEATH_TEXTURE_INTERNAL_PATH = "images/huntress/15 - death.png";
    private static final String PLAYER_HIT_TEXTURE_ABSOLUTE_PATH =
            PROJECT_ROOT_ABSOLUTE_PATH + "/SHADOW Series - The Blind Huntress/Sprite Sheet/14 - hit.png";
    private static final String PLAYER_DEATH_TEXTURE_ABSOLUTE_PATH =
            PROJECT_ROOT_ABSOLUTE_PATH + "/SHADOW Series - The Blind Huntress/Sprite Sheet/15 - death.png";
    private static final String EVIL_WIZARD_ATTACK1_SFX_ABSOLUTE_PATH =
            PROJECT_ROOT_ABSOLUTE_PATH + "/DSA GAME/ENEMY-Assets/EVil Wizard 2/wizard1.mp3";
    private static final String EVIL_WIZARD_ATTACK2_SFX_ABSOLUTE_PATH =
            PROJECT_ROOT_ABSOLUTE_PATH + "/DSA GAME/ENEMY-Assets/EVil Wizard 2/wizard2.mp3";
    private static final String EVIL_WIZARD_PRE_EXPLOSION_SFX_ABSOLUTE_PATH =
            PROJECT_ROOT_ABSOLUTE_PATH + "/DSA GAME/ENEMY-Assets/EVil Wizard 2/wizard-explosion.mp3";
    private static final String GOLEM_1_EXPLOSION_SFX_ABSOLUTE_PATH =
            PROJECT_ROOT_ABSOLUTE_PATH + "/DSA GAME/ENEMY-Assets/Golem_1/Orange/White_Swoosh_VFX/Golem_explosion.mp3";

    private static String resolveProjectRootAbsolutePath() {
        File current = new File(System.getProperty("user.dir", ".")).getAbsoluteFile();
        for (int depth = 0; depth < 6 && current != null; depth++) {
            File settingsGradle = new File(current, "settings.gradle");
            File coreModule = new File(current, "core");
            if (settingsGradle.isFile() && coreModule.isDirectory()) {
                return current.getAbsolutePath().replace('\\', '/');
            }
            current = current.getParentFile();
        }
        return new File(System.getProperty("user.dir", ".")).getAbsolutePath().replace('\\', '/');
    }

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

    private enum BossArenaFinalIntroPhase {
        NONE,
        AUTO_WALK,
        FOCUS_BOSS
    }

    private enum FifthQuestIntroCameraPhase {
        NONE,
        CAMERA_1,
        CAMERA_2,
        CAMERA_3
    }

    private enum QuestDialogueId {
        NONE,
        FIRST_QUEST_FATHER,
        SECOND_QUEST_CAFE_OWNER,
        SECOND_QUEST_SCIENTIST_INTRO,
        SECOND_QUEST_SCIENTIST_RETURN,
        THIRD_QUEST_SCIENTIST_SENDOFF,
        THIRD_QUEST_FATHER_RETURN,
        THIRD_QUEST_FATHER_TRAINING_INTRO,
        THIRD_QUEST_FATHER_AFTER_MOVEMENT,
        THIRD_QUEST_FATHER_AFTER_ATTACK,
        THIRD_QUEST_FATHER_PRE_SPAR,
        THIRD_QUEST_FATHER_COMPLETE,
        FOURTH_QUEST_INTERRUPTION,
        FOURTH_QUEST_SCIENTIST_REPORT,
        FOURTH_QUEST_CAFE_OWNER,
        FOURTH_QUEST_CAFE_MARK,
        FOURTH_QUEST_CENTER_MARK,
        FOURTH_QUEST_CENTER_AFTER,
        FOURTH_QUEST_EDGE_MARK,
        FOURTH_QUEST_EDGE_AFTER,
        FOURTH_QUEST_SAMPLE_ANALYSIS,
        FOURTH_QUEST_ARRAY_SCAN_COMPLETE,
        FOURTH_QUEST_BREACH_INTRO,
        FOURTH_QUEST_BREACH_AFTER,
        FOURTH_QUEST_SPATIAL_UNLOCK,
        FOURTH_QUEST_EPILOGUE,
        FIFTH_QUEST_LAB_ALARM,
        FIFTH_QUEST_RUINS_INTRO,
        FIFTH_QUEST_PORTAL_AFTER,
        SIXTH_QUEST_PORTAL_BRIEFING,
        SIXTH_QUEST_CASTLE_ARRIVAL,
        SIXTH_QUEST_PHASE_1_INTRO,
        SIXTH_QUEST_PHASE_1_TRANSITION,
        SIXTH_QUEST_FINAL_PHASE_INTRO,
        SIXTH_QUEST_ENDING
    }

    private enum QuestItemCardId {
        NONE,
        FIRST_QUEST_HERB_AND_SPICE,
        SECOND_QUEST_SCORCHED_FRAGMENT,
        SECOND_QUEST_ARCHIVE_NOTES,
        FOURTH_QUEST_CONDENSED_CORE,
        THIRD_QUEST_DASH_ATTACK_UNLOCKED,
        FOURTH_QUEST_SPATIAL_DASH_UNLOCKED,
        SIXTH_QUEST_CLEAR
    }

    private enum NpcDialogueVoiceSet {
        NONE,
        FATHER,
        BAKERY,
        SCIENTIST,
        DEMON,
        ZOMBIE
    }

    private enum FifthQuestDemonType {
        DEMON_1,
        DEMON_2,
        DEMON_3,
        COMMANDER
    }

    private static final class BatInstance {
        final Vector2 position = new Vector2();
        BatAnimationState state = BatAnimationState.SLEEPING;
        BatAnimationState stateBeforeHurt = BatAnimationState.SLEEPING;
        float stateTime = 0f;
        float playerHitCooldownTimer = 0f;
        boolean useAttack1Next = true;
        boolean usingAttack1 = true;
        boolean attackDamageApplied = false;
        boolean tutorialEnemy = false;
        int questEncounterId = FOURTH_QUEST_ENCOUNTER_NONE;
        boolean elite = false;
        float maxHealth = BAT_MAX_HEALTH;
        float health = BAT_MAX_HEALTH;
        float chaseSpeed = BAT_CHASE_SPEED;
        float playerDamage = BAT_PLAYER_DAMAGE;
        float attackCooldownTimer = 0f;
        float attackIntervalSeconds = 0f;
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
        float attackCooldownTimer = 0f;
        float hurtStateTime = 0f;
        float skillStateTime = 0f;
        float skillLoopTimer = 0f;
        float skillCooldownTimer = 0f;
        float maxHealth = SIXTH_QUEST_BOSS_PHASE_2_HEALTH;
        float health = SIXTH_QUEST_BOSS_PHASE_2_HEALTH;
        float playerHitCooldownTimer = 0f;
        boolean facingLeft = false;
        boolean moving = false;
        boolean attacking = false;
        boolean hurting = false;
        boolean attackDamageApplied = false;
        int lastDashAttackHitId = -1;
        BossArenaPhase2SkillPhase skillPhase = BossArenaPhase2SkillPhase.NONE;
    }

    private static final class BossArenaPhase2ExplosionInstance {
        final Vector2 position = new Vector2();
        float delayTimer = 0f;
        float stateTime = 0f;
        boolean active = false;
        boolean damageApplied = false;
    }

    private enum BossArenaPhase2SkillPhase {
        NONE,
        START,
        LOOP,
        END
    }

    private enum PortalActivationPhase {
        NONE,
        START,
        LOOP,
        END
    }

    private enum PortalChoiceResult {
        NONE,
        ENTER_CASTLE,
        CLOSE_PORTAL
    }

    private enum EndingMusicTransitionPhase {
        NONE,
        FADING_OUT_BATTLE,
        FADING_IN_ENDING
    }

    private static final class FifthQuestDemonInstance {
        final Vector2 position = new Vector2();
        final Rectangle zoneBounds = new Rectangle();
        FifthQuestDemonType type = FifthQuestDemonType.DEMON_1;
        float stateTime = 0f;
        float attackStateTime = 0f;
        float hurtStateTime = 0f;
        float dieStateTime = 0f;
        float maxHealth = FIFTH_QUEST_DEMON_1_HEALTH;
        float health = FIFTH_QUEST_DEMON_1_HEALTH;
        float chaseSpeed = FIFTH_QUEST_DEMON_1_SPEED;
        float playerDamage = FIFTH_QUEST_DEMON_1_DAMAGE;
        float attackCooldownTimer = 0f;
        float playerHitCooldownTimer = 0f;
        float attackIntervalSeconds = FIFTH_QUEST_DEMON_ATTACK_COOLDOWN_SECONDS;
        float attackRange = FIFTH_QUEST_DEMON_ATTACK_RANGE;
        boolean facingLeft = false;
        boolean moving = false;
        boolean attacking = false;
        boolean hurting = false;
        boolean dying = false;
        boolean attackDamageApplied = false;
        int questEncounterId = FIFTH_QUEST_ENCOUNTER_NONE;
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
    private Texture abilityCooldownCircleTex;
    private HudRenderer hudRenderer;
    private Texture jumpUpAttackTex;
    private Texture jumpDownAttackTex;
    private Texture idleUpAttackTex;
    private Texture playerHitTex;
    private Texture playerDeathTex;
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
    private Texture fifthQuestDemon1IdleTex;
    private Texture fifthQuestDemon1WalkTex;
    private Texture fifthQuestDemon1AttackTex;
    private Texture fifthQuestDemon1HurtTex;
    private Texture fifthQuestDemon1DeadTex;
    private Texture fifthQuestDemon2IdleTex;
    private Texture fifthQuestDemon2WalkTex;
    private Texture fifthQuestDemon2AttackTex;
    private Texture fifthQuestDemon2HurtTex;
    private Texture fifthQuestDemon2DeadTex;
    private Texture fifthQuestDemon3IdleTex;
    private Texture fifthQuestDemon3WalkTex;
    private Texture fifthQuestDemon3AttackTex;
    private Texture fifthQuestDemon3HurtTex;
    private Texture fifthQuestDemon3DeadTex;
    private Texture chestIdleTex;
    private Texture chestOpenTex;
    private Sound attack1Sfx;
    private Sound attack3Sfx;
    private Sound attackSpecialSfx;
    private Sound batHurtSfx;
    private Sound evilWizardAttack1Sfx;
    private Sound evilWizardAttack2Sfx;
    private Sound golem1ExplosionSfx;
    private Sound indoorFootstepLoopSfx;
    private Sound typewriterLoopSfx;
    private Sound fifthQuestVillagerScreamSfx;
    private Sound demonFirstInteractionSfx;
    private Sound demonNeutral2Sfx;
    private final Sound[] fatherNeutralDialogueSfx = new Sound[3];
    private Sound fatherHappyDialogueSfx;
    private Sound fatherSadDialogueSfx;
    private final Sound[] bakeryNeutralDialogueSfx = new Sound[3];
    private Sound bakeryWorryDialogueSfx;
    private final Sound[] scientistNeutralDialogueSfx = new Sound[3];
    private Sound scientistConfuseDialogueSfx;
    private Sound scientistSadDialogueSfx;
    private final Sound[] zombieDialogueSfx = new Sound[2];
    private Sound activeDialogueVoiceSfx;
    private Music villageMusic;
    private Music sadVillageMusic;
    private Music villageCombatMusic;
    private Music castleMusic;
    private Music demonBattleMusic;
    private Music sixthQuestEndingMusic;
    private Music evilWizardPreExplosionSfx;
    private ShaderProgram saturationShader;
    private ShaderProgram lightAreaMaskShader;
    private ShaderProgram screenBlurShader;
    private FrameBuffer questBlurFrameBuffer;
    private TextureRegion questBlurFrameRegion;
    private int questBlurBufferWidth = 0;
    private int questBlurBufferHeight = 0;
    private float questBlurProgress = 0f;
    private float villageMusicFadeProgress = 0f;
    private float demonBattleMusicFadeProgress = 1f;
    private float sixthQuestEndingMusicFadeProgress = 0f;
    private float dashAttackAbilityCooldownTimer = 0f;
    private float spatialDashAbilityCooldownTimer = 0f;
    private long indoorFootstepLoopId = -1L;
    private long typewriterLoopSfxId = -1L;
    private long activeDialogueVoiceSfxId = -1L;
    private float typewriterLoopSfxFadeTimer = 0f;
    private int fatherNeutralDialogueSfxIndex = 0;
    private int bakeryNeutralDialogueSfxIndex = 0;
    private int scientistNeutralDialogueSfxIndex = 0;
    private int demonDialogueSfxIndex = 0;
    private int zombieDialogueSfxIndex = 0;
    private boolean villageMusicPlaying = false;
    private boolean sadVillageMusicPlaying = false;
    private boolean villageCombatMusicPlaying = false;
    private boolean villageMusicSuppressedForCombat = false;
    private boolean castleMusicPlaying = false;
    private boolean demonBattleMusicPlaying = false;
    private boolean sixthQuestEndingMusicPlaying = false;
    private EndingMusicTransitionPhase endingMusicTransitionPhase = EndingMusicTransitionPhase.NONE;
    private boolean sixthQuestEndingVideoSequenceActive = false;
    private boolean sixthQuestEndingVideoTriggered = false;
    private float sixthQuestEndingVideoSequenceTimer = 0f;
    private Texture firstQuestPanelTex;
    private Texture firstQuestFatherDialogueBoxTex;
    private Texture firstQuestItemTex;
    private Texture secondQuestFragmentTex;
    private Texture fourthQuestCondensedCoreTex;
    private Texture secondQuestArchiveNotesTex;
    private Texture thirdQuestFatherVillageTex;
    private Texture sixthQuestEndingScientistTex;
    private TextureRegion[][] thirdQuestFatherVillageTiles;
    private BitmapFont firstQuestTitleFont;
    private BitmapFont firstQuestBodyFont;
    private BitmapFont firstQuestHintFont;
    private GlyphLayout firstQuestLayout;
    private Animation<TextureRegion> firstQuestCafeNpcIdleAnim;
    private Animation<TextureRegion> fifthQuestAdultManIdleAnim;
    private Animation<TextureRegion> fifthQuestOldWomanIdleAnim;
    private Animation<TextureRegion> sixthQuestEndingScientistAnim;
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
    private Animation<TextureRegion> playerDeathAnim;
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
    private Animation<TextureRegion> bossArenaPhase2SkillStartAnim;
    private Animation<TextureRegion> bossArenaPhase2SkillLoopAnim;
    private Animation<TextureRegion> bossArenaPhase2SkillEndAnim;
    private Animation<TextureRegion> bossArenaPhase2ExplosionAnim;
    private Animation<TextureRegion> fifthQuestDemon1IdleAnim;
    private Animation<TextureRegion> fifthQuestDemon1WalkAnim;
    private Animation<TextureRegion> fifthQuestDemon1AttackAnim;
    private Animation<TextureRegion> fifthQuestDemon1HurtAnim;
    private Animation<TextureRegion> fifthQuestDemon1DeadAnim;
    private Animation<TextureRegion> fifthQuestDemon2IdleAnim;
    private Animation<TextureRegion> fifthQuestDemon2WalkAnim;
    private Animation<TextureRegion> fifthQuestDemon2AttackAnim;
    private Animation<TextureRegion> fifthQuestDemon2HurtAnim;
    private Animation<TextureRegion> fifthQuestDemon2DeadAnim;
    private Animation<TextureRegion> fifthQuestDemon3IdleAnim;
    private Animation<TextureRegion> fifthQuestDemon3WalkAnim;
    private Animation<TextureRegion> fifthQuestDemon3AttackAnim;
    private Animation<TextureRegion> fifthQuestDemon3HurtAnim;
    private Animation<TextureRegion> fifthQuestDemon3DeadAnim;
    private Animation<TextureRegion> chestIdleAnim;
    private Animation<TextureRegion> chestOpenAnim;
    private Animation<TextureRegion> activeAttackAnim;
    private final WorldState world = new WorldState();
    private final PlayerState player = new PlayerState();
    private final PixelRpgGame game;
    private final SaveGameData initialSaveData;
    private final String initialMapId;
    private final String initialSpawnLayer;
    private final String characterName;
    private final String activeSaveSlotId;
    private final String activeSaveDisplayName;
    private int firstQuestStage;
    private int secondQuestStage;
    private int thirdQuestStage;
    private int fourthQuestStage;
    private int fifthQuestStage;
    private int sixthQuestStage;
    private int progressionLevelCap;
    private boolean scientistProgressionUnlocked;
    private boolean thirdQuestAvailable;
    private boolean gameCompleted;
    private boolean firstQuestDialogueActive = false;
    private boolean firstQuestItemCardVisible = false;
    private int firstQuestDialogueLineIndex = 0;
    private QuestDialogueId activeQuestDialogueId = QuestDialogueId.NONE;
    private QuestItemCardId activeQuestItemCardId = QuestItemCardId.NONE;
    private String[] activeQuestDialogueSpeakers = new String[0];
    private String[] activeQuestDialogueLines = new String[0];
    private float activeQuestDialogueTypewriterProgress = 0f;
    private int activeQuestDialogueVisibleCharacterCount = 0;
    private String activeQuestItemTitle = "";
    private String activeQuestItemDescription = "";
    private Texture activeQuestItemTexture;
    private float activeQuestItemTitleScale = 1f;
    private float activeQuestItemImageScaleMultiplier = 1f;
    private float activeQuestItemTitleOffsetY = 0f;
    private float activeQuestItemDescriptionOffsetY = 0f;
    private float activeQuestItemHintOffsetY = 0f;
    private float firstQuestMarkerTimer = 0f;
    private float firstQuestCafeNpcStateTime = 0f;
    private boolean sixthQuestEndingNpcFadeActive = false;
    private float sixthQuestEndingNpcFadeTimer = 0f;
    private float questGuidanceSuppressTimer = 0f;
    private final Rectangle thirdQuestTrainingZoneBounds = new Rectangle();
    private final Rectangle thirdQuestFatherTrainingBounds = new Rectangle();
    private final Rectangle thirdQuestAttackDummyBounds = new Rectangle();
    private final Rectangle thirdQuestSparZoneBounds = new Rectangle();
    private final Vector2[] thirdQuestMovementMarkers = {
            new Vector2(),
            new Vector2(),
            new Vector2()
    };
    private final boolean[] thirdQuestMovementMarkerReached = new boolean[3];
    private boolean thirdQuestTrainingLayoutReady = false;
    private int thirdQuestMovementMarkersReached = 0;
    private int thirdQuestAttackDummyHitCount = 0;
    private boolean thirdQuestAttackDummyBroken = false;
    private boolean thirdQuestDummyHitRegistered = false;
    private boolean thirdQuestTutorialEnemiesSpawned = false;
    private boolean thirdQuestSparActive = false;
    private boolean thirdQuestFatherHitRegistered = false;
    private float thirdQuestFatherSparHealth = THIRD_QUEST_SPAR_FATHER_MAX_HEALTH;
    private float thirdQuestFatherSparAttackCooldownTimer = 0f;
    private float thirdQuestFatherSparHitCooldownTimer = 0f;
    private float thirdQuestFatherSparElapsed = 0f;
    private float thirdQuestFatherSparCalloutTimer = 0f;
    private int thirdQuestFatherSparCalloutIndex = 0;
    private int thirdQuestFatherSparHitsTaken = 0;
    private final Rectangle fourthQuestCafeSiteBounds = new Rectangle();
    private final Rectangle fourthQuestCenterSiteBounds = new Rectangle();
    private final Rectangle fourthQuestEdgeSiteBounds = new Rectangle();
    private final Rectangle fourthQuestBreachSiteBounds = new Rectangle();
    private final Rectangle fourthQuestArrayConsoleBounds = new Rectangle();
    private final Rectangle[] fourthQuestArrayNodeBounds = {
            new Rectangle(),
            new Rectangle(),
            new Rectangle()
    };
    private final Rectangle fifthQuestRuinsIntroBounds = new Rectangle();
    private final Rectangle fifthQuestIntroTriggerBounds = new Rectangle();
    private final Rectangle fifthQuestOuterRuinsBounds = new Rectangle();
    private final Rectangle fifthQuestMidRuinsBounds = new Rectangle();
    private final Rectangle fifthQuestFightingAreaBounds = new Rectangle();
    private final Rectangle fifthQuestPortalBounds = new Rectangle();
    private final Rectangle[] fifthQuestAnchorBounds = {
            new Rectangle(),
            new Rectangle(),
            new Rectangle()
    };
    private final Rectangle[] fifthQuestIntroCameraBounds = {
            new Rectangle(),
            new Rectangle(),
            new Rectangle()
    };
    private final Rectangle fifthQuestPortalDialogueFatherBounds = new Rectangle();
    private final Rectangle fifthQuestPortalDialogueScientistBounds = new Rectangle();
    private final Rectangle fifthQuestPortalDialogueCombinedBounds = new Rectangle();
    private final Vector2 fifthQuestIntroCameraStart = new Vector2();
    private final Vector2 fifthQuestIntroCameraTarget = new Vector2();
    private final Rectangle sixthQuestOuterCastleBounds = new Rectangle();
    private final Rectangle sixthQuestSealABounds = new Rectangle();
    private final Rectangle sixthQuestSealAInteractBounds = new Rectangle();
    private final Rectangle sixthQuestSealBBounds = new Rectangle();
    private final Rectangle sixthQuestSealBInteractBounds = new Rectangle();
    private final Rectangle sixthQuestThroneBounds = new Rectangle();
    private final Rectangle sixthQuestBossPhase1SpawnBounds = new Rectangle();
    private final Rectangle sixthQuestFinalPhaseBounds = new Rectangle();
    private final Rectangle sixthQuestFinalPhaseExplosionBounds = new Rectangle();
    private final Rectangle sixthQuestFinalPhaseSpawnBounds = new Rectangle();
    private boolean fourthQuestVillageLayoutReady = false;
    private boolean fourthQuestFloor2LayoutReady = false;
    private boolean fourthQuestEncounterSpawned = false;
    private float fourthQuestArrayDefenseTimer = 0f;
    private int fourthQuestArrayDefenseWaveCount = 0;
    private float fourthQuestBreachCalloutTimer = 0f;
    private int fourthQuestBreachCalloutIndex = 0;
    private boolean fifthQuestVillageLayoutReady = false;
    private boolean fifthQuestEncounterSpawned = false;
    private float fifthQuestCommanderCalloutTimer = 0f;
    private int fifthQuestCommanderCalloutIndex = 0;
    private float fifthQuestLabVillagersFadeTimer = 0f;
    private boolean fifthQuestLabVillagersVisibleLastFrame = false;
    private boolean fifthQuestRuinsIntroTriggered = false;
    private FifthQuestIntroCameraPhase fifthQuestIntroCameraPhase = FifthQuestIntroCameraPhase.NONE;
    private float fifthQuestIntroCameraTimer = 0f;
    private float fifthQuestIntroCameraStartZoom = DEFAULT_ZOOM;
    private boolean fifthQuestFirstEncounterIntroPlayed = false;
    private boolean fifthQuestFirstEncounterIntroActive = false;
    private float fifthQuestFirstEncounterIntroTimer = 0f;
    private int fifthQuestFirstEncounterIntroEncounterId = FIFTH_QUEST_ENCOUNTER_NONE;
    private final Vector2 fifthQuestFirstEncounterIntroFocus = new Vector2();
    private float fifthQuestZombieSfxCooldownTimer = 0f;
    private float fifthQuestPortalDialogueNpcFadeTimer = 0f;
    private boolean fifthQuestPortalDialogueNpcVisibleLastFrame = false;
    private boolean sixthQuestCastleLayoutReady = false;
    private boolean sixthQuestFinalArenaLayoutReady = false;
    private boolean sixthQuestEncounterSpawned = false;
    private float sixthQuestBossCalloutTimer = 0f;
    private int sixthQuestBossCalloutIndex = 0;
    private boolean sixthQuestLowHealthExchangePlayed = false;
    private PortalActivationPhase sixthQuestPortalActivationPhase = PortalActivationPhase.NONE;
    private PortalChoiceResult sixthQuestPortalChoiceResult = PortalChoiceResult.NONE;
    private float sixthQuestPortalActivationStateTime = 0f;
    private float sixthQuestPortalActivationLoopTimer = 0f;
    private boolean sixthQuestPortalChoiceVisible = false;
    private int sixthQuestPortalChoiceIndex = 0;
    private boolean fifthQuestDeathChoicePending = false;
    private boolean fifthQuestDeathChoiceVisible = false;
    private int fifthQuestDeathChoiceIndex = 1;
    private final Vector2 sixthQuestPortalEffectStartPosition = new Vector2();
    private final Vector2 sixthQuestPortalEffectTargetPosition = new Vector2();
    private final Vector2 sixthQuestPortalEffectPosition = new Vector2();
    private final Rectangle sixthQuestPortalChoiceYesBounds = new Rectangle();
    private final Rectangle sixthQuestPortalChoiceNoBounds = new Rectangle();
    private final Vector3 sixthQuestPortalChoiceUiPoint = new Vector3();
    private final Rectangle fifthQuestDeathChoiceQuestBounds = new Rectangle();
    private final Rectangle fifthQuestDeathChoiceFightBounds = new Rectangle();
    private final Vector3 fifthQuestDeathChoiceUiPoint = new Vector3();
    private boolean infiniteHealthEnabled = false;
    private final Vector3 questMarkerProjection = new Vector3();
    private final Vector2 dialogueCameraFocusPoint = new Vector2();
    private final Rectangle dialogueHighlightWorldBounds = new Rectangle();
    private final Rectangle dialogueHighlightScreenBounds = new Rectangle();
    private final Rectangle dialogueHighlightCombinedScreenBounds = new Rectangle();
    private final Vector3 dialogueHighlightProjectionMin = new Vector3();
    private final Vector3 dialogueHighlightProjectionMax = new Vector3();
    private final EarClippingTriangulator triangulator = new EarClippingTriangulator();
    private final Polygon playerCollisionPolygon = new Polygon();
    private final float[] playerCollisionVertices = new float[8];
    private DayNightCycleManager dayNightCycle;
    private MapTransitionPhase mapTransitionPhase = MapTransitionPhase.NONE;
    private float mapTransitionTimer = 0f;
    private float screenFadeAlpha = 1f;
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
    private final Array<FifthQuestDemonInstance> fifthQuestDemons = new Array<>();
    private final Array<Texture> bossArenaPhase2DemonIdleFrameTextures = new Array<>();
    private final Array<Texture> bossArenaPhase2DemonWalkFrameTextures = new Array<>();
    private final Array<Texture> bossArenaPhase2DemonAttackFrameTextures = new Array<>();
    private final Array<Texture> bossArenaPhase2DemonHurtFrameTextures = new Array<>();
    private final Array<Texture> bossArenaPhase2SkillStartFrameTextures = new Array<>();
    private final Array<Texture> bossArenaPhase2SkillLoopFrameTextures = new Array<>();
    private final Array<Texture> bossArenaPhase2SkillEndFrameTextures = new Array<>();
    private final Array<Texture> bossArenaPhase2ExplosionFrameTextures = new Array<>();
    private final Array<BossArenaPhase2ExplosionInstance> bossArenaPhase2Explosions = new Array<>();
    private final Array<Texture> firstQuestCafeNpcFrameTextures = new Array<>();
    private final Array<Texture> fifthQuestAdultManFrameTextures = new Array<>();
    private final Array<Texture> fifthQuestOldWomanFrameTextures = new Array<>();
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
    private final Array<MapLayer> room3DoorAnimationFrames = new Array<>();
    private MapLayer room3DoorTriggerLayer;
    private final Rectangle room3DoorTriggerBounds = new Rectangle();
    private final Rectangle room3DoorClosedCollisionBounds = new Rectangle();
    private final Array<Rectangle> room3DoorTriggerZoneBounds = new Array<>();
    private int room3DoorAnimationFrameIndex = 0;
    private float room3DoorAnimationTimer = 0f;
    private float room3DoorPauseTimer = 0f;
    private boolean room3DoorClosing = false;
    private boolean room3DoorClosed = false;
    private boolean room3DoorCollisionEnabled = false;
    private boolean room3DoorTriggered = false;
    private boolean room3DoorTriggerPrimed = false;
    private boolean wasPlayerWithinRoom3DoorTrigger = false;
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
    private BossArenaFinalIntroPhase bossArenaFinalIntroPhase = BossArenaFinalIntroPhase.NONE;
    private float bossArenaFinalIntroTimer = 0f;
    private final Vector2 bossArenaFinalIntroCameraTarget = new Vector2();
    private float bossArenaHudRevealTimer = 0f;
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
    private float autoSaveTimer = 0f;
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
    private float playerDeathStateTime = 0f;
    private boolean playerDead = false;
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

    public GameScreen() {
        this(resolveGameInstance(), null, null, null, null);
    }

    public GameScreen(SaveGameData initialSaveData) {
        this(resolveGameInstance(), initialSaveData, null, null, null);
    }

    public GameScreen(PixelRpgGame game, SaveGameData initialSaveData) {
        this(game, initialSaveData, null, null, null);
    }

    public GameScreen(String initialMapId, String initialSpawnLayer) {
        this(resolveGameInstance(), null, initialMapId, initialSpawnLayer, null);
    }

    public GameScreen(PixelRpgGame game, String initialMapId, String initialSpawnLayer) {
        this(game, null, initialMapId, initialSpawnLayer, null);
    }

    public GameScreen(String initialMapId, String initialSpawnLayer, String characterName) {
        this(resolveGameInstance(), null, initialMapId, initialSpawnLayer, characterName);
    }

    public GameScreen(PixelRpgGame game, String initialMapId, String initialSpawnLayer, String characterName) {
        this(game, null, initialMapId, initialSpawnLayer, characterName);
    }

    private GameScreen(PixelRpgGame game, SaveGameData initialSaveData, String initialMapId, String initialSpawnLayer,
                       String initialCharacterName) {
        this.game = game;
        this.initialSaveData = initialSaveData;
        this.initialMapId = initialMapId;
        this.initialSpawnLayer = initialSpawnLayer;
        this.characterName = resolveCharacterName(initialSaveData, initialCharacterName);
        this.activeSaveSlotId = resolveSaveSlotId(initialSaveData, this.characterName);
        this.activeSaveDisplayName = resolveSaveDisplayName(initialSaveData, this.characterName, this.activeSaveSlotId);
        this.firstQuestStage = resolveInitialFirstQuestStage(initialSaveData);
        this.secondQuestStage = resolveInitialSecondQuestStage(initialSaveData);
        this.thirdQuestStage = resolveInitialThirdQuestStage(initialSaveData);
        this.fourthQuestStage = resolveInitialFourthQuestStage(initialSaveData);
        this.fifthQuestStage = resolveInitialFifthQuestStage(initialSaveData);
        this.sixthQuestStage = resolveInitialSixthQuestStage(initialSaveData);
        this.progressionLevelCap = resolveInitialProgressionLevelCap(initialSaveData);
        this.scientistProgressionUnlocked = resolveInitialScientistProgressionUnlocked(initialSaveData);
        this.thirdQuestAvailable = resolveInitialThirdQuestAvailable(initialSaveData);
        this.gameCompleted = resolveInitialGameCompleted(initialSaveData);
    }

    private static PixelRpgGame resolveGameInstance() {
        if (Gdx.app == null) {
            return null;
        }
        return Gdx.app.getApplicationListener() instanceof PixelRpgGame pixelRpgGame
                ? pixelRpgGame
                : null;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 960, 640); // standard LibGDX Y-up
        camera.zoom = DEFAULT_ZOOM; // locked zoom
        uiCamera = new OrthographicCamera();
        uiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        screenFadeAlpha = 1f;

        batch = new SpriteBatch();
        hudRenderer = new HudRenderer(uiCamera, settingsService());
        hudRenderer.loadAssets();
        loadFirstQuestAssets();
        loadFifthQuestAssets();
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
        abilityCooldownCircleTex = createCooldownCircleTexture(ABILITY_COOLDOWN_CIRCLE_TEXTURE_SIZE);

        loadMapCatalog();
        loadMap(world.activeMapId, initialSpawnLayer);
        applyLoadedSaveIfPresent();
        centerCameraOnPlayer();
        writeAutosave();
        installInputProcessor();
    }

    @Override
    public void render(float delta) {
        if (screenFadeAlpha > 0f) {
            screenFadeAlpha = Math.max(0f, screenFadeAlpha - delta / SCREEN_FADE_DURATION_SECONDS);
        }
        applyRuntimeAudioSettings();
        updateVillageMusic(delta);
        updateCastleMusic();
        updateVillageCombatMusic();
        updateDemonBattleMusic();
        updateSixthQuestEndingMusic(delta);
        clearCameraShakeOffsetFromCameraPosition();
        if (hudRenderer != null && hudRenderer.isMainMenuVisible()) {
            if (handleMainMenuInput()) {
                stopIndoorFootstepLoop();
                return;
            }
            if (hudRenderer != null && hudRenderer.isMainMenuVisible()) {
                player.moving = false;
                updateIndoorFootstepLoop();
                centerCameraOnPlayer();
                hudRenderer.update(delta);
                updateQuestBlur(delta);

                if (world.mapRenderer == null) {
                    return;
                }

                float nightBlend = currentNightBlend();
                float worldSaturation = lerp(1f, NIGHT_WORLD_SATURATION, nightBlend);
                int[] backgroundRenderLayers = activeBackgroundRenderLayers();
                int[] foregroundRenderLayers = activeForegroundRenderLayers();
                renderWorldWithOptionalQuestBlur(nightBlend, worldSaturation, backgroundRenderLayers, foregroundRenderLayers);
                hudRenderer.draw(
                        batch,
                        player.currentHealth,
                        player.maxHealth,
                        player.currentMana,
                        player.maxMana,
                        player.playerLevel,
                        currentMainMenuQuestTitle(),
                        currentMainMenuQuestObjective(),
                        currentMainMenuRegionName(),
                        shouldShowHudQuestPanel(),
                        currentBossArenaHudHealth(),
                        currentBossArenaHudMaxHealth(),
                        currentBossArenaHudAlpha());
                drawFirstQuestUi();
                drawAbilityCooldownUi();
                drawMapTransitionOverlay();
                drawScreenFadeOverlay();
                drawBossArenaTopLetterboxOverlay();
                return;
            }
        }
        if (postDashMovementLockTimer > 0f) {
            postDashMovementLockTimer = Math.max(0f, postDashMovementLockTimer - delta);
        }
        updatePlayerHazardHitReaction(delta);
        updatePlayerDeath(delta);
        player.stateTime += delta;
        updateBats(delta);
        updateEvilWizards(delta);
        updateGolem1s(delta);
        updateBossArenaPhase2Demons(delta);
        updateBossArenaPhase2Explosions(delta);
        updateFifthQuestDemons(delta);
        updateRewardChests(delta);
        updateEvilWizardAttackHazard(delta);
        updateDayNightCycle(delta);
        updateMapTransition(delta);
        updateEntranceDoorAnimation(delta);
        updateRoom1DoorAnimation(delta);
        updateRoom3DoorAnimation(delta);
        updateFirstBossDoorAnimation(delta);
        updateBoss1DefeatDoorAnimation(delta);
        updateBossFightZoneDarkMode(delta);
        updateGolemFightZoneDarkMode(delta);
        updateBoss1IntroSequence(delta);
        updateBossArenaFinalIntroSequence(delta);
        updateFifthQuestIntroCameraSequence(delta);
        updateFifthQuestFirstEncounterIntro(delta);
        updateBossArenaHudReveal(delta);
        if (isMapTransitionActive()
                || isEntranceDoorInputBlocked()
                || isRoom1DoorInputBlocked()
                || isRoom3DoorInputBlocked()
                || isFirstBossDoorInputBlocked()
                || isBoss1IntroActive()
                || isBossArenaFinalIntroActive()
                || isFifthQuestIntroCameraActive()
                || isFifthQuestFirstEncounterIntroActive()
                || isPlayerHazardHitReactionActive()
                || isPlayerDead()) {
            if (!isBoss1IntroStepDownActive() && !isBossArenaFinalIntroAutoWalkActive()) {
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
        updateFifthQuestDemonHurtFromPlayer();
        updateTorchAnimation(delta);
        updateNpcAnimation(delta);
        updatePlayerStamina(delta);
        updatePlayerHealth(delta);
        updatePlayerAbilityCooldowns(delta);
        updateFirstQuest(delta);
        updateFifthQuestLabVillagers(delta);
        updateQuestDialogueTypewriter(delta);
        updateTypewriterLoopSfx(delta);
        updateIndoorFootstepLoop();
        hudRenderer.update(delta);
        updateQuestBlur(delta);
        updateBossArenaPhase2AttackZoom(delta);
        updateDialogueCamera(delta);
        updateAutosave(delta);
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
        hudRenderer.draw(
                batch,
                player.currentHealth,
                player.maxHealth,
                player.currentMana,
                player.maxMana,
                player.playerLevel,
                currentMainMenuQuestTitle(),
                currentMainMenuQuestObjective(),
                currentMainMenuRegionName(),
                shouldShowHudQuestPanel(),
                currentBossArenaHudHealth(),
                currentBossArenaHudMaxHealth(),
                currentBossArenaHudAlpha());
        drawFirstQuestUi();
        drawAbilityCooldownUi();
        drawMapTransitionOverlay();
        drawScreenFadeOverlay();
        drawBossArenaTopLetterboxOverlay();
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
        stopActiveDialogueVoiceSfx();
        stopTypewriterLoopSfxImmediately();
        stopIndoorFootstepLoop();
        stopVillageMusic();
        stopSadVillageMusic();
        stopVillageCombatMusic();
        stopDemonBattleMusic();
        stopSixthQuestEndingMusic();
        writeAutosave();
        restoreInputProcessor();
    }

    @Override
    public void dispose() {
        stopActiveDialogueVoiceSfx();
        stopTypewriterLoopSfxImmediately();
        stopIndoorFootstepLoop();
        writeAutosave();
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
        if (abilityCooldownCircleTex != null) abilityCooldownCircleTex.dispose();
        if (jumpUpAttackTex != null) jumpUpAttackTex.dispose();
        if (jumpDownAttackTex != null) jumpDownAttackTex.dispose();
        if (idleUpAttackTex != null) idleUpAttackTex.dispose();
        if (playerHitTex != null) playerHitTex.dispose();
        if (playerDeathTex != null) playerDeathTex.dispose();
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
        if (fifthQuestDemon1IdleTex != null) fifthQuestDemon1IdleTex.dispose();
        if (fifthQuestDemon1WalkTex != null) fifthQuestDemon1WalkTex.dispose();
        if (fifthQuestDemon1AttackTex != null) fifthQuestDemon1AttackTex.dispose();
        if (fifthQuestDemon1HurtTex != null) fifthQuestDemon1HurtTex.dispose();
        if (fifthQuestDemon1DeadTex != null) fifthQuestDemon1DeadTex.dispose();
        if (fifthQuestDemon2IdleTex != null) fifthQuestDemon2IdleTex.dispose();
        if (fifthQuestDemon2WalkTex != null) fifthQuestDemon2WalkTex.dispose();
        if (fifthQuestDemon2AttackTex != null) fifthQuestDemon2AttackTex.dispose();
        if (fifthQuestDemon2HurtTex != null) fifthQuestDemon2HurtTex.dispose();
        if (fifthQuestDemon2DeadTex != null) fifthQuestDemon2DeadTex.dispose();
        if (fifthQuestDemon3IdleTex != null) fifthQuestDemon3IdleTex.dispose();
        if (fifthQuestDemon3WalkTex != null) fifthQuestDemon3WalkTex.dispose();
        if (fifthQuestDemon3AttackTex != null) fifthQuestDemon3AttackTex.dispose();
        if (fifthQuestDemon3HurtTex != null) fifthQuestDemon3HurtTex.dispose();
        if (fifthQuestDemon3DeadTex != null) fifthQuestDemon3DeadTex.dispose();
        if (chestIdleTex != null) chestIdleTex.dispose();
        if (chestOpenTex != null) chestOpenTex.dispose();
        if (attack1Sfx != null) attack1Sfx.dispose();
        if (attack3Sfx != null) attack3Sfx.dispose();
        if (attackSpecialSfx != null) attackSpecialSfx.dispose();
        if (batHurtSfx != null) batHurtSfx.dispose();
        if (evilWizardAttack1Sfx != null) evilWizardAttack1Sfx.dispose();
        if (evilWizardAttack2Sfx != null) evilWizardAttack2Sfx.dispose();
        if (golem1ExplosionSfx != null) golem1ExplosionSfx.dispose();
        if (indoorFootstepLoopSfx != null) indoorFootstepLoopSfx.dispose();
        if (typewriterLoopSfx != null) typewriterLoopSfx.dispose();
        disposeNpcDialogueVoiceSfx();
        if (villageMusic != null) villageMusic.dispose();
        if (sadVillageMusic != null) sadVillageMusic.dispose();
        if (villageCombatMusic != null) villageCombatMusic.dispose();
        if (castleMusic != null) castleMusic.dispose();
        if (demonBattleMusic != null) demonBattleMusic.dispose();
        if (sixthQuestEndingMusic != null) sixthQuestEndingMusic.dispose();
        if (evilWizardPreExplosionSfx != null) evilWizardPreExplosionSfx.dispose();
        if (saturationShader != null) saturationShader.dispose();
        if (lightAreaMaskShader != null) lightAreaMaskShader.dispose();
        if (screenBlurShader != null) screenBlurShader.dispose();
        disposeQuestBlurFrameBuffer();
        if (firstQuestPanelTex != null) firstQuestPanelTex.dispose();
        if (firstQuestFatherDialogueBoxTex != null) firstQuestFatherDialogueBoxTex.dispose();
        if (firstQuestItemTex != null) firstQuestItemTex.dispose();
        if (secondQuestFragmentTex != null) secondQuestFragmentTex.dispose();
        if (fourthQuestCondensedCoreTex != null && fourthQuestCondensedCoreTex != secondQuestFragmentTex) fourthQuestCondensedCoreTex.dispose();
        if (secondQuestArchiveNotesTex != null) secondQuestArchiveNotesTex.dispose();
        if (thirdQuestFatherVillageTex != null) thirdQuestFatherVillageTex.dispose();
        if (sixthQuestEndingScientistTex != null) sixthQuestEndingScientistTex.dispose();
        if (firstQuestTitleFont != null) firstQuestTitleFont.dispose();
        if (firstQuestBodyFont != null) firstQuestBodyFont.dispose();
        if (firstQuestHintFont != null) firstQuestHintFont.dispose();
        for (int i = 0; i < firstQuestCafeNpcFrameTextures.size; i++) {
            Texture frameTexture = firstQuestCafeNpcFrameTextures.get(i);
            if (frameTexture != null) {
                frameTexture.dispose();
            }
        }
        firstQuestCafeNpcFrameTextures.clear();
        for (int i = 0; i < fifthQuestAdultManFrameTextures.size; i++) {
            Texture frameTexture = fifthQuestAdultManFrameTextures.get(i);
            if (frameTexture != null) {
                frameTexture.dispose();
            }
        }
        fifthQuestAdultManFrameTextures.clear();
        for (int i = 0; i < fifthQuestOldWomanFrameTextures.size; i++) {
            Texture frameTexture = fifthQuestOldWomanFrameTextures.get(i);
            if (frameTexture != null) {
                frameTexture.dispose();
            }
        }
        fifthQuestOldWomanFrameTextures.clear();
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

    private void loadFirstQuestAssets() {
        firstQuestPanelTex = loadOptionalTexture(FIRST_QUEST_PANEL_TEXTURE_PATH);
        firstQuestFatherDialogueBoxTex = loadOptionalTexture(FIRST_QUEST_FATHER_DIALOGUE_BOX_TEXTURE_PATH);
        firstQuestItemTex = loadOptionalTexture(FIRST_QUEST_ITEM_TEXTURE_PATH);
        secondQuestFragmentTex = loadOptionalTexture(SECOND_QUEST_FRAGMENT_TEXTURE_PATH);
        thirdQuestFatherVillageTex = loadOptionalTexture(THIRD_QUEST_FATHER_TEXTURE_PATH);
        sixthQuestEndingScientistTex = loadOptionalTexture(SIXTH_QUEST_ENDING_SCIENTIST_TEXTURE_PATH);
        if (secondQuestFragmentTex == null) {
            secondQuestFragmentTex = createScorchedFragmentTexture();
        }
        fourthQuestCondensedCoreTex = loadOptionalTexture(FOURTH_QUEST_CONDENSED_CORE_TEXTURE_PATH);
        if (fourthQuestCondensedCoreTex == null) {
            fourthQuestCondensedCoreTex = secondQuestFragmentTex;
        }
        secondQuestArchiveNotesTex = createArchiveNotesTexture();
        firstQuestTitleFont = loadQuestFont(GUI_FONT_PATH, FIRST_QUEST_TITLE_FONT_SIZE);
        firstQuestBodyFont = loadQuestFont(GUI_FONT_PATH, FIRST_QUEST_BODY_FONT_SIZE);
        firstQuestHintFont = loadQuestFont(GUI_FONT_PATH, FIRST_QUEST_HINT_FONT_SIZE);
        villageMusic = loadOptionalAbsoluteMusic(VILLAGE_MUSIC_ABSOLUTE_PATH);
        sadVillageMusic = loadOptionalAbsoluteMusic(SAD_VILLAGE_MUSIC_ABSOLUTE_PATH);
        villageCombatMusic = loadOptionalAbsoluteMusic(VILLAGE_COMBAT_MUSIC_ABSOLUTE_PATH);
        castleMusic = loadOptionalAbsoluteMusic(CASTLE_MUSIC_ABSOLUTE_PATH);
        demonBattleMusic = loadOptionalAbsoluteMusic(DEMON_BATTLE_MUSIC_ABSOLUTE_PATH);
        sixthQuestEndingMusic = loadOptionalMusic(SIXTH_QUEST_ENDING_MUSIC_ASSET_PATH);
        firstQuestLayout = new GlyphLayout();
        firstQuestCafeNpcIdleAnim = loadOptionalAbsoluteFrameAnimation(
                FIRST_QUEST_CAFE_NPC_IDLE_DIRECTORY,
                FIRST_QUEST_CAFE_NPC_FRAME_DURATION,
                firstQuestCafeNpcFrameTextures);
        setNearestFilter(firstQuestPanelTex);
        setNearestFilter(firstQuestFatherDialogueBoxTex);
        setNearestFilter(firstQuestItemTex);
        setNearestFilter(secondQuestFragmentTex);
        if (fourthQuestCondensedCoreTex != secondQuestFragmentTex) {
            setNearestFilter(fourthQuestCondensedCoreTex);
        }
        setNearestFilter(secondQuestArchiveNotesTex);
        setNearestFilter(thirdQuestFatherVillageTex);
        setNearestFilter(sixthQuestEndingScientistTex);
        if (thirdQuestFatherVillageTex != null) {
            int tileSize = Math.max(1, Math.round(DEFAULT_TILE_SIZE));
            thirdQuestFatherVillageTiles = TextureRegion.split(thirdQuestFatherVillageTex, tileSize, tileSize);
        } else {
            thirdQuestFatherVillageTiles = null;
        }
        sixthQuestEndingScientistAnim = sixthQuestEndingScientistTex == null
                ? null
                : buildRawAnimation(
                sixthQuestEndingScientistTex,
                4,
                sixthQuestEndingScientistTex.getHeight(),
                sixthQuestEndingScientistTex.getHeight(),
                SIXTH_QUEST_ENDING_SCIENTIST_FRAME_DURATION);
    }

    private void loadFifthQuestAssets() {
        fifthQuestDemon1IdleTex = loadOptionalAbsoluteTexture(FIFTH_QUEST_DEMON_1_IDLE_TEXTURE_ABSOLUTE_PATH);
        fifthQuestDemon1WalkTex = loadOptionalAbsoluteTexture(FIFTH_QUEST_DEMON_1_WALK_TEXTURE_ABSOLUTE_PATH);
        fifthQuestDemon1AttackTex = loadOptionalAbsoluteTexture(FIFTH_QUEST_DEMON_1_ATTACK_TEXTURE_ABSOLUTE_PATH);
        fifthQuestDemon1HurtTex = loadOptionalAbsoluteTexture(FIFTH_QUEST_DEMON_1_HURT_TEXTURE_ABSOLUTE_PATH);
        fifthQuestDemon1DeadTex = loadOptionalAbsoluteTexture(FIFTH_QUEST_DEMON_1_DEAD_TEXTURE_ABSOLUTE_PATH);
        fifthQuestDemon2IdleTex = loadOptionalAbsoluteTexture(FIFTH_QUEST_DEMON_2_IDLE_TEXTURE_ABSOLUTE_PATH);
        fifthQuestDemon2WalkTex = loadOptionalAbsoluteTexture(FIFTH_QUEST_DEMON_2_WALK_TEXTURE_ABSOLUTE_PATH);
        fifthQuestDemon2AttackTex = loadOptionalAbsoluteTexture(FIFTH_QUEST_DEMON_2_ATTACK_TEXTURE_ABSOLUTE_PATH);
        fifthQuestDemon2HurtTex = loadOptionalAbsoluteTexture(FIFTH_QUEST_DEMON_2_HURT_TEXTURE_ABSOLUTE_PATH);
        fifthQuestDemon2DeadTex = loadOptionalAbsoluteTexture(FIFTH_QUEST_DEMON_2_DEAD_TEXTURE_ABSOLUTE_PATH);
        fifthQuestDemon3IdleTex = loadOptionalAbsoluteTexture(FIFTH_QUEST_DEMON_3_IDLE_TEXTURE_ABSOLUTE_PATH);
        fifthQuestDemon3WalkTex = loadOptionalAbsoluteTexture(FIFTH_QUEST_DEMON_3_WALK_TEXTURE_ABSOLUTE_PATH);
        fifthQuestDemon3AttackTex = loadOptionalAbsoluteTexture(FIFTH_QUEST_DEMON_3_ATTACK_TEXTURE_ABSOLUTE_PATH);
        fifthQuestDemon3HurtTex = loadOptionalAbsoluteTexture(FIFTH_QUEST_DEMON_3_HURT_TEXTURE_ABSOLUTE_PATH);
        fifthQuestDemon3DeadTex = loadOptionalAbsoluteTexture(FIFTH_QUEST_DEMON_3_DEAD_TEXTURE_ABSOLUTE_PATH);

        fifthQuestDemon1IdleAnim = buildOptionalStripAnimation(fifthQuestDemon1IdleTex, FIFTH_QUEST_DEMON_IDLE_FRAME_DURATION, true);
        fifthQuestDemon1WalkAnim = buildOptionalStripAnimation(fifthQuestDemon1WalkTex, FIFTH_QUEST_DEMON_WALK_FRAME_DURATION, true);
        fifthQuestDemon1AttackAnim = buildOptionalStripAnimation(fifthQuestDemon1AttackTex, FIFTH_QUEST_DEMON_ATTACK_FRAME_DURATION, false);
        fifthQuestDemon1HurtAnim = buildOptionalStripAnimation(fifthQuestDemon1HurtTex, FIFTH_QUEST_DEMON_HURT_FRAME_DURATION, false);
        fifthQuestDemon1DeadAnim = buildOptionalStripAnimation(fifthQuestDemon1DeadTex, FIFTH_QUEST_DEMON_DEAD_FRAME_DURATION, false);
        fifthQuestDemon2IdleAnim = buildOptionalStripAnimation(fifthQuestDemon2IdleTex, FIFTH_QUEST_DEMON_IDLE_FRAME_DURATION, true);
        fifthQuestDemon2WalkAnim = buildOptionalStripAnimation(fifthQuestDemon2WalkTex, FIFTH_QUEST_DEMON_WALK_FRAME_DURATION, true);
        fifthQuestDemon2AttackAnim = buildOptionalStripAnimation(fifthQuestDemon2AttackTex, FIFTH_QUEST_DEMON_ATTACK_FRAME_DURATION, false);
        fifthQuestDemon2HurtAnim = buildOptionalStripAnimation(fifthQuestDemon2HurtTex, FIFTH_QUEST_DEMON_HURT_FRAME_DURATION, false);
        fifthQuestDemon2DeadAnim = buildOptionalStripAnimation(fifthQuestDemon2DeadTex, FIFTH_QUEST_DEMON_DEAD_FRAME_DURATION, false);
        fifthQuestDemon3IdleAnim = buildOptionalStripAnimation(fifthQuestDemon3IdleTex, FIFTH_QUEST_DEMON_IDLE_FRAME_DURATION, true);
        fifthQuestDemon3WalkAnim = buildOptionalStripAnimation(fifthQuestDemon3WalkTex, FIFTH_QUEST_DEMON_WALK_FRAME_DURATION, true);
        fifthQuestDemon3AttackAnim = buildOptionalStripAnimation(fifthQuestDemon3AttackTex, FIFTH_QUEST_DEMON_ATTACK_FRAME_DURATION, false);
        fifthQuestDemon3HurtAnim = buildOptionalStripAnimation(fifthQuestDemon3HurtTex, FIFTH_QUEST_DEMON_HURT_FRAME_DURATION, false);
        fifthQuestDemon3DeadAnim = buildOptionalStripAnimation(fifthQuestDemon3DeadTex, FIFTH_QUEST_DEMON_DEAD_FRAME_DURATION, false);
        fifthQuestAdultManIdleAnim = loadOptionalAbsoluteFrameAnimation(
                FIFTH_QUEST_ADULT_MAN_IDLE_DIRECTORY,
                FIRST_QUEST_CAFE_NPC_FRAME_DURATION,
                fifthQuestAdultManFrameTextures);
        fifthQuestOldWomanIdleAnim = loadOptionalAbsoluteFrameAnimation(
                FIFTH_QUEST_OLD_WOMAN_IDLE_DIRECTORY,
                FIRST_QUEST_CAFE_NPC_FRAME_DURATION,
                fifthQuestOldWomanFrameTextures);
    }

    private Animation<TextureRegion> buildOptionalStripAnimation(Texture texture, float frameDuration, boolean loop) {
        if (texture == null) {
            return null;
        }
        setNearestFilter(texture);
        int frameSize = Math.max(1, texture.getHeight());
        int frameCount = Math.max(1, texture.getWidth() / frameSize);
        Animation<TextureRegion> animation = buildRawAnimation(texture, frameCount, frameSize, frameSize, frameDuration);
        animation.setPlayMode(loop ? Animation.PlayMode.LOOP : Animation.PlayMode.NORMAL);
        return animation;
    }

    private void loadMapCatalog() {
        world.mapCatalog = MapCatalog.load(MAP_CATALOG_PATH, DEFAULT_MAP_PADDING);
        world.activeMap = resolveInitialMap();
        world.activeMapId = world.activeMap.getId();
    }

    private MapDefinition resolveInitialMap() {
        if (initialSaveData != null && initialSaveData.mapId != null) {
            MapDefinition savedMap = world.mapCatalog.getById(initialSaveData.mapId);
            if (savedMap != null) {
                return savedMap;
            }
        }
        if (initialMapId != null && !initialMapId.isBlank()) {
            MapDefinition configuredMap = world.mapCatalog.getById(initialMapId);
            if (configuredMap != null) {
                return configuredMap;
            }
        }
        return world.mapCatalog.getDefaultMap();
    }

    private int resolveInitialFirstQuestStage(SaveGameData saveData) {
        if (saveData != null) {
            return clampFirstQuestStage(saveData.firstQuestStage);
        }
        return FIRST_QUEST_TALK_TO_FATHER;
    }

    private int resolveInitialSecondQuestStage(SaveGameData saveData) {
        if (saveData != null) {
            if (saveData.sixthQuestStage != null && clampSixthQuestStage(saveData.sixthQuestStage) > SIXTH_QUEST_DISABLED) {
                return SECOND_QUEST_COMPLETE;
            }
            if (saveData.fifthQuestStage != null && clampFifthQuestStage(saveData.fifthQuestStage) > FIFTH_QUEST_DISABLED) {
                return SECOND_QUEST_COMPLETE;
            }
            if (saveData.fourthQuestStage != null && clampFourthQuestStage(saveData.fourthQuestStage) > FOURTH_QUEST_DISABLED) {
                return SECOND_QUEST_COMPLETE;
            }
            if (saveData.thirdQuestStage != null && clampThirdQuestStage(saveData.thirdQuestStage) > THIRD_QUEST_DISABLED) {
                return SECOND_QUEST_COMPLETE;
            }
            if (Boolean.TRUE.equals(saveData.thirdQuestAvailable)
                    || Boolean.TRUE.equals(saveData.scientistProgressionUnlocked)) {
                return SECOND_QUEST_COMPLETE;
            }
            if (saveData.secondQuestStage != null) {
                return clampSecondQuestStage(saveData.secondQuestStage);
            }
            if (clampFirstQuestStage(saveData.firstQuestStage) >= FIRST_QUEST_COMPLETE) {
                return SECOND_QUEST_TALK_TO_CAFE_OWNER;
            }
        }
        return SECOND_QUEST_DISABLED;
    }

    private int resolveInitialThirdQuestStage(SaveGameData saveData) {
        if (saveData != null) {
            if (saveData.sixthQuestStage != null && clampSixthQuestStage(saveData.sixthQuestStage) > SIXTH_QUEST_DISABLED) {
                return THIRD_QUEST_COMPLETE;
            }
            if (saveData.fifthQuestStage != null && clampFifthQuestStage(saveData.fifthQuestStage) > FIFTH_QUEST_DISABLED) {
                return THIRD_QUEST_COMPLETE;
            }
            if (saveData.fourthQuestStage != null && clampFourthQuestStage(saveData.fourthQuestStage) > FOURTH_QUEST_DISABLED) {
                return THIRD_QUEST_COMPLETE;
            }
            if (saveData.thirdQuestStage != null) {
                return clampThirdQuestStage(saveData.thirdQuestStage);
            }
            if (Boolean.TRUE.equals(saveData.thirdQuestAvailable)
                    || Boolean.TRUE.equals(saveData.scientistProgressionUnlocked)) {
                return THIRD_QUEST_RETURN_TO_FATHER;
            }
        }
        return THIRD_QUEST_DISABLED;
    }

    private int resolveInitialFourthQuestStage(SaveGameData saveData) {
        if (saveData != null) {
            if (saveData.sixthQuestStage != null && clampSixthQuestStage(saveData.sixthQuestStage) > SIXTH_QUEST_DISABLED) {
                return FOURTH_QUEST_COMPLETE;
            }
            if (saveData.fifthQuestStage != null && clampFifthQuestStage(saveData.fifthQuestStage) > FIFTH_QUEST_DISABLED) {
                return FOURTH_QUEST_COMPLETE;
            }
            if (saveData.fourthQuestStage != null) {
                return clampFourthQuestStage(saveData.fourthQuestStage);
            }
            if (saveData.thirdQuestStage != null && clampThirdQuestStage(saveData.thirdQuestStage) >= THIRD_QUEST_COMPLETE) {
                return FOURTH_QUEST_REPORT_TO_SCIENTIST;
            }
        }
        return FOURTH_QUEST_DISABLED;
    }

    private int resolveInitialFifthQuestStage(SaveGameData saveData) {
        if (saveData != null) {
            if (saveData.sixthQuestStage != null && clampSixthQuestStage(saveData.sixthQuestStage) > SIXTH_QUEST_DISABLED) {
                return FIFTH_QUEST_COMPLETE;
            }
            if (saveData.fifthQuestStage != null) {
                return clampFifthQuestStage(saveData.fifthQuestStage);
            }
            if (saveData.fourthQuestStage != null && clampFourthQuestStage(saveData.fourthQuestStage) >= FOURTH_QUEST_COMPLETE) {
                return FIFTH_QUEST_GO_TO_ABANDONED_VILLAGE;
            }
        }
        return FIFTH_QUEST_DISABLED;
    }

    private int resolveInitialSixthQuestStage(SaveGameData saveData) {
        if (saveData != null) {
            if (Boolean.TRUE.equals(saveData.gameCompleted)) {
                return SIXTH_QUEST_COMPLETE;
            }
            if (saveData.sixthQuestStage != null) {
                return clampSixthQuestStage(saveData.sixthQuestStage);
            }
            if (MAP_ID_BOSS_ARENA_PHASE_2.equals(saveData.mapId)) {
                return SIXTH_QUEST_FINAL_PHASE_BATTLE;
            }
            if (MAP_ID_DEMON_CASTLE.equals(saveData.mapId)) {
                return SIXTH_QUEST_OUTER_ASSAULT;
            }
            if (saveData.fifthQuestStage != null && clampFifthQuestStage(saveData.fifthQuestStage) >= FIFTH_QUEST_COMPLETE) {
                return SIXTH_QUEST_ENTER_PORTAL;
            }
        }
        return SIXTH_QUEST_DISABLED;
    }

    private int resolveInitialProgressionLevelCap(SaveGameData saveData) {
        if (saveData != null && saveData.progressionLevelCap != null) {
            return clampProgressionLevelCap(saveData.progressionLevelCap);
        }
        if (saveData != null && saveData.fourthQuestStage != null
                && clampFourthQuestStage(saveData.fourthQuestStage) >= FOURTH_QUEST_COMPLETE) {
            return QUEST_4_LEVEL_CAP;
        }
        if (saveData != null && saveData.playerLevel > 0) {
            return clampProgressionLevelCap(saveData.playerLevel);
        }
        return QUEST_LEVEL_CAP_DEFAULT;
    }

    private boolean resolveInitialScientistProgressionUnlocked(SaveGameData saveData) {
        return saveData != null && Boolean.TRUE.equals(saveData.scientistProgressionUnlocked);
    }

    private boolean resolveInitialThirdQuestAvailable(SaveGameData saveData) {
        return saveData != null && Boolean.TRUE.equals(saveData.thirdQuestAvailable);
    }

    private boolean resolveInitialGameCompleted(SaveGameData saveData) {
        return saveData != null
                && (Boolean.TRUE.equals(saveData.gameCompleted)
                || (saveData.sixthQuestStage != null
                && clampSixthQuestStage(saveData.sixthQuestStage) >= SIXTH_QUEST_COMPLETE));
    }

    private int clampFirstQuestStage(Integer stage) {
        if (stage == null) {
            return FIRST_QUEST_DISABLED;
        }
        return Math.max(FIRST_QUEST_DISABLED, Math.min(FIRST_QUEST_COMPLETE, stage));
    }

    private int clampSecondQuestStage(Integer stage) {
        if (stage == null) {
            return SECOND_QUEST_DISABLED;
        }
        return Math.max(SECOND_QUEST_DISABLED, Math.min(SECOND_QUEST_COMPLETE, stage));
    }

    private int clampThirdQuestStage(Integer stage) {
        if (stage == null) {
            return THIRD_QUEST_DISABLED;
        }
        return Math.max(THIRD_QUEST_DISABLED, Math.min(THIRD_QUEST_COMPLETE, stage));
    }

    private int clampFourthQuestStage(Integer stage) {
        if (stage == null) {
            return FOURTH_QUEST_DISABLED;
        }
        return Math.max(FOURTH_QUEST_DISABLED, Math.min(FOURTH_QUEST_COMPLETE, stage));
    }

    private int clampFifthQuestStage(Integer stage) {
        if (stage == null) {
            return FIFTH_QUEST_DISABLED;
        }
        return Math.max(FIFTH_QUEST_DISABLED, Math.min(FIFTH_QUEST_COMPLETE, stage));
    }

    private int clampSixthQuestStage(Integer stage) {
        if (stage == null) {
            return SIXTH_QUEST_DISABLED;
        }
        return Math.max(SIXTH_QUEST_DISABLED, Math.min(SIXTH_QUEST_COMPLETE, stage));
    }

    private int clampProgressionLevelCap(int levelCap) {
        return PlayerLevelStats.clampLevel(Math.max(QUEST_LEVEL_CAP_DEFAULT, levelCap));
    }

    private void applyLoadedSaveIfPresent() {
        if (initialSaveData == null) {
            return;
        }

        int savedLevel = PlayerLevelStats.clampLevel(initialSaveData.playerLevel);
        applyPlayerLevelStats(
                savedLevel,
                scaleSavedResourceToLevel(initialSaveData.currentHealth, initialSaveData.maxHealth,
                        PlayerLevelStats.maxHealthForLevel(savedLevel)),
                scaleSavedResourceToLevel(initialSaveData.currentMana, initialSaveData.maxMana,
                        PlayerLevelStats.maxStaminaForLevel(savedLevel)));
        firstQuestStage = clampFirstQuestStage(initialSaveData.firstQuestStage);
        secondQuestStage = resolveInitialSecondQuestStage(initialSaveData);
        thirdQuestStage = resolveInitialThirdQuestStage(initialSaveData);
        fourthQuestStage = resolveInitialFourthQuestStage(initialSaveData);
        fifthQuestStage = resolveInitialFifthQuestStage(initialSaveData);
        sixthQuestStage = resolveInitialSixthQuestStage(initialSaveData);
        progressionLevelCap = resolveInitialProgressionLevelCap(initialSaveData);
        scientistProgressionUnlocked = resolveInitialScientistProgressionUnlocked(initialSaveData);
        thirdQuestAvailable = resolveInitialThirdQuestAvailable(initialSaveData);
        gameCompleted = resolveInitialGameCompleted(initialSaveData);
        if (thirdQuestStage > THIRD_QUEST_DISABLED) {
            thirdQuestAvailable = true;
        }
        if (world.activeMapId != null && world.activeMapId.equals(initialSaveData.mapId)) {
            float halfW = HITBOX_WIDTH / 2f;
            Vector2 savedBottomCenter = new Vector2(initialSaveData.playerBoundsX + halfW, initialSaveData.playerBoundsY);
            Vector2 free = findNearestFreeSpot(savedBottomCenter);
            player.bounds.set(free.x - halfW, free.y, HITBOX_WIDTH, HITBOX_HEIGHT);
        }
        player.moving = false;
        player.attacking = false;
        player.attackTimer = 0f;
        player.attackCooldownTimer = 0f;
        player.attackComboStep = 0;
        player.dashing = false;
        player.dashTimer = 0f;
        player.dashCooldownTimer = 0f;
        resetPlayerAbilityCooldowns();
        player.dashAttackActive = false;
        player.dashAttackMoveDuration = 0f;
        player.dashAttackFollowupQueued = false;
        activeAttackAnim = null;
        cameraTeleportSmoothTimer = 0f;
        postDashMovementLockTimer = 0f;
        cameraTeleportTargetActive = false;
        dashAttackPlannedPositionValid = false;
        clearDashAttackSweepState();
        applyVillagePortalVisualState();
        handleQuestMapLoadEvents();
        onPlayerSpawned();
    }

    private void updateAutosave(float delta) {
        autoSaveTimer += delta;
        if (autoSaveTimer >= AUTO_SAVE_INTERVAL_SECONDS) {
            writeAutosave();
        }
    }

    private void writeAutosave() {
        if (!canWriteAutosave()) {
            return;
        }

        SaveGameData saveData = new SaveGameData();
        saveData.slotId = activeSaveSlotId;
        saveData.displayName = activeSaveDisplayName;
        saveData.characterName = characterName;
        saveData.mapId = world.activeMapId;
        saveData.playerBoundsX = player.bounds.x;
        saveData.playerBoundsY = player.bounds.y;
        saveData.currentHealth = player.currentHealth;
        saveData.maxHealth = player.maxHealth;
        saveData.currentMana = player.currentMana;
        saveData.maxMana = player.maxMana;
        saveData.playerLevel = player.playerLevel;
        saveData.firstQuestStage = firstQuestStage;
        saveData.secondQuestStage = secondQuestStage;
        saveData.thirdQuestStage = thirdQuestStage;
        saveData.fourthQuestStage = fourthQuestStage;
        saveData.fifthQuestStage = fifthQuestStage;
        saveData.sixthQuestStage = sixthQuestStage;
        saveData.progressionLevelCap = progressionLevelCap;
        saveData.scientistProgressionUnlocked = scientistProgressionUnlocked;
        saveData.thirdQuestAvailable = thirdQuestStage > THIRD_QUEST_DISABLED || thirdQuestAvailable;
        saveData.gameCompleted = gameCompleted;
        SaveGameService.save(saveData);
        autoSaveTimer = 0f;
    }

    private void applyPlayerLevelStats(int level, float currentHealth, float currentStamina) {
        player.playerLevel = PlayerLevelStats.clampLevel(level);
        player.maxHealth = PlayerLevelStats.maxHealthForLevel(player.playerLevel);
        player.maxMana = PlayerLevelStats.maxStaminaForLevel(player.playerLevel);
        player.currentHealth = Math.min(player.maxHealth, Math.max(0f, currentHealth));
        player.currentMana = Math.min(player.maxMana, Math.max(0f, currentStamina));
    }

    private float scaleSavedResourceToLevel(float savedCurrentValue, float savedMaxValue, float levelMaxValue) {
        if (savedCurrentValue <= 0f) {
            return 0f;
        }
        if (savedMaxValue > 0f) {
            return levelMaxValue * clamp01(savedCurrentValue / savedMaxValue);
        }
        return Math.min(levelMaxValue, savedCurrentValue);
    }

    private boolean canWriteAutosave() {
        return world.activeMapId != null
                && !world.activeMapId.isBlank()
                && player.bounds.width > 0f
                && player.bounds.height > 0f
                && !isPlayerDead();
    }

    private String resolveCharacterName(SaveGameData saveData, String initialCharacterName) {
        if (saveData != null && saveData.characterName != null && !saveData.characterName.isBlank()) {
            return normalizeCharacterName(saveData.characterName);
        }
        return normalizeCharacterName(initialCharacterName);
    }

    private String resolveSaveSlotId(SaveGameData saveData, String resolvedCharacterName) {
        if (saveData != null && saveData.slotId != null && !saveData.slotId.isBlank()) {
            return saveData.slotId;
        }
        return SaveGameService.createNewSlotId(resolvedCharacterName);
    }

    private String resolveSaveDisplayName(SaveGameData saveData, String resolvedCharacterName, String resolvedSlotId) {
        if (saveData != null && saveData.displayName != null && !saveData.displayName.isBlank()) {
            return saveData.displayName;
        }
        if (resolvedCharacterName != null && !resolvedCharacterName.isBlank()) {
            return resolvedCharacterName;
        }
        return resolvedSlotId;
    }

    private String normalizeCharacterName(String rawCharacterName) {
        if (rawCharacterName == null) {
            return DEFAULT_CHARACTER_NAME;
        }
        String normalized = rawCharacterName.trim().replaceAll("\\s+", " ");
        return normalized.isEmpty() ? DEFAULT_CHARACTER_NAME : normalized;
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
        configureRoom3DoorAnimation();
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
        applyVillagePortalVisualState();
        handleQuestMapLoadEvents();
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
        thirdQuestTrainingLayoutReady = false;
        thirdQuestSparActive = false;
        fourthQuestVillageLayoutReady = false;
        fourthQuestFloor2LayoutReady = false;
        fourthQuestEncounterSpawned = false;
        fourthQuestArrayDefenseTimer = 0f;
        fourthQuestArrayDefenseWaveCount = 0;
        fourthQuestBreachCalloutTimer = 0f;
        fourthQuestBreachCalloutIndex = 0;
        fifthQuestVillageLayoutReady = false;
        fifthQuestEncounterSpawned = false;
        fifthQuestCommanderCalloutTimer = 0f;
        fifthQuestCommanderCalloutIndex = 0;
        fifthQuestLabVillagersFadeTimer = 0f;
        fifthQuestLabVillagersVisibleLastFrame = false;
        fifthQuestPortalDialogueNpcFadeTimer = 0f;
        fifthQuestPortalDialogueNpcVisibleLastFrame = false;
        fifthQuestRuinsIntroTriggered = false;
        fifthQuestIntroCameraPhase = FifthQuestIntroCameraPhase.NONE;
        fifthQuestIntroCameraTimer = 0f;
        sixthQuestCastleLayoutReady = false;
        sixthQuestFinalArenaLayoutReady = false;
        sixthQuestEncounterSpawned = false;
        sixthQuestBossCalloutTimer = 0f;
        sixthQuestBossCalloutIndex = 0;
        sixthQuestLowHealthExchangePlayed = false;
        clearSixthQuestPortalActivation();
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
        room3DoorAnimationFrames.clear();
        room3DoorTriggerLayer = null;
        room3DoorTriggerBounds.set(0f, 0f, 0f, 0f);
        room3DoorClosedCollisionBounds.set(0f, 0f, 0f, 0f);
        room3DoorTriggerZoneBounds.clear();
        room3DoorAnimationFrameIndex = 0;
        room3DoorAnimationTimer = 0f;
        room3DoorPauseTimer = 0f;
        room3DoorClosing = false;
        room3DoorClosed = false;
        room3DoorCollisionEnabled = false;
        room3DoorTriggered = false;
        room3DoorTriggerPrimed = false;
        wasPlayerWithinRoom3DoorTrigger = false;
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
        bossArenaFinalIntroPhase = BossArenaFinalIntroPhase.NONE;
        bossArenaFinalIntroTimer = 0f;
        bossArenaFinalIntroCameraTarget.setZero();
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
        clearSpawnedFifthQuestDemons();
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

    private boolean isRoom3DoorInputBlocked() {
        return room3DoorPauseTimer > 0f || room3DoorClosing;
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

    private boolean isBossArenaFinalIntroActive() {
        return bossArenaFinalIntroPhase != BossArenaFinalIntroPhase.NONE;
    }

    private boolean isBossArenaFinalIntroAutoWalkActive() {
        return bossArenaFinalIntroPhase == BossArenaFinalIntroPhase.AUTO_WALK;
    }

    private boolean isBossArenaFinalIntroFocusActive() {
        return bossArenaFinalIntroPhase == BossArenaFinalIntroPhase.FOCUS_BOSS;
    }

    private boolean isFifthQuestFirstEncounterIntroActive() {
        return fifthQuestFirstEncounterIntroActive && fifthQuestFirstEncounterIntroTimer > 0f;
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

    private boolean isPlayerDead() {
        return playerDead || player.currentHealth <= 0f;
    }

    private void updatePlayerDeath(float delta) {
        if (!isPlayerDead()) {
            return;
        }
        if (fifthQuestDeathChoiceVisible) {
            return;
        }
        if (!playerDead) {
            startPlayerDeath();
        }
        if (delta <= 0f) {
            return;
        }
        float respawnDelay = PLAYER_DEATH_ANIMATION_SECONDS;
        if (playerDeathAnim != null) {
            float animationDuration = Math.max(0f, playerDeathAnim.getAnimationDuration());
            playerDeathStateTime = Math.min(animationDuration, playerDeathStateTime + delta);
            respawnDelay = animationDuration;
        } else {
            playerDeathStateTime += delta;
        }
        if (playerDeathStateTime >= Math.max(0.1f, respawnDelay)) {
            if (fifthQuestDeathChoicePending) {
                showFifthQuestDeathChoice();
                return;
            }
            respawnPlayerAfterDeath();
        }
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

    private void applyDamageToPlayer(float damage) {
        if (player.currentHealth <= 0f || infiniteHealthEnabled) {
            return;
        }
        player.currentHealth = Math.max(0f, player.currentHealth - Math.max(0f, damage));
        if (player.currentHealth <= 0f) {
            startPlayerDeath();
        } else {
            triggerPlayerHazardHitReaction();
        }
    }

    private void applyFifthQuestDemonDamageToPlayer(float damage) {
        float safeDamage = Math.max(0f, damage);
        if (player.currentHealth > 0f
                && !infiniteHealthEnabled
                && player.currentHealth - safeDamage <= 0f
                && shouldOfferFifthQuestDeathChoice()) {
            fifthQuestDeathChoicePending = true;
        }
        applyDamageToPlayer(safeDamage);
    }

    private boolean shouldOfferFifthQuestDeathChoice() {
        if (!MAP_ID_VILLAGE.equals(world.activeMapId) || gameCompleted) {
            return false;
        }
        return switch (fifthQuestStage) {
            case FIFTH_QUEST_OUTER_RUINS_BATTLE,
                    FIFTH_QUEST_MID_RUINS_BATTLE,
                    FIFTH_QUEST_ANCHOR_1_BATTLE,
                    FIFTH_QUEST_ANCHOR_2_BATTLE,
                    FIFTH_QUEST_ANCHOR_3_BATTLE,
                    FIFTH_QUEST_PORTAL_BOSS -> true;
            default -> false;
        };
    }

    private void startPlayerDeath() {
        if (playerDead) {
            return;
        }
        playerDead = true;
        playerDeathStateTime = 0f;
        clearPlayerHazardHitReaction();
        player.attacking = false;
        player.attackTimer = 0f;
        player.dashing = false;
        player.dashTimer = 0f;
        player.dashAttackActive = false;
        player.dashAttackMoveDuration = 0f;
        player.dashAttackFollowupQueued = false;
        player.moving = false;
        activeAttackAnim = null;
        postDashMovementLockTimer = 0f;
        cameraTeleportTargetActive = false;
        dashAttackPlannedPositionValid = false;
        clearDashAttackSweepState();
    }

    private void resetPlayerDeathState() {
        playerDead = false;
        playerDeathStateTime = 0f;
        resetPlayerAbilityCooldowns();
    }

    private void resetPlayerAbilityCooldowns() {
        dashAttackAbilityCooldownTimer = 0f;
        spatialDashAbilityCooldownTimer = 0f;
    }

    private void respawnPlayerAfterDeath() {
        int currentLevel = PlayerLevelStats.clampLevel(player.playerLevel);
        String respawnMapId = MAP_ID_VILLAGE;
        String respawnSpawnLayer = THIRD_QUEST_INN_ENTRANCE_LAYER_NAME;
        String respawnMessage = "You recover outside the inn.";
        boolean saveRespawnStateAfterLoad = false;
        if (MAP_ID_BOSS_ARENA_PHASE_2.equals(world.activeMapId)) {
            respawnMapId = MAP_ID_BOSS_ARENA_PHASE_2;
            respawnSpawnLayer = BOSS_ARENA_PHASE_2_PLAYER_SPAWN_LAYER_NAME;
            respawnMessage = "You recover at the arena spawn.";
        } else if (MAP_ID_DEMON_CASTLE.equals(world.activeMapId)) {
            respawnMapId = MAP_ID_DEMON_CASTLE;
            respawnSpawnLayer = "Spawn";
            respawnMessage = "You recover at the castle entrance.";
            saveRespawnStateAfterLoad = true;
        }

        loadMap(respawnMapId, respawnSpawnLayer);
        applyPlayerLevelStats(
                currentLevel,
                PlayerLevelStats.maxHealthForLevel(currentLevel),
                PlayerLevelStats.maxStaminaForLevel(currentLevel));
        centerCameraOnPlayer();
        if (saveRespawnStateAfterLoad) {
            writeAutosave();
        }
        if (hudRenderer != null) {
            hudRenderer.showNotification(
                    "Respawn",
                    respawnMessage,
                    FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
        }
    }

    private void showFifthQuestDeathChoice() {
        fifthQuestDeathChoicePending = false;
        fifthQuestDeathChoiceVisible = true;
        fifthQuestDeathChoiceIndex = 1;
    }

    private void clearFifthQuestDeathChoice() {
        fifthQuestDeathChoicePending = false;
        fifthQuestDeathChoiceVisible = false;
        fifthQuestDeathChoiceIndex = 1;
        fifthQuestDeathChoiceQuestBounds.set(0f, 0f, 0f, 0f);
        fifthQuestDeathChoiceFightBounds.set(0f, 0f, 0f, 0f);
    }

    private void confirmFifthQuestDeathChoice(boolean redoEntireQuest) {
        if (!fifthQuestDeathChoiceVisible) {
            return;
        }
        clearFifthQuestDeathChoice();
        if (redoEntireQuest) {
            restartFifthQuestFromLabAlarm();
            return;
        }
        restartFifthQuestFromFirstBattle();
    }

    private void restartFifthQuestFromLabAlarm() {
        int currentLevel = PlayerLevelStats.clampLevel(player.playerLevel);
        stopActiveDialogueVoiceSfx();
        stopTypewriterLoopSfxImmediately();
        firstQuestDialogueActive = false;
        firstQuestItemCardVisible = false;
        activeQuestDialogueId = QuestDialogueId.NONE;
        activeQuestDialogueSpeakers = new String[0];
        activeQuestDialogueLines = new String[0];
        activeQuestItemCardId = QuestItemCardId.NONE;
        activeQuestItemTitle = "";
        activeQuestItemDescription = "";
        resetQuestDialogueTypewriter();
        resetPlayerAbilityCooldowns();
        fourthQuestStage = FOURTH_QUEST_COMPLETE;
        fifthQuestStage = FIFTH_QUEST_DISABLED;
        sixthQuestStage = SIXTH_QUEST_DISABLED;
        gameCompleted = false;
        loadMap(MAP_ID_RESEARCH_LAB_FLOOR_1, null);
        applyPlayerLevelStats(
                currentLevel,
                PlayerLevelStats.maxHealthForLevel(currentLevel),
                PlayerLevelStats.maxStaminaForLevel(currentLevel));
        centerCameraOnPlayer();
        startQuestDialogue(
                QuestDialogueId.FIFTH_QUEST_LAB_ALARM,
                FIFTH_QUEST_LAB_ALARM_SPEAKERS,
                FIFTH_QUEST_LAB_ALARM_DIALOGUE_LINES);
    }

    private void restartFifthQuestFromFirstBattle() {
        int currentLevel = PlayerLevelStats.clampLevel(player.playerLevel);
        stopActiveDialogueVoiceSfx();
        stopTypewriterLoopSfxImmediately();
        firstQuestDialogueActive = false;
        firstQuestItemCardVisible = false;
        activeQuestDialogueId = QuestDialogueId.NONE;
        activeQuestDialogueSpeakers = new String[0];
        activeQuestDialogueLines = new String[0];
        activeQuestItemCardId = QuestItemCardId.NONE;
        activeQuestItemTitle = "";
        activeQuestItemDescription = "";
        resetQuestDialogueTypewriter();
        resetPlayerAbilityCooldowns();
        fourthQuestStage = FOURTH_QUEST_COMPLETE;
        fifthQuestStage = FIFTH_QUEST_ANCHOR_1_BATTLE;
        sixthQuestStage = SIXTH_QUEST_DISABLED;
        gameCompleted = false;
        loadMap(MAP_ID_VILLAGE, VILLAGE_PORTAL_ACTUAL_LAYER_NAME);
        ensureFifthQuestVillageLayout();
        positionPlayerAtFifthQuestFirstBattle();
        applyPlayerLevelStats(
                currentLevel,
                PlayerLevelStats.maxHealthForLevel(currentLevel),
                PlayerLevelStats.maxStaminaForLevel(currentLevel));
        fifthQuestEncounterSpawned = false;
        fifthQuestFirstEncounterIntroPlayed = false;
        stopFifthQuestFirstEncounterIntro();
        refreshFirstQuestGuidance();
        centerCameraOnPlayer();
    }

    private void positionPlayerAtFifthQuestFirstBattle() {
        ensureFifthQuestVillageLayout();
        Rectangle firstBattleBounds = fifthQuestAnchorBounds[0].width > 0f
                ? fifthQuestAnchorBounds[0]
                : fifthQuestOuterRuinsBounds;
        if (firstBattleBounds == null || firstBattleBounds.width <= 0f || firstBattleBounds.height <= 0f) {
            return;
        }
        movePlayerBottomCenterTo(
                firstBattleBounds.x + firstBattleBounds.width * 0.5f,
                firstBattleBounds.y + firstBattleBounds.height * 0.25f);
    }

    private void positionPlayerAtFifthQuestThirdAnchor() {
        ensureFifthQuestVillageLayout();
        Rectangle finalAnchorBounds = fifthQuestAnchorBounds[2].width > 0f
                ? fifthQuestAnchorBounds[2]
                : fifthQuestPortalBounds;
        if (finalAnchorBounds == null || finalAnchorBounds.width <= 0f || finalAnchorBounds.height <= 0f) {
            return;
        }
        movePlayerBottomCenterTo(
                finalAnchorBounds.x + finalAnchorBounds.width * 0.5f,
                finalAnchorBounds.y + finalAnchorBounds.height * 0.25f);
        player.facingLeft = false;
    }

    private void movePlayerBottomCenterTo(float centerX, float bottomY) {
        Vector2 free = findNearestFreeSpot(new Vector2(centerX, bottomY));
        float halfW = HITBOX_WIDTH * 0.5f;
        player.bounds.set(free.x - halfW, free.y, HITBOX_WIDTH, HITBOX_HEIGHT);
        player.moving = false;
    }

    private void movePlayerBesideScientistForLabDialogue() {
        Rectangle scientistBounds = scientistNpcBounds();
        if (scientistBounds == null) {
            return;
        }
        float scientistCenterX = scientistBounds.x + scientistBounds.width * 0.5f;
        float targetCenterX = scientistCenterX - scientistBounds.width * 0.5f - DEFAULT_TILE_SIZE * 1.5f;
        movePlayerBottomCenterTo(targetCenterX, scientistBounds.y);
        player.facingLeft = false;
        centerCameraOnPlayer();
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

    private void configureRoom3DoorAnimation() {
        room3DoorAnimationFrames.clear();
        room3DoorTriggerLayer = null;
        room3DoorTriggerBounds.set(0f, 0f, 0f, 0f);
        room3DoorClosedCollisionBounds.set(0f, 0f, 0f, 0f);
        room3DoorTriggerZoneBounds.clear();
        room3DoorAnimationFrameIndex = 0;
        room3DoorAnimationTimer = 0f;
        room3DoorPauseTimer = 0f;
        room3DoorClosing = false;
        room3DoorClosed = false;
        room3DoorCollisionEnabled = false;
        room3DoorTriggered = false;
        room3DoorTriggerPrimed = false;
        wasPlayerWithinRoom3DoorTrigger = false;

        if (!MAP_ID_DEMON_CASTLE.equals(world.activeMapId)) {
            return;
        }

        MapLayer room3DoorGroup = findLayerByName(ROOM3_DOOR_ANIMATION_GROUP_NAME);
        room3DoorTriggerLayer = findLayerInGroup(
                ROOM3_DOOR_ANIMATION_GROUP_NAME,
                ROOM3_DOOR_TRIGGER_LAYER_NAME_ALT);
        if (room3DoorTriggerLayer == null) {
            room3DoorTriggerLayer = findLayerInGroup(
                    ROOM3_DOOR_ANIMATION_GROUP_NAME,
                    ROOM3_DOOR_TRIGGER_LAYER_NAME);
        }
        if (room3DoorTriggerLayer == null) {
            room3DoorTriggerLayer = findLayerByName(ROOM3_DOOR_TRIGGER_LAYER_NAME_ALT);
        }
        if (room3DoorTriggerLayer == null) {
            // In Demon-Castle_finite.tmx this trigger is authored in a sibling door group.
            room3DoorTriggerLayer = findLayerByName(ROOM3_DOOR_TRIGGER_LAYER_NAME);
        }
        appendDoorFramesFromGroup(room3DoorGroup, room3DoorAnimationFrames);
        if (room3DoorFrameCount() == 0) {
            return;
        }

        // Build proximity zones directly from DOOR-1 tiles so multiple authored door placements trigger reliably.
        if (room3DoorAnimationFrames.size > 0) {
            appendTileBoundsFromLayer(room3DoorAnimationFrames.get(0), room3DoorTriggerZoneBounds);
        }

        Rectangle visualBounds = doorAnimationVisualBounds(room3DoorAnimationFrames);
        if (visualBounds != null && visualBounds.width > 0f && visualBounds.height > 0f) {
            room3DoorTriggerBounds.set(visualBounds);
            room3DoorClosedCollisionBounds.set(visualBounds);
        }

        Rectangle triggerBounds = objectLayerBounds(room3DoorTriggerLayer);
        if (triggerBounds != null && triggerBounds.width > 0f && triggerBounds.height > 0f
                && room3DoorTriggerBounds.width <= 0f && room3DoorTriggerBounds.height <= 0f) {
            room3DoorTriggerBounds.set(triggerBounds);
            room3DoorClosedCollisionBounds.set(triggerBounds);
        }

        // Room-3 door starts open (DOOR-1) and closes once when the player enters trigger bounds.
        room3DoorAnimationFrameIndex = 0;
        setRoom3DoorFrameVisible(room3DoorAnimationFrameIndex);
        room3DoorClosed = false;
        room3DoorCollisionEnabled = false;
        room3DoorTriggered = false;
        room3DoorTriggerPrimed = false;
        wasPlayerWithinRoom3DoorTrigger = false;
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
        bossFightZoneLayer = resolveFightZoneLayer(BOSS_1_GROUP_LAYER_NAME, BOSS_FIGHT_ZONE_LAYER_NAME);
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
        if (isSixthQuestCastleRaidActive() || isSixthQuestFinalBattleActive() || (chestIdleAnim == null && chestOpenAnim == null)) {
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

    private boolean isSixthQuestCastleRaidActive() {
        return MAP_ID_DEMON_CASTLE.equals(world.activeMapId)
                && sixthQuestStage >= SIXTH_QUEST_ENTER_PORTAL
                && sixthQuestStage < SIXTH_QUEST_FINAL_PHASE_INTRO;
    }

    private boolean isSixthQuestFinalBattleActive() {
        return MAP_ID_BOSS_ARENA_PHASE_2.equals(world.activeMapId)
                && sixthQuestStage >= SIXTH_QUEST_FINAL_PHASE_INTRO
                && sixthQuestStage < SIXTH_QUEST_COMPLETE;
    }

    private MapLayer resolveGolem1WalkableLayer() {
        return resolveFightZoneLayer(GOLEM_1_GROUP_LAYER_NAME, GOLEM_1_WALKABLE_LAYER_NAME);
    }

    private MapLayer resolveFightZoneLayer(String groupLayerName, String layerName) {
        MapLayer layer = findLayerInGroup(groupLayerName, layerName);
        if (isWallCollisionLayer(layer)) {
            return null;
        }
        return layer;
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

    private void addTutorialBatAt(float x, float y) {
        BatInstance bat = new BatInstance();
        bat.position.set(x, y);
        bat.tutorialEnemy = true;
        bat.maxHealth = THIRD_QUEST_TUTORIAL_BAT_HEALTH;
        bat.health = THIRD_QUEST_TUTORIAL_BAT_HEALTH;
        bat.chaseSpeed = THIRD_QUEST_TUTORIAL_BAT_SPEED;
        bat.playerDamage = THIRD_QUEST_TUTORIAL_BAT_DAMAGE;
        bat.attackIntervalSeconds = VILLAGE_BAT_ATTACK_INTERVAL_SECONDS;
        bat.state = BatAnimationState.CHASING;
        bat.stateTime = 0f;
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
        demon.maxHealth = SIXTH_QUEST_BOSS_PHASE_2_HEALTH;
        demon.health = demon.maxHealth;
        demon.facingLeft = true;
        demon.attacking = false;
        demon.attackStateTime = 0f;
        demon.hurting = false;
        demon.hurtStateTime = 0f;
        demon.attackDamageApplied = false;
        demon.playerHitCooldownTimer = 0f;
        demon.skillCooldownTimer = BOSS_ARENA_PHASE_2_EXPLOSION_SKILL_COOLDOWN_SECONDS;
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
        boolean introLocked = MAP_ID_BOSS_ARENA_PHASE_2.equals(world.activeMapId)
                && (sixthQuestStage == SIXTH_QUEST_FINAL_PHASE_INTRO
                || isBossArenaFinalIntroActive()
                || activeQuestDialogueId == QuestDialogueId.SIXTH_QUEST_FINAL_PHASE_INTRO);
        float playerCenterX = player.bounds.x + player.bounds.width * 0.5f;
        float playerCenterY = player.bounds.y + player.bounds.height * 0.5f;
        float stopDistance = BOSS_ARENA_PHASE_2_DEMON_CHASE_STOP_DISTANCE_TILES * DEFAULT_TILE_SIZE;
        float stopDistanceSq = stopDistance * stopDistance;
        float attackRangeSq = BOSS_ARENA_PHASE_2_DEMON_ATTACK_RANGE * BOSS_ARENA_PHASE_2_DEMON_ATTACK_RANGE;
        for (int i = bossArenaPhase2Demons.size - 1; i >= 0; i--) {
            BossArenaPhase2DemonInstance demon = bossArenaPhase2Demons.get(i);
            if (demon.playerHitCooldownTimer > 0f) {
                demon.playerHitCooldownTimer = Math.max(0f, demon.playerHitCooldownTimer - safeDelta);
            }
            if (demon.attackCooldownTimer > 0f) {
                demon.attackCooldownTimer = Math.max(0f, demon.attackCooldownTimer - safeDelta);
            }
            if (demon.skillCooldownTimer > 0f) {
                demon.skillCooldownTimer = Math.max(0f, demon.skillCooldownTimer - safeDelta);
            }
            if (demon.health <= 0f && !demon.hurting) {
                bossArenaPhase2Demons.removeIndex(i);
                continue;
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

            if (introLocked) {
                demon.attacking = false;
                demon.attackStateTime = 0f;
                demon.attackDamageApplied = false;
                demon.stateTime += safeDelta;
                continue;
            }

            if (demon.hurting) {
                demon.attackStateTime = 0f;
                demon.attacking = false;
                demon.attackDamageApplied = false;
                demon.hurtStateTime += safeDelta;
                if (bossArenaPhase2DemonHurtAnim == null
                        || bossArenaPhase2DemonHurtAnim.isAnimationFinished(demon.hurtStateTime)) {
                    demon.hurting = false;
                    demon.hurtStateTime = 0f;
                    if (demon.health <= 0f) {
                        bossArenaPhase2Demons.removeIndex(i);
                        continue;
                    }
                }
                demon.stateTime += safeDelta;
                continue;
            }

            if (demon.attacking) {
                demon.attackStateTime += safeDelta;
                if (!demon.attackDamageApplied) {
                    float impactTime = safeDelta;
                    if (bossArenaPhase2DemonAttackAnim != null) {
                        impactTime = Math.max(safeDelta, bossArenaPhase2DemonAttackAnim.getAnimationDuration() * 0.5f);
                    }
                    if (demon.attackStateTime >= impactTime) {
                        demon.attackDamageApplied = true;
                    }
                }
                boolean attackAnimFinished = bossArenaPhase2DemonAttackAnim == null
                        || demon.attackStateTime >= bossArenaPhase2DemonAttackAnimationDuration();
                if (attackAnimFinished) {
                    demon.attacking = false;
                    demon.attackStateTime = 0f;
                    demon.attackDamageApplied = false;
                }
            }

            if (hasPendingOrActiveBossArenaPhase2Explosions()) {
                demon.moving = false;
                demon.stateTime += safeDelta;
                continue;
            }

            if (distanceSq <= attackRangeSq
                    && demon.skillCooldownTimer <= 0f
                    && bossArenaPhase2ExplosionAnim != null) {
                queueBossArenaPhase2ExplosionPattern(demon);
                demon.skillCooldownTimer = BOSS_ARENA_PHASE_2_EXPLOSION_SKILL_COOLDOWN_SECONDS;
                demon.attackCooldownTimer = BOSS_ARENA_PHASE_2_EXPLOSION_SKILL_COOLDOWN_SECONDS;
                if (bossArenaPhase2DemonAttackAnim != null) {
                    demon.attacking = true;
                    demon.attackStateTime = 0f;
                    demon.attackDamageApplied = false;
                }
                demon.moving = false;
                demon.stateTime += safeDelta;
                continue;
            }

            if (distanceSq <= stopDistanceSq && bossArenaPhase2DemonAttackAnim != null) {
                if (!demon.attacking && demon.attackCooldownTimer <= 0f) {
                    demon.attacking = true;
                    demon.attackStateTime = 0f;
                    demon.attackDamageApplied = false;
                    demon.attackCooldownTimer = BOSS_ARENA_PHASE_2_DEMON_ATTACK_INTERVAL_SECONDS;
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

    private void startBossArenaPhase2DemonSkill(BossArenaPhase2DemonInstance demon) {
        if (demon == null) {
            return;
        }
        demon.skillStateTime = 0f;
        demon.skillLoopTimer = 0f;
        if (bossArenaPhase2SkillStartAnim != null) {
            demon.skillPhase = BossArenaPhase2SkillPhase.START;
        } else if (bossArenaPhase2SkillLoopAnim != null) {
            demon.skillPhase = BossArenaPhase2SkillPhase.LOOP;
        } else if (bossArenaPhase2SkillEndAnim != null) {
            demon.skillPhase = BossArenaPhase2SkillPhase.END;
        } else {
            demon.skillPhase = BossArenaPhase2SkillPhase.NONE;
        }
    }

    private void updateBossArenaPhase2DemonSkill(BossArenaPhase2DemonInstance demon, float delta) {
        if (demon == null || delta <= 0f || demon.skillPhase == BossArenaPhase2SkillPhase.NONE) {
            return;
        }
        demon.skillStateTime += delta;
        switch (demon.skillPhase) {
            case START -> {
                if (bossArenaPhase2SkillStartAnim == null
                        || bossArenaPhase2SkillStartAnim.isAnimationFinished(demon.skillStateTime)) {
                    demon.skillPhase = bossArenaPhase2SkillLoopAnim != null
                            ? BossArenaPhase2SkillPhase.LOOP
                            : (bossArenaPhase2SkillEndAnim != null ? BossArenaPhase2SkillPhase.END : BossArenaPhase2SkillPhase.NONE);
                    demon.skillStateTime = 0f;
                    demon.skillLoopTimer = 0f;
                }
            }
            case LOOP -> {
                demon.skillLoopTimer += delta;
                if (bossArenaPhase2SkillLoopAnim == null || demon.skillLoopTimer >= BOSS_ARENA_PHASE_2_SKILL_LOOP_SECONDS) {
                    demon.skillPhase = bossArenaPhase2SkillEndAnim != null
                            ? BossArenaPhase2SkillPhase.END
                            : BossArenaPhase2SkillPhase.NONE;
                    demon.skillStateTime = 0f;
                }
            }
            case END -> {
                if (bossArenaPhase2SkillEndAnim == null
                        || bossArenaPhase2SkillEndAnim.isAnimationFinished(demon.skillStateTime)) {
                    demon.skillPhase = BossArenaPhase2SkillPhase.NONE;
                    demon.skillStateTime = 0f;
                    demon.skillLoopTimer = 0f;
                }
            }
            case NONE -> {
            }
        }
    }

    private boolean bossArenaPhase2DemonHasSkillAnimations() {
        return bossArenaPhase2SkillStartAnim != null
                || bossArenaPhase2SkillLoopAnim != null
                || bossArenaPhase2SkillEndAnim != null;
    }

    private Animation<TextureRegion> activeBossArenaPhase2DemonSkillAnimation(BossArenaPhase2DemonInstance demon) {
        if (demon == null) {
            return null;
        }
        return switch (demon.skillPhase) {
            case START -> bossArenaPhase2SkillStartAnim;
            case LOOP -> bossArenaPhase2SkillLoopAnim;
            case END -> bossArenaPhase2SkillEndAnim;
            case NONE -> null;
        };
    }

    private void updateBossArenaPhase2Explosions(float delta) {
        if (bossArenaPhase2Explosions.size == 0 || delta <= 0f) {
            return;
        }
        if (bossArenaPhase2ExplosionAnim == null) {
            bossArenaPhase2Explosions.clear();
            return;
        }
        float safeDelta = Math.max(0f, delta);
        float impactTime = bossArenaPhase2ExplosionAnim.getAnimationDuration() * 0.45f;
        boolean playedExplosionSfxThisFrame = false;
        for (int i = bossArenaPhase2Explosions.size - 1; i >= 0; i--) {
            BossArenaPhase2ExplosionInstance burst = bossArenaPhase2Explosions.get(i);
            if (!burst.active) {
                burst.delayTimer = Math.max(0f, burst.delayTimer - safeDelta);
                if (burst.delayTimer <= 0f) {
                    burst.active = true;
                    burst.stateTime = 0f;
                    burst.damageApplied = false;
                    triggerGolemExplosionCameraShake();
                    if (!playedExplosionSfxThisFrame) {
                        playGolem1ExplosionSfx();
                        playedExplosionSfxThisFrame = true;
                    }
                }
            }
            if (!burst.active) {
                continue;
            }
            burst.stateTime += safeDelta;
            if (!burst.damageApplied && burst.stateTime >= impactTime) {
                burst.damageApplied = true;
                applyBossArenaPhase2ExplosionHitToPlayer(burst);
            }
            if (bossArenaPhase2ExplosionAnim.isAnimationFinished(burst.stateTime)) {
                bossArenaPhase2Explosions.removeIndex(i);
            }
        }
    }

    private boolean hasPendingOrActiveBossArenaPhase2Explosions() {
        return bossArenaPhase2Explosions.size > 0;
    }

    private void queueBossArenaPhase2ExplosionPattern(BossArenaPhase2DemonInstance demon) {
        bossArenaPhase2Explosions.clear();
        ensureSixthQuestFinalArenaLayout();
        Rectangle bounds = sixthQuestFinalPhaseExplosionBounds.width > 0f && sixthQuestFinalPhaseExplosionBounds.height > 0f
                ? new Rectangle(sixthQuestFinalPhaseExplosionBounds)
                : (world.mapBounds != null ? new Rectangle(world.mapBounds) : null);
        if (bounds == null || bounds.width <= 0f || bounds.height <= 0f) {
            return;
        }

        float margin = BOSS_ARENA_PHASE_2_EXPLOSION_EDGE_MARGIN_TILES * DEFAULT_TILE_SIZE;
        float minX = bounds.x + margin;
        float maxX = bounds.x + bounds.width - margin;
        float minY = bounds.y + margin;
        float maxY = bounds.y + bounds.height - margin;
        if (maxX <= minX || maxY <= minY) {
            minX = bounds.x;
            maxX = bounds.x + bounds.width;
            minY = bounds.y;
            maxY = bounds.y + bounds.height;
        }

        float minSeparation = BOSS_ARENA_PHASE_2_EXPLOSION_MIN_SEPARATION_TILES * DEFAULT_TILE_SIZE;
        float minSeparationSq = minSeparation * minSeparation;
        Rectangle testBounds = new Rectangle();
        float testSize = DEFAULT_TILE_SIZE;
        int attempts = 0;
        while (bossArenaPhase2Explosions.size < BOSS_ARENA_PHASE_2_EXPLOSION_SPOT_COUNT && attempts < 600) {
            attempts++;
            float spotX = minX + (float) Math.random() * Math.max(1f, maxX - minX);
            float spotY = minY + (float) Math.random() * Math.max(1f, maxY - minY);
            testBounds.set(spotX - testSize * 0.5f, spotY - testSize * 0.5f, testSize, testSize);
            if (collides(testBounds)) {
                continue;
            }

            boolean tooClose = false;
            for (int i = 0; i < bossArenaPhase2Explosions.size; i++) {
                BossArenaPhase2ExplosionInstance existing = bossArenaPhase2Explosions.get(i);
                float dx = existing.position.x - spotX;
                float dy = existing.position.y - spotY;
                if (dx * dx + dy * dy < minSeparationSq) {
                    tooClose = true;
                    break;
                }
            }
            if (tooClose) {
                continue;
            }

            BossArenaPhase2ExplosionInstance burst = new BossArenaPhase2ExplosionInstance();
            burst.position.set(spotX, spotY);
            burst.delayTimer = BOSS_ARENA_PHASE_2_EXPLOSION_CAST_DELAY_SECONDS
                    + (float) Math.random() * BOSS_ARENA_PHASE_2_EXPLOSION_CAST_DELAY_JITTER_SECONDS;
            bossArenaPhase2Explosions.add(burst);
        }

        if (bossArenaPhase2Explosions.size == 0) {
            BossArenaPhase2ExplosionInstance fallbackBurst = new BossArenaPhase2ExplosionInstance();
            if (demon != null) {
                fallbackBurst.position.set(demon.position.x, demon.position.y);
            } else {
                fallbackBurst.position.set(bounds.x + bounds.width * 0.5f, bounds.y + bounds.height * 0.5f);
            }
            fallbackBurst.delayTimer = BOSS_ARENA_PHASE_2_EXPLOSION_CAST_DELAY_SECONDS;
            bossArenaPhase2Explosions.add(fallbackBurst);
        }
    }

    private void applyBossArenaPhase2ExplosionHitToPlayer(BossArenaPhase2ExplosionInstance burst) {
        if (burst == null || player.currentHealth <= 0f || isPlayerHazardHitReactionActive()) {
            return;
        }
        float radius = currentBossArenaPhase2ExplosionRadiusTiles() * DEFAULT_TILE_SIZE;
        float radiusSq = radius * radius;
        float playerCenterX = player.bounds.x + player.bounds.width * 0.5f;
        float playerCenterY = player.bounds.y + player.bounds.height * 0.5f;
        float dx = playerCenterX - burst.position.x;
        float dy = playerCenterY - burst.position.y;
        if (dx * dx + dy * dy > radiusSq) {
            return;
        }
        applyDamageToPlayer(SIXTH_QUEST_BOSS_PHASE_2_DAMAGE);
    }

    private void updateFifthQuestDemons(float delta) {
        if (fifthQuestDemons.size == 0
                || delta <= 0f
                || (!MAP_ID_VILLAGE.equals(world.activeMapId)
                && !MAP_ID_DEMON_CASTLE.equals(world.activeMapId)
                && !MAP_ID_BOSS_ARENA_PHASE_2.equals(world.activeMapId))) {
            return;
        }
        float safeDelta = Math.max(0f, delta);
        if (fifthQuestZombieSfxCooldownTimer > 0f) {
            fifthQuestZombieSfxCooldownTimer = Math.max(0f, fifthQuestZombieSfxCooldownTimer - safeDelta);
        }
        float playerCenterX = player.bounds.x + player.bounds.width * 0.5f;
        float playerCenterY = player.bounds.y + player.bounds.height * 0.5f;
        for (int i = fifthQuestDemons.size - 1; i >= 0; i--) {
            FifthQuestDemonInstance demon = fifthQuestDemons.get(i);
            if (demon.playerHitCooldownTimer > 0f) {
                demon.playerHitCooldownTimer = Math.max(0f, demon.playerHitCooldownTimer - safeDelta);
            }
            if (demon.attackCooldownTimer > 0f) {
                demon.attackCooldownTimer = Math.max(0f, demon.attackCooldownTimer - safeDelta);
            }

            if (demon.dying) {
                demon.dieStateTime += safeDelta;
                Animation<TextureRegion> deathAnimation = fifthQuestDemonDeadAnimation(demon.type);
                if (deathAnimation == null || deathAnimation.isAnimationFinished(demon.dieStateTime)) {
                    fifthQuestDemons.removeIndex(i);
                }
                continue;
            }

            float dx = playerCenterX - demon.position.x;
            float dy = playerCenterY - demon.position.y;
            if (Math.abs(dx) > 0.001f) {
                demon.facingLeft = dx < 0f;
            }

            if (isFifthQuestFirstEncounterIntroActive()
                    && demon.questEncounterId == fifthQuestFirstEncounterIntroEncounterId) {
                demon.moving = false;
                demon.attacking = false;
                demon.attackStateTime = 0f;
                demon.attackDamageApplied = false;
                demon.stateTime += safeDelta;
                continue;
            }

            if (demon.hurting) {
                demon.moving = false;
                demon.attacking = false;
                demon.attackStateTime = 0f;
                demon.attackDamageApplied = false;
                demon.hurtStateTime += safeDelta;
                Animation<TextureRegion> hurtAnimation = fifthQuestDemonHurtAnimation(demon.type);
                if (hurtAnimation == null || hurtAnimation.isAnimationFinished(demon.hurtStateTime)) {
                    demon.hurting = false;
                    demon.hurtStateTime = 0f;
                }
                demon.stateTime += safeDelta;
                continue;
            }

            if (demon.attacking) {
                demon.moving = false;
                demon.attackStateTime += safeDelta;
                Animation<TextureRegion> attackAnimation = fifthQuestDemonAttackAnimation(demon.type);
                tryApplyFifthQuestDemonAttackHitToPlayer(demon, attackAnimation);
                if (attackAnimation == null || attackAnimation.isAnimationFinished(demon.attackStateTime)) {
                    demon.attacking = false;
                    demon.attackStateTime = 0f;
                    demon.attackDamageApplied = false;
                }
                demon.stateTime += safeDelta;
                continue;
            }

            if (!Intersector.overlaps(player.bounds, demon.zoneBounds)) {
                demon.moving = false;
                demon.stateTime += safeDelta;
                continue;
            }

            float distanceSq = dx * dx + dy * dy;
            if (distanceSq <= demon.attackRange * demon.attackRange) {
                demon.moving = false;
                if (demon.attackCooldownTimer <= 0f) {
                    startFifthQuestDemonAttack(demon);
                }
                demon.stateTime += safeDelta;
                continue;
            }

            float stopDistance = Math.max(18f, demon.attackRange * 0.62f);
            float stopDistanceSq = stopDistance * stopDistance;
            boolean moved = false;
            if (distanceSq > stopDistanceSq) {
                float distance = (float) Math.sqrt(distanceSq);
                float maxStep = demon.chaseSpeed * safeDelta;
                float remainingAfterStop = Math.max(0f, distance - stopDistance);
                float step = Math.min(maxStep, remainingAfterStop);
                if (step > 0f && distance > 0.001f) {
                    demon.position.x += dx / distance * step;
                    demon.position.y += dy / distance * step;
                    clampFifthQuestDemonToZone(demon);
                    moved = true;
                }
            }
            demon.moving = moved;
            demon.stateTime += safeDelta;
        }
    }

    private void startFifthQuestDemonAttack(FifthQuestDemonInstance demon) {
        if (demon == null || demon.dying) {
            return;
        }
        demon.attacking = true;
        demon.attackStateTime = 0f;
        demon.attackDamageApplied = false;
        demon.attackCooldownTimer = Math.max(0.2f, demon.attackIntervalSeconds);
        playFifthQuestZombieSfx(FIFTH_QUEST_ZOMBIE_ATTACK_SFX_VOLUME_MULTIPLIER);
    }

    private void tryApplyFifthQuestDemonAttackHitToPlayer(FifthQuestDemonInstance demon, Animation<TextureRegion> attackAnimation) {
        if (demon == null
                || attackAnimation == null
                || demon.attackDamageApplied
                || player.currentHealth <= 0f
                || isPlayerHazardHitReactionActive()) {
            return;
        }
        float hitMoment = Math.max(0.08f, attackAnimation.getAnimationDuration() * 0.52f);
        if (demon.attackStateTime < hitMoment) {
            return;
        }
        float playerCenterX = player.bounds.x + player.bounds.width * 0.5f;
        float playerCenterY = player.bounds.y + player.bounds.height * 0.5f;
        float dx = playerCenterX - demon.position.x;
        float dy = playerCenterY - demon.position.y;
        if (dx * dx + dy * dy > demon.attackRange * demon.attackRange) {
            return;
        }
        demon.attackDamageApplied = true;
        applyFifthQuestDemonDamageToPlayer(demon.playerDamage);
    }

    private void updateFifthQuestDemonHurtFromPlayer() {
        if (fifthQuestDemons.size == 0
                || !player.attacking
                || activeAttackAnim == null
                || isTeleportDashAnimation(activeAttackAnim)) {
            return;
        }
        float hitRange = playerBatHitRange();
        float hitRangeSq = hitRange * hitRange;
        for (int i = 0; i < fifthQuestDemons.size; i++) {
            FifthQuestDemonInstance demon = fifthQuestDemons.get(i);
            if (demon.dying
                    || demon.health <= 0f
                    || demon.playerHitCooldownTimer > 0f
                    || !isFifthQuestDemonWithinPlayerAttackRange(demon, hitRangeSq)) {
                continue;
            }
            applyHitToFifthQuestDemonFromPlayer(demon);
        }
    }

    private void applyHitToFifthQuestDemonFromPlayer(FifthQuestDemonInstance demon) {
        if (demon == null || demon.dying || demon.health <= 0f || demon.playerHitCooldownTimer > 0f) {
            return;
        }
        demon.health = Math.max(0f, demon.health - BAT_HIT_DAMAGE * currentPlayerAttackDamageMultiplier());
        demon.moving = false;
        demon.attacking = false;
        demon.attackStateTime = 0f;
        demon.attackDamageApplied = false;
        if (demon.health <= 0f) {
            demon.dying = true;
            demon.dieStateTime = 0f;
            demon.hurting = false;
            demon.hurtStateTime = 0f;
            demon.attackCooldownTimer = 0f;
            demon.playerHitCooldownTimer = 0f;
            return;
        }
        Animation<TextureRegion> hurtAnimation = fifthQuestDemonHurtAnimation(demon.type);
        if (hurtAnimation != null) {
            demon.hurting = true;
            demon.hurtStateTime = 0f;
        }
        demon.playerHitCooldownTimer = FIFTH_QUEST_DEMON_PLAYER_HIT_COOLDOWN_SECONDS;
    }

    private boolean isFifthQuestDemonWithinPlayerAttackRange(FifthQuestDemonInstance demon, float hitRangeSq) {
        if (demon == null) {
            return false;
        }
        float playerCenterX = player.bounds.x + player.bounds.width * 0.5f;
        float playerCenterY = player.bounds.y + player.bounds.height * 0.5f;
        float centerX = fifthQuestDemonHitboxCenterX(demon);
        float centerY = fifthQuestDemonHitboxCenterY(demon);
        float halfWidth = fifthQuestDemonHitboxHalfWidth();
        float halfHeight = fifthQuestDemonHitboxHalfHeight();
        float left = centerX - halfWidth;
        float right = centerX + halfWidth;
        float bottom = centerY - halfHeight;
        float top = centerY + halfHeight;
        float closestX = Math.max(left, Math.min(playerCenterX, right));
        float closestY = Math.max(bottom, Math.min(playerCenterY, top));
        float dx = playerCenterX - closestX;
        float dy = playerCenterY - closestY;
        return dx * dx + dy * dy <= hitRangeSq;
    }

    private void clampFifthQuestDemonToZone(FifthQuestDemonInstance demon) {
        if (demon == null || demon.zoneBounds.width <= 0f || demon.zoneBounds.height <= 0f) {
            return;
        }
        float minX = demon.zoneBounds.x;
        float minY = demon.zoneBounds.y;
        float maxX = demon.zoneBounds.x + demon.zoneBounds.width;
        float maxY = demon.zoneBounds.y + demon.zoneBounds.height;
        demon.position.x = Math.max(minX, Math.min(demon.position.x, maxX));
        demon.position.y = Math.max(minY, Math.min(demon.position.y, maxY));
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
            playSfx(attackSfx, EVIL_WIZARD_ATTACK_SFX_VOLUME);
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
        applyDamageToPlayer(EVIL_WIZARD_ATTACK_HAZARD_DAMAGE);
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
            GameSettingsService settingsService = settingsService();
            if (settingsService != null) {
                settingsService.applyMusicVolume(evilWizardPreExplosionSfx, EVIL_WIZARD_PRE_EXPLOSION_SFX_VOLUME);
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
        wizard.health = Math.max(0f, wizard.health - EVIL_WIZARD_HIT_DAMAGE * currentPlayerAttackDamageMultiplier());
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
        golem.health = Math.max(0f, golem.health - GOLEM_1_HIT_DAMAGE * currentPlayerAttackDamageMultiplier());
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
        if (demon == null || demon.hurting || demon.playerHitCooldownTimer > 0f || demon.health <= 0f) {
            return;
        }
        demon.health = Math.max(0f, demon.health - BAT_HIT_DAMAGE * currentPlayerAttackDamageMultiplier());
        demon.moving = false;
        demon.attacking = false;
        demon.attackStateTime = 0f;
        demon.attackDamageApplied = false;
        if (bossArenaPhase2DemonHurtAnim != null) {
            demon.hurting = true;
            demon.hurtStateTime = 0f;
        }
        demon.playerHitCooldownTimer = demon.health > 0f
                ? BOSS_ARENA_PHASE_2_DEMON_PLAYER_HIT_COOLDOWN_SECONDS
                : 0f;
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

    private TextureRegion currentBossArenaPhase2DemonAttackFrame(float attackStateTime) {
        if (bossArenaPhase2DemonAttackAnim == null) {
            return null;
        }
        return bossArenaPhase2DemonAttackAnim.getKeyFrame(
                adjustedBossArenaPhase2DemonAttackTime(attackStateTime),
                false);
    }

    private float adjustedBossArenaPhase2DemonAttackTime(float attackStateTime) {
        if (bossArenaPhase2DemonAttackAnim == null) {
            return attackStateTime;
        }
        int attackFrameCount = bossArenaPhase2DemonAttackFrameTextures.size;
        if (attackFrameCount <= 0
                || BOSS_ARENA_PHASE_2_DEMON_ATTACK_HOLD_FRAME_INDEX < 0
                || BOSS_ARENA_PHASE_2_DEMON_ATTACK_HOLD_FRAME_INDEX >= attackFrameCount) {
            return attackStateTime;
        }
        float frameDuration = bossArenaPhase2DemonAttackAnim.getFrameDuration();
        float holdStartTime = frameDuration * BOSS_ARENA_PHASE_2_DEMON_ATTACK_HOLD_FRAME_INDEX;
        float holdEndTime = holdStartTime + BOSS_ARENA_PHASE_2_DEMON_ATTACK_HOLD_SECONDS;
        if (attackStateTime < holdStartTime) {
            return attackStateTime;
        }
        if (attackStateTime < holdEndTime) {
            return holdStartTime;
        }
        return attackStateTime - BOSS_ARENA_PHASE_2_DEMON_ATTACK_HOLD_SECONDS;
    }

    private float bossArenaPhase2DemonAttackAnimationDuration() {
        if (bossArenaPhase2DemonAttackAnim == null) {
            return 0f;
        }
        int attackFrameCount = bossArenaPhase2DemonAttackFrameTextures.size;
        if (attackFrameCount <= 0
                || BOSS_ARENA_PHASE_2_DEMON_ATTACK_HOLD_FRAME_INDEX < 0
                || BOSS_ARENA_PHASE_2_DEMON_ATTACK_HOLD_FRAME_INDEX >= attackFrameCount) {
            return bossArenaPhase2DemonAttackAnim.getAnimationDuration();
        }
        return bossArenaPhase2DemonAttackAnim.getAnimationDuration()
                + BOSS_ARENA_PHASE_2_DEMON_ATTACK_HOLD_SECONDS;
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
            if (bat.attackCooldownTimer > 0f) {
                bat.attackCooldownTimer = Math.max(0f, bat.attackCooldownTimer - delta);
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
                    bat.attackDamageApplied = false;
                }
                continue;
            }
            if (bat.state == BatAnimationState.WAKING_UP) {
                if (batWakeUpAnim != null && batWakeUpAnim.isAnimationFinished(bat.stateTime)) {
                    bat.state = BatAnimationState.IDLE_FLY;
                    bat.stateTime = 0f;
                    bat.attackDamageApplied = false;
                }
                continue;
            }
            if (bat.state == BatAnimationState.IDLE_FLY) {
                if (bat.stateTime >= BAT_IDLE_FLY_TO_CHASE_SECONDS) {
                    bat.state = BatAnimationState.CHASING;
                    bat.stateTime = 0f;
                    bat.attackDamageApplied = false;
                }
                continue;
            }
            if (bat.state == BatAnimationState.CHASING) {
                updateBatChaseMovement(bat, delta);
                if (bat.attackCooldownTimer <= 0f && isBatWithinAttackRange(bat)) {
                    startBatAttack(bat);
                }
                continue;
            }
            if (bat.state == BatAnimationState.ATTACKING) {
                Animation<TextureRegion> activeAttack = activeBatAttackAnimation(bat);
                if (activeAttack == null) {
                    bat.state = BatAnimationState.CHASING;
                    bat.stateTime = 0f;
                    bat.attackDamageApplied = false;
                } else if (activeAttack.isAnimationFinished(bat.stateTime)) {
                    if (bat.attackCooldownTimer <= 0f && isBatWithinAttackRange(bat)) {
                        startBatAttack(bat);
                    } else {
                        bat.state = BatAnimationState.CHASING;
                        bat.stateTime = 0f;
                        bat.attackDamageApplied = false;
                    }
                } else {
                    tryApplyBatAttackHitToPlayer(bat, activeAttack);
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
        float maxStep = bat.chaseSpeed * delta;
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
        bat.attackDamageApplied = false;
        bat.attackCooldownTimer = Math.max(0f, bat.attackIntervalSeconds);
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

    private void tryApplyBatAttackHitToPlayer(BatInstance bat, Animation<TextureRegion> attackAnimation) {
        if (bat == null
                || attackAnimation == null
                || bat.attackDamageApplied
                || player.currentHealth <= 0f
                || isPlayerHazardHitReactionActive()) {
            return;
        }

        float hitMoment = Math.max(0.08f, attackAnimation.getAnimationDuration() * 0.5f);
        if (bat.stateTime < hitMoment || !isBatWithinAttackRange(bat)) {
            return;
        }

        bat.attackDamageApplied = true;
        applyDamageToPlayer(bat.playerDamage);
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
        bat.health = Math.max(0f, bat.health - BAT_HIT_DAMAGE * currentPlayerAttackDamageMultiplier());
        if (batHurtSfx != null) {
            playSfx(batHurtSfx, BAT_HURT_SFX_VOLUME);
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

    private void updateRoom3DoorAnimation(float delta) {
        if (!MAP_ID_DEMON_CASTLE.equals(world.activeMapId)
                || room3DoorFrameCount() == 0) {
            return;
        }

        boolean playerInTrigger = isPlayerWithinRoom3DoorTrigger();
        if (!room3DoorTriggerPrimed) {
            // Ignore the very first sample so spawn placement does not auto-trigger the door.
            room3DoorTriggerPrimed = true;
            wasPlayerWithinRoom3DoorTrigger = playerInTrigger;
        } else if (!room3DoorTriggered
                && !room3DoorClosed
                && !room3DoorClosing
                && room3DoorPauseTimer <= 0f
                && playerInTrigger
                && !wasPlayerWithinRoom3DoorTrigger) {
            stopPlayerForDoorAction();
            room3DoorPauseTimer = ROOM3_DOOR_TRIGGER_PAUSE_SECONDS;
            room3DoorTriggered = true;
        }
        wasPlayerWithinRoom3DoorTrigger = playerInTrigger;

        if (room3DoorPauseTimer > 0f) {
            room3DoorPauseTimer = Math.max(0f, room3DoorPauseTimer - Math.max(0f, delta));
            if (room3DoorPauseTimer > 0f) {
                return;
            }
            startRoom3DoorClosing();
        }

        if (!room3DoorClosing) {
            return;
        }

        room3DoorAnimationTimer += Math.max(0f, delta);
        if (room3DoorAnimationTimer < ROOM3_DOOR_FRAME_DURATION_SECONDS) {
            return;
        }
        room3DoorAnimationTimer = 0f;

        int frameCount = room3DoorFrameCount();
        int nextIndex = room3DoorAnimationFrameIndex + 1;
        if (nextIndex >= frameCount) {
            room3DoorAnimationFrameIndex = frameCount - 1;
            setRoom3DoorFrameVisible(room3DoorAnimationFrameIndex);
            room3DoorClosing = false;
            room3DoorClosed = true;
            room3DoorCollisionEnabled = hasRoom3DoorCollisionBounds();
            return;
        }
        room3DoorAnimationFrameIndex = nextIndex;
        setRoom3DoorFrameVisible(room3DoorAnimationFrameIndex);
        if (room3DoorAnimationFrameIndex == frameCount - 1) {
            room3DoorClosing = false;
            room3DoorClosed = true;
            room3DoorCollisionEnabled = hasRoom3DoorCollisionBounds();
        }
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
            float step = currentPlayerMoveSpeed() * safeDelta;
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

    private void startBossArenaFinalIntroSequence() {
        if (!MAP_ID_BOSS_ARENA_PHASE_2.equals(world.activeMapId)
                || isBossArenaFinalIntroActive()
                || firstQuestDialogueActive
                || activeQuestDialogueId != QuestDialogueId.NONE) {
            return;
        }
        ensureSixthQuestFinalArenaLayout();
        if (bossArenaPhase2Demons.size == 0) {
            configureBossArenaPhase2Demons();
        }
        stopPlayerForDoorAction();
        player.moving = false;
        bossArenaFinalIntroPhase = BossArenaFinalIntroPhase.AUTO_WALK;
        bossArenaFinalIntroTimer = BOSS_ARENA_FINAL_INTRO_AUTO_WALK_SECONDS;
    }

    private void updateBossArenaFinalIntroSequence(float delta) {
        if (!isBossArenaFinalIntroActive() || camera == null || !MAP_ID_BOSS_ARENA_PHASE_2.equals(world.activeMapId)) {
            return;
        }
        float safeDelta = Math.max(0f, delta);
        float playerTargetX = player.bounds.x + player.bounds.width * 0.5f;
        float playerTargetY = player.bounds.y + player.bounds.height * 0.5f;
        if (bossArenaFinalIntroPhase == BossArenaFinalIntroPhase.AUTO_WALK) {
            float step = currentPlayerMoveSpeed() * safeDelta;
            tryMove(step, 0f);
            player.lastMoveX = 1f;
            player.lastMoveY = 0f;
            player.facingLeft = false;
            player.moving = true;
            smoothCameraToward(playerTargetX, playerTargetY, safeDelta);
            bossArenaFinalIntroTimer = Math.max(0f, bossArenaFinalIntroTimer - safeDelta);
            if (bossArenaFinalIntroTimer <= 0f) {
                player.moving = false;
                bossArenaFinalIntroPhase = BossArenaFinalIntroPhase.FOCUS_BOSS;
                bossArenaFinalIntroTimer = BOSS_ARENA_FINAL_INTRO_FOCUS_SECONDS;
                startDemonBattleMusic();
            }
            return;
        }
        if (bossArenaFinalIntroPhase == BossArenaFinalIntroPhase.FOCUS_BOSS) {
            player.moving = false;
            if (resolveBossArenaFinalIntroCameraTarget()) {
                smoothCameraToward(
                        bossArenaFinalIntroCameraTarget.x,
                        bossArenaFinalIntroCameraTarget.y + BOSS_ARENA_FINAL_INTRO_CAMERA_Y_OFFSET_TILES * DEFAULT_TILE_SIZE,
                        safeDelta);
            } else {
                smoothCameraToward(playerTargetX, playerTargetY, safeDelta);
            }
            float zoomAlpha = 1f - (float) Math.exp(-TELEPORT_CAMERA_SMOOTH_SPEED * safeDelta);
            float targetZoom = DEFAULT_ZOOM * BOSS_ARENA_FINAL_INTRO_ZOOM_MULTIPLIER;
            camera.zoom = lerp(camera.zoom, targetZoom, zoomAlpha);
            if (Math.abs(camera.zoom - targetZoom) < 0.0005f) {
                camera.zoom = targetZoom;
            }
            bossArenaFinalIntroTimer = Math.max(0f, bossArenaFinalIntroTimer - safeDelta);
            if (bossArenaFinalIntroTimer <= 0f) {
                bossArenaFinalIntroPhase = BossArenaFinalIntroPhase.NONE;
                startQuestDialogue(
                        QuestDialogueId.SIXTH_QUEST_FINAL_PHASE_INTRO,
                        SIXTH_QUEST_FINAL_PHASE_INTRO_SPEAKERS,
                        SIXTH_QUEST_FINAL_PHASE_INTRO_DIALOGUE_LINES);
            }
        }
    }

    private boolean isFifthQuestIntroCameraActive() {
        return fifthQuestIntroCameraPhase != FifthQuestIntroCameraPhase.NONE;
    }

    private void startFifthQuestIntroCameraSequence() {
        if (!MAP_ID_VILLAGE.equals(world.activeMapId)) {
            beginFifthQuestAnchorBattle(0);
            return;
        }
        ensureFifthQuestVillageLayout();
        if (!hasFifthQuestIntroCameraBounds(0)
                || !hasFifthQuestIntroCameraBounds(1)
                || !hasFifthQuestIntroCameraBounds(2)) {
            beginFifthQuestAnchorBattle(0);
            return;
        }
        beginFifthQuestIntroCameraSegment(FifthQuestIntroCameraPhase.CAMERA_1);
    }

    private void beginFifthQuestIntroCameraSegment(FifthQuestIntroCameraPhase phase) {
        fifthQuestIntroCameraPhase = phase == null ? FifthQuestIntroCameraPhase.NONE : phase;
        if (fifthQuestIntroCameraPhase == FifthQuestIntroCameraPhase.NONE || camera == null) {
            return;
        }
        Rectangle bounds = currentFifthQuestIntroCameraBounds();
        if (bounds == null) {
            finishFifthQuestIntroCameraSequence();
            return;
        }
        fifthQuestIntroCameraStart.set(camera.position.x, camera.position.y);
        fifthQuestIntroCameraTarget.set(
                bounds.x + bounds.width * 0.5f,
                bounds.y + bounds.height * 0.5f);
        fifthQuestIntroCameraStartZoom = camera.zoom;
        fifthQuestIntroCameraTimer = FIFTH_QUEST_INTRO_CAMERA_SEGMENT_SECONDS;
        player.moving = false;
    }

    private void updateFifthQuestIntroCameraSequence(float delta) {
        if (!isFifthQuestIntroCameraActive() || camera == null || !MAP_ID_VILLAGE.equals(world.activeMapId)) {
            return;
        }
        float safeDelta = Math.max(0f, delta);
        float duration = Math.max(0.001f, FIFTH_QUEST_INTRO_CAMERA_SEGMENT_SECONDS);
        fifthQuestIntroCameraTimer = Math.max(0f, fifthQuestIntroCameraTimer - safeDelta);
        float progress = clamp01(1f - (fifthQuestIntroCameraTimer / duration));
        float easedProgress = progress * progress * (3f - 2f * progress);
        camera.position.x = lerp(fifthQuestIntroCameraStart.x, fifthQuestIntroCameraTarget.x, easedProgress);
        camera.position.y = lerp(fifthQuestIntroCameraStart.y, fifthQuestIntroCameraTarget.y, easedProgress);
        camera.position.z = 0f;
        camera.zoom = lerp(fifthQuestIntroCameraStartZoom, DEFAULT_ZOOM, easedProgress);
        if (Math.abs(camera.zoom - DEFAULT_ZOOM) < 0.0005f) {
            camera.zoom = DEFAULT_ZOOM;
        }
        player.moving = false;

        if (fifthQuestIntroCameraTimer > 0f) {
            return;
        }

        switch (fifthQuestIntroCameraPhase) {
            case CAMERA_1 -> beginFifthQuestIntroCameraSegment(FifthQuestIntroCameraPhase.CAMERA_2);
            case CAMERA_2 -> beginFifthQuestIntroCameraSegment(FifthQuestIntroCameraPhase.CAMERA_3);
            case CAMERA_3 -> finishFifthQuestIntroCameraSequence();
            default -> {
            }
        }
    }

    private void finishFifthQuestIntroCameraSequence() {
        fifthQuestIntroCameraPhase = FifthQuestIntroCameraPhase.NONE;
        fifthQuestIntroCameraTimer = 0f;
        float playerTargetX = player.bounds.x + player.bounds.width * 0.5f;
        float playerTargetY = player.bounds.y + player.bounds.height * 0.5f;
        startCameraTeleportSmoothing(playerTargetX, playerTargetY, 1.1f);
        beginFifthQuestAnchorBattle(0);
    }

    private void startFifthQuestFirstEncounterIntro(int encounterId) {
        if (camera == null
                || !MAP_ID_VILLAGE.equals(world.activeMapId)
                || encounterId == FIFTH_QUEST_ENCOUNTER_NONE
                || !resolveFifthQuestEncounterFocusPoint(encounterId, fifthQuestFirstEncounterIntroFocus)) {
            return;
        }
        fifthQuestFirstEncounterIntroPlayed = true;
        fifthQuestFirstEncounterIntroActive = true;
        fifthQuestFirstEncounterIntroTimer = FIFTH_QUEST_FIRST_ENCOUNTER_INTRO_SECONDS;
        fifthQuestFirstEncounterIntroEncounterId = encounterId;
        player.moving = false;
        fifthQuestZombieSfxCooldownTimer = 0f;
        playFifthQuestZombieSfx(FIFTH_QUEST_ZOMBIE_INTRO_SFX_VOLUME_MULTIPLIER);
    }

    private void updateFifthQuestFirstEncounterIntro(float delta) {
        if (!isFifthQuestFirstEncounterIntroActive()) {
            return;
        }
        if (camera == null
                || !MAP_ID_VILLAGE.equals(world.activeMapId)
                || !resolveFifthQuestEncounterFocusPoint(
                fifthQuestFirstEncounterIntroEncounterId,
                fifthQuestFirstEncounterIntroFocus)) {
            stopFifthQuestFirstEncounterIntro();
            return;
        }

        float safeDelta = Math.max(0f, delta);
        float alpha = 1f - (float) Math.exp(-DIALOGUE_CAMERA_SMOOTH_SPEED * safeDelta);
        camera.position.x = lerp(camera.position.x, fifthQuestFirstEncounterIntroFocus.x, alpha);
        camera.position.y = lerp(camera.position.y, fifthQuestFirstEncounterIntroFocus.y, alpha);
        camera.position.z = 0f;
        float targetZoom = DEFAULT_ZOOM * FIFTH_QUEST_FIRST_ENCOUNTER_INTRO_ZOOM_MULTIPLIER;
        camera.zoom = lerp(camera.zoom, targetZoom, alpha);
        if (Math.abs(camera.zoom - targetZoom) < 0.0005f) {
            camera.zoom = targetZoom;
        }

        player.moving = false;
        fifthQuestFirstEncounterIntroTimer = Math.max(0f, fifthQuestFirstEncounterIntroTimer - safeDelta);
        if (fifthQuestFirstEncounterIntroTimer <= 0f) {
            stopFifthQuestFirstEncounterIntro();
        }
    }

    private void stopFifthQuestFirstEncounterIntro() {
        fifthQuestFirstEncounterIntroActive = false;
        fifthQuestFirstEncounterIntroTimer = 0f;
        fifthQuestFirstEncounterIntroEncounterId = FIFTH_QUEST_ENCOUNTER_NONE;
        if (camera != null) {
            camera.zoom = DEFAULT_ZOOM;
        }
    }

    private boolean resolveFifthQuestEncounterFocusPoint(int encounterId, Vector2 focusOut) {
        if (focusOut == null || encounterId == FIFTH_QUEST_ENCOUNTER_NONE) {
            return false;
        }
        float sumX = 0f;
        float sumY = 0f;
        int count = 0;
        for (int i = 0; i < fifthQuestDemons.size; i++) {
            FifthQuestDemonInstance demon = fifthQuestDemons.get(i);
            if (demon.questEncounterId != encounterId || demon.dying || demon.health <= 0f) {
                continue;
            }
            sumX += demon.position.x;
            sumY += demon.position.y;
            count++;
        }
        if (count <= 0) {
            return false;
        }
        focusOut.set(sumX / count, sumY / count);
        return true;
    }

    private Rectangle currentFifthQuestIntroCameraBounds() {
        return switch (fifthQuestIntroCameraPhase) {
            case CAMERA_1 -> hasFifthQuestIntroCameraBounds(0) ? fifthQuestIntroCameraBounds[0] : null;
            case CAMERA_2 -> hasFifthQuestIntroCameraBounds(1) ? fifthQuestIntroCameraBounds[1] : null;
            case CAMERA_3 -> hasFifthQuestIntroCameraBounds(2) ? fifthQuestIntroCameraBounds[2] : null;
            default -> null;
        };
    }

    private boolean hasFifthQuestIntroCameraBounds(int index) {
        return index >= 0
                && index < fifthQuestIntroCameraBounds.length
                && fifthQuestIntroCameraBounds[index].width > 0f
                && fifthQuestIntroCameraBounds[index].height > 0f;
    }

    private void updateBossArenaHudReveal(float delta) {
        if (!MAP_ID_BOSS_ARENA_PHASE_2.equals(world.activeMapId)) {
            bossArenaHudRevealTimer = 0f;
            return;
        }
        if (isBossArenaFinalIntroActive()) {
            bossArenaHudRevealTimer = 0f;
            return;
        }
        BossArenaPhase2DemonInstance boss = findBossArenaPhase2Leader();
        if (boss == null || boss.health <= 0f) {
            bossArenaHudRevealTimer = 0f;
            return;
        }
        bossArenaHudRevealTimer = Math.min(
                BOSS_ARENA_HUD_FADE_IN_SECONDS,
                bossArenaHudRevealTimer + Math.max(0f, delta));
    }

    private boolean resolveBossArenaFinalIntroCameraTarget() {
        BossArenaPhase2DemonInstance boss = findBossArenaPhase2Leader();
        if (boss != null && boss.health > 0f) {
            bossArenaFinalIntroCameraTarget.set(
                    bossArenaPhase2DemonHitboxCenterX(boss),
                    bossArenaPhase2DemonHitboxCenterY(boss));
            return true;
        }
        ensureSixthQuestFinalArenaLayout();
        return setVectorToRectCenter(bossArenaFinalIntroCameraTarget, sixthQuestFinalPhaseSpawnBounds);
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
        boolean playerInsideZone = isPlayerInsideZoneBounds(bossFightZoneBounds);
        if (!bossFightZoneDarkMode && playerInsideZone) {
            bossFightZoneDarkMode = true;
        }
        if (!boss1IntroPlayed
                && !isBoss1IntroActive()
                && playerInsideZone) {
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
        boolean playerInside = isPlayerInsideZoneBounds(golemFightZoneBounds);
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

    private boolean hasLivingBatInZone(Rectangle zoneBounds) {
        if (zoneBounds == null || zoneBounds.width <= 0f || zoneBounds.height <= 0f) {
            return false;
        }
        for (int i = 0; i < bats.size; i++) {
            BatInstance bat = bats.get(i);
            if (bat == null || bat.health <= 0f || bat.state == BatAnimationState.DIE) {
                continue;
            }
            if (zoneBounds.contains(bat.position.x, bat.position.y)) {
                return true;
            }
        }
        return false;
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
        if (bossFightZoneBounds.width <= 0f || bossFightZoneBounds.height <= 0f) {
            return false;
        }
        float centerX = rect.x + rect.width * 0.5f;
        float centerY = rect.y + rect.height * 0.5f;
        return bossFightZoneBounds.contains(centerX, centerY);
    }

    private boolean isPlayerInsideZoneBounds(Rectangle zoneBounds) {
        if (zoneBounds == null || zoneBounds.width <= 0f || zoneBounds.height <= 0f) {
            return false;
        }
        float centerX = player.bounds.x + player.bounds.width * 0.5f;
        float centerY = player.bounds.y + player.bounds.height * 0.5f;
        return zoneBounds.contains(centerX, centerY);
    }

    private void startRoom3DoorClosing() {
        int frameCount = room3DoorFrameCount();
        if (frameCount == 0 || room3DoorClosed) {
            return;
        }
        room3DoorClosing = true;
        room3DoorAnimationTimer = 0f;
        room3DoorCollisionEnabled = false;
        // Room-3 trigger should play from DOOR-1 up to the last frame.
        room3DoorAnimationFrameIndex = 0;
        setRoom3DoorFrameVisible(room3DoorAnimationFrameIndex);
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

    private boolean isPlayerWithinRoom3DoorTrigger() {
        return isPlayerWithinObjectLayer(room3DoorTriggerLayer)
                || isPlayerNearAnyRoom3DoorTriggerZone();
    }

    private boolean isPlayerNearAnyRoom3DoorTriggerZone() {
        if (room3DoorTriggerZoneBounds.size == 0) {
            return false;
        }
        float playerCenterX = player.bounds.x + player.bounds.width * 0.5f;
        float playerCenterY = player.bounds.y + player.bounds.height * 0.5f;
        final float proximityX = 20f;
        final float proximityY = 36f;
        for (int i = 0; i < room3DoorTriggerZoneBounds.size; i++) {
            Rectangle zone = room3DoorTriggerZoneBounds.get(i);
            float minX = zone.x - proximityX;
            float maxX = zone.x + zone.width + proximityX;
            float minY = zone.y - proximityY;
            float maxY = zone.y + zone.height + proximityY;
            if (playerCenterX >= minX && playerCenterX <= maxX
                    && playerCenterY >= minY && playerCenterY <= maxY) {
                return true;
            }
        }
        return false;
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

    private boolean hasRoom3DoorCollisionBounds() {
        return room3DoorClosedCollisionBounds.width > 0f && room3DoorClosedCollisionBounds.height > 0f;
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

    private int room3DoorFrameCount() {
        return room3DoorAnimationFrames.size;
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

    private void setRoom3DoorFrameVisible(int frameIndex) {
        setDoorFrameArrayVisible(room3DoorAnimationFrames, frameIndex);
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

    private void appendTileBoundsFromLayer(MapLayer layer, Array<Rectangle> outBounds) {
        if (!(layer instanceof TiledMapTileLayer tileLayer)) {
            return;
        }
        float tileWidth = tileLayer.getTileWidth();
        float tileHeight = tileLayer.getTileHeight();
        float offsetX = layerOffsetX(tileLayer);
        float offsetY = layerOffsetY(tileLayer);
        int width = tileLayer.getWidth();
        int height = tileLayer.getHeight();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                TiledMapTileLayer.Cell cell = tileLayer.getCell(x, y);
                if (cell == null || cell.getTile() == null) {
                    continue;
                }
                outBounds.add(new Rectangle(
                        offsetX + x * tileWidth,
                        offsetY + y * tileHeight,
                        tileWidth,
                        tileHeight));
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
        if (world.map == null || name == null || name.isBlank()) {
            return null;
        }
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
        if (layers == null || name == null || name.isBlank()) {
            return null;
        }
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
        resetPlayerDeathState();
        clearFifthQuestDeathChoice();
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

    private void updatePlayerAbilityCooldowns(float delta) {
        float safeDelta = Math.max(0f, delta);
        if (dashAttackAbilityCooldownTimer > 0f) {
            dashAttackAbilityCooldownTimer = Math.max(0f, dashAttackAbilityCooldownTimer - safeDelta);
        }
        if (spatialDashAbilityCooldownTimer > 0f) {
            spatialDashAbilityCooldownTimer = Math.max(0f, spatialDashAbilityCooldownTimer - safeDelta);
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
        // Automatic cycling is disabled. Time-of-day now changes only through explicit controls.
    }

    private void updatePlayerStamina(float delta) {
        if (delta <= 0f || isPlayerDead() || player.currentMana >= player.maxMana) {
            return;
        }
        player.currentMana = Math.min(player.maxMana, player.currentMana + STAMINA_REGEN_PER_SECOND * delta);
    }

    private void updatePlayerHealth(float delta) {
        if (delta <= 0f || isPlayerDead() || player.currentHealth >= player.maxHealth) {
            return;
        }
        player.currentHealth = Math.min(player.maxHealth, player.currentHealth + HEALTH_REGEN_PER_SECOND * delta);
    }

    private void updateFirstQuest(float delta) {
        firstQuestMarkerTimer += Math.max(0f, delta);
        firstQuestCafeNpcStateTime += Math.max(0f, delta);
        updateSixthQuestEndingNpcFade(delta);
        updateFifthQuestPortalDialogueNpcs(delta);
        updateSixthQuestEndingVideoSequence(delta);
        questGuidanceSuppressTimer = Math.max(0f, questGuidanceSuppressTimer - Math.max(0f, delta));
        updateThirdQuest(delta);
        updateFourthQuest(delta);
        updateFifthQuest(delta);
        updateSixthQuest(delta);
        refreshFirstQuestGuidance();
    }

    private void updateSixthQuestEndingNpcFade(float delta) {
        if (!sixthQuestEndingNpcFadeActive) {
            if (sixthQuestStage < SIXTH_QUEST_ENDING) {
                sixthQuestEndingNpcFadeTimer = 0f;
            }
            return;
        }
        sixthQuestEndingNpcFadeTimer = Math.min(
                SIXTH_QUEST_ENDING_NPC_FADE_OUT_SECONDS,
                sixthQuestEndingNpcFadeTimer + Math.max(0f, delta));
    }

    private void updateFifthQuestPortalDialogueNpcs(float delta) {
        boolean shouldShowNpcs = isFifthQuestPortalDialogueSceneActive();
        if (!shouldShowNpcs) {
            fifthQuestPortalDialogueNpcFadeTimer = 0f;
            fifthQuestPortalDialogueNpcVisibleLastFrame = false;
            return;
        }
        if (!fifthQuestPortalDialogueNpcVisibleLastFrame) {
            fifthQuestPortalDialogueNpcFadeTimer = 0f;
            fifthQuestPortalDialogueNpcVisibleLastFrame = true;
        } else {
            fifthQuestPortalDialogueNpcFadeTimer = Math.min(
                    FIFTH_QUEST_PORTAL_DIALOGUE_NPC_FADE_IN_SECONDS,
                    fifthQuestPortalDialogueNpcFadeTimer + Math.max(0f, delta));
        }
    }

    private void updateSixthQuestEndingVideoSequence(float delta) {
        if (!sixthQuestEndingVideoSequenceActive || sixthQuestEndingVideoTriggered) {
            if (!sixthQuestEndingVideoSequenceActive && sixthQuestStage < SIXTH_QUEST_ENDING) {
                sixthQuestEndingVideoSequenceTimer = 0f;
            }
            return;
        }
        sixthQuestEndingVideoSequenceTimer += Math.max(0f, delta);
        float totalDuration = SIXTH_QUEST_ENDING_VIDEO_DELAY_SECONDS + SIXTH_QUEST_ENDING_VIDEO_FADE_SECONDS;
        if (sixthQuestEndingVideoSequenceTimer < totalDuration) {
            return;
        }
        sixthQuestEndingVideoTriggered = true;
        if (sixthQuestEndingMusic != null && sixthQuestEndingMusicPlaying) {
            game.handoffPersistentMusic(sixthQuestEndingMusic, SIXTH_QUEST_ENDING_MUSIC_BASE_VOLUME);
            sixthQuestEndingMusic = null;
            sixthQuestEndingMusicPlaying = false;
            sixthQuestEndingMusicFadeProgress = 0f;
        }
        game.playEndingVideo();
    }

    private void updateFifthQuestLabVillagers(float delta) {
        boolean shouldShowVillagers = MAP_ID_RESEARCH_LAB_FLOOR_1.equals(world.activeMapId)
                && (fifthQuestStage == FIFTH_QUEST_GO_TO_ABANDONED_VILLAGE
                || activeQuestDialogueId == QuestDialogueId.FIFTH_QUEST_LAB_ALARM);
        if (!shouldShowVillagers) {
            fifthQuestLabVillagersFadeTimer = 0f;
            fifthQuestLabVillagersVisibleLastFrame = false;
            return;
        }
        if (!fifthQuestLabVillagersVisibleLastFrame) {
            fifthQuestLabVillagersFadeTimer = 0f;
            fifthQuestLabVillagersVisibleLastFrame = true;
        } else {
            fifthQuestLabVillagersFadeTimer = Math.min(
                    FIFTH_QUEST_LAB_VILLAGER_FADE_IN_SECONDS,
                    fifthQuestLabVillagersFadeTimer + Math.max(0f, delta));
        }
    }

    private void refreshFirstQuestGuidance() {
        if (hudRenderer == null || isFirstQuestOverlayActive() || questGuidanceSuppressTimer > 0f) {
            return;
        }
        if (firstQuestStage == FIRST_QUEST_DELIVER_TO_CAFE && MAP_ID_INN_ROOM.equals(world.activeMapId)) {
            hudRenderer.showPersistentNotification(FIRST_QUEST_NOTIFICATION_TITLE, "Leave the room.");
            return;
        }
        if (firstQuestStage == FIRST_QUEST_DELIVER_TO_CAFE && MAP_ID_INN.equals(world.activeMapId)) {
            hudRenderer.showPersistentNotification(FIRST_QUEST_NOTIFICATION_TITLE, "Leave the inn.");
            return;
        }
        if (firstQuestStage == FIRST_QUEST_DELIVER_TO_CAFE && MAP_ID_VILLAGE.equals(world.activeMapId)) {
            hudRenderer.showPersistentNotification(FIRST_QUEST_NOTIFICATION_TITLE,
                    "Bring the Herb-and-Spice to the bakery owner outside the bakery.");
            return;
        }
        if (secondQuestStage == SECOND_QUEST_TALK_TO_CAFE_OWNER && MAP_ID_VILLAGE.equals(world.activeMapId)) {
            hudRenderer.showPersistentNotification(SECOND_QUEST_NOTIFICATION_TITLE, "Speak with the bakery owner.");
            return;
        }
        if (secondQuestStage == SECOND_QUEST_TAKE_FRAGMENT_TO_SCIENTIST) {
            if (MAP_ID_VILLAGE.equals(world.activeMapId)) {
                hudRenderer.showPersistentNotification(SECOND_QUEST_NOTIFICATION_TITLE,
                        "Bring the Scorched Fragment to the Research Lab.");
                return;
            }
            if (MAP_ID_RESEARCH_LAB_FLOOR_1.equals(world.activeMapId)) {
                hudRenderer.showPersistentNotification(SECOND_QUEST_NOTIFICATION_TITLE,
                        "Show the fragment to the scientist.");
                return;
            }
        }
        if (secondQuestStage == SECOND_QUEST_RETRIEVE_ARCHIVE_NOTES) {
            if (MAP_ID_RESEARCH_LAB_FLOOR_1.equals(world.activeMapId)) {
                hudRenderer.showPersistentNotification(SECOND_QUEST_NOTIFICATION_TITLE,
                        "Go upstairs to Research Lab Floor 2.");
                return;
            }
            if (MAP_ID_RESEARCH_LAB_FLOOR_2.equals(world.activeMapId)) {
                hudRenderer.showPersistentNotification(SECOND_QUEST_NOTIFICATION_TITLE,
                        "Retrieve the Archive Notes from the worktable.");
                return;
            }
        }
        if (secondQuestStage == SECOND_QUEST_RETURN_TO_SCIENTIST) {
            if (MAP_ID_RESEARCH_LAB_FLOOR_2.equals(world.activeMapId)) {
                hudRenderer.showPersistentNotification(SECOND_QUEST_NOTIFICATION_TITLE,
                        "Return downstairs to the scientist.");
                return;
            }
            if (MAP_ID_RESEARCH_LAB_FLOOR_1.equals(world.activeMapId)) {
                hudRenderer.showPersistentNotification(SECOND_QUEST_NOTIFICATION_TITLE,
                        "Bring the Archive Notes to the scientist.");
                return;
            }
        }
        if (thirdQuestStage == THIRD_QUEST_RETURN_TO_FATHER) {
            if (MAP_ID_RESEARCH_LAB_FLOOR_1.equals(world.activeMapId)) {
                hudRenderer.showPersistentNotification(THIRD_QUEST_NOTIFICATION_TITLE, "Leave the Research Lab.");
                return;
            }
            if (MAP_ID_VILLAGE.equals(world.activeMapId)) {
                hudRenderer.showPersistentNotification(THIRD_QUEST_NOTIFICATION_TITLE, "Return to the inn.");
                return;
            }
            if (MAP_ID_INN.equals(world.activeMapId)) {
                hudRenderer.showPersistentNotification(THIRD_QUEST_NOTIFICATION_TITLE, "Go back to the inn room.");
                return;
            }
            if (MAP_ID_INN_ROOM.equals(world.activeMapId)) {
                hudRenderer.showPersistentNotification(THIRD_QUEST_NOTIFICATION_TITLE, "Speak with Father.");
                return;
            }
        }
        if (thirdQuestStage == THIRD_QUEST_MEET_FATHER_OUTSIDE) {
            if (MAP_ID_INN_ROOM.equals(world.activeMapId)) {
                hudRenderer.showPersistentNotification(THIRD_QUEST_NOTIFICATION_TITLE, "Leave the room.");
                return;
            }
            if (MAP_ID_INN.equals(world.activeMapId)) {
                hudRenderer.showPersistentNotification(THIRD_QUEST_NOTIFICATION_TITLE, "Leave the inn.");
                return;
            }
            if (MAP_ID_VILLAGE.equals(world.activeMapId)) {
                hudRenderer.showPersistentNotification(THIRD_QUEST_NOTIFICATION_TITLE, "Meet Father behind the inn.");
                return;
            }
        }
        if (thirdQuestStage == THIRD_QUEST_MOVEMENT_TUTORIAL && MAP_ID_VILLAGE.equals(world.activeMapId)) {
            hudRenderer.showPersistentNotification(
                    THIRD_QUEST_NOTIFICATION_TITLE,
                    "Reach all training markers (" + thirdQuestMovementMarkersReached + "/" + thirdQuestMovementMarkers.length + ").");
            return;
        }
        if (thirdQuestStage == THIRD_QUEST_ATTACK_TUTORIAL && MAP_ID_VILLAGE.equals(world.activeMapId)) {
            hudRenderer.showPersistentNotification(
                    THIRD_QUEST_NOTIFICATION_TITLE,
                    "Hit the training dummy (" + thirdQuestAttackDummyHitCount + "/" + THIRD_QUEST_DUMMY_HITS_REQUIRED + ").");
            return;
        }
        if (thirdQuestStage == THIRD_QUEST_COMBAT_TUTORIAL && MAP_ID_VILLAGE.equals(world.activeMapId)) {
            hudRenderer.showPersistentNotification(
                    THIRD_QUEST_NOTIFICATION_TITLE,
                    "Defeat the training shades and wait for safe openings.");
            return;
        }
        if (thirdQuestStage == THIRD_QUEST_SPAR_WITH_FATHER && MAP_ID_VILLAGE.equals(world.activeMapId)) {
            hudRenderer.showPersistentNotification(
                    THIRD_QUEST_NOTIFICATION_TITLE,
                    thirdQuestSparActive
                            ? "Spar with Father. Stay calm and strike with intent."
                            : "Speak with Father.");
            return;
        }
        if (fourthQuestStage == FOURTH_QUEST_REPORT_TO_SCIENTIST) {
            if (MAP_ID_VILLAGE.equals(world.activeMapId)) {
                hudRenderer.showPersistentNotification(FOURTH_QUEST_NOTIFICATION_TITLE, "Head to the Research Lab.");
                return;
            }
            if (MAP_ID_RESEARCH_LAB_FLOOR_1.equals(world.activeMapId)) {
                hudRenderer.showPersistentNotification(FOURTH_QUEST_NOTIFICATION_TITLE, "Report to the scientist.");
                return;
            }
        }
        if (fourthQuestStage == FOURTH_QUEST_TALK_TO_CAFE_OWNER) {
            if (MAP_ID_RESEARCH_LAB_FLOOR_1.equals(world.activeMapId)) {
                hudRenderer.showPersistentNotification(FOURTH_QUEST_NOTIFICATION_TITLE, "Leave the Research Lab.");
                return;
            }
            if (MAP_ID_VILLAGE.equals(world.activeMapId)) {
                hudRenderer.showPersistentNotification(FOURTH_QUEST_NOTIFICATION_TITLE, "Investigate the corrupted site outside the bakery.");
                return;
            }
        }
        if (fourthQuestStage == FOURTH_QUEST_EXAMINE_CAFE_MARK && MAP_ID_VILLAGE.equals(world.activeMapId)) {
            hudRenderer.showPersistentNotification(FOURTH_QUEST_NOTIFICATION_TITLE, "Examine the corrupted mark outside the bakery.");
            return;
        }
        if (fourthQuestStage == FOURTH_QUEST_CAFE_BATTLE && MAP_ID_VILLAGE.equals(world.activeMapId)) {
            hudRenderer.showPersistentNotification(FOURTH_QUEST_NOTIFICATION_TITLE, "Defeat the corrupted enemies near the bakery.");
            return;
        }
        if (fourthQuestStage == FOURTH_QUEST_EXAMINE_CENTER_MARK && MAP_ID_VILLAGE.equals(world.activeMapId)) {
            hudRenderer.showPersistentNotification(FOURTH_QUEST_NOTIFICATION_TITLE, "Investigate the corrupted site in the village center.");
            return;
        }
        if (fourthQuestStage == FOURTH_QUEST_CENTER_BATTLE && MAP_ID_VILLAGE.equals(world.activeMapId)) {
            hudRenderer.showPersistentNotification(FOURTH_QUEST_NOTIFICATION_TITLE, "Clear the corruption threatening the village center.");
            return;
        }
        if (fourthQuestStage == FOURTH_QUEST_EXAMINE_EDGE_MARK && MAP_ID_VILLAGE.equals(world.activeMapId)) {
            hudRenderer.showPersistentNotification(FOURTH_QUEST_NOTIFICATION_TITLE, "Investigate the final corrupted site near the outer road.");
            return;
        }
        if (fourthQuestStage == FOURTH_QUEST_EDGE_BATTLE && MAP_ID_VILLAGE.equals(world.activeMapId)) {
            hudRenderer.showPersistentNotification(FOURTH_QUEST_NOTIFICATION_TITLE, "Defeat the corrupted enemies at the village edge.");
            return;
        }
        if (fourthQuestStage == FOURTH_QUEST_RETURN_TO_SCIENTIST) {
            if (MAP_ID_VILLAGE.equals(world.activeMapId)) {
                hudRenderer.showPersistentNotification(FOURTH_QUEST_NOTIFICATION_TITLE, "Bring the samples back to the scientist.");
                return;
            }
            if (MAP_ID_RESEARCH_LAB_FLOOR_1.equals(world.activeMapId)) {
                hudRenderer.showPersistentNotification(FOURTH_QUEST_NOTIFICATION_TITLE, "Deliver the samples to the scientist.");
                return;
            }
        }
        if (fourthQuestStage == FOURTH_QUEST_GO_TO_ARRAY) {
            if (MAP_ID_RESEARCH_LAB_FLOOR_1.equals(world.activeMapId)) {
                hudRenderer.showPersistentNotification(FOURTH_QUEST_NOTIFICATION_TITLE, "Go upstairs and activate the Resonance Array.");
                return;
            }
            if (MAP_ID_RESEARCH_LAB_FLOOR_2.equals(world.activeMapId)) {
                hudRenderer.showPersistentNotification(FOURTH_QUEST_NOTIFICATION_TITLE, "Find the Resonance Array controls.");
                return;
            }
        }
        if (fourthQuestStage == FOURTH_QUEST_ACTIVATE_ARRAY_NODE_1 && MAP_ID_RESEARCH_LAB_FLOOR_2.equals(world.activeMapId)) {
            hudRenderer.showPersistentNotification(FOURTH_QUEST_NOTIFICATION_TITLE, "Activate the first outer array node.");
            return;
        }
        if (fourthQuestStage == FOURTH_QUEST_ACTIVATE_ARRAY_NODE_2 && MAP_ID_RESEARCH_LAB_FLOOR_2.equals(world.activeMapId)) {
            hudRenderer.showPersistentNotification(FOURTH_QUEST_NOTIFICATION_TITLE, "Activate the second outer array node.");
            return;
        }
        if (fourthQuestStage == FOURTH_QUEST_ACTIVATE_ARRAY_NODE_3 && MAP_ID_RESEARCH_LAB_FLOOR_2.equals(world.activeMapId)) {
            hudRenderer.showPersistentNotification(FOURTH_QUEST_NOTIFICATION_TITLE, "Activate the third outer array node.");
            return;
        }
        if (fourthQuestStage == FOURTH_QUEST_ACTIVATE_ARRAY_CONSOLE && MAP_ID_RESEARCH_LAB_FLOOR_2.equals(world.activeMapId)) {
            hudRenderer.showPersistentNotification(FOURTH_QUEST_NOTIFICATION_TITLE, "Return to the main console and begin the scan.");
            return;
        }
        if (fourthQuestStage == FOURTH_QUEST_ARRAY_DEFENSE && MAP_ID_RESEARCH_LAB_FLOOR_2.equals(world.activeMapId)) {
            hudRenderer.showPersistentNotification(
                    FOURTH_QUEST_NOTIFICATION_TITLE,
                    "Protect the Resonance Array until the scan completes.");
            return;
        }
        if (fourthQuestStage == FOURTH_QUEST_GO_TO_BREACH) {
            if (MAP_ID_RESEARCH_LAB_FLOOR_2.equals(world.activeMapId)) {
                hudRenderer.showPersistentNotification(FOURTH_QUEST_NOTIFICATION_TITLE, "Go back downstairs.");
                return;
            }
            if (MAP_ID_RESEARCH_LAB_FLOOR_1.equals(world.activeMapId)) {
                hudRenderer.showPersistentNotification(FOURTH_QUEST_NOTIFICATION_TITLE, "Leave the Research Lab and head to the breach site.");
                return;
            }
            if (MAP_ID_VILLAGE.equals(world.activeMapId)) {
                hudRenderer.showPersistentNotification(FOURTH_QUEST_NOTIFICATION_TITLE, "Go to the breach forming near the outer road.");
                return;
            }
        }
        if (fourthQuestStage == FOURTH_QUEST_EXAMINE_BREACH && MAP_ID_VILLAGE.equals(world.activeMapId)) {
            hudRenderer.showPersistentNotification(FOURTH_QUEST_NOTIFICATION_TITLE, "Investigate the breach site.");
            return;
        }
        if (fourthQuestStage == FOURTH_QUEST_BREACH_BATTLE && MAP_ID_VILLAGE.equals(world.activeMapId)) {
            hudRenderer.showPersistentNotification(FOURTH_QUEST_NOTIFICATION_TITLE, "Defeat the Rift Beast before the breach expands.");
            return;
        }
        if (fourthQuestStage == FOURTH_QUEST_RETURN_RIFT_CORE) {
            if (MAP_ID_VILLAGE.equals(world.activeMapId)) {
                hudRenderer.showPersistentNotification(FOURTH_QUEST_NOTIFICATION_TITLE, "Bring the Rift Core to the scientist.");
                return;
            }
            if (MAP_ID_RESEARCH_LAB_FLOOR_1.equals(world.activeMapId)) {
                hudRenderer.showPersistentNotification(FOURTH_QUEST_NOTIFICATION_TITLE, "Give the Rift Core to the scientist.");
                return;
            }
        }
        if (fourthQuestStage == FOURTH_QUEST_EPILOGUE && MAP_ID_RESEARCH_LAB_FLOOR_1.equals(world.activeMapId)) {
            hudRenderer.showPersistentNotification(FOURTH_QUEST_NOTIFICATION_TITLE, "Speak with the scientist.");
            return;
        }
        if (fifthQuestStage == FIFTH_QUEST_GO_TO_ABANDONED_VILLAGE) {
            if (MAP_ID_RESEARCH_LAB_FLOOR_1.equals(world.activeMapId)) {
                hudRenderer.showPersistentNotification(FIFTH_QUEST_NOTIFICATION_TITLE, "Leave the Research Lab.");
                return;
            }
            if (MAP_ID_VILLAGE.equals(world.activeMapId)) {
                hudRenderer.showPersistentNotification(FIFTH_QUEST_NOTIFICATION_TITLE, "Go to the abandoned village by the portal.");
                return;
            }
        }
        if (fifthQuestStage == FIFTH_QUEST_OUTER_RUINS_BATTLE && MAP_ID_VILLAGE.equals(world.activeMapId)) {
            hudRenderer.showPersistentNotification(FIFTH_QUEST_NOTIFICATION_TITLE, "Defeat all demons in the outer ruins.");
            return;
        }
        if (fifthQuestStage == FIFTH_QUEST_ADVANCE_TO_PORTAL && MAP_ID_VILLAGE.equals(world.activeMapId)) {
            hudRenderer.showPersistentNotification(FIFTH_QUEST_NOTIFICATION_TITLE, "Advance deeper toward the portal.");
            return;
        }
        if (fifthQuestStage == FIFTH_QUEST_MID_RUINS_BATTLE && MAP_ID_VILLAGE.equals(world.activeMapId)) {
            hudRenderer.showPersistentNotification(FIFTH_QUEST_NOTIFICATION_TITLE, "Defeat the demons blocking the path.");
            return;
        }
        if ((fifthQuestStage == FIFTH_QUEST_ANCHOR_1_BATTLE
                || fifthQuestStage == FIFTH_QUEST_ANCHOR_2_BATTLE
                || fifthQuestStage == FIFTH_QUEST_ANCHOR_3_BATTLE)
                && MAP_ID_VILLAGE.equals(world.activeMapId)) {
            hudRenderer.showPersistentNotification(FIFTH_QUEST_NOTIFICATION_TITLE, "Defeat the demons guarding the anchor.");
            return;
        }
        if ((fifthQuestStage == FIFTH_QUEST_ANCHOR_1_DESTROY
                || fifthQuestStage == FIFTH_QUEST_ANCHOR_2_DESTROY
                || fifthQuestStage == FIFTH_QUEST_ANCHOR_3_DESTROY)
                && MAP_ID_VILLAGE.equals(world.activeMapId)) {
            hudRenderer.showPersistentNotification(FIFTH_QUEST_NOTIFICATION_TITLE, "Use the interact key/button to destroy the anchor.");
            return;
        }
        if (fifthQuestStage == FIFTH_QUEST_PORTAL_BOSS && MAP_ID_VILLAGE.equals(world.activeMapId)) {
            hudRenderer.showPersistentNotification(FIFTH_QUEST_NOTIFICATION_TITLE, "Defeat the demon guarding the portal.");
            return;
        }
        if (fifthQuestStage == FIFTH_QUEST_COMPLETE
                && sixthQuestStage <= SIXTH_QUEST_ENTER_PORTAL
                && MAP_ID_VILLAGE.equals(world.activeMapId)) {
            hudRenderer.showPersistentNotification(SIXTH_QUEST_NOTIFICATION_TITLE, "Go to the dormant portal.");
            return;
        }
        if (sixthQuestStage >= SIXTH_QUEST_ENTER_PORTAL
                && sixthQuestStage < SIXTH_QUEST_ENDING
                && MAP_ID_VILLAGE.equals(world.activeMapId)) {
            hudRenderer.showPersistentNotification(SIXTH_QUEST_NOTIFICATION_TITLE,
                    sixthQuestPortalChoiceVisible
                            ? "Choose whether to enter the final stage."
                            : (isSixthQuestPortalActivationRunning()
                            ? "The portal is opening."
                            : "Go to the dormant portal."));
            return;
        }
        if (sixthQuestStage == SIXTH_QUEST_OUTER_ASSAULT && MAP_ID_DEMON_CASTLE.equals(world.activeMapId)) {
            hudRenderer.showPersistentNotification(SIXTH_QUEST_NOTIFICATION_TITLE, "Defeat the demons blocking the route to the inner gate.");
            return;
        }
        if (sixthQuestStage == SIXTH_QUEST_SEAL_A_BATTLE && MAP_ID_DEMON_CASTLE.equals(world.activeMapId)) {
            hudRenderer.showPersistentNotification(SIXTH_QUEST_NOTIFICATION_TITLE, "Break the first Castle Seal.");
            return;
        }
        if (sixthQuestStage == SIXTH_QUEST_SEAL_A_DESTROY && MAP_ID_DEMON_CASTLE.equals(world.activeMapId)) {
            hudRenderer.showPersistentNotification(SIXTH_QUEST_NOTIFICATION_TITLE, "Use the interact key/button to shatter the first seal.");
            return;
        }
        if (sixthQuestStage == SIXTH_QUEST_SEAL_B_BATTLE && MAP_ID_DEMON_CASTLE.equals(world.activeMapId)) {
            hudRenderer.showPersistentNotification(SIXTH_QUEST_NOTIFICATION_TITLE, "Break the second Castle Seal.");
            return;
        }
        if (sixthQuestStage == SIXTH_QUEST_SEAL_B_DESTROY && MAP_ID_DEMON_CASTLE.equals(world.activeMapId)) {
            hudRenderer.showPersistentNotification(SIXTH_QUEST_NOTIFICATION_TITLE, "Use the interact key/button to shatter the second seal.");
            return;
        }
        if (sixthQuestStage == SIXTH_QUEST_THRONE_GUARDS && MAP_ID_DEMON_CASTLE.equals(world.activeMapId)) {
            hudRenderer.showPersistentNotification(SIXTH_QUEST_NOTIFICATION_TITLE, "Advance to the Throne Hall and clear the demon guards.");
            return;
        }
        if (sixthQuestStage == SIXTH_QUEST_BOSS_PHASE_1 && MAP_ID_DEMON_CASTLE.equals(world.activeMapId)) {
            hudRenderer.showPersistentNotification(SIXTH_QUEST_NOTIFICATION_TITLE, "Confront the Demon in the throne hall.");
            return;
        }
        if ((sixthQuestStage == SIXTH_QUEST_FINAL_PHASE_INTRO || sixthQuestStage == SIXTH_QUEST_FINAL_PHASE_BATTLE)
                && MAP_ID_BOSS_ARENA_PHASE_2.equals(world.activeMapId)) {
            hudRenderer.showPersistentNotification(SIXTH_QUEST_NOTIFICATION_TITLE, "Survive the final phase and end the demon.");
            return;
        }
        if (sixthQuestStage == SIXTH_QUEST_COMPLETE || gameCompleted) {
            hudRenderer.showPersistentNotification("Game Complete", "The demon is gone. The village is safe.");
            return;
        }
        if (thirdQuestStage == THIRD_QUEST_COMPLETE && MAP_ID_VILLAGE.equals(world.activeMapId)) {
            hudRenderer.showPersistentNotification(NEXT_QUEST_NOTIFICATION_TITLE,
                    "Investigate the fresh demonic mark near the village.");
        }
    }

    private void updateThirdQuest(float delta) {
        if (thirdQuestStage <= THIRD_QUEST_DISABLED) {
            return;
        }
        if (MAP_ID_VILLAGE.equals(world.activeMapId)) {
            ensureThirdQuestTrainingLayout();
        }
        if (isFirstQuestOverlayActive()) {
            return;
        }
        switch (thirdQuestStage) {
            case THIRD_QUEST_MOVEMENT_TUTORIAL -> updateThirdQuestMovementTutorial();
            case THIRD_QUEST_ATTACK_TUTORIAL -> updateThirdQuestAttackTutorial();
            case THIRD_QUEST_COMBAT_TUTORIAL -> updateThirdQuestCombatTutorial();
            case THIRD_QUEST_SPAR_WITH_FATHER -> updateThirdQuestFatherSpar(delta);
            default -> {
                thirdQuestDummyHitRegistered = false;
                thirdQuestFatherHitRegistered = false;
            }
        }
    }

    private void updateFourthQuest(float delta) {
        if (fourthQuestStage <= FOURTH_QUEST_DISABLED) {
            return;
        }
        if (MAP_ID_VILLAGE.equals(world.activeMapId)) {
            ensureFourthQuestVillageLayout();
        } else if (MAP_ID_RESEARCH_LAB_FLOOR_2.equals(world.activeMapId)) {
            ensureFourthQuestFloor2Layout();
        }
        if (isFirstQuestOverlayActive()) {
            return;
        }
        switch (fourthQuestStage) {
            case FOURTH_QUEST_CAFE_BATTLE -> updateFourthQuestCafeBattle();
            case FOURTH_QUEST_CENTER_BATTLE -> updateFourthQuestCenterBattle();
            case FOURTH_QUEST_EDGE_BATTLE -> updateFourthQuestEdgeBattle();
            case FOURTH_QUEST_ARRAY_DEFENSE -> updateFourthQuestArrayDefense(delta);
            case FOURTH_QUEST_BREACH_BATTLE -> updateFourthQuestBreachBattle(delta);
            default -> {
            }
        }
    }

    private void updateFifthQuest(float delta) {
        if (fifthQuestStage <= FIFTH_QUEST_DISABLED) {
            return;
        }
        normalizeFifthQuestStageForThreeBattleFlow();
        if (MAP_ID_VILLAGE.equals(world.activeMapId)) {
            ensureFifthQuestVillageLayout();
        }
        if (isFirstQuestOverlayActive()) {
            return;
        }
        switch (fifthQuestStage) {
            case FIFTH_QUEST_GO_TO_ABANDONED_VILLAGE -> {
                if (MAP_ID_VILLAGE.equals(world.activeMapId)
                        && fifthQuestVillageLayoutReady
                        && !fifthQuestRuinsIntroTriggered
                        && overlapsPlayerExpanded(fifthQuestRuinsIntroBounds)) {
                    fifthQuestRuinsIntroTriggered = true;
                    startQuestDialogue(
                            QuestDialogueId.FIFTH_QUEST_RUINS_INTRO,
                            FIFTH_QUEST_RUINS_INTRO_SPEAKERS,
                            FIFTH_QUEST_RUINS_INTRO_DIALOGUE_LINES);
                }
            }
            case FIFTH_QUEST_ANCHOR_1_BATTLE -> updateFifthQuestAnchorBattle(0, FIFTH_QUEST_ENCOUNTER_ANCHOR_1);
            case FIFTH_QUEST_ANCHOR_2_BATTLE -> updateFifthQuestAnchorBattle(1, FIFTH_QUEST_ENCOUNTER_ANCHOR_2);
            case FIFTH_QUEST_ANCHOR_3_BATTLE -> updateFifthQuestAnchorBattle(2, FIFTH_QUEST_ENCOUNTER_ANCHOR_3);
            default -> {
            }
        }
    }

    private void normalizeFifthQuestStageForThreeBattleFlow() {
        fifthQuestStage = switch (fifthQuestStage) {
            case FIFTH_QUEST_OUTER_RUINS_BATTLE -> FIFTH_QUEST_ANCHOR_1_BATTLE;
            case FIFTH_QUEST_ADVANCE_TO_PORTAL, FIFTH_QUEST_MID_RUINS_BATTLE -> FIFTH_QUEST_ANCHOR_2_BATTLE;
            case FIFTH_QUEST_PORTAL_BOSS -> FIFTH_QUEST_ANCHOR_3_BATTLE;
            default -> fifthQuestStage;
        };
    }

    private void ensureFifthQuestVillageLayout() {
        if (!MAP_ID_VILLAGE.equals(world.activeMapId) || fifthQuestVillageLayoutReady) {
            return;
        }
        Rectangle portalBounds = objectLayerBounds(findLayerByName(FIFTH_QUEST_PORTAL_LAYER_NAME));
        if (portalBounds == null) {
            return;
        }
        fifthQuestPortalBounds.set(portalBounds);
        clampRectangleToMap(fifthQuestPortalBounds);
        fifthQuestIntroTriggerBounds.set(0f, 0f, 0f, 0f);
        for (Rectangle cameraBounds : fifthQuestIntroCameraBounds) {
            cameraBounds.set(0f, 0f, 0f, 0f);
        }

        if (setFifthQuestVillageAreasFromLayer()) {
            fifthQuestVillageLayoutReady = true;
            return;
        }

        Rectangle fightingAreaBounds = objectLayerBounds(findLayerByName(FIFTH_QUEST_FIGHTING_AREA_LAYER_NAME));
        if (fightingAreaBounds != null && fightingAreaBounds.width > 0f && fightingAreaBounds.height > 0f) {
            fifthQuestFightingAreaBounds.set(fightingAreaBounds);
            clampRectangleToMap(fifthQuestFightingAreaBounds);

            float outerWidth = Math.min(FIFTH_QUEST_OUTER_RUINS_WIDTH, fifthQuestFightingAreaBounds.width * 0.54f);
            float outerHeight = Math.min(FIFTH_QUEST_OUTER_RUINS_HEIGHT, fifthQuestFightingAreaBounds.height * 0.86f);
            float midWidth = Math.min(FIFTH_QUEST_MID_RUINS_WIDTH, fifthQuestFightingAreaBounds.width * 0.58f);
            float midHeight = Math.min(FIFTH_QUEST_MID_RUINS_HEIGHT, fifthQuestFightingAreaBounds.height * 0.88f);
            float introWidth = Math.min(108f, outerWidth);
            float introHeight = Math.min(90f, outerHeight);

            setRectangleInsideBounds(fifthQuestRuinsIntroBounds, fifthQuestFightingAreaBounds, 0.18f, 0.52f, introWidth, introHeight);
            setRectangleInsideBounds(fifthQuestOuterRuinsBounds, fifthQuestFightingAreaBounds, 0.34f, 0.52f, outerWidth, outerHeight);
            setRectangleInsideBounds(fifthQuestMidRuinsBounds, fifthQuestFightingAreaBounds, 0.60f, 0.52f, midWidth, midHeight);
            setRectangleInsideBounds(fifthQuestAnchorBounds[0], fifthQuestFightingAreaBounds, 0.42f, 0.52f, FIFTH_QUEST_ANCHOR_SIZE, FIFTH_QUEST_ANCHOR_SIZE);
            setRectangleInsideBounds(fifthQuestAnchorBounds[1], fifthQuestFightingAreaBounds, 0.60f, 0.52f, FIFTH_QUEST_ANCHOR_SIZE, FIFTH_QUEST_ANCHOR_SIZE);
            setRectangleInsideBounds(fifthQuestAnchorBounds[2], fifthQuestFightingAreaBounds, 0.76f, 0.52f, FIFTH_QUEST_ANCHOR_SIZE, FIFTH_QUEST_ANCHOR_SIZE);

            clampRectangleToMap(fifthQuestRuinsIntroBounds);
            clampRectangleToMap(fifthQuestOuterRuinsBounds);
            clampRectangleToMap(fifthQuestMidRuinsBounds);
            for (Rectangle anchorBounds : fifthQuestAnchorBounds) {
                clampRectangleToMap(anchorBounds);
            }

            fifthQuestVillageLayoutReady = true;
            return;
        }
        fifthQuestFightingAreaBounds.set(0f, 0f, 0f, 0f);

        float portalCenterX = portalBounds.x + portalBounds.width * 0.5f;
        float portalCenterY = portalBounds.y + portalBounds.height * 0.5f;
        setCenteredRectangle(
                fifthQuestRuinsIntroBounds,
                portalCenterX - 150f,
                portalCenterY + 18f,
                FIFTH_QUEST_OUTER_RUINS_WIDTH,
                FIFTH_QUEST_OUTER_RUINS_HEIGHT);
        setCenteredRectangle(
                fifthQuestOuterRuinsBounds,
                portalCenterX - 112f,
                portalCenterY + 22f,
                FIFTH_QUEST_OUTER_RUINS_WIDTH,
                FIFTH_QUEST_OUTER_RUINS_HEIGHT);
        setCenteredRectangle(
                fifthQuestMidRuinsBounds,
                portalCenterX - 24f,
                portalCenterY + 28f,
                FIFTH_QUEST_MID_RUINS_WIDTH,
                FIFTH_QUEST_MID_RUINS_HEIGHT);
        setCenteredRectangle(
                fifthQuestPortalBounds,
                portalCenterX + 14f,
                portalCenterY + 28f,
                FIFTH_QUEST_PORTAL_ZONE_WIDTH,
                FIFTH_QUEST_PORTAL_ZONE_HEIGHT);
        setCenteredRectangle(
                fifthQuestAnchorBounds[0],
                portalCenterX - 72f,
                portalCenterY + 80f,
                FIFTH_QUEST_ANCHOR_SIZE,
                FIFTH_QUEST_ANCHOR_SIZE);
        setCenteredRectangle(
                fifthQuestAnchorBounds[1],
                portalCenterX + 18f,
                portalCenterY + 92f,
                FIFTH_QUEST_ANCHOR_SIZE,
                FIFTH_QUEST_ANCHOR_SIZE);
        setCenteredRectangle(
                fifthQuestAnchorBounds[2],
                portalCenterX + 72f,
                portalCenterY + 18f,
                FIFTH_QUEST_ANCHOR_SIZE,
                FIFTH_QUEST_ANCHOR_SIZE);

        clampRectangleToMap(fifthQuestRuinsIntroBounds);
        clampRectangleToMap(fifthQuestOuterRuinsBounds);
        clampRectangleToMap(fifthQuestMidRuinsBounds);
        clampRectangleToMap(fifthQuestPortalBounds);
        snapQuestZoneToWalkableArea(fifthQuestRuinsIntroBounds, 6, 7);
        snapQuestZoneToWalkableArea(fifthQuestOuterRuinsBounds, 6, 7);
        snapQuestZoneToWalkableArea(fifthQuestMidRuinsBounds, 6, 7);
        snapQuestZoneToWalkableArea(fifthQuestPortalBounds, 6, 7);
        for (Rectangle anchorBounds : fifthQuestAnchorBounds) {
            clampRectangleToMap(anchorBounds);
        }

        fifthQuestVillageLayoutReady = true;
    }

    private boolean setFifthQuestVillageAreasFromLayer() {
        MapLayer areaLayer = findLayerInGroup(FIFTH_QUEST_AREA_GROUP_LAYER_NAME, FIFTH_QUEST_AREA_LAYER_NAME);
        if (areaLayer == null) {
            areaLayer = findLayerByName(FIFTH_QUEST_AREA_LAYER_NAME);
        }
        if (areaLayer == null) {
            return false;
        }

        Rectangle firstAreaBounds = nthObjectBounds(areaLayer, 0);
        Rectangle secondAreaBounds = nthObjectBounds(areaLayer, 1);
        Rectangle thirdAreaBounds = nthObjectBounds(areaLayer, 2);
        if (firstAreaBounds == null || secondAreaBounds == null || thirdAreaBounds == null) {
            return false;
        }

        Rectangle triggerBounds = objectLayerBounds(findLayerInGroup(
                FIFTH_QUEST_CAMERA_GROUP_LAYER_NAME,
                FIFTH_QUEST_CAMERA_TRIGGER_LAYER_NAME));
        Rectangle camera1Bounds = objectLayerBounds(findLayerInGroup(
                FIFTH_QUEST_CAMERA_GROUP_LAYER_NAME,
                FIFTH_QUEST_CAMERA_1_LAYER_NAME));
        Rectangle camera2Bounds = objectLayerBounds(findLayerInGroup(
                FIFTH_QUEST_CAMERA_GROUP_LAYER_NAME,
                FIFTH_QUEST_CAMERA_2_LAYER_NAME));
        Rectangle camera3Bounds = objectLayerBounds(findLayerInGroup(
                FIFTH_QUEST_CAMERA_GROUP_LAYER_NAME,
                FIFTH_QUEST_CAMERA_3_LAYER_NAME));

        if (triggerBounds != null && triggerBounds.width > 0f && triggerBounds.height > 0f) {
            fifthQuestRuinsIntroBounds.set(triggerBounds);
            fifthQuestIntroTriggerBounds.set(triggerBounds);
        } else {
            fifthQuestRuinsIntroBounds.set(firstAreaBounds);
            fifthQuestIntroTriggerBounds.set(firstAreaBounds);
        }
        fifthQuestOuterRuinsBounds.set(firstAreaBounds);
        fifthQuestMidRuinsBounds.set(secondAreaBounds);
        setFifthQuestRandomAnchorBounds(fifthQuestAnchorBounds[0], firstAreaBounds, 0);
        setFifthQuestRandomAnchorBounds(fifthQuestAnchorBounds[1], secondAreaBounds, 1);
        setFifthQuestRandomAnchorBounds(fifthQuestAnchorBounds[2], thirdAreaBounds, 2);
        if (camera1Bounds != null && camera1Bounds.width > 0f && camera1Bounds.height > 0f) {
            fifthQuestIntroCameraBounds[0].set(camera1Bounds);
        }
        if (camera2Bounds != null && camera2Bounds.width > 0f && camera2Bounds.height > 0f) {
            fifthQuestIntroCameraBounds[1].set(camera2Bounds);
        }
        if (camera3Bounds != null && camera3Bounds.width > 0f && camera3Bounds.height > 0f) {
            fifthQuestIntroCameraBounds[2].set(camera3Bounds);
        }

        Rectangle allAreaBounds = objectLayerBounds(areaLayer);
        if (allAreaBounds != null && allAreaBounds.width > 0f && allAreaBounds.height > 0f) {
            fifthQuestFightingAreaBounds.set(allAreaBounds);
            clampRectangleToMap(fifthQuestFightingAreaBounds);
        } else {
            fifthQuestFightingAreaBounds.set(0f, 0f, 0f, 0f);
        }

        clampRectangleToMap(fifthQuestRuinsIntroBounds);
        clampRectangleToMap(fifthQuestIntroTriggerBounds);
        clampRectangleToMap(fifthQuestOuterRuinsBounds);
        clampRectangleToMap(fifthQuestMidRuinsBounds);
        for (Rectangle anchorBounds : fifthQuestAnchorBounds) {
            clampRectangleToMap(anchorBounds);
        }
        for (Rectangle cameraBounds : fifthQuestIntroCameraBounds) {
            if (cameraBounds.width > 0f && cameraBounds.height > 0f) {
                clampRectangleToMap(cameraBounds);
            }
        }
        return true;
    }

    private void setFifthQuestRandomAnchorBounds(Rectangle targetBounds, Rectangle areaBounds, int salt) {
        if (targetBounds == null || areaBounds == null || areaBounds.width <= 0f || areaBounds.height <= 0f) {
            return;
        }
        float normalizedX = 0.22f + deterministicUnitFloat(areaBounds, salt * 2) * 0.56f;
        float normalizedY = 0.24f + deterministicUnitFloat(areaBounds, salt * 2 + 1) * 0.52f;
        setRectangleInsideBounds(targetBounds, areaBounds, normalizedX, normalizedY, FIFTH_QUEST_ANCHOR_SIZE, FIFTH_QUEST_ANCHOR_SIZE);
        snapQuestZoneToWalkableArea(targetBounds, 6, 4);
    }

    private float deterministicUnitFloat(Rectangle bounds, int salt) {
        int seed = Float.floatToIntBits(bounds.x * 1.217f)
                ^ Float.floatToIntBits(bounds.y * 1.513f)
                ^ Float.floatToIntBits(bounds.width * 0.917f)
                ^ Float.floatToIntBits(bounds.height * 0.733f)
                ^ (salt + 1) * 0x9E3779B9;
        seed ^= (seed >>> 16);
        seed *= 0x7feb352d;
        seed ^= (seed >>> 15);
        seed *= 0x846ca68b;
        seed ^= (seed >>> 16);
        return (seed & 0x7fffffff) / (float) Integer.MAX_VALUE;
    }

    private void setRectangleInsideBounds(Rectangle bounds, Rectangle parentBounds, float normalizedCenterX,
                                          float normalizedCenterY, float width, float height) {
        if (bounds == null || parentBounds == null) {
            return;
        }
        float clampedWidth = Math.min(Math.max(1f, width), parentBounds.width);
        float clampedHeight = Math.min(Math.max(1f, height), parentBounds.height);
        float centerX = parentBounds.x + parentBounds.width * clamp01(normalizedCenterX);
        float centerY = parentBounds.y + parentBounds.height * clamp01(normalizedCenterY);
        float minCenterX = parentBounds.x + clampedWidth * 0.5f;
        float maxCenterX = parentBounds.x + parentBounds.width - clampedWidth * 0.5f;
        float minCenterY = parentBounds.y + clampedHeight * 0.5f;
        float maxCenterY = parentBounds.y + parentBounds.height - clampedHeight * 0.5f;
        if (minCenterX > maxCenterX) {
            centerX = parentBounds.x + parentBounds.width * 0.5f;
        } else {
            centerX = Math.max(minCenterX, Math.min(maxCenterX, centerX));
        }
        if (minCenterY > maxCenterY) {
            centerY = parentBounds.y + parentBounds.height * 0.5f;
        } else {
            centerY = Math.max(minCenterY, Math.min(maxCenterY, centerY));
        }
        setCenteredRectangle(bounds, centerX, centerY, clampedWidth, clampedHeight);
    }

    private void clampRectangleToMap(Rectangle bounds) {
        if (bounds == null || world.mapBounds == null) {
            return;
        }
        float minX = world.mapBounds.x + 12f;
        float minY = world.mapBounds.y + 12f;
        float maxX = world.mapBounds.x + world.mapBounds.width - bounds.width - 12f;
        float maxY = world.mapBounds.y + world.mapBounds.height - bounds.height - 12f;
        bounds.x = Math.max(minX, Math.min(bounds.x, maxX));
        bounds.y = Math.max(minY, Math.min(bounds.y, maxY));
    }

    private void snapQuestZoneToWalkableArea(Rectangle bounds, int maxSearchSteps, int minimumWalkableSamples) {
        if (bounds == null || bounds.width <= 0f || bounds.height <= 0f) {
            return;
        }

        Rectangle bestBounds = new Rectangle(bounds);
        Rectangle candidateBounds = new Rectangle(bounds);
        float preferredCenterX = bounds.x + bounds.width * 0.5f;
        float preferredCenterY = bounds.y + bounds.height * 0.5f;
        float step = DEFAULT_TILE_SIZE;
        int bestScore = scoreQuestZoneWalkability(bestBounds);
        float bestDistanceSq = 0f;
        if (bestScore >= minimumWalkableSamples) {
            return;
        }

        for (int radius = 1; radius <= Math.max(1, maxSearchSteps); radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    if (Math.abs(dx) != radius && Math.abs(dy) != radius) {
                        continue;
                    }
                    float candidateCenterX = preferredCenterX + dx * step;
                    float candidateCenterY = preferredCenterY + dy * step;
                    candidateBounds.set(
                            candidateCenterX - bounds.width * 0.5f,
                            candidateCenterY - bounds.height * 0.5f,
                            bounds.width,
                            bounds.height);
                    clampRectangleToMap(candidateBounds);
                    int score = scoreQuestZoneWalkability(candidateBounds);
                    float distanceSq = dx * dx + dy * dy;
                    if (score > bestScore || (score == bestScore && distanceSq < bestDistanceSq)) {
                        bestBounds.set(candidateBounds);
                        bestScore = score;
                        bestDistanceSq = distanceSq;
                    }
                    if (score >= minimumWalkableSamples) {
                        bounds.set(candidateBounds);
                        return;
                    }
                }
            }
        }

        bounds.set(bestBounds);
    }

    private int scoreQuestZoneWalkability(Rectangle bounds) {
        if (bounds == null || bounds.width <= 0f || bounds.height <= 0f) {
            return 0;
        }
        float[] sampleXs = {0.18f, 0.50f, 0.82f};
        float[] sampleYs = {0.18f, 0.50f, 0.82f};
        int score = 0;
        for (float sampleX : sampleXs) {
            for (float sampleY : sampleYs) {
                float centerX = bounds.x + bounds.width * sampleX;
                float centerY = bounds.y + bounds.height * sampleY;
                if (isWalkablePlayerCenter(centerX, centerY)) {
                    score++;
                }
            }
        }
        return score;
    }

    private Rectangle nthObjectBounds(MapLayer layer, int targetIndex) {
        if (layer == null || targetIndex < 0) {
            return null;
        }
        int objectIndex = 0;
        for (MapObject object : layer.getObjects()) {
            Rectangle bounds = objectBounds(layer, object);
            if (bounds == null || bounds.width <= 0f || bounds.height <= 0f) {
                continue;
            }
            if (objectIndex == targetIndex) {
                return bounds;
            }
            objectIndex++;
        }
        return null;
    }

    private boolean isWalkablePlayerCenter(float centerX, float centerY) {
        Rectangle testBounds = new Rectangle(
                centerX - HITBOX_WIDTH * 0.5f,
                centerY - HITBOX_HEIGHT * 0.5f,
                HITBOX_WIDTH,
                HITBOX_HEIGHT);
        return !collides(testBounds);
    }

    private Rectangle createWalkableCombatZone(Rectangle anchorBounds, float width, float height) {
        Rectangle combatBounds = new Rectangle();
        if (anchorBounds == null || anchorBounds.width <= 0f || anchorBounds.height <= 0f) {
            return combatBounds;
        }
        setCenteredRectangle(
                combatBounds,
                anchorBounds.x + anchorBounds.width * 0.5f,
                anchorBounds.y + anchorBounds.height * 0.5f,
                width,
                height);
        clampRectangleToMap(combatBounds);
        snapQuestZoneToWalkableArea(combatBounds, 6, 7);
        return combatBounds;
    }

    private Vector2 questZonePoint(Rectangle bounds, float normalizedX, float normalizedY) {
        if (bounds == null) {
            return new Vector2();
        }
        float clampedX = clamp01(normalizedX);
        float clampedY = clamp01(normalizedY);
        return new Vector2(
                bounds.x + bounds.width * clampedX,
                bounds.y + bounds.height * clampedY);
    }

    private void startFifthQuest() {
        fifthQuestStage = FIFTH_QUEST_GO_TO_ABANDONED_VILLAGE;
        fifthQuestEncounterSpawned = false;
        fifthQuestCommanderCalloutTimer = 0f;
        fifthQuestCommanderCalloutIndex = 0;
        fifthQuestLabVillagersFadeTimer = 0f;
        fifthQuestLabVillagersVisibleLastFrame = false;
        fifthQuestRuinsIntroTriggered = false;
        fifthQuestIntroCameraPhase = FifthQuestIntroCameraPhase.NONE;
        fifthQuestIntroCameraTimer = 0f;
        fifthQuestFirstEncounterIntroPlayed = false;
        stopFifthQuestFirstEncounterIntro();
        fifthQuestZombieSfxCooldownTimer = 0f;
        showQuestNotification(
                FIFTH_QUEST_NOTIFICATION_TITLE,
                "Objective Updated: Go to the abandoned village by the portal.",
                FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
    }

    private void beginFifthQuestOuterRuinsBattle() {
        fifthQuestStage = FIFTH_QUEST_OUTER_RUINS_BATTLE;
        fifthQuestEncounterSpawned = false;
        showQuestNotification(
                "Field Reminder",
                "Use E Dash Attack to engage fast, and right click Spatial Dash to reposition.",
                FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
        if (hudRenderer != null) {
            hudRenderer.showNotification(
                    FIFTH_QUEST_NOTIFICATION_TITLE,
                    "Objective Updated: Clear the outer ruins.",
                    FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
        }
    }

    private void beginFifthQuestMidRuinsBattle() {
        fifthQuestStage = FIFTH_QUEST_MID_RUINS_BATTLE;
        fifthQuestEncounterSpawned = false;
        if (hudRenderer != null) {
            hudRenderer.showNotification("Father", "They're guarding the way.", FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
            hudRenderer.showNotification(characterName, "Then I break through.", FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
        }
        showQuestNotification(
                FIFTH_QUEST_NOTIFICATION_TITLE,
                "Objective Updated: Defeat the demons blocking the path to the portal.",
                FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
    }

    private void beginFifthQuestAnchorBattle(int anchorIndex) {
        fifthQuestStage = switch (anchorIndex) {
            case 0 -> FIFTH_QUEST_ANCHOR_1_BATTLE;
            case 1 -> FIFTH_QUEST_ANCHOR_2_BATTLE;
            default -> FIFTH_QUEST_ANCHOR_3_BATTLE;
        };
        fifthQuestEncounterSpawned = false;
        if (anchorIndex == 0) {
            showQuestNotification(
                    "Field Reminder",
                    "Use E Dash Attack to engage fast, and right click Spatial Dash to reposition.",
                    FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
        }
        showQuestNotification(
                FIFTH_QUEST_NOTIFICATION_TITLE,
                "Objective Updated: Destroy the portal anchors.",
                FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
    }

    private void beginFifthQuestPortalBoss() {
        fifthQuestStage = FIFTH_QUEST_PORTAL_BOSS;
        fifthQuestEncounterSpawned = false;
        fifthQuestCommanderCalloutTimer = 0f;
        fifthQuestCommanderCalloutIndex = 0;
        showQuestNotification(
                FIFTH_QUEST_NOTIFICATION_TITLE,
                "Objective Updated: Defeat the demon guarding the portal.",
                FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
    }

    private void updateFifthQuestOuterRuinsBattle() {
        if (fifthQuestStage != FIFTH_QUEST_OUTER_RUINS_BATTLE || !MAP_ID_VILLAGE.equals(world.activeMapId)) {
            return;
        }
        if (!fifthQuestEncounterSpawned) {
            spawnFifthQuestOuterEncounter();
        }
        if (fifthQuestEncounterSpawned && countFifthQuestEncounterDemons(FIFTH_QUEST_ENCOUNTER_OUTER) <= 0) {
            fifthQuestEncounterSpawned = false;
            fifthQuestStage = FIFTH_QUEST_ADVANCE_TO_PORTAL;
            if (hudRenderer != null) {
                hudRenderer.showNotification(
                        characterName,
                        "These are not scouts anymore. This is an invasion.",
                        FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
            }
            showQuestNotification(
                    FIFTH_QUEST_NOTIFICATION_TITLE,
                    "Objective Updated: Advance toward the portal.",
                    FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
        }
    }

    private void updateFifthQuestMidRuinsBattle() {
        if (fifthQuestStage != FIFTH_QUEST_MID_RUINS_BATTLE || !MAP_ID_VILLAGE.equals(world.activeMapId)) {
            return;
        }
        if (!fifthQuestEncounterSpawned) {
            spawnFifthQuestMidEncounter();
        }
        if (fifthQuestEncounterSpawned && countFifthQuestEncounterDemons(FIFTH_QUEST_ENCOUNTER_MID) <= 0) {
            fifthQuestEncounterSpawned = false;
            if (hudRenderer != null) {
                hudRenderer.showNotification(
                        characterName,
                        "They're not just emerging here. They're preparing this place.",
                        FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
            }
            beginFifthQuestAnchorBattle(0);
        }
    }

    private void updateFifthQuestAnchorBattle(int anchorIndex, int encounterId) {
        if (!MAP_ID_VILLAGE.equals(world.activeMapId) || anchorIndex < 0 || anchorIndex >= fifthQuestAnchorBounds.length) {
            return;
        }
        Rectangle anchorBounds = fifthQuestAnchorBounds[anchorIndex];
        if (!fifthQuestEncounterSpawned) {
            if (overlapsPlayerExpanded(anchorBounds)) {
                spawnFifthQuestAnchorEncounter(anchorIndex, encounterId);
            }
            return;
        }
        if (countFifthQuestEncounterDemons(encounterId) > 0) {
            return;
        }
        fifthQuestEncounterSpawned = false;
        fifthQuestStage = switch (anchorIndex) {
            case 0 -> FIFTH_QUEST_ANCHOR_1_DESTROY;
            case 1 -> FIFTH_QUEST_ANCHOR_2_DESTROY;
            default -> FIFTH_QUEST_ANCHOR_3_DESTROY;
        };
        showQuestNotification(
                FIFTH_QUEST_NOTIFICATION_TITLE,
                "The anchor is exposed. Use the interact key/button to destroy it.",
                FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
    }

    private void updateFifthQuestPortalBoss(float delta) {
        if (fifthQuestStage != FIFTH_QUEST_PORTAL_BOSS || !MAP_ID_VILLAGE.equals(world.activeMapId)) {
            return;
        }
        if (!fifthQuestEncounterSpawned) {
            Rectangle triggerBounds = fifthQuestFightingAreaBounds.width > 0f ? fifthQuestFightingAreaBounds : fifthQuestPortalBounds;
            if (overlapsPlayerExpanded(triggerBounds)) {
                spawnFifthQuestPortalBossEncounter();
            }
            return;
        }
        fifthQuestCommanderCalloutTimer += Math.max(0f, delta);
        if (hudRenderer != null && fifthQuestCommanderCalloutTimer >= FIFTH_QUEST_COMMANDER_CALLOUT_SECONDS) {
            fifthQuestCommanderCalloutTimer = 0f;
            hudRenderer.showNotification(
                    "Father",
                    FIFTH_QUEST_COMMANDER_CALLOUTS[fifthQuestCommanderCalloutIndex % FIFTH_QUEST_COMMANDER_CALLOUTS.length],
                    1.4f);
            fifthQuestCommanderCalloutIndex++;
        }
        if (countFifthQuestEncounterDemons(FIFTH_QUEST_ENCOUNTER_COMMANDER) <= 0) {
            fifthQuestEncounterSpawned = false;
            clearFifthQuestEncounterDemons(FIFTH_QUEST_ENCOUNTER_COMMANDER);
            startQuestDialogue(
                    QuestDialogueId.FIFTH_QUEST_PORTAL_AFTER,
                    FIFTH_QUEST_PORTAL_AFTER_SPEAKERS,
                    FIFTH_QUEST_PORTAL_AFTER_DIALOGUE_LINES);
        }
    }

    private void spawnFifthQuestOuterEncounter() {
        ensureFifthQuestVillageLayout();
        clearFifthQuestEncounterDemons(FIFTH_QUEST_ENCOUNTER_OUTER);
        Rectangle zoneBounds = createWalkableCombatZone(
                fifthQuestOuterRuinsBounds,
                fifthQuestOuterRuinsBounds.width,
                fifthQuestOuterRuinsBounds.height);
        Vector2 leftDemon = questZonePoint(zoneBounds, 0.20f, 0.42f);
        Vector2 upperDemon = questZonePoint(zoneBounds, 0.48f, 0.68f);
        Vector2 rightDemon = questZonePoint(zoneBounds, 0.80f, 0.32f);
        addFifthQuestDemonAt(leftDemon.x, leftDemon.y, FifthQuestDemonType.DEMON_1, FIFTH_QUEST_ENCOUNTER_OUTER,
                zoneBounds, FIFTH_QUEST_DEMON_1_HEALTH, FIFTH_QUEST_DEMON_1_DAMAGE, FIFTH_QUEST_DEMON_1_SPEED);
        addFifthQuestDemonAt(upperDemon.x, upperDemon.y, FifthQuestDemonType.DEMON_1, FIFTH_QUEST_ENCOUNTER_OUTER,
                zoneBounds, FIFTH_QUEST_DEMON_1_HEALTH, FIFTH_QUEST_DEMON_1_DAMAGE, FIFTH_QUEST_DEMON_1_SPEED);
        addFifthQuestDemonAt(rightDemon.x, rightDemon.y, FifthQuestDemonType.DEMON_1, FIFTH_QUEST_ENCOUNTER_OUTER,
                zoneBounds, FIFTH_QUEST_DEMON_1_HEALTH, FIFTH_QUEST_DEMON_1_DAMAGE, FIFTH_QUEST_DEMON_1_SPEED);
        fifthQuestEncounterSpawned = true;
    }

    private void spawnFifthQuestMidEncounter() {
        ensureFifthQuestVillageLayout();
        clearFifthQuestEncounterDemons(FIFTH_QUEST_ENCOUNTER_MID);
        Rectangle zoneBounds = createWalkableCombatZone(
                fifthQuestMidRuinsBounds,
                fifthQuestMidRuinsBounds.width,
                fifthQuestMidRuinsBounds.height);
        Vector2 leftDemon = questZonePoint(zoneBounds, 0.18f, 0.48f);
        Vector2 upperDemon = questZonePoint(zoneBounds, 0.42f, 0.70f);
        Vector2 rightDemon = questZonePoint(zoneBounds, 0.68f, 0.44f);
        Vector2 rearDemon = questZonePoint(zoneBounds, 0.84f, 0.24f);
        addFifthQuestDemonAt(leftDemon.x, leftDemon.y, FifthQuestDemonType.DEMON_1, FIFTH_QUEST_ENCOUNTER_MID,
                zoneBounds, FIFTH_QUEST_DEMON_1_HEALTH + 6f, FIFTH_QUEST_DEMON_1_DAMAGE + 2f, FIFTH_QUEST_DEMON_1_SPEED + 2f);
        addFifthQuestDemonAt(upperDemon.x, upperDemon.y, FifthQuestDemonType.DEMON_2, FIFTH_QUEST_ENCOUNTER_MID,
                zoneBounds, FIFTH_QUEST_DEMON_2_HEALTH, FIFTH_QUEST_DEMON_2_DAMAGE, FIFTH_QUEST_DEMON_2_SPEED);
        addFifthQuestDemonAt(rightDemon.x, rightDemon.y, FifthQuestDemonType.DEMON_1, FIFTH_QUEST_ENCOUNTER_MID,
                zoneBounds, FIFTH_QUEST_DEMON_1_HEALTH + 6f, FIFTH_QUEST_DEMON_1_DAMAGE + 2f, FIFTH_QUEST_DEMON_1_SPEED + 2f);
        addFifthQuestDemonAt(rearDemon.x, rearDemon.y, FifthQuestDemonType.DEMON_2, FIFTH_QUEST_ENCOUNTER_MID,
                zoneBounds, FIFTH_QUEST_DEMON_2_HEALTH + 8f, FIFTH_QUEST_DEMON_2_DAMAGE + 2f, FIFTH_QUEST_DEMON_2_SPEED + 2f);
        fifthQuestEncounterSpawned = true;
    }

    private void spawnFifthQuestAnchorEncounter(int anchorIndex, int encounterId) {
        ensureFifthQuestVillageLayout();
        Rectangle anchorBounds = fifthQuestAnchorBounds[anchorIndex];
        clearFifthQuestEncounterDemons(encounterId);
        Rectangle zoneBounds = createWalkableCombatZone(anchorBounds, 118f, 96f);
        Vector2 leftDemon = questZonePoint(zoneBounds, 0.20f, 0.44f);
        Vector2 upperDemon = questZonePoint(zoneBounds, 0.52f, 0.68f);
        Vector2 eliteDemon = questZonePoint(zoneBounds, 0.78f, 0.30f);
        addFifthQuestDemonAt(leftDemon.x, leftDemon.y, FifthQuestDemonType.DEMON_1, encounterId,
                zoneBounds, FIFTH_QUEST_DEMON_1_HEALTH + 8f, FIFTH_QUEST_DEMON_1_DAMAGE + 2f, FIFTH_QUEST_DEMON_1_SPEED + 2f);
        addFifthQuestDemonAt(upperDemon.x, upperDemon.y, FifthQuestDemonType.DEMON_2, encounterId,
                zoneBounds, FIFTH_QUEST_DEMON_2_HEALTH + 10f, FIFTH_QUEST_DEMON_2_DAMAGE + 2f, FIFTH_QUEST_DEMON_2_SPEED + 2f);
        FifthQuestDemonType eliteType = anchorIndex == 2 ? FifthQuestDemonType.DEMON_3 : FifthQuestDemonType.DEMON_2;
        float eliteHealth = anchorIndex == 2 ? FIFTH_QUEST_DEMON_3_HEALTH : FIFTH_QUEST_DEMON_2_HEALTH + 18f;
        float eliteDamage = anchorIndex == 2 ? FIFTH_QUEST_DEMON_3_DAMAGE : FIFTH_QUEST_DEMON_2_DAMAGE + 4f;
        float eliteSpeed = anchorIndex == 2 ? FIFTH_QUEST_DEMON_3_SPEED : FIFTH_QUEST_DEMON_2_SPEED + 4f;
        addFifthQuestDemonAt(eliteDemon.x, eliteDemon.y, eliteType, encounterId, zoneBounds, eliteHealth, eliteDamage, eliteSpeed);
        if (anchorIndex == 2) {
            Vector2 rearDemon = questZonePoint(zoneBounds, 0.32f, 0.16f);
            addFifthQuestDemonAt(rearDemon.x, rearDemon.y, FifthQuestDemonType.DEMON_3, encounterId,
                    zoneBounds, FIFTH_QUEST_DEMON_3_HEALTH + 10f, FIFTH_QUEST_DEMON_3_DAMAGE + 2f, FIFTH_QUEST_DEMON_3_SPEED + 2f);
        }
        fifthQuestEncounterSpawned = true;
        if (anchorIndex == 0 && !fifthQuestFirstEncounterIntroPlayed) {
            startFifthQuestFirstEncounterIntro(encounterId);
        }
    }

    private void spawnFifthQuestPortalBossEncounter() {
        ensureFifthQuestVillageLayout();
        clearFifthQuestEncounterDemons(FIFTH_QUEST_ENCOUNTER_COMMANDER);
        Rectangle combatAnchorBounds = fifthQuestFightingAreaBounds.width > 0f ? fifthQuestFightingAreaBounds : fifthQuestPortalBounds;
        Rectangle zoneBounds = createWalkableCombatZone(
                combatAnchorBounds,
                combatAnchorBounds == fifthQuestFightingAreaBounds
                        ? Math.max(104f, combatAnchorBounds.width - 12f)
                        : Math.max(104f, combatAnchorBounds.width - 16f),
                combatAnchorBounds == fifthQuestFightingAreaBounds
                        ? Math.max(92f, combatAnchorBounds.height - 8f)
                        : Math.max(92f, combatAnchorBounds.height - 16f));
        Vector2 leftGuard = questZonePoint(zoneBounds, 0.24f, 0.48f);
        Vector2 rightGuard = questZonePoint(zoneBounds, 0.76f, 0.44f);
        Vector2 commander = questZonePoint(zoneBounds, 0.52f, 0.72f);
        addFifthQuestDemonAt(leftGuard.x, leftGuard.y, FifthQuestDemonType.DEMON_2, FIFTH_QUEST_ENCOUNTER_COMMANDER,
                zoneBounds, FIFTH_QUEST_DEMON_2_HEALTH + 14f, FIFTH_QUEST_DEMON_2_DAMAGE + 3f, FIFTH_QUEST_DEMON_2_SPEED + 2f);
        addFifthQuestDemonAt(rightGuard.x, rightGuard.y, FifthQuestDemonType.DEMON_2, FIFTH_QUEST_ENCOUNTER_COMMANDER,
                zoneBounds, FIFTH_QUEST_DEMON_2_HEALTH + 14f, FIFTH_QUEST_DEMON_2_DAMAGE + 3f, FIFTH_QUEST_DEMON_2_SPEED + 2f);
        addFifthQuestDemonAt(commander.x, commander.y, FifthQuestDemonType.COMMANDER, FIFTH_QUEST_ENCOUNTER_COMMANDER,
                zoneBounds, FIFTH_QUEST_COMMANDER_HEALTH, FIFTH_QUEST_COMMANDER_DAMAGE, FIFTH_QUEST_COMMANDER_SPEED);
        fifthQuestEncounterSpawned = true;
    }

    private void addFifthQuestDemonAt(float x, float y, FifthQuestDemonType type, int encounterId,
                                      Rectangle zoneBounds, float health, float damage, float speed) {
        FifthQuestDemonInstance demon = new FifthQuestDemonInstance();
        Vector2 spawnPosition = resolveFifthQuestSpawnPosition(x, y, encounterId, zoneBounds);
        demon.position.set(spawnPosition);
        demon.type = type == null ? FifthQuestDemonType.DEMON_1 : type;
        demon.questEncounterId = encounterId;
        demon.zoneBounds.set(zoneBounds != null ? zoneBounds : fifthQuestPortalBounds);
        demon.maxHealth = Math.max(1f, health);
        demon.health = demon.maxHealth;
        demon.playerDamage = Math.max(1f, damage);
        demon.chaseSpeed = Math.max(1f, speed * FIFTH_QUEST_DEMON_SPEED_MULTIPLIER);
        demon.attackRange = demon.type == FifthQuestDemonType.COMMANDER
                ? FIFTH_QUEST_DEMON_ATTACK_RANGE + 10f
                : FIFTH_QUEST_DEMON_ATTACK_RANGE;
        demon.attackIntervalSeconds = demon.type == FifthQuestDemonType.COMMANDER
                ? 1.05f
                : FIFTH_QUEST_DEMON_ATTACK_COOLDOWN_SECONDS;
        fifthQuestDemons.add(demon);
    }

    private Vector2 resolveFifthQuestSpawnPosition(float preferredX, float preferredY, int encounterId, Rectangle zoneBounds) {
        Vector2 preferred = new Vector2(preferredX, preferredY);
        if (!shouldEnforceFifthQuestSpawnDistanceFromPlayer()) {
            return preferred;
        }

        Rectangle activeZoneBounds = zoneBounds != null && zoneBounds.width > 0f && zoneBounds.height > 0f
                ? zoneBounds
                : fifthQuestPortalBounds;
        float playerCenterX = player.bounds.x + player.bounds.width * 0.5f;
        float playerCenterY = player.bounds.y + player.bounds.height * 0.5f;
        float minDistanceSq = FIFTH_QUEST_SPAWN_MIN_DISTANCE_FROM_PLAYER * FIFTH_QUEST_SPAWN_MIN_DISTANCE_FROM_PLAYER;
        float preferredDistanceSq = distanceSquared(preferred.x, preferred.y, playerCenterX, playerCenterY);
        if (preferredDistanceSq >= minDistanceSq && isFifthQuestSpawnPointClear(preferred.x, preferred.y, encounterId)) {
            return preferred;
        }

        Vector2 bestCandidate = new Vector2(preferred);
        float bestDistanceSq = preferredDistanceSq;
        float[] samplePoints = {0.12f, 0.26f, 0.40f, 0.54f, 0.68f, 0.82f};
        for (float sampleX : samplePoints) {
            for (float sampleY : samplePoints) {
                Vector2 candidate = questZonePoint(activeZoneBounds, sampleX, sampleY);
                float candidateDistanceSq = distanceSquared(candidate.x, candidate.y, playerCenterX, playerCenterY);
                if (!isFifthQuestSpawnPointClear(candidate.x, candidate.y, encounterId)) {
                    continue;
                }
                if (candidateDistanceSq >= minDistanceSq) {
                    return candidate;
                }
                if (candidateDistanceSq > bestDistanceSq) {
                    bestCandidate.set(candidate);
                    bestDistanceSq = candidateDistanceSq;
                }
            }
        }
        return bestCandidate;
    }

    private boolean shouldEnforceFifthQuestSpawnDistanceFromPlayer() {
        return MAP_ID_VILLAGE.equals(world.activeMapId)
                && fifthQuestStage > FIFTH_QUEST_DISABLED
                && fifthQuestStage < FIFTH_QUEST_COMPLETE;
    }

    private boolean isFifthQuestSpawnPointClear(float x, float y, int encounterId) {
        if (!isWalkablePlayerCenter(x, y)) {
            return false;
        }
        float minSeparationSq = FIFTH_QUEST_SPAWN_MIN_DISTANCE_BETWEEN_DEMONS * FIFTH_QUEST_SPAWN_MIN_DISTANCE_BETWEEN_DEMONS;
        for (int i = 0; i < fifthQuestDemons.size; i++) {
            FifthQuestDemonInstance existing = fifthQuestDemons.get(i);
            if (existing.questEncounterId != encounterId || existing.dying || existing.health <= 0f) {
                continue;
            }
            if (distanceSquared(x, y, existing.position.x, existing.position.y) < minSeparationSq) {
                return false;
            }
        }
        return true;
    }

    private float distanceSquared(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        return dx * dx + dy * dy;
    }

    private int countFifthQuestEncounterDemons(int encounterId) {
        int remaining = 0;
        for (int i = 0; i < fifthQuestDemons.size; i++) {
            FifthQuestDemonInstance demon = fifthQuestDemons.get(i);
            if (demon.questEncounterId == encounterId && !demon.dying && demon.health > 0f) {
                remaining++;
            }
        }
        return remaining;
    }

    private void clearFifthQuestEncounterDemons(int encounterId) {
        for (int i = fifthQuestDemons.size - 1; i >= 0; i--) {
            if (fifthQuestDemons.get(i).questEncounterId == encounterId) {
                fifthQuestDemons.removeIndex(i);
            }
        }
    }

    private void updateSixthQuest(float delta) {
        if (sixthQuestStage <= SIXTH_QUEST_DISABLED
                || sixthQuestStage >= SIXTH_QUEST_COMPLETE
                || (isFirstQuestOverlayActive() && !isSixthQuestPortalActivationRunning())) {
            return;
        }

        if (sixthQuestStage == SIXTH_QUEST_ENTER_PORTAL) {
            updateSixthQuestPortalActivation(delta);
            return;
        }
        if (sixthQuestStage == SIXTH_QUEST_OUTER_ASSAULT) {
            updateSixthQuestOuterAssault();
            return;
        }
        if (sixthQuestStage == SIXTH_QUEST_SEAL_A_BATTLE) {
            updateSixthQuestSealBattle(true);
            return;
        }
        if (sixthQuestStage == SIXTH_QUEST_SEAL_B_BATTLE) {
            updateSixthQuestSealBattle(false);
            return;
        }
        if (sixthQuestStage == SIXTH_QUEST_THRONE_GUARDS) {
            updateSixthQuestThroneGuards();
            return;
        }
        if (sixthQuestStage == SIXTH_QUEST_BOSS_PHASE_1) {
            updateSixthQuestBossPhase1(delta);
            return;
        }
        if (sixthQuestStage == SIXTH_QUEST_FINAL_PHASE_BATTLE) {
            updateSixthQuestFinalPhaseBattle(delta);
        }
    }

    private void ensureSixthQuestCastleLayout() {
        if (!MAP_ID_DEMON_CASTLE.equals(world.activeMapId) || sixthQuestCastleLayoutReady) {
            return;
        }
        Rectangle room1Spawn = objectLayerBounds(findLayerInGroup(ROOM1_GROUP_LAYER_NAME, ROOM1_BATS_SPAWN_LAYER_NAME));
        Rectangle sealAZone = objectLayerBounds(findLayerInGroup(BOSS_1_GROUP_LAYER_NAME, BOSS_FIGHT_ZONE_LAYER_NAME));
        Rectangle sealAInteract = objectLayerBounds(findLayerInGroup(BOSS_1_GROUP_LAYER_NAME, BOSS_PHASE_2_BOSS_LAYER_NAME));
        Rectangle throneZone = objectLayerBounds(findLayerInGroup(BOSS_2_GROUP_LAYER_NAME, BOSS_WALKABLE_LAYER_NAME));
        Rectangle throneSpawn = objectLayerBounds(findLayerInGroup(BOSS_2_GROUP_LAYER_NAME, BOSS_PHASE_2_BOSS_LAYER_NAME));
        Rectangle sealBInteract = objectLayerBounds(findLayerInGroup(GOLEM_1_GROUP_LAYER_NAME, GOLEM_1_OBJECT_LAYER_NAME));
        if (sealBInteract == null) {
            sealBInteract = objectLayerBounds(findLayerInGroup(GOLEM_1_GROUP_LAYER_NAME, GOLEM_1_OBJECT_LAYER_NAME_ALT));
        }

        Rectangle mapBounds = world.mapBounds != null ? new Rectangle(world.mapBounds) : new Rectangle(0f, 0f, 0f, 0f);
        if (room1Spawn != null) {
            setCenteredRectangle(
                    sixthQuestOuterCastleBounds,
                    room1Spawn.x + room1Spawn.width * 0.5f,
                    room1Spawn.y + room1Spawn.height * 0.5f,
                    SIXTH_QUEST_OUTER_ASSAULT_WIDTH,
                    SIXTH_QUEST_OUTER_ASSAULT_HEIGHT);
        } else if (mapBounds.width > 0f && mapBounds.height > 0f) {
            setCenteredRectangle(
                    sixthQuestOuterCastleBounds,
                    mapBounds.x + mapBounds.width * 0.34f,
                    mapBounds.y + mapBounds.height * 0.22f,
                    SIXTH_QUEST_OUTER_ASSAULT_WIDTH,
                    SIXTH_QUEST_OUTER_ASSAULT_HEIGHT);
        }

        if (sealAZone != null) {
            sixthQuestSealABounds.set(sealAZone);
        } else if (mapBounds.width > 0f && mapBounds.height > 0f) {
            setCenteredRectangle(
                    sixthQuestSealABounds,
                    mapBounds.x + mapBounds.width * 0.58f,
                    mapBounds.y + mapBounds.height * 0.46f,
                    SIXTH_QUEST_SEAL_WIDTH,
                    SIXTH_QUEST_SEAL_HEIGHT);
        }
        if (sealAInteract != null) {
            sixthQuestSealAInteractBounds.set(sealAInteract);
        } else {
            setCenteredRectangle(
                    sixthQuestSealAInteractBounds,
                    sixthQuestSealABounds.x + sixthQuestSealABounds.width * 0.5f,
                    sixthQuestSealABounds.y + sixthQuestSealABounds.height * 0.62f,
                    28f,
                    28f);
        }

        if (golemFightZoneBounds.width > 0f && golemFightZoneBounds.height > 0f) {
            sixthQuestSealBBounds.set(golemFightZoneBounds);
        } else if (mapBounds.width > 0f && mapBounds.height > 0f) {
            setCenteredRectangle(
                    sixthQuestSealBBounds,
                    mapBounds.x + mapBounds.width * 0.76f,
                    mapBounds.y + mapBounds.height * 0.36f,
                    SIXTH_QUEST_SEAL_WIDTH,
                    SIXTH_QUEST_SEAL_HEIGHT);
        }
        if (sealBInteract != null) {
            sixthQuestSealBInteractBounds.set(sealBInteract);
        } else {
            setCenteredRectangle(
                    sixthQuestSealBInteractBounds,
                    sixthQuestSealBBounds.x + sixthQuestSealBBounds.width * 0.5f,
                    sixthQuestSealBBounds.y + sixthQuestSealBBounds.height * 0.58f,
                    30f,
                    30f);
        }

        if (throneZone != null) {
            sixthQuestThroneBounds.set(throneZone);
        } else if (mapBounds.width > 0f && mapBounds.height > 0f) {
            setCenteredRectangle(
                    sixthQuestThroneBounds,
                    mapBounds.x + mapBounds.width * 0.82f,
                    mapBounds.y + mapBounds.height * 0.62f,
                    SIXTH_QUEST_THRONE_WIDTH,
                    SIXTH_QUEST_THRONE_HEIGHT);
        }
        if (throneSpawn != null) {
            sixthQuestBossPhase1SpawnBounds.set(throneSpawn);
        } else {
            setCenteredRectangle(
                    sixthQuestBossPhase1SpawnBounds,
                    sixthQuestThroneBounds.x + sixthQuestThroneBounds.width * 0.5f,
                    sixthQuestThroneBounds.y + sixthQuestThroneBounds.height * 0.64f,
                    24f,
                    24f);
        }
        sixthQuestCastleLayoutReady = true;
    }

    private void ensureSixthQuestFinalArenaLayout() {
        if (!MAP_ID_BOSS_ARENA_PHASE_2.equals(world.activeMapId) || sixthQuestFinalArenaLayoutReady) {
            return;
        }
        Rectangle spawnBounds = objectLayerBounds(findLayerByName(BOSS_ARENA_PHASE_2_DEMON_SPAWN_LAYER_NAME));
        Rectangle explosionWalkableBounds = objectLayerBounds(findLayerByName(BOSS_ARENA_PHASE_2_EXPLOSION_WALKABLE_LAYER_NAME));
        Rectangle walkableBounds = objectLayerBounds(findLayerByName(BOSS_WALKABLE_LAYER_NAME));
        Rectangle mapBounds = world.mapBounds != null ? new Rectangle(world.mapBounds) : new Rectangle(0f, 0f, 0f, 0f);
        if (hasValidBossZoneBounds(walkableBounds)) {
            sixthQuestFinalPhaseBounds.set(walkableBounds);
        } else if (mapBounds.width > 0f && mapBounds.height > 0f) {
            sixthQuestFinalPhaseBounds.set(mapBounds);
        }
        if (hasValidBossZoneBounds(explosionWalkableBounds)) {
            sixthQuestFinalPhaseExplosionBounds.set(explosionWalkableBounds);
        } else if (sixthQuestFinalPhaseBounds.width > 0f && sixthQuestFinalPhaseBounds.height > 0f) {
            sixthQuestFinalPhaseExplosionBounds.set(sixthQuestFinalPhaseBounds);
        } else {
            sixthQuestFinalPhaseExplosionBounds.set(0f, 0f, 0f, 0f);
        }
        if (spawnBounds != null) {
            sixthQuestFinalPhaseSpawnBounds.set(spawnBounds);
        } else if (mapBounds.width > 0f && mapBounds.height > 0f) {
            setCenteredRectangle(
                    sixthQuestFinalPhaseSpawnBounds,
                    mapBounds.x + mapBounds.width * 0.5f,
                    mapBounds.y + mapBounds.height * 0.5f,
                    24f,
                    24f);
        }
        sixthQuestFinalArenaLayoutReady = true;
    }

    private void updateSixthQuestOuterAssault() {
        if (!MAP_ID_DEMON_CASTLE.equals(world.activeMapId)) {
            return;
        }
        ensureSixthQuestCastleLayout();
        if (!overlapsPlayerExpanded(sixthQuestOuterCastleBounds) && !isPlayerInsideZoneBounds(sixthQuestOuterCastleBounds)) {
            return;
        }
        if (!hasLivingBatInZone(sixthQuestOuterCastleBounds)) {
            if (hudRenderer != null) {
                hudRenderer.showNotification(
                        characterName,
                        "These are not scouts anymore. This is the army that burned my first home.",
                        FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
            }
            beginSixthQuestSealABattle();
        }
    }

    private void updateSixthQuestSealBattle(boolean firstSeal) {
        if (!MAP_ID_DEMON_CASTLE.equals(world.activeMapId)) {
            return;
        }
        ensureSixthQuestCastleLayout();
        Rectangle triggerBounds = firstSeal ? sixthQuestSealABounds : sixthQuestSealBBounds;
        if (!overlapsPlayerExpanded(triggerBounds) && !isPlayerInsideZoneBounds(triggerBounds)) {
            return;
        }

        boolean encounterRemaining = firstSeal
                ? hasLivingEvilWizardInZone(triggerBounds)
                : hasLivingGolem1InZone(triggerBounds);
        if (encounterRemaining) {
            return;
        }
        sixthQuestStage = firstSeal ? SIXTH_QUEST_SEAL_A_DESTROY : SIXTH_QUEST_SEAL_B_DESTROY;
        showQuestNotification(
                SIXTH_QUEST_NOTIFICATION_TITLE,
                firstSeal
                        ? "The first seal is exposed. Use the interact key/button to shatter it."
                        : "The second seal is exposed. Use the interact key/button to shatter it.",
                FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
    }

    private void updateSixthQuestThroneGuards() {
        if (!MAP_ID_DEMON_CASTLE.equals(world.activeMapId)) {
            return;
        }
        ensureSixthQuestCastleLayout();
        if (!overlapsPlayerExpanded(sixthQuestThroneBounds) && !isPlayerInsideZoneBounds(sixthQuestThroneBounds)) {
            return;
        }
        if (!hasLivingEvilWizardInZone(sixthQuestThroneBounds)) {
            beginSixthQuestFinalPhaseIntro();
        }
    }

    private void updateSixthQuestBossPhase1(float delta) {
        if (!MAP_ID_DEMON_CASTLE.equals(world.activeMapId)) {
            return;
        }
        ensureSixthQuestCastleLayout();
        if (!sixthQuestEncounterSpawned) {
            spawnSixthQuestBossPhase1Encounter();
            return;
        }
        sixthQuestBossCalloutTimer += Math.max(0f, delta);
        if (hudRenderer != null && sixthQuestBossCalloutTimer >= SIXTH_QUEST_BOSS_CALLOUT_SECONDS) {
            sixthQuestBossCalloutTimer = 0f;
            hudRenderer.showNotification(
                    "Demon",
                    SIXTH_QUEST_PHASE_1_CALLOUTS[sixthQuestBossCalloutIndex % SIXTH_QUEST_PHASE_1_CALLOUTS.length],
                    1.3f);
            sixthQuestBossCalloutIndex++;
        }

        FifthQuestDemonInstance boss = findFifthQuestEncounterLeader(SIXTH_QUEST_ENCOUNTER_BOSS_PHASE_1);
        if (boss == null) {
            sixthQuestEncounterSpawned = false;
            return;
        }
        if (boss.health <= boss.maxHealth * SIXTH_QUEST_BOSS_PHASE_TRANSITION_HEALTH_RATIO) {
            sixthQuestEncounterSpawned = false;
            clearFifthQuestEncounterDemons(SIXTH_QUEST_ENCOUNTER_BOSS_PHASE_1);
            startQuestDialogue(
                    QuestDialogueId.SIXTH_QUEST_PHASE_1_TRANSITION,
                    SIXTH_QUEST_PHASE_1_TRANSITION_SPEAKERS,
                    SIXTH_QUEST_PHASE_1_TRANSITION_DIALOGUE_LINES);
        }
    }

    private void updateSixthQuestFinalPhaseBattle(float delta) {
        if (!MAP_ID_BOSS_ARENA_PHASE_2.equals(world.activeMapId)) {
            return;
        }
        ensureSixthQuestFinalArenaLayout();
        if (!sixthQuestEncounterSpawned) {
            spawnSixthQuestFinalPhaseEncounter();
            return;
        }
        sixthQuestBossCalloutTimer += Math.max(0f, delta);
        if (hudRenderer != null && sixthQuestBossCalloutTimer >= SIXTH_QUEST_BOSS_CALLOUT_SECONDS) {
            sixthQuestBossCalloutTimer = 0f;
            hudRenderer.showNotification(
                    "Demon",
                    SIXTH_QUEST_PHASE_2_CALLOUTS[sixthQuestBossCalloutIndex % SIXTH_QUEST_PHASE_2_CALLOUTS.length],
                    1.25f);
            sixthQuestBossCalloutIndex++;
        }

        BossArenaPhase2DemonInstance boss = findBossArenaPhase2Leader();
        if (boss == null) {
            if (hudRenderer != null) {
                hudRenderer.showNotification("Demon", "This... cannot be...", 1.8f);
                hudRenderer.showNotification(characterName, "It already is.", 1.8f);
            }
            sixthQuestEncounterSpawned = false;
            beginSixthQuestEnding();
            return;
        }
        if (!sixthQuestLowHealthExchangePlayed && boss.health <= boss.maxHealth * 0.25f) {
            sixthQuestLowHealthExchangePlayed = true;
            if (hudRenderer != null) {
                hudRenderer.showNotification("Demon", "I am fire eternal!", 1.7f);
                hudRenderer.showNotification(characterName, "No. You were only the night. And night ends.", 2.2f);
            }
        }
    }

    private void spawnSixthQuestOuterAssaultEncounter() {
        ensureSixthQuestCastleLayout();
        clearFifthQuestEncounterDemons(SIXTH_QUEST_ENCOUNTER_OUTER);
        float baseX = sixthQuestOuterCastleBounds.x + sixthQuestOuterCastleBounds.width * 0.5f;
        float baseY = sixthQuestOuterCastleBounds.y + sixthQuestOuterCastleBounds.height * 0.5f;
        Rectangle zoneBounds = new Rectangle(sixthQuestOuterCastleBounds);
        addFifthQuestDemonAt(baseX - 54f, baseY + 18f, FifthQuestDemonType.DEMON_1, SIXTH_QUEST_ENCOUNTER_OUTER,
                zoneBounds, FIFTH_QUEST_DEMON_1_HEALTH + 10f, FIFTH_QUEST_DEMON_1_DAMAGE + 3f, FIFTH_QUEST_DEMON_1_SPEED + 3f);
        addFifthQuestDemonAt(baseX - 8f, baseY + 30f, FifthQuestDemonType.DEMON_1, SIXTH_QUEST_ENCOUNTER_OUTER,
                zoneBounds, FIFTH_QUEST_DEMON_1_HEALTH + 10f, FIFTH_QUEST_DEMON_1_DAMAGE + 3f, FIFTH_QUEST_DEMON_1_SPEED + 3f);
        addFifthQuestDemonAt(baseX + 42f, baseY + 10f, FifthQuestDemonType.DEMON_2, SIXTH_QUEST_ENCOUNTER_OUTER,
                zoneBounds, FIFTH_QUEST_DEMON_2_HEALTH + 16f, FIFTH_QUEST_DEMON_2_DAMAGE + 4f, FIFTH_QUEST_DEMON_2_SPEED + 3f);
        addFifthQuestDemonAt(baseX + 76f, baseY - 18f, FifthQuestDemonType.DEMON_1, SIXTH_QUEST_ENCOUNTER_OUTER,
                zoneBounds, FIFTH_QUEST_DEMON_1_HEALTH + 12f, FIFTH_QUEST_DEMON_1_DAMAGE + 3f, FIFTH_QUEST_DEMON_1_SPEED + 4f);
        sixthQuestEncounterSpawned = true;
        if (hudRenderer != null) {
            hudRenderer.showNotification(
                    "Final Reminder",
                    "Use E Dash Attack to break through and Spatial Dash to reposition when the hall closes around you.",
                    FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
        }
    }

    private void spawnSixthQuestSealAEncounter() {
        clearFifthQuestEncounterDemons(SIXTH_QUEST_ENCOUNTER_SEAL_A);
        float baseX = sixthQuestSealABounds.x + sixthQuestSealABounds.width * 0.5f;
        float baseY = sixthQuestSealABounds.y + sixthQuestSealABounds.height * 0.5f;
        Rectangle zoneBounds = new Rectangle(sixthQuestSealABounds);
        addFifthQuestDemonAt(baseX - 42f, baseY + 10f, FifthQuestDemonType.DEMON_1, SIXTH_QUEST_ENCOUNTER_SEAL_A,
                zoneBounds, FIFTH_QUEST_DEMON_1_HEALTH + 12f, FIFTH_QUEST_DEMON_1_DAMAGE + 4f, FIFTH_QUEST_DEMON_1_SPEED + 3f);
        addFifthQuestDemonAt(baseX + 12f, baseY + 24f, FifthQuestDemonType.DEMON_2, SIXTH_QUEST_ENCOUNTER_SEAL_A,
                zoneBounds, FIFTH_QUEST_DEMON_2_HEALTH + 18f, FIFTH_QUEST_DEMON_2_DAMAGE + 4f, FIFTH_QUEST_DEMON_2_SPEED + 4f);
        addFifthQuestDemonAt(baseX + 52f, baseY - 14f, FifthQuestDemonType.DEMON_2, SIXTH_QUEST_ENCOUNTER_SEAL_A,
                zoneBounds, FIFTH_QUEST_DEMON_2_HEALTH + 14f, FIFTH_QUEST_DEMON_2_DAMAGE + 3f, FIFTH_QUEST_DEMON_2_SPEED + 4f);
        sixthQuestEncounterSpawned = true;
    }

    private void spawnSixthQuestSealBEncounter() {
        clearFifthQuestEncounterDemons(SIXTH_QUEST_ENCOUNTER_SEAL_B);
        float baseX = sixthQuestSealBBounds.x + sixthQuestSealBBounds.width * 0.5f;
        float baseY = sixthQuestSealBBounds.y + sixthQuestSealBBounds.height * 0.5f;
        Rectangle zoneBounds = new Rectangle(sixthQuestSealBBounds);
        addFifthQuestDemonAt(baseX - 48f, baseY + 12f, FifthQuestDemonType.DEMON_2, SIXTH_QUEST_ENCOUNTER_SEAL_B,
                zoneBounds, FIFTH_QUEST_DEMON_2_HEALTH + 22f, FIFTH_QUEST_DEMON_2_DAMAGE + 5f, FIFTH_QUEST_DEMON_2_SPEED + 5f);
        addFifthQuestDemonAt(baseX + 12f, baseY + 22f, FifthQuestDemonType.DEMON_2, SIXTH_QUEST_ENCOUNTER_SEAL_B,
                zoneBounds, FIFTH_QUEST_DEMON_2_HEALTH + 18f, FIFTH_QUEST_DEMON_2_DAMAGE + 4f, FIFTH_QUEST_DEMON_2_SPEED + 4f);
        addFifthQuestDemonAt(baseX + 56f, baseY - 10f, FifthQuestDemonType.DEMON_3, SIXTH_QUEST_ENCOUNTER_SEAL_B,
                zoneBounds, FIFTH_QUEST_DEMON_3_HEALTH + 28f, FIFTH_QUEST_DEMON_3_DAMAGE + 6f, FIFTH_QUEST_DEMON_3_SPEED + 4f);
        sixthQuestEncounterSpawned = true;
    }

    private void spawnSixthQuestThroneGuardEncounter() {
        clearFifthQuestEncounterDemons(SIXTH_QUEST_ENCOUNTER_THRONE_GUARDS);
        float baseX = sixthQuestThroneBounds.x + sixthQuestThroneBounds.width * 0.5f;
        float baseY = sixthQuestThroneBounds.y + sixthQuestThroneBounds.height * 0.5f;
        Rectangle zoneBounds = new Rectangle(sixthQuestThroneBounds);
        addFifthQuestDemonAt(baseX - 64f, baseY + 12f, FifthQuestDemonType.DEMON_2, SIXTH_QUEST_ENCOUNTER_THRONE_GUARDS,
                zoneBounds, FIFTH_QUEST_DEMON_2_HEALTH + 22f, FIFTH_QUEST_DEMON_2_DAMAGE + 5f, FIFTH_QUEST_DEMON_2_SPEED + 5f);
        addFifthQuestDemonAt(baseX - 18f, baseY + 28f, FifthQuestDemonType.DEMON_3, SIXTH_QUEST_ENCOUNTER_THRONE_GUARDS,
                zoneBounds, FIFTH_QUEST_DEMON_3_HEALTH + 24f, FIFTH_QUEST_DEMON_3_DAMAGE + 6f, FIFTH_QUEST_DEMON_3_SPEED + 4f);
        addFifthQuestDemonAt(baseX + 34f, baseY + 6f, FifthQuestDemonType.DEMON_2, SIXTH_QUEST_ENCOUNTER_THRONE_GUARDS,
                zoneBounds, FIFTH_QUEST_DEMON_2_HEALTH + 24f, FIFTH_QUEST_DEMON_2_DAMAGE + 5f, FIFTH_QUEST_DEMON_2_SPEED + 5f);
        addFifthQuestDemonAt(baseX + 82f, baseY - 18f, FifthQuestDemonType.DEMON_3, SIXTH_QUEST_ENCOUNTER_THRONE_GUARDS,
                zoneBounds, FIFTH_QUEST_DEMON_3_HEALTH + 26f, FIFTH_QUEST_DEMON_3_DAMAGE + 6f, FIFTH_QUEST_DEMON_3_SPEED + 4f);
        sixthQuestEncounterSpawned = true;
    }

    private void spawnSixthQuestBossPhase1Encounter() {
        clearFifthQuestEncounterDemons(SIXTH_QUEST_ENCOUNTER_BOSS_PHASE_1);
        float spawnX = sixthQuestBossPhase1SpawnBounds.x + sixthQuestBossPhase1SpawnBounds.width * 0.5f;
        float spawnY = sixthQuestBossPhase1SpawnBounds.y + sixthQuestBossPhase1SpawnBounds.height * 0.5f;
        addFifthQuestDemonAt(
                spawnX,
                spawnY,
                FifthQuestDemonType.COMMANDER,
                SIXTH_QUEST_ENCOUNTER_BOSS_PHASE_1,
                new Rectangle(sixthQuestThroneBounds),
                SIXTH_QUEST_BOSS_PHASE_1_HEALTH,
                SIXTH_QUEST_BOSS_PHASE_1_DAMAGE,
                SIXTH_QUEST_BOSS_PHASE_1_SPEED);
        sixthQuestEncounterSpawned = true;
    }

    private void spawnSixthQuestFinalPhaseEncounter() {
        clearFifthQuestEncounterDemons(SIXTH_QUEST_ENCOUNTER_BOSS_PHASE_2);
        if (bossArenaPhase2Demons.size == 0) {
            configureBossArenaPhase2Demons();
        }
        sixthQuestEncounterSpawned = bossArenaPhase2Demons.size > 0;
    }

    private FifthQuestDemonInstance findFifthQuestEncounterLeader(int encounterId) {
        for (int i = 0; i < fifthQuestDemons.size; i++) {
            FifthQuestDemonInstance demon = fifthQuestDemons.get(i);
            if (demon.questEncounterId == encounterId && !demon.dying && demon.health > 0f) {
                return demon;
            }
        }
        return null;
    }

    private BossArenaPhase2DemonInstance findBossArenaPhase2Leader() {
        for (int i = 0; i < bossArenaPhase2Demons.size; i++) {
            BossArenaPhase2DemonInstance demon = bossArenaPhase2Demons.get(i);
            if (demon.health > 0f) {
                return demon;
            }
        }
        return null;
    }

    private void ensureFourthQuestVillageLayout() {
        if (!MAP_ID_VILLAGE.equals(world.activeMapId) || fourthQuestVillageLayoutReady) {
            return;
        }
        fourthQuestCafeSiteBounds.set(0f, 0f, 0f, 0f);
        fourthQuestCenterSiteBounds.set(0f, 0f, 0f, 0f);
        fourthQuestEdgeSiteBounds.set(0f, 0f, 0f, 0f);
        fourthQuestBreachSiteBounds.set(0f, 0f, 0f, 0f);

        boolean hasArea1 = setFourthQuestVillageSiteBoundsFromLayer(
                fourthQuestCafeSiteBounds,
                FOURTH_QUEST_AREA_1_LAYER_NAME);
        boolean hasArea2 = setFourthQuestVillageSiteBoundsFromLayer(
                fourthQuestCenterSiteBounds,
                FOURTH_QUEST_AREA_2_LAYER_NAME);
        boolean hasArea3 = setFourthQuestVillageSiteBoundsFromLayer(
                fourthQuestEdgeSiteBounds,
                FOURTH_QUEST_AREA_3_LAYER_NAME);

        Rectangle cafeDoorBounds = firstQuestCafeDoorBounds();
        Rectangle labEntranceBounds = objectLayerBounds(findLayerByName(FOURTH_QUEST_LAB_ENTRANCE_LAYER_NAME));
        Rectangle innEntranceBounds = objectLayerBounds(findLayerByName(THIRD_QUEST_INN_ENTRANCE_LAYER_NAME));
        Rectangle villageEdgeBounds = objectLayerBounds(findLayerByName(FOURTH_QUEST_VILLAGE_EDGE_LAYER_NAME));
        if (!hasArea1 && cafeDoorBounds != null) {
            setCenteredRectangle(
                    fourthQuestCafeSiteBounds,
                    cafeDoorBounds.x + cafeDoorBounds.width * 0.5f,
                    cafeDoorBounds.y + cafeDoorBounds.height * 0.5f - 12f,
                    FOURTH_QUEST_SITE_WIDTH,
                    FOURTH_QUEST_SITE_HEIGHT);
            clampRectangleToMap(fourthQuestCafeSiteBounds);
            hasArea1 = true;
        }

        if (!hasArea2 && cafeDoorBounds != null && labEntranceBounds != null && innEntranceBounds != null) {
            float centerSiteX = (cafeDoorBounds.x + innEntranceBounds.x + labEntranceBounds.x) / 3f + 120f;
            float centerSiteY = (cafeDoorBounds.y + innEntranceBounds.y + labEntranceBounds.y) / 3f + 20f;
            setCenteredRectangle(
                    fourthQuestCenterSiteBounds,
                    centerSiteX,
                    centerSiteY,
                    FOURTH_QUEST_SITE_WIDTH + 8f,
                    FOURTH_QUEST_SITE_HEIGHT + 6f);
            clampRectangleToMap(fourthQuestCenterSiteBounds);
            hasArea2 = true;
        }

        float edgeCenterX = 0f;
        float edgeCenterY = 0f;
        if (hasArea3) {
            edgeCenterX = fourthQuestEdgeSiteBounds.x + fourthQuestEdgeSiteBounds.width * 0.5f;
            edgeCenterY = fourthQuestEdgeSiteBounds.y + fourthQuestEdgeSiteBounds.height * 0.5f;
        } else if (villageEdgeBounds != null) {
            edgeCenterX = villageEdgeBounds.x + villageEdgeBounds.width * 0.5f + 24f;
            edgeCenterY = villageEdgeBounds.y + villageEdgeBounds.height * 0.5f + 8f;
            setCenteredRectangle(
                    fourthQuestEdgeSiteBounds,
                    edgeCenterX,
                    edgeCenterY,
                    FOURTH_QUEST_SITE_WIDTH + 14f,
                    FOURTH_QUEST_SITE_HEIGHT + 10f);
            clampRectangleToMap(fourthQuestEdgeSiteBounds);
            hasArea3 = true;
        }

        if (hasArea3) {
            setCenteredRectangle(
                    fourthQuestBreachSiteBounds,
                    edgeCenterX + 16f,
                    edgeCenterY - 10f,
                    FOURTH_QUEST_BREACH_SITE_SIZE,
                    FOURTH_QUEST_BREACH_SITE_SIZE);
            clampRectangleToMap(fourthQuestBreachSiteBounds);
        }

        fourthQuestVillageLayoutReady = hasArea1
                && hasArea2
                && hasArea3
                && fourthQuestBreachSiteBounds.width > 0f
                && fourthQuestBreachSiteBounds.height > 0f;
    }

    private boolean setFourthQuestVillageSiteBoundsFromLayer(Rectangle targetBounds, String layerName) {
        if (targetBounds == null || layerName == null || layerName.isBlank()) {
            return false;
        }
        Rectangle layerBounds = objectLayerBounds(findLayerByName(layerName));
        if (!setRectangle(targetBounds, layerBounds)) {
            return false;
        }
        clampRectangleToMap(targetBounds);
        return true;
    }

    private void ensureFourthQuestFloor2Layout() {
        if (!MAP_ID_RESEARCH_LAB_FLOOR_2.equals(world.activeMapId) || fourthQuestFloor2LayoutReady) {
            return;
        }
        Rectangle table1Bounds = secondQuestArchiveBounds();
        Rectangle table2Bounds = tiledLayerBounds(findLayerByName("Table 2"));
        Rectangle stairsBounds = objectLayerBounds(findLayerByName(FOURTH_QUEST_LAB_FLOOR_2_EXIT_LAYER_NAME));
        Rectangle mapBounds = world.mapBounds;
        if (mapBounds == null || mapBounds.width <= 0f || mapBounds.height <= 0f) {
            return;
        }

        float centerX = mapBounds.x + mapBounds.width * 0.5f;
        float centerY = mapBounds.y + mapBounds.height * 0.54f;
        setCenteredRectangle(fourthQuestArrayConsoleBounds, centerX, centerY, 44f, 34f);

        if (table1Bounds != null) {
            setCenteredRectangle(
                    fourthQuestArrayNodeBounds[0],
                    table1Bounds.x + table1Bounds.width * 0.5f,
                    table1Bounds.y + table1Bounds.height * 0.5f,
                    26f,
                    26f);
        } else {
            setCenteredRectangle(fourthQuestArrayNodeBounds[0], centerX - 72f, centerY + 30f, 26f, 26f);
        }
        if (table2Bounds != null) {
            setCenteredRectangle(
                    fourthQuestArrayNodeBounds[1],
                    table2Bounds.x + table2Bounds.width * 0.5f,
                    table2Bounds.y + table2Bounds.height * 0.5f,
                    26f,
                    26f);
        } else {
            setCenteredRectangle(fourthQuestArrayNodeBounds[1], centerX + 64f, centerY + 24f, 26f, 26f);
        }
        if (stairsBounds != null) {
            setCenteredRectangle(
                    fourthQuestArrayNodeBounds[2],
                    stairsBounds.x + stairsBounds.width * 0.5f + 42f,
                    stairsBounds.y + stairsBounds.height * 0.5f + 22f,
                    26f,
                    26f);
        } else {
            setCenteredRectangle(fourthQuestArrayNodeBounds[2], centerX + 8f, centerY - 54f, 26f, 26f);
        }

        fourthQuestFloor2LayoutReady = true;
    }

    private void setCenteredRectangle(Rectangle bounds, float centerX, float centerY, float width, float height) {
        bounds.set(centerX - width * 0.5f, centerY - height * 0.5f, width, height);
    }

    private void startFourthQuest() {
        fourthQuestStage = FOURTH_QUEST_REPORT_TO_SCIENTIST;
        fourthQuestEncounterSpawned = false;
        fourthQuestArrayDefenseTimer = 0f;
        fourthQuestArrayDefenseWaveCount = 0;
        fourthQuestBreachCalloutTimer = 0f;
        fourthQuestBreachCalloutIndex = 0;
        showQuestNotification(
                FOURTH_QUEST_NOTIFICATION_TITLE,
                "Objective Updated: Report to the Scientist at the Research Lab.",
                FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
    }

    private void beginFourthQuestCafeBattle() {
        fourthQuestStage = FOURTH_QUEST_CAFE_BATTLE;
        fourthQuestEncounterSpawned = false;
        showQuestNotification(
                "Dash Attack",
                "Press E to lunge forward with an attack.",
                FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
    }

    private void beginFourthQuestCenterBattle() {
        fourthQuestStage = FOURTH_QUEST_CENTER_BATTLE;
        fourthQuestEncounterSpawned = false;
        showQuestNotification(
                FOURTH_QUEST_NOTIFICATION_TITLE,
                "Objective Updated: Clear the corruption threatening the village center.",
                FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
    }

    private void beginFourthQuestEdgeBattle() {
        fourthQuestStage = FOURTH_QUEST_EDGE_BATTLE;
        fourthQuestEncounterSpawned = false;
        fourthQuestBreachCalloutTimer = 0f;
        fourthQuestBreachCalloutIndex = 0;
        showQuestNotification(
                FOURTH_QUEST_NOTIFICATION_TITLE,
                "Objective Updated: Destroy the strongest corrupted site near the outer road.",
                FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
    }

    private void beginFourthQuestArraySequence() {
        fourthQuestStage = FOURTH_QUEST_GO_TO_ARRAY;
        showQuestNotification(
                FOURTH_QUEST_NOTIFICATION_TITLE,
                "Objective Updated: Activate the Resonance Array on Floor 2.",
                FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
        if (hudRenderer != null) {
            hudRenderer.showNotification(
                    "Scientist",
                    "Realign the outer nodes first. The central array will not respond until all three are active.",
                    FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
        }
    }

    private void beginFourthQuestArrayDefense() {
        fourthQuestStage = FOURTH_QUEST_ARRAY_DEFENSE;
        fourthQuestEncounterSpawned = false;
        fourthQuestArrayDefenseTimer = 0f;
        fourthQuestArrayDefenseWaveCount = 0;
        clearFourthQuestEncounterBats(FOURTH_QUEST_ENCOUNTER_ARRAY);
        showQuestNotification(
                FOURTH_QUEST_NOTIFICATION_TITLE,
                "Objective Updated: Protect the Resonance Array until the scan is complete.",
                FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
    }

    private void beginFourthQuestBreachBattle() {
        fourthQuestStage = FOURTH_QUEST_BREACH_BATTLE;
        fourthQuestEncounterSpawned = false;
        fourthQuestBreachCalloutTimer = 0f;
        fourthQuestBreachCalloutIndex = 0;
        showQuestNotification(
                FOURTH_QUEST_NOTIFICATION_TITLE,
                "Objective Updated: Close the breach before it stabilizes.",
                FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
    }

    private void updateFourthQuestCafeBattle() {
        if (fourthQuestStage != FOURTH_QUEST_CAFE_BATTLE || !MAP_ID_VILLAGE.equals(world.activeMapId)) {
            return;
        }
        if (!fourthQuestEncounterSpawned) {
            spawnFourthQuestCafeEncounter();
        }
        if (fourthQuestEncounterSpawned && countFourthQuestEncounterBats(FOURTH_QUEST_ENCOUNTER_CAFE) <= 0) {
            fourthQuestEncounterSpawned = false;
            showQuestNotification(
                    FOURTH_QUEST_NOTIFICATION_TITLE,
                    "Ash Sample I obtained. Investigate the corrupted site in the village center.",
                    FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
            if (hudRenderer != null) {
                hudRenderer.showNotification(
                        characterName,
                        "This is more than residue. It is spreading.",
                        FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
            }
            fourthQuestStage = FOURTH_QUEST_EXAMINE_CENTER_MARK;
        }
    }

    private void updateFourthQuestCenterBattle() {
        if (fourthQuestStage != FOURTH_QUEST_CENTER_BATTLE || !MAP_ID_VILLAGE.equals(world.activeMapId)) {
            return;
        }
        if (!fourthQuestEncounterSpawned) {
            spawnFourthQuestCenterEncounter();
        }
        if (fourthQuestEncounterSpawned && countFourthQuestEncounterBats(FOURTH_QUEST_ENCOUNTER_CENTER) <= 0) {
            fourthQuestEncounterSpawned = false;
            showQuestNotification(
                    FOURTH_QUEST_NOTIFICATION_TITLE,
                    "Ash Sample II obtained. Investigate the final corrupted site near the outer road.",
                    FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
            startQuestDialogue(
                    QuestDialogueId.FOURTH_QUEST_CENTER_AFTER,
                    FOURTH_QUEST_CENTER_AFTER_SPEAKERS,
                    FOURTH_QUEST_CENTER_AFTER_DIALOGUE_LINES);
        }
    }

    private void updateFourthQuestEdgeBattle() {
        if (fourthQuestStage != FOURTH_QUEST_EDGE_BATTLE || !MAP_ID_VILLAGE.equals(world.activeMapId)) {
            return;
        }
        if (!fourthQuestEncounterSpawned) {
            spawnFourthQuestEdgeEncounter();
        }
        if (fourthQuestEncounterSpawned && countFourthQuestEncounterBats(FOURTH_QUEST_ENCOUNTER_EDGE) <= 0) {
            fourthQuestEncounterSpawned = false;
            showQuestNotification(
                    FOURTH_QUEST_NOTIFICATION_TITLE,
                    "Ash Sample III and a Broken Demon Horn were recovered.",
                    FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
            startQuestDialogue(
                    QuestDialogueId.FOURTH_QUEST_EDGE_AFTER,
                    FOURTH_QUEST_EDGE_AFTER_SPEAKERS,
                    FOURTH_QUEST_EDGE_AFTER_DIALOGUE_LINES);
        }
    }

    private void updateFourthQuestArrayDefense(float delta) {
        if (fourthQuestStage != FOURTH_QUEST_ARRAY_DEFENSE || !MAP_ID_RESEARCH_LAB_FLOOR_2.equals(world.activeMapId)) {
            return;
        }
        fourthQuestArrayDefenseTimer += Math.max(0f, delta);
        if (fourthQuestArrayDefenseWaveCount == 0) {
            spawnFourthQuestArrayDefenseWave(0);
        }
        if (fourthQuestArrayDefenseTimer >= 4.5f && fourthQuestArrayDefenseWaveCount == 1) {
            spawnFourthQuestArrayDefenseWave(1);
        }
        if (fourthQuestArrayDefenseTimer >= 9.5f && fourthQuestArrayDefenseWaveCount == 2) {
            spawnFourthQuestArrayDefenseWave(2);
        }
        if (fourthQuestArrayDefenseTimer >= FOURTH_QUEST_ARRAY_DEFENSE_SECONDS
                && countFourthQuestEncounterBats(FOURTH_QUEST_ENCOUNTER_ARRAY) <= 0) {
            fourthQuestEncounterSpawned = false;
            clearFourthQuestEncounterBats(FOURTH_QUEST_ENCOUNTER_ARRAY);
            startQuestDialogue(
                    QuestDialogueId.FOURTH_QUEST_ARRAY_SCAN_COMPLETE,
                    FOURTH_QUEST_ARRAY_SCAN_COMPLETE_SPEAKERS,
                    FOURTH_QUEST_ARRAY_SCAN_COMPLETE_DIALOGUE_LINES);
        }
    }

    private void updateFourthQuestBreachBattle(float delta) {
        if (fourthQuestStage != FOURTH_QUEST_BREACH_BATTLE || !MAP_ID_VILLAGE.equals(world.activeMapId)) {
            return;
        }
        if (!fourthQuestEncounterSpawned) {
            spawnFourthQuestBreachEncounter();
        }
        fourthQuestBreachCalloutTimer += Math.max(0f, delta);
        if (fourthQuestBreachCalloutTimer >= FOURTH_QUEST_BREACH_CALLOUT_SECONDS && hudRenderer != null) {
            fourthQuestBreachCalloutTimer = 0f;
            hudRenderer.showNotification(
                    "Father",
                    FOURTH_QUEST_BREACH_CALLOUTS[fourthQuestBreachCalloutIndex % FOURTH_QUEST_BREACH_CALLOUTS.length],
                    1.5f);
            fourthQuestBreachCalloutIndex++;
        }
        if (fourthQuestEncounterSpawned && countFourthQuestEncounterBats(FOURTH_QUEST_ENCOUNTER_BREACH) <= 0) {
            fourthQuestEncounterSpawned = false;
            clearFourthQuestEncounterBats(FOURTH_QUEST_ENCOUNTER_BREACH);
            showQuestItemCard(
                    QuestItemCardId.FOURTH_QUEST_CONDENSED_CORE,
                    "Condensed Core",
                    "You received the Condensed Core left behind by the breach.",
                    fourthQuestCondensedCoreTex,
                    0.5f,
                    0.866f,
                    0f,
                    -10f,
                    0f);
        }
    }

    private void spawnFourthQuestCafeEncounter() {
        ensureFourthQuestVillageLayout();
        clearFourthQuestEncounterBats(FOURTH_QUEST_ENCOUNTER_CAFE);
        Rectangle combatBounds = createWalkableCombatZone(fourthQuestCafeSiteBounds, 116f, 88f);
        Vector2 leftBat = questZonePoint(combatBounds, 0.18f, 0.44f);
        Vector2 upperBat = questZonePoint(combatBounds, 0.50f, 0.72f);
        Vector2 rightBat = questZonePoint(combatBounds, 0.82f, 0.34f);
        addFourthQuestBat(leftBat.x, leftBat.y, FOURTH_QUEST_ENCOUNTER_CAFE,
                FOURTH_QUEST_CORRUPTED_BAT_HEALTH, FOURTH_QUEST_CORRUPTED_BAT_DAMAGE, FOURTH_QUEST_CORRUPTED_BAT_SPEED, false);
        addFourthQuestBat(upperBat.x, upperBat.y, FOURTH_QUEST_ENCOUNTER_CAFE,
                FOURTH_QUEST_CORRUPTED_BAT_HEALTH, FOURTH_QUEST_CORRUPTED_BAT_DAMAGE, FOURTH_QUEST_CORRUPTED_BAT_SPEED, false);
        addFourthQuestBat(rightBat.x, rightBat.y, FOURTH_QUEST_ENCOUNTER_CAFE,
                FOURTH_QUEST_CORRUPTED_BAT_HEALTH, FOURTH_QUEST_CORRUPTED_BAT_DAMAGE, FOURTH_QUEST_CORRUPTED_BAT_SPEED, false);
        fourthQuestEncounterSpawned = true;
    }

    private void spawnFourthQuestCenterEncounter() {
        ensureFourthQuestVillageLayout();
        clearFourthQuestEncounterBats(FOURTH_QUEST_ENCOUNTER_CENTER);
        Rectangle combatBounds = createWalkableCombatZone(fourthQuestCenterSiteBounds, 132f, 96f);
        Vector2 leftBat = questZonePoint(combatBounds, 0.16f, 0.48f);
        Vector2 upperBat = questZonePoint(combatBounds, 0.40f, 0.74f);
        Vector2 eliteBat = questZonePoint(combatBounds, 0.68f, 0.46f);
        Vector2 rightBat = questZonePoint(combatBounds, 0.84f, 0.24f);
        addFourthQuestBat(leftBat.x, leftBat.y, FOURTH_QUEST_ENCOUNTER_CENTER,
                FOURTH_QUEST_CORRUPTED_BAT_HEALTH, FOURTH_QUEST_CORRUPTED_BAT_DAMAGE, FOURTH_QUEST_CORRUPTED_BAT_SPEED, false);
        addFourthQuestBat(upperBat.x, upperBat.y, FOURTH_QUEST_ENCOUNTER_CENTER,
                FOURTH_QUEST_CORRUPTED_BAT_HEALTH, FOURTH_QUEST_CORRUPTED_BAT_DAMAGE, FOURTH_QUEST_CORRUPTED_BAT_SPEED, false);
        addFourthQuestBat(eliteBat.x, eliteBat.y, FOURTH_QUEST_ENCOUNTER_CENTER,
                FOURTH_QUEST_CENTER_ELITE_HEALTH, FOURTH_QUEST_CENTER_ELITE_DAMAGE, FOURTH_QUEST_CORRUPTED_BAT_SPEED + 2f, true);
        addFourthQuestBat(rightBat.x, rightBat.y, FOURTH_QUEST_ENCOUNTER_CENTER,
                FOURTH_QUEST_CORRUPTED_BAT_HEALTH, FOURTH_QUEST_CORRUPTED_BAT_DAMAGE, FOURTH_QUEST_CORRUPTED_BAT_SPEED, false);
        fourthQuestEncounterSpawned = true;
    }

    private void spawnFourthQuestEdgeEncounter() {
        ensureFourthQuestVillageLayout();
        clearFourthQuestEncounterBats(FOURTH_QUEST_ENCOUNTER_EDGE);
        Rectangle combatBounds = createWalkableCombatZone(fourthQuestEdgeSiteBounds, 148f, 104f);
        Vector2 leftBat = questZonePoint(combatBounds, 0.16f, 0.50f);
        Vector2 upperBat = questZonePoint(combatBounds, 0.38f, 0.76f);
        Vector2 eliteBat = questZonePoint(combatBounds, 0.66f, 0.56f);
        Vector2 rightBat = questZonePoint(combatBounds, 0.86f, 0.28f);
        addFourthQuestBat(leftBat.x, leftBat.y, FOURTH_QUEST_ENCOUNTER_EDGE,
                FOURTH_QUEST_CORRUPTED_BAT_HEALTH + 8f, FOURTH_QUEST_CORRUPTED_BAT_DAMAGE + 2f, FOURTH_QUEST_CORRUPTED_BAT_SPEED + 2f, false);
        addFourthQuestBat(upperBat.x, upperBat.y, FOURTH_QUEST_ENCOUNTER_EDGE,
                FOURTH_QUEST_CORRUPTED_BAT_HEALTH + 8f, FOURTH_QUEST_CORRUPTED_BAT_DAMAGE + 2f, FOURTH_QUEST_CORRUPTED_BAT_SPEED + 2f, false);
        addFourthQuestBat(eliteBat.x, eliteBat.y, FOURTH_QUEST_ENCOUNTER_EDGE,
                FOURTH_QUEST_EDGE_ELITE_HEALTH, FOURTH_QUEST_EDGE_ELITE_DAMAGE, FOURTH_QUEST_CORRUPTED_BAT_SPEED + 4f, true);
        addFourthQuestBat(rightBat.x, rightBat.y, FOURTH_QUEST_ENCOUNTER_EDGE,
                FOURTH_QUEST_CORRUPTED_BAT_HEALTH + 8f, FOURTH_QUEST_CORRUPTED_BAT_DAMAGE + 2f, FOURTH_QUEST_CORRUPTED_BAT_SPEED + 2f, false);
        fourthQuestEncounterSpawned = true;
    }

    private void spawnFourthQuestArrayDefenseWave(int waveIndex) {
        ensureFourthQuestFloor2Layout();
        float centerX = fourthQuestArrayConsoleBounds.x + fourthQuestArrayConsoleBounds.width * 0.5f;
        float centerY = fourthQuestArrayConsoleBounds.y + fourthQuestArrayConsoleBounds.height * 0.5f;
        if (waveIndex == 0) {
            addFourthQuestBat(centerX - 96f, centerY + 34f, FOURTH_QUEST_ENCOUNTER_ARRAY,
                    FOURTH_QUEST_CORRUPTED_BAT_HEALTH, FOURTH_QUEST_CORRUPTED_BAT_DAMAGE, FOURTH_QUEST_CORRUPTED_BAT_SPEED, false);
            addFourthQuestBat(centerX + 88f, centerY + 28f, FOURTH_QUEST_ENCOUNTER_ARRAY,
                    FOURTH_QUEST_CORRUPTED_BAT_HEALTH, FOURTH_QUEST_CORRUPTED_BAT_DAMAGE, FOURTH_QUEST_CORRUPTED_BAT_SPEED, false);
        } else if (waveIndex == 1) {
            addFourthQuestBat(centerX - 102f, centerY - 24f, FOURTH_QUEST_ENCOUNTER_ARRAY,
                    FOURTH_QUEST_CORRUPTED_BAT_HEALTH + 6f, FOURTH_QUEST_CORRUPTED_BAT_DAMAGE + 2f, FOURTH_QUEST_CORRUPTED_BAT_SPEED + 2f, false);
            addFourthQuestBat(centerX + 96f, centerY - 18f, FOURTH_QUEST_ENCOUNTER_ARRAY,
                    FOURTH_QUEST_CORRUPTED_BAT_HEALTH + 6f, FOURTH_QUEST_CORRUPTED_BAT_DAMAGE + 2f, FOURTH_QUEST_CORRUPTED_BAT_SPEED + 2f, false);
            addFourthQuestBat(centerX - 10f, centerY + 82f, FOURTH_QUEST_ENCOUNTER_ARRAY,
                    FOURTH_QUEST_CENTER_ELITE_HEALTH, FOURTH_QUEST_CENTER_ELITE_DAMAGE, FOURTH_QUEST_CORRUPTED_BAT_SPEED + 2f, true);
        } else {
            addFourthQuestBat(centerX - 112f, centerY + 62f, FOURTH_QUEST_ENCOUNTER_ARRAY,
                    FOURTH_QUEST_CORRUPTED_BAT_HEALTH + 10f, FOURTH_QUEST_CORRUPTED_BAT_DAMAGE + 3f, FOURTH_QUEST_CORRUPTED_BAT_SPEED + 3f, false);
            addFourthQuestBat(centerX + 114f, centerY + 44f, FOURTH_QUEST_ENCOUNTER_ARRAY,
                    FOURTH_QUEST_CORRUPTED_BAT_HEALTH + 10f, FOURTH_QUEST_CORRUPTED_BAT_DAMAGE + 3f, FOURTH_QUEST_CORRUPTED_BAT_SPEED + 3f, false);
            addFourthQuestBat(centerX + 18f, centerY - 82f, FOURTH_QUEST_ENCOUNTER_ARRAY,
                    FOURTH_QUEST_CENTER_ELITE_HEALTH + 20f, FOURTH_QUEST_CENTER_ELITE_DAMAGE + 2f, FOURTH_QUEST_CORRUPTED_BAT_SPEED + 4f, true);
        }
        fourthQuestEncounterSpawned = true;
        fourthQuestArrayDefenseWaveCount = Math.max(fourthQuestArrayDefenseWaveCount, waveIndex + 1);
    }

    private void spawnFourthQuestBreachEncounter() {
        ensureFourthQuestVillageLayout();
        clearFourthQuestEncounterBats(FOURTH_QUEST_ENCOUNTER_BREACH);
        Rectangle combatBounds = createWalkableCombatZone(fourthQuestBreachSiteBounds, 176f, 132f);
        Vector2 leftBat = questZonePoint(combatBounds, 0.18f, 0.48f);
        Vector2 upperBat = questZonePoint(combatBounds, 0.40f, 0.76f);
        Vector2 eliteBat = questZonePoint(combatBounds, 0.66f, 0.54f);
        Vector2 rightBat = questZonePoint(combatBounds, 0.86f, 0.30f);
        addFourthQuestBat(leftBat.x, leftBat.y, FOURTH_QUEST_ENCOUNTER_BREACH,
                FOURTH_QUEST_CORRUPTED_BAT_HEALTH + 14f, FOURTH_QUEST_CORRUPTED_BAT_DAMAGE + 4f, FOURTH_QUEST_CORRUPTED_BAT_SPEED + 3f, false);
        addFourthQuestBat(upperBat.x, upperBat.y, FOURTH_QUEST_ENCOUNTER_BREACH,
                FOURTH_QUEST_CORRUPTED_BAT_HEALTH + 14f, FOURTH_QUEST_CORRUPTED_BAT_DAMAGE + 4f, FOURTH_QUEST_CORRUPTED_BAT_SPEED + 3f, false);
        addFourthQuestBat(eliteBat.x, eliteBat.y, FOURTH_QUEST_ENCOUNTER_BREACH,
                FOURTH_QUEST_BREACH_BOSS_HEALTH, FOURTH_QUEST_BREACH_BOSS_DAMAGE, FOURTH_QUEST_BREACH_BOSS_SPEED, true);
        addFourthQuestBat(rightBat.x, rightBat.y, FOURTH_QUEST_ENCOUNTER_BREACH,
                FOURTH_QUEST_CORRUPTED_BAT_HEALTH + 18f, FOURTH_QUEST_CORRUPTED_BAT_DAMAGE + 5f, FOURTH_QUEST_CORRUPTED_BAT_SPEED + 4f, false);
        fourthQuestEncounterSpawned = true;
    }

    private void addFourthQuestBat(float x, float y, int encounterId, float maxHealth, float playerDamage,
                                   float chaseSpeed, boolean elite) {
        BatInstance bat = new BatInstance();
        bat.position.set(x, y);
        bat.questEncounterId = encounterId;
        bat.elite = elite;
        bat.maxHealth = maxHealth;
        bat.health = maxHealth;
        bat.playerDamage = playerDamage;
        bat.chaseSpeed = chaseSpeed;
        bat.attackIntervalSeconds = VILLAGE_BAT_ATTACK_INTERVAL_SECONDS;
        bat.state = BatAnimationState.CHASING;
        bat.stateTime = 0f;
        bats.add(bat);
    }

    private void clearFourthQuestEncounterBats(int encounterId) {
        for (int i = bats.size - 1; i >= 0; i--) {
            if (bats.get(i).questEncounterId == encounterId) {
                bats.removeIndex(i);
            }
        }
    }

    private int countFourthQuestEncounterBats(int encounterId) {
        int count = 0;
        for (int i = 0; i < bats.size; i++) {
            if (bats.get(i).questEncounterId == encounterId) {
                count++;
            }
        }
        return count;
    }

    private void ensureThirdQuestTrainingLayout() {
        if (!MAP_ID_VILLAGE.equals(world.activeMapId)) {
            return;
        }
        if (thirdQuestTrainingLayoutReady) {
            return;
        }

        Rectangle trainingAreaBounds = objectLayerBounds(findLayerByName(THIRD_QUEST_TRAINING_AREA_LAYER_NAME));
        if (trainingAreaBounds == null) {
            Rectangle innEntranceBounds = objectLayerBounds(findLayerByName(THIRD_QUEST_INN_ENTRANCE_LAYER_NAME));
            if (innEntranceBounds == null) {
                return;
            }
            float fallbackCenterX = innEntranceBounds.x + innEntranceBounds.width * 0.5f + 20f;
            float fallbackCenterY = innEntranceBounds.y + innEntranceBounds.height * 0.5f + 208f;
            trainingAreaBounds = new Rectangle(
                    fallbackCenterX - THIRD_QUEST_TRAINING_ZONE_HALF_WIDTH,
                    fallbackCenterY - THIRD_QUEST_TRAINING_ZONE_HALF_HEIGHT + THIRD_QUEST_TRAINING_ZONE_Y_OFFSET,
                    THIRD_QUEST_TRAINING_ZONE_HALF_WIDTH * 2f,
                    THIRD_QUEST_TRAINING_ZONE_HALF_HEIGHT * 2f);
        }
        thirdQuestTrainingZoneBounds.set(trainingAreaBounds);
        float fatherCenterX = thirdQuestTrainingZoneBounds.x + thirdQuestTrainingZoneBounds.width * 0.5f;
        float fatherCenterY = thirdQuestTrainingZoneBounds.y + thirdQuestTrainingZoneBounds.height - 24f;
        thirdQuestFatherTrainingBounds.set(fatherCenterX - 16f, fatherCenterY - 24f, 32f, 48f);

        thirdQuestAttackDummyBounds.set(
                thirdQuestTrainingZoneBounds.x + thirdQuestTrainingZoneBounds.width * 0.56f,
                thirdQuestTrainingZoneBounds.y + thirdQuestTrainingZoneBounds.height * 0.44f,
                20f,
                34f);
        thirdQuestMovementMarkers[0].set(
                thirdQuestTrainingZoneBounds.x + 42f,
                thirdQuestTrainingZoneBounds.y + 34f);
        thirdQuestMovementMarkers[1].set(
                thirdQuestTrainingZoneBounds.x + thirdQuestTrainingZoneBounds.width - 54f,
                thirdQuestTrainingZoneBounds.y + 46f);
        thirdQuestMovementMarkers[2].set(
                thirdQuestTrainingZoneBounds.x + thirdQuestTrainingZoneBounds.width * 0.5f,
                thirdQuestTrainingZoneBounds.y + thirdQuestTrainingZoneBounds.height - 36f);
        thirdQuestSparZoneBounds.set(
                thirdQuestTrainingZoneBounds.x - 20f,
                thirdQuestTrainingZoneBounds.y - 16f,
                thirdQuestTrainingZoneBounds.width + 40f,
                thirdQuestTrainingZoneBounds.height + 32f);
        thirdQuestTrainingLayoutReady = true;
    }

    private void resetThirdQuestMovementTutorial() {
        Arrays.fill(thirdQuestMovementMarkerReached, false);
        thirdQuestMovementMarkersReached = 0;
    }

    private void beginThirdQuestMovementTutorial() {
        thirdQuestStage = THIRD_QUEST_MOVEMENT_TUTORIAL;
        resetThirdQuestMovementTutorial();
        showQuestNotification(
                THIRD_QUEST_NOTIFICATION_TITLE,
                "Movement Tutorial: Use W, A, S, and D to reach all 3 training markers.",
                FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
    }

    private void beginThirdQuestAttackTutorial() {
        thirdQuestStage = THIRD_QUEST_ATTACK_TUTORIAL;
        thirdQuestAttackDummyHitCount = 0;
        thirdQuestAttackDummyBroken = false;
        thirdQuestDummyHitRegistered = false;
        showQuestNotification(
                THIRD_QUEST_NOTIFICATION_TITLE,
                "Basic Attack Tutorial: Left click to strike the training dummy 5 times.",
                FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
    }

    private void beginThirdQuestCombatTutorial() {
        thirdQuestStage = THIRD_QUEST_COMBAT_TUTORIAL;
        clearThirdQuestTutorialBats();
        thirdQuestTutorialEnemiesSpawned = false;
        showQuestNotification(
                THIRD_QUEST_NOTIFICATION_TITLE,
                "Combat Tutorial: Defeat the training shades and attack only when their movement opens a window.",
                FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
    }

    private void beginThirdQuestFatherSpar() {
        thirdQuestSparActive = true;
        thirdQuestFatherHitRegistered = false;
        thirdQuestFatherSparHealth = THIRD_QUEST_SPAR_FATHER_MAX_HEALTH;
        thirdQuestFatherSparAttackCooldownTimer = 0.35f;
        thirdQuestFatherSparHitCooldownTimer = 0f;
        thirdQuestFatherSparElapsed = 0f;
        thirdQuestFatherSparCalloutTimer = 0f;
        thirdQuestFatherSparCalloutIndex = 0;
        thirdQuestFatherSparHitsTaken = 0;
        showQuestNotification(
                THIRD_QUEST_NOTIFICATION_TITLE,
                "Final Sparring Match: Stay alive, watch Father, and strike with intent.",
                FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
    }

    private void startThirdQuest() {
        thirdQuestStage = THIRD_QUEST_RETURN_TO_FATHER;
        thirdQuestAvailable = true;
        showQuestNotification(
                THIRD_QUEST_NOTIFICATION_TITLE,
                "Objective Updated: Return to Father in the inn room.",
                FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
    }

    private void updateThirdQuestMovementTutorial() {
        if (thirdQuestStage != THIRD_QUEST_MOVEMENT_TUTORIAL
                || !MAP_ID_VILLAGE.equals(world.activeMapId)
                || !thirdQuestTrainingLayoutReady) {
            return;
        }

        float playerCenterX = player.bounds.x + player.bounds.width * 0.5f;
        float playerCenterY = player.bounds.y + player.bounds.height * 0.5f;
        float reachDistanceSq = THIRD_QUEST_TRAINING_MARKER_REACH_DISTANCE * THIRD_QUEST_TRAINING_MARKER_REACH_DISTANCE;
        for (int i = 0; i < thirdQuestMovementMarkers.length; i++) {
            if (thirdQuestMovementMarkerReached[i]) {
                continue;
            }
            float dx = thirdQuestMovementMarkers[i].x - playerCenterX;
            float dy = thirdQuestMovementMarkers[i].y - playerCenterY;
            if (dx * dx + dy * dy > reachDistanceSq) {
                continue;
            }
            thirdQuestMovementMarkerReached[i] = true;
            thirdQuestMovementMarkersReached++;
        }

        if (thirdQuestMovementMarkersReached >= thirdQuestMovementMarkers.length) {
            startQuestDialogue(
                    QuestDialogueId.THIRD_QUEST_FATHER_AFTER_MOVEMENT,
                    THIRD_QUEST_FATHER_AFTER_MOVEMENT_SPEAKERS,
                    THIRD_QUEST_FATHER_AFTER_MOVEMENT_DIALOGUE_LINES);
        }
    }

    private void updateThirdQuestAttackTutorial() {
        if (thirdQuestStage != THIRD_QUEST_ATTACK_TUTORIAL
                || !MAP_ID_VILLAGE.equals(world.activeMapId)
                || !thirdQuestTrainingLayoutReady) {
            thirdQuestDummyHitRegistered = false;
            return;
        }
        if (!player.attacking || activeAttackAnim == null) {
            thirdQuestDummyHitRegistered = false;
            return;
        }
        if (player.attackTimer < 0.05f) {
            thirdQuestDummyHitRegistered = false;
        }
        if (thirdQuestDummyHitRegistered || !isThirdQuestTargetWithinPlayerAttackRange(thirdQuestAttackDummyBounds)) {
            return;
        }
        thirdQuestDummyHitRegistered = true;
        thirdQuestAttackDummyHitCount = Math.min(THIRD_QUEST_DUMMY_HITS_REQUIRED, thirdQuestAttackDummyHitCount + 1);
        if (thirdQuestAttackDummyHitCount >= THIRD_QUEST_DUMMY_HITS_REQUIRED) {
            thirdQuestAttackDummyBroken = true;
            startQuestDialogue(
                    QuestDialogueId.THIRD_QUEST_FATHER_AFTER_ATTACK,
                    THIRD_QUEST_FATHER_AFTER_ATTACK_SPEAKERS,
                    THIRD_QUEST_FATHER_AFTER_ATTACK_DIALOGUE_LINES);
        }
    }

    private void updateThirdQuestCombatTutorial() {
        if (thirdQuestStage != THIRD_QUEST_COMBAT_TUTORIAL || !MAP_ID_VILLAGE.equals(world.activeMapId)) {
            return;
        }
        if (!thirdQuestTutorialEnemiesSpawned) {
            spawnThirdQuestTutorialBats();
        }
        if (countThirdQuestTutorialBats() <= 0) {
            thirdQuestStage = THIRD_QUEST_SPAR_WITH_FATHER;
            startQuestDialogue(
                    QuestDialogueId.THIRD_QUEST_FATHER_PRE_SPAR,
                    THIRD_QUEST_FATHER_PRE_SPAR_SPEAKERS,
                    THIRD_QUEST_FATHER_PRE_SPAR_DIALOGUE_LINES);
        }
    }

    private void updateThirdQuestFatherSpar(float delta) {
        if (thirdQuestStage != THIRD_QUEST_SPAR_WITH_FATHER
                || !thirdQuestSparActive
                || !MAP_ID_VILLAGE.equals(world.activeMapId)
                || !thirdQuestTrainingLayoutReady) {
            thirdQuestFatherHitRegistered = false;
            return;
        }

        thirdQuestFatherSparAttackCooldownTimer = Math.max(0f, thirdQuestFatherSparAttackCooldownTimer - Math.max(0f, delta));
        thirdQuestFatherSparHitCooldownTimer = Math.max(0f, thirdQuestFatherSparHitCooldownTimer - Math.max(0f, delta));
        thirdQuestFatherSparElapsed += Math.max(0f, delta);
        thirdQuestFatherSparCalloutTimer += Math.max(0f, delta);

        if (thirdQuestFatherSparCalloutTimer >= THIRD_QUEST_SPAR_CALL_OUT_SECONDS && hudRenderer != null) {
            thirdQuestFatherSparCalloutTimer = 0f;
            hudRenderer.showNotification(
                    "Father",
                    THIRD_QUEST_SPAR_CALLOUTS[thirdQuestFatherSparCalloutIndex % THIRD_QUEST_SPAR_CALLOUTS.length],
                    1.6f);
            thirdQuestFatherSparCalloutIndex++;
        }

        if (!player.attacking || activeAttackAnim == null) {
            thirdQuestFatherHitRegistered = false;
        } else {
            if (player.attackTimer < 0.05f) {
                thirdQuestFatherHitRegistered = false;
            }
            if (!thirdQuestFatherHitRegistered
                    && thirdQuestFatherSparHitCooldownTimer <= 0f
                    && isThirdQuestTargetWithinPlayerAttackRange(thirdQuestFatherTrainingBounds)) {
                thirdQuestFatherHitRegistered = true;
                thirdQuestFatherSparHitCooldownTimer = THIRD_QUEST_SPAR_FATHER_HIT_COOLDOWN;
                thirdQuestFatherSparHitsTaken++;
                thirdQuestFatherSparHealth = Math.max(0f,
                        thirdQuestFatherSparHealth - BAT_HIT_DAMAGE * currentPlayerAttackDamageMultiplier());
            }
        }

        float fatherCenterX = thirdQuestFatherTrainingBounds.x + thirdQuestFatherTrainingBounds.width * 0.5f;
        float fatherCenterY = thirdQuestFatherTrainingBounds.y + thirdQuestFatherTrainingBounds.height * 0.5f;
        float playerCenterX = player.bounds.x + player.bounds.width * 0.5f;
        float playerCenterY = player.bounds.y + player.bounds.height * 0.5f;
        float dx = playerCenterX - fatherCenterX;
        float dy = playerCenterY - fatherCenterY;
        float distanceSq = dx * dx + dy * dy;

        if (distanceSq <= THIRD_QUEST_SPAR_FATHER_ATTACK_RANGE * THIRD_QUEST_SPAR_FATHER_ATTACK_RANGE
                && thirdQuestFatherSparAttackCooldownTimer <= 0f
                && player.currentHealth > 0f
                && !isPlayerHazardHitReactionActive()) {
            applyDamageToPlayer(THIRD_QUEST_SPAR_FATHER_DAMAGE);
            thirdQuestFatherSparAttackCooldownTimer = THIRD_QUEST_SPAR_FATHER_ATTACK_COOLDOWN;
        }

        boolean sparThresholdReached = thirdQuestFatherSparHealth <= THIRD_QUEST_SPAR_FATHER_MAX_HEALTH * 0.35f
                || thirdQuestFatherSparHitsTaken >= THIRD_QUEST_SPAR_HITS_TO_WIN
                || (thirdQuestFatherSparElapsed >= THIRD_QUEST_SPAR_SURVIVE_SECONDS && thirdQuestFatherSparHitsTaken >= 3);
        if (sparThresholdReached) {
            thirdQuestSparActive = false;
            startQuestDialogue(
                    QuestDialogueId.THIRD_QUEST_FATHER_COMPLETE,
                    THIRD_QUEST_FATHER_COMPLETE_SPEAKERS,
                    THIRD_QUEST_FATHER_COMPLETE_DIALOGUE_LINES);
        }
    }

    private boolean isThirdQuestTargetWithinPlayerAttackRange(Rectangle targetBounds) {
        if (targetBounds == null || !player.attacking || activeAttackAnim == null) {
            return false;
        }
        float hitRange = playerBatHitRange();
        float hitRangeSq = hitRange * hitRange;
        float playerCenterX = player.bounds.x + player.bounds.width * 0.5f;
        float playerCenterY = player.bounds.y + player.bounds.height * 0.5f;
        float targetCenterX = targetBounds.x + targetBounds.width * 0.5f;
        float targetCenterY = targetBounds.y + targetBounds.height * 0.5f;
        float dx = playerCenterX - targetCenterX;
        float dy = playerCenterY - targetCenterY;
        return dx * dx + dy * dy <= hitRangeSq;
    }

    private void spawnThirdQuestTutorialBats() {
        if (!thirdQuestTrainingLayoutReady) {
            return;
        }
        clearThirdQuestTutorialBats();
        float baseX = thirdQuestTrainingZoneBounds.x + thirdQuestTrainingZoneBounds.width * 0.5f;
        float baseY = thirdQuestTrainingZoneBounds.y + thirdQuestTrainingZoneBounds.height * 0.58f;
        addTutorialBatAt(baseX - 54f, baseY + 12f);
        addTutorialBatAt(baseX + 18f, baseY + 24f);
        addTutorialBatAt(baseX + 68f, baseY - 10f);
        thirdQuestTutorialEnemiesSpawned = true;
    }

    private void clearThirdQuestTutorialBats() {
        for (int i = bats.size - 1; i >= 0; i--) {
            if (bats.get(i).tutorialEnemy) {
                bats.removeIndex(i);
            }
        }
        thirdQuestTutorialEnemiesSpawned = false;
    }

    private int countThirdQuestTutorialBats() {
        int count = 0;
        for (int i = 0; i < bats.size; i++) {
            if (bats.get(i).tutorialEnemy) {
                count++;
            }
        }
        return count;
    }

    private boolean isFirstQuestOverlayActive() {
        return firstQuestDialogueActive
                || firstQuestItemCardVisible
                || sixthQuestPortalChoiceVisible
                || fifthQuestDeathChoiceVisible;
    }

    private boolean handleFirstQuestOverlayInput() {
        if (!isFirstQuestOverlayActive()) {
            return false;
        }
        if (fifthQuestDeathChoiceVisible) {
            return handleFifthQuestDeathChoiceInput();
        }
        if (sixthQuestPortalChoiceVisible) {
            return handleSixthQuestPortalChoiceInput();
        }
        boolean advancePressed = Gdx.input.isKeyJustPressed(Input.Keys.E)
                || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                || Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);
        if (!advancePressed) {
            return true;
        }

        if (firstQuestDialogueActive) {
            if (!isCurrentQuestDialogueLineFullyVisible()) {
                revealCurrentQuestDialogueLineImmediately();
                return true;
            }
            firstQuestDialogueLineIndex++;
            if (firstQuestDialogueLineIndex >= activeQuestDialogueLines.length) {
                firstQuestDialogueActive = false;
                resetQuestDialogueTypewriter();
                stopActiveDialogueVoiceSfx();
                handleCompletedQuestDialogue();
            } else {
                resetQuestDialogueTypewriter();
                playCurrentQuestDialogueVoice();
            }
            return true;
        }

        if (firstQuestItemCardVisible) {
            firstQuestItemCardVisible = false;
            handleCompletedQuestItemCard();
            return true;
        }

        return true;
    }

    private boolean handleFifthQuestDeathChoiceInput() {
        if (!fifthQuestDeathChoiceVisible) {
            return false;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)
                || Gdx.input.isKeyJustPressed(Input.Keys.A)
                || Gdx.input.isKeyJustPressed(Input.Keys.UP)
                || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            fifthQuestDeathChoiceIndex = 0;
            return true;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)
                || Gdx.input.isKeyJustPressed(Input.Keys.D)
                || Gdx.input.isKeyJustPressed(Input.Keys.DOWN)
                || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            fifthQuestDeathChoiceIndex = 1;
            return true;
        }
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && uiCamera != null) {
            fifthQuestDeathChoiceUiPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
            uiCamera.unproject(fifthQuestDeathChoiceUiPoint);
            float uiX = fifthQuestDeathChoiceUiPoint.x;
            float uiY = fifthQuestDeathChoiceUiPoint.y;
            if (fifthQuestDeathChoiceQuestBounds.contains(uiX, uiY)) {
                fifthQuestDeathChoiceIndex = 0;
                confirmFifthQuestDeathChoice(true);
                return true;
            }
            if (fifthQuestDeathChoiceFightBounds.contains(uiX, uiY)) {
                fifthQuestDeathChoiceIndex = 1;
                confirmFifthQuestDeathChoice(false);
                return true;
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            confirmFifthQuestDeathChoice(fifthQuestDeathChoiceIndex == 0);
            return true;
        }
        return true;
    }

    private boolean handleSixthQuestPortalChoiceInput() {
        if (!sixthQuestPortalChoiceVisible) {
            return false;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)
                || Gdx.input.isKeyJustPressed(Input.Keys.A)
                || Gdx.input.isKeyJustPressed(Input.Keys.UP)
                || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            sixthQuestPortalChoiceIndex = 0;
            return true;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)
                || Gdx.input.isKeyJustPressed(Input.Keys.D)
                || Gdx.input.isKeyJustPressed(Input.Keys.DOWN)
                || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            sixthQuestPortalChoiceIndex = 1;
            return true;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            confirmSixthQuestPortalChoice(false);
            return true;
        }
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && uiCamera != null) {
            sixthQuestPortalChoiceUiPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
            uiCamera.unproject(sixthQuestPortalChoiceUiPoint);
            float uiX = sixthQuestPortalChoiceUiPoint.x;
            float uiY = sixthQuestPortalChoiceUiPoint.y;
            if (sixthQuestPortalChoiceYesBounds.contains(uiX, uiY)) {
                sixthQuestPortalChoiceIndex = 0;
                confirmSixthQuestPortalChoice(true);
                return true;
            }
            if (sixthQuestPortalChoiceNoBounds.contains(uiX, uiY)) {
                sixthQuestPortalChoiceIndex = 1;
                confirmSixthQuestPortalChoice(false);
                return true;
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            confirmSixthQuestPortalChoice(sixthQuestPortalChoiceIndex == 0);
            return true;
        }
        return true;
    }

    private void startQuestDialogue(QuestDialogueId dialogueId, String[] speakers, String[] lines) {
        stopActiveDialogueVoiceSfx();
        resetNpcDialogueVoiceSequence();
        activeQuestDialogueId = dialogueId == null ? QuestDialogueId.NONE : dialogueId;
        activeQuestDialogueSpeakers = speakers == null ? new String[0] : speakers;
        activeQuestDialogueLines = lines == null ? new String[0] : lines;
        firstQuestDialogueLineIndex = 0;
        firstQuestItemCardVisible = false;
        activeQuestItemCardId = QuestItemCardId.NONE;
        firstQuestDialogueActive = activeQuestDialogueLines.length > 0;
        resetQuestDialogueTypewriter();
        if (firstQuestDialogueActive) {
            playCurrentQuestDialogueVoice();
        }
    }

    private void showQuestItemCard(QuestItemCardId itemCardId, String title, String description, Texture texture,
                                   float titleScale, float imageScaleMultiplier,
                                   float titleOffsetY, float descriptionOffsetY, float hintOffsetY) {
        stopActiveDialogueVoiceSfx();
        resetQuestDialogueTypewriter();
        activeQuestItemCardId = itemCardId == null ? QuestItemCardId.NONE : itemCardId;
        activeQuestItemTitle = title == null ? "" : title;
        activeQuestItemDescription = description == null ? "" : description;
        activeQuestItemTexture = texture;
        activeQuestItemTitleScale = titleScale <= 0f ? 1f : titleScale;
        activeQuestItemImageScaleMultiplier = imageScaleMultiplier <= 0f ? 1f : imageScaleMultiplier;
        activeQuestItemTitleOffsetY = titleOffsetY;
        activeQuestItemDescriptionOffsetY = descriptionOffsetY;
        activeQuestItemHintOffsetY = hintOffsetY;
        firstQuestDialogueActive = false;
        firstQuestItemCardVisible = true;
    }

    private void showQuestNotification(String title, String message, float durationSeconds) {
        questGuidanceSuppressTimer = Math.max(questGuidanceSuppressTimer, Math.max(0f, durationSeconds));
        if (hudRenderer != null) {
            hudRenderer.showNotification(title, message, durationSeconds);
        }
    }

    private void updateQuestDialogueTypewriter(float delta) {
        if (!shouldTypeCurrentQuestDialogueLine()) {
            return;
        }
        String line = currentQuestDialogueLine();
        int targetCharacterCount = line.length();
        if (activeQuestDialogueVisibleCharacterCount >= targetCharacterCount) {
            return;
        }
        activeQuestDialogueTypewriterProgress += Math.max(0f, delta) * FIRST_QUEST_DIALOGUE_TYPEWRITER_CHARACTERS_PER_SECOND;
        activeQuestDialogueVisibleCharacterCount = Math.min(
                targetCharacterCount,
                Math.max(activeQuestDialogueVisibleCharacterCount, (int) activeQuestDialogueTypewriterProgress));
    }

    private void updateTypewriterLoopSfx(float delta) {
        if (typewriterLoopSfx == null) {
            return;
        }
        float safeDelta = Math.max(0f, delta);
        if (shouldPlayTypewriterLoopSfx()) {
            typewriterLoopSfxFadeTimer = 0f;
            float volume = typewriterLoopSfxFullVolume();
            if (typewriterLoopSfxId < 0L) {
                typewriterLoopSfxId = typewriterLoopSfx.loop(volume);
            } else {
                typewriterLoopSfx.setVolume(typewriterLoopSfxId, volume);
            }
            return;
        }
        if (typewriterLoopSfxId < 0L) {
            typewriterLoopSfxFadeTimer = 0f;
            return;
        }
        if (TYPEWRITER_LOOP_SFX_FADE_OUT_SECONDS <= 0f) {
            stopTypewriterLoopSfxImmediately();
            return;
        }
        if (typewriterLoopSfxFadeTimer <= 0f) {
            typewriterLoopSfxFadeTimer = TYPEWRITER_LOOP_SFX_FADE_OUT_SECONDS;
        }
        typewriterLoopSfxFadeTimer = Math.max(0f, typewriterLoopSfxFadeTimer - safeDelta);
        float volume = typewriterLoopSfxFullVolume() * (typewriterLoopSfxFadeTimer / TYPEWRITER_LOOP_SFX_FADE_OUT_SECONDS);
        typewriterLoopSfx.setVolume(typewriterLoopSfxId, volume);
        if (typewriterLoopSfxFadeTimer <= 0f) {
            stopTypewriterLoopSfxImmediately();
        }
    }

    private boolean shouldPlayTypewriterLoopSfx() {
        return shouldTypeCurrentQuestDialogueLine() && !isCurrentQuestDialogueLineFullyVisible();
    }

    private float typewriterLoopSfxFullVolume() {
        GameSettingsService settingsService = settingsService();
        if (settingsService != null) {
            return settingsService.scaleSfxVolume(TYPEWRITER_LOOP_SFX_BASE_VOLUME);
        }
        return TYPEWRITER_LOOP_SFX_BASE_VOLUME;
    }

    private void stopTypewriterLoopSfxImmediately() {
        if (typewriterLoopSfx != null && typewriterLoopSfxId >= 0L) {
            typewriterLoopSfx.stop(typewriterLoopSfxId);
        }
        typewriterLoopSfxId = -1L;
        typewriterLoopSfxFadeTimer = 0f;
    }

    private boolean shouldTypeCurrentQuestDialogueLine() {
        return firstQuestDialogueActive
                && activeQuestDialogueId != QuestDialogueId.NONE
                && firstQuestDialogueLineIndex >= 0
                && firstQuestDialogueLineIndex < activeQuestDialogueLines.length;
    }

    private String currentQuestDialogueLine() {
        return activeQuestDialogueLines.length == 0
                ? ""
                : activeQuestDialogueLines[Math.max(0,
                Math.min(activeQuestDialogueLines.length - 1, firstQuestDialogueLineIndex))];
    }

    private String visibleQuestDialogueLine() {
        String line = currentQuestDialogueLine();
        if (!shouldTypeCurrentQuestDialogueLine()) {
            return line;
        }
        int safeVisibleCharacterCount = Math.max(0, Math.min(activeQuestDialogueVisibleCharacterCount, line.length()));
        return line.substring(0, safeVisibleCharacterCount);
    }

    private boolean isCurrentQuestDialogueLineFullyVisible() {
        return !shouldTypeCurrentQuestDialogueLine()
                || activeQuestDialogueVisibleCharacterCount >= currentQuestDialogueLine().length();
    }

    private void revealCurrentQuestDialogueLineImmediately() {
        String line = currentQuestDialogueLine();
        activeQuestDialogueTypewriterProgress = line.length();
        activeQuestDialogueVisibleCharacterCount = line.length();
    }

    private void resetQuestDialogueTypewriter() {
        activeQuestDialogueTypewriterProgress = 0f;
        activeQuestDialogueVisibleCharacterCount = 0;
        if (!shouldTypeCurrentQuestDialogueLine()) {
            String line = currentQuestDialogueLine();
            activeQuestDialogueVisibleCharacterCount = line.length();
            activeQuestDialogueTypewriterProgress = line.length();
        }
    }

    private void handleCompletedQuestDialogue() {
        QuestDialogueId completedDialogue = activeQuestDialogueId;
        stopActiveDialogueVoiceSfx();
        resetQuestDialogueTypewriter();
        activeQuestDialogueId = QuestDialogueId.NONE;
        activeQuestDialogueSpeakers = new String[0];
        activeQuestDialogueLines = new String[0];

        switch (completedDialogue) {
            case FIRST_QUEST_FATHER -> showQuestItemCard(
                    QuestItemCardId.FIRST_QUEST_HERB_AND_SPICE,
                    "Herb and Spice",
                    "Father asked you to bring this bundle to the bakery.",
                    firstQuestItemTex,
                    0.5f,
                    6f,
                    0f,
                    -20f,
                    0f);
            case SECOND_QUEST_CAFE_OWNER -> showQuestItemCard(
                    QuestItemCardId.SECOND_QUEST_SCORCHED_FRAGMENT,
                    "Scorched Fragment",
                    "The bakery owner found a warm, soot-black fragment and asked you to bring it to the scientist.",
                    secondQuestFragmentTex,
                    1f,
                    3.7f,
                    0f,
                    -10f,
                    0f);
            case SECOND_QUEST_SCIENTIST_INTRO -> {
                secondQuestStage = SECOND_QUEST_RETRIEVE_ARCHIVE_NOTES;
                refreshFirstQuestGuidance();
                showQuestNotification(
                        SECOND_QUEST_NOTIFICATION_TITLE,
                        "Objective Updated: Retrieve the Archive Notes from Research Lab Floor 2.",
                        FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
            }
            case SECOND_QUEST_SCIENTIST_RETURN -> {
                finishSecondQuest();
                startQuestDialogue(
                        QuestDialogueId.THIRD_QUEST_SCIENTIST_SENDOFF,
                        THIRD_QUEST_SCIENTIST_SENDOFF_SPEAKERS,
                        THIRD_QUEST_SCIENTIST_SENDOFF_DIALOGUE_LINES);
            }
            case THIRD_QUEST_SCIENTIST_SENDOFF -> startThirdQuest();
            case THIRD_QUEST_FATHER_RETURN -> {
                thirdQuestStage = THIRD_QUEST_MEET_FATHER_OUTSIDE;
                showQuestNotification(
                        THIRD_QUEST_NOTIFICATION_TITLE,
                        "Objective Updated: Meet Father at the training ground behind the inn.",
                        FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
            }
            case THIRD_QUEST_FATHER_TRAINING_INTRO -> beginThirdQuestMovementTutorial();
            case THIRD_QUEST_FATHER_AFTER_MOVEMENT -> beginThirdQuestAttackTutorial();
            case THIRD_QUEST_FATHER_AFTER_ATTACK -> beginThirdQuestCombatTutorial();
            case THIRD_QUEST_FATHER_PRE_SPAR -> beginThirdQuestFatherSpar();
            case THIRD_QUEST_FATHER_COMPLETE -> {
                finishThirdQuest();
                showQuestItemCard(
                        QuestItemCardId.THIRD_QUEST_DASH_ATTACK_UNLOCKED,
                        "Dash Attack Unlocked",
                        "Press E to lunge forward with an attack.",
                        null,
                        0.9f,
                        1f,
                        0f,
                        52f,
                        0f);
            }
            case FOURTH_QUEST_INTERRUPTION -> startFourthQuest();
            case FOURTH_QUEST_SCIENTIST_REPORT -> {
                fourthQuestStage = FOURTH_QUEST_TALK_TO_CAFE_OWNER;
                showQuestNotification(
                        FOURTH_QUEST_NOTIFICATION_TITLE,
                        "Objective Updated: Investigate the corrupted site outside the bakery.",
                        FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
            }
            case FOURTH_QUEST_CAFE_OWNER -> {
                fourthQuestStage = FOURTH_QUEST_EXAMINE_CAFE_MARK;
                showQuestNotification(
                        FOURTH_QUEST_NOTIFICATION_TITLE,
                        "Objective Updated: Examine the corrupted mark outside the bakery.",
                        FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
            }
            case FOURTH_QUEST_CAFE_MARK -> beginFourthQuestCafeBattle();
            case FOURTH_QUEST_CENTER_MARK -> beginFourthQuestCenterBattle();
            case FOURTH_QUEST_CENTER_AFTER -> {
                fourthQuestStage = FOURTH_QUEST_EXAMINE_EDGE_MARK;
                showQuestNotification(
                        FOURTH_QUEST_NOTIFICATION_TITLE,
                        "Objective Updated: Investigate the final corrupted site near the outer road.",
                        FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
            }
            case FOURTH_QUEST_EDGE_MARK -> beginFourthQuestEdgeBattle();
            case FOURTH_QUEST_EDGE_AFTER -> {
                fourthQuestStage = FOURTH_QUEST_RETURN_TO_SCIENTIST;
                showQuestNotification(
                        FOURTH_QUEST_NOTIFICATION_TITLE,
                        "Objective Updated: Return the samples to the scientist.",
                        FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
            }
            case FOURTH_QUEST_SAMPLE_ANALYSIS -> beginFourthQuestArraySequence();
            case FOURTH_QUEST_ARRAY_SCAN_COMPLETE -> {
                fourthQuestStage = FOURTH_QUEST_GO_TO_BREACH;
                showQuestNotification(
                        FOURTH_QUEST_NOTIFICATION_TITLE,
                        "Objective Updated: Go to the breach site near the outer road.",
                        FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
            }
            case FOURTH_QUEST_BREACH_INTRO -> beginFourthQuestBreachBattle();
            case FOURTH_QUEST_BREACH_AFTER -> {
                fourthQuestStage = FOURTH_QUEST_RETURN_RIFT_CORE;
                showQuestNotification(
                        FOURTH_QUEST_NOTIFICATION_TITLE,
                        "Objective Updated: Bring the Rift Core to the scientist.",
                        FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
            }
            case FOURTH_QUEST_SPATIAL_UNLOCK -> {
                fourthQuestStage = FOURTH_QUEST_EPILOGUE;
                levelPlayerToCap(QUEST_4_LEVEL_CAP);
                showQuestItemCard(
                        QuestItemCardId.FOURTH_QUEST_SPATIAL_DASH_UNLOCKED,
                        "Spatial Dash Unlocked",
                        "Use Spatial Dash to cross gaps, evade danger, and move through unstable space.",
                        null,
                        0.5f,
                        1f,
                        0f,
                        52f,
                        0f);
            }
            case FOURTH_QUEST_EPILOGUE -> {
                finishFourthQuest();
                startQuestDialogue(
                        QuestDialogueId.FIFTH_QUEST_LAB_ALARM,
                        FIFTH_QUEST_LAB_ALARM_SPEAKERS,
                        FIFTH_QUEST_LAB_ALARM_DIALOGUE_LINES);
            }
            case FIFTH_QUEST_LAB_ALARM -> startFifthQuest();
            case FIFTH_QUEST_RUINS_INTRO -> startFifthQuestIntroCameraSequence();
            case FIFTH_QUEST_PORTAL_AFTER -> {
                finishFifthQuest();
                startQuestDialogue(
                        QuestDialogueId.SIXTH_QUEST_PORTAL_BRIEFING,
                        SIXTH_QUEST_PORTAL_BRIEFING_SPEAKERS,
                        SIXTH_QUEST_PORTAL_BRIEFING_DIALOGUE_LINES);
            }
            case SIXTH_QUEST_PORTAL_BRIEFING -> startSixthQuest();
            case SIXTH_QUEST_CASTLE_ARRIVAL -> beginSixthQuestOuterAssault();
            case SIXTH_QUEST_PHASE_1_INTRO -> beginSixthQuestBossPhase1();
            case SIXTH_QUEST_PHASE_1_TRANSITION -> beginSixthQuestFinalPhaseIntro();
            case SIXTH_QUEST_FINAL_PHASE_INTRO -> beginSixthQuestFinalPhaseBattle();
            case SIXTH_QUEST_ENDING -> {
                sixthQuestEndingNpcFadeActive = true;
                sixthQuestEndingNpcFadeTimer = 0f;
                triggerSixthQuestEndingMusicTransition();
                sixthQuestEndingVideoSequenceActive = true;
                sixthQuestEndingVideoTriggered = false;
                sixthQuestEndingVideoSequenceTimer = 0f;
                showQuestItemCard(
                        QuestItemCardId.SIXTH_QUEST_CLEAR,
                        "The Last Ember",
                        "The fire that once took everything from her was finally extinguished.\nFrom grief, she found strength. From ashes, she found a home. And from the last ember, a new dawn was born.",
                        null,
                        0.92f,
                        1f,
                        0f,
                        44f,
                        0f);
            }
            default -> {
            }
        }
    }

    private void handleCompletedQuestItemCard() {
        QuestItemCardId completedItemCard = activeQuestItemCardId;
        activeQuestItemCardId = QuestItemCardId.NONE;
        activeQuestItemTitle = "";
        activeQuestItemDescription = "";
        activeQuestItemTexture = null;
        activeQuestItemTitleScale = 1f;
        activeQuestItemImageScaleMultiplier = 1f;
        activeQuestItemTitleOffsetY = 0f;
        activeQuestItemDescriptionOffsetY = 0f;
        activeQuestItemHintOffsetY = 0f;

        switch (completedItemCard) {
            case FIRST_QUEST_HERB_AND_SPICE -> {
                if (firstQuestStage < FIRST_QUEST_DELIVER_TO_CAFE) {
                    firstQuestStage = FIRST_QUEST_DELIVER_TO_CAFE;
                }
            }
            case SECOND_QUEST_SCORCHED_FRAGMENT -> {
                secondQuestStage = SECOND_QUEST_TAKE_FRAGMENT_TO_SCIENTIST;
                showQuestNotification(
                        SECOND_QUEST_NOTIFICATION_TITLE,
                        "Objective Updated: Bring the Scorched Fragment to the Scientist at the Research Lab.",
                        FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
            }
            case SECOND_QUEST_ARCHIVE_NOTES -> {
                secondQuestStage = SECOND_QUEST_RETURN_TO_SCIENTIST;
                showQuestNotification(
                        SECOND_QUEST_NOTIFICATION_TITLE,
                        "Objective Updated: Return to the Scientist.",
                        FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
            }
            case FOURTH_QUEST_CONDENSED_CORE -> startQuestDialogue(
                    QuestDialogueId.FOURTH_QUEST_BREACH_AFTER,
                    FOURTH_QUEST_BREACH_AFTER_SPEAKERS,
                    FOURTH_QUEST_BREACH_AFTER_DIALOGUE_LINES);
            case THIRD_QUEST_DASH_ATTACK_UNLOCKED -> startQuestDialogue(
                    QuestDialogueId.FOURTH_QUEST_INTERRUPTION,
                    FOURTH_QUEST_INTERRUPTION_SPEAKERS,
                    FOURTH_QUEST_INTERRUPTION_DIALOGUE_LINES);
            case FOURTH_QUEST_SPATIAL_DASH_UNLOCKED -> startQuestDialogue(
                    QuestDialogueId.FOURTH_QUEST_EPILOGUE,
                    FOURTH_QUEST_EPILOGUE_SPEAKERS,
                    FOURTH_QUEST_EPILOGUE_DIALOGUE_LINES);
            case SIXTH_QUEST_CLEAR -> finishSixthQuest();
            default -> {
            }
        }
        refreshFirstQuestGuidance();
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

    private boolean handleMainMenuInput() {
        if (hudRenderer == null || !hudRenderer.isMainMenuVisible()) {
            return false;
        }
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                HudRenderer.PauseMenuAction clickedAction =
                        hudRenderer.handleMainMenuClick(Gdx.input.getX(), Gdx.input.getY());
                if (executePauseMenuAction(clickedAction)) {
                    return true;
                }
            } else {
                hudRenderer.handleMainMenuDrag(Gdx.input.getX(), Gdx.input.getY());
            }
        } else {
            hudRenderer.endMainMenuDrag();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            hudRenderer.endMainMenuDrag();
            if (hudRenderer.isMainMenuConfirmationVisible()) {
                hudRenderer.cancelMainMenuConfirmation();
            } else {
                hudRenderer.closeMainMenu();
            }
            return false;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            hudRenderer.selectPreviousMainMenuItem();
            return false;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            hudRenderer.selectNextMainMenuItem();
            return false;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            hudRenderer.adjustSelectedMainMenuSetting(-1);
            return false;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            hudRenderer.adjustSelectedMainMenuSetting(1);
            return false;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            hudRenderer.selectPreviousMainMenuTab();
            return false;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            hudRenderer.selectNextMainMenuTab();
            return false;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            hudRenderer.selectNextMainMenuTab();
            return false;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            return executePauseMenuAction(hudRenderer.activateSelectedMainMenuItem());
        }
        return false;
    }

    private boolean executePauseMenuAction(HudRenderer.PauseMenuAction action) {
        if (action == null || action == HudRenderer.PauseMenuAction.NONE) {
            return false;
        }
        switch (action) {
            case RESUME -> {
                if (hudRenderer != null) {
                    hudRenderer.closeMainMenu();
                }
                return false;
            }
            case MAIN_MENU -> {
                writeAutosave();
                if (hudRenderer != null) {
                    hudRenderer.closeMainMenu();
                }
                if (game != null) {
                    game.showMainMenu();
                    return true;
                }
                return false;
            }
            case RELOAD_AUTOSAVE -> {
                if (SaveGameService.load(activeSaveSlotId) == null) {
                    if (hudRenderer != null) {
                        hudRenderer.showNotification("Save", "No save is available to reload.", 2.4f);
                    }
                    return false;
                }
                if (hudRenderer != null) {
                    hudRenderer.closeMainMenu();
                }
                if (game != null) {
                    game.loadGame(activeSaveSlotId);
                    return true;
                }
                return false;
            }
            case EXIT_GAME -> {
                Gdx.app.exit();
                return true;
            }
            default -> {
                return false;
            }
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
        if (handleFirstQuestOverlayInput()) {
            player.moving = false;
            centerCameraOnPlayer();
            return;
        }
        if (isSixthQuestPortalActivationRunning()) {
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
        if ((Gdx.input.isKeyJustPressed(Input.Keys.M) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))
                && hudRenderer != null) {
            hudRenderer.openMainMenu();
            player.moving = false;
            centerCameraOnPlayer();
            return;
        }
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)
                && hudRenderer != null
                && hudRenderer.handleQuestPanelClick(Gdx.input.getX(), Gdx.input.getY())) {
            player.moving = false;
            centerCameraOnPlayer();
            return;
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
        boolean combatEnabled = isPlayerCombatEnabledOnCurrentMap();
        boolean dashAttackUnlocked = isDashAttackUnlocked();
        boolean spatialDashUnlocked = isSpatialDashUnlocked();
        boolean dashAttackPressed = combatEnabled && interactPressed && dashAttackUnlocked;

        if (combatEnabled && interactPressed && !dashAttackUnlocked) {
            if (hudRenderer != null) {
                hudRenderer.showNotification(
                        "Technique Locked",
                        "E Dash Attack unlocks at Level " + DASH_ATTACK_UNLOCK_LEVEL + ".",
                        TECHNIQUE_LOCKED_NOTIFICATION_SECONDS);
            }
            centerCameraOnPlayer();
            return;
        }

        if (dashAttackPressed
                && !player.attacking
                && !player.dashing
                && player.attackCooldownTimer <= 0f
                && player.dashCooldownTimer <= 0f
                && dashAttackAbilityCooldownTimer <= 0f) {
            player.dashAttackFollowupQueued = false;
            if (dashAttackAnim != null) {
                activeAttackAnim = dashAttackAnim;
                player.attacking = true;
                player.attackTimer = 0f;
                player.attackCooldownTimer = ATTACK_COOLDOWN;
                player.attackComboStep = 0;
                if (attackSpecialSfx != null) {
                    playSfx(attackSpecialSfx);
                }
                startDashAttack(dashInputX, dashInputY);
            }
        }

        if (isDashAttackAnimationActive()) {
            player.moving = false;
            centerCameraOnPlayer();
            return;
        }

        if (combatEnabled && rightPressed && !spatialDashUnlocked) {
            if (hudRenderer != null) {
                hudRenderer.showNotification(
                        "Technique Locked",
                        "Spatial Dash unlocks at Level " + SPATIAL_DASH_UNLOCK_LEVEL + ".",
                        TECHNIQUE_LOCKED_NOTIFICATION_SECONDS);
            }
            centerCameraOnPlayer();
            return;
        }

        if (combatEnabled
                && rightPressed
                && !player.attacking
                && !player.dashing
                && player.attackCooldownTimer <= 0f
                && player.dashCooldownTimer <= 0f
                && spatialDashAbilityCooldownTimer <= 0f) {
            activeAttackAnim = spetialDashAnim;
            if (activeAttackAnim != null) {
                player.attacking = true;
                player.attackTimer = 0f;
                player.attackCooldownTimer = ATTACK_COOLDOWN;
                player.attackComboStep = 0;
                if (attackSpecialSfx != null) {
                    playSfx(attackSpecialSfx);
                }
                startDashAttack(dashInputX, dashInputY);
            }
        }

        if (combatEnabled && leftPressed && !player.attacking && player.attackCooldownTimer <= 0f) {
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
                    playSfx(attackSound);
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

        float step = currentPlayerMoveSpeed() * delta;
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

    private float currentPlayerMoveSpeed() {
        float moveSpeed = PLAYER_SPEED;
        if (PlayerLevelStats.clampLevel(player.playerLevel) >= PlayerLevelStats.MAX_LEVEL) {
            moveSpeed *= MAX_LEVEL_MOVE_SPEED_MULTIPLIER;
        }
        return moveSpeed;
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
        String command = tokens[0].toLowerCase(Locale.ROOT);
        if ("/health".equals(command)) {
            infiniteHealthEnabled = false;
            if (hudRenderer != null) {
                hudRenderer.showNotification("System", "Health is back to normal.", 2.4f);
            }
            return;
        }
        if ("/inf".equals(command) && tokens.length >= 2 && "health".equalsIgnoreCase(tokens[1])) {
            infiniteHealthEnabled = true;
            if (hudRenderer != null) {
                hudRenderer.showNotification("System", "Infinite health enabled.", 2.4f);
            }
            return;
        }

        if (tokens.length < 2) {
            return;
        }

        if ("/to".equals(command)) {
            String destination = tokens[1].toLowerCase(Locale.ROOT);
            if (destination.startsWith("quest") && jumpToQuestDebugStart(destination)) {
                return;
            }
            if ("castle".equals(destination)) {
                teleportToMap(MAP_ID_DEMON_CASTLE, "Spawn");
            } else if ("village".equals(destination)) {
                teleportToMap(MAP_ID_VILLAGE, null);
            } else if ("boss".equals(destination)) {
                sixthQuestStage = SIXTH_QUEST_FINAL_PHASE_INTRO;
                sixthQuestEncounterSpawned = false;
                sixthQuestBossCalloutTimer = 0f;
                sixthQuestBossCalloutIndex = 0;
                sixthQuestLowHealthExchangePlayed = false;
                bossArenaFinalIntroPhase = BossArenaFinalIntroPhase.NONE;
                bossArenaFinalIntroTimer = 0f;
                bossArenaFinalIntroCameraTarget.setZero();
                teleportToMap(MAP_ID_BOSS_ARENA_PHASE_2, BOSS_ARENA_PHASE_2_PLAYER_SPAWN_LAYER_NAME);
            }
            return;
        }

        if ("/spawn".equals(command) && "bat".equalsIgnoreCase(tokens[1])) {
            spawnBatInFrontOfPlayer();
        }
    }

    private boolean jumpToQuestDebugStart(String destination) {
        if (destination == null || destination.isBlank()) {
            return false;
        }
        switch (destination) {
            case "quest1" -> {
                prepareQuestDebugState();
                firstQuestStage = FIRST_QUEST_TALK_TO_FATHER;
                secondQuestStage = SECOND_QUEST_DISABLED;
                thirdQuestStage = THIRD_QUEST_DISABLED;
                fourthQuestStage = FOURTH_QUEST_DISABLED;
                fifthQuestStage = FIFTH_QUEST_DISABLED;
                sixthQuestStage = SIXTH_QUEST_DISABLED;
                progressionLevelCap = clampProgressionLevelCap(1);
                scientistProgressionUnlocked = false;
                thirdQuestAvailable = false;
                gameCompleted = false;
                restorePlayerForQuestDebug(1);
                teleportToMap(MAP_ID_INN_ROOM, "FIRST-SPAWN");
                notifyQuestDebugJump("Quest 1");
                refreshFirstQuestGuidance();
                return true;
            }
            case "quest2" -> {
                prepareQuestDebugState();
                firstQuestStage = FIRST_QUEST_COMPLETE;
                secondQuestStage = SECOND_QUEST_TALK_TO_CAFE_OWNER;
                thirdQuestStage = THIRD_QUEST_DISABLED;
                fourthQuestStage = FOURTH_QUEST_DISABLED;
                fifthQuestStage = FIFTH_QUEST_DISABLED;
                sixthQuestStage = SIXTH_QUEST_DISABLED;
                progressionLevelCap = clampProgressionLevelCap(1);
                scientistProgressionUnlocked = false;
                thirdQuestAvailable = false;
                gameCompleted = false;
                restorePlayerForQuestDebug(1);
                teleportToMap(MAP_ID_VILLAGE, FIRST_QUEST_CAFE_ENTRANCE_LAYER_NAME);
                notifyQuestDebugJump("Quest 2");
                refreshFirstQuestGuidance();
                return true;
            }
            case "quest3" -> {
                prepareQuestDebugState();
                firstQuestStage = FIRST_QUEST_COMPLETE;
                secondQuestStage = SECOND_QUEST_COMPLETE;
                thirdQuestStage = THIRD_QUEST_RETURN_TO_FATHER;
                fourthQuestStage = FOURTH_QUEST_DISABLED;
                fifthQuestStage = FIFTH_QUEST_DISABLED;
                sixthQuestStage = SIXTH_QUEST_DISABLED;
                progressionLevelCap = clampProgressionLevelCap(QUEST_2_LEVEL_CAP);
                scientistProgressionUnlocked = true;
                thirdQuestAvailable = true;
                gameCompleted = false;
                restorePlayerForQuestDebug(QUEST_2_LEVEL_CAP);
                teleportToMap(MAP_ID_RESEARCH_LAB_FLOOR_1, null);
                notifyQuestDebugJump("Quest 3");
                refreshFirstQuestGuidance();
                return true;
            }
            case "quest4" -> {
                prepareQuestDebugState();
                firstQuestStage = FIRST_QUEST_COMPLETE;
                secondQuestStage = SECOND_QUEST_COMPLETE;
                thirdQuestStage = THIRD_QUEST_COMPLETE;
                fourthQuestStage = FOURTH_QUEST_REPORT_TO_SCIENTIST;
                fifthQuestStage = FIFTH_QUEST_DISABLED;
                sixthQuestStage = SIXTH_QUEST_DISABLED;
                progressionLevelCap = clampProgressionLevelCap(QUEST_3_LEVEL_CAP);
                scientistProgressionUnlocked = true;
                thirdQuestAvailable = true;
                gameCompleted = false;
                restorePlayerForQuestDebug(QUEST_3_LEVEL_CAP);
                teleportToMap(MAP_ID_VILLAGE, THIRD_QUEST_TRAINING_AREA_LAYER_NAME);
                notifyQuestDebugJump("Quest 4");
                refreshFirstQuestGuidance();
                return true;
            }
            case "quest4.0" -> {
                prepareQuestDebugState();
                firstQuestStage = FIRST_QUEST_COMPLETE;
                secondQuestStage = SECOND_QUEST_COMPLETE;
                thirdQuestStage = THIRD_QUEST_COMPLETE;
                fourthQuestStage = FOURTH_QUEST_COMPLETE;
                fifthQuestStage = FIFTH_QUEST_DISABLED;
                sixthQuestStage = SIXTH_QUEST_DISABLED;
                progressionLevelCap = clampProgressionLevelCap(QUEST_4_LEVEL_CAP);
                scientistProgressionUnlocked = true;
                thirdQuestAvailable = true;
                gameCompleted = false;
                restorePlayerForQuestDebug(QUEST_4_LEVEL_CAP);
                teleportToMap(MAP_ID_RESEARCH_LAB_FLOOR_1, null);
                movePlayerBesideScientistForLabDialogue();
                startQuestDialogue(
                        QuestDialogueId.FIFTH_QUEST_LAB_ALARM,
                        FIFTH_QUEST_LAB_ALARM_SPEAKERS,
                        FIFTH_QUEST_LAB_ALARM_DIALOGUE_LINES);
                notifyQuestDebugJump("Quest 4.0");
                refreshFirstQuestGuidance();
                return true;
            }
            case "quest4.0.1" -> {
                prepareQuestDebugState();
                firstQuestStage = FIRST_QUEST_COMPLETE;
                secondQuestStage = SECOND_QUEST_COMPLETE;
                thirdQuestStage = THIRD_QUEST_COMPLETE;
                fourthQuestStage = FOURTH_QUEST_COMPLETE;
                fifthQuestStage = FIFTH_QUEST_ANCHOR_3_DESTROY;
                sixthQuestStage = SIXTH_QUEST_DISABLED;
                progressionLevelCap = clampProgressionLevelCap(QUEST_4_LEVEL_CAP);
                scientistProgressionUnlocked = true;
                thirdQuestAvailable = true;
                gameCompleted = false;
                fifthQuestRuinsIntroTriggered = true;
                fifthQuestIntroCameraPhase = FifthQuestIntroCameraPhase.NONE;
                restorePlayerForQuestDebug(QUEST_4_LEVEL_CAP);
                teleportToMap(MAP_ID_VILLAGE, VILLAGE_PORTAL_ACTUAL_LAYER_NAME);
                positionPlayerAtFifthQuestThirdAnchor();
                centerCameraOnPlayer();
                notifyQuestDebugJump("Quest 4.0.1");
                refreshFirstQuestGuidance();
                return true;
            }
            case "quest4.1" -> {
                prepareQuestDebugState();
                firstQuestStage = FIRST_QUEST_COMPLETE;
                secondQuestStage = SECOND_QUEST_COMPLETE;
                thirdQuestStage = THIRD_QUEST_COMPLETE;
                fourthQuestStage = FOURTH_QUEST_COMPLETE;
                fifthQuestStage = FIFTH_QUEST_COMPLETE;
                sixthQuestStage = SIXTH_QUEST_FINAL_PHASE_INTRO;
                progressionLevelCap = clampProgressionLevelCap(QUEST_4_LEVEL_CAP);
                scientistProgressionUnlocked = true;
                thirdQuestAvailable = true;
                gameCompleted = false;
                restorePlayerForQuestDebug(QUEST_4_LEVEL_CAP);
                teleportToMap(MAP_ID_BOSS_ARENA_PHASE_2, BOSS_ARENA_PHASE_2_PLAYER_SPAWN_LAYER_NAME);
                notifyQuestDebugJump("Quest 4.1");
                refreshFirstQuestGuidance();
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private void prepareQuestDebugState() {
        stopActiveDialogueVoiceSfx();
        firstQuestDialogueActive = false;
        firstQuestItemCardVisible = false;
        firstQuestDialogueLineIndex = 0;
        activeQuestDialogueId = QuestDialogueId.NONE;
        activeQuestDialogueSpeakers = new String[0];
        activeQuestDialogueLines = new String[0];
        activeQuestItemCardId = QuestItemCardId.NONE;
        activeQuestItemTitle = "";
        activeQuestItemDescription = "";
        activeQuestItemTexture = null;
        activeQuestItemTitleScale = 1f;
        activeQuestItemImageScaleMultiplier = 1f;
        activeQuestItemTitleOffsetY = 0f;
        activeQuestItemDescriptionOffsetY = 0f;
        activeQuestItemHintOffsetY = 0f;
        resetQuestDialogueTypewriter();
        questGuidanceSuppressTimer = 0f;
        resetPlayerAbilityCooldowns();
        clearFifthQuestDeathChoice();
        fifthQuestFirstEncounterIntroPlayed = false;
        stopFifthQuestFirstEncounterIntro();
        fifthQuestZombieSfxCooldownTimer = 0f;
        fifthQuestPortalDialogueNpcFadeTimer = 0f;
        fifthQuestPortalDialogueNpcVisibleLastFrame = false;
        sixthQuestEncounterSpawned = false;
        sixthQuestBossCalloutTimer = 0f;
        sixthQuestBossCalloutIndex = 0;
        sixthQuestLowHealthExchangePlayed = false;
        bossArenaFinalIntroPhase = BossArenaFinalIntroPhase.NONE;
        bossArenaFinalIntroTimer = 0f;
        bossArenaFinalIntroCameraTarget.setZero();
        bossArenaHudRevealTimer = 0f;
        clearSixthQuestPortalActivation();
        clearSpawnedBat();
        clearSpawnedGolem1();
        clearSpawnedBossArenaPhase2Demons();
        clearSpawnedFifthQuestDemons();
    }

    private void restorePlayerForQuestDebug(int level) {
        int safeLevel = PlayerLevelStats.clampLevel(level);
        applyPlayerLevelStats(
                safeLevel,
                PlayerLevelStats.maxHealthForLevel(safeLevel),
                PlayerLevelStats.maxStaminaForLevel(safeLevel));
        clearPlayerHazardHitReaction();
        resetPlayerDeathState();
    }

    private void notifyQuestDebugJump(String label) {
        if (hudRenderer != null && label != null && !label.isBlank()) {
            hudRenderer.showNotification("System", "Jumped to " + label + ".", 2.4f);
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
        bossArenaPhase2Explosions.clear();
    }

    private void clearSpawnedFifthQuestDemons() {
        fifthQuestDemons.clear();
        stopFifthQuestFirstEncounterIntro();
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
        for (int i = 0; i < bossArenaPhase2SkillStartFrameTextures.size; i++) {
            Texture frameTexture = bossArenaPhase2SkillStartFrameTextures.get(i);
            if (frameTexture != null) {
                frameTexture.dispose();
            }
        }
        bossArenaPhase2SkillStartFrameTextures.clear();
        for (int i = 0; i < bossArenaPhase2SkillLoopFrameTextures.size; i++) {
            Texture frameTexture = bossArenaPhase2SkillLoopFrameTextures.get(i);
            if (frameTexture != null) {
                frameTexture.dispose();
            }
        }
        bossArenaPhase2SkillLoopFrameTextures.clear();
        for (int i = 0; i < bossArenaPhase2SkillEndFrameTextures.size; i++) {
            Texture frameTexture = bossArenaPhase2SkillEndFrameTextures.get(i);
            if (frameTexture != null) {
                frameTexture.dispose();
            }
        }
        bossArenaPhase2SkillEndFrameTextures.clear();
        for (int i = 0; i < bossArenaPhase2ExplosionFrameTextures.size; i++) {
            Texture frameTexture = bossArenaPhase2ExplosionFrameTextures.get(i);
            if (frameTexture != null) {
                frameTexture.dispose();
            }
        }
        bossArenaPhase2ExplosionFrameTextures.clear();
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

    private boolean tryHandleFirstQuestInteraction() {
        if (isFirstQuestOverlayActive() || world.activeMapId == null) {
            return false;
        }
        if (firstQuestStage == FIRST_QUEST_TALK_TO_FATHER
                && MAP_ID_INN_ROOM.equals(world.activeMapId)
                && isPlayerNearFirstQuestFather()) {
            startQuestDialogue(
                    QuestDialogueId.FIRST_QUEST_FATHER,
                    repeatSpeaker("Father", FIRST_QUEST_FATHER_DIALOGUE_LINES.length),
                    FIRST_QUEST_FATHER_DIALOGUE_LINES);
            return true;
        }
        if (firstQuestStage >= FIRST_QUEST_DELIVER_TO_CAFE
                && firstQuestStage < FIRST_QUEST_COMPLETE
                && MAP_ID_VILLAGE.equals(world.activeMapId)
                && isPlayerNearFirstQuestCafeNpc()) {
            firstQuestStage = FIRST_QUEST_COMPLETE;
            if (secondQuestStage < SECOND_QUEST_TALK_TO_CAFE_OWNER) {
                secondQuestStage = SECOND_QUEST_TALK_TO_CAFE_OWNER;
            }
            showQuestNotification(
                    "Quest Complete",
                    "You delivered the Herb-and-Spice to the bakery owner.",
                    FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
            refreshFirstQuestGuidance();
            startQuestDialogue(
                    QuestDialogueId.SECOND_QUEST_CAFE_OWNER,
                    SECOND_QUEST_CAFE_OWNER_SPEAKERS,
                    SECOND_QUEST_CAFE_OWNER_DIALOGUE_LINES);
            return true;
        }
        if (secondQuestStage == SECOND_QUEST_TALK_TO_CAFE_OWNER
                && MAP_ID_VILLAGE.equals(world.activeMapId)
                && isPlayerNearFirstQuestCafeNpc()) {
            startQuestDialogue(
                    QuestDialogueId.SECOND_QUEST_CAFE_OWNER,
                    SECOND_QUEST_CAFE_OWNER_SPEAKERS,
                    SECOND_QUEST_CAFE_OWNER_DIALOGUE_LINES);
            return true;
        }
        if (secondQuestStage == SECOND_QUEST_TAKE_FRAGMENT_TO_SCIENTIST
                && MAP_ID_RESEARCH_LAB_FLOOR_1.equals(world.activeMapId)
                && isPlayerNearScientistNpc()) {
            startQuestDialogue(
                    QuestDialogueId.SECOND_QUEST_SCIENTIST_INTRO,
                    SECOND_QUEST_SCIENTIST_INTRO_SPEAKERS,
                    SECOND_QUEST_SCIENTIST_INTRO_DIALOGUE_LINES);
            return true;
        }
        if (secondQuestStage == SECOND_QUEST_RETRIEVE_ARCHIVE_NOTES
                && MAP_ID_RESEARCH_LAB_FLOOR_2.equals(world.activeMapId)
                && isPlayerNearSecondQuestArchive()) {
            showQuestItemCard(
                    QuestItemCardId.SECOND_QUEST_ARCHIVE_NOTES,
                    "Archive Notes",
                    "You found the Archive Notes the scientist asked for.",
                    secondQuestArchiveNotesTex,
                    0.75f,
                    1.35f,
                    0f,
                    -8f,
                    0f);
            return true;
        }
        if (secondQuestStage == SECOND_QUEST_RETURN_TO_SCIENTIST
                && MAP_ID_RESEARCH_LAB_FLOOR_1.equals(world.activeMapId)
                && isPlayerNearScientistNpc()) {
            startQuestDialogue(
                    QuestDialogueId.SECOND_QUEST_SCIENTIST_RETURN,
                    SECOND_QUEST_SCIENTIST_RETURN_SPEAKERS,
                    SECOND_QUEST_SCIENTIST_RETURN_DIALOGUE_LINES);
            return true;
        }
        if (thirdQuestStage == THIRD_QUEST_RETURN_TO_FATHER
                && MAP_ID_INN_ROOM.equals(world.activeMapId)
                && isPlayerNearFirstQuestFather()) {
            startQuestDialogue(
                    QuestDialogueId.THIRD_QUEST_FATHER_RETURN,
                    THIRD_QUEST_FATHER_RETURN_SPEAKERS,
                    THIRD_QUEST_FATHER_RETURN_DIALOGUE_LINES);
            return true;
        }
        if (thirdQuestStage == THIRD_QUEST_MEET_FATHER_OUTSIDE
                && MAP_ID_VILLAGE.equals(world.activeMapId)
                && isPlayerNearThirdQuestVillageFather()) {
            startQuestDialogue(
                    QuestDialogueId.THIRD_QUEST_FATHER_TRAINING_INTRO,
                    THIRD_QUEST_FATHER_TRAINING_INTRO_SPEAKERS,
                    THIRD_QUEST_FATHER_TRAINING_INTRO_DIALOGUE_LINES);
            return true;
        }
        if (fourthQuestStage == FOURTH_QUEST_REPORT_TO_SCIENTIST
                && MAP_ID_RESEARCH_LAB_FLOOR_1.equals(world.activeMapId)
                && isPlayerNearScientistNpc()) {
            startQuestDialogue(
                    QuestDialogueId.FOURTH_QUEST_SCIENTIST_REPORT,
                    FOURTH_QUEST_SCIENTIST_REPORT_SPEAKERS,
                    FOURTH_QUEST_SCIENTIST_REPORT_DIALOGUE_LINES);
            return true;
        }
        if (fourthQuestStage == FOURTH_QUEST_TALK_TO_CAFE_OWNER
                && MAP_ID_VILLAGE.equals(world.activeMapId)
                && isPlayerNearFirstQuestCafeNpc()) {
            startQuestDialogue(
                    QuestDialogueId.FOURTH_QUEST_CAFE_OWNER,
                    FOURTH_QUEST_CAFE_OWNER_SPEAKERS,
                    FOURTH_QUEST_CAFE_OWNER_DIALOGUE_LINES);
            return true;
        }
        if (fourthQuestStage == FOURTH_QUEST_EXAMINE_CAFE_MARK
                && MAP_ID_VILLAGE.equals(world.activeMapId)
                && isPlayerNearFourthQuestCafeMark()) {
            startQuestDialogue(
                    QuestDialogueId.FOURTH_QUEST_CAFE_MARK,
                    FOURTH_QUEST_CAFE_MARK_SPEAKERS,
                    FOURTH_QUEST_CAFE_MARK_DIALOGUE_LINES);
            return true;
        }
        if (fourthQuestStage == FOURTH_QUEST_EXAMINE_CENTER_MARK
                && MAP_ID_VILLAGE.equals(world.activeMapId)
                && isPlayerNearFourthQuestCenterMark()) {
            startQuestDialogue(
                    QuestDialogueId.FOURTH_QUEST_CENTER_MARK,
                    FOURTH_QUEST_CENTER_MARK_SPEAKERS,
                    FOURTH_QUEST_CENTER_MARK_DIALOGUE_LINES);
            return true;
        }
        if (fourthQuestStage == FOURTH_QUEST_EXAMINE_EDGE_MARK
                && MAP_ID_VILLAGE.equals(world.activeMapId)
                && isPlayerNearFourthQuestEdgeMark()) {
            startQuestDialogue(
                    QuestDialogueId.FOURTH_QUEST_EDGE_MARK,
                    FOURTH_QUEST_EDGE_MARK_SPEAKERS,
                    FOURTH_QUEST_EDGE_MARK_DIALOGUE_LINES);
            return true;
        }
        if (fourthQuestStage == FOURTH_QUEST_RETURN_TO_SCIENTIST
                && MAP_ID_RESEARCH_LAB_FLOOR_1.equals(world.activeMapId)
                && isPlayerNearScientistNpc()) {
            startQuestDialogue(
                    QuestDialogueId.FOURTH_QUEST_SAMPLE_ANALYSIS,
                    FOURTH_QUEST_SAMPLE_ANALYSIS_SPEAKERS,
                    FOURTH_QUEST_SAMPLE_ANALYSIS_DIALOGUE_LINES);
            return true;
        }
        if ((fourthQuestStage == FOURTH_QUEST_GO_TO_ARRAY || fourthQuestStage == FOURTH_QUEST_ACTIVATE_ARRAY_NODE_1)
                && MAP_ID_RESEARCH_LAB_FLOOR_2.equals(world.activeMapId)
                && isPlayerNearFourthQuestArrayNode(0)) {
            fourthQuestStage = FOURTH_QUEST_ACTIVATE_ARRAY_NODE_2;
            showQuestNotification(
                    FOURTH_QUEST_NOTIFICATION_TITLE,
                    "First array node aligned. Activate the second node.",
                    FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
            return true;
        }
        if (fourthQuestStage == FOURTH_QUEST_ACTIVATE_ARRAY_NODE_2
                && MAP_ID_RESEARCH_LAB_FLOOR_2.equals(world.activeMapId)
                && isPlayerNearFourthQuestArrayNode(1)) {
            fourthQuestStage = FOURTH_QUEST_ACTIVATE_ARRAY_NODE_3;
            showQuestNotification(
                    FOURTH_QUEST_NOTIFICATION_TITLE,
                    "Second array node aligned. Activate the third node.",
                    FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
            return true;
        }
        if (fourthQuestStage == FOURTH_QUEST_ACTIVATE_ARRAY_NODE_3
                && MAP_ID_RESEARCH_LAB_FLOOR_2.equals(world.activeMapId)
                && isPlayerNearFourthQuestArrayNode(2)) {
            fourthQuestStage = FOURTH_QUEST_ACTIVATE_ARRAY_CONSOLE;
            showQuestNotification(
                    FOURTH_QUEST_NOTIFICATION_TITLE,
                    "All outer nodes are active. Return to the main console.",
                    FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
            return true;
        }
        if (fourthQuestStage == FOURTH_QUEST_ACTIVATE_ARRAY_CONSOLE
                && MAP_ID_RESEARCH_LAB_FLOOR_2.equals(world.activeMapId)
                && isPlayerNearFourthQuestArrayConsole()) {
            beginFourthQuestArrayDefense();
            return true;
        }
        if (fourthQuestStage == FOURTH_QUEST_GO_TO_BREACH
                && MAP_ID_VILLAGE.equals(world.activeMapId)
                && isPlayerNearFourthQuestBreachSite()) {
            startQuestDialogue(
                    QuestDialogueId.FOURTH_QUEST_BREACH_INTRO,
                    FOURTH_QUEST_BREACH_INTRO_SPEAKERS,
                    FOURTH_QUEST_BREACH_INTRO_DIALOGUE_LINES);
            return true;
        }
        if (fourthQuestStage == FOURTH_QUEST_RETURN_RIFT_CORE
                && MAP_ID_RESEARCH_LAB_FLOOR_1.equals(world.activeMapId)
                && isPlayerNearScientistNpc()) {
            startQuestDialogue(
                    QuestDialogueId.FOURTH_QUEST_SPATIAL_UNLOCK,
                    FOURTH_QUEST_SPATIAL_UNLOCK_SPEAKERS,
                    FOURTH_QUEST_SPATIAL_UNLOCK_DIALOGUE_LINES);
            return true;
        }
        if (fifthQuestStage == FIFTH_QUEST_ANCHOR_1_DESTROY
                && MAP_ID_VILLAGE.equals(world.activeMapId)
                && isPlayerNearFifthQuestAnchor(0)) {
            if (hudRenderer != null) {
                hudRenderer.showNotification(characterName, "One down.", FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
            }
            beginFifthQuestAnchorBattle(1);
            return true;
        }
        if (fifthQuestStage == FIFTH_QUEST_ANCHOR_2_DESTROY
                && MAP_ID_VILLAGE.equals(world.activeMapId)
                && isPlayerNearFifthQuestAnchor(1)) {
            if (hudRenderer != null) {
                hudRenderer.showNotification(characterName, "The portal is weakening.", FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
            }
            beginFifthQuestAnchorBattle(2);
            return true;
        }
        if (fifthQuestStage == FIFTH_QUEST_ANCHOR_3_DESTROY
                && MAP_ID_VILLAGE.equals(world.activeMapId)
                && isPlayerNearFifthQuestAnchor(2)) {
            if (hudRenderer != null) {
                hudRenderer.showNotification(characterName, "The anchors are down.", FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
            }
            startQuestDialogue(
                    QuestDialogueId.FIFTH_QUEST_PORTAL_AFTER,
                    FIFTH_QUEST_PORTAL_AFTER_SPEAKERS,
                    FIFTH_QUEST_PORTAL_AFTER_DIALOGUE_LINES);
            return true;
        }
        if (sixthQuestStage >= SIXTH_QUEST_ENTER_PORTAL
                && sixthQuestStage < SIXTH_QUEST_ENDING
                && MAP_ID_VILLAGE.equals(world.activeMapId)
                && isVillagePortalActive()
                && isPlayerWithinObjectLayer(FIFTH_QUEST_PORTAL_LAYER_NAME)) {
            if (sixthQuestStage >= SIXTH_QUEST_FINAL_PHASE_INTRO) {
                startMapTransition(MAP_ID_BOSS_ARENA_PHASE_2, BOSS_ARENA_PHASE_2_PLAYER_SPAWN_LAYER_NAME);
            } else {
                startMapTransition(MAP_ID_DEMON_CASTLE, "Spawn");
            }
            return true;
        }
        if (sixthQuestStage == SIXTH_QUEST_SEAL_A_DESTROY
                && MAP_ID_DEMON_CASTLE.equals(world.activeMapId)
                && isPlayerNearSixthQuestSeal(true)) {
            if (hudRenderer != null) {
                hudRenderer.showNotification(characterName, "One seal broken.", FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
            }
            beginSixthQuestSealBBattle();
            return true;
        }
        if (sixthQuestStage == SIXTH_QUEST_SEAL_B_DESTROY
                && MAP_ID_DEMON_CASTLE.equals(world.activeMapId)
                && isPlayerNearSixthQuestSeal(false)) {
            if (hudRenderer != null) {
                hudRenderer.showNotification(characterName, "The path is opening.", FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
            }
            beginSixthQuestThroneGuards();
            return true;
        }
        if (secondQuestStage == SECOND_QUEST_COMPLETE
                && scientistProgressionUnlocked
                && (fourthQuestStage <= FOURTH_QUEST_DISABLED || fourthQuestStage >= FOURTH_QUEST_COMPLETE)
                && (fifthQuestStage <= FIFTH_QUEST_DISABLED || fifthQuestStage >= FIFTH_QUEST_COMPLETE)
                && MAP_ID_RESEARCH_LAB_FLOOR_1.equals(world.activeMapId)
                && isPlayerNearScientistNpc()) {
            if (player.playerLevel < progressionLevelCap) {
                levelPlayerToCap(progressionLevelCap);
                showQuestNotification(
                        "Progression",
                        "The scientist helps you reach Level " + player.playerLevel + ".",
                        FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
            } else if (hudRenderer != null) {
                hudRenderer.showNotification(
                        "Scientist",
                        "Return when your training deepens. Level 3 unlocks E Dash Attack. Level 4 unlocks Spatial Dash.",
                        FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
            }
            return true;
        }
        return false;
    }

    private boolean isPlayerCombatEnabledOnCurrentMap() {
        if (world.activeMapId == null) {
            return false;
        }
        return MAP_ID_DEMON_CASTLE.equals(world.activeMapId)
                || MAP_ID_BOSS_ARENA_PHASE_2.equals(world.activeMapId)
                || (MAP_ID_VILLAGE.equals(world.activeMapId)
                && (thirdQuestStage == THIRD_QUEST_ATTACK_TUTORIAL
                || thirdQuestStage == THIRD_QUEST_COMBAT_TUTORIAL
                || thirdQuestStage == THIRD_QUEST_SPAR_WITH_FATHER
                || fourthQuestStage == FOURTH_QUEST_CAFE_BATTLE
                || fourthQuestStage == FOURTH_QUEST_CENTER_BATTLE
                || fourthQuestStage == FOURTH_QUEST_EDGE_BATTLE
                || fourthQuestStage == FOURTH_QUEST_BREACH_BATTLE
                || fifthQuestStage == FIFTH_QUEST_OUTER_RUINS_BATTLE
                || fifthQuestStage == FIFTH_QUEST_MID_RUINS_BATTLE
                || fifthQuestStage == FIFTH_QUEST_ANCHOR_1_BATTLE
                || fifthQuestStage == FIFTH_QUEST_ANCHOR_2_BATTLE
                || fifthQuestStage == FIFTH_QUEST_ANCHOR_3_BATTLE
                || fifthQuestStage == FIFTH_QUEST_PORTAL_BOSS))
                || (MAP_ID_RESEARCH_LAB_FLOOR_2.equals(world.activeMapId)
                && fourthQuestStage == FOURTH_QUEST_ARRAY_DEFENSE);
    }

    private boolean isDashAttackUnlocked() {
        return player.playerLevel >= DASH_ATTACK_UNLOCK_LEVEL;
    }

    private boolean isSpatialDashUnlocked() {
        return player.playerLevel >= SPATIAL_DASH_UNLOCK_LEVEL;
    }

    private boolean isPlayerNearFirstQuestFather() {
        Rectangle fatherBounds = firstQuestFatherBounds();
        if (fatherBounds == null) {
            return false;
        }
        Rectangle expandedBounds = new Rectangle(
                fatherBounds.x - FIRST_QUEST_INTERACT_RANGE * 0.5f,
                fatherBounds.y - FIRST_QUEST_INTERACT_RANGE * 0.25f,
                fatherBounds.width + FIRST_QUEST_INTERACT_RANGE,
                fatherBounds.height + FIRST_QUEST_INTERACT_RANGE * 0.75f);
        return expandedBounds.overlaps(player.bounds);
    }

    private Rectangle firstQuestFatherBounds() {
        MapLayer fatherLayer = findLayerByName(FIRST_QUEST_FATHER_LAYER_NAME);
        if (fatherLayer instanceof MapGroupLayer groupLayer) {
            MapLayer nestedFatherLayer = findLayerByName(groupLayer.getLayers(), FIRST_QUEST_FATHER_LAYER_NAME);
            if (nestedFatherLayer != null) {
                fatherLayer = nestedFatherLayer;
            } else if (groupLayer.getLayers().getCount() > 0) {
                fatherLayer = groupLayer.getLayers().get(0);
            }
        }
        return tiledLayerBounds(fatherLayer);
    }

    private Rectangle firstQuestCafeDoorBounds() {
        return objectLayerBounds(findLayerByName(FIRST_QUEST_CAFE_ENTRANCE_LAYER_NAME));
    }

    private Vector2 firstQuestCafeNpcPosition() {
        Rectangle cafeDoorBounds = firstQuestCafeDoorBounds();
        if (cafeDoorBounds == null) {
            return null;
        }
        return new Vector2(
                cafeDoorBounds.x + cafeDoorBounds.width * 0.5f,
                cafeDoorBounds.y + cafeDoorBounds.height * 0.5f + FIRST_QUEST_CAFE_NPC_Y_OFFSET);
    }

    private boolean isPlayerNearFirstQuestCafeNpc() {
        Vector2 npcPosition = firstQuestCafeNpcPosition();
        if (npcPosition == null) {
            return false;
        }
        float playerCenterX = player.bounds.x + player.bounds.width * 0.5f;
        float playerCenterY = player.bounds.y + player.bounds.height * 0.5f;
        float dx = npcPosition.x - playerCenterX;
        float dy = npcPosition.y - playerCenterY;
        return dx * dx + dy * dy <= FIRST_QUEST_CAFE_NPC_INTERACT_RANGE * FIRST_QUEST_CAFE_NPC_INTERACT_RANGE;
    }

    private boolean isPlayerNearThirdQuestVillageFather() {
        if (!MAP_ID_VILLAGE.equals(world.activeMapId)) {
            return false;
        }
        ensureThirdQuestTrainingLayout();
        return overlapsPlayerExpanded(thirdQuestFatherTrainingBounds);
    }

    private Rectangle scientistNpcBounds() {
        if (!MAP_ID_RESEARCH_LAB_FLOOR_1.equals(world.activeMapId) || world.npcAnimationFrames.size == 0) {
            return null;
        }
        return doorAnimationVisualBounds(world.npcAnimationFrames);
    }

    private Rectangle sixthQuestEndingFatherBounds() {
        if (!MAP_ID_VILLAGE.equals(world.activeMapId)
                || sixthQuestStage < SIXTH_QUEST_ENDING
                || thirdQuestFatherVillageTiles == null
                || thirdQuestFatherVillageTiles.length < 4) {
            return null;
        }
        Rectangle portalBounds = sixthQuestActualPortalBounds();
        if (portalBounds == null) {
            portalBounds = objectLayerBounds(findLayerByName(FIFTH_QUEST_PORTAL_LAYER_NAME));
        }
        if (portalBounds == null) {
            return null;
        }
        return new Rectangle(
                portalBounds.x + portalBounds.width * 0.5f + SIXTH_QUEST_ENDING_FATHER_OFFSET_X_TILES * DEFAULT_TILE_SIZE,
                portalBounds.y + SIXTH_QUEST_ENDING_FATHER_OFFSET_Y_TILES * DEFAULT_TILE_SIZE,
                32f,
                48f);
    }

    private Rectangle fifthQuestPortalDialogueFatherBounds() {
        if (!MAP_ID_VILLAGE.equals(world.activeMapId)
                || thirdQuestFatherVillageTiles == null
                || thirdQuestFatherVillageTiles.length < 4) {
            return null;
        }
        float playerCenterX = player.bounds.x + player.bounds.width * 0.5f;
        float playerBottomY = player.bounds.y;
        fifthQuestPortalDialogueFatherBounds.set(
                playerCenterX + FIFTH_QUEST_PORTAL_DIALOGUE_FATHER_CENTER_OFFSET_X - 16f,
                playerBottomY + FIFTH_QUEST_PORTAL_DIALOGUE_FATHER_BOTTOM_OFFSET_Y,
                32f,
                48f);
        return fifthQuestPortalDialogueFatherBounds;
    }

    private Rectangle sixthQuestEndingScientistBounds() {
        Rectangle fatherBounds = sixthQuestEndingFatherBounds();
        if (fatherBounds == null || sixthQuestEndingScientistAnim == null) {
            return null;
        }
        TextureRegion frame = sixthQuestEndingScientistAnim.getKeyFrame(firstQuestCafeNpcStateTime, true);
        if (frame == null) {
            return null;
        }
        float drawW = frame.getRegionWidth() * SIXTH_QUEST_ENDING_SCIENTIST_DRAW_SCALE;
        float drawH = frame.getRegionHeight() * SIXTH_QUEST_ENDING_SCIENTIST_DRAW_SCALE;
        dialogueHighlightWorldBounds.set(
                fatherBounds.x + fatherBounds.width * 0.5f + SIXTH_QUEST_ENDING_SCIENTIST_OFFSET_X_TILES * DEFAULT_TILE_SIZE
                        - drawW * 0.5f,
                fatherBounds.y + SIXTH_QUEST_ENDING_SCIENTIST_OFFSET_Y_TILES * DEFAULT_TILE_SIZE,
                drawW,
                drawH);
        return dialogueHighlightWorldBounds;
    }

    private Rectangle fifthQuestPortalDialogueScientistBounds() {
        if (!MAP_ID_VILLAGE.equals(world.activeMapId) || sixthQuestEndingScientistAnim == null) {
            return null;
        }
        float playerCenterX = player.bounds.x + player.bounds.width * 0.5f;
        float playerBottomY = player.bounds.y;
        fifthQuestPortalDialogueScientistBounds.set(
                playerCenterX + FIFTH_QUEST_PORTAL_DIALOGUE_SCIENTIST_CENTER_OFFSET_X
                        - FIFTH_QUEST_PORTAL_DIALOGUE_SCIENTIST_WIDTH * 0.5f,
                playerBottomY + FIFTH_QUEST_PORTAL_DIALOGUE_SCIENTIST_BOTTOM_OFFSET_Y,
                FIFTH_QUEST_PORTAL_DIALOGUE_SCIENTIST_WIDTH,
                FIFTH_QUEST_PORTAL_DIALOGUE_SCIENTIST_HEIGHT);
        return fifthQuestPortalDialogueScientistBounds;
    }

    private boolean isFifthQuestPortalDialogueSceneActive() {
        if (!MAP_ID_VILLAGE.equals(world.activeMapId)) {
            return false;
        }
        return activeQuestDialogueId == QuestDialogueId.FIFTH_QUEST_PORTAL_AFTER
                || activeQuestDialogueId == QuestDialogueId.SIXTH_QUEST_PORTAL_BRIEFING;
    }

    private float fifthQuestPortalDialogueNpcFadeAlpha() {
        if (!isFifthQuestPortalDialogueSceneActive()) {
            return 0f;
        }
        if (FIFTH_QUEST_PORTAL_DIALOGUE_NPC_FADE_IN_SECONDS <= 0f) {
            return 1f;
        }
        return clamp01(fifthQuestPortalDialogueNpcFadeTimer / FIFTH_QUEST_PORTAL_DIALOGUE_NPC_FADE_IN_SECONDS);
    }

    private boolean isPlayerNearScientistNpc() {
        return overlapsPlayerExpanded(scientistNpcBounds());
    }

    private Rectangle secondQuestArchiveBounds() {
        if (!MAP_ID_RESEARCH_LAB_FLOOR_2.equals(world.activeMapId)) {
            return null;
        }
        return tiledLayerBounds(findLayerByName(SECOND_QUEST_ARCHIVE_LAYER_NAME));
    }

    private boolean isPlayerNearSecondQuestArchive() {
        return overlapsPlayerExpanded(secondQuestArchiveBounds());
    }

    private boolean isPlayerNearFourthQuestCafeMark() {
        ensureFourthQuestVillageLayout();
        return MAP_ID_VILLAGE.equals(world.activeMapId) && overlapsPlayerExpanded(fourthQuestCafeSiteBounds);
    }

    private boolean isPlayerNearFourthQuestCenterMark() {
        ensureFourthQuestVillageLayout();
        return MAP_ID_VILLAGE.equals(world.activeMapId) && overlapsPlayerExpanded(fourthQuestCenterSiteBounds);
    }

    private boolean isPlayerNearFourthQuestEdgeMark() {
        ensureFourthQuestVillageLayout();
        return MAP_ID_VILLAGE.equals(world.activeMapId) && overlapsPlayerExpanded(fourthQuestEdgeSiteBounds);
    }

    private boolean isPlayerNearFourthQuestArrayNode(int nodeIndex) {
        if (nodeIndex < 0 || nodeIndex >= fourthQuestArrayNodeBounds.length) {
            return false;
        }
        ensureFourthQuestFloor2Layout();
        return MAP_ID_RESEARCH_LAB_FLOOR_2.equals(world.activeMapId)
                && overlapsPlayerExpanded(fourthQuestArrayNodeBounds[nodeIndex]);
    }

    private boolean isPlayerNearFourthQuestArrayConsole() {
        ensureFourthQuestFloor2Layout();
        return MAP_ID_RESEARCH_LAB_FLOOR_2.equals(world.activeMapId)
                && overlapsPlayerExpanded(fourthQuestArrayConsoleBounds);
    }

    private boolean isPlayerNearFourthQuestBreachSite() {
        ensureFourthQuestVillageLayout();
        return MAP_ID_VILLAGE.equals(world.activeMapId)
                && overlapsPlayerExpanded(fourthQuestBreachSiteBounds);
    }

    private boolean isPlayerNearFifthQuestAnchor(int anchorIndex) {
        ensureFifthQuestVillageLayout();
        return MAP_ID_VILLAGE.equals(world.activeMapId)
                && anchorIndex >= 0
                && anchorIndex < fifthQuestAnchorBounds.length
                && overlapsPlayerExpanded(fifthQuestAnchorBounds[anchorIndex]);
    }

    private boolean isPlayerNearFifthQuestPortal() {
        ensureFifthQuestVillageLayout();
        return MAP_ID_VILLAGE.equals(world.activeMapId)
                && overlapsPlayerExpanded(fifthQuestPortalBounds);
    }

    private boolean isPlayerNearSixthQuestSeal(boolean firstSeal) {
        ensureSixthQuestCastleLayout();
        Rectangle bounds = firstSeal ? sixthQuestSealAInteractBounds : sixthQuestSealBInteractBounds;
        return MAP_ID_DEMON_CASTLE.equals(world.activeMapId)
                && bounds != null
                && overlapsPlayerExpanded(bounds);
    }

    private String[] repeatSpeaker(String speaker, int count) {
        int safeCount = Math.max(0, count);
        String[] speakers = new String[safeCount];
        Arrays.fill(speakers, speaker == null ? "" : speaker);
        return speakers;
    }

    private void levelPlayerToCap(int targetLevelCap) {
        int nextLevel = clampProgressionLevelCap(targetLevelCap);
        progressionLevelCap = Math.max(progressionLevelCap, nextLevel);
        applyPlayerLevelStats(nextLevel,
                PlayerLevelStats.maxHealthForLevel(nextLevel),
                PlayerLevelStats.maxStaminaForLevel(nextLevel));
    }

    private void finishSecondQuest() {
        secondQuestStage = SECOND_QUEST_COMPLETE;
        scientistProgressionUnlocked = true;
        thirdQuestAvailable = true;
        levelPlayerToCap(QUEST_2_LEVEL_CAP);
        showQuestNotification(
                "Quest Complete",
                "Ashes That Remain is complete. The scientist can now guide your progression.",
                FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
        refreshFirstQuestGuidance();
    }

    private void finishThirdQuest() {
        thirdQuestStage = THIRD_QUEST_COMPLETE;
        thirdQuestAvailable = true;
        scientistProgressionUnlocked = true;
        levelPlayerToCap(QUEST_3_LEVEL_CAP);
        showQuestNotification(
                "Quest Complete",
                "Lessons of Survival is complete. E Dash Attack is now ready.",
                FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
        refreshFirstQuestGuidance();
    }

    private void finishFourthQuest() {
        fourthQuestStage = FOURTH_QUEST_COMPLETE;
        scientistProgressionUnlocked = true;
        levelPlayerToCap(QUEST_4_LEVEL_CAP);
        showQuestNotification(
                "Quest Complete",
                "The Mark Returns is complete. Spatial Dash is now ready.",
                FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
        refreshFirstQuestGuidance();
    }

    private void finishFifthQuest() {
        fifthQuestStage = FIFTH_QUEST_COMPLETE;
        scientistProgressionUnlocked = true;
        levelPlayerToCap(QUEST_4_LEVEL_CAP);
        applyVillagePortalVisualState();
        showQuestNotification(
                "Quest Complete",
                "Embers at the Gate is complete. The path to the Demon Castle is open.",
                FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
        refreshFirstQuestGuidance();
    }

    private void startSixthQuest() {
        sixthQuestStage = SIXTH_QUEST_ENTER_PORTAL;
        sixthQuestEncounterSpawned = false;
        sixthQuestBossCalloutTimer = 0f;
        sixthQuestBossCalloutIndex = 0;
        sixthQuestLowHealthExchangePlayed = false;
        clearSixthQuestPortalActivation();
        applyVillagePortalVisualState();
        showQuestNotification(
                SIXTH_QUEST_NOTIFICATION_TITLE,
                "Objective Updated: Go to the dormant portal.",
                FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
        refreshFirstQuestGuidance();
    }

    private void beginSixthQuestOuterAssault() {
        sixthQuestStage = SIXTH_QUEST_OUTER_ASSAULT;
        sixthQuestEncounterSpawned = false;
        clearSixthQuestPortalActivation();
        clearFifthQuestEncounterDemons(SIXTH_QUEST_ENCOUNTER_OUTER);
        showQuestNotification(
                SIXTH_QUEST_NOTIFICATION_TITLE,
                "Objective Updated: Defeat the demons blocking the route to the inner gate.",
                FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
    }

    private void beginSixthQuestSealABattle() {
        sixthQuestStage = SIXTH_QUEST_SEAL_A_BATTLE;
        sixthQuestEncounterSpawned = false;
        clearFifthQuestEncounterDemons(SIXTH_QUEST_ENCOUNTER_SEAL_A);
        showQuestNotification(
                SIXTH_QUEST_NOTIFICATION_TITLE,
                "Objective Updated: Break the first Castle Seal.",
                FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
    }

    private void beginSixthQuestSealBBattle() {
        sixthQuestStage = SIXTH_QUEST_SEAL_B_BATTLE;
        sixthQuestEncounterSpawned = false;
        clearFifthQuestEncounterDemons(SIXTH_QUEST_ENCOUNTER_SEAL_B);
        showQuestNotification(
                SIXTH_QUEST_NOTIFICATION_TITLE,
                "Objective Updated: Break the second Castle Seal.",
                FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
    }

    private void beginSixthQuestThroneGuards() {
        sixthQuestStage = SIXTH_QUEST_THRONE_GUARDS;
        sixthQuestEncounterSpawned = false;
        clearFifthQuestEncounterDemons(SIXTH_QUEST_ENCOUNTER_THRONE_GUARDS);
        showQuestNotification(
                SIXTH_QUEST_NOTIFICATION_TITLE,
                "Objective Updated: Advance to the Throne Hall and defeat the throne guards.",
                FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
    }

    private void beginSixthQuestBossPhase1() {
        sixthQuestStage = SIXTH_QUEST_BOSS_PHASE_1;
        sixthQuestEncounterSpawned = false;
        sixthQuestBossCalloutTimer = 0f;
        sixthQuestBossCalloutIndex = 0;
        sixthQuestLowHealthExchangePlayed = false;
        showQuestNotification(
                SIXTH_QUEST_NOTIFICATION_TITLE,
                "Objective Updated: Confront the Demon.",
                FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
    }

    private void beginSixthQuestFinalPhaseIntro() {
        sixthQuestStage = SIXTH_QUEST_FINAL_PHASE_INTRO;
        sixthQuestEncounterSpawned = false;
        sixthQuestBossCalloutTimer = 0f;
        sixthQuestBossCalloutIndex = 0;
        sixthQuestLowHealthExchangePlayed = false;
        startMapTransition(MAP_ID_BOSS_ARENA_PHASE_2, BOSS_ARENA_PHASE_2_PLAYER_SPAWN_LAYER_NAME);
    }

    private void beginSixthQuestFinalPhaseBattle() {
        sixthQuestStage = SIXTH_QUEST_FINAL_PHASE_BATTLE;
        sixthQuestEncounterSpawned = false;
        sixthQuestBossCalloutTimer = 0f;
        sixthQuestBossCalloutIndex = 0;
        sixthQuestLowHealthExchangePlayed = false;
        clearFifthQuestEncounterDemons(SIXTH_QUEST_ENCOUNTER_BOSS_PHASE_2);
        showQuestNotification(
                SIXTH_QUEST_NOTIFICATION_TITLE,
                "Objective Updated: Defeat the Demon.",
                FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
        if (hudRenderer != null) {
            hudRenderer.showNotification(
                    "Final Battle",
                    "Use E Dash Attack to punish openings and Spatial Dash to evade the demon's heaviest attacks.",
                    FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
        }
    }

    private void beginSixthQuestEnding() {
        sixthQuestStage = SIXTH_QUEST_ENDING;
        sixthQuestEncounterSpawned = false;
        sixthQuestBossCalloutTimer = 0f;
        sixthQuestBossCalloutIndex = 0;
        sixthQuestLowHealthExchangePlayed = false;
        sixthQuestEndingNpcFadeActive = false;
        sixthQuestEndingNpcFadeTimer = 0f;
        sixthQuestEndingVideoSequenceActive = false;
        sixthQuestEndingVideoTriggered = false;
        sixthQuestEndingVideoSequenceTimer = 0f;
        clearSixthQuestPortalActivation();
        startMapTransition(MAP_ID_VILLAGE, VILLAGE_PORTAL_ACTUAL_LAYER_NAME);
    }

    private void finishSixthQuest() {
        sixthQuestStage = SIXTH_QUEST_COMPLETE;
        gameCompleted = true;
        clearSixthQuestPortalActivation();
        applyVillagePortalVisualState();
        showQuestNotification(
                "Game Complete",
                "The Last Ember is complete. The demon's fire is gone.",
                FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
        refreshFirstQuestGuidance();
    }

    private boolean isVillagePortalActive() {
        if (gameCompleted || sixthQuestStage >= SIXTH_QUEST_ENDING) {
            return false;
        }
        if (sixthQuestStage == SIXTH_QUEST_ENTER_PORTAL) {
            return isSixthQuestPortalActivationRunning();
        }
        return fifthQuestStage >= FIFTH_QUEST_COMPLETE || sixthQuestStage > SIXTH_QUEST_DISABLED;
    }

    private boolean isSixthQuestPortalActivationRunning() {
        return sixthQuestPortalActivationPhase != PortalActivationPhase.NONE;
    }

    private void clearSixthQuestPortalActivation() {
        sixthQuestPortalActivationPhase = PortalActivationPhase.NONE;
        sixthQuestPortalChoiceResult = PortalChoiceResult.NONE;
        sixthQuestPortalActivationStateTime = 0f;
        sixthQuestPortalActivationLoopTimer = 0f;
        sixthQuestPortalChoiceVisible = false;
        sixthQuestPortalChoiceIndex = 0;
        sixthQuestPortalEffectStartPosition.setZero();
        sixthQuestPortalEffectTargetPosition.setZero();
        sixthQuestPortalEffectPosition.setZero();
        sixthQuestPortalChoiceYesBounds.set(0f, 0f, 0f, 0f);
        sixthQuestPortalChoiceNoBounds.set(0f, 0f, 0f, 0f);
    }

    private Rectangle sixthQuestInactivePortalBounds() {
        return tiledLayerBounds(findLayerInGroup(VILLAGE_PORTAL_INACTIVE_LAYER_NAME, VILLAGE_PORTAL_INACTIVE_LAYER_NAME));
    }

    private Rectangle sixthQuestActivePortalBounds() {
        return tiledLayerBounds(findLayerInGroup(VILLAGE_PORTAL_ACTIVE_LAYER_NAME, VILLAGE_PORTAL_ACTIVE_LAYER_NAME));
    }

    private Rectangle sixthQuestActualPortalBounds() {
        return objectLayerBounds(findLayerByName(VILLAGE_PORTAL_ACTUAL_LAYER_NAME));
    }

    private Rectangle sixthQuestPortalInteractionBounds() {
        Rectangle bounds = isSixthQuestPortalActivationRunning()
                ? sixthQuestActivePortalBounds()
                : sixthQuestInactivePortalBounds();
        if (bounds != null && bounds.width > 0f && bounds.height > 0f) {
            return bounds;
        }
        bounds = sixthQuestActualPortalBounds();
        if (bounds != null && bounds.width > 0f && bounds.height > 0f) {
            return bounds;
        }
        return objectLayerBounds(findLayerByName(FIFTH_QUEST_PORTAL_LAYER_NAME));
    }

    private boolean isPlayerWithinVillagePortalInteraction() {
        Rectangle bounds = sixthQuestPortalInteractionBounds();
        return bounds != null && Intersector.overlaps(player.bounds, bounds);
    }

    private Rectangle sixthQuestPortalGuideBounds() {
        Rectangle bounds = sixthQuestInactivePortalBounds();
        if (bounds != null && bounds.width > 0f && bounds.height > 0f) {
            return bounds;
        }
        bounds = sixthQuestActualPortalBounds();
        if (bounds != null && bounds.width > 0f && bounds.height > 0f) {
            return bounds;
        }
        return objectLayerBounds(findLayerByName(FIFTH_QUEST_PORTAL_LAYER_NAME));
    }

    private void ensureSixthQuestPortalEffectAnimationsLoaded() {
        if (bossArenaPhase2SkillStartAnim != null
                || bossArenaPhase2SkillLoopAnim != null
                || bossArenaPhase2SkillEndAnim != null) {
            return;
        }
        loadBossArenaPhase2DemonAssets();
    }

    private void startSixthQuestPortalActivation() {
        if (world.map == null || !MAP_ID_VILLAGE.equals(world.activeMapId) || isSixthQuestPortalActivationRunning()) {
            return;
        }
        ensureSixthQuestPortalEffectAnimationsLoaded();
        Rectangle startBounds = sixthQuestPortalGuideBounds();
        Rectangle targetBounds = sixthQuestActualPortalBounds();
        if (startBounds != null && startBounds.width > 0f && startBounds.height > 0f) {
            sixthQuestPortalEffectStartPosition.set(
                    startBounds.x + startBounds.width * 0.5f,
                    startBounds.y + startBounds.height * 0.5f);
        } else {
            sixthQuestPortalEffectStartPosition.set(player.bounds.x + player.bounds.width * 0.5f,
                    player.bounds.y + player.bounds.height * 0.5f);
        }
        if (targetBounds != null && targetBounds.width > 0f && targetBounds.height > 0f) {
            sixthQuestPortalEffectTargetPosition.set(
                    targetBounds.x + targetBounds.width * 0.5f,
                    targetBounds.y + targetBounds.height * 0.5f);
        } else {
            sixthQuestPortalEffectTargetPosition.set(sixthQuestPortalEffectStartPosition);
        }
        sixthQuestPortalEffectPosition.set(sixthQuestPortalEffectStartPosition);
        sixthQuestPortalActivationPhase = bossArenaPhase2SkillStartAnim != null
                ? PortalActivationPhase.START
                : PortalActivationPhase.LOOP;
        sixthQuestPortalChoiceResult = PortalChoiceResult.NONE;
        sixthQuestPortalActivationStateTime = 0f;
        sixthQuestPortalActivationLoopTimer = 0f;
        sixthQuestPortalChoiceVisible = false;
        sixthQuestPortalChoiceIndex = 0;
        applyVillagePortalVisualState();
        if (hudRenderer != null) {
            hudRenderer.showNotification(
                    SIXTH_QUEST_NOTIFICATION_TITLE,
                    "The portal awakens.",
                    FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
        }
    }

    private void updateSixthQuestPortalActivation(float delta) {
        if (!isSixthQuestPortalActivationRunning()) {
            return;
        }
        float safeDelta = Math.max(0f, delta);
        sixthQuestPortalActivationStateTime += safeDelta;
        switch (sixthQuestPortalActivationPhase) {
            case START -> {
                float startDuration = bossArenaPhase2SkillStartAnim != null
                        ? Math.max(0.001f, bossArenaPhase2SkillStartAnim.getAnimationDuration())
                        : 0.001f;
                float startProgress = clamp01(sixthQuestPortalActivationStateTime / startDuration);
                sixthQuestPortalEffectPosition.set(sixthQuestPortalEffectStartPosition)
                        .lerp(sixthQuestPortalEffectTargetPosition, startProgress);
                if (bossArenaPhase2SkillStartAnim == null
                        || bossArenaPhase2SkillStartAnim.isAnimationFinished(sixthQuestPortalActivationStateTime)) {
                    sixthQuestPortalActivationPhase = PortalActivationPhase.LOOP;
                    sixthQuestPortalActivationStateTime = 0f;
                    sixthQuestPortalActivationLoopTimer = 0f;
                    sixthQuestPortalChoiceVisible = false;
                    sixthQuestPortalEffectPosition.set(sixthQuestPortalEffectTargetPosition);
                }
            }
            case LOOP -> {
                sixthQuestPortalEffectPosition.set(sixthQuestPortalEffectTargetPosition);
                sixthQuestPortalActivationLoopTimer += safeDelta;
                if (!sixthQuestPortalChoiceVisible
                        && sixthQuestPortalActivationLoopTimer >= SIXTH_QUEST_PORTAL_EFFECT_LOOP_SECONDS) {
                    sixthQuestPortalChoiceVisible = true;
                    sixthQuestPortalChoiceIndex = 0;
                }
            }
            case END -> {
                sixthQuestPortalEffectPosition.set(sixthQuestPortalEffectTargetPosition);
                if (bossArenaPhase2SkillEndAnim == null
                        || bossArenaPhase2SkillEndAnim.isAnimationFinished(sixthQuestPortalActivationStateTime)) {
                    finishSixthQuestPortalActivationChoice();
                }
            }
            case NONE -> {
            }
        }
    }

    private void confirmSixthQuestPortalChoice(boolean enterCastle) {
        if (!sixthQuestPortalChoiceVisible || sixthQuestPortalActivationPhase != PortalActivationPhase.LOOP) {
            return;
        }
        sixthQuestPortalChoiceVisible = false;
        sixthQuestPortalChoiceResult = enterCastle
                ? PortalChoiceResult.ENTER_CASTLE
                : PortalChoiceResult.CLOSE_PORTAL;
        sixthQuestPortalActivationStateTime = 0f;
        sixthQuestPortalActivationLoopTimer = 0f;
        if (bossArenaPhase2SkillEndAnim != null) {
            sixthQuestPortalActivationPhase = PortalActivationPhase.END;
            return;
        }
        finishSixthQuestPortalActivationChoice();
    }

    private void finishSixthQuestPortalActivationChoice() {
        PortalChoiceResult choiceResult = sixthQuestPortalChoiceResult;
        clearSixthQuestPortalActivation();
        applyVillagePortalVisualState();
        if (choiceResult == PortalChoiceResult.ENTER_CASTLE) {
            startMapTransition(MAP_ID_DEMON_CASTLE, "Spawn");
            return;
        }
        if (hudRenderer != null) {
            hudRenderer.showNotification(
                    SIXTH_QUEST_NOTIFICATION_TITLE,
                    "The portal closes.",
                    FIRST_QUEST_COMPLETION_NOTIFICATION_SECONDS);
        }
        refreshFirstQuestGuidance();
    }

    private void applyVillagePortalVisualState() {
        if (!MAP_ID_VILLAGE.equals(world.activeMapId) || world.map == null) {
            return;
        }
        boolean active = isVillagePortalActive();
        MapLayer portalRootLayer = findLayerByName(VILLAGE_PORTAL_GROUP_NAME);
        if (portalRootLayer instanceof MapGroupLayer portalGroup) {
            portalGroup.setVisible(true);
            applyPortalVariantVisibility(portalGroup.getLayers(), VILLAGE_PORTAL_ACTIVE_LAYER_NAME, active);
            applyPortalVariantVisibility(portalGroup.getLayers(), VILLAGE_PORTAL_INACTIVE_LAYER_NAME, !active);
            return;
        }

        MapLayer activeLayer = findLayerByName(VILLAGE_PORTAL_ACTIVE_LAYER_NAME);
        MapLayer inactiveLayer = findLayerByName(VILLAGE_PORTAL_INACTIVE_LAYER_NAME);
        setLayerTreeVisible(activeLayer, active);
        setLayerTreeVisible(inactiveLayer, !active);
    }

    private void applyPortalVariantVisibility(MapLayers layers, String targetName, boolean visible) {
        if (layers == null || targetName == null || targetName.isBlank()) {
            return;
        }
        for (MapLayer layer : layers) {
            if (layer == null) {
                continue;
            }
            if (targetName.equalsIgnoreCase(layer.getName())) {
                setLayerTreeVisible(layer, visible);
            }
            if (layer instanceof MapGroupLayer groupLayer) {
                applyPortalVariantVisibility(groupLayer.getLayers(), targetName, visible);
            }
        }
    }

    private void setLayerTreeVisible(MapLayer layer, boolean visible) {
        if (layer == null) {
            return;
        }
        layer.setVisible(visible);
        if (layer instanceof MapGroupLayer groupLayer) {
            for (MapLayer childLayer : groupLayer.getLayers()) {
                setLayerTreeVisible(childLayer, visible);
            }
        }
    }

    private void handleQuestMapLoadEvents() {
        if (world.activeMapId == null || activeQuestDialogueId != QuestDialogueId.NONE || firstQuestDialogueActive) {
            return;
        }
        if (sixthQuestStage == SIXTH_QUEST_ENTER_PORTAL && MAP_ID_DEMON_CASTLE.equals(world.activeMapId)) {
            beginSixthQuestOuterAssault();
            return;
        }
        if (sixthQuestStage == SIXTH_QUEST_FINAL_PHASE_INTRO && MAP_ID_BOSS_ARENA_PHASE_2.equals(world.activeMapId)) {
            startBossArenaFinalIntroSequence();
            return;
        }
        if (sixthQuestStage == SIXTH_QUEST_ENDING && MAP_ID_VILLAGE.equals(world.activeMapId)) {
            startQuestDialogue(
                    QuestDialogueId.SIXTH_QUEST_ENDING,
                    SIXTH_QUEST_ENDING_SPEAKERS,
                    SIXTH_QUEST_ENDING_DIALOGUE_LINES);
        }
    }

    private boolean tryHandleMapInteraction() {
        return tryHandleFirstQuestInteraction()
                || trySixthQuestPortalTransition()
                || tryMapTransition(
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

    private boolean trySixthQuestPortalTransition() {
        if (sixthQuestStage < SIXTH_QUEST_ENTER_PORTAL
                || sixthQuestStage >= SIXTH_QUEST_ENDING
                || !MAP_ID_VILLAGE.equals(world.activeMapId)
                || !isPlayerWithinVillagePortalInteraction()) {
            return false;
        }
        if (sixthQuestStage == SIXTH_QUEST_ENTER_PORTAL) {
            if (!isSixthQuestPortalActivationRunning()) {
                startSixthQuestPortalActivation();
            }
            return true;
        }
        if (!isVillagePortalActive()) {
            return false;
        }
        if (sixthQuestStage >= SIXTH_QUEST_FINAL_PHASE_INTRO) {
            startMapTransition(MAP_ID_BOSS_ARENA_PHASE_2, BOSS_ARENA_PHASE_2_PLAYER_SPAWN_LAYER_NAME);
        } else {
            startMapTransition(MAP_ID_DEMON_CASTLE, "Spawn");
        }
        return true;
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
        if (player.currentMana < DASH_STAMINA_COST) {
            return;
        }
        player.dashDirX = dx / len;
        player.dashDirY = dy / len;
        if (player.dashDirX != 0f) player.facingLeft = player.dashDirX < 0f;
        player.currentMana = Math.max(0f, player.currentMana - DASH_STAMINA_COST);
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
        if (activeAttackAnim == dashAttackAnim) {
            dashAttackAbilityCooldownTimer = DASH_ATTACK_ABILITY_COOLDOWN_SECONDS;
        } else if (activeAttackAnim == spetialDashAnim) {
            spatialDashAbilityCooldownTimer = SPATIAL_DASH_ABILITY_COOLDOWN_SECONDS;
        }

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
        if (bats.size == 0 && evilWizards.size == 0 && golem1s.size == 0
                && bossArenaPhase2Demons.size == 0 && fifthQuestDemons.size == 0) {
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

        for (int i = 0; i < fifthQuestDemons.size; i++) {
            FifthQuestDemonInstance demon = fifthQuestDemons.get(i);
            if (demon.lastDashAttackHitId == activeDashAttackHitId
                    || demon.dying
                    || demon.health <= 0f
                    || demon.playerHitCooldownTimer > 0f) {
                continue;
            }
            float demonHitboxWidth = fifthQuestDemonHitboxHalfWidth() * 2f;
            float demonHitboxHeight = fifthQuestDemonHitboxHalfHeight() * 2f;
            float demonHitboxAverageSize = (demonHitboxWidth + demonHitboxHeight) * 0.5f;
            float demonHitRadius = hitRadius + demonHitboxAverageSize * 0.25f;
            float demonHitRadiusSq = demonHitRadius * demonHitRadius;
            float demonHitboxCenterX = fifthQuestDemonHitboxCenterX(demon);
            float demonHitboxCenterY = fifthQuestDemonHitboxCenterY(demon);
            float distanceSq = pointSegmentDistanceSquared(
                    demonHitboxCenterX,
                    demonHitboxCenterY,
                    startCenterX,
                    startCenterY,
                    endCenterX,
                    endCenterY);
            if (distanceSq <= demonHitRadiusSq) {
                demon.lastDashAttackHitId = activeDashAttackHitId;
                applyHitToFifthQuestDemonFromPlayer(demon);
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

        if (room3DoorCollisionEnabled
                && hasRoom3DoorCollisionBounds()
                && Intersector.overlaps(rect, room3DoorClosedCollisionBounds)) {
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
        if (isBossArenaFinalIntroActive() || isFifthQuestFirstEncounterIntroActive()) {
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

    private void updateDialogueCamera(float delta) {
        if (camera == null || !firstQuestDialogueActive) {
            return;
        }
        float playerCenterX = player.bounds.x + player.bounds.width * 0.5f;
        float playerCenterY = player.bounds.y + player.bounds.height * 0.5f;
        float targetX = playerCenterX;
        float targetY = playerCenterY;
        if (resolveActiveDialogueFocusPoint(dialogueCameraFocusPoint)) {
            targetX = (playerCenterX + dialogueCameraFocusPoint.x) * 0.5f;
            targetY = (playerCenterY + dialogueCameraFocusPoint.y) * 0.5f;
        }
        float safeDelta = Math.max(0f, delta);
        float alpha = 1f - (float) Math.exp(-DIALOGUE_CAMERA_SMOOTH_SPEED * safeDelta);
        camera.position.x = lerp(camera.position.x, targetX, alpha);
        camera.position.y = lerp(camera.position.y, targetY, alpha);
        camera.position.z = 0f;
        float targetZoom = DEFAULT_ZOOM * DIALOGUE_CAMERA_ZOOM_MULTIPLIER;
        camera.zoom = lerp(camera.zoom, targetZoom, alpha);
        if (Math.abs(camera.zoom - targetZoom) < 0.0005f) {
            camera.zoom = targetZoom;
        }
    }

    private boolean resolveActiveDialogueFocusPoint(Vector2 focusPoint) {
        if (focusPoint == null || activeQuestDialogueId == null || activeQuestDialogueId == QuestDialogueId.NONE) {
            return false;
        }
        return switch (activeQuestDialogueId) {
            case FIRST_QUEST_FATHER, THIRD_QUEST_FATHER_RETURN -> setVectorToRectCenter(focusPoint, firstQuestFatherBounds());
            case SECOND_QUEST_CAFE_OWNER, FOURTH_QUEST_CAFE_OWNER -> setVector(focusPoint, firstQuestCafeNpcPosition());
            case SECOND_QUEST_SCIENTIST_INTRO,
                    SECOND_QUEST_SCIENTIST_RETURN,
                    THIRD_QUEST_SCIENTIST_SENDOFF,
                    FOURTH_QUEST_SCIENTIST_REPORT,
                    FOURTH_QUEST_SAMPLE_ANALYSIS,
                    FOURTH_QUEST_SPATIAL_UNLOCK,
                    FOURTH_QUEST_EPILOGUE,
                    FIFTH_QUEST_LAB_ALARM -> setVectorToRectCenter(focusPoint, scientistNpcBounds());
            case THIRD_QUEST_FATHER_TRAINING_INTRO,
                    THIRD_QUEST_FATHER_AFTER_MOVEMENT,
                    THIRD_QUEST_FATHER_AFTER_ATTACK,
                    THIRD_QUEST_FATHER_PRE_SPAR,
                    THIRD_QUEST_FATHER_COMPLETE,
                    FOURTH_QUEST_INTERRUPTION -> setVectorToThirdQuestFather(focusPoint);
            case SIXTH_QUEST_ENDING -> setVectorToRectCenter(focusPoint, sixthQuestEndingFatherBounds());
            case FOURTH_QUEST_CAFE_MARK -> setVectorToFourthQuestVillageSite(focusPoint, fourthQuestCafeSiteBounds);
            case FOURTH_QUEST_CENTER_MARK, FOURTH_QUEST_CENTER_AFTER -> setVectorToFourthQuestVillageSite(focusPoint, fourthQuestCenterSiteBounds);
            case FOURTH_QUEST_EDGE_MARK, FOURTH_QUEST_EDGE_AFTER -> setVectorToFourthQuestVillageSite(focusPoint, fourthQuestEdgeSiteBounds);
            case FOURTH_QUEST_ARRAY_SCAN_COMPLETE -> setVectorToFourthQuestArrayConsole(focusPoint);
            case FOURTH_QUEST_BREACH_INTRO, FOURTH_QUEST_BREACH_AFTER -> setVectorToFourthQuestVillageSite(focusPoint, fourthQuestBreachSiteBounds);
            case FIFTH_QUEST_PORTAL_AFTER, SIXTH_QUEST_PORTAL_BRIEFING -> setVectorToFifthQuestPortalDialogueScene(focusPoint);
            case SIXTH_QUEST_CASTLE_ARRIVAL -> setVectorToRectCenter(focusPoint, player.bounds);
            case SIXTH_QUEST_PHASE_1_INTRO, SIXTH_QUEST_PHASE_1_TRANSITION -> setVectorToSixthQuestBossPhase1(focusPoint);
            case SIXTH_QUEST_FINAL_PHASE_INTRO -> setVectorToFinalPhaseBoss(focusPoint);
            default -> false;
        };
    }

    private boolean setVectorToThirdQuestFather(Vector2 target) {
        ensureThirdQuestTrainingLayout();
        return setVectorToRectCenter(target, thirdQuestFatherTrainingBounds);
    }

    private boolean setVectorToFifthQuestPortalDialogueScene(Vector2 target) {
        if (target == null) {
            return false;
        }
        Rectangle combinedBounds = fifthQuestPortalDialogueCombinedBounds();
        if (combinedBounds != null) {
            return setVectorToRectCenter(target, combinedBounds);
        }
        return setVectorToFifthQuestPortalArea(target);
    }

    private boolean setVectorToFifthQuestPortalArea(Vector2 target) {
        ensureFifthQuestVillageLayout();
        Rectangle focusBounds = fifthQuestFightingAreaBounds.width > 0f ? fifthQuestFightingAreaBounds : fifthQuestPortalBounds;
        return setVectorToRectCenter(target, focusBounds);
    }

    private boolean setVectorToFourthQuestVillageSite(Vector2 target, Rectangle bounds) {
        ensureFourthQuestVillageLayout();
        return setVectorToRectCenter(target, bounds);
    }

    private boolean setVectorToFourthQuestArrayConsole(Vector2 target) {
        ensureFourthQuestFloor2Layout();
        return setVectorToRectCenter(target, fourthQuestArrayConsoleBounds);
    }

    private boolean setVectorToSixthQuestBossPhase1(Vector2 target) {
        ensureSixthQuestCastleLayout();
        return setVectorToRectCenter(target, sixthQuestBossPhase1SpawnBounds);
    }

    private boolean setVectorToFinalPhaseBoss(Vector2 target) {
        if (target == null) {
            return false;
        }
        for (int i = 0; i < bossArenaPhase2Demons.size; i++) {
            BossArenaPhase2DemonInstance demon = bossArenaPhase2Demons.get(i);
            if (demon != null && demon.health > 0f) {
                target.set(demon.position.x, demon.position.y);
                return true;
            }
        }
        ensureSixthQuestFinalArenaLayout();
        return setVectorToRectCenter(target, sixthQuestFinalPhaseSpawnBounds);
    }

    private boolean setVectorToRectCenter(Vector2 target, Rectangle bounds) {
        if (target == null || bounds == null || bounds.width <= 0f || bounds.height <= 0f) {
            return false;
        }
        target.set(bounds.x + bounds.width * 0.5f, bounds.y + bounds.height * 0.5f);
        return true;
    }

    private boolean setVector(Vector2 target, Vector2 source) {
        if (target == null || source == null) {
            return false;
        }
        target.set(source);
        return true;
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
            playSfx(golem1ExplosionSfx, GOLEM_1_EXPLOSION_SFX_VOLUME);
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

        applyDamageToPlayer(GOLEM_1_EXPLOSION_DAMAGE);
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
        playerDeathTex = loadPlayerDeathTexture();
        attack1Sfx = Gdx.audio.newSound(Gdx.files.internal("sounds/attack 1.mp3"));
        attack3Sfx = Gdx.audio.newSound(Gdx.files.internal("sounds/attack 3.mp3"));
        attackSpecialSfx = Gdx.audio.newSound(Gdx.files.internal("sounds/attacks.mp3"));
        batHurtSfx = Gdx.audio.newSound(Gdx.files.internal("sounds/Bat-hurt.mp3"));
        indoorFootstepLoopSfx = Gdx.audio.newSound(Gdx.files.internal(INDOOR_FOOTSTEP_LOOP_SFX_PATH));
        typewriterLoopSfx = Gdx.audio.newSound(Gdx.files.internal(TYPEWRITER_LOOP_SFX_PATH));
        fifthQuestVillagerScreamSfx = loadOptionalInternalSound(FIFTH_QUEST_VILLAGER_SCREAM_SFX_PATH);
        demonFirstInteractionSfx = loadOptionalInternalSound(DEMON_FIRST_INTERACTION_SFX_PATH);
        demonNeutral2Sfx = loadOptionalInternalSound(DEMON_NEUTRAL_2_SFX_PATH);
        loadNpcDialogueVoiceSfx();
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
        if (playerDeathTex != null) {
            playerDeathTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
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
                    PLAYER_HAZARD_HIT_REACTION_SECONDS / hitFrameCount,
                    0,
                    1);
            playerHitAnim.setPlayMode(Animation.PlayMode.NORMAL);
        } else {
            playerHitAnim = null;
        }
        if (playerDeathTex != null) {
            int deathFrameCount = Math.max(1, playerDeathTex.getWidth() / (int) SPRITE_FRAME_WIDTH);
            playerDeathAnim = buildAnimation(
                    playerDeathTex,
                    deathFrameCount,
                    (int) SPRITE_FRAME_WIDTH,
                    (int) SPRITE_FRAME_HEIGHT,
                    PLAYER_DEATH_ANIMATION_SECONDS / deathFrameCount,
                    0,
                    1);
            playerDeathAnim.setPlayMode(Animation.PlayMode.NORMAL);
        } else {
            playerDeathAnim = null;
        }
        loadBatAssets();
        loadEvilWizardAssets();
        loadGolem1Assets();
        loadBossArenaPhase2DemonAssets();
        loadChestAssets();
    }

    private void loadNpcDialogueVoiceSfx() {
        fatherHappyDialogueSfx = loadOptionalAbsoluteSound(npcDialogueSoundPath("Father", "Father_Happy1.mp3"));
        fatherNeutralDialogueSfx[0] = loadOptionalAbsoluteSound(npcDialogueSoundPath("Father", "Father_Neutral1.mp3"));
        fatherNeutralDialogueSfx[1] = loadOptionalAbsoluteSound(npcDialogueSoundPath("Father", "Father_Neutral2.mp3"));
        fatherNeutralDialogueSfx[2] = loadOptionalAbsoluteSound(npcDialogueSoundPath("Father", "Father_Neutral3.mp3"));
        fatherSadDialogueSfx = loadOptionalAbsoluteSound(npcDialogueSoundPath("Father", "Father_Sad1.mp3"));

        bakeryNeutralDialogueSfx[0] = loadOptionalAbsoluteSound(npcDialogueSoundPath("Bakery", "Bakery_Neutral1.mp3"));
        bakeryNeutralDialogueSfx[1] = loadOptionalAbsoluteSound(npcDialogueSoundPath("Bakery", "Bakery_Neutral2.mp3"));
        bakeryNeutralDialogueSfx[2] = loadOptionalAbsoluteSound(npcDialogueSoundPath("Bakery", "Bakery_Neutral3.mp3"));
        bakeryWorryDialogueSfx = loadOptionalAbsoluteSound(npcDialogueSoundPath("Bakery", "Bakery_Worry1.mp3"));

        scientistConfuseDialogueSfx = loadOptionalAbsoluteSound(npcDialogueSoundPath("Scientist", "Scientist_Confuse1.mp3"));
        scientistNeutralDialogueSfx[0] = loadOptionalAbsoluteSound(npcDialogueSoundPath("Scientist", "Scientist_Neutral1.mp3"));
        scientistNeutralDialogueSfx[1] = loadOptionalAbsoluteSound(npcDialogueSoundPath("Scientist", "Scientist_Neutral2.mp3"));
        scientistNeutralDialogueSfx[2] = loadOptionalAbsoluteSound(npcDialogueSoundPath("Scientist", "Scientist_Neutral3.mp3"));
        scientistSadDialogueSfx = loadOptionalAbsoluteSound(npcDialogueSoundPath("Scientist", "Scientist_Sad1.mp3"));

        zombieDialogueSfx[0] = loadOptionalAbsoluteSound(npcDialogueSoundPath("Zombie", "Zombie_1.mp3"));
        zombieDialogueSfx[1] = loadOptionalAbsoluteSound(npcDialogueSoundPath("Zombie", "Zombie_2.mp3"));
    }

    private String npcDialogueSoundPath(String folderName, String fileName) {
        return NPC_SOUNDS_DIRECTORY_ABSOLUTE_PATH + File.separator + folderName + File.separator + fileName;
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

    private Texture loadOptionalTexture(String internalPath) {
        if (internalPath == null || internalPath.isBlank()) {
            return null;
        }
        FileHandle file = Gdx.files.internal(internalPath);
        if (!file.exists()) {
            return null;
        }
        try {
            return new Texture(file);
        } catch (RuntimeException error) {
            if (Gdx.app != null) {
                Gdx.app.error("PixelRpgGame", "Failed to load texture: " + internalPath, error);
            }
            return null;
        }
    }

    private Texture createScorchedFragmentTexture() {
        Pixmap pixmap = new Pixmap(96, 96, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.10f, 0.07f, 0.05f, 1f);
        pixmap.fill();

        pixmap.setColor(0.43f, 0.26f, 0.10f, 1f);
        for (int i = 0; i < 5; i++) {
            pixmap.drawRectangle(i, i, 96 - i * 2, 96 - i * 2);
        }

        pixmap.setColor(0.82f, 0.27f, 0.10f, 0.55f);
        pixmap.fillCircle(48, 44, 20);
        pixmap.setColor(0.95f, 0.54f, 0.18f, 0.40f);
        pixmap.fillCircle(48, 44, 12);

        pixmap.setColor(0.14f, 0.10f, 0.09f, 1f);
        pixmap.fillTriangle(48, 76, 26, 40, 42, 16);
        pixmap.fillTriangle(48, 76, 68, 40, 42, 16);
        pixmap.fillTriangle(42, 16, 68, 40, 56, 12);
        pixmap.fillTriangle(34, 44, 48, 76, 60, 50);

        pixmap.setColor(1.00f, 0.68f, 0.25f, 1f);
        pixmap.drawLine(48, 72, 54, 38);
        pixmap.drawLine(45, 54, 35, 34);
        pixmap.drawLine(50, 30, 58, 18);

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private Texture createArchiveNotesTexture() {
        Pixmap pixmap = new Pixmap(128, 128, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.14f, 0.09f, 0.05f, 1f);
        pixmap.fill();

        pixmap.setColor(0.84f, 0.74f, 0.57f, 1f);
        pixmap.fillRectangle(22, 16, 84, 96);
        pixmap.setColor(0.54f, 0.39f, 0.23f, 1f);
        pixmap.drawRectangle(22, 16, 84, 96);
        pixmap.drawRectangle(23, 17, 82, 94);

        pixmap.setColor(0.32f, 0.21f, 0.12f, 1f);
        for (int y = 0; y < 10; y++) {
            int lineY = 30 + y * 8;
            pixmap.drawLine(34, lineY, 94, lineY);
        }
        pixmap.setColor(0.63f, 0.18f, 0.10f, 1f);
        pixmap.fillTriangle(88, 16, 106, 16, 106, 34);
        pixmap.setColor(0.76f, 0.55f, 0.22f, 1f);
        pixmap.drawRectangle(18, 12, 92, 100);

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private void setNearestFilter(Texture texture) {
        if (texture != null) {
            texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        }
    }

    private BitmapFont loadQuestFont(String fontPath, int size) {
        FileHandle fontFile = Gdx.files.internal(fontPath);
        if (!fontFile.exists()) {
            return new BitmapFont();
        }
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(fontFile);
        try {
            FreeTypeFontGenerator.FreeTypeFontParameter parameter =
                    new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = size;
            parameter.magFilter = Texture.TextureFilter.Linear;
            parameter.minFilter = Texture.TextureFilter.Linear;
            return generator.generateFont(parameter);
        } finally {
            generator.dispose();
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

    private GameSettingsService settingsService() {
        return game != null ? game.getSettingsService() : null;
    }

    private long playSfx(Sound sound) {
        return playSfx(sound, 1f);
    }

    private long playSfx(Sound sound, float baseVolume) {
        GameSettingsService settingsService = settingsService();
        if (settingsService != null) {
            return settingsService.playSound(sound, baseVolume);
        }
        return sound != null ? sound.play(baseVolume) : -1L;
    }

    private void playCurrentQuestDialogueVoice() {
        stopActiveDialogueVoiceSfx();
        if (!firstQuestDialogueActive || firstQuestDialogueLineIndex < 0
                || firstQuestDialogueLineIndex >= activeQuestDialogueLines.length) {
            return;
        }
        String speaker = activeQuestDialogueSpeakers.length == 0
                ? ""
                : activeQuestDialogueSpeakers[Math.max(0,
                Math.min(activeQuestDialogueSpeakers.length - 1, firstQuestDialogueLineIndex))];
        String line = activeQuestDialogueLines[firstQuestDialogueLineIndex];
        NpcDialogueVoiceSet voiceSet = resolveNpcDialogueVoiceSet(speaker);
        Sound voiceSfx = resolveNpcDialogueVoiceSfx(voiceSet, speaker, line);
        if (voiceSfx == null) {
            return;
        }
        activeDialogueVoiceSfx = voiceSfx;
        activeDialogueVoiceSfxId = playNpcDialogueVoiceSfx(voiceSfx, voiceSet);
    }

    private void stopActiveDialogueVoiceSfx() {
        if (activeDialogueVoiceSfx != null && activeDialogueVoiceSfxId >= 0L) {
            activeDialogueVoiceSfx.stop(activeDialogueVoiceSfxId);
        }
        activeDialogueVoiceSfx = null;
        activeDialogueVoiceSfxId = -1L;
    }

    private Sound resolveNpcDialogueVoiceSfx(NpcDialogueVoiceSet voiceSet, String speaker, String line) {
        if (voiceSet == NpcDialogueVoiceSet.NONE) {
            return null;
        }
        String normalizedLine = line == null ? "" : line.trim().toLowerCase(Locale.ROOT);
        if (isFifthQuestVillagerAlarmDialogue(speaker)) {
            return firstNonNull(fifthQuestVillagerScreamSfx, resolveBakeryDialogueVoiceSfx(normalizedLine));
        }
        return switch (voiceSet) {
            case FATHER -> resolveFatherDialogueVoiceSfx(normalizedLine, speaker);
            case BAKERY -> resolveBakeryDialogueVoiceSfx(normalizedLine);
            case SCIENTIST -> resolveScientistDialogueVoiceSfx(normalizedLine);
            case DEMON -> nextDemonDialogueVoiceSfx();
            case ZOMBIE -> nextZombieDialogueVoiceSfx();
            default -> null;
        };
    }

    private boolean isFifthQuestVillagerAlarmDialogue(String speaker) {
        return activeQuestDialogueId == QuestDialogueId.FIFTH_QUEST_LAB_ALARM
                && speaker != null
                && "Villager".equals(speaker.trim());
    }

    private boolean isSixthQuestFirstDemonInteractionDialogue(String speaker) {
        return activeQuestDialogueId == QuestDialogueId.SIXTH_QUEST_CASTLE_ARRIVAL
                && speaker != null
                && "Demon".equals(speaker.trim());
    }

    private NpcDialogueVoiceSet resolveNpcDialogueVoiceSet(String speaker) {
        if (speaker == null || speaker.isBlank()) {
            return NpcDialogueVoiceSet.NONE;
        }
        return switch (speaker.trim()) {
            case "Father", "Old Man" -> NpcDialogueVoiceSet.FATHER;
            case "Bakery Owner", "Villager" -> NpcDialogueVoiceSet.BAKERY;
            case "Scientist" -> NpcDialogueVoiceSet.SCIENTIST;
            case "Demon" -> NpcDialogueVoiceSet.DEMON;
            default -> NpcDialogueVoiceSet.NONE;
        };
    }

    private long playNpcDialogueVoiceSfx(Sound sound, NpcDialogueVoiceSet voiceSet) {
        if (sound == null) {
            return -1L;
        }
        return sound.play(npcDialogueVoiceVolume(voiceSet, sound));
    }

    private float npcDialogueVoiceVolume(NpcDialogueVoiceSet voiceSet, Sound sound) {
        float multiplier = npcDialogueVoiceVolumeMultiplier(voiceSet, sound);
        GameSettingsService settingsService = settingsService();
        if (settingsService != null) {
            return Math.max(0f, settingsService.scaleSfxVolume(NPC_DIALOGUE_SFX_BASE_VOLUME) * multiplier);
        }
        return Math.max(0f, NPC_DIALOGUE_SFX_BASE_VOLUME * multiplier);
    }

    private float npcDialogueVoiceVolumeMultiplier(NpcDialogueVoiceSet voiceSet, Sound sound) {
        if (sound != null && sound == fifthQuestVillagerScreamSfx) {
            return 1f;
        }
        return switch (voiceSet) {
            case BAKERY -> BAKERY_NPC_DIALOGUE_SFX_VOLUME_MULTIPLIER;
            case SCIENTIST -> scientistDialogueVoiceVolumeMultiplier(sound);
            default -> 1f;
        };
    }

    private float scientistDialogueVoiceVolumeMultiplier(Sound sound) {
        if (sound != null && sound == scientistNeutralDialogueSfx[1]) {
            return SCIENTIST_NEUTRAL_2_NPC_DIALOGUE_SFX_VOLUME_MULTIPLIER;
        }
        return SCIENTIST_NPC_DIALOGUE_SFX_VOLUME_MULTIPLIER;
    }

    private Sound resolveFatherDialogueVoiceSfx(String normalizedLine, String speaker) {
        if ("Old Man".equals(speaker)) {
            return firstNonNull(fatherSadDialogueSfx, nextFatherNeutralDialogueVoiceSfx());
        }
        if (containsAny(normalizedLine,
                "good",
                "come at me",
                "then go",
                "you came back",
                "that is enough",
                "i will not stop you",
                "i understand")) {
            return firstNonNull(fatherHappyDialogueSfx, nextFatherNeutralDialogueVoiceSfx());
        }
        if (containsAny(normalizedLine,
                "afraid",
                "fear",
                "dead",
                "graveyard",
                "ashes",
                "destroyed",
                "danger",
                "rest",
                "over",
                "alone")) {
            return firstNonNull(fatherSadDialogueSfx, nextFatherNeutralDialogueVoiceSfx());
        }
        return nextFatherNeutralDialogueVoiceSfx();
    }

    private Sound resolveBakeryDialogueVoiceSfx(String normalizedLine) {
        if (containsAny(normalizedLine,
                "!",
                "wrong",
                "burn",
                "burns",
                "burned",
                "strange",
                "stay back",
                "too close",
                "creatures",
                "portal",
                "alive",
                "destroy us")) {
            return firstNonNull(bakeryWorryDialogueSfx, nextBakeryNeutralDialogueVoiceSfx());
        }
        return nextBakeryNeutralDialogueVoiceSfx();
    }

    private Sound resolveScientistDialogueVoiceSfx(String normalizedLine) {
        if (containsAny(normalizedLine,
                "?",
                "...",
                "cannot prove",
                "what do they mean",
                "may",
                "perhaps",
                "confirm",
                "pattern")) {
            return firstNonNull(scientistConfuseDialogueSfx, nextScientistNeutralDialogueVoiceSfx());
        }
        if (containsAny(normalizedLine,
                "worse",
                "destroyed",
                "demon",
                "battlefield",
                "graveyard",
                "collapse",
                "dangerous",
                "corruption")) {
            return firstNonNull(scientistSadDialogueSfx, nextScientistNeutralDialogueVoiceSfx());
        }
        return nextScientistNeutralDialogueVoiceSfx();
    }

    private Sound nextFatherNeutralDialogueVoiceSfx() {
        Sound sound = nextVoiceFromArray(fatherNeutralDialogueSfx, fatherNeutralDialogueSfxIndex);
        fatherNeutralDialogueSfxIndex = nextVoiceIndex(fatherNeutralDialogueSfx, fatherNeutralDialogueSfxIndex);
        return firstNonNull(sound, fatherHappyDialogueSfx, fatherSadDialogueSfx);
    }

    private Sound nextBakeryNeutralDialogueVoiceSfx() {
        Sound sound = nextVoiceFromArray(bakeryNeutralDialogueSfx, bakeryNeutralDialogueSfxIndex);
        bakeryNeutralDialogueSfxIndex = nextVoiceIndex(bakeryNeutralDialogueSfx, bakeryNeutralDialogueSfxIndex);
        return firstNonNull(sound, bakeryWorryDialogueSfx);
    }

    private Sound nextScientistNeutralDialogueVoiceSfx() {
        Sound sound = nextVoiceFromArray(scientistNeutralDialogueSfx, scientistNeutralDialogueSfxIndex);
        scientistNeutralDialogueSfxIndex = nextVoiceIndex(scientistNeutralDialogueSfx, scientistNeutralDialogueSfxIndex);
        return firstNonNull(sound, scientistConfuseDialogueSfx, scientistSadDialogueSfx);
    }

    private Sound nextDemonDialogueVoiceSfx() {
        Sound primary = demonDialogueSfxIndex % 2 == 0 ? demonFirstInteractionSfx : demonNeutral2Sfx;
        Sound secondary = demonDialogueSfxIndex % 2 == 0 ? demonNeutral2Sfx : demonFirstInteractionSfx;
        demonDialogueSfxIndex = (demonDialogueSfxIndex + 1) % 2;
        return firstNonNull(primary, secondary, nextZombieDialogueVoiceSfx());
    }

    private Sound nextZombieDialogueVoiceSfx() {
        Sound sound = nextVoiceFromArray(zombieDialogueSfx, zombieDialogueSfxIndex);
        zombieDialogueSfxIndex = nextVoiceIndex(zombieDialogueSfx, zombieDialogueSfxIndex);
        return sound;
    }

    private void playFifthQuestZombieSfx(float volumeMultiplier) {
        if (!MAP_ID_VILLAGE.equals(world.activeMapId)
                || fifthQuestStage <= FIFTH_QUEST_DISABLED
                || fifthQuestStage >= FIFTH_QUEST_COMPLETE
                || fifthQuestZombieSfxCooldownTimer > 0f) {
            return;
        }
        Sound sound = nextZombieDialogueVoiceSfx();
        if (sound == null) {
            return;
        }
        float volume = npcDialogueVoiceVolume(NpcDialogueVoiceSet.ZOMBIE, sound) * Math.max(0f, volumeMultiplier);
        sound.play(volume);
        fifthQuestZombieSfxCooldownTimer = FIFTH_QUEST_ZOMBIE_SFX_COOLDOWN_SECONDS;
    }

    private Sound nextVoiceFromArray(Sound[] sounds, int startIndex) {
        if (sounds == null || sounds.length == 0) {
            return null;
        }
        int safeIndex = Math.max(0, startIndex);
        for (int offset = 0; offset < sounds.length; offset++) {
            Sound sound = sounds[(safeIndex + offset) % sounds.length];
            if (sound != null) {
                return sound;
            }
        }
        return null;
    }

    private int nextVoiceIndex(Sound[] sounds, int currentIndex) {
        if (sounds == null || sounds.length == 0) {
            return 0;
        }
        return (Math.max(0, currentIndex) + 1) % sounds.length;
    }

    private Sound firstNonNull(Sound... sounds) {
        if (sounds == null) {
            return null;
        }
        for (Sound sound : sounds) {
            if (sound != null) {
                return sound;
            }
        }
        return null;
    }

    private boolean containsAny(String text, String... snippets) {
        if (text == null || snippets == null) {
            return false;
        }
        for (String snippet : snippets) {
            if (snippet != null && !snippet.isEmpty() && text.contains(snippet)) {
                return true;
            }
        }
        return false;
    }

    private void resetNpcDialogueVoiceSequence() {
        fatherNeutralDialogueSfxIndex = 0;
        bakeryNeutralDialogueSfxIndex = 0;
        scientistNeutralDialogueSfxIndex = 0;
        demonDialogueSfxIndex = 0;
        zombieDialogueSfxIndex = 0;
    }

    private void disposeNpcDialogueVoiceSfx() {
        disposeSoundArray(fatherNeutralDialogueSfx);
        disposeSoundArray(bakeryNeutralDialogueSfx);
        disposeSoundArray(scientistNeutralDialogueSfx);
        disposeSoundArray(zombieDialogueSfx);
        if (fatherHappyDialogueSfx != null) fatherHappyDialogueSfx.dispose();
        if (fatherSadDialogueSfx != null) fatherSadDialogueSfx.dispose();
        if (bakeryWorryDialogueSfx != null) bakeryWorryDialogueSfx.dispose();
        if (scientistConfuseDialogueSfx != null) scientistConfuseDialogueSfx.dispose();
        if (scientistSadDialogueSfx != null) scientistSadDialogueSfx.dispose();
        if (fifthQuestVillagerScreamSfx != null) fifthQuestVillagerScreamSfx.dispose();
        if (demonFirstInteractionSfx != null) demonFirstInteractionSfx.dispose();
        if (demonNeutral2Sfx != null) demonNeutral2Sfx.dispose();
    }

    private void disposeSoundArray(Sound[] sounds) {
        if (sounds == null) {
            return;
        }
        for (Sound sound : sounds) {
            if (sound != null) {
                sound.dispose();
            }
        }
    }

    private void updateIndoorFootstepLoop() {
        if (!shouldPlayIndoorFootstepLoop()) {
            stopIndoorFootstepLoop();
            return;
        }
        float volume = indoorFootstepLoopVolume();
        if (indoorFootstepLoopId < 0L) {
            indoorFootstepLoopId = indoorFootstepLoopSfx.loop(volume);
        } else {
            indoorFootstepLoopSfx.setVolume(indoorFootstepLoopId, volume);
        }
    }

    private void stopIndoorFootstepLoop() {
        if (indoorFootstepLoopSfx != null && indoorFootstepLoopId >= 0L) {
            indoorFootstepLoopSfx.stop(indoorFootstepLoopId);
            indoorFootstepLoopId = -1L;
        }
    }

    private boolean shouldPlayIndoorFootstepLoop() {
        return indoorFootstepLoopSfx != null
                && player != null
                && player.moving
                && world != null
                && isIndoorFootstepMap(world.activeMapId);
    }

    private boolean isIndoorFootstepMap(String mapId) {
        return MAP_ID_INN.equals(mapId)
                || MAP_ID_INN_ROOM.equals(mapId)
                || MAP_ID_RESEARCH_LAB_FLOOR_1.equals(mapId)
                || MAP_ID_RESEARCH_LAB_FLOOR_2.equals(mapId);
    }

    private float indoorFootstepLoopVolume() {
        GameSettingsService settingsService = settingsService();
        if (settingsService != null) {
            return settingsService.scaleSfxVolume(INDOOR_FOOTSTEP_LOOP_BASE_VOLUME);
        }
        return INDOOR_FOOTSTEP_LOOP_BASE_VOLUME;
    }

    private void playMusicFromStart(Music music, float baseVolume, boolean looping) {
        GameSettingsService settingsService = settingsService();
        if (settingsService != null) {
            settingsService.playMusicFromStart(music, baseVolume, looping);
            return;
        }
        if (music != null) {
            music.stop();
            music.setLooping(looping);
            music.setPosition(0f);
            music.setVolume(baseVolume);
            music.play();
        }
    }

    private void applyRuntimeAudioSettings() {
        GameSettingsService settingsService = settingsService();
        if (settingsService == null) {
            if (villageMusic != null && villageMusicPlaying) {
                villageMusic.setVolume(VILLAGE_MUSIC_BASE_VOLUME * clamp01(villageMusicFadeProgress));
            }
        if (sadVillageMusic != null && sadVillageMusicPlaying) {
            sadVillageMusic.setVolume(VILLAGE_MUSIC_BASE_VOLUME * clamp01(villageMusicFadeProgress));
        }
        if (villageCombatMusic != null && villageCombatMusicPlaying) {
            villageCombatMusic.setVolume(VILLAGE_COMBAT_MUSIC_BASE_VOLUME);
        }
        if (castleMusic != null && castleMusicPlaying) {
            castleMusic.setVolume(CASTLE_MUSIC_BASE_VOLUME);
        }
        if (demonBattleMusic != null && demonBattleMusicPlaying) {
            demonBattleMusic.setVolume(DEMON_BATTLE_MUSIC_BASE_VOLUME * clamp01(demonBattleMusicFadeProgress));
        }
            if (sixthQuestEndingMusic != null && sixthQuestEndingMusicPlaying) {
                sixthQuestEndingMusic.setVolume(SIXTH_QUEST_ENDING_MUSIC_BASE_VOLUME * clamp01(sixthQuestEndingMusicFadeProgress));
            }
            return;
        }
        if (villageMusic != null && villageMusicPlaying) {
            villageMusic.setVolume(settingsService.scaleMusicVolume(VILLAGE_MUSIC_BASE_VOLUME)
                    * clamp01(villageMusicFadeProgress));
        }
        if (sadVillageMusic != null && sadVillageMusicPlaying) {
            settingsService.applyMusicVolume(
                    sadVillageMusic,
                    VILLAGE_MUSIC_BASE_VOLUME * clamp01(villageMusicFadeProgress));
        }
        if (villageCombatMusic != null && villageCombatMusicPlaying) {
            settingsService.applyMusicVolume(villageCombatMusic, VILLAGE_COMBAT_MUSIC_BASE_VOLUME);
        }
        if (castleMusic != null && castleMusicPlaying) {
            settingsService.applyMusicVolume(castleMusic, CASTLE_MUSIC_BASE_VOLUME);
        }
        if (demonBattleMusic != null && demonBattleMusicPlaying) {
            settingsService.applyMusicVolume(
                    demonBattleMusic,
                    DEMON_BATTLE_MUSIC_BASE_VOLUME * clamp01(demonBattleMusicFadeProgress));
        }
        if (sixthQuestEndingMusic != null && sixthQuestEndingMusicPlaying) {
            settingsService.applyMusicVolume(
                    sixthQuestEndingMusic,
                    SIXTH_QUEST_ENDING_MUSIC_BASE_VOLUME * clamp01(sixthQuestEndingMusicFadeProgress));
        }
        settingsService.applyMusicVolume(evilWizardPreExplosionSfx, EVIL_WIZARD_PRE_EXPLOSION_SFX_VOLUME);
    }

    private void updateVillageMusic(float delta) {
        boolean endingSequenceMusicActive = sixthQuestStage >= SIXTH_QUEST_ENDING
                || endingMusicTransitionPhase != EndingMusicTransitionPhase.NONE
                || sixthQuestEndingMusicPlaying
                || sixthQuestEndingVideoSequenceActive;
        boolean shouldPlayVillageMusic = MAP_ID_VILLAGE.equals(world.activeMapId) && !endingSequenceMusicActive;
        if (!shouldPlayVillageMusic) {
            stopVillageMusic();
            stopSadVillageMusic();
            return;
        }
        boolean useSadVillageMusic = shouldUseSadVillageMusic() && sadVillageMusic != null;
        Music activeVillageAmbientMusic = useSadVillageMusic ? sadVillageMusic : villageMusic;
        if (activeVillageAmbientMusic == null) {
            return;
        }
        if (useSadVillageMusic) {
            if (villageMusicPlaying) {
                stopVillageMusic();
            }
            if (!sadVillageMusicPlaying || !sadVillageMusic.isPlaying()) {
                startSadVillageMusic();
            }
        } else {
            if (sadVillageMusicPlaying) {
                stopSadVillageMusic();
            }
            if (!villageMusicPlaying || !villageMusic.isPlaying()) {
                startVillageMusic();
            }
        }
        boolean combatMusicActive = shouldPlayVillageCombatMusic();
        if (combatMusicActive) {
            villageMusicSuppressedForCombat = true;
        }
        float targetFadeProgress = combatMusicActive ? 0f : 1f;
        float fadeSeconds = targetFadeProgress < villageMusicFadeProgress
                ? VILLAGE_MUSIC_FADE_OUT_TO_COMBAT_SECONDS
                : (villageMusicSuppressedForCombat
                ? VILLAGE_MUSIC_RETURN_FROM_COMBAT_FADE_IN_SECONDS
                : VILLAGE_MUSIC_FADE_IN_SECONDS);
        if (fadeSeconds <= 0f) {
            villageMusicFadeProgress = targetFadeProgress;
        } else if (targetFadeProgress > villageMusicFadeProgress) {
            villageMusicFadeProgress = Math.min(
                    targetFadeProgress,
                    villageMusicFadeProgress + Math.max(0f, delta) / fadeSeconds);
        } else {
            villageMusicFadeProgress = Math.max(
                    targetFadeProgress,
                    villageMusicFadeProgress - Math.max(0f, delta) / fadeSeconds);
        }
        if (!combatMusicActive && villageMusicSuppressedForCombat && villageMusicFadeProgress >= 1f) {
            villageMusicSuppressedForCombat = false;
        }
    }

    private void updateCastleMusic() {
        boolean shouldPlayCastleMusic = MAP_ID_DEMON_CASTLE.equals(world.activeMapId)
                && sixthQuestStage < SIXTH_QUEST_ENDING
                && !gameCompleted;
        if (!shouldPlayCastleMusic) {
            stopCastleMusic();
            return;
        }
        if (castleMusic == null) {
            return;
        }
        if (!castleMusicPlaying || !castleMusic.isPlaying()) {
            startCastleMusic();
        }
    }

    private void startVillageMusic() {
        if (villageMusic == null) {
            return;
        }
        villageMusic.stop();
        villageMusic.setLooping(true);
        villageMusic.setPosition(0f);
        villageMusic.setVolume(0f);
        villageMusic.play();
        villageMusicFadeProgress = 0f;
        villageMusicPlaying = true;
    }

    private void stopVillageMusic() {
        villageMusicFadeProgress = 0f;
        villageMusicSuppressedForCombat = false;
        if (villageMusic != null) {
            villageMusic.stop();
        }
        villageMusicPlaying = false;
    }

    private boolean shouldUseSadVillageMusic() {
        return fifthQuestStage > FIFTH_QUEST_DISABLED && fifthQuestStage < FIFTH_QUEST_COMPLETE;
    }

    private void startSadVillageMusic() {
        if (sadVillageMusic == null) {
            return;
        }
        sadVillageMusic.stop();
        sadVillageMusic.setLooping(true);
        sadVillageMusic.setPosition(0f);
        sadVillageMusic.setVolume(0f);
        sadVillageMusic.play();
        villageMusicFadeProgress = 0f;
        sadVillageMusicPlaying = true;
    }

    private void stopSadVillageMusic() {
        if (sadVillageMusic != null) {
            sadVillageMusic.stop();
        }
        sadVillageMusicPlaying = false;
    }

    private void startCastleMusic() {
        if (castleMusic == null) {
            return;
        }
        playMusicFromStart(castleMusic, CASTLE_MUSIC_BASE_VOLUME, true);
        castleMusicPlaying = true;
    }

    private void stopCastleMusic() {
        if (castleMusic != null) {
            castleMusic.stop();
        }
        castleMusicPlaying = false;
    }

    private void updateVillageCombatMusic() {
        if (!shouldPlayVillageCombatMusic()) {
            stopVillageCombatMusic();
            return;
        }
        if (villageCombatMusic == null) {
            return;
        }
        if (!villageCombatMusicPlaying || !villageCombatMusic.isPlaying()) {
            startVillageCombatMusic();
        }
    }

    private boolean shouldPlayVillageCombatMusic() {
        if (villageCombatMusic == null || gameCompleted || !MAP_ID_VILLAGE.equals(world.activeMapId)) {
            return false;
        }
        return isThirdQuestVillageCombatMusicActive()
                || isFourthQuestVillageCombatMusicActive()
                || isFifthQuestVillageCombatMusicActive();
    }

    private boolean isThirdQuestVillageCombatMusicActive() {
        return thirdQuestStage == THIRD_QUEST_COMBAT_TUTORIAL
                || (thirdQuestStage == THIRD_QUEST_SPAR_WITH_FATHER && thirdQuestSparActive);
    }

    private boolean isFourthQuestVillageCombatMusicActive() {
        return fourthQuestStage == FOURTH_QUEST_CAFE_BATTLE
                || fourthQuestStage == FOURTH_QUEST_CENTER_BATTLE
                || fourthQuestStage == FOURTH_QUEST_EDGE_BATTLE
                || fourthQuestStage == FOURTH_QUEST_BREACH_BATTLE;
    }

    private boolean isFifthQuestVillageCombatMusicActive() {
        return switch (fifthQuestStage) {
            case FIFTH_QUEST_OUTER_RUINS_BATTLE,
                    FIFTH_QUEST_MID_RUINS_BATTLE,
                    FIFTH_QUEST_ANCHOR_1_BATTLE,
                    FIFTH_QUEST_ANCHOR_2_BATTLE,
                    FIFTH_QUEST_ANCHOR_3_BATTLE,
                    FIFTH_QUEST_PORTAL_BOSS -> true;
            default -> false;
        };
    }

    private void startVillageCombatMusic() {
        if (villageCombatMusic == null) {
            return;
        }
        playMusicFromStart(villageCombatMusic, VILLAGE_COMBAT_MUSIC_BASE_VOLUME, true);
        villageCombatMusicPlaying = true;
    }

    private void stopVillageCombatMusic() {
        if (villageCombatMusic != null) {
            villageCombatMusic.stop();
        }
        villageCombatMusicPlaying = false;
    }

    private void updateDemonBattleMusic() {
        if (endingMusicTransitionPhase == EndingMusicTransitionPhase.FADING_OUT_BATTLE) {
            return;
        }
        if (!shouldPlayDemonBattleMusic()) {
            stopDemonBattleMusic();
            return;
        }
        if (demonBattleMusic == null) {
            return;
        }
        if (!demonBattleMusicPlaying || !demonBattleMusic.isPlaying()) {
            startDemonBattleMusic();
        }
    }

    private boolean shouldPlayDemonBattleMusic() {
        if (gameCompleted || sixthQuestStage >= SIXTH_QUEST_ENDING) {
            return false;
        }
        if (!MAP_ID_BOSS_ARENA_PHASE_2.equals(world.activeMapId)) {
            return false;
        }
        return bossArenaFinalIntroPhase == BossArenaFinalIntroPhase.FOCUS_BOSS
                || activeQuestDialogueId == QuestDialogueId.SIXTH_QUEST_FINAL_PHASE_INTRO
                || sixthQuestStage >= SIXTH_QUEST_FINAL_PHASE_BATTLE;
    }

    private void startDemonBattleMusic() {
        if (demonBattleMusic == null) {
            return;
        }
        demonBattleMusic.stop();
        demonBattleMusic.setLooping(true);
        demonBattleMusic.setPosition(0f);
        GameSettingsService settingsService = settingsService();
        if (settingsService != null) {
            demonBattleMusic.setVolume(settingsService.scaleMusicVolume(DEMON_BATTLE_MUSIC_BASE_VOLUME));
        } else {
            demonBattleMusic.setVolume(DEMON_BATTLE_MUSIC_BASE_VOLUME);
        }
        demonBattleMusic.play();
        demonBattleMusicFadeProgress = 1f;
        demonBattleMusicPlaying = true;
    }

    private void stopDemonBattleMusic() {
        if (demonBattleMusic != null) {
            demonBattleMusic.stop();
        }
        demonBattleMusicFadeProgress = 0f;
        demonBattleMusicPlaying = false;
    }

    private void updateSixthQuestEndingMusic(float delta) {
        if (sixthQuestStage < SIXTH_QUEST_ENDING && !gameCompleted) {
            endingMusicTransitionPhase = EndingMusicTransitionPhase.NONE;
            sixthQuestEndingMusicFadeProgress = 0f;
            stopSixthQuestEndingMusic();
            return;
        }

        float safeDelta = Math.max(0f, delta);
        switch (endingMusicTransitionPhase) {
            case FADING_OUT_BATTLE -> {
                if (SIXTH_QUEST_ENDING_MUSIC_FADE_OUT_SECONDS <= 0f) {
                    demonBattleMusicFadeProgress = 0f;
                } else {
                    demonBattleMusicFadeProgress = Math.max(
                            0f,
                            demonBattleMusicFadeProgress - safeDelta / SIXTH_QUEST_ENDING_MUSIC_FADE_OUT_SECONDS);
                }
                if (demonBattleMusicFadeProgress <= 0f) {
                    stopDemonBattleMusic();
                    startSixthQuestEndingMusic();
                    endingMusicTransitionPhase = EndingMusicTransitionPhase.FADING_IN_ENDING;
                }
            }
            case FADING_IN_ENDING -> {
                if (sixthQuestEndingMusic == null) {
                    endingMusicTransitionPhase = EndingMusicTransitionPhase.NONE;
                    return;
                }
                if (!sixthQuestEndingMusicPlaying || !sixthQuestEndingMusic.isPlaying()) {
                    startSixthQuestEndingMusic();
                }
                if (SIXTH_QUEST_ENDING_MUSIC_FADE_IN_SECONDS <= 0f) {
                    sixthQuestEndingMusicFadeProgress = 1f;
                } else {
                    sixthQuestEndingMusicFadeProgress = Math.min(
                            1f,
                            sixthQuestEndingMusicFadeProgress + safeDelta / SIXTH_QUEST_ENDING_MUSIC_FADE_IN_SECONDS);
                }
                if (sixthQuestEndingMusicFadeProgress >= 1f) {
                    endingMusicTransitionPhase = EndingMusicTransitionPhase.NONE;
                }
            }
            case NONE -> {
                if (sixthQuestEndingMusicPlaying && sixthQuestEndingMusic != null && !sixthQuestEndingMusic.isPlaying()) {
                    startSixthQuestEndingMusic();
                    sixthQuestEndingMusicFadeProgress = 1f;
                }
            }
        }
    }

    private void triggerSixthQuestEndingMusicTransition() {
        if (sixthQuestEndingMusic == null) {
            stopDemonBattleMusic();
            return;
        }
        if (demonBattleMusicPlaying && demonBattleMusic != null && demonBattleMusic.isPlaying()) {
            endingMusicTransitionPhase = EndingMusicTransitionPhase.FADING_OUT_BATTLE;
            demonBattleMusicFadeProgress = Math.max(0.001f, demonBattleMusicFadeProgress);
            sixthQuestEndingMusicFadeProgress = 0f;
            stopSixthQuestEndingMusic();
            return;
        }
        stopDemonBattleMusic();
        startSixthQuestEndingMusic();
        endingMusicTransitionPhase = EndingMusicTransitionPhase.FADING_IN_ENDING;
    }

    private void startSixthQuestEndingMusic() {
        if (sixthQuestEndingMusic == null) {
            return;
        }
        sixthQuestEndingMusic.stop();
        sixthQuestEndingMusic.setLooping(true);
        sixthQuestEndingMusic.setPosition(0f);
        sixthQuestEndingMusic.setVolume(0f);
        sixthQuestEndingMusic.play();
        sixthQuestEndingMusicFadeProgress = 0f;
        sixthQuestEndingMusicPlaying = true;
    }

    private void stopSixthQuestEndingMusic() {
        if (sixthQuestEndingMusic != null) {
            sixthQuestEndingMusic.stop();
        }
        sixthQuestEndingMusicFadeProgress = 0f;
        sixthQuestEndingMusicPlaying = false;
    }

    private Sound loadOptionalAbsoluteSound(String absolutePath) {
        if (absolutePath == null || absolutePath.isBlank()) {
            return null;
        }
        FileHandle file = Gdx.files.absolute(absolutePath);
        if (file == null || !file.exists()) {
            if (Gdx.app != null) {
                Gdx.app.log("PixelRpgGame", "Optional sound not found: " + absolutePath);
            }
            return null;
        }
        try {
            return Gdx.audio.newSound(file);
        } catch (RuntimeException error) {
            if (Gdx.app != null) {
                Gdx.app.error("PixelRpgGame", "Failed to load sound: " + absolutePath, error);
            }
            return null;
        }
    }

    private Sound loadOptionalInternalSound(String internalPath) {
        if (internalPath == null || internalPath.isBlank()) {
            return null;
        }
        try {
            FileHandle file = Gdx.files.internal(internalPath);
            if (file == null || !file.exists()) {
                return null;
            }
            return Gdx.audio.newSound(file);
        } catch (RuntimeException error) {
            if (Gdx.app != null) {
                Gdx.app.error("PixelRpgGame", "Failed to load sound: " + internalPath, error);
            }
            return null;
        }
    }

    private Music loadOptionalMusic(String internalPath) {
        if (internalPath == null || internalPath.isBlank()) {
            return null;
        }
        try {
            FileHandle file = Gdx.files.internal(internalPath);
            if (file == null || !file.exists()) {
                return null;
            }
            return Gdx.audio.newMusic(file);
        } catch (RuntimeException error) {
            if (Gdx.app != null) {
                Gdx.app.error("PixelRpgGame", "Failed to load music: " + internalPath, error);
            }
            return null;
        }
    }

    private Music loadOptionalAbsoluteMusic(String absolutePath) {
        if (absolutePath == null || absolutePath.isBlank()) {
            return null;
        }
        FileHandle file = Gdx.files.absolute(absolutePath);
        if (file == null || !file.exists()) {
            if (Gdx.app != null) {
                Gdx.app.log("PixelRpgGame", "Optional music not found: " + absolutePath);
            }
            return null;
        }
        try {
            return Gdx.audio.newMusic(file);
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

    private Texture loadPlayerDeathTexture() {
        RuntimeException absoluteError = null;
        try {
            return new Texture(Gdx.files.absolute(PLAYER_DEATH_TEXTURE_ABSOLUTE_PATH));
        } catch (RuntimeException error) {
            absoluteError = error;
        }

        try {
            return new Texture(Gdx.files.internal(PLAYER_DEATH_TEXTURE_INTERNAL_PATH));
        } catch (RuntimeException internalError) {
            if (Gdx.app != null) {
                Gdx.app.error(
                        "PixelRpgGame",
                        "Failed to load player death texture from absolute path '"
                                + PLAYER_DEATH_TEXTURE_ABSOLUTE_PATH
                                + "' and fallback internal path '"
                                + PLAYER_DEATH_TEXTURE_INTERNAL_PATH + "'.",
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
        bossArenaPhase2SkillStartAnim = loadOptionalAbsoluteFrameAnimation(
                BOSS_ARENA_PHASE_2_SKILL_START_FRAMES_DIR_ABSOLUTE_PATH,
                BOSS_ARENA_PHASE_2_SKILL_START_FRAME_DURATION,
                bossArenaPhase2SkillStartFrameTextures);
        bossArenaPhase2SkillLoopAnim = loadOptionalAbsoluteFrameAnimation(
                BOSS_ARENA_PHASE_2_SKILL_LOOP_FRAMES_DIR_ABSOLUTE_PATH,
                BOSS_ARENA_PHASE_2_SKILL_LOOP_FRAME_DURATION,
                bossArenaPhase2SkillLoopFrameTextures);
        bossArenaPhase2SkillEndAnim = loadOptionalAbsoluteFrameAnimation(
                BOSS_ARENA_PHASE_2_SKILL_END_FRAMES_DIR_ABSOLUTE_PATH,
                BOSS_ARENA_PHASE_2_SKILL_END_FRAME_DURATION,
                bossArenaPhase2SkillEndFrameTextures);
        if (golem1ExplosionTex != null) {
            bossArenaPhase2ExplosionAnim = buildRawAnimation(
                    golem1ExplosionTex,
                    GOLEM_1_EXPLOSION_FRAMES,
                    GOLEM_1_EXPLOSION_FRAME_WIDTH,
                    GOLEM_1_EXPLOSION_FRAME_HEIGHT,
                    BOSS_ARENA_PHASE_2_EXPLOSION_FRAME_DURATION);
        } else {
            bossArenaPhase2ExplosionAnim = loadOptionalAbsoluteFrameAnimation(
                    BOSS_ARENA_PHASE_2_EXPLOSION_FRAMES_DIR_ABSOLUTE_PATH,
                    BOSS_ARENA_PHASE_2_EXPLOSION_FRAME_DURATION,
                    bossArenaPhase2ExplosionFrameTextures);
        }
        if (bossArenaPhase2DemonAttackAnim != null) {
            bossArenaPhase2DemonAttackAnim.setPlayMode(Animation.PlayMode.NORMAL);
        }
        if (bossArenaPhase2DemonHurtAnim != null) {
            bossArenaPhase2DemonHurtAnim.setPlayMode(Animation.PlayMode.NORMAL);
        }
        if (bossArenaPhase2SkillStartAnim != null) {
            bossArenaPhase2SkillStartAnim.setPlayMode(Animation.PlayMode.NORMAL);
        }
        if (bossArenaPhase2SkillLoopAnim != null) {
            bossArenaPhase2SkillLoopAnim.setPlayMode(Animation.PlayMode.LOOP);
        }
        if (bossArenaPhase2SkillEndAnim != null) {
            bossArenaPhase2SkillEndAnim.setPlayMode(Animation.PlayMode.NORMAL);
        }
        if (bossArenaPhase2ExplosionAnim != null) {
            bossArenaPhase2ExplosionAnim.setPlayMode(Animation.PlayMode.NORMAL);
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

    private Texture createCooldownCircleTexture(int size) {
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        float center = size * 0.5f;
        float maxDist = center - 1f;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = x + 0.5f - center;
                float dy = y + 0.5f - center;
                float dist = (float) Math.sqrt(dx * dx + dy * dy) / Math.max(1f, maxDist);
                float alpha;
                if (dist >= 1f) {
                    alpha = 0f;
                } else if (dist <= 0.78f) {
                    alpha = 1f;
                } else {
                    alpha = 1f - smoothStep((dist - 0.78f) / 0.22f);
                }
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

    private void drawScreenFadeOverlay() {
        float overlayAlpha = Math.max(screenFadeAlpha, sixthQuestEndingVideoFadeAlpha());
        if (overlayAlpha <= 0f || dayNightOverlayTex == null || uiCamera == null) {
            return;
        }
        uiCamera.update();
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        batch.setShader(null);
        batch.setColor(0f, 0f, 0f, overlayAlpha);
        batch.draw(dayNightOverlayTex, 0f, 0f, uiCamera.viewportWidth, uiCamera.viewportHeight);
        batch.setColor(1f, 1f, 1f, 1f);
        batch.end();
    }

    private float sixthQuestEndingVideoFadeAlpha() {
        if (!sixthQuestEndingVideoSequenceActive || sixthQuestEndingVideoTriggered) {
            return 0f;
        }
        if (sixthQuestEndingVideoSequenceTimer <= SIXTH_QUEST_ENDING_VIDEO_DELAY_SECONDS) {
            return 0f;
        }
        if (SIXTH_QUEST_ENDING_VIDEO_FADE_SECONDS <= 0f) {
            return 1f;
        }
        return clamp01(
                (sixthQuestEndingVideoSequenceTimer - SIXTH_QUEST_ENDING_VIDEO_DELAY_SECONDS)
                        / SIXTH_QUEST_ENDING_VIDEO_FADE_SECONDS);
    }

    private void drawBossArenaTopLetterboxOverlay() {
        if (hudRenderer == null
                || !MAP_ID_BOSS_ARENA_PHASE_2.equals(world.activeMapId)
                || !isBossArenaFinalIntroFocusActive()) {
            return;
        }
        hudRenderer.drawLetterboxOverlay(batch);

        float nightBlend = currentNightBlend();
        float characterSaturation = lerp(CHARACTER_SATURATION, NIGHT_CHARACTER_SATURATION, nightBlend);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setShader(null);
        drawPlayer(characterSaturation);
        batch.end();
    }

    private float currentBossArenaHudHealth() {
        if (!MAP_ID_BOSS_ARENA_PHASE_2.equals(world.activeMapId)) {
            return -1f;
        }
        BossArenaPhase2DemonInstance boss = findBossArenaPhase2Leader();
        return boss != null ? boss.health : -1f;
    }

    private float currentBossArenaHudMaxHealth() {
        if (!MAP_ID_BOSS_ARENA_PHASE_2.equals(world.activeMapId)) {
            return 0f;
        }
        BossArenaPhase2DemonInstance boss = findBossArenaPhase2Leader();
        return boss != null ? boss.maxHealth : 0f;
    }

    private float currentBossArenaHudAlpha() {
        if (!MAP_ID_BOSS_ARENA_PHASE_2.equals(world.activeMapId)) {
            return 0f;
        }
        if (isBossArenaFinalIntroActive()) {
            return 0f;
        }
        return clamp01(bossArenaHudRevealTimer / BOSS_ARENA_HUD_FADE_IN_SECONDS);
    }

    private float currentBossArenaPhase2ExplosionGrowthProgress() {
        BossArenaPhase2DemonInstance boss = findBossArenaPhase2Leader();
        if (boss == null || boss.maxHealth <= 0f) {
            return 0f;
        }
        return clamp01(1f - boss.health / boss.maxHealth);
    }

    private float currentBossArenaPhase2ExplosionRadiusTiles() {
        float maxRadiusTiles = BOSS_ARENA_PHASE_2_EXPLOSION_MAX_DIAMETER_TILES * 0.5f;
        return lerp(
                BOSS_ARENA_PHASE_2_EXPLOSION_HIT_RADIUS_TILES,
                maxRadiusTiles,
                currentBossArenaPhase2ExplosionGrowthProgress());
    }

    private float currentBossArenaPhase2ExplosionDrawScale() {
        float baseRadiusTiles = Math.max(0.001f, BOSS_ARENA_PHASE_2_EXPLOSION_HIT_RADIUS_TILES);
        return BOSS_ARENA_PHASE_2_EXPLOSION_DRAW_SCALE
                * (currentBossArenaPhase2ExplosionRadiusTiles() / baseRadiusTiles);
    }

    private boolean shouldShowHudQuestPanel() {
        return !MAP_ID_BOSS_ARENA_PHASE_2.equals(world.activeMapId);
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
        boolean blurVisible = (hudRenderer != null && hudRenderer.isMainMenuVisible())
                || isFirstQuestOverlayActive()
                || isSixthQuestPortalActivationRunning()
                || isBossArenaFinalIntroFocusActive()
                || isFifthQuestFirstEncounterIntroActive();
        if (QUEST_BLUR_TRANSITION_SECONDS <= 0f) {
            questBlurProgress = blurVisible ? 1f : 0f;
            return;
        }

        float step = Math.max(0f, delta) / QUEST_BLUR_TRANSITION_SECONDS;
        if (blurVisible) {
            questBlurProgress = Math.min(1f, questBlurProgress + step);
        } else {
            questBlurProgress = Math.max(0f, questBlurProgress - step);
        }
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
        drawThirdQuestTrainingGround(batch);
        drawFourthQuestWorldHints(batch);
        drawFifthQuestWorldHints(batch);
        drawSixthQuestPortalActivationEffect(characterSaturation);
        drawEvilWizards(characterSaturation, true);
        drawGolem1s(characterSaturation, true);
        drawBossArenaPhase2Demons(characterSaturation, true);
        drawFifthQuestDemons(characterSaturation, true);
        drawRewardChests(characterSaturation, true);
        drawFirstQuestCafeNpc(characterSaturation, true);
        drawFifthQuestLabVillagers(characterSaturation, true);
        drawThirdQuestVillageFather(batch);
        drawSixthQuestEndingScientist(characterSaturation);
        drawSixthQuestEndingFather(batch);
        drawPlayer(characterSaturation);
        drawSpawnedBat(characterSaturation);
        drawFifthQuestLabVillagers(characterSaturation, false);
        drawFirstQuestCafeNpc(characterSaturation, false);
        drawRewardChests(characterSaturation, false);
        drawFifthQuestDemons(characterSaturation, false);
        drawBossArenaPhase2Demons(characterSaturation, false);
        drawBossArenaPhase2Explosions(characterSaturation);
        drawGolem1s(characterSaturation, false);
        drawEvilWizards(characterSaturation, false);
        // Explosion effects should sit above both character and golems.
        drawGolem1Explosions(characterSaturation);
        batch.end();

        renderMapWithSaturation(foregroundRenderLayers, worldSaturation);
        drawDayNightOverlay();
        drawVignette();
    }

    private void drawThirdQuestTrainingGround(SpriteBatch batch) {
        if (batch == null
                || dayNightOverlayTex == null
                || !MAP_ID_VILLAGE.equals(world.activeMapId)
                || fourthQuestStage > FOURTH_QUEST_DISABLED
                || thirdQuestStage < THIRD_QUEST_MEET_FATHER_OUTSIDE
                || !thirdQuestTrainingLayoutReady) {
            return;
        }

        batch.setColor(0.30f, 0.22f, 0.12f, 0.24f);
        batch.draw(
                dayNightOverlayTex,
                thirdQuestTrainingZoneBounds.x,
                thirdQuestTrainingZoneBounds.y,
                thirdQuestTrainingZoneBounds.width,
                thirdQuestTrainingZoneBounds.height);
        batch.setColor(0.72f, 0.58f, 0.30f, 0.72f);
        drawQuestRectOutline(batch, thirdQuestTrainingZoneBounds, 3f);

        if (thirdQuestStage == THIRD_QUEST_MOVEMENT_TUTORIAL) {
            for (int i = 0; i < thirdQuestMovementMarkers.length; i++) {
                Vector2 marker = thirdQuestMovementMarkers[i];
                float size = thirdQuestMovementMarkerReached[i] ? 16f : 20f;
                float colorBoost = thirdQuestMovementMarkerReached[i] ? 0.55f : 0.88f;
                batch.setColor(0.36f, colorBoost, 0.42f, thirdQuestMovementMarkerReached[i] ? 0.46f : 0.82f);
                batch.draw(dayNightOverlayTex, marker.x - size * 0.5f, marker.y - size * 0.5f, size, size);
            }
        }

        if (thirdQuestStage >= THIRD_QUEST_ATTACK_TUTORIAL) {
            batch.setColor(thirdQuestAttackDummyBroken ? 0.20f : 0.47f, 0.28f, 0.10f, 0.95f);
            batch.draw(
                    dayNightOverlayTex,
                    thirdQuestAttackDummyBounds.x + thirdQuestAttackDummyBounds.width * 0.3f,
                    thirdQuestAttackDummyBounds.y,
                    thirdQuestAttackDummyBounds.width * 0.4f,
                    thirdQuestAttackDummyBounds.height);
            batch.setColor(thirdQuestAttackDummyBroken ? 0.25f : 0.62f, 0.42f, 0.18f, 0.95f);
            batch.draw(
                    dayNightOverlayTex,
                    thirdQuestAttackDummyBounds.x,
                    thirdQuestAttackDummyBounds.y + thirdQuestAttackDummyBounds.height * 0.55f,
                    thirdQuestAttackDummyBounds.width,
                    thirdQuestAttackDummyBounds.height * 0.25f);
        }

        if (thirdQuestSparActive) {
            float hpRatio = THIRD_QUEST_SPAR_FATHER_MAX_HEALTH <= 0f
                    ? 0f
                    : clamp01(thirdQuestFatherSparHealth / THIRD_QUEST_SPAR_FATHER_MAX_HEALTH);
            float hpWidth = 28f;
            float hpX = thirdQuestFatherTrainingBounds.x + thirdQuestFatherTrainingBounds.width * 0.5f - hpWidth * 0.5f;
            float hpY = thirdQuestFatherTrainingBounds.y + thirdQuestFatherTrainingBounds.height + 10f;
            batch.setColor(0f, 0f, 0f, 0.9f);
            batch.draw(dayNightOverlayTex, hpX - 1f, hpY - 1f, hpWidth + 2f, 6f);
            batch.setColor(0.22f, 0.14f, 0.14f, 1f);
            batch.draw(dayNightOverlayTex, hpX, hpY, hpWidth, 4f);
            batch.setColor(0.82f, 0.24f, 0.24f, 1f);
            batch.draw(dayNightOverlayTex, hpX, hpY, hpWidth * hpRatio, 4f);
        }

        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void drawThirdQuestVillageFather(SpriteBatch batch) {
        if (batch == null
                || !MAP_ID_VILLAGE.equals(world.activeMapId)
                || fourthQuestStage > FOURTH_QUEST_DISABLED
                || thirdQuestStage < THIRD_QUEST_MEET_FATHER_OUTSIDE
                || !thirdQuestTrainingLayoutReady
                || thirdQuestFatherVillageTiles == null
                || thirdQuestFatherVillageTiles.length < 4) {
            return;
        }

        int frame = ((int) (firstQuestCafeNpcStateTime / FIRST_QUEST_CAFE_NPC_FRAME_DURATION)) % 5;
        int baseColumn = 2 + frame * 5;
        int[] sourceRows = {3, 2, 1};
        for (int row = 0; row < sourceRows.length; row++) {
            int sourceRow = sourceRows[row];
            if (sourceRow < 0 || sourceRow >= thirdQuestFatherVillageTiles.length) {
                continue;
            }
            TextureRegion[] rowRegions = thirdQuestFatherVillageTiles[sourceRow];
            if (rowRegions == null || baseColumn + 1 >= rowRegions.length) {
                continue;
            }
            for (int col = 0; col < 2; col++) {
                TextureRegion region = rowRegions[baseColumn + col];
                if (region == null) {
                    continue;
                }
                batch.draw(
                        region,
                        thirdQuestFatherTrainingBounds.x + col * DEFAULT_TILE_SIZE,
                        thirdQuestFatherTrainingBounds.y + row * DEFAULT_TILE_SIZE,
                        DEFAULT_TILE_SIZE,
                        DEFAULT_TILE_SIZE);
            }
        }
    }

    private void drawSixthQuestEndingFather(SpriteBatch batch) {
        Rectangle fatherBounds = sixthQuestEndingFatherBounds();
        if (batch == null
                || fatherBounds == null
                || thirdQuestFatherVillageTiles == null
                || thirdQuestFatherVillageTiles.length < 4) {
            return;
        }
        float fadeAlpha = sixthQuestEndingNpcFadeAlpha();
        if (fadeAlpha <= 0f) {
            return;
        }

        int talkFrame = Math.max(0, SIXTH_QUEST_ENDING_FATHER_TALKING_FRAME_INDEX);
        int baseColumn = 2 + talkFrame * 5;
        int[] sourceRows = {3, 2, 1};
        batch.setColor(1f, 1f, 1f, fadeAlpha);
        for (int row = 0; row < sourceRows.length; row++) {
            int sourceRow = sourceRows[row];
            if (sourceRow < 0 || sourceRow >= thirdQuestFatherVillageTiles.length) {
                continue;
            }
            TextureRegion[] rowRegions = thirdQuestFatherVillageTiles[sourceRow];
            if (rowRegions == null || baseColumn + 1 >= rowRegions.length) {
                continue;
            }
            for (int col = 0; col < 2; col++) {
                TextureRegion region = rowRegions[baseColumn + col];
                if (region == null) {
                    continue;
                }
                batch.draw(
                        region,
                        fatherBounds.x + col * DEFAULT_TILE_SIZE,
                        fatherBounds.y + row * DEFAULT_TILE_SIZE,
                        DEFAULT_TILE_SIZE,
                        DEFAULT_TILE_SIZE);
            }
        }
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void drawSixthQuestEndingScientist(float saturation) {
        if (batch == null || sixthQuestEndingScientistAnim == null) {
            return;
        }
        Rectangle scientistBounds = sixthQuestEndingScientistBounds();
        if (scientistBounds == null) {
            return;
        }
        float fadeAlpha = sixthQuestEndingNpcFadeAlpha();
        if (fadeAlpha <= 0f) {
            return;
        }
        TextureRegion frame = sixthQuestEndingScientistAnim.getKeyFrame(firstQuestCafeNpcStateTime, true);
        if (frame == null) {
            return;
        }
        if (saturationShader != null) {
            batch.setShader(saturationShader);
            saturationShader.setUniformf("u_saturation", saturation);
        }
        batch.setColor(1f, 1f, 1f, fadeAlpha);
        batch.draw(frame, scientistBounds.x, scientistBounds.y, scientistBounds.width, scientistBounds.height);
        batch.setColor(1f, 1f, 1f, 1f);
        if (saturationShader != null) {
            batch.setShader(null);
        }
    }

    private void drawFifthQuestPortalDialogueFather(SpriteBatch batch) {
        Rectangle fatherBounds = fifthQuestPortalDialogueFatherBounds();
        if (batch == null
                || fatherBounds == null
                || thirdQuestFatherVillageTiles == null
                || thirdQuestFatherVillageTiles.length < 4) {
            return;
        }
        float fadeAlpha = fifthQuestPortalDialogueNpcFadeAlpha();
        if (fadeAlpha <= 0f) {
            return;
        }

        int talkFrame = Math.max(0, SIXTH_QUEST_ENDING_FATHER_TALKING_FRAME_INDEX);
        int baseColumn = 2 + talkFrame * 5;
        int[] sourceRows = {3, 2, 1};
        batch.setColor(1f, 1f, 1f, fadeAlpha);
        for (int row = 0; row < sourceRows.length; row++) {
            int sourceRow = sourceRows[row];
            if (sourceRow < 0 || sourceRow >= thirdQuestFatherVillageTiles.length) {
                continue;
            }
            TextureRegion[] rowRegions = thirdQuestFatherVillageTiles[sourceRow];
            if (rowRegions == null || baseColumn + 1 >= rowRegions.length) {
                continue;
            }
            for (int col = 0; col < 2; col++) {
                TextureRegion region = rowRegions[baseColumn + col];
                if (region == null) {
                    continue;
                }
                batch.draw(
                        region,
                        fatherBounds.x + col * DEFAULT_TILE_SIZE,
                        fatherBounds.y + row * DEFAULT_TILE_SIZE,
                        DEFAULT_TILE_SIZE,
                        DEFAULT_TILE_SIZE);
            }
        }
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void drawFifthQuestPortalDialogueScientist(float saturation) {
        if (batch == null || sixthQuestEndingScientistAnim == null) {
            return;
        }
        Rectangle scientistBounds = fifthQuestPortalDialogueScientistBounds();
        if (scientistBounds == null) {
            return;
        }
        float fadeAlpha = fifthQuestPortalDialogueNpcFadeAlpha();
        if (fadeAlpha <= 0f) {
            return;
        }
        TextureRegion frame = sixthQuestEndingScientistAnim.getKeyFrame(firstQuestCafeNpcStateTime, true);
        if (frame == null) {
            return;
        }
        if (saturationShader != null) {
            batch.setShader(saturationShader);
            saturationShader.setUniformf("u_saturation", saturation);
        }
        batch.setColor(1f, 1f, 1f, fadeAlpha);
        batch.draw(
                frame,
                scientistBounds.x + scientistBounds.width,
                scientistBounds.y,
                -scientistBounds.width,
                scientistBounds.height);
        batch.setColor(1f, 1f, 1f, 1f);
        if (saturationShader != null) {
            batch.setShader(null);
        }
    }

    private float sixthQuestEndingNpcFadeAlpha() {
        if (!sixthQuestEndingNpcFadeActive) {
            return 1f;
        }
        if (SIXTH_QUEST_ENDING_NPC_FADE_OUT_SECONDS <= 0f) {
            return 0f;
        }
        return 1f - clamp01(sixthQuestEndingNpcFadeTimer / SIXTH_QUEST_ENDING_NPC_FADE_OUT_SECONDS);
    }

    private void drawSixthQuestPortalActivationEffect(float saturation) {
        drawSixthQuestPortalActivationEffect(saturation, 1f);
    }

    private void drawSixthQuestPortalActivationEffect(float saturation, float alphaMultiplier) {
        if (!MAP_ID_VILLAGE.equals(world.activeMapId) || !isSixthQuestPortalActivationRunning()) {
            return;
        }
        Animation<TextureRegion> portalAnim = switch (sixthQuestPortalActivationPhase) {
            case START -> bossArenaPhase2SkillStartAnim;
            case LOOP -> bossArenaPhase2SkillLoopAnim;
            case END -> bossArenaPhase2SkillEndAnim;
            case NONE -> null;
        };
        if (saturationShader != null) {
            batch.setShader(saturationShader);
            saturationShader.setUniformf("u_saturation", saturation);
        }
        if (portalAnim != null) {
            boolean looping = sixthQuestPortalActivationPhase == PortalActivationPhase.LOOP;
            TextureRegion portalFrame = portalAnim.getKeyFrame(sixthQuestPortalActivationStateTime, looping);
            if (portalFrame != null) {
                float drawWidth = portalFrame.getRegionWidth() * SIXTH_QUEST_PORTAL_EFFECT_DRAW_SCALE;
                float drawHeight = portalFrame.getRegionHeight() * SIXTH_QUEST_PORTAL_EFFECT_DRAW_SCALE;
                float drawX = sixthQuestPortalEffectPosition.x - drawWidth * 0.5f;
                float drawY = sixthQuestPortalEffectPosition.y - drawHeight * 0.5f;
                batch.setColor(1f, 1f, 1f, clamp01(alphaMultiplier));
                batch.draw(portalFrame, drawX, drawY, drawWidth, drawHeight);
                batch.setColor(1f, 1f, 1f, 1f);
            }
        } else {
            drawSixthQuestPortalActivationFallback(alphaMultiplier);
        }
        if (saturationShader != null) {
            batch.setShader(null);
        }
    }

    private void drawSixthQuestPortalActivationFallback(float alphaMultiplier) {
        if (batch == null || dayNightOverlayTex == null) {
            return;
        }

        Rectangle portalBounds = sixthQuestActualPortalBounds();
        if ((portalBounds == null || portalBounds.width <= 0f || portalBounds.height <= 0f)
                && sixthQuestPortalGuideBounds() != null) {
            portalBounds = sixthQuestPortalGuideBounds();
        }

        float centerX = sixthQuestPortalEffectPosition.x;
        float centerY = sixthQuestPortalEffectPosition.y;
        float baseWidth = portalBounds != null && portalBounds.width > 0f ? portalBounds.width : DEFAULT_TILE_SIZE * 2.25f;
        float baseHeight = portalBounds != null && portalBounds.height > 0f ? portalBounds.height : DEFAULT_TILE_SIZE * 3.25f;
        float time = sixthQuestPortalActivationPhase == PortalActivationPhase.LOOP
                ? sixthQuestPortalActivationLoopTimer
                : sixthQuestPortalActivationStateTime;
        float pulse = 0.82f + 0.18f * (float) Math.sin(time * 5.4f);

        float phaseScale = switch (sixthQuestPortalActivationPhase) {
            case START -> lerp(0.55f, 1.08f, clamp01(sixthQuestPortalActivationStateTime / 0.45f));
            case END -> lerp(1.02f, 0.45f, clamp01(sixthQuestPortalActivationStateTime / 0.35f));
            case LOOP -> 1f + 0.04f * (float) Math.sin(time * 2.6f);
            case NONE -> 1f;
        };

        float auraWidth = baseWidth * 2.8f * phaseScale;
        float auraHeight = baseHeight * 2.5f * phaseScale;
        float ringWidth = baseWidth * (1.95f + 0.18f * pulse) * phaseScale;
        float ringHeight = baseHeight * (2.1f + 0.18f * pulse) * phaseScale;
        float coreWidth = baseWidth * (1.2f + 0.1f * pulse) * phaseScale;
        float coreHeight = baseHeight * (1.55f + 0.12f * pulse) * phaseScale;
        float beamWidth = Math.max(10f, baseWidth * 0.16f);
        float beamHeight = baseHeight * (2.25f + 0.2f * pulse) * phaseScale;
        float outlineThickness = Math.max(2f, baseWidth * 0.045f);
        float alpha = clamp01(alphaMultiplier);

        batch.setColor(0.20f, 0.02f, 0.02f, 0.22f * alpha * pulse);
        batch.draw(dayNightOverlayTex, centerX - auraWidth * 0.5f, centerY - auraHeight * 0.5f, auraWidth, auraHeight);

        batch.setColor(0.85f, 0.18f, 0.10f, 0.34f * alpha * pulse);
        batch.draw(dayNightOverlayTex, centerX - ringWidth * 0.5f, centerY - ringHeight * 0.5f, ringWidth, ringHeight);

        float ringX = centerX - ringWidth * 0.5f;
        float ringY = centerY - ringHeight * 0.5f;
        batch.setColor(1f, 0.70f, 0.32f, 0.78f * alpha * pulse);
        batch.draw(dayNightOverlayTex, ringX, ringY, ringWidth, outlineThickness);
        batch.draw(dayNightOverlayTex, ringX, ringY + ringHeight - outlineThickness, ringWidth, outlineThickness);
        batch.draw(dayNightOverlayTex, ringX, ringY, outlineThickness, ringHeight);
        batch.draw(dayNightOverlayTex, ringX + ringWidth - outlineThickness, ringY, outlineThickness, ringHeight);

        batch.setColor(1f, 0.82f, 0.42f, 0.42f * alpha * pulse);
        batch.draw(dayNightOverlayTex, centerX - beamWidth * 0.5f, centerY - beamHeight * 0.5f, beamWidth, beamHeight);

        batch.setColor(1f, 0.96f, 0.84f, 0.72f * alpha * pulse);
        batch.draw(dayNightOverlayTex, centerX - coreWidth * 0.5f, centerY - coreHeight * 0.5f, coreWidth, coreHeight);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void drawFourthQuestWorldHints(SpriteBatch batch) {
        if (batch == null || dayNightOverlayTex == null || world.activeMapId == null) {
            return;
        }
        if (MAP_ID_VILLAGE.equals(world.activeMapId)) {
            ensureFourthQuestVillageLayout();
            if (!fourthQuestVillageLayoutReady || fourthQuestStage <= FOURTH_QUEST_DISABLED) {
                return;
            }
            if (fourthQuestStage == FOURTH_QUEST_EXAMINE_CAFE_MARK || fourthQuestStage == FOURTH_QUEST_CAFE_BATTLE) {
                drawFourthQuestSigil(batch, fourthQuestCafeSiteBounds, 0.86f, 0.28f, 0.22f, 0.72f);
            }
            if (fourthQuestStage == FOURTH_QUEST_EXAMINE_CENTER_MARK || fourthQuestStage == FOURTH_QUEST_CENTER_BATTLE) {
                drawFourthQuestSigil(batch, fourthQuestCenterSiteBounds, 0.90f, 0.34f, 0.24f, 0.76f);
            }
            if (fourthQuestStage == FOURTH_QUEST_EXAMINE_EDGE_MARK || fourthQuestStage == FOURTH_QUEST_EDGE_BATTLE) {
                drawFourthQuestSigil(batch, fourthQuestEdgeSiteBounds, 0.96f, 0.30f, 0.18f, 0.82f);
            }
            if (fourthQuestStage == FOURTH_QUEST_GO_TO_BREACH
                    || fourthQuestStage == FOURTH_QUEST_EXAMINE_BREACH
                    || fourthQuestStage == FOURTH_QUEST_BREACH_BATTLE
                    || fourthQuestStage == FOURTH_QUEST_RETURN_RIFT_CORE) {
                drawFourthQuestSigil(batch, fourthQuestBreachSiteBounds, 0.98f, 0.20f, 0.20f, 0.88f);
            }
            batch.setColor(1f, 1f, 1f, 1f);
            return;
        }
        if (MAP_ID_RESEARCH_LAB_FLOOR_2.equals(world.activeMapId)
                && fourthQuestStage >= FOURTH_QUEST_GO_TO_ARRAY
                && fourthQuestStage <= FOURTH_QUEST_ARRAY_DEFENSE) {
            ensureFourthQuestFloor2Layout();
            if (!fourthQuestFloor2LayoutReady) {
                return;
            }
            for (int i = 0; i < fourthQuestArrayNodeBounds.length; i++) {
                Rectangle nodeBounds = fourthQuestArrayNodeBounds[i];
                boolean active = (fourthQuestStage > FOURTH_QUEST_GO_TO_ARRAY && i == 0)
                        || (fourthQuestStage > FOURTH_QUEST_ACTIVATE_ARRAY_NODE_2 && i == 1)
                        || (fourthQuestStage > FOURTH_QUEST_ACTIVATE_ARRAY_NODE_3 && i == 2)
                        || fourthQuestStage >= FOURTH_QUEST_ACTIVATE_ARRAY_CONSOLE;
                batch.setColor(active ? 0.26f : 0.10f, active ? 0.92f : 0.42f, 0.98f, active ? 0.94f : 0.64f);
                batch.draw(dayNightOverlayTex, nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
                batch.setColor(0.08f, 0.14f, 0.18f, 0.92f);
                drawQuestRectOutline(batch, nodeBounds, 2f);
            }
            batch.setColor(
                    fourthQuestStage >= FOURTH_QUEST_ACTIVATE_ARRAY_CONSOLE ? 0.24f : 0.14f,
                    fourthQuestStage >= FOURTH_QUEST_ACTIVATE_ARRAY_CONSOLE ? 0.78f : 0.34f,
                    0.98f,
                    fourthQuestStage == FOURTH_QUEST_ARRAY_DEFENSE ? 0.96f : 0.72f);
            batch.draw(
                    dayNightOverlayTex,
                    fourthQuestArrayConsoleBounds.x,
                    fourthQuestArrayConsoleBounds.y,
                    fourthQuestArrayConsoleBounds.width,
                    fourthQuestArrayConsoleBounds.height);
            batch.setColor(0.10f, 0.12f, 0.16f, 0.92f);
            drawQuestRectOutline(batch, fourthQuestArrayConsoleBounds, 2f);
            batch.setColor(1f, 1f, 1f, 1f);
        }
    }

    private void drawFifthQuestWorldHints(SpriteBatch batch) {
        if (batch == null || dayNightOverlayTex == null || !MAP_ID_VILLAGE.equals(world.activeMapId) || fifthQuestStage <= FIFTH_QUEST_DISABLED) {
            return;
        }
        ensureFifthQuestVillageLayout();
        if (!fifthQuestVillageLayoutReady) {
            return;
        }

        if (fifthQuestStage == FIFTH_QUEST_ANCHOR_1_BATTLE
                || fifthQuestStage == FIFTH_QUEST_ANCHOR_1_DESTROY) {
            batch.setColor(0.16f, 0.08f, 0.08f, 0.42f);
            batch.draw(dayNightOverlayTex,
                    fifthQuestAnchorBounds[0].x,
                    fifthQuestAnchorBounds[0].y,
                    fifthQuestAnchorBounds[0].width,
                    fifthQuestAnchorBounds[0].height);
            batch.setColor(0.54f, 0.24f, 0.18f, 0.84f);
            drawQuestRectOutline(batch, fifthQuestAnchorBounds[0], 2.5f);
        }

        if (fifthQuestStage == FIFTH_QUEST_ANCHOR_2_BATTLE
                || fifthQuestStage == FIFTH_QUEST_ANCHOR_2_DESTROY) {
            batch.setColor(0.18f, 0.06f, 0.06f, 0.48f);
            batch.draw(dayNightOverlayTex,
                    fifthQuestAnchorBounds[1].x,
                    fifthQuestAnchorBounds[1].y,
                    fifthQuestAnchorBounds[1].width,
                    fifthQuestAnchorBounds[1].height);
            batch.setColor(0.58f, 0.20f, 0.18f, 0.88f);
            drawQuestRectOutline(batch, fifthQuestAnchorBounds[1], 2.5f);
        }

        if (fifthQuestStage == FIFTH_QUEST_ANCHOR_3_BATTLE
                || fifthQuestStage == FIFTH_QUEST_ANCHOR_3_DESTROY) {
            batch.setColor(0.18f, 0.06f, 0.06f, 0.48f);
            batch.draw(dayNightOverlayTex,
                    fifthQuestAnchorBounds[2].x,
                    fifthQuestAnchorBounds[2].y,
                    fifthQuestAnchorBounds[2].width,
                    fifthQuestAnchorBounds[2].height);
            batch.setColor(0.58f, 0.20f, 0.18f, 0.88f);
            drawQuestRectOutline(batch, fifthQuestAnchorBounds[2], 2.5f);
        }

        if (fifthQuestStage < FIFTH_QUEST_COMPLETE) {
            for (int i = 0; i < fifthQuestAnchorBounds.length; i++) {
                Rectangle anchorBounds = fifthQuestAnchorBounds[i];
                boolean destroyed = (i == 0 && fifthQuestStage > FIFTH_QUEST_ANCHOR_1_DESTROY)
                        || (i == 1 && fifthQuestStage > FIFTH_QUEST_ANCHOR_2_DESTROY)
                        || (i == 2 && fifthQuestStage > FIFTH_QUEST_ANCHOR_3_DESTROY);
                if (destroyed) {
                    batch.setColor(0.20f, 0.20f, 0.20f, 0.44f);
                } else {
                    float pulse = 0.72f + 0.20f * (float) Math.sin(firstQuestMarkerTimer * 4.1f + i * 0.9f);
                    batch.setColor(0.86f, 0.22f, 0.18f, 0.55f * pulse);
                }
                batch.draw(dayNightOverlayTex, anchorBounds.x, anchorBounds.y, anchorBounds.width, anchorBounds.height);
                batch.setColor(destroyed ? 0.34f : 0.92f, destroyed ? 0.34f : 0.34f, destroyed ? 0.34f : 0.18f, 0.92f);
                drawQuestRectOutline(batch, anchorBounds, 2f);
            }
        }

        if (fifthQuestStage >= FIFTH_QUEST_COMPLETE
                && (sixthQuestStage <= SIXTH_QUEST_ENTER_PORTAL || sixthQuestStage == SIXTH_QUEST_DISABLED)) {
            float portalPulse = 0.74f + 0.22f * (float) Math.sin(firstQuestMarkerTimer * 3.3f);
            Rectangle highlightBounds = fifthQuestPortalBounds;
            batch.setColor(0.34f, 0.02f, 0.02f, fifthQuestStage >= FIFTH_QUEST_COMPLETE ? 0.50f : 0.68f);
            batch.draw(dayNightOverlayTex,
                    highlightBounds.x - 16f,
                    highlightBounds.y - 12f,
                    highlightBounds.width + 32f,
                    highlightBounds.height + 24f);
            batch.setColor(
                    fifthQuestStage >= FIFTH_QUEST_COMPLETE ? 0.88f : 0.98f,
                    fifthQuestStage >= FIFTH_QUEST_COMPLETE ? 0.18f : 0.10f,
                    fifthQuestStage >= FIFTH_QUEST_COMPLETE ? 0.22f : 0.14f,
                    0.86f * portalPulse);
            drawQuestRectOutline(batch, highlightBounds, 3f);
            batch.draw(
                    dayNightOverlayTex,
                    highlightBounds.x + highlightBounds.width * 0.46f,
                    highlightBounds.y - 10f,
                    6f,
                    highlightBounds.height + 18f);
        }
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void drawFifthQuestLabVillagers(float saturation, boolean drawBehindPlayer) {
        if (!MAP_ID_RESEARCH_LAB_FLOOR_1.equals(world.activeMapId)
                || (fifthQuestStage != FIFTH_QUEST_GO_TO_ABANDONED_VILLAGE
                && activeQuestDialogueId != QuestDialogueId.FIFTH_QUEST_LAB_ALARM)) {
            return;
        }
        Rectangle scientistBounds = scientistNpcBounds();
        if (scientistBounds == null) {
            return;
        }
        float fadeAlpha = FIFTH_QUEST_LAB_VILLAGER_FADE_IN_SECONDS <= 0f
                ? 1f
                : clamp01(fifthQuestLabVillagersFadeTimer / FIFTH_QUEST_LAB_VILLAGER_FADE_IN_SECONDS);
        float scientistCenterX = scientistBounds.x + scientistBounds.width * 0.5f;
        float scientistCenterY = scientistBounds.y + scientistBounds.height * 0.5f;
        if (activeQuestDialogueId == QuestDialogueId.FIFTH_QUEST_LAB_ALARM) {
            drawFifthQuestLabFather(
                    scientistCenterX + FIFTH_QUEST_LAB_FATHER_OFFSET_X,
                    scientistCenterY + FIFTH_QUEST_LAB_FATHER_OFFSET_Y,
                    saturation,
                    drawBehindPlayer,
                    fadeAlpha);
        }
        drawFifthQuestVillager(
                fifthQuestAdultManIdleAnim,
                scientistCenterX - 72f,
                scientistCenterY - 10f,
                saturation,
                drawBehindPlayer,
                fadeAlpha);
        drawFifthQuestVillager(
                fifthQuestOldWomanIdleAnim,
                scientistCenterX + 76f - FIFTH_QUEST_OLD_WOMAN_X_SHIFT,
                scientistCenterY - 16f,
                saturation,
                drawBehindPlayer,
                fadeAlpha);
    }

    private void drawFifthQuestLabFather(float centerX, float centerY,
                                         float saturation, boolean drawBehindPlayer, float alpha) {
        if (thirdQuestFatherVillageTiles == null || thirdQuestFatherVillageTiles.length < 4) {
            return;
        }
        int frame = ((int) (firstQuestCafeNpcStateTime / FIRST_QUEST_CAFE_NPC_FRAME_DURATION)) % 5;
        int baseColumn = 2 + frame * 5;
        int[] sourceRows = {3, 2, 1};
        float drawW = DEFAULT_TILE_SIZE * 2f;
        float drawH = DEFAULT_TILE_SIZE * 3f;
        float depthBasisY = centerY - drawH * 0.35f;
        boolean fatherBehindPlayer = depthBasisY > player.bounds.y;
        if (fatherBehindPlayer != drawBehindPlayer) {
            return;
        }
        if (saturationShader != null) {
            batch.setShader(saturationShader);
            saturationShader.setUniformf("u_saturation", saturation);
        }
        batch.setColor(1f, 1f, 1f, clamp01(alpha));
        for (int row = 0; row < sourceRows.length; row++) {
            int sourceRow = sourceRows[row];
            if (sourceRow < 0 || sourceRow >= thirdQuestFatherVillageTiles.length) {
                continue;
            }
            TextureRegion[] rowRegions = thirdQuestFatherVillageTiles[sourceRow];
            if (rowRegions == null || baseColumn + 1 >= rowRegions.length) {
                continue;
            }
            for (int col = 0; col < 2; col++) {
                TextureRegion region = rowRegions[baseColumn + col];
                if (region == null) {
                    continue;
                }
                float drawX = centerX - drawW * 0.5f + (2 - col) * DEFAULT_TILE_SIZE;
                batch.draw(
                        region,
                        drawX,
                        centerY - drawH * 0.5f + row * DEFAULT_TILE_SIZE,
                        -DEFAULT_TILE_SIZE,
                        DEFAULT_TILE_SIZE);
            }
        }
        batch.setColor(1f, 1f, 1f, 1f);
        if (saturationShader != null) {
            batch.setShader(null);
        }
    }

    private void drawFifthQuestVillager(Animation<TextureRegion> animation, float centerX, float centerY,
                                        float saturation, boolean drawBehindPlayer, float alpha) {
        if (animation == null) {
            return;
        }
        TextureRegion frame = animation.getKeyFrame(firstQuestCafeNpcStateTime, true);
        if (frame == null) {
            return;
        }
        float drawW = frame.getRegionWidth() * FIFTH_QUEST_VILLAGER_DRAW_SCALE;
        float drawH = frame.getRegionHeight() * FIFTH_QUEST_VILLAGER_DRAW_SCALE;
        float depthBasisY = centerY - drawH * 0.35f;
        boolean villagerBehindPlayer = depthBasisY > player.bounds.y;
        if (villagerBehindPlayer != drawBehindPlayer) {
            return;
        }
        if (saturationShader != null) {
            batch.setShader(saturationShader);
            saturationShader.setUniformf("u_saturation", saturation);
        }
        batch.setColor(1f, 1f, 1f, clamp01(alpha));
        batch.draw(frame, centerX - drawW * 0.5f, centerY - drawH * 0.5f, drawW, drawH);
        batch.setColor(1f, 1f, 1f, 1f);
        if (saturationShader != null) {
            batch.setShader(null);
        }
    }

    private void drawFifthQuestDemons(float saturation, boolean drawBehindPlayer) {
        if (fifthQuestDemons.size == 0) {
            return;
        }
        float playerDepthBasisY = player.bounds.y;
        if (shadowRegion != null) {
            float shadowW = FIFTH_QUEST_DEMON_SHADOW_WIDTH_TILES * DEFAULT_TILE_SIZE;
            float shadowH = FIFTH_QUEST_DEMON_SHADOW_HEIGHT_TILES * DEFAULT_TILE_SIZE;
            float shadowDownOffset = FIFTH_QUEST_DEMON_SHADOW_DOWN_OFFSET_TILES * DEFAULT_TILE_SIZE;
            batch.setShader(null);
            batch.setColor(1f, 1f, 1f, FIFTH_QUEST_DEMON_SHADOW_ALPHA);
            for (int i = 0; i < fifthQuestDemons.size; i++) {
                FifthQuestDemonInstance demon = fifthQuestDemons.get(i);
                float demonDepthBasisY = demon.position.y;
                boolean demonBehindPlayer = demonDepthBasisY > playerDepthBasisY;
                if (demonBehindPlayer != drawBehindPlayer || demon.dying) {
                    continue;
                }
                batch.draw(
                        shadowRegion,
                        demon.position.x - shadowW * 0.5f,
                        demonDepthBasisY - shadowH * 0.25f - shadowDownOffset,
                        shadowW,
                        shadowH);
            }
            batch.setColor(1f, 1f, 1f, 1f);
        }
        if (saturationShader != null) {
            batch.setShader(saturationShader);
            saturationShader.setUniformf("u_saturation", saturation);
        }
        for (int i = 0; i < fifthQuestDemons.size; i++) {
            FifthQuestDemonInstance demon = fifthQuestDemons.get(i);
            float demonDepthBasisY = demon.position.y;
            boolean demonBehindPlayer = demonDepthBasisY > playerDepthBasisY;
            if (demonBehindPlayer != drawBehindPlayer) {
                continue;
            }
            Animation<TextureRegion> activeAnimation;
            boolean loop = true;
            float animationTime;
            if (demon.dying && fifthQuestDemonDeadAnimation(demon.type) != null) {
                activeAnimation = fifthQuestDemonDeadAnimation(demon.type);
                animationTime = demon.dieStateTime;
                loop = false;
            } else if (demon.hurting && fifthQuestDemonHurtAnimation(demon.type) != null) {
                activeAnimation = fifthQuestDemonHurtAnimation(demon.type);
                animationTime = demon.hurtStateTime;
                loop = false;
            } else if (demon.attacking && fifthQuestDemonAttackAnimation(demon.type) != null) {
                activeAnimation = fifthQuestDemonAttackAnimation(demon.type);
                animationTime = demon.attackStateTime;
                loop = false;
            } else if (demon.moving && fifthQuestDemonWalkAnimation(demon.type) != null) {
                activeAnimation = fifthQuestDemonWalkAnimation(demon.type);
                animationTime = demon.stateTime;
            } else {
                activeAnimation = fifthQuestDemonIdleAnimation(demon.type);
                animationTime = demon.stateTime;
            }
            if (activeAnimation == null) {
                continue;
            }
            TextureRegion frame = activeAnimation.getKeyFrame(animationTime, loop);
            if (frame == null) {
                continue;
            }
            float drawW = frame.getRegionWidth() * FIFTH_QUEST_DEMON_DRAW_SCALE;
            float drawH = frame.getRegionHeight() * FIFTH_QUEST_DEMON_DRAW_SCALE;
            float drawX = demon.position.x - drawW * 0.5f;
            float drawY = demon.position.y - drawH * 0.5f;
            float originX = drawW * 0.5f;
            float originY = drawH * 0.5f;
            float scaleX = demon.facingLeft ? -1f : 1f;
            batch.draw(frame, drawX, drawY, originX, originY, drawW, drawH, scaleX, 1f, 0f);
        }
        if (saturationShader != null) {
            batch.setShader(null);
        }

        if (dayNightOverlayTex == null) {
            return;
        }
        for (int i = 0; i < fifthQuestDemons.size; i++) {
            FifthQuestDemonInstance demon = fifthQuestDemons.get(i);
            float demonDepthBasisY = demon.position.y;
            boolean demonBehindPlayer = demonDepthBasisY > playerDepthBasisY;
            if (demonBehindPlayer != drawBehindPlayer || demon.dying) {
                continue;
            }
            float hpRatio = demon.maxHealth > 0f ? clamp01(demon.health / demon.maxHealth) : 0f;
            float hpX = demon.position.x - BAT_HP_BAR_WIDTH * 0.5f;
            float hpY = demon.position.y + fifthQuestDemonFrameHeight(demon.type) * FIFTH_QUEST_DEMON_DRAW_SCALE * 0.52f;
            batch.setShader(null);
            batch.setColor(0f, 0f, 0f, 0.85f);
            batch.draw(dayNightOverlayTex, hpX - 0.5f, hpY - 0.5f, BAT_HP_BAR_WIDTH + 1f, BAT_HP_BAR_HEIGHT + 1f);
            batch.setColor(0.15f, 0.15f, 0.15f, 1f);
            batch.draw(dayNightOverlayTex, hpX, hpY, BAT_HP_BAR_WIDTH, BAT_HP_BAR_HEIGHT);
            if (hpRatio > 0f) {
                batch.setColor(0.88f, 0.22f, 0.18f, 1f);
                batch.draw(dayNightOverlayTex, hpX, hpY, BAT_HP_BAR_WIDTH * hpRatio, BAT_HP_BAR_HEIGHT);
            }
        }
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void drawFourthQuestSigil(SpriteBatch batch, Rectangle bounds, float r, float g, float b, float alpha) {
        if (batch == null || bounds == null || dayNightOverlayTex == null) {
            return;
        }
        float pulse = 0.82f + 0.18f * (float) Math.sin(firstQuestMarkerTimer * 3.7f);
        batch.setColor(r * 0.34f, g * 0.18f, b * 0.18f, alpha * 0.36f);
        batch.draw(dayNightOverlayTex, bounds.x - 12f, bounds.y - 10f, bounds.width + 24f, bounds.height + 20f);
        batch.setColor(r, g, b, alpha * pulse);
        drawQuestRectOutline(batch, bounds, 3f);
        batch.draw(
                dayNightOverlayTex,
                bounds.x + bounds.width * 0.2f,
                bounds.y + bounds.height * 0.45f,
                bounds.width * 0.6f,
                3f);
        batch.draw(
                dayNightOverlayTex,
                bounds.x + bounds.width * 0.45f,
                bounds.y + bounds.height * 0.15f,
                3f,
                bounds.height * 0.7f);
    }

    private void drawQuestRectOutline(SpriteBatch batch, Rectangle bounds, float thickness) {
        if (batch == null || bounds == null || thickness <= 0f || dayNightOverlayTex == null) {
            return;
        }
        batch.draw(dayNightOverlayTex, bounds.x, bounds.y, bounds.width, thickness);
        batch.draw(dayNightOverlayTex, bounds.x, bounds.y + bounds.height - thickness, bounds.width, thickness);
        batch.draw(dayNightOverlayTex, bounds.x, bounds.y, thickness, bounds.height);
        batch.draw(dayNightOverlayTex, bounds.x + bounds.width - thickness, bounds.y, thickness, bounds.height);
    }

    private void drawFirstQuestUi() {
        if (uiCamera == null || batch == null || dayNightOverlayTex == null) {
            return;
        }
        if (hudRenderer != null && hudRenderer.isMainMenuVisible()) {
            return;
        }

        boolean portalActivationOverlayActive = isSixthQuestPortalActivationRunning();
        boolean questOverlayActive = isFirstQuestOverlayActive();
        boolean bossArenaIntroOverlayActive = isBossArenaFinalIntroFocusActive();
        boolean fifthQuestEncounterOverlayActive = isFifthQuestFirstEncounterIntroActive();

        uiCamera.update();
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        batch.setShader(null);
        batch.setColor(1f, 1f, 1f, 1f);
        if (questOverlayActive || portalActivationOverlayActive || bossArenaIntroOverlayActive || fifthQuestEncounterOverlayActive) {
            batch.setColor(0f, 0f, 0f, FIRST_QUEST_OVERLAY_DIM_ALPHA * clamp01(questBlurProgress));
            batch.draw(dayNightOverlayTex, 0f, 0f, uiCamera.viewportWidth, uiCamera.viewportHeight);
            batch.setColor(1f, 1f, 1f, 1f);
            if (firstQuestDialogueActive) {
                batch.end();
                drawDialogueForegroundActors();
                uiCamera.update();
                batch.setProjectionMatrix(uiCamera.combined);
                batch.begin();
                batch.setShader(null);
                batch.setColor(1f, 1f, 1f, 1f);
            } else if (portalActivationOverlayActive) {
                batch.end();
                drawSixthQuestPortalForegroundActors();
                uiCamera.update();
                batch.setProjectionMatrix(uiCamera.combined);
                batch.begin();
                batch.setShader(null);
                batch.setColor(1f, 1f, 1f, 1f);
            } else if (bossArenaIntroOverlayActive) {
                batch.end();
                drawBossArenaFinalIntroForegroundActors();
                uiCamera.update();
                batch.setProjectionMatrix(uiCamera.combined);
                batch.begin();
                batch.setShader(null);
                batch.setColor(1f, 1f, 1f, 1f);
            } else if (fifthQuestEncounterOverlayActive) {
                batch.end();
                drawFifthQuestFirstEncounterForegroundActors();
                uiCamera.update();
                batch.setProjectionMatrix(uiCamera.combined);
                batch.begin();
                batch.setShader(null);
                batch.setColor(1f, 1f, 1f, 1f);
            }
            if (firstQuestDialogueActive) {
                drawFirstQuestDialogueOverlay(batch);
            } else if (fifthQuestDeathChoiceVisible) {
                drawFifthQuestDeathChoiceOverlay(batch);
            } else if (sixthQuestPortalChoiceVisible) {
                drawSixthQuestPortalChoiceOverlay(batch);
            } else if (firstQuestItemCardVisible) {
                drawFirstQuestItemCardOverlay(batch);
            }
        } else {
            drawFirstQuestMarkers(batch);
            drawFirstQuestPrompt(batch);
        }
        batch.end();
    }

    private void drawSixthQuestPortalForegroundActors() {
        if (batch == null
                || camera == null
                || world.mapRenderer == null
                || !isSixthQuestPortalActivationRunning()
                || !MAP_ID_VILLAGE.equals(world.activeMapId)) {
            return;
        }

        float nightBlend = currentNightBlend();
        float worldSaturation = lerp(1f, NIGHT_WORLD_SATURATION, nightBlend);
        float characterSaturation = lerp(CHARACTER_SATURATION, NIGHT_CHARACTER_SATURATION, nightBlend);

        camera.update();
        world.mapRenderer.setView(camera);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setShader(null);
        drawSixthQuestPortalActivationEffect(characterSaturation);
        if (sixthQuestPortalChoiceVisible && sixthQuestPortalActivationPhase == PortalActivationPhase.LOOP) {
            drawSixthQuestPortalActivationEffect(characterSaturation, 0.55f);
        }
        batch.end();

        int portalLayerIndex = findTopLevelLayerIndexContaining(VILLAGE_PORTAL_GROUP_NAME);
        if (portalLayerIndex >= 0) {
            renderMapWithSaturation(new int[]{portalLayerIndex}, worldSaturation);
        }

        batch.begin();
        batch.setShader(null);
        drawPlayer(characterSaturation);
        batch.end();
    }

    private void drawBossArenaFinalIntroForegroundActors() {
        if (batch == null
                || camera == null
                || world.mapRenderer == null
                || !isBossArenaFinalIntroFocusActive()
                || !MAP_ID_BOSS_ARENA_PHASE_2.equals(world.activeMapId)) {
            return;
        }

        BossArenaPhase2DemonInstance boss = findBossArenaPhase2Leader();
        if (boss == null) {
            return;
        }

        float nightBlend = currentNightBlend();
        float characterSaturation = lerp(CHARACTER_SATURATION, NIGHT_CHARACTER_SATURATION, nightBlend);

        camera.update();
        world.mapRenderer.setView(camera);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setShader(null);
        drawSingleBossArenaPhase2Demon(boss, characterSaturation);
        drawPlayer(characterSaturation);
        batch.end();
    }

    private void drawSingleBossArenaPhase2Demon(BossArenaPhase2DemonInstance demon, float saturation) {
        if (demon == null
                || (bossArenaPhase2DemonIdleAnim == null
                && bossArenaPhase2DemonWalkAnim == null
                && bossArenaPhase2DemonAttackAnim == null
                && bossArenaPhase2DemonHurtAnim == null)) {
            return;
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
        } else if (bossArenaPhase2DemonWalkAnim != null) {
            activeAnim = bossArenaPhase2DemonWalkAnim;
        } else if (bossArenaPhase2DemonAttackAnim != null) {
            activeAnim = bossArenaPhase2DemonAttackAnim;
        } else {
            activeAnim = bossArenaPhase2DemonHurtAnim;
        }

        if (activeAnim == null) {
            return;
        }

        float animationTime;
        if (demon.hurting) {
            animationTime = demon.hurtStateTime;
        } else if (demon.attacking) {
            animationTime = demon.attackStateTime;
        } else {
            animationTime = demon.stateTime;
        }

        TextureRegion frame = demon.attacking && activeAnim == bossArenaPhase2DemonAttackAnim
                ? currentBossArenaPhase2DemonAttackFrame(animationTime)
                : activeAnim.getKeyFrame(animationTime, loop);
        if (frame == null) {
            return;
        }

        if (saturationShader != null) {
            batch.setShader(saturationShader);
            saturationShader.setUniformf("u_saturation", saturation);
        }
        float drawW = frame.getRegionWidth() * BOSS_ARENA_PHASE_2_DEMON_DRAW_SCALE;
        float drawH = frame.getRegionHeight() * BOSS_ARENA_PHASE_2_DEMON_DRAW_SCALE;
        float drawX = demon.position.x - drawW * 0.5f;
        float drawY = demon.position.y - drawH * 0.5f;
        float originX = drawW * 0.5f;
        float originY = drawH * 0.5f;
        float scaleX = demon.facingLeft ? 1f : -1f;
        batch.draw(frame, drawX, drawY, originX, originY, drawW, drawH, scaleX, 1f, 0f);
        if (saturationShader != null) {
            batch.setShader(null);
        }
    }

    private void drawFifthQuestFirstEncounterForegroundActors() {
        if (batch == null
                || camera == null
                || world.mapRenderer == null
                || !isFifthQuestFirstEncounterIntroActive()
                || !MAP_ID_VILLAGE.equals(world.activeMapId)) {
            return;
        }

        float nightBlend = currentNightBlend();
        float characterSaturation = lerp(CHARACTER_SATURATION, NIGHT_CHARACTER_SATURATION, nightBlend);

        camera.update();
        world.mapRenderer.setView(camera);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setShader(null);

        if (shadowRegion != null) {
            float shadowW = FIFTH_QUEST_DEMON_SHADOW_WIDTH_TILES * DEFAULT_TILE_SIZE;
            float shadowH = FIFTH_QUEST_DEMON_SHADOW_HEIGHT_TILES * DEFAULT_TILE_SIZE;
            float shadowDownOffset = FIFTH_QUEST_DEMON_SHADOW_DOWN_OFFSET_TILES * DEFAULT_TILE_SIZE;
            batch.setColor(1f, 1f, 1f, FIFTH_QUEST_DEMON_SHADOW_ALPHA);
            for (int i = 0; i < fifthQuestDemons.size; i++) {
                FifthQuestDemonInstance demon = fifthQuestDemons.get(i);
                if (demon.questEncounterId != fifthQuestFirstEncounterIntroEncounterId || demon.dying || demon.health <= 0f) {
                    continue;
                }
                batch.draw(
                        shadowRegion,
                        demon.position.x - shadowW * 0.5f,
                        demon.position.y - shadowH * 0.25f - shadowDownOffset,
                        shadowW,
                        shadowH);
            }
            batch.setColor(1f, 1f, 1f, 1f);
        }

        if (saturationShader != null) {
            batch.setShader(saturationShader);
            saturationShader.setUniformf("u_saturation", characterSaturation);
        }
        for (int i = 0; i < fifthQuestDemons.size; i++) {
            FifthQuestDemonInstance demon = fifthQuestDemons.get(i);
            if (demon.questEncounterId != fifthQuestFirstEncounterIntroEncounterId || demon.dying || demon.health <= 0f) {
                continue;
            }

            Animation<TextureRegion> activeAnimation;
            boolean loop = true;
            float animationTime;
            if (demon.hurting && fifthQuestDemonHurtAnimation(demon.type) != null) {
                activeAnimation = fifthQuestDemonHurtAnimation(demon.type);
                animationTime = demon.hurtStateTime;
                loop = false;
            } else if (demon.attacking && fifthQuestDemonAttackAnimation(demon.type) != null) {
                activeAnimation = fifthQuestDemonAttackAnimation(demon.type);
                animationTime = demon.attackStateTime;
                loop = false;
            } else if (demon.moving && fifthQuestDemonWalkAnimation(demon.type) != null) {
                activeAnimation = fifthQuestDemonWalkAnimation(demon.type);
                animationTime = demon.stateTime;
            } else {
                activeAnimation = fifthQuestDemonIdleAnimation(demon.type);
                animationTime = demon.stateTime;
            }
            if (activeAnimation == null) {
                continue;
            }
            TextureRegion frame = activeAnimation.getKeyFrame(animationTime, loop);
            if (frame == null) {
                continue;
            }
            float drawW = frame.getRegionWidth() * FIFTH_QUEST_DEMON_DRAW_SCALE;
            float drawH = frame.getRegionHeight() * FIFTH_QUEST_DEMON_DRAW_SCALE;
            float drawX = demon.position.x - drawW * 0.5f;
            float drawY = demon.position.y - drawH * 0.5f;
            float originX = drawW * 0.5f;
            float originY = drawH * 0.5f;
            float scaleX = demon.facingLeft ? -1f : 1f;
            batch.draw(frame, drawX, drawY, originX, originY, drawW, drawH, scaleX, 1f, 0f);
        }
        if (saturationShader != null) {
            batch.setShader(null);
        }
        batch.end();
    }

    private void drawDialogueForegroundActors() {
        if (batch == null || camera == null || world.mapRenderer == null || !firstQuestDialogueActive) {
            return;
        }

        float nightBlend = currentNightBlend();
        float worldSaturation = lerp(1f, NIGHT_WORLD_SATURATION, nightBlend);
        float characterSaturation = lerp(CHARACTER_SATURATION, NIGHT_CHARACTER_SATURATION, nightBlend);

        camera.update();
        world.mapRenderer.setView(camera);

        int speakerLayerIndex = activeDialogueSpeakerTopLevelLayerIndex();
        if (speakerLayerIndex >= 0) {
            renderMapWithSaturation(new int[]{speakerLayerIndex}, worldSaturation);
        }

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setShader(null);
        if (activeQuestDialogueId == QuestDialogueId.FIFTH_QUEST_LAB_ALARM) {
            drawFifthQuestLabVillagers(characterSaturation, true);
        }
        drawRuntimeDialogueSpeaker(characterSaturation);
        drawPlayer(characterSaturation);
        if (activeQuestDialogueId == QuestDialogueId.FIFTH_QUEST_LAB_ALARM) {
            drawFifthQuestLabVillagers(characterSaturation, false);
        }
        batch.end();
    }

    private int activeDialogueSpeakerTopLevelLayerIndex() {
        if (world.map == null || activeQuestDialogueId == null || activeQuestDialogueId == QuestDialogueId.NONE) {
            return -1;
        }
        return switch (activeQuestDialogueId) {
            case FIRST_QUEST_FATHER, THIRD_QUEST_FATHER_RETURN -> findTopLevelLayerIndexContaining(FIRST_QUEST_FATHER_LAYER_NAME);
            case SECOND_QUEST_SCIENTIST_INTRO,
                    SECOND_QUEST_SCIENTIST_RETURN,
                    THIRD_QUEST_SCIENTIST_SENDOFF,
                    FOURTH_QUEST_SCIENTIST_REPORT,
                    FOURTH_QUEST_SAMPLE_ANALYSIS,
                    FOURTH_QUEST_SPATIAL_UNLOCK,
                    FOURTH_QUEST_EPILOGUE,
                    FIFTH_QUEST_LAB_ALARM,
                    SIXTH_QUEST_PORTAL_BRIEFING -> activeScientistTopLevelLayerIndex();
            default -> -1;
        };
    }

    private int activeScientistTopLevelLayerIndex() {
        if (world.npcAnimationFrames.size == 0
                || world.npcAnimationFrameIndex < 0
                || world.npcAnimationFrameIndex >= world.npcAnimationFrames.size) {
            return -1;
        }
        return findTopLevelLayerIndexContaining(world.npcAnimationFrames.get(world.npcAnimationFrameIndex));
    }

    private void drawRuntimeDialogueSpeaker(float characterSaturation) {
        if (activeQuestDialogueId == null) {
            return;
        }
        switch (activeQuestDialogueId) {
            case SECOND_QUEST_CAFE_OWNER, FOURTH_QUEST_CAFE_OWNER -> {
                drawFirstQuestCafeNpc(characterSaturation, true);
                drawFirstQuestCafeNpc(characterSaturation, false);
            }
            case THIRD_QUEST_FATHER_TRAINING_INTRO,
                    THIRD_QUEST_FATHER_AFTER_MOVEMENT,
                    THIRD_QUEST_FATHER_AFTER_ATTACK,
                    THIRD_QUEST_FATHER_PRE_SPAR,
                    THIRD_QUEST_FATHER_COMPLETE,
                    FOURTH_QUEST_INTERRUPTION -> drawThirdQuestVillageFather(batch);
            case FIFTH_QUEST_PORTAL_AFTER, SIXTH_QUEST_PORTAL_BRIEFING -> {
                drawFifthQuestPortalDialogueScientist(characterSaturation);
                drawFifthQuestPortalDialogueFather(batch);
            }
            case SIXTH_QUEST_ENDING -> {
                drawSixthQuestEndingScientist(characterSaturation);
                drawSixthQuestEndingFather(batch);
            }
            default -> {
            }
        }
    }

    private int findTopLevelLayerIndexContaining(String layerName) {
        if (world.map == null || layerName == null || layerName.isBlank()) {
            return -1;
        }
        MapLayers topLevelLayers = world.map.getLayers();
        for (int i = 0; i < topLevelLayers.getCount(); i++) {
            MapLayer layer = topLevelLayers.get(i);
            if (layerName.equalsIgnoreCase(layer.getName())) {
                return i;
            }
            if (layer instanceof MapGroupLayer groupLayer
                    && findLayerByName(groupLayer.getLayers(), layerName) != null) {
                return i;
            }
        }
        return -1;
    }

    private int findTopLevelLayerIndexContaining(MapLayer targetLayer) {
        if (world.map == null || targetLayer == null) {
            return -1;
        }
        MapLayers topLevelLayers = world.map.getLayers();
        for (int i = 0; i < topLevelLayers.getCount(); i++) {
            MapLayer layer = topLevelLayers.get(i);
            if (layer == targetLayer) {
                return i;
            }
            if (layer instanceof MapGroupLayer groupLayer
                    && containsLayerRecursive(groupLayer.getLayers(), targetLayer)) {
                return i;
            }
        }
        return -1;
    }

    private boolean containsLayerRecursive(MapLayers layers, MapLayer targetLayer) {
        if (layers == null || targetLayer == null) {
            return false;
        }
        for (MapLayer layer : layers) {
            if (layer == targetLayer) {
                return true;
            }
            if (layer instanceof MapGroupLayer groupLayer
                    && containsLayerRecursive(groupLayer.getLayers(), targetLayer)) {
                return true;
            }
        }
        return false;
    }

    private void drawDialogueForegroundHighlights(SpriteBatch batch) {
        if (batch == null
                || !firstQuestDialogueActive
                || uiCamera == null
                || camera == null
                || questBlurFrameRegion == null) {
            return;
        }

        boolean hasCombinedBounds = projectDialogueHighlightWorldBounds(player.bounds, dialogueHighlightCombinedScreenBounds);
        if (resolveActiveDialogueHighlightWorldBounds(dialogueHighlightWorldBounds)
                && projectDialogueHighlightWorldBounds(dialogueHighlightWorldBounds, dialogueHighlightScreenBounds)) {
            if (hasCombinedBounds) {
                mergeRectangles(dialogueHighlightCombinedScreenBounds, dialogueHighlightScreenBounds);
            } else {
                dialogueHighlightCombinedScreenBounds.set(dialogueHighlightScreenBounds);
                hasCombinedBounds = true;
            }
        }

        if (!hasCombinedBounds || !clampScreenBoundsToUi(dialogueHighlightCombinedScreenBounds)) {
            return;
        }

        Texture blurSourceTexture = questBlurFrameRegion.getTexture();
        if (blurSourceTexture == null) {
            return;
        }

        float uSpan = questBlurFrameRegion.getU2() - questBlurFrameRegion.getU();
        float vSpan = questBlurFrameRegion.getV2() - questBlurFrameRegion.getV();
        float u1 = questBlurFrameRegion.getU()
                + (dialogueHighlightCombinedScreenBounds.x / uiCamera.viewportWidth) * uSpan;
        float u2 = questBlurFrameRegion.getU()
                + ((dialogueHighlightCombinedScreenBounds.x + dialogueHighlightCombinedScreenBounds.width)
                / uiCamera.viewportWidth) * uSpan;
        float v1 = questBlurFrameRegion.getV()
                + (dialogueHighlightCombinedScreenBounds.y / uiCamera.viewportHeight) * vSpan;
        float v2 = questBlurFrameRegion.getV()
                + ((dialogueHighlightCombinedScreenBounds.y + dialogueHighlightCombinedScreenBounds.height)
                / uiCamera.viewportHeight) * vSpan;

        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(
                blurSourceTexture,
                dialogueHighlightCombinedScreenBounds.x,
                dialogueHighlightCombinedScreenBounds.y,
                dialogueHighlightCombinedScreenBounds.width,
                dialogueHighlightCombinedScreenBounds.height,
                u1,
                v1,
                u2,
                v2);
    }

    private boolean resolveActiveDialogueHighlightWorldBounds(Rectangle out) {
        if (out == null || activeQuestDialogueId == null || activeQuestDialogueId == QuestDialogueId.NONE) {
            return false;
        }
        return switch (activeQuestDialogueId) {
            case FIRST_QUEST_FATHER, THIRD_QUEST_FATHER_RETURN -> setRectangle(out, firstQuestFatherBounds());
            case SECOND_QUEST_CAFE_OWNER, FOURTH_QUEST_CAFE_OWNER -> setRectangle(out, firstQuestCafeNpcVisualBounds());
            case SECOND_QUEST_SCIENTIST_INTRO,
                    SECOND_QUEST_SCIENTIST_RETURN,
                    THIRD_QUEST_SCIENTIST_SENDOFF,
                    FOURTH_QUEST_SCIENTIST_REPORT,
                    FOURTH_QUEST_SAMPLE_ANALYSIS,
                    FOURTH_QUEST_SPATIAL_UNLOCK,
                    FOURTH_QUEST_EPILOGUE,
                    FIFTH_QUEST_LAB_ALARM -> setRectangle(out, scientistNpcBounds());
            case THIRD_QUEST_FATHER_TRAINING_INTRO,
                    THIRD_QUEST_FATHER_AFTER_MOVEMENT,
                    THIRD_QUEST_FATHER_AFTER_ATTACK,
                    THIRD_QUEST_FATHER_PRE_SPAR,
                    THIRD_QUEST_FATHER_COMPLETE,
                    FOURTH_QUEST_INTERRUPTION -> setRectangleToThirdQuestFather(out);
            case FIFTH_QUEST_PORTAL_AFTER, SIXTH_QUEST_PORTAL_BRIEFING -> setRectangleToFifthQuestPortalDialogueActors(out);
            case SIXTH_QUEST_ENDING -> setRectangle(out, sixthQuestEndingFatherBounds());
            default -> setRectangleToDialogueFocusFallback(out);
        };
    }

    private Rectangle firstQuestCafeNpcVisualBounds() {
        if (!MAP_ID_VILLAGE.equals(world.activeMapId) || firstQuestCafeNpcIdleAnim == null) {
            return null;
        }
        Vector2 npcPosition = firstQuestCafeNpcPosition();
        if (npcPosition == null) {
            return null;
        }
        TextureRegion frame = firstQuestCafeNpcIdleAnim.getKeyFrame(firstQuestCafeNpcStateTime, true);
        if (frame == null) {
            return null;
        }
        float drawW = frame.getRegionWidth() * FIRST_QUEST_CAFE_NPC_DRAW_SCALE;
        float drawH = frame.getRegionHeight() * FIRST_QUEST_CAFE_NPC_DRAW_SCALE;
        dialogueHighlightWorldBounds.set(
                npcPosition.x - drawW * 0.5f,
                npcPosition.y - drawH * 0.5f,
                drawW,
                drawH);
        return dialogueHighlightWorldBounds;
    }

    private boolean setRectangleToThirdQuestFather(Rectangle out) {
        ensureThirdQuestTrainingLayout();
        return setRectangle(out, thirdQuestFatherTrainingBounds);
    }

    private boolean setRectangleToDialogueFocusFallback(Rectangle out) {
        if (out == null || !resolveActiveDialogueFocusPoint(dialogueCameraFocusPoint)) {
            return false;
        }
        out.set(
                dialogueCameraFocusPoint.x - DIALOGUE_FOREGROUND_FALLBACK_TARGET_WIDTH * 0.5f,
                dialogueCameraFocusPoint.y - DIALOGUE_FOREGROUND_FALLBACK_TARGET_HEIGHT * 0.5f,
                DIALOGUE_FOREGROUND_FALLBACK_TARGET_WIDTH,
                DIALOGUE_FOREGROUND_FALLBACK_TARGET_HEIGHT);
        return true;
    }

    private boolean setRectangle(Rectangle out, Rectangle source) {
        if (out == null || source == null || source.width <= 0f || source.height <= 0f) {
            return false;
        }
        out.set(source);
        return true;
    }

    private Rectangle fifthQuestPortalDialogueCombinedBounds() {
        Rectangle fatherBounds = fifthQuestPortalDialogueFatherBounds();
        Rectangle scientistBounds = fifthQuestPortalDialogueScientistBounds();
        if (fatherBounds == null && scientistBounds == null) {
            return null;
        }
        if (fatherBounds == null) {
            fifthQuestPortalDialogueCombinedBounds.set(scientistBounds);
            return fifthQuestPortalDialogueCombinedBounds;
        }
        if (scientistBounds == null) {
            fifthQuestPortalDialogueCombinedBounds.set(fatherBounds);
            return fifthQuestPortalDialogueCombinedBounds;
        }
        float minX = Math.min(fatherBounds.x, scientistBounds.x);
        float minY = Math.min(fatherBounds.y, scientistBounds.y);
        float maxX = Math.max(fatherBounds.x + fatherBounds.width, scientistBounds.x + scientistBounds.width);
        float maxY = Math.max(fatherBounds.y + fatherBounds.height, scientistBounds.y + scientistBounds.height);
        fifthQuestPortalDialogueCombinedBounds.set(minX, minY, maxX - minX, maxY - minY);
        return fifthQuestPortalDialogueCombinedBounds;
    }

    private boolean setRectangleToFifthQuestPortalDialogueActors(Rectangle out) {
        if (out == null) {
            return false;
        }
        return setRectangle(out, fifthQuestPortalDialogueCombinedBounds());
    }

    private boolean projectDialogueHighlightWorldBounds(Rectangle worldBounds, Rectangle screenBounds) {
        if (worldBounds == null
                || screenBounds == null
                || camera == null
                || uiCamera == null
                || worldBounds.width <= 0f
                || worldBounds.height <= 0f) {
            return false;
        }
        dialogueHighlightProjectionMin.set(worldBounds.x, worldBounds.y, 0f);
        dialogueHighlightProjectionMax.set(worldBounds.x + worldBounds.width, worldBounds.y + worldBounds.height, 0f);
        camera.project(dialogueHighlightProjectionMin);
        camera.project(dialogueHighlightProjectionMax);
        float minX = Math.min(dialogueHighlightProjectionMin.x, dialogueHighlightProjectionMax.x) - DIALOGUE_FOREGROUND_SCREEN_PADDING_X;
        float minY = Math.min(dialogueHighlightProjectionMin.y, dialogueHighlightProjectionMax.y) - DIALOGUE_FOREGROUND_SCREEN_PADDING_Y;
        float maxX = Math.max(dialogueHighlightProjectionMin.x, dialogueHighlightProjectionMax.x) + DIALOGUE_FOREGROUND_SCREEN_PADDING_X;
        float maxY = Math.max(dialogueHighlightProjectionMin.y, dialogueHighlightProjectionMax.y) + DIALOGUE_FOREGROUND_SCREEN_PADDING_Y;
        screenBounds.set(minX, minY, maxX - minX, maxY - minY);
        return screenBounds.width > 0f && screenBounds.height > 0f;
    }

    private void mergeRectangles(Rectangle base, Rectangle addition) {
        if (base == null || addition == null) {
            return;
        }
        float minX = Math.min(base.x, addition.x);
        float minY = Math.min(base.y, addition.y);
        float maxX = Math.max(base.x + base.width, addition.x + addition.width);
        float maxY = Math.max(base.y + base.height, addition.y + addition.height);
        base.set(minX, minY, maxX - minX, maxY - minY);
    }

    private boolean clampScreenBoundsToUi(Rectangle bounds) {
        if (bounds == null || uiCamera == null) {
            return false;
        }
        float minX = Math.max(0f, bounds.x);
        float minY = Math.max(0f, bounds.y);
        float maxX = Math.min(uiCamera.viewportWidth, bounds.x + bounds.width);
        float maxY = Math.min(uiCamera.viewportHeight, bounds.y + bounds.height);
        if (maxX <= minX || maxY <= minY) {
            return false;
        }
        bounds.set(minX, minY, maxX - minX, maxY - minY);
        return true;
    }

    private void drawFirstQuestMarkers(SpriteBatch batch) {
        if (firstQuestTitleFont == null || camera == null || world.activeMapId == null) {
            return;
        }
        float bobOffset = (float) Math.sin(firstQuestMarkerTimer * FIRST_QUEST_MARKER_BOB_SPEED)
                * FIRST_QUEST_MARKER_BOB_AMPLITUDE;
        if (firstQuestStage == FIRST_QUEST_TALK_TO_FATHER && MAP_ID_INN_ROOM.equals(world.activeMapId)) {
            Rectangle fatherBounds = firstQuestFatherBounds();
            if (fatherBounds != null) {
                drawQuestMarker(batch,
                        fatherBounds.x + fatherBounds.width * 0.5f,
                        fatherBounds.y + fatherBounds.height + 22f + bobOffset,
                        "!",
                        false,
                        0.98f, 0.85f, 0.35f);
            }
            return;
        }
        if (firstQuestStage == FIRST_QUEST_DELIVER_TO_CAFE && MAP_ID_VILLAGE.equals(world.activeMapId)) {
            Vector2 cafeNpcPosition = firstQuestCafeNpcPosition();
            if (cafeNpcPosition != null) {
                drawQuestMarker(batch,
                        cafeNpcPosition.x,
                        cafeNpcPosition.y + 36f + bobOffset,
                        "v",
                        true,
                        0.90f, 0.80f, 0.34f);
            }
            return;
        }
        if (secondQuestStage == SECOND_QUEST_TALK_TO_CAFE_OWNER && MAP_ID_VILLAGE.equals(world.activeMapId)) {
            Vector2 cafeNpcPosition = firstQuestCafeNpcPosition();
            if (cafeNpcPosition != null) {
                drawQuestMarker(batch,
                        cafeNpcPosition.x,
                        cafeNpcPosition.y + 36f + bobOffset,
                        "!",
                        false,
                        0.98f, 0.85f, 0.35f);
            }
            return;
        }
        if (secondQuestStage == SECOND_QUEST_TAKE_FRAGMENT_TO_SCIENTIST) {
            if (MAP_ID_VILLAGE.equals(world.activeMapId)) {
                drawQuestMarkerForObjectLayer(batch, "ENTRANCE-Research-Lab", bobOffset, true);
                return;
            }
            if (MAP_ID_RESEARCH_LAB_FLOOR_1.equals(world.activeMapId)) {
                Rectangle scientistBounds = scientistNpcBounds();
                if (scientistBounds != null) {
                    drawQuestMarker(batch,
                            scientistBounds.x + scientistBounds.width * 0.5f,
                            scientistBounds.y + scientistBounds.height + 22f + bobOffset,
                            "!",
                            false,
                            0.98f, 0.85f, 0.35f);
                }
                return;
            }
        }
        if (secondQuestStage == SECOND_QUEST_RETRIEVE_ARCHIVE_NOTES) {
            if (MAP_ID_RESEARCH_LAB_FLOOR_1.equals(world.activeMapId)) {
                drawQuestMarkerForObjectLayer(batch, "ENTRANCE-Research-Lab-Floor-2", bobOffset, true);
                return;
            }
            if (MAP_ID_RESEARCH_LAB_FLOOR_2.equals(world.activeMapId)) {
                Rectangle archiveBounds = secondQuestArchiveBounds();
                if (archiveBounds != null) {
                    drawQuestMarker(batch,
                            archiveBounds.x + archiveBounds.width * 0.5f,
                            archiveBounds.y + archiveBounds.height + 18f + bobOffset,
                            "!",
                            false,
                            0.98f, 0.85f, 0.35f);
                }
                return;
            }
        }
        if (secondQuestStage == SECOND_QUEST_RETURN_TO_SCIENTIST) {
            if (MAP_ID_RESEARCH_LAB_FLOOR_2.equals(world.activeMapId)) {
                drawQuestMarkerForObjectLayer(batch, "EXIT-Research-Lab-Floor-2", bobOffset, true);
                return;
            }
            if (MAP_ID_RESEARCH_LAB_FLOOR_1.equals(world.activeMapId)) {
                Rectangle scientistBounds = scientistNpcBounds();
                if (scientistBounds != null) {
                    drawQuestMarker(batch,
                            scientistBounds.x + scientistBounds.width * 0.5f,
                            scientistBounds.y + scientistBounds.height + 22f + bobOffset,
                            "!",
                            false,
                            0.98f, 0.85f, 0.35f);
                }
            }
            return;
        }
        if (thirdQuestStage == THIRD_QUEST_RETURN_TO_FATHER) {
            if (MAP_ID_RESEARCH_LAB_FLOOR_1.equals(world.activeMapId)) {
                drawQuestMarkerForObjectLayer(batch, THIRD_QUEST_LAB_EXIT_LAYER_NAME, bobOffset, true);
                return;
            }
            if (MAP_ID_VILLAGE.equals(world.activeMapId)) {
                drawQuestMarkerForObjectLayer(batch, THIRD_QUEST_INN_ENTRANCE_LAYER_NAME, bobOffset, true);
                return;
            }
            if (MAP_ID_INN.equals(world.activeMapId)) {
                drawQuestMarkerForObjectLayer(batch, THIRD_QUEST_INN_ROOM_ENTRANCE_LAYER_NAME, bobOffset, true);
                return;
            }
            if (MAP_ID_INN_ROOM.equals(world.activeMapId)) {
                Rectangle fatherBounds = firstQuestFatherBounds();
                if (fatherBounds != null) {
                    drawQuestMarker(batch,
                            fatherBounds.x + fatherBounds.width * 0.5f,
                            fatherBounds.y + fatherBounds.height + 22f + bobOffset,
                            "!",
                            false,
                            0.98f, 0.85f, 0.35f);
                }
                return;
            }
        }
        if (thirdQuestStage == THIRD_QUEST_MEET_FATHER_OUTSIDE) {
            if (MAP_ID_INN_ROOM.equals(world.activeMapId)) {
                drawQuestMarkerForObjectLayer(batch, "EXIT-Inn-Room", bobOffset, true);
                return;
            }
            if (MAP_ID_INN.equals(world.activeMapId)) {
                drawQuestMarkerForObjectLayer(batch, "EXIT-Inn", bobOffset, true);
                return;
            }
            if (MAP_ID_VILLAGE.equals(world.activeMapId)) {
                ensureThirdQuestTrainingLayout();
                if (thirdQuestTrainingLayoutReady) {
                    drawQuestMarker(batch,
                            thirdQuestFatherTrainingBounds.x + thirdQuestFatherTrainingBounds.width * 0.5f,
                            thirdQuestFatherTrainingBounds.y + thirdQuestFatherTrainingBounds.height + 22f + bobOffset,
                            "v",
                            true,
                            0.98f, 0.85f, 0.35f);
                }
                return;
            }
        }
        if ((thirdQuestStage == THIRD_QUEST_MOVEMENT_TUTORIAL
                || thirdQuestStage == THIRD_QUEST_ATTACK_TUTORIAL
                || thirdQuestStage == THIRD_QUEST_COMBAT_TUTORIAL
                || thirdQuestStage == THIRD_QUEST_SPAR_WITH_FATHER)
                && MAP_ID_VILLAGE.equals(world.activeMapId)) {
            ensureThirdQuestTrainingLayout();
            if (!thirdQuestTrainingLayoutReady) {
                return;
            }
            if (thirdQuestStage == THIRD_QUEST_MOVEMENT_TUTORIAL) {
                for (int i = 0; i < thirdQuestMovementMarkers.length; i++) {
                    if (thirdQuestMovementMarkerReached[i]) {
                        continue;
                    }
                    drawQuestMarker(batch,
                            thirdQuestMovementMarkers[i].x,
                            thirdQuestMovementMarkers[i].y + 16f + bobOffset,
                            "!",
                            false,
                            0.72f, 0.95f, 0.48f);
                }
                return;
            }
            if (thirdQuestStage == THIRD_QUEST_ATTACK_TUTORIAL) {
                drawQuestMarker(batch,
                        thirdQuestAttackDummyBounds.x + thirdQuestAttackDummyBounds.width * 0.5f,
                        thirdQuestAttackDummyBounds.y + thirdQuestAttackDummyBounds.height + 18f + bobOffset,
                        "!",
                        false,
                        0.98f, 0.85f, 0.35f);
                return;
            }
            if (thirdQuestStage == THIRD_QUEST_COMBAT_TUTORIAL) {
                return;
            }
            drawQuestMarker(batch,
                    thirdQuestFatherTrainingBounds.x + thirdQuestFatherTrainingBounds.width * 0.5f,
                    thirdQuestFatherTrainingBounds.y + thirdQuestFatherTrainingBounds.height + 22f + bobOffset,
                    "!",
                    false,
                    0.98f, 0.85f, 0.35f);
        }
        if (fourthQuestStage == FOURTH_QUEST_REPORT_TO_SCIENTIST) {
            if (MAP_ID_VILLAGE.equals(world.activeMapId)) {
                drawQuestMarkerForObjectLayer(batch, FOURTH_QUEST_LAB_ENTRANCE_LAYER_NAME, bobOffset, true);
                return;
            }
            if (MAP_ID_RESEARCH_LAB_FLOOR_1.equals(world.activeMapId)) {
                Rectangle scientistBounds = scientistNpcBounds();
                if (scientistBounds != null) {
                    drawQuestMarker(batch,
                            scientistBounds.x + scientistBounds.width * 0.5f,
                            scientistBounds.y + scientistBounds.height + 22f + bobOffset,
                            "!",
                            false,
                            0.98f, 0.85f, 0.35f);
                }
                return;
            }
        }
        if (fourthQuestStage == FOURTH_QUEST_TALK_TO_CAFE_OWNER && MAP_ID_VILLAGE.equals(world.activeMapId)) {
            Vector2 cafeNpcPosition = firstQuestCafeNpcPosition();
            if (cafeNpcPosition != null) {
                drawQuestMarker(batch,
                        cafeNpcPosition.x,
                        cafeNpcPosition.y + 36f + bobOffset,
                        "!",
                        false,
                        0.98f, 0.85f, 0.35f);
            }
            return;
        }
        if ((fourthQuestStage == FOURTH_QUEST_EXAMINE_CAFE_MARK
                || fourthQuestStage == FOURTH_QUEST_CAFE_BATTLE)
                && MAP_ID_VILLAGE.equals(world.activeMapId)) {
            ensureFourthQuestVillageLayout();
            drawQuestMarker(batch,
                    fourthQuestCafeSiteBounds.x + fourthQuestCafeSiteBounds.width * 0.5f,
                    fourthQuestCafeSiteBounds.y + fourthQuestCafeSiteBounds.height + 18f + bobOffset,
                    "!",
                    false,
                    0.92f, 0.48f, 0.28f);
            return;
        }
        if ((fourthQuestStage == FOURTH_QUEST_EXAMINE_CENTER_MARK
                || fourthQuestStage == FOURTH_QUEST_CENTER_BATTLE)
                && MAP_ID_VILLAGE.equals(world.activeMapId)) {
            ensureFourthQuestVillageLayout();
            drawQuestMarker(batch,
                    fourthQuestCenterSiteBounds.x + fourthQuestCenterSiteBounds.width * 0.5f,
                    fourthQuestCenterSiteBounds.y + fourthQuestCenterSiteBounds.height + 18f + bobOffset,
                    "!",
                    false,
                    0.92f, 0.48f, 0.28f);
            return;
        }
        if ((fourthQuestStage == FOURTH_QUEST_EXAMINE_EDGE_MARK
                || fourthQuestStage == FOURTH_QUEST_EDGE_BATTLE)
                && MAP_ID_VILLAGE.equals(world.activeMapId)) {
            ensureFourthQuestVillageLayout();
            drawQuestMarker(batch,
                    fourthQuestEdgeSiteBounds.x + fourthQuestEdgeSiteBounds.width * 0.5f,
                    fourthQuestEdgeSiteBounds.y + fourthQuestEdgeSiteBounds.height + 18f + bobOffset,
                    "!",
                    false,
                    0.92f, 0.48f, 0.28f);
            return;
        }
        if (fourthQuestStage == FOURTH_QUEST_RETURN_TO_SCIENTIST) {
            if (MAP_ID_VILLAGE.equals(world.activeMapId)) {
                drawQuestMarkerForObjectLayer(batch, FOURTH_QUEST_LAB_ENTRANCE_LAYER_NAME, bobOffset, true);
                return;
            }
            if (MAP_ID_RESEARCH_LAB_FLOOR_1.equals(world.activeMapId)) {
                Rectangle scientistBounds = scientistNpcBounds();
                if (scientistBounds != null) {
                    drawQuestMarker(batch,
                            scientistBounds.x + scientistBounds.width * 0.5f,
                            scientistBounds.y + scientistBounds.height + 22f + bobOffset,
                            "!",
                            false,
                            0.98f, 0.85f, 0.35f);
                }
                return;
            }
        }
        if (fourthQuestStage == FOURTH_QUEST_GO_TO_ARRAY) {
            if (MAP_ID_RESEARCH_LAB_FLOOR_1.equals(world.activeMapId)) {
                drawQuestMarkerForObjectLayer(batch, FOURTH_QUEST_LAB_FLOOR_2_LAYER_NAME, bobOffset, true);
                return;
            }
            if (MAP_ID_RESEARCH_LAB_FLOOR_2.equals(world.activeMapId)) {
                ensureFourthQuestFloor2Layout();
                drawQuestMarker(batch,
                        fourthQuestArrayNodeBounds[0].x + fourthQuestArrayNodeBounds[0].width * 0.5f,
                        fourthQuestArrayNodeBounds[0].y + fourthQuestArrayNodeBounds[0].height + 18f + bobOffset,
                        "!",
                        false,
                        0.64f, 0.94f, 0.98f);
                return;
            }
        }
        if (fourthQuestStage == FOURTH_QUEST_ACTIVATE_ARRAY_NODE_2 && MAP_ID_RESEARCH_LAB_FLOOR_2.equals(world.activeMapId)) {
            ensureFourthQuestFloor2Layout();
            drawQuestMarker(batch,
                    fourthQuestArrayNodeBounds[1].x + fourthQuestArrayNodeBounds[1].width * 0.5f,
                    fourthQuestArrayNodeBounds[1].y + fourthQuestArrayNodeBounds[1].height + 18f + bobOffset,
                    "!",
                    false,
                    0.64f, 0.94f, 0.98f);
            return;
        }
        if (fourthQuestStage == FOURTH_QUEST_ACTIVATE_ARRAY_NODE_3 && MAP_ID_RESEARCH_LAB_FLOOR_2.equals(world.activeMapId)) {
            ensureFourthQuestFloor2Layout();
            drawQuestMarker(batch,
                    fourthQuestArrayNodeBounds[2].x + fourthQuestArrayNodeBounds[2].width * 0.5f,
                    fourthQuestArrayNodeBounds[2].y + fourthQuestArrayNodeBounds[2].height + 18f + bobOffset,
                    "!",
                    false,
                    0.64f, 0.94f, 0.98f);
            return;
        }
        if ((fourthQuestStage == FOURTH_QUEST_ACTIVATE_ARRAY_CONSOLE
                || fourthQuestStage == FOURTH_QUEST_ARRAY_DEFENSE)
                && MAP_ID_RESEARCH_LAB_FLOOR_2.equals(world.activeMapId)) {
            ensureFourthQuestFloor2Layout();
            drawQuestMarker(batch,
                    fourthQuestArrayConsoleBounds.x + fourthQuestArrayConsoleBounds.width * 0.5f,
                    fourthQuestArrayConsoleBounds.y + fourthQuestArrayConsoleBounds.height + 18f + bobOffset,
                    "!",
                    false,
                    0.64f, 0.94f, 0.98f);
            return;
        }
        if (fourthQuestStage == FOURTH_QUEST_GO_TO_BREACH) {
            if (MAP_ID_RESEARCH_LAB_FLOOR_2.equals(world.activeMapId)) {
                drawQuestMarkerForObjectLayer(batch, FOURTH_QUEST_LAB_FLOOR_2_EXIT_LAYER_NAME, bobOffset, true);
                return;
            }
            if (MAP_ID_RESEARCH_LAB_FLOOR_1.equals(world.activeMapId)) {
                drawQuestMarkerForObjectLayer(batch, THIRD_QUEST_LAB_EXIT_LAYER_NAME, bobOffset, true);
                return;
            }
            if (MAP_ID_VILLAGE.equals(world.activeMapId)) {
                ensureFourthQuestVillageLayout();
                drawQuestMarker(batch,
                        fourthQuestBreachSiteBounds.x + fourthQuestBreachSiteBounds.width * 0.5f,
                        fourthQuestBreachSiteBounds.y + fourthQuestBreachSiteBounds.height + 18f + bobOffset,
                        "!",
                        false,
                        0.96f, 0.42f, 0.34f);
                return;
            }
        }
        if ((fourthQuestStage == FOURTH_QUEST_EXAMINE_BREACH
                || fourthQuestStage == FOURTH_QUEST_BREACH_BATTLE)
                && MAP_ID_VILLAGE.equals(world.activeMapId)) {
            ensureFourthQuestVillageLayout();
            drawQuestMarker(batch,
                    fourthQuestBreachSiteBounds.x + fourthQuestBreachSiteBounds.width * 0.5f,
                    fourthQuestBreachSiteBounds.y + fourthQuestBreachSiteBounds.height + 18f + bobOffset,
                    "!",
                    false,
                    0.96f, 0.42f, 0.34f);
            return;
        }
        if (fourthQuestStage == FOURTH_QUEST_RETURN_RIFT_CORE) {
            if (MAP_ID_VILLAGE.equals(world.activeMapId)) {
                drawQuestMarkerForObjectLayer(batch, FOURTH_QUEST_LAB_ENTRANCE_LAYER_NAME, bobOffset, true);
                return;
            }
            if (MAP_ID_RESEARCH_LAB_FLOOR_1.equals(world.activeMapId)) {
                Rectangle scientistBounds = scientistNpcBounds();
                if (scientistBounds != null) {
                    drawQuestMarker(batch,
                            scientistBounds.x + scientistBounds.width * 0.5f,
                            scientistBounds.y + scientistBounds.height + 22f + bobOffset,
                            "!",
                            false,
                            0.98f, 0.85f, 0.35f);
                }
                return;
            }
        }
        if (fifthQuestStage == FIFTH_QUEST_GO_TO_ABANDONED_VILLAGE) {
            if (MAP_ID_RESEARCH_LAB_FLOOR_1.equals(world.activeMapId)) {
                drawQuestMarkerForObjectLayer(batch, THIRD_QUEST_LAB_EXIT_LAYER_NAME, bobOffset, true);
                return;
            }
            if (MAP_ID_VILLAGE.equals(world.activeMapId)) {
                ensureFifthQuestVillageLayout();
                drawQuestMarker(batch,
                        fifthQuestRuinsIntroBounds.x + fifthQuestRuinsIntroBounds.width * 0.5f,
                        fifthQuestRuinsIntroBounds.y + fifthQuestRuinsIntroBounds.height + 18f + bobOffset,
                        "v",
                        true,
                        0.98f, 0.46f, 0.34f);
                return;
            }
        }
        if (fifthQuestStage == FIFTH_QUEST_OUTER_RUINS_BATTLE && MAP_ID_VILLAGE.equals(world.activeMapId)) {
            ensureFifthQuestVillageLayout();
            drawQuestMarker(batch,
                    fifthQuestOuterRuinsBounds.x + fifthQuestOuterRuinsBounds.width * 0.5f,
                    fifthQuestOuterRuinsBounds.y + fifthQuestOuterRuinsBounds.height + 18f + bobOffset,
                    "!",
                    false,
                    0.98f, 0.46f, 0.34f);
            return;
        }
        if ((fifthQuestStage == FIFTH_QUEST_ADVANCE_TO_PORTAL || fifthQuestStage == FIFTH_QUEST_MID_RUINS_BATTLE)
                && MAP_ID_VILLAGE.equals(world.activeMapId)) {
            ensureFifthQuestVillageLayout();
            drawQuestMarker(batch,
                    fifthQuestMidRuinsBounds.x + fifthQuestMidRuinsBounds.width * 0.5f,
                    fifthQuestMidRuinsBounds.y + fifthQuestMidRuinsBounds.height + 18f + bobOffset,
                    "!",
                    false,
                    0.98f, 0.46f, 0.34f);
            return;
        }
        if ((fifthQuestStage == FIFTH_QUEST_ANCHOR_1_BATTLE || fifthQuestStage == FIFTH_QUEST_ANCHOR_1_DESTROY)
                && MAP_ID_VILLAGE.equals(world.activeMapId)) {
            drawQuestMarker(batch,
                    fifthQuestAnchorBounds[0].x + fifthQuestAnchorBounds[0].width * 0.5f,
                    fifthQuestAnchorBounds[0].y + fifthQuestAnchorBounds[0].height + 18f + bobOffset,
                    "!",
                    false,
                    0.98f, 0.46f, 0.34f);
            return;
        }
        if ((fifthQuestStage == FIFTH_QUEST_ANCHOR_2_BATTLE || fifthQuestStage == FIFTH_QUEST_ANCHOR_2_DESTROY)
                && MAP_ID_VILLAGE.equals(world.activeMapId)) {
            drawQuestMarker(batch,
                    fifthQuestAnchorBounds[1].x + fifthQuestAnchorBounds[1].width * 0.5f,
                    fifthQuestAnchorBounds[1].y + fifthQuestAnchorBounds[1].height + 18f + bobOffset,
                    "!",
                    false,
                    0.98f, 0.46f, 0.34f);
            return;
        }
        if ((fifthQuestStage == FIFTH_QUEST_ANCHOR_3_BATTLE || fifthQuestStage == FIFTH_QUEST_ANCHOR_3_DESTROY)
                && MAP_ID_VILLAGE.equals(world.activeMapId)) {
            drawQuestMarker(batch,
                    fifthQuestAnchorBounds[2].x + fifthQuestAnchorBounds[2].width * 0.5f,
                    fifthQuestAnchorBounds[2].y + fifthQuestAnchorBounds[2].height + 18f + bobOffset,
                    "!",
                    false,
                    0.98f, 0.46f, 0.34f);
            return;
        }
        if (fifthQuestStage == FIFTH_QUEST_PORTAL_BOSS
                && MAP_ID_VILLAGE.equals(world.activeMapId)) {
            Rectangle questBounds = fifthQuestFightingAreaBounds.width > 0f ? fifthQuestFightingAreaBounds : fifthQuestPortalBounds;
            drawQuestMarker(batch,
                    questBounds.x + questBounds.width * 0.5f,
                    questBounds.y + questBounds.height + 18f + bobOffset,
                    "v",
                    true,
                    0.98f, 0.46f, 0.34f);
            return;
        }
        if ((fifthQuestStage == FIFTH_QUEST_COMPLETE && sixthQuestStage <= SIXTH_QUEST_ENTER_PORTAL)
                && MAP_ID_VILLAGE.equals(world.activeMapId)) {
            drawVillagePortalQuestMarker(batch, sixthQuestPortalGuideBounds(), bobOffset);
            return;
        }
        if (sixthQuestStage >= SIXTH_QUEST_ENTER_PORTAL
                && sixthQuestStage < SIXTH_QUEST_ENDING
                && MAP_ID_VILLAGE.equals(world.activeMapId)) {
            Rectangle portalMarkerBounds = isSixthQuestPortalActivationRunning()
                    ? sixthQuestActualPortalBounds()
                    : sixthQuestPortalGuideBounds();
            drawVillagePortalQuestMarker(batch, portalMarkerBounds, bobOffset);
            return;
        }
        if (MAP_ID_DEMON_CASTLE.equals(world.activeMapId)) {
            ensureSixthQuestCastleLayout();
            if (sixthQuestStage == SIXTH_QUEST_OUTER_ASSAULT) {
                drawQuestMarker(batch,
                        sixthQuestOuterCastleBounds.x + sixthQuestOuterCastleBounds.width * 0.5f,
                        sixthQuestOuterCastleBounds.y + sixthQuestOuterCastleBounds.height + 18f + bobOffset,
                        "!",
                        false,
                        0.96f, 0.48f, 0.28f);
                return;
            }
            if ((sixthQuestStage == SIXTH_QUEST_SEAL_A_BATTLE || sixthQuestStage == SIXTH_QUEST_SEAL_A_DESTROY)) {
                drawQuestMarker(batch,
                        sixthQuestSealAInteractBounds.x + sixthQuestSealAInteractBounds.width * 0.5f,
                        sixthQuestSealAInteractBounds.y + sixthQuestSealAInteractBounds.height + 18f + bobOffset,
                        QUEST_SEAL_MARKER_TEXT,
                        sixthQuestStage == SIXTH_QUEST_SEAL_A_BATTLE,
                        0.96f, 0.36f, 0.24f);
                return;
            }
            if ((sixthQuestStage == SIXTH_QUEST_SEAL_B_BATTLE || sixthQuestStage == SIXTH_QUEST_SEAL_B_DESTROY)) {
                drawQuestMarker(batch,
                        sixthQuestSealBInteractBounds.x + sixthQuestSealBInteractBounds.width * 0.5f,
                        sixthQuestSealBInteractBounds.y + sixthQuestSealBInteractBounds.height + 18f + bobOffset,
                        QUEST_SEAL_MARKER_TEXT,
                        sixthQuestStage == SIXTH_QUEST_SEAL_B_BATTLE,
                        0.96f, 0.36f, 0.24f);
                return;
            }
            if (sixthQuestStage == SIXTH_QUEST_THRONE_GUARDS || sixthQuestStage == SIXTH_QUEST_BOSS_PHASE_1) {
                drawQuestMarker(batch,
                        sixthQuestBossPhase1SpawnBounds.x + sixthQuestBossPhase1SpawnBounds.width * 0.5f,
                        sixthQuestBossPhase1SpawnBounds.y + sixthQuestBossPhase1SpawnBounds.height + 22f + bobOffset,
                        "!",
                        false,
                        0.98f, 0.28f, 0.22f);
                return;
            }
        }
        if ((sixthQuestStage == SIXTH_QUEST_FINAL_PHASE_INTRO || sixthQuestStage == SIXTH_QUEST_FINAL_PHASE_BATTLE)
                && MAP_ID_BOSS_ARENA_PHASE_2.equals(world.activeMapId)) {
            ensureSixthQuestFinalArenaLayout();
            drawQuestMarker(batch,
                    sixthQuestFinalPhaseSpawnBounds.x + sixthQuestFinalPhaseSpawnBounds.width * 0.5f,
                    sixthQuestFinalPhaseSpawnBounds.y + sixthQuestFinalPhaseSpawnBounds.height + 22f + bobOffset,
                    "!",
                    false,
                    0.98f, 0.30f, 0.22f);
        }
    }

    private void drawQuestMarkerForObjectLayer(SpriteBatch batch, String layerName, float bobOffset, boolean directional) {
        Rectangle targetBounds = objectLayerBounds(findLayerByName(layerName));
        drawQuestMarkerForBounds(batch, targetBounds, bobOffset, directional);
    }

    private void drawQuestMarkerForBounds(SpriteBatch batch, Rectangle targetBounds, float bobOffset, boolean directional) {
        if (targetBounds == null) {
            return;
        }
        drawQuestMarker(batch,
                targetBounds.x + targetBounds.width * 0.5f,
                targetBounds.y + targetBounds.height + 20f + bobOffset,
                directional ? "v" : "!",
                directional,
                0.98f, 0.85f, 0.35f);
    }

    private void drawVillagePortalQuestMarker(SpriteBatch batch, Rectangle targetBounds, float bobOffset) {
        if (targetBounds == null) {
            return;
        }
        drawQuestMarker(batch,
                targetBounds.x + targetBounds.width * 0.5f,
                targetBounds.y + targetBounds.height + 20f + bobOffset + VILLAGE_PORTAL_QUEST_MARKER_Y_OFFSET,
                "v",
                true,
                0.98f, 0.85f, 0.35f);
    }

    private void drawQuestMarker(SpriteBatch batch, float worldX, float worldY, String markerText,
                                 boolean directional, float r, float g, float b) {
        if (firstQuestTitleFont == null || uiCamera == null || camera == null) {
            return;
        }
        questMarkerProjection.set(worldX, worldY, 0f);
        camera.project(questMarkerProjection);
        float screenX = questMarkerProjection.x;
        float screenY = questMarkerProjection.y;
        float marginX = 32f;
        float marginY = 64f;
        boolean offscreen = screenX < -marginX || screenX > uiCamera.viewportWidth + marginX
                || screenY < -marginY || screenY > uiCamera.viewportHeight + marginY;
        if (directional || offscreen) {
            float clampedX = Math.max(marginX, Math.min(uiCamera.viewportWidth - marginX, screenX));
            float clampedY = Math.max(marginY, Math.min(uiCamera.viewportHeight - marginY, screenY));
            if (clampedX != screenX || clampedY != screenY) {
                float deltaX = screenX - uiCamera.viewportWidth * 0.5f;
                float deltaY = screenY - uiCamera.viewportHeight * 0.5f;
                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    markerText = deltaX >= 0f ? ">" : "<";
                } else {
                    markerText = deltaY >= 0f ? "^" : "v";
                }
            }
            screenX = clampedX;
            screenY = clampedY;
        }
        boolean arrowMarker = isQuestArrowMarker(markerText);
        boolean sealMarker = isQuestSealMarker(markerText);
        float baseScaleX = firstQuestTitleFont.getData().scaleX;
        float baseScaleY = firstQuestTitleFont.getData().scaleY;
        if (arrowMarker) {
            firstQuestTitleFont.getData().setScale(
                    baseScaleX * QUEST_ARROW_MARKER_SCALE,
                    baseScaleY * QUEST_ARROW_MARKER_SCALE);
            drawCenteredQuestText(batch, firstQuestTitleFont, markerText,
                    screenX - QUEST_ARROW_MARKER_OUTLINE_OFFSET, screenY,
                    0f, 0f, 0f, 0.95f);
            drawCenteredQuestText(batch, firstQuestTitleFont, markerText,
                    screenX + QUEST_ARROW_MARKER_OUTLINE_OFFSET, screenY,
                    0f, 0f, 0f, 0.95f);
            drawCenteredQuestText(batch, firstQuestTitleFont, markerText,
                    screenX, screenY - QUEST_ARROW_MARKER_OUTLINE_OFFSET,
                    0f, 0f, 0f, 0.95f);
            drawCenteredQuestText(batch, firstQuestTitleFont, markerText,
                    screenX, screenY + QUEST_ARROW_MARKER_OUTLINE_OFFSET,
                    0f, 0f, 0f, 0.95f);
        }
        if (sealMarker) {
            drawSealQuestMarker(batch, screenX, screenY, r, g, b);
        } else {
            drawCenteredQuestText(batch, firstQuestTitleFont, markerText, screenX, screenY, r, g, b, 1f);
        }
        if (arrowMarker) {
            firstQuestTitleFont.getData().setScale(baseScaleX, baseScaleY);
        }
    }

    private void drawSealQuestMarker(SpriteBatch batch, float centerX, float centerY, float r, float g, float b) {
        if (batch == null || dayNightOverlayTex == null || abilityCooldownCircleTex == null) {
            drawCenteredQuestText(batch, firstQuestTitleFont, "!", centerX, centerY, r, g, b, 1f);
            return;
        }

        float outerSize = QUEST_SEAL_MARKER_SIZE;
        float outerX = centerX - outerSize * 0.5f;
        float outerY = centerY - outerSize * 0.5f;
        float ringSize = outerSize * 0.74f;
        float ringX = centerX - ringSize * 0.5f;
        float ringY = centerY - ringSize * 0.5f;
        float coreSize = QUEST_SEAL_MARKER_CORE_SIZE;
        float coreX = centerX - coreSize * 0.5f;
        float coreY = centerY - coreSize * 0.5f;
        float glowR = Math.min(1f, r + 0.18f);
        float glowG = Math.min(1f, g + 0.28f);
        float glowB = Math.min(1f, b + 0.20f);

        batch.setColor(0f, 0f, 0f, 0.28f);
        batch.draw(abilityCooldownCircleTex, outerX + 2f, outerY - 2f, outerSize + 2f, outerSize + 2f);

        batch.setColor(r * 0.36f, g * 0.22f, b * 0.24f, 0.96f);
        batch.draw(abilityCooldownCircleTex, outerX, outerY, outerSize, outerSize);

        batch.setColor(glowR, glowG, glowB, 0.96f);
        batch.draw(abilityCooldownCircleTex, ringX, ringY, ringSize, ringSize);

        batch.setColor(0.10f, 0.03f, 0.04f, 0.94f);
        batch.draw(abilityCooldownCircleTex, coreX, coreY, coreSize, coreSize);

        drawSealQuestMarkerRect(batch, centerX, centerY, QUEST_SEAL_MARKER_RUNE_LENGTH,
                QUEST_SEAL_MARKER_RUNE_THICKNESS, 0f, glowR, glowG, glowB, 0.96f);
        drawSealQuestMarkerRect(batch, centerX, centerY, QUEST_SEAL_MARKER_RUNE_LENGTH,
                QUEST_SEAL_MARKER_RUNE_THICKNESS, 90f, glowR, glowG, glowB, 0.96f);
        drawSealQuestMarkerRect(batch, centerX, centerY, QUEST_SEAL_MARKER_RUNE_LENGTH * 0.82f,
                QUEST_SEAL_MARKER_RUNE_THICKNESS, 45f, glowR, glowG, glowB, 0.9f);
        drawSealQuestMarkerRect(batch, centerX, centerY, QUEST_SEAL_MARKER_RUNE_LENGTH * 0.82f,
                QUEST_SEAL_MARKER_RUNE_THICKNESS, -45f, glowR, glowG, glowB, 0.9f);

        drawSealQuestMarkerRect(batch, centerX, centerY + QUEST_SEAL_MARKER_ORBIT_OFFSET,
                QUEST_SEAL_MARKER_ORBIT_SIZE, QUEST_SEAL_MARKER_ORBIT_SIZE, 45f, glowR, glowG, glowB, 0.92f);
        drawSealQuestMarkerRect(batch, centerX + QUEST_SEAL_MARKER_ORBIT_OFFSET, centerY,
                QUEST_SEAL_MARKER_ORBIT_SIZE, QUEST_SEAL_MARKER_ORBIT_SIZE, 45f, glowR, glowG, glowB, 0.92f);
        drawSealQuestMarkerRect(batch, centerX, centerY - QUEST_SEAL_MARKER_ORBIT_OFFSET,
                QUEST_SEAL_MARKER_ORBIT_SIZE, QUEST_SEAL_MARKER_ORBIT_SIZE, 45f, glowR, glowG, glowB, 0.92f);
        drawSealQuestMarkerRect(batch, centerX - QUEST_SEAL_MARKER_ORBIT_OFFSET, centerY,
                QUEST_SEAL_MARKER_ORBIT_SIZE, QUEST_SEAL_MARKER_ORBIT_SIZE, 45f, glowR, glowG, glowB, 0.92f);

        batch.setColor(glowR, glowG, glowB, 1f);
        batch.draw(abilityCooldownCircleTex, centerX - 2f, centerY - 2f, 4f, 4f);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void drawSealQuestMarkerRect(
            SpriteBatch batch,
            float centerX,
            float centerY,
            float width,
            float height,
            float rotationDegrees,
            float r,
            float g,
            float b,
            float alpha) {
        batch.setColor(r, g, b, alpha);
        batch.draw(
                dayNightOverlayTex,
                centerX - width * 0.5f,
                centerY - height * 0.5f,
                width * 0.5f,
                height * 0.5f,
                width,
                height,
                1f,
                1f,
                rotationDegrees,
                0,
                0,
                1,
                1,
                false,
                false);
    }

    private boolean isQuestArrowMarker(String markerText) {
        return ">".equals(markerText)
                || "<".equals(markerText)
                || "^".equals(markerText)
                || "v".equals(markerText);
    }

    private boolean isQuestSealMarker(String markerText) {
        return QUEST_SEAL_MARKER_TEXT.equals(markerText);
    }

    private void drawFirstQuestPrompt(SpriteBatch batch) {
        String promptText = currentFirstQuestPrompt();
        if (promptText == null || firstQuestHintFont == null || uiCamera == null || firstQuestLayout == null) {
            return;
        }
        firstQuestLayout.setText(firstQuestHintFont, promptText);
        float paddingX = 18f;
        float paddingY = 10f;
        float boxWidth = firstQuestLayout.width + paddingX * 2f;
        float boxHeight = firstQuestLayout.height + paddingY * 2f;
        float boxX = (uiCamera.viewportWidth - boxWidth) * 0.5f;
        float boxY = 104f;
        batch.setColor(0f, 0f, 0f, 0.58f);
        batch.draw(dayNightOverlayTex, boxX, boxY, boxWidth, boxHeight);
        batch.setColor(1f, 1f, 1f, 1f);
        drawCenteredQuestText(batch, firstQuestHintFont, promptText,
                uiCamera.viewportWidth * 0.5f,
                boxY + paddingY + firstQuestLayout.height,
                0.95f, 0.92f, 0.83f, 1f);
    }

    private String currentMainMenuQuestTitle() {
        if (gameCompleted || sixthQuestStage == SIXTH_QUEST_COMPLETE) {
            return "Game Complete";
        }
        if (sixthQuestStage > SIXTH_QUEST_DISABLED) {
            return SIXTH_QUEST_NOTIFICATION_TITLE;
        }
        if (fifthQuestStage == FIFTH_QUEST_COMPLETE) {
            return SIXTH_QUEST_NOTIFICATION_TITLE;
        }
        if (fifthQuestStage > FIFTH_QUEST_DISABLED && fifthQuestStage < FIFTH_QUEST_COMPLETE) {
            return FIFTH_QUEST_NOTIFICATION_TITLE;
        }
        if (fourthQuestStage > FOURTH_QUEST_DISABLED && fourthQuestStage < FOURTH_QUEST_COMPLETE) {
            return FOURTH_QUEST_NOTIFICATION_TITLE;
        }
        if (thirdQuestStage > THIRD_QUEST_DISABLED && thirdQuestStage < THIRD_QUEST_COMPLETE) {
            return THIRD_QUEST_NOTIFICATION_TITLE;
        }
        if (secondQuestStage > SECOND_QUEST_DISABLED && secondQuestStage < SECOND_QUEST_COMPLETE) {
            return SECOND_QUEST_NOTIFICATION_TITLE;
        }
        if (firstQuestStage > FIRST_QUEST_DISABLED && firstQuestStage < FIRST_QUEST_COMPLETE) {
            return FIRST_QUEST_NOTIFICATION_TITLE;
        }
        return "No active quest";
    }

    private String currentMainMenuQuestObjective() {
        if (gameCompleted || sixthQuestStage == SIXTH_QUEST_COMPLETE) {
            return "The demon is defeated. The story is complete.";
        }
        if (sixthQuestStage > SIXTH_QUEST_DISABLED) {
            return switch (sixthQuestStage) {
                case SIXTH_QUEST_ENTER_PORTAL -> sixthQuestPortalChoiceVisible
                        ? "Choose whether to enter the final stage."
                        : "Go to the dormant portal.";
                case SIXTH_QUEST_OUTER_ASSAULT -> "Defeat the demons blocking the route to the inner gate.";
                case SIXTH_QUEST_SEAL_A_BATTLE, SIXTH_QUEST_SEAL_A_DESTROY -> "Break the first Castle Seal.";
                case SIXTH_QUEST_SEAL_B_BATTLE, SIXTH_QUEST_SEAL_B_DESTROY -> "Break the second Castle Seal.";
                case SIXTH_QUEST_THRONE_GUARDS -> "Advance to the Throne Hall and defeat the throne guards.";
                case SIXTH_QUEST_BOSS_PHASE_1 -> "Confront the Demon.";
                case SIXTH_QUEST_FINAL_PHASE_INTRO, SIXTH_QUEST_FINAL_PHASE_BATTLE -> "Survive the final phase and end the demon.";
                case SIXTH_QUEST_ENDING -> "See the final aftermath.";
                default -> "Enter the castle and finish the demon.";
            };
        }
        if (fifthQuestStage == FIFTH_QUEST_COMPLETE) {
            return "Enter the Portal and raid the Demon Castle.";
        }
        if (fifthQuestStage > FIFTH_QUEST_DISABLED) {
            return switch (fifthQuestStage) {
                case FIFTH_QUEST_GO_TO_ABANDONED_VILLAGE -> "Go to the abandoned village by the portal.";
                case FIFTH_QUEST_OUTER_RUINS_BATTLE -> "Defeat all demons in the outer ruins.";
                case FIFTH_QUEST_ADVANCE_TO_PORTAL, FIFTH_QUEST_MID_RUINS_BATTLE -> "Advance deeper toward the portal.";
                case FIFTH_QUEST_ANCHOR_1_BATTLE, FIFTH_QUEST_ANCHOR_1_DESTROY,
                     FIFTH_QUEST_ANCHOR_2_BATTLE, FIFTH_QUEST_ANCHOR_2_DESTROY,
                     FIFTH_QUEST_ANCHOR_3_BATTLE, FIFTH_QUEST_ANCHOR_3_DESTROY ->
                        "Destroy the portal anchors (" + currentFifthQuestDestroyedAnchorCount() + "/3).";
                case FIFTH_QUEST_PORTAL_BOSS -> "Defeat the demon guarding the portal.";
                default -> "Push back the demonic invasion near the portal.";
            };
        }
        if (fourthQuestStage > FOURTH_QUEST_DISABLED) {
            return switch (fourthQuestStage) {
                case FOURTH_QUEST_REPORT_TO_SCIENTIST -> "Report the fresh burn marks to the scientist.";
                case FOURTH_QUEST_TALK_TO_CAFE_OWNER, FOURTH_QUEST_EXAMINE_CAFE_MARK, FOURTH_QUEST_CAFE_BATTLE ->
                        "Investigate the corruption outside the bakery.";
                case FOURTH_QUEST_EXAMINE_CENTER_MARK, FOURTH_QUEST_CENTER_BATTLE ->
                        "Investigate the corruption in the village center.";
                case FOURTH_QUEST_EXAMINE_EDGE_MARK, FOURTH_QUEST_EDGE_BATTLE ->
                        "Investigate the corruption near the outer road.";
                case FOURTH_QUEST_RETURN_TO_SCIENTIST -> "Bring the ash samples back to the scientist.";
                case FOURTH_QUEST_GO_TO_ARRAY -> "Go to Floor 2 and activate the Resonance Array.";
                case FOURTH_QUEST_ACTIVATE_ARRAY_NODE_1, FOURTH_QUEST_ACTIVATE_ARRAY_NODE_2, FOURTH_QUEST_ACTIVATE_ARRAY_NODE_3 ->
                        "Activate the outer array nodes (" + currentFourthQuestActivatedArrayNodeCount() + "/3).";
                case FOURTH_QUEST_ACTIVATE_ARRAY_CONSOLE -> "Return to the main console and begin the scan.";
                case FOURTH_QUEST_ARRAY_DEFENSE -> "Protect the Resonance Array until the scan completes.";
                case FOURTH_QUEST_GO_TO_BREACH, FOURTH_QUEST_EXAMINE_BREACH, FOURTH_QUEST_BREACH_BATTLE ->
                        "Go to the breach site and stop it.";
                case FOURTH_QUEST_RETURN_RIFT_CORE, FOURTH_QUEST_EPILOGUE ->
                        "Bring the Rift Core back to the scientist.";
                default -> "Investigate the demonic marks threatening the village.";
            };
        }
        if (thirdQuestStage > THIRD_QUEST_DISABLED) {
            return switch (thirdQuestStage) {
                case THIRD_QUEST_RETURN_TO_FATHER -> "Return to Father in the inn room.";
                case THIRD_QUEST_MEET_FATHER_OUTSIDE -> "Meet Father at the training ground.";
                case THIRD_QUEST_MOVEMENT_TUTORIAL ->
                        "Reach all training markers (" + thirdQuestMovementMarkersReached + "/" + thirdQuestMovementMarkers.length + ").";
                case THIRD_QUEST_ATTACK_TUTORIAL ->
                        "Hit the training dummy (" + thirdQuestAttackDummyHitCount + "/" + THIRD_QUEST_DUMMY_HITS_REQUIRED + ").";
                case THIRD_QUEST_COMBAT_TUTORIAL -> "Defeat the training enemies.";
                case THIRD_QUEST_SPAR_WITH_FATHER -> "Spar with Father and prove your control.";
                default -> "Train with Father behind the inn.";
            };
        }
        if (secondQuestStage > SECOND_QUEST_DISABLED) {
            return switch (secondQuestStage) {
                case SECOND_QUEST_TALK_TO_CAFE_OWNER -> "Speak with the bakery owner outside the bakery.";
                case SECOND_QUEST_TAKE_FRAGMENT_TO_SCIENTIST -> "Bring the Scorched Fragment to the scientist.";
                case SECOND_QUEST_RETRIEVE_ARCHIVE_NOTES -> "Retrieve the Archive Notes from Floor 2.";
                case SECOND_QUEST_RETURN_TO_SCIENTIST -> "Return the Archive Notes to the scientist.";
                default -> "Follow the ash trail to the Research Lab.";
            };
        }
        if (firstQuestStage > FIRST_QUEST_DISABLED) {
            return switch (firstQuestStage) {
                case FIRST_QUEST_TALK_TO_FATHER -> "Speak with Father in the inn room.";
                case FIRST_QUEST_DELIVER_TO_CAFE -> "Deliver the Herb-and-Spice to the bakery owner.";
                default -> "Help Father with his request.";
            };
        }
        return "Explore the world.";
    }

    private int currentFourthQuestActivatedArrayNodeCount() {
        if (fourthQuestStage >= FOURTH_QUEST_ACTIVATE_ARRAY_CONSOLE) {
            return 3;
        }
        if (fourthQuestStage >= FOURTH_QUEST_ACTIVATE_ARRAY_NODE_3) {
            return 2;
        }
        if (fourthQuestStage >= FOURTH_QUEST_ACTIVATE_ARRAY_NODE_2) {
            return 1;
        }
        return 0;
    }

    private int currentFifthQuestDestroyedAnchorCount() {
        int destroyedCount = 0;
        if (fifthQuestStage > FIFTH_QUEST_ANCHOR_1_DESTROY) {
            destroyedCount++;
        }
        if (fifthQuestStage > FIFTH_QUEST_ANCHOR_2_DESTROY) {
            destroyedCount++;
        }
        if (fifthQuestStage > FIFTH_QUEST_ANCHOR_3_DESTROY) {
            destroyedCount++;
        }
        return destroyedCount;
    }

    private String currentMainMenuRegionName() {
        if (world.activeMapId == null) {
            return "Unknown";
        }
        return switch (world.activeMapId) {
            case MAP_ID_VILLAGE -> "Hagi Village";
            case MAP_ID_INN, MAP_ID_INN_ROOM -> "Hagi Inn";
            case MAP_ID_RESEARCH_LAB_FLOOR_1, MAP_ID_RESEARCH_LAB_FLOOR_2 -> "Research Lab";
            case MAP_ID_DEMON_CASTLE -> "Demon Castle";
            case MAP_ID_BOSS_ARENA_PHASE_2 -> "Boss Arena";
            default -> world.activeMapId;
        };
    }

    private String currentFirstQuestPrompt() {
        if (world.activeMapId == null) {
            return null;
        }
        if (firstQuestStage == FIRST_QUEST_TALK_TO_FATHER
                && MAP_ID_INN_ROOM.equals(world.activeMapId)
                && isPlayerNearFirstQuestFather()) {
            return "Press E to talk";
        }
        if (firstQuestStage == FIRST_QUEST_DELIVER_TO_CAFE
                && MAP_ID_VILLAGE.equals(world.activeMapId)
                && isPlayerNearFirstQuestCafeNpc()) {
            return "Press E to deliver the Herb and Spice";
        }
        if (secondQuestStage == SECOND_QUEST_TALK_TO_CAFE_OWNER
                && MAP_ID_VILLAGE.equals(world.activeMapId)
                && isPlayerNearFirstQuestCafeNpc()) {
            return "Press E to talk";
        }
        if (secondQuestStage == SECOND_QUEST_TAKE_FRAGMENT_TO_SCIENTIST
                && MAP_ID_VILLAGE.equals(world.activeMapId)
                && isPlayerWithinObjectLayer("ENTRANCE-Research-Lab")) {
            return "Press E to enter the Research Lab";
        }
        if (secondQuestStage == SECOND_QUEST_TAKE_FRAGMENT_TO_SCIENTIST
                && MAP_ID_RESEARCH_LAB_FLOOR_1.equals(world.activeMapId)
                && isPlayerNearScientistNpc()) {
            return "Press E to talk";
        }
        if (secondQuestStage == SECOND_QUEST_RETRIEVE_ARCHIVE_NOTES
                && MAP_ID_RESEARCH_LAB_FLOOR_1.equals(world.activeMapId)
                && isPlayerWithinObjectLayer("ENTRANCE-Research-Lab-Floor-2")) {
            return "Press E to go upstairs";
        }
        if (secondQuestStage == SECOND_QUEST_RETRIEVE_ARCHIVE_NOTES
                && MAP_ID_RESEARCH_LAB_FLOOR_2.equals(world.activeMapId)
                && isPlayerNearSecondQuestArchive()) {
            return "Press E to inspect";
        }
        if (secondQuestStage == SECOND_QUEST_RETURN_TO_SCIENTIST
                && MAP_ID_RESEARCH_LAB_FLOOR_2.equals(world.activeMapId)
                && isPlayerWithinObjectLayer("EXIT-Research-Lab-Floor-2")) {
            return "Press E to go downstairs";
        }
        if (secondQuestStage == SECOND_QUEST_RETURN_TO_SCIENTIST
                && MAP_ID_RESEARCH_LAB_FLOOR_1.equals(world.activeMapId)
                && isPlayerNearScientistNpc()) {
            return "Press E to talk";
        }
        if (thirdQuestStage == THIRD_QUEST_RETURN_TO_FATHER
                && MAP_ID_RESEARCH_LAB_FLOOR_1.equals(world.activeMapId)
                && isPlayerWithinObjectLayer(THIRD_QUEST_LAB_EXIT_LAYER_NAME)) {
            return "Press E to leave the Research Lab";
        }
        if (thirdQuestStage == THIRD_QUEST_RETURN_TO_FATHER
                && MAP_ID_VILLAGE.equals(world.activeMapId)
                && isPlayerWithinObjectLayer(THIRD_QUEST_INN_ENTRANCE_LAYER_NAME)) {
            return "Press E to enter the inn";
        }
        if (thirdQuestStage == THIRD_QUEST_RETURN_TO_FATHER
                && MAP_ID_INN.equals(world.activeMapId)
                && isPlayerWithinObjectLayer(THIRD_QUEST_INN_ROOM_ENTRANCE_LAYER_NAME)) {
            return "Press E to enter the inn room";
        }
        if (thirdQuestStage == THIRD_QUEST_RETURN_TO_FATHER
                && MAP_ID_INN_ROOM.equals(world.activeMapId)
                && isPlayerNearFirstQuestFather()) {
            return "Press E to talk";
        }
        if (thirdQuestStage == THIRD_QUEST_MEET_FATHER_OUTSIDE
                && MAP_ID_INN_ROOM.equals(world.activeMapId)
                && isPlayerWithinObjectLayer("EXIT-Inn-Room")) {
            return "Press E to leave the room";
        }
        if (thirdQuestStage == THIRD_QUEST_MEET_FATHER_OUTSIDE
                && MAP_ID_INN.equals(world.activeMapId)
                && isPlayerWithinObjectLayer("EXIT-Inn")) {
            return "Press E to leave the inn";
        }
        if ((thirdQuestStage == THIRD_QUEST_MEET_FATHER_OUTSIDE
                || (thirdQuestStage == THIRD_QUEST_SPAR_WITH_FATHER && !thirdQuestSparActive))
                && MAP_ID_VILLAGE.equals(world.activeMapId)
                && isPlayerNearThirdQuestVillageFather()) {
            return "Press E to talk";
        }
        if (fourthQuestStage == FOURTH_QUEST_REPORT_TO_SCIENTIST
                && MAP_ID_VILLAGE.equals(world.activeMapId)
                && isPlayerWithinObjectLayer(FOURTH_QUEST_LAB_ENTRANCE_LAYER_NAME)) {
            return "Use the interact key/button to enter the Research Lab";
        }
        if (fourthQuestStage == FOURTH_QUEST_REPORT_TO_SCIENTIST
                && MAP_ID_RESEARCH_LAB_FLOOR_1.equals(world.activeMapId)
                && isPlayerNearScientistNpc()) {
            return "Use the interact key/button to speak";
        }
        if (fourthQuestStage == FOURTH_QUEST_TALK_TO_CAFE_OWNER
                && MAP_ID_VILLAGE.equals(world.activeMapId)
                && isPlayerNearFirstQuestCafeNpc()) {
            return "Use the interact key/button to speak";
        }
        if (fourthQuestStage == FOURTH_QUEST_EXAMINE_CAFE_MARK
                && MAP_ID_VILLAGE.equals(world.activeMapId)
                && isPlayerNearFourthQuestCafeMark()) {
            return "Use the interact key/button to examine the mark";
        }
        if (fourthQuestStage == FOURTH_QUEST_EXAMINE_CENTER_MARK
                && MAP_ID_VILLAGE.equals(world.activeMapId)
                && isPlayerNearFourthQuestCenterMark()) {
            return "Use the interact key/button to examine the mark";
        }
        if (fourthQuestStage == FOURTH_QUEST_EXAMINE_EDGE_MARK
                && MAP_ID_VILLAGE.equals(world.activeMapId)
                && isPlayerNearFourthQuestEdgeMark()) {
            return "Use the interact key/button to examine the mark";
        }
        if (fourthQuestStage == FOURTH_QUEST_RETURN_TO_SCIENTIST
                && MAP_ID_VILLAGE.equals(world.activeMapId)
                && isPlayerWithinObjectLayer(FOURTH_QUEST_LAB_ENTRANCE_LAYER_NAME)) {
            return "Use the interact key/button to enter the Research Lab";
        }
        if (fourthQuestStage == FOURTH_QUEST_RETURN_TO_SCIENTIST
                && MAP_ID_RESEARCH_LAB_FLOOR_1.equals(world.activeMapId)
                && isPlayerNearScientistNpc()) {
            return "Use the interact key/button to speak";
        }
        if (fourthQuestStage == FOURTH_QUEST_GO_TO_ARRAY
                && MAP_ID_RESEARCH_LAB_FLOOR_1.equals(world.activeMapId)
                && isPlayerWithinObjectLayer(FOURTH_QUEST_LAB_FLOOR_2_LAYER_NAME)) {
            return "Use the interact key/button to go upstairs";
        }
        if ((fourthQuestStage == FOURTH_QUEST_GO_TO_ARRAY || fourthQuestStage == FOURTH_QUEST_ACTIVATE_ARRAY_NODE_1)
                && MAP_ID_RESEARCH_LAB_FLOOR_2.equals(world.activeMapId)
                && isPlayerNearFourthQuestArrayNode(0)) {
            return "Use the interact key/button to activate the node";
        }
        if (fourthQuestStage == FOURTH_QUEST_ACTIVATE_ARRAY_NODE_2
                && MAP_ID_RESEARCH_LAB_FLOOR_2.equals(world.activeMapId)
                && isPlayerNearFourthQuestArrayNode(1)) {
            return "Use the interact key/button to activate the node";
        }
        if (fourthQuestStage == FOURTH_QUEST_ACTIVATE_ARRAY_NODE_3
                && MAP_ID_RESEARCH_LAB_FLOOR_2.equals(world.activeMapId)
                && isPlayerNearFourthQuestArrayNode(2)) {
            return "Use the interact key/button to activate the node";
        }
        if (fourthQuestStage == FOURTH_QUEST_ACTIVATE_ARRAY_CONSOLE
                && MAP_ID_RESEARCH_LAB_FLOOR_2.equals(world.activeMapId)
                && isPlayerNearFourthQuestArrayConsole()) {
            return "Use the interact key/button to begin the scan";
        }
        if (fourthQuestStage == FOURTH_QUEST_GO_TO_BREACH
                && MAP_ID_RESEARCH_LAB_FLOOR_2.equals(world.activeMapId)
                && isPlayerWithinObjectLayer(FOURTH_QUEST_LAB_FLOOR_2_EXIT_LAYER_NAME)) {
            return "Use the interact key/button to go downstairs";
        }
        if (fourthQuestStage == FOURTH_QUEST_GO_TO_BREACH
                && MAP_ID_RESEARCH_LAB_FLOOR_1.equals(world.activeMapId)
                && isPlayerWithinObjectLayer(THIRD_QUEST_LAB_EXIT_LAYER_NAME)) {
            return "Use the interact key/button to leave the Research Lab";
        }
        if (fourthQuestStage == FOURTH_QUEST_EXAMINE_BREACH
                && MAP_ID_VILLAGE.equals(world.activeMapId)
                && isPlayerNearFourthQuestBreachSite()) {
            return "Use the interact key/button to examine the breach";
        }
        if (fourthQuestStage == FOURTH_QUEST_RETURN_RIFT_CORE
                && MAP_ID_VILLAGE.equals(world.activeMapId)
                && isPlayerWithinObjectLayer(FOURTH_QUEST_LAB_ENTRANCE_LAYER_NAME)) {
            return "Use the interact key/button to enter the Research Lab";
        }
        if (fourthQuestStage == FOURTH_QUEST_RETURN_RIFT_CORE
                && MAP_ID_RESEARCH_LAB_FLOOR_1.equals(world.activeMapId)
                && isPlayerNearScientistNpc()) {
            return "Use the interact key/button to speak";
        }
        if (fifthQuestStage == FIFTH_QUEST_GO_TO_ABANDONED_VILLAGE
                && MAP_ID_RESEARCH_LAB_FLOOR_1.equals(world.activeMapId)
                && isPlayerWithinObjectLayer(THIRD_QUEST_LAB_EXIT_LAYER_NAME)) {
            return "Use the interact key/button to leave the Research Lab";
        }
        if ((fifthQuestStage == FIFTH_QUEST_ANCHOR_1_DESTROY
                || fifthQuestStage == FIFTH_QUEST_ANCHOR_2_DESTROY
                || fifthQuestStage == FIFTH_QUEST_ANCHOR_3_DESTROY)
                && MAP_ID_VILLAGE.equals(world.activeMapId)) {
            int anchorIndex = fifthQuestStage == FIFTH_QUEST_ANCHOR_1_DESTROY
                    ? 0
                    : (fifthQuestStage == FIFTH_QUEST_ANCHOR_2_DESTROY ? 1 : 2);
            if (isPlayerNearFifthQuestAnchor(anchorIndex)) {
                return "Use the interact key/button to destroy the anchor";
            }
        }
        if (sixthQuestStage >= SIXTH_QUEST_ENTER_PORTAL
                && sixthQuestStage < SIXTH_QUEST_ENDING
                && MAP_ID_VILLAGE.equals(world.activeMapId)
                && isPlayerWithinVillagePortalInteraction()) {
            return isSixthQuestPortalActivationRunning()
                    ? null
                    : "Use the interact key/button to awaken the portal";
        }
        if (sixthQuestStage == SIXTH_QUEST_SEAL_A_DESTROY
                && MAP_ID_DEMON_CASTLE.equals(world.activeMapId)
                && isPlayerNearSixthQuestSeal(true)) {
            return "Use the interact key/button to shatter the seal";
        }
        if (sixthQuestStage == SIXTH_QUEST_SEAL_B_DESTROY
                && MAP_ID_DEMON_CASTLE.equals(world.activeMapId)
                && isPlayerNearSixthQuestSeal(false)) {
            return "Use the interact key/button to shatter the seal";
        }
        return null;
    }

    private boolean isWallCollisionLayer(MapLayer layer) {
        if (layer == null || layer.getName() == null) {
            return false;
        }
        return layer.getName().toUpperCase(Locale.ROOT).contains("WALL-COLLISION");
    }

    private Animation<TextureRegion> fifthQuestDemonIdleAnimation(FifthQuestDemonType type) {
        return switch (type) {
            case DEMON_2 -> fifthQuestDemon2IdleAnim;
            case DEMON_3, COMMANDER -> fifthQuestDemon3IdleAnim;
            default -> fifthQuestDemon1IdleAnim;
        };
    }

    private Animation<TextureRegion> fifthQuestDemonWalkAnimation(FifthQuestDemonType type) {
        return switch (type) {
            case DEMON_2 -> fifthQuestDemon2WalkAnim;
            case DEMON_3, COMMANDER -> fifthQuestDemon3WalkAnim;
            default -> fifthQuestDemon1WalkAnim;
        };
    }

    private Animation<TextureRegion> fifthQuestDemonAttackAnimation(FifthQuestDemonType type) {
        return switch (type) {
            case DEMON_2 -> fifthQuestDemon2AttackAnim;
            case DEMON_3, COMMANDER -> fifthQuestDemon3AttackAnim;
            default -> fifthQuestDemon1AttackAnim;
        };
    }

    private Animation<TextureRegion> fifthQuestDemonHurtAnimation(FifthQuestDemonType type) {
        return switch (type) {
            case DEMON_2 -> fifthQuestDemon2HurtAnim;
            case DEMON_3, COMMANDER -> fifthQuestDemon3HurtAnim;
            default -> fifthQuestDemon1HurtAnim;
        };
    }

    private Animation<TextureRegion> fifthQuestDemonDeadAnimation(FifthQuestDemonType type) {
        return switch (type) {
            case DEMON_2 -> fifthQuestDemon2DeadAnim;
            case DEMON_3, COMMANDER -> fifthQuestDemon3DeadAnim;
            default -> fifthQuestDemon1DeadAnim;
        };
    }

    private TextureRegion firstFifthQuestDemonReferenceFrame(FifthQuestDemonType type) {
        Animation<TextureRegion> idle = fifthQuestDemonIdleAnimation(type);
        if (idle != null) {
            return idle.getKeyFrame(0f, true);
        }
        Animation<TextureRegion> walk = fifthQuestDemonWalkAnimation(type);
        if (walk != null) {
            return walk.getKeyFrame(0f, true);
        }
        Animation<TextureRegion> attack = fifthQuestDemonAttackAnimation(type);
        if (attack != null) {
            return attack.getKeyFrame(0f, false);
        }
        Animation<TextureRegion> hurt = fifthQuestDemonHurtAnimation(type);
        if (hurt != null) {
            return hurt.getKeyFrame(0f, false);
        }
        Animation<TextureRegion> dead = fifthQuestDemonDeadAnimation(type);
        if (dead != null) {
            return dead.getKeyFrame(0f, false);
        }
        return null;
    }

    private float fifthQuestDemonFrameWidth(FifthQuestDemonType type) {
        TextureRegion referenceFrame = firstFifthQuestDemonReferenceFrame(type);
        if (referenceFrame != null && referenceFrame.getRegionWidth() > 0) {
            return referenceFrame.getRegionWidth();
        }
        return FIFTH_QUEST_DEMON_FRAME_WIDTH;
    }

    private float fifthQuestDemonFrameHeight(FifthQuestDemonType type) {
        TextureRegion referenceFrame = firstFifthQuestDemonReferenceFrame(type);
        if (referenceFrame != null && referenceFrame.getRegionHeight() > 0) {
            return referenceFrame.getRegionHeight();
        }
        return FIFTH_QUEST_DEMON_FRAME_HEIGHT;
    }

    private float fifthQuestDemonHitboxCenterX(FifthQuestDemonInstance demon) {
        return demon != null ? demon.position.x : 0f;
    }

    private float fifthQuestDemonHitboxCenterY(FifthQuestDemonInstance demon) {
        if (demon == null) {
            return 0f;
        }
        return demon.position.y - FIFTH_QUEST_DEMON_HITBOX_DOWN_OFFSET_TILES * DEFAULT_TILE_SIZE;
    }

    private float fifthQuestDemonHitboxHalfWidth() {
        float frameWidth = FIFTH_QUEST_DEMON_FRAME_WIDTH * FIFTH_QUEST_DEMON_DRAW_SCALE;
        return frameWidth * FIFTH_QUEST_DEMON_HITBOX_WIDTH_SCALE;
    }

    private float fifthQuestDemonHitboxHalfHeight() {
        float frameHeight = FIFTH_QUEST_DEMON_FRAME_HEIGHT * FIFTH_QUEST_DEMON_DRAW_SCALE;
        return frameHeight * FIFTH_QUEST_DEMON_HITBOX_HEIGHT_SCALE;
    }

    private void drawFirstQuestDialogueOverlay(SpriteBatch batch) {
        if (uiCamera == null) {
            return;
        }
        if (firstQuestFatherDialogueBoxTex != null) {
            drawFirstQuestFatherDialogueOverlay(batch);
            return;
        }
        float panelWidth = Math.min(uiCamera.viewportWidth * 0.72f, 760f);
        float panelHeight = Math.min(uiCamera.viewportHeight * 0.42f, 320f);
        float panelX = (uiCamera.viewportWidth - panelWidth) * 0.5f;
        float panelY = (uiCamera.viewportHeight - panelHeight) * 0.5f - 20f;
        drawQuestPanel(batch, panelX, panelY, panelWidth, panelHeight);
        String speaker = activeQuestDialogueSpeakers.length == 0
                ? ""
                : activeQuestDialogueSpeakers[Math.max(0,
                Math.min(activeQuestDialogueSpeakers.length - 1, firstQuestDialogueLineIndex))];
        speaker = resolveStorySpeaker(speaker);
        if (firstQuestTitleFont != null) {
            float baseScaleX = firstQuestTitleFont.getData().scaleX;
            float baseScaleY = firstQuestTitleFont.getData().scaleY;
            firstQuestTitleFont.getData().setScale(
                    baseScaleX * FIRST_QUEST_OVERLAY_HEADER_SCALE,
                    baseScaleY * FIRST_QUEST_OVERLAY_HEADER_SCALE);
            drawCenteredQuestText(batch, firstQuestTitleFont, speaker,
                    uiCamera.viewportWidth * 0.5f,
                    panelY + panelHeight - 36f + FIRST_QUEST_DIALOGUE_HEADER_OFFSET_Y,
                    0.88f, 0.76f, 0.40f, 1f);
            firstQuestTitleFont.getData().setScale(baseScaleX, baseScaleY);
        }

        String line = visibleQuestDialogueLine();
        drawWrappedQuestText(batch, firstQuestBodyFont, line,
                panelX + 60f, panelY + panelHeight - 90f, panelWidth - 120f,
                0.92f, 0.90f, 0.86f, 1f);
        drawCenteredQuestText(batch, firstQuestHintFont, "Press E to continue",
                uiCamera.viewportWidth * 0.5f,
                panelY + 26f,
                0.76f, 0.72f, 0.66f, 1f);
    }

    private void drawFirstQuestFatherDialogueOverlay(SpriteBatch batch) {
        if (uiCamera == null || firstQuestFatherDialogueBoxTex == null) {
            return;
        }
        float drawWidth = Math.min(uiCamera.viewportWidth * FIRST_QUEST_FATHER_DIALOGUE_BOX_WIDTH_RATIO,
                FIRST_QUEST_FATHER_DIALOGUE_BOX_MAX_WIDTH);
        float aspectRatio = firstQuestFatherDialogueBoxTex.getHeight() / (float) firstQuestFatherDialogueBoxTex.getWidth();
        float drawHeight = drawWidth * aspectRatio;
        float drawX = (uiCamera.viewportWidth - drawWidth) * 0.5f;
        float drawY = FIRST_QUEST_FATHER_DIALOGUE_BOX_BOTTOM_MARGIN;
        float scale = drawWidth / firstQuestFatherDialogueBoxTex.getWidth();
        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(firstQuestFatherDialogueBoxTex, drawX, drawY, drawWidth, drawHeight);

        String speaker = activeQuestDialogueSpeakers.length == 0
                ? ""
                : activeQuestDialogueSpeakers[Math.max(0,
                Math.min(activeQuestDialogueSpeakers.length - 1, firstQuestDialogueLineIndex))];
        speaker = resolveStorySpeaker(speaker);
        if (firstQuestTitleFont != null) {
            float baseScaleX = firstQuestTitleFont.getData().scaleX;
            float baseScaleY = firstQuestTitleFont.getData().scaleY;
            firstQuestTitleFont.getData().setScale(
                    baseScaleX * FIRST_QUEST_OVERLAY_HEADER_SCALE * FIRST_QUEST_FATHER_DIALOGUE_BOX_SPEAKER_SCALE_MULTIPLIER,
                    baseScaleY * FIRST_QUEST_OVERLAY_HEADER_SCALE * FIRST_QUEST_FATHER_DIALOGUE_BOX_SPEAKER_SCALE_MULTIPLIER);
            drawCenteredQuestText(batch, firstQuestTitleFont, speaker,
                    drawX + drawWidth * FIRST_QUEST_FATHER_DIALOGUE_BOX_SPEAKER_CENTER_X_RATIO,
                    drawY + drawHeight - FIRST_QUEST_FATHER_DIALOGUE_BOX_SPEAKER_BASELINE_FROM_TOP * scale
                            + FIRST_QUEST_FATHER_DIALOGUE_BOX_SPEAKER_OFFSET_Y,
                    0.94f, 0.90f, 0.78f, 1f);
            firstQuestTitleFont.getData().setScale(baseScaleX, baseScaleY);
        }

        String line = visibleQuestDialogueLine();
        float baseBodyScaleX = firstQuestBodyFont == null ? 1f : firstQuestBodyFont.getData().scaleX;
        float baseBodyScaleY = firstQuestBodyFont == null ? 1f : firstQuestBodyFont.getData().scaleY;
        if (firstQuestBodyFont != null) {
            firstQuestBodyFont.getData().setScale(
                    baseBodyScaleX * FIRST_QUEST_FATHER_DIALOGUE_BOX_TEXT_SCALE_MULTIPLIER,
                    baseBodyScaleY * FIRST_QUEST_FATHER_DIALOGUE_BOX_TEXT_SCALE_MULTIPLIER);
        }
        drawWrappedQuestTextAligned(batch, firstQuestBodyFont, line,
                drawX + FIRST_QUEST_FATHER_DIALOGUE_BOX_TEXT_PADDING_LEFT * scale
                        + FIRST_QUEST_FATHER_DIALOGUE_BOX_TEXT_OFFSET_X,
                drawY + drawHeight - FIRST_QUEST_FATHER_DIALOGUE_BOX_TEXT_TOP_FROM_TOP * scale
                        + FIRST_QUEST_FATHER_DIALOGUE_BOX_TEXT_OFFSET_Y,
                drawWidth - (FIRST_QUEST_FATHER_DIALOGUE_BOX_TEXT_PADDING_LEFT
                + FIRST_QUEST_FATHER_DIALOGUE_BOX_TEXT_PADDING_RIGHT) * scale,
                Align.left,
                0.27f, 0.19f, 0.09f, 1f);
        if (firstQuestBodyFont != null) {
            firstQuestBodyFont.getData().setScale(baseBodyScaleX, baseBodyScaleY);
        }
        drawCenteredQuestText(batch, firstQuestHintFont, "Press E to continue",
                drawX + drawWidth - FIRST_QUEST_FATHER_DIALOGUE_BOX_HINT_RIGHT_PADDING * scale
                        + FIRST_QUEST_FATHER_DIALOGUE_BOX_HINT_OFFSET_X,
                drawY + FIRST_QUEST_FATHER_DIALOGUE_BOX_HINT_BOTTOM_PADDING * scale
                        + FIRST_QUEST_FATHER_DIALOGUE_BOX_HINT_OFFSET_Y - 8f,
                0.44f, 0.33f, 0.20f, 1f);
    }

    private void drawSixthQuestPortalChoiceOverlay(SpriteBatch batch) {
        if (uiCamera == null) {
            return;
        }
        float drawWidth = Math.min(uiCamera.viewportWidth * SIXTH_QUEST_PORTAL_CHOICE_BOX_WIDTH_RATIO,
                SIXTH_QUEST_PORTAL_CHOICE_BOX_MAX_WIDTH);
        float drawHeight;
        float drawX = (uiCamera.viewportWidth - drawWidth) * 0.5f;
        float drawY = SIXTH_QUEST_PORTAL_CHOICE_BOX_BOTTOM_MARGIN;
        if (firstQuestFatherDialogueBoxTex != null) {
            float aspectRatio = firstQuestFatherDialogueBoxTex.getHeight() / (float) firstQuestFatherDialogueBoxTex.getWidth();
            drawHeight = drawWidth * aspectRatio;
            batch.setColor(1f, 1f, 1f, 1f);
            batch.draw(firstQuestFatherDialogueBoxTex, drawX, drawY, drawWidth, drawHeight);
        } else {
            drawHeight = Math.min(uiCamera.viewportHeight * 0.42f, 320f);
            drawQuestPanel(batch, drawX, drawY, drawWidth, drawHeight);
        }
        float scale = firstQuestFatherDialogueBoxTex != null
                ? drawWidth / firstQuestFatherDialogueBoxTex.getWidth()
                : 1f;

        if (firstQuestTitleFont != null) {
            float baseScaleX = firstQuestTitleFont.getData().scaleX;
            float baseScaleY = firstQuestTitleFont.getData().scaleY;
            firstQuestTitleFont.getData().setScale(
                    baseScaleX * FIRST_QUEST_OVERLAY_HEADER_SCALE * 1.65f,
                    baseScaleY * FIRST_QUEST_OVERLAY_HEADER_SCALE * 1.65f);
            drawCenteredQuestText(batch, firstQuestTitleFont, "Portal",
                    drawX + drawWidth * SIXTH_QUEST_PORTAL_CHOICE_TITLE_CENTER_X_RATIO,
                    drawY + drawHeight - SIXTH_QUEST_PORTAL_CHOICE_TITLE_BASELINE_FROM_TOP * scale
                            + SIXTH_QUEST_PORTAL_CHOICE_TITLE_OFFSET_Y
                            + SIXTH_QUEST_PORTAL_CHOICE_TITLE_EXTRA_OFFSET_Y,
                    0.94f, 0.90f, 0.78f, 1f);
            firstQuestTitleFont.getData().setScale(baseScaleX, baseScaleY);
        }

        String portalPrompt = "Enter the final stage and step into the Demon Castle?";
        drawWrappedQuestTextAligned(batch, firstQuestBodyFont, portalPrompt,
                drawX + SIXTH_QUEST_PORTAL_CHOICE_TEXT_PADDING_LEFT * scale + SIXTH_QUEST_PORTAL_CHOICE_TEXT_OFFSET_X,
                drawY + drawHeight - SIXTH_QUEST_PORTAL_CHOICE_TEXT_TOP_FROM_TOP * scale
                        + SIXTH_QUEST_PORTAL_CHOICE_TEXT_OFFSET_Y
                        + SIXTH_QUEST_PORTAL_CHOICE_TEXT_EXTRA_OFFSET_Y,
                drawWidth - (SIXTH_QUEST_PORTAL_CHOICE_TEXT_PADDING_LEFT
                + SIXTH_QUEST_PORTAL_CHOICE_TEXT_PADDING_RIGHT) * scale,
                Align.left,
                0.27f, 0.19f, 0.09f, 1f);

        float buttonWidth = SIXTH_QUEST_PORTAL_CHOICE_BUTTON_WIDTH * scale;
        float buttonHeight = SIXTH_QUEST_PORTAL_CHOICE_BUTTON_HEIGHT * scale;
        float buttonY = drawY + SIXTH_QUEST_PORTAL_CHOICE_BUTTON_BOTTOM_PADDING * scale
                + SIXTH_QUEST_PORTAL_CHOICE_BUTTON_EXTRA_OFFSET_Y;
        float centerX = drawX + drawWidth * 0.5f;
        sixthQuestPortalChoiceYesBounds.set(
                centerX - SIXTH_QUEST_PORTAL_CHOICE_BUTTON_GAP * 0.5f * scale - buttonWidth,
                buttonY,
                buttonWidth,
                buttonHeight);
        sixthQuestPortalChoiceNoBounds.set(
                centerX + SIXTH_QUEST_PORTAL_CHOICE_BUTTON_GAP * 0.5f * scale,
                buttonY,
                buttonWidth,
                buttonHeight);
        drawSixthQuestPortalChoiceButton(batch,
                sixthQuestPortalChoiceYesBounds,
                "Yes",
                sixthQuestPortalChoiceIndex == 0,
                SIXTH_QUEST_PORTAL_CHOICE_BUTTON_TEXT_SCALE);
        drawSixthQuestPortalChoiceButton(batch,
                sixthQuestPortalChoiceNoBounds,
                "No",
                sixthQuestPortalChoiceIndex == 1,
                SIXTH_QUEST_PORTAL_CHOICE_BUTTON_TEXT_SCALE);

        drawCenteredQuestText(batch, firstQuestHintFont, "Choose with Left/Right, Enter, or click",
                drawX + drawWidth - SIXTH_QUEST_PORTAL_CHOICE_HINT_RIGHT_PADDING * scale
                        + SIXTH_QUEST_PORTAL_CHOICE_HINT_EXTRA_OFFSET_X,
                drawY + SIXTH_QUEST_PORTAL_CHOICE_HINT_BOTTOM_PADDING * scale
                        + SIXTH_QUEST_PORTAL_CHOICE_HINT_EXTRA_OFFSET_Y,
                0.44f, 0.33f, 0.20f, 1f);
    }

    private void drawFifthQuestDeathChoiceOverlay(SpriteBatch batch) {
        if (uiCamera == null) {
            return;
        }
        float drawWidth = Math.min(uiCamera.viewportWidth * SIXTH_QUEST_PORTAL_CHOICE_BOX_WIDTH_RATIO,
                SIXTH_QUEST_PORTAL_CHOICE_BOX_MAX_WIDTH);
        float drawHeight;
        float drawX = (uiCamera.viewportWidth - drawWidth) * 0.5f;
        float drawY = SIXTH_QUEST_PORTAL_CHOICE_BOX_BOTTOM_MARGIN;
        if (firstQuestFatherDialogueBoxTex != null) {
            float aspectRatio = firstQuestFatherDialogueBoxTex.getHeight() / (float) firstQuestFatherDialogueBoxTex.getWidth();
            drawHeight = drawWidth * aspectRatio;
            batch.setColor(1f, 1f, 1f, 1f);
            batch.draw(firstQuestFatherDialogueBoxTex, drawX, drawY, drawWidth, drawHeight);
        } else {
            drawHeight = Math.min(uiCamera.viewportHeight * 0.42f, 320f);
            drawQuestPanel(batch, drawX, drawY, drawWidth, drawHeight);
        }
        float scale = firstQuestFatherDialogueBoxTex != null
                ? drawWidth / firstQuestFatherDialogueBoxTex.getWidth()
                : 1f;

        if (firstQuestTitleFont != null) {
            float baseScaleX = firstQuestTitleFont.getData().scaleX;
            float baseScaleY = firstQuestTitleFont.getData().scaleY;
            firstQuestTitleFont.getData().setScale(
                    baseScaleX * FIRST_QUEST_OVERLAY_HEADER_SCALE * 1.55f,
                    baseScaleY * FIRST_QUEST_OVERLAY_HEADER_SCALE * 1.55f);
            drawCenteredQuestText(batch, firstQuestTitleFont, FIFTH_QUEST_NOTIFICATION_TITLE,
                    drawX + drawWidth * SIXTH_QUEST_PORTAL_CHOICE_TITLE_CENTER_X_RATIO,
                    drawY + drawHeight - SIXTH_QUEST_PORTAL_CHOICE_TITLE_BASELINE_FROM_TOP * scale
                            + SIXTH_QUEST_PORTAL_CHOICE_TITLE_OFFSET_Y,
                    0.94f, 0.90f, 0.78f, 1f);
            firstQuestTitleFont.getData().setScale(baseScaleX, baseScaleY);
        }

        String prompt = "You fell in the abandoned village. Redo the entire quest, or restart from the first fight?";
        drawWrappedQuestTextAligned(batch, firstQuestBodyFont, prompt,
                drawX + SIXTH_QUEST_PORTAL_CHOICE_TEXT_PADDING_LEFT * scale + SIXTH_QUEST_PORTAL_CHOICE_TEXT_OFFSET_X,
                drawY + drawHeight - SIXTH_QUEST_PORTAL_CHOICE_TEXT_TOP_FROM_TOP * scale
                        + SIXTH_QUEST_PORTAL_CHOICE_TEXT_OFFSET_Y,
                drawWidth - (SIXTH_QUEST_PORTAL_CHOICE_TEXT_PADDING_LEFT
                + SIXTH_QUEST_PORTAL_CHOICE_TEXT_PADDING_RIGHT) * scale,
                Align.left,
                0.27f, 0.19f, 0.09f, 1f);

        float buttonWidth = 184f * scale;
        float buttonHeight = SIXTH_QUEST_PORTAL_CHOICE_BUTTON_HEIGHT * scale;
        float buttonY = drawY + SIXTH_QUEST_PORTAL_CHOICE_BUTTON_BOTTOM_PADDING * scale;
        float centerX = drawX + drawWidth * 0.5f;
        fifthQuestDeathChoiceQuestBounds.set(
                centerX - SIXTH_QUEST_PORTAL_CHOICE_BUTTON_GAP * 0.5f * scale - buttonWidth,
                buttonY,
                buttonWidth,
                buttonHeight);
        fifthQuestDeathChoiceFightBounds.set(
                centerX + SIXTH_QUEST_PORTAL_CHOICE_BUTTON_GAP * 0.5f * scale,
                buttonY,
                buttonWidth,
                buttonHeight);
        drawSixthQuestPortalChoiceButton(
                batch,
                fifthQuestDeathChoiceQuestBounds,
                "Entire Quest",
                fifthQuestDeathChoiceIndex == 0,
                1f);
        drawSixthQuestPortalChoiceButton(
                batch,
                fifthQuestDeathChoiceFightBounds,
                "Fight Only",
                fifthQuestDeathChoiceIndex == 1,
                1f);

        drawCenteredQuestText(batch, firstQuestHintFont, "Choose with Left/Right, Enter, or click",
                drawX + drawWidth - SIXTH_QUEST_PORTAL_CHOICE_HINT_RIGHT_PADDING * scale,
                drawY + SIXTH_QUEST_PORTAL_CHOICE_HINT_BOTTOM_PADDING * scale,
                0.44f, 0.33f, 0.20f, 1f);
    }

    private void drawSixthQuestPortalChoiceButton(SpriteBatch batch,
                                                  Rectangle bounds,
                                                  String text,
                                                  boolean selected,
                                                  float textScaleMultiplier) {
        if (batch == null || bounds == null || dayNightOverlayTex == null) {
            return;
        }
        if (selected) {
            batch.setColor(0.71f, 0.56f, 0.22f, 0.36f);
        } else {
            batch.setColor(0f, 0f, 0f, 0.34f);
        }
        batch.draw(dayNightOverlayTex, bounds.x, bounds.y, bounds.width, bounds.height);
        batch.setColor(selected ? 0.88f : 0.44f, selected ? 0.70f : 0.31f, selected ? 0.30f : 0.16f, 0.95f);
        drawQuestRectOutline(batch, bounds, 2f);
        if (firstQuestBodyFont != null) {
            float baseScaleX = firstQuestBodyFont.getData().scaleX;
            float baseScaleY = firstQuestBodyFont.getData().scaleY;
            firstQuestBodyFont.getData().setScale(
                    baseScaleX * textScaleMultiplier,
                    baseScaleY * textScaleMultiplier);
            drawCenteredQuestText(batch, firstQuestBodyFont, text,
                    bounds.x + bounds.width * 0.5f,
                    bounds.y + bounds.height * 0.65f,
                    selected ? 0.24f : 0.92f,
                    selected ? 0.16f : 0.88f,
                    selected ? 0.07f : 0.80f,
                    1f);
            firstQuestBodyFont.getData().setScale(baseScaleX, baseScaleY);
        }
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private String resolveStorySpeaker(String speaker) {
        if (speaker == null || speaker.isBlank()) {
            return "";
        }
        return DEFAULT_CHARACTER_NAME.equalsIgnoreCase(speaker.trim()) ? characterName : speaker;
    }

    private void drawFirstQuestItemCardOverlay(SpriteBatch batch) {
        if (uiCamera == null) {
            return;
        }
        float panelWidth = Math.min(uiCamera.viewportWidth * 0.58f, 580f);
        float panelHeight = Math.min(uiCamera.viewportHeight * 0.72f, 540f);
        float panelX = (uiCamera.viewportWidth - panelWidth) * 0.5f;
        float panelY = (uiCamera.viewportHeight - panelHeight) * 0.5f - 12f;
        drawQuestPanel(batch, panelX, panelY, panelWidth, panelHeight);
        boolean hasItemTitle = activeQuestItemTitle != null && !activeQuestItemTitle.isBlank();
        boolean useReferenceItemCardLayout = usesReferenceItemCardLayout();
        if (firstQuestTitleFont != null && hasItemTitle) {
            float baseScaleX = firstQuestTitleFont.getData().scaleX;
            float baseScaleY = firstQuestTitleFont.getData().scaleY;
            float titleScaleMultiplier = useReferenceItemCardLayout ? 1f : itemCardTitleScaleMultiplier();
            float titleOffsetY = useReferenceItemCardLayout ? 0f : itemCardTitleOffsetY();
            firstQuestTitleFont.getData().setScale(
                    baseScaleX * activeQuestItemTitleScale * titleScaleMultiplier,
                    baseScaleY * activeQuestItemTitleScale * titleScaleMultiplier);
            drawCenteredQuestText(batch, firstQuestTitleFont, activeQuestItemTitle,
                    uiCamera.viewportWidth * 0.5f,
                    panelY + panelHeight - 64f + activeQuestItemTitleOffsetY + titleOffsetY,
                    0.88f, 0.76f, 0.40f, 1f);
            firstQuestTitleFont.getData().setScale(baseScaleX, baseScaleY);
        }

        if (activeQuestItemTexture != null) {
            float maxImageWidth = panelWidth * 0.42f;
            float maxImageHeight = panelHeight * 0.44f;
            float scale = Math.min(maxImageWidth / activeQuestItemTexture.getWidth(),
                    maxImageHeight / activeQuestItemTexture.getHeight());
            float drawWidth = activeQuestItemTexture.getWidth() * scale;
            float drawHeight = activeQuestItemTexture.getHeight() * scale;
            float drawCenterX = panelX + panelWidth * 0.5f;
            float drawCenterY = panelY + panelHeight * 0.42f + drawHeight * 0.5f;
            drawWidth *= activeQuestItemImageScaleMultiplier;
            drawHeight *= activeQuestItemImageScaleMultiplier;
            float drawX = drawCenterX - drawWidth * 0.5f;
            float drawY = drawCenterY - drawHeight * 0.5f;
            batch.setColor(1f, 1f, 1f, 1f);
            batch.draw(activeQuestItemTexture, drawX, drawY, drawWidth, drawHeight);
        }

        float descriptionOffsetY = useReferenceItemCardLayout ? -20f : activeQuestItemDescriptionOffsetY;
        float hintOffsetY = useReferenceItemCardLayout ? 0f : activeQuestItemHintOffsetY;
        drawWrappedQuestText(batch, firstQuestBodyFont, activeQuestItemDescription,
                panelX + 52f, panelY + panelHeight * 0.36f + descriptionOffsetY, panelWidth - 104f,
                0.92f, 0.90f, 0.86f, 1f);
        drawCenteredQuestText(batch, firstQuestHintFont, "Press E to continue",
                uiCamera.viewportWidth * 0.5f,
                panelY + 44f + hintOffsetY,
                0.76f, 0.72f, 0.66f, 1f);
    }

    private boolean usesReferenceItemCardLayout() {
        return activeQuestItemTexture != null
                || activeQuestItemCardId == QuestItemCardId.FOURTH_QUEST_SPATIAL_DASH_UNLOCKED;
    }

    private void drawAbilityCooldownUi() {
        if (batch == null
                || uiCamera == null
                || abilityCooldownCircleTex == null
                || firstQuestHintFont == null
                || firstQuestLayout == null
                || (hudRenderer != null && hudRenderer.isMainMenuVisible())
                || isFirstQuestOverlayActive()) {
            return;
        }

        boolean showDashAttack = isDashAttackUnlocked();
        boolean showSpatialDash = isSpatialDashUnlocked();
        int visibleCount = (showDashAttack ? 1 : 0) + (showSpatialDash ? 1 : 0);
        if (visibleCount <= 0) {
            return;
        }

        uiCamera.update();
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        batch.setShader(null);

        float totalWidth = visibleCount * ABILITY_COOLDOWN_ICON_SIZE
                + Math.max(0, visibleCount - 1) * ABILITY_COOLDOWN_ICON_GAP;
        float centerX = uiCamera.viewportWidth - ABILITY_COOLDOWN_MARGIN_RIGHT - totalWidth + ABILITY_COOLDOWN_ICON_SIZE * 0.5f;
        float centerY = ABILITY_COOLDOWN_MARGIN_BOTTOM + ABILITY_COOLDOWN_ICON_SIZE * 0.5f;

        if (showDashAttack) {
            drawAbilityCooldownIndicator(
                    centerX,
                    centerY,
                    "E",
                    "E",
                    dashAttackAbilityCooldownTimer,
                    DASH_ATTACK_ABILITY_COOLDOWN_SECONDS,
                    0.86f, 0.62f, 0.28f);
            centerX += ABILITY_COOLDOWN_ICON_SIZE + ABILITY_COOLDOWN_ICON_GAP;
        }
        if (showSpatialDash) {
            drawAbilityCooldownIndicator(
                    centerX,
                    centerY,
                    "R",
                    "RMB",
                    spatialDashAbilityCooldownTimer,
                    SPATIAL_DASH_ABILITY_COOLDOWN_SECONDS,
                    0.42f, 0.72f, 0.92f);
        }

        batch.end();
    }

    private void drawAbilityCooldownIndicator(
            float centerX,
            float centerY,
            String readyText,
            String caption,
            float cooldownRemaining,
            float cooldownDuration,
            float readyR,
            float readyG,
            float readyB) {
        float size = ABILITY_COOLDOWN_ICON_SIZE;
        float drawX = centerX - size * 0.5f;
        float drawY = centerY - size * 0.5f;

        batch.setColor(0f, 0f, 0f, 0.32f);
        batch.draw(abilityCooldownCircleTex, drawX + 2f, drawY - 2f, size, size);
        batch.setColor(0.10f, 0.10f, 0.10f, 0.86f);
        batch.draw(abilityCooldownCircleTex, drawX, drawY, size, size);
        batch.setColor(readyR, readyG, readyB, cooldownRemaining > 0f ? 0.24f : 0.92f);
        batch.draw(abilityCooldownCircleTex, drawX + 2f, drawY + 2f, size - 4f, size - 4f);

        if (cooldownRemaining > 0f) {
            float ratio = clamp01(cooldownRemaining / Math.max(0.001f, cooldownDuration));
            int srcHeight = Math.max(1, Math.round(abilityCooldownCircleTex.getHeight() * ratio));
            float overlayHeight = size * ratio;
            batch.setColor(0f, 0f, 0f, 0.72f);
            batch.draw(
                    abilityCooldownCircleTex,
                    drawX,
                    drawY + size - overlayHeight,
                    size,
                    overlayHeight,
                    0,
                    abilityCooldownCircleTex.getHeight() - srcHeight,
                    abilityCooldownCircleTex.getWidth(),
                    srcHeight,
                    false,
                    false);
        }

        float baseScaleX = firstQuestHintFont.getData().scaleX;
        float baseScaleY = firstQuestHintFont.getData().scaleY;
        String innerText = cooldownRemaining > 0f
                ? Integer.toString(Math.max(1, (int) Math.ceil(cooldownRemaining)))
                : readyText;

        firstQuestHintFont.getData().setScale(baseScaleX * (cooldownRemaining > 0f ? 0.92f : 1f),
                baseScaleY * (cooldownRemaining > 0f ? 0.92f : 1f));
        firstQuestHintFont.setColor(0.98f, 0.96f, 0.92f, 1f);
        firstQuestLayout.setText(firstQuestHintFont, innerText);
        firstQuestHintFont.draw(
                batch,
                firstQuestLayout,
                centerX - firstQuestLayout.width * 0.5f,
                centerY + firstQuestLayout.height * 0.32f);

        firstQuestHintFont.getData().setScale(baseScaleX * 0.56f, baseScaleY * 0.56f);
        firstQuestHintFont.setColor(0.84f, 0.80f, 0.74f, 0.98f);
        firstQuestLayout.setText(firstQuestHintFont, caption);
        firstQuestHintFont.draw(
                batch,
                firstQuestLayout,
                centerX - firstQuestLayout.width * 0.5f,
                drawY - 4f + firstQuestLayout.height);

        firstQuestHintFont.setColor(1f, 1f, 1f, 1f);
        firstQuestHintFont.getData().setScale(baseScaleX, baseScaleY);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private float itemCardTitleScaleMultiplier() {
        if (activeQuestItemCardId == QuestItemCardId.FIRST_QUEST_HERB_AND_SPICE) {
            return 1f;
        }
        return FIRST_QUEST_OVERLAY_HEADER_SCALE;
    }

    private float itemCardTitleOffsetY() {
        if (activeQuestItemCardId == QuestItemCardId.FIRST_QUEST_HERB_AND_SPICE) {
            return 0f;
        }
        return FIRST_QUEST_ITEM_CARD_HEADER_OFFSET_Y;
    }

    private float itemCardImageOffsetY() {
        if (activeQuestItemCardId == QuestItemCardId.SECOND_QUEST_SCORCHED_FRAGMENT) {
            return SECOND_QUEST_FRAGMENT_CARD_IMAGE_OFFSET_Y;
        }
        if (activeQuestItemCardId == QuestItemCardId.FOURTH_QUEST_CONDENSED_CORE) {
            return FOURTH_QUEST_CONDENSED_CORE_CARD_IMAGE_OFFSET_Y;
        }
        return 0f;
    }

    private void drawQuestPanel(SpriteBatch batch, float x, float y, float width, float height) {
        if (firstQuestPanelTex != null) {
            batch.setColor(1f, 1f, 1f, 0.97f);
            batch.draw(firstQuestPanelTex, x, y, width, height);
            batch.setColor(1f, 1f, 1f, 1f);
            return;
        }
        batch.setColor(0f, 0f, 0f, 0.88f);
        batch.draw(dayNightOverlayTex, x, y, width, height);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void drawCenteredQuestText(SpriteBatch batch, BitmapFont font, String text,
                                       float centerX, float baselineY,
                                       float r, float g, float b, float a) {
        if (font == null || text == null || firstQuestLayout == null) {
            return;
        }
        font.setColor(r, g, b, a);
        firstQuestLayout.setText(font, text);
        font.draw(batch, firstQuestLayout, centerX - firstQuestLayout.width * 0.5f, baselineY);
        font.setColor(1f, 1f, 1f, 1f);
    }

    private void drawWrappedQuestText(SpriteBatch batch, BitmapFont font, String text,
                                      float x, float topY, float width,
                                      float r, float g, float b, float a) {
        drawWrappedQuestTextAligned(batch, font, text, x, topY, width, Align.center, r, g, b, a);
    }

    private void drawWrappedQuestTextAligned(SpriteBatch batch, BitmapFont font, String text,
                                             float x, float topY, float width, int align,
                                             float r, float g, float b, float a) {
        if (font == null || text == null) {
            return;
        }
        font.setColor(r, g, b, a);
        font.draw(batch, text, x, topY, width, align, true);
        font.setColor(1f, 1f, 1f, 1f);
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
        if (isPlayerDead() && playerDeathAnim != null) {
            return playerDeathAnim.getKeyFrame(playerDeathStateTime, false);
        }
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

    private float currentPlayerAttackDamageMultiplier() {
        if (activeAttackAnim == dashAttackAnim) {
            return DASH_ATTACK_DAMAGE_MULTIPLIER;
        }
        if (activeAttackAnim == spetialDashAnim) {
            return SPATIAL_DASH_DAMAGE_MULTIPLIER;
        }
        return 1f;
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

    private void drawFirstQuestCafeNpc(float saturation, boolean drawBehindPlayer) {
        if (!MAP_ID_VILLAGE.equals(world.activeMapId)
                || firstQuestStage < FIRST_QUEST_DELIVER_TO_CAFE
                || firstQuestCafeNpcIdleAnim == null) {
            return;
        }
        Vector2 npcPosition = firstQuestCafeNpcPosition();
        if (npcPosition == null) {
            return;
        }
        TextureRegion frame = firstQuestCafeNpcIdleAnim.getKeyFrame(firstQuestCafeNpcStateTime, true);
        if (frame == null) {
            return;
        }
        float playerDepthBasisY = player.bounds.y;
        float drawW = frame.getRegionWidth() * FIRST_QUEST_CAFE_NPC_DRAW_SCALE;
        float drawH = frame.getRegionHeight() * FIRST_QUEST_CAFE_NPC_DRAW_SCALE;
        float npcDepthBasisY = npcPosition.y - drawH * 0.35f;
        boolean npcBehindPlayer = npcDepthBasisY > playerDepthBasisY;
        if (npcBehindPlayer != drawBehindPlayer) {
            return;
        }
        if (saturationShader != null) {
            batch.setShader(saturationShader);
            saturationShader.setUniformf("u_saturation", saturation);
        }
        float drawX = npcPosition.x - drawW * 0.5f;
        float drawY = npcPosition.y - drawH * 0.5f;
        batch.draw(frame, drawX, drawY, drawW, drawH);
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
            TextureRegion frame = demon.attacking && activeAnim == bossArenaPhase2DemonAttackAnim
                    ? currentBossArenaPhase2DemonAttackFrame(animationTime)
                    : activeAnim.getKeyFrame(animationTime, loop);
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

    private void drawBossArenaPhase2Explosions(float saturation) {
        if (bossArenaPhase2Explosions.size == 0 || bossArenaPhase2ExplosionAnim == null) {
            return;
        }
        for (int i = 0; i < bossArenaPhase2Explosions.size; i++) {
            BossArenaPhase2ExplosionInstance burst = bossArenaPhase2Explosions.get(i);
            if (burst == null || burst.active) {
                continue;
            }
            drawBossArenaPhase2ExplosionTelegraph(burst);
        }
        if (saturationShader != null) {
            batch.setShader(saturationShader);
            saturationShader.setUniformf("u_saturation", saturation);
        }
        for (int i = 0; i < bossArenaPhase2Explosions.size; i++) {
            BossArenaPhase2ExplosionInstance burst = bossArenaPhase2Explosions.get(i);
            if (!burst.active) {
                continue;
            }
            TextureRegion frame = bossArenaPhase2ExplosionAnim.getKeyFrame(burst.stateTime, false);
            if (frame == null) {
                continue;
            }
            float drawScale = currentBossArenaPhase2ExplosionDrawScale();
            float drawW = frame.getRegionWidth() * drawScale;
            float drawH = frame.getRegionHeight() * drawScale;
            float drawX = burst.position.x - drawW * 0.5f;
            float drawY = burst.position.y - drawH * 0.5f;
            batch.draw(frame, drawX, drawY, drawW, drawH);
        }
        batch.setColor(1f, 1f, 1f, 1f);
        if (saturationShader != null) {
            batch.setShader(null);
        }
    }

    private void drawBossArenaPhase2ExplosionTelegraph(BossArenaPhase2ExplosionInstance burst) {
        if (burst == null || burst.active) {
            return;
        }
        TextureRegion telegraphRegion = shadowRegion;
        if (telegraphRegion == null && dayNightOverlayTex == null) {
            return;
        }

        float telegraphDuration = Math.max(0.001f, BOSS_ARENA_PHASE_2_EXPLOSION_CAST_DELAY_SECONDS);
        float progress = clamp01(1f - burst.delayTimer / telegraphDuration);
        float pulse = 0.5f + 0.5f * (float) Math.sin(progress * (float) Math.PI * 6f);
        float baseRadius = currentBossArenaPhase2ExplosionRadiusTiles() * DEFAULT_TILE_SIZE;
        float outerRadius = baseRadius * (1.08f + 0.06f * pulse);
        float innerRadius = outerRadius * 0.74f;

        batch.setShader(null);
        batch.setColor(0.92f, 0.08f, 0.08f, clamp01(0.2f + 0.22f * progress + 0.08f * pulse));
        if (telegraphRegion != null) {
            batch.draw(
                    telegraphRegion,
                    burst.position.x - outerRadius,
                    burst.position.y - outerRadius,
                    outerRadius * 2f,
                    outerRadius * 2f);
        } else {
            batch.draw(
                    dayNightOverlayTex,
                    burst.position.x - outerRadius,
                    burst.position.y - outerRadius,
                    outerRadius * 2f,
                    outerRadius * 2f);
        }

        batch.setColor(1f, 0.2f, 0.2f, clamp01(0.1f + 0.1f * progress));
        if (telegraphRegion != null) {
            batch.draw(
                    telegraphRegion,
                    burst.position.x - innerRadius,
                    burst.position.y - innerRadius,
                    innerRadius * 2f,
                    innerRadius * 2f);
        } else {
            batch.draw(
                    dayNightOverlayTex,
                    burst.position.x - innerRadius,
                    burst.position.y - innerRadius,
                    innerRadius * 2f,
                    innerRadius * 2f);
        }
        batch.setColor(1f, 1f, 1f, 1f);
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






