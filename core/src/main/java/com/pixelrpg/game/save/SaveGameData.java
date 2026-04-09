package com.pixelrpg.game.save;

import com.pixelrpg.game.GameDifficulty;

public class SaveGameData {
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
    public long savedAtEpochMillis;
}
