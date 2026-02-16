package insane96mcp.insanelib;

import insane96mcp.insanelib.core.Module;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ILConfig {
    public static final ModConfigSpec COMMON_SPEC;
    public static final CommonConfig COMMON;

    public static final ModConfigSpec.Builder builder;

    static {
        builder = new ModConfigSpec.Builder();
        final Pair<CommonConfig, ModConfigSpec> specPair = builder.configure(CommonConfig::new);
        COMMON = specPair.getLeft();
        COMMON_SPEC = specPair.getRight();
    }

    public static class CommonConfig {
        public CommonConfig(final ModConfigSpec.Builder builder) {
            //Modules.init();
            Module.loadFeatures(ModConfig.Type.COMMON, InsaneLib.MOD_ID, this.getClass().getClassLoader());
        }
    }
}
