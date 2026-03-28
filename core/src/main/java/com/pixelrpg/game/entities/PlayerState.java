package com.pixelrpg.game.entities;

import com.badlogic.gdx.math.Rectangle;

/**
 * Mutable player state used by the gameplay screen.
 */
public class PlayerState {
    public final Rectangle bounds = new Rectangle();

    public float stateTime = 0f;
    public boolean moving = false;
    public boolean facingLeft = false;
    public boolean attacking = false;
    public float attackTimer = 0f;
    public float attackCooldownTimer = 0f;
    public int attackComboStep = 0;
    public boolean dashing = false;
    public float dashTimer = 0f;
    public float dashCooldownTimer = 0f;
    public float dashDirX = 1f;
    public float dashDirY = 0f;
    public boolean dashAttackActive = false;
    public float dashAttackMoveDuration = 0f;
    public float lastMoveX = 1f;
    public float lastMoveY = 0f;
    public boolean dashAttackFollowupQueued = false;

    public float currentHealth = PlayerLevelStats.maxHealthForLevel(PlayerLevelStats.MIN_LEVEL);
    public float maxHealth = PlayerLevelStats.maxHealthForLevel(PlayerLevelStats.MIN_LEVEL);
    public float currentMana = PlayerLevelStats.maxStaminaForLevel(PlayerLevelStats.MIN_LEVEL);
    public float maxMana = PlayerLevelStats.maxStaminaForLevel(PlayerLevelStats.MIN_LEVEL);
    public int playerLevel = PlayerLevelStats.MIN_LEVEL;
}
