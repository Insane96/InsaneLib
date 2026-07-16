package insane96mcp.insanelib.mixin;

import insane96mcp.insanelib.module.base.items.ItemComponentsFeature;
import insane96mcp.insanelib.module.base.items.ItemComponentsReloadListener;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class ItemMixin {
    @Mutable
    @Shadow
    @Final
    public static int ABSOLUTE_MAX_STACK_SIZE;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void onClinit(CallbackInfo ci) {
        ABSOLUTE_MAX_STACK_SIZE = ItemComponentsFeature.stackLimit;
    }

    @Inject(method = "components", at = @At("HEAD"), cancellable = true)
    private void onComponents(CallbackInfoReturnable<DataComponentMap> cir) {
        DataComponentMap patched = ItemComponentsReloadListener.PATCHED_COMPONENTS.get((Item) (Object) this);
        if (patched != null)
            cir.setReturnValue(patched);
    }
}
