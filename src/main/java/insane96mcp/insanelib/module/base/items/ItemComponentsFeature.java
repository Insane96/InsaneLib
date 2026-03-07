package insane96mcp.insanelib.module.base.items;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

import java.util.*;

@LoadFeature(description = "Allows modifying item data components via data packs", canBeDisabled = false)
public class ItemComponentsFeature extends Feature {

    @SubscribeEvent
    public void onTagsUpdated(TagsUpdatedEvent event) {
        if (!event.shouldUpdateStaticData())
            return;

        ItemComponentsReloadListener.PATCHED_COMPONENTS.clear();

        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, event.getRegistryAccess());

        // Collect merged components per item (higher priority wins per component type)
        Map<Item, Map<DataComponentType<?>, Object>> allComponents = new HashMap<>();
        Map<Item, Set<DataComponentType<?>>> allRemovals = new HashMap<>();

        // Seed with programmatic patches first (lowest priority — data pack definitions overwrite these)
        if (!ItemComponentsReloadListener.PROGRAMMATIC_PROVIDERS.isEmpty()) {
            Map<Item, DataComponentPatch> programmatic = new HashMap<>();
            for (var provider : ItemComponentsReloadListener.PROGRAMMATIC_PROVIDERS)
                provider.accept(event.getRegistryAccess(), programmatic);

            for (Map.Entry<Item, DataComponentPatch> entry : programmatic.entrySet()) {
                Item item = entry.getKey();
                Map<DataComponentType<?>, Object> setMap = allComponents.computeIfAbsent(item, k -> new LinkedHashMap<>());
                Set<DataComponentType<?>> removeSet = allRemovals.computeIfAbsent(item, k -> new HashSet<>());
                entry.getValue().entrySet().forEach(e -> {
                    if (e.getValue().isPresent()) {
                        removeSet.remove(e.getKey());
                        setMap.put(e.getKey(), e.getValue().get());
                    } else {
                        setMap.remove(e.getKey());
                        removeSet.add(e.getKey());
                    }
                });
            }
        }

        if (ItemComponentsReloadListener.DEFINITIONS.isEmpty() && allComponents.isEmpty() && allRemovals.isEmpty())
            return;

        // Sort by priority ascending so higher priority definitions are applied last and win
        List<ItemComponent> sorted = new ArrayList<>(ItemComponentsReloadListener.DEFINITIONS);
        sorted.sort(Comparator.comparingInt(ItemComponent::priority));

        for (ItemComponent definition : sorted) {
            if (!definition.item().isValid()) {
                InsaneLib.LOGGER.warn("ItemComponents: item '{}' not found in registry, skipping", definition.item().toSerializedString());
                continue;
            }
            if (definition.componentsRaw().isEmpty() && definition.removeComponents().isEmpty())
                continue;

            Map<DataComponentType<?>, Object> decoded = decodeComponents(definition.componentsRaw(), ops);

            for (Item item : BuiltInRegistries.ITEM) {
                if (!definition.item().matches(item))
                    continue;

                Map<DataComponentType<?>, Object> setMap = allComponents.computeIfAbsent(item, k -> new LinkedHashMap<>());
                Set<DataComponentType<?>> removeSet = allRemovals.computeIfAbsent(item, k -> new HashSet<>());

                // Set operations: remove from removals, add to set map (last wins)
                decoded.forEach((type, value) -> {
                    removeSet.remove(type);
                    setMap.put(type, value);
                });

                // Remove operations: remove from set map, add to removals (last wins)
                for (ResourceLocation id : definition.removeComponents()) {
                    DataComponentType<?> type = BuiltInRegistries.DATA_COMPONENT_TYPE.get(id);
                    if (type == null) {
                        InsaneLib.LOGGER.warn("ItemComponents: unknown component type '{}' in remove_components, skipping", id);
                        continue;
                    }
                    setMap.remove(type);
                    removeSet.add(type);
                }
            }
        }

        // PATCHED_COMPONENTS is still empty here → Item.components() in the mixin falls through → vanilla prototype
        Set<Item> affectedItems = new HashSet<>();
        affectedItems.addAll(allComponents.keySet());
        affectedItems.addAll(allRemovals.keySet());

        for (Item item : affectedItems) {
            Map<DataComponentType<?>, Object> toSet = allComponents.getOrDefault(item, Map.of());
            Set<DataComponentType<?>> toRemove = allRemovals.getOrDefault(item, Set.of());
            DataComponentPatch patch = buildPatch(toSet, toRemove);
            DataComponentMap patched = PatchedDataComponentMap.fromPatch(item.components(), patch);
            ItemComponentsReloadListener.PATCHED_COMPONENTS.put(item, patched);
        }

        InsaneLib.LOGGER.info("ItemComponents: applied components to {} items", affectedItems.size());
    }

    private static Map<DataComponentType<?>, Object> decodeComponents(Map<ResourceLocation, JsonElement> raw, RegistryOps<JsonElement> ops) {
        Map<DataComponentType<?>, Object> decoded = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, JsonElement> entry : raw.entrySet()) {
            DataComponentType<?> type = BuiltInRegistries.DATA_COMPONENT_TYPE.get(entry.getKey());
            if (type == null) {
                InsaneLib.LOGGER.warn("ItemComponents: unknown data component type '{}', skipping", entry.getKey());
                continue;
            }
            if (type.codec() == null) {
                InsaneLib.LOGGER.warn("ItemComponents: data component type '{}' is transient (no codec), skipping", entry.getKey());
                continue;
            }
            try {
                decoded.put(type, decodeComponent(type, entry.getValue(), ops));
            } catch (Exception e) {
                InsaneLib.LOGGER.error("ItemComponents: failed to decode component '{}': {}", entry.getKey(), e.getMessage());
            }
        }
        return decoded;
    }

    private static <T> T decodeComponent(DataComponentType<T> type, JsonElement json, RegistryOps<JsonElement> ops) {
        return type.codec().parse(ops, json).getOrThrow();
    }

    @SuppressWarnings("unchecked")
    private static DataComponentPatch buildPatch(Map<DataComponentType<?>, Object> toSet, Set<DataComponentType<?>> toRemove) {
        DataComponentPatch.Builder builder = DataComponentPatch.builder();
        toSet.forEach((type, value) -> builder.set((DataComponentType<Object>) type, value));
        toRemove.forEach(builder::remove);
        return builder.build();
    }
}
