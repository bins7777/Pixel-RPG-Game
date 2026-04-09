package com.pixelrpg.game;

public enum GameDifficulty {
    EASY("Easy", "Current balance. Standard incoming damage.", 1f, false),
    NORMAL("Normal", "You take 1.3x damage.", 1.3f, false),
    HARD("Hard", "You take 2x damage.", 2f, false),
    HARDCORE("Hardcore (1 Life)", "You take 2x damage. Death ends the run.", 2f, true);

    private final String label;
    private final String description;
    private final float incomingDamageMultiplier;
    private final boolean permanentDeath;

    GameDifficulty(String label, String description, float incomingDamageMultiplier, boolean permanentDeath) {
        this.label = label;
        this.description = description;
        this.incomingDamageMultiplier = incomingDamageMultiplier;
        this.permanentDeath = permanentDeath;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public float getIncomingDamageMultiplier() {
        return incomingDamageMultiplier;
    }

    public boolean isPermanentDeath() {
        return permanentDeath;
    }
}
