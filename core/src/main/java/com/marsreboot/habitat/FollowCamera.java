package com.marsreboot.habitat;

/**
 * Smooth 2D follow camera with deadzone, velocity look-ahead, smart terminal framing,
 * strict world-bounds clamping, and decaying trauma shake. Pure math (unit-testable without GL).
 */
public final class FollowCamera {
    private static final float DEADZONE_X = 24f;
    private static final float DEADZONE_Y = 15f;
    private static final float EXPLORE_FOLLOW_RATE = 7.5f;
    private static final float MODAL_PAN_RATE = 6.0f;
    private static final float LOOK_AHEAD_TIME = 0.16f;
    private static final float SHAKE_DECAY = 1.8f;
    private static final float MAX_SHAKE = 7f;

    private float focusX;
    private float focusY;
    private float camX;
    private float camY;
    private float renderX;
    private float renderY;
    private float trauma;
    private float shakeTime;
    private boolean initialized;

    public float camX() { return renderX; }
    public float camY() { return renderY; }

    public void addTrauma(float amount) {
        if (Float.isFinite(amount) && amount > 0f) {
            trauma = Math.min(1f, trauma + amount);
        }
    }

    public void update(HabitatWorld world, float delta, float viewWidth, float viewHeight, boolean reducedMotion) {
        float ax = world.x() + HabitatWorld.PLAYER_WIDTH * 0.5f;
        float ay = world.y() + HabitatWorld.PLAYER_HEIGHT * 0.5f;
        float dt = Float.isFinite(delta) && delta > 0f ? Math.min(delta, 0.25f) : 0f;

        if (!initialized) {
            focusX = ax;
            focusY = ay;
            camX = clampX(ax, viewWidth);
            camY = clampY(ay, viewHeight);
            renderX = camX;
            renderY = camY;
            initialized = true;
            return;
        }

        // Update deadzone focus anchor around astronaut
        float dx = ax - focusX;
        float dy = ay - focusY;
        if (Math.abs(dx) > DEADZONE_X) {
            focusX += dx - Math.signum(dx) * DEADZONE_X;
        }
        if (Math.abs(dy) > DEADZONE_Y) {
            focusY += dy - Math.signum(dy) * DEADZONE_Y;
        }

        float targetX;
        float targetY;
        float rate;
        if (world.mode() == HabitatWorld.Mode.TERMINAL) {
            // Frame Bio-Dome & Rover visibly on the right side while floating editor sits on left
            targetX = HabitatWorld.GARDEN.centerX() - viewWidth * 0.20f;
            targetY = HabitatWorld.GARDEN.centerY() - viewHeight * 0.03f;
            rate = MODAL_PAN_RATE;
        } else if (world.mode() == HabitatWorld.Mode.INSPECTION) {
            targetX = (ax + HabitatWorld.Facility.PANEL.bounds.centerX()) * 0.5f;
            targetY = (ay + HabitatWorld.Facility.PANEL.bounds.centerY()) * 0.5f;
            rate = MODAL_PAN_RATE;
        } else {
            targetX = focusX + world.vx() * LOOK_AHEAD_TIME;
            targetY = focusY + world.vy() * LOOK_AHEAD_TIME * 0.85f;
            rate = EXPLORE_FOLLOW_RATE;
        }

        targetX = clampX(targetX, viewWidth);
        targetY = clampY(targetY, viewHeight);

        float alpha = (reducedMotion || dt <= 0f) ? 1f : 1f - (float) Math.exp(-rate * dt);
        camX = clampX(camX + (targetX - camX) * alpha, viewWidth);
        camY = clampY(camY + (targetY - camY) * alpha, viewHeight);

        // Decaying trauma shake offset applied AFTER clamping follow center, then re-clamped
        float offX = 0f, offY = 0f;
        if (trauma > 0f && !reducedMotion && dt > 0f) {
            trauma = Math.max(0f, trauma - SHAKE_DECAY * dt);
            shakeTime += dt * 32f;
            float mag = trauma * trauma * MAX_SHAKE;
            offX = mag * (float) Math.sin(shakeTime * 1.7f);
            offY = mag * (float) Math.sin(shakeTime * 2.3f);
        } else if (reducedMotion) {
            trauma = 0f;
        }
        renderX = clampX(camX + offX, viewWidth);
        renderY = clampY(camY + offY, viewHeight);
    }

    private static float clampX(float cx, float viewWidth) {
        float halfW = Math.min(viewWidth * 0.5f, HabitatWorld.WIDTH * 0.5f);
        return Math.max(halfW, Math.min(HabitatWorld.WIDTH - halfW, cx));
    }

    private static float clampY(float cy, float viewHeight) {
        float halfH = Math.min(viewHeight * 0.5f, HabitatWorld.HEIGHT * 0.5f);
        return Math.max(halfH, Math.min(HabitatWorld.HEIGHT - halfH, cy));
    }
}
