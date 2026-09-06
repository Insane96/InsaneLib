package insane96mcp.insanelib.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.insanelib.module.SoundOverrides;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.block.TntBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TntBlock.class)
public abstract class TntBlockMixin {
    // Targets the private static explode(Level, BlockPos, LivingEntity) overload (the one that actually plays TNT_PRIMED),
    // not the public no-entity explode(Level, BlockPos) which just delegates to it.
    @ModifyExpressionValue(method = "explode(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/LivingEntity;)V",
            at = @At(value = "FIELD", target = "Lnet/minecraft/sounds/SoundEvents;TNT_PRIMED:Lnet/minecraft/sounds/SoundEvent;"))
    private static SoundEvent insanelib$fuseSound(SoundEvent original, @Local PrimedTnt primedTnt) {
        return SoundOverrides.getFuseSound(primedTnt, original);
    }
}
