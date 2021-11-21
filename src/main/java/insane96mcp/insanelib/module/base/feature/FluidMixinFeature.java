package insane96mcp.insanelib.module.base.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.setup.Config;
import insane96mcp.insanelib.utils.IdTagMatcher;
import insane96mcp.insanelib.utils.LogHelper;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Label(name = "Fluid Mixin", description = "Set custom blocks appearing when fluids merge")
public class FluidMixinFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<List<? extends String>> customFluidMixinConfig;

	public ArrayList<FluidMix> customFluidMixin = new ArrayList<>();

	public FluidMixinFeature(Module module) {
		super(Config.builder, module);
		this.pushConfig(Config.builder);
		customFluidMixinConfig = Config.builder
				.comment("A list of fluids flowing over other fluids and the block created. It's highly recommended to use tags since fluids are split between still and flowing.\n" +
						"Format must be modid:fluid_or_tag_id,modid:fluid_or_tag_id,modid_block_output[,modid:block_below_required]\n" +
						"block_below_required is optional.")
				.defineList("Custom Fluid Mixin", Collections.emptyList(), o -> o instanceof String);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		customFluidMixin.clear();
		for (String s : this.customFluidMixinConfig.get()) {
			FluidMix f = FluidMix.parseLine(s);
			if (f != null)
				customFluidMixin.add(f);
		}
	}

	public boolean customFluidMix(World world, BlockPos pos, BlockState state) {
		for (FluidMixinFeature.FluidMix fluidMix : this.customFluidMixin) {
			if (!fluidMix.flowingFluid.matchesFluid(state.getFluidState().getType()))
				continue;

			BlockState stateBelow = world.getBlockState(pos.below());
			if (fluidMix.blockBelow != null && !stateBelow.getBlock().getRegistryName().equals(fluidMix.blockBelow))
				continue;

			for(Direction direction : Direction.values()) {
				if (direction == Direction.DOWN)
					continue;

				BlockPos blockpos = pos.relative(direction);
				if (fluidMix.touchingFluid.matchesFluid(world.getFluidState(blockpos).getType())) {
					world.setBlockAndUpdate(pos, net.minecraftforge.event.ForgeEventFactory.fireFluidPlaceBlockEvent(world, pos, pos, ForgeRegistries.BLOCKS.getValue(fluidMix.blockOutput).defaultBlockState()));
					return true;
				}
			}
		}
		return false;
	}

	public static class FluidMix {
		public IdTagMatcher flowingFluid;
		public IdTagMatcher touchingFluid;
		public ResourceLocation blockOutput;
		public ResourceLocation blockBelow;

		public FluidMix(IdTagMatcher flowingFluid, IdTagMatcher touchingFluid, ResourceLocation blockOutput, @Nullable ResourceLocation blockBelow) {
			this.flowingFluid = flowingFluid;
			this.touchingFluid = touchingFluid;
			this.blockOutput = blockOutput;
			this.blockBelow = blockBelow;
		}

		public static FluidMix parseLine(String line) {
			String[] split = line.split(",");
			if (split.length < 3 || split.length > 4) {
				LogHelper.warn("Invalid line \"%s\". Format must be modid:fluid_or_tag_id,modid:fluid_or_tag_id,modid_block_output[,modid:block_below_required]", line);
				return null;
			}
			IdTagMatcher flowingFluid = IdTagMatcher.parseLine(split[0]);
			if (flowingFluid == null) {
				LogHelper.warn("Could not parse IdTagMatcher for flowing fluid: %s", split[0]);
				return null;
			}
			IdTagMatcher touchingFluid = IdTagMatcher.parseLine(split[1]);
			if (touchingFluid == null) {
				LogHelper.warn("Could not parse IdTagMatcher for touching fluid: %s", split[1]);
				return null;
			}
			ResourceLocation blockOutput = ResourceLocation.tryParse(split[2]);
			if (blockOutput == null) {
				LogHelper.warn("Invalid Resource Location for block output: %s", split[2]);
				return null;
			}
			ResourceLocation blockBelow = null;
			if (split.length == 4) {
				blockBelow = ResourceLocation.tryParse(split[3]);
				if (blockBelow == null) {
					LogHelper.warn("Invalid Resource Location for block below: %s", split[3]);
					return null;
				}
			}
			FluidMix fluidMix = new FluidMix(flowingFluid, touchingFluid, blockOutput, blockBelow);
			return fluidMix;
		}
	}
}
