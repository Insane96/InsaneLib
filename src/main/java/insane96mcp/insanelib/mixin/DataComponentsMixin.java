package insane96mcp.insanelib.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanelib.module.base.items.ItemComponentsFeature;
import net.minecraft.core.component.DataComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DataComponents.class)
public abstract class DataComponentsMixin {
    @ModifyExpressionValue(method = "lambda$static$1", at = @At(value = "CONSTANT", args = "intValue=99"))
    private static int insanelib$maxStackSizeRange(int original) {
        return ItemComponentsFeature.stackLimit;
    }
}
