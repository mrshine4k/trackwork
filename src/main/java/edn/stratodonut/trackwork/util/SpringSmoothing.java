package edn.stratodonut.trackwork.util;

import net.minecraft.util.Mth;

/**
 * Critically damped spring for smooth visual interpolation. FPS-independent.
 *
 * Second-order system (position + velocity) that approaches the target as fast
 * as possible without overshoot. Replaces the previous first-order system which
 * had no velocity state — it fully converged between packet updates, causing
 * visible stepping at low update rates and jitter at high rates.
 *
 * Suspension travel: asymmetric. Compression slow (visual weight), rebound fast
 * (quick settle). Snap beyond 1.2 (cliff jumps teleport).
 *   new SpringSmoothing(Preset.SUSPENSION)
 *
 * Steering angle: symmetric, fast. No overshoot, left/right identical.
 *   new SpringSmoothing(Preset.STEERING)
 *
 * Driven wheel RPM: weighted. Gradual ramp-up, slightly slower deceleration.
 *   new SpringSmoothing(Preset.WHEELSPIN)
 *
 * Freespin wheel RPM: snappy. Swapped in via configure() on freespin state change.
 *   new SpringSmoothing(Preset.FREESPIN_HL)
 *
 * Spring math from orangeduck's simple_spring_damper_exact (MIT).
 */
public final class SpringSmoothing {

    public enum Preset {
        //                          halflife   snap       secondary
        //                          (s)        (blocks)   halflife (s)
        SUSPENSION(                 0.08f,     1.2f,      0.05f),
        STEERING(                   0.05f,     0f,        0f),
        WHEELSPIN(                  0.08f,     0f,        0.10f),

        FREESPIN_HL(                0.03f,     0f,        0f);

        public final float halflife;
        public final float snapDistance;
        public final float secondaryHalflife;

        Preset(float halflife, float snapDistance, float secondaryHalflife) {
            this.halflife = halflife;
            this.snapDistance = snapDistance;
            this.secondaryHalflife = secondaryHalflife;
        }
    }

    private static final float LN2 = 0.69314718056f;

    private float position;
    private float previousPosition;
    private float velocity;
    private float target;

    private float halflife;
    private float secondaryHalflife;
    private float snapDistance;

    public SpringSmoothing(Preset preset) {
        this.halflife = preset.halflife;
        this.secondaryHalflife = preset.secondaryHalflife;
        this.snapDistance = preset.snapDistance;
    }

    public void configure(float halflife, float secondaryHalflife) {
        this.halflife = halflife;
        this.secondaryHalflife = secondaryHalflife;
    }

    public void setTarget(float target) {
        this.target = target;
    }

    /** Advance by dt seconds. Call once per client tick with real elapsed time. */
    public float tick(float dt) {
        previousPosition = position;
        float gap = target - position;

        if (snapDistance > 0f && Math.abs(gap) > snapDistance) {
            position = target;
            velocity = 0f;
            return position;
        }

        float hl = halflife;
        if (secondaryHalflife > 0f && gap < 0f) {
            hl = secondaryHalflife;
        }

        float y = (4f * LN2) / (hl + 1e-5f) / 2f;
        float j0 = position - target;
        float j1 = velocity + j0 * y;
        float eydt = (float) Math.exp(-y * dt);

        position = eydt * (j0 + j1 * dt) + target;
        velocity = eydt * (velocity - j1 * y * dt);

        return position;
    }

    public float getValue() {
        return position;
    }

    /** For renderers: interpolate between ticks with Minecraft's partialTick (0..1). */
    public float getValue(float partialTick) {
        return Mth.lerp(partialTick, previousPosition, position);
    }

    /** Set position + target simultaneously. For NBT load, where interpolating from 0 would be wrong. */
    public void reset(float value) {
        this.position = value;
        this.previousPosition = value;
        this.velocity = 0f;
        this.target = value;
    }
}
