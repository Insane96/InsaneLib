package insane96mcp.insanelib.base;

import net.minecraftforge.common.ForgeConfigSpec;

public interface IConfigurableObject {
    ForgeConfigSpec.ConfigValue<?> get();
}
