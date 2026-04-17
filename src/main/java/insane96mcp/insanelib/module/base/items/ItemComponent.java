package insane96mcp.insanelib.module.base.items;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import insane96mcp.insanelib.data.ObjTag;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Defines component overrides for one or more items, loaded from data packs.
 *
 * <p>JSON files must be placed under {@code data/<namespace>/item_components/} and have a {@code .json} extension.
 * Each file defines a single item component definition.
 *
 * <p>The {@code "item"} field accepts either an item id or a tag (prefixed with {@code #}):
 * <pre>{@code
 * "item": "minecraft:diamond_sword"
 * "item": "#minecraft:swords"
 * }</pre>
 *
 * <p>The {@code "components"} field is a map of data component ids to their values, using the same
 * format as vanilla item NBT/codec (same as the {@code /give} command components):
 * <pre>{@code
 * {
 *   "item": "minecraft:diamond_sword",
 *   "components": {
 *     "minecraft:max_damage": 3000,
 *     "minecraft:attribute_modifiers": {
 *       "modifiers": [
 *         {
 *           "type": "minecraft:generic.attack_damage",
 *           "id": "minecraft:attack_damage",
 *           "amount": 3.0,
 *           "operation": "add_value",
 *           "slot": "mainhand"
 *         }
 *       ]
 *     }
 *   }
 * }
 * }</pre>
 *
 * <p>Use {@code "remove_components"} to explicitly remove components that the item has by default:
 * <pre>{@code
 * {
 *   "item": "minecraft:apple",
 *   "remove_components": [
 *     "minecraft:food"
 *   ]
 * }
 * }</pre>
 *
 * <p>Use {@code "merge_components"} to deep-merge component values with the item's existing ones instead of
 * replacing them. Arrays are concatenated, objects are recursively merged, and primitives are overridden.
 * This is useful for adding entries to a list component (e.g. appending attribute modifiers) without
 * discarding the item's existing values:
 * <pre>{@code
 * {
 *   "item": "minecraft:iron_pickaxe",
 *   "merge_components": {
 *     "minecraft:attribute_modifiers": {
 *       "modifiers": [
 *         {
 *           "type": "insanesurvivaloverhaul:piercing_damage",
 *           "id": "insanesurvivaloverhaul:piercing_damage",
 *           "amount": 1.0,
 *           "operation": "add_value",
 *           "slot": "mainhand"
 *         }
 *       ]
 *     }
 *   }
 * }
 * }</pre>
 *
 * <p>The optional {@code "priority"} field (integer, default {@code 0}) controls merge order when multiple
 * definitions target the same item. Higher priority wins — definitions are applied in ascending priority order,
 * so a definition with priority {@code 10} overwrites one with priority {@code 0}. Definitions with equal
 * priority follow file load order.
 *
 * <p>When multiple definitions target the same item, they are merged — higher priority wins per component type.
 * A higher-priority remove overrides a lower-priority set, and vice versa.
 */
public record ItemComponent(ObjTag<Item> item, Map<ResourceLocation, JsonElement> componentsRaw, Map<ResourceLocation, JsonElement> mergeComponentsRaw, List<ResourceLocation> removeComponents, int priority) {

    public static ItemComponent fromJson(JsonObject json) {
        ObjTag<Item> item = ObjTag.deserialize(json.get("item"), Registries.ITEM);
        Map<ResourceLocation, JsonElement> componentsRaw = new LinkedHashMap<>();
        if (json.has("components")) {
            JsonObject components = json.getAsJsonObject("components");
            for (Map.Entry<String, JsonElement> entry : components.entrySet()) {
                componentsRaw.put(ResourceLocation.parse(entry.getKey()), entry.getValue());
            }
        }
        Map<ResourceLocation, JsonElement> mergeComponentsRaw = new LinkedHashMap<>();
        if (json.has("merge_components")) {
            JsonObject mergeComponents = json.getAsJsonObject("merge_components");
            for (Map.Entry<String, JsonElement> entry : mergeComponents.entrySet()) {
                mergeComponentsRaw.put(ResourceLocation.parse(entry.getKey()), entry.getValue());
            }
        }
        List<ResourceLocation> removeComponents = new ArrayList<>();
        if (json.has("remove_components")) {
            for (JsonElement element : json.getAsJsonArray("remove_components")) {
                removeComponents.add(ResourceLocation.parse(element.getAsString()));
            }
        }
        int priority = json.has("priority") ? json.get("priority").getAsInt() : 0;
        return new ItemComponent(item, componentsRaw, mergeComponentsRaw, removeComponents, priority);
    }
}
