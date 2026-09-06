package insane96mcp.insanelib.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.insanelib.module.SoundOverrides;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.item.PrimedTnt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Targets the anonymous DefaultDispenseItemBehavior that DispenseItemBehavior's static initializer registers for
// Blocks.TNT (dispensing TNT from a dispenser). Verified against the vanilla 1.21.1 compiled classes: it's the
// only DispenseItemBehavior$<n> anonymous class referencing SoundEvents.TNT_PRIMED. This index is compiler-assigned
// and can shift if Mojang reorders DispenseItemBehavior.bootStrap() in a future update — re-verify with javap if
// this mixin fails to apply after an MC version bump.
@Mixin(targets = "net.minecraft.core.dispenser.DispenseItemBehavior$10")
public abstract class DispenseItemBehaviorTntMixin {
    @ModifyExpressionValue(method = "execute", at = @At(value = "FIELD", target = "Lnet/minecraft/sounds/SoundEvents;TNT_PRIMED:Lnet/minecraft/sounds/SoundEvent;"))
    private SoundEvent insanelib$fuseSound(SoundEvent original, @Local PrimedTnt primedTnt) {
        return SoundOverrides.getFuseSound(primedTnt, original);
    }
}
