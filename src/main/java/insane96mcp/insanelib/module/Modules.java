package insane96mcp.insanelib.module;

import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.core.feature.Module;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public class Modules {
    static Module BaseModule;

    public static void init(IEventBus modEventBus, ModConfigSpec.Builder configBuilder) {
        BaseModule = Module.Builder.create(InsaneLib.location("base"), "Base", ModConfig.Type.COMMON, configBuilder, modEventBus)
                .canBeDisabled(false)
                .build();
    }
}
