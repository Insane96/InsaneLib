package insane96mcp.insanelib.core.feature.config;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.toml.TomlParser;
import insane96mcp.insanelib.InsaneLib;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLConfig;
import net.neoforged.fml.loading.FMLPaths;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reads SERVER config files before NeoForge loads them. Data packs are loaded before the {@code MinecraftServer} exists,
 * while SERVER configs are loaded when it's about to start, so during the first data pack load the values can only be
 * read from the file. The file is chosen as NeoForge will do when loading the config.
 */
public final class ServerConfigFileReader {
    // Same folder as ServerLifecycleHooks#SERVERCONFIG
    private static final String WORLD_OVERRIDE_FOLDER = "serverconfig";

    // Conditions are tested once per recipe, so the file is parsed again only if changed
    private static final Map<Path, CachedFile> CACHE = new ConcurrentHashMap<>();

    @Nullable
    private static volatile Path openLevelDir;

    private record CachedFile(FileTime lastModified, @Nullable Config config) {}

    private ServerConfigFileReader() {}

    public static void onLevelOpened(Path levelDir) {
        openLevelDir = levelDir;
    }

    public static void onLevelClosed(Path levelDir) {
        if (levelDir.equals(openLevelDir))
            openLevelDir = null;
    }

    /**
     * @return the contents of the file of the config. Empty if the file doesn't exist yet or can't be read, as NeoForge
     * will create it with the default values
     */
    public static Config read(ModConfig modConfig) {
        String fileName = modConfig.getFileName();
        Config config = parse(resolveFile(fileName));
        if (config == null)
            config = parse(FMLPaths.GAMEDIR.get().resolve(FMLConfig.defaultConfigPath()).resolve(fileName));
        return config != null ? config : Config.inMemory();
    }

    private static Path resolveFile(String fileName) {
        Path levelDir = openLevelDir;
        if (levelDir != null) {
            Path override = levelDir.resolve(WORLD_OVERRIDE_FOLDER).resolve(fileName);
            if (Files.exists(override))
                return override;
        }
        return FMLPaths.CONFIGDIR.get().resolve(fileName);
    }

    @Nullable
    private static Config parse(Path file) {
        FileTime lastModified;
        try {
            lastModified = Files.getLastModifiedTime(file);
        }
        catch (IOException e) {
            return null;
        }
        CachedFile cached = CACHE.get(file);
        if (cached == null || !cached.lastModified().equals(lastModified)) {
            cached = new CachedFile(lastModified, parseToml(file));
            CACHE.put(file, cached);
        }
        return cached.config();
    }

    @Nullable
    private static Config parseToml(Path file) {
        try (var reader = Files.newBufferedReader(file)) {
            return new TomlParser().parse(reader);
        }
        catch (IOException | RuntimeException e) {
            InsaneLib.LOGGER.warn("Failed to read the config file {}: {}", file, e.toString());
            return null;
        }
    }

    /**
     * Reads a boolean accepting the same values as a boolean {@code ModConfigSpec} option
     * @return the value in the config, or the default one if it's missing or not valid
     */
    public static boolean getBoolean(Config config, List<String> path, boolean defaultValue) {
        Object value = config.get(path);
        if (value instanceof Boolean bool)
            return bool;
        if (value instanceof String string && (string.equalsIgnoreCase("true") || string.equalsIgnoreCase("false")))
            return Boolean.parseBoolean(string);
        return defaultValue;
    }
}
