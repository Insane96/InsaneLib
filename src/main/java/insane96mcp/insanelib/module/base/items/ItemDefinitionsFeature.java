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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@LoadFeature(description = "Allows modifying item data components via data packs", canBeDisabled = false)
public class ItemDefinitionsFeature extends Feature {

    @SubscribeEvent
    public void onTagsUpdated(TagsUpdatedEvent event) {
        if (!event.shouldUpdateStaticData())
            return;

        ItemDefinitionsReloadListener.PATCHED_COMPONENTS.clear();
        if (ItemDefinitionsReloadListener.DEFINITIONS.isEmpty())
            return;

        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, event.getRegistryAccess());

        // Collect merged components per item (last definition wins per component type)
        Map<Item, Map<DataComponentType<?>, Object>> allComponents = new HashMap<>();

        for (ItemDefinition definition : ItemDefinitionsReloadListener.DEFINITIONS) {
            if (!definition.item().isValid()) {
                InsaneLib.LOGGER.warn("ItemDefinitions: item '{}' not found in registry, skipping", definition.item().toSerializedString());
                continue;
            }
            if (definition.componentsRaw().isEmpty())
                continue;

            Map<DataComponentType<?>, Object> decoded = decodeComponents(definition.componentsRaw(), ops);
            if (decoded.isEmpty())
                continue;

            for (Item item : BuiltInRegistries.ITEM) {
                if (!definition.item().matches(item))
                    continue;
                allComponents.computeIfAbsent(item, k -> new LinkedHashMap<>()).putAll(decoded);
            }
        }

        // PATCHED_COMPONENTS is still empty here → Item.components() in the mixin falls through → vanilla prototype
        for (Map.Entry<Item, Map<DataComponentType<?>, Object>> entry : allComponents.entrySet()) {
            Item item = entry.getKey();
            DataComponentPatch patch = buildPatch(entry.getValue());
            DataComponentMap patched = PatchedDataComponentMap.fromPatch(item.components(), patch);
            ItemDefinitionsReloadListener.PATCHED_COMPONENTS.put(item, patched);
        }

        InsaneLib.LOGGER.info("ItemDefinitions: applied components to {} items", allComponents.size());
    }

    private static Map<DataComponentType<?>, Object> decodeComponents(Map<ResourceLocation, JsonElement> raw, RegistryOps<JsonElement> ops) {
        Map<DataComponentType<?>, Object> decoded = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, JsonElement> entry : raw.entrySet()) {
            DataComponentType<?> type = BuiltInRegistries.DATA_COMPONENT_TYPE.get(entry.getKey());
            if (type == null) {
                InsaneLib.LOGGER.warn("ItemDefinitions: unknown data component type '{}', skipping", entry.getKey());
                continue;
            }
            if (type.codec() == null) {
                InsaneLib.LOGGER.warn("ItemDefinitions: data component type '{}' is transient (no codec), skipping", entry.getKey());
                continue;
            }
            try {
                decoded.put(type, decodeComponent(type, entry.getValue(), ops));
            } catch (Exception e) {
                InsaneLib.LOGGER.error("ItemDefinitions: failed to decode component '{}': {}", entry.getKey(), e.getMessage());
            }
        }
        return decoded;
    }

    private static <T> T decodeComponent(DataComponentType<T> type, JsonElement json, RegistryOps<JsonElement> ops) {
        return type.codec().parse(ops, json).getOrThrow();
    }

    @SuppressWarnings("unchecked")
    private static DataComponentPatch buildPatch(Map<DataComponentType<?>, Object> components) {
        DataComponentPatch.Builder builder = DataComponentPatch.builder();
        components.forEach((type, value) -> builder.set((DataComponentType<Object>) type, value));
        return builder.build();
    }
}
