package insane96mcp.insanelib.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.event.ILEventFactory;
import insane96mcp.insanelib.module.base.FixesFeature;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyExpressionValue(method = "jumpFromGround", at = @At(value = "CONSTANT", args = "doubleValue=0.2"))
    private double insanelib$changeForwardJumpStrengthWhenSprinting(double original) {
        if (!(self() instanceof Player player) || !Feature.isEnabled(FixesFeature.class) || !FixesFeature.fixAirSpeed$sprintingJumpSlowdown)
            return original;
        return FixesFeature.getFlyingSpeed(player, (float) original);
    }

    @Inject(method = "addEatEffect", at = @At("HEAD"), cancellable = true)
    private void onAddEatEffect(FoodProperties foodProperties, CallbackInfo ci) {
        if (ILEventFactory.onAddEatEffect(self(), foodProperties))
            ci.cancel();
    }

    public LivingEntity self() {
        return (LivingEntity) (Object) this;
    }
}
