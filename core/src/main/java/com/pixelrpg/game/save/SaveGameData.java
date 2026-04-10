package com.pixelrpg.game.save;

import com.pixelrpg.game.GameDifficulty;

public class SaveGameData {
    public static class DemonCastleCheckpointData {
        public Integer sixthQuestStage;
        public Boolean sixthQuestEncounterSpawned;
        public Integer sixthQuestBossCalloutIndex;
        public Boolean sixthQuestLowHealthExchangePlayed;
        public Boolean boss1IntroPlayed;
        public Boolean boss1DefeatDoorTriggered;
        public Boolean boss1DefeatDoorOpened;
        public Float boss1DefeatDoorOpenDelayTimer;
        public BatState[] bats;
        public EvilWizardState[] evilWizards;
        public Golem1State[] golem1s;
        public FifthQuestDemonState[] fifthQuestDemons;
    }

    public static class BatState {
        public float x;
        public float y;
        public float maxHealth;
        public float health;
        public float chaseSpeed;
        public float playerDamage;
        public float attackIntervalSeconds;
        public boolean elite;
    }

    public static class EvilWizardState {
        public float x;
        public float y;
        public float zoneX;
        public float zoneY;
        public float zoneWidth;
        public float zoneHeight;
        public float depthBasisOffsetTiles;
        public float shadowDownOffsetTiles;
        public float maxHealth;
        public float health;
    }

    public static class Golem1State {
        public float x;
        public float y;
        public float zoneX;
        public float zoneY;
        public float zoneWidth;
        public float zoneHeight;
        public float chaseSpeed;
        public float maxHealth;
        public float health;
    }

    public static class FifthQuestDemonState {
        public float x;
        public float y;
        public String type;
        public int questEncounterId;
        public float zoneX;
        public float zoneY;
        public float zoneWidth;
        public float zoneHeight;
        public float maxHealth;
        public float health;
        public float chaseSpeed;
        public float playerDamage;
        public float attackIntervalSeconds;
        public float attackRange;
    }

    public String slotId;
    public String displayName;
    public String characterName;
    public GameDifficulty difficulty;
    public String mapId;
    public float playerBoundsX;
    public float playerBoundsY;
    public float currentHealth;
    public float maxHealth;
    public float currentMana;
    public float maxMana;
    public int playerLevel;
    public Integer firstQuestStage;
    public Integer secondQuestStage;
    public Integer thirdQuestStage;
    public Integer fourthQuestStage;
    public Integer fifthQuestStage;
    public Integer sixthQuestStage;
    public Integer progressionLevelCap;
    public Boolean scientistProgressionUnlocked;
    public Boolean thirdQuestAvailable;
    public Boolean gameCompleted;
    public DemonCastleCheckpointData demonCastleCheckpoint;
    public long savedAtEpochMillis;
}
