package insane96mcp.insanelib.mixin;

import insane96mcp.insanelib.module.Modules;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FlowingFluidBlock.class)
public class FlowingFluidBlockMixin {

	@Final
	@Shadow private FlowingFluid fluid;

	@Inject(at = @At("HEAD"), method = "shouldSpreadLiquid(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z", cancellable = true)
	private void reactWithNeighbors(World worldIn, BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> callback) {
		if (Modules.base.fluixMixin.customFluidMix(worldIn, pos, state)) {
			this.fizz(worldIn, pos);
			callback.setReturnValue(false);
		}
	}

	@Shadow private void fizz(IWorld worldIn, BlockPos pos) {
		worldIn.levelEvent(1501, pos, 0);
	}
}
