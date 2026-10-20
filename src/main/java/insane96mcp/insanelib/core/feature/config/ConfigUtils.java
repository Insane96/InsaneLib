package insane96mcp.insanelib.core.feature.config;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import insane96mcp.insanelib.InsaneLib;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class ConfigUtils {
    private static final Splitter DOT_SPLITTER = Splitter.on(".");
    public static List<String> split(String path)
    {
        return Lists.newArrayList(DOT_SPLITTER.split(path));
    }

    /**
     * Returns true if the config holding the value is loaded, so the value can be read and set.
     * SERVER configs are not loaded before joining a world and get unloaded when the server stops.
     */
    public static boolean isLoaded(ModConfigSpec.ConfigValue<?> configValue) {
        try {
            configValue.getRaw();
            return true;
        }
        catch (IllegalStateException | NullPointerException e) {
            return false;
        }
    }

    /**
     * Same as {@link #isLoaded(ModConfigSpec.ConfigValue)} but logs a warning if the config is not loaded
     */
    public static boolean canSet(ModConfigSpec.ConfigValue<?> configValue, String name) {
        if (isLoaded(configValue))
            return true;
        InsaneLib.LOGGER.warn("Could not set config option {}. The config is not loaded", name);
        return false;
    }
}
