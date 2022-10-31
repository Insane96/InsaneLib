package insane96mcp.insanelib.module;

import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.setup.Config;
import net.minecraftforge.fml.config.ModConfig;

public class Modules {
    static Module BaseModule;

    public static void init() {
        BaseModule = Module.Builder.create(InsaneLib.MOD_ID, "base", "Base", ModConfig.Type.COMMON, Config.builder)
                .canBeDisabled(false)
                .build();
    }
}
