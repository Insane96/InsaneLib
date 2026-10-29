package insane96mcp.insanelib.module.base.items;

import com.google.gson.JsonObject;
import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.mixin.accessor.ItemAccessor;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class ItemComponentsReloadListener extends SimplePreparableReloadListener<List<ItemComponent>> {
    public static final ItemComponentsReloadListener INSTANCE = new ItemComponentsReloadListener();

    public static List<ItemComponent> DEFINITIONS = new ArrayList<>();
    /**
     * Never modified, but replaced as a whole: in singleplayer it's shared by the client and the integrated server,
     * which read it from different threads.
     */
    public static volatile Map<Item, DataComponentMap> PATCHED_COMPONENTS = Map.of();
    /**
     * The final per-item patches computed alongside {@link #PATCHED_COMPONENTS}, keyed by item registry id.
     * Kept separate because {@link DataComponentMap} isn't itself network-serializable, while {@link DataComponentPatch}
     * is; this is what gets sent to clients so their {@link #PATCHED_COMPONENTS} matches the server's.
     * Never modified, but replaced as a whole.
     */
    public static volatile Map<ResourceLocation, DataComponentPatch> SYNCED_PATCHES = Map.of();

    /**
     * Register a provider to supply programmatic component patches.
     * Providers are called before data pack definitions are applied,
     * so data pack entries always take priority over programmatic ones.
     * The consumer receives the current {@link RegistryAccess} and a mutable map
     * to populate with {@link DataComponentPatch} entries per item.<br>
     * Providers are called multiple times (on world load, /reload and when a SERVER config made with {@code ILModConfig}
     * is reloaded), always with a new empty map: they must be idempotent and not accumulate state between calls.<br>
     * On world load tags are updated before SERVER configs are loaded, so providers reading SERVER config values first
     * see the default values (or the previous world's ones). The final patches are the ones computed on
     * {@link net.neoforged.neoforge.event.server.ServerAboutToStartEvent}, once SERVER configs are loaded.
     */
    public static final List<BiConsumer<RegistryAccess, Map<Item, DataComponentPatch>>> PROGRAMMATIC_PROVIDERS = new ArrayList<>();

    @Override
    protected @NotNull List<ItemComponent> prepare(@NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        List<ItemComponent> definitions = new ArrayList<>();
        resourceManager.listResources("item_components", path -> path.getPath().endsWith(".json"))
                .forEach((location, resource) -> {
                    try (InputStreamReader reader = new InputStreamReader(resource.open())) {
                        JsonObject json = GsonHelper.parse(reader);
                        definitions.add(ItemComponent.fromJson(json));
                    } catch (Exception e) {
                        InsaneLib.LOGGER.error("Failed to load item component definition {}: {}", location, e.getMessage());
                    }
                });
        return definitions;
    }

    /**
     * Returns the item's own components, ignoring {@link #PATCHED_COMPONENTS}
     */
    public static DataComponentMap getUnpatchedComponents(Item item) {
        return ((ItemAccessor) item).insanelib$getUnpatchedComponents();
    }

    /**
     * The patches are replaced in {@link ItemComponentsFeature#onTagsUpdated}, once tags are bound, and again in
     * {@link ItemComponentsFeature#onServerAboutToStart}
     */
    @Override
    protected void apply(@NotNull List<ItemComponent> definitions, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        DEFINITIONS = definitions;
        InsaneLib.LOGGER.info("Loaded {} item component definitions", DEFINITIONS.size());
    }
}
