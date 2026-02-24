package insane96mcp.insanelib.module.base;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.setup.ILAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;

@LoadFeature(description = "Adds a new `insanelib:push_resistance` attribute to mobs, making them harder to be pushed (or not pushable with 1) (this is not knockback resistance, this is for the bounding boxes of mobs intersecting). Disabling this will prevent the attribute from working. This doesn't work on players.")
public class PushResistance extends Feature {
    public static void attribute(EntityAttributeModificationEvent event) {
        for (EntityType<? extends LivingEntity> entityType : event.getTypes()) {
            if (!event.has(entityType, ILAttributes.PUSH_RESISTANCE))
                event.add(entityType, ILAttributes.PUSH_RESISTANCE);
        }
    }

    public static double getPushResistance(Entity entity) {
        if (!Feature.isEnabled(PushResistance.class)
                || !(entity instanceof LivingEntity living)
                || living.getAttribute(ILAttributes.PUSH_RESISTANCE) == null)
            return 1d;

        double resistance = living.getAttributeValue(ILAttributes.PUSH_RESISTANCE);
        return 1d - resistance;
    }
}
