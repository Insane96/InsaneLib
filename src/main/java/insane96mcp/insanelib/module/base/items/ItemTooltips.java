package insane96mcp.insanelib.module.base.items;

import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@LoadFeature(description = "Add item tooltips via item tags. Items in the insanelib:has_tooltip item tag get a tooltip with the vanilla name + .tooltip (e.g. item.minecraft.arrow.tooltip). Items in insanelib:has_hidden_tooltip get the same tooltip, but only shown while holding SHIFT.")
public class ItemTooltips extends Feature {
	public static final TagKey<Item> HAS_TOOLTIP = tag("has_tooltip");
	public static final TagKey<Item> HAS_HIDDEN_TOOLTIP = tag("has_hidden_tooltip");

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onItemTooltips(ItemTooltipEvent event) {
		if (!this.isEnabled())
			return;

		boolean showTooltip = event.getItemStack().is(HAS_TOOLTIP)
				|| (event.getItemStack().is(HAS_HIDDEN_TOOLTIP) && event.getFlags().hasShiftDown());
		if (!showTooltip)
			return;

		event.getToolTip().add(1, Component.translatable(event.getItemStack().getItem().getDescriptionId() + ".tooltip").withStyle(ChatFormatting.GRAY));
	}

	private static TagKey<Item> tag(String path) {
		return TagKey.create(Registries.ITEM, InsaneLib.id(path));
	}
}
