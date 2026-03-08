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

    public float currentHealth = 44250f;
    public float maxHealth = 48500f;
    public float currentMana = 1590f;
    public float maxMana = 2100f;
    public int playerLevel = 80;
}
