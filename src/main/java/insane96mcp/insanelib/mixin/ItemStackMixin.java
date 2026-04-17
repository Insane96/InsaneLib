package insane96mcp.insanelib.mixin;

import insane96mcp.insanelib.event.ILEventFactory;
import insane96mcp.insanelib.module.base.items.ItemComponentsReloadListener;
import insane96mcp.insanelib.setup.ILDataComponents;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.function.Consumer;

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
        DataComponentMap override = ItemComponentsReloadListener.PATCHED_COMPONENTS.get(getItem());
        if (override == null)
            return;
        cir.setReturnValue(PatchedDataComponentMap.fromPatch(override, this.components.asPatch()));
    }

    /**
     * Returns insanelib:enchantability component value when present, overriding the item's default enchantability
     */
    @Inject(method = "getEnchantmentValue", at = @At("RETURN"), cancellable = true)
    private void insanelib$onGetEnchantmentValue(CallbackInfoReturnable<Integer> cir) {
        Integer enchantability = ((ItemStack) (Object) this).get(ILDataComponents.ENCHANTABILITY.get());
        if (enchantability != null)
            cir.setReturnValue(enchantability);
    }

    @ModifyVariable(at = @At(value = "STORE", ordinal = 0), method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V", ordinal = 0, argsOnly = true)
    public int onHurtAmount(int amount, int pAmount, ServerLevel level, @Nullable LivingEntity livingEntity, Consumer<Item> consumer) {
        return ILEventFactory.getHurtAmount((ItemStack) (Object) this, amount, level.random, livingEntity);
    }
}
