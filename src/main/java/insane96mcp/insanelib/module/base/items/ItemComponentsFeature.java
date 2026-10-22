package insane96mcp.insanelib.module.base.items;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
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
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

import javax.annotation.Nullable;
import java.util.*;

@LoadFeature(description = "Allows modifying item data components via data packs", canBeDisabled = false)
public class ItemComponentsFeature extends Feature {

    @Config(description = "Vanilla limits max stack sizes to 99 for some reasons. This increases that limit to 1073741823 (half the max integer value, so that two stacks can be summed without overflowing). For items with stack > 99 the number showing the count is shrunk. Items with count > 999 will show 1k, 1m, 1b. Requires MC restart.")
    public static Boolean increaseStackLimit = true;
    @Config(description = "Always render the item stack count at a smaller scale, even when it's 99 or below")
    public static Boolean alwaysShrinkStackCount = false;
    @Config
    public static Boolean disableShrinkStackCount = false;

    /**
     * {@link #increaseStackLimit} as read the first time the config is loaded. Changing it while the game is running
     * isn't supported, as stacks over the new limit would fail to be saved and loaded.
     */
    @Nullable
    private static Boolean stackLimitIncreased = null;

    @Override
    public void readConfig(ModConfigEvent event) {
        super.readConfig(event);
        if (stackLimitIncreased == null)
            stackLimitIncreased = increaseStackLimit;
    }

    /**
     * Half of {@link Integer#MAX_VALUE}, as vanilla sums the counts of two stacks in an int when merging them
     * (e.g. {@code AbstractContainerMenu#moveItemStackTo}), which would overflow and delete the items
     */
    public static final int INCREASED_STACK_LIMIT = Integer.MAX_VALUE / 2;

    public static int getStackLimit() {
        boolean increased = stackLimitIncreased != null ? stackLimitIncreased : increaseStackLimit;
        return increased ? INCREASED_STACK_LIMIT : Item.ABSOLUTE_MAX_STACK_SIZE;
    }

    /**
     * Replaces vanilla's {@code ExtraCodecs.intRange(min, 99)}. The max is checked on each decode/encode instead of being
     * baked in the codec, as the codecs are built while bootstrapping Minecraft, before the config is loaded.
     */
    public static Codec<Integer> stackSizeCodec(int min) {
        return Codec.INT.validate(value -> value >= min && value <= getStackLimit()
                ? DataResult.success(value)
                : DataResult.error(() -> "Value must be within range [" + min + ";" + getStackLimit() + "]: " + value));
    }

    /**
     * Only the server computes the patches; remote clients receive them through {@link ItemComponentsSyncMessage}.
     * Clients must not touch them here: on /reload the tags packet is received after the sync message.
     */
    @SubscribeEvent
    public void onTagsUpdated(TagsUpdatedEvent event) {
        if (event.getUpdateCause() != TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD)
            return;

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

        if (ItemComponentsReloadListener.DEFINITIONS.isEmpty() && allComponents.isEmpty() && allRemovals.isEmpty()) {
            ItemComponentsReloadListener.PATCHED_COMPONENTS = Map.of();
            ItemComponentsReloadListener.SYNCED_PATCHES = Map.of();
            return;
        }

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
                    // A component removed by a previous definition counts as absent: the merge json alone becomes the new value
                    boolean removed = removeSet.contains(type);
                    Object existingValue = setMap.containsKey(type) ? setMap.get(type) : removed ? null : ItemComponentsReloadListener.getUnpatchedComponents(item).get(type);
                    try {
                        Object value = existingValue == null
                                ? decodeComponent(type, entry.getValue(), ops)
                                : decodeComponent(type, deepMerge(encodeComponent(type, existingValue, ops), entry.getValue()), ops);
                        removeSet.remove(type);
                        setMap.put(type, value);
                    } catch (Exception e) {
                        InsaneLib.LOGGER.error("ItemComponents: failed to merge component '{}' for item {}{}: {}", entry.getKey(), BuiltInRegistries.ITEM.getKey(item), removed ? " (component kept removed)" : "", e.getMessage());
                    }
                }
            }
        }

        Set<Item> affectedItems = new HashSet<>();
        affectedItems.addAll(allComponents.keySet());
        affectedItems.addAll(allRemovals.keySet());

        Map<Item, DataComponentMap> patchedComponents = new HashMap<>();
        Map<ResourceLocation, DataComponentPatch> syncedPatches = new HashMap<>();
        for (Item item : affectedItems) {
            Map<DataComponentType<?>, Object> toSet = allComponents.getOrDefault(item, Map.of());
            Set<DataComponentType<?>> toRemove = allRemovals.getOrDefault(item, Set.of());
            DataComponentPatch patch = buildPatch(toSet, toRemove);
            patchedComponents.put(item, PatchedDataComponentMap.fromPatch(ItemComponentsReloadListener.getUnpatchedComponents(item), patch));
            syncedPatches.put(BuiltInRegistries.ITEM.getKey(item), patch);
        }
        ItemComponentsReloadListener.PATCHED_COMPONENTS = Map.copyOf(patchedComponents);
        ItemComponentsReloadListener.SYNCED_PATCHES = Map.copyOf(syncedPatches);

        InsaneLib.LOGGER.info("ItemComponents: applied components to {} items", affectedItems.size());
    }

    @SubscribeEvent
    public void onDatapackSync(OnDatapackSyncEvent event) {
        // Sent even when empty, so clients drop the patches of a previous server/reload
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
