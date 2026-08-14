package insane96mcp.insanelib.module.base.items;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.network.message.ItemComponentsSyncMessage;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

import java.util.*;

@LoadFeature(description = "Allows modifying item data components via data packs", canBeDisabled = false)
public class ItemComponentsFeature extends Feature {

    @Config(description = "Vanilla limits max stack sizes to 99 for some reasons. This replaces that limit")
    public static Integer stackLimit = 9999;

    @Config
    public static Boolean disableShrinkStackCount = false;
    @Config(description = "Always render the item stack count at a smaller scale, even when it's 99 or below")
    public static Boolean alwaysShrinkStackCount = false;

    @SubscribeEvent
    public void onTagsUpdated(TagsUpdatedEvent event) {
        if (!event.shouldUpdateStaticData())
            return;

        ItemComponentsReloadListener.PATCHED_COMPONENTS.clear();
        ItemComponentsReloadListener.SYNCED_PATCHES.clear();

        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, event.getRegistryAccess());

        // Collect merged components per item (higher priority wins per component type)
        Map<Item, Map<DataComponentType<?>, Object>> allComponents = new HashMap<>();
        Map<Item, Set<DataComponentType<?>>> allRemovals = new HashMap<>();

        // Seed with programmatic patches first (lowest priority — data pack definitions overwrite these)
        for (var provider : ItemComponentsReloadListener.PROGRAMMATIC_PROVIDERS) {
            Map<Item, DataComponentPatch> programmatic = new HashMap<>();
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
            if (definition.componentsRaw().isEmpty() && definition.mergeComponentsRaw().isEmpty() && definition.removeComponents().isEmpty())
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

                // TODO: merge_components can silently resurrect a component that a same-item definition already
                //  removed earlier in this same pass. Below, `existingValue` falls back to `item.components().get(type)`
                //  (the vanilla/default value) whenever the component isn't already staged in `setMap` - it never checks
                //  `removeSet`. So given two definitions targeting the same item (e.g. one matching by item id, another
                //  matching the same item via a tag) processed in file/priority order, if the first removes a component
                //  and the second merges into that same component type, the merge fallback pulls the vanilla default
                //  back in and re-adds it to `setMap`, undoing the removal with no warning logged.
                //  Concrete repro (found in ISO): `golden_carrot.json` removed `minecraft:food` via `remove_components`,
                //  but `raw_foods.json` targeted `#insanesurvivaloverhaul:raw_foods` (which golden_carrot was tagged
                //  into) with a `merge_components` on `minecraft:food`. Since raw_foods.json sorted after
                //  golden_carrot.json (same priority, later file), its merge resurrected `minecraft:food` on golden
                //  carrot with the raw-food poison effect merged in, making it edible again. Worked around on the ISO
                //  side by removing golden_carrot from that tag, but the underlying gap here remains: nothing stops the
                //  next definition/tag combo from hitting the same silent resurrection.
                //  Fix direction: check `removeSet.contains(type)` before falling back to `item.components().get(type)`
                //  and decide the semantics - either treat it as "no existing value" (merge JSON becomes the fresh
                //  value, removal is no longer relevant) or treat the prior removal as authoritative (skip the merge
                //  and warn) - then remove `removeSet.remove(type)` above if the removal should win.
                // Merge operations: deep-merge JSON into existing value (arrays concatenated, objects recursively merged)
                for (Map.Entry<ResourceLocation, JsonElement> entry : definition.mergeComponentsRaw().entrySet()) {
                    DataComponentType<?> type = BuiltInRegistries.DATA_COMPONENT_TYPE.get(entry.getKey());
                    if (type == null) {
                        InsaneLib.LOGGER.warn("ItemComponents: unknown component type '{}' in merge_components, skipping", entry.getKey());
                        continue;
                    }
                    if (type.codec() == null) {
                        InsaneLib.LOGGER.warn("ItemComponents: component type '{}' in merge_components is transient (no codec), skipping", entry.getKey());
                        continue;
                    }
                    removeSet.remove(type);
                    Object existingValue = setMap.containsKey(type) ? setMap.get(type) : item.components().get(type);
                    try {
                        if (existingValue == null) {
                            setMap.put(type, decodeComponent(type, entry.getValue(), ops));
                        } else {
                            JsonElement existingJson = encodeComponent(type, existingValue, ops);
                            JsonElement merged = deepMerge(existingJson, entry.getValue());
                            setMap.put(type, decodeComponent(type, merged, ops));
                        }
                    } catch (Exception e) {
                        InsaneLib.LOGGER.error("ItemComponents: failed to merge component '{}': {}", entry.getKey(), e.getMessage());
                    }
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
            ItemComponentsReloadListener.SYNCED_PATCHES.put(BuiltInRegistries.ITEM.getKey(item), patch);
        }

        InsaneLib.LOGGER.info("ItemComponents: applied components to {} items", affectedItems.size());
    }

    @SubscribeEvent
    public void onDatapackSync(OnDatapackSyncEvent event) {
        if (ItemComponentsReloadListener.SYNCED_PATCHES.isEmpty())
            return;

        if (event.getPlayer() == null) {
            for (ServerPlayer player : event.getPlayerList().getPlayers())
                ItemComponentsSyncMessage.sync(ItemComponentsReloadListener.SYNCED_PATCHES, player);
        }
        else {
            ItemComponentsSyncMessage.sync(ItemComponentsReloadListener.SYNCED_PATCHES, event.getPlayer());
        }
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
    private static <T> JsonElement encodeComponent(DataComponentType<T> type, Object value, RegistryOps<JsonElement> ops) {
        return type.codec().encodeStart(ops, (T) value).getOrThrow();
    }

    private static JsonElement deepMerge(JsonElement base, JsonElement override) {
        if (base.isJsonObject() && override.isJsonObject()) {
            JsonObject result = base.getAsJsonObject().deepCopy();
            for (Map.Entry<String, JsonElement> e : override.getAsJsonObject().entrySet())
                result.add(e.getKey(), result.has(e.getKey()) ? deepMerge(result.get(e.getKey()), e.getValue()) : e.getValue());
            return result;
        } else if (base.isJsonArray() && override.isJsonArray()) {
            JsonArray result = new JsonArray();
            result.addAll(base.getAsJsonArray());
            result.addAll(override.getAsJsonArray());
            return result;
        }
        return override;
    }

    @SuppressWarnings("unchecked")
    private static DataComponentPatch buildPatch(Map<DataComponentType<?>, Object> toSet, Set<DataComponentType<?>> toRemove) {
        DataComponentPatch.Builder builder = DataComponentPatch.builder();
        toSet.forEach((type, value) -> builder.set((DataComponentType<Object>) type, value));
        toRemove.forEach(builder::remove);
        return builder.build();
    }
}
