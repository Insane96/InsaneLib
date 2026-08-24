package insane96mcp.insanelib.setup;

import insane96mcp.insanelib.InsaneLib;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;

public class ILTags {

    public static class DamageTypes {
        // Damage sources whose damage type is in this tag will not grant invincibility frames (hurt cooldown) to the entity they damage
        public static final TagKey<DamageType> NO_INVINCIBILITY_FRAMES = tag("no_invincibility_frames");

        private static TagKey<DamageType> tag(String path) {
            return TagKey.create(Registries.DAMAGE_TYPE, InsaneLib.id(path));
        }
    }

    public static class Items {
        // Wooden
        public static final TagKey<Item> EQUIPMENT_TOOLS_WOODEN = tag("equipment/tools/wooden");
        public static final TagKey<Item> EQUIPMENT_WEAPONS_WOODEN = tag("equipment/weapons/wooden");
        public static final TagKey<Item> EQUIPMENT_HAND_WOODEN = tag("equipment/hand/wooden");
        public static final TagKey<Item> EQUIPMENT_WOODEN = tag("equipment/wooden");

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

        // Golden
        public static final TagKey<Item> EQUIPMENT_TOOLS_GOLDEN = tag("equipment/tools/golden");
        public static final TagKey<Item> EQUIPMENT_WEAPONS_GOLDEN = tag("equipment/weapons/golden");
        public static final TagKey<Item> EQUIPMENT_HAND_GOLDEN = tag("equipment/hand/golden");
        public static final TagKey<Item> EQUIPMENT_ARMOR_GOLDEN = tag("equipment/armor/golden");
        public static final TagKey<Item> EQUIPMENT_GOLDEN = tag("equipment/golden");

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

        // Copper (items don't exist in vanilla 1.21.1, entries are required=false)
        public static final TagKey<Item> EQUIPMENT_TOOLS_COPPER = tag("equipment/tools/copper");
        public static final TagKey<Item> EQUIPMENT_WEAPONS_COPPER = tag("equipment/weapons/copper");
        public static final TagKey<Item> EQUIPMENT_HAND_COPPER = tag("equipment/hand/copper");
        public static final TagKey<Item> EQUIPMENT_ARMOR_COPPER = tag("equipment/armor/copper");
        public static final TagKey<Item> EQUIPMENT_COPPER = tag("equipment/copper");

        // Leather (armor only)
        public static final TagKey<Item> EQUIPMENT_ARMOR_LEATHER = tag("equipment/armor/leather");
        public static final TagKey<Item> EQUIPMENT_LEATHER = tag("equipment/leather");

        // Chainmail (armor only)
        public static final TagKey<Item> EQUIPMENT_ARMOR_CHAINMAIL = tag("equipment/armor/chainmail");
        public static final TagKey<Item> EQUIPMENT_CHAINMAIL = tag("equipment/chainmail");

        // Turtle (armor only)
        public static final TagKey<Item> EQUIPMENT_ARMOR_TURTLE = tag("equipment/armor/turtle");
        public static final TagKey<Item> EQUIPMENT_TURTLE = tag("equipment/turtle");

        // Items in this tag are removed from creative mode tabs, even if added by other mods
        public static final TagKey<Item> CREATIVE_REMOVAL = tag("creative_removal");

        private static TagKey<Item> tag(String path) {
            return TagKey.create(Registries.ITEM, InsaneLib.id(path));
        }
    }
}
