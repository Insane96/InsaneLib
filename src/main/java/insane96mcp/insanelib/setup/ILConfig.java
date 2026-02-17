package insane96mcp.insanelib.setup;

import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.module.Modules;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ILConfig {
    public static ModConfigSpec COMMON_SPEC;
    public static CommonConfig COMMON;

    public static ModConfigSpec.Builder builder;

    public static void init(IEventBus eventBus) {
        builder = new ModConfigSpec.Builder();
        final Pair<CommonConfig, ModConfigSpec> specPair = builder.configure(b -> new CommonConfig(b, eventBus));
        COMMON = specPair.getLeft();
        COMMON_SPEC = specPair.getRight();
    }

    public static class CommonConfig {
        public CommonConfig(final ModConfigSpec.Builder builder, IEventBus eventBus) {
            Modules.init(eventBus);
            Module.loadFeatures(ModConfig.Type.COMMON, InsaneLib.MOD_ID, this.getClass().getClassLoader());
        }
    }
}
