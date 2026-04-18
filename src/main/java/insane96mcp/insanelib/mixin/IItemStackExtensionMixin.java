package insane96mcp.insanelib.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import insane96mcp.insanelib.setup.ILDataComponents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IItemStackExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(IItemStackExtension.class)
public interface IItemStackExtensionMixin {
    /**
     * Returns insanelib:enchantability component value when present, overriding the item's default enchantability
     */
    @ModifyReturnValue(method = "getEnchantmentValue", at = @At("RETURN"))
    private int insanelib$onGetEnchantmentValue(int original) {
        Integer enchantability = ((ItemStack) (Object) this).get(ILDataComponents.ENCHANTABILITY.get());
        if (enchantability != null)
            return enchantability;
        return original;
    }
}
