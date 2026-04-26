package insane96mcp.insanelib.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.insanelib.event.ILEventFactory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = IItemExtension.class, remap = false)
public interface IItemExtensionMixin {
    @ModifyReturnValue(method = "getMaxDamage", at = @At("RETURN"))
    default int insanelib$modifyMaxDamage(int original, @Local(argsOnly = true) ItemStack stack) {
        return ILEventFactory.getMaxDamage(stack, original);
    }
}