package insane96mcp.insanelib.mixin.accessor;

import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.common.EffectCure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(MobEffectInstance.class)
public interface MobEffectInstanceAccessor {
    @Accessor(value = "cures")
    Set<EffectCure> getCuresField();
}
