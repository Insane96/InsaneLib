package insane96mcp.insanelib.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;
import insane96mcp.insanelib.event.ILEventFactory;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends Player {
    public LocalPlayerMixin(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    // Replaces the 1.21.1 injection into hasEnoughFoodToStartSprinting, which was moved to Player
    // (hasEnoughFoodToDoExhaustiveManoeuvres) and is now only reachable through this method
    @Inject(at = @At("RETURN"), method = "isSprintingPossible", cancellable = true)
    private void canSprintEvent(boolean allowedInShallowWater, CallbackInfoReturnable<Boolean> callback) {
        if (!ILEventFactory.doPlayerSprintCheck((LocalPlayer) (Object) this))
            callback.setReturnValue(false);
    }

    // Replaces the 1.21.1 constant modification in aiStep: the 0.2 slowdown now comes from the
    // USE_EFFECTS data component, read by this method
    @ModifyReturnValue(method = "itemUseSpeedMultiplier", at = @At("RETURN"))
    public float insanelib$speedReductionWhenUsingItem(float original) {
        return ILEventFactory.onUseItemMovementSpeedModifier((LocalPlayer) (Object) this, this.useItem);
    }
}
