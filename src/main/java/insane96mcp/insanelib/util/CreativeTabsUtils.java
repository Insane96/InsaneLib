package insane96mcp.insanelib.util;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import java.util.function.Supplier;

public class CreativeTabsUtils {
	public static void addBefore(BuildCreativeModeTabContentsEvent event, Item before, ItemLike itemToAdd) {
		addBefore(event, before, new ItemStack(itemToAdd));
	}

	public static void addBefore(BuildCreativeModeTabContentsEvent event, Item before, ItemStack stackToAdd) {
		event.insertBefore(new ItemStack(before), stackToAdd, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	}

	public static void addBefore(BuildCreativeModeTabContentsEvent event, Item before, Supplier<? extends ItemLike> itemToAdd) {
		addBefore(event, before, itemToAdd.get());
	}

	public static void addAfter(BuildCreativeModeTabContentsEvent event, Item after, ItemLike itemToAdd) {
		addAfter(event, after, new ItemStack(itemToAdd));
	}

	public static void addAfter(BuildCreativeModeTabContentsEvent event, Item after, ItemStack stackToAdd) {
		event.insertAfter(new ItemStack(after), stackToAdd, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	}

	public static void addAfter(BuildCreativeModeTabContentsEvent event, Item after, Supplier<? extends ItemLike> itemToAdd) {
		addAfter(event, after, itemToAdd.get());
	}

	public static void remove(BuildCreativeModeTabContentsEvent event, ItemStack itemToRemove) {
		event.remove(itemToRemove, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	}

	public static void remove(BuildCreativeModeTabContentsEvent event, Item itemToRemove) {
		event.remove(new ItemStack(itemToRemove), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	}
}
