package insane96mcp.insanelib.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

/**
 * Fired when a food item is consumed, before its consume effects are applied.
 * <p>
 * Canceling the event will not apply the status effects of the food ({@code minecraft:apply_effects} consume effects
 * of the {@code minecraft:consumable} component). Other consume effects are still applied.
 */
public class AddEatEffectEvent extends LivingEvent implements ICancellableEvent {

    FoodProperties foodProperties;
    ItemStack stack;

    public AddEatEffectEvent(LivingEntity entity, FoodProperties foodProperties, ItemStack stack) {
        super(entity);
        this.foodProperties = foodProperties;
        this.stack = stack;
    }

    public ItemStack getStack() {
        return stack;
    }

    public FoodProperties getFoodProperties() {
        return foodProperties;
    }
}
