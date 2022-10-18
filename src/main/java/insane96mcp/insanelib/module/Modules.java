package insane96mcp.insanelib.module;

import insane96mcp.insanelib.base.Module;

public class Modules {
    static Module BaseModule;

    public static void init() {
        BaseModule = Module.Builder.create("insanelib:base", "Base")
                .canBeDisabled(false)
                .build();
    }
}
