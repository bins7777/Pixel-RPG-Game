package com.pixelrpg.game.entities;

public final class PlayerLevelStats {
    public static final int MIN_LEVEL = 1;
    public static final int MAX_LEVEL = 4;

    private static final float[] MAX_HEALTH_BY_LEVEL = {
            100f,
            125f,
            155f,
            190f
    };

    private static final float[] MAX_STAMINA_BY_LEVEL = {
            50f,
            60f,
            72f,
            85f
    };

    private PlayerLevelStats() {
    }

    public static int clampLevel(int level) {
        return Math.max(MIN_LEVEL, Math.min(MAX_LEVEL, level));
    }

    public static float maxHealthForLevel(int level) {
        return MAX_HEALTH_BY_LEVEL[clampLevel(level) - 1];
    }

    public static float maxStaminaForLevel(int level) {
        return MAX_STAMINA_BY_LEVEL[clampLevel(level) - 1];
    }
}
