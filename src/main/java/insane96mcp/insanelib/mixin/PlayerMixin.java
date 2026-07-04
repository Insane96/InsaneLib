package insane96mcp.insanelib.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanelib.event.ILEventFactory;
import insane96mcp.insanelib.module.base.Fixes;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Player.class)
public class PlayerMixin {
    @ModifyExpressionValue(method = "getFlyingSpeed", at = { @At(value = "CONSTANT", args = "floatValue=0.02"), @At(value = "CONSTANT", args = "floatValue=0.025999999") })
    private float insanelib$changeAirSpeed(float original) {
        return Fixes.getFlyingSpeed((Player) (Object) this, original);
    }

    @ModifyVariable(method = "causeFoodExhaustion", argsOnly = true, at = @At("HEAD"))
    private float changeExhaustionAmount(float amount) {
        return ILEventFactory.onPlayerExhaustionEvent((Player) (Object) this, amount);
    }
}
