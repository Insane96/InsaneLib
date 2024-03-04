package insane96mcp.insanelib.world.enchantments;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public interface IEnchantmentTooltip {
    Component getTooltip(ItemStack stack, int lvl);
}
