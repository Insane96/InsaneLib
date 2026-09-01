package insane96mcp.insanelib.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CommonHooks.class)
public abstract class CommonHooksMixin {
	@WrapOperation(method = "lambda$onGrindstoneTake$2", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;atCenterOf(Lnet/minecraft/core/Vec3i;)Lnet/minecraft/world/phys/Vec3;"))
	private static Vec3 insanelib$spawnGrindstoneXpAtCorrectFace(Vec3i pos, Operation<Vec3> original, @Local(argsOnly = true) Level level, @Local(argsOnly = true) BlockPos blockPos) {
		return MCUtils.getGrindstoneOutputPos(level, blockPos);
	}
}
