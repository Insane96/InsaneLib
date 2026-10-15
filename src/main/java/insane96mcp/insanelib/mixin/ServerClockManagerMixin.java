package insane96mcp.insanelib.mixin;

import insane96mcp.insanelib.module.base.TimeStopNoPlayerOnline;
import net.minecraft.world.clock.ServerClockManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerClockManager.class)
public class ServerClockManagerMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void insanelib$stopClocksIfNoPlayerOnline(CallbackInfo ci) {
        if (TimeStopNoPlayerOnline.clocksStopped)
            ci.cancel();
    }
}
