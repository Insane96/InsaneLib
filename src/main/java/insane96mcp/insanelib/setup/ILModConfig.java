package insane96mcp.insanelib.setup;

import insane96mcp.insanelib.core.feature.Module;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.function.BiConsumer;

/**
 * Utility class that handles the boilerplate of creating a NeoForge mod config
 * alongside InsaneLib's module/feature system.
 *
 * <p>Usage in your main mod class:
 * <pre>{@code
 * public static ILModConfig CONFIG;
 *
 * public MyMod(IEventBus modEventBus, ModContainer modContainer) {
 *     CONFIG = new ILModConfig(MOD_ID, ModConfig.Type.COMMON, modEventBus,
 *             MyModModules::init, MyMod.class.getClassLoader());
 *     modContainer.registerConfig(ModConfig.Type.COMMON, CONFIG.spec);
 * }
 * }</pre>
 *
 * <p>{@code MyModModules.init} must accept {@code (IEventBus, ModConfigSpec.Builder)}.
 */
public class ILModConfig {
    public final ModConfigSpec.Builder builder;
    public final ModConfigSpec spec;

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
