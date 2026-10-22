package insane96mcp.insanelib.mixin;

import insane96mcp.insanelib.module.base.items.ItemComponentsReloadListener;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class ItemMixin {
    @Inject(method = "components", at = @At("HEAD"), cancellable = true)
    private void onComponents(CallbackInfoReturnable<DataComponentMap> cir) {
        DataComponentMap patched = ItemComponentsReloadListener.PATCHED_COMPONENTS.get((Item) (Object) this);
        if (patched != null)
            cir.setReturnValue(patched);
    }

    /**
     * getDefaultMaxStackSize reads the Item's `components` field directly, bypassing components() (and thus
     * onComponents above). Mods querying an item's stack size without an ItemStack instance (e.g. Storage
     * Drawers computing drawer capacity from an item prototype) call this and would otherwise see the vanilla
     * default instead of the patch applied by ItemComponentsFeature.
     */
    @Inject(method = "getDefaultMaxStackSize", at = @At("HEAD"), cancellable = true)
    private void onGetDefaultMaxStackSize(CallbackInfoReturnable<Integer> cir) {
        DataComponentMap patched = ItemComponentsReloadListener.PATCHED_COMPONENTS.get((Item) (Object) this);
        if (patched != null)
            cir.setReturnValue(patched.getOrDefault(DataComponents.MAX_STACK_SIZE, 1));
    }
}
