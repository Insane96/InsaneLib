package insane96mcp.insanelib.mixin;

import insane96mcp.insanelib.event.ILEventFactory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "addEatEffect", at = @At("HEAD"), cancellable = true)
    private void onAddEatEffect(ItemStack pFood, Level pLevel, LivingEntity pLivingEntity, CallbackInfo ci) {
        if (ILEventFactory.onAddEatEffect(pFood, pLevel, pLivingEntity))
            ci.cancel();
    }
}
