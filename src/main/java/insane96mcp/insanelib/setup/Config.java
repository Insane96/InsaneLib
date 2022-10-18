package insane96mcp.insanelib.setup;

import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.base.Module;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;

@Mod.EventBusSubscriber(modid = InsaneLib.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
	public static final ForgeConfigSpec COMMON_SPEC;
	public static final CommonConfig COMMON;

	public static final ForgeConfigSpec.Builder builder;

	static {
		builder = new ForgeConfigSpec.Builder();
		final Pair<CommonConfig, ForgeConfigSpec> specPair = builder.configure(CommonConfig::new);
		COMMON = specPair.getLeft();
		COMMON_SPEC = specPair.getRight();
	}

	public static class CommonConfig {
		public CommonConfig(final ForgeConfigSpec.Builder builder) {
			Module.loadFeatures(InsaneLib.MOD_ID, this.getClass().getClassLoader(), builder);
		}
	}
}