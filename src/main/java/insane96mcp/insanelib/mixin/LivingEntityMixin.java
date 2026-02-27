package insane96mcp.insanelib.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.module.base.FixesFeature;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyExpressionValue(method = "jumpFromGround", at = @At(value = "CONSTANT", args = "doubleValue=0.2"))
    private double insanelib$changeForwardJumpStrengthWhenSprinting(double original) {
        if (!((LivingEntity) (Object) this instanceof Player player) || !Feature.isEnabled(FixesFeature.class) || !FixesFeature.fixAirSpeed$sprintingJumpSlowdown)
            return original;
        return FixesFeature.getFlyingSpeed(player, (float) original);
    }

    @ModifyExpressionValue(method = "hurt", at = @At(value = "CONSTANT", args = "intValue=10"))
    public int insanelib$reflectInvulnerabilityFrames(int original) {
        return this.invulnerableTime - 10;
    }

    @ModifyExpressionValue(method = "handleDamageEvent", at = @At(value = "CONSTANT", args = "intValue=20"))
    public int insanelib$reflectInvulnerabilityFramesInEvent(int original) {
        return this.invulnerableTime > 10 ? this.invulnerableTime : original;
    }

    @ModifyExpressionValue(method = "handleDamageEvent", at = @At(value = "CONSTANT", args = "intValue=10"))
    public int insanelib$reflectInvulnerabilityFramesInEvent2(int original) {
        return this.invulnerableTime > 10 ? this.invulnerableTime - 10 : original;
    }
}
