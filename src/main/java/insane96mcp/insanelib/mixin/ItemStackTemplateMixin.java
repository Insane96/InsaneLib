package insane96mcp.insanelib.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanelib.module.base.items.ItemComponentsFeature;
import net.minecraft.world.item.ItemStackTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStackTemplate.class)
public abstract class ItemStackTemplateMixin {
    @ModifyExpressionValue(method = "lambda$static$0", at = @At(value = "CONSTANT", args = "intValue=99"))
    private static int insanelib$maxCountRange(int original) {
        return ItemComponentsFeature.stackLimit;
    }
}
