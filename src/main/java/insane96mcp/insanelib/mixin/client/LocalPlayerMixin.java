package insane96mcp.insanelib.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfile;
import insane96mcp.insanelib.event.ILEventFactory;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends Player {
    public LocalPlayerMixin(Level p_250508_, BlockPos p_250289_, float p_251702_, GameProfile p_252153_) {
        super(p_250508_, p_250289_, p_251702_, p_252153_);
    }

    @Inject(at = @At("RETURN"), method = "hasEnoughFoodToStartSprinting", cancellable = true)
    private void canSprintEvent(CallbackInfoReturnable<Boolean> callback) {
        if (!ILEventFactory.doPlayerSprintCheck((LocalPlayer) (Object) this))
            callback.setReturnValue(false);
    }

    @ModifyExpressionValue(method = "aiStep", at = @At(value = "CONSTANT", args = "floatValue=0.2"))
    public float insanelib$speedReductionWhenUsingItem(float original) {
        return ILEventFactory.onUseItemModifier((LocalPlayer) (Object) this, this.useItem);
    }
}
