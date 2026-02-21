package insane96mcp.insanelib.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanelib.module.base.FixesFeature;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public class PlayerMixin {
    @ModifyExpressionValue(method = "getFlyingSpeed", at = { @At(value = "CONSTANT", args = "floatValue=0.02"), @At(value = "CONSTANT", args = "floatValue=0.025999999") })
    private float insanelib$changeAirSpeed(float original) {
        return FixesFeature.getFlyingSpeed((Player) (Object) this, original);
    }

    /*@ModifyExpressionValue(method = "attack", at = @At(value = "CONSTANT", args = "floatValue=0.2", ordinal = 0))
    public float iguanatweaksreborn$noDamageWhenSpamming(float value) {
        return PlayerAttributes.noDamageWhenSpamming() ? 0f : value;
    }

    @ModifyExpressionValue(method = "attack", at = @At(value = "CONSTANT", args = "floatValue=0.8"))
    public float iguanatweaksreborn$noDamageWhenSpamming2(float value) {
        return PlayerAttributes.noDamageWhenSpamming() ? 1f : value;
    }*/

    /*@ModifyVariable(method = "causeFoodExhaustion", argsOnly = true, at = @At("HEAD"))
    private float changeExhaustionAmount(float amount) {
        return ILEventFactory.onPlayerExhaustionEvent((Player) (Object) this, amount);
    }*/
}
