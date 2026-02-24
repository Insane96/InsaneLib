package insane96mcp.insanelib.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.insanelib.module.base.PushResistance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "push(DDD)V", at = @At("HEAD"), cancellable = true)
    public void enhancedai$cancelPush(double pX, double pY, double pZ, CallbackInfo ci) {
        double resistance = PushResistance.getPushResistance((Entity) (Object) this);
        if (resistance <= 0d)
            ci.cancel();
    }

    @WrapOperation(method = "push(DDD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;"))
    public Vec3 enhancedai$pushResistance(Vec3 instance, double x, double y, double z, Operation<Vec3> original) {
        double resistance = PushResistance.getPushResistance((Entity) (Object) this);
        return original.call(instance, x * resistance, y * resistance, z * resistance);
    }
}
