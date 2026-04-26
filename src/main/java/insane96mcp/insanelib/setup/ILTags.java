package insane96mcp.insanelib.setup;

import insane96mcp.insanelib.InsaneLib;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ILTags {

    public static class Items {
        // Wood
        public static final TagKey<Item> EQUIPMENT_TOOLS_WOOD = tag("equipment/tools/wood");
        public static final TagKey<Item> EQUIPMENT_WEAPONS_WOOD = tag("equipment/weapons/wood");
        public static final TagKey<Item> EQUIPMENT_HAND_WOOD = tag("equipment/hand/wood");
        public static final TagKey<Item> EQUIPMENT_WOOD = tag("equipment/wood");

        // Stone
        public static final TagKey<Item> EQUIPMENT_TOOLS_STONE = tag("equipment/tools/stone");
        public static final TagKey<Item> EQUIPMENT_WEAPONS_STONE = tag("equipment/weapons/stone");
        public static final TagKey<Item> EQUIPMENT_HAND_STONE = tag("equipment/hand/stone");
        public static final TagKey<Item> EQUIPMENT_STONE = tag("equipment/stone");

        // Iron
        public static final TagKey<Item> EQUIPMENT_TOOLS_IRON = tag("equipment/tools/iron");
        public static final TagKey<Item> EQUIPMENT_WEAPONS_IRON = tag("equipment/weapons/iron");
        public static final TagKey<Item> EQUIPMENT_HAND_IRON = tag("equipment/hand/iron");
        public static final TagKey<Item> EQUIPMENT_ARMOR_IRON = tag("equipment/armor/iron");
        public static final TagKey<Item> EQUIPMENT_IRON = tag("equipment/iron");

        // Gold
        public static final TagKey<Item> EQUIPMENT_TOOLS_GOLD = tag("equipment/tools/gold");
        public static final TagKey<Item> EQUIPMENT_WEAPONS_GOLD = tag("equipment/weapons/gold");
        public static final TagKey<Item> EQUIPMENT_HAND_GOLD = tag("equipment/hand/gold");
        public static final TagKey<Item> EQUIPMENT_ARMOR_GOLD = tag("equipment/armor/gold");
        public static final TagKey<Item> EQUIPMENT_GOLD = tag("equipment/gold");

        // Diamond
        public static final TagKey<Item> EQUIPMENT_TOOLS_DIAMOND = tag("equipment/tools/diamond");
        public static final TagKey<Item> EQUIPMENT_WEAPONS_DIAMOND = tag("equipment/weapons/diamond");
        public static final TagKey<Item> EQUIPMENT_HAND_DIAMOND = tag("equipment/hand/diamond");
        public static final TagKey<Item> EQUIPMENT_ARMOR_DIAMOND = tag("equipment/armor/diamond");
        public static final TagKey<Item> EQUIPMENT_DIAMOND = tag("equipment/diamond");

        // Netherite
        public static final TagKey<Item> EQUIPMENT_TOOLS_NETHERITE = tag("equipment/tools/netherite");
        public static final TagKey<Item> EQUIPMENT_WEAPONS_NETHERITE = tag("equipment/weapons/netherite");
        public static final TagKey<Item> EQUIPMENT_HAND_NETHERITE = tag("equipment/hand/netherite");
        public static final TagKey<Item> EQUIPMENT_ARMOR_NETHERITE = tag("equipment/armor/netherite");
        public static final TagKey<Item> EQUIPMENT_NETHERITE = tag("equipment/netherite");

        // Leather (armor only)
        public static final TagKey<Item> EQUIPMENT_ARMOR_LEATHER = tag("equipment/armor/leather");
        public static final TagKey<Item> EQUIPMENT_LEATHER = tag("equipment/leather");

        // Chainmail (armor only)
        public static final TagKey<Item> EQUIPMENT_ARMOR_CHAINMAIL = tag("equipment/armor/chainmail");
        public static final TagKey<Item> EQUIPMENT_CHAINMAIL = tag("equipment/chainmail");

        // Turtle (armor only)
        public static final TagKey<Item> EQUIPMENT_ARMOR_TURTLE = tag("equipment/armor/turtle");
        public static final TagKey<Item> EQUIPMENT_TURTLE = tag("equipment/turtle");

        private static TagKey<Item> tag(String path) {
            return TagKey.create(Registries.ITEM, InsaneLib.location(path));
        }
    }
}
