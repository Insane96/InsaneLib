package insane96mcp.insanelib.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.insanelib.event.ILEventFactory;
import insane96mcp.insanelib.module.base.Fixes;
import insane96mcp.insanelib.util.CurrentAttacker;
import net.minecraft.world.entity.LivingEntity;
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

    /**
     * Tracks {@code this} as the in-progress attacker for the duration of each {@code LivingEntity#knockback}
     * call triggered by {@code attack(Entity)}, covering both the direct hit and every hit in the sweep attack
     * loop. This lets listeners of {@code LivingKnockBackEvent} resolve the real attacker via
     * {@link CurrentAttacker#resolve(LivingEntity)} even for sweep hits, where vanilla calls knockback before
     * hurt (so getLastHurtByMob() isn't updated yet when the event fires).
     */
    @WrapOperation(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"))
    private void insanelib$trackAttackerForKnockback(LivingEntity target, double strength, double x, double z, Operation<Void> original) {
        LivingEntity previous = CurrentAttacker.get();
        CurrentAttacker.set((Player) (Object) this);
        try {
            original.call(target, strength, x, z);
        } finally {
            CurrentAttacker.set(previous);
        }
    }
}
