package insane96mcp.insanelib.setup;

import insane96mcp.insanelib.core.feature.Module;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.function.BiConsumer;

/**
 * Utility class that handles the boilerplate of creating a NeoForge mod config
 * alongside InsaneLib's module/feature system.
 *
 * <p><b>Single-module usage</b> (no Modules class needed):
 * <pre>{@code
 * public static ILModConfig CONFIG;
 *
 * public MyMod(IEventBus modEventBus, ModContainer modContainer) {
 *     CONFIG = new ILModConfig(location("main"), "Single Module", ModConfig.Type.COMMON,
 *             modEventBus, MyMod.class.getClassLoader());
 *     modContainer.registerConfig(ModConfig.Type.COMMON, CONFIG.spec);
 * }
 * }</pre>
 * Features use {@code @LoadFeature(module = "mymod:main", ...)}.
 *
 * <p><b>Multi-module usage</b>:
 * <pre>{@code
 *     CONFIG = new ILModConfig(MOD_ID, ModConfig.Type.COMMON, modEventBus,
 *             MyModModules::init, MyMod.class.getClassLoader());
 * }</pre>
 * {@code MyModModules.init} must accept {@code (IEventBus, ModConfigSpec.Builder)}.
 */
public class ILModConfig {
    public final ModConfigSpec.Builder builder;
    public final ModConfigSpec spec;

    /**
     * Single-module constructor. Automatically creates a module with the given id and name —
     * no separate Modules class needed.
     */
    public ILModConfig(Identifier moduleId, String moduleName, ModConfig.Type type,
                       IEventBus modEventBus, ClassLoader classLoader) {
        this.builder = new ModConfigSpec.Builder();
        this.spec = this.builder.configure(b -> {
            Module.Builder.create(moduleId, moduleName, type, b, modEventBus)
                    .canBeDisabled(false)
                    .build();
            Module.loadFeatures(type, moduleId.getNamespace(), classLoader);
            return b;
        }).getRight();
    }

    /**
     * Multi-module constructor. The {@code modulesInit} callback receives the event bus and
     * config builder, and is responsible for creating all modules.
     */
    public ILModConfig(String modId, ModConfig.Type type, IEventBus modEventBus,
                       BiConsumer<IEventBus, ModConfigSpec.Builder> modulesInit, ClassLoader classLoader) {
        this.builder = new ModConfigSpec.Builder();
        this.spec = this.builder.configure(b -> {
            modulesInit.accept(modEventBus, b);
            Module.loadFeatures(type, modId, classLoader);
            return b;
        }).getRight();
    }
}
