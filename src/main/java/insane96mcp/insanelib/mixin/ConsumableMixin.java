package insane96mcp.insanelib.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.insanelib.event.ILEventFactory;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.function.Consumer;

@Mixin(Consumable.class)
public class ConsumableMixin {
	/**
	 * Fires {@link insane96mcp.insanelib.event.AddEatEffectEvent} when food is consumed. If canceled, the status effects
	 * of the food (minecraft:apply_effects consume effects) are not applied.
	 */
	@WrapOperation(method = "onConsume", at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V"))
	private void insanelib$onAddEatEffect(List<ConsumeEffect> effects, Consumer<ConsumeEffect> action, Operation<Void> original, @Local(argsOnly = true) LivingEntity user, @Local(argsOnly = true) ItemStack stack) {
		FoodProperties foodProperties = stack.get(DataComponents.FOOD);
		if (foodProperties == null || !ILEventFactory.onAddEatEffect(user, foodProperties, stack)) {
			original.call(effects, action);
			return;
		}
		original.call(effects.stream().filter(effect -> !(effect instanceof ApplyStatusEffectsConsumeEffect)).toList(), action);
	}
}
