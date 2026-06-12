package insane96mcp.insanelib.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.inventory.AnvilMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AnvilMenu.class)
public class AnvilMenuMixin {
	@Definition(id = "cost", field = "Lnet/minecraft/world/inventory/AnvilMenu;cost:Lnet/minecraft/world/inventory/DataSlot;")
	@Definition(id = "get", method = "Lnet/minecraft/world/inventory/DataSlot;get()I")
	@Expression("this.cost.get() > 0")
	@ModifyExpressionValue(method = "mayPickup", at = @At("MIXINEXTRAS:EXPRESSION"))
	public boolean insanelib$allowPickUpWithCost0(boolean original) {
		return true;
	}
}
