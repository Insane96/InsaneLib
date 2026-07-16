package insane96mcp.insanelib.datagen;

import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.setup.ILTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

import java.util.concurrent.CompletableFuture;

public class ILItemTagProvider extends ItemTagsProvider {

    public ILItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, InsaneLib.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // Wooden
        tag(ILTags.Items.EQUIPMENT_TOOLS_WOODEN).add(Items.WOODEN_PICKAXE, Items.WOODEN_AXE, Items.WOODEN_SHOVEL, Items.WOODEN_HOE);
        tag(ILTags.Items.EQUIPMENT_WEAPONS_WOODEN).add(Items.WOODEN_SWORD);
        tag(ILTags.Items.EQUIPMENT_HAND_WOODEN).addTag(ILTags.Items.EQUIPMENT_TOOLS_WOODEN).addTag(ILTags.Items.EQUIPMENT_WEAPONS_WOODEN);
        tag(ILTags.Items.EQUIPMENT_WOODEN).addTag(ILTags.Items.EQUIPMENT_HAND_WOODEN);

        // Stone
        tag(ILTags.Items.EQUIPMENT_TOOLS_STONE).add(Items.STONE_PICKAXE, Items.STONE_AXE, Items.STONE_SHOVEL, Items.STONE_HOE);
        tag(ILTags.Items.EQUIPMENT_WEAPONS_STONE).add(Items.STONE_SWORD);
        tag(ILTags.Items.EQUIPMENT_HAND_STONE).addTag(ILTags.Items.EQUIPMENT_TOOLS_STONE).addTag(ILTags.Items.EQUIPMENT_WEAPONS_STONE);
        tag(ILTags.Items.EQUIPMENT_STONE).addTag(ILTags.Items.EQUIPMENT_HAND_STONE);

        // Iron
        tag(ILTags.Items.EQUIPMENT_TOOLS_IRON).add(Items.IRON_PICKAXE, Items.IRON_AXE, Items.IRON_SHOVEL, Items.IRON_HOE);
        tag(ILTags.Items.EQUIPMENT_WEAPONS_IRON).add(Items.IRON_SWORD);
        tag(ILTags.Items.EQUIPMENT_HAND_IRON).addTag(ILTags.Items.EQUIPMENT_TOOLS_IRON).addTag(ILTags.Items.EQUIPMENT_WEAPONS_IRON);
        tag(ILTags.Items.EQUIPMENT_ARMOR_IRON).add(Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS);
        tag(ILTags.Items.EQUIPMENT_IRON).addTag(ILTags.Items.EQUIPMENT_HAND_IRON).addTag(ILTags.Items.EQUIPMENT_ARMOR_IRON);

        // Golden
        tag(ILTags.Items.EQUIPMENT_TOOLS_GOLDEN).add(Items.GOLDEN_PICKAXE, Items.GOLDEN_AXE, Items.GOLDEN_SHOVEL, Items.GOLDEN_HOE);
        tag(ILTags.Items.EQUIPMENT_WEAPONS_GOLDEN).add(Items.GOLDEN_SWORD);
        tag(ILTags.Items.EQUIPMENT_HAND_GOLDEN).addTag(ILTags.Items.EQUIPMENT_TOOLS_GOLDEN).addTag(ILTags.Items.EQUIPMENT_WEAPONS_GOLDEN);
        tag(ILTags.Items.EQUIPMENT_ARMOR_GOLDEN).add(Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS);
        tag(ILTags.Items.EQUIPMENT_GOLDEN).addTag(ILTags.Items.EQUIPMENT_HAND_GOLDEN).addTag(ILTags.Items.EQUIPMENT_ARMOR_GOLDEN);

        // Diamond
        tag(ILTags.Items.EQUIPMENT_TOOLS_DIAMOND).add(Items.DIAMOND_PICKAXE, Items.DIAMOND_AXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_HOE);
        tag(ILTags.Items.EQUIPMENT_WEAPONS_DIAMOND).add(Items.DIAMOND_SWORD);
        tag(ILTags.Items.EQUIPMENT_HAND_DIAMOND).addTag(ILTags.Items.EQUIPMENT_TOOLS_DIAMOND).addTag(ILTags.Items.EQUIPMENT_WEAPONS_DIAMOND);
        tag(ILTags.Items.EQUIPMENT_ARMOR_DIAMOND).add(Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS);
        tag(ILTags.Items.EQUIPMENT_DIAMOND).addTag(ILTags.Items.EQUIPMENT_HAND_DIAMOND).addTag(ILTags.Items.EQUIPMENT_ARMOR_DIAMOND);

        // Netherite
        tag(ILTags.Items.EQUIPMENT_TOOLS_NETHERITE).add(Items.NETHERITE_PICKAXE, Items.NETHERITE_AXE, Items.NETHERITE_SHOVEL, Items.NETHERITE_HOE);
        tag(ILTags.Items.EQUIPMENT_WEAPONS_NETHERITE).add(Items.NETHERITE_SWORD);
        tag(ILTags.Items.EQUIPMENT_HAND_NETHERITE).addTag(ILTags.Items.EQUIPMENT_TOOLS_NETHERITE).addTag(ILTags.Items.EQUIPMENT_WEAPONS_NETHERITE);
        tag(ILTags.Items.EQUIPMENT_ARMOR_NETHERITE).add(Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS);
        tag(ILTags.Items.EQUIPMENT_NETHERITE).addTag(ILTags.Items.EQUIPMENT_HAND_NETHERITE).addTag(ILTags.Items.EQUIPMENT_ARMOR_NETHERITE);

        // Copper (vanilla equipment since the Copper Age drop)
        tag(ILTags.Items.EQUIPMENT_TOOLS_COPPER).add(Items.COPPER_PICKAXE, Items.COPPER_AXE, Items.COPPER_SHOVEL, Items.COPPER_HOE);
        tag(ILTags.Items.EQUIPMENT_WEAPONS_COPPER).add(Items.COPPER_SWORD);
        tag(ILTags.Items.EQUIPMENT_HAND_COPPER).addTag(ILTags.Items.EQUIPMENT_TOOLS_COPPER).addTag(ILTags.Items.EQUIPMENT_WEAPONS_COPPER);
        tag(ILTags.Items.EQUIPMENT_ARMOR_COPPER).add(Items.COPPER_HELMET, Items.COPPER_CHESTPLATE, Items.COPPER_LEGGINGS, Items.COPPER_BOOTS);
        tag(ILTags.Items.EQUIPMENT_COPPER).addTag(ILTags.Items.EQUIPMENT_HAND_COPPER).addTag(ILTags.Items.EQUIPMENT_ARMOR_COPPER);

        // Leather (armor only)
        tag(ILTags.Items.EQUIPMENT_ARMOR_LEATHER).add(Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS);
        tag(ILTags.Items.EQUIPMENT_LEATHER).addTag(ILTags.Items.EQUIPMENT_ARMOR_LEATHER);

        // Chainmail (armor only)
        tag(ILTags.Items.EQUIPMENT_ARMOR_CHAINMAIL).add(Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS);
        tag(ILTags.Items.EQUIPMENT_CHAINMAIL).addTag(ILTags.Items.EQUIPMENT_ARMOR_CHAINMAIL);

        // Turtle (armor only)
        tag(ILTags.Items.EQUIPMENT_ARMOR_TURTLE).add(Items.TURTLE_HELMET);
        tag(ILTags.Items.EQUIPMENT_TURTLE).addTag(ILTags.Items.EQUIPMENT_ARMOR_TURTLE);
    }

    @Override
    public String getName() {
        return "InsaneLib Item Tags";
    }
}
