package insane96mcp.insanelib.setup;

import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.module.base.items.ItemComponentsFeature;
import insane96mcp.insanelib.network.message.ServerConfigSyncMessage;
import net.minecraft.resources.ResourceLocation;
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
 * Single features of a module whose config type is another one can also be put in the SERVER config with
 * {@code @ConfigType(ModConfig.Type.SERVER)} and a SERVER ILModConfig, see Multiple configs.<br>
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
 *     <li>Only SERVER configs are synced: a client always reads its own COMMON and CLIENT configs, also when connected to a server.</li>
 * </ul>
 *
 * <p><b>Multiple configs</b> (e.g. COMMON gameplay features and CLIENT rendering features in the same modules):
 * <pre>{@code
 * public static final String COMBAT = MOD_ID + ":combat";
 *
 * // MyModModules
 * public static void init(IEventBus modEventBus) {
 *     Module.Builder.create(ResourceLocation.parse(COMBAT), "Combat", ModConfig.Type.COMMON, modEventBus).build();
 *     ...
 * }
 *
 * // Mod constructor: the modules are created once, then one ILModConfig per config type
 *     MyModModules.init(modEventBus);
 *     CONFIG = new ILModConfig(MOD_ID, ModConfig.Type.COMMON, modEventBus, MyMod.class.getClassLoader());
 *     modContainer.registerConfig(ModConfig.Type.COMMON, CONFIG.spec);
 *     CLIENT_CONFIG = new ILModConfig(MOD_ID, ModConfig.Type.CLIENT, modEventBus, MyMod.class.getClassLoader());
 *     modContainer.registerConfig(ModConfig.Type.CLIENT, CLIENT_CONFIG.spec);
 * }</pre>
 * Features use {@code @LoadFeature(module = MyMod.COMBAT)} to be in the COMMON config (the module's config type), and
 * {@code @LoadFeature(module = MyMod.COMBAT)} with {@code @ConfigType(ModConfig.Type.CLIENT)} to be in the CLIENT config.
 * Notes on multiple configs:
 * <ul>
 *     <li>A feature without {@code @ConfigType} is in the config of its module's config type (the one passed to
 *     {@code Module.Builder.create}).</li>
 *     <li>A module appears in the config of each type it has features of, with its own section and "Enable" toggle
 *     containing only the features of that type. Each toggle enables / disables only the features in its own config, e.g.
 *     the client can't disable COMMON or SERVER features. The module's description, {@code canBeDisabled} and
 *     {@code enabledByDefault} apply to all its configs.</li>
 *     <li>Each ILModConfig loads only the features of its own type, of the modules registered <b>before it's created</b>,
 *     so create the modules first. The {@code modulesInit} constructor still works: the modules created by the
 *     {@code modulesInit} of an ILModConfig are available to the ILModConfigs created after it, not to the ones created before.</li>
 *     <li>Create all the configs in the mod constructor, also the CLIENT one on dedicated servers (its features are skipped there).</li>
 *     <li>Features must always specify {@code module} if the mod has more than one. If they don't, the feature is
 *     assigned to the only module existing when it's processed, which depends on the order the modules and configs are
 *     created, and a warning is logged.</li>
 *     <li>Once all the mods have been constructed (common setup) warnings are logged for: a {@code module} not
 *     registered (e.g. a typo), unless the feature's {@code requiresMods} are missing; a feature whose config type has
 *     no ILModConfig created by the mod; a feature not loaded because its module was registered after the ILModConfig
 *     of its config type; a module without features, that doesn't appear in any config.</li>
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
    public ILModConfig(ResourceLocation moduleId, String moduleName, ModConfig.Type type,
                       IEventBus modEventBus, ClassLoader classLoader) {
        this.builder = new ModConfigSpec.Builder();
        this.spec = this.builder.configure(b -> {
            Module.Builder.create(moduleId, moduleName, type, modEventBus)
                    .canBeDisabled(false)
                    .build();
            Module.loadFeatures(type, moduleId.getNamespace(), classLoader, b);
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
            Module.loadFeatures(type, modId, classLoader, b);
            return b;
        }).getRight();
        this.registerServerConfigSync(type, modEventBus);
        trackActiveModContainer();
    }

    /**
     * Constructor for the modules already registered by the mod (created before, or by the {@code modulesInit} of
     * another ILModConfig). Loads only the features of the given config type; modules without features of that type
     * are not added to this config.
     */
    public ILModConfig(String modId, ModConfig.Type type, IEventBus modEventBus, ClassLoader classLoader) {
        if (!Module.hasModules(modId))
            InsaneLib.LOGGER.warn("ILModConfig ({}) created for mod {}, but the mod has not registered any module yet. Create the modules before the ILModConfigs that load their features", type, modId);
        this.builder = new ModConfigSpec.Builder();
        this.spec = this.builder.configure(b -> {
            Module.loadFeatures(type, modId, classLoader, b);
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
     * NeoForge syncs SERVER configs only on login, so when the server reloads the file the config is sent again to clients.
     * Item component patches are recomputed too, as programmatic providers may read the config. Done here as the event
     * is posted only on the bus of the mod owning the config, so InsaneLib can't listen to other mods' configs.
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
        server.execute(() -> {
            ServerConfigSyncMessage.syncToAll(modConfig, server);
            ItemComponentsFeature.onServerConfigReloaded(server);
        });
    }
}
