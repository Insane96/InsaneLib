package insane96mcp.insanelib.world.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Basically a MobEffect with the possibility to set the mob effect as non-curable (e.g. by milk or totems)
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
}
