package insane96mcp.insanelib.mixin;

import insane96mcp.insanelib.module.Modules;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LiquidBlock.class)
public abstract class FlowingFluidBlockMixin {

	@Inject(at = @At("HEAD"), method = "shouldSpreadLiquid(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z", cancellable = true)
	private void reactWithNeighbors(Level worldIn, BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> callback) {
		if (Modules.base.fluixMixin.customFluidMix(worldIn, pos, state)) {
			this.fizz(worldIn, pos);
			callback.setReturnValue(false);
		}
	}

	@Shadow
	protected abstract void fizz(LevelAccessor worldIn, BlockPos pos);
}
