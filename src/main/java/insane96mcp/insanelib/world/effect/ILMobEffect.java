package insane96mcp.insanelib.world.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.common.EffectCure;

import java.util.Set;

/**
 * Basically a MobEffect with the possibility to set the mob effect as non-curable
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

    @Override
    public void fillEffectCures(Set<EffectCure> cures, MobEffectInstance effectInstance) {
        if (canBeCured)
            super.fillEffectCures(cures, effectInstance);
    }
}
