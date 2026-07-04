package insane96mcp.insanelib.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.insanelib.module.base.Fixes;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CrossbowItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CrossbowItem.class)
public abstract class CrossbowItemMixin {
	@ModifyExpressionValue(method = "shootProjectile", at = @At(value = "CONSTANT", args = "doubleValue=0.20000000298023224"))
	private double enhancedai$noGravityDropCompensationForFireworks(double original, @Local(argsOnly = true) Projectile projectile) {
		if (!Fixes.shouldFixMobCrossbowFireworkShooting()
				|| !(projectile instanceof FireworkRocketEntity))
			return original;
		return 0d;
	}
}
