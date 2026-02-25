package insane96mcp.insanelib.mixin;

import insane96mcp.insanelib.module.base.items.ItemDefinitionsReloadListener;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow @Final PatchedDataComponentMap components;
    @Shadow public abstract Item getItem();

    /**
     * Intercepts the component map returned for this stack.
     * Returns a combined map where:
     *  1. Stack's own patch is applied first (enchantments, custom name, etc. — highest priority)
     *  2. Item definition override is the prototype (PATCHED_COMPONENTS)
     *  3. Vanilla prototype is embedded inside the override
     * This ensures existing stacks with stale prototypes see the correct values
     * without needing to be recreated after a reload.
     */
    @Inject(method = "getComponents", at = @At("HEAD"), cancellable = true)
    private void onGetComponents(CallbackInfoReturnable<DataComponentMap> cir) {
        DataComponentMap override = ItemDefinitionsReloadListener.PATCHED_COMPONENTS.get(getItem());
        if (override == null)
            return;
        cir.setReturnValue(PatchedDataComponentMap.fromPatch(override, this.components.asPatch()));
    }
}
