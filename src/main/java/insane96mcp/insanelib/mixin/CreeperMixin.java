package insane96mcp.insanelib.mixin;

import insane96mcp.insanelib.network.message.CreeperDataSyncMessage;
import net.minecraft.world.level.storage.ValueInput;
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
    private void onReadAdditionalSaveData(ValueInput input, CallbackInfo ci) {
        if (self().level().isClientSide())
            return;
        CreeperDataSyncMessage.syncCreeperToTrackingPlayers(self());
    }
}
