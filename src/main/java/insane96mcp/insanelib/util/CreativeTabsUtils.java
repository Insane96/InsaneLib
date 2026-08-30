package insane96mcp.insanelib.util;

import insane96mcp.insanelib.setup.ILTags;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
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

	/**
	 * Removes every item in the {@link ILTags.Items#CREATIVE_REMOVAL} tag from creative mode tabs.
	 * Must be registered at {@code EventPriority.LOWEST} so it also catches items added by other mods.
	 */
	public static void removeCreativeRemovalTaggedItems(BuildCreativeModeTabContentsEvent event) {
		BuiltInRegistries.ITEM.getTag(ILTags.Items.CREATIVE_REMOVAL).ifPresent(holders -> {
			for (Holder<Item> holder : holders) {
				Item item = holder.value();
				// Match by item type only (not exact ItemStack/components): some items (e.g. Storage Drawers'
				// Detached Drawer) attach non-empty data components to their default instance, which would never
				// equal the bare ItemStack built by remove(Item), silently failing to remove them from the tab.
				// BuildCreativeModeTabContentsEvent has no removeIf, so collect matches first (its entry sets are
				// unmodifiable views) then remove each one.
				List<ItemStack> matches = new ArrayList<>();
				for (ItemStack stack : event.getParentEntries()) {
					if (stack.is(item))
						matches.add(stack);
				}
				for (ItemStack stack : event.getSearchEntries()) {
					if (stack.is(item))
						matches.add(stack);
				}
				for (ItemStack stack : matches) {
					event.remove(stack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
				}
			}
		});
	}
}
