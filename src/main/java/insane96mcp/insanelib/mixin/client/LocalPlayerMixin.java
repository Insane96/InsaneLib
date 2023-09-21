package insane96mcp.insanelib.mixin.client;

import insane96mcp.insanelib.event.ILEventFactory;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    @Inject(at = @At("RETURN"), method = "hasEnoughFoodToStartSprinting", cancellable = true)
    private void canSprintEvent(CallbackInfoReturnable<Boolean> callback) {
        if (!ILEventFactory.doPlayerSprintCheck((LocalPlayer) (Object) this))
            callback.setReturnValue(false);
    }
}
