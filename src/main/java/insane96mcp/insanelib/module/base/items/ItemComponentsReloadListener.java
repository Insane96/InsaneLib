package insane96mcp.insanelib.module.base.items;

import com.google.gson.JsonObject;
import insane96mcp.insanelib.InsaneLib;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.resources.Identifier;
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
    public static volatile Map<Identifier, DataComponentPatch> SYNCED_PATCHES = Map.of();

    /**
     * Register a provider to supply programmatic component patches.
     * Providers are called on each reload before data pack definitions are applied,
     * so data pack entries always take priority over programmatic ones.
     * The consumer receives the current registries ({@link HolderLookup.Provider}) and a mutable map
     * to populate with {@link DataComponentPatch} entries per item.
     */
    public static final List<BiConsumer<HolderLookup.Provider, Map<Item, DataComponentPatch>>> PROGRAMMATIC_PROVIDERS = new ArrayList<>();

    /**
     * The registries of the last data reload, used to decode the components once they're bound to the items.
     */
    public static HolderLookup.Provider registries;

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
     * Returns the item's own components (the ones bound to its holder), ignoring {@link #PATCHED_COMPONENTS}
     */
    public static DataComponentMap getUnpatchedComponents(Item item) {
        return item.builtInRegistryHolder().components();
    }

    /**
     * The patches are replaced in {@link ItemComponentsFeature#onDefaultComponentsBound}, once default components are bound
     */
    @Override
    protected void apply(@NotNull List<ItemComponent> definitions, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        DEFINITIONS = definitions;
        registries = this.getRegistryLookup();
        InsaneLib.LOGGER.info("Loaded {} item component definitions", DEFINITIONS.size());
    }
}
