package insane96mcp.insanelib.module;

import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.setup.Config;

public class Modules {
    static Module BaseModule;

    public static void init() {
        BaseModule = Module.Builder.create(Config.builder, "insanelib:base", "Base")
                .canBeDisabled(false)
                .build();
    }
}
