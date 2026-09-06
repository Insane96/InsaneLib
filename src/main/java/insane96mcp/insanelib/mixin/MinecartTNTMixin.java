package insane96mcp.insanelib.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanelib.module.SoundOverrides;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MinecartTNT.class)
public abstract class MinecartTNTMixin {
    @Unique
    private MinecartTNT self() {
        return (MinecartTNT) (Object) this;
    }

    @ModifyExpressionValue(method = "primeFuse", at = @At(value = "FIELD", target = "Lnet/minecraft/sounds/SoundEvents;TNT_PRIMED:Lnet/minecraft/sounds/SoundEvent;"))
    private SoundEvent insanelib$fuseSound(SoundEvent original) {
        return SoundOverrides.getFuseSound(self(), original);
    }
}
