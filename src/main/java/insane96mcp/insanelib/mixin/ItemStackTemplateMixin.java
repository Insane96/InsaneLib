package insane96mcp.insanelib.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.Codec;
import insane96mcp.insanelib.module.base.items.ItemComponentsFeature;
import net.minecraft.world.item.ItemStackTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStackTemplate.class)
public abstract class ItemStackTemplateMixin {
    @WrapOperation(method = "lambda$static$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ExtraCodecs;intRange(II)Lcom/mojang/serialization/Codec;"))
    private static Codec<Integer> insanelib$maxCountRange(int min, int max, Operation<Codec<Integer>> original) {
        return ItemComponentsFeature.stackSizeCodec(min);
    }
}
