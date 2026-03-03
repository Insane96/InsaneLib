package insane96mcp.insanelib.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

/**
 * Fired before eating effects are applied.
 * <p>
 * Canceling the event will not apply vanilla effects.
 */
public class AddEatEffectEvent extends LivingEvent implements ICancellableEvent {

    FoodProperties foodProperties;

    public AddEatEffectEvent(LivingEntity entity, FoodProperties foodProperties) {
        super(entity);
        this.foodProperties = foodProperties;
    }

    public FoodProperties getFoodProperties() {
        return foodProperties;
    }
}
