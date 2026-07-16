package insane96mcp.insanelib.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanelib.module.base.items.ItemComponentsFeature;
import net.minecraft.world.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Container.class)
public interface ContainerMixin {
    @ModifyExpressionValue(method = "getMaxStackSize()I", at = @At(value = "CONSTANT", args = "intValue=99"))
    default int insanelib$maxStackSize(int original) {
        return ItemComponentsFeature.stackLimit;
    }
}
