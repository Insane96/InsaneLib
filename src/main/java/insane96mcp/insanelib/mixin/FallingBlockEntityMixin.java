package insane96mcp.insanelib.mixin;

import insane96mcp.insanelib.event.ILEventFactory;
import net.minecraft.world.entity.item.FallingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FallingBlockEntity.class)
public class FallingBlockEntityMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/FallingBlockEntity;discard()V", ordinal = 2), method = "tick")
    private void onDiscardOnLand(CallbackInfo ci) {
        ILEventFactory.onFallingBlockLand((FallingBlockEntity) (Object) this);
    }
}
