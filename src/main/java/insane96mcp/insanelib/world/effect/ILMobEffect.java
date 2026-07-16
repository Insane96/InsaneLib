package insane96mcp.insanelib.world.effect;

import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

/**
 * Basically a MobEffect with the possibility to set the mob effect as non-curable.
 * <p>
 * Since 26.1 removed NeoForge's EffectCure system, non-curability is enforced by cancelling
 * {@link MobEffectEvent.Remove}. Note that unlike the old cure system this also prevents removal
 * via commands.
 */
public class ILMobEffect extends MobEffect {
    boolean canBeCured;

    public ILMobEffect(MobEffectCategory typeIn, int liquidColorIn) {
        this(typeIn, liquidColorIn, true);
    }

    public ILMobEffect(MobEffectCategory typeIn, int liquidColorIn, boolean canBeCured) {
        super(typeIn, liquidColorIn);
        this.canBeCured = canBeCured;
    }

    public boolean canBeCured() {
        return this.canBeCured;
    }

    /**
     * Registered to the game event bus by InsaneLib. Prevents removal of non-curable effects,
     * both effect-level ({@link ILMobEffect} with {@code canBeCured = false}) and instance-level
     * ({@link MCUtils#createEffectInstance}).
     */
    public static void onEffectRemove(MobEffectEvent.Remove event) {
        if (event.getEffectInstance() == null)
            return;
        if ((event.getEffect().value() instanceof ILMobEffect ilMobEffect && !ilMobEffect.canBeCured())
                || MCUtils.isNonCurable(event.getEffectInstance()))
            event.setCanceled(true);
    }
}
