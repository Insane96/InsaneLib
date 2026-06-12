package insane96mcp.insanelib.mixin.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AnvilScreen.class, priority = 1001)
public abstract class AnvilScreenMixin extends ItemCombinerScreen<AnvilMenu> {
	public AnvilScreenMixin(AnvilMenu menu, Inventory inventory, Component title, ResourceLocation background) {
		super(menu, inventory, title, background);
	}

	@Inject(method = "renderLabels", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/ItemCombinerScreen;renderLabels(Lnet/minecraft/client/gui/GuiGraphics;II)V", shift = At.Shift.AFTER), cancellable = true)
	public void insanelib$preventLabelRenderWithCost0(GuiGraphics guiGraphics, int mouseX, int mouseY, CallbackInfo ci) {
		if (this.menu.getCost() != 0 || !this.menu.getSlot(2).hasItem())
			return;

		ci.cancel();
	}
}