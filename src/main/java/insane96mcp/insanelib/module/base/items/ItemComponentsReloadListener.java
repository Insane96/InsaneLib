package insane96mcp.insanelib.module.base.items;

import com.google.gson.JsonObject;
import insane96mcp.insanelib.InsaneLib;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemComponentsReloadListener extends SimplePreparableReloadListener<List<ItemComponent>> {
    public static final ItemComponentsReloadListener INSTANCE = new ItemComponentsReloadListener();

    public static List<ItemComponent> DEFINITIONS = new ArrayList<>();
    public static final Map<Item, DataComponentMap> PATCHED_COMPONENTS = new HashMap<>();

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

    @Override
    protected void apply(@NotNull List<ItemComponent> definitions, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        PATCHED_COMPONENTS.clear();
        DEFINITIONS = definitions;
        InsaneLib.LOGGER.info("Loaded {} item component definitions", DEFINITIONS.size());
    }
}
