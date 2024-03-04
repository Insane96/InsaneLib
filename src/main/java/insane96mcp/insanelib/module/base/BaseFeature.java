package insane96mcp.insanelib.module.base;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.insanelib.world.enchantments.IEnchantmentTooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;

@LoadFeature(module = "insanelib:base", canBeDisabled = false)
@Label(name = "Base")
public class BaseFeature extends Feature {

    public BaseFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent event) {
        if (!this.isEnabled()
                || !event.getItemStack().isEnchanted()
                || !Screen.hasShiftDown())
            return;

        Map<Enchantment, Integer> allEnchantments = event.getItemStack().getAllEnchantments();
        for (Enchantment enchantment : allEnchantments.keySet()) {
            if (!(enchantment instanceof IEnchantmentTooltip enchantmentTooltip))
                continue;
            int lvl = allEnchantments.get(enchantment);
            event.getToolTip().add(enchantmentTooltip.getTooltip(event.getItemStack(), lvl));
        }
    }
}
