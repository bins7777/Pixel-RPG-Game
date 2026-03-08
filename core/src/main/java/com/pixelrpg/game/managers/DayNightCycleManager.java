package com.pixelrpg.game.managers;

/**
 * Holds day/night progression state and blend calculations.
 */
public class DayNightCycleManager {
    private final float dayDuration;
    private final float sunsetDuration;
    private final float nightDuration;
    private final float sunriseDuration;
    private final float minSpeed;
    private final float maxSpeed;

    private float cycleTime = 0f;
    private float timeScale = 1f;

    public DayNightCycleManager(float dayDuration, float sunsetDuration, float nightDuration, float sunriseDuration,
                                float minSpeed, float maxSpeed) {
        this.dayDuration = dayDuration;
        this.sunsetDuration = sunsetDuration;
        this.nightDuration = nightDuration;
        this.sunriseDuration = sunriseDuration;
        this.minSpeed = minSpeed;
        this.maxSpeed = maxSpeed;
    }

    public void update(float delta) {
        float cycle = cycleLength();
        if (cycle <= 0f) {
            cycleTime = 0f;
            return;
        }
        cycleTime += delta * timeScale;
        while (cycleTime >= cycle) {
            cycleTime -= cycle;
        }
    }

    public void forceDayTime() {
        cycleTime = 0f;
    }

    public void forceNightTime() {
        cycleTime = dayDuration + sunsetDuration + (nightDuration * 0.5f);
    }

    public void adjustSpeed(float deltaScale) {
        setSpeed(timeScale + deltaScale);
    }

    public void setSpeed(float scale) {
        timeScale = Math.max(minSpeed, Math.min(maxSpeed, scale));
    }

    public float nightBlend() {
        float t = cycleTime;
        float dayEnd = dayDuration;
        float sunsetEnd = dayEnd + sunsetDuration;
        float nightEnd = sunsetEnd + nightDuration;
        float sunriseEnd = cycleLength();

        if (t < dayEnd) {
            return 0f;
        }
        if (t < sunsetEnd) {
            float x = clamp01((t - dayEnd) / sunsetDuration);
            return smoothStep(x);
        }
        if (t < nightEnd) {
            return 1f;
        }
        if (t < sunriseEnd) {
            float x = clamp01((t - nightEnd) / sunriseDuration);
            return 1f - smoothStep(x);
        }
        return 0f;
    }

    private float cycleLength() {
        return dayDuration + sunsetDuration + nightDuration + sunriseDuration;
    }

    private static float clamp01(float value) {
        return Math.max(0f, Math.min(1f, value));
    }

    private static float smoothStep(float t) {
        float x = clamp01(t);
        return x * x * (3f - 2f * x);
    }
}
