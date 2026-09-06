package insane96mcp.insanelib.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanelib.module.SoundOverrides;
import insane96mcp.insanelib.network.message.CreeperDataSyncMessage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Creeper.class)
public abstract class CreeperMixin {
    @Unique
    private Creeper self() {
        return (Creeper) (Object) this;
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void onReadAdditionalSaveData(CompoundTag compound, CallbackInfo ci) {
        if (self().level().isClientSide)
            return;
        CreeperDataSyncMessage.syncCreeperToTrackingPlayers(self());
    }

    @ModifyExpressionValue(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/sounds/SoundEvents;CREEPER_PRIMED:Lnet/minecraft/sounds/SoundEvent;"))
    private SoundEvent insanelib$fuseSound(SoundEvent original) {
        return SoundOverrides.getFuseSound(self(), original);
    }
}
