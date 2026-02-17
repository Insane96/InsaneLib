package insane96mcp.insanelib;

import com.mojang.logging.LogUtils;
import insane96mcp.insanelib.setup.ILConfig;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(InsaneLib.MOD_ID)
public class InsaneLib {
    public static final String MOD_ID = "insanelib";
    public static final Logger LOGGER = LogUtils.getLogger();

    public InsaneLib(IEventBus modEventBus, ModContainer modContainer) {
        ILConfig.init(modEventBus);
        modContainer.registerConfig(ModConfig.Type.COMMON, ILConfig.COMMON_SPEC, MOD_ID + ".toml");
    }

    /**
     * Creates a {@link ResourceLocation} using the provided namespace and path.
     *
     * @param path The specific path for the resource within the mod's namespace.
     * @return A {@link ResourceLocation} with the mod's namespace and the given path.
     */
    public static ResourceLocation location(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    /**
     * Constructs a namespaced language key using the mod's ID and the provided path.
     *
     * @param path The specific path for the language key within the mod's namespace.
     * @return A fully-qualified language key in the format "MOD_ID.path".
     */
    public static String lang(String path) {
        return MOD_ID + "." + path;
    }
}
