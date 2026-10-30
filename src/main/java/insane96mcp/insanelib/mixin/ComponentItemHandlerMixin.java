package insane96mcp.insanelib.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanelib.module.base.items.ItemComponentsFeature;
import net.neoforged.neoforge.items.ComponentItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ComponentItemHandler.class)
public abstract class ComponentItemHandlerMixin {
    @ModifyExpressionValue(method = "getSlotLimit", at = @At(value = "CONSTANT", args = "intValue=99"), remap = false)
    private int insanelib$slotLimit(int original) {
        return ItemComponentsFeature.getStackLimit();
    }
}
