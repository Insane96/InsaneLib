package insane96mcp.insanelib.setup;

import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.network.message.ServerConfigSyncMessage;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
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
 *
 * <p><b>SERVER config usage</b> (values decided by the server and synced to clients):
 * <pre>{@code
 *     CONFIG = new ILModConfig(location("main"), "Single Module", ModConfig.Type.SERVER,
 *             modEventBus, MyMod.class.getClassLoader());
 *     modContainer.registerConfig(ModConfig.Type.SERVER, CONFIG.spec);
 * }</pre>
 * Notes on SERVER configs:
 * <ul>
 *     <li>The file is {@code config/<modid>-server.toml}, loaded when the server starts. A world can override it with
 *     {@code saves/<world>/serverconfig/<modid>-server.toml}, used only if present. If the file in {@code config/}
 *     doesn't exist it's created by copying {@code defaultconfigs/<modid>-server.toml}, if present.</li>
 *     <li>NeoForge sends it to clients on login (configuration phase) and clients receive it with
 *     {@link net.neoforged.fml.event.config.ModConfigEvent.Reloading}. In singleplayer / LAN host no sync is needed as
 *     client and server share the same JVM. Clients connecting to a vanilla server load the default values.</li>
 *     <li>Changes to the file while the server is running are reloaded on the server and InsaneLib syncs them again
 *     to connected clients (NeoForge syncs only on login). Operators get a chat message when this happens.</li>
 *     <li>Before the config is loaded for the first time (e.g. in the main menu) features are disabled ({@code Feature#isEnabled()} returns
 *     false) and {@code @Config} fields hold the values they are initialized with.</li>
 *     <li>When the server stops the config is unloaded but {@code @Config} fields keep the last values read.
 *     {@code ConfigOption#set} and {@code Feature#setEnabledConfig} do nothing (and log a warning) while the config is not loaded.</li>
 * </ul>
 */
public class ILModConfig {
    private static final Set<ModContainer> MOD_CONTAINERS = Collections.synchronizedSet(new LinkedHashSet<>());

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
        this.registerServerConfigSync(type, modEventBus);
        trackActiveModContainer();
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
        this.registerServerConfigSync(type, modEventBus);
        trackActiveModContainer();
    }

    /**
     * Mods that created an ILModConfig, used to automatically register the config screen
     */
    public static Set<ModContainer> getModContainers() {
        synchronized (MOD_CONTAINERS) {
            return Set.copyOf(MOD_CONTAINERS);
        }
    }

    private static void trackActiveModContainer() {
        // Falls back to the minecraft container when not called while constructing a mod
        ModContainer container = ModLoadingContext.get().getActiveContainer();
        if (!container.getModId().equals("minecraft"))
            MOD_CONTAINERS.add(container);
    }

    private void registerServerConfigSync(ModConfig.Type type, IEventBus modEventBus) {
        if (type == ModConfig.Type.SERVER)
            modEventBus.addListener(this::onServerConfigReloading);
    }

    /**
     * NeoForge syncs SERVER configs only on login, so when the server reloads the file the config is sent again to clients
     */
    private void onServerConfigReloading(final ModConfigEvent.Reloading event) {
        ModConfig modConfig = event.getConfig();
        if (modConfig.getSpec() != this.spec)
            return;
        // Null on clients, that receive the synced config with a Reloading event too
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null)
            return;
        // Reloading is fired from the config file watcher thread
        server.execute(() -> ServerConfigSyncMessage.syncToAll(modConfig, server));
    }
}
