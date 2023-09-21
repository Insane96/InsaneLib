package insane96mcp.insanelib.mixin;

import insane96mcp.insanelib.event.ILEventFactory;
import insane96mcp.insanelib.module.base.feature.FixFeature;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Player.class)
public class PlayerMixin {
    @Shadow @Final private Abilities abilities;

    @Inject(at = @At("RETURN"), method = "getFlyingSpeed", cancellable = true)
    private void changeFlyingSpeed(CallbackInfoReturnable<Float> cir) {
        if (!this.abilities.flying || ((Player) (Object) this).isPassenger()) {
            Optional<Float> flyingSpeed = FixFeature.getFlyingSpeed((Player) (Object) this);
            flyingSpeed.ifPresent(cir::setReturnValue);
        }
    }

    @ModifyVariable(method = "causeFoodExhaustion", argsOnly = true, at = @At("HEAD"))
    private float changeExhaustionAmount(float amount) {
        return ILEventFactory.onPlayerExhaustionEvent((Player) (Object) this, amount);
    }
}
