package insane96mcp.insanelib.mixin;

import insane96mcp.insanelib.module.Modules;
import insane96mcp.insanelib.module.base.feature.FluidMixinFeature;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
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
		for (FluidMixinFeature.FluidMix fluidMix : Modules.base.fluixMixin.customFluidMixin) {
			if (!fluidMix.flowingFluid.matchesFluid(this.fluid))
				continue;

			BlockState stateBelow = worldIn.getBlockState(pos.below());
			if (fluidMix.blockBelow != null && !stateBelow.getBlock().getRegistryName().equals(fluidMix.blockBelow))
				continue;

			for(Direction direction : Direction.values()) {
				if (direction == Direction.DOWN)
					continue;

				BlockPos blockpos = pos.relative(direction);
				if (worldIn.getFluidState(blockpos).is(FluidTags.WATER)) {
					worldIn.setBlockAndUpdate(pos, net.minecraftforge.event.ForgeEventFactory.fireFluidPlaceBlockEvent(worldIn, pos, pos, ForgeRegistries.BLOCKS.getValue(fluidMix.blockOutput).defaultBlockState()));
					this.fizz(worldIn, pos);
					callback.setReturnValue(false);
					return;
				}
			}
		}
	}

	@Shadow private void fizz(IWorld worldIn, BlockPos pos) {
		worldIn.levelEvent(1501, pos, 0);
	}
}
