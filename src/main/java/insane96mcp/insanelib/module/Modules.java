package insane96mcp.insanelib.module;

import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.setup.ILConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.config.ModConfig;

public class Modules {
    static Module BaseModule;

    public static void init(IEventBus modEventBus) {
        BaseModule = Module.Builder.create(InsaneLib.location("base"), "Base", ModConfig.Type.COMMON, ILConfig.builder, modEventBus)
                .canBeDisabled(false)
                .build();
    }
}
