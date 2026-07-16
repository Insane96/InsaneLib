package insane96mcp.insanelib.mixin;

import insane96mcp.insanelib.event.ILEventFactory;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Replaces the 1.21.1 injection into {@code LivingEntity#addEatEffect}, removed by the consumable rework.
 * Eat effects are now applied through this consume effect; the event only fires for actual food
 * (stack has the {@code FOOD} component), matching the old semantics.
 */
@Mixin(ApplyStatusEffectsConsumeEffect.class)
public class ApplyStatusEffectsConsumeEffectMixin {
    @Inject(method = "apply", at = @At("HEAD"), cancellable = true)
    private void insanelib$onAddEatEffect(Level level, ItemStack stack, LivingEntity user, CallbackInfoReturnable<Boolean> cir) {
        FoodProperties foodProperties = stack.get(DataComponents.FOOD);
        if (foodProperties != null && ILEventFactory.onAddEatEffect(user, foodProperties))
            cir.setReturnValue(false);
    }
}
