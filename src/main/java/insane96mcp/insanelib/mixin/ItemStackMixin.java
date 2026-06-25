package insane96mcp.insanelib.mixin;

import insane96mcp.insanelib.event.ILEventFactory;
import insane96mcp.insanelib.module.base.items.ItemComponentsReloadListener;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Mutable @Shadow @Final PatchedDataComponentMap components;
    @Shadow public abstract Item getItem();

    @Inject(method = "getComponents", at = @At("HEAD"), cancellable = true)
    private void onGetComponents(CallbackInfoReturnable<DataComponentMap> cir) {
        DataComponentMap override = ItemComponentsReloadListener.PATCHED_COMPONENTS.get(getItem());
        if (override == null)
            return;
        PatchedDataComponentMap corrected = PatchedDataComponentMap.fromPatch(override, this.components.asPatch());
        // Update the field so direct accesses (copy(), serialization, etc.) also see the patched prototype.
        this.components = corrected;
        cir.setReturnValue(corrected);
    }

    /**
     * isSameItemSameComponents compares stack.components (the field) directly, bypassing getComponents().
     * This causes stacks created before PATCHED_COMPONENTS was populated (e.g. crafted items whose recipe
     * result was cached at startup) to have a stale vanilla prototype, making them compare unequal to
     * stacks created after population (e.g. loot drops) even though both appear identical via getComponents().
     * Redirecting through getComponents() ensures the patched view is used for all stack comparisons.
     */
    @Inject(method = "isSameItemSameComponents", at = @At("HEAD"), cancellable = true)
    private static void onIsSameItemSameComponents(ItemStack stack, ItemStack other, CallbackInfoReturnable<Boolean> cir) {
        if (!stack.is(other.getItem())) {
            cir.setReturnValue(false);
            return;
        }
        if (stack.isEmpty() && other.isEmpty()) {
            cir.setReturnValue(true);
            return;
        }
        cir.setReturnValue(Objects.equals(stack.getComponents(), other.getComponents()));
    }

    @ModifyVariable(at = @At(value = "STORE", ordinal = 0), method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V", ordinal = 0, argsOnly = true)
    public int onHurtAmount(int amount, int pAmount, ServerLevel level, @Nullable LivingEntity livingEntity, Consumer<Item> consumer) {
        return ILEventFactory.getHurtAmount((ItemStack) (Object) this, amount, level.random, livingEntity);
    }
}
