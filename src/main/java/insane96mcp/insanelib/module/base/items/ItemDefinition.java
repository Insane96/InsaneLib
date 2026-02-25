package insane96mcp.insanelib.module.base.items;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import insane96mcp.insanelib.data.ObjTag;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Defines component overrides for one or more items, loaded from data packs.
 *
 * <p>JSON files must be placed under {@code data/<namespace>/item_definitions/} and have a {@code .json} extension.
 * Each file defines a single item definition.
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
 * <p>When multiple definitions target the same item, they are merged — last definition wins per component type.
 */
public record ItemDefinition(ObjTag<Item> item, Map<ResourceLocation, JsonElement> componentsRaw) {

    public static ItemDefinition fromJson(JsonObject json) {
        ObjTag<Item> item = ObjTag.deserialize(json.get("item"), Registries.ITEM);
        Map<ResourceLocation, JsonElement> componentsRaw = new LinkedHashMap<>();
        if (json.has("components")) {
            JsonObject components = json.getAsJsonObject("components");
            for (Map.Entry<String, JsonElement> entry : components.entrySet()) {
                componentsRaw.put(ResourceLocation.parse(entry.getKey()), entry.getValue());
            }
        }
        return new ItemDefinition(item, componentsRaw);
    }
}